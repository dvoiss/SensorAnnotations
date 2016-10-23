package com.dvoiss.sensorannotations;

import com.dvoiss.sensorannotations.exception.ProcessingException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * This wrapper class holds all the annotations found in a given class {@link
 * #mEnclosingClassName}.
 *
 * The {@link #mItemsMap} is a map with sensor types as the key and a value of a map between the
 * annotation class to the method annotated.
 */
class AnnotatedMethodsPerClass {
    @NonNull private String mEnclosingClassName;
    @NonNull private Map<Integer, Map<Class, AnnotatedMethod>> mItemsMap = new LinkedHashMap<>();

    AnnotatedMethodsPerClass(@NonNull String enclosingClassName) {
        this.mEnclosingClassName = enclosingClassName;
    }

    void add(@NonNull Class<? extends Annotation> annotationClass, @NonNull AnnotatedMethod method)
        throws ProcessingException {
        Map<Class, AnnotatedMethod> annotationMap = mItemsMap.get(method.getSensorType());
        if (annotationMap == null) {
            annotationMap = new HashMap<>();
        }

        if (annotationMap.get(annotationClass) != null) {
            String error =
                String.format("@%s is already annotated on a different method in class %s",
                    annotationClass.getSimpleName(), method.getExecutableElement().getSimpleName());
            throw new ProcessingException(method.getExecutableElement(), error);
        }

        annotationMap.put(annotationClass, method);
        mItemsMap.put(method.getSensorType(), annotationMap);
    }

    boolean hasAnnotationsOfType(Class<? extends Annotation> annotationClass) {
        for (Map<Class, AnnotatedMethod> values : mItemsMap.values()) {
            if (values.get(annotationClass) != null) {
                return true;
            }
        }

        return false;
    }

    @NonNull String getEnclosingClassName() {
        return mEnclosingClassName;
    }

    @NonNull Map<Integer, Map<Class, AnnotatedMethod>> getItemsMap() {
        return mItemsMap;
    }
}
