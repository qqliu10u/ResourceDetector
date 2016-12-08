package org.qcode.resourcedetector.detect;

import android.graphics.Bitmap;
import android.webkit.WebView;

import org.qcode.resourcedetector.ResourceDetectorApp;
import org.qcode.resourcedetector.base.IObjectFactory;
import org.qcode.resourcedetector.base.LockWaitNotifyHelper;
import org.qcode.resourcedetector.base.ObjectPool;
import org.qcode.resourcedetector.base.UITaskRunner;
import org.qcode.resourcedetector.base.WeakReferenceHelper;
import org.qcode.resourcedetector.base.observable.IObservable;
import org.qcode.resourcedetector.base.observable.Observable;
import org.qcode.resourcedetector.base.utils.Logging;
import org.qcode.resourcedetector.base.utils.Utils;
import org.qcode.resourcedetector.common.DetectErrorCode;
import org.qcode.resourcedetector.common.ResourceDetectorConstant;
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

public class DetectActionHandler implements IObservable<IDetectEventListener> {
    private static final String TAG = "DetectActionHandler";

    //使用的WebView池
    private ObjectPool<BrowserWebView> mWebViewPool = new ObjectPool<BrowserWebView>();

    //探测事件监听
    private Observable<IDetectEventListener> mObservable = new Observable<IDetectEventListener>();

    //加载的页面链条管理类
    private DetectingItemHelper mDetectingItemHelper = new DetectingItemHelper();

    //等待加载的url队列
    private LinkedBlockingQueue<String> mWaitDetectingUrlQueue = new LinkedBlockingQueue<String>();

    //不支持探测的url管理
    private ExcludedWebsiteHelper mExcludedWebsiteHelper = new ExcludedWebsiteHelper();

    private static volatile DetectActionHandler mInstance;

    private DetectActionHandler() {
        mWebViewPool.setFactory(mWebViewFactory);
        mWebViewPool.setMaxSize(5);
    }

    public static DetectActionHandler getInstance() {
        if (null == mInstance) {
            synchronized (DetectActionHandler.class) {
                if (null == mInstance) {
                    mInstance = new DetectActionHandler();
                }
            }
        }
        return mInstance;
    }

    //在WebView内加载url
    public int detectUrl(String url, String parentUrl) {
        if (Utils.isEmpty(url)) {
            return DetectErrorCode.ILLEGAL_PARAM;
        }

        if (mExcludedWebsiteHelper.isExcludedUrl(url)) {
            return DetectErrorCode.URL_EXCLUDED;
        }

        if (Utils.isEmpty(parentUrl)) {
            mObservable.sendEvent("onDetectProgressBegin", url);
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

    //开始回收加载完成的webview
    private void recycleWebView(BrowserWebView webView, String url) {
        mDetectingItemHelper.setItemDetected(url);
        webView.loadOriginUrl(ResourceDetectorConstant.BLANK_PAGE);
        mWebViewPool.recycle(webView);

        ArrayList<String> detectedUrlList = mDetectingItemHelper.popDetectedRootItems();
        if (!Utils.isEmpty(detectedUrlList)) {
            for (String detectedUrl : detectedUrlList) {
                mObservable.sendEvent("onDetectProgressComplete", detectedUrl);
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
                    UITaskRunner.getHandler().post(new Runnable() {
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

                mObservable.sendEvent("onDetectUrlBegin", mDetectingItemHelper.getRootUrl(url), url);
            } finally {
                mLoadUrlLockHelper.signalWaiter();
            }
        }
    };

    @Override
    public void addObserver(IDetectEventListener observer) {
        mObservable.addObserver(observer);
    }

    @Override
    public void removeObserver(IDetectEventListener observer) {
        mObservable.removeObserver(observer);
    }

    @Override
    public void removeObservers() {
        mObservable.removeObservers();
    }

    @Override
    public int countObservers() {
        return mObservable.countObservers();
    }

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

    public boolean isDetecting(String url) {
        return mDetectingItemHelper.contains(url);
    }

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

            mObservable.sendEvent("onDetected",
                    mDetectingItemHelper.getRootUrl(pageUrl), type, url);

            //初始化用户名部分
            String userName = TumblrHelper.getRootDirName(pageUrl);

            //初始化pageTitle
            String subName = pageTitle;
            if(Utils.isEmpty(pageTitle)) {
                subName = TumblrHelper.getSubDirName(pageUrl);
            }

            DownloadController.getInstance().startDownload(
                    DownloadTask.create()
                            .setType(type)
                            .setDirectory(userName
                                    + File.separator
                                    + subName)
                            .setTitle(null)
                            .setDownloadUrl(url));
        }

        @Override
        public void onDetectCompleted(String pageUrl) {
            mObservable.sendEvent("onDetectUrlComplete", mDetectingItemHelper.getRootUrl(pageUrl), pageUrl);

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
