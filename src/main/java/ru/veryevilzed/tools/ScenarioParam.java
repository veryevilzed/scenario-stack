package ru.veryevilzed.tools;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by zed on 11.08.16.
 */
@Target({})
@Retention(RetentionPolicy.SOURCE)
public @interface ScenarioParam {
    String name();
    String value();
}
