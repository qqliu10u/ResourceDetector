package org.qcode.resourcedetector;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

/**
 * qqliu
 * 2016/12/2.
 */

public class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    public <T extends View> T castViewById(int id) {
        return (T)findViewById(id);
    }
}
