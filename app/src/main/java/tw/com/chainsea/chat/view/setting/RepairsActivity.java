package tw.com.chainsea.chat.view.setting;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.common.collect.Lists;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.config.SelectModeConfig;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.permission.PermissionHelper;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.databinding.ActivityRepairsBinding;
import tw.com.chainsea.chat.network.contact.ViewModelFactory;
import tw.com.chainsea.chat.ui.activity.RecordActivity;
import tw.com.chainsea.chat.ui.adapter.GridImageAdapter;
import tw.com.chainsea.chat.ui.utils.permissionUtils.DialogUtil;
import tw.com.chainsea.chat.ui.utils.permissionUtils.RequestCodeManger;
import tw.com.chainsea.chat.ui.utils.permissionUtils.XPermissionUtils;
import tw.com.chainsea.chat.util.GlideEngine;
import tw.com.chainsea.chat.view.BaseActivity;
import tw.com.chainsea.custom.view.layout.manager.FullyGridLayoutManager;

public class RepairsActivity extends BaseActivity {
    private ProgressDialog progressDialog;
    private GridImageAdapter mGridImageAdapter;
    private ActivityRepairsBinding binding;
    private RepairsViewModel repairsViewModel;

    private final ActivityResultLauncher<Intent> audioARL = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            if (result.getData() != null) {
                LocalMedia media = result.getData().getParcelableExtra(BundleKey.IS_SEND_VIDEO.key());
                if (media != null) {
                    repairsViewModel.getMLocalMedia().add(media);
                    mGridImageAdapter.setList(repairsViewModel.getMLocalMedia());
                }
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_repairs);
        String[] array = getResources().getStringArray(R.array.repairs_list);
        List<String> list = Lists.newArrayList(array);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerRepairsType.setAdapter(adapter);
        FullyGridLayoutManager manager = new FullyGridLayoutManager(RepairsActivity.this, 4, GridLayoutManager.VERTICAL, false);
        binding.rvMedia.setLayoutManager(manager);
        mGridImageAdapter = new GridImageAdapter(this, () -> {
        });
        binding.rvMedia.setAdapter(mGridImageAdapter);
        mGridImageAdapter.setSelectMax(1);
        mGridImageAdapter.setOnItemClickListener(new GridImageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, View v) {

            }

            @Override
            public void onItemDeleteClick(int position) {
                repairsViewModel.getMLocalMedia().remove(position);
                mGridImageAdapter.setList(repairsViewModel.getMLocalMedia());
            }
        });

        initListener();
        initViewModel();
    }

    private void initViewModel() {
        ViewModelFactory repairFactory = new ViewModelFactory(getApplication());
        repairsViewModel = new ViewModelProvider(this,
            repairFactory).get(RepairsViewModel.class);

        repairsViewModel.getSendRepairSuccess().observe(this, isSuccess -> {
            Toast.makeText(this, isSuccess ? getString(R.string.text_repair_report_success) : getString(R.string.text_repair_report_failure), Toast.LENGTH_SHORT).show();
            hideLoadingView();
            if (isSuccess)
                finish();
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
    }

    public void showLoadingView(int resId) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.warning_uploading) + "0%");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    public void setLoadingViewProgress(int progress) {
        if (progressDialog != null) {
            progressDialog.setMessage(getString(R.string.warning_uploading) + progress + "%");
        }
    }

    public void hideLoadingView() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void repairs() {
        String name = binding.editRepairsTitle.getText().toString().trim();
        String type = binding.spinnerRepairsType.getSelectedItem().toString();
        String content = binding.editRepairsStep.getText().toString().trim();
        List<String> localMediaPaths = new ArrayList<>();
        List<String> logPaths = CELog.getLogPath();

        if (!TextUtils.isEmpty(name)) {
            if (!TextUtils.isEmpty(type)) {
                if (!TextUtils.isEmpty(content)) {
                    showLoadingView(R.string.wording_loading);
                    for (LocalMedia localMedia : repairsViewModel.getMLocalMedia()) {
                        localMediaPaths.add(!Objects.equals(localMedia.getMimeType(), "audio/mpeg") ? localMedia.getRealPath() : localMedia.getAvailablePath());
                    }
                    repairsViewModel.doRepair(name, type, content, localMediaPaths, logPaths, this::setLoadingViewProgress,
                        isSuccess -> {
                            repairsViewModel.getSendRepairSuccess().postValue(isSuccess);
                            return null;
                        }
                    );
                } else {
                    Toast.makeText(this, getString(R.string.text_repair_report_tip2), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, getString(R.string.text_repair_report_tip), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, getString(R.string.text_repair_report_tip1), Toast.LENGTH_SHORT).show();
        }
    }

    private void initListener() {
        binding.back.setOnClickListener(v -> finish());
        binding.txtAgreeSend.setOnClickListener(v -> repairs());
        binding.imgAlbum.setOnClickListener(v -> chooseMedia(SelectMimeType.ofAll()));
        binding.imgVoice.setOnClickListener(v -> doRecord());
    }

    private void doRecord() {
        XPermissionUtils.requestPermissions(this, RequestCodeManger.AUDIO, new String[]{Manifest.permission.RECORD_AUDIO},
            new XPermissionUtils.OnPermissionListener() {
                @Override
                public void onPermissionGranted() {
                    if (PermissionHelper.isAudioEnable()) {
                        Intent intent = new Intent(RepairsActivity.this, RecordActivity.class);
                        audioARL.launch(intent);
                    } else DialogUtil.showPermissionManagerDialog(RepairsActivity.this, getString(R.string.text_record_title));
                }

                @Override
                public void onPermissionDenied(final String[] deniedPermissions, boolean alwaysDenied) {
                    Toast.makeText(RepairsActivity.this, getString(R.string.text_record_permission_failure), Toast.LENGTH_SHORT).show();
                    if (alwaysDenied) {
                        DialogUtil.showPermissionManagerDialog(RepairsActivity.this, getString(R.string.text_record_title));
                    } else {
                        new AlertDialog.Builder(RepairsActivity.this).setTitle(getString(R.string.text_record_tip))
                            .setMessage(getString(R.string.text_record_tip1))
                            .setNegativeButton(getString(R.string.cancel), null)
                            .setPositiveButton(getString(R.string.text_record_verify_permission), (dialog, which) -> XPermissionUtils.requestPermissionsAgain(RepairsActivity.this, deniedPermissions,
                                RequestCodeManger.AUDIO))
                            .show();
                    }
                }
            });
    }

    public void chooseMedia(int mimeType) {
        // 选择照片
        PictureSelector.create(this)
            .openGallery(mimeType)  //图片
            .setSelectionMode(SelectModeConfig.MULTIPLE)
            .setMaxSelectNum(5)
            .setMinSelectNum(1)
            .setSelectionMode(SelectModeConfig.MULTIPLE)//多选 or 单选 PictureConfig.MULTIPLE or PictureConfig.SINGLE
            .isPreviewImage(true)// 是否可预览图片 true or false
            .isDisplayCamera(false)// 是否顯示拍照按钮 true or false
            .isGif(false)// 是否顯示gif图片 true or false
            .isOpenClickSound(false)// 是否开启点击声音 true or false
            .setImageEngine(GlideEngine.Companion.createGlideEngine())
            .forResult(new OnResultCallbackListener<LocalMedia>() {
                @Override
                public void onResult(ArrayList<LocalMedia> result) {
                    repairsViewModel.getMLocalMedia().addAll(result);
                    mGridImageAdapter.setList(repairsViewModel.getMLocalMedia());
                }

                @Override
                public void onCancel() {

                }
            });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        XPermissionUtils.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
