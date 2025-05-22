package tw.com.chainsea.chat.view.service

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.http.ce.base.BaseViewModel
import tw.com.chainsea.ce.sdk.network.model.common.ApiResult
import tw.com.chainsea.ce.sdk.http.ce.base.TokenRepository
import tw.com.chainsea.ce.sdk.http.ce.model.Member
import tw.com.chainsea.ce.sdk.network.model.response.DictionaryItems
import tw.com.chainsea.chat.R

class ServiceNumberAgentsManageViewModel(
    private val application: Application,
    private val repository: ServiceNumberAgentsManageRepository,
    private val tokenRepository: TokenRepository,
    private val serviceNumberSettingRepository: ServiceNumberSettingRepository
) : BaseViewModel(application, tokenRepository) {


    val isTenantManager = MutableLiveData<Boolean>()
    val agentsList = MutableLiveData<List<Member>>()
    val modifySuccess = MutableLiveData<Int>()

    val message = MutableLiveData<Int>()
    val timeList = MutableLiveData<List<DictionaryItems>>()
    val onUpdateServiceNumberTimeoutTime = MutableLiveData<Boolean>()

    fun getIsTenantManager() = viewModelScope.launch {
        val currentTenant = TokenPref.getInstance(application).cpCurrentTenant
        val selfUserId = TokenPref.getInstance(application).userId
        checkTokenValid(repository.getIsTenantManager(currentTenant, selfUserId))?.collect {
            isTenantManager.postValue(it)
        }
    }


    fun getServiceEntityFromRemote(serviceNumberId: String) = viewModelScope.launch {
        checkTokenValid(repository.getServiceEntityFromRemote(serviceNumberId))?.collect {
            when (it) {
                is ApiResult.Success -> {
                    agentsList.postValue(it.data)
                }

                else -> {}
            }
        }
    }

    fun getServiceEntity(serviceNumberId: String) = viewModelScope.launch {
        checkTokenValid(repository.getServiceEntity(serviceNumberId))?.collect {
            when (it) {
                is ApiResult.Success -> {
                    agentsList.postValue(it.data)
                }

                else -> {}
            }
        }
    }

    //刪除服務人員
    fun deleteAgent(serviceNumberId: String, agentId: String) = viewModelScope.launch {
        checkTokenValid(repository.removeAgent(serviceNumberId, agentId))?.collect {
            when (it) {
                is ApiResult.Success -> {
                    message.postValue(R.string.text_deleted_member_success)
                }

                is ApiResult.Failure -> {
                    message.postValue(R.string.text_toast_device_delete_failure)
                }

                else -> {}
            }
        }
    }

    //移除管理權限
    fun removeManager(serviceNumberId: String, agentId: String) = viewModelScope.launch {
        checkTokenValid(repository.removeManagement(serviceNumberId, agentId))?.collect {
            when (it) {
                is ApiResult.Success -> {
                    getServiceEntityFromRemote(serviceNumberId)
                    message.postValue(R.string.text_cancel_management_success)
                }

                is ApiResult.Failure -> {
                    message.postValue(R.string.text_cancel_management_failure)
                }

                else -> {
                }
            }
        }
    }

    //新增管理權限
    fun addManager(serviceNumberId: String, agentId: String) = viewModelScope.launch {
        checkTokenValid(repository.addManagement(serviceNumberId, agentId))?.collect {
            when (it) {
                is ApiResult.Success -> {
                    getServiceEntityFromRemote(serviceNumberId)
                    message.postValue(R.string.text_designate_management_success)
                }

                is ApiResult.Failure -> {
                    message.postValue(R.string.text_designate_management_failure)
                }

                else -> {
                }
            }
        }
    }

    //轉移擁有者權限
    fun modifyOwner(serviceNumberId: String, agentId: String) = viewModelScope.launch {
        checkTokenValid(repository.modifyOwner(serviceNumberId, agentId))?.collect {
            when (it) {
                is ApiResult.Success -> {
                    getServiceEntityFromRemote(serviceNumberId)
                    message.postValue(R.string.text_sure_to_transfer_ownership_success)
                }

                is ApiResult.Failure -> {
                    message.postValue(R.string.text_sure_to_transfer_ownership_failure)
                }
                else -> {
                }
            }
        }
    }

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