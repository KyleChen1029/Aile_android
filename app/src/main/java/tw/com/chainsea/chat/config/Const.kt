package tw.com.chainsea.chat.config

object Const {
    const val AREA_CODE_TAIWAN = "+886"
    const val DELETE = "Delete"
    const val ADD = "Add"
    const val UPDATE = "Update"
    const val ADD_FROM_PROVISIONAL = "AddFromProvisional"
    const val APP = "APP"
}

enum class CallStatus {
    CallEnd,
    CallCancel,
    CallBusy
}

enum class ScannerType {
    None,
    JoinTenant,
    Scanner,
    ReScanGuarantor,
    FirstJoinTenant
}

enum class InvitationType {
    ProvisionalMember, // 臨時成員
    GroupRoom, // 社團成員
    MessageToTransfer, // 轉發訊息
    Discuss, // 多人聊天室成員
    ShareIn, // 內部分享
    ServiceNUmberConsultationAI, // 服務號AI諮詢
    ShareScreenShot // 截圖分享
}

enum class AiffDisplayLocation {
    PrivateRoom, // 單人聊天室
    DiscussRoom, // 多人聊天室
    GroupRoom, // 社團聊天室
    SelfRoom, // 個人聊天室
    SystemRoom, // 系統聊天室
    BusinessRoom, // 物件聊天室
    ServiceMemberRoom, // 服務號服務成員聊天室
    GroupRoomOwner, // 社團聊天室 (擁有者)
    BossServiceRoom, // 商務號聊天室
    BossServiceMemberRoom, // 商務號秘書群聊天室
    ServiceRoomAgent, // 服務號員工進線聊天室 (服務人員)
    ServiceRoomEmployee, // 服務號員工進線聊天室 (詢問者)
    ServiceRoomContact, // 服務號客戶進線聊天室
    ServiceRoom // BossServiceRoom + ServiceRoomContact---線上代碼是舊的，代碼保持原來邏輯新增新的邏輯
}

enum class AiffEmbedLocation {
    ChatRoomMenu, // 聊天室菜單
    MessageMenu, // 消息菜單
    ContactHome, // 客戶主頁
    ApplicationList // 聯絡人主頁
}
