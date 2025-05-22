package tw.com.chainsea.ce.sdk.http.ce.request

import com.squareup.moshi.JsonClass
import tw.com.chainsea.ce.sdk.network.model.common.BaseRequest

@JsonClass(generateAdapter = true)
data class AddContactFriendRequest(val userIds: List<String>, val alias: String? = null) :
        BaseRequest(0, System.currentTimeMillis())