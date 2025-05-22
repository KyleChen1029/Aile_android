package tw.com.chainsea.ce.sdk.network.model.request

data class DeleteLoginDeviceRequest(
    val id: String, //载具记录数据的唯一值
    val deviceId: String? //载具在线记录的ID 可选 载具在线时才传
)
data class AutoLoginRequest(
    val id: String
)
data class ForceLogoutDeviceRequest(
    val deviceId: String
)
