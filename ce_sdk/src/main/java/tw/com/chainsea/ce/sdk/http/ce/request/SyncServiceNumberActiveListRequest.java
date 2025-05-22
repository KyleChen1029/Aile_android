package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.http.ce.response.RoomRecentRsp;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.service.listener.ApiCallback;

public class SyncServiceNumberActiveListRequest extends NewRequestBase {
    private final ApiCallback<RoomRecentRsp> listener;

    public SyncServiceNumberActiveListRequest(Context ctx, @Nullable ApiCallback<RoomRecentRsp> listener) {
        super(ctx, "/" + ApiPath.syncServiceNumberActiveList);
        this.listener = listener;
    }


    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        RoomRecentRsp rsp;
        try {
            rsp = JsonHelper.getInstance().from(s, RoomRecentRsp.class);
            if(rsp != null)
                listener.complete(rsp);
            else
                listener.error("no data");
        } catch (Exception exception) {
            if (listener != null) listener.error("data error : " + s);
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        listener.error("errCode: " + code + ",msg: " + errorMessage);
    }
}


