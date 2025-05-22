package tw.com.chainsea.chat.view.vision.base;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import tw.com.chainsea.android.common.permission.PermissionHelper;
import tw.com.chainsea.android.common.ui.UiHelper;
import tw.com.chainsea.custom.view.alert.AlertView;


public abstract class BaseVisionPreviewActivity extends AppCompatActivity {

    int statusHeight;
    protected boolean CAN_NEXT = false;


    @ColorInt
    protected abstract int getStatusBarColor();

    protected abstract boolean isHideStatusBar();

    protected abstract void initData();

    protected abstract void setListener();

    protected abstract boolean doAdvanceExecution();

    protected abstract void doExecution();

    protected int getStatusBarHeight() {
        return this.statusHeight;
    }

    @Override
    @SuppressLint({"InternalInsetResource", "DiscouragedApi"})
    protected void onCreate(Bundle savedInstanceState) {
        if (android.os.Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            // only for gingerbread and newer versions
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
        super.onCreate(savedInstanceState);
        int resourceId = getApplicationContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            this.statusHeight = getApplicationContext().getResources().getDimensionPixelSize(resourceId);
        } else {
            this.statusHeight = UiHelper.dip2px(this, 24);
        }


        if (isHideStatusBar()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS); // 確認取消半透明設置。
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // 全螢幕顯示，status bar 不隱藏，activity 上方 layout 會被 status bar 覆蓋。
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE); // 配合其他 flag 使用，防止 system bar 改變後 layout 的變動。
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS); // 跟系統表示要渲染 system bar 背景。
            window.setStatusBarColor(Color.TRANSPARENT);
        }
//        else {
//            getWindow().setStatusBarColor(getStatusBarColor());
//        }

    }

    private void init() {
        initData();
        setListener();

        if (doAdvanceExecution()) {
            doExecution();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        getCameraPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        CAN_NEXT = true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionHelper.RequestCode.ACCESS_LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                init();
            } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                    shouldShowRequest();
                } else {
                    getCameraPermissions();
                }
            }
            return;
        }
    }

    private void getCameraPermissions() {
        PermissionHelper.checkCameraPermissions(this, statusResult -> {
            switch (statusResult) {
                case GRANTED:
                    init();
                    break;
                case DENIED:
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PermissionHelper.RequestCode.ACCESS_LOCATION_REQUEST_CODE);
                    break;
            }
        });
    }

    private void shouldShowRequest() {
        new AlertView.Builder()
            .setContext(this)
            .setStyle(AlertView.Style.Alert)
            .setMessage("相機權限申請\n請前往設定頁面開啟相機權限。")
            .setOthers(new String[]{"前往設定", "取消"})
            .setOnItemClickListener((o, position) -> {
                if (position == 0) {
                    startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", getPackageName(), null)));
                } else {
                    finish();
                }
            })
            .build()
            .setCancelable(false)
            .show();
    }
}
