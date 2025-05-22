package tw.com.chainsea.chat.lib;

import android.content.Context;
import android.widget.Toast;

import tw.com.chainsea.chat.widget.toast.CenterToast;

public class ToastUtils {
    public static void showToast(Context context, String msg) {
        try {
            Toast.makeText(context.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        } catch (Exception ignored) {
        }
    }

    public static void showCenterToast(Context context, String msg) {
        try {
            new CenterToast(context.getApplicationContext(), msg).show();
        } catch (Exception ignored) {
        }
    }
}
