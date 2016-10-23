package com.dvoiss.sensorannotations;

import com.dvoiss.sensorannotations.exception.ProcessingException;
import com.dvoiss.sensorannotations.internal.ListenerMethod;
import com.google.common.base.Joiner;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.dvoiss.sensorannotations.AnnotatedMethod.INVALID_DELAY;

class SensorAnnotationsFileBuilder {
    /**
     * The suffix will be added to the name of the generated class.
     */
    private static final String SUFFIX = "$$SensorBinder";

    static final int TYPE_SIGNIFICANT_MOTION = 17;

    // region Static Types that are used in the methods below to create types and specs.

    private static final ClassName LISTENER_WRAPPER =
        ClassName.get("com.dvoiss.sensorannotations.internal", "EventListenerWrapper");
    private static final ClassName SENSOR_EVENT_LISTENER_WRAPPER =
        ClassName.get("com.dvoiss.sensorannotations.internal", "SensorEventListenerWrapper");
    private static final ClassName TRIGGER_EVENT_LISTENER_WRAPPER =
        ClassName.get("com.dvoiss.sensorannotations.internal", "TriggerEventListenerWrapper");
    private static final ClassName SENSOR_BINDER =
        ClassName.get("com.dvoiss.sensorannotations.internal", "SensorBinder");

    private static final ClassName SENSOR = ClassName.get("android.hardware", "Sensor");
    private static final ClassName SENSOR_MANAGER =
        ClassName.get("android.hardware", "SensorManager");
    private static final ClassName SENSOR_EVENT = ClassName.get("android.hardware", "SensorEvent");
    private static final ClassName TRIGGER_EVENT =
        ClassName.get("android.hardware", "TriggerEvent");
    private static final ClassName SENSOR_EVENT_LISTENER =
        ClassName.get("android.hardware", "SensorEventListener");
    private static final ClassName TRIGGER_EVENT_LISTENER =
        ClassName.get("android.hardware", "TriggerEventListener");
    private static final ClassName CONTEXT = ClassName.get("android.content", "Context");

    private static final ClassName LIST = ClassName.get("java.util", "List");
    private static final ClassName ARRAY_LIST = ClassName.get("java.util", "ArrayList");

    private static final FieldSpec LISTENER_WRAPPERS_FIELD =
        FieldSpec.builder(ParameterizedTypeName.get(LIST, LISTENER_WRAPPER), "listeners")
            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
            .build();
    private static final FieldSpec SENSOR_MANAGER_FIELD =
        FieldSpec.builder(SENSOR_MANAGER, "sensorManager")
            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
            .build();
    private static final MethodSpec UNBIND_METHOD =
        getBaseMethodBuilder("unbind").beginControlFlow("if (this.$N != null)",
            SENSOR_MANAGER_FIELD)
            .beginControlFlow("for ($T wrapper : $N)", LISTENER_WRAPPER, LISTENER_WRAPPERS_FIELD)
            .addStatement("wrapper.unregisterListener($N)", SENSOR_MANAGER_FIELD)
            .endControlFlow()
            .endControlFlow()
            .build();

    // endregion

    /**
     * Generates the code for our "Sensor Binder" class and writes it to the same package as the
     * annotated class.
     *
     * @param groupedMethodsMap Map of annotated methods per class.
     * @param elementUtils ElementUtils class from {@link ProcessingEnvironment}.
     * @param filer File writer class from {@link ProcessingEnvironment}.
     * @throws IOException
     * @throws ProcessingException
     */
    static void generateCode(@NonNull Map<String, AnnotatedMethodsPerClass> groupedMethodsMap,
        @NonNull Elements elementUtils, @NonNull Filer filer)
        throws IOException, ProcessingException {
        for (AnnotatedMethodsPerClass groupedMethods : groupedMethodsMap.values()) {
            // If we've annotated methods in an activity called "ExampleActivity" then that will be
            // the enclosing type element.
            TypeElement enclosingClassTypeElement =
                elementUtils.getTypeElement(groupedMethods.getEnclosingClassName());

            // Create the parameterized type that our generated class will implement,
            // (such as "SensorBinder<ExampleActivity>").
            ParameterizedTypeName parameterizedInterface = ParameterizedTypeName.get(SENSOR_BINDER,
                TypeName.get(enclosingClassTypeElement.asType()));

            // Create the target parameter that will be used in the constructor and bind method,
            // (such as "ExampleActivity").
            ParameterSpec targetParameter =
                ParameterSpec.builder(TypeName.get(enclosingClassTypeElement.asType()), "target")
                    .addModifiers(Modifier.FINAL)
                    .build();

            MethodSpec constructor =
                createConstructor(targetParameter, groupedMethods.getItemsMap());
            MethodSpec bindMethod = createBindMethod(targetParameter, groupedMethods);

            TypeSpec sensorBinderClass =
                TypeSpec.classBuilder(enclosingClassTypeElement.getSimpleName() + SUFFIX)
                    .addModifiers(Modifier.FINAL)
                    .addSuperinterface(parameterizedInterface)
                    .addField(SENSOR_MANAGER_FIELD)
                    .addField(LISTENER_WRAPPERS_FIELD)
                    .addMethod(constructor)
                    .addMethod(bindMethod)
                    .addMethod(UNBIND_METHOD)
                    .build();

            // Output our generated file with the same package as the target class.
            PackageElement packageElement = elementUtils.getPackageOf(enclosingClassTypeElement);
            JavaFileObject jfo =
                filer.createSourceFile(enclosingClassTypeElement.getQualifiedName() + SUFFIX);
            Writer writer = jfo.openWriter();
            JavaFile.builder(packageElement.toString(), sensorBinderClass)
                .addFileComment("This class is generated code from Sensor Lib. Do not modify!")
                .addStaticImport(CONTEXT, "SENSOR_SERVICE")
                .build()
                .writeTo(writer);
            writer.close();
        }
    }

    /**
     * Create the constructor for our generated class.
     *
     * @param targetParameter The target class that has annotated methods.
     * @param itemsMap A map of sensor types found in the annotations with the annotated methods.
     * @return {@link MethodSpec} representing the constructor of our generated class.
     */
    @NonNull
    private static MethodSpec createConstructor(@NonNull ParameterSpec targetParameter,
        @NonNull Map<Integer, Map<Class, AnnotatedMethod>> itemsMap) throws ProcessingException {
        ParameterSpec contextParameter = ParameterSpec.builder(CONTEXT, "context").build();
        Builder constructorBuilder = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addParameter(contextParameter)
            .addParameter(targetParameter)
            .addStatement("this.$N = ($T) $N.getSystemService(SENSOR_SERVICE)",
                SENSOR_MANAGER_FIELD, SENSOR_MANAGER, contextParameter)
            .addStatement("this.$N = new $T()", LISTENER_WRAPPERS_FIELD, ARRAY_LIST);

        // Loop through the sensor types that we have annotations for and create the listeners which
        // will call the annotated methods on our target class.
        for (Integer sensorType : itemsMap.keySet()) {
            Map<Class, AnnotatedMethod> annotationMap = itemsMap.get(sensorType);
            AnnotatedMethod sensorChangedAnnotatedMethod = annotationMap.get(OnSensorChanged.class);
            AnnotatedMethod accuracyChangedAnnotatedMethod =
                annotationMap.get(OnAccuracyChanged.class);
            AnnotatedMethod triggerAnnotatedMethod = annotationMap.get(OnTrigger.class);

            if (sensorType == TYPE_SIGNIFICANT_MOTION && (accuracyChangedAnnotatedMethod != null
                || sensorChangedAnnotatedMethod != null)) {
                throw new ProcessingException(null, String.format(
                    "@%s and @%s are not supported for the \"TYPE_SIGNIFICANT_MOTION\" type. Use @%s for this type.",
                    OnSensorChanged.class.getSimpleName(), OnAccuracyChanged.class.getSimpleName(),
                    OnTrigger.class.getSimpleName()));
            } else if (sensorType != TYPE_SIGNIFICANT_MOTION && triggerAnnotatedMethod != null) {
                throw new ProcessingException(null, String.format(
                    "The @%s is only supported for the \"TYPE_SIGNIFICANT_MOTION\" type.",
                    OnTrigger.class.getSimpleName()));
            }

            CodeBlock listenerWrapperCodeBlock;
            if (triggerAnnotatedMethod != null) {
                listenerWrapperCodeBlock = createTriggerListenerWrapper(triggerAnnotatedMethod);
                constructorBuilder.addCode(listenerWrapperCodeBlock);
            } else if (sensorChangedAnnotatedMethod != null
                || accuracyChangedAnnotatedMethod != null) {
                listenerWrapperCodeBlock =
                    createSensorListenerWrapper(sensorType, sensorChangedAnnotatedMethod,
                        accuracyChangedAnnotatedMethod);
                constructorBuilder.addCode(listenerWrapperCodeBlock);
            }
        }

        return constructorBuilder.build();
    }

    /**
     * Create an {@code EventListenerWrapper} that contains the {@code TriggerEventListener} and
     * calls the annotated methods on our target.
     *
     * @param triggerAnnotatedMethod Method annotated with {@link OnTrigger}.
     * @return {@link CodeBlock} of the {@code EventListenerWrapper}.
     */
    @NonNull
    private static CodeBlock createTriggerListenerWrapper(
        @NonNull AnnotatedMethod triggerAnnotatedMethod) throws ProcessingException {
        checkAnnotatedMethodForErrors(triggerAnnotatedMethod.getExecutableElement(),
            OnTrigger.class);

        CodeBlock listenerBlock = CodeBlock.builder()
            .add("new $T() {\n", TRIGGER_EVENT_LISTENER)
            .indent()
            .add(createOnTriggerListenerMethod(triggerAnnotatedMethod).toString())
            .unindent()
            .add("}")
            .build();

        return CodeBlock.builder()
            .addStatement("this.$N.add(new $T($L))", LISTENER_WRAPPERS_FIELD,
                TRIGGER_EVENT_LISTENER_WRAPPER, listenerBlock)
            .build();
    }

    /**
     * Create an {@code EventListenerWrapper} that contains the {@code
     * SensorEventListener} and calls the annotated methods on our target.
     *
     * @param sensorType The {@code Sensor} type.
     * @param sensorChangedAnnotatedMethod Method annotated with {@link OnSensorChanged}.
     * @param accuracyChangedAnnotatedMethod Method annotated with {@link OnAccuracyChanged}.
     * @return {@link CodeBlock} of the {@code EventListenerWrapper}.
     */
    @NonNull
    private static CodeBlock createSensorListenerWrapper(int sensorType,
        @Nullable AnnotatedMethod sensorChangedAnnotatedMethod,
        @Nullable AnnotatedMethod accuracyChangedAnnotatedMethod) throws ProcessingException {
        if (sensorChangedAnnotatedMethod != null) {
            checkAnnotatedMethodForErrors(sensorChangedAnnotatedMethod.getExecutableElement(),
                OnSensorChanged.class);
        }
        if (accuracyChangedAnnotatedMethod != null) {
            checkAnnotatedMethodForErrors(accuracyChangedAnnotatedMethod.getExecutableElement(),
                OnAccuracyChanged.class);
        }

        CodeBlock listenerBlock = CodeBlock.builder()
            .add("new $T() {\n", SENSOR_EVENT_LISTENER)
            .indent()
            .add(createOnSensorChangedListenerMethod(sensorChangedAnnotatedMethod).toString())
            .add(createOnAccuracyChangedListenerMethod(accuracyChangedAnnotatedMethod).toString())
            .unindent()
            .add("}")
            .build();

        int delay =
            getDelayForListener(sensorChangedAnnotatedMethod, accuracyChangedAnnotatedMethod);

        if (delay == INVALID_DELAY) {
            String error =
                String.format("@%s or @%s needs a delay value specified in the annotation",
                    OnSensorChanged.class.getSimpleName(), OnAccuracyChanged.class.getSimpleName());
            throw new ProcessingException(null, error);
        }

        return CodeBlock.builder()
            .addStatement("this.$N.add(new $T($L, $L, $L))", LISTENER_WRAPPERS_FIELD,
                SENSOR_EVENT_LISTENER_WRAPPER, sensorType, delay, listenerBlock)
            .build();
    }

    /**
     * Creates the implementation of {@code TriggerEventListener#onTrigger(TriggerEvent)} which
     * calls the annotated method on our target class.
     *
     * @param annotatedMethod Method annotated with {@code OnTrigger}.
     * @return {@link MethodSpec} of {@code TriggerEventListener#onTrigger(TriggerEvent)}.
     */
    @NonNull
    private static MethodSpec createOnTriggerListenerMethod(
        @NonNull AnnotatedMethod annotatedMethod) {
        ParameterSpec triggerEventParameter = ParameterSpec.builder(TRIGGER_EVENT, "event").build();
        ExecutableElement triggerExecutableElement = annotatedMethod.getExecutableElement();
        return getBaseMethodBuilder("onTrigger").addParameter(triggerEventParameter)
            .addStatement("target.$L($N)", triggerExecutableElement.getSimpleName(),
                triggerEventParameter)
            .build();
    }

    /**
     * Creates the implementation of {@code SensorEventListener#onSensorChanged(SensorEvent)} which
     * calls the annotated method on our target class.
     *
     * @param annotatedMethod Method annotated with {@code OnSensorChanged}.
     * @return {@link MethodSpec} of {@code SensorEventListener#onSensorChanged(SensorEvent)}.
     */
    @NonNull
    private static MethodSpec createOnSensorChangedListenerMethod(
        @Nullable AnnotatedMethod annotatedMethod) {
        ParameterSpec sensorEventParameter = ParameterSpec.builder(SENSOR_EVENT, "event").build();
        Builder methodBuilder =
            getBaseMethodBuilder("onSensorChanged").addParameter(sensorEventParameter);

        if (annotatedMethod != null) {
            ExecutableElement sensorChangedExecutableElement =
                annotatedMethod.getExecutableElement();
            methodBuilder.addStatement("target.$L($N)",
                sensorChangedExecutableElement.getSimpleName(), sensorEventParameter);
        }

        return methodBuilder.build();
    }

    /**
     * Creates the implementation of {@code SensorEventListener#onAccuracyChanged(Sensor, int)}
     * which calls the annotated method on our target class.
     *
     * @param annotatedMethod Method annotated with {@link OnAccuracyChanged}.
     * @return {@link MethodSpec} of {@code SensorEventListener#onAccuracyChanged(Sensor, int)}.
     */
    @NonNull
    private static MethodSpec createOnAccuracyChangedListenerMethod(
        @Nullable AnnotatedMethod annotatedMethod) {
        ParameterSpec sensorParameter = ParameterSpec.builder(SENSOR, "sensor").build();
        ParameterSpec accuracyParameter = ParameterSpec.builder(TypeName.INT, "accuracy").build();
        Builder methodBuilder =
            getBaseMethodBuilder("onAccuracyChanged").addParameter(sensorParameter)
                .addParameter(accuracyParameter);

        if (annotatedMethod != null) {
            ExecutableElement accuracyChangedExecutableElement =
                annotatedMethod.getExecutableElement();
            methodBuilder.addStatement("target.$L($N, $N)",
                accuracyChangedExecutableElement.getSimpleName(), sensorParameter,
                accuracyParameter);
        }

        return methodBuilder.build();
    }

    /**
     * Returns a delay to be used when registering the listener for the sensor. Both {@link
     * OnSensorChanged} and {@link OnAccuracyChanged} can have
     * a delay property set but only one can be used when registering the listener.
     * <p>
     * We try {@link OnSensorChanged} first, then {@link OnAccuracyChanged}, otherwise we return a
     * sentinel value that will be used for errors.
     *
     * @param sensorChangedAnnotatedMethod The method wrapper for the method with the {@link
     * OnSensorChanged} annotation.
     * @param accuracyChangedAnnotatedMethod The method wrapper for the method with the {@link
     * OnAccuracyChanged} annotation.
     * @return A delay value for the sensor listener.
     */
    private static int getDelayForListener(@Nullable AnnotatedMethod sensorChangedAnnotatedMethod,
        @Nullable AnnotatedMethod accuracyChangedAnnotatedMethod) {
        if (sensorChangedAnnotatedMethod != null
            && sensorChangedAnnotatedMethod.getDelay() != INVALID_DELAY) {
            return sensorChangedAnnotatedMethod.getDelay();
        } else if (accuracyChangedAnnotatedMethod != null
            && accuracyChangedAnnotatedMethod.getDelay() != INVALID_DELAY) {
            return accuracyChangedAnnotatedMethod.getDelay();
        }

        return INVALID_DELAY;
    }

    /**
     * Create the bind method for our generated class.
     *
     * @param targetParameter The target class that has annotated methods.
     * @param annotatedMethodsPerClass The annotated methods that are in a given class.
     * @return {@link MethodSpec} of the generated class bind method.
     */
    @NonNull
    private static MethodSpec createBindMethod(@NonNull ParameterSpec targetParameter,
        @NonNull AnnotatedMethodsPerClass annotatedMethodsPerClass) throws ProcessingException {
        Map<Integer, Map<Class, AnnotatedMethod>> itemsMap = annotatedMethodsPerClass.getItemsMap();

        Builder bindMethodBuilder = getBaseMethodBuilder("bind").addParameter(targetParameter)
            .addStatement("int sensorType")
            .addStatement("$T sensor", SENSOR)
            .beginControlFlow("for ($T wrapper : $N)", LISTENER_WRAPPER, LISTENER_WRAPPERS_FIELD)
            .addStatement("sensorType = wrapper.getSensorType()")
            .addStatement("sensor = wrapper.getSensor($N)", SENSOR_MANAGER_FIELD);

        if (annotatedMethodsPerClass.hasAnnotationsOfType(OnSensorNotAvailable.class)) {
            bindMethodBuilder.beginControlFlow("if (sensor == null)");

            // Iterate through our map of sensor types and check whether an OnSensorNotAvailable
            // annotation exists, if so and the sensor is unavailable call the method.
            List<Integer> sensorTypes = new ArrayList<>();
            sensorTypes.addAll(itemsMap.keySet());
            for (int i = 0; i < sensorTypes.size(); i++) {
                Integer sensorType = sensorTypes.get(i);
                Map<Class, AnnotatedMethod> annotationMap = itemsMap.get(sensorType);
                AnnotatedMethod annotatedMethod = annotationMap.get(OnSensorNotAvailable.class);

                if (annotatedMethod != null) {
                    checkAnnotatedMethodForErrors(annotatedMethod.getExecutableElement(),
                        OnSensorNotAvailable.class);

                    if (i == 0) {
                        bindMethodBuilder.beginControlFlow("if (sensorType == $L)", sensorType);
                    } else {
                        bindMethodBuilder.nextControlFlow("else if (sensorType == $L)", sensorType);
                    }

                    bindMethodBuilder.addStatement("$N.$L()", targetParameter,
                        annotatedMethod.getExecutableElement().getSimpleName());
                }
            }

            bindMethodBuilder.endControlFlow().addStatement("continue").endControlFlow();
        }

        return bindMethodBuilder.addStatement("wrapper.registerListener($N)", SENSOR_MANAGER_FIELD)
            .endControlFlow()
            .build();
    }

    /**
     * Return a {@link Builder} with the given method name and default properties.
     *
     * @param name The name of the method.
     * @return A base {@link Builder} to use for methods.
     */
    @NonNull
    private static Builder getBaseMethodBuilder(@NonNull String name) {
        return MethodSpec.methodBuilder(name)
            .addModifiers(Modifier.PUBLIC)
            .returns(void.class)
            .addAnnotation(Override.class);
    }

    /**
     * Check the annotated method for the correct parameters needed based on the annotation.
     *
     * @param element The annotated element.
     * @param annotation The annotation class being checked.
     * @throws ProcessingException
     */
    private static void checkAnnotatedMethodForErrors(@NonNull ExecutableElement element,
        @NonNull Class<? extends Annotation> annotation) throws ProcessingException {
        ListenerMethod method = annotation.getAnnotation(ListenerMethod.class);
        String[] expectedParameters = method.parameters();
        List<? extends VariableElement> parameters = element.getParameters();
        if (parameters.size() != expectedParameters.length) {
            String error = String.format("@%s methods can only have %s parameter(s). (%s.%s)",
                annotation.getSimpleName(), method.parameters().length,
                element.getEnclosingElement().getSimpleName(), element.getSimpleName());
            throw new ProcessingException(element, error);
        }

        for (int i = 0; i < parameters.size(); i++) {
            VariableElement parameter = parameters.get(i);
            TypeMirror methodParameterType = parameter.asType();
            String expectedType = expectedParameters[i];
            if (!expectedType.equals(methodParameterType.toString())) {
                String error = String.format(
                    "Method parameters are not valid for @%s annotated method. Expected parameters of type(s): %s. (%s.%s)",
                    annotation.getSimpleName(), Joiner.on(", ").join(expectedParameters),
                    element.getEnclosingElement().getSimpleName(), element.getSimpleName());
                throw new ProcessingException(element, error);
            }
        }
    }
}
