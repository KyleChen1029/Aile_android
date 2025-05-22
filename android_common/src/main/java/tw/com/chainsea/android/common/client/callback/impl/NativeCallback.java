package tw.com.chainsea.android.common.client.callback.impl;

import java.io.IOException;

import okhttp3.ResponseBody;
import tw.com.chainsea.android.common.client.exception.ClientRequestException;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;

/**
 * NativeCallback Http Call back after the request is completed
 *
 * @author Evan Wang
 * @version 0.0.1
 * @since 0.0.1
 */
public abstract class NativeCallback extends ACallBack {

    public NativeCallback(boolean mainThreadEnable) {
        super(mainThreadEnable);
    }

    /**
     * Custom response success function
     *
     * @param resp
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    abstract public void onSuccess(String resp);

    /**
     * Custom response failure function
     *
     * @param e
     * @param errorMsg
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    abstract public void onFailure(Exception e, String errorMsg);

    @Override
    public void onSuccess(ResponseBody body, boolean mainThreadEnable) {
        try {
            String respData = body.string();
            if (respData == null) {
                onFailure(new ClientRequestException("response data == null"), "response data == null");
                return;
            }
            if (mainThreadEnable) {
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> onSuccess(respData));
            } else {
                onSuccess(respData);
            }
        } catch (IOException e) {
            onFailure(e, e.getMessage());
        }
    }
}
