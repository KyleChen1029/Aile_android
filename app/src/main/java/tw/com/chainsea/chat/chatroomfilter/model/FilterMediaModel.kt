package tw.com.chainsea.chat.chatroomfilter.model

import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.chat.chatroomfilter.BaseFilterModel

data class FilterMediaModel(
    var thumbnail: String? = "",
    var videoDuration: Double = 0.0,
    val messageEntity: MessageEntity,
) : BaseFilterModel()