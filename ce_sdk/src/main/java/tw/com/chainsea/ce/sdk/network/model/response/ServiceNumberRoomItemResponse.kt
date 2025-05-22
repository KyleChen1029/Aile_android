package tw.com.chainsea.ce.sdk.network.model.response


import com.squareup.moshi.JsonClass
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.network.model.common.Header

@JsonClass(generateAdapter = true, generator = "java")
data class ServiceNumberRoomItemResponse(val _header_: Header?) : ChatRoomEntity()