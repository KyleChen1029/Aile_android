package tw.com.chainsea.ce.sdk.http.ce.base

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONObject
import tw.com.chainsea.android.common.json.JsonHelper
import tw.com.chainsea.android.common.log.CELog
import tw.com.chainsea.ce.sdk.http.cp.request.RefreshTokenRequest
import tw.com.chainsea.ce.sdk.http.cp.respone.RefreshTokenResponse
import tw.com.chainsea.ce.sdk.http.cp.respone.TokenAnewResponse
import tw.com.chainsea.ce.sdk.network.model.common.ApiErrorData
import tw.com.chainsea.ce.sdk.network.model.common.ApiResult
import tw.com.chainsea.ce.sdk.network.services.TokenService

class TokenRepository(private val cpTokenService: TokenService,
                      private val ceTokenService: TokenService) {

    /**
     * 延長 CP token 使用期限
     * */
    fun newToken(service: ServiceEnum) = flow {
//        emit(ApiResult.Loading(true))
        val response = if (service == ServiceEnum.CP ) cpTokenService.newCpToken() else ceTokenService.newCeToken()
        response.body()?.let {
            it._header_?.let {header ->
                if (header.success == true) {
                    if (header.status != null && header.status == "0000") {
                        emit(ApiResult.Success(it))
                    } else if(it.status != null && it.status == "0000") {
                        emit(ApiResult.Success(it))
                    } else {
                        emit(ApiResult.Failure<TokenAnewResponse>(ApiErrorData(header.errorMessage?: it.errorMessage ?: "", it.status!!)))
                    }
                } else {
                    emit(ApiResult.Failure<TokenAnewResponse>(ApiErrorData(header.errorMessage!!, "9999")))
                }
            }
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("newToken Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }

    /**
     * 更換 CP token
     * @param refreshToken refresh token
     * */
    fun refreshToken(refreshToken: String, service: ServiceEnum) = flow {
//        emit(ApiResult.Loading(true))
        val response = if (service == ServiceEnum.CP ) cpTokenService.refreshCpToken(RefreshTokenRequest(refreshToken)) else ceTokenService.refreshCeToken(RefreshTokenRequest(refreshToken))
        response.body()?.let {
            it._header_?.let { header ->
                val isSuccess = it.success?: true
                if (header.success == true && isSuccess) {
                    emit(ApiResult.Success(it))
                } else {
                    val apiErrorData = ApiErrorData()
                    apiErrorData.errorMessage = if (!it.errorMessage.isNullOrEmpty()) {
                        it.errorMessage
                    } else if (!header.errorMessage.isNullOrEmpty()) {
                        header.errorMessage
                    } else { "" }
                    apiErrorData.errorCode = if (!it.errorCode.isNullOrEmpty()) {
                        it.errorCode
                    } else if (!header.errorCode.isNullOrEmpty()){
                        header.errorCode
                    } else { "" }

                    emit(ApiResult.Failure<RefreshTokenResponse>(apiErrorData))
                }
            }
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("refreshToken Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }
}

enum class ServiceEnum {
    CE,CP
}