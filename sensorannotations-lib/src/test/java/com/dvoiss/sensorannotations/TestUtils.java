package com.dvoiss.sensorannotations;

import com.google.testing.compile.CompileTester;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaFileObjects.forSourceString;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

class TestUtils {
    static void shouldFailWithError(String source, String error) {
        getBaseCompileTester(source).failsToCompile().withErrorContaining(error);
    }

    static void shouldGenerateBindingSource(String source, String bindingSource) {
        getBaseCompileTester(source).compilesWithoutError()
            .and()
            .generatesSources(forSourceString("test/Test$$SensorBinder", bindingSource));
    }

    private static CompileTester getBaseCompileTester(String source) {
        return assertAbout(javaSource()).that(forSourceString("test.Test", source))
            .withCompilerOptions("-Xlint:-processing")
            .processedWith(new SensorAnnotationsProcessor());
    }
}
