package com.dvoiss.sensorannotations;

import com.dvoiss.sensorannotations.internal.ListenerMethod;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static android.hardware.SensorManager.SENSOR_DELAY_NORMAL;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
@ListenerMethod(parameters = { "android.hardware.SensorEvent" })
public @interface OnSensorChanged {
    int value() default -1;

    int delay() default SENSOR_DELAY_NORMAL;
}
