package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.lib.ParseUtils;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.android.common.log.CELog;

/**
 * UserSearchRequest to search the people you want to search
 * Created by Andy on 2016/6/13.
 */
public class UserSearchRequest extends NewRequestBase {
    private final Listener mListener;

    public UserSearchRequest(Context ctx, Listener listener) {
        super(ctx, ApiPath.userList);
        mListener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        List<UserProfileEntity> list = new ArrayList<>();
        boolean hasNextPage = jsonObject.getBoolean("hasNextPage");
        JSONArray items = jsonObject.getJSONArray("items");
        for (int i = 0, length = items.length(); i < length; i++) {
            list.add(ParseUtils.parseAccount(items.getJSONObject(i)));
        }

        String  keyword = mJSONObject.getString("keyword");
        int  index = mJSONObject.getInt("pageIndex");
        mListener.onEmployeeListSuccess(list, hasNextPage, keyword, index);
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        CELog.e("search user failed: " + code.getValue());
        mListener.onEmployeeListFailed(errorMessage);
    }

    public interface Listener {
        void onEmployeeListSuccess(List<UserProfileEntity> list, boolean hasNextPage, String keyWord, int index);

        void onEmployeeListFailed(String errorMessage);
    }

}
