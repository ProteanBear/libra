package xyz.proteanbear.libra.framework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A custom annotation that identifies the execution method in the task class
 * component of the exchange platform
 *
 * @author ProteanBear
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JobTaskExecute
{
}