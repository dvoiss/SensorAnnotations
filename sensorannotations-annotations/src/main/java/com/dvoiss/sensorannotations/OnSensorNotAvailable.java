package com.dvoiss.sensorannotations;

import com.dvoiss.sensorannotations.internal.ListenerMethod;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
@ListenerMethod
public @interface OnSensorNotAvailable {
    int value() default -1;
}
