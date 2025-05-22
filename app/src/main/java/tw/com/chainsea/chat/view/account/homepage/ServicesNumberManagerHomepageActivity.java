package tw.com.chainsea.chat.view.account.homepage;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.common.base.Strings;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import tw.com.chainsea.chat.util.DaVinci;
import cn.hadcn.davinci.image.base.ImageEntity;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.ce.sdk.bean.CustomerEntity;
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.event.EventBusUtils;
import tw.com.chainsea.ce.sdk.event.EventMsg;
import tw.com.chainsea.ce.sdk.event.MsgConstant;
import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.http.cp.CpApiManager;
import tw.com.chainsea.ce.sdk.http.cp.base.CpApiListener;
import tw.com.chainsea.ce.sdk.http.cp.respone.RelationTenant;
import tw.com.chainsea.ce.sdk.service.ChatServiceNumberService;
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.base.Constant;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.config.SystemConfig;
import tw.com.chainsea.chat.databinding.ActivityServicesNumberManagerHomepageBinding;
import tw.com.chainsea.chat.network.contact.ViewModelFactory;
import tw.com.chainsea.chat.network.tenant.TenantViewModel;
import tw.com.chainsea.chat.service.ActivityTransitionsControl;
import tw.com.chainsea.chat.ui.activity.ClipImageActivity;
import tw.com.chainsea.chat.ui.utils.permissionUtils.DialogUtil;
import tw.com.chainsea.chat.ui.utils.permissionUtils.RequestCodeManger;
import tw.com.chainsea.chat.ui.utils.permissionUtils.XPermissionUtils;
import tw.com.chainsea.chat.util.AvatarKit;
import tw.com.chainsea.chat.util.SystemKit;
import tw.com.chainsea.chat.util.ThemeHelper;
import tw.com.chainsea.chat.view.account.UserInformationHomepageActivity;
import tw.com.chainsea.chat.view.account.fragments.GroupUpgradeFragment;
import tw.com.chainsea.chat.view.homepage.CustomerListFragment;
import tw.com.chainsea.chat.view.homepage.GroupMemberListFragment;
import tw.com.chainsea.chat.view.homepage.ServiceNumberListFragment;
import tw.com.chainsea.chat.view.homepage.WelcomeMessageSettingFragment;
import tw.com.chainsea.chat.view.homepage.viewmodel.ServiceNumberViewModel;
import tw.com.chainsea.custom.view.alert.AlertView;
import tw.com.chainsea.custom.view.alert.OnItemClickListener;

public class ServicesNumberManagerHomepageActivity extends UserInformationHomepageActivity {
    private final AvatarKit avatarKit = new AvatarKit();
    private ActivityServicesNumberManagerHomepageBinding binding;
    private RelationTenant currentTenant;
    private ServiceNumberViewModel viewModel;
    private ServiceNumberEntity serviceNumberEntity;
    private String serviceNumberId;
    private String broadcastRoomId;
    boolean CAN_NEXT = false;

    private TenantViewModel tenantViewModel;

    private static final int TENANT_BACKGROUND_PICS_RESULT_CODE = 0x138A;
    private static final int TENANT_AVATAR_RESULT_CODE = 0x138B;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.INSTANCE.setTheme(ServicesNumberManagerHomepageActivity.this);
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_services_number_manager_homepage);
        EventBusUtils.register(this);
        binding.clRoot.setPadding(0, getStatusHeight(), 0, 0);
        currentTenant = TokenPref.getInstance(this).getCpCurrentTenant();
        viewModel = new ViewModelProvider(this).get(ServiceNumberViewModel.class);
        Intent intent = getIntent();
        if (intent != null) {
            serviceNumberId = intent.getStringExtra(BundleKey.SERVICE_NUMBER_ID.key());
            getServiceNumberEntry(serviceNumberId);
            getServiceNumberContactList();
        }
        bindingInformation(currentTenant);
        initListener();
        initViewModel();
        observerData();
    }

    private void initViewModel() {
        ViewModelFactory viewModelFactory = new ViewModelFactory(getApplication());
        tenantViewModel = new ViewModelProvider(this, viewModelFactory).get(TenantViewModel.class);
    }

    private void observerData() {
        tenantViewModel.getOnTenantAvatarUpdated().observe(this, avatarUrl -> {
            try {
                Glide.with(binding.civTeamAvatar).load(avatarUrl).into(binding.civTeamAvatar);
            } catch (Exception ignored) {
            }

        });
    }

    private void initListener() {
        binding.leftAction.setOnClickListener(this::doLeftAction);
        binding.ivCamera.setOnClickListener(this::doAvatarCameraAction);
        binding.llServiceNumberNameBox.setOnClickListener(this::changeTenantName);
        binding.llDescribeBox.setOnClickListener(this::changeDescription);
        binding.tvCompany.setOnClickListener(this::doGroupUpgradeAction);
        binding.tvCustomerList.setOnClickListener(this::customerListAction);
        binding.tvWelcomeMessage.setOnClickListener(this::welcomeMessageAction);
        binding.tvBroadcast.setOnClickListener(this::serviceNumberBroadcastAction);
        binding.ivMore.setOnClickListener(this::doMoreSettingAction);
        binding.tvGroupMemberList.setOnClickListener(this::groupMemberListAction);
        binding.tvServiceList.setOnClickListener(this::serviceNumberListAction);
    }

    private void bindingInformation(RelationTenant relationTenant) {
//        AvatarService.post(this, relationTenant.getAvatarId(), PicSize.LARGE, binding.civTeamAvatar, R.drawable.default_avatar);
        avatarKit.loadCpTenantAvatar(relationTenant.getAvatarId(), binding.civTeamAvatar);
        binding.tvName.setText(relationTenant.getTenantName());
        binding.tvDescription.setText(relationTenant.getDescription());
        if (Strings.isNullOrEmpty(relationTenant.getDescription())) {
            binding.tvDescription.setText("團隊說明");
        } else {
            binding.tvDescription.setText(relationTenant.getDescription());
        }

        binding.civTeamAvatar.setOnClickListener(v -> {
            String url = AvatarKit.getCpAvatarUrl(relationTenant.getAvatarId(), "m");
            String turl = AvatarKit.getCpAvatarUrl(relationTenant.getAvatarId());

            ActivityTransitionsControl.navigateToPhotoGallery(ServicesNumberManagerHomepageActivity.this, url, turl, (intent, s1) -> {
                startActivity(intent);
            });
        });
    }

    @Override
    protected void switchMode(PageMode pageMode) {

    }

    private void changeTenantName(View v) {
        String nickName = binding.tvName.getText().toString();
        new AlertView.Builder()
            .setContext(this)
            .setStyle(AlertView.Style.EditAlert)
            .setTitle("")
            .setMessage("請輸入團隊名稱")
            .setInputDatas(new String[]{Strings.isNullOrEmpty(nickName) ? "請輸入團隊名稱" : currentTenant.getTenantName()},
                Strings.isNullOrEmpty(currentTenant.getTenantName()))
            .setOnItemClickListener((OnItemClickListener<List<EditText>>) (o, position) -> {
                if (position == 0) {
                    String text = o.get(0).getText().toString();
                    if (text.length() > 20) {
                        Toast.makeText(this, getString(R.string.tenant_name_length_tip), Toast.LENGTH_SHORT).show();
                    } else if (text.contains(" ")) {
                        Toast.makeText(this, "不能輸入特殊字符，如空格", Toast.LENGTH_SHORT).show();
                    } else {
                        updateTenant(text, null, null);
                    }
                }
            })
            .build()
            .setCancelable(true)
            .setOnDismissListener(o -> CAN_NEXT = true)
            .show();
    }

    private void changeDescription(View v) {
        String description = binding.tvDescription.getText().toString();
        new AlertView.Builder()
            .setContext(this)
            .setStyle(AlertView.Style.EditAlert)
            .setTitle("")
            .setMessage("輸入團隊說明")
            .setInputDatas(new String[]{Strings.isNullOrEmpty(description) ? "輸入團隊說明" : description},
                Strings.isNullOrEmpty(description))
            .setOnItemClickListener((OnItemClickListener<List<EditText>>) (o, position) -> {
                if (position == 0) {
                    String text = o.get(0).getText().toString();
                    if (text.length() > 150) {
                        Toast.makeText(this, getString(R.string.description_length_tip), Toast.LENGTH_SHORT).show();
                    } else if (text.contains(" ")) {
                        Toast.makeText(this, "不能輸入特殊字符，如空格", Toast.LENGTH_SHORT).show();
                    } else {
                        updateTenant(null, text, null);
                    }
                }
            })
            .build()
            .setCancelable(true)
            .setOnDismissListener(o -> CAN_NEXT = true)
            .show();
    }

    private void groupMemberListAction(View view) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_left_in, R.anim.slide_left_out, R.anim.slide_right_in, R.anim.slide_right_out)
            .add(R.id.fl_group, GroupMemberListFragment.newInstance(serviceNumberId, broadcastRoomId))
            .addToBackStack(null).commit();
    }

    private void serviceNumberBroadcastAction(View view) {
        //群發隱藏, 等新版本完成後在開啟
        if (SystemConfig.enableBroadcast) {
            SystemKit.openServiceNumberBroadcast(this, "", broadcastRoomId, serviceNumberId);
        } else Toast.makeText(this, "即將開放", Toast.LENGTH_SHORT).show();
    }

    private void customerListAction(View view) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_left_in, R.anim.slide_left_out, R.anim.slide_right_in, R.anim.slide_right_out)
            .add(R.id.fl_group, CustomerListFragment.newInstance(serviceNumberId))
            .addToBackStack(null).commit();
    }

    private void welcomeMessageAction(View view) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_left_in, R.anim.slide_left_out, R.anim.slide_right_in, R.anim.slide_right_out)
            .add(R.id.fl_group, WelcomeMessageSettingFragment.newInstance(serviceNumberId, true))
            .addToBackStack(null).commit();
    }

    private void serviceNumberListAction(View view) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_left_in, R.anim.slide_left_out, R.anim.slide_right_in, R.anim.slide_right_out)
            .add(R.id.fl_group, ServiceNumberListFragment.newInstance())
            .addToBackStack(null).commit();
    }

    private void getServiceNumberContactList() {
        if (currentTenant.getManageServiceNumberInfo() != null) {
            ApiManager.doServiceNumberContactListRequest(this, serviceNumberId, new ApiListener<List<CustomerEntity>>() {
                @Override
                public void onSuccess(List<CustomerEntity> serviceNumberContactEntities) {
                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                        viewModel.setCustomers(serviceNumberContactEntities);
                    });
                }

                @Override
                public void onFailed(String errorMessage) {

                }
            });
        }
    }

    private void getServiceNumberEntry(String serviceNumberId) {
        ChatServiceNumberService.findServiceNumber(this, serviceNumberId, RefreshSource.LOCAL, new ServiceCallBack<ServiceNumberEntity, RefreshSource>() {
            @Override
            public void complete(ServiceNumberEntity entity, RefreshSource source) {
                serviceNumberEntity = entity;
                broadcastRoomId = entity.getBroadcastRoomId();
            }

            @Override
            public void error(String message) {
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                    Toast.makeText(ServicesNumberManagerHomepageActivity.this, message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void doLeftAction(View v) {
        finish();
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
    }

    private void doGroupUpgradeAction(View view) {
        new AlertView.Builder()
            .setContext(this)
            .setStyle(AlertView.Style.Alert)
            .setMessage("想在Aile生態中成為獨一無二的認證企業嗎? 若您想要升級請按確定。")
            .setOthers(new String[]{getString(R.string.alert_cancel), getString(R.string.alert_confirm)})
            .setOnItemClickListener((o, position) -> {
                if (position == 1) {
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.setCustomAnimations(R.anim.slide_left_in, R.anim.slide_left_out, R.anim.slide_right_in, R.anim.slide_right_out)
                        .add(R.id.fl_group, GroupUpgradeFragment.newInstance(currentTenant.getTenantId(),
                            currentTenant.getAvatarId(), currentTenant.getTenantName(), currentTenant.getDescription()))
                        .addToBackStack(null).commit();
                }
            })
            .build()
            .setCancelable(true)
            .show();
    }

    private void doBackgroundCameraAction(View view) {
        doOpenCamera(TENANT_BACKGROUND_PICS_RESULT_CODE);
    }

    private void doAvatarCameraAction(View view) {
        doOpenCamera(TENANT_AVATAR_RESULT_CODE);
    }

    private void doMoreSettingAction(View view) {
        new AlertView.Builder().setContext(this)
                .setStyle(AlertView.Style.ActionSheet).setOthers(new String[]{"解散團隊"})
                .setCancelText(getString(R.string.alert_cancel)).setOnItemClickListener((o, position) -> {
                    if (position == 0) {
                        new AlertView.Builder().setContext(this)
                                .setStyle(AlertView.Style.Alert).setOthers(new String[]{getString(R.string.alert_cancel), getString(R.string.alert_confirm)})
                                .setMessage("提醒您，解散後所有服務號將會停止使用，同時會將所有與團隊相關資料清除，若您要解散團隊請按確定。")
                                .setOnItemClickListener((o1, position1) -> {
                                    if (position1 == 1) {
                                        ApiManager.doTenantDismiss(this, new ApiListener<String>() {
                                            @Override
                                            public void onSuccess(String s) {
                                                SystemKit.logoutToLoginPage();
                                            }

                                            @Override
                                            public void onFailed(String errorMessage) {
                                                Toast.makeText(ServicesNumberManagerHomepageActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }).build().setCancelable(true).show();
                    }
                })
                .build().setOnDismissListener(o -> CAN_NEXT = true).setCancelable(true).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        CAN_NEXT = true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            String name = data.getStringExtra(BundleKey.RESULT_PIC_URI.key());
            ImageEntity entity = DaVinci.with().getImageLoader().getImage(name);
            String path = DaVinci.with().getImageLoader().getAbsolutePath(name);
            switch (requestCode) {
                case TENANT_AVATAR_RESULT_CODE:
                    tenantViewModel.updateTenantAvatar(path, name, entity.getBitmap().getHeight());
                    break;
                case TENANT_BACKGROUND_PICS_RESULT_CODE:
//                    UserProfileService.uploadSelfHomepagePics(this, path, name);
                    break;
            }
        }
    }

    private void updateTenant(String tenantName, String description, String filePath) {
        CpApiManager.getInstance().tenantUpdate(this, currentTenant.getTenantId(), tenantName, description, filePath, new CpApiListener<String>() {
            @Override
            public void onSuccess(String s) {
                if (!Strings.isNullOrEmpty(tenantName)) {
                    currentTenant.setTenantName(tenantName);
                    binding.tvName.setText(tenantName);
                }

                if (!Strings.isNullOrEmpty(description)) {
                    currentTenant.setDescription(description);
                    binding.tvDescription.setText(description);
                }
            }

            @Override
            public void onFailed(String errorCode, String errorMessage) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBusUtils.unregister(this);
    }

    private void doOpenCamera(int requestCode) {
        XPermissionUtils.requestPermissions(ServicesNumberManagerHomepageActivity.this, RequestCodeManger.CAMERA, new String[]{Manifest.permission.CAMERA},
            new XPermissionUtils.OnPermissionListener() {
                @Override
                public void onPermissionGranted() {
//                        if (PermissionHelper.isCameraEnable()) {
                    showPicDialog(requestCode);
//                            CELog.e("Did not receive permission callback");
//                        } else {
//                            DialogUtil.showPermissionManagerDialog(ServicesNumberManagerHomepageActivity.this, getString(R.string.warning_camera));
//                        }
                }

                @Override
                public void onPermissionDenied(final String[] deniedPermissions, boolean alwaysDenied) {
                    Toast.makeText(ServicesNumberManagerHomepageActivity.this, "獲取相機權限失敗", Toast.LENGTH_SHORT).show();
                    // Don't ask again after rejection -> prompt to jump to settings
                    if (alwaysDenied) {
                        DialogUtil.showPermissionManagerDialog(ServicesNumberManagerHomepageActivity.this, getString(R.string.warning_camera));
                    } else {    // 拒绝 -> 提示此公告的意义，并可再次尝试获取权限
                        new AlertDialog.Builder(ServicesNumberManagerHomepageActivity.this).setTitle("溫馨提示")
                            .setMessage("我們需要相機權限才能正常使用該功能")
                            .setNegativeButton(getString(R.string.alert_cancel), null)
                            .setPositiveButton("驗證權限", (dialog, which) -> XPermissionUtils.requestPermissionsAgain(ServicesNumberManagerHomepageActivity.this, deniedPermissions,
                                RequestCodeManger.CAMERA))
                            .show();
                    }
                }
            });
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            getServiceNumberEntry(serviceNumberId);
        } else {
            super.onBackPressed();
        }
    }

    private void showPicDialog(int requestCode) {
        new AlertView.Builder()
                .setContext(this)
                .setStyle(AlertView.Style.ActionSheet)
                .setOthers(new String[]{getString(R.string.warning_photos), getString(R.string.warning_camera)})
                .setCancelText(getString(R.string.alert_cancel))
                .setOnItemClickListener((o, position) -> {
                    if (position == 0) {
                        Intent intent = new Intent(this, ClipImageActivity.class);
                        intent.putExtra(Constant.INTENT_IMAGE_GET_WAY, Constant.INTENT_CODE_ALBUM);
                        intent.putExtra(Constant.INTENT_IMAGE_CONFIRM_TYPE, Constant.INTENT_CODE_CROP);
                        startActivityForResult(intent, requestCode);
                    } else if (position == 1) {
                        Intent intent = new Intent(this, ClipImageActivity.class);
                        intent.putExtra(Constant.INTENT_IMAGE_GET_WAY, Constant.INTENT_CODE_CAMERA);
                        intent.putExtra(Constant.INTENT_IMAGE_CONFIRM_TYPE, Constant.INTENT_CODE_CROP);
                        startActivityForResult(intent, requestCode);
                    }
                })
                .build()
                .setOnDismissListener(o -> CAN_NEXT = true)
                .setCancelable(true)
                .show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleMainEvent(EventMsg eventMsg) {
        switch (eventMsg.getCode()) {
            case MsgConstant.NOTICE_REFRESH_HOMEPAGE_BACKGROUND_PICS:
            case MsgConstant.NOTICE_REFRESH_HOMEPAGE_AVATAR:
                break;
            case MsgConstant.ACCOUNT_REFRESH_FILTER:
            case MsgConstant.SELF_REFRESH_FILTER:
                break;
        }
    }
}
