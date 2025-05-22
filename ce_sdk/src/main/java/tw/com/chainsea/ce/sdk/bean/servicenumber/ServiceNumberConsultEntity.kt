package tw.com.chainsea.ce.sdk.bean.servicenumber

import tw.com.chainsea.ce.sdk.http.ce.request.ActiveServiceConsultArray
import tw.com.chainsea.ce.sdk.network.model.common.Header
import java.io.Serializable

data class ServiceNumberConsultEntity(
    val serviceNumberId: String,
    val serviceNumberType: String,
    val name: String,
    val description: String,
    val roomId: String,
    val serviceNumberAvatarId: String?
): Serializable

data class AIConsultation(
    val name: String,
    val consultId: String? = ""
)
data class ServiceNumberChatRoomAgentServicedResponse(
    val result: Boolean?,
    val transferFlag: Boolean?,
    val _header_: Header,
    val warned: Boolean?,
    val robotChatRecordLink: String?,
    val serviceNumberAgentId: String?,
    val startTime: Long?,
    val serviceNumberStatus: String?,
    val activeServiceConsultArray: List<ActiveServiceConsultArray>?,
    val aiConsultArray: List<AiConsultArrayDetail>?
): Serializable

data class AiConsultArrayDetail(
    val consultId: String
)
data class ServiceNumberChatRoomAgentServicedRequest(
    val roomId: String
)

data class StartServiceNUmberConsultAiRequest(
    val srcRoomId: String
)

data class StartServiceNUmberConsultAiResponse(
    val consultId: String?,
    val _header_: Header
)