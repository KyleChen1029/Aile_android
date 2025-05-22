package tw.com.chainsea.ce.sdk.network.services

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import tw.com.chainsea.ce.sdk.http.ce.ApiPath
import tw.com.chainsea.ce.sdk.network.model.request.GetBusinessCardRequest
import tw.com.chainsea.ce.sdk.network.model.response.GetBusinessCardResponse

interface BusinessCardService {

    @POST(ApiPath.serviceNumberBusinessCardUrl)
    suspend fun getServiceNumberBusinessCard(@Body request: GetBusinessCardRequest): Response<GetBusinessCardResponse>
}