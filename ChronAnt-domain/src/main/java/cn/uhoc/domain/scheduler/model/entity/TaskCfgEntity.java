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
     * 任务一次性调度（拉取）的数量
     */
    private Integer scheduleLimit;
    /**
     * 任务调度（拉取）时间间隔
     */
    private Integer scheduleInterval;
    /**
     * 该任务执行耗时的上限：是一个兜底值，这个时间是由业务方进行预估的，这个时间要尽量避免一个任务还处于正常执行中就被判定为异常的情况。
     */
    private Integer maxProcessingTime;
    /**
     * 任务执行失败时重试的最大次数
     */
    private Integer maxRetryNum;
    /**
     * 重试间隔：重试间隔渐进式增加（翻倍），如果重试间隔为5s，则第一次重试间隔5s，第二次重试间隔10s，第三次15s...
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
