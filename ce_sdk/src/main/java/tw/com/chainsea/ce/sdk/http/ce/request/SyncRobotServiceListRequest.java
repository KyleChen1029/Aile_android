package tw.com.chainsea.ce.sdk.http.ce.request;

import static tw.com.chainsea.ce.sdk.database.DBContract.REFRESH_TIME_SOURCE.CHAT_ROOM_ROBOT_SERVICE_LIST;

import android.content.Context;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.http.ce.response.RoomRecentRsp;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.service.listener.ApiCallback;
import tw.com.chainsea.ce.sdk.service.type.ChatRoomSource;

public class SyncRobotServiceListRequest extends NewRequestBase {
    private final ApiCallback<List<ChatRoomEntity>> listener;

    public SyncRobotServiceListRequest(Context ctx, @Nullable ApiCallback<List<ChatRoomEntity>> listener) {
        super(ctx, "/" + ApiPath.chatRoomRobotServiceList);
        this.listener = listener;
    }


    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        RoomRecentRsp rsp;
        try {
            rsp = JsonHelper.getInstance().from(s, RoomRecentRsp.class);
        } catch (Exception exception) {
            if (listener != null) listener.error("data error : " + s);
            return;
        }
        if (rsp != null && rsp.getItems() != null && !rsp.getItems().isEmpty()) {
            List<ChatRoomEntity> items = new ArrayList<>();
            for (ChatRoomEntity entity : rsp.getItems()) {
                if (ChatRoomType.services.equals(entity.getType()))
                    entity.setListClassify(ChatRoomSource.SERVICE);
                items.add(entity);
            }
            if (listener != null) listener.complete(items);
            DBManager.getInstance().updateOrInsertApiInfoField(CHAT_ROOM_ROBOT_SERVICE_LIST, rsp.getRefreshTime());
            if (rsp.getHasNextPage()) {
                new SyncRobotServiceListRequest(ctx, listener)
                        .setMainThreadEnable(false)
                        .request(new JSONObject()
                                .put("refreshTime", rsp.getRefreshTime()));
            } else {
                if (listener != null) listener.finish();
            }
        } else {
            if (listener != null) listener.error("no data");
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        listener.error("errCode: " + code + ",msg: " + errorMessage);
    }
}

