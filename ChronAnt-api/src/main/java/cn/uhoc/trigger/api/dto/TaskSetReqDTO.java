package cn.uhoc.trigger.api.dto;

import lombok.Data;

@Data
public class TaskSetReqDTO {

    private String taskId; // NOT NULL DEFAULT '',

    private String taskStage;

    private int status; //tinyint(3) unsigned NOT NULL DEFAULT '0',

    private String scheduleLog;// varchar(4096) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '调度信息记录',

    private String taskContext;

    private long orderTime;

    private int priority;

    private int crtRetryNum; //NOT NULL DEFAULT '0' COMMENT '已经重试几次了',

    private int maxRetryNum; //NOT NULL DEFAULT '0' COMMENT '最大能重试几次',

    private int maxRetryInterval;
}
