package sample.withwings.updateversion.andpermission;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sample.withwings.updateversion.andpermission.listener.AndPermissionListener;

/**
 * AndPermission 工具类
 * 创建：WithWings 时间：2017/11/22.
 * Email:wangtong1175@sina.com
 */
public class PermissionUtils {

    public static final Integer DEFAULT_REQUEST_CODE = 123;

    private PermissionUtils() {

    }

    public static void checkPermission(final Context context, SettingDialogShow rationaleDialog, final AndPermissionListener andPermissionListener, String... permissions) {
        checkPermission(context, DEFAULT_REQUEST_CODE, rationaleDialog, andPermissionListener, permissions);
    }

    /**
     * 建议用 mustPermission 在部分手机上，用户第一次拒绝后就相当于勾选了不再提示
     * 所以除非是需求度不高的权限，否则还是建议还是用mustPermission
     *
     * @param context               上下文对象
     * @param requestCode           请求 code
     * @param rationaleDialog       用户拒绝后的提示框
     * @param andPermissionListener 权限授予情况的监听器
     * @param permissions           申请的权限表
     */
    public static void checkPermission(final Context context, int requestCode, SettingDialogShow rationaleDialog, final AndPermissionListener andPermissionListener, String... permissions) {
        // 敲黑板，Arrays.asList 返回的集合不可修改
        final List<String> requestList = Arrays.asList(permissions);
        AndPermission.with(context)
                .requestCode(requestCode)
                .permission(permissions)
                .rationale(rationaleDialog != null ? rationaleDialog.rationale() : null)
                .callback(new PermissionListener() {
                    @Override
                    public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
                        if (andPermissionListener != null) {
                            List<String> copyGrantPermissions = new ArrayList<>();
                            copyGrantPermissions.addAll(grantPermissions);
                            for (String copyGrantPermission : grantPermissions) {
                                if (!AndPermission.hasPermission(context, copyGrantPermission)) {
                                    copyGrantPermissions.remove(copyGrantPermission);
                                }
                            }
                            if (copyGrantPermissions.size() == requestList.size()) {
                                andPermissionListener.onAllSucceed(requestCode, copyGrantPermissions);
                                andPermissionListener.onSucceed(requestCode, copyGrantPermissions);
                            } else if (copyGrantPermissions.size() == 0) {
                                andPermissionListener.onAllFailed(requestCode, grantPermissions);
                                andPermissionListener.onFailed(requestCode, grantPermissions);
                            } else {
                                andPermissionListener.onSucceed(requestCode, copyGrantPermissions);

                                List<String> deniedPermissions = new ArrayList<>();
                                deniedPermissions.addAll(requestList);
                                // 部分成功
                                if (deniedPermissions.removeAll(copyGrantPermissions)) {
                                    andPermissionListener.onFailed(requestCode, deniedPermissions);
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                        if (andPermissionListener != null) {
                            // 移除某些通知没有获得，实际已经获得的权限
                            List<String> copyDeniedPermissions = new ArrayList<>();
                            copyDeniedPermissions.addAll(deniedPermissions);
                            for (String deniedPermission : deniedPermissions) {
                                if (AndPermission.hasPermission(context, deniedPermission)) {
                                    copyDeniedPermissions.remove(deniedPermission);
                                }
                            }

                            // 全部失败
                            if (copyDeniedPermissions.size() == requestList.size()) {
                                andPermissionListener.onAllFailed(requestCode, copyDeniedPermissions);
                                andPermissionListener.onFailed(requestCode, copyDeniedPermissions);
                            } else if (copyDeniedPermissions.size() == 0) {
                                andPermissionListener.onAllSucceed(requestCode, requestList);
                                andPermissionListener.onSucceed(requestCode, requestList);
                            } else {
                                // 部分失败
                                andPermissionListener.onFailed(requestCode, copyDeniedPermissions);
                                List<String> grantPermissions = new ArrayList<>();
                                grantPermissions.addAll(requestList);
                                // 部分成功
                                if (grantPermissions.removeAll(copyDeniedPermissions)) {
                                    andPermissionListener.onSucceed(requestCode, grantPermissions);
                                }
                            }
                        }
                    }
                })
                .start();
    }

    public static void mustPermission(final Activity activity, final SettingDialogShow rationaleDialog, final SettingDialogShow settingDialog, final AndPermissionListener andPermissionListener, String... permissions) {
        mustPermission(activity, DEFAULT_REQUEST_CODE, rationaleDialog, settingDialog, andPermissionListener, permissions);
    }


    /**
     * 用户勾选后仍然可以弹窗让用户去设置界面设置
     *
     * @param activity              跳转设置界面依赖的界面
     * @param requestCode           请求code
     * @param rationaleDialog       说明权限用处的弹窗
     * @param settingDialog         跳转设置的弹窗
     * @param andPermissionListener 权限授予情况的监听器
     * @param permissions           申请的权限表
     */
    public static void mustPermission(final Activity activity, int requestCode, final SettingDialogShow rationaleDialog, final SettingDialogShow settingDialog, final AndPermissionListener andPermissionListener, String... permissions) {
        // 敲黑板，Arrays.asList 返回的集合不可修改
        final List<String> requestList = Arrays.asList(permissions);
        AndPermission.with(activity)
                .requestCode(requestCode)
                .permission(permissions)
                .rationale(rationaleDialog != null ? rationaleDialog.rationale() : null)
                .callback(new PermissionListener() {
                    @Override
                    public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {

                        // 是否有不再提示并拒绝的权限。
                        if (settingDialog != null && AndPermission.hasAlwaysDeniedPermission(activity, grantPermissions) && !AndPermission.hasPermission(activity, grantPermissions)) {
                            settingDialog.show();
                        }

                        if (andPermissionListener != null) {
                            List<String> copyGrantPermissions = new ArrayList<>();
                            copyGrantPermissions.addAll(grantPermissions);
                            for (String copyGrantPermission : grantPermissions) {
                                if (!AndPermission.hasPermission(activity, copyGrantPermission)) {
                                    copyGrantPermissions.remove(copyGrantPermission);
                                }
                            }
                            if (copyGrantPermissions.size() == requestList.size()) {
                                andPermissionListener.onAllSucceed(requestCode, copyGrantPermissions);
                                andPermissionListener.onSucceed(requestCode, copyGrantPermissions);
                            } else if (copyGrantPermissions.size() == 0) {
                                andPermissionListener.onAllFailed(requestCode, grantPermissions);
                                andPermissionListener.onFailed(requestCode, grantPermissions);
                            } else {
                                andPermissionListener.onSucceed(requestCode, copyGrantPermissions);

                                List<String> deniedPermissions = new ArrayList<>();
                                deniedPermissions.addAll(requestList);
                                // 部分成功
                                if (deniedPermissions.removeAll(copyGrantPermissions)) {
                                    andPermissionListener.onFailed(requestCode, deniedPermissions);
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {

                        // 是否有不再提示并拒绝的权限。
                        if (settingDialog != null && AndPermission.hasAlwaysDeniedPermission(activity, deniedPermissions) && !AndPermission.hasPermission(activity,deniedPermissions)) {
                            settingDialog.show();
                        }

                        if (andPermissionListener != null) {
                            // 移除某些通知没有获得，实际已经获得的权限
                            List<String> copyDeniedPermissions = new ArrayList<>();
                            copyDeniedPermissions.addAll(deniedPermissions);
                            for (String deniedPermission : deniedPermissions) {
                                if (AndPermission.hasPermission(activity, deniedPermission)) {
                                    copyDeniedPermissions.remove(deniedPermission);
                                }
                            }

                            // 全部失败
                            if (copyDeniedPermissions.size() == requestList.size()) {
                                andPermissionListener.onAllFailed(requestCode, copyDeniedPermissions);
                                andPermissionListener.onFailed(requestCode, copyDeniedPermissions);
                            } else if (copyDeniedPermissions.size() == 0) {
                                andPermissionListener.onAllSucceed(requestCode, requestList);
                                andPermissionListener.onSucceed(requestCode, requestList);
                            } else {
                                // 部分失败
                                andPermissionListener.onFailed(requestCode, copyDeniedPermissions);
                                List<String> grantPermissions = new ArrayList<>();
                                grantPermissions.addAll(requestList);
                                // 部分成功
                                if (grantPermissions.removeAll(copyDeniedPermissions)) {
                                    andPermissionListener.onSucceed(requestCode, grantPermissions);
                                }
                            }
                        }
                    }
                })
                .start();
    }

}
