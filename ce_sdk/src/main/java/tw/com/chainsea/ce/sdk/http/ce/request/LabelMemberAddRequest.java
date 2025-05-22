package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import com.google.common.collect.ImmutableMap;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.lib.ErrCode;

/**
 * LabelMemberAddRequest to set user's alias
 * Created by Fleming on 2016/8/3.
 */
public class LabelMemberAddRequest extends NewRequestBase {
    private ApiListener<Map<String, String>> listener;

    public LabelMemberAddRequest(Context ctx, ApiListener<Map<String, String>> listener) {
        super(ctx, ApiPath.labelMemberAdd);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        if (this.listener != null) {
            if (jsonObject.has("labelId")) {
                String labelId = jsonObject.getString("labelId");
                String userId = this.mJSONObject.getString("userId");
                this.listener.onSuccess(ImmutableMap.of("labelId", labelId, "userId", userId));
            }
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        CELog.e("set contact alias failed: " + code.getValue());
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }

    public interface Listener {
        void onSuccess(String labelId, String userId);

        void onFailed(String errorMessage);
    }
}
