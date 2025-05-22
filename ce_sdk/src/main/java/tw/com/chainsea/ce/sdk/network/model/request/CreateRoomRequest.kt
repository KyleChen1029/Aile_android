package tw.com.chainsea.ce.sdk.network.model.request

import tw.com.chainsea.ce.sdk.network.model.common.RequestHeader

data class CreateRoomRequest(
    val userIds: List<String>,
    val type: String,
    val businessId: String,
    val name: String,
    val isCustomName: Boolean
)

data class ArgsRequest(
    val _header_: RequestHeader,
    val size: Int,
    val userIds: List<String>,
    val name: String,
    val type: String = "group",
    val x: Int = 0,
    val y: Int = 0,
)

data class ChatRoomHomePageRequest(
    val roomId: String
)

data class ChatRoomNameUpdateRequest(
    val data: ChatRoomNameUpdateDetail,
    val roomId: String
)

data class ChatRoomNameUpdateDetail(
    val name: String? = null,
    val ownerId: String? = null,
    val serviceAgent: String? = null
)