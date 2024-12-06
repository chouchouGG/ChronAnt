package cn.uhoc.domain.scheduler.model.entity;

import cn.uhoc.trigger.api.dto.TaskCfgDTO;
import lombok.Builder;
import lombok.Data;

/**
 * @program: ChronAnt
 * @description: 多阶段异步任务配置实体
 * @author: chouchouGG
 * @create: 2024-12-01 19:59
 **/
@Data
@Builder
public class TaskCfgEntity {

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

    public static TaskCfgEntity fromDTO(TaskCfgDTO taskCfgDTO) {
        return TaskCfgEntity.builder()
                .taskType(taskCfgDTO.getTaskType())
                .scheduleLimit(taskCfgDTO.getScheduleLimit())
                .scheduleInterval(taskCfgDTO.getScheduleInterval())
                .maxProcessingTime(taskCfgDTO.getMaxProcessingTime())
                .maxRetryNum(taskCfgDTO.getMaxRetryNum())
                .retryInterval(taskCfgDTO.getRetryInterval())
                .build();
    }
}
