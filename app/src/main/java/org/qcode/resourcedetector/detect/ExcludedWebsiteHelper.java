package org.qcode.resourcedetector.detect;

import android.net.Uri;

import org.qcode.resourcedetector.base.utils.Utils;

import java.util.ArrayList;

/**
 * qqliu
 * 2016/12/4.
 */

class ExcludedWebsiteHelper {

    private ArrayList<String> mExcludedList = new ArrayList<String>();

    public ExcludedWebsiteHelper() {
        mExcludedList.add("assets.tumblr.com");
        mExcludedList.add("cookiex.ngd.yahoo.com");
    }

    public void addToExcluded(String host) {
        if(Utils.isEmpty(host)) {
            return;
        }

        host = host.toLowerCase();
        if(mExcludedList.contains(host)) {
            return;
        }

        mExcludedList.add(host);
    }

    public void removeFromExcluded(String host) {
        if(Utils.isEmpty(host)) {
            return;
        }

        host = host.toLowerCase();
        mExcludedList.remove(host);
    }

    public boolean isExcludedHost(String host) {
        if(Utils.isEmpty(host)) {
            return false;
        }

        host = host.toLowerCase();
        return mExcludedList.contains(host);
    }

    public boolean isExcludedUrl(String url) {
        Uri uri = Uri.parse(url);
        String host = uri.getHost();
        return isExcludedHost(host);
    }
}
