package tw.com.chainsea.chat.view.chat

sealed class SendingState {
    data class Sending(val current: Int, val total: Int) : SendingState()
    data class Error(val message: String, val index: Int) : SendingState()
    object Completed : SendingState()
}