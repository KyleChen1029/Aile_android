package tw.com.chainsea.ce.sdk.network.model.request

data class SendFacebookCommentRequest(
    val postId: String,
    val commentId: String,
    val operation: String = "Add",
    val type: String,
    val content: String,
)
