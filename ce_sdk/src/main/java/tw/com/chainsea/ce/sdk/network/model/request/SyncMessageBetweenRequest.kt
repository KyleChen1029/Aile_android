package tw.com.chainsea.ce.sdk.network.model.request

data class SyncMessageBetweenRequest(
    val roomId: String,
    val previous_sequence: Int,
    val last_sequence: Int,
)