package tw.com.chainsea.chat.searchfilter.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.core.os.bundleOf
import androidx.core.util.Pair
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.ce.sdk.bean.GroupEntity
import tw.com.chainsea.ce.sdk.bean.ServiceNum
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType
import tw.com.chainsea.ce.sdk.cache.ChatMemberCacheService
import tw.com.chainsea.ce.sdk.database.DBManager
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.http.ce.ApiManager
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener
import tw.com.chainsea.ce.sdk.http.ce.base.BaseViewModel
import tw.com.chainsea.ce.sdk.http.ce.base.TokenRepository
import tw.com.chainsea.ce.sdk.http.ce.request.UploadManager
import tw.com.chainsea.ce.sdk.http.ce.request.UploadManager.OnUploadAvatarListener
import tw.com.chainsea.ce.sdk.network.model.common.ApiResult
import tw.com.chainsea.ce.sdk.reference.AccountRoomRelReference
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference
import tw.com.chainsea.ce.sdk.reference.UserProfileReference
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.config.BundleKey
import tw.com.chainsea.chat.config.InvitationType
import tw.com.chainsea.chat.lib.Tools
import tw.com.chainsea.chat.searchfilter.repository.SearchFilterSharedRepository
import tw.com.chainsea.chat.service.ActivityTransitionsControl
import tw.com.chainsea.chat.ui.activity.ChatNormalActivity
import tw.com.chainsea.chat.util.DaVinci
import tw.com.chainsea.chat.util.IntentUtil
import tw.com.chainsea.chat.view.base.BottomTabEnum
import tw.com.chainsea.chat.view.chat.ChatRepository

class SearchFilterSharedViewModel(
    private val application: Application,
    private val searchFilterSharedRepository: SearchFilterSharedRepository,
    private val chatRepository: ChatRepository,
    private val tokenRepository: TokenRepository
) : BaseViewModel(application, tokenRepository) {
    val etGroupName = MutableStateFlow("")
    val createGroupSuccessful = MutableSharedFlow<String>()
    val createGroupFailure = MutableSharedFlow<String>()
    val employeeSelectedList: MutableList<Any> = arrayListOf() // for selected list
    val selectedMemberList = MutableSharedFlow<List<Any>>()
    val sharedPageType = MutableStateFlow(SearchFilterPageType.UNDEF)
    val submitMemberIds = MutableSharedFlow<List<String>>()
    lateinit var transferMessageIds: Array<String>
    val transferMsgSuccess = MutableSharedFlow<Pair<Int, String>>()
    val transferMsgFailure = MutableSharedFlow<String>()
    val transferMsgNotFoundUser = MutableSharedFlow<String>()
    val sendSelectedMemberItem = MutableSharedFlow<Any>()
    val sendDismissSelectedMemberItem = MutableSharedFlow<Any>()
    val ownerId = MutableStateFlow("")
    val memberIds: MutableList<String> = mutableListOf()
    val serviceNumberId = MutableStateFlow("")
    val etGlobalSearchInput = MutableStateFlow("")
    val sendCollectDataDone = MutableSharedFlow<Int>()
    val globalSearchTabEntry = MutableStateFlow(BottomTabEnum.MAIN)
    val sendTotalResultCount = MutableSharedFlow<kotlin.Pair<SearchResultTab, Int>>()
    val activeServiceNUmberId = MutableStateFlow("")
    val activeRoomId = MutableStateFlow("")
    val activeConsultAIId = MutableStateFlow("")
    val sendCompleted = MutableSharedFlow<Unit>()
    val userId = TokenPref.getInstance(application).userId

    //    val activeServiceNumberConsultArray: MutableList<ActiveServiceConsultArray> = mutableListOf()
    enum class SearchFilterPageType {
        CREATE_GROUP,
        GLOBAL_SEARCH,
        MESSAGE_TRANSFER,
        INVITATION,
        PROVISIONAL_MEMBER,
        SHARE_IN,
        CREATE_DISCUSS,
        TRANSFER_TO_GROUP,
        SERVICE_NUMBER_CONSULTATION_AI,
        ShareScreenShot,
        UNDEF
    }

    enum class SearchResultTab {
        CONTACT,
        CHAT_ROOM,
        SERVICE_NUMBER
    }

    // Share data between fragments start ---
    val sendOwnerSelectedItem = MutableSharedFlow<Any>()
    val sendEmployeeSelectedItem = MutableSharedFlow<kotlin.Pair<String, Any>>()
    val sendInputText = MutableSharedFlow<String>()
    val employeesTextRecordList = MutableSharedFlow<String>()
    val sendOnRemoveEmployeeItem = MutableSharedFlow<UserProfileEntity>()
    val sendAvatarSelectedItem = MutableSharedFlow<Any>()
    val sendGlobalSearchNewRecord = MutableSharedFlow<kotlin.Pair<String, Any>>()
    var localAllMessagesEntities: MutableList<MessageEntity> = mutableListOf()
    // Share data between fragments end ---

    // for global search page start ---
    val globalSearchInput = MutableStateFlow("")
    val sendVerifyCountAndTransfer = MutableSharedFlow<Unit>()
    // for global search page end ---

    // discuss room start ----
    val sendMemberName = MutableSharedFlow<Triple<String, Map<String, String>, List<String>>>()
    private val discussRoomName = MutableStateFlow("")
    val discussMemberIds = mutableMapOf<String, String>()
    val sendCreateDiscussRoomSuccess = MutableSharedFlow<ChatRoomEntity>()
    val sendCreateDiscussRoomFailure = MutableSharedFlow<Int>()
    // discuss room end ----

    val userMemberIds: MutableList<String> = mutableListOf() // 暫存「與好友建立多人聊天室」及「多人轉為社團」的memberIds

    // transfer to group room start ----
    val sendTransferToCrowdRoomSuccess = MutableSharedFlow<String>()
    val sendTransferToCrowdRoomFailure = MutableSharedFlow<Int>()

    // transfer to group room end ----
    val sendResultForServiceNumberConsultation = MutableSharedFlow<String>()
    val sendOnAddProvisionalMemberComplete = MutableSharedFlow<MutableList<String>>()
    val sendToastFailMsg = MutableSharedFlow<String>()
    val sendOnCompleteMemberAdded = MutableSharedFlow<ArrayList<UserProfileEntity>>()

    fun createGroup(name: String) =
        viewModelScope.launch(Dispatchers.IO) {
            val userIds: MutableList<String> = mutableListOf()
            val path = DaVinci.with().imageLoader.getAbsolutePath(name)

            for (item in employeeSelectedList) {
                if (item is UserProfileEntity) {
                    userIds.add(item.id)
                }
                if (item is GroupEntity) {
                    userIds.add(item.id)
                }
            }

            UploadManager.getInstance().uploadGroupAvatar(
                application,
                TokenPref.getInstance(application).tokenId,
                path,
                Tools.getSize(path),
                object : OnUploadAvatarListener {
                    override fun onUploadSuccess(groupId: String) {
                        getGroupRoomItem(groupId)
                    }

                    override fun onUploadFailed(reason: String) {
                        viewModelScope.launch {
                            createGroupFailure.emit("社團建立失敗")
                        }
                    }
                },
                userIds,
                etGroupName.value.trim()
            )
        }

    fun transferToGroup(
        name: String,
        roomId: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        val userIds: MutableList<String> = mutableListOf()
        val path = DaVinci.with().imageLoader.getAbsolutePath(name)

        for (item in employeeSelectedList) {
            if (item is UserProfileEntity) {
                userIds.add(item.id)
            }
        }
        UploadManager.getInstance().upgradeGroupWithAvatar(
            application,
            TokenPref.getInstance(application).tokenId,
            path,
            Tools.getSize(path),
            userIds,
            object : UploadManager.OnUpgradeWithAvatarListener {
                override fun onUploadSuccess(
                    groupId: String,
                    groupOwnerId: String
                ) {
                    getCrowdRoomItem(groupId)
                }

                override fun onUploadFailed(reason: String) {
                    CoroutineScope(Dispatchers.IO).launch {
                        sendTransferToCrowdRoomFailure.emit(R.string.text_transfer_to_crowd_failure)
                    }
                }
            },
            roomId,
            etGroupName.value.trim()
        )
    }

    fun onInvitationClick(
        context: Context,
        userId: String,
        roomType: String?
    ) = viewModelScope.launch(Dispatchers.IO) {
        val ids: MutableList<String> = mutableListOf()
        when (sharedPageType.value) {
            SearchFilterPageType.INVITATION -> { // 邀請聯絡人, 聊天室
                roomType?.let { type ->
                    if (type == InvitationType.Discuss.name) { // 只針對快來新增聊天吧入口點
                        if (employeeSelectedList.size == 1) { // 只選中一個聯絡人/聊天室則直接跳轉
                            when (employeeSelectedList[0]) {
                                is UserProfileEntity -> {
                                    val user = employeeSelectedList[0] as UserProfileEntity

                                    if (user.roomId.isNullOrEmpty()) {
                                        val bundle =
                                            bundleOf(
                                                BundleKey.USER_ID.key() to user.id,
                                                BundleKey.USER_NICKNAME.key() to user.nickName
                                            )
                                        IntentUtil.startIntent(context, ChatNormalActivity::class.java, bundle)
                                    } else {
                                        ActivityTransitionsControl.navigateToChat(
                                            context,
                                            user.roomId
                                        ) { i, _ -> context.startActivity(i) }
                                    }
                                }

                                is ChatRoomEntity -> {
                                    val chatRoom = employeeSelectedList[0] as ChatRoomEntity
                                    val bundle =
                                        bundleOf(
                                            BundleKey.EXTRA_SESSION_ID.key() to chatRoom.id
                                        )
                                    IntentUtil.startIntent(context, ChatNormalActivity::class.java, bundle)
                                }
                            }
                            sendCompleted.emit(Unit)
                        } else {
                            inviteMembers(employeeSelectedList)
                        }
                    } else {
                        inviteMembers(employeeSelectedList)
                    }
                }
            }

            SearchFilterPageType.PROVISIONAL_MEMBER -> { // 邀請臨時成員
                inviteMembers(employeeSelectedList)
            }

            SearchFilterPageType.ShareScreenShot,
            SearchFilterPageType.SHARE_IN -> { // 內部分享
                for (item in employeeSelectedList) {
                    if (item is UserProfileEntity) {
                        ids.add(item.roomId)
                    }
                    if (item is GroupEntity) {
                        ids.add(item.id)
                    }
                    if (item is ChatRoomEntity) {
                        when (item.type) {
                            ChatRoomType.person,
                            ChatRoomType.group,
                            ChatRoomType.discuss -> {
                                ids.add(item.id)
                            }

                            else -> {
                                if (item.chatRoomMember != null) {
                                    val selectedUser =
                                        UserProfileReference.findById(
                                            null,
                                            item.chatRoomMember
                                                .filterNot { it.memberId == userId }
                                                .firstOrNull()
                                                ?.memberId
                                        )
                                    selectedUser?.let {
                                        ids.add(it.roomId)
                                    }
                                } else {
                                    ids.add(item.id)
                                }
                            }
                        }
                    }
                    if (item is ServiceNum) {
                        ids.add(item.roomId)
                    }
                }
                submitMemberIds.emit(ids)
            }

            SearchFilterPageType.MESSAGE_TRANSFER -> { // 轉發
                for (item in employeeSelectedList) {
                    if (item is UserProfileEntity) {
                        ids.add(item.roomId)
                    }
                    if (item is GroupEntity) {
                        ids.add(item.id)
                    }
                    if (item is ChatRoomEntity) {
                        ids.add(item.id)
                    }
                    if (item is ServiceNum) {
                        ids.add(item.roomId)
                    }
                }
                submitTransferAction(ids)
            }

            else -> {}
        }
    }

    private fun inviteMembers(memberList: MutableList<Any>) =
        viewModelScope.launch(Dispatchers.IO) {
            val ids: MutableList<String> = mutableListOf()
            for (item in memberList) {
                if (item is UserProfileEntity) {
                    ids.add(item.id)
                }
                if (item is GroupEntity) {
                    ids.add(item.id)
                }
                if (item is ChatRoomEntity) {
                    val selectedUser = UserProfileReference.findById(null, item.memberIds.filterNot { it == userId }.firstOrNull())
                    selectedUser?.let {
                        ids.add(it.id)
                    }
                }
            }
            submitMemberIds.emit(ids)
        }

    private fun submitTransferAction(roomIds: List<String>) =
        viewModelScope.launch(Dispatchers.IO) {
            if (roomIds.isNotEmpty()) {
                ApiManager.doMessageForward(
                    application,
                    transferMessageIds.toMutableList(),
                    roomIds,
                    object : ApiListener<List<MessageEntity?>?> {
                        override fun onSuccess(list: List<MessageEntity?>?) {
                            // 如果送單選轉發，直接跳到轉發成功的聊天室
                            viewModelScope.launch { transferMsgSuccess.emit(Pair(roomIds.size, roomIds[0])) }
                        }

                        override fun onFailed(errorMessage: String) {
                            viewModelScope.launch { transferMsgFailure.emit(errorMessage) }
                        }
                    }
                )
            }
        }

    fun doCreateRoom(
        ids: List<String>,
        type: ChatRoomType,
        roomName: String = "",
        isCustomName: Boolean = false
    ) = viewModelScope.launch(Dispatchers.IO) {
        checkTokenValid(chatRepository.doCreateRoom(ids, type, roomName, isCustomName))?.collect {
            when (it) {
                is ApiResult.Success -> {
                    getDiscussRoomItem(it.data, roomName, isCustomName) // 向server拿room item資料
                }

                is ApiResult.Failure -> {
                    sendCreateDiscussRoomFailure.emit(R.string.text_new_chat_room_failure)
                    Log.e("doCreateRoom", "create room failure error =${it.errorMessage}")
                }

                else -> {}
            }
        }
    }

    private fun getDiscussRoomItem(
        roomId: String,
        roomName: String = "",
        isCustomName: Boolean = false
    ) = viewModelScope.launch(Dispatchers.IO) {
        checkTokenValid(chatRepository.getRoomItem(roomId, userId))?.collect {
            when (it) {
                is ApiResult.Failure -> {
                    sendCreateDiscussRoomFailure.emit(R.string.text_new_chat_room_failure)
                }

                is ApiResult.Success -> {
                    if (!isCustomName && roomName.isNotEmpty()) {
                        it.data.name = roomName
                    }
                    val status: Boolean = ChatRoomReference.getInstance().save(it.data)
                    if (status) {
                        checkTokenValid(chatRepository.getChatMember(roomId))?.collect { list ->
                            when (list) {
                                is ApiResult.Success -> {
                                    sendCreateDiscussRoomSuccess.emit(it.data)
                                }

                                is ApiResult.Failure -> {
                                    sendCreateDiscussRoomFailure.emit(R.string.text_members_info_failure)
                                }

                                else -> {}
                            }
                        }
                    }
                }

                else -> {}
            }
        }
    }

    private fun getGroupRoomItem(roomId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            checkTokenValid(chatRepository.getRoomItem(roomId, userId))?.collect {
                when (it) {
                    is ApiResult.Failure -> {
                        createGroupFailure.emit("取得聊天室資訊失敗")
                    }

                    is ApiResult.Success -> {
                        val status: Boolean = ChatRoomReference.getInstance().save(it.data)
                        if (status) {
                            checkTokenValid(chatRepository.getChatMember(roomId))?.collect { response ->
                                when (response) {
                                    is ApiResult.Success -> {
                                        createGroupSuccessful.emit(it.data.id)
                                    }

                                    is ApiResult.Failure -> {
                                        createGroupFailure.emit(application.getString(R.string.text_members_info_failure))
                                    }

                                    else -> {}
                                }
                            }
                        }
                    }

                    else -> {}
                }
            }
        }

    private fun getCrowdRoomItem(roomId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            checkTokenValid(chatRepository.getRoomItem(roomId, userId))?.collect {
                when (it) {
                    is ApiResult.Failure -> {
                        sendTransferToCrowdRoomFailure.emit(R.string.text_transfer_to_crowd_failure)
                    }

                    is ApiResult.Success -> {
                        val status: Boolean = ChatRoomReference.getInstance().save(it.data)
                        if (status) {
                            checkTokenValid(chatRepository.getChatMember(roomId))?.collect { response ->
                                when (response) {
                                    is ApiResult.Success -> {
                                        sendTransferToCrowdRoomSuccess.emit(it.data.id)
                                    }

                                    is ApiResult.Failure -> {
                                        createGroupFailure.emit(application.getString(R.string.text_members_info_failure))
                                    }

                                    else -> {}
                                }
                            }
                        }
                    }

                    else -> {}
                }
            }
        }

    fun appendDiscussRoomMemberName(memberIds: MutableList<String>) =
        viewModelScope.launch(Dispatchers.IO) {
            memberIds.forEach {
                searchFilterSharedRepository
                    .queryFriend(it)
                    .onEach { entity ->
                        discussMemberIds[entity.id] = entity.nickName.ifEmpty { entity.alias.ifEmpty { entity.name } }
                        discussRoomName.value = discussMemberIds.values.joinToString(",")
                    }.collect()
            }
            sendMemberName.emit(Triple(discussRoomName.value, discussMemberIds, memberIds))
        }

    fun handleDiscussRoomMemberName(
        item: Any,
        selected: Boolean
    ) = viewModelScope.launch {
        when (item) {
            is UserProfileEntity -> {
                if (selected) { // true: 點擊新增, false: 點擊移除
                    discussMemberIds[item.id] = item.nickName.ifEmpty { item.alias.ifEmpty { item.name } }
                } else {
                    discussMemberIds.remove(item.id)
                }
                discussRoomName.value = discussMemberIds.values.joinToString(",")

                val memberIds = mutableListOf<String>()
                for (id in discussMemberIds.keys) {
                    memberIds.add(id)
                }

                sendMemberName.emit(
                    Triple(
                        discussRoomName.value,
                        discussMemberIds,
                        memberIds
                    )
                )
            }
        }
    }

    fun doChatMemberAdd(
        roomId: String,
        memberIds: List<String>
    ) {
        ApiManager.doMemberAdd(
            application,
            roomId,
            memberIds,
            object : ApiListener<String?> {
                override fun onSuccess(s: String?) {
                    val userEntity = arrayListOf<UserProfileEntity>()
                    for (newAccountId in memberIds) {
                        val account = DBManager.getInstance().queryFriend(newAccountId)
                        userEntity.add(account)
                        AccountRoomRelReference.saveByAccountIdAndRoomId(null, newAccountId, roomId)
                        ChatMemberCacheService.refresh(roomId)
                    }
                    CoroutineScope(Dispatchers.Main).launch {
                        sendOnCompleteMemberAdded.emit(userEntity)
                    }
                }

                override fun onFailed(errorMessage: String) {
                    CoroutineScope(Dispatchers.Main).launch {
                        sendToastFailMsg.emit(errorMessage)
                    }
                }
            }
        )
    }

    fun doProvisionalMemberAdd(
        roomId: String,
        memberIds: List<String>
    ) = viewModelScope.launch(Dispatchers.IO) {
        ApiManager.doMemberAdd(
            application,
            roomId,
            memberIds,
            object : ApiListener<String?> {
                override fun onSuccess(s: String?) {
                    CoroutineScope(Dispatchers.Main).launch {
                        sendOnAddProvisionalMemberComplete.emit(memberIds.toMutableList())
                    }
                }

                override fun onFailed(errorMessage: String) {
                    CoroutineScope(Dispatchers.Main).launch {
                        sendToastFailMsg.emit(errorMessage)
                    }
                }
            }
        )
    }
}
