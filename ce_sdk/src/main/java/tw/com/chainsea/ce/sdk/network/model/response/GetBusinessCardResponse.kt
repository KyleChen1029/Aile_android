package tw.com.chainsea.ce.sdk.network.model.response

import tw.com.chainsea.ce.sdk.network.model.common.Header

data class GetBusinessCardResponse(
    val _header_: Header,
    val businessCardUrl: String
)
