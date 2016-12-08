package org.qcode.resourcedetector.view.browser;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;

/**
 * qqliu
 * 2016/11/30.
 */

public class WebviewEventAdapter implements IWebviewEventListener {

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return false;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {

    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {

    }

    @Override
    public void onPageFinished(WebView view, String url) {

    }

    @Override
    public boolean onLoadResource(WebView view, String url) {
        return false;
    }

    @Override
    public boolean onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        return false;
    }

    @Override
    public boolean onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
        return false;
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {

    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {

    }
}
