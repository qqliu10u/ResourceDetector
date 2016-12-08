package org.qcode.resourcedetector.detect;

import org.qcode.resourcedetector.base.utils.Utils;

import java.util.ArrayList;

/**
 * qqliu
 * 2016/12/4.
 */

class DetectingItemHelper {

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
                return item.mUrl;
            }
        }

        return null;
    }

    public ArrayList<String> popDetectedRootItems() {
        ArrayList<DetectingItem> itemList = new ArrayList<DetectingItem>();
        for(DetectingItem item : mRootDetectingItemList) {
            if(item.isDetected) {
                itemList.add(item);
            }
        }

        mRootDetectingItemList.removeAll(itemList);

        ArrayList<String> tmpList = new ArrayList<String>();
        for(DetectingItem item : itemList) {
            tmpList.add(item.mUrl);
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

    private static class DetectingItem {
        String mUrl;
        boolean isDetected = false;
        ArrayList<DetectingItem> mSubItems = new ArrayList<DetectingItem>(5);

        public DetectingItem(String url) {
            this.mUrl = url;
        }

        public void setItemDetected(String url) {
            if (Utils.isEmpty(url)) {
                return;
            }

            if(isDetected) {
                return;
            }

            if (url.equals(this.mUrl)) {
                //只有子元素为空时才能置detected为true
                if(Utils.isEmpty(mSubItems)) {
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
            if(mSubItems.size() > 0) {
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
            if(Utils.isEmpty(url)) {
                return false;
            }

            if(url.equals(mUrl)) {
                return true;
            }

            for (DetectingItem item : mSubItems) {
                if(item.contains(url)) {
                    return true;
                }
            }

            return false;
        }
    }
}
