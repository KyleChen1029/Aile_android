package tw.com.chainsea.ce.sdk.network.model.request

data class DeleteMessageRequest(
    val roomId: String,
    val messageIds: MutableList<String>,
)