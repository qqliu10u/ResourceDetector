package org.qcode.resourcedetector.download;

import org.qcode.resourcedetector.base.utils.FileUtils;
import org.qcode.resourcedetector.base.utils.Logging;
import org.qcode.resourcedetector.base.utils.Utils;
import org.qcode.resourcedetector.download.entities.DownloadTask;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * author
 * 2016/11/30.
 */

public class DownloadController implements IDownloadController {
    private static final String TAG = "DownloadController";

    private static volatile DownloadController mInstance;

    private DownloadController() {}

    public static DownloadController getInstance() {
        if(null == mInstance) {
            synchronized (DownloadController.class) {
                if(null == mInstance) {
                    mInstance = new DownloadController();
                }
            }
        }
        return mInstance;
    }

    @Override
    public boolean startDownload(DownloadTask task) {
        final String type = task.getType();
        final String directoryName = task.getDirectory();
        final String title = task.getTitle();
        final String downloadUrl = task.getDownloadUrl();

        FileUtils.assumeDirectoryExist(FileUtils.getExternalDirectory() + directoryName);
        final String fileName = !Utils.isEmpty(title) ? title : FileUtils.getFileName(type, downloadUrl);
        new Thread() {
            @Override
            public void run() {
                super.run();

                URL httpUrl = null;
                HttpURLConnection connection = null;
                InputStream inputStream = null;
                BufferedOutputStream bos = null;
                try {
                    httpUrl = new URL(downloadUrl);
                    connection = (HttpURLConnection) httpUrl.openConnection();
                    connection.setDoInput(true);
                    connection.setConnectTimeout(20 * 1000);
                    connection.setReadTimeout(10 * 1000);

                    inputStream = connection.getInputStream();
                    bos = new BufferedOutputStream(
                            new FileOutputStream(
                                    new File(FileUtils.getExternalDirectory()
                                            + directoryName
                                            + File.separator
                                            + fileName)));
                    byte[] tmpArray = new byte[256];
                    int length;
                    while ((length = inputStream.read(tmpArray)) != -1) {
                        bos.write(tmpArray, 0, length);
                    }

                    bos.flush();
                } catch (Exception ex) {
                    Logging.d(TAG, "run()| error happened", ex);
                } finally {
                    if (null != connection) {
                        connection.disconnect();
                    }

                    if (null != inputStream) {
                        try {
                            inputStream.close();
                        } catch (IOException ex) {
                            Logging.d(TAG, "run()| error happened", ex);
                        }
                    }

                    if (null != bos) {
                        try {
                            bos.close();
                        } catch (IOException ex) {
                            Logging.d(TAG, "run()| error happened", ex);
                        }
                    }
                }
            }
        }.start();
//        };
        return true;
    }
}
