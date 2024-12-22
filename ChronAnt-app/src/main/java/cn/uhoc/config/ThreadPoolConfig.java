package cn.uhoc.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.*;

@Slf4j
@EnableAsync
@Configuration // 标记一个类作为 Java 配置类
@EnableConfigurationProperties({ThreadPoolConfigProp.class, AsyncTaskWithMultiStageConfig.class})
@Deprecated
public class ThreadPoolConfig {

    @Bean
    @ConditionalOnMissingBean(ThreadPoolExecutor.class)
    public ThreadPoolExecutor threadPoolExecutor(ThreadPoolConfigProp baseThreadPoolProperties, AsyncTaskWithMultiStageConfig taskWithMultiStageConfig) {
        // TODO 学习线程池的常见拒绝策略
        // 实例化拒绝策略
        RejectedExecutionHandler handler;
        switch (baseThreadPoolProperties.getPolicy()) {
            case "DiscardPolicy":
                handler = new ThreadPoolExecutor.DiscardPolicy();
                break;
            case "DiscardOldestPolicy":
                handler = new ThreadPoolExecutor.DiscardOldestPolicy();
                break;
            case "CallerRunsPolicy":
                handler = new ThreadPoolExecutor.CallerRunsPolicy();
                break;
            case "AbortPolicy":
            default:
                handler = new ThreadPoolExecutor.AbortPolicy();
                break;
        }

        Long keepAliveTime = taskWithMultiStageConfig.getKeepAliveTimeForExecuteTaskThreadPool() == 0
                ? baseThreadPoolProperties.getKeepAliveTime() : taskWithMultiStageConfig.getKeepAliveTimeForExecuteTaskThreadPool();
        Integer corePoolSize = taskWithMultiStageConfig.getCoreConcurrentRunTimesForExecuteTaskThreadPool() == 0
                ? baseThreadPoolProperties.getCorePoolSize() : taskWithMultiStageConfig.getCoreConcurrentRunTimesForExecuteTaskThreadPool();
        Integer maxPoolSize = taskWithMultiStageConfig.getMaxConcurrentRunTimesForExecuteTaskThreadPool() == 0
                ? baseThreadPoolProperties.getMaxPoolSize() : taskWithMultiStageConfig.getMaxConcurrentRunTimesForExecuteTaskThreadPool();


        // TODO 给线程池其他参数也应该增加用户可以配置的属性？


        // 创建线程池
        return new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(baseThreadPoolProperties.getBlockQueueSize()),
                Executors.defaultThreadFactory(),
                handler
        );
    }

}
