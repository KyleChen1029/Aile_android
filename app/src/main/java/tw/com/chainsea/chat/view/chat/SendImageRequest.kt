package tw.com.chainsea.chat.view.chat

data class SendImageRequest(
    val path: String,
    val isQuote: Boolean,
    val isOriginPhoto: Boolean,
    val isFacebookReplyPublic: Boolean
)
