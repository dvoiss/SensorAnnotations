package com.dvoiss.sensorannotations;

import org.junit.Test;

import static com.dvoiss.sensorannotations.TestUtils.shouldFailWithError;
import static com.dvoiss.sensorannotations.TestUtils.shouldGenerateBindingSource;

public class BindOnTriggerTest {

    public void bindOnTriggerFailsWithInvalidMethodParameter() {
        String source = "package test;\n"
            + "\n"
            + "import android.app.Activity;\n"
            + "import com.dvoiss.sensorannotations.OnTrigger;\n"
            + "\n"
            + "public class Test extends Activity {\n"
            + "    @OnTrigger\n"
            + "    public void testSignificantMotionTrigger(Object wrongType) {}\n"
            + "}\n";

        String error =
            "Method parameters are not valid for @OnTrigger annotated method. Expected parameters of type(s): android.hardware.TriggerEvent. (Test.testSignificantMotionTrigger)";

        shouldFailWithError(source, error);
    }

    @Test
    public void bindOnTriggerFailsWithInvalidNumberOfMethodParameter() {
        String source = "package test;\n"
            + "\n"
            + "import android.app.Activity;\n"
            + "import android.hardware.TriggerEvent;\n"
            + "import com.dvoiss.sensorannotations.OnTrigger;\n"
            + "\n"
            + "public class Test extends Activity {\n"
            + "    @OnTrigger\n"
            + "    public void testSignificantMotionTrigger(TriggerEvent event, int extra) {}\n"
            + "}\n";

        String error =
            "@OnTrigger methods can only have 1 parameter(s). (Test.testSignificantMotionTrigger)";

        shouldFailWithError(source, error);
    }

    @Test
    public void bindOnTriggerSucceeds() {
        String source = "package test;\n"
            + "\n"
            + "import android.app.Activity;\n"
            + "import android.hardware.TriggerEvent;\n"
            + "import com.dvoiss.sensorannotations.OnTrigger;\n"
            + "\n"
            + "public class Test extends Activity {\n"
            + "    @OnTrigger\n"
            + "    public void testSignificantMotionTrigger(TriggerEvent event) {}\n"
            + "}\n";

        String bindingSource = "// This class is generated code from Sensor Lib. Do not modify!\n"
            + "package test;\n"
            + "\n"
            + "import static android.content.Context.SENSOR_SERVICE;\n"
            + "\n"
            + "import android.content.Context;\n"
            + "import android.hardware.Sensor;\n"
            + "import android.hardware.SensorManager;\n"
            + "import android.hardware.TriggerEventListener;\n"
            + "import com.dvoiss.sensorannotations.internal.EventListenerWrapper;\n"
            + "import com.dvoiss.sensorannotations.internal.SensorBinder;\n"
            + "import com.dvoiss.sensorannotations.internal.TriggerEventListenerWrapper;\n"
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
            + "    this.listeners.add(new TriggerEventListenerWrapper(new TriggerEventListener() {\n"
            + "          @java.lang.Override\n"
            + "          public void onTrigger(android.hardware.TriggerEvent event) {\n"
            + "            target.testSignificantMotionTrigger(event);\n"
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
