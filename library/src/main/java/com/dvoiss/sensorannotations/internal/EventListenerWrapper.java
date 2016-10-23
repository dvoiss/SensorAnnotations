package com.dvoiss.sensorannotations.internal;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;

/**
 * This is a helper class used for re-registering the event listener with the correct values.
 */
@SuppressWarnings({ "UnusedDeclaration" })
abstract public class EventListenerWrapper<T> {
    private final int mSensorType;
    @NonNull private final T mEventListener;

    public EventListenerWrapper(int sensorType, @NonNull T eventListener) {
        this.mSensorType = sensorType;
        this.mEventListener = eventListener;
    }

    public int getSensorType() {
        return mSensorType;
    }

    @NonNull
    public T getEventListener() {
        return mEventListener;
    }

    @NonNull
    public Sensor getSensor(@NonNull SensorManager sensorManager) {
        return sensorManager.getDefaultSensor(mSensorType);
    }

    abstract public void registerListener(@NonNull SensorManager sensorManager);

    abstract public void unregisterListener(@NonNull SensorManager sensorManager);
}
