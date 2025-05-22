package tw.com.chainsea.ce.sdk.network.model.request

import com.google.common.collect.Lists

data class MessageReadRequest(val roomId: String, val messageList: List<String> = Lists.newArrayList())
