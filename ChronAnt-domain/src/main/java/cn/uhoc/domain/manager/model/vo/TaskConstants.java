package cn.uhoc.domain.manager.model.vo;

public class TaskConstants {
    // 任务列表的最大限制
    public static final int MAX_TASK_LIST_LIMIT = 1000;

    // 任务列表的默认限制
    public static final int DEFAULT_TASK_LIST_LIMIT = 1000;

    // 默认的任务状态
    public static final int DEFAULT_TASK_STATUS = 0;

    // 默认的任务阶段调度日志上下文
    public static final String DEFAULT_TASK_STAGE_SCHEDULE_LOG_CONTEXT = "";

    public final static int DEFAULT_TIME_INTERVAL = 20;
    public final static int MAX_ERR_MSG_LEN = 256;
    private final static int SCHEDULE_LIMIT = 10;
    private final static int SCHEDULE_INTERVAL = 10;
    private final static int MAX_PROCESSING_TIME = 60;
    private final static int MAX_RETRY_NUM = 5;
    private final static int RETRY_INTERVAL = 10;
}