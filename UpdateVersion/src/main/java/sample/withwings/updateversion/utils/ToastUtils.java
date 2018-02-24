package sample.withwings.updateversion.utils;

import android.app.Activity;
import android.widget.Toast;

/**
 * TODO
 * 创建：WithWings 时间 2018/2/24
 * Email:wangtong1175@sina.com
 */
public class ToastUtils {

    private static Toast mToast;

    private ToastUtils(){

    }

    public synchronized static void show(Activity activity, String text) {
        if(mToast == null) {
            mToast = Toast.makeText(activity, text, Toast.LENGTH_LONG);
        } else {
            mToast.setText(text);
        }
        mToast.show();
    }

}
