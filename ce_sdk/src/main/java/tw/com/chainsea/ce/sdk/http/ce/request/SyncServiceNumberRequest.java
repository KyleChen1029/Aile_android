package tw.com.chainsea.ce.sdk.http.ce.request;

import static tw.com.chainsea.ce.sdk.database.DBContract.REFRESH_TIME_SOURCE.SYNC_SERVICENUMBER;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.bean.ServiceNum;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.http.ce.response.SyncServicenumberRsp;
import tw.com.chainsea.ce.sdk.lib.ErrCode;

public class SyncServiceNumberRequest extends NewRequestBase {
    public static final int PAGE_SIZE = 100;
    private final Listener listener;
    private final Context context;

    public SyncServiceNumberRequest(Context ctx, Listener listener) {
        super(ctx, "/" + ApiPath.syncServiceNumber);
        this.context = ctx;
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        SyncServicenumberRsp rsp = JsonHelper.getInstance().from(s, SyncServicenumberRsp.class);
        if(rsp.getItems() != null && !rsp.getItems().isEmpty()){
            for (ServiceNum serviceNum : rsp.getItems()) {
                DBManager.getInstance().insertServiceNum(serviceNum);
            }
            DBManager.getInstance().updateOrInsertApiInfoField(SYNC_SERVICENUMBER, rsp.getRefreshTime());
            if(rsp.isHasNextPage()){
                new SyncServiceNumberRequest(context, listener)
                        .setMainThreadEnable(false)
                        .request(new JSONObject()
                                .put("pageSize", PAGE_SIZE)
                                .put("refreshTime", rsp.getRefreshTime()));
            }else{
                if(listener != null) {
                    listener.onSuccess();
                    listener.onCheckJoinTime(rsp.getItems());
                }

            }
        }else {
            if(listener != null) listener.onSuccess();
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }

    public interface Listener {
        void onSuccess();
        void onFailed(String errorMessage);

        void onCheckJoinTime(List<ServiceNum> serviceNum);
    }
}
