package tw.com.chainsea.ce.sdk.http.ce.request;

import static tw.com.chainsea.ce.sdk.database.DBContract.REFRESH_TIME_SOURCE.BOSSSERVICENUMBER_CONTACT_LIST;

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
import tw.com.chainsea.ce.sdk.http.ce.response.ServiceNumberContactListRsp;
import tw.com.chainsea.ce.sdk.lib.ErrCode;

public class ServiceNumberContactListRequest extends NewRequestBase {
    public static final int PAGE_SIZE = 100;
    private final ApiListener<List<CustomerEntity>> listener;
    private final Context context;
    private int pageIndex;
    private final String serviceNumberId;

    public ServiceNumberContactListRequest(Context context, int pageIndex, String serviceNumberId, ApiListener<List<CustomerEntity>> listener) {
        super(context, "/" + ApiPath.serviceNumberContactList);
        this.listener = listener;
        this.context = ctx;
        this.pageIndex = pageIndex;
        this.serviceNumberId = serviceNumberId;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {

        ServiceNumberContactListRsp rsp = JsonHelper.getInstance().from(s, ServiceNumberContactListRsp.class);
        DBManager.getInstance().updateOrInsertApiInfoField(BOSSSERVICENUMBER_CONTACT_LIST, rsp.getRefreshTime());
        if(rsp != null && rsp.getItems() != null) {
            for(CustomerEntity item : rsp.getItems()){
                DBManager.getInstance().insertCustomer(item);
            }
            if(rsp.isHasNextPage()){
//                pageIndex++;
                long lastRefreshTime = DBManager.getInstance().getLastRefreshTime(BOSSSERVICENUMBER_CONTACT_LIST);
                JSONObject requestObject = new JSONObject()
//                        .put("pageIndex", pageIndex)
//                        .put("pageSize", PAGE_SIZE)
                        .put("refreshTime", lastRefreshTime);
                if(serviceNumberId != null && !serviceNumberId.isEmpty()){
                    requestObject.put("serviceNumberId", serviceNumberId);
                }
                new ServiceNumberContactListRequest(context, pageIndex, serviceNumberId, listener)
                        .setMainThreadEnable(false)
                        .request(requestObject);
            }else {
                List<CustomerEntity> list = DBManager.getInstance().queryCustomers();
                if (this.listener != null) {
                    this.listener.onSuccess(list);
                }
            }
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }
}
