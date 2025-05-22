package tw.com.chainsea.ce.sdk.network.model.response

import tw.com.chainsea.ce.sdk.http.cp.base.BaseResponse

data class GetAtChatRoomResponse(val roomIds: List<String>): BaseResponse()