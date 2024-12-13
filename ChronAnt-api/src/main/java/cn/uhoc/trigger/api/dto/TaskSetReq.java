package cn.uhoc.trigger.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TaskSetReq {

    private String taskId;

    private String taskStage;

    private Integer status;

    private String scheduleLog;

    private String taskContext;

    private Long orderTime; // todo 暂时不允许修改

    private Integer priority; // todo 暂时不允许修改

    /**
     * 只有任务执行失败时，才需要设置重试次数
     */
    private Integer crtRetryNum;

    private Integer maxRetryNum; // todo 暂时不允许修改

    private Integer maxRetryInterval; // todo 暂时不允许修改

    public TaskSetReq updateCurrentRetryCount(Integer newRetryCount) {
        this.crtRetryNum = newRetryCount;
        return this;
    }
}
