package tw.com.chainsea.chat.searchfilter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import tw.com.chainsea.ce.sdk.SdkLib
import tw.com.chainsea.ce.sdk.bean.AvatarRecord
import tw.com.chainsea.ce.sdk.bean.CustomerEntity
import tw.com.chainsea.ce.sdk.bean.GroupEntity
import tw.com.chainsea.ce.sdk.bean.RoomType
import tw.com.chainsea.ce.sdk.bean.ServiceNum
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType
import tw.com.chainsea.ce.sdk.database.DBManager
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference
import tw.com.chainsea.ce.sdk.reference.ServiceNumberReference
import tw.com.chainsea.chat.searchfilter.repository.SearchFilterRepository
import tw.com.chainsea.chat.util.SharedPreferenceManager

class SearchFilterViewModel : ViewModel() {
    val etSearchInput = MutableStateFlow("")
    val allEmployeeSelectedList = MutableSharedFlow<List<Any>>()
    val employeesAvatarRecordList = MutableSharedFlow<List<AvatarRecord>>()
    val employeesTextRecordList = MutableSharedFlow<List<String>>()
    val searchInput = MutableSharedFlow<String>()
    private val employeeSelectedList: MutableList<Any> = arrayListOf() // for selected list
    private var employeesSearchMemberRecordList: MutableList<AvatarRecord> = arrayListOf() // for saved employees avatar
    private var employeesSearchTextRecordList: MutableList<String> = arrayListOf() // for saved search text
    val sendClearInputText = MutableSharedFlow<Unit>()
    val sendCloseKeyboard = MutableSharedFlow<Unit>()
    val sendOnRemoveEmployeeItem = MutableSharedFlow<UserProfileEntity>()
    val sendSelectedMemberList = MutableSharedFlow<List<Any>>()
    var ownerId = ""
    val sendSelectedMemberItem = MutableSharedFlow<Pair<Any, Boolean>>()
    val sendDismissSelectedMemberItem = MutableSharedFlow<Any>()
    val sendAvatarSelectedItem = MutableSharedFlow<Any>()
    val pageType = MutableStateFlow(SearchFilterSharedViewModel.SearchFilterPageType.UNDEF) // 功能頁面判斷
    var memberIds: MutableList<String> = mutableListOf()
    val sendIntentToOpenChatRoom = MutableSharedFlow<String>()
    val sendIntentToOpenServiceNumberChatRoom = MutableSharedFlow<String>()
    val sendIntentToOpenChatRoomForContact = MutableSharedFlow<UserProfileEntity>()
    val sendChangeViewPagerItem = MutableSharedFlow<Int>()
    private var record: MutableList<AvatarRecord> = mutableListOf()
    private lateinit var repository: SearchFilterRepository
    private lateinit var sharedPreferenceManager: SharedPreferenceManager

    fun initSearchRecord(userId: String) =
        viewModelScope.launch {
            ownerId = userId
            sharedPreferenceManager = SharedPreferenceManager(SdkLib.getAppContext(), userId)

            repository = SearchFilterRepository(sharedPreferenceManager)
            repository
                .getEmployeesAvatarRecord()
                .onEach {
                    record = if (it.size > 8) it.asReversed().subList(0, 8).toMutableList() else it.toMutableList().asReversed()
                }.collect()

            // 建立社團、邀請、臨時成員只顯示聯絡人頭像記錄
            when (pageType.value) {
                SearchFilterSharedViewModel.SearchFilterPageType.CREATE_GROUP,
                SearchFilterSharedViewModel.SearchFilterPageType.CREATE_DISCUSS,
                SearchFilterSharedViewModel.SearchFilterPageType.INVITATION,
                SearchFilterSharedViewModel.SearchFilterPageType.PROVISIONAL_MEMBER -> {
                    record =
                        record
                            .filter {
                                it.type == RoomType.Contact
                            }.filterNot { avatarRecord ->
                                // 過濾頭圖紀錄中有memberIds裡的成員
                                memberIds.any { b ->
                                    avatarRecord.id.equals(b)
                                }
                            }.toMutableList()
                }
                SearchFilterSharedViewModel.SearchFilterPageType.MESSAGE_TRANSFER,
                SearchFilterSharedViewModel.SearchFilterPageType.SHARE_IN -> {
                    record =
                        record
                            .filterNot {
                                it.type == RoomType.Customer || it.type == RoomType.ServiceOutSideChatRoom
                            }.toMutableList()
                }
                else -> {}
            }
            record =
                record
                    .filterNot { avatarRecord ->
                        if (avatarRecord.type == RoomType.ChatRoom || avatarRecord.type == RoomType.Discuss || avatarRecord.type == RoomType.Crowd) {
                            val entity = ChatRoomReference.getInstance().findById(avatarRecord.id)
                            entity?.isDeleted == true
                        } else {
                            false
                        }
                    }.toMutableList()

            employeesSearchMemberRecordList.addAll(record)

            repository
                .getEmployeesTextRecord()
                .onEach {
                    employeesSearchTextRecordList = it.toMutableList()
                }.collect()

            employeesAvatarRecordList.emit(employeesSearchMemberRecordList)
            employeesTextRecordList.emit(employeesSearchTextRecordList.asReversed())
        }

    fun onSelectEmployeeAvatarItem(item: AvatarRecord) =
        viewModelScope.launch(Dispatchers.IO) {
            closeKeyboard()
            when (pageType.value) {
                SearchFilterSharedViewModel.SearchFilterPageType.GLOBAL_SEARCH -> {
                    globalSearchHandleAvatarLogic(item) // 點選即跳轉聊天室
                }
                else -> {
                    otherHandleAvatarLogic(item) // 點選即列入下方選擇列表
                }
            }
        }

    private fun globalSearchHandleAvatarLogic(item: AvatarRecord) =
        viewModelScope.launch(Dispatchers.IO) {
            when (item.type) {
                RoomType.Contact -> {
                    item.id?.let {
                        val user = DBManager.getInstance().queryUser(it)
                        sendIntentToOpenChatRoomForContact.emit(user)
                    }
                }
                RoomType.ServiceNumber -> {
                    item.id?.let {
                        val sn = ServiceNumberReference.findServiceNumberById(it)
                        sendIntentToOpenServiceNumberChatRoom.emit(sn.roomId)
                    }
                }
                else -> {
                    item.id?.let {
                        sendIntentToOpenChatRoom.emit(it)
                    }
                }
            }
        }

    private fun otherHandleAvatarLogic(item: AvatarRecord) =
        viewModelScope.launch(Dispatchers.IO) {
            when (item.type) {
                RoomType.Contact -> {
                    item.id?.let {
                        repository
                            .findUserProfileById(it)
                            .onEach { user ->
                                user?.let { u ->
                                    checkIsSameContactPerson(u)
                                }
                            }.collect()
                    }
                }
                RoomType.ChatRoom,
                RoomType.ServiceOutSideChatRoom,
                RoomType.Discuss -> {
                    item.id?.let {
                        repository
                            .findChatRoomById(it)
                            .onEach { chatRoom ->
                                chatRoom?.let { room ->
                                    checkIsSameContactPerson(room)
                                }
                            }.collect()
                    }
                }
                RoomType.Crowd -> {
                    item.id?.let {
                        repository
                            .findCrowdById(it)
                            .onEach { crowd ->
                                crowd?.let { c ->
                                    checkIsSameContactPerson(c)
                                }
                            }.collect()
                    }
                }
                RoomType.ServiceNumber -> {
                    item.id?.let {
                        repository
                            .findServiceNumberById(it)
                            .onEach { serviceNumber ->
                                serviceNumber?.let { s ->
                                    checkIsSameContactPerson(s)
                                }
                            }.collect()
                    }
                }
                else -> {}
            }
        }

    private fun checkIsSameContactPerson(item: Any) =
        viewModelScope.launch(Dispatchers.IO) {
            if (!isMemberExclude(item)) {
                getSelectedMember(etSearchInput.value, item, isAvatarRecordSelected = true)
            }
        }

    private fun isMemberExclude(item: Any): Boolean {
        // 檢查頭圖紀錄成員是否有被排除
        var userId: String? = ""
        var isGroupRoom = false
        when (item) {
            is UserProfileEntity -> userId = item.id // 好友id
            is ChatRoomEntity -> {
                when (item.type) {
                    ChatRoomType.friend -> userId = item.memberIds.filterNot { it == ownerId }.firstOrNull() // 好友聊天室對方id
                    ChatRoomType.discuss -> isGroupRoom = true // 多人聊天室排除roomId，不用考慮成員
                    else -> {}
                }
            }
        }
        if (!isGroupRoom) {
            userId?.let {
                if (it.isNotEmpty() && memberIds.isNotEmpty()) {
                    return memberIds.contains(it)
                }
            }
        }
        return false
    }

    fun onSelectedSearchTextItem(text: String) =
        viewModelScope.launch {
            searchInput.emit(text)
        }

    fun onRemoveEmployeeItem(item: Any) =
        viewModelScope.launch {
            removeTheSameContactPerson(employeeSelectedList, item)
            sendDismissSelectedMemberItem.emit(item)
        }

    private fun removeTheSameContactPerson(
        list: MutableList<Any>,
        item: Any
    ) = viewModelScope.launch {
        val id =
            when (item) {
                is UserProfileEntity -> item.id
                is ChatRoomEntity -> {
                    when (item.type) {
                        ChatRoomType.friend -> item.memberIds.filterNot { it == ownerId }.firstOrNull() // 好友聊天室對方id
                        ChatRoomType.services,
                        ChatRoomType.serviceMember,
                        ChatRoomType.system,
                        ChatRoomType.group,
                        ChatRoomType.person,
                        ChatRoomType.subscribe,
                        ChatRoomType.discuss -> item.id // 排除roomId，不用考慮成員
                        else -> {
                            ""
                        }
                    }
                }
                is GroupEntity -> item.id
                is ServiceNum -> item.serviceNumberId
                else -> ""
            }
        for (entity in list) {
            when (entity) {
                is UserProfileEntity -> {
                    if (entity.id == id) {
                        list.remove(entity)
                        break
                    }
                }
                is ChatRoomEntity -> {
                    when (entity.type) {
                        ChatRoomType.friend -> {
                            // 好友聊天室對方id
                            if (id == entity.memberIds.filterNot { it == ownerId }.firstOrNull()) {
                                list.remove(entity)
                                break
                            }
                        }
                        ChatRoomType.services,
                        ChatRoomType.serviceMember,
                        ChatRoomType.system,
                        ChatRoomType.group,
                        ChatRoomType.person,
                        ChatRoomType.subscribe,
                        ChatRoomType.discuss -> {
                            if (id == entity.id) {
                                list.remove(entity)
                                break
                            }
                        }
                        else -> { }
                    }
                }
                is GroupEntity -> {
                    if (id == entity.id) {
                        list.remove(entity)
                        break
                    }
                }
                is ServiceNum -> {
                    if (id == entity.serviceNumberId) {
                        list.remove(entity)
                        break
                    }
                }
            }
        }
        allEmployeeSelectedList.emit(employeeSelectedList)
    }

    fun clearInputText() =
        viewModelScope.launch {
            sendClearInputText.emit(Unit)
        }

    fun closeKeyboard() =
        viewModelScope.launch {
            sendCloseKeyboard.emit(Unit)
        }

    // save search text as long as etSearchInput is not empty
    fun saveSearchTextRecord(s: String) =
        viewModelScope.launch(Dispatchers.IO) {
            if (s.isNotEmpty()) {
                employeesSearchTextRecordList.remove(s)
                employeesSearchTextRecordList.add(s)

                repository
                    .saveEmployeesTextRecord(employeesSearchTextRecordList)
                    .onEach { success ->
                        if (success) employeesTextRecordList.emit(employeesSearchTextRecordList.asReversed())
                    }.collect()
            }
        }

    fun getOwnerAvatar(item: Any) =
        viewModelScope.launch {
            if (!employeeSelectedList.contains(item)) {
                employeeSelectedList.add(item)
                sendSelectedMemberList.emit(employeeSelectedList)
            }
        }

    fun getSelectedMember(
        s: String,
        item: Any,
        isAvatarRecordSelected: Boolean
    ) = viewModelScope.launch {
        if (!employeeSelectedList.contains(item) && !isSameEntity(employeeSelectedList, item)) {
            employeeSelectedList.add(item)

            if (s.isNotEmpty() && !isAvatarRecordSelected) {
                doSavingAvatarTextRecord(s, item)
            }

            sendSelectedMemberList.emit(employeeSelectedList)

            sendSelectedMemberItem.emit(Pair(item, true)) // 選取/取消選取都要更新列表選中樣式
        } else {
            if (!isAvatarRecordSelected) { // 頭圖記錄點選不移除已選擇項目
                onRemoveEmployeeItem(item) // 取消選取
            }
        }
    }

    private fun isSameEntity(
        list: MutableList<Any>,
        item: Any
    ): Boolean {
        var userId: String? = ""
        var roomId = ""
        when (item) {
            is UserProfileEntity -> userId = item.id // 好友id
            is ChatRoomEntity -> {
                when (item.type) {
                    ChatRoomType.friend -> userId = item.memberIds.filterNot { it == ownerId }.firstOrNull()
                    else -> roomId = item.id
                }
            } // 好友聊天室對方id
            is GroupEntity -> userId = item.id // 社團id
            is ServiceNum -> userId = item.serviceNumberId
        }
        for (info in list) {
            when (info) {
                is UserProfileEntity -> if (info.id == userId) return true
                is ChatRoomEntity -> {
                    when (info.type) {
                        ChatRoomType.friend -> if (info.memberIds.filterNot { it == ownerId }.firstOrNull() == userId) return true
                        else -> {
                            if (roomId == info.id) return true
                        }
                    }
                }
                is GroupEntity -> if (info.id == userId) return true
                is ServiceNum -> if (info.serviceNumberId == userId) return true
            }
        }
        return false
    }

    private fun doSavingAvatarTextRecord(
        keyWord: String,
        item: Any
    ) = viewModelScope.launch(Dispatchers.IO) {
        val avatarRecord: AvatarRecord
        val originalSize = employeesSearchMemberRecordList.size
        employeesSearchTextRecordList.remove(keyWord)
        employeesSearchTextRecordList.add(keyWord)
        repository
            .saveEmployeesTextRecord(employeesSearchTextRecordList)
            .onEach { success ->
                if (success) employeesTextRecordList.emit(employeesSearchTextRecordList.asReversed())
            }.collect()
        when (item) {
            is ChatRoomEntity -> {
                if (item.type.equals(ChatRoomType.friend)) { // 判斷夥伴聊天室是否和聯絡人資料重覆，並把聯絡人資料濾掉
                    val friendId = item.memberIds.filterNot { it == ownerId }.firstOrNull()
                    employeesSearchMemberRecordList =
                        employeesSearchMemberRecordList
                            .filterNot { it.type == RoomType.Contact && it.id == friendId }
                            .toMutableList()
                    avatarRecord =
                        AvatarRecord(
                            item.id,
                            RoomType.ChatRoom,
                            friendId
                        )
                } else if (item.type.equals(ChatRoomType.discuss)) {
                    employeesSearchMemberRecordList =
                        employeesSearchMemberRecordList
                            .filterNot { it.type == RoomType.Discuss && it.id == item.id }
                            .toMutableList()
                    avatarRecord =
                        AvatarRecord(
                            item.id,
                            RoomType.Discuss
                        )
                } else if (item.type.equals(ChatRoomType.group)) {
                    employeesSearchMemberRecordList =
                        employeesSearchMemberRecordList
                            .filterNot { it.type == RoomType.Crowd && it.id == item.id }
                            .toMutableList()
                    avatarRecord =
                        AvatarRecord(
                            item.id,
                            RoomType.Crowd
                        )
                } else if (item.type.equals(ChatRoomType.system)) {
                    employeesSearchMemberRecordList =
                        employeesSearchMemberRecordList
                            .filterNot { it.type == RoomType.System && it.id == item.id }
                            .toMutableList()
                    avatarRecord =
                        AvatarRecord(
                            item.id,
                            RoomType.System
                        )
                } else {
                    avatarRecord =
                        AvatarRecord(
                            item.id,
                            if (item.type == ChatRoomType.services && item.serviceNumberOpenType.contains("O")) RoomType.ServiceOutSideChatRoom else RoomType.ChatRoom
                        )
                }
                // 檢查是否有相同的聊天室資料，有則濾掉
                employeesSearchMemberRecordList = employeesSearchMemberRecordList.filterNot { (it.type == RoomType.ChatRoom || it.type == RoomType.ServiceOutSideChatRoom) && it.id == item.id }.toMutableList()
                val newSize = employeesSearchMemberRecordList.size
                employeesSearchMemberRecordList.add(avatarRecord)
                if (originalSize == newSize) {
                    saveAvatarRecord(avatarRecord)
                }
            }
            is UserProfileEntity -> {
                avatarRecord =
                    AvatarRecord(
                        item.id,
                        RoomType.Contact
                    )
                employeesSearchMemberRecordList = employeesSearchMemberRecordList.filterNot { (it.type == RoomType.Contact && it.id == item.id) || it.type == RoomType.ChatRoom && it.ownerId == item.id }.toMutableList()
                val newSize = employeesSearchMemberRecordList.size
                employeesSearchMemberRecordList.add(avatarRecord)
                if (originalSize == newSize) {
                    saveAvatarRecord(avatarRecord)
                }
            }
            is GroupEntity -> {
                avatarRecord =
                    AvatarRecord(
                        item.id,
                        RoomType.Crowd
                    )
                employeesSearchMemberRecordList = employeesSearchMemberRecordList.filterNot { it.type == RoomType.Crowd && it.id == item.id }.toMutableList()
                val newSize = employeesSearchMemberRecordList.size
                employeesSearchMemberRecordList.add(avatarRecord)
                if (originalSize == newSize) {
                    saveAvatarRecord(avatarRecord)
                }
            }
            is ServiceNum -> {
                avatarRecord =
                    AvatarRecord(
                        item.serviceNumberId,
                        RoomType.ServiceNumber
                    )
                employeesSearchMemberRecordList = employeesSearchMemberRecordList.filterNot { it.type == RoomType.ServiceNumber && it.id == item.serviceNumberId }.toMutableList()
                val newSize = employeesSearchMemberRecordList.size
                employeesSearchMemberRecordList.add(avatarRecord)
                if (originalSize == newSize) {
                    saveAvatarRecord(avatarRecord)
                }
            }
            is CustomerEntity -> {
                avatarRecord =
                    AvatarRecord(
                        item.id,
                        RoomType.Customer
                    )
                employeesSearchMemberRecordList = employeesSearchMemberRecordList.filterNot { it.type == RoomType.Customer && it.id == item.id }.toMutableList()
                val newSize = employeesSearchMemberRecordList.size
                employeesSearchMemberRecordList.add(avatarRecord)
                if (originalSize == newSize) {
                    saveAvatarRecord(avatarRecord)
                }
            }
        }
        employeesAvatarRecordList.emit(employeesSearchMemberRecordList.asReversed())
    }

    private fun saveAvatarRecord(avatarRecord: AvatarRecord) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.addEmployeesAvatarRecord(avatarRecord).collect()
        }

    fun updateAvatarTextRecord(
        keyWord: String,
        item: Any
    ) = viewModelScope.launch {
        doSavingAvatarTextRecord(keyWord, item)
    }

    // 當頭圖紀錄所記載的聊天室被移除的處理
    fun removeAvatarItem(roomId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            repository
                .removeAvatarRecord(roomId)
                .onEach {
                    if (it) {
                        initSearchRecord(ownerId)
                    }
                }.collect()
        }
}
