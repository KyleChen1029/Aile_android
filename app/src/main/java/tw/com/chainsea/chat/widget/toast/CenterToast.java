package tw.com.chainsea.chat.widget.toast;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import tw.com.chainsea.chat.R;

/**
 * Created by sunhui on 2018/3/30.
 */

public class CenterToast extends Toast {
    private TextView mToast_text;

    /**
     * Construct an empty Toast object.  You must call {@link #setView} before you
     * can call {@link #show}.
     *
     * @param context The context to use.  Usually your {@link Application}
     *                or {@link Activity} object.
     */
    public CenterToast(Context context, CharSequence text) {
        super(context);
        initToast(context, text);
    }

    @SuppressLint("InflateParams")
    private void initToast(Context context, CharSequence text) {
        try {
            // 获取LayoutInflater对象
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // 由layout文件创建一个View对象
            View layout = inflater.inflate(R.layout.center_toast_layout, null);
            // 吐司上的文字
            mToast_text = (TextView) layout.findViewById(R.id.toast_text);
            mToast_text.setText(text);
            setView(layout);
            setGravity(Gravity.CENTER, 0, 70);
        } catch (Exception ignored) {
        }
    }

    public void cancel() {
        try {
            super.cancel();
        } catch (Exception ignored) {

        }
    }

    public void showToast(String toastText) {
        mToast_text.setText(toastText);
        show();
    }
}
