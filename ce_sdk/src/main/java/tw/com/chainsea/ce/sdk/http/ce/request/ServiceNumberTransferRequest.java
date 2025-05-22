package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;

/**
 * current by evan on 12/22/20
 *
 * @author Evan Wang
 * @date 12/22/20
 */
public class ServiceNumberTransferRequest extends NewRequestBase {
    private ApiListener<String> listener;

    public ServiceNumberTransferRequest(Context ctx, ApiListener<String> listener) {
        super(ctx, ApiPath.serviceNumberTransfer);
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
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }

}
