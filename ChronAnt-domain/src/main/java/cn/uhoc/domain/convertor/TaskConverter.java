package cn.uhoc.domain.convertor;

import cn.uhoc.domain.launcher.entity.ScheduleLog;
import cn.uhoc.domain.launcher.entity.TaskBase;
import cn.uhoc.domain.launcher.entity.TaskContext;
import cn.uhoc.domain.manager.model.entity.TaskEntity;
import cn.uhoc.trigger.api.dto.TaskRes;
import cn.uhoc.type.common.ReflectionUtils;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * Task相关实体类的转换器（工具类）
 */
@Slf4j
public class TaskConverter {

    public static TaskBase toTaskBaseEntity(TaskEntity taskEntity, Class<?> taskClazz) {
        if (taskEntity == null) {
            return null;
        }
        TaskBase taskBase = TaskBase.builder()
                .userId(taskEntity.getUserId())
                .taskId(taskEntity.getTaskId())
                .taskType(taskEntity.getTaskType())
                .taskStage(taskEntity.getTaskStage())
                .status(taskEntity.getStatus())
                .crtRetryNum(taskEntity.getCrtRetryNum())
                .maxRetryNum(taskEntity.getMaxRetryNum())
                .maxRetryInterval(taskEntity.getMaxRetryInterval())
                .scheduleLog(JSON.parseObject(taskEntity.getScheduleLog(), ScheduleLog.class))
                .build();
        // 默认使用JSON解析上下文
        taskBase.setTaskContext(JSON.parseObject(taskEntity.getTaskContext(), TaskContext.class));
        // 尝试获取自定义的上下文解析结果（contextLoad方法交由用户实现）
        TaskContext contextLoad = ReflectionUtils.reflectMethod(taskClazz, "contextLoad", new Object[]{taskEntity.getTaskContext()}, new Class[]{String.class});
        if (Objects.nonNull(contextLoad)) {
            taskBase.setTaskContext(contextLoad);
        }
        return taskBase;
    }

    public static TaskEntity toTaskEntity(TaskBase taskBase) {
        if (taskBase == null) {
            return null;
        }
        return TaskEntity.builder()
                .userId(taskBase.getUserId())
                .taskId(taskBase.getTaskId())
                .taskType(taskBase.getTaskType())
                .taskStage(taskBase.getTaskStage())
                .status(taskBase.getStatus())
                .crtRetryNum(taskBase.getCrtRetryNum())
                .maxRetryNum(taskBase.getMaxRetryNum())
                .orderTime(taskBase.getOrderTime())
                .priority(taskBase.getPriority())
                .maxRetryInterval(taskBase.getMaxRetryInterval())
                .scheduleLog(taskBase.getScheduleLog().toString())
                .taskContext(taskBase.getTaskContext().toString())
                .build();
    }

    /**
     * Entity转为DTO
     */
    public static TaskRes toDTO(TaskEntity task) {
        if (task == null) {
            return null;
        }
        return TaskRes.builder()
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
