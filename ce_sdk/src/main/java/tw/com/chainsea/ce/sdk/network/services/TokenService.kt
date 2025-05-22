package tw.com.chainsea.ce.sdk.network.services

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import tw.com.chainsea.ce.sdk.http.ce.ApiPath
import tw.com.chainsea.ce.sdk.http.cp.CpApiPath
import tw.com.chainsea.ce.sdk.http.cp.request.RefreshTokenRequest
import tw.com.chainsea.ce.sdk.http.cp.respone.RefreshTokenResponse
import tw.com.chainsea.ce.sdk.http.cp.respone.TokenAnewResponse

interface TokenService {

    //============================================
    //cp
    @POST(CpApiPath.TOKEN_ANEW)
    suspend fun newCpToken(): Response<TokenAnewResponse>

    @POST(CpApiPath.TOKEN_REFRESH)
    suspend fun refreshCpToken(@Body request: RefreshTokenRequest): Response<RefreshTokenResponse>

    //================= Ce Api ==================
    @POST(ApiPath.tokenAnew)
    suspend fun newCeToken(): Response<TokenAnewResponse>

    @POST(ApiPath.tokenRefresh)
    suspend fun refreshCeToken(@Body request: RefreshTokenRequest): Response<RefreshTokenResponse>
}