package tw.com.chainsea.android.common.log;

import static tw.com.chainsea.android.common.SystemConfig.enableLogcat;

import android.util.Log;

/**
 * Created by 90Chris on 2016/6/8.
 */
public enum LogLevel {
    DEBUG(15) {
        @Override
        int log(String tag, String message) {
            return (enableLogcat) ? Log.d(tag, message) : 0;
        }
    },
    ERROR(1) {
        @Override
        int log(String tag, String message) {
            return (enableLogcat) ? Log.e(tag, message) : 0;
        }
    },
    INFO(7) {
        @Override
        int log(String tag, String message) {
            return (enableLogcat) ? Log.i(tag, message) : 0;
        }
    },
    NONE(0) {
        @Override
        int log(String tag, String message) {
            return 0;
        }
    },
    WARN(3) {
        @Override
        int log(String tag, String message) {
            return (enableLogcat) ? Log.w(tag, message) : 0;
        }
    };

    private final int mLevel;

    LogLevel(int level) {
        mLevel = level;
    }

    abstract int log(String tag, String message);

    final public int getValue() {
        return mLevel;
    }
}
