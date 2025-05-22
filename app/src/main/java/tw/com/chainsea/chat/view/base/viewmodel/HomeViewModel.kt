package tw.com.chainsea.chat.view.base.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.json.JSONArray
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.android.common.json.JsonHelper
import tw.com.chainsea.android.common.log.CELog
import tw.com.chainsea.android.common.network.NetworkHelper
import tw.com.chainsea.ce.sdk.bean.FacebookTag
import tw.com.chainsea.ce.sdk.bean.msg.FacebookCommentStatus
import tw.com.chainsea.ce.sdk.bean.msg.FacebookPostStatus
import tw.com.chainsea.ce.sdk.bean.response.AileTokenApply
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType
import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberType
import tw.com.chainsea.ce.sdk.database.DBContract.REFRESH_TIME_SOURCE
import tw.com.chainsea.ce.sdk.database.DBContract.REFRESH_TIME_SOURCE.CHAT_ROBOT_SERVICE_LIST
import tw.com.chainsea.ce.sdk.database.DBContract.REFRESH_TIME_SOURCE.SYNC_CONTACT
import tw.com.chainsea.ce.sdk.database.DBContract.REFRESH_TIME_SOURCE.SYNC_GROUP
import tw.com.chainsea.ce.sdk.database.DBContract.REFRESH_TIME_SOURCE.SYNC_ROOM_SERVICE_NUMBER
import tw.com.chainsea.ce.sdk.database.DBContract.REFRESH_TIME_SOURCE.SYNC_ROOM_UNREAD
import tw.com.chainsea.ce.sdk.database.DBContract.REFRESH_TIME_SOURCE.SYNC_TODO
import tw.com.chainsea.ce.sdk.database.DBManager
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.database.sp.UserPref
import tw.com.chainsea.ce.sdk.event.EventMsg
import tw.com.chainsea.ce.sdk.event.MsgConstant
import tw.com.chainsea.ce.sdk.http.ce.ApiManager
import tw.com.chainsea.ce.sdk.http.ce.base.BaseViewModel
import tw.com.chainsea.ce.sdk.http.ce.base.TokenRepository
import tw.com.chainsea.ce.sdk.http.ce.request.SyncLabelRequest
import tw.com.chainsea.ce.sdk.http.ce.request.TokenApplyRequest
import tw.com.chainsea.ce.sdk.http.cp.CpApiPath
import tw.com.chainsea.ce.sdk.http.cp.base.CpNewRequestBase
import tw.com.chainsea.ce.sdk.http.cp.respone.CheckVersionResponse
import tw.com.chainsea.ce.sdk.lib.ErrCode
import tw.com.chainsea.ce.sdk.network.model.common.ApiResult
import tw.com.chainsea.ce.sdk.network.model.common.BaseRequest
import tw.com.chainsea.ce.sdk.network.model.response.DeviceRecordItem
import tw.com.chainsea.ce.sdk.network.model.response.SyncRoomNormalResponse
import tw.com.chainsea.ce.sdk.reference.BusinessReference
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference
import tw.com.chainsea.ce.sdk.reference.MessageReference
import tw.com.chainsea.ce.sdk.reference.ServiceNumberReference
import tw.com.chainsea.ce.sdk.reference.TodoReference
import tw.com.chainsea.ce.sdk.reference.UserProfileReference
import tw.com.chainsea.ce.sdk.service.type.ChatRoomSource
import tw.com.chainsea.ce.sdk.service.type.RefreshSource
import tw.com.chainsea.ce.sdk.socket.ce.bean.UpdateCustomerNameSocket
import tw.com.chainsea.ce.sdk.socket.cp.CpSocket
import tw.com.chainsea.chat.App
import tw.com.chainsea.chat.BuildConfig
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.network.apicount.ApiDataCountRepository
import tw.com.chainsea.chat.network.contact.ContactRepository
import tw.com.chainsea.chat.network.logindevices.LoginDevicesRepository
import tw.com.chainsea.chat.network.selfprofile.SelfProfileRepository
import tw.com.chainsea.chat.network.todo.TodoRepository
import tw.com.chainsea.chat.network.version.VersionRepository
import tw.com.chainsea.chat.refactor.ServerEnvironment
import tw.com.chainsea.chat.service.fcm.FCMTokenManager
import tw.com.chainsea.chat.util.SystemKit
import tw.com.chainsea.chat.view.base.BottomTab
import tw.com.chainsea.chat.view.base.BottomTabEnum
import tw.com.chainsea.chat.view.base.PreloadStepEnum
import tw.com.chainsea.chat.view.chat.ChatRepository
import tw.com.chainsea.chat.view.service.ServiceNumberAgentsManageRepository
import java.util.concurrent.Semaphore

class HomeViewModel(
    private val application: Application,
    private val contactRepository: ContactRepository,
    private val selfProfileRepository: SelfProfileRepository,
    private val chatRepository: ChatRepository,
    private val loginDevicesRepository: LoginDevicesRepository,
    private val versionRepository: VersionRepository,
    private val tokenRepository: TokenRepository,
    private val apiDataCountRepository: ApiDataCountRepository,
    private val todoRepository: TodoRepository,
    private val serviceNumberAgentsManageRepository: ServiceNumberAgentsManageRepository
) : BaseViewModel(application, tokenRepository) {
    var isFirstLoading = false

    val contactAccountSize = MutableLiveData(0)

    val chatRoomUnreadNumber = MutableLiveData(0) // 當下團隊一般列表的總未讀數

    val serviceRoomUnreadNumber = MutableLiveData(0) // 當下團隊服務號列表的總未讀數

    val unreadCount = MutableLiveData(0) // 所有團隊的總未讀數

    // 是否顯示下方服務號 tab (true: 顯示, false：不顯示)
    val shouldShowServiceNumberTab = MutableLiveData<Boolean>()

    val bottomTabData = MutableLiveData<List<BottomTab>>()

    val sendRefreshDB = MutableLiveData<Boolean>()
    val sendRefreshListByAPI = MutableLiveData<Boolean>()
    val loadingStatus = MutableStateFlow(LoadingType.Undef)
    val sendGoHomePage = MutableSharedFlow<Boolean>()
    val isBindAile = MutableLiveData<Pair<String, Boolean>>()
    val sendGoLoginPage = MutableSharedFlow<String?>()
    val sendLoginDevicesList = MutableLiveData<MutableList<DeviceRecordItem>>()
    private val _timer = MutableLiveData<Int>()
    val timer: LiveData<Int> get() = _timer
    val sendToast = MutableLiveData<Int>()

    // 版本小於 3.2.0 需要洗檔使用，否則就版本升上來 多人聊天室頭圖會有問題
    val isNeedProLoad = MutableLiveData<Boolean>()

    val showUpdateDialog = MutableLiveData<CheckVersionResponse>()
    val sendCloseDrawer = MutableSharedFlow<Unit>() // close drawer

    // 火箭頁用
    // api 的進行階段
    val preloadApi = MutableLiveData<String>()

    // api 資料總數
    val preloadTotalCount = MutableLiveData<Int>()
    private var _preloadTotalCount = 0

    // 火箭頁步驟邏輯
    val preloadApiStep = MutableLiveData<Int>()

    // 火箭頁完成的資料數
    val preloadCount = MutableLiveData<Int>()
    private var _preloadCount = 0
    private var preloadRetryCount = 5
    private var stepMethodName = ""

    // 如果 token 不過 要停止 preload
    val preloadJob by lazy {
        preLoad()
    }

    // 火箭頁失敗
    private var isPreloadFailed = false
    private var hasNextPage = false
    var preloadStepEnum = PreloadStepEnum.of(TokenPref.getInstance(application).preloadStep)
    val sendMainChatRoomListRefresh = MutableSharedFlow<Boolean>()
    val sendMainChatRoomListRefreshByDB = MutableSharedFlow<Unit>()
    val sendShowPageRefreshByDbDone = MutableLiveData(PageReadyType.Undef) // 是否UI渲染完成
    val doSyncDataByRocket = MutableLiveData(false) // 判斷是否由火箭頁進行同步 或 從背景進入同步
    private val selfUserId by lazy {
        TokenPref.getInstance(application).userId
    }

    val onCustomerGot = MutableLiveData<Int>()
    val todoExpiredCount = MutableLiveData<Int>()
    val roomListRecyclerViewPool by lazy { RecyclerView.RecycledViewPool() }
    var isBackgroundSyncing = false

    enum class LoadingType {
        Undef,
        FirstLoading,
        OtherLoading
    }

    enum class PageReadyType {
        Undef,
        MainChatPage,
        ServiceNumberPage
    }

    val sendRefreshBossServiceNumberOwnerChanged = MutableLiveData<Boolean>()

    // 聊天室頁面未讀數
    fun getChatRoomListUnReadSum() =
        viewModelScope.launch(Dispatchers.IO) {
            chatRoomUnreadNumber.postValue(DBManager.getInstance().getChatRoomListUnReadSum("MAIN"))
        }

    fun getServiceRoomUnReadSum() =
        viewModelScope.launch(Dispatchers.IO) {
            serviceRoomUnreadNumber.postValue(
                DBManager.getInstance().getChatRoomListUnReadSum("SERVICE")
            )
        }

    fun getBottomTab(shouldShowServiceNumber: Boolean) =
        viewModelScope.launch(Dispatchers.IO) {
            val tabData =
                if (shouldShowServiceNumber) {
                    mutableListOf(
                        BottomTab(
                            getApplication<App>().getString(R.string.toolbar_chat_title),
                            R.drawable.icon_message,
                            BottomTabEnum.MAIN,
                            true
                        ),
                        BottomTab(
                            getApplication<App>().getString(R.string.toolbar_service_number_title),
                            R.drawable.icon_service,
                            BottomTabEnum.SERVICE
                        ),
                        BottomTab(
                            getApplication<App>().getString(R.string.toolbar_contact_title),
                            R.drawable.icon_contact,
                            BottomTabEnum.CONTACT
                        ),
                        BottomTab(
                            getApplication<App>().getString(R.string.toolbar_todo_title),
                            R.drawable.icon_todo,
                            BottomTabEnum.TODO
                        )
                    )
                } else {
                    mutableListOf(
                        BottomTab(
                            getApplication<App>().getString(R.string.toolbar_chat_title),
                            R.drawable.icon_message,
                            BottomTabEnum.MAIN,
                            true
                        ),
                        BottomTab(
                            getApplication<App>().getString(R.string.toolbar_contact_title),
                            R.drawable.icon_contact,
                            BottomTabEnum.CONTACT
                        ),
                        BottomTab(
                            getApplication<App>().getString(R.string.toolbar_todo_title),
                            R.drawable.icon_todo,
                            BottomTabEnum.TODO
                        )
                    )
                }
            bottomTabData.postValue(tabData)
        }

    // 取得聯絡人數量
    fun getContactAccountSize() =
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val selfId = TokenPref.getInstance(getApplication()).userId
                val size = DBManager.queryUsersWithSelfTableSize(selfId)
                contactAccountSize.postValue(size)
            } catch (e: Exception) {
                Log.e("ContactViewModel", "Error getting contact size: ${e.message}")
                contactAccountSize.postValue(0)
            }
        }

    // 判斷該使用者是否為某個服務號的服務成員, 如果都沒有要隱藏服務號tab
    fun getShouldShowServiceNumberTab() =
        viewModelScope.launch(Dispatchers.IO) {
            val serviceNumberList = ServiceNumberReference.findSelfServiceNumber(selfUserId)

            val isServiceNumberMember =
                serviceNumberList.any { serviceNumber ->
                    serviceNumber.memberItems.any { it.id == selfUserId }
                }
            shouldShowServiceNumberTab.postValue(isServiceNumberMember)
        }

    fun disableServiceNumber(broadcastRoomId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            DBManager.getInstance().updateServiceStatus(broadcastRoomId, false)
        }

    /**
     * 取得標籤
     * */
    private fun syncLabels(): Deferred<Unit> =
        viewModelScope.async(Dispatchers.IO) {
            val refreshTime =
                DBManager.getInstance().getLastRefreshTime(REFRESH_TIME_SOURCE.SYNC_LABEL)
            contactRepository
                .getLabels(
                    BaseRequest(SyncLabelRequest.PAGE_SIZE, refreshTime),
                    RefreshSource.REMOTE
                ).collect {
                    when (it) {
                        is ApiResult.SaveStatus -> {
                            preloadCount.postValue(++_preloadCount)
                        }

                        is ApiResult.Failure -> {
                            if (isCanRetry("syncLabels")) {
                                syncLabels().await()
                            } else {
                                isPreloadFailed = true
                                CELog.e("syncLabels Failure", it.errorMessage.errorMessage)
                                preloadCount.postValue(++_preloadCount)
                            }
                        }

                        is ApiResult.NextPage -> {
                            if (it.hasNextPage) syncLabels().await()
                        }

                        else -> {}
                    }
                }
        }

    // 取得記事列表
    private fun syncTodo(): Deferred<Unit> =
        viewModelScope.async(Dispatchers.IO) {
            val refreshTime = DBManager.getInstance().getLastRefreshTime(SYNC_TODO)
            todoRepository.syncTodo(refreshTime).collect {
                when (it) {
                    is ApiResult.Failure -> {
                        if (isCanRetry("syncTodo")) {
                            syncTodo().await()
                        } else {
                            preloadCount.postValue(++_preloadCount)
                            CELog.e("syncTodo Failure", it.errorMessage.errorMessage)
                            isPreloadFailed = true
                        }
                    }

                    is ApiResult.SaveStatus -> {
                        preloadCount.postValue(++_preloadCount)
                    }

                    is ApiResult.NextPage -> {
                        if (it.hasNextPage) syncTodo().await()
                    }

                    else -> {}
                }
            }
        }

    // 取得訂閱服務號列表
    private fun syncSubscribeNumber(): Deferred<Unit> =
        viewModelScope.async(Dispatchers.IO) {
            contactRepository.getSubscribeServiceNumberList().collect {
                when (it) {
                    is ApiResult.Failure -> {
                        if (isCanRetry("syncSubscribeNumber")) {
                            syncSubscribeNumber().await()
                        } else {
                            isPreloadFailed = true
                            CELog.e("syncSubscribeNumber Failure", it.errorMessage.errorMessage)
                            preloadCount.postValue(++_preloadCount)
                        }
                    }

                    is ApiResult.SaveStatus -> {
                        preloadCount.postValue(++_preloadCount)
                    }

                    is ApiResult.NextPage -> {
                        if (it.hasNextPage) syncSubscribeNumber().await()
                    }

                    else -> {}
                }
            }
        }

    private fun syncServiceNumber(): Deferred<Unit> =
        viewModelScope.async(Dispatchers.IO) {
            val refreshTime = DBManager.getInstance().getLastRefreshTime(REFRESH_TIME_SOURCE.SYNC_SERVICENUMBER)
            contactRepository.syncServiceNumber(refreshTime).collect {
                when (it) {
                    is ApiResult.Failure -> {
                        if (isCanRetry("syncServiceNumber")) {
                            syncServiceNumber().await()
                        } else {
                            isPreloadFailed = true
                            CELog.e("syncServiceNumber Failure", it.errorMessage.errorMessage)
                            preloadCount.postValue(++_preloadCount)
                        }
                    }

                    is ApiResult.SaveStatus -> {
                        preloadCount.postValue(++_preloadCount)
                    }

                    is ApiResult.NextPage -> {
                        if (it.hasNextPage) syncServiceNumber().await()
                    }

                    else -> {}
                }
            }
        }

    // 取得夥伴列表
    private fun syncEmployee(): Deferred<Unit> =
        viewModelScope.async(Dispatchers.IO) {
            val refreshTime = DBManager.getInstance().getLastRefreshTime(REFRESH_TIME_SOURCE.SYNC_EMPLOYEE)
            contactRepository.getEmployeeList(refreshTime, selfUserId, RefreshSource.REMOTE).collect {
                when (it) {
                    is ApiResult.Failure -> {
                        if (isCanRetry("syncEmployee")) {
                            syncEmployee().await()
                        } else {
                            isPreloadFailed = true
                            CELog.e("syncEmployee Failure", it.errorMessage.errorMessage)
                            preloadCount.postValue(++_preloadCount)
                        }
                    }

                    is ApiResult.SaveStatus -> {
                        preloadCount.postValue(++_preloadCount)
                    }

                    is ApiResult.NextPage -> {
                        if (it.hasNextPage) syncEmployee().await()
                    }

                    else -> {}
                }
            }
        }

    // 取得所有客戶列表
    private fun syncContact(): Deferred<Unit> =
        viewModelScope.async(Dispatchers.IO) {
            contactRepository.syncAllContactList().collect {
                when (it) {
                    is ApiResult.Failure -> {
                        if (isCanRetry("syncContact")) {
                            syncContact().await()
                        } else {
                            isPreloadFailed = true
                            CELog.e("syncContact Failure", it.errorMessage.errorMessage)
                            preloadCount.postValue(++_preloadCount)
                        }
                    }

                    is ApiResult.SaveStatus -> {
                        preloadCount.postValue(++_preloadCount)
                    }

                    is ApiResult.NextPage -> {
                        if (it.hasNextPage) syncContact().await()
                    }

                    else -> {}
                }
            }
        }

    // 取得聊天室列表
    private fun syncRoomNormal(): Deferred<Unit> =
        viewModelScope.async(Dispatchers.IO) {
            val refreshTime = DBManager.getInstance().getLastRefreshTime(REFRESH_TIME_SOURCE.SYNC_ROOM_NORMAL)
            chatRepository.syncRoomNormal(refreshTime).collect {
                when (it) {
                    is ApiResult.NextPage -> {
                        hasNextPage = it.hasNextPage
                    }

                    is ApiResult.Failure -> {
                        if (isCanRetry("syncRoomNormal")) {
                            syncRoomNormal().await()
                        } else {
                            isPreloadFailed = true
                            CELog.e("syncRoomNormal Failure", it.errorMessage.errorMessage)
                            preloadCount.postValue(++_preloadCount)
                        }
                    }

                    is ApiResult.Success -> {
                        it.data?.let { response ->
                            val data = response as List<SyncRoomNormalResponse>

                            val discussRoomList = data.filter { it.type == ChatRoomType.discuss }
//                        val groupRoomList = data.filter { it.type == ChatRoomType.group }
//                        val friendsRoomList = data.filter { it.type == ChatRoomType.friend }
                            val normalRoom = data.filter { it.type != ChatRoomType.discuss && it.type != ChatRoomType.group && it.type != ChatRoomType.friend }

                            if (discussRoomList.isNotEmpty()) {
                                discussRoomList.forEach { resp ->
                                    if (!resp.member_deleted && !resp.deleted) {
                                        chatRepository
                                            .getDiscussRoomChatMember(
                                                resp.id,
                                                ChatRoomType.discuss,
                                                resp.isCustomName ?: false
                                            ).collect { result ->
                                                when (result) {
                                                    is ApiResult.Failure,
                                                    is ApiResult.Success -> {
                                                        preloadCount.postValue(++_preloadCount)
                                                    }

                                                    else -> {}
                                                }
                                            }
                                    }
                                }
                            }

                            if (hasNextPage) {
                                syncRoomNormal().await()
                            }
                            if (normalRoom.isNotEmpty()) {
                                normalRoom.forEach { _ ->
                                    preloadCount.postValue(++_preloadCount)
                                }
                            }
                        }
                    }

                    else -> {}
                }
            }
        }

    private fun syncUnreadRoom(): Deferred<Unit> =
        viewModelScope.async(Dispatchers.IO) {
            val refreshTime = DBManager.getInstance().getLastRefreshTime(SYNC_ROOM_UNREAD)
            chatRepository.getRoomUnReadList(selfUserId, refreshTime).collect {
                when (it) {
                    is ApiResult.Failure -> {
                        if (isCanRetry("syncUnreadRoom")) {
                            syncUnreadRoom().await()
                        } else {
                            isPreloadFailed = true
                            CELog.e("syncUnreadRoom Failure", it.errorMessage.errorMessage)
                            preloadCount.postValue(++_preloadCount)
                        }
                    }

                    is ApiResult.SaveStatus -> {
                        preloadCount.postValue(++_preloadCount)
                    }

                    is ApiResult.NextPage -> {
                        if (it.hasNextPage) {
                            syncUnreadRoom().await()
                        } else {
                            // 打完syncRoomNormal()和syncUnreadRoom()後直接先更新一般聊天列表
                            sendMainChatRoomListRefresh.emit(true)
                        }
                    }

                    else -> {}
                }
            }
        }

    // 取得"進線中"和"機器人服務中"的聊天室紀錄
    private fun syncServiceNumberActiveList(): Deferred<Unit> =
        viewModelScope.async(Dispatchers.IO) {
            val refreshTime =
                DBManager
                    .getInstance()
                    .getLastRefreshTime(REFRESH_TIME_SOURCE.SYNC_SERVICE_NUMBER_ACTIVE_LIST)
            val list = DBManager.getInstance().queryAllServiceNumberId()
            chatRepository.syncServiceNumberActiveList(list, refreshTime, selfUserId).collect {
                when (it) {
                    is ApiResult.Failure -> {
                        if (isCanRetry("syncServiceNumberActiveList")) {
                            syncServiceNumberActiveList().await()
                        } else {
                            isPreloadFailed = true
                            CELog.e("syncServiceNumberActiveList Failure", it.errorMessage.errorMessage)
                            preloadCount.postValue(++_preloadCount)
                        }
                    }

                    is ApiResult.SaveStatus -> {
                        preloadCount.postValue(++_preloadCount)
                    }

                    is ApiResult.NextPage -> {
                        if (it.hasNextPage) syncServiceNumberActiveList().await()
                    }

                    else -> {}
                }
            }
        }

    // 取得機器人服務中的列表
    private fun syncRobotServiceRoom(): Deferred<Unit> =
        viewModelScope.async(Dispatchers.IO) {
            val refreshTime = DBManager.getInstance().getLastRefreshTime(CHAT_ROBOT_SERVICE_LIST)
            chatRepository.getRobotServiceList(refreshTime).collect {
                when (it) {
                    is ApiResult.Failure -> {
                        if (isCanRetry("syncRobotServiceRoom")) {
                            syncRobotServiceRoom().await()
                        } else {
                            isPreloadFailed = true
                            CELog.e("syncRobotServiceRoom Failure", it.errorMessage.errorMessage)
                            preloadCount.postValue(++_preloadCount)
                        }
                    }

                    is ApiResult.SaveStatus -> {
                        preloadCount.postValue(++_preloadCount)
                    }

                    is ApiResult.NextPage -> {
                        if (it.hasNextPage) syncRobotServiceRoom().await()
                    }

                    else -> {}
                }
            }
        }

    private fun syncRoomServiceNumber(): Deferred<Unit> =
        viewModelScope.async(Dispatchers.IO) {
            val refreshTime = DBManager.getInstance().getLastRefreshTime(SYNC_ROOM_SERVICE_NUMBER)
            chatRepository.syncRoomServiceNumber(refreshTime, selfUserId).collect {
                when (it) {
                    is ApiResult.Failure -> {
                        if (isCanRetry("syncRoomServiceNumber")) {
                            syncRoomServiceNumber().await()
                        } else {
                            isPreloadFailed = true
                            CELog.e("syncRoomServiceNumber Failure", it.errorMessage.errorMessage)
                            preloadCount.postValue(++_preloadCount)
                        }
                    }

                    is ApiResult.SaveStatus -> {
                        preloadCount.postValue(++_preloadCount)
                    }

                    is ApiResult.NextPage -> {
                        if (it.hasNextPage) syncRoomServiceNumber().await()
                    }

                    else -> {}
                }
            }
        }

    private fun syncGroup(): Deferred<Unit> =
        viewModelScope.async(Dispatchers.IO) {
            val refreshTime = DBManager.getInstance().getLastRefreshTime(SYNC_GROUP)
            chatRepository.getAllGroupList(refreshTime).collect {
                when (it) {
                    is ApiResult.Failure -> {
                        if (isCanRetry("syncGroup")) {
                            syncGroup().await()
                        } else {
                            isPreloadFailed = true
                            CELog.e("syncGroup Failure", it.errorMessage.errorMessage)
                        }
                    }

                    is ApiResult.NextPage -> {
                        if (it.hasNextPage) syncGroup().await()
                    }

                    else -> {}
                }
            }
        }

    /**
     * 火箭頁預載邏輯
     * 同步內容主體標籤(含我的最愛)
     * 同步內容主體記事
     * 同步內容主體服務號
     * 同步內容主體人
     * 同步內容主體聊天室
     **/
    fun preLoad() =
        viewModelScope.launch(Dispatchers.IO) {
            when (preloadStepEnum) {
                PreloadStepEnum.SYNC_LABEL -> {
//                DBManager.getInstance().clearTableData()
                    // 同步內容主體標籤
                    val refreshTime = DBManager.getInstance().getLastRefreshTime(REFRESH_TIME_SOURCE.SYNC_LABEL)
                    getApiDataCount("Label", refreshTime).await()
                    stepMethodName = "syncLabels"
                    syncLabels().await()
                    setPreloadNextStep()
                }

                PreloadStepEnum.SYNC_TODO -> {
                    // 同步內容主體記事
                    _preloadTotalCount = 0
                    val refreshTime = DBManager.getInstance().getLastRefreshTime(SYNC_TODO)
                    getApiDataCount("Todo", refreshTime).await()
                    stepMethodName = "syncTodo"
                    syncTodo().await()
                    setPreloadNextStep()
                }

                PreloadStepEnum.SYNC_CONTACT_PERSON -> {
                    // 同步內容主體人
                    _preloadTotalCount = 0
                    val employeeRefreshTime = DBManager.getInstance().getLastRefreshTime(REFRESH_TIME_SOURCE.SYNC_EMPLOYEE)
                    val contactRefreshTime = DBManager.getInstance().getLastRefreshTime(SYNC_CONTACT)
                    getApiDataCount("Employee", employeeRefreshTime).await()
                    getApiDataCount("Contact", contactRefreshTime).await()
                    stepMethodName = "syncEmployee"
                    syncEmployee().await()
                    stepMethodName = "syncContact"
                    syncContact().await()
                    setPreloadNextStep()
                }

                PreloadStepEnum.SYNC_SERVICE_NUMBER -> {
                    // 同步內容主體服務號
                    _preloadTotalCount = 0
                    getApiDataCount("SubscribeServiceNumber", 0L).await()
                    val refreshTime = DBManager.getInstance().getLastRefreshTime(REFRESH_TIME_SOURCE.SYNC_SERVICENUMBER)
                    getApiDataCount("ServiceNumber", refreshTime).await()
                    stepMethodName = "syncSubscribeNumber"
                    syncSubscribeNumber().await()
                    stepMethodName = "syncServiceNumber"
                    syncServiceNumber().await()
                    setPreloadNextStep()
                }

                PreloadStepEnum.SYNC_CHAT_ROOM -> {
                    // 同步內容主體聊天室
                    _preloadTotalCount = 0
                    val normalRoomRefreshTime = DBManager.getInstance().getLastRefreshTime(REFRESH_TIME_SOURCE.SYNC_ROOM_NORMAL)
                    getApiDataCount("NormalRoom", normalRoomRefreshTime).await()
                    val unReadRoomRefreshTime = DBManager.getInstance().getLastRefreshTime(SYNC_ROOM_UNREAD)
                    getApiDataCount("UnReadRoom", unReadRoomRefreshTime).await()
                    val activeRoomRefreshTime = DBManager.getInstance().getLastRefreshTime(REFRESH_TIME_SOURCE.SYNC_SERVICE_NUMBER_ACTIVE_LIST)
                    getApiDataCount("ActiveRoom", activeRoomRefreshTime).await()
                    val robotActiveRoomRefreshTime = DBManager.getInstance().getLastRefreshTime(CHAT_ROBOT_SERVICE_LIST)
                    getApiDataCount("RobotActiveRoom", robotActiveRoomRefreshTime).await()
                    val serviceRoomRefreshTime = DBManager.getInstance().getLastRefreshTime(SYNC_ROOM_SERVICE_NUMBER)
                    getApiDataCount("ServiceRoom", serviceRoomRefreshTime).await()
                    stepMethodName = "syncRoomNormal"
                    syncRoomNormal().await()
                    stepMethodName = "syncUnreadRoom"
                    syncUnreadRoom().await()
                    stepMethodName = "syncGroup"
                    syncGroup().await()
                    stepMethodName = "syncServiceNumberActiveList"
                    syncServiceNumberActiveList().await()
                    stepMethodName = "syncRobotServiceRoom"
                    syncRobotServiceRoom().await()
                    stepMethodName = "syncRoomServiceNumber"
                    syncRoomServiceNumber().await()
                    setPreloadNextStep()
                }

                PreloadStepEnum.SYNC_DONE -> {
                    stepMethodName = ""
                    TokenPref.getInstance(application).preloadStep = PreloadStepEnum.SYNC_DONE.ordinal
                    if (!isPreloadFailed) {
                        // 已經 preload 完的 儲存成 map<tenantId, false>
                        // 以確保使用者登出，下次登入的時候 切換團隊可以再次進入 preload
//                    val map = TokenPref.getInstance(application).tenantList
//                    val currentTenant = TokenPref.getInstance(application).cpCurrentTenant
//                    map[currentTenant.tenantId] = false
//                    TokenPref.getInstance(application).tenantList = map
                        DBManager.getInstance().updateOrInsertApiInfoField(
                            REFRESH_TIME_SOURCE.FIRST_LOAD,
                            System.currentTimeMillis()
                        )
                        DBManager
                            .getInstance()
                            .updateOrInsertApiInfoField(ChatRoomSource.SERVICE.name, System.currentTimeMillis())
                    }
                    loadingStatus.value = LoadingType.OtherLoading
//                sendRefreshDB.postValue(true)
                    sendShowPageRefreshByDbDone.postValue(PageReadyType.MainChatPage)
                }
            }
        }

    private fun setPreloadNextStep() {
        preloadStepEnum =
            when (preloadStepEnum) {
                PreloadStepEnum.SYNC_LABEL -> {
                    PreloadStepEnum.SYNC_TODO
                }

                PreloadStepEnum.SYNC_TODO -> {
                    PreloadStepEnum.SYNC_CONTACT_PERSON
                }

                PreloadStepEnum.SYNC_CONTACT_PERSON -> {
                    PreloadStepEnum.SYNC_SERVICE_NUMBER
                }

                PreloadStepEnum.SYNC_SERVICE_NUMBER -> {
                    PreloadStepEnum.SYNC_CHAT_ROOM
                }

                PreloadStepEnum.SYNC_CHAT_ROOM -> {
                    PreloadStepEnum.SYNC_DONE
                }

                else -> {
                    PreloadStepEnum.SYNC_DONE
                }
            }
        preLoad()
    }

    private fun getApiDataCount(
        queryType: String,
        refreshTime: Long
    ): Deferred<Unit> =
        viewModelScope.async {
            checkTokenValid(apiDataCountRepository.getApiDataCount(queryType, refreshTime))?.collect {
                when (it) {
                    is ApiResult.Failure -> {
                        // 失敗給預設值
                        preloadTotalCount.postValue(100)
                    }

                    is ApiResult.Success -> {
                        preloadApiStep.postValue(preloadStepEnum.type)
                        _preloadCount = 0
                        _preloadTotalCount += it.data
                        when (queryType) {
                            "Label" -> {
                                preloadApi.postValue("我的最愛")
                            }

                            "Todo" -> {
                                preloadApi.postValue("記事")
                            }

                            "SubscribeServiceNumber", "ServiceNumber" -> {
                                preloadApi.postValue("服務號")
                            }

                            "Employee", "Contact" -> {
                                preloadApi.postValue("聯絡人")
                            }

                            "NormalRoom", "UnReadRoom", "ActiveRoom", "RobotActiveRoom", "ServiceRoom" -> {
                                preloadApi.postValue("聊天室")
                            }
                        }
                        preloadTotalCount.postValue(_preloadTotalCount)
                        preloadCount.postValue(_preloadCount)
                    }

                    else -> {}
                }
            }
        }

    private fun isCanRetry(stepMethodName: String): Boolean {
        if (stepMethodName != this.stepMethodName) preloadRetryCount = 5
        preloadRetryCount--
        return preloadRetryCount > 0
    }

    fun savePreloadStep() {
        if (preloadStepEnum != PreloadStepEnum.SYNC_DONE) {
            TokenPref.getInstance(application).preloadStep = preloadStepEnum.ordinal
        }
    }

    fun backgroundSync() =
        viewModelScope.launch(Dispatchers.IO) {
            if (!NetworkHelper.hasNetWork(application)) return@launch
            if (isBackgroundSyncing) return@launch
            isBackgroundSyncing = true
            val semaphore = Semaphore(5)
            val syncFunctions =
                listOf(
                    ::syncLabels,
                    ::syncTodo,
                    ::syncEmployee,
                    ::syncContact,
                    ::syncSubscribeNumber,
                    ::syncServiceNumber,
                    ::syncRoomNormal,
                    ::syncUnreadRoom,
                    ::syncGroup,
                    ::syncServiceNumberActiveList,
                    ::syncRobotServiceRoom,
                    ::syncRoomServiceNumber
                )

            val tasks =
                syncFunctions.map { syncFunc ->
                    async {
                        semaphore.acquire()
                        try {
                            syncFunc().await()
                        } finally {
                            semaphore.release()
                        }
                    }
                }

            try {
                // 等待所有任務完成
                tasks.awaitAll()
                DBManager.getInstance().updateOrInsertApiInfoField(REFRESH_TIME_SOURCE.FIRST_LOAD, System.currentTimeMillis())
            } catch (e: Exception) {
                CELog.e("background error", e)
            } finally {
                loadingStatus.value = LoadingType.OtherLoading
                isBackgroundSyncing = false
                sendMainChatRoomListRefresh.emit(false)
            }
        }

    fun setServiceUrl() =
        viewModelScope.launch(Dispatchers.IO) {
            when (ServerEnvironment.parse(TokenPref.getInstance(application).currentServer)) {
                ServerEnvironment.FORMAL -> {
                    CpNewRequestBase.BASE_URL = CpApiPath.FORMAL_SERVER
                    CpSocket.BASE_URL = CpSocket.FORMAL_SERVER
                }

                ServerEnvironment.UAT -> {
                    CpNewRequestBase.BASE_URL = CpApiPath.UAT_SERVER
                    CpSocket.BASE_URL = CpSocket.UAT_SERVER_SOCKET
                }

                ServerEnvironment.QA -> {
                    CpNewRequestBase.BASE_URL = CpApiPath.QA_SERVER
                    CpSocket.BASE_URL = CpSocket.QA_SERVER_SOCKET
                }

                ServerEnvironment.DEV -> {
                    CpNewRequestBase.BASE_URL = CpApiPath.DEV_SERVER
                    CpSocket.BASE_URL = CpSocket.DEV_SERVER_SOCKET
                }

                ServerEnvironment.SELF_DEFINE -> {
                    val customDomain = TokenPref.getInstance(application).selfDefineEnvironment
                    CpNewRequestBase.BASE_URL = customDomain.plus("cp/openapi/")
                    CpSocket.BASE_URL = customDomain.plus("cp")
                }
            }
        }

    // cp login
    fun autoLogin() =
        viewModelScope.launch {
            doTokenApply()
        }

    private fun doTokenApply() =
        viewModelScope.launch(Dispatchers.IO) {
            ApiManager.doTokenApply(
                application,
                false,
                object : TokenApplyRequest.Listener {
                    override fun allCallBack(
                        isRefresh: Boolean,
                        status: Boolean
                    ) {
                        if (isRefresh && status) {
                            FCMTokenManager.refreshFCMTokenIdToRemote(application)
                        }
                    }

                    override fun onSuccess(
                        isRefresh: Boolean,
                        resp: AileTokenApply.Resp
                    ) {
                        if (resp.user != null) CELog.startLogSave(application, resp.user.id)
                        SystemKit.saveCEInfo(resp)
                        SystemKit.syncApiData()
                        viewModelScope.launch {
                            if (resp.user.isBindAile) {
                                isBindAile.postValue(Pair(resp.user.bindUrl, resp.user.isCollectInfo))
                            }
                            sendGoHomePage.emit(true)
                        }
                    }

                    override fun onFailed(
                        errorCode: ErrCode?,
                        errorMessage: String?
                    ) {
                        viewModelScope.launch {
                            sendGoLoginPage.emit(application.getString(R.string.invalid_token))
                        }
                    }

                    override fun onCallData(
                        roomId: String?,
                        meetingId: String?,
                        callKey: String?
                    ) {
                        viewModelScope.launch {
//                    sendGoHomePage.emit(Triple(true, "", true))
                        }
                    }
                }
            )
        }

    /**
     * 取得登入設備列表
     **/
    fun getLoginDevicesList() =
        viewModelScope.launch(Dispatchers.IO) {
            checkTokenValid(loginDevicesRepository.getLoginDevicesList())?.collect {
                when (it) {
                    is ApiResult.Success -> {
                        it.data.let { items ->
                            val filterItems =
                                items
                                    .filter { item ->
                                        (item.isOnline == true && item.uniqueID == TokenPref.getInstance(application).uniqueID && item.bundleId == "tw.com.chainsea.chat") || (item.osType == "pc")
                                    }.distinctBy { item ->
                                        if (item.osType == "pc") item.uniqueID else item
                                    }

                            sendLoginDevicesList.postValue(filterItems.toMutableList())
                        }
                    }

                    is ApiResult.Failure -> {
                        Log.e("HomeViewModel", "getLoginDevicesList failure = ${it.errorMessage}")
                    }

                    else -> {}
                }
            }
        }

    /**
     * 強制登出其他設備
     **/
    fun doForceLogoutDevice(deviceId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            loginDevicesRepository.doForceLogoutDevice(deviceId).collect {
                when (it) {
                    is ApiResult.Success -> {
                        if (it.data) {
                            Log.w("HomeViewModel", "Force logout device $deviceId success!")
                            sendToast.postValue(R.string.text_toast_device_logout)
                            getLoginDevicesList()
                        } else {
                            Log.e("HomeViewModel", "Force logout device $deviceId Failure!")
                            sendToast.postValue(R.string.text_toast_operator_failure)
                        }
                    }

                    is ApiResult.Failure -> {
                        sendToast.postValue(R.string.text_toast_operator_failure)
                    }

                    else -> {}
                }
            }
        }

    /**
     * 取消自動登入設定
     **/
    fun doCancelAutoLogin(id: String) =
        viewModelScope.launch(Dispatchers.IO) {
            checkTokenValid(loginDevicesRepository.doCancelAutoLogin(id))?.collect {
                when (it) {
                    is ApiResult.Success -> {
                        if (it.data) {
                            Log.w("HomeViewModel", "Cancel device auto login $id success!")
                            sendToast.postValue(R.string.text_toast_device_auto_login_canceled)
                            getLoginDevicesList()
                        } else {
                            Log.e("HomeViewModel", "Cancel device auto login $id Failure!")
                            sendToast.postValue(R.string.text_toast_device_operator_failure)
                        }
                    }

                    is ApiResult.Failure -> {
                        sendToast.postValue(R.string.text_toast_device_operator_failure)
                    }

                    else -> {}
                }
            }
        }

    /**
     * 新增自動登入設定
     **/
    fun doAllowAutoLogin(id: String) =
        viewModelScope.launch(Dispatchers.IO) {
            checkTokenValid(loginDevicesRepository.doAllowAutoLogin(id))?.collect {
                when (it) {
                    is ApiResult.Success -> {
                        if (it.data) {
                            Log.w("HomeViewModel", "Allow device auto login $id success!")
                            sendToast.postValue(R.string.text_toast_allowed_device_auto_login)
                            getLoginDevicesList()
                        } else {
                            Log.e("HomeViewModel", "Allow device auto login $id Failure!")
                            sendToast.postValue(R.string.text_toast_device_operator_failure)
                        }
                    }

                    is ApiResult.Failure -> {
                        sendToast.postValue(R.string.text_toast_device_operator_failure)
                    }

                    else -> {}
                }
            }
        }

    /**
     * 刪除登入設備
     **/
    fun doDeleteLoginDevice(
        uniqueId: String,
        id: String,
        deviceId: String?
    ) = viewModelScope.launch(Dispatchers.IO) {
        checkTokenValid(loginDevicesRepository.doDeleteLoginDevice(id, deviceId))?.collect {
            when (it) {
                is ApiResult.Success -> {
                    if (it.data) {
                        Log.w("HomeViewModel", "Delete login device $id success!")
                        sendToast.postValue(R.string.text_toast_device_has_been_deleted)
                        if (uniqueId != TokenPref.getInstance(application).uniqueID) {
                            // 刪除本機，不需刷新列表
                            getLoginDevicesList()
                        }
                    } else {
                        Log.e("HomeViewModel", "Delete login device $id Failure!")
                        sendToast.postValue(R.string.text_toast_operator_failure)
                    }
                }

                is ApiResult.Failure -> {
                    sendToast.postValue(R.string.text_toast_operator_failure)
                }

                else -> {}
            }
        }
    }

    fun startCountdown(seconds: Int) =
        viewModelScope.launch(Dispatchers.IO) {
            for (i in seconds downTo 0) {
                _timer.postValue(i)
                delay(1000L)
            }
        }

    fun setRead(entity: ChatRoomEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            DBManager.getInstance().setChatRoomListItemUnreadNum(entity.id, 0)
        }

    /*
     * 強制洗檔用
     */
    fun forceRefreshSqlData() =
        viewModelScope.launch(Dispatchers.IO) {
            checkTokenValid(versionRepository.getVersion())?.collect {
                when (it) {
                    is ApiResult.Failure -> {
                        isNeedProLoad.postValue(false)
                        CELog.e(it.errorMessage.errorMessage)
                    }

                    is ApiResult.Success -> {
                        val isNeedToShowPreloadPage = isNeedToShowPreloadPage()
                        if (isNeedToShowPreloadPage) {
                            preloadStepEnum = PreloadStepEnum.SYNC_LABEL
                        }
                        try {
                            val targetForceRefreshDbVersion = "3.5.2" // 需要強制洗檔的版本
                            val latestVersion = it.data.version

                            val locationVersion = TokenPref.getInstance(application).locationVersion

                            // 有新版本
                            if (BuildConfig.VERSION_CODE < latestVersion.toInt()) {
                                showUpdateDialog.postValue(it.data)
                            }

                            val needPreLoad =
                                (targetForceRefreshDbVersion == BuildConfig.VERSION_NAME && BuildConfig.VERSION_NAME != locationVersion) ||
                                    locationVersion == ""

                            if (needPreLoad) {
                                clearPreloadFlag()
                            }
                            TokenPref.getInstance(application).currentAppVersionFromServer = latestVersion.toInt()
                            TokenPref.getInstance(application).locationVersion = BuildConfig.VERSION_NAME

                            isNeedProLoad.postValue(needPreLoad || isNeedToShowPreloadPage)
                        } catch (e: Exception) {
                            isNeedProLoad.postValue(isNeedToShowPreloadPage)
                            CELog.e(e.message)
                        }
                    }

                    else -> {}
                }
            }
        }

    // 超過一天沒同步 需要顯示火箭頁
    private suspend fun isNeedToShowPreloadPage(): Boolean =
        withContext(Dispatchers.IO) {
            val lastPreloadTime = DBManager.getInstance().getLastRefreshTime(REFRESH_TIME_SOURCE.FIRST_LOAD)
            return@withContext System.currentTimeMillis() - lastPreloadTime > 86400000
        }

    private fun clearPreloadFlag() {
        DBManager.getInstance().updateOrInsertApiInfoField(ChatRoomSource.SERVICE.name, 0L)
        preloadStepEnum = PreloadStepEnum.SYNC_LABEL
//        val tenantList = TokenPref.getInstance(application).tenantList
//        tenantList.entries.forEach { entry ->
//            tenantList[entry.key] = true
//        }
//        TokenPref.getInstance(application).tenantList = tenantList
    }

    fun clearDataWhenLeaveTenant() =
        viewModelScope.launch(Dispatchers.IO) {
            val selfUserId = TokenPref.getInstance(application).userId
            DBManager.getInstance().close()
            application.deleteDatabase("$selfUserId.db")
            SystemKit.cleanCE()
        }

    fun refreshServiceNumberListByDb() =
        viewModelScope.launch(Dispatchers.IO) {
            val userId = TokenPref.getInstance(application).userId
            val list = ChatRoomReference.getInstance().findRoomByChatRoomSource(ChatRoomSource.SERVICE, userId)

            if (list.size > 0) {
                sendRefreshDB.postValue(true)
            }
        }

    fun getCustomer(position: Int) =
        viewModelScope.launch(Dispatchers.IO) {
            val bossServiceNumberId = TokenPref.getInstance(application).bossServiceNumberId
            val refreshTime =
                DBManager
                    .getInstance()
                    .getLastRefreshTime(REFRESH_TIME_SOURCE.BOSSSERVICENUMBER_CONTACT_LIST)
            checkTokenValid(contactRepository.getServiceNumberContactList(RefreshSource.REMOTE, bossServiceNumberId, BaseRequest(refreshTime)))?.collect {
                when (it) {
                    is ApiResult.Success -> {
                        onCustomerGot.postValue(position)
                    }

                    else -> {
                        // nothing
                    }
                }
            }
        }

    fun setFacebookCommentStatus(
        roomId: String,
        commentId: String,
        status: FacebookCommentStatus
    ) = viewModelScope.launch(Dispatchers.IO) {
        val currentMessage = findCurrentFacebookComment(roomId, commentId)
        currentMessage?.let {
            MessageReference.updateFacebookCommentStatus(roomId, it.id, status)
        }
    }

    fun setFacebookPostStatus(
        roomIds: JSONArray?,
        commentId: String,
        status: FacebookPostStatus
    ) = viewModelScope.launch(Dispatchers.IO) {
        roomIds?.let { roomIds ->
            for (i in 0 until roomIds.length()) {
                val currentMessageList = findCurrentFacebookPost(roomIds.optString(i), commentId)
                currentMessageList.forEach {
                    MessageReference.updateFacebookPostStatus(roomIds.optString(i), it.id, status)
                }
            }
        }
    }

    /**
     * 找尋該留言訊息
     * @param roomId 聊天室Id
     * @param commentId Facebook 留言 Id
     * */
    private suspend fun findCurrentFacebookComment(
        roomId: String,
        commentId: String
    ): MessageEntity? =
        withContext(Dispatchers.IO) {
            val messageList = MessageReference.findByRoomId(roomId)
            messageList.forEach { message ->
                message.tag?.let {
                    val facebookTag = JsonHelper.getInstance().from(it, FacebookTag::class.java)
                    if (facebookTag.data.commentId == commentId) {
                        return@withContext message
                    }
                }
            }
            return@withContext null
        }

    /**
     * 找尋該貼文所有的留言
     * @param roomId 聊天室Id
     * @param postId Facebook 貼文 Id
     * */
    private suspend fun findCurrentFacebookPost(
        roomId: String,
        postId: String
    ): List<MessageEntity> =
        withContext(Dispatchers.IO) {
            val messageList = MessageReference.findByRoomId(roomId)
            val filterList =
                messageList.filter {
                    if (it.tag != null) {
                        val facebookTag = JsonHelper.getInstance().from(it.tag, FacebookTag::class.java)
                        facebookTag.data.postId == postId
                    } else {
                        false
                    }
                }
            return@withContext filterList
        }

    fun checkIsBossServiceNumberOwnerModify(serviceNumberId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            val serviceNumber = ServiceNumberReference.findServiceNumberById(serviceNumberId)
            if (serviceNumber.serviceNumberType == ServiceNumberType.BOSS.type) {
                val list = ChatRoomReference.getInstance().findAllChatRoomsByBossServiceNumberId(selfUserId, serviceNumberId)
                serviceNumberAgentsManageRepository.getServiceNumberItem(serviceNumberId).collect {
                    when (it) {
                        is ApiResult.Success -> {
                            if (it.data.ownerId == selfUserId) {
                                TokenPref.getInstance(application).bossServiceNumberId = serviceNumberId
                                list.forEach {
                                    it.listClassify = ChatRoomSource.MAIN
                                }
                            } else {
                                TokenPref.getInstance(application).bossServiceNumberId = ""
                                list.forEach {
                                    it.listClassify = ChatRoomSource.SERVICE
                                }
                            }
                            ChatRoomReference.getInstance().save(list)
                            sendRefreshBossServiceNumberOwnerChanged.postValue(true)
                        }

                        else -> {
                            // nothing
                        }
                    }
                }
            }
        }

    fun updateUserAvatar(data: Map<String, String>) =
        viewModelScope.launch(Dispatchers.IO) {
            val accountId = data["accountId"]
            val avatarId = data["avatarId"]
            UserProfileReference.updateUserAvatar(accountId, avatarId)
            EventBus.getDefault().post(EventMsg<Any?>(MsgConstant.UPDATE_LINE_CUSTOMER_AVATAR))
        }

    fun updateCustomer(data: String) =
        viewModelScope.launch(Dispatchers.IO) {
            val customerData = JsonHelper.getInstance().from(data, UpdateCustomerNameSocket::class.java)
            val isSuccess =
                UserProfileReference.updateCustomerAlias(
                    customerData.id,
                    customerData.customerName
                )
            if (isSuccess) {
                var roomList = mutableListOf<String>()
                customerData.serviceNumberIds.forEach {
                    roomList = ChatRoomReference.getInstance().findRoomIdByServiceNumberId(it)
                    ChatRoomReference.getInstance().updateCustomerRoomName(it, customerData.customerName, customerData.id)
                }
                EventBus.getDefault().post(EventMsg<Any>(MsgConstant.REFRESH_CUSTOMER_NAME, roomList))
            }
        }

    fun doFilterAIServiceRoom(entities: List<ChatRoomEntity>) =
        viewModelScope.launch(Dispatchers.IO) {
            if (entities.isNotEmpty()) {
                for (entity in entities) {
                    if (entity.ownerId == selfUserId && entity.serviceNumberOpenType.contains("I")) {
                        // 內部服務號人員諮詢服務號, 聊天室擁有者不觸發AI服務分組
                        entity.listClassify = ChatRoomSource.MAIN
                    }
                }
                ChatRoomReference.getInstance().save(entities)
                sendRefreshDB.postValue(true)
            }
        }

    fun getTodoExpiredCount() =
        viewModelScope.launch(Dispatchers.IO) {
            var businessExpired = 0
            if (UserPref.getInstance(application).hasBusinessSystem()) {
                businessExpired = BusinessReference.getExpiredCount(null)
            }
            val todoExpired = TodoReference.getExpiredCount(null)
            todoExpiredCount.postValue(todoExpired + businessExpired)
        }

    suspend fun queryRoomById(roomId: String): ChatRoomEntity? =
        withContext(Dispatchers.IO) {
            return@withContext ChatRoomReference.getInstance().findById(roomId)
        }
}
