package cn.uhoc.domain.convertor;

import cn.uhoc.domain.executor.model.entity.ScheduleLogEntity;
import cn.uhoc.domain.executor.model.entity.TaskBaseEntity;
import cn.uhoc.domain.executor.model.entity.TaskContextEntity;
import cn.uhoc.domain.scheduler.model.entity.TaskEntity;
import cn.uhoc.domain.task.TaskRet;
import cn.uhoc.trigger.api.dto.TaskResDTO;
import cn.uhoc.type.common.ReflectionUtils;
import cn.uhoc.type.exception.ReflectionException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * Task相关实体类的转换器（工具类）
 */
@Slf4j
public class TaskConverter {

    public static TaskBaseEntity toTaskBaseEntity(TaskEntity taskEntity, Class<?> taskClazz) {
        if (taskEntity == null) {
            return null;
        }
        TaskBaseEntity taskBaseEntity = TaskBaseEntity.builder()
                .userId(taskEntity.getUserId())
                .taskId(taskEntity.getTaskId())
                .taskType(taskEntity.getTaskType())
                .taskStage(taskEntity.getTaskStage())
                .status(taskEntity.getStatus())
                .crtRetryNum(taskEntity.getCrtRetryNum())
                .maxRetryNum(taskEntity.getMaxRetryNum())
                .maxRetryInterval(taskEntity.getMaxRetryInterval())
                .scheduleLog(JSON.parseObject(taskEntity.getScheduleLog(), ScheduleLogEntity.class))
                .build();
        TaskRet<TaskContextEntity> contextLoad = null;
        try {
            contextLoad = ReflectionUtils.reflectMethod(taskClazz, "contextLoad", new Object[]{taskEntity.getTaskContext()}, new Class[]{String.class});
        } catch (ReflectionException e) {
            log.error("Error occurred while invoking 'contextLoad' in class {}: {}", taskClazz.getName(), e.getMessage(), e);
            throw new RuntimeException("Failed to load context via reflection: " + e.getMessage(), e);
        }
        if (Objects.nonNull(contextLoad)) {
            taskBaseEntity.setTaskContext(contextLoad.getResult());
        } else {
            // 设置为上次的context
            taskBaseEntity.setTaskContext(JSON.parseObject(taskEntity.getTaskContext(), TaskContextEntity.class));
        }
        return taskBaseEntity;
    }

    public static TaskEntity toTaskEntity(TaskBaseEntity taskBaseEntity) {
        if (taskBaseEntity == null) {
            return null;
        }
        return TaskEntity.builder()
                .userId(taskBaseEntity.getUserId())
                .taskId(taskBaseEntity.getTaskId())
                .taskType(taskBaseEntity.getTaskType())
                .taskStage(taskBaseEntity.getTaskStage())
                .status(taskBaseEntity.getStatus())
                .crtRetryNum(taskBaseEntity.getCrtRetryNum())
                .maxRetryNum(taskBaseEntity.getMaxRetryNum())
                .orderTime(taskBaseEntity.getOrderTime())
                .priority(taskBaseEntity.getPriority())
                .maxRetryInterval(taskBaseEntity.getMaxRetryInterval())
                .scheduleLog(taskBaseEntity.getScheduleLog().toString())
                .taskContext(taskBaseEntity.getTaskContext().toString())
                .build();
    }

    /**
     * Entity转为DTO
     */
    public static TaskResDTO toDTO(TaskEntity task) {
        if (task == null) {
            return null;
        }
        return TaskResDTO.builder()
                .userId(task.getUserId())
                .taskId(task.getTaskId())
                .taskType(task.getTaskType())
                .taskStage(task.getTaskStage())
                .status(task.getStatus())
                .crtRetryNum(task.getCrtRetryNum())
                .maxRetryNum(task.getMaxRetryNum())
                .maxRetryInterval(task.getMaxRetryInterval())
                .scheduleLog(task.getScheduleLog())
                .taskContext(task.getTaskContext())
                .build();
    }
}
