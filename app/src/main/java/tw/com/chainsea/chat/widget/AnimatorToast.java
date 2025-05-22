package tw.com.chainsea.chat.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import tw.com.chainsea.chat.R;

/**
 * Created by sunhui on 2018/3/30.
 */

public class AnimatorToast extends Toast {
    private ImageView mToast_img;
    boolean showSccess = false;

    /**
     * Construct an empty Toast object.  You must call {@link #setView} before you
     * can call {@link #show}.
     *
     * @param context The context to use.  Usually your {@link Application}
     *                or {@link Activity} object.
     */
//    public AnimatorToast(Context context, CharSequence text) {
//        super(context);
//        initToast(context, text);
//    }
    private AnimatorToast(Context context, CharSequence text, boolean showSccess) {
        super(context);
        initToast(context, text);
        this.showSccess = showSccess;
    }


    public static AnimatorToast makeSccessToast(Context context, CharSequence text) {
        return new AnimatorToast(context, text, true);
    }

    public static AnimatorToast makeErrorToast(Context context, CharSequence text) {
        return new AnimatorToast(context, text, false);
    }

    @Override
    public void show() {
        if (showSccess) {
            mToast_img.setImageResource(R.drawable.ic_done);
//            ObjectAnimator.ofFloat(mToast_img, "rotationY", 0, 360).setDuration(1700).start();
        } else {
            mToast_img.setImageResource(R.drawable.del2);
//            ObjectAnimator.ofFloat(mToast_img, "rotationY", 0, 360).setDuration(1700).start();
        }
        super.show();
    }

    @SuppressLint("InflateParams")
    private void initToast(Context context, CharSequence text) {
        try {
            // 获取LayoutInflater对象
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // 由layout文件创建一个View对象
            View layout = inflater.inflate(R.layout.toast_layout, null);
            // 吐司上的图片
            mToast_img = layout.findViewById(R.id.toast_img);
            // 吐司上的文字
            TextView mToast_text = layout.findViewById(R.id.toast_text);
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

//    public void setToast_text(String toastText) {
//        mToast_text.setText(toastText);
//    }
//
//    public void showSccessToast() {
//        // 动画
//        mToast_img.setImageResource(R.drawable.ic_done);
//        ObjectAnimator.ofFloat(mToast_img, "rotationY", 0, 360).setDuration(1700).start();
//        show();
//    }
//
//    public void showErrorToast() {
//        // 动画
//        mToast_img.setImageResource(R.drawable.del2);
//        ObjectAnimator.ofFloat(mToast_img, "rotationY", 0, 360).setDuration(1700).start();
//        show();
//    }
}
