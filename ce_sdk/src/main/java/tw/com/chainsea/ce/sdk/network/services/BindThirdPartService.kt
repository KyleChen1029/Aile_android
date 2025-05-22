package tw.com.chainsea.ce.sdk.network.services

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import tw.com.chainsea.ce.sdk.http.ce.ApiPath
import tw.com.chainsea.ce.sdk.http.cp.base.BaseResponse
import tw.com.chainsea.ce.sdk.network.model.request.BindFansPageRequest
import tw.com.chainsea.ce.sdk.network.model.request.UnBindFansPageRequest

interface BindThirdPartService {

    @POST(ApiPath.bindFacebook)
    suspend fun bindFacebook(@Body request: BindFansPageRequest): Response<BaseResponse>

    @POST(ApiPath.unBindFacebook)
    suspend fun unBindFacebook(@Body request: UnBindFansPageRequest): Response<BaseResponse>

    @POST(ApiPath.bindInstagram)
    suspend fun bindInstagram(@Body request: BindFansPageRequest): Response<BaseResponse>

    @POST(ApiPath.unBindInstagram)
    suspend fun unBindInstagram(@Body request: UnBindFansPageRequest): Response<BaseResponse>

}