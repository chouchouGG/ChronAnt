package cn.uhoc.launcher;


import cn.uhoc.domain.executor.model.entity.TaskBaseEntity;
import cn.uhoc.domain.executor.model.entity.TaskSetStageEntity;
import cn.uhoc.domain.observer.vo.ObserverType;
import cn.uhoc.domain.scheduler.model.entity.TaskCfgEntity;
import cn.uhoc.domain.scheduler.model.entity.TaskEntity;
import cn.uhoc.domain.scheduler.model.vo.TaskConstants;
import cn.uhoc.domain.scheduler.model.vo.TaskStatus;
import cn.uhoc.domain.scheduler.service.ITaskCfgService;
import cn.uhoc.domain.scheduler.service.ITaskService;
import cn.uhoc.domain.task.Lark;
import cn.uhoc.domain.task.TaskRet;
import cn.uhoc.type.common.ReflectionUtils;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class AppLaunch extends AbstractTaskLauncher {

    @Resource
    private ITaskService taskService;

    @Resource
    private ITaskCfgService taskCfgService;

    // 饿汉单例
    private static final AppLaunch instance = new AppLaunch();

    private AppLaunch() {
        super();
    }

    public static AppLaunch getInstance() {
        return instance;
    }

    /**
     * 目前只有一个Lark类型任务
     */
    @Override
    protected void loadTaskTypes() {
        taskType = Lark.class;
        packageName = taskType.getPackage().getName();
    }

    /**
     * 初始化任务拉取限制
     */
    @Override
    protected void initScheduleLimit() {
        scheduleLimit = taskTypeConfigMap.get(taskType.getSimpleName()).getScheduleLimit();
    }

    /**
     * 注册任务配置信息
     */
    @Override
    protected void loadCfg() {
        List<TaskCfgEntity> taskTypeCfgList = taskCfgService.getTaskTypeCfgList();
        for (TaskCfgEntity taskTypeCfg : taskTypeCfgList) {
            taskTypeConfigMap.put(taskTypeCfg.getTaskType(), taskTypeCfg);
        }
    }

    @Override
    protected void initIntervalTime() {
        // 读取对应任务配置信息
        TaskCfgEntity scheduleConfig = taskTypeConfigMap.get(taskType.getSimpleName());
        // TODO 如果有多种任务时，应该考虑给每个类型任务对应一个时间间隔的配置
        intervalTime = scheduleConfig.getScheduleInterval() == 0 ? TaskConstants.DEFAULT_TIME_INTERVAL : scheduleConfig.getScheduleInterval();
    }

    @Override
    protected List<TaskBaseEntity> fetchTasks() {
        // TODO 后期扩展redis的使用，需要先整体学习redis
//        // 分布式锁的参数
//        LockParam lockParam = new LockParam(LOCK_KEY);
//        // 分布式锁
//        RedisLock redisLock = new RedisLock(lockParam);
        try {
//            // 上锁
//            if (redisLock.lock()) {
                List<TaskEntity> taskList = taskService.getTaskList(taskType.getSimpleName(), TaskStatus.PENDING.getStatus(), scheduleLimit);
                if (taskList == null || taskList.isEmpty()) {
                    log.warn("No task to deal");
                    return null;
                }
                List<TaskBaseEntity> taskBaseList = new ArrayList<>();
                observerManager.wakeupObserver(ObserverType.onObtain, taskList, taskBaseList);
                return taskBaseList;
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        finally {
//            // 释放锁
//            redisLock.unlock();
//        }
        return null;
    }

    @Override
    protected TaskSetStageEntity executeTaskLogic(TaskBaseEntity task) {
        // 执行任务具体逻辑（具体反射调用等）
        Class<?> taskClass = getTaskClass(task);
        Method method = getTaskMethod(taskClass, task);
        TaskRet returnVal = invokeTaskMethod(taskClass, method, task);

        if (returnVal != null) {
            TaskSetStageEntity taskSetStageEntity = returnVal.getAsyncTaskSetStage();
            System.out.println("执行结果为：" + returnVal.getResult());
            return taskSetStageEntity;
        }
        return null;
    }

    private Class<?> getTaskClass(TaskBaseEntity task) {
        try {
            return Class.forName(packageName + "." + task.getTaskType());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
        return null;
    }
}
