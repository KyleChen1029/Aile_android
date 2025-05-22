package tw.com.chainsea.android.common.system;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AsyncQueryHandler;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import tw.com.chainsea.android.common.R;

/**
 * current by evan on 2019-10-22
 */
public class BadgeHelper {
    private static final String TAG = BadgeHelper.class.getSimpleName();
    private static final String NOTITY_TAG = "BRAND";

    public static boolean setCount(final int count, final Context context, Class<?> cls, int smallIcon) {

        if (count >= 0 && context != null) {
            switch (Build.BRAND.toLowerCase()) {
                case "xiaomi":
                        return true;
                case "honor":
                    return setHuaweiBadge(count, context);
                case "oppo":
                    return setOPPOBadge(count, context) || setOPPOBadge2(count, context);
                case "vivo":
                    return setVivoBadge(count, context);
                case "lenovo":
                    return setZukBadge(count, context);
                case "htc":
                    return setHTCBadge(count, context);
                case "sony":
                    return setSonyBadge(count, context);
                case "samsung":
                default:
                    return setNotificationBadge(count, context, cls, smallIcon);
            }
        } else {
            Log.e(TAG, "set up Badge failure");
            return false;
        }
    }

    public static boolean setNotificationBadge(int count, Context context, Class<?> cls, int smallIcon) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            return false;
        }
        // 8.0之后添加角标需要NotificationChannel
        NotificationChannel channel = new NotificationChannel("badge", context.getString(R.string.text_notification_badge), NotificationManager.IMPORTANCE_LOW);
        channel.setShowBadge(true);
        channel.enableLights(false);
        channel.enableVibration(false);
        channel.setVibrationPattern(new long[]{0});
        channel.setSound(null, null);
        notificationManager.createNotificationChannel(channel);
        Intent intent = new Intent(context, cls);
        PendingIntent pendingIntent;
        pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        Notification builder = new NotificationCompat.Builder(context, "badge")
            .setContentTitle("")
            .setContentText("您有" + count + "條未讀消息")
//                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
            .setSmallIcon(smallIcon)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setChannelId("badge")
            .setNumber(count)
            .setSound(null)
            .setDefaults(Notification.DEFAULT_ALL)
//                .setDefaults(NotificationCompat.FLAG_ONLY_ALERT_ONCE)
            .setPriority(Notification.PRIORITY_LOW)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            .build();
        // 小米
        if (Build.BRAND.equalsIgnoreCase("xiaomi")) {
            setXiaomiBadge(count, builder);
        }
        builder.sound = null;
        notificationManager.notify(NOTITY_TAG, 0, builder);
        if (count == 0) {
            notificationManager.cancel(NOTITY_TAG, 0);
        }
        return true;
    }


    private static void setXiaomiBadge(int count, Notification notification) {
        try {
            Field field = notification.getClass().getDeclaredField("extraNotification");
            Object extraNotification = field.get(notification);
            Method method = extraNotification.getClass().getDeclaredMethod("setMessageCount", int.class);
            method.invoke(extraNotification, count);
        } catch (Exception ignored) {
            Log.e(TAG, "set up Xiaomi Badge failure");
        }
    }

    private static boolean setHuaweiBadge(int count, Context context) {
        try {
            String launchClassName = getLauncherClassName(context);
            if (TextUtils.isEmpty(launchClassName)) {
                return false;
            }
            Bundle bundle = new Bundle();
            bundle.putString("package", context.getPackageName());
            bundle.putString("class", launchClassName);
            bundle.putInt("badgenumber", count);
            context.getContentResolver().call(Uri.parse("content://com.huawei.android.launcher.settings/badge/"), "change_badge", null, bundle);
            return true;
        } catch (Exception ignored) {
            Log.e(TAG, "set up Huawei Badge failure");
            return false;
        }
    }

    private static boolean setSamsungBadge(int count, Context context) {
        try {
            String launcherClassName = getLauncherClassName(context);
            if (TextUtils.isEmpty(launcherClassName)) {
                return false;
            }
            Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
            intent.putExtra("badge_count", count);
            intent.putExtra("badge_count_package_name", context.getPackageName());
            intent.putExtra("badge_count_class_name", launcherClassName);
            context.sendBroadcast(intent);
            return true;
        } catch (Exception ignored) {
            Log.e(TAG, "set up Samsung Badge failure");
            return false;
        }
    }

    @Deprecated
    private static boolean setOPPOBadge(int count, Context context) {
        try {
            Bundle extras = new Bundle();
            extras.putInt("app_badge_count", count);
            context.getContentResolver().call(Uri.parse("content://com.android.badge/badge"), "setAppBadgeCount", String.valueOf(count), extras);
            return true;
        } catch (Exception ignored) {
            Log.e(TAG, "set up OPPO 1 Badge failure");
            return false;
        }
    }


    @Deprecated
    private static boolean setOPPOBadge2(int count, Context context) {
        try {
            Intent intent = new Intent("com.oppo.unsettledevent");
            intent.putExtra("packageName", context.getPackageName());
            intent.putExtra("number", count);
            intent.putExtra("upgradeNumber", count);
            PackageManager packageManager = context.getPackageManager();
            List<ResolveInfo> receivers = packageManager.queryBroadcastReceivers(intent, 0);
            if (receivers != null && !receivers.isEmpty()) {
                context.sendBroadcast(intent);
            } else {
                Bundle extras = new Bundle();
                extras.putInt("app_badge_count", count);
                context.getContentResolver().call(Uri.parse("content://com.android.badge/badge"), "setAppBadgeCount", null, extras);
            }
            return true;
        } catch (Exception ignored) {
            Log.e(TAG, "set up OPPO 2 Badge failure");
            return false;
        }
    }

    @Deprecated
    private static boolean setVivoBadge(int count, Context context) {
        try {
            String launcherClassName = getLauncherClassName(context);
            if (TextUtils.isEmpty(launcherClassName)) {
                return false;
            }
            Intent intent = new Intent("launcher.action.CHANGE_APPLICATION_NOTIFICATION_NUM");
            intent.putExtra("packageName", context.getPackageName());
            intent.putExtra("className", launcherClassName);
            intent.putExtra("notificationNum", count);
            context.sendBroadcast(intent);
            return true;
        } catch (Exception ignored) {
            Log.e(TAG, "set up Vivo Badge failure");
            return false;
        }
    }

    private static boolean setZukBadge(int count, Context context) {
        try {
            Bundle extra = new Bundle();
            ArrayList<String> ids = new ArrayList<>();
            extra.putStringArrayList("app_shortcut_custom_id", ids);
            extra.putInt("app_badge_count", count);
            Uri contentUri = Uri.parse("content://com.android.badge/badge");
            Bundle bundle = context.getContentResolver().call(contentUri, "setAppBadgeCount", null, extra);
            return bundle != null;
        } catch (Exception ignored) {
            Log.e(TAG, "set up Zuk Badge failure");
            return false;
        }
    }

    private static boolean setHTCBadge(int count, Context context) {
        try {
            ComponentName launcherComponentName = getLauncherComponentName(context);
            if (launcherComponentName == null) {
                return false;
            }

            Intent intent1 = new Intent("com.htc.launcher.action.SET_NOTIFICATION");
            intent1.putExtra("com.htc.launcher.extra.COMPONENT", launcherComponentName.flattenToShortString());
            intent1.putExtra("com.htc.launcher.extra.COUNT", count);
            context.sendBroadcast(intent1);

            Intent intent2 = new Intent("com.htc.launcher.action.UPDATE_SHORTCUT");
            intent2.putExtra("packagename", launcherComponentName.getPackageName());
            intent2.putExtra("count", count);
            context.sendBroadcast(intent2);

            return true;
        } catch (Exception ignored) {
            Log.e(TAG, "set up HTC Badge failure");
            return false;
        }
    }

    private static boolean setSonyBadge(int count, Context context) {
        String launcherClassName = getLauncherClassName(context);
        if (TextUtils.isEmpty(launcherClassName)) {
            return false;
        }
        try {
            //官方给出方法
            ContentValues contentValues = new ContentValues();
            contentValues.put("badge_count", count);
            contentValues.put("package_name", context.getPackageName());
            contentValues.put("activity_name", launcherClassName);
            SonyAsyncQueryHandler asyncQueryHandler = new SonyAsyncQueryHandler(context.getContentResolver());
            asyncQueryHandler.startInsert(0, null, Uri.parse("content://com.sonymobile.home.resourceprovider/badge"), contentValues);
            return true;
        } catch (Exception e) {
            try {
                //网上大部分使用方法
                Intent intent = new Intent("com.sonyericsson.home.action.UPDATE_BADGE");
                intent.putExtra("com.sonyericsson.home.intent.extra.badge.SHOW_MESSAGE", count > 0);
                intent.putExtra("com.sonyericsson.home.intent.extra.badge.ACTIVITY_NAME", launcherClassName);
                intent.putExtra("com.sonyericsson.home.intent.extra.badge.MESSAGE", String.valueOf(count));
                intent.putExtra("com.sonyericsson.home.intent.extra.badge.PACKAGE_NAME", context.getPackageName());
                context.sendBroadcast(intent);
                return true;
            } catch (Exception ignored) {
                Log.e(TAG, "set up Sony Badge failure");
                return false;
            }
        }
    }

    private static String getLauncherClassName(Context context) {
        ComponentName launchComponent = getLauncherComponentName(context);
        if (launchComponent == null) {
            return "";
        } else {
            return launchComponent.getClassName();
        }
    }

    private static ComponentName getLauncherComponentName(Context context) {
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        if (launchIntent != null) {
            return launchIntent.getComponent();
        } else {
            return null;
        }
    }

    static class SonyAsyncQueryHandler extends AsyncQueryHandler {

        SonyAsyncQueryHandler(ContentResolver cr) {
            super(cr);
        }
    }
}
