package tw.com.chainsea.chat.view.chat

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.text.TextUtils
import android.util.Log
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.google.common.base.Strings
import com.google.common.collect.ImmutableMap
import com.google.common.collect.Lists
import com.google.common.collect.Maps
import com.google.common.collect.Sets
import com.google.gson.reflect.TypeToken
import com.luck.picture.lib.entity.LocalMedia
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.android.common.client.type.FileMedia
import tw.com.chainsea.android.common.client.type.Media
import tw.com.chainsea.android.common.image.BitmapHelper
import tw.com.chainsea.android.common.json.JsonHelper
import tw.com.chainsea.android.common.log.CELog
import tw.com.chainsea.android.common.random.RandomHelper
import tw.com.chainsea.android.common.video.IVideoSize
import tw.com.chainsea.ce.sdk.base.MsgBuilder
import tw.com.chainsea.ce.sdk.bean.CrowdEntity
import tw.com.chainsea.ce.sdk.bean.FacebookTag
import tw.com.chainsea.ce.sdk.bean.MsgNoticeBean
import tw.com.chainsea.ce.sdk.bean.MsgStatusBean
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity
import tw.com.chainsea.ce.sdk.bean.account.UserType
import tw.com.chainsea.ce.sdk.bean.common.EnableType
import tw.com.chainsea.ce.sdk.bean.msg.ChannelType
import tw.com.chainsea.ce.sdk.bean.msg.FacebookCommentStatus
import tw.com.chainsea.ce.sdk.bean.msg.FacebookPostStatus
import tw.com.chainsea.ce.sdk.bean.msg.MessageFlag
import tw.com.chainsea.ce.sdk.bean.msg.MessageStatus
import tw.com.chainsea.ce.sdk.bean.msg.MessageType
import tw.com.chainsea.ce.sdk.bean.msg.MsgKitAssembler
import tw.com.chainsea.ce.sdk.bean.msg.SourceType
import tw.com.chainsea.ce.sdk.bean.msg.TemplateContent
import tw.com.chainsea.ce.sdk.bean.msg.Tools
import tw.com.chainsea.ce.sdk.bean.msg.content.AtContent
import tw.com.chainsea.ce.sdk.bean.msg.content.BusinessContent
import tw.com.chainsea.ce.sdk.bean.msg.content.FileContent
import tw.com.chainsea.ce.sdk.bean.msg.content.ImageContent
import tw.com.chainsea.ce.sdk.bean.msg.content.StickerContent
import tw.com.chainsea.ce.sdk.bean.msg.content.TextContent
import tw.com.chainsea.ce.sdk.bean.msg.content.UndefContent
import tw.com.chainsea.ce.sdk.bean.msg.content.VideoContent
import tw.com.chainsea.ce.sdk.bean.msg.content.VoiceContent
import tw.com.chainsea.ce.sdk.bean.parameter.Sort
import tw.com.chainsea.ce.sdk.bean.response.AileTokenApply
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType
import tw.com.chainsea.ce.sdk.bean.room.QuickReplyItem
import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberStatus
import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberType
import tw.com.chainsea.ce.sdk.bean.servicenumber.type.GroupPrivilegeEnum
import tw.com.chainsea.ce.sdk.bean.todo.TodoStatus
import tw.com.chainsea.ce.sdk.cache.ChatMemberCacheService
import tw.com.chainsea.ce.sdk.config.AppConfig
import tw.com.chainsea.ce.sdk.database.DBManager
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.database.sp.UserPref
import tw.com.chainsea.ce.sdk.event.EventBusUtils
import tw.com.chainsea.ce.sdk.event.EventMsg
import tw.com.chainsea.ce.sdk.event.MsgConstant
import tw.com.chainsea.ce.sdk.http.ce.ApiManager
import tw.com.chainsea.ce.sdk.http.ce.ApiPath
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener
import tw.com.chainsea.ce.sdk.http.ce.base.BaseViewModel
import tw.com.chainsea.ce.sdk.http.ce.base.TokenRepository
import tw.com.chainsea.ce.sdk.http.ce.model.User
import tw.com.chainsea.ce.sdk.http.ce.request.ActiveServiceConsultArray
import tw.com.chainsea.ce.sdk.http.ce.request.AddContactFriendRequest
import tw.com.chainsea.ce.sdk.http.ce.request.FromAppointRequest
import tw.com.chainsea.ce.sdk.http.ce.request.MessageSendRequest
import tw.com.chainsea.ce.sdk.http.ce.request.ServiceNumberChatroomAgentServicedRequest
import tw.com.chainsea.ce.sdk.http.ce.request.TokenApplyRequest
import tw.com.chainsea.ce.sdk.http.ce.request.UploadManager
import tw.com.chainsea.ce.sdk.http.ce.request.UploadManager.OnFileUploadListener
import tw.com.chainsea.ce.sdk.http.ce.request.UploadManager.OnUploadListener
import tw.com.chainsea.ce.sdk.http.ce.request.UploadManager.OnVoiceUploadListener
import tw.com.chainsea.ce.sdk.lib.ErrCode
import tw.com.chainsea.ce.sdk.network.NetworkManager.provideRetrofit
import tw.com.chainsea.ce.sdk.network.model.common.ApiErrorData
import tw.com.chainsea.ce.sdk.network.model.common.ApiResult
import tw.com.chainsea.ce.sdk.network.model.common.CommonResponse
import tw.com.chainsea.ce.sdk.network.model.common.ErrorCode
import tw.com.chainsea.ce.sdk.network.model.common.Header
import tw.com.chainsea.ce.sdk.network.model.request.CheckCommentStatusRequest
import tw.com.chainsea.ce.sdk.network.model.request.SendAtMessageRequest
import tw.com.chainsea.ce.sdk.network.model.request.SendFacebookCommentRequest
import tw.com.chainsea.ce.sdk.network.model.response.ChatRoomMemberResponse
import tw.com.chainsea.ce.sdk.network.model.response.CheckCommentResponse
import tw.com.chainsea.ce.sdk.network.model.response.FromAppointResponse
import tw.com.chainsea.ce.sdk.network.model.response.RobotServiceResponse
import tw.com.chainsea.ce.sdk.network.model.response.SendFacebookCommentResponse
import tw.com.chainsea.ce.sdk.network.model.response.ServicesIdentityListResponse
import tw.com.chainsea.ce.sdk.reference.AccountRoomRelReference
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference
import tw.com.chainsea.ce.sdk.reference.MessageReference
import tw.com.chainsea.ce.sdk.reference.ServiceNumberReference
import tw.com.chainsea.ce.sdk.reference.TodoReference
import tw.com.chainsea.ce.sdk.reference.UserProfileReference
import tw.com.chainsea.ce.sdk.service.ChatMessageService
import tw.com.chainsea.ce.sdk.service.ChatRoomService
import tw.com.chainsea.ce.sdk.service.ChatServiceNumberService
import tw.com.chainsea.ce.sdk.service.ChatServiceNumberService.ServicedTransferType
import tw.com.chainsea.ce.sdk.service.FileService
import tw.com.chainsea.ce.sdk.service.FileService.FacebookImageArgs
import tw.com.chainsea.ce.sdk.service.RepairMessageService
import tw.com.chainsea.ce.sdk.service.TodoService
import tw.com.chainsea.ce.sdk.service.listener.AServiceCallBack
import tw.com.chainsea.ce.sdk.service.listener.AgentSnatchCallback
import tw.com.chainsea.ce.sdk.service.listener.RoomRecentCallBack
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack
import tw.com.chainsea.ce.sdk.service.type.ChatRoomSource
import tw.com.chainsea.ce.sdk.service.type.RefreshSource
import tw.com.chainsea.chat.App
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.aiff.AiffManager
import tw.com.chainsea.chat.aiff.database.AiffDB
import tw.com.chainsea.chat.aiff.database.entity.AiffInfo
import tw.com.chainsea.chat.config.AiffEmbedLocation
import tw.com.chainsea.chat.config.BundleKey
import tw.com.chainsea.chat.flow.dataclass.ReSendMessageFormat
import tw.com.chainsea.chat.flow.dataclass.SendBusinessFormat
import tw.com.chainsea.chat.flow.dataclass.SendButtonClickedFormat
import tw.com.chainsea.chat.flow.dataclass.SendFileFormat
import tw.com.chainsea.chat.flow.dataclass.SendImageFormat
import tw.com.chainsea.chat.flow.dataclass.SendImageGifFormat
import tw.com.chainsea.chat.flow.dataclass.SendImageWithBitMapFormat
import tw.com.chainsea.chat.flow.dataclass.SendVideoFormat
import tw.com.chainsea.chat.flow.dataclass.SendVoiceFormat
import tw.com.chainsea.chat.keyboard.view.HadEditText.AtMentionComponent
import tw.com.chainsea.chat.keyboard.view.HadEditText.SendData
import tw.com.chainsea.chat.lib.ChatService
import tw.com.chainsea.chat.lib.NetworkUtils
import tw.com.chainsea.chat.lib.PictureParse
import tw.com.chainsea.chat.lib.Tools.generateMessageId
import tw.com.chainsea.chat.lib.Tools.getNameWithoutExt
import tw.com.chainsea.chat.messagekit.lib.MessageDomino
import tw.com.chainsea.chat.network.contact.ContactRepository
import tw.com.chainsea.chat.ui.activity.ChatNormalActivity
import tw.com.chainsea.chat.ui.adapter.entity.LoadMoreEntity
import tw.com.chainsea.chat.util.CountDownTimer
import tw.com.chainsea.chat.util.DaVinci
import tw.com.chainsea.chat.util.ImageUtil
import tw.com.chainsea.chat.util.SortUtil
import tw.com.chainsea.chat.util.TimeUtil
import tw.com.chainsea.chat.util.UploadUtil
import tw.com.chainsea.chat.view.chatroom.adapter.SmallRoomData
import tw.com.chainsea.chat.view.homepage.EmployeeInformationHomepageActivity
import tw.com.chainsea.chat.view.homepage.SelfInformationHomepageActivity
import tw.com.chainsea.chat.view.homepage.VisitorHomepageActivity
import tw.com.chainsea.chat.view.service.ServiceNumberAgentsManageRepository
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Collections
import java.util.LinkedList
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer
import java.util.stream.Collectors

class ChatViewModel(
    private val application: Application,
    private val chatRepository: ChatRepository,
    private val contactRepository: ContactRepository,
    private val serviceNumberAgentsManageRepository: ServiceNumberAgentsManageRepository,
    private val tokenRepository: TokenRepository
) : BaseViewModel(application, tokenRepository),
    MediaSender {
    private val selfUserId by lazy { TokenPref.getInstance(application).userId }
    private val tokenId by lazy { TokenPref.getInstance(application).tokenId }
    private val mSelfAccount by lazy { DBManager.getInstance().queryFriend(selfUserId) }

    val chatRoomEntity = MutableLiveData<ChatRoomEntity>()
    val updateChatRoomEntity = MutableLiveData<ChatRoomEntity>()

    val managerList = MutableLiveData<List<ChatRoomMemberResponse>>()

    val becomeOwnerStatus = MutableLiveData<String>()

    val sendUpdateMember =
        MutableLiveData<Triple<Boolean, ChatRoomEntity, List<UserProfileEntity>>>() // Triple(Boolean -> 用來判斷是否要更新成員列表選單)
    val sendProvisionalMember = MutableLiveData<List<String>>()
    val sendCloseChatActivity = MutableLiveData<Int>()

    // 取消 AI 監控成功
    val isCancelAiWarningSuccess = MutableLiveData<Boolean>()

    // ChatHomePage
    val crowdEntity = MutableLiveData<CrowdEntity>()

    // 升級社團
    val upgradeGroup = MutableLiveData<CrowdEntity>()

    // 顯示臨時成員成功轉乘服務人員
    val showServiceNumberAddFromProvisionalDialog = MutableLiveData<ChatRoomEntity>()

    val userProfile = MutableLiveData<UserProfileEntity>()

    // 解散社團/多人聊天室成功
    val isRoomDismissSuccess = MutableLiveData<Boolean>()

    // 聊天室成員離開成功
    val isChatMemberExitSuccess = MutableLiveData<Boolean>()

    // 聊天室是否靜音
    val isRoomMute = MutableLiveData<Boolean>()

    // 聊天室是否置頂
    val isRoomTop = MutableLiveData<Boolean>()

    // 聊天室是否滑動刪除成功
    val isRoomDeleted = MutableLiveData<Boolean>()

    // 聊天室是否手動已讀/未讀
    val isMessageRead = MutableLiveData<Boolean>()

    // 服務號成員列表(含權限)
    val agentsList = MutableLiveData<List<UserProfileEntity>>()

    // 服務號聊天室左下諮詢清單
    val consultList = MutableLiveData<List<SmallRoomData>>()
    private var _consultList = mutableSetOf<SmallRoomData>()

    // Boolean 判斷是否是 ai 諮詢
    val consultRoomId = MutableLiveData<Pair<Boolean, String>>()

    val startConsultError = MutableLiveData<ApiErrorData>()

    val memberList = MutableLiveData<List<UserProfileEntity>>()
    val sendUpdatedRoomTitle = MutableSharedFlow<String>()
    val sendShowLoadMoreMsg = MutableLiveData<MutableList<MessageEntity>>()

    val onProvisionalIdsGet = MutableLiveData<List<String>>()
    val sendToast = MutableSharedFlow<Int>()
    val isSettingBusinessCardInfo = MutableStateFlow(false)
    var channelType: String = ""
    val sendCloseExtraLayout = MutableLiveData(false)
    val hideLoadingDialog = MutableLiveData(false)
    val sendToastByWord = MutableSharedFlow<String>()
    val sendGroupAddPermission = MutableSharedFlow<Boolean>()

    // ------refactor ChatPresenter variants-----start--
    var unreadMessageId = ""
    var unreadNum = 0
    private val mUnReadMsgIds: MutableList<String> = mutableListOf()
    var recordMode: Boolean = false
    var roomEntity: ChatRoomEntity? = null // 儲存聊天室entity
    var isObserverKeyboard: Boolean = false
    val sendSearchKeyWord = MutableSharedFlow<String>()
    private val loadMoreMessages: MutableList<MessageEntity> = mutableListOf()

    // 主訊息資料
    val mainMessageData: MutableList<MessageEntity> = Lists.newArrayList()
    val mainMessageDataFlow = MutableSharedFlow<Pair<Boolean, MutableList<MessageEntity>>>()
    val sendDisplayThemeMessage = MutableSharedFlow<Triple<String, Boolean, MessageEntity>>()
    private var currentDate: String? = null
    val sendNotifyChange = MutableSharedFlow<MessageEntity>()
    private val messageTimeLineFormat =
        DateTimeFormatter
            .ofPattern("MMMdd日(EEE)")
            .withLocale(Locale.TAIWAN)
            .withZone(ZoneId.systemDefault())
    val sendChatRoomTitleChangeListener = MutableSharedFlow<MessageEntity>()
    val sendThemeMRVData = MutableSharedFlow<MessageEntity>()
    val sendBindingDataHandled = MutableSharedFlow<Triple<Boolean, Boolean, MessageEntity>>()
    var doesNotExist = false
    val sendShowMetaOverTimeView = MutableSharedFlow<Boolean>()
    val sendScrollToTop = MutableSharedFlow<Unit>()
    val sendHighLightUnReadLine = MutableSharedFlow<Boolean>()
    val sendShowIsNotMemberMessage = MutableSharedFlow<String>()
    val sendSetQuickReply = MutableSharedFlow<List<QuickReplyItem>>()
    val sendSnatchedAgent = MutableSharedFlow<Any>()
    val sendShowLoadingView = MutableSharedFlow<Int>()
    val sendHideLoadingView = MutableSharedFlow<Unit>()
    val sendShowErrorMsg = MutableSharedFlow<String?>()
    val sendTransferModeDisplay = MutableSharedFlow<List<MessageEntity>>()
    val sendSetupAppointStatus = MutableSharedFlow<FromAppointRequest.Resp?>()
    val sendSetupBottomServicedStatus = MutableSharedFlow<Triple<ServiceNumberStatus, ServiceNumberType, ServiceNumberChatroomAgentServicedRequest.Resp?>>()

    // 服務號渠道相關UI
    var appointResp: FromAppointRequest.Resp? = null
    val sendSetRobotChatRecord = MutableSharedFlow<ServiceNumberChatroomAgentServicedRequest.Resp?>()
    val sendLoadRobotChatRecordLink = MutableSharedFlow<String>()
    val sendRobotStopStatus = MutableSharedFlow<Unit>()
    var themeMessage: MessageEntity? = null
    var nearMessage: MessageEntity? = null
    val sendShowSendVideoProgress = MutableSharedFlow<String>()
    var isThemeOpen = false // 主題聊天室是否開啟狀態
    var isChildRoomOpen = false
    val sendUpdateSendVideoProgress = MutableSharedFlow<Unit>()
    val sendUpdateSendVideoProgressInt = MutableSharedFlow<Int>()
    val sendUpdateMsgProgress = MutableSharedFlow<Pair<String, Int>>()
    val sendUpdateMsgStatus = MutableSharedFlow<Triple<String?, String, MessageStatus>>()
    val sendDismissSendVideoProgress = MutableSharedFlow<Unit>()
    val sendOnSendFacebookImageReplySuccess = MutableSharedFlow<Unit>()
    val sendOnSendFacebookImageReplyFailed = MutableSharedFlow<String>()
    val sendInitProvisionMemberList = MutableSharedFlow<Pair<List<UserProfileEntity>, List<String>>>()
    val loadMoreMsgs: MutableList<MessageEntity> = mutableListOf()
    val sendOnRefreshMore = MutableSharedFlow<Pair<MutableList<MessageEntity>, MessageEntity>>()
    val sendMoveToFacebookReplyMessage = MutableSharedFlow<Int>()
    private val tempFacebookAlreadyCheckStatus = HashSet<String>()
    private var isTokenNeedRefresh = false
    private val checkCommentQueue = LinkedList<String>()
    val sendUpdateFacebookStatus = MutableSharedFlow<MessageEntity>()
    val sendDoChatRoomSnatchByAgent = MutableSharedFlow<Pair<Boolean, SendData>>()
    val sendShowToast = MutableSharedFlow<Int>()
    val sendShowErrorToast = MutableSharedFlow<String>()
    val sendClearTypedMessage = MutableSharedFlow<Unit>()
    var isVisible = false
    private val mReceivedNoticeIds: MutableList<String?> = mutableListOf()
    val sendRefreshListView = MutableSharedFlow<Unit>()
    val sendShowNoMoreMessage = MutableSharedFlow<Unit>()
    val sendUpdateMsgStatusPair = MutableSharedFlow<Pair<String?, MessageStatus>>()
    val sendSetThemeOpen = MutableSharedFlow<Boolean>()
    val sendMessageToUI = MutableSharedFlow<SendData>()
    val sendSetup = MutableSharedFlow<Unit>()
    val sendSetIsBlock = MutableSharedFlow<Boolean>()
    val sendNavigateToChat = MutableSharedFlow<ChatRoomEntity>()
    val sendSetChatDisable = MutableSharedFlow<Int>()
    val sendSetChatEnable = MutableSharedFlow<Unit>()
    val sendClearProvisionalMember = MutableSharedFlow<Unit>()
    var executeRetractCount: Int = 0
    val scopeRetractTipInvisible = MutableSharedFlow<Unit>()
    val sendBindingBusiness = MutableSharedFlow<Triple<String, BusinessContent, Boolean>>()

    val refreshCurrentMessagePosition = MutableSharedFlow<Int>()
    val scrollListToPosition = MutableSharedFlow<Int>()
    val stopLoadMoreMessage = MutableSharedFlow<Boolean>()
    val notifyRecyclerRemove = MutableSharedFlow<Int>()
    val updateFacebookCommentStats = MutableSharedFlow<MessageEntity>()
    val onMessageReceived = MutableSharedFlow<MutableList<MessageEntity>>()
    val onAvatarIconClickIntent = MutableSharedFlow<Intent>()
    var isUserScroll = false
    val onReplyNearMessageClick = MutableSharedFlow<Int>()
    val onMessageDeleted = MutableSharedFlow<Int>()
    private var syncMessageRetryCount = 0
    val showSnatchRobotDialog = MutableSharedFlow<Any>()
    val onSnatchRobotSuccess = MutableSharedFlow<Pair<Boolean, Any>>()
    // ------refactor ChatPresenter variants-----end--

    private val sendingState = MutableStateFlow<SendingState?>(null)
    val messageListRecyclerViewPool = RecyclerView.RecycledViewPool()
    private val _searchResult = Lists.newArrayList<Any>()
    val searchResult = MutableSharedFlow<Pair<String, List<Any>>>()

    private val systemUserName by lazy { TokenPref.getInstance(application).systemUserName }
    private val systemUserId by lazy { TokenPref.getInstance(application).systemUserId }
    private val systemUserAvatar by lazy { TokenPref.getInstance(application).systemUserAvatarId }
    val onMessageRead = MutableSharedFlow<Boolean>()

    private val sequentialSender =
        SequentialMediaSender(
            viewModelScope = viewModelScope,
            mediaSender = this,
            onStateChange = { state -> sendingState.value = state },
            application = application
        )

    private val imageSendQueue = Channel<SendImageRequest>(capacity = Channel.UNLIMITED)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            for (request in imageSendQueue) {
                processSendImage(request)
            }
        }
    }

    // meta 渠道的回覆時限計時
    private val metaReplyLimitTimer =
        CountDownTimer(
            viewModelScope = viewModelScope,
            onTick = {},
            onFinish = {
                viewModelScope.launch {
                    sendShowMetaOverTimeView.emit(true)
                }
            }
        )

    fun checkIsChatMember() =
        viewModelScope.launch {
            roomEntity?.let { room ->
                checkTokenValid(chatRepository.getRoomItem(room.id, selfUserId))?.collect {
                    when (it) {
                        is ApiResult.Failure -> {
                            errorMessage.postValue(it.errorMessage.errorMessage)
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
                        errorMessage.postValue(it.errorMessage.errorMessage)
                    }

                    is ApiResult.Success -> {
                        val status: Boolean = ChatRoomReference.getInstance().save(it.data)
                        if (status) {
                            roomEntity = it.data
                            chatRoomEntity.postValue(it.data)
                        } else {
                            errorMessage.postValue("save room entity failed ")
                        }
                    }

                    else -> {}
                }
            }
        }

    /**
     * 拿服務號的臨時成員資料
     * */
    fun getProvisionalMember(roomId: String) =
        viewModelScope.launch {
            checkTokenValid(chatRepository.getRoomItem(roomId, selfUserId))?.collect {
                when (it) {
                    is ApiResult.Failure -> {
                        errorMessage.postValue(it.errorMessage.errorMessage)
                    }

                    is ApiResult.Success -> {
                        val status: Boolean = ChatRoomReference.getInstance().save(it.data)
                        if (status) {
                            onProvisionalIdsGet.postValue(it.data.provisionalIds)
                        }
                    }

                    else -> {
                        // nothing
                    }
                }
            }
        }

    /**
     * 聊天室成員&權限
     * */
    fun getChatMember(roomId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            checkTokenValid(chatRepository.getChatMember(roomId))?.collect {
                when (it) {
                    is ApiResult.Loading -> loading.postValue(it.isLoading)
                    is ApiResult.Failure -> {
                        errorMessage.postValue(it.errorMessage.errorMessage)
                    }

                    is ApiResult.Success -> {
                        val filterManager =
                            it.data
                                .stream()
                                .filter { member ->
                                    member.privilege == GroupPrivilegeEnum.Manager
                                }?.collect(Collectors.toList())

                        filterManager?.let {
                            managerList.postValue(it)
                        }
                        roomEntity?.chatRoomMember = it.data
                        ChatRoomReference.getInstance().updateChatRoomMember(roomId, it.data)
                        doHandleGroupMemberPrivilege()
                    }

                    else -> {}
                }
            }
        }

    /**
     * 成為社團擁有者
     * */
    fun becomeOwner(roomId: String) =
        viewModelScope.launch {
            checkTokenValid(chatRepository.becomeOwner(roomId))?.collect {
                when (it) {
                    is ApiResult.Loading -> loading.postValue(it.isLoading)
                    is ApiResult.Failure -> {
                        errorMessage.postValue(application.getString(R.string.api_http_failure))
                    }

                    is ApiResult.Success -> {
                        it.data._header_?.let { header ->
                            val becomeOwnerString =
                                if (header.success!!) {
                                    application.getString(R.string.group_room_become_owner)
                                } else if (header.stackTrace!!.contains("Ce-Group-NotManager")) {
                                    application.getString(R.string.group_room_no_permissions)
                                } else if (header.stackTrace!!.contains("Ce-Group-HasOwner")) {
                                    application.getString(R.string.group_room_already_owner)
                                } else {
                                    application.getString(R.string.api_http_failure)
                                }
                            becomeOwnerStatus.postValue(becomeOwnerString)
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
                    CELog.e("contact add failed: ${it.errorMessage}")
                }

                is ApiResult.Success -> {
                    launch {
                        it.data.roomIds?.let {
                            it[0]?.let { roomId ->
                                getRoomItem(roomId)
                                UserProfileReference.updateUserRoomId(userId, roomId)
                            }
                        }
                    }
                }

                else -> {}
            }
        }
    }

    fun getChatRoomEntity(
        userId: String,
        alias: String
    ) = viewModelScope.launch {
        val userProfile = DBManager.getInstance().queryFriend(userId)
        if (Strings.isNullOrEmpty(userProfile.roomId)) {
            addContactFriend(userId, alias)
        } else {
            getRoomItem(userProfile.roomId)
        }
    }

    fun doHandleMemberFromDB(
        data: List<String> = Lists.newArrayList(),
        roomId: String,
        isMemberRemoved: Boolean = false
    ) = viewModelScope.launch(Dispatchers.IO) {
        val entity = ChatRoomReference.getInstance().findById(roomId)
        entity?.let { chatRoomEntity ->
            if (isMemberRemoved) { // 移除成員
                data.forEach { removedMemberId ->
                    chatRoomEntity.chatRoomMember.removeIf { it.memberId == removedMemberId }
                }
                val memberIds = mutableListOf<String>()
                val members = mutableListOf<UserProfileEntity>()
                chatRoomEntity.chatRoomMember.forEach {
                    memberIds.add(it.memberId)
                    val userProfile = DBManager.getInstance().queryFriend(it.memberId)
                    members.add(userProfile)
                }
                chatRoomEntity.memberIds = memberIds
                chatRoomEntity.members = members

                ChatRoomReference.getInstance().updateChatRoomMember(roomId, chatRoomEntity.chatRoomMember)

                // 是多人聊天室 & 沒改過聊天室標題
                if (chatRoomEntity.type == ChatRoomType.discuss && !chatRoomEntity.isCustomName) {
                    setDiscussRoomTitleWhenMemberRemoved(chatRoomEntity, data)
                } else {
                    sendUpdateMember.postValue(Triple(true, chatRoomEntity, getMembersFromChatRoomMember(chatRoomEntity.chatRoomMember)))
                }
            } else {
                // 新增成員
                when {
                    // 多人聊天室
                    (chatRoomEntity.type == ChatRoomType.discuss) -> {
                        checkTokenValid(
                            chatRepository.getDiscussRoomChatMember(
                                roomId,
                                ChatRoomType.discuss,
                                chatRoomEntity.isCustomName
                            )
                        )?.collect {
                            when (it) {
                                is ApiResult.Success -> {
                                    val roomEntity =
                                        ChatRoomReference.getInstance().findById(roomId)
                                    roomEntity?.let { entity ->
                                        sendUpdateMember.postValue(Triple(false, entity, getMembersFromChatRoomMember(entity.chatRoomMember)))
                                    }
                                }

                                else -> {}
                            }
                        }
                    }
                    // 社團
                    chatRoomEntity.type == ChatRoomType.group -> {
                        checkTokenValid(chatRepository.getChatMember(roomId))?.collect {
                            when (it) {
                                is ApiResult.Success -> {
                                    roomEntity?.let { entity ->
                                        entity.chatRoomMember = it.data
                                        val memberList = mutableListOf<UserProfileEntity>()
                                        val memberIds = mutableListOf<String>()
                                        it.data.forEach {
                                            val member = DBManager.getInstance().queryFriend(it.memberId)
                                            memberIds.add(it.memberId)
                                            memberList.add(member)
                                        }
                                        entity.members = memberList
                                        entity.memberIds = memberIds
                                        ChatRoomReference.getInstance().save(entity)
                                        sendUpdateMember.postValue(Triple(false, entity, getMembersFromChatRoomMember(it.data)))
                                    }
                                }

                                else -> {
                                    // nothing
                                }
                            }
                        }
//                            val newMemberId = data.subtract(chatRoomEntity.memberIds.toSet())
//                            val newMember = DBManager.getInstance().queryFriend(newMemberId.first())
//                            chatRoomEntity.members.add(newMember)
//                            chatRoomEntity.memberIds = data
//                            val isAdd = ChatRoomReference.getInstance()
//                                .updateMemberIds(chatRoomEntity.memberIds, roomId)
//                            if (isAdd)
//                                sendUpdateMember.postValue(Pair(false, chatRoomEntity))
                    }

                    chatRoomEntity.type == ChatRoomType.services -> { // 服務號臨時成員
                        val serviceNumber =
                            DBManager
                                .getInstance()
                                .queryServiceNumberById(chatRoomEntity.serviceNumberId)
                        val serviceNumberMemberIds =
                            serviceNumber.memberItems.map { ids -> ids.id }
                        val provisionalMemberIds =
                            data
                                .subtract(serviceNumberMemberIds.toSet())
                                .filter { chatRoomEntity.ownerId != it }
                        sendProvisionalMember.postValue(provisionalMemberIds.toList())
                    }
                }
            }
        }
    }

    fun doKickOutChatRoomByOtherMember(roomId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            val isDeleted = DBManager.getInstance().deleteRoomListItem(roomId)
            if (isDeleted) {
                sendCloseChatActivity.postValue(R.string.text_kick_out_member_by_others)
            }
        }

    /**
     * 組合多人聊天室 title
     *
     * @param chatRoomEntity 該聊天室物件
     * @param removedUser 被移除的成員
     * */
    fun setDiscussRoomTitleWhenMemberRemoved(
        chatRoomEntity: ChatRoomEntity?,
        removedUser: List<String>
    ) = viewModelScope.launch {
        chatRoomEntity?.let { chatRoomEntity ->
            val chatRoomMemberList = chatRoomEntity.chatRoomMember.toMutableList()
            val membersList = chatRoomEntity.members.toMutableList()
            val memberIdsList = chatRoomEntity.memberIds.toMutableList()

            removedUser.forEach {
                chatRoomMemberList.removeIf { member ->
                    member.memberId == it
                }

                membersList.removeIf { member ->
                    member.id == it
                }

                memberIdsList.remove(it)
            }

            chatRoomEntity.apply {
                chatRoomMember = chatRoomMemberList
                members = membersList
                memberIds = memberIdsList
            }
            // 判斷是不是有自訂聊天室名稱
            if (!chatRoomEntity.isCustomName) {
                val chatRoomMember =
                    if (chatRoomEntity.chatRoomMember.size > 4) {
                        chatRoomEntity.chatRoomMember.subList(
                            0,
                            4
                        )
                    } else {
                        chatRoomEntity.chatRoomMember
                    }
                val stringBuilder = StringBuilder()
                for (i in chatRoomMember.indices) {
                    val userProfileEntity =
                        DBManager.getInstance().queryUser(chatRoomMember[i].memberId)
                    stringBuilder.append(userProfileEntity.nickName)
                    if (i < chatRoomMember.size - 1) {
                        stringBuilder.append(",")
                    }
                }
                chatRoomEntity.name = stringBuilder.toString()
            }
            ChatRoomReference.getInstance().updateChatRoomMember(chatRoomEntity.id, chatRoomEntity.chatRoomMember)
            updateChatRoomEntity.postValue(chatRoomEntity)
        }
    }

    fun deletedRoom(roomId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            val isDeleted = DBManager.getInstance().deleteRoomListItem(roomId)
            if (isDeleted) {
                sendCloseChatActivity.postValue(-1)
            }
        }

    fun doUpdateRoomTitle(
        roomId: String,
        userId: String,
        nickName: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        val isUpdated = UserProfileReference.updateNickNameById(userId, nickName)
        if (isUpdated) {
            val entity = ChatRoomReference.getInstance().findById(roomId)
            val title = StringBuilder()
            entity?.let {
                if (it.type == ChatRoomType.discuss && !it.isCustomName) {
                    val chatMembers =
                        ChatRoomReference.getInstance().queryChatRoomMember(roomId)

                    val subList =
                        if (chatMembers.size >= 4) {
                            chatMembers.subList(0, 4)
                        } else {
                            chatMembers
                        }

                    val isContainUser =
                        subList.any { item ->
                            item.memberId == userId
                        }
                    if (isContainUser) {
                        subList.forEachIndexed { index, s ->
                            val member = DBManager.getInstance().queryFriend(s.memberId)
                            member?.let { user ->
                                title.append(user.nickName)
                                if (index < subList.size - 1) {
                                    title.append(",")
                                } else {
                                    return@forEachIndexed
                                }
                            }
                        }
                        it.name = title.toString()
                    }
                    it.members.forEachIndexed { _, user ->
                        if (user.id == userId) {
                            user.nickName = nickName
                            return@forEachIndexed
                        }
                    }
                    ChatRoomReference.getInstance().save(it)
                    sendUpdateMember.postValue(Triple(true, it, getMembersFromChatRoomMember(it.chatRoomMember)))
                }
            }
        }
    }

    // 取得服務號的成員權限
    fun getServiceEntity(serviceNumberId: String) =
        viewModelScope.launch {
            checkTokenValid(serviceNumberAgentsManageRepository.getServiceEntity(serviceNumberId))?.collect {
                when (it) {
                    is ApiResult.Success -> {
                        val users =
                            DBManager.getInstance().queryMembersFromUser(
                                it.data.map { member -> member.id }
                            )
                        it.data.forEach {
                            val userProfileEntity = users.find { user -> user.id == it.id }
                            userProfileEntity?.privilege = it.privilege
                        }
                        val sortedList = SortUtil.sortServiceNumberOwnerManagerByPrivilege(users)
                        agentsList.postValue(sortedList)
                    }

                    else -> {}
                }
            }
        }

    // 取消 ai 監控
    fun cancelAiWarning(roomId: String) =
        viewModelScope.launch {
            checkTokenValid(chatRepository.cancelAiWarning(roomId))?.collect {
                when (it) {
                    is ApiResult.Loading -> {
                        loading.postValue(it.isLoading)
                    }

                    is ApiResult.Failure -> {
                        isCancelAiWarningSuccess.postValue(false)
                    }

                    is ApiResult.Success -> {
                        isCancelAiWarningSuccess.postValue(true)
                    }

                    else -> {}
                }
            }
        }

    // 送 quick reply
    fun sendQuickReplyMessage(
        roomId: String,
        type: String,
        content: String
    ) = viewModelScope.launch {
        checkTokenValid(chatRepository.sendQuickReply(roomId, type, content))?.collect {
            when (it) {
                is ApiResult.Loading -> {
                    loading.postValue(it.isLoading)
                }

                is ApiResult.Failure -> {
                }

                is ApiResult.Success -> {
                }

                else -> {}
            }
        }
    }

//    fun getGroupMember(roomId: String): Int {
//        val group = DBManager.getInstance().queryGroupInfo(roomId)
//        group?.let {
//            it.memberIds?.let { ids ->
//                return ids.size
//            }
//        }
//        return 3
//    }

    // /更新 chatRoomEntity
//    fun updateSession(roomId: String) = viewModelScope.launch(Dispatchers.IO) {
//        chatRepository.getRoomItem(roomId, selfUserId).collect {
//            when (it) {
//                is ApiResult.Success -> {
//                    updateChatRoomEntity.postValue(it.data)
//                    ChatRoomReference.getInstance().save(it.data)
//                }
//
//                else -> {}
//            }
//        }
//    }

    fun upgradeToGroup(roomId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            checkTokenValid(chatRepository.getHomePage(roomId))?.collect {
                when (it) {
                    is ApiResult.Success -> {
                        upgradeGroup.postValue(it.data)
                    }

                    else -> {}
                }
            }
        }

    // 聊天室首頁
    fun getChatHomePage(roomId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            checkTokenValid(chatRepository.getHomePage(roomId))?.collect {
                when (it) {
                    is ApiResult.Success -> {
                        it.data.members?.let { member ->
                            ChatMemberCacheService.refresh(roomId)
                            ChatRoomReference
                                .getInstance()
                                .updateOwnerIdById(it.data.id, it.data.ownerId)
                            UserProfileReference.saveUserProfiles(
                                null,
                                Sets.newHashSet<UserProfileEntity>(member)
                            )
                            crowdEntity.postValue(it.data)
                        }
                    }

                    else -> {}
                }
            }
        }

    // 臨時成員轉服務人員
    fun serviceNumberAddFromProvisional(roomId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            checkTokenValid(chatRepository.getRoomItem(roomId, selfUserId))?.collect {
                when (it) {
                    is ApiResult.Success -> {
                        showServiceNumberAddFromProvisionalDialog.postValue(it.data)
                    }

                    else -> {}
                }
            }
        }

    // 取得用戶資訊
    fun getUserItem(userId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            val profile = UserProfileReference.findById(null, userId)
            profile?.let {
                userProfile.postValue(it)
            } ?: run {
                checkTokenValid(contactRepository.getUserItem(userId))?.collect {
                    when (it) {
                        is ApiResult.Success -> {
                            userProfile.postValue(it.data)
                        }

                        else -> {}
                    }
                }
            }
        }

//    fun getMemberList(roomId: String) = viewModelScope.launch(Dispatchers.IO) {
//        chatRepository.getMemberList(roomId).collect {
//            when (it) {
//                is ApiResult.Success -> {
//                    it.data?.forEach {
//                        if (!it.avatarId.isNullOrEmpty()) {
//                            MessageReference.updateMessageAvatar(null, it.id, it.avatarId)
//                        }
//                    }
//                }
//
//                else -> {}
//            }
//        }
//    }

    // 擁有者解散社團
    fun roomDismiss(roomId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            checkTokenValid(chatRepository.roomDismiss(roomId))?.collect {
                when (it) {
                    is ApiResult.Success -> {
                        ChatRoomReference.getInstance().deleteById(roomId)
                        TodoService.unBindRoom(application, roomId, null)
                        isRoomDismissSuccess.postValue(true)
                    }

                    else -> {}
                }
            } ?: run {
                sendToast.emit(R.string.text_disband_crowd_failure)
            }
        }

    // 自己離開社團
    fun chatMemberExit(
        roomId: String,
        ownerId: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        checkTokenValid(chatRepository.chatMemberExit(roomId, ownerId))?.collect {
            when (it) {
                is ApiResult.Success -> {
                    ChatRoomReference.getInstance().deleteById(roomId)
//                    DBManager.getInstance().deleteRoomListItem(roomId) //v2 api
                    DBManager.getInstance().deleteGroup(roomId)
                    TodoService.unBindRoom(application, roomId, null)
                    isChatMemberExitSuccess.postValue(true)
                }

                else -> {}
            }
        } ?: run {
            sendToast.emit(R.string.text_leave_crowd_failure)
        }
    }

    // 是否靜音
    fun changeMute(
        isMute: Boolean,
        roomId: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        if (isMute) {
            checkTokenValid(chatRepository.cancelRoomMute(roomId))?.collect {
                when (it) {
                    is ApiResult.Success -> {
                        isRoomMute.postValue(false)
                    }

                    else -> {
                        // nothing
                    }
                }
            }
        } else {
            checkTokenValid(chatRepository.muteRoom(roomId))?.collect {
                when (it) {
                    is ApiResult.Success -> {
                        isRoomMute.postValue(true)
                    }

                    else -> {
                        // nothing
                    }
                }
            }
        }
    }

    // 是否置頂
    fun changeTop(
        itTop: Boolean,
        roomId: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        if (itTop) {
            checkTokenValid(chatRepository.cancelRoomTop(roomId))?.collect {
                when (it) {
                    is ApiResult.Success -> {
                        isRoomTop.postValue(false)
                    }

                    else -> {
                        // nothing
                    }
                }
            }
        } else {
            checkTokenValid(chatRepository.topRoom(roomId))?.collect {
                when (it) {
                    is ApiResult.Success -> {
                        isRoomTop.postValue(true)
                    }

                    else -> {
                        // nothing
                    }
                }
            }
        }
    }

    // 滑動刪除聊天室
    fun deleteRoom(roomId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            checkTokenValid(chatRepository.deleteRoom(roomId))?.collect {
                when (it) {
                    is ApiResult.Success -> {
                        isRoomDeleted.postValue(false)
                    }

                    else -> {
                        // nothing
                    }
                }
            }
        }

    // 手動設定未讀/已讀
    fun setupUnread(
        roomId: String,
        unreadNum: Int
    ) = viewModelScope.launch(Dispatchers.IO) {
        when (unreadNum) {
            // 手動設定未讀
            0 -> {
                DBManager.getInstance().setChatRoomListItemUnreadNum(roomId, -1)
                isMessageRead.postValue(true)
            }

            // 從手動設定未讀 便已讀
            -1 -> {
                DBManager.getInstance().setChatRoomListItemUnreadNum(roomId, 0)
                DBManager.getInstance().setChatRoomListItemInteractionTime(roomId)
                isMessageRead.postValue(true)
            }

            // 已讀
            else -> {
                checkTokenValid(chatRepository.messageRead(roomId))?.collect {
                    when (it) {
                        is ApiResult.Success -> {
                            DBManager.getInstance().setChatRoomListItemInteractionTime(roomId)
                            isMessageRead.postValue(true)
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
     * 服務號成員聊天室
     * @param serviceNumberId 服務號 id
     * @param serviceNumberMemberRoomIcon 服務號成員聊天室的 icon
     * */
    fun getServiceNumberMemberRoom(
        serviceNumberId: String,
        serviceNumberMemberRoomIcon: Int
    ) = viewModelScope.launch(Dispatchers.IO) {
        val serviceMemberEntity =
            ServiceNumberReference.findServiceMemberRoomIdById(serviceNumberId)
        serviceMemberEntity?.let {
            val smallRoomData =
                SmallRoomData(
                    roomType = it.type,
                    roomId = it.id,
                    avatarId = serviceNumberMemberRoomIcon.toString(),
                    sort = 1,
                    unReadNum = serviceMemberEntity.unReadNum
                )
            if (!isConsultListAlreadyHasData(smallRoomData)) {
                withContext(Dispatchers.Main) {
                    _consultList.add(smallRoomData)
                }
                consultList.postValue(_consultList.sortedBy { it.sort }.toMutableList())
            }
        }
    }

    /**
     * 新增諮詢聊天室
     * @param consultRoomId 諮詢的聊天室 id
     * */
    fun addConsultRoom(consultRoomId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            val serviceNum = DBManager.getInstance().queryServiceNumberByConsultRoomId(consultRoomId)
            serviceNum?.let {
                val smallRoomData =
                    SmallRoomData(
                        roomType = ChatRoomType.services,
                        roomId = serviceNum.roomId,
                        avatarId = serviceNum.serviceNumberAvatarId,
                        roomName = serviceNum.name,
                        serviceNumberId = serviceNum.serviceNumberId
                    )
                if (!isConsultListAlreadyHasData(smallRoomData)) {
                    withContext(Dispatchers.Main) {
                        _consultList.add(smallRoomData)
                    }
                    consultList.postValue(_consultList.sortedBy { it.sort }.toMutableList())
                }
            }
        }

    fun addAiConsultRoom(
        consultId: String,
        serviceRoomId: String,
        serviceNumberId: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        val isExist = _consultList.any { it.roomId == consultId }
        if (isExist) return@launch
        consultRoomId.postValue(Pair(true, consultId))
        val smallRoomData =
            SmallRoomData(
                roomType = ChatRoomType.consultAi,
                roomId = consultId,
                serviceNumberRoomId = serviceRoomId,
                serviceNumberId = serviceNumberId,
                sort = 3
            )
        if (!isConsultListAlreadyHasData(smallRoomData)) {
            withContext(Dispatchers.Main) {
                _consultList.add(smallRoomData)
            }
            consultList.postValue(_consultList.sortedBy { it.sort }.toMutableList())
        }
    }

    /**
     * 設置左下諮詢清單
     * @param aiConsult Ai 諮詢清單
     * @param activeConsultRoom 一般服務號諮詢清單
     * @param roomId 服務號聊天室 id -> 給 AI 諮詢使用
     * @param serviceNumberId 服務號 id -> 給 AI 諮詢使用
     * */
    fun setConsultRoom(
        aiConsult: List<ServiceNumberChatroomAgentServicedRequest.ConsultArray>?,
        activeConsultRoom: List<ActiveServiceConsultArray>?,
        roomId: String,
        serviceNumberId: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        aiConsult?.let {
            it.forEach { aiConsultData ->
                if (_consultList.any { it.roomId == aiConsultData.consultId }) return@forEach
                val smallRoomData =
                    SmallRoomData(
                        roomType = ChatRoomType.consultAi,
                        roomId = aiConsultData.consultId,
                        serviceNumberRoomId = roomId,
                        serviceNumberId = serviceNumberId
                    )
                if (!isConsultListAlreadyHasData(smallRoomData)) {
                    withContext(Dispatchers.Main) {
                        _consultList.add(smallRoomData)
                    }
                }
            }
            if (it.isEmpty()) {
                _consultList = _consultList.filterNot { it.roomType == ChatRoomType.consultAi }.toMutableSet()
            }
        }

        activeConsultRoom?.let {
            it.forEach { consultRoomData ->
                if (_consultList.any { it.roomId == consultRoomData.consultRoomId }) return@forEach
                val smallRoomData =
                    SmallRoomData(
                        roomType = ChatRoomType.services,
                        roomId = consultRoomData.consultRoomId,
                        serviceNumberId = consultRoomData.consultServiceNumberId,
                        avatarId = consultRoomData.serviceNumberAvatarId ?: ""
                    )
                if (!isConsultListAlreadyHasData(smallRoomData)) {
                    withContext(Dispatchers.Main) {
                        _consultList.add(smallRoomData)
                    }
                }
            }
        }
        consultList.postValue(_consultList.sortedBy { it.sort }.toMutableList())
    }

    /**
     * 移除諮詢聊天室
     * @param consultRoomId 需要被移除的 room id
     * */
    fun removeConsultRoom(consultRoomId: String) =
        viewModelScope.launch(Dispatchers.Main) {
            _consultList.removeIf { it.roomId == consultRoomId }
            consultList.postValue(_consultList.sortedBy { it.sort }.toMutableList())
        }

    /**
     * 清除諮詢聊天室
     * */
    fun clearConsultRoom() =
        viewModelScope.launch(Dispatchers.Main) {
            _consultList.removeIf { it.roomType != ChatRoomType.serviceMember }
            consultList.postValue(_consultList.toMutableList())
        }

    private suspend fun isConsultListAlreadyHasData(smallRoomData: SmallRoomData): Boolean =
        withContext(Dispatchers.IO) {
            _consultList.forEach {
                if (it.roomId == smallRoomData.roomId) return@withContext true
            }
            return@withContext false
        }

    /**
     * 左下諮詢列表要出現已過期的記事
     * */
    fun getConsultTodoList(
        roomType: ChatRoomType,
        roomId: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        val selfRoomId = UserPref.getInstance(application).personRoomId
        val todoEntities =
            if (selfRoomId == roomId) {
                TodoReference.findBySelf(null, roomId)
            } else {
                TodoReference.findByRoomId(null, roomId)
            }
        withContext(Dispatchers.Main) {
            _consultList.removeIf { it.sort == 0 || it.todoList.isNotEmpty() }
        }
        val filterTodoList = todoEntities.filter { it.remindTime > 0 && it.remindTime < System.currentTimeMillis() && it.status == TodoStatus.PROGRESS }
        if (filterTodoList.isNotEmpty()) {
            val smallRoomData =
                SmallRoomData(
                    roomType,
                    roomId,
                    todoList = filterTodoList,
                    unReadNum = filterTodoList.size,
                    sort = 0
                )
            if (!isConsultListAlreadyHasData(smallRoomData)) {
                withContext(Dispatchers.Main) {
                    _consultList.add(smallRoomData)
                }
            }
        }
        val result =
            _consultList
                .takeIf { it.isNotEmpty() }
                ?.sortedBy { it.sort }
                ?.toMutableList()
                ?: mutableListOf()

        consultList.postValue(result)
    }

    /**
     * 開始諮詢
     * @param srcRoomId 當前服務號room ID
     * @param consultRoomId 選擇到的諮詢 room id
     * */
    fun startConsult(
        srcRoomId: String,
        consultRoomId: String
    ) = viewModelScope.launch {
        val filterConsultList = _consultList.filter { it.roomId == consultRoomId }
        if (filterConsultList.isNotEmpty()) {
            this@ChatViewModel.consultRoomId.postValue(Pair(false, consultRoomId))
        } else {
            checkTokenValid(chatRepository.startConsult(srcRoomId, consultRoomId))?.collect {
                when (it) {
                    is ApiResult.Success -> {
                        this@ChatViewModel.consultRoomId.postValue(Pair(false, consultRoomId))
                    }

                    is ApiResult.Failure -> {
                        startConsultError.postValue(it.errorMessage)
                    }

                    else -> {
                        // nothing
                    }
                }
            }
        }
    }

    /**
     * 取得聊天室成員資料以拼接AT訊息
     **/
    fun getChatMemberList(chatRoomEntity: ChatRoomEntity?) =
        viewModelScope.launch(Dispatchers.IO) {
            val chatRoomMemberList: MutableList<UserProfileEntity> = Lists.newArrayList()
            chatRoomEntity?.let {
                it.chatRoomMember?.let { list ->
                    list.forEach { chaRoomMember ->
                        val userProfileEntity = DBManager.getInstance().queryUser(chaRoomMember.memberId)
                        userProfileEntity?.let { user ->
                            chatRoomMemberList.add(user)
                        }
                    }
                    memberList.postValue(chatRoomMemberList)
                } ?: run {
                    it.memberIds?.let { list ->
                        list.forEach { id ->
                            val userProfileEntity = DBManager.getInstance().queryUser(id)
                            userProfileEntity?.let { user ->
                                chatRoomMemberList.add(user)
                            }
                        }
                        memberList.postValue(chatRoomMemberList)
                    }
                }
            }
        }

    fun getChatMemberFromService() =
        viewModelScope.launch(Dispatchers.IO) {
            roomEntity?.let { room ->
                chatRepository.getChatMember(room.id).collect {
                    when (it) {
                        is ApiResult.Success -> {
                            room.chatRoomMember = it.data
                            ChatRoomReference.getInstance().updateChatRoomMember(room.id, it.data)
                            EventBus.getDefault().post(EventMsg<Any>(MsgConstant.REFRESH_ROOM_BY_LOCAL))
                        }

                        is ApiResult.Failure -> {
                            when (it.errorMessage.errorCode) {
                                ErrorCode.ChatMemberInvalid.type, ErrorCode.RoomNotExist.type -> {
                                    disableChatRoom(room.id)
                                    errorMessage.postValue(it.errorMessage.errorMessage)
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

    fun disableChatRoom(roomId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            val isDeleted = DBManager.getInstance().deleteRoomListItem(roomId)
            if (isDeleted) {
                EventBus.getDefault().post(EventMsg<Any>(MsgConstant.REFRESH_ROOM_BY_LOCAL))
            }
        }

    fun getChatRoomEntity(roomId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            chatRepository
                .getChatRoomEntity(roomId)
                .onEach { entity ->
                    entity?.let {
                        sendUpdatedRoomTitle.emit(it.name)
                    }
                }.collect()
        }

    fun getSearchMessageList(
        data: List<MessageEntity>,
        isLoadMore: Boolean
    ): List<Any> {
        val searchMessageList: MutableList<Any> = mutableListOf()
        if (isLoadMore) {
            searchMessageList.addAll(data)
            if (data.size > 1) {
                searchMessageList.add(
                    LoadMoreEntity(name = "LoadMore")
                )
            }
            return searchMessageList
        } else {
            return data
        }
    }

    private fun checkMemberDisabled(entity: MessageEntity): MessageEntity {
        val user = DBManager.getInstance().queryFriend(entity.senderId)
        if (user != null) {
            entity.enable =
                if (user.status == User.Status.DISABLE) EnableType.N else EnableType.Y
        }
        return entity
    }

    fun filterMessageFromLocalDB(
        keyWord: String,
        data: List<MessageEntity>
    ): List<MessageEntity> {
        val searchResult: MutableList<MessageEntity> = Lists.newArrayList()
        for (entity in data) {
            if (filterByKeyWord(entity, keyWord)) {
                searchResult.add(
                    checkMemberDisabled(entity)
                )
            }
        }
        return searchResult
    }

    fun loadMoreBySearch(
        keyWord: String,
        data: List<MessageEntity>
    ) {
        val searchResult: MutableList<MessageEntity> = Lists.newArrayList()
        for (entity in data) if (filterByKeyWord(entity, keyWord)) searchResult.add(entity)
        sendShowLoadMoreMsg.postValue(searchResult)
    }

    private fun filterByKeyWord(
        entity: MessageEntity,
        keyWord: String
    ): Boolean =
        entity.getContent(entity.content).contains(keyWord, true) &&
            (entity.type == MessageType.TEXT || entity.type == MessageType.AT) &&
            entity.flag != MessageFlag.RETRACT &&
            entity.sourceType != SourceType.SYSTEM

    private fun getMembersFromChatRoomMember(chatRoomMember: List<ChatRoomMemberResponse>): List<UserProfileEntity> = DBManager.getInstance().queryMembersFromUser(chatRoomMember.map { it.memberId })

    fun clearIsAtMeFlag() =
        viewModelScope.launch(Dispatchers.IO) {
            roomEntity?.let {
                chatRepository.clearIsAtMeFlag(it.id).collect {}
                chatRepository.sendAtMessageRead(it.id).collect {}
            }
        }

    fun getLastChannelFrom(roomId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            checkTokenValid(chatRepository.getFromAppoint(roomId))?.collect {
                when (it) {
                    is ApiResult.Success -> {
                        val fromAppointResponse = it.data
                        fromAppointResponse.lastFrom?.let {
                            channelType = it
                            checkIsMetaAndOverTime(fromAppointResponse)
                        }
                    }

                    is ApiResult.Failure -> {
                        Log.e("chatViewModel", "getLastFrom failure")
                    }

                    else -> {}
                }
            } ?: run {
                Log.e("chatViewModel", "getLastFrom failure")
            }
        }

    /**
     * 發送電子名片
     */
    fun doSendBusinessCard(roomId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            if (channelType.isNotEmpty()) {
                checkTokenValid(chatRepository.doSendBusinessCard(roomId, channelType))?.collect {
                    when (it) {
                        is ApiResult.Success -> {
                            sendToast.emit(if (it.data) R.string.text_send_business_card_successful else R.string.text_send_business_card_failure)
                            hideLoadingDialog.postValue(true)
                        }

                        is ApiResult.Failure -> {
                            sendToast.emit(R.string.text_send_business_card_failure)
                            hideLoadingDialog.postValue(true)
                        }

                        else -> {}
                    }
                } ?: run {
                    sendToast.emit(R.string.text_send_business_card_failure)
                    hideLoadingDialog.postValue(true)
                }
            } else {
                sendToast.emit(R.string.text_send_business_card_failure)
                hideLoadingDialog.postValue(true)
            }
        }

    /**
     * 發送企業會員名片邀請
     */
    fun doSendBusinessMemberCard(roomId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            if (channelType.isNotEmpty()) {
                if (channelType.lowercase() != "line") { // 當前僅提供 LINE 渠道發送
                    sendToast.emit(R.string.text_only_provide_line_channel_invite)
                    hideLoadingDialog.postValue(true)
                } else {
                    checkTokenValid(chatRepository.doSendBusinessMemberCard(roomId, channelType))?.collect {
                        when (it) {
                            is ApiResult.Success -> {
                                when (it.data) {
                                    is Boolean -> {
                                        if (it.data as Boolean) {
                                            sendToast.emit(R.string.text_send_success)
                                            sendCloseExtraLayout.postValue(true)
                                        } else {
                                            sendToast.emit(R.string.text_send_failure)
                                            hideLoadingDialog.postValue(true)
                                        }
                                    }

                                    is Header -> {
                                        (it.data as Header).let { header ->
                                            header.errorMessage?.let { msg ->
                                                sendToastByWord.emit(msg)
                                                hideLoadingDialog.postValue(true)
                                            }
                                        }
                                    }

                                    else -> {}
                                }
                            }

                            is ApiResult.Failure -> {
                                sendToast.emit(R.string.text_send_failure)
                                hideLoadingDialog.postValue(true)
                            }

                            else -> {}
                        }
                    } ?: run {
                        sendToast.emit(R.string.text_send_failure)
                        hideLoadingDialog.postValue(true)
                    }
                }
            } else {
                sendToast.emit(R.string.text_send_failure)
                hideLoadingDialog.postValue(true)
            }
        }

    /**
     * 取得服務號電子名片設定
     * 對外服務號聊天室, 且server有設置電子名片才顯示發送電子名片功能
     */
    fun getBusinessCardInfo(id: String) =
        viewModelScope.launch(Dispatchers.IO) {
            checkTokenValid(serviceNumberAgentsManageRepository.getServiceNumberItem(id))?.collect {
                when (it) {
                    is ApiResult.Success -> {
                        it.data.businessCardInfo?.let { card ->
                            // if (card.cardUrl.isNotEmpty() && card.imageCardUrl.isNotEmpty() && card.shareLiffUrl.isNotEmpty()) {
                            isSettingBusinessCardInfo.value = true
                            // }
                        } ?: run {
                            isSettingBusinessCardInfo.value = false
                        }
                    }

                    is ApiResult.Failure -> {
                        Log.e("chatViewModel", "getBusinessCardInfo failure ${it.errorMessage}")
                    }

                    else -> {}
                }
            } ?: run {
                isSettingBusinessCardInfo.value = false
            }
        }

    fun updateContactPersonChatRoomTitle(
        entity: ChatRoomEntity?,
        id: String
    ): String? =
        entity?.let {
            entity.chatRoomMember?.let { members ->
                members.firstOrNull { it.memberId == id }.let { member ->
                    val user = DBManager.getInstance().queryFriend(member?.memberId)
                    user?.let {
                        user.alias?.ifEmpty { user.nickName?.ifEmpty { user.name?.ifEmpty { entity.name } } }
                    } ?: run {
                        entity.name
                    }
                }
            } ?: run {
                entity.memberIds?.let { memberIds ->
                    memberIds.firstOrNull { it == id }.let {
                        val user = DBManager.getInstance().queryFriend(id)
                        user?.let {
                            user.alias?.ifEmpty { user.nickName?.ifEmpty { user.name?.ifEmpty { entity.name } } }
                        } ?: run {
                            entity.name
                        }
                    }
                }
            }
        }

    fun getFriendAliasName(entity: ChatRoomEntity?): String =
        entity?.let {
            it.chatRoomMember?.let { members ->
                members.firstOrNull { id -> id.memberId != selfUserId }.let { member ->
                    val user = DBManager.getInstance().queryFriend(member?.memberId)
                    user?.let {
                        user.nickName
                    } ?: run {
                        it.memberIds?.let { memberIds ->
                            memberIds.firstOrNull { id -> id != selfUserId }.let { friendId ->
                                val user1 = DBManager.getInstance().queryFriend(friendId)
                                user1?.let {
                                    user1.nickName
                                } ?: entity.name
                            }
                        } ?: entity.name
                    }
                } ?: entity.name
            } ?: run {
                if (it.memberIds != null && it.memberIds.size > 0) {
                    it.memberIds.find { it != selfUserId }?.let {
                        val user1 = DBManager.getInstance().queryFriend(it)
                        user1?.let {
                            user1.nickName
                        } ?: entity.name
                    }
                } else if (it.members != null && it.members.size > 0) {
                    it.members.find { it.id != selfUserId }?.let {
                        it.nickName
                    }
                } else {
                    entity.name
                }
            }
        } ?: ""

    fun setKeyWordByMessage(
        keyWord: String,
        messageEntity: MessageEntity
    ) = viewModelScope.launch(Dispatchers.IO) {
        isObserverKeyboard = false
        findMessageFromByLocalDataBaseASC(messageEntity)
        loadMoreMsgDB()
        CoroutineScope(Dispatchers.IO).launch {
            delay(500)
            isObserverKeyboard = true
        }
        sendSearchKeyWord.emit(keyWord)
    }

    fun loadMoreMsgDB() {
        roomEntity?.let {
            if (loadMoreMessages.isEmpty()) {
                val lastMsgTime: Long = if (mainMessageData.isNotEmpty()) mainMessageData[mainMessageData.size - 1].sendTime else 0
                val messages =
                    MessageReference.findByRoomIdAndTypeAndSortAndLimitAndInnerJoniAliasName(
                        it.id,
                        lastMsgTime,
                        it.type,
                        MessageReference.Sort.ASC,
                        20
                    )
                loadMoreMessages.addAll(messages)
            } else {
                val time: Long = loadMoreMessages[loadMoreMessages.size - 1].sendTime
                val messages =
                    MessageReference.findByRoomIdAndTypeAndSortAndLimitAndInnerJoniAliasName(
                        it.id,
                        time,
                        it.type,
                        MessageReference.Sort.ASC,
                        20
                    )
                loadMoreMessages.clear()
                loadMoreMessages.addAll(messages)
            }
        }
    }

    private fun findMessageFromByLocalDataBaseASC(messageEntity: MessageEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            roomEntity?.let {
                val messages =
                    MessageReference.findByRoomIdAndTypeAndSortAndLimitAndInnerJoniAliasName(
                        it.id,
                        messageEntity.sendTime,
                        it.type,
                        MessageReference.Sort.ASC,
                        20
                    )
                messages.add(messageEntity)
                messages.sort()
                messages.removeIf { it.flag == MessageFlag.DELETED }
                verifyMessageReadingState(messages)
                displayLocalDataBaseMessages(messages, it)
                sendScrollToTop.emit(Unit)
            }
        }

    private fun verifyMessageReadingState(messageList: MutableList<MessageEntity>) =
        viewModelScope.launch(Dispatchers.IO) {
            roomEntity?.let {
                ChatMessageService.verifyMessageReadingState(
                    application,
                    it,
                    messageList,
                    object : ServiceCallBack<List<MessageEntity>, Enum<*>?> {
                        override fun error(message: String?) {
                        }

                        override fun complete(
                            t: List<MessageEntity>,
                            e: Enum<*>?
                        ) {
                            CoroutineScope(Dispatchers.IO).launch {
                                for (entity in t) {
                                    displayMainMessageLogic(isNew = false, scrollToBottom = false, entity)
                                }
                            }
                        }
                    }
                )
            }
        }

    fun displayMainMessageLogic(
        isNew: Boolean,
        scrollToBottom: Boolean,
        message: MessageEntity
    ) = viewModelScope.launch(Dispatchers.Main) {
        if (message.flag == MessageFlag.DELETED) return@launch
        roomEntity?.let {
            if (mainMessageData.contains(message)) {
                mainMessageDataFlow.emit(Pair(false, mainMessageData.toMutableList()))
                sendNotifyChange.emit(message)
                return@launch
            }

            val formattedDate = formatDateSafely(message.sendTime)
            if (currentDate != null) {
                currentDate =
                    if (mainMessageData.isEmpty()) {
                        formatDateSafely(System.currentTimeMillis())
                    } else {
                        formatDateSafely(mainMessageData.last().sendTime)
                    }
            }
            if (formattedDate != currentDate || mainMessageData.size == 0) {
                currentDate = formattedDate
                val dayBegin = TimeUtil.getDayBegin(message.sendTime)

                val timeMessage =
                    MessageEntity
                        .Builder()
                        .id(Tools.generateTimeMessageId(dayBegin))
                        .sendTime(dayBegin)
                        .roomId(it.id)
                        .status(MessageStatus.SUCCESS)
                        .sourceType(SourceType.SYSTEM)
                        .content(UndefContent("TIME_LINE").toStringContent())
                        .sendTime(dayBegin)
                        .build()
                if (!mainMessageData.contains(timeMessage)) {
                    mainMessageData.add(timeMessage)
                    sendBindingDataHandled.emit(Triple(false, scrollToBottom, timeMessage))
                }
            }
            doesNotExist = false
            if (mainMessageData.contains(message)) {
                val index = mainMessageData.indexOf(message)
                mainMessageData.removeAt(index)
            } else {
                doesNotExist = true
            }
            mainMessageData.remove(message)

            // sync the nickname of sender when changed
            if (selfUserId != message.senderId && isNew) {
                checkAndSyncSenderNicknameForNewMsg(message, it.type == ChatRoomType.friend)
            }

            // 将顯示在聊天室的回复消息同步到回复列表中
            if (!Strings.isNullOrEmpty(message.themeId) || !Strings.isNullOrEmpty(message.nearMessageId)) {
                sendThemeMRVData.emit(message)
            }
            mainMessageData.add(message)
            mainMessageDataFlow.emit(Pair(false, mainMessageData.toMutableList()))
            sendBindingDataHandled.emit(Triple(isNew, scrollToBottom, message))
        }
    }

    fun checkAndSyncSenderNicknameForNewMsg(
        msg: MessageEntity?,
        isFriend: Boolean
    ) = viewModelScope.launch(Dispatchers.IO) {
        // check nickName
        msg?.let { message ->
            var senderNickNameInList: String? = "" // get the last one
            if (message.senderId != null) {
                for (i in mainMessageData.size - 1 downTo 1) {
                    if (mainMessageData[i].senderId != null) {
                        if (mainMessageData[i].senderId == message.senderId) {
                            senderNickNameInList = mainMessageData[i].senderName
                            break
                        }
                    }
                }
                if (message.senderName != null) {
                    if (senderNickNameInList != message.senderName) {
                        // update the list to correct nickname
                        if (isFriend) {
                            sendChatRoomTitleChangeListener.emit(message)
                        }
                        for (i in mainMessageData.size - 1 downTo 1) {
                            if (mainMessageData[i].senderId == null) {
                                // some senderId may be null, do not need to handle it
                                continue
                            }
                            if (mainMessageData[i].senderId == message.senderId) {
                                mainMessageData[i].senderName =
                                    message.senderName
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 在訊息中插入時間標籤訊息
     * @param messageList 要顯示的訊息 List
     * */
    private suspend fun setTimeTag(messageList: MutableList<MessageEntity>): MutableList<MessageEntity> =
        withContext(Dispatchers.IO) {
            val copy = messageList.toMutableList()
            copy.sort()
            var currentDate = ""
            val dateMessageList = mutableListOf<MessageEntity>()
            roomEntity?.let {
                copy.forEach { message ->
                    if (message.flag == MessageFlag.DELETED) return@forEach
                    val sendDate = formatDateSafely(message.sendTime)
                    val messageDate = getDateLabelMessage(message.sendTime, it.id)
                    val isMessageListAlreadyAddDateLabel = dateMessageList.contains(messageDate) || copy.contains(messageDate)

                    if (sendDate != currentDate && !isMessageListAlreadyAddDateLabel) {
                        currentDate = sendDate
                        messageDate?.let { dateMessageList.add(it) }
                    }

                    dateMessageList.add(message)
                }
            }
            return@withContext dateMessageList
        }

    /**
     * 取得時間標籤 MessageEntity
     * @param sendTime 訊息發送的時間
     * @param roomId 原本的聊天室ID
     * */
    private suspend fun getDateLabelMessage(
        sendTime: Long,
        roomId: String?
    ): MessageEntity? =
        withContext(Dispatchers.IO) {
            val dayBegin = TimeUtil.getDayBegin(sendTime)
            return@withContext MessageEntity
                .Builder()
                .id(Tools.generateTimeMessageId(dayBegin))
                .sendTime(dayBegin)
                .roomId(roomId)
                .status(MessageStatus.SUCCESS)
                .sourceType(SourceType.SYSTEM)
                .content(UndefContent("TIME_LINE").toStringContent())
                .sendTime(dayBegin)
                .build()
        }

    fun disPlayMessageFromDatabase() =
        viewModelScope.launch(Dispatchers.IO) {
            val latestMessageSequence = getChatRoomLastMessageSequence()
            val entities = getMessageListFromDb(sequence = latestMessageSequence)
            formatMessageList(true, entities).await()
            scrollListToPosition(isInit = true, isLoadMore = false, entities).await()
        }

    private fun checkIsMetaAndOverTime(fromAppointResponse: FromAppointResponse) =
        viewModelScope.launch(Dispatchers.IO) {
            if (fromAppointResponse.lastFrom != "facebook" && fromAppointResponse.lastFrom != "instagram") {
                sendShowMetaOverTimeView.emit(false)
                return@launch
            }
            if (fromAppointResponse.lastMessageTimeOut) {
                sendShowMetaOverTimeView.emit(true)
            } else if (checkIsOver24Hours(fromAppointResponse.lastMessageTime, fromAppointResponse.fbReplyTimeLimit)) {
                sendShowMetaOverTimeView.emit(true)
            } else {
                startMetaReplyLimitCountDown(fromAppointResponse.lastMessageTime, fromAppointResponse.fbReplyTimeLimit)
                sendShowMetaOverTimeView.emit(false)
            }
        }

    private suspend fun checkIsOver24Hours(
        lastMessageTime: Long,
        fbReplyTimeLimit: Long
    ): Boolean =
        withContext(Dispatchers.IO) {
            if (lastMessageTime == 0L) return@withContext false
            if (fbReplyTimeLimit == 0L) return@withContext false
            val time = System.currentTimeMillis() - lastMessageTime
            val days = TimeUnit.MILLISECONDS.toDays(time)
            return@withContext days >= fbReplyTimeLimit
        }

    private fun startMetaReplyLimitCountDown(
        lastMessageTime: Long,
        fbReplyTimeLimit: Long
    ) {
        if (lastMessageTime == 0L) return
        val replyLimitTime = (fbReplyTimeLimit * 24 * 60 * 60 - 60) * 1000
        val limitTime = (replyLimitTime + lastMessageTime) - System.currentTimeMillis()
        metaReplyLimitTimer.start(limitTime)
    }

    fun checkFacebookReplyType(messageList: List<MessageEntity>) {
        roomEntity?.let {
            for (message in messageList) {
                if (!Strings.isNullOrEmpty(message.tag)) {
                    val facebookTag =
                        JsonHelper.getInstance().from(
                            message.tag,
                            FacebookTag::class.java
                        )
                    if (facebookTag == null) continue
                    if (facebookTag.data == null) continue
                    if (facebookTag.data.replyType == null) continue
                    if (facebookTag.data.replyType.isEmpty()) continue
                    val roomMessageList = MessageReference.findByRoomId(it.id)
                    for (roomMessage in roomMessageList) {
                        if (roomMessage.tag == null) continue
                        val roomFacebookTag =
                            JsonHelper.getInstance().from(
                                roomMessage.tag,
                                FacebookTag::class.java
                            )
                        if (roomFacebookTag == null) continue
                        if (roomFacebookTag.data == null) continue
                        if (roomFacebookTag.data.commentId == null) continue
                        if (roomFacebookTag.data.commentId == facebookTag.data.commentId && (roomFacebookTag.data.replyType == null || roomFacebookTag.data.replyType.isEmpty())) {
                            message.themeId = roomMessage.id
                            message.nearMessageId = roomMessage.id
                            message.nearMessageType = roomMessage.type
                            message.nearMessageAvatarId = roomMessage.avatarId
                            message.nearMessageContent = roomMessage.content
                            message.nearMessageSenderId = roomMessage.senderId
                            message.nearMessageSenderName = roomMessage.senderName
                            MessageReference.updateNearMessage(message)
                        }
                    }
                }
            }
        }
    }

    private suspend fun getUnReadMessageTag(message: MessageEntity): MessageEntity? =
        withContext(Dispatchers.IO) {
            roomEntity?.let {
                val unreadMsg =
                    MessageEntity
                        .Builder()
                        .id("以下為未讀訊息")
                        .roomId(it.id)
                        .content(UndefContent("UNREAD").toStringContent())
                        .status(MessageStatus.SUCCESS)
                        .sourceType(SourceType.SYSTEM)
                        .senderId(selfUserId)
                        .sendTime(message.sendTime - 1L)
                        .build()
                it.unReadNum = 0
                return@withContext unreadMsg
            } ?: return@withContext null
        }

    private fun setUnReadMessage(messageEntity: MessageEntity) {
        roomEntity?.let {
            if (messageEntity.id == unreadMessageId) {
                val unreadMsg =
                    MessageEntity
                        .Builder()
                        .id("以下為未讀訊息")
                        .roomId(it.id)
                        .content(UndefContent("UNREAD").toStringContent())
                        .status(MessageStatus.SUCCESS)
                        .sourceType(SourceType.SYSTEM)
                        .senderId(selfUserId)
                        .sendTime(System.currentTimeMillis())
                        .build()
                unreadMsg.sendTime = messageEntity.sendTime - 1L
                displayMainMessageLogic(isNew = false, scrollToBottom = true, unreadMsg)
                unreadMessageId = ""
            }
            it.unReadNum = 0
        }
    }

    private fun displayLocalDataBaseMessages(
        messages: List<MessageEntity>,
        sessionEntity: ChatRoomEntity
    ) = viewModelScope.launch(Dispatchers.IO) {
        roomEntity?.let {
            var hasUnreadLine = false
            val failMessageIds = Lists.newLinkedList<String?>()
            for (msg in messages) {
                // 如果有失敗訊息，往最下面排序
                if (MessageStatus.FAILED_or_ERROR.contains(msg.status)) {
                    msg.sendTime = System.currentTimeMillis()
                    failMessageIds.add(msg.id)
                }

                val senderId = msg.senderId
                if (selfUserId == senderId) {
                    if (msg.sendNum!! > 0) {
                        msg.status = MessageStatus.SUCCESS
                    }
                    if (msg.receivedNum!! > 0) {
                        msg.status = MessageStatus.RECEIVED
                    }
                    if (msg.readedNum!! > 0) {
                        msg.status = MessageStatus.READ
                    }
                }

                if (msg.id == unreadMessageId) {
                    if (!hasUnreadLine) {
                        hasUnreadLine = true
                        val unreadMsg =
                            MessageEntity
                                .Builder()
                                .id("以下為未讀訊息")
                                .roomId(it.id)
                                .content(UndefContent("UNREAD").toStringContent())
                                .status(MessageStatus.SUCCESS)
                                .sourceType(SourceType.SYSTEM)
                                .senderId(selfUserId)
                                .sendTime(System.currentTimeMillis())
                                .build()
                        unreadMsg.sendTime = msg.sendTime - 1L
                        displayMainMessageLogic(isNew = false, scrollToBottom = true, unreadMsg)
                        unreadMessageId = ""
                    }
                }

                displayMainMessageLogic(isNew = false, scrollToBottom = true, msg)
            }
            sessionEntity.unReadNum = 0
        }
    }

    /**
     * 用 sequence 拿取訊息
     * @param isInit 是否是第一次拿取訊息 -> 指的是第一次20筆
     * @param isLoadMore 是否是讀取更多
     * @param sequence 要找小於此 sequence 的訊息
     * @param pageSize 需要取得的數量
     * */
    private fun getChatMessageListFromSequence(
        isInit: Boolean = false,
        isLoadMore: Boolean = false,
        sequence: Int,
        pageSize: Int = 20
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            roomEntity?.let { chatRoomEntity ->
                chatRepository.syncMessage(chatRoomEntity.id, sequence, pageSize = pageSize).collect {
                    when (it) {
                        is ApiResult.Success -> {
                            checkMessageSequenceIsConsecutive(isInit, isLoadMore, sequence, pageSize)
                        }

                        is ApiResult.Failure -> {
                            CoroutineScope(Dispatchers.IO).launch {
                                sendShowIsNotMemberMessage.emit(it.errorMessage.errorMessage)
                                sendHighLightUnReadLine.emit(unreadNum > 0)
                                unreadNum = -1
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
     * 整理訊息 並清理重複的訊息
     * @param messageListFromDb 訊息列表
     * */
    private fun formatMessageList(
        isInit: Boolean,
        messageListFromDb: MutableList<MessageEntity>
    ) = CoroutineScope(Dispatchers.IO).async {
        messageListFromDb.removeIf { it.flag == MessageFlag.DELETED }
        setMessageSenderInfo(messageListFromDb).await()
        val originList = mainMessageData.toMutableList()
        // 設置時間標籤
        val dateMessageList = setTimeTag(messageListFromDb)
        originList.addAll(dateMessageList)

        // 未讀
        if (originList.isNotEmpty() && !isInit) {
            val unreadIndex =
                originList.indexOfFirst {
                    it.sourceType != SourceType.SYSTEM &&
                        it.readedNum == 0 &&
                        (it.flag == MessageFlag.SEND || it.flag == MessageFlag.ARRIVED)
                }

            if (unreadIndex != -1) {
                val unreadTag = getUnReadMessageTag(originList[unreadIndex])
                if (!originList.contains(unreadTag)) {
                    unreadTag?.let {
                        originList.add(unreadIndex, it)
                    }
                }
            }
        }

        // 去除重複
        val originListSet = originList.toSet()
        mainMessageData.clear()
        mainMessageData.addAll(originListSet)
        mainMessageData.sort()
        mainMessageDataFlow.emit(Pair(isInit, mainMessageData.toMutableList()))
    }

    private var isAnonymousMode = false
    private val userMap = mutableMapOf<String, UserProfileEntity?>()

    private fun setMessageSenderInfo(messageList: List<MessageEntity>) =
        CoroutineScope(Dispatchers.IO).async {
            roomEntity?.let { room ->
                val chatOwner = DBManager.getInstance().queryUser(room.ownerId)
                messageList.forEach {
                    val senderUserProfile =
                        if (!userMap.contains(it.senderId)) {
                            it.senderId?.let {
                                val userProfile = DBManager.getInstance().queryFriend(it)
                                userMap[it] = userProfile
                                userProfile
                            }
                        } else {
                            it.senderId?.let {
                                userMap[it]
                            }
                        }
                    val isRightMessage = isRightMessage(it)
                    val isSelfSent = isSelfSent(it)
                    when {
                        isRightMessage && it.tag != null && it.tag!!.isNotEmpty() -> {
                            val facebookTag = JsonHelper.getInstance().from(it.tag, FacebookTag::class.java)
                            if (facebookTag != null && facebookTag.data != null && facebookTag.data.replyType != null) {
                                var senderName =
                                    if (facebookTag.data.replyType == "public") {
                                        application.getString(R.string.facebook_public_replied)
                                    } else {
                                        application.getString(R.string.facebook_private_replied)
                                    }
                                if (!isSelfSent) {
                                    senderUserProfile?.let {
                                        senderName += " by ${it.nickName}"
                                    }
                                }
                                it.senderName = senderName
                            }
                        }

                        //  若打開匿名模式且是對方訊息
                        isAnonymousMode && !isRightMessage -> {
                            val domino =
                                getDomino(it.senderId)?.let {
                                    MessageDomino.DominoName.valueOf(it.name).getName()
                                } ?: ""
                            it.senderName = domino
                        }

                        room.ownerId == it.senderId &&
                            room.serviceNumberType != null &&
                            ChatRoomType.services == room.type &&
                            chatOwner != null &&
                            UserType.EMPLOYEE != chatOwner.userType -> {
                            if (!(!isRightMessage && it.from == ChannelType.FB && it.type == MessageType.TEMPLATE)) {
                                senderUserProfile?.let { profile ->
                                    val customerName =
                                        if (!senderUserProfile.alias.isNullOrEmpty()) {
                                            senderUserProfile.alias
                                        } else if (!senderUserProfile.customerName.isNullOrEmpty()) {
                                            senderUserProfile.customerName
                                        } else {
                                            senderUserProfile.nickName
                                        }
                                    it.senderName = customerName
                                    it.avatarId = profile.avatarId
                                }
                            }
                        }

                        // 系統聊天室
                        ChatRoomType.system == room.type -> {
                            if (systemUserId.isNullOrEmpty()) return@forEach
                            val systemUserName =
                                if (!systemUserName.isNullOrEmpty()) {
                                    systemUserName
                                } else {
                                    "Unknown"
                                }
                            val systemUserAvatarId =
                                if (!systemUserAvatar.isNullOrEmpty()) {
                                    systemUserAvatar
                                } else {
                                    it.avatarId
                                }
                            it.avatarId = systemUserAvatarId
                            it.senderName = systemUserName
                        }

                        else -> {
                            senderUserProfile?.let { profile ->
                                it.senderName = profile.nickName
                                it.avatarId = profile.avatarId
                            }
                        }
                    }
                }
            }
        }

    private suspend fun isRightMessage(message: MessageEntity): Boolean =
        withContext(Dispatchers.IO) {
            roomEntity?.let {
                return@withContext if (it.roomType == ChatRoomType.person) {
                    Sets.newHashSet("android", "ios").contains(message.osType)
                } else if (it.roomType == ChatRoomType.consultAi) {
                    selfUserId == message.senderId
                } else if (ChatRoomType.SERVICES_or_SUBSCRIBE.contains(it.roomType) && selfUserId != it.ownerId) {
                    it.ownerId != message.senderId
                } else {
                    !TextUtils.isEmpty(selfUserId) && selfUserId == message.senderId
                }
            } ?: return@withContext false
        }

    private suspend fun isSelfSent(message: MessageEntity): Boolean =
        withContext(Dispatchers.IO) {
            return@withContext message.senderId == selfUserId
        }

    private suspend fun getDomino(userId: String?): MessageDomino? =
        withContext(Dispatchers.IO) {
            try {
                val dominos = MessageDomino.getDomino(MessageDomino.getBatchNumber())
                val dominoData = MessageDomino.getDominoData()
                if (dominoData[userId] == null) {
                    val domino = dominos.pollFirst()
                    MessageDomino.dominoData[userId] =
                        MessageDomino(
                            domino.name,
                            Color.BLACK,
                            domino.resId,
                            RandomHelper.randomColor()
                        )
                }
                return@withContext dominoData[userId]
            } catch (e: Exception) {
                return@withContext null
            }
        }

    /**
     * 滑動 list 到指定位置
     * @param isInit 是否是第一次拿取訊息 -> 指的是第一次20筆
     * @param isLoadMore 是否是讀取更多
     * */
    private fun scrollListToPosition(
        isInit: Boolean,
        isLoadMore: Boolean,
        messageList: List<MessageEntity>
    ) = CoroutineScope(Dispatchers.IO).async {
        val unreadIndex =
            mainMessageData.indexOfFirst {
                it.sourceType != SourceType.SYSTEM &&
                    it.readedNum == 0 &&
                    (it.flag == MessageFlag.SEND || it.flag == MessageFlag.ARRIVED)
            }

        if (!isInit && unreadIndex != -1) {
            withContext(Dispatchers.Main) {
                scrollListToPosition.emit(unreadIndex - 2)
            }
        } else if (isLoadMore) {
            withContext(Dispatchers.Main) {
                scrollListToPosition.emit(mainMessageData.indexOf(messageList.first { it.sequence != 0 }) - 1)
            }
        } else {
            withContext(Dispatchers.Main) {
                scrollListToPosition.emit(mainMessageData.size - 1)
            }
        }
    }

    /**
     * 設置訊息已讀
     * */
    fun setMessageRead(roomId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            chatRepository.messageRead(roomId).collect {
                when (it) {
                    is ApiResult.Success -> {
                        onMessageRead.emit(true)
                    }

                    is ApiResult.Failure -> {
                        onMessageRead.emit(false)
                    }

                    else -> {
                        // nothing
                    }
                }
            }
        }

    fun onAvatarIconClick(senderId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            if (NetworkUtils.isNetworkAvailable(application)) {
                checkTokenValid(contactRepository.getUserItem(senderId))?.collect {
                    when (it) {
                        is ApiResult.Success -> {
                            doAvatarIconIntent(it.data)
                        }

                        else -> {
                            // nothing
                        }
                    }
                }
            } else {
                val user = DBManager.getInstance().queryUser(senderId)
                user?.let {
                    doAvatarIconIntent(it)
                }
            }
        }

    private fun doAvatarIconIntent(userProfile: UserProfileEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            roomEntity?.let { chatRoom ->
                when {
                    chatRoom.type == ChatRoomType.service -> {
                        val aiffInfo = getClientAiffHomePage()
                        aiffInfo?.let {
                            val aiffManager = AiffManager(application, chatRoom.id)
                            aiffManager.showAiffViewByInfo(it)
                        } ?: run {
                            val intent = Intent(application, VisitorHomepageActivity::class.java)
                            intent
                                .putExtra(BundleKey.ACCOUNT_TYPE.key(), userProfile.userType.name)
                                .putExtra(BundleKey.ACCOUNT_ID.key(), userProfile.id)
                                .putExtra(BundleKey.ROOM_ID.key(), userProfile.roomId)
                                .putExtra(BundleKey.USER_NICKNAME.key(), userProfile.nickName)
                            onAvatarIconClickIntent.emit(intent)
                        }
                    }

                    chatRoom.type == ChatRoomType.friend -> {
                        if (UserType.VISITOR == userProfile.userType) {
                            sendToast.emit(R.string.text_no_guest_page)
                        } else {
                            val bundle =
                                bundleOf(
                                    BundleKey.ACCOUNT_TYPE.key() to userProfile.userType.name,
                                    BundleKey.ACCOUNT_ID.key() to userProfile.id
                                )
                            val intent =
                                if (selfUserId == userProfile.id) {
                                    Intent(application, SelfInformationHomepageActivity::class.java)
                                } else {
                                    Intent(application, EmployeeInformationHomepageActivity::class.java)
                                }
                            intent.putExtras(bundle)
                            onAvatarIconClickIntent.emit(intent)
                        }
                    }

                    !userProfile.roomId.isNullOrEmpty() -> {
                        if (NetworkUtils.isNetworkAvailable(application)) {
                            val intent = Intent(application, ChatNormalActivity::class.java)
                            intent.putExtra(BundleKey.EXTRA_SESSION_ID.key(), userProfile.roomId)
                            onAvatarIconClickIntent.emit(intent)
                        } else {
                            // 在無網路情況下，檢查db是否有roomEntity，有才跳轉
                            val room = ChatRoomReference.getInstance().findById2("", userProfile.roomId, true, true, true, true, true)
                            room?.let {
                                val intent = Intent(application, ChatNormalActivity::class.java)
                                intent.putExtra(BundleKey.EXTRA_SESSION_ID.key(), room.id)
                                onAvatarIconClickIntent.emit(intent)
                            }
                        }
                    }

                    else -> {
                        val intent = Intent(application, VisitorHomepageActivity::class.java)
                        intent
                            .putExtra(BundleKey.ACCOUNT_TYPE.key(), userProfile.userType.name)
                            .putExtra(BundleKey.ACCOUNT_ID.key(), userProfile.id)
                            .putExtra(BundleKey.ROOM_ID.key(), userProfile.roomId)
                            .putExtra(BundleKey.USER_NICKNAME.key(), userProfile.nickName)
                        onAvatarIconClickIntent.emit(intent)
                    }
                }
            }
        }

    private suspend fun getClientAiffHomePage(): AiffInfo? =
        withContext(Dispatchers.IO) {
            val aiffDao = AiffDB.getInstance(application).aiffInfoDao
            val aiffInfoList = aiffDao.aiffInfoListByIndex
            if (aiffInfoList != null && aiffInfoList.size > 0) {
                aiffInfoList.forEach {
                    if (it.embedLocation == AiffEmbedLocation.ContactHome.name) {
                        return@withContext aiffDao.getAiffInfo(it.id)
                    }
                }
            }
            return@withContext null
        }

    /**
     * 設置快速回覆
     * */
    private fun setQuickReply() =
        viewModelScope.launch(Dispatchers.IO) {
            if (mainMessageData.isNotEmpty()) {
                val lastMessage = mainMessageData[0]
                try {
                    val listType =
                        object :
                            TypeToken<List<QuickReplyItem?>?>() {
                        }.type
                    val quickReplyItemList =
                        JsonHelper
                            .getInstance()
                            .from<List<QuickReplyItem>>(
                                JSONObject(lastMessage.content)
                                    .optJSONObject("quickReply")
                                    ?.optJSONArray("items")
                                    .toString(),
                                listType
                            )
                    quickReplyItemList?.let {
                        sendSetQuickReply.emit(it)
                    }
                } catch (e: Exception) {
                    CELog.e("Quick Reply Parse Error", e)
                }
            }
        }

    fun onMessageReceived(newMessageIdString: String) =
        viewModelScope.launch(Dispatchers.IO) {
            roomEntity?.let { chatRoomEntity ->
                val newMessageId =
                    JsonHelper.getInstance().fromToList(newMessageIdString, Array<String>::class.java)
                val localEntity =
                    MessageReference.findByIdsAndRoomId(null, newMessageId.toTypedArray<String>(), chatRoomEntity.id)
                localEntity.forEach {
                    it.tag?.let { tag ->
                        val facebookTag =
                            if (tag.isNotEmpty()) {
                                JsonHelper.getInstance().from(it.tag, FacebookTag::class.java)
                            } else {
                                null
                            }

                        facebookTag?.let { facebookTag ->
                            if (facebookTag.data.replyType.isNotEmpty()) {
                                if ("private" == facebookTag.data.replyType) {
                                    val commentId = facebookTag.data.commentId ?: return@forEach
                                    val targetMessageList = mainMessageData.filter { it.tag?.isNotEmpty() == true && it.tag!!.contains(commentId) }
                                    targetMessageList.forEach {
                                        MessageReference.updateFacebookPrivateReplyStatus(chatRoomEntity.id, it.id, true)
                                        it.isFacebookPrivateReplied = true
                                        updateFacebookCommentStats.emit(it)
                                    }
                                }
                            }
                        }
                    }
                    it.flag = MessageFlag.READ
                    if (!mainMessageData.contains(it)) {
                        mainMessageData.add(it)
                    }
                }
                MessageReference.saveByRoomId(chatRoomEntity.id, localEntity)
                val dateMessageList = setTimeTag(mainMessageData)
                mainMessageData.clear()
                mainMessageData.addAll(dateMessageList)
                mainMessageData.sort()
                onMessageReceived.emit(mainMessageData.toMutableList())
            }
        }

    /**
     * 從本地資料庫讀取聊天室訊息
     * */
    fun loadChatMessageListFromDb(isInit: Boolean = true) =
        viewModelScope.launch(Dispatchers.IO) {
            mainMessageData.clear()
            roomEntity?.let {
                val latestMessageSequence = getChatRoomLastMessageSequence()
                val entities = getMessageListFromDb(sequence = latestMessageSequence)
                formatMessageList(isInit, entities).await()
                scrollListToPosition(isInit, false, entities).await()
                delay(500)

                if (entities.isNotEmpty()) {
                    val localMessageSequence = entities.firstOrNull { it.sequence != 0 }?.sequence ?: 0
                    if (latestMessageSequence != localMessageSequence) {
                        getChatMessageListFromSequence(false, sequence = latestMessageSequence, pageSize = 30)
                    } else {
                        val firstMessageSequence =
                            entities.lastOrNull { it.sequence != 0 }?.sequence ?: 0
                        val nextPageData =
                            getMessageListFromDb(sequence = firstMessageSequence, limit = 30)
                        val s =
                            nextPageData.firstOrNull { it.sequence != 0 }?.sequence
                                ?: (firstMessageSequence - 30)
                        if (nextPageData.isNotEmpty()) {
                            checkMessageSequenceIsConsecutive(isInit, false, s, 30)
                        } else {
                            getChatMessageListFromSequence(isInit, sequence = latestMessageSequence)
                        }
                    }
                } else {
                    getChatMessageListFromSequence(isInit, sequence = latestMessageSequence)
                }
            }
        }

    /**
     * 從 ChatRoomEntity 拿取最後訊息 sequence
     * */
    private suspend fun getChatRoomLastMessageSequence(): Int =
        withContext(Dispatchers.IO) {
            val chatRoomEntity = roomEntity ?: return@withContext 0
            val currentLastMessageSequence = DBManager.getInstance().querySequenceFromLastMessage(chatRoomEntity.id)

            val sequence =
                chatRoomEntity.lastSequence.takeIf { it != null && it != 0 }
                    ?: run {
                        val lastMessage =
                            chatRoomEntity.lastMessage?.let {
                                JsonHelper.getInstance().from(it, MessageEntity::class.java)
                            } ?: JsonHelper.getInstance().from(chatRoomEntity.lastMessageStr, MessageEntity::class.java)
                        lastMessage.sequence ?: 0
                    }

            return@withContext maxOf(currentLastMessageSequence, sequence) + chatRoomEntity.unReadNum + 1
        }

    /**
     * 判斷列表中的訊息是否有缺漏
     * */
    private fun checkMessageSequenceIsConsecutive(
        isInit: Boolean,
        isLoadMore: Boolean,
        sequence: Int,
        pageSize: Int,
        nearMessageSequence: Int = -1
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val messageListFromDb = getMessageListFromDb(sequence, pageSize)
            messageListFromDb.sortByDescending { it.sequence }
            if (syncMessageRetryCount >= 5) {
                syncMessageRetryCount = 0
                stopLoadMoreMessage.emit(true)
                showMessageList(messageListFromDb, isInit, isLoadMore)
                return@launch
            }
            try {
                val latestMessageSequence =
                    if (mainMessageData.isNotEmpty()) {
                        mainMessageData.lastOrNull { it.sequence != 0 }?.sequence ?: 0
                    } else {
                        messageListFromDb.firstOrNull { it.sequence != 0 }?.sequence ?: 0
                    }
                val firstMessageSequence =
                    if (getFirstMessageSequence(messageListFromDb, latestMessageSequence) == 0) {
                        latestMessageSequence - 20
                    } else {
                        getFirstMessageSequence(messageListFromDb, latestMessageSequence)
                    }
                if (firstMessageSequence > -1 && latestMessageSequence - 1 != firstMessageSequence && nearMessageSequence == -1) {
                    syncMessageBetweenSequence(isInit, isLoadMore, firstMessageSequence, latestMessageSequence + 1, sequence, pageSize)
                    syncMessageRetryCount++
                } else {
                    showMessageList(messageListFromDb, isInit, isLoadMore)
                    syncMessageRetryCount = 0
                }

                // 判斷是不是點擊有回覆的訊息
                if (nearMessageSequence != -1) {
                    val nearMessage = mainMessageData.find { it.sequence == nearMessageSequence }
                    nearMessage?.let {
                        showMessageList(messageListFromDb, isInit, isLoadMore)
                        onReplyNearMessageClick.emit(nearMessageSequence)
                        return@launch
                    } ?: run {
                        getLoadMoreMessageList(mainMessageData.first { it.sequence != 0 }, nearMessageSequence)
                    }
                }
            } catch (ignored: Exception) {
            }
        }
    }

    private fun showMessageList(
        messageList: MutableList<MessageEntity>,
        isInit: Boolean,
        isLoadMore: Boolean,
        nearMessageSequence: Int = -1
    ) = viewModelScope.launch(Dispatchers.IO) {
        formatMessageList(isInit, messageList).await()
        if (messageList.isEmpty() || isUserScroll) {
            if (isLoadMore) stopLoadMoreMessage.emit(true)
            return@launch
        }

        if (nearMessageSequence != -1) {
            onReplyNearMessageClick.emit(nearMessageSequence)
            return@launch
        }
        scrollListToPosition(isInit, isLoadMore, messageList).await()
        roomEntity?.let { chatRoomEntity ->
            chatRoomEntity.serviceNumberOpenType?.let {
                if (it.contains("O")) {
                    checkFacebookReplyType(messageList)
//                    checkIsFacebookAndOverTime(messageList)
                }
            }

            val firstMessageSequence = mainMessageData.firstOrNull { it.sequence != 0 }?.sequence ?: 0

            // 再次撈取30筆
            if (isInit && mainMessageData.size in 1..49 && firstMessageSequence > 0) {
                getChatMessageListFromSequence(
                    isInit = false,
                    sequence = firstMessageSequence + 1,
                    pageSize = 30
                )
            }

            if (!isInit) {
                setQuickReply()
            }
            stopLoadMoreMessage.emit(true)
        }
    }

    /**
     * 取得列表中缺失的訊息
     * */
    private fun syncMessageBetweenSequence(
        isInit: Boolean,
        isLoadMore: Boolean,
        firstMessageSequence: Int,
        latestMessageSequence: Int,
        sequence: Int,
        pageSize: Int
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            roomEntity?.let {
                chatRepository
                    .syncMessageBetweenSequence(
                        it.id,
                        firstMessageSequence,
                        latestMessageSequence
                    ).collect {
                        when (it) {
                            is ApiResult.Success -> {
                                checkMessageSequenceIsConsecutive(isInit, isLoadMore, sequence, pageSize)
                            }

                            is ApiResult.NextPage -> {
                                syncMessageBetweenSequence(isInit, isLoadMore, firstMessageSequence + 20, latestMessageSequence, sequence, pageSize)
                            }

                            else -> { // nothing
                            }
                        }
                    }
            }
        }
    }

    /**
     * 取得列表中 缺失的前後 sequence
     * */
    private suspend fun getFirstMessageSequence(
        messageList: MutableList<MessageEntity>,
        latestMessageSequence: Int
    ): Int =
        withContext(Dispatchers.IO) {
            var currentFirstMessageSequence = latestMessageSequence
            messageList.sortByDescending { it.sequence }
            messageList.forEach {
                if (it.status == MessageStatus.ERROR) {
                    currentFirstMessageSequence--
                    return@forEach
                }
                when (it.sequence) {
                    currentFirstMessageSequence -> {
                        currentFirstMessageSequence--
                    }

                    currentFirstMessageSequence - 1, currentFirstMessageSequence + 1 -> {
                        currentFirstMessageSequence = it.sequence!! - 1
                    }

                    else -> {
                        return@withContext it.sequence!!
                    }
                }
            }
            return@withContext -1
        }

    /**
     * 取得聊天室最早的一筆訊息的 sequence
     * */
    private suspend fun getLoadMoreSequence(): MessageEntity? =
        withContext(Dispatchers.IO) {
            if (mainMessageData.isNotEmpty()) {
                return@withContext mainMessageData.first { it.sequence != 0 }
            } else {
                return@withContext null
            }
        }

    /**
     * 撈取更多訊息
     * */
    fun loadMoreMessage() =
        CoroutineScope(Dispatchers.IO).launch {
            isUserScroll = false
            if (mainMessageData.isNotEmpty()) {
                val roomFirstMessage = getLoadMoreSequence()
                roomFirstMessage?.let {
                    if (it.sequence != 1) {
                        getLoadMoreMessageList(it)
                    } else {
                        stopLoadMoreMessage.emit(true)
                    }
                }
            } else {
                stopLoadMoreMessage.emit(true)
            }
        }

    /**
     * 撈取更多訊息 從全域搜尋來
     * @param targetMessage 需要找到的訊息
     * */
    fun loadMoreForGlobalSearch(targetMessage: MessageEntity) =
        CoroutineScope(Dispatchers.IO).launch {
            val firstMessage = mainMessageData.firstOrNull { it.sequence != 0 }
            firstMessage?.let {
                getLoadMoreMessageList(it, targetMessage.sequence ?: -1)
            }
        }

    /**
     * 從資料庫orAPI撈取更多訊息
     * @param roomFirstMessage 目前聊天室內第一筆的訊息(最舊的)
     * @param nearMessageId 判斷是否是點擊回覆的訊息
     * */
    fun getLoadMoreMessageList(
        roomFirstMessage: MessageEntity,
        nearMessageSequence: Int = -1
    ) = CoroutineScope(Dispatchers.IO).launch {
        roomEntity?.let {
            val sequence = mainMessageData.filter { it.sequence != 0 }.minByOrNull { it.sequence!! }?.sequence!! - 1
            val loadMoreMessageList = getMessageListFromDb(sequence, 40)
            if (loadMoreMessageList.isNotEmpty() && loadMoreMessageList.size > 1) {
                val firstMessageSequence = getFirstMessageSequence(loadMoreMessageList, sequence)
                if (firstMessageSequence == -1) {
                    showMessageList(loadMoreMessageList, isInit = false, isLoadMore = true, nearMessageSequence)
                } else {
                    syncMessageBetweenSequence(isInit = false, isLoadMore = true, firstMessageSequence, sequence, sequence, sequence - firstMessageSequence)
                }
            } else {
                getChatMessageListFromSequence(isLoadMore = true, sequence = roomFirstMessage.sequence ?: 0, pageSize = 40)
            }
        }
    }

    /**
     * 從 DB 撈取聊天室訊息
     * @param sequence 要找尋小於此 sequence 的訊息
     * @param limit 需要撈取的數量
     * @param sort 排序方式
     * */
    private suspend fun getMessageListFromDb(
        sequence: Int = 0,
        limit: Int = 15,
        sort: MessageReference.Sort = MessageReference.Sort.DESC
    ): MutableList<MessageEntity> =
        withContext(Dispatchers.IO) {
            roomEntity?.let {
                return@withContext MessageReference.findRoomMessageList(it.id, sequence, sort, limit)
            } ?: return@withContext mutableListOf()
        }

    fun loadChatMessageList() =
        viewModelScope.launch(Dispatchers.IO) {
            roomEntity?.let {
                val lastReadMsgId =
                    if (DBManager.getInstance().queryLastReadMsgId(it.id, selfUserId) != null) {
                        DBManager.getInstance().queryLastReadMsgId(it.id, selfUserId)
                    } else {
                        it.lastMessage?.id ?: run {
                            val lastMessage = JsonHelper.getInstance().from(it.lastMessageStr, MessageEntity::class.java)
                            lastMessage?.id
                        }
                    }
                ChatMessageService.getChatMessageEntities(
                    application,
                    it.id,
                    lastReadMsgId,
                    Sort.DESC,
                    object : ServiceCallBack<List<MessageEntity>, RefreshSource> {
                        override fun error(message: String) {
                            CoroutineScope(Dispatchers.IO).launch {
                                sendShowIsNotMemberMessage.emit(message)
                                sendHighLightUnReadLine.emit(unreadNum > 0)
                                unreadNum = -1
                            }
                        }

                        override fun complete(
                            messageEntities: List<MessageEntity>,
                            refreshSource: RefreshSource
                        ) {
                            CoroutineScope(Dispatchers.IO).launch {
                                if (messageEntities.isNotEmpty()) {
                                    CELog.d("Enter the chat room, have data locally, read messages from the server:" + messageEntities.size)
                                    checkFacebookReplyType(messageEntities)
//                            getMessageListFrequency++
//                            if (getMessageListFrequency > 1) {
                                    sendHighLightUnReadLine.emit(unreadNum > 0)
                                    unreadNum = -1
//                            }
                                    val newMsgIds: MutableList<String?> = Lists.newArrayList()
                                    val entities2 =
                                        MessageReference.findByRoomIdAndTypeAndSortAndLimitAndInnerJoniAliasName(
                                            it.id,
                                            -1,
                                            it.type,
                                            MessageReference.Sort.DESC,
                                            50
                                        )
                                    if (entities2.isNotEmpty()) {
                                        for (i in entities2.indices.reversed()) {
                                            val msg = entities2[i]
                                            // As long as it is read, it will not be sent
                                            if (MessageStatus.READ != msg.status && MessageStatus.IS_REMOTE != msg.status) {
                                                newMsgIds.add(msg.id)
                                            }
                                            displayMainMessageLogic(isNew = false, scrollToBottom = true, msg)
                                        }
                                        val message = entities2[0]
                                        // 設置最後訊息如果有 quick reply
                                        try {
                                            val listType =
                                                object :
                                                    TypeToken<List<QuickReplyItem?>?>() {
                                                }.type
                                            val quickReplyItemList =
                                                JsonHelper.getInstance().from<List<QuickReplyItem>>(
                                                    JSONObject(message.content)
                                                        .optJSONObject("quickReply")
                                                        ?.optJSONArray("items")
                                                        .toString(),
                                                    listType
                                                )
                                            quickReplyItemList?.let {
                                                sendSetQuickReply.emit(it)
                                            }
                                        } catch (e: Exception) {
                                            CELog.e("Quick Reply Parse Error", e)
                                        }

                                        it.serviceNumberOpenType?.let {
                                            if (it.contains("O")) {
                                                checkFacebookReplyType(entities2)
                                            }
                                        }
                                        ChatMessageService.doMessageReadAllByRoomId(
                                            application,
                                            it,
                                            it.unReadNum,
                                            newMsgIds,
                                            false
                                        )
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }

    fun sendVideo(iVideoSize: IVideoSize) {
        sendVideo(iVideoSize, false)
    }

    fun sendVideo(
        iVideoSize: IVideoSize,
        isQuote: Boolean
    ) {
        sendVideo(iVideoSize, isQuote, false)
    }

    suspend fun sendVideoStream(iVideoSize: IVideoSize) {
        sendVideoStream(iVideoSize, false)
    }

    suspend fun sendVideoStream(
        iVideoSize: IVideoSize,
        isQuote: Boolean
    ) {
        sendVideoStream(iVideoSize, isQuote, false).await()
    }

    // 確認是否是強制接手
    private fun checkIsSnatch(): Boolean {
        roomEntity?.let {
            return (
                ServiceNumberType.PROFESSIONAL == it.serviceNumberType &&
                    it.serviceNumberAgentId != null &&
                    it.serviceNumberAgentId.isNotEmpty() &&
                    it.serviceNumberAgentId != selfUserId &&
                    it.ownerId != selfUserId &&
                    (!it.provisionalIds.contains(selfUserId) && it.listClassify != ChatRoomSource.MAIN)
            ) &&
                // 臨時成員聊天室
                it.serviceNumberStatus != ServiceNumberStatus.TIME_OUT
        } ?: run {
            return false
        }
    }

    private fun checkIsRobotSnatch(): Boolean {
        roomEntity?.let {
            return ServiceNumberStatus.ROBOT_SERVICE == it.serviceNumberStatus && it.ownerId != selfUserId
        }
        return false
    }

    private fun buildVideoContent(iVideoSize: IVideoSize): VideoContent =
        VideoContent(
            iVideoSize.height(),
            iVideoSize.width(),
            iVideoSize.size(),
            iVideoSize.path(),
            iVideoSize.name(),
            iVideoSize.path(),
            iVideoSize.duration().toDouble(),
            false
        )

    private fun buildVideoMessage(
        messageId: String,
        videoContent: VideoContent
    ): MessageEntity? {
        roomEntity?.let {
            return MessageEntity
                .Builder()
                .id(messageId)
                .avatarId(mSelfAccount.avatarId)
                .roomId(it.id)
                .senderName(mSelfAccount.nickName)
                .senderId(mSelfAccount.id)
                .osType(AppConfig.osType)
                .type(MessageType.VIDEO)
                .flag(MessageFlag.OWNER)
                .status(MessageStatus.SENDING)
                .sendTime(System.currentTimeMillis())
                .content(videoContent.toStringContent())
                .build()
        } ?: run {
            return null
        }
    }

    /**
     * 送出 video message
     */
    fun sendVideo(
        iVideoSize: IVideoSize,
        isQuote: Boolean,
        isFacebookReplyPublic: Boolean
    ) = viewModelScope.launch(Dispatchers.IO) {
        roomEntity?.let {
            // 確認是否是強制接手
            if (checkIsSnatch()) {
                val agentName =
                    UserProfileReference.findAccountName(null, it.serviceNumberAgentId)
                sendSnatchedAgent.emit(SendVideoFormat(agentName, iVideoSize))
                return@launch
            }

            if (checkIsRobotSnatch()) {
                showSnatchRobotDialog.emit(SendVideoFormat(iVideoSize = iVideoSize))
                return@launch
            }

            if (recordMode) {
                loadMsgDBDesc()
                recordMode = false
            }

            if (isFacebookReply()) {
                sendFacebookAttachmentReply(isFacebookReplyPublic, iVideoSize.name(), iVideoSize.path())
                CoroutineScope(Dispatchers.Main).launch {
                    sendShowSendVideoProgress.emit(application.getString(R.string.text_sending_video_message))
                }
                return@launch
            }

            CoroutineScope(Dispatchers.Main).launch {
                sendShowSendVideoProgress.emit(if (isQuote) application.getString(R.string.text_video_quote_on_the_way) else application.getString(R.string.text_sending_video_message))
            }
            val messageId = withContext(Dispatchers.Main) { generateMessageId() }
            val videoContent: VideoContent = buildVideoContent(iVideoSize)
            val msg: MessageEntity? = buildVideoMessage(messageId, videoContent)
            msg?.let { _ ->
                showMessage(msg)
                setReplyMsg(msg)
                UploadManager.getInstance().onUploadFile(
                    application,
                    iVideoSize.name(),
                    messageId,
                    tokenId,
                    MessageType.VIDEO,
                    iVideoSize.path(),
                    object : OnUploadListener {
                        override fun onSuccess(
                            messageId: String,
                            type: MessageType,
                            response: String
                        ) {
                            val entity =
                                JsonHelper.getInstance().from(
                                    response,
                                    UploadManager.FileEntity::class.java
                                )
                            if (!Strings.isNullOrEmpty(entity.url) &&
                                !Strings.isNullOrEmpty(
                                    entity.name
                                ) &&
                                entity.size >= 0
                            ) {
                                videoContent.name = entity.name
                                videoContent.url = entity.url
                                videoContent.size = entity.size.toLong()
                                MessageReference.save(msg.roomId, msg)
                                if (isThemeOpen) {
                                    ChatService.getInstance().sendReplyVideoMessage(
                                        it.id,
                                        messageId,
                                        themeMessage?.id,
                                        videoContent,
                                        entity.mD5,
                                        entity.thumbnailUrl,
                                        entity.thumbnailWidth,
                                        entity.thumbnailHeight
                                    )
                                } else {
                                    ChatService.getInstance().sendVideoMessage(
                                        it.id,
                                        messageId,
                                        "",
                                        videoContent,
                                        entity.mD5,
                                        entity.thumbnailUrl,
                                        entity.thumbnailWidth,
                                        entity.thumbnailHeight
                                    )
                                }
                                CoroutineScope(Dispatchers.Main).launch {
                                    sendSetThemeOpen.emit(false)
                                    scrollListToPosition.emit(mainMessageData.size - 1)
                                }
                            }
                            CoroutineScope(Dispatchers.Main).launch {
                                sendUpdateSendVideoProgress.emit(Unit)
                            }
                        }

                        override fun onProgress(
                            messageId: String,
                            progress: Int,
                            total: Long
                        ) {
                            CoroutineScope(Dispatchers.Main).launch {
                                sendUpdateMsgProgress.emit(Pair(messageId, progress))
                                sendUpdateSendVideoProgressInt.emit(progress)
                            }
                        }

                        override fun onFailed(reason: String) {
                            CELog.e(reason)
                            CoroutineScope(Dispatchers.Main).launch {
                                sendUpdateMsgStatus.emit(Triple(it.id, messageId, MessageStatus.FAILED))
                                sendDismissSendVideoProgress.emit(Unit)
                                scrollListToPosition.emit(mainMessageData.size - 1)
                            }
                        }
                    }
                )
            }
        }
    }

    private fun setReplyMsg(msg: MessageEntity) {
        if (!isThemeOpen) {
            return
        }
        nearMessage?.let {
            msg.nearMessageId = it.id
            msg.nearMessageType = it.type
            msg.nearMessageSenderId = it.senderId
            msg.nearMessageSenderName = it.senderName
            msg.nearMessageAvatarId = it.avatarId
            msg.nearMessageContent = it.content().toStringContent()
        }
        themeMessage?.let {
            msg.themeId = it.id
        }
    }

    // 下下策 等之後有重構再處理
    fun doAgentSnatchByAgent(
        agentSnatchCallback: AgentSnatchCallback?
    ) = viewModelScope.launch(Dispatchers.IO) {
        roomEntity?.let {
            CoroutineScope(Dispatchers.Main).launch {
                sendShowLoadingView.emit(R.string.wording_loading)
            }
            ChatRoomService.getInstance().snatchAgentServicing(
                application,
                it.id,
                object : RoomRecentCallBack<CommonResponse<Any>, ServicedTransferType?> {
                    override fun error(message: String) {
                        CoroutineScope(Dispatchers.Main).launch {
                            sendHideLoadingView.emit(Unit)
                            sendShowErrorMsg.emit(message)
                        }
                    }

                    override fun complete(
                        objectCommonResponse: CommonResponse<Any>,
                        e: ServicedTransferType?
                    ) {
                        CoroutineScope(Dispatchers.Main).launch {
                            sendHideLoadingView.emit(Unit)
                            objectCommonResponse._header_?.let { header ->
                                if (header.success!!) {
                                    doServiceNumberServicedStatus(agentSnatchCallback)
                                    it.serviceNumberAgentId = selfUserId
                                } else {
                                    sendShowErrorMsg.emit(header.errorMessage)
                                }
                            }
                        }
                    }

                    override fun finish() {
                        CoroutineScope(Dispatchers.Main).launch {
                            sendHideLoadingView.emit(Unit)
                        }
                    }
                }
            )
        }
    }

    private fun loadMsgDBDesc() =
        viewModelScope.launch(Dispatchers.IO) {
            roomEntity?.let {
                val messages =
                    MessageReference.findByRoomIdAndTypeAndSortAndLimitAndInnerJoniAliasName(
                        it.id,
                        System.currentTimeMillis(),
                        it.type,
                        MessageReference.Sort.DESC,
                        50
                    )
                CoroutineScope(Dispatchers.Main).launch {
                    sendTransferModeDisplay.emit(messages)
                }
            }
        }

    fun doServiceNumberServicedStatus(
        agentSnatchCallback: AgentSnatchCallback?
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            roomEntity?.let { entity ->
                doAppointStatus(entity.id)
                if (selfUserId == entity.ownerId) return@let
                ChatServiceNumberService.findServicedStatusAndAppoint(
                    application,
                    entity.id,
                    object :
                        ServiceCallBack<ServiceNumberChatroomAgentServicedRequest.Resp?, ChatServiceNumberService.ServicedType> {
                        override fun complete(
                            resp: ServiceNumberChatroomAgentServicedRequest.Resp?,
                            servicedType: ChatServiceNumberService.ServicedType
                        ) {
                            when (servicedType) {
                                ChatServiceNumberService.ServicedType.AGENT_SERVICED -> {
                                    resp?.let {
                                        entity.serviceNumberStatus = resp.serviceNumberStatus
                                        entity.serviceNumberAgentId = resp.serviceNumberAgentId
                                        entity.identityId = resp.identityId

                                        // 如果用戶在線，且沒人接線
                                        if (ServiceNumberStatus.ON_LINE == resp.serviceNumberStatus && Strings.isNullOrEmpty(resp.serviceNumberAgentId)) {
                                            doServiceNumberStart(resp.roomId)
                                        } else {
                                            CoroutineScope(Dispatchers.Main).launch {
                                                sendSetupBottomServicedStatus.emit(
                                                    Triple(
                                                        resp.serviceNumberStatus,
                                                        entity.serviceNumberType,
                                                        resp
                                                    )
                                                )
                                            }
                                        }

                                        if (resp.aiConsultArray != null && resp.aiConsultArray.size > 0) {
                                            entity.aiConsultId = resp.aiConsultArray[0].consultId
                                        } else {
                                            entity.aiConsultId = ""
                                        }
                                        CoroutineScope(Dispatchers.Main).launch {
                                            sendSetRobotChatRecord.emit(resp)
                                        }

                                        // 諮詢聊天室
                                        setConsultRoom(
                                            resp.aiConsultArray,
                                            resp.activeServiceConsultArray,
                                            entity.id,
                                            entity.serviceNumberId
                                        )
                                    }
                                }

                                ChatServiceNumberService.ServicedType.APPOINT -> {
                                    resp?.let {
                                        appointResp =
                                            FromAppointRequest.Resp(
                                                resp.serviceNumberStatus,
                                                resp.lastFrom,
                                                resp.otherFroms
                                            )
                                        CoroutineScope(Dispatchers.Main).launch {
                                            sendSetupAppointStatus.emit(appointResp)
                                        }
                                    } ?: run {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            sendSetupAppointStatus.emit(null)
                                        }
                                    }
                                }

                                ChatServiceNumberService.ServicedType.ROBOT_SERVICED ->
                                    if (resp != null) {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            sendLoadRobotChatRecordLink.emit(resp.robotChatRecordLink)
                                        }
                                    }

                                ChatServiceNumberService.ServicedType.ROBOT_SERVICE_STOP ->
                                    if (entity.serviceNumberStatus == ServiceNumberStatus.ON_LINE) {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            sendRobotStopStatus.emit(Unit)
                                        }
                                    }
                            }
                            agentSnatchCallback?.onSnatchSuccess()
                        }

                        override fun error(message: String) {
                            CoroutineScope(Dispatchers.Main).launch {
                                sendSetupAppointStatus.emit(null)
                            }
                        }
                    }
                )
            }
        }
    }

    fun doServiceNumberStart(roomId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            roomEntity?.let {
                ApiManager.doServiceNumberStartService(
                    application,
                    roomId,
                    object : ApiListener<Boolean> {
                        override fun onSuccess(result: Boolean) {
                            if (result) {
                                it.serviceNumberAgentId = selfUserId
                                doServiceNumberServicedStatus(null)
                                CoroutineScope(Dispatchers.Main).launch {
                                    sendSetupAppointStatus.emit(appointResp)
                                }
                            }
                        }

                        override fun onFailed(errorMessage: String) {
                            Log.i("doServiceNumberStart", errorMessage + "")
                        }
                    }
                )
            }
        }

    fun onMessageSendStats(msgStatusBean: MsgStatusBean) =
        viewModelScope.launch(Dispatchers.IO) {
            val index = mainMessageData.indexOfFirst { it.id == msgStatusBean.messageId }
            if (index < 0) return@launch
            val currentMessage = mainMessageData[index].copy()
            if (msgStatusBean.sendNum < 0) {
                currentMessage.status = MessageStatus.FAILED
            } else {
                currentMessage.status = MessageStatus.SUCCESS
            }
            currentMessage.sendNum = msgStatusBean.sendNum

            roomEntity?.let {
                chatRepository.getMessageItem(it.id, msgStatusBean.messageId).collect {
                    when (it) {
                        is ApiResult.Success -> {
                            currentMessage.sequence = it.data.sequence
                            mainMessageData[index] = currentMessage
                            mainMessageDataFlow.emit(Pair(false, mainMessageData.toMutableList()))
                        }

                        else -> {
                            mainMessageData[index] = currentMessage
                            mainMessageDataFlow.emit(Pair(false, mainMessageData.toMutableList()))
                        }
                    }
                }
            }
        }

    fun resendMessage(message: MessageEntity) =
        viewModelScope.launch(Dispatchers.Main) {
            val removeIndex = mainMessageData.indexOf(message)
            mainMessageData.remove(message)
            mainMessageDataFlow.emit(Pair(false, mainMessageData.toMutableList()))
//            notifyRecyclerRemove.emit(removeIndex)
            retrySend(message)
        }

    /**
     * 更新訊息 已讀/未讀狀態
     * @param msgNoticeBean Socket 來的資料
     * */
    fun onReadReceived(msgNoticeBean: MsgNoticeBean) =
        viewModelScope.launch(Dispatchers.IO) {
            val index = mainMessageData.indexOfFirst { it.id == msgNoticeBean.messageId }
            if (index < 0) return@launch
            val currentMessage = mainMessageData[index]

            if (msgNoticeBean.receivedNum > (currentMessage.receivedNum ?: 0)) {
                currentMessage.receivedNum = msgNoticeBean.receivedNum
                DBManager.getInstance().updateReceivedNum(msgNoticeBean.messageId, msgNoticeBean.receivedNum)
            }

            if (msgNoticeBean.readNum > (currentMessage.readedNum ?: 0)) {
                currentMessage.readedNum = msgNoticeBean.readNum
                DBManager.getInstance().updateReadNum(msgNoticeBean.messageId, msgNoticeBean.readNum)
            }

            if (msgNoticeBean.sendNum > (currentMessage.sendNum ?: 0)) {
                currentMessage.sendNum = msgNoticeBean.sendNum
                DBManager.getInstance().updateSendNum(msgNoticeBean.messageId, msgNoticeBean.sendNum)
            }
            roomEntity?.let {
                if (it.type == ChatRoomType.group || it.type == ChatRoomType.discuss) {
                    currentMessage.status = MessageStatus.SUCCESS
                    DBManager.getInstance().updateMessageStatus(currentMessage.id, MessageStatus.SUCCESS)
                } else {
                    if (msgNoticeBean.readNum > 0) {
                        currentMessage.status = MessageStatus.READ
                        DBManager.getInstance().updateMessageStatus(currentMessage.id, MessageStatus.READ)
                    } else if (msgNoticeBean.readNum == 0 && msgNoticeBean.receivedNum > 0) {
                        currentMessage.status = MessageStatus.RECEIVED
                        DBManager.getInstance().updateMessageStatus(currentMessage.id, MessageStatus.RECEIVED)
                    } else {
                        currentMessage.status = MessageStatus.SUCCESS
                        DBManager.getInstance().updateMessageStatus(currentMessage.id, MessageStatus.SUCCESS)
                    }
                }
            }
            refreshCurrentMessagePosition.emit(index)
        }

    fun doAppointStatus(roomId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            ChatServiceNumberService.findAppoint(
                application,
                roomId,
                object :
                    ServiceCallBack<ServiceNumberChatroomAgentServicedRequest.Resp?, ChatServiceNumberService.ServicedType> {
                    override fun complete(
                        resp: ServiceNumberChatroomAgentServicedRequest.Resp?,
                        servicedType: ChatServiceNumberService.ServicedType
                    ) {
                        resp?.let {
                            appointResp =
                                FromAppointRequest.Resp(
                                    resp.serviceNumberStatus,
                                    resp.lastFrom,
                                    resp.otherFroms
                                )
                            CoroutineScope(Dispatchers.Main).launch {
                                sendSetupAppointStatus.emit(appointResp)
                            }
                        } ?: run {
                            CoroutineScope(Dispatchers.Main).launch {
                                sendSetupAppointStatus.emit(null)
                            }
                        }
                    }

                    override fun error(message: String) {
                    }
                }
            )
        }

    private fun isFacebookReply(): Boolean = themeMessage?.from == ChannelType.FB

    /**
     * 發送 Facebook 圖片回覆
     * @param isFacebookReplyPublic 判斷是否是公開回覆 true 公開 false 私人
     * @param fileName 圖片名稱
     * @param filePath 圖片路徑
     */
    fun sendFacebookAttachmentReply(
        isFacebookReplyPublic: Boolean,
        fileName: String,
        filePath: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        val request =
            buildSendFacebookAttachmentRequest(isFacebookReplyPublic, fileName, filePath)
        try {
            request?.let {
                OkHttpClient().newCall(it.build()).execute().use { response ->
                    val responseBody = response.body
                    if (responseBody != null) {
                        val responseBodyString = responseBody.string()
                        val commonResponse: CommonResponse<*> =
                            JsonHelper.getInstance().from(
                                responseBodyString,
                                CommonResponse::class.java
                            )
                        commonResponse._header_?.let { header ->
                            if (header.success != null) {
                                if (header.success!!) {
                                    sendOnSendFacebookImageReplySuccess.emit(Unit)
                                } else {
                                    sendOnSendFacebookImageReplyFailed.emit("發送失敗，請稍後再試")
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: IOException) {
            CELog.e("sendFacebookImageReply Error", e)
        } finally {
            CoroutineScope(Dispatchers.Main).launch {
                sendUpdateSendVideoProgress.emit(Unit)
            }
        }
    }

    /**
     * 組成 Facebook 圖片回覆 request
     * @param isFacebookReplyPublic 判斷是否是公開回覆 true 公開 false 私人
     * @param fileName 圖片名稱
     * @param filePath 圖片路徑
     */
    private fun buildSendFacebookAttachmentRequest(
        isFacebookReplyPublic: Boolean,
        fileName: String,
        filePath: String
    ): Request.Builder? {
        try {
            val originThemeMessage: MessageEntity? = themeMessage
            originThemeMessage?.let { messageEntity ->
                val facebookTag =
                    JsonHelper.getInstance().from(
                        messageEntity.tag,
                        FacebookTag::class.java
                    )
                val builder: MultipartBody.Builder = MultipartBody.Builder().setType(MultipartBody.FORM)
                val fileType = filePath.substring(filePath.lastIndexOf("."))
                val mediaType = FileMedia.of(fileType)
                val fileRequestBody = RequestBody.create(mediaType, File(filePath))
                builder.addFormDataPart("attachment", fileName, fileRequestBody)
                val args =
                    FacebookImageArgs(tokenId)
                        .postId(facebookTag.data.postId)
                        .commentId(facebookTag.data.commentId)
                        .toJson()
                val formData: Map<String, String> = Maps.newHashMap(ImmutableMap.of("args", args))

                for ((key, value) in formData) {
                    builder.addFormDataPart(key, value)
                }

                var url = ""
                url =
                    if (isFacebookReplyPublic) {
                        TokenPref.getInstance(application).currentTenantUrl + "/" + ApiPath.sendFacebookPublicReply
                    } else {
                        TokenPref.getInstance(application).currentTenantUrl + "/" + ApiPath.sendFacebookPrivateReply
                    }

                return Request
                    .Builder()
                    .url(url)
                    .post(builder.build())
            } ?: run {
                return null
            }
        } catch (e: java.lang.Exception) {
            CELog.e("buildSendFacebookImageRequest Error", e)
            return null
        }
    }

    private fun showMessage(msg: MessageEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            roomEntity?.let {
                MessageReference.save(it.id, msg)
                if (isThemeOpen) {
                    if (!Strings.isNullOrEmpty(msg.themeId)) {
                        CoroutineScope(Dispatchers.Main).launch {
                            sendDisplayThemeMessage.emit(Triple(msg.themeId ?: "", true, msg))
                        }
                    }
                }
                isUserScroll = false
                displayMainMessageLogic(isNew = false, scrollToBottom = true, msg)
            }
        }

    fun sendImage(
        name: String,
        thumbnailName: String
    ) {
        sendImage(name, thumbnailName, isQuote = false, isFacebookReplyPublic = false)
    }

    fun sendImage(
        name: String,
        thumbnailName: String,
        isFacebookReplyPublic: Boolean
    ) {
        sendImage(name, thumbnailName, false, isFacebookReplyPublic)
    }

    /**
     * FIFO 的發送圖片－目前是給 ChatNormalActivity 使用
     * */
    fun sendImage(
        isOriginPhoto: Boolean = false,
        path: String,
        isQuote: Boolean = false,
        isFacebookReplyPublic: Boolean = false
    ) = viewModelScope.launch(Dispatchers.IO) {
        val request = SendImageRequest(path, isQuote, isOriginPhoto, isFacebookReplyPublic)
        imageSendQueue.trySend(request)
    }

    /**
     * 送出圖片訊息
     */
    fun sendImage(
        name: String,
        thumbnailName: String,
        isQuote: Boolean,
        isFacebookReplyPublic: Boolean
    ) = viewModelScope.launch(Dispatchers.IO) {
        roomEntity?.let { room ->
            // 確認是否是強制接手
            if (checkIsSnatch()) {
                val agentName =
                    UserProfileReference.findAccountName(null, room.serviceNumberAgentId)
                sendSnatchedAgent.emit(SendImageFormat(agentName, thumbnailName))
                return@launch
            }

            if (checkIsRobotSnatch()) {
                showSnatchRobotDialog.emit(SendImageFormat(thumbnailName = thumbnailName))
                return@launch
            }

            if (recordMode) {
                loadMsgDBDesc()
                recordMode = false
            }

            val messageId = withContext(Dispatchers.Main) { generateMessageId() }
            val cacheFile = DaVinci.with().imageLoader.getAbsolutePath(name)
            BitmapHelper.getBitmapFromLocal(cacheFile)

            val image = DaVinci.with().imageLoader.getImage(name)
            val bitmap = image.bitmap
            val path = DaVinci.with().imageLoader.getAbsolutePath(name)
            val msg =
                MsgKitAssembler.assembleSendImageMessage(
                    room.id,
                    messageId,
                    mSelfAccount.id,
                    mSelfAccount.avatarId,
                    mSelfAccount.nickName,
                    name,
                    path,
                    bitmap.width,
                    bitmap.height
                )
            showMessage(msg)
            setReplyMsg(msg)
            if (isFacebookReply()) {
                val path = DaVinci.with().imageLoader.getAbsolutePath(name)
                sendFacebookAttachmentReply(isFacebookReplyPublic, name, path)
                CoroutineScope(Dispatchers.Main).launch {
                    sendShowSendVideoProgress.emit("圖片發送中")
                }
                return@launch
            }
            sendShowSendVideoProgress.emit(if (isQuote) application.getString(R.string.text_image_quote_on_the_way) else "圖片發送中")

            FileService.uploadFile(
                application,
                true,
                tokenId,
                Media.findByFileType(name),
                path,
                name,
                object : AServiceCallBack<UploadManager.FileEntity?, RefreshSource>() {
                    override fun onProgress(
                        progress: Float,
                        total: Long
                    ) {
                        CoroutineScope(Dispatchers.Main).launch {
                            sendUpdateMsgProgress.emit(Pair(messageId, (progress * 100).toInt()))
                            sendUpdateSendVideoProgressInt.emit((progress * 100).toInt())
                        }
                    }

                    override fun complete(
                        entity: UploadManager.FileEntity?,
                        refreshSource: RefreshSource
                    ) {
                        entity?.let { fileEntity ->
                            if (Strings.isNullOrEmpty(entity.url) || Strings.isNullOrEmpty(entity.thumbnailUrl)) {
                                error("上傳失敗")
                                return
                            }

                            val message = MessageReference.findById(messageId)
                            if (message != null && message.content() is ImageContent) {
                                val imageContent = message.content() as ImageContent

                                imageContent.url = entity.url
                                imageContent.size = entity.size
                                imageContent.height = entity.height
                                imageContent.width = entity.width

                                imageContent.thumbnailUrl = entity.thumbnailUrl
                                imageContent.thumbnailSize = entity.thumbnailSize
                                imageContent.thumbnailHeight = entity.thumbnailHeight
                                imageContent.thumbnailWidth = entity.thumbnailWidth

                                message.content = imageContent.toStringContent()

                                MessageReference.save(message.roomId, message)
                            }

                            if (isThemeOpen) {
                                themeMessage?.let {
                                    ChatService.getInstance().sendReplyImageMessage(
                                        room.id,
                                        messageId,
                                        it.id,
                                        entity
                                    )
                                }
                            } else {
                                ChatService
                                    .getInstance()
                                    .sendImageMessage(room.id, messageId, "", entity)
                            }
                            CoroutineScope(Dispatchers.Main).launch {
                                sendUpdateSendVideoProgress.emit(Unit)
                                sendSetThemeOpen.emit(false)
                                scrollListToPosition.emit(mainMessageData.size - 1)
                            }
                        } ?: run {
                            error("上傳失敗")
                        }
                    }

                    override fun error(errorMessage: String) {
                        CELog.e(errorMessage)
                        CoroutineScope(Dispatchers.Main).launch {
                            sendUpdateMsgStatus.emit(Triple(room.id, messageId, MessageStatus.FAILED))
                            sendUpdateSendVideoProgress.emit(Unit)
                            scrollListToPosition.emit(mainMessageData.size - 1)
                        }
                    }
                }
            )
        }
    }

    fun sendImage(
        name: String,
        path: String,
        bitmap: Bitmap,
        isFacebookReplyPublic: Boolean
    ) {
        sendImage(name, path, bitmap, false, isFacebookReplyPublic)
    }

    fun sendImage(
        name: String,
        path: String,
        bitmap: Bitmap
    ) {
        sendImage(name, path, bitmap, isQuote = false, isFacebookReplyPublic = false)
    }

    fun sendImage(
        name: String,
        path: String,
        bitmap: Bitmap,
        isQuote: Boolean,
        isFacebookReplyPublic: Boolean
    ) = viewModelScope.launch(Dispatchers.IO) {
        roomEntity?.let { chatRoomEntity ->
            // 確認是否是強制接手
            if (checkIsSnatch()) {
                val agentName =
                    UserProfileReference.findAccountName(null, chatRoomEntity.serviceNumberAgentId)
                sendSnatchedAgent.emit(SendImageWithBitMapFormat(agentName, path, bitmap))
                return@launch
            }

            if (checkIsRobotSnatch()) {
                showSnatchRobotDialog.emit(SendImageWithBitMapFormat(path = path, bitmap = bitmap))
                return@launch
            }

            if (recordMode) {
                loadMsgDBDesc()
                recordMode = false
            }
            val messageId = generateMessageId()
            val msg =
                MsgKitAssembler.assembleSendImageMessage(
                    chatRoomEntity.id,
                    messageId,
                    mSelfAccount.id,
                    mSelfAccount.avatarId,
                    mSelfAccount.nickName,
                    name,
                    path,
                    bitmap.width,
                    bitmap.height
                )
            showMessage(msg)
            setReplyMsg(msg)
            if (isFacebookReply()) {
                sendFacebookAttachmentReply(isFacebookReplyPublic, name, path)
                CoroutineScope(Dispatchers.Main).launch {
                    sendShowSendVideoProgress.emit("圖片發送中")
                }
                return@launch
            }
            sendShowSendVideoProgress.emit(if (isQuote) application.getString(R.string.text_image_quote_on_the_way) else "圖片發送中")

            FileService.uploadFile(
                application,
                true,
                tokenId,
                Media.findByFileType(name),
                path,
                name,
                object : AServiceCallBack<UploadManager.FileEntity?, RefreshSource>() {
                    override fun onProgress(
                        progress: Float,
                        total: Long
                    ) {
                        CoroutineScope(Dispatchers.Main).launch {
                            sendUpdateMsgProgress.emit(Pair(messageId, (progress * 100).toInt()))
                        }
                    }

                    override fun complete(
                        entity: UploadManager.FileEntity?,
                        refreshSource: RefreshSource
                    ) {
                        entity?.let { fileEntity ->
                            if (Strings.isNullOrEmpty(entity.url) || Strings.isNullOrEmpty(entity.thumbnailUrl)) {
                                error("上傳失敗")
                                return
                            }

                            val message = MessageReference.findById(messageId)
                            if (message != null && message.content() is ImageContent) {
                                val imageContent = message.content() as ImageContent

                                imageContent.url = entity.url
                                imageContent.size = entity.size
                                imageContent.height = entity.height
                                imageContent.width = entity.width

                                imageContent.thumbnailUrl = entity.thumbnailUrl
                                imageContent.thumbnailSize = entity.thumbnailSize
                                imageContent.thumbnailHeight = entity.thumbnailHeight
                                imageContent.thumbnailWidth = entity.thumbnailWidth

                                message.content = imageContent.toStringContent()

                                MessageReference.save(message.roomId, message)
                            }

                            if (isThemeOpen) {
                                themeMessage?.let {
                                    ChatService.getInstance().sendReplyImageMessage(
                                        chatRoomEntity.id,
                                        messageId,
                                        it.id,
                                        entity
                                    )
                                }
                            } else {
                                ChatService
                                    .getInstance()
                                    .sendImageMessage(chatRoomEntity.id, messageId, "", entity)
                            }
                            CoroutineScope(Dispatchers.Main).launch {
                                sendUpdateSendVideoProgress.emit(Unit)
                                sendSetThemeOpen.emit(false)
                            }
                        } ?: run {
                            error("上傳失敗")
                        }
                    }

                    override fun error(errorMessage: String) {
                        CELog.e(errorMessage)
                        CoroutineScope(Dispatchers.Main).launch {
                            sendUpdateMsgStatus.emit(
                                Triple(
                                    chatRoomEntity.id,
                                    messageId,
                                    MessageStatus.FAILED
                                )
                            )
                            sendUpdateSendVideoProgress.emit(Unit)
                        }
                    }
                }
            )
        }
    }

    private suspend fun processSendImage(request: SendImageRequest) =
        withContext(Dispatchers.IO) {
            val loadingMessage =
                when {
                    request.isQuote -> application.getString(R.string.text_image_quote_on_the_way)
                    else -> application.getString(R.string.text_sending_image)
                }
            CoroutineScope(Dispatchers.Main).launch {
                sendShowSendVideoProgress.emit(loadingMessage)
            }
            roomEntity?.let { chatRoomEntity ->
                val imagePath =
                    if (request.isOriginPhoto) request.path else ImageUtil(application).compressImageFile(request.path)
                if (imagePath == null) {
                    sendUpdateSendVideoProgress.emit(Unit)
                    return@withContext
                }
                val imageFile = File(imagePath)
                if (!imageFile.exists()) {
                    sendUpdateSendVideoProgress.emit(Unit)
                    return@withContext
                }
                val imageBitmap = BitmapFactory.decodeFile(imagePath)

                if (isFacebookReply()) {
                    sendFacebookAttachmentReply(request.isFacebookReplyPublic, imageFile.name, imagePath)
                    return@withContext
                }
                if (checkIsSnatch()) {
                    val agentName =
                        UserProfileReference.findAccountName(null, chatRoomEntity.serviceNumberAgentId)
                    sendSnatchedAgent.emit(SendImageWithBitMapFormat(agentName, imagePath, imageBitmap))
                    return@withContext
                }

                if (checkIsRobotSnatch()) {
                    showSnatchRobotDialog.emit(SendImageWithBitMapFormat(path = imagePath, bitmap = imageBitmap))
                    return@withContext
                }

                if (recordMode) {
                    loadMsgDBDesc()
                    recordMode = false
                }
                val messageId = generateMessageId()
                val msg =
                    MsgKitAssembler.assembleSendImageMessage(
                        chatRoomEntity.id,
                        messageId,
                        mSelfAccount.id,
                        mSelfAccount.avatarId,
                        mSelfAccount.nickName,
                        imageFile.name,
                        imagePath,
                        imageBitmap.width,
                        imageBitmap.height
                    )
                showMessage(msg)
                setReplyMsg(msg)
                UploadUtil(application).uploadImage(imagePath, onUploadSuccess = { responseBodyString ->
                    CoroutineScope(Dispatchers.IO).launch {
                        msg.content = responseBodyString
                        MessageReference.save(chatRoomEntity.id, msg)
                        val imageContent = msg.content() as ImageContent
                        val themeId = if (isThemeOpen) themeMessage?.id ?: "" else ""
                        if (Strings.isNullOrEmpty(themeId)) {
                            ChatService
                                .getInstance()
                                .sendImageMessage(chatRoomEntity.id, messageId, "", imageContent)
                        } else {
                            ChatService
                                .getInstance()
                                .sendReplyImageMessage(chatRoomEntity.id, messageId, themeId, imageContent)
                        }

                        withContext(Dispatchers.Main) {
                            sendUpdateSendVideoProgress.emit(Unit)
                            sendSetThemeOpen.emit(false)
                        }
                    }
                }, onUploadProgress = { progress ->
                    CoroutineScope(Dispatchers.Main).launch {
                        sendUpdateMsgProgress.emit(Pair(messageId, (progress * 100)))
                        sendUpdateSendVideoProgressInt.emit((progress * 100))
                    }
                }, onUploadError = { errorMessage ->
                    CELog.e(errorMessage)
                    CoroutineScope(Dispatchers.Main).launch {
                        val index = mainMessageData.indexOf(msg)
                        msg.status = MessageStatus.FAILED
                        mainMessageData[index] = msg
                        sendUpdateSendVideoProgress.emit(Unit)
                        scrollListToPosition.emit(mainMessageData.size - 1)
                    }
                })
            }
        }

    fun getMemberProfileEntity(memberIds: List<String>) =
        viewModelScope.launch(Dispatchers.IO) {
            val memberItems: MutableList<UserProfileEntity> = Lists.newArrayList()
            for (id in memberIds) memberItems.add(DBManager.getInstance().queryFriend(id))
            CoroutineScope(Dispatchers.Main).launch {
                sendInitProvisionMemberList.emit(Pair(memberItems, memberIds))
            }
        }

    fun getMemberProfileEntityDirectly(memberIds: List<String>): List<UserProfileEntity> {
        val memberItems: MutableList<UserProfileEntity> = Lists.newArrayList()
        for (id in memberIds) memberItems.add(DBManager.getInstance().queryFriend(id))
        return memberItems
    }

    fun removeProvisionalMember(memberId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            roomEntity?.let {
                ApiManager.doMemberDelete(
                    application,
                    it.id,
                    Lists.newArrayList<CharSequence>(memberId),
                    object : ApiListener<String> {
                        override fun onSuccess(s: String) {
                        }

                        override fun onFailed(errorMessage: String) {
                            CoroutineScope(Dispatchers.Main).launch {
                                sendShowErrorMsg.emit(errorMessage)
                            }
                        }
                    }
                )
            }
        }

    fun refreshMoreMsg(entity: MessageEntity?) =
        viewModelScope.launch(Dispatchers.IO) {
            roomEntity?.let {
                entity?.let { message ->
                    messageListRequestDesc(it.id, it.type, "")

                    val entities =
                        MessageReference.findByRoomIdAndTypeAndSortAndLimitAndInnerJoniAliasName(
                            it.id,
                            message.sendTime - 1,
                            it.type,
                            MessageReference.Sort.DESC,
                            50
                        )
                    if (entities.isEmpty() || entities.size <= 1) {
                        // message/list抓後台資料
                        messageListRequestDesc(
                            it.id,
                            it.type,
                            message,
                            50
                        )
                    } else if (entities.size <= 50) {
                        entities.sort()
                        sendOnRefreshMore.emit(Pair(entities, message))
                        messageListRequestDesc(
                            it.id,
                            it.type,
                            entities[0],
                            50
                        )
                    } else {
                        sendOnRefreshMore.emit(Pair(entities, message))
                    }
                }
            }
        }

    /**
     * get Message List Information (new) --> (old)
     */
    private fun messageListRequestDesc(
        roomId: String,
        chatRoomType: ChatRoomType,
        lastMessageId: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        ChatMessageService.getChatMessageEntities(
            application,
            roomId,
            chatRoomType,
            lastMessageId,
            50,
            Sort.DESC,
            object : ServiceCallBack<List<MessageEntity>, RefreshSource> {
                override fun complete(
                    entities: List<MessageEntity>,
                    refreshSource: RefreshSource
                ) {
                    Collections.sort(entities)
                    if (entities.isEmpty()) {
                        return
                    }
                    val newMsgIds: MutableList<String?> = Lists.newArrayList()
                    for (i in entities.indices.reversed()) {
                        val msg = entities[i]
                        // EVAN_FLAG 2019-09-24 As long as it is read, it will not be sent
                        if (MessageStatus.READ == msg.status) {
                            newMsgIds.add(msg.id)
                        }
                        displayMainMessageLogic(isNew = false, scrollToBottom = true, msg)
                    }
                    if (newMsgIds.isNotEmpty()) {
                        ChatMessageService.doMessageReadAllByRoomId(
                            application,
                            roomId,
                            newMsgIds,
                            false
                        )
                    }
                }

                override fun error(message: String) {
                }
            }
        )
    }

    /**
     * get  Message List Information (new) --> (old)
     */
    private fun messageListRequestDesc(
        sessionId: String,
        chatRoomType: ChatRoomType,
        lastMsg: MessageEntity,
        number: Int
    ) = viewModelScope.launch(Dispatchers.IO) {
        ChatMessageService.getChatMessageEntities(
            application,
            sessionId,
            chatRoomType,
            lastMsg.id,
            number,
            Sort.DESC,
            object : ServiceCallBack<MutableList<MessageEntity>, RefreshSource> {
                override fun complete(
                    entities: MutableList<MessageEntity>,
                    refreshSource: RefreshSource
                ) {
                    CoroutineScope(Dispatchers.IO).launch {
                        entities.removeIf { it.flag == MessageFlag.DELETED }
                        sendOnRefreshMore.emit(Pair(entities, lastMsg))
                    }
                }

                override fun error(message: String) {
                }
            }
        )
    }

    fun getFirstMsgId(): MessageEntity? {
        var msg: MessageEntity? = null
        for (message in mainMessageData) {
//            通过sendId区分自定义日期消息和server消息
            if (message.senderId != null) {
                msg = message
                break
            }
        }
        return msg
    }

    /**
     * 如果該 Facebook 留言已經被私訊回覆過，則移動到該私訊回覆的訊息
     * @param messageId Facebook 留言訊息的 Id
     * @param commentId Facebook 留言訊息的 commentId
     * @param mainMessageList 全部的 Message
     */
    fun moveToFacebookReplyMessage(
        messageId: String?,
        commentId: String,
        mainMessageList: List<MessageEntity>
    ) = viewModelScope.launch(Dispatchers.IO) {
        for (i in mainMessageList.indices) {
            val message = mainMessageList[i]
            if (message.tag == null) continue
            if (message.tag!!.isEmpty()) continue
            if (messageId == null || messageId == message.id) continue
            val facebookTag =
                JsonHelper.getInstance().from(
                    message.tag,
                    FacebookTag::class.java
                )
            if (commentId == facebookTag.data.commentId && "private" == facebookTag.data.replyType) {
                message.isAnimator = true
                sendMoveToFacebookReplyMessage.emit(i)
                return@launch
            }
        }
    }

    /**
     * 更新留言的狀態，用 Queue 確保不會並發太多
     */
    fun checkCommentStatus(message: MessageEntity): Deferred<Unit> =
        viewModelScope.async(Dispatchers.IO) {
            roomEntity?.let { room ->
                if (tempFacebookAlreadyCheckStatus.contains(message.id)) return@async
                if (message.tag == null) return@async
                val facebookTag =
                    JsonHelper.getInstance().from(
                        message.tag,
                        FacebookTag::class.java
                    )
                if (message.facebookPostStatus == FacebookPostStatus.Delete) return@async
                if (facebookTag == null) return@async
                if (facebookTag.data == null) return@async
                if (facebookTag.data.commentId == null) return@async
                val checkCommentStatusRequest = CheckCommentStatusRequest(facebookTag.data.commentId)
                val responseQueue =
                    provideRetrofit(application).create(tw.com.chainsea.chat.view.chat.ChatService::class.java).checkCommentStatus(checkCommentStatusRequest)
                if (isTokenNeedRefresh) {
                    checkCommentStatus(message).await()
                    return@async
                }
                message.id?.let { id ->
                    if (!checkCommentQueue.contains(id)) {
                        checkCommentQueue.add(id)
                    }
                }
                responseQueue.enqueue(
                    object : Callback<CheckCommentResponse?> {
                        override fun onResponse(
                            call: Call<CheckCommentResponse?>,
                            response: Response<CheckCommentResponse?>
                        ) {
                            val checkCommentResponse = response.body()
                            checkCommentResponse?.let { response1 ->
                                response1._header_.let { header ->
                                    header.success?.let {
                                        if (it) {
                                            message.id?.let { id ->
                                                checkCommentQueue.remove(id)
                                                isTokenNeedRefresh = false
                                                tempFacebookAlreadyCheckStatus.add(id)
                                                response1.result?.let { result ->
                                                    MessageReference.updateFacebookCommentStatus(
                                                        room.id,
                                                        id,
                                                        FacebookCommentStatus.of(
                                                            result.commentStatus
                                                        )
                                                    )
                                                    MessageReference.updateFacebookPrivateReplyStatus(
                                                        room.id,
                                                        id,
                                                        !result.canPrivateReply
                                                    )
                                                    MessageReference.updateFacebookPostStatus(
                                                        room.id,
                                                        id,
                                                        FacebookPostStatus.of(
                                                            result.postStatus
                                                        )
                                                    )
                                                    message.facebookCommentStatus =
                                                        FacebookCommentStatus.of(
                                                            result.commentStatus
                                                        )
                                                    message.isFacebookPrivateReplied =
                                                        !result.canPrivateReply
                                                    message.facebookPostStatus =
                                                        FacebookPostStatus.of(result.postStatus)
                                                    CoroutineScope(Dispatchers.Main).launch {
                                                        sendUpdateFacebookStatus.emit(message)
                                                    }
                                                }
                                            }
                                        } else {
                                            header.errorCode?.let { errorCode ->
                                                val code = ErrCode.of(errorCode)
                                                if (ErrCode.TOKEN_INVALID_or_TOKEN_REQUIRED.contains(code)) {
                                                    isTokenNeedRefresh = true
                                                    refreshToken(message)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        override fun onFailure(
                            call: Call<CheckCommentResponse?>,
                            t: Throwable
                        ) {
                            CELog.e("checkCommentStatus", t)
                        }
                    }
                )
            }
        }

    private fun refreshToken(message: MessageEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            ApiManager.doTokenApply(
                application,
                false,
                object : TokenApplyRequest.Listener {
                    override fun allCallBack(
                        isRefresh: Boolean,
                        status: Boolean
                    ) {
                    }

                    override fun onSuccess(
                        isRefresh: Boolean,
                        resp: AileTokenApply.Resp
                    ) {
                        isTokenNeedRefresh = false
                        AppConfig.tokenForNewAPI = resp.tokenId
                        CoroutineScope(Dispatchers.IO).launch {
                            checkCommentStatus(message).await()
                        }
                    }

                    override fun onFailed(
                        errorCode: ErrCode?,
                        errorMessage: String?
                    ) {
                    }

                    override fun onCallData(
                        roomId: String?,
                        meetingId: String?,
                        callKey: String?
                    ) {
                    }
                }
            )
        }

    /**
     * 送出截圖分享
     */
    fun sendScreenshotsImageToRooms(
        roomIds: List<String?>,
        filePath: String?
    ) = viewModelScope.launch(Dispatchers.IO) {
        val paths = PictureParse.parsePath(application, filePath)
        val path = DaVinci.with().imageLoader.getAbsolutePath(paths[0])
        FileService.uploadFile(
            application,
            true,
            tokenId,
            Media.findByFileType(
                paths[0]
            ),
            path,
            paths[0],
            object : AServiceCallBack<UploadManager.FileEntity, RefreshSource>() {
                override fun onProgress(
                    progress: Float,
                    total: Long
                ) {
                }

                override fun complete(
                    fileEntity: UploadManager.FileEntity,
                    refreshSource: RefreshSource
                ) {
                    for (i in roomIds.indices) {
                        val roomId = roomIds[i]
                        val mMessageId = generateMessageId()
                        ChatService
                            .getInstance()
                            .sendImageMessage(roomId, mMessageId, "", fileEntity)
                    }
                }

                override fun error(errorMessage: String) {
                    Log.e("sendScreenshotsImageToRooms", errorMessage)
                }
            }
        )
    }

    fun sendGifImg(
        davitPath: String,
        localPath: String,
        width: Int,
        height: Int
    ) {
        sendGifImg(davitPath, localPath, width, height, false)
    }

    fun sendGifImg(
        davitPath: String,
        localPath: String,
        width: Int,
        height: Int,
        isFacebookReplyPublic: Boolean
    ) = viewModelScope.launch(Dispatchers.IO) {
        roomEntity?.let { room ->
            if (checkIsSnatch()) {
                val agentName = UserProfileReference.findAccountName(null, room.serviceNumberAgentId)
                sendSnatchedAgent.emit(SendImageGifFormat(agentName, davitPath, localPath, width, height))
                return@launch
            }
            if (isFacebookReply()) {
                val path = DaVinci.with().imageLoader.getAbsolutePath(davitPath)
                sendFacebookAttachmentReply(isFacebookReplyPublic, davitPath, path)
                CoroutineScope(Dispatchers.Main).launch {
                    sendShowSendVideoProgress.emit("圖片發送中")
                }
                return@launch
            }

            if (recordMode) {
                loadMsgDBDesc()
                recordMode = false
            }
            val messageId = generateMessageId()
            val msg =
                MsgKitAssembler.assembleSendImageMessage(
                    room.id,
                    messageId,
                    mSelfAccount.id,
                    mSelfAccount.avatarId,
                    mSelfAccount.nickName,
                    localPath,
                    localPath,
                    width,
                    height
                )
            val imageContent = msg.content() as ImageContent
            imageContent.width = width
            imageContent.height = height
            showMessage(msg)
            setReplyMsg(msg)
            val path = DaVinci.with().imageLoader.getAbsolutePath(davitPath)
            FileService.uploadFile(
                application,
                true,
                tokenId,
                Media.findByFileType(davitPath),
                path,
                davitPath,
                object : AServiceCallBack<UploadManager.FileEntity, RefreshSource>() {
                    override fun complete(
                        entity: UploadManager.FileEntity?,
                        refreshSource: RefreshSource
                    ) {
                        entity?.let { fileEntity ->
                            val message = MessageReference.findById(messageId)
                            message?.let { msg ->
                                if (msg.content() is ImageContent) {
                                    val ic = msg.content() as ImageContent

                                    ic.url = fileEntity.url
                                    ic.size = fileEntity.size
                                    ic.height = fileEntity.height
                                    ic.width = fileEntity.width

                                    ic.thumbnailUrl = fileEntity.thumbnailUrl
                                    ic.thumbnailSize = fileEntity.thumbnailSize
                                    ic.thumbnailHeight = fileEntity.thumbnailHeight
                                    ic.thumbnailWidth = fileEntity.thumbnailWidth

                                    msg.content = ic.toStringContent()
                                    MessageReference.save(msg.roomId, msg)
                                }
                            }

                            if (isThemeOpen) {
                                themeMessage?.let { theme ->
                                    ChatService.getInstance().sendReplyImageMessage(
                                        room.id,
                                        messageId,
                                        theme.id,
                                        entity
                                    )
                                }
                            } else {
                                ChatService
                                    .getInstance()
                                    .sendImageMessage(room.id, messageId, "", fileEntity)
                            }
                            CoroutineScope(Dispatchers.Main).launch {
                                sendSetThemeOpen.emit(false)
                                sendUpdateSendVideoProgress.emit(Unit)
                            }
                        } ?: run {
                            error("上傳失敗")
                        }
                    }

                    override fun onProgress(
                        progress: Float,
                        total: Long
                    ) {
                        CoroutineScope(Dispatchers.Main).launch {
                            sendUpdateMsgProgress.emit(Pair(messageId, (progress * 100).toInt()))
                        }
                    }

                    override fun error(message: String) {
                        CELog.e(message)
                        CoroutineScope(Dispatchers.Main).launch {
                            sendUpdateSendVideoProgress.emit(Unit)
                            sendUpdateMsgStatus.emit(Triple(room.id, messageId, MessageStatus.FAILED))
                        }
                    }
                }
            )
        }
    }

    fun sendFile(path: String) {
        sendFile(path, false)
    }

    fun sendFile(
        path: String,
        isFacebookReplyPublic: Boolean
    ) = viewModelScope.launch(Dispatchers.IO) {
        roomEntity?.let {
            // 確認是否是強制接手
            if (checkIsSnatch()) {
                val agentName =
                    UserProfileReference.findAccountName(null, it.serviceNumberAgentId)
                sendSnatchedAgent.emit(SendFileFormat(agentName, path))
                return@launch
            }

            if (recordMode) {
                loadMsgDBDesc()
                recordMode = false
            }
            val messageId = generateMessageId()
            var size = 0
            val file: File
            var fileName: String? = ""
            try {
                file = File(path)
                fileName = file.name
                val fis = FileInputStream(file)
                size = fis.available()
            } catch (e: java.lang.Exception) {
                CELog.e("fileInputStreamError$e")
            }
            if (isFacebookReply()) {
                sendFacebookAttachmentReply(isFacebookReplyPublic, fileName ?: "", path)
                return@launch
            }
            val baseUrl = TokenPref.getInstance(application).currentTenantUrl
            val msg =
                MsgKitAssembler.assembleSendFileMessage(
                    baseUrl,
                    it.id,
                    messageId,
                    mSelfAccount.id,
                    mSelfAccount.avatarId,
                    mSelfAccount.nickName,
                    path,
                    fileName,
                    size,
                    ""
                )
            showMessage(msg)
            setReplyMsg(msg)
            UploadManager
                .getInstance()
                .uploadFile(
                    application,
                    messageId,
                    tokenId,
                    path,
                    object : OnFileUploadListener {
                        override fun onUploadSuccess(
                            messageId: String,
                            entity: UploadManager.FileEntity
                        ) {
                            val message = MessageReference.findById(messageId)
                            message?.let { msg ->
                                if (msg.content() is FileContent) {
                                    (msg.content() as FileContent).url = entity.url
                                }

                                CoroutineScope(Dispatchers.Main).launch {
                                    sendUpdateSendVideoProgress.emit(Unit)
                                }
                                uploadSuccess(
                                    messageId,
                                    entity.name,
                                    entity.size,
                                    path,
                                    entity.url,
                                    entity.mD5
                                )
                            }
                        }

                        override fun onUploadIng(
                            messageId: String,
                            progress: Int,
                            total: Long
                        ) {
                            CoroutineScope(Dispatchers.Main).launch {
                                sendUpdateMsgProgress.emit(Pair(messageId, progress))
                            }
                        }

                        override fun onUploadFailed(
                            reason: String,
                            messageId: String
                        ) {
                            CoroutineScope(Dispatchers.Main).launch {
                                sendUpdateSendVideoProgress.emit(Unit)
                                sendUpdateMsgStatus.emit(Triple(it.id, messageId, MessageStatus.FAILED))
                            }
                        }
                    }
                )
        }
    }

    private fun uploadSuccess(
        messageId: String?,
        name: String?,
        size: Int,
        localPath: String?,
        url: String?,
        md5: String?
    ) {
        roomEntity?.let { room ->
            url?.let {
                if (isThemeOpen) {
                    themeMessage?.let { message ->
                        ChatService.getInstance().sendReplyFileMessage(
                            room.id,
                            messageId,
                            name,
                            size,
                            it,
                            message.id,
                            md5
                        )
                    }
                } else {
                    ChatService.getInstance().sendFileMessage(
                        room.id,
                        messageId,
                        name,
                        size,
                        localPath,
                        it,
                        "",
                        md5
                    )
                }
            }
            CoroutineScope(Dispatchers.Main).launch {
                sendUpdateSendVideoProgress.emit(Unit)
                sendSetThemeOpen.emit(false)
            }
        }
    }

    fun switchIdentity(servicesIdentityListResponse: ServicesIdentityListResponse) {
        mSelfAccount.avatarId = servicesIdentityListResponse.avatarId
        mSelfAccount.name = servicesIdentityListResponse.name
    }

    fun snatchRobot(data: Any) =
        viewModelScope.launch(Dispatchers.IO) {
            roomEntity?.let { room ->
                chatRepository.snatchRobot(room.id).collect {
                    when (it) {
                        is ApiResult.Success -> {
                            onSnatchRobotSuccess.emit(Pair(true, data))
                        }

                        is ApiResult.Failure -> {
                            onSnatchRobotSuccess.emit(Pair(false, data))
                        }

                        else -> {
                            // nothing
                        }
                    }
                }
            }
        }

    fun doRobotSnatchByAgent(sendData: SendData) =
        viewModelScope.launch(Dispatchers.IO) {
            roomEntity?.let {
                ChatRoomService.getInstance().snatchRobotServicingAPI(
                    application,
                    it.id,
                    object : RoomRecentCallBack<RobotServiceResponse, RefreshSource> {
                        override fun error(message: String) {
                            CELog.e("doRobotSnatchByAgent error = $message")
                            CoroutineScope(Dispatchers.Main).launch {
                                sendDoChatRoomSnatchByAgent.emit(Pair(false, sendData))
                            }
                        }

                        override fun complete(
                            robotServiceResponse: RobotServiceResponse,
                            refreshSource: RefreshSource
                        ) {
                            CoroutineScope(Dispatchers.Main).launch {
                                sendDoChatRoomSnatchByAgent.emit(Pair(robotServiceResponse.success, sendData))
                            }
                        }

                        override fun finish() {
                        }
                    }
                )
            }
        }

    fun clearSession() {
        roomEntity?.let {
            RepairMessageService.stop(application, it.id)
        }
    }

    /**
     * 發送文字 Facebook 私人回覆
     * @param message 回覆的訊息
     * @param content 回覆的內容
     */
    fun sendFacebookPrivateReply(
        message: MessageEntity,
        content: SendData
    ) {
        roomEntity?.let { room ->
            val facebookTag =
                JsonHelper.getInstance().from(
                    message.tag,
                    FacebookTag::class.java
                )
            val sendFacebookCommentRequest =
                SendFacebookCommentRequest(
                    facebookTag.data.postId,
                    facebookTag.data.commentId,
                    "Add",
                    "Text",
                    content.content
                )
            provideRetrofit(application)
                .create(
                    tw.com.chainsea.chat.view.chat.ChatService::class.java
                ).sendFacebookPrivateComment(sendFacebookCommentRequest)
                .enqueue(
                    object : Callback<CommonResponse<SendFacebookCommentResponse>> {
                        override fun onResponse(
                            call: Call<CommonResponse<SendFacebookCommentResponse>>,
                            response: Response<CommonResponse<SendFacebookCommentResponse>>
                        ) {
                            val commonResponse: CommonResponse<*>? = response.body()
                            response.body()?.let { responseBody ->
                                responseBody._header_?.let { header ->
                                    header.success?.let { isSuccess ->
                                        if (!isSuccess) {
                                            MessageReference.updateFacebookPrivateReplyStatus(
                                                room.id,
                                                message.id,
                                                true
                                            )
                                            CoroutineScope(Dispatchers.Main).launch {
                                                sendOnSendFacebookImageReplyFailed.emit(application.getString(R.string.facebook_already_private_replied))
                                            }
                                            disPlayMessageFromDatabase()
                                        }
                                    }
                                }
                            }
                            Log.d("sendFacebookPrivateReply", "onResponse")
                        }

                        override fun onFailure(
                            call: Call<CommonResponse<SendFacebookCommentResponse>>,
                            t: Throwable
                        ) {
                            ChatService.getInstance().handleSendMessageFail(room.id, message.id)
                            Log.e("sendFacebookPrivateReply", t.message ?: "")
                        }
                    }
                )
        }
    }

    /**
     * 發送文字 Facebook 公開回覆
     * @param message 回覆的訊息
     * @param content 回覆的內容
     */
    fun sendFacebookPublicReply(
        message: MessageEntity,
        content: SendData
    ) = viewModelScope.launch(Dispatchers.IO) {
        val facebookTag =
            JsonHelper.getInstance().from(
                message.tag,
                FacebookTag::class.java
            )
        val sendFacebookCommentRequest =
            SendFacebookCommentRequest(
                facebookTag.data.postId,
                facebookTag.data.commentId,
                "Add",
                "Text",
                content.content
            )
        provideRetrofit(application)
            .create(
                tw.com.chainsea.chat.view.chat.ChatService::class.java
            ).sendFacebookPublicComment(sendFacebookCommentRequest)
            .enqueue(
                object : Callback<CommonResponse<SendFacebookCommentResponse>> {
                    override fun onResponse(
                        call: Call<CommonResponse<SendFacebookCommentResponse>>,
                        response: Response<CommonResponse<SendFacebookCommentResponse>>
                    ) {
                        Log.d("sendFacebookPublicReply", "onResponse")
                    }

                    override fun onFailure(
                        call: Call<CommonResponse<SendFacebookCommentResponse>>,
                        t: Throwable
                    ) {
                        roomEntity?.let {
                            ChatService.getInstance().handleSendMessageFail(it.id, message.id)
                        }
                        CELog.e("sendFacebookPublicReply", t.message)
                    }
                }
            )
    }

    fun sendButtonClicked(sendData: SendData) =
        viewModelScope.launch(Dispatchers.IO) {
            roomEntity?.let {
                val content = sendData.content
                // 確認是否是強制接手
                if (checkIsSnatch()) {
                    val agentName =
                        UserProfileReference.findAccountName(null, it.serviceNumberAgentId)
                    sendSnatchedAgent.emit(SendButtonClickedFormat(agentName, sendData))
                    return@launch
                }

                when (sendData.type) {
                    MessageType.TEXT -> {
                        if (content.trim { it <= ' ' }.isEmpty()) { // filter empty message
                            CoroutineScope(Dispatchers.Main).launch {
                                sendShowToast.emit(R.string.text_can_not_send_empty_message)
                            }
                            return@launch
                        }

                        if (content.trim { it <= ' ' }.length > 10000) {
                            CoroutineScope(Dispatchers.Main).launch {
                                sendShowErrorToast.emit("您輸入的字數超過傳送限制。(" + content.trim { it <= ' ' }.length + ">10000)")
                            }
                            return@launch
                        }
                        sendText(content)
                    }

                    MessageType.AT -> {
                        val atContent = sendData.content
                        sendAtText(atContent)
                        sendAtMessageToService(atContent)
                    }

                    MessageType.TEMPLATE -> {
                        val templateContent = sendData.content
                        sendTemplate(templateContent)
                    }

                    else -> {}
                }
                CoroutineScope(Dispatchers.Main).launch {
                    sendClearTypedMessage.emit(Unit)
                }
            }
        }

    private fun sendTemplate(content: String) =
        viewModelScope.launch(Dispatchers.IO) {
            roomEntity?.let { room ->
                if (recordMode) {
                    loadMsgDBDesc()
                    recordMode = false
                }

                val messageId = generateMessageId()
                val msg =
                    MsgKitAssembler.assembleSendTemplateMessage(
                        room.id,
                        messageId,
                        selfUserId,
                        mSelfAccount.avatarId,
                        JsonHelper.getInstance().from(content, TemplateContent::class.java),
                        mSelfAccount.nickName
                    )
                showMessage(msg)
                setReplyMsg(msg)

                if (isThemeOpen) {
                    themeMessage?.let { message ->
                        ChatService.getInstance().sendReplyMessage(
                            room.id,
                            messageId,
                            MessageType.TEMPLATE,
                            content,
                            message.id
                        )
                    }
                } else {
                    ChatService
                        .getInstance()
                        .sendMessage(room.id, messageId, MessageType.TEMPLATE, content, "")
                }
                CoroutineScope(Dispatchers.Main).launch {
                    sendSetThemeOpen.emit(false)
                }
            }
        }

    private fun sendText(content: String) =
        viewModelScope.launch(Dispatchers.IO) {
            roomEntity?.let { room ->
                if (recordMode) {
                    loadMsgDBDesc()
                    recordMode = false
                }
                val newContent =
                    if ("{}" == content) {
                        "%$content%"
                    } else {
                        content
                    }

                // 这里生成的是UUID字符串
                val messageId = generateMessageId()
                val msg =
                    MsgKitAssembler.assembleSendTextMessage(
                        room.id,
                        messageId,
                        selfUserId,
                        mSelfAccount.avatarId,
                        newContent,
                        mSelfAccount.nickName
                    )
                showMessage(msg)
                setReplyMsg(msg)

                if (isThemeOpen) {
                    themeMessage?.let { message ->
                        ChatService.getInstance().sendReplyMessage(
                            room.id,
                            messageId,
                            MessageType.TEXT,
                            newContent,
                            message.id
                        )
                    }
                } else {
//                if (isChildRoomOpen) {
//                    val id: String = iView.getChildRoomId()
//                    assert(Strings.isNullOrEmpty(id))
//                    ChatService.getInstance()
//                        .sendMessage(id, messageId, MessageType.TEXT, newContent, "")
//                } else {
                    ChatService
                        .getInstance()
                        .sendMessage(room.id, messageId, MessageType.TEXT, newContent, "")
//                }
                }
                CoroutineScope(Dispatchers.Main).launch {
                    sendSetThemeOpen.emit(false)
                }
            }
        }

    /**
     * 送出AT 資料
     */
    private fun sendAtText(atContent: String) =
        viewModelScope.launch(Dispatchers.IO) {
            roomEntity?.let {
                if (recordMode) {
                    loadMsgDBDesc()
                    recordMode = false
                }
                // 这里生成的是UUID字符串
                val messageId = generateMessageId()
                val msg =
                    MsgKitAssembler.assembleSendAtMessage(
                        it.id,
                        messageId,
                        selfUserId,
                        mSelfAccount.avatarId,
                        atContent,
                        mSelfAccount.nickName
                    )
                showMessage(msg)
                setReplyMsg(msg)
                if (isThemeOpen) {
                    themeMessage?.let { message ->
                        ChatService.getInstance().sendReplyMessage(
                            it.id,
                            messageId,
                            MessageType.AT,
                            atContent,
                            message.id
                        )
                    }
                } else {
                    ChatService
                        .getInstance()
                        .sendMessage(it.id, messageId, MessageType.AT, atContent, "")
                }
                CoroutineScope(Dispatchers.Main).launch {
                    sendSetThemeOpen.emit(false)
                }
            }
        }

    /**
     * 發送at的資訊給 service
     */
    private fun sendAtMessageToService(atMessage: String) =
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val typeToken = object : TypeToken<List<AtMentionComponent?>?>() {}.type
                val atMentionComponents =
                    JsonHelper.getInstance().from<List<AtMentionComponent>>(atMessage, typeToken)
                val isAtAll = AtomicBoolean(false)
                val atUserId = AtomicReference<List<String>>(Lists.newArrayList())
                atMentionComponents.forEach(
                    Consumer { atMentionComponent: AtMentionComponent ->
                        if ("All" == atMentionComponent.objectType) {
                            isAtAll.set(true)
                        } else {
                            atUserId.set(atMentionComponent.userIds)
                        }
                    }
                )
                val chatService =
                    provideRetrofit(application).create(
                        tw.com.chainsea.chat.view.chat.ChatService::class.java
                    )
                val callback: Callback<CommonResponse<Any>> =
                    object : Callback<CommonResponse<Any>> {
                        override fun onResponse(
                            p0: Call<CommonResponse<Any>>,
                            p1: Response<CommonResponse<Any>>
                        ) {
                        }

                        override fun onFailure(
                            p0: Call<CommonResponse<Any>>,
                            p1: Throwable
                        ) {
                        }
                    }
                roomEntity?.let {
                    if (isAtAll.get()) {
                        chatService
                            .sendAtMessage(SendAtMessageRequest(it.id, isAtAll.get()))
                            .enqueue(callback)
                    } else {
                        chatService
                            .sendAtMessage(SendAtMessageRequest(it.id, atUserId.get()))
                            .enqueue(callback)
                    }
                }
            } catch (_: java.lang.Exception) {
            }
        }

    fun sendVoice(
        path: String,
        duration: Int
    ) {
        sendVoice(path, duration, false)
    }

    /**
     * 送出語音訊息
     * ersion 1.9.1
     */
    fun sendVoice(
        path: String,
        duration: Int,
        isFacebookReplyPublic: Boolean
    ) = viewModelScope.launch(Dispatchers.IO) {
        roomEntity?.let { room ->
            // 確認是否是強制接手
            if (checkIsSnatch()) {
                val agentName =
                    UserProfileReference.findAccountName(null, room.serviceNumberAgentId)
                sendSnatchedAgent.emit(SendVoiceFormat(agentName, path, duration))
                return@launch
            }

            if (checkIsRobotSnatch()) {
                showSnatchRobotDialog.emit(SendVoiceFormat(path = path, duration = duration))
                return@launch
            }

            if (isFacebookReply()) {
                sendFacebookAttachmentReply(isFacebookReplyPublic, "", path)
                return@launch
            }

            if (recordMode) {
                loadMsgDBDesc()
                recordMode = false
            }
            val messageId = getNameWithoutExt(path)
            val newPath =
                if (path.startsWith("/openapi")) {
                    TokenPref.getInstance(application).currentTenantUrl + ApiPath.ROUTE + path
                } else {
                    path
                }
            val msg =
                MsgKitAssembler.assembleSendVoiceMessage(
                    room.id,
                    messageId,
                    mSelfAccount.id,
                    mSelfAccount.nickName,
                    mSelfAccount.avatarId,
                    duration,
                    newPath
                )
            showMessage(msg)
            setReplyMsg(msg)

            UploadManager
                .getInstance()
                .uploadVoice(
                    application,
                    messageId,
                    tokenId,
                    path,
                    object : OnVoiceUploadListener {
                        override fun onUploadSuccess(
                            messageId: String,
                            entity: UploadManager.FileEntity
                        ) {
                            if (isThemeOpen) {
                                themeMessage?.let { message ->
                                    ChatService.getInstance().sendReplyVoiceMessage(
                                        room.id,
                                        messageId,
                                        duration,
                                        entity.url,
                                        message.id,
                                        entity.mD5
                                    )
                                }
                            } else {
                                ChatService.getInstance().sendVoiceMessage(
                                    room.id,
                                    messageId,
                                    duration.toDouble(),
                                    entity.url,
                                    "",
                                    entity.mD5
                                )
                            }
                            CoroutineScope(Dispatchers.Main).launch {
                                sendSetThemeOpen.emit(false)
                            }
                        }

                        override fun onUploadFailed(reason: String) {
                            CELog.e(reason)
                            CoroutineScope(Dispatchers.Main).launch {
                                sendUpdateMsgStatus.emit(Triple(room.id, messageId, MessageStatus.FAILED))
                            }
                        }
                    }
                )
        }
    }

    fun sendSticker(
        stickerId: String?,
        packageId: String?
    ) {
        roomEntity?.let { room ->
            if (recordMode) {
                loadMsgDBDesc()
                recordMode = false
            }
            val messageId = generateMessageId()
            val msg =
                MsgKitAssembler.assembleSendStickerMessage(
                    room.id,
                    messageId,
                    mSelfAccount.id,
                    mSelfAccount.avatarId,
                    mSelfAccount.nickName,
                    stickerId,
                    packageId
                )
            showMessage(msg)
            setReplyMsg(msg)

            if (isThemeOpen) {
                themeMessage?.let {
                    ChatService.getInstance().sendReplyStickerMessage(
                        room.id,
                        messageId,
                        stickerId,
                        packageId,
                        it.id
                    )
                }
            } else {
                ChatService
                    .getInstance()
                    .sendStickerMessage(room.id, messageId, stickerId, packageId, "")
            }
            CoroutineScope(Dispatchers.Main).launch {
                sendSetThemeOpen.emit(false)
            }
        }
    }

    fun sendNoticeRead() =
        viewModelScope.launch(Dispatchers.IO) {
            roomEntity?.let { room ->
                if (mUnReadMsgIds.size > 0) {
                    ChatMessageService.doMessageReadAllByRoomId(
                        application,
                        room,
                        room.unReadNum,
                        mUnReadMsgIds,
                        false
                    )
                    mUnReadMsgIds.clear()
                }
            }
        }

    fun appendOfflineMessage(messageIds: MutableList<String>) =
        viewModelScope.launch(Dispatchers.IO) {
            roomEntity?.let {
                chatRepository.getMessageListFromIds(it.id, messageIds).collect {
                    when (it) {
                        is ApiResult.Success -> {
                            val messageList = it.data
                            mainMessageData.addAll(messageList)
                            val dateMessageList = setTimeTag(mainMessageData)
                            mainMessageDataFlow.emit(Pair(false, dateMessageList.toMutableList()))
                        }

                        else -> {
                        }
                    }
                }
            }
        }

    fun messageListRequestAsc(message: MessageEntity?) =
        viewModelScope.launch(Dispatchers.IO) {
            roomEntity?.let { room ->
                message?.let { msg ->
                    ChatMessageService.getChatMessageEntities(
                        application,
                        room.id,
                        null,
                        msg.id,
                        50,
                        Sort.ASC,
                        object : ServiceCallBack<List<MessageEntity>, RefreshSource> {
                            override fun complete(
                                entities: List<MessageEntity>,
                                refreshSource: RefreshSource
                            ) {
                                if (entities.isNotEmpty()) {
                                    CELog.d("Enter the chat room, have data locally, read messages from the server:" + entities.size)
                                    val newMsgIds: MutableList<String?> = Lists.newArrayList()
                                    entities.forEach {
                                        if (MessageStatus.READ != it.status) {
                                            newMsgIds.add(it.id)
                                        }
                                        displayMainMessageLogic(isNew = false, scrollToBottom = true, msg)
                                    }
                                    if (newMsgIds.isNotEmpty()) {
                                        ChatMessageService.doMessageReadAllByRoomId(
                                            application,
                                            room,
                                            room.unReadNum,
                                            newMsgIds,
                                            false
                                        )
                                    }
                                }
                            }

                            override fun error(message: String) {
                                CELog.e(message)
                            }
                        }
                    )
                }
            }
        }

    fun dimQueryLastReadMsgForInternet() =
        viewModelScope.launch(Dispatchers.IO) {
            roomEntity?.let { room ->
                val iMessages = DBManager.getInstance().dimQueryLastReadMsg(room.id)
                if (iMessages.isNotEmpty()) {
                    messageListRequestAsc(iMessages[0])
                }
            }
        }

    fun onReceive(receiverMessage: MessageEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            roomEntity?.let { room ->
                if (mainMessageData.contains(receiverMessage)) {
                    mainMessageData[mainMessageData.indexOf(receiverMessage)] = receiverMessage
                    MessageReference.save(room.id, receiverMessage)
                    mainMessageDataFlow.emit(Pair(false, mainMessageData.toMutableList()))
                } else {
                    // Send read notification
                    val sessionId = receiverMessage.roomId
                    // Handle the online and offline status of the service account, identify the temporary string, and add type to the jocket response data format to be repaired

                    /**
                     * 處理用戶在多渠道服務號是否上線
                     * 檢查為服務號聊天室，及狀態欄位為顯示狀態
                     */
                    if (room.isService(selfUserId)) {
                        // EVAN_FLAG 2019-11-19 (1.8.0) 多渠道進線錯亂影響到其它聊天室
                        if (room.id == sessionId) {
                            doAppointStatus(room.id)
                        }
                    }

                    if (room.id == sessionId) {
                        mReceivedNoticeIds.clear()
                        mReceivedNoticeIds.add(receiverMessage.id)
                        if (isVisible) {
                            if (room.type == ChatRoomType.friend) {
                                if (receiverMessage.senderId != selfUserId) {
                                    // After receiving the message, set all the previously arrived messages as read
                                    val iMessages =
                                        DBManager
                                            .getInstance()
                                            .queryMessagesByMsgStatus(room.id, MessageStatus.RECEIVED)
                                    iMessages?.let {
                                        for (msg in iMessages) {
                                            mReceivedNoticeIds.add(msg.id)
                                            msg.readedNum = 1
                                            msg.status = MessageStatus.READ
                                        }
                                    }
                                    ApiManager.doMessagesRead(
                                        application,
                                        room.id,
                                        mReceivedNoticeIds,
                                        object : ApiListener<String> {
                                            override fun onSuccess(s: String) {
                                                if (iMessages != null && iMessages.size > 0) {
                                                    for (msg in iMessages) {
                                                        msg.readedNum = 1
                                                        msg.status = MessageStatus.READ
                                                        MessageReference.save(room.id, msg)
                                                    }
                                                }
                                            }

                                            override fun onFailed(errorMessage: String) {
                                                CELog.d(errorMessage)
                                            }
                                        }
                                    )
                                    receiverMessage.readedNum = 1
                                    receiverMessage.status = MessageStatus.READ
                                } else {
                                    val status: MessageStatus? = constructStatus(receiverMessage)
                                    receiverMessage.status = status
                                }
                            } else {
                                receiverMessage.status = MessageStatus.READ
                                ChatMessageService.doMessageReadAllByRoomId(
                                    application,
                                    room,
                                    room.unReadNum,
                                    mReceivedNoticeIds,
                                    false
                                )
                            }
                        } else {
                            mUnReadMsgIds.add(receiverMessage.id!!)
                        }
                        upDateSession()
                        MessageReference.save(sessionId, receiverMessage)
                        if (!recordMode) {
                            displayMainMessageLogic(
                                isNew = true,
                                scrollToBottom = true,
                                receiverMessage
                            )
                        }
                    }
                }
            }
        }

    private fun constructStatus(message: MessageEntity) =
        when {
            message.sendNum!! > 0 -> MessageStatus.SUCCESS
            (message.receivedNum ?: 0) > 0 -> MessageStatus.RECEIVED
            (message.readedNum ?: 0) > 0 -> MessageStatus.READ
            else -> null
        }

    fun upDateSession() =
        viewModelScope.launch(Dispatchers.IO) {
            roomEntity?.let {
                ChatRoomReference.getInstance().save(it)
                roomEntity =
                    ChatRoomReference
                        .getInstance()
                        .findById2(selfUserId, it.id, true, true, true, true, true)
                setRoomType()
                EventBusUtils.sendEvent(
                    EventMsg<Any>(
                        MsgConstant.SESSION_UPDATE_FILTER,
                        JsonHelper.getInstance().toJson(roomEntity)
                    )
                )
            }
        }

    fun refreshFacebookComment(
        message: MessageEntity?,
        facebookCommentStatus: FacebookCommentStatus?
    ) = viewModelScope.launch(Dispatchers.IO) {
        roomEntity?.let { room ->
            message?.let { message ->
                chatRepository.getMessageItem(room.id, message.id!!).collect {
                    when (it) {
                        is ApiResult.Success -> {
                            val responseMessage = it.data
                            MessageReference.updateFacebookMessageContent(responseMessage)
                            message.content = responseMessage.content
                            message.tag = responseMessage.tag
                            message.facebookCommentStatus = facebookCommentStatus!!
                            sendUpdateFacebookStatus.emit(responseMessage)
                            mainMessageData.forEach {
                                if (it.id == message.id) return@forEach
                                if (it.themeId == null || it.themeId!!.isEmpty()) return@forEach
                                if (it.themeId == message.id) {
                                    it.nearMessageContent = responseMessage.content
                                    sendUpdateFacebookStatus.emit(it)
                                }
                            }
                        }

                        is ApiResult.Failure -> {
                            CELog.e("refreshFacebookComment Failure", it.errorMessage.errorMessage)
                        }

                        else -> {
                            // nothing
                        }
                    }
                }
            }
        }
    }

    fun updateFacebookPostStatus(message: MessageEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            if (message.tag == null) return@launch
            if (message.tag!!.isEmpty()) return@launch
            val facebookTag =
                JsonHelper.getInstance().from(
                    message.tag,
                    FacebookTag::class.java
                )
            if (facebookTag == null) return@launch
            if (facebookTag.data == null) return@launch
            val postId = facebookTag.data.postId
            for (i in mainMessageData.indices) {
                val replyMessage = mainMessageData[i]
                if (replyMessage.id == message.id) continue
                if (replyMessage.tag == null) continue
                if (replyMessage.tag!!.isEmpty()) continue
                val facebookReplyTag =
                    JsonHelper.getInstance().from(
                        replyMessage.tag,
                        FacebookTag::class.java
                    )
                if (facebookReplyTag == null) continue
                if (facebookReplyTag.data == null) continue
                if (facebookReplyTag.data.postId == postId) {
                    CoroutineScope(Dispatchers.Main).launch {
                        sendUpdateFacebookStatus.emit(replyMessage)
                    }
                }
            }
        }

    /**
     * 新版刪除訊息
     * */
    fun deleteMessages(message: MessageEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            roomEntity?.let {
                MessageReference.deleteById(message.id)
                val lastMessageEntity = MessageReference.findMessageByRoomIdAndStatusAndLimitOne(null, it.id, MessageStatus.getValidStatus(), MessageReference.Sort.DESC)
                it.lastMessage = lastMessageEntity
                val index = mainMessageData.indexOf(message)
                onMessageDeleted.emit(index)
                mainMessageData.removeIf { it.id == message.id }
                mainMessageDataFlow.emit(Pair(false, mainMessageData.toMutableList()))
                EventBusUtils.sendEvent(EventMsg<Any?>(MsgConstant.CHANGE_LAST_MESSAGE, it.id))
            }
        }

    /**
     * 送出 Business 資料
     */
    fun sendBusiness(businessContent: BusinessContent) =
        viewModelScope.launch(Dispatchers.IO) {
            roomEntity?.let { room ->
                // 確認是否是強制接手
                if (checkIsSnatch()) {
                    val agentName =
                        UserProfileReference.findAccountName(null, room.serviceNumberAgentId)
                    sendSnatchedAgent.emit(SendBusinessFormat(agentName, businessContent))
                    return@launch
                }

                if (recordMode) {
                    loadMsgDBDesc()
                    recordMode = false
                }
                // 这里生成的是UUID字符串
                val messageId = generateMessageId()
                val msg =
                    MsgKitAssembler.assembleSendBusinessMessage(
                        room.id,
                        messageId,
                        mSelfAccount,
                        businessContent
                    )
                showMessage(msg)
                setReplyMsg(msg)
                if (!isThemeOpen) {
                    ChatService.getInstance().sendMessage(
                        room.id,
                        messageId,
                        MessageType.BUSINESS,
                        businessContent.toSendContent(),
                        ""
                    )
                }
                CoroutineScope(Dispatchers.Main).launch {
                    sendSetThemeOpen.emit(false)
                }
            }
        }

    fun searchMoreMsg(entity: MessageEntity?) =
        viewModelScope.launch(Dispatchers.IO) {
            roomEntity?.let { room ->
                entity?.let {
                    val entities =
                        MessageReference.findByRoomIdAndTypeAndSortAndLimitAndInnerJoniAliasName(
                            room.id,
                            it.sendTime - 1,
                            room.type,
                            MessageReference.Sort.DESC,
                            200
                        )
                    if (entities.isEmpty() || entities.size <= 1) {
                        // message/list抓後台資料
                        messageListRequestDesc(room.id, room.type, it, 200)
                        sendShowNoMoreMessage.emit(Unit)
                    } else if (entities.size <= 200) {
                        entities.sort()
                        sendOnRefreshMore.emit(Pair(entities, entity))
                        messageListRequestDesc(
                            room.id,
                            room.type,
                            entities[0],
                            200
                        )
                    } else {
                        sendOnRefreshMore.emit(Pair(entities, entity))
                    }
                } ?: run {
                    messageListRequestDesc(room.id, room.type, "")
                    return@launch
                }
            }
        }

    /**
     * 嘗試重新送出訊息
     * version 1.9.1
     */
    fun retrySend(message: MessageEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            // 確認是否是強制接手
            if (checkIsSnatch()) {
                roomEntity?.let {
                    val agentName =
                        UserProfileReference.findAccountName(null, it.serviceNumberAgentId)
                    sendSnatchedAgent.emit(ReSendMessageFormat(agentName, message))
                    return@launch
                }
            }

            if (recordMode) {
                loadMsgDBDesc()
                recordMode = false
            }

            message.status = MessageStatus.SENDING
            message.sendTime = System.currentTimeMillis()
            mainMessageData.add(message)
            mainMessageDataFlow.emit(Pair(false, mainMessageData.toMutableList()))
            //            sendUpdateMsgStatusPair.emit(Pair(message.id, MessageStatus.SENDING))
            val type = message.type

            if (MessageType.TEXT == type && message.content() is TextContent) {
                reSendTextOrAt(message, MessageType.TEXT)
                return@launch
            }

            if (MessageType.AT == type && message.content() is AtContent) {
                reSendTextOrAt(message, MessageType.AT)
                return@launch
            }

            if (MessageType.IMAGE == type && message.content() is ImageContent) {
                reSendImage(message)
                return@launch
            }

            if (MessageType.FILE == type && message.content() is FileContent) {
                reSendFiles(message)
                return@launch
            }

            if (MessageType.STICKER == type && message.content() is StickerContent) {
                reSendSticker(message)
                return@launch
            }

            if (MessageType.VOICE == type && message.content() is VoiceContent) {
                reSendVoice(message)
                return@launch
            }

            if (MessageType.VIDEO == type && message.content() is VideoContent) {
                reSendVideo(message)
                return@launch
            }
        }

    private fun reSendTextOrAt(
        msg: MessageEntity,
        type: MessageType
    ) = viewModelScope.launch(Dispatchers.IO) {
        roomEntity?.let {
            var content: String? = null
            when (type) {
                MessageType.TEXT -> content = (msg.content() as TextContent).text
                MessageType.AT -> content = JsonHelper.getInstance().toJson((msg.content() as AtContent).mentionContents)
                else -> {}
            }
            if (!Strings.isNullOrEmpty(content)) {
//                showMessage(msg)
                ChatService.getInstance().sendMessage(it.id, msg.id, type, content, msg.themeId)
            }
        }
    }

    /**
     * 重新發送圖片訊息
     * version 1.9.1
     */
    private fun reSendImage(message: MessageEntity?) =
        viewModelScope.launch(Dispatchers.IO) {
            message?.let {
                if (it.type != MessageType.IMAGE || it.content() !is ImageContent) {
                    return@launch
                }

                val imageContent = it.content() as ImageContent
                val url = imageContent.url
//                showMessage(it)
                val path = imageContent.thumbnailUrl
                FileService.uploadFile(
                    application,
                    true,
                    tokenId,
                    Media.findByFileType(url),
                    path,
                    url,
                    object :
                        AServiceCallBack<UploadManager.FileEntity, RefreshSource>() {
                        override fun complete(
                            entity: UploadManager.FileEntity?,
                            e: RefreshSource?
                        ) {
                            entity?.let { fileEntity ->
                                if (Strings.isNullOrEmpty(fileEntity.url) ||
                                    Strings.isNullOrEmpty(
                                        fileEntity.thumbnailUrl
                                    )
                                ) {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        sendShowErrorToast.emit("上傳失敗")
                                    }
                                    return
                                }
                                val imageBitmap = BitmapFactory.decodeFile(path)
//                                val imageEntity = DaVinci.with().imageLoader.getImage(url)
//                                val bitmap = imageEntity.bitmap
                                if (imageBitmap.height <= 0 || imageBitmap.width <= 0) {
                                    Log.e("isHeightAndWidthError:", url)
                                }
                                roomEntity?.let { room ->
                                    ChatService
                                        .getInstance()
                                        .sendImageMessage(room.id, it.id, it.themeId, fileEntity)
                                }

                                if (!it.themeId.isNullOrEmpty()) {
                                }
                            } ?: run {
                                CoroutineScope(Dispatchers.Main).launch {
                                    sendShowErrorToast.emit("上傳失敗")
                                }
                            }
                        }

                        override fun onProgress(
                            progress: Float,
                            total: Long
                        ) {
                            CoroutineScope(Dispatchers.Main).launch {
                                it.id?.let { id ->
                                    sendUpdateMsgProgress.emit(Pair(id, (progress * 100).toInt()))
                                }
                            }
                        }

                        override fun error(message: String?) {
                            super.error(message)
                            it.status = MessageStatus.FAILED
                            CoroutineScope(Dispatchers.IO).launch {
                                refreshCurrentMessagePosition.emit(mainMessageData.indexOf(it))
                            }
                        }
                    }
                )
            }
        }

    /**
     * 重新提交檔案訊息
     * version 1.9.1
     */
    private fun reSendFiles(message: MessageEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            if ((MessageType.IMAGE != message.type && message.content() !is FileContent)) {
                return@launch
            }
            roomEntity?.let { room ->
                val fileContent = message.content() as FileContent
                val path = fileContent.android_local_path
//                showMessage(message)
                UploadManager
                    .getInstance()
                    .uploadFile(
                        application,
                        message.id,
                        tokenId,
                        path,
                        object : OnFileUploadListener {
                            override fun onUploadSuccess(
                                messageId: String,
                                entity: UploadManager.FileEntity
                            ) {
                                ChatService.getInstance().sendFileMessage(
                                    room.id,
                                    messageId,
                                    entity.name,
                                    entity.size,
                                    path,
                                    entity.url,
                                    message.themeId,
                                    entity.mD5
                                )
                            }

                            override fun onUploadIng(
                                messageId: String,
                                progress: Int,
                                total: Long
                            ) {
                            }

                            override fun onUploadFailed(
                                reason: String,
                                messageId: String
                            ) {
                                CELog.e(reason)
                                CoroutineScope(Dispatchers.Main).launch {
                                    message.id?.let {
                                        sendUpdateMsgStatus.emit(Triple(room.id, it, MessageStatus.FAILED))
                                    }
                                }
                            }
                        }
                    )
            }
        }

    /**
     * 重新提交表情訊息
     * version 1.9.1
     */
    private fun reSendSticker(message: MessageEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            if ((MessageType.STICKER != message.type && message.content() !is StickerContent)) {
                return@launch
            }
            val stickerId = (message.content() as StickerContent).id
            val packageId = (message.content() as StickerContent).packageId
//            showMessage(message)
            roomEntity?.let {
                ChatService
                    .getInstance()
                    .sendStickerMessage(it.id, message.id, stickerId, packageId, message.themeId)
            }
        }

    /**
     * 重新提交影音訊息
     * version 1.9.1
     */
    private fun reSendVideo(message: MessageEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            if (MessageType.VIDEO != message.type && message.content() !is VideoContent) {
                return@launch
            }
            roomEntity?.let { room ->
                val videoMessage = (message.content() as VideoContent)
                CoroutineScope(Dispatchers.Main).launch {
                    sendUpdateSendVideoProgressInt.emit(R.string.text_sending_video_message)
                }
//                showMessage(message)
                UploadManager.getInstance().onUploadFile(
                    application,
                    videoMessage.name,
                    message.id,
                    tokenId,
                    MessageType.VIDEO,
                    videoMessage.android_local_path,
                    object : OnUploadListener {
                        override fun onSuccess(
                            messageId: String,
                            type: MessageType,
                            response: String
                        ) {
                            val entity =
                                JsonHelper.getInstance().from(
                                    response,
                                    UploadManager.FileEntity::class.java
                                )
                            val msg = MessageReference.findById(messageId)
                            msg?.let { message ->
                                if (message.content() is VideoContent && entity != null) {
                                    if (!Strings.isNullOrEmpty(entity.url) &&
                                        !Strings.isNullOrEmpty(
                                            entity.name
                                        ) &&
                                        entity.size > 0
                                    ) {
                                        val videoContent = (message.content() as VideoContent)
                                        videoContent.name = entity.name
                                        videoContent.url = entity.url
                                        videoContent.size = entity.size.toLong()
                                        message.content = videoContent.toStringContent()
                                        MessageReference.save(message.roomId, message)

                                        if (isThemeOpen) {
                                            themeMessage?.let { theme ->
                                                ChatService.getInstance().sendReplyVideoMessage(
                                                    room.id,
                                                    messageId,
                                                    theme.id,
                                                    videoContent,
                                                    entity.mD5,
                                                    entity.thumbnailUrl,
                                                    entity.thumbnailWidth,
                                                    entity.thumbnailHeight
                                                )
                                            }
                                        } else {
//                                        if (isChildRoomOpen) {
//                                            val id: String = getChildRoomId()
//                                            assert(Strings.isNullOrEmpty(id))
//                                            ChatService.getInstance().sendVideoMessage(
//                                                id,
//                                                messageId,
//                                                "",
//                                                videoContent,
//                                                entity.mD5,
//                                                entity.thumbnailUrl,
//                                                entity.thumbnailWidth,
//                                                entity.thumbnailHeight
//                                            )
//                                        } else {
                                            ChatService.getInstance().sendVideoMessage(
                                                room.id,
                                                messageId,
                                                "",
                                                videoContent,
                                                entity.mD5,
                                                entity.thumbnailUrl,
                                                entity.thumbnailWidth,
                                                entity.thumbnailHeight
                                            )
//                                        }
                                        }
                                        CoroutineScope(Dispatchers.Main).launch {
                                            sendSetThemeOpen.emit(false)
                                        }
                                    }
                                }
                            }
                            CoroutineScope(Dispatchers.Main).launch {
                                sendUpdateSendVideoProgress.emit(Unit)
                            }
                        }

                        override fun onProgress(
                            messageId: String,
                            progress: Int,
                            total: Long
                        ) {
                        }

                        override fun onFailed(reason: String) {
                            CoroutineScope(Dispatchers.Main).launch {
                                sendUpdateSendVideoProgress.emit(Unit)
                                message.id?.let {
                                    sendUpdateMsgStatus.emit(Triple(room.id, it, MessageStatus.FAILED))
                                }
                            }
                            CELog.e(reason)
                        }
                    }
                )
            }
        }

    /**
     * 重新提交影音訊息
     * version 1.9.1
     */
    private fun reSendVoice(message: MessageEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            if ((MessageType.VOICE != message.type && message.content() !is VoiceContent)) {
                return@launch
            }
            roomEntity?.let { room ->
                val voiceContent = (message.content() as VoiceContent)
                val path = voiceContent.url
//                showMessage(message)
                UploadManager
                    .getInstance()
                    .uploadVoice(
                        application,
                        message.id,
                        tokenId,
                        path,
                        object : OnVoiceUploadListener {
                            override fun onUploadSuccess(
                                messageId: String,
                                entity: UploadManager.FileEntity
                            ) {
                                ChatService.getInstance().sendVoiceMessage(
                                    room.id,
                                    messageId,
                                    voiceContent.duration,
                                    entity.url,
                                    message.themeId,
                                    entity.mD5
                                )
                            }

                            override fun onUploadFailed(reason: String) {
                                CELog.e(reason)
                                CoroutineScope(Dispatchers.Main).launch {
                                    message.id?.let {
                                        sendUpdateMsgStatus.emit(Triple(room.id, it, MessageStatus.FAILED))
                                    }
                                }
                            }
                        }
                    )
            }
        }

    fun doAddContact(
        userId: String?,
        userName: String?,
        sendData: SendData
    ) = viewModelScope.launch(Dispatchers.IO) {
        ChatService.getInstance().addContact(
            object : ApiListener<String> {
                override fun onSuccess(roomId: String) {
                    // 虛擬聊天室
                    Log.d("Kyle111", "加好友成功")
                    updateChatRoom(roomId, userId, userName)

                    ApiManager.doAddressbookSync(
                        application,
                        object : ApiListener<List<UserProfileEntity?>> {
                            override fun onSuccess(profiles: List<UserProfileEntity?>) {
                                Log.d("Kyle111", "資料同步成功 : $profiles")
                                CoroutineScope(Dispatchers.Main).launch {
                                    sendMessageToUI.emit(sendData)
                                }
                            }

                            override fun onFailed(errorMessage: String) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    sendShowErrorMsg.emit("資料同步失敗$errorMessage")
                                }
                            }
                        }
                    )
                }

                override fun onFailed(errorMessage: String) {
                    if (errorMessage.contains("已經是您的好友")) {
                        ApiManager.doAddressbookSync(
                            application,
                            object : ApiListener<List<UserProfileEntity?>> {
                                override fun onSuccess(profiles: List<UserProfileEntity?>) {
                                    Log.d("Kyle111", "資料同步成功 : $profiles")
                                    CoroutineScope(Dispatchers.Main).launch {
                                        sendMessageToUI.emit(sendData)
                                    }
                                }

                                override fun onFailed(errorMessage: String) {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        sendShowErrorMsg.emit("資料同步失敗$errorMessage")
                                    }
                                }
                            }
                        )
                    } else {
                        CoroutineScope(Dispatchers.Main).launch {
                            sendShowErrorMsg.emit(errorMessage)
                        }
                    }
                }
            },
            userId,
            userName
        )
    }

    private fun updateChatRoom(
        roomId: String,
        userId: String?,
        userName: String?
    ) = viewModelScope.launch(Dispatchers.IO) {
        val chatRoom =
            ChatRoomEntity
                .Build()
                .id(roomId)
                .name(userName)
                .ownerId(userId)
                .updateTime(System.currentTimeMillis())
                .type(ChatRoomType.friend)
                .build()
        ChatRoomReference.getInstance().save(chatRoom)
        UserPref.getInstance(application).currentRoomId = roomId
        App.getInstance().chatRoomId = chatRoom.id
        roomEntity = chatRoom
        setRoomType()
        CoroutineScope(Dispatchers.Main).launch {
            sendSetup.emit(Unit)
        }
    }

    fun queryMemberIsBlock() =
        viewModelScope.launch(Dispatchers.IO) {
            roomEntity?.let {
                val memberIds = AccountRoomRelReference.findMemberIdsByRoomId(null, it.id)
                if (memberIds.size > 1 && it.type == ChatRoomType.friend) {
                    val isBlock =
                        DBManager
                            .getInstance()
                            .queryFriendIsBlock(if (memberIds[0] == selfUserId) memberIds[1] else memberIds[0])
                    CoroutineScope(Dispatchers.Main).launch {
                        sendSetIsBlock.emit(isBlock)
                    }
                }
            }
        }

    fun addContact(entity: UserProfileEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            contactRepository
                .addContactFriend(
                    AddContactFriendRequest(listOf(entity.id), entity.alias)
                ).collect {
                    when (it) {
                        is ApiResult.Success -> {
                            it.data.roomIds?.let { list ->
                                list.forEach { roomId ->
                                    chatRepository.getRoomItem(roomId!!, selfUserId).collect {
                                        when (it) {
                                            is ApiResult.Success -> {
                                                val status = ChatRoomReference.getInstance().save(it.data)
                                                if (status) {
                                                    CoroutineScope(Dispatchers.Main).launch {
                                                        sendNavigateToChat.emit(it.data)
                                                    }
                                                }
                                            }

                                            is ApiResult.Failure -> {
                                                CELog.e(it.errorMessage.errorMessage)
                                            }

                                            else -> {}
                                        }
                                    }
                                }
                            }
                        }

                        is ApiResult.Failure -> {
                        }

                        else -> {}
                    }
                }
        }

    fun doSwitchChannel(newFrom: ChannelType) =
        viewModelScope.launch(Dispatchers.IO) {
            roomEntity?.let {
                if (ServiceNumberType.PROFESSIONAL == it.serviceNumberType &&
                    !Strings.isNullOrEmpty(
                        it.serviceNumberAgentId
                    ) &&
                    selfUserId != it.serviceNumberAgentId
                ) {
                    CoroutineScope(Dispatchers.Main).launch {
                        sendToast.emit(R.string.text_someone_is_servicing_can_not_change_channel)
                    }
                    return@launch
                }
                CoroutineScope(Dispatchers.Main).launch {
                    sendShowLoadingView.emit(R.string.text_changing)
                }
                ApiManager.doFromSwitch(
                    application,
                    it.id,
                    appointResp?.lastFrom?.value,
                    newFrom.value,
                    object : ApiListener<Map<String?, String?>> {
                        override fun onSuccess(data: Map<String?, String?>) {
                            CoroutineScope(Dispatchers.Main).launch {
                                sendHideLoadingView.emit(Unit)
                                appointResp?.lastFrom = newFrom
                                if (ChannelType.QBI == newFrom) {
                                    sendSetChatDisable.emit(R.string.text_qbi_channel_not_support_offline_reply)
                                } else {
                                    sendSetChatEnable.emit(Unit)
                                }
                                sendSetupAppointStatus.emit(appointResp)
                                sendToast.emit(R.string.text_change_successful)
                                getLastChannelFrom(it.id)
                            }
                        }

                        override fun onFailed(errorMessage: String) {
                            CoroutineScope(Dispatchers.Main).launch {
                                sendHideLoadingView.emit(Unit)
                                sendToast.emit(R.string.text_change_failure)
                            }
                        }
                    }
                )
            }
        }

    fun doServiceNumberStop(roomId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            ApiManager.doServiceNumberStopService(
                application,
                roomId,
                object : ApiListener<Boolean> {
                    override fun onSuccess(result: Boolean) {
                        if (!result) {
                            CoroutineScope(Dispatchers.Main).launch {
                                sendToast.emit(R.string.text_close_service_failure)
                            }
                            return
                        }
                        roomEntity?.serviceNumberAgentId = ""
                    }

                    override fun onFailed(errorMessage: String) {
                        Log.i("doServiceNumberStop", errorMessage + "")
                    }
                }
            )
        }

    fun stopService() =
        viewModelScope.launch(Dispatchers.IO) {
            roomEntity?.let {
                ApiManager.doAgentStopService(
                    application,
                    it.id,
                    object : ApiListener<String> {
                        override fun onSuccess(s: String) {
                            ApiManager.doServiceNumberStopService(
                                application,
                                it.id,
                                object : ApiListener<Boolean> {
                                    override fun onSuccess(result: Boolean) {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            sendHideLoadingView.emit(Unit)
                                            if (!result) {
                                                sendToast.emit(R.string.text_close_service_failure)
                                                return@launch
                                            }
                                            appointResp?.let { resp ->
                                                if (ChannelType.QBI == resp.lastFrom) {
                                                    sendSetChatDisable.emit(R.string.text_qbi_channel_not_support_offline_reply)
                                                } else {
                                                    sendSetChatEnable.emit(Unit)
                                                }
                                                resp.status = ServiceNumberStatus.OFF_LINE
                                                sendSetupAppointStatus.emit(resp)
                                            }
                                            sendToast.emit(R.string.text_close_service_successful)
                                            it.serviceNumberAgentId = ""
                                            ChatRoomReference.getInstance().updateServiceNumberStatusById(it.id, ServiceNumberStatus.OFF_LINE)
                                            sendSetup.emit(Unit)
                                            sendClearProvisionalMember.emit(Unit)
                                            clearConsultRoom()
                                            EventBusUtils.sendEvent(
                                                EventMsg(
                                                    MsgConstant.REMOVE_GROUP_FILTER,
                                                    it.id
                                                )
                                            )
                                            EventBusUtils.sendEvent(
                                                EventMsg(
                                                    MsgConstant.REFRESH_ROOM_BY_LOCAL,
                                                    JsonHelper.getInstance().toJson(it)
                                                )
                                            )
                                        }
                                    }

                                    override fun onFailed(errorMessage: String) {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            sendHideLoadingView.emit(Unit)
                                            sendToast.emit(R.string.text_close_service_failure)
                                        }
                                    }
                                }
                            )
                        }

                        override fun onFailed(errorMessage: String) {
                            CoroutineScope(Dispatchers.Main).launch {
                                sendHideLoadingView.emit(Unit)
                                sendToast.emit(R.string.text_close_service_failure)
                            }
                        }
                    }
                )
            }
        }

    fun doMessageRetract(
        message: MessageEntity,
        messageSize: Int
    ) = viewModelScope.launch(Dispatchers.IO) {
        roomEntity?.let { room ->
            ApiManager.doMessageRetract(
                application,
                room.id,
                message.id,
                object : ApiListener<String> {
                    override fun onSuccess(s: String) {
                        executeRetractCount++
                        // 自己撤回消息
                        // 1、构造系統消息
                        message.flag = MessageFlag.RETRACT
                        if (!Strings.isNullOrEmpty(message.nearMessageId)) {
                            MessageReference.deleteByIds(arrayOf(message.nearMessageId))
                        }

                        MessageReference.save(message.roomId, message)
                        if (executeRetractCount == messageSize) {
                            CoroutineScope(Dispatchers.Main).launch {
                                sendRefreshListView.emit(Unit)
                                sendHideLoadingView.emit(Unit)
                            }
                        }
                        CoroutineScope(Dispatchers.Main).launch {
                            scopeRetractTipInvisible.emit(Unit)
                        }
                    }

                    override fun onFailed(errorMessage: String) {
                        executeRetractCount++
                        if (executeRetractCount == messageSize) {
                            CoroutineScope(Dispatchers.Main).launch {
                                sendRefreshListView.emit(Unit)
                                sendHideLoadingView.emit(Unit)
                                // EVAN_FLAG 2019-09-10 因為實現多筆撤回機制，暫時無法組裝無法被撤回的信息，超過60分鐘的信息。
                                sendShowErrorToast.emit(errorMessage)
                            }
                        }
                    }
                }
            )
        }
    }

    fun doExecutionSendImage(
        sessionIds: List<String>,
        path: String?
    ) = viewModelScope.launch(Dispatchers.IO) {
        FileService.uploadFile(
            application,
            true,
            tokenId,
            Media.findByFileType(path),
            path,
            path,
            object : AServiceCallBack<UploadManager.FileEntity, RefreshSource>() {
                override fun complete(
                    entity: UploadManager.FileEntity,
                    refreshSource: RefreshSource
                ) {
                    for (i in sessionIds.indices) {
                        val mMessageId = generateMessageId()
                        val roomId = sessionIds[i]
                        ApiManager.doMessageSend(
                            application,
                            roomId,
                            mMessageId,
                            "",
                            MsgBuilder(MessageType.IMAGE)
                                .url(entity.url)
                                .size(entity.size)
                                .width(entity.width)
                                .height(entity.height)
                                .thumbnailUrl(entity.thumbnailUrl)
                                .thumbnailSize(entity.thumbnailSize)
                                .name("pic"),
                            object : MessageSendRequest.Listener<MessageEntity>() {
                                override fun onFailed(
                                    message: MessageEntity,
                                    errorMessage: String
                                ) {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        sendToast.emit(R.string.text_share_media_failure)
                                    }
                                }

                                override fun onSuccess(message: MessageEntity) {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        sendToast.emit(R.string.text_shared_successful)
                                    }
                                }
                            }
                        )
                    }
                }

                override fun onProgress(
                    progress: Float,
                    total: Long
                ) {
                }

                override fun error(message: String) {
                    CELog.e(message)
                    CoroutineScope(Dispatchers.Main).launch {
                        sendToast.emit(R.string.text_share_media_failure)
                    }
                }
            }
        )
    }

    // 判斷聊天室權限來顯示 +
    fun doHandleGroupMemberPrivilege() =
        viewModelScope.launch(Dispatchers.IO) {
            roomEntity?.let {
                if (it.type == ChatRoomType.group) {
                    if ((
                            it.ownerId == selfUserId ||
                                it.chatRoomMember?.find { member -> member.memberId == selfUserId }?.privilege == GroupPrivilegeEnum.Manager
                        )
                    ) {
                        CoroutineScope(Dispatchers.Main).launch {
                            sendGroupAddPermission.emit(true)
                        }
                    } else {
                        CoroutineScope(Dispatchers.Main).launch {
                            sendGroupAddPermission.emit(false)
                        }
                    }
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        sendGroupAddPermission.emit(false)
                    }
                }
            }
        }

    private fun formatDateSafely(timestamp: Long): String =
        try {
            if (timestamp < 0) {
                throw IllegalArgumentException("Invalid timestamp: $timestamp")
            }
            val instant = Instant.ofEpochMilli(timestamp)
            messageTimeLineFormat.format(instant)
        } catch (e: Exception) {
            Log.e("DateFormatting", "Error formatting date: ${e.message}")
            "date error"
        }

    fun refreshCurrentMessage(messageId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            mainMessageData.toMutableList().forEachIndexed { index, messageEntity ->
                if (messageEntity.id == messageId) {
                    refreshCurrentMessagePosition.emit(index)
                }
            }
        }

    fun setRoomType() {
        roomEntity?.let {
            it.roomType =
                when {
                    // 臨時成員聊天室
                    it.provisionalIds.contains(selfUserId) -> {
                        ChatRoomType.provisional
                    }
                    // 商務號擁有者秘書聊天室
                    it.type == ChatRoomType.serviceMember && it.serviceNumberType == ServiceNumberType.BOSS && selfUserId != it.serviceNumberOwnerId -> {
                        ChatRoomType.bossSecretary
                    }
                    // 商務號擁有者
                    it.type == ChatRoomType.services && it.serviceNumberType == ServiceNumberType.BOSS && selfUserId == it.serviceNumberOwnerId -> {
                        ChatRoomType.bossOwner
                    }
                    // 商務號擁有者的秘書聊天室
                    it.type == ChatRoomType.serviceMember && it.serviceNumberType == ServiceNumberType.BOSS && selfUserId == it.serviceNumberOwnerId -> {
                        ChatRoomType.bossOwnerWithSecretary
                    }

                    // 商務號服務號聊天室
                    it.type == ChatRoomType.services && it.serviceNumberType == ServiceNumberType.BOSS -> {
                        ChatRoomType.bossServiceNumber
                    }

                    // 內部服務號
                    // 服務號員工進線聊天室(詢問者)
                    (ChatRoomType.services == it.type || ChatRoomType.subscribe == it.type) && it.serviceNumberOpenType.contains("I") && it.ownerId == selfUserId -> {
                        ChatRoomType.serviceINumberAsker
                    }

                    // 內部服務號 服務人員
                    (ChatRoomType.services == it.type || ChatRoomType.subscribe == it.type) && it.serviceNumberOpenType.contains("I") && it.ownerId != selfUserId -> {
                        ChatRoomType.serviceINumberStaff
                    }

                    // 外部服務號 服務人員
                    ChatRoomType.services == it.type && ServiceNumberType.BOSS != it.serviceNumberType -> {
                        ChatRoomType.serviceONumberStaff
                    }

                    // 外部服務號 進線者
                    ChatRoomType.services == it.type &&
                        it.serviceNumberOpenType.contains(
                            "O"
                        ) &&
                        it.ownerId != selfUserId -> {
                        ChatRoomType.serviceONumberAsker
                    }

                    else -> it.type
                }
        }
    }

    fun doGetChatRoomMemberIds(roomId: String) =
        flow {
            checkTokenValid(chatRepository.getRoomItem(roomId, selfUserId))?.collect {
                when (it) {
                    is ApiResult.Success -> {
                        it.data.memberIds?.let { memberIds ->
                            emit(memberIds)
                        } ?: emit(mutableListOf<String>())
                    }

                    is ApiResult.Failure -> {
                        Log.e("doGetChatRoomMemberIds", "get room failure error =${it.errorMessage}")
                    }

                    else -> {}
                }
            }
        }

    fun onRoomMessageClean() =
        viewModelScope.launch(Dispatchers.IO) {
            mainMessageData.clear()
            mainMessageDataFlow.emit(Pair(false, mainMessageData.toMutableList()))
        }

    override suspend fun sendImageMedia(
        path1: String,
        path2: String
    ): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                sendImage(path1, path2)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun sendVideoMedia(videoSize: IVideoSize): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                sendVideoStream(videoSize)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun sendGifMedia(
        url: String,
        path: String,
        width: Int,
        height: Int
    ): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                sendGifImg(url, path, width, height)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    fun sendMediaList(result: List<LocalMedia>) {
        sequentialSender.send(result)
    }

    fun cancelSending() {
        sequentialSender.cancel()
    }

    private fun sendVideoStream(
        iVideoSize: IVideoSize,
        isQuote: Boolean,
        isFacebookReplyPublic: Boolean
    ): Deferred<Unit> =
        viewModelScope.async(Dispatchers.IO) {
            roomEntity?.let {
                // 確認是否是強制接手
                if (checkIsSnatch()) {
                    val agentName =
                        UserProfileReference.findAccountName(null, it.serviceNumberAgentId)
                    sendSnatchedAgent.emit(SendVideoFormat(agentName, iVideoSize))
                    return@async
                }

                if (recordMode) {
                    loadMsgDBDesc()
                    recordMode = false
                }

                if (isFacebookReply()) {
                    sendFacebookAttachmentReply(isFacebookReplyPublic, iVideoSize.name(), iVideoSize.path())
                    CoroutineScope(Dispatchers.Main).launch {
                        sendShowSendVideoProgress.emit(application.getString(R.string.text_sending_video_message))
                    }
                    return@async
                }

                CoroutineScope(Dispatchers.Main).launch {
                    sendShowSendVideoProgress.emit(if (isQuote) application.getString(R.string.text_video_quote_on_the_way) else application.getString(R.string.text_sending_video_message))
                }
                val messageId = withContext(Dispatchers.Main) { generateMessageId() }
                val videoContent: VideoContent = buildVideoContent(iVideoSize)
                val msg: MessageEntity? = buildVideoMessage(messageId, videoContent)
                msg?.let { _ ->
                    showMessage(msg)
                    setReplyMsg(msg)
                    UploadManager.getInstance().onUploadFile(
                        application,
                        iVideoSize.name(),
                        messageId,
                        tokenId,
                        MessageType.VIDEO,
                        iVideoSize.path(),
                        object : OnUploadListener {
                            override fun onSuccess(
                                messageId: String,
                                type: MessageType,
                                response: String
                            ) {
                                val entity =
                                    JsonHelper.getInstance().from(
                                        response,
                                        UploadManager.FileEntity::class.java
                                    )
                                if (!Strings.isNullOrEmpty(entity.url) &&
                                    !Strings.isNullOrEmpty(
                                        entity.name
                                    ) &&
                                    entity.size >= 0
                                ) {
                                    videoContent.name = entity.name
                                    videoContent.url = entity.url
                                    videoContent.size = entity.size.toLong()
                                    MessageReference.save(msg.roomId, msg)
                                    if (isThemeOpen) {
                                        ChatService.getInstance().sendReplyVideoMessage(
                                            it.id,
                                            messageId,
                                            themeMessage?.id,
                                            videoContent,
                                            entity.mD5,
                                            entity.thumbnailUrl,
                                            entity.thumbnailWidth,
                                            entity.thumbnailHeight
                                        )
                                    } else {
                                        ChatService.getInstance().sendVideoMessage(
                                            it.id,
                                            messageId,
                                            "",
                                            videoContent,
                                            entity.mD5,
                                            entity.thumbnailUrl,
                                            entity.thumbnailWidth,
                                            entity.thumbnailHeight
                                        )
                                    }
                                    CoroutineScope(Dispatchers.Main).launch {
                                        sendSetThemeOpen.emit(false)
                                    }
                                }
                                CoroutineScope(Dispatchers.Main).launch {
                                    sendUpdateSendVideoProgress.emit(Unit)
                                }
                            }

                            override fun onProgress(
                                messageId: String,
                                progress: Int,
                                total: Long
                            ) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    sendUpdateMsgProgress.emit(Pair(messageId, progress))
                                    sendUpdateSendVideoProgressInt.emit(progress)
                                }
                            }

                            override fun onFailed(reason: String) {
                                CELog.e(reason)
                                CoroutineScope(Dispatchers.Main).launch {
                                    sendUpdateMsgStatus.emit(Triple(it.id, messageId, MessageStatus.FAILED))
                                    sendDismissSendVideoProgress.emit(Unit)
                                }
                            }
                        }
                    )
                }
            }
        }

    fun search(
        keyword: String,
        isMoreSearch: Boolean = false
    ) = viewModelScope.launch(Dispatchers.IO) {
        _searchResult.clear()
        val searchMessages =
            mainMessageData
                .toMutableList()
                .filter {
                    it.getContent(it.content).contains(keyword, true) &&
                        (it.type == MessageType.TEXT || it.type == MessageType.AT) &&
                        it.flag != MessageFlag.RETRACT &&
                        it.sourceType != SourceType.SYSTEM
                }.distinctBy { it.sequence }

        if (searchMessages.isEmpty()) {
            errorMessage.postValue(application.getString(R.string.text_not_find_any_result))
            return@launch
        }
        _searchResult.addAll(searchMessages)
        _searchResult.sortByDescending { if (it is MessageEntity) it.sendTime else 1 }
        if (searchMessages.size > 1) {
            _searchResult.add(LoadMoreEntity(name = "LoadMore"))
        }

        if (isMoreSearch) {
            _searchResult.removeIf { it is LoadMoreEntity }
        }

        searchResult.emit(Pair(keyword, _searchResult.toMutableList()))
    }

    fun setChatRoomNotDeleted() =
        viewModelScope.launch(Dispatchers.IO) {
            roomEntity?.let {
                DBManager.getInstance().setRoomNotDeleted(it.id)
            }
        }

    fun searchMoreMessage(
        roomId: String,
        keyword: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        val oldestMessage =
            mainMessageData
                .toMutableList()
                .filter { it.senderName != null }
                .maxByOrNull { it.sequence ?: Int.MAX_VALUE }
        chatRepository.getSearchMoreMessage(roomId, oldestMessage?.sequence ?: 0).collect {
            when (it) {
                is ApiResult.Success -> {
                    formatMessageList(false, it.data).await()
                    search(keyword, isMoreSearch = true)
                }

                else -> {
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        metaReplyLimitTimer.cancel()
        viewModelScope.cancel()
    }
}
