package tw.com.chainsea.chat.searchfilter.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.common.base.Strings
import com.google.common.collect.Lists
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tw.com.aile.sdk.bean.filter.FilterRoomType
import tw.com.aile.sdk.bean.filter.FilterTab
import tw.com.aile.sdk.bean.filter.SearchFilterEntity
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.ce.sdk.bean.msg.MessageType
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType
import tw.com.chainsea.ce.sdk.database.DBManager
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.http.ce.base.BaseViewModel
import tw.com.chainsea.ce.sdk.http.ce.base.TokenRepository
import tw.com.chainsea.ce.sdk.http.ce.model.User
import tw.com.chainsea.ce.sdk.network.model.common.ApiResult
import tw.com.chainsea.ce.sdk.network.model.response.ChatRoomMemberResponse
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.searchfilter.repository.ChatRoomSearchRepository
import tw.com.chainsea.chat.searchfilter.repository.SearchFilterSharedRepository
import tw.com.chainsea.chat.view.chat.ChatRepository

class ChatRoomSearchViewModel(
    private val application: Application,
    private val repository: ChatRoomSearchRepository,
    private val searchFilterSharedRepository: SearchFilterSharedRepository,
    private val chatRepository: ChatRepository,
    private val tokenRepository: TokenRepository
) : BaseViewModel(application, tokenRepository) {
    var localChatRoomsEntities: MutableList<ChatRoomEntity> = mutableListOf()
    private var chatRoomFilteredSectionedList: MutableList<SearchFilterEntity> = arrayListOf() // filtered list
    private var chatRoomSectionedList: MutableList<SearchFilterEntity> = arrayListOf() // all data list
    val sendQueryList = MutableSharedFlow<Triple<String, MutableList<SearchFilterEntity>, Int>>()
    val navigateToChatRoom = MutableSharedFlow<Pair<String, ChatRoomEntity>>()
    private val searchKeyWord = MutableStateFlow("")
    private var allNewsGrouped: Map<String?, List<MessageEntity>> = mapOf()
    val navigateToMessageList = MutableSharedFlow<Triple<String, List<MessageEntity>, ChatRoomEntity>>()
    val userId = MutableStateFlow("")
    val sendChatRoomSelectedItem = MutableSharedFlow<Pair<String, Any>>()
    val sendSelectedItemList = MutableSharedFlow<Pair<String, MutableList<SearchFilterEntity>>>()

    fun initChatRoomDiscussFriendsData(ownerId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            userId.value = ownerId
            val chatRoomTypeList = Lists.newArrayList<ChatRoomType>()
            chatRoomTypeList.apply {
                add(ChatRoomType.friend)
                add(ChatRoomType.discuss)
                add(ChatRoomType.group)
                add(ChatRoomType.system)
                add(ChatRoomType.person)
            }
            repository
                .getAllChatRoomEntities(ownerId, chatRoomTypeList)
                .onEach {
                    it?.let { data ->
                        localChatRoomsEntities = data
                    }
                }.collect()
            showAllData()
        }

    private suspend fun checkFilerCondition(
        filter: String,
        item: Any
    ): Boolean {
        return when (item) {
            is ChatRoomEntity -> {
                if (item.type.equals(ChatRoomType.person)) {
                    var nickName = ""
                    repository
                        .queryFriend(item.ownerId)
                        .onEach {
                            nickName = it.nickName
                        }.collect()
                    return nickName.contains(filter, true)
                }
                if (!Strings.isNullOrEmpty(item.name)) {
                    // 過濾夥伴聊天室名稱, 多人聊天室名稱及夥伴名稱
                    (!item.type.equals(ChatRoomType.person) && item.name.contains(filter, true)) ||
                        (
                            item.type.equals(ChatRoomType.discuss) &&
                                item.members.any {
                                    it.nickName.contains(filter, true) ||
                                        it.alias?.contains(filter, true) == true
                                }
                        ) ||
                        // 過濾社團聊天室名稱, 社團成員名稱
                        (item.type.equals(ChatRoomType.group)) &&
                        (
                            item.name.contains(filter, true) ||
                                (
                                    item.members.any {
                                        it.nickName.contains(filter, true) ||
                                            it.alias?.contains(filter, true) == true
                                    }
                                )
                        ) ||
                        (item.type.equals(ChatRoomType.system) && item.name.contains(filter, true)) ||
                        item.type.equals(ChatRoomType.person)
                } else {
                    return false
                }
            }

            is MessageEntity -> {
                item.getContent(item.content).contains(filter, true)
            }

            else -> {
                false
            }
        }
    }

    /**
     * 轉發分享的過濾
     **/
    fun filter(s: String) =
        viewModelScope.launch(Dispatchers.IO) {
            searchKeyWord.value = s
            val friendRooms: MutableList<ChatRoomEntity> = mutableListOf()
            val discussRooms: MutableList<ChatRoomEntity> = mutableListOf()
            val groupRooms: MutableList<ChatRoomEntity> = mutableListOf()

            if (s.isNotEmpty()) {
                chatRoomFilteredSectionedList.clear()
                for (entity in localChatRoomsEntities) {
                    if (checkFilerCondition(s, entity)) {
                        // 將聊天室分組成夥伴、多人及社團
                        if (entity.type.equals(ChatRoomType.friend)) {
                            if (checkUserIsNotBlock(entity.memberIds.filterNot { it == userId.value }.firstOrNull())) {
                                friendRooms.add(entity)
                            }
                        } else if (entity.type.equals(ChatRoomType.person) || entity.type.equals(ChatRoomType.system)) {
                            friendRooms.add(entity) // 個人聊天室及Aile小助手也放入夥伴聊天室
                        } else if (entity.type.equals(ChatRoomType.group)) { // 社團
                            if (entity.memberIds.isNullOrEmpty()) {
                                val groupInfo = DBManager.getInstance().queryGroupInfo(entity.id)
                                groupInfo?.let {
                                    it.memberIds?.let { ids ->
                                        queryGroupDiscussMemberInfo(s, entity, ids).collect { chatRoom ->
                                            groupRooms.add(chatRoom)
                                        }
                                    }
                                }
                            } else {
                                queryGroupDiscussMemberInfo(s, entity, entity.memberIds).collect {
                                    groupRooms.add(it)
                                }
                            }
                        } else if (entity.type.equals(ChatRoomType.discuss)) { // 多人
                            val memberIds = entity.chatRoomMember.map { it.memberId }
                            queryGroupDiscussMemberInfo(s, entity, memberIds).collect { chatRoom ->
                                discussRooms.add(chatRoom)
                            }
                        }
                    }
                }

                if (friendRooms.isNotEmpty()) {
                    friendRooms.sortByDescending { it.lastMessage?.sendTime ?: Long.MAX_VALUE }
                    chatRoomFilteredSectionedList.add(
                        SearchFilterEntity(
                            application.getString(R.string.text_sectioned_friend_chatroom, friendRooms.size),
                            FilterRoomType.FRIENDS,
                            FilterTab.CHAT_ROOM,
                            friendRooms
                        )
                    )
                }
                if (discussRooms.isNotEmpty()) {
                    discussRooms.sortByDescending { it.lastMessage?.sendTime ?: Long.MAX_VALUE }
                    chatRoomFilteredSectionedList.add(
                        SearchFilterEntity(
                            application.getString(R.string.text_sectioned_group_chatroom, discussRooms.size),
                            FilterRoomType.DISCUSS,
                            FilterTab.CHAT_ROOM,
                            discussRooms
                        )
                    )
                }
                if (groupRooms.isNotEmpty()) {
                    groupRooms.sortByDescending { it.lastMessage?.sendTime ?: Long.MAX_VALUE }
                    chatRoomFilteredSectionedList.add(
                        SearchFilterEntity(
                            application.getString(R.string.text_sectioned_communities_filter, groupRooms.size),
                            FilterRoomType.GROUP,
                            FilterTab.CHAT_ROOM,
                            groupRooms
                        )
                    )
                }
            }
            sendQueryList.emit(
                Triple(
                    s,
                    if (s.isNotEmpty()) chatRoomFilteredSectionedList else Lists.newArrayList(),
                    friendRooms.size + discussRooms.size + groupRooms.size
                )
            )
        }

    /**
     * 全局搜尋的過濾
     **/
    fun globalFilter(
        userId: String,
        s: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        searchKeyWord.value = s
        repository
            .queryChatRoomByKeyWord(userId, s)
            .onEach {
                classifyChatRoom(s, it)
            }.collect()
    }

    private suspend fun classifyChatRoom(
        s: String,
        result: List<ChatRoomEntity>
    ) {
        val dateTime = System.currentTimeMillis()
        val friendRooms: MutableList<ChatRoomEntity> = mutableListOf()
        val discussRooms: MutableList<ChatRoomEntity> = mutableListOf()
        val newsRooms: MutableList<MessageEntity> = mutableListOf()
        val eachNewsRooms: MutableList<ChatRoomEntity> = mutableListOf()
        val groupRooms: MutableList<ChatRoomEntity> = mutableListOf()
        val messageTypeList = Lists.newArrayList<MessageType>()
        messageTypeList.apply {
            add(MessageType.AT)
            add(MessageType.TEXT)
        }
        if (s.isNotEmpty()) {
            chatRoomFilteredSectionedList.clear()
            for (entity in result) {
                if (checkFilerCondition(s, entity)) {
                    // 將聊天室分組成夥伴、多人及社團
                    if (entity.type.equals(ChatRoomType.friend)) {
                        if (checkUserIsNotBlock(entity.memberIds.filterNot { it == userId.value }.firstOrNull())) {
                            friendRooms.add(entity)
                        }
                    } else if (entity.type.equals(ChatRoomType.person) || entity.type.equals(ChatRoomType.system)) {
                        if (entity.type.equals(ChatRoomType.system)) {
                            entity.avatarId = TokenPref.getInstance(application).systemUserAvatarId
                            entity.name = TokenPref.getInstance(application).systemUserName
                        }
                        friendRooms.add(entity) // 個人聊天室及Aile小助手也放入夥伴聊天室
                    } else if (entity.type.equals(ChatRoomType.group)) { // 社團
                        if (entity.memberIds.isNullOrEmpty()) {
                            val groupInfo = DBManager.getInstance().queryGroupInfo(entity.id)
                            groupInfo?.let {
                                it.memberIds?.let { ids ->
                                    queryGroupDiscussMemberInfo(s, entity, ids).collect { chatRoom ->
                                        groupRooms.add(chatRoom)
                                    }
                                }
                            }
                        } else {
                            queryGroupDiscussMemberInfo(s, entity, entity.memberIds).collect {
                                groupRooms.add(it)
                            }
                        }
                    } else if (entity.type.equals(ChatRoomType.discuss)) { // 多人
                        val memberIds = entity.chatRoomMember?.map { it.memberId } ?: mutableListOf()
                        queryGroupDiscussMemberInfo(s, entity, memberIds).collect { chatRoom ->
                            discussRooms.add(chatRoom)
                        }
                    }
                }
            }

            // 消息過濾
            searchFilterSharedRepository
                .getAllMessageEntitiesByKeyWord(messageTypeList, s)
                .onEach {
                    newsRooms.addAll(it)
                }.collect()

            if (friendRooms.isNotEmpty()) {
                val friendList = friendRooms.filterNot { it.deleted }.toMutableList()
                friendList.sortByDescending { it.lastMessage?.sendTime ?: Long.MAX_VALUE }
                chatRoomFilteredSectionedList.add(
                    SearchFilterEntity(
                        application.getString(R.string.text_sectioned_friend_chatroom, friendList.size),
                        FilterRoomType.FRIENDS,
                        FilterTab.CHAT_ROOM,
                        friendList
                    )
                )
            }
            if (discussRooms.isNotEmpty()) {
                val discussList = discussRooms.filterNot { it.deleted }
                chatRoomFilteredSectionedList.add(
                    SearchFilterEntity(
                        application.getString(R.string.text_sectioned_group_chatroom, discussList.size),
                        FilterRoomType.DISCUSS,
                        FilterTab.CHAT_ROOM,
                        discussList
                    )
                )
            }
            if (groupRooms.isNotEmpty()) {
                val groupList = groupRooms.filterNot { it.deleted }.toMutableList()
                groupList.sortByDescending { it.lastMessage?.sendTime ?: Long.MAX_VALUE }
                chatRoomFilteredSectionedList.add(
                    SearchFilterEntity(
                        application.getString(R.string.text_sectioned_communities_filter, groupList.size),
                        FilterRoomType.GROUP,
                        FilterTab.CHAT_ROOM,
                        groupList
                    )
                )
            }
            if (newsRooms.isNotEmpty()) {
                allNewsGrouped = newsRooms.groupBy { it.roomId } // 先group by roomId和消息
                for ((roomId, list) in allNewsGrouped) {
                    roomId?.let {
                        repository
                            .findRoomById(it)
                            .onEach { entity ->
                                if (ChatRoomType.discuss == entity.type || ChatRoomType.friend == entity.type || ChatRoomType.group == entity.type || ChatRoomType.system == entity.type || ChatRoomType.person == entity.type) { // 只顯示夥伴、多人聊天室及社團
                                    if (list.size > 1) {
                                        entity.searchMessageCount =
                                            application.getString(R.string.text_sectioned_find_news_count, list.size) // 顯示幾則消息
                                    } else {
                                        entity.searchMessageCount = list[0].content // 直接顯示消息內容
                                    }
                                    entity.isSearchMultipleMessage = list.size > 1
                                    eachNewsRooms.add(entity)
                                }
                            }.collect()
                    }
                }

                if (eachNewsRooms.isNotEmpty()) {
                    val eachNewsList = eachNewsRooms.filterNot { it.deleted }.toMutableList()
                    eachNewsList.sortByDescending { it.lastMessage?.sendTime ?: Long.MAX_VALUE }
                    chatRoomFilteredSectionedList.add(
                        SearchFilterEntity(
                            application.getString(R.string.text_sectioned_search_news, eachNewsList.size),
                            FilterRoomType.NEWS,
                            FilterTab.CHAT_ROOM,
                            eachNewsList
                        )
                    )
                }
            }
        }
        val totalSize = friendRooms.filterNot { it.deleted }.size + discussRooms.filterNot { it.deleted }.size + eachNewsRooms.filterNot { it.deleted }.size + groupRooms.filterNot { it.deleted }.size
        sendQueryList.emit(
            Triple(
                s,
                if (s.isNotEmpty()) chatRoomFilteredSectionedList else Lists.newArrayList(),
                totalSize
            )
        )
        Log.d(
            "Kyle116",
            String.format(
                "classifyChatRoom count->%s, use time->%s/秒  ",
                totalSize,
                (System.currentTimeMillis() - dateTime) / 1000.0
            )
        )
    }
//    private suspend fun checkCustomerAlias(item: ChatRoomEntity, s: String): Boolean = viewModelScope.async(Dispatchers.IO) {
//        var customerInfo: CustomerEntity? = null
//        repository.queryCustomerInfo(item.ownerId).onEach {
//            it?.let {
//                customerInfo = it
//            }
//        }.collect()
//        customerInfo?.run { name.contains(s, true) || nickName.contains(s, true) || customerName.contains(s, true) } ?: false
//    }.await()

    fun onSelectChatRoomItem(
        item: ChatRoomEntity,
        isNews: Boolean
    ) = viewModelScope.launch {
        if (isNews) { // 點擊消息轉頁
            val messageList = allNewsGrouped[item.id]
            messageList?.let {
                navigateToMessageList.emit(Triple(searchKeyWord.value, it, item))
            }
        } else {
            navigateToChatRoom.emit(Pair(searchKeyWord.value, item))
        }
    }

    fun onSelectTransferItem(item: ChatRoomEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            sendChatRoomSelectedItem.emit(Pair(searchKeyWord.value, item))
        }

    private suspend fun checkUserIsNotBlock(id: String?): Boolean =
        withContext(Dispatchers.IO) {
            if (id == null) return@withContext false

            val user = repository.queryFriend(id).firstOrNull()
            user?.status != User.Status.DISABLE
        }

    fun showAllData() =
        viewModelScope.launch(Dispatchers.IO) {
            val friendRooms: MutableList<ChatRoomEntity> = mutableListOf()
            val discussRooms: MutableList<ChatRoomEntity> = mutableListOf()
            val groupRooms: MutableList<ChatRoomEntity> = mutableListOf()
            chatRoomSectionedList.clear()
            if (localChatRoomsEntities.isNotEmpty()) {
                for (entity in localChatRoomsEntities) {
                    // 將聊天室分組成夥伴、多人及社團
                    if (entity.type.equals(ChatRoomType.friend)) {
                        if (checkUserIsNotBlock(entity.memberIds.filterNot { it == userId.value }.firstOrNull())) {
                            friendRooms.add(entity)
                        }
                    } else if (entity.type.equals(ChatRoomType.person) || entity.type.equals(ChatRoomType.system)
                    ) {
                        friendRooms.add(entity) // 個人聊天室及Aile小助手也放入夥伴聊天室
                    } else if (entity.type.equals(ChatRoomType.discuss) || entity.type.equals(ChatRoomType.group)
                    ) { // 社團or多人
                        if (entity.type.equals(ChatRoomType.group)) {
                            groupRooms.add(entity)
                        } else {
                            discussRooms.add(entity)
                        }
                    }
                }
            }
            if (friendRooms.isNotEmpty()) {
                friendRooms.sortByDescending { it.lastMessage?.sendTime ?: Long.MAX_VALUE }
                chatRoomSectionedList.add(
                    SearchFilterEntity(
                        application.getString(R.string.text_sectioned_friend_chatroom, friendRooms.size),
                        FilterRoomType.FRIENDS,
                        FilterTab.CHAT_ROOM,
                        friendRooms
                    )
                )
            }
            if (discussRooms.isNotEmpty()) {
                discussRooms.sortByDescending { it.lastMessage?.sendTime ?: Long.MAX_VALUE }
                chatRoomSectionedList.add(
                    SearchFilterEntity(
                        application.getString(R.string.text_sectioned_group_chatroom, discussRooms.size),
                        FilterRoomType.DISCUSS,
                        FilterTab.CHAT_ROOM,
                        discussRooms
                    )
                )
            }
            if (groupRooms.isNotEmpty()) {
                groupRooms.sortByDescending { it.lastMessage?.sendTime ?: Long.MAX_VALUE }
                chatRoomSectionedList.add(
                    SearchFilterEntity(
                        application.getString(R.string.text_sectioned_communities_filter, groupRooms.size),
                        FilterRoomType.GROUP,
                        FilterTab.CHAT_ROOM,
                        groupRooms
                    )
                )
            }
            sendQueryList.emit(
                Triple(
                    "",
                    chatRoomSectionedList,
                    -1 // -1表示不顯示統計數字
                )
            )
        }

    fun updateSelectedItemList(item: Any) =
        viewModelScope.launch(Dispatchers.IO) {
            val filteredList: List<SearchFilterEntity>
            val allList: List<SearchFilterEntity>

            if (item is ChatRoomEntity) {
                if (searchKeyWord.value.isNotEmpty()) {
                    filteredList = chatRoomFilteredSectionedList.map { it }
                    filteredList.map { info ->
                        info.data.map { t ->
                            if ((t as ChatRoomEntity).id == item.id) {
                                t.isSelected = !item.isSelected
                            }
                        }
                    }
                    chatRoomFilteredSectionedList = filteredList.map { it }.toMutableList()
                }

                allList = chatRoomSectionedList.map { it }
                allList.forEach { info ->
                    info.data.map { t ->
                        if ((t as ChatRoomEntity).id == item.id) {
                            t.isSelected = !item.isSelected
                        }
                    }
                }

                chatRoomSectionedList = allList.map { it }.toMutableList()
                sendSelectedItemList.emit(Pair(searchKeyWord.value, if (searchKeyWord.value.isNotEmpty()) chatRoomFilteredSectionedList else chatRoomSectionedList))
            }
        }

    fun dismissSelectedItem(item: Any) =
        viewModelScope.launch(Dispatchers.IO) {
            val filteredList: List<SearchFilterEntity>
            val allList: List<SearchFilterEntity>

            if (item is ChatRoomEntity) {
                if (searchKeyWord.value.isNotEmpty()) {
                    filteredList = chatRoomFilteredSectionedList.map { it }
                    filteredList.map { info ->
                        info.data.map { t ->
                            if ((t as ChatRoomEntity).id == item.id) {
                                t.isSelected = false
                            }
                        }
                    }
                    chatRoomFilteredSectionedList = filteredList.map { it }.toMutableList()
                }

                allList = chatRoomSectionedList.map { it }
                allList.forEach { info ->
                    info.data.map { t ->
                        if ((t as ChatRoomEntity).id == item.id) {
                            t.isSelected = false
                        }
                    }
                }

                chatRoomSectionedList = allList.map { it }.toMutableList()
                sendSelectedItemList.emit(Pair(searchKeyWord.value, if (searchKeyWord.value.isNotEmpty()) chatRoomFilteredSectionedList else chatRoomSectionedList))
            }
        }

    fun getChatMember(item: ChatRoomEntity): LiveData<List<ChatRoomMemberResponse>> {
        val result = MutableLiveData<List<ChatRoomMemberResponse>>()
        viewModelScope.launch(Dispatchers.IO) {
            checkTokenValid(chatRepository.getChatMember(item.id))?.collect {
                when (it) {
                    is ApiResult.Success -> {
                        withContext(Dispatchers.Main) {
                            result.value = it.data
                        }
                    }

                    else -> {}
                }
            }
        }
        return result
    }

    private fun queryGroupDiscussMemberInfo(
        keyWord: String,
        entity: ChatRoomEntity,
        memberIds: List<String>
    ) = flow {
        repository.queryUserProfileFromMemberIds(memberIds).collect { users ->
            val lastMsg =
                coroutineScope {
                    users
                        .filter { it.nickName.contains(keyWord, ignoreCase = true) }
                        .map { member ->
                            async {
                                val isNotBlocked = checkUserIsNotBlock(member.id)
                                if (isNotBlocked) {
                                    member.nickName
                                } else {
                                    "${member.nickName}${application.getString(R.string.text_forbidden)}"
                                }
                            }
                        }.awaitAll()
                        .joinToString(separator = ",")
                }

            entity.lastMessage?.let {
                if (lastMsg.isNotEmpty()) {
                    it.content =
                        application.getString(
                            R.string.text_group_chatroom_last_message_contain,
                            lastMsg
                        )
                }
            }
            emit(entity)
        }
    }
}
