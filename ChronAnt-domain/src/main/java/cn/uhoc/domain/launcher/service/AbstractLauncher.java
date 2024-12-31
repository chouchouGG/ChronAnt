package cn.uhoc.domain.launcher.service;

import cn.uhoc.domain.manager.model.entity.TaskCfgEntity;
import cn.uhoc.domain.observer.ObserverManager;
import cn.uhoc.domain.register.IMultiStageAsyncTaskRegistry;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
public abstract class AbstractLauncher implements ILauncherService {

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
     * 拉取任务的初始偏移，每个任务类型有自己的偏移量
     */
    protected Map<String, AtomicInteger> taskOffsetMap = new ConcurrentHashMap<>();
    /**
     * 拉取任务的间隔时间：单位为 ms
     */
    protected Map<String, Integer> intervalTimeMap = new HashMap<>();
    /**
     * 一次拉取多少个任务，用户配置
     */
    protected Map<String, Integer> scheduleLimitMap = new HashMap<>();
    /**
     * 存储任务配置信息
     */
    protected Map<String, TaskCfgEntity> taskTypeCfgMap = new ConcurrentHashMap<>();
    /**
     * 任务执行线程池
     */
    protected Map<String, ThreadPoolExecutor> taskExecutorMap = new HashMap<>();
    /**
     * 任务拉取线程池，任务拉取线程一经初始化就自动开始周期性的任务拉取，即整个服务开始启动执行
     */
    protected Map<String, ScheduledExecutorService> taskFetcherMap = new HashMap<>();


    // TODO 需要增加对初始化信息以日志形式打印在控制台
    public AbstractLauncher() {
        log.info("Initialization completed.");
    }

    /**
     * 任务调度启动器入口
     * <p>关于任务的拉取方案：目前是为每类任务都配备一个任务拉取线程池。</p>
     */
    @Override
    public int start() {
        // 依次启动所有的任务拉取线程池
        for (String taskType : taskTypeSet) {
            refreshTaskFetchThreadPool(taskType);
        }
        return 0;
    }

    @Override
    public int destroy() {
        return 0;
    }


    /**
     * 初始化任务拉取线程池并启动任务处理
     */
    protected abstract void refreshTaskFetchThreadPool(String taskType);

}
