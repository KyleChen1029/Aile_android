package tw.com.chainsea.ce.sdk.network.model.response

data class SendFacebookCommentResponse(
    val result: Result
)

data class Result(
    val ids: List<String>,
    val id: String
)
