package tw.com.chainsea.chat.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.lib.ToastUtils;
import tw.com.chainsea.chat.qrcode.QrCodeKit;
import tw.com.chainsea.chat.util.DownloadUtil;
import tw.com.chainsea.chat.zxing.encoding.EncodingHandler;

/**
 * Created by sunhui on 2018/3/2.
 */

public class QrCodeDialog extends Dialog implements View.OnClickListener {
    private ImageView avatar;
    private TextView name;
    private TextView duty;
    private ImageView qrCode;
    private final File storageDir = new File(DownloadUtil.INSTANCE.getDownloadImageDir());


    public QrCodeDialog(Context context) {
        //设置自定义的style
        this(context, R.style.ios_bottom_dialog);
    }

    public QrCodeDialog(Context context, int themeResId) {
        super(context, themeResId);
        setContentView(R.layout.qr_code_layout);
        initView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void initView() {
        avatar = findViewById(R.id.iv_avatar);
        name = findViewById(R.id.tv_name);
        duty = findViewById(R.id.tv_duty);
        qrCode = findViewById(R.id.qr_code);
        ImageView ivCopy = findViewById(R.id.iv_copy);

        ivCopy.setOnClickListener(this);

        this.setCanceledOnTouchOutside(true);
        this.setCancelable(true);
        Window window = this.getWindow();
        //让Dialog顯示在屏幕的底部
        assert window != null;
        window.setGravity(Gravity.CENTER);
        //设置窗口出现和窗口隐藏的动画
        window.setWindowAnimations(R.style.fade_dialog_anim);
        //设置BottomDialog的宽高属性
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        String userId = TokenPref.getInstance(this.getContext()).getUserId();
        UserProfileEntity account = DBManager.getInstance().querySelfAccount(userId);
        setQrCode(TokenPref.getInstance(this.getContext()).getCurrentTenantCode(),
                TokenPref.getInstance(this.getContext()).getCpAccountId(),
                TokenPref.getInstance(this.getContext()).getCpCurrentTenant().getTenantName(),
                userId,
                account.getNickName());
    }

    public ImageView getAvatar() {return avatar;}

    public void setAvatar(Drawable avatar) {
        this.avatar.setImageDrawable(avatar);
    }

    public void setAvatar(Bitmap avatar) {
        this.avatar.setImageBitmap(avatar);
    }

    public void setName(String name) {
        this.name.setText(name);
    }

    public void setDuty(String duty) {
        this.duty.setText(duty);
    }

    public void setQrCode(Bitmap qrCode) {
        this.qrCode.setImageBitmap(qrCode);
    }

    public void setQrCode(Drawable qrCode) {
        this.qrCode.setImageDrawable(qrCode);
    }

    private void setQrCode(String tenantCode, String accountId, String tenantName, String userId, String nickName) {
        String qrcode = new QrCodeKit().getUserInfoQrCode(tenantCode, accountId, tenantName, userId, nickName);
        try {
            Bitmap codeBitmap = EncodingHandler.createQRCode(qrcode, 600, 600, null);
            this.qrCode.setImageBitmap(codeBitmap);
        } catch (Exception ignored) {
            CELog.d("save qrCode id error");
        }
    }

    private void saveImage(Bitmap image) {
        String saveImagePath;
        String imageFileName = "JPEG_" + "down" + System.currentTimeMillis() + ".jpg";
        boolean success = true;
        if (!storageDir.exists()) {
            success = storageDir.mkdirs();
        }

        if (success) {
            File imageFile = new File(storageDir, imageFileName);
            saveImagePath = imageFile.getAbsolutePath();
            try {
                OutputStream fout = new FileOutputStream(imageFile);
                image.compress(Bitmap.CompressFormat.JPEG, 100, fout);
                fout.close();
            } catch (Exception ignored) {
            }

            galleryAddPic(saveImagePath);
        }
    }

    private void galleryAddPic(String imagePath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(imagePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.getContext().sendBroadcast(mediaScanIntent);
        ToastUtils.showToast(this.getContext(), this.getContext().getString(R.string.photo_save_to_phone));
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_copy) {
            qrCode.setDrawingCacheEnabled(true);
            saveImage(qrCode.getDrawingCache());
        }
    }
}
