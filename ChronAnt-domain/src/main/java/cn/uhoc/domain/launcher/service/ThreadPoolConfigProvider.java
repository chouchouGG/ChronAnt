package cn.uhoc.domain.launcher.service;

/**
 * @program: ChronAnt
 * @description:
 * @author: chouchouGG
 * @create: 2024-12-22 23:42
 **/
public interface ThreadPoolConfigProvider {

    Integer getCorePoolSize();

    Integer getMaxPoolSize();

    Long getKeepAliveTime();

    Integer getBlockQueueSize();

    String getPolicy();

}
