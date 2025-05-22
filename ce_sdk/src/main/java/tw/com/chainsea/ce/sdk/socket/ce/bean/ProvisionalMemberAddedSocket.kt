package tw.com.chainsea.ce.sdk.socket.ce.bean

data class ProvisionalMemberAddedSocket(
    val type: String,
    val userId: String,
    val roomId: String,
    val memberIds: List<String>
)
