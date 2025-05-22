package tw.com.chainsea.chat.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;

import java.text.MessageFormat;

import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.DialogTransferBinding;

public class TransferDialogBuilder {
    public interface OnSubmitListener{
        void OnSubmitClick(String message);
    }
    private OnSubmitListener onSubmitListener;
    private final Context context;
    private final String[] messages = {
      "此專業我不熟悉，請同事接手",
      "給之前互動的同事接手",
      "我有事必須離席",
      "客戶抱怨，需要支援",
      "忙錄中，需要同事接手服務"
    };

    public TransferDialogBuilder(Context context) {
        this.context = context;
    }

    public TransferDialogBuilder setOnSubmitListener(OnSubmitListener onSubmitListener){
        this.onSubmitListener = onSubmitListener;
        return this;
    }

    public Dialog create(){
        LayoutInflater inflater = LayoutInflater.from(context);
        DialogTransferBinding binding;
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_transfer, null, false);

        for (int i = 0; i < messages.length; i++) {
            RadioButton button = (RadioButton) inflater.inflate(R.layout.radiobutton_transfer, binding.radioGroup, false);
            button.setId(i);
            button.setText(messages[i]);
            button.setChecked(i == 0);
            button.setOnClickListener(v -> binding.edtMessage.clearFocus());
            binding.radioGroup.addView(button);
        }
        binding.edtMessage.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus) binding.radioGroup.clearCheck();
        });
        binding.edtMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                binding.txtInputCount.setText(MessageFormat.format("{0}/50", s.length()));
            }
        });
        binding.btnSubmit.setOnClickListener(v -> {
            Editable message = binding.edtMessage.getText();
            if(onSubmitListener != null) {
                int select = binding.radioGroup.getCheckedRadioButtonId();
                if (select != -1) {
                    RadioButton button = binding.root.findViewById(select);
                    onSubmitListener.OnSubmitClick(button.getText().toString());
                }
                else if (message != null) {
                    if (message.toString().trim().isEmpty()) {
                        Toast.makeText(context, "請輸入文字", Toast.LENGTH_SHORT).show();
                    } else {
                        onSubmitListener.OnSubmitClick(message.toString());
                    }
                }
            }
        });
        Dialog dialog = new AlertDialog.Builder(context).setView(binding.getRoot()).create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }
}