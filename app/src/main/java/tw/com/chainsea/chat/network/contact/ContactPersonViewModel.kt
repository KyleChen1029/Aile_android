package tw.com.chainsea.chat.network.contact

import android.app.Application
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tw.com.chainsea.android.common.log.CELog
import tw.com.chainsea.ce.sdk.bean.GroupEntity
import tw.com.chainsea.ce.sdk.bean.ServiceNum
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity
import tw.com.chainsea.ce.sdk.bean.account.UserType
import tw.com.chainsea.ce.sdk.bean.label.LabelRequest
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity
import tw.com.chainsea.ce.sdk.database.DBContract
import tw.com.chainsea.ce.sdk.database.DBContract.REFRESH_TIME_SOURCE
import tw.com.chainsea.ce.sdk.database.DBManager
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.database.sp.UserPref
import tw.com.chainsea.ce.sdk.http.ce.base.BaseViewModel
import tw.com.chainsea.ce.sdk.http.ce.base.TokenRepository
import tw.com.chainsea.ce.sdk.http.ce.request.AddContactFriendRequest
import tw.com.chainsea.ce.sdk.network.model.common.ApiResult
import tw.com.chainsea.ce.sdk.network.model.common.BaseRequest
import tw.com.chainsea.ce.sdk.network.model.common.ErrorCode
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference
import tw.com.chainsea.ce.sdk.reference.MessageReference
import tw.com.chainsea.ce.sdk.reference.ServiceNumberReference
import tw.com.chainsea.ce.sdk.service.type.RefreshSource
import tw.com.chainsea.chat.lib.NetworkUtils
import tw.com.chainsea.chat.network.selfprofile.SelfProfileRepository
import tw.com.chainsea.chat.ui.utils.countrycode.GetCountryNameSort
import tw.com.chainsea.chat.view.chat.ChatRepository
import tw.com.chainsea.chat.view.contact.ContactListModel
import tw.com.chainsea.chat.view.contact.ContactViewHolderType

class ContactPersonViewModel(
    private val application: Application,
    private val contactRepository: ContactRepository,
    private val selfProfileRepository: SelfProfileRepository,
    private val chatRepository: ChatRepository,
    private val tokenRepository: TokenRepository
) : BaseViewModel(application, tokenRepository),
    LifecycleObserver {
    // 夥伴 + 好友的 list
    val allContactListData = MutableLiveData<List<UserProfileEntity>>()

    // 好友
    private val friendsListData = MutableLiveData<List<UserProfileEntity>>()

    // 聊天室 data
    val chatRoomData = MutableLiveData<ChatRoomEntity>()

    // 加完好友後的 roomId
    val roomId = MutableLiveData<String?>()

    // 點擊服務號 Home
    val serviceNumberEntity = MutableLiveData<ServiceNumberEntity>()

    val addFriendFailed = MutableLiveData<Triple<Boolean, String, String>>()
    val updatedFriendInfo = MutableLiveData<Triple<Boolean, String, String>>()
    private val selfUserId by lazy { TokenPref.getInstance(application).userId }
    private val _contactListData = mutableListOf<ContactListModel>()
    val contactListData = MutableSharedFlow<MutableList<ContactListModel>>()
    private val groupOpenList = HashSet<ContactViewHolderType>()
    val onGetLocalDataDone = MutableSharedFlow<Boolean>()

    fun getAllData(source: RefreshSource) =
        viewModelScope.launch(Dispatchers.IO) {
            val tasks =
                listOf<suspend () -> Unit>(
                    { getSelfProfile(source) },
                    { getCollectionData(source) },
                    { getSubscribeServiceNumberList(source) },
                    { getGroupRoomList(source) },
                    { getServiceNumberContactList(source) },
                    { getAllContentList(source) },
                    { getAiffList() },
                    { synGroupRoomList() },
                    { getBlockEmployee() }
                )

            // 依序執行每個 suspend 函數
            for (task in tasks) {
                task() // 這裡會等當前函數執行完畢後再執行下一個
            }

            if (source == RefreshSource.LOCAL) {
                onGetLocalDataDone.emit(true)
            }
        }

    /**
     * 取得自己的 profile
     * */
    fun getSelfProfile(source: RefreshSource) {
        viewModelScope.launch(Dispatchers.IO) {
            if (source == RefreshSource.REMOTE) {
                checkTokenValid(selfProfileRepository.getUserProfile(selfUserId, source))?.collect {
                    when (it) {
                        is ApiResult.Loading -> loading.postValue(it.isLoading)
                        is ApiResult.Failure -> {}
                        is ApiResult.Success -> {
                            addListData(ContactViewHolderType.SELF, it.data)
                        }

                        else -> {}
                    }
                }
            } else {
                val profile = DBManager.getInstance().queryUser(selfUserId)
                profile?.let {
                    addListData(ContactViewHolderType.SELF, it)
                } ?: run {
                    getSelfProfile(RefreshSource.REMOTE)
                }
            }
        }
    }

    /**
     * 取得我的收藏(我的最愛
     * */
    fun getCollectionData(source: RefreshSource) {
        viewModelScope.launch(Dispatchers.IO) {
            val labelId = UserPref.getInstance(application).loveLabelId
            if (source == RefreshSource.REMOTE) {
                checkTokenValid(contactRepository.getCollectionData(LabelRequest(labelId), source))?.collect {
                    when (it) {
                        is ApiResult.Loading -> loading.postValue(it.isLoading)
                        is ApiResult.Failure -> errorMessage.postValue(it.errorMessage.errorMessage)
                        is ApiResult.Success -> {
                            addListData(ContactViewHolderType.COLLECTS, it.data)
                        }

                        else -> {}
                    }
                }
            } else {
                // Local
                val collects: MutableList<UserProfileEntity> = mutableListOf()
                val labelList = DBManager.getInstance().queryAllLabels()
                labelList.forEach { label ->
                    if (label.isReadOnly) {
                        label.users.forEach { user ->
                            if (user.labels == null) {
                                user.labels = mutableListOf()
                            }
                            user.labels.add(label)
                        }
                        collects.addAll(label.users)
                    }
                }
                if (collects.isNotEmpty()) {
                    addListData(ContactViewHolderType.COLLECTS, collects)
                } else {
                    getCollectionData(RefreshSource.REMOTE)
                }
            }
        }
    }

    /**
     * 取得訂閱服務號
     * */
    fun getSubscribeServiceNumberList(source: RefreshSource) {
        viewModelScope.launch(Dispatchers.IO) {
            if (source == RefreshSource.REMOTE) {
                checkTokenValid(contactRepository.getSubscribeServiceNumberList(source))?.collect {
                    when (it) {
                        is ApiResult.Loading -> loading.postValue(it.isLoading)
                        is ApiResult.Failure -> errorMessage.postValue(it.errorMessage.errorMessage)
                        is ApiResult.Success -> {
                            if (it.data is List<*>) {
                                val sortedList = (it.data as List<ServiceNum>).sortedByDescending { it.updateTime }
                                addListData(ContactViewHolderType.SUBSCRIBE_SERVICE_NUMBER, sortedList.toMutableList())
                            }
                        }

                        else -> {}
                    }
                }
            } else {
                val serviceNumList = DBManager.getInstance().querySubscribeServiceNumber()
                serviceNumList?.let {
                    val filterList = it.filter { !it.serviceOpenType.contains("C") }
                    addListData(ContactViewHolderType.SUBSCRIBE_SERVICE_NUMBER, filterList.toMutableList())
                } ?: run {
                    getSubscribeServiceNumberList(RefreshSource.REMOTE)
                }
            }
        }
    }

    /**
     * 取得社團
     * */
    fun getGroupRoomList(source: RefreshSource): Job =
        viewModelScope.launch(Dispatchers.IO) {
            if (source == RefreshSource.REMOTE) {
                if (NetworkUtils.isNetworkAvailable(application)) {
                    val createGroup =
                        GroupEntity().apply {
                            id = "isAdd"
                            name = "建立社團"
                        }
                    val result = checkTokenValid(chatRepository.getAllGroupList(0))
                    if (result != null) {
                        result.collect {
                            when (it) {
                                is ApiResult.Loading -> loading.postValue(it.isLoading)
                                is ApiResult.Failure -> {
                                    addListData(ContactViewHolderType.GROUP, mutableListOf(createGroup))
                                    errorMessage.postValue(it.errorMessage.errorMessage)
                                }

                                is ApiResult.Success -> {
                                    val groupList = it.data.toMutableList()
                                    groupList.add(0, createGroup)
                                    addListData(ContactViewHolderType.GROUP, groupList)
                                }

                                else -> {}
                            }
                        }
                    } else {
                        queryLocalGroupList()
                    }
                }
            } else {
                queryLocalGroupList()
            }
        }

    private suspend fun queryLocalGroupList() =
        withContext(Dispatchers.IO) {
            val groupRoomList = DBManager.getInstance().findAllGroups()
            if (groupRoomList.isNotEmpty()) {
                val createGroup =
                    GroupEntity().apply {
                        id = "isAdd"
                        name = "建立社團"
                    }
                groupRoomList.add(0, createGroup)
                addListData(ContactViewHolderType.GROUP, groupRoomList)
            } else {
                getGroupRoomList(RefreshSource.REMOTE)
            }
        }

    fun synGroupRoomList() =
        viewModelScope.launch(Dispatchers.IO) {
            val result = checkTokenValid(chatRepository.getAllGroupList(DBManager.getInstance().getLastRefreshTime(REFRESH_TIME_SOURCE.SYNC_GROUP)))
            if (result != null) {
                result.collect {
                    if (it is ApiResult.Failure) {
                        errorMessage.postValue(it.errorMessage.errorMessage)
                    }
                }
            } else {
                queryLocalGroupList()
            }
        }

    /**
     * 取得服務號客戶
     * */
    fun getServiceNumberContactList(source: RefreshSource): Job =
        viewModelScope.launch(Dispatchers.IO) {
            val bossServiceNumberId = TokenPref.getInstance(application).bossServiceNumberId
            if (source == RefreshSource.REMOTE) {
                val refreshTime =
                    DBManager
                        .getInstance()
                        .getLastRefreshTime(REFRESH_TIME_SOURCE.BOSSSERVICENUMBER_CONTACT_LIST)
                val result = checkTokenValid(contactRepository.getServiceNumberContactList(source, bossServiceNumberId, BaseRequest(refreshTime)))
                if (result != null) {
                    result.collect {
                        when (it) {
                            is ApiResult.Loading -> loading.postValue(it.isLoading)
                            is ApiResult.Failure -> errorMessage.postValue(it.errorMessage.errorMessage)
                            is ApiResult.Success -> {
                                addListData(ContactViewHolderType.CUSTOMER, it.data.toMutableList())
                            }

                            is ApiResult.NextPage -> if (it.hasNextPage) getServiceNumberContactList(RefreshSource.REMOTE)
                            else -> {}
                        }
                    }
                }
            } else {
                val customers =
                    DBManager.getInstance().queryCustomers().filter {
                        it.userType != "visitor" && it.serviceNumberIds.contains(bossServiceNumberId)
                    }
                if (customers.isNotEmpty()) {
                    addListData(ContactViewHolderType.CUSTOMER, customers.toMutableList())
                } else {
                    getServiceNumberContactList(RefreshSource.REMOTE)
                }
            }
        }

    /**
     * 取得黑名單夥伴
     * */
    private fun getBlockEmployee() =
        viewModelScope.launch(Dispatchers.IO) {
            val profiles = DBManager.getInstance().queryBlockFriends()
            addListData(ContactViewHolderType.BLOCK, profiles)
        }

    /**
     * 取得跟自己聊天的聊天室
     * */
    fun getSelfChatRoom() =
        viewModelScope.launch {
            val selfRoom = ChatRoomReference.getInstance().findSelfRoom(selfUserId)
            selfRoom?.let {
                chatRoomData.postValue(it)
            } ?: run {
                checkTokenValid(contactRepository.getUserItem(selfUserId))?.collect {
                    when (it) {
                        is ApiResult.Success -> {
                            val localChatRoom = ChatRoomReference.getInstance().findById2(selfUserId, it.data.personRoomId, true, true, true, true, true)
                            localChatRoom?.let {
                                chatRoomData.postValue(it)
                            } ?: run {
                                getRoomItem(it.data.personRoomId)
                            }
                        }

                        else -> {
                            // nothing
                        }
                    }
                }
            }
        }

    /**
     * 取得聊天室資訊
     * */
    fun getRoomItem(roomId: String) =
        viewModelScope.launch {
            checkTokenValid(chatRepository.getRoomItem(roomId, selfUserId))?.collect {
                when (it) {
                    is ApiResult.Loading -> loading.postValue(it.isLoading)
                    is ApiResult.Failure -> {
                        when (it.errorMessage.errorCode) {
                            ErrorCode.ServiceNumberDisable.type -> {
                                getSubscribeServiceNumberList(RefreshSource.REMOTE)
                            }

                            ErrorCode.RoomNotExist.type -> {
                                DBManager.getInstance().deleteGroup(roomId)
                                DBManager.getInstance().deleteGroupInfo(roomId)
                                getAllData(RefreshSource.REMOTE)
                            }

                            else -> {
                                if ("您不是該聊天室的成員。" == it.errorMessage.errorMessage) {
                                    DBManager.getInstance().deleteGroup(roomId)
                                    DBManager.getInstance().deleteGroupInfo(roomId)
                                    getAllData(RefreshSource.REMOTE)
                                }
                            }
                        }
                        errorMessage.postValue(it.errorMessage.errorMessage)
                    }

                    is ApiResult.Success -> {
                        val status: Boolean = ChatRoomReference.getInstance().save(it.data)
                        if (status) {
                            // 如果該聊天室被滑動刪除，從聯絡人列表點擊後，需要將 DB 的 deleted 狀態改回 N
                            setChatRoomNotDeleted(roomId)
                            chatRoomData.postValue(it.data)
                        } else {
                            errorMessage.postValue("save room entity failed ")
                        }
                    }

                    else -> {}
                }
            }
        }

    /**
     * 加好友取得 roomId
     * */
    fun addContactFriend(
        userId: String,
        alias: String
    ) = viewModelScope.launch {
        checkTokenValid(contactRepository.addContactFriend(AddContactFriendRequest(arrayListOf(userId), alias)))?.collect {
            when (it) {
                is ApiResult.Loading -> loading.postValue(it.isLoading)
                is ApiResult.Failure -> {
                    addFriendFailed.postValue(Triple(false, userId, alias))
                    CELog.e("contact add failed: ${it.errorMessage}")
                }

                is ApiResult.Success -> {
                    launch {
                        syncFriends(RefreshSource.REMOTE)
                        it.data.roomIds?.let {
                            it[0]?.let { roomId ->
                                getRoomItem(roomId)
                            }
                        }
                    }
                }

                else -> {}
            }
        }
    }

    /**
     * 將 friends 和 employee 兩個合起來組成 "夥伴" 的 collection
     */
    fun getAllContentList(source: RefreshSource) {
        if (source == RefreshSource.REMOTE) {
            if (NetworkUtils.isNetworkAvailable(application)) {
                viewModelScope.launch(Dispatchers.IO) {
                    val refreshTime =
                        DBManager.getInstance().getLastRefreshTime(REFRESH_TIME_SOURCE.SYNC_EMPLOYEE)

                    val employeeFlow = async { contactRepository.getEmployeeList(refreshTime, selfUserId, source) }
                    val syncFriendsFlow = async { contactRepository.syncFriends(source) }

                    val employeeData = employeeFlow.await()
                    val friendData = syncFriendsFlow.await()

                    val employeeList: ArrayList<UserProfileEntity> = arrayListOf()
                    val friendList: ArrayList<UserProfileEntity> = arrayListOf()
                    checkTokenValid(employeeData)?.collect {
                        if (it is ApiResult.Success) {
                            employeeList.addAll(it.data)
                        }
                    }

                    checkTokenValid(friendData)?.collect {
                        if (it is ApiResult.Success) {
                            friendList.addAll(it.data)
                        }
                    }

                    employeeList.plus(friendList).toSet().toList()
                    employeeList.sortWith(
                        compareBy {
                            GetCountryNameSort().getSortLetterBySortKey(it.nickName)
                        }
                    )
//                    allContactListData.postValue(employeeList)
                    addListData(ContactViewHolderType.EMPLOYEE, employeeList.toMutableList())
                }
            }
        } else {
            queryLocalAllContentList()
        }
    }

    private fun queryLocalAllContentList() =
        viewModelScope.launch(Dispatchers.IO) {
            // Local
            val employeeList =
                DBManager.getInstance().queryEmployeeList().filter { userProfileEntity ->
                    UserType.EMPLOYEE == userProfileEntity.userType && userProfileEntity.id != selfUserId
                } as ArrayList<UserProfileEntity>
            val friendList = DBManager.getInstance().queryFriends()
            employeeList.plus(friendList).toSet().toList()
            employeeList.sortWith(
                compareBy {
                    GetCountryNameSort().getSortLetterBySortKey(it.nickName)
                }
            )
            if (employeeList.isNotEmpty()) {
                addListData(ContactViewHolderType.EMPLOYEE, employeeList.toMutableList())
            } else {
                getAllContentList(RefreshSource.REMOTE)
            }
        }

    private fun syncFriends(source: RefreshSource) =
        viewModelScope.launch {
            checkTokenValid(contactRepository.syncFriends(source))?.collect {
                when (it) {
                    is ApiResult.Loading -> loading.postValue(it.isLoading)
                    is ApiResult.Failure -> {}
                    is ApiResult.Success -> friendsListData.postValue(it.data)

                    else -> {}
                }
            }
        }

    /**
     * 取得聯絡人頁面 aiff List
     * */
    private fun getAiffList() =
        viewModelScope.launch(Dispatchers.IO) {
            checkTokenValid(contactRepository.getAiffList())?.collect {
                when (it) {
                    is ApiResult.Loading -> loading.postValue(it.isLoading)
                    is ApiResult.Failure -> {
                        Log.e(javaClass.simpleName, "getAiffList error: ${it.errorMessage.errorMessage}")
                    }

                    is ApiResult.Success -> {
                        addListData(ContactViewHolderType.AIFF, it.data.toMutableList())
//                    aiffListData.postValue(it.data)
                    }

                    else -> {}
                }
            }
        }

    private fun addListData(
        type: ContactViewHolderType,
        data: Any
    ) = viewModelScope.launch(Dispatchers.Main) {
        val settings = getSettingsForType(type)

        _contactListData.find { it.type == type }?.let {
            it.data = data
        } ?: run {
            _contactListData.add(
                ContactListModel(type, settings.first, settings.second, data, groupOpenList.any { it == type })
            )
        }

        _contactListData.forEach { contactListData ->
            contactListData.isOpen = groupOpenList.any { it == contactListData.type }
        }

        if (data is MutableList<*> && data.isEmpty()) {
            _contactListData.retainAll { it.type != type }
        }

        _contactListData.sortBy { it.sort }
        contactListData.emit(_contactListData.toMutableList())
    }

    private fun getSettingsForType(type: ContactViewHolderType): Pair<String, Int> =
        when (type) {
            ContactViewHolderType.SELF -> Pair("個人資料", 0)
            ContactViewHolderType.COLLECTS -> Pair("我的收藏", 1)
            ContactViewHolderType.SUBSCRIBE_SERVICE_NUMBER -> Pair("訂閱服務號", 2)
            ContactViewHolderType.GROUP -> Pair("社團", 3)
            ContactViewHolderType.EMPLOYEE -> Pair("夥伴", 4)
            ContactViewHolderType.CUSTOMER -> Pair("客戶", 5)
            ContactViewHolderType.BLOCK -> Pair("封鎖", 6)
            ContactViewHolderType.AIFF -> Pair("應用", 7)
        }

    /**
     * 儲存已經展開的群組
     * @param contactViewHolderType 群組的 type
     */
    fun addGroupOpenList(contactViewHolderType: ContactViewHolderType) {
        groupOpenList.add(contactViewHolderType)
    }

    /**
     * 移除已經展開的群組
     * @param contactViewHolderType 群組的 type
     */
    fun removeGroupOpenList(contactViewHolderType: ContactViewHolderType) {
        val iterator = groupOpenList.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (item == contactViewHolderType) iterator.remove()
        }
    }

    /**
     * 取得客戶的聊天室 (本地)
     * */
    fun getCustomerRoom(customerId: String) =
        viewModelScope.launch {
            checkTokenValid(contactRepository.getCustomerRoom(customerId))?.collect {
                when (it) {
                    is ApiResult.Failure -> {
                        CELog.e(it.errorMessage.errorMessage)
                    }

                    is ApiResult.Success -> {
                        it.data?.let {
                            chatRoomData.postValue(it)
                        } ?: run {
                            getServiceNumberRoomItem(customerId)
                        }
                    }

                    else -> {}
                }
            }
        }

    /**
     * 取得客戶的聊天室 (API)
     * */
    fun getServiceNumberRoomItem(customerId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            checkTokenValid(contactRepository.getServiceNumberRoom(customerId, application))?.collect {
                when (it) {
                    is ApiResult.Failure -> {
                        CELog.e(it.errorMessage.errorMessage)
                    }

                    is ApiResult.Success -> {
                        roomId.postValue(it.data.id)
                    }

                    else -> {
                    }
                }
            }
        }

    fun getServiceNumberEntity(serviceNumberId: String) =
        viewModelScope.launch {
            val entity = ServiceNumberReference.findServiceNumberById(serviceNumberId)
            serviceNumberEntity.postValue(entity)
        }

    // 如果該聊天室被滑動刪除，從聯絡人列表點擊後，需要將 DB 的 deleted 狀態改回 N
    fun setChatRoomNotDeleted(roomId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            DBManager.getInstance().setRoomNotDeleted(roomId)
        }

    fun doUpdateFriendInfo(
        userId: String,
        alias: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        contactRepository.modifyFriendInfo(userId, alias).collect {
            when (it) {
                is ApiResult.Success -> {
                    val isUpdate = DBManager.getInstance().updateFriendField(userId, DBContract.UserProfileEntry.COLUMN_ALIAS, alias)
                    MessageReference.updateSendNameBySenderId(userId, alias)
                    if (isUpdate) {
                        updatedFriendInfo.postValue(Triple(true, alias, userId))
                    }
                }

                is ApiResult.Failure -> {
                    updatedFriendInfo.postValue(Triple(false, "", ""))
                }

                else -> {}
            }
        }
    }
}
