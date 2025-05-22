package tw.com.chainsea.chat.mainpage.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.common.collect.Lists
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import tw.com.chainsea.ce.sdk.bean.GroupRefreshBean
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType
import tw.com.chainsea.ce.sdk.bean.servicenumber.type.GroupPrivilegeEnum
import tw.com.chainsea.ce.sdk.database.DBContract
import tw.com.chainsea.ce.sdk.database.DBManager
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.event.EventBusUtils
import tw.com.chainsea.ce.sdk.event.EventMsg
import tw.com.chainsea.ce.sdk.event.MsgConstant
import tw.com.chainsea.ce.sdk.http.ce.ApiManager
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener
import tw.com.chainsea.ce.sdk.http.ce.base.BaseViewModel
import tw.com.chainsea.ce.sdk.http.ce.base.TokenRepository
import tw.com.chainsea.ce.sdk.http.ce.request.UploadManager
import tw.com.chainsea.ce.sdk.http.ce.request.UploadManager.OnPicUploadListener
import tw.com.chainsea.ce.sdk.network.model.common.ApiResult
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference
import tw.com.chainsea.ce.sdk.service.TodoService
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.lib.Tools
import tw.com.chainsea.chat.mainpage.model.NewMemberEntity
import tw.com.chainsea.chat.mainpage.repository.MainPageRepository
import tw.com.chainsea.chat.util.DaVinci
import tw.com.chainsea.chat.util.SortUtil
import tw.com.chainsea.chat.view.chat.ChatRepository

class MainPageViewModel(
    private val application: Application,
    private val repository: MainPageRepository,
    private val chatRepository: ChatRepository,
    private val tokenRepository: TokenRepository
) : BaseViewModel(application, tokenRepository) {
    val chatRoomName = MutableStateFlow("")
    val chatRoomId = MutableStateFlow("")
    val sendNavigateToPage = MutableSharedFlow<UserProfileEntity>()
    val sendInviteNewMember = MutableSharedFlow<NewMemberEntity>()
    val entity = MutableStateFlow(ChatRoomEntity())
    val sendUpdateRoomNameSuccess = MutableSharedFlow<Pair<Boolean, ChatRoomEntity>>()
    val sendUpdateRoomNameFailure = MutableSharedFlow<Unit>()
    val memberList: MutableList<UserProfileEntity> = Lists.newArrayList()
    val sendUploadMessage = MutableSharedFlow<Int>()
    val sendUpdateRoomAvatarId = MutableSharedFlow<String?>()
    val sendHomePagePics = MutableLiveData<String>()
    val sendSortedMemberList = MutableSharedFlow<Triple<ChatRoomEntity, List<UserProfileEntity>, Boolean>>()
    val showAllMembers = MutableSharedFlow<Unit>()
    val privilege = MutableStateFlow(GroupPrivilegeEnum.UNDEF)
    val ownerId = MutableStateFlow("")
    val sendToast = MutableSharedFlow<Int>()
    val isExitCrowdSuccess = MutableSharedFlow<String>()
    val isExitDiscussRoomSuccess = MutableSharedFlow<String>()
    val sendNoticeRoomOwnerChanged = MutableSharedFlow<Pair<String, String>>()
    val keyWord = MutableStateFlow("")
    val refreshGroupMemberPermission = MutableSharedFlow<Unit>()
    var navState: Int? = null
    val sendCloseMainPage = MutableSharedFlow<Int>()

    enum class PhotoType {
        AVATAR,
        BACKGROUND,
        UNDEF
    }

    val photoType = MutableStateFlow(PhotoType.UNDEF)

    fun onAllMembers() =
        viewModelScope.launch {
            showAllMembers.emit(Unit)
        }

    fun getMemberIdsList(list: List<UserProfileEntity>): ArrayList<String> = list.map { it.id }.toCollection(ArrayList())

    fun onSelectEmployeeItem(item: Any) =
        viewModelScope.launch {
            when (item) {
                is UserProfileEntity -> sendNavigateToPage.emit(item)
                is NewMemberEntity -> sendInviteNewMember.emit(item)
            }
        }

    fun onShowAllMembersClick() =
        viewModelScope.launch {
            showAllMembers.emit(Unit)
        }

    fun getChatRoomEntity(
        userId: String,
        id: String?
    ) = viewModelScope.launch(Dispatchers.IO) {
        ownerId.value = userId
        id?.let {
            chatRoomId.value = it
            repository
                .getChatRoomEntity(it)
                .onEach { entity ->
                    entity?.let { e ->
                        getChatRoomMember(it, e)
                    }
                }.collect()
            getCrowdHomePagePics(it)
        }
    }

    // 更新聊天室名稱
    fun doUpdateChatRoomName(
        name: String,
        roomId: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        checkTokenValid(chatRepository.doUpdateRoomName(roomId, name))?.collect {
            when (it) {
                is ApiResult.Success -> {
                    getRoomItem(roomId)
                }

                is ApiResult.Failure -> {
                    sendUpdateRoomNameFailure.emit(Unit)
                }

                else -> {}
            }
        } ?: run {
            sendUpdateRoomNameFailure.emit(Unit)
        }
    }

    private fun getRoomItem(roomId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            val userId = TokenPref.getInstance(application).userId
            checkTokenValid(chatRepository.getRoomItem(roomId, userId))?.collect {
                when (it) {
                    is ApiResult.Failure -> {
                        sendUpdateRoomNameFailure.emit(Unit)
                    }

                    is ApiResult.Success -> {
                        repository
                            .updateChatRoomName(it.data.name, it.data.id)
                            .onEach { result ->
                                sendUpdateRoomNameSuccess.emit(Pair(result, it.data))
                            }.collect()
                    }

                    else -> {}
                }
            } ?: run {
                sendUpdateRoomNameFailure.emit(Unit)
            }
        }

    fun doUploadAvatar(
        avatarPath: String,
        token: String,
        roomId: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        val path = DaVinci.with().imageLoader.getAbsolutePath(avatarPath)
        UploadManager.getInstance().uploadRoomAvatar(
            application,
            roomId,
            token,
            path,
            Tools.getSize(path),
            object : UploadManager.OnUploadAvatarListener {
                override fun onUploadSuccess(avatarId: String?) {
                    avatarId?.let { avatar ->
                        DBManager.getInstance().updateGroupField(
                            roomId,
                            DBContract.GroupEntry.COLUMN_AVATAR_URL,
                            avatar
                        )
                        CoroutineScope(Dispatchers.Main).launch {
                            sendUploadMessage.emit(R.string.text_upload_image_success_and_updating)
                        }
                        CoroutineScope(Dispatchers.IO).launch {
                            sendUpdateRoomAvatarId.emit(avatar)
                        }
                    } ?: run {
                        CoroutineScope(Dispatchers.Main).launch {
                            sendUploadMessage.emit(R.string.text_upload_image_failure)
                        }
                    }
                }

                override fun onUploadFailed(reason: String?) {
                    CoroutineScope(Dispatchers.Main).launch {
                        sendUploadMessage.emit(R.string.text_upload_image_failure)
                    }
                }
            }
        )
    }

    fun doUploadBackground(
        path: String,
        token: String,
        roomId: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        val imagePath = DaVinci.with().imageLoader.getAbsolutePath(path)
        UploadManager.getInstance().uploadPic(
            application,
            token,
            imagePath,
            object : OnPicUploadListener {
                override fun onUploadSuccess(url: String) {
                    val picUrls: MutableList<String> = java.util.ArrayList()
                    picUrls.add(url)
                    CoroutineScope(Dispatchers.Main).launch {
                        sendUploadMessage.emit(R.string.text_upload_image_success_and_updating)
                    }
                    ApiManager.doRoomHomePagePicsUpdate(
                        application,
                        roomId,
                        picUrls,
                        object : ApiListener<String?> {
                            override fun onSuccess(e: String?) {
                                getCrowdHomePagePics(roomId, isFromUpdated = true)
                            }

                            override fun onFailed(errorMessage: String?) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    sendUploadMessage.emit(R.string.text_upload_image_failure)
                                }
                            }
                        }
                    )
                }

                override fun onUploadFailed(reason: String) {
                    CoroutineScope(Dispatchers.Main).launch {
                        sendUploadMessage.emit(R.string.text_upload_image_failure)
                    }
                }
            }
        )
    }

    private fun getCrowdHomePagePics(
        roomId: String,
        isFromUpdated: Boolean = false
    ) = viewModelScope.launch(Dispatchers.IO) {
        checkTokenValid(chatRepository.getCrowdHomePagePics(roomId))?.collect {
            when (it) {
                is ApiResult.Success -> {
                    it.data.lastOrNull()?.picUrl?.let { pic ->
                        sendHomePagePics.postValue(pic)
                        if (isFromUpdated) {
                            CoroutineScope(Dispatchers.Main).launch {
                                sendUploadMessage.emit(R.string.text_update_image_success)
                            }
                        }
                    }
                }

                is ApiResult.Failure -> {
                    Log.e("getCrowdHomePagePics", "error=${it.errorMessage}")
                }

                else -> {}
            }
        }
    }

    // 取得最新成員列表
    fun getChatRoomMember(
        roomId: String,
        entity: ChatRoomEntity,
        isTransferOwner: Boolean = false
    ) = viewModelScope.launch(Dispatchers.IO) {
        var newList = mutableListOf<UserProfileEntity>()
        checkTokenValid(chatRepository.getChatMember(roomId))?.collect {
            when (it) {
                is ApiResult.Success -> {
                    it.data.forEach { member ->
                        val userProfileEntity = DBManager.getInstance().queryUser(member.memberId)
                        if (entity.ownerId == member.memberId && entity.type == ChatRoomType.group) {
                            userProfileEntity.groupPrivilege = GroupPrivilegeEnum.Owner
                        } else {
                            userProfileEntity.groupPrivilege = member.privilege
                        }
                        newList.add(userProfileEntity)
                    }
                    if (entity.type == ChatRoomType.group) {
                        newList = SortUtil.sortGroupOwnerManagerByPrivilege(newList)
                        getMemberPrivilege(ownerId.value, newList) // 先判斷使用者在社團內的權限
                    }
                    memberList.clear()
                    memberList.addAll(newList)
                    sendSortedMemberList.emit(
                        Triple(
                            entity,
                            if (keyWord.value.isNotEmpty()) {
                                memberList.filter { member -> member.nickName.contains(keyWord.value, true) }
                            } else {
                                memberList
                            },
                            isTransferOwner
                        )
                    )
                    EventBusUtils.sendEvent(
                        EventMsg(
                            MsgConstant.GROUP_REFRESH_FILTER,
                            GroupRefreshBean(chatRoomId.value)
                        )
                    )
                }

                else -> {}
            }
        }
    }

    private fun getMemberPrivilege(
        userId: String,
        memberList: List<UserProfileEntity>
    ) = viewModelScope.launch {
        memberList.find { it.id == userId }?.let { user ->
            privilege.value = user.groupPrivilege
        }
        refreshGroupMemberPermission.emit(Unit)
    }

    fun searchMember(keyword: String) =
        viewModelScope.launch(Dispatchers.IO) {
            keyWord.value = keyword
            val list = memberList.filter { it.nickName.contains(keyword, true) }
            sendSortedMemberList.emit(Triple(entity.value, list, false))
        }

    /**
     * 社團/多人聊天室移除成員
     */
    fun doDeleteMember(item: UserProfileEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            checkTokenValid(chatRepository.doDeletedMember(chatRoomId.value, Lists.newArrayList(item.id)))?.collect {
                when (it) {
                    is ApiResult.Success -> {
                        if (it.data) {
                            getChatRoomMember(chatRoomId.value, entity.value)
                            val isDeleted = deletedMemberFromLocalDB(item.id, chatRoomId.value)
                            if (isDeleted) {
                                sendToast.emit(R.string.text_deleted_member_success)
                            }
                        } else {
                            sendToast.emit(R.string.text_toast_device_delete_failure)
                        }
                    }

                    is ApiResult.Failure -> {
                        sendToast.emit(R.string.text_toast_device_delete_failure)
                    }

                    else -> {}
                }
            } ?: run {
                sendToast.emit(R.string.text_toast_device_delete_failure)
            }
        }

    fun doTransferChatRoomOwner(item: UserProfileEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            checkTokenValid(chatRepository.doModifyChatRoomOwner(chatRoomId.value, item.id))?.collect {
                when (it) {
                    is ApiResult.Success -> {
                        if (it.data) {
                            doRoomItem(chatRoomId.value, isTransferOwner = true)
                        } else {
                            sendToast.emit(R.string.text_sure_to_transfer_ownership_failure)
                        }
                    }

                    is ApiResult.Failure -> {
                        sendToast.emit(R.string.text_sure_to_transfer_ownership_failure)
                    }

                    else -> {}
                }
            } ?: run {
                sendToast.emit(R.string.text_sure_to_transfer_ownership_failure)
            }
        }

    fun doRoomItem(
        roomId: String,
        isTransferOwner: Boolean = false
    ) = viewModelScope.launch(Dispatchers.IO) {
        checkTokenValid(chatRepository.getRoomItem(roomId, ownerId.value))?.collect {
            when (it) {
                is ApiResult.Success -> {
                    entity.value = it.data
                    repository
                        .saveChatRoomEntity(it.data)
                        .onEach { isSave ->
                            if (isSave) {
                                getChatRoomMember(chatRoomId.value, it.data, isTransferOwner)
                                sendToast.emit(R.string.text_sure_to_transfer_ownership_success)
                                sendNoticeRoomOwnerChanged.emit(Pair(roomId, it.data.ownerId)) // 通知聊天室擁有者改變
                            } else {
                                sendToast.emit(R.string.text_sure_to_transfer_ownership_failure)
                            }
                        }.collect()
                }

                else -> {}
            }
        }
    }

    fun doDesignateManager(item: UserProfileEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            checkTokenValid(chatRepository.doAddChatRoomManager(chatRoomId.value, Lists.newArrayList(item.id)))?.collect {
                when (it) {
                    is ApiResult.Success -> {
                        if (it.data) {
                            getChatRoomMember(chatRoomId.value, entity.value)
                            sendToast.emit(R.string.text_designate_management_success)
                        } else {
                            sendToast.emit(R.string.text_designate_management_failure)
                        }
                    }

                    is ApiResult.Failure -> {
                        sendToast.emit(R.string.text_designate_management_failure)
                    }

                    else -> {}
                }
            } ?: run {
                sendToast.emit(R.string.text_designate_management_failure)
            }
        }

    fun doCancelManager(item: UserProfileEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            checkTokenValid(chatRepository.doDeleteChatRoomManager(chatRoomId.value, Lists.newArrayList(item.id)))?.collect {
                when (it) {
                    is ApiResult.Success -> {
                        if (it.data) {
                            getChatRoomMember(chatRoomId.value, entity.value)
                            sendToast.emit(R.string.text_cancel_management_success)
                        } else {
                            sendToast.emit(R.string.text_cancel_management_failure)
                        }
                    }

                    is ApiResult.Failure -> {
                        sendToast.emit(R.string.text_cancel_management_failure)
                    }

                    else -> {}
                }
            } ?: run {
                sendToast.emit(R.string.text_cancel_management_failure)
            }
        }

    fun doLeaveChatRoom() =
        viewModelScope.launch(Dispatchers.IO) {
            checkTokenValid(chatRepository.chatMemberExit(chatRoomId.value, ownerId.value))?.collect {
                when (it) {
                    is ApiResult.Success -> {
                        ChatRoomReference.getInstance().deleteById(chatRoomId.value)
                        TodoService.unBindRoom(application, chatRoomId.value, null)
                        isExitDiscussRoomSuccess.emit(chatRoomId.value)
                        sendToast.emit(R.string.text_leave_crowd_success)
                    }

                    is ApiResult.Failure -> {
                        sendToast.emit(R.string.text_leave_crowd_failure)
                    }

                    else -> {}
                }
            } ?: run {
                sendToast.emit(R.string.text_leave_crowd_failure)
            }
        }

    fun doDisbandCrowd() =
        viewModelScope.launch(Dispatchers.IO) {
            checkTokenValid(chatRepository.roomDismiss(chatRoomId.value))?.collect {
                when (it) {
                    is ApiResult.Success -> {
                        ChatRoomReference.getInstance().deleteById(chatRoomId.value)
                        TodoService.unBindRoom(application, chatRoomId.value, null)
                        isExitCrowdSuccess.emit(chatRoomId.value)
                        sendToast.emit(R.string.text_disband_crowd_success)
                    }

                    is ApiResult.Failure -> {
                        sendToast.emit(R.string.text_disband_crowd_failure)
                    }

                    else -> {}
                }
            } ?: run {
                sendToast.emit(R.string.text_disband_crowd_failure)
            }
        }

    fun doCleanMessage() =
        viewModelScope.launch(Dispatchers.IO) {
            checkTokenValid(chatRepository.doCleanChatRoomMessage(chatRoomId.value))?.collect {
                when (it) {
                    is ApiResult.Success -> {
                        if (it.data) {
                            repository
                                .deleteMessageByRoomId(chatRoomId.value)
                                .onEach { isDelete ->
                                    if (isDelete) {
                                        repository
                                            .deleteLastMessageByRoomId(chatRoomId.value)
                                            .onEach { isClear ->
                                                sendToast.emit(if (isClear) R.string.text_clear_chat_record_success else R.string.text_clear_chat_record_failure)
                                            }.collect()
                                    } else {
                                        sendToast.emit(R.string.text_clear_chat_record_failure)
                                    }
                                }.collect()
                        } else {
                            sendToast.emit(R.string.text_clear_chat_record_failure)
                        }
                    }

                    is ApiResult.Failure -> {
                        sendToast.emit(R.string.text_clear_chat_record_failure)
                    }

                    else -> {}
                }
            } ?: run {
                sendToast.emit(R.string.text_clear_chat_record_failure)
            }
        }

    fun doKickOutChatRoomByOtherMember(roomId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            val isDeleted = DBManager.getInstance().deleteRoomListItem(roomId)
            if (isDeleted) {
                sendCloseMainPage.emit(R.string.text_kick_out_member_by_others)
            }
        }

    private fun deletedMemberFromLocalDB(
        userId: String,
        roomId: String
    ) = ChatRoomReference.getInstance().deleteMemberById(userId, roomId) &&
        ChatRoomReference.getInstance().deleteMemberIdsById(userId, roomId)
}
