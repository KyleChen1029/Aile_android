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
 * RoomCreateRequest to create a group
 * Created by Fleming on 2016/8/8.
 */
public class LabelCreateRequest extends NewRequestBase {
    private ApiListener<String> listener;

    public LabelCreateRequest(Context ctx, ApiListener<String> listener) {
        super(ctx, ApiPath.labelCreate);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        String labelId = jsonObject.getString("labelId");
        if (this.listener != null) {
            this.listener.onSuccess(labelId);
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        CELog.e("group create failed:" + code.getValue());
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }

    public interface Listener {
        void onSuccess(String labelId);

        void onFailed(String errorMessage);
    }
}
