package org.qcode.resourcedetector.detect.interfaces;

/***
 * author: author
 * created at 2017/7/30
 */
public interface IDetectHandler {

    /***
     * url是否可以被解析
     * @param url
     * @return
     */
    boolean canDetect(String url);

    /**
     * 探测url
     * @param url 待探测的url
     * @param parentUrl 待探测url的来源
     * @return
     */
    int detectUrl(String url, String parentUrl);

    /***
     * url是否正在探测
     * @param url
     * @return
     */
    boolean isDetecting(String url);
}
