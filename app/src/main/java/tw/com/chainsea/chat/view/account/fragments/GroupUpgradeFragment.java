package tw.com.chainsea.chat.view.account.fragments;

import static android.Manifest.permission.CAMERA;
import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.bigkoo.pickerview.MyOptionsPickerView;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.XXPermissions;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import tw.com.chainsea.chat.util.DaVinci;
import cn.hadcn.davinci.image.base.ImageEntity;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.ce.sdk.bean.PicSize;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.http.cp.CpApiManager;
import tw.com.chainsea.ce.sdk.http.cp.base.CpApiListener;
import tw.com.chainsea.ce.sdk.http.cp.respone.IndustryScaleResponse;
import tw.com.chainsea.ce.sdk.service.AvatarService;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.base.Constant;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.databinding.FragmentGroupUpgradeBinding;
import tw.com.chainsea.chat.lib.NetworkUtils;
import tw.com.chainsea.chat.lib.ToastUtils;
import tw.com.chainsea.chat.ui.activity.ClipImageActivity;
import tw.com.chainsea.chat.ui.dialog.IosProgressDialog;
import tw.com.chainsea.chat.ui.fragment.BaseFragment;
import tw.com.chainsea.custom.view.alert.AlertView;

public class GroupUpgradeFragment extends BaseFragment {
    public static final String TAG = GroupUpgradeFragment.class.getSimpleName();
    private Context context;
    private FragmentGroupUpgradeBinding binding;
    private static final int REQUEST_CARD = 1999;
    private static final int PROVE_DOC = 2000;
    private String avatarPath;
    private IosProgressDialog iosProgressDialog;
    //    private Gson gson;
    private String tenantId;
    private String avatarId;
    private String tenantName;
    private String description;
    private String supportFileId;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        iosProgressDialog = new IosProgressDialog(context);
    }

    public static GroupUpgradeFragment newInstance(String tenantId, String avatarId, String name, String description) {
        GroupUpgradeFragment fragment = new GroupUpgradeFragment();
        Bundle bundle = new Bundle();
        bundle.putString("tenantId", tenantId);
        bundle.putString("avatarId", avatarId);
        bundle.putString("tenantName", name);
        bundle.putString("tenantDescription", description);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            tenantId = bundle.getString("tenantId");
            avatarId = bundle.getString("avatarId");
            tenantName = bundle.getString("tenantName");
            description = bundle.getString("tenantDescription");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_group_upgrade, container, false);
        ViewGroup.LayoutParams params = binding.viewSpace.getLayoutParams();
        params.height = getStatusHeight();
        binding.viewSpace.setLayoutParams(params);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.txtTitle.setText("升級團隊");
        binding.btnClose.setOnClickListener(v -> getActivity().onBackPressed());
        binding.layoutAvatar.setOnClickListener(v -> editAvatar());
        CpApiManager.getInstance().tenantDictionaryIndustry(requireActivity(), TokenPref.getInstance(requireActivity()).getTokenId(), new CpApiListener<String>() {
            @Override
            public void onSuccess(String s) {
                IndustryScaleResponse industryJson = JsonHelper.getInstance().from(s, IndustryScaleResponse.class);
                ArrayList<String> industryList = new ArrayList<>();
                for (IndustryScaleResponse.DictionaryItems industry : industryJson.getDictionaryItems()) {
                    industryList.add(industry.getText());
                }
                MyOptionsPickerView<String> typePicker = new MyOptionsPickerView<>(context);
                typePicker.setPicker(industryList);
                typePicker.setTitle("請選擇您的產業類別");
                Button btnSubmit = (Button) typePicker.getBtnSubmit();
                btnSubmit.setText("確認");
                btnSubmit.setOnClickListener(v -> {
                    int[] currentItems = typePicker.getWheelOptions().getCurrentItems();
                    binding.txtChooseType.setText(industryList.get(currentItems[0]));
                    typePicker.dismiss();
                });
                Button btnCancel = (Button) typePicker.getBtnCancel();
                btnCancel.setText("取消");
                btnCancel.setOnClickListener(v -> typePicker.dismiss());
                typePicker.setCyclic(false);
                typePicker.setSelectOptions(0);
                typePicker.setOnoptionsSelectListener((options1, option2, options3) -> {
                    binding.txtChooseType.setText(industryList.get(options1));
                    typePicker.dismiss();
                    Toast.makeText(context, "" + industryList.get(options1), Toast.LENGTH_SHORT).show();
                });
                binding.txtChooseType.setOnClickListener(v -> typePicker.show());
            }

            @Override
            public void onFailed(String errorCode, String errorMessage) {

            }
        });

        CpApiManager.getInstance().tenantDictionaryScale(requireActivity(), TokenPref.getInstance(requireActivity()).getTokenId(), new CpApiListener<String>() {
            @Override
            public void onSuccess(String s) {
                IndustryScaleResponse scaleJson = JsonHelper.getInstance().from(s, IndustryScaleResponse.class);
                ArrayList<String> scaleList = new ArrayList<>();
                for (IndustryScaleResponse.DictionaryItems scale : scaleJson.getDictionaryItems()) {
                    scaleList.add(scale.getText());
                }
                MyOptionsPickerView<String> scalePicker = new MyOptionsPickerView<>(context);
                scalePicker.setPicker(scaleList);
                scalePicker.setTitle("請選擇您的公司規模");
                Button btnSubmit1 = (Button) scalePicker.getBtnSubmit();
                btnSubmit1.setText("確認");
                btnSubmit1.setOnClickListener(v -> {
                    int[] currentItems = scalePicker.getWheelOptions().getCurrentItems();
                    binding.txtChooseScale.setText(scaleList.get(currentItems[0]));
                    scalePicker.dismiss();
                });
                Button btnCancel1 = (Button) scalePicker.getBtnCancel();
                btnCancel1.setText("取消");
                btnCancel1.setOnClickListener(v -> scalePicker.dismiss());
                scalePicker.setCyclic(false);
                scalePicker.setSelectOptions(0);
                scalePicker.setOnoptionsSelectListener((options1, option2, options3) -> {
                    binding.txtChooseScale.setText(scaleList.get(options1));
                    scalePicker.dismiss();
                    Toast.makeText(context, "" + scaleList.get(options1), Toast.LENGTH_SHORT).show();
                });
                binding.txtChooseScale.setOnClickListener(v -> scalePicker.show());
            }

            @Override
            public void onFailed(String errorCode, String errorMessage) {

            }
        });


        binding.edtName.setText(tenantName);
        binding.txtNameCount.setText(MessageFormat.format("{0}/20", tenantName.length()));
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

        binding.edtIntro.setText(description);
        binding.txtIntroCount.setText(MessageFormat.format("{0}/150", description.length()));
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

        AvatarService.post(requireActivity(), avatarId, PicSize.LARGE, binding.imgAvatar, R.drawable.custom_default_avatar);
        binding.clAddProve.setOnClickListener(this::uploadProveDocAction);
        binding.clDelPreview.setOnClickListener(this::delPreviewAction);
        binding.layoutConfirm.setOnClickListener(this::groupUpgradeAction);
    }

    private void delPreviewAction(View view) {
        binding.clDelPreview.setVisibility(View.INVISIBLE);
        binding.ivPreview.setImageBitmap(null);
        binding.clAddProve.setVisibility(View.VISIBLE);
        binding.viewSpace1.setVisibility(View.GONE);
        supportFileId = "";
    }

    private void uploadProveDocAction(View view) {
        XXPermissions.with(context).permission(CAMERA).request(new OnPermissionCallback() {
            @Override
            public void onGranted(List<String> permissions, boolean all) {
                new AlertView.Builder()
                    .setContext(context).setStyle(AlertView.Style.ActionSheet).setOthers(new String[]{"相簿", "照相機"}).setCancelText("取消").setOnItemClickListener((o, position) -> {
                        if (position == 0) {
                            Intent intent = new Intent(context, ClipImageActivity.class);
                            intent.putExtra(Constant.INTENT_IMAGE_GET_WAY, Constant.INTENT_CODE_ALBUM);
                            intent.putExtra(Constant.INTENT_IMAGE_CONFIRM_TYPE, Constant.INTENT_CODE_NO_CROP);
                            startActivityForResult(intent, PROVE_DOC);
                        } else if (position == 1) {
                            Intent intent = new Intent(context, ClipImageActivity.class);
                            intent.putExtra(Constant.INTENT_IMAGE_GET_WAY, Constant.INTENT_CODE_CAMERA);
                            intent.putExtra(Constant.INTENT_IMAGE_CONFIRM_TYPE, Constant.INTENT_CODE_NO_CROP);
                            startActivityForResult(intent, PROVE_DOC);
                        }
                    }).build()
                    .setCancelable(true)
                    .show();
            }

            @Override
            public void onDenied(List<String> permissions, boolean never) {
                Toast.makeText(requireActivity(), requireActivity().getString(R.string.text_need_camera_permission), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void editAvatar() {
        XXPermissions.with(context).permission(CAMERA).request(new OnPermissionCallback() {
            @Override
            public void onGranted(List<String> permissions, boolean all) {
                new AlertView.Builder().setContext(context).setStyle(AlertView.Style.ActionSheet).setOthers(new String[]{"相簿", "照相機"}).setCancelText("取消")
                    .setOnItemClickListener((o, position) -> {
                        if (position == 0) {
                            Intent intent = new Intent(context, ClipImageActivity.class);
                            intent.putExtra(Constant.INTENT_IMAGE_GET_WAY, Constant.INTENT_CODE_ALBUM);
                            intent.putExtra(Constant.INTENT_IMAGE_CONFIRM_TYPE, Constant.INTENT_CODE_NO_CROP);
                            startActivityForResult(intent, REQUEST_CARD);
                        } else if (position == 1) {
                            Intent intent = new Intent(context, ClipImageActivity.class);
                            intent.putExtra(Constant.INTENT_IMAGE_GET_WAY, Constant.INTENT_CODE_CAMERA);
                            intent.putExtra(Constant.INTENT_IMAGE_CONFIRM_TYPE, Constant.INTENT_CODE_CAMERA);
                            startActivityForResult(intent, REQUEST_CARD);
                        }
                    }).build()
                    .setCancelable(true)
                    .show();
            }

            @Override
            public void onDenied(List<String> permissions, boolean never) {
                Toast.makeText(requireActivity(), requireActivity().getString(R.string.text_need_camera_permission), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CARD:
                    if (NetworkUtils.isNetworkAvailable(context)) {
                        String imageName = data.getStringExtra(BundleKey.RESULT_PIC_URI.key());
                        ImageEntity imageEntity = DaVinci.with().getImageLoader().getImage(imageName);
                        avatarPath = DaVinci.with().getImageLoader().getAbsolutePath(imageName);
                        binding.imgAvatar.setImageBitmap(imageEntity.getBitmap());
                    } else {
                        ToastUtils.showToast(context, getResources().getString(R.string.network_error));
                    }
                    break;
                case PROVE_DOC:
                    String imageName = data.getStringExtra(BundleKey.RESULT_PIC_URI.key());
                    ImageEntity imageEntity = DaVinci.with().getImageLoader().getImage(imageName);
                    String path = DaVinci.with().getImageLoader().getAbsolutePath(imageName);
                    binding.ivPreview.setImageBitmap(imageEntity.getBitmap());
                    binding.clAddProve.setVisibility(View.INVISIBLE);
                    binding.clDelPreview.setVisibility(View.VISIBLE);
                    binding.viewSpace1.setVisibility(View.VISIBLE);
                    iosProgressDialog.show("上傳中...");
                    CpApiManager.getInstance().tenantSupportFileUpload(requireActivity(), tenantId, path, new CpApiListener<String>() {
                        @Override
                        public void onSuccess(String s) {
                            iosProgressDialog.dismiss();
                            Toast.makeText(requireActivity(), "上傳完成", Toast.LENGTH_SHORT).show();
                            try {
                                JSONObject jsonObject = new JSONObject(s);
                                supportFileId = jsonObject.getString("supportFileId");
                            } catch (JSONException ignored) {

                            }
                        }

                        @Override
                        public void onFailed(String errorCode, String errorMessage) {
                            iosProgressDialog.dismiss();
                        }
                    });
                    break;
            }

        }
    }

    private void groupUpgradeAction(View view) {
        Editable name = binding.edtName.getText();
        Editable description = binding.edtIntro.getText();
        String industry = binding.txtChooseType.getText().toString();
        String scale = binding.txtChooseScale.getText().toString();
        if (name == null || name.length() == 0) {
            ToastUtils.showToast(context, "請輸入團隊名稱");
        } else if (description == null || description.length() == 0) {
            ToastUtils.showToast(context, "請輸入團隊說明");
        } else if ("請選擇您的產業類別".equals(industry)) {
            ToastUtils.showToast(context, "請選擇您的產業類別");
        } else if ("請選擇您的公司規模".equals(scale)) {
            ToastUtils.showToast(context, "請選擇您的公司規模");
        } else if (supportFileId == null || supportFileId.isEmpty()) {
            ToastUtils.showToast(context, "請上傳公司證明文件");
        } else {
            iosProgressDialog.show("團隊升級中...");
            CpApiManager.getInstance().tenantUpgrade(context, tenantId, name.toString(), description.toString(), industry,
                scale, supportFileId, avatarPath,
                new CpApiListener<>() {
                    @Override
                    public void onSuccess(String s) {
                        Log.d("TAG", s);
                        requireActivity().onBackPressed();
                    }

                    @Override
                    public void onFailed(String errorCode, String errorMessage) {
                        Log.d("TAG", errorCode + errorMessage);
                        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show());
                        iosProgressDialog.dismiss();
                    }
                });
        }
    }


}
