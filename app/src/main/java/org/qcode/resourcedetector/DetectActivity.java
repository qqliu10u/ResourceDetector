package org.qcode.resourcedetector;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.qcode.resourcedetector.base.utils.Logging;
import org.qcode.resourcedetector.base.utils.UIUtil;
import org.qcode.resourcedetector.base.utils.Utils;
import org.qcode.resourcedetector.detect.DetectActionHandler;
import org.qcode.resourcedetector.detect.IDetectEventListener;
import org.qcode.resourcedetector.urlparser.ShareUrlParser;
import org.qcode.resourcedetector.view.LoadingView;

public class DetectActivity extends BaseActivity {
    private static final String TAG = "DetectActivity";

    //当前加载进度的文本提示
    private TextView mTxtViewDetectProgress;

    //当前正在嗅探的转圈动画
    private LoadingView mLoadingView;

    private DetectActionHandler mDetectActionHandler;
    private String mUrl;
    private EditText mEdtTextUrlInput;
    private Button mBtnBeginDetect;
    private TextView mTxtViewTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logging.d(TAG, "onCreate()");

        setContentView(R.layout.activity_detect);
        initView(this);

        mDetectActionHandler = DetectActionHandler.getInstance();
        mDetectActionHandler.addObserver(mDetectEventListener);

        initIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Logging.d(TAG, "onNewIntent()");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Logging.d(TAG, "onRestart()| ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logging.d(TAG, "onResume()| ");
    }

    private void initView(Context context) {
        mTxtViewTitle = castViewById(R.id.title_text);
        mTxtViewTitle.setText("资源检测");

        mEdtTextUrlInput = castViewById(R.id.edttext_detect_url);
        mBtnBeginDetect = castViewById(R.id.btn_begin_detect);

        mTxtViewDetectProgress = castViewById(R.id.detect_progress_showing);
        mTxtViewDetectProgress.setMovementMethod(ScrollingMovementMethod.getInstance());

        mLoadingView = castViewById(R.id.detect_loading_view);

        mBtnBeginDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Utils.isEmpty(mUrl)) {
                    UIUtil.showToast(DetectActivity.this, "正在其他任务，不能开始探测");
                    return;
                }

                String url = mEdtTextUrlInput.getText().toString();
                if(Utils.isEmpty(url)) {
                    UIUtil.showToast(DetectActivity.this, "请输入探测地址");
                    return;
                }

                mTxtViewDetectProgress.setText("");
                mUrl = url;

                setInputEnabled(false);
                mDetectActionHandler.detectUrl(mUrl, null);
            }
        });
    }

    private void initIntent(Intent intent) {
        mUrl = ShareUrlParser.parseShareUrl(intent);
        if(Utils.isEmpty(mUrl)) {
            return;
        }

        setInputEnabled(false);
        mEdtTextUrlInput.setText(mUrl);
        mDetectActionHandler.detectUrl(mUrl, null);
    }

    private void addTextToView(String text) {
        String currText = mTxtViewDetectProgress.getText().toString();
        if(null == currText) {
            currText = "";
        }
        mTxtViewDetectProgress.setText(currText + "\n" + text);

        Layout layout = mTxtViewDetectProgress.getLayout();
        if(null != layout) {
            mTxtViewDetectProgress.scrollTo(mTxtViewDetectProgress.getScrollX(),
                    layout.getLineTop(0));
        }
    }

    private void setInputEnabled(boolean enabled) {
        mEdtTextUrlInput.setEnabled(enabled);
        mBtnBeginDetect.setEnabled(enabled);
    }

    private IDetectEventListener mDetectEventListener = new IDetectEventListener() {

        @Override
        public void onDetectProgressBegin(String rootUrl) {
            if(null == mUrl || !mUrl.equals(rootUrl)) {
                return;
            }

            mLoadingView.setVisibility(View.VISIBLE);
        }

        @Override
        public void onDetectUrlBegin(String rootUrl, String url) {
            if(null == mUrl || !mUrl.equals(rootUrl)) {
                return;
            }

            addTextToView("探测任务开始：" + url + "\n");
        }

        @Override
        public void onDetected(String rootUrl, String type, String url) {
            if(null == mUrl || !mUrl.equals(rootUrl)) {
                return;
            }

            String typeName = "资源";
            if("pic".equals(type)) {
                typeName = "图片";
            } else if("video".equals(type)) {
                typeName = "视频";
            }
            addTextToView(">>>>**探测到"+ typeName + ": " + url + "\n");
        }

        @Override
        public void onDetectUrlComplete(String rootUrl, String url) {
            if(null == mUrl || !mUrl.equals(rootUrl)) {
                return;
            }
            addTextToView("探测任务完成：" + url + "\n");
        }

        @Override
        public void onDetectProgressComplete(String rootUrl) {
            if(null == mUrl || !mUrl.equals(rootUrl)) {
                return;
            }

            Logging.d(TAG, "onDetectProgressComplete()| rootUrl= " + rootUrl);

            mLoadingView.setVisibility(View.GONE);
            UIUtil.showToast(DetectActivity.this, "资源探测完成");

            mUrl = null;
            setInputEnabled(true);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDetectActionHandler.removeObserver(mDetectEventListener);
        if(null != mUrl && mDetectActionHandler.isDetecting(mUrl)) {
            UIUtil.showToast(this, "切换到后台探测");
        }
    }
}
