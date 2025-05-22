package tw.com.chainsea.ce.sdk.bean.room;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;

import com.google.common.base.Strings;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.collection.CollectionHelper;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.text.StringHelper;
import tw.com.chainsea.ce.sdk.R;
import tw.com.chainsea.ce.sdk.bean.Entity;
import tw.com.chainsea.ce.sdk.bean.InputLogBean;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.business.BusinessCode;
import tw.com.chainsea.ce.sdk.bean.common.EnableType;
import tw.com.chainsea.ce.sdk.bean.msg.ChannelType;
import tw.com.chainsea.ce.sdk.bean.msg.MessageFlag;
import tw.com.chainsea.ce.sdk.bean.msg.Tools;
import tw.com.chainsea.ce.sdk.database.DBContract;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.database.sp.UserPref;
import tw.com.chainsea.ce.sdk.network.model.response.ChatRoomMemberResponse;
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference;
import tw.com.chainsea.ce.sdk.reference.UserProfileReference;
import tw.com.chainsea.ce.sdk.service.type.ChatRoomSource;
import tw.com.chainsea.ce.sdk.util.ConditionUtil;

/**
 * current by evan on 2019-11-12
 */
public class ChatRoomEntity implements Serializable, Comparable<ChatRoomEntity>, Entity, Cloneable {
    private static final String TAG = ChatRoomEntity.class.getSimpleName();
    private static final long serialVersionUID = -5425641246261915528L;

    private String id; //*聊天室 Id
    private String name; //*聊天室名稱, 一般聊天室: 好友聊天室固定給好友的名字、社團給社團名字、多人聊天室給所有成員的名字組合, 服務號是訂閱者的名字
    @SerializedName("type")
    private ChatRoomType type = ChatRoomType.undef; //*Server那邊是String, 被轉型, 原聊天室類型，分為：friend | discuss | group | serviceNumber | broadcast
    private String avatarId; //*聊天室的頭像 Id
    private String ownerId; //*聊天室擁有者 Id
    private int unReadNum; //*聊天室未讀數量 , -999 代表被標註成未讀狀態
    // status
    private boolean isTop;
    private long topTime;
    private boolean isCustomName; //*多人聊天室使用, true為聊天室title被使用者修改過
    public boolean isMute; //*聊天室是否靜音
    // service data
    private String serviceNumberId = ""; //*若聊天室為服務號聊天室，則會給服務號 Id
    private String serviceNumberName; //*若聊天室為服務號聊天室，則會給服務號名稱
    private String serviceNumberAvatarId; //*若聊天室為服務號聊天室，則會給服務號的頭像 Id
    @SerializedName("serviceNumberType")
    private ServiceNumberType serviceNumberType = ServiceNumberType.NONE; //*Server那邊是String, 服務號類型
    private String serviceNumberAgentId = "";
    private String serviceNumberOwnerId; //*若聊天室為服務號聊天室，則會給服務號擁有者 Id
    @SerializedName("serviceNumberStatus")
    private ServiceNumberStatus serviceNumberStatus = ServiceNumberStatus.UNDEF; //*Server那邊是String, 若聊天室為服務號聊天室，則會給服務號聊天室的服務狀態
    // business data
    private String businessId;
    private String businessExecutorId;
    private String businessName;
    @SerializedName("businessCode")
    private BusinessCode businessCode = BusinessCode.UNDEF;
    // last message toString
    private String lastMessageStr;

    private long firstunReadMessageTime; //*(補)該聊天室第一句未讀訊息的時間
    private String firstunReadMessageId; //*(補)第一句未讀訊息 Id
    private int roomMemberIdentity; //*1: visitor 2:serviceMember,  服務號聊天室才有此屬性
    // local control
    private long updateTime;
    private String unfinishedEdited;
    private long unfinishedEditedTime;
    private ChatRoomSource listClassify = ChatRoomSource.ALL;

    private boolean blocked;
    private boolean isBlock;
    //transfer
    private boolean transferFlag; //*是否有轉接服務(有人服務而且按了換手)
    private String transferReason; //*轉接理由 transferFlag為true時提供

    private List<String> memberIds = Lists.newArrayList(); //*聊天室成員 Ids
    private List<String> provisionalIds = Lists.newArrayList(); //*(補)聊天室臨時成員 Ids, 不一定會帶
    @SerializedName("lastMessage")
    private MessageEntity lastMessage; //*最後訊息相關資料
    private long dfrTime = -1L;
    private long lastEndServiceTime; //*若聊天室為服務號聊天室，則會給服務聊天室的最後服務時間
    private String consultSrcRoomId = "";
    @SerializedName("serviceNumberOpenType")
    private Set<String> serviceNumberOpenType = Sets.newHashSet(); //*??原本是array 資若聊天室為服務號聊天室，則會給服務號的開放狀態，分為：P（全部開放）| I（對外）| O（對內）

    // ui control
    private List<UserProfileEntity> members = Lists.newArrayList();
    private Map<String, String> memberAvatarData = Maps.newHashMap();

    private MessageEntity failedMessage;
    private String content = "";

    private boolean isLeftMenuOpen, isRightMenuOpen;
    private boolean isFavourite;
    private boolean isAtMe;

    private boolean isSub;
    private boolean isSubTop;
    private boolean isSubCenter;
    private boolean isSubEnd;

    private boolean noMaster;
    private boolean isHardCode;
    private boolean animationEnable;
    public boolean deleted; //*該聊天室是否被刪除
    private String bindKey;
    private int consultSrcUnreadNumber;

    //for robot room
    private int lastSequence;
    private String chatId;
    private boolean warned;

    public boolean serviceNumberOwnerStop;
    // combination Chat Rooms
    private List<ChatRoomEntity> componentEntities = Lists.newArrayList();

    private boolean isSelected = false; //Adapter是否有被點選
    public String searchMessageCount = ""; //全局搜尋聊天室消息
    public boolean isSearchMultipleMessage = false; //全局搜尋多個消息

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isSelected() {
        return isSelected;
    }

    //商務號身份id (商務身份、助理身份)
    private String identityId = "";

    private String ownerUserType; //*本地用, 只有商務號在一般列表時會需要用到, 判斷使用者身分 後端不存在 by 20230720

    // 判斷是否被刪除
    private boolean member_deleted;

    private ChatRoomType roomType = ChatRoomType.undef; // 前端使用 判斷該聊天室是什麼樣的類型

    private List<ChatRoomMemberResponse> chatRoomMember = new ArrayList<>();

    private List<UserProfileEntity> agentsList = new ArrayList<>();

    private String aiConsultId = "";

    public ChatRoomEntity(String id, String name, ChatRoomType type, String avatarId, String ownerId, int unReadNum, boolean isTop, long topTime, boolean isCustomName, boolean isMute, String serviceNumberId, String serviceNumberName, String serviceNumberAvatarId, ServiceNumberType serviceNumberType, String serviceNumberAgentId, String serviceNumberOwnerId, ServiceNumberStatus serviceNumberStatus, String businessId, String businessExecutorId, String businessName, BusinessCode businessCode, String lastMessageStr, long firstunReadMessageTime, String firstunReadMessageId, int roomMemberIdentity, long updateTime, String unfinishedEdited, long unfinishedEditedTime, ChatRoomSource listClassify, boolean blocked, boolean isBlock, boolean transferFlag, String transferReason, List<String> memberIds, List<String> provisionalIds, MessageEntity lastMessage, long dfrTime, long lastEndServiceTime, String consultSrcRoomId, Set<String> serviceNumberOpenType, List<UserProfileEntity> members, Map<String, String> memberAvatarData, MessageEntity failedMessage, String content, boolean isLeftMenuOpen, boolean isRightMenuOpen, boolean isFavourite, boolean isAtMe, boolean isSub, boolean isSubTop, boolean isSubCenter, boolean isSubEnd, boolean noMaster, boolean isHardCode, boolean animationEnable, boolean deleted, String bindKey, int consultSrcUnreadNumber, int lastSequence, String chatId, boolean warned, boolean serviceNumberOwnerStop, List<ChatRoomEntity> componentEntities, String ownerUserType, List<ChatRoomMemberResponse> chatRoomMember) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.avatarId = avatarId;
        this.ownerId = ownerId;
        this.unReadNum = unReadNum;
        this.isTop = isTop;
        this.topTime = topTime;
        this.isCustomName = isCustomName;
        this.isMute = isMute;
        this.serviceNumberId = serviceNumberId;
        this.serviceNumberName = serviceNumberName;
        this.serviceNumberAvatarId = serviceNumberAvatarId;
        this.serviceNumberType = serviceNumberType;
        this.serviceNumberAgentId = serviceNumberAgentId;
        this.serviceNumberOwnerId = serviceNumberOwnerId;
        this.serviceNumberStatus = serviceNumberStatus;
        this.businessId = businessId;
        this.businessExecutorId = businessExecutorId;
        this.businessName = businessName;
        this.businessCode = businessCode;
        this.lastMessageStr = lastMessageStr;
        this.firstunReadMessageTime = firstunReadMessageTime;
        this.firstunReadMessageId = firstunReadMessageId;
        this.roomMemberIdentity = roomMemberIdentity;
        this.updateTime = updateTime;
        this.unfinishedEdited = unfinishedEdited;
        this.unfinishedEditedTime = unfinishedEditedTime;
        this.listClassify = listClassify;
        this.blocked = blocked;
        this.isBlock = isBlock;
        this.transferFlag = transferFlag;
        this.transferReason = transferReason;
        this.memberIds = memberIds;
        this.provisionalIds = provisionalIds;
        this.lastMessage = lastMessage;
        this.dfrTime = dfrTime;
        this.lastEndServiceTime = lastEndServiceTime;
        this.consultSrcRoomId = consultSrcRoomId;
        this.serviceNumberOpenType = serviceNumberOpenType;
        this.members = members;
        this.memberAvatarData = memberAvatarData;
        this.failedMessage = failedMessage;
        this.content = content;
        this.isLeftMenuOpen = isLeftMenuOpen;
        this.isRightMenuOpen = isRightMenuOpen;
        this.isFavourite = isFavourite;
        this.isAtMe = isAtMe;
        this.isSub = isSub;
        this.isSubTop = isSubTop;
        this.isSubCenter = isSubCenter;
        this.isSubEnd = isSubEnd;
        this.noMaster = noMaster;
        this.isHardCode = isHardCode;
        this.animationEnable = animationEnable;
        this.deleted = deleted;
        this.bindKey = bindKey;
        this.consultSrcUnreadNumber = consultSrcUnreadNumber;
        this.lastSequence = lastSequence;
        this.chatId = chatId;
        this.warned = warned;
        this.serviceNumberOwnerStop = serviceNumberOwnerStop;
        this.componentEntities = componentEntities;
        this.ownerUserType = ownerUserType;
        this.chatRoomMember = chatRoomMember;
    }

    public ChatRoomEntity() {
    }

    private static ChatRoomType $default$type() {
        return ChatRoomType.undef;
    }

    private static String $default$serviceNumberId() {
        return "";
    }

    private static ServiceNumberType $default$serviceNumberType() {
        return ServiceNumberType.NONE;
    }

    private static String $default$serviceNumberAgentId() {
        return "";
    }

    private static ServiceNumberStatus $default$serviceNumberStatus() {
        return ServiceNumberStatus.OFF_LINE;
    }

    private static BusinessCode $default$businessCode() {
        return BusinessCode.UNDEF;
    }

    private static ChatRoomSource $default$listClassify() {
        return ChatRoomSource.ALL;
    }

    private static List<String> $default$memberIds() {
        return Lists.newArrayList();
    }

    private static long $default$dfrTime() {
        return -1L;
    }

    private static String $default$consultSrcRoomId() {
        return "";
    }

    private static Set<String> $default$serviceNumberOpenType() {
        return Sets.newHashSet();
    }

    private static List<UserProfileEntity> $default$members() {
        return Lists.newArrayList();
    }

    private static Map<String, String> $default$memberAvatarData() {
        return Maps.newHashMap();
    }

    private static String $default$content() {
        return "";
    }

    private static List<ChatRoomEntity> $default$componentEntities() {
        return Lists.newArrayList();
    }

    public static ChatRoomEntityBuilder Build() {
        return new ChatRoomEntityBuilder();
    }

    public void setUpdateTime(long updateTime) {
        if (updateTime > this.updateTime)
            this.updateTime = updateTime;
    }

    public void setInitUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public int getLastSequence() {
        return lastSequence;
    }

    public void setLastSequence(int lastSequence) {
        this.lastSequence = lastSequence;
    }

    public long getUpdateTime() {
        return this.updateTime;
    }

    // feature
    public boolean getFeatureTop() {
        boolean isTop = this.isTop;
        for (ChatRoomEntity sub : this.componentEntities) {
            if (!isTop) {
                isTop = sub.isTop;
            }
        }
        return isTop;
    }

    public String getOwnerUserType() {
        return ownerUserType;
    }

    public void setOwnerUserType(String ownerUserType) {
        this.ownerUserType = ownerUserType;
    }

    public String serviceUniqueId() {
        return serviceNumberId + "_" + ownerId;
    }

    public String getServiceNumberId() {
        return serviceNumberId;
    }

    public boolean getFeatureIsAtMe() {
        boolean isAt = this.isAtMe;
        for (ChatRoomEntity sub : this.componentEntities) {
            if (!isAt) {
                isAt = sub.isAtMe;
            }
        }
        return isAtMe;
    }

    public boolean getFeatureFavourite() {
        boolean isFavourite = this.isFavourite;
        for (ChatRoomEntity sub : this.componentEntities) {
            if (!isFavourite) {
                isFavourite = sub.isFavourite;
            }
        }
        return isFavourite;
    }

    public int getFeatureUnReadNumCount() {
        int unReadNum = this.unReadNum;
        for (ChatRoomEntity sub : this.componentEntities) {
            unReadNum += sub.unReadNum;
        }
        return unReadNum;
    }

    public Set<UserProfileEntity> getFeatureMembers() {
        Set<UserProfileEntity> members = Sets.newHashSet(this.members);
        for (ChatRoomEntity sub : this.componentEntities) {
            members.addAll(sub.members);
        }
        return members;
    }

    public void componentAdd(ChatRoomEntity entity) {
        if (this.componentEntities.contains(entity)) {
            this.componentEntities.remove(entity);
        }
        this.componentEntities.add(entity);
    }

    public ArrayList<String> getComponentIds() {
        ArrayList<String> ids = Lists.newArrayList();
        ids.add(getId());
        for (ChatRoomEntity componentEntity : getComponentEntities()) {
            ids.add(componentEntity.getId());
        }
        return ids;
    }

    public String getMergeMemberId() {
        StringBuilder mergeMemberId = new StringBuilder();
        Collections.sort(this.memberIds);
        for (String id : this.memberIds) {
            mergeMemberId.append(id);
        }
        return mergeMemberId.toString();
    }

    /**
     * main Chat Room Table Return Logic
     */
    public boolean isMainListComponent(ChatRoomEntity entity) {
        if (entity != null) {
            if (ChatRoomType.friend.equals(getType()) && ChatRoomType.friend.equals(entity.getType())) {
                // EVAN_FLAG 2020-04-06 (1.10.0) If it is a friend chat room, BusinessId is not empty and the number of members is the same as the ID.
                return (!Strings.isNullOrEmpty(entity.getBusinessId()) || !Strings.isNullOrEmpty(getBusinessId())) &&
                    CollectionHelper.isEqualCollection(getMemberIds(), entity.getMemberIds());
            } else if (ChatRoomType.subscribe.equals(getType()) && ChatRoomType.subscribe.equals(entity.getType())) {
                // EVAN_FLAG 2020-04-06 (1.10.0) If it is a subscription number chat room, BusinessId is not empty and ServiceNumberId is the same.
                return (!Strings.isNullOrEmpty(entity.getBusinessId()) || !Strings.isNullOrEmpty(getBusinessId())) &&
                    !Strings.isNullOrEmpty(getServiceNumberId()) &&
                    !Strings.isNullOrEmpty(entity.getServiceNumberId()) &&
                    getServiceNumberId().equals(entity.getServiceNumberId());
            }
        }
        return false;
    }

    /**
     * service Account Chat Room Return Logic
     */
    public boolean isServiceListComponent(ChatRoomEntity entity) {
        if (entity != null && ChatRoomType.services.equals(getType()) && ChatRoomType.services.equals(entity.getType())) {
            // EVAN_FLAG 2020-04-06 (1.10.0) If it is a service account chat room, BusinessId is not empty,
            //  and the service account ID is the same, the owner ID is the same,
            //  and the unread number is 0.
            return (!Strings.isNullOrEmpty(entity.getBusinessId()) || !Strings.isNullOrEmpty(getBusinessId()))
                && (!Strings.isNullOrEmpty(getServiceNumberId()) && !Strings.isNullOrEmpty(entity.getServiceNumberId()))
                && getServiceNumberId().equals(entity.getServiceNumberId())
                && getOwnerId().equals(entity.getOwnerId());
        }
        return false;
    }

    public static ChatRoomEntity.ChatRoomEntityBuilder formatByCursor(Map<String, Integer> index, Cursor cursor, boolean isJoin) {
        Type listType = new TypeToken<ArrayList<ChatRoomMemberResponse>>() {
        }.getType();
        // status
        // service data
        // business data
        // last message toString
        // local control
        // reserved unused
        //                .sortWeights(cursor.getInt(index.get(DBContract.ChatRoomEntry.COLUMN_SORT_WEIGHTS)))
        ArrayList<ChatRoomMemberResponse> chatRoomMemberList = JsonHelper.getInstance().from(cursor.getString(index.get(DBContract.ChatRoomEntry.COLUMN_CHAT_ROOM_MEMBER)), listType);
        if (chatRoomMemberList == null) {
            chatRoomMemberList = new ArrayList<>();
        }

        return ChatRoomEntity.Build()
            .id(cursor.getString(index.get(DBContract.ChatRoomEntry._ID)))
            .name(cursor.getString(index.get(DBContract.ChatRoomEntry.COLUMN_TITLE)))
            .type(ChatRoomType.of(cursor.getString(index.get(DBContract.ChatRoomEntry.COLUMN_TYPE))))
            .avatarId(cursor.getString(index.get(DBContract.ChatRoomEntry.COLUMN_AVATAR_ID)))
            .ownerId(cursor.getString(index.get(DBContract.ChatRoomEntry.COLUMN_OWNER_ID)))
            .unReadNum(cursor.getInt(index.get(DBContract.ChatRoomEntry.COLUMN_UNREAD_NUMBER)))
            // status
            .isTop(EnableType.valueOf(cursor.getString(index.get(DBContract.ChatRoomEntry.COLUMN_IS_TOP))).isStatus())
            .topTime(cursor.getLong(index.get(DBContract.ChatRoomEntry.COLUMN_TOP_TIME)))
            .isCustomName(EnableType.valueOf(cursor.getString(index.get(DBContract.ChatRoomEntry.COLUMN_IS_CUSTOM_NAME))).isStatus())
            .isMute(EnableType.valueOf(cursor.getString(index.get(DBContract.ChatRoomEntry.COLUMN_IS_MUTE))).isStatus())
            // service data
            .serviceNumberId(cursor.getString(index.get(DBContract.ChatRoomEntry.COLUMN_SERVICE_NUMBER_ID)))
            .serviceNumberName(cursor.getString(index.get(DBContract.ChatRoomEntry.COLUMN_SERVICE_NUMBER_NAME)))
            .serviceNumberAvatarId(cursor.getString(index.get(DBContract.ChatRoomEntry.COLUMN_SERVICE_NUMBER_AVATAR_ID)))
            .serviceNumberType(ServiceNumberType.of(cursor.getString(index.get(DBContract.ChatRoomEntry.COLUMN_SERVICE_NUMBER_TYPE))))
            .serviceNumberAgentId(cursor.getString(index.get(DBContract.ChatRoomEntry.COLUMN_SERVICE_NUMBER_AGENT_ID)))
            .serviceNumberOwnerId(cursor.getString(index.get(DBContract.ChatRoomEntry.COLUMN_SERVICE_NUMBER_OWNER_ID)))
            .serviceNumberStatus(ServiceNumberStatus.of(cursor.getString(index.get(DBContract.ChatRoomEntry.COLUMN_SERVICE_NUMBER_STATUS))))
            // business data
            .businessId(cursor.getString(index.get(DBContract.ChatRoomEntry.COLUMN_BUSINESS_ID)))
            .businessExecutorId(cursor.getString(index.get(DBContract.ChatRoomEntry.COLUMN_BUSINESS_EXECUTOR_ID)))
            .businessName(cursor.getString(index.get(DBContract.ChatRoomEntry.COLUMN_BUSINESS_NAME)))
            .businessCode(BusinessCode.of(cursor.getString(index.get(DBContract.ChatRoomEntry.COLUMN_BUSINESS_CODE))))
            // last message toString
            .lastMessageStr(cursor.getString(index.get(DBContract.ChatRoomEntry.COLUMN_LAST_MESSAGE_ENTITY)))
            .chatRoomMember(chatRoomMemberList)
            // local control
            .updateTime(cursor.getLong(index.get(DBContract.ChatRoomEntry.COLUMN_UPDATE_TIME)))
            .unfinishedEdited(cursor.getString(index.get(DBContract.ChatRoomEntry.COLUMN_UNFINISHED_EDITED)))
            .unfinishedEditedTime(cursor.getLong(index.get(DBContract.ChatRoomEntry.COLUMN_UNFINISHED_EDITED_TIME)))
            .listClassify(ChatRoomSource.of(cursor.getString(index.get(DBContract.ChatRoomEntry.COLUMN_LIST_CLASSIFY))))
            .consultSrcRoomId(cursor.getString(index.get(DBContract.ChatRoomEntry.COLUMN_CONSULT_ROOM_ID)))
            .serviceNumberOpenType(JsonHelper.getInstance().fromToSet(cursor.getString(index.get(DBContract.ChatRoomEntry.COLUMN_SERVICE_NUMBER_OPEN_TYPES)), String[].class))
            .warned(EnableType.valueOf(cursor.getString(index.get(DBContract.ChatRoomEntry.COLUMN_AI_SERVICE_WARNED))).isStatus())
            .deleted(EnableType.valueOf(cursor.getString(index.get(DBContract.ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED))).isStatus())
            .provisionalIds(JsonHelper.getInstance().fromToList(cursor.getString(index.get(DBContract.ChatRoomEntry.COLUMN_PROVISIONAL_IDS)), String[].class))
            .memberIds(JsonHelper.getInstance().fromToList(cursor.getString(index.get(DBContract.ChatRoomEntry.COLUMN_MEMBER_IDS)), String[].class))
            .lastEndServiceTime(cursor.getLong(index.get(DBContract.ChatRoomEntry.COLUMN_LAST_END_SERVICE_TIME)))
            .transferFlag((Tools.getDbInt(cursor, DBContract.ChatRoomEntry.COLUMN_TRANSFER_FLAG) == 1))
            .serviceNumberOwnerStop((Tools.getDbInt(cursor, DBContract.ChatRoomEntry.COLUMN_SERVICE_NUMBER_OWNER_STOP) == 1))
            .transferReason(cursor.getString(index.get(DBContract.ChatRoomEntry.COLUMN_TRANSFER_REASON)))
            .ownerUserType(cursor.getString(index.get(DBContract.ChatRoomEntry.COLUMN_OWNER_USER_TYPE)))
            .dfrTime(cursor.getLong(index.get(DBContract.ChatRoomEntry.COLUMN_DFR_TIME)))
            .isAtMe((cursor.getInt(index.get(DBContract.ChatRoomEntry.COLUMN_IS_AT_ME)) == 1))
            // reserved unused
//                .sortWeights(cursor.getInt(index.get(DBContract.ChatRoomEntry.COLUMN_SORT_WEIGHTS)))
            .lastSequence(cursor.getInt(index.get(DBContract.ChatRoomEntry.COLUMN_LAST_SEQUENCE)))
            .blocked(false)
            .isBlock(false)
            .content("");
    }

    public static ChatRoomEntity.ChatRoomEntityBuilder formatByCursor(Map<String, Integer> index, Cursor cursor, boolean isJoin, MessageEntity lastMessage) {
        ChatRoomEntity.ChatRoomEntityBuilder build = formatByCursor(index, cursor, isJoin);
        build.lastMessage(lastMessage);
        return build;
    }

    public static ChatRoomEntity.ChatRoomEntityBuilder formatByCursor(Map<String, Integer> index, Cursor cursor, boolean isJoin, MessageEntity lastMessage, MessageEntity failedMessage) {
        ChatRoomEntity.ChatRoomEntityBuilder build = formatByCursor(index, cursor, isJoin, lastMessage);
        build.failedMessage(failedMessage);
        return build;
    }

    public static ChatRoomEntity.ChatRoomEntityBuilder formatByCursor(Map<String, Integer> index, Cursor cursor, boolean isJoin, List<String> memberIds, MessageEntity lastMessage, MessageEntity failedMessage) {
        ChatRoomEntity.ChatRoomEntityBuilder build = formatByCursor(index, cursor, isJoin, lastMessage, failedMessage);
        if (!memberIds.isEmpty()) {
            build.memberIds(memberIds);
        }
        return build;
    }

    public static ChatRoomEntity.ChatRoomEntityBuilder formatByCursor(Map<String, Integer> index, Cursor cursor, boolean isJoin, List<String> memberIds, List<UserProfileEntity> members, MessageEntity lastMessage, MessageEntity failedMessage) {
        ChatRoomEntity.ChatRoomEntityBuilder build = formatByCursor(index, cursor, isJoin, memberIds, lastMessage, failedMessage);
        if (!members.isEmpty()) {
            build.members(members);
        }
        return build;
    }

    public ComponentRoomType getComponentType() {
        return !getComponentEntities().isEmpty() ? ComponentRoomType.MULTI : ComponentRoomType.SINGLE;
    }

    public static ContentValues getContentValues(ChatRoomEntity entity) {

        ContentValues values = new ContentValues();
        values.put(DBContract.ChatRoomEntry._ID, entity.id);
        values.put(DBContract.ChatRoomEntry.COLUMN_TITLE, entity.name);
        values.put(DBContract.ChatRoomEntry.COLUMN_TYPE, entity.type != null ? entity.getType().name() : ChatRoomType.undef.name());
        values.put(DBContract.ChatRoomEntry.COLUMN_AVATAR_ID, entity.avatarId);
        values.put(DBContract.ChatRoomEntry.COLUMN_OWNER_ID, entity.ownerId);
        values.put(DBContract.ChatRoomEntry.COLUMN_UNREAD_NUMBER, entity.unReadNum);
        // status
        values.put(DBContract.ChatRoomEntry.COLUMN_IS_TOP, EnableType.of(entity.isTop).name());
        values.put(DBContract.ChatRoomEntry.COLUMN_TOP_TIME, entity.isTop ? Math.max(entity.topTime, 0L) : 0L);
        values.put(DBContract.ChatRoomEntry.COLUMN_IS_CUSTOM_NAME, EnableType.of(entity.isCustomName).name());
        values.put(DBContract.ChatRoomEntry.COLUMN_IS_MUTE, EnableType.of(entity.isMute).name());
        // service data
        values.put(DBContract.ChatRoomEntry.COLUMN_SERVICE_NUMBER_ID, entity.getServiceNumberId());
        values.put(DBContract.ChatRoomEntry.COLUMN_SERVICE_NUMBER_NAME, entity.getServiceNumberName());
        values.put(DBContract.ChatRoomEntry.COLUMN_SERVICE_NUMBER_AVATAR_ID, entity.getServiceNumberAvatarId());
        if (entity.getServiceNumberType() != null) {
            values.put(DBContract.ChatRoomEntry.COLUMN_SERVICE_NUMBER_TYPE, entity.getServiceNumberType().name());
        }
        values.put(DBContract.ChatRoomEntry.COLUMN_SERVICE_NUMBER_AGENT_ID, !Strings.isNullOrEmpty(entity.getServiceNumberAgentId()) ? entity.getServiceNumberAgentId() : "");
        values.put(DBContract.ChatRoomEntry.COLUMN_SERVICE_NUMBER_OWNER_ID, entity.getServiceNumberOwnerId());
        values.put(DBContract.ChatRoomEntry.COLUMN_SERVICE_NUMBER_STATUS, entity.getServiceNumberStatus().getStatus());
        // business data
        values.put(DBContract.ChatRoomEntry.COLUMN_BUSINESS_ID, Strings.isNullOrEmpty(entity.getBusinessId()) ? "" : entity.getBusinessId());
        values.put(DBContract.ChatRoomEntry.COLUMN_BUSINESS_EXECUTOR_ID, Strings.isNullOrEmpty(entity.getBusinessExecutorId()) ? "" : entity.getBusinessExecutorId());
        values.put(DBContract.ChatRoomEntry.COLUMN_BUSINESS_NAME, Strings.isNullOrEmpty(entity.getBusinessName()) ? "" : entity.getBusinessName());
        values.put(DBContract.ChatRoomEntry.COLUMN_BUSINESS_CODE, entity.getBusinessCode() == null ? "" : entity.getBusinessCode().getCode());

        // last message toString
        if (entity.getLastMessage() != null) {
            String lastMessageStr = entity.getLastMessage().toJson();
            values.put(DBContract.ChatRoomEntry.COLUMN_LAST_MESSAGE_ENTITY, lastMessageStr);
        }

        values.put(DBContract.ChatRoomEntry.COLUMN_LAST_SEQUENCE, entity.getLastSequence());

        // local control
        values.put(DBContract.ChatRoomEntry.COLUMN_UPDATE_TIME, entity.getUpdateTime());
        values.put(DBContract.ChatRoomEntry.COLUMN_UNFINISHED_EDITED, Strings.isNullOrEmpty(entity.getUnfinishedEdited()) ? "" : entity.getUnfinishedEdited());
        values.put(DBContract.ChatRoomEntry.COLUMN_UNFINISHED_EDITED_TIME, Strings.isNullOrEmpty(entity.getUnfinishedEdited()) ? 0L : entity.getUnfinishedEditedTime());
        values.put(DBContract.ChatRoomEntry.COLUMN_LIST_CLASSIFY, entity.getListClassify().name());
        // reserved unused
        values.put(DBContract.ChatRoomEntry.COLUMN_SORT_WEIGHTS, entity.getWeights());
        values.put(DBContract.ChatRoomEntry.COLUMN_CONSULT_ROOM_ID, entity.getConsultSrcRoomId());
        values.put(DBContract.ChatRoomEntry.COLUMN_SERVICE_NUMBER_OPEN_TYPES, JsonHelper.getInstance().toJson(entity.getServiceNumberOpenType()));
        values.put(DBContract.ChatRoomEntry.COLUMN_AI_SERVICE_WARNED, EnableType.of(entity.warned).name());
        values.put(DBContract.ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED, EnableType.of(entity.deleted).name());
        values.put(DBContract.ChatRoomEntry.COLUMN_PROVISIONAL_IDS, JsonHelper.getInstance().toJson(entity.getProvisionalIds()));

        values.put(DBContract.ChatRoomEntry.COLUMN_OWNER_USER_TYPE, entity.getOwnerUserType());
        values.put(DBContract.ChatRoomEntry.COLUMN_MEMBER_IDS, JsonHelper.getInstance().toJson(entity.getMemberIds()));
        values.put(DBContract.ChatRoomEntry.COLUMN_TRANSFER_FLAG, (entity.isTransferFlag()) ? "1" : "0");
        values.put(DBContract.ChatRoomEntry.COLUMN_SERVICE_NUMBER_OWNER_STOP, (entity.isServiceNumberOwnerStop()) ? "1" : "0");
        values.put(DBContract.ChatRoomEntry.COLUMN_TRANSFER_REASON, entity.getTransferReason());
        values.put(DBContract.ChatRoomEntry.COLUMN_LAST_END_SERVICE_TIME, entity.getLastEndServiceTime());
        values.put(DBContract.ChatRoomEntry.COLUMN_DFR_TIME, entity.getDfrTime());
        List<ChatRoomMemberResponse> chatRoomMemberResponses = ChatRoomReference.getInstance().getChatMember(entity.getId());
        values.put(DBContract.ChatRoomEntry.COLUMN_CHAT_ROOM_MEMBER, JsonHelper.getInstance().toJson(chatRoomMemberResponses));
        boolean isAtMe = ChatRoomReference.getInstance().getIsAtMe(entity.getId());
        values.put(DBContract.ChatRoomEntry.COLUMN_IS_AT_ME, isAtMe ? 1 : 0);
//        if (entity.getChatRoomMember() != null && !entity.getChatRoomMember().isEmpty()) {

//        }
        return values;
    }

    public static ContentValues getLastMessageContentValues(MessageEntity lastMessage) {
        ContentValues values = new ContentValues();
        values.put(DBContract.LastMessageEntry.COLUMN_ROOM_ID, lastMessage.getRoomId());
        values.put(DBContract.LastMessageEntry.COLUMN_DEVICE_TYPE, lastMessage.getDeviceType());
        values.put(DBContract.LastMessageEntry.COLUMN_FLAG, (lastMessage.getFlag() == null) ? MessageFlag.READ.getFlag() : lastMessage.getFlag().getFlag());
        values.put(DBContract.LastMessageEntry.COLUMN_RECEIVE_NUM, lastMessage.getReceivedNum());
        values.put(DBContract.LastMessageEntry.COLUMN_CHAT_ID, lastMessage.getChatId());
        values.put(DBContract.LastMessageEntry.COLUMN_MSG_SRC, lastMessage.getMsgSrc());
        values.put(DBContract.LastMessageEntry.COLUMN_TYPE, Objects.requireNonNull(lastMessage.getType()).getType());
        values.put(DBContract.LastMessageEntry.COLUMN_CONTENT, lastMessage.getContent());
        values.put(DBContract.LastMessageEntry.COLUMN_SEND_TIME, lastMessage.getSendTime());
        values.put(DBContract.LastMessageEntry.COLUMN_SEQUENCE, lastMessage.getSequence());
        values.put(DBContract.LastMessageEntry.COLUMN_SENDER_ID, lastMessage.getSenderId());
        values.put(DBContract.LastMessageEntry.COLUMN_SENDER_NAME, lastMessage.getSenderName());
        values.put(DBContract.LastMessageEntry.COLUMN_SOURCE_TYPE, Objects.requireNonNull(lastMessage.getSourceType()).getSourceType());
        values.put(DBContract.LastMessageEntry.COLUMN_OS_TYPE, lastMessage.getOsType());
        values.put(DBContract.LastMessageEntry.COLUMN_FROM, (lastMessage.getFrom() == null) ? ChannelType.UNDEF.name() : lastMessage.getFrom().name());
        values.put(DBContract.LastMessageEntry.COLUMN_ID, lastMessage.getId());
        values.put(DBContract.LastMessageEntry.COLUMN_TAG, lastMessage.getTag());
        values.put(DBContract.LastMessageEntry.COLUMN_SEND_NUM, lastMessage.getSendNum());
        values.put(DBContract.LastMessageEntry.COLUMN_READED_NUM, lastMessage.getReadedNum());
        return values;
    }

    public static ContentValues getLastMessageContentValues(String roomId) {
        ContentValues values = new ContentValues();
        values.put(DBContract.LastMessageEntry.COLUMN_ROOM_ID, roomId);
        return values;
    }

    public static ContentValues getMemberId(String roomId, String memberId) {
        ContentValues values = new ContentValues();
        values.put(DBContract.ChatRoomMemberIdsEntry.COLUMN_ROOM_ID, roomId);
        values.put(DBContract.ChatRoomMemberIdsEntry.COLUMN_MEMBER_ID, memberId);
        return values;
    }

    public Map<String, String> getMembersTable() {
        if (getMembers() == null || getMembers().isEmpty()) {
            return Maps.newHashMap();
        }
        Map<String, String> data = Maps.newHashMap();
        for (UserProfileEntity a : getMembers()) {
            data.put(a.getId(), !Strings.isNullOrEmpty(a.getAlias()) ? a.getAlias() : a.getNickName());
        }
        return data;
    }


    public String getMembersName() {
        StringBuilder builder = new StringBuilder();

        for (UserProfileEntity profile : getMembers()) {
            String profileName = !Strings.isNullOrEmpty(profile.getAlias()) ? profile.getAlias() : profile.getNickName();
            builder.append(profileName).append(", ");
        }
        if (builder.length() - 2 > 0) {
            builder.replace(builder.length() - 2, builder.length(), "");
        }
        return builder.toString();
    }


    public LinkedList<UserProfileEntity> getMembersLinkedList() {
        if (getMembers() == null || getMembers().isEmpty()) {
            return Lists.newLinkedList();
        }
        return Lists.newLinkedList(getMembers());
    }


    public boolean isService(Context context) {
        return isService(TokenPref.getInstance(context).getUserId());
    }

    public boolean isService(String userId) {
        if (ChatRoomType.services.equals(getType()) && !userId.equals(getOwnerId())) {
            return !ServiceNumberType.BOSS.equals(getServiceNumberType()) || !userId.equals(getServiceNumberOwnerId());
        }
        return false;
    }

    public boolean isBoos(String userId) {
        return ChatRoomType.services.equals(getType()) && ServiceNumberType.BOSS.equals(getServiceNumberType()) && userId.equals(getServiceNumberOwnerId());
    }

    /**
     * restore Local Data And Merge Remote Data
     */
    public void setLocalControl(ChatRoomEntity localEntity) {
        setUnfinishedEdited(localEntity.getUnfinishedEdited());
        setUnfinishedEditedTime(localEntity.getUnfinishedEditedTime());
        setFailedMessage(localEntity.getFailedMessage());
        setUpdateTime(localEntity.getUpdateTime());
        setMembers(localEntity.getMembers());
        setFavourite(localEntity.isFavourite());
        setAtMe(localEntity.isAtMe());
        setLastMessageStr(localEntity.getLastMessageStr());
        setConsultSrcUnreadNumber(localEntity.getConsultSrcUnreadNumber());

        if (this.getLastMessage() != null) {
            if (this.getLastMessage().getSendTime() > this.getUpdateTime()) {
                setUpdateTime(this.getLastMessage().getSendTime());
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ChatRoomEntity other = (ChatRoomEntity) obj;
        if (this.id == null || other.getId() == null) {
            return false;
        } else if (this.isSelected != ((ChatRoomEntity) obj).isSelected) {
            return false;
        } else return this.id.equals(other.getId());
    }


    @Override
    @NonNull
    public String toString() {
        return "{" + this.id + "},{" + this.listClassify + "},{" + this.type + "}";
    }

    @Override
    @NonNull
    public ChatRoomEntity clone() {
        try {
            ChatRoomEntity clone = (ChatRoomEntity) super.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public int compareTo(ChatRoomEntity o) {
        return ComparisonChain.start()
            .compare(o.getWeights(), this.getWeights())
            .compare(o.getUpdateTime(), this.getUpdateTime())
            .result();
    }

    /**
     * get Title
     */
    public String getTitle(Context context) {
        StringBuilder builder = new StringBuilder();
        if (ChatRoomType.person.equals(this.type) && this.getMembers() != null) {
            String selfName = UserPref.getInstance(context).getUserName();
            return builder.append(selfName).toString();
        } else if (ChatRoomType.discuss.equals(this.type) && this.getMembers() != null && !isCustomName) {
            String selfId = TokenPref.getInstance(context).getUserId();
            String title = UserProfileReference.getDiscussTitle(null, this.id, selfId, 4, this.chatRoomMember.size());
            return String.valueOf(StringHelper.getString(title, this.name));
        } else if (ChatRoomType.broadcast.equals(this.type)) {
            return "群發訊息";
        } else if (ChatRoomType.serviceMember.equals(this.type)) {
            return this.serviceNumberName;
        } else
            return !getMembers().isEmpty() && ChatRoomType.GROUP_or_DISCUSS.contains(this.type) ? context.getString(R.string.text_chat_room_title_format, this.name, getMembers().size()) : this.name;
    }

    /**
     * get Services Number Title
     */
    public String getServicesNumberTitle(String selfId) {
        if (ChatRoomType.serviceMember.equals(type)) {
            if (ServiceNumberType.BOSS.equals(this.serviceNumberType)) { //商務號成員聊天室不論顯示在一般列表(自己是商務號擁有者時)或者服務號列表(自己是商務號成員時)，名稱顯示格式都是『{商務號擁有者名稱}的助理成員』

                if (name != null) { //
                    return name + "的助理成員";
                } else {
                    return "未知的助理成員";
                }
            } else { //其他的服務號成員聊天室，只會顯示在服務號列表，顯示名稱格式為『{服務號名稱}的服務成員』
                return serviceNumberName + "的服務成員";
            }
        }
        if (ServiceNumberType.BOSS.equals(this.serviceNumberType) && selfId.equals(this.serviceNumberOwnerId)) {
            return this.name;
        }
        if (this.name == null || this.name.isEmpty()) {
            UserProfileEntity userProfileEntity = DBManager.getInstance().queryFriend(this.ownerId);
            if (userProfileEntity != null) {
                this.name = !Strings.isNullOrEmpty(userProfileEntity.getName()) ? userProfileEntity.getName() : "未知";
            } else {
                this.name = "未知";
            }
        }
        return this.isSub ? this.name : this.name + "@" + this.serviceNumberName;
    }


    /**
     * get Subtitle
     */
    public String getSubTitle(Context context) {
        if (ChatRoomType.broadcast.equals(this.type)) {
            return this.serviceNumberName;
        } else if (!Strings.isNullOrEmpty(this.businessId)) {
            return this.businessName;
        }
        return "";
    }

    /**
     * Assembled avatar URLs
     * The maximum number of assembled avatars is four avatars
     * If there are more than five people, put your own avatar at the end
     */
    public List<String> getAvatarIds(Context context, int max) {
        String userId = TokenPref.getInstance(context).getUserId();
        List<String> avatarIds = Lists.newArrayList();
        String selfAvatarId = "";

        if (ChatRoomType.person.equals(this.getType())) {
            return Lists.newArrayList(UserPref.getInstance(context).getUserAvatarId());
        } else if (ChatRoomType.PERSON_or_DISCUSS.contains(this.type) && this.getMembers() != null && !this.getMembers().isEmpty()) {
            Collections.sort(this.getMembers(), (o1, o2) -> ComparisonChain.start().compare(o1.getId(), o2.getId()).result());
            for (UserProfileEntity profile : this.getMembers()) {
                if (profile.getId().equals(userId)) {
                    selfAvatarId = profile.getAvatarId();
                } else if (avatarIds.size() != max && !profile.isHardCode() && !profile.getId().equals(userId)) {
                    avatarIds.add(profile.getAvatarId());
                }

                if (avatarIds.size() == max) {
                    break;
                }
            }
            if (avatarIds.size() != max && !Strings.isNullOrEmpty(selfAvatarId)) {
                avatarIds.add(selfAvatarId);
            }
            return avatarIds;
        } else if (ChatRoomType.group.equals(this.type) && this.getMembers() != null && !this.getMembers().isEmpty()) {
            if (Strings.isNullOrEmpty(this.avatarId)) {
                Collections.sort(this.getMembers(), (o1, o2) -> ComparisonChain.start().compare(o1.getId(), o2.getId()).result());
                for (UserProfileEntity profile : this.getMembers()) {
                    if (profile.getId().equals(userId)) {
                        selfAvatarId = profile.getAvatarId();
                    } else if (avatarIds.size() != max && !profile.isHardCode()) {
                        avatarIds.add(profile.getAvatarId());
                    }
                    if (avatarIds.size() == max) {
                        break;
                    }
                }

                if (avatarIds.size() != max && !Strings.isNullOrEmpty(selfAvatarId)) {
                    avatarIds.add(selfAvatarId);
                }
                return avatarIds;
            }
            return Strings.isNullOrEmpty(this.avatarId) ? Lists.newArrayList() : Lists.newArrayList(this.avatarId);
        } else if (ChatRoomType.services.equals(this.type) && ServiceNumberType.BOSS.equals(this.serviceNumberType)) {
            return Strings.isNullOrEmpty(this.avatarId) ? Lists.newArrayList() : Lists.newArrayList(this.avatarId);
        } else if (ChatRoomType.SERVICES_or_SUBSCRIBE.contains(this.type)) {
            return Strings.isNullOrEmpty(this.serviceNumberAvatarId) ? Lists.newArrayList() : Lists.newArrayList(this.serviceNumberAvatarId);
        } else {
            return Strings.isNullOrEmpty(this.avatarId) ? Lists.newArrayList() : Lists.newArrayList(this.avatarId);
        }
    }

    public List<String> getAvatarIds2(Context context, int max) {
//        String userId = TokenPref.getInstance(context).getUserId();
        List<String> avatarIds = Lists.newArrayList();
        String selfAvatarId = UserPref.getInstance(context).getUserAvatarId();

        if (ChatRoomType.person.equals(this.getType())) {
            return Lists.newArrayList(selfAvatarId);
        } else if (ChatRoomType.PERSON_or_DISCUSS.contains(this.type) && this.getMemberAvatarData() != null && !this.getMemberAvatarData().isEmpty()) {
//            Collections.sort(this.getMembers(), (o1, o2) -> ComparisonChain.start().compare(o1.getId(), o2.getId()).result());
            for (Map.Entry<String, String> entry : this.getMemberAvatarData().entrySet()) {
                if (avatarIds.size() != max) {
                    avatarIds.add(entry.getValue());
                }
                if (avatarIds.size() == max) {
                    break;
                }
            }
            if (avatarIds.size() != max && avatarIds.size() < this.getMemberIds().size() && !Strings.isNullOrEmpty(selfAvatarId)) {
                avatarIds.add(selfAvatarId);
            }
            return avatarIds;
        } else if (ChatRoomType.group.equals(this.type) && this.getMemberAvatarData() != null && !this.getMemberAvatarData().isEmpty()) {
            if (Strings.isNullOrEmpty(this.avatarId)) {
//                Collections.sort(this.getMemberAvatarData(), (o1, o2) -> ComparisonChain.start().compare(o1.getId(), o2.getId()).result());
                for (Map.Entry<String, String> entry : this.getMemberAvatarData().entrySet()) {
                    if (avatarIds.size() != max) {
                        avatarIds.add(entry.getValue());
                    }
                    if (avatarIds.size() == max) {
                        break;
                    }
                }
                if (avatarIds.size() != max && avatarIds.size() < this.getMemberIds().size() && !Strings.isNullOrEmpty(selfAvatarId)) {
                    avatarIds.add(selfAvatarId);
                }
                return avatarIds;
            } else {
                return Strings.isNullOrEmpty(this.avatarId) ? Lists.newArrayList() : Lists.newArrayList(this.avatarId);
            }
        } else if (ChatRoomType.friend.equals(this.type)) {
            String avatarId = "";
            List<UserProfileEntity> users = UserProfileReference.findUserProfilesByRoomId(null, this.getId());
            for (UserProfileEntity user : users) {
                if (!selfAvatarId.equals(user.getAvatarId())) {
                    avatarId = user.getAvatarId();
                }
            }
            return Strings.isNullOrEmpty(avatarId) ? Lists.newArrayList() : Lists.newArrayList(avatarId);
        } else if (ChatRoomType.services.equals(this.type) && ServiceNumberType.BOSS.equals(this.serviceNumberType)) {
            return Strings.isNullOrEmpty(this.avatarId) ? Lists.newArrayList() : Lists.newArrayList(this.avatarId);
        } else if (ChatRoomType.SERVICES_or_SUBSCRIBE.contains(this.type)) {
            return Strings.isNullOrEmpty(this.serviceNumberAvatarId) ? Lists.newArrayList() : Lists.newArrayList(this.serviceNumberAvatarId);
        } else {
            return Strings.isNullOrEmpty(this.avatarId) ? Lists.newArrayList() : Lists.newArrayList(this.avatarId);
        }
    }

    // Todo 互動時間機制不算正常, 排序現在只用sendTime來判斷顯示
    public double getTimeWeights() {
        long sendTime = (lastMessage != null && lastMessage.getSendTime() > 0) ? lastMessage.getSendTime() : 0;
//        if(unReadNum > 0){
        return sendTime;
//        }
//        return Math.max(updateTime, sendTime);
    }

    /**
     * 有未讀 128
     * ＄有主管未結束 64
     * 置頂 32
     * 我的最愛(有未讀才加上該權重分數) 16
     * 有@我(有未讀才加上該權重分數) 8
     * 是物件聊天室(有未讀才加上該權重分數) 4
     * 有發送失敗 2
     * 有草稿  1
     * 都不符合的 0
     */
    public double getWeights() {
        double weight = 0.0d;
        weight += Math.abs(this.unReadNum) > 0 ? 128.0d : 0.0d; //有未讀
        weight += ConditionUtil.INSTANCE.isNotBossServiceNumberOwnerStop(this) && Math.abs(this.unReadNum) <= 0 ? 64.0d : 0.0d; //主管未結束的N標記，指有主管未結束且沒有未讀
        weight += this.isTop ? 32.0d : 0.0d; //置頂
        weight += this.isFavourite ? Math.abs(this.unReadNum) > 0 ? 16.0d : 0.0d : 0.0d; //我的最愛(有未讀才加上該權重分數)
        weight += this.isAtMe ? Math.abs(this.unReadNum) > 0 ? 8.0d : 0.0d : 0.0d; //有@我(有未讀才加上該權重分數)
        weight += !Strings.isNullOrEmpty(this.businessId) ? Math.abs(this.unReadNum) > 0 ? 4.0d : 0.0d : 0.0d; //物件聊天室(有未讀才加上該權重分數)
        weight += this.failedMessage != null ? 2.0d : 0.0d; //訊息發送失敗

        InputLogBean bean = InputLogBean.from(this.getUnfinishedEdited());
        weight += bean != null && !Strings.isNullOrEmpty(bean.getText()) ? 1.0d : 0.0d; //草稿

        return weight;
    }

    @Override
    public String getClassName() {
        return this.getClass().getSimpleName();
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public ChatRoomType getType() {
        return this.type;
    }

    public String getAvatarId() {
        return this.avatarId;
    }

    public String getOwnerId() {
        if (ownerId == null) return "";
        return this.ownerId;
    }

    public int getUnReadNum() {
        return this.unReadNum;
    }

    public boolean isTop() {
        return this.isTop;
    }

    public long getTopTime() {
        return this.topTime;
    }

    public boolean isCustomName() {
        return this.isCustomName;
    }

    public boolean isMute() {
        return this.isMute;
    }

    public String getServiceNumberName() {
        return this.serviceNumberName;
    }

    public String getServiceNumberAvatarId() {
        return this.serviceNumberAvatarId;
    }

    public ServiceNumberType getServiceNumberType() {
        return this.serviceNumberType;
    }

    public String getServiceNumberAgentId() {
        return this.serviceNumberAgentId;
    }

    public String getServiceNumberOwnerId() {
        return this.serviceNumberOwnerId;
    }

    public ServiceNumberStatus getServiceNumberStatus() {
        return this.serviceNumberStatus;
    }

    public String getBusinessId() {
        return this.businessId;
    }

    public String getBusinessExecutorId() {
        return this.businessExecutorId;
    }

    public String getBusinessName() {
        return this.businessName;
    }

    public BusinessCode getBusinessCode() {
        return this.businessCode;
    }

    public String getLastMessageStr() {
        return this.lastMessageStr;
    }

    public long getFirstunReadMessageTime() {
        return this.firstunReadMessageTime;
    }

    public String getFirstunReadMessageId() {
        return this.firstunReadMessageId;
    }

    public int getRoomMemberIdentity() {
        return this.roomMemberIdentity;
    }

    public String getUnfinishedEdited() {
        return this.unfinishedEdited;
    }

    public long getUnfinishedEditedTime() {
        return this.unfinishedEditedTime;
    }

    public ChatRoomSource getListClassify() {
        return this.listClassify;
    }

    public boolean isBlocked() {
        return this.blocked;
    }

    public boolean isBlock() {
        return this.isBlock;
    }

    public boolean isTransferFlag() {
        return this.transferFlag;
    }

    public String getTransferReason() {
        return this.transferReason;
    }

    public List<String> getMemberIds() {
        return this.memberIds;
    }

    public List<String> getProvisionalIds() {
        return this.provisionalIds;
    }

    public MessageEntity getLastMessage() {
        return this.lastMessage;
    }

    public long getDfrTime() {
        return this.dfrTime;
    }

    public long getLastEndServiceTime() {
        return this.lastEndServiceTime;
    }

    public String getConsultSrcRoomId() {
        return this.consultSrcRoomId;
    }

    public Set<String> getServiceNumberOpenType() {
        return this.serviceNumberOpenType;
    }

    public List<UserProfileEntity> getMembers() {
        return this.members;
    }

    public Map<String, String> getMemberAvatarData() {
        return this.memberAvatarData;
    }

    public MessageEntity getFailedMessage() {
        return this.failedMessage;
    }

    public String getContent() {
        return this.content;
    }

    public boolean isLeftMenuOpen() {
        return this.isLeftMenuOpen;
    }

    public boolean isRightMenuOpen() {
        return this.isRightMenuOpen;
    }

    public boolean isFavourite() {
        return this.isFavourite;
    }

    public boolean isAtMe() {
        return this.isAtMe;
    }

    public boolean isSub() {
        return this.isSub;
    }

    public boolean isSubTop() {
        return this.isSubTop;
    }

    public boolean isSubCenter() {
        return this.isSubCenter;
    }

    public boolean isSubEnd() {
        return this.isSubEnd;
    }

    public boolean isNoMaster() {
        return this.noMaster;
    }

    public boolean isHardCode() {
        return this.isHardCode;
    }

    public boolean isAnimationEnable() {
        return this.animationEnable;
    }

    public boolean isDeleted() {
        return this.deleted;
    }

    public String getBindKey() {
        return this.bindKey;
    }

    public int getConsultSrcUnreadNumber() {
        return this.consultSrcUnreadNumber;
    }

    public boolean isWarned() {
        return this.warned;
    }

    public boolean isServiceNumberOwnerStop() {
        return this.serviceNumberOwnerStop;
    }

    public List<ChatRoomEntity> getComponentEntities() {
        return this.componentEntities;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(ChatRoomType type) {
        this.type = type;
    }

    public void setAvatarId(String avatarId) {
        this.avatarId = avatarId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public void setUnReadNum(int unReadNum) {
        this.unReadNum = unReadNum;
    }

    public void setTop(boolean isTop) {
        this.isTop = isTop;
    }

    public void setTopTime(long topTime) {
        this.topTime = topTime;
    }

    public void setCustomName(boolean isCustomName) {
        this.isCustomName = isCustomName;
    }

    public void setMute(boolean isMute) {
        this.isMute = isMute;
    }

    public void setServiceNumberId(String serviceNumberId) {
        this.serviceNumberId = serviceNumberId;
    }

    public void setServiceNumberName(String serviceNumberName) {
        this.serviceNumberName = serviceNumberName;
    }

    public void setServiceNumberAvatarId(String serviceNumberAvatarId) {
        this.serviceNumberAvatarId = serviceNumberAvatarId;
    }

    public void setServiceNumberType(ServiceNumberType serviceNumberType) {
        this.serviceNumberType = serviceNumberType;
    }

    public void setServiceNumberAgentId(String serviceNumberAgentId) {
        this.serviceNumberAgentId = serviceNumberAgentId;
    }

    public void setServiceNumberOwnerId(String serviceNumberOwnerId) {
        this.serviceNumberOwnerId = serviceNumberOwnerId;
    }

    public void setServiceNumberStatus(ServiceNumberStatus serviceNumberStatus) {
        this.serviceNumberStatus = serviceNumberStatus;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public void setBusinessExecutorId(String businessExecutorId) {
        this.businessExecutorId = businessExecutorId;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public void setBusinessCode(BusinessCode businessCode) {
        this.businessCode = businessCode;
    }

    public void setLastMessageStr(String lastMessageStr) {
        this.lastMessageStr = lastMessageStr;
    }

    public void setFirstunReadMessageTime(long firstunReadMessageTime) {
        this.firstunReadMessageTime = firstunReadMessageTime;
    }

    public void setFirstunReadMessageId(String firstunReadMessageId) {
        this.firstunReadMessageId = firstunReadMessageId;
    }

    public void setRoomMemberIdentity(int roomMemberIdentity) {
        this.roomMemberIdentity = roomMemberIdentity;
    }

    public void setUnfinishedEdited(String unfinishedEdited) {
        this.unfinishedEdited = unfinishedEdited;
    }

    public void setUnfinishedEditedTime(long unfinishedEditedTime) {
        this.unfinishedEditedTime = unfinishedEditedTime;
    }

    public void setListClassify(ChatRoomSource listClassify) {
        this.listClassify = listClassify;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public void setBlock(boolean isBlock) {
        this.isBlock = isBlock;
    }

    public void setTransferFlag(boolean transferFlag) {
        this.transferFlag = transferFlag;
    }

    public void setTransferReason(String transferReason) {
        this.transferReason = transferReason;
    }

    public void setMemberIds(List<String> memberIds) {
        this.memberIds = memberIds;
    }

    public void setProvisionalIds(List<String> provisionalIds) {
        this.provisionalIds = provisionalIds;
    }

    public void setLastMessage(MessageEntity lastMessage) {
        this.lastMessage = lastMessage;
    }

    public void setDfrTime(long dfrTime) {
        this.dfrTime = dfrTime;
    }

    public void setLastEndServiceTime(long lastEndServiceTime) {
        this.lastEndServiceTime = lastEndServiceTime;
    }

    public void setConsultSrcRoomId(String consultSrcRoomId) {
        this.consultSrcRoomId = consultSrcRoomId;
    }

    public void setServiceNumberOpenType(Set<String> serviceNumberOpenType) {
        this.serviceNumberOpenType = serviceNumberOpenType;
    }

    public String getAiConsultId() {
        return aiConsultId;
    }

    public void setAiConsultId(String aiConsultId) {
        this.aiConsultId = aiConsultId;
    }

    public void setMembers(List<UserProfileEntity> members) {
        this.members = members;
    }

    public void setMemberAvatarData(Map<String, String> memberAvatarData) {
        this.memberAvatarData = memberAvatarData;
    }

    public void setFailedMessage(MessageEntity failedMessage) {
        this.failedMessage = failedMessage;
    }

    public List<UserProfileEntity> getAgentsList() {
        return agentsList;
    }

    public void setAgentsList(
        List<UserProfileEntity> agentsList) {
        this.agentsList = agentsList;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setLeftMenuOpen(boolean isLeftMenuOpen) {
        this.isLeftMenuOpen = isLeftMenuOpen;
    }

    public void setRightMenuOpen(boolean isRightMenuOpen) {
        this.isRightMenuOpen = isRightMenuOpen;
    }

    public void setFavourite(boolean isFavourite) {
        this.isFavourite = isFavourite;
    }

    public void setAtMe(boolean isAtMe) {
        this.isAtMe = isAtMe;
    }

    public void setSub(boolean isSub) {
        this.isSub = isSub;
    }

    public void setSubTop(boolean isSubTop) {
        this.isSubTop = isSubTop;
    }

    public void setSubCenter(boolean isSubCenter) {
        this.isSubCenter = isSubCenter;
    }

    public void setSubEnd(boolean isSubEnd) {
        this.isSubEnd = isSubEnd;
    }

    public void setNoMaster(boolean noMaster) {
        this.noMaster = noMaster;
    }

    public void setHardCode(boolean isHardCode) {
        this.isHardCode = isHardCode;
    }

    public void setAnimationEnable(boolean animationEnable) {
        this.animationEnable = animationEnable;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public void setBindKey(String bindKey) {
        this.bindKey = bindKey;
    }

    public void setConsultSrcUnreadNumber(int consultSrcUnreadNumber) {
        this.consultSrcUnreadNumber = consultSrcUnreadNumber;
    }

    public void setWarned(boolean warned) {
        this.warned = warned;
    }

    public void setServiceNumberOwnerStop(boolean serviceNumberOwnerStop) {
        this.serviceNumberOwnerStop = serviceNumberOwnerStop;
    }

    public String getIdentityId() {
        return identityId;
    }

    public void setIdentityId(String identityId) {
        this.identityId = identityId;
    }

    public void setComponentEntities(List<ChatRoomEntity> componentEntities) {
        this.componentEntities = componentEntities;


    }

    public boolean isMember_deleted() {
        return member_deleted;
    }

    public void setMember_deleted(boolean member_deleted) {
        this.member_deleted = member_deleted;
    }

    public ChatRoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(ChatRoomType roomType) {
        this.roomType = roomType;
    }

    public List<ChatRoomMemberResponse> getChatRoomMember() {
        return chatRoomMember;
    }

    public void setChatRoomMember(List<ChatRoomMemberResponse> chatRoomMember) {
        this.chatRoomMember = chatRoomMember;
    }

    // 判斷是否隱藏
    public Boolean isHidden() {
        // 過濾如果是滑動刪除的聊天室
        if (getType() != ChatRoomType.person) {
            if (getLastMessage() != null) {
                if (getDfrTime() > getLastMessage().getSendTime()) return true;
            } else if (!Strings.isNullOrEmpty(getLastMessageStr())) {
                MessageEntity lastMessage = JsonHelper.getInstance().from(getLastMessageStr(), MessageEntity.class);
                if (getDfrTime() > lastMessage.getSendTime()) return true;
            } else {
                if (getDfrTime() > 0) return true;
            }
        }

        if (ChatRoomType.serviceMember == getType()) {
            if (getLastMessage() != null) {
                if (getDfrTime() > getLastMessage().getSendTime()) return true;
            } else {
                if (getDfrTime() > 0) return true;
            }

            if (isDeleted()) {
                return true;
            } else return isMember_deleted() || (getLastMessage() != null && getDfrTime() > getLastMessage().getSendTime());
        }

        return false;
    }

    public ChatRoomEntityBuilder toBuilder() {
        return new ChatRoomEntityBuilder().id(this.id).name(this.name).type(this.type).avatarId(this.avatarId).ownerId(this.ownerId).unReadNum(this.unReadNum).isTop(this.isTop).topTime(this.topTime).isCustomName(this.isCustomName).isMute(this.isMute).serviceNumberId(this.serviceNumberId).serviceNumberName(this.serviceNumberName).serviceNumberAvatarId(this.serviceNumberAvatarId).serviceNumberType(this.serviceNumberType).serviceNumberAgentId(this.serviceNumberAgentId).serviceNumberOwnerId(this.serviceNumberOwnerId).serviceNumberStatus(this.serviceNumberStatus).businessId(this.businessId).businessExecutorId(this.businessExecutorId).businessName(this.businessName).businessCode(this.businessCode).lastMessageStr(this.lastMessageStr).firstunReadMessageTime(this.firstunReadMessageTime).firstunReadMessageId(this.firstunReadMessageId).roomMemberIdentity(this.roomMemberIdentity).updateTime(this.updateTime).unfinishedEdited(this.unfinishedEdited).unfinishedEditedTime(this.unfinishedEditedTime).listClassify(this.listClassify).blocked(this.blocked).isBlock(this.isBlock).transferFlag(this.transferFlag).transferReason(this.transferReason).memberIds(this.memberIds).provisionalIds(this.provisionalIds).lastMessage(this.lastMessage).dfrTime(this.dfrTime).lastEndServiceTime(this.lastEndServiceTime).consultSrcRoomId(this.consultSrcRoomId).serviceNumberOpenType(this.serviceNumberOpenType).members(this.members).memberAvatarData(this.memberAvatarData).failedMessage(this.failedMessage).content(this.content).isLeftMenuOpen(this.isLeftMenuOpen).isRightMenuOpen(this.isRightMenuOpen).isFavourite(this.isFavourite).isAtMe(this.isAtMe).isSub(this.isSub).isSubTop(this.isSubTop).isSubCenter(this.isSubCenter).isSubEnd(this.isSubEnd).noMaster(this.noMaster).isHardCode(this.isHardCode).animationEnable(this.animationEnable).deleted(this.deleted).bindKey(this.bindKey).consultSrcUnreadNumber(this.consultSrcUnreadNumber).lastSequence(this.lastSequence).chatId(this.chatId).warned(this.warned).serviceNumberOwnerStop(this.serviceNumberOwnerStop).componentEntities(this.componentEntities).ownerUserType(this.ownerUserType);
    }

    public static class ChatRoomEntityBuilder {
        private String id;
        private String name;
        private ChatRoomType type$value;
        private boolean type$set;
        private String avatarId;
        private String ownerId;
        private int unReadNum;
        private boolean isTop;
        private long topTime;
        private boolean isCustomName;
        private boolean isMute;
        private String serviceNumberId$value;
        private boolean serviceNumberId$set;
        private String serviceNumberName;
        private String serviceNumberAvatarId;
        private ServiceNumberType serviceNumberType$value;
        private boolean serviceNumberType$set;
        private String serviceNumberAgentId$value;
        private boolean serviceNumberAgentId$set;
        private String serviceNumberOwnerId;
        private ServiceNumberStatus serviceNumberStatus$value;
        private boolean serviceNumberStatus$set;
        private String businessId;
        private String businessExecutorId;
        private String businessName;
        private BusinessCode businessCode$value;
        private boolean businessCode$set;
        private String lastMessageStr;
        private long firstunReadMessageTime;
        private String firstunReadMessageId;
        private int roomMemberIdentity;
        private long updateTime;
        private String unfinishedEdited;
        private long unfinishedEditedTime;
        private ChatRoomSource listClassify$value;
        private boolean listClassify$set;
        private boolean blocked;
        private boolean isBlock;
        private boolean transferFlag;
        private String transferReason;
        private List<String> memberIds$value;
        private boolean memberIds$set;
        private List<String> provisionalIds;
        private MessageEntity lastMessage;
        private long dfrTime$value;
        private boolean dfrTime$set;
        private long lastEndServiceTime;
        private String consultSrcRoomId$value;
        private boolean consultSrcRoomId$set;
        private Set<String> serviceNumberOpenType$value;
        private boolean serviceNumberOpenType$set;
        private List<UserProfileEntity> members$value;
        private boolean members$set;
        private Map<String, String> memberAvatarData$value;
        private boolean memberAvatarData$set;
        private MessageEntity failedMessage;
        private String content$value;
        private boolean content$set;
        private boolean isLeftMenuOpen;
        private boolean isRightMenuOpen;
        private boolean isFavourite;
        private boolean isAtMe;
        private boolean isSub;
        private boolean isSubTop;
        private boolean isSubCenter;
        private boolean isSubEnd;
        private boolean noMaster;
        private boolean isHardCode;
        private boolean animationEnable;
        private boolean deleted;
        private String bindKey;
        private int consultSrcUnreadNumber;
        private int lastSequence;
        private String chatId;
        private boolean warned;
        private boolean serviceNumberOwnerStop;
        private List<ChatRoomEntity> componentEntities$value;
        private boolean componentEntities$set;
        private String ownerUserType;

        private List<ChatRoomMemberResponse> chatRoomMember;

        ChatRoomEntityBuilder() {
        }

        public ChatRoomEntityBuilder id(String id) {
            this.id = id;
            return this;
        }

        public ChatRoomEntityBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ChatRoomEntityBuilder type(ChatRoomType type) {
            this.type$value = type;
            this.type$set = true;
            return this;
        }

        public ChatRoomEntityBuilder avatarId(String avatarId) {
            this.avatarId = avatarId;
            return this;
        }

        public ChatRoomEntityBuilder ownerId(String ownerId) {
            this.ownerId = ownerId;
            return this;
        }

        public ChatRoomEntityBuilder unReadNum(int unReadNum) {
            this.unReadNum = unReadNum;
            return this;
        }

        public ChatRoomEntityBuilder isTop(boolean isTop) {
            this.isTop = isTop;
            return this;
        }

        public ChatRoomEntityBuilder topTime(long topTime) {
            this.topTime = topTime;
            return this;
        }

        public ChatRoomEntityBuilder isCustomName(boolean isCustomName) {
            this.isCustomName = isCustomName;
            return this;
        }

        public ChatRoomEntityBuilder isMute(boolean isMute) {
            this.isMute = isMute;
            return this;
        }

        public ChatRoomEntityBuilder serviceNumberId(String serviceNumberId) {
            this.serviceNumberId$value = serviceNumberId;
            this.serviceNumberId$set = true;
            return this;
        }

        public ChatRoomEntityBuilder serviceNumberName(String serviceNumberName) {
            this.serviceNumberName = serviceNumberName;
            return this;
        }

        public ChatRoomEntityBuilder serviceNumberAvatarId(String serviceNumberAvatarId) {
            this.serviceNumberAvatarId = serviceNumberAvatarId;
            return this;
        }

        public ChatRoomEntityBuilder serviceNumberType(ServiceNumberType serviceNumberType) {
            this.serviceNumberType$value = serviceNumberType;
            this.serviceNumberType$set = true;
            return this;
        }

        public ChatRoomEntityBuilder serviceNumberAgentId(String serviceNumberAgentId) {
            this.serviceNumberAgentId$value = serviceNumberAgentId;
            this.serviceNumberAgentId$set = true;
            return this;
        }

        public ChatRoomEntityBuilder serviceNumberOwnerId(String serviceNumberOwnerId) {
            this.serviceNumberOwnerId = serviceNumberOwnerId;
            return this;
        }

        public ChatRoomEntityBuilder serviceNumberStatus(ServiceNumberStatus serviceNumberStatus) {
            this.serviceNumberStatus$value = serviceNumberStatus;
            this.serviceNumberStatus$set = true;
            return this;
        }

        public ChatRoomEntityBuilder businessId(String businessId) {
            this.businessId = businessId;
            return this;
        }

        public ChatRoomEntityBuilder businessExecutorId(String businessExecutorId) {
            this.businessExecutorId = businessExecutorId;
            return this;
        }

        public ChatRoomEntityBuilder businessName(String businessName) {
            this.businessName = businessName;
            return this;
        }

        public ChatRoomEntityBuilder businessCode(BusinessCode businessCode) {
            this.businessCode$value = businessCode;
            this.businessCode$set = true;
            return this;
        }

        public ChatRoomEntityBuilder lastMessageStr(String lastMessageStr) {
            this.lastMessageStr = lastMessageStr;
            return this;
        }

        public ChatRoomEntityBuilder firstunReadMessageTime(long firstunReadMessageTime) {
            this.firstunReadMessageTime = firstunReadMessageTime;
            return this;
        }

        public ChatRoomEntityBuilder firstunReadMessageId(String firstunReadMessageId) {
            this.firstunReadMessageId = firstunReadMessageId;
            return this;
        }

        public ChatRoomEntityBuilder roomMemberIdentity(int roomMemberIdentity) {
            this.roomMemberIdentity = roomMemberIdentity;
            return this;
        }

        public ChatRoomEntityBuilder updateTime(long updateTime) {
            this.updateTime = updateTime;
            return this;
        }

        public ChatRoomEntityBuilder unfinishedEdited(String unfinishedEdited) {
            this.unfinishedEdited = unfinishedEdited;
            return this;
        }

        public ChatRoomEntityBuilder unfinishedEditedTime(long unfinishedEditedTime) {
            this.unfinishedEditedTime = unfinishedEditedTime;
            return this;
        }

        public ChatRoomEntityBuilder listClassify(ChatRoomSource listClassify) {
            this.listClassify$value = listClassify;
            this.listClassify$set = true;
            return this;
        }

        public ChatRoomEntityBuilder blocked(boolean blocked) {
            this.blocked = blocked;
            return this;
        }

        public ChatRoomEntityBuilder isBlock(boolean isBlock) {
            this.isBlock = isBlock;
            return this;
        }

        public ChatRoomEntityBuilder transferFlag(boolean transferFlag) {
            this.transferFlag = transferFlag;
            return this;
        }

        public ChatRoomEntityBuilder transferReason(String transferReason) {
            this.transferReason = transferReason;
            return this;
        }

        public ChatRoomEntityBuilder memberIds(List<String> memberIds) {
            this.memberIds$value = memberIds;
            this.memberIds$set = true;
            return this;
        }

        public ChatRoomEntityBuilder provisionalIds(List<String> provisionalIds) {
            this.provisionalIds = provisionalIds;
            return this;
        }

        public ChatRoomEntityBuilder lastMessage(MessageEntity lastMessage) {
            this.lastMessage = lastMessage;
            return this;
        }

        public ChatRoomEntityBuilder dfrTime(long dfrTime) {
            this.dfrTime$value = dfrTime;
            this.dfrTime$set = true;
            return this;
        }

        public ChatRoomEntityBuilder lastEndServiceTime(long lastEndServiceTime) {
            this.lastEndServiceTime = lastEndServiceTime;
            return this;
        }

        public ChatRoomEntityBuilder consultSrcRoomId(String consultSrcRoomId) {
            this.consultSrcRoomId$value = consultSrcRoomId;
            this.consultSrcRoomId$set = true;
            return this;
        }

        public ChatRoomEntityBuilder serviceNumberOpenType(Set<String> serviceNumberOpenType) {
            this.serviceNumberOpenType$value = serviceNumberOpenType;
            this.serviceNumberOpenType$set = true;
            return this;
        }

        public ChatRoomEntityBuilder members(List<UserProfileEntity> members) {
            this.members$value = members;
            this.members$set = true;
            return this;
        }

        public ChatRoomEntityBuilder memberAvatarData(Map<String, String> memberAvatarData) {
            this.memberAvatarData$value = memberAvatarData;
            this.memberAvatarData$set = true;
            return this;
        }

        public ChatRoomEntityBuilder failedMessage(MessageEntity failedMessage) {
            this.failedMessage = failedMessage;
            return this;
        }

        public ChatRoomEntityBuilder content(String content) {
            this.content$value = content;
            this.content$set = true;
            return this;
        }

        public ChatRoomEntityBuilder isLeftMenuOpen(boolean isLeftMenuOpen) {
            this.isLeftMenuOpen = isLeftMenuOpen;
            return this;
        }

        public ChatRoomEntityBuilder isRightMenuOpen(boolean isRightMenuOpen) {
            this.isRightMenuOpen = isRightMenuOpen;
            return this;
        }

        public ChatRoomEntityBuilder isFavourite(boolean isFavourite) {
            this.isFavourite = isFavourite;
            return this;
        }

        public ChatRoomEntityBuilder isAtMe(boolean isAtMe) {
            this.isAtMe = isAtMe;
            return this;
        }

        public ChatRoomEntityBuilder isSub(boolean isSub) {
            this.isSub = isSub;
            return this;
        }

        public ChatRoomEntityBuilder isSubTop(boolean isSubTop) {
            this.isSubTop = isSubTop;
            return this;
        }

        public ChatRoomEntityBuilder isSubCenter(boolean isSubCenter) {
            this.isSubCenter = isSubCenter;
            return this;
        }

        public ChatRoomEntityBuilder isSubEnd(boolean isSubEnd) {
            this.isSubEnd = isSubEnd;
            return this;
        }

        public ChatRoomEntityBuilder noMaster(boolean noMaster) {
            this.noMaster = noMaster;
            return this;
        }

        public ChatRoomEntityBuilder isHardCode(boolean isHardCode) {
            this.isHardCode = isHardCode;
            return this;
        }

        public ChatRoomEntityBuilder animationEnable(boolean animationEnable) {
            this.animationEnable = animationEnable;
            return this;
        }

        public ChatRoomEntityBuilder deleted(boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        public ChatRoomEntityBuilder bindKey(String bindKey) {
            this.bindKey = bindKey;
            return this;
        }

        public ChatRoomEntityBuilder consultSrcUnreadNumber(int consultSrcUnreadNumber) {
            this.consultSrcUnreadNumber = consultSrcUnreadNumber;
            return this;
        }

        public ChatRoomEntityBuilder lastSequence(int lastSequence) {
            this.lastSequence = lastSequence;
            return this;
        }

        public ChatRoomEntityBuilder chatId(String chatId) {
            this.chatId = chatId;
            return this;
        }

        public ChatRoomEntityBuilder warned(boolean warned) {
            this.warned = warned;
            return this;
        }

        public ChatRoomEntityBuilder serviceNumberOwnerStop(boolean serviceNumberOwnerStop) {
            this.serviceNumberOwnerStop = serviceNumberOwnerStop;
            return this;
        }

        public ChatRoomEntityBuilder componentEntities(List<ChatRoomEntity> componentEntities) {
            this.componentEntities$value = componentEntities;
            this.componentEntities$set = true;
            return this;
        }

        public ChatRoomEntityBuilder ownerUserType(String ownerUserType) {
            this.ownerUserType = ownerUserType;
            return this;
        }

        public ChatRoomEntityBuilder chatRoomMember(List<ChatRoomMemberResponse> chatRoomMember) {
            this.chatRoomMember = chatRoomMember;
            return this;
        }

        public ChatRoomEntity build() {
            ChatRoomType type$value = this.type$value;
            if (!this.type$set) {
                type$value = ChatRoomEntity.$default$type();
            }
            String serviceNumberId$value = this.serviceNumberId$value;
            if (!this.serviceNumberId$set) {
                serviceNumberId$value = ChatRoomEntity.$default$serviceNumberId();
            }
            ServiceNumberType serviceNumberType$value = this.serviceNumberType$value;
            if (!this.serviceNumberType$set) {
                serviceNumberType$value = ChatRoomEntity.$default$serviceNumberType();
            }
            String serviceNumberAgentId$value = this.serviceNumberAgentId$value;
            if (!this.serviceNumberAgentId$set) {
                serviceNumberAgentId$value = ChatRoomEntity.$default$serviceNumberAgentId();
            }
            ServiceNumberStatus serviceNumberStatus$value = this.serviceNumberStatus$value;
            if (!this.serviceNumberStatus$set) {
                serviceNumberStatus$value = ChatRoomEntity.$default$serviceNumberStatus();
            }
            BusinessCode businessCode$value = this.businessCode$value;
            if (!this.businessCode$set) {
                businessCode$value = ChatRoomEntity.$default$businessCode();
            }
            ChatRoomSource listClassify$value = this.listClassify$value;
            if (!this.listClassify$set) {
                listClassify$value = ChatRoomEntity.$default$listClassify();
            }
            List<String> memberIds$value = this.memberIds$value;
            if (!this.memberIds$set) {
                memberIds$value = ChatRoomEntity.$default$memberIds();
            }
            long dfrTime$value = this.dfrTime$value;
            if (!this.dfrTime$set) {
                dfrTime$value = ChatRoomEntity.$default$dfrTime();
            }
            String consultSrcRoomId$value = this.consultSrcRoomId$value;
            if (!this.consultSrcRoomId$set) {
                consultSrcRoomId$value = ChatRoomEntity.$default$consultSrcRoomId();
            }
            Set<String> serviceNumberOpenType$value = this.serviceNumberOpenType$value;
            if (!this.serviceNumberOpenType$set) {
                serviceNumberOpenType$value = ChatRoomEntity.$default$serviceNumberOpenType();
            }
            List<UserProfileEntity> members$value = this.members$value;
            if (!this.members$set) {
                members$value = ChatRoomEntity.$default$members();
            }
            Map<String, String> memberAvatarData$value = this.memberAvatarData$value;
            if (!this.memberAvatarData$set) {
                memberAvatarData$value = ChatRoomEntity.$default$memberAvatarData();
            }
            String content$value = this.content$value;
            if (!this.content$set) {
                content$value = ChatRoomEntity.$default$content();
            }
            List<ChatRoomEntity> componentEntities$value = this.componentEntities$value;
            if (!this.componentEntities$set) {
                componentEntities$value = ChatRoomEntity.$default$componentEntities();
            }
            return new ChatRoomEntity(id, name, type$value, avatarId, ownerId, unReadNum, isTop, topTime, isCustomName, isMute, serviceNumberId$value, serviceNumberName, serviceNumberAvatarId, serviceNumberType$value, serviceNumberAgentId$value, serviceNumberOwnerId, serviceNumberStatus$value, businessId, businessExecutorId, businessName, businessCode$value, lastMessageStr, firstunReadMessageTime, firstunReadMessageId, roomMemberIdentity, updateTime, unfinishedEdited, unfinishedEditedTime, listClassify$value, blocked, isBlock, transferFlag, transferReason, memberIds$value, provisionalIds, lastMessage, dfrTime$value, lastEndServiceTime, consultSrcRoomId$value, serviceNumberOpenType$value, members$value, memberAvatarData$value, failedMessage, content$value, isLeftMenuOpen, isRightMenuOpen, isFavourite, isAtMe, isSub, isSubTop, isSubCenter, isSubEnd, noMaster, isHardCode, animationEnable, deleted, bindKey, consultSrcUnreadNumber, lastSequence, chatId, warned, serviceNumberOwnerStop, componentEntities$value, ownerUserType, chatRoomMember);
        }

        @NonNull
        public String toString() {
            return "ChatRoomEntity.ChatRoomEntityBuilder(id=" + this.id + ", name=" + this.name + ", type$value=" + this.type$value + ", avatarId=" + this.avatarId + ", ownerId=" + this.ownerId + ", unReadNum=" + this.unReadNum + ", isTop=" + this.isTop + ", topTime=" + this.topTime + ", isCustomName=" + this.isCustomName + ", isMute=" + this.isMute + ", serviceNumberId$value=" + this.serviceNumberId$value + ", serviceNumberName=" + this.serviceNumberName + ", serviceNumberAvatarId=" + this.serviceNumberAvatarId + ", serviceNumberType$value=" + this.serviceNumberType$value + ", serviceNumberAgentId$value=" + this.serviceNumberAgentId$value + ", serviceNumberOwnerId=" + this.serviceNumberOwnerId + ", serviceNumberStatus$value=" + this.serviceNumberStatus$value + ", businessId=" + this.businessId + ", businessExecutorId=" + this.businessExecutorId + ", businessName=" + this.businessName + ", businessCode$value=" + this.businessCode$value + ", lastMessageStr=" + this.lastMessageStr + ", firstunReadMessageTime=" + this.firstunReadMessageTime + ", firstunReadMessageId=" + this.firstunReadMessageId + ", roomMemberIdentity=" + this.roomMemberIdentity + ", updateTime=" + this.updateTime + ", unfinishedEdited=" + this.unfinishedEdited + ", unfinishedEditedTime=" + this.unfinishedEditedTime + ", listClassify$value=" + this.listClassify$value + ", blocked=" + this.blocked + ", isBlock=" + this.isBlock + ", transferFlag=" + this.transferFlag + ", transferReason=" + this.transferReason + ", memberIds$value=" + this.memberIds$value + ", provisionalIds=" + this.provisionalIds + ", lastMessage=" + this.lastMessage + ", dfrTime$value=" + this.dfrTime$value + ", lastEndServiceTime=" + this.lastEndServiceTime + ", consultSrcRoomId$value=" + this.consultSrcRoomId$value + ", serviceNumberOpenType$value=" + this.serviceNumberOpenType$value + ", members$value=" + this.members$value + ", memberAvatarData$value=" + this.memberAvatarData$value + ", failedMessage=" + this.failedMessage + ", content$value=" + this.content$value + ", isLeftMenuOpen=" + this.isLeftMenuOpen + ", isRightMenuOpen=" + this.isRightMenuOpen + ", isFavourite=" + this.isFavourite + ", isAtMe=" + this.isAtMe + ", isSub=" + this.isSub + ", isSubTop=" + this.isSubTop + ", isSubCenter=" + this.isSubCenter + ", isSubEnd=" + this.isSubEnd + ", noMaster=" + this.noMaster + ", isHardCode=" + this.isHardCode + ", animationEnable=" + this.animationEnable + ", deleted=" + this.deleted + ", bindKey=" + this.bindKey + ", consultSrcUnreadNumber=" + this.consultSrcUnreadNumber + ", lastSequence=" + this.lastSequence + ", chatId=" + this.chatId + ", warned=" + this.warned + ", serviceNumberOwnerStop=" + this.serviceNumberOwnerStop + ", componentEntities$value=" + this.componentEntities$value + ")";
        }
    }
}
