package tw.com.chainsea.ce.sdk.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import tw.com.chainsea.ce.sdk.network.model.common.ApiErrorData
import tw.com.chainsea.ce.sdk.network.model.common.ApiResult
import tw.com.chainsea.ce.sdk.network.services.APIService


class APIRepository<in BaseTokenApplyRequest>(private val apiService: APIService<BaseTokenApplyRequest>) {


    /**
     *
     * */
    fun tokenApply(request: BaseTokenApplyRequest) = flow {
        emit(ApiResult.Loading(true))
        val response = apiService.tokenApply(request)
        response.body()?.let {
            emit(ApiResult.Success(it))
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        emit(ApiResult.Failure(ApiErrorData(e.message!!, e.stackTrace[0].toString())))
    }
}