package cn.uhoc.infra.persistent.po;


import cn.uhoc.domain.manager.model.entity.TaskEntity;
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
     * 存储任务的类型名
     */
    private String taskType;
    /**
     * 存储任务阶段信息
     */
    private String taskStage;
    /**
     *
     */
    private Integer status;
    /**
     * 已经重试几次了
     */
    private Integer crtRetryNum;
    /**
     * 最大能重试几次
     */
    private Integer maxRetryNum;
    /**
     *
     */
    private Long orderTime;
    /**
     *
     */
    private Integer priority;
    /**
     * 最大重试间隔
     */
    private Integer maxRetryInterval;
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

    public TaskEntity toEntity() {
        return TaskEntity.builder()
                .userId(this.userId)
                .taskId(this.taskId)
                .taskType(this.taskType)
                .taskStage(this.taskStage)
                .status(this.status)
                .crtRetryNum(this.crtRetryNum)
                .maxRetryNum(this.maxRetryNum)
                .orderTime(this.orderTime)
                .priority(this.priority)
                .maxRetryInterval(this.maxRetryInterval)
                .scheduleLog(this.scheduleLog)
                .taskContext(this.taskContext)
                .build();
    }
}
