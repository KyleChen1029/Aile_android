package tw.com.chainsea.chat.network.logindevices

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import tw.com.chainsea.ce.sdk.network.model.common.ApiErrorData
import tw.com.chainsea.ce.sdk.network.model.common.ApiResult
import tw.com.chainsea.ce.sdk.network.model.request.AutoLoginRequest
import tw.com.chainsea.ce.sdk.network.model.request.DeleteLoginDeviceRequest
import tw.com.chainsea.ce.sdk.network.model.request.ForceLogoutDeviceRequest
import tw.com.chainsea.ce.sdk.network.services.LoginDevicesService

class LoginDevicesRepository(
    private val loginDevicesCpService: LoginDevicesService
) {
    fun getLoginDevicesList() = flow {
        emit(ApiResult.Loading(true))
        val response = loginDevicesCpService.getDeviceRecordList()
        response.body()?.let {
            it.status?.let { status ->
                if(status == "0000") {
                    it.items?.let { items ->
                        emit(ApiResult.Success(items))
                    }
                }
            }
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        emit(ApiResult.Failure(ApiErrorData(e.message!!, e.stackTrace[0].toString())))
    }

    fun doForceLogoutDevice(deviceId: String) = flow {
        emit(ApiResult.Loading(true))
        val response = loginDevicesCpService.forceLogoutDevice(ForceLogoutDeviceRequest(deviceId))
        response.body()?.let {
            it.status?.let { status ->
                emit(ApiResult.Success(status == "0000"))
            }
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        emit(ApiResult.Failure(ApiErrorData(e.message!!, e.stackTrace[0].toString())))
    }

    fun doCancelAutoLogin(id: String) = flow {
        emit(ApiResult.Loading(true))
        val response = loginDevicesCpService.removeDeviceRecordRememberMe(AutoLoginRequest(id))
        response.body()?.let {
            it.status?.let { status ->
                emit(ApiResult.Success(status == "0000"))
            }
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        emit(ApiResult.Failure(ApiErrorData(e.message!!, e.stackTrace[0].toString())))
    }
    fun doAllowAutoLogin(id: String) = flow {
        emit(ApiResult.Loading(true))
        val response = loginDevicesCpService.addDeviceRecordRememberMe(AutoLoginRequest(id))
        response.body()?.let {
            it.status?.let { status ->
                emit(ApiResult.Success(status == "0000"))
            }
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        emit(ApiResult.Failure(ApiErrorData(e.message!!, e.stackTrace[0].toString())))
    }

    fun doDeleteLoginDevice(id: String, deviceId: String?) = flow {
        emit(ApiResult.Loading(true))
        val response = loginDevicesCpService.deleteDeviceRecord(DeleteLoginDeviceRequest(id, deviceId))
        response.body()?.let {
            it.status?.let { status ->
                emit(ApiResult.Success(status == "0000"))
            }
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        emit(ApiResult.Failure(ApiErrorData(e.message!!, e.stackTrace[0].toString())))
    }
}