package sample.withwings.updateversion;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.yanzhenjie.permission.Permission;

import java.io.File;
import java.util.List;

import sample.withwings.updateversion.andpermission.PermissionUtils;
import sample.withwings.updateversion.andpermission.SettingDialogUtils;
import sample.withwings.updateversion.andpermission.listener.AndPermissionListener;
import sample.withwings.updateversion.dialog.BaseDialog;
import sample.withwings.updateversion.dialog.ControlDialog;
import sample.withwings.updateversion.dialog.NoticeDialog;
import sample.withwings.updateversion.dialog.listener.OnDialogClickListener;
import sample.withwings.updateversion.down.DownLoadingListener;
import sample.withwings.updateversion.down.DownloadRunnable;
import sample.withwings.updateversion.down.info.TaskInfo;
import sample.withwings.updateversion.utils.FileUtils;
import sample.withwings.updateversion.utils.ProgressUtil;
import sample.withwings.updateversion.utils.StatusBarUtils;
import sample.withwings.updateversion.utils.ToastUtils;

/**
 * 下载APP更新版本弹框
 * 创建：WithWings 时间 2018/2/22
 * Email:wangtong1175@sina.com
 */
@SuppressWarnings({"WeakerAccess", "StaticFieldLeak", "SameParameterValue"})
public class UpdateVersion {

    private static final String SP_KEY = "UpdateVersion";

    private static final String SP_SHOULD = "shouldUpdate";

    private static final String SP_VERSION = "shouldCode";

    private static final String SP_URL = "downUrl";

    private static final int REQUEST_FOR_INSTALL = 102;

    private static Activity mActivity;

    private static BaseDialog mBaseDialog;

    private static TaskInfo mInfo;
    private static DownloadRunnable mRunnable;

    private static ProgressUtil mProgressUtil;

    private UpdateVersion() {
    }

    /**
     * 检查是否有强制升级需求，防止用户关闭APP，断网重启APP
     *
     * @param activity 界面
     * @return true 代表有强制升级，并且已经自动弹出升级提示框 false 代表并没有强制升级的需求，可以自己检查一下版本是否正确
     */
    public static boolean checkLocal(Activity activity) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(SP_KEY, Context.MODE_PRIVATE);
        boolean spShould = sharedPreferences.getBoolean(SP_SHOULD, false);
        int version = sharedPreferences.getInt(SP_VERSION, 0);
        String url = sharedPreferences.getString(SP_URL, "");
        // 同时判断 APK 存在URL / 需要强制更新 / 且版本号还是当初保存的或更低的版本（防止用户在应用市场自行更新走进死循环）
        if (!TextUtils.isEmpty(url) && spShould && version <= getVersionCode(activity)) {
            downApk(activity, url, true);
            return true;
        }
        return false;
    }

    /**
     * 提示用户有新版本更新
     *
     * @param activity 界面
     * @param url      APK下载路径
     */
    public static void downApk(Activity activity, String url) {
        downApk(activity, url, false);
    }

    /**
     * 提示用户有新版本更新
     *
     * @param activity 界面
     * @param url      APK下载路径
     * @param must     是否强制更新
     */
    public static void downApk(Activity activity, String url, boolean must) {
        mActivity = activity;
        if (must) {
            mBaseDialog = NoticeDialog.create(activity);
            SharedPreferences sharedPreferences = activity.getSharedPreferences(SP_KEY, Context.MODE_PRIVATE);
            sharedPreferences.edit().putBoolean(SP_SHOULD, true).apply();
            sharedPreferences.edit().putInt(SP_VERSION, getVersionCode(activity)).apply();
            sharedPreferences.edit().putString(SP_URL, url).apply();
        } else {
            mBaseDialog = ControlDialog.create(activity);
        }

        mBaseDialog.setOnDialogClickListener(new MyOnDialogClickListener());

        //实例化任务信息对象
        mInfo = new TaskInfo(FileUtils.getSDCardPath() + "/SinaFQ/Download", url);

        mBaseDialog.show();
    }

    //用于更新进度的Handler
    private static Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            //使用Handler制造一个200毫秒为周期的循环
            handler.sendEmptyMessageDelayed(1, 200);
            //计算下载进度
            int l = (int) ((float) mInfo.getCompletedLen() / (float) mInfo.getContentLen() * 100);
            //设置进度条进度
            if(mProgressUtil != null) {
                mBaseDialog.setProgress(l);
                mProgressUtil.update(l);
            } else {
                handler.removeCallbacksAndMessages(null);
            }
            if (l >= 100) {//当进度>=100时，取消Handler循环
                handler.removeCallbacksAndMessages(null);
                mProgressUtil.dismiss();
                mProgressUtil = null;


                Intent intent = new Intent(Intent.ACTION_VIEW);

                //判断是否是AndroidN以及更高的版本
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Uri contentUri = FileProvider.getUriForFile(mActivity, BuildConfig.APPLICATION_ID + ".fileProvider", new File(mInfo.getPath(), mInfo.getName()));
                    intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
                } else {
                    intent.setDataAndType(Uri.fromFile(new File(mInfo.getPath(), mInfo.getName())), "application/vnd.android.package-archive");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                // 开启B同时还要索要结果：用户可能取消安装等
                mActivity.startActivityForResult(intent, REQUEST_FOR_INSTALL);
            }
            return true;
        }
    });

    static class MyOnDialogClickListener implements OnDialogClickListener {

        @Override
        public void onPositive(View view) {
            TextView textView = (TextView) view;
            switch (textView.getText().toString().trim()) {
                case "升级":
                    startDown();
                    break;
                case "暂停":
                    //调用DownloadRunnable中的stop方法，停止下载
                    mRunnable.stop();
                    mRunnable = null;//强迫症，不用的对象手动置空
                    mBaseDialog.setPositive("继续");
                    break;
                case "继续":
                    startDown();
                    mBaseDialog.setPositive("暂停");
                    break;
            }

        }

        @Override
        public void onNegative(View view) {

        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            if (mRunnable == null && mProgressUtil != null) {
                mProgressUtil.dismiss();
                mProgressUtil = null;
            }
            mBaseDialog.setProgressVisibility(View.GONE);
        }
    }

    private static void startDown() {

        PermissionUtils.checkPermission(mActivity, 101, SettingDialogUtils.getRationaleDialog(mActivity, ""), new AndPermissionListener() {

            @Override
            public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {

            }

            @Override
            public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                mBaseDialog.show();
            }

            @Override
            public void onAllSucceed(int requestCode, @NonNull List<String> grantPermissions) {

                mProgressUtil = StatusBarUtils.showNotificationProgress(mActivity);


                mBaseDialog.setProgressMax(100);
                mBaseDialog.setProgressVisibility(View.VISIBLE);
                mBaseDialog.setPositive("暂停");

                //创建下载任务
                mRunnable = new DownloadRunnable(mInfo, new DownLoadingListener() {
                    @Override
                    public void netWorkError() {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //调用DownloadRunnable中的stop方法，停止下载
                                mRunnable.stop();
                                mRunnable = null;//强迫症，不用的对象手动置空
                                mBaseDialog.setPositive("继续");
                                ToastUtils.show(mActivity, "网络连接中断，请检查网络后重试！");
                            }
                        });
                    }
                });
                //开始下载任务
                new Thread(mRunnable).start();
                //开始Handler循环
                handler.sendEmptyMessageDelayed(1, 200);

            }

            @Override
            public void onAllFailed(int requestCode, @NonNull List<String> deniedPermissions) {

            }
        }, Permission.STORAGE);
    }

    private static int getVersionCode(Context context) {
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
