package tw.com.chainsea.chat.account;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.common.base.Strings;

import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.http.cp.CpApiManager;
import tw.com.chainsea.ce.sdk.http.cp.base.CpApiListener;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.ActivityAccountDeleteBinding;
import tw.com.chainsea.chat.lib.ActivityManager;
import tw.com.chainsea.chat.refactor.loginPage.LoginCpActivity;
import tw.com.chainsea.chat.util.SystemKit;
import tw.com.chainsea.chat.util.ThemeHelper;
import tw.com.chainsea.custom.view.alert.AlertView;

public class AccountDeleteActivity extends AppCompatActivity {

    private Page currentPage = Page.TERMS;
    private String deleteReason;

    private enum Page {TERMS, REASON}

    private ActivityAccountDeleteBinding binding;
    private boolean isGreenTheme = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeHelper.INSTANCE.setTheme(this);
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_account_delete);
        switchView();
        isGreenTheme = ThemeHelper.INSTANCE.isGreenTheme();
    }

    private void switchView() {
        if (currentPage.equals(Page.TERMS)) {
            binding.tvBottomDesc.setText(getString(R.string.delete_account_terms_next_remind));
            binding.clTerms.setVisibility(View.VISIBLE);

            binding.rgDeleteReason.setVisibility(View.GONE);

            binding.tvNext.setOnClickListener(view -> {
                currentPage = Page.REASON;
                switchView();
            });
            binding.leftAction.setOnClickListener(this::doLeftAction);
        } else {
            binding.tvTitle.setText(getString(R.string.delete_account_reason));
            binding.tvBottomDesc.setText(getString(R.string.delete_account_reason_next_remind));
            binding.clTerms.setVisibility(View.GONE);
            binding.rgDeleteReason.setVisibility(View.VISIBLE);
            binding.tvNext.setOnClickListener(this::showDialog);
            binding.rgDeleteReason.setOnCheckedChangeListener(null);
            binding.rgDeleteReason.clearCheck();
            binding.rgDeleteReason.setOnCheckedChangeListener((radioGroup, i) -> {
                RadioButton choice = radioGroup.findViewById(i);
                deleteReason = choice.isChecked() ? setDeleteReason(choice) : null;
                checkRequireData();
            });
            checkRequireData();
            binding.leftAction.setOnClickListener(view -> backToTerms());
        }
    }

    private void backToTerms() {
        currentPage = Page.TERMS;
        deleteReason = null;
        switchView();
    }

    private String setDeleteReason(RadioButton choice) {
        String reason = "";
        if (choice.equals(binding.radio1)) {
            reason = "leaveTenant";
        } else if (choice.equals(binding.radio2)) {
            reason = "noLongerLogin";
        } else if (choice.equals(binding.radio3)) {
            reason = "interfaceUnfriendly";
        } else if (choice.equals(binding.radio4)) {
            reason = "resourceOverload";
        }
        return reason;
    }

    private void doLeftAction(View v) {
        finish();
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
    }

    private void checkRequireData() {
        binding.tvNext.setClickable(!Strings.isNullOrEmpty(deleteReason));
    }

    private void showDialog(View v) {
        new AlertView.Builder()
            .setContext(this)
            .setStyle(AlertView.Style.Alert)
            .setMessage(getString(isGreenTheme ? R.string.text_setting_delete_account_content_for_green : R.string.text_setting_delete_account_content))
            .setOthers(new String[]{getString(R.string.alert_cancel), getString(R.string.alert_confirm)})
            .setOnItemClickListener((o, position) -> {
                if (position == 1) {
                    delete();
                }
            })
            .build()
            .setCancelable(true)
            .setOnDismissListener(null)
            .show();
    }

    private void delete() {
        if (Strings.isNullOrEmpty(deleteReason)) {
            return;
        }

        CpApiManager.getInstance().delete(this, deleteReason, new CpApiListener<>() {
            @Override
            public void onSuccess(String s) {
                TokenPref.getInstance(AccountDeleteActivity.this).clearByKey(TokenPref.PreferencesKey.THEME_ITEM);
                ActivityManager.finishBaseActivity();
                ActivityManager.finishAll();
                SystemKit.cleanCE();
                SystemKit.cleanCP();
                Intent intent = new Intent(AccountDeleteActivity.this, LoginCpActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                AccountDeleteActivity.this.finish();
            }

            @Override
            public void onFailed(String errorCode, String errorMessage) {
                Log.d("TAG", errorCode + errorMessage);
                Toast.makeText(AccountDeleteActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
