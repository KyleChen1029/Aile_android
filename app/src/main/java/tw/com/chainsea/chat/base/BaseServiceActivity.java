package tw.com.chainsea.chat.base;

import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.socket.ce.SocketManager;
import tw.com.chainsea.ce.sdk.socket.cp.CpSocket;
import tw.com.chainsea.chat.App;
import tw.com.chainsea.chat.lib.NetworkUtils;
import tw.com.chainsea.chat.view.BaseActivity;

/**
 * current by evan on 2020-06-22
 *
 * @author Evan Wang
 * date 2020-06-22
 */
public class BaseServiceActivity extends BaseActivity {
    @Override
    protected void onRestart() {
        super.onRestart();
        if (App.APP_STATUS == App.APP_STATUS_NORMAL) { // Abnormal startup process, directly reinitialize the application interface
            SocketManager.reconnect();
            CpSocket.getInstance().connect(
                TokenPref.getInstance(getApplicationContext()).getCpSocketUrl(),
                TokenPref.getInstance(getApplicationContext()).getCpSocketNameSpace(),
                TokenPref.getInstance(getApplicationContext()).getCpSocketName(),
                TokenPref.getInstance(getApplicationContext()).getCpSocketDeviceId()
            );
        }
        if (NetworkUtils.isNetworkAvailable(BaseServiceActivity.this)) {
            homeViewModel.backgroundSync();
        }
    }
}
