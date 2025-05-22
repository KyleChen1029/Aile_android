package tw.com.chainsea.chat.receiver;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.databinding.DataBindingUtil;

import com.google.common.base.Strings;

import org.greenrobot.eventbus.EventBus;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.system.ActivityManagerHelper;
import tw.com.chainsea.android.common.ui.UiHelper;
import tw.com.chainsea.ce.sdk.bean.PicSize;
import tw.com.chainsea.ce.sdk.bean.todo.TodoEntity;
import tw.com.chainsea.ce.sdk.bean.todo.TodoStatus;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.event.EventBusUtils;
import tw.com.chainsea.ce.sdk.event.EventMsg;
import tw.com.chainsea.ce.sdk.event.MsgConstant;
import tw.com.chainsea.ce.sdk.service.AvatarService;
import tw.com.chainsea.chat.BuildConfig;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.FloatWindowTodoAlarmAlertBinding;
import tw.com.chainsea.chat.lib.ActivityManager;
import tw.com.chainsea.chat.lib.RingtoneHelper;
import tw.com.chainsea.chat.refactor.welcomePage.WelcomeActivity;
import tw.com.chainsea.chat.service.TodoAlarmTimerService;

/**
 * current by evan on 2020-07-16
 *
 * @author Evan Wang
 * on 2020-07-16
 */
public class AlarmReceiver extends BroadcastReceiver {
//public class AlarmReceiver extends WakefulBroadcastReceiver {

    //    private static final String TAG = AlarmReceiver.class.getSimpleName();
    //    private NotificationManager m_notificationMgr = null;
    private static final int NOTIFICATION_FLAG = 3;
    //    private static final String NOTIFICATION_TAG = "AlarmClock";
    private static final String channelId = "TodoNotice";
//    SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmm");

    long fiveMinutes = BuildConfig.DEBUG ? 1000 * 10L : 1000 * 60 * 5L;

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String currentUserId = TokenPref.getInstance(context).getUserId();
        if (intent.hasExtra("data")) {
            String data = intent.getStringExtra("data");
            TodoEntity entity = JsonHelper.getInstance().from(data, TodoEntity.class);

            // Instance cannot be sequenced
            if (entity == null) {
                return;
            }
            // Currently removed or logged out, do not remind
            if (Strings.isNullOrEmpty(currentUserId)) {
                return;
            }
            // The reminder does not belong to you or there is no User Id
            if (!currentUserId.equals(entity.getUserId())) {
                return;
            }

            String action = intent.getAction();
            switch (action) {
                case AlarmValues.DELAY_ACTION: // delay
                    long newTime = System.currentTimeMillis() + fiveMinutes;
                    entity.setRemindTime(newTime);
                    manager.cancel(entity.getId(), entity.getUniqueId());
                    RingtoneHelper.stop(context);
                    TodoAlarmTimerService.setAlarmTimer(context, AlarmValues.TIMER_ACTION, AlarmManager.RTC_WAKEUP, entity);
                    try {
                        EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.NOTICE_TODO_ALARM_DELAY, entity.getId()));
                    } catch (Exception ignored) {
                    }
                    break;
                case AlarmValues.CONFIRM_ACTION:
                    manager.cancel(entity.getId(), entity.getUniqueId());
                    RingtoneHelper.stop(context);
                    break;
                case AlarmValues.TIMER_ACTION:
                case AlarmValues.TIMER_ACTION_REPEATING:
                    if (TodoStatus.DONE_or_DELETED.contains(entity.getStatus()) || entity.isOutOfBounds()) {
//                        CELog.w("completed || deleted || timed out");
                    } else {
                        EventBus.getDefault().post(new EventMsg<>(MsgConstant.REFRESH_FILTER));
                        AppCompatActivity activity = ActivityManager.findBaseActivity();
                        if (entity.getOpenClock()) {
                            RingtoneHelper.play(context);
                        }
                        try {
                            if (activity != null && ActivityManagerHelper.isInTheForeground(context)) {
                                createFloatTodoDialog(activity, entity);
                            }
                        } catch (Exception ignored) {
                        }
                        sendNotify(manager, context, intent, entity);
                    }
                    break;
                case AlarmValues.NOTIFY_ACTION:
                    sendNotify(manager, context, intent, entity);
                    break;
                case AlarmValues.REMOVE_ACTION:
                    RingtoneHelper.stop(context);
                    break;

            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void createFloatTodoDialog(Context context, TodoEntity entity) {
        WindowManager windowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();

        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        params.width = UiHelper.getDisplayWidth(context) - UiHelper.dip2px(context.getApplicationContext(), 16);
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.y = 100;
        params.gravity = Gravity.TOP | Gravity.CENTER;
        params.dimAmount = 0.2f;
        params.flags = (WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        params.format = PixelFormat.TRANSLUCENT;

        FloatWindowTodoAlarmAlertBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context),
            R.layout.float_window_todo_alarm_alert, null, false);

        if (entity.getRoomEntity() != null) {
            AvatarService.post(context.getApplicationContext(), entity.getRoomEntity().getAvatarIds(context.getApplicationContext(), 4), PicSize.SMALL, binding.civIcon, R.drawable.res_check_list_circle_def);
        } else {
            binding.civIcon.setImageResource(R.drawable.res_check_list_circle_def);
        }
        binding.tvContent.setText(entity.getTitle());
        binding.tvContent.setOnClickListener(v -> {
            EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.UI_NOTICE_TO_TODO_ITEM, entity.toJson()));
            RingtoneHelper.stop(context.getApplicationContext());
            windowManager.removeView(binding.getRoot());
        });
        binding.btnDelay.setOnClickListener(v -> {
            long alarmTime = System.currentTimeMillis() + fiveMinutes;
            TodoAlarmTimerService.setAlarmTimer(context.getApplicationContext(), AlarmValues.TIMER_ACTION, AlarmManager.RTC_WAKEUP, entity, alarmTime);
            RingtoneHelper.stop(context.getApplicationContext());
            // 一併清除 Notification
            NotificationManager manager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(entity.getId(), entity.getUniqueId());
            windowManager.removeView(binding.getRoot());
        });
        //向上滑關閉
        binding.clRoot.setOnTouchListener(new View.OnTouchListener() {
            private float y;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        y = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        if (y - event.getY() > 90) {
                            RingtoneHelper.stop(context);
                            windowManager.removeView(binding.getRoot());
                            return true;
                        }
                        v.performClick();
                        break;
                }
                return false;
            }
        });
        binding.clRoot.setOnClickListener(view -> {
        });
        windowManager.addView(binding.getRoot(), params);
        //設定多久後自動關掉, 暫定5分鐘
        new CountDownTimer(5 * 60 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                try { //如果視窗已經關閉則會crash
                    RingtoneHelper.stop(context);
                    windowManager.removeView(binding.getRoot());
                } catch (Exception ignored) {
                }
            }
        }.start();
    }

    private void sendNotify(NotificationManager manager, Context context, Intent intent, TodoEntity entity) {

        PendingIntent pendingIntent = PendingIntent.getActivity(context, entity.getUniqueId(), new Intent(context, WelcomeActivity.class), PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        // confirm action
        Intent proceedIntent = new Intent(context, this.getClass()).putExtra("data", entity.toJson());
        proceedIntent.setAction(AlarmValues.CONFIRM_ACTION);
        PendingIntent proceedPendingIntent = PendingIntent.getBroadcast(context, entity.getUniqueId(), proceedIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        // delay action
        Intent delayIntent = new Intent(context, this.getClass()).putExtra("data", entity.toJson());
        delayIntent.setAction(AlarmValues.DELAY_ACTION);
        PendingIntent delayPendingIntent = PendingIntent.getBroadcast(context, entity.getUniqueId(), delayIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        // remove action
        Intent removeIntent = new Intent(context, this.getClass()).putExtra("data", entity.toJson());
        removeIntent.setAction(AlarmValues.REMOVE_ACTION);
        PendingIntent removePendingIntent = PendingIntent.getBroadcast(context, entity.getUniqueId(), removeIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder notificationBuilder =
            new NotificationCompat.Builder(context.getApplicationContext(), channelId)
                .setColor(context.getColor(R.color.colorAccent))
                .setSmallIcon(R.drawable.ce_notification_icon)
                .setContentTitle(entity.getTitle())
                .setContentText(context.getString(R.string.todo_you_have_a_reminder_that_has_expired))
                .setAutoCancel(true)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//                        .setSound(defaultSoundUri)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true)
//                        .setDefaults(NotificationCompat.DEFAULT_VIBRATE | NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setDeleteIntent(removePendingIntent)
                .addAction(new NotificationCompat.Action(0, context.getString(R.string.alert_confirm), proceedPendingIntent))
                .addAction(new NotificationCompat.Action(0, context.getString(R.string.alert_delay_time), delayPendingIntent));

        // Since android Oreo notification channel is needed.
        NotificationChannel channel = new NotificationChannel(channelId, entity.getTitle(), NotificationManager.IMPORTANCE_LOW);
        channel.setShowBadge(true);
//            AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
//            channel.setSound(defaultSoundUri, audioAttributes);
        manager.createNotificationChannel(channel);
        manager.notify(entity.getId(), entity.getUniqueId() /* ID of notification */, notificationBuilder.build());
    }


}
