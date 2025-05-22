package tw.com.chainsea.ce.sdk.network

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tw.com.chainsea.ce.sdk.database.sp.TenantPref
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.database.sp.UserPref
import tw.com.chainsea.ce.sdk.http.ce.base.BaseViewModel
import tw.com.chainsea.ce.sdk.http.ce.base.TokenRepository
import tw.com.chainsea.ce.sdk.network.model.common.ApiResult
import tw.com.chainsea.ce.sdk.network.model.request.AuthTokenLoginRequest
import tw.com.chainsea.ce.sdk.network.model.request.BaseTokenApplyRequest
import tw.com.chainsea.ce.sdk.network.model.request.OtherLoginRequest

class APIViewModel(
    private val application: Application,
    private val apiRepository: APIRepository<BaseTokenApplyRequest>,
    private val tokenRepository: TokenRepository
) : BaseViewModel(application, tokenRepository),
    LifecycleObserver {
    fun tokenApply() =
        viewModelScope.launch {
            val startUser = TokenPref.getInstance(application).userId // workround for logout
            val sp: SharedPreferences =
                application.getSharedPreferences("aile_token", Context.MODE_PRIVATE)
            val request =
                if ("AuthTokenLogin" == sp.getString(TokenPref.PreferencesKey.LOGIN_MODE.key, "")) {
                    AuthTokenLoginRequest(
                        sp.getString(TokenPref.PreferencesKey.LOGIN_MODE.key, "")!!,
                        sp.getString(TokenPref.PreferencesKey.TENANT_CODE.key, "")!!,
                        sp.getString(TokenPref.PreferencesKey.DEVICE_TYPE.key, "")!!,
                        sp.getString(TokenPref.PreferencesKey.DEVICE_NAME.key, "")!!,
                        sp.getString(TokenPref.PreferencesKey.OS_TYPE.key, "")!!,
                        sp.getString(TokenPref.PreferencesKey.UNIQUE_ID.key, "")!!,
                        sp.getString(TokenPref.PreferencesKey.AUTH_TOKEN.key, "")!!
                    )
                } else {
                    OtherLoginRequest(
                        sp.getString(TokenPref.PreferencesKey.COUNTRY_CODE.key, "")!!,
                        sp.getString(TokenPref.PreferencesKey.DEVICE_NAME.key, "")!!,
                        sp.getString(TokenPref.PreferencesKey.DEVICE_TYPE.key, "")!!,
                        sp.getString(TokenPref.PreferencesKey.LOGIN_MODE.key, "")!!,
                        sp.getString(TokenPref.PreferencesKey.ACCOUNT_NUMBER.key, "")!!,
                        sp.getString(TokenPref.PreferencesKey.OS_TYPE.key, "")!!,
                        sp.getString(TokenPref.PreferencesKey.PASSWORD.key, "")!!,
                        sp.getString(TokenPref.PreferencesKey.CURRENT_TENANT_CODE.key, "")!!,
                        sp.getString(TokenPref.PreferencesKey.UNIQUE_ID.key, "")!!
                    )
                }

            apiRepository.tokenApply(request).collect {
                when (it) {
                    is ApiResult.Success -> {
                        if (startUser != TokenPref.getInstance(application).userId) {
                            return@collect // workround for logout
                        }

                        TokenPref.getInstance(application).setTokenId(it.data.tokenId).deviceId =
                            it.data.deviceId

                        if (it.data.user != null) {
                            TokenPref.getInstance(application).setUserId(it.data.user.id).isMute =
                                it.data.user.isMute
                        }
                        // re init user data
                        UserPref.newInstance(application)
                        val accountNumber = TokenPref.getInstance(application).accountNumber
                        val psw = TokenPref.getInstance(application).password
                        val uuid = TokenPref.getInstance(application).currentTenantId
                        TenantPref
                            .newInstance(application, uuid)
                            .setAccountID(uuid)
                            .setAccountNumber(accountNumber)
                            .setAccountPsw(psw)

                        it.data.user?.let { user ->
                            TokenPref.getInstance(application).setUserId(user.id).isMute = user.isMute
                            UserPref
                                .getInstance(application)
                                .setHasBindEmployee(user.hasBusinessSystem)
                                .setHasBusinessSystem(user.hasBusinessSystem)
                        }

                        it.data.tenantInfo?.let { tenantInfo ->
                            TokenPref
                                .getInstance(application)
                                .setEnableCall(tenantInfo.isEnableCall)
                                .tokenValidSecond =
                                System.currentTimeMillis() + tenantInfo.tokenValidSeconds * 1000L
                        }

                        it.data.configuration?.let { configuration ->
                            TokenPref
                                .getInstance(application)
                                .setSocketIoUrl(configuration.socketIoUrl)
//                            .setSocketAckEnable(configuration.enableAck)
                                .setSocketIoNameSpace(configuration.socketIoNamespace)
                        } ?: run {
                            TokenPref
                                .getInstance(application)
                                .clearByKey(TokenPref.PreferencesKey.SOCKET_IO_URL)
                                .clearByKey(TokenPref.PreferencesKey.SOCKET_ACK_ENABLE)
                                .clearByKey(TokenPref.PreferencesKey.SOCKET_IO_NAME_SPACE)
                                .clearByKey(TokenPref.PreferencesKey.CONNECT_TYPE)
                        }
                    }

                    else -> {}
                }
            }
        }
}
