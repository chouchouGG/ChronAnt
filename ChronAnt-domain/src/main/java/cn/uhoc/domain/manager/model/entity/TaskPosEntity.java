package cn.uhoc.domain.manager.model.entity;

import lombok.Builder;
import lombok.Data;

/**
 * @program: ChronAnt
 * @description:
 * @author: chouchouGG
 * @create: 2024-12-01 19:07
 **/
@Data
@Builder
public class TaskPosEntity {

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
}
