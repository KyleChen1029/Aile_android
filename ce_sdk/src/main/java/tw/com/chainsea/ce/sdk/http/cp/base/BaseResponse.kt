package tw.com.chainsea.ce.sdk.http.cp.base

import tw.com.chainsea.ce.sdk.network.model.common.Header

open class BaseResponse(var status: String? = null,
                        var errorCode: String? = null,
                        var errorMessage: String? = null,
                        var _header_: Header? = null,
                        val result: Boolean = false,
                        val msg: String = "",
                        val success: Boolean? = false)