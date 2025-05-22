package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.lib.ParseUtils;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;

/**
 * UserItemRequest to get user's info
 * Created by Andy on 2016/6/9.
 */
public class UserItemRequest extends NewRequestBase {
    private final Listener listener;

    public UserItemRequest(Context ctx, Listener listener) {
        super(ctx, "/" + ApiPath.userItem);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        UserProfileEntity accountCE = ParseUtils.parseUser(jsonObject);
        if (this.listener != null) {
            this.listener.onSuccess(accountCE);
        }
        DBManager.getInstance().insertFriends(accountCE);
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }

    public interface Listener {
        void onSuccess(UserProfileEntity entity);

        void onFailed(String errorMessage);
    }
}
