package com.dvoiss.sensorannotations;

import org.junit.Test;

import static com.dvoiss.sensorannotations.TestUtils.shouldFailWithError;
import static com.dvoiss.sensorannotations.TestUtils.shouldGenerateBindingSource;

public class BindOnSensorNotAvailableTest {

    @Test
    public void bindOnSensorNotAvailableWithInvalidAnnotationParameter() {
        String source = "package test;\n"
            + "\n"
            + "import android.app.Activity;\n"
            + "import android.hardware.Sensor;\n"
            + "import com.dvoiss.sensorannotations.OnSensorNotAvailable;\n"
            + "\n"
            + "public class Test extends Activity {\n"
            + "    @OnSensorNotAvailable\n"
            + "    void testMagneticFieldSensorNotAvailable() {}\n"
            + "}\n";

        String error =
            "No sensor type specified in @OnSensorNotAvailable for method testMagneticFieldSensorNotAvailable. Set a sensor type such as Sensor.TYPE_ACCELEROMETER.";

        shouldFailWithError(source, error);
    }

    @Test
    public void bindOnSensorNotAvailableFailsWithInvalidNumberOfMethodParameter() {
        String source = "package test;\n"
            + "\n"
            + "import android.app.Activity;\n"
            + "import android.hardware.Sensor;\n"
            + "import com.dvoiss.sensorannotations.OnSensorNotAvailable;\n"
            + "\n"
            + "public class Test extends Activity {\n"
            + "    @OnSensorNotAvailable(Sensor.TYPE_MAGNETIC_FIELD)\n"
            + "    void testMagneticFieldSensorNotAvailable(int extra) {}\n"
            + "}\n";

        String error =
            "@OnSensorNotAvailable methods can only have 0 parameter(s). (Test.testMagneticFieldSensorNotAvailable)";

        shouldFailWithError(source, error);
    }

    @Test
    public void bindOnSensorNotAvailableSucceeds() {
        String source = "package test;\n"
            + "\n"
            + "import android.app.Activity;\n"
            + "import android.hardware.Sensor;\n"
            + "import com.dvoiss.sensorannotations.OnSensorNotAvailable;\n"
            + "\n"
            + "public class Test extends Activity {\n"
            + "    @OnSensorNotAvailable(Sensor.TYPE_MAGNETIC_FIELD)\n"
            + "    void testMagneticFieldSensorNotAvailable() {}\n"
            + "}";

        String bindingSource = "// This class is generated code from Sensor Lib. Do not modify!\n"
            + "package test;\n"
            + "\n"
            + "import static android.content.Context.SENSOR_SERVICE;\n"
            + "\n"
            + "import android.content.Context;\n"
            + "import android.hardware.Sensor;\n"
            + "import android.hardware.SensorManager;\n"
            + "import com.dvoiss.sensorannotations.internal.EventListenerWrapper;\n"
            + "import com.dvoiss.sensorannotations.internal.SensorBinder;\n"
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
            + "  }\n"
            + "\n"
            + "  @Override\n"
            + "  public void bind(final Test target) {\n"
            + "    int sensorType;\n"
            + "    Sensor sensor;\n"
            + "    for (EventListenerWrapper wrapper : listeners) {\n"
            + "      sensorType = wrapper.getSensorType();\n"
            + "      sensor = wrapper.getSensor(sensorManager);\n"
            + "      if (sensor == null) {\n"
            + "        if (sensorType == 2) {\n"
            + "          target.testMagneticFieldSensorNotAvailable();\n"
            + "        }\n"
            + "        continue;\n"
            + "      }\n"
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
