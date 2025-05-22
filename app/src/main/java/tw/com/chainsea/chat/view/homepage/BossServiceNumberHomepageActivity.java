package tw.com.chainsea.chat.view.homepage;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.common.base.Strings;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.ce.sdk.bean.CustomerEntity;
import tw.com.chainsea.ce.sdk.bean.PicSize;
import tw.com.chainsea.ce.sdk.bean.account.UserType;
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.network.model.response.DictionaryItems;
import tw.com.chainsea.ce.sdk.service.AvatarService;
import tw.com.chainsea.ce.sdk.service.ChatServiceNumberService;
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.config.SystemConfig;
import tw.com.chainsea.chat.databinding.ActivityBossServicenumberHomepageBinding;
import tw.com.chainsea.chat.databinding.DialogServiceNumberTimeOutBinding;
import tw.com.chainsea.chat.network.contact.ViewModelFactory;
import tw.com.chainsea.chat.service.ActivityTransitionsControl;
import tw.com.chainsea.chat.ui.dialog.IosProgressDialog;
import tw.com.chainsea.chat.util.IntentUtil;
import tw.com.chainsea.chat.util.NoDoubleClickListener;
import tw.com.chainsea.chat.util.SystemKit;
import tw.com.chainsea.chat.view.homepage.bind.BindThirdPartActivity;
import tw.com.chainsea.chat.view.homepage.viewmodel.ServiceNumberViewModel;
import tw.com.chainsea.chat.view.roomAction.adapter.MemberAdapter;
import tw.com.chainsea.chat.view.service.ServiceNumberAgentsManageViewModel;
import tw.com.chainsea.custom.view.alert.AlertView;
import tw.com.chainsea.custom.view.alert.OnItemClickListener;

public class BossServiceNumberHomepageActivity extends BaseHomepageActivity {
    private ActivityBossServicenumberHomepageBinding binding;
    private MemberAdapter memberAdapter;
    private String name;
    private String broadcastRoomId;
    private String serviceNumberId;
    private ServiceNumberViewModel viewModel;
    private ServiceNumberEntity serviceNumberEntity;

    private ServiceNumberAgentsManageViewModel serviceViewModel;
    private Dialog serviceNumberTimeOutDialog;
    private IosProgressDialog iosProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.TRANSPARENT);

        Intent intent = getIntent();
        if (intent != null) {
            name = intent.getStringExtra(BundleKey.TITLE.key());
            broadcastRoomId = intent.getStringExtra(BundleKey.BROADCAST_ROOM_ID.key());
            serviceNumberId = intent.getStringExtra(BundleKey.SERVICE_NUMBER_ID.key());
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_boss_servicenumber_homepage);


        binding.btnExit.setOnClickListener(clickListener);
        binding.layoutBroadcast.setOnClickListener(clickListener);
        binding.txtMemberAll.setOnClickListener(clickListener);
        binding.txtDesc.setOnClickListener(clickListener);
        binding.layoutCustomer.setOnClickListener(clickListener);
        binding.layoutWelcomeMessage.setOnClickListener(clickListener);
        binding.clServiceNumberTimeoutSetting.setOnClickListener(clickListener);

        binding.rvMemberList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvMemberList.setHasFixedSize(true);
        binding.rvMemberList.setItemAnimator(new DefaultItemAnimator());
        memberAdapter = new MemberAdapter();
        binding.rvMemberList.setAdapter(memberAdapter);

        refreshData(RefreshSource.REMOTE);

        getServiceNumberContactList();

        binding.layoutBindOutsideGateway.setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            bundle.putString(BundleKey.SERVICE_NUMBER_ID.name(), serviceNumberId);
            IntentUtil.INSTANCE.startIntent(this, BindThirdPartActivity.class, bundle);
        });

        initViewModel();

        observeData();
        getData();
    }

    private void initViewModel() {
        ViewModelFactory serviceNumberAgentsManageRepository = new ViewModelFactory(getApplication());
        serviceViewModel = new ViewModelProvider(this, serviceNumberAgentsManageRepository).get(ServiceNumberAgentsManageViewModel.class);
        viewModel = new ViewModelProvider(this).get(ServiceNumberViewModel.class);
    }

    private void observeData() {
        serviceViewModel.isTenantManager().observe(this, aBoolean -> {
            binding.layoutBindOutsideGateway.setVisibility(serviceNumberEntity.isManager() || serviceNumberEntity.isOwner() || aBoolean ? View.VISIBLE : View.GONE);
            binding.bottomDivide.setVisibility(serviceNumberEntity.isManager() || serviceNumberEntity.isOwner() || aBoolean ? View.VISIBLE : View.GONE);
        });

        viewModel.getCustomers().observe(this, customerEntities -> {
            binding.txtContactList.setText(MessageFormat.format(getString(R.string.customer_list) + "({0})", customerEntities.size()));
        });

        viewModel.getServiceNumberEntity().observe(this, serviceNumberEntity -> {
            setServiceInfo(serviceNumberEntity);
            setMembersProfile(serviceNumberEntity);
            this.serviceNumberEntity = serviceNumberEntity;
            serviceViewModel.getIsTenantManager();
        });

        serviceViewModel.getTimeList().observe(this, this::createDialog);

        serviceViewModel.getOnUpdateServiceNumberTimeoutTime().observe(this, isSuccess -> {
            if (isSuccess) {
                refreshData(RefreshSource.LOCAL);
                dismissIosDialog();
                Toast.makeText(this, "更新成功", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getData() {
        serviceViewModel.getServiceTimeOutList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData(RefreshSource.REMOTE);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            onResume();
        } else {
            super.onBackPressed();
            overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
        }
    }

    private final NoDoubleClickListener clickListener = new NoDoubleClickListener() {
        @Override
        protected void onNoDoubleClick(View v) {
            if (v.equals(binding.btnExit)) {
                finish();
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
            } else if (v.equals(binding.layoutBroadcast)) {
                SystemKit.openServiceNumberBroadcast(BossServiceNumberHomepageActivity.this, name, broadcastRoomId, serviceNumberId);
            } else if (v.equals(binding.txtMemberAll)) {
                ActivityTransitionsControl.navigateToServiceAgentsManage(BossServiceNumberHomepageActivity.this, broadcastRoomId, serviceNumberId, (intent, s) -> startActivity(intent));
            } else if (v.equals(binding.txtDesc)) {
                changeDesc();
            } else if (v.equals(binding.layoutCustomer)) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.slide_left_in, R.anim.slide_left_out, R.anim.slide_right_in, R.anim.slide_right_out)
                    .add(R.id.fl_self, CustomerListFragment.newInstance(serviceNumberId))
                    .addToBackStack(null).commit();
            } else if (v.equals(binding.layoutWelcomeMessage)) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.slide_left_in, R.anim.slide_left_out, R.anim.slide_right_in, R.anim.slide_right_out)
                    .add(R.id.fl_self, WelcomeMessageSettingFragment.newInstance(serviceNumberId, true))
                    .addToBackStack(null).commit();
            } else if (v.equals(binding.clServiceNumberTimeoutSetting)) {
                showTimeOutDialog();
            }
        }
    };

    private void createDialog(List<DictionaryItems> list) {
        serviceNumberTimeOutDialog = new Dialog(this);
        DialogServiceNumberTimeOutBinding dialogServiceNumberTimeOutBinding = DialogServiceNumberTimeOutBinding.inflate(LayoutInflater.from(this));
        serviceNumberTimeOutDialog.setContentView(dialogServiceNumberTimeOutBinding.getRoot());
        ServiceNumberTimeOutAdapter serviceNumberTimeOutAdapter = new ServiceNumberTimeOutAdapter(list, time -> {
            serviceViewModel.updateServiceNumberTimeoutTime(serviceNumberId, time);
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

    private void refreshData(RefreshSource source) {
        String serviceNumberId = getIntent().getStringExtra(BundleKey.SERVICE_NUMBER_ID.key());
        ChatServiceNumberService.findServiceNumber(this, serviceNumberId, source, new ServiceCallBack<ServiceNumberEntity, RefreshSource>() {
            @Override
            public void complete(ServiceNumberEntity entity, RefreshSource source) {
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                    viewModel.setServiceNumberEntity(entity);
                });
            }

            @Override
            public void error(String message) {
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                    Toast.makeText(BossServiceNumberHomepageActivity.this, message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void getServiceNumberContactList() {
        ApiManager.doServiceNumberContactListRequest(BossServiceNumberHomepageActivity.this, serviceNumberId, new ApiListener<List<CustomerEntity>>() {
            @Override
            public void onSuccess(List<CustomerEntity> serviceNumberContactEntities) {
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                    List<CustomerEntity> filterList = serviceNumberContactEntities.stream()
                        .filter(customerEntity -> !customerEntity.getUserType().equals(UserType.VISITOR.getUserType()) && customerEntity.getServiceNumberIds().contains(serviceNumberId))
                        .collect(Collectors.toList());
                    viewModel.setCustomers(filterList);
                });
            }

            @Override
            public void onFailed(String errorMessage) {

            }
        });
    }

    private void setServiceInfo(ServiceNumberEntity serviceNumberEntity) {
        // 服務號名稱
        binding.txtName.setText(serviceNumberEntity.getName());
        binding.txtDesc.setText(serviceNumberEntity.getDescription());
        // 服務號頭像
        AvatarService.post(BossServiceNumberHomepageActivity.this, serviceNumberEntity.getAvatarId(), PicSize.SMALL, binding.imgAvatar, R.drawable.custom_default_avatar);
        //服務號擁有者和管理者才可以用群發和歡迎語設定
        if (serviceNumberEntity.isManager() || serviceNumberEntity.isOwner()) {
            //群發隱藏, 等新版本完成後在開啟
            if (SystemConfig.enableBroadcast) {
                binding.layoutBroadcast.setVisibility(View.VISIBLE);
            }
            binding.layoutWelcomeMessage.setVisibility(View.VISIBLE);
        }

        binding.tvTimeOutValue.setText(getServiceNumberDisplayText(serviceNumberEntity));
    }

    private String getServiceNumberDisplayText(ServiceNumberEntity serviceNumberEntity) {
        Context context = binding.getRoot().getContext();
        int timeout = serviceNumberEntity.getServiceTimeoutTime();
        if (timeout == 0) {
            return context.getString(R.string.text_dialog_time_out_no_limit);
        } else if (timeout < 60) {
            return context.getString(R.string.text_dialog_time_out_thirty_min);
        } else {
            int hour = timeout / 60;
            return switch (hour) {
                case 1 -> context.getString(R.string.text_dialog_time_out_one_hour);
                case 6 -> context.getString(R.string.text_dialog_time_out_six_hour);
                case 12 -> context.getString(R.string.text_dialog_time_out_twelve_hour);
                case 24 -> context.getString(R.string.text_dialog_time_out_twenty_four_hour);
                case 48 -> context.getString(R.string.text_dialog_time_out_forty_eight_hour);
                default -> context.getString(R.string.text_dialog_time_out_no_limit);
            };
        }
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    private void setMembersProfile(ServiceNumberEntity serviceNumberEntity) {
        String userId = TokenPref.getInstance(this).getUserId();
        binding.txtServiceMemberCount.setText("服務號成員 " + serviceNumberEntity.getMemberItems().size());
        memberAdapter.setManager(serviceNumberEntity.isManager())
            .setSelfId(userId)
            .setData(serviceNumberEntity.getMemberItems())
            .notifyDataSetChanged();
    }

    private void updateDescription(String description) {
        ApiManager.updateServiceNumberDescription(BossServiceNumberHomepageActivity.this, serviceNumberId, description, new ApiListener<String>() {
            @Override
            public void onSuccess(String result) {
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                        Toast.makeText(BossServiceNumberHomepageActivity.this, "更新成功", Toast.LENGTH_SHORT).show();
                        binding.txtDesc.setText(description);
                    }
                );
            }

            @Override
            public void onFailed(String errorMessage) {
                ThreadExecutorHelper.getMainThreadExecutor().execute(() ->
                    Toast.makeText(BossServiceNumberHomepageActivity.this, errorMessage, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void changeDesc() {
        String nickName = binding.txtDesc.getText().toString();
        new AlertView.Builder()
            .setContext(BossServiceNumberHomepageActivity.this)
            .setStyle(AlertView.Style.EditAlert)
            .setTitle("")
            .setMessage(getString(R.string.text_input_remark))
            .setInputDatas(new String[]{Strings.isNullOrEmpty(nickName) ? getString(R.string.text_input_remark) : nickName}, Strings.isNullOrEmpty(nickName))
            .setOnItemClickListener((OnItemClickListener<List<EditText>>) (o, position) -> {
                if (position == 0) {
                    String text = o.get(0).getText().toString();
                    if (text.length() > 20) {
                        Toast.makeText(this, getString(R.string.name_length_tip), Toast.LENGTH_SHORT).show();
                    } else if (text.contains(" ")) {
                        Toast.makeText(this, "不能輸入特殊字符，如空格", Toast.LENGTH_SHORT).show();
                    } else {
                        updateDescription(text);
                    }
                }
            })
            .build()
            .setCancelable(true)
            .setOnDismissListener(null)
            .show();
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
