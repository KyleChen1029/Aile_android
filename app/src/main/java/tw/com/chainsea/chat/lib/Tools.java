package tw.com.chainsea.chat.lib;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.UUID;

import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.bean.msg.MessageType;

/**
 * Tools for system
 * Created by 90Chris on 2014/11/12.
 */
public class Tools {
    private static final String TAG = "Tools";
    /**
     * calculate the proper times to reduce the Bitmap
     *
     * @param options   options
     * @param reqWidth  required width
     * @param reqHeight required height
     * @return sample size
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        CELog.i("height = " + height + ", width = " + width);
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    /**
     * create a unique file name according type, uid and current time.
     *
     * @param uid  user id
     * @param type
     * @return unique name
     */
    public static String createName(String uid, MessageType type) {
        String name = null;
        switch (type) {
            case IMAGE:
                name = "android_chat_" + uid + "_" + System.currentTimeMillis() + ".jpeg";
                break;
            case VOICE:
                name = "android_chat_" + uid + "_" + System.currentTimeMillis() + ".spx";
                break;
            default:
                break;
        }
        return name;
    }

    public static String generateMessageId() {
        return String.valueOf(UUID.randomUUID());
    }

    /**
     * change file to base64
     *
     * @param path file path
     * @return base64
     * @throws Exception
     */
    public static String encodeBase64File(String path) throws Exception {
        File file = new File(path);
        FileInputStream inputFile = new FileInputStream(file);
        byte[] buffer = new byte[(int) file.length()];
        inputFile.read(buffer);
        inputFile.close();
        return Base64.encodeToString(buffer, Base64.DEFAULT);
    }

    public static int getSize(String path) {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        return bitmap.getWidth();
    }

    public static String getNameWithoutExt(String path) {
        int l_Index = path.lastIndexOf('/');
        int p_index = path.lastIndexOf('.');
        return path.substring(l_Index + 1, p_index);
    }

    /**
     * 检测应用是否运行
     *
     * @param packageName 包名
     * @param context     上下文
     * @return 是否存在
     */
    public static boolean isAppAlive(String packageName, Context context) {
        if (context == null || TextUtils.isEmpty(packageName)) {
            return false;
        }

        ActivityManager activityManager = (ActivityManager)
                context.getSystemService(Context.ACTIVITY_SERVICE);

        if (activityManager != null) {
            List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
            if (procInfos != null && !procInfos.isEmpty()) {
                for (int i = 0; i < procInfos.size(); i++) {
                    if (procInfos.get(i).processName.equals(packageName)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    //创建TextView用作Activity的rightView
    public static TextView createRightView(Context context, int resId) {
        TextView tvRight = new TextView(context);
        tvRight.setText(resId);
        tvRight.setTextSize(17);
        tvRight.setPadding(10, 0, 16, 0);
        tvRight.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        tvRight.setGravity(Gravity.CENTER);
        tvRight.setTextColor(Color.WHITE);
        return tvRight;
    }

    public static String assemblerRoomId(String selfUserId, String otherUserId) {
        String roomId;
        String userId1 = selfUserId.replaceAll("-", "");
        String userId2 = otherUserId.replaceAll("-", "");
        long i = Long.parseLong(userId1);
        long j = Long.parseLong(userId2);
        if (i > j) {
            roomId = otherUserId + "," + selfUserId;
        } else {
            roomId = selfUserId + "," + otherUserId;
        }
        return roomId;
    }

    /**
     * 根据Uri返回文件绝对路径
     * 兼容了file:///开头的 和 content://开头的情况
     */
    public static String getRealFilePathFromUri(final Context context, final Uri uri) {
        if (null == uri) {
            return null;
        }
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_FILE.equalsIgnoreCase(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equalsIgnoreCase(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

//    public static String getApplicationName() {
//        PackageManager packageManager = null;
//        ApplicationInfo applicationInfo = null;
//        try {
//            packageManager = getApplicationContext().getPackageManager();
//            applicationInfo = packageManager.getApplicationInfo(getApplicationContext().getPackageName(), 0);
//        } catch (PackageManager.NameNotFoundException e) {
//            applicationInfo = null;
//        }
//        String applicationName =
//                (String) packageManager.getApplicationLabel(applicationInfo);
//        return applicationName;
//    }

}
