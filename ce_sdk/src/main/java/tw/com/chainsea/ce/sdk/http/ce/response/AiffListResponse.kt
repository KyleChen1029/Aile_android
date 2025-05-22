package tw.com.chainsea.ce.sdk.http.ce.response

import com.squareup.moshi.JsonClass
import tw.com.chainsea.ce.sdk.bean.response.AileTokenApply.Resp.AiffInfo
import tw.com.chainsea.ce.sdk.network.model.common.Header

/**
 * 用於聯絡人頁面的 aiff response
 * */
@JsonClass(generateAdapter = true)
data class AiffListResponse(val _header_: Header, val aiffInfo: List<AiffInfo>)