package cn.uhoc.domain.observer;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *  观察者类型
 */
@Getter
@AllArgsConstructor
public enum ObserverTypeEnum {

    onBoot(0),
    onError(1),
    onExecute(2),
    onFinish(3),
    onStop(4),
    onObtain(5),
    ;

    private final int code;
}