package tw.com.chainsea.ce.sdk.bean

data class AvatarRecord(
    val id: String?,
    val type: RoomType,
    val ownerId: String? = "" //夥伴聊天室除了紀錄roomId, 也紀錄好友Id以利過濾
)

enum class RoomType {
    Contact, ChatRoom, Crowd, ServiceNumber, Customer, Discuss, ServiceOutSideChatRoom, System //聯絡人, 聊天室, 社團, 服務號, 客戶, 多人聊天室, 對外服務號聊天室, Aile小助手
}
