package tw.com.chainsea.ce.sdk.bean.label

import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity
import tw.com.chainsea.ce.sdk.network.model.common.Header

data class LabelResponse(
    val _header_: Header?,
    val deleted: Boolean,
    val createTime: Long,
    val name: String,
    val readOnly: Boolean,
    val updateTime: Long,
    val id: String,
    val ownerId: String,
    val users: List<UserProfileEntity>
)