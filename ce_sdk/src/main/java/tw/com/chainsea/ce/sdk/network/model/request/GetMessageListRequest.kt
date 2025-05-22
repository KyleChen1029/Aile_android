package tw.com.chainsea.ce.sdk.network.model.request

data class GetMessageListRequest(
    val sort: String = "asc",
    val roomId: String,
    val lastMessageId: String
)
