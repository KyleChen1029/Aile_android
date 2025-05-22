package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Set;

import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.ce.sdk.bean.account.Gender;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.lib.ParseUtils;
import tw.com.chainsea.ce.sdk.reference.AccountRoomRelReference;

/**
 * MemberListRequest to get members of group
 * Created by Fleming on 2016/8/8.
 */
public class MemberListRequest extends NewRequestBase {
    private ApiListener<List<UserProfileEntity>> listener;

    public MemberListRequest(Context ctx, ApiListener<List<UserProfileEntity>> listener) {
        super(ctx, "/" + ApiPath.memberList);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        List<UserProfileEntity> list = Lists.newArrayList();
        JSONArray items = jsonObject.getJSONArray("items");
        Set<String> memberIdSet = Sets.newHashSet();
        for (int i = 0, length = items.length(); i < length; i++) {
            UserProfileEntity account = ParseUtils.parseAccount(items.getJSONObject(i));
            list.add(account);
            memberIdSet.add(account.getId());
        }
        String roomId = mJSONObject.getString("roomId");
//        AccountRoomRelReference.deleteRelByRoomId(null, roomId);
        AccountRoomRelReference.batchSaveByAccountIdsAndRoomId(null, roomId, Lists.newArrayList(memberIdSet));
        if (this.listener != null) {
            ThreadExecutorHelper.getMainThreadExecutor().execute(() -> this.listener.onSuccess(list));
        }
    }

    private UserProfileEntity parseJson(JSONObject json) {
        UserProfileEntity accountCE = new UserProfileEntity();

        try {
            if (json.has("id")) {
                accountCE.setId(json.getString("id"));
            }

            if (json.has("name")) {
                accountCE.setNickName(json.getString("name"));
            }

            if (json.has("")) {
            }
            if (json.has("gender")) {
                Gender gender = null;
                String value = json.getString("gender");
                if (value.equals("0")) {
                    gender = Gender.UNDEF;
                } else if (value.equals("M")) {
                    gender = Gender.MALE;
                } else if (value.equals("F")) {
                    gender = Gender.FEMALE;
                }
                accountCE.setGender(gender);
            }
            if (json.has("avatarId")) {
                accountCE.setAvatarId(json.getString("avatarId"));
            }
        } catch (JSONException ignored) {
        }

        return accountCE;

    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        if (this.listener != null) {
            ThreadExecutorHelper.getMainThreadExecutor().execute(() -> this.listener.onFailed(errorMessage));
        }
    }

}
