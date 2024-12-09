package cn.uhoc.domain.observer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ObserverStage {

    ObserverTypeEnum observerType();

    String taskType() default "*";

}
