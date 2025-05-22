package tw.com.chainsea.chat.view.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import tw.com.chainsea.android.common.log.CELog
import tw.com.chainsea.ce.sdk.database.DBManager
import tw.com.chainsea.ce.sdk.network.model.common.ApiErrorData
import tw.com.chainsea.ce.sdk.network.model.common.ApiResult
import tw.com.chainsea.ce.sdk.network.model.request.UpdateServiceNumberRequest
import tw.com.chainsea.ce.sdk.reference.ServiceNumberReference

class ServiceNumberSettingRepository(private val serviceNumberSettingService: ServiceNumberSettingService) {


    fun getServiceTimeOutList() = flow {
        emit(ApiResult.Loading(true))
        val response = serviceNumberSettingService.getServiceTimeOutList()
        response.body()?.let {
            it._header_?.let { header ->
                if (header.success == true) {
                    emit(ApiResult.Success(it.dictionaryItems))
                }
            }
        }
        emit(ApiResult.Loading(false))
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("getServiceTimeOutList Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }



    fun getServiceIdleList() = flow {
        emit(ApiResult.Loading(true))
        val response = serviceNumberSettingService.getServiceIdleList()
        response.body()?.let {
            it._header_?.let { header ->
                if (header.success == true) {
                    emit(ApiResult.Success(it.dictionaryItems))
                }
            }
        }
        emit(ApiResult.Loading(false))
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("getServiceIdleList Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }

    fun updateServiceNumberTimeOutTime(serviceNumberId: String, timeOutTime: Int) = flow {
        emit(ApiResult.Loading(true))
        val response = serviceNumberSettingService.updateServiceNumber(UpdateServiceNumberRequest(serviceNumberId, timeOutTime))
        response.body()?.let {
            it._header_?.let { header ->
                if (header.success == true) {
                    val result = ServiceNumberReference.updateTimeOutTime(serviceNumberId, timeOutTime)
                    emit(ApiResult.Success(result))
                } else {
                    emit(ApiResult.Failure<Boolean>(ApiErrorData(it.errorMessage ?: "", it.errorCode ?: "")))
                }
            }
        }
        emit(ApiResult.Loading(false))
    } .flowOn(Dispatchers.IO).catch { e ->
        CELog.e("updateServiceNumberTimeOutTime Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }
}