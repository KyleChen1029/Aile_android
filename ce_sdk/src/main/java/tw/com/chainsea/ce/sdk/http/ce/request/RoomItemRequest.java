package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import com.google.common.base.Strings;

import org.json.JSONException;
import org.json.JSONObject;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberStatus;
import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberType;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.service.type.ChatRoomSource;

/**
 * SessionInfoRequest to search session's detail info
 * Created by 90Chris on 2016/7/1.
 */
public class RoomItemRequest extends NewRequestBase {
    private final ApiListener<ChatRoomEntity> listener;
    private final String userId;

    public RoomItemRequest(Context ctx, String userId, ApiListener<ChatRoomEntity> listener) {
        super(ctx, "/" + ApiPath.chatRoomItem);
        this.userId = userId;
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        if (this.listener != null) {
            ChatRoomEntity entity = JsonHelper.getInstance().from(jsonObject.toString(), ChatRoomEntity.class);

            if (ServiceNumberStatus.OFF_LINE.equals(entity.getServiceNumberStatus())) {
                entity.setServiceNumberAgentId("");
            }
            if (!Strings.isNullOrEmpty(entity.getBusinessExecutorId())) {
                CELog.w("has BusinessExecutorId:: roomId = " + entity.getId());
            }

            if (ChatRoomType.serviceMember.equals(entity.getType())) {
                entity.setListClassify(ChatRoomSource.SERVICE);
                if (ServiceNumberType.BOSS.equals(entity.getServiceNumberType())) {
                    entity.setListClassify(!userId.equals(entity.getServiceNumberOwnerId()) ? ChatRoomSource.SERVICE : ChatRoomSource.MAIN);
                }
            } else if (ChatRoomType.services.equals(entity.getType())) {
                entity.setListClassify(ChatRoomSource.SERVICE);

                if (entity.getOwnerId().equals(userId)) {
                    entity.setType(ChatRoomType.subscribe);
                    entity.setListClassify(ChatRoomSource.MAIN);
                } else {
                    if (ServiceNumberType.BOSS.equals(entity.getServiceNumberType()) && !userId.equals(entity.getServiceNumberOwnerId())) {
                        entity.setListClassify(ChatRoomSource.SERVICE);
                    }

                    if (ServiceNumberType.BOSS.equals(entity.getServiceNumberType()) && userId.equals(entity.getServiceNumberOwnerId())) {
                        entity.setListClassify(ChatRoomSource.MAIN);
                    }
                }
            } else if (!ChatRoomType.MAIN_CHAT_ROOM_TYPES_2.contains(entity.getType())) {
                entity.setListClassify(ChatRoomSource.MAIN);
            }

            this.listener.onSuccess(entity);
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        CELog.e(errorMessage);
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }

}
