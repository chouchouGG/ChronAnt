package cn.uhoc.domain.observer;

import cn.uhoc.domain.executor.model.entity.TaskBase;
import cn.uhoc.domain.scheduler.model.entity.TaskEntity;
import cn.uhoc.domain.scheduler.service.ITaskService;
import cn.uhoc.type.common.UserConfig;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.List;

/**
 * 观察者
 */
@Slf4j
public class TimeObserver implements IObserver { // TODO 将控制台打印(System.out.println)改为日志输出

    // TODO 完善TimeObserver对于时间的日志记录（追加到任务的日志记录scheduleLog中）

    private Long beginTime;

    @Resource
    private ITaskService taskService;

    // 获取任务时改变任务状态
    @Override
    @ObserverStage(observerType = ObserverTypeEnum.onObtain)
    public void onObtain(List<TaskEntity> taskEntityList) {
        log.info("Starting to load context, number of tasks: {}", taskEntityList.size());
    }

    // 执行任务前做的动作，目前是简单打印
    @Override
    @ObserverStage(observerType = ObserverTypeEnum.onExecute)
    public void onExecute(TaskBase taskBase) {
        beginTime = System.currentTimeMillis();
        log.info("Task '{}' execution started, Task ID: '{}'", taskBase.getTaskType(), taskBase.getTaskId());
    }

    // 启动动作
    @Override
    @ObserverStage(observerType = ObserverTypeEnum.onBoot)
    public void onBoot() {
        log.info("Task started, User ID: {}, Current thread: {}, Fetching task", UserConfig.USERID, Thread.currentThread().getName());
    }

    // 任务执行完成做的动作
    @Override
    @ObserverStage(observerType = ObserverTypeEnum.onFinish)
    public void onFinish(TaskBase taskBase) {
        long costTime = System.currentTimeMillis() - beginTime;
        log.info("Task '{}' finished, time taken: {} ms", taskBase.getTaskType(), costTime);
    }

    // 执行任务失败时的动作，目前是本地重试
    @Override
    @ObserverStage(observerType = ObserverTypeEnum.onError)
    public void onError(TaskBase taskBase, Exception e) {
        log.error("Task '{}' failed, error message: {}", taskBase.getTaskType(), e.getMessage());
    }

    // 获取待定使用
    @Override
    @ObserverStage(observerType = ObserverTypeEnum.onStop)
    public void onStop(TaskBase taskBase) {
        log.info("Task '{}' stopped", taskBase.getTaskType());
    }
}
