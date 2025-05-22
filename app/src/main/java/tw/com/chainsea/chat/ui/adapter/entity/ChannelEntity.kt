package tw.com.chainsea.chat.ui.adapter.entity

import tw.com.chainsea.ce.sdk.bean.msg.ChannelType

data class ChannelEntity(
    val code: ChannelType,
    val name: String,
    val resId: Int
)
