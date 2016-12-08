package org.qcode.resourcedetector;

import android.app.Application;
import android.content.Context;

import org.qcode.resourcedetector.base.Settings;
import org.qcode.resourcedetector.jsdetector.JSResourceDetector;

/**
 * qqliu
 * 2016/12/1.
 */

public class ResourceDetectorApp extends Application {

    private static Context mAppContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mAppContext = this;

        Settings.createInstance(mAppContext);

        JSResourceDetector.getInstance().init();
    }

    public static Context getAppContext() {
        return mAppContext;
    }
}
