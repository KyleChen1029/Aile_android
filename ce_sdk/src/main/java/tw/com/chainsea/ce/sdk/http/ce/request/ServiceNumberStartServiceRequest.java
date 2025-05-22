package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;

/**
 * current by evan on 2019-11-14
 * 專人開始服務
 * {
 * "roomId": "",
 * }   
 */
public class ServiceNumberStartServiceRequest extends NewRequestBase {
    private ApiListener<Boolean> listener;


    public ServiceNumberStartServiceRequest(Context ctx, ApiListener<Boolean> listener) {
        super(ctx, ApiPath.serviceNumberStartService);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        boolean status = false;
        if (jsonObject.has("result")) {
            status = jsonObject.getBoolean("result");
        }
        if (this.listener != null) {
            this.listener.onSuccess(status);
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }

}
