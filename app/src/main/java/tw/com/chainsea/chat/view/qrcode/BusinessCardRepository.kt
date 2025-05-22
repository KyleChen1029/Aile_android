package tw.com.chainsea.chat.view.qrcode

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import tw.com.chainsea.android.common.log.CELog
import tw.com.chainsea.ce.sdk.network.model.common.ApiErrorData
import tw.com.chainsea.ce.sdk.network.model.common.ApiResult
import tw.com.chainsea.ce.sdk.network.model.request.GetBusinessCardRequest
import tw.com.chainsea.ce.sdk.network.services.BusinessCardService

class BusinessCardRepository(private val businessCardService: BusinessCardService) {


    fun getServiceNumberBusinessCard(serviceNumberId: String) = flow {
        emit(ApiResult.Loading(true))
        val response =
            businessCardService.getServiceNumberBusinessCard(GetBusinessCardRequest(serviceNumberId))
        response.body()?.let {
            emit(ApiResult.Success(it.businessCardUrl))
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("businessCardService Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }
}