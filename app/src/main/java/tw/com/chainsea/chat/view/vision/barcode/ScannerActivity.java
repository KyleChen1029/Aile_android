package tw.com.chainsea.chat.view.vision.barcode;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import tw.com.chainsea.android.common.barcode.BarcodeDetectorHelper;
import tw.com.chainsea.android.common.hash.AESHelper;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.multimedia.AMediaBean;
import tw.com.chainsea.android.common.multimedia.MediaContentObserver;
import tw.com.chainsea.android.common.multimedia.MultimediaHelper;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.ce.sdk.bean.PicSize;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.tenant.EnvironmentInfo;
import tw.com.chainsea.ce.sdk.config.AppConfig;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.ce.sdk.database.sp.TenantPref;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.database.sp.UserPref;
import tw.com.chainsea.ce.sdk.event.EventMsg;
import tw.com.chainsea.ce.sdk.event.MsgConstant;
import tw.com.chainsea.ce.sdk.event.SocketEvent;
import tw.com.chainsea.ce.sdk.http.cp.CpApiManager;
import tw.com.chainsea.ce.sdk.http.cp.base.CpApiListener;
import tw.com.chainsea.ce.sdk.http.cp.respone.RelationTenant;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.network.model.response.Guarantor;
import tw.com.chainsea.ce.sdk.reference.UserProfileReference;
import tw.com.chainsea.ce.sdk.service.AvatarService;
import tw.com.chainsea.ce.sdk.socket.cp.model.GuarantorJoinAgreeContent;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.config.ScannerType;
import tw.com.chainsea.chat.databinding.ActivityScanBinding;
import tw.com.chainsea.chat.dialog.QrCodeDialog;
import tw.com.chainsea.chat.lib.ToastUtils;
import tw.com.chainsea.chat.network.contact.ViewModelFactory;
import tw.com.chainsea.chat.network.tenant.TenantViewModel;
import tw.com.chainsea.chat.presenter.ScannerPresenter;
import tw.com.chainsea.chat.qrcode.module.TransTenantInfo;
import tw.com.chainsea.chat.qrcode.module.UserInfo;
import tw.com.chainsea.chat.ui.dialog.MessageDialogBuilder;
import tw.com.chainsea.chat.ui.ife.IScannerView;
import tw.com.chainsea.chat.util.GlideEngine;
import tw.com.chainsea.chat.util.IntentUtil;
import tw.com.chainsea.chat.util.NoDoubleClickListener;
import tw.com.chainsea.chat.util.SystemKit;
import tw.com.chainsea.chat.util.ThemeHelper;
import tw.com.chainsea.chat.util.VibratorKit;
import tw.com.chainsea.chat.view.account.ChangeTenantActivity;
import tw.com.chainsea.chat.view.group.GroupWaitConfirmActivity;
import tw.com.chainsea.chat.view.login.LoginOtherDeviceActivity;
import tw.com.chainsea.chat.view.qrcode.ServiceNumberQrCodeActivity;
import tw.com.chainsea.chat.view.vision.ScanResultBean;
import tw.com.chainsea.chat.view.vision.base.BaseVisionPreviewActivity;
import tw.com.chainsea.chat.view.vision.model.CpScanResult;
import tw.com.chainsea.chat.zxing.encoding.EncodingHandler;
import tw.com.chainsea.custom.view.alert.AlertView;
import tw.com.chainsea.custom.view.progress.IosProgressBar;

public class ScannerActivity extends BaseVisionPreviewActivity implements MediaContentObserver.Listener, IScannerView {
    private int emptyReadyPosition = 1;
    private ActivityScanBinding binding;
    private String tenantCode = "";
    private String firstGuarantorMember = "";
    private ScannerPresenter scannerPresenter;

    private boolean isDialogOpen = false;

    private TenantViewModel tenantViewModel;
    private String[] joinTenantSplit;

    private final NoDoubleClickListener clickListener = new NoDoubleClickListener() {
        @Override
        protected void onNoDoubleClick(View v) {
            if (v.equals(binding.btnClose)) {
                closeScanView();
            }
        }
    };
    private String scannerType = ScannerType.None.name();
    private IosProgressBar progressBar;
    private boolean isGreenTheme = false;

    private QrcodeScanner qrcodeScanner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.INSTANCE.setTheme(ScannerActivity.this);
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_scan);
        try {
            Glide.with(this)
                .asGif()
                .load(R.raw.loading)
                .into(binding.includeWaitConfirm.imgLoading);
        } catch (Exception ignored) {
        }

        if (getIntent() != null)
            scannerType = getIntent().getStringExtra(BundleKey.SCANNER_TYPE.key());

        scannerPresenter = new ScannerPresenter(this);
        isGreenTheme = ThemeHelper.INSTANCE.isGreenTheme();
        binding.txtName1.setBackgroundResource(isGreenTheme ? R.drawable.ic_join_team_head_green : R.drawable.ic_join_team_head_blue);
        binding.txtName2.setBackgroundResource(isGreenTheme ? R.drawable.ic_join_team_head_green : R.drawable.ic_join_team_head_blue);
        initViewModel();
        observeData();
        initReScanGuarantorData();
    }

    private void observeData() {
        tenantViewModel.isAddGuarantorSuccess().observe(this, aBoolean -> scanRestart());
        tenantViewModel.getAddGuarantorError().observe(this, aBoolean -> scanRestart());
        tenantViewModel.getTenantList().observe(this, list -> {
            try {
                if (TokenPref.getInstance(ScannerActivity.this).getJoinTenantPermission() > 0 || Objects.equals(scannerType, ScannerType.FirstJoinTenant.name())) {
                    showJoinTenantDialog();
                } else {
                    ToastUtils.showToast(ScannerActivity.this, getString(R.string.text_not_join_tenant_permission));
                    scanRestart();
                }
            } catch (Exception ignored) {
                ToastUtils.showToast(ScannerActivity.this, getString(R.string.text_not_join_tenant_permission));
                scanRestart();
            }
        });
    }

    private void showJoinTenantDialog() throws Exception {
        UserInfo userInfo = JsonHelper.getInstance().from(AESHelper.decrypt(joinTenantSplit[1], AppConfig.AES_KEY), UserInfo.class);
        boolean verify = verifyQRCode(userInfo);
        if (!verify) {
            scanRestart();
            return;
        }
        new AlertView.Builder()
            .setContext(this)
            .setStyle(AlertView.Style.Alert)
            .setMessage("您確認要取得" + userInfo.getName() + "成員同意嗎？")
            .setOthers(new String[]{"取消", "確定"})
            .setOnItemClickListener((o, position) -> {
                if (position == 1) {
                    if (scannerType.equals(ScannerType.ReScanGuarantor.name())) {
                        // 重新加擔保人 Ce
                        addGuarantor(userInfo);
                    } else {
                        // 加入團隊 Cp
                        joinTenant(userInfo);
                    }

                } else {
                    scanRestart();
                }
            })
            .build()
            .setCancelable(false)
            .show();
    }

    private void initViewModel() {
        ViewModelProvider.Factory tenantRepository = new ViewModelFactory(getApplication());
        tenantViewModel = new ViewModelProvider(this, tenantRepository).get(TenantViewModel.class);
    }

    @Override
    protected int getStatusBarColor() {
        return getColor(R.color.colorPrimary);
    }

    @Override
    protected boolean isHideStatusBar() {
        return false;
    }

    @Override
    protected void initData() {
        if (scannerType.equals(ScannerType.Scanner.name())) {
            binding.scopeScanner.setVisibility(View.VISIBLE);
            binding.scopeJoinTeam.setVisibility(View.GONE);
        } else if (scannerType.equals(ScannerType.JoinTenant.name()) || scannerType.equals(ScannerType.FirstJoinTenant.name())) {
            binding.scopeScanner.setVisibility(View.GONE);
            binding.scopeJoinTeam.setVisibility(View.VISIBLE);
        } else if (scannerType.equals(ScannerType.ReScanGuarantor.name())) {
            binding.scopeInviteCode.setVisibility(View.GONE);
            binding.scopeScanner.setVisibility(View.GONE);
            binding.scopeJoinTeam.setVisibility(View.VISIBLE);
        }
//        binding.scopeScanner.setVisibility(scannerType.equals(ScannerType.Scanner.name()) ? View.VISIBLE : View.GONE);
//        binding.scopeJoinTeam.setVisibility(scannerType.equals(ScannerType.JoinTenant.name()) ? View.VISIBLE : View.GONE);
        MediaContentObserver imageObserver = new MediaContentObserver(MultimediaHelper.Type.IMAGE, new Handler(), this);
        getContentResolver().registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, imageObserver);

        binding.txtTitle.setText(getIntent().getStringExtra(BundleKey.TITLE.key()));
    }

    private void initReScanGuarantorData() {
        // 缺少擔保人的流程
        if (scannerType.equals(ScannerType.ReScanGuarantor.name())) {
            Type listType = new com.google.gson.reflect.TypeToken<List<Guarantor>>() {
            }.getType();
            List<Guarantor> guarantorList = JsonHelper.getInstance().from(getIntent().getStringExtra(BundleKey.RE_SCAN_GUARANTOR.key()), listType);
            RelationTenant currentTenant = TokenPref.getInstance(this).getCpCurrentTenant();
            guarantorList.forEach(guarantor -> {
                GuarantorJoinAgreeContent guarantorJoinAgreeContent = new GuarantorJoinAgreeContent();
                guarantorJoinAgreeContent.setTenantName(currentTenant.getTenantName());
                guarantorJoinAgreeContent.setTenantCode(currentTenant.getTenantCode());
                guarantorJoinAgreeContent.setServiceUrl(currentTenant.getServiceUrl());
                guarantorJoinAgreeContent.setAccountId(guarantor.getName());
                guarantorJoinAgreeContent.setName(guarantor.getName());
                addMember(emptyReadyPosition, guarantorJoinAgreeContent);
            });
        }
    }

    @Override
    protected void setListener() {
        binding.btnClose.setOnClickListener(clickListener);
        //binding.tvFlashMode.setOnClickListener(this::doFlashModeAction);
        binding.ivPhotoSelect.setOnClickListener(this::doPhotoSelectAction);
        binding.tvMyBarCode.setOnClickListener(this::doMyQrCodeAction);
//        binding.includeWaitConfirm.btnClose.setOnClickListener(this::doCloseWaitConfirm);
        binding.tvSendCode.setOnClickListener(this::doSendInvitationCode);
        binding.tvOfficial.setOnClickListener(this::intentToOfficialServiceNumber);
        binding.btnConfirm.setOnClickListener(this::doInvitationCodeConfirm);
    }

    @Override
    protected boolean doAdvanceExecution() {
        String type = getIntent().getStringExtra(BundleKey.VISION_TYPE.key());
        if (Strings.isNullOrEmpty(type)) {
            return false;
        } else {
            internalHandle();
        }
        return true;
    }

    private void internalHandle() {
        setPhotoSelectThumbnail(MultimediaHelper.Type.IMAGE);
        //TODO 目前是CE, 之後要改成CP的
        binding.tvMyBarCode.setVisibility(View.GONE);
        binding.tvMyBarCode.setEnabled(false);
    }

    @Override
    protected void doExecution() {
        qrcodeScanner = new QrcodeScanner(this, binding.previewView.getSurfaceProvider(), (result) -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            sendResult(result);
            return null;
        });
        qrcodeScanner.startScan(this);
    }

    private void startAnimation() {
        final View line = findViewById(R.id.v_scanning_line);
        final Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.move);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                line.startAnimation(anim);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        line.startAnimation(anim);
    }

    private void stopAnimation() {
        final View line = findViewById(R.id.v_scanning_line);
        line.clearAnimation();
    }

    @RequiresPermission(Manifest.permission.CAMERA) //掃到東西後回來
    private void sendResult(String text) {
        stopAnimation();
        VibratorKit.longClick();
        isDialogOpen = true;

        String type = getIntent().getStringExtra(BundleKey.VISION_TYPE.key());
        // 區分外部使用 url scheme or 內部使用
        if (!Strings.isNullOrEmpty(type)) {
            Uri uri = Uri.parse(text);
            if ("https".equals(uri.getScheme()) && "aile.com.tw".equals(uri.getAuthority())) {
                String[] split;
                String decode;
                try {
                    switch (Objects.requireNonNull(uri.getPath())) {
                        case "/cp/joinTransTenant":
                            split = text.split("code=");
                            TransTenantInfo tenantInfo = JsonHelper.getInstance().from(AESHelper.decryptBase64(split[1]), TransTenantInfo.class);
                            new MessageDialogBuilder(this).setTitle("加入團隊").setMessage("您正要加入" + tenantInfo.getCreator() + "的團隊，若您同意，請按確定")
                                .setOnConfirmListener(message -> {
                                    joinTenantTrans(tenantInfo);
                                    TokenPref.getInstance(this).setCpTransTenantId(tenantInfo.getTenantId());
                                    scanRestart();
                                })
                                .setOnCancelListener(message -> scanRestart())
                                .create()
                                .show();
                            break;
                        case "/cp/qrlogin":
                            split = text.split("code=");
                            decode = AESHelper.decrypt(split[1], AppConfig.AES_KEY);
                            CpScanResult scanResult = JsonHelper.getInstance().from(decode, CpScanResult.class);
                            CpApiManager.getInstance().loginDeviceScan(this, scanResult.getOnceToken(), new CpApiListener<>() {
                                @Override
                                public void onSuccess(String s) {
                                    Intent intent = new Intent(ScannerActivity.this, LoginOtherDeviceActivity.class);
                                    intent.putExtra(BundleKey.ONCE_TOKEN.key(), scanResult.getOnceToken());
                                    intent.putExtra(BundleKey.DEVICE_NAME.key(), scanResult.getDeviceName());
                                    startActivity(intent);
                                    finish();
                                }

                                @Override
                                public void onFailed(String errorCode, String errorMessage) {
                                    Log.d("TAG", errorCode + errorMessage);
                                    Toast.makeText(ScannerActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> scanRestart());
                                }
                            });
                            break;
                        case "/cp/userInfo":
                            joinTenantSplit = text.split("code=");
                            tenantViewModel.getTenantList(false); // 重新取得團隊後台設定值joinTenant
                            break;
                        default:
                            // 內部 1.15.0 , CE 舊有的流程
                            String code = Uri.parse(text).getQueryParameter("code");
                            String json = AESHelper.decrypt(code, AppConfig.AES_KEY);
                            EnvironmentInfo environmentInfo = JsonHelper.getInstance().from(json, EnvironmentInfo.class);
                            new AlertView.Builder()
                                .setContext(this)
                                .setStyle(AlertView.Style.Alert)
                                .setOthers(new String[]{getString(R.string.alert_cancel), getString(R.string.alert_confirm)})
                                .setMessage(getString(R.string.barcode_detector_alert_welcome_message) + environmentInfo.getName() + getString(R.string.barcode_detector_alert_welcome_message_tail))
                                .setOnItemClickListener((o, position) -> {
                                    if (position == 1) {
                                        TenantPref.getClosed(this, environmentInfo.getId())
                                            .setEnvironment(environmentInfo)
                                            .setJoinTime(System.currentTimeMillis());
                                        TokenPref.getInstance(this)
                                            .setCurrentTenantId(environmentInfo.getId())
                                            .setCurrentTenantUrl(environmentInfo.getUrl())
                                            .setCurrentTenantCode(environmentInfo.getCode());
                                        TokenPref.getInstance(this).setRecordEnvironment(environmentInfo.getId());
                                        Intent intent = new Intent();
                                        intent.putExtra("INFO_NAME", environmentInfo.getName());
                                        setResult(RESULT_OK, intent);
                                        finish();
                                    }
                                    scanRestart();
                                })
                                .build()
                                .setCancelable(false)
                                .setOnDismissListener(o -> {
                                    isDialogOpen = false;
                                    scanRestart();
                                })
                                .show();
                            break;
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "No results from parsing", Toast.LENGTH_SHORT).show();
                    isDialogOpen = false;
                    scanRestart();
                }
            } else if ("https".contains(Objects.requireNonNull(uri.getScheme()))) {
                openInBrowser(uri.toString());
            } else {
                Toast.makeText(this, "No results from parsing", Toast.LENGTH_SHORT).show();
                scanRestart();
            }
        } else {
            Toast.makeText(this, "No results from parsing", Toast.LENGTH_SHORT).show();
            scanRestart();
        }
    }

    private void openInBrowser(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    private boolean verifyQRCode(UserInfo userInfo) {
        //加擔保人的話拿 userId
        //一般的加入團隊拿 accountId
        String id = userInfo.getAccountId();
        if (scannerType.equals(ScannerType.ReScanGuarantor.name())) {
            id = userInfo.getUserId();
        }

        //掃描QRCODE為同一擔保人時，不能加入團隊
        if (firstGuarantorMember.equals(id)) {
            Toast.makeText(this, "請掃描另一位團隊成員的QR code", Toast.LENGTH_SHORT).show();
            return false;
        }

        //掃描QRCODE為不同團隊時，不能加入團隊
        if (!tenantCode.isEmpty()) {
            if (!tenantCode.equals(userInfo.getTenantCode())) {
                Toast.makeText(this, "請掃描相同的團隊加入", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        //掃描到自己 (重新加擔保人 -> 拿 userId; 加入團隊 -> 拿 account Id)
        String selfId = TokenPref.getInstance(this).getCpAccountId();
        if (scannerType.equals(ScannerType.ReScanGuarantor.name())) {
            selfId = TokenPref.getInstance(this).getUserId();
        }
        if (id.equals(selfId)) {
            showQRCodeErrorDialog();
            return false;
        }

        //是否加入過同一個團隊
        List<RelationTenant> tenantList = TokenPref.getInstance(this).getCpRelationTenantList();
        if (tenantList != null) {
            tenantList = tenantList.stream().filter(tenant -> tenant.getTenantCode().equals(userInfo.getTenantCode())).collect(Collectors.toList());
        } else {
            tenantList = Lists.newArrayList();
        }

        if (!tenantList.isEmpty() && !scannerType.equals(ScannerType.ReScanGuarantor.name())) {
            Toast.makeText(this, "您已加入過此團隊", Toast.LENGTH_SHORT).show();
            return false;
        }


        return true;
    }

    private void addGuarantor(UserInfo userInfo) {
        tenantViewModel.tenantGuarantorAdd(userInfo.getUserId());
    }


    private void joinTenant(UserInfo userInfo) {
        CpApiManager.getInstance().tenantGuarantorAdd(this, userInfo.getTenantCode(),
            userInfo.getAccountId(), new CpApiListener<>() {
                @Override
                public void onSuccess(String s) {
                    scanRestart();
                    //showWaitConfirm();
                    //從掃一掃加入團隊要切換成加入團隊頁面
                    if (scannerType.equals(ScannerType.Scanner.name())) {
                        scannerType = ScannerType.JoinTenant.name();
                        binding.scopeScanner.setVisibility(View.GONE);
                        binding.scopeJoinTeam.setVisibility(View.VISIBLE);
                        binding.txtTitle.setText(getString(R.string.join_group));
                    }
                    binding.tvAlreadyJoin.setVisibility(View.VISIBLE);
                    binding.tvAlreadyJoin.setText(getString(R.string.text_joining_to_team, userInfo.getTenantName()));
                }

                @Override
                public void onFailed(String errorCode, String errorMessage) {
                    if (ErrCode.TENANT_USER_IS_ALREADY.getValue().equals(errorCode)) {
                        Toast.makeText(ScannerActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    } else if (ErrCode.TENANT_USER_NOT_EXIST.getValue().equals(errorCode)) {
                        Toast.makeText(ScannerActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                    binding.includeWaitConfirm.getRoot().setVisibility(View.GONE);
                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> scanRestart());

                }
            });
    }

    private void joinTenantTrans(TransTenantInfo tenantInfo) {
        CpApiManager.getInstance().joinTenantTrans(this, tenantInfo.getTenantId(), new CpApiListener<>() {
            @Override
            public void onSuccess(String s) {
                startActivity(new Intent(ScannerActivity.this, GroupWaitConfirmActivity.class));
                finish();
            }

            @Override
            public void onFailed(String errorCode, String errorMessage) {
                Log.d("TAG", errorCode + errorMessage);
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> Toast.makeText(ScannerActivity.this, errorMessage, Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    @SuppressLint("MissingPermission")
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }


    @Override
    protected void onResume() {
        super.onResume();
        this.isDialogOpen = false;
        scanRestart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (qrcodeScanner != null) {
            qrcodeScanner.release();
        }
    }

    private void showExitDialog() {
        new AlertView.Builder()
            .setContext(this)
            .setStyle(AlertView.Style.Alert)
            .setMessage("您確定要關閉掃描嗎？")
            .setOthers(new String[]{"取消", "確定"})
            .setOnItemClickListener((o, position) -> {
                if (position == 1) {
                    List<RelationTenant> relationTenants = TokenPref.getInstance(this).getCpRelationTenantList();
                    if (relationTenants.size() > 1) {
                        startActivity(new Intent(this, ChangeTenantActivity.class));
                    } else {
                        SystemKit.logoutToLoginPage();
                    }
                }
            })
            .build()
            .setCancelable(true)
            .show();

    }

    private void closeScanView() {
        if (scannerType.equals(ScannerType.ReScanGuarantor.name())) {
            showExitDialog();
            return;
        }
        if (tenantCode.isEmpty()) {
            ScannerActivity.this.finish();
        } else {
            CpApiManager.getInstance().tenantGuarantorCancel(this, tenantCode,
                new CpApiListener<>() {
                    @Override
                    public void onSuccess(String s) {
                        finish();
                    }

                    @Override
                    public void onFailed(String errorCode, String errorMessage) {
                        if (ErrCode.TENANT_USER_IS_ALREADY.getValue().equals(errorCode)) {
                            Toast.makeText(ScannerActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
        }
    }


    @SuppressLint("MissingPermission")
    private void doMyQrCodeAction(View view) {
//        if (cameraSource != null) {
//            cameraSource.stop();
//        }
        stopAnimation();
        VibratorKit.longClick();
        String selfId = TokenPref.getInstance(this).getUserId();
        String selfAvatarId = UserPref.getInstance(this).getUserAvatarId();
        UserProfileEntity selfEntity = UserProfileReference.findById(null, selfId);
        QrCodeDialog qrCodeDialog = new QrCodeDialog(this);
        assert selfEntity != null;
        qrCodeDialog.setName(selfEntity.getNickName());
        qrCodeDialog.setDuty(selfEntity.getMood());
        try {
            String json = ScanResultBean.Build().userId(selfId).build().toJson();
            Bitmap code = EncodingHandler.createQRCode(json, 500, 500, null);
            qrCodeDialog.setQrCode(code);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
        AvatarService.post(this, selfAvatarId, PicSize.SMALL, qrCodeDialog.getAvatar(), R.drawable.custom_default_avatar);
        qrCodeDialog.setOnDismissListener(dialog -> {
            CAN_NEXT = true;
            try {
                qrcodeScanner.startScan(this);
//                cameraSource.start(binding.surfaceView.getHolder());
                startAnimation();
            } catch (Exception ignored) {
            }
        });
        if (CAN_NEXT) {
            CAN_NEXT = false;
            qrCodeDialog.show();
        }
    }

//    private void doFlashModeAction(View view) {
//        if (cameraSource != null && !Strings.isNullOrEmpty(cameraSource.getFlashMode())) {
//            VibratorKit.longClick();
//            if ("off".equals(cameraSource.getFlashMode())) {
//                cameraSource.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
//                binding.tvFlashMode.setText(R.string.barcode_detector_flash_close);
//            } else {
//                cameraSource.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
//                binding.tvFlashMode.setText(R.string.barcode_detector_flash_open);
//            }
//        }
//    }

    private void doSendInvitationCode(View view) {
        CpApiManager.getInstance().getInvitationCode(this, TokenPref.getInstance(this).getTenantCode(), new CpApiListener<>() {
            @Override
            public void onSuccess(String s) {
                try {
                    JSONObject jsonObject = JsonHelper.getInstance().toJsonObject(s);
                    String text = jsonObject.getString("text");

                    if (!text.isEmpty()) {
                        String updateText = text.replace("\\n", "\\\n").replace("\\", "");
                        Intent shareInvitationCodeIntent = new Intent();
                        shareInvitationCodeIntent.setAction(Intent.ACTION_SEND);
                        shareInvitationCodeIntent.setType("text/plain");
                        shareInvitationCodeIntent.putExtra(Intent.EXTRA_TEXT, updateText);
                        startActivity(Intent.createChooser(shareInvitationCodeIntent, null));
                    } else
                        ToastUtils.showToast(ScannerActivity.this, "Invitation Code is Empty");

                } catch (Exception e) {
                    CELog.e("doSendInvitationCode onSuccess error=" + e.getMessage());
                    ToastUtils.showToast(ScannerActivity.this, e.getMessage());
                }
            }

            @Override
            public void onFailed(String errorCode, String errorMessage) {
                CELog.e("doSendInvitationCode onFailed=" + errorCode + ", " + errorMessage);
                ToastUtils.showToast(ScannerActivity.this, errorMessage);
            }
        });
    }

    private void doInvitationCodeConfirm(View view) {
        scannerPresenter.sendInvitationCodeToJoinTenant(this, Objects.requireNonNull(binding.etInviteCode.getText()).toString());
    }

    private void intentToOfficialServiceNumber(View view) {
        Bundle bundle = new Bundle();
        bundle.putInt(BundleKey.TARGET_QR_CODE_POSITION.key(), 1);
        IntentUtil.INSTANCE.startIntent(this, ServiceNumberQrCodeActivity.class, bundle);
    }

    private void doPhotoSelectAction(View view) {
        PictureSelector.create(this)
            .openGallery(SelectMimeType.ofImage())
            .setMaxSelectNum(10)
            .setMinSelectNum(1)
            .isPreviewImage(true)
            .isDisplayCamera(false)
            .isOriginalSkipCompress(false)
            .isGif(true)
            .isOpenClickSound(false)
            .setImageEngine(GlideEngine.Companion.createGlideEngine())
            .forResult(new OnResultCallbackListener<>() {
                @Override
                public void onResult(ArrayList<LocalMedia> result) {
                    if (result != null && !result.isEmpty()) {
                        SparseArray<Barcode> detectedItems = BarcodeDetectorHelper.detect(ScannerActivity.this, result.get(0).getRealPath(), Barcode.DATA_MATRIX, Barcode.QR_CODE);
                        if (detectedItems.size() != 0) {
                            if (ActivityCompat.checkSelfPermission(ScannerActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
                            doDetectedItemsParsing(detectedItems);
                        } else {
                            Toast.makeText(ScannerActivity.this, "No results from parsing", Toast.LENGTH_SHORT).show();
                            scanRestart();
                        }
                    }
                }

                @Override
                public void onCancel() {

                }
            });
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    private void doDetectedItemsParsing(SparseArray<Barcode> detectedItems) {
        if (detectedItems.size() != 0) {
            if (!isDialogOpen) {
                isDialogOpen = true;

                new Handler(Looper.getMainLooper()).post(() -> {
                    try {
                        qrcodeScanner.pauseScan();
                        stopAnimation();
                        sendResult(detectedItems.valueAt(0).displayValue);
                    } catch (Exception e) {
                        Toast.makeText(ScannerActivity.this, "No results from parsing", Toast.LENGTH_SHORT).show();
                        scanRestart();
                    }
                });


            }
        }
    }

    private void setPhotoSelectThumbnail(MultimediaHelper.Type type) {
        List<AMediaBean> beans = MultimediaHelper.query(this, MultimediaHelper.Type.IMAGE, 1);
        if (beans == null || beans.isEmpty()) {
            binding.ivPhotoSelect.setImageResource(isGreenTheme ? R.drawable.image_load_error_green : R.drawable.image_load_error);
            return;
        }
        if (Objects.requireNonNull(type) == MultimediaHelper.Type.IMAGE) {
            AMediaBean bean = beans.get(0);
            if (bean.getThumbnailBitmap() != null) {
                Bitmap bitmap = bean.getThumbnailBitmap();
                binding.ivPhotoSelect.setImageBitmap(bitmap);
            }
        }
    }

    @Override
    public void onChange(MultimediaHelper.Type type, boolean selfChange, Uri uri) {
        if (MultimediaHelper.Type.IMAGE.equals(type)) {
            setPhotoSelectThumbnail(type);
        }
    }

    //加入團隊 Cp
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SocketEvent socketEvent) {
        binding.includeWaitConfirm.getRoot().setVisibility(View.GONE);
        scanRestart();
        switch (socketEvent.getType()) {
            case GuarantorJoinReject:
                rejectJoin();
                break;
            case GuarantorJoinAgree:
                GuarantorJoinAgreeContent guarantorJoinAgreeContent = (GuarantorJoinAgreeContent) socketEvent.getData();
                addMember(emptyReadyPosition, guarantorJoinAgreeContent);
                break;
        }
    }

    //缺少擔保人 Ce
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventMsg eventMsg) {
        switch (eventMsg.getCode()) {
            case MsgConstant.NOTICE_GUARANTOR_JOIN_AGREE:
                GuarantorJoinAgreeContent guarantorJoinContent = JsonHelper.getInstance().from(eventMsg.getData().toString(), GuarantorJoinAgreeContent.class);
                addMember(emptyReadyPosition, guarantorJoinContent);
                break;
            case MsgConstant.NOTICE_GUARANTOR_JOIN:
                finish();
                break;
            case MsgConstant.NOTICE_GUARANTOR_JOIN_REJECT:
                rejectJoin();
                break;
        }
    }

    private void rejectJoin() {
        new AlertView.Builder()
            .setContext(this)
            .setStyle(AlertView.Style.Alert)
            .setTitle("加入團隊被拒")
            .setMessage("請重新掃描團隊QR Code或加入其他團隊。")
            .setOthers(new String[]{"確定"})
            .setOnItemClickListener((o, position) -> {

            })
            .build()
            .setCancelable(true)
            .show();
    }

    private void addMember(int position, GuarantorJoinAgreeContent agreeContent) {
        switch (position) {
            case 1: {
                UserProfileEntity user = DBManager.getInstance().queryFriend(agreeContent.getAccountId());
                binding.txtName1.loadAvatarIcon(user != null && user.getAvatarId() != null ? user.getAvatarId() : "", agreeContent.getName(), agreeContent.getAccountId());
                binding.txtWait1.setText(agreeContent.getName());
                binding.imgWait1.setVisibility(View.INVISIBLE);
                if (scannerType.equals(ScannerType.ReScanGuarantor.name())) {
                    firstGuarantorMember = DBManager.getInstance().queryUserIdByName(agreeContent.getName());
                } else {
                    firstGuarantorMember = agreeContent.getAccountId();
                }
                tenantCode = agreeContent.getTenantCode();
                emptyReadyPosition = 2;
                break;
            }

            case 2: {
                UserProfileEntity user = DBManager.getInstance().queryFriend(agreeContent.getAccountId());
                binding.txtName2.loadAvatarIcon(user != null && user.getAvatarId() != null ? user.getAvatarId() : "", agreeContent.getName(), agreeContent.getAccountId());
                binding.txtWait2.setText(agreeContent.getName());
                binding.imgWait2.setVisibility(View.INVISIBLE);
                emptyReadyPosition = 3;
                if (scannerType.equals(ScannerType.ReScanGuarantor.name())) {
                    finish();
                } else if (scannerType.equals(ScannerType.JoinTenant.name()) || scannerType.equals(ScannerType.FirstJoinTenant.name())) {
                    //加入團隊
                    SystemKit.changeTenant(this, new RelationTenant(agreeContent.getTenantName(), agreeContent.getServiceUrl(), agreeContent.getTenantCode()), false, scannerType);
                }
                break;
            }

            case 3: {
                //加入團隊
                SystemKit.changeTenant(this, new RelationTenant(agreeContent.getTenantName(), agreeContent.getServiceUrl(), agreeContent.getTenantCode()), false);
                break;
            }
        }
    }

    private void scanRestart() {
        isDialogOpen = false;
        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                qrcodeScanner.resumeScan();
                startAnimation();
            } catch (Exception exception) {
                CELog.e(exception.getMessage());
            }
            binding.includeWaitConfirm.getRoot().setVisibility(View.GONE);
        });
    }

    private void showQRCodeErrorDialog() {
        new AlertView.Builder()
            .setContext(this)
            .setStyle(AlertView.Style.Alert)
            .setTitle("加入圑隊失敗")
            .setMessage("QR Code掃描錯誤或已失效，請檢查輸入是否正確，或再向團隊成員索取")
            .setOthers(new String[]{getString(R.string.text_for_sure)})
            .setOnItemClickListener((o, position) -> {
            })
            .build()
            .setCancelable(true)
            .show();
    }

    @Override
    public void onJoinFailure(@NonNull String error) {
        new AlertView.Builder()
            .setContext(this)
            .setStyle(AlertView.Style.Alert)
            .setTitleSize(24)
            .setTitle(!error.isEmpty() ? getString(R.string.text_joining_team_failure) : "")
            .setMessage(!error.isEmpty() ? error : getString(R.string.text_joining_team_failure))
            .setOthers(new String[]{getString(R.string.text_sure)})
            .setOnItemClickListener((o, position) -> {
            })
            .build()
            .setCancelable(true)
            .show();
        hideLoadingView();
    }

    @Override
    public void onJoinSuccess(@NonNull RelationTenant relationTenant) {
        hideLoadingView();
        new AlertView.Builder()
            .setContext(this)
            .setStyle(AlertView.Style.Alert)
            .setMessage(getString(R.string.text_you_join_tenant_already, relationTenant.getTenantName()))
            .setOthers(new String[]{getString(R.string.text_sure)})
            .setOnItemClickListener((o, position) -> SystemKit.changeTenant(this, relationTenant, false))
            .build()
            .setCancelable(false)
            .show();
    }

    @Override
    public void showLoading() {
        progressBar = IosProgressBar.show(this, "", true, false, dialog -> {
        });
    }

    @Override
    public void dismissLoading() {
        if (progressBar != null && progressBar.isShowing()) {
            progressBar.dismiss();
        }
    }

    private void hideLoadingView() {
        try {
            if (progressBar != null && progressBar.isShowing())
                progressBar.dismiss();
        } catch (Exception ignored) {
        }
    }
}
