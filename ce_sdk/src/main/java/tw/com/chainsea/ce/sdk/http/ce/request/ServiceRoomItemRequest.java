package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.lib.ErrCode;

public class ServiceRoomItemRequest extends NewRequestBase {
    private final ApiListener<ChatRoomEntity> listener;

    public ServiceRoomItemRequest(Context ctx, ApiListener<ChatRoomEntity> listener) {
        super(ctx, "/" + ApiPath.serviceRoomItem);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        ChatRoomEntity item = new ChatRoomEntity();
        item.setId(jsonObject.getString("id")); //目前只有aiff內用到而且只要其id
        if (this.listener != null) {
            this.listener.onSuccess(item);
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        CELog.e("request session list failed: " + code.getValue());
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }

}
