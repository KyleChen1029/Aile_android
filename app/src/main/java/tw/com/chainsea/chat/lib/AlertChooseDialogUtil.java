package tw.com.chainsea.chat.lib;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import tw.com.chainsea.chat.R;


/**
 * Created by json.wang on 2019/1/8.
 */

public class AlertChooseDialogUtil {
    private AlertDialog dialog;
    private static final AlertChooseDialogUtil ALERT_CHOOSE_DIALOG_UTIL = new AlertChooseDialogUtil();


    public static AlertChooseDialogUtil getInstance() {
        return ALERT_CHOOSE_DIALOG_UTIL;
    }

    public void show(final Context context, String title, final String upgradeKind, final OnChosedListener onChosedListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_dialog_choose, null, false);
        TextView tvOk = view.findViewById(R.id.tv_ok);
        TextView tvCancel = view.findViewById(R.id.tv_cancel);
        TextView tvTitle = view.findViewById(R.id.tv_title);
        tvTitle.setText(title);
        tvOk.setOnClickListener(v -> {
            dialog.dismiss();
            onChosedListener.onChoosed(true);
        });
        tvCancel.setOnClickListener(v -> {
            if (upgradeKind.equals("must")) {
                System.exit(0);
            } else {
                dialog.dismiss();
            }
            onChosedListener.onChoosed(false);
        });
        builder.setView(view);
        dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }

    public interface OnChosedListener {
        void onChoosed(boolean choose);
    }
}
