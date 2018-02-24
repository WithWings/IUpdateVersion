package sample.withwings.updateversion.utils;

import android.app.NotificationManager;
import android.support.v4.app.NotificationCompat;

/**
 * TODO
 * 创建：WithWings 时间 2018/2/24
 * Email:wangtong1175@sina.com
 */
public class ProgressUtil {

    private NotificationManager mNotificationManager;

    private NotificationCompat.Builder mBuilderProgress;

    public ProgressUtil(NotificationManager notificationManager, NotificationCompat.Builder builderProgress) {
        mNotificationManager = notificationManager;
        mBuilderProgress = builderProgress;
    }

    public void update(int num) {
        mBuilderProgress.setProgress(100, num, false);
        mNotificationManager.notify(2, mBuilderProgress.build());
    }

    public void dismiss() {
        mNotificationManager.cancel(2);
    }
}
