package tw.com.chainsea.ce.sdk.service

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberChatRoomAgentServicedResponse
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberChatRoomAgentServicedRequest
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberConsultEntity
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity
import tw.com.chainsea.ce.sdk.bean.servicenumber.StartServiceNUmberConsultAiRequest
import tw.com.chainsea.ce.sdk.bean.servicenumber.StartServiceNUmberConsultAiResponse
import tw.com.chainsea.ce.sdk.http.ce.ApiPath
import tw.com.chainsea.ce.sdk.network.model.common.CommonResponse
import tw.com.chainsea.ce.sdk.network.model.request.GetServiceItemRequest
import tw.com.chainsea.ce.sdk.network.model.request.ServiceManagementRequest
import tw.com.chainsea.ce.sdk.network.model.request.ServiceModifyOwnerRequest

interface ManagementPermissionService {

    //轉移服務號擁有者
    @POST(ApiPath.ServiceNumberModifyOwner)
    suspend fun modifyOwner(@Body request: ServiceModifyOwnerRequest): Response<CommonResponse<Any>>

    //新增服務號管理者
    @POST(ApiPath.ServiceNumberMemberAddManager)
    suspend fun addManager(@Body request: ServiceManagementRequest): Response<CommonResponse<Any>>

    //移除服務號管理者
    @POST(ApiPath.ServiceNumberMemberDeleteManager)
    suspend fun deleteManager(@Body request: ServiceManagementRequest): Response<CommonResponse<Any>>

    //移除服務號服務人員
    @POST(ApiPath.ServiceNumberMemberRemove)
    suspend fun removeMember(@Body request: ServiceManagementRequest): Response<CommonResponse<Any>>

    //取得服務號資料
    @POST(ApiPath.serviceNumberItem)
    suspend fun getServiceNumberItem(@Body request: GetServiceItemRequest): Response<ServiceNumberEntity>

    //服務號諮詢列表
    @POST(ApiPath.serviceNumberConsultList)
    suspend fun getServiceNumberConsultationList(): Response<CommonResponse<List<ServiceNumberConsultEntity>>>

    //是否有服務人員服務中
    @POST(ApiPath.serviceNumberChatRoomAgentServiced)
    suspend fun getServiceNumberChatRoomAgentServicedInfo(@Body request: ServiceNumberChatRoomAgentServicedRequest): Response<ServiceNumberChatRoomAgentServicedResponse>

    //開始AI咨詢
    @POST(ApiPath.serviceNumberConsultAiStart)
    suspend fun doStartServiceNUmberConsultAi(@Body request: StartServiceNUmberConsultAiRequest): Response<StartServiceNUmberConsultAiResponse>
}