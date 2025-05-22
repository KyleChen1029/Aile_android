package tw.com.chainsea.chat.view.consultai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import tw.com.chainsea.android.common.log.CELog
import tw.com.chainsea.ce.sdk.network.model.common.ApiErrorData
import tw.com.chainsea.ce.sdk.network.model.common.ApiResult
import tw.com.chainsea.ce.sdk.network.model.common.CommonResponse
import tw.com.chainsea.ce.sdk.network.model.request.ServiceNumberConsultAIMessageListRequest
import tw.com.chainsea.ce.sdk.network.model.request.ServiceNumberConsultAISendMessageRequest

class ConsultAIRepository(private val consultAIService: ConsultAIService) {


    /**
     * 發送訊息給 AI
     * @param roomId 服務號聊天室 ID
     * @param serviceNumberId 服務號 ID
     * @param content 訊息內容
     * */
    fun sendConsultAIMessage(roomId: String, serviceNumberId: String, content: String, type: String = "Action") = flow {
        emit(ApiResult.Loading(true))
        val response = consultAIService.serviceNumberConsultAISendMessage(
            ServiceNumberConsultAISendMessageRequest(roomId, serviceNumberId, content, type)
        )
        response.body()?.let {
            it._header_?.let { header ->
                if (header.success!!) {
                    emit(ApiResult.Success(it))
                } else {
                    emit(ApiResult.Failure<CommonResponse<Any>>(ApiErrorData(header.errorMessage!!, header.errorCode!!)))
                }
            }
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("sendConsultAIMessage Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }


    /**
     * 取得諮詢 AI 歷史訊息
     * @param consultId 諮詢 AI ID
     * */
    fun getAIHistoryMessageList(consultId: String) = flow {
        emit(ApiResult.Loading(true))
        val response = consultAIService.serviceNumberConsultAIMessage(
            ServiceNumberConsultAIMessageListRequest(consultId)
        )
        response.body()?.let {
            emit(ApiResult.Success(it))
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("getAIHistoryMessageList Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }
}