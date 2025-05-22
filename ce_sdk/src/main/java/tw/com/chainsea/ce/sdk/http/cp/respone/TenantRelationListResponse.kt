package tw.com.chainsea.ce.sdk.http.cp.respone

import tw.com.chainsea.ce.sdk.http.cp.base.BaseResponse

data class TenantRelationListResponse(
    var relationTenantArray: List<RelationTenant>,
    val count: Int?,
    val createTenant: Int? = -1,
    val joinTenant: Int? = -1,) :
        BaseResponse()


data class RelationTenant(var tenantName: String = "",
                          var openId: String = "",
                          var serviceUrl: String = "",
                          var description: String = "",
                          var scale: String = "",
                          var industry: String = "",
                          var tenantCode: String = "", //團隊的唯一識別碼
                          var certificationStatus: Int = 0,
                          var unReadNum: Int = 0,
                          var avatarId: String? = "",
                          var createTime: Long = 0,
                          var officialServiceNumberInfo: OfficialServiceNumberInfoDTO? = null,
                          var tenantId: String = "",
                          var isCommon: Boolean = false,
                          var userType: String = "",
                          var manageServiceNumberInfo: ManageServiceNumberInfoDTO? = null,
                          var isLastLogin: Boolean = false,
                          var abbreviationTenantName: String = "") {
    constructor(tenantName: String, serviceUrl: String, tenantCode: String) : this(tenantName,
            "", serviceUrl, "", "", "", tenantCode, 0, 0, "", 0, null, "", false, "", null, false)
    constructor(tenantName: String) : this(tenantName,
            "", "", "", "", "", "", 0, 0, "", 0, null, "", false, "", null, false, tenantName)

    constructor(tenantCode: String, tenantName: String, serviceUrl: String,avatarId: String?, unReadNum: Int): this(tenantName, "", serviceUrl, "", "", "", tenantCode, 0, unReadNum)
}

data class OfficialServiceNumberInfoDTO(
    var channels: List<ChannelsDTO?>? = mutableListOf(),
    var name: String = "",
    var id: String = "",
)

data class ChannelsDTO(
    var channelUrl: String = "",
    var name: String = "",
    var channelType: String = "",
    var channelId: String = "",
)

data class ManageServiceNumberInfoDTO(var name: String = "", var id: String = "")