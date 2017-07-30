package org.qcode.resourcedetector.detect.tumblr;

import android.net.Uri;
import android.text.TextUtils;

import com.tumblr.jumblr.types.Photo;
import com.tumblr.jumblr.types.PhotoPost;
import com.tumblr.jumblr.types.PhotoSize;
import com.tumblr.jumblr.types.Post;
import com.tumblr.jumblr.types.Video;
import com.tumblr.jumblr.types.VideoPost;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.qcode.resourcedetector.base.TaskRunner;
import org.qcode.resourcedetector.base.utils.Logging;
import org.qcode.resourcedetector.base.utils.Utils;
import org.qcode.resourcedetector.common.DetectErrorCode;
import org.qcode.resourcedetector.detect.AbsDetectHandler;
import org.qcode.resourcedetector.detect.entities.ResourceType;

import java.util.List;

/***
 * 通过Tumblr接口获取资源
 * author: author
 * created at 2017/7/30
 */
public class TumblrDetectHandler extends AbsDetectHandler {

    private static final String TAG = "TumblrDetectHandler";

    private boolean mIsDetecting = false;

    @Override
    public boolean canDetect(String url) {
        final String blogName = extractBlogName(url);
        final long blogId = extractBlogId(url);
        return !Utils.isEmpty(blogName) && blogId > 0;
    }

    @Override
    public int detectUrl(final String url, final String parentUrl) {
        if (Utils.isEmpty(url)) {
            return DetectErrorCode.ILLEGAL_PARAM;
        }

        final String blogName = extractBlogName(url);
        final long blogId = extractBlogId(url);
        if (Utils.isEmpty(blogName) || blogId < 0) {
            return DetectErrorCode.URL_EXCLUDED;
        }

        mIsDetecting = true;
        TaskRunner.getUIHandler().post(new Runnable() {
            @Override
            public void run() {
                sendEvent("onDetectProgressBegin", url);
                sendEvent("onDetectUrlBegin", parentUrl, url);
            }
        });

        TaskRunner.getBackHandler().post(new Runnable() {
            @Override
            public void run() {
                Post post = TumblrClient.getClient().blogPost(blogName, blogId);

                if (post instanceof PhotoPost) {
                    detectPhotoPost(url, (PhotoPost) post);
                } else if (post instanceof VideoPost) {
                    detectVideoPost(url, (VideoPost) post);
                } else {
                    Logging.d(TAG, "run()| post not match, do nothing");
                }

                mIsDetecting = false;
                TaskRunner.getUIHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        sendEvent("onDetectUrlComplete", parentUrl, url);
                        sendEvent("onDetectProgressComplete", url);
                    }
                });
            }
        });

        return DetectErrorCode.OK;
    }

    private long extractBlogId(String url) {
//        try {
//            Pattern pattern = Pattern.compile("post/([0-9]*?)/");
//            Matcher matcher = pattern.matcher(url);
//            String blogId = matcher.group(1);
//            long id = Long.valueOf(blogId);
//            return id;
//        } catch (Exception ex) {
//            return -1;
//        }
        try {
            Uri uri = Uri.parse(url);
            String path = uri.getPath();
            String[] pathArray = path.split("/");
            for (String str : pathArray) {
                if (!Utils.isEmpty(str) && TextUtils.isDigitsOnly(str)) {
                    return Long.parseLong(str);
                }
            }
        } catch (Exception ex) {
        }
        return -1;
    }

    private String extractBlogName(String url) {
//        try {
//            Pattern pattern = Pattern.compile("://(.*?)\\.");
//            Matcher matcher = pattern.matcher(url);
//            String blogName = matcher.group(1);
//            return blogName;
//        } catch (Exception ex) {
//            return null;
//        }
        try {
            Uri uri = Uri.parse(url);
            String host = uri.getHost();
            return host.substring(0, host.indexOf("."));
        } catch (Exception ex) {
            return null;
        }
    }

    private void detectVideoPost(final String url, VideoPost post) {
        List<Video> videoList = post.getVideos();
        if (null == videoList) {
            return;
        }

        Video maxSizeVideo = null;
        for (Video video : videoList) {
            if (null == video) {
                continue;
            }

            if (null == maxSizeVideo) {
                maxSizeVideo = video;
                continue;
            }

            if (maxSizeVideo.getWidth() < video.getWidth()) {
                maxSizeVideo = video;
            }
        }

        if (null != maxSizeVideo) {
            final String videoUrl = extractUrl(maxSizeVideo.getEmbedCode());
            if (null != videoUrl) {
                TaskRunner.getUIHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        sendEvent("onDetected", url, ResourceType.VIDEO, videoUrl);
                    }
                });
            }
        }
    }

    private String extractUrl(String embedCode) {
        try {
            Document document = Jsoup.parse(embedCode);
            Elements elements = document.getElementsByTag("source");

            String url = null;
            for (Element element : elements) {
                url = element.attr("src");
                if (!Utils.isEmpty(url)) {
                    break;
                }
            }

            return url;
        } catch (Exception ex) {
            return null;
        }
    }

    private void detectPhotoPost(final String url, PhotoPost post) {
        List<Photo> list = post.getPhotos();
        if (null == list) {
            return;
        }

        for (Photo photo : list) {
            List<PhotoSize> photoSizeList = photo.getSizes();
            if (Utils.isEmpty(photoSizeList)) {
                continue;
            }

            PhotoSize maxSizePhoto = null;
            for (PhotoSize photoSize : photoSizeList) {
                if (null == photoSize) {
                    continue;
                }

                if (null == maxSizePhoto) {
                    maxSizePhoto = photoSize;
                    continue;
                }

                if (maxSizePhoto.getWidth() * maxSizePhoto.getHeight()
                        < photoSize.getWidth() * photoSize.getHeight()) {
                    maxSizePhoto = photoSize;
                }
            }

            if (null != maxSizePhoto) {
                final PhotoSize finalPhotoSize = maxSizePhoto;
                TaskRunner.getUIHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        sendEvent("onDetected", url, ResourceType.PIC, finalPhotoSize.getUrl());
                    }
                });
            }
        }
    }

    @Override
    public boolean isDetecting(String url) {
        return mIsDetecting;
    }
}