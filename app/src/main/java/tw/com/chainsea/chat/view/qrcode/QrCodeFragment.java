package tw.com.chainsea.chat.view.qrcode;

import static tw.com.chainsea.chat.util.FileUtils.mergeQrCode;

import android.Manifest;
import android.app.Activity;
import android.app.RecoverableSecurityException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;

import com.google.common.base.Strings;

import java.io.File;
import java.util.List;
import java.util.Objects;

import cn.hadcn.davinci.image.base.ImageEntity;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity;
import tw.com.chainsea.ce.sdk.database.DBContract;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.http.cp.respone.RelationTenant;
import tw.com.chainsea.ce.sdk.reference.ServiceNumberReference;
import tw.com.chainsea.ce.sdk.service.UserProfileService;
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.base.Constant;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.databinding.FragmentQrCodeBinding;
import tw.com.chainsea.chat.lib.ToastUtils;
import tw.com.chainsea.chat.presenter.QrCodePresenter;
import tw.com.chainsea.chat.qrcode.QrCodeKit;
import tw.com.chainsea.chat.ui.activity.ClipImageActivity;
import tw.com.chainsea.chat.ui.fragment.BaseFragment;
import tw.com.chainsea.chat.ui.ife.IQrCodeView;
import tw.com.chainsea.chat.ui.utils.permissionUtils.DialogUtil;
import tw.com.chainsea.chat.ui.utils.permissionUtils.RequestCodeManger;
import tw.com.chainsea.chat.ui.utils.permissionUtils.XPermissionUtils;
import tw.com.chainsea.chat.util.DaVinci;
import tw.com.chainsea.chat.util.DownloadUtil;
import tw.com.chainsea.chat.util.IntentUtil;
import tw.com.chainsea.chat.zxing.encoding.EncodingHandler;
import tw.com.chainsea.custom.view.alert.AlertView;
import tw.com.chainsea.custom.view.alert.OnItemClickListener;


public class QrCodeFragment extends BaseFragment implements IQrCodeView {
    private FragmentQrCodeBinding binding;
    private ServiceNumberEntity bossServiceNumberEntity;
    private UserProfileEntity userProfile;
    private static final int USER_AVATAR_RESULT_CODE = 0x138E;
    private QrCodePresenter qrCodePresenter;
    private ActivityResultLauncher<String> downloadPermissionResult;
    private ActivityResultLauncher<String[]> launcher;
    private boolean isShareQrCode = false; //判斷是點擊下載或分享
    private Uri deleteUri;
    private int qrCodeType;
    private RelationTenant relationTenant = null;
    private final ActivityResultLauncher<IntentSenderRequest> intentSenderLauncher = registerForActivityResult(
        new ActivityResultContracts.StartIntentSenderForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                // 用戶授權成功，重試刪除操作
                requireActivity().getContentResolver().delete(deleteUri, null, null);
                startToMergeQrCode();
            } else {
                // 用戶拒絕授權
                Toast.makeText(requireActivity(), getString(R.string.text_allow_modify_permission_to_share_this_file), Toast.LENGTH_SHORT).show();
            }
        }
    );

    public QrCodeFragment() {

    }

    public static QrCodeFragment newInstance() {
        return new QrCodeFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_qr_code, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        qrCodePresenter = new QrCodePresenter(this);
        Intent intent = requireActivity().getIntent();
        if (intent != null) {
            qrCodeType = intent.getIntExtra(BundleKey.TYPE.key(), 0);
            bossServiceNumberEntity = ServiceNumberReference.findSelfBossServiceNumber();
            getUserProfile();
        }
        binding.tvTitle.setText(getString(R.string.invite_join_group));
        initListener();

        downloadPermissionResult = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) doDownloadQrCode();
            else ToastUtils.showToast(requireContext(), requireContext().getString(R.string.text_need_storage_permission));
        });
        launcher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                if (result.get(Manifest.permission.READ_MEDIA_IMAGES) != null && result.get(Manifest.permission.READ_MEDIA_VIDEO) != null) {
                    if (Objects.requireNonNull(result.get(Manifest.permission.READ_MEDIA_IMAGES)).equals(true) && Objects.requireNonNull(result.get(Manifest.permission.READ_MEDIA_VIDEO)).equals(true)) {
                        doDownloadQrCode();
                    } else {
                        ToastUtils.showToast(requireContext(), getString(R.string.text_need_storage_permission));
                    }
                }
            });
    }

    private void bindGroupData(UserProfileEntity profile) {
        binding.tvTitle.setText(getString(R.string.invite_join_group));
        binding.tvNotice.setText(getString(R.string.text_scan_tenant_qrcode_join));
        binding.ivEdit.setVisibility(View.GONE);
        binding.ivCamera.setVisibility(View.GONE);
        binding.ivNameEdit.setVisibility(View.GONE);
        relationTenant = TokenPref.getInstance(requireActivity()).getCpCurrentTenant();
        String name = relationTenant.getTenantName();
        binding.ivAvatar.loadTenantAvatarIcon(relationTenant);
        binding.icLogo.loadTenantAvatarIcon(relationTenant);
        binding.tvName.setText(name);

        binding.tvDescription.setVisibility(View.INVISIBLE);
        binding.tvDuty.setVisibility(View.GONE);

        setQrCode(
            TokenPref.getInstance(this.getContext()).getCurrentTenantCode(),
            profile.getId(),
            profile.getNickName()
        );
    }

    private void setQrCode(String tenantCode, String userId, String nickName) {
        String qrcode = new QrCodeKit().getUserInfoQrCode(
            tenantCode,
            TokenPref.getInstance(this.getContext()).getCpAccountId(),
            TokenPref.getInstance(this.getContext()).getCpCurrentTenant().getTenantName(),
            userId, nickName
        );
        try {
            Bitmap codeBitmap = EncodingHandler.createQRCode(qrcode, 600, 600, null);
            binding.qrCode.setImageBitmap(codeBitmap);
        } catch (Exception e) {
            CELog.d("save qrCode id error " + e.getMessage());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    private void initListener() {
        binding.ivBack.setOnClickListener(v -> requireActivity().onBackPressed());

        binding.ivCamera.setOnClickListener(this::doCameraAction);
        binding.tvDownload.setOnClickListener(this::doDownloadAction);
        binding.tvSendCode.setOnClickListener(this::doSendInvitationCode);
        //binding.tvScan.setOnClickListener(this::doScannerAction);
        binding.tvShare.setOnClickListener(this::doShareAction);
        binding.ivEdit.setOnClickListener(this::changeNickName);
    }

    private void changeNickName(View v) {
        String nickName = binding.tvName.getText().toString();
        new AlertView.Builder()
            .setContext(requireActivity())
            .setStyle(AlertView.Style.EditAlert)
            .setTitle("")
            .setMessage(getString(R.string.text_input_nickname))
            .setInputDatas(new String[]{Strings.isNullOrEmpty(nickName) ? getString(R.string.text_input_nickname) : nickName}, Strings.isNullOrEmpty(nickName))
            .setOnItemClickListener((OnItemClickListener<List<EditText>>) (o, position) -> {
                if (position == 0) {
                    String text = o.get(0).getText().toString();
                    if (text.length() > 20) {
                        Toast.makeText(requireActivity(), getString(R.string.name_length_tip), Toast.LENGTH_SHORT).show();
                    } else if (text.contains(" ")) {
                        Toast.makeText(requireActivity(), "不能輸入特殊字符，如空格", Toast.LENGTH_SHORT).show();
                    } else {
                        updateProfile(text);
                    }
                }
            })
            .build()
            .setCancelable(true)
            .show();
    }

    private void updateProfile(String nickName) {
        if (userProfile != null) {
            if (!Strings.isNullOrEmpty(nickName)) {
                userProfile.setNickName(nickName);
            }

            ApiManager.updateProfile(requireActivity(), userProfile, new ApiListener<String>() {
                @Override
                public void onSuccess(String s) {
                    if (!Strings.isNullOrEmpty(nickName)) {
                        DBManager.getInstance().updateFriendField(userProfile.getId(), DBContract.UserProfileEntry.COLUMN_NICKNAME, userProfile.getNickName());
                        binding.tvName.setText(nickName);
                        Toast.makeText(requireActivity(), "修改暱稱成功", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailed(String errorMessage) {
                    if (!Strings.isNullOrEmpty(errorMessage)) {
                        Toast.makeText(requireActivity(), errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


    private void doCameraAction(View view) {
        doOpenCamera(USER_AVATAR_RESULT_CODE);
    }

    private void doDownloadAction(View view) {
        isShareQrCode = false;
        if (checkPermission())
            doDownloadQrCode();
        else {
            requirePermission();
        }
    }

    private void doDownloadQrCode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = requireActivity().getContentResolver();
            Cursor cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DISPLAY_NAME + "=?",
                new String[]{getFileName()},
                null
            );

            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        // 如果存在相同名稱的檔案，則刪除該檔案
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                        deleteUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                        try {
                            resolver.delete(deleteUri, null, null);
                        } catch (RecoverableSecurityException e) {
                            IntentSender intentSender = e.getUserAction().getActionIntent().getIntentSender();
                            IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(intentSender).build();
                            intentSenderLauncher.launch(intentSenderRequest);
                        }
                    }
                } catch (Exception e) {
                    Log.e("QrCodeFragment", "err=" + e.getMessage());
                } finally {
                    startToMergeQrCode();
                    cursor.close();
                }
            }
        } else {
            startToMergeQrCode();
        }
    }

    private String getFileName() {
        String fileName = "";
        if (qrCodeType == QrCodeType.person) {
            fileName = "aile_" + relationTenant.getTenantId() + ".jpg";
        } else {
            fileName = "aile_" + bossServiceNumberEntity.getServiceNumberId() + ".jpg";
        }
        return fileName;
    }

    private void startToMergeQrCode() {
        binding.clQrcode.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(binding.clQrcode.getDrawingCache());
        mergeQrCode(
            bitmap,
            getFileName(),
            requireActivity(),
            isShareQrCode,
            requireContext().getString(R.string.photo_save_to_phone)
        );
        binding.clQrcode.destroyDrawingCache();
    }

    private void doSendInvitationCode(View view) {
        qrCodePresenter.getInvitationCode(requireActivity());
    }

    private void doShareAction(View view) {
        isShareQrCode = true;
        if (checkPermission()) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            try {
                File file = new File(DownloadUtil.INSTANCE.getDownloadImageDir(), "aile_" + relationTenant.getTenantId() + ".jpg");
                if (file.exists()) {
                    shareIntent.setType("image/*");
                    Uri photoUri = FileProvider.getUriForFile(requireActivity(), requireActivity().getPackageName() + ".fileprovider", file);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, photoUri);
                    IntentUtil.INSTANCE.start(requireContext(), shareIntent);
                } else {
                    doDownloadQrCode();
                }
            } catch (Exception e) {
                Log.e("doShareAction", e.getMessage());
            }
        } else {
            requirePermission();
        }
    }

    private void requirePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(new String[]{Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO});
        } else
            downloadPermissionResult.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            String[] permissions = new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO};
            for (String permission : permissions) {
                int isPermissionGranted = requireContext().checkSelfPermission(permission);
                if (isPermissionGranted == PackageManager.PERMISSION_DENIED) {
                    return false;
                }
            }
        } else {
            return requireContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    private void getUserProfile() {
        UserProfileService.getSelfProfile(requireActivity(), RefreshSource.LOCAL, new ServiceCallBack<UserProfileEntity, RefreshSource>() {
            @Override
            public void complete(UserProfileEntity userProfileEntity, RefreshSource source) {
                userProfile = userProfileEntity;
                bindGroupData(userProfile);
            }

            @Override
            public void error(String message) {

            }
        });
    }

    private void doOpenCamera(int requestCode) {
        XPermissionUtils.requestPermissions(requireActivity(), RequestCodeManger.CAMERA, new String[]{Manifest.permission.CAMERA},
            new XPermissionUtils.OnPermissionListener() {
                @Override
                public void onPermissionGranted() {
                    showPicDialog(requestCode);
                }

                @Override
                public void onPermissionDenied(final String[] deniedPermissions, boolean alwaysDenied) {
                    Toast.makeText(requireActivity(), "獲取相機權限失敗", Toast.LENGTH_SHORT).show();
                    // Don't ask again after rejection -> prompt to jump to settings
                    if (alwaysDenied) {
                        DialogUtil.showPermissionManagerDialog(requireActivity(), getString(R.string.warning_camera));
                    } else {    // 拒绝 -> 提示此公告的意义，并可再次尝试获取权限
                        new AlertDialog.Builder(requireActivity()).setTitle("溫馨提示")
                            .setMessage("我們需要相機權限才能正常使用該功能")
                            .setNegativeButton(getString(R.string.alert_cancel), null)
                            .setPositiveButton("驗證權限", (dialog, which) -> XPermissionUtils.requestPermissionsAgain(requireActivity(), deniedPermissions,
                                RequestCodeManger.CAMERA))
                            .show();
                    }
                }
            });
    }

    private void showPicDialog(int requestCode) {
        new AlertView.Builder()
            .setContext(requireActivity())
            .setStyle(AlertView.Style.ActionSheet)
            .setOthers(new String[]{getString(R.string.warning_photos), getString(R.string.warning_camera)})
            .setCancelText(getString(R.string.alert_cancel))
            .setOnItemClickListener((o, position) -> {
                if (position == 0) {
                    Intent intent = new Intent(requireActivity(), ClipImageActivity.class);
                    intent.putExtra(Constant.INTENT_IMAGE_GET_WAY, Constant.INTENT_CODE_ALBUM);
                    intent.putExtra(Constant.INTENT_IMAGE_CONFIRM_TYPE, Constant.INTENT_CODE_CROP);
                    startActivityForResult(intent, requestCode);
                } else if (position == 1) {
                    Intent intent = new Intent(requireActivity(), ClipImageActivity.class);
                    intent.putExtra(Constant.INTENT_IMAGE_GET_WAY, Constant.INTENT_CODE_CAMERA);
                    intent.putExtra(Constant.INTENT_IMAGE_CONFIRM_TYPE, Constant.INTENT_CODE_CROP);
                    startActivityForResult(intent, requestCode);
                }
            })
            .build()
            .setCancelable(true)
            .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            String name = data.getStringExtra(BundleKey.RESULT_PIC_URI.key());
            ImageEntity entity = DaVinci.with().getImageLoader().getImage(name);
            String path = DaVinci.with().getImageLoader().getAbsolutePath(name);
            if (requestCode == USER_AVATAR_RESULT_CODE) {
                UserProfileService.uploadSelfAvatar(requireActivity(), entity.getBitmap().getHeight(), path, name);
            }
        }
    }

    @Override
    public void onInvitationCodeComplete(@NonNull String text) {
        String updateText = text.replace("\\n", "\\\n").replace("\\", "");
        Intent shareInvitationCodeIntent = new Intent();
        shareInvitationCodeIntent.setAction(Intent.ACTION_SEND);
        shareInvitationCodeIntent.setType("text/plain");
        shareInvitationCodeIntent.putExtra(Intent.EXTRA_TEXT, updateText);
        IntentUtil.INSTANCE.start(requireContext(), Intent.createChooser(shareInvitationCodeIntent, null));
    }

    @Override
    public void onError(@NonNull String error) {
        ToastUtils.showToast(requireContext(), error);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        downloadPermissionResult.unregister();
    }
}
