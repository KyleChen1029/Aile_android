package tw.com.chainsea.chat.service;

import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import tw.com.chainsea.android.common.system.BadgeHelper;
import tw.com.chainsea.ce.sdk.database.sp.UserPref;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.refactor.welcomePage.WelcomeActivity;

/**
 * current by evan on 2020-06-22
 *
 * @author Evan Wang
 * @date 2020-06-22
 */
public class BadgeServer extends NotificationListenerService {

    private BadgeServerBinder binder = new BadgeServerBinder();

    Handler handler = new Handler();
    SendRunnable sendRunnable = new SendRunnable();

    public class BadgeServerBinder extends Binder {
        public BadgeServer getService() {
            return BadgeServer.this;
        }
    }

    public BadgeServer() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_REDELIVER_INTENT;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        handler.post(sendRunnable);
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        handler.removeCallbacks(sendRunnable);
        return false;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
    }

    class SendRunnable implements Runnable {
        @Override
        public void run() {
            int badge = UserPref.getInstance(getApplicationContext()).getBrand();
            BadgeHelper.setCount(badge, getApplicationContext(), WelcomeActivity.class, R.drawable.ce_notification_icon);
            handler.postDelayed(sendRunnable, 500L);
        }
    }

}
