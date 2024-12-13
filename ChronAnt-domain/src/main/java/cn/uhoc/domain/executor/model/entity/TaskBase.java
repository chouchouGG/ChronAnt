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
public class TaskBase {

    private String userId; //

    private String taskId;

    private String taskType; //

    private String taskStage; //

    private Integer status;

    private Integer crtRetryNum;

    private Integer maxRetryNum;

    private Long orderTime;

    private Integer priority;

    private Integer maxRetryInterval;

    private ScheduleLog scheduleLog; //

    private TaskContext taskContext; //

}
