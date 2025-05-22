package tw.com.chainsea.chat.ui.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import tw.com.chainsea.chat.util.DaVinci;
import cn.hadcn.davinci.image.base.ImageEntity;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.ce.sdk.bean.account.AccountType;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.database.sp.UserPref;
import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.http.ce.request.UploadManager;
import tw.com.chainsea.ce.sdk.http.ce.request.UserProfileRequest;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.base.Constant;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.databinding.FragmentUploadAvaterBinding;
import tw.com.chainsea.chat.lib.ActivityManager;
import tw.com.chainsea.chat.lib.ToastUtils;
import tw.com.chainsea.chat.lib.Tools;
import tw.com.chainsea.chat.ui.activity.ClipImageActivity;
import tw.com.chainsea.chat.ui.utils.permissionUtils.DialogUtil;
import tw.com.chainsea.chat.ui.utils.permissionUtils.RequestCodeManger;
import tw.com.chainsea.chat.ui.utils.permissionUtils.XPermissionUtils;
import tw.com.chainsea.chat.util.IntentUtil;
import tw.com.chainsea.chat.view.base.HomeActivity;
import tw.com.chainsea.custom.view.alert.AlertView;
import tw.com.chainsea.custom.view.progress.IosProgressBar;


/**
 * Created by sunhui on 2017/12/19.
 */
@SuppressLint("ValidFragment")
public class UploadAvaterFragment extends BaseFragment implements UserProfileRequest.Listener {

    private FragmentUploadAvaterBinding binding;

    private static final int RESULT_AVATAR = 1;
    //    private ProgressDialog progressDialog;
    private IosProgressBar iosProgressBar;
    private String mToken;
    private String mImgName;
    private boolean isUploadAvaterSuccess;
    private boolean isUpdateNicknameSuccess;
    private Bundle mBundle;
    private final String mNickName;

    @SuppressLint("ValidFragment")
    public UploadAvaterFragment(Bundle bundle, String nickName) {
        mBundle = bundle;
        mNickName = nickName;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUploadAvaterBinding.inflate(inflater, container, false);
//        mToken = TokenSave.getInstance(getActivity()).getToken();
        mToken = TokenPref.getInstance(getActivity()).getTokenId();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initListener();
    }

    private void initListener() {
        binding.ivAvatar.setOnClickListener(this::odCameraOrPicPickAction);
        binding.imgCamera.setOnClickListener(this::odCameraOrPicPickAction);
    }

    public void odCameraOrPicPickAction(View v) {
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

        XPermissionUtils.requestPermissions(getActivity(), RequestCodeManger.CAMERA, new String[]{Manifest.permission.CAMERA},
            new XPermissionUtils.OnPermissionListener() {
                @Override
                public void onPermissionGranted() {
//                        if (PermissionHelper.isCameraEnable()) {
                    showCameraOrPicPick();
//                            CELog.e("没收到权限回调");
//                        } else {
//                            DialogUtil.showPermissionManagerDialog(getActivity(), "相機");
//                        }
                }

                @Override
                public void onPermissionDenied(final String[] deniedPermissions, boolean alwaysDenied) {
                    Toast.makeText(getActivity(), "獲取相機權限失敗", Toast.LENGTH_SHORT).show();
                    // 拒绝后不再询问 -> 提示跳转到设置
                    if (alwaysDenied) {
                        DialogUtil.showPermissionManagerDialog(getActivity(), "相機");
                    } else {    // 拒绝 -> 提示此公告的意义，并可再次尝试获取权限

                        new AlertView.Builder()
                            .setContext(getContext())
                            .setStyle(AlertView.Style.Alert)
                            .setTitle("溫馨提示")
                            .setMessage("我們需要相機權限才能正常使用該功能")
                            .setOthers(new String[]{"取消", "驗證權限"})
                            .setOnItemClickListener((o, position) -> {
                                if (position == 1) {
                                    XPermissionUtils.requestPermissionsAgain(getActivity(), deniedPermissions, RequestCodeManger.CAMERA);
                                }
                            }).build()
                            .show();


//                            new AlertDialog.Builder(getActivity()).setTitle("溫馨提示")
//                                    .setMessage("我們需要相機權限才能正常使用該功能")
//                                    .setNegativeButton("取消", null)
//                                    .setPositiveButton("驗證權限", new DialogInterface.OnClickListener() {
//                                        @RequiresApi(api = Build.VERSION_CODES.M)
//                                        @Override
//                                        public void onClick(DialogInterface dialog, int which) {
//                                            XPermissionUtils.requestPermissionsAgain(getActivity(), deniedPermissions,
//                                                    RequestCodeManger.CAMERA);
//                                        }
//                                    }).show();
                    }
                }
            });
    }

    private void showCameraOrPicPick() {
//        showAvatarMenu();
        new AlertView.Builder()
            .setContext(getContext())
            .setStyle(AlertView.Style.ActionSheet)
            .setOthers(new String[]{getString(R.string.warning_photos), getString(R.string.warning_camera)})
            .setCancelText("取消")
            .setOnItemClickListener((o, position) -> {
                if (position == 0) {
                    Intent intent = new Intent(getActivity(), ClipImageActivity.class);
                    intent.putExtra(Constant.INTENT_IMAGE_GET_WAY, Constant.INTENT_CODE_ALBUM);
                    intent.putExtra(Constant.INTENT_IMAGE_CONFIRM_TYPE, Constant.INTENT_CODE_CROP);
                    startActivityForResult(intent, RESULT_AVATAR);
                } else if (position == 1) {
                    Intent intent = new Intent(getActivity(), ClipImageActivity.class);
                    intent.putExtra(Constant.INTENT_IMAGE_GET_WAY, Constant.INTENT_CODE_CAMERA);
                    intent.putExtra(Constant.INTENT_IMAGE_CONFIRM_TYPE, Constant.INTENT_CODE_CROP);
                    startActivityForResult(intent, RESULT_AVATAR);
                }
            })
            .build()
            .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == RESULT_AVATAR) {
                mImgName = data.getStringExtra(BundleKey.RESULT_PIC_URI.key());
                CELog.e("获取个人头像：", mImgName);
                loadImage();
            }
        }
    }

    public void upload() {
        String nickName = binding.editNick.getText().toString().trim();
        if (TextUtils.isEmpty(nickName)) {
            Toast.makeText(getContext(), "幫自己設置專屬暱稱，讓朋友更快認識你...^^", Toast.LENGTH_SHORT).show();
            return;
        }
        if (nickName.length() > 20 && !TextUtils.isEmpty(nickName)) {
            ToastUtils.showToast(getContext(), "暱稱不能超過20個字");
            return;
        }
        if (TextUtils.isEmpty(mImgName) && !TextUtils.isEmpty(nickName)) {
            Toast.makeText(getContext(), "為自己添加個人圖片，讓朋友更快認識你...^^", Toast.LENGTH_SHORT).show();
            return;
        }
        CELog.e("上传个人头像：", mImgName);
        showLoadingView();
        String path = DaVinci.with().getImageLoader().getAbsolutePath(mImgName);
        UploadManager.getInstance().uploadAvatar(getContext(), mToken, path, Tools.getSize(path), new UploadManager.OnUploadAvatarListener() {
            @Override
            public void onUploadSuccess(String url) {
                isUploadAvaterSuccess = true;
//                String avatarUrl = NetConfig.getInstance().assembleAvatarUrl(url, PicSize.SMALL);
                if (TextUtils.isEmpty(url)) {
                    return;
                }
                loadImage();
                if (isUpdateNicknameSuccess && isUploadAvaterSuccess) {
//                    hideLoadingView();
                    skipto();
                }
            }

            @Override
            public void onUploadFailed(String reason) {
                hideLoadingView();
                CELog.e(reason);
                ToastUtils.showToast(getActivity(), reason);
            }
        });
        ApiManager.getInstance().updateProfile_name(getContext(), nickName, new ApiListener<String>() {
            @Override
            public void onSuccess(String s) {
                isUpdateNicknameSuccess = true;
                if (isUpdateNicknameSuccess && isUploadAvaterSuccess) {
//                    hideLoadingView();
                    skipto();
                }
            }

            @Override
            public void onFailed(String errorMessage) {
                hideLoadingView();
                if (!TextUtils.isEmpty(errorMessage)) {
                    ToastUtils.showToast(getActivity(), errorMessage);
                }
            }
        });
    }

    public void skipto() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getActivity().getWindow().getDecorView().getWindowToken(),
                0);
        }
        ApiManager.doUserProfile(getContext(), this);
    }

    private void loadImage() {
        ImageEntity entity = DaVinci.with().getImageLoader().getImage(mImgName);
        Bitmap bitmap = entity.getBitmap();
        binding.ivAvatar.setImageBitmap(bitmap);
    }

    public void showLoadingView() {
        if (iosProgressBar == null) {
            iosProgressBar = IosProgressBar.show(getContext(), getString(R.string.wording_loading), true, false, dialog -> {
            });
        }

    }

    public void hideLoadingView() {
        if (iosProgressBar != null && iosProgressBar.isShowing()) {
            iosProgressBar.dismiss();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    @Override
    public void onProfileSuccess(UserProfileEntity profile) {
        profile.setType(AccountType.SELF);

        String userId = profile.getId();
        // resume db
//        DBManager.init(getActivity());
//        AileDataBaseHelper.init(getActivity(), userId);
        CELog.startLogSave(requireContext(), userId);

        // save to db
        Log.e("LoginPresenter", "insertStart:" + System.currentTimeMillis());
        DBManager.getInstance().insertUserAndFriends(profile);
        Log.e("LoginPresenter", "insertGroup:" + System.currentTimeMillis());
//        Log.e("LoginPresenter", "insertProfile:" + b + " userId:" + profile.getId() + " name:" + profile.getNickname());
        // save user id
//        TokenPref.getInstance(getActivity()).setUserId(userId);
        UserPref.getInstance(getActivity()).saveUserType(profile.getUserType().getUserType());
//        TokenSave.getInstance(getActivity()).saveUserId(userId);

        String googleId = profile.getGoogleId();
        if (!TextUtils.isEmpty(googleId)) {
//            TokenSave.getInstance(getActivity()).saveGoogleValue(profile.getGoogleId());
        } else {
//            TokenSave.getInstance(getActivity()).clearGoogleValue();
        }


        String fbId = profile.getFbId();
        if (!TextUtils.isEmpty(fbId)) {
//            TokenSave.getInstance(getActivity()).saveFacebookValue(fbId);
        } else {
//            TokenSave.getInstance(getActivity()).clearFacebookValue();
        }

        String lineId = profile.getLineId();
        if (!TextUtils.isEmpty(lineId)) {
//            TokenSave.getInstance(getActivity()).saveLineValue(lineId);
        } else {
//            TokenSave.getInstance(getActivity()).clearLineValue();
        }
        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
            hideLoadingView();
            navigateToMain();
        });

    }

    @Override
    public void onInvalidAccPw() {
        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
            hideLoadingView();
        });
//        showToast(R.string.invalid_account_pw);
    }

    @Override
    public void onProfileFailed(String errorMessage) {
        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
            hideLoadingView();
            if (!TextUtils.isEmpty(errorMessage)) {
                ToastUtils.showToast(getActivity(), errorMessage);
            }
        });

    }

    @Override
    public void onThridLoginUnBind() {
//        toRegisterBecThird(mIdentifyBy, mIdentifyValue);
    }

    public void navigateToMain() {
        if (isAdded()) {
            Intent intent = new Intent(getActivity(), HomeActivity.class);
            if (mBundle != null) {
                intent.putExtras(mBundle);
            }
            ActivityManager.finishAll();
            IntentUtil.INSTANCE.start(requireContext(), intent);
//            UserPref.getInstance(getContext()).setLoginTag(true);
            requireActivity().finish();
        }
    }
}
