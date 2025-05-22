package tw.com.chainsea.android.common.version;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * current by evan on 2020-11-09
 *
 * @author Evan Wang
 * date 2020-11-09
 */
public class VersionHelper {

    /**
     * get App versionCode
     */
    public static String getVersionCode(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return String.valueOf(packageInfo.versionCode);
        } catch (PackageManager.NameNotFoundException ignored) {
            return "0";
        }
    }

    /**
     * get App versionName
     */
    public static String getVersionName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException ignored) {
            return "";
        }
    }

    public static boolean isDayuOldVersion(Context context, String oldVersion) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo;
        String versionName = "";
        try {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return versionName.compareTo(oldVersion) > 0;
    }

}
