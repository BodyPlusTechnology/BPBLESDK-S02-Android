package cc.bodyplus.sdk.ble.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by shihu.wang on 2017/3/21.
 * Email shihu.wang@bodyplus.cc
 */
public class FileUtils {

    /**
     * The constant SIZETYPE_B.
     */
    public static final int SIZETYPE_B = 1;//获取文件大小单位为B的double值
    /**
     * The constant SIZETYPE_KB.
     */
    public static final int SIZETYPE_KB = 2;//获取文件大小单位为KB的double值
    /**
     * The constant SIZETYPE_MB.
     */
    public static final int SIZETYPE_MB = 3;//获取文件大小单位为MB的double值
    /**
     * The constant SIZETYPE_GB.
     */
    public static final int SIZETYPE_GB = 4;//获取文件大小单位为GB的double值

    /**
     * 获取文件指定文件的指定单位的大小
     *
     * @param filePath 文件路径
     * @param sizeType 获取大小的类型1为B、2为KB、3为MB、4为GB
     * @return double值的大小 file or files size
     */
    public static double getFileOrFilesSize(String filePath, int sizeType) {
        File file = new File(filePath);
        long blockSize = 0;
        try {
            if (file.isDirectory()) {
                blockSize = getFileSizes(file);
            } else {
                blockSize = getFileSize(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return FormetFileSize(blockSize, sizeType);
    }

    /**
     * 调用此方法自动计算指定文件或指定文件夹的大小
     *
     * @param filePath 文件路径
     * @return 计算好的带B 、KB、MB、GB的字符串
     */
    public static String getAutoFileOrFilesSize(String filePath) {
        File file = new File(filePath);
        long blockSize = 0;
        try {
            if (file.isDirectory()) {
                blockSize = getFileSizes(file);
            } else {
                blockSize = getFileSize(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return FormetFileSize(blockSize);
    }


    /***
     * 递归获取指定目录下的所有的文件（不包括文件夹）
     *
     * @param dirPath the dir path
     * @return all files
     */
    public static ArrayList<File> getAllFiles(String dirPath) {
        File dir = new File(dirPath);

        ArrayList<File> files = new ArrayList<File>();

        if (dir.isDirectory()) {
            File[] fileArr = dir.listFiles();
            for (int i = 0; i < fileArr.length; i++) {
                File f = fileArr[i];
                if (f.isFile()) {
                    files.add(f);
                } else {
                    files.addAll(getAllFiles(f.getPath()));
                }
            }
        }
        return files;
    }


    /**
     * 删除文件或文件夹(包括里面所有的内容)
     *
     * @param filePath 文件绝对路径
     * @return boolean
     */
    public static boolean deleteFile(String filePath) {
        boolean bl = false;
        File file = new File(filePath);
        if (!file.exists()) {// 如果文件不存在，表示删除成功
            return true;
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) {
                return false;
            }
            int size = files.length;
            if (size == 0) {// 如果文件夹为空文件夹，则可直接删除
                return file.delete();
            }
            for (int i = 0; i < size; i++) {
                bl = deleteFile(files[i].getAbsolutePath());
            }
            bl = file.delete();// 删除完文件夹里面的子文件后，将该文件夹也删除
        } else if (file.isFile()) {
            bl = file.delete();
        }
        return bl;
    }


    /**
     * 读取文件内容
     *
     * @param fileName 待读取的完整文件名
     * @return 文件内容 string
     * @throws IOException the io exception
     */
    public static String read(String fileName) throws IOException {
        File f = new File(fileName);
        FileInputStream fs = new FileInputStream(f);
        String result = null;
        byte[] b = new byte[fs.available()];
        fs.read(b);
        fs.close();
        result = new String(b);
        return result;
    }

    /**
     * 写文件
     *
     * @param fileName    目标文件名
     * @param fileContent 写入的内容
     * @return boolean
     * @throws IOException the io exception
     */
    public static boolean write(String fileName, String fileContent) throws IOException {
        boolean result = false;
        File f = new File(fileName);
        FileOutputStream fs = new FileOutputStream(f);
        byte[] b = fileContent.getBytes();
        fs.write(b);
        fs.flush();
        fs.close();
        result = true;
        return result;
    }


    /**
     * 获取指定文件大小
     *
     * @param file
     * @return
     * @throws Exception
     */
    private static long getFileSize(File file) throws Exception {
        long size = 0;
        if (file.exists()) {
            FileInputStream fis = null;
            fis = new FileInputStream(file);
            size = fis.available();
            fis.close();
        } else {
            file.createNewFile();
        }
        return size;
    }

    /**
     * 转换文件大小
     *
     * @param fileS
     * @return
     */
    private static String FormetFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (fileS == 0) {
            return wrongSize;
        }
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }


    /**
     * 获取指定文件夹
     *
     * @param f
     * @return
     * @throws Exception
     */
    private static long getFileSizes(File f) throws Exception {
        long size = 0;
        File flist[] = f.listFiles();
        for (int i = 0; i < flist.length; i++) {
            if (flist[i].isDirectory()) {
                size = size + getFileSizes(flist[i]);
            } else {
                size = size + getFileSize(flist[i]);
            }
        }
        return size;
    }

    /**
     * 转换文件大小,指定转换的类型
     *
     * @param fileS
     * @param sizeType
     * @return
     */
    private static double FormetFileSize(long fileS, int sizeType) {
        DecimalFormat df = new DecimalFormat("#.00");
        double fileSizeLong = 0;
        switch (sizeType) {
            case SIZETYPE_B:
                fileSizeLong = Double.valueOf(df.format((double) fileS));
                break;
            case SIZETYPE_KB:
                fileSizeLong = Double.valueOf(df.format((double) fileS / 1024));
                break;
            case SIZETYPE_MB:
                fileSizeLong = Double.valueOf(df.format((double) fileS / 1048576));
                break;
            case SIZETYPE_GB:
                fileSizeLong = Double.valueOf(df.format((double) fileS / 1073741824));
                break;
            default:
                break;
        }
        return fileSizeLong;
    }
}
