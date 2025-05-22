package tw.com.chainsea.chat.view.service;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.common.base.Strings;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.Objects;

import tw.com.chainsea.chat.util.DaVinci;
import cn.hadcn.davinci.image.base.ImageEntity;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.android.common.ui.LayoutHelper;
import tw.com.chainsea.android.common.ui.UiHelper;
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity;
import tw.com.chainsea.ce.sdk.bean.servicenumber.type.ServiceNumberPrivilege;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.event.EventMsg;
import tw.com.chainsea.ce.sdk.event.MsgConstant;
import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.http.ce.model.Member;
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference;
import tw.com.chainsea.ce.sdk.reference.ServiceNumberReference;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.databinding.ActivityServiceNumberManageBinding;
import tw.com.chainsea.chat.network.contact.ViewModelFactory;
import tw.com.chainsea.chat.service.ActivityTransitionsControl;
import tw.com.chainsea.chat.ui.dialog.InputDialogBuilder;
import tw.com.chainsea.chat.ui.utils.permissionUtils.XPermissionUtils;
import tw.com.chainsea.chat.util.SortUtil;
import tw.com.chainsea.chat.util.UploadAvatarUtil;
import tw.com.chainsea.chat.view.BaseActivity;
import tw.com.chainsea.chat.view.roomAction.adapter.MemberAdapter;

public class ServiceNumberManageActivity extends BaseActivity {
    private ActivityServiceNumberManageBinding binding;
    private MemberAdapter memberAdapter;

    private boolean CAN_NEXT = false;

    private ServiceNumberAgentsManageViewModel serviceViewModel;

    private ServiceNumberEntity serviceNumberEntity;

    private UploadAvatarUtil uploadAvatar;

    private boolean isCanShowEditIcon = false;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_service_number_manage);
        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
        getWindow().setStatusBarColor(0xFF6BC2BA);

        // 統計chart
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);

        binding.rvMemberList.setLayoutManager(new GridLayoutManager(this, LayoutHelper.calculateNoOfColumns(this, 65)));
        binding.rvMemberList.setHasFixedSize(true);
        binding.rvMemberList.setItemAnimator(new DefaultItemAnimator());
        memberAdapter = new MemberAdapter();
        binding.rvMemberList.setAdapter(memberAdapter);

        refreshData(RefreshSource.LOCAL);
        initListener();
        initViewModel();
        observeData();
        serviceViewModel.getServiceEntityFromRemote(Objects.requireNonNull(getIntent().getStringExtra(BundleKey.SERVICE_NUMBER_ID.key())));
    }

    @Override
    protected void onResume() {
        super.onResume();
        CAN_NEXT = true;
        refreshData(RefreshSource.REMOTE);
    }

    private void refreshData(RefreshSource source) {
        String serviceNumberId = getIntent().getStringExtra(BundleKey.SERVICE_NUMBER_ID.key());
        if (source == RefreshSource.LOCAL) {
            getServiceNumberFromLocal(serviceNumberId);
        } else {
            getServiceNumberFromRemote(serviceNumberId);
        }
    }

    private void getServiceNumberFromLocal(String serviceNumberId) {
        serviceNumberEntity = ServiceNumberReference.findServiceNumberById(serviceNumberId);
        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
            setServiceInfo();
            setMembersProfile(serviceNumberEntity);
        });
    }

    private void getServiceNumberFromRemote(String serviceNumberId) {
        ApiManager.doServiceNumberItem(this, serviceNumberId, new ApiListener<>() {
            @Override
            public void onSuccess(ServiceNumberEntity entity) {
                // 判斷服務號是否有更換頭圖
                if (!serviceNumberEntity.getAvatarId().equals(entity.getAvatarId())) {
                    ChatRoomReference.getInstance().updateRoomAvatarByServiceNumberId(entity.getServiceNumberId(), entity.getAvatarId());
                }


                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                    serviceNumberEntity = entity;
                    setServiceInfo();
                    setMembersProfile(entity);
                });

                ServiceNumberReference.save(null, entity);
                if (entity != null && !Strings.isNullOrEmpty(entity.getBroadcastRoomId())) {
                    getIntent().putExtra(BundleKey.BROADCAST_ROOM_ID.key(), entity.getBroadcastRoomId());
                }
                EventBus.getDefault().post(new EventMsg<>(MsgConstant.REFRESH_SERVICE_NUMBER));
            }

            @Override
            public void onFailed(String errorMessage) {
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 成員資訊列表
     */
    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    private void setMembersProfile(ServiceNumberEntity entity) {
        String userId = TokenPref.getInstance(this).getUserId();
        //排序
        List<Member> sortedList = SortUtil.INSTANCE.sortOwnerManagerByPrivilege(entity.getMemberItems());
        //members.remove(UserProfileEntity.Build().id(userId).build());
        binding.tvServiceMemberCount.setText("服務號成員 " + sortedList.size());
        memberAdapter.setManager(entity.isManager())
            .setSelfId(userId)
            .setData(sortedList)
            .notifyDataSetChanged();
    }

    private void initViewModel() {
        ViewModelFactory serviceNumberAgentsManageRepository = new ViewModelFactory(getApplication());
        serviceViewModel = new ViewModelProvider(this, serviceNumberAgentsManageRepository).get(ServiceNumberAgentsManageViewModel.class);
    }

    private void observeData() {
        serviceViewModel.isTenantManager().observe(this, aBoolean -> {
            int visibility = serviceNumberEntity.isManager() || serviceNumberEntity.isOwner() || aBoolean ? View.VISIBLE : View.GONE;
            isCanShowEditIcon = serviceNumberEntity.isManager() || serviceNumberEntity.isOwner() || aBoolean;
            binding.rightAction.setVisibility(visibility);
            binding.ivEditServiceNumberName.setVisibility(visibility);
            binding.ivCamera.setVisibility(visibility);
            showEditContentIcon();
        });

        serviceViewModel.getAgentsList().observe(this, members -> {
            members.forEach(member -> {
                String selfUserId = TokenPref.getInstance(this).getUserId();
                if (member.getId().equals(selfUserId)) {
                    serviceNumberEntity.setCommon(member.getPrivilege().equals(ServiceNumberPrivilege.COMMON));
                    serviceNumberEntity.setManager(member.getPrivilege().equals(ServiceNumberPrivilege.MANAGER));
                    serviceNumberEntity.setOwner(member.getPrivilege().equals(ServiceNumberPrivilege.OWNER));
                }
            });
            serviceViewModel.getIsTenantManager();
        });
    }


    /**
     * 服務號資訊
     */
    private void setServiceInfo() {
        // 服務號名稱
        String name = serviceNumberEntity.getName();
        binding.tvTitle.setText(name);
        binding.tvServiceNumberName.setText(name);
        // 服務號頭像
        binding.civServiceAvatar.loadAvatarIcon(serviceNumberEntity.getAvatarId(), serviceNumberEntity.getName(), serviceNumberEntity.getServiceNumberId());

        if (serviceNumberEntity.getDescription() != null) {
            if (serviceNumberEntity.getDescription().isEmpty()) {
                binding.tvServiceNumberContent.setText("服務號說明");
            } else {
                binding.tvServiceNumberContent.setText(serviceNumberEntity.getDescription());
            }
        }

        binding.tvServiceNumberContent.setMovementMethod(ScrollingMovementMethod.getInstance());
        serviceViewModel.getServiceEntityFromRemote(serviceNumberEntity.getServiceNumberId());
    }

    private void showEditContentIcon() {
        if (!isCanShowEditIcon) return;
        binding.tvServiceNumberContent.post(() -> {
            int width = binding.glLeft.getLeft() - binding.glRight.getLeft();
            if (binding.tvServiceNumberContent.getWidth() > width - UiHelper.dp2px(this, 12)) {
                binding.ivEditServiceNumberContent2.setVisibility(View.GONE);
                binding.ivEditServiceNumberContent.setVisibility(View.VISIBLE);
            } else {
                binding.ivEditServiceNumberContent2.setVisibility(View.VISIBLE);
                binding.ivEditServiceNumberContent.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
    }

    private void initListener() {
        binding.leftAction.setOnClickListener(this::doLeftAction);
        binding.rightAction.setOnClickListener(this::doRightAction);
        binding.clMemberSectioned.setOnClickListener(this::doNavigateToAgentsManageAction);
        binding.ivEditServiceNumberContent.setOnClickListener(this::doUpdateServiceNumberDescription);
        binding.ivEditServiceNumberContent2.setOnClickListener(this::doUpdateServiceNumberDescription);
        binding.ivEditServiceNumberName.setOnClickListener(this::doUpdateServiceNumberName);
        binding.ivCamera.setOnClickListener(this::doUpdateServiceNumberAvatar);
    }

    /**
     * 退出
     */
    private void doLeftAction(View view) {
        finish();
    }

    /**
     * 更多設定
     */
    private void doRightAction(View view) {
        if (CAN_NEXT) {
            CAN_NEXT = false;
            String serviceNumberId = getIntent().getStringExtra(BundleKey.SERVICE_NUMBER_ID.key());
            ActivityTransitionsControl.navigateToServiceNumberMoreSettings(this, serviceNumberId, (intent, s) -> startActivity(intent));
        }
    }

    /**
     * 到成員列表
     */
    private void doNavigateToAgentsManageAction(View view) {
        if (CAN_NEXT) {
            CAN_NEXT = false;
            String broadcastRoomId = getIntent().getStringExtra(BundleKey.BROADCAST_ROOM_ID.key());
            String serviceNumberId = getIntent().getStringExtra(BundleKey.SERVICE_NUMBER_ID.key());
            ActivityTransitionsControl.navigateToServiceAgentsManage(this, broadcastRoomId, serviceNumberId, (intent, s) -> startActivity(intent));
        }
    }

    private void doUpdateServiceNumberAvatar(View view) {
        uploadAvatar = new UploadAvatarUtil();
        uploadAvatar.showPickPictureDialog(this);
    }


    /**
     * 更新服務號名稱 dialog
     */
    private void doUpdateServiceNumberName(View view) {
        Dialog dialog = new InputDialogBuilder(this)
            .setTitle("服務號名稱")
            .setHint("請輸入服務號名稱")
            .setToastMessage("請輸入名稱")
            .setMaxLength(20)
            .setInputData(serviceNumberEntity.getName())
            .setOnConfirmListener(this::updateServiceNumberName)
            .create();
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout((int) (UiHelper.getDisplayWidth(this) * 0.8), GridLayoutManager.LayoutParams.WRAP_CONTENT);
        }
    }

    /**
     * 更新服務號名稱
     */
    private void updateServiceNumberName(String name) {
        ApiManager.updateServiceNumberName(this, serviceNumberEntity.getServiceNumberId(), name, new ApiListener<>() {
            @Override
            public void onSuccess(String result) {
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                    Toast.makeText(ServiceNumberManageActivity.this, "更新成功", Toast.LENGTH_SHORT).show();
                    getServiceNumberFromRemote(serviceNumberEntity.getServiceNumberId());
                });
            }

            @Override
            public void onFailed(String errorMessage) {
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> Toast.makeText(ServiceNumberManageActivity.this, errorMessage, Toast.LENGTH_SHORT).show());
            }
        });
    }

    /**
     * 更新服務號描述 dialog
     */
    private void doUpdateServiceNumberDescription(View view) {
        dialog = new InputDialogBuilder(this)
            .setTitle("服務號說明")
            .setHint("請輸入服務號說明")
            .setMaxLength(150)
            .setIsCanEmpty(true)
            .setInputData(serviceNumberEntity.getDescription())
            .setCustomConfirmClickListener(this::updateServiceNumberDescription)
            .create();
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout((int) (UiHelper.getDisplayWidth(this) * 0.8), GridLayoutManager.LayoutParams.WRAP_CONTENT);
        }
    }

    /**
     * 更新服務號描述
     */
    private void updateServiceNumberDescription(String description) {
        ApiManager.updateServiceNumberDescription(this, serviceNumberEntity.getServiceNumberId(), description, new ApiListener<>() {
            @Override
            public void onSuccess(String result) {
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                    Toast.makeText(ServiceNumberManageActivity.this, "更新成功", Toast.LENGTH_SHORT).show();
                    if (description.isEmpty()) {
                        binding.tvServiceNumberContent.setText("服務號說明");
                    } else {
                        binding.tvServiceNumberContent.setText(description);
                    }
                    serviceNumberEntity.setDescription(description);
                    showEditContentIcon();
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                });
            }

            @Override
            public void onFailed(String errorMessage) {
                Toast.makeText(ServiceNumberManageActivity.this, "儲存失敗", Toast.LENGTH_SHORT).show();
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> Toast.makeText(ServiceNumberManageActivity.this, errorMessage, Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == UploadAvatarUtil.REQUEST_AVATAR_CODE_RESULT) {
                String name = data.getStringExtra(BundleKey.RESULT_PIC_URI.key());
                ImageEntity entity = DaVinci.with().getImageLoader().getImage(name);
                String path = DaVinci.with().getImageLoader().getAbsolutePath(name);
                if (uploadAvatar != null) {
                    uploadAvatar.uploadServiceNumberAvatar(this, serviceNumberEntity.getServiceNumberId(), entity.getBitmap().getHeight(), path, name, () -> {
                        getServiceNumberFromRemote(serviceNumberEntity.getServiceNumberId());
                        return null;
                    });
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        XPermissionUtils.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void handleBackgroundEvent(EventMsg eventMsg) {
        if (eventMsg.getCode() == MsgConstant.INTERNET_STSTE_FILTER) {
            if ("true".equals(eventMsg.getData().toString())) {
                refreshData(RefreshSource.REMOTE);
            }
        }
    }
}
