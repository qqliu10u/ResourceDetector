package org.qcode.resourcedetector.jsdetector;

import android.content.Context;
import android.os.AsyncTask;

import org.qcode.resourcedetector.ResourceDetectorApp;
import org.qcode.resourcedetector.base.LockWaitNotifyHelper;
import org.qcode.resourcedetector.base.utils.FileUtils;
import org.qcode.resourcedetector.base.utils.Utils;
import org.qcode.resourcedetector.common.ResourceDetectorConstant;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import static org.qcode.resourcedetector.base.utils.FileUtils.readTextFile;

/**
 * author
 * 2016/11/28.
 */

public class JSResourceDetector {

    private static final String TAG = "JSResourceDetector";

    private static volatile JSResourceDetector mInstance;

    private Context mContext;

    private volatile String mDetectorFrameWorkJs;

    private LockWaitNotifyHelper mResInitNotifyHelper;

    private JSResourceDetector() {
        mContext = ResourceDetectorApp.getAppContext();
        mResInitNotifyHelper = new LockWaitNotifyHelper(null);
    }

    public static JSResourceDetector getInstance() {
        if (null == mInstance) {
            synchronized (JSResourceDetector.class) {
                if (null == mInstance) {
                    mInstance = new JSResourceDetector();
                }
            }
        }
        return mInstance;
    }

    public void init() {
        mResInitNotifyHelper.beginLockAction();

        //开启线程完成注入代码初始化
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                //根目录
                String targetFileDirectory = FileUtils.getDecodeCodeDirectory();

                //step1 拷贝Asset文件
                ArrayList<String> srcFileNameList = new ArrayList<String>();
                srcFileNameList.add(ResourceDetectorConstant.FRAME_RESOURCES_DETECTOR_JS_PATH);
                srcFileNameList.add(ResourceDetectorConstant.USER_TUMBLR_COM_DETECTOR_JS_PATH);
                srcFileNameList.add(ResourceDetectorConstant.VIDEO_VINE_CO_DETECTOR_JS_PATH);
                srcFileNameList.add(ResourceDetectorConstant.WWW_TUMBLR_COM_JS_PATH);

                for (String srcFileName : srcFileNameList) {
                    String filePath = targetFileDirectory + File.separator + srcFileName;
                    if (ResourceDetectorConstant.NEED_COVER_ASSET_FILE
                            || !FileUtils.isExist(filePath)) {
                        FileUtils.copyAssetFile(mContext, srcFileName, targetFileDirectory);
                    }
                }

                //step2 读取框架js
                String srcFilePath = ResourceDetectorConstant.FRAME_RESOURCES_DETECTOR_JS_PATH;
                String filePath = targetFileDirectory + File.separator + srcFilePath;
                String detectorFrameWorkJs = readTextFile(filePath);

                if (Utils.isEmpty(detectorFrameWorkJs)) {
                    return null;
                }

                //step3 读取所有检测的js
                String allDetectCode = getAllDetectCode(targetFileDirectory);

                //step4 将所有的检测资源js添加到框架内
                detectorFrameWorkJs = detectorFrameWorkJs.replace(
                        ResourceDetectorConstant.JS_FRAMEWORK_SPACE, allDetectCode);

                //step5 去掉所有的注释
                detectorFrameWorkJs = detectorFrameWorkJs.replaceAll("[^:]//[\\s\\S]*?\\n", "\n");

                //step6 加上javascript前缀
                detectorFrameWorkJs = ResourceDetectorConstant.JS_PREFIX + detectorFrameWorkJs;

                return detectorFrameWorkJs;
            }

            @Override
            protected void onPostExecute(String jsResult) {
                //注入代码初始化成功
                mDetectorFrameWorkJs = jsResult;

                //通知等待着事件完成
                mResInitNotifyHelper.signalWaiter();
            }

        }.execute();
    }

    public String getDetectJavascript() {
        if(null == mDetectorFrameWorkJs) {
            //初始化未完成则等待
            mResInitNotifyHelper.waitForSignal();
        }

        return mDetectorFrameWorkJs;
    }

    private String getAllDetectCode(String directoryPath) {
        File directory = new File(directoryPath);
        String detectFileNameArray[] = directory.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                if (ResourceDetectorConstant.FRAME_RESOURCES_DETECTOR_JS_PATH.equals(filename)) {
                    return false;
                }

                return true;
            }
        });

        if (null == detectFileNameArray) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (String detectFileName : detectFileNameArray) {
            String jsCode = FileUtils.readTextFile(
                    directory.getAbsolutePath() + File.separator + detectFileName);
            sb.append(jsCode).append("\n");
        }

        return sb.toString();
    }
}
