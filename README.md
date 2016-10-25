# SensorAnnotations

Annotate methods to use as listeners for sensor events.

[![Build Status](https://img.shields.io/travis/dvoiss/SensorAnnotations.svg?style=flat-square)](https://travis-ci.org/dvoiss/SensorAnnotations)
[![Download](https://api.bintray.com/packages/dvoiss/maven/sensorannotations/images/download.svg)](https://bintray.com/dvoiss/maven/sensorannotations/_latestVersion)

```java
public class MyActivity extends Activity {
    /**
     * Perform actions as accelerometer data changes...
     */
    @OnSensorChanged(Sensor.TYPE_ACCELEROMETER)
    void accelerometerSensorChanged(@NonNull SensorEvent event) {
        doSomething(event.values);
    }

    /**
     * If the sensor isn't available, update UI accordingly...
     */
    @OnSensorNotAvailable(Sensor.TYPE_ACCELEROMETER)
    void testTemperatureSensorNotAvailable() {
        hideAccelerometerUi();
    }
    
    @Override protected void onResume() {
        super.onResume();
        SensorAnnotations.bind(this);
    }

    @Override protected void onPause() {
        super.onPause();
        SensorAnnotations.unbind(this); // Unbind to save the user's battery life.
    }
}
```

There are four possible annotations: `@OnSensorChanged`, `@OnAccuracyChanged`, `@OnSensorNotAvailable`, and `@OnTrigger`. The annotated methods must have the method signatures specified in the [Sensors Overview](https://developer.android.com/guide/topics/sensors/sensors_overview.html) Android docs.

```java
@OnSensorChanged(Sensor.TYPE_HEART_RATE)
void method(@NonNull SensorEvent event) {}

// or the following syntax can be used which accepts a delay value:
@OnSensorChanged(value = Sensor.TYPE_LIGHT, delay = SensorManager.SENSOR_DELAY_NORMAL)
void method(@NonNull SensorEvent event) {}

@OnAccuracyChanged(Sensor.TYPE_MAGNETIC_FIELD)
void method(@NonNull Sensor sensor, int accuracy) {}

@OnSensorNotAvailable(Sensor.TYPE_AMBIENT_TEMPERATURE)
void method() {}

@OnTrigger
void method(@NonNull TriggerEvent event) {}
```

For information about sensor delays and accuracy events see the ["Monitoring Sensor Events"](https://developer.android.com/guide/topics/sensors/sensors_overview.html#sensors-monitor) portion of the Android docs.

Calling `SensorAnnotations.bind` should be done when you want to start receiving sensor events. Because this consumes battery life you need to call `unbind` when you are finished. The `bind` method needs to take a `Context` object. There are two variations:

```java
SensorAnnotations.bind(context);
// Use this alternative to bind to a different target. See the example application.
SensorAnnotations.bind(this, context);
```

The `@OnTrigger` annotation is a specific annotation for sensors of `TYPE_SIGNIFICANT_MOTION` (introduced in 4.3). This type has a different method and parameter than the others. For more info see the Android docs on [Using the Significant Motion Sensor](https://developer.android.com/guide/topics/sensors/sensors_motion.html#sensors-motion-significant).

## How does it work?

A binding class is created for each class that has annotations. In the example app, the classes `MainActivity` and `AccelerometerManager` will have two classes generated at compile time: `MainActivity$$SensorBinder` and `AccelerometerManager$$SensorBinder`. Because these classes are generated at compile time no reflection is needed.

These classes register the listener with the sensor system service. If the sensor isn't available on the device and a method has been annotated with `@OnSensorNotAvailable` it will be invoked. If an accuracy event occurs and a method has been annotated with `@OnAccuracyChanged` it will be invoked. The `TYPE_SIGNIFICANT_MOTION` sensor doesn't have an accuracy callback.

## Use in your project

```groovy
buildscript {
    dependencies {
        classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'
    }
}

apply plugin: 'com.neenbedankt.android-apt'

dependencies {
    compile 'com.dvoiss:sensorannotations:0.1.0'
    apt 'com.dvoiss:sensorannotations-compiler:0.1.0'
}
```

Using Android Gradle Plugin version 2.2.0+:

```groovy
dependencies {
    compile 'com.dvoiss:sensorannotations:0.1.0'
    annotationProcessor 'com.dvoiss:sensorannotations-compiler:0.1.0'
}
```
