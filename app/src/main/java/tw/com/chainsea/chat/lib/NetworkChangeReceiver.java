package tw.com.chainsea.chat.lib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.widget.Toast;

import com.google.common.base.Strings;

import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.event.EventBusUtils;
import tw.com.chainsea.ce.sdk.event.EventMsg;
import tw.com.chainsea.ce.sdk.event.MsgConstant;

/**
 * NetworkChangeReceiver
 * Created by Fleming on 2016/9/19.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    public static NetworkChangeReceiver newInstance() {
        return new NetworkChangeReceiver();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            int status = NetworkUtils.getNetworkStatus(context);
            String netStatus = NetworkUtils.getNetworkStatusString(context);
            // Forward network status broadcast
            boolean state = true;
            if (status == NetworkUtils.NO_CONNECTION_STATUS) {
                Toast.makeText(context, netStatus, Toast.LENGTH_SHORT).show();
                // Actively hang up the phone when disconnected
                state = false;
            } else {
                // Refresh the chat room,
                String userId = TokenPref.getInstance(context)
                        .getUserId();
                if (!Strings.isNullOrEmpty(userId)) {
                    state = true;
                }
            }

            EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.INTERNET_STSTE_FILTER, state));
        }
    }


    private NetworkConnectChangedListener mNetworkConnectChangedListener;

    public void setNetworkConnectChangedListener(NetworkConnectChangedListener networkConnectChangedListener) {
        mNetworkConnectChangedListener = networkConnectChangedListener;
    }

    public interface NetworkConnectChangedListener {
        void OnInternetListener(boolean internet);
    }

}
