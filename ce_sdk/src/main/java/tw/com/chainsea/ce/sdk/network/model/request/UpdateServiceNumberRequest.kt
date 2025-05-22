package tw.com.chainsea.ce.sdk.network.model.request

data class UpdateServiceNumberRequest(
    val id: String,
    val serviceTimeoutTime: Int
)