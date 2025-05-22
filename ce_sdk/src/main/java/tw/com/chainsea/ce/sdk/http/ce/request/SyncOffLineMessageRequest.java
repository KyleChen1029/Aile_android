package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.lib.ErrCode;

public class SyncOffLineMessageRequest extends NewRequestBase {
    private ApiListener<JSONObject> listener;

    public SyncOffLineMessageRequest(Context ctx, ApiListener<JSONObject> listener) {
        super(ctx, ApiPath.offLineMessageList);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        if (this.listener != null) {
            this.listener.onSuccess(jsonObject);
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        CELog.e("sync off line message failed: " + code.getValue());
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }

}
