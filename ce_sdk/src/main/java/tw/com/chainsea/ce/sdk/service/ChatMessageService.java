package tw.com.chainsea.ce.sdk.service;

import android.content.Context;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.ce.sdk.bean.broadcast.TopicEntity;
import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.ce.sdk.bean.msg.MessageFlag;
import tw.com.chainsea.ce.sdk.bean.msg.MessageStatus;
import tw.com.chainsea.ce.sdk.bean.msg.MessageType;
import tw.com.chainsea.ce.sdk.bean.parameter.Sort;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.database.sp.UserPref;
import tw.com.chainsea.ce.sdk.event.EventBusUtils;
import tw.com.chainsea.ce.sdk.event.EventMsg;
import tw.com.chainsea.ce.sdk.event.MsgConstant;
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference;
import tw.com.chainsea.ce.sdk.reference.MessageReference;
import tw.com.chainsea.ce.sdk.reference.TopicReference;
import tw.com.chainsea.ce.sdk.http.ce.request.MessageListRequest;
import tw.com.chainsea.ce.sdk.http.ce.request.MessageReadingStateRequest;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
import tw.com.chainsea.ce.sdk.service.listener.OnToolBarProgressListener;
import tw.com.chainsea.ce.sdk.service.listener.RefreshMsgCallBack;
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
import tw.com.chainsea.android.common.log.CELog;

/**
 * current by evan on 2020-03-13
 */
public class ChatMessageService {


    /**
     * Verify message status
     */
    public static void verifyMessageReadingState(Context context, ChatRoomEntity roomEntity, List<MessageEntity> entities, ServiceCallBack<List<MessageEntity>, Enum> callBack) {
        Set<String> messageIdSet = Sets.newHashSet();
        if (!ChatRoomType.SELF_or_SYSTEM.contains(roomEntity.getType())) {
            for (MessageEntity entity : entities) {
                if (MessageFlag.OWNER.equals(entity.getFlag()) && entity.getSendNum() > 0 && entity.getSendNum() != entity.getReadedNum()) {
                    messageIdSet.add(entity.getId());
                }
            }
        }

        ApiManager.doMessageReadingState(context, roomEntity.getId(), Lists.newArrayList(messageIdSet), new ApiListener<List<MessageReadingStateRequest.Resp.Item>>() {
            @Override
            public void onSuccess(List<MessageReadingStateRequest.Resp.Item> result) {
                Iterator<MessageEntity> iterator = entities.iterator();
                Iterator<MessageReadingStateRequest.Resp.Item> rIterator = result.iterator();
                List<MessageEntity> list = Lists.newArrayList();
                while (iterator.hasNext()) {
                    MessageEntity entity = iterator.next();
                    while (rIterator.hasNext()) {
                        MessageReadingStateRequest.Resp.Item item = rIterator.next();
                        if (item.getId().equals(entity.getId())) {
                            entity.setSendNum(item.getSendNum());
                            entity.setReadedNum(item.getReadedNum());
                            entity.setReceivedNum(item.getReceivedNum());
                            list.add(entity);
                        }
                    }
                }

                boolean status = MessageReference.saveByRoomId(roomEntity.getId(), entities);
                if (callBack != null) {
                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> callBack.complete(list, null));
                }
            }

            @Override
            public void onFailed(String errorMessage) {
                if (callBack != null) {
                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> callBack.error(errorMessage));
                }
            }
        });
    }

    private static void processingBroadcastMessageData(String broadcastRoomId, List<MessageEntity> entities, ServiceCallBack<List<MessageEntity>, RefreshSource> callBack) {
        boolean saveStatus = MessageReference.saveByRoomId(broadcastRoomId, entities);
        Iterator<MessageEntity> iterator = entities.iterator();
        // Exclude non-broadcast chat room messages
        while (iterator.hasNext()) {
            MessageEntity entity = iterator.next();
            if (MessageType.BROADCAST.equals(entity.getType()) && !entity.getTopicArray().isEmpty()) {
                Set<TopicEntity> topicSet = Sets.newHashSet(entity.getTopicArray());
                entity.setTopicArray(Lists.newArrayList(topicSet));
                boolean status = TopicReference.saveByRelIdAndTopicIdsAndType(null, entity.getId(), entity.getTopicIds(), TopicReference.TopicRelType.MESSAGE);
            } else {
                iterator.remove();
            }
        }

        if (callBack != null) {
            callBack.complete(entities, RefreshSource.REMOTE);
        }
    }


    public static void getChatMessageEntities(Context context, String roomId, String lastMessageId, Sort sort, ServiceCallBack<List<MessageEntity>, RefreshSource> callBack) {
        if (lastMessageId == null || lastMessageId.isEmpty()) {
            ApiManager.doMessageList(context, roomId, 20, lastMessageId, Sort.DESC.name().toLowerCase(), false, true, new ApiListener<MessageListRequest.Resp>() {
                @Override
                public void onSuccess(MessageListRequest.Resp response) {
                    List<MessageEntity> items = response.getItems();
                    processingChatMessageData(roomId, items, callBack);
                }

                @Override
                public void onFailed(String errorMessage) {
                    CELog.e(errorMessage);
                    processingChatMessageData(roomId, Lists.newArrayList(), callBack);
                    callBack.error(errorMessage);
                }
            });
        }else {
            ApiManager.doMessageList(context, roomId, 50, lastMessageId, Sort.ASC.name().toLowerCase(), false, true, new ApiListener<MessageListRequest.Resp>() {
                @Override
                public void onSuccess(MessageListRequest.Resp response) {
                    List<MessageEntity> items = response.getItems();
                    processingChatMessageData(roomId, items, callBack);
                }

                @Override
                public void onFailed(String errorMessage) {
                    CELog.e(errorMessage);
                    processingChatMessageData(roomId, Lists.newArrayList(), callBack);
                    callBack.error(errorMessage);
                }
            });
        }
    }


    /**
     * 取得 Chat Message Entities 後，處理儲存 Local DB 相關流程
     * @param roomId
     * @param entities
     * @param callBack
     */
    private static void processingChatMessageData(String roomId, List<MessageEntity> entities, ServiceCallBack<List<MessageEntity>, RefreshSource> callBack) {
        Iterator<MessageEntity> iterator = entities.iterator();
        // Exclude non-broadcast chat room messages
        while (iterator.hasNext()) {
            MessageEntity entity = iterator.next();
            if (!roomId.equals(entity.getRoomId())) {
                iterator.remove();
            }
        }
        boolean saveStatus = MessageReference.saveByRoomId(roomId, entities);
        if (callBack != null) {
            ThreadExecutorHelper.getMainThreadExecutor().execute(() -> callBack.complete(entities, RefreshSource.REMOTE));
        }
    }


    // TODO 看起來沒有用到這個 Method 可以確認一下不知為何有此 Method? 或者被誤砍？如真沒用到就刪掉 (主要使用 getChatMessageEntities)
//    public static void getMessageEntities(Context context, String roomId, String lastMessageId, Sort sort, ServiceCallBack<List<MessageEntity>, RefreshSource> callBack) {
//        if (Sort.ALL_or_DESC.contains(sort)) {
//            ApiManager.doMessageList(context, roomId, 50, lastMessageId, Sort.DESC.name().toLowerCase(), false, true, new ApiListener<MessageListRequest.Resp>() {
//                @Override
//                public void onSuccess(MessageListRequest.Resp response) {
//                    List<MessageEntity> items = response.getItems();
//                    processingMessageData(roomId, items, callBack);
//                }
//
//                @Override
//                public void onFailed(String errorMessage) {
//                    CELog.e(errorMessage);
//                    if (callBack != null) {
//                        callBack.error(errorMessage);
//                    }
//                }
//            });
//        }
//
//        if (Sort.ALL_or_ASC.contains(sort)) {
//            ApiManager.doMessageList(context, roomId, 50, lastMessageId, Sort.ASC.name().toLowerCase(), false, true, new ApiListener<MessageListRequest.Resp>() {
//                @Override
//                public void onSuccess(MessageListRequest.Resp response) {
//                    List<MessageEntity> items = response.getItems();
//                    processingMessageData(roomId, items, callBack);
//                }
//
//                @Override
//                public void onFailed(String errorMessage) {
//                    CELog.e(errorMessage);
//                    if (callBack != null) {
//                        callBack.error(errorMessage);
//                    }
//                }
//            });
//        }
//    }

//    /**
//     * 這裡基本只 Call --> MessageReference.saveByRoomId(roomId, entities)
//     * TODO 本來有幾行演算法但 Evan 自己又將這幾行邏輯 mark 掉，這邊應可以改寫更精簡
//     *
//     * @param roomId
//     * @param entities
//     * @param callBack
//     */
//    private static void processingMessageData(String roomId, List<MessageEntity> entities, ServiceCallBack<List<MessageEntity>, RefreshSource> callBack) {
//        boolean saveStatus = MessageReference.saveByRoomId(roomId, entities);
//        Iterator<MessageEntity> iterator = entities.iterator();
        // Exclude non-broadcast chat room messages
//        while (iterator.hasNext()) {
//            MessageEntity entity = iterator.next();
//            if (MessageType.BROADCAST.equals(entity.getType()) && !entity.getTopicArray().isEmpty()) {
//                Set<TopicEntity> topicSet = Sets.newHashSet(entity.getTopicArray());
//                entity.setTopicArray(Lists.newArrayList(topicSet));
//                boolean status = TopicReference.saveByRelIdAndTopicIdsAndType(null, entity.getId(), entity.getTopicIds(), TopicReference.TopicRelType.MESSAGE);
//            } else {
//                iterator.remove();
//            }
//        }

//        if (callBack != null) {
//            callBack.complete(entities, RefreshSource.REMOTE);
//        }
//    }


    public static void getBroadcastMessageEntities(Context context, String broadcastRoomId, String lastMessageId, Sort sort, ServiceCallBack<List<MessageEntity>, RefreshSource> callBack) {
        if (Sort.ALL_or_DESC.contains(sort)) {
            ApiManager.doMessageList(context, broadcastRoomId, 50, lastMessageId, Sort.DESC.name().toLowerCase(), false, true, new ApiListener<MessageListRequest.Resp>() {
                @Override
                public void onSuccess(MessageListRequest.Resp response) {
                    List<MessageEntity> items = response.getItems();
                    processingBroadcastMessageData(broadcastRoomId, items, callBack);
                }

                @Override
                public void onFailed(String errorMessage) {
                    CELog.e(errorMessage);
                    if (callBack != null) {
                        callBack.error(errorMessage);
                    }
                }
            });
        }

        if (Sort.ALL_or_ASC.contains(sort)) {
            ApiManager.doMessageList(context, broadcastRoomId, 50, lastMessageId, Sort.ASC.name().toLowerCase(), false, true, new ApiListener<MessageListRequest.Resp>() {
                @Override
                public void onSuccess(MessageListRequest.Resp response) {
                    List<MessageEntity> items = response.getItems();
                    processingBroadcastMessageData(broadcastRoomId, items, callBack);
                }

                @Override
                public void onFailed(String errorMessage) {
                    CELog.e(errorMessage);
                    if (callBack != null) {
                        callBack.error(errorMessage);
                    }
                }
            });
        }
    }

    private static int getMessageCount = 0;
    private static String tempMessageId = "";
    /**
     * Information (old) --> (new)
     */
    public static void getChatMessageEntities(Context context, String roomId, ChatRoomType chatRoomType, String lastReadMsgId, int pageSize, Sort sort, final ServiceCallBack<List<MessageEntity>, RefreshSource> callBack) {
        if (getMessageCount > 5) {
            getMessageCount = 0;
        }
        ApiManager.doMessageList(context, roomId, pageSize, lastReadMsgId, sort.name().toLowerCase(), false, true, new ApiListener<MessageListRequest.Resp>() {
            @Override
            public void onSuccess(MessageListRequest.Resp response) {
                getMessageCount++;
                List<MessageEntity> entities = response.getItems();
                if (entities.isEmpty()) {
                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> callBack.complete(Lists.newArrayList(), RefreshSource.REMOTE));
                    return;
                }

                if (!tempMessageId.isEmpty()) {
                    if (tempMessageId.equals(entities.get(0).getId())) {
                        tempMessageId = "";
                        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> callBack.complete(Lists.newArrayList(), RefreshSource.REMOTE));
                        return;
                    }
                }
                tempMessageId = entities.get(0).getId();

                List<String> receivedIds = Lists.newArrayList();
                String userId = TokenPref.getInstance(context).getUserId();

                for (MessageEntity entity : entities) {
                    // If it is not (1) has arrived, (2) has been read, (3) is retracted, (-1) the message sent by oneself means that the message has arrived.
                    if (MessageFlag.SEND.equals(entity.getFlag())) {
                        receivedIds.add(entity.getId());
                    }
                    String senderId = entity.getSenderId();
                    if (senderId.equals(userId)) {
                        if (entity.getSendNum() >= 0) {
                            entity.setStatus(MessageStatus.SUCCESS);
                        }
                        if (entity.getReceivedNum() > 0) {
                            entity.setStatus(MessageStatus.RECEIVED);
                        }
                        if (entity.getReadedNum() > 0) {
                            entity.setStatus(MessageStatus.READ);
                        }
                    } else {
                        entity.setStatus(MessageStatus.READ);
                    }
                }

                boolean status = MessageReference.saveByRoomId(roomId, entities);
                //TODO 這個 Long.valueOf 理論上是都會噴 Exception ，但有看過 lastReadMsgId 裡面存 millisecond
                List<MessageEntity> entities2 =
                        MessageReference.findByRoomIdAndTypeAndSortAndLimitAndInnerJoniAliasName(roomId, System.currentTimeMillis(), chatRoomType, MessageReference.Sort.DESC, 50);

                if (!status) {
                    CELog.e("DBManager.insertMessages error", "roomId: " + roomId, entities2);
                }

                doMessageReceived(context, receivedIds);
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> callBack.complete(entities2, RefreshSource.REMOTE));
                if (response.isHasNextPage() && getMessageCount < 5) {
                    String previousMessageId = entities.get(0).getPreviousMessageId();
                    getChatMessageEntities(context, roomId, chatRoomType, previousMessageId, pageSize, sort, callBack);
                }

            }

            @Override
            public void onFailed(String errorMessage) {
                CELog.e("request message failed "+errorMessage);
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> callBack.complete(Lists.newArrayList(), RefreshSource.REMOTE));
            }
        });
    }

    public static void doMessageReceived(Context context, List<String> messageIds) {
        ApiManager.doMessagesReceived(context, messageIds, new ApiListener<String>() {
            @Override
            public void onSuccess(String s) {
                CELog.d("reminder sent successfully");
            }

            @Override
            public void onFailed(String errorMessage) {
                CELog.e("failed to send reminder");
            }
        });
    }

    /**
     * //     * message list id to id
     * //     *
     * //     * @param roomId
     * //     * @param srcMessageId
     * //     * @param destMessageId    <p> DB last message id <p/>
     * //     * @param refreshCallback
     * //     * @param progressListener
     * //
     */
//    public static void messageListRequestIdToId(Context context, String roomId, String srcMessageId, String destMessageId, RefreshMsgCallBack refreshCallback) {
//        ApiManager.doMessageList(context, roomId, srcMessageId, destMessageId, new ApiListener<MessageListRequest.Resp>() {
//            @Override
//            public void onSuccess(MessageListRequest.Resp response) {
//                List<MessageEntity> entities = response.getItems();
//                boolean hasNextPage = response.isHasNextPage();
//                if (entities.isEmpty()) {
//                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
//                        refreshCallback.refreshFinish(Lists.newArrayList());
//                    });
//                    return;
//                }
//
//                Set<String> idSet = Sets.newHashSet();
//                List<String> receivedIds = Lists.newArrayList();
//                String userId = TokenPref.getInstance(context).getUserId();
//                for (MessageEntity entity : entities) {
//                    idSet.add(entity.getId());
//                    // If it is not (1) has arrived, (2) has been read, (3) is retracted, (-1) the message sent by oneself means that the message has arrived.
//                    if (MessageFlag.SEND.equals(entity.getFlag())) {
//                        receivedIds.add(entity.getId());
//                    }
//
//                    String senderId = entity.getSenderId();
//                    if (senderId.equals(userId)) {
//                        if (entity.getSendNum() >= 0) {
//                            entity.setStatus(MessageStatus.SUCCESS);
//                        }
//                        if (entity.getReceivedNum() > 0) {
//                            entity.setStatus(MessageStatus.RECEIVED);
//                        }
//                        if (entity.getReadedNum() > 0) {
//                            entity.setStatus(MessageStatus.READ);
//                        }
//                    } else {
//                        entity.setStatus(MessageStatus.READ);
//                    }
//                }
//                long start = System.currentTimeMillis();
//                boolean status = MessageReference.saveByRoomId(roomId, entities);
//                doMessageReceived(context, receivedIds);
//                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
//                    refreshCallback.refreshFinish(entities);
//                });
//            }
//
//            @Override
//            public void onFailed(String errorMessage) {
//                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
//                    refreshCallback.refreshFinish(Lists.newArrayList());
//                });
//            }
//        });
//    }
    public static void messageListRequestAsc(Context context, String roomId, String localLastId, final RefreshMsgCallBack refreshCallback) {
        ApiManager.doMessageList(context, roomId, 50, localLastId, Sort.ASC.name().toLowerCase(), false, true, new ApiListener<MessageListRequest.Resp>() {
            @Override
            public void onSuccess(MessageListRequest.Resp response) {
                List<MessageEntity> entities = response.getItems();
                if (entities.isEmpty()) {
                    refreshCallback.refreshFinish(Lists.newArrayList());
                    return;
                }

                Set<String> idSet = Sets.newHashSet();
                List<String> receivedIds = Lists.newArrayList();
                String userId = TokenPref.getInstance(context).getUserId();
                for (MessageEntity entity : entities) {
                    idSet.add(entity.getId());
                    if (MessageFlag.SEND.equals(entity.getFlag())) {
                        receivedIds.add(entity.getId());
                    }

                    String senderId = entity.getSenderId();
                    if (senderId.equals(userId)) {
                        if (entity.getSendNum() >= 0) {
                            entity.setStatus(MessageStatus.SUCCESS);
                        }
                        if (entity.getReceivedNum() > 0) {
                            entity.setStatus(MessageStatus.RECEIVED);
                        }
                        if (entity.getReadedNum() > 0) {
                            entity.setStatus(MessageStatus.READ);
                        }
                    } else {
                        entity.setStatus(MessageStatus.READ);
                    }
                }
                long start = System.currentTimeMillis();
                boolean status = MessageReference.saveByRoomId(roomId, entities);
                doMessageReceived(context, receivedIds);
                refreshCallback.refreshFinish(entities);

            }

            @Override
            public void onFailed(String errorMessage) {
                refreshCallback.refreshFinish(Lists.newArrayList());
            }
        });
    }

    /**
     * complete message list processing
     */
    public static void doCompleteMessageEntitiesProcessing(Context context, ChatRoomEntity entity, boolean isUnreadRoom) {
        String roomId = entity.getId();
        MessageEntity remoteLastMessageEntity = entity.getLastMessage();
        String selfId = TokenPref.getInstance(context).getUserId();

        // If the remote data does not have LastMessage
        if (entity.getLastMessage() == null) {
            EventBusUtils.sendEvent(new EventMsg(MsgConstant.CHAT_ROOM_DATA_LOADING_PROGRESS_EVENT, 1));
            if (entity.getUnReadNum() > 0) {
                boolean hasUnReadAtMe = MessageReference.findUnreadAtMessagesByRoomId(null, selfId, roomId);
                if (hasUnReadAtMe) {
                    EventBusUtils.sendEvent(new EventMsg(MsgConstant.OUTCROP_MENTION_UNREAD_ROOM, JsonHelper.getInstance().toJson(ChatRoomEntity.Build().id(roomId).isAtMe(hasUnReadAtMe).build())));
                }
            }
            return;
        }


        // get local data  lastMessageId
        String localLastMessageId = MessageReference.findLastMessageIdByRoomId(null, roomId);
        // If there is no local data
        if (Strings.isNullOrEmpty(localLastMessageId)) {
            ApiManager.doMessageList(context, entity.getId(), 50, "", "desc", false, false, new ApiListener<MessageListRequest.Resp>() {
                @Override
                public void onSuccess(MessageListRequest.Resp response) {
                    List<MessageEntity> items = response.getItems();
                    boolean hasNextPage = response.isHasNextPage();
                    resultDisposal(context, selfId, entity, items);
//                    EventBusUtils.sendEvent(new EventMsg(MsgConstant.CHANGE_LAST_MESSAGE, response.getRoomId()));
                    EventBusUtils.sendEvent(new EventMsg(MsgConstant.CHAT_ROOM_DATA_LOADING_PROGRESS_EVENT, 1));

                }

                @Override
                public void onFailed(String errorMessage) {
//                    EventBusUtils.sendEvent(new EventMsg(MsgConstant.CHANGE_LAST_MESSAGE, roomId));
                    EventBusUtils.sendEvent(new EventMsg(MsgConstant.CHAT_ROOM_DATA_LOADING_PROGRESS_EVENT, 1));
                    if (entity.getUnReadNum() > 0) {
                        boolean hasUnReadAtMe = MessageReference.findUnreadAtMessagesByRoomId(null, selfId, roomId);
                        if (hasUnReadAtMe) {
                            ChatRoomEntity entity = ChatRoomEntity.Build().id(roomId).isAtMe(hasUnReadAtMe).build();
                            EventBusUtils.sendEvent(new EventMsg(MsgConstant.OUTCROP_MENTION_UNREAD_ROOM, JsonHelper.getInstance().toJson(entity)));
                        }
                    }
                }
            });
            return;
        }

        // If there is data locally and it is LastMessage
        if (remoteLastMessageEntity.getId().equals(localLastMessageId)) {
            EventBusUtils.sendEvent(new EventMsg(MsgConstant.CHAT_ROOM_DATA_LOADING_PROGRESS_EVENT, 1));
            if (entity.getUnReadNum() > 0) {
                boolean hasUnReadAtMe = MessageReference.findUnreadAtMessagesByRoomId(null, selfId, roomId);
                if (hasUnReadAtMe) {
                    EventBusUtils.sendEvent(new EventMsg(MsgConstant.OUTCROP_MENTION_UNREAD_ROOM, JsonHelper.getInstance().toJson(ChatRoomEntity.Build().id(roomId).isAtMe(hasUnReadAtMe).build())));
                }
            }
            return;
        }

        MessageReference.save(entity.getId(), entity.getLastMessage());

        // If there is data locally and it is not LastMessage id to id
        ApiManager.doMessageListIdToId(context, false, entity.getId(), localLastMessageId, remoteLastMessageEntity.getId(), new ApiListener<MessageListRequest.Resp>() {
            @Override
            public void onSuccess(MessageListRequest.Resp response) {
                List<MessageEntity> items = response.getItems();
                resultDisposal(context, selfId, entity, items);
//                EventBusUtils.sendEvent(new EventMsg(MsgConstant.CHANGE_LAST_MESSAGE, response.getRoomId()));
                EventBusUtils.sendEvent(new EventMsg(MsgConstant.CHAT_ROOM_DATA_LOADING_PROGRESS_EVENT, 1));
            }

            @Override
            public void onFailed(String errorMessage) {
//                EventBusUtils.sendEvent(new EventMsg(MsgConstant.CHANGE_LAST_MESSAGE, roomId));
                EventBusUtils.sendEvent(new EventMsg(MsgConstant.CHAT_ROOM_DATA_LOADING_PROGRESS_EVENT, 1));
                if (entity.getUnReadNum() > 0) {
                    boolean hasUnReadAtMe = MessageReference.findUnreadAtMessagesByRoomId(null, selfId, roomId);
                    if (hasUnReadAtMe) {
                        ChatRoomEntity entity = ChatRoomEntity.Build().id(roomId).isAtMe(hasUnReadAtMe).build();
                        EventBusUtils.sendEvent(new EventMsg(MsgConstant.OUTCROP_MENTION_UNREAD_ROOM, JsonHelper.getInstance().toJson(entity)));
                    }
                }
            }
        });
    }

    private static void resultDisposal(Context context, String selfId, ChatRoomEntity roomEntity, List<MessageEntity> entities) {
        String roomId = roomEntity.getId();
        if (entities == null) {
            return;
        } else {
            List<String> receivedIds = Lists.newArrayList();
            for (MessageEntity entity : entities) {
                // If it is not (1) has arrived, (2) has been read, (3) is retracted, (-1) the message sent by oneself means that the message has arrived.
                if (MessageFlag.SEND.equals(entity.getFlag())) {
                    receivedIds.add(entity.getId());
                }

                String senderId = entity.getSenderId();
                if (senderId.equals(selfId)) {
                    if (entity.getSendNum() >= 0) {
                        entity.setStatus(MessageStatus.SUCCESS);
                    }
                    if (entity.getReceivedNum() > 0) {
                        entity.setStatus(MessageStatus.RECEIVED);
                    }
                    if (entity.getReadedNum() > 0) {
                        entity.setStatus(MessageStatus.READ);
                    }
                } else {
                    entity.setStatus(MessageStatus.READ);
                }
            }
            boolean status = MessageReference.saveByRoomId(roomId, entities);
            String currentRoomId = UserPref.getInstance(context).getCurrentRoomId();
            if (roomId.equals(currentRoomId)) {
                doMessageReadAllByRoomId(context, roomEntity, roomEntity.getUnReadNum(), null, false);
            } else {
                doMessageReceived(context, receivedIds);
            }
        }

        if (roomEntity.getUnReadNum() > 0) {
            boolean hasUnReadAtMe = MessageReference.findUnreadAtMessagesByRoomId(null, selfId, roomId);
            if (hasUnReadAtMe) {
                ChatRoomEntity entity = ChatRoomEntity.Build().id(roomId).isAtMe(hasUnReadAtMe).build();
                EventBusUtils.sendEvent(new EventMsg(MsgConstant.OUTCROP_MENTION_UNREAD_ROOM, JsonHelper.getInstance().toJson(entity)));
            }
        }
    }

    public static void messageListRequestRecursiveDesc(Context context, ChatRoomEntity entity, boolean isReceived, OnToolBarProgressListener listener) {
        String localLastMessageId = "";
        MessageEntity lastMessageEntity = MessageReference.findMessageByRoomIdAndStatusAndLimitOne(null, entity.getId(), MessageStatus.getLocalValidStatus(), MessageReference.Sort.DESC);

        if (lastMessageEntity != null) {
            localLastMessageId = lastMessageEntity.getId();
        }

        if (entity.getLastMessage() != null) {
            entity.getLastMessage().setStatus(MessageStatus.SUCCESS);
            MessageReference.save(entity.getId(), entity.getLastMessage());
        }

        // If the local data is the same as the remote data
        // There is a problem. If it is determined that there is already data in the local area and no longer fill in the message, the message change will cause the data surface error
        if (entity.getLastMessage() != null && entity.getLastMessage().getId().equals(localLastMessageId)) {
            if (listener != null) {
                listener.onToolProgress(entity.getId(), 1);
            }
            return;
        }

        if (entity.getLastMessage() != null && !Strings.isNullOrEmpty(entity.getLastMessage().getId())) {
            recursiveMessagesDesc(context, entity.getId(), entity.getLastMessage().getId(), Strings.isNullOrEmpty(localLastMessageId) ? "" : localLastMessageId, entity.getUnReadNum(), messageList -> {
                if (messageList == null) {
                    if (listener != null) {
                        listener.onToolProgress(entity.getId(), 1);
                    }
                    return;
                }
                List<String> newMsgIds = Lists.newArrayList();
                for (MessageEntity msg : messageList) {
                    newMsgIds.add(msg.getId());
                }
                if (isReceived) {
                    doMessageReceived(context, newMsgIds);
                } else {
                    doMessageReadAllByRoomId(context, entity, entity.getUnReadNum(), null, false);
                }
            }, listener);
        } else {
            if (listener != null) {
                listener.onToolProgress(entity.getId(), 1);
            }
        }
    }

    public static void recursiveMessagesDesc(Context context, String roomId, String lastMsgId, String localLastMessageId, int page, RefreshMsgCallBack refreshCallback, OnToolBarProgressListener progressListener) {
        ApiManager.doMessageList(context, roomId, page <= 0 ? 50 : page + 1, lastMsgId, "desc", false, true, new ApiListener<MessageListRequest.Resp>() {
            @Override
            public void onSuccess(MessageListRequest.Resp response) {
                List<MessageEntity> entities = response.getItems();
                if (entities.isEmpty()) {
                    refreshCallback.refreshFinish(Lists.newArrayList());
                    if (progressListener != null) {
                        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                            progressListener.onToolProgress(roomId, 1);
                        });
                    }
                    return;
                }

                Set<String> idSet = Sets.newHashSet();
                List<String> receivedIds = Lists.newArrayList();
                String userId = TokenPref.getInstance(context).getUserId();
                for (MessageEntity entity : entities) {
                    idSet.add(entity.getId());
                    // If it is not (1) has arrived, (2) has been read, (3) is retracted, (-1) the message sent by oneself means that the message has arrived.
                    if (MessageFlag.SEND.equals(entity.getFlag())) {
                        receivedIds.add(entity.getId());
                    }

                    String senderId = entity.getSenderId();
                    if (senderId.equals(userId)) {
                        if (entity.getSendNum() >= 0) {
                            entity.setStatus(MessageStatus.SUCCESS);
                        }
                        if (entity.getReceivedNum() > 0) {
                            entity.setStatus(MessageStatus.RECEIVED);
                        }
                        if (entity.getReadedNum() > 0) {
                            entity.setStatus(MessageStatus.READ);
                        }
                    } else {
                        entity.setStatus(MessageStatus.READ);
                    }
                }

                doMessageReceived(context, receivedIds);
                refreshCallback.refreshFinish(entities);

                //  If local message != null,
                //  resp messages contains local message,
                //  and the number of resp messages is greater than 1 (By Page 50)
                if (!Strings.isNullOrEmpty(localLastMessageId) && !idSet.contains(localLastMessageId) && entities.size() > 1) {
                    Collections.sort(entities);
                    String lastMessageId = entities.get(0).getId();
                    recursiveMessagesDesc(context, roomId, lastMessageId, localLastMessageId, 50, refreshCallback, progressListener);
                } else {
                    if (progressListener != null) {
                        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                            progressListener.onToolProgress(roomId, 1);
                        });
                    }
                }
            }

            @Override
            public void onFailed(String errorMessage) {
                refreshCallback.refreshFinish(Lists.newArrayList());
                if (progressListener != null) {
                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                        progressListener.onToolProgress(roomId, 1);
                    });
                }
            }
        });
    }

    /**
     * All read messages according to room id
     */
    public static void doMessageReadAllByRoomId(Context context, ChatRoomEntity entity, int unReadNum, List<String> messageIds, boolean needNotification) {
        if (entity == null) {
            return;
        }
        ApiManager.doMessagesRead(context, entity.getId(), messageIds, new ApiListener<String>() {
            @Override
            public void onSuccess(String s) {
                if (unReadNum > 0) {
                    ChatRoomReference.getInstance().updateInteractionTimeById(entity.getId());
                    entity.setUnReadNum(0);
                }
                MessageReference.updateReadFlagByRoomId(entity.getId());
                ChatRoomReference.getInstance().updateUnreadNumberById(entity.getId(), 0);
                CELog.d("all read reminders sent successfully");

                if (needNotification) {
                    EventBusUtils.sendEvent(new EventMsg(MsgConstant.SESSION_UPDATE_FILTER, JsonHelper.getInstance().toJson(entity)));
                }
            }

            @Override
            public void onFailed(String errorMessage) {
                CELog.d("failed to send all read reminders");
            }
        });
    }

    public static void doMessageReadAllByRoomId(Context context, String roomId, List<String> messageIds, boolean needNotification) {
        ChatRoomEntity entity = ChatRoomReference.getInstance().findById(roomId);
        if (entity != null) {
            doMessageReadAllByRoomId(context, entity, entity.getUnReadNum(), messageIds, needNotification);
        }
    }
}
