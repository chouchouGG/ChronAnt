package cn.uhoc.type.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误状态和信息
 */
@Getter
@AllArgsConstructor
public enum ExceptionStatus {

    // TODO 为每个响应码添加注释
    // TODO 学习枚举enum

    /**
     * 成功
     */
    SUCCESS(0, "ok"),
    ERR_INPUT_INVALID(8020, "input invalid"),
//    ERR_SHOULD_BIND(8021, "should bind failed"),
//    ERR_JSON_MARSHAL(8022, "json marshal failed"),
    ERR_GET_TASK_INFO(8035, "get task info failed"),
//    ERR_GET_TASK_HANDLE_PROCESS(8036, "get task handle process Failed"),
    ERR_CREATE_TASK(8037, "create task failed"),
    ERR_GET_TASK_LIST(8038, "get task list failed"),
    ERR_GET_TASK_CFG(8039, "get task config failed"),
//    ERR_INCREASE_CRT_RETRY_NUM(8040, "set task failed"),
    ERR_SET_TASK(8041, "increase crt retry num failed"),
    /**
     * 获取任务位置失败
     */
    ERR_GET_TASK_POS(8042, "get task position failed"),
//    ERR_GET_PROCESSING_COUNT(8043, "get processing count failed"),
//    ERR_SET_USER_PRIORITY(8045, "set user priority failed"),
//    ERR_GET_TASK_CFG_FROM_DB(8046, "get task cfg failed"),
//    ERR_SET_TASK_CFG_FROM_DB(8047, "set task cfg failed")
    ;


    private final int code;
    private final String info;
}
