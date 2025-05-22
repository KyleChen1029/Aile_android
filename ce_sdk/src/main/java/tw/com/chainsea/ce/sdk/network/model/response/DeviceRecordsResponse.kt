package tw.com.chainsea.ce.sdk.network.model.response

data class DeviceRecordItem(
    val accountId: String?,
    val createTime: Long?,
    val osType: String?,
    val bundleId: String?,
    val updateTime: Long?,
    val isOnline: Boolean?,
    val rememberMe: Boolean?,
    val id: String,
    val deviceName: String?,
    val deviceId: String?,
    val uniqueID: String?,
    val empty: Boolean?
)
