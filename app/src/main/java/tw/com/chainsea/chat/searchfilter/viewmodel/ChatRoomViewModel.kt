package tw.com.chainsea.chat.searchfilter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType
import tw.com.chainsea.ce.sdk.http.ce.model.User
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference
import tw.com.chainsea.chat.searchfilter.repository.ChatRoomSearchRepository

class ChatRoomViewModel(
    private val repository: ChatRoomSearchRepository
) : ViewModel() {
    private var localEntities: MutableList<ChatRoomEntity> = arrayListOf()
    val sendQueryList = MutableSharedFlow<Pair<String, List<Any>>>()
    val sendFilterQueryList = MutableSharedFlow<Pair<String, List<Any>>>()
    private val searchInputText = MutableStateFlow("")
    private var chatRoomFilteredSectionedList: MutableList<ChatRoomEntity> = arrayListOf()

    val sendEmployeeSelectedItem = MutableSharedFlow<Triple<String, Any, Int>>()
    val sendChatRoomText = MutableSharedFlow<String>()
    var ownerId: String = ""

    fun getAllChatRoom(
        userId: String,
        roomTypeList: List<ChatRoomType>,
        memberIds: MutableList<String>
    ) = viewModelScope.launch(Dispatchers.IO) {
        ownerId = userId
        localEntities =
            ChatRoomReference.getInstance().findAllChatRoomsByType(
                null,
                userId,
                roomTypeList,
                true,
                false,
                true
            )

        // 過濾被禁用的夥伴聊天室
        val filter: MutableList<ChatRoomEntity> = mutableListOf()
        localEntities.forEach { entity ->
            entity.chatRoomMember?.let { chatMember ->
                if (checkUserIsNotBlock(chatMember.filterNot { it.memberId == userId }.firstOrNull()?.memberId)) {
                    filter.add(entity)
                }
            } ?: run {
                if (checkUserIsNotBlock(entity.memberIds.filterNot { it == userId }.firstOrNull())) {
                    filter.add(entity)
                }
            }
        }
        localEntities.clear()
        localEntities.addAll(filter)
        localEntities.sortByDescending { it.lastMessage?.sendTime ?: Long.MAX_VALUE }

        if (memberIds.isNotEmpty()) {
            localEntities =
                localEntities
                    .filterNot { m ->
                        m.chatRoomMember?.let {
                            memberIds.contains(
                                m.chatRoomMember
                                    .filterNot { it.memberId == userId }
                                    .firstOrNull()
                                    ?.memberId
                            )
                        } ?: run {
                            memberIds.contains(m.memberIds.filterNot { it == userId }.firstOrNull())
                        }
                    }.toMutableList()
        }
        sendQueryList.emit(Pair(searchInputText.value, localEntities))
    }

    private fun checkFilterCondition(
        filter: String,
        item: ChatRoomEntity
    ): Boolean {
        // 過濾夥伴聊天室名稱, 多人聊天室名稱及夥伴名稱
        return item.name.contains(filter, true) ||
            (
                item.type.equals(ChatRoomType.discuss) &&
                    item.members.any {
                        it.nickName.contains(filter, true) ||
                            it.alias?.contains(filter, true) == true
                    }
            )
    }

    fun filter(s: String) =
        viewModelScope.launch {
            searchInputText.value = s
            if (s.isNotEmpty()) {
                chatRoomFilteredSectionedList.clear()
                localEntities.forEach { entity ->
                    if (checkFilterCondition(s, entity)) {
                        chatRoomFilteredSectionedList.add(entity)
                    }
                }
            }
            sendFilterQueryList.emit(Pair(s, if (s.isNotEmpty()) chatRoomFilteredSectionedList else localEntities))
        }

    fun onSelectChatRoomItem(
        item: ChatRoomEntity,
        position: Int
    ) = viewModelScope.launch {
        sendEmployeeSelectedItem.emit(Triple(searchInputText.value, item, position))
    }

    fun updateSelectedItemList(item: Any) =
        viewModelScope.launch {
            val allList: List<ChatRoomEntity>
            val filteredList: List<ChatRoomEntity>

            if (item is ChatRoomEntity) {
                if (searchInputText.value.isNotEmpty()) {
                    filteredList = chatRoomFilteredSectionedList.map { it.clone() }
                    filteredList.map { info ->
                        if (info.id == item.id) {
                            info.isSelected = !info.isSelected
                        }
                    }
                    chatRoomFilteredSectionedList = filteredList.map { it.clone() }.toMutableList()
                }
                allList = localEntities.map { it.clone() }
                allList.map { info ->
                    if (info.id == item.id) {
                        info.isSelected = !info.isSelected
                    }
                }
                localEntities = allList.map { it.clone() }.toMutableList()
                sendQueryList.emit(Pair(searchInputText.value, if (searchInputText.value.isNotEmpty()) chatRoomFilteredSectionedList else localEntities))
            } else if (item is UserProfileEntity) { // 從別的tab選取要檢查並刷新

                if (searchInputText.value.isNotEmpty()) {
                    filteredList = chatRoomFilteredSectionedList.map { it.clone() }
                    filteredList.map { info ->
                        if (item.id == info.memberIds.filterNot { it == ownerId }.firstOrNull()) {
                            info.isSelected = !info.isSelected
                        }
                    }
                    chatRoomFilteredSectionedList = filteredList.map { it.clone() }.toMutableList()
                }
                allList = localEntities.map { it.clone() }
                allList.map { info ->
                    if (item.id == info.memberIds.filterNot { it == ownerId }.firstOrNull()) {
                        info.isSelected = !info.isSelected
                    }
                }
                localEntities = allList.map { it.clone() }.toMutableList()
                sendQueryList.emit(Pair(searchInputText.value, if (searchInputText.value.isNotEmpty()) chatRoomFilteredSectionedList else localEntities))
            }
        }

    fun dismissSelectedItem(item: Any) =
        viewModelScope.launch {
            val allList: List<ChatRoomEntity>
            val filteredList: List<ChatRoomEntity>
            if (item is ChatRoomEntity) {
                if (searchInputText.value.isNotEmpty()) {
                    filteredList = chatRoomFilteredSectionedList.map { it.clone() }
                    filteredList.map { info ->
                        if (info.id == item.id) {
                            info.isSelected = false
                        }
                    }
                    chatRoomFilteredSectionedList = filteredList.map { it.clone() }.toMutableList()
                }
                allList = localEntities.map { it.clone() }
                allList.map { info ->
                    if (info.id == item.id) {
                        info.isSelected = false
                    }
                }
                localEntities = allList.map { it.clone() }.toMutableList()
                sendQueryList.emit(Pair(searchInputText.value, if (searchInputText.value.isNotEmpty()) chatRoomFilteredSectionedList else localEntities))
            } else if (item is UserProfileEntity) { // 從別的tab選取要檢查並刷新

                if (searchInputText.value.isNotEmpty()) {
                    filteredList = chatRoomFilteredSectionedList.map { it.clone() }
                    filteredList.map { info ->
                        if (item.id == info.memberIds.filterNot { it == ownerId }.firstOrNull()) {
                            info.isSelected = false
                        }
                    }
                    chatRoomFilteredSectionedList = filteredList.map { it.clone() }.toMutableList()
                }
                allList = localEntities.map { it.clone() }
                allList.map { info ->
                    if (item.id == info.memberIds.filterNot { it == ownerId }.firstOrNull()) {
                        info.isSelected = false
                    }
                }
                localEntities = allList.map { it.clone() }.toMutableList()
                sendQueryList.emit(Pair(searchInputText.value, if (searchInputText.value.isNotEmpty()) chatRoomFilteredSectionedList else localEntities))
            }
        }

    private suspend fun checkUserIsNotBlock(id: String?): Boolean =
        viewModelScope
            .async(Dispatchers.IO) {
                // 在夥伴聊天室，當對方被禁用，不顯示
                var user: UserProfileEntity? = null
                id?.let {
                    repository
                        .queryFriend(it)
                        .onEach { entity ->
                            entity?.let { e ->
                                user = e
                            }
                        }.collect()
                    user?.run { status != User.Status.DISABLE } ?: false
                } ?: false
            }.await()
}
