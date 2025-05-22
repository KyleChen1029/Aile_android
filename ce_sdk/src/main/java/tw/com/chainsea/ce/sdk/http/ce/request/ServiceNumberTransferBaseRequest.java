package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;

/**
 * current by evan on 12/23/20
 *
 * @author Evan Wang
 * @date 12/23/20
 */
public class ServiceNumberTransferBaseRequest extends NewRequestBase {
    private ApiListener<String> listener;

    public ServiceNumberTransferBaseRequest(Context ctx, String path , ApiListener<String> listener) {
        super(ctx, ApiPath.serviceNumberTransfer + path);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {

    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {

    }
}
