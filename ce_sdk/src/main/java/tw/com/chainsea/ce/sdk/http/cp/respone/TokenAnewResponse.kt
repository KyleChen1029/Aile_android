package tw.com.chainsea.ce.sdk.http.cp.respone

import tw.com.chainsea.ce.sdk.http.cp.base.BaseResponse
import tw.com.chainsea.ce.sdk.http.cp.model.TransTenantInfo

data class TokenAnewResponse(val tokenId: String?, val tokenValidSeconds: Int?, val transTenantInfo: TransTenantInfo?): BaseResponse()
