package tw.com.chainsea.ce.sdk.network.model.request

data class QuickReplyRequest(
    val roomId: String,
    val type: String,
    val content: String
)