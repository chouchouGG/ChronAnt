package cn.uhoc.domain.manager.model.vo;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 任务状态
 */
@Getter
@RequiredArgsConstructor
public enum TaskStatus {

    // 1-从未执行，2-待执行，4-执行中，8-执行成功，16-执行失败
    NEVER_EXECUTED(0x01),   // 从未执行
    PENDING(0x02),          // 待执行
    EXECUTING(0x04),        // 执行中
    SUCCESS(0x08),          // 执行成功
    FAIL(0x10),             // 执行失败
    ;

    /**
     * 状态码
     */
    private final int code;


    public static String getErrMsg(TaskStatus taskStatus) {
        switch (taskStatus) {
            case PENDING:
                return "Task waits processing...";
            case EXECUTING:
                return "Task in progress...";
            case SUCCESS:
                return "Task completed successfully.";
            case FAIL:
                return "Task execution failed: ";
            default:
                throw new IllegalArgumentException("Unhandled TaskStatus: " + taskStatus);
        }
    }


    /**
     * 解析任务状态
     *
     * <p>如果任务状态为 0x0c，等价于二进制的 1100，代表成功（SUCCESS）和失败（FAIL）。</p>
     *
     * @param status 合并的状态
     * @return 分解的状态列表
     */
    public static List<Integer> parseStatusList(int combinedStatus) {
        List<Integer> statusList = new ArrayList<>();
        while (combinedStatus != 0) {
            // 提取最低位的状态
            int currentStatus = combinedStatus & -combinedStatus;
            statusList.add(currentStatus);
            // 移除当前提取的状态
            combinedStatus ^= currentStatus;
        }
        return statusList;
    }

    /**
     * 合并任务状态
     *
     * <p>将多个状态值合并为一个状态</p>
     *
     * @param statusList 状态列表
     * @return 合并后的状态值
     */
    public static int combineStatus(List<TaskStatus> statusList) {
        int combinedStatus = 0;
        for (TaskStatus status : statusList) {
            combinedStatus |= status.getCode();
        }
        return combinedStatus;
    }

}