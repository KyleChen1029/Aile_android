package tw.com.chainsea.ce.sdk.network.model.request

import tw.com.chainsea.ce.sdk.network.model.common.RequestHeader

data class RobotChatRoomSnatchRequest(
    val pageSize: Int?,
    val refreshTime: Long?,
    val _header_: RequestHeader,
    val roomId: String
)
