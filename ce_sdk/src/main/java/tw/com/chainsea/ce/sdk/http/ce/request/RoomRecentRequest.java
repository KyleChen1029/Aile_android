package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;
import androidx.annotation.Nullable;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.List;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.http.ce.response.RoomRecentRsp;
import tw.com.chainsea.ce.sdk.lib.ErrCode;

/**
 * RoomRecentRequest to get session list
 * Created by Fleming on 2016/6/12.
 * 1、Get the information first displayed on the UI interface
 * 2、after Saving The DB Update The UI One By One
 * 3、save lastRefreshTime
 * @version 1.10.0
 */
public class RoomRecentRequest extends NewRequestBase {
    private final ApiListener<RoomRecentRsp> listener;

    public RoomRecentRequest(Context ctx, @Nullable ApiListener<RoomRecentRsp> listener) {
//        super(ctx, ApiPath.chatRoomRecent); // 套用新的 sync/room api 在舊的流程上，看起來可以相通也比較快一些
        super(ctx, "/" + ApiPath.syncRoom);
        this.listener = listener;
    }

    @Override
    protected void success(final JSONObject jsonObject, String s) throws JSONException {
//        if (JsonHelper.getInstance().has(jsonObject, "items", "lastRefreshTime")) {
            try {
                RoomRecentRsp resp = JsonHelper.getInstance().from(s, RoomRecentRsp.class);
                if (resp != null && resp.getItems() != null && !resp.getItems().isEmpty()) {
                    if (listener != null) {
                        listener.onSuccess(resp);
                    }
                } else {
                    if (listener != null) {
                        listener.onFailed("no data");
                    }
                }
            } catch (Exception e) {
                CELog.e(e.getMessage(), e);
                if (listener != null) {
                    listener.onFailed(e.getMessage());
                }
            }
//        }
    }


    @Override
    protected void failed(ErrCode code, String errorMessage) {
        CELog.e(errorMessage);
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }

    public interface Listener {
        void onSuccess(List<ChatRoomEntity> entities, long lastRefreshTime);

        void onFailed(String errorMessage);
    }
}
