package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.android.common.log.CELog;

/**
 * ContactAddRequest to add a contact
 * Created by Fleming on 2016/6/13.
 */
public class ContactAddRequest extends NewRequestBase {
    private final ApiListener<String> listener;

    public ContactAddRequest(Context ctx, ApiListener<String> listener) {
        super(ctx, "/" + ApiPath.addressBookAdd);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        if (this.listener != null) {
            if (JsonHelper.getInstance().has(jsonObject, "roomIds")) {
                JSONArray array = jsonObject.getJSONArray("roomIds");
                this.listener.onSuccess(array.getString(0));
            }
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        CELog.e("contact add failed: " + code.getValue());
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }

}
