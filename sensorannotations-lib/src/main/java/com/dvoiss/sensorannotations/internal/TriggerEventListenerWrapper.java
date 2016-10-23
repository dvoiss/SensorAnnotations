package com.dvoiss.sensorannotations.internal;

import android.annotation.TargetApi;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.TriggerEventListener;
import android.os.Build;
import android.support.annotation.NonNull;

/**
 * This is a helper class used for re-registering the event listener with the correct values.
 */
@SuppressWarnings({ "UnusedDeclaration" })
public class TriggerEventListenerWrapper extends EventListenerWrapper<TriggerEventListener> {
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public TriggerEventListenerWrapper(@NonNull TriggerEventListener sensorEventListener) {
        super(Sensor.TYPE_SIGNIFICANT_MOTION, sensorEventListener);
    }

    public void registerListener(@NonNull SensorManager sensorManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            sensorManager.requestTriggerSensor(getEventListener(), getSensor(sensorManager));
        }
    }

    public void unregisterListener(@NonNull SensorManager sensorManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            sensorManager.cancelTriggerSensor(getEventListener(), getSensor(sensorManager));
        }
    }
}
