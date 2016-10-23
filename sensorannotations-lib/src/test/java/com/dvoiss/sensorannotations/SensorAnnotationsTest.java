package com.dvoiss.sensorannotations;

import android.content.Context;
import com.dvoiss.sensorannotations.internal.SensorBinder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowLog;

import static com.dvoiss.sensorannotations.SensorAnnotations.NO_OP_VIEW_BINDER;
import static com.google.common.truth.Truth.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class SensorAnnotationsTest {
    private final Context mContext = ShadowApplication.getInstance().getApplicationContext();

    @Before
    public void resetBinderCache() {
        ShadowLog.stream = System.out;
        SensorAnnotations.BINDER_CACHE.clear();
    }

    @Test(expected = RuntimeException.class)
    public void validTargetAndNullContextParameterThrowsException() {
        class Example {}
        SensorAnnotations.bind(new Example(), null);
    }

    @Test(expected = RuntimeException.class)
    public void nullContextParameterThrowsException() {
        SensorAnnotations.bind(null);
    }

    @Test(expected = RuntimeException.class)
    public void nullTargetParameterThrowsException() {
        SensorAnnotations.bind(null, mContext);
    }

    @Test
    public void bindingFrameworkPackagesAreNotCached() {
        SensorAnnotations.bind(mContext);
        assertThat(SensorAnnotations.BINDER_CACHE).isEmpty();
        SensorAnnotations.bind(new Object(), mContext);
        assertThat(SensorAnnotations.BINDER_CACHE).isEmpty();
    }

    @Test
    public void findsNoOpViewBinderWithInvalidConstructor() {
        class Example {}
        Example example = new Example();
        SensorAnnotations.bind(example, mContext);
        SensorBinder sensorBinder = SensorAnnotations.BINDER_CACHE.get(example.getClass());
        assertThat(sensorBinder).isSameAs(NO_OP_VIEW_BINDER);
    }
}
