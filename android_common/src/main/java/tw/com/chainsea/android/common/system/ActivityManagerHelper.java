package tw.com.chainsea.android.common.system;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

/**
 * current by evan on 2020-08-06
 *
 * @author Evan Wang
 * @date 2020-08-06
 */
public class ActivityManagerHelper {
    private static final String TAG = ActivityManagerHelper.class.getSimpleName();


    /**
     * APP Is it in the foreground
     *
     * @param context
     */
    public static boolean isInTheForeground(Context context) {
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        int importance = 300;
        List<ActivityManager.RunningAppProcessInfo> list = mActivityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : list) {
            if (importance != 100) {
                importance = info.importance;
            }
        }
        return importance == 100;
    }
}
