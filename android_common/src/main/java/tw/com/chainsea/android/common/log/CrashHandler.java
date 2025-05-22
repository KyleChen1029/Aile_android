package tw.com.chainsea.android.common.log;

import static tw.com.chainsea.android.common.log.CELog.eLogFile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;

import androidx.annotation.NonNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * CrashHandler
 * GlobalExceptionHandler
 */
public class CrashHandler implements UncaughtExceptionHandler {

    private UncaughtExceptionHandler mDefaultHandler;
    @SuppressLint("StaticFieldLeak")
    private static volatile CrashHandler INSTANCE = new CrashHandler();
    private Context mContext;
    private final Map<String, String> infos = new HashMap<>();

    private CrashHandler() {
    }

    protected static CrashHandler getInstance() {
        return INSTANCE;
    }

    public void init(Context context) {
        mContext = context;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }


    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable ex) {
//        if (BuildConfig.DEBUG) {
//            try {
//                ClientsHelper.post(true).execute("http://10.11.5.88:8080/api/logger", ex.getMessage(), null);
//                //保存到本地
////            exportExceptionToSDCard(ex);
//                //下面也可以写上传的服务器的代码
//            } catch (Exception e1) {
//            }
//        }

        handleException(ex);
        INSTANCE = null;
        if (mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(thread, ex);
        }
    }

    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        collectDeviceInfo(mContext);
        saveCrashInfo2File(ex);
        return true;
    }


    protected void collectDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
            }
        } catch (NameNotFoundException e) {
            CELog.e("an error occurred when collect package info", e);
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
            } catch (Exception e) {
                CELog.e("an error occurred when collect crash info", e);
            }
        }
    }

    private void saveCrashInfo2File(Throwable ex) {

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key);
            sb.append("=");
            sb.append(value);
            sb.append("\n");
        }

        CELog.appendLog(eLogFile, "=====================Unexpected Crash Happened begin=================");
        CELog.appendLog(eLogFile, sb.toString());

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        CELog.e(result);
        CELog.appendLog(eLogFile, "=====================Unexpected Crash Happened end=================");
    }
}
