package com.dvoiss.sensorannotations;

import com.dvoiss.sensorannotations.exception.ProcessingException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The main annotation processor for the library. See {@link SensorAnnotationsFileBuilder} for more
 * info.
 */
public class SensorAnnotationsProcessor extends AbstractProcessor {
    private static final boolean DEBUG_LOGGING = false;

    @NonNull private Elements mElementUtils;
    @NonNull private Filer mFiler;
    @NonNull private Messager mMessager;

    /**
     * Mapping between classes and a wrapper object containing all the annotated methods on it.
     */
    @NonNull private final Map<String, AnnotatedMethodsPerClass> mGroupedMethodsMap =
        new LinkedHashMap<>();

    @Override
    public synchronized void init(@NonNull ProcessingEnvironment env) {
        super.init(env);
        mMessager = processingEnv.getMessager();
        mFiler = processingEnv.getFiler();
        mElementUtils = processingEnv.getElementUtils();
    }

    @Override
    public boolean process(@NonNull Set<? extends TypeElement> annotations,
        @NonNull RoundEnvironment roundEnv) {
        try {
            processAnnotation(OnSensorChanged.class, roundEnv);
            processAnnotation(OnAccuracyChanged.class, roundEnv);
            processAnnotation(OnSensorNotAvailable.class, roundEnv);
            processAnnotation(OnTrigger.class, roundEnv);
        } catch (ProcessingException e) {
            error(e.getElement(), e.getMessage());
        }

        try {
            warn(null, "Preparing to create %d generated classes.", mGroupedMethodsMap.size());

            // If we've gotten here we've found all the annotations and grouped them accordingly.
            // Now generate the SensorBinder classes.
            SensorAnnotationsFileBuilder.generateCode(mGroupedMethodsMap, mElementUtils, mFiler);

            // Clear the map so a future processing round doesn't re-process the same annotations.
            mGroupedMethodsMap.clear();
        } catch (IOException e) {
            error(null, e.getMessage());
        } catch (ProcessingException e) {
            error(e.getElement(), e.getMessage());
        }

        return true;
    }

    private void processAnnotation(Class<? extends Annotation> annotationClass,
        @NonNull RoundEnvironment roundEnv) throws ProcessingException {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotationClass);

        warn(null, "Processing %d elements annotated with @%s", elements.size(), elements);

        for (Element element : elements) {
            if (element.getKind() != ElementKind.METHOD) {
                throw new ProcessingException(element,
                    String.format("Only methods can be annotated with @%s",
                        annotationClass.getSimpleName()));
            } else {
                ExecutableElement executableElement = (ExecutableElement) element;

                try {
                    processMethod(executableElement, annotationClass);
                } catch (IllegalArgumentException e) {
                    throw new ProcessingException(executableElement, e.getMessage());
                }
            }
        }
    }

    private void processMethod(ExecutableElement executableElement,
        Class<? extends Annotation> annotationClass) throws ProcessingException {
        AnnotatedMethod annotatedMethod = new AnnotatedMethod(executableElement, annotationClass);

        checkMethodValidity(annotatedMethod);

        TypeElement enclosingClass = findEnclosingClass(annotatedMethod);
        if (enclosingClass == null) {
            throw new ProcessingException(null,
                String.format("Can not find enclosing class for method %s",
                    annotatedMethod.getExecutableElement().getSimpleName().toString()));
        } else {
            String enclosingClassName = enclosingClass.getQualifiedName().toString();
            AnnotatedMethodsPerClass groupedMethods = mGroupedMethodsMap.get(enclosingClassName);
            if (groupedMethods == null) {
                groupedMethods = new AnnotatedMethodsPerClass(enclosingClassName);
                mGroupedMethodsMap.put(enclosingClassName, groupedMethods);
            }

            groupedMethods.add(annotationClass, annotatedMethod);
        }
    }

    private void checkMethodValidity(@NonNull AnnotatedMethod item) throws ProcessingException {
        ExecutableElement methodElement = item.getExecutableElement();
        Set<Modifier> modifiers = methodElement.getModifiers();

        // The annotated method needs to be accessible by the generated class which will have
        // the same package. Public or "package private" (default) methods are required.
        if (modifiers.contains(Modifier.PRIVATE) || modifiers.contains(Modifier.PROTECTED)) {
            throw new ProcessingException(methodElement,
                String.format("The method %s can not be private or protected.",
                    methodElement.getSimpleName().toString()));
        }

        // We cannot annotate abstract methods, we need to annotate the actual implementation of
        // the method on the implementing class.
        if (modifiers.contains(Modifier.ABSTRACT)) {
            throw new ProcessingException(methodElement, String.format(
                "The method %s is abstract. You can't annotate abstract methods with @%s",
                methodElement.getSimpleName().toString(), AnnotatedMethod.class.getSimpleName()));
        }
    }

    @Nullable
    private TypeElement findEnclosingClass(@NonNull AnnotatedMethod annotatedMethod) {
        TypeElement enclosingClass;

        ExecutableElement methodElement = annotatedMethod.getExecutableElement();
        while (true) {
            Element enclosingElement = methodElement.getEnclosingElement();
            if (enclosingElement.getKind() == ElementKind.CLASS) {
                enclosingClass = (TypeElement) enclosingElement;
                break;
            }
        }

        return enclosingClass;
    }

    @NonNull
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new HashSet<>();
        types.add(OnSensorChanged.class.getCanonicalName());
        types.add(OnAccuracyChanged.class.getCanonicalName());
        types.add(OnSensorNotAvailable.class.getCanonicalName());
        types.add(OnTrigger.class.getCanonicalName());
        return types;
    }

    @NonNull
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private void error(@Nullable Element e, @NonNull String msg, @Nullable Object... args) {
        mMessager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
    }

    private void warn(@Nullable Element e, @NonNull String msg, @Nullable Object... args) {
        if (DEBUG_LOGGING) {
            mMessager.printMessage(Diagnostic.Kind.WARNING, String.format(msg, args), e);
        }
    }
}
