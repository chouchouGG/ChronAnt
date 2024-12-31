package cn.uhoc.infra.persistent.po;

import cn.uhoc.domain.manager.model.entity.TaskPosEntity;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 
 * @TableName t_schedule_pos
 */
@Data
public class TaskPos {

    /**
     * 自增id
     */
    private Long id;
    /**
     * 任务类型（类名）
     */
    private String taskType;
    /**
     * 调度开始于几号表
     */
    private Integer scheduleBeginPos;
    /**
     * 调度结束于几号表
     */
    private Integer scheduleEndPos;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 修改时间
     */
    private LocalDateTime modifyTime;

    public TaskPosEntity toEntity() {
        return TaskPosEntity.builder()
                .taskType(this.getTaskType())
                .scheduleBeginPos(this.getScheduleBeginPos())
                .scheduleEndPos(this.getScheduleEndPos())
                .build();
    }

}