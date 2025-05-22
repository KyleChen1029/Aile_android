package tw.com.chainsea.ce.sdk.network.model.request

import com.google.common.collect.Lists

data class SendAtMessageRequest(val roomId: String, val atAll: Boolean, val userIds: List<String>) {
    constructor(roomId: String, atAll: Boolean): this(roomId, atAll, Lists.newArrayList())
    constructor(roomId: String, userIds: List<String>): this(roomId, false, userIds)
}
