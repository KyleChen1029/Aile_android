//package tw.com.chainsea.chat.presenter;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.graphics.Bitmap;
//import android.net.Uri;
//import android.os.Handler;
//import android.provider.MediaStore;
//import android.text.SpannableStringBuilder;
//import android.text.TextUtils;
//import android.util.Log;
//import android.webkit.URLUtil;
//
//import com.bumptech.glide.Glide;
//import com.google.common.base.Strings;
//import com.google.common.collect.ImmutableMap;
//import com.google.common.collect.Lists;
//import com.google.common.collect.Maps;
//import com.google.gson.reflect.TypeToken;
//
//import org.json.JSONObject;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.lang.ref.WeakReference;
//import java.lang.reflect.Type;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.HashSet;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.concurrent.atomic.AtomicReference;
//
//import tw.com.chainsea.chat.util.DaVinci;
//import cn.hadcn.davinci.image.base.ImageEntity;
//import okhttp3.MediaType;
//import okhttp3.MultipartBody;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.RequestBody;
//import okhttp3.ResponseBody;
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//import tw.com.aile.sdk.bean.message.MessageEntity;
//import tw.com.chainsea.android.common.client.type.FileMedia;
//import tw.com.chainsea.android.common.client.type.Media;
//import tw.com.chainsea.android.common.image.BitmapHelper;
//import tw.com.chainsea.android.common.json.JsonHelper;
//import tw.com.chainsea.android.common.log.CELog;
//import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
//import tw.com.chainsea.android.common.video.IVideoSize;
//import tw.com.chainsea.ce.sdk.bean.FacebookTag;
//import tw.com.chainsea.ce.sdk.bean.MsgStatusBean;
//import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
//import tw.com.chainsea.ce.sdk.bean.msg.ChannelType;
//import tw.com.chainsea.ce.sdk.bean.msg.FacebookCommentStatus;
//import tw.com.chainsea.ce.sdk.bean.msg.FacebookPostStatus;
//import tw.com.chainsea.ce.sdk.bean.msg.MessageFlag;
//import tw.com.chainsea.ce.sdk.bean.msg.MessageStatus;
//import tw.com.chainsea.ce.sdk.bean.msg.MessageType;
//import tw.com.chainsea.ce.sdk.bean.msg.MsgKitAssembler;
//import tw.com.chainsea.ce.sdk.bean.msg.SourceType;
//import tw.com.chainsea.ce.sdk.bean.msg.content.AtContent;
//import tw.com.chainsea.ce.sdk.bean.msg.content.BusinessContent;
//import tw.com.chainsea.ce.sdk.bean.msg.content.FileContent;
//import tw.com.chainsea.ce.sdk.bean.msg.content.IMessageContent;
//import tw.com.chainsea.ce.sdk.bean.msg.content.ImageContent;
//import tw.com.chainsea.ce.sdk.bean.msg.content.MentionContent;
//import tw.com.chainsea.ce.sdk.bean.msg.content.StickerContent;
//import tw.com.chainsea.ce.sdk.bean.msg.content.TextContent;
//import tw.com.chainsea.ce.sdk.bean.msg.content.UndefContent;
//import tw.com.chainsea.ce.sdk.bean.msg.content.VideoContent;
//import tw.com.chainsea.ce.sdk.bean.msg.content.VoiceContent;
//import tw.com.chainsea.ce.sdk.bean.parameter.Sort;
//import tw.com.chainsea.ce.sdk.bean.response.AileTokenApply;
//import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
//import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
//import tw.com.chainsea.ce.sdk.bean.room.QuickReplyItem;
//import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberType;
//import tw.com.chainsea.ce.sdk.config.AppConfig;
//import tw.com.chainsea.ce.sdk.database.DBManager;
//import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
//import tw.com.chainsea.ce.sdk.event.EventBusUtils;
//import tw.com.chainsea.ce.sdk.event.EventMsg;
//import tw.com.chainsea.ce.sdk.event.MsgConstant;
//import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
//import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
//import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
//import tw.com.chainsea.ce.sdk.http.ce.request.TokenApplyRequest;
//import tw.com.chainsea.ce.sdk.http.ce.request.UploadManager;
//import tw.com.chainsea.ce.sdk.lib.ErrCode;
//import tw.com.chainsea.ce.sdk.network.NetworkManager;
//import tw.com.chainsea.ce.sdk.network.model.common.CommonResponse;
//import tw.com.chainsea.ce.sdk.network.model.request.CheckCommentStatusRequest;
//import tw.com.chainsea.ce.sdk.network.model.request.MessageItemRequest;
//import tw.com.chainsea.ce.sdk.network.model.request.SendAtMessageReadRequest;
//import tw.com.chainsea.ce.sdk.network.model.request.SendAtMessageRequest;
//import tw.com.chainsea.ce.sdk.network.model.request.SendFacebookCommentRequest;
//import tw.com.chainsea.ce.sdk.network.model.response.CheckCommentResponse;
//import tw.com.chainsea.ce.sdk.network.model.response.RobotServiceResponse;
//import tw.com.chainsea.ce.sdk.network.model.response.SendFacebookCommentResponse;
//import tw.com.chainsea.ce.sdk.network.model.response.ServicesIdentityListResponse;
//import tw.com.chainsea.ce.sdk.reference.ChatRoomReference;
//import tw.com.chainsea.ce.sdk.reference.MessageReference;
//import tw.com.chainsea.ce.sdk.reference.UserProfileReference;
//import tw.com.chainsea.ce.sdk.service.ChatMessageService;
//import tw.com.chainsea.ce.sdk.service.ChatRoomService;
//import tw.com.chainsea.ce.sdk.service.ChatServiceNumberService;
//import tw.com.chainsea.ce.sdk.service.FileService;
//import tw.com.chainsea.ce.sdk.service.RepairMessageService;
//import tw.com.chainsea.ce.sdk.service.listener.AServiceCallBack;
//import tw.com.chainsea.ce.sdk.service.listener.AgentSnatchCallback;
//import tw.com.chainsea.ce.sdk.service.listener.RoomRecentCallBack;
//import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
//import tw.com.chainsea.ce.sdk.service.type.ChatRoomSource;
//import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
//import tw.com.chainsea.chat.R;
//import tw.com.chainsea.chat.config.BundleKey;
//import tw.com.chainsea.chat.keyboard.view.HadEditText;
//import tw.com.chainsea.chat.lib.AtMatcherHelper;
//import tw.com.chainsea.chat.lib.ChatService;
//import tw.com.chainsea.chat.lib.NetworkUtils;
//import tw.com.chainsea.chat.lib.PictureParse;
//import tw.com.chainsea.chat.lib.Tools;
//import tw.com.chainsea.chat.messagekit.lib.AudioLib;
//import tw.com.chainsea.chat.ui.ife.IChatView;
//import tw.com.chainsea.custom.view.alert.AlertView;
//
///**
// * ChatPresenter
// * Created by 90Chris on 2016/4/21.
// */
//public class ChatPresenter {
//    private final IChatView iView;
//    private final String token;
//    //    private String sessionId;
//    private  String userId;
//    private final List<String> mReceivedNoticeIds;
//    // Used to store messages received when the chat page is not visible
//    private final List<String> mUnReadMsgIds = Lists.newArrayList();
//    private ChatRoomEntity session;
//    private int getMessageListFrequency;
//    private int unreadNum = -1;
//    private String unreadMessageId;
//    private final List<Integer> mLogoBeans = Lists.newArrayList();
//    private final int[] logos = {R.drawable.share_facebook, R.drawable.share_line, R.drawable.share_twitter,
//            R.drawable.share_twitter, R.drawable.share_wechat, R.drawable.share_wechatfavorite,
//            R.drawable.share_wechatmoments, R.drawable.share_qq, R.drawable.share_alipay,
//            R.drawable.share_gmail};
//    private boolean isVisible;
//    //    public static IWXAPI api;
//    private static final int THUMB_SIZE = 150;
//
//    private final UserProfileEntity mSelfAccount;
//    public boolean mIsCancel = false;
//    private final IntentFilter intentFilter;
//    private static final String ACTION_TIMEZONE_CHANGED = Intent.ACTION_TIMEZONE_CHANGED;
//    private final TimeZoneChangeReceiver mTimeZoneChangeReceiver;
//    public boolean recordMode;
//    private MessageEntity recordMsg, nearMessage, themeMessage;
//    public boolean isObserverKeboard = true;
//
//    private final WeakReference<Context> weakReference;
//
//    private final HashSet<String> tempFacebookAlreadyCheckStatus = new HashSet<>();
//    private final LinkedList<String> checkCommentQueue = new LinkedList<>();
//    private boolean isTokenNeedRefresh = false;
//
//    public ChatPresenter(ChatRoomEntity chatRoomEntity, IChatView iView, String unreadMessageId, Context context) {
//        this.weakReference = new WeakReference<>(context);
//        this.iView = iView;
//        this.unreadMessageId = unreadMessageId;
//        this.session = chatRoomEntity;
//        if(session != null) unreadNum = this.session.getUnReadNum();
//        mReceivedNoticeIds = new ArrayList<>();
//        userId = TokenPref.getInstance(this.iView.getCtx()).getUserId();
//        token = TokenPref.getInstance(this.iView.getCtx()).getTokenId();
//        mSelfAccount = DBManager.getInstance().querySelfAccount(userId);
//
//        // Broadcast receiver that dynamically accepts network changes
//        intentFilter = new IntentFilter();
//        intentFilter.addAction("android.intent.action.TIMEZONE_CHANGED");
//        mTimeZoneChangeReceiver = new TimeZoneChangeReceiver();
//        this.iView.getCtx().registerReceiver(mTimeZoneChangeReceiver, intentFilter);
//    }
//
//    public boolean isRecordMode() {
//        return recordMode;
//    }
//
//    public void setRecordMode(boolean recordMode) {
//        this.recordMode = recordMode;
//    }
//
//    public void init(MessageEntity recordMsg, String keyWord) {
//        this.recordMode = recordMsg != null;
//        this.recordMsg = recordMsg;
//        if (this.recordMsg != null) {
//            this.recordMsg.setAnimator(true); //搜尋訊息晃動效果
//        }
//
//        iView.initMessageList();
//        iView.initKeyboard();
//        iView.initListener();
//
//        mUnReadMsgIds.clear();
//
//        if(session == null) return;
//        if (recordMode) {
//            isObserverKeboard = false;
//            findMessageFromByLocalDataBaseASC(session);
//            loadMoreMsgDB();
//            new Handler().postDelayed(() -> isObserverKeboard = true, 500);
//            iView.setSearchKeyWord(keyWord);
//        } else {
//            if (!NetworkUtils.isNetworkAvailable(iView.getCtx())) {
//                CELog.w("當前無網絡連接，請檢查您的網絡");
//                highLightUnReadLine(unreadNum > 0);
//                disPlayMessageFromDatabase();
//                return;
//            }
////            disPlayMessageFromDatabase();
//            String lastReadMsgId = DBManager.getInstance().queryLastReadMsgId(session.getId(), userId);
//            ChatMessageService.getChatMessageEntities(weakReference.get(), session.getId(), lastReadMsgId, Sort.DESC, new ServiceCallBack<List<MessageEntity>, RefreshSource>() {
//                @Override
//                public void error(String message) {
//                    iView.showIsNotMemberMessage(message);
//                    highLightUnReadLine(unreadNum > 0);
//                }
//
//                @Override
//                public void complete(List<MessageEntity> messageEntities, RefreshSource refreshSource) {
//                    CELog.d("Enter the chat room, have data locally, read messages from the server:" + messageEntities.size());
//                    ThreadExecutorHelper.getIoThreadExecutor().execute(() -> {
//                        checkIsFacebookAndOverTime(messageEntities);
//                        checkFacebookReplyType(messageEntities);
//                        getMessageListFrequency++;
//                        if (getMessageListFrequency > 1) {
//                            iView.highLightUnReadLine(unreadNum > 0);
//                            unreadNum = -1;
//                        }
//                        if (messageEntities.isEmpty()) {
//                            return;
//                        }
//                        List<String> newMsgIds = Lists.newArrayList();
////                        MessageReference.saveByRoomId(session.getId(), messageEntities);
//                        List<MessageEntity> entities2 = MessageReference.findByRoomIdAndTypeAndSortAndLimitAndInnerJoniAliasName(session.getId(), -1, session.getType(), MessageReference.Sort.DESC, 50);
//                        if(entities2.isEmpty()) return;
//                        for (int i = entities2.size() - 1; i >= 0; i--) {
//                            MessageEntity msg = entities2.get(i);
//                            // As long as it is read, it will not be sent
//                            if (!MessageStatus.READ.equals(msg.getStatus()) && !MessageStatus.IS_REMOTE.equals(msg.getStatus())) {
//                                newMsgIds.add(msg.getId());
//                            }
//                            ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
//                                iView.displayMainMessage(false, false, true, msg, false);
//                            });
//                        }
//                        MessageEntity message = entities2.get(0);
//                        // 設置最後訊息如果有 quick reply
//                        try {
//                            Type listType = new TypeToken<List<QuickReplyItem>>() {
//                            }.getType();
//                            List<QuickReplyItem> quickReplyItemList = JsonHelper.getInstance().from(
//                                    new JSONObject(message.getContent())
//                                            .optJSONObject("quickReply")
//                                            .optJSONArray("items").toString(), listType);
//                            iView.setQuickReply(quickReplyItemList);
//                        } catch (Exception e) {
//                            CELog.e("Quick Reply Parse Error", e);
//                        }
//
//
//                        if (session.getServiceNumberOpenType() != null && session.getServiceNumberOpenType().contains("O")) {
//                            checkFacebookReplyType(entities2);
//                            // 如果是 FB 回覆 超過 7天無法私訊回覆
//                            checkIsFacebookAndOverTime(entities2);
//                        }
//
////                        if (!newMsgIds.isEmpty() || unreadNum > 0) {
//                            ChatMessageService.doMessageReadAllByRoomId(iView.getCtx(), session, session.getUnReadNum(), newMsgIds, false);
////                        }
//                    });
//                }
//            });
//        }
//    }
//
//    public void checkFacebookReplyType(List<MessageEntity> messageList) {
//        for (MessageEntity message: messageList) {
//            if (message.getTag() != null && !message.getTag().isEmpty()) {
//                FacebookTag facebookTag = JsonHelper.getInstance().from(message.getTag(), FacebookTag.class);
//                if (facebookTag == null) continue;
//                if (facebookTag.getData() == null) continue;
//                if (facebookTag.getData().getReplyType() == null) continue;
//                if (facebookTag.getData().getReplyType().isEmpty()) continue;
//                List<MessageEntity> roomMessageList = MessageReference.findByRoomId(session.getId());
//                for (MessageEntity roomMessage: roomMessageList) {
//                    if (roomMessage.getTag() == null) continue;
//                    FacebookTag roomFacebookTag = JsonHelper.getInstance().from(roomMessage.getTag(), FacebookTag.class);
//                    if (roomFacebookTag == null) continue;
//                    if (roomFacebookTag.getData() == null) continue;
//                    if (roomFacebookTag.getData().getCommentId() == null) continue;
//                    if (roomFacebookTag.getData().getCommentId().equals(facebookTag.getData().getCommentId())
//                            && (roomFacebookTag.getData().getReplyType() == null ||roomFacebookTag.getData().getReplyType().isEmpty())) {
//                        message.setThemeId(roomMessage.getId());
//                        message.setNearMessageId(roomMessage.getId());
//                        message.setNearMessageType(roomMessage.getType());
//                        message.setNearMessageAvatarId(roomMessage.getAvatarId());
//                        message.setNearMessageContent(roomMessage.getContent());
//                        message.setNearMessageSenderId(roomMessage.getSenderId());
//                        message.setNearMessageSenderName(roomMessage.getSenderName());
//                        MessageReference.updateNearMessage(message);
//                    }
//                }
//            }
//        }
//    }
//
//    private void checkIsFacebookAndOverTime(List<MessageEntity> messageList) {
//        for (MessageEntity message: messageList) {
//            if (message.getFrom() == ChannelType.FB && message.getType() == MessageType.TEMPLATE) {
//                long time = System.currentTimeMillis() - (message.getSendTime());
//                long days = TimeUnit.MILLISECONDS.toDays(time);
//                if (days >= 7) {
//                    iView.showFacebookOverTimeView();
//                } else {
//                    iView.setFacebookKeyboard();
//                }
//                return;
//            }
//        }
//    }
//
//    public void switchIdentity(ServicesIdentityListResponse servicesIdentityListResponse) {
//        mSelfAccount.setAvatarId(servicesIdentityListResponse.getAvatarId());
//        mSelfAccount.setName(servicesIdentityListResponse.getName());
//    }
//
//    public void sendNoticeReaded() {
//        if (session != null && mUnReadMsgIds.size() > 0) {
//            ChatMessageService.doMessageReadAllByRoomId(iView.getCtx(), session, session.getUnReadNum(), mUnReadMsgIds, false);
////            ChatService.getInstance().doMessagesRead(session.getId(), mUnReadMsgIds);
//            mUnReadMsgIds.clear();
//        }
//    }
//
//    public void searchMoreMsg(MessageEntity entity) {
//        if (entity == null && session != null) {
//            messageListRequestDesc(session.getId(), session.getType(), "");
//            return;
//        }
//
//        List<MessageEntity> entities = MessageReference.findByRoomIdAndTypeAndSortAndLimitAndInnerJoniAliasName(session.getId(), entity.getSendTime() - 1, session.getType(), MessageReference.Sort.DESC, 200);
//        if (entities.isEmpty() || entities.size() <= 1) {
//            //message/list抓後台資料
//            if(session != null) messageListRequestDesc(session.getId(), session.getType(), entity, 200);
//            iView.showNoMoreMessage();
//        } else if (entities.size() <= 200) {
//            Collections.sort(entities);
//            iView.onRefreshMore(entities, entity);
//            if(session != null) messageListRequestDesc(session.getId(), session.getType(), entities.get(0), 200);
//        } else
//            iView.onRefreshMore(entities, entity);
//    }
//    // It is found that the Message SendTime is the same and cannot be fetched correctly
//    public void refreshMoreMsg(MessageEntity entity) {
//        if (entity == null && session != null) {
//            messageListRequestDesc(session.getId(), session.getType(), "");
//            return;
//        }
//
//        List<MessageEntity> entities = MessageReference.findByRoomIdAndTypeAndSortAndLimitAndInnerJoniAliasName(session.getId(), entity.getSendTime() - 1, session.getType(), MessageReference.Sort.DESC, 50);
//        if (entities.isEmpty() || entities.size() <= 1) {
//            //message/list抓後台資料
//            if(session != null) messageListRequestDesc(session.getId(), session.getType(), entity, 50);
//        } else if (entities.size() <= 50) {
//            Collections.sort(entities);
//            iView.onRefreshMore(entities, entity);
//            if(session != null) messageListRequestDesc(session.getId(), session.getType(), entities.get(0), 50);
//        } else
//            iView.onRefreshMore(entities, entity);
//    }
//
//    private final List<MessageEntity> loadMoreMsgs = Lists.newArrayList();
//
//    public void loadMoreMsgDB() {
//        if(session == null) return;
//        if (loadMoreMsgs.isEmpty()) {
//            long lastMsgTime = iView.getLastMsgTime();
//            List<MessageEntity> messages = MessageReference.findByRoomIdAndTypeAndSortAndLimitAndInnerJoniAliasName(session.getId(), lastMsgTime, session.getType(), MessageReference.Sort.ASC, 20);
//            loadMoreMsgs.addAll(messages);
//        } else {
//            long time = loadMoreMsgs.get(loadMoreMsgs.size() - 1).getSendTime();
//            List<MessageEntity> messages = MessageReference.findByRoomIdAndTypeAndSortAndLimitAndInnerJoniAliasName(session.getId(), time, session.getType(), MessageReference.Sort.ASC, 20);
//            loadMoreMsgs.clear();
//            loadMoreMsgs.addAll(messages);
//        }
//    }
//
//    public void displayMore() {
//        if (!loadMoreMsgs.isEmpty()) {
//            iView.onLoadMoreMsg(loadMoreMsgs);
//            loadMoreMsgDB();
//        }
//    }
//
//    public void onReceive(MessageEntity receiverMessage) {
//        // Send read notification
//        String sessionId = receiverMessage.getRoomId();
//
//        // Handle the online and offline status of the service account, identify the temporary string, and add type to the jocket response data format to be repaired
//        iView.doChannelOnLineStatus(sessionId, receiverMessage);
//        if (session != null && this.session.getId().equals(sessionId)) {
//            mReceivedNoticeIds.clear();
//            mReceivedNoticeIds.add(receiverMessage.getId());
//            if (isVisible) {
//                if (session.getType().equals(ChatRoomType.friend)) {
//                    if (!receiverMessage.getSenderId().equals(userId)) {
//                        // After receiving the message, set all the previously arrived messages as read
//                        List<MessageEntity> iMessages = DBManager.getInstance().queryMessagesByMsgStatus(this.session.getId(), MessageStatus.RECEIVED);
//                        if (iMessages != null) {
//                            for (MessageEntity msg : iMessages) {
//                                mReceivedNoticeIds.add(msg.getId());
//                                msg.setReadedNum(1);
//                                msg.setStatus(MessageStatus.READ);
//                            }
//                        }
////                        ChatService.getInstance().onMessageReadForFriend(this.session.getId(), mReceivedNoticeIds, iMessages);
//                        ApiManager.doMessagesRead(iView.getCtx(), this.session.getId(), mReceivedNoticeIds, new ApiListener<String>() {
//                            @Override
//                            public void onSuccess(String s) {
//                                if (iMessages != null && iMessages.size() > 0) {
//                                    for (MessageEntity msg : iMessages) {
//                                        msg.setReadedNum(1);
//                                        msg.setStatus(MessageStatus.READ);
//                                        boolean status = MessageReference.save(session.getId(), msg);
//                                    }
//                                }
//                            }
//
//                            @Override
//                            public void onFailed(String errorMessage) {
//                                CELog.d(errorMessage);
//                            }
//                        });
//                        receiverMessage.setReadedNum(1);
//                        receiverMessage.setStatus(MessageStatus.READ);
//                    } else {
//                        MessageStatus status = constructStaus(receiverMessage);
//                        receiverMessage.setStatus(status);
//                    }
//                } else {
//                    receiverMessage.setStatus(MessageStatus.READ);
//                    ChatMessageService.doMessageReadAllByRoomId(iView.getCtx(), session, session.getUnReadNum(), mReceivedNoticeIds, false);
//                }
//            } else {
//                mUnReadMsgIds.add(receiverMessage.getId());
//            }
//            upDateSession(receiverMessage);
//            MessageReference.save(sessionId, receiverMessage);
//            if (!recordMode) {
//                iView.displayMainMessage(true, false, true, receiverMessage, false);
//            }
//        }
//    }
//
//    private MessageStatus constructStaus(MessageEntity message) {
//
//        MessageStatus status = null;
//        if (message.getSendNum() > 0) {
//            status = MessageStatus.SUCCESS;
//        }
//        if (message.getReceivedNum() > 0) {
//            status = MessageStatus.RECEIVED;
//        }
//        if (message.getReadedNum() > 0) {
//            status = MessageStatus.READ;
//        }
//
//        return status;
//    }
//
//    public class TimeZoneChangeReceiver extends BroadcastReceiver {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if (ACTION_TIMEZONE_CHANGED.equals(action)) {
//                iView.refreshUI();
//            }
//        }
//    }
//
//    /**
//     * get  Message List Information (new) --> (old)
//     */
//    private void messageListRequestDesc(String sessionId, ChatRoomType chatRoomType, final MessageEntity lastMsg, int number) {
//        ChatMessageService.getChatMessageEntities(iView.getCtx(), sessionId, chatRoomType, lastMsg.getId(), number, Sort.DESC, new ServiceCallBack<List<MessageEntity>, RefreshSource>() {
//            @Override
//            public void complete(List<MessageEntity> entities, RefreshSource refreshSource) {
//                iView.onRefreshMore(entities, lastMsg);
//            }
//
//            @Override
//            public void error(String message) {
//            }
//        });
//    }
//
//    /**
//     * get Message List Information (new) --> (old)
//     */
//    private void messageListRequestDesc(String roomId, ChatRoomType chatRoomType, String lastMessageId) {
//        ChatMessageService.getChatMessageEntities(iView.getCtx(), roomId, chatRoomType, lastMessageId, 50, Sort.DESC, new ServiceCallBack<List<MessageEntity>, RefreshSource>() {
//            @Override
//            public void complete(List<MessageEntity> entities, RefreshSource refreshSource) {
//                Collections.sort(entities);
//                if (entities.isEmpty()) {
//                    return;
//                }
//                List<String> newMsgIds = Lists.newArrayList();
//                for (int i = entities.size() - 1; i >= 0; i--) {
//                    MessageEntity msg = entities.get(i);
//                    // EVAN_FLAG 2019-09-24 As long as it is read, it will not be sent
//                    if (MessageStatus.READ.equals(msg.getStatus())) {
//                        newMsgIds.add(msg.getId());
//                    }
//                    iView.displayMainMessage(false, false, true, msg, false);
//                }
//                MessageEntity iMessage = entities.get(0);
//                if (!newMsgIds.isEmpty()) {
//                    ChatMessageService.doMessageReadAllByRoomId(iView.getCtx(), roomId, newMsgIds, false);
//                }
//            }
//
//            @Override
//            public void error(String message) {
//
//            }
//        });
//    }
//
//    /**
//     * verification local messages status and update local database
//     */
//    private void verifyMessageReadingState(ChatRoomEntity entity, List<MessageEntity> entities) {
//        ChatMessageService.verifyMessageReadingState(iView.getCtx(), entity, entities, new ServiceCallBack<List<MessageEntity>, Enum>() {
//            @Override
//            public void complete(List<MessageEntity> entities, Enum anEnum) {
//                for (MessageEntity entity : entities) {
//                    iView.displayMainMessage(false, false, true, entity, false);
//                }
//            }
//
//            @Override
//            public void error(String message) {
//
//            }
//        });
//    }
//
//    /**
//     * Message Information (old) --> (new)
//     */
//    public void messageListRequestAsc(MessageEntity lastMessage) {
//        String lastMessageId = lastMessage == null ? "" : Strings.isNullOrEmpty(lastMessage.getId()) ? "" : lastMessage.getId();
//        this.messageListRequestAsc(lastMessageId);
//    }
//
//    /**
//     * Message Information (old) --> (new)
//     */
//    private void messageListRequestAsc(final String lastMsgId) {
//        if(session == null) return;
//        ChatMessageService.getChatMessageEntities(iView.getCtx(), session.getId(), null, lastMsgId, 50, Sort.ASC, new ServiceCallBack<List<MessageEntity>, RefreshSource>() {
//            @Override
//            public void complete(List<MessageEntity> entities, RefreshSource refreshSource) {
//                if (entities == null || entities.size() == 0) {
//                    return;
//                }
//                CELog.d("Enter the chat room, have data locally, read messages from the server:" + entities.size());
//                List<String> newMsgIds = Lists.newArrayList();
//                for (int i = 0; i < entities.size(); i++) {
//                    MessageEntity msg = entities.get(i);
//                    // As long as it is read, it will not be sent
//                    if (!MessageStatus.READ.equals(msg.getStatus())) {
//                        newMsgIds.add(msg.getId());
//                    }
//                    iView.displayMainMessage(false, false, true, msg, false);
//                }
//
//                if (!newMsgIds.isEmpty()) {
//                    ChatMessageService.doMessageReadAllByRoomId(iView.getCtx(), session, session.getUnReadNum(), newMsgIds, false);
//                }
//            }
//
//            @Override
//            public void error(String message) {
//                CELog.e(message);
//            }
//        });
//    }
//
//    private void highLightUnReadLine(boolean needProcess) {
//        ThreadExecutorHelper.getIoThreadExecutor().execute(() -> {
//            iView.highLightUnReadLine(needProcess);
//            unreadNum = -1;
//        });
//    }
//
//    private void disPlayMessageFromDatabase() {
//        ThreadExecutorHelper.getIoThreadExecutor().execute(() -> {
//            List<MessageEntity> entities = MessageReference.findByRoomIdAndTypeAndSortAndLimitAndInnerJoniAliasName(session.getId(), -1l, session.getType(), MessageReference.Sort.DESC, 50);
//            checkIsFacebookAndOverTime(entities);
//            checkFacebookReplyType(entities);
//            for (int i = entities.size() - 1; i >= 0; i--) {
//                MessageEntity messageEntity = entities.get(i);
//                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
//                    setUnReadMessage(messageEntity);
//                    iView.displayMainMessage(false, false, true, messageEntity, false);
//                });
//            }
//        });
//    }
//
//    private void setUnReadMessage(MessageEntity messageEntity) {
//        if (Objects.equals(messageEntity.getId(), unreadMessageId)) {
//            if(session != null) {
//                MessageEntity unreadMsg = new MessageEntity.Builder()
//                        .id("以下為未讀訊息")
//                        .roomId(session.getId())
//                        .content(new UndefContent("UNREAD").toStringContent())
//                        .status(MessageStatus.SUCCESS)
//                        .sourceType(SourceType.SYSTEM)
//                        .senderId(userId)
//                        .sendTime(System.currentTimeMillis())
//                        .build();
//                unreadMsg.setSendTime(messageEntity.getSendTime() - 1L);
//                iView.displayMainMessage(false, false, true, unreadMsg, false);
//                unreadMessageId = "";
//            }
//        }
//        session.setUnReadNum(0);
//    }
//
//
//    /**
//     * Get locally stored message list information
//     * (New) --> (old)
//     */
//    private void findMessageFormByLocalDataBase(ChatRoomEntity entity, ServiceCallBack<MessageEntity, Enum> callBack) {
//        ThreadExecutorHelper.getIoThreadExecutor().execute(() -> {
//            List<MessageEntity> entities = MessageReference.findByRoomIdAndTypeAndSortAndLimitAndInnerJoniAliasName(entity.getId(), -1l, entity.getType(), MessageReference.Sort.DESC, -1);
//            Collections.sort(entities);
//
//            // Supplementary message checkpoint
////            RepairMessageService.handlePrevious(iView.getCtx(), entity.getId(), entities, true);
////            verifyMessageReadingState(entity, entities);
//            ThreadExecutorHelper.getMainThreadExecutor().execute(() -> displayLocalDataBaseMessages(entities, entity));
//            if (!entities.isEmpty() && entities.size() < 10) {
//                messageListRequestDesc(entity.getId(), entity.getType(), entities.get(0).getId());
//            }
//
//            if (callBack != null) {
////                if (entities != null && !entities.isEmpty()) {
////                    return entities.get(entities.size() - 1);
////                } else {
////                    return null;
////                }
//                // If there are less than 10 local records, check the old data
////                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> callBack.complete(entities != null && !entities.isEmpty() ? entities.get(entities.size() - 1) : null, null));
//                callBack.complete(!entities.isEmpty() ? entities.get(entities.size() - 1) : null, null);
//            }
//        });
//
//    }
//
//    /**
//     * 取本地已經儲存信息
//     * 信息 (舊)  --> (新)
//     */
//    private void findMessageFromByLocalDataBaseASC(ChatRoomEntity sessionEntity) {
//        List<MessageEntity> messages = MessageReference.findByRoomIdAndTypeAndSortAndLimitAndInnerJoniAliasName(session.getId(), recordMsg.getSendTime(), session.getType(), MessageReference.Sort.ASC, 20);
////        List<MessageEntity> messages = DBManager.getInstance().queryMessagesAsc(sessionEntity.getId(), recordMsg.getSendTime(), sessionEntity.getTodoOverviewType(), 20);
//        messages.add(recordMsg);
//        Collections.sort(messages);
//        verifyMessageReadingState(sessionEntity, messages);
//        displayLocalDataBaseMessages(Lists.newArrayList(messages), sessionEntity);
//        iView.scrollToTop();
////        return messages.isEmpty() ? null : messages.first();
//    }
//
//    private void displayLocalDataBaseMessages(List<MessageEntity> messages, ChatRoomEntity sessionEntity) {
//        boolean hasUnreadLine = false;
////        int unreadLinePosition = 0;
//        LinkedList<String> failMessageIds = Lists.newLinkedList();
//        for (MessageEntity msg : messages) {
//            // 如果有失敗訊息，往最下面排序
//            if (MessageStatus.FAILED_or_ERROR.contains(msg.getStatus())) {
//                msg.setSendTime(System.currentTimeMillis());
//                failMessageIds.add(msg.getId());
//            }
//
//            String senderId = msg.getSenderId();
//            if (userId.equals(senderId)) {
//                if (msg.getSendNum() > 0) {
//                    msg.setStatus(MessageStatus.SUCCESS);
//                }
//                if (msg.getReceivedNum() > 0) {
//                    msg.setStatus(MessageStatus.RECEIVED);
//                }
//                if (msg.getReadedNum() > 0) {
//                    msg.setStatus(MessageStatus.READ);
//                }
//            }
//
//            if (msg.getId().equals(this.unreadMessageId)) {
//                if (!hasUnreadLine) {
//                    hasUnreadLine = true;
//                    if(session != null) {
//
//                        MessageEntity unreadMsg = new MessageEntity.Builder()
//                                .id("以下為未讀訊息")
//                                .roomId(session.getId())
//                                .content(new UndefContent("UNREAD").toStringContent())
//                                .status(MessageStatus.SUCCESS)
//                                .sourceType(SourceType.SYSTEM)
//                                .senderId(userId)
//                                .sendTime(System.currentTimeMillis())
//                                .build();
//                        unreadMsg.setSendTime(msg.getSendTime() - 1L);
//                        iView.displayMainMessage(false, false, true, unreadMsg, false);
//                    }
//                    this.unreadMessageId = "";
//                }
//            }
//
//            iView.displayMainMessage(false, false, true, msg, false);
//        }
//
//        sessionEntity.setUnReadNum(0);
////        iView.highLightUnReadLine(unreadNum > 0);
////        this.unreadNum = -1;
//    }
//
//    public void setVisible(boolean visible) {
//        isVisible = visible;
//    }
//
//    /**
//     * 嘗試重新送出訊息
//     * version 1.9.1
//     */
//    public void retrySend(MessageEntity message) {
//        //確認是否是強制接手
//        if (checkIsSnatch()) {
//            String agentName = UserProfileReference.findAccountName(null, session.getServiceNumberAgentId());
//            getSnatchDialog(String.format(weakReference.get().getString(R.string.text_agent_servicing_snatch_by_agent), agentName))
//                    .setOthers(new String[]{weakReference.get().getString(R.string.cancel), weakReference.get().getString(R.string.text_for_sure)})
//                    .setOnItemClickListener((o, position) -> {
//                        if (position == 1) {
//                            doAgentSnatchByAgent(weakReference.get(), session.getId(), () -> retrySend(message));
//                            iView.setServicedGreenStatus();
//                        }
//                    })
//                    .build()
//                    .setCancelable(true)
//                    .show();
//            return;
//        }
//
//
//
//
//        if (recordMode) {
//            loadMsgDBDesc();
//            recordMode = false;
//        }
////        session.setFailNum(session.getFailNum() - 1);
//        message.setStatus(MessageStatus.SENDING);
//        iView.updateMsgStatus(message.getId(), MessageStatus.SENDING);
//        message.setSendTime(System.currentTimeMillis());
//        MessageType type = message.getType();
//
//        if (MessageType.TEXT.equals(type) && message.content() instanceof TextContent) {
//            reSendTextOrAt(message, MessageType.TEXT);
//            return;
//        }
//
//        if (MessageType.AT.equals(type) && message.content() instanceof AtContent) {
//            reSendTextOrAt(message, MessageType.AT);
//            return;
//        }
//
//        if (MessageType.IMAGE.equals(type) && message.content() instanceof ImageContent) {
//            reSendImage(message);
//            return;
//        }
//
//        if (MessageType.FILE.equals(type) && message.content() instanceof FileContent) {
//            reSendFiles(message);
//            return;
//        }
//
//        if (MessageType.STICKER.equals(type) && message.content() instanceof StickerContent) {
//            reSendSticker(message);
//            return;
//        }
//
//        if (MessageType.VOICE.equals(type) && message.content() instanceof VoiceContent) {
//            reSendVoice(message);
//            return;
//        }
//
//        if (MessageType.VIDEO.equals(type) && message.content() instanceof VideoContent) {
//            reSendVideo(message);
//            return;
//        }
//    }
//
//    public void sendButtonClicked(ChatRoomEntity entity) {
//        HadEditText.SendData data = iView.getInputAreaContent();
//        String content = data.getContent();
//        //確認是否是強制接手
//        if (checkIsSnatch()) {
//            String agentName = UserProfileReference.findAccountName(null, session.getServiceNumberAgentId());
//            getSnatchDialog(String.format(weakReference.get().getString(R.string.text_agent_servicing_snatch_by_agent), agentName))
//                    .setOthers(new String[]{weakReference.get().getString(R.string.cancel), weakReference.get().getString(R.string.text_for_sure)})
//                    .setOnItemClickListener((o, position) -> {
//                        if (position == 1) {
//                            doAgentSnatchByAgent(weakReference.get(), session.getId(), () -> sendButtonClicked(session));
//                            iView.setServicedGreenStatus();
//                        }
//                    })
//                    .build()
//                    .setCancelable(true)
//                    .show();
//            return;
//        }
//
//
//        MessageType type = data.getType();
//        switch (type) {
//            case TEXT:
//                if (content.trim().isEmpty()) { // filter empty message
//                    iView.showToast(R.string.text_can_not_send_empty_message);
//                    return;
//                }
//
//                if (content.trim().length() > 10000) {
//                    iView.showErrorToast("您輸入的字數超過傳送限制。(" + content.trim().length() + ">10000)");
//                    return;
//                }
//                sendText(content);
//                break;
//            case AT:
//                String atContent = data.getContent();
//                sendAtText(atContent);
//                sendAtMessageToService(atContent);
//                break;
//        }
//
//        iView.clearTypedMessage();
//    }
//
//    /**
//     * 發送文字 Facebook 私人回覆
//     * @param message 回覆的訊息
//     * @param content 回覆的內容
//     * */
//    public void sendFacebookPrivateReply(MessageEntity message, HadEditText.SendData content) {
//        FacebookTag facebookTag = JsonHelper.getInstance().from(message.getTag(), FacebookTag.class);
//        SendFacebookCommentRequest sendFacebookCommentRequest = new SendFacebookCommentRequest(facebookTag.getData().getPostId(), facebookTag.getData().getCommentId(), "Add", "Text", content.getContent());
//        NetworkManager.INSTANCE.provideRetrofit(weakReference.get()).create(tw.com.chainsea.chat.view.chat.ChatService.class).sendFacebookPrivateComment(sendFacebookCommentRequest).enqueue(new Callback<CommonResponse<SendFacebookCommentResponse>>() {
//            @Override
//            public void onResponse(Call<CommonResponse<SendFacebookCommentResponse>> call, Response<CommonResponse<SendFacebookCommentResponse>> response) {
//                CommonResponse commonResponse = response.body();
//                if (commonResponse != null && commonResponse.get_header_() != null) {
//                    if (commonResponse.get_header_().getSuccess() != null) {
//                        if (!commonResponse.get_header_().getSuccess()) {
//                            MessageReference.updateFacebookPrivateReplyStatus(session.getId(), message.getId(), true);
//                            iView.onSendFacebookImageReplyFailed(weakReference.get().getString(R.string.facebook_already_private_replied));
//                            disPlayMessageFromDatabase();
//                        }
//                    }
//                }
//                Log.d("sendFacebookPrivateReply", "onResponse");
//            }
//
//            @Override
//            public void onFailure(Call<CommonResponse<SendFacebookCommentResponse>> call, Throwable t) {
//                ChatService.getInstance().handleSendMessageFail(session.getId(), message.getId());
//                CELog.e("sendFacebookPrivateReply", t.getMessage());
//            }
//        });
//    }
//
//    /**
//     * 發送文字 Facebook 公開回覆
//     * @param message 回覆的訊息
//     * @param content 回覆的內容
//     * */
//    public void sendFacebookPublicReply(MessageEntity message, HadEditText.SendData content) {
//        FacebookTag facebookTag =  JsonHelper.getInstance().from(message.getTag(), FacebookTag.class);
//        SendFacebookCommentRequest sendFacebookCommentRequest = new SendFacebookCommentRequest(facebookTag.getData().getPostId(), facebookTag.getData().getCommentId(), "Add", "Text", content.getContent());
//        NetworkManager.INSTANCE.provideRetrofit(weakReference.get()).create(tw.com.chainsea.chat.view.chat.ChatService.class).sendFacebookPublicComment(sendFacebookCommentRequest).enqueue(new Callback<CommonResponse<SendFacebookCommentResponse>>() {
//            @Override
//            public void onResponse(Call<CommonResponse<SendFacebookCommentResponse>> call, Response<CommonResponse<SendFacebookCommentResponse>> response) {
//                Log.d("sendFacebookPublicReply", "onResponse");
//            }
//
//            @Override
//            public void onFailure(Call<CommonResponse<SendFacebookCommentResponse>> call, Throwable t) {
//                ChatService.getInstance().handleSendMessageFail(session.getId(), message.getId());
//                CELog.e("sendFacebookPublicReply", t.getMessage());
//            }
//        });
//    }
//
//
//    /**
//     * 組成 Facebook 圖片回覆 request
//     * @param isFacebookReplyPublic 判斷是否是公開回覆 true 公開 false 私人
//     * @param fileName 圖片名稱
//     * @param filePath 圖片路徑
//     * */
//    private Request.Builder buildSendFacebookAttachmentRequest(boolean isFacebookReplyPublic, String fileName, String filePath) {
//        try {
//            MessageEntity originThemeMessage = iView.getThemeMessage();
//            FacebookTag facebookTag = JsonHelper.getInstance().from(originThemeMessage.getTag(), FacebookTag.class);
//            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
//            String fileType = filePath.substring(filePath.lastIndexOf("."));
//            MediaType mediaType = FileMedia.of(fileType);
//            RequestBody fileRequestBody = RequestBody.create(mediaType, new File(filePath));
//            builder.addFormDataPart("attachment", fileName, fileRequestBody);
//            String args = new FileService.FacebookImageArgs(token)
//                    .postId(facebookTag.getData().getPostId())
//                    .commentId(facebookTag.getData().getCommentId())
//                    .toJson();
//            Map<String, String> formData = Maps.newHashMap(ImmutableMap.of("args", args));
//            for(Map.Entry<String, String> entry : formData.entrySet()) {
//                String key = entry.getKey();
//                String value = entry.getValue();
//                builder.addFormDataPart(key, value);
//            }
//            String url = "";
//            if (isFacebookReplyPublic) {
//                url = TokenPref.getInstance(weakReference.get()).getCurrentTenantUrl() + "/" + ApiPath.sendFacebookPublicReply;
//            } else {
//                url = TokenPref.getInstance(weakReference.get()).getCurrentTenantUrl() + "/" + ApiPath.sendFacebookPrivateReply;
//            }
//
//            return new Request.Builder()
//                    .url(url)
//                    .post(builder.build());
//        } catch (Exception e) {
//            CELog.e("buildSendFacebookImageRequest Error", e);
//            return null;
//        }
//    }
//
//    /**
//     * 發送 Facebook 圖片回覆
//     * @param isFacebookReplyPublic 判斷是否是公開回覆 true 公開 false 私人
//     * @param fileName 圖片名稱
//     * @param filePath 圖片路徑
//     * */
//    public void sendFacebookAttachmentReply(boolean isFacebookReplyPublic, String fileName, String filePath) {
//        ThreadExecutorHelper.getIoThreadExecutor().execute(() -> {
//            Request.Builder request = buildSendFacebookAttachmentRequest(isFacebookReplyPublic, fileName, filePath);
//            try (okhttp3.Response response = new OkHttpClient().newCall(request.build()).execute()) {
//                ResponseBody responseBody = response.body();
//                if (responseBody != null) {
//                    String responseBodyString = responseBody.string();
//                    CommonResponse<SendFacebookCommentResponse> commonResponse = JsonHelper.getInstance().from(responseBodyString, CommonResponse.class);
//                    if (commonResponse.get_header_() != null) {
//                        if (commonResponse.get_header_().getSuccess() != null) {
//                            if (commonResponse.get_header_().getSuccess()) {
//                                iView.onSendFacebookImageReplySuccess();
//                            } else {
//                                iView.onSendFacebookImageReplyFailed("發送失敗，請稍後再試");
//                            }
//                        }
//                    }
//                }
//            } catch (IOException e) {
//                CELog.e("sendFacebookImageReply Error", e);
//            } finally {
//                iView.updateSendVideoProgress();
//            }
//        });
//    }
//
//    /**
//     * 判斷是否是 Facebook 的回覆
//     * */
//    private boolean isFacebookReply() {
//        MessageEntity themeMessage = iView.getThemeMessage();
//        if (themeMessage == null) return false;
//        return themeMessage.getFrom() == ChannelType.FB;
//    }
//
//    public void updateFacebookPostStatus(MessageEntity message, List<MessageEntity> messageEntityList) {
//        if (message.getTag() == null) return;
//        if (message.getTag().isEmpty()) return;
//        FacebookTag facebookTag = JsonHelper.getInstance().from(message.getTag(), FacebookTag.class);
//        if (facebookTag == null) return;
//        if (facebookTag.getData() == null) return;
//        String postId = facebookTag.getData().getPostId();
//        for (int i = 0; i < messageEntityList.size(); i++) {
//            MessageEntity replyMessage = messageEntityList.get(i);
//            if (replyMessage.getId().equals(message.getId())) continue;
//            if (replyMessage.getTag() == null) continue;
//            if (replyMessage.getTag().isEmpty()) continue;
//            FacebookTag facebookReplyTag = JsonHelper.getInstance().from(replyMessage.getTag(), FacebookTag.class);
//            if (facebookReplyTag == null) continue;
//            if (facebookReplyTag.getData() == null) continue;
//            if (facebookReplyTag.getData().getPostId().equals(postId)) {
//                iView.updateFacebookStatus(replyMessage);
//            }
//        }
//    }
//
//    public void refreshFacebookComment(MessageEntity message, FacebookCommentStatus facebookCommentStatus, List<MessageEntity> allMessage) {
//        if (message == null) return;
//        NetworkManager.INSTANCE.provideRetrofit(weakReference.get()).create(tw.com.chainsea.chat.view.chat.ChatService.class).getMessageItem(new MessageItemRequest(message.getId())).enqueue(new Callback<MessageEntity>() {
//            @Override
//            public void onResponse(Call<MessageEntity> call, Response<MessageEntity> response) {
//                if (response.body() != null) {
//                    MessageEntity responseMessage = response.body();
//                    MessageReference.updateFacebookMessageContent(responseMessage);
//                    message.setContent(responseMessage.getContent());
//                    message.setTag(responseMessage.getTag());
//                    message.setFacebookCommentStatus(facebookCommentStatus);
//                    iView.updateFacebookStatus(message);
//                    for (MessageEntity messageEntity: allMessage) {
//                        if (messageEntity.getId().equals(message.getId())) continue;
//                        if (messageEntity.getThemeId() == null || messageEntity.getThemeId().isEmpty()) continue;
//                        if (messageEntity.getThemeId().equals(message.getId())) {
//                            messageEntity.setNearMessageContent(responseMessage.getContent());
//                            iView.updateFacebookStatus(messageEntity);
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(Call<MessageEntity> call, Throwable t) {
//                CELog.e("refreshFacebookComment Failure", t);
//            }
//        });
//    }
//
//
//    /**
//     * 更新留言的狀態，用 Queue 確保不會並發太多
//     * */
//    public void checkCommentStatus(MessageEntity message) {
//        if (tempFacebookAlreadyCheckStatus.contains(message.getId())) return;
//        if (message.getTag() == null) return;
//        FacebookTag facebookTag = JsonHelper.getInstance().from(message.getTag(), FacebookTag.class);
//        if (message.getFacebookPostStatus() == FacebookPostStatus.Delete) return;
//        if (facebookTag == null) return;
//        if (facebookTag.getData() == null) return;
//        if(facebookTag.getData().getCommentId() == null) return;
//        CheckCommentStatusRequest checkCommentStatusRequest = new CheckCommentStatusRequest(facebookTag.getData().getCommentId());
//        Call<CheckCommentResponse> responseQueue = NetworkManager.INSTANCE.provideRetrofit(weakReference.get()).create(tw.com.chainsea.chat.view.chat.ChatService.class).checkCommentStatus(checkCommentStatusRequest);
//        if (isTokenNeedRefresh) {
//            new Handler().postDelayed(() -> {
//                checkCommentStatus(message);
//            }, 1000);
//            return;
//        }
//        if (!checkCommentQueue.contains(message.getId())) {
//            checkCommentQueue.add(message.getId());
//        }
//        responseQueue.enqueue(new Callback<>() {
//            @Override
//            public void onResponse(Call<CheckCommentResponse> call, Response<CheckCommentResponse> response) {
//                CheckCommentResponse checkCommentResponse = response.body();
//                if (checkCommentResponse.get_header_() != null) {
//                    if (checkCommentResponse.get_header_().getSuccess() != null) {
//                        if (checkCommentResponse.get_header_().getSuccess()) {
//                            checkCommentQueue.remove(message.getId());
//                            isTokenNeedRefresh = false;
//                            tempFacebookAlreadyCheckStatus.add(message.getId());
//                            if (checkCommentResponse != null && checkCommentResponse.getResult() != null) {
//                                MessageReference.updateFacebookCommentStatus(session.getId(), message.getId(), FacebookCommentStatus.of(checkCommentResponse.getResult().getCommentStatus()));
//                                MessageReference.updateFacebookPrivateReplyStatus(session.getId(), message.getId(), !checkCommentResponse.getResult().getCanPrivateReply());
//                                MessageReference.updateFacebookPostStatus(session.getId(), message.getId(), FacebookPostStatus.of(checkCommentResponse.getResult().getPostStatus()));
//                                message.setFacebookCommentStatus(FacebookCommentStatus.of(checkCommentResponse.getResult().getCommentStatus()));
//                                message.setFacebookPrivateReplied(!checkCommentResponse.getResult().getCanPrivateReply());
//                                message.setFacebookPostStatus(FacebookPostStatus.of(checkCommentResponse.getResult().getPostStatus()));
//                                iView.updateFacebookStatus(message);
//                            }
//                        } else {
//                            if (checkCommentResponse.get_header_().getErrorCode() != null) {
//                                ErrCode code = ErrCode.of(checkCommentResponse.get_header_().getErrorCode());
//                                if (ErrCode.TOKEN_INVALID_or_TOKEN_REQUIRED.contains(code)) {
//                                    isTokenNeedRefresh = true;
//                                    refreshToken(message);
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(Call<CheckCommentResponse> call, Throwable t) {
//                CELog.e("checkCommentStatus", t);
//            }
//        });
//    }
//
//    /**
//     * 更新 Token
//     * */
//    private void refreshToken(MessageEntity message) {
//        new TokenApplyRequest(weakReference.get(), new TokenApplyRequest.Listener() {
//            @Override
//            public void allCallBack(boolean isRefresh, boolean status) {
//            }
//
//            @Override
//            public void onSuccess(boolean isRefresh, AileTokenApply.Resp resp) {
//                isTokenNeedRefresh = false;
//                AppConfig.tokenForNewAPI = resp.getTokenId();
//                checkCommentStatus(message);
//            }
//
//            @Override
//            public void onFailed(ErrCode errorCode, String errorMessage) {
//
//            }
//
//            @Override
//            public void onCallData(String roomId, String meetingId, String callKey) {
//
//            }
//        }).setMainThreadEnable(false).request(true);
//    }
//
//    /**
//     * 如果該 Facebook 留言已經被私訊回覆過，則移動到該私訊回覆的訊息
//     * @param  messageId Facebook 留言訊息的 Id
//     * @param commentId Facebook 留言訊息的 commentId
//     * @param mainMessageList 全部的 Message
//     * */
//    public void moveToFacebookReplyMessage(String messageId, String commentId, List<MessageEntity> mainMessageList) {
//        for (int i = 0; i < mainMessageList.size(); i++) {
//            MessageEntity message = mainMessageList.get(i);
//            if (message.getTag() == null) continue;
//            if (message.getTag().isEmpty()) continue;
//            if (messageId.equals(message.getId())) continue;
//            FacebookTag facebookTag = JsonHelper.getInstance().from(message.getTag(), FacebookTag.class);
//            if (commentId.equals(facebookTag.getData().getCommentId()) && "private".equals(facebookTag.getData().getReplyType())) {
//                message.setAnimator(true);
//                iView.moveToFacebookReplyMessage(i);
//                return;
//            }
//        }
//    }
//
//    /**
//     * 發送at的資訊給 service
//     * */
//    private void sendAtMessageToService(String atMessage) {
//        try {
//            Type typeToken = new TypeToken<List<HadEditText.AtMentionComponent>>(){}.getType();
//            List<HadEditText.AtMentionComponent> atMentionComponents = JsonHelper.getInstance().from(atMessage, typeToken);
//            AtomicBoolean isAtAll = new AtomicBoolean(false);
//            AtomicReference<List<String>> atUserId = new AtomicReference<>(Lists.newArrayList());
//            atMentionComponents.forEach(atMentionComponent -> {
//                if ("All".equals(atMentionComponent.getObjectType())) {
//                    isAtAll.set(true);
//                } else {
//                    atUserId.set(atMentionComponent.getUserIds());
//                }
//            });
//            tw.com.chainsea.chat.view.chat.ChatService chatService = NetworkManager.INSTANCE.provideRetrofit(weakReference.get()).create(tw.com.chainsea.chat.view.chat.ChatService.class);
//            Callback<CommonResponse<Object>> callback = new Callback<>() {
//                @Override
//                public void onResponse(Call call, Response response) {
//
//                }
//
//                @Override
//                public void onFailure(Call call, Throwable t) {
//
//                }
//            };
//
//            if (isAtAll.get()) {
//                chatService.sendAtMessage(new SendAtMessageRequest(session.getId(), isAtAll.get()))
//                        .enqueue(callback);
//            } else {
//                chatService.sendAtMessage(new SendAtMessageRequest(session.getId(), atUserId.get()))
//                        .enqueue(callback);
//            }
//
//        } catch (Exception e) {
//
//        }
//    }
//
//
//
//    public void doRobotSnatchByAgent(Context ctx, String roomId, HadEditText.SendData sendData) {
//        ChatRoomService.getInstance().snatchRobotServicingAPI(ctx, roomId, new RoomRecentCallBack<RobotServiceResponse, RefreshSource>() {
//            @Override
//            public void error(String message) {
//                CELog.e("doRobotSnatchByAgent error = "+message);
//            }
//            @Override
//            public void complete(RobotServiceResponse robotServiceResponse, RefreshSource refreshSource) {
//                iView.doChatRoomSnatchByAgent(robotServiceResponse.getSuccess(), sendData);
//            }
//            @Override
//            public void finish() {
//
//            }
//        });
//    }
//
//
//    // 下下策 等之後有重構再處理
//    public void doAgentSnatchByAgent(Context context, String roomId, AgentSnatchCallback agentSnatchCallback) {
//        iView.showLoadingView(R.string.wording_loading);
//        ChatRoomService.getInstance().snatchAgentServicing(context, roomId, new RoomRecentCallBack<CommonResponse<Object>, ChatServiceNumberService.ServicedTransferType>() {
//            @Override
//            public void error(String message) {
//                iView.hideLoadingView();
//                iView.showErrorMsg(message);
//            }
//
//            @Override
//            public void complete(CommonResponse<Object> objectCommonResponse, ChatServiceNumberService.ServicedTransferType servicedTransferType) {
//                iView.hideLoadingView();
//                if (objectCommonResponse.get_header_() != null) {
//                    if (objectCommonResponse.get_header_().getSuccess()) {
//                        iView.doServiceNumberServicedStatus(session, agentSnatchCallback);
//                        session.setServiceNumberAgentId(userId);
//                    } else {
//                        iView.showErrorMsg(objectCommonResponse.get_header_().getErrorMessage());
//                    }
//                }
//            }
//
//            @Override
//            public void finish() {
//                iView.hideLoadingView();
//            }
//        });
//    }
//
//    private void loadMsgDBDesc() {
//        List<MessageEntity> messages = MessageReference.findByRoomIdAndTypeAndSortAndLimitAndInnerJoniAliasName(session.getId(), System.currentTimeMillis(), session.getType(), MessageReference.Sort.DESC, 50);
//        iView.transferModeDisplay(messages);
//    }
//
//    /**
//     * 送出AT 資料
//     */
//    private void sendAtText(String atContent) {
//        if (recordMode) {
//            loadMsgDBDesc();
//            recordMode = false;
//        }
//        //这里生成的是UUID字符串
//        String messageId = Tools.generateMessageId();
//        if(session != null) {
//            MessageEntity msg = MsgKitAssembler.assembleSendAtMessage(session.getId(), messageId, userId, mSelfAccount.getAvatarId(), atContent, mSelfAccount.getNickName());
//            setReplyMsg(msg);
//            showMessage(msg);
//            if (iView.isThemeOpen() && themeMessage != null) {
//                ChatService.getInstance().sendReplyMessage(session.getId(), messageId, MessageType.AT, atContent, themeMessage.getId());
//            } else {
//                if (iView.isThemeOpen() && themeMessage != null) {
//                    ChatService.getInstance().sendMessage(session.getId(), messageId, MessageType.AT, atContent, themeMessage.getId());
//                } else {
//                    ChatService.getInstance().sendMessage(session.getId(), messageId, MessageType.AT, atContent, "");
//                }
//            }
//        }
//        iView.setThemeOpen(false);
//    }
//
//    /**
//     * 送出 Business 資料
//     */
//    public void sendBusiness(BusinessContent businessContent) {
//        //確認是否是強制接手
//        if (checkIsSnatch()) {
//            String agentName = UserProfileReference.findAccountName(null, session.getServiceNumberAgentId());
//            getSnatchDialog(String.format(weakReference.get().getString(R.string.text_agent_servicing_snatch_by_agent), agentName))
//                    .setOthers(new String[]{weakReference.get().getString(R.string.cancel), weakReference.get().getString(R.string.text_for_sure)})
//                    .setOnItemClickListener((o, position) -> {
//                        if (position == 1) {
//                            doAgentSnatchByAgent(weakReference.get(), session.getId(), () -> sendBusiness(businessContent));
//                            iView.setServicedGreenStatus();
//                        }
//                    })
//                    .build()
//                    .setCancelable(true)
//                    .show();
//            return;
//        }
//
//
//
//        if (recordMode) {
//            loadMsgDBDesc();
//            recordMode = false;
//        }
//        //这里生成的是UUID字符串
//        String messageId = Tools.generateMessageId();
//        if (session != null) {
//
//            MessageEntity msg = MsgKitAssembler.assembleSendBusinessMessage(session.getId(), messageId, mSelfAccount, businessContent);
////        MessageEntity msg = MsgKitAssembler.assembleSendAtMessage(session.getId(), messageId, userId, mSelfAccount.getAvatarId(), atContent, mSelfAccount.getNickName());
//            setReplyMsg(msg);
//            showMessage(msg);
//            if (iView.isThemeOpen() && themeMessage != null) {
////            ChatService.getInstance().sendReplyMessage(session.getId(), messageId, MessageType.AT, atContent, themeMessage.getId());
//            } else {
//                if (iView.isThemeOpen() && themeMessage != null) {
////                ChatService.getInstance().sendMessage(session.getId(), messageId, MessageType.AT, atContent, themeMessage.getId());
//                } else {
//                    if (session != null)
//                        ChatService.getInstance().sendMessage(session.getId(), messageId, MessageType.BUSINESS, businessContent.toSendContent(), "");
//                }
//            }
//        }
//        iView.setThemeOpen(false);
//    }
//
//    public AlertView.Builder getSnatchDialog(String message) {
//        return  new AlertView.Builder()
//                .setContext(weakReference.get())
//                .setStyle(AlertView.Style.Alert)
//                .setMessage(message)
//                .setOthers(new String[]{weakReference.get().getString(R.string.picture_cancel), weakReference.get().getString(R.string.text_robot_servicing_transfer)});
//    }
//
//    private void sendText(String content) {
//        if (recordMode) {
//            loadMsgDBDesc();
//            recordMode = false;
//        }
//        if ("{}".equals(content)) {
//            content = "%" + content + "%";
//        }
//        //这里生成的是UUID字符串
//        String messageId = Tools.generateMessageId();
//        if(session != null) {
//            MessageEntity msg = MsgKitAssembler.assembleSendTextMessage(session.getId(), messageId, userId, mSelfAccount.getAvatarId(), content, mSelfAccount.getNickName());
//            setReplyMsg(msg);
//            showMessage(msg);
//            if (iView.isThemeOpen() && themeMessage != null) {
//                ChatService.getInstance().sendReplyMessage(session.getId(), messageId, MessageType.TEXT, content, themeMessage.getId());
////            ChatService.getInstance().sendReplyTextMessage(session.getId(), messageId, content, mThemeMsg.getId());
//            } else {
//                if (iView.isThemeOpen() && themeMessage != null) {
//                    ChatService.getInstance().sendMessage(session.getId(), messageId, MessageType.TEXT, content, themeMessage.getId());
//                } else {
//                    if (iView.isChildRoomOpen()) {
//                        String id = iView.getChildRoomId();
//                        assert Strings.isNullOrEmpty(id);
//                        ChatService.getInstance().sendMessage(id, messageId, MessageType.TEXT, content, "");
//                    } else {
//                        ChatService.getInstance().sendMessage(session.getId(), messageId, MessageType.TEXT, content, "");
//                    }
//                }
//            }
//        }
//        iView.setThemeOpen(false);
//    }
//
//    /**
//     * 重新提交文字或標註訊息
//     */
//    private void reSendTextOrAt(MessageEntity msg, MessageType type) {
//        String content = null;
//        switch (type) {
//            case TEXT:
//                content = ((TextContent) msg.content()).getText();
//                break;
//            case AT:
//                content = JsonHelper.getInstance().toJson(((AtContent) msg.content()).getMentionContents());
//                break;
//        }
//        if (!Strings.isNullOrEmpty(content)) {
//            showMessage(msg);
//            if(session != null) ChatService.getInstance().sendMessage(session.getId(), msg.getId(), type, content, msg.getThemeId());
//        }
//    }
//
//    /**
//     * 重新提交表情訊息
//     * version 1.9.1
//     */
//    private void reSendSticker(MessageEntity message) {
//        if (message == null || (!MessageType.STICKER.equals(message.getType()) && !(message.content() instanceof StickerContent))) {
//            return;
//        }
//        String stickerId = ((StickerContent) message.content()).getId();
//        String packageId = ((StickerContent) message.content()).getPackageId();
//        showMessage(message);
//        if(session != null) ChatService.getInstance().sendStickerMessage(session.getId(), message.getId(), stickerId, packageId, message.getThemeId());
//    }
//
//    /**
//     * 重新提交影音訊息
//     * version 1.9.1
//     */
//    public void reSendVideo(final MessageEntity message) {
//        if (message == null || (!MessageType.VIDEO.equals(message.getType()) && !(message.content() instanceof VideoContent))) {
//            return;
//        }
//        VideoContent videoContent = ((VideoContent) message.content());
//        iView.showSendVideoProgress(weakReference.get().getString(R.string.text_sending_video_message));
//        UploadManager.getInstance().onUploadFile(iView.getCtx(), videoContent.getName(), message.getId(), token, MessageType.VIDEO, videoContent.getAndroid_local_path(), new UploadManager.OnUploadListener() {
//            @Override
//            public void onSuccess(String messageId, MessageType type, String response) {
//                UploadManager.FileEntity entity = JsonHelper.getInstance().from(response, UploadManager.FileEntity.class);
//                MessageEntity message = MessageReference.findById(messageId);
//                if (message != null && message.content() instanceof VideoContent && entity != null) {
//                    if (!Strings.isNullOrEmpty(entity.getUrl()) && !Strings.isNullOrEmpty(entity.getName()) && entity.getSize() > 0) {
//                        VideoContent videoContent = ((VideoContent) message.content());
//                        videoContent.setName(entity.getName());
//                        videoContent.setUrl(entity.getUrl());
//                        videoContent.setSize(entity.getSize());
//                        message.setContent(videoContent.toStringContent());
//                        MessageReference.save(message.getRoomId(), message);
//                        showMessage(message);
//                        if(session != null) {
//                            if (iView.isThemeOpen() && themeMessage != null) {
//                                ChatService.getInstance().sendReplyVideoMessage(session.getId(), messageId, themeMessage.getId(), videoContent, entity.getMD5(), entity.getThumbnailUrl(), entity.getThumbnailWidth(), entity.getThumbnailHeight());
//                            } else {
//                                if (iView.isThemeOpen() && themeMessage != null) {
//                                    ChatService.getInstance().sendVideoMessage(session.getId(), messageId, themeMessage.getId(), videoContent, entity.getMD5(), entity.getThumbnailUrl(), entity.getThumbnailWidth(), entity.getThumbnailHeight());
//                                } else {
//                                    if (iView.isChildRoomOpen()) {
//                                        String id = iView.getChildRoomId();
//                                        assert Strings.isNullOrEmpty(id);
//                                        ChatService.getInstance().sendVideoMessage(id, messageId, "", videoContent, entity.getMD5(), entity.getThumbnailUrl(), entity.getThumbnailWidth(), entity.getThumbnailHeight());
//                                    } else {
//                                        ChatService.getInstance().sendVideoMessage(session.getId(), messageId, "", videoContent, entity.getMD5(), entity.getThumbnailUrl(), entity.getThumbnailWidth(), entity.getThumbnailHeight());
//                                    }
//                                }
//                            }
//                        }
//                        iView.setThemeOpen(false);
//                    }
//                }
////                iView.hideLoadingView();
//                iView.updateSendVideoProgress();
//            }
//
//            @Override
//            public void onProgress(String messageId, int progress, long total) {
////                updateMsgProgress(messageId, progress);
//            }
//
//            @Override
//            public void onFailed(String reason) {
//                iView.updateSendVideoProgress();
//                showMessage(message);
//                CELog.e(reason);
//                updateMsgStatus(session.getId(), message.getId(), MessageStatus.FAILED);
//
//            }
//        });
//    }
//
//    /**
//     * 重新提交影音訊息
//     * version 1.9.1
//     */
//    private void reSendVoice(final MessageEntity message) {
//        if (message == null || (!MessageType.VOICE.equals(message.getType()) && !(message.content() instanceof VoiceContent))) {
//            return;
//        }
//        VoiceContent voiceContent = ((VoiceContent) message.content());
//        String path = voiceContent.getUrl();
//        showMessage(message);
//        UploadManager.getInstance().uploadVoice(iView.getCtx(), message.getId(), token, path, new UploadManager.OnVoiceUploadListener() {
////            @Override
////            public void onUploadSuccess(String messageId, String url) {
//////                ChatService.getInstance().sendVoiceMessage(session.getId(), messageId, voiceContent.getDuration(), url, message.getThemeId());
////            }
//
//            @Override
//            public void onUploadSuccess(String messageId, UploadManager.FileEntity entity) {
//                ChatService.getInstance().sendVoiceMessage(session.getId(), messageId, voiceContent.getDuration(), entity.getUrl(), message.getThemeId(), entity.getMD5());
//            }
//
//            @Override
//            public void onUploadFailed(String reason) {
//                CELog.e(reason);
//                updateMsgStatus(session.getId(), message.getId(), MessageStatus.FAILED);
//            }
//        });
//    }
//
//    /**
//     * 重新發送圖片訊息
//     * version 1.9.1
//     */
//    public void reSendImage(final MessageEntity message) {
//        if (message == null || (!MessageType.IMAGE.equals(message.getType()) && !(message.content() instanceof ImageContent))) {
//            return;
//        }
//        ImageContent imageContent = ((ImageContent) message.content());
//        final String name = imageContent.getUrl();
//        showMessage(message);
//
//        String path = DaVinci.with().getImageLoader().getAbsolutePath(name);
//        String messageId = message.getId();
//        FileService.uploadFile(iView.getCtx(), true, token, Media.findByFileType(name), path, name, new AServiceCallBack<UploadManager.FileEntity, RefreshSource>() {
//            @Override
//            public void onProgress(float progress, long total) {
//                updateMsgProgress(messageId, (int) (progress * 100));
//            }
//
//            @Override
//            public void complete(UploadManager.FileEntity fileEntity, RefreshSource refreshSource) {
//                if (fileEntity == null) {
//                    error("上傳失敗");
//                    return;
//                }
//
//                if (Strings.isNullOrEmpty(fileEntity.getUrl()) || Strings.isNullOrEmpty(fileEntity.getThumbnailUrl())) {
//                    error("上傳失敗");
//                    return;
//                }
//
//                ImageEntity entity = DaVinci.with().getImageLoader().getImage(name);
//                Bitmap bitmap = entity.getBitmap();
//                if (bitmap.getHeight() <= 0 || bitmap.getWidth() <= 0) {
//                    Log.e("isHeightAndWidthError:", name);
//                }
//                ChatService.getInstance().sendImageMessage(session.getId(), messageId, message.getThemeId(), fileEntity);
//            }
//
//            @Override
//            public void error(String errorMessage) {
//                CELog.e(errorMessage);
//                updateMsgStatus(session.getId(), message.getId(), MessageStatus.FAILED);
//            }
//        });
//
//
////        UploadManager.getInstance().uploadImage(iView.getCtx(),name, message.getId(), token, DaVinci.with().getImageLoader().getAbsolutePath(name), new UploadManager.OnImageUploadListener() {
////            @Override
////            public void onUploadSuccess(String messageId, UploadManager.FileEntity fileEntity) {
////                if (fileEntity != null && fileEntity.getUrl() != null && fileEntity.getThumbnailUrl() != null) {
////                    ImageEntity entity = DaVinci.with().getImageLoader().getImage(name);
////                    Bitmap bitmap = entity.getBitmap();
////                    if (bitmap.getHeight() <= 0 || bitmap.getWidth() <= 0) {
////                        Log.e("isHeightAndWidthError:", name);
////                    }
////                    ChatService.getInstance().sendImageMessage(session.getId(), messageId, message.getThemeId(), fileEntity);
////                }
////            }
////
////            @Override
////            public void onUploadIng(String messageId, int progress, long total) {
////                updateMsgProgress(messageId, progress);
////            }
////
////            @Override
////            public void onUploadFailed(String reason) {
////                CELog.e(reason);
////                updateMsgStatus(session.getId(), message.getId(), MessageStatus.FAILED);
////            }
////        });
//    }
//
//    /**
//     * 重新提交圖文訊息
//     *
//     * @param message
//     * @version 1.9.1
//     */
////    private void reSendImageText(final MessageEntity message) {
////        if (message == null || (!MessageType.IMAGE.equals(message.getTodoOverviewType()) && !(message.content() instanceof ImageTextContent))) {
////            return;
////        }
////
////        ImageTextFormat format = (ImageTextFormat) msg.getFormat();
////        final String title = format.getTitle();
////        final String text = format.getText();
////        String coverName = format.getImageUrl();
////        final String msgId = msg.getId();
////        String content = Constant.image_msg_text;
////        showMessage(msg);
////        updateSession(content, msg.getSendTime(), MessageType.IMAGE_TEXT, msgId);
//////        ChatService.getInstance().updateChatRoomEntity(sessionId, message, content, message.getTime(), MessageType.IMAGE_TEXT);
////        UploadManager.getInstance().uploadImage(coverName, msgId, token, DaVinci.with().getImageLoader().getAbsolutePath(coverName),
////                new UploadManager.OnImageUploadListener() {
////                    @Override
////                    public void onUploadSuccess(String messageId, String url, String thumbnailUrl, int size, int i, int width, int height) {
////                        if (url != null && thumbnailUrl != null) {
////                            ChatService.getInstance().sendImageTextMessage(session.getId(), messageId, "1", title, text, url, msg.getThemeId());
////                        }
////                    }
////
////                    @Override
////                    public void onUploadIng(String messageId, int progress, long total) {
////
////                    }
////
////                    @Override
////                    public void onUploadFailed(String reason) {
////                        CELog.e(reason);
////                        updateMsgStatus(session.getId(), msgId, MessageStatus.FAILED);
////                    }
////                });
////    }
//
//    /**
//     * 重新提交檔案訊息
//     * version 1.9.1
//     */
//    private void reSendFiles(MessageEntity message) {
//        if (message == null || (!MessageType.IMAGE.equals(message.getType()) && !(message.content() instanceof FileContent))) {
//            return;
//        }
//
//        FileContent fileContent = (FileContent) message.content();
//        String path = fileContent.getAndroid_local_path();
//        showMessage(message);
//        UploadManager.getInstance().uploadFile(iView.getCtx(), message.getId(), token, path, new UploadManager.OnFileUploadListener() {
//            @Override
//            public void onUploadSuccess(String messageId, UploadManager.FileEntity entity) {
//                ChatService.getInstance().sendFileMessage(session.getId(), messageId, entity.getName(), entity.getSize(), path, entity.getUrl(), message.getThemeId(), entity.getMD5());
//            }
//
//            @Override
//            public void onUploadIng(String messageId, int progress, long total) {
//
//            }
//
//            @Override
//            public void onUploadFailed(String reason, String messageId) {
//                CELog.e(reason);
//                updateMsgStatus(session.getId(), message.getId(), MessageStatus.FAILED);
//            }
//        });
//    }
//
//    public void sendSticker(String stickerId, String packageId) {
//        if (recordMode) {
//            loadMsgDBDesc();
//            recordMode = false;
//        }
//        String messageId = Tools.generateMessageId();
//        MessageEntity msg = MsgKitAssembler.assembleSendStickerMessage(session.getId(), messageId, mSelfAccount.getId(), mSelfAccount.getAvatarId(), mSelfAccount.getNickName(), stickerId, packageId);
//        setReplyMsg(msg);
//        showMessage(msg);
//        if (iView.isThemeOpen() && themeMessage != null) {
//            ChatService.getInstance().sendReplyStickerMessage(session.getId(), messageId, stickerId, packageId, themeMessage.getId());
//        } else {
//            if (iView.isThemeOpen() && themeMessage != null) {
//                ChatService.getInstance().sendStickerMessage(session.getId(), messageId, stickerId, packageId, themeMessage.getId());
//            } else {
//                ChatService.getInstance().sendStickerMessage(session.getId(), messageId, stickerId, packageId, "");
//            }
//        }
//        iView.setThemeOpen(false);
//    }
//
//    public void sendVoice(String path, final int duration) {
//        sendVoice(path, duration, false);
//    }
//    /**
//     * 送出語音訊息
//     * ersion 1.9.1
//     */
//    public void sendVoice(String path, final int duration, boolean isFacebookReplyPublic) {
//
//        //確認是否是強制接手
//        if (checkIsSnatch()) {
//            String agentName = UserProfileReference.findAccountName(null, session.getServiceNumberAgentId());
//            String finalPath = path;
//            getSnatchDialog(String.format(weakReference.get().getString(R.string.text_agent_servicing_snatch_by_agent), agentName))
//                    .setOthers(new String[]{weakReference.get().getString(R.string.cancel), weakReference.get().getString(R.string.text_for_sure)})
//                    .setOnItemClickListener((o, position) -> {
//                        if (position == 1) {
//                            doAgentSnatchByAgent(weakReference.get(), session.getId(), () -> sendVoice(finalPath, duration));
//                            iView.setServicedGreenStatus();
//                        }
//                    })
//                    .build()
//                    .setCancelable(true)
//                    .show();
//            return;
//        }
//
//        if (isFacebookReply()) {
//            sendFacebookAttachmentReply(isFacebookReplyPublic, "", path);
//            return;
//        }
//
//        if (recordMode) {
//            loadMsgDBDesc();
//            recordMode = false;
//        }
//        final String messageId = Tools.getNameWithoutExt(path);
//        if (path.startsWith("/openapi")) {
//            path = TokenPref.getInstance(iView.getCtx()).getCurrentTenantUrl() + ApiPath.ROUTE + path;
////            path = NetConfig.getInstance().getUrl() + path;
//        }
//        MessageEntity msg = MsgKitAssembler.assembleSendVoiceMessage(session.getId(), messageId, mSelfAccount.getId(), mSelfAccount.getNickName(), mSelfAccount.getAvatarId(), duration, path);
//        setReplyMsg(msg);
//        showMessage(msg);
//
//        UploadManager.getInstance().uploadVoice(iView.getCtx(), messageId, token, path, new UploadManager.OnVoiceUploadListener() {
//
//            @Override
//            public void onUploadSuccess(String messageId, UploadManager.FileEntity entity) {
//                if (iView.isThemeOpen() && themeMessage != null) {
//                    ChatService.getInstance().sendReplyVoiceMessage(session.getId(), messageId, duration, entity.getUrl(), themeMessage.getId(), entity.getMD5());
//                } else {
//                    if (iView.isThemeOpen() && themeMessage != null) {
//                        ChatService.getInstance().sendVoiceMessage(session.getId(), messageId, duration, entity.getUrl(), themeMessage.getId(), entity.getMD5());
//                    } else {
//                        ChatService.getInstance().sendVoiceMessage(session.getId(), messageId, duration, entity.getUrl(), "", entity.getMD5());
//                    }
//                }
//                iView.setThemeOpen(false);
//            }
//
//            @Override
//            public void onUploadFailed(String reason) {
//                CELog.e(reason);
//                if(session != null) updateMsgStatus(session.getId(), messageId, MessageStatus.FAILED);
//            }
//        });
//    }
//
//    public void updateMsgProgress(String messageId, int progress) {
////        session.setFailNum(session.getFailNum() + 1);
////        DBManager.getInstance().updateMessageStatus(messageId, status);
//        iView.updateMsgProgress(messageId, progress);
//    }
//
//    private void updateMsgStatus(String roomId, String messageId, MessageStatus status) {
////        session.setFailNum(session.getFailNum() + 1);
//        DBManager.getInstance().updateMessageStatus(messageId, status);
//        MessageEntity message = MessageReference.findById(messageId);
//        if (message != null) {
//            if (message.getStatus().equals(MessageStatus.SUCCESS)) {
//                return;
//            }
//            DBManager.getInstance().updateMessageStatus(messageId, MessageStatus.FAILED);
////            String content = "";
////            MessageType type = message.getType();
////            switch (type) {
////                case FILE:
////                    content = Constant.file_msg;
////                    break;
////                case IMAGE:
////                    content = Constant.image_msg;
////                    break;
////                case AT:
////                    if (message.content() instanceof AtContent) {
////                        content = JsonHelper.getInstance().toJson(((AtContent) message.content()).getMentionContents());
////                    }
////                    break;
////                case TEXT:
////                    if (message.content() instanceof TextContent) {
////                        content = ((TextContent) message.content()).getText();
////                    }
////                    break;
////                case VOICE:
////                    content = Constant.voice_msg;
////                    break;
////                case VIDEO:
////                    content = Constant.video_msg;
////                    break;
////                case STICKER:
////                    content = Constant.sticker_msg;
////                    break;
////                case IMAGE_TEXT:
////                    content = Constant.image_msg_text;
////                    break;
////                case AD:
////                case CALL:
////                case LIST_TEXT:
////                case LOCATION:
////                case BUSINESS_TEXT:
////                case UNDEF:
////                default:
////                    content = "";
////            }
//            // TODO: 2019-11-20 處理發送失敗 Content
////            if (!Strings.isNullOrEmpty(content)) {
////                ChatRoomReference.getInstance().updateFailContentById(roomId, content);
////            }
////            broadcastMsgStatus(msgId, -1, System.currentTimeMillis());
//            EventBusUtils.sendEvent(new EventMsg(MsgConstant.MSG_STATUS_FILTER, new MsgStatusBean(messageId, -1, System.currentTimeMillis())));
//        }
//
//        iView.updateMsgStatus(messageId, MessageStatus.FAILED);
//    }
//
//    public void sendImage(final String name, final String thumbnailName) {
//        sendImage(name, thumbnailName, false, false);
//    }
//
//    public void sendImage(final String name, final String thumbnailName, boolean isFacebookReplyPublic) {
//        sendImage(name, thumbnailName, false, isFacebookReplyPublic);
//    }
//    /**
//     * 送出圖片訊息
//     */
//    public void sendImage(final String name, final String thumbnailName, boolean isQuote, boolean isFacebookReplyPublic) {
//
//        //確認是否是強制接手
//        if (checkIsSnatch()) {
//            String agentName = UserProfileReference.findAccountName(null, session.getServiceNumberAgentId());
//            getSnatchDialog(String.format(weakReference.get().getString(R.string.text_agent_servicing_snatch_by_agent), agentName))
//                    .setOthers(new String[]{weakReference.get().getString(R.string.cancel), weakReference.get().getString(R.string.text_for_sure)})
//                    .setOnItemClickListener((o, position) -> {
//                        if (position == 1) {
//                            doAgentSnatchByAgent(weakReference.get(), session.getId(), () -> sendImage(name, thumbnailName));
//                            iView.setServicedGreenStatus();
//                        }
//                    })
//                    .build()
//                    .setCancelable(true)
//                    .show();
//            return;
//        }
//
//
//        if (recordMode) {
//            loadMsgDBDesc();
//            recordMode = false;
//        }
//
//        final String messageId = Tools.generateMessageId();
//        String cacheFile = DaVinci.with().getImageLoader().getAbsolutePath(name);
//        BitmapHelper.getBitmapFromLocal(cacheFile);
//
//        ImageEntity image = DaVinci.with().getImageLoader().getImage(name);
//        Bitmap bitmap = image.getBitmap();
//
//        if(session != null) {
//            MessageEntity msg = MsgKitAssembler.assembleSendImageMessage(session.getId(), messageId, mSelfAccount.getId(), mSelfAccount.getAvatarId(), mSelfAccount.getNickName(), name, thumbnailName, bitmap.getWidth(), bitmap.getHeight());
//
//            if (isFacebookReply()) {
//                String path = DaVinci.with().getImageLoader().getAbsolutePath(name);
//                sendFacebookAttachmentReply(isFacebookReplyPublic, name, path);
//                iView.showSendVideoProgress("圖片發送中");
//                return;
//            }
//
//            setReplyMsg(msg);
//            showMessage(msg);
//            if (isQuote) {
//                iView.showSendVideoProgress(weakReference.get().getString(R.string.text_image_quote_on_the_way));
//            } else {
//                iView.showSendVideoProgress("圖片發送中");
//            }
//            String path = DaVinci.with().getImageLoader().getAbsolutePath(name);
//            FileService.uploadFile(iView.getCtx(), true, token, Media.findByFileType(name), path, name, new AServiceCallBack<UploadManager.FileEntity, RefreshSource>() {
//                @Override
//                public void onProgress(float progress, long total) {
//                    updateMsgProgress(messageId, (int) (progress * 100));
//                }
//
//                @Override
//                public void complete(UploadManager.FileEntity entity, RefreshSource refreshSource) {
//                    if (entity == null) {
//                        error("上傳失敗");
//                        return;
//                    }
//
//                    if (Strings.isNullOrEmpty(entity.getUrl()) || Strings.isNullOrEmpty(entity.getThumbnailUrl())) {
//                        error("上傳失敗");
//                        return;
//                    }
//                    MessageEntity message = MessageReference.findById(messageId);
//                    if (message != null && message.content() instanceof ImageContent) {
//                        ImageContent imageContent = (ImageContent) message.content();
//
//                        imageContent.setUrl(entity.getUrl());
//                        imageContent.setSize(entity.getSize());
//                        imageContent.setHeight(entity.getHeight());
//                        imageContent.setWidth(entity.getWidth());
//
//                        imageContent.setThumbnailUrl(entity.getThumbnailUrl());
//                        imageContent.setThumbnailSize(entity.getThumbnailSize());
//                        imageContent.setThumbnailHeight(entity.getThumbnailHeight());
//                        imageContent.setThumbnailWidth(entity.getThumbnailWidth());
//
//                        message.setContent(imageContent.toStringContent());
//
//                        MessageReference.save(message.getRoomId(), message);
////                    MessageReference.saveByRoomId(message.getRoomId(), Lists.newArrayList(message));
//                    }
//
//                    if (iView.isThemeOpen() && themeMessage != null) {
////                        ChatService.getInstance().sendReplyImageMessage(session.getId(), messageId, entity.getWidth(), entity.getHeight(), entity.getUrl(), entity.getThumbnailUrl(), themeMessage.getId());
//                        ChatService.getInstance().sendReplyImageMessage(session.getId(), messageId, themeMessage.getId(), entity);
//
//                    } else {
//                        if (iView.isThemeOpen() && themeMessage != null) {
////                            ChatService.getInstance().sendImageMessage(session.getId(), messageId, entity.getWidth(), entity.getHeight(), entity.getUrl(), entity.getThumbnailUrl(), themeMessage.getId(), entity.getSize(), entity.getThumbnailSize());
//                            ChatService.getInstance().sendImageMessage(session.getId(), messageId, themeMessage.getId(), entity);
//                        } else {
////                            ChatService.getInstance().sendImageMessage(session.getId(), messageId, entity.getWidth(), entity.getHeight(), entity.getUrl(), entity.getThumbnailUrl(), "", entity.getSize(), entity.getThumbnailSize());
//                            ChatService.getInstance().sendImageMessage(session.getId(), messageId, "", entity);
//                        }
//                    }
//                    iView.updateSendVideoProgress();
//                    iView.setThemeOpen(false);
//                }
//
//                @Override
//                public void error(String errorMessage) {
//                    CELog.e(errorMessage);
//                    updateMsgStatus(session.getId(), messageId, MessageStatus.FAILED);
//                    iView.updateSendVideoProgress();
//                }
//            });
//        }
//    }
//
//    public void sendImage(String name, String path, Bitmap bitmap, boolean isFacebookReplyPublic) {
//        sendImage(name, path, bitmap, false, isFacebookReplyPublic);
//    }
//
//    public void sendImage(String name, String path, Bitmap bitmap) {
//        sendImage(name, path, bitmap, false, false);
//    }
//
//    public void sendImage(String name, String path, Bitmap bitmap,boolean isQuote, boolean isFacebookReplyPublic) {
//        //確認是否是強制接手
//        if (checkIsSnatch()) {
//            String agentName = UserProfileReference.findAccountName(null, session.getServiceNumberAgentId());
//            getSnatchDialog(String.format(weakReference.get().getString(R.string.text_agent_servicing_snatch_by_agent), agentName))
//                    .setOthers(new String[]{weakReference.get().getString(R.string.cancel), weakReference.get().getString(R.string.text_for_sure)})
//                    .setOnItemClickListener((o, position) -> {
//                        if (position == 1) {
//                            doAgentSnatchByAgent(weakReference.get(), session.getId(), () -> sendImage(name, path, bitmap));
//                            iView.setServicedGreenStatus();
//                        }
//                    })
//                    .build()
//                    .setCancelable(true)
//                    .show();
//            return;
//        }
//
//
//        if (recordMode) {
//            loadMsgDBDesc();
//            recordMode = false;
//        }
//        final String messageId = Tools.generateMessageId();
//        if(session != null) {
//            MessageEntity msg = MsgKitAssembler.assembleSendImageMessage(session.getId(), messageId, mSelfAccount.getId(), mSelfAccount.getAvatarId(), mSelfAccount.getNickName(), name, path, bitmap.getWidth(), bitmap.getHeight());
//            if (isFacebookReply()) {
//                sendFacebookAttachmentReply(isFacebookReplyPublic, name, path);
//                iView.showSendVideoProgress("圖片發送中");
//                return;
//            }
//            setReplyMsg(msg);
//            showMessage(msg);
//            if (isQuote) {
//                iView.showSendVideoProgress(weakReference.get().getString(R.string.text_image_quote_on_the_way));
//            } else {
//                iView.showSendVideoProgress("圖片發送中");
//            }
//
//            FileService.uploadFile(iView.getCtx(), true, token, Media.findByFileType(name), path, name, new AServiceCallBack<UploadManager.FileEntity, RefreshSource>() {
//                @Override
//                public void onProgress(float progress, long total) {
//                    updateMsgProgress(messageId, (int) (progress * 100));
//                }
//
//                @Override
//                public void complete(UploadManager.FileEntity entity, RefreshSource refreshSource) {
//                    if (entity == null) {
//                        error("上傳失敗");
//                        return;
//                    }
//
//                    if (Strings.isNullOrEmpty(entity.getUrl()) || Strings.isNullOrEmpty(entity.getThumbnailUrl())) {
//                        error("上傳失敗");
//                        return;
//                    }
//                    MessageEntity message = MessageReference.findById(messageId);
//                    if (message != null && message.content() instanceof ImageContent) {
//                        ImageContent imageContent = (ImageContent) message.content();
//
//                        imageContent.setUrl(entity.getUrl());
//                        imageContent.setSize(entity.getSize());
//                        imageContent.setHeight(entity.getHeight());
//                        imageContent.setWidth(entity.getWidth());
//
//                        imageContent.setThumbnailUrl(entity.getThumbnailUrl());
//                        imageContent.setThumbnailSize(entity.getThumbnailSize());
//                        imageContent.setThumbnailHeight(entity.getThumbnailHeight());
//                        imageContent.setThumbnailWidth(entity.getThumbnailWidth());
//
//                        message.setContent(imageContent.toStringContent());
//
//                        MessageReference.save(message.getRoomId(), message);
////                    MessageReference.saveByRoomId(message.getRoomId(), Lists.newArrayList(message));
//                    }
//
//                    if (iView.isThemeOpen() && themeMessage != null) {
////                        ChatService.getInstance().sendReplyImageMessage(session.getId(), messageId, entity.getWidth(), entity.getHeight(), entity.getUrl(), entity.getThumbnailUrl(), themeMessage.getId());
//                        ChatService.getInstance().sendReplyImageMessage(session.getId(), messageId, themeMessage.getId(), entity);
//
//                    } else {
//                        if (iView.isThemeOpen() && themeMessage != null) {
////                            ChatService.getInstance().sendImageMessage(session.getId(), messageId, entity.getWidth(), entity.getHeight(), entity.getUrl(), entity.getThumbnailUrl(), themeMessage.getId(), entity.getSize(), entity.getThumbnailSize());
//                            ChatService.getInstance().sendImageMessage(session.getId(), messageId, themeMessage.getId(), entity);
//                        } else {
////                            ChatService.getInstance().sendImageMessage(session.getId(), messageId, entity.getWidth(), entity.getHeight(), entity.getUrl(), entity.getThumbnailUrl(), "", entity.getSize(), entity.getThumbnailSize());
//                            ChatService.getInstance().sendImageMessage(session.getId(), messageId, "", entity);
//                        }
//                    }
//                    iView.updateSendVideoProgress();
//                    iView.setThemeOpen(false);
//                }
//
//                @Override
//                public void error(String errorMessage) {
//                    CELog.e(errorMessage);
//                    updateMsgStatus(session.getId(), messageId, MessageStatus.FAILED);
//                    iView.updateSendVideoProgress();
//                }
//            });
//        }
//    }
//
//    public void sendVideo(IVideoSize iVideoSize) {
//        sendVideo(iVideoSize, false);
//    }
//
//    public void sendVideo(IVideoSize iVideoSize, boolean isQuote) {
//        sendVideo(iVideoSize, isQuote, false);
//    }
//        /**
//         * 送出 video message
//         */
//    public void sendVideo(IVideoSize iVideoSize, boolean isQuote, boolean isFacebookReplyPublic) {
//
//        //確認是否是強制接手
//        if (checkIsSnatch()) {
//            String agentName = UserProfileReference.findAccountName(null, session.getServiceNumberAgentId());
//            getSnatchDialog(String.format(weakReference.get().getString(R.string.text_agent_servicing_snatch_by_agent), agentName))
//                    .setOthers(new String[]{weakReference.get().getString(R.string.cancel), weakReference.get().getString(R.string.text_for_sure)})
//                    .setOnItemClickListener((o, position) -> {
//                        if (position == 1) {
//                            doAgentSnatchByAgent(weakReference.get(), session.getId(), () -> sendVideo(iVideoSize));
//                            iView.setServicedGreenStatus();
//                        }
//                    })
//                    .build()
//                    .setCancelable(true)
//                    .show();
//            return;
//        }
//
//        if (recordMode) {
//            loadMsgDBDesc();
//            recordMode = false;
//        }
//
//        if (isFacebookReply()) {
//            sendFacebookAttachmentReply(isFacebookReplyPublic, iVideoSize.name(), iVideoSize.path());
//            iView.showSendVideoProgress(weakReference.get().getString(R.string.text_sending_video_message));
//            return;
//        }
//
//        if(session != null) {
//            if (isQuote) {
//                iView.showSendVideoProgress(weakReference.get().getString(R.string.text_video_quote_on_the_way));
//            } else {
//                iView.showSendVideoProgress(weakReference.get().getString(R.string.text_sending_video_message));
//            }
//            final String messageId = Tools.generateMessageId();
//            VideoContent videoContent =buildVideoContent(iVideoSize);
//            MessageEntity msg =  buildVideoMessage(messageId, videoContent);
//            /**
//             * 移除影片轉碼
//             */
////            boolean isCorrect = FfmpegUtil.INSTANCE.isVideoCodecCorrect(iVideoSize.path());
////            if(!isCorrect) {
////                FfmpegUtil.INSTANCE.convertVideoCodec(
////                        iVideoSize.path(),
////                        weakReference.get().getFilesDir().getAbsolutePath(),
////                        outputPath -> {
////                            if(outputPath == null) {
////                                iView.showErrorToast(weakReference.get().getString(R.string.text_transfer_video_failure));
////                            }else {
////                                try {
////                                    File file = new File(outputPath);
////                                    if(file.exists()) {
////                                        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
////                                        Uri contentUri = Uri.fromFile(file);
////                                        mediaScanIntent.setData(contentUri);
////                                        weakReference.get().sendBroadcast(mediaScanIntent);
////                                        onUploadFile(iVideoSize.name(), outputPath, messageId, videoContent, msg);
////                                    } else
////                                        iView.showErrorToast(weakReference.get().getString(R.string.text_transfer_video_failure));
////                                }catch (Exception e) {
////                                    iView.showErrorToast(weakReference.get().getString(R.string.text_transfer_video_failure));
////                                }
////                            }
////                            return null;
////                        },
////                        progress -> {
////                            iView.updateSendVideoProgress(true, progress); //更新影片轉碼進度
////                            return null;
////                        });
////            }else
//                //onUploadFile(iVideoSize.name(), iVideoSize.path(), messageId, videoContent, msg);
//            UploadManager.getInstance().onUploadFile(iView.getCtx(), iVideoSize.name(), messageId, token, MessageType.VIDEO, iVideoSize.path(), new UploadManager.OnUploadListener() {
//                @Override
//                public void onSuccess(String messageId, MessageType type, String response) {
//                    UploadManager.FileEntity entity = JsonHelper.getInstance().from(response, UploadManager.FileEntity.class);
////                    MessageEntity message = MessageReference.findById(messageId);
//                    if (!Strings.isNullOrEmpty(entity.getUrl()) && !Strings.isNullOrEmpty(entity.getName()) && entity.getSize() >= 0) {
//                        videoContent.setName(entity.getName());
//                        videoContent.setUrl(entity.getUrl());
//                        videoContent.setSize(entity.getSize());
//                        MessageReference.save(msg.getRoomId(), msg);
//                        setReplyMsg(msg);
//                        showMessage(msg);
//                        if (iView.isThemeOpen() && themeMessage != null) {
//                            ChatService.getInstance().sendReplyVideoMessage(session.getId(), messageId, themeMessage.getId(), videoContent, entity.getMD5(), entity.getThumbnailUrl(), entity.getThumbnailWidth(), entity.getThumbnailHeight());
//                        } else {
//                            ChatService.getInstance().sendVideoMessage(session.getId(), messageId, "", videoContent, entity.getMD5(), entity.getThumbnailUrl(), entity.getThumbnailWidth(), entity.getThumbnailHeight());
//                        }
//                        iView.setThemeOpen(false);
//                    }
//                    iView.updateSendVideoProgress();
//                }
//
//                @Override
//                public void onProgress(String messageId, int progress, long total) {
//                    updateMsgProgress(messageId, progress);
//                    iView.updateSendVideoProgress(progress);
//                }
//
//                @Override
//                public void onFailed(String reason) {
//                    setReplyMsg(msg);
//                    showMessage(msg);
//                    CELog.e(reason);
//                    updateMsgStatus(session.getId(), messageId, MessageStatus.FAILED);
//                    iView.dismissSendVideoProgress();
//                }
//            });
//        }
//    }
//
//    private void onUploadFile(String name, String path, String messageId, VideoContent videoContent, MessageEntity msg) {
//        UploadManager.getInstance().onUploadFile(iView.getCtx(), name, messageId, token, MessageType.VIDEO, path, new UploadManager.OnUploadListener() {
//                @Override
//                public void onSuccess(String messageId, MessageType type, String response) {
//                    UploadManager.FileEntity entity = JsonHelper.getInstance().from(response, UploadManager.FileEntity.class);
////                    MessageEntity message = MessageReference.findById(messageId);
//                        if (!Strings.isNullOrEmpty(entity.getUrl()) && !Strings.isNullOrEmpty(entity.getName()) && entity.getSize() >= 0) {
//                            videoContent.setName(entity.getName());
//                            videoContent.setUrl(entity.getUrl());
//                            videoContent.setSize(entity.getSize());
//                            MessageReference.save(msg.getRoomId(), msg);
//                            setReplyMsg(msg);
//                            showMessage(msg);
//                            if (iView.isThemeOpen() && themeMessage != null) {
//                                ChatService.getInstance().sendReplyVideoMessage(session.getId(), messageId, themeMessage.getId(), videoContent, entity.getMD5(), entity.getThumbnailUrl(), entity.getThumbnailWidth(), entity.getThumbnailHeight());
//                            } else {
//                                ChatService.getInstance().sendVideoMessage(session.getId(), messageId, "", videoContent, entity.getMD5(), entity.getThumbnailUrl(), entity.getThumbnailWidth(), entity.getThumbnailHeight());
//                            }
//                            iView.setThemeOpen(false);
//                    }
//                    iView.updateSendVideoProgress();
//                }
//
//                @Override
//                public void onProgress(String messageId, int progress, long total) {
//                    iView.updateSendVideoProgress(false, String.valueOf(progress)); //更新影片發送進度
//                    updateMsgProgress(messageId, progress);
//                }
//
//                @Override
//                public void onFailed(String reason) {
//                    setReplyMsg(msg);
//                    showMessage(msg);
//                    CELog.e(reason);
//                    updateMsgStatus(session.getId(), messageId, MessageStatus.FAILED);
//                    iView.dismissSendVideoProgress();
//                    iView.updateSendVideoProgress(false, "100");//影片發送失敗通知
//                }
//            });
//    }
//
//    private VideoContent buildVideoContent(IVideoSize iVideoSize) {
//        return new VideoContent(
//                iVideoSize.height(),
//                iVideoSize.width(),
//                iVideoSize.size(),
//                iVideoSize.path(),
//                iVideoSize.name(),
//                iVideoSize.path(),
//                iVideoSize.duration(),
//                false
//        );
//    }
//
//    private MessageEntity buildVideoMessage(String messageId, VideoContent videoContent) {
//        return new MessageEntity.Builder()
//                .id(messageId)
//                .avatarId(mSelfAccount.getAvatarId())
//                .roomId(session.getId())
//                .senderName(mSelfAccount.getNickName())
//                .senderId(mSelfAccount.getId())
//                .osType(AppConfig.osType)
//                .type(MessageType.VIDEO)
//                .flag(MessageFlag.OWNER)
//                .status(MessageStatus.SENDING)
//                .sendTime(System.currentTimeMillis())
//                .content(videoContent.toStringContent()).build();
//    }
//
//    public void sendGifImg(String daviciPath, String lacalPath, final int width, final int height) {
//        sendGifImg(daviciPath, lacalPath, width, height, false);
//    }
//    /**
//     * 送出圖片訊息(gif)
//     */
//    public void sendGifImg(String daviciPath, String lacalPath, final int width, final int height, boolean isFacebookReplyPublic) {
//        //確認是否是強制接手
//        if (checkIsSnatch()) {
//            String agentName = UserProfileReference.findAccountName(null, session.getServiceNumberAgentId());
//            getSnatchDialog(String.format(weakReference.get().getString(R.string.text_agent_servicing_snatch_by_agent), agentName))
//                    .setOthers(new String[]{weakReference.get().getString(R.string.cancel), weakReference.get().getString(R.string.text_for_sure)})
//                    .setOnItemClickListener((o, position) -> {
//                        if (position == 1) {
//                            doAgentSnatchByAgent(weakReference.get(), session.getId(), () -> sendGifImg(daviciPath, lacalPath, width, height));
//                            iView.setServicedGreenStatus();
//                        }
//                    })
//                    .build()
//                    .setCancelable(true)
//                    .show();
//            return;
//        }
//
//        if (isFacebookReply()) {
//            String path = DaVinci.with().getImageLoader().getAbsolutePath(daviciPath);
//            sendFacebookAttachmentReply(isFacebookReplyPublic, daviciPath, path);
//            iView.showSendVideoProgress("圖片發送中");
//            return;
//        }
//
//        if (recordMode) {
//            loadMsgDBDesc();
//            recordMode = false;
//        }
//        final String messageId = Tools.generateMessageId();
//        if(session != null) {
//            final MessageEntity msg = MsgKitAssembler.assembleSendImageMessage(session.getId(), messageId, mSelfAccount.getId(), mSelfAccount.getAvatarId(), mSelfAccount.getNickName(), lacalPath, lacalPath, width, height);
//            ImageContent imageContent = (ImageContent) msg.content();
//            imageContent.setWidth(width);
//            imageContent.setHeight(height);
//            setReplyMsg(msg);
//            showMessage(msg);
//            String path = DaVinci.with().getImageLoader().getAbsolutePath(daviciPath);
//            FileService.uploadFile(iView.getCtx(), true, token, Media.findByFileType(daviciPath), path, daviciPath, new AServiceCallBack<UploadManager.FileEntity, RefreshSource>() {
//
//                @Override
//                public void complete(UploadManager.FileEntity entity, RefreshSource refreshSource) {
//                    if (entity == null) {
//                        error("上傳失敗");
//                        return;
//                    }
//
//                    MessageEntity message = MessageReference.findById(messageId);
//                    if (message != null && message.content() instanceof ImageContent) {
//                        ImageContent imageContent = (ImageContent) message.content();
//
//                        imageContent.setUrl(entity.getUrl());
//                        imageContent.setSize(entity.getSize());
//                        imageContent.setHeight(entity.getHeight());
//                        imageContent.setWidth(entity.getWidth());
//
//                        imageContent.setThumbnailUrl(entity.getThumbnailUrl());
//                        imageContent.setThumbnailSize(entity.getThumbnailSize());
//                        imageContent.setThumbnailHeight(entity.getThumbnailHeight());
//                        imageContent.setThumbnailWidth(entity.getThumbnailWidth());
//
//                        message.setContent(imageContent.toStringContent());
//                        MessageReference.save(message.getRoomId(), message);
//                    }
//
//                    if (entity != null) {
//                        if (iView.isThemeOpen() && themeMessage != null) {
////                                ChatService.getInstance().sendReplyImageMessage(session.getId(), messageId, entity.getWidth(), entity.getHeight(), entity.getUrl(), entity.getThumbnailUrl(), themeMessage.getId());
//                            ChatService.getInstance().sendReplyImageMessage(session.getId(), messageId, themeMessage.getId(), entity);
//                        } else {
//                            if (iView.isThemeOpen() && themeMessage != null) {
////                                    ChatService.getInstance().sendImageMessage(session.getId(), messageId, entity.getWidth(), entity.getHeight(), entity.getUrl(), entity.getThumbnailUrl(), themeMessage.getId(), entity.getSize(), entity.getThumbnailSize());
//                                ChatService.getInstance().sendImageMessage(session.getId(), messageId, themeMessage.getId(), entity);
//                            } else {
////                                    ChatService.getInstance().sendImageMessage(session.getId(), messageId, entity.getWidth(), entity.getHeight(), entity.getUrl(), entity.getThumbnailUrl(), "", entity.getSize(), entity.getThumbnailSize());
//                                ChatService.getInstance().sendImageMessage(session.getId(), messageId, "", entity);
//                            }
//                        }
//                        iView.setThemeOpen(false);
//                        iView.updateSendVideoProgress();
//                    }
//                }
//
//                @Override
//                public void onProgress(float progress, long total) {
//                    updateMsgProgress(messageId, (int) (progress * 100));
//                }
//
//                @Override
//                public void error(String message) {
//                    CELog.e(message);
//                    iView.updateSendVideoProgress();
//                    updateMsgStatus(session.getId(), messageId, MessageStatus.FAILED);
//                }
//            });
//        }
//
////        UploadManager.getInstance().uploadImage(iView.getCtx(), daviciPath, messageId, token, DaVinci.with().getImageLoader().getAbsolutePath(daviciPath),
////                new UploadManager.OnImageUploadListener() {
////                    @Override
////                    public void onUploadSuccess(String messageId, UploadManager.FileEntity entity) {
////                        MessageEntity message = MessageReference.findById(messageId);
////                        if (message != null && message.content() instanceof ImageContent && entity != null) {
////                            ImageContent imageContent = (ImageContent) message.content();
////
////                            imageContent.setUrl(entity.getUrl());
////                            imageContent.setSize(entity.getSize());
////                            imageContent.setHeight(entity.getHeight());
////                            imageContent.setWidth(entity.getWidth());
////
////                            imageContent.setThumbnailUrl(entity.getThumbnailUrl());
////                            imageContent.setThumbnailSize(entity.getThumbnailSize());
////                            imageContent.setThumbnailHeight(entity.getThumbnailHeight());
////                            imageContent.setThumbnailWidth(entity.getThumbnailWidth());
////
////                            message.setContent(imageContent.toStringContent());
////                            MessageReference.save(message.getRoomId(), message);
////                        }
////
////                        if (entity != null) {
////                            if (iView.isThemeOpen() && themeMessage != null) {
//////                                ChatService.getInstance().sendReplyImageMessage(session.getId(), messageId, entity.getWidth(), entity.getHeight(), entity.getUrl(), entity.getThumbnailUrl(), themeMessage.getId());
////                                ChatService.getInstance().sendReplyImageMessage(session.getId(), messageId, themeMessage.getId(), entity);
////                            } else {
////                                if (iView.isThemeOpen() && themeMessage != null) {
//////                                    ChatService.getInstance().sendImageMessage(session.getId(), messageId, entity.getWidth(), entity.getHeight(), entity.getUrl(), entity.getThumbnailUrl(), themeMessage.getId(), entity.getSize(), entity.getThumbnailSize());
////                                    ChatService.getInstance().sendImageMessage(session.getId(), messageId, themeMessage.getId(), entity);
////                                } else {
//////                                    ChatService.getInstance().sendImageMessage(session.getId(), messageId, entity.getWidth(), entity.getHeight(), entity.getUrl(), entity.getThumbnailUrl(), "", entity.getSize(), entity.getThumbnailSize());
////                                    ChatService.getInstance().sendImageMessage(session.getId(), messageId, "", entity);
////                                }
////                            }
////                            iView.setThemeOpen(false);
////                        }
////                    }
////
////                    @Override
////                    public void onUploadIng(String messageId, int progress, long total) {
////                        updateMsgProgress(messageId, progress);
////                    }
////
////                    @Override
////                    public void onUploadFailed(String reason) {
////                        CELog.e(reason);
////                        updateMsgStatus(session.getId(), messageId, MessageStatus.FAILED);
////                    }
////                });
//    }
//
//    public void sendFile(final String path) {
//        sendFile(path, false);
//    }
//
//    public void sendFile(final String path, boolean isFacebookReplyPublic) {
//        //確認是否是強制接手
//        if (checkIsSnatch()) {
//            String agentName = UserProfileReference.findAccountName(null, session.getServiceNumberAgentId());
//            getSnatchDialog(String.format(weakReference.get().getString(R.string.text_agent_servicing_snatch_by_agent), agentName))
//                    .setOthers(new String[]{weakReference.get().getString(R.string.cancel), weakReference.get().getString(R.string.text_for_sure)})
//                    .setOnItemClickListener((o, position) -> {
//                        if (position == 1) {
//                            doAgentSnatchByAgent(weakReference.get(), session.getId(), () -> sendFile(path));
//                            iView.setServicedGreenStatus();
//                        }
//                    })
//                    .build()
//                    .setCancelable(true)
//                    .show();
//            return;
//        }
//
//        if (recordMode) {
//            loadMsgDBDesc();
//            recordMode = false;
//        }
//        final String messageId = Tools.generateMessageId();
//        int size = 0;
//        File file;
//        String fileName = "";
//        try {
//            file = new File(path);
//            fileName = file.getName();
//            FileInputStream fis = new FileInputStream(file);
//            size = fis.available();
//        } catch (Exception e) {
//            CELog.e("fileInputStreamError" + e);
//        }
//        if (isFacebookReply()) {
//            sendFacebookAttachmentReply(isFacebookReplyPublic, fileName, path);
//            return;
//        }
//        String baseUrl = TokenPref.getInstance(iView.getCtx()).getCurrentTenantUrl();
//        if(session != null) {
//            MessageEntity msg = MsgKitAssembler.assembleSendFileMessage(baseUrl, session.getId(), messageId, mSelfAccount.getId(), mSelfAccount.getAvatarId(), mSelfAccount.getNickName(), path, fileName, size, "");
//
//            setReplyMsg(msg);
//            showMessage(msg);
//
//            UploadManager.getInstance().uploadFile(iView.getCtx(), messageId, token, path, new UploadManager.OnFileUploadListener() {
//                @Override
//                public void onUploadSuccess(String messageId, UploadManager.FileEntity entity) {
//                    // listener.onUploadSuccess(messageId, entity.getName(), entity.getSize(), entity.getUrl());
//                    MessageEntity message = MessageReference.findById(messageId);
//                    if (message != null && message.content() instanceof FileContent) {
//                        ((FileContent) message.content()).setUrl(entity.getUrl());
//                        MessageReference.save(message.getRoomId(), message);
//                    }
//                    iView.updateSendVideoProgress();
//                    UploadSuccess(messageId, entity.getName(), entity.getSize(), path, entity.getUrl(), entity.getMD5());
//                }
//
//
//                @Override
//                public void onUploadIng(String messageId, int progress, long total) {
//                    updateMsgProgress(messageId, progress);
//                }
//
//                @Override
//                public void onUploadFailed(String reason, String messageId) {
//                    iView.updateSendVideoProgress();
//                    uploadFailed(reason, messageId);
//                }
//            });
//        }
//    }
//
//    public void uploadFailed(String reason, String messageId) {
//        CELog.e(reason);
//        if(session != null) updateMsgStatus(session.getId(), messageId, MessageStatus.FAILED);
//    }
//
//    public void UploadSuccess(String messageId, String name, int size, String localPath, String url, String md5) {
//        if (url != null) {
//            if(session != null) {
//                if (iView.isThemeOpen() && themeMessage != null) {
//                    ChatService.getInstance().sendReplyFileMessage(session.getId(), messageId, name, size, url, themeMessage.getId(), md5);
//                } else {
//                    if (iView.isThemeOpen() && themeMessage != null) {
//                        ChatService.getInstance().sendFileMessage(session.getId(), messageId, name, size, localPath, url, themeMessage.getId(), md5);
//                    } else {
//                        ChatService.getInstance().sendFileMessage(session.getId(), messageId, name, size, localPath, url, "", md5);
//                    }
//                }
//            }
//        }
//        iView.setThemeOpen(false);
//        iView.updateSendVideoProgress();
//    }
//
//    private void setReplyMsg(MessageEntity msg) {
//        if (iView.isChildRoomOpen()) {
//            return;
//        }
//        if (!iView.isThemeOpen()) {
//            return;
//        }
//        nearMessage = iView.getNearMessage();
//        themeMessage = iView.getThemeMessage();
//        if (nearMessage != null) {
//            msg.setNearMessageId(nearMessage.getId());
//            msg.setNearMessageType(nearMessage.getType());
//            msg.setNearMessageSenderId(nearMessage.getSenderId());
//            msg.setNearMessageSenderName(nearMessage.getSenderName());
//            msg.setNearMessageAvatarId(nearMessage.getAvatarId());
//            msg.setNearMessageContent(nearMessage.content().toStringContent());
//        }
//        if (themeMessage != null) {
//            msg.setThemeId(themeMessage.getId());
//        }
//    }
//
//    /**
//     * 檢查聊天室並取得聊天室實體
//     */
//    int count = 0;
//
//    public void checkRoomEntities(String[] roomIds, ServiceCallBack<List<String>, Enum> callBack) {
//        count = roomIds.length;
//        List<ChatRoomEntity> roomEntities = ChatRoomReference.getInstance().findByIds(userId, Lists.newArrayList(roomIds), true, true, true);
//        for (String roomId : roomIds) {
//            if (roomEntities.contains(ChatRoomEntity.Build().id(roomId).build())) {
//                count--;
//                if (count == 0) {
//                    callBack.complete(Lists.newArrayList(roomIds), null);
////                    presenter.sendScreenshotsImageToRooms(Lists.newArrayList(roomIds), screenshotsPath);
//                }
//            } else {
//                ChatRoomService.getInstance().getChatRoomItem(iView.getCtx(), userId, roomId, RefreshSource.REMOTE, new ServiceCallBack<ChatRoomEntity, RefreshSource>() {
//                    @Override
//                    public void complete(ChatRoomEntity entity, RefreshSource source) {
//                        count--;
//                        if (count == 0) {
//                            callBack.complete(Lists.newArrayList(roomIds), null);
////                            presenter.sendScreenshotsImageToRooms(Lists.newArrayList(roomIds), screenshotsPath);
//                        }
//                    }
//
//                    @Override
//                    public void error(String message) {
//                        count--;
//                        if (count == 0) {
//                            callBack.complete(Lists.newArrayList(roomIds), null);
////                            presenter.sendScreenshotsImageToRooms(Lists.newArrayList(roomIds), screenshotsPath);
//                        }
//                    }
//                });
//            }
//        }
//    }
//
//    /**
//     * 送出截圖分享
//     */
//    public void sendScreenshotsImageToRooms(List<String> roomIds, String filePath) {
////        String token = TokenSave.getInstance(iView.getCtx()).getToken();
//        String token = TokenPref.getInstance(this.iView.getCtx()).getTokenId();
//        String[] paths = PictureParse.parsePath(iView.getCtx(), filePath);
//
//        String path = DaVinci.with().getImageLoader().getAbsolutePath(paths[0]);
//        FileService.uploadFile(iView.getCtx(), true, token, Media.findByFileType(paths[0]), path, paths[0], new AServiceCallBack<UploadManager.FileEntity, RefreshSource>() {
//            @Override
//            public void onProgress(float progress, long total) {
////                updateMsgProgress(messageId, (int) (progress * 100));
//            }
//
//            @Override
//            public void complete(UploadManager.FileEntity fileEntity, RefreshSource refreshSource) {
//                for (int i = 0; i < roomIds.size(); i++) {
//                    String roomId = roomIds.get(i);
//                    String mMessageId = Tools.generateMessageId();
//                    ChatService.getInstance().sendImageMessage(roomId, mMessageId, "", fileEntity);
//                }
//            }
//
//            @Override
//            public void error(String errorMessage) {
//                CELog.e(errorMessage);
//
//            }
//        });
//    }
//
//    // EVAN_FLAG 2019-12-28 (1.9.0) 出现问题
//    private void showMessage(MessageEntity msg) {
//        if (iView.isChildRoomOpen()) {
//            String roomId = iView.getChildRoomId();
//            MessageReference.save(roomId, msg);
//            iView.displayChildRoomMessage(roomId, msg);
//            return;
//        }
//        if(session != null) {
//            MessageReference.save(session.getId(), msg);
//            if (iView.isThemeOpen()) {
//                if (!Strings.isNullOrEmpty(msg.getThemeId())) {
//                    iView.displayThemeMessage(msg.getThemeId(), true, msg);
//                }
//            }
//            iView.displayMainMessage(false, true, true, msg, false);
//        }
//    }
//
//    public void release() {
//        if (session != null) {
//            RepairMessageService.stop(iView.getCtx(), session.getId());
//        }
//        iView.getCtx().unregisterReceiver(mTimeZoneChangeReceiver);
//        AudioLib.getInstance(iView.getCtx()).stopPlay();//防止返回后还在播放語音的情况
//    }
//
//    public void clearSession() {
//        if (session != null) {
//            RepairMessageService.stop(iView.getCtx(), session.getId());
//        }
//        session = null;
//    }
//
//
//    public void deleteMessages(final List<MessageEntity> messages) {
//        List<String> msgIds = Lists.newArrayList();
//        List<String> failedIds = Lists.newArrayList();
//        for (MessageEntity m : messages) {
//            if (MessageStatus.FAILED_or_ERROR.contains(m.getStatus())) {
//                failedIds.add(m.getId());
//            } else {
//                msgIds.add(m.getId());
//            }
//        }
//        // 處理失敗、錯誤的訊息刪除邏輯
//        if (!failedIds.isEmpty()) {
//            String[] ids = new String[failedIds.size()];
//            for (int i = 0; i < failedIds.size(); i++) {
//                ids[i] = failedIds.get(i);
//            }
//            deleteDBMessages(ids);
//            if(session != null) EventBusUtils.sendEvent(new EventMsg(MsgConstant.CHANGE_LAST_MESSAGE, session.getId()));
//        }
//
//
//        if (session != null && !msgIds.isEmpty()) {
//            ApiManager.doMessageDelete(this.iView.getCtx(), session.getId(), msgIds, new ApiListener<String>() {
//                @Override
//                public void onSuccess(String s) {
//                    String[] ids = new String[msgIds.size()];
//                    for (int i = 0; i < msgIds.size(); i++) {
//                        ids[i] = msgIds.get(i);
//                    }
//                    deleteDBMessages(ids);
//
//                    EventBusUtils.sendEvent(new EventMsg(MsgConstant.CHANGE_LAST_MESSAGE, session.getId()));
//                }
//
//                @Override
//                public void onFailed(String errorMessage) {
//                    if (!TextUtils.isEmpty(errorMessage)) {
//                        iView.showErrorToast(errorMessage);
//                    }
//                }
//            });
//        }
//    }
//
//    private void deleteDBMessages(String... messageIds) {
//        if (messageIds == null) {
//            return;
//        }
//        MessageReference.deleteByIds(messageIds);
//        // EVAN_FLAG 2019-12-02 (1.8.0) 判斷是否還有 send failed message and update chat room fail content
//        for (String id : messageIds) {
//            iView.deleteMessage(id);
//        }
//    }
//
//
//    public void clearUnReadNumber(MessageEntity iMessage) {
////        if (session == null) {
////            return;
////        }
////        ChatRoomEntity chatRoomEntity = ChatRoomReference.getInstance().findById(session.getId());
////        if (chatRoomEntity == null) {
////            return;
////        }
////        session = chatRoomEntity;
////        if (session != null && !mIsCancel) {
////            List<MessageEntity> messages = MessageReference.findByRoomId(session.getId());
////            ArrayList<MessageEntity> list = Lists.newArrayList();
////            for (MessageEntity message : messages) {
////                list.add(message);
////            }
////
////            Collections.sort(list);
////            if (list != null && list.size() > 0
////                    && list.get(list.size() - 1).getSendTime() > iMessage.getSendTime()) {
////                iMessage = list.get(list.size() - 1);
////            }
////            session.setUnReadNum(0);
////
////            List<String> memberIds = AccountRoomRelReference.findMemberIdsByRoomId(null, session.getId());
////            session.setMemberIds(memberIds);
//////            session.setUpdateTime(iMessage.getSendTime());
////            ChatRoomReference.getInstance().save(session);
////            ChatRoomEntity iSession1 = ChatRoomReference.getInstance().findById(session.getId());
////            EventBusUtils.sendEvent(new EventMsg(MsgConstant.SESSION_UPDATE_FILTER, iSession1.getId()));
////        }
//    }
//
//    public void upDateSession(MessageEntity message) {
//        if(session == null) return;
////        Log.i("", "" + message);
////        session.setTime(message.getSendTime());
////        session.setContent(ChatRoomHelper.getChatRoomLastMessageContent(message));
////        session.setSenderName(message.getSenderName());
////        session.setMFrom(message.getFrom());
////        DBManager.getInstance().insertSession(session);
//        ChatRoomReference.getInstance().save(session);
////        session = ChatRoomReference.getInstance().findById(session.getId());
//        session = ChatRoomReference.getInstance().findById2( userId, session.getId(), true, true, true, true, true);
////        ChatService.getInstance().broadcastSession(session);
//        EventBusUtils.sendEvent(new EventMsg(MsgConstant.SESSION_UPDATE_FILTER, JsonHelper.getInstance().toJson(session)));
////        EventBusUtils.sendEvent(new EventMsg(MsgConstant.SESSION_UPDATE_FILTER, session.getId()));
//    }
//
////    public void getLostMessageForJocketDisConnnet() {
////        List<IMessage> iMessages = DBManager.getInstance().dimQueryLastReadMsg(session.getId());
////        if (iMessages != null && iMessages.size() > 0) {
////            messageListRequestAsc(iMessages.get(0));
////        }
////    }
//
////    public String getChatRoomLastMessageContent(IMessage message) {
//////        String content = "";
////        MsgFormatBase format = message.getFormat();
////        if (format == null) {
////            return "";
////        }
////
////        if (format instanceof TextMsgFormat) {
////            return ((TextMsgFormat) format).getContent();
//////            content = ((TextMsgFormat) format).getContent();
////        } else if (format instanceof StickerMsgFormat) {
////            return Constant.sticker_msg;
//////            content = Constant.sticker_msg;
////        } else if (format instanceof VoiceMsgFormat) {
////            return Constant.voice_msg;
//////            content = Constant.voice_msg;
////        } else if (format instanceof ImageMsgFormat) {
////            return Constant.image_msg;
//////            content = Constant.image_msg;
////        } else if (format instanceof ImageTextFormat) {
////            return Constant.image_msg_text;
//////            content = Constant.image_msg_text;
////        } else if (format instanceof FileMsgFormat) {
////            return Constant.file_msg;
//////            content = Constant.file_msg;
////        } else if (format instanceof CallMsgFormat) {
////            return ((CallMsgFormat) format).getContent();
//////            content = ((CallMsgFormat) format).getContent();
////        } else if (format instanceof TipMsgFormat) {
////            return ((TipMsgFormat) format).getContent();
//////            content = ((TipMsgFormat) format).getContent();
////        } else if (format instanceof BusinessTextMsgFormat) {
////            return ((BusinessTextMsgFormat) format).getBusinessContent();
//////            content = ((BusinessTextMsgFormat) format).getBusinessContent();
////        } else if (format instanceof AtTextMsgFormat) {
////            return ((AtTextMsgFormat) format).getContent();
//////            content = ((AtTextMsgFormat) format).getContent();
////        }
////
////        return "";
////    }
//
//
////    private void shareVoice2WeiChat(MessageEntity shareMessage) {
////        VoiceMsgFormat format = (VoiceMsgFormat) shareMessage.getFormat();
////        WXMusicObject music = new WXMusicObject();
////        music.musicUrl = format.getUrl();
////
////        WXMediaMessage msg = new WXMediaMessage();
////        msg.mediaObject = music;
////        msg.title = shareMessage.getId();
////        msg.description = "語音";
////        Bitmap thumb = BitmapFactory.decodeResource(iView.getCtx().getResources(), R.drawable.share_wechat);
////        msg.thumbData = Util.bmpToByteArray(thumb, true);
////
////        SendMessageToWX.Req req = new SendMessageToWX.Req();
////        req.transaction = buildTransaction("music");
////        req.message = msg;
////        req.scene = SendMessageToWX.Req.WXSceneTimeline;
////
////        api.sendReq(req);
////    }
//
////    public void shareImageToWeiChat(MessageEntity shareMessage) {
////        ImageMsgFormat format = (ImageMsgFormat) shareMessage.getFormat();
////
////        ImageEntity entity = DaVinci.with(iView.getCtx()).getImageLoader().getImage(format.getThumbnailUrl());
////        Bitmap bitmap = entity.getBitmap();
////        WXImageObject imgObj = new WXImageObject(bitmap);
////
////        WXMediaMessage msg = new WXMediaMessage();
////        msg.mediaObject = imgObj;
////        Bitmap thumbBmp = Bitmap.createScaledBitmap(bitmap, THUMB_SIZE, THUMB_SIZE, true);//缩略图大小
////        bitmap.recycle();
////        msg.thumbData = Util.bmpToByteArray(thumbBmp, true);  // 设置缩略图
////
////        SendMessageToWX.Req req = new SendMessageToWX.Req();
////        req.transaction = buildTransaction("img");
////        req.message = msg;
////        req.scene = SendMessageToWX.Req.WXSceneTimeline;
////        api.sendReq(req);
////    }
//
////    public void shareTextToWeiChat(MessageEntity shareMessage) {
////
////        WXTextObject textObject = new WXTextObject();
////        TextMsgFormat format = (TextMsgFormat) shareMessage.getFormat();
////
////        textObject.text = format.getContent();
////
////        WXMediaMessage msg = new WXMediaMessage();
////        msg.mediaObject = textObject;   // 发送文本类型的消息时，title字段不起作用
////        // message.title = "Will be ignored";
////        msg.description = "微信文本分享测试";   // 构造一个Req
////        SendMessageToWX.Req req = new SendMessageToWX.Req();
////        req.transaction = buildTransaction("text"); // transaction字段用于唯一标识一个请求
////        req.message = msg;   // 分享或收藏的目标场景，通过修改scene场景值实现。
////        // 发送到聊天界面 —— WXSceneSession
////        // 发送到朋友圈 —— WXSceneTimeline
////        // 添加到微信收藏 —— WXSceneFavorite
////        req.scene = SendMessageToWX.Req.WXSceneTimeline;
////        // 调用api接口发送数据到微信
////        api.sendReq(req);
////    }
//
////    public boolean isAppExist(String packageName) {
////        PackageManager manager = iView.getCtx().getPackageManager();
////        List<PackageInfo> pkgList = manager.getInstalledPackages(0);
////        for (int i = 0; i < pkgList.size(); i++) {
////            PackageInfo pI = pkgList.get(i);
////            if (pI.packageName.equalsIgnoreCase(packageName)) {
////                return true;
////            }
////        }
////        return false;
////    }
//
//
////    private File writeFileByBitmap2(Bitmap bitmap) {
////        File file = new File("/mnt/sdcard/pic/01.jpg");//将要保存图片的路径
////        try {
////            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
////            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
////            bos.flush();
////            bos.close();
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
////        return file;
////    }
//
//    public void share(MessageEntity message, String pg, String name) {
//        MessageType type = message.getType();
//
//        if (MessageType.AT_or_TEXT_or_IMAGE_or_VIDDO.contains(message.getType())) {
//            IMessageContent content = message.content();
//            if (content instanceof VideoContent) {
//                shareVideo((VideoContent) content, pg, name);
//                return;
//            }
//            if (content instanceof TextContent) {
//                shareText((TextContent) content, pg, name);
//                return;
//            }
//
//            if (content instanceof AtContent) {
//                shareAt((AtContent) content, pg, name);
//                return;
//            }
//
//            if (content instanceof ImageContent) {
//                try {
//                    MessageEntity entity = MessageReference.findById(message.getId());
//                    shareImage((ImageContent) entity.content(), pg, name);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                return;
//            }
//        }
//
////        MsgFormatBase format = shareMessage.getFormat();
////        if (format instanceof TextMsgFormat) {
////            shareText((TextMsgFormat) format, pg, name);
////        }
////        if (format instanceof ImageMsgFormat) {
////            try {
////                MessageEntity entity = MessageReference.findById(shareMessage.getId());
////                shareImage((ImageMsgFormat) entity.getFormat(), pg, name);
////            } catch (Exception e) {
////
////            }
////        }
////
////        if (format instanceof AtTextMsgFormat) {
////            shareAt((AtTextMsgFormat) format, pg, name);
////        }
//        /*if (isAppExist(pg)) {
//        } else {
//            ToastUtils.showToast(iView.getCtx(), "您未安裝该应用，無法進行分享哦");
//        }*/
//    }
//
//    /**
//     * 分享 文字訊息
//     */
//    public void shareText(TextContent textContent, String pg, String name) {
//        Intent sendIntent = new Intent();
//        if(session != null) sendIntent.putExtra(BundleKey.ROOM_ID.key(), session.getId());
//        sendIntent.setAction(Intent.ACTION_SEND);
//        sendIntent.setType("text/plain");
//        sendIntent.putExtra(Intent.EXTRA_TEXT, textContent.getText());
//        iView.getCtx().startActivity(sendIntent);
//    }
//
//    /**
//     * 分享視頻到外部App
//     * version 1.10.0 待定
//     */
//    public void shareVideo(VideoContent videoContent, String pg, String name) {
//        Intent sendIntent = new Intent();
//        if(session != null) sendIntent.putExtra(BundleKey.ROOM_ID.key(), session.getId());
//        sendIntent.setAction(Intent.ACTION_SEND);
//        sendIntent.setType("video/mp4");
//        sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(videoContent.getAndroid_local_path()));
//        iView.getCtx().startActivity(sendIntent);
//    }
//
//
//    /**
//     * 可分享標註訊息，且格式文字
//     */
//    public void shareAt(AtContent atContent, String pg, String name) {
//        Intent sendIntent = new Intent();
//        String content = "";
//        try {
//            List<MentionContent> ceMentions = atContent.getMentionContents();
//            if(session != null) {
//                SpannableStringBuilder ssb = AtMatcherHelper.matcherAtUsers("@", ceMentions, this.session.getMembersTable());
//                content = ssb.toString();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        if (Strings.isNullOrEmpty(content)) {
//            iView.showErrorToast("標註訊息格式錯誤！");
//            return;
//        }
//        if(session != null) sendIntent.putExtra(BundleKey.ROOM_ID.key(), session.getId());
//        sendIntent.setAction(Intent.ACTION_SEND);
//        sendIntent.setType("text/plain");
//        sendIntent.putExtra(Intent.EXTRA_TEXT, content);
//        iView.getCtx().startActivity(sendIntent);
//    }
//
//
//    /**
//     * 分享 圖片訊息
//     */
//    private void shareImage(final ImageContent imageContent, final String pg, final String name) {
//        String path = imageContent.getUrl();
//        ThreadExecutorHelper.getIoThreadExecutor().execute(() -> {
//            String errorMsg = "";
//            if (URLUtil.isValidUrl(path)) {
//                try {
//                    Bitmap bitmap = Glide.with(iView.getCtx()).asBitmap().load(path).submit().get();
//                    if (bitmap != null) {
//                        Uri img = Uri.parse(MediaStore.Images.Media.insertImage(iView.getCtx().getContentResolver(), bitmap, null, null));
//                        Intent sendIntent = new Intent();
//                        if(session != null) sendIntent.putExtra(BundleKey.ROOM_ID.key(), session.getId());
//                        sendIntent.setAction(Intent.ACTION_SEND);
//                        sendIntent.setType("image/*");
//                        sendIntent.putExtra(Intent.EXTRA_STREAM, img);
//                        iView.getCtx().startActivity(sendIntent);
//                    } else {
//                        errorMsg = "分享失敗";
//                    }
//                } catch (Exception e) {
//                    errorMsg = "分享失敗";
//                    CELog.e(e.getMessage(), e);
//                }
//            } else {
//                ImageEntity entity = DaVinci.with(iView.getCtx()).getImageLoader().getImage(path);
//                if (entity != null) {
//                    Uri img = Uri.parse(MediaStore.Images.Media.insertImage(iView.getCtx().getContentResolver(), entity.getBitmap(), null, null));
//                    Intent sendIntent = new Intent();
//                    if(session != null) sendIntent.putExtra(BundleKey.ROOM_ID.key(), session.getId());
//                    sendIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
//                    sendIntent.setType("image/*");
//                    sendIntent.putExtra(Intent.EXTRA_STREAM, img);
//                    iView.getCtx().startActivity(sendIntent);
//                } else {
//                    errorMsg = "分享失敗";
//                }
//            }
//            if (!Strings.isNullOrEmpty(errorMsg)) {
//                String finalErrorMsg = errorMsg;
//                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> iView.showErrorToast(finalErrorMsg));
//            }
//        });
//
//
////        ImageEntity entity = DaVinci.with(iView.getCtx()).getImageLoader().getImage(path);
////        if (entity != null) {
////            Uri img = Uri.parse(MediaStore.Images.Media.insertImage(iView.getCtx().getContentResolver(), entity.getBitmap(), null, null));
////            Intent sendIntent = new Intent();
////            sendIntent.setAction(Intent.ACTION_SEND);
////            sendIntent.setType("image/*");
////            sendIntent.putExtra(Intent.EXTRA_STREAM, img);
////            iView.getCtx().startActivity(sendIntent);
////            return;
////        } else {
////            iView.showLoadingView(Constant.Dialog_Loading);
////            String sToken = TokenSave.getInstance(iView.getCtx()).getToken();
////            OkHttpUtils.get()
////                    .url(imageContent.getUrl() + "?tokenId=" + sToken)
////                    .tag(this)
////                    .build()
////                    .execute(new BitmapCallback() {
////                        @Override
////                        public void onError(Call call, Exception e, int id) {
////                            iView.hideLoadingView();
////                            ToastUtils.showToast(iView.getCtx(), e.toString());
////                        }
////
////                        @Override
////                        public void onResponse(Bitmap bitmap, int id) {
////                            iView.hideLoadingView();
////                            Uri img = Uri.parse(MediaStore.Images.Media.insertImage(iView.getCtx().getContentResolver(), bitmap, null, null));
////                            Intent sendIntent = new Intent();
////                            sendIntent.putExtra(BundleKey.ROOM_ID.key(), session.getId());
////                            sendIntent.setAction(Intent.ACTION_SEND);
////                            sendIntent.setType("image/*");
////                            sendIntent.putExtra(Intent.EXTRA_STREAM, img);
////                            iView.getCtx().startActivity(sendIntent);
////                        }
////                    });
////        }
//    }
//
//    public void initProvisionalMemberList(List<String> memberIds) {
//        iView.onCompleteProvisionalMemberList(getMemberProfileEntity(memberIds), memberIds);
//    }
//
//    public List<UserProfileEntity> getMemberProfileEntity(List<String> memberIds) {
//        List<UserProfileEntity> memberItems = Lists.newArrayList();
//        for(String id : memberIds)
//            memberItems.add(DBManager.getInstance().queryFriend(id));
//        return memberItems;
//    }
//    public void addProvisionalMember(List<String> newMemberIds, List<UserProfileEntity> entities) {
////        List<UserProfileEntity> newEntities = Lists.newArrayList(entities);
////        for(String id : newMemberIds)
////            newEntities.add(DBManager.getInstance().queryFriend(id));
//        iView.onAddProvisionalMember(newMemberIds);
//    }
//
//    public void removeProvisionalMember(Context ctx, String roomId, String memberId) {
//        ApiManager.doMemberDelete(ctx, roomId, Lists.newArrayList(memberId), new ApiListener<String>() {
//            @Override
//            public void onSuccess(String s) {
//            }
//            @Override
//            public void onFailed(String errorMessage) {
//                iView.showErrorMsg(errorMessage);
//            }
//        });
//    }
//
//    //確認是否是強制接手
//    public boolean checkIsSnatch() {
//        return ServiceNumberType.PROFESSIONAL.equals(session.getServiceNumberType())
//                && session.getServiceNumberAgentId() != null && !session.getServiceNumberAgentId().isEmpty()
//                && !session.getServiceNumberAgentId().equals(userId) && !session.getOwnerId().equals(userId)
//                && (!session.getProvisionalIds().contains(userId) && session.getListClassify() != ChatRoomSource.MAIN); //臨時成員聊天室
//    }
//
//    public void clearIsAtMeFlag(Context context, String roomId) {
//        NetworkManager.INSTANCE.provideRetrofit(context).create(tw.com.chainsea.chat.view.chat.ChatService.class).sendAtMessageRead(new SendAtMessageReadRequest(roomId)).enqueue(new Callback<CommonResponse<Object>>() {
//            @Override
//            public void onResponse(Call<CommonResponse<Object>> call, Response<CommonResponse<Object>> response) {
//                ChatRoomReference.getInstance().updateIsAtMeFlag(roomId, false);
//            }
//
//            @Override
//            public void onFailure(Call<CommonResponse<Object>> call, Throwable t) {
//            }
//        });
//    }
//}
