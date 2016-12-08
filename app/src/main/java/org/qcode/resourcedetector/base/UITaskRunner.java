package org.qcode.resourcedetector.base;

import android.os.Handler;
import android.os.Looper;

/**
 * qqliu
 * 2016/11/25.
 */

public class UITaskRunner {

    private UITaskRunner() {
        // not allowed
        throw new RuntimeException("UITaskRunner cannot be constructed");
    }

    private static Handler mUIHandler;

    public static Handler getHandler() {
        if(null == mUIHandler) {
            mUIHandler = new Handler(Looper.getMainLooper());
        }
        return mUIHandler;
    }
}
