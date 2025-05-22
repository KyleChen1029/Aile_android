package tw.com.chainsea.chat.searchfilter.viewmodel


import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import tw.com.chainsea.ce.sdk.bean.servicenumber.AIConsultation
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberConsultEntity
import tw.com.chainsea.ce.sdk.http.ce.base.BaseViewModel
import tw.com.chainsea.ce.sdk.http.ce.base.TokenRepository
import tw.com.chainsea.ce.sdk.network.model.common.ApiResult
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.view.service.ServiceNumberAgentsManageRepository

class ServiceNumberConsultationViewModel(
    private val application: Application,
    private val serviceNumberAgentsManageRepository: ServiceNumberAgentsManageRepository,
    private val tokenRepository: TokenRepository
) : BaseViewModel(application, tokenRepository) {
    private var localEntities : MutableList<Any> = arrayListOf()
    val sendQueryList = MutableSharedFlow<Pair<String, List<Any>>>()
    private val searchInputText = MutableStateFlow("")
    private var serviceNumberRoomFilteredList : MutableList<Any> = arrayListOf()
    var ownerId : String = ""
    val selectedServiceNumberConsultItem = MutableSharedFlow<Pair<String, String>>()
    val selectedAiConsultationItem = MutableSharedFlow<Pair<String, String>>()
    val startedAiConsultationSuccess = MutableSharedFlow<String>()
    val startedAiConsultationFailure = MutableSharedFlow<Int>()
    val dismissLoading = MutableSharedFlow<Unit>()
    fun getAllServiceNumberConsultationList(serviceNumberId: String, activeConsultAIId: String?) = viewModelScope.launch(Dispatchers.IO) {

        if(serviceNumberId.isNotEmpty()) {
            checkTokenValid(serviceNumberAgentsManageRepository.getServiceNumberItem(serviceNumberId))?.collect {
                when(it) {
                    is ApiResult.Success -> {
                        localEntities.clear()
                        if(it.data.isEnableConsultationAI && it.data.isRobotServiceFlag) {
                            val aiConsultation = AIConsultation(name = application.getString(R.string.text_consultation_ai), activeConsultAIId?: "")
                            localEntities.add(aiConsultation)
                        }
                        showServiceNumberConsultationList()
                    }
                    is ApiResult.Failure -> {
                        showServiceNumberConsultationList()
                    }
                    else -> { }
                }
            }?:run {
                dismissLoading.emit(Unit)
            }
        }
    }

    /**
     * 取得正在諮詢的服務號列表
     * */
//    private fun checkServiceNumberChatRoomAgentServicedInfo(activeRoomId: String) = viewModelScope.launch(Dispatchers.IO) {
//        checkTokenValid(serviceNumberAgentsManageRepository.getServiceNumberChatRoomAgentServicedInfo(activeRoomId))?.collect {
//            when(it) {
//                is ApiResult.Success -> {
//                    showServiceNumberConsultationList()
//                }
//                is ApiResult.Failure -> {
//                    showServiceNumberConsultationList()
//                }
//                else -> { }
//            }
//        }
//    }

    /**
     * 取得可諮詢的服務號列表
     * */
    private fun showServiceNumberConsultationList() = viewModelScope.launch(Dispatchers.IO) {
        checkTokenValid(serviceNumberAgentsManageRepository.getServiceNumberConsultationList())?.collect {
            when(it) {
                is ApiResult.Success -> {
                    localEntities.addAll(it.data)
                }
                else -> {
                    dismissLoading.emit(Unit)
                }
            }
        }?:run {
            dismissLoading.emit(Unit)
        }
        sendQueryList.emit(Pair(searchInputText.value, localEntities))
    }

    private fun checkFilterCondition(filter: String, item: Any) : Boolean {
        when(item) {
            is ServiceNumberConsultEntity -> {
                return item.name.contains(filter, true)
            }
            is AIConsultation -> {
                return item.name.contains(filter, true)
            }
        }
        return false
    }

    fun filter(s: String) = viewModelScope.launch {
        searchInputText.value = s
        if(s.isNotEmpty()) {
            serviceNumberRoomFilteredList.clear()
            for (entity in localEntities){
                when(entity) {
                    is ServiceNumberConsultEntity -> {
                        if(checkFilterCondition(s, entity))
                            serviceNumberRoomFilteredList.add(entity)
                    }
                    is AIConsultation -> {
                        if(checkFilterCondition(s, entity))
                            serviceNumberRoomFilteredList.add(entity)
                    }
                }
            }
        }
        sendQueryList.emit(Pair(s, if(s.isNotEmpty()) serviceNumberRoomFilteredList else localEntities))
    }

    fun onSelectServiceNumberItem(keyWord: String, roomId: String) = viewModelScope.launch(Dispatchers.IO) {
        selectedServiceNumberConsultItem.emit(Pair(keyWord, roomId))
    }

    fun onSelectAiConsultation(keyWord: String, consultId: String) = viewModelScope.launch(Dispatchers.IO) {
        selectedAiConsultationItem.emit(Pair(keyWord, consultId))
    }

    fun doStartAiConsultation(srcRoomId: String) = viewModelScope.launch(Dispatchers.IO) {
        checkTokenValid(serviceNumberAgentsManageRepository.doStartServiceNUmberConsultAi(srcRoomId))?.collect {
            when(it) {
                is ApiResult.Success -> {
                    it.data.let { consultId ->
                        startedAiConsultationSuccess.emit(consultId)
                    }
                }
                is ApiResult.Failure -> {
                    startedAiConsultationFailure.emit(R.string.text_consultation_ai_failure)
                }
                else -> {}
            }
        }
    }
}