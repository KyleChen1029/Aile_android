package tw.com.chainsea.ce.sdk.network.model.response

import tw.com.chainsea.ce.sdk.network.model.common.Header

data class FromAppointResponse(
    val _header_: Header?,
    val otherFroms: List<String>?,
    val lastFrom: String?,
    val status: String?,
    val lastMessageTime: Long = 0L,
    val lastMessageTimeOut: Boolean = false,
    // Facebook 回覆時限(天數
    val fbReplyTimeLimit: Long = 0L
)
