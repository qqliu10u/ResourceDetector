package org.qcode.resourcedetector.download.entities;

/**
 * qqliu
 * 2016/11/30.
 */

public class DownloadTask {

    private String type;
    private String downloadUrl;
    private String title;
    private String fileDir;

    private DownloadTask() {

    }

    public static DownloadTask create() {
        return new DownloadTask();
    }

    public String getType() {
        return type;
    }

    public DownloadTask setType(String type) {
        this.type = type;
        return this;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public DownloadTask setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public DownloadTask setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDirectory() {
        return fileDir;
    }

    public DownloadTask setDirectory(String fileDir) {
        this.fileDir = fileDir;
        return this;
    }
}
