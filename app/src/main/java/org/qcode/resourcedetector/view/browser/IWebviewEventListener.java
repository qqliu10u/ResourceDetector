package org.qcode.resourcedetector.view.browser;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;

/**
 * 浏览器内核监听器
 * 
 * @author mdhuang 2011-10-23
 * 
 */
public interface IWebviewEventListener {
	boolean shouldOverrideUrlLoading(WebView view, String url);

	void onPageStarted(WebView view, String url, Bitmap favicon);

	void onProgressChanged(WebView view, int newProgress);

	void onPageFinished(WebView view, String url);

	boolean onLoadResource(WebView view, String url);

	boolean onReceivedError(WebView view, int errorCode, String description, String failingUrl);

	boolean onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength);

	void onReceivedTitle(WebView view, String title);

	void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error);
}
