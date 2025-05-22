package tw.com.chainsea.chat.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.databinding.DataBindingUtil;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.BottomSheetSettingBinding;
import tw.com.chainsea.android.common.log.CELog;

public class SettingDialogBuilder {
    private final Context context;
    private final UserProfileEntity profileEntity;

    public SettingDialogBuilder(Context context, UserProfileEntity profileEntity){
        this.context = context;
        this.profileEntity = profileEntity;
    }

    public Dialog create(){
        BottomSheetDialog dialog = new BottomSheetDialog(context, R.style.bottom_dialog);
        BottomSheetSettingBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.bottom_sheet_setting, null, false);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        binding.getRoot().setLayoutParams(params);
        binding.btnComplaint.setOnClickListener(v-> dialog.dismiss());
        binding.btnBlock.setText(profileEntity.isBlock() ? "解除封鎖" : "封鎖");
        binding.btnBlock.setOnClickListener(v -> {
            binding.btnBlock.setText(!profileEntity.isBlock() ? "解除封鎖" : "封鎖");
            block();
        });
        binding.btnCancel.setOnClickListener(v-> dialog.dismiss());
        dialog.setContentView(binding.getRoot());
        return dialog;
    }

    private void block(){
        String id = profileEntity.getId();
        boolean isBlock = !profileEntity.isBlock();
        ApiManager.doUserBlock(context, id, isBlock, new ApiListener<String>() {
            @Override
            public void onSuccess(String s) {
                DBManager.getInstance().setFriendBlock(id, isBlock);
                profileEntity.setBlock(isBlock);
            }

            @Override
            public void onFailed(String errorMessage) {
                CELog.e(errorMessage);
            }
        });
    }
}
