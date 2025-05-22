package tw.com.aile.sdk.bean.message

import android.content.ContentValues
import android.database.Cursor
import com.google.common.base.Strings
import com.google.common.collect.ComparisonChain
import com.google.common.collect.Lists
import com.google.common.collect.Sets
import com.google.gson.annotations.SerializedName
import org.json.JSONArray
import tw.com.chainsea.android.common.json.JsonHelper
import tw.com.chainsea.ce.sdk.bean.broadcast.TopicEntity
import tw.com.chainsea.ce.sdk.bean.common.EnableType
import tw.com.chainsea.ce.sdk.bean.msg.BroadcastFlag
import tw.com.chainsea.ce.sdk.bean.msg.ChannelType
import tw.com.chainsea.ce.sdk.bean.msg.FacebookCommentStatus
import tw.com.chainsea.ce.sdk.bean.msg.FacebookPostStatus
import tw.com.chainsea.ce.sdk.bean.msg.MessageFlag
import tw.com.chainsea.ce.sdk.bean.msg.MessageStatus
import tw.com.chainsea.ce.sdk.bean.msg.MessageType
import tw.com.chainsea.ce.sdk.bean.msg.SourceType
import tw.com.chainsea.ce.sdk.bean.msg.content.IMessageContent
import tw.com.chainsea.ce.sdk.bean.msg.content.UndefContent
import tw.com.chainsea.ce.sdk.database.DBContract
import tw.com.chainsea.ce.sdk.reference.MessageReference
import tw.com.chainsea.ce.sdk.service.ChatRoomService
import java.io.Serializable

class MessageEntity private constructor(
    var id: String?, // *訊息 Id
    var roomId: String?, // *聊天室 Id
    var senderId: String?, // *訊息發送者 Id
    var senderName: String?, // *訊息發送者的名稱
    var type: MessageType?, // *Server是String, 訊息類型
    var sourceType: SourceType?, // *該訊息的狀態，分為：user | system|Login
    var sequence: Int?, // 每則訊息所帶序號，會是連續號碼
    var msgSrc: String?, // *(補)如果為forward訊息會塞入前一筆的來源id
    var avatarId: String?,
    @SerializedName("content")
    var content: String, // *訊息內容
    var sendTime: Long, // *訊息發送時間
    @SerializedName("flag")
    var flag: MessageFlag?, // *Server是String, 訊息狀態，分為：-1（當前登入者發的）| 0 | 1（已到）| 2（已讀）| 3（收回）
    var readedNum: Int?, // *訊息已讀取數量
    var sendNum: Int?, // *訊息發送對象總數
    var receivedNum: Int?, // *訊息已到達對方載具總數
    var osType: String?,
    var deviceType: String?,
    @SerializedName("from")
    var from: ChannelType?, // *Server是String, 訊息來源，分為：line | facebook | ce (外部服務號進線會填入)
    var previousMessageId: String?,
    // extra
    var ringName: String?,
    // theme
    var themeId: String?,
    var nearMessageAvatarId: String?,
    var nearMessageSenderId: String?,
    var nearMessageId: String?,
    @SerializedName("nearMessageType")
    var nearMessageType: MessageType?,
    @SerializedName("nearMessageContent")
    var nearMessageContent: String?,
    var nearMessageSenderName: String?,
    // local control
    var _status: MessageStatus? = null,
    var enable: EnableType?,
    var broadcastFlag: BroadcastFlag?,
    var topicArray: List<TopicEntity>?,
    var broadcastTime: Long?,
    var createUserId: String?,
    var updateUserId: String?,
    // ui control
    var isDelete: Boolean?,
    var isShowChecked: Boolean?,
    var isForward: Boolean?,
    var isShowSelection: Boolean?,
    var isAnimator: Boolean?,
    // socket io on('message/Message.New') this is an unread amount form room
    var unReadNum: Int?,
    var chatId: String?,
    var tag: String?,
    var isFacebookPrivateReplied: Boolean? = false,
    var facebookCommentStatus: FacebookCommentStatus = FacebookCommentStatus.Create,
    var facebookPostStatus: FacebookPostStatus = FacebookPostStatus.Create
) : Comparable<MessageEntity>,
    Serializable {
    /**
     * gson parse data 沒辦法有 default value
     * 原本的 MessageEntity 有給 MessageStatus.IS_REMOTE
     * 故轉換成 data class 時用此解法
     */
    var status: MessageStatus?
        get() = _status ?: MessageStatus.IS_REMOTE
        set(value) {
            _status = value
        }

    companion object {
        @JvmStatic
        fun formatByCursor(
            index: Map<String?, Int>,
            cursor: Cursor
        ): MessageEntity {
            val builder =
                Builder()
                    .id(cursor.getString(index[DBContract.MessageEntry._ID]!!))
                    .previousMessageId(cursor.getString(index[DBContract.MessageEntry.COLUMN_PREVIOUS_MESSAGE_ID]!!))
                    .roomId(cursor.getString(index[DBContract.MessageEntry.COLUMN_ROOM_ID]!!))
                    .content(cursor.getString(index[DBContract.MessageEntry.COLUMN_CONTENT]!!))
                    .sendTime(cursor.getLong(index[DBContract.MessageEntry.COLUMN_SEND_TIME]!!))
                    .sendNum(cursor.getInt(index[DBContract.MessageEntry.COLUMN_SEND_NUM]!!))
                    .receivedNum(cursor.getInt(index[DBContract.MessageEntry.COLUMN_RECEIVED_NUM]!!))
                    .readedNum(cursor.getInt(index[DBContract.MessageEntry.COLUMN_READED_NUM]!!))
                    .flag(MessageFlag.of(cursor.getInt(index[DBContract.MessageEntry.COLUMN_FLAG]!!)))
                    .type(MessageType.of(cursor.getString(index[DBContract.MessageEntry.COLUMN_TYPE]!!)))
                    .senderId(cursor.getString(index[DBContract.MessageEntry.COLUMN_SENDER_ID]!!))
                    .senderName(cursor.getString(index[DBContract.MessageEntry.COLUMN_SENDER_NAME]!!))
                    .avatarId(cursor.getString(index[DBContract.MessageEntry.COLUMN_AVATAR_ID]!!))
                    .sourceType(SourceType.of(cursor.getString(index[DBContract.MessageEntry.COLUMN_SOURCE_TYPE]!!)))
                    .sequence(cursor.getInt(index[DBContract.MessageEntry.COLUMN_SEQUENCE]!!))
                    .from(ChannelType.of(cursor.getString(index[DBContract.MessageEntry.COLUMN_FROM]!!)))
                    .osType(cursor.getString(index[DBContract.MessageEntry.COLUMN_OS_TYPE]!!))
                    .deviceType(cursor.getString(index[DBContract.MessageEntry.COLUMN_DEVICE_TYPE]!!))
                    .tag(cursor.getString(index[DBContract.MessageEntry.COLUMN_TAG]!!))
                    .isFacebookPrivateReplied(cursor.getInt(index[DBContract.MessageEntry.COLUMN_IS_FACEBOOK_PRIVATE_REPLIED]!!) == 1)
                    .facebookCommentStatus(FacebookCommentStatus.of(cursor.getString(index[DBContract.MessageEntry.COLUMN_FACEBOOK_COMMENT_STATUS]!!)))
                    .facebookPostStatus(FacebookPostStatus.of(cursor.getString(index[DBContract.MessageEntry.COLUMN_FACEBOOK_POST_STATUS]!!)))

            index[DBContract.MessageEntry.COLUMN_BROADCAST_FLAG]?.let {
                builder.broadcastFlag(BroadcastFlag.of(it))
            }
            if (index[DBContract.UserProfileEntry.COLUMN_ALIAS] != -1) {
                val alias = cursor.getString(index[DBContract.UserProfileEntry.COLUMN_ALIAS]!!)
                if (!Strings.isNullOrEmpty(alias)) {
                    builder.senderName(cursor.getString(index[DBContract.UserProfileEntry.COLUMN_ALIAS]!!))
                }
            }
            val status = MessageStatus.of(cursor.getInt(index[DBContract.MessageEntry.COLUMN_STATUS]!!))
            if (MessageStatus.SENDING == status) {
                builder.status(MessageStatus.ERROR)
            } else {
                builder.status(status)
            }
            val themeId = cursor.getString(index[DBContract.MessageEntry.COLUMN_THEME_ID]!!)
            val nearMessageId = cursor.getString(index[DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_ID]!!)
            val nearMessageType = MessageType.of(cursor.getString(index[DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_TYPE]!!))
            val nearMessageContent = cursor.getString(index[DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_CONTENT]!!)
            val nearMessageSenderId = cursor.getString(index[DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_SEND_ID]!!)
            val nearMessageAvatarId = cursor.getString(index[DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_AVATAR_ID]!!)
            val nearMessageSenderName = cursor.getString(index[DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_SEND_NAME]!!)
            if (!Strings.isNullOrEmpty(themeId)) {
                builder
                    .themeId(themeId)
                    .nearMessageId(cursor.getString(index[DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_ID]!!))
            }

            if (nearMessageType != null) {
                builder.nearMessageType(nearMessageType)
            }

            if (!Strings.isNullOrEmpty(nearMessageContent)) {
                builder.nearMessageContent(nearMessageContent)
            }

            if (!Strings.isNullOrEmpty(nearMessageSenderId)) {
                builder.nearMessageSenderId(nearMessageSenderId)
            }

            if (!Strings.isNullOrEmpty(nearMessageAvatarId)) {
                builder.nearMessageAvatarId(nearMessageAvatarId)
            }

            if (!Strings.isNullOrEmpty(nearMessageSenderName)) {
                builder.nearMessageSenderName(nearMessageSenderName)
            }

            if (!Strings.isNullOrEmpty(nearMessageId)) {
                builder.nearMessageId(nearMessageId)
            }

            builder
                .createUserId(cursor.getString(index[DBContract.MessageEntry.COLUMN_CREATE_USER_ID]!!))
                .updateUserId(cursor.getString(index[DBContract.MessageEntry.COLUMN_UPDATE_USER_ID]!!))
                .broadcastTime(cursor.getLong(index[DBContract.MessageEntry.COLUMN_BROADCAST_TIME]!!))

            if ("null" != cursor.getString(index[DBContract.MessageEntry.COLUMN_BROADCAST_FLAG]!!)) {
                builder.broadcastFlag(BroadcastFlag.valueOf(cursor.getString(index[DBContract.MessageEntry.COLUMN_BROADCAST_FLAG]!!)))
            }
            return builder.build()
        }
    }

    fun getContentValues(roomId: String?): ContentValues {
        val values = ContentValues()
        values.put(DBContract.MessageEntry._ID, this.id)
        values.put(DBContract.MessageEntry.COLUMN_ROOM_ID, roomId)
        values.put(DBContract.MessageEntry.COLUMN_PREVIOUS_MESSAGE_ID, this.previousMessageId)
        values.put(DBContract.MessageEntry.COLUMN_CONTENT, if (Strings.isNullOrEmpty(this.content)) "" else this.content)
        values.put(DBContract.MessageEntry.COLUMN_SENDER_ID, if (Strings.isNullOrEmpty(this.senderId)) "" else this.senderId)
        values.put(DBContract.MessageEntry.COLUMN_AVATAR_ID, if (Strings.isNullOrEmpty(this.avatarId)) "" else this.avatarId)
        values.put(DBContract.MessageEntry.COLUMN_SENDER_NAME, if (Strings.isNullOrEmpty(this.senderName)) "" else this.senderName)
        values.put(DBContract.MessageEntry.COLUMN_SOURCE_TYPE, if (this.sourceType == null) SourceType.USER.name else this.sourceType!!.name)
        values.put(DBContract.MessageEntry.COLUMN_SEQUENCE, this.sequence)
        values.put(DBContract.MessageEntry.COLUMN_FROM, if (this.from == null) ChannelType.UNDEF.value else this.from!!.value)
        values.put(DBContract.MessageEntry.COLUMN_TYPE, if (this.type == null) MessageType.UNDEF.value else this.type!!.value)
        values.put(DBContract.MessageEntry.COLUMN_OS_TYPE, if (Strings.isNullOrEmpty(this.osType)) "" else this.osType)
        values.put(DBContract.MessageEntry.COLUMN_DEVICE_TYPE, if (Strings.isNullOrEmpty(this.deviceType)) "" else this.deviceType)
        val flag = if (this.flag == null) MessageFlag.OWNER else this.flag
        values.put(DBContract.MessageEntry.COLUMN_FLAG, flag!!.flag)
        values.put(DBContract.MessageEntry.COLUMN_SEND_TIME, this.sendTime)
        values.put(DBContract.MessageEntry.COLUMN_SEND_NUM, this.sendNum)
        values.put(DBContract.MessageEntry.COLUMN_RECEIVED_NUM, this.receivedNum)
        values.put(DBContract.MessageEntry.COLUMN_READED_NUM, this.readedNum)
        values.put(DBContract.MessageEntry.COLUMN_CREATE_USER_ID, this.createUserId)
        values.put(DBContract.MessageEntry.COLUMN_UPDATE_USER_ID, this.updateUserId)
        values.put(DBContract.MessageEntry.COLUMN_BROADCAST_TIME, this.broadcastTime)
        values.put(DBContract.MessageEntry.COLUMN_BROADCAST_FLAG, if (this.broadcastFlag != null) this.broadcastFlag!!.name else "null")
        values.put(DBContract.MessageEntry.COLUMN_TAG, this.tag)
        val message = MessageReference.findById(this.id)
        message?.let {
            values.put(DBContract.MessageEntry.COLUMN_IS_FACEBOOK_PRIVATE_REPLIED, if (it.isFacebookPrivateReplied == null || !it.isFacebookPrivateReplied!!) 0 else 1)
            values.put(DBContract.MessageEntry.COLUMN_THEME_ID, if (Strings.isNullOrEmpty(it.themeId)) themeId else it.themeId)
            values.put(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_ID, if (Strings.isNullOrEmpty(it.nearMessageId)) nearMessageId else it.nearMessageId)
            values.put(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_SEND_ID, if (Strings.isNullOrEmpty(it.nearMessageSenderId)) this.nearMessageSenderId else it.nearMessageSenderId)
            values.put(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_SEND_NAME, if (Strings.isNullOrEmpty(it.nearMessageSenderName)) nearMessageSenderName else it.nearMessageSenderName)
            values.put(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_AVATAR_ID, if (Strings.isNullOrEmpty(it.nearMessageAvatarId)) nearMessageAvatarId else it.nearMessageAvatarId)
            values.put(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_CONTENT, if (Strings.isNullOrEmpty(it.nearMessageContent)) nearMessageContent else it.nearMessageContent)
            val nearMessageType =
                if (it.nearMessageType == null || it.nearMessageType == MessageType.UNDEF) {
                    this.nearMessageType?.let {
                        it.value
                    } ?: run { MessageType.UNDEF.value }
                } else {
                    MessageType.UNDEF.value
                }

            values.put(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_TYPE, if (it.nearMessageType == null || it.nearMessageType == MessageType.UNDEF) nearMessageType else it.nearMessageType!!.value)
            values.put(DBContract.MessageEntry.COLUMN_FACEBOOK_COMMENT_STATUS, if (it.facebookCommentStatus == null) "" else it.facebookCommentStatus.name)
            values.put(DBContract.MessageEntry.COLUMN_FACEBOOK_POST_STATUS, if (it.facebookPostStatus == null) "" else it.facebookPostStatus.name)
        } ?: run {
            values.put(DBContract.MessageEntry.COLUMN_IS_FACEBOOK_PRIVATE_REPLIED, this.isFacebookPrivateReplied)
            values.put(DBContract.MessageEntry.COLUMN_THEME_ID, this.themeId)
            values.put(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_ID, this.nearMessageId)
            values.put(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_SEND_ID, this.nearMessageSenderId)
            values.put(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_SEND_NAME, this.nearMessageSenderName)
            values.put(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_AVATAR_ID, this.nearMessageAvatarId)
            values.put(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_CONTENT, this.nearMessageContent)
            values.put(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_TYPE, this.nearMessageType?.value)
            values.put(DBContract.MessageEntry.COLUMN_FACEBOOK_COMMENT_STATUS, if (this.facebookCommentStatus == null) "" else this.facebookCommentStatus.name)
            values.put(DBContract.MessageEntry.COLUMN_FACEBOOK_POST_STATUS, if (this.facebookPostStatus == null) "" else this.facebookPostStatus.name)
            values.put(DBContract.MessageEntry.COLUMN_IS_FACEBOOK_PRIVATE_REPLIED, this.isFacebookPrivateReplied)
        }
        values.put(DBContract.MessageEntry.COLUMN_STATUS, if (this.status == null) MessageStatus.SENDING.value else this.status!!.value)
        return values
    }

    fun isCanEditBroadcast(): Boolean {
        return if (BroadcastFlag.BOOKING == broadcastFlag) {
            if (Strings.isNullOrEmpty(id)) {
                true
            } else {
                if (MessageStatus.FAILED_or_ERROR.contains(status)) {
                    return true
                }
                val now = System.currentTimeMillis()
                broadcastTime?.let {
                    return it > 0 && it > now
                }
                return false
            }
        } else {
            false
        }
    }

    fun isCanEditBroadcastAndCheckType(): Boolean {
        return if (BroadcastFlag.BOOKING == broadcastFlag) {
            if (Strings.isNullOrEmpty(id)) {
                true
            } else {
                if (MessageStatus.FAILED_or_ERROR.contains(status)) {
                    return true
                }
                if (MessageType.TEXT != type) {
                    return false
                }
                val now = System.currentTimeMillis()
                broadcastTime?.let {
                    return it > 0 && it > now
                }
                return false
            }
        } else {
            false
        }
    }

    fun isCanDeleteBroadcast(): Boolean {
        if (BroadcastFlag.BOOKING == broadcastFlag) {
            if (Strings.isNullOrEmpty(id)) {
                return false
            }
            if (MessageStatus.FAILED_or_ERROR.contains(status)) {
                return true
            }
            val now = System.currentTimeMillis()
            broadcastTime?.let {
                return it > 0 && it > now
            }
            return false
        }
        return false
    }

    fun content(): IMessageContent<MessageType> =
        if (type == null) {
            UndefContent()
        } else {
            type!!.from(content)
        }

    fun nearMessageContent(): IMessageContent<MessageType> =
        if (nearMessageType == null) {
            UndefContent()
        } else {
            nearMessageType!!.from(nearMessageContent)
        }

    fun getBroadcastWeights(): Double {
        var weight = 0.0
        weight += if (MessageType.BROADCAST == type) 2.0 else 0.0
        weight += if (BroadcastFlag.BOOKING == broadcastFlag) 1.0 else 0.0
        return weight
    }

    fun getTopicIds(): Array<String> {
        val idset: MutableSet<String> = Sets.newHashSet()
        topicArray?.let {
            for (t in it) {
                if (!t.isHardCode) {
                    idset.add(t.id)
                }
            }
        }
        return idset.toTypedArray()
    }

    fun getFormatContent(): CharSequence =
        if (type == null) {
            ""
        } else {
            when (type) {
                MessageType.AT -> ChatRoomService.getAtContent(content)
                MessageType.VIDEO -> "[影片]"
                MessageType.LOCATION -> "[位置]"
                MessageType.BUSINESS -> "[物件]"
                MessageType.FILE -> "[文件]"
                MessageType.VOICE -> "[語音]"
                MessageType.IMAGE -> "[圖片]"
                MessageType.AD -> "[廣告]"
                MessageType.STICKER -> "[表情]"
                MessageType.TEXT -> getContent(content)
                MessageType.TEMPLATE -> "[卡片訊息]"
                else -> "[未知訊息格式]"
            }
        }

    fun getContent(input: String): String =
        runCatching {
            val jsonObject = JsonHelper.getInstance().toJsonObject(input)
            jsonObject.getString("text")
        }.getOrElse {
            runCatching {
                val jsonArray = JSONArray(input)
                val firstItem = jsonArray.getJSONObject(0)
                val contentObject = firstItem.getJSONObject("content")
                contentObject.getString("text")
            }.getOrElse { input }
        }

    override fun hashCode(): Int {
        val prime = 31
        val result = 1
        prime * result + if (id == null) 0 else id.hashCode()
        return result
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj == null) {
            return false
        }
        if (javaClass != obj.javaClass) {
            return false
        }
        val messageEntity = obj as MessageEntity
        return if (this.id == null || messageEntity.id == null) {
            false
        } else {
            this.id.equals(messageEntity.id)
        }
    }

    fun toJson(): String = JsonHelper.getInstance().toJson(this)

    data class Builder(
        var id: String? = null, // *訊息 Id
        var roomId: String? = null, // *聊天室 Id
        var senderId: String? = null, // *訊息發送者 Id
        var senderName: String? = null, // *訊息發送者的名稱
        var type: MessageType? = MessageType.UNDEF, // *Server是String, 訊息類型
        var sourceType: SourceType? = SourceType.UNDEF, // *該訊息的狀態，分為：user | system|Login
        var sequence: Int = 0, // 每則訊息所帶序號，會是連續號碼
        var msgSrc: String? = null, // *(補)如果為forward訊息會塞入前一筆的來源id
        var avatarId: String? = null,
        @SerializedName("content")
        var content: String = "", // *訊息內容
        var sendTime: Long = 0, // *訊息發送時間
        @SerializedName("flag")
        var flag: MessageFlag? = MessageFlag.UNDEF, // *Server是String, 訊息狀態，分為：-1（當前登入者發的）| 0 | 1（已到）| 2（已讀）| 3（收回）
        var readedNum: Int = 0, // *訊息已讀取數量
        var sendNum: Int = 0, // *訊息發送對象總數
        var receivedNum: Int = 0, // *訊息已到達對方載具總數
        var osType: String = "",
        var deviceType: String = "",
        @SerializedName("from")
        var from: ChannelType? = ChannelType.UNDEF, // *Server是String, 訊息來源，分為：line | facebook | ce (外部服務號進線會填入)
        var previousMessageId: String? = null,
        // extra
        var ringName: String? = null,
        // theme
        var themeId: String? = null,
        var nearMessageAvatarId: String? = null,
        var nearMessageSenderId: String? = null,
        var nearMessageId: String? = null,
        @SerializedName("nearMessageType")
        var nearMessageType: MessageType? = MessageType.UNDEF,
        @SerializedName("nearMessageContent")
        var nearMessageContent: String? = null,
        var nearMessageSenderName: String? = null,
        // local control
        var status: MessageStatus? = MessageStatus.IS_REMOTE,
        var enable: EnableType = EnableType.Y,
        var broadcastFlag: BroadcastFlag? = BroadcastFlag.BOOKING,
        var topicArray: List<TopicEntity>? = Lists.newArrayList(),
        var broadcastTime: Long = 0,
        var createUserId: String? = null,
        var updateUserId: String? = null,
        // ui control
        var isDelete: Boolean = false,
        var isShowChecked: Boolean = false,
        var isForward: Boolean = false,
        var isShowSelection: Boolean = false,
        var isAnimator: Boolean = false,
        // socket io on('message/Message.New') this is an unread amount form room
        var unReadNum: Int = 0,
        var chatId: String? = null,
        var tag: String? = null,
        var isFacebookPrivateReplied: Boolean? = false,
        var facebookCommentStatus: FacebookCommentStatus = FacebookCommentStatus.Create,
        var facebookPostStatus: FacebookPostStatus = FacebookPostStatus.Create
    ) {
        fun id(id: String?) = apply { this.id = id }

        fun roomId(roomId: String?) = apply { this.roomId = roomId }

        fun senderId(senderId: String) = apply { this.senderId = senderId }

        fun senderName(senderName: String) = apply { this.senderName = senderName }

        fun type(type: MessageType) = apply { this.type = type }

        fun sourceType(sourceType: SourceType) = apply { this.sourceType = sourceType }

        fun sequence(sequence: Int) = apply { this.sequence = sequence }

        fun msgSrc(msgSrc: String?) = apply { this.msgSrc = msgSrc }

        fun avatarId(avatarId: String?) = apply { this.avatarId = avatarId }

        fun content(content: String) = apply { this.content = content }

        fun sendTime(sendTime: Long) = apply { this.sendTime = sendTime }

        fun flag(flag: MessageFlag) = apply { this.flag = flag }

        fun readedNum(readedNum: Int) = apply { this.readedNum = readedNum }

        fun sendNum(sendNum: Int) = apply { this.sendNum = sendNum }

        fun receivedNum(receivedNum: Int) = apply { this.receivedNum = receivedNum }

        fun osType(osType: String) = apply { this.osType = osType }

        fun deviceType(deviceType: String) = apply { this.deviceType = deviceType }

        fun from(from: ChannelType) = apply { this.from = from }

        fun previousMessageId(previousMessageId: String?) = apply { this.previousMessageId = previousMessageId }

        fun ringName(ringName: String) = apply { this.ringName = ringName }

        fun themeId(themeId: String) = apply { this.themeId = themeId }

        fun nearMessageAvatarId(nearMessageAvatarId: String?) = apply { this.nearMessageAvatarId = nearMessageAvatarId }

        fun nearMessageSenderId(nearMessageSenderId: String?) = apply { this.nearMessageSenderId = nearMessageSenderId }

        fun nearMessageId(nearMessageId: String?) = apply { this.nearMessageId = nearMessageId }

        fun nearMessageType(nearMessageType: MessageType?) = apply { this.nearMessageType = nearMessageType }

        fun nearMessageContent(nearMessageContent: String?) = apply { this.nearMessageContent = nearMessageContent }

        fun nearMessageSenderName(nearMessageSenderName: String?) = apply { this.nearMessageSenderName = nearMessageSenderName }

        fun status(status: MessageStatus) = apply { this.status = status }

        fun enable(enable: EnableType) = apply { this.enable = enable }

        fun broadcastFlag(broadcastFlag: BroadcastFlag) = apply { this.broadcastFlag = broadcastFlag }

        fun topicArray(topicArray: List<TopicEntity>) = apply { this.topicArray = topicArray }

        fun broadcastTime(broadcastTime: Long) = apply { this.broadcastTime = broadcastTime }

        fun createUserId(createUserId: String?) = apply { this.createUserId = createUserId }

        fun updateUserId(updateUserId: String?) = apply { this.updateUserId = updateUserId }

        fun isDelete(isDelete: Boolean) = apply { this.isDelete = isDelete }

        fun isShowChecked(isShowChecked: Boolean) = apply { this.isShowChecked = isShowChecked }

        fun isForward(isForward: Boolean) = apply { this.isForward = isForward }

        fun isShowSelection(isShowSelection: Boolean) = apply { this.isShowSelection = isShowSelection }

        fun isAnimator(isAnimator: Boolean) = apply { this.isAnimator = isAnimator }

        fun unReadNum(unReadNum: Int) = apply { this.unReadNum = unReadNum }

        fun chatId(chatId: String) = apply { this.chatId = chatId }

        fun tag(tag: String?) = apply { this.tag = tag }

        fun isFacebookPrivateReplied(isFacebookPrivateReplied: Boolean) = apply { this.isFacebookPrivateReplied = isFacebookPrivateReplied }

        fun facebookCommentStatus(facebookCommentStatus: FacebookCommentStatus) = apply { this.facebookCommentStatus = facebookCommentStatus }

        fun facebookPostStatus(facebookPostStatus: FacebookPostStatus) = apply { this.facebookPostStatus = facebookPostStatus }

        fun build() = MessageEntity(id, roomId, senderId, senderName, type, sourceType, sequence, msgSrc, avatarId, content, sendTime, flag, readedNum, sendNum, receivedNum, osType, deviceType, from, previousMessageId, ringName, themeId, nearMessageAvatarId, nearMessageSenderId, nearMessageId, nearMessageType, nearMessageContent, nearMessageSenderName, status, enable, broadcastFlag, topicArray, broadcastTime, createUserId, updateUserId, isDelete, isShowChecked, isForward, isShowSelection, isAnimator, unReadNum, chatId, tag, isFacebookPrivateReplied, facebookCommentStatus, facebookPostStatus)
    }

    override fun compareTo(other: MessageEntity): Int =
        ComparisonChain
            .start()
            .compare(this.sendTime, other.sendTime + 0.5.toLong())
            .result()

    fun copy(): MessageEntity =
        MessageEntity(
            id = this.id,
            roomId = this.roomId,
            senderId = this.senderId,
            senderName = this.senderName,
            type = this.type,
            sourceType = this.sourceType,
            sequence = this.sequence,
            msgSrc = this.msgSrc,
            avatarId = this.avatarId,
            content = this.content,
            sendTime = this.sendTime,
            flag = this.flag,
            readedNum = this.readedNum,
            sendNum = this.sendNum,
            receivedNum = this.receivedNum,
            osType = this.osType,
            deviceType = this.deviceType,
            from = this.from,
            previousMessageId = this.previousMessageId,
            ringName = this.ringName,
            themeId = this.themeId,
            nearMessageAvatarId = this.nearMessageAvatarId,
            nearMessageSenderId = this.nearMessageSenderId,
            nearMessageId = this.nearMessageId,
            nearMessageType = this.nearMessageType,
            nearMessageContent = this.nearMessageContent,
            nearMessageSenderName = this.nearMessageSenderName,
            _status = this._status,
            enable = this.enable,
            broadcastFlag = this.broadcastFlag,
            topicArray = this.topicArray?.toList(), // 如果需要深拷貝，可以將這裡改成深拷貝的處理
            broadcastTime = this.broadcastTime,
            createUserId = this.createUserId,
            updateUserId = this.updateUserId,
            isDelete = this.isDelete,
            isShowChecked = this.isShowChecked,
            isForward = this.isForward,
            isShowSelection = this.isShowSelection,
            isAnimator = this.isAnimator,
            unReadNum = this.unReadNum,
            chatId = this.chatId,
            tag = this.tag,
            isFacebookPrivateReplied = this.isFacebookPrivateReplied,
            facebookCommentStatus = this.facebookCommentStatus,
            facebookPostStatus = this.facebookPostStatus
        )
}
