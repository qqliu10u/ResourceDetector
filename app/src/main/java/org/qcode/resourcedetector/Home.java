package org.qcode.resourcedetector;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import org.qcode.resourcedetector.base.utils.Logging;

/**
 * qqliu
 * 2016/12/5.
 */

public class Home extends BaseActivity {
    private static final String TAG = "Home";
    private TextView mBtnTitleRight;
    private TextView mDescTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logging.d(TAG, "onCreate()| ");
        setContentView(R.layout.activity_home);

        initView(this);
    }

    private void initView(Context context) {
        mBtnTitleRight = castViewById(R.id.title_right_btn);
        mDescTextView = castViewById(R.id.app_desc);

        View leftTitlePart = castViewById(R.id.title_right_frame_layout);
        mBtnTitleRight.setVisibility(View.VISIBLE);
        mBtnTitleRight.setText("探测资源");
        leftTitlePart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, DetectActivity.class);
                startActivity(intent);
            }
        });

        mDescTextView.setText(Html.fromHtml("<html>" +
                "        <body>" +
                "        <strong>简介：</strong><p />" +
                "        &nbsp&nbsp&nbsp&nbsp&nbsp 资源嗅探器是一个对网页页面内视频和图片进行探测的一个小应用，纯属作者闲暇练手之作，目前适配了Tumblr的分享页面。<p />" +
                "        <strong>郑重声明：</strong><p />" +
                "        <font color=\"#FF0000\">&nbsp&nbsp&nbsp&nbsp&nbsp 1. 任何使用者不能使用此软件进行盗版或可能引起版权问题的活动。任何使用者不得利用此应用从事任何不符合国内或所在地区法律的行为。</font><p />" +
                "        <font color=\"#FF0000\">&nbsp&nbsp&nbsp&nbsp&nbsp 2.对已适配的Tumblr分享页面内涉及版权的资源，不应使用此应用嗅探或下载，更不应进行传播。</font><p /><p /><p />" +
                "        <strong>使用方法：</strong><p />" +
                "        &nbsp&nbsp&nbsp&nbsp&nbsp 资源嗅探器的设计目标是对网页内的图片和视频进行嗅探并自动下载。<p />" +
                "        &nbsp&nbsp&nbsp&nbsp&nbsp 理论上支持任何网站的资源嗅探，不过使用者需要在/sdcard/ResourceDetector/detect_code/下放入指定网站的检测代码。<p/>" +
                "        &nbsp&nbsp&nbsp&nbsp&nbsp 检测代码写法见***。<p/>" +
                "        &nbsp&nbsp&nbsp&nbsp&nbsp 应用目前适配了Tumblr分享页面的资源下载，使用者可以在看到感兴趣的图片/视频时条目时点击条目下的分享按钮，进入此应用进行资源嗅探。<p/>" +
                "        &nbsp&nbsp&nbsp&nbsp&nbsp 应用会自动识别传入的分享url，并逐层分析页面嵌入的资源地址。识别出的资源会下载到/sdcard/ResourceDetector/下，资源会按userName/Id/**进行归类。" +
                "        </body></html>"));
    }
}
