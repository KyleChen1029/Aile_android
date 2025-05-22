package tw.com.chainsea.ce.sdk.event

enum class SocketEventEnum(val type: String) {
    TransTenantJoin("TransTenantJoin"),
    TransTenantRemove("TransTenantRemove"),
    TransTenantExit("TransTenantExit"),
    TransTenantDismiss("TransTenantDismiss"),
    TransTenantActive("TransTenantActive"),
    TransTenantJoinReject("TransTenantJoinReject"),
    TransTenantJoinAgree("TransTenantJoinAgree"),
    TransTenantMemberAdd("TransTenantMemberAdd"),
    TenantUnReadNum("TenantUnReadNum"),//團隊未讀更新
    SqueezedOut("SqueezedOut"),//被其他裝置搶登
    ForceLogout("ForceLogout"),//被強制登出
    DeviceLogin("DeviceLogin"),//其他裝置登入允許
    GuarantorJoin("GuarantorJoin"),//詢問擔保人加入團隊
    GuarantorJoinReject("GuarantorJoinReject"),//擔保人拒絕加入團隊
    GuarantorJoinAgree("GuarantorJoinAgree"),//擔保人同意加入團隊
    TenantJoinAgree("TenantJoinAgree"),//同意加入團隊
    TenantDeleteMember("TenantDeleteMember"),//退出團隊
    LoginSuccess("LoginSuccess")//Desktop登入成功
}