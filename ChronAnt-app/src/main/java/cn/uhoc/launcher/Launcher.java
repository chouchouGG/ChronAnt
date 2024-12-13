package cn.uhoc.launcher;


import cn.uhoc.domain.convertor.TaskConverter;
import cn.uhoc.domain.executor.model.entity.*;
import cn.uhoc.domain.observer.ObserverTypeEnum;
import cn.uhoc.domain.scheduler.model.entity.TaskCfgEntity;
import cn.uhoc.domain.scheduler.model.entity.TaskEntity;
import cn.uhoc.domain.scheduler.model.vo.TaskConstants;
import cn.uhoc.domain.scheduler.model.vo.TaskStatus;
import cn.uhoc.domain.scheduler.service.ITaskCfgService;
import cn.uhoc.domain.scheduler.service.ITaskService;
import cn.uhoc.trigger.api.dto.TaskSetReq;
import cn.uhoc.type.common.ReflectionUtils;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
public class Launcher extends AbstractLauncher {

    @Resource
    private ITaskService taskService;

    @Resource
    private ITaskCfgService taskCfgService;

    // 饿汉单例
    private static final Launcher instance = new Launcher();

    private Launcher() {
        super();
    }

    public static Launcher getInstance() {
        return instance;
    }

    @Override
    protected void registerTaskTypes() {
        taskRegistry.init();
    }

    /**
     * 初始化任务拉取限制
     */
    @Override
    protected void initScheduleLimit() {
        for (String taskType : taskTypeSet) {
            Integer limit = taskTypeConfigMap.get(taskType).getScheduleLimit();
            scheduleLimitMap.put(taskType, limit);
        }
    }

    /**
     * 注册任务配置信息
     */
    @Override
    protected void loadCfg() {
        List<TaskCfgEntity> taskTypeCfgList = taskCfgService.getTaskTypeCfgList();
        for (TaskCfgEntity taskTypeCfg : taskTypeCfgList) {
            // 将新增的任务配置进行更新
            if (taskTypeConfigMap.containsKey(taskTypeCfg.getTaskType())) {
                continue;
            }
            taskTypeConfigMap.put(taskTypeCfg.getTaskType(), taskTypeCfg);
            // fixme 每次新任务配置更新后，还需要更新新任务其他的属性配置（线程池、拉取间隔、拉取限制...）
        }
    }

    @Override
    protected void initIntervalTime() {
        for (String taskType : taskTypeSet) {
            TaskCfgEntity taskCfgEntity = taskTypeConfigMap.get(taskType);
            Integer interval = taskCfgEntity.getScheduleInterval() == 0 ? TaskConstants.DEFAULT_TIME_INTERVAL : taskCfgEntity.getScheduleInterval();
            intervalTimeMap.put(taskType, interval);
        }
    }

    @Override
    protected void initTaskExecuteThreadPool() {
        // 初始化任务执行线程池
        for (String taskType : taskTypeSet) {
            taskExecutorMap.put(taskType, defaultThreadPoolExecutor(taskType));
        }
    }

    @Override
    protected void refreshTaskFetchThreadPool(String taskType) {
        ScheduledExecutorService scheduledExecutorService = defaultTaskFetchExecutor(taskType);
        // 设置任务拉取调度策略
        setSchedulePolicy(scheduledExecutorService, taskType);
        // 管理任务拉取线程池
        taskFetcherMap.put(taskType, scheduledExecutorService);
    }

    private ScheduledExecutorService defaultTaskFetchExecutor(String taskType) {
        return Executors.newScheduledThreadPool(1);
    }

    private void setSchedulePolicy(ScheduledExecutorService scheduledExecutorService, String taskType) {
        scheduledExecutorService.scheduleAtFixedRate(processTask(taskType), 0, intervalTimeMap.get(taskType), TimeUnit.MILLISECONDS);
    }

    private Runnable processTask(String taskType) {
        Integer intervalTime = intervalTimeMap.get(taskType);
        return () -> {
            try {
                long begTime = System.currentTimeMillis();
                // 1. 拉取任务
                List<TaskBase> taskBaseList = fetchTasks(taskType);
                // 2. 执行任务
                executeTasks(taskType, taskBaseList);
                long endTime = System.currentTimeMillis();
                // 3. 动态休眠
                long sleepTime = processDynamicSleep(begTime, endTime, intervalTime);
                log.debug("Dynamic sleep for '{}'ms after processing tasks for taskType='{}'", sleepTime, taskType);
            } catch (Exception e) {
                log.error("Error occurred while processing tasks for {}", taskType, e);
            }
        };
    }

    /**
     * <h2>动态调整任务拉取间隔时间的机制</h2>
     * <p><strong>1. intervalTime：</strong>每次任务拉取的基础间隔时间，用于确保系统具备稳定的调度节奏。</p>
     * <p><strong>2. Math.random() * 500：</strong>通过引入随机性，避免多个实例完全同步拉取任务。</p>
     * <p><strong>3. elapsedTime：</strong>本次任务拉取和处理所消耗的时间。</p>
     *
     * <p><strong>4. 结果逻辑：</strong></p>
     * <ol>
     *     <li>当 elapsedTime &lt; intervalTime：线程休眠一段时间（intervalTime - elapsedTime + 随机延迟），弥补剩余的拉取间隔。</li>
     *     <li>当 elapsedTime &gt;= intervalTime：跳过休眠，直接开始下一次任务拉取。</li>
     * </ol>
     */
    private long processDynamicSleep(long begTime, long endTime, Integer intervalTime) {
        // 本次任务拉取耗时
        long elapsedTime = endTime - begTime;
        // 动态间隔计算公式
        int dynamicSleepTime = intervalTime + (int) (Math.random() * 500) - (int) elapsedTime;
        if (dynamicSleepTime <= 0) return 0;
        try {
            Thread.sleep(dynamicSleepTime);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return dynamicSleepTime;
    }

    /**
     * 获取任务列表
     *
     * @return 解析后的任务信息实体
     */
    private List<TaskBase> fetchTasks(String taskType) {
        // 防止拉取任务过多来不及处理
        if (getAvailableCapacity(taskType) < scheduleLimitMap.get(taskType)) {
            return null;
        }
        // TODO 后期扩展redis使用，分布式部署，需要先整体学习redis
        try {
            List<TaskEntity> taskList = taskService.getTaskList(taskType, TaskStatus.PENDING.getCode(), scheduleLimitMap.get(taskType));
            if (taskList == null || taskList.isEmpty()) {
                log.warn("No taskBase to deal for taskType={}", taskType);
                return null;
            }
            observerManager.wakeupObserver(ObserverTypeEnum.onObtain, taskList);
            Class<?> taskClazz = taskRegistry.getTaskClass(taskType);
            List<TaskBase> taskBaseList = taskList.stream()
                    .map(taskEntity -> TaskConverter.toTaskBaseEntity(taskEntity, taskClazz))
                    .collect(Collectors.toList());
            return taskBaseList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取线程池剩余容量
     */
    private int getAvailableCapacity(String taskType) {
        ThreadPoolExecutor threadPoolExecutor = taskExecutorMap.get(taskType);
        return threadPoolExecutor != null ? threadPoolExecutor.getQueue().remainingCapacity() : 0;
    }

    /**
     * 执行任务列表
     */
    protected void executeTasks(String taskType, List<TaskBase> taskBaseList) {
        observerManager.wakeupObserver(ObserverTypeEnum.onBoot);
        ThreadPoolExecutor taskExecutor = taskExecutorMap.get(taskType);
        if (taskExecutor != null) {
            // 线程池处理任务
            for (TaskBase taskBase : taskBaseList) {
                taskExecutor.execute(() -> executeTask(taskBase, taskType));
            }
        } else {
            log.error("No thread pool found for taskBase type: {}", taskType);
        }
    }

    /**
     * 执行任务
     */
    private void executeTask(TaskBase taskBase, String taskType) {
        Class<?> taskClazz = taskRegistry.getTaskClass(taskType);
        observerManager.wakeupObserver(ObserverTypeEnum.onExecute, taskBase);
        TaskSetReq taskSetReq;
        try {
            TaskStageResult taskStageResult = executeTaskLogic(taskBase, taskType);
            taskSetReq = postProcessTask(taskBase, taskStageResult, taskClazz);
        } catch (Exception e) {
            observerManager.wakeupObserver(ObserverTypeEnum.onError, e);
            taskSetReq = errProcessTask(taskBase, taskType, e);
        }
        // 更新任务
        taskService.setTask(taskSetReq);
    }

    /**
     * 任务成功执行的后置处理
     */
    private TaskSetReq postProcessTask(TaskBase taskBase, TaskStageResult taskStageResult, Class<?> taskClazz) {
        TaskSetReq taskSetReq;
        // 为空代表任务所有阶段都已经执行完毕，则需要修改状态
        if (taskStageResult == null) {
            observerManager.wakeupObserver(ObserverTypeEnum.onFinish, taskBase);
            taskSetReq = createTaskSetReq(taskBase, TaskStatus.SUCCESS, null, "");
            ReflectionUtils.reflectMethod(taskClazz, "handleFinish", new Object[]{}, new Class[]{});
        } else {
            taskSetReq = createTaskSetReq(taskBase, TaskStatus.PENDING, taskStageResult, "");
        }
        return taskSetReq;
    }

    private ThreadPoolExecutor defaultThreadPoolExecutor(String taskType) {
        return new ThreadPoolExecutor(
                threadPoolConfig.getCorePoolSize(),
                threadPoolConfig.getMaxPoolSize(),
                threadPoolConfig.getKeepAliveTime(),
                TimeUnit.SECONDS, new LinkedBlockingQueue<>(threadPoolConfig.getBlockQueueSize()),
                new ThreadPoolExecutor.AbortPolicy()
        );
    }

    /**
     * 执行任务具体逻辑（具体反射调用等）
     */
    private TaskStageResult executeTaskLogic(TaskBase taskBase, String taskType) {
        Class<?> taskClazz = taskRegistry.getTaskClass(taskBase.getTaskType());
        Method method = ReflectionUtils.getMethod(taskClazz, taskBase.getTaskStage(), taskBase.getTaskContext().getParamsClazz());
        TaskStageResult taskStageResult = ReflectionUtils.invokeMethod(taskClazz, method, taskBase.getTaskContext().getParams());
        return taskStageResult;
    }

    private TaskSetReq errProcessTask(TaskBase taskBase, String taskType, Exception e) {
        TaskSetReq taskSetReq;
        // 根据当前重试情况，决定任务是直接标记为失败还是继续尝试
        if (taskBase.getMaxRetryNum() == 0 || taskBase.getCrtRetryNum() >= taskBase.getMaxRetryNum()) {
            taskSetReq = createTaskSetReq(taskBase, TaskStatus.FAIL, null, e.getMessage());
            ReflectionUtils.reflectMethod(taskRegistry.getTaskClass(taskType), "handleError", new Object[0], new Class[0]);
        } else {
            taskSetReq = createTaskSetReq(taskBase, TaskStatus.PENDING, null, "Last Exception Information: \n" + e.getMessage())
                    .updateCurrentRetryCount(taskBase.getCrtRetryNum() + 1);
        }
        return taskSetReq;
    }

    private TaskSetReq createTaskSetReq(TaskBase taskBase, TaskStatus taskStatus, TaskStageResult taskStageResult, String errMsg) {
        // TODO 暂时没有设置 ‘TaskSetReq’ 中的某些字段，后续扩展考虑提供更为灵活的设置
        TaskSetReq taskSetReq = TaskSetReq.builder()
                .taskId(taskBase.getTaskId()) // 设置任务ID
                .status(taskStatus.getCode()) // 设置任务状态
                .build();
        if (taskStageResult != null) {
            // 设置 ’任务阶段‘ 和 ’任务上下文‘
            TaskStageMeta stageMeta = taskStageResult.getMetadata();
            taskSetReq.setTaskStage(stageMeta.getTaskStage());
            taskSetReq.setTaskContext(JSON.toJSONString(stageMeta.getTaskContext()));

            // 设置 ’调度日志‘
            String result = JSON.toJSONString(taskStageResult.getResult());
            String scheduleLog = getScheduleLog(taskBase.getScheduleLog(), result, getErrMsg(taskStatus, errMsg));
            taskSetReq.setScheduleLog(scheduleLog);
        }
        return taskSetReq;
    }

    private String getErrMsg(TaskStatus taskStatus, String errMsg) {
        if (errMsg == null) {
            return TaskStatus.getErrMsg(taskStatus);
        } else {
            return TaskStatus.getErrMsg(taskStatus) + errMsg;
        }
    }

    /**
     * fixme 获取排序时间的算法
     */
    private Long getOrderTime(TaskBase taskBase, TaskStageMeta taskStageMeta) {
//        return taskStageMeta != null ? System.currentTimeMillis() - taskBase.getPriority() : taskBase.getOrderTime() - taskBase.getPriority();
        return null;
    }

    /**
     * 构建调度日志
     *
     * @param scheduleLog 任务日志实体对象
     * @param result      用户自定义每个任务阶段的结果消息
     * @param errMsg      错误信息
     * @return JSON字符串格式的日志数据
     */
    private String getScheduleLog(ScheduleLog scheduleLog, String result, String errMsg) {
        // 将上次日志数据加入历史数据
        List<ScheduleData> history = scheduleLog.getHistoryData();
        ScheduleData last = scheduleLog.getLastData();
        history.add(last);
        // 构建新的日志数据
        ScheduleData newData = ScheduleData.builder()
                .traceId(String.valueOf(UUID.randomUUID()))
                .errMsg(errMsg)
                .result(result)
                .build();
        scheduleLog.setLastData(newData);
        return JSON.toJSONString(scheduleLog);
    }

}
