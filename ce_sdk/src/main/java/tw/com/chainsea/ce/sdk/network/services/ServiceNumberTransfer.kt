package tw.com.chainsea.ce.sdk.network.services

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import tw.com.chainsea.ce.sdk.http.ce.ApiPath
import tw.com.chainsea.ce.sdk.network.model.common.CommonResponse
import tw.com.chainsea.ce.sdk.network.model.request.ServiceNumberSnatchRequest

interface ServiceNumberTransfer {
    @POST(ApiPath.serviceNumberTransferSnatch)
    fun snatchAgentServicing(@Body request: ServiceNumberSnatchRequest): Call<CommonResponse<Any>>
}