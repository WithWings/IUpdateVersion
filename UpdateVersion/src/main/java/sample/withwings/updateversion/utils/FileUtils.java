package sample.withwings.updateversion.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static android.app.Activity.RESULT_OK;

/**
 * 文件工具类
 * 创建：WithWings 时间 2018/2/9
 * Email:wangtong1175@sina.com
 */
@SuppressWarnings({"unused", "WeakerAccess", "ConstantConditions", "SameParameterValue", "SuspiciousNameCombination", "UnusedReturnValue"})
public class FileUtils {

    public final String TAG = "LAZY";

    public final static String FILE_EXTENSION_SEPARATOR = ".";

    /** URI类型：file */
    public static final String URI_TYPE_FILE = "file";


    private FileUtils() {
        throw new AssertionError();
    }

    /**
     * 获得SD 卡路径，如果没有则获取内存卡路径
     * @return 路径
     */
    public static String getSDCardPath() {
        return getSDCardPath(false);
    }

    /**
     * 获取sd卡路径
     * 请注意：如果你使用外置sd卡，那么你只能有读取的权限，想要写文件，必须单独申请权限
     * 我在下面一个方法封装了权限请求的方式，但是因为要处理权限回调，这里只做跳转到申请界面，回调处理请自行操作
     * @param isMust 如果必须是外置sd卡
     * @return 外置为空
     */
    public static String getSDCardPath(boolean isMust){
        String SDPath = System.getenv("SECONDARY_STORAGE");
        if (TextUtils.isEmpty(SDPath) && !isMust) {
            SDPath = getPhonePath();
        }
        return SDPath;
    }

    public static void requestSDCardPermission(Activity activity, int requestCode) {
        activity.startActivityForResult(new Intent("android.intent.action.OPEN_DOCUMENT_TREE"),requestCode);
    }

    /**
     * 返回值
     * @param resultCode 返回的请求码 判断requestCode 符合调用该方法处理
     * @return 获得的sd uri，如果为空则代表未获得
     */
    public static DocumentFile checkSDCardPermission(Context context, int resultCode, Intent data, String filePath){
        if (resultCode == RESULT_OK) {
            Uri treeUri = data.getData();
            if(treeUri == null){
                return null;
            }
            if (!":".equals(treeUri.getPath().substring(treeUri.getPath().length() - 1)) || treeUri.getPath().contains("primary")) {
                // 选择的内部存储卡
                return null;
            } else {
                return getDocumentFilePath(context, Uri.parse(treeUri.toString()), filePath, true);
            }
        }
        return null;
    }

    /**
     * 获得可操作的文件
     * @param context 上下文
     * @param path 文件路径 file.getAbsolutePath()
     * @param createDirectories 新建还是删除文件操作
     * @return File对象
     */
    public static DocumentFile getDocumentFilePath(Context context, Uri uri, String path, boolean createDirectories) {
        DocumentFile document = DocumentFile.fromTreeUri(context, uri);

        String[] parts = path.split("/");
        for (int i = 3; i < parts.length; i++) {
            DocumentFile nextDocument = document.findFile(parts[i]);
            if (nextDocument == null) {
                if (i < parts.length - 1) {
                    if (createDirectories) {
                        nextDocument = document.createDirectory(parts[i]);
                    } else {
                        return null;
                    }
                } else {
                    nextDocument = document.createFile("image", parts[i]);
                }
            }
            document = nextDocument;
        }
        return document;
    }

    /**
     * 获得写入流
     * @param context 上下文
     * @param documentFile DocumentFile
     * @return 写入流
     */
    public static OutputStream documentFileToOutputStream(Context context, DocumentFile documentFile){
        documentFile.canWrite();
        try {
            return context.getContentResolver().openOutputStream(documentFile.getUri());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获得读取流
     * @param context 上下文
     * @param documentFile DocumentFile
     * @return 读取流
     */
    public static InputStream documentFileToInputStream(Context context, DocumentFile documentFile){
        documentFile.canWrite();
        try {
            return context.getContentResolver().openInputStream(documentFile.getUri());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获得路径 “/storage/emulated/legacy” 之所以不是 “/storage/emulated/0” 是因为 0 是新的格式，但是存储位置还是软链接到 legacy
     * @return 路径
     */
    public static String getPhonePath(){
        return Environment.getExternalStorageDirectory().getPath();
    }

    /**
     * 创建文件夹
     * @param file 文件对象
     * @return 是否是文件夹/如果没有会自动创建
     */
    public static boolean makeDir(File file) {
        if (file.exists() && file.isDirectory()) {
            return true;
        } else {
            return file.mkdirs();
        }
    }

    /**
     * 创建文件
     * @param file 创建文件
     * @return 创建结果
     */
    public static boolean makeFile(File file){
        if(file.exists() && file.isFile()) {
            return true;
        } else {
            try {
                return file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}