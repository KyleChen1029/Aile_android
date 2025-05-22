package tw.com.chainsea.ce.sdk.network.services

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.http.ce.ApiPath
import tw.com.chainsea.ce.sdk.network.model.request.RobotChatRoomSnatchRequest
import tw.com.chainsea.ce.sdk.network.model.response.RobotServiceResponse
import tw.com.chainsea.ce.sdk.network.model.common.CommonRequest
import tw.com.chainsea.ce.sdk.network.model.common.CommonResponse

interface RobotService {

    @POST(ApiPath.chatRoomRobotServiceSnatch)
    fun snatchRobotServicing(
        @Body robotChatRoomSnatchRequest: RobotChatRoomSnatchRequest
    ): Call<RobotServiceResponse>

}