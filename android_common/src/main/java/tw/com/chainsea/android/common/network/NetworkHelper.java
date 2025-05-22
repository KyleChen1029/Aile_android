package tw.com.chainsea.android.common.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * NetworkHelper Network tools
 *
 * @author Evan Wang
 * @version 0.0.1
 * @since 0.0.1
 */
public class NetworkHelper {

    /**
     * Determine whether the network
     *
     * @param ctx
     * @return boolean
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public static boolean hasNetWork(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            int networkType = activeNetwork.getType();
            return networkType == ConnectivityManager.TYPE_WIFI || networkType == ConnectivityManager.TYPE_MOBILE;
        } else {
            return false;
        }
    }


}
