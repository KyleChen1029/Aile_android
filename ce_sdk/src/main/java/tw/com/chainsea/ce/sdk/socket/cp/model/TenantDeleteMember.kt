package tw.com.chainsea.ce.sdk.socket.cp.model

data class TenantDeleteMember(
    val selfOperated: Boolean,
    val tenantName: String,
    val tenantCode: String
)