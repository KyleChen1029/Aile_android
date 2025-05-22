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
 * SearchVipcnRequest to search the people you want to search
 * Created by Andy on 2016/6/13.
 */

public class ServiceNumberSubscribeRequest extends NewRequestBase {
    private ApiListener<String> listener;

    public ServiceNumberSubscribeRequest(Context ctx, ApiListener<String> listener) {
        super(ctx, ApiPath.serviceNumberSubscribe);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        if (this.listener != null) {
            String roomId = jsonObject.getString("roomId");
            this.listener.onSuccess(roomId);
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        CELog.e(errorMessage);
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }


}
