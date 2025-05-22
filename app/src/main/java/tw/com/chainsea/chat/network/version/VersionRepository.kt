package tw.com.chainsea.chat.network.version

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import tw.com.chainsea.ce.sdk.network.model.common.ApiErrorData
import tw.com.chainsea.ce.sdk.network.model.common.ApiResult
import tw.com.chainsea.ce.sdk.network.services.VersionService
import tw.com.chainsea.chat.BuildConfig

class VersionRepository(private val versionService: VersionService) {


    fun getVersion() = flow {
        emit(ApiResult.Loading(true))
        val response = versionService.getVersion()
        response.body()?.let {
            emit(ApiResult.Success(it))
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }
}