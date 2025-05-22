package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.android.common.log.CELog;

/**
 * MemberAddRequest to add a member into the group
 * Created by Fleming on 2016/8/8.
 */
public class MemberAddRequest extends NewRequestBase {
    private ApiListener<String> listener;

    public MemberAddRequest(Context ctx, ApiListener<String> listener) {
        super(ctx, ApiPath.chatMemberAdd);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        CELog.d("groupMmember add success: " + jsonObject.toString());
        if (this.listener != null) {
            this.listener.onSuccess("");
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        CELog.e("group member add failed: " + code.getValue());
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }

}
