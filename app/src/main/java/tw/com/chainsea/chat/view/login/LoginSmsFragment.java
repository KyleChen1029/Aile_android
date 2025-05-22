package tw.com.chainsea.chat.view.login;

import static tw.com.chainsea.ce.sdk.database.DBContract.REFRESH_TIME_SOURCE.FIRST_LOAD;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.common.base.Strings;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.Timer;

import tw.com.chainsea.android.common.event.KeyboardHelper;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.ce.sdk.bean.response.AileTokenApply;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
import tw.com.chainsea.ce.sdk.http.ce.request.TokenApplyRequest;
import tw.com.chainsea.ce.sdk.http.cp.CpApiManager;
import tw.com.chainsea.ce.sdk.http.cp.base.CpApiListener;
import tw.com.chainsea.ce.sdk.http.cp.model.Configuration;
import tw.com.chainsea.ce.sdk.http.cp.model.TransTenantInfo;
import tw.com.chainsea.ce.sdk.http.cp.respone.AccountLoginCheckCodeValidateResponse;
import tw.com.chainsea.ce.sdk.http.cp.respone.AccountLoginDeviceCheckResponse;
import tw.com.chainsea.ce.sdk.http.cp.respone.LoginResponse;
import tw.com.chainsea.ce.sdk.http.cp.respone.RelationTenant;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.socket.cp.CpSocket;
import tw.com.chainsea.chat.App;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.config.SystemConfig;
import tw.com.chainsea.chat.databinding.FragmentLoginSmsBinding;
import tw.com.chainsea.chat.lib.ToastUtils;
import tw.com.chainsea.chat.service.fcm.FCMTokenManager;
import tw.com.chainsea.chat.ui.dialog.IosProgressDialog;
import tw.com.chainsea.chat.util.NoDoubleClickListener;
import tw.com.chainsea.chat.util.SystemKit;
import tw.com.chainsea.chat.view.base.HomeActivity;
import tw.com.chainsea.chat.widget.CallData;

public class LoginSmsFragment extends Fragment implements View.OnTouchListener, View.OnKeyListener {
    public static final String TAG = LoginSmsFragment.class.getSimpleName();
    private Context context;
    private FragmentLoginSmsBinding binding;
    private NavController navController;
    //    private LoginViewModel viewModel;
    private Timer timer;
    private final Handler countDownHandler = new Handler();
    int count = 0;
    private final Runnable countDownRunnable = new Runnable() {
        @Override
        public void run() {
            if (count < 60) {
                binding.txtResendTime.setText(MessageFormat.format("{0}({1}s)", getString(R.string.alert_sms_resend), SystemConfig.smsCountdownTime - count++));
                countDownHandler.postDelayed(this, 1000);
            } else {
                count = 0;
                binding.txtResendTime.setText(getString(R.string.please_enter_when_not_receive));
                binding.txtResend.setVisibility(View.VISIBLE);
            }
        }
    };

    private Editable code1;
    private Editable code2;
    private Editable code3;
    private Editable code4;
    private Editable code5;
    private Editable code6;
    private IosProgressDialog iosProgressDialog;

    private String onceToken = "";
    private String countryCode = "";
    private String phoneNumber = "";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        iosProgressDialog = new IosProgressDialog(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login_sms, container, false);
        navController = NavHostFragment.findNavController(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        viewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        getBundleArguments();
        binding.edtSms1.requestFocus();
        setEditListener();
        binding.txtAccount.setText(getString(R.string.already_send_sms, phoneNumber));
        binding.txtResend.setOnClickListener(clickListener);
        binding.btnBack.setOnClickListener(clickListener);
        SystemKit.cleanCE();
        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> KeyboardHelper.postOpen(binding.edtSms1));
        countDownHandler.post(countDownRunnable);
    }

    private void getBundleArguments() {
        if (getArguments() != null) {
            onceToken = getArguments().getString("onceToken");
            countryCode = getArguments().getString("countryCode");
            phoneNumber = getArguments().getString("phoneNumber");
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setEditListener() {
        binding.edtSms1.setSelectAllOnFocus(true);
        binding.edtSms2.setSelectAllOnFocus(true);
        binding.edtSms3.setSelectAllOnFocus(true);
        binding.edtSms4.setSelectAllOnFocus(true);
        binding.edtSms5.setSelectAllOnFocus(true);
        binding.edtSms6.setSelectAllOnFocus(true);

        binding.edtSms1.setOnTouchListener(this);
        binding.edtSms2.setOnTouchListener(this);
        binding.edtSms3.setOnTouchListener(this);
        binding.edtSms4.setOnTouchListener(this);
        binding.edtSms5.setOnTouchListener(this);
        binding.edtSms6.setOnTouchListener(this);

        binding.edtSms1.setOnKeyListener(this);
        binding.edtSms2.setOnKeyListener(this);
        binding.edtSms3.setOnKeyListener(this);
        binding.edtSms4.setOnKeyListener(this);
        binding.edtSms5.setOnKeyListener(this);
        binding.edtSms6.setOnKeyListener(this);

        binding.edtSms1.addTextChangedListener(new FocusSwitchingTextWatcher(binding.edtSms1, binding.edtSms2));
        binding.edtSms2.addTextChangedListener(new FocusSwitchingTextWatcher(binding.edtSms2, binding.edtSms3));
        binding.edtSms3.addTextChangedListener(new FocusSwitchingTextWatcher(binding.edtSms3, binding.edtSms4));
        binding.edtSms4.addTextChangedListener(new FocusSwitchingTextWatcher(binding.edtSms4, binding.edtSms5));
        binding.edtSms5.addTextChangedListener(new FocusSwitchingTextWatcher(binding.edtSms5, binding.edtSms6));
        binding.edtSms6.addTextChangedListener(new FocusSwitchingTextWatcher(binding.edtSms6, binding.edtSms6));
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (v.getId()) {
                case R.id.edt_sms_1:
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        binding.edtSms2.requestFocus();
                        break;
                    }
                    break;
                case R.id.edt_sms_2:
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        binding.edtSms3.requestFocus();
                        break;
                    }
                    if (keyCode == KeyEvent.KEYCODE_DEL && Objects.requireNonNull(binding.edtSms2.getText()).length() == 0) {
                        binding.edtSms1.setText("");
                        binding.edtSms1.requestFocus();
                        break;
                    }
                    break;
                case R.id.edt_sms_3:
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        binding.edtSms4.requestFocus();
                        break;
                    }
                    if (keyCode == KeyEvent.KEYCODE_DEL && Objects.requireNonNull(binding.edtSms3.getText()).length() == 0) {
                        binding.edtSms2.setText("");
                        binding.edtSms2.requestFocus();
                        break;
                    }
                    break;
                case R.id.edt_sms_4:
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        binding.edtSms5.requestFocus();
                        break;
                    }
                    if (keyCode == KeyEvent.KEYCODE_DEL && Objects.requireNonNull(binding.edtSms4.getText()).length() == 0) {
                        binding.edtSms3.setText("");
                        binding.edtSms3.requestFocus();
                        break;
                    }
                    break;
                case R.id.edt_sms_5:
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        binding.edtSms6.requestFocus();
                        break;
                    }
                    if (keyCode == KeyEvent.KEYCODE_DEL && Objects.requireNonNull(binding.edtSms5.getText()).length() == 0) {
                        binding.edtSms4.setText("");
                        binding.edtSms4.requestFocus();
                        break;
                    }
                    break;

                case R.id.edt_sms_6:
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        break;
                    }

                    if (keyCode == KeyEvent.KEYCODE_DEL && Objects.requireNonNull(binding.edtSms6.getText()).length() == 0) {
                        binding.edtSms5.setText("");
                        binding.edtSms5.requestFocus();
                        break;
                    }
                    break;
            }
        }

        return false;
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouch(View v, MotionEvent event) {
        v.clearFocus();
        return false;
    }

    private class FocusSwitchingTextWatcher implements TextWatcher {
        private final EditText nowViewToFocus;
        private final EditText nextViewToFocus;

        FocusSwitchingTextWatcher(EditText nowViewToFocus, EditText nextViewToFocus) {
            this.nextViewToFocus = nextViewToFocus;
            this.nowViewToFocus = nowViewToFocus;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (nowViewToFocus.equals(nextViewToFocus)) {
                return;
            }
            if (count == 1) {
                nextViewToFocus.requestFocus();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (binding.edtSms1.length() + binding.edtSms2.length() + binding.edtSms3.length()
                + binding.edtSms4.length() + binding.edtSms5.length() + binding.edtSms6.length() == 6 && isCheckCodeReady()) {
                checkCodeValidate();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
//        startSmsDeviceCheck();
        binding.txtResend.setVisibility(View.GONE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (timer != null) timer.cancel();
        KeyboardHelper.hide(this.getView());
    }

    @Override
    public void onDestroyView() {
        binding.txtResendTime.setText(null);
        countDownHandler.removeCallbacks(countDownRunnable);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //自動檢查使用者是否用簡訊連結登入
//    private void startSmsDeviceCheck(){
//        timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                loginDeviceCheck();
//            }
//        }, 5 * 1000, 10 * 1000);
//    }


    private void cleanInput() {
        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
            binding.edtSms1.setText("");
            binding.edtSms2.setText("");
            binding.edtSms3.setText("");
            binding.edtSms4.setText("");
            binding.edtSms5.setText("");
            binding.edtSms6.setText("");
            binding.edtSms1.requestFocus();
        });
    }

    private final NoDoubleClickListener clickListener = new NoDoubleClickListener() {
        @Override
        protected void onNoDoubleClick(View v) {
            if (v.equals(binding.txtResend)) {
                resendCheckCode();
                binding.txtResend.setVisibility(View.GONE);
                countDownHandler.post(countDownRunnable);
            } else if (v.equals(binding.btnBack)) {
                requireActivity().onBackPressed();
            }
        }
    };

    private void resendCheckCode() {
        CpApiManager.getInstance().login(context, countryCode, phoneNumber, new CpApiListener<>() {
            @Override
            public void onSuccess(String s) {
                LoginResponse response = JsonHelper.getInstance().from(s, LoginResponse.class);
                onceToken = response.getOnceToken();
                cleanInput();
            }

            @Override
            public void onFailed(String errorCode, String errorMessage) {
                Log.d("TAG", errorCode + errorMessage);
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> ToastUtils.showToast(context, errorMessage));
                cleanInput();
            }
        });
    }

    private boolean isCheckCodeReady() {
        code1 = binding.edtSms1.getText();
        code2 = binding.edtSms2.getText();
        code3 = binding.edtSms3.getText();
        code4 = binding.edtSms4.getText();
        code5 = binding.edtSms5.getText();
        code6 = binding.edtSms6.getText();
        if (Strings.isNullOrEmpty(code1.toString()) ||
            Strings.isNullOrEmpty(code2.toString()) ||
            Strings.isNullOrEmpty(code3.toString()) ||
            Strings.isNullOrEmpty(code4.toString()) ||
            Strings.isNullOrEmpty(code5.toString()) ||
            Strings.isNullOrEmpty(code6.toString())) {
            Toast.makeText(requireContext(), R.string.wording_please_check_code_correct, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void checkCodeValidate() {
        KeyboardHelper.hide(Objects.requireNonNull(LoginSmsFragment.this.requireView()));
        iosProgressDialog.show("登入中...");
        String checkCode = code1.toString() + code2.toString() + code3.toString() + code4.toString() + code5.toString() + code6.toString();
        CpApiManager.getInstance().loginCheckCodeValidate(context, onceToken, checkCode, new CpApiListener<>() {
            @Override
            public void onSuccess(String s) {
                AccountLoginCheckCodeValidateResponse response = JsonHelper.getInstance().from(s, AccountLoginCheckCodeValidateResponse.class);
                if (response.isValidateResult()) {
                    loginDeviceCheck();
                }
            }

            @Override
            public void onFailed(String errorCode, String errorMessage) {
                Log.d("TAG", errorCode + errorMessage);
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
                iosProgressDialog.dismiss();
                binding.edtSms6.requestFocus();
                KeyboardHelper.open(Objects.requireNonNull(LoginSmsFragment.this.requireView()));
            }
        });
    }

    private void loginDeviceCheck() {
        CpApiManager.getInstance().loginDeviceCheck(context, onceToken, new CpApiListener<>() {
            @Override
            public void onSuccess(String s) {
                AccountLoginDeviceCheckResponse response = JsonHelper.getInstance().from(s, AccountLoginDeviceCheckResponse.class);
                if ("1".equals(response.getLoginStatus())) {
                    TokenPref.getInstance(context).setCpName(response.getName());
                    TokenPref.getInstance(context).setCpAccountId(response.getAccountId());
                    TokenPref.getInstance(context).setAuthToken(response.getAuthToken());
                    Configuration configuration = response.getConfiguration();
                    TokenPref.getInstance(context)
                        .setCpRefreshTokenId(response.getRefreshTokenId())
                        .setCpTokenId(response.getTokenId())
                        .setCpSocketName(response.getName())
                        .setCpSocketDeviceId(response.getDeviceId())
                        .setCpSocketUrl(configuration.getSocketIoUrl())
//                        .setSocketAckEnable(true)
                        .setCpSocketNameSpace(configuration.getSocketIoNamespace());
                    TokenPref.getInstance(context).setCreateTenantPermission(response.getCreateTenant());
                    TokenPref.getInstance(context).setJoinTenantPermission(response.getJoinTenant());
                    CpSocket.getInstance().connect(
                        TokenPref.getInstance(context).getCpSocketUrl(),
                        TokenPref.getInstance(context).getCpSocketNameSpace(),
                        TokenPref.getInstance(context).getCpSocketName(),
                        TokenPref.getInstance(context).getCpSocketDeviceId()
                    );

                    //CP創建團隊回復
                    TransTenantInfo transTenantInfo = response.getTransTenantInfo();
                    if (transTenantInfo != null) {
                        TokenPref.getInstance(context).setCpTransTenantId(transTenantInfo.getTenantId());
                        TokenPref.getInstance(context).setCpTransTenantInfo(transTenantInfo);
                    }
                    //CP創建團隊回復-end

                    List<RelationTenant> relationTenantArray = response.getRelationTenantArray();
                    if (relationTenantArray != null && !relationTenantArray.isEmpty()) {
                        TokenPref.getInstance(context).setCpRelationTenantList(relationTenantArray);
                        boolean isLogin = false;
                        for (RelationTenant tenant : relationTenantArray) {
                            if (tenant.isLastLogin()) {
                                TokenPref.getInstance(context)
                                    .setCpCurrentTenant(tenant)
                                    .setCurrentTenantCode(tenant.getTenantCode())
                                    .setCurrentTenantUrl(tenant.getServiceUrl())
                                    .setCurrentTenantId(tenant.getTenantName());
                                loginCE(tenant.getTenantCode(), response.getAuthToken());
                                isLogin = true;
                                break;
                            }
                        }
                        if (!isLogin) {
                            RelationTenant tenant = relationTenantArray.get(0);
                            TokenPref.getInstance(context)
                                .setCpCurrentTenant(tenant)
                                .setCurrentTenantCode(tenant.getTenantCode())
                                .setCurrentTenantUrl(tenant.getServiceUrl())
                                .setCurrentTenantId(tenant.getTenantName());
                            loginCE(tenant.getTenantCode(), response.getAuthToken());
                        }
                    } else {
                        iosProgressDialog.dismiss();
                        navController.navigate(R.id.action_loginSmsFragment_to_loginCreateOrJoinFragment);
                    }
                }
            }

            @Override
            public void onFailed(String errorCode, String errorMessage) {
                iosProgressDialog.dismiss();
                Log.d("TAG", errorCode + errorMessage);
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> ToastUtils.showToast(context, errorMessage));
                cleanInput();
            }
        });
    }

    private void loginCE(String tenantCode, String authToken) {
        ApiManager.getInstance().setCEAccount(context, countryCode, phoneNumber, tenantCode, authToken);

        ApiManager.doTokenApply(context, false, new TokenApplyRequest.Listener() {
            @Override
            public void onSuccess(boolean isRefresh, AileTokenApply.Resp resp) {
                if (resp.getUser() != null) CELog.startLogSave(requireContext(), resp.getUser().getId());
                SystemKit.saveCEInfo(resp);
                SystemKit.syncApiData();

                long lastRefreshTime = DBManager.getInstance().getLastRefreshTime(FIRST_LOAD);
                Intent intent = new Intent(requireActivity(), HomeActivity.class)
                    .putExtra(BundleKey.IS_BIND_AILE.key(), resp.getUser().getIsBindAile())
                    .putExtra(BundleKey.IS_COLLECT_INFO.key(), resp.getUser().getIsCollectInfo())
                    .putExtra(BundleKey.BIND_URL.key(), resp.getUser().getBindUrl())
                    .putExtra(BundleKey.NEED_PRELOAD.key(), lastRefreshTime == 0)
                    .putExtra(BundleKey.IS_NEED_SAVE_TENANT.key(), true);
                startActivity(intent);

                requireActivity().overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
                requireActivity().finish();
                iosProgressDialog.dismiss();
            }

            @Override
            public void allCallBack(boolean isRefresh, boolean status) {
                if (isRefresh && status) {
                    FCMTokenManager.INSTANCE.refreshFCMTokenIdToRemote(context);
                }
            }

            @Override
            public void onFailed(ErrCode errorCode, String errorMessage) {
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                    iosProgressDialog.dismiss();
                    if (!TextUtils.isEmpty(errorMessage)) {
                        ToastUtils.showToast(context, errorMessage);
                    }
                });
            }

            @Override
            public void onCallData(String roomId, String meetingId, String callKey) {
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                    CallData data = new CallData();
                    data.setRoomId(roomId);
                    data.setMeetingId(meetingId);
                    data.setCallKey(callKey);
                    data.setStatus(CallData.COMMING);
                    //保存callingInfo
                    App.getInstance().saveMeetingId(roomId, data);
                });
            }
        });
    }
}
