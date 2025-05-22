package tw.com.chainsea.chat.ui.utils.permissionUtils;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;

import tw.com.chainsea.chat.R;


/**
 * Dialog工具类
 *
 * @author AndSync
 * @date 2017/10/30
 * Copyright © 2014-2017 AndSync All rights reserved.
 */
public class DialogUtil {
    public static void showPermissionManagerDialog(final Context context, String str) {
        new AlertDialog.Builder(context).setTitle("獲取" + str + "權限被禁用")
            .setMessage("請在 設置-應用管理-" + context.getString(R.string.app_name) + "-權限管理 (將" + str + "權限打開)")
            .setNegativeButton("確定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            })
//            .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                    intent.setProfileData(Uri.parse("package:" + context.getPackageName()));
//                    context.startActivity(intent);
//                }
//            })
            .show();
//        final MaterialDialog mDialog = new MaterialDialog(context);
////        mDialog.setCanceledOnTouchOutside(false);
////        mDialog.setCancelable(false);
//        mDialog.title("獲取" + str + "權限被禁用")
//                .btnNum(1)
//                .content("請參考一下方法\n在 設置-應用管理-" + context.getString(R.string.app_name) + "-權限管理 (將" + str + "權限打開)")
////                .btnText("取消","去設置")
//                .btnText("確定")
//                .show();
//        mDialog.setOnBtnClickL(new OnBtnClickL() {
//            @Override
//            public void onBtnClick() {
//                mDialog.dismiss();
//            }
//        });
    }

    public static void showLocServiceDialog(final Context context) {
        new AlertDialog.Builder(context).setTitle("手机未开启位置服务")
            .setMessage("请在 设置-系統安全-位置信息 (将位置服务打开))")
            .setNegativeButton("取消", null)
            .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        context.startActivity(intent);
                    } catch (ActivityNotFoundException ex) {
                        intent.setAction(Settings.ACTION_SETTINGS);
                        try {
                            context.startActivity(intent);
                        } catch (Exception ignored) {

                        }
                    }
                }
            }).show();

//        final MaterialDialog mDialog = new MaterialDialog(context);
////        mDialog.setCanceledOnTouchOutside(false);
////        mDialog.setCancelable(false);
//        mDialog.title("手機未開啟位置服務")
//                .btnNum(1)
//                .content("請參考一下方法\n在 設置-系統安全-位置信息 (將位置服務打開))")
//                .btnText("確定")
//                .show();
//        mDialog.setOnBtnClickL(new OnBtnClickL() {
//            @Override
//            public void onBtnClick() {
//                mDialog.dismiss();
//            }
//        });
    }
}
