package tw.com.chainsea.ce.sdk.socket.ce.bean

data class AtMessageSocket(val event: AtMessageContentEventEnum, val roomId: String)


enum class AtMessageContentEventEnum {
    Add, Update, Delete
}