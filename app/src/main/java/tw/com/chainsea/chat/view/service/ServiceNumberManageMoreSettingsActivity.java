package tw.com.chainsea.chat.view.service;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Window;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.util.List;

import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity;
import tw.com.chainsea.ce.sdk.network.model.response.DictionaryItems;
import tw.com.chainsea.ce.sdk.service.ChatServiceNumberService;
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.config.SystemConfig;
import tw.com.chainsea.chat.databinding.ActivityServiceNumberManageMoreSettingsBinding;
import tw.com.chainsea.chat.databinding.DialogServiceNumberTimeOutBinding;
import tw.com.chainsea.chat.network.contact.ViewModelFactory;
import tw.com.chainsea.chat.ui.dialog.IosProgressDialog;
import tw.com.chainsea.chat.util.IntentUtil;
import tw.com.chainsea.chat.util.SystemKit;
import tw.com.chainsea.chat.view.homepage.CustomItemDecoration;
import tw.com.chainsea.chat.view.homepage.ServiceNumberTimeOutAdapter;
import tw.com.chainsea.chat.view.homepage.WelcomeMessageSettingFragment;
import tw.com.chainsea.chat.view.homepage.bind.BindThirdPartActivity;
import tw.com.chainsea.chat.view.service.adapter.ManageMoreSettingsAdapter;
import tw.com.chainsea.chat.view.service.bean.MoreSettingsBean;
import tw.com.chainsea.chat.view.service.bean.MoreSettingsType;

public class ServiceNumberManageMoreSettingsActivity extends AppCompatActivity implements ManageMoreSettingsAdapter.OnMoreSettingsItemClickListener<MoreSettingsBean> {
    private ActivityServiceNumberManageMoreSettingsBinding binding;

    private ServiceNumberEntity serviceNumberEntity;

    String serviceNumberId = "";

    private ServiceNumberHomePageViewModel serviceNumberHomePageViewModel;

    private Dialog serviceNumberTimeOutDialog;

    private IosProgressDialog iosProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_service_number_manage_more_settings);
        getWindow().setStatusBarColor(0xFF6BC2BA);
        initViewModel();
        observeData();
        getData();
    }

    private void initViewModel() {
        ViewModelFactory viewModelFactory = new ViewModelFactory(getApplication());
        serviceNumberHomePageViewModel = new ViewModelProvider(this, viewModelFactory).get(ServiceNumberHomePageViewModel.class);
    }

    private void getData() {
        serviceNumberHomePageViewModel.getServiceTimeOutList();
    }

    private void observeData() {
        serviceNumberHomePageViewModel.getTimeList().observe(this, this::createDialog);

        serviceNumberHomePageViewModel.getOnUpdateServiceNumberTimeoutTime().observe(this, isSuccess -> {
            if (isSuccess) {
                refreshData(RefreshSource.LOCAL);
                dismissIosDialog();
                Toast.makeText(this, "更新成功", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initList(ServiceNumberEntity entity) {
        ManageMoreSettingsAdapter adapter = new ManageMoreSettingsAdapter(this);
        List<MoreSettingsBean> moreSettingsBeans = getBeans(entity);
        adapter.setData(moreSettingsBeans);
        adapter.setEntity(entity);

        binding.rvSettings.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSettings.setAdapter(adapter);
        binding.leftAction.setOnClickListener(v -> finish());
    }

    private List<MoreSettingsBean> getBeans(ServiceNumberEntity entity) {
        List<MoreSettingsBean> moreSettingsBeans = Lists.newArrayList();
        moreSettingsBeans.add(new MoreSettingsBean("", MoreSettingsType.SERVICE_INFO));

//        moreSettingsBeans.add(MoreSettingsBean.Build().title("請輸入服務號描述").type(MoreSettingsType.DEPICTION_INPUT).build());

        moreSettingsBeans.add(new MoreSettingsBean("啟用服務號", MoreSettingsType.ENABLE));
        moreSettingsBeans.add(new MoreSettingsBean("查看所有成員", MoreSettingsType.MEMBERS));
        //群發隱藏, 等新版本完成後在開啟
        if (SystemConfig.enableBroadcast) {
            moreSettingsBeans.add(new MoreSettingsBean("群發訊息", MoreSettingsType.BROADCAST_MESSAGE));
        }

//        moreSettingsBeans.add(MoreSettingsBean.Build().title("升級成專業服務號").type(MoreSettingsType.UPGRADE_PROFESSIONAL).build());
//        moreSettingsBeans.add(MoreSettingsBean.Build().title("內部服務號").type(MoreSettingsType.INSIDE_SERVICE_NUMBER).build());
//        moreSettingsBeans.add(MoreSettingsBean.Build().title("外部服務號").type(MoreSettingsType.OUTSIDE_SERVICE_NUMBER).build());
        moreSettingsBeans.add(new MoreSettingsBean(getString(R.string.service_number_welcome_message_setting), MoreSettingsType.WELCOME_MESSAGE));
        //內部服務號不用有
        if (!entity.getServiceOpenType().contains("I")) {
            moreSettingsBeans.add(new MoreSettingsBean(getString(R.string.service_number_connection_setting), MoreSettingsType.POST_BACK_CALLBACK));
        }

        moreSettingsBeans.add(new MoreSettingsBean(getString(R.string.service_number_connection_timeout), MoreSettingsType.TIMEOUT_SETTING));
        return moreSettingsBeans;
    }

    @Override
    public void onItemClick(MoreSettingsType type, MoreSettingsBean moreSettingsBean) {
        switch (type) {
            case BROADCAST_MESSAGE:
                String name = getIntent().getStringExtra(BundleKey.TITLE.key());
                String broadcastRoomId = getIntent().getStringExtra(BundleKey.BROADCAST_ROOM_ID.key());

                SystemKit.openServiceNumberBroadcast(this, name, broadcastRoomId, serviceNumberId);
                break;
            case WELCOME_MESSAGE:
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.slide_left_in, R.anim.slide_left_out, R.anim.slide_right_in, R.anim.slide_right_out)
                    .add(R.id.fl_self, WelcomeMessageSettingFragment.newInstance(serviceNumberId, false))
                    .addToBackStack(null).commit();
                break;
            case POST_BACK_CALLBACK:
                Bundle bundle = new Bundle();
                bundle.putString(BundleKey.SERVICE_NUMBER_ID.name(), serviceNumberId);
                IntentUtil.INSTANCE.startIntent(this, BindThirdPartActivity.class, bundle);
                break;
            case TIMEOUT_SETTING:
                showTimeOutDialog();
                break;
        }
    }

    private void createDialog(List<DictionaryItems> list) {
        serviceNumberTimeOutDialog = new Dialog(this);
        DialogServiceNumberTimeOutBinding dialogServiceNumberTimeOutBinding = DialogServiceNumberTimeOutBinding.inflate(LayoutInflater.from(this));
        serviceNumberTimeOutDialog.setContentView(dialogServiceNumberTimeOutBinding.getRoot());
        ServiceNumberTimeOutAdapter serviceNumberTimeOutAdapter = new ServiceNumberTimeOutAdapter(list, time -> {
            serviceNumberHomePageViewModel.updateServiceNumberTimeoutTime(serviceNumberId, time);
            serviceNumberTimeOutDialog.dismiss();
            showIosDialog();
            return null;
        });
        CustomItemDecoration divider = new CustomItemDecoration(this);
        dialogServiceNumberTimeOutBinding.rvTimeList.addItemDecoration(divider);
        dialogServiceNumberTimeOutBinding.rvTimeList.setLayoutManager(new LinearLayoutManager(this));
        dialogServiceNumberTimeOutBinding.rvTimeList.setAdapter(serviceNumberTimeOutAdapter);
        dialogServiceNumberTimeOutBinding.tvTitle.setText(getString(R.string.text_dialog_time_out_title));
        dialogServiceNumberTimeOutBinding.tvCancel.setOnClickListener(v -> {
            if (serviceNumberTimeOutDialog.isShowing()) serviceNumberTimeOutDialog.dismiss();
        });
    }

    private void showTimeOutDialog() {
        if (serviceNumberTimeOutDialog != null) {
            if (!isFinishing() && !isDestroyed()) {
                serviceNumberTimeOutDialog.show();
                Window window = serviceNumberTimeOutDialog.getWindow();
                if (window != null) {
                    window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                }
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        refreshData(RefreshSource.REMOTE);
    }

    private void refreshData(RefreshSource source) {
        serviceNumberId = getIntent().getStringExtra(BundleKey.SERVICE_NUMBER_ID.key());
        ChatServiceNumberService.findServiceNumber(this, serviceNumberId, source, new ServiceCallBack<ServiceNumberEntity, RefreshSource>() {
            @Override
            public void complete(ServiceNumberEntity entity, RefreshSource source) {
                if (entity != null && !Strings.isNullOrEmpty(entity.getBroadcastRoomId())) {
                    getIntent().putExtra(BundleKey.BROADCAST_ROOM_ID.key(), entity.getBroadcastRoomId());
                }
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                    initList(entity);
                    serviceNumberEntity = entity;
                });
            }

            @Override
            public void error(String message) {
            }
        });
    }

    private void showIosDialog() {
        if (!isFinishing() && !isDestroyed())
            if (iosProgressDialog == null) iosProgressDialog = new IosProgressDialog(this);
        iosProgressDialog.show();
    }

    private void dismissIosDialog() {
        if (iosProgressDialog != null) {
            iosProgressDialog.dismiss();
        }
    }
}
