package org.qcode.resourcedetector.detect.entities;

import org.qcode.resourcedetector.base.utils.Utils;

import java.util.ArrayList;

/***
 * author: author
 * created at 2017/7/30
 */
public class DetectingItem {
    String mUrl;
    boolean isDetected = false;
    ArrayList<DetectingItem> mSubItems = new ArrayList<DetectingItem>(5);

    public DetectingItem(String url) {
        this.mUrl = url;
    }

    public String getUrl() {
        return mUrl;
    }

    public boolean isDetected() {
        return isDetected;
    }

    public void setItemDetected(String url) {
        if (Utils.isEmpty(url)) {
            return;
        }

        if (isDetected) {
            return;
        }

        if (url.equals(this.mUrl)) {
            //只有子元素为空时才能置detected为true
            if (Utils.isEmpty(mSubItems)) {
                this.isDetected = true;
            } else {
                //do nothing
            }
        } else {
            for (int i = 0; i < mSubItems.size(); i++) {
                DetectingItem subItem = mSubItems.get(i);
                subItem.setItemDetected(url);
            }
            checkDetected();
        }
    }

    public void addSubDetectingItem(String url, String parentUrl) {
        if (Utils.isEmpty(url) || Utils.isEmpty(parentUrl)) {
            return;
        }

        if (parentUrl.equals(mUrl)) {
            mSubItems.add(new DetectingItem(url));
        } else {
            for (int i = 0; i < mSubItems.size(); i++) {
                DetectingItem subItem = mSubItems.get(i);
                subItem.addSubDetectingItem(url, parentUrl);
            }
        }
    }

    private void checkDetected() {
        //所有子元素都已经check完成，则重新计算当前元素的check值
        if (mSubItems.size() > 0) {
            for (int i = 0; i < mSubItems.size(); i++) {
                if (!mSubItems.get(i).isDetected) {
                    return;
                }
            }

            isDetected = true;
        } else {
            //没有子元素，则等待外部通知
        }
    }

    public boolean contains(String url) {
        if (Utils.isEmpty(url)) {
            return false;
        }

        if (url.equals(mUrl)) {
            return true;
        }

        for (DetectingItem item : mSubItems) {
            if (item.contains(url)) {
                return true;
            }
        }

        return false;
    }
}
