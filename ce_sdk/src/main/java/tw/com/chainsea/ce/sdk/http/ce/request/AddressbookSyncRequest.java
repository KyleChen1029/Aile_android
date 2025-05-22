package tw.com.chainsea.ce.sdk.http.ce.request;

import static tw.com.chainsea.ce.sdk.database.DBContract.REFRESH_TIME_SOURCE.ADDRESS_BOOK_SYNC;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.http.ce.model.Item;
import tw.com.chainsea.ce.sdk.http.ce.response.UserEmployeeListRsp;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;

public class AddressbookSyncRequest extends NewRequestBase {
    private final ApiListener<List<UserProfileEntity>> listener;

    public AddressbookSyncRequest(Context ctx, ApiListener<List<UserProfileEntity>> listener) {
        super(ctx, "/" + ApiPath.addressBookSync);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        UserEmployeeListRsp rsp = JsonHelper.getInstance().from(s, UserEmployeeListRsp.class);
        if(rsp != null) {
            for(Item item : rsp.getItems()){
                DBManager.getInstance().insertFriends(new UserProfileEntity(item));
            }
            DBManager.getInstance().updateOrInsertApiInfoField(ADDRESS_BOOK_SYNC, rsp.getRefreshTime());
            List<UserProfileEntity> profiles = DBManager.getInstance().queryEmployeeList();
            if (listener != null) listener.onSuccess(profiles);
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }
}
