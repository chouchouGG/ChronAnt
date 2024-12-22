package cn.uhoc.domain.executor.service;

public interface IExecutorService {

    /**
     * 启动
     */
    int start();

    /**
     * Destroy the server.
     */
    int destroy();

    /**
     * 初始化
     */
    int init();

}
