package tw.com.aile.sdk.bean.filter

import java.io.Serializable

data class SearchFilterEntity(
    val sectioned: String,
    val roomType: FilterRoomType,
    val tab: FilterTab,
    val data: List<Any>,
    var isLoadMore: Boolean = false,
    var isExpand: Boolean = true
): Serializable

enum class FilterRoomType {
    FRIENDS, //夥伴聊天室
    DISCUSS, //多人聊天室
    EMPLOYEE, //夥伴
    CUSTOMER, //我的商務號客戶
    GROUP, //社團
    SUBSCRIBE_SERVICE_NUMBER, //我訂閱的服務號
    SERVICE_NUMBER_CHATROOM, //服務號聊天室
    NEWS //消息
}

enum class FilterTab {
    CHAT_ROOM, //聊天室
    CONTACT_PERSON, //聯絡人
    COMMUNITY, //社團 v3.2.1併入聯絡人與聊天室頁籤
    SERVICE_NUMBER //服務號
}
