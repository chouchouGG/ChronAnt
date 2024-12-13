package cn.uhoc.trigger.api.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @program: ChronAnt
 * @description:
 * @author: chouchouGG
 * @create: 2024-12-02 21:54
 **/
@Builder
@Data
public class TaskRes {

    private String userId;
    private String taskId;
    private String taskType;
    private String taskStage;
    private int status;
    private int crtRetryNum;
    private int maxRetryNum;
    private int maxRetryInterval;
    private String scheduleLog;
    private String taskContext;

//    private Long create_time;
//    private Long modify_time;
}
