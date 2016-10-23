package com.example.sensorannotations;

import android.annotation.TargetApi;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.TriggerEvent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.dvoiss.sensorannotations.OnAccuracyChanged;
import com.dvoiss.sensorannotations.OnSensorChanged;
import com.dvoiss.sensorannotations.OnSensorNotAvailable;
import com.dvoiss.sensorannotations.OnTrigger;
import com.dvoiss.sensorannotations.SensorAnnotations;

public class MainActivity extends AppCompatActivity {

    AccelerometerManager mAccelerometerManager;

    @BindView(R.id.magnetic_field_event_output) TextView mMagneticFieldEventOutputTextView;
    @BindView(R.id.light_event_output) TextView mLightEventOutputTextView;
    @BindView(R.id.heart_rate_event_output) TextView mHeartRateEventOutputTextView;
    @BindView(R.id.significant_motion_event_output) TextView mSignificantMotionEventOutputTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // Accelerometer is an example of binding to another object.
        mAccelerometerManager = new AccelerometerManager(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Binding should be done when the sensors are needed,
        // in this example the onResume is used,
        // in the AccelerometerManager class it is when a button is clicked.

        SensorAnnotations.bind(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Read the Sensors Overview docs:
        // developer.android.com/guide/topics/sensors/sensors_overview.html#sensors-practices
        //
        // "Be sure to unregister a sensor's listener when you
        // are done using the sensor or when the sensor activity pauses.
        //
        // If a sensor listener is registered and its activity is paused, the sensor will
        // continue to acquire data and use battery resources unless you unregister the sensor."

        SensorAnnotations.unbind(this);
        SensorAnnotations.unbind(mAccelerometerManager);
    }

    // region Magnetic Field Tests

    @OnSensorChanged(Sensor.TYPE_MAGNETIC_FIELD)
    void testMagneticFieldSensorChanged(@NonNull SensorEvent event) {
        updateTextViewWithEventData(mMagneticFieldEventOutputTextView, event);
    }

    @OnSensorNotAvailable(Sensor.TYPE_MAGNETIC_FIELD)
    void testMagneticFieldSensorNotAvailable() {
        updateTextViewWithSensorNotAvailable(mMagneticFieldEventOutputTextView);
    }

    @OnAccuracyChanged(Sensor.TYPE_MAGNETIC_FIELD)
    void testMagneticFieldAccuracyChanged(@NonNull Sensor sensor, int accuracy) {
        logAccuracyChangedForSensor(sensor, accuracy);
    }

    // endregion

    // region Heart Rate Tests

    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    @OnSensorChanged(Sensor.TYPE_HEART_RATE)
    public void testHeartRateSensorChanged(@NonNull SensorEvent event) {
        updateTextViewWithEventData(mHeartRateEventOutputTextView, event);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    @OnSensorNotAvailable(Sensor.TYPE_HEART_RATE)
    public void testHeartRateSensorNotAvailable() {
        updateTextViewWithSensorNotAvailable(mHeartRateEventOutputTextView);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    @OnAccuracyChanged(Sensor.TYPE_HEART_RATE)
    public void testHeartRateAccuracyChanged(@NonNull Sensor sensor, int accuracy) {
        logAccuracyChangedForSensor(sensor, accuracy);
    }

    // endregion

    // region Light Tests

    @OnSensorChanged(Sensor.TYPE_LIGHT)
    public void testLightSensorChanged(@NonNull SensorEvent event) {
        updateTextViewWithEventData(mLightEventOutputTextView, event);
    }

    @OnSensorNotAvailable(Sensor.TYPE_LIGHT)
    public void testLightSensorNotAvailable() {
        updateTextViewWithSensorNotAvailable(mLightEventOutputTextView);
    }

    @OnAccuracyChanged(Sensor.TYPE_LIGHT)
    public void testLightAccuracyChanged(@NonNull Sensor sensor, int accuracy) {
        logAccuracyChangedForSensor(sensor, accuracy);
    }

    // endregion

    // region Significant Motion Tests

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @OnTrigger
    public void testSignificantMotionTrigger(@NonNull TriggerEvent event) {
        updateTextViewWithEventData(mSignificantMotionEventOutputTextView, event);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @OnSensorNotAvailable(Sensor.TYPE_SIGNIFICANT_MOTION)
    public void testSignificantMotionSensorNotAvailable() {
        updateTextViewWithSensorNotAvailable(mSignificantMotionEventOutputTextView);
    }

    // endregion

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    void updateTextViewWithEventData(@NonNull TextView textView, @NonNull TriggerEvent event) {
        updateTextViewWithEventData(textView, event.timestamp, event.values);
    }

    void updateTextViewWithEventData(@NonNull TextView textView, @NonNull SensorEvent event) {
        updateTextViewWithEventData(textView, event.timestamp, event.values);
    }

    void updateTextViewWithEventData(@NonNull TextView textView, long timestamp, float[] values) {
        StringBuilder builder = new StringBuilder(String.valueOf(timestamp)).append(": (");
        for (int i = 0; i < values.length; i++) {
            if (i != 0) {
                builder.append(", ");
            }
            builder.append(values[i]);
        }
        builder.append(')');

        textView.setText(builder);
    }

    void updateTextViewWithSensorNotAvailable(@NonNull TextView textView) {
        textView.setText(getString(R.string.sensor_not_available));
    }

    void updateTextViewWithSensorNotBound(@NonNull TextView textView) {
        textView.setText(getString(R.string.sensor_not_bound));
    }

    void logAccuracyChangedForSensor(@NonNull Sensor sensor, int accuracy) {
        Log.i(getClass().getSimpleName(), sensor.getName() + " accuracy: " + accuracy);
    }
}
