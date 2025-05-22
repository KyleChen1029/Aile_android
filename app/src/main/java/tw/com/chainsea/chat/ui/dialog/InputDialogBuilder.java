package tw.com.chainsea.chat.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;

import java.text.MessageFormat;

import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.DialogInputBinding;

public class InputDialogBuilder {
    public interface OnConfirmListener {
        void OnConfirmClick(String message);
    }

    private OnConfirmListener onConfirmListener;
    private final Context context;
    private int maxLength = 20;
    private String hint = "";
    private String title;
    private String defaultString = "";
    private Boolean isCanEmpty = false;
    private String toastMessage = "請輸入文字";
    private OnConfirmListener onCustomConfirmListener;

    public InputDialogBuilder(Context context) {
        this.context = context;
    }

    public InputDialogBuilder setOnConfirmListener(OnConfirmListener onConfirmListener) {
        this.onConfirmListener = onConfirmListener;
        return this;
    }

    public InputDialogBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public InputDialogBuilder setMaxLength(int maxLength) {
        this.maxLength = maxLength;
        return this;
    }

    public InputDialogBuilder setInputData(String defaultString) {
        this.defaultString = defaultString;
        return this;
    }

    public InputDialogBuilder setHint(String hint) {
        this.hint = hint;
        return this;
    }

    public InputDialogBuilder setIsCanEmpty(Boolean isCanEmpty) {
        this.isCanEmpty = isCanEmpty;
        return this;
    }

    public InputDialogBuilder setToastMessage(String toastMessage) {
        this.toastMessage = toastMessage;
        return this;
    }

    public InputDialogBuilder setCustomConfirmClickListener(OnConfirmListener clickListener) {
        this.onCustomConfirmListener = clickListener;
        return this;
    }

    public Dialog create() {
        LayoutInflater inflater = LayoutInflater.from(context);
        DialogInputBinding binding;
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_input, null, false);

        Dialog dialog = new AlertDialog.Builder(context).setView(binding.getRoot()).create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        //設定最大長度
        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(maxLength);
        binding.edtMessage.setFilters(FilterArray);
        binding.edtMessage.requestFocus();

        binding.txtTitle.setText(title);
        binding.txtInputCount.setText(MessageFormat.format("{0}/" + maxLength, defaultString.length()));
        binding.edtMessage.setHint(hint);
        binding.edtMessage.setText(defaultString);
        binding.edtMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                binding.txtInputCount.setText(MessageFormat.format("{0}/" + maxLength, s.length()));
            }
        });
        if (onCustomConfirmListener != null) {
            binding.btnConfirm.setOnClickListener(v -> {
                Editable message = binding.edtMessage.getText();
                if (message != null) {
                    onCustomConfirmListener.OnConfirmClick(message.toString());
                }
            });
        } else {
            binding.btnConfirm.setOnClickListener(v -> {
                Editable message = binding.edtMessage.getText();
                if (onConfirmListener != null) {
                    if (message != null) {
                        if (message.toString().trim().isEmpty() && !isCanEmpty) {
                            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();
                        } else {
                            onConfirmListener.OnConfirmClick(message.toString());
                            dialog.dismiss();
                        }
                    }
                }
            });
        }
        binding.btnCancel.setOnClickListener(v -> dialog.dismiss());
        return dialog;
    }
}
