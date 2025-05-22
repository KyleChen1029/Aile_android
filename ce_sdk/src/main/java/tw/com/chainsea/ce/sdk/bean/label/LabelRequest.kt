package tw.com.chainsea.ce.sdk.bean.label

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LabelRequest(val labelId: String)