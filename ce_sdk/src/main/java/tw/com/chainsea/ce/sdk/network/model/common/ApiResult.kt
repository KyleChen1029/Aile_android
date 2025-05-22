package tw.com.chainsea.ce.sdk.network.model.common

sealed class ApiResult<T> {
    data class Loading<T>(val isLoading: Boolean) : ApiResult<T>()
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Failure<T>(val errorMessage: ApiErrorData) : ApiResult<T>()
    data class NextPage<T>(val hasNextPage: Boolean) : ApiResult<T>()
    data class SaveStatus<T>(val isSuccess: Boolean): ApiResult<T>()
}

data class ApiErrorData(var errorMessage: String = "", var errorCode: String = "")


enum class ErrorCode(val type: String) {
    UNDEF("UNDEF"),
    // 服務號被禁用
    ServiceNumberDisable("Ce.ServiceNumber.Disable"),
    // 已經不是聊天室成員
    ChatMemberInvalid("Ce.ChatMember.Invalid"),
    DeviceNotExist("Ce.Device.NotExist"),
    RoomNotExist("Ce.ChatRoom.NotExist");

    companion object {
        @JvmStatic
        public fun of(flag: String): ErrorCode {
            for (e in ErrorCode.values()) {
                if (e.type == flag) {
                    return e
                }
            }
            return UNDEF
        }
    }
}