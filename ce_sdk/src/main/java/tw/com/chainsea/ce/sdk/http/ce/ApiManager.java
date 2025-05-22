package tw.com.chainsea.ce.sdk.http.ce;

import static tw.com.chainsea.android.common.system.DeviceHelper.getUUID;
import static tw.com.chainsea.ce.sdk.database.DBContract.REFRESH_TIME_SOURCE.ADDRESS_BOOK_SYNC;
import static tw.com.chainsea.ce.sdk.database.DBContract.REFRESH_TIME_SOURCE.BOSSSERVICENUMBER_CONTACT_LIST;
import static tw.com.chainsea.ce.sdk.database.DBContract.REFRESH_TIME_SOURCE.CHAT_ROOM_ROBOT_SERVICE_LIST;
import static tw.com.chainsea.ce.sdk.database.DBContract.REFRESH_TIME_SOURCE.SYNC_CONTACT;
import static tw.com.chainsea.ce.sdk.database.DBContract.REFRESH_TIME_SOURCE.SYNC_EMPLOYEE;
import static tw.com.chainsea.ce.sdk.database.DBContract.REFRESH_TIME_SOURCE.SYNC_SERVICENUMBER;
import static tw.com.chainsea.ce.sdk.database.DBContract.REFRESH_TIME_SOURCE.SYNC_TODO;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.system.DeviceHelper;
import tw.com.chainsea.ce.sdk.base.MsgBuilder;
import tw.com.chainsea.ce.sdk.bean.CrowdEntity;
import tw.com.chainsea.ce.sdk.bean.CustomerEntity;
import tw.com.chainsea.ce.sdk.bean.account.Gender;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.broadcast.BroadcastMessageBean;
import tw.com.chainsea.ce.sdk.bean.broadcast.TopicEntity;
import tw.com.chainsea.ce.sdk.bean.label.Label;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity;
import tw.com.chainsea.ce.sdk.bean.sticker.StickerItemEntity;
import tw.com.chainsea.ce.sdk.bean.sticker.StickerPackageEntity;
import tw.com.chainsea.ce.sdk.bean.todo.TodoEntity;
import tw.com.chainsea.ce.sdk.config.AppConfig;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.database.sp.UserPref;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.http.ce.request.*;
import tw.com.chainsea.ce.sdk.http.ce.response.RoomRecentRsp;
import tw.com.chainsea.ce.sdk.http.cp.respone.RefreshTokenResponse;
import tw.com.chainsea.ce.sdk.service.listener.ApiCallback;
import tw.com.chainsea.ce.sdk.service.type.ChatRoomSource;
import tw.com.chainsea.ce.sdk.socket.ce.SocketManager;

/**
 * ApiManager
 * Created by 90Chris on 2016/6/7.
 */
public class ApiManager {
    private static ApiManager INSTANCE;
    private static final Map<String, String> requestTasks = Maps.newConcurrentMap();

    public static void addRequestTask(String taskName, String taskValue) {
        requestTasks.put(taskName, taskValue);
//        Log.d("TAG","requestTasks :: add-->" + taskName + ", size-->" + requestTasks.size());
    }

    public static void removeRequestTask(String taskName) {
        requestTasks.remove(taskName);
//        Log.d("TAG", "requestTasks :: remove-->" + taskName + ", size-->" + requestTasks.size());
    }

    public static ApiManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ApiManager();
        }
        return INSTANCE;
    }

    private ApiManager() {
    }

    /**
     * set account info
     *
     * @param ctx     context
     * @param account user id of self
     * @param pw      password
     * @param lan     language
     *                //@param deviceType device type, like pad/phone/desktop
     */
    public void setAccount(Context ctx, String countryCode, String loginMode, String account, String pw, String lan, boolean isPad, String uniqueID, String identifyBy, String identifyValue) {
        String device = DeviceHelper.getDeviceName(ctx);
        TokenPref.getInstance(ctx)
            .setLoginMode(loginMode)
            .setAccountNumber(account)
            .setPassword(pw)
            .setLanguage(lan)
            .setUniqueID(uniqueID)
            .setCountryCode(countryCode)
            .setOsType(AppConfig.osType)
            .setDeviceName(Strings.isNullOrEmpty(device) ? "" : device)
            .setDeviceType(isPad ? "pad" : Build.MODEL);
    }

    //CP登入
    public void setCEAccount(Context context, String countryCode, String account, String tenantCode, String authToken) {
        TokenPref.getInstance(context)
            .setLoginMode("AuthTokenLogin")
            .setCountryCode(countryCode)
            .setAccountNumber(account)
            .setDeviceType(Build.MODEL)
            .setOsType(AppConfig.osType)
            .setUniqueID(getUUID())
            .setDeviceName(DeviceHelper.getDeviceName(context))
            .setTenantCode(tenantCode)
            .setAuthToken(authToken);
    }

    public boolean isCEAccountAlive(Context context) {
        String countryCode = TokenPref.getInstance(context).getCountryCode();
        String account = TokenPref.getInstance(context).getAccountNumber();
        String tenantCode = TokenPref.getInstance(context).getTenantCode();
        String authToken = TokenPref.getInstance(context).getAuthToken();
        return (!countryCode.isEmpty() && !account.isEmpty() && !tenantCode.isEmpty() && !authToken.isEmpty());
    }

    public static void startSocket(String url, String namespace, String query, SocketManager.OnNoticeListener onNoticeListener) {
        SocketManager.init(url, namespace, query, onNoticeListener);
    }

    public void closeSocket() {
        SocketManager.disconnect();
    }

    public void searchEmployee(Context ctx, UserSearchRequest.Listener listener, String keyword, int index) {
        try {
            JSONObject jsonObject = new JSONObject()
                .put("keyword", keyword)
                .put("pageSize", 10)
                .put("pageIndex", index);
            new UserSearchRequest(ctx, listener)
                .setMainThreadEnable(true)
                .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    public void addContact(Context ctx, ApiListener<String> listener, String userId, String alias) {
        try {
            JSONObject requestJson = new JSONObject()
                .put("alias", alias)
                .put("userIds", new JSONArray().put(userId));
            new ContactAddRequest(ctx, listener)
                .setMainThreadEnable(true)
                .request(requestJson);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    public void blockContact(Context ctx, String userId, boolean isBlock, @Nullable ApiListener<String> listener) {
        try {
            JSONObject requestJson = new JSONObject()
                .put("userId", userId)
                .put("block", isBlock);
            new BlockFriendRequest(ctx, listener)
                .setMainThreadEnable(true)
                .request(requestJson);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }


    // report
    public static void doRoomComplaint(Context ctx, String objectUserId, String objectType, String content, @Nullable ApiListener<String> listener) {
        try {
            JSONObject requestJson = new JSONObject()
                .put("objectUserId", objectUserId)
                .put("type", content)
                .put("objectType", objectType)
                .put("content", content);
            new RoomComplaintRequest(ctx, listener)
                .setMainThreadEnable(true)
                .request(requestJson);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    public static void setAlias(Context ctx, ApiListener<String> listener, String userId, String alias) {
        try {
            JSONObject requestJson = new JSONObject()
                .put("userId", userId)
                .put("alias", alias);
            new CustomFriendInfoRequest(ctx, listener)
                .setMainThreadEnable(true)
                .request(requestJson);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    /**
     * Get the member information of the chat room according to the room id
     */
    public static void doMemberList(Context ctx, String roomId, @Nullable ApiListener<List<UserProfileEntity>> listener) {
        try {
            new MemberListRequest(ctx, listener)
                .setMainThreadEnable(false)
                .request(new JSONObject().put("roomId", roomId));
        } catch (Exception e) {
            listener.onFailed(e.getMessage());
            CELog.e(e.getMessage());
        }
    }

    public static void doMemberDelete(Context ctx, String groupId, List<CharSequence> memberIds, @Nullable ApiListener<String> listener) {
        try {
            JSONArray jsonArray = new JSONArray();
            for (CharSequence memberId : memberIds) {
                jsonArray.put(memberId);
            }
            JSONObject requestJson = new JSONObject()
                .put("roomId", groupId)
                .put("userIds", jsonArray);
            new MemberDeleteRequest(ctx, listener)
                .setMainThreadEnable(true)
                .request(requestJson);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    public static void doRoomDismiss(Context ctx, String groupId, @Nullable ApiListener<String> listener) {
        try {
            new RoomDismissRequest(ctx, listener)
                .setMainThreadEnable(true)
                .request(new JSONObject().put("roomId", groupId));
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    public void syncOfflineMessageList(Context ctx, int sequence, @Nullable ApiListener<JSONObject> listener) {
        try {
            new SyncOffLineMessageRequest(ctx, listener)
                .setMainThreadEnable(false)
                .request(new JSONObject()
                    .put("pageSize", 100)
                    .put("sequence", sequence)
                );
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    public void cleanOfflineMessage(Context ctx, @Nullable ApiListener<JSONObject> listener) {
        try {
            new CleanOfflineMessageRequest(ctx, listener)
                .setMainThreadEnable(false)
                .request();
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    public static void doTenantDismiss(Context ctx, @Nullable ApiListener<String> listener) {
        try {
            new TenantDismissRequest(ctx, listener)
                .setMainThreadEnable(true)
                .request();
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    public static void doTenantServiceNumberList(Context ctx, @Nullable ApiListener<String> listener) {
        try {
            new TenantServiceNumberListRequest(ctx, listener)
                .setMainThreadEnable(true)
                .request();
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }


    public static void doTenantEmployeeDel(Context ctx, String userId, @Nullable ApiListener<String> listener) {
        try {
            JSONObject requestJson = new JSONObject()
                .put("userId", userId);
            new TenantEmployeeDelRequest(ctx, listener)
                .setMainThreadEnable(true)
                .request(requestJson);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    public static void doChatMemberExit(Context ctx, String groupId, String ownerId, @Nullable ApiListener<String> listener) {
        try {
            JSONObject requestJson = new JSONObject()
                .put("roomId", groupId)
                .put("ownerId", ownerId);
            new ChatMemberExitRequest(ctx, listener)
                .setMainThreadEnable(true)
                .request(requestJson);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    /**
     * Update chat room name
     */
    public void updateGroupName(Context ctx, ApiListener<String> listener, String groupId, String name) {
        try {
            JSONObject jsonObject = new JSONObject()
                .put("name", name);
            JSONObject requestJson = new JSONObject()
                .put("roomId", groupId)
                .put("data", jsonObject);
            new GroupUpdateRequest(ctx, listener)
                .setMainThreadEnable(true)
                .request(requestJson);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    public void changeGroupName(Context ctx, String roomId, String name, @Nullable ApiListener<String> listener) {
        try {
            JSONObject jsonObject = new JSONObject()
                .put("roomId", roomId)
                .put("data", new JSONObject()
                    .put("name", name)
                );
            new ChangeGroupNameRequest(ctx, listener)
                .setMainThreadEnable(true)
                .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    public static void updateProfile(Context ctx, UserProfileEntity account, @Nullable ApiListener<String> listener) {

        JSONObject jsonObject = new JSONObject();
        try {
            String loginName = account.getLoginName();
            long mobile = account.getMobile();
            String email = account.getEmail();
            Gender gender = account.getGender();
            String otherPhone = account.getOtherPhone();
            String alias = account.getAlias();
            String mood = account.getMood();
            String nickname = account.getNickName();
            String birthday = account.getBirthday();
            if (!TextUtils.isEmpty(loginName)) {
                jsonObject.put("loginName", loginName);
            }
            if (mobile > 0) {
                jsonObject.put("mobile", mobile);
            }
            if (!TextUtils.isEmpty(email)) {
                jsonObject.put("email", email);
            }
            if (gender != null) {
                jsonObject.put("gender", gender.getValue());
            }
            if (!TextUtils.isEmpty(otherPhone)) {
                jsonObject.put("otherPhone", otherPhone);
            }
            if (!TextUtils.isEmpty(alias)) {
                jsonObject.put("alias", alias);
            }
            if (!TextUtils.isEmpty(mood)) {
                jsonObject.put("mood", mood);
            }
            if (!TextUtils.isEmpty(nickname)) {
                jsonObject.put("nickName", nickname);
            }
            if (!TextUtils.isEmpty(birthday)) {
                jsonObject.put("birthday", birthday);
            }
            new UpdateProfileRequest(ctx, listener)
                .setMainThreadEnable(true)
                .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    public static void updateMood(Context context, String mood, ApiListener<String> listener) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("content", mood);
            new UpdateMoodRequest(context, listener)
                .setMainThreadEnable(true)
                .request(jsonObject);

        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    public void updateProfile_name(Context ctx, String nickname, @Nullable ApiListener<String> listener) {

        JSONObject jsonObject = new JSONObject();
        try {
            if (!TextUtils.isEmpty(nickname)) {
                jsonObject.put("nickName", nickname);
            }
            new UpdateProfileRequest(ctx, listener)
                .setMainThreadEnable(true)
                .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    /**
     * transfer group management rights
     */
    public static void transferOwner(Context ctx, String roomId, String ownerId, @Nullable ApiListener<String> listener) {
        try {
            JSONObject jsonObject = new JSONObject()
                .put("roomId", roomId)
                .put("data", new JSONObject()
                    .put("ownerId", ownerId)
                );
            new TransferOwnerRequest(ctx, listener)
                .setMainThreadEnable(true)
                .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    public static void doRoomHomePagePicsUpdate(Context ctx, String roomId, List<String> picUrls, ApiListener<String> listener) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("roomId", roomId);
            jsonObject.put("deleteOthers", false);
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < picUrls.size(); i++) {
                JSONObject object = new JSONObject();
                object.put("id", "");
                object.put("sequence", i + 1);
                object.put("picUrl", picUrls.get(i));
                jsonArray.put(object);
            }
            jsonObject.put("pics", jsonArray);
            new RoomHomePagePicsUpdateRequest(ctx, listener)
                .setMainThreadEnable(true)
                .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    /**
     * query a single message
     */
    public static void doMessageItem(Context ctx, String messageId, @Nullable ApiListener<MessageEntity> listener) {
        try {
            new MessageItemRequest(ctx, listener)
                .setMainThreadEnable(false)
                .request(new JSONObject().put("messageId", messageId));
        } catch (Exception e) {
            CELog.e(e.getMessage());
            if (listener != null) {
                listener.onFailed(e.getMessage());
            }
        }
    }

    /**
     * query message list
     */
    public static void doMessageListIdToId(Context ctx, boolean isAsync, String roomId, String srcMessageId, String destMessageId, @Nullable ApiListener<MessageListRequest.Resp> listener) {
        try {
            JSONObject requestJson = new JSONObject()
                .put("roomId", roomId)
                .put("srcMessageId", srcMessageId) // local data base last message id (May be the same, purpose)
                .put("destMessageId", destMessageId); // remote room entity last Message id (Relatively new, starting)
            new MessageListRequest(ctx, listener)
                .setMainThreadEnable(false)
                .setAsync(isAsync)
                .request(requestJson);
        } catch (Exception e) {
            CELog.e(e.getMessage());
            if (listener != null) {
                listener.onFailed(e.getMessage());
            }
        }
    }

    /**
     * query message list
     */
    public static void doMessageList(Context ctx, String roomId, int pageSize, String lastMessageId, String sort, boolean mainThreadEnable, boolean isAsync, @Nullable ApiListener<MessageListRequest.Resp> listener) {
        try {
            JSONObject requestJson = new JSONObject()
                .put("roomId", roomId)
                .put("pageSize", pageSize)
                .put("lastMessageId", lastMessageId)
                .put("sort", sort);
            new MessageListRequest(ctx, listener)
                .setMainThreadEnable(mainThreadEnable)
                .setAsync(isAsync)
                .request(requestJson);
        } catch (Exception e) {
            CELog.e(e.getMessage());
            if (listener != null) {
                listener.onFailed(e.getMessage());
            }
        }
    }

    /**
     * the last 7 days(由後端決定) chat room list,
     * 在 FirstLoadActivity preload() 裡面會走這個流程
     */
    public static void doRoomRecent(Context ctx, ChatRoomSource source, @Nullable ApiListener<RoomRecentRsp> listener) {
        try {
            long refreshTime = DBManager.getInstance().getLastRefreshTime(source.name());
//            CELog.d(String.format("request room recent source:: %s, lastRefreshTime:: %s", source, lastRefreshTime));
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("refreshTime", refreshTime);
            new RoomRecentRequest(ctx, listener)
                .setMainThreadEnable(false).request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
            if (listener != null) {
                listener.onFailed(e.getMessage());
            }
        }
    }

    /**
     * send reply message
     */
    public static void doMessageReply(Context ctx, String roomId, String messageId, String themeId, MsgBuilder msgBuilder, @Nullable MessageReplyRequest.Listener listener) {
        try {
            JSONObject requestJson = msgBuilder.build()
                .put("messageId", messageId)
                .put("roomId", roomId)
                .put("themeId", themeId)
                .put("srcMessageIds", new JSONArray());
            new MessageReplyRequest(ctx, listener)
                .setMainThreadEnable(true)
                .request(requestJson);
        } catch (Exception e) {
            CELog.e(e.getMessage());
            listener.onFailed(roomId, messageId, e.getMessage());
        }
    }

    /**
     * Send new message
     */
    public static void doMessageSend(Context ctx, String roomId, String messageId, String themeId, MsgBuilder msgBuilder, @Nullable MessageSendRequest.Listener<MessageEntity> listener) {
        try {
            JSONObject jsonObject = msgBuilder.build()
                .put("roomId", roomId)
                .put("themeId", themeId)
                .put("messageId", messageId);
            new MessageSendRequest(ctx, listener)
                .setMainThreadEnable(true)
                .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
            if (listener != null) {
                listener.onFailed(new MessageEntity.Builder().id(messageId).roomId(roomId).build(), e.getMessage());
            }
        }
    }


    /**
     * retract message
     */
    public static void doMessageRetract(Context ctx, String roomId, String messageId, @Nullable ApiListener<String> listener) {
        try {
            JSONObject jsonObject = new JSONObject()
                .put("roomId", roomId)
                .put("messageId", messageId);
            new MessageRetractRequest(ctx, listener)
                .setMainThreadEnable(true)
                .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
            if (listener != null) {
                listener.onFailed(e.getMessage());
            }
        }
    }

    /**
     * message read
     * Not giving messageIds means reading all the chat rooms
     */
    public static void doMessagesRead(Context ctx, String roomId, List<String> unReadMessageIds, @NonNull ApiListener<String> listener) {
        try {
            JSONObject jsonObject = new JSONObject()
                .put("roomId", roomId);
            if (unReadMessageIds != null && !unReadMessageIds.isEmpty()) {
                JSONArray ids = new JSONArray();
                for (String unReadMessage : unReadMessageIds) {
                    ids.put(unReadMessage);
                }
            }
            new MessageReadRequest(ctx, listener)
                .setMainThreadEnable(false)
                .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
            listener.onFailed(e.getMessage());
        }
    }

    /**
     * message received
     */
    public static void doMessagesReceived(Context ctx, List<String> messageIds, @Nullable ApiListener<String> listener) {
        try {
            JSONArray ids = new JSONArray();
            if (messageIds != null && !messageIds.isEmpty()) {
                for (String msgId : messageIds) {
                    ids.put(msgId);
                }
                new MessageReceivedRequest(ctx, listener)
                    .setMainThreadEnable(false)
                    .request(new JSONObject().put("messageIds", ids));
            }
        } catch (Exception e) {
            CELog.e(e.getMessage());
            if (listener != null) {
                listener.onFailed(e.getMessage());
            }
        }
    }

    /**
     * forward message
     */
    public static void doMessageForward(Context ctx, List<String> messageIds, List<String> roomIds, @Nullable ApiListener<List<MessageEntity>> listener) {
        try {
            JSONObject requestJson = new JSONObject()
                .put("messageIds", new JSONArray(messageIds))
                .put("roomIds", new JSONArray(roomIds));
            new MessageForwardRequest(ctx, listener)
                .setMainThreadEnable(true)
                .request(requestJson);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    /**
     * clear message
     */
    public static void doMessageClean(Context ctx, String roomId, @Nullable ApiListener<String> listener) {
        try {
            new MessageCleanRequest(ctx, listener)
                .setMainThreadEnable(true)
                .request(new JSONObject().put("roomId", roomId));
        } catch (Exception e) {
            CELog.e(e.getMessage());
            if (listener != null) {
                listener.onFailed(e.getMessage());
            }
        }
    }

    /**
     * delete message
     */
    public static void doMessageDelete(Context ctx, String roomId, List<String> messageIds, @Nullable ApiListener<String> listener) {
        try {
            JSONObject jsonObject = new JSONObject()
                .put("roomId", roomId)
                .put("messageIds", new JSONArray(messageIds));
            new MessageDeleteRequest(ctx, listener)
                .setMainThreadEnable(true)
                .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    /**
     * channel information
     */
    public static void doFromAppoint(Context ctx, String roomId, @Nullable ApiListener<FromAppointRequest.Resp> listener) {
        FromAppointRequest request = new FromAppointRequest(ctx, listener);
        request.setMainThreadEnable(false);
        if (roomId == null || roomId.isEmpty()) {
            if (listener != null) {
                listener.onFailed("Room Id Is Empty");
            }
        } else {
            try {
                request.request(new JSONObject().put("roomId", roomId));
            } catch (Exception e) {
                CELog.e(e.getMessage());
                if (listener != null) {
                    listener.onFailed(e.getMessage());
                }
            }
        }
    }

    /**
     * switch multi channel
     */
    public static void doFromSwitch(Context ctx, String roomId, String oldFrom, String newFrom, @Nullable ApiListener<Map<String, String>> listener) {
        try {
            JSONObject jsonObject = new JSONObject()
                .put("roomId", roomId)
                .put("from", newFrom);
            new FromSwitchRequest(ctx, oldFrom, newFrom, listener)
                .setMainThreadEnable(true)
                .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
            if (listener != null) {
                listener.onFailed(e.getMessage());
            }
        }
    }

    /**
     * stop multi channel current conversation service
     */
    public static void doAgentStopService(Context ctx, String roomId, @Nullable ApiListener<String> listener) {
        try {
            new AgentStopServiceRequest(ctx, listener)
                .setMainThreadEnable(true)
                .request(new JSONObject().put("roomId", roomId));
        } catch (Exception e) {
            CELog.e(e.getMessage());
            if (listener != null) {
                listener.onFailed(e.getMessage());
            }
        }
    }

    /**
     * Check all information of the service account
     */
    public static void doServiceNumberItem(Context ctx, String serviceNumberId, @Nullable ApiListener<ServiceNumberEntity> listener) {
        try {
            new ServiceNumberItem2Request(ctx, listener)
                .setMainThreadEnable(false)
                .request(new JSONObject().put("serviceNumberId", serviceNumberId));
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    /**
     * 更新服務號名稱
     */
    public static void updateServiceNumberName(Context context, String serviceNumberId, String name, @Nullable ApiListener<String> listener) {
        try {
            JSONObject jsonObject = new JSONObject()
                .put("id", serviceNumberId)
                .put("name", name);
            new ServiceNumberUpdateRequest(context, listener)
                .setMainThreadEnable(false)
                .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    /**
     * 更新服務號描述
     */
    public static void updateServiceNumberDescription(Context context, String serviceNumberId, String description, @Nullable ApiListener<String> listener) {
        try {
            JSONObject jsonObject = new JSONObject()
                .put("id", serviceNumberId)
                .put("description", description);
            new ServiceNumberUpdateRequest(context, listener)
                .setMainThreadEnable(false)
                .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    public static void updateServiceNumberWelcomeMessage(Context context, String serviceNumberId, String serviceWelcomeMessage,
                                                         String serviceIdleMessage, String everyContactMessage, int idleTime, @Nullable ApiListener<String> listener) {
        try {
            JSONObject jsonObject = new JSONObject()
                .put("id", serviceNumberId);
            if (serviceWelcomeMessage != null)
                jsonObject.put("serviceWelcomeMessage", serviceWelcomeMessage);
            if (serviceWelcomeMessage != null)
                jsonObject.put("serviceIdleMessage", serviceIdleMessage);
            if (serviceWelcomeMessage != null)
                jsonObject.put("everyContactMessage", everyContactMessage);
            jsonObject.put("serviceIdleTime", idleTime);
            new ServiceNumberUpdateRequest(context, listener)
                .setMainThreadEnable(false)
                .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    /**
     * dedicated to start service
     */
    public static void doServiceNumberStartService(Context ctx, String roomId, @Nullable ApiListener<Boolean> listener) {
        try {
            new ServiceNumberStartServiceRequest(ctx, listener)
                .setMainThreadEnable(true)
                .request(new JSONObject().put("roomId", roomId));
        } catch (Exception e) {
            CELog.e(e.getMessage());
            if (listener != null) {
                listener.onFailed(e.getMessage());
            }
        }
    }

    /**
     * end of service
     */
    public static void doServiceNumberStopService(Context ctx, String roomId, @Nullable ApiListener<Boolean> listener) {
        try {
            new ServiceNumberStopServiceRequest(ctx, listener)
                .setMainThreadEnable(true)
                .request(new JSONObject().put("roomId", roomId));
        } catch (Exception e) {
            CELog.e(e.getMessage());
            if (listener != null) {
                listener.onFailed(e.getMessage());
            }
        }
    }

    /**
     * Is there a dedicated service in the service number chat room?
     */
    public static void doServiceNumberChatroomAgentServiced(Context ctx, String roomId, @Nullable ApiListener<ServiceNumberChatroomAgentServicedRequest.Resp> listener) {
        try {
            new ServiceNumberChatroomAgentServicedRequest(ctx, listener)
                .setMainThreadEnable(false)
                .request(new JSONObject().put("roomId", roomId));
        } catch (Exception e) {
            CELog.e(e.getMessage());
            if (listener != null) {
                listener.onFailed(e.getMessage());
            }
        }
    }

    /**
     * Multiplayer chat room upgraded to club
     */
    public static void doRoomUpgrade(Context ctx, String roomId, String name, @Nullable RoomUpgradeRequest.Listener listener) {
        try {
            JSONObject requestJson = new JSONObject()
                .put("roomId", roomId)
                .put("name", name)
                .put("x", 0)
                .put("y", 0)
                .put("size", 128);
            new RoomUpgradeRequest(ctx, listener)
                .setMainThreadEnable(true)
                .request(requestJson);
        } catch (Exception e) {
            CELog.e(e.getMessage());
            if (listener != null) {
                listener.onFailed(e.getMessage());
            }
        }
    }

    /**
     * Get the main business information of the chat room
     */
    public static void doRoomHomePage(Context ctx, String roomId, @Nullable ApiListener<CrowdEntity> listener) {
        try {
            new RoomHomePageRequest(ctx, listener)
                .setMainThreadEnable(false)
                .request(new JSONObject().put("roomId", roomId));
        } catch (Exception e) {
            CELog.e(e.getMessage());
            if (listener != null) {
                listener.onFailed(e.getMessage());
            }
        }
    }

    /**
     * Get user information
     */
    public static void doUserItem(Context ctx, String userId, @Nullable UserItemRequest.Listener listener) {
        try {
            new UserItemRequest(ctx, listener)
                .setMainThreadEnable(true)
                .request(new JSONObject().put("userId", userId));
        } catch (Exception e) {
            CELog.e(e.getMessage());
            if (listener != null) {
                listener.onFailed(e.getMessage());
            }
        }
    }


    /**
     * Get user information
     */
    public void doUserItem2(Context ctx, String userId, @Nullable UserItemRequest.Listener listener) {
        try {
            new UserItemRequest(ctx, listener)
                .setMainThreadEnable(false)
                .request(new JSONObject().put("userId", userId));
        } catch (Exception e) {
            CELog.e(e.getMessage());
            if (listener != null) {
                listener.onFailed(e.getMessage());
            }
        }
    }

    /**
     * Get chat room information
     */
    public static void doRoomItem(Context ctx, String roomId, String userId, @Nullable ApiListener<ChatRoomEntity> listener) {
        try {
            new RoomItemRequest(ctx, userId, listener)
                .setMainThreadEnable(true)
                .request(new JSONObject().put("roomId", roomId));
        } catch (Exception e) {
            CELog.e(e.getMessage());
            if (listener != null) {
                listener.onFailed(e.getMessage());
            }
        }
    }

    /**
     * Get chat room information
     */
    public static void doRoomItemInIoThread(Context ctx, String roomId, String userId, @Nullable ApiListener<ChatRoomEntity> listener) {
        try {
            new RoomItemRequest(ctx, userId, listener)
                .setMainThreadEnable(false)
                .request(new JSONObject().put("roomId", roomId));
        } catch (Exception e) {
            CELog.e(e.getMessage());
            if (listener != null) {
                listener.onFailed(e.getMessage());
            }
        }
    }

    /**
     * Update Litian Chamber Information (Name)
     */
    public static void doRoomUpdate(Context ctx, String roomId, String name, @Nullable ApiListener<String> listener) {
        try {
            JSONObject jsonObject = new JSONObject()
                .put("roomId", roomId)
                .put("data", new JSONObject()
                    .put("name", name)
                );
            new RoomUpdateRequest(ctx, listener)
                .setMainThreadEnable(true)
                .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
            if (listener != null) {
                listener.onFailed(e.getMessage());
            }
        }
    }

    /**
     * Update Litian Room information (avatar & name)
     */
    public static void doRoomUpdate(Context ctx, String roomId, String name, String path, int size, @Nullable ApiListener<String> listener) {
        try {
            JSONObject jsonObject = new JSONObject()
                .put("roomId", roomId)
                .put("x", 0)
                .put("y", 0)
                .put("size", size)
                .put("data", new JSONObject().put("name", name));
            new RoomUpdateRequest(ctx, listener).postFile(jsonObject, path);
        } catch (Exception e) {
            CELog.e(e.getMessage());
            if (listener != null) {
                listener.onFailed(e.getMessage());
            }
        }
    }

    /**
     * Update FCM Device token (FCM Push)
     */
    public static void doUpdateFcmToken(Context ctx, String deviceType, String osType, String fcmToken, @Nullable ApiListener<String> listener) {
        try {
            JSONObject requestJson = new JSONObject()
                .put("deviceType", deviceType)
                .put("osType", osType)
                .put("appToken", fcmToken);
            new UpdateTokenRequest(ctx, listener)
                .setMainThreadEnable(false)
                .request(requestJson);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    /**
     * Sticky chat room
     */
    public void doRoomTop(Context ctx, String roomId, @Nullable ApiListener<String> listener) {
        try {
            new RoomTopRequest(ctx, listener)
                .setMainThreadEnable(true)
                .request(new JSONObject().put("roomId", roomId));
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    /**
     * Unpin chat room
     * version 1.10.0
     */
    public void doRoomTopCancel(Context ctx, String roomId, @Nullable ApiListener<String> listener) {
        try {
            JSONObject requestJson = new JSONObject()
                .put("roomId", roomId);
            new RoomTopCancelRequest(ctx, listener)
                .setMainThreadEnable(true)
                .request(requestJson);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    /**
     * Delete chat room
     */
    public void doRoomRecentDelete(Context ctx, String roomId, @Nullable ApiListener<String> listener) {
        try {
            new RoomRecentDeleteRequest(ctx, listener)
                .setMainThreadEnable(true)
                .request(new JSONObject()
                    .put("roomId", roomId));
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    /**
     * Label item
     */
    public static void doLabelItem(Context ctx, String labelId, @Nullable ApiListener<Label> listener) {
//        if(Strings.isNullOrEmpty(labelId)) {
//            CELog.e("請求 label/items, 但 labelId 沒有值，不送出請求");
//            listener.onFailed("請求 label/items, 但 labelId 沒有值，不送出請求");
//            return;
//        }
        try {
            new LabelItemRequest(ctx, listener)
                .setMainThreadEnable(false)
                .request(new JSONObject().put("labelId", labelId));
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    /**
     * Personal account information
     */
    public static void doUserProfile(Context ctx, @Nullable UserProfileRequest.Listener listener) {
        try {
            new UserProfileRequest(ctx, listener)
                .setMainThreadEnable(false)
                .request();
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    /**
     * create chat room
     */
    public static void doRoomCreate(Context ctx, ChatRoomType type, List<String> userIds, String kind, @Nullable RoomCreateRequest.Listener listener) {
        try {
            JSONObject requestJson = new JSONObject()
                .put("userIds", new JSONArray(userIds))
                .put("kind", kind)
                .put("type", type.getName());
            new RoomCreateRequest(ctx, listener)
                .setMainThreadEnable(true)
                .request(requestJson);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    /**
     * create label
     */
    public static void doCreateLabel(Context ctx, String labelName, List<String> userIds, @Nullable ApiListener<String> listener) {
        try {
            JSONArray arrayUserId = new JSONArray();
            for (String userId : userIds) {
                arrayUserId.put(userId);
            }
            JSONObject requestJson = new JSONObject()
                .put("name", labelName)
                .put("userIds", arrayUserId);
            new LabelCreateRequest(ctx, listener)
                .setMainThreadEnable(true)
                .request(requestJson);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    /**
     * add my favorite
     */
    public static void doAddFavourite(Context context, String userId, @Nullable ApiListener<Map<String, String>> listener) {
        try {
            String labelId = UserPref.getInstance(context).getLoveLabelId();
            new LabelMemberAddRequest(context, listener)
                .setMainThreadEnable(false)
                .request(new JSONObject()
                    .put("labelId", labelId)
                    .put("userId", userId));
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    /**
     * delete favorites
     */
    public static void doDeleteFavourite(Context context, String userId, @Nullable ApiListener<String> listener) {
        try {
            String labelId = UserPref.getInstance(context).getLoveLabelId();
            new LabelMemberDeleteRequest(context, listener)
                .setMainThreadEnable(false)
                .request(new JSONObject()
                    .put("labelId", labelId)
                    .put("userId", userId)
                );
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    /**
     * update label information
     */
    public static void doUpdateLabel(Context ctx, Label label, @Nullable ApiListener<String> listener) {
        try {
            JSONArray arrayUserId = new JSONArray();
            for (UserProfileEntity account : label.getUsers()) {
                arrayUserId.put(account.getId());
            }
            JSONObject requestJson = new JSONObject()
                .put("labelId", label.getId())
                .put("name", label.getName())
                .put("userIds", arrayUserId);
            new LabelUpdateRequest(ctx, listener)
                .setMainThreadEnable(true)
                .request(requestJson);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    /**
     * remove label
     */
    public static void doDeleteLabel(Context ctx, String labelId, @Nullable ApiListener<String> listener) {
        try {
            new LabelDeleteRequest(ctx, listener)
                .setMainThreadEnable(true)
                .request(new JSONObject().put("labelId", labelId));
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    /**
     * Confirm the message has received the number of reads
     */
    public static void doMessageReadingState(Context ctx, String roomId, List<String> messageIds, @Nullable ApiListener<List<MessageReadingStateRequest.Resp.Item>> listener) {
        try {
            if (messageIds == null || messageIds.isEmpty()) {
                return;
            }
            new MessageReadingStateRequest(ctx, listener)
                .setMainThreadEnable(false)
                .request(new JSONObject()
                    .put("roomId", roomId)
                    .put("messageIds", new JSONArray(messageIds)));
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    public static void doMemberAdd(Context ctx, String groupId, List<CharSequence> memberIds, @Nullable ApiListener<String> listener) {
        try {

            JSONArray jsonArray = new JSONArray();
            for (CharSequence memberId : memberIds) {
                jsonArray.put(memberId);
            }

            JSONObject requestJson = new JSONObject()
                .put("roomId", groupId)
                .put("userIds", jsonArray);

            new MemberAddRequest(ctx, listener)
                .setMainThreadEnable(true)
                .request(requestJson);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    /**
     * group list
     */
    public static void doRoomList(Context ctx, @Nullable ApiListener<List<CrowdEntity>> listener) {
        try {
            new RoomListRequest(ctx, listener)
                .setMainThreadEnable(false)
                .request();
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }


    /**
     * mute chat room
     */
    public void doRoomMute(Context ctx, String roomId, @Nullable ApiListener<String> listener) {
        try {
            new RoomMuteRequest(ctx, listener)
                .setMainThreadEnable(false)
                .request(new JSONObject().put("roomId", roomId));
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    /**
     * unmute chat room
     */
    public void doRoomMuteCancel(Context ctx, String roomId, @Nullable ApiListener<String> listener) {
        try {
            new RoomMuteCancelRequest(ctx, listener)
                .setMainThreadEnable(false)
                .request(new JSONObject().put("roomId", roomId));
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }


    /**
     * mute chat room
     */
    public void doUserMute(Context ctx, @Nullable ApiListener<String> listener) {
        try {
            new UserMuteRequest(ctx, listener)
                .setMainThreadEnable(true)
                .request();
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    /**
     * unmute chat room
     */
    public static void doUserMuteCancel(Context ctx, @Nullable ApiListener<String> listener) {
        try {
            new UserMuteCancelRequest(ctx, listener)
                .setMainThreadEnable(true)
                .request();
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }


    /**
     * to do item
     */
    public static void doTodoItem(Context ctx, String todoId, @Nullable ApiListener<TodoEntity> listener) {
        try {
            new TodoItemRequest(ctx, listener)
                .setMainThreadEnable(true)
                .request(new JSONObject().put("id", todoId));
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    /**
     * new to do
     */
    public static void doTodoCreate(Context ctx, TodoEntity entity, @Nullable TodoCreateRequest.Listener listener) {
        try {
            new TodoCreateRequest(ctx, listener)
                .setMainThreadEnable(true)
                .request(entity.toCreateJsonObj());
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    /**
     * update to do
     */
    public static void doTodoUpdate(Context ctx, TodoEntity entity, @Nullable TodoUpdateRequest.Listener listener) {
        try {
            new TodoUpdateRequest(ctx, listener)
                .setMainThreadEnable(true)
                .request(entity.toUpdateJsonObj());
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }


    /**
     * complete to do
     */
    public static void doTodoComplete(Context ctx, TodoEntity entity, @Nullable TodoCompleteRequest.Listener listener) {
        try {
            new TodoCompleteRequest(ctx, listener)
                .setMainThreadEnable(true)
                .request(entity.toCompleteJsonObj());
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    /**
     * get subscription list
     */
    public static void doTopicList(Context context, @Nullable ApiListener<List<TopicEntity>> listener) {
        try {
            new TopicListRequest(context, listener)
                .setMainThreadEnable(false)
                .request();
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    /**
     * send service number broadcast content
     */
    public static void doServiceNumberBroadcastSend(Context context, BroadcastMessageBean broadcastMessage, @Nullable ServiceNumberBroadcastSendRequest.Listener listener) {
        try {
            new ServiceNumberBroadcastSendRequest(context, listener)
                .setMainThreadEnable(false)
                .request(broadcastMessage.buildSendObj());
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    /**
     * update service account broadcast content
     */
    public static void doServiceNumberBroadcastUpdate(Context context, BroadcastMessageBean broadcastMessage, @Nullable ServiceNumberBroadcastUpdateRequest.Listener listener) {
        try {
            new ServiceNumberBroadcastUpdateRequest(context, listener)
                .setMainThreadEnable(false)
                .request(broadcastMessage.buildUpdateObj());
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    /**
     * delete broadcast chat room broadcast message
     */
    public static void doServiceNumberBroadcastDelete(Context context, String broadcastRoomId, String messageId, @Nullable ServiceNumberBroadcastDeleteRequest.Listener listener) {
        try {
            JSONObject requestJson = new JSONObject()
                .put("roomId", broadcastRoomId)
                .put("messageId", messageId);
            new ServiceNumberBroadcastDeleteRequest(context, listener)
                .setMainThreadEnable(false)
                .request(requestJson);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    /**
     * ServiceNumber Contact list
     */
    public static void doServiceNumberContactListRequest(Context ctx, String serviceNumberId, @Nullable ApiListener<List<CustomerEntity>> listener) {
        try {
            long refreshTime = DBManager.getInstance().getLastRefreshTime(BOSSSERVICENUMBER_CONTACT_LIST);
            JSONObject jsonObject = new JSONObject()
//                    .put("pageIndex", 1)
//                    .put("pageSize", ServiceNumberContactListRequest.PAGE_SIZE)
                .put("refreshTime", refreshTime);
            if (serviceNumberId != null && !serviceNumberId.isEmpty()) {
                jsonObject.put("serviceNumberId", serviceNumberId);
            }
            new ServiceNumberContactListRequest(ctx, 1, serviceNumberId, listener)
                .setMainThreadEnable(false)
                .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    /**
     * sticker pack list
     * <p>
     * Own
     * </p>
     */
    public static void doStickerPackageList(Context context, @Nullable ApiListener<List<StickerPackageEntity>> listener) {
        try {
            new StickerPackageListRequest(context, listener)
                .setMainThreadEnable(false)
                .request();
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    /**
     * list of stickers under the package
     * <p>
     * according to Sticker Package Id,
     * find the list of stickers
     * </p>
     */
    public static void doStickerList(Context context, String stickerPackageId, @Nullable ApiListener<List<StickerItemEntity>> listener) {
        try {
            new StickerListRequest(context, listener)
                .setMainThreadEnable(false)
                .request(new JSONObject().put("stickerPackageId", stickerPackageId));
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }


    public static void doTokenApply(Context context, boolean isRefresh, TokenApplyRequest.Listener listener) {
        try {
            new TokenApplyRequest(context, listener)
                .setMainThreadEnable(false)
                .request(isRefresh);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    public static void doUserBlock(Context context, String userId, boolean isBlock, @Nullable ApiListener<String> listener) {
        try {
            JSONObject requestJson = new JSONObject()
                .put("userId", userId)
                .put("block", isBlock);
            new UserBlockRequest(context, listener)
                .setMainThreadEnable(false)
                .request(requestJson);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }


    public static void doModifyUser(Context context, String userId, Map<String, String> data, @Nullable ApiListener<String> listener) {
        try {
            JSONObject requestJson = new JSONObject()
                .put("userId", userId);
            for (Map.Entry<String, String> entry : data.entrySet()) {
                requestJson.put(entry.getKey(), entry.getValue());
            }
            new CustomFriendInfoRequest2(context, listener)
                .setMainThreadEnable(false)
                .request(requestJson);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    public static void doAddressbookAdd(Context context, String userId, String alias, @Nullable ApiListener<Set<String>> listener) {
        try {
            JSONObject requestJson = new JSONObject()
                .put("alias", alias)
                .put("userIds", new JSONArray(Sets.newHashSet(userId)));
            new AddressbookAddRequest(context, listener)
                .setMainThreadEnable(false)
                .request(requestJson);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    public static void doAddressbookDelete(Context context, String userId, @Nullable ApiListener<String> listener) {
        try {
            JSONObject requestJson = new JSONObject()
                .put("userIds", new JSONArray(Sets.newHashSet(userId)));
            new AddressbookDeleteRequest(context, listener)
                .setMainThreadEnable(false)
                .request(requestJson);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    public static void doAddressbookSync(Context context, @Nullable ApiListener<List<UserProfileEntity>> listener) {
        try {
            long lastRefreshTime = DBManager.getInstance().getLastRefreshTime(ADDRESS_BOOK_SYNC);
            JSONObject requestJson = new JSONObject()
                .put("refreshTime", lastRefreshTime);
            new AddressbookSyncRequest(context, listener)
                .setMainThreadEnable(true)
                .request(requestJson);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    public static void doStickerDownload(Context context, boolean mainThreadEnable, String packageId, String stickerId, StickerDownloadRequest.Type type, @Nullable ApiListener<Drawable> listener) {
        try {
            new StickerDownloadRequest(context, listener)
                .postSticker(packageId, stickerId, type, mainThreadEnable);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    /**
     * Professional service number, if the agent is itself, the current service can be transferred to other agents
     */
    public static void doServiceNumberTransfer(Context context, String roomId, String reason, @Nullable ApiListener<String> listener) {
        try {
            new ServiceNumberTransferRequest(context, listener)
                .setMainThreadEnable(false)
                .request(new JSONObject().put("roomId", roomId).put("transferReason", reason));
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    /**
     * Professional service account, if the agent is not itself, and transferFlag == true, you can pick up the service
     */
    public static void doServiceNumberTransferComplete(Context context, String roomId, @Nullable ApiListener<String> listener) {
        try {
            new ServiceNumberTransferCompleteRequest(context, listener)
                .setMainThreadEnable(false)
                .request(new JSONObject().put("roomId", roomId));
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    /**
     * Professional service account, if the agent is itself and the transfer status is sent, TransferFlag == true, the transfer can be cancelled.
     */
    public static void doServiceNumberTransferCancel(Context context, String roomId, @Nullable ApiListener<String> listener) {
        try {
            new ServiceNumberTransferCancelRequest(context, listener)
                .setMainThreadEnable(false)
                .request(new JSONObject().put("roomId", roomId));
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    public static void doServiceNumberTransferSnatch(Context context, String roomId, @Nullable ApiListener<String> listener) {
        try {
            new ServiceNumberTransferSnatchRequest(context, listener)
                .setMainThreadEnable(false)
                .request(new JSONObject().put("roomId", roomId));
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }


    public static void doServiceNumberSearch(Context context, String keyword, @Nullable ApiListener<ServiceNumberSearchRequest.Resp> listener) {
        try {
            new ServiceNumberSearchRequest(context, listener)
                .setMainThreadEnable(true)
                .request(new JSONObject().put("keyword", keyword));
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }


    public static void doServiceNumberSubscribe(Context context, String serviceNumberId, boolean isSubscribe, @Nullable ApiListener<String> listener) {
        try {
            new ServiceNumberSubscribeRequest(context, listener)
                .setMainThreadEnable(true)
                .request(new JSONObject()
                    .put("serviceNumberId", serviceNumberId)
                    .put("subscribe", isSubscribe));
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    public static void doServiceRoomItem(Context context, String serviceNumberId, String userId, int type, String openId, String userType, @Nullable ApiListener<ChatRoomEntity> listener) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("serviceNumberId", serviceNumberId);
            jsonObject.put("type", type);
            jsonObject.put("userType", userType);
            if (!userId.isEmpty())
                jsonObject.put("userId", userId);
            if (!openId.isEmpty())
                jsonObject.put("openId", openId);
            new ServiceRoomItemRequest(context, listener)
                .setMainThreadEnable(true)
                .request(jsonObject);

        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    public static void doServiceNumberConsultList(Context context, @Nullable ApiListener<ServiceNumberConsultListRequest.Resp> listener) {
        try {
            new ServiceNumberConsultListRequest(context, listener)
                .setMainThreadEnable(false)
                .request();
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    public static void doServiceNumberConsult(Context context, String srcRoomId, String consultRoomId, @Nullable ApiListener<String> listener) {
        try {
            new ServiceNumberConsultRequest(context, listener)
                .setMainThreadEnable(false)
                .request(new JSONObject()
                    .put("srcRoomId", srcRoomId)
                    .put("consultRoomId", consultRoomId)
                );
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    public static void doServiceNumberBusinessCardUrl(Context context, @Nullable String serviceNumberId, @Nullable ApiListener<String> listener) {
        try {
            new ServiceNumberBusinessCardUrlRequest(context, listener)
                .setMainThreadEnable(false)
                .request(new JSONObject()
                    .put("serviceNumberId", serviceNumberId)
                );
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    /**
     * remove customer homepage business
     */
    public static void removeCustomerBusinessCard(Context ctx, String userId, @Nullable ApiListener<String> listener) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userId", userId);
            jsonObject.put("cardPhotoDelete", true);

            new UserHomePageUpdateProfileCustomer(ctx, listener)
                .setMainThreadEnable(false)
                .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    /**
     * Update customer homepage Profile customer
     */
    public static void doUserHomePageUpdateProfileCustomer(Context ctx, String userId, String customerName, String desc, @Nullable ApiListener<String> listener) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userId", userId);
            jsonObject.put("customerName", customerName);
            jsonObject.put("customerDescription", desc);

            new UserHomePageUpdateProfileCustomer(ctx, listener)
                .setMainThreadEnable(false)
                .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    /**
     * Upload user homepage background
     */
    public static void doUserHomePagePicsUpdate(Context ctx, List<String> picUrls, @Nullable ApiListener<String> listener) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("deleteOthers", false);
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < picUrls.size(); i++) {
                JSONObject object = new JSONObject();
                object.put("id", "");
                object.put("sequence", i + 1);
                object.put("picUrl", picUrls.get(i));
                jsonArray.put(object);
            }
            jsonObject.put("pics", jsonArray);
            new UserHomePagePicsUpdateRequest(ctx, listener)
                .setMainThreadEnable(false)
                .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    /**
     * 差異化同步API
     */
    public static void doSyncEmployee(Context ctx, @Nullable ApiListener<List<UserProfileEntity>> listener) {
        try {
            long refreshTime = DBManager.getInstance().getLastRefreshTime(SYNC_EMPLOYEE);
            JSONObject jsonObject = new JSONObject()
                .put("pageSize", SyncEmployeeRequest.PAGE_SIZE);
            jsonObject.put("refreshTime", refreshTime);
            new SyncEmployeeRequest(ctx, listener)
                .setMainThreadEnable(false)
                .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
            if (listener != null) {
                listener.onFailed(e.getMessage());
            }
        }
    }

    public static void doSyncTodo(Context ctx, @Nullable SyncTodoRequest.Listener listener) {
        try {
            long refreshTime = DBManager.getInstance().getLastRefreshTime(SYNC_TODO);
            JSONObject jsonObject = new JSONObject()
                .put("pageSize", SyncTodoRequest.PAGE_SIZE);

            jsonObject.put("refreshTime", refreshTime);
            new SyncTodoRequest(ctx, listener)
                .setMainThreadEnable(false)
                .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
            if (listener != null) {
                listener.onFailed(e.getMessage());
            }
        }
    }

    public static void doSyncServiceNumber(Context ctx, @Nullable SyncServiceNumberRequest.Listener listener) {
        try {
            long refreshTime = DBManager.getInstance().getLastRefreshTime(SYNC_SERVICENUMBER);
            JSONObject jsonObject = new JSONObject()
                .put("pageSize", SyncServiceNumberRequest.PAGE_SIZE);
            jsonObject.put("refreshTime", refreshTime);
            new SyncServiceNumberRequest(ctx, listener)
                .setMainThreadEnable(false)
                .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
            if (listener != null) {
                listener.onFailed(e.getMessage());
            }
        }
    }

    public static void doSyncContact(Context ctx, @Nullable ApiListener<List<CustomerEntity>> listener) {
        try {
            long refreshTime = DBManager.getInstance().getLastRefreshTime(SYNC_CONTACT);
            JSONObject jsonObject = new JSONObject()
                .put("serviceNumberId", TokenPref.getInstance(ctx).getBossServiceNumberId())
                .put("pageSize", SyncContactRequest.PAGE_SIZE);
            jsonObject.put("refreshTime", refreshTime);
            new SyncContactRequest(ctx, listener)
                .setMainThreadEnable(false)
                .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
            if (listener != null) {
                listener.onFailed(e.getMessage());
            }
        }
    }

    public void doSyncRobotServiceList(Context ctx, ApiCallback<List<ChatRoomEntity>> listener) {
        try {
            long refreshTime = DBManager.getInstance().getLastRefreshTime(CHAT_ROOM_ROBOT_SERVICE_LIST);
            JSONObject jsonObject = new JSONObject()
                .put("pageSize", SyncContactRequest.PAGE_SIZE)
                .put("refreshTime", refreshTime);
            new SyncRobotServiceListRequest(ctx, listener)
                .setMainThreadEnable(false)
                .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
            if (listener != null) {
                listener.error(e.getMessage());
            }
        }
    }

    public void doSyncServiceNumberActiveList(Context ctx, List<String> serviceNumberIds, long refreshTime, ApiCallback<RoomRecentRsp> listener) {
        try {
            JSONObject jsonObject = new JSONObject()
                .put("pageSize", SyncContactRequest.PAGE_SIZE)
                .put("refreshTime", refreshTime);
            if (!serviceNumberIds.isEmpty()) {
                JSONArray jsonArray = new JSONArray();
                for (String memberId : serviceNumberIds) {
                    jsonArray.put(memberId);
                }
                jsonObject.put("serviceNumberIds", jsonArray);
            }
            new SyncServiceNumberActiveListRequest(ctx, listener)
                .setMainThreadEnable(false)
                .request(jsonObject);
        } catch (Exception e) {
            CELog.e(e.getMessage());
            if (listener != null) {
                listener.error(e.getMessage());
            }
        }
    }

    public void refreshToken(Context context, Callback callback) {
        try {
            String tokenId = TokenPref.getInstance(context).getTokenId();
            if ("".equals(tokenId)) return;
            JSONObject postBody = new JSONObject();
            JSONObject header = new JSONObject();
            header.put("language", AppConfig.LANGUAGE);
            header.put("tokenId", tokenId);
            postBody.put("_header_", header);
            postBody.put("refreshTokenId", TokenPref.getInstance(context).getCeTokenRefreshId());
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(postBody.toString(), JSON);
            Request request = new Request.Builder()
                .url(TokenPref.getInstance(context).getCurrentTenantUrl() + ApiPath.ROUTE + "/" + ApiPath.tokenRefresh)
                .post(body)
                .build();

            new OkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    CELog.e("Refresh Token Request Failed: " + e.getMessage());
                    callback.onFailure(call, e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseBody = response.body().string();
                        RefreshTokenResponse refreshTokenResponse = JsonHelper.getInstance().from(responseBody, RefreshTokenResponse.class);
                        TokenPref.getInstance(context).setTokenId(refreshTokenResponse.getTokenId());
                        AppConfig.tokenForNewAPI = refreshTokenResponse.getTokenId();
                        CELog.d("Refresh Token Response: " + responseBody);
                        callback.onResponse(call, response);
                    } else {
                        CELog.e("Refresh Token Request Failed: " + response.code() + " " + response.message());
                        if (response.body() != null) {
                            CELog.e("Error Body: " + response.body().string());
                        }
                    }
                }
            });
        } catch (Exception e) {
            CELog.e("NewRequestBase refresh error", e);
        }
    }
}
