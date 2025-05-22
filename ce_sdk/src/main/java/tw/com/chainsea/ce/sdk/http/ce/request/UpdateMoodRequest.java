package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.lib.ErrCode;

public class UpdateMoodRequest extends NewRequestBase {
    private final ApiListener<String> listener;
    public UpdateMoodRequest(Context ctx, ApiListener<String> listener) {
        super(ctx, "/" + ApiPath.updateMood);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        if (listener != null) {
            listener.onSuccess(s);
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        if (listener != null) {
            listener.onFailed(errorMessage);
        }
    }
}
