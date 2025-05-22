package tw.com.chainsea.chat.view.homepage;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import cn.hadcn.davinci.image.base.ImageEntity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.ce.sdk.bean.PicSize;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity;
import tw.com.chainsea.ce.sdk.database.DBContract;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.database.sp.UserPref;
import tw.com.chainsea.ce.sdk.event.EventBusUtils;
import tw.com.chainsea.ce.sdk.event.EventMsg;
import tw.com.chainsea.ce.sdk.event.MsgConstant;
import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.network.NetworkManager;
import tw.com.chainsea.ce.sdk.network.model.common.CommonResponse;
import tw.com.chainsea.ce.sdk.network.services.TenantService;
import tw.com.chainsea.ce.sdk.reference.ServiceNumberReference;
import tw.com.chainsea.ce.sdk.service.AvatarService;
import tw.com.chainsea.ce.sdk.service.PhotoService;
import tw.com.chainsea.ce.sdk.service.UserProfileService;
import tw.com.chainsea.ce.sdk.service.listener.ProgressServiceCallBack;
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.base.Constant;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.databinding.ActivitySelfInformationHomepageBinding;
import tw.com.chainsea.chat.dialog.QuitTenantConfirmDialog;
import tw.com.chainsea.chat.service.ActivityTransitionsControl;
import tw.com.chainsea.chat.ui.activity.ChatNormalActivity;
import tw.com.chainsea.chat.ui.activity.ClipImageActivity;
import tw.com.chainsea.chat.ui.dialog.BottomSheetDialogBuilder;
import tw.com.chainsea.chat.ui.fragment.BaseFragment;
import tw.com.chainsea.chat.ui.utils.permissionUtils.DialogUtil;
import tw.com.chainsea.chat.ui.utils.permissionUtils.RequestCodeManger;
import tw.com.chainsea.chat.ui.utils.permissionUtils.XPermissionUtils;
import tw.com.chainsea.chat.util.AvatarKit;
import tw.com.chainsea.chat.util.DaVinci;
import tw.com.chainsea.chat.util.IntentUtil;
import tw.com.chainsea.chat.util.NameKit;
import tw.com.chainsea.chat.util.NoDoubleClickListener;
import tw.com.chainsea.chat.util.SystemKit;
import tw.com.chainsea.chat.view.account.BackgroundCanvasTransformer;
import tw.com.chainsea.chat.view.account.UserInformationHomepageActivity;
import tw.com.chainsea.chat.view.qrcode.ServiceNumberQrCodeActivity;
import tw.com.chainsea.custom.view.alert.AlertView;
import tw.com.chainsea.custom.view.alert.OnItemClickListener;

/**
 * Create by evan on 2/20/21
 *
 * @author Evan Wang
 * @date 2/20/21
 */
public class SelfInformationHomepageActivity extends UserInformationHomepageActivity {
    private ActivitySelfInformationHomepageBinding binding;
    private final AvatarKit avatarKit = new AvatarKit();
    private final NameKit nameKit = new NameKit();

    boolean CAN_NEXT = false;

    private final List<BaseFragment<TabType, ?>> fragments = Lists.newArrayList();

    private static final int HOMEPAGE_BACKGROUND_PICS_RESULT_CODE = 0x138D;
    private static final int USER_AVATAR_RESULT_CODE = 0x138E;
    private ServiceNumberEntity bossServiceNumberEntity;
    private UserProfileEntity userProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_self_information_homepage);
        EventBusUtils.register(this);
        binding.clRoot.setPadding(0, getStatusHeight(), 0, 0);
        bossServiceNumberEntity = ServiceNumberReference.findSelfBossServiceNumber();
        getUserProfile(RefreshSource.LOCAL);
        initListener();
    }

    private void initListener() {
        binding.leftAction.setOnClickListener(this::doLeftAction);
        binding.ivBackgroundCamera.setOnClickListener(this::doBackgroundCameraAction);
        binding.ivAvatarCamera.setOnClickListener(this::doAvatarCameraAction);
        binding.tvChat.setOnClickListener(this::doSelfChartAction);
        if (bossServiceNumberEntity != null) {
            binding.tvQrCode.setVisibility(View.VISIBLE);
            binding.tvQrCode.setOnClickListener(this::doMyQRCodeAction);
        } else {
            binding.tvQrCode.setVisibility(View.GONE);
        }
        binding.llNameBox.setOnClickListener(this::changeNickName);
        binding.llMoodBox.setOnClickListener(this::changeMood);
        binding.tvBusiness.setOnClickListener(this::doOpenBusiness);
        binding.ivMore.setOnClickListener(this::doMoreAction);
    }

    protected void getUserProfile(RefreshSource source) {
        UserProfileService.getSelfProfile(this, source, new ServiceCallBack<UserProfileEntity, RefreshSource>() {
            @Override
            public void complete(UserProfileEntity userProfileEntity, RefreshSource source) {
                userProfile = userProfileEntity;
                bindingInformation(userProfile);
            }

            @Override
            public void error(String message) {

            }
        });
    }

    private void bindingInformation(UserProfileEntity profile) {
        this.profile = profile;
        // init Home page Background Pics
        initBackgroundPics();
        String avatarId = profile.getAvatarId();
//        avatarKit.loadCEAvatar(profile.getAvatarId(), binding.civAccountAvatar);
        String name = profile.getNickName();
        String shortName = nameKit.getAvatarName(name);
        if (Strings.isNullOrEmpty(avatarId) || AvatarKit.DEFAULT_AVATAR_ID.equals(avatarId)) {
            binding.civAccountAvatar.setVisibility(View.GONE);
            binding.tvAvatar.setVisibility(View.VISIBLE);
            binding.tvAvatar.setText(shortName);
            GradientDrawable gradientDrawable = (GradientDrawable) binding.tvAvatar.getBackground();
            gradientDrawable.setColor(Color.parseColor(nameKit.getBackgroundColor(shortName)));
        } else {
            avatarKit.loadCEAvatar(avatarId, binding.civAccountAvatar, binding.tvAvatar, name);
        }
        if (Strings.isNullOrEmpty(profile.getAlias())) {
            binding.tvName.setText(profile.getNickName());
        } else {
            binding.tvName.setText(String.format("%s(%s)", profile.getNickName(), profile.getAlias()));
        }
        if (Strings.isNullOrEmpty(profile.getDuty())) {
            binding.tvDutyName.setText(profile.getDepartment());
        } else {
            binding.tvDutyName.setText(String.format("%s/%s", profile.getDepartment(), profile.getDuty()));
        }
        if (Strings.isNullOrEmpty(profile.getMood())) {
            binding.tvMood.setText("此處可編輯心情");
        } else {
            binding.tvMood.setText(profile.getMood());
        }

        //文字頭像暫不顯示圖片
        binding.tvAvatar.setOnClickListener(null);
        binding.civAccountAvatar.setOnClickListener(clickListener);

    }

    private final NoDoubleClickListener clickListener = new NoDoubleClickListener() {
        @Override
        protected void onNoDoubleClick(View v) {
            String avatarUrl = TokenPref.getInstance(SelfInformationHomepageActivity.this).getCurrentTenantUrl()
                + "/openapi/base/avatar/view?args=%7B%22id%22:%22" + profile.getAvatarId() + "%22,%20%22size%22:%22m%22%7D";
            if (v.equals(binding.civAccountAvatar) || v.equals(binding.tvAvatar)) {
                String turl = AvatarService.getAvatarUrl(SelfInformationHomepageActivity.this, profile.getAvatarId(), PicSize.SMALL);
                ActivityTransitionsControl.navigateToPhotoGallery(SelfInformationHomepageActivity.this, avatarUrl, turl, (intent, s1) -> {
                    startActivity(intent);
                });
            }
        }
    };


    private void initBackgroundPics() {
        String picUrl = UserPref.getInstance(this).getUserHomePageBackgroundUrl();
        String remoteUrl = "";
        BackgroundCanvasTransformer.Res res = BackgroundCanvasTransformer.getBackgroundCanvas(profile.getId());
        if (!Strings.isNullOrEmpty(picUrl) || !profile.getHomePagePics().isEmpty()) {
            if (!profile.getHomePagePics().isEmpty()) {
                UserProfileEntity.HomePagePic pic = Iterables.getLast(profile.getHomePagePics());
                UserPref.getInstance(this).setUserHomePageBackgroundUrl(pic.getPicUrl());
                String picurl = pic.getPicUrl();
                remoteUrl = picurl.startsWith("http") ? picurl : TokenPref.getInstance(this).getCurrentTenantUrl() + ApiPath.ROUTE + pic.getPicUrl();
            }

            PhotoService.post(this, remoteUrl, binding.ivBackgroundPhoto, res.getResId(), new ProgressServiceCallBack<Drawable, RefreshSource>() {
                @Override
                public void progress(float progress, long total) {

                }

                @Override
                public void error(String message) {
                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> binding.ivBackgroundPhoto.setImageResource(res.getResId()));
                }

                @Override
                public void complete(Drawable drawable, RefreshSource source) {
                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> binding.ivBackgroundPhoto.setImageDrawable(drawable));
                }
            });
        } else {
            binding.ivBackgroundPhoto.setImageResource(res.getResId());
        }
    }

    @Override
    protected void switchMode(PageMode pageMode) {

    }

    private void changeNickName(View v) {
        String nickName = binding.tvName.getText().toString();
        new AlertView.Builder()
            .setContext(this)
            .setStyle(AlertView.Style.EditAlert)
            .setTitle("")
            .setMessage(getString(R.string.text_input_nickname))
            .setInputDatas(new String[]{Strings.isNullOrEmpty(nickName) ? getString(R.string.text_input_nickname) : nickName}, Strings.isNullOrEmpty(nickName))
            .setOnItemClickListener((OnItemClickListener<List<EditText>>) (o, position) -> {
                if (position == 0) {
                    String text = o.get(0).getText().toString();
                    if (text.length() > 20) {
                        Toast.makeText(this, getString(R.string.name_length_tip), Toast.LENGTH_SHORT).show();
                    } else if (text.contains(" ")) {
                        Toast.makeText(this, "不能輸入特殊字符，如空格", Toast.LENGTH_SHORT).show();
                    } else {
                        updateProfile(text);
                    }
                }
            })
            .build()
            .setCancelable(true)
            .setOnDismissListener(o -> CAN_NEXT = true)
            .show();
    }

    private void changeMood(View v) {
        String mood = binding.tvMood.getText().toString();
        new AlertView.Builder()
            .setContext(this)
            .setStyle(AlertView.Style.EditAlert)
            .setTitle("")
            .setMessage("輸入心情")
            .setInputDatas(new String[]{Strings.isNullOrEmpty(mood) ? "輸入心情" : mood}, Strings.isNullOrEmpty(mood))
            .setOnItemClickListener((OnItemClickListener<List<EditText>>) (o, position) -> {
                if (position == 0) {
                    String text = o.get(0).getText().toString();
                    if (text.length() > 100) {
                        Toast.makeText(this, getString(R.string.mood_length_tip), Toast.LENGTH_SHORT).show();
                    } else if (text.contains(" ")) {
                        Toast.makeText(this, "不能輸入特殊字符，如空格", Toast.LENGTH_SHORT).show();
                    } else {
                        updateMood(text);
                    }
                }
            })
            .build()
            .setCancelable(true)
            .setOnDismissListener(o -> CAN_NEXT = true)
            .show();
    }

    private void doOpenBusiness(View v) {
        if (bossServiceNumberEntity != null) {
            Intent intent = new Intent(this, BossServiceNumberHomepageActivity.class);
            intent.putExtra(BundleKey.BROADCAST_ROOM_ID.key(), bossServiceNumberEntity.getBroadcastRoomId());
            intent.putExtra(BundleKey.SERVICE_NUMBER_ID.key(), bossServiceNumberEntity.getServiceNumberId());
            startActivity(intent);
        } else {
            Toast.makeText(this, "您還未申請商務號，請聯繫管理員", Toast.LENGTH_SHORT).show();
        }
    }

    private void doLeftAction(View v) {
        finish();
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
    }

    private void doBackgroundCameraAction(View view) {
        doOpenCamera(HOMEPAGE_BACKGROUND_PICS_RESULT_CODE);
    }

    private void doAvatarCameraAction(View view) {
        doOpenCamera(USER_AVATAR_RESULT_CODE);
    }

    private void doSelfChartAction(View view) {
        String roomId = UserPref.getInstance(this).getPersonRoomId();
        Bundle bundle = new Bundle();
        bundle.putString(BundleKey.EXTRA_SESSION_ID.key(), roomId);
        IntentUtil.INSTANCE.startIntent(this, ChatNormalActivity.class, bundle);
    }

    private void doMyQRCodeAction(View view) {
        IntentUtil.INSTANCE.startIntent(this, ServiceNumberQrCodeActivity.class, null);
    }

    private void doMoreAction(View view) {
        new BottomSheetDialogBuilder(this, getLayoutInflater())
            .getQuitTenantDialog(() -> {
                showQuitTenantConfirmDialog();
                return null;
            }).show();
    }

    private void showQuitTenantConfirmDialog() {
        new QuitTenantConfirmDialog(this, () -> {
            quitTenant();
            return null;
        }).show();
    }

    private void quitTenant() {
        NetworkManager.INSTANCE.provideRetrofit(this).create(TenantService.class)
            .quitTenant().enqueue(new Callback<CommonResponse<String>>() {
                @Override
                public void onResponse(@NonNull Call<CommonResponse<String>> call, @NonNull Response<CommonResponse<String>> response) {
                    SystemKit.quitTenant(SelfInformationHomepageActivity.this);
                }

                @Override
                public void onFailure(@NonNull Call<CommonResponse<String>> call, @NonNull Throwable t) {
                    new AlertView.Builder()
                        .setContext(SelfInformationHomepageActivity.this)
                        .setStyle(AlertView.Style.Alert)
                        .setOthers(new String[]{getString(R.string.alert_cancel), getString(R.string.alert_confirm)})
                        .setTitle("退出團隊失敗")
                        .setMessage("退出前，請將管理服務號的擁有權移交給其它服務成員。")
                        .build()
                        .setCancelable(true).show();
                }
            });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getUserProfile(RefreshSource.REMOTE);
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
                case USER_AVATAR_RESULT_CODE:
                    UserProfileService.uploadSelfAvatar(this, entity.getBitmap().getHeight(), path, name);
                    break;
                case HOMEPAGE_BACKGROUND_PICS_RESULT_CODE:
                    UserProfileService.uploadSelfHomepagePics(this, path, name);
                    break;
            }
        }
    }

    private void updateMood(String mood) {
        ApiManager.updateMood(this, mood, new ApiListener<String>() {
            @Override
            public void onSuccess(String s) {
                binding.tvMood.setText(mood);
                DBManager.getInstance().updateUserField(userProfile.getId(), DBContract.UserProfileEntry.COLUMN_MOOD, mood);
                Toast.makeText(SelfInformationHomepageActivity.this, "修改心情成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(String errorMessage) {
                if (!Strings.isNullOrEmpty(errorMessage)) {
                    Toast.makeText(SelfInformationHomepageActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateProfile(String nickName) {
        if (userProfile != null) {
            if (!Strings.isNullOrEmpty(nickName)) {
                userProfile.setNickName(nickName);
            }

            ApiManager.updateProfile(SelfInformationHomepageActivity.this, userProfile, new ApiListener<String>() {
                @Override
                public void onSuccess(String s) {
                    if (!Strings.isNullOrEmpty(nickName)) {
                        DBManager.getInstance().updateFriendField(userProfile.getId(), DBContract.UserProfileEntry.COLUMN_NICKNAME, userProfile.getNickName());
                        binding.tvName.setText(nickName);
                        Toast.makeText(SelfInformationHomepageActivity.this, "修改暱稱成功", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailed(String errorMessage) {
                    if (!Strings.isNullOrEmpty(errorMessage)) {
                        Toast.makeText(SelfInformationHomepageActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBusUtils.unregister(this);
    }

    private void doOpenCamera(int requestCode) {
        String[] permission;
        permission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ? new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA} : new String[]{Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.CAMERA};
        XPermissionUtils.requestPermissions(SelfInformationHomepageActivity.this, RequestCodeManger.CAMERA, permission,
            new XPermissionUtils.OnPermissionListener() {
                @Override
                public void onPermissionGranted() {
//                        if (PermissionHelper.isCameraEnable()) {
                    showPicDialog(requestCode);
                    CELog.e("Did not receive permission callback");
//                        } else {
//                            DialogUtil.showPermissionManagerDialog(SelfInformationHomepageActivity.this, getString(R.string.warning_camera));
//                        }
                }

                @Override
                public void onPermissionDenied(final String[] deniedPermissions, boolean alwaysDenied) {
                    Toast.makeText(SelfInformationHomepageActivity.this, "獲取相機權限失敗", Toast.LENGTH_SHORT).show();
                    // Don't ask again after rejection -> prompt to jump to settings
                    if (alwaysDenied) {
                        DialogUtil.showPermissionManagerDialog(SelfInformationHomepageActivity.this, getString(R.string.warning_camera));
                    } else {    // 拒绝 -> 提示此公告的意义，并可再次尝试获取权限
                        new AlertDialog.Builder(SelfInformationHomepageActivity.this).setTitle("溫馨提示")
                            .setMessage("我們需要相機權限才能正常使用該功能")
                            .setNegativeButton(getString(R.string.alert_cancel), null)
                            .setPositiveButton("驗證權限", (dialog, which) -> XPermissionUtils.requestPermissionsAgain(SelfInformationHomepageActivity.this, deniedPermissions,
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
                getUserProfile(RefreshSource.REMOTE);
                break;
            case MsgConstant.ACCOUNT_REFRESH_FILTER:
            case MsgConstant.SELF_REFRESH_FILTER:
                getUserProfile(RefreshSource.LOCAL);
                for (BaseFragment<TabType, ?> f : fragments) {
                    f.refresh();
                }
                break;
        }
    }
}
