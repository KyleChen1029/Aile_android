package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import com.google.common.collect.Lists;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.bean.CrowdEntity;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.lib.ParseUtils;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;

/**
 * ContactDeleteRequest to delete a contact
 * Created by Fleming on 2016/6/13.
 */
public class RoomHomePageRequest extends NewRequestBase {
    ApiListener<CrowdEntity> apiListener;

    public RoomHomePageRequest(Context ctx, ApiListener<CrowdEntity> apiListener) {
        super(ctx, "/" + ApiPath.chatRoomHomepage);
        this.apiListener = apiListener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        CrowdEntity crowdEntity = ParseUtils.parseGroup(jsonObject);
        JSONArray jsonArray = jsonObject.getJSONArray("members");
        List<UserProfileEntity> members = Lists.newArrayList();
        for (int i = 0; i < jsonArray.length(); i++) {
            UserProfileEntity member = ParseUtils.parseMember(jsonArray.getJSONObject(i));
            members.add(member);
        }

        if (this.apiListener != null) {
            this.apiListener.onSuccess(crowdEntity);
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        if (this.apiListener != null) {
            this.apiListener.onFailed(errorMessage);
        }
    }
}
