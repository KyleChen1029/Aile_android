package tw.com.chainsea.ce.sdk.network.services

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import tw.com.chainsea.ce.sdk.http.ce.ApiPath
import tw.com.chainsea.ce.sdk.http.cp.CpApiPath
import tw.com.chainsea.ce.sdk.http.cp.respone.TenantRelationListResponse
import tw.com.chainsea.ce.sdk.network.model.common.CommonResponse
import tw.com.chainsea.ce.sdk.network.model.request.TenantGuarantorRequest
import tw.com.chainsea.ce.sdk.network.model.response.TenantGuarantorResponse

interface TenantService {

    //================= Ce Api ==================
    @POST(ApiPath.tenantEmployeeExit)
    fun quitTenant(): Call<CommonResponse<String>>

    @POST(ApiPath.TenantReGuarantorAgree)
    suspend fun tenantReGuarantorAgree(@Body request: TenantGuarantorRequest): Response<CommonResponse<String>>

    @POST(ApiPath.TenantReGuarantorReject)
    suspend fun tenantReGuarantorReject(@Body request: TenantGuarantorRequest): Response<CommonResponse<String>>

    @POST(ApiPath.TenantGuarantorList)
    suspend fun getTenantGuarantorList(): Response<TenantGuarantorResponse>

    @POST(ApiPath.TenantGuarantorAdd)
    suspend fun tenantGuarantorAdd(@Body request: TenantGuarantorRequest): Response<CommonResponse<Any>>

    //============================================
    //cp
    @POST(CpApiPath.TENANT_RELATION_LIST)
    suspend fun getTenantList(): Response<TenantRelationListResponse>


}