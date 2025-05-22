package tw.com.chainsea.chat.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;

import androidx.databinding.DataBindingUtil;

import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.DialogMessageBinding;

public class MessageDialogBuilder {
    public interface OnConfirmListener {
        void OnConfirmClick(String message);
    }

    public interface OnCancelListener {
        void OnCancelClick(String message);
    }

    private OnConfirmListener onConfirmListener;
    private OnCancelListener onCancelListener;
    private final Context context;
    private String title;
    private String message;
    private String confirmText;
    private String cancelText;
    private int showLevel;

    public MessageDialogBuilder(Context context) {
        this.context = context;
    }

    public MessageDialogBuilder setOnConfirmListener(OnConfirmListener onConfirmListener) {
        this.onConfirmListener = onConfirmListener;
        return this;
    }

    public MessageDialogBuilder setOnCancelListener(OnCancelListener onCancelListener) {
        this.onCancelListener = onCancelListener;
        return this;
    }

    public MessageDialogBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public MessageDialogBuilder setMessage(String message) {
        this.message = message;
        return this;
    }

    public MessageDialogBuilder setConfirmText(String confirmText) {
        this.confirmText = confirmText;
        return this;
    }

    public MessageDialogBuilder setCancelText(String cancelText) {
        this.cancelText = cancelText;
        return this;
    }

    public MessageDialogBuilder setShowLevel(int showLevel) {
        this.showLevel = showLevel;
        return this;
    }

    public Dialog create() {
        LayoutInflater inflater = LayoutInflater.from(context);
        DialogMessageBinding binding = DataBindingUtil.inflate(inflater, R.layout.dialog_message, null, false);

        Dialog dialog = new Dialog(context);
        dialog.setContentView(binding.getRoot());
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        if (showLevel == 1) {
            Window window = dialog.getWindow();
            WindowManager.LayoutParams wl = window.getAttributes();
            wl.x = 0;
            wl.y = 0;
            wl.gravity = Gravity.CENTER;
            // 设置显示位置
            dialog.onWindowAttributesChanged(wl);
        }
        dialog.setCancelable(false);

        binding.txtTitle.setText(title);
        binding.txtMessage.setText(message);

        if (confirmText != null) binding.btnConfirm.setText(confirmText);
        if (cancelText != null) binding.btnCancel.setText(cancelText);

        binding.btnConfirm.setOnClickListener(v -> {
            if (onConfirmListener != null) {
                onConfirmListener.OnConfirmClick(message);
            }
            dialog.dismiss();
        });
        binding.btnCancel.setOnClickListener(v -> {
            if (onCancelListener != null) {
                onCancelListener.OnCancelClick(message);
            }
            dialog.dismiss();
        });
        return dialog;
    }
}
