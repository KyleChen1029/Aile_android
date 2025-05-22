package tw.com.chainsea.chat.view.roomList.serviceRoomList

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberStatus
import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberType
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.http.ce.base.BaseViewModel
import tw.com.chainsea.ce.sdk.http.ce.base.TokenRepository
import tw.com.chainsea.ce.sdk.network.model.common.ApiResult
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference
import tw.com.chainsea.chat.App
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.lib.NetworkUtils
import tw.com.chainsea.chat.view.base.PreloadStepEnum
import tw.com.chainsea.chat.view.chat.ChatRepository
import tw.com.chainsea.chat.view.roomList.serviceRoomList.adapter.ActionBean

class ServiceNumberRoomViewModel(
    private val application: Application,
    private val tokenRepository: TokenRepository,
    private val serviceNumberRoomRepository: ServiceNumberRoomRepository,
    private val chatRepository: ChatRepository
) : BaseViewModel(application, tokenRepository) {
    private val selfUserId by lazy { TokenPref.getInstance(application).userId }
    private val _serviceNumberListData = mutableListOf<ServiceNumberListModel>()
    val serviceNumberListData: MutableLiveData<MutableList<ServiceNumberListModel>> = MutableLiveData(_serviceNumberListData)

    private val groupOpenList = HashMap<String, String>()
    private val mutex = Mutex()
    val refreshGroup = MutableLiveData<Int>()

    /**
     * 從資料庫取得服務號資料
     * @param sortType 排序方式
     * */
    fun getServiceNumberFromDb(sortType: ServiceNumberSortType = ServiceNumberSortType.BY_GROUP) =
        viewModelScope.async {
            mutex.lock()
            serviceNumberRoomRepository.getServiceNumberFromDb(selfUserId).collect {
                when (it) {
                    is ApiResult.Success -> {
                        when (sortType) {
                            ServiceNumberSortType.BY_GROUP -> {
                                formatServiceNumberGroup(sortType, it.data).await()
                                getServiceNumberRoomFromDb(sortType).await()
                                getServiceNumberControlList().await()
                            }

                            ServiceNumberSortType.BY_TIME -> {
                                formatServiceNumberGroup(sortType).await()
                                getServiceNumberRoomFromDb(sortType).await()
                            }
                        }
                        mutex.unlock()
                    }

                    is ApiResult.Failure -> {
                        mutex.unlock()
                    }

                    else -> {
                        // nothing
                    }
                }
            }
        }

    /**
     * 從 API 取得AI服務聊天室
     * */
    fun getRobotServicingCatRoomFromServer(sortType: ServiceNumberSortType): Deferred<Unit> =
        viewModelScope.async(Dispatchers.IO) {
            mutex.lock()
            _serviceNumberListData.removeIf { it.type == ServiceNumberListType.AIService || it.type == ServiceNumberListType.MonitorAI }
            checkTokenValid(serviceNumberRoomRepository.getRobotServicingCatRoomFromServer())?.collect {
                when (it) {
                    is ApiResult.Success -> {
                        getServiceNumberRoomFromDb(sortType).await()
                        mutex.unlock()
                    }

                    is ApiResult.NextPage -> {
                        if (it.hasNextPage) {
                            getRobotServicingCatRoomFromServer(sortType).await()
                        }
                    }

                    is ApiResult.Failure -> {
                        mutex.unlock()
                    }

                    else -> {
                        // nothing
                    }
                }
            }
        }

    fun onRobotStop(onStopRoomId: String?) =
        viewModelScope.launch {
            if (onStopRoomId.isNullOrEmpty()) return@launch
            val copyList = _serviceNumberListData.deepCopy().toMutableList()
            val listIterator = copyList.iterator()
            while (listIterator.hasNext()) {
                val group = listIterator.next()
                if (group.type == ServiceNumberListType.AIService || group.type == ServiceNumberListType.MonitorAI) {
                    group.serviceNumberEndServiceChatRoom.removeIf { it.id == onStopRoomId }
                    if (group.serviceNumberEndServiceChatRoom.isEmpty()) {
                        listIterator.remove()
                    }
                }
            }
            serviceNumberListData.postValue(copyList)
        }

    fun getServicedChatRoomFromServer(sortType: ServiceNumberSortType) =
        viewModelScope.launch(Dispatchers.IO) {
            if (NetworkUtils.isNetworkAvailable(application)) {
                checkTokenValid(chatRepository.getAllChatRoomList(selfUserId, PreloadStepEnum.SYNC_DONE))?.collect {
                    when (it) {
                        is ApiResult.NextPage, is ApiResult.Failure -> {
                            getServiceNumberFromDb(sortType).await()
                            getRobotServicingCatRoomFromServer(sortType).await()
                        }

                        else -> {
                            // nothing
                        }
                    }
                }
            } else {
                getServiceNumberFromDb(sortType).await()
            }
        }

    /**
     * 重新組裝服務號資料
     * @param sortType 排序方式
     * @param serviceNumberList 服務號清單
     * */
    private fun formatServiceNumberGroup(
        sortType: ServiceNumberSortType,
        serviceNumberList: MutableList<ServiceNumberEntity> = mutableListOf()
    ) = viewModelScope.async(Dispatchers.IO) {
        _serviceNumberListData.clear()
        when (sortType) {
            ServiceNumberSortType.BY_GROUP -> {
                serviceNumberList.forEach {
                    val serviceNumberListModel =
                        ServiceNumberListModel(
                            id = it.serviceNumberId,
                            type = ServiceNumberListType.ServiceNumberGroup,
                            serviceNumberEntity = it,
                            serviceNumberIcon = if (it.serviceOpenType.contains("O")) R.drawable.ic_slice_o else R.drawable.ic_slice_i,
                            unReadNum = it.unreadNumber,
                            isOpen = groupOpenList[it.serviceNumberId] != null
                        )
                    _serviceNumberListData.add(serviceNumberListModel)
                }
            }

            ServiceNumberSortType.BY_TIME -> {
                val serviceNumberListModel =
                    ServiceNumberListModel(
                        id = ServiceNumberListType.Other.name,
                        type = ServiceNumberListType.Other
                    )
                _serviceNumberListData.add(serviceNumberListModel)
            }
        }
    }

    /**
     * AI 服務的條件
     * @param chatRoomEntity 聊天室
     * */
    private suspend fun isAiServiced(chatRoomEntity: ChatRoomEntity): Boolean =
        withContext(Dispatchers.IO) {
            return@withContext ServiceNumberStatus.ROBOT_SERVICE == chatRoomEntity.serviceNumberStatus &&
                !chatRoomEntity.isDeleted &&
                !chatRoomEntity.isWarned &&
                chatRoomEntity.serviceNumberAgentId.isNullOrEmpty()
        }

    /**
     * 監控 AI 服務的條件
     * @param chatRoomEntity 聊天室
     * */
    private suspend fun isMonitorAi(chatRoomEntity: ChatRoomEntity): Boolean =
        withContext(Dispatchers.IO) {
            return@withContext ServiceNumberStatus.ROBOT_SERVICE == chatRoomEntity.serviceNumberStatus &&
                !chatRoomEntity.isDeleted &&
                chatRoomEntity.isWarned &&
                chatRoomEntity.serviceNumberAgentId.isNullOrEmpty()
        }

    /**
     * 剛進線的條件
     * @param chatRoomEntity 聊天室
     * */
    private suspend fun isUnService(chatRoomEntity: ChatRoomEntity): Boolean =
        withContext(Dispatchers.IO) {
            // 商務號的擁有者 不會出現在服務號列表
            if (ServiceNumberType.BOSS == chatRoomEntity.serviceNumberType) {
                // 是商務號且不是擁有者
                if (!chatRoomEntity.serviceNumberOwnerId.isNullOrEmpty()) {
                    if (chatRoomEntity.serviceNumberAgentId.isNullOrEmpty() &&
                        chatRoomEntity.serviceNumberOwnerId != selfUserId
                    ) {
                        if (ServiceNumberStatus.ON_LINE == chatRoomEntity.serviceNumberStatus && chatRoomEntity.unReadNum >= 0) {
                            return@withContext true
                        }
                        if (ServiceNumberStatus.TIME_OUT == chatRoomEntity.serviceNumberStatus && chatRoomEntity.unReadNum > 0) {
                            return@withContext true
                        }
                    }
                }
            } else {
                // 不是商務號
                if (chatRoomEntity.serviceNumberAgentId.isNullOrEmpty()) {
                    if (ServiceNumberStatus.ON_LINE == chatRoomEntity.serviceNumberStatus && chatRoomEntity.unReadNum >= 0) {
                        return@withContext true
                    }

                    if (ServiceNumberStatus.TIME_OUT == chatRoomEntity.serviceNumberStatus && chatRoomEntity.unReadNum > 0) {
                        return@withContext true
                    }
                }
            }
            return@withContext false
        }

    /**
     * 我服務中的條件
     * @param chatRoomEntity 聊天室
     * */
    private suspend fun isMyService(chatRoomEntity: ChatRoomEntity): Boolean =
        withContext(Dispatchers.IO) {
            if (chatRoomEntity.unReadNum == -1 && ServiceNumberStatus.OFF_LINE_or_TIME_OUT.contains(chatRoomEntity.serviceNumberStatus)) return@withContext true
            if (selfUserId == chatRoomEntity.serviceNumberAgentId && ServiceNumberType.PROFESSIONAL == chatRoomEntity.serviceNumberType && ServiceNumberStatus.ON_LINE == chatRoomEntity.serviceNumberStatus && chatRoomEntity.unReadNum >= 0) return@withContext true
            if (selfUserId == chatRoomEntity.serviceNumberAgentId && ServiceNumberType.PROFESSIONAL == chatRoomEntity.serviceNumberType && ServiceNumberStatus.TIME_OUT == chatRoomEntity.serviceNumberStatus && chatRoomEntity.unReadNum > 0) return@withContext true
            if (selfUserId == chatRoomEntity.serviceNumberAgentId &&
                ServiceNumberStatus.OFF_LINE == chatRoomEntity.serviceNumberStatus &&
                (
                    chatRoomEntity.lastMessage != null &&
                        chatRoomEntity.lastMessage.content != null &&
                        chatRoomEntity.lastMessage.content.contains(application.getString(R.string.text_service_time_out))
                ) &&
                chatRoomEntity.unReadNum >= 0
            ) {
                return@withContext true
            }
            return@withContext false
        }

    /**
     * 服務中的條件
     * @param chatRoomEntity 聊天室
     * */
    private suspend fun isServiced(chatRoomEntity: ChatRoomEntity): Boolean =
        withContext(Dispatchers.IO) {
            if (selfUserId == chatRoomEntity.serviceNumberAgentId &&
                ServiceNumberType.PROFESSIONAL != chatRoomEntity.serviceNumberType &&
                ServiceNumberStatus.ON_LINE == chatRoomEntity.serviceNumberStatus &&
                chatRoomEntity.unReadNum >= 0
            ) {
                return@withContext true
            }

            if (selfUserId == chatRoomEntity.serviceNumberAgentId &&
                ServiceNumberType.PROFESSIONAL != chatRoomEntity.serviceNumberType &&
                ServiceNumberStatus.TIME_OUT == chatRoomEntity.serviceNumberStatus &&
                chatRoomEntity.unReadNum > 0
            ) {
                return@withContext true
            }

            if (!chatRoomEntity.serviceNumberAgentId.isNullOrEmpty() &&
                ServiceNumberStatus.ON_LINE == chatRoomEntity.serviceNumberStatus &&
                chatRoomEntity.unReadNum >= 0
            ) {
                return@withContext true
            }

            if (!chatRoomEntity.serviceNumberAgentId.isNullOrEmpty() &&
                ServiceNumberStatus.TIME_OUT == chatRoomEntity.serviceNumberStatus &&
                chatRoomEntity.unReadNum > 0
            ) {
                return@withContext true
            }

            // 條件∶商務號擁有者接手服務，其他成員顯示服務中
            if (!chatRoomEntity.serviceNumberAgentId.isNullOrEmpty() &&
                selfUserId != chatRoomEntity.serviceNumberAgentId &&
                chatRoomEntity.unReadNum >= 0 &&
                ServiceNumberStatus.ON_LINE == chatRoomEntity.serviceNumberStatus &&
                chatRoomEntity.serviceNumberType == ServiceNumberType.BOSS
            ) {
                return@withContext true
            }

            return@withContext false
        }

    /**
     * 從資料庫取得服務號中的聊天室
     * @param sortType 排序方式
     * */
    private fun getServiceNumberRoomFromDb(sortType: ServiceNumberSortType) =
        viewModelScope.async(Dispatchers.IO) {
            when (sortType) {
                ServiceNumberSortType.BY_GROUP -> {
                    serviceNumberRoomRepository.getServiceNumberChatRoomFromDb(selfUserId).collect {
                        when (it) {
                            is ApiResult.Success -> {
                                addServiceNumberChatRoomToGroup(sortType, it.data).await()
                            }

                            else -> {
                                // nothing
                            }
                        }
                    }
                }

                ServiceNumberSortType.BY_TIME -> {
                    serviceNumberRoomRepository.getServiceNumberChatRoomFromDbByTime().collect { other ->
                        when (other) {
                            is ApiResult.Success -> {
                                // 服務號分組聊天室
                                addServiceNumberChatRoomToGroup(sortType, other.data).await()

                                serviceNumberRoomRepository.getServiceNumberChatRoomFromDb(selfUserId).collect {
                                    when (it) {
                                        is ApiResult.Success -> {
                                            // 服務號分組以外的聊天室
                                            addServiceNumberChatRoomToGroup(sortType, it.data).await()
                                        }

                                        else -> {
                                            // nothing
                                        }
                                    }
                                }
                            }

                            else -> {
                                // nothing
                            }
                        }
                    }
                }
            }
        }

    /**
     * 將各個聊天室資料加入到已經整理好的服務號資料中
     * @param sortType 排序方式
     * @param chatRoomList 聊天室資料
     * */
    private fun addServiceNumberChatRoomToGroup(
        sortType: ServiceNumberSortType,
        chatRoomList: MutableList<ChatRoomEntity>
    ) = viewModelScope.async(Dispatchers.IO) {
        val iterator = chatRoomList.iterator()
        val bossServiceNumberId = TokenPref.getInstance(application).bossServiceNumberId

        while (iterator.hasNext()) {
            val chatRoom = iterator.next()
            if (chatRoom.ownerId == selfUserId) continue

            // 修正如果從普通成員/管理員轉移成商務號擁有者 列表上還會有該服務號聊天室殘留
            if (chatRoom.serviceNumberType == ServiceNumberType.BOSS) {
                if (chatRoom.serviceNumberId == bossServiceNumberId) {
                    iterator.remove()
                    continue
                }
            }

            // 將聊天室依照條件放到群組裡，要注要先後順序
            // AI服務
            if (isAiServiced(chatRoom)) {
                addCurrentGroupChatRoomList(ServiceNumberListType.AIService, chatRoom).await()
                iterator.remove()
                continue
            }

            // 監控 AI
            if (isMonitorAi(chatRoom)) {
                addCurrentGroupChatRoomList(ServiceNumberListType.MonitorAI, chatRoom).await()
                iterator.remove()
                continue
            }

            // 剛進線
            if (isUnService(chatRoom)) {
                addCurrentGroupChatRoomList(ServiceNumberListType.UnService, chatRoom).await()
                iterator.remove()
                continue
            }

            // 我的服務
            if (isMyService(chatRoom)) {
                addCurrentGroupChatRoomList(ServiceNumberListType.MyService, chatRoom).await()
                val serviceNumberServicingChatRoom = _serviceNumberListData.find { it.id == chatRoom.serviceNumberId }?.serviceNumberServicingChatRoom ?: mutableListOf()
                if (!serviceNumberServicingChatRoom.any { chatRoom.id == it.id }) {
                    serviceNumberServicingChatRoom.add(chatRoom)
                } else {
                    serviceNumberServicingChatRoom[serviceNumberServicingChatRoom.indexOf(chatRoom)] = chatRoom
                }
                iterator.remove()
                continue
            }

            // 服務中
            if (isServiced(chatRoom)) {
                addCurrentGroupChatRoomList(ServiceNumberListType.Serviced, chatRoom).await()
                val serviceNumberServicingChatRoom = _serviceNumberListData.find { it.id == chatRoom.serviceNumberId }?.serviceNumberServicingChatRoom ?: mutableListOf()
                if (!serviceNumberServicingChatRoom.any { chatRoom.id == it.id }) {
                    serviceNumberServicingChatRoom.add(chatRoom)
                } else {
                    serviceNumberServicingChatRoom[serviceNumberServicingChatRoom.indexOf(chatRoom)] = chatRoom
                }
                iterator.remove()
                continue
            }
        }

        when (sortType) {
            ServiceNumberSortType.BY_GROUP -> {
                _serviceNumberListData.forEach {
                    // 在群組上顯示該服務號全部的未讀
                    it.serviceNumberEntity?.let { serviceNumberEntity ->
                        val result =
                            ChatRoomReference.getInstance().queryServiceRoomByServiceNumberId(
                                serviceNumberEntity.serviceNumberId,
                                0,
                                0
                            )

                        val unServiceList = _serviceNumberListData.find { it.type == ServiceNumberListType.UnService }
                        val data =
                            result.filter { item ->
                                unServiceList?.let {
                                    it.serviceNumberEndServiceChatRoom?.none { unItem ->
                                        item.id == unItem.id
                                    }
                                } ?: true
                            }
                        // 下方服務號的未讀
                        it.unReadNum = data.sumOf { it.unReadNum }
                    } ?: run {
                        // 剛進件、我服務中、服務中的未讀
                        it.unReadNum = it.serviceNumberEndServiceChatRoom.sumOf { it.unReadNum }
                    }
                }
            }

            ServiceNumberSortType.BY_TIME -> {
                _serviceNumberListData.forEach {
                    if (it.type == ServiceNumberListType.Other) {
                        it.serviceNumberEndServiceChatRoom.addAll(chatRoomList)
                        it.unReadNum = ChatRoomReference.getInstance().getAllServiceNumberChatRoomUnreadNumber()
                    } else {
                        // 在群組上顯示該服務號全部的未讀
                        it.unReadNum = it.serviceNumberEndServiceChatRoom.sumOf { it.unReadNum }
                    }
                }
            }
        }

        _serviceNumberListData.sortBy { it.type.ordinal }
        serviceNumberListData.postValue(_serviceNumberListData.toMutableList())
    }

    /**
     * 新增對應的服務號群組下的聊天室資料
     * @param type 群組的 type
     * @see ServiceNumberListType
     * */
    private fun addCurrentGroupChatRoomList(
        type: ServiceNumberListType,
        chatRoomEntity: ChatRoomEntity
    ) = viewModelScope.async(Dispatchers.IO) {
        _serviceNumberListData.find { it.type == type }?.let {
            val index = it.serviceNumberEndServiceChatRoom.indexOf(chatRoomEntity)
            if (index == -1) {
                it.serviceNumberEndServiceChatRoom.add(chatRoomEntity)
            } else {
                it.serviceNumberEndServiceChatRoom.set(index, chatRoomEntity)
            }
        } ?: run {
            val serviceNumberListModel =
                ServiceNumberListModel(
                    id = type.name,
                    type = type
                )
            serviceNumberListModel.isOpen = groupOpenList[type.name] != null
            serviceNumberListModel.serviceNumberEndServiceChatRoom.add(chatRoomEntity)
            _serviceNumberListData.add(serviceNumberListModel)
        }
    }

    /**
     * 清除服務號下聊天室的未讀
     * @param roomId 需要清除的聊天室 ID
     * */
    fun clearChatRoomUnread(roomId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            _serviceNumberListData.forEachIndexed { index, serviceNumberListModel ->
                serviceNumberListModel.serviceNumberEndServiceChatRoom.forEach { chatRoom ->
                    if (chatRoom.id == roomId) {
                        chatRoom.unReadNum = 0
                        // 在群組上顯示該服務號全部的未讀
                        serviceNumberListModel.unReadNum = serviceNumberListModel.serviceNumberEndServiceChatRoom.sumOf { it.unReadNum }
                        refreshGroup.postValue(index)
                    }
                }
            }
        }

    /**
     * 取得服務號下方控制項(主頁、聊天、成員等)
     * */
    private fun getServiceNumberControlList() =
        viewModelScope.async(Dispatchers.IO) {
            App.getInstance().serviceChatRoom.clear()
            _serviceNumberListData.forEach {
                it.serviceNumberControl[ActionBean.CHAT.index] = ActionBean.CHAT
                it.serviceNumberControl[ActionBean.HOME.index] = ActionBean.HOME
                it.serviceNumberControl[ActionBean.MEMBERS.index] = ActionBean.MEMBERS

                // 判斷現在服務中的聊天室是否有發出換手
                it.serviceNumberServicingChatRoom.forEach lit@{ chatRoom ->
                    if (chatRoom.isTransferFlag) {
                        App.getInstance().serviceChatRoom.add(chatRoom)
                        val currentTransferServiceNumber = _serviceNumberListData.find { chatRoom.serviceNumberId == it.id }
                        currentTransferServiceNumber?.serviceNumberControl?.put(ActionBean.WAIT_TRANSFER.index, ActionBean.WAIT_TRANSFER)
                        return@lit
                    }
                }
            }
        }

    /**
     * 儲存已經展開的群組
     * @param serviceNumberListType 群組的 type
     * @param serviceNumberId 群組的服務號 ID
     */
    fun addGroupOpenList(
        serviceNumberListType: ServiceNumberListType,
        serviceNumberId: String?
    ) {
        serviceNumberId?.let {
            groupOpenList[it] = serviceNumberListType.name
        } ?: run {
            groupOpenList[serviceNumberListType.name] = ""
        }
    }

    /**
     * 移除已經展開的群組
     * @param serviceNumberListType 群組的 type
     * @param serviceNumberId 群組的服務號 ID
     */
    fun removeGroupOpenList(
        serviceNumberListType: ServiceNumberListType,
        serviceNumberId: String?
    ) {
        serviceNumberId?.let {
            groupOpenList.remove(it)
        } ?: run {
            groupOpenList.remove(serviceNumberListType.name)
        }
    }
}
