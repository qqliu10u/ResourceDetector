package org.qcode.resourcedetector.detect.tumblr;

import com.tumblr.jumblr.JumblrClient;

/***
 * author: author
 * created at 2017/7/30
 */
public class TumblrClient {
    private static JumblrClient mClient;

    public static JumblrClient getClient() {
        if (null == mClient) {
            // Create a client
            mClient = new JumblrClient(
                    "去api.tumblr.com申请",
                    "去api.tumblr.com申请"
            );

            // Give it a token
            mClient.setToken(
                    "去api.tumblr.com申请",
                    "去api.tumblr.com申请"
            );
        }

        return mClient;
    }
}
