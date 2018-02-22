package sample.withwings.updateversion.dialog.listener;

import android.content.DialogInterface;
import android.view.View;

/**
 * 点击监听
 * 创建：WithWings 时间：2017/11/24.
 * Email:wangtong1175@sina.com
 */
public interface OnDialogClickListener {

    void onPositive(View view);

    void onNegative(View view);

    void onDismiss(DialogInterface dialog);

}
