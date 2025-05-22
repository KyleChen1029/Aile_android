package tw.com.chainsea.ce.sdk.network.services

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import tw.com.chainsea.ce.sdk.http.ce.ApiPath
import tw.com.chainsea.ce.sdk.network.model.response.TokenApplyResponse

interface APIService<in BaseTokenApplyRequest> {

    @POST(ApiPath.tokenApply)
    suspend fun tokenApply(@Body request: BaseTokenApplyRequest): Response<TokenApplyResponse>


}