package cn.uhoc.domain.observer;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *  观察者类型
 */
@Getter
@AllArgsConstructor
public enum ObserverTypeEnum {



    onBoot(0), // 任务开始丢入线程池
    onError(1), // 任务执行出错
    onExecute(2), // 任务正在执行
    onFinish(3), // 整个任务多个阶段执行结束
    onStop(4), // 这个状态暂时没有使用
    onObtain(5), // 任务被获取
    ;

    private final int code;
}