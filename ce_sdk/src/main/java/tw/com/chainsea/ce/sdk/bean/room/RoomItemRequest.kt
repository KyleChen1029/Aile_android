package tw.com.chainsea.ce.sdk.bean.room

import com.squareup.moshi.JsonClass
import tw.com.chainsea.ce.sdk.http.ce.request.SyncContactRequest
import tw.com.chainsea.ce.sdk.network.model.common.BaseRequest

@JsonClass(generateAdapter = true)
data class RoomItemRequest(override val pageSize: Int,
                           override val refreshTime: Long,
                           val roomId: String) : BaseRequest(pageSize, refreshTime) {
    constructor(roomId: String) : this(SyncContactRequest.PAGE_SIZE, 0L, roomId)
}