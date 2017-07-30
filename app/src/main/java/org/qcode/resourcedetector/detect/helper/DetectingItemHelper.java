package org.qcode.resourcedetector.detect.helper;

import org.qcode.resourcedetector.base.utils.Utils;
import org.qcode.resourcedetector.detect.entities.DetectingItem;

import java.util.ArrayList;

/**
 * author
 * 2016/12/4.
 */

public class DetectingItemHelper {

    private ArrayList<DetectingItem> mRootDetectingItemList
            = new ArrayList<DetectingItem>();

    public void addNewDetectingItem(String url, String parentUrl) {
        if (Utils.isEmpty(url)) {
            return;
        }

        if (Utils.isEmpty(parentUrl)) {
            mRootDetectingItemList.add(new DetectingItem(url));
        } else {
            for (DetectingItem item : mRootDetectingItemList) {
                item.addSubDetectingItem(url, parentUrl);
            }
        }
    }

    public void setItemDetected(String url) {
        for (DetectingItem item : mRootDetectingItemList) {
            item.setItemDetected(url);
        }
    }

    public String getRootUrl(String url) {
        for (DetectingItem item : mRootDetectingItemList) {
            if(item.contains(url)) {
                return item.getUrl();
            }
        }

        return null;
    }

    public ArrayList<String> popDetectedRootItems() {
        ArrayList<DetectingItem> itemList = new ArrayList<DetectingItem>();
        for(DetectingItem item : mRootDetectingItemList) {
            if(item.isDetected()) {
                itemList.add(item);
            }
        }

        mRootDetectingItemList.removeAll(itemList);

        ArrayList<String> tmpList = new ArrayList<String>();
        for(DetectingItem item : itemList) {
            tmpList.add(item.getUrl());
        }

        return tmpList;
    }

    public boolean contains(String url) {
        for (DetectingItem item : mRootDetectingItemList) {
            if(item.contains(url)) {
                return true;
            }
        }

        return false;
    }
}
