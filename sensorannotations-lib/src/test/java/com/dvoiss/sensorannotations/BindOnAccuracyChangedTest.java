package com.dvoiss.sensorannotations;

import org.junit.Test;

import static com.dvoiss.sensorannotations.TestUtils.shouldFailWithError;
import static com.dvoiss.sensorannotations.TestUtils.shouldGenerateBindingSource;

public class BindOnAccuracyChangedTest {

    @Test
    public void bindOnAccuracyChangedFailsWithInvalidAnnotationParameter() {
        String source = "package test;\n"
            + "\n"
            + "import android.app.Activity;\n"
            + "import android.hardware.Sensor;\n"
            + "import com.dvoiss.sensorannotations.OnAccuracyChanged;\n"
            + "\n"
            + "public class Test extends Activity {\n"
            + "    @OnAccuracyChanged\n"
            + "    void testMagneticFieldAccuracyChanged(Sensor sensor, int accuracy) {}\n"
            + "}\n";

        String error =
            "No sensor type specified in @OnAccuracyChanged for method testMagneticFieldAccuracyChanged. Set a sensor type such as Sensor.TYPE_ACCELEROMETER.";

        shouldFailWithError(source, error);
    }

    @Test
    public void bindOnAccuracyChangedFailsWithInvalidMethodParameter() {
        String source = "package test;\n"
            + "\n"
            + "import android.app.Activity;\n"
            + "import android.hardware.Sensor;\n"
            + "import com.dvoiss.sensorannotations.OnAccuracyChanged;\n"
            + "\n"
            + "public class Test extends Activity {\n"
            + "    @OnAccuracyChanged(Sensor.TYPE_MAGNETIC_FIELD)\n"
            + "    void testMagneticFieldAccuracyChanged(Object wrongType, int accuracy) {}\n"
            + "}\n";

        String error =
            "Method parameters are not valid for @OnAccuracyChanged annotated method. Expected parameters of type(s): android.hardware.Sensor, int. (Test.testMagneticFieldAccuracyChanged)";

        shouldFailWithError(source, error);
    }

    @Test
    public void bindOnAccuracyChangedFailsWithInvalidNumberOfMethodParameter() {
        String source = "package test;\n"
            + "\n"
            + "import android.app.Activity;\n"
            + "import android.hardware.Sensor;\n"
            + "import com.dvoiss.sensorannotations.OnAccuracyChanged;\n"
            + "\n"
            + "public class Test extends Activity {\n"
            + "    @OnAccuracyChanged(Sensor.TYPE_MAGNETIC_FIELD)\n"
            + "    void testMagneticFieldSensorChanged(Sensor sensor, int accuracy, int extra) {}\n"
            + "}\n";

        String error =
            "@OnAccuracyChanged methods can only have 2 parameter(s). (Test.testMagneticFieldSensorChanged)";

        shouldFailWithError(source, error);
    }

    @Test
    public void bindOnAccuracyChangedSucceeds() {
        String source = "package test;\n"
            + "\n"
            + "import android.app.Activity;\n"
            + "import android.hardware.Sensor;\n"
            + "import com.dvoiss.sensorannotations.OnAccuracyChanged;\n"
            + "\n"
            + "public class Test extends Activity {\n"
            + "    @OnAccuracyChanged(Sensor.TYPE_MAGNETIC_FIELD)\n"
            + "    void testMagneticFieldAccuracyChanged(Sensor sensor, int accuracy) {}\n"
            + "}\n";

        String bindingSource = "// This class is generated code from Sensor Lib. Do not modify!\n"
            + "package test;\n"
            + "\n"
            + "import static android.content.Context.SENSOR_SERVICE;\n"
            + "\n"
            + "import android.content.Context;\n"
            + "import android.hardware.Sensor;\n"
            + "import android.hardware.SensorEventListener;\n"
            + "import android.hardware.SensorManager;\n"
            + "import com.dvoiss.sensorannotations.internal.EventListenerWrapper;\n"
            + "import com.dvoiss.sensorannotations.internal.SensorBinder;\n"
            + "import com.dvoiss.sensorannotations.internal.SensorEventListenerWrapper;\n"
            + "import java.lang.Override;\n"
            + "import java.util.ArrayList;\n"
            + "import java.util.List;\n"
            + "\n"
            + "final class Test$$SensorBinder implements SensorBinder<Test> {\n"
            + "  private final SensorManager sensorManager;\n"
            + "\n"
            + "  private final List<EventListenerWrapper> listeners;\n"
            + "\n"
            + "  public Test$$SensorBinder(Context context, final Test target) {\n"
            + "    this.sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);\n"
            + "    this.listeners = new ArrayList();\n"
            + "    this.listeners.add(new SensorEventListenerWrapper(2, 3, new SensorEventListener() {\n"
            + "          @java.lang.Override\n"
            + "          public void onSensorChanged(android.hardware.SensorEvent event) {\n"
            + "          }\n"
            + "          @java.lang.Override\n"
            + "          public void onAccuracyChanged(android.hardware.Sensor sensor, int accuracy) {\n"
            + "            target.testMagneticFieldAccuracyChanged(sensor, accuracy);\n"
            + "          }\n"
            + "        }));\n"
            + "  }\n"
            + "\n"
            + "  @Override\n"
            + "  public void bind(final Test target) {\n"
            + "    int sensorType;\n"
            + "    Sensor sensor;\n"
            + "    for (EventListenerWrapper wrapper : listeners) {\n"
            + "      sensorType = wrapper.getSensorType();\n"
            + "      sensor = wrapper.getSensor(sensorManager);\n"
            + "      wrapper.registerListener(sensorManager);\n"
            + "    }\n"
            + "  }\n"
            + "\n"
            + "  @Override\n"
            + "  public void unbind() {\n"
            + "    if (this.sensorManager != null) {\n"
            + "      for (EventListenerWrapper wrapper : listeners) {\n"
            + "        wrapper.unregisterListener(sensorManager);\n"
            + "      }\n"
            + "    }\n"
            + "  }\n"
            + "}\n";

        shouldGenerateBindingSource(source, bindingSource);
    }
}
