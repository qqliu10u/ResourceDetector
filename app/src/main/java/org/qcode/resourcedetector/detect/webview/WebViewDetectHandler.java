package org.qcode.resourcedetector.detect.webview;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.webkit.WebView;

import org.qcode.resourcedetector.ResourceDetectorApp;
import org.qcode.resourcedetector.base.IObjectFactory;
import org.qcode.resourcedetector.base.LockWaitNotifyHelper;
import org.qcode.resourcedetector.base.ObjectPool;
import org.qcode.resourcedetector.base.TaskRunner;
import org.qcode.resourcedetector.base.WeakReferenceHelper;
import org.qcode.resourcedetector.base.observable.Observable;
import org.qcode.resourcedetector.base.utils.Logging;
import org.qcode.resourcedetector.base.utils.Utils;
import org.qcode.resourcedetector.common.DetectErrorCode;
import org.qcode.resourcedetector.common.ResourceDetectorConstant;
import org.qcode.resourcedetector.detect.AbsDetectHandler;
import org.qcode.resourcedetector.detect.helper.DetectingItemHelper;
import org.qcode.resourcedetector.detect.interfaces.IDetectEventListener;
import org.qcode.resourcedetector.detect.interfaces.IDetectHandler;
import org.qcode.resourcedetector.download.DownloadController;
import org.qcode.resourcedetector.download.entities.DownloadTask;
import org.qcode.resourcedetector.jsdetector.JSResourceDetector;
import org.qcode.resourcedetector.jsdetector.ResourceDetectorJSInterface;
import org.qcode.resourcedetector.tumblr.TumblrHelper;
import org.qcode.resourcedetector.view.browser.BrowserWebView;
import org.qcode.resourcedetector.view.browser.WebviewEventAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class WebViewDetectHandler extends AbsDetectHandler {
    private static final String TAG = "WebViewDetectHandler";

    //使用的WebView池
    private ObjectPool<BrowserWebView> mWebViewPool = new ObjectPool<BrowserWebView>();

    //加载的页面链条管理类
    private DetectingItemHelper mDetectingItemHelper = new DetectingItemHelper();

    //等待加载的url队列
    private LinkedBlockingQueue<String> mWaitDetectingUrlQueue = new LinkedBlockingQueue<String>();

    //不支持探测的url管理
    private ExcludedWebsiteHelper mExcludedWebsiteHelper = new ExcludedWebsiteHelper();

    public WebViewDetectHandler() {
        mWebViewPool.setFactory(mWebViewFactory);
        mWebViewPool.setMaxSize(5);
    }

    @Override
    public boolean canDetect(String url) {
        return true;
    }

    //在WebView内加载url
    @Override
    public int detectUrl(String url, String parentUrl) {
        if (Utils.isEmpty(url)) {
            return DetectErrorCode.ILLEGAL_PARAM;
        }

        if (mExcludedWebsiteHelper.isExcludedUrl(url)) {
            return DetectErrorCode.URL_EXCLUDED;
        }

        if (Utils.isEmpty(parentUrl)) {
            sendEvent("onDetectProgressBegin", url);
        }

        //添加探测任务
        mDetectingItemHelper.addNewDetectingItem(url, parentUrl);
        mWaitDetectingUrlQueue.add(url);

        //开始任务执行
        if (Thread.State.NEW == mWorkThread.getState()) {
            mWorkThread.start();
        }

        return DetectErrorCode.OK;
    }

    @Override
    public boolean isDetecting(String url) {
        return mDetectingItemHelper.contains(url);
    }

    //开始回收加载完成的webview
    private void recycleWebView(BrowserWebView webView, String url) {
        mDetectingItemHelper.setItemDetected(url);
        webView.loadOriginUrl(ResourceDetectorConstant.BLANK_PAGE);
        mWebViewPool.recycle(webView);

        ArrayList<String> detectedUrlList = mDetectingItemHelper.popDetectedRootItems();
        if (!Utils.isEmpty(detectedUrlList)) {
            for (String detectedUrl : detectedUrlList) {
                sendEvent("onDetectProgressComplete", detectedUrl);
            }
        }
    }

    private boolean mIsRunning = true;
    private LockWaitNotifyHelper mLoadUrlLockHelper = new LockWaitNotifyHelper(null);
    private Thread mWorkThread = new Thread() {

        @Override
        public void run() {
            super.run();

            while (mIsRunning) {
                try {
                    final String url = mWaitDetectingUrlQueue.take();

                    final BrowserWebView webView = mWebViewPool.pop();
                    mLoadUrlLockHelper.beginLockAction();
                    TaskRunner.getUIHandler().post(new Runnable() {
                                @Override
                                public void run() {
                                    loadUrlInWebview(webView, url);
                                }
                            }
                    );
                    mLoadUrlLockHelper.waitForSignal();
                } catch (InterruptedException ex) {
                    Logging.d(TAG, "run()| error happened", ex);
                }
            }
        }

        private void loadUrlInWebview(BrowserWebView webView, String url) {
            try {
                webView.loadOriginUrl(url);

                sendEvent("onDetectUrlBegin", mDetectingItemHelper.getRootUrl(url), url);
            } finally {
                mLoadUrlLockHelper.signalWaiter();
            }
        }
    };

    private IObjectFactory<BrowserWebView> mWebViewFactory = new IObjectFactory<BrowserWebView>() {

        @Override
        public BrowserWebView createObject() {
            Logging.d(TAG, "createObject()");
            BrowserWebView webView = new BrowserWebView(
                    ResourceDetectorApp.getAppContext());
            webView.addJavascriptInterface(
                    new ResourceDetectorJSInterface(
                            webView, new DetectActionListener(webView)),
                    "resourceDetectorListener");
            webView.getSettings().setLoadsImagesAutomatically(false);
            webView.setBrowserCoreListener(
                    new MyBrowserCoreListener(webView));
            return webView;
        }
    };

    private class DetectActionListener implements ResourceDetectorJSInterface.IActionListener {

        private BrowserWebView mWebView;

        public DetectActionListener(BrowserWebView webView) {
            this.mWebView = webView;
        }

        @Override
        public void openUrl(String pageUrl) {
            if (mExcludedWebsiteHelper.isExcludedUrl(pageUrl)) {
                Logging.d(TAG, "openUrl()| the url is excluded");
                return;
            }

            detectUrl(pageUrl, mWebView.getOriginUrl());
        }

        @Override
        public void onDetected(String type, String pageUrl, String pageTitle, String url,
                               String media, String mimeType) {
            if(null == pageUrl) {
                pageUrl = "";
            }

            sendEvent("onDetected",
                    mDetectingItemHelper.getRootUrl(pageUrl), type, url);
        }

        @Override
        public void onDetectCompleted(String pageUrl) {
            sendEvent("onDetectUrlComplete", mDetectingItemHelper.getRootUrl(pageUrl), pageUrl);

            recycleWebView(mWebView, pageUrl);
        }
    }

    private static class MyBrowserCoreListener extends WebviewEventAdapter {

        private WeakReferenceHelper<WebView> mWebViewRef;

        public MyBrowserCoreListener(WebView webView) {
            mWebViewRef = new WeakReferenceHelper<WebView>(webView);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Logging.d(TAG, "onPageStarted()| url= " + url);

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Logging.d(TAG, "onPageFinished()| url= " + url);
            if(ResourceDetectorConstant.BLANK_PAGE.equalsIgnoreCase(url)) {
                return;
            }

            loadJavascript();
        }

        private void loadJavascript() {
            if (null == mWebViewRef.getData()) {
                return;
            }

            mWebViewRef.getData().loadUrl(
                    JSResourceDetector.getInstance().getDetectJavascript());
        }
    }
}
