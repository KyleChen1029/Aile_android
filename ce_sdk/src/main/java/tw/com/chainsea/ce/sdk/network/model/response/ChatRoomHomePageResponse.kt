package tw.com.chainsea.ce.sdk.network.model.response

import tw.com.chainsea.ce.sdk.network.model.common.Header

data class ChatRoomHomePageResponse(
    val _header_: Header,
    val roomPics: List<RoomPicsItem>?
)
data class RoomPicsItem(
    val sequence: Int?,
    val picUrl: String?,
    val id: String?,
    val userId: String?,
    val roomId: String?
)