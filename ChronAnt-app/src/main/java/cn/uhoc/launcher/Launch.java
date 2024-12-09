package cn.uhoc.launcher;

public interface Launch {

    int start();

    /**
     * Destroy the server.
     */
    int destroy();

    int init();

}
