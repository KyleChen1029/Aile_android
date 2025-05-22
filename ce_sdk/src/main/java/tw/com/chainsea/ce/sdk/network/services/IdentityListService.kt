package tw.com.chainsea.ce.sdk.network.services

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import tw.com.chainsea.ce.sdk.http.ce.ApiPath
import tw.com.chainsea.ce.sdk.network.model.request.ServicesIdentityListRequest
import tw.com.chainsea.ce.sdk.network.model.response.ServicesIdentityListResponse
import tw.com.chainsea.ce.sdk.network.model.common.CommonResponse

interface IdentityListService {

    @POST(ApiPath.serviceNumberIdentityList)
    fun getIdentityList(@Body servicesIdentityListRequest: ServicesIdentityListRequest): Call<CommonResponse<List<ServicesIdentityListResponse>>>

}