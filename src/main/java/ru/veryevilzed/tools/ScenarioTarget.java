package ru.veryevilzed.tools;

import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by zed on 11.08.16.
 */
@Target({})
@Retention(RetentionPolicy.SOURCE)
public @interface ScenarioTarget {
    String name();
    String autowired() default "";
    String autowiredName() default "";
    String qualifier() default "";
    String constructor() default "";
}
