package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.bean.ServiceNum;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.http.ce.response.SyncSubscribeServicenumberRsp;
import tw.com.chainsea.ce.sdk.lib.ErrCode;

public class SyncSubscribeServicenumberRequest extends NewRequestBase {
    private final ApiListener<List<ServiceNum>> listener;

    public SyncSubscribeServicenumberRequest(Context ctx, ApiListener<List<ServiceNum>> listener) {
        super(ctx, "/" + ApiPath.syncSubscribeServicenumber);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        SyncSubscribeServicenumberRsp rsp = JsonHelper.getInstance().from(s, SyncSubscribeServicenumberRsp.class);
        if(rsp.getItems() != null && rsp.getCount() > 0){
            if (this.listener != null) {
                this.listener.onSuccess(rsp.getItems());
            }
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        CELog.e("request session list failed: " + code.getValue());
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }

}
