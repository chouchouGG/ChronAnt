package cn.uhoc.domain.observer;

import cn.uhoc.domain.convertor.TaskConverter;
import cn.uhoc.domain.executor.model.entity.*;
import cn.uhoc.domain.register.IMultiStageAsyncTaskRegistry;
import cn.uhoc.domain.scheduler.model.entity.TaskCfgEntity;
import cn.uhoc.domain.scheduler.model.entity.TaskEntity;
import cn.uhoc.domain.scheduler.model.vo.TaskStatus;
import cn.uhoc.domain.scheduler.service.ITaskService;
import cn.uhoc.domain.task.TaskRet;
import cn.uhoc.trigger.api.dto.TaskSetReqDTO;
import cn.uhoc.type.common.ReflectionUtils;
import cn.uhoc.type.common.UserConfig;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 观察者
 */
@Slf4j
public class TimeObserver implements IObserver { // TODO 将控制台打印(System.out.println)改为日志输出

    private Long beginTime;

    @Resource
    private IMultiStageAsyncTaskRegistry taskClassRegister;

    @Resource
    private ITaskService taskService;

    // 获取任务时改变任务状态
    @Override
    @ObserverStage(observerType = ObserverTypeEnum.onObtain)
    public void onObtain(List<TaskEntity> source, List<TaskBaseEntity> target) {
        log.info("Starting to load context, number of tasks: {}", source.size());
        convertModel(source, target);
        try {
            convertModel(source, target);
            log.info("Context loading completed, number of target tasks: {}", target.size());
        } catch (Exception e) {
            log.error("Error occurred while loading context: ", e);
        }
    }

    // 执行任务前做的动作，目前是简单打印
    @Override
    @ObserverStage(observerType = ObserverTypeEnum.onExecute)
    public void onExecute(TaskBaseEntity taskBaseEntity) {
        beginTime = System.currentTimeMillis();
        log.info("Task '{}' execution started, Task ID: {}", taskBaseEntity.getTaskType(), taskBaseEntity.getTaskId());
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
    public void onFinish(TaskBaseEntity taskBaseEntity, TaskSetStageEntity taskSetStageEntity, Class<?> aClass) {
        long costTime = System.currentTimeMillis() - beginTime;
        log.info("Task '{}' finished, time taken: {} ms", taskBaseEntity.getTaskType(), costTime);
        try {
            TaskSetReqDTO taskSetReqDTO = modifyTaskInfo(taskBaseEntity, TaskStatus.SUCCESS, taskSetStageEntity);
            taskSetReqDTO.setScheduleLog(getScheduleLog(taskBaseEntity, costTime, ""));
            if (Objects.isNull(taskSetStageEntity)) {
                reflectMethod(aClass, "handleFinish", new Object[0], new Class[0]);
            }
            setTaskNow(taskSetReqDTO);
        } catch (Exception e) {
            log.error("Error occurred when finishing task '{}' execution: ", taskBaseEntity.getTaskType(), e);
        }
    }

    // 执行任务失败时的动作，目前是本地重试
    @Override
    @ObserverStage(observerType = ObserverTypeEnum.onError)
    public void onError(TaskBaseEntity taskBaseEntity, TaskCfgEntity scheduleConfig, List<TaskBaseEntity> asyncTaskBaseList, Class<?> aClass, Exception e) {
//        if (taskBaseEntity.getCrt_retry_num() < 60) {
//            if (taskBaseEntity.getCrt_retry_num() != 0) {
//                taskBaseEntity.setMax_retry_num(taskBaseEntity.getCrt_retry_num() << 1);
//            }
//        } else {
//            taskBaseEntity.setMax_retry_interval(scheduleConfig.getRetry_interval());
//        }
//        if (taskBaseEntity.getMax_retry_interval() > scheduleConfig.getRetry_interval()) {
//            taskBaseEntity.setMax_retry_interval(scheduleConfig.getRetry_interval());
//        }
//        taskBaseEntity.getSchedule_log().getLastData().setErrMsg(e.getMessage());
//        if (taskBaseEntity.getMax_retry_num() == 0 || taskBaseEntity.getCrt_retry_num() >= taskBaseEntity.getMax_retry_num()) {
//            TaskSetReqDTO taskSetReqDTO = modifyStatus(taskBaseEntity, TaskStatus.FAIL);
//            taskSetReqDTO.setCrt_retry_num(taskBaseEntity.getCrt_retry_num());
//            taskSetReqDTO.setMax_retry_interval(taskBaseEntity.getMax_retry_interval());
//            taskSetReqDTO.setMax_retry_num(taskBaseEntity.getMax_retry_num());
//            setTaskNow(taskSetReqDTO);
//            return;
//        }
        log.error("Task '{}' failed, error message: {}", taskBaseEntity.getTaskType(), e.getMessage());

        TaskSetReqDTO taskSetReqDTO;
        if (taskBaseEntity.getMaxRetryNum() == 0 || taskBaseEntity.getCrtRetryNum() >= taskBaseEntity.getMaxRetryNum()) {
            taskSetReqDTO = modifyTaskInfo(taskBaseEntity, TaskStatus.FAIL, null);
            taskSetReqDTO.setScheduleLog(JSON.toJSONString(taskBaseEntity.getScheduleLog()));
            taskSetReqDTO.setCrtRetryNum(taskBaseEntity.getMaxRetryNum());
            reflectMethod(aClass, "handleError", new Object[0], new Class[0]);
        } else {
            taskSetReqDTO = modifyTaskInfo(taskBaseEntity, TaskStatus.PENDING, null);
            taskSetReqDTO.setScheduleLog(JSON.toJSONString(taskBaseEntity.getScheduleLog()));
            taskSetReqDTO.setCrtRetryNum(taskBaseEntity.getCrtRetryNum() + 1);

        }
        taskSetReqDTO.setOrderTime(System.currentTimeMillis() + ((long) scheduleConfig.getRetryInterval() << taskBaseEntity.getCrtRetryNum()));
        taskSetReqDTO.setMaxRetryInterval(taskBaseEntity.getMaxRetryInterval());
        taskSetReqDTO.setMaxRetryNum(taskBaseEntity.getMaxRetryNum());
        taskSetReqDTO.setScheduleLog(getScheduleLog(taskBaseEntity, System.currentTimeMillis() - beginTime, e.getMessage()));
        setTaskNow(taskSetReqDTO);
    }

    // 获取待定使用
    @Override
    @ObserverStage(observerType = ObserverTypeEnum.onStop)
    public void onStop(TaskBaseEntity taskBaseEntity) {
        log.info("Task '{}' stopped", taskBaseEntity.getTaskType());
    }

    private void convertModel(List<TaskEntity> source, List<TaskBaseEntity> target) {
        for (TaskEntity taskEntity : source) {
            TaskRet<TaskContextEntity> contextLoad = reflectMethod(
                    taskClassRegister.getClass(taskEntity.getTaskType()),
                    "contextLoad",
                    new Object[]{taskEntity.getTaskContext()},
                    new Class[]{String.class}
            );
            TaskBaseEntity taskBaseEntity = TaskConverter.toTaskBaseEntity(taskEntity, contextLoad);
            target.add(taskBaseEntity);
        }
    }

    private TaskRet reflectMethod(Class<?> clazz, String methodName, Object[] params, Class<?>[] paramTypes) {
        if (Objects.nonNull(clazz)) {
            Method method = ReflectionUtils.getMethod(clazz, methodName, paramTypes);
            try {
                Object instance = clazz.getDeclaredConstructor().newInstance();
                return (TaskRet) method.invoke(instance, params);
            } catch (InstantiationException | InvocationTargetException | IllegalAccessException |
                     NoSuchMethodException e) {
                log.error("Error occurred while invoking method '{}' via reflection: ", methodName, e);
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    // 修改任务状态
    private TaskSetReqDTO modifyTaskInfo(TaskBaseEntity taskBaseEntity, TaskStatus taskStatus, TaskSetStageEntity taskSetStageEntity) {
        return TaskSetReqDTO.builder()
                .taskId(taskBaseEntity.getTaskId())
                .taskStage(taskSetStageEntity != null ? taskSetStageEntity.getTaskStage() : taskBaseEntity.getTaskStage()).status(taskStatus.getStatus())
                .status(taskSetStageEntity != null ? TaskStatus.PENDING.getStatus() : taskStatus.getStatus())
                .taskContext(taskSetStageEntity != null ? JSON.toJSONString(taskSetStageEntity.getTaskContext()) : JSON.toJSONString(taskBaseEntity.getTaskContext()))
                .orderTime(taskSetStageEntity != null ? System.currentTimeMillis() - taskBaseEntity.getPriority() : taskBaseEntity.getOrderTime() - taskBaseEntity.getPriority())
                .priority(taskBaseEntity.getPriority())
                .crtRetryNum(taskBaseEntity.getCrtRetryNum())
                .maxRetryInterval(taskBaseEntity.getMaxRetryInterval())
                .build();
    }

    private String getScheduleLog(TaskBaseEntity taskBaseEntity, long costTime, String errMsg) {
        ScheduleLogEntity scheduleLogEntity = taskBaseEntity.getScheduleLog();
        // 1. 将上次日志数据加入历史数据
        ScheduleDataEntity lastLogData = scheduleLogEntity.getLastData();
        List<ScheduleDataEntity> historyLogData = scheduleLogEntity.getHistoryData();
        historyLogData.add(lastLogData);
        // 2. 设置新的日志数据
        ScheduleDataEntity newLogData = new ScheduleDataEntity(UUID.randomUUID() + "", errMsg, costTime + "");
        scheduleLogEntity.setLastData(newLogData);
        // 3. 返回JSON格式日志数据
        return JSON.toJSONString(scheduleLogEntity);
    }

    /**
     * 通过调度器修改任务
     */
    private void setTaskNow(TaskSetReqDTO taskSetReqDTO) {
        taskService.setTask(taskSetReqDTO);
    }
}
