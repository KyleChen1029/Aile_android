package tw.com.chainsea.ce.sdk.network.model.request

data class AuthTokenLoginRequest(
    override val loginMode: String,
    override val tenantCode: String,
    override val deviceType: String,
    override val deviceName: String,
    override val osType: String,
    override val uniqueID: String,
    val authToken: String,
) : BaseTokenApplyRequest(deviceName, deviceType, loginMode, osType, tenantCode, uniqueID)

data class OtherLoginRequest(
    override val loginMode: String,
    override val tenantCode: String,
    override val deviceType: String,
    override val deviceName: String,
    override val osType: String,
    override val uniqueID: String,
    val countryCode: String,
    val loginName: String,
    val password: String,
) : BaseTokenApplyRequest(deviceName, deviceType, loginMode, osType, tenantCode, uniqueID)