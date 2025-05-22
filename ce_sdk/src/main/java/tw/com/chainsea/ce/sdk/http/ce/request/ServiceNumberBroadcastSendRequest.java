package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.bean.msg.BroadcastFlag;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.android.common.log.CELog;

/**
 * current by evan on 2020-08-18
 *
 * @author Evan Wang
 * date 2020-08-18
 */
public class ServiceNumberBroadcastSendRequest extends NewRequestBase {
    private final Listener listener;

    public ServiceNumberBroadcastSendRequest(Context ctx, Listener listener) {
        super(ctx, ApiPath.serviceNumberBroadcastSend);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        if (this.listener != null) {
            if (JsonHelper.getInstance().has(jsonObject, "flag", "roomId", "messageId", "updateUserId", "createUserId", "broadcastTime")) {
                int flag = jsonObject.getInt("flag");
                String broadcastRoomId = jsonObject.getString("roomId");
                String messageId = jsonObject.getString("messageId");
                String updateUserId = jsonObject.getString("updateUserId");
                String createUserId = jsonObject.getString("createUserId");
                long broadcastTime = jsonObject.getLong("broadcastTime");
                this.listener.onSuccess(broadcastRoomId, messageId, updateUserId, createUserId, BroadcastFlag.of(flag), broadcastTime);
            } else {
                this.listener.onFailed("缺少 resp ");
            }
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        CELog.e(errorMessage);
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }

    public interface Listener {
        void onSuccess(String broadcastRoomId, String messageId, String updateUserId, String createUserId, BroadcastFlag flag, long broadcastTime);

        void onFailed(String errorMessage);
    }
}