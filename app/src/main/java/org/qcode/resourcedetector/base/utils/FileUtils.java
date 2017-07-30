package org.qcode.resourcedetector.base.utils;

import android.content.Context;
import android.os.Environment;

import org.qcode.resourcedetector.common.ResourceDetectorConstant;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import static android.content.ContentValues.TAG;

/**
 * author
 * 2016/11/30.
 */

public class FileUtils {

    public static String getExternalDirectory() {
        return Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator
                + ResourceDetectorConstant.WORK_DIR
                + File.separator;
    }

    public static String getDecodeCodeDirectory() {
        return getExternalDirectory()
                + File.separator
                + ResourceDetectorConstant.ADDITIONAL_DETECT_CODE
                + File.separator;
    }

    public static void assumeDirectoryExist(String directory) {
        if(Utils.isEmpty(directory)) {
            return;
        }
        File file = new File(directory);
        file.mkdirs();
    }


    public static String getFileName(String type, String downloadUrl) {
        if(null == downloadUrl) {
            return "";
        }

        int index = downloadUrl.lastIndexOf("/");

        String suffix = getSuffix(type);
        if(index != -1) {
            return String.valueOf(Math.abs(downloadUrl.hashCode()) + suffix);
        } else {
            String fileName = downloadUrl.substring(index, downloadUrl.length());
            if(fileName.indexOf(".") != -1) {
                return fileName;
            } else {
                return fileName + suffix;
            }
        }


    }

    public static String getSuffix(String type) {
        if("video".equals(type)) {
            return ".mp4";
        } else if("pic".equals(type)) {
            return ".jpg";
        }

        return "";
    }

//    public static String readTextFile(FileReader fr) {
//        if(null == fr) {
//            return null;
//        }
//
//        BufferedReader br = null;
//        StringBuilder resultCache = new StringBuilder();
//        try {
//            br = new BufferedReader(fr);
//
//            String lineStr;
//            while ((lineStr = br.readLine()) != null) {
//                resultCache.append(lineStr);
//            }
//        } catch (Exception ex) {
//            Logging.d(TAG, "readTextFile()| error happened", ex);
//        } finally {
//            if(null != br) {
//                try {
//                    br.close();
//                } catch (Exception ex) {
//                    Logging.d(TAG, "readTextFile()| error happened", ex);
//                }
//            }
//        }
//
//        return resultCache.toString();
//    }

    public static String readTextFile(InputStream is) {
        if(null == is) {
            return null;
        }

        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();

            byte[] tmpData = new byte[256];

            int len;
            while ((len = is.read(tmpData)) != -1) {
                baos.write(tmpData, 0, len);
            }

            byte[] array = baos.toByteArray();
            return Utils.parseString(array);

        } catch (Exception ex) {
            Logging.d(TAG, "readTextFile()| error happened", ex);
        } finally {
            if(null != baos) {
                try {
                    baos.close();
                } catch (Exception ex) {
                    Logging.d(TAG, "readTextFile()| error happened", ex);
                }
            }
        }

        return null;
    }

    public static String readTextFile(String filePath) {
        if(Utils.isEmpty(filePath)) {
            return null;
        }

        File file = new File(filePath);
        if(!file.exists()) {
            return null;
        }

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            return readTextFile(fis);
        } catch (Exception ex) {
           Logging.d(TAG, "readTextFile()| error happened", ex);
        } finally {
            if(null != fis) {
                try {
                    fis.close();
                } catch (Exception ex) {
                    Logging.d(TAG, "readTextFile()| error happened", ex);
                }
            }
        }

        return null;
    }

//    /***
//     * 不建议使用，因为asset内的文件基本上都是压缩的
//     * @param context
//     * @param filePath
//     * @return
//     */
//    public static String readTextFileFromAsset(Context context, String filePath) {
//        if(null == context || Utils.isEmpty(filePath)) {
//            return null;
//        }
//
//        AssetFileDescriptor assetFileDescriptor = null;
//        FileReader fr = null;
//        try {
//            assetFileDescriptor = context.getAssets().openFd(filePath);
//            fr = new FileReader(assetFileDescriptor.getFileDescriptor());
//            return readTextFile(fr);
//        } catch (Exception ex) {
//            Logging.d(TAG, "readTextFile()| error happened", ex);
//        } finally {
//            if(null != assetFileDescriptor) {
//                try {
//                    assetFileDescriptor.close();
//                } catch (Exception ex) {
//                    Logging.d(TAG, "readTextFileFromAsset()| error happened", ex);
//                }
//            }
//            if(null != fr) {
//                try {
//                    fr.close();
//                } catch (Exception ex) {
//                    Logging.d(TAG, "readTextFile()| error happened", ex);
//                }
//            }
//        }
//
//        return null;
//    }

    public static boolean isExist(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    public static boolean copyAssetFile(
            Context context, String srcfilePath, String destFileDirectory) {
        if(null == context || Utils.isEmpty(srcfilePath) || Utils.isEmpty(destFileDirectory)) {
            return false;
        }

        int index = srcfilePath.lastIndexOf(File.separator);
        String srcDirectory;
        if(index == -1) {
            srcDirectory = "";
        } else {
            srcDirectory = srcfilePath.substring(0, index);
        }

        File directory = new File(destFileDirectory + File.separator + srcDirectory);
        if(!directory.exists()) {
            directory.mkdirs();
        }

        InputStream is = null;
        FileOutputStream fos = null;
        try {
            is = context.getAssets().open(srcfilePath);
            fos = new FileOutputStream(new File(destFileDirectory + File.separator + srcfilePath));
            byte[] tmpData = new byte[256];
            int len;
            while ((len = is.read(tmpData)) != -1) {
                fos.write(tmpData, 0 ,len);
            }
            fos.flush();

            return true;
        } catch (Exception ex) {
            Logging.d(TAG, "copyAssetFile()| error happened", ex);
        } finally {
            if(null != is) {
                try {
                    is.close();
                } catch (Exception ex) {
                    Logging.d(TAG, "copyAssetFile()| error happened", ex);
                }
            }
            if(null != fos) {
                try {
                    fos.close();
                } catch (Exception ex) {
                    Logging.d(TAG, "copyAssetFile()| error happened", ex);
                }
            }
        }

        return false;
    }
}
