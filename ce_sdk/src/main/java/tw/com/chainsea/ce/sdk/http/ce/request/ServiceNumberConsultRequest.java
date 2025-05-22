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
 * Create by Evan.W on 2021/01/21.
 *
 * @author Evan Wang
 * @date 1/21/21
 */
public class ServiceNumberConsultRequest extends NewRequestBase {

    private ApiListener<String> listener;
    public ServiceNumberConsultRequest(Context ctx, ApiListener<String> listener) {
        super(ctx, "/" + ApiPath.serviceNumberConsult);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        CELog.d("");
        if (this.listener != null) {
            this.listener.onSuccess("");
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        CELog.d("");
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }
}
