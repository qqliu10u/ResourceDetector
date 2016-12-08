package org.qcode.resourcedetector.detect;

/**
 * qqliu
 * 2016/12/4.
 */

public interface IDetectEventListener {
    void onDetectProgressBegin(String rootUrl);
    void onDetectUrlBegin(String rootUrl, String url);
    void onDetected(String rootUrl, String type, String url);
    void onDetectUrlComplete(String rootUrl, String url);
    void onDetectProgressComplete(String rootUrl);
}
