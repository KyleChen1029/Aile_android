package tw.com.chainsea.chat.network.apicount

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import tw.com.chainsea.android.common.log.CELog
import tw.com.chainsea.ce.sdk.network.model.common.ApiErrorData
import tw.com.chainsea.ce.sdk.network.model.common.ApiResult
import tw.com.chainsea.ce.sdk.network.model.request.ApiDataCountRequest
import tw.com.chainsea.ce.sdk.network.services.ApiDataCountService

class ApiDataCountRepository(private val syncApiDataCountService: ApiDataCountService) {


    fun getApiDataCount(queryType: String, refreshTime: Long) = flow {
        emit(ApiResult.Loading(true))
        val response = syncApiDataCountService.getApiDataCount(ApiDataCountRequest(queryType, refreshTime))
        response.body()?.let {
            emit(ApiResult.Success(it.count))
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("getApiDataCount Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }
}