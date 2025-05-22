package tw.com.chainsea.chat.searchfilter.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.common.collect.Lists
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import tw.com.aile.sdk.bean.filter.FilterRoomType
import tw.com.aile.sdk.bean.filter.FilterTab
import tw.com.aile.sdk.bean.filter.SearchFilterEntity
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.ce.sdk.bean.ServiceNum
import tw.com.chainsea.ce.sdk.bean.msg.MessageType
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.searchfilter.repository.ChatRoomSearchRepository
import tw.com.chainsea.chat.searchfilter.repository.SearchFilterSharedRepository
import tw.com.chainsea.chat.searchfilter.repository.ServiceNumberSearchRepository

class ServiceNumberSearchViewModel(
    val application: Application,
    private val serviceNumberSearchRepository: ServiceNumberSearchRepository,
    private val chatRoomSearchRepository: ChatRoomSearchRepository,
    private val searchFilterSharedRepository: SearchFilterSharedRepository,
) : ViewModel() {
    private var localEntities : MutableList<ServiceNum> = arrayListOf()
    private val searchKeyWord = MutableStateFlow("")
    private var serviceNumberFilteredSectionedList : MutableList<SearchFilterEntity> = arrayListOf() //filtered data
    private var serviceNumberSectionedList : MutableList<SearchFilterEntity> = arrayListOf() //all data
    val sendFilterQueryList = MutableSharedFlow<Triple<String, MutableList<SearchFilterEntity>, Int>>()
    val navigateToChatRoom = MutableSharedFlow<Pair<String, Any>>()
    private var allNewsGrouped : Map<String?, List<MessageEntity>> = mapOf()
    val navigateToMessageList =  MutableSharedFlow<Triple<String, List<MessageEntity>, ChatRoomEntity>>()
    private var localServicesEntities : MutableList<ChatRoomEntity> = arrayListOf()
    var ownerId = ""
    val sendServiceNumberSelectedItem = MutableSharedFlow<Pair<String, Any>>()
    val sendSelectedItem = MutableSharedFlow<Pair<String, MutableList<SearchFilterEntity>>>()
    fun getAllServiceNumber(userId: String) = viewModelScope.launch(Dispatchers.IO) {
        ownerId = userId
        val chatRoomTypeList = Lists.newArrayList<ChatRoomType>()
        chatRoomTypeList.apply {
            add(ChatRoomType.services)
            add(ChatRoomType.serviceMember)
            add(ChatRoomType.subscribe)
        }
        localEntities = serviceNumberSearchRepository.queryAllSubscribeServiceNumbers().single().toMutableList()

        chatRoomSearchRepository.getAllChatRoomEntities(userId, chatRoomTypeList).onEach {
            localServicesEntities = it
        }.collect()
        showAllData()
    }

    private fun checkFilterCondition(filter: String, item: Any) : Boolean {
        return when(item) {
            is ServiceNum -> item.name.contains(filter, true)
            is ChatRoomEntity ->
                (item.serviceNumberName.contains(filter, true) ||
                        item.name.contains(filter, true))
            is MessageEntity -> item.getContent(item.content).contains(filter, true)
            else -> { false }
        }
    }

    /**
     * 轉發分享的過濾
     **/
    fun filter(s: String) = viewModelScope.launch(Dispatchers.IO) {
        searchKeyWord.value = s
        val subscribeServiceNumberList: MutableList<ServiceNum> = mutableListOf()
        if(s.isNotEmpty()) {
            serviceNumberFilteredSectionedList.clear()
            for (entity in localEntities) {
                if (checkFilterCondition(s, entity))
                    subscribeServiceNumberList.add(entity)
            }

            if (subscribeServiceNumberList.isNotEmpty()) {
                serviceNumberFilteredSectionedList.add(
                    SearchFilterEntity(
                        application.getString(
                            R.string.text_sectioned_search_service_number_filter,
                            subscribeServiceNumberList.size
                        ),
                        FilterRoomType.SUBSCRIBE_SERVICE_NUMBER,
                        FilterTab.SERVICE_NUMBER,
                        subscribeServiceNumberList
                    )
                )
            }
        }
        sendFilterQueryList.emit(Triple(s, if(s.isNotEmpty()) serviceNumberFilteredSectionedList else Lists.newArrayList(), subscribeServiceNumberList.size))
    }

    /**
    * 全局搜尋的過濾
    **/
    fun globalFilter(s: String, userId: String) = viewModelScope.launch(Dispatchers.IO) {
        searchKeyWord.value = s
        val subscribeServiceNumberList: MutableList<ServiceNum> = mutableListOf()
        val servicesChatRooms: MutableList<ChatRoomEntity> = mutableListOf()
        val newsRooms: MutableList<MessageEntity> = mutableListOf()
        val eachNewsRooms : MutableList<ChatRoomEntity> = mutableListOf()
        val chatRoomTypeList = Lists.newArrayList<ChatRoomType>()
        chatRoomTypeList.apply {
            add(ChatRoomType.services)
            add(ChatRoomType.serviceMember)
            add(ChatRoomType.subscribe)
        }

        if(s.isNotEmpty()) {
            serviceNumberFilteredSectionedList.clear()
            serviceNumberSearchRepository.queryAllSubscribeServiceNumbersByName(s).onEach {
                subscribeServiceNumberList.addAll(it)
            }.collect()

            chatRoomSearchRepository.queryServiceNumberChatRoomByTypeAndName(
                userId,
                chatRoomTypeList,
                s
            ).onEach {
                servicesChatRooms.addAll(it)
            }.collect()

            //消息過濾
            val messageTypeList = Lists.newArrayList<MessageType>()
            messageTypeList.apply {
                add(MessageType.AT)
                add(MessageType.TEXT)
            }
            searchFilterSharedRepository.getAllMessageEntitiesByKeyWordForServiceNumber(messageTypeList, s).onEach { list ->
                newsRooms.addAll(list)
            }.collect()

            if (subscribeServiceNumberList.isNotEmpty()) {
                serviceNumberFilteredSectionedList.add(
                    SearchFilterEntity(
                        application.getString(
                            R.string.text_sectioned_search_service_number_filter,
                            subscribeServiceNumberList.size
                        ),
                        FilterRoomType.SUBSCRIBE_SERVICE_NUMBER,
                        FilterTab.SERVICE_NUMBER,
                        subscribeServiceNumberList
                    )
                )
            }
            if(servicesChatRooms.isNotEmpty()) {
                serviceNumberFilteredSectionedList.add(
                    SearchFilterEntity(
                        application.getString(
                            R.string.text_sectioned_search_service_number_member,
                            servicesChatRooms.size
                        ),
                        FilterRoomType.SERVICE_NUMBER_CHATROOM,
                        FilterTab.SERVICE_NUMBER,
                        servicesChatRooms
                    )
                )
            }
            if(newsRooms.isNotEmpty()) {
                allNewsGrouped = newsRooms.groupBy { it.roomId } //先group by roomId和消息
                for((roomId, list) in allNewsGrouped) {
                    val chatRoomEntity = ChatRoomReference.getInstance().findById(roomId)
                    chatRoomEntity?.let {
                        if(ChatRoomType.services == it.type || ChatRoomType.serviceMember == it.type || ChatRoomType.subscribe == it.type) {
                            if (list.size > 1)
                                it.searchMessageCount =
                                    application.getString(R.string.text_sectioned_find_news_count, list.size) //顯示幾則消息
                            else
                                it.searchMessageCount = list[0].content //直接顯示消息內容
                            it.isSearchMultipleMessage = list.size > 1
                            eachNewsRooms.add(it)
                        }
                    }
                }

                if(eachNewsRooms.isNotEmpty()) {
                    eachNewsRooms.sortByDescending { it.lastMessage?.sendTime ?: Long.MAX_VALUE }
                    serviceNumberFilteredSectionedList.add(
                        SearchFilterEntity(
                            application.getString(R.string.text_sectioned_search_news, eachNewsRooms.size),
                            FilterRoomType.NEWS,
                            FilterTab.SERVICE_NUMBER,
                            eachNewsRooms
                        )
                    )
                }
            }
        }
        sendFilterQueryList.emit(Triple(s, if(s.isNotEmpty()) serviceNumberFilteredSectionedList else Lists.newArrayList(), subscribeServiceNumberList.size + servicesChatRooms.size + eachNewsRooms.size))
    }

    fun onSelectServiceNumberItem(item: Any, isNews: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        if(isNews) { //點擊消息轉頁
            if(item is ChatRoomEntity) {
                val messageList = allNewsGrouped[item.id]
                messageList?.let {
                    navigateToMessageList.emit(Triple(searchKeyWord.value, it, item))
                }
            }
        } else {
            when(item) {
                is ServiceNum -> {
                    val entity = ChatRoomReference.getInstance().findById(item.roomId)
                    entity?.let {
                        navigateToChatRoom.emit(Pair(searchKeyWord.value, it))
                    }?: run {
                        navigateToChatRoom.emit(Pair(searchKeyWord.value, item))
                    }
                }
                is ChatRoomEntity -> navigateToChatRoom.emit(Pair(searchKeyWord.value, item))
            }
        }
    }
    fun onSelectedTransferItem(item: Any) = viewModelScope.launch {
        sendServiceNumberSelectedItem.emit(Pair(searchKeyWord.value, item))
    }
    fun showAllData() = viewModelScope.launch(Dispatchers.IO) {
        val subscribeServiceNumberList: MutableList<ServiceNum> = mutableListOf()
        serviceNumberSectionedList.clear()
        for (entity in localEntities) {
            subscribeServiceNumberList.add(entity)
        }

        if (subscribeServiceNumberList.isNotEmpty()) {
            serviceNumberSectionedList.add(
                SearchFilterEntity(
                    application.getString(
                        R.string.text_sectioned_search_service_number_filter,
                        subscribeServiceNumberList.size
                    ),
                    FilterRoomType.SUBSCRIBE_SERVICE_NUMBER,
                    FilterTab.SERVICE_NUMBER,
                    subscribeServiceNumberList
                )
            )
        }
        sendFilterQueryList.emit(Triple("", serviceNumberSectionedList, -1)) // -1表示不顯示統計數字
    }

    fun updateSelectedItemList(item: Any) = viewModelScope.launch(Dispatchers.IO) {
        val filteredList : List<SearchFilterEntity>
        val allList : List<SearchFilterEntity>

        if(item is ServiceNum) {
            if(searchKeyWord.value.isNotEmpty()) {
                filteredList = serviceNumberFilteredSectionedList.map { it }
                filteredList.map { info ->
                    if(info.roomType == FilterRoomType.SUBSCRIBE_SERVICE_NUMBER && info.data is ServiceNum) {
                        info.data.map { t ->
                            if((t as ServiceNum).serviceNumberId == item.serviceNumberId)
                                t.isSelected = !item.isSelected
                        }
                    }
                }
                serviceNumberFilteredSectionedList = filteredList.map { it }.toMutableList()
            }

            allList = serviceNumberSectionedList.map { it }
            allList.forEach { info ->
                if(info.roomType == FilterRoomType.SUBSCRIBE_SERVICE_NUMBER) {
                    info.data.map { t ->
                        if((t as ServiceNum).serviceNumberId == item.serviceNumberId) {
                            t.isSelected = !item.isSelected
                        }
                    }
                }
            }

            serviceNumberSectionedList = allList.map { it }.toMutableList()

        } else if(item is ChatRoomEntity) {

            if(searchKeyWord.value.isNotEmpty()) {
                filteredList = serviceNumberFilteredSectionedList.map { it }
                filteredList.map { info ->
                    if(info.data is ChatRoomEntity) {
                        info.data.map { t ->
                            if((t as ChatRoomEntity).id == item.id)
                                t.isSelected = !item.isSelected
                        }
                    }
                }
                serviceNumberFilteredSectionedList = filteredList.map { it }.toMutableList()
            }

            allList = serviceNumberSectionedList.map { it }
            allList.forEach { info ->
                if(info.roomType == FilterRoomType.SERVICE_NUMBER_CHATROOM) {
                    info.data.map { t ->
                        if((t as ChatRoomEntity).id == item.id) {
                            t.isSelected = !item.isSelected
                        }
                    }
                }
            }

            serviceNumberSectionedList = allList.map { it }.toMutableList()
        }
        sendSelectedItem.emit(Pair(searchKeyWord.value, if(searchKeyWord.value.isNotEmpty()) serviceNumberFilteredSectionedList else serviceNumberSectionedList))
    }

    fun dismissSelectedItem(item: Any) = viewModelScope.launch(Dispatchers.IO) {
        val allList : List<SearchFilterEntity>
        val filteredList : List<SearchFilterEntity>
        when(item) {
            is ServiceNum -> {
                if(searchKeyWord.value.isNotEmpty()) {
                    filteredList = serviceNumberFilteredSectionedList.map { it }
                    filteredList.map { info ->
                        if(info.data is ServiceNum) {
                            info.data.map { t ->
                                if((t as ServiceNum).serviceNumberId == item.serviceNumberId)
                                    t.isSelected = false
                            }
                        }
                    }
                    serviceNumberFilteredSectionedList = filteredList.map { it }.toMutableList()
                }

                allList = serviceNumberSectionedList.map { it }
                allList.forEach { info ->
                    if(info.roomType == FilterRoomType.SUBSCRIBE_SERVICE_NUMBER) {
                        info.data.map { t ->
                            if((t as ServiceNum).serviceNumberId == item.serviceNumberId) {
                                t.isSelected = false
                            }
                        }
                    }
                }

                serviceNumberSectionedList = allList.map { it }.toMutableList()
            }
            is ChatRoomEntity -> {
                if(searchKeyWord.value.isNotEmpty()) {
                    filteredList = serviceNumberFilteredSectionedList.map { it }
                    filteredList.map { info ->
                        if(info.data is ChatRoomEntity) {
                            info.data.map { t ->
                                if((t as ChatRoomEntity).id == item.id)
                                    t.isSelected = false
                            }
                        }
                    }
                    serviceNumberFilteredSectionedList = filteredList.map { it }.toMutableList()
                }

                allList = serviceNumberSectionedList.map { it }
                allList.forEach { info ->
                    if(info.roomType == FilterRoomType.SERVICE_NUMBER_CHATROOM) {
                        info.data.map { t ->
                            if((t as ChatRoomEntity).id == item.id) {
                                t.isSelected = false
                            }
                        }
                    }
                }

                serviceNumberSectionedList = allList.map { it }.toMutableList()
            }
        }
        sendSelectedItem.emit(Pair(searchKeyWord.value, if(searchKeyWord.value.isNotEmpty()) serviceNumberFilteredSectionedList else serviceNumberSectionedList))
    }
}