package tw.com.chainsea.chat.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;

import tw.com.chainsea.chat.util.DaVinci;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import tw.com.chainsea.ce.sdk.bean.msg.MessageType;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.base.Constant;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.databinding.ActivityClipImgeBinding;
import tw.com.chainsea.chat.lib.Tools;
import tw.com.chainsea.chat.ui.utils.permissionUtils.DialogUtil;
import tw.com.chainsea.chat.ui.utils.permissionUtils.RequestCodeManger;
import tw.com.chainsea.chat.ui.utils.permissionUtils.XPermissionUtils;
import tw.com.chainsea.chat.util.DownloadUtil;


public class ClipImageActivity extends AppCompatActivity {
    private ActivityClipImgeBinding binding;

    final int SMALL_PIC_SIZE = 450;
    final String IMAGE_FILE_NAME = "pic.jpg";
    File picTempFile = null;

    private ActivityResultLauncher<Intent> galleryARL = null, cameraARL = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_clip_imge);
        int way = getIntent().getIntExtra(Constant.INTENT_IMAGE_GET_WAY, Constant.INTENT_CODE_ALBUM);
        int type = getIntent().getIntExtra(Constant.INTENT_IMAGE_CONFIRM_TYPE, Constant.INTENT_CODE_NO_CROP);

        galleryARL = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                if (result.getData() != null) {
                    Uri uri = result.getData().getData();
                    binding.clipView.setImageSrc(uri);
                }
            } else
                closePage();
        });

        cameraARL = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                binding.clipView.setImageSrc(Uri.fromFile(picTempFile));
            } else
                closePage();
        });

        if (way == Constant.INTENT_CODE_CAMERA) {
            Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            DownloadUtil.INSTANCE.getNotDuplicateFileForJava(DownloadUtil.INSTANCE.getDownloadImageDir(), IMAGE_FILE_NAME, new Function1<File, Unit>() {
                @Override
                public Unit invoke(File file) {
                    picTempFile = file;
                    Uri uri = FileProvider.getUriForFile(ClipImageActivity.this, getPackageName() + ".fileprovider", picTempFile);
                    XPermissionUtils.requestPermissions(ClipImageActivity.this, RequestCodeManger.CAMERA, new String[]{Manifest.permission.CAMERA},
                        new XPermissionUtils.OnPermissionListener() {
                            @Override
                            public void onPermissionGranted() {
//                            if (PermissionHelper.isCameraEnable()) {
                                openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                                cameraARL.launch(openCameraIntent);
//                            } else {
//                                DialogUtil.showPermissionManagerDialog(ClipImageActivity.this, "相機");
//                            }
                            }

                            @Override
                            public void onPermissionDenied(final String[] deniedPermissions, boolean alwaysDenied) {
                                Toast.makeText(ClipImageActivity.this, "獲取相機權限失敗", Toast.LENGTH_SHORT).show();
                                // 拒绝后不再询问 -> 提示跳转到设置
                                if (alwaysDenied) {
                                    DialogUtil.showPermissionManagerDialog(ClipImageActivity.this, "相機");
                                } else {    // 拒绝 -> 提示此公告的意义，并可再次尝试获取权限
                                    new AlertDialog.Builder(ClipImageActivity.this).setTitle("溫馨提示")
                                        .setMessage("我們需要相機權限才能正常使用該功能")
                                        .setNegativeButton("取消", null)
                                        .setPositiveButton("驗證權限", (dialog, which) -> XPermissionUtils.requestPermissionsAgain(ClipImageActivity.this, deniedPermissions, RequestCodeManger.CAMERA))
                                        .show();
                                }
                            }
                        });
                    return null;
                }
            });
        } else {
            Intent intentFromGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryARL.launch(intentFromGallery);
        }

        binding.ivBack.setOnClickListener(v -> closePage());
        binding.btnCancel.setOnClickListener(v -> closePage());
        binding.btOk.setOnClickListener(v -> confirmImage());
    }

    private void closePage() {
        finish();
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
    }

    private void confirmImage() {
        Bitmap bitmap = binding.clipView.clip();
        String key = Tools.createName("uid_photo", MessageType.IMAGE);
        String key_small = "small" + key;
        /*compress the pic to stream*/
        ByteArrayOutputStream baoBig = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baoBig);

        DaVinci.with(this).getImageLoader().putImage(key, baoBig.toByteArray());

        ByteArrayOutputStream baoSmall = new ByteArrayOutputStream();
        zoomImage(bitmap, SMALL_PIC_SIZE).compress(Bitmap.CompressFormat.JPEG, 50, baoSmall);
        DaVinci.with(this).getImageLoader().putImage(key_small, baoSmall.toByteArray());

        Intent it = new Intent();
        it.putExtra(BundleKey.RESULT_PIC_URI.key(), key);
        it.putExtra(BundleKey.RESULT_PIC_SMALL_URI.key(), key_small);
        setResult(Activity.RESULT_OK, it);
        closePage();
    }

    public Bitmap zoomImage(Bitmap bitmap, int maxSize) {
        int outWidth;
        int outHeight;
        int inWidth = bitmap.getWidth();
        int inHeight = bitmap.getHeight();

        if (inWidth > inHeight) {
            outWidth = maxSize;
            outHeight = (inHeight * maxSize) / inWidth;
        } else {
            outHeight = maxSize;
            outWidth = (inWidth * maxSize) / inHeight;
        }

        return Bitmap.createScaledBitmap(bitmap, outWidth, outHeight, false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraARL.unregister();
        galleryARL.unregister();
    }
}
