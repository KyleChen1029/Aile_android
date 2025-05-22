package tw.com.chainsea.ce.sdk.network.model.request

data class ServiceNumberRoomItemRequest(
    val serviceNumberId: String,
    val userId: String,
    val type: Int
)