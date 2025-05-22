package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;

/**
 * MessageSendRequest to send message to server
 * Created by 90Chris on 2016/6/20.
 */
public class MessageRetractRequest extends NewRequestBase {
    private ApiListener<String> listener;

    public MessageRetractRequest(Context ctx, ApiListener<String> listener) {
        super(ctx, ApiPath.messageRetract);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        JSONObject header = jsonObject.getJSONObject("_header_");
        if (header.getBoolean("success")) {
            if (this.listener != null) {
                String messageId = mJSONObject.getString("messageId");
                this.listener.onSuccess(messageId);
            }
        } else {
            if (this.listener != null) {
                this.listener.onFailed("撤回失敗，請重試!");
            }
        }
    }

    @Override
    protected void failed(ErrCode reason, String errorMessage) {
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }

}
