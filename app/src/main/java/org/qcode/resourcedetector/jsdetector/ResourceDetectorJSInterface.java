package org.qcode.resourcedetector.jsdetector;

import android.webkit.JavascriptInterface;

import org.qcode.resourcedetector.common.ResourceDetectorConstant;
import org.qcode.resourcedetector.base.UITaskRunner;
import org.qcode.resourcedetector.base.WeakReferenceHelper;
import org.qcode.resourcedetector.base.utils.Logging;
import org.qcode.resourcedetector.base.utils.Utils;
import org.qcode.resourcedetector.view.browser.BrowserWebView;

/**
 * qqliu
 * 2016/11/29.
 */

public class ResourceDetectorJSInterface {

    private static final String TAG = "ResourceDetectorJSInterface";

    private WeakReferenceHelper<BrowserWebView> mWebViewRef;

    private IActionListener mActionListener = null;

    public ResourceDetectorJSInterface(
            BrowserWebView webView, IActionListener listener) {
        mWebViewRef = new WeakReferenceHelper<BrowserWebView>(webView);
        mActionListener = listener;
    }

    @JavascriptInterface
    public void onDetected(final String type,
                           final String pageTitle,
                           final String url,
                           final String media,
                           final String mimeType) {
        final String decodedUrl = Utils.parseStringUnicodeToUtf8(url);

        if(null == mWebViewRef.getData()) {
            Logging.d(TAG, "onDetectCompleted()| webview is null");
            return;
        }

        final String pageUrl = mWebViewRef.getData().getOriginUrl();

//        Logging.d(TAG, "onDetected()| type= " + type + " title= " + title + " url= " + decodedUrl);

        UITaskRunner.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if(null != mActionListener) {
                    mActionListener.onDetected(type, pageUrl,
                            pageTitle, decodedUrl,
                            media, mimeType);
                }
            }
        });

    }

    @JavascriptInterface
    public void openUrlForDetect(String url) {
        final String decodedUrl = Utils.parseStringUnicodeToUtf8(url);

        Logging.d(TAG, "openUrlFromDetect()| url= " + decodedUrl);

        if(ResourceDetectorConstant.BLANK_PAGE.equalsIgnoreCase(decodedUrl)) {
            return;
        }

        UITaskRunner.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if(null != mActionListener) {
                    mActionListener.openUrl(decodedUrl);
                }
            }
        });

    }

    @JavascriptInterface
    public void onDetectCompleted() {
        if(null == mWebViewRef.getData()) {
            Logging.d(TAG, "onDetectCompleted()| webview is null");
            return;
        }

        final String url = mWebViewRef.getData().getOriginUrl();
        Logging.d(TAG, "onDetectCompleted()| url= " + url);
        if(ResourceDetectorConstant.BLANK_PAGE.equalsIgnoreCase(url)) {
            return;
        }

        UITaskRunner.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if(null != mActionListener) {
                    mActionListener.onDetectCompleted(url);
                }
            }
        });
    }

    public interface IActionListener {
        void openUrl(String url);
        void onDetected(String type, String pageUrl, String title, String url, String media, String mimeType);
        void onDetectCompleted(String url);
    }
}
