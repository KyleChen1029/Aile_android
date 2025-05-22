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
 * ContactDeleteRequest to delete a contact
 * Created by Fleming on 2016/6/13.
 */
public class RoomComplaintRequest extends NewRequestBase {
    private ApiListener<String> listener;

    public RoomComplaintRequest(Context ctx, ApiListener<String> listener) {
        super(ctx, ApiPath.chatRoomComplaint);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        if (this.listener != null) {
            this.listener.onSuccess(s);
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        CELog.e("contact delete failed: " + code.getValue());
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }
}
