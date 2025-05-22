package tw.com.chainsea.chat.lib;

import android.content.ContentResolver;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import java.io.File;

import tw.com.chainsea.android.common.system.ThreadExecutorHelper;

/**
 * current by evan on 2020-08-04
 *
 * @author Evan Wang
 * on 2020-08-04
 */
public class RingtoneHelper {

    private static long currentPlayStart = -1L;
    private static final long playInterval = 1000 * 30L;
    private static Ringtone instance = null;

    static Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (instance != null && !instance.isPlaying()) {
                instance.play();
            }
            long now = System.currentTimeMillis();
            if (now > currentPlayStart + playInterval) {
                ThreadExecutorHelper.getHandlerExecutor().remove(this);
                if (instance != null && instance.isPlaying()) {
                    instance.stop();
                }
                currentPlayStart = -1L;
            } else {
                ThreadExecutorHelper.getHandlerExecutor().execute(runnable, 2000L);
            }
        }
    };

    /**
     * Dedicated Scheduled Alarm
     */
    public static Ringtone getRingtoneInstance(Context context) {
        if (instance == null) {
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            if (alarmSound == null) {
                alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                if (alarmSound == null) {
                    alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                }
            }
            instance = RingtoneManager.getRingtone(context, alarmSound);
        }
        return instance;
    }


    public static void play(Context context) {
        currentPlayStart = System.currentTimeMillis();
        try {
            if (!getRingtoneInstance(context).isPlaying()) {
                getRingtoneInstance(context).play();
                ThreadExecutorHelper.getHandlerExecutor().execute(runnable);
            }
        } catch (Exception ignored) {
        }
    }

    public static void stop(Context context) {
        try {
            if (getRingtoneInstance(context).isPlaying()) {
                getRingtoneInstance(context).stop();
            }
            ThreadExecutorHelper.getHandlerExecutor().removeCallbacks(runnable);
        } catch (Exception ignored) {
        }
    }


    public static Uri getRawUri(Context context, String filename) {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + File.pathSeparator + File.separator + context.getPackageName() + "/raw/" + filename);
    }
}
