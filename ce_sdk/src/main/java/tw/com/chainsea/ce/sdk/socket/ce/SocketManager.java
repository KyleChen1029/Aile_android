package tw.com.chainsea.ce.sdk.socket.ce;

import static tw.com.chainsea.ce.sdk.event.MsgConstant.NOTICE_SERVICE_NUMBER_ADD_FROM_PROVISIONAL;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.List;
import java.util.Set;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.engineio.client.transports.Polling;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.WebSocket;
import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.client.ClientsManager;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.SdkLib;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
import tw.com.chainsea.ce.sdk.database.DBContract;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.database.sp.UserPref;
import tw.com.chainsea.ce.sdk.event.EventBusUtils;
import tw.com.chainsea.ce.sdk.event.EventMsg;
import tw.com.chainsea.ce.sdk.event.MsgConstant;
import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.reference.UserProfileReference;
import tw.com.chainsea.ce.sdk.socket.ce.bean.AckBean;
import tw.com.chainsea.ce.sdk.socket.ce.bean.MessageBean;
import tw.com.chainsea.ce.sdk.socket.ce.bean.NoticeBean;
import tw.com.chainsea.ce.sdk.socket.ce.bean.ReceiveMessageBean;
import tw.com.chainsea.ce.sdk.socket.ce.code.NoticeCode;
import tw.com.chainsea.ce.sdk.socket.ce.code.NoticeName;
import tw.com.chainsea.ce.sdk.socket.ce.code.OnEventCode;
import tw.com.chainsea.ce.sdk.socket.ce.listener.OnConnectListener;
import tw.com.chainsea.ce.sdk.socket.ce.listener.OnDisconnectListener;
import tw.com.chainsea.ce.sdk.socket.ce.listener.OnListener;
import tw.com.chainsea.ce.sdk.socket.ce.listener.OnMessageListener;
import tw.com.chainsea.ce.sdk.socket.cp.model.GuarantorJoinContent;

/**
 * current by evan on 2020-09-29
 *
 * @author Evan Wang
 * date 2020-09-29
 * SocketManager.init
 */
public class SocketManager {
    static Socket instance = null;
    static IO.Options options;

    static OnNoticeListener listener;
    static String deviceName = null;

    private static final int DISCONNECT_MSG = 1001;
    private static final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == DISCONNECT_MSG) {
                if (instance != null && !instance.connected()) {
                    refreshConnect();
                }
                handler.sendEmptyMessageDelayed(DISCONNECT_MSG, 2000L);
            }
        }
    };

    static {
        options = new IO.Options();
        options.reconnection = true;
        options.reconnectionDelay = 1000L;
        options.reconnectionDelayMax = 5000L;
        options.reconnectionAttempts = Integer.MAX_VALUE;
//        options.transports = new String[]{Polling.NAME, WebSocket.NAME};
        options.transports = new String[]{Polling.NAME};
        options.secure = true;
        options.timeout = 30 * 1000L;
        options.timestampRequests = true;

        OkHttpClient client = ClientsManager.initClient()
//                .threadPoolNumber(1)
            .giveLog(true)
//                .logger(logger)
            .single(true)
            .newSocketBuild();

        IO.setDefaultOkHttpWebSocketFactory((WebSocket.Factory) client);
        IO.setDefaultOkHttpCallFactory((Call.Factory) client);
        options.callFactory = (Call.Factory) client;
        options.webSocketFactory = (WebSocket.Factory) client;
    }

    public static void init(String url, String namespace, String query, OnNoticeListener listener) {
        CELog.d("[CE Socket] init() at time:" + System.currentTimeMillis());
        deviceName = TokenPref.getInstance(SdkLib.getAppContext()).getDeviceName();
        if (SocketManager.listener == null) {
            SocketManager.listener = listener;
        }
        start(url, namespace, query);
    }

    public static void start(String url, String namespace, String query) {
        CELog.d("[CE Socket] start() at time:" + System.currentTimeMillis());
        if (instance != null) {
            instance.close();
            instance = null;
        }

        try {
            options.forceNew = true;
            options.query = query;
            Manager manager = new Manager(new URI(url), options);
            instance = manager.socket(namespace);
            onEvent(instance, true);
            instance.connect();
        } catch (Exception e) {
            CELog.e("[CE Socket] " + e.getMessage());
        }
    }

    private static void onEvent(Socket instance, boolean ackEnable) {
        if (instance != null) {
            instance.on(OnEventCode.CONNECTION.getCode(), new OnListener(OnEventCode.CONNECTION.getCode()) {
                    @Override
                    public void call(String data) {
                        CELog.d("[CE Socket] connection " + data);
                    }
                })
                .on(OnEventCode.CONNECT.getCode(), new OnConnectListener() {
                    @Override
                    protected void connect(String data) {
                        CELog.d("[CE Socket] connect] " + data);
                        if (listener != null) {
                            listener.onConnected();
                        }
                        handler.removeMessages(DISCONNECT_MSG);
                    }
                })
                .on(OnEventCode.DISCONNECT.getCode(), new OnDisconnectListener() {
                    @Override
                    protected void disconnect(String data) {
                        CELog.d("[CE Socket] disconnect]" + data);
                        if (!handler.getLooper().isCurrentThread()) {
                            handler.sendEmptyMessageDelayed(DISCONNECT_MSG, 2000L);
                        }
                    }
                })
                .on(OnEventCode.MESSAGE.getCode(), new OnMessageListener(ackEnable) {
                    @Override
                    protected void confirm(Ack ack, MessageEntity item) {
                        CELog.d("[CE Socket] confirm " + item.toJson());
                        emitAck(ack, AckBean.Build().ack(true)
                            .event(NoticeName.CONFIRM.getName())
                        );
                        if (listener != null) {
                            listener.onConfirm(item);
                        }
                    }

                    @Override
                    protected void notice(Ack ack, NoticeCode noticeCode, JSONObject content) {
                        CELog.d("[CE Socket] notice " + noticeCode.getName());
                        try {
                            handlingNoticeEvents(noticeCode, content);
                            emitAck(ack, AckBean.Build().ack(true)
                                .event(NoticeName.NOTICE.getName())
                                .code(noticeCode.getName())
                            );
                        } catch (JSONException e) {
                            CELog.e("[CE Socket] " + e.getMessage());
                        }
                    }

                    @Override
                    protected void messageNew(Ack ack, List<MessageEntity> items) {
                        CELog.d("[CE Socket] message new " + items.size());
                        if (listener != null) {
                            listener.onMessagesNew(ack, items);
                        }

                        if (!items.isEmpty()) {
                            MessageEntity entity = items.get(0);
                            String roomId = entity.getRoomId();
                            String currentRoomId = UserPref.getInstance(SdkLib.getAppContext()).getCurrentRoomId();
                            CELog.d("[CE Socket] ack message new currentRoomId :: " + currentRoomId + ", roomId :: " + roomId + ", action :: " + (currentRoomId.equals(roomId) ? "read" : "received"));
                            emitAck(ack, AckBean.Build().ack(true)
                                .event(NoticeName.MESSAGE_NEW.getName())
                                .content(entity.content().simpleContent())
                                .action(currentRoomId.equals(roomId) ? "read" : "received")
                            );
                        }
                    }

                    @Override
                    protected void messageOffline(Ack ack, List<MessageEntity> items) {
                        CELog.d("[CE Socket] message offline " + items.size());
                        emitAck(ack, AckBean.Build().ack(true)
                            .event(NoticeName.MESSAGE_OFFLINE.getName())
                            .content("offline")
                        );
                        if (listener != null) {
                            listener.onMessagesOffline(ack, items);
                        }
                    }

                    @Override
                    protected void callEvent(Ack ack, String data) {
                        CELog.d("[CE Socket] call " + data);
                    }

                    @Override
                    protected void socketError(Ack ack, String errorMessage) {
                        CELog.d("[CE Socket] Error " + errorMessage);
//                            ThreadExecutorHelper.getHandlerExecutor().execute(SocketManager::refreshConnect, 0L);
                    }
                });
        }
    }

    public static void emitAck(Ack ack, AckBean.AckBeanBuilder builder) {
        if (ack != null) {
            if (deviceName == null) {
                deviceName = TokenPref.getInstance(SdkLib.getAppContext()).getDeviceName();
            }
            builder.deviceName(deviceName);
            try {
                JSONObject jsonObject = builder.build().toJsonObject();
                ack.call(jsonObject);
            } catch (Exception e) {
                CELog.e("[CE Socket] " + e.getMessage());
            }
        }
    }

    public static void disconnect() {
        if (instance != null) {
            CELog.d("[CE Socket] disconnect() " + System.currentTimeMillis());
            instance.disconnect();
            instance.off();
            instance.close();
        }
    }

    public static void close() {
        if (instance != null) {
            CELog.d("[CE Socket] close() " + System.currentTimeMillis());
            instance.disconnect();
            instance.close();
            instance = null;
        }
    }

    public static void reconnect() {
        if (instance != null) {
            CELog.d("[CE Socket] reconnect() --> instance.close() " + System.currentTimeMillis());
            instance.close();
        }

        CELog.d("[CE Socket] reconnect() --> reStart() " + System.currentTimeMillis());
        String url = TokenPref.getInstance(SdkLib.getAppContext()).getSocketIoUrl();
        String namespace = TokenPref.getInstance(SdkLib.getAppContext()).getSocketIoNameSpace();
        String query = TokenPref.getInstance(SdkLib.getAppContext()).getSocketQuery();
        start(url, namespace, query);
        Log.d("Kyle113", "SocketManager reconnect() syncOfflineMessageList");

        //Sync offline message when socket reconnect, set sequence start from 0
        syncOfflineMessageList(0);
    }

    private static void syncOfflineMessageList(int sequence) {
        ApiManager.getInstance().syncOfflineMessageList(SdkLib.getAppContext(), sequence, new ApiListener<>() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                Log.d("Kyle113", "syncOfflineMessageList jsonObject: " + jsonObject.toString());
                try {
                    int sequence1 = jsonObject.getInt("sequence");
                    boolean hasNextPage = jsonObject.getBoolean("hasNextPage");
                    JSONArray itemsArray = jsonObject.getJSONArray("items");
                    for (int i = 0; i < itemsArray.length(); i++) {

                        JSONObject item = itemsArray.getJSONObject(i);
                        ReceiveMessageBean receiveBean = ReceiveMessageBean.socketSpecialFrom(item);
                        String data = receiveBean.getData();
                        if (data != null && !data.isEmpty() && receiveBean.getName() != null) {
                            Log.d("Kyle113", "syncOfflineMessageList data= " + data + ", name= " + receiveBean.getName() + ", sequece = " + sequence1 + ", hasNextPage = " + hasNextPage);
                            switch (receiveBean.getName()) {
                                case MESSAGE_NEW:
                                    Log.d("Kyle113", "syncOfflineMessageList MESSAGE_NEW");
                                    MessageBean messageBean = JsonHelper.getInstance().from(data, MessageBean.class);
                                    if (messageBean != null) {
                                        if (listener != null) {
                                            listener.onMessagesNew(null, messageBean.getItems());
                                        }
                                    }
                                    break;
                                case CONFIRM:
                                    Log.d("Kyle113", "syncOfflineMessageList CONFIRM");
                                    MessageEntity entity = JsonHelper.getInstance().from(data, MessageEntity.class);
                                    if (listener != null) {
                                        listener.onConfirm(entity);
                                    }
                                    break;
                                case NOTICE:
                                    Log.d("Kyle113", "syncOfflineMessageList NOTICE");
                                    NoticeBean noticeBean = JsonHelper.getInstance().socketSpecialFrom(data, NoticeBean.class);
                                    if (noticeBean != null) {
                                        if (noticeBean.getCode() != null && noticeBean.getContent() != null) {
                                            try {
                                                handlingNoticeEvents(noticeBean.getCode(), noticeBean.getContent());
                                            } catch (JSONException e) {
                                                CELog.e("[CE Socket] " + e.getMessage());
                                            }
                                        }
                                    } else {
                                        CELog.e("[CE Socket] UnHandle notice:" + receiveBean.getName());
                                    }
                                    break;
                                case MESSAGE_OFFLINE:
                                    Log.d("Kyle113", "syncOfflineMessageList MESSAGE_OFFLINE");
                                    MessageBean messageOfflineBean = JsonHelper.getInstance().from(data, MessageBean.class);
                                    if (listener != null) {
                                        listener.onMessagesOffline(null, messageOfflineBean.getItems());
                                    }
                                    break;
                                default:
                                    break;
                            }
                        } else {
                            Log.e("Kyle113", "SocketManager reconnect() syncOfflineMessageList  data or name is null ");
                            throw new JSONException("");
                        }
                    }

                    if (hasNextPage) {
                        syncOfflineMessageList(sequence1);
                    } else {
                        if (itemsArray.length() > 0) {
                            Log.d("Kyle113", "SocketManager reconnect() cleanOfflineMessage");
                            //clean offline message from server
                            ApiManager.getInstance().cleanOfflineMessage(SdkLib.getAppContext(), new ApiListener<>() {
                                @Override
                                public void onSuccess(JSONObject jsonObject) {
                                    Log.d("Kyle113", "SocketManager reconnect() cleanOfflineMessage onSuccess jsonObject = " + jsonObject);
                                }

                                @Override
                                public void onFailed(String errorMessage) {
                                    Log.e("Kyle113", "SocketManager reconnect() cleanOfflineMessage onFailed errorMessage = " + errorMessage);
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    Log.e("Kyle113", "SocketManager reconnect() cleanOfflineMessage exception = " + e.getMessage());
                }
            }

            @Override
            public void onFailed(String errorMessage) {
                Log.e("Kyle113", "SocketManager reconnect() syncOfflineMessageList errorMessage = " + errorMessage);
            }
        });
    }

    private static void refreshConnect() {
        CELog.d("[CE Socket] refreshConnect() " + System.currentTimeMillis());
        reconnect();
    }

    private static String keepUpdateUserAvatar = "";
    private final static String UPDATE_PROFILE_BIRTHDAY = "Update_Profile_Birthday";
    private final static String UPDATE_PROFILE_GENDER = "Update_Profile_Gender";
    private final static String UPDATE_PROFILE_NICKNAME = "Update_Profile_NickName";

    private static void handlingNoticeEvents(NoticeCode code, JSONObject contentJson) throws JSONException {
        if (code == null || listener == null) {
            return;
        }
        CELog.d(String.format("[CE Socket] Notice code: %s, content: %s", code, contentJson));
        switch (code) {
            case READ_RECEIVED:
                if (JsonHelper.getInstance().has(contentJson, "messageId", "receivedNum", "readedNum", "sendNum")) {
                    listener.onNotice(contentJson.getString("messageId"), contentJson.getInt("receivedNum"), contentJson.getInt("readedNum"), contentJson.getInt("sendNum"));
                } else {
                    throw new JSONException("socket lack data");
                }
                break;
            case RETRACT_MESSAGE:
                if (JsonHelper.getInstance().has(contentJson, "roomId", "messageId", "flag")) {
                    listener.onRetractMessage(contentJson.getString("roomId"), contentJson.getString("messageId"), contentJson.getInt("flag"));
                } else {
                    throw new JSONException("socket lack data");
                }
                break;
            case CREATE_ROOM:
                String createRoomName = contentJson.has("name") ? contentJson.getString("name") : "";
                if (JsonHelper.getInstance().has(contentJson, "type", "userId", "roomId", "memberIds")) { //創建新服務號通知
                    listener.createNewServiceNumber(contentJson.getString("type"));
                } else if (contentJson.has("roomId")) {
                    listener.modifyRoomName(createRoomName, contentJson.getString("roomId"));
                } else {
                    throw new JSONException("socket lack data");
                }
                break;
            case UPGRADE_ROOM:
                String upgradeRoomName = contentJson.has("name") ? contentJson.getString("name") : "";
                if (contentJson.has("roomId")) {
                    listener.UpgradeRoom(upgradeRoomName, contentJson.getString("roomId"));
                } else {
                    throw new JSONException("socket lack data");
                }
                break;
            case ADD_ROOM_MEMBER: // 加成員或是加服務號臨時成員
            case INVITE_ADD_ROOM:
                String type = contentJson.optString("type");
                if (ChatRoomType.discuss.name().equals(type) || ChatRoomType.group.name().equals(type)) {
                    EventBus.getDefault().post(new EventMsg<>(MsgConstant.NOTICE_DISCUSS_MEMBER_ADD, contentJson));
                } else if (ChatRoomType.services.name().equals(type)) {
                    // 臨時成員
                    EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.NOTICE_PROVISIONAL_MEMBER_ADDED, contentJson));
                } else if (ChatRoomType.serviceMember.name().equals(type)) {
                    // 服務號加成員
                    EventBusUtils.sendEvent(new EventMsg(MsgConstant.NOTICE_PROVISIONAL_MEMBER_ADDED, contentJson));

                } else if (JsonHelper.getInstance().has(contentJson, "memberIds", "userId", "roomId", "type")) {
                    //邀請多人聊天室/社團
                    EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.NOTICE_SERVICE_NUMBER_REFRESH_BY_DB, contentJson));
                } else {
                    throw new JSONException("socket lack data");
                }
                break;
            case USER_EXIT:
                if (JsonHelper.getInstance().has(contentJson, "userId", "roomId", "name", "avatarId", "memberIds")) {
                    listener.userExit(contentJson.getString("userId"), contentJson.getString("roomId"), contentJson.getString("name"), contentJson.getString("avatarId"), Lists.newArrayList());
                } else if (JsonHelper.getInstance().has(contentJson, "userId", "roomId", "memberIds", "deletedMemberIds", "type")) {
                    Set<String> idSet = JsonHelper.getInstance().fromToSet(contentJson.getJSONArray("deletedMemberIds").toString(), String[].class);
                    listener.userExit(contentJson.getString("userId"), contentJson.getString("roomId"), "", "", Lists.newArrayList(idSet));
                } else if (JsonHelper.getInstance().has(contentJson, "name", "type", "userId", "roomId", "memberIds")) {
                    //多人聊天室有人離開
                    if (contentJson.optString("type").equals("discuss")) {
                        EventBus.getDefault().post(new EventMsg<>(MsgConstant.NOTICE_DISCUSS_MEMBER_EXIT, contentJson));
                    }
                } else {
                    throw new JSONException("socket lack data");
                }
                break;
            case DELETE_ROOM_MEMBER: // 刪除臨時成員
                if (JsonHelper.getInstance().has(contentJson, "deletedMemberIds", "userId", "roomId", "type", "memberIds")) {
                    //多人聊天室成員 or 社團成員被移除
                    if (contentJson.optString("type").equals("discuss") || contentJson.optString("type").equals("group")) {
                        EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.NOTICE_DISCUSS_GROUP_MEMBER_REMOVED, contentJson));
                    } else {
                        Set<String> idSet = JsonHelper.getInstance().fromToSet(contentJson.getJSONArray("deletedMemberIds").toString(), String[].class);
                        EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.NOTICE_PROVISIONAL_MEMBER_REMOVED, idSet));
                    }
                } else if (JsonHelper.getInstance().has(contentJson, "deletedMemberIds", "userId", "roomId", "name", "avatarId", "memberIds")) {
                    Set<String> idSet = JsonHelper.getInstance().fromToSet(contentJson.getJSONArray("deletedMemberIds").toString(), String[].class);
                    listener.onDeleteFromRoom(contentJson.getString("userId"), contentJson.getString("roomId"), Lists.newArrayList(idSet));
                } else {
                    throw new JSONException("socket lack data");
                }
                break;
            case DISMISS_ROOM:
                if (JsonHelper.getInstance().has(contentJson, "type", "roomId")) {
                    String roomType = contentJson.getString("type");
                    if ("private".equals(roomType)) {
                        listener.dismissSingleRoom(contentJson.getString("userId"));
                    } else if ("group".equals(roomType)) {
                        listener.dismissGroupRoom(contentJson.getString("roomId"));
                    } else if ("discuss".equals(roomType)) {
                        EventBus.getDefault().post(new EventMsg<>(MsgConstant.NOTICE_DISMISS_DISCUSS_ROOM, contentJson.optString("roomId")));
                    } else if ("serviceMember".equals(roomType) || ("broadcast").equals(roomType)) {
                        //服務號禁用
                        EventBus.getDefault().post(new EventMsg<>(MsgConstant.NOTICE_SERVICE_NUMBER_DISABLE, contentJson.getString("roomId")));
                    }
                } else
                    throw new JSONException("socket lack data");
                break;
            case UPDATE_ROOM_NAME:
                if (JsonHelper.getInstance().has(contentJson, "roomId", "roomId")) {
                    listener.modifyRoomName(contentJson.getString("name"), contentJson.getString("roomId"));
                } else {
                    throw new JSONException("socket lack data");
                }
                break;
            case TRANSFER_OWNER:
                if (JsonHelper.getInstance().has(contentJson, "ownerId", "roomId")) {
                    listener.transfer_Owner(contentJson.getString("ownerId"), contentJson.getString("roomId"));
                } else {
                    throw new JSONException("socket lack data");
                }
                break;
            case ADD_ADDRESS_BOOK:
                if (JsonHelper.getInstance().has(contentJson, "userId")) {
                    listener.onAddFriend(contentJson.getString("userId"));
                } else {
                    throw new JSONException("socket lack data");
                }
                break;
            case DELETE_FRIEND:
                if (JsonHelper.getInstance().has(contentJson, "userId", "roomId")) {
                    listener.onDeleteFriend(contentJson.getString("userId"), contentJson.getString("roomId"));
                } else {
                    throw new JSONException("socket lack data");
                }
                break;
//            case SQUEEZED_OUT:
//                listener.squeezedOut();
//                break;
            case OTHER_DEVICE_LOGIN:
                if (JsonHelper.getInstance().has(contentJson, "message")) {
                    listener.otherDeviceLogin(contentJson.getString("message"));
                } else {
                    throw new JSONException("socket lack data");
                }
                break;
            case UPDATE_PROFILE:
                String key = "";
                String value = "";
                if (JsonHelper.getInstance().has(contentJson, "nickName")) {
                    String nickName = contentJson.getString("nickName");
                    key = UPDATE_PROFILE_NICKNAME;
                    value = nickName;
                } else if (contentJson.has("gender")) {
                    String gender = contentJson.getString("gender");
                    key = UPDATE_PROFILE_GENDER;
                    value = gender;
                } else if (contentJson.has("birthday")) {
                    String birthday = contentJson.getString("birthday");
                    key = UPDATE_PROFILE_BIRTHDAY;
                    value = birthday;
                } else {
                    CELog.d("[CE Socket] Analyze notifications of friend information" + contentJson);
                }
                listener.onUpdateProfile(contentJson.getString("userId"), key, value, contentJson);
                break;
            case UPDATE_CUSTOMER_PROFILE:
                EventBus.getDefault().post(new EventMsg<JSONObject>(MsgConstant.UPDATE_CUSTOMER_NAME, contentJson));
                break;
            case UPDATE_USER_AVATAR:
                if (JsonHelper.getInstance().has(contentJson, "avatarId", "userId")) {
                    String userId = contentJson.getString("userId");
                    String selfUserId = TokenPref.getInstance(SdkLib.getAppContext()).getUserId();
                    String keep = contentJson.getString("userId") + "_" + contentJson.getString("avatarId");
                    if (selfUserId.equals(userId)) {
                        UserProfileReference.updateByCursorNameAndValues(null, selfUserId, DBContract.UserProfileEntry.COLUMN_AVATAR_URL, contentJson.getString("avatarId"));
                        EventBus.getDefault().post(new EventMsg<>(MsgConstant.NOTICE_REFRESH_HOMEPAGE_AVATAR));
                    } else if (!keepUpdateUserAvatar.equals(keep)) {
                        keepUpdateUserAvatar = keep;
                        listener.onUpdateUserAvatar(contentJson.getString("userId"), contentJson.getString("avatarId"));
                    }
                } else {
                    throw new JSONException("socket lack data");
                }
                break;
            case UPDATE_GROUP_AVATAR:
                if (JsonHelper.getInstance().has(contentJson, "avatarId", "userId", "userId")) {
                    listener.onUpdateGroupAvatar(contentJson.getString("roomId"), contentJson.getString("userId"), contentJson.getString("avatarId"));
                } else {
                    throw new JSONException("socket lack data");
                }
                break;
            case PUBLISH_MOOD:
                if (JsonHelper.getInstance().has(contentJson, "userId", "mood")) {
                    listener.onPublishMood(contentJson.getString("userId"), contentJson.getString("mood"));
                } else {
                    throw new JSONException("socket lack data");
                }
                break;
            case SYNC_READ:
                if (JsonHelper.getInstance().has(contentJson, "timeStamp", "roomId", "messageId")) {
                    listener.onSyncRead(contentJson.getString("roomId"), contentJson.getString("messageId"));
                } else {
                    throw new JSONException("socket lack data");
                }
                break;
            case SERVICE_REGISTER_AGENT: // Service account members start service
                if (JsonHelper.getInstance().has(contentJson, "roomId", "serviceNumberAgentId")) {
                    listener.onServiceRegisterAgent(contentJson.getString("roomId"), contentJson.getString("serviceNumberAgentId"));
                } else {
                    throw new JSONException("socket lack data");
                }
                break;
            case SERVICE_RELEASE_AGENT: // Service account member ends service
                if (JsonHelper.getInstance().has(contentJson, "roomId")) {
                    listener.onServiceReleaseAgent(contentJson.getString("roomId"));
                } else {
                    throw new JSONException("socket lack data");
                }
                break;
            case SERVICE_APPOINT_OFFLINE:
            case SERVICE_APPOINT_ONLINE:
                if (JsonHelper.getInstance().has(contentJson, "roomId")) {
                    listener.onCheckingAppointStatus(contentJson.getString("roomId"));
                } else {
                    throw new JSONException("socket lack data");
                }
                break;
            case DELETE_MESSAGE:
                CELog.d("[CE Socket] " + contentJson);
                break;
            case BUSINESS_BINDING_ROOM:
                if (JsonHelper.getInstance().has(contentJson, "roomId", "businessId", "businessName", "businessCode")) {
                    listener.onBusinessBindingRoom(contentJson.getString("roomId"), contentJson.getString("businessId"), contentJson.getString("businessName"), contentJson.getString("businessCode"));
                } else {
                    throw new JSONException("socket lack data");
                }
                break;
            case CE_NOTICE_DISMISS_BUSINESS_OBJECT_ROOM:
                if (JsonHelper.getInstance().has(contentJson, "userId", "roomId", "type")) {
                    listener.onDismissBusinessRoom(contentJson.getString("userId"), contentJson.getString("roomId"), contentJson.getString("type"));
                } else {
                    throw new JSONException("socket lack data");
                }
                break;
            case NOTICE_CANCEL_TOP_ROOM:
                if (JsonHelper.getInstance().has(contentJson, "roomId")) {
                    listener.onCancelTopRoom(contentJson.getString("roomId"));
                } else {
                    throw new JSONException("socket lack data");
                }
                break;
            case NOTICE_TOP_ROOM:
                if (JsonHelper.getInstance().has(contentJson, "roomId", "topTime")) {
                    listener.onTopRoom(contentJson.getString("roomId"), contentJson.getLong("topTime"));
                } else {
                    throw new JSONException("socket lack data");
                }
                break;
            case NOTICE_MUTE_ROOM:
                if (JsonHelper.getInstance().has(contentJson, "roomId", "isMute")) {
                    listener.onMuteRoom(contentJson.getString("roomId"), contentJson.getBoolean("isMute"));
                } else {
                    throw new JSONException("socket lack data");
                }
                break;
            case NOTICE_MUTE_USER:
                if (JsonHelper.getInstance().has(contentJson, "userId", "isMute")) {
                    listener.onMuteUser(contentJson.getString("userId"), contentJson.getBoolean("isMute"));
                } else {
                    throw new JSONException("socket lack data");
                }
                break;
            case NOTICE_TODO:
                if (JsonHelper.getInstance().has(contentJson, "id", "userId", "osType", "event")) {
                    listener.onTodoEvent(contentJson.getString("id"), contentJson.getString("userId"), contentJson.getString("osType"), contentJson.getString("event"));
                } else {
                    throw new JSONException("socket lack data");
                }
                break;
            case NOTICE_BROADCAST_EVENT:
                if (JsonHelper.getInstance().has(contentJson, "event", "roomId", "messageId")) {
                    String event = contentJson.getString("event");
                    if (Sets.newHashSet("AssignStart", "AssignComplete").contains(event)) {
                        listener.onBroadcastAssignEvent(contentJson.getString("roomId"), contentJson.getString("messageId"), event);
                    }
                    if (JsonHelper.getInstance().has(contentJson, "userId", "osType")) {
                        listener.onBroadcastEvent(contentJson.getString("userId"), contentJson.getString("roomId"), contentJson.getString("messageId"), event, contentJson.getString("osType"));
                    }
                } else {
                    throw new JSONException("socket lack data");
                }
                break;
            case NOTICE_SERVICE_NUMBER_MEMBER:
                String serviceNumberEvent = contentJson.optString("event");
                Set<String> idSet = JsonHelper.getInstance().fromToSet(contentJson.getJSONArray("memberIds").toString(), String[].class);
                // 從臨時成員轉成服務人員
                if ("AddFromProvisional".equals(serviceNumberEvent)) {
                    EventBus.getDefault().post(new EventMsg<>(NOTICE_SERVICE_NUMBER_ADD_FROM_PROVISIONAL, contentJson));
                    listener.onServiceNumberMemberEvent(contentJson.getString("serviceNumberId"), serviceNumberEvent, idSet);
                } else {
                    if (JsonHelper.getInstance().has(contentJson, "serviceNumberId", "event", "memberIds")) {
                        if (!idSet.isEmpty()) {
                            listener.onServiceNumberMemberEvent(contentJson.getString("serviceNumberId"), serviceNumberEvent, idSet);
                        }
                    } else {
                        throw new JSONException("socket lack data");
                    }
                }
                break;
            case NOTICE_SERVICE_NUMBER:
                // event ['TransferCancel' , 'TransferComplete' ,'TransferStart']
                if (JsonHelper.getInstance().has(contentJson, "srcRoomId", "event", "userId", "consultRoomId")) {
//                    listener.onServiceNumberTransferEvent(contentJson.getString("userId"), contentJson.getString("roomId"), contentJson.getString("serviceNumberId"), contentJson.getString("event"));
                    String event = contentJson.getString("event");
                    if (Sets.newHashSet("ConsultStart", "ConsultComplete").contains(event)) {
                        listener.onServiceNumberConsultEvent(contentJson.getString("userId"), contentJson.getString("srcRoomId"), contentJson.getString("consultRoomId"), contentJson.getString("event"));
                    }
                } else if (JsonHelper.getInstance().has(contentJson, "serviceNumberId", "event", "userId", "roomId", "transferReason")) { //換手
                    EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.SERVICE_NUMBER_TRANSFER_STATUS, contentJson.getString("roomId")));
                } else if (JsonHelper.getInstance().has(contentJson, "serviceNumberId", "event", "userId", "roomId")) {//取消換手
//                    listener.onServiceNumberTransferEvent(contentJson.getString("userId"), contentJson.getString("roomId"), contentJson.getString("serviceNumberId"), contentJson.getString("event"));
                    EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.SERVICE_NUMBER_TRANSFER_STATUS, contentJson.getString("roomId")));
                } else {
                    throw new JSONException("socket lack data");
                }
                break;
            case NOTICE_ROBOT_WARNING:
                if (JsonHelper.getInstance().has(contentJson, "roomId")) {
                    EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.NOTICE_ROBOT_SERVICE_WARNED, contentJson));
                }
                break;
            case NOTICE_ROBOT_LAST_MESSAGE:
                if (JsonHelper.getInstance().has(contentJson, "roomId")) {
                    EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.UPDATE_ROBOT_SERVICE_LIST));
                }
                break;
            case NOTICE_ROBOT_STOP:
                if (JsonHelper.getInstance().has(contentJson, "roomId")) {
                    EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.NOTICE_ROBOT_STOP, contentJson));
                }
                break;
            // 重新加入保證人
            case NOTICE_GUARANTOR_JOIN:
                GuarantorJoinContent guarantorJoinContent = JsonHelper.getInstance().from(contentJson.toString(), GuarantorJoinContent.class);
                EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.NOTICE_GUARANTOR_JOIN, guarantorJoinContent));
                break;
            case NOTICE_GUARANTOR_JOIN_AGREE:
                EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.NOTICE_GUARANTOR_JOIN_AGREE, contentJson));
                break;
            case NOTICE_GUARANTOR_JOIN_REJECT:
                EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.NOTICE_GUARANTOR_JOIN_REJECT));
                break;
            case NOTICE_INTELLIGENT_ASSISTANCE:
                EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.NOTICE_INTELLIGENT_ASSISTANCE, contentJson));
                break;
            case NOTICE_AT:
                EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.MESSAGE_AT, contentJson));
                break;
            case NOTICE_COMMENT:
                String commentEvent = contentJson.optString("event");
                switch (commentEvent) {
                    case "Create":
                        // 有新留言
                        EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.FACEBOOK_COMMENT_CREATE, contentJson));
                        break;
                    case "Update":
                        // 編輯留言
                        EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.FACEBOOK_COMMENT_UPDATE, contentJson));
                        break;
                    case "Delete":
                        // 刪除留言
                        EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.FACEBOOK_COMMENT_DELETE, contentJson));
                        break;
                }
                break;
            case NOTICE_POST:
                String postEvent = contentJson.optString("event");
                switch (postEvent) {
                    case "Create":
                        // 有新貼文
                        break;
                    case "Update":
                        // 編輯貼文
                        break;
                    case "Delete":
                        // 刪除貼文
                        EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.FACEBOOK_POST_DELETE, contentJson));
                        break;
                }
                break;
            default:
                CELog.d("[CE Socket] default   code::" + code + "\n" + "contentJson::" + contentJson);
                break;
        }

    }

    public interface OnNoticeListener {
        void onConnected();

        void onMessagesNew(Ack ack, List<MessageEntity> entities);

        void onMessagesOffline(Ack ack, List<MessageEntity> entities);

        void onConfirm(MessageEntity msgEntity);

        void onNotice(String msgId, int receivedNum, int readNum, int sendNum);

        void onUpdateProfile(String userId, String updateProfileKey, String updateProfileValue, JSONObject jsonObject);

        void onUpdateUserAvatar(String userId, String avatarId);

        void onUpdateGroupAvatar(String roomId, String userId, String avatarId);

        void onRetractMessage(String roomId, String messageId, int flag);

        void squeezedOut();

        void dismissGroupRoom(String roomId);

        void userExit(String userId, String roomId, String roomName, String avatarUrl, List<String> deletedMemberIds);

        void dismissSingleRoom(String userId);

        void onAddFriend(String userId);

        void onDeleteFriend(String userId, String roomId);

        void onDeleteFromRoom(String userId, String roomId, List<String> deletedMemberIdsList);

        void transfer_Owner(String ownerId, String roomId);

        void modifyRoomName(String newName, String roomId);

        void UpgradeRoom(String newName, String roomId);

        void otherDeviceLogin(String message);

        void onPublishMood(String userId, String mood);

        void onCallEnd();

        void onSyncRead(String roomId, String messageId);

        void onServiceRegisterAgent(String roomId, String serviceNumberAgentId);

        void onServiceReleaseAgent(String roomId);

        void onCheckingAppointStatus(String roomId);

        void onBusinessBindingRoom(String roomId, String businessId, String businessName, String businessCode);

        void onDismissBusinessRoom(String userId, String roomId, String type);

        void onTopRoom(String roomId, long topTime);

        void onCancelTopRoom(String roomId);

        void onMuteRoom(String roomId, boolean isMute);

        void onMuteUser(String userId, boolean isMute);

        void onTodoEvent(String id, String userId, String osType, String event);

        void onBroadcastEvent(String userId, String roomId, String messageId, String event, String osType);

        void onServiceNumberMemberEvent(String serviceNumberId, String event, Set<String> memberIds);

        void onBroadcastAssignEvent(String roomId, String messageId, String event);

        void onServiceNumberTransferEvent(String userId, String roomId, String serviceNumberId, String event);

        void onServiceNumberConsultEvent(String userId, String srcRoomId, String consultRoomId, String event);

        void createNewServiceNumber(String type);

        void dismissServiceNumberRoom(String roomId);
    }
}
