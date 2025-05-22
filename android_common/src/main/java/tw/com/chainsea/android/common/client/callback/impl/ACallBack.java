package tw.com.chainsea.android.common.client.callback.impl;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;
import okhttp3.ResponseBody;
import tw.com.chainsea.android.common.client.callback.intf.ICallBack;
import tw.com.chainsea.android.common.client.exception.ClientRequestException;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;

public abstract class ACallBack implements ICallBack {

    // enable main Thread
    protected boolean mainThreadEnable = false;

    public ACallBack() {
    }

    public ACallBack(boolean mainThreadEnable) {
        this.mainThreadEnable = mainThreadEnable;
    }

    /**
     * Set whether to enable the main thread
     */
    public ACallBack setMainThreadEnable(boolean mainThreadEnable) {
        this.mainThreadEnable = mainThreadEnable;
        return this;
    }

    /**
     * Custom response success function
     *
     * @param body
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    abstract public void onSuccess(ResponseBody body, boolean mainThreadEnable);

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

    /**
     * <p>
     * Whether to follow the thread logic for processing failures
     * Whether to follow the main thread succession class implementation
     * </p>
     *
     * @param e
     * @param errorMsg
     * @param mainThreadEnable
     */
    private void onFailure(Exception e, String errorMsg, boolean mainThreadEnable) {
        if (mainThreadEnable) {
            ThreadExecutorHelper.getMainThreadExecutor().execute(() -> onFailure(e, errorMsg));
        } else {
            onFailure(e, errorMsg);
        }
    }

    @Override
    public void onFailure(@NonNull Call call, IOException e) {
        onFailure(e, e.getMessage(), mainThreadEnable);
    }

    @Override
    public void onResponse(@NonNull Call call, @NonNull Response resp) throws IOException {
        response(resp);
    }

    @Override
    public void progress(float progress, long total) {
        onProgress(progress, total);
    }

    public void onProgress(float progress, long total) {

    }

    /**
     * Http Request complete response callback method
     *
     * @param resp
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    @Override
    public void response(Response resp) {
        try {
            if (resp.isSuccessful()) {
                ResponseBody body = resp.body();
                if (body == null) {
                    onFailure(new ClientRequestException("response data == null"), "response data == null", this.mainThreadEnable);
                    return;
                }
                onSuccess(body, mainThreadEnable);
            } else {
                Log.e("Response Failure", "Response Failure::: " + resp);
                onFailure(new ClientRequestException("response is failure"), "response is failure", this.mainThreadEnable);
            }
        } catch (Exception e) {
            onFailure(e, e.getMessage(), this.mainThreadEnable);
        }
    }
}
