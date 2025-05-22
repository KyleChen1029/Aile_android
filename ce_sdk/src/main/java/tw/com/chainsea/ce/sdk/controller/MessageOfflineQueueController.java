package tw.com.chainsea.ce.sdk.controller;

import android.content.Context;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.msg.MessageStatus;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.database.sp.UserPref;
import tw.com.chainsea.ce.sdk.event.EventBusUtils;
import tw.com.chainsea.ce.sdk.event.EventMsg;
import tw.com.chainsea.ce.sdk.event.MsgConstant;
import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.http.ce.request.MessageListRequest;
import tw.com.chainsea.ce.sdk.http.ce.request.UserItemRequest;
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference;
import tw.com.chainsea.ce.sdk.reference.MessageReference;
import tw.com.chainsea.ce.sdk.reference.UserProfileReference;
import tw.com.chainsea.ce.sdk.service.ChatRoomService;

/**
 * current by evan on 2020-11-13
 *
 * @author Evan Wang
 * @date 2020-11-13
 */
public class MessageOfflineQueueController extends AbsMessageQueueController {

    private static Queue<String> queue = Queues.newConcurrentLinkedQueue();
    private static Multimap<String, MessageEntity> multimap = ArrayListMultimap.create();

    private static MessageOfflineQueueController INSTANCE;

    public static MessageOfflineQueueController getInstance() {
        if (INSTANCE == null) {
            synchronized (MessageOfflineQueueController.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MessageOfflineQueueController();

                }
            }
        }
        return INSTANCE;
    }

    public synchronized void setQueue(Context context, String roomId, Set<MessageEntity> entities) {
//        init();
//        mContext = context;
        queue.remove(roomId);
        queue.add(roomId);
        multimap.putAll(roomId, entities);
        execute(context);
    }

    public synchronized void execute(Context context) {
        String roomId = queue.remove();
        TreeSet<MessageEntity> entitiesTree = Sets.newTreeSet((o1, o2) -> ComparisonChain.start()
            .compare(o1.getSendTime(), o2.getSendTime() + 0.5)
            .result());
        entitiesTree.addAll(multimap.removeAll(roomId));

        String selfId = TokenPref.getInstance(context).getUserId();
        Set<String> sends = getSendsIds(selfId, entitiesTree);
        sends.remove(selfId);

        Iterator<String> sendIterator = sends.iterator();
        while (sendIterator.hasNext()) {
            if (UserProfileReference.hasLocalData(null, sendIterator.next())) {
                sendIterator.remove();
            }
        }

        if (!sends.isEmpty()) {
            handleStranger(context, Queues.newLinkedBlockingDeque(sends), selfId, roomId, entitiesTree);
        } else {
            handlingChatRooms(context, selfId, roomId, entitiesTree);
        }
    }

    /**
     * if user profile is no data locally
     *
     * @param senderIdQueue
     * @param selfId
     * @param roomId
     */
    private synchronized void handleStranger(Context context, Queue<String> senderIdQueue, String selfId, String roomId, TreeSet<MessageEntity> entitiesTree) {
        if (senderIdQueue.isEmpty()) {
            handlingChatRooms(context, selfId, roomId, entitiesTree);
            return;
        }

        String senderId = senderIdQueue.remove();
        ApiManager.getInstance().doUserItem2(context, senderId, new UserItemRequest.Listener() {
            @Override
            public void onSuccess(UserProfileEntity account) {
                DBManager.getInstance().insertFriends(account);
                handleStranger(context, senderIdQueue, selfId, roomId, entitiesTree);
            }

            @Override
            public void onFailed(String errorMessage) {
                handleStranger(context, senderIdQueue, selfId, roomId, entitiesTree);
            }
        });
    }

    /**
     * message/list id to di
     * offline last messageId to local last messageId{ApiManager.doMessageList()}
     * is chat room has local data ,don't refresh unread number to UI
     * is chat room has local ,need refresh chat room list to UI
     *
     * @param selfId
     * @param roomId
     */
    private synchronized void handlingChatRooms(Context context, String selfId, String roomId, TreeSet<MessageEntity> entitiesTree) {

        MessageEntity first = entitiesTree.first();
        MessageEntity last = entitiesTree.last();
        boolean hasChatRoomEntity = ChatRoomReference.getInstance().hasLocalData(roomId);
        if (hasChatRoomEntity) {
            long localLastSendTime = MessageReference.findSendTimeByRoomIdAndSotFormLimitOne(null, roomId, MessageReference.Sort.DESC);
            if (last.getSendTime() > localLastSendTime) {
                handlingMessagesByRemote(context, true, roomId, first.getId(), last.getId(), getMessageIds(entitiesTree), false);
            }
        } else {
            ApiManager.doRoomItemInIoThread(context, roomId, selfId, new ApiListener<ChatRoomEntity>() {
                @Override
                public void onSuccess(ChatRoomEntity entity) {
                    // is delete room
                    if (entity.getDfrTime() > 0L) {
//                        if (entity.getDfrTime() > 0L && ChatRoomType.FRIEND.equals(entity.getType())) {
                        boolean deleteStatus = ChatRoomReference.getInstance().deleteById(entity.getId());
                        ChatRoomService.getInstance().getBadge(context);
                        return;
                    }

                    // message/list id to id
                    String srcMessageId = MessageReference.findIdByRoomIdAndSotFormLimitOne(null, entity.getId(), MessageReference.Sort.DESC);
                    if (Strings.isNullOrEmpty(srcMessageId)) {
                        srcMessageId = first.getId();
                    }

                    String destMessageId;
                    if (entity.getLastMessage() == null) {
                        destMessageId = last.getId();
                    } else {
                        destMessageId = entity.getLastMessage().getId();
                        MessageReference.save(null, entity);
                    }

                    //save room & save last message
                    ChatRoomReference.getInstance().save(entity);

                    // EVAN_FLAG 11/16/20 display to chat room list UI
                    ChatRoomEntity localEntity = ChatRoomReference.getInstance().findById2(TokenPref.getInstance(context).getUserId(), roomId, true, true, true, true, true);
                    EventBusUtils.sendEvent(new EventMsg(MsgConstant.REFRESH_ROOM_BY_LOCAL, JsonHelper.getInstance().toJson(localEntity)));

                    handlingMessagesByRemote(context, false, roomId, srcMessageId, destMessageId, getMessageIds(entitiesTree), true);

                    ChatRoomService.getInstance().getBadge(context);
                }

                @Override
                public void onFailed(String errorMessage) {
                    if (Objects.equals(errorMessage, "聊天室已被刪除") || Objects.equals(errorMessage, "你已不是聊天室成員")) {
                        ChatRoomReference.getInstance().deleteById(roomId);
                    }
                }
            });
        }
    }

    private synchronized void handlingMessagesByRemote(Context context, boolean hasChatRoomEntity, String roomId, String firstMessageId, String lastMessageId, Set<String> messageIds, boolean isNewRoom) {
        if (isNewRoom) {
            ApiManager.doMessageList(context, roomId, 50, "", "desc", false, false, new ApiListener<MessageListRequest.Resp>() {
                @Override
                public void onSuccess(MessageListRequest.Resp response) {
                    List<MessageEntity> items = response.getItems();
                    MessageReference.saveByRoomId(roomId, items);
                    for (MessageEntity m : items) {
                        messageIds.add(m.getId());
                        m.setStatus(MessageStatus.IS_REMOTE);
                    }

                    handlingMessageByRemote(context, roomId, messageIds);
                    EventBusUtils.sendEvent(new EventMsg(MsgConstant.NOTICE_APPEND_OFFLINE_MESSAGE_IDS, JsonHelper.getInstance().toJson(messageIds)));
                    String currentRoomId = UserPref.getInstance(context).getCurrentRoomId();
                    if (!roomId.equals(currentRoomId)) {
                        ApiManager.doMessagesReceived(context, Lists.newArrayList(getMessageIds(items)), null);
                    } else {
                        ApiManager.doMessagesRead(context, roomId, null, null);
                    }
                }

                @Override
                public void onFailed(String errorMessage) {

                }
            });
        } else {
            ApiManager.doMessageListIdToId(context, true, roomId, firstMessageId, lastMessageId, new ApiListener<MessageListRequest.Resp>() {
                @Override
                public void onSuccess(MessageListRequest.Resp response) {
                    List<MessageEntity> items = response.getItems();
                    MessageReference.saveByRoomId(roomId, items);
                    if (hasChatRoomEntity) {
                        // is current chat room
                        for (MessageEntity m : items) {
                            messageIds.add(m.getId());
                        }

                        handlingMessageByRemote(context, roomId, messageIds);

                        boolean isInCurrentRoom = UserPref.getInstance(context).isInCurrentRoomId(roomId);
                        if (!isInCurrentRoom) {
                            // 依照聊天室 id 計算 message count
                            Map<String, Long> map = items.stream().collect(Collectors.groupingBy(MessageEntity::getRoomId, Collectors.counting()));
                            map.forEach((roomId, count) -> {
                                ChatRoomReference.getInstance().addUnreadCount(roomId, count.intValue());
                            });

                            ApiManager.doMessagesReceived(context, Lists.newArrayList(getMessageIds(items)), null);
                        } else {
                            ApiManager.doMessagesRead(context, roomId, null, null);
                        }
                        EventBusUtils.sendEvent(new EventMsg(MsgConstant.NOTICE_APPEND_OFFLINE_MESSAGE_IDS, JsonHelper.getInstance().toJson(messageIds)));
                    }
                }

                @Override
                public void onFailed(String errorMessage) {

                }
            });
        }
    }

}
