package tw.com.chainsea.ce.sdk.network.model.response

import tw.com.chainsea.ce.sdk.network.model.common.Header

data class ServiceNumberTimeResponse(
    val _header_: Header?,
    val dictionaryItems: List<DictionaryItems>
)

data class DictionaryItems(
    val index: Int = 0,
    val text: String = "",
    val value: String = "0",
)