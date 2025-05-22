package tw.com.chainsea.chat.lib;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tw.com.chainsea.chat.util.DaVinci;
import io.socket.client.Ack;
import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.ui.UiHelper;
import tw.com.chainsea.ce.sdk.base.MsgBuilder;
import tw.com.chainsea.ce.sdk.bean.BadgeUpgradeBean;
import tw.com.chainsea.ce.sdk.bean.CrowdEntity;
import tw.com.chainsea.ce.sdk.bean.FacebookTag;
import tw.com.chainsea.ce.sdk.bean.GroupRefreshBean;
import tw.com.chainsea.ce.sdk.bean.GroupUpgradeBean;
import tw.com.chainsea.ce.sdk.bean.MsgNoticeBean;
import tw.com.chainsea.ce.sdk.bean.MsgStatusBean;
import tw.com.chainsea.ce.sdk.bean.PicSize;
import tw.com.chainsea.ce.sdk.bean.ProcessStatus;
import tw.com.chainsea.ce.sdk.bean.UpdateAvatarBean;
import tw.com.chainsea.ce.sdk.bean.UserExitBean;
import tw.com.chainsea.ce.sdk.bean.account.AccountType;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.broadcast.BroadcastEvent;
import tw.com.chainsea.ce.sdk.bean.business.BusinessCode;
import tw.com.chainsea.ce.sdk.bean.msg.BroadcastFlag;
import tw.com.chainsea.ce.sdk.bean.msg.MessageFlag;
import tw.com.chainsea.ce.sdk.bean.msg.MessageStatus;
import tw.com.chainsea.ce.sdk.bean.msg.MessageType;
import tw.com.chainsea.ce.sdk.bean.msg.content.BusinessContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.CallContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.ImageContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.VideoContent;
import tw.com.chainsea.ce.sdk.bean.response.AileTokenApply;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
import tw.com.chainsea.ce.sdk.bean.room.QuickReplySocket;
import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberType;
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity;
import tw.com.chainsea.ce.sdk.bean.todo.TodoEntity;
import tw.com.chainsea.ce.sdk.bean.todo.TodoStatus;
import tw.com.chainsea.ce.sdk.config.AppConfig;
import tw.com.chainsea.ce.sdk.controller.MessageNewQueueController;
import tw.com.chainsea.ce.sdk.controller.MessageOfflineQueueController;
import tw.com.chainsea.ce.sdk.controller.SyncReadBatchUpdateController;
import tw.com.chainsea.ce.sdk.database.DBContract;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.database.sp.UserPref;
import tw.com.chainsea.ce.sdk.event.EventBusUtils;
import tw.com.chainsea.ce.sdk.event.EventMsg;
import tw.com.chainsea.ce.sdk.event.MsgConstant;
import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.http.ce.request.MessageSendRequest;
import tw.com.chainsea.ce.sdk.http.ce.request.UploadManager;
import tw.com.chainsea.ce.sdk.http.ce.request.UserItemRequest;
import tw.com.chainsea.ce.sdk.http.ce.request.UserSearchRequest;
import tw.com.chainsea.ce.sdk.reference.AccountRoomRelReference;
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference;
import tw.com.chainsea.ce.sdk.reference.MessageReference;
import tw.com.chainsea.ce.sdk.reference.TodoReference;
import tw.com.chainsea.ce.sdk.service.AvatarService;
import tw.com.chainsea.ce.sdk.service.ChatMessageService;
import tw.com.chainsea.ce.sdk.service.ChatRoomService;
import tw.com.chainsea.ce.sdk.service.ChatServiceNumberService;
import tw.com.chainsea.ce.sdk.service.RepairMessageService;
import tw.com.chainsea.ce.sdk.service.TodoService;
import tw.com.chainsea.ce.sdk.service.listener.ApiCallback;
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
import tw.com.chainsea.ce.sdk.socket.ce.SocketManager;
import tw.com.chainsea.ce.sdk.socket.ce.code.NoticeCode;
import tw.com.chainsea.chat.App;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.base.Constant;
import tw.com.chainsea.chat.config.Const;
import tw.com.chainsea.chat.databinding.FloatWindowTransferAlertBinding;
import tw.com.chainsea.chat.service.ActivityTransitionsControl;
import tw.com.chainsea.chat.service.fcm.FCMTokenManager;
import tw.com.chainsea.chat.ui.activity.ChatActivity;
import tw.com.chainsea.chat.util.IntentUtil;

/**
 * ChatService
 * Created by 90Chris on 2016/6/8.
 */
public class ChatService {

    private static final String TAG = ChatService.class.getSimpleName();

    private static ChatService INSTANCE;
    private String token;
    private SoundPool soundPool;
    private int messageSound;

    public static ChatService getInstance() {
        if (INSTANCE == null) {
            synchronized (ChatService.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ChatService();
                }
            }
        }
        return INSTANCE;
    }

    ChatService() {
        EventBusUtils.register(this);
    }

    public void connect(Context context) {
        token = TokenPref.getInstance(context).getTokenId();

        String url = TokenPref.getInstance(context).getSocketIoUrl();
        String namespace = TokenPref.getInstance(context).getSocketIoNameSpace();

        String query = TokenPref.getInstance(App.getContext()).getSocketQuery();

        if (token != null) {
            AileTokenApply.ConnectType type = TokenPref.getInstance(context).getConnectType();
            if (AileTokenApply.ConnectType.SOCKET_IO.equals(type)) {
                ApiManager.startSocket(url, namespace, query, new SocketOnNoticeListener());
            }
//            else {
//                ApiManager.getInstance().startJocket(context, new JocketOnNoticeListener(), token);
//            }
            setDaVinciToken();
//            initFireBaseCloudMessageToken();
            FCMTokenManager.INSTANCE.refreshFCMTokenIdToRemote(context);
//            ThreadExecutorHelper.getHandlerExecutor().postSticker(fcmTokenRun);
        }
    }


    private void setDaVinciToken() {
        JSONObject requestJson = new JSONObject();
        if ((token != null) && (!token.isEmpty())) {
            JSONObject header = new JSONObject();
            try {
                header.put("tokenId", token);
                requestJson.put("_header_", header);
            } catch (JSONException e) {
                CELog.e("construct header failed", e);
            }
        }
        DaVinci.with().getImageLoader().gBody(requestJson.toString());
    }

    public void close() {
        ApiManager.getInstance().closeSocket();
    }

    public void doMessageReceived(List<String> messageIds) {
        ApiManager.doMessagesReceived(App.getContext(), messageIds, new ApiListener<String>() {
            @Override
            public void onSuccess(String s) {
                CELog.d("Reminder sent successfully");
            }

            @Override
            public void onFailed(String errorMessage) {
                CELog.e("Failed to send reminder");
            }
        });
    }

    /**
     * 提交訊息
     */
    public void sendMessage(String roomId, String messageId, MessageType type, String content, String themeId) {
        ApiManager.doMessageSend(App.getContext(), roomId, messageId, themeId, new MsgBuilder(type).content(content), new SendMsgListener());
    }

    /**
     * 提交主題訊息
     */
    public void sendReplyMessage(String roomId, String messageId, MessageType type, String content, String themeId, ReplyMessageListener replyMessageListener) {
        ApiManager.doMessageReply(App.getContext(), roomId, messageId, themeId, new MsgBuilder(type).content(content), replyMessageListener);
    }

    /**
     * 提交主題訊息
     */
    public void sendReplyMessage(String roomId, String messageId, MessageType type, String content, String themeId) {
        ApiManager.doMessageReply(App.getContext(), roomId, messageId, themeId, new MsgBuilder(type).content(content), new ReplyMessageListener());
    }

    /**
     * 提交檔案訊息
     */
    public void sendFileMessage(String roomId, String messageId, String name, int size, String androidLocalPath, String url, String themeId, String md5) {
        ApiManager.doMessageSend(App.getContext(), roomId, messageId, themeId,
            new MsgBuilder(MessageType.FILE)
                .name(name)
                .size(size)
                .md5(md5)
                .androidLocalPath(androidLocalPath)
                .url(url)
            , new SendMsgListener());
    }

    /**
     * 提交檔案主題訊息
     */
    public void sendReplyFileMessage(String roomId, String messageId, String name, int size, String url, String themeId, String md5) {
        ApiManager.doMessageReply(App.getContext(), roomId, messageId, themeId,
            new MsgBuilder(MessageType.FILE)
                .name(name)
                .md5(md5)
                .size(size)
                .url(url)
            , new ReplyMessageListener());
    }

    /**
     * 提交圖片訊息
     */
    public void sendImageMessage(String roomId, String messageId, String themeId, UploadManager.FileEntity entity) {
        ApiManager.doMessageSend(App.getContext(), roomId, messageId, themeId,
            new MsgBuilder(MessageType.IMAGE)
                .url(entity.getUrl())
                .size(entity.getSize())
                .width(entity.getWidth())
                .height(entity.getHeight())
                .md5(entity.getMD5())
                .thumbnailUrl(entity.getThumbnailUrl())
                .thumbnailSize(entity.getThumbnailSize())
                .thumbnailWidth(entity.getThumbnailWidth())
                .thumbnailHeight(entity.getThumbnailHeight())
                .name("pic")
            , new SendMsgListener());
    }

    /**
     * 提交圖片主題訊息
     */
    public void sendReplyImageMessage(String roomId, String messageId, String themeId, UploadManager.FileEntity entity) {
        ApiManager.doMessageReply(App.getContext(), roomId, messageId, themeId,
            new MsgBuilder(MessageType.IMAGE)
                .url(entity.getUrl())
                .size(entity.getSize())
                .name("pic")
                .md5(entity.getMD5())
                .width(entity.getWidth())
                .height(entity.getHeight())
                .thumbnailUrl(entity.getThumbnailUrl())
                .thumbnailSize(entity.getThumbnailSize())
                .thumbnailWidth(entity.getThumbnailWidth())
                .thumbnailHeight(entity.getThumbnailHeight())
            , new ReplyMessageListener());
    }

    /**
     * 提交圖片主題訊息
     */
    public void sendReplyImageMessage(String roomId, String messageId, String themeId, ImageContent entity) {
        ApiManager.doMessageReply(App.getContext(), roomId, messageId, themeId,
                new MsgBuilder(MessageType.IMAGE)
                        .url(entity.getUrl())
                        .size(entity.getSize())
                        .name("pic")
                        .md5(entity.getMD5())
                        .width(entity.getWidth())
                        .height(entity.getHeight())
                        .thumbnailUrl(entity.getThumbnailUrl())
                        .thumbnailSize(entity.getThumbnailSize())
                        .thumbnailWidth(entity.getThumbnailWidth())
                        .thumbnailHeight(entity.getThumbnailHeight())
                , new ReplyMessageListener());
    }

    /**
     * 提交圖片訊息
     */
    public void sendImageMessage(String roomId, String messageId, String themeId, ImageContent entity) {
        ApiManager.doMessageSend(App.getContext(), roomId, messageId, themeId,
                new MsgBuilder(MessageType.IMAGE)
                        .url(entity.getUrl())
                        .size(entity.getSize())
                        .width(entity.getWidth())
                        .height(entity.getHeight())
                        .md5(entity.getMD5())
                        .thumbnailUrl(entity.getThumbnailUrl())
                        .thumbnailSize(entity.getThumbnailSize())
                        .thumbnailWidth(entity.getThumbnailWidth())
                        .thumbnailHeight(entity.getThumbnailHeight())
                        .name("pic")
                , new SendMsgListener());
    }

    public void sendVideoMessage(String roomId, String messageId, String themeId, VideoContent videoContent, String md5, String thumbnailUrl, int thumbnailWidth, int thumbnailHeight) {
        ApiManager.doMessageSend(App.getContext(), roomId, messageId, themeId,
            new MsgBuilder(MessageType.VIDEO)
                .url(videoContent.getUrl())
                .size((int) videoContent.getSize())
                .width(videoContent.getWidth())
                .md5(md5)
                .height(videoContent.getHeight())
                .name(videoContent.getName())
                .duration((int) videoContent.getDuration())
                .thumbnailUrl(thumbnailUrl)
                .thumbnailWidth(thumbnailWidth)
                .thumbnailHeight(thumbnailHeight)
                .androidLocalPath(videoContent.getAndroid_local_path())
            , new SendMsgListener());
    }

    public void sendReplyVideoMessage(String roomId, String messageId, String themeId, VideoContent videoContent, String md5, String thumbnailUrl, int thumbnailWidth, int thumbnailHeight) {
        ApiManager.doMessageReply(App.getContext(), roomId, messageId, themeId,
            new MsgBuilder(MessageType.VIDEO)
                .url(videoContent.getUrl())
                .size((int) videoContent.getSize())
                .width(videoContent.getWidth())
                .height(videoContent.getHeight())
                .md5(md5)
                .name(videoContent.getName())
                .duration((int) videoContent.getDuration())
                .thumbnailUrl(thumbnailUrl)
                .thumbnailWidth(thumbnailWidth)
                .thumbnailHeight(thumbnailHeight)
                .androidLocalPath(videoContent.getAndroid_local_path())
            , new ReplyMessageListener());
    }

    /**
     * 提交影音訊息
     */
    public void sendVoiceMessage(String roomId, String messageId, double duration, String url, String themeId, String md5) {
        ApiManager.doMessageSend(App.getContext(), roomId, messageId, themeId,
            new MsgBuilder(MessageType.VOICE)
                .duration(duration)
                .md5(md5)
                .url(url)
            , new SendMsgListener());
    }

    /**
     * 提交影音主題訊息
     */
    public void sendReplyVoiceMessage(String roomId, String messageId, int duration, String url, String themeId, String md5) {
        ApiManager.doMessageReply(App.getContext(), roomId, messageId, themeId,
            new MsgBuilder(MessageType.VOICE)
                .md5(md5)
                .duration(duration)
                .url(url)
            , new ReplyMessageListener());
    }

    /**
     * 提交貼圖訊息
     */
    public void sendStickerMessage(String roomId, String messageId, String id, String packageId, String themeId) {
        ApiManager.doMessageSend(App.getContext(), roomId, messageId, themeId, new MsgBuilder(MessageType.STICKER).id(id).packageId(packageId), new SendMsgListener());
    }

    /**
     * 提交貼圖主題訊息
     */
    public void sendReplyStickerMessage(String roomId, String messageId, String id, String packageId, String themeId) {
        ApiManager.doMessageReply(App.getContext(), roomId, messageId, themeId,
            new MsgBuilder(MessageType.STICKER).id(id).packageId(packageId)
            , new ReplyMessageListener());
    }

    private void hangupCalling(String roomId) {
    }

    /*private void updateCallSession(String serviceRoomId, final long serviceStartTime) {
        ISession session = DBManager.getInstance().querySession(serviceRoomId);
        if (session == null) {
            ApiManager.getInstance().requestSessionDetail(serviceRoomId, userId, new SessionDetailRequest.OnNoticeListener() {
                @Override
                public void onSessionInfoSuccess(ChatRoomEntity entity) {
                    entity.setCalling(true);
                    entity.setTime(serviceStartTime);
                    DBManager.getChatRoomDao().save(entity);
                    broadcastCallingSession(entity);
                }

                @Override
                public void onFailed(String reason) {

                }
            });
        } else {
            session.setCalling(true);
            session.setTime(serviceStartTime);
            broadcastCallingSession(session);
        }
    }*/

//    private void broadcastCallSession(String roomId) {
//        EventBusUtils.sendEvent(new EventMsg(MsgConstant.SESSION_UPDATE_CALLING_FILTER, roomId));
//    }

    private void broadCastCancelChat() {
        EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.CANCEL_FILTER));
    }

    private void broadcastUserExit(String roomId, String roomName, String avatarUrl, String userId) {
        AccountRoomRelReference.deleteRelByRoomIdAndAccountId(null, roomId, userId);
//        DBManager.getInstance().delAccount_Room(roomId, userId);
        EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.USER_EXIT, new UserExitBean(roomId, roomName, avatarUrl, userId)));
        //更新群组
        EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.GROUP_REFRESH_FILTER, new GroupRefreshBean(roomId, null, null)));
    }

    private void broadcastSingleDismissRoom(String userId) {
        UserProfileEntity account = DBManager.getInstance().queryFriend(userId);
        if (account != null && AccountType.FRIEND.equals(account.getType())) {
            EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.REMOVE_FRIEND_FILTER, account));
        }
    }

    private void broadcastGroupDismissRoom(String roomId) {
        EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.REMOVE_GROUP_FILTER, roomId));
        //更新联系人的群组列表
        EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.GROUP_REFRESH_FILTER, new GroupRefreshBean(roomId, null, null)));
    }

//    private void broadcastSqueezedOut() {
//        EventBusUtils.sendEvent(new EventMsg(MsgConstant.SQUEEZED_OUT_FILTER));
//    }

    private void broadcastMsgNotice(String msgId, int receivedNum, int readNum, int sendNum) {
        EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.MSG_NOTICE_FILTER, new MsgNoticeBean(msgId, receivedNum, readNum, sendNum)));
    }

    /**
     * 構造確認消息
     */
    // EVAN_FLAG 2020-02-10 Change request API message/list id to id
    private void constructConfirmMsg(MessageEntity entity, UserProfileEntity sender) {
        String sessionId = entity.getRoomId();
        String msgId = entity.getId();


        // EVAN_FLAG 2019-10-19 If you receive sandId == yourself, it means that multiple vehicles are sending new messages.
        //  Update the chat room interaction time
        String userId = TokenPref.getInstance(App.getContext()).getUserId();
        if (userId.equals(entity.getSenderId())) {
            boolean dd = ChatRoomReference.getInstance().updateInteractionTimeById(sessionId);
        }

        // Send notification
        if (!AccountType.SELF.equals(sender.getType())) {
//            App application = (App) App.getAppContext().getApplicationContext();
            String currentRoomId = UserPref.getInstance(App.getContext()).getCurrentRoomId();
            if (!sessionId.equals(currentRoomId)) {
                doMessageReceived(Lists.newArrayList(msgId));
//                doMessageReceived(msgId);
            }
//            if (!sessionId.equals(application.currentRoomId)) {
//                doMessagesReceived(msgId);
//            }
        }

        MessageFlag flag = entity.getFlag();

        if (entity.getType() == null) {
            entity.setType(MessageType.UNDEF);
        }


        if (entity != null) {
            if (entity.getTag() != null && !entity.getTag().isEmpty()) {
                FacebookTag facebookTag = JsonHelper.getInstance().from(entity.getTag(), FacebookTag.class);
                List<MessageEntity> messageEntities = MessageReference.findByRoomId(entity.getRoomId());
                for (MessageEntity roomMessage : messageEntities) {
                    if (entity.getId().equals(roomMessage.getId())) continue;
                    if (roomMessage.getTag() != null && !roomMessage.getTag().isEmpty()) {
                        FacebookTag roomFacebookTag = JsonHelper.getInstance().from(roomMessage.getTag(), FacebookTag.class);
                        if (roomFacebookTag.getData() == null) continue;
                        if (roomFacebookTag.getData().getCommentId() == null) continue;
                        if (roomFacebookTag.getData().getCommentId().equals(facebookTag.getData().getCommentId())
                            && (roomFacebookTag.getData().getReplyType() == null || roomFacebookTag.getData().getReplyType().isEmpty())) {
                            entity.setThemeId(roomMessage.getId());
                            entity.setNearMessageId(roomMessage.getId());
                            entity.setNearMessageType(roomMessage.getType());
                            entity.setNearMessageAvatarId(roomMessage.getAvatarId());
                            entity.setNearMessageContent(roomMessage.getContent());
                            entity.setNearMessageSenderId(roomMessage.getSenderId());
                            entity.setNearMessageSenderName(roomMessage.getSenderName());
                            // 私訊回覆只能一次 要紀錄到 DB
                            if ("private".equals(facebookTag.getData().getReplyType())) {
                                MessageReference.updateFacebookPrivateReplyStatus(entity.getRoomId(), roomMessage.getId(), true);
                                EventBus.getDefault().post(new EventMsg<>(MsgConstant.ON_FACEBOOK_PRIVATE_REPLY, roomMessage.getId()));
                            }
                            MessageReference.save(entity.getRoomId(), entity);
                        }
                    }
                }
            }

//            MessageEntity nearMsg = null;
//            assembleNearMsg(entity, sessionId, msg, nearMsg);
//            msg.setType(entity.getTodoOverviewType());
//
//            msg.setSourceType(SourceType.of(entity.getSourceType()));
//
//            msg.setSendNum(entity.getSendNum());
//            msg.setStatus(MessageStatus.RECEIVED);
//            msg.setFromMsgId(entity.getFromMsgId());
//            msg.setMsgSrc(entity.getMsgSrc());
//            msg.setFromRoomId(entity.getFromRoomId());
//            msg.setSessionId(entity.getId());
//            msg.setSenderId(entity.getSenderId());
//            msg.setSendName(entity.getSendName());
//            msg.setFrom(entity.getFrom());
//            msg.setSenderAvatarUrl(entity.getSenderUrl());
//            msg.setSendTime(entity.getSendTime());
            boolean insertMessage = MessageReference.save(sessionId, entity);
//            boolean insertMessage = MessageReference.saveByRoomId(sessionId, Lists.newArrayList(entity));

            String currentRoomId = UserPref.getInstance(App.getContext()).getCurrentRoomId();
            if (sessionId.equals(currentRoomId)) {
                ChatMessageService.doMessageReadAllByRoomId(App.getContext(), sessionId, null, false);

//                ChatService.getInstance().doMessagesRead(sessionId, null, false);
            }
//            if (sessionId.equals(application.currentRoomId)) {
//                ChatService.getInstance().doMessageReadAllByRoomId(sessionId);
//            }
//            doMessagesRead

            if (insertMessage) {

                boolean hasData = ChatRoomReference.getInstance().hasLocalData(sessionId);
                if (hasData) {
                    updateChatRoomEntity(null, sessionId, entity, false, true);
                } else {
                    updateChatRoomEntity(null, sessionId, entity, false, true);
                }

//                ChatRoomEntity chatRoomEntity = ChatRoomReference.getInstance().findById(sessionId);
                //根据时间，判断是否是最后一条消息
//                if (chatRoomEntity != null) {
////                if (chatRoomEntity != null && (chatRoomEntity.getTime() < entity.getSendTime())) {
//                    ChatRoomReference.getInstance().updateLastMessageIdById(sessionId, msgId);
//                    updateChatRoomEntity(null, chatRoomEntity.getId(), entity, false, true);
//                }
//
//                if (chatRoomEntity == null) {
//                    updateChatRoomEntity(null, sessionId, entity, false, true);
//                }

                EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.MSG_RECEIVED_FILTER, JsonHelper.getInstance().toJson(entity)));
//                EventBusUtils.sendEvent(new EventMsg(MsgConstant.MSG_RECEIVED_FILTER, new ReceiverMessageBean(sessionId, entity)));
                if (!entity.isForward() && userId.equals(entity.getSenderId()) && !MessageType.CALL.equals(entity.getType())) {
                    broadCastUpdateBadgeItem(1);
                } else {
                    EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.TRANSFER_SEND_SUCCESS_FILTER));
                }
            }
        }
    }

    //判断聊天室是否是服务号
    private boolean isService(ChatRoomEntity session) {
        String userId = TokenPref.getInstance(App.getContext()).getUserId();
        return session.getType().equals(ChatRoomType.services) && !session.getOwnerId().equals(userId);
    }

    public void broadCastUpdateBadgeItem(int num) {
        Log.e(TAG, "計算未讀數： " + num);
        EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.BADGE_UPDATE_FILTER, new BadgeUpgradeBean(num, Constant.SESSION_NUM_SINGLE)));
    }

    /**
     * 增加控制 關於 message new
     */
    public void updateChatRoomEntity(ChatRoomEntity entity, String roomId, final MessageEntity msg, final boolean isCalling, boolean isNewMessage) {
        if (MessageType.BROADCAST.equals(msg.getType())) {
            return;
        }
        String userId = TokenPref.getInstance(App.getContext()).getUserId();
        String ringName = msg.getRingName();
        ChatRoomService.getInstance().getChatRoomItem(App.getContext(), userId, roomId, RefreshSource.REMOTE, new ServiceCallBack<ChatRoomEntity, RefreshSource>() {
            @Override
            public void complete(ChatRoomEntity entity, RefreshSource source) {
                MessageEntity lastMessage = entity.getLastMessage();
                if (lastMessage == null) {
                    return;
                }
                String senderId = lastMessage.getSenderId();
                if (!senderId.equals(userId)) {
                    if (!MessageType.CALL.equals(lastMessage.getType()) && !(lastMessage.content() instanceof CallContent)) {
                        if (!(isService(entity) && !entity.getOwnerId().equals(userId) && !senderId.equals(entity.getOwnerId()))) {
                            if (Strings.isNullOrEmpty(entity.getServiceNumberAgentId())) {
                                playNotifySound(entity, ringName);
                            } else if (entity.getServiceNumberAgentId().equals(userId) && ServiceNumberType.PROFESSIONAL.equals(entity.getServiceNumberType())) {
                                playNotifySound(entity, ringName);
                            } else if (ServiceNumberType.NONE.equals(entity.getServiceNumberType())) {
                                playNotifySound(entity, ringName);
                            }
                        }
                    }
                }

//                ChatRoomEntity localEntity = ChatRoomReference.getInstance().findById(roomId);
                ChatRoomEntity localEntity = ChatRoomReference.getInstance().findById2(userId, roomId, true, true, true, true, true);
                if (localEntity != null) {
                    entity.setLocalControl(localEntity);
                }

                boolean hasAtMe = MessageType.AT.equals(lastMessage.getType()) && (lastMessage.getContent().contains(userId) || lastMessage.getContent().contains("\"objectType\": \"All\""));

                entity.setAtMe(hasAtMe);
                if (entity.getLastMessage() != null) {
                    entity.setUpdateTime(entity.getLastMessage().getSendTime());
                    MessageReference.save(null, entity);
                }
                ChatRoomReference.getInstance().save(entity);
//                CELog.e(" On Message New And Up UI");
                if (isNewMessage) {
                    EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.SESSION_UPDATE_FILTER, JsonHelper.getInstance().toJson(entity)));
                } else {
                    EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.SESSION_UPDATE_FILTER, JsonHelper.getInstance().toJson(entity)));
                }
            }

            @Override
            public void error(String message) {

            }
        });


//        final String senderId = msg.getSenderId();
//        String userId = TokenPref.getInstance(App.getAppContext()).getUserId();
//        if (entity == null) {
//            ApiManager.doRoomItem(App.getAppContext(), roomId, userId, new RoomItemRequest.ApiListener() {
//                @Override
//                public void onSuccess(ChatRoomEntity entity) {
////                    entity.setCalling(isCalling);
//                    if (!senderId.equals(userId)) {
//                        if (!MessageType.CALL.equals(msg.getTodoOverviewType()) && !(msg.content() instanceof CallContent)) {
//                            if (!(isService(entity) && !entity.getOwnerId().equals(userId) && !msg.getSenderId().equals(entity.getOwnerId()))) {
////                                entity.setUnReadNum(1);
//                                if (Strings.isNullOrEmpty(entity.getServiceNumberAgentId())) {
//                                    playNotifySound();
//                                } else if (entity.getServiceNumberAgentId().equals(userId) && ServiceNumberType.PROFESSIONAL.equals(entity.getServiceNumberType())) {
//                                    playNotifySound();
//                                } else if (ServiceNumberType.NONE.equals(entity.getServiceNumberType())) {
//                                    playNotifySound();
//                                }
//                            }
//                        }
//                    }
//                    ChatRoomEntity localEntity = ChatRoomReference.getInstance().findById(roomId);
//                    if (localEntity != null) {
//                        entity.setLocalControl(localEntity);
//                    }
//                    boolean hasAtMe = msg.getContent().contains(userId) || msg.getContent().contains("\"objectType\": \"All\"");
//
//                    entity.setAtMe(hasAtMe);
//                    if (entity.getLastMessage() != null) {
//                        entity.setUpdateTime(entity.getLastMessage().getSendTime());
//                    }
//
//                    ChatRoomReference.getInstance().save(entity);
//                    CELog.e(" On Message New And Up UI");
//                    if (isNewMessage) {
//                        EventBusUtils.sendEvent(new EventMsg(MsgConstant.SESSION_UPDATE_FILTER, JsonHelper.getInstance().toJson(entity)));
//                    } else {
//                        EventBusUtils.sendEvent(new EventMsg(MsgConstant.SESSION_UPDATE_FILTER, JsonHelper.getInstance().toJson(entity)));
//                    }
//
//                }
//
//                @Override
//                public void onFailed(String reason) {
//
//                }
//            });
//        } else {
//            entity.setContent(msg.getContent());
//            entity.setUpdateTime(msg.getSendTime());
//            if (!senderId.equals(userId)) {
//                if (!MessageType.CALL.equals(msg.getTodoOverviewType()) && !(msg.content() instanceof CallContent)) {
//                    if (!(isService(entity) && !entity.getOwnerId().equals(userId) && !msg.getSenderId().equals(entity.getOwnerId()))) {
//                        if (!ServiceNumberType.PROFESSIONAL.equals(entity.getServiceNumberType()) || Strings.isNullOrEmpty(entity.getServiceNumberAgentId())) {
////                            entity.setUnReadNum(entity.getUnReadNum() + 1);
//                        } else if (ServiceNumberType.PROFESSIONAL.equals(entity.getServiceNumberType()) && entity.getServiceNumberAgentId().equals(userId)) {
////                            entity.setUnReadNum(entity.getUnReadNum() + 1);
//                        }
//
//                        //更新chat页面未读书
//                        App application = (App) App.getAppContext().getApplicationContext();
//                        String currentRoomId = UserPref.getInstance(App.getAppContext()).getCurrentRoomId();
//                        if (!entity.getId().equals(currentRoomId)) {
//                            application.unReadNum++;
//                            if (!ServiceNumberType.PROFESSIONAL.equals(entity.getServiceNumberType()) || Strings.isNullOrEmpty(entity.getServiceNumberAgentId())) {
//                                playNotifySound();
//                            } else if (ServiceNumberType.PROFESSIONAL.equals(entity.getServiceNumberType()) && !entity.getServiceNumberAgentId().equals(userId)) {
//                                ChatMessageService.doMessageReadAllByRoomId(App.getAppContext(), entity, entity.getUnReadNum(), Lists.newArrayList(msg.getId()), false);
////                                doMessagesRead(entity.getId(), Lists.newArrayList(msg.getId()), false);
//                            } else {
//                                playNotifySound();
//                            }
//                        }
//
//                    }
//                }
//            }
//            ChatRoomReference.getInstance().save(entity);
////            broadcastSession(chatRoomEntity);
//            CELog.e(" On Message New And Up UI");
//            if (isNewMessage) {
////                ChatRoomBatchUpdateController.handler(entity.getId(), msg.getId());
//                EventBusUtils.sendEvent(new EventMsg(MsgConstant.SESSION_UPDATE_FILTER, JsonHelper.getInstance().toJson(entity)));
//            } else {
//                EventBusUtils.sendEvent(new EventMsg(MsgConstant.SESSION_UPDATE_FILTER, JsonHelper.getInstance().toJson(entity)));
////                EventBusUtils.sendEvent(new EventMsg(MsgConstant.SESSION_UPDATE_FILTER, entity.getId()));
//            }
//        }
    }

    // EVAN_FLAG 2019-08-30 來消息聲音佔據藍芽斷線問題
    //来新消息声音
    private void playNotifySound(ChatRoomEntity entity, String ringName) {
        if (Strings.isNullOrEmpty(ringName)) {
            return;
        }
        if (soundPool == null) {
            SoundPool.Builder builder = new SoundPool.Builder();
            //传入最多播放音频数量,
            builder.setMaxStreams(10);
            //AudioAttributes是一个封装音频各种属性的方法
            AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
            //设置音频流的合适的属性
            attrBuilder.setLegacyStreamType(AudioManager.STREAM_RING);
//                attrBuilder.setLegacyStreamType(AudioManager.STREAM_MUSIC);
            //加载一个AudioAttributes
            builder.setAudioAttributes(attrBuilder.build());
            soundPool = builder.build();
            messageSound = soundPool.load(App.getContext(), R.raw.message_ringtone, 1);
            soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                @Override
                public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                    soundPool.play(messageSound, 1, 1, 0, 0, 1);
                }
            });
        }

        soundPool.play(messageSound, 1, 1, 0, 0, 1);

//        if (soundPool == null) {
//            soundPool = new SoundPool(10, AudioManager.STREAM_NOTIFICATION, 0);
//            messageSound = soundPool.load(App.getAppContext(), R.raw.message_ringtone, 1);
//            soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
//                @Override
//                public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
//                    soundPool.play(1, 1, 1, 100, 0, 1);
//                }
//            });
//        } else {
//            soundPool.play(messageSound, 1, 1, 0, 0, 1);
//        }
    }

//    public class ReplyMessageListener implements MessageReplyRequest.Listener {
//
//        @Override
//        public void onSuccess(String id, int sendNum, long sendTime, String roomId) {
//            ChatService.this.sendMsgSuccess(id, sendNum, sendTime, roomId);
//        }
//
//        @Override
//        public void onFailed(String roomId, String msgId, String errorMessage) {
//            handleSendMessageFail(roomId, msgId);
//        }
//
//    }

    // EVAN_FLAG 2019-11-20 (1.8.0) 處理傳送失敗的訊息
    public void handleSendMessageFail(String roomId, String msgId) {
        MessageEntity message = MessageReference.findById(msgId);
        if (message != null) {
            if (message.getStatus().equals(MessageStatus.SUCCESS)) {
                return;
            }
            DBManager.getInstance().updateMessageStatus(msgId, MessageStatus.FAILED);
//            String content = "";
//            MessageType type = message.getTodoOverviewType();
//            switch (type) {
//                case FILE:
//                    content = Constant.file_msg;
//                    break;
//                case IMAGE:
//                    content = Constant.image_msg;
//                    break;
//                case AT:
//                    if (message.content() instanceof AtContent) {
//                        content = JsonHelper.getInstance().toJson(((AtContent) message.content()).getMentionContents());
//                    }
//                    break;
//                case TEXT:
//                    if (message.content() instanceof TextContent) {
//                        content = ((TextContent) message.content()).getText();
//                    }
//                    break;
//                case VOICE:
//                    content = Constant.voice_msg;
//                    break;
//                case VIDEO:
//                    content = Constant.video_msg;
//                    break;
//                case STICKER:
//                    content = Constant.sticker_msg;
//                    break;
//                case IMAGE_TEXT:
//                    content = Constant.image_msg_text;
//                    break;
//                case AD:
//                case CALL:
//                case LIST_TEXT:
//                case LOCATION:
//                case BUSINESS_TEXT:
//                case UNDEF:
//                default:
//                    content = "";
//            }
            // TODO: 2019-11-20 處理發送失敗 Content
//            if (!Strings.isNullOrEmpty(content)) {
//                DBManager.getChatRoomDao().updateFailContentById(roomId, content);
//            }
//            broadcastMsgStatus(msgId, -1, System.currentTimeMillis());

            EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.MESSAGE_SEND_FAIL, roomId));
            EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.MSG_STATUS_FILTER, new MsgStatusBean(msgId, -1, System.currentTimeMillis())));
        }
    }

    public class SendMsgListener extends MessageSendRequest.Listener<MessageEntity> {

        @Override
        public void onSuccess(MessageEntity success) {
            ChatService.this.sendMsgSuccess(success);
        }

        @Override
        public void onFailed(MessageEntity failed, String errorMessage) {
            handleSendMessageFail(failed.getRoomId(), failed.getId());
        }
    }

    public void sendMsgSuccess(String messageId, int sendNum, long sendTime, String roomId) {
        DBManager.getInstance().updateMessageStatus(messageId, MessageStatus.SUCCESS);
        DBManager.getInstance().updateSendNum(messageId, sendNum);
        DBManager.getInstance().updateSendTime(messageId, sendTime);
        EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.MSG_STATUS_FILTER, new MsgStatusBean(messageId, sendNum, sendTime)));
//        broadcastMsgStatus(id, sendNum, sendTime);
        // EVAN_FLAG 2019-10-19 Send a new reply message, update the chat room interaction time
        boolean dd = ChatRoomReference.getInstance().updateInteractionTimeById(roomId);
    }

    public void sendMsgSuccess(MessageEntity success) {
        DBManager.getInstance().updateMessageStatus(success.getId(), MessageStatus.SUCCESS);
        DBManager.getInstance().updateSendNum(success.getId(), success.getSendNum());
        DBManager.getInstance().updateSendTime(success.getId(), success.getSendTime());
        EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.MSG_STATUS_FILTER, new MsgStatusBean(success.getId(), success.getSendNum(), success.getSendTime())));
//        broadcastMsgStatus(id, sendNum, sendTime);
        // EVAN_FLAG 2019-10-19 Send a new reply message, update the chat room interaction time
        boolean dd = ChatRoomReference.getInstance().updateInteractionTimeById(success.getRoomId());
    }

    public void searchEmployee(UserSearchRequest.Listener listener, String keyword, int index) {
        ApiManager.getInstance().searchEmployee(App.getContext(), listener, keyword, index);
    }

    public void addContact(ApiListener<String> listener, String id, String alias) {
        ApiManager.getInstance().addContact(App.getContext(), listener, id, alias);
    }

    /*@Override
    public void onDestroy() {
        super.onDestroy();
        if (soundPool != null) {
            soundPool.release();
        }
        close();
    }*/

    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
//        if (timer != null) {
//            timer.cancel();
//        }
        close();
    }

    private class SocketOnNoticeListener implements SocketManager.OnNoticeListener {
        @Override
        public void onConnected() {
            CELog.i("connected");
        }


        @Override
        public void onConfirm(MessageEntity msgEntity) {
            String senderId = msgEntity.getSenderId();
            UserProfileEntity accountCE = DBManager.getInstance().queryFriend(senderId);
            constructConfirmMsg(msgEntity, accountCE);
        }


        /**
         * new message 收到訊息的地方
         */
        @Override
        public void onMessagesNew(Ack ack, List<MessageEntity> entities) {
            Collections.sort(entities);
            if (!entities.isEmpty()) {
                for (MessageEntity entity : entities) {
                    // EVAN_FLAG 2020-08-25 (1.12.0) 擋住廣播訊息
                    if (MessageType.BROADCAST.equals(entity.getType())) {
                        RepairMessageService.setQueue(App.getContext(), entity.getRoomId(), Sets.newHashSet(entity.getId()), false);
                        return;
                    }
                    QuickReplySocket quickReplySocket = new QuickReplySocket();
                    quickReplySocket.parse(entity.getContent());
                    if (quickReplySocket != null) {
                        if (!quickReplySocket.getItems().isEmpty()) {
                            quickReplySocket.setRoomId(entity.getRoomId());
                            EventBus.getDefault().post(
                                new EventMsg<>(MsgConstant.MESSAGE_QUICK_REPLY,
                                    JsonHelper.getInstance().toJson(quickReplySocket)));
                        }
                    }

                    String roomId = entity.getRoomId();
                    MessageNewQueueController.getInstance().setQueue(App.getContext(), roomId, entity);


                    return;

//                    UserProfileEntity accountCE = DBManager.getInstance().queryFriend(entity.getSenderId());
//                    if (accountCE == null) {
//                        handleStrangerMsg(entity, entity.getSenderId());
//                    } else {
//                        constructNewMessage(entity, accountCE);
//                    }

//                    String currentRoomId = UserPref.getInstance(App.getAppContext()).getCurrentRoomId();
//                    SocketManager.emitAck(App.getAppContext(), ack, AckBean.Build().ack(true)
//                            .event(NoticeName.MESSAGE_NEW.getName())
//                            .content(entity.content().simpleContent())
//                            .action(currentRoomId.equals(entity.getRoomId()) ? "read" : "received")
//                    );
                }
            }
        }

        @Override
        public void onMessagesOffline(Ack ack, List<MessageEntity> entities) {
//            String currentRoomId = UserPref.getInstance(App.getAppContext()).getCurrentRoomId();
            Iterator<MessageEntity> iterator = entities.iterator();
            Multimap<String, MessageEntity> batchData = ArrayListMultimap.create();
            while (iterator.hasNext()) {
                MessageEntity entity = iterator.next();
                // ack callback
//                SocketManager.emitAck(App.getAppContext(), ack, AckBean.Build().ack(true)
//                        .event(NoticeName.MESSAGE_OFFLINE.getName())
//                        .content(entity.content().simpleContent())
//                        .action(currentRoomId.equals(entity.getRoomId()) ? "read" : "received")
//                );
                boolean hasLocalData = MessageReference.hasLocalData(null, entity.getRoomId(), entity.getId());
                if (!hasLocalData) {
                    // no local data in Queue
                    if (MessageType.BROADCAST.equals(entity.getType())) {
                        RepairMessageService.setQueue(App.getContext(), entity.getRoomId(), Sets.newHashSet(entity.getId()), false);
                        iterator.remove();
                    } else {
                        batchData.put(entity.getRoomId(), entity);
                    }
                } else {
                    iterator.remove();
                }
            }

            for (String key : batchData.keySet()) {
                Collection<MessageEntity> values = batchData.get(key);
                MessageOfflineQueueController.getInstance().setQueue(App.getContext(), key, Sets.newHashSet(values));
            }
        }

//        @Override
//        public void onSendSuccess(String id, int sendNum) {
//            DBManager.getInstance().updateMessageStatus(id, MessageStatus.SUCCESS);
//            DBManager.getInstance().updateSendNum(id, sendNum);
//            EventBusUtils.sendEvent(new EventMsg(MsgConstant.MSG_STATUS_FILTER, new MsgStatusBean(id, sendNum, System.currentTimeMillis())));

        /// /            broadcastMsgStatus(id, sendNum, System.currentTimeMillis());   //该方法未被调用
//        }
        @Override
        public void onNotice(String msgId, int receivedNum, int readNum, int sendNum) {
            //发送已到已读广播
            MessageEntity message = MessageReference.findById(msgId);
            if (message == null) {
                return;
            }
            message.setSendNum(sendNum);
            message.setReceivedNum(receivedNum);
            message.setReadedNum(readNum);
            if (receivedNum > 0) {
                if (readNum > 0) {
                    message.setStatus(MessageStatus.READ);
                } else {
                    message.setStatus(MessageStatus.RECEIVED);
                }
            }
//            MessageReference.saveByRoomId(message.getRoomId(), Lists.newArrayList(message));
            MessageReference.save(message.getRoomId(), message);
            broadcastMsgNotice(msgId, receivedNum, readNum, sendNum);

        }

        @Override
        public void onUpdateProfile(final String userId, final String updateProfileKey, String updateProfileValue, JSONObject jsonObject) {
            ApiManager.doUserItem(App.getContext(), userId, new UserItemRequest.Listener() {
                @Override
                public void onSuccess(UserProfileEntity entity) {
                    String name = TextUtils.isEmpty(entity.getAlias()) ? entity.getNickName() : entity.getAlias();
                    if ("Update_Profile_NickName".equals(updateProfileKey)) {
                        MessageReference.updateSenderName(entity.getRoomId(), name);
                        DBManager.getInstance().insertFriends(entity);
                        UserProfileEntity accountCE = DBManager.getInstance().queryFriend(userId);
                        if (accountCE != null && !accountCE.isBlock() && !Strings.isNullOrEmpty(accountCE.getRoomId())) {
                            ChatRoomEntity iSession = ChatRoomReference.getInstance().findById2(userId, accountCE.getRoomId(), false, false, false, false, false);
                            if (iSession != null && iSession.getType().equals(ChatRoomType.friend)) {
                                boolean isUpdated = ChatRoomReference.getInstance().updateChatRoomTitle(iSession.getId(), name);
                                if (isUpdated) EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.SESSION_UPDATE_FILTER, iSession));
                            }
                        }
                        EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.SELF_REFRESH_FILTER, name));
                        EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.ADD_FRIEND_FILTER));
                        EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.NOTICE_DISCUSS_GROUP_USER_PROFILE_CHANGED, jsonObject));
                    }
                }

                @Override
                public void onFailed(String errorMessage) {
                    if (!TextUtils.isEmpty(errorMessage)) {
                        ToastUtils.showToast(App.getContext(), errorMessage);
                    }
                }
            });
        }

        @Override
        public void onUpdateUserAvatar(final String accountId, final String avatarId) {
            // EVAN_FLAG 2020-07-02 (1.12.0)
            //  更新聯絡人頭像，
            //  更新聊天室包含該用戶頭像（好友、多人、社團(無avatarId)），
            //  訊息senderId 為該用戶

//            CELog.e("");
//            for (ChatRoomEntity entity : rooms) {
//                ChatRoomReference.getInstance().updateLogoUtlById(entity.getId(), avatarId);
//                EventBusUtils.sendEvent(new EventMsg(MsgConstant.SEND_UPDATE_AVATAR, new UpdateAvatarBean(JocketManager.UPDATE_USER_AVATAR, entity.getId(), accountId, avatarId)));
//            }

//            ApiManager.doUserItem(App.getContext(), accountId, new UserItemRequest.Listener() {
//                @Override
//                public void onSuccess(UserProfileEntity entity) {
//                    DBManager.getInstance().insertFriends(entity);
//                    UserProfileEntity accountCE = DBManager.getInstance().queryFriend(accountId);
//                    String roomId = accountCE.getRoomId();
//                    if (roomId != null) {
//                        ChatRoomReference.getInstance().updateLogoUtlById(roomId, avatarId);
//                    }
//                    EventBusUtils.sendEvent(new EventMsg(MsgConstant.SEND_UPDATE_AVATAR, new UpdateAvatarBean(JocketManager.UPDATE_USER_AVATAR, roomId, accountId, avatarId)));


//                    String userId = TokenPref.getInstance(App.getContext()).getUserId();

//                    List<ChatRoomEntity> fRrooms = ChatRoomReference.getInstance().findFriendRoomEntitysByRelAccountId(userId, accountId, false, false, false);
//                    for (ChatRoomEntity f : fRrooms) {
//                        ChatRoomReference.getInstance().updateLogoUtlById(f.getId(), avatarId);
//                    }
            // 通知所有關係聊天室
//                    List<ChatRoomEntity> gdFooms = ChatRoomReference.getInstance().findByRelAccountIdAndInType(null, userId, accountId, ChatRoomType.GROUP_or_DISCUSS, false, false, false);
//                    Set<ChatRoomEntity> roomSet = Sets.newHashSet(Iterables.concat(fRrooms, gdFooms));
//
//                    if (roomSet != null && !roomSet.isEmpty()) {
//                        List<UpdateAvatarBean> beans = Lists.newArrayList();
//                        Iterator<ChatRoomEntity> iterator = roomSet.iterator();
//                        while (iterator.hasNext()) {
//                            ChatRoomEntity r = iterator.next();
//                            // 如果社團 avatarId 為空才通知更新
//                            if (ChatRoomType.GROUP.equals(r.getTodoOverviewType()) && !Strings.isNullOrEmpty(r.getAvatarId())) {
//                            } else {
//                                beans.add(UpdateAvatarBean.Build()
//                                        .userId(accountId)
//                                        .avatar(avatarId)
//                                        .roomId(r.getId())
//                                        .build());
//                            }
////                            EventBusUtils.sendEvent(new EventMsg(MsgConstant.SEND_UPDATE_AVATAR, UpdateAvatarBean.Build()
////                                    .userId(accountId)
////                                    .type(JocketManager.UPDATE_USER_AVATAR)
////                                    .avatar(avatarId)
////                                    .roomId(r.getId())
////                                    .build()));
//                        }


            Map<String, String> data = Maps.newHashMap(ImmutableMap.of("accountId", accountId, "avatarId", avatarId));
            EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.NOTICE_UPDATE_AVATARS, JsonHelper.getInstance().toJson(data)));
//                    }
//                }

//                @Override
//                public void onFailed(String errorMessage) {
//                    if (!TextUtils.isEmpty(errorMessage)) {
//                        ToastUtils.showToast(App.getContext(), errorMessage);
//                    }
//                }
//            });
        }

//        @Override
//        public void onUpdateDiscussAvatar(String roomId, final String avatarId) {
//            ChatRoomReference.getInstance().updateLogoUtlById(roomId, avatarId);
//            EventBusUtils.sendEvent(new EventMsg(MsgConstant.SEND_UPDATE_AVATAR, new UpdateAvatarBean(JocketManager.UPDATE_DISCUSS_AVATAR, roomId, "", avatarId)));
//        }

        @Override
        public void onUpdateGroupAvatar(String roomId, final String userId, final String avatarId) {
            ChatRoomReference.getInstance().updateLogoUtlById(roomId, avatarId);
            EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.SEND_UPDATE_AVATAR, new UpdateAvatarBean(NoticeCode.UPDATE_USER_AVATAR.getName(), roomId, userId, avatarId)));
//            EventBusUtils.sendEvent(new EventMsg(MsgConstant.SEND_UPDATE_AVATAR, new UpdateAvatarBean(JocketManager.UPDATE_GROUP_AVATAR, roomId, userId, avatarId)));
        }

        /**
         * 收回一則訊息
         */
        @Override
        public void onRetractMessage(String roomId, String messageId, int flag) {
            MessageEntity retractMessageEntity = MessageReference.findById(messageId);
            if (retractMessageEntity == null) {
                return;
            }
            NotifyHelper.retractNotify(App.getContext(), roomId, messageId);
            retractMessageEntity.setFlag(MessageFlag.RETRACT);
            MessageReference.save(roomId, retractMessageEntity);
            String userId = TokenPref.getInstance(App.getContext()).getUserId();
            ChatRoomService.getInstance().getChatRoomItem(App.getContext(), userId, roomId, RefreshSource.REMOTE, new ServiceCallBack<ChatRoomEntity, RefreshSource>() {
                @Override
                public void complete(ChatRoomEntity entity, RefreshSource source) {
                    ChatRoomReference.getInstance().save(entity);
                    EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.MSG_RECEIVED_FILTER, JsonHelper.getInstance().toJson(retractMessageEntity)));
//                    EventBusUtils.sendEvent(new EventMsg(MsgConstant.MSG_RECEIVED_FILTER, new ReceiverMessageBean(roomId, retractMessageEntity)));
                    EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.SESSION_UPDATE_FILTER, JsonHelper.getInstance().toJson(entity)));
                }

                @Override
                public void error(String message) {

                }
            });

//            MessageEntity message = MessageReference.findById(messageId);


//            if (message != null) {
////                IMessage retractMsg = MsgKitAssembler.assembleTimeMsg(roomId, Constant.RETRACT_MSG, message.getTime(), TipMsgFormat.NOTICE);
////                retractMsg.setMsgId(messageId);
////                message.setFlag(Constant.RETRACT_FLAG);
//                message.setFlag(MessageFlag.RETRACT);
////                retractMsg.setTime(message.getTime());
//                String senderId = message.getSenderId();
////                retractMsg.setSenderId(senderId);
////                retractMsg.setSenderName(message.getSenderName());
//
////                MessageReference.saveByRoomId(roomId, Lists.newArrayList(message));
//                MessageReference.save(roomId, message);
//
//
//                MessageEntity iMessage = MessageReference.findById(messageId);
//                ChatRoomEntity iSession = ChatRoomReference.getInstance().findById(roomId);
//                if (iMessage != null && !MessageStatus.READ.equals(iMessage.getStatus())) {
//                    broadCastUpdateBadgeItem(-1);//左下角未读数
//                    String userId = TokenPref.getInstance(App.getAppContext()).getUserId();
//                    if (!senderId.equals(userId)) {
////                        iSession.setUnReadNum(iSession.getUnReadNum() > 0 ? iSession.getUnReadNum() - 1 : 0);
//                        //更新chat页面未读书
//                        App application = (App) App.getAppContext().getApplicationContext();
//                        String currentRoomId = UserPref.getInstance(App.getAppContext()).getCurrentRoomId();
//                        if (!roomId.equals(currentRoomId) && application.unReadNum > 0) {
//                            application.unReadNum--;
//                        }
//                    }
//                }
//                EventBusUtils.sendEvent(new EventMsg(MsgConstant.MSG_RECEIVED_FILTER, new ReceiverMessageBean(roomId, message)));
////                broadcastMsg(roomId, retractMsg);
//                if (iSession != null) {
////                if (iSession != null && iSession.getTime() <= iMessage.getSendTime()) {
////                    iSession.setSenderName(message.getSenderName());
////                    iSession.setContent(Constant.RETRACT_MSG);
////                    iSession.setFlag(message.getFlag() == null ? MessageFlag.OWNER.getFlag() : message.getFlag().getFlag());
////                    iSession.setSenderId(message.getSenderId());
////                    iSession.setLastMessageId(message.getId());
//                }
//                ChatRoomReference.getInstance().save(iSession);
////                broadcastSession(iSession);
//                EventBusUtils.sendEvent(new EventMsg(MsgConstant.SESSION_UPDATE_FILTER, iSession.getId()));
//            } else {
//                //TODO 未收到消息，撤回的情况
//            }
        }

        @Override
        public void squeezedOut() {
            //broadcastSqueezedOut();
        }

        @Override
        public void dismissGroupRoom(String roomId) {
            hangupCalling(roomId);
            CrowdEntity crowdEntity = DBManager.getInstance().queryGroup(roomId);
            if (crowdEntity != null) {
                DBManager.getInstance().deleteGroup(roomId);
                DBManager.getInstance().deleteGroupInfo(roomId);
            }

            //删除本地聊天室
            ChatRoomReference.getInstance().deleteById(roomId);
            DBManager.getInstance().deleteGroupInfo(roomId);
            //清空该聊天室本地消息
//            MessageReference.deleteByRoomId(roomId);
//            DBManager.getInstance().deleteMessageByRoomId(roomId);
//            ChatService.getInstance().broadCastUpdateBadgeItem(-removeEntity.getUnReadNum());
//
//
//
            broadCastCancelChat();
            broadcastGroupDismissRoom(roomId);
        }

        @Override
        public void userExit(String userId, String roomId, String roomName, String avatarUrl, List<String> deletedMemberIds) {
            if (!userId.equals(TokenPref.getInstance(App.getContext()).getUserId()) && deletedMemberIds.isEmpty()) {
                broadcastUserExit(roomId, roomName, avatarUrl, userId);
            } else if (!deletedMemberIds.isEmpty() && deletedMemberIds.contains(TokenPref.getInstance(App.getContext()).getUserId())) {
                EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.NOTICE_SELF_EXIT_ROOM, roomId));
            } else {
                // 自己從其它載具退出聊天室
                ChatRoomReference.getInstance().deleteById(roomId);
                DBManager.getInstance().deleteGroup(roomId);
                TodoService.unBindRoom(App.getContext(), roomId, null);
                EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.NOTICE_SELF_EXIT_ROOM, roomId));
                CELog.e("");
            }
        }

        @Override
        public void dismissSingleRoom(String userId) {
            broadCastCancelChat();
            broadcastSingleDismissRoom(userId);
        }

        @Override
        public void onAddFriend(String userId) {
            EventBusUtils.sendEvent(new EventMsg(MsgConstant.ADD_FRIEND_FILTER));
        }

        @Override
        public void onDeleteFriend(String userId, String roomId) {
            UserProfileEntity accountCE = new UserProfileEntity();
            accountCE.setId(userId);
            accountCE.setRoomId(roomId);

            EventBusUtils.sendEvent(new EventMsg(MsgConstant.REMOVE_FRIEND_FILTER, accountCE));
        }

        @Override
        public void onDeleteFromRoom(String userId, String roomId, List<String> deletedMemberIdsList) {
            String callRoomId = App.getInstance().getCallRoomId();
//            String myId = TokenSave.getInstance(App.getAppContext()).getUserId();
            String selfId = TokenPref.getInstance(App.getContext()).getUserId();
            if (!TextUtils.isEmpty(callRoomId) && callRoomId.equals(roomId) && deletedMemberIdsList.contains(selfId)) {
//                if (LinphoneService.getInstance().isWindowShowing()) {
//                    LinphoneService.getInstance().hangup();
//                } else {
//                    Activity topActivity = ActivityManager.getTopActivity();
//                    if (topActivity != null && topActivity instanceof ChatActivity) {
//                        ((ChatActivity) topActivity).hangup();
//                    }
//                }

                Activity topActivity = ActivityManager.getTopActivity();
                if (topActivity instanceof ChatActivity) {
//                    ((ChatActivity) topActivity).hangup();
                }
            }

            String userIdLocal = TokenPref.getInstance(App.getContext()).getUserId();
            if (deletedMemberIdsList != null && deletedMemberIdsList.size() == 1) {
                userId = deletedMemberIdsList.get(0);
            }

            //ChatActivity 已經有做了
//            if (!TextUtils.isEmpty(userId) && !TextUtils.isEmpty(roomId) && !userId.equals(userIdLocal)) {
            //删除的不是自己
//                AccountRoomRelReference.deleteRelByRoomIdAndAccountId(null, roomId, userId);
//                DBManager.getInstance().delAccount_Room(roomId, userId);
//            }

            EventBusUtils.sendEvent(new EventMsg(MsgConstant.GROUP_REFRESH_FILTER, new GroupRefreshBean(roomId, null, deletedMemberIdsList)));
        }

        @Override
        public void transfer_Owner(String ownerId, String roomId) {

            ChatRoomReference.getInstance().updateOwnerIdById(roomId, ownerId);
            DBManager.getInstance().updateGroupField(roomId, DBContract.GroupEntry.COLUMN_OWNER_ID, ownerId);
            String json = JsonHelper.getInstance().toJson(ImmutableMap.of(
                "key", "ownerId",
                "values", ownerId,
                "roomId", roomId));
            EventBusUtils.sendEvent(new EventMsg(MsgConstant.SESSION_REFRESH_FILTER, json));

        }

        @Override
        public void modifyRoomName(String newName, String roomId) {
//            boolean updateStatus = ChatRoomReference.getInstance().updateTitleById(roomId, newName);
//            if (updateStatus) {
//                EventBusUtils.sendEvent(new EventMsg(MsgConstant.CHANGE_LAST_MESSAGE, roomId));
//            }
            ChatRoomService.getInstance().getChatRoomItem(App.getContext(), TokenPref.getInstance(App.getContext()).getUserId(), roomId, RefreshSource.ALL, new ServiceCallBack<ChatRoomEntity, RefreshSource>() {
                @Override
                public void complete(ChatRoomEntity roomEntity, RefreshSource source) {
                    if (ChatRoomType.broadcast.equals(roomEntity.getType())) {
                        CELog.e("" + roomEntity);
                    } else {
                        switch (source) {
                            case ALL:
                                break;
                            case LOCAL:
                                roomEntity.setCustomName(true);
                                roomEntity.setName(newName);
                                ChatRoomReference.getInstance().save(roomEntity);
                                EventBusUtils.sendEvent(new EventMsg(MsgConstant.NOTICE_DISCUSS_ROOM_TITLE_UPDATE, roomId));
                                break;
                            case REMOTE:
                                ChatRoomEntity entity = ChatRoomReference.getInstance().findById2(TokenPref.getInstance(App.getContext()).getUserId(), roomId, false, false, false, false, false);
                                EventBusUtils.sendEvent(new EventMsg(MsgConstant.REFRESH_ROOM_BY_LOCAL, JsonHelper.getInstance().toJson(entity)));
                                break;
                        }
                    }
                }

                @Override
                public void error(String message) {
                    CELog.e(message);
                }
            });

//            try {

//                EventBusUtils.sendEvent(new EventMsg(MsgConstant.NOTICE_DISCUSS_ROOM_TITLE_UPDATE, roomId));
//            } catch (Exception e) {
//                CELog.e(e.getMessage());
//            }

//            EventBusUtils.sendEvent(new EventMsg(MsgConstant.GROUP_REFRESH_FILTER, new GroupRefreshBean(roomId, newName, null)));
//            EventBusUtils.sendEvent(new EventMsg(MsgConstant.SESSION_REFRESH_FILTER, new SessionFrefreshBean("name", newName, roomId)));
        }

        @Override
        public void UpgradeRoom(String newName, String roomId) {
            EventBusUtils.sendEvent(new EventMsg(MsgConstant.GROUP_UPGRADE_FILTER, new GroupUpgradeBean(roomId, newName)));
        }

        @Override
        public void otherDeviceLogin(String message) {
            Toast.makeText(App.getContext(), message, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onPublishMood(String userId, String mood) {
            UserProfileEntity account = DBManager.getInstance().queryFriend(userId);
            if (account != null) {
                account.setMood(mood);
                EventBusUtils.sendEvent(new EventMsg(MsgConstant.ACCOUNT_REFRESH_FILTER, account));
            }
        }


        @Override
        public void onCallEnd() {
        }


        @Override
        public void onSyncRead(String roomId, String messageId) {

            SyncReadBatchUpdateController.handler(App.getContext(), roomId, messageId);
            // EVAN_FLAG 2020-06-08 (1.11.0) 改call RoomItem or RoomRecent
//            EventBusUtils.sendEvent(new EventMsg(MsgConstant.REFRESH_FILTER));
            int debug = 0;
            if (debug == 0) {
                return;
            }

            MessageEntity entity = MessageReference.findByIdAndRoomId(null, messageId, roomId);
            if (entity != null) {
                if (!MessageFlag.OWNER_or_SEND_or_ARRIVED.contains(entity.getFlag())) {
                    return;
                }
            }

            String userId = TokenPref.getInstance(App.getContext()).getUserId();
            ChatRoomEntity syncEntity = ChatRoomReference.getInstance().findById2(userId, roomId, true, true, true, true, true);
//            ChatRoomEntity syncEntity = ChatRoomReference.getInstance().findById(roomId);
            if (syncEntity == null) {
                return;
            }
            if (ServiceNumberType.PROFESSIONAL.equals(syncEntity.getServiceNumberType()) && !Strings.isNullOrEmpty(syncEntity.getServiceNumberAgentId())) {
                return;
            }

            if (syncEntity.getUnReadNum() > 0) {
                MessageReference.updateReadFlagByRoomId(roomId);
                syncEntity.setUnReadNum(0);
                boolean saveStatus = ChatRoomReference.getInstance().save(syncEntity);
                ChatRoomService.getInstance().getBadge(App.getContext());
                if (saveStatus) {
                    EventBusUtils.sendEvent(new EventMsg(MsgConstant.SYNC_READ, syncEntity));
                }
                ApiManager.doMessagesRead(App.getContext(), roomId, Lists.newArrayList(messageId), null);
            }

//
//            MessageEntity readMessage = MessageReference.findById(messageId);
//            if (readMessage != null && !MessageFlag.READ.equals(readMessage.getFlag())) {
//                ChatRoomEntity syncEntity = ChatRoomReference.getInstance().findById(roomId);
//                if (ServiceNumberType.PROFESSIONAL.equals(syncEntity.getServiceNumberType()) && !Strings.isNullOrEmpty(syncEntity.getServiceNumberAgentId())) {
//                    return;
//                }
//
//                if (syncEntity.getUnReadNum() > 0) {
//                    syncEntity.setUnReadNum(0);
//                    boolean saveStatus = ChatRoomReference.getInstance().save(syncEntity);
//                    if (saveStatus) {
//                        EventBusUtils.sendEvent(new EventMsg(MsgConstant.SYNC_READ, syncEntity));
//                    }
//                    ApiManager.doMessagesRead(App.getAppContext(), roomId, null, null);
//                }
//            }

//            // EVAN_FLAG 2019-11-06 同步已讀 送 api
//            ChatRoomEntity mSession = DBManager.getInstance().querySession(roomId);
//            if (isPublicService(mSession)) {
//                ApiManager.getInstance().doMessageReadAllByRoomId(App.getAppContext(), mSession.getId(), new MessageReadRequest.ApiListener() {
//                    @Override
//                    public void onSuccess() {
//                        EventBusUtils.sendEvent(new EventMsg(MsgConstant.SYNC_READ, roomId));
//                    }
//
//                    @Override
//                    public void onFailed(String errorMessage) {
//
//                    }
//                });
//            } else {
//                EventBusUtils.sendEvent(new EventMsg(MsgConstant.SYNC_READ, roomId));
//            }
        }


        // EVAN_FLAG 2019-11-14 新增服務號接聽斷線事件
        @Override
        public void onServiceRegisterAgent(String roomId, String serviceNumberAgentId) {
            ChatRoomReference.getInstance().updateServiceNumberAgentIdById(roomId, serviceNumberAgentId);
            Map<String, String> data = Maps.newHashMap(ImmutableMap.of("roomId", roomId, "serviceNumberAgentId", serviceNumberAgentId));
            EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.SERVICE_NUMBER_PERSONAL_START, data));
        }

        @Override
        public void onServiceReleaseAgent(String roomId) {
            ChatRoomReference.getInstance().updateServiceNumberAgentIdById(roomId, "");
            EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.SERVICE_NUMBER_PERSONAL_STOP, roomId));
        }

        @Override
        public void onCheckingAppointStatus(String roomId) {
            ChatRoomEntity entity = ChatRoomReference.getInstance().findById(roomId);
            if (entity != null)
                EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.SERVICE_NUMBER_PERSONAL_STOP, roomId));
        }

        /**
         * 多人聊天室被物件綁定
         */
        @Override
        public void onBusinessBindingRoom(String roomId, String businessId, String businessName, String businessCode) {
            boolean status = ChatRoomReference.getInstance().updateBusinessContent(roomId,
                new BusinessContent(businessId, businessName, BusinessCode.of(businessCode)));

            if (status) {
                Map<String, String> data = Maps.newHashMap(ImmutableMap.of("roomId", roomId, "businessId", businessId, "businessName", businessName, "businessCode", businessCode));
//                ChatRoomEntity entity = ChatRoomReference.getInstance().findById2(TokenPref.getInstance(App.getAppContext()).getUserId(), roomId, false, false, false, false, false);
                EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.BUSINESS_BINDING_ROOM_EVENT, data));
            }
        }

        /**
         * 多人 or 好友取消物件聊天室
         */
        @Override
        public void onDismissBusinessRoom(String userId, String roomId, String type) {
            ChatRoomEntity entity = ChatRoomReference.getInstance().findById2(userId, roomId, true, true, true, true, true);
            if (entity != null && !Strings.isNullOrEmpty(entity.getBusinessId())) {
                if (ChatRoomType.discuss.equals(entity.getType())) {
                    EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.SESSION_UPDATE_FILTER, JsonHelper.getInstance().toJson(entity)));
                } else {
                    EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.SESSION_REMOVE_FILTER, roomId));
                }
            }
        }

        @Override
        public void onTopRoom(String roomId, long topTime) {
            boolean status = ChatRoomReference.getInstance().updateTopAndTopTimeById(roomId, true, topTime);
            if (status) {
                String data = JsonHelper.getInstance().toJson(Maps.newHashMap(ImmutableMap.of("id", roomId, "topTime", topTime, "isTop", true)));
                EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.CHANGE_TOP_ROOM, data));
            }
        }

        @Override
        public void onCancelTopRoom(String roomId) {
            boolean status = ChatRoomReference.getInstance().updateTopAndTopTimeById(roomId, false, 0L);
            if (status) {
                String data = JsonHelper.getInstance().toJson(Maps.newHashMap(ImmutableMap.of("id", roomId, "topTime", 0L, "isTop", false)));
                EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.CHANGE_TOP_ROOM, data));
            }

        }

        @Override
        public void onMuteRoom(String roomId, boolean isMute) {
            boolean status = ChatRoomReference.getInstance().updateMuteById(roomId, isMute);
            String userId = TokenPref.getInstance(App.getContext()).getUserId();
            ChatRoomEntity entity = ChatRoomReference.getInstance().findById2(userId, roomId, true, true, true, true, true);
            if (status && entity != null) {
                EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.CHANGE_MUTE_ROOM, JsonHelper.getInstance().toJson(entity)));
            }
        }

        @Override
        public void onMuteUser(String userId, boolean isMute) {
            TokenPref.getInstance(App.getContext()).setMute(isMute);
            EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.CHANGE_MUTE_USER, ""));
        }

        @Override
        public void onTodoEvent(String id, String userId, String osType, String event) {
            if (AppConfig.osType.equals(osType)) {
                return;
            }

            if (event.equals("Delete")) {
                TodoEntity entity = TodoReference.findById(null, id);
                if (entity != null) {
                    entity.setStatus(TodoStatus.DELETED);
                    entity.setProcessStatus(ProcessStatus.UNDEF);
                    EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.UI_NOTICE_TODO_DELETE_ALARM, entity.toJson()));
                    EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.UI_NOTICE_TODO_REFRESH, entity.toJson()));
                    TodoReference.delete(null, id);
                }
                return;
            }
            TodoService.getItem(App.getContext(), id, RefreshSource.REMOTE, new ServiceCallBack<TodoEntity, RefreshSource>() {
                @Override
                public void complete(TodoEntity entity, RefreshSource source) {
                    if (entity == null) {
                        return;
                    }
                    entity.setProcessStatus(ProcessStatus.UNDEF);
                    TodoStatus status = entity.getStatus();
                    switch (status) {
                        case DONE:
                        case PROGRESS:
                            boolean saveStatus = TodoReference.save(null, entity);
                            if (saveStatus) {
                                TodoEntity localEntity = TodoReference.findById(null, entity.getId());
                                if (localEntity != null) {
                                    EventBusUtils.sendEvent(new EventMsg<>(TodoStatus.DONE.equals(status) ? MsgConstant.UI_NOTICE_TODO_DELETE_ALARM : MsgConstant.UI_NOTICE_TODO_UPDATE_ALARM, localEntity.toJson()));
                                }
                            }
                            break;
                        case DELETED:
                            boolean deleteStatus = TodoReference.delete(null, id);
                            if (deleteStatus) {
                                EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.UI_NOTICE_TODO_DELETE_ALARM, entity.toJson()));
                            }
                            break;
                    }
                    EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.UI_NOTICE_TODO_REFRESH, entity.toJson()));
                }

                @Override
                public void error(String message) {

                }
            });
        }

        // EVAN_FLAG 2020-08-25 (1.12.0) 廣播訊息 delete or update
        @Override
        public void onBroadcastEvent(String userId, String roomId, String messageId, String event, String osType) {
            BroadcastEvent broadcastEvent = BroadcastEvent.of(event);
            MessageEntity entity = MessageReference.findByIdAndRoomId(null, messageId, roomId);
            if (entity == null) {
                RepairMessageService.setQueue(App.getContext(), roomId, Sets.newHashSet(entity.getId()), false);
            } else {
                if (BroadcastEvent.DELETE.equals(broadcastEvent)) {
                    entity.setBroadcastFlag(BroadcastFlag.DELETED);
                    MessageReference.updateBroadcastFlag(null, roomId, messageId, BroadcastFlag.DELETED);
                    EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.NOTICE_APPEND_MESSAGE, JsonHelper.getInstance().toJson(entity)));
                }
                if (BroadcastEvent.UPDATE.equals(broadcastEvent)) {
                    EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.NOTICE_APPEND_MESSAGE, JsonHelper.getInstance().toJson(entity)));
                }
            }
        }

        @Override
        public void onServiceNumberMemberEvent(String serviceNumberId, String event, Set<String> memberIds) {
            if (event.equals(Const.DELETE)) {
                if (memberIds.contains(TokenPref.getInstance(App.getContext()).getUserId())) {
                    if (DBManager.getInstance().deleteServiceNumber(serviceNumberId) && ChatRoomReference.getInstance().deleteByServiceNumberId(serviceNumberId)) {
                        EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.DELETE_SERVICE_NUMBER_MEMBER, serviceNumberId));
                    } else
                        CELog.e("Kyle1 delete serviceNumberId = " + serviceNumberId + " failure");
                } else {
                    // 如果不是自己 通知刪除其他人
                    EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.DELETE_SERVICE_NUMBER_OTHER_MEMBER, memberIds));
                }
            } else if (event.equals(Const.ADD) || event.equals(Const.ADD_FROM_PROVISIONAL)) {
                //前端收到Socket通知 Ce.Notice.ServiceNumberMember 時，檢查自己是不是被新增的Member，調用新API，參數帶serviceNumberId去同步這個服務號全部的未結束服務聊天室
                if (memberIds.contains(TokenPref.getInstance(App.getContext()).getUserId())) {
                    ChatServiceNumberService.findServiceNumber(App.getContext(), serviceNumberId, RefreshSource.REMOTE, new ServiceCallBack<ServiceNumberEntity, RefreshSource>() {
                        @Override
                        public void error(String message) {

                        }

                        @Override
                        public void complete(ServiceNumberEntity serviceNumberEntity, RefreshSource refreshSource) {
                            EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.NOTICE_SERVICE_NUMBER_REFRESH_BY_DB));
                            getServiceMemberActiveList(serviceNumberId);
                        }
                    });
                }
            } else if (event.equals(Const.UPDATE)) {
                EventBus.getDefault().post(new EventMsg<>(MsgConstant.SERVICE_NUMBER_UPDATE, serviceNumberId));
            } else EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.ADD_SERVICE_NUM_FILTER));

            ChatServiceNumberService.updateServiceNumberMember(serviceNumberId);//更新本地服務號成員列表
        }

        private void getServiceMemberActiveList(String serviceNumberId) {
            try {
                List<String> serviceNumberIdList = Lists.newArrayList();
                serviceNumberIdList.add(serviceNumberId);

                ChatRoomService.getInstance().getServiceMemberActiveList(App.getContext(), serviceNumberIdList, 0L, new ApiCallback<List<ChatRoomEntity>>() {
                    @Override
                    public void error(String message) {
                    }

                    @Override
                    public void complete(List<ChatRoomEntity> entities) {
                    }

                    @Override
                    public void finish() {
                        EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.NOTICE_SERVICE_NUMBER_REFRESH_BY_DB));
                    }
                });
            } catch (Exception e) {

            }
        }

        // EVAN_FLAG 2020-08-25 (1.12.0) 廣播訊息調度狀態通知
        @Override
        public void onBroadcastAssignEvent(String roomId, String messageId, String event) {
            BroadcastEvent broadcastEvent = BroadcastEvent.of(event);
            MessageEntity entity = MessageReference.findByIdAndRoomId(null, messageId, roomId);
            if (entity == null) {
                RepairMessageService.setQueue(App.getContext(), roomId, Sets.newHashSet(entity.getId()), false);
            } else {
                if (BroadcastEvent.ASSIGN_START.equals(broadcastEvent)) {
                    entity.setBroadcastFlag(BroadcastFlag.DISPATCHING);
                    EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.NOTICE_BROADCAST_FLAG_STATUS, entity.toJson()));
                }
                if (BroadcastEvent.ASSIGN_COMPLETE.equals(broadcastEvent)) {
                    entity.setBroadcastFlag(BroadcastFlag.DOME);
                    EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.NOTICE_BROADCAST_FLAG_STATUS, entity.toJson()));
                }
            }
        }

        @Override
        public void onServiceNumberTransferEvent(String userId, String roomId, String serviceNumberId, String event) {
            EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.SERVICE_NUMBER_TRANSFER_STATUS, roomId));
        }


        @Override
        public void onServiceNumberConsultEvent(String userId, String srcRoomId, String consultRoomId, String event) {
            if ("ConsultComplete".equals(event)) {
                ChatRoomReference.getInstance().updateConsultRoomIdById(consultRoomId, "");
            } else {
                ChatRoomReference.getInstance().updateConsultRoomIdById(consultRoomId, srcRoomId);
            }
            Map<String, String> data = ImmutableMap.of(
                "srcRoomId", srcRoomId,
                "consultRoomId", consultRoomId,
                "userId", userId,
                "event", event
            );
            EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.NOTICE_SERVICE_NUMBER_CONSULT_EVENT, JsonHelper.getInstance().toJson(data)));
        }

        @Override
        public void createNewServiceNumber(String type) {
            if (type.equals("serviceMember")) {
                ApiManager.doSyncEmployee(App.getContext(), null);
                ApiManager.doSyncContact(App.getContext(), null);
                ApiManager.doSyncServiceNumber(App.getContext(), null);
                EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.SERVICE_NUMBER_PERSONAL_START));
            } else {
                EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.NOTICE_CREATE_ROOM));
            }
        }

        @Override
        public void dismissServiceNumberRoom(String roomId) {
            ChatRoomReference.getInstance().deleteById(roomId);
            DBManager.getInstance().deleteServiceNum(roomId);
            EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.NOTICE_DISABLE_SERVICE_NUMBER, roomId));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleEvent(EventMsg eventMsg) {
        if (eventMsg.getCode() == MsgConstant.SERVICE_NUMBER_TRANSFER_STATUS) {
            try {
                createTransferFloatWindow((JSONObject) eventMsg.getData()); //被換手會收到, 但JSONObject可能會轉換失敗
            } catch (Exception ignored) {

            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void createTransferFloatWindow(JSONObject object) throws Exception {
        String event = object.getString("event");
        if (!"TransferStart".equals(event)) return;

        String myUserId = TokenPref.getInstance(App.getContext()).getUserId();
        if (myUserId.equals(object.getString("userId"))) { //代表是自己則不顯示
            return;
        }
        String roomId = object.getString("roomId");
        if (roomId.equals(App.getInstance().chatRoomId)) return; //已經在對象房間時則不顯示

        String customerName = object.getString("customerName");
        String serviceNumberAvatarId = object.getString("serviceNumberAvatarId");
        String serviceNumberName = object.getString("serviceNumberName");
        String transferReason = object.getString("transferReason");

        WindowManager windowManager = (WindowManager) App.getContext().getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        params.width = UiHelper.getDisplayWidth(App.getContext()) - UiHelper.dip2px(App.getContext(), 16);
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.y = 100;
        params.gravity = Gravity.TOP | Gravity.CENTER;
        params.dimAmount = 0.2f;
        params.flags = (WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        params.format = PixelFormat.TRANSLUCENT;

        FloatWindowTransferAlertBinding binding = DataBindingUtil.inflate(LayoutInflater.from(App.getContext()),
            R.layout.float_window_transfer_alert, null, false);

        AvatarService.post(App.getContext(), serviceNumberAvatarId, PicSize.SMALL, binding.img, R.drawable.custom_default_avatar);
        binding.txtName.setText(serviceNumberName);
        binding.txtMessage.setText(MessageFormat.format("({0})需接續服務", customerName));
        binding.txtMessage.setOnClickListener(v -> {
            ActivityTransitionsControl.navigateToChat(App.getContext(), roomId, (intent, s) -> {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                IntentUtil.INSTANCE.start(App.getContext(), intent);
                windowManager.removeView(binding.getRoot());
            });
        });
        binding.txtReason.setText(transferReason);

        //向上滑關閉
        binding.clRoot.setOnTouchListener(new View.OnTouchListener() {
            private float y;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        y = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        if (y - event.getY() > 90) {
                            RingtoneHelper.stop(App.getContext());
                            windowManager.removeView(binding.getRoot());
                        }
                        v.performClick();
                        break;
                }
                v.performClick();
                return false;
            }
        });
        windowManager.addView(binding.getRoot(), params);
        //設定多久後自動關掉, 暫定5分鐘
        new CountDownTimer(5 * 60 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                try { //如果視窗已經關閉則會crash
                    RingtoneHelper.stop(App.getContext());
                    windowManager.removeView(binding.getRoot());
                } catch (Exception ignored) {
                }
            }
        }.start();
    }
}
