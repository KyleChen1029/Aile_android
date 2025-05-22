package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;

/**
 * Create by evan on 2/22/21
 *
 * @author Evan Wang
 * date 2/22/21
 */
public class UserHomePagePicsUpdateRequest extends NewRequestBase {
    private final ApiListener<String> listener;

    public UserHomePagePicsUpdateRequest(Context ctx, ApiListener<String> listener) {
        super(ctx, ApiPath.userHomePagePicsUpdate);
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
