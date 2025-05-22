package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import com.google.common.collect.ImmutableMap;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;

public class FromSwitchRequest extends NewRequestBase {
    private ApiListener<Map<String, String>> listener;
    private String oldFrom;
    private String newFrom;

    public FromSwitchRequest(Context ctx, String oldFrom, String newFrom, ApiListener<Map<String, String>> listener) {
        super(ctx, ApiPath.fromSwitch);
        this.listener = listener;
        this.oldFrom = oldFrom;
        this.newFrom = newFrom;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        if (this.listener != null) {
            this.listener.onSuccess(ImmutableMap.of("oldFrom", oldFrom, "newFrom", newFrom));
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        if (this.listener != null) {
            listener.onFailed(errorMessage);
        }
    }

}
