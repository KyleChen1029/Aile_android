package tw.com.chainsea.ce.sdk.network.model.response

import tw.com.chainsea.ce.sdk.network.model.common.Header
import java.io.Serializable

data class UploadFileResponse(
    val size: Int?,
    val _header_ : Header,
    val thumbnailSize: Int?,
    val name: String?,
    val width: Int?,
    val thumbnailWidth: Int?,
    val thumbnailHeight: Int?,
    val url: String?,
    val MD5: String?,
    val height: Int?,
    val thumbnailUrl: String?
): Serializable
