package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.lib.ErrCode;

/**
 * LabelMemberAddRequest to set user's alias
 * Created by Fleming on 2016/8/3.
 */
public class LabelMemberDeleteRequest extends NewRequestBase {
    private ApiListener<String> listener;

    public LabelMemberDeleteRequest(Context ctx, ApiListener<String> listener) {
        super(ctx, ApiPath.labelMemberDelete);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        if (this.listener != null) {
            this.listener.onSuccess("");
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        CELog.e("set contact alias failed: " + code.getValue());
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }

}
