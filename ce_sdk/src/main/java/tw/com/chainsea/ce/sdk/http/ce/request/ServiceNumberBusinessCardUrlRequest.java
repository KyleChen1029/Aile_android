package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.http.ce.response.BusinessCardUrlResponse;
import tw.com.chainsea.ce.sdk.lib.ErrCode;

/**
 * Create by Evan.W on 2021/01/21.
 *
 * @author Evan Wang
 * @date 1/21/21
 */
public class ServiceNumberBusinessCardUrlRequest extends NewRequestBase {

    private ApiListener<String> listener;
    public ServiceNumberBusinessCardUrlRequest(Context ctx, ApiListener<String> listener) {
        super(ctx, "/" +ApiPath.serviceNumberBusinessCardUrl);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        try {
            BusinessCardUrlResponse resp = JsonHelper.getInstance().from(s, BusinessCardUrlResponse.class);
            if (this.listener != null) {
                if (resp != null) {
                    String businessCard = resp.getBusinessCardUrl();
                    this.listener.onSuccess(businessCard);
                }
            }
        } catch (Exception e) {
            failed(ErrCode.JSON_PARSE_FAILED, e.getMessage());
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }
}
