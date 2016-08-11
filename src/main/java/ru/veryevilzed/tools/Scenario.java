package ru.veryevilzed.tools;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Атребут сценария
 * Created by zed on 10.08.16.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface Scenario {
    String name() default "main";
    String incoming();
    String context() default "java.lang.Object";
}
