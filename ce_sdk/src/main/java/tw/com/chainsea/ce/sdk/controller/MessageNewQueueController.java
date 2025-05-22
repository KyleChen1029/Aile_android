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
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.bean.FacebookTag;
import tw.com.chainsea.ce.sdk.bean.InputLogBean;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.ce.sdk.bean.msg.MessageStatus;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
import tw.com.chainsea.ce.sdk.event.EventBusUtils;
import tw.com.chainsea.ce.sdk.event.EventMsg;
import tw.com.chainsea.ce.sdk.event.MsgConstant;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.database.sp.UserPref;
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference;
import tw.com.chainsea.ce.sdk.reference.MessageReference;
import tw.com.chainsea.ce.sdk.reference.UserProfileReference;
import tw.com.chainsea.ce.sdk.http.ce.request.MessageListRequest;
import tw.com.chainsea.ce.sdk.http.ce.request.UserItemRequest;
import tw.com.chainsea.ce.sdk.service.ChatRoomService;
import tw.com.chainsea.ce.sdk.service.RepairMessageService;
import tw.com.chainsea.android.common.log.CELog;

/**
 * current by evan on 11/16/20
 *
 * @author Evan Wang
 * date 11/16/20
 */
public class MessageNewQueueController extends AbsMessageQueueController {


    private static MessageNewQueueController INSTANCE;

    public static MessageNewQueueController getInstance() {
        if (INSTANCE == null) {
            synchronized (MessageNewQueueController.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MessageNewQueueController();
                }
            }
        }
        return INSTANCE;
    }

    private static final Queue<String> queue = Queues.newConcurrentLinkedQueue();
    private static final Multimap<String, MessageEntity> multimap = ArrayListMultimap.create();

//    private static boolean processing = false;
    private static long exeTime = System.currentTimeMillis();

//    private static final int CHECK_EXE_MSG = 7878;
//    private static final int POLLING_MSG = 7879;

//    private final Handler handler = new Handler(Looper.getMainLooper()) {
//
//        private WeakReference<Context> weakReference;
//
//
//        @Override
//        public void handleMessage(@NonNull Message msg) {
//            super.handleMessage(msg);
//            switch (msg.what) {
//                case CHECK_EXE_MSG:
//                    if (queue.isEmpty()) {
////                        processing = false;
//                    } else {
//                        execute();
//                    }
//                    break;
//                case POLLING_MSG:
//                    if (queue.isEmpty()) {
////                        processing = false;
//                    } else {
//                        execute();
//                    }
//                    handler.removeMessages(CHECK_EXE_MSG);
//                    handler.sendEmptyMessageDelayed(CHECK_EXE_MSG, DELAY_INTERVAL * 10);
//                    break;
//            }
//
//        }
//    };


    public synchronized void setQueue(Context context, String roomId, MessageEntity entity) {
//        mContext = context;
        queue.remove(roomId);
        queue.add(roomId);
        multimap.put(roomId, entity);
        execute(context);
    }

    public synchronized void execute(Context context) {
        long now = System.currentTimeMillis();
        CELog.w("MessageNewQueueController.execute() exeTime :: " + now + ", interval:: " + ((now - exeTime) / 1000.0d));
        exeTime = now;

        String roomId = queue.remove();
        CELog.w("MessageNewQueueController.execute() " + roomId + "----" + System.currentTimeMillis());


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
            handling(context, selfId, roomId, entitiesTree);
        }

    }

    /**
     * if user profile is no data locally
     */
    private synchronized void handleStranger(Context context, Queue<String> senderIdQueue, String selfId, String roomId, TreeSet<MessageEntity> entitiesTree) {
        CELog.w("MessageNewQueueController.handleStranger() " + roomId);
        if (senderIdQueue.isEmpty()) {
            handling(context, selfId, roomId, entitiesTree);
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

    private synchronized void handling(Context context, String selfId, String roomId, TreeSet<MessageEntity> entitiesTree) {

        if (entitiesTree.isEmpty()) {
            return;
        }

        String lastId = entitiesTree.last().getId();
        String localLastMessageId = MessageReference.findIdByRoomIdAndSotFormLimitOne(null, roomId, MessageReference.Sort.DESC);
        if (!Strings.isNullOrEmpty(localLastMessageId) && lastId.equals(localLastMessageId)) {
//            String currentRoomId = UserPref.getInstance(context).getCurrentRoomId();
            if (UserPref.getInstance(context).isInCurrentRoomId(roomId)) {
                EventBusUtils.sendEvent(new EventMsg(MsgConstant.NOTICE_APPEND_NEW_MESSAGE_IDS, JsonHelper.getInstance().toJson(getMessageIds(entitiesTree))));
                handlingMessageByRemote(context, roomId, getMessageIds(entitiesTree));
            }
        } else {
            handlingChatRoomByRemote(context, selfId, roomId, entitiesTree);
        }
    }

    private synchronized void handlingChatRoomByRemote(Context context, String selfId, String roomId, TreeSet<MessageEntity> entitiesTree) {
        CELog.w("MessageNewQueueController.handlingChatRooms() " + roomId);
        ApiManager.doRoomItemInIoThread(context, roomId, selfId, new ApiListener<ChatRoomEntity>() {
            @Override
            public void onSuccess(ChatRoomEntity entity) {
                // 如果是廣播聊天室
                if (ChatRoomType.broadcast.equals(entity.getType())) {
                    RepairMessageService.setQueue(context, entity.getId(), getMessageIds(entitiesTree), false);
                    return;
                }

                if (entity.isDeleted() || (entity.getLastMessage() != null && entity.getDfrTime() > entity.getLastMessage().getSendTime())) {
                    ChatRoomReference.getInstance().deleteById(entity.getId());
                    return;
                }

                // message/list id to id
                String srcMessageId = MessageReference.findIdByRoomIdAndSotFormLimitOne(null, entity.getId(), MessageReference.Sort.DESC);
                if (Strings.isNullOrEmpty(srcMessageId)) {
                    srcMessageId = entitiesTree.first().getId();
                }

                String destMessageId = "";
                if (entity.getLastMessage() != null) {
                    destMessageId = entity.getLastMessage().getId();
                    MessageReference.save(null, entity);
                } else {
                    destMessageId = entitiesTree.last().getId();
                }

                boolean isNewRoom = ChatRoomReference.getInstance().hasLocalData(roomId);
                handlingMessagesByRemote(context, roomId, srcMessageId, destMessageId, getMessageIds(entitiesTree), !isNewRoom);

                entity.setUpdateTime(System.currentTimeMillis());

                String unfinishedEdited = ChatRoomReference.getInstance().getUnfinishedEdited(roomId);
                InputLogBean localBean = InputLogBean.from(unfinishedEdited);
                if(!Strings.isNullOrEmpty(localBean.getText())) {
                    entity.setUnfinishedEdited(unfinishedEdited);
                }
                //save room & save last message
                ChatRoomReference.getInstance().save(entity);

                String ringName = getRingName(entitiesTree);

                // EVAN_FLAG 11/16/20 confirm Whether The Ringing Logic
                EventBusUtils.sendEvent(new EventMsg(MsgConstant.NOTICE_PLAY_NOTIFY_TONE, ringName));

                // EVAN_FLAG 11/16/20 display to chat room list UI
                ChatRoomEntity localEntity = ChatRoomReference.getInstance().findById2( TokenPref.getInstance(context).getUserId(), roomId, true, true, true, true, true);
                EventBusUtils.sendEvent(new EventMsg(MsgConstant.REFRESH_ROOM_BY_LOCAL, JsonHelper.getInstance().toJson(localEntity)));

                ChatRoomService.getInstance().getBadge(context, roomId);
            }

            @Override
            public void onFailed(String errorMessage) {
                CELog.e(errorMessage);
                if(Objects.equals(errorMessage, "聊天室已被刪除") || Objects.equals(errorMessage, "你已不是聊天室成員")) {
                    ChatRoomReference.getInstance().deleteById(roomId);
                }
            }
        });
    }

    private synchronized void handlingMessagesByRemote(Context context, String roomId, String firstMessageId, String lastMessageId, Set<String> messageIds, boolean isNewRoom) {
        CELog.w("MessageNewQueueController.handlingMessages() ");
        if (isNewRoom) {
            ApiManager.doMessageList(context, roomId, 50, "", "desc", false, false, new ApiListener<MessageListRequest.Resp>() {
                @Override
                public void onSuccess(MessageListRequest.Resp response) {
                    List<MessageEntity> items = response.getItems();
                    CELog.d(String.format("handlingMessages isNewRoom :: <-- roomId =%s, src=%s, dest=%s , size=%s", response.getRoomId(), firstMessageId, lastMessageId, items.size()));
//                    boolean ackEnable = TokenPref.getInstance(context).isSocketAckEnable();
                    for (MessageEntity m : items) {
                        messageIds.add(m.getId());
                        m.setStatus(MessageStatus.IS_REMOTE);
                    }
                    MessageReference.saveByRoomId(response.getRoomId(), items);
                    handlingMessageByRemote(context, response.getRoomId(), messageIds);
                    String currentConsultationRoomId = UserPref.getInstance(context).getCurrentConsultationRoomId();
//                    String currentRoomId = UserPref.getInstance(context).getCurrentRoomId();
                    boolean isInCurrentRoomId = UserPref.getInstance(context).isInCurrentRoomId(response.getRoomId());
                    if (isInCurrentRoomId) {
                        EventBusUtils.sendEvent(new EventMsg(MsgConstant.NOTICE_APPEND_NEW_MESSAGE_IDS, JsonHelper.getInstance().toJson(messageIds)));
                        EventBusUtils.sendEvent(new EventMsg(MsgConstant.NOTICE_NEW_MESSAGE_CHECKING_SEVICENUMBER_SERVICED, response.getRoomId()));
                    }

                    if (response.getRoomId().equals(currentConsultationRoomId)) {
                        EventBusUtils.sendEvent(new EventMsg(MsgConstant.NOTICE_APPEND_CONSULTATION_NEW_MESSAGE_IDS, JsonHelper.getInstance().toJson(messageIds)));
                    }

//                    if (!ackEnable) {
                        if (isInCurrentRoomId || currentConsultationRoomId.contains(response.getRoomId())) {
                            ApiManager.doMessagesRead(context, response.getRoomId(), null, new ApiListener<String>() {
                                @Override
                                public void onSuccess(String s) {
                                    ChatRoomReference.getInstance().updateInteractionTimeById(response.getRoomId());
                                    ChatRoomEntity localEntity = ChatRoomReference.getInstance().findById2( TokenPref.getInstance(context).getUserId(), response.getRoomId(), true, true, true, true, true);
                                    EventBusUtils.sendEvent(new EventMsg(MsgConstant.REFRESH_ROOM_BY_LOCAL, JsonHelper.getInstance().toJson(localEntity)));
                                }

                                @Override
                                public void onFailed(String errorMessage) {
                                    CELog.e(errorMessage);
                                }
                            });
                        } else {
                            ApiManager.doMessagesReceived(context, Lists.newArrayList(getMessageIds(items)), null);
                        }
//                    }
                }

                @Override
                public void onFailed(String errorMessage) {

                }
            });
        } else {
            if (lastMessageId.equals(firstMessageId)) {
//                String currentRoomId = UserPref.getInstance(context).getCurrentRoomId();
                if (UserPref.getInstance(context).isInCurrentRoomId(roomId)) {
                    EventBusUtils.sendEvent(new EventMsg(MsgConstant.NOTICE_APPEND_NEW_MESSAGE_IDS, JsonHelper.getInstance().toJson(messageIds)));
                    EventBusUtils.sendEvent(new EventMsg(MsgConstant.NOTICE_NEW_MESSAGE_CHECKING_SEVICENUMBER_SERVICED, roomId));
                }
                CELog.d("already Filled");
                handlingMessageByRemote(context, roomId, messageIds);
                return;
            }

            CELog.d(String.format("handlingMessages:: --> roomId =%s, src=%s, dest=%s , size=%s", roomId, firstMessageId, lastMessageId, messageIds.size()));
            ApiManager.doMessageListIdToId(context, false, roomId, firstMessageId, lastMessageId, new ApiListener<MessageListRequest.Resp>() {
                @Override
                public void onSuccess(MessageListRequest.Resp response) {
                    List<MessageEntity> items = response.getItems();
                    CELog.d(String.format("handlingMessages:: <-- roomId =%s, src=%s, dest=%s , size=%s", response.getRoomId(), firstMessageId, lastMessageId, items.size()));
//                    boolean ackEnable = TokenPref.getInstance(context).isSocketAckEnable();
                    for (MessageEntity m : items) {
                        // 判斷是否是 Facebook 回覆
                        if (m.getTag() != null) {
                            FacebookTag facebookTag = JsonHelper.getInstance().from(m.getTag(), FacebookTag.class);
                            if (facebookTag.getData().getReplyType() != null && !facebookTag.getData().getReplyType().isEmpty()) {
                                List<MessageEntity> roomMessageList = MessageReference.findByRoomId(roomId);
                                for (MessageEntity roomMessage : roomMessageList) {
                                    if (m.getId().equals(roomMessage.getId())) continue;
                                    if (roomMessage.getTag() != null && !roomMessage.getTag().isEmpty()) {
                                        FacebookTag roomFacebookTag = JsonHelper.getInstance().from(roomMessage.getTag(), FacebookTag.class);
                                        if (roomFacebookTag.getData() == null) continue;
                                        if (roomFacebookTag.getData().getCommentId() == null) continue;
                                        if (roomFacebookTag.getData().getCommentId().equals(facebookTag.getData().getCommentId())
                                                && (roomFacebookTag.getData().getReplyType() == null ||roomFacebookTag.getData().getReplyType().isEmpty())) {
                                            m.setThemeId(roomMessage.getId());
                                            m.setNearMessageId(roomMessage.getId());
                                            m.setNearMessageType(roomMessage.getType());
                                            m.setNearMessageAvatarId(roomMessage.getAvatarId());
                                            m.setNearMessageContent(roomMessage.getContent());
                                            m.setNearMessageSenderId(roomMessage.getSenderId());
                                            m.setNearMessageSenderName(roomMessage.getSenderName());
                                            // 私訊回覆只能一次 要紀錄到 DB
                                            if ("private".equals(facebookTag.getData().getReplyType())) {
                                                MessageReference.updateFacebookPrivateReplyStatus(roomId, roomMessage.getId(), true);
                                            }
                                            MessageReference.save(response.getRoomId(), m);
                                        }
                                    }
                                }
                            }
                        }
                        messageIds.add(m.getId());
                        m.setStatus(MessageStatus.IS_REMOTE);
                    }

                    MessageReference.saveByRoomId(response.getRoomId(), items);
                    handlingMessageByRemote(context, response.getRoomId(), messageIds);
                    messageIds.remove(firstMessageId);
                    String currentConsultationRoomId = UserPref.getInstance(context).getCurrentConsultationRoomId();
                    boolean isInCurrentRoomId = UserPref.getInstance(context).isInCurrentRoomId(response.getRoomId());
                    if (isInCurrentRoomId) {
                        EventBusUtils.sendEvent(new EventMsg(MsgConstant.NOTICE_APPEND_NEW_MESSAGE_IDS, JsonHelper.getInstance().toJson(messageIds)));
                        EventBusUtils.sendEvent(new EventMsg(MsgConstant.NOTICE_NEW_MESSAGE_CHECKING_SEVICENUMBER_SERVICED, response.getRoomId()));
                    }

                    if (response.getRoomId().equals(currentConsultationRoomId)) {
                        EventBusUtils.sendEvent(new EventMsg(MsgConstant.NOTICE_APPEND_CONSULTATION_NEW_MESSAGE_IDS, JsonHelper.getInstance().toJson(messageIds)));
                    }

//                    if (!ackEnable) {
                        if (isInCurrentRoomId || currentConsultationRoomId.contains(response.getRoomId())) {
                            ApiManager.doMessagesRead(context, response.getRoomId(), null, new ApiListener<String>() {
                                @Override
                                public void onSuccess(String s) {
                                    ChatRoomReference.getInstance().updateInteractionTimeById(response.getRoomId());
                                    ChatRoomEntity localEntity = ChatRoomReference.getInstance().findById2( TokenPref.getInstance(context).getUserId(), response.getRoomId(), true, true, true, true, true);
                                    EventBusUtils.sendEvent(new EventMsg(MsgConstant.REFRESH_ROOM_BY_LOCAL, JsonHelper.getInstance().toJson(localEntity)));
                                }

                                @Override
                                public void onFailed(String errorMessage) {

                                }
                            });
                        } else {
                            ApiManager.doMessagesReceived(context, Lists.newArrayList(getMessageIds(items)), null);
                        }


//                        if (!response.getRoomId().equals(currentRoomId)) {
//                            ApiManager.doMessagesReceived(mContext, Lists.newArrayList(getMessageIds(items)), new ApiListener<String>() {
//                                @Override
//                                public void onSuccess(String s) {
//
//                                }
//
//                                @Override
//                                public void onFailed(String errorMessage) {
//
//                                }
//                            });
//                        } else {
//                            ApiManager.doMessagesRead(mContext, response.getRoomId(), null, new ApiListener<String>() {
//                                @Override
//                                public void onSuccess(String s) {
//                                    ChatRoomReference.getInstance().updateInteractionTimeById(response.getRoomId());
//                                    ChatRoomEntity localEntity = ChatRoomReference.getInstance().findById2( TokenPref.getInstance(mContext).getUserId(), response.getRoomId(), true, true, true, true, true);
//                                    EventBusUtils.sendEvent(new EventMsg(MsgConstant.REFRESH_ROOM_BY_LOCAL, JsonHelper.getInstance().toJson(localEntity)));
//                                }
//
//                                @Override
//                                public void onFailed(String errorMessage) {
//
//                                }
//                            });
//                        }
//                    }
                }

                @Override
                public void onFailed(String errorMessage) {

                }
            });
        }
    }


//    private void setPostDelay() {
//        handler.removeMessages(POLLING_MSG);
//        handler.sendEmptyMessageDelayed(POLLING_MSG, DELAY_INTERVAL);
//    }


}
