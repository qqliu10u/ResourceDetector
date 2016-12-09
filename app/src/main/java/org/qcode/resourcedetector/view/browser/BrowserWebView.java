package org.qcode.resourcedetector.view.browser;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.qcode.resourcedetector.base.utils.Logging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/***
 * webview
 */
public class BrowserWebView extends WebView {

    private static final String TAG = "BrowserWebView";

    private static final boolean SHOW_WEB_LOG = false;

    /**
     * webview辅助控制实例
     */
    protected IFlyWebViewClient webViewClient;

    /**
     * webview辅助控制实例
     */
    protected IFlyWebChromeClient chromeClient;

    /**
     * Web事件监听器
     */
    protected IWebviewEventListener browserCoreListener;

    /**
     * 是否由上层应用程序处理回退键
     */
    protected boolean appHandleBackEvent;

    /**
     * 程序上下文对象
     */
    protected Context context;

    /**
     * 是否对focus变化时进行处理，默认为true
     */
    protected boolean isHandleFocusChange = true;

    /**
     * 是否已销毁
     */
    protected boolean isDestroy;

    /**
     * 是否监听返回键
     */
    protected boolean isListenBackKeyEvent = true;

    /**
     * 是否忽略电话号码，即不解析页面中的电话格式
     */
    protected boolean isSkipTelNumber = false;

    /**
     * 当前url，在onPageStart时赋值
     */
    protected String currentUrl;

    /**
     * 异常界面
     */
    private View mErrorView;

    /**
     * 保存异常界面使用的drawable，在界面销毁时，逐个回收，确保图片资源被回收
     */
    private List<Drawable> mDrawableListForRecycle;

    /**
     * 图片资源路径
     */
    private static final String ASSET_IMAGE_PATH = "image/";


    /**
     * 最后一次点击时间，两次点击刷新时间间隔小于1000毫秒不做处理
     */
    private static long lastClickTime = -1;
    private String mOriginUrl;

    /**
     * 构造函数
     *
     * @param context
     */
    public BrowserWebView(Context context) {
        super(context);
        this.context = context;
        initView();
    }

    /**
     * 构造函数
     *
     * @param context
     * @param isHandleFocusChange
     */
    public BrowserWebView(Context context, boolean isHandleFocusChange) {
        super(context);
        this.context = context;
        this.isHandleFocusChange = isHandleFocusChange;
        initView();
    }

    /**
     * 构造函数
     *
     * @param context
     * @param attrs
     */
    public BrowserWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
        this.context = context;
    }

    /**
     * 添加监听器
     *
     * @param listener
     */
    public void setBrowserCoreListener(IWebviewEventListener listener) {
        browserCoreListener = listener;
    }

    private void log(String text) {
        if(!SHOW_WEB_LOG) {
            return;
        }
        Logging.d(TAG, text);
    }

    private void log(String text, Throwable ex) {
        if(!SHOW_WEB_LOG) {
            return;
        }
        Logging.d(TAG, text, ex);
    }

    /**
     * 初始化webview
     */
    private void initView() {
        //设置webview不监听返回键
        setListenBackKeyEvent(false);

        this.setInitialScale(0);
        // 获取焦点，不然webview中的输入框无法调出输入法
        this.requestFocusFromTouch();

        // 设置滚动条悬浮模式
        this.setVerticalScrollbarOverlay(true);
        this.setHorizontalScrollbarOverlay(true);

        // Enable JavaScript
        WebSettings settings = this.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setLayoutAlgorithm(LayoutAlgorithm.NORMAL);

        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

        // 设置渲染优先级
        settings.setRenderPriority(RenderPriority.HIGH);

        if (getOSVersionCode() >= 7) {
            //LocalStorage设置
            settings.setDomStorageEnabled(true);
            settings.setDatabaseEnabled(true);
            //设置数据库路径，在dada目录下会出现app_database文件夹
            String app_database_url = getContext().getDir("database", Context.MODE_PRIVATE).getPath();
            settings.setDatabasePath(app_database_url);

            //定位设置，还需在WebChromeClient中onGeolocationPermissionsShowPrompt回调中设置权限
            settings.setGeolocationEnabled(true);
            settings.setGeolocationDatabasePath(app_database_url);

            //离线缓存设置，还需在WebChromeClient中onReachedMaxAppCacheSize扩充缓存容量
            //另外还需修改服务器设置，使其支持text/cache-manifest的mime类型
            settings.setAppCacheMaxSize(8 * 1024 * 1024);//缓存大小
            settings.setAllowFileAccess(true);//可以读取文件缓存(manifest生效)
            settings.setAppCacheEnabled(true);//启用离线缓存功能
            String appCaceDir = getContext().getDir("cache", Context.MODE_PRIVATE).getPath();
            settings.setAppCachePath(appCaceDir);
        }

        // 解决4.1及以上版本从本地文件访问远程服务的权限问题
        if (getOSVersionCode() >= 16) {
            try {
                Class<? extends WebSettings> clazz = settings.getClass();
                Method method = clazz.getMethod("setAllowUniversalAccessFromFileURLs", boolean.class);
                if (method != null) {
                    method.invoke(settings, true);
                }
            } catch (Exception e) {
                log("reflect setAllowUniversalAccessFromFileURLs fail", e);
            }
        }

        // 关闭插件，规避部分手机上出现的webkit监听程序安装/卸载导致的崩溃问题
        if (getOSVersionCode() >= 8) {
            log("SDK version >= 8(2.2) -> setPluginState(PluginState.OFF)");
            settings.setPluginState(PluginState.OFF);
        }

        webViewClient = new IFlyWebViewClient();
        chromeClient = new IFlyWebChromeClient();
        setWebViewClient(webViewClient);
        setWebChromeClient(chromeClient);

        // 设置浏览器下载监听
        setDownloadListener(new IFlyWebViewDownLoadListener());

        enableOverScrollMode();
    }

    @SuppressLint("NewApi")
    private void enableOverScrollMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            this.setOverScrollMode(OVER_SCROLL_NEVER);
        }
    }

    public String getOriginUrl() {
        return mOriginUrl;
    }

    public void loadOriginUrl(String url) {
        mOriginUrl = url;
        loadUrl(url);
    }

    @Override
    public void loadUrl(String url) {
        try {
            log("loadUrl :" + url);
            super.loadUrl(url);
        } catch (Exception e) {
            log("loadUrl error", e);
        }
    }

    @Override
    public void reload() {
        log("reload, url is " + getUrl());
        super.reload();
        // 隐藏异常界面
        if (mErrorView != null) {
            mErrorView.setVisibility(View.GONE);
        }
    }

    /**
     * 调用javascript代码
     *
     * @param javascriptCode
     */
    public void loadJavaScript(final String javascriptCode) {
        log("loadJavaScript:" + javascriptCode + " ,current thread is " + Thread.currentThread().getName());
        if (isDestroy) {
            log("webview is destroyed, so not loadJavaScript");
            return;
        }

        //解决非UI线程调用js更新界面的问题
        if (context != null && context instanceof Activity) {
            //runOnUiThread实现机制：如果当前是ui线程则直接运行，否则post到ui线程再运行
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        BrowserWebView.this.loadUrl("javascript:" + javascriptCode);
                    } catch (Throwable t) {
                        log("loadUrl error", t);
                    }
                }
            });
        } else {
            log("context is null or is not activity context");
        }
    }

    /**
     * 程序退出处理
     */
    public void onDestroy() {
        isDestroy = true;
        mErrorView = null;
        if (null != mDrawableListForRecycle) {
            for (Drawable d : mDrawableListForRecycle) {
                recyleDrawable(d);
            }
            mDrawableListForRecycle = null;
        }
        loadJavaScript("onAppExit()");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        log("onKeyDown begin, keyCode is " + keyCode);

        if (keyCode == KeyEvent.KEYCODE_BACK && isListenBackKeyEvent) {
            if (handleKeyBackEvent()) {
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 处理手机回退键事件
     *
     * @return
     */
    private boolean handleKeyBackEvent() {
        if (appHandleBackEvent) {
            loadJavaScript("onBack()");
            return true;
        }

        if (this.canGoBack()) {
            this.goBack();
            return true;
        }
        return false;
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        log("onFocusChanged begin, focused is " + focused
                + " ,isHandleFocusChange is " + isHandleFocusChange);

        // 4.1版本开始webview代码结构变化较大，onFocusChanged反射失败会导致一些手机出现异常。
        if (getOSVersionCode() >= 16) {
            return;
        }

        if (isHandleFocusChange && focused) {
            try {
                // 通过反射来修改mDefaultScale的值，屏蔽在某些手机webview中的文本框获取焦点后，页面自动放大的问题。
                Field defaultScale = WebView.class
                        .getDeclaredField("mDefaultScale");
                defaultScale.setAccessible(true);
                defaultScale.setFloat(this, 1.0f);
            } catch (SecurityException e) {
                log("onFocusChanged error", e);
            } catch (IllegalArgumentException e) {
                log("onFocusChanged error", e);
            } catch (IllegalAccessException e) {
                log("onFocusChanged error", e);
            } catch (NoSuchFieldException e) {
                try {
                    // 适配android4.0
                    Field zoomManager = WebView.class
                            .getDeclaredField("mZoomManager");
                    zoomManager.setAccessible(true);
                    Object zoomValue = zoomManager.get(this);
                    Field defaultScale = zoomManager.getType()
                            .getDeclaredField("mDefaultScale");
                    defaultScale.setAccessible(true);
                    defaultScale.setFloat(zoomValue, 1.0f);
                } catch (Exception e1) {
                    log("onFocusChanged error", e1);
                }
            }
        }
    }

    private int getOSVersionCode() {
        return Build.VERSION.SDK_INT;
    }

    /**
     * web辅助类
     */
    private class IFlyWebChromeClient extends WebChromeClient {
        @Override
        public void onReceivedTitle(WebView view, String title) {
            log("onReceivedTitle, title is " + title);
            if (browserCoreListener != null) {
                browserCoreListener.onReceivedTitle(view, title);
            }
            super.onReceivedTitle(view, title);
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
//            log("onProgressChanged, newProgress is " + newProgress + " ,url is " + view.getUrl());

            if (context != null && context instanceof Activity) {
                ((Activity) context).setProgress(newProgress * 100);
            }

            if (browserCoreListener != null) {
                browserCoreListener.onProgressChanged(view, newProgress);
            }
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message,
                                 final JsResult result) {
            log("onJsAlert, url is " + url + " ,message is "
                    + message);
            if (getWindowToken() == null || !getWindowToken().isBinderAlive()) {
                log("onJsAlert begin, but windowToken is not valid, so return");
                return false;
            }

            AlertDialog.Builder dlg = new AlertDialog.Builder(getContext());
            try {
                JSONArray array = new JSONArray(message);
                String title = array.getString(0);
                String content = array.getString(1);
                dlg.setTitle(title);
                dlg.setMessage(content);
            } catch (JSONException e) {
                log("onJsAlert error, so show a default dialog");
                dlg.setMessage(message);
                dlg.setTitle("Alert");
            }

            dlg.setCancelable(false);
            dlg.setPositiveButton(android.R.string.ok,
                    new AlertDialog.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            result.confirm();
                        }
                    });
            dlg.create();
            dlg.show();
            return true;
        }

        // 设置定位权限
        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, Callback callback) {
            if (getOSVersionCode() >= 7) {
                callback.invoke(origin, true, false);
                super.onGeolocationPermissionsShowPrompt(origin, callback);
            }
        }

        // 扩充缓存的容量
        @Override
        public void onReachedMaxAppCacheSize(long spaceNeeded, long totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater) {
            if (getOSVersionCode() >= 7) {
                quotaUpdater.updateQuota(spaceNeeded * 2);
            }
        }

        // 启用Web SQL数据库
        @Override
        public void onExceededDatabaseQuota(String url,
                                            String databaseIdentifier, long currentQuota,
                                            long estimatedSize, long totalUsedQuota,
                                            WebStorage.QuotaUpdater quotaUpdater) {
            if (getOSVersionCode() >= 7) {
                quotaUpdater.updateQuota(estimatedSize * 2);
            }
        }
    }

    /**
     * web辅助类
     */
    private class IFlyWebViewClient extends WebViewClient {
        /**
         * 页面加载开始时间
         */
        private long pageStartTime;

        // 跳转到其它页面会触发此回调 ，返回时不会触发。
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            log("shouldOverrideUrlLoading url is " + url);

            if (url != null && "tel:".equals(url.substring(0, 4))) {
                if (isSkipTelNumber) {
                    log("sip tel number");
                    return true;
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    try {
                        context.startActivity(intent);
                    } catch (Exception e) {
                        log("startSystemBrowser() uriString = " + url, e);
                    }
                    return true;
                }
            }


            // 通知监听器
            if (browserCoreListener != null && browserCoreListener.shouldOverrideUrlLoading(view, url)) {
                return true;
            } else {
                if (url.startsWith("http") || url.startsWith("https") || url.startsWith("file") || url.startsWith("about")) {
                    view.loadUrl(url);
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    try {
                        context.startActivity(intent);
                    } catch (Exception e) {
                        log("startActivity error, uriString = " + url, e);
                    }
                }
                return true;
            }
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            log("onLoadResource url is " + url);

            // 通知监听器
            if (browserCoreListener != null) {
                if(!browserCoreListener.onLoadResource(view, url)) {
                    return;
                }
            }

            super.onLoadResource(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            log("onPageStarted url is " + url + " ,old url is " + getUrl());
            currentUrl = url;
            pageStartTime = System.currentTimeMillis();
            super.onPageStarted(view, url, favicon);

            // 通知监听器
            if (browserCoreListener != null) {
                browserCoreListener.onPageStarted(view, url, favicon);
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            long durationTime = System.currentTimeMillis() - pageStartTime;
            log("onPageFinished, " + url + " load time is: " + durationTime);
            super.onPageFinished(view, url);

            // 通知监听器
            if (browserCoreListener != null) {
                browserCoreListener.onPageFinished(view, url);
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            stopLoading();
            super.onReceivedError(view, errorCode, description, failingUrl);

            log("onReceivedError, errorCode is " + errorCode
                    + " ,description is: " + description + " ,failingUrl is "
                    + failingUrl);

            // 通知监听器
            if (browserCoreListener != null && browserCoreListener.onReceivedError(view, errorCode, description, failingUrl)) {
                return;
            } else {
                // 显示异常界面
                showErrorView();

                // 出错后显示指定内容。不使用加载页面的方式，规避回退时引起的页面堆栈不对的问题
                InputStream inputStream;
                try {
                    inputStream = context.getResources().getAssets()
                            .open("errorpage/empty.htm");
                    InputStreamReader inputStreamReader = new InputStreamReader(
                            inputStream);
                    BufferedReader reader = new BufferedReader(
                            inputStreamReader);

                    final StringBuffer stringBuffer = new StringBuffer();
                    String m;
                    while ((m = reader.readLine()) != null) {
                        stringBuffer.append(m);
                    }
                    reader.close();

                    //loadJavaScript("document.write('" + CommonUtil.jsonCharFilter(stringBuffer.toString()) + "')");

                    loadDataWithBaseURL(failingUrl, stringBuffer.toString(),
                            "text/html", "utf-8", failingUrl);

                } catch (IOException e1) {
                    // 没有指定的出错页面，则使用系统默认行为
                    log("read errorpage faile", e1);
                }
            }
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            super.onReceivedSslError(view, handler, error);
            log("onReceivedSslError, url is " + view.getUrl());
            //处理证书过期或出错的网页加载
            handler.proceed();
//			// 通知监听器
//			if (browserCoreListener != null) {
//				browserCoreListener.onReceivedSslError(view, handler, error);
//			}
        }
    }

    private long preTouchTime = 0;
    private int firstDX = 0, firstDY = 0;
    private ViewConfiguration vcfg = ViewConfiguration.get(getContext());
    private int mConsideredDoubleSquare = vcfg.getScaledDoubleTapSlop() * vcfg.getScaledDoubleTapSlop();
    private int mMoveSquare = vcfg.getScaledTouchSlop() * vcfg.getScaledTouchSlop();

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            long currentTouchTime = System.currentTimeMillis();
            if (currentTouchTime - preTouchTime <= ViewConfiguration.getJumpTapTimeout()) {
                if (firstDX != 0 && firstDY != 0) {
                    int secX = (int) event.getX();
                    int secY = (int) event.getY();
                    int delX = secX - firstDX;
                    int delY = secY - firstDY;
//                    log("------------->> webview-action : delX=" + delX + ", delY = " + delY + ", ss = " + mConsideredDoubleSquare);
                    if ((delX * delX + delY * delY) < mConsideredDoubleSquare) {
                        preTouchTime = currentTouchTime;
                        firstDX = (int) event.getX();
                        firstDY = (int) event.getY();
                        log("------------->> webview-action : handle double-tap");

                        return true;
                    }
                }
            }
            firstDX = (int) event.getX();
            firstDY = (int) event.getY();
            preTouchTime = currentTouchTime;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (firstDX != 0 && firstDY != 0) {
                int secX = (int) event.getX();
                int secY = (int) event.getY();
                int delX = secX - firstDX;
                int delY = secY - firstDY;
                if ((delX * delX + delY * delY) >= mMoveSquare) {
                    firstDX = 0;
                    firstDY = 0;
                    log("------------->> webview-action : cancel double-tap");
                }
            }
        }

        try {
            return super.dispatchTouchEvent(event);
        } catch (Exception ex) {
            log("dispatchTouchEvent failed", ex);
            return false;
        }
    }

    /**
     * 浏览器下载监听
     */
    public class IFlyWebViewDownLoadListener implements DownloadListener {
        @Override
        public void onDownloadStart(String url, String userAgent,
                                    String contentDisposition, String mimetype, long contentLength) {
            log("onDownloadStart url is " + url + " ,mimetype is "
                    + mimetype + " ,contentLength is " + contentLength);
            if (browserCoreListener != null && browserCoreListener.onDownloadStart(url, userAgent, contentDisposition, mimetype, contentLength)) {
                return;
            } else {
                // 使用浏览器自带的下载器
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    context.startActivity(intent);
                } catch (Exception e) {
                    log("startActivity error", e);
                }
            }
        }
    }

    /**
     * 设置监听返回键的开关
     *
     * @param listenBackKeyEvent true  webview监听页面历史记录
     *                           false webview不监听页面历史记录，即不处理返回键
     */
    public void setListenBackKeyEvent(boolean listenBackKeyEvent) {
        this.isListenBackKeyEvent = listenBackKeyEvent;
    }

    /**
     * 设置解析电话号码的开关
     *
     * @param isParseTelNumber true 开启(遇到电话号码能进行自动拨号处理)
     *                         false 关闭
     */
    public void setSkipTelNumber(boolean isParseTelNumber) {
        this.isSkipTelNumber = isParseTelNumber;
    }

    /**
     * 刷新异常界面，防止异常界面 只显示一部分
     */
    private void refresh() {
        if (mErrorView != null && mErrorView.getVisibility() == View.VISIBLE) {
            showErrorView();
        }
    }

    /**
     * 显示异常界面
     */
    @SuppressWarnings("deprecation")
    private void showErrorView() {
        // 添加异常界面
        if (null == mErrorView) {
            mErrorView = createErrorView();
            addView(mErrorView);
            mErrorView.setVisibility(View.GONE);
        }
        LayoutParams param = (LayoutParams) mErrorView.getLayoutParams();
        param.width = getWidth();
        param.height = getHeight();
        mErrorView.setLayoutParams(param);
        mErrorView.setVisibility(View.VISIBLE);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed) {
            log("refresh webview");
            refresh();
        }
    }

    /**
     * 创建异常界面
     *
     * @return
     */
    private View createErrorView() {
        LinearLayout errorView = new LinearLayout(context);
        LinearLayout.LayoutParams errorViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.FILL_PARENT);
        errorViewParams.gravity = Gravity.CENTER;
        errorView.setLayoutParams(errorViewParams);
        errorView.setBackgroundColor(Color.WHITE);
        errorView.setOrientation(LinearLayout.VERTICAL);
        errorView.setGravity(Gravity.CENTER);

        LinearLayout viewPackage = new LinearLayout(context);
        LinearLayout.LayoutParams viewPackageParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        viewPackage.setLayoutParams(viewPackageParams);
        viewPackage.setOrientation(LinearLayout.VERTICAL);
        viewPackage.setGravity(Gravity.CENTER);
        errorView.addView(viewPackage);

        // 情感图片
        ImageView emotionImage = new ImageView(context);
        LinearLayout.LayoutParams emotionImageParams = new LinearLayout.LayoutParams(
                dip2px(149), dip2px(149));
        emotionImage.setLayoutParams(emotionImageParams);
        emotionImage
                .setImageDrawable(getDrawableFromAsset("emotion_error.png"));
        viewPackage.addView(emotionImage);
        // 错误提示
        TextView errorTip = new TextView(context);
        LinearLayout.LayoutParams errorTipParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        errorTipParams.gravity = Gravity.CENTER;
        errorTipParams.topMargin = dip2px(18);
        errorTip.setGravity(Gravity.CENTER_HORIZONTAL);
        errorTip.setLayoutParams(errorTipParams);
        errorTip.setTextSize(23);
        errorTip.setTextColor(Color.parseColor("#515151"));
        errorTip.setText("抱歉，打不开网页了");
        viewPackage.addView(errorTip);
        // 分割线
        LinearLayout errorLine = new LinearLayout(context);
        LinearLayout.LayoutParams errorLineParams = new LinearLayout.LayoutParams(
                dip2px(272), 1);
        errorLineParams.topMargin = dip2px(7);
        errorLine.setLayoutParams(errorLineParams);
        errorLine.setBackgroundColor(Color.parseColor("#C8C8C8"));
        viewPackage.addView(errorLine);
        // 异常信息：包含两条
        LinearLayout errorInfos = new LinearLayout(context);
        LinearLayout.LayoutParams errorInfosParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        errorInfosParams.topMargin = dip2px(15);
        errorInfos.setLayoutParams(errorInfosParams);
        errorInfos.setOrientation(LinearLayout.VERTICAL);
        viewPackage.addView(errorInfos);
        // 小圆点图片
        Drawable pointDrawable = getDrawableFromAsset("browser_circle.png");
        // 第一条异常
        LinearLayout infoAPackage = new LinearLayout(context);
        LinearLayout.LayoutParams infoAPackageParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        infoAPackageParams.rightMargin = dip2px(5);
        infoAPackage.setLayoutParams(infoAPackageParams);
        infoAPackage.setOrientation(LinearLayout.HORIZONTAL);
        infoAPackage.setGravity(Gravity.CENTER_VERTICAL);
        errorInfos.addView(infoAPackage);
        // 第一条按钮图片
        ImageView infoAImage = new ImageView(context);
        LinearLayout.LayoutParams infoAImageParams = new LinearLayout.LayoutParams(
                dip2px(7), dip2px(7));
        infoAImageParams.rightMargin = dip2px(5);
        infoAImage.setLayoutParams(infoAImageParams);
        infoAImage.setImageDrawable(pointDrawable);
        infoAPackage.addView(infoAImage);
        // 第一条异常信息
        TextView infoA = new TextView(context);
        infoA.setTextSize(14);
        infoA.setTextColor(Color.parseColor("#515151"));
        infoA.setText("暂时没有网络信号或数据连接");
        infoAPackage.addView(infoA);
        // 第二条异常
        LinearLayout infoBPackage = new LinearLayout(context);
        LinearLayout.LayoutParams infoBPackageParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        infoBPackageParams.rightMargin = dip2px(5);
        infoBPackage.setLayoutParams(infoBPackageParams);
        infoBPackage.setOrientation(LinearLayout.HORIZONTAL);
        infoBPackage.setGravity(Gravity.CENTER_VERTICAL);
        errorInfos.addView(infoBPackage);
        // 第二条按钮图片
        ImageView infoBImage = new ImageView(context);
        LinearLayout.LayoutParams infoBImageParams = new LinearLayout.LayoutParams(
                dip2px(7), dip2px(7));
        infoBImageParams.rightMargin = dip2px(5);
        infoBImage.setLayoutParams(infoBImageParams);
        infoBImage.setImageDrawable(pointDrawable);
        infoBPackage.addView(infoBImage);
        // 第二条异常信息
        TextView infoB = new TextView(context);
        infoB.setTextSize(14);
        infoB.setTextColor(Color.parseColor("#515151"));
        infoB.setText("此网页可能出现了故障或不存在");
        infoBPackage.addView(infoB);

        // 两个按钮
        LinearLayout btnPackage = new LinearLayout(context);
        LinearLayout.LayoutParams btnPackageParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        btnPackageParams.topMargin = dip2px(33);
        btnPackageParams.gravity = Gravity.CENTER;
        btnPackage.setLayoutParams(btnPackageParams);
        btnPackage.setOrientation(LinearLayout.HORIZONTAL);
        viewPackage.addView(btnPackage);
        // 第一个“刷新重试”
        LinearLayout refreshBtn = new LinearLayout(context);
        LinearLayout.LayoutParams refreshBtnParams = new LinearLayout.LayoutParams(
                dip2px(98), dip2px(33));
        refreshBtnParams.rightMargin = dip2px(25);
        refreshBtn.setLayoutParams(refreshBtnParams);
        refreshBtn.setOrientation(LinearLayout.HORIZONTAL);
        refreshBtn.setGravity(Gravity.CENTER);
        refreshBtn.setBackgroundDrawable(getStateListDrawable());
        btnPackage.addView(refreshBtn);
        // 按钮图片
        ImageView refreshImage = new ImageView(context);
        LinearLayout.LayoutParams refreshImageParams = new LinearLayout.LayoutParams(
                dip2px(15), dip2px(15));
        refreshImageParams.rightMargin = dip2px(5);
        refreshImage.setLayoutParams(refreshImageParams);
        refreshImage
                .setImageDrawable(getDrawableFromAsset("ico_refresh_tryagain.png"));
        refreshBtn.addView(refreshImage);
        // “刷新重试”
        TextView refreshText = new TextView(context);
        refreshText.setTextSize(14);
        refreshText.setTextColor(Color.parseColor("#515151"));
        refreshText.setText("刷新重试");
        refreshBtn.addView(refreshText);
        refreshBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastClickTime == -1) {
                    lastClickTime = System.currentTimeMillis();
                } else {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastClickTime <= 1000) {
                        log("You clicked too fast...");
                        return;
                    }
                    lastClickTime = currentTime;
                }
                reload();
            }
        });
        // 第二个“设置网络”
        LinearLayout setnetBtn = new LinearLayout(context);
        LinearLayout.LayoutParams setnetBtnParams = new LinearLayout.LayoutParams(
                dip2px(98), dip2px(33));
        setnetBtn.setLayoutParams(setnetBtnParams);
        setnetBtn.setOrientation(LinearLayout.HORIZONTAL);
        setnetBtn.setGravity(Gravity.CENTER);
        setnetBtn.setBackgroundDrawable(getStateListDrawable());
        btnPackage.addView(setnetBtn);
        // 按钮图片
        ImageView setnetImage = new ImageView(context);
        LinearLayout.LayoutParams setnetImageParams = new LinearLayout.LayoutParams(
                dip2px(15), dip2px(15));
        setnetImageParams.rightMargin = dip2px(5);
        setnetImage.setLayoutParams(setnetImageParams);
        setnetImage
                .setImageDrawable(getDrawableFromAsset("ico_install_network.png"));
        setnetBtn.addView(setnetImage);
        // “刷新重试”
        TextView setnetText = new TextView(context);
        setnetText.setTextSize(14);
        setnetText.setTextColor(Color.parseColor("#515151"));
        setnetText.setText("设置网络");
        setnetBtn.addView(setnetText);
        setnetBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = null;
                if (Build.VERSION.SDK_INT < 14) {
                    intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                } else {
                    intent = new Intent(Settings.ACTION_SETTINGS);
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                try {
                    context.startActivity(intent);
                } catch (Exception e) {
                    log("startNetSetting()", e);
                }
            }
        });

        return errorView;
    }

    private int dip2px(double dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5);
    }

    /**
     * 从asset中获取Drawable
     *
     * @param fileName
     * @return
     */
    private Drawable getDrawableFromAsset(String fileName) {
        AssetManager asset = context.getAssets();
        Drawable bd = null;
        try {
            InputStream in = asset.open(ASSET_IMAGE_PATH + fileName);
            bd = BitmapDrawable.createFromStream(in, null);
            if (null == mDrawableListForRecycle) {
                mDrawableListForRecycle = new ArrayList<Drawable>();
            }
            if (!mDrawableListForRecycle.contains(bd)) {
                mDrawableListForRecycle.add(bd);
            }
            in.close();
        } catch (IOException e) {
            log("获取资源图片失败：" + fileName, e);
        }

        return bd;
    }

    private StateListDrawable getStateListDrawable() {
        /**Tip:如果是.9.png图片，在asset里是使用的话是无法规则拉伸的，
         * 需要把apk中drawable对应的图片拿出来使用，这里的图片是已经编码处理的，
         * 可以asset里直接使用（asset里的.09.png无法进行指定的编码处理）。
         * */
        Drawable normal = getDrawableFromAsset("btn_refresh_tryagain_nor_bg.png");
        Drawable pressed = getDrawableFromAsset("btn_refresh_tryagain_press_bg.png");
        Drawable focused = pressed;
        Drawable disabled = normal;

        StateListDrawable statelist = new StateListDrawable();

        statelist.addState(View.PRESSED_ENABLED_STATE_SET, focused);
        statelist.addState(View.ENABLED_FOCUSED_STATE_SET, focused);
        statelist.addState(View.ENABLED_STATE_SET, normal);
        statelist.addState(View.FOCUSED_STATE_SET, focused);
        statelist.addState(View.EMPTY_STATE_SET, disabled);

        return statelist;
    }

    /**
     * 释放指定图片资源
     *
     * @param drawable
     */
    public void recyleDrawable(Drawable drawable) {
        try {
            if (null != drawable) {
                if (drawable instanceof BitmapDrawable) {
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                    bitmapDrawable.getBitmap().recycle();
                }
                drawable.setCallback(null);
            }
        } catch (Exception e) {
            log("", e);
        }
    }

    public String getCurrentUrl() {
        return currentUrl;
    }
}
