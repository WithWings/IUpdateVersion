package sample.withwings.updateversion;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.view.View;

import sample.withwings.updateversion.dialog.BaseDialog;
import sample.withwings.updateversion.dialog.ControlDialog;
import sample.withwings.updateversion.dialog.NoticeDialog;
import sample.withwings.updateversion.dialog.listener.OnDialogClickListener;

/**
 * 下载APP更新版本弹框
 * 创建：WithWings 时间 2018/2/22
 * Email:wangtong1175@sina.com
 */
public class UpdateVersion {

    private static final String SP_KEY = "UpdateVersion";

    private static final String SP_SHOULD = "shouldUpdate";

    private static final String SP_VERSION = "shouldCode";

    private static final String SP_URL = "downUrl";

    private UpdateVersion() {
    }

    /**
     * 检查是否有强制升级需求，防止用户关闭APP，断网重启APP
     * @param activity 界面
     * @return true 代表有强制升级，并且已经自动弹出升级提示框 false 代表并没有强制升级的需求，完全可以
     */
    public static boolean checkLocal(Activity activity) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(SP_KEY, Context.MODE_PRIVATE);
        boolean spShould = sharedPreferences.getBoolean(SP_SHOULD, false);
        int version = sharedPreferences.getInt(SP_VERSION, 0);
        String url = sharedPreferences.getString(SP_URL, "");
        // 同时判断 APK 存在URL / 需要强制更新 / 且版本号还是当初保存的或更低的版本（防止用户在应用市场自行更新走进死循环）
        if(!TextUtils.isEmpty(url) && spShould && version <= getVersionCode(activity)) {
            downApk(activity, url, true);
            return true;
        }
        return false;
    }

    public static void downApk(Activity activity, String url) {
        downApk(activity, url, false);
    }

    public static void downApk(Activity activity, String url, boolean must) {
        BaseDialog baseDialog;
        if(must) {
            baseDialog = NoticeDialog.create(activity);
            SharedPreferences sharedPreferences = activity.getSharedPreferences(SP_KEY, Context.MODE_PRIVATE);
            sharedPreferences.edit().putBoolean(SP_SHOULD, true).apply();
            sharedPreferences.edit().putInt(SP_VERSION, getVersionCode(activity)).apply();
            sharedPreferences.edit().putString(SP_URL, url).apply();
        } else {
            baseDialog = ControlDialog.create(activity);
        }

        baseDialog.setOnDialogClickListener(new MyOnDialogClickListener());

        baseDialog.show();
    }

    static class MyOnDialogClickListener implements OnDialogClickListener {

        @Override
        public void onPositive(View view) {
            // TODO 下载
        }

        @Override
        public void onNegative(View view) {

        }

        @Override
        public void onDismiss(DialogInterface dialog) {

        }
    }

    private static int getVersionCode(Context context){
        int version;
        try {
            version = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.COMPONENT_ENABLED_STATE_DEFAULT).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // 因为取得是自身的版本号，不会出现包名找不到的情况，所以这里写死 0
            version = 0;
        }
        return version;
    }
}
