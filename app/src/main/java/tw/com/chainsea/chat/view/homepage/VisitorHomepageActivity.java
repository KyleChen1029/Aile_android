package tw.com.chainsea.chat.view.homepage;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.bumptech.glide.Glide;
import com.google.common.base.Strings;

import java.util.Objects;

import cn.hadcn.davinci.image.base.ImageEntity;
import tw.com.chainsea.android.common.ui.UiHelper;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.account.UserType;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.http.ce.request.UploadManager;
import tw.com.chainsea.ce.sdk.reference.UserProfileReference;
import tw.com.chainsea.ce.sdk.service.UserProfileService;
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.base.Constant;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.databinding.ActivityVisitorHomepageBinding;
import tw.com.chainsea.chat.lib.NetworkUtils;
import tw.com.chainsea.chat.lib.ToastUtils;
import tw.com.chainsea.chat.lib.Tools;
import tw.com.chainsea.chat.service.ActivityTransitionsControl;
import tw.com.chainsea.chat.ui.activity.ClipImageActivity;
import tw.com.chainsea.chat.ui.dialog.InputDialogBuilder;
import tw.com.chainsea.chat.ui.dialog.SettingDialogBuilder;
import tw.com.chainsea.chat.util.DaVinci;
import tw.com.chainsea.chat.util.IntentUtil;
import tw.com.chainsea.chat.view.account.BackgroundCanvasTransformer;
import tw.com.chainsea.custom.view.alert.AlertView;

/**
 * 這是客戶首頁（Customer）命名上是 Visitor 容易混淆，注意
 */
public class VisitorHomepageActivity extends BaseHomepageActivity implements View.OnClickListener, View.OnTouchListener, LifecycleEventObserver {
    private static final int REQUEST_CARD = 1999;
    private ActivityVisitorHomepageBinding binding;
    private String accountId;
    private String roomId;
    private Dialog settingDialog;

    private String customerName = "", customerDesc = "";

    public ActivityResultLauncher<String[]> launcher;
    private ActivityResultLauncher<String> storagePermissionResult;


    @Override
    @SuppressLint({"ClickableViewAccessibility", "DiscouragedApi", "InternalInsetResource"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLifecycle().addObserver(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_visitor_homepage);
        int resourceId = getApplicationContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            binding.getRoot().setPadding(0, getApplicationContext().getResources().getDimensionPixelSize(resourceId), 0, 0);
        } else {
            binding.getRoot().setPadding(0, UiHelper.dip2px(this, 24), 0, 0);
        }

        accountId = getIntent().getStringExtra(BundleKey.ACCOUNT_ID.key());
        roomId = getIntent().getStringExtra(BundleKey.ROOM_ID.key());

        binding.layoutVisitor.setOnTouchListener(this);
        binding.layoutVisitor.setOnClickListener(l -> {
        });
        binding.layoutCard.setOnClickListener(this);
        binding.imgCard.setOnClickListener(this);
        binding.bgVisitor.setImageResource(BackgroundCanvasTransformer.getBackgroundCanvas(accountId).getResId());
        binding.btnBack.setOnClickListener(this);
        binding.btnSetting.setOnClickListener(this);
        binding.txtName.setOnClickListener(this);
        binding.btnEditName.setOnClickListener(this);
        binding.txtDesc.setOnClickListener(this);
        binding.btnEditDesc.setOnClickListener(this);
        binding.btnChat.setOnClickListener(this);
        binding.btnSetting.setOnClickListener(this);
        binding.btnEditCard.setOnClickListener(this);
        binding.btnRemoveCard.setOnClickListener(this);
        initActivityRequestLauncher();
        getUserProfile(null);
    }

    private void initActivityRequestLauncher() {
        //相簿權限
        launcher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                if (result.get(Manifest.permission.READ_MEDIA_IMAGES) != null && result.get(Manifest.permission.READ_MEDIA_VIDEO) != null && result.get(Manifest.permission.READ_MEDIA_AUDIO) != null) {
                    if (Objects.requireNonNull(result.get(Manifest.permission.READ_MEDIA_IMAGES)).equals(true) && Objects.requireNonNull(result.get(Manifest.permission.READ_MEDIA_VIDEO)).equals(true) && Objects.requireNonNull(result.get(Manifest.permission.READ_MEDIA_AUDIO)).equals(true)) {
                        showSelectDialog();
                    } else {
                        ToastUtils.showToast(this, getString(R.string.text_need_storage_permission));
                    }
                }
            });
        storagePermissionResult = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
            if (result) {
                showSelectDialog();
            } else {
                ToastUtils.showToast(this, getString(R.string.text_need_storage_permission));
            }
        });
    }

    private void updateCustomerName(String name) {
        UserProfileService.updateCustomerProfile(this, accountId, name, null);
        customerName = name;
        binding.txtName.setText(name);
    }

    private void updateCustomerDesc(String desc) {
        UserProfileService.updateCustomerProfile(this, accountId, null, desc);
        customerDesc = desc;
        binding.txtDesc.setText(desc);
    }

    private void removeCustomerBusinessCard() {
        UserProfileService.removeCustomerBusinessCard(this, accountId);
        binding.imgCard.setImageBitmap(null);
        shiftY = 0;
        binding.layoutEdit.setVisibility(View.GONE);
    }

    private void updateCustomerBusinessCard(String imageName) {
        String tokenId = TokenPref.getInstance(this).getTokenId();
        String path = DaVinci.with().getImageLoader().getAbsolutePath(imageName);
        UploadManager.getInstance().uploadBusinessCard(this, tokenId, accountId, path, Tools.getSize(path), new UploadManager.OnUploadAvatarListener() {
            @Override
            public void onUploadSuccess(String url) {
                Toast.makeText(VisitorHomepageActivity.this, getString(R.string.text_upload_avatar_success) + url, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUploadFailed(String reason) {
                Toast.makeText(VisitorHomepageActivity.this, getString(R.string.text_upload_image_failure) + reason, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getUserProfileFromRemote() {
        UserProfileService.getProfileFromRemote(this, accountId, new ServiceCallBack<>() {
            @Override
            public void complete(UserProfileEntity entity, RefreshSource source) {
                getUserProfile(entity);
            }

            @Override
            public void error(String message) {
                Toast.makeText(VisitorHomepageActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getUserProfile(UserProfileEntity entity) {
        if(entity == null) {
            // get userProfile from local db
            UserProfileEntity customerInfo = UserProfileReference.findCustomerInfoByAccountId(accountId);
            if (customerInfo != null) {
                customerName = Strings.isNullOrEmpty(customerInfo.getCustomerName()) ? customerInfo.getNickName() : customerInfo.getCustomerName();
                customerDesc = Strings.isNullOrEmpty(customerInfo.getCustomerDescription()) ? "" : customerInfo.getCustomerDescription();
                settingDialog = new SettingDialogBuilder(VisitorHomepageActivity.this, customerInfo).create();
                binding.txtName.setText(customerName);
                binding.txtDesc.setText(customerDesc);
                binding.imgType.setImageResource(UserType.CONTACT.equals(customerInfo.getUserType()) ? R.drawable.ic_customer_15dp : R.drawable.ic_visitor_15dp);
                binding.imgAvatar.loadAvatarIcon(customerInfo.getAvatarId(), customerName, customerInfo.getId());
            }
        } else {
            // get userProfile from remote
            settingDialog = new SettingDialogBuilder(VisitorHomepageActivity.this, entity).create();
            binding.imgAvatar.loadAvatarIcon(entity.getAvatarId(), entity.getNickName(), entity.getId());
            customerName = Strings.isNullOrEmpty(entity.getCustomerName()) ? entity.getNickName() : entity.getCustomerName();
            customerDesc = Strings.isNullOrEmpty(entity.getCustomerDescription()) ? "" : entity.getCustomerDescription();
            binding.txtName.setText(customerName);
            binding.txtDesc.setText(customerDesc);
            binding.imgType.setImageResource(UserType.CONTACT.equals(entity.getUserType()) ? R.drawable.ic_customer_15dp : R.drawable.ic_visitor_15dp);
            if (!Strings.isNullOrEmpty(entity.getCustomerBusinessCardUrl())) {
                try {
                    Glide.with(VisitorHomepageActivity.this).load(entity.getCustomerBusinessCardUrl()).into(binding.imgCard);
                } catch (Exception ignored) {
                }

                binding.layoutEdit.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.equals(binding.btnBack)) {
            finish();
            overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
        } else if (v.equals(binding.txtName) || v.equals(binding.btnEditName)) {
            Dialog editNameDialog = new InputDialogBuilder(VisitorHomepageActivity.this)
                .setTitle(getString(R.string.text_input_nickname))
                .setInputData(customerName)
                .setMaxLength(20)
                .setOnConfirmListener(this::updateCustomerName)
                .create();
            if (!editNameDialog.isShowing()) {
                editNameDialog.show();
            }
        } else if (v.equals(binding.txtDesc) || v.equals(binding.btnEditDesc)) {
            Dialog editDescDialog = new InputDialogBuilder(VisitorHomepageActivity.this)
                .setTitle(getString(R.string.text_input_remark))
                .setInputData(customerDesc)
                .setMaxLength(20)
                .setOnConfirmListener(this::updateCustomerDesc)
                .create();
            if (!editDescDialog.isShowing()) {
                editDescDialog.show();
            }
        } else if (v.equals(binding.btnChat)) {
            ActivityTransitionsControl.navigateToChat(VisitorHomepageActivity.this, roomId, (intent, s) -> IntentUtil.INSTANCE.start(this, intent));
        } else if (v.equals(binding.btnSetting)) {
            if (!settingDialog.isShowing()) settingDialog.show();
        } else if (v.equals(binding.btnRemoveCard)) {
            removeCustomerBusinessCard();
        } else if (v.equals(binding.layoutCard) || v.equals(binding.btnEditCard) || v.equals(binding.imgCard)) {
            checkPermission();
        }
    }

    private void showSelectDialog() {
        new AlertView.Builder()
            .setContext(this)
            .setStyle(AlertView.Style.ActionSheet)
            .setOthers(new String[]{"相簿", "照相機"})
            .setCancelText("取消")
            .setOnItemClickListener((o, position) -> {
                if (position == 0) {
                    Intent intent = new Intent(this, ClipImageActivity.class);
                    intent.putExtra(Constant.INTENT_IMAGE_GET_WAY, Constant.INTENT_CODE_ALBUM);
                    intent.putExtra(Constant.INTENT_IMAGE_CONFIRM_TYPE, Constant.INTENT_CODE_NO_CROP);
                    startActivityForResult(intent, REQUEST_CARD);
                } else if (position == 1) {
                    Intent intent = new Intent(this, ClipImageActivity.class);
                    intent.putExtra(Constant.INTENT_IMAGE_GET_WAY, Constant.INTENT_CODE_CAMERA);
                    intent.putExtra(Constant.INTENT_IMAGE_CONFIRM_TYPE, Constant.INTENT_CODE_CAMERA);
                    startActivityForResult(intent, REQUEST_CARD);
                }
            }).build()
            .setCancelable(true)
            .show();
    }

    private float preY;
    private float shiftY;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                preY = event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                if (shiftY > binding.layoutCard.getHeight() / 2.f) {
                    binding.layoutVisitor.offsetTopAndBottom((int) (binding.layoutCard.getHeight() - shiftY));
                    shiftY = binding.layoutCard.getHeight();
                } else if (shiftY < binding.layoutCard.getHeight() / 2.f) {
                    binding.layoutVisitor.offsetTopAndBottom((int) -shiftY + 8);
                    shiftY = 8;
                } else {
                    v.performClick();
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float moveY = event.getRawY() - preY;
                preY = event.getRawY();
                shiftY += moveY;
                if (moveY > 0 && (shiftY > binding.layoutCard.getHeight())) {
                    shiftY -= moveY;
                } else if (moveY < 0 && (shiftY < 0)) {
                    shiftY -= moveY;
                } else {
                    binding.layoutVisitor.offsetTopAndBottom((int) moveY);
                }
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CARD) {
            if (NetworkUtils.isNetworkAvailable(this)) {
                binding.imgCard.setImageBitmap(null);
                String imageName = data.getStringExtra(BundleKey.RESULT_PIC_URI.key());
                ImageEntity imageEntity = DaVinci.with().getImageLoader().getImage(imageName);
                binding.imgCard.setImageBitmap(imageEntity.getBitmap());
                binding.layoutEdit.setVisibility(View.VISIBLE);
                updateCustomerBusinessCard(imageName);
                shiftY = 0;
            } else {
                ToastUtils.showToast(this, getResources().getString(R.string.network_error));
            }
        }
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        if (event == Lifecycle.Event.ON_CREATE) {
            getUserProfileFromRemote();
        }
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            String[] permissions = new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.READ_MEDIA_VIDEO};
            for (String permission : permissions) {
                int isPermissionGranted = checkSelfPermission(permission);
                if (isPermissionGranted == PackageManager.PERMISSION_DENIED) {
                    launcher.launch(new String[]{Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_AUDIO,
                        Manifest.permission.READ_MEDIA_VIDEO});
                    return;
                }
            }
            showSelectDialog();
        } else {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                showSelectDialog();
            } else {
                storagePermissionResult.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
    }

}
