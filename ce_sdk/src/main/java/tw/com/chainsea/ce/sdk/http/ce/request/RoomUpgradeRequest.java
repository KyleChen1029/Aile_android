package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;

/**
 * 升級為社團
 */
public class RoomUpgradeRequest extends NewRequestBase {
    private Listener listener;

    public RoomUpgradeRequest(Context ctx, Listener listener) {
        super(ctx, "/" + ApiPath.chatRoomUpgrade);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        String groupOwnerId = jsonObject.getString("ownerId");
        String groupId = jsonObject.getString("roomId");
        String avatarId = jsonObject.getString("avatarId");
        if (this.listener != null) {
            this.listener.onSuccess(groupId, groupOwnerId, avatarId);
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }

    }

    public interface Listener {
        void onSuccess(String roomId, String ownerId, String avatarId);

        void onFailed(String errorMessage);
    }
}
