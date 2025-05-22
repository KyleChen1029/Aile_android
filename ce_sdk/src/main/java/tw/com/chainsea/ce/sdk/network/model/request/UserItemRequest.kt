package tw.com.chainsea.ce.sdk.network.model.request

data class UserItemRequest(val userId: String)

data class AddressBookCustomFriendInfoRequest(
    val userId: String,
    val alias: String? = null,
    val otherPhone: String? = null,
    val addBookMem: String? = null
)
