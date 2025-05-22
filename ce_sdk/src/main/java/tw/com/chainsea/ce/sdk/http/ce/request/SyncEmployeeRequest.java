package tw.com.chainsea.ce.sdk.http.ce.request;

import static tw.com.chainsea.ce.sdk.database.DBContract.REFRESH_TIME_SOURCE.SYNC_EMPLOYEE;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.http.ce.response.SyncEmployeeRsp;
import tw.com.chainsea.ce.sdk.lib.ErrCode;

public class SyncEmployeeRequest extends NewRequestBase {
    public static final int PAGE_SIZE = 100;
    private final ApiListener<List<UserProfileEntity>> listener;
    private final Context context;

    public SyncEmployeeRequest(Context ctx, ApiListener<List<UserProfileEntity>> listener) {
        super(ctx, "/" + ApiPath.syncEmployee);
        this.listener = listener;
        this.context = ctx;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        SyncEmployeeRsp rsp = JsonHelper.getInstance().from(s, SyncEmployeeRsp.class);
        if(rsp.getItems() != null){
            for(UserProfileEntity item : rsp.getItems()){
                DBManager.getInstance().insertFriends(item);
            }
            DBManager.getInstance().updateOrInsertApiInfoField(SYNC_EMPLOYEE, rsp.getRefreshTime());
            if(rsp.isHasNextPage()){
                new SyncEmployeeRequest(context, listener)
                        .setMainThreadEnable(false)
                        .request(new JSONObject()
                                .put("pageSize", PAGE_SIZE)
                                .put("refreshTime", rsp.getRefreshTime()));
            }else{
                List<UserProfileEntity> profiles = DBManager.getInstance().queryEmployeeList();
                if(listener != null) listener.onSuccess(profiles);
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
