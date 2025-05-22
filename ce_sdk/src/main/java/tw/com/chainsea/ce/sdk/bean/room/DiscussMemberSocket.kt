package tw.com.chainsea.ce.sdk.bean.room

import com.google.common.collect.Lists

data class DiscussMemberSocket(
    val name: String = "",
    val type: ChatRoomType = ChatRoomType.undef,
    val userId: String = "",
    val roomId: String = "",
    val memberIds: List<String> = Lists.newArrayList()
)