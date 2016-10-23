package com.dvoiss.sensorannotations;

import java.lang.annotation.Annotation;
import javax.lang.model.element.ExecutableElement;
import org.checkerframework.checker.nullness.qual.NonNull;

import static com.dvoiss.sensorannotations.SensorAnnotationsFileBuilder.TYPE_SIGNIFICANT_MOTION;

/**
 * This is a wrapper class that holds information about the method that was annotated ({@link
 * #mAnnotatedMethodElement}) and the values specified in the annotation.
 */
class AnnotatedMethod {
    private static final int INVALID_SENSOR = -1;
    static final int INVALID_DELAY = -1;

    @NonNull private final ExecutableElement mAnnotatedMethodElement;

    private final int mSensorType;
    private final int mDelay;

    AnnotatedMethod(@NonNull ExecutableElement methodElement,
        @NonNull Class<? extends Annotation> annotationClass) throws IllegalArgumentException {
        Annotation annotation = methodElement.getAnnotation(annotationClass);
        mAnnotatedMethodElement = methodElement;
        mDelay = getDelayFromAnnotation(annotation);
        mSensorType = getSensorTypeFromAnnotation(annotation);

        if (mSensorType == INVALID_SENSOR) {
            throw new IllegalArgumentException(String.format(
                "No sensor type specified in @%s for method %s."
                    + " Set a sensor type such as Sensor.TYPE_ACCELEROMETER.",
                annotationClass.getSimpleName(), methodElement.getSimpleName().toString()));
        }
    }

    int getSensorType() {
        return mSensorType;
    }

    int getDelay() {
        return mDelay;
    }

    @NonNull ExecutableElement getExecutableElement() {
        return mAnnotatedMethodElement;
    }

    /**
     * Return the sensor type set on the annotation.
     *
     * @param annotation The annotation we want to inspect for the sensor type.
     * @return The sensor type or {@link #INVALID_SENSOR}.
     */
    private int getSensorTypeFromAnnotation(@NonNull Annotation annotation) {
        if (annotation instanceof OnSensorChanged) {
            return ((OnSensorChanged) annotation).value();
        } else if (annotation instanceof OnAccuracyChanged) {
            return ((OnAccuracyChanged) annotation).value();
        } else if (annotation instanceof OnSensorNotAvailable) {
            return ((OnSensorNotAvailable) annotation).value();
        } else if (annotation instanceof OnTrigger) {
            return TYPE_SIGNIFICANT_MOTION;
        }

        return INVALID_SENSOR;
    }

    /**
     * Return the sensor mDelay value on the annotation or return a sentinel value if no value is
     * found.
     *
     * @param annotation The annotation we want to inspect for the delay value.
     * @return The delay value or {@link #INVALID_DELAY}.
     */
    private int getDelayFromAnnotation(@NonNull Annotation annotation) {
        if (annotation instanceof OnSensorChanged) {
            return ((OnSensorChanged) annotation).delay();
        } else if (annotation instanceof OnAccuracyChanged) {
            return ((OnAccuracyChanged) annotation).delay();
        }

        return INVALID_DELAY;
    }
}
