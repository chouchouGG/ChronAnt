package cn.uhoc.domain.register;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE) // 只能应用于类上
@Retention(RetentionPolicy.RUNTIME) // 运行时有效
@Component
public @interface MultiStageAsyncTask {
    @AliasFor("name")
    String value() default "";

    @AliasFor("value")
    String name() default "";
}
