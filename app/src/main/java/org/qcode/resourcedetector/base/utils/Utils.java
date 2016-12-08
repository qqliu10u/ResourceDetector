package org.qcode.resourcedetector.base.utils;

import java.io.UnsupportedEncodingException;
import java.util.Collection;

/**
 * qqliu
 * 2016/7/14.
 */
public class Utils {

    public static boolean isEmpty(Collection<?> collection) {
        return null == collection || collection.size() <= 0;
    }

    public static <T> boolean isEmpty(T[] array) {
        return null == array || array.length <= 0;
    }

    public static boolean isEmpty(CharSequence charSequence) {
        return null == charSequence || charSequence.length() <= 0;
    }

    public static void assertNotNull(Object object, String exceptionTip) {
        if (null == object) {
            throw new RuntimeException(exceptionTip);
        }
    }

    public static String parseString(byte[] data) {
        try {
            return new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static String parseStringUnicodeToUtf8(String str) {
        try {
            byte[] utf8 = str.getBytes("UTF-8");
            str = new String(utf8, "UTF-8");
            return str;
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
}
