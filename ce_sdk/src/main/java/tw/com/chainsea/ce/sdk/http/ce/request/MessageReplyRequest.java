package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;

/**
 * MessageRequest to refresh messages
 * Created by Fleming on 2016/6/13.
 */
public class MessageReplyRequest extends NewRequestBase {
    private Listener listener;

    public MessageReplyRequest(Context ctx, Listener listener) {
        super(ctx, ApiPath.messageReply);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        int sendNum = jsonObject.getInt("sendNum");
        long sendTime = jsonObject.getLong("sendTime");
        if (this.listener != null) {
            String messageId = mJSONObject.getString("messageId");
            String roomId = mJSONObject.getString("roomId");

            this.listener.onSuccess(messageId, sendNum, sendTime, roomId);
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        if (this.listener != null) {
            try {
                String messageId = mJSONObject.getString("messageId");
                String roomId = mJSONObject.getString("roomId");
                this.listener.onFailed(roomId, messageId, errorMessage);
            } catch (Exception e) {

            }
        }
    }

    public interface Listener {
        void onSuccess(String id, int sendNum, long sendTime, String roomId);

        void onFailed(String roomId, String msgId, String errorMessage);
    }

}
