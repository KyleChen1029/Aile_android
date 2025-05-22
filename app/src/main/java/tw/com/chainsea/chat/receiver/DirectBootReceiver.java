package tw.com.chainsea.chat.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.os.BuildCompat;

/**
 * current by evan on 12/9/20
 *
 * @author Evan Wang
 * @date 12/9/20
 */
public class DirectBootReceiver extends BroadcastReceiver {
    private static final String TAG = DirectBootReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean bootCompleted;
        String action = intent.getAction();
        if (BuildCompat.isAtLeastN()) {
            bootCompleted = Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(action);
        } else {
            bootCompleted = Intent.ACTION_BOOT_COMPLETED.equals(action);

        }
        if (!bootCompleted) {
            return;
        }
    }
}
