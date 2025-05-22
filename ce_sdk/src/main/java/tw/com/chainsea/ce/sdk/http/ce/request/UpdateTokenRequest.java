package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.android.common.log.CELog;

/**
 * UpdateTokenRequest to update token
 * Created by sihuan on 2016/6/22.
 */
public class UpdateTokenRequest extends NewRequestBase {
    private ApiListener<String> listener;

    public UpdateTokenRequest(Context ctx, ApiListener<String> listener) {
        super(ctx, ApiPath.deviceTokenUpdate);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        if (this.listener != null) {
            this.listener.onSuccess(mJSONObject.getString("appToken"));
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        CELog.e("update token failed: " + code.getValue());
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }
}
