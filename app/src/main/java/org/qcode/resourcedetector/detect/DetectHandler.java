package org.qcode.resourcedetector.detect;

import org.qcode.resourcedetector.common.DetectErrorCode;
import org.qcode.resourcedetector.detect.tumblr.TumblrDetectHandler;
import org.qcode.resourcedetector.detect.webview.WebViewDetectHandler;

import java.util.ArrayList;

/***
 * author: author
 * created at 2017/7/30
 */
public class DetectHandler {
    private static ArrayList<AbsDetectHandler> mDetectHandlerList = new ArrayList<AbsDetectHandler>();
    static {
        mDetectHandlerList.add(new TumblrDetectHandler());
        mDetectHandlerList.add(new WebViewDetectHandler());
    }

    public static AbsDetectHandler getHandler(String url) {
        for (AbsDetectHandler handler : mDetectHandlerList) {
            if (handler.canDetect(url)) {
                return handler;
            }
        }

        return new EmptyDetectHandler();
    }

    public static AbsDetectHandler getTumblrHandler() {
        for (AbsDetectHandler handler : mDetectHandlerList) {
            if (handler instanceof TumblrDetectHandler) {
                return handler;
            }
        }

        return new TumblrDetectHandler();
    }

    public static AbsDetectHandler getWebHandler() {
        for (AbsDetectHandler handler : mDetectHandlerList) {
            if (handler instanceof WebViewDetectHandler) {
                return handler;
            }
        }

        return new WebViewDetectHandler();
    }

    private static class EmptyDetectHandler extends AbsDetectHandler {
        @Override
        public boolean canDetect(String url) {
            return false;
        }

        @Override
        public int detectUrl(String url, String parentUrl) {
            return DetectErrorCode.ILLEGAL_PARAM;
        }

        @Override
        public boolean isDetecting(String url) {
            return false;
        }
    }
}
