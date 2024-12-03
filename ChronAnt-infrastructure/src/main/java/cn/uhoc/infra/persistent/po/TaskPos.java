package cn.uhoc.infra.persistent.po;

import lombok.Data;

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
    private Long createTime;
    /**
     * 修改时间
     */
    private Long modifyTime;

}