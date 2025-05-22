package tw.com.chainsea.android.common.permission;

import static android.Manifest.permission.VIBRATE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Vibrator;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import tw.com.chainsea.android.common.permission.callback.PermissionCallBack;
import tw.com.chainsea.android.common.permission.callback.ServiceCallBack;

/**
 * Android SDK > Android M Permission related tools,
 * Obtain service tools
 *
 * @author Evan Wang
 * @version 0.0.1
 * @since 0.0.1
 */
public class PermissionHelper {
    public static class RequestCode {
        public static final int MULTI_REQUEST_CODE = 0x8000;
        public static final int READ_PHONE_STATE_REQUEST_CODE = 0x8001;
        public static final int ACCESS_LOCATION_REQUEST_CODE = 0x8002;
        public static final int ACCESS_WIFI_STATE_REQUEST_CODE = 0x8003;
        public static final int BLUETOOTH_REQUEST_CODE = 0x8004;
    }

    /**
     * Determine permissions and obtain phone management permissions
     *
     * @author Evan Wang
     * @since 0.0.1
     */
//    public static void getTelephonyManager(Activity activity, ServiceCallBack callBack) {
//        if (isLessThanSdkM(activity, callBack, Context.TELEPHONY_SERVICE)) {
//            return;
//        }
//        if (isSelfPermission(activity, READ_PHONE_STATE)) {
//            if (activity.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE) instanceof TelephonyManager) {
//                callBack.request(activity.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE));
//            }
//        } else {
//            ActivityCompat.requestPermissions(activity, new String[]{READ_PHONE_STATE}, RequestCode.READ_PHONE_STATE_REQUEST_CODE);
//        }
//    }

//    /**
//     * 判斷權限並取得 地理管理權限
//     *
//     * onRequestPermissionsResult.requestCode = {@link RequestCode.ACCESS_LOCATION_REQUEST_CODE }
//     * @param activity
//     * @param callBack
//     * @author Evan Wang
//     * @version 0.0.1
//     * @since 0.0.1
//     */
//    public static void getLocationManager(Activity activity, ServiceCallBack callBack) {
//        if (isLessThanSdkM(activity, callBack, Context.LOCATION_SERVICE)) {
//            return;
//        }
//        String[] permissions = new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION};
//        if (isSelfPermission(activity, permissions)) {
//            if (activity.getApplicationContext().getSystemService(Context.LOCATION_SERVICE) instanceof LocationManager) {
//                callBack.request(activity.getApplicationContext().getSystemService(Context.LOCATION_SERVICE));
//            }
//        } else {
//            ActivityCompat.requestPermissions(activity, permissions, RequestCode.ACCESS_LOCATION_REQUEST_CODE);
//        }
//    }


    public enum StatusResult {
        GRANTED,
        DENIED,
        EVER_DENIED
    }

    //
    public static void checkCameraPermissions(Activity activity, PermissionCallBack<StatusResult> callback) {
        if (!checkSDK_M()) {
//            String[] permissions = new String[]{Manifest.permission.CAMERA};
            StatusResult result = getPermissionStatus(activity, Manifest.permission.CAMERA);
//            if (StatusResult.DENIED.equals(result)) {
//                ActivityCompat.requestPermissions(activity, permissions, PermissionHelper.RequestCode.ACCESS_LOCATION_REQUEST_CODE);
//            }
            callback.request(result);
//            switch (result) {
//                case GRANTED:
//                    callback.request(StatusResult.GRANTED);
//                case DENIED:
//                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)) {
//
//                        new AlertDialog.Builder(activity).setTitle("獲取" + "" + "權限被禁用")
//                                .setMessage("請在 設置-應用管理-" + activity.getString(R.string.app_name) + "-權限管理 (將" + "" + "權限打開)")
//                                .setNegativeButton("確定", (dialog, which) -> {
//                                    Log.i("","");
//                                }).show();
//                    } else {
//                        // No explanation needed, we can request the permission.
//                        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, RequestCode.ACCESS_LOCATION_REQUEST_CODE);
//                    }
//                    return false;
////                case EVER_DENIED:
////                    ActivityCompat.requestPermissions(activity, permissions, RequestCode.ACCESS_LOCATION_REQUEST_CODE);
////                    return false;
//            }

//            if (isPermission(activity, Manifest.permission.CAMERA)) {
//                return true;
//            } else {
//                ActivityCompat.requestPermissions(activity, permissions, RequestCode.ACCESS_LOCATION_REQUEST_CODE);
//                return false;
//            }
        }
//        return true;
    }


    /**
     * 判斷權限並取得 WiFi管理權限
     * @author Evan Wang
     * @since 0.0.1
     */
//    public static void getWifiManager(Activity activity, ServiceCallBack callBack) {
//        if (isLessThanSdkM(activity, callBack, Context.WIFI_SERVICE)) {
//            return;
//        }
//        String[] permissions = new String[]{CHANGE_WIFI_STATE, ACCESS_WIFI_STATE};
//        if (isSelfPermission(activity, permissions)) {
//            if (activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE) instanceof WifiManager) {
//                callBack.request(activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE));
//            }
//        } else {
//            ActivityCompat.requestPermissions(activity, permissions, RequestCode.ACCESS_WIFI_STATE_REQUEST_CODE);
//        }
//    }

    /**
     * 取得震動元件
     *
     * @author Evan Wang
     * @since 0.0.1
     */
    public static void getVibrator(Activity activity, ServiceCallBack callBack) {
        if (isLessThanSdkM(activity, callBack, Context.VIBRATOR_SERVICE)) {
            return;
        }
        String[] permissions = new String[]{VIBRATE};
        if (isSelfPermission(activity, permissions)) {
            if (activity.getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE) instanceof Vibrator) {
                callBack.request(activity.getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE));
            }
        } else {
            ActivityCompat.requestPermissions(activity, permissions, RequestCode.BLUETOOTH_REQUEST_CODE);
        }
    }

    /**
     * 判斷Android SDK < Android M (6) (23)
     *
     * @author Evan Wang
     * @since 0.0.1
     */
    private static boolean isLessThanSdkM(Activity activity, ServiceCallBack callBack, String name) {
        if (checkSDK_M()) {
            callBack.request(activity.getApplicationContext().getSystemService(name));
            return true;
        }
        return false;
    }


    public static boolean checkSDK_M() {
        return false;
    }

    @SuppressLint("SwitchIntDef")
    public static StatusResult getPermissionStatus(Activity activity, String permission) {
//        int result1 = ContextCompat.checkSelfPermission(activity, permission);
        int result = PermissionChecker.checkSelfPermission(activity, permission);
        Log.i("sfsdfs", result + "");
        return switch (result) {
            case PackageManager.PERMISSION_GRANTED -> StatusResult.GRANTED;
            case PackageManager.PERMISSION_DENIED -> StatusResult.DENIED;
//                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
//                    return StatusResult.EVER_DENIED;
//                } else {
//                    return StatusResult.DENIED;
//                }
            default -> StatusResult.EVER_DENIED;
        };
    }

    /**
     * 判斷是否有權限
     *
     * @author Evan Wang
     * @since 0.0.1
     */
    public static boolean isSelfPermission(Activity activity, String[] permissions) {
        boolean status = false;
        for (int i = 0; i < permissions.length; i++) {
            status = ActivityCompat.checkSelfPermission(activity, permissions[i]) == PackageManager.PERMISSION_GRANTED;
        }
        return status;
    }

    //    public static boolean isSelfPermission(Activity activity, String permission) {
//        return ActivityCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED;
//    }


    /**
     * 6.0以下判断是否开启录音权限
     */
    @SuppressLint("MissingPermission")
    public static boolean isAudioEnable() {
        boolean isValid = true;
        AudioRecord mRecorder;
        int bufferSize =
            AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        short[] mBuffer = new short[bufferSize];
        mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000, AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        //开始录制音频
        try {
            // 防止某些手机崩溃，例如联想
            mRecorder.startRecording();
        } catch (IllegalStateException ignored) {
            isValid = false;
            return isValid;
        }
        int readSize = mRecorder.read(mBuffer, 0, mBuffer.length);
        if (AudioRecord.ERROR_INVALID_OPERATION == readSize) {
            //录音可能被禁用了，做出适当的提示
            isValid = false;
        }
        // 停止录制
        try {
            // 防止某些手机崩溃，例如联想
            if (mRecorder != null) {
                // 停止
                mRecorder.stop();
                mRecorder.release();
            }
        } catch (IllegalStateException ignored) {
        }
        return isValid;
    }

    /**
     * 手机是否开启位置服务，如果没有开启那么所有app将不能使用定位功能
     */
//    public static boolean isLocServiceEnable(Context context) {
//        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//        return gps || network;
//    }


    /**
     * 取得多項權限
     */
//    public static void getMultiPermission(Activity activity, String[] permissions, PermissionCallBack callBack) {
//        if (isSelfPermission(activity, permissions)) {
//            callBack.request(null);
//        } else {
//            ActivityCompat.requestPermissions(activity, permissions, RequestCode.MULTI_REQUEST_CODE);
//        }
//    }

}
