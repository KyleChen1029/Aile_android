package tw.com.chainsea.chat.lib;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.Optional;

import tw.com.chainsea.chat.R;

/**
 * NetworkUtils
 * Created by Fleming on 2016/12/27.
 */
public class NetworkUtils {

    public static final int MOBILE_STATUS = 0;
    public static final int WIFI_STATUS = 1;
    public static final int NO_CONNECTION_STATUS = 2;

    public static boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = manager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isAvailable();
        } catch (Exception e) {
            return false;
        }

    }

    public static int getNetworkStatus(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = manager.getActiveNetworkInfo();
        if (isNetworkAvailable(context)) {
            return Optional.ofNullable(activeNetworkInfo)
                .map(networkInfo -> {
                    if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                        return MOBILE_STATUS;
                    } else if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        return WIFI_STATUS;
                    }
                    return NO_CONNECTION_STATUS;
                })
                .orElse(NO_CONNECTION_STATUS); // 如果 `activeNetworkInfo` 為空，返回 NO_CONNECTION_STATUS
        }
        return NO_CONNECTION_STATUS;
    }

    public static String getNetworkStatusString(Context context) {
        int status = getNetworkStatus(context);
        String statusName = null;
        switch (status) {
            case NetworkUtils.MOBILE_STATUS:
                statusName = context.getResources().getString(R.string.mobile_connection);
                break;
            case NetworkUtils.WIFI_STATUS:
                statusName = context.getResources().getString(R.string.wifi_connection);
                break;
            case NetworkUtils.NO_CONNECTION_STATUS:
                statusName = context.getResources().getString(R.string.no_connection);
                break;
        }
        return statusName;
    }


    public static final int TYPE_WIFI = 1;
    public static final int TYPE_MOBILE = 2;
    public static final int TYPE_NOT_CONNECTED = 0;
    public static final int NETWORK_STATUS_NOT_CONNECTED = 0;
    public static final int NETWORK_STATUS_WIFI = 1;
    public static final int NETWORK_STATUS_MOBILE = 2;

    public static int getConnectivityStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                return TYPE_WIFI;

            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return TYPE_MOBILE;
        }
        return TYPE_NOT_CONNECTED;
    }

    public static int getConnectivityStatusString(Context context) {
        int conn = NetworkUtils.getConnectivityStatus(context);
        int status = 0;
        if (conn == NetworkUtils.TYPE_WIFI) {
            status = NETWORK_STATUS_WIFI;
        } else if (conn == NetworkUtils.TYPE_MOBILE) {
            status = NETWORK_STATUS_MOBILE;
        } else if (conn == NetworkUtils.TYPE_NOT_CONNECTED) {
            status = NETWORK_STATUS_NOT_CONNECTED;
        }
        return status;
    }
}
