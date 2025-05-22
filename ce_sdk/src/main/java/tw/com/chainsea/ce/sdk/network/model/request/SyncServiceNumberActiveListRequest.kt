package tw.com.chainsea.ce.sdk.network.model.request

import tw.com.chainsea.ce.sdk.network.model.common.BaseRequest

data class SyncServiceNumberActiveListRequest(
    val serviceNumberIds: List<String>,
    override val refreshTime: Long
) : BaseRequest(refreshTime)
