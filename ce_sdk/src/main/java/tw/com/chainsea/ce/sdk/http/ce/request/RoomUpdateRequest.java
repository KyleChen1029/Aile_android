package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.android.common.log.CELog;

/**
 * current by evan on 2020-02-05
 */
public class RoomUpdateRequest extends NewRequestBase {
    private ApiListener<String> listener;

    public RoomUpdateRequest(Context ctx, ApiListener<String> listener) {
        super(ctx, "/" + ApiPath.chatRoomUpdate);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        if (this.listener != null) {
            this.listener.onSuccess(jsonObject.has("avatarId") ? jsonObject.getString("avatarId") : "");
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        CELog.e(errorMessage);
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }
}
