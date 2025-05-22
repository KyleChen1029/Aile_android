package tw.com.chainsea.ce.sdk.network.services

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import tw.com.chainsea.ce.sdk.http.ce.ApiPath
import tw.com.chainsea.ce.sdk.network.model.request.ServiceSwitchIdentityRequest
import tw.com.chainsea.ce.sdk.network.model.common.ServiceSwitchIdentityResponse

interface SwitchIdentityService {

    @POST(ApiPath.serviceNumberIdentityBind)
    fun setIdentity(@Body request: ServiceSwitchIdentityRequest): Call<ServiceSwitchIdentityResponse>
}