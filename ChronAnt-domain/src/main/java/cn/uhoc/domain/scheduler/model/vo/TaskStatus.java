package cn.uhoc.domain.scheduler.model.vo;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 任务状态
 */
@Getter
@RequiredArgsConstructor
public enum TaskStatus {

    PENDING(0x01, "Pending Execution"),
    EXECUTING(0x02, "Executing"),
    SUCCESS(0x04, "Execution Successful"),
    FAIL(0x08, "Execution Failed"),
    ;

    /**
     * 状态码
     */
    private final int status;
    /**
     * 状态描述
     */
    private final String description;

    /**
     * 根据状态码查询枚举
     *
     * @param status 状态码
     * @return 对应的 TaskStatus 枚举
     */
    public static TaskStatus fromStatus(int status) {
        // values() 是一个 Java 枚举类原生提供的静态方法，用于返回该枚举中所有常量的数组。
        return Arrays.stream(values())
                .filter(ts -> ts.status == status)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown status: " + status));
    }
}