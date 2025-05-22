package tw.com.chainsea.ce.sdk.bean.servicenumber;

import android.content.ContentValues;
import android.database.Cursor;

import androidx.annotation.NonNull;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tw.com.chainsea.android.common.datetime.DateTimeHelper;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.bean.common.EnableType;
import tw.com.chainsea.ce.sdk.bean.msg.Tools;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
import tw.com.chainsea.ce.sdk.bean.statistics.ServiceNumberStatType;
import tw.com.chainsea.ce.sdk.bean.statistics.StatisticsEntity;
import tw.com.chainsea.ce.sdk.database.DBContract;
import tw.com.chainsea.ce.sdk.http.ce.model.Member;

/**
 * current by evan on 2020-08-19
 *
 * @author Evan Wang
 * date 2020-08-19
 */

public class ServiceNumberEntity implements Serializable {
    private static final long serialVersionUID = -7436992702878480645L;

    private String serviceNumberId;
    @SerializedName(value = "serviceNumberAvatarId", alternate = {"avatarId"})
    private String avatarId;
    private String name;
    private String description;
    private String roomId;
    private String broadcastRoomId;
    private String serviceMemberRoomId;

    private boolean isTop;

    @SerializedName("serviceWelcomeMessage")
    private String firstWelcomeMessage = "";
    @SerializedName("everyContactMessage")
    private String eachWelcomeMessage = "";
    @SerializedName("serviceIdleMessage")
    private String intervalWelcomeMessage = "";

    private List<Member> memberItems = Lists.newArrayList();
    private boolean isOwner;
    private String ownerId;
    private boolean isManager;
    private boolean isSubscribe;
    private boolean isCommon;
    private String status = "Enable";
    private EnableType enable = EnableType.Y;
    private EnableType isInSiteService = EnableType.N;
    private EnableType isOutSiteService = EnableType.N;

    private int internalSubscribeCount;  // Number of internal subscriptions
    private int externalSubscribeCount; // Number of external subscriptions

    // statistics data
    @SerializedName("serviceNumberStat")
    private Map serviceNumberStat;
    private String serviceNumberType;

    private List<String> serviceOpenType = Lists.newArrayList();


    // Local control project (Member chat room unread)
    private int unreadNumber;
    private boolean enableConsultationAI;
    private boolean robotServiceFlag;

    private List<FacebookFansPages> faceBookFansPages;
    private List<InstagramFansPages> instagramFansPages;
    private BusinessCardInfo businessCardInfo;
    private String allChannelURL;
    private int serviceTimeoutTime;
    private int serviceIdleTime;

    public ServiceNumberEntity(String serviceNumberId, String avatarId, String name, String description, String roomId, String broadcastRoomId, String serviceMemberRoomId, boolean isTop, String firstWelcomeMessage, String eachWelcomeMessage, String intervalWelcomeMessage, List<Member> memberItems, boolean isOwner, boolean isManager, boolean isSubscribe, boolean isCommon, String status, EnableType enable, EnableType isInSiteService, EnableType isOutSiteService, int internalSubscribeCount, int externalSubscribeCount, Map serviceNumberStat, String serviceNumberType, int unreadNumber, List<String> serviceOpenType, boolean enableConsultationAI, boolean robotServiceFlag, List<FacebookFansPages> facebookFansPages, int serviceTimeoutTime, int serviceIdleTime) {
        this.serviceNumberId = serviceNumberId;
        this.avatarId = avatarId;
        this.name = name;
        this.description = description;
        this.roomId = roomId;
        this.broadcastRoomId = broadcastRoomId;
        this.serviceMemberRoomId = serviceMemberRoomId;
        this.isTop = isTop;
        this.firstWelcomeMessage = firstWelcomeMessage;
        this.eachWelcomeMessage = eachWelcomeMessage;
        this.intervalWelcomeMessage = intervalWelcomeMessage;
        this.memberItems = memberItems;
        this.isOwner = isOwner;
        this.isManager = isManager;
        this.isSubscribe = isSubscribe;
        this.isCommon = isCommon;
        this.status = status;
        this.enable = enable;
        this.isInSiteService = isInSiteService;
        this.isOutSiteService = isOutSiteService;
        this.internalSubscribeCount = internalSubscribeCount;
        this.externalSubscribeCount = externalSubscribeCount;
        this.serviceNumberStat = serviceNumberStat;
        this.serviceNumberType = serviceNumberType;
        this.unreadNumber = unreadNumber;
        this.serviceOpenType = serviceOpenType;
//        this.chatMember = chatMember;
        this.enableConsultationAI = enableConsultationAI;
        this.robotServiceFlag = robotServiceFlag;
        this.faceBookFansPages = facebookFansPages;
        this.serviceTimeoutTime = serviceTimeoutTime;
        this.serviceIdleTime = serviceIdleTime;
    }

    public ServiceNumberEntity() {
    }

    private static String $default$firstWelcomeMessage() {
        return "";
    }

    private static String $default$eachWelcomeMessage() {
        return "";
    }

    private static String $default$intervalWelcomeMessage() {
        return "";
    }

    private static List<Member> $default$memberItems() {
        return Lists.newArrayList();
    }

    private static EnableType $default$enable() {
        return EnableType.Y;
    }

    private static EnableType $default$isInSiteService() {
        return EnableType.N;
    }

    private static EnableType $default$isOutSiteService() {
        return EnableType.N;
    }

    public static ServiceNumberEntityBuilder Build() {
        return new ServiceNumberEntityBuilder();
    }

    public int getServiceTimeoutTime() {
        return serviceTimeoutTime;
    }

    public void setServiceTimeoutTime(int serviceTimeoutTime) {
        this.serviceTimeoutTime = serviceTimeoutTime;
    }

    public int getServiceIdleTime() {
        return serviceIdleTime;
    }

    public void setServiceIdleTime(int serviceIdleTime) {
        this.serviceIdleTime = serviceIdleTime;
    }

    public Set<String> memberIds() {
        Set<String> idset = Sets.newHashSet();
        for (Member member : memberItems) {
            idset.add(member.getId());
        }
        return idset;
    }

    public ServiceNumberStat getStat() {
        try {
            return JsonHelper.getInstance().from(JsonHelper.getInstance().toJson(this.serviceNumberStat), ServiceNumberStat.class);
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return null;
        }
    }

    public List<StatisticsEntity> getStatisticsEntities() {
        if (getStat() != null) {
            ServiceNumberStat stat = getStat();

            List<StatisticsEntity> entities = Lists.newArrayList();
            long now = System.currentTimeMillis();
            buildStatistics(ServiceNumberStatType.LAST_DAY, stat.getLastDay(), entities, now);
            buildStatistics(ServiceNumberStatType.LAST_30_DAY, stat.getLast30Day(), entities, now);
            buildStatistics(ServiceNumberStatType.LAST_DAY_MEMBER, stat.getLastDayMember(), entities, now);
            buildStatistics(ServiceNumberStatType.LAST_30_DAY_MEMBER, stat.getLast30DayMember(), entities, now);
            return entities;
        }
        return Lists.newArrayList();
    }

    private void buildStatistics(ServiceNumberStatType type, ServiceNumberStat.Stat stat, List<StatisticsEntity> entities, long now) {
        if (stat != null) {
            stat.setType(type);

            // 解析開始～結束時間
            long startTime = 0L;
            long endTime = 0L;
            if (!Strings.isNullOrEmpty(stat.getStatDate())) {
                try {
                    Calendar current = DateTimeHelper.parseToCal(stat.getStatDate(), "yyyy-MM-dd");
                    Calendar start = DateTimeHelper.getStartByPlusDay(current, type.getPlusDay());
                    startTime = start.getTimeInMillis();
                    Calendar end = DateTimeHelper.getEnd(current);
                    endTime = end.getTimeInMillis();
                } catch (ParseException e) {
                    CELog.e(e.getMessage());
                }
            }

            entities.add(StatisticsEntity.Build()
                .relId(this.serviceNumberId)
                .originalContent(JsonHelper.getInstance().toJson(stat))
                .ascription(ChatRoomType.services.name())
                .statType(type.name())
                .totalRow(stat.getTotalServiceCount())
                .rowCount(stat.getServiceCount())
                .startTime(startTime)
                .endTime(endTime)
                .updateTime(now)
                .build());
        }
    }


    public static ServiceNumberEntity.ServiceNumberEntityBuilder formatByCursor(Map<String, Integer> index, Cursor cursor) {
//        Type typeToken = new TypeToken<List<ChatRoomMemberResponse>>(){}.getType();
        ServiceNumberEntity.ServiceNumberEntityBuilder build = ServiceNumberEntity.Build()
            .serviceNumberId(cursor.getString(index.get(DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID)))
            .serviceNumberType(cursor.getString(index.get(DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_TYPE)))
            .roomId(cursor.getString(index.get(DBContract.ServiceNumEntry.COLUMN_ROOM_ID)))
            .broadcastRoomId(cursor.getString(index.get(DBContract.ServiceNumEntry.COLUMN_BROADCAST_ROOM_ID)))

            .isOwner("true".equals(cursor.getString(index.get(DBContract.ServiceNumEntry.COLUMN_IS_OWNER))))
            .isManager("true".equals(cursor.getString(index.get(DBContract.ServiceNumEntry.COLUMN_IS_MANAGER))))
            .isSubscribe("true".equals(cursor.getString(index.get(DBContract.ServiceNumEntry.COLUMN_SUBSCRIBE))))
            .status(cursor.getString(index.get(DBContract.ServiceNumEntry.COLUMN_STATUS)))
            .name(cursor.getString(index.get(DBContract.ServiceNumEntry.COLUMN_NAME)))
            .description(cursor.getString(index.get(DBContract.ServiceNumEntry.COLUMN_DESCRIPTION)))
            .avatarId(cursor.getString(index.get(DBContract.ServiceNumEntry.COLUMN_AVATAR_URL)))

            .enable(EnableType.valueOf(cursor.getString(index.get(DBContract.ServiceNumEntry.COLUMN_IS_ENABLE))))
            .isInSiteService(EnableType.valueOf(cursor.getString(index.get(DBContract.ServiceNumEntry.COLUMN_IS_IN_SITE_SERVICE))))
            .isOutSiteService(EnableType.valueOf(cursor.getString(index.get(DBContract.ServiceNumEntry.COLUMN_IS_OUT_SITE_SERVICE))))

            .serviceNumberOpenType(JsonHelper.getInstance().from(Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_SERVICE_OPEN_TYPE), new TypeToken<List<String>>() {
            }.getType()))
            .firstWelcomeMessage(cursor.getString(index.get(DBContract.ServiceNumEntry.COLUMN_FIRST_WELCOME_MESSAGE)))
            .eachWelcomeMessage(cursor.getString(index.get(DBContract.ServiceNumEntry.COLUMN_EACH_WELCOME_MESSAGE)))
            .intervalWelcomeMessage(cursor.getString(index.get(DBContract.ServiceNumEntry.COLUMN_INTERVAL_WELCOME_MESSAGE)))
            .internalSubscribeCount(cursor.getInt(index.get(DBContract.ServiceNumEntry.COLUMN_INTERNAL_SUBSCRIBE_COUNT)))
            .externalSubscribeCount(cursor.getInt(index.get(DBContract.ServiceNumEntry.COLUMN_EXTERNAL_SUBSCRIBE_COUNT)))
            .memberItems(JsonHelper.getInstance().from(cursor.getString(index.get(DBContract.ServiceNumEntry.COLUMN_MEMBER_ITEMS)), new TypeToken<List<Member>>() {
            }.getType()))
            .serviceMemberRoomId(cursor.getString(index.get(DBContract.ServiceNumEntry.COLUMN_SERVICE_MEMBER_ROOM_ID)))
            .updateTime(cursor.getInt(index.get(DBContract.ServiceNumEntry.COLUMN_UPDATE_TIME)));
        return build;
    }


    public static ServiceNumberEntity.ServiceNumberEntityBuilder formatByCursor(Cursor cursor) {
        ServiceNumberEntity.ServiceNumberEntityBuilder build = ServiceNumberEntity.Build()
            .serviceNumberId(Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID))
            .serviceNumberType(Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_TYPE))
            .roomId(Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_ROOM_ID))
            .broadcastRoomId(Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_BROADCAST_ROOM_ID))

            .isOwner("true".equals(Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_IS_OWNER)))
            .isManager("true".equals(Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_IS_MANAGER)))
            .isSubscribe("true".equals(Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_SUBSCRIBE)))
            .isCommon("true".equals(Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_IS_COMMON)))

            .name(Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_NAME))
            .description(Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_DESCRIPTION))
            .avatarId(Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_AVATAR_URL))
            .enable(EnableType.valueOf(Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_IS_ENABLE)))
            .isInSiteService(EnableType.valueOf(Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_IS_IN_SITE_SERVICE)))
            .isOutSiteService(EnableType.valueOf(Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_IS_OUT_SITE_SERVICE)))
            .status(Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_STATUS))
            .firstWelcomeMessage(Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_FIRST_WELCOME_MESSAGE))
            .eachWelcomeMessage(Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_EACH_WELCOME_MESSAGE))
            .intervalWelcomeMessage(Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_INTERVAL_WELCOME_MESSAGE))
            .internalSubscribeCount(Tools.getDbInt(cursor, DBContract.ServiceNumEntry.COLUMN_INTERNAL_SUBSCRIBE_COUNT))
            .externalSubscribeCount(Tools.getDbInt(cursor, DBContract.ServiceNumEntry.COLUMN_EXTERNAL_SUBSCRIBE_COUNT))
            .memberItems(JsonHelper.getInstance().from(Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_MEMBER_ITEMS), new TypeToken<List<Member>>() {
            }.getType()))
            .serviceNumberOpenType(JsonHelper.getInstance().from(Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_SERVICE_OPEN_TYPE), new TypeToken<List<String>>() {
            }.getType()))
            .serviceMemberRoomId(Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_SERVICE_MEMBER_ROOM_ID))
            .updateTime(Tools.getDbInt(cursor, DBContract.ServiceNumEntry.COLUMN_UPDATE_TIME))
            .serviceTimeoutTime(Tools.getDbInt(cursor, DBContract.ServiceNumEntry.COLUMN_SERVICE_TIMEOUT_TIME))
            .serviceIdleTime(Tools.getDbInt(cursor, DBContract.ServiceNumEntry.COLUMN_SERVICE_IDEL_TIME));

        return build;
    }

    public static ContentValues getContentValues(ServiceNumberEntity entity) {
        ContentValues values = new ContentValues();
        values.put(DBContract.ServiceNumEntry.COLUMN_ROOM_ID, entity.getServiceNumberId() + "_" + entity.getBroadcastRoomId());
        values.put(DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID, entity.getServiceNumberId());
        values.put(DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_TYPE, entity.getServiceNumberType());
        values.put(DBContract.ServiceNumEntry.COLUMN_BROADCAST_ROOM_ID, entity.getBroadcastRoomId());

        values.put(DBContract.ServiceNumEntry.COLUMN_IS_OWNER, String.valueOf(entity.isOwner()));
        values.put(DBContract.ServiceNumEntry.COLUMN_IS_MANAGER, String.valueOf(entity.isManager()));
        values.put(DBContract.ServiceNumEntry.COLUMN_OWNER_ID, entity.ownerId);
        values.put(DBContract.ServiceNumEntry.COLUMN_SUBSCRIBE, String.valueOf(entity.isSubscribe()));
        values.put(DBContract.ServiceNumEntry.COLUMN_IS_COMMON, String.valueOf(entity.isCommon()));

        values.put(DBContract.ServiceNumEntry.COLUMN_NAME, entity.getName());
        values.put(DBContract.ServiceNumEntry.COLUMN_AVATAR_URL, entity.getAvatarId());
        values.put(DBContract.ServiceNumEntry.COLUMN_STATUS, entity.getStatus());

        values.put(DBContract.ServiceNumEntry.COLUMN_IS_ENABLE, entity.enable.name());
        values.put(DBContract.ServiceNumEntry.COLUMN_IS_IN_SITE_SERVICE, entity.getIsInSiteService().name());
        values.put(DBContract.ServiceNumEntry.COLUMN_IS_OUT_SITE_SERVICE, entity.getIsOutSiteService().name());

        values.put(DBContract.ServiceNumEntry.COLUMN_FIRST_WELCOME_MESSAGE, entity.getFirstWelcomeMessage());
        values.put(DBContract.ServiceNumEntry.COLUMN_EACH_WELCOME_MESSAGE, entity.getEachWelcomeMessage());
        values.put(DBContract.ServiceNumEntry.COLUMN_INTERVAL_WELCOME_MESSAGE, entity.getIntervalWelcomeMessage());
        values.put(DBContract.ServiceNumEntry.COLUMN_SERVICE_OPEN_TYPE, JsonHelper.getInstance().toJson(entity.getServiceOpenType(), new TypeToken<List<String>>() {
        }.getType()));

        ServiceNumberStat stat = entity.getStat();
        values.put(DBContract.ServiceNumEntry.COLUMN_INTERNAL_SUBSCRIBE_COUNT, stat.getInternalSubscribeCount());
        values.put(DBContract.ServiceNumEntry.COLUMN_EXTERNAL_SUBSCRIBE_COUNT, stat.getExternalSubscribeCount());

        values.put(DBContract.ServiceNumEntry.COLUMN_SERVICE_MEMBER_ROOM_ID, entity.getServiceMemberRoomId());
        values.put(DBContract.ServiceNumEntry.COLUMN_MEMBER_ITEMS, JsonHelper.getInstance().toJson(entity.getMemberItems(), new TypeToken<List<Member>>() {
        }.getType()));

        values.put(DBContract.ServiceNumEntry.COLUMN_SERVICE_TIMEOUT_TIME, entity.getServiceTimeoutTime());
        values.put(DBContract.ServiceNumEntry.COLUMN_SERVICE_IDEL_TIME, entity.getServiceIdleTime());
        return values;
    }

    public String getServiceNumberId() {
        return this.serviceNumberId;
    }

    public String getAvatarId() {
        return this.avatarId;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String getRoomId() {
        return this.roomId;
    }

    public String getBroadcastRoomId() {
        return this.broadcastRoomId;
    }

    public String getServiceMemberRoomId() {
        return this.serviceMemberRoomId;
    }

    public boolean isTop() {
        return this.isTop;
    }

    public String getFirstWelcomeMessage() {
        return this.firstWelcomeMessage;
    }

    public String getEachWelcomeMessage() {
        return this.eachWelcomeMessage;
    }

    public String getIntervalWelcomeMessage() {
        return this.intervalWelcomeMessage;
    }

    public List<Member> getMemberItems() {
        return this.memberItems;
    }

    public boolean isOwner() {
        return this.isOwner;
    }

    public boolean isManager() {
        return this.isManager;
    }

    public boolean isSubscribe() {
        return this.isSubscribe;
    }

    public boolean isCommon() {
        return this.isCommon;
    }

    public String getStatus() {
        return this.status;
    }

    public EnableType getEnable() {
        return this.enable;
    }

    public EnableType getIsInSiteService() {
        return this.isInSiteService;
    }

    public EnableType getIsOutSiteService() {
        return this.isOutSiteService;
    }

    public int getInternalSubscribeCount() {
        return this.internalSubscribeCount;
    }

    public int getExternalSubscribeCount() {
        return this.externalSubscribeCount;
    }

    public Map getServiceNumberStat() {
        return this.serviceNumberStat;
    }

    public String getServiceNumberType() {
        return this.serviceNumberType;
    }

    public int getUnreadNumber() {
        return this.unreadNumber;
    }

    public void setServiceNumberId(String serviceNumberId) {
        this.serviceNumberId = serviceNumberId;
    }

    public void setAvatarId(String avatarId) {
        this.avatarId = avatarId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public void setBroadcastRoomId(String broadcastRoomId) {
        this.broadcastRoomId = broadcastRoomId;
    }

    public void setServiceMemberRoomId(String serviceMemberRoomId) {
        this.serviceMemberRoomId = serviceMemberRoomId;
    }

    public void setTop(boolean isTop) {
        this.isTop = isTop;
    }

    public void setFirstWelcomeMessage(String firstWelcomeMessage) {
        this.firstWelcomeMessage = firstWelcomeMessage;
    }

    public void setEachWelcomeMessage(String eachWelcomeMessage) {
        this.eachWelcomeMessage = eachWelcomeMessage;
    }

    public void setIntervalWelcomeMessage(String intervalWelcomeMessage) {
        this.intervalWelcomeMessage = intervalWelcomeMessage;
    }

    public void setMemberItems(List<Member> memberItems) {
        this.memberItems = memberItems;
    }

    public void setOwner(boolean isOwner) {
        this.isOwner = isOwner;
    }

    public void setManager(boolean isManager) {
        this.isManager = isManager;
    }

    public void setSubscribe(boolean isSubscribe) {
        this.isSubscribe = isSubscribe;
    }

    public void setCommon(boolean isCommon) {
        this.isCommon = isCommon;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setEnable(EnableType enable) {
        this.enable = enable;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public List<FacebookFansPages> getFaceBookFansPages() {
        return faceBookFansPages;
    }

    public void setFaceBookFansPages(List<FacebookFansPages> faceBookFansPages) {
        this.faceBookFansPages = faceBookFansPages;
    }

    public List<InstagramFansPages> getInstagramFansPages() {
        return instagramFansPages;
    }

    public void setInstagramFansPages(List<InstagramFansPages> instagramFansPages) {
        this.instagramFansPages = instagramFansPages;
    }

    public void setIsInSiteService(EnableType isInSiteService) {
        this.isInSiteService = isInSiteService;
    }

    public void setIsOutSiteService(EnableType isOutSiteService) {
        this.isOutSiteService = isOutSiteService;
    }

    public void setInternalSubscribeCount(int internalSubscribeCount) {
        this.internalSubscribeCount = internalSubscribeCount;
    }

    public void setExternalSubscribeCount(int externalSubscribeCount) {
        this.externalSubscribeCount = externalSubscribeCount;
    }

    public void setServiceNumberStat(Map serviceNumberStat) {
        this.serviceNumberStat = serviceNumberStat;
    }

    public void setServiceNumberType(String serviceNumberType) {
        this.serviceNumberType = serviceNumberType;
    }

    public void setUnreadNumber(int unreadNumber) {
        this.unreadNumber = unreadNumber;
    }

    public List<String> getServiceOpenType() {
        return serviceOpenType;
    }

    public void setServiceOpenType(List<String> serviceOpenType) {
        this.serviceOpenType = serviceOpenType;
    }

    public boolean isEnableConsultationAI() {
        return enableConsultationAI;
    }

    public boolean isRobotServiceFlag() {
        return robotServiceFlag;
    }

    public BusinessCardInfo getBusinessCardInfo() {
        return businessCardInfo;
    }

    public String getAllChannelURL() {
        return allChannelURL;
    }

    public void setAllChannelURL(String allChannelURL) {
        this.allChannelURL = allChannelURL;
    }

    public void setBusinessCardInfo(BusinessCardInfo businessCardInfo) {
        this.businessCardInfo = businessCardInfo;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof ServiceNumberEntity))
            return false;
        final ServiceNumberEntity other = (ServiceNumberEntity) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$serviceNumberId = this.getServiceNumberId();
        final Object other$serviceNumberId = other.getServiceNumberId();
        if (this$serviceNumberId == null ? other$serviceNumberId != null : !this$serviceNumberId.equals(other$serviceNumberId))
            return false;
        final Object this$avatarId = this.getAvatarId();
        final Object other$avatarId = other.getAvatarId();
        if (this$avatarId == null ? other$avatarId != null : !this$avatarId.equals(other$avatarId))
            return false;
        final Object this$name = this.getName();
        final Object other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
        final Object this$description = this.getDescription();
        final Object other$description = other.getDescription();
        if (this$description == null ? other$description != null : !this$description.equals(other$description))
            return false;
        final Object this$roomId = this.getRoomId();
        final Object other$roomId = other.getRoomId();
        if (this$roomId == null ? other$roomId != null : !this$roomId.equals(other$roomId))
            return false;
        final Object this$broadcastRoomId = this.getBroadcastRoomId();
        final Object other$broadcastRoomId = other.getBroadcastRoomId();
        if (this$broadcastRoomId == null ? other$broadcastRoomId != null : !this$broadcastRoomId.equals(other$broadcastRoomId))
            return false;
        final Object this$serviceMemberRoomId = this.getServiceMemberRoomId();
        final Object other$serviceMemberRoomId = other.getServiceMemberRoomId();
        if (this$serviceMemberRoomId == null ? other$serviceMemberRoomId != null : !this$serviceMemberRoomId.equals(other$serviceMemberRoomId))
            return false;
        if (this.isTop() != other.isTop()) return false;
        final Object this$firstWelcomeMessage = this.getFirstWelcomeMessage();
        final Object other$firstWelcomeMessage = other.getFirstWelcomeMessage();
        if (this$firstWelcomeMessage == null ? other$firstWelcomeMessage != null : !this$firstWelcomeMessage.equals(other$firstWelcomeMessage))
            return false;
        final Object this$eachWelcomeMessage = this.getEachWelcomeMessage();
        final Object other$eachWelcomeMessage = other.getEachWelcomeMessage();
        if (this$eachWelcomeMessage == null ? other$eachWelcomeMessage != null : !this$eachWelcomeMessage.equals(other$eachWelcomeMessage))
            return false;
        final Object this$intervalWelcomeMessage = this.getIntervalWelcomeMessage();
        final Object other$intervalWelcomeMessage = other.getIntervalWelcomeMessage();
        if (this$intervalWelcomeMessage == null ? other$intervalWelcomeMessage != null : !this$intervalWelcomeMessage.equals(other$intervalWelcomeMessage))
            return false;
        final Object this$memberItems = this.getMemberItems();
        final Object other$memberItems = other.getMemberItems();
        if (this$memberItems == null ? other$memberItems != null : !this$memberItems.equals(other$memberItems))
            return false;
        if (this.isOwner() != other.isOwner()) return false;
        if (this.isManager() != other.isManager()) return false;
        if (this.isSubscribe() != other.isSubscribe()) return false;
        if (this.isCommon() != other.isCommon()) return false;
        final Object this$status = this.getStatus();
        final Object other$status = other.getStatus();
        if (this$status == null ? other$status != null : !this$status.equals(other$status))
            return false;
        final Object this$enable = this.getEnable();
        final Object other$enable = other.getEnable();
        if (this$enable == null ? other$enable != null : !this$enable.equals(other$enable))
            return false;
        final Object this$isInSiteService = this.getIsInSiteService();
        final Object other$isInSiteService = other.getIsInSiteService();
        if (this$isInSiteService == null ? other$isInSiteService != null : !this$isInSiteService.equals(other$isInSiteService))
            return false;
        final Object this$isOutSiteService = this.getIsOutSiteService();
        final Object other$isOutSiteService = other.getIsOutSiteService();
        if (this$isOutSiteService == null ? other$isOutSiteService != null : !this$isOutSiteService.equals(other$isOutSiteService))
            return false;
        if (this.getInternalSubscribeCount() != other.getInternalSubscribeCount()) return false;
        if (this.getExternalSubscribeCount() != other.getExternalSubscribeCount()) return false;
        final Object this$serviceNumberStat = this.getServiceNumberStat();
        final Object other$serviceNumberStat = other.getServiceNumberStat();
        if (this$serviceNumberStat == null ? other$serviceNumberStat != null : !this$serviceNumberStat.equals(other$serviceNumberStat))
            return false;
        final Object this$serviceNumberType = this.getServiceNumberType();
        final Object other$serviceNumberType = other.getServiceNumberType();
        if (this$serviceNumberType == null ? other$serviceNumberType != null : !this$serviceNumberType.equals(other$serviceNumberType))
            return false;
        return this.getUnreadNumber() == other.getUnreadNumber();
    }

    protected boolean canEqual(final Object other) {
        return other instanceof ServiceNumberEntity;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $serviceNumberId = this.getServiceNumberId();
        result = result * PRIME + ($serviceNumberId == null ? 43 : $serviceNumberId.hashCode());
        final Object $avatarId = this.getAvatarId();
        result = result * PRIME + ($avatarId == null ? 43 : $avatarId.hashCode());
        final Object $name = this.getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        final Object $description = this.getDescription();
        result = result * PRIME + ($description == null ? 43 : $description.hashCode());
        final Object $roomId = this.getRoomId();
        result = result * PRIME + ($roomId == null ? 43 : $roomId.hashCode());
        final Object $broadcastRoomId = this.getBroadcastRoomId();
        result = result * PRIME + ($broadcastRoomId == null ? 43 : $broadcastRoomId.hashCode());
        final Object $serviceMemberRoomId = this.getServiceMemberRoomId();
        result = result * PRIME + ($serviceMemberRoomId == null ? 43 : $serviceMemberRoomId.hashCode());
        result = result * PRIME + (this.isTop() ? 79 : 97);
        final Object $firstWelcomeMessage = this.getFirstWelcomeMessage();
        result = result * PRIME + ($firstWelcomeMessage == null ? 43 : $firstWelcomeMessage.hashCode());
        final Object $eachWelcomeMessage = this.getEachWelcomeMessage();
        result = result * PRIME + ($eachWelcomeMessage == null ? 43 : $eachWelcomeMessage.hashCode());
        final Object $intervalWelcomeMessage = this.getIntervalWelcomeMessage();
        result = result * PRIME + ($intervalWelcomeMessage == null ? 43 : $intervalWelcomeMessage.hashCode());
        final Object $memberItems = this.getMemberItems();
        result = result * PRIME + ($memberItems == null ? 43 : $memberItems.hashCode());
        result = result * PRIME + (this.isOwner() ? 79 : 97);
        result = result * PRIME + (this.isManager() ? 79 : 97);
        result = result * PRIME + (this.isSubscribe() ? 79 : 97);
        result = result * PRIME + (this.isCommon() ? 79 : 97);
        final Object $status = this.getStatus();
        result = result * PRIME + ($status == null ? 43 : $status.hashCode());
        final Object $enable = this.getEnable();
        result = result * PRIME + ($enable == null ? 43 : $enable.hashCode());
        final Object $isInSiteService = this.getIsInSiteService();
        result = result * PRIME + ($isInSiteService == null ? 43 : $isInSiteService.hashCode());
        final Object $isOutSiteService = this.getIsOutSiteService();
        result = result * PRIME + ($isOutSiteService == null ? 43 : $isOutSiteService.hashCode());
        result = result * PRIME + this.getInternalSubscribeCount();
        result = result * PRIME + this.getExternalSubscribeCount();
        final Object $serviceNumberStat = this.getServiceNumberStat();
        result = result * PRIME + ($serviceNumberStat == null ? 43 : $serviceNumberStat.hashCode());
        final Object $serviceNumberType = this.getServiceNumberType();
        result = result * PRIME + ($serviceNumberType == null ? 43 : $serviceNumberType.hashCode());
        result = result * PRIME + this.getUnreadNumber();
        return result;
    }

    @NonNull
    public String toString() {
        return "ServiceNumberEntity(serviceNumberId=" + this.getServiceNumberId() + ", avatarId=" + this.getAvatarId() + ", name=" + this.getName() + ", description=" + this.getDescription() + ", roomId=" + this.getRoomId() + ", broadcastRoomId=" + this.getBroadcastRoomId() + ", serviceMemberRoomId=" + this.getServiceMemberRoomId() + ", isTop=" + this.isTop() + ", firstWelcomeMessage=" + this.getFirstWelcomeMessage() + ", eachWelcomeMessage=" + this.getEachWelcomeMessage() + ", intervalWelcomeMessage=" + this.getIntervalWelcomeMessage() + ", memberItems=" + this.getMemberItems() + ", isOwner=" + this.isOwner() + ", isManager=" + this.isManager() + ", isSubscribe=" + this.isSubscribe() + ", isCommon=" + this.isCommon() + ", status=" + this.getStatus() + ", enable=" + this.getEnable() + ", isInSiteService=" + this.getIsInSiteService() + ", isOutSiteService=" + this.getIsOutSiteService() + ", internalSubscribeCount=" + this.getInternalSubscribeCount() + ", externalSubscribeCount=" + this.getExternalSubscribeCount() + ", serviceNumberStat=" + this.getServiceNumberStat() + ", serviceNumberType=" + this.getServiceNumberType() + ", unreadNumber=" + this.getUnreadNumber() + ")";
    }

    public ServiceNumberEntityBuilder toBuilder() {
        return new ServiceNumberEntityBuilder().serviceNumberId(this.serviceNumberId).avatarId(this.avatarId).name(this.name).description(this.description).roomId(this.roomId).broadcastRoomId(this.broadcastRoomId).serviceMemberRoomId(this.serviceMemberRoomId).isTop(this.isTop).firstWelcomeMessage(this.firstWelcomeMessage).eachWelcomeMessage(this.eachWelcomeMessage).intervalWelcomeMessage(this.intervalWelcomeMessage).memberItems(this.memberItems).isOwner(this.isOwner).isManager(this.isManager).isSubscribe(this.isSubscribe).isCommon(this.isCommon).status(this.status).enable(this.enable).isInSiteService(this.isInSiteService).isOutSiteService(this.isOutSiteService).internalSubscribeCount(this.internalSubscribeCount).externalSubscribeCount(this.externalSubscribeCount).serviceNumberStat(this.serviceNumberStat).serviceNumberType(this.serviceNumberType).unreadNumber(this.unreadNumber);
    }

    public static class ServiceNumberEntityBuilder {
        private String serviceNumberId;
        private String avatarId;
        private String name;
        private String description;
        private String roomId;
        private String broadcastRoomId;
        private String serviceMemberRoomId;
        private boolean isTop;
        private String firstWelcomeMessage$value;
        private boolean firstWelcomeMessage$set;
        private String eachWelcomeMessage$value;
        private boolean eachWelcomeMessage$set;
        private String intervalWelcomeMessage$value;
        private boolean intervalWelcomeMessage$set;
        private List<Member> memberItems$value;
        private boolean memberItems$set;
        private boolean isOwner;
        private boolean isManager;
        private boolean isSubscribe;
        private boolean isCommon;
        private String status;
        private EnableType enable$value;
        private boolean enable$set;
        private EnableType isInSiteService$value;
        private boolean isInSiteService$set;
        private EnableType isOutSiteService$value;
        private boolean isOutSiteService$set;
        private int internalSubscribeCount;
        private int externalSubscribeCount;
        private Map serviceNumberStat;
        private String serviceNumberType;
        private int unreadNumber;

        private List<String> serviceOpenType;
        private boolean enableConsultationAI;
        private boolean robotServiceFlag;
        private List<FacebookFansPages> facebookFansPages;
        private int updateTime;
        private int serviceTimeoutTime;
        private int serviceIdleTime;

        public ServiceNumberEntityBuilder enableConsultationAI(boolean enableConsultationAI) {
            this.enableConsultationAI = enableConsultationAI;
            return this;
        }

        public ServiceNumberEntityBuilder robotServiceFlag(boolean robotServiceFlag) {
            this.robotServiceFlag = robotServiceFlag;
            return this;
        }

        public ServiceNumberEntityBuilder serviceNumberId(String serviceNumberId) {
            this.serviceNumberId = serviceNumberId;
            return this;
        }

        public ServiceNumberEntityBuilder avatarId(String avatarId) {
            this.avatarId = avatarId;
            return this;
        }

        public ServiceNumberEntityBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ServiceNumberEntityBuilder description(String description) {
            this.description = description;
            return this;
        }

        public ServiceNumberEntityBuilder roomId(String roomId) {
            this.roomId = roomId;
            return this;
        }

        public ServiceNumberEntityBuilder broadcastRoomId(String broadcastRoomId) {
            this.broadcastRoomId = broadcastRoomId;
            return this;
        }

        public ServiceNumberEntityBuilder serviceMemberRoomId(String serviceMemberRoomId) {
            this.serviceMemberRoomId = serviceMemberRoomId;
            return this;
        }

        public ServiceNumberEntityBuilder isTop(boolean isTop) {
            this.isTop = isTop;
            return this;
        }

        public ServiceNumberEntityBuilder firstWelcomeMessage(String firstWelcomeMessage) {
            this.firstWelcomeMessage$value = firstWelcomeMessage;
            this.firstWelcomeMessage$set = true;
            return this;
        }

        public ServiceNumberEntityBuilder eachWelcomeMessage(String eachWelcomeMessage) {
            this.eachWelcomeMessage$value = eachWelcomeMessage;
            this.eachWelcomeMessage$set = true;
            return this;
        }

        public ServiceNumberEntityBuilder intervalWelcomeMessage(String intervalWelcomeMessage) {
            this.intervalWelcomeMessage$value = intervalWelcomeMessage;
            this.intervalWelcomeMessage$set = true;
            return this;
        }

        public ServiceNumberEntityBuilder memberItems(List<Member> memberItems) {
            this.memberItems$value = memberItems;
            this.memberItems$set = true;
            return this;
        }

        public ServiceNumberEntityBuilder isOwner(boolean isOwner) {
            this.isOwner = isOwner;
            return this;
        }

        public ServiceNumberEntityBuilder isManager(boolean isManager) {
            this.isManager = isManager;
            return this;
        }

        public ServiceNumberEntityBuilder isSubscribe(boolean isSubscribe) {
            this.isSubscribe = isSubscribe;
            return this;
        }

        public ServiceNumberEntityBuilder isCommon(boolean isCommon) {
            this.isCommon = isCommon;
            return this;
        }

        public ServiceNumberEntityBuilder status(String status) {
            this.status = status;
            return this;
        }

        public ServiceNumberEntityBuilder enable(EnableType enable) {
            this.enable$value = enable;
            this.enable$set = true;
            return this;
        }

        public ServiceNumberEntityBuilder isInSiteService(EnableType isInSiteService) {
            this.isInSiteService$value = isInSiteService;
            this.isInSiteService$set = true;
            return this;
        }

        public ServiceNumberEntityBuilder isOutSiteService(EnableType isOutSiteService) {
            this.isOutSiteService$value = isOutSiteService;
            this.isOutSiteService$set = true;
            return this;
        }

        public ServiceNumberEntityBuilder internalSubscribeCount(int internalSubscribeCount) {
            this.internalSubscribeCount = internalSubscribeCount;
            return this;
        }

        public ServiceNumberEntityBuilder externalSubscribeCount(int externalSubscribeCount) {
            this.externalSubscribeCount = externalSubscribeCount;
            return this;
        }

        public ServiceNumberEntityBuilder serviceNumberStat(Map serviceNumberStat) {
            this.serviceNumberStat = serviceNumberStat;
            return this;
        }

        public ServiceNumberEntityBuilder serviceNumberType(String serviceNumberType) {
            this.serviceNumberType = serviceNumberType;
            return this;
        }

        public ServiceNumberEntityBuilder unreadNumber(int unreadNumber) {
            this.unreadNumber = unreadNumber;
            return this;
        }

        public ServiceNumberEntityBuilder serviceNumberOpenType(List<String> serviceOpenType) {
            this.serviceOpenType = serviceOpenType;
            return this;
        }

        public ServiceNumberEntityBuilder facebookFansPages(List<FacebookFansPages> facebookFansPages) {
            this.facebookFansPages = facebookFansPages;
            return this;
        }

        public ServiceNumberEntityBuilder updateTime(int updateTime) {
            this.updateTime = updateTime;
            return this;
        }

        public ServiceNumberEntityBuilder serviceTimeoutTime(int serviceTimeoutTime) {
            this.serviceTimeoutTime = serviceTimeoutTime;
            return this;
        }

        public ServiceNumberEntityBuilder serviceIdleTime(int serviceIdleTime) {
            this.serviceIdleTime = serviceIdleTime;
            return this;
        }

        public ServiceNumberEntity build() {
            String firstWelcomeMessage$value = this.firstWelcomeMessage$value;
            if (!this.firstWelcomeMessage$set) {
                firstWelcomeMessage$value = ServiceNumberEntity.$default$firstWelcomeMessage();
            }
            String eachWelcomeMessage$value = this.eachWelcomeMessage$value;
            if (!this.eachWelcomeMessage$set) {
                eachWelcomeMessage$value = ServiceNumberEntity.$default$eachWelcomeMessage();
            }
            String intervalWelcomeMessage$value = this.intervalWelcomeMessage$value;
            if (!this.intervalWelcomeMessage$set) {
                intervalWelcomeMessage$value = ServiceNumberEntity.$default$intervalWelcomeMessage();
            }
            List<Member> memberItems$value = this.memberItems$value;
            if (!this.memberItems$set) {
                memberItems$value = ServiceNumberEntity.$default$memberItems();
            }
            EnableType enable$value = this.enable$value;
            if (!this.enable$set) {
                enable$value = ServiceNumberEntity.$default$enable();
            }
            EnableType isInSiteService$value = this.isInSiteService$value;
            if (!this.isInSiteService$set) {
                isInSiteService$value = ServiceNumberEntity.$default$isInSiteService();
            }
            EnableType isOutSiteService$value = this.isOutSiteService$value;
            if (!this.isOutSiteService$set) {
                isOutSiteService$value = ServiceNumberEntity.$default$isOutSiteService();
            }
            return new ServiceNumberEntity(serviceNumberId, avatarId, name, description, roomId, broadcastRoomId, serviceMemberRoomId, isTop, firstWelcomeMessage$value, eachWelcomeMessage$value, intervalWelcomeMessage$value, memberItems$value, isOwner, isManager, isSubscribe, isCommon, status, enable$value, isInSiteService$value, isOutSiteService$value, internalSubscribeCount, externalSubscribeCount, serviceNumberStat, serviceNumberType, unreadNumber, serviceOpenType, enableConsultationAI, robotServiceFlag, facebookFansPages, serviceTimeoutTime, serviceIdleTime);
        }

        @NonNull
        public String toString() {
            return "ServiceNumberEntity.ServiceNumberEntityBuilder(serviceNumberId=" + this.serviceNumberId + ", avatarId=" + this.avatarId + ", name=" + this.name + ", description=" + this.description + ", roomId=" + this.roomId + ", broadcastRoomId=" + this.broadcastRoomId + ", serviceMemberRoomId=" + this.serviceMemberRoomId + ", isTop=" + this.isTop + ", firstWelcomeMessage$value=" + this.firstWelcomeMessage$value + ", eachWelcomeMessage$value=" + this.eachWelcomeMessage$value + ", intervalWelcomeMessage$value=" + this.intervalWelcomeMessage$value + ", memberItems$value=" + this.memberItems$value + ", isOwner=" + this.isOwner + ", isManager=" + this.isManager + ", isSubscribe=" + this.isSubscribe + ", isCommon=" + this.isCommon + ", status=" + this.status + ", enable$value=" + this.enable$value + ", isInSiteService$value=" + this.isInSiteService$value + ", isOutSiteService$value=" + this.isOutSiteService$value + ", internalSubscribeCount=" + this.internalSubscribeCount + ", externalSubscribeCount=" + this.externalSubscribeCount + ", serviceNumberStat=" + this.serviceNumberStat + ", serviceNumberType=" + this.serviceNumberType + ", unreadNumber=" + this.unreadNumber + ")";
        }
    }
}
