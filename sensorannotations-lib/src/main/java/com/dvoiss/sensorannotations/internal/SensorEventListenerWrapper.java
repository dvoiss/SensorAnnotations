package com.dvoiss.sensorannotations.internal;

import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;

/**
 * This is a helper class used for re-registering the event listener with the correct values.
 */
@SuppressWarnings({ "UnusedDeclaration" })
public class SensorEventListenerWrapper extends EventListenerWrapper<SensorEventListener> {
    private final int mDelay;

    public SensorEventListenerWrapper(int sensorType, int delay,
        @NonNull SensorEventListener sensorEventListener) {
        super(sensorType, sensorEventListener);
        this.mDelay = delay;
    }

    public void registerListener(@NonNull SensorManager sensorManager) {
        sensorManager.registerListener(getEventListener(), getSensor(sensorManager), mDelay);
    }

    public void unregisterListener(@NonNull SensorManager sensorManager) {
        sensorManager.unregisterListener(getEventListener());
    }
}
