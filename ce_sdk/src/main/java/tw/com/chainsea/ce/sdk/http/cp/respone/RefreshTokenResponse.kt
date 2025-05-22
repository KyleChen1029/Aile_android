package tw.com.chainsea.ce.sdk.http.cp.respone

import tw.com.chainsea.ce.sdk.http.cp.model.TransTenantInfo
import tw.com.chainsea.ce.sdk.network.model.common.Header

data class RefreshTokenResponse(
    val tokenId: String?,
    val transTenantInfo: TransTenantInfo?,
    val _header_: Header?,
    val success: Boolean?,
    val status: String?,
    val errorMessage: String?,
    val errorCode: String?
)
