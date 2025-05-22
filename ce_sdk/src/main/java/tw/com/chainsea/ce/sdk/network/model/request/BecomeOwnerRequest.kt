package tw.com.chainsea.ce.sdk.network.model.request

import com.squareup.moshi.JsonClass

/**
 * 成為社團擁有者需要的 request
 * */
@JsonClass(generateAdapter = true)
data class BecomeOwnerRequest(val roomId: String)

@JsonClass(generateAdapter = true)
data class GetRoomMemberRequest(val roomId: String)