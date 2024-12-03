package cn.uhoc.type.common;


public class Utils {
    /**
     * 获得任务Id
     * @return
     */
    public static String getTaskId() {
        return SnowFlake.nextId() + "";
    }





}
