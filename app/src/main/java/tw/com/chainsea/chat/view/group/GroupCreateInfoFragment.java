package tw.com.chainsea.chat.view.group;

import static tw.com.chainsea.ce.sdk.database.sp.TokenPref.PreferencesKey.CP_TRANS_TENANT_ID;
import static tw.com.chainsea.ce.sdk.database.sp.TokenPref.PreferencesKey.CP_TRANS_TENANT_INFO;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.bigkoo.pickerview.MyOptionsPickerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;

import tw.com.chainsea.chat.util.DaVinci;
import cn.hadcn.davinci.image.base.ImageEntity;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.event.SocketEvent;
import tw.com.chainsea.ce.sdk.event.SocketEventEnum;
import tw.com.chainsea.ce.sdk.http.cp.CpApiManager;
import tw.com.chainsea.ce.sdk.http.cp.base.CpApiListener;
import tw.com.chainsea.ce.sdk.http.cp.model.TenantInfo;
import tw.com.chainsea.ce.sdk.http.cp.respone.RelationTenant;
import tw.com.chainsea.ce.sdk.http.cp.respone.TransTenantActiveResponse;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.base.Constant;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.databinding.FragmentGroupCreateInfoBinding;
import tw.com.chainsea.chat.lib.NetworkUtils;
import tw.com.chainsea.chat.lib.ToastUtils;
import tw.com.chainsea.chat.ui.activity.ClipImageActivity;
import tw.com.chainsea.chat.ui.dialog.IosProgressDialog;
import tw.com.chainsea.chat.ui.utils.permissionUtils.XPermissionUtils;
import tw.com.chainsea.chat.util.SystemKit;
import tw.com.chainsea.chat.view.BackFragment;
import tw.com.chainsea.chat.view.group.viewmodel.GroupViewModel;
import tw.com.chainsea.custom.view.alert.AlertView;

public class GroupCreateInfoFragment extends BackFragment {
    public static final String TAG = GroupCreateInfoFragment.class.getSimpleName();
    private Context context;
    private FragmentGroupCreateInfoBinding binding;
    private GroupViewModel viewModel;
    private static final int REQUEST_CARD = 1999;
    private String avatarPath;
    private IosProgressDialog iosProgressDialog;
    private ActivityResultLauncher<Intent> albumARL = null;
    private final String[] types = {
        "農、林、漁、牧業",
        "礦業及土石採取業",
        "製造業",
        "電力及燃氣供應業",
        "用水供應及汙染整治業",
        "營建工程業",
        "批發及零售業",
        "運輸及倉儲業",
        "住宿及餐飲業",
        "出版影音及資通訊業",
        "金融及保險業",
        "不動產業",
        "專業、科學及技術服務業",
        "支援服務業",
        " 公共行政及國防；強制性社會安全",
        "教育業",
        "醫療保健及社會工作服務業",
        "藝術、娛樂及休閒服務業",
        "其他服務業"
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        iosProgressDialog = new IosProgressDialog(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_group_create_info, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        viewModel = new ViewModelProvider(requireActivity()).get(GroupViewModel.class);

        binding.txtTitle.setText("創建團隊");
        binding.btnClose.setOnClickListener(v -> requireActivity().finish());
        binding.btnClose.setVisibility(View.GONE);
        binding.layoutAvatar.setOnClickListener(v -> editAvatar());
        MyOptionsPickerView<String> singlePicker = new MyOptionsPickerView<>(context);
        final ArrayList<String> items = new ArrayList<>(Arrays.asList(types));
        singlePicker.setPicker(items);
        singlePicker.setTitle("請選擇您的產業類別");
        Button btnSubmit = (Button) singlePicker.getBtnSubmit();
        btnSubmit.setText("確認");
        btnSubmit.setOnClickListener(v -> {
            int[] currentItems = singlePicker.getWheelOptions().getCurrentItems();
            binding.txtChooseType.setText(types[currentItems[0]]);
            singlePicker.dismiss();
        });
        Button btnCancel = (Button) singlePicker.getBtnCancel();
        btnCancel.setText("取消");
        btnCancel.setOnClickListener(v -> singlePicker.dismiss());
        singlePicker.setCyclic(false);
        singlePicker.setSelectOptions(0);
        singlePicker.setOnoptionsSelectListener((options1, option2, options3) -> {
            binding.txtChooseType.setText(items.get(options1));
            singlePicker.dismiss();
            Toast.makeText(context, items.get(options1), Toast.LENGTH_SHORT).show();
        });

        binding.txtChooseType.setOnClickListener(v -> singlePicker.show());

        binding.edtName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                binding.txtNameCount.setText(MessageFormat.format("{0}/20", s.length()));
            }
        });

        binding.edtIntro.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                binding.txtIntroCount.setText(MessageFormat.format("{0}/150", s.length()));
            }
        });
        binding.layoutConfirm.setOnClickListener(v -> activeGroup());

        albumARL = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getData() != null) {
                if (NetworkUtils.isNetworkAvailable(context)) {
                    String imageName = result.getData().getStringExtra(BundleKey.RESULT_PIC_URI.key());
                    ImageEntity imageEntity = DaVinci.with().getImageLoader().getImage(imageName);
                    avatarPath = DaVinci.with().getImageLoader().getAbsolutePath(imageName);
                    binding.imgAvatar.setImageBitmap(imageEntity.getBitmap());
                } else {
                    ToastUtils.showToast(context, getResources().getString(R.string.network_error));
                }
            }
        });
    }

    private void editAvatar() {
        int REQUEST_PERMISSION_CODE = 0x02;
        String[] permission;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            permission = new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA};
        } else {
            permission = new String[]{
                android.Manifest.permission.READ_MEDIA_VIDEO,
                android.Manifest.permission.READ_MEDIA_IMAGES,
                android.Manifest.permission.CAMERA
            };
        }
        XPermissionUtils.requestPermissions(context, REQUEST_PERMISSION_CODE, permission, new XPermissionUtils.OnPermissionListener() {
            @Override
            public void onPermissionGranted() {
                showPicDialog();
            }

            @Override
            public void onPermissionDenied(String[] deniedPermissions, boolean alwaysDenied) {
                Toast.makeText(context, "獲取權限失敗", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void showPicDialog() {
        new AlertView.Builder()
            .setContext(context)
            .setStyle(AlertView.Style.ActionSheet)
            .setOthers(new String[]{"相簿", "照相機"})
            .setCancelText("取消")
            .setOnItemClickListener((o, position) -> {
                if (position == 0) {
                    Intent intent = new Intent(context, ClipImageActivity.class);
                    intent.putExtra(Constant.INTENT_IMAGE_GET_WAY, Constant.INTENT_CODE_ALBUM);
                    intent.putExtra(Constant.INTENT_IMAGE_CONFIRM_TYPE, Constant.INTENT_CODE_NO_CROP);
                    albumARL.launch(intent);
                } else if (position == 1) {
                    Intent intent = new Intent(context, ClipImageActivity.class);
                    intent.putExtra(Constant.INTENT_IMAGE_GET_WAY, Constant.INTENT_CODE_CAMERA);
                    intent.putExtra(Constant.INTENT_IMAGE_CONFIRM_TYPE, Constant.INTENT_CODE_CAMERA);
                    albumARL.launch(intent);
                }
            }).build()
            .setCancelable(true)
            .show();
    }

    private void activeGroup() {
        Editable name = binding.edtName.getText();
        if (name == null || name.length() == 0) {
            ToastUtils.showToast(context, "請輸入團隊名稱");
            return;
        }
        Editable description = binding.edtIntro.getText();
        if (description == null || description.length() == 0) {
            ToastUtils.showToast(context, "請輸入團隊說明");
            return;
        }
        String industry = binding.txtChooseType.getText().toString();
        if ("請選擇您的產業類別".equals(industry)) {
            ToastUtils.showToast(context, "請選擇您的產業類別");
            return;
        }
        if (avatarPath == null || avatarPath.isEmpty()) {
            ToastUtils.showToast(context, "請選擇團隊頭像");
            return;
        }

        iosProgressDialog.show("團隊創建中...");
        CpApiManager.getInstance().activeTenantTrans(context,
            viewModel.tenantId,
            name.toString(),
            description.toString(),
            industry,
            avatarPath,
            new CpApiListener<>() {
                @Override
                public void onSuccess(String s) {
                    Log.d("TAG", s);
                    TransTenantActiveResponse response = JsonHelper.getInstance().from(s, TransTenantActiveResponse.class);
                    if ("0000".equals(response.getStatus())) {
                        TenantInfo transTenantInfo = response.getTenantInfo();
                        if (transTenantInfo != null) {
                            TokenPref.getInstance(context)
                                .clearByKey(CP_TRANS_TENANT_INFO)
                                .clearByKey(CP_TRANS_TENANT_ID);
                        }
                    }
                }

                @Override
                public void onFailed(String errorCode, String errorMessage) {
                    Log.d("TAG", errorCode + errorMessage);
                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show());
                    iosProgressDialog.dismiss();
                }
            });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleEvent(SocketEvent eventMsg) {
        // 團隊建立完成
        if (eventMsg.getType() == SocketEventEnum.TransTenantActive) {
            iosProgressDialog.dismiss();
            RelationTenant relationTenant = JsonHelper.getInstance().from(eventMsg.getData(), RelationTenant.class);
            SystemKit.changeTenant(requireActivity(), relationTenant, false);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        albumARL.unregister();
    }
}
