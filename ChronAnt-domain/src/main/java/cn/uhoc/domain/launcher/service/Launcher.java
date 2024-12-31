package cn.uhoc.domain.launcher.service;


import cn.uhoc.domain.convertor.TaskConverter;
import cn.uhoc.domain.launcher.entity.*;
import cn.uhoc.domain.manager.model.entity.TaskCfgEntity;
import cn.uhoc.domain.manager.model.entity.TaskEntity;
import cn.uhoc.domain.manager.model.entity.TaskPosEntity;
import cn.uhoc.domain.manager.model.vo.TaskConstants;
import cn.uhoc.domain.manager.model.vo.TaskStatus;
import cn.uhoc.domain.manager.service.ITaskCfgService;
import cn.uhoc.domain.manager.service.ITaskPosService;
import cn.uhoc.domain.manager.service.ITaskService;
import cn.uhoc.domain.observer.ObserverTypeEnum;
import cn.uhoc.domain.observer.TimeObserver;
import cn.uhoc.domain.register.impl.MultiStageAsyncTaskRegistry;
import cn.uhoc.trigger.api.dto.TaskSetReq;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Component
public class Launcher extends AbstractLauncher {

    @Resource
    private ITaskService taskService;

    @Resource
    private ITaskCfgService taskCfgService;

    @Resource
    private ITaskPosService taskPosService;

    public Launcher() {
        super();
    }

    @PostConstruct
    public void initLauncher() {
        try {
            registerObserver();
            initTaskTypeCfg();
            initTaskTypes();
            initTaskOffset();
            initIntervalTime();
            initScheduleLimit();
            initTaskExecuteThreadPool();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            checkDataConsistency();
        }
    }

    /**
     * 检查任务数据的一致性
     */
    private void checkDataConsistency() {
        // 检查以下所有 Map 是否包含所有任务类型
        for (String taskType : taskTypeSet) {
            // 检查任务配置
            if (!taskTypeCfgMap.containsKey(taskType)) {
                throw new IllegalStateException("任务配置缺失: 任务类型 [" + taskType + "] 没有对应的配置信息");
            }
            // 检查任务偏移量
            if (!taskOffsetMap.containsKey(taskType)) {
                throw new IllegalStateException("任务偏移量缺失: 任务类型 [" + taskType + "] 没有对应的偏移量配置");
            }
            // 检查任务间隔时间
            if (!intervalTimeMap.containsKey(taskType)) {
                throw new IllegalStateException("任务间隔时间缺失: 任务类型 [" + taskType + "] 没有对应的间隔时间配置");
            }
            // 检查拉取任务的限制
            if (!scheduleLimitMap.containsKey(taskType)) {
                throw new IllegalStateException("拉取任务限制缺失: 任务类型 [" + taskType + "] 没有对应的拉取限制配置");
            }
            // 检查任务执行线程池
            if (!taskExecutorMap.containsKey(taskType)) {
                throw new IllegalStateException("任务执行线程池缺失: 任务类型 [" + taskType + "] 没有对应的线程池配置");
            }
        }
        log.info("数据一致性检查完成，所有任务类型均已正确配置");
    }

    private void initTaskOffset() {
        List<TaskPosEntity> taskPosList = taskPosService.getTaskPosList();
        for (TaskPosEntity taskPosEntity : taskPosList) {
            taskOffsetMap.put(taskPosEntity.getTaskType(), new AtomicInteger(taskPosEntity.getScheduleBeginPos()));
        }
    }

    /**
     * 注册观察者
     */
    private void registerObserver() {
        observerManager.registerObserver(new TimeObserver());
    }

    /**
     * 加载任务类型的 Class对象
     */
    private void initTaskTypes() { // TODO 后期作为组件进行引入，需要扩展为根据注解进行识别
        // 注册任务类型名到任务Class对象的映射
        taskRegistry.init();
        // 初始化任务类型名称集合
        taskTypeSet = taskRegistry.getTaskTypeSet();
    }

    /**
     * 初始化任务一次性拉取限制
     */
    private void initScheduleLimit() {
        for (String taskType : taskTypeSet) {
            Integer limit = taskTypeCfgMap.get(taskType).getScheduleLimit();
            scheduleLimitMap.put(taskType, limit);
        }
    }

    /**
     * 加载任务配置信息
     */
    private void initTaskTypeCfg() {
        List<TaskCfgEntity> taskTypeCfgList = taskCfgService.getTaskTypeCfgList();
        for (TaskCfgEntity taskTypeCfg : taskTypeCfgList) {
            // 将新增的任务配置进行更新
            if (taskTypeCfgMap.containsKey(taskTypeCfg.getTaskType())) {
                continue;
            }
            taskTypeCfgMap.put(taskTypeCfg.getTaskType(), taskTypeCfg);
            // fixme 每次新任务配置更新后，还需要更新新任务其他的属性配置（线程池、拉取间隔、拉取限制...）
        }
    }

    /**
     * 初始化拉取任务的时间间隔
     */
    private void initIntervalTime() {
        for (String taskType : taskTypeSet) {
            TaskCfgEntity taskCfgEntity = taskTypeCfgMap.get(taskType);
            Integer interval = taskCfgEntity.getScheduleInterval() == 0 ? TaskConstants.DEFAULT_TIME_INTERVAL : taskCfgEntity.getScheduleInterval();
            intervalTimeMap.put(taskType, interval);
        }
    }

    /**
     * 初始化任务执行线程池
     */
    private void initTaskExecuteThreadPool() {
        // 初始化任务执行线程池
        for (String taskType : taskTypeSet) {
            taskExecutorMap.put(taskType, defaultThreadPoolExecutor(taskType));
        }
    }

    protected void refreshTaskFetchThreadPool(String taskType) {
        ScheduledExecutorService ses = defaultTaskFetchExecutor(taskType);
        Runnable command = processTask(taskType);
        // 设置任务拉取的调度策略
        ses.scheduleAtFixedRate(
                command,
                0,
                intervalTimeMap.get(taskType),
                TimeUnit.MILLISECONDS
        );
        // 统一管理任务拉取线程
        taskFetcherMap.put(taskType, ses);
    }

    private ScheduledExecutorService defaultTaskFetchExecutor(String taskType) {
        return Executors.newScheduledThreadPool(1); // TODO 学习Jdk提供的线程池：ScheduledExecutorService等
    }

    private Runnable processTask(String taskType) {
        Integer intervalTime = intervalTimeMap.get(taskType);
        return () -> {
            try {
                long begTime = System.currentTimeMillis();

                // 1. 拉取任务
                List<TaskBase> taskBaseList = fetchTasks(taskType);

                // 2. 判空检查
                if (taskBaseList == null || taskBaseList.isEmpty()) {
                    log.info("没有任务需要处理, 任务类型：{}", taskType);
                    return; // 任务列表为空，直接返回
                }

                // 2. 执行任务
                executeTasks(taskType, taskBaseList);

                long endTime = System.currentTimeMillis();

                // 3. 动态休眠
                long sleepTime = processDynamicSleep(begTime, endTime, intervalTime);
                log.debug("处理'{}'类型任务后，动态休眠'{}'ms", taskType, sleepTime);
            } catch (Exception e) {
                log.error("处理任务时发生错误, taskType: {}", taskType, e);
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
            int statusList = TaskStatus.NEVER_EXECUTED.getCode() | TaskStatus.PENDING.getCode();
            List<TaskEntity> taskList = taskService.getTaskList(taskType, statusList, scheduleLimitMap.get(taskType));
            if (taskList == null || taskList.isEmpty()) {
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
            log.error("未找到任务执行线程池, taskType: {}", taskType);
        }
    }

    /**
     * 执行任务
     */
    private void executeTask(TaskBase taskBase, String taskType) {
        observerManager.wakeupObserver(ObserverTypeEnum.onExecute, taskBase);
        TaskSetReq taskSetReq = null;
        TaskStageResult taskStageResult = null;
        try {
            // 自定义任务的核心执行逻辑
            taskStageResult = executeTaskLogic(taskBase, taskType);
            // 为空代表任务所有阶段都已经执行完毕，则需要修改状态
            if (taskStageResult.getMetadata() == null) {
                observerManager.wakeupObserver(ObserverTypeEnum.onFinish, taskBase);
                // fixme null 修改为 taskStageResult
                taskSetReq = createTaskSetReq(taskBase, TaskStatus.SUCCESS, null, "");
            } else {
                taskSetReq = createTaskSetReq(taskBase, TaskStatus.PENDING, taskStageResult, "");
            }
        } catch (Exception e) {
            observerManager.wakeupObserver(ObserverTypeEnum.onError, taskBase, e);
            // 根据当前重试情况，决定任务是直接标记为失败还是继续尝试
            if (taskBase.getMaxRetryNum() == 0 || taskBase.getCrtRetryNum() >= taskBase.getMaxRetryNum()) {
                taskSetReq = createTaskSetReq(taskBase, TaskStatus.FAIL, taskStageResult, e.getMessage());
                invoke(taskType, "handleError");
            } else {
                taskSetReq = createTaskSetReq(taskBase, TaskStatus.PENDING, taskStageResult, "Last Exception Information: \n" + e.getMessage());
                taskSetReq.updateCurrentRetryCount(taskBase.getCrtRetryNum() + 1);
            }
        } finally {
            // 更新任务信息
            taskService.setTask(taskSetReq);
        }
    }

    private ThreadPoolExecutor defaultThreadPoolExecutor(String taskType) {
        RejectedExecutionHandler handler;
        switch (threadPoolConfigProvider.getPolicy()) {
            case "DiscardPolicy":
                handler = new ThreadPoolExecutor.DiscardPolicy();
                break;
            case "DiscardOldestPolicy":
                handler = new ThreadPoolExecutor.DiscardOldestPolicy();
                break;
            case "CallerRunsPolicy":
                handler = new ThreadPoolExecutor.CallerRunsPolicy();
                break;
            case "AbortPolicy":
            default:
                handler = new ThreadPoolExecutor.AbortPolicy();
                break;
        }
        return new ThreadPoolExecutor(
                threadPoolConfigProvider.getCorePoolSize(),
                threadPoolConfigProvider.getMaxPoolSize(),
                threadPoolConfigProvider.getKeepAliveTime(),
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(threadPoolConfigProvider.getBlockQueueSize()),
                Executors.defaultThreadFactory(),
                handler
        );
    }

    /**
     * 执行任务具体逻辑
     */
    private TaskStageResult executeTaskLogic(TaskBase taskBase, String taskType) {
        Integer status = taskBase.getStatus();
        Object[] args = taskBase.getTaskContext().getParams();
        TaskStageResult taskStageResult;
        if (TaskStatus.NEVER_EXECUTED.getCode() == status) { // 如果任务从未执行，则调用 handleProcess 方法
            taskStageResult = invoke(taskType, "handleProcess", args);
        } else if (TaskStatus.PENDING.getCode() == status) { // 如果任务处于待执行状态，则调用任务的当前阶段方法
            taskStageResult = invoke(taskType, taskBase.getTaskStage(), args);
        } else if (TaskStatus.EXECUTING.getCode() == status) { // 如果任务正在执行，记录日志或采取相应措施
            throw new IllegalStateException("任务正在执行中，不允许重复执行！状态: EXECUTING");
        } else if (TaskStatus.SUCCESS.getCode() == status) { // 如果任务已成功执行，记录日志或返回成功的状态
            throw new IllegalStateException("任务已成功执行，无法重复执行！状态: SUCCESS");
        } else if (TaskStatus.FAIL.getCode() == status) { // 如果任务执行失败，则根据逻辑重新执行
            taskStageResult = invoke(taskType, "handleFailure");
        } else {
            throw new IllegalArgumentException("未知任务状态: " + status);
        }
        return taskStageResult;
    }

    private TaskStageResult invoke(String taskType, String stageMethodName, Object... args) {
        // 根据类型获取对应的任务代理处理器
        MultiStageAsyncTaskRegistry.TaskProxyHandler taskProxyHandler = MultiStageAsyncTaskRegistry.getTaskProxyMap().get(taskType);
        TaskStageResult result = taskProxyHandler.invoke(stageMethodName, args);
        return result;
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
            taskSetReq.setTaskStage(stageMeta.getNextTaskStage());
            taskSetReq.setTaskContext(JSON.toJSONString(stageMeta.getTaskContext()));

            // TODO 日志中也将每个阶段的元数据作为日志进行记录
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
