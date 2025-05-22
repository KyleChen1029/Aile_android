package tw.com.chainsea.ce.sdk.network.model.response

import tw.com.chainsea.ce.sdk.http.cp.base.BaseResponse
import tw.com.chainsea.ce.sdk.network.model.common.Header

data class CheckCommentResponse(
    val result: CheckCommentResult?,
    val _header_: Header
)


data class CheckCommentResult(
    val canPrivateReply: Boolean,
    val postStatus: String,
    val commentStatus: String
)