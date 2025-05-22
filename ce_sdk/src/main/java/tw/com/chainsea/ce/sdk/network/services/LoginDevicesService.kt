package tw.com.chainsea.ce.sdk.network.services

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import tw.com.chainsea.ce.sdk.http.cp.CpApiPath
import tw.com.chainsea.ce.sdk.network.model.common.CommonResponse
import tw.com.chainsea.ce.sdk.network.model.request.AutoLoginRequest
import tw.com.chainsea.ce.sdk.network.model.request.DeleteLoginDeviceRequest
import tw.com.chainsea.ce.sdk.network.model.request.ForceLogoutDeviceRequest
import tw.com.chainsea.ce.sdk.network.model.response.DeviceRecordItem

interface LoginDevicesService {
    //================= Cp Api start==================
    @POST(CpApiPath.DEVICE_RECORD_LIST)
    suspend fun getDeviceRecordList(): Response<CommonResponse<MutableList<DeviceRecordItem>>>

    @POST(CpApiPath.DEVICE_RECORD_DELETE)
    suspend fun deleteDeviceRecord(@Body deleteLoginDeviceRequest: DeleteLoginDeviceRequest): Response<CommonResponse<Any>>

    @POST(CpApiPath.DEVICE_RECORD_REMEMBER_ME_ADD)
    suspend fun addDeviceRecordRememberMe(@Body autoLoginRequest: AutoLoginRequest): Response<CommonResponse<Any>>

    @POST(CpApiPath.DEVICE_RECORD_REMEMBER_ME_REMOVE)
    suspend fun removeDeviceRecordRememberMe(@Body autoLoginRequest: AutoLoginRequest): Response<CommonResponse<Any>>

    @POST(CpApiPath.DEVICE_LOGOUT_FORCE)
    suspend fun forceLogoutDevice(@Body forceLogoutDeviceRequest: ForceLogoutDeviceRequest): Response<CommonResponse<Any>>

    //================= Cp Api end==================
}