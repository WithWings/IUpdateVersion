package sample.withwings.updateversion.utils;

import android.os.Environment;
import android.text.TextUtils;

/**
 * 文件工具类
 * 创建：WithWings 时间 2018/2/9
 * Email:wangtong1175@sina.com
 */
@SuppressWarnings({"unused", "WeakerAccess", "ConstantConditions", "SameParameterValue", "SuspiciousNameCombination", "UnusedReturnValue"})
public class FileUtils {

    public final String TAG = "LAZY";

    public final static String FILE_EXTENSION_SEPARATOR = ".";

    /**
     * URI类型：file
     */
    public static final String URI_TYPE_FILE = "file";


    private FileUtils() {
        throw new AssertionError();
    }

    /**
     * 获得SD 卡路径，如果没有则获取内存卡路径
     *
     * @return 路径
     */
    public static String getSDCardPath() {
        return getSDCardPath(false);
    }

    /**
     * 获取sd卡路径
     * 请注意：如果你使用外置sd卡，那么你只能有读取的权限，想要写文件，必须单独申请权限
     * 我在下面一个方法封装了权限请求的方式，但是因为要处理权限回调，这里只做跳转到申请界面，回调处理请自行操作
     *
     * @param isMust 如果必须是外置sd卡
     * @return 外置为空
     */
    public static String getSDCardPath(boolean isMust) {
        String SDPath = System.getenv("SECONDARY_STORAGE");
        if (TextUtils.isEmpty(SDPath) && !isMust) {
            SDPath = getPhonePath();
        }
        return SDPath;
    }

    /**
     * 获得路径 “/storage/emulated/legacy” 之所以不是 “/storage/emulated/0” 是因为 0 是新的格式，但是存储位置还是软链接到 legacy
     *
     * @return 路径
     */
    public static String getPhonePath() {
        return Environment.getExternalStorageDirectory().getPath();
    }
}