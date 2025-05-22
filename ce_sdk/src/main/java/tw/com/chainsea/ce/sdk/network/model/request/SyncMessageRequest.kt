package tw.com.chainsea.ce.sdk.network.model.request

data class SyncMessageRequest(
    val roomId: String,
    val previous_sequence: Int,
    val sort: String,
    val pageSize: Int,
)