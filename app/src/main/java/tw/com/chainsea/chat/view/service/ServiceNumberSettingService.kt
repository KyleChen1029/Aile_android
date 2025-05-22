package tw.com.chainsea.chat.view.service

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import tw.com.chainsea.ce.sdk.http.ce.ApiPath
import tw.com.chainsea.ce.sdk.http.cp.base.BaseResponse
import tw.com.chainsea.ce.sdk.network.model.request.UpdateServiceNumberRequest
import tw.com.chainsea.ce.sdk.network.model.response.ServiceNumberTimeResponse

interface ServiceNumberSettingService {


    @POST(ApiPath.getServiceTimeOutList)
    suspend fun getServiceTimeOutList(): Response<ServiceNumberTimeResponse>

    @POST(ApiPath.getServiceIdleList)
    suspend fun getServiceIdleList(): Response<ServiceNumberTimeResponse>

    @POST(ApiPath.serviceNumberUpdate)
    suspend fun updateServiceNumber(@Body request: UpdateServiceNumberRequest): Response<BaseResponse>
}