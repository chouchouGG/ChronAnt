package cn.uhoc.domain.executor.service;

import cn.uhoc.domain.observer.ObserverManager;
import cn.uhoc.domain.observer.TimeObserver;
import cn.uhoc.domain.register.IMultiStageAsyncTaskRegistry;
import cn.uhoc.domain.manager.model.entity.TaskCfgEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
@Component
public abstract class AbstractLauncher implements IExecutorService {

    @Resource
    protected ThreadPoolConfigProvider threadPoolConfigProvider;

    /**
     * 任务注册器
     */
    @Resource
    protected IMultiStageAsyncTaskRegistry taskRegistry;

    /**
     * 观察者模式的观察管理者
     */
    @Resource
    protected ObserverManager observerManager; // TODO 学习观察者模式的代码

    /**
     * 拉取哪几类任务
     */
    protected Set<String> taskTypeSet; // TODO 后期扩展应该使用列表管理多种任务类型

    /**
     * 拉取任务的初始偏移
     */
    protected AtomicInteger initOffset;

    /**
     * 拉取任务的间隔时间
     */
    protected Map<String, Integer> intervalTimeMap = new HashMap<>();

    /**
     * 一次拉取多少个任务，用户配置
     */
    protected Map<String, Integer> scheduleLimitMap = new HashMap<>();

    /**
     * 多长时间拉取一次任务配置信息
     */
    protected Long updateTaskConfigPeriod;

//    /**
//     * 分布式锁的键
//     */
//    protected static final String LOCK_KEY = "lock";

    /**
     * 存储任务配置信息
     */
    protected Map<String, TaskCfgEntity> taskTypeCfgMap;

    /**
     * 拉取任务的线程池
     */
    protected ScheduledExecutorService loadPool; // TODO 学习Jdk提供的线程池：ScheduledExecutorService等

    /**
     * 任务类型到线程池的映射
     */
    protected Map<String, ThreadPoolExecutor> taskExecutorMap = new HashMap<>();

    /**
     * 任务拉取线程池
     */
    protected Map<String, ScheduledExecutorService> taskFetcherMap = new HashMap<>();


    // TODO 需要增加对初始化信息以日志形式打印在控制台
    public AbstractLauncher() {
        // 初始化拉取任务的线程池
        loadPool = Executors.newScheduledThreadPool(1);
        // 注册观察者
        observerManager.registerObserver(new TimeObserver());
        // 初始化拉取任务的偏移
        initOffset = new AtomicInteger(0); // TODO 思考这里使用原子整型的原因？
        // 初始化拉取任务配置的时间周期
        updateTaskConfigPeriod = 60L;
        // 存储任务配置信息
        taskTypeCfgMap = new ConcurrentHashMap<>();
        // 初始化，拉取任务配置信息
        init();
    }

    @Override
    public int init() {
        loadCfg();

        registerTaskTypes();

        initScheduleLimit();

        initIntervalTime();

        initTaskExecuteThreadPool();

        loadPool.scheduleAtFixedRate(this::loadCfg, updateTaskConfigPeriod, updateTaskConfigPeriod, TimeUnit.SECONDS);

        return 0;
    }

    /**
     * 启动：拉取任务
     * <p>关于任务的拉取方案：目前是为每类任务都配备一个任务拉取线程池。</p>
     */
    @Override
    public int start() {
        while (true) {
            for (String taskType : taskTypeSet) {
                refreshTaskFetchThreadPool(taskType);
            }
        }
    }

    @Override
    public int destroy() {
        return 0;
    }

    /**
     * 加载任务配置信息
     */
    protected abstract void loadCfg();

    /**
     * 加载任务类型的 Class对象
     */
    protected abstract void registerTaskTypes(); // TODO 后期作为组件进行引入，需要扩展为根据注解进行识别

    /**
     * 初始化任务一次性拉取限制
     */
    protected abstract void initScheduleLimit();

    /**
     * 初始化拉取任务的时间间隔
     */
    protected abstract void initIntervalTime();

    /**
     * 初始化任务执行线程池
     */
    protected abstract void initTaskExecuteThreadPool();

    /**
     * 初始化任务拉取线程池并启动任务处理
     */
    protected abstract void refreshTaskFetchThreadPool(String taskType);

}
