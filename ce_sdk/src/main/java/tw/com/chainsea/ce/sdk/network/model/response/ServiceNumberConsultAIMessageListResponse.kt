package tw.com.chainsea.ce.sdk.network.model.response

data class ServiceNumberConsultAIMessageListResponse(
    val consultId: String,
    val senderId: String,
    val senderName: String,
    val avatarId: String?,
    val chatId: String,
    val id: String,
    val type: String,
    val content: String,
    val sendTime: Long
)
