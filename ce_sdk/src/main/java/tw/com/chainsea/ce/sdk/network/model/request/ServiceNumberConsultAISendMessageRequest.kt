package tw.com.chainsea.ce.sdk.network.model.request

data class ServiceNumberConsultAISendMessageRequest(
    val roomId: String,
    val serviceNumberId: String,
    val content: String,
    val type: String
)
