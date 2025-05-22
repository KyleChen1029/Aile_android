package tw.com.chainsea.ce.sdk.bean.chatrobot

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RobotServicingItem(
    val serviceNumberId: String?,
    val avatarId: String?,
    val updateTime: Long?,
    val ownerId: String?,
    val serviceNumberOwnerId: String?,
    val type: String?,
    val deleted: Boolean?,
    val unReadNum: Int?,
    val roomMemberIdentity: Int?,
    val id: String?,
    val memberIds: List<String>?,
    val serviceNumberType: String?,
    val lastSequence: Int?,
    val serviceNumberStatus: String?,
    val serviceNumberAvatarId: String?,
    val name: String?,
    val serviceNumberAgentId: String?,
    val lastEndServiceTime: Long?,
    val serviceNumberOpenType: Set<String>,
    val lastMessage: LastMessageResponse?,
    val isMute: Boolean?,
    val chatId: String?,
    val serviceNumberName: String?,
    val warned: Boolean?
): Parcelable

@Parcelize
data class LastMessageResponse(
    val sourceType: String?,
    val tag: String?,
    val msgSrc: String?,
    val sendTime: Long?,
    val readedNum: Int?,
    val roomId: String?,
    val sendNum: Int?,
    val senderId: String?,
    val from: String?,
    val type: String?,
    val content: String?,
    val id: String?,
    val flag: Int?,
    val senderName: String?,
    val sequence: Int?,
    val receivedNum: Int?,
    val chatId: String?
): Parcelable
