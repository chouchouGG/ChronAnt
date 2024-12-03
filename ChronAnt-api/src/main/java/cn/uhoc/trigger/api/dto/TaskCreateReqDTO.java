package cn.uhoc.trigger.api.dto;

import lombok.Data;

@Data
public class TaskCreateReqDTO {

    private String userId;

    private String taskType;
    
    private String taskStage;

    private String scheduleLog;

    private String taskContext;

}
