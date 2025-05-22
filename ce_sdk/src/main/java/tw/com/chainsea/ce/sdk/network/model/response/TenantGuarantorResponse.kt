package tw.com.chainsea.ce.sdk.network.model.response

import tw.com.chainsea.ce.sdk.network.model.common.Header

data class TenantGuarantorResponse(
    val _header_: Header,
    val minGuarantorCount: Int,
    val hadGuarantorCount: Int,
    val items: List<Guarantor>?
)

data class Guarantor(val createTime: Long, val name: String)