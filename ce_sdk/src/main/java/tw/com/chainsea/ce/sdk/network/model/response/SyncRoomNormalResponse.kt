package tw.com.chainsea.ce.sdk.network.model.response

import android.content.ContentValues
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.android.common.json.JsonHelper
import tw.com.chainsea.ce.sdk.bean.common.EnableType
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType
import tw.com.chainsea.ce.sdk.database.DBContract
import tw.com.chainsea.ce.sdk.service.type.ChatRoomSource
import kotlin.math.max

data class SyncRoomNormalResponse(
    val firstSequence: Int,
    val isMute: Boolean,
    val lastReadSequence: Int,
    val lastReceivedSequence: Int,
    val isCustomName: Boolean?,
    var lastMessage: MessageEntity?,
    val member_deleted: Boolean,
    var updateTime: Long,
    val type: ChatRoomType?,
    val ownerId: String?,
    val topTime: Long,
    var unReadNum: Int,
    val lastSequence: Int,
    val avatarId: String?,
    val deleted: Boolean,
    val isTop: Boolean,
    val dfrTime: Long?,
    val name: String?,
    val id: String,
    val memberIds: List<String>
) {
    fun getContentValues(): ContentValues {
        val contentValues = ContentValues()
        contentValues.put(DBContract.ChatRoomEntry._ID, id)
        contentValues.put(DBContract.ChatRoomEntry.COLUMN_TITLE, name)
        contentValues.put(
            DBContract.ChatRoomEntry.COLUMN_TYPE,
            type?.let { it.name } ?: run { ChatRoomType.undef.name }
        )
        contentValues.put(DBContract.ChatRoomEntry.COLUMN_AVATAR_ID, avatarId)
        contentValues.put(DBContract.ChatRoomEntry.COLUMN_OWNER_ID, ownerId)
        contentValues.put(DBContract.ChatRoomEntry.COLUMN_UNREAD_NUMBER, unReadNum)
        contentValues.put(DBContract.ChatRoomEntry.COLUMN_LAST_SEQUENCE, lastSequence)
        // status
        contentValues.put(DBContract.ChatRoomEntry.COLUMN_IS_TOP, EnableType.of(isTop).name)
        contentValues.put(
            DBContract.ChatRoomEntry.COLUMN_TOP_TIME,
            if (isTop) max(topTime, 0L) else 0L
        )
        contentValues.put(
            DBContract.ChatRoomEntry.COLUMN_IS_CUSTOM_NAME,
            isCustomName?.let { EnableType.of(it).name } ?: run { "N" }
        )
        contentValues.put(DBContract.ChatRoomEntry.COLUMN_IS_MUTE, EnableType.of(isMute).name)
        // last message toString
        lastMessage?.let {
            val lastMessageJson = it.toJson()
            contentValues.put(DBContract.ChatRoomEntry.COLUMN_LAST_MESSAGE_ENTITY, lastMessageJson)
//             local control
            contentValues.put(DBContract.ChatRoomEntry.COLUMN_UPDATE_TIME, it.sendTime)
        } ?: run {
            // local control
            contentValues.put(DBContract.ChatRoomEntry.COLUMN_UPDATE_TIME, updateTime)
        }

        contentValues.put(
            DBContract.ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED,
            EnableType.of(deleted).name
        )

        contentValues.put(
            DBContract.ChatRoomEntry.COLUMN_LIST_CLASSIFY,
            ChatRoomSource.MAIN.name
        )

        contentValues.put(
            DBContract.ChatRoomEntry.COLUMN_MEMBER_IDS,
            JsonHelper.getInstance().toJson(memberIds)
        )

        contentValues.put(
            DBContract.ChatRoomEntry.COLUMN_DFR_TIME,
            dfrTime ?: 0L
        )

        val isDeleted = if (member_deleted || deleted) "Y" else "N"

        contentValues.put(
            DBContract.ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED,
            isDeleted
        )

        return contentValues
    }
}
