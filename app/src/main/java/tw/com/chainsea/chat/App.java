package tw.com.chainsea.chat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDexApplication;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.sentry.android.core.SentryAndroid;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.database.DBHelper;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.chat.refactor.welcomePage.WelcomeActivity;
import tw.com.chainsea.chat.widget.CallData;


/**
 * application
 * Created by 90Chris on 2016/5/25.
 */
public class App extends MultiDexApplication {
    @SuppressLint("StaticFieldLeak")
    private static volatile App sInstance;
    public String callRoomId;
    private final Map<String, CallData> meetingIdMap = Maps.newHashMap();
    private WeakReference<Activity> currentActivity;

    public final static int APP_STATUS_KILLED = 0; // Indicates that the application was started after being killed
    public final static int APP_STATUS_NORMAL = 1; // Indicates the normal startup process of the application
    public static int APP_STATUS = APP_STATUS_KILLED; // record the startup status of the App
    public String chatRoomId;
    public List<ChatRoomEntity> serviceChatRoom = new ArrayList<>();

    @SuppressLint("SimpleDateFormat")
    @Override
    public void onCreate() {
        super.onCreate();
        SentryAndroid.init(this, sentryAndroidOptions -> sentryAndroidOptions.setEnvironment(BuildConfig.FLAVOR));
        CELog.d("start up usd time ::: " + new SimpleDateFormat("HH:mm:ss.SSS").format(System.currentTimeMillis()));
        sInstance = this;

        ThreadExecutorHelper.getIoThreadExecutor().execute(() -> {
            String userId = TokenPref.getInstance(getApplicationContext()).getUserId();
            if (!Strings.isNullOrEmpty(userId)) {
                CELog.startLogSave(getApplicationContext(), userId);
            }

            try {
                CELog.deleteLogForNotTodayAndMine();
            } catch (Exception ignored) {

            }
        });

        ThreadExecutorHelper.getIoThreadExecutor().execute(() -> {
            int ceVersion = TokenPref.getInstance(getApplicationContext()).getDBVersion();
            CELog.d("SQLite  database version-->" + ceVersion);
            if (ceVersion < DBHelper.DATABASE_VERSION) {
                CELog.e("SQLite  database version upgrade ( " + ceVersion + " --> " + DBHelper.DATABASE_VERSION + " )");
                // Database Version upgrade No longer log out
                TokenPref.getInstance(getApplicationContext()).setDBVersion(DBHelper.DATABASE_VERSION);
            }

            registerActivityLifecycleCallbacks(new ActivityLifecycleListener());
        });

        CELog.d("start up usd time ::: " + new SimpleDateFormat("HH:mm:ss.SSS").format(System.currentTimeMillis()));
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }

    public static Context getContext() {
        return sInstance.getApplicationContext();
    }

    public Activity currentActivity() {
        return currentActivity.get();
    }

    public static App getInstance() {
        return sInstance;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        sInstance = null;  // 清理引用
    }

    public void saveMeetingId(String callRoomId, CallData data) {
        meetingIdMap.clear();
        meetingIdMap.put(callRoomId, data);
    }

    public String getCallRoomId() {
        return callRoomId;
    }

    public void clearMeetingId() {
        meetingIdMap.clear();
    }

    class ActivityLifecycleListener implements ActivityLifecycleCallbacks {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            CELog.d("onActivityCreated :: " + activity.getClass().getSimpleName());
        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {
        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {
            currentActivity = new WeakReference<>(activity);
        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {
        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, Bundle outState) {
            CELog.d("");
        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
        }
    }

    //如果記憶體的東西被外部清除, 那這個就會幫他自動重啟
    public static void reInitApp() {
        if (!BuildConfig.DEBUG) {
            Intent intent = new Intent(getContext(), WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);
        }
    }
}
