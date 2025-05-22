package tw.com.chainsea.ce.sdk.network.model.common

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class CommonResponse<T>(
    val hasNextPage: Boolean?,
    val count: Int?,
    val items: T? = null,
    val _header_: Header?,
    val refreshTime: Long?,
    val status: String?,
    val sendSuccess: Boolean?
)

@JsonClass(generateAdapter = true)
@Parcelize
data class Header(
    val errorMessage: String?,
    val errorCode: String?,
    val timeCost: Int?,
    val success: Boolean?,
    val stackTrace: String?,
    val status: String?
): Parcelable, Serializable