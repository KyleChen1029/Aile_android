package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.bean.msg.BroadcastFlag;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;

/**
 * current by evan on 2020-08-18
 *
 * @author Evan Wang
 * @date 2020-08-18
 */
public class ServiceNumberBroadcastUpdateRequest extends NewRequestBase {
    private Listener listener;

    public ServiceNumberBroadcastUpdateRequest(Context ctx, Listener listener) {
        super(ctx, ApiPath.serviceNumberBroadcastUpdate);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        if (this.listener != null) {
            if (JsonHelper.getInstance().has(jsonObject, "roomId", "messageId", "updateUserId", "createUserId")) {
                int flag = -2;
                long broadcastTime = System.currentTimeMillis();
//                int flag = jsonObject.getInt("flag");
//                long broadcastTime = jsonObject.getLong("broadcastTime");

                String broadcastRoomId = jsonObject.getString("roomId");
                String messageId = jsonObject.getString("messageId");
                String updateUserId = jsonObject.getString("updateUserId");
                String createUserId = jsonObject.getString("createUserId");
                this.listener.onSuccess(broadcastRoomId, messageId, createUserId, updateUserId, BroadcastFlag.of(flag), broadcastTime);
            } else {
                this.listener.onFailed("缺少 resp ");
            }
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }

    public interface Listener {
        void onSuccess(String broadcastRoomId, String messageId, String createUserId, String updateUserId, BroadcastFlag flag, long broadcastTime);

        void onFailed(String errorMessage);
    }
}
