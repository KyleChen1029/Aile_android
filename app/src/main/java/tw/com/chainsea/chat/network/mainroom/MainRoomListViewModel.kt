package tw.com.chainsea.chat.network.mainroom

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.common.collect.Lists
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import tw.com.chainsea.android.common.json.JsonHelper
import tw.com.chainsea.ce.sdk.bean.msg.MessageFlag
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType
import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberType
import tw.com.chainsea.ce.sdk.database.DBManager
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.http.ce.base.BaseViewModel
import tw.com.chainsea.ce.sdk.http.ce.base.TokenRepository
import tw.com.chainsea.ce.sdk.http.ce.model.User
import tw.com.chainsea.ce.sdk.network.model.common.ApiResult
import tw.com.chainsea.ce.sdk.network.model.common.ErrorCode
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference
import tw.com.chainsea.ce.sdk.reference.LabelReference
import tw.com.chainsea.ce.sdk.reference.MessageReference
import tw.com.chainsea.ce.sdk.service.ChatRoomService
import tw.com.chainsea.ce.sdk.socket.ce.bean.AtMessageContentEventEnum
import tw.com.chainsea.ce.sdk.socket.ce.bean.AtMessageSocket
import tw.com.chainsea.chat.App
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.util.TextViewHelper
import tw.com.chainsea.chat.view.base.PreloadStepEnum
import tw.com.chainsea.chat.view.chat.ChatRepository

class MainRoomListViewModel(
    private val application: Application,
    private val chatRepository: ChatRepository,
    private val tokenRepository: TokenRepository
) : BaseViewModel(application, tokenRepository) {
    val sendChatRoomList = MutableSharedFlow<MutableList<ChatRoomEntity>>()
    val sendChatRoomListByLoadMore = MutableSharedFlow<MutableList<Any>>()
    val sendRefreshByDB = MutableSharedFlow<Unit>()
    val onAvatarUpdated = MutableSharedFlow<ChatRoomEntity>()
    val onSelfAvatarUpdated = MutableSharedFlow<ChatRoomEntity>()
    val onAtChatRoom = MutableSharedFlow<Pair<String, Boolean>>()
    var dateTime = 0L
    val selfUserId: String = TokenPref.getInstance(application).userId
    var isLoadMoreIng = false
    var currentIndex = 0
    var chunkSize = 20

    fun refreshList(type: PreloadStepEnum) =
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("Kyle111", "refreshList " + System.currentTimeMillis().toString())
            syncChatRoomEntitiesFromServer(type)
        }

    private fun syncChatRoomEntitiesFromServer(type: PreloadStepEnum) {
        viewModelScope.launch(Dispatchers.IO) {
            checkTokenValid(chatRepository.getAllChatRoomList(selfUserId, type))?.collect {
                when (it) {
                    is ApiResult.Failure -> {
                        refreshListByDb()
                    }

                    is ApiResult.NextPage -> {
                        if (it.hasNextPage) {
                            Log.d("Kyle111", "syncChatRoomEntitiesFromServer hasNextPage")
                            syncChatRoomEntitiesFromServer(type)
                        } else {
                            // 更新未讀數
                            ChatRoomService.getInstance().getBadge(application)
                            getAllAtMeChatRoom()
                        }
                        Log.d(
                            "Kyle557",
                            "MainRoomListViewModel hasNextPage=${it.hasNextPage}"
                        )
                    }

                    else -> {}
                }
            }
        }
    }

    fun refreshListByDb() =
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("Kyle116", "refreshListByDb chunkSize=$chunkSize")
            val list = ChatRoomReference.getInstance().findMainChatRoomData(0, chunkSize)
            Log.d("Kyle116", "refreshListByDb list size =${list.size}")
            dateTime = System.currentTimeMillis()
            list?.let { data ->
                data
                    .filter { entity ->
                        entity.type == ChatRoomType.friend && entity.memberIds.contains(selfUserId)
                    }.map { entity1 ->
                        val isFavourite =
                            LabelReference.findIsFavouriteById(
                                null,
                                entity1.memberIds.filterNot { it == selfUserId }.firstOrNull()
                            )
                        entity1.isFavourite = isFavourite
                    }
            }
            if (list.isNotEmpty()) {
                checkChatRoomRules(list)
            } else {
                sendChatRoomList.emit(Lists.newArrayList())
            }
        }

    private fun checkChatRoomRules(list: List<ChatRoomEntity>) =
        viewModelScope.launch(Dispatchers.IO) {
            val filterList =
                list
                    .filterNot {
                        (it.name == null && it.type != ChatRoomType.person) || it.isHidden
                    }.map { item ->
                        if (item.type == ChatRoomType.friend) { // 判斷夥伴聊天室成員是否被禁用
                            val user = DBManager.getInstance().queryFriend(item.memberIds.filterNot { it == selfUserId }.firstOrNull())
                            user?.let {
                                item.lastMessage?.let { lastMsg ->
                                    if (it.status == User.Status.DISABLE && it.id == lastMsg.senderId) {
                                        item.lastMessage.senderName += getApplication<App>().getString(R.string.text_forbidden)
                                    }
                                }
                            }
                        }
                        if (item.type == ChatRoomType.discuss && item.chatRoomMember == null) { // chatRoomMember為null則取本地成員資料
                            val chatRoomMember = ChatRoomReference.getInstance().queryChatMembers(item.id)
                            item.chatRoomMember = chatRoomMember
                            Log.d("Kyle122", "checkChatRoomRules chatRoomMember = null and db chatRoomMember size =${item.chatRoomMember.size}")
                        }
                        item
                    }.toMutableList()
            filterList.forEach {
                // 臨時成員聊天室
                if (it.provisionalIds.contains(selfUserId)) {
                    it.roomType = ChatRoomType.provisional
                }

                // 商務號擁有者秘書聊天室
                if (it.type == ChatRoomType.serviceMember && it.serviceNumberType == ServiceNumberType.BOSS) {
                    it.roomType = ChatRoomType.bossSecretary
                }

                // 內部服務號
                if (it.type == ChatRoomType.services && it.serviceNumberOpenType.contains("I")) {
                    it.roomType = ChatRoomType.subscribe
                }

                if (it.roomType == null || it.roomType == ChatRoomType.undef) {
                    it.roomType = it.type
                }
            }
            Log.d("Kyle111", String.format("checkChatRoomRules use time->%s/秒  ", (System.currentTimeMillis() - dateTime) / 1000.0))
            filterList.sort()
            sendChatRoomList.emit(filterList)
        }

    // 多人聊天室更新 chat member & title
    fun updateDiscussOrGroupRoom(
        roomId: String,
        roomType: ChatRoomType = ChatRoomType.undef
    ) = viewModelScope.launch {
        if (!(roomType == ChatRoomType.discuss || roomType == ChatRoomType.group)) return@launch
        val chatRoomEntity = ChatRoomReference.getInstance().findById(roomId)
        chatRoomEntity?.let {
            checkTokenValid(
                chatRepository.getDiscussRoomChatMember(
                    roomId,
                    roomType,
                    chatRoomEntity.isCustomName
                )
            )?.collect {
                when (it) {
                    is ApiResult.Success -> {
                        refreshListByDb()
                        val chatRoomMember = ChatRoomReference.getInstance().queryChatRoomMember(roomId)
                        if (!chatRoomMember.any { it.memberId == selfUserId }) {
                            disableChatRoom(roomId)
                        }
                    }

                    is ApiResult.Failure -> {
                        when (it.errorMessage.errorCode) {
                            ErrorCode.RoomNotExist.type, ErrorCode.ChatMemberInvalid.type -> {
                                disableChatRoom(roomId)
                            }
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    fun disableChatRoom(roomId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            val isDeleted = DBManager.getInstance().deleteRoomListItem(roomId)
            if (isDeleted) refreshListByDb()
        }

    fun updateDiscussRoomTitle(
        userId: String,
        nickName: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        val list = ChatRoomReference.getInstance().findAllChatRoomsByType(null, selfUserId, ChatRoomType.discuss, false)
        list?.let {
            it.forEach { entity ->
                if (entity.chatRoomMember == null) return@forEach
                if (entity.chatRoomMember.any { member -> member.memberId == userId }) {
                    val chatMembers = ChatRoomReference.getInstance().queryChatRoomMember(entity.id)
                    val discussRoomTitle = TextViewHelper.getDiscussTitle(chatMembers)
                    entity.name = discussRoomTitle
                    entity.members.forEachIndexed { _, m ->
                        if (m.id == userId) {
                            m.nickName = nickName
                            return@forEachIndexed
                        }
                    }
                    ChatRoomReference.getInstance().save(entity)
                }
            }
            sendRefreshByDB.emit(Unit)
        }
    }

    fun getChatRoomMember(roomId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            checkTokenValid(chatRepository.getChatMember(roomId))?.collect {
                when (it) {
                    is ApiResult.Success -> {
                        refreshListByDb()
                    }

                    else -> {
                        // nothing
                    }
                }
            }
        }

    fun updateAvatarInDiscussRoom(
        userId: String,
        avatarId: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        DBManager.getInstance().updateUserAvatarId(userId, avatarId)
        // 更新多人聊天室頭圖
        // 主要是拿多然聊天室去找adapter 位置之後 notifyItemChanged
        val discussRoomList = ChatRoomReference.getInstance().findAllChatRoomsByType(null, selfUserId, ChatRoomType.discuss, true)
        discussRoomList?.let {
            it.forEach { entity ->
                onAvatarUpdated.emit(entity)
            }
        }

        // 更新好友聊天室的頭圖
        val friendRoomList = ChatRoomReference.getInstance().findAllChatRoomsByType(null, selfUserId, ChatRoomType.friend, true)
        friendRoomList?.let {
            it.forEach { entity ->
                if (entity.memberIds.contains(userId)) {
                    DBManager.getInstance().updateFriendRoomAvatarId(entity.id, avatarId)
                    return@forEach
                }
            }
        }
        refreshListByDb()
    }

    fun updateSelfAvatar() =
        viewModelScope.launch(Dispatchers.IO) {
            val selfProfile = DBManager.getInstance().queryUser(selfUserId)
            updateAvatarInDiscussRoom(selfUserId, selfProfile.avatarId)
            val selfRoom = ChatRoomReference.getInstance().findSelfRoom(selfProfile.id)
            selfRoom?.let { onSelfAvatarUpdated.emit(it) }
        }

    fun updateSelfRoomTitle(modifyName: String) =
        viewModelScope.launch(Dispatchers.IO) {
            val selfProfile = DBManager.getInstance().queryUser(selfUserId)
            val selfRoom = ChatRoomReference.getInstance().findById(selfProfile.roomId)
            selfRoom?.let {
                it.name = modifyName
                ChatRoomReference.getInstance().save(it)
                refreshListByDb()
            }
        }

    fun removeProvisionalRoom(roomId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            ChatRoomReference.getInstance().deleteById(roomId)
            refreshListByDb()
        }

    /**
     * 取得有被 At 的聊天室
     * */
    fun getAtChatRoom(content: String) =
        viewModelScope.launch(Dispatchers.IO) {
            val data = JsonHelper.getInstance().from(content, AtMessageSocket::class.java)
            val isAtMe = data.event != AtMessageContentEventEnum.Delete
            ChatRoomReference.getInstance().updateIsAtMeFlag(data.roomId, isAtMe)
            onAtChatRoom.emit(Pair(data.roomId, isAtMe))
        }

    /**
     * 取得所有有被 At 的聊天室
     * */
    private fun getAllAtMeChatRoom() =
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("Kyle111", "getAllAtMeChatRoom()")
            checkTokenValid(chatRepository.getAtChatRoom())?.collect {
                when (it) {
                    is ApiResult.Success -> {
                        if (it.data.isEmpty()) {
                            refreshListByDb()
                            return@collect
                        }
                        it.data.forEach { roomId ->
                            var isHasNotReadAtMessage = false
                            val lastMessageId = MessageReference.findLastMessageIdByRoomId(null, roomId)
                            if (lastMessageId != "") {
                                chatRepository.getMessageList(roomId, lastMessageId).collect {
                                    when (it) {
                                        is ApiResult.Success -> {
                                            it.data?.forEach {
                                                if (it.flag != MessageFlag.READ) {
                                                    isHasNotReadAtMessage = true
                                                    ChatRoomReference
                                                        .getInstance()
                                                        .updateIsAtMeFlag(roomId, true)
                                                }
                                            }
                                            if (!isHasNotReadAtMessage) {
                                                chatRepository.sendAtMessageRead(roomId).collect {}
                                            }
//                                    refreshListByDb()
                                        }

                                        is ApiResult.Failure -> {
//                                    refreshListByDb()
                                        }

                                        else -> {
                                            // nothing
                                        }
                                    }
                                }
                            }
                        }
                        refreshListByDb()
                    }

                    is ApiResult.Failure -> {
                        refreshListByDb()
                    }

                    else -> {
                        // nothing
                    }
                }
            }
        }

    /**
     * 聊天列表第一次載入更多邏輯
     * 先從db抓出所有聊天室資料並且分類重組
     * 再以每20筆加載至UI進行渲染以減少UI loading
     */
    fun doLoadMore(
        currentList: MutableList<Any>,
        lastVisibleItemPosition: Int
    ) = viewModelScope.launch(Dispatchers.IO) {
        Log.d("Kyle116", "doLoadMore(), offset = ${currentList.size}, lastVisibleItemPosition = $lastVisibleItemPosition")
        currentIndex = lastVisibleItemPosition
        if (!isLoadMoreIng) {
            isLoadMoreIng = true
            val list = ChatRoomReference.getInstance().findMainChatRoomData(currentList.size, 20)
            sendChatRoomListByLoadMore.emit(currentList.plus(list).toMutableList())
        }
    }

    fun changeLastMessage(roomId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            val lastMessage = MessageReference.findRoomLastMessage(roomId)
            ChatRoomReference.getInstance().updateLastMessage(roomId, lastMessage)
            refreshListByDb()
        }

    fun getDiscussChatMember(
        roomId: String,
        isCustomName: Boolean
    ) = viewModelScope.launch(Dispatchers.IO) {
        checkTokenValid(chatRepository.getDiscussRoomChatMember(roomId, ChatRoomType.discuss, isCustomName))?.collect {
            when (it) {
                is ApiResult.Success -> {
                    refreshListByDb()
                }

                else -> {}
            }
        }
    }

    fun getGroupChatMember(roomId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            checkTokenValid(chatRepository.getChatMember(roomId))?.collect {
                when (it) {
                    is ApiResult.Success -> {
                        refreshListByDb()
                    }

                    else -> {
                        // nothing
                    }
                }
            }
        }
}
