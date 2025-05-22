package tw.com.chainsea.ce.sdk.http.ce.request;

import static tw.com.chainsea.ce.sdk.database.DBContract.REFRESH_TIME_SOURCE.SYNC_CONTACT;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.bean.CustomerEntity;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.http.ce.response.SyncContactRsp;
import tw.com.chainsea.ce.sdk.lib.ErrCode;

public class SyncContactRequest extends NewRequestBase {
    public static final int PAGE_SIZE = 100;
    private final ApiListener<List<CustomerEntity>> listener;
    private final Context context;

    public SyncContactRequest(Context ctx, ApiListener<List<CustomerEntity>> listener) {
        super(ctx, "/" + ApiPath.syncContact);
        this.context = ctx;
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        SyncContactRsp rsp = JsonHelper.getInstance().from(s, SyncContactRsp.class);
        if(rsp.getItems() != null && !rsp.getItems().isEmpty()){
            for(CustomerEntity item : rsp.getItems()){
                DBManager.getInstance().insertCustomer(item);
            }
            DBManager.getInstance().updateOrInsertApiInfoField(SYNC_CONTACT, rsp.getRefreshTime());
            if(rsp.isHasNextPage()){
                new SyncContactRequest(context, listener)
                        .setMainThreadEnable(false)
                        .request(new JSONObject()
                                .put("pageSize", PAGE_SIZE)
                                .put("refreshTime", rsp.getRefreshTime()));
            }else{
                List<CustomerEntity> list = DBManager.getInstance().queryCustomers();
                if (this.listener != null) {
                    this.listener.onSuccess(list);
                }
            }
        }else {
            List<CustomerEntity> list = DBManager.getInstance().queryCustomers();
            if(listener != null) listener.onSuccess(list);
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
    }
}
