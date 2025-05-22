package tw.com.chainsea.chat.service;

import static android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import tw.com.chainsea.ce.sdk.bean.todo.TodoEntity;
import tw.com.chainsea.chat.receiver.AlarmReceiver;
import tw.com.chainsea.android.common.log.CELog;

/**
 * current by evan on 2020-07-16
 *
 * @author Evan Wang
 * @date 2020-07-16
 */
public class TodoAlarmTimerService {


    /**
     * setup timed alarm
     *
     * @param context
     * @param action
     * @param alarmManagerType The type of alarm clock, there are 5 commonly used values：
     *                         AlarmManager.ELAPSED_REALTIME、
     *                         AlarmManager.ELAPSED_REALTIME_WAKEUP、
     *                         AlarmManager.RTC、
     *                         AlarmManager.RTC_WAKEUP、
     *                         AlarmManager.POWER_OFF_WAKEUP
     */
    public static void setAlarmTimer(Context context, String action, int alarmManagerType, TodoEntity entity) {
        entity.setOutOfBounds(false);

        long notifyTime = entity.getRemindTime();
        long now = System.currentTimeMillis();
        long alarmTime = entity.getRemindTime();

        // If the cancel notification time is updated after the original set alarm notification
        if (alarmTime <= 0) {
            notifyTime = now - 60 * 60 * 1000L;
            entity.setOutOfBounds(true);
        }
        // expired
        if (now >= alarmTime) {
            notifyTime = alarmTime - 60 * 60 * 1000L;
            entity.setOutOfBounds(true);
        }

        Intent intent = new Intent().putExtra("data", entity.toJson());
        intent.setAction(action);
        intent.setClass(context, AlarmReceiver.class);
        // Set a different ID for each alarm to prevent overwriting
        int alarmId = entity.getUniqueId();
        PendingIntent sender = PendingIntent.getBroadcast(context, alarmId, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
//        PendingIntent.getBroadcast(context, alarmId, intent, 0);

        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarm.canScheduleExactAlarms()) {
                context.startActivity(new Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM));
            }
            Log.d("TodoAlarmTimerService", "isCanScheduleExactAlarms: " + alarm.canScheduleExactAlarms());
        }
        try {
            alarm.setExactAndAllowWhileIdle(alarmManagerType, notifyTime, sender);
        } catch (Exception e) {
            alarm.set(alarmManagerType, notifyTime, sender);
        }
    }


    public static void setAlarmTimer(Context context, String action, int alarmManagerType, TodoEntity entity, long alarmTime) {
        Intent intent = new Intent().putExtra("data", entity.toJson());
        intent.setAction(action);
        intent.setClass(context, AlarmReceiver.class);
        // Set a different ID for each alarm to prevent overwriting
        int alarmId = entity.getUniqueId();
        PendingIntent sender = PendingIntent.getBroadcast(context, alarmId, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.set(alarmManagerType, alarmTime, sender);

    }


    /**
     * Cancel alarm
     * // EVAN_FLAG 2020-08-06 (1.12.0) unable to work properly
     *
     * @param context
     * @param action
     */
    @Deprecated
    public static void cancelAlarmTimer(Context context, String action, TodoEntity entity) {
        if (entity == null) {
            return;
        }
        Intent intent = new Intent();
        intent.setAction(action);
        CELog.e("cancel :" + entity.getUniqueId());
        PendingIntent sender = PendingIntent.getBroadcast(context, entity.getUniqueId(), intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (sender != null) {
            alarm.cancel(sender);
        }
    }


    /**
     * Update alarm
     *
     * @param context
     * @param action
     */
    @Deprecated
    public static void updateAlarmTimer(Context context, String action, int alarmManagerType, TodoEntity entity) {
        if (entity == null) {
            return;
        }
        cancelAlarmTimer(context, action, entity);
        setAlarmTimer(context, action, alarmManagerType, entity);
    }


//    /**
//     * Cancel alarm
//     *
//     * @param context
//     * @param action
//     */
//    public static void cancelAlarmTimer(Context context, String action) {
//        Intent myIntent = new Intent();
//        myIntent.setAction(action);
//        PendingIntent sender = PendingIntent.getBroadcast(context, 0, myIntent, 0);
//        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        alarm.cancel(sender);
//    }
}
