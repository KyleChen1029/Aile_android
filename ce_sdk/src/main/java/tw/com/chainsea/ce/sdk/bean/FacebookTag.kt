package tw.com.chainsea.ce.sdk.bean

data class FacebookTag(
    val data: FacebookTagData,
    val type: String
) {
    data class FacebookTagData(
        val commentId: String,
        val from: String,
        val postId: String,
        val replyType: String = "",
        val content: List<FacebookTagContent>? = mutableListOf(),
    )

    data class FacebookTagContent(
        val type: FacebookContentTypes?,
        val url: String,
        val content: String

    )
}
