package cn.uhoc.launcher;

import cn.uhoc.config.AsyncTaskWithMultiStageConfig;
import cn.uhoc.domain.executor.model.entity.TaskBaseEntity;
import cn.uhoc.domain.executor.model.entity.TaskSetStageEntity;
import cn.uhoc.domain.observer.ObserverManager;
import cn.uhoc.domain.observer.TimeObserver;
import cn.uhoc.domain.observer.vo.ObserverType;
import cn.uhoc.domain.scheduler.model.entity.TaskCfgEntity;
import cn.uhoc.type.common.UserConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


@EnableConfigurationProperties(AsyncTaskWithMultiStageConfig.class)  // 显式注册配置类
public abstract class AbstractTaskLauncher implements Launch {

    @Resource
    private AsyncTaskWithMultiStageConfig asyncTaskWithMultiStageConfig;

    /**
     * 要调度的任务类的包名，
     */
    public String packageName; // TODO （目前只有一个Lark类型任务），后期需要进行扩展为数组？

    /**
     * 拉取哪几类任务
     */
    Class taskType; // TODO 后期扩展应该使用列表管理多种任务类型

    /**
     * 拉取任务的初始偏移
     */
    AtomicInteger offset;

    /**
     * 观察者模式的观察管理者
     */
    @Resource
    ObserverManager observerManager; // TODO 学习观察者模式的代码

    /**
     * 拉取任务的间隔时间
     */
    protected int intervalTime;

    /**
     * 一次拉取多少个任务，用户配置
     */
    protected int scheduleLimit;

    /**
     * 多长时间拉取一次任务配置信息
     */
    public Long cycleScheduleConfigTime = 10000L;

    /**
     * 分布式锁的键
     */
    private static final String LOCK_KEY = "lock";

    /**
     * 存储任务配置信息
     */
    Map<String, TaskCfgEntity> taskTypeConfigMap;

    /**
     * 拉取任务的线程池
     */
    ScheduledExecutorService loadPool; // TODO 学习Jdk提供的线程池：ScheduledExecutorService等

    /**
     * 注入执行任务的线程池Bean
     */
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    // TODO 需要增加对初始化信息以日志形式打印在控制台
    public AbstractTaskLauncher() {
        // 初始化拉取任务的线程池
        loadPool = Executors.newScheduledThreadPool(1);
        // 注册观察者
        observerManager.registerObserver(new TimeObserver());
        // 初始化拉取任务的偏移
        offset = new AtomicInteger(asyncTaskWithMultiStageConfig.getInitOffsetForScheduleTask()); // TODO 思考这里使用原子整型的原因？
        // 拉取任务的时间周期
        cycleScheduleConfigTime = asyncTaskWithMultiStageConfig.getCycleUpdateScheduleTaskTypeConfigTime();
        // 存储任务配置信息
        taskTypeConfigMap = new ConcurrentHashMap<>();
        // 初始化，拉取任务配置信息
        init();
    }

    @Override
    public int init() {
        loadCfg();

        loadTaskTypes();

        initScheduleLimit();

        initIntervalTime();

        loadPool.scheduleAtFixedRate(this::loadCfg, cycleScheduleConfigTime, cycleScheduleConfigTime, TimeUnit.MILLISECONDS);

        return 0;
    }

    // 加载任务配置信息
    protected abstract void loadCfg();

    // 加载任务类型的 Class对象
    protected abstract void loadTaskTypes(); // TODO 后期作为组件进行引入，需要扩展为根据注解进行识别

    // 初始化任务一次性拉取限制
    protected abstract void initScheduleLimit();

    // 初始化拉取任务的时间间隔
    protected abstract void initIntervalTime();

    // 启动：拉取任务
    @Override
    public int start() {
        while (true) {
            // if语句，防止拉取过多任务，但是来不及处理的情况
            int availableCapacity = getAvailableCapacity();
            if (availableCapacity >= scheduleLimit) {
                executeTasks();
            }
            try {
                Thread.sleep(intervalTime + (int) (Math.random() * 500));
            } catch (InterruptedException e) {
                e.printStackTrace();  // TODO 采用更好的异常处理，而不是只单纯打印堆栈信息
            }
        }
    }

    // 获取线程池剩余容量
    private int getAvailableCapacity() {
        return UserConfig.QUEUE_SIZE - threadPoolExecutor.getQueue().size();// fixme 去掉QUEUE_SIZE
    }

    /**
     * 执行任务列表
     */
    protected void executeTasks() {
        try {
            observerManager.wakeupObserver(ObserverType.onBoot);
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        List<TaskBaseEntity> taskBaseList = fetchTasks();
        if (taskBaseList == null || taskBaseList.isEmpty()) {
            return;
        }
        for (TaskBaseEntity task : taskBaseList) {
            threadPoolExecutor.execute(() -> executeTask(task));
        }
    }

    /**
     * 获取任务列表
     * @return 解析后的任务信息实体
     */
    protected abstract List<TaskBaseEntity> fetchTasks();

    /**
     * 执行任务
     */
    private void executeTask(TaskBaseEntity task) {
        try {
            observerManager.wakeupObserver(ObserverType.onExecute, task);
            TaskSetStageEntity taskSetStageEntity = executeTaskLogic(task);
            observerManager.wakeupObserver(ObserverType.onFinish, task, taskSetStageEntity);
        } catch (InvocationTargetException | IllegalAccessException e) {
            handleTaskError(e, task);
        }
    }

    /**
     * 任务逻辑执行
     */
    protected abstract TaskSetStageEntity executeTaskLogic(TaskBaseEntity task);

    /**
     * 错误处理
     */
    private void handleTaskError(Exception e, TaskBaseEntity task) {
        try {
            observerManager.wakeupObserver(ObserverType.onError, task, e);
        } catch (InvocationTargetException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public int destroy() {
        return 0;
    }
}
