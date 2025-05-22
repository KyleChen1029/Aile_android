package tw.com.chainsea.ce.sdk.socket.ce.bean

data class UpdateCustomerNameSocket(
    val nickName: String = "",
    val userNO: String = "",
    val updateTime: String = "",
    val tenantCode: String = "",
    val serviceNumberIds: List<String> = mutableListOf(),
    val customerDescription: String = "",
    val customerName: String = "",
    val isAnonymous: Boolean = true,
    val createTime: Long = 0,
    val tenantId: String = "",
    val id: String = "",
    val userType: String = "",
    val status: String = "",
)