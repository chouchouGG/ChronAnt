package cn.uhoc.infra.persistent.po;


import cn.uhoc.domain.scheduler.model.entity.TaskEntity;
import lombok.Data;

@Data
public class Task {

    /**
     * 自增id
     */
    private String id;
    /**
     *
     */
    private String userId;
    /**
     *
     */
    private String taskId;
    /**
     * 存储任务的全类名
     */
    private String taskType;
    /**
     * 存储任务阶段信息
     */
    private String taskStage;
    /**
     *
     */
    private int status;
    /**
     * 已经重试几次了
     */
    private int crtRetryNum;
    /**
     * 最大能重试几次
     */
    private int maxRetryNum;
    /**
     *
     */
    private long orderTime;
    /**
     *
     */
    private int priority;
    /**
     * 最大重试间隔
     */
    private int maxRetryInterval;
    /**
     * 调度信息记录
     */
    private String scheduleLog;
    /**
     * 任务上下文，用户自定义
     */
    private String taskContext;
    /**
     *
     */
    private Long createTime;
    /**
     *
     */
    private Long modifyTime;

    public static TaskEntity toEntity(Task task) {
        return TaskEntity.builder()
                .userId(task.userId)
                .taskId(task.taskId)
                .taskType(task.taskType)
                .taskStage(task.taskStage)
                .status(task.status)
                .crtRetryNum(task.crtRetryNum)
                .maxRetryNum(task.maxRetryNum)
                .orderTime(task.orderTime)
                .priority(task.priority)
                .maxRetryInterval(task.maxRetryInterval)
                .scheduleLog(task.scheduleLog)
                .taskContext(task.taskContext)
                .build();
    }
}
