package tw.com.chainsea.ce.sdk.network.model.response

import tw.com.chainsea.ce.sdk.bean.response.AileTokenApply

data class TokenApplyResponse(val serialVersionUID: Long = -145252097559103811L,
                              val tokenId: String,
                              val employeeTokenId: String,
                              val contactTokenId: String,
                              val tenantInfo: TenantInfo?,
                              val configuration: Configuration?,
                              val user: User?,
                              val deviceId: String,
                              val authToken: String,
                              val aiffInfo: List<AileTokenApply.Resp.AiffInfo>)

data class TenantInfo(val serialVersionUID: Long,
                      val tokenValidSeconds: Int,
                      val businessSystemUrl: String,
                      val isEnableCall: Boolean,
                      val uploadFileMaxSize: Long)

data class Configuration(val serialVersionUID: Long,
                         val socketIoUrl: String,
                         val enableAck: Boolean,
                         val socketIoNamespace: String,
                         val connectType: TokenApplyConnectType,
                         val socketIoPassword: String)

data class User(val serialVersionUID: Long,
                val id: String,
                val avatarId: String,
                val isBindAile: Boolean,
                val bindUrl: String,
                val isCollectInfo: Boolean,
                val nickName: String,
                val userType: UserType,
                val sipwayInfo: SipwayInfo,
                val employee: Employee,
                val onlineDeviceInfo: List<OnlineDeviceInfo>,
                val hasBusinessSystem: Boolean,
                val hasBindEmployee: Boolean,
                val isMute: Boolean,
                val personRoomId: String)

data class SipwayInfo(val serialVersionUID: Long,
                      val authPass: String,
                      val sipxExtensionURL: String,
                      val sipxURL: String,
                      val duration: Int,
                      val userNo: String,
                      val corpVccId: String,
                      val webCallURL: String,
                      val isExtension: Boolean)

data class Employee(val serialVersionUID: Long,
                    val id: String,
                    val extension: String,
                    val duty: String,
                    val department: String,
                    val name: String,
                    val businessSystemUrl: String,
                    val loginName: String)

data class OnlineDeviceInfo(val serialVersionUID: Long,
                            val loginTime: Long,
                            val deviceType: String,
                            val osType: String)

enum class TokenApplyConnectType(private val type: String) {
    SOCKET_IO("Socket.IO"), JOCKET("Jocket"), UNDEF("UNDEF")
}

enum class UserType(private val type: String) {
    EMPLOYEE("employee"), employee("employee"), CONTACT("contact"), VISITOR("visitor"), UNDEF(
            "undef"),
    IS_ADD("isAdd")
}