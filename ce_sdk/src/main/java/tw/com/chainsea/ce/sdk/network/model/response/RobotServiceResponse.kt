package tw.com.chainsea.ce.sdk.network.model.response

import tw.com.chainsea.ce.sdk.network.model.common.Header

data class RobotServiceResponse(
    val _header_: Header,
    val success: Boolean
)
