package cn.uhoc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @program: ChronAnt
 * @description:
 * @author: chouchouGG
 * @create: 2024-12-06 21:15
 **/
@Data
@ConfigurationProperties(prefix = "chronant.config", ignoreInvalidFields = true)
public class AsyncTaskWithMultiStageConfig {

    // TODO 数据库进行更新任务类型配置后，隔一段间隔才会生效。
    // 循环更新任务类型配置的时间
    private Long cycleUpdateScheduleTaskTypeConfigTime = 0L;

    // 拉取任务的初始偏移
    private Integer initOffsetForScheduleTask = 0;

    // 线程存活时间（执行任务线程池）
    private Long keepAliveTimeForExecuteTaskThreadPool = 0L;

    // 核心线程池（执行任务线程池）
    private Integer coreConcurrentRunTimesForExecuteTaskThreadPool = 0;

    // 最大线程池（执行任务线程池）
    private Integer maxConcurrentRunTimesForExecuteTaskThreadPool = 0;

}
