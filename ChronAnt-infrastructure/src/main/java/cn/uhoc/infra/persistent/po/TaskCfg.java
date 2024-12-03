package cn.uhoc.infra.persistent.po;

import lombok.Data;

@Data
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
}
