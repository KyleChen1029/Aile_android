package tw.com.chainsea.ce.sdk.network.model.request

data class TenantAvatarRequest(
    val id: String,
    val size: String = "m"
)