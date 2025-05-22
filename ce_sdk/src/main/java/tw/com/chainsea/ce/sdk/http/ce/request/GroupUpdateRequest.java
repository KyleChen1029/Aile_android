package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.android.common.log.CELog;

/**
 * GroupUpdateRequest to update group's name or owner
 * Created by Fleming on 2016/8/8.
 */
public class GroupUpdateRequest extends NewRequestBase {
    private ApiListener<String> listener;

    public GroupUpdateRequest(Context ctx, ApiListener<String> listener) {
        super(ctx, "/" + ApiPath.chatRoomUpdate);
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
        CELog.e("group member update failed: " + code.getValue());
        Log.d("UpgradeToGroupActivity", code.getValue());
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }
}
