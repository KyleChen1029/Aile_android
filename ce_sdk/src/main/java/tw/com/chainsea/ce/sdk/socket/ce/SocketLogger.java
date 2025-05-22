package tw.com.chainsea.ce.sdk.socket.ce;

import androidx.annotation.NonNull;

import okhttp3.logging.HttpLoggingInterceptor;
import tw.com.chainsea.android.common.log.CELog;

/**
 * current by evan on 2020-10-27
 *
 * @author Evan Wang
 * date 2020-10-27
 */
public class SocketLogger implements HttpLoggingInterceptor.Logger {
    @Override
    public void log(@NonNull String message) {
        CELog.i(message);
    }

    public void d(String message) {
        CELog.d(message);
    }

    public void e(String message) {
        CELog.e(message);
    }

    public void i(String message, Object... args) {
        CELog.i(message, args);
    }

}
