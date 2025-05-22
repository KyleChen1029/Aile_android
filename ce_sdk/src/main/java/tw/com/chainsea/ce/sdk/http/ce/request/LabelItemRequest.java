package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.bean.label.Label;
import tw.com.chainsea.ce.sdk.lib.ParseUtils;

/**
 * LabelItemRequest to create a group
 * Created by Fleming on 2016/8/8.
 */
public class LabelItemRequest extends NewRequestBase {
    private ApiListener<Label> listener;

    public LabelItemRequest(Context ctx, ApiListener<Label> listener) {
        super(ctx, "/" + ApiPath.labelItem);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        Label label = ParseUtils.parseLabel(jsonObject);
        if (this.listener != null) {
            this.listener.onSuccess(label);
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        CELog.e("group create failed:" + code.getValue());
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }

}
