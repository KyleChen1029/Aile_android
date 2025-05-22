package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.SdkLib;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.database.sp.UserPref;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.lib.ParseUtils;

/**
 * UserProfileRequest to get user's simple profile
 * Created by chris on 7/29/16.
 */
public class UserProfileRequest extends NewRequestBase {
    private final Listener listener;

    public UserProfileRequest(Context ctx, Listener listener) {
        super(ctx, "/" + ApiPath.userProfile);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        UserProfileEntity account = ParseUtils.parseUser(jsonObject);
        boolean hasBindEmployee = false;
        if (JsonHelper.getInstance().has(jsonObject , "hasBindEmployee")) {
            hasBindEmployee = jsonObject.getBoolean("hasBindEmployee");
        }

        UserPref.getInstance(SdkLib.getAppContext())
                .setHasBindEmployee(hasBindEmployee)
                .setUserName(account.getNickName())
                .setUserAvatarId(account.getAvatarId())
                .setPersonRoomId(account.getPersonRoomId());

        if (this.listener != null) {
            this.listener.onProfileSuccess(account);
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        CELog.e("request user profile failed: " + code.getValue());
        if (ErrCode.INVALID_LOGIN_NAME_OR_PASSWORD.equals(code)) {
            if (this.listener != null) {
                this.listener.onInvalidAccPw();
            }
        } else if (code == ErrCode.THRID_LOGIN_UNBIND) {
            if (this.listener != null) {
                this.listener.onThridLoginUnBind();
            }
        }
        if (this.listener != null) {
            this.listener.onProfileFailed(errorMessage);
        }
    }

    public interface Listener {
        void onProfileSuccess(UserProfileEntity profile);

        void onInvalidAccPw();

        void onProfileFailed(String errorMessage);

        void onThridLoginUnBind();
    }
}
