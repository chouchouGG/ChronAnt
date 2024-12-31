package cn.uhoc.domain.register;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE) // 只能应用于类上
@Retention(RetentionPolicy.RUNTIME) // 运行时有效
@Component // 元注解: 任何使用 @MultiStageAsyncTask 标记的类都会自动具备 @Component 的功能
public @interface MultiStageAsyncTask {
    String taskType() default "";
}
