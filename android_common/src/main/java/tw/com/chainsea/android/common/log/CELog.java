package tw.com.chainsea.android.common.log;

import static tw.com.chainsea.android.common.SystemConfig.enableSaveLogFile;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import tw.com.chainsea.android.common.R;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;


/**
 * CELog
 * Created by 90Chris on 2016/3/30.
 */
// EVAN_REFACTOR: 2019-09-25 修改 package
public class CELog {
    private static final int DEBUG_CODE = 8;
    private static final int INFO_CODE = 4;
    private static final int WARN_CODE = 2;
    private static final int ERROR_CODE = 1;

    private static String LOG_TAG = "CELog";
    public static LogLevel logLevel = LogLevel.NONE;

    private static File dLogFile;
    private static File iLogFile;
    private static File wLogFile;
    public static File eLogFile;
    private static File mLogFile;

    private static String mUserId;
    private static File dir;

    /**
     * init, you should call it before any PtLog used
     */
    public static void init(LogLevel level, String tag, Context ctx) {
        LOG_TAG = tag;
        logLevel = level;

        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(ctx);

        dir = new File(ctx.getFilesDir() + "/log");
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                CELog.e(LOG_TAG, "LOG File create Fail!!");
            }
        }
    }

    /**
     * start saving log in the file, it can be called multiple times
     * and will save id different file
     * the log saved under /Android/data/{package_name}/log
     */
    public static void startLogSave(Context context, String userId) {
        mUserId = userId;
        String debugFileName = context.getString(R.string.debug_log_file_name);
        String infoFileName = context.getString(R.string.info_log_file_name);
        String warningFileName = context.getString(R.string.warning_log_file_name);
        String errorFileName = context.getString(R.string.error_log_file_name);
        String messageFileName = context.getString(R.string.message_log_file_name);

        SimpleDateFormat sdFormatter = new SimpleDateFormat("MM-dd", Locale.TAIWAN);
        String today = sdFormatter.format(System.currentTimeMillis());
        dLogFile = new File(dir, today + "-" + userId + "_" + debugFileName);
        iLogFile = new File(dir, today + "-" + userId + "_" + infoFileName);
        wLogFile = new File(dir, today + "-" + userId + "_" + warningFileName);
        eLogFile = new File(dir, today + "-" + userId + "_" + errorFileName);
        mLogFile = new File(dir, today + "-" + userId + "_" + messageFileName);
    }

    public static List<String> getLogPath() {
        List<String> paths = new ArrayList<>();
        paths.add(dLogFile.getAbsolutePath());
        paths.add(wLogFile.getAbsolutePath());
        paths.add(eLogFile.getAbsolutePath());
        paths.add(mLogFile.getAbsolutePath());
        return paths;
    }

    public static void deleteLogForNotTodayAndMine() {
        if (mUserId == null || mUserId.isEmpty()) {
            dir.delete();
            return;
        }

        File[] files = dir.listFiles();
        for (File file : files) {
            if (!file.getName().equals(dLogFile.getName()) && !file.getName().equals(iLogFile.getName()) && !file.getName().equals(eLogFile.getName()) && !file.getName().equals(wLogFile.getName()) && !file.getName().equals(mLogFile.getName())) {
                file.delete();
            }
        }
    }

    private static final int RETURN_NOLOG = 99;

    private static int logcat(LogLevel logLevel, String tag, String content) {
        int p = 2000;
        long length = content.length();
        if (length < p || length == p)
            return logLevel.log(tag, content);
        else {
            while (content.length() > p) {
                String logContent = content.substring(0, p);
                content = content.replace(logContent, "");
                logLevel.log(tag, logContent);
            }
            return logLevel.log(tag, "\uD83D\uDC49 \uD83D\uDC49 \uD83D\uDC49 \uD83D\uDC49 ---->" + content);
        }
    }

    public static void d(String msg, Object... args) {
//        if ((logLevel.getValue() & DEBUG_CODE) == 0) {
//            return RETURN_NOLOG;
//        }
        String con = dressUpTag() + ":" + buildMessage(msg, args);
        appendLog(dLogFile, "[DEBUG]:" + LOG_TAG + ":" + con);
        logcat(LogLevel.DEBUG, LOG_TAG, con);
    }

    public static void i(String msg, Object... args) {
//        if ((logLevel.getValue() & INFO_CODE) == 0) {
//            return RETURN_NOLOG;
//        }
        String con = dressUpTag() + ":" + buildMessage(msg, args);
        appendLog(iLogFile, "[INFO]:" + LOG_TAG + ":" + con);
        logcat(LogLevel.INFO, LOG_TAG, con);
    }

    public static void w(String msg, Object... args) {
//        if ((logLevel.getValue() & WARN_CODE) == 0) {
//            return RETURN_NOLOG;
//        }
        String con = dressUpTag() + ":" + buildMessage(msg, args);
        appendLog(wLogFile, "[WARNING]:" + LOG_TAG + ":" + con);
        logcat(LogLevel.WARN, LOG_TAG, con);
    }

    public static void e(String msg, Object... args) {
//        if ((logLevel.getValue() & ERROR_CODE) == 0) {
//            return RETURN_NOLOG;
//        }
        String con = dressUpTag() + ":" + buildMessage(msg, args);
        appendLog(eLogFile, "[ERROR]:" + LOG_TAG + ":" + con);
        logcat(LogLevel.ERROR, LOG_TAG, con);
    }

    public static void e(String msg, Throwable ex, Object... args) {
//        if ((logLevel.getValue() & ERROR_CODE) == 0) {
//            return RETURN_NOLOG;
//        }
        String con = dressUpTag() + ":" + buildMessage(msg, args);

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        printWriter.close();
        String result = writer.toString();

        appendLog(eLogFile, "[ERROR]:" + LOG_TAG + ":" + con + ":" + result);
        Log.e(LOG_TAG, con, ex);
    }

    /**
     * 給message專用logger
     */
    public static int msg(String msg, Object... args) {
        if ((logLevel.getValue() & DEBUG_CODE) == 0) {
            return RETURN_NOLOG;
        }
        String con = dressUpTag() + ":" + buildMessage(msg, args);
        appendLog(mLogFile, "[MESSAGE]:" + LOG_TAG + ":" + con);
        return logcat(LogLevel.INFO, LOG_TAG, con);
    }

    /**
     * after saveLog opened, call the method, append the text in log file
     */
    protected static synchronized void appendLog(File file, String text) {
        if (!enableSaveLogFile) return;
        if (file == null) {
            return;
        }
        ThreadExecutorHelper.getIoThreadExecutor().execute(() -> {
            try {
                SimpleDateFormat sdFormatter = new SimpleDateFormat("[MM-dd HH:mm:ss]", Locale.TAIWAN);
                if (!dir.exists()) {
                    if (!dir.mkdir()) {
                        CELog.e(LOG_TAG, "LOG File create Fail!!");
                    }
                }
                if (!file.exists()) {
                    OutputStream outputStream = new FileOutputStream(file);
                    outputStream.write((sdFormatter.format(System.currentTimeMillis()) + "Created File\n").getBytes());
                    outputStream.close();
                }
                //BufferedWriter for performance, true to set append to file flag
                BufferedWriter buf = new BufferedWriter(new FileWriter(file, true));
                buf.append(sdFormatter.format(System.currentTimeMillis()));

                buf.append(text);
                buf.newLine();
                buf.close();
            } catch (IOException ignored) {

            }
        });
    }

    private static String dressUpTag() {
        String className;
        int lineNum;
        String methodName;
        String fireName;
        StackTraceElement thisMethodStack = (new Exception()).getStackTrace()[2];
        className = thisMethodStack.getClassName();
        lineNum = thisMethodStack.getLineNumber();
        methodName = thisMethodStack.getMethodName();
        fireName = thisMethodStack.getFileName();

        int lastIndex = className.lastIndexOf(".");
        className = className.substring(lastIndex + 1);
        String caller = className + "." + methodName + "(" + fireName + ":" + lineNum + ")";
        return String.format(Locale.US, "[%d] %s", Thread.currentThread().getId(), caller);
    }

    private static String buildMessage(String format, Object... args) {
        try {
            return (args == null) ? format : String.format(Locale.US, format, args);
        } catch (Exception e) {
            return format;
        }
    }

    /**
     * A simple event log with records containing a name, thread ID, and timestamp.
     */
    public static class MarkerLog {
        /**
         * Minimum duration from first marker to last in an marker log to warrant logging.
         */
        private static final long MIN_DURATION_FOR_LOGGING_MS = 0;

        private static class Marker {
            public final String name;
            public final long thread;
            public final long time;

            public Marker(String name, long thread, long time) {
                this.name = name;
                this.thread = thread;
                this.time = time;
            }
        }

        private final List<Marker> mMarkers = new ArrayList<>();
        private boolean mFinished = false;

        /**
         * Adds a marker to this log with the specified name.
         */
        public synchronized void add(String name, long threadId) {
            if (mFinished) {
                throw new IllegalStateException("Marker added to finished log");
            }

            mMarkers.add(new Marker(name, threadId, SystemClock.elapsedRealtime()));
        }

        /**
         * Closes the log, dumping it to logcat if the time difference between
         * the first and last markers is greater than {@link #MIN_DURATION_FOR_LOGGING_MS}.
         *
         * @param header Header string to print above the marker log.
         */
        public synchronized void finish(String header) {
            mFinished = true;

            long duration = getTotalDuration();
            if (duration <= MIN_DURATION_FOR_LOGGING_MS) {
                return;
            }

            long prevTime = mMarkers.get(0).time;
            d("(%-4d ms) %s", duration, header);
            for (Marker marker : mMarkers) {
                long thisTime = marker.time;
                d("(+%-4d) [%2d] %s", (thisTime - prevTime), marker.thread, marker.name);
                prevTime = thisTime;
            }
        }

        @Override
        protected void finalize() throws Throwable {
            // Catch requests that have been collected (and hence end-of-lifed)
            // but had no debugging output printed for them.
            super.finalize();
            if (!mFinished) {
                finish("Request on the loose");
                e("Marker log finalized without finish() - uncaught exit point for request");
            }
        }

        /**
         * Returns the time difference between the first and last events in this log.
         */
        private long getTotalDuration() {
            if (mMarkers.isEmpty()) {
                return 0;
            }

            long first = mMarkers.get(0).time;
            long last = mMarkers.get(mMarkers.size() - 1).time;
            return last - first;
        }
    }
}
