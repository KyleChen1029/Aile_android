package tw.com.chainsea.chat.network.selfprofile

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.http.ce.base.BaseViewModel
import tw.com.chainsea.ce.sdk.network.model.common.ApiResult
import tw.com.chainsea.ce.sdk.network.model.request.SelfProfileRequest
import tw.com.chainsea.ce.sdk.service.type.RefreshSource
import tw.com.chainsea.chat.R
import kotlinx.coroutines.flow.collect
import tw.com.chainsea.ce.sdk.http.ce.base.TokenRepository

open class SelfProfileViewModel(private val application: Application,
                                private val selfProfileRepository: SelfProfileRepository,
                                private val tokenRepository: TokenRepository) :
        BaseViewModel(application, tokenRepository) {

    //自己的 profile
    val selfProfileData = MutableLiveData<UserProfileEntity>()

    val mobileVisible = MutableLiveData<Boolean>()

    /**
     * 取得自己的 profile
     * */
    fun getSelfProfile(source: RefreshSource) = viewModelScope.launch {
        val selfId = TokenPref.getInstance(application).userId
        checkTokenValid(selfProfileRepository.getUserProfile(selfId, source))?.collect {
            when (it) {
                is ApiResult.Loading -> loading.postValue(it.isLoading)
                is ApiResult.Failure -> {}
                is ApiResult.Success -> selfProfileData.postValue(it.data)
                else -> {}
            }
        }
    }

    fun updateMobileVisible(visible: Boolean) = viewModelScope.launch {
        val selfId = TokenPref.getInstance(application).userId
        checkTokenValid(selfProfileRepository.updateSelfMobileVisible(
            SelfProfileRequest.UpdateProfile(selfId, visible)))?.collect {
            when (it) {
                is ApiResult.Loading -> loading.postValue(it.isLoading)
                is ApiResult.Failure -> errorMessage.postValue(application.getString(R.string.system_setting_hide_phone_error))
                is ApiResult.Success -> mobileVisible.postValue(visible)
                else -> {}
            }
        }
    }
}