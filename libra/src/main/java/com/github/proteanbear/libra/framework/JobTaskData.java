package com.github.proteanbear.libra.framework;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to mark the task to pass the data.
 *
 * @author ProteanBear
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JobTaskData
{
    //The key for data map
    String value() default "";

    //The key for data map
    @AliasFor("value")
    String name() default "";
}