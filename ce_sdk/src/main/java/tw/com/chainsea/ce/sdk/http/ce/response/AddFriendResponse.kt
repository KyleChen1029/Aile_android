package tw.com.chainsea.ce.sdk.http.ce.response

import com.squareup.moshi.JsonClass
import tw.com.chainsea.ce.sdk.network.model.common.Header

@JsonClass(generateAdapter = true)
data class AddFriendResponse(val roomIds: List<String?>?, val _header_: Header?)