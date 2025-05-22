package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.android.common.log.CELog;

/**
 * RoomCreateRequest to create a group
 * Created by Fleming on 2016/8/8.
 */
public class RoomCreateRequest extends NewRequestBase {
    private Listener listener;

    public RoomCreateRequest(Context ctx, Listener listener) {
        super(ctx, "/" + ApiPath.chatRoomCreate);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        if (listener != null) {
            listener.onCreateSuccess(jsonObject.getString("id"));
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        CELog.e("group create failed:" + code.getValue());
        if (listener != null) {
            listener.onCreateFailed(errorMessage);
        }
    }

    public interface Listener {
        void onCreateSuccess(String id);

        void onCreateFailed(String errorMessage);
    }
}
