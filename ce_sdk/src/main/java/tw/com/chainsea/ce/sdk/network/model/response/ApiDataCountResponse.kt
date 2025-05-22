package tw.com.chainsea.ce.sdk.network.model.response

import tw.com.chainsea.ce.sdk.network.model.common.Header

data class ApiDataCountResponse(val _header_: Header, val count: Int = 0)
