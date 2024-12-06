package cn.uhoc.domain.scheduler.model.entity;

import cn.uhoc.domain.scheduler.model.vo.TaskConstants;
import cn.uhoc.trigger.api.dto.TaskResDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @program: ChronAnt
 * @description: 任务实体类
 * @author: chouchouGG
 * @create: 2024-12-01 18:05
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskEntity {

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
     * 已经重试几次了：current retry num
     */
    private int crtRetryNum;
    /**
     * 最大能重试几次：maxmimum retry num
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
     * Entity转为DTO
     */
    public TaskResDTO toDTO() {
        return TaskResDTO.builder()
                .userId(this.getUserId())
                .taskId(this.getTaskId())
                .taskType(this.getTaskType())
                .taskStage(this.getTaskStage())
                .status(this.getStatus())
                .crtRetryNum(this.getCrtRetryNum())
                .maxRetryNum(this.getMaxRetryNum())
                .maxRetryInterval(this.getMaxRetryInterval())
                .scheduleLog(this.getScheduleLog())
                .taskContext(this.getTaskContext())
                .build();
    }

}
