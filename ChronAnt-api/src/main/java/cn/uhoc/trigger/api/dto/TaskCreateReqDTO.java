package cn.uhoc.trigger.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TaskCreateReqDTO {

    private String userId;

    private String taskType;
    
    private String taskStage;

    private String scheduleLog;

    private String taskContext;

}
