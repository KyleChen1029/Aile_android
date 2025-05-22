package tw.com.chainsea.ce.sdk.http.ce.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import tw.com.chainsea.android.common.log.CELog
import tw.com.chainsea.ce.sdk.config.AppConfig
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.event.EventMessage
import tw.com.chainsea.ce.sdk.http.cp.model.TransTenantInfo
import tw.com.chainsea.ce.sdk.lib.ErrCode
import tw.com.chainsea.ce.sdk.network.model.common.ApiErrorData
import tw.com.chainsea.ce.sdk.network.model.common.ApiResult
import tw.com.chainsea.ce.sdk.network.model.common.ErrorCode
import tw.com.chainsea.ce.sdk.socket.cp.CpSocket

open class BaseViewModel(private val application: Application,
    private val tokenRepository: TokenRepository) : AndroidViewModel(application) {

    val loading: MutableLiveData<Boolean> = MutableLiveData()
    val errorMessage: MutableLiveData<String> = MutableLiveData()
    val refreshTokenError =  MutableLiveData<ApiErrorData>()


    suspend fun <T> checkTokenValid(originFlow: Flow<T>): Flow<T>? = withContext(Dispatchers.IO) {
        var flow : Flow<T>? = null
        // 檢查 Token 是否過期
        val currentTime = System.currentTimeMillis()
        val ceTokenValidSecond = TokenPref.getInstance(application).ceTokenValidSecond
        val isTokenValid = ceTokenValidSecond + currentTime > System.currentTimeMillis()
        CoroutineScope(Dispatchers.IO).async {
            flow = if (isTokenValid || ceTokenValidSecond == 0L) {
                anewCeToken(originFlow)
            } else {
                refreshCeToken(originFlow)
            }
        }.await()
        return@withContext flow
    }

    /**
     * 延長 CE token 期限
     * */
    private suspend fun <T> anewCeToken(originFlow: Flow<T>?): Flow<T>? = withContext(Dispatchers.IO) {
        CELog.d("Refresh CE Token")
        var flow : Flow<T>? = null
        tokenRepository.newToken(ServiceEnum.CE).collect {
            when (it) {
                is ApiResult.Success -> {
                    flow = originFlow
                    // 刷新 token
                    TokenPref.getInstance(application).tokenId = it.data.tokenId
                    AppConfig.tokenForNewAPI = it.data.tokenId
                    // 刷新 token valid second time
                    it.data.tokenValidSeconds?.let { seconds ->
                        TokenPref.getInstance(application).ceTokenValidSecond = seconds * 1000L
                    }
                }
                is ApiResult.Failure -> {
                    CELog.e("Anew CE Token Error")
                    flow = refreshCeToken(originFlow)
                }

                else -> {
                    // nothing
                }
            }
        }
        return@withContext flow
    }

    /**
     * 更新 CE token
     * */
    private suspend fun <T> refreshCeToken(originFlow: Flow<T>? = null): Flow<T>? = withContext(Dispatchers.IO) {
        val refreshTokenId = TokenPref.getInstance(application).ceTokenRefreshId
        var flow : Flow<T>? = null
        tokenRepository.refreshToken(refreshTokenId, ServiceEnum.CE).collect {
            when (it) {
                is ApiResult.Success -> {
                    flow = originFlow
                    TokenPref.getInstance(application).tokenId = it.data.tokenId
                    AppConfig.tokenForNewAPI = it.data.tokenId
                    it.data.transTenantInfo?.let {
                        TokenPref.getInstance(application).cpTransTenantInfo = it
                    }
                }

                is ApiResult.Failure -> {
                    if (it.errorMessage.errorCode == ErrorCode.DeviceNotExist.type) {
                        refreshTokenError.postValue(it.errorMessage)
                    }
                    //refreshTokenError.postValue(it.errorMessage) //先不用強制登出，因為QA做斷網測試會強制登出變成缺陷
                    CELog.e("Refresh CE Token Error", it.errorMessage)
                }
                else -> {
                    // nothing
                }
            }
        }
        return@withContext flow
    }

    suspend fun <T> checkCpTokenValid(originFlow: Flow<T>? = null): Flow<T>? = withContext(Dispatchers.IO) {
        var flow : Flow<T>? = null
        // 檢查 Token 是否過期
        val currentTime = System.currentTimeMillis()
        val ceTokenValidSecond = TokenPref.getInstance(application).tokenValidSecond
        val isTokenValid = ceTokenValidSecond + currentTime > System.currentTimeMillis()

        runBlocking {
            // 如果沒有 cpToken 有效時間，重新拿取
            flow = if (isTokenValid || ceTokenValidSecond == 0L) {
                anewCpToken(originFlow)
            } else {
                refreshCpToken(originFlow)
            }
        }
        return@withContext flow
    }

    /**
     * 延長 CP token 期限
     * */
    private suspend fun <T> anewCpToken(originFlow: Flow<T>? = null): Flow<T>? = withContext(Dispatchers.IO) {
        CELog.d("Refresh CP Token")
        var flow : Flow<T>? = null
        tokenRepository.newToken(ServiceEnum.CP).collect {
            when (it) {
                is ApiResult.Failure -> {
                    flow = refreshCpToken(originFlow)
                }
                is ApiResult.Success -> {
                    CpSocket.getInstance().connect(
                        TokenPref.getInstance(application).cpSocketUrl,
                        TokenPref.getInstance(application).cpSocketNameSpace,
                        TokenPref.getInstance(application).cpSocketName,
                        TokenPref.getInstance(application).cpSocketDeviceId
                    )
                    // 刷新 token
                    it.data.tokenId?.let { tokenId ->
                        TokenPref.getInstance(application).cpTokenId = tokenId
                    }
                    // 刷新 token valid second time
                    it.data.tokenValidSeconds?.let { seconds ->
                        TokenPref.getInstance(application).tokenValidSecond = seconds * 1000L
                    }
                    setTransTenantInfo(it.data.transTenantInfo)
                    flow = originFlow
                }
                else -> {
                    // nothing
                }
            }
        }

        return@withContext flow
    }

    /**
     * 更新 CP token
     * */
    private suspend fun <T> refreshCpToken(originFlow: Flow<T>? = null) = withContext(Dispatchers.IO) {
        val refreshTokenId = TokenPref.getInstance(application).cpRefreshTokenId
        var flow : Flow<T>? = null
        tokenRepository.refreshToken(refreshTokenId, ServiceEnum.CP).collect {
            when (it) {
                is ApiResult.Failure -> {
                    when (it.errorMessage.errorCode) {
                        ErrCode.CP_REFRESH_TOKEN_EXPIRED.value,
                        ErrCode.CP_REFRESH_TOKEN_NOT_EXIST.value -> {
                            EventBus.getDefault().post(EventMessage(it.errorMessage.errorCode))
                        }

                        ErrCode.CP_SQUEEZED_OUT.value -> {
                            EventBus.getDefault().post(EventMessage(it.errorMessage.errorCode))
                        }
                    }
                }

                is ApiResult.Success -> {
                    TokenPref.getInstance(application).setCpTokenId(it.data.tokenId)
                    setTransTenantInfo(it.data.transTenantInfo)
                    flow = originFlow
                }

                else -> {
                    // nothing
                }
            }
        }
        return@withContext flow
    }

    //CP創建團隊回復
    private fun setTransTenantInfo(transTenantInfo: TransTenantInfo?) = viewModelScope.launch(Dispatchers.IO) {
        transTenantInfo?.let {
            TokenPref.getInstance(application).cpTransTenantInfo = it
        }
    }
}
