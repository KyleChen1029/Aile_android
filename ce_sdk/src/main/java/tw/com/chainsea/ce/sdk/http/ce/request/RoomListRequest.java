package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.bean.CrowdEntity;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.lib.ParseUtils;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.android.common.log.CELog;

/**
 * RoomListRequest to get group list
 * Created by Fleming on 2016/8/8.
 */
public class RoomListRequest extends NewRequestBase {
    private ApiListener<List<CrowdEntity>> listener;

    public RoomListRequest(Context ctx, ApiListener<List<CrowdEntity>> listener) {
        super(ctx, "/" + ApiPath.chatRoomList);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        if (JsonHelper.getInstance().has(jsonObject, "items")) {
            JSONArray items = jsonObject.getJSONArray("items");
            for (int i = 0, length = items.length(); i < length; i++) {
                CrowdEntity crowdEntity = ParseUtils.parseGroup(items.getJSONObject(i));
                DBManager.getInstance().insertGroup(crowdEntity);
            }
        }

        if (this.listener != null) {
            List<CrowdEntity> crowdEntities = DBManager.getInstance().findAllCrowds();
            this.listener.onSuccess(crowdEntities);
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        CELog.e("request group list failed: " + code.getValue());
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }

    public interface Listener {
        void onSuccess(List<CrowdEntity> crowdEntities);

        void onFailed(String errorMessage);
    }

}
