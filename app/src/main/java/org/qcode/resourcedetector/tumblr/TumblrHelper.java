package org.qcode.resourcedetector.tumblr;

import android.net.Uri;

import org.qcode.resourcedetector.base.utils.Logging;
import org.qcode.resourcedetector.base.utils.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.ContentValues.TAG;

/**
 * author
 * 2016/12/5.
 */

public class TumblrHelper {

    public static String getUserNameFromUrl(String url) {
        if(Utils.isEmpty(url)) {
            return null;
        }

        Pattern pattern = Pattern.compile("(http://)([\\s\\S]*?)(.tumblr.com)");
        Matcher matcher = pattern.matcher(url);
        if(matcher.find()) {
            return matcher.group(2);
        }

        return null;
    }

    public static String getTumblrIdFromUrl(String url) {
        if(Utils.isEmpty(url)) {
            return null;
        }

        Pattern pattern = Pattern.compile("/(tumblr_.*?)/");
        Matcher matcher = pattern.matcher(url);
        if(matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    public static String getHostFromUrl(String url) {
        if(Utils.isEmpty(url)) {
            return null;
        }

        try {
            Uri uri = Uri.parse(url);
            return uri.getHost();
        } catch (Exception ex) {
            Logging.d(TAG, "getHostFromUrl()| error happened", ex);
        }

        return null;
    }

    public static String getRootDirName(String url) {
        if(Utils.isEmpty(url)) {
            return "";
        }

        //优先取userName
        String dirName = getUserNameFromUrl(url);

        //取不到userName就取host
        if(Utils.isEmpty(dirName)) {
            dirName = getHostFromUrl(url);
            dirName = dirName.replace('.', '_');
            dirName = dirName.replace(':', '_');
        }

        //还不行就直接返回url
        if(Utils.isEmpty(dirName)) {
            dirName = url;
        }

        return dirName;
    }

    public static String getSubDirName(String url) {
        if(Utils.isEmpty(url)) {
            return "";
        }

        String subDirName = TumblrHelper.getTumblrIdFromUrl(url);
        if(Utils.isEmpty(subDirName)) {
            subDirName = String.valueOf(Math.abs(url.hashCode()));
        }

        return subDirName;
    }
}
