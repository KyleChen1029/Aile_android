package tw.com.chainsea.ce.sdk.network.model.common

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.JsonClass
import tw.com.chainsea.ce.sdk.http.ce.request.SyncLabelRequest
import java.io.Serializable

@JsonClass(generateAdapter = true)
open class CommonRequest(
    override val pageSize: Int,
    override val refreshTime: Long,
    open val _header_: RequestHeader?
):BaseRequest(pageSize, refreshTime)

@JsonClass(generateAdapter = true)
data class RequestHeader(
    val tokenId: String,
    val language: String
)

@JsonClass(generateAdapter = true)
open class BaseRequest(
    open val pageSize: Int,
    open val refreshTime: Long) {
    constructor(refreshTime: Long): this(SyncLabelRequest.PAGE_SIZE, refreshTime)
}