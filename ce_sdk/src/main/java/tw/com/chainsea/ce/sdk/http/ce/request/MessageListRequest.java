package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import com.google.common.collect.Lists;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.ce.sdk.bean.response.base.ResponseBean;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.lib.ErrCode;

/**
 * MessageRequest to refresh messages
 * Created by Fleming on 2016/6/13.
 */
public class MessageListRequest extends NewRequestBase {
    private final ApiListener<Resp> listener;

    public MessageListRequest(Context ctx, ApiListener<Resp> listener) {
        super(ctx, "/" + ApiPath.messageList);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        try {
            Resp resp = JsonHelper.getInstance().from(s, Resp.class);
            if (resp != null && resp.items != null && !resp.items.isEmpty()) {
                String roomId = resp.roomId;
                Iterator<MessageEntity> iterator = resp.items.iterator();
                while (iterator.hasNext()) {
                    MessageEntity entity = iterator.next();
                    if (!roomId.equals(entity.getRoomId())) {
                        iterator.remove();
                    }
                }
                Collections.sort(resp.items);

//                completeMessageReadingStatus(resp);

                if (this.listener != null) {
                    this.listener.onSuccess(resp);
                }
            }
        } catch (Exception e) {
            failed(ErrCode.RESPONSE_PARAMETER_NOT_FOUND, e.getMessage());
        }


//        CELog.e("");
//        JSONArray items = jsonObject.getJSONArray("items");
//        boolean hasNextPage = jsonObject.getBoolean("hasNextPage");
//        List<MessageEntity> messages = JsonHelper.getInstance().fromToList(items.toString(), MessageEntity[].class);
//        try {
//            String roomId = mJSONObject.getString("roomId");
//            Iterator<MessageEntity> iterator = messages.iterator();
//            while (iterator.hasNext()) {
//                MessageEntity entity = iterator.next();
//                if (Strings.isNullOrEmpty(entity.getAvatarId())) {
//                    throw new RuntimeException("avatar id is null ");
//                }
//                if (!roomId.equals(entity.getRoomId())) {
//                    iterator.remove();
//                }
//            }
//        } catch (Exception e) {
//
//        }
//        Collections.sort(messages);
//        if (this.listener != null) {
//            this.listener.onSuccess(Resp.Build()
//                    .items(messages)
//                    .hasNextPage(hasNextPage)
//                    .build());
//        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        CELog.e(errorMessage, code);
        if (this.listener != null) {
            this.listener.onFailed(errorMessage);
        }
    }


//    /**
//     * Created by Fleming on 2021/2/17.
//     * Organize the correctness of message read receive
//     * @version 1.15.1
//     * @author Evan.W
//     */
//    private Resp completeMessageReadingStatus(Resp resp) {
//        List<MessageEntity> list = Lists.newArrayList(resp.getItems());
//        List<MessageEntity> resultList = Lists.newArrayList();
//
//        int maxRead = -1, maxReceive = -1;
//        long maxReadTime = -1L, maxReceiveTime = -1L;
//        String maxReadId = "", maxReceiveId = "";
//        boolean readChange, receiveChange;
//
//        for (MessageEntity m : list) {
//            readChange = false;
//            receiveChange = false;
//            int read = m.getReadedNum();
//            int receive = m.getReceivedNum();
//            for (MessageEntity s : list) {
//                if (s.getSendTime() < m.getSendTime()) {
//                    if (s.getReadedNum() < read) {
//                        readChange = true;
//                        read = Math.max(s.getReadedNum(), read);
//                        CELog.w("complete Message Reading Status By message/list" + m.getId());
//                    }
//
//                    if (s.getReceivedNum() < receive) {
//                        receiveChange = true;
//                        receive = Math.max(s.getReceivedNum(), receive);
//                        CELog.w("complete Message Receiving Status By message/list" + m.getId());
//                    }
//                } else {
//                    if (s.getReadedNum() > read) {
//                        readChange = true;
//                        read = Math.max(s.getReadedNum(), read);
//                        CELog.w("complete Message Reading Status By message/list" + m.getId());
//                    }
//
//                    if (s.getReceivedNum() > receive) {
//                        receiveChange = true;
//                        receive = Math.max(s.getReceivedNum(), receive);
//                        CELog.w("complete Message Receiving Status By message/list" + m.getId());
//                    }
//                }
//            }
//
//            if (read >= maxRead && readChange) {
//                maxRead = read;
//                maxReadId = m.getId();
//                maxReadTime = m.getSendTime();
//            }
//
//            if (receive >= maxReceive && receiveChange) {
//                maxReceive = receive;
//                maxReceiveId = m.getId();
//                maxReceiveTime = m.getSendTime();
//            }
//            resultList.add(m.toBuilder().readedNum(Math.min(m.getSendNum(), read)).receivedNum(Math.min(m.getSendNum(), receive)).build());
//        }
//
//        resp.getItems().clear();
//        resp.setItems(resultList);
//
//
//        if (maxReadTime > -1L && maxRead > -1) {
//            resp.getUpdateItems().add(Resp.UpdateItem.of(maxReadId, maxRead, maxReadTime));
//        }
//
//        if (maxReceiveTime > -1L && maxReceive > -1) {
//            resp.getUpdateItems().add(Resp.UpdateItem.of(maxReceiveId, maxReceive, maxReceiveTime));
//        }
//
//        return resp;
//    }

    public static class Resp extends ResponseBean implements Serializable {
        private static final long serialVersionUID = -8435105573423615338L;
        private String roomId;
        private List<MessageEntity> items = Lists.newArrayList();
        private boolean hasNextPage;

//        private List<UpdateItem> updateItems = Lists.newArrayList();

        @Override
        public void close() throws Exception {

        }

        public String getRoomId() {
            return roomId;
        }

        public void setRoomId(String roomId) {
            this.roomId = roomId;
        }

        public List<MessageEntity> getItems() {
            return items;
        }

        public void setItems(List<MessageEntity> items) {
            this.items = items;
        }

        public boolean isHasNextPage() {
            return hasNextPage;
        }

        public void setHasNextPage(boolean hasNextPage) {
            this.hasNextPage = hasNextPage;
        }

        //        public static class UpdateItem {
//            private String id;
//            private long sendTime;
//            private int value;
//
//
//            public static UpdateItem of(String id, int value, long sendTime) {
//                UpdateItem item = new UpdateItem();
//                item.id = id;
//                item.value = value;
//                item.sendTime = sendTime;
//                return item;
//            }
//        }
    }
}
