package tw.com.chainsea.ce.sdk.network.model.request

import tw.com.chainsea.ce.sdk.network.model.common.BaseRequest

data class ServicesIdentityListRequest(override val pageSize: Int,
                                       override val refreshTime: Long,
                                       val serviceNumberId: String = "") :
        BaseRequest(pageSize, refreshTime)