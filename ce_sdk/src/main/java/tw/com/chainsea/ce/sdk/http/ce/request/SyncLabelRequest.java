package tw.com.chainsea.ce.sdk.http.ce.request;

import static tw.com.chainsea.ce.sdk.database.DBContract.REFRESH_TIME_SOURCE.SYNC_LABEL;

import android.content.Context;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.bean.label.Label;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.http.ce.response.LabelSyncResponse;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.service.listener.ApiCallback;

public class SyncLabelRequest extends NewRequestBase {
    private final ApiCallback<List<Label>> listener;
    public static final int PAGE_SIZE = 100;

    public SyncLabelRequest(Context ctx, @Nullable ApiCallback<List<Label>> listener) {
        super(ctx, "/" + ApiPath.syncLabel);
        this.listener = listener;
    }

    @Override
    protected void success(final JSONObject jsonObject, String s) throws JSONException {
        LabelSyncResponse rsp;
        try {
            rsp = JsonHelper.getInstance().from(s, LabelSyncResponse.class);
        }catch (Exception exception){
            if(listener != null) listener.error("data error : " + s);
            return;
        }
        if(rsp != null && rsp.getItems() != null){
            List<Label> items = new ArrayList<>();
            if(rsp.getItems() != null) {
                items.addAll(rsp.getItems());
            }
            if(listener != null) listener.complete(items);
            if(rsp.isHasNextPage()){
                new SyncLabelRequest(ctx, listener)
                        .setMainThreadEnable(false)
                        .request(new JSONObject()
                                .put("pageSize", PAGE_SIZE)
                                .put("refreshTime", rsp.getRefreshTime()));
            }else{
                DBManager.getInstance().updateOrInsertApiInfoField(SYNC_LABEL, rsp.getRefreshTime());
                if(listener != null) listener.finish();
            }
        }else{
            if(listener != null) listener.error("no data");
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        CELog.e(errorMessage);
        if (this.listener != null) {
            this.listener.error(errorMessage);
        }
    }
}
