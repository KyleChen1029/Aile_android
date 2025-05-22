package tw.com.chainsea.ce.sdk.network.model.response

import tw.com.chainsea.ce.sdk.network.model.common.Header

data class FriendRoomItemResponse(val isBlock: Boolean = false,
                                  val _header_: Header?,
                                  val isMute: Boolean = false,
                                  val updateTime: Long = -1,
                                  val type: String = "",
                                  val ownerId: String = "",
                                  val unReadNum: Int = 0,
                                  val lastSequence: Int = 0,
                                  val avatarId: String = "",
                                  val deleted: Boolean = false,
                                  val blocked: Boolean = false,
                                  val name: String = "",
                                  val id: String = "",
                                  val memberIds: List<String> = arrayListOf()
)