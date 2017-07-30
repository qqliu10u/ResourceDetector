package org.qcode.resourcedetector.download;

import org.qcode.resourcedetector.download.entities.DownloadTask;

/**
 * author
 * 2016/11/30.
 */

public interface IDownloadController {

    boolean startDownload(DownloadTask downloadTask);
}
