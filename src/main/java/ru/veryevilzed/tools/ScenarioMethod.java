package ru.veryevilzed.tools;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Альтернативный способ указать точку входа
 * Created by zed on 10.08.16.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface ScenarioMethod {
    ScenarioTarget[] targets() default {};
}
