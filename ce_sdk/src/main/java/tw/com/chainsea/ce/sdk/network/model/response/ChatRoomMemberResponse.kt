package tw.com.chainsea.ce.sdk.network.model.response

import android.content.ContentValues
import com.squareup.moshi.JsonClass
import tw.com.chainsea.ce.sdk.bean.msg.SourceType
import tw.com.chainsea.ce.sdk.bean.servicenumber.type.GroupPrivilegeEnum
import tw.com.chainsea.ce.sdk.database.DBContract
import java.io.Serializable

/**
 * 抓取社團成員權限回來的 Response
 * */
@JsonClass(generateAdapter = true)
data class ChatRoomMemberResponse(val firstSequence: Int = 0,
                                  val deleted: Boolean = false,
                                  val sourceType: SourceType = SourceType.USER,
                                  val lastReadSequence: Int = 0,
                                  val joinTime: Long = 0,
                                  val lastReceivedSequence: Int = 0,
                                  val updateTime: Long = 0,
                                  val type: String = "",
                                  val memberId: String = "",
                                  val privilege: GroupPrivilegeEnum = GroupPrivilegeEnum.Common

): Serializable {

    companion object {
        @JvmStatic
        fun getContentValue(roomId: String, chatRoomMemberResponses: ChatRoomMemberResponse) : ContentValues {
            val contentValues = ContentValues()
            contentValues.put(DBContract.ChatMemberEntry.COLUMN_ROOM_ID, roomId)
            contentValues.put(DBContract.ChatMemberEntry.COLUMN_FIRST_SEQUENCE, chatRoomMemberResponses.firstSequence)
            contentValues.put(DBContract.ChatMemberEntry.COLUMN_DELETED, if(chatRoomMemberResponses.deleted) "Y" else "N")
            contentValues.put(DBContract.ChatMemberEntry.COLUMN_SOURCE_TYPE, chatRoomMemberResponses.sourceType.name)
            contentValues.put(DBContract.ChatMemberEntry.COLUMN_LAST_READ_SEQUENCE, chatRoomMemberResponses.lastReadSequence)
            contentValues.put(DBContract.ChatMemberEntry.COLUMN_JOIN_TIME, chatRoomMemberResponses.joinTime)
            contentValues.put(DBContract.ChatMemberEntry.COLUMN_LAST_RECEIVED_SEQUENCE, chatRoomMemberResponses.lastReceivedSequence)
            contentValues.put(DBContract.ChatMemberEntry.COLUMN_UPDATE_TIME, chatRoomMemberResponses.updateTime)
            contentValues.put(DBContract.ChatMemberEntry.COLUMN_TYPE, chatRoomMemberResponses.type)
            contentValues.put(DBContract.ChatMemberEntry.COLUMN_MEMBER_ID, chatRoomMemberResponses.memberId)
            return contentValues
        }
    }
}