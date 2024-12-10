package cn.uhoc.launcher;


import cn.uhoc.domain.executor.model.entity.TaskBaseEntity;
import cn.uhoc.domain.executor.model.entity.TaskSetStageEntity;
import cn.uhoc.domain.observer.ObserverTypeEnum;
import cn.uhoc.domain.scheduler.model.entity.TaskCfgEntity;
import cn.uhoc.domain.scheduler.model.entity.TaskEntity;
import cn.uhoc.domain.scheduler.model.vo.TaskConstants;
import cn.uhoc.domain.scheduler.model.vo.TaskStatus;
import cn.uhoc.domain.scheduler.service.ITaskCfgService;
import cn.uhoc.domain.scheduler.service.ITaskService;
import cn.uhoc.domain.task.TaskRet;
import cn.uhoc.type.common.ReflectionUtils;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

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

    /**
     * 目前只有一个Lark类型任务
     */
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
                List<TaskBaseEntity> taskBaseList = fetchTasks(taskType);
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
    private List<TaskBaseEntity> fetchTasks(String taskType) {
        // 防止拉取任务过多来不及处理
        if (getAvailableCapacity(taskType) < scheduleLimitMap.get(taskType)) {
            return null;
        }
        // TODO 后期扩展redis使用，分布式部署，需要先整体学习redis
        try {
            List<TaskEntity> taskList = taskService.getTaskList(taskType, TaskStatus.PENDING.getStatus(), scheduleLimitMap.get(taskType));
            if (taskList == null || taskList.isEmpty()) {
                log.warn("No task to deal for taskType={}", taskType);
                return null;
            }
            List<TaskBaseEntity> taskBaseList = new ArrayList<>();
            observerManager.wakeupObserver(ObserverTypeEnum.onObtain, taskList, taskBaseList);
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
    protected void executeTasks(String taskType, List<TaskBaseEntity> taskBaseList) {
        observerManager.wakeupObserver(ObserverTypeEnum.onBoot);
        ThreadPoolExecutor taskExecutor = taskExecutorMap.get(taskType);
        if (taskExecutor != null) {
            // 线程池处理任务
            for (TaskBaseEntity taskBaseEntity : taskBaseList) {
                taskExecutor.execute(() -> executeTask(taskBaseEntity));
            }
        } else {
            log.error("No thread pool found for task type: {}", taskType);
        }
    }

    /**
     * 执行任务
     */
    private void executeTask(TaskBaseEntity task) {
        observerManager.wakeupObserver(ObserverTypeEnum.onExecute, task);
        TaskSetStageEntity taskSetStageEntity = executeTaskLogic(task);
        observerManager.wakeupObserver(ObserverTypeEnum.onFinish, task, taskSetStageEntity);
    }

    private ThreadPoolExecutor defaultThreadPoolExecutor(String taskType) {
        return new ThreadPoolExecutor(threadPoolConfig.getCorePoolSize(), threadPoolConfig.getMaxPoolSize(), threadPoolConfig.getKeepAliveTime(), TimeUnit.SECONDS, new LinkedBlockingQueue<>(threadPoolConfig.getBlockQueueSize()), new ThreadPoolExecutor.AbortPolicy());
    }

    /**
     * 任务执行逻辑
     */
    private TaskSetStageEntity executeTaskLogic(TaskBaseEntity task) {
        // 执行任务具体逻辑（具体反射调用等）
        Class<?> taskClass = taskRegistry.getClass(task.getTaskType());
        Method method = getTaskMethod(taskClass, task);
        TaskRet ret = invokeTaskMethod(taskClass, method, task);

        if (ret != null) {
            TaskSetStageEntity taskSetStageEntity = ret.getAsyncTaskSetStage();
            System.out.println("执行结果为：" + ret.getResult());
            return taskSetStageEntity;
        }
        return null;
    }

    private Method getTaskMethod(Class<?> taskClass, TaskBaseEntity task) {
        return ReflectionUtils.getMethod(taskClass, task.getTaskStage(), task.getTaskContext().getParamsClazz());
    }

    private TaskRet invokeTaskMethod(Class<?> taskClass, Method method, TaskBaseEntity task) {
        try {
            return (TaskRet) method.invoke(taskClass.newInstance(), task.getTaskContext().getParams());
        } catch (Exception e) {
            log.error("Error invoking method '{}' of task class '{}'. Task: '{}'. Exception: '{}'",
                    method.getName(), taskClass.getName(), task, e.getMessage(), e);
        }
        return null;
    }
}
