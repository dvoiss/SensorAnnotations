package com.example.sensorannotations;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.support.annotation.NonNull;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.dvoiss.sensorannotations.OnAccuracyChanged;
import com.dvoiss.sensorannotations.OnSensorChanged;
import com.dvoiss.sensorannotations.OnSensorNotAvailable;
import com.dvoiss.sensorannotations.SensorAnnotations;

class AccelerometerManager {
    @NonNull private final MainActivity mMainActivity;

    private TextView mAccelerometerManagerTextView;

    AccelerometerManager(@NonNull MainActivity mainActivity) {
        mMainActivity = mainActivity;

        mAccelerometerManagerTextView =
            (TextView) mMainActivity.findViewById(R.id.accelerometer_event_output);

        ToggleButton accelerometerButton =
            (ToggleButton) mMainActivity.findViewById(R.id.accelerometer_button);

        accelerometerButton.setOnCheckedChangeListener(
            new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        // Example of binding to another object, a Context is still needed:
                        SensorAnnotations.bind(AccelerometerManager.this, buttonView.getContext());
                    } else {
                        SensorAnnotations.unbind(AccelerometerManager.this);
                        mMainActivity.updateTextViewWithSensorNotBound(
                            mAccelerometerManagerTextView);
                    }
                }
            });
    }

    // region Accelerometer Tests

    @OnSensorChanged(Sensor.TYPE_ACCELEROMETER)
    void testTemperatureSensorChanged(@NonNull SensorEvent event) {
        mMainActivity.updateTextViewWithEventData(mAccelerometerManagerTextView, event);
    }

    @OnSensorNotAvailable(Sensor.TYPE_ACCELEROMETER)
    void testTemperatureSensorNotAvailable() {
        mMainActivity.updateTextViewWithSensorNotAvailable(mAccelerometerManagerTextView);
    }

    @OnAccuracyChanged(Sensor.TYPE_ACCELEROMETER)
    void testTemperatureAccuracyChanged(@NonNull Sensor sensor, int accuracy) {
        mMainActivity.logAccuracyChangedForSensor(sensor, accuracy);
    }

    // endregion
}
