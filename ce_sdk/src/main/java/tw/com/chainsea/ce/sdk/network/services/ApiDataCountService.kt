package tw.com.chainsea.ce.sdk.network.services

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import tw.com.chainsea.ce.sdk.http.ce.ApiPath
import tw.com.chainsea.ce.sdk.network.model.request.ApiDataCountRequest
import tw.com.chainsea.ce.sdk.network.model.response.ApiDataCountResponse

interface ApiDataCountService {

    @POST(ApiPath.ApiDataCount)
    suspend fun getApiDataCount(@Body request: ApiDataCountRequest): Response<ApiDataCountResponse>
}