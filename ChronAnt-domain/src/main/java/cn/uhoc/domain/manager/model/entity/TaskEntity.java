package cn.uhoc.domain.manager.model.entity;

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
     * 用户ID
     */
    private String userId;
    /**
     * 任务ID
     */
    private String taskId;
    /**
     * 任务的类型
     */
    private String taskType; // TODO 目前一个表里的所有任务类型相同，为了防止由于业务方过多导致的表数量增加，后期升级为一个表中可以包含多种任务类型
    /**
     * 存储任务处于哪一阶段，任务的阶段始终是正向推进，阶段1 -> 阶段2 -> 阶段3 -> ...
     */
    private String taskStage;
    /**
     * 状态是针对于当前阶段而言的，描述当前阶段是待执行、执行中、成功、失败
     */
    private int status;
    /**
     * 已重试次数：current retry num
     */
    private int crtRetryNum;
    /**
     * 最大重试次数：配置表中也有该字段，可以理解为某一类型任务的默认配置。特定的任务还支持单独指定。
     */
    private int maxRetryNum;
    /**
     * 排序时间：考虑任务创建时间、更新时间、重试间隔、优先级等因素后，对任务执行顺序排序的因子
     */
    private long orderTime;
    /**
     * 任务优先级
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



}
