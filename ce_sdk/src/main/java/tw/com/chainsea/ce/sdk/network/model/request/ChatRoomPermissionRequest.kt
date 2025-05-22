package tw.com.chainsea.ce.sdk.network.model.request

data class DeletedChatRoomMember(
    val roomId: String,
    val userIds: List<String>
)

data class ModifyChatRoomOwner(
    val roomId: String,
    val userId: String
)

data class AddChatRoomManager(
    val roomId: String,
    val userIds: List<String>
)

data class DeleteChatRoomManager(
    val roomId: String,
    val userIds: List<String>
)

data class CleanChatRoomMessage(
    val roomId: String
)