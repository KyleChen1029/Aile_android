//package tw.com.chainsea.ce.sdk.bean.msg;
//
//import static tw.com.chainsea.ce.sdk.service.ChatRoomService.getAtContent;
//
//import android.content.ContentValues;
//import android.database.Cursor;
//
//import com.google.common.base.Strings;
//import com.google.common.collect.ComparisonChain;
//import com.google.common.collect.Lists;
//import com.google.common.collect.Sets;
//import com.google.gson.annotations.SerializedName;
//
//import org.json.JSONObject;
//
//import java.io.Serializable;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import tw.com.chainsea.android.common.json.JsonHelper;
//import tw.com.chainsea.ce.sdk.bean.broadcast.TopicEntity;
//import tw.com.chainsea.ce.sdk.bean.common.EnableType;
//import tw.com.chainsea.ce.sdk.bean.msg.content.IMessageContent;
//import tw.com.chainsea.ce.sdk.bean.msg.content.UndefContent;
//import tw.com.chainsea.ce.sdk.database.DBContract;
//
///**
// * MessageEntity
// * Created by 90Chris on 2016/4/19.
// */
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder(builderMethodName = "Build", toBuilder = true)
//public class MessageEntity implements Serializable, Comparable<MessageEntity> {
//    private static final long serialVersionUID = 254829272788445833L;
//
//    String id; //*訊息 Id
//    String roomId; //*聊天室 Id
//    String senderId; //*訊息發送者 Id
//    String senderName; //*訊息發送者的名稱
//    @SerializedName("type")
//    @Builder.Default
//    MessageType type = MessageType.UNDEF; //*Server是String, 訊息類型
//    @SerializedName("sourceType")
//    @Builder.Default
//    SourceType sourceType = SourceType.UNDEF; //*該訊息的狀態，分為：user | system|Login
//    int sequence; //每則訊息所帶序號，會是連續號碼
//    String msgSrc; //*(補)如果為forward訊息會塞入前一筆的來源id
//    String avatarId;
//    @SerializedName("content")
//    String content; //*訊息內容
//    long sendTime; //*訊息發送時間
//    @SerializedName("flag")
//    @Builder.Default
//    MessageFlag flag = MessageFlag.UNDEF; //*Server是String, 訊息狀態，分為：-1（當前登入者發的）| 0 | 1（已到）| 2（已讀）| 3（收回）
//    int readedNum; //*訊息已讀取數量
//    int sendNum; //*訊息發送對象總數
//    int receivedNum; //*訊息已到達對方載具總數
//    String osType;
//    String deviceType;
//    @SerializedName("from")
//    @Builder.Default
//    ChannelType from = ChannelType.UNDEF; //*Server是String, 訊息來源，分為：line | facebook | ce (外部服務號進線會填入)
//    @Builder.Default
//    private String previousMessageId = "";
//
//    // extra
//    @Builder.Default
//    String ringName = "";
//
//    // theme
//    String themeId;
//    String nearMessageAvatarId;
//    String nearMessageSenderId;
//    String nearMessageId;
//    @SerializedName("nearMessageType")
//    @Builder.Default
//    MessageType nearMessageType = MessageType.UNDEF;
//    @SerializedName("nearMessageContent")
//    String nearMessageContent;
//    String nearMessageSenderName;
//
//    // local control
//    @Builder.Default
//    private MessageStatus status = MessageStatus.IS_REMOTE;
//
//    private EnableType enable;
//
//    @Builder.Default
//    private BroadcastFlag broadcastFlag = BroadcastFlag.BOOKING;
//    @Builder.Default
//    private List<TopicEntity> topicArray = Lists.newArrayList();
//    private long broadcastTime;
//    private String createUserId;
//    private String updateUserId;
//
//    // ui control
//    private boolean isDelete;
//    private boolean isShowChecked;
//    private boolean isForward;
//    private boolean isShowSelection;
//    private boolean isAnimator;
//
//    // socket io on('message/Message.New') this is an unread amount form room
//    private int unReadNum;
//
//    String chatId;
//    String tag;
//    /**
//     * checking can editable
//     * 狀態 BroadcastFlag == BOOKING && (
//     * 沒有 Message Id or
//     * local message status contains ERROR, FAILED or
//     * broadcastTime > 0 && broadcastTime > now
//     */
//    public boolean isCanEditBroadcast() {
//        if (BroadcastFlag.BOOKING.equals(broadcastFlag)) {
//            if (Strings.isNullOrEmpty(id)) {
//                return true;
//            } else {
//                if (MessageStatus.FAILED_or_ERROR.contains(status)) {
//                    return true;
//                }
//
//                long now = System.currentTimeMillis();
//                return broadcastTime > 0 && broadcastTime > now;
//            }
//        }
//        return false;
//    }
//
//    public CharSequence getFormatContent(){
//        if(type == null) return "";
//        switch (type){
//            case AT: return getAtContent(content);
//            case VIDEO: return "[視頻]";
//            case LOCATION: return "[位置]";
//            case BUSINESS: return "[物件]";
//            case FILE: return "[文件]";
//            case VOICE: return "[語音]";
//            case IMAGE: return "[圖片]";
//            case AD: return "[廣告]";
//            case STICKER: return "[表情]";
//            case TEXT: return getContent(content);
//            default:
//                return "[未知訊息格式]";
//        }
//    }
//
//    public static String getContent(String input) {
//        String parseStr = "";
//        try {
//            JSONObject jsonObject = JsonHelper.toJsonObject(input);
//            parseStr = jsonObject.getString("text");
//        }catch (Exception e){
//            parseStr = input;
//        }
//        return parseStr;
//    }
//
//    public boolean isCanEditBroadcastAndChackType() {
//        if (BroadcastFlag.BOOKING.equals(broadcastFlag)) {
//            if (Strings.isNullOrEmpty(id)) {
//                return true;
//            } else {
//                if (MessageStatus.FAILED_or_ERROR.contains(status)) {
//                    return true;
//                }
//
//                if (!MessageType.TEXT.equals(this.type)) {
//                    return false;
//                }
//
//                long now = System.currentTimeMillis();
//                return broadcastTime > 0 && broadcastTime > now;
//            }
//        }
//        return false;
//    }
//
//    public boolean isCanDeleteBroadcast() {
//        if (BroadcastFlag.BOOKING.equals(broadcastFlag)) {
//            if (Strings.isNullOrEmpty(id)) {
//                return false;
//            }
//
//            if (MessageStatus.FAILED_or_ERROR.contains(status)) {
//                return true;
//            }
//
//            long now = System.currentTimeMillis();
//            return broadcastTime > 0 && broadcastTime > now;
//        }
//        return false;
//    }
//
//    public IMessageContent<MessageType> content() {
//        if (type == null) {
//            return new UndefContent();
//        }
//        return type.from(content);
//    }
//
//    public IMessageContent<MessageType> nearMessageContent() {
//        if (nearMessageType == null) {
//            return new UndefContent();
//        }
//        return nearMessageType.from(nearMessageContent);
//    }
//
//    @Override
//    public int compareTo(MessageEntity o) {
//        return ComparisonChain.start()
//                .compare(this.getSendTime(), o.getSendTime() + 0.5)
//                .result();
//    }
//
//    public double getBroadcastWeights() {
//        double weight = 0.0d;
//        weight += MessageType.BROADCAST.equals(this.type) ? 2.0d : 0.0d;
//        weight += BroadcastFlag.BOOKING.equals(this.broadcastFlag) ? 1.0d : 0.0d;
//        return weight;
//    }
//
//    @Override
//    public int hashCode() {
//        final int prime = 31;
//        int result = 1;
//        result = prime * result + ((id == null) ? 0 : id.hashCode());
//        return result;
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj)
//            return true;
//        if (obj == null)
//            return false;
//        if (getClass() != obj.getClass())
//            return false;
//        MessageEntity other = (MessageEntity) obj;
//        if (this.id == null || other.getId() == null) {
//            return false;
//        } else return this.id.equals(other.getId());
//    }
//
//    // local data
//    public String[] getTopicIds() {
//        Set<String> idset = Sets.newHashSet();
//        for (TopicEntity t : this.topicArray) {
//            if (!t.isHardCode()) {
//                idset.add(t.getId());
//            }
//
//        }
//        return idset.toArray(new String[idset.size()]);
//    }
//
//    public static ContentValues getContentValues(String roomId, MessageEntity entity) {
//
//        ContentValues values = new ContentValues();
//        values.put(DBContract.MessageEntry._ID, entity.getId());
//        values.put(DBContract.MessageEntry.COLUMN_ROOM_ID, roomId);
//        values.put(DBContract.MessageEntry.COLUMN_PREVIOUS_MESSAGE_ID, entity.getPreviousMessageId());
//
//        values.put(DBContract.MessageEntry.COLUMN_CONTENT, Strings.isNullOrEmpty(entity.getContent()) ? "" : entity.getContent());
//        values.put(DBContract.MessageEntry.COLUMN_SENDER_ID, Strings.isNullOrEmpty(entity.getSenderId()) ? "" : entity.getSenderId());
//        values.put(DBContract.MessageEntry.COLUMN_AVATAR_ID, Strings.isNullOrEmpty(entity.getAvatarId()) ? "" : entity.getAvatarId());
//
//        values.put(DBContract.MessageEntry.COLUMN_SENDER_NAME, Strings.isNullOrEmpty(entity.getSenderName()) ? "" : entity.getSenderName());
//        values.put(DBContract.MessageEntry.COLUMN_SOURCE_TYPE, entity.getSourceType() == null ? SourceType.USER.name() : entity.getSourceType().name());
//        values.put(DBContract.MessageEntry.COLUMN_SEQUENCE, entity.getSequence());
//        values.put(DBContract.MessageEntry.COLUMN_FROM, entity.getFrom() == null ? ChannelType.UNDEF.getValue() : entity.getFrom().getValue());
//        values.put(DBContract.MessageEntry.COLUMN_TYPE, entity.getType() == null ? MessageType.UNDEF.getValue() : entity.getType().getValue());
//        values.put(DBContract.MessageEntry.COLUMN_OS_TYPE, Strings.isNullOrEmpty(entity.getOsType()) ? "" : entity.getOsType());
//        values.put(DBContract.MessageEntry.COLUMN_DEVICE_TYPE, Strings.isNullOrEmpty(entity.getDeviceType()) ? "" : entity.getDeviceType());
//
//        MessageFlag flag = entity.getFlag() == null ? MessageFlag.OWNER : entity.getFlag();
//        values.put(DBContract.MessageEntry.COLUMN_FLAG, flag.getFlag());
//        values.put(DBContract.MessageEntry.COLUMN_SEND_TIME, entity.getSendTime());
//        values.put(DBContract.MessageEntry.COLUMN_SEND_NUM, entity.getSendNum());
//        values.put(DBContract.MessageEntry.COLUMN_RECEIVED_NUM, entity.getReceivedNum());
//        values.put(DBContract.MessageEntry.COLUMN_READED_NUM, entity.getReadedNum());
//
//        values.put(DBContract.MessageEntry.COLUMN_CREATE_USER_ID, entity.getCreateUserId());
//        values.put(DBContract.MessageEntry.COLUMN_UPDATE_USER_ID, entity.getUpdateUserId());
//        values.put(DBContract.MessageEntry.COLUMN_BROADCAST_TIME, entity.getBroadcastTime());
//        values.put(DBContract.MessageEntry.COLUMN_BROADCAST_FLAG, (entity.getBroadcastFlag() != null) ?entity.getBroadcastFlag().name() : "null");
//
//        // Theme
//        if (!Strings.isNullOrEmpty(entity.getNearMessageContent()) && !Strings.isNullOrEmpty(entity.getThemeId())) {
//            values.put(DBContract.MessageEntry.COLUMN_THEME_ID, Strings.isNullOrEmpty(entity.getThemeId()) ? "" : entity.getThemeId());
//            values.put(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_ID, Strings.isNullOrEmpty(entity.getNearMessageId()) ? "" : entity.getNearMessageId());
//            values.put(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_SEND_ID, Strings.isNullOrEmpty(entity.getNearMessageSenderId()) ? "" : entity.getNearMessageSenderId());
//            values.put(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_SEND_NAME, Strings.isNullOrEmpty(entity.getNearMessageSenderName()) ? "" : entity.getNearMessageSenderName());
//            values.put(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_AVATAR_ID, Strings.isNullOrEmpty(entity.getNearMessageAvatarId()) ? "" : entity.getNearMessageAvatarId());
//            values.put(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_CONTENT, Strings.isNullOrEmpty(entity.getNearMessageContent()) ? "" : entity.getNearMessageContent());
//            values.put(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_TYPE, entity.getNearMessageType() == null ? MessageType.UNDEF.getValue() : entity.getNearMessageType().getValue());
//        } else {
//            values.put(DBContract.MessageEntry.COLUMN_THEME_ID, "");
//            values.put(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_ID, "");
//            values.put(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_SEND_ID, "");
//            values.put(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_SEND_NAME, "");
//            values.put(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_AVATAR_ID, "");
//            values.put(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_CONTENT, "");
//            values.put(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_TYPE, MessageType.UNDEF.getValue());
//        }
//
//        values.put(DBContract.MessageEntry.COLUMN_STATUS, entity.getStatus() == null ? MessageStatus.SENDING.getValue() : entity.getStatus().getValue());
//        return values;
//    }
//
//    public static MessageEntity.MessageEntityBuilder formatByCursor(Cursor cursor) {
//
//        MessageEntity.MessageEntityBuilder builder = MessageEntity.Build()
//                .id(Tools.getDbString(cursor, DBContract.MessageEntry._ID))
//                .previousMessageId(Tools.getDbString(cursor, DBContract.MessageEntry.COLUMN_PREVIOUS_MESSAGE_ID))
//                .roomId(Tools.getDbString(cursor, DBContract.MessageEntry.COLUMN_ROOM_ID))
//                .content(Tools.getDbString(cursor, DBContract.MessageEntry.COLUMN_CONTENT))
//                .sendTime(Tools.getDbLong(cursor, DBContract.MessageEntry.COLUMN_SEND_TIME))
//                .sendNum(Tools.getDbInt(cursor, DBContract.MessageEntry.COLUMN_SEND_NUM))
//                .receivedNum(Tools.getDbInt(cursor, DBContract.MessageEntry.COLUMN_RECEIVED_NUM))
//                .readedNum(Tools.getDbInt(cursor, DBContract.MessageEntry.COLUMN_READED_NUM))
//                .flag(MessageFlag.of(Tools.getDbInt(cursor, DBContract.MessageEntry.COLUMN_FLAG)))
//                .type(MessageType.of(Tools.getDbString(cursor, DBContract.MessageEntry.COLUMN_TYPE)))
//                .senderId(Tools.getDbString(cursor, DBContract.MessageEntry.COLUMN_SENDER_ID))
//                .senderName(Tools.getDbString(cursor, DBContract.MessageEntry.COLUMN_SENDER_NAME))
//                .avatarId(Tools.getDbString(cursor, DBContract.MessageEntry.COLUMN_AVATAR_ID))
//                .sourceType(SourceType.of(Tools.getDbString(cursor, DBContract.MessageEntry.COLUMN_SOURCE_TYPE)))
//                .from(ChannelType.of(Tools.getDbString(cursor, DBContract.MessageEntry.COLUMN_FROM)))
//                .osType(Tools.getDbString(cursor, DBContract.MessageEntry.COLUMN_OS_TYPE))
//                .deviceType(Tools.getDbString(cursor, DBContract.MessageEntry.COLUMN_DEVICE_TYPE));
//
//
//        if (Tools.checkIndex(cursor, DBContract.UserProfileEntry.COLUMN_ALIAS) != -1) {
//            String alias = Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_ALIAS);
//            if (!Strings.isNullOrEmpty(alias)) {
//                builder.senderName(Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_ALIAS));
//            }
//        }
//
//
//        MessageStatus status = MessageStatus.of(Tools.getDbInt(cursor, DBContract.MessageEntry.COLUMN_STATUS));
//        if (MessageStatus.SENDING.equals(status)) {
//            builder.status(MessageStatus.ERROR);
//        } else {
//            builder.status(status);
//        }
//
//        String themeId = Tools.getDbString(cursor, DBContract.MessageEntry.COLUMN_THEME_ID);
//        String nearMessageContent = Tools.getDbString(cursor, DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_CONTENT);
//        if (!Strings.isNullOrEmpty(themeId) && !Strings.isNullOrEmpty(nearMessageContent)) {
//            builder.themeId(themeId)
//                    .nearMessageId(Tools.getDbString(cursor, DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_ID))
//                    .nearMessageContent(nearMessageContent)
//                    .nearMessageType(MessageType.of(Tools.getDbString(cursor, DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_TYPE)))
//                    .nearMessageSenderId(Tools.getDbString(cursor, DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_SEND_ID))
//                    .nearMessageAvatarId(Tools.getDbString(cursor, DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_AVATAR_ID))
//                    .nearMessageSenderName(Tools.getDbString(cursor, DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_SEND_NAME));
//        } else {
//            builder.themeId("")
//                    .nearMessageId("")
//                    .nearMessageContent("")
//                    .nearMessageType(MessageType.UNDEF)
//                    .nearMessageSenderId("")
//                    .nearMessageAvatarId("")
//                    .nearMessageSenderName("");
//        }
//
//        builder.createUserId(Tools.getDbString(cursor, DBContract.MessageEntry.COLUMN_CREATE_USER_ID))
//                .updateUserId(Tools.getDbString(cursor, DBContract.MessageEntry.COLUMN_UPDATE_USER_ID))
//                .broadcastTime(Tools.getDbLong(cursor, DBContract.MessageEntry.COLUMN_BROADCAST_TIME))
//                .broadcastFlag(BroadcastFlag.valueOf(Tools.getDbString(cursor, DBContract.MessageEntry.COLUMN_BROADCAST_FLAG)));
//
//        return builder;
//    }
//
//
//    public static MessageEntity.MessageEntityBuilder formatByCursor(Map<String, Integer> index, Cursor cursor) {
//        MessageEntity.MessageEntityBuilder builder = MessageEntity.Build()
//                .id(cursor.getString(index.get(DBContract.MessageEntry._ID)))
//                .previousMessageId(cursor.getString(index.get(DBContract.MessageEntry.COLUMN_PREVIOUS_MESSAGE_ID)))
//                .roomId(cursor.getString(index.get(DBContract.MessageEntry.COLUMN_ROOM_ID)))
//                .content(cursor.getString(index.get(DBContract.MessageEntry.COLUMN_CONTENT)))
//                .sendTime(cursor.getLong(index.get(DBContract.MessageEntry.COLUMN_SEND_TIME)))
//                .sendNum(cursor.getInt(index.get(DBContract.MessageEntry.COLUMN_SEND_NUM)))
//                .receivedNum(cursor.getInt(index.get(DBContract.MessageEntry.COLUMN_RECEIVED_NUM)))
//                .readedNum(cursor.getInt(index.get(DBContract.MessageEntry.COLUMN_READED_NUM)))
//                .flag(MessageFlag.of(cursor.getInt(index.get(DBContract.MessageEntry.COLUMN_FLAG))))
//                .type(MessageType.of(cursor.getString(index.get(DBContract.MessageEntry.COLUMN_TYPE))))
//                .senderId(cursor.getString(index.get(DBContract.MessageEntry.COLUMN_SENDER_ID)))
//                .senderName(cursor.getString(index.get(DBContract.MessageEntry.COLUMN_SENDER_NAME)))
//                .avatarId(cursor.getString(index.get(DBContract.MessageEntry.COLUMN_AVATAR_ID)))
//                .sourceType(SourceType.of(cursor.getString(index.get(DBContract.MessageEntry.COLUMN_SOURCE_TYPE))))
//                .sequence(cursor.getInt(index.get(DBContract.MessageEntry.COLUMN_SEQUENCE)))
//                .from(ChannelType.of(cursor.getString(index.get(DBContract.MessageEntry.COLUMN_FROM))))
//                .osType(cursor.getString(index.get(DBContract.MessageEntry.COLUMN_OS_TYPE)))
//                .deviceType(cursor.getString(index.get(DBContract.MessageEntry.COLUMN_DEVICE_TYPE)))
//                .broadcastFlag(BroadcastFlag.of(index.get(DBContract.MessageEntry.COLUMN_BROADCAST_FLAG)));
//
//
//        if (index.get(DBContract.UserProfileEntry.COLUMN_ALIAS) != -1) {
//            String alias = cursor.getString(index.get(DBContract.UserProfileEntry.COLUMN_ALIAS));
//            if (!Strings.isNullOrEmpty(alias)) {
//                builder.senderName(cursor.getString(index.get(DBContract.UserProfileEntry.COLUMN_ALIAS)));
//            }
//        }
//
//
//        MessageStatus status = MessageStatus.of(cursor.getInt(index.get(DBContract.MessageEntry.COLUMN_STATUS)));
//        if (MessageStatus.SENDING.equals(status)) {
//            builder.status(MessageStatus.ERROR);
//        } else {
//            builder.status(status);
//        }
//
//        String themeId = cursor.getString(index.get(DBContract.MessageEntry.COLUMN_THEME_ID));
//        String nearMessageContent = cursor.getString(index.get(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_CONTENT));
//        if (!Strings.isNullOrEmpty(themeId) && !Strings.isNullOrEmpty(nearMessageContent)) {
//            builder.themeId(themeId)
//                    .nearMessageId(cursor.getString(index.get(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_ID)))
//                    .nearMessageContent(nearMessageContent)
//                    .nearMessageType(MessageType.of(cursor.getString(index.get(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_TYPE))))
//                    .nearMessageSenderId(cursor.getString(index.get(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_SEND_ID)))
//                    .nearMessageAvatarId(cursor.getString(index.get(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_AVATAR_ID)))
//                    .nearMessageSenderName(cursor.getString(index.get(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_SEND_NAME)));
//        } else {
//            builder.themeId("")
//                    .nearMessageId("")
//                    .nearMessageContent("")
//                    .nearMessageType(MessageType.UNDEF)
//                    .nearMessageSenderId("")
//                    .nearMessageAvatarId("")
//                    .nearMessageSenderName("");
//        }
//
//        builder.createUserId(cursor.getString(index.get(DBContract.MessageEntry.COLUMN_CREATE_USER_ID)))
//                .updateUserId(cursor.getString(index.get(DBContract.MessageEntry.COLUMN_UPDATE_USER_ID)))
//                .broadcastTime(cursor.getLong(index.get(DBContract.MessageEntry.COLUMN_BROADCAST_TIME)))
//                .broadcastFlag(BroadcastFlag.valueOf(cursor.getString(index.get(DBContract.MessageEntry.COLUMN_BROADCAST_FLAG))));
//
//        return builder;
//    }
//
//    public String toJson() {
//        return JsonHelper.toJson(this);
//    }
//
//    @Override
//    public String toString() {
//        return JsonHelper.toJson(this);
//    }
//
//}
//package tw.com.chainsea.ce.sdk.bean.msg;
//
//import android.content.ContentValues;
//import android.database.Cursor;
//
//import com.google.common.base.Strings;
//import com.google.common.collect.ComparisonChain;
//import com.google.common.collect.Lists;
//import com.google.common.collect.Sets;
//import com.google.gson.annotations.SerializedName;
//
//import java.io.Serializable;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import tw.com.chainsea.android.common.json.JsonHelper;
//import tw.com.chainsea.ce.sdk.bean.common.EnableType;
//import tw.com.chainsea.ce.sdk.bean.broadcast.TopicEntity;
//import tw.com.chainsea.ce.sdk.bean.msg.content.IMessageContent;
//import tw.com.chainsea.ce.sdk.bean.msg.content.UndefContent;
//import tw.com.chainsea.ce.sdk.database.DBContract;
//
///**
// * MessageEntity
// * Created by 90Chris on 2016/4/19.
// */
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder(builderMethodName = "Build", toBuilder = true)
//public class MessageEntity implements Serializable, Comparable<MessageEntity> {
//    private static final long serialVersionUID = 254829272788445833L;
//
//    String id; //*訊息 Id
//    String roomId; //*聊天室 Id
//    String senderId; //*訊息發送者 Id
//    String senderName; //*訊息發送者的名稱
//    @SerializedName("type")
//    @Builder.Default
//    MessageType type = MessageType.UNDEF; //*Server是String, 訊息類型
//    @SerializedName("sourceType")
//    @Builder.Default
//    SourceType sourceType = SourceType.UNDEF; //*該訊息的狀態，分為：user | system|Login
//    int sequence; //每則訊息所帶序號，會是連續號碼
//    String msgSrc; //*(補)如果為forward訊息會塞入前一筆的來源id
//    String avatarId;
//    @SerializedName("content")
//    String content; //*訊息內容
//    long sendTime; //*訊息發送時間
//    @SerializedName("flag")
//    @Builder.Default
//    MessageFlag flag = MessageFlag.UNDEF; //*Server是String, 訊息狀態，分為：-1（當前登入者發的）| 0 | 1（已到）| 2（已讀）| 3（收回）
//    int readedNum; //*訊息已讀取數量
//    int sendNum; //*訊息發送對象總數
//    int receivedNum; //*訊息已到達對方載具總數
//    String osType;
//    String deviceType;
//    @SerializedName("from")
//    @Builder.Default
//    ChannelType from = ChannelType.UNDEF; //*Server是String, 訊息來源，分為：line | facebook | ce (外部服務號進線會填入)
//    @Builder.Default
//    private String previousMessageId = "";
//
//    // extra
//    @Builder.Default
//    String ringName = "";
//
//    // theme
//    String themeId;
//    String nearMessageAvatarId;
//    String nearMessageSenderId;
//    String nearMessageId;
//    @SerializedName("nearMessageType")
//    @Builder.Default
//    MessageType nearMessageType = MessageType.UNDEF;
//    @SerializedName("nearMessageContent")
//    String nearMessageContent;
//    String nearMessageSenderName;
//
//    // local control
//    @Builder.Default
//    private MessageStatus status = MessageStatus.IS_REMOTE;
//
//    private EnableType enable;
//
//    @Builder.Default
//    private BroadcastFlag broadcastFlag = BroadcastFlag.BOOKING;
//    @Builder.Default
//    private List<TopicEntity> topicArray = Lists.newArrayList();
//    private long broadcastTime;
//    private String createUserId;
//    private String updateUserId;
//
//    // ui control
//    private boolean isDelete;
//    private boolean isShowChecked;
//    private boolean isForward;
//    private boolean isShowSelection;
//    private boolean isAnimator;
//
//    // socket io on('message/Message.New') this is an unread amount form room
//    private int unReadNum;
//
//    String chatId;
//    String tag;
//    /**
//     * checking can editable
//     * 狀態 BroadcastFlag == BOOKING && (
//     * 沒有 Message Id or
//     * local message status contains ERROR, FAILED or
//     * broadcastTime > 0 && broadcastTime > now
//     */
//    public boolean isCanEditBroadcast() {
//        if (BroadcastFlag.BOOKING.equals(broadcastFlag)) {
//            if (Strings.isNullOrEmpty(id)) {
//                return true;
//            } else {
//                if (MessageStatus.FAILED_or_ERROR.contains(status)) {
//                    return true;
//                }
//
//                long now = System.currentTimeMillis();
//                return broadcastTime > 0 && broadcastTime > now;
//            }
//        }
//        return false;
//    }
//
//    public boolean isCanEditBroadcastAndChackType() {
//        if (BroadcastFlag.BOOKING.equals(broadcastFlag)) {
//            if (Strings.isNullOrEmpty(id)) {
//                return true;
//            } else {
//                if (MessageStatus.FAILED_or_ERROR.contains(status)) {
//                    return true;
//                }
//
//                if (!MessageType.TEXT.equals(this.type)) {
//                    return false;
//                }
//
//                long now = System.currentTimeMillis();
//                return broadcastTime > 0 && broadcastTime > now;
//            }
//        }
//        return false;
//    }
//
//    public boolean isCanDeleteBroadcast() {
//        if (BroadcastFlag.BOOKING.equals(broadcastFlag)) {
//            if (Strings.isNullOrEmpty(id)) {
//                return false;
//            }
//
//            if (MessageStatus.FAILED_or_ERROR.contains(status)) {
//                return true;
//            }
//
//            long now = System.currentTimeMillis();
//            return broadcastTime > 0 && broadcastTime > now;
//        }
//        return false;
//    }
//
//    public IMessageContent<MessageType> content() {
//        if (type == null) {
//            return new UndefContent();
//        }
//        return type.from(content);
//    }
//
//    public IMessageContent<MessageType> nearMessageContent() {
//        if (nearMessageType == null) {
//            return new UndefContent();
//        }
//        return nearMessageType.from(nearMessageContent);
//    }
//
//    @Override
//    public int compareTo(MessageEntity o) {
//        return ComparisonChain.start()
//                .compare(this.getSendTime(), o.getSendTime() + 0.5)
//                .result();
//    }
//
//    public double getBroadcastWeights() {
//        double weight = 0.0d;
//        weight += MessageType.BROADCAST.equals(this.type) ? 2.0d : 0.0d;
//        weight += BroadcastFlag.BOOKING.equals(this.broadcastFlag) ? 1.0d : 0.0d;
//        return weight;
//    }
//
//    @Override
//    public int hashCode() {
//        final int prime = 31;
//        int result = 1;
//        result = prime * result + ((id == null) ? 0 : id.hashCode());
//        return result;
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj)
//            return true;
//        if (obj == null)
//            return false;
//        if (getClass() != obj.getClass())
//            return false;
//        MessageEntity other = (MessageEntity) obj;
//        if (this.id == null || other.getId() == null) {
//            return false;
//        } else return this.id.equals(other.getId());
//    }
//
//    // local data
//    public String[] getTopicIds() {
//        Set<String> idset = Sets.newHashSet();
//        for (TopicEntity t : this.topicArray) {
//            if (!t.isHardCode()) {
//                idset.add(t.getId());
//            }
//
//        }
//        return idset.toArray(new String[idset.size()]);
//    }
//
//    public static ContentValues getContentValues(String roomId, MessageEntity entity) {
//
//        ContentValues values = new ContentValues();
//        values.put(DBContract.MessageEntry._ID, entity.getId());
//        values.put(DBContract.MessageEntry.COLUMN_ROOM_ID, roomId);
//        values.put(DBContract.MessageEntry.COLUMN_PREVIOUS_MESSAGE_ID, entity.getPreviousMessageId());
//
//        values.put(DBContract.MessageEntry.COLUMN_CONTENT, Strings.isNullOrEmpty(entity.getContent()) ? "" : entity.getContent());
//        values.put(DBContract.MessageEntry.COLUMN_SENDER_ID, Strings.isNullOrEmpty(entity.getSenderId()) ? "" : entity.getSenderId());
//        values.put(DBContract.MessageEntry.COLUMN_AVATAR_ID, Strings.isNullOrEmpty(entity.getAvatarId()) ? "" : entity.getAvatarId());
//
//        values.put(DBContract.MessageEntry.COLUMN_SENDER_NAME, Strings.isNullOrEmpty(entity.getSenderName()) ? "" : entity.getSenderName());
//        values.put(DBContract.MessageEntry.COLUMN_SOURCE_TYPE, entity.getSourceType() == null ? SourceType.USER.name() : entity.getSourceType().name());
//        values.put(DBContract.MessageEntry.COLUMN_SEQUENCE, entity.getSequence());
//        values.put(DBContract.MessageEntry.COLUMN_FROM, entity.getFrom() == null ? ChannelType.UNDEF.getValue() : entity.getFrom().getValue());
//        values.put(DBContract.MessageEntry.COLUMN_TYPE, entity.getType() == null ? MessageType.UNDEF.getValue() : entity.getType().getValue());
//        values.put(DBContract.MessageEntry.COLUMN_OS_TYPE, Strings.isNullOrEmpty(entity.getOsType()) ? "" : entity.getOsType());
//        values.put(DBContract.MessageEntry.COLUMN_DEVICE_TYPE, Strings.isNullOrEmpty(entity.getDeviceType()) ? "" : entity.getDeviceType());
//
//        MessageFlag flag = entity.getFlag() == null ? MessageFlag.OWNER : entity.getFlag();
//        values.put(DBContract.MessageEntry.COLUMN_FLAG, flag.getFlag());
//        values.put(DBContract.MessageEntry.COLUMN_SEND_TIME, entity.getSendTime());
//        values.put(DBContract.MessageEntry.COLUMN_SEND_NUM, entity.getSendNum());
//        values.put(DBContract.MessageEntry.COLUMN_RECEIVED_NUM, entity.getReceivedNum());
//        values.put(DBContract.MessageEntry.COLUMN_READED_NUM, entity.getReadedNum());
//
//        values.put(DBContract.MessageEntry.COLUMN_CREATE_USER_ID, entity.getCreateUserId());
//        values.put(DBContract.MessageEntry.COLUMN_UPDATE_USER_ID, entity.getUpdateUserId());
//        values.put(DBContract.MessageEntry.COLUMN_BROADCAST_TIME, entity.getBroadcastTime());
//        values.put(DBContract.MessageEntry.COLUMN_BROADCAST_FLAG, (entity.getBroadcastFlag() != null) ?entity.getBroadcastFlag().name() : "null");
//
//        // Theme
//        if (!Strings.isNullOrEmpty(entity.getNearMessageContent()) && !Strings.isNullOrEmpty(entity.getThemeId())) {
//            values.put(DBContract.MessageEntry.COLUMN_THEME_ID, Strings.isNullOrEmpty(entity.getThemeId()) ? "" : entity.getThemeId());
//            values.put(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_ID, Strings.isNullOrEmpty(entity.getNearMessageId()) ? "" : entity.getNearMessageId());
//            values.put(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_SEND_ID, Strings.isNullOrEmpty(entity.getNearMessageSenderId()) ? "" : entity.getNearMessageSenderId());
//            values.put(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_SEND_NAME, Strings.isNullOrEmpty(entity.getNearMessageSenderName()) ? "" : entity.getNearMessageSenderName());
//            values.put(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_AVATAR_ID, Strings.isNullOrEmpty(entity.getNearMessageAvatarId()) ? "" : entity.getNearMessageAvatarId());
//            values.put(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_CONTENT, Strings.isNullOrEmpty(entity.getNearMessageContent()) ? "" : entity.getNearMessageContent());
//            values.put(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_TYPE, entity.getNearMessageType() == null ? MessageType.UNDEF.getValue() : entity.getNearMessageType().getValue());
//        } else {
//            values.put(DBContract.MessageEntry.COLUMN_THEME_ID, "");
//            values.put(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_ID, "");
//            values.put(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_SEND_ID, "");
//            values.put(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_SEND_NAME, "");
//            values.put(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_AVATAR_ID, "");
//            values.put(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_CONTENT, "");
//            values.put(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_TYPE, MessageType.UNDEF.getValue());
//        }
//
//        values.put(DBContract.MessageEntry.COLUMN_STATUS, entity.getStatus() == null ? MessageStatus.SENDING.getValue() : entity.getStatus().getValue());
//        return values;
//    }
//
//    public static MessageEntity.MessageEntityBuilder formatByCursor(Cursor cursor) {
//
//        MessageEntity.MessageEntityBuilder builder = new MessageEntity.Builder()
//                .id(Tools.getDbString(cursor, DBContract.MessageEntry._ID))
//                .previousMessageId(Tools.getDbString(cursor, DBContract.MessageEntry.COLUMN_PREVIOUS_MESSAGE_ID))
//                .roomId(Tools.getDbString(cursor, DBContract.MessageEntry.COLUMN_ROOM_ID))
//                .content(Tools.getDbString(cursor, DBContract.MessageEntry.COLUMN_CONTENT))
//                .sendTime(Tools.getDbLong(cursor, DBContract.MessageEntry.COLUMN_SEND_TIME))
//                .sendNum(Tools.getDbInt(cursor, DBContract.MessageEntry.COLUMN_SEND_NUM))
//                .receivedNum(Tools.getDbInt(cursor, DBContract.MessageEntry.COLUMN_RECEIVED_NUM))
//                .readedNum(Tools.getDbInt(cursor, DBContract.MessageEntry.COLUMN_READED_NUM))
//                .flag(MessageFlag.of(Tools.getDbInt(cursor, DBContract.MessageEntry.COLUMN_FLAG)))
//                .type(MessageType.of(Tools.getDbString(cursor, DBContract.MessageEntry.COLUMN_TYPE)))
//                .senderId(Tools.getDbString(cursor, DBContract.MessageEntry.COLUMN_SENDER_ID))
//                .senderName(Tools.getDbString(cursor, DBContract.MessageEntry.COLUMN_SENDER_NAME))
//                .avatarId(Tools.getDbString(cursor, DBContract.MessageEntry.COLUMN_AVATAR_ID))
//                .sourceType(SourceType.of(Tools.getDbString(cursor, DBContract.MessageEntry.COLUMN_SOURCE_TYPE)))
//                .from(ChannelType.of(Tools.getDbString(cursor, DBContract.MessageEntry.COLUMN_FROM)))
//                .osType(Tools.getDbString(cursor, DBContract.MessageEntry.COLUMN_OS_TYPE))
//                .deviceType(Tools.getDbString(cursor, DBContract.MessageEntry.COLUMN_DEVICE_TYPE));
//
//
//        if (Tools.checkIndex(cursor, DBContract.UserProfileEntry.COLUMN_ALIAS) != -1) {
//            String alias = Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_ALIAS);
//            if (!Strings.isNullOrEmpty(alias)) {
//                builder.senderName(Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_ALIAS));
//            }
//        }
//
//
//        MessageStatus status = MessageStatus.of(Tools.getDbInt(cursor, DBContract.MessageEntry.COLUMN_STATUS));
//        if (MessageStatus.SENDING.equals(status)) {
//            builder.status(MessageStatus.ERROR);
//        } else {
//            builder.status(status);
//        }
//
//        String themeId = Tools.getDbString(cursor, DBContract.MessageEntry.COLUMN_THEME_ID);
//        String nearMessageContent = Tools.getDbString(cursor, DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_CONTENT);
//        if (!Strings.isNullOrEmpty(themeId) && !Strings.isNullOrEmpty(nearMessageContent)) {
//            builder.themeId(themeId)
//                    .nearMessageId(Tools.getDbString(cursor, DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_ID))
//                    .nearMessageContent(nearMessageContent)
//                    .nearMessageType(MessageType.of(Tools.getDbString(cursor, DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_TYPE)))
//                    .nearMessageSenderId(Tools.getDbString(cursor, DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_SEND_ID))
//                    .nearMessageAvatarId(Tools.getDbString(cursor, DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_AVATAR_ID))
//                    .nearMessageSenderName(Tools.getDbString(cursor, DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_SEND_NAME));
//        } else {
//            builder.themeId("")
//                    .nearMessageId("")
//                    .nearMessageContent("")
//                    .nearMessageType(MessageType.UNDEF)
//                    .nearMessageSenderId("")
//                    .nearMessageAvatarId("")
//                    .nearMessageSenderName("");
//        }
//
//        builder.createUserId(Tools.getDbString(cursor, DBContract.MessageEntry.COLUMN_CREATE_USER_ID))
//                .updateUserId(Tools.getDbString(cursor, DBContract.MessageEntry.COLUMN_UPDATE_USER_ID))
//                .broadcastTime(Tools.getDbLong(cursor, DBContract.MessageEntry.COLUMN_BROADCAST_TIME))
//                .broadcastFlag(BroadcastFlag.valueOf(Tools.getDbString(cursor, DBContract.MessageEntry.COLUMN_BROADCAST_FLAG)));
//
//        return builder;
//    }
//
//
//    public static MessageEntity.MessageEntityBuilder formatByCursor(Map<String, Integer> index, Cursor cursor) {
//        MessageEntity.MessageEntityBuilder builder = new MessageEntity.Builder()
//                .id(cursor.getString(index.get(DBContract.MessageEntry._ID)))
//                .previousMessageId(cursor.getString(index.get(DBContract.MessageEntry.COLUMN_PREVIOUS_MESSAGE_ID)))
//                .roomId(cursor.getString(index.get(DBContract.MessageEntry.COLUMN_ROOM_ID)))
//                .content(cursor.getString(index.get(DBContract.MessageEntry.COLUMN_CONTENT)))
//                .sendTime(cursor.getLong(index.get(DBContract.MessageEntry.COLUMN_SEND_TIME)))
//                .sendNum(cursor.getInt(index.get(DBContract.MessageEntry.COLUMN_SEND_NUM)))
//                .receivedNum(cursor.getInt(index.get(DBContract.MessageEntry.COLUMN_RECEIVED_NUM)))
//                .readedNum(cursor.getInt(index.get(DBContract.MessageEntry.COLUMN_READED_NUM)))
//                .flag(MessageFlag.of(cursor.getInt(index.get(DBContract.MessageEntry.COLUMN_FLAG))))
//                .type(MessageType.of(cursor.getString(index.get(DBContract.MessageEntry.COLUMN_TYPE))))
//                .senderId(cursor.getString(index.get(DBContract.MessageEntry.COLUMN_SENDER_ID)))
//                .senderName(cursor.getString(index.get(DBContract.MessageEntry.COLUMN_SENDER_NAME)))
//                .avatarId(cursor.getString(index.get(DBContract.MessageEntry.COLUMN_AVATAR_ID)))
//                .sourceType(SourceType.of(cursor.getString(index.get(DBContract.MessageEntry.COLUMN_SOURCE_TYPE))))
//                .sequence(cursor.getInt(index.get(DBContract.MessageEntry.COLUMN_SEQUENCE)))
//                .from(ChannelType.of(cursor.getString(index.get(DBContract.MessageEntry.COLUMN_FROM))))
//                .osType(cursor.getString(index.get(DBContract.MessageEntry.COLUMN_OS_TYPE)))
//                .deviceType(cursor.getString(index.get(DBContract.MessageEntry.COLUMN_DEVICE_TYPE)))
//                .broadcastFlag(BroadcastFlag.of(index.get(DBContract.MessageEntry.COLUMN_BROADCAST_FLAG)));
//
//
//        if (index.get(DBContract.UserProfileEntry.COLUMN_ALIAS) != -1) {
//            String alias = cursor.getString(index.get(DBContract.UserProfileEntry.COLUMN_ALIAS));
//            if (!Strings.isNullOrEmpty(alias)) {
//                builder.senderName(cursor.getString(index.get(DBContract.UserProfileEntry.COLUMN_ALIAS)));
//            }
//        }
//
//
//        MessageStatus status = MessageStatus.of(cursor.getInt(index.get(DBContract.MessageEntry.COLUMN_STATUS)));
//        if (MessageStatus.SENDING.equals(status)) {
//            builder.status(MessageStatus.ERROR);
//        } else {
//            builder.status(status);
//        }
//
//        String themeId = cursor.getString(index.get(DBContract.MessageEntry.COLUMN_THEME_ID));
//        String nearMessageContent = cursor.getString(index.get(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_CONTENT));
//        if (!Strings.isNullOrEmpty(themeId) && !Strings.isNullOrEmpty(nearMessageContent)) {
//            builder.themeId(themeId)
//                    .nearMessageId(cursor.getString(index.get(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_ID)))
//                    .nearMessageContent(nearMessageContent)
//                    .nearMessageType(MessageType.of(cursor.getString(index.get(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_TYPE))))
//                    .nearMessageSenderId(cursor.getString(index.get(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_SEND_ID)))
//                    .nearMessageAvatarId(cursor.getString(index.get(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_AVATAR_ID)))
//                    .nearMessageSenderName(cursor.getString(index.get(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_SEND_NAME)));
//        } else {
//            builder.themeId("")
//                    .nearMessageId("")
//                    .nearMessageContent("")
//                    .nearMessageType(MessageType.UNDEF)
//                    .nearMessageSenderId("")
//                    .nearMessageAvatarId("")
//                    .nearMessageSenderName("");
//        }
//
//        builder.createUserId(cursor.getString(index.get(DBContract.MessageEntry.COLUMN_CREATE_USER_ID)))
//                .updateUserId(cursor.getString(index.get(DBContract.MessageEntry.COLUMN_UPDATE_USER_ID)))
//                .broadcastTime(cursor.getLong(index.get(DBContract.MessageEntry.COLUMN_BROADCAST_TIME)))
//                .broadcastFlag(BroadcastFlag.valueOf(cursor.getString(index.get(DBContract.MessageEntry.COLUMN_BROADCAST_FLAG))));
//
//        return builder;
//    }
//
//    public String toJson() {
//        return JsonHelper.getInstance().toJson(this);
//    }
//
//    @Override
//    public String toString() {
//        return JsonHelper.getInstance().toJson(this);
//    }
//
//}
