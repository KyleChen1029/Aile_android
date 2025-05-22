package tw.com.chainsea.chat.view.consultai

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import tw.com.chainsea.ce.sdk.http.ce.ApiPath
import tw.com.chainsea.ce.sdk.network.model.common.CommonResponse
import tw.com.chainsea.ce.sdk.network.model.request.ServiceNumberConsultAIMessageListRequest
import tw.com.chainsea.ce.sdk.network.model.request.ServiceNumberConsultAISendMessageRequest
import tw.com.chainsea.ce.sdk.network.model.response.ServiceNumberConsultAIMessageListResponse

interface ConsultAIService {

    @POST(ApiPath.serviceNumberConsultAISendMessage)
    suspend fun serviceNumberConsultAISendMessage(@Body
    request: ServiceNumberConsultAISendMessageRequest
    ): Response<CommonResponse<Any>>


    @POST(ApiPath.serviceNumberConsultAIMessage)
    suspend fun serviceNumberConsultAIMessage(@Body
    request: ServiceNumberConsultAIMessageListRequest): Response<CommonResponse<List<ServiceNumberConsultAIMessageListResponse>>>
}