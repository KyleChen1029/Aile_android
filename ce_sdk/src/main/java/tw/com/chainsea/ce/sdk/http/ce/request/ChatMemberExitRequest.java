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
public class ChatMemberExitRequest extends NewRequestBase {
    private ApiListener<String> listener;

    public ChatMemberExitRequest(Context ctx, ApiListener<String> listener) {
        super(ctx, "/" + ApiPath.chatMemberExit);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        if (this.listener != null) {
            this.listener.onSuccess("");
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        CELog.e("dismiss group failed: " + code.getValue());
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }
}
