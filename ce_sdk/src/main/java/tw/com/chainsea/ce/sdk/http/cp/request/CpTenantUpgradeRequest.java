package tw.com.chainsea.ce.sdk.http.cp.request;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import tw.com.chainsea.ce.sdk.http.cp.CpApiPath;
import tw.com.chainsea.ce.sdk.http.cp.base.CpApiListener;
import tw.com.chainsea.ce.sdk.http.cp.base.CpNewRequestBase;
import tw.com.chainsea.ce.sdk.lib.ErrCode;

public class CpTenantUpgradeRequest extends CpNewRequestBase {
    private final CpApiListener<String> listener;

    public CpTenantUpgradeRequest(Context ctx, CpApiListener<String> listener) {
        super(ctx, CpApiPath.TENANT_UPGRADE);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        if (this.listener != null) {
            this.listener.onSuccess(s);
        }
    }

    @Override
    protected void failed(ErrCode errCode, String errorMessage) {
        if (this.listener != null) {
            this.listener.onFailed(errCode.getValue(), errorMessage);
        }
    }
}
