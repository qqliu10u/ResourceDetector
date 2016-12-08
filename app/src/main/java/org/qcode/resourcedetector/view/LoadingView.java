package org.qcode.resourcedetector.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.qcode.resourcedetector.R;

/**
 * qqliu
 * 2016/8/29.
 */
public class LoadingView extends FrameLayout {
    private static final String TAG = "LoadingView";

    private View mLoadingPicView;
    private Animation mRotateAnimation;
    private View mLayoutLoading;
    private TextView mTxtViewLoadingResult;
    private TextView mTxtViewLoadingTip;

    public LoadingView(Context context) {
        this(context, null);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.loading_view, this);
        mLoadingPicView = findViewById(R.id.loading_view_pic);
        mLayoutLoading = findViewById(R.id.layout_loading_state);
        mTxtViewLoadingTip = (TextView) findViewById(R.id.loading_tip);
        mTxtViewLoadingResult = (TextView)findViewById(R.id.txtview_loading_result_tip);

        //begin animation
        mRotateAnimation = getRotateAnimation();
        mLoadingPicView.startAnimation(mRotateAnimation);
    }

    private Animation getRotateAnimation() {
        RotateAnimation animation = new RotateAnimation(
                0,
                360,
                Animation.RELATIVE_TO_SELF,
                0.5f, //中心点x
                Animation.RELATIVE_TO_SELF, //中心点y
                0.5f);
        animation.setDuration(800);
        animation.setRepeatCount(Animation.INFINITE);
        animation.setInterpolator(new LinearInterpolator());
        return animation;
    }

    public void showLoadingView() {
        mLayoutLoading.setVisibility(VISIBLE);
        mTxtViewLoadingResult.setVisibility(GONE);
        setOnClickListener(null);
    }

    public void setLoadingTip(String loadingTip) {
        mTxtViewLoadingTip.setText(loadingTip);
    }

    public void showTipView(String tip, OnClickListener tipClickListener) {
        mLayoutLoading.setVisibility(GONE);
        mTxtViewLoadingResult.setVisibility(VISIBLE);
        mTxtViewLoadingResult.setText(tip);
        setOnClickListener(tipClickListener);
    }
}
