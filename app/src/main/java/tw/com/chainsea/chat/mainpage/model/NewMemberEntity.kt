package tw.com.chainsea.chat.mainpage.model

data class NewMemberEntity(
    val roomId: String,
    val type: MemberRoomType
)

data class MoreMembersEntity(
    val roomId: String,
    val type: MemberRoomType,
    val avatarId: String,
    val userName: String,
    val memberLeftCount: String
)

enum class MemberRoomType {
    Discuss, Crowd
}

