package tw.com.chainsea.ce.sdk.network.model.request

import com.squareup.moshi.JsonClass
import tw.com.chainsea.ce.sdk.network.model.common.BaseRequest

@JsonClass(generateAdapter = true)
data class ServiceSwitchIdentityRequest(override val pageSize: Int,
                                        override val refreshTime: Long,
                                        val serviceNumberId: String = "",
                                        val identityId: String = "") :
        BaseRequest(pageSize, refreshTime)