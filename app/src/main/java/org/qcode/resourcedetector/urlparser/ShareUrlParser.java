package org.qcode.resourcedetector.urlparser;

import android.content.Intent;
import android.net.Uri;

/**
 * qqliu
 * 2016/11/28.
 */

public class ShareUrlParser {

    public static String parseShareUrl(Intent intent) {
        if(null == intent) {
            return null;
        }

        String action = intent.getAction();
        if(!Intent.ACTION_SEND.equals(action)) {
            return null;
        }

        Uri stream = (Uri)intent.getSerializableExtra(Intent.EXTRA_STREAM);
        String subject = intent.getStringExtra(Intent.EXTRA_SUBJECT);
        String content = intent.getStringExtra(Intent.EXTRA_TEXT);

        return content;
    }
}
