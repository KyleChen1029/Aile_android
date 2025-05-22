package tw.com.chainsea.chat.view.roomList.serviceRoomList

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.http.ce.ApiPath
import tw.com.chainsea.ce.sdk.network.model.common.CommonResponse
import tw.com.chainsea.ce.sdk.network.model.request.AiServicingChatRoomRequest

interface ServiceNumberRoomService {

    @POST(ApiPath.chatRoomRobotServiceList)
    suspend fun getRobotServicingCatRoomFromServer(@Body requestBody: AiServicingChatRoomRequest): Response<CommonResponse<MutableList<ChatRoomEntity>>>
}