package tw.com.chainsea.chat.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.ViewModelProvider;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.ce.sdk.event.EventMessage;
import tw.com.chainsea.ce.sdk.event.SocketEvent;
import tw.com.chainsea.ce.sdk.http.cp.CpApiManager;
import tw.com.chainsea.ce.sdk.http.cp.base.CpApiListener;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.network.model.common.ErrorCode;
import tw.com.chainsea.ce.sdk.socket.ce.SocketManager;
import tw.com.chainsea.ce.sdk.socket.cp.CpSocket;
import tw.com.chainsea.ce.sdk.socket.cp.model.DeviceLoginContent;
import tw.com.chainsea.ce.sdk.socket.cp.model.GuarantorJoinContent;
import tw.com.chainsea.ce.sdk.socket.cp.model.SqueezedOutContent;
import tw.com.chainsea.chat.App;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.network.contact.ViewModelFactory;
import tw.com.chainsea.chat.network.tenant.TenantViewModel;
import tw.com.chainsea.chat.refactor.loginPage.LoginCpActivity;
import tw.com.chainsea.chat.ui.dialog.MessageDialogBuilder;
import tw.com.chainsea.chat.util.IntentUtil;
import tw.com.chainsea.chat.util.SystemKit;
import tw.com.chainsea.chat.util.ThemeHelper;
import tw.com.chainsea.chat.view.base.viewmodel.HomeViewModel;
import tw.com.chainsea.chat.view.login.LoginOtherDeviceActivity;
import tw.com.chainsea.custom.view.alert.AlertView;

public abstract class BaseActivity extends AppCompatActivity implements LifecycleObserver {
    protected CpSocket cpSocket;

    protected TenantViewModel tenantViewModel;

    protected HomeViewModel homeViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeHelper.INSTANCE.setTheme(this);
        super.onCreate(savedInstanceState);
        getLifecycle().addObserver(this);
        EventBus.getDefault().register(this);
        initViewModel();
        observeData();
        cpSocket = CpSocket.getInstance();
        IntentUtil.INSTANCE.resetClickTime();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    //從 socket 來的訊息
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SocketEvent socketEvent) {
        switch (socketEvent.getType()) {
            case ForceLogout:
                CpSocket.getInstance().disconnect();
                SocketManager.disconnect();
                //CP收到被強登時處理
                showForceLogoutDialog(getString(R.string.text_you_are_forced_logout));
                break;
            case SqueezedOut:
                CpSocket.getInstance().disconnect();
                SocketManager.disconnect();
                //CP收到被搶登時處理
                SqueezedOutContent squeezedOutContent = (SqueezedOutContent) socketEvent.getData();
                showOtherDeviceLogin(squeezedOutContent);
                break;
            case DeviceLogin:
                //CP收到需要確認裝置登入處理
                DeviceLoginContent deviceLoginContent = (DeviceLoginContent) socketEvent.getData();
                onReceiveDeviceLogin(deviceLoginContent);
                break;
        }
    }

    //API收到需要強制登出時處理
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventMessage eventMessage) {
        String message = eventMessage.getMessage();
        if (ErrCode.CP_REFRESH_TOKEN_NOT_EXIST.getValue().equals(message)
            || ErrCode.CP_REFRESH_TOKEN_EXPIRED.getValue().equals(message)) {
            showForceLogoutDialog(getString(R.string.invalid_token));
        } else if (ErrCode.CP_SQUEEZED_OUT.getValue().equals(message)) {
            showForceLogoutDialog(getString(R.string.text_you_are_forced_logout));
        }
    }

    //CP收到需要確認裝置登入處理
    public void onReceiveDeviceLogin(DeviceLoginContent deviceLoginContent) {
        Intent intent = new Intent();
        intent.putExtra(BundleKey.ONCE_TOKEN.key(), deviceLoginContent.getOnceToken());
        intent.putExtra(BundleKey.DEVICE_NAME.key(), deviceLoginContent.getDeviceName());
        intent.setClass(this, LoginOtherDeviceActivity.class);
        startActivity(intent);
    }

    //CP收到被搶登時處理
    public void showOtherDeviceLogin(SqueezedOutContent squeezedOutContent) {
        String message = (squeezedOutContent.getDeviceName() != null && !squeezedOutContent.getDeviceName().isEmpty())
            ? getString(R.string.text_your_account_has_been_login_tip, squeezedOutContent.getDeviceName()) : getString(R.string.text_your_account_has_been_login_by_other_device);
        showForceLogoutDialog(message);
    }

    private void observeData() {
        tenantViewModel.getAgreeToBeGuarantor().observe(this, isAgree -> {
            if (isAgree) {
                Toast.makeText(BaseActivity.this, getString(R.string.text_agree_member_to_join), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(BaseActivity.this, getString(R.string.text_reject_member_to_join), Toast.LENGTH_SHORT).show();
            }
        });

        homeViewModel.getRefreshTokenError().observe(this, error -> {
            if (ErrorCode.of(error.getErrorCode()) == ErrorCode.DeviceNotExist) {
                if (!"您尚未登入，請先登入".equals(error.getErrorMessage())) {
                    Intent intent = new Intent(this, LoginCpActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putBoolean(BundleKey.TO_CREATE_OR_JOIN_TENANT.name(), true);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    finish();
                } else {
                    SystemKit.logoutToLoginPage();
                }
            } else {
                showForceLogoutDialog(error.getErrorMessage());
            }
        });

        tenantViewModel.getErrorMessage().observe(this, errorMessage -> {
//            Toast.makeText(BaseActivity.this, "系統異常，請稍後再試", Toast.LENGTH_SHORT).show();
        });
    }

    private void initViewModel() {
        ViewModelFactory viewModelFactory = new ViewModelFactory(getApplication());
        homeViewModel = new ViewModelProvider(this, viewModelFactory).get(HomeViewModel.class);
        tenantViewModel = new ViewModelProvider(this, viewModelFactory).get(TenantViewModel.class);
    }

    //缺少擔保人流程 打的是 CE API
    public void reGuarantorJoin(GuarantorJoinContent content) {
        ThreadExecutorHelper.getMainThreadExecutor().execute(() ->
            new MessageDialogBuilder(App.getInstance().currentActivity())
                .setTitle(getString(R.string.text_joining_team))
                .setMessage(getString(R.string.text_join_tenant_tip, content.getName(), content.getTenantName()))
                .setShowLevel(1)
                .setConfirmText(getString(R.string.text_agree))
                .setOnConfirmListener(message -> {
                    tenantViewModel.tenantGuarantorJoin(content.getUserId());
                })
                .setCancelText(getString(R.string.text_disagree))
                .setOnCancelListener(message -> {
                    tenantViewModel.tenantGuarantorReject(content.getUserId());
                })
                .create().show());
    }


    public void guarantorJoin(GuarantorJoinContent content) {
        ThreadExecutorHelper.getMainThreadExecutor().execute(() ->
            new MessageDialogBuilder(App.getInstance().currentActivity())
                .setTitle(getString(R.string.text_joining_team))
                .setMessage(getString(R.string.text_join_tenant_tip, content.getName(), content.getTenantName()))
                .setShowLevel(1)
                .setConfirmText(getString(R.string.text_agree))
                .setOnConfirmListener(message -> {
                    CpApiManager.getInstance().tenantGuarantorAgree(this, content.getOnceToken(),
                        new CpApiListener<String>() {
                            @Override
                            public void onSuccess(String s) {
                                Toast.makeText(BaseActivity.this, "同意成員加入", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailed(String errorCode, String errorMessage) {
                                if (ErrCode.TENANT_USER_IS_ALREADY.getValue().equals(errorCode)) {
                                    Toast.makeText(BaseActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                })
                .setCancelText(getString(R.string.text_disagree))
                .setOnCancelListener(message -> {
                    CpApiManager.getInstance().tenantGuarantorReject(this, content.getOnceToken(),
                        new CpApiListener<String>() {
                            @Override
                            public void onSuccess(String s) {
                                Toast.makeText(BaseActivity.this, "拒絕成員加入", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailed(String errorCode, String errorMessage) {
                                if (ErrCode.GUARANTOR_NOT_JOIN.getValue().equals(errorCode)) {
                                    Toast.makeText(BaseActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                } else if (ErrCode.GUARANTOR_ALREADY_AGREE.getValue().equals(errorCode)) {
                                    Toast.makeText(BaseActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                })
                .create()
                .show()
        );
    }

    protected void showForceLogoutDialog(String content) {
        ThreadExecutorHelper.getMainThreadExecutor().execute(() ->
            new AlertView.Builder()
                .setContext(this)
                .setStyle(AlertView.Style.Alert)
                .setTitle(content)
                .setOthers(new String[]{getString(R.string.text_for_sure)})
                .setOnItemClickListener((o, position) -> {
                    if (position == 0) {
                        SystemKit.logoutToLoginPage();
                    }
                })
                .build()
                .setCancelable(false)
                .show()
        );
    }
}
