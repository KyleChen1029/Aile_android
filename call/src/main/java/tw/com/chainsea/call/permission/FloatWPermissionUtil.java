/*
 * Copyright (C) 2016 Facishare Technology Co., Ltd. All Rights Reserved.
 */
package tw.com.chainsea.call.permission;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import ezy.assist.compat.SettingsCompat;

/**
 * Description:
 *
 * @author zhaozp
 * @since 2016-10-17
 */

public class FloatWPermissionUtil {
    private static final String TAG = "FloatWPermissionUtil";

    private static volatile FloatWPermissionUtil instance;

    private Dialog dialog;

    public static FloatWPermissionUtil getInstance() {
        if (instance == null) {
            synchronized (FloatWPermissionUtil.class) {
                if (instance == null) {
                    instance = new FloatWPermissionUtil();
                }
            }
        }
        return instance;
    }

    public boolean checkPermission(Context context) {
        return SettingsCompat.canDrawOverlays(context);

    }

    public void applyPermission(final Context context) {
        showConfirmDialog(context, new OnConfirmResult() {
            @Override
            public void confirmResult(boolean confirm) {
                if (confirm) {
                    try {
                        SettingsCompat.manageDrawOverlays(context);
                    } catch (Exception e) {//抛出異常就直接打开设置页面
                        Uri uri=Uri.parse("package:"+"tw.com.chainsea.chat");//包名，指定该应用
                        Intent intent=new Intent("android.settings.APPLICATION_DETAILS_SETTINGS", uri);
                        context.startActivity(intent);
                    }
                }
            }
        });
    }

    private void showConfirmDialog(Context context, OnConfirmResult result) {
        showConfirmDialog(context, "您的手機沒有授予懸浮窗權限，請開啟後再試", result);
    }

    private void showConfirmDialog(Context context, String message, final OnConfirmResult result) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }

        dialog = new AlertDialog.Builder(context).setCancelable(true).setTitle("")
                .setMessage(message)
                .setPositiveButton("現在開啟",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirmResult(true);
                                dialog.dismiss();
                            }
                        }).setNegativeButton("暫不開啟",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirmResult(false);
                                dialog.dismiss();
                            }
                        }).create();
        dialog.show();
    }

    private interface OnConfirmResult {
        void confirmResult(boolean confirm);
    }

}
