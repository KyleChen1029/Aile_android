package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.lib.ErrCode;

/**
 * ContactDeleteRequest to delete a contact
 * Created by Fleming on 2016/6/13.
 */
public class RoomHomePagePicsUpdateRequest extends NewRequestBase {
    private ApiListener<String> listener;

    public RoomHomePagePicsUpdateRequest(Context ctx, ApiListener<String> listener) {
        super(ctx, ApiPath.chatRoomHomepagepicsUpdate);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        Log.d("QuitGroupRequest", jsonObject.toString());
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
