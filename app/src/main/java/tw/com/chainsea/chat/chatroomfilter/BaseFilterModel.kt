package tw.com.chainsea.chat.chatroomfilter

import tw.com.chainsea.ce.sdk.bean.msg.MessageType

open class BaseFilterModel(
    var date: String = "",
    var selectedNumber: Int = 0,
    var itemDate: String = "",
    var type: MessageType = MessageType.UNDEF,
)