package tw.com.chainsea.custom.view.progress;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.StringRes;

import tw.com.chainsea.custom.view.R;

public class IosProgressBar extends Dialog {
    public IosProgressBar(Context context) {
        super(context);
    }

    public IosProgressBar(Context context, int theme) {
        super(context, theme);
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        ImageView imageView = (ImageView) findViewById(R.id.spinnerImageView);
        AnimationDrawable spinner = (AnimationDrawable) imageView.getBackground();
        spinner.start();
    }

    public IosProgressBar setMessage(CharSequence message) {
        if (message != null && message.length() > 0) {
            findViewById(R.id.message).setVisibility(View.VISIBLE);
            TextView txt = (TextView) findViewById(R.id.message);
            txt.setText(message);
            txt.invalidate();
        }
        return this;
    }

    public static IosProgressBar show(Context context, CharSequence message, boolean indeterminate, boolean cancelable, OnCancelListener cancelListener) {
        IosProgressBar dialog = new IosProgressBar(context, R.style.ProgressBar_Style);
        dialog.setTitle("");
        dialog.setContentView(R.layout.dialog_progress_hud);
        if (message == null || message.length() == 0) {
            dialog.findViewById(R.id.message).setVisibility(View.GONE);
        } else {
            TextView txt = (TextView) dialog.findViewById(R.id.message);
            txt.setText(message);
        }

        dialog.setCancelable(cancelable);
        dialog.setOnCancelListener(cancelListener);
        dialog.getWindow().getAttributes().gravity = Gravity.CENTER;
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.dimAmount = 0.2f;
        dialog.getWindow().setAttributes(lp);
        //dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        try {
            dialog.show();
        } catch (Exception ignored) {
        }
        return dialog;
    }

    public static IosProgressBar show(Context context, @StringRes int resId, boolean indeterminate, boolean cancelable, OnCancelListener cancelListener) {
        String message = context.getString(resId);
        return show(context, message, indeterminate, cancelable, cancelListener);
    }
}
