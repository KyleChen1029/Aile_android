package tw.com.chainsea.chat.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import tw.com.chainsea.custom.view.R;

public class IosProgressDialog extends Dialog {
    private final TextView textView;

    public IosProgressDialog(Context context) {
        super(context, R.style.ProgressBar_Style);
        setContentView(R.layout.dialog_progress_hud);
        textView = findViewById(R.id.message);
        ImageView imageView = findViewById(R.id.spinnerImageView);
        AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getBackground();
        animationDrawable.start();
        setCancelable(false);
        getWindow().getAttributes().gravity = Gravity.CENTER;
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.dimAmount = 0.2f;
        getWindow().setAttributes(lp);
    }

    public void show() {
        textView.setVisibility(View.GONE);
        super.show();
    }

    public void show(CharSequence message) {
        if (message == null || message.length() == 0) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setText(message);
        }
        super.show();
    }
}
