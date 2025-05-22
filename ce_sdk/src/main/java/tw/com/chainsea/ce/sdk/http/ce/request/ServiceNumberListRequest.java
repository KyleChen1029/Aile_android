package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.bean.ServiceNum;
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.lib.ErrCode;

/**
 * current by evan on 2020-08-21
 *
 * @author Evan Wang
 * date 2020-08-21
 */
public class ServiceNumberListRequest extends NewRequestBase {
    private final ApiListener<List<ServiceNumberEntity>> listener;

    public ServiceNumberListRequest(Context context, ApiListener<List<ServiceNumberEntity>> listener) {
        super(context, ApiPath.serviceNumberList);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        if (JsonHelper.getInstance().has(jsonObject, "items")) {
            String json = jsonObject.getJSONArray("items").toString();
            List<ServiceNumberEntity> list = JsonHelper.getInstance().fromToList(json, ServiceNumberEntity[].class);
//            ServiceNumberReference.save(null, list);

            if (this.listener != null) {
                this.listener.onSuccess(list);
            }

            for (ServiceNumberEntity entity : list) {
                DBManager.getInstance().insertServiceNum(ServiceNum.of(entity));
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
