package xyz.proteanbear.libra.framework;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Custom annotations that identify the task component of the exchange platform
 *
 * @author ProteanBear
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface JobTask
{
    //Task name
    String value();
    //Task display name
    String title();
    //Task group
    String group() default "libra";
    //Task description
    String description() default "";
    //Is concurrent?
    //When is true,stateful tasks can not be executed concurrently.
    //When is false,stateless tasks can execute concurrently.
    boolean concurrent() default false;
}
