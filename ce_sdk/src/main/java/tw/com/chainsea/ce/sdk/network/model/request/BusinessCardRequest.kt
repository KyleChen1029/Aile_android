package tw.com.chainsea.ce.sdk.network.model.request

data class BusinessCardRequest(
    val roomId: String,
    val channelType: String
)

data class FromAppointRequest(
    val roomId: String
)