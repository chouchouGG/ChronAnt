package cn.uhoc.domain.executor.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据库中任务记录进行解析后的实体类
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskBaseEntity {

    private String userId; //

    private String taskId;

    private String taskType; //

    private String taskStage; //

    private int status;

    private int crtRetryNum;

    private int maxRetryNum;

    private long orderTime;

    private int priority;

    private int maxRetryInterval;

    private ScheduleLogEntity scheduleLog; //

    private TaskContextEntity taskContext; //

}
