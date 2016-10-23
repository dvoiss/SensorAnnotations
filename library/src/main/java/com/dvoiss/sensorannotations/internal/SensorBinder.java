package com.dvoiss.sensorannotations.internal;

public interface SensorBinder<T> {
    void bind(T target);

    void unbind();
}
