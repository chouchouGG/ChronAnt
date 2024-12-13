package cn.uhoc.trigger.api.dto;

import lombok.Data;

@Data
public class TaskCfgReq {

    private String taskType;

    private Integer scheduleLimit;

    private Integer scheduleInterval;

    private Integer maxProcessingTime;

    private Integer maxRetryNum;

    private Integer retryInterval;

//    private Long createTime;
//
//    private Long modifyTime;


}
