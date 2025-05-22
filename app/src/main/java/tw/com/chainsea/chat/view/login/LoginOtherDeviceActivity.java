package tw.com.chainsea.chat.view.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import java.text.MessageFormat;

import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.ce.sdk.http.cp.CpApiManager;
import tw.com.chainsea.ce.sdk.http.cp.base.CpApiListener;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.databinding.ActivityLoginOtherDeviceBinding;
import tw.com.chainsea.chat.lib.ToastUtils;
import tw.com.chainsea.chat.util.NoDoubleClickListener;
import tw.com.chainsea.chat.view.BaseActivity;

public class LoginOtherDeviceActivity extends BaseActivity {
    public static final String TAG = LoginOtherDeviceActivity.class.getSimpleName();
    private ActivityLoginOtherDeviceBinding binding;
    private String onceToken;
    private String device = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login_other_device);
        binding.btnClose.setOnClickListener(clickListener);
        binding.btnLogin.setOnClickListener(clickListener);
        binding.btnRefusedLogin.setOnClickListener(clickListener);

        Intent intent = getIntent();
        if (intent != null) {
            onceToken = intent.getStringExtra(BundleKey.ONCE_TOKEN.key());
            device = intent.getStringExtra(BundleKey.DEVICE_NAME.key());
        }
        binding.txtDevice.setText(MessageFormat.format("{0}{1}{2}", getString(R.string.other_device_hint_one), device, getString(R.string.other_device_hint_two)));
    }

    private final NoDoubleClickListener clickListener = new NoDoubleClickListener() {
        @Override
        protected void onNoDoubleClick(View v) {
            if (v.equals(binding.btnClose)) {
                finish();
            } else if (v.equals(binding.btnLogin)) {
                agreeDeviceLogin();
            } else if (v.equals(binding.btnRefusedLogin)) {
                rejectDeviceLogin();
            }
        }
    };

    private void agreeDeviceLogin() {
        CpApiManager.getInstance().loginDeviceAgree(this, onceToken, binding.cbAutoLogin.isChecked(), new CpApiListener<String>() {
            @Override
            public void onSuccess(String s) {
                finish();
            }

            @Override
            public void onFailed(String errorCode, String errorMessage) {
                Log.d("TAG", errorCode + errorMessage);
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> ToastUtils.showToast(LoginOtherDeviceActivity.this, errorMessage));
            }
        });
    }

    private void rejectDeviceLogin() {
        CpApiManager.getInstance().loginDeviceReject(this, onceToken, new CpApiListener<String>() {
            @Override
            public void onSuccess(String s) {
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> ToastUtils.showToast(LoginOtherDeviceActivity.this, "已拒絕「" + device + "」登入"));
                finish();
            }

            @Override
            public void onFailed(String errorCode, String errorMessage) {
                Log.d("TAG", errorCode + errorMessage);
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> ToastUtils.showToast(LoginOtherDeviceActivity.this, errorMessage));
            }
        });
    }
}
