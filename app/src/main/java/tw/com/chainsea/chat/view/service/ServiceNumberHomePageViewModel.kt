package tw.com.chainsea.chat.view.service

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tw.com.chainsea.ce.sdk.http.ce.base.BaseViewModel
import tw.com.chainsea.ce.sdk.http.ce.base.TokenRepository
import tw.com.chainsea.ce.sdk.network.model.common.ApiResult
import tw.com.chainsea.ce.sdk.network.model.response.DictionaryItems

class ServiceNumberHomePageViewModel(
    application: Application,
    tokenRepository: TokenRepository,
    private val serviceNumberSettingRepository: ServiceNumberSettingRepository
) :
    BaseViewModel(application, tokenRepository) {

    val timeList = MutableLiveData<List<DictionaryItems>>()
    val onUpdateServiceNumberTimeoutTime = MutableLiveData<Boolean>()


    fun getServiceTimeOutList() = viewModelScope.launch(Dispatchers.IO) {
        serviceNumberSettingRepository.getServiceTimeOutList().collect {
            when (it) {
                is ApiResult.Success -> {
                    timeList.postValue(it.data)
                }

                else -> {
                    // nothing
                }
            }
        }
    }

    fun getServiceIdleList() = viewModelScope.launch(Dispatchers.IO) {
        serviceNumberSettingRepository.getServiceIdleList().collect {
            when (it) {
                is ApiResult.Success -> {
                    timeList.postValue(it.data)
                }

                else -> {
                    // nothing
                }
            }
        }
    }

    fun updateServiceNumberTimeoutTime(serviceNumberId: String, timeoutTime: Int) = viewModelScope.launch(Dispatchers.IO) {
        serviceNumberSettingRepository.updateServiceNumberTimeOutTime(serviceNumberId, timeoutTime).collect {
            when (it) {
                is ApiResult.Success -> {
                    onUpdateServiceNumberTimeoutTime.postValue(it.data)
                }

                is ApiResult.Failure -> {

                }

                else -> {
                    // nothing
                }
            }
        }
    }
}