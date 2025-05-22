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
 * RoomRecentDeleteRequest to delete session from server
 * Created by Fleming on 2016/7/1.
 */
public class RoomRecentDeleteRequest extends NewRequestBase {
    private ApiListener<String> listener;

    public RoomRecentDeleteRequest(Context ctx, ApiListener<String> listener) {
        super(ctx, "/" + ApiPath.chatRoomRecentDelete);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        if (this.listener != null) {
            this.listener.onSuccess(mJSONObject.getString("roomId"));
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        CELog.e("session delete failed: " + code.getValue());
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }

    public interface Listener {
        void onSuccess();

        void onFailed(String errorMessage);
    }

}
