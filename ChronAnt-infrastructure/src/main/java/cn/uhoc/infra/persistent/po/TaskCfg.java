package cn.uhoc.infra.persistent.po;

import cn.uhoc.domain.scheduler.model.entity.TaskCfgEntity;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TaskCfg {

    /**
     * 任务类型
     */
    private String taskType;
    /**
     * 一次拉取多少个任务
     */
    private Integer scheduleLimit;
    /**
     *
     */
    private Integer scheduleInterval;
    /**
     * 处于执行中的最大时间
     */
    private Integer maxProcessingTime;
    /**
     * 最大重试次数
     */
    private Integer maxRetryNum;
    /**
     * 重试间隔
     */
    private Integer retryInterval;
    /**
     *
     */
    private Long createTime;
    /**
     *
     */
    private Long modifyTime;

    public static TaskCfgEntity toEntity(TaskCfg taskCfg) {
        return TaskCfgEntity.builder()
                .taskType(taskCfg.getTaskType())
                .scheduleLimit(taskCfg.getScheduleLimit())
                .scheduleInterval(taskCfg.getScheduleInterval())
                .maxProcessingTime(taskCfg.getMaxProcessingTime())
                .maxRetryNum(taskCfg.getMaxRetryNum())
                .retryInterval(taskCfg.getRetryInterval())
                .build();
    }

    public static TaskCfg fromEntity(TaskCfgEntity taskCfgEntity) {
        return TaskCfg.builder()
                .taskType(taskCfgEntity.getTaskType())
                .scheduleLimit(taskCfgEntity.getScheduleLimit())
                .scheduleInterval(taskCfgEntity.getScheduleInterval())
                .maxProcessingTime(taskCfgEntity.getMaxProcessingTime())
                .maxRetryNum(taskCfgEntity.getMaxRetryNum())
                .retryInterval(taskCfgEntity.getRetryInterval())
                .build();
    }
}
