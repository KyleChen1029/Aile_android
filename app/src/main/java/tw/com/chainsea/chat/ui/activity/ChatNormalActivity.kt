package tw.com.chainsea.chat.ui.activity

import android.Manifest
import android.Manifest.permission
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.media.CamcorderProfile
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.text.ClipboardManager
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.util.Log
import android.util.LruCache
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import android.view.inputmethod.EditorInfo
import android.webkit.URLUtil
import android.widget.FrameLayout
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.isGone
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.common.base.Strings
import com.google.common.collect.Lists
import com.google.common.primitives.Longs
import com.google.gson.reflect.TypeToken
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.FileSizeUnit
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectModeConfig
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.luck.picture.lib.style.BottomNavBarStyle
import com.luck.picture.lib.style.PictureSelectorStyle
import com.luck.picture.lib.style.SelectMainStyle
import com.luck.picture.lib.style.TitleBarStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.android.common.event.KeyboardHelper
import tw.com.chainsea.android.common.image.BitmapHelper
import tw.com.chainsea.android.common.json.JsonHelper
import tw.com.chainsea.android.common.log.CELog
import tw.com.chainsea.android.common.multimedia.AMediaBean
import tw.com.chainsea.android.common.multimedia.ImageBean
import tw.com.chainsea.android.common.multimedia.MultimediaHelper
import tw.com.chainsea.android.common.text.KeyWordHelper
import tw.com.chainsea.android.common.ui.UiHelper
import tw.com.chainsea.android.common.video.IVideoSize
import tw.com.chainsea.android.common.video.VideoSizeFromVideoFile
import tw.com.chainsea.ce.sdk.bean.BadgeDataModel
import tw.com.chainsea.ce.sdk.bean.GroupRefreshBean
import tw.com.chainsea.ce.sdk.bean.GroupUpgradeBean
import tw.com.chainsea.ce.sdk.bean.InputLogBean
import tw.com.chainsea.ce.sdk.bean.MsgNoticeBean
import tw.com.chainsea.ce.sdk.bean.MsgStatusBean
import tw.com.chainsea.ce.sdk.bean.ProcessStatus
import tw.com.chainsea.ce.sdk.bean.UpdateAvatarBean
import tw.com.chainsea.ce.sdk.bean.UserExitBean
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity
import tw.com.chainsea.ce.sdk.bean.account.UserType
import tw.com.chainsea.ce.sdk.bean.business.BusinessCode
import tw.com.chainsea.ce.sdk.bean.common.EnableType
import tw.com.chainsea.ce.sdk.bean.msg.ChannelType
import tw.com.chainsea.ce.sdk.bean.msg.MessageFlag
import tw.com.chainsea.ce.sdk.bean.msg.MessageStatus
import tw.com.chainsea.ce.sdk.bean.msg.MessageType
import tw.com.chainsea.ce.sdk.bean.msg.SourceType
import tw.com.chainsea.ce.sdk.bean.msg.Tools
import tw.com.chainsea.ce.sdk.bean.msg.content.AtContent
import tw.com.chainsea.ce.sdk.bean.msg.content.BusinessContent
import tw.com.chainsea.ce.sdk.bean.msg.content.FileContent
import tw.com.chainsea.ce.sdk.bean.msg.content.IMessageContent
import tw.com.chainsea.ce.sdk.bean.msg.content.ImageContent
import tw.com.chainsea.ce.sdk.bean.msg.content.TextContent
import tw.com.chainsea.ce.sdk.bean.msg.content.UndefContent
import tw.com.chainsea.ce.sdk.bean.msg.content.VideoContent
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEnum
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType
import tw.com.chainsea.ce.sdk.bean.room.DiscussMemberSocket
import tw.com.chainsea.ce.sdk.bean.room.QuickReplyItem
import tw.com.chainsea.ce.sdk.bean.room.QuickReplySocket
import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberStatus
import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberType
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberAddModel
import tw.com.chainsea.ce.sdk.bean.servicenumber.type.GroupPrivilegeEnum
import tw.com.chainsea.ce.sdk.bean.todo.TodoEntity
import tw.com.chainsea.ce.sdk.bean.todo.TodoStatus
import tw.com.chainsea.ce.sdk.database.DBManager
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.database.sp.UserPref
import tw.com.chainsea.ce.sdk.event.EventBusUtils
import tw.com.chainsea.ce.sdk.event.EventMsg
import tw.com.chainsea.ce.sdk.event.MsgConstant
import tw.com.chainsea.ce.sdk.http.ce.model.User
import tw.com.chainsea.ce.sdk.network.model.common.ApiErrorData
import tw.com.chainsea.ce.sdk.network.model.response.ChatRoomMemberResponse
import tw.com.chainsea.ce.sdk.network.model.response.DeviceRecordItem
import tw.com.chainsea.ce.sdk.reference.AccountRoomRelReference
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference
import tw.com.chainsea.ce.sdk.reference.MessageReference
import tw.com.chainsea.ce.sdk.reference.UserProfileReference
import tw.com.chainsea.ce.sdk.service.UserProfileService
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack
import tw.com.chainsea.ce.sdk.service.type.ChatRoomSource
import tw.com.chainsea.ce.sdk.service.type.RefreshSource
import tw.com.chainsea.ce.sdk.socket.ce.bean.ProvisionalMemberAddedSocket
import tw.com.chainsea.ce.sdk.socket.ce.code.NoticeCode
import tw.com.chainsea.chat.App
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.aiff.AiffManager
import tw.com.chainsea.chat.aiff.database.AiffDB
import tw.com.chainsea.chat.base.Constant
import tw.com.chainsea.chat.chatroomfilter.ChatRoomFilterActivity
import tw.com.chainsea.chat.config.AiffDisplayLocation
import tw.com.chainsea.chat.config.AiffEmbedLocation
import tw.com.chainsea.chat.config.BundleKey
import tw.com.chainsea.chat.config.InvitationType
import tw.com.chainsea.chat.databinding.ActivityChatNormalLayoutBinding
import tw.com.chainsea.chat.databinding.GridMemberViewBinding
import tw.com.chainsea.chat.databinding.GridViewBinding
import tw.com.chainsea.chat.databinding.ProgressSendVideoBinding
import tw.com.chainsea.chat.keyboard.ChatKeyboardLayout
import tw.com.chainsea.chat.keyboard.ChatKeyboardLayout.OnChatKeyBoardListener
import tw.com.chainsea.chat.keyboard.ChatKeyboardLayout.OnMentionFeatureListener
import tw.com.chainsea.chat.keyboard.MentionSelectAdapter
import tw.com.chainsea.chat.keyboard.media.MediaSelectorPreviewActivity
import tw.com.chainsea.chat.keyboard.view.HadEditText
import tw.com.chainsea.chat.keyboard.view.HadEditText.SendData
import tw.com.chainsea.chat.lib.ActivityManager
import tw.com.chainsea.chat.lib.AtMatcherHelper
import tw.com.chainsea.chat.lib.NetworkUtils
import tw.com.chainsea.chat.lib.PictureParse
import tw.com.chainsea.chat.lib.ToastUtils
import tw.com.chainsea.chat.mainpage.view.MainPageActivity
import tw.com.chainsea.chat.mediagallery.view.MediaGalleryActivity
import tw.com.chainsea.chat.messagekit.enums.OpenBottomRichMeunType
import tw.com.chainsea.chat.messagekit.enums.RichMenuBottom
import tw.com.chainsea.chat.messagekit.enums.RichMenuType
import tw.com.chainsea.chat.messagekit.isScrolledToBottom
import tw.com.chainsea.chat.messagekit.lib.AudioLib
import tw.com.chainsea.chat.messagekit.lib.FileUtil
import tw.com.chainsea.chat.messagekit.lib.Global
import tw.com.chainsea.chat.messagekit.lib.MessageDomino
import tw.com.chainsea.chat.messagekit.main.adapter.BottomRichMeunAdapter
import tw.com.chainsea.chat.messagekit.main.adapter.MessageAdapterMode
import tw.com.chainsea.chat.messagekit.main.adapter.MessageListAdapter
import tw.com.chainsea.chat.messagekit.main.adapter.OnMessageClickListener
import tw.com.chainsea.chat.messagekit.main.viewholder.base.OnMessageSlideReply
import tw.com.chainsea.chat.network.contact.ViewModelFactory
import tw.com.chainsea.chat.searchfilter.view.activity.CreateDiscussActivity
import tw.com.chainsea.chat.searchfilter.view.activity.CreateGroupActivity
import tw.com.chainsea.chat.searchfilter.view.activity.MemberInvitationActivity
import tw.com.chainsea.chat.searchfilter.view.fragment.CommunitiesSearchFragment
import tw.com.chainsea.chat.searchfilter.view.fragment.ContactPersonClientSearchFragment
import tw.com.chainsea.chat.searchfilter.view.fragment.ServiceNumberSearchFragment
import tw.com.chainsea.chat.service.ActivityTransitionsControl
import tw.com.chainsea.chat.style.RoomThemeStyle
import tw.com.chainsea.chat.ui.adapter.ChatRoomMembersAdapter
import tw.com.chainsea.chat.ui.adapter.ChatRoomMessageSearchAdapter
import tw.com.chainsea.chat.ui.adapter.LoginDevicesInfoAdapter
import tw.com.chainsea.chat.ui.adapter.OnMessageItemClick
import tw.com.chainsea.chat.ui.adapter.RichMenuAdapter
import tw.com.chainsea.chat.ui.adapter.entity.LoadMoreEntity
import tw.com.chainsea.chat.ui.adapter.entity.RichMenuInfo
import tw.com.chainsea.chat.ui.dialog.BottomSheetDialogBuilder
import tw.com.chainsea.chat.ui.fragment.ChatFragment
import tw.com.chainsea.chat.util.DaVinci
import tw.com.chainsea.chat.util.DownloadUtil
import tw.com.chainsea.chat.util.GlideEngine.Companion.createGlideEngine
import tw.com.chainsea.chat.util.IntentUtil
import tw.com.chainsea.chat.util.IntentUtil.start
import tw.com.chainsea.chat.util.IntentUtil.startIntent
import tw.com.chainsea.chat.util.SortUtil
import tw.com.chainsea.chat.util.TextViewHelper
import tw.com.chainsea.chat.util.ThemeHelper
import tw.com.chainsea.chat.util.TimeUtil
import tw.com.chainsea.chat.util.UnreadUtil.getUnreadText
import tw.com.chainsea.chat.view.BaseActivity
import tw.com.chainsea.chat.view.business.BusinessTaskAction
import tw.com.chainsea.chat.view.chat.ChatViewModel
import tw.com.chainsea.chat.view.chatroom.adapter.AdvisoryRoomAdapter
import tw.com.chainsea.chat.view.contact.ContactPersonFragment
import tw.com.chainsea.chat.view.gallery.ScreenshotsPreviewActivity
import tw.com.chainsea.chat.view.homepage.EmployeeInformationHomepageActivity
import tw.com.chainsea.chat.view.homepage.SelfInformationHomepageActivity
import tw.com.chainsea.chat.view.homepage.SubscribeInformationHomepageActivity
import tw.com.chainsea.chat.view.homepage.VisitorHomepageActivity
import tw.com.chainsea.chat.view.login.LogoutSmsDialogFragment
import tw.com.chainsea.chat.view.service.ServiceNumberManageActivity
import tw.com.chainsea.chat.view.todo.OnSetRemindTime
import tw.com.chainsea.chat.view.todo.TodoListFragment
import tw.com.chainsea.chat.view.todo.TodoSettingDialog
import tw.com.chainsea.chat.widget.GridItemDecoration
import tw.com.chainsea.chat.widget.xrefreshlayout.XRefreshLayout
import tw.com.chainsea.chat.widget.xrefreshlayout.XRefreshLayout.OnBackgroundClickListener
import tw.com.chainsea.custom.view.alert.AlertView
import tw.com.chainsea.custom.view.progress.IosProgressBar
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.MessageFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Collections
import java.util.LinkedList
import java.util.Locale
import java.util.TreeMap
import java.util.function.Consumer
import kotlin.math.max
import kotlin.math.min

class ChatNormalActivity :
    BaseActivity(),
    ChatRoomMembersAdapter.OnItemClickListener,
    OnMessageClickListener,
    OnBackgroundClickListener,
    XRefreshLayout.OnRefreshListener {
    companion object {
        val MEDIA_SELECTOR_REQUEST_CODE: Int = 0x2705
    }

    private val viewModelFactory by lazy { ViewModelFactory(application) }

    private val chatViewModel: ChatViewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[ChatViewModel::class.java]
    }

    private val binding: ActivityChatNormalLayoutBinding by lazy {
        ActivityChatNormalLayoutBinding.inflate(layoutInflater)
    }

    private var aiffManager: AiffManager? = null
    private var roomId = ""
    private var userName = ""
    private val selfUserId by lazy {
        TokenPref.getInstance(this).userId
    }

    private var chatFragment: Any? = null

    private var themeStyle = RoomThemeStyle.UNDEF

    private var aiffPopupWindows: PopupWindow? = null
    private var memberPopupWindow: PopupWindow? = null

    // activity result launcher
    private var addMemberARL: ActivityResultLauncher<Intent>? = null
    private var addProvisionalMemberARL: ActivityResultLauncher<Intent>? = null
    private var updateGroupARL: ActivityResultLauncher<Intent>? = null
    private var toGroupSessionARL: ActivityResultLauncher<Intent>? = null
    private lateinit var mediaPreviewARL: ActivityResultLauncher<Intent>

    // 個人聊天室裝置 adapter
    private val loginDevicesInfoAdapter by lazy { LoginDevicesInfoAdapter() }

    // 成員列表 Adapter
    private var chatRoomMembersAdapter: ChatRoomMembersAdapter? = null

    private var isDeletedMember = false

    private var rightCancelTV: TextView? = null

    private var newFriendRoomWhereCome: String? = null
    private var isNeedRefreshList: Boolean = false
    private var isGreenTheme: Boolean = false

    private val onMessageSlideReplyListener: OnMessageSlideReply =
        object : OnMessageSlideReply {
            override fun onMessageSlideReply(messageEntity: MessageEntity) {
                executeReply(messageEntity)
            }
        }

    private val messageListAdapter by lazy {
        chatViewModel.roomEntity?.let { MessageListAdapter(it, onMessageSlideReplyListener) }
    }

    private var lastItemPosition = 0

    private val screenShotData: MutableList<MessageEntity> = Lists.newArrayList()

    var timeBoxTarget: Runnable =
        Runnable {
            binding.floatTimeBoxTV
                .animate()
                .alpha(0.0f)
                .setDuration(300)
                .setListener(
                    object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            binding.floatTimeBoxTV.visibility = View.GONE
                        }
                    }
                )
        }

    private val messageTimeLineFormat = SimpleDateFormat("MMdd日(EEE)", Locale.TAIWAN)
    private var progressBar: IosProgressBar? = null
    private var actionStatus = ActionStatus.SCROLL
    private var isReply = false
    private var themeId: String? = ""
    private var userId: String? = null
    private var sendVideoProgress: Dialog? = null
    private var sendVideoProgressBinding: ProgressSendVideoBinding? = null
    private var isSendSingleFile = false
    private var sendFileSize = 0
    private var addProgressValue = 0
    var searchSelectorIndex: Int = 0

    // 紀錄手指按下 subRoom 的時間
    var subRoomOnTouchDownTime: Long = 0

    // 當新訊息進來時才顯示未讀浮窗
    private var doGettingNewMessage: Boolean = false

    // AI 諮詢引用的回覆
    private val aiConsultQuoteTextResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let { data ->
                    val bundle = data.extras
                    if (bundle != null) {
                        val listType = object : TypeToken<MessageType>() {}.type
                        val messageType: MessageType =
                            JsonHelper.getInstance().from(
                                bundle.getString(BundleKey.CONSULT_AI_QUOTE_TYPE.key()),
                                listType
                            )
                        when (messageType) {
                            MessageType.TEXT -> {
                                val quoteString =
                                    data.getStringExtra(BundleKey.CONSULT_AI_QUOTE_STRING.key())
                                binding.chatKeyboardLayout.setUnfinishedEdited(
                                    InputLogBean.from(quoteString)
                                )
                            }

                            else -> {}
                        }
                    }
                }
            }
        }

    private val advisoryRoomAdapter: AdvisoryRoomAdapter by lazy {
        AdvisoryRoomAdapter(aiConsultQuoteTextResult)
    }

    private lateinit var mentionSelectAdapter: MentionSelectAdapter
    private lateinit var keyBoardBarListener: KeyBoardBarListener

    @RequiresApi(VERSION_CODES.TIRAMISU)
    val permissions =
        arrayOf(
            permission.READ_MEDIA_IMAGES,
            permission.READ_MEDIA_AUDIO,
            permission.READ_MEDIA_VIDEO
        )
    private val permissionsCamera: Array<String> =
        arrayOf(
            permission.CAMERA,
            permission.WRITE_EXTERNAL_STORAGE
        )

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val permissionsTIRAMISU: Array<String> =
        arrayOf(
            permission.CAMERA,
            permission.READ_MEDIA_IMAGES
        )

    private val sendFileOpenResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let { data ->
                    val filePaths: List<String>? =
                        data.getStringArrayListExtra(BundleKey.FILE_PATH_LIST.key())
                    filePaths?.let { paths ->
                        sendFileSize = paths.size
                        isSendSingleFile = sendFileSize == 1
                        for (filePath in paths) {
                            val fileType = FileUtil.getFileType(filePath)
                            when (fileType) {
                                Global.FileType_Png, Global.FileType_Jpg, Global.FileType_jpeg, Global.FileType_bmp -> {
                                    val path =
                                        PictureParse.parsePath(
                                            this,
                                            filePath
                                        )
                                    chatViewModel.sendImage(path[0], path[1])
                                }

                                Global.FileType_gif -> {
                                    val bitmapBean =
                                        PictureParse.parseGifPath(
                                            this,
                                            filePath
                                        )
                                    chatViewModel.sendGifImg(
                                        bitmapBean.url,
                                        filePath,
                                        bitmapBean.width,
                                        bitmapBean.height
                                    )
                                }

                                Global.FileType_mov, Global.FileType_mp4, Global.FileType_rmvb, Global.FileType_avi -> {
                                    val iVideoSize: IVideoSize = VideoSizeFromVideoFile(filePath)
                                    if (iVideoSize.size() == 0L) {
                                        chatViewModel.sendFile(filePath)
                                    } else {
                                        chatViewModel.sendVideo(iVideoSize)
                                    }
                                }

                                else -> chatViewModel.sendFile(filePath)
                            }
                        }
                    }
                }
            }
        }

    // Camera權限
    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            result.forEach { (_, isGranted) ->
                if (!isGranted) {
                    ToastUtils.showToast(this, getString(R.string.text_need_camera_permission))
                    return@registerForActivityResult
                }
            }
            doOpenCamera()
        }

    private var isProvisionMember = false

    private var isActivityForResult = false

    private val storagePermissionResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                binding.funMedia.init(this)
                showFunMedia(false)
            } else {
                ToastUtils.showToast(
                    this,
                    getString(R.string.text_need_storage_permission)
                )
            }
        }

    // 相簿權限
    private val launcher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            result.forEach { (_, isGranted) ->
                if (!isGranted) {
                    ToastUtils.showToast(
                        this,
                        getString(R.string.text_need_storage_permission)
                    )
                    return@registerForActivityResult
                }
            }
            binding.funMedia.init(this)
            showFunMedia(false)
        }

    // /麥克風權限
    private val recordPermissionResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                if (checkMicAvailable()) {
                    showRecordingWindow()
                }
            } else {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.setData(Uri.parse("package:" + packageName))
                start(this, intent)
                ToastUtils.showToast(this, getString(R.string.text_need_mic_permission))
            }
        }

    private val sendVideoCaptureResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val size = VideoSizeFromVideoFile(videoFile.absolutePath)
                chatViewModel.sendVideo(size)
            }
        }

    private val sendImageCaptureResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                chatViewModel.sendImage(
                    isOriginPhoto = true,
                    path = photoFile.absolutePath
                )
            }
        }

    private lateinit var videoFile: File
    private lateinit var photoFile: File
    private var isSearchMode = false
    private var searchKeyword = ""
    private val chatRoomMessageSearchAdapter: ChatRoomMessageSearchAdapter by lazy {
        ChatRoomMessageSearchAdapter(
            object : OnMessageItemClick {
                override fun onItemClick(
                    item: MessageEntity,
                    position: Int,
                    itemView: View
                ) {
                    binding.searchRV.adapter = null
                    binding.searchRV.visibility = View.GONE
                    binding.scopeSectioned.visibility = View.GONE
                    binding.searchMask.visibility = View.GONE
                    binding.searchBottomBar.visibility = View.VISIBLE
                    searchSelectorIndex = position
                    setSearchSelector()
                    doScrollToSelectMessageItemAndSetKeyword(searchKeyword, item)
                }

                override fun onLoadMoreClick() {
                    chatViewModel.searchMoreMessage(roomId, searchKeyword)
                }
            },
            isGreenTheme
        )
    }

    val isFloatViewOpenAndExecuteClose: Boolean
        get() {
            // 先關閉進階選單
            val v = binding.chatKeyboardLayout.richMenuRecyclerView
            if (v != null && v.visibility == View.VISIBLE) {
                binding.chatKeyboardLayout.showKeyboard()
                for (m in chatViewModel.mainMessageData) {
                    m.isDelete = false
                    m.isShowSelection = false
                }
                setThemeStyle()
                showToolBar(true)
                screenShotData.clear()
                messageListAdapter?.switchAnonymousMode(false)
                messageListAdapter?.setAdapterMode(MessageAdapterMode.DEFAULT)
                chatViewModel.isObserverKeyboard = true
                return true
            }

            if (binding.chatKeyboardLayout.isOpenFuncView) {
                return true
            }

            // 關閉主題聊天室
            if (binding.themeMRV.visibility == View.VISIBLE) {
                binding.expandIV.tag = null
                binding.themeMRV.setMaxHeight(setupDefaultThemeHeight(2.0 / 3.0))
                binding.themeMRV.clearData()
                binding.themeMRV.visibility = View.GONE
                chatViewModel.isThemeOpen = false
                chatViewModel.themeMessage = null
                chatViewModel.nearMessage = null
                this.themeId = ""
                return true
            }
            return false
        }

    private val searchedMessage: MessageEntity? by lazy {
        if (intent.getSerializableExtra(BundleKey.EXTRA_MESSAGE.key()) != null) {
            intent.getSerializableExtra(BundleKey.EXTRA_MESSAGE.key()) as MessageEntity
        } else {
            null
        }
    }

    override fun onStart() {
        super.onStart()
        initActivityLaunch()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        onViewCreated()
        chatViewModel.setChatRoomNotDeleted()
    }

    private fun onViewCreated() {
        init()
        observerData()
        initListener()
        setup()
        initPopupWindows()
        clearAtMessage()
    }

    private fun init() =
        CoroutineScope(Dispatchers.IO).launch {
            intent?.let {
                if (it.hasExtra(BundleKey.EXTRA_SESSION_ID.key())) {
                    roomId = it.getStringExtra(BundleKey.EXTRA_SESSION_ID.key()).toString()
                    chatViewModel.setMessageRead(roomId)
                    val serializable = it.getSerializableExtra(BundleKey.EXTRA_ROOM_ENTITY.key())
                    chatViewModel.roomEntity =
                        if (serializable is ChatRoomEntity) {
                            serializable
                        } else {
                            ChatRoomReference.getInstance().findById2("", roomId, true, true, true, true, true)
                        }
                }

                // 交互時間
                if (it.hasExtra(BundleKey.WHERE_COME.key())) {
                    val whereCome = it.getStringExtra(BundleKey.WHERE_COME.key())
                    setWhereCome(whereCome)
                }

                userName = it.getStringExtra(BundleKey.USER_NICKNAME.key()).toString()
                val friendUserId = it.getStringExtra(BundleKey.USER_ID.key())

                chatViewModel.roomEntity?.let {
                    chatViewModel.setRoomType()
                    UserPref.getInstance(this@ChatNormalActivity).setCurrentRoomIds(it.id)
                    aiffManager = AiffManager(this@ChatNormalActivity, roomId)
                    chatViewModel.checkIsChatMember()
                    chatViewModel.getChatMemberFromService()
                    initChatRoom()
                } ?: run {
                    if (!Strings.isNullOrEmpty(friendUserId)) {
                        chatViewModel.getChatRoomEntity(friendUserId!!, userName)
                        initVirtualChatRoom()
                    } else if (roomId.isNotEmpty()) {
                        chatViewModel.getRoomItem(roomId)
                    } else {
                        // nothing
                    }
                }
            }
            chatViewModel.apply {
                sendUpdatedRoomTitle
                    .onEach { newTitle ->
                        roomEntity?.let {
                            it.name = newTitle
                            it.isCustomName = true
                            setTitleBar()
                        }
                    }.launchIn(this@ChatNormalActivity.lifecycleScope)
            }
        }

    private fun observerData() {
        homeViewModel.chatRoomUnreadNumber.observe(this) { count: Int -> setUnreadCount(count) }
        homeViewModel.serviceRoomUnreadNumber.observe(this) { count: Int -> setUnreadCount(count) }

        homeViewModel.sendLoginDevicesList.observe(this) {
            loginDevicesInfoAdapter.setTheme(isGreenTheme)
            loginDevicesInfoAdapter.submitList(it)
            binding.scopeDevices.visibility = View.VISIBLE
            binding.scopeDevicesList.visibility = View.VISIBLE
            binding.devicesNumber.text = String.format(it.size.toString())
            loginDevicesInfoAdapter.itemClickListener = { deviceRecordItem ->
                if (homeViewModel.timer.hasObservers()) homeViewModel.timer.removeObservers(this)
                doShowLoginDeviceSettingBottomSheetDialog(deviceRecordItem)
            }
        }

        homeViewModel.timer.observe(this) {
            if (it <= 0) {
                if (binding.scopeDevicesList.visibility == View.VISIBLE) {
                    binding.scopeDevicesList.visibility = View.GONE
                }
            }
        }
        homeViewModel.sendToast.observe(this) {
            Toast.makeText(this, getString(it), Toast.LENGTH_SHORT).show()
        }

        chatViewModel.chatRoomEntity.observe(this) {
            chatViewModel.setRoomType()
            UserPref.getInstance(this@ChatNormalActivity).setCurrentRoomIds(it.id)
//            UserPref.getInstance(this@ChatNormalActivity).currentRoomId = it.id
            initChatRoom()
            aiffManager = AiffManager(this@ChatNormalActivity, it.id)
            // 加完好友通知更新聊天室列表
            EventBus.getDefault().post(EventMsg<Any>(MsgConstant.REFRESH_ROOM_BY_LOCAL))
            // 新增聊天室後更新交互時間
            setWhereCome(newFriendRoomWhereCome)
            newFriendRoomWhereCome = null
        }

        chatViewModel.updateChatRoomEntity.observe(this) {
            chatViewModel.roomEntity = it
            setTitleBar()
            mentionSelectAdapter.setUserProfiles(it.membersLinkedList).refreshData()
            setKeyboardChatRoomEntity()
        }

        chatViewModel.sendUpdateMember.observe(this) {
            chatViewModel.roomEntity = it.second
            isDeletedMember = it.first
            setTitleBar()
            ChatRoomReference.getInstance().save(it.second)
            lifecycleScope.launch {
                val memberList = getSinkingGroupMemberList()
                withContext(Dispatchers.Main) {
                    chatRoomMembersAdapter?.setData(memberList)?.refreshData()
                    mentionSelectAdapter.setUserProfiles(it.second.membersLinkedList).refreshData()
                    setKeyboardChatRoomEntity()
                }
            }
        }

        chatViewModel.sendCloseChatActivity.observe(this) {
            if (it > 0) {
                ToastUtils.showToast(this, getString(it))
            }
            EventBus.getDefault().post(EventMsg<Any>(MsgConstant.NOTICE_REFRESH_CHAT_ROOM_LIST))
            finish()
        }

        // UpgradeGroup
        chatViewModel.upgradeGroup.observe(this) {
            CoroutineScope(Dispatchers.IO).launch {
                chatViewModel.roomEntity?.let { chatRoomEntity ->
                    chatRoomEntity.name = it.name
                    chatRoomEntity.type = ChatRoomType.group
                    chatRoomEntity.roomType = ChatRoomType.group
                    chatRoomEntity.ownerId = it.ownerId
                    chatRoomEntity.members.clear()
                    chatRoomEntity.memberIds.clear()
                    // 將擁有者放到第一個 再來是管理者 最後才是一般成員
                    val soredList = SortUtil.sortOwnerManagerByBoolean(it.members)
                    soredList.forEach {
                        chatRoomEntity.members.add(it)
                        chatRoomEntity.memberIds.add(it.id)
                        DBManager.getInstance().insertFriends(it)
                    }
                    chatRoomMembersAdapter?.refreshData()
                    setTitleBar()
                    AccountRoomRelReference.deleteRelByRoomId(null, chatRoomEntity.id)
                    AccountRoomRelReference.batchSaveByAccountIdsAndRoomId(
                        null,
                        chatRoomEntity.id,
                        chatRoomEntity.memberIds
                    )
                    ChatRoomReference.getInstance().save(chatRoomEntity)
                    DBManager.getInstance().insertGroup(it)
                    ChatRoomReference.getInstance().updateTitleById(chatRoomEntity.id, it.name)
                }
            }
        }

        // 顯示轉乘服務人員提示
        chatViewModel.showServiceNumberAddFromProvisionalDialog.observe(this) {
            showProvisionalToServiceNumber(it)
        }

        chatViewModel.userProfile.observe(this) {
            val bundle =
                bundleOf(
                    BundleKey.ACCOUNT_TYPE.key() to it.userType,
                    BundleKey.ACCOUNT_ID.key() to it.id,
                    BundleKey.WHERE_COME.key() to javaClass.simpleName
                )
            IntentUtil.startIntent(this, EmployeeInformationHomepageActivity::class.java, bundle)
        }

        chatViewModel.isRoomDismissSuccess.observe(this) {
            Toast.makeText(this, getString(R.string.text_disband_crowd_success), Toast.LENGTH_SHORT).show()
            finish()
        }

        chatViewModel.isChatMemberExitSuccess.observe(this) {
            Toast.makeText(this@ChatNormalActivity, getString(R.string.text_leave_crowd_success), Toast.LENGTH_SHORT).show()
            finish()
        }

        chatViewModel.isRoomMute.observe(this) {
            chatViewModel.roomEntity?.setMute(it)
        }

        chatViewModel.agentsList.observe(this) {
            chatViewModel.roomEntity?.agentsList = it
        }

        chatViewModel.errorMessage.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            if (it.equals(getString(R.string.text_not_find_any_result))) return@observe
            EventBus.getDefault().post(EventMsg<Any>(MsgConstant.REFRESH_ROOM_BY_LOCAL))
            homeViewModel.sendRefreshDB.postValue(true) // 刷新列表
            finish()
        }

        chatViewModel.memberList.observe(this) {
            chatViewModel.roomEntity?.members = it
        }
        chatViewModel.sendToast
            .onEach {
                ToastUtils.showToast(this, getString(it))
            }.launchIn(this@ChatNormalActivity.lifecycleScope)
        chatViewModel.sendToastByWord
            .onEach { string ->
                ToastUtils.showToast(this, string)
            }.launchIn(this@ChatNormalActivity.lifecycleScope)
        chatViewModel.sendGroupAddPermission
            .onEach { isGroupManagerOrOwner ->
                chatViewModel.roomEntity?.let {
                    if (!ChatRoomType.SERVICES_or_SUBSCRIBE_or_SYSTEM_or_PERSON_or_SERVICE_MEMBER.contains(it.type)) {
                        if (it.type == ChatRoomType.group && !isGroupManagerOrOwner) {
                            binding.inviteIV.visibility = View.GONE
                        } else {
                            binding.inviteIV.visibility = View.VISIBLE
                        }
                    } else {
                        binding.inviteIV.visibility = View.GONE
                    }
                }
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

//        chatViewModel.chatRoomEntity.observe(
//            this
//        ) { chatRoomEntity: ChatRoomEntity ->
//            onRefreshMemberList(chatRoomEntity.provisionalIds)
//        }

        chatViewModel.managerList.observe(
            this
        ) { groupRoomManager: List<ChatRoomMemberResponse>? ->
            filterToShowNoOwnerNotify(
                groupRoomManager
            )
        }

        chatViewModel.becomeOwnerStatus.observe(
            this
        ) { string: String? ->
            ToastUtils.showToast(this, string)
            binding.clNotifyNoOwner.visibility = View.GONE
        }

        chatViewModel.errorMessage.observe(
            this
        ) { errorMessage: String? ->
            ToastUtils.showToast(this, errorMessage)
        }

        chatViewModel.isCancelAiWarningSuccess.observe(
            this
        ) { isSuccess: Boolean ->
            hideLoadingView()
            if (isSuccess) {
                Toast
                    .makeText(
                        this,
                        getString(R.string.text_ai_warning_return),
                        Toast.LENGTH_SHORT
                    ).show()
            } else {
                Toast
                    .makeText(
                        this,
                        getString(R.string.text_toast_operator_failure),
                        Toast.LENGTH_SHORT
                    ).show()
            }
        }

        chatViewModel.consultList.observe(
            this
        ) {
            if (it.isEmpty()) {
                chatViewModel.roomEntity?.let {
                    it.aiConsultId = ""
                }
            }

            binding.rvBottomRoomList.visibility = if (it.isNotEmpty()) View.VISIBLE else View.GONE
            advisoryRoomAdapter.setData(it)
        }

        chatViewModel.consultRoomId.observe(
            this
        ) { consultRoomId: Pair<Boolean, String?> ->
            if (consultRoomId.first) {
                chatViewModel.roomEntity?.let {
                    it.aiConsultId = consultRoomId.second
                }
            } else {
                val bundle = Bundle()
                bundle.putString(BundleKey.EXTRA_SESSION_ID.key(), consultRoomId.second)
                startIntent(this, ChatActivity::class.java, bundle)
            }
        }

        chatViewModel.startConsultError.observe(
            this
        ) { errorData: ApiErrorData ->
            AlertView
                .Builder()
                .setContext(this)
                .setStyle(AlertView.Style.Alert)
                .setMessage(errorData.errorMessage)
                .setOthers(arrayOf(getString(R.string.alert_confirm)))
                .setOnItemClickListener { _: Any?, _: Int -> }
                .build()
                .setCancelable(true)
                .setOnDismissListener(null)
                .show()
        }

        chatViewModel.onProvisionalIdsGet.observe(
            this
        ) { provisionalIds: List<String> ->
            chatViewModel.getMemberProfileEntity(provisionalIds)
        }

        chatViewModel.sendCloseExtraLayout.observe(
            this
        ) { isClose: Boolean ->
            if (isClose) {
                binding.chatKeyboardLayout.doExtraAction(binding.chatKeyboardLayout)
                hideLoadingView()
            }
        }
        chatViewModel.hideLoadingDialog.observe(
            this
        ) { isHide: Boolean ->
            if (isHide) hideLoadingView()
        }

        chatViewModel.sendChatRoomTitleChangeListener
            .onEach {
                binding.title.text = it.senderName ?: ""
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

        chatViewModel.sendThemeMRVData
            .onEach {
                binding.themeMRV.setData(
                    if (Strings.isNullOrEmpty(it.themeId)) it.nearMessageId else it.themeId,
                    it
                )
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

//        chatViewModel.sendBindingDataHandled.onEach {
//            handleBindingData(it.first, it.second, it.third)
//        }.launchIn(this@ChatNormalActivity.lifecycleScope)

        chatViewModel.sendScrollToTop
            .onEach {
//            scrollToTop()
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

        chatViewModel.sendHighLightUnReadLine
            .onEach {
                highLightUnReadLine(it)
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

        chatViewModel.sendShowIsNotMemberMessage
            .onEach {
                showIsNotMemberMessage(it)
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

        chatViewModel.sendShowLoadingView
            .onEach {
                showLoadingView(it)
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

        chatViewModel.sendHideLoadingView
            .onEach {
                hideLoadingView()
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

        chatViewModel.sendShowErrorMsg
            .onEach {
                it?.let { message ->
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

        chatViewModel.sendTransferModeDisplay
            .onEach {
                transferModeDisplay(it)
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

        chatViewModel.sendSetupAppointStatus
            .onEach {
                setup()
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

        chatViewModel.sendRobotStopStatus
            .onEach {
                finish()
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

        chatViewModel.sendDisplayThemeMessage
            .onEach {
                displayThemeMessage(it.first, it.second, it.third)
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

        chatViewModel.sendShowSendVideoProgress
            .onEach {
                showSendVideoProgress(it)
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

        chatViewModel.sendOnRefreshMore
            .onEach {
                stopRefresh()
                messageListAdapter?.submitList(it.first)
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

        chatViewModel.sendUpdateFacebookStatus
            .onEach {
                updateFacebookStatus(it)
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

        chatViewModel.sendShowToast
            .onEach {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

        chatViewModel.sendShowErrorToast
            .onEach {
                showErrorToast(it)
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

        chatViewModel.sendClearTypedMessage
            .onEach {
                binding.chatKeyboardLayout.clearInputArea()
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

        chatViewModel.sendRefreshListView
            .onEach {
                refreshListView()
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

        chatViewModel.sendShowNoMoreMessage
            .onEach {
                showNoMoreMessage()
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

        chatViewModel.sendUpdateMsgStatusPair
            .onEach {
                updateMsgStatus(it.first, it.second)
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

        chatViewModel.sendUpdateSendVideoProgress
            .onEach {
                updateSendVideoProgress()
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

        chatViewModel.sendUpdateSendVideoProgressInt
            .onEach {
                updateSendVideoProgress(it)
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

        chatViewModel.sendUpdateMsgProgress
            .onEach {
                updateMsgProgress(it.first, it.second)
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

        chatViewModel.sendUpdateMsgStatus
            .onEach {
                updateMsgStatus(it.second, it.third)
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

        chatViewModel.sendDismissSendVideoProgress
            .onEach {
                dismissSendVideoProgress()
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

        chatViewModel.sendSetThemeOpen
            .onEach {
                setThemeOpen(it)
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

        chatViewModel.sendMessageToUI
            .onEach {
                sendMessage(it)
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

        chatViewModel.sendSetup
            .onEach {
                setup()
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

        chatViewModel.sendSetIsBlock
            .onEach {
                binding.chatKeyboardLayout.setIsBlock(it)
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

        chatViewModel.sendNavigateToChat
            .onEach {
                ActivityTransitionsControl.navigateToChat(
                    this,
                    it,
                    ChatFragment::class.java.simpleName
                ) { intent: Intent, _: String ->
                    start(this, intent)
                }
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

        chatViewModel.sendSetChatDisable
            .onEach {
                setChatDisable(it)
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

        chatViewModel.sendSetChatEnable
            .onEach {
                binding.chatKeyboardLayout.setChatEnable()
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

        chatViewModel.scopeRetractTipInvisible
            .onEach {
                binding.scopeRetractTip.visibility = View.GONE
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

        chatViewModel.sendBindingBusiness
            .onEach {
                bindingBusiness(it.first, it.second, it.third)
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

        chatViewModel.stopLoadMoreMessage
            .onEach {
                if (it) stopRefresh()
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

        // 設置第一次的訊息
        chatViewModel.mainMessageDataFlow
            .onEach { pair ->
                messageListAdapter?.apply {
                    submitList(pair.second)
                    chatViewModel.roomEntity?.let { setChatMembers(it.members) }
                }

                if (pair.first) {
                    setUnfinishedMessage()
                    initKeyboard()
                    initBottomRoomList()
                    setMentionSelectList()
                    chatViewModel.roomEntity?.let {
                        App.getInstance().chatRoomId = it.id
                    }
                }
                setStartPosition(pair.first, pair.second.size - 1)
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

        chatViewModel.scrollListToPosition
            .onEach { position ->
                if (searchKeyword.isEmpty()) {
                    binding.messageRV.postDelayed({
                        (binding.messageRV.layoutManager as LinearLayoutManager?)?.scrollToPositionWithOffset(
                            position,
                            0
                        )
                    }, 50)
                }
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

        chatViewModel.updateFacebookCommentStats
            .onEach { message ->
                updateFacebookStatus(message)
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

        chatViewModel.onMessageReceived
            .onEach { messageList ->
                val isAlreadyScrolledToBottom = binding.messageRV.isScrolledToBottom()
                if (isAlreadyScrolledToBottom) {
                    scrollToCurrentMessage(messageList.size)
                }
                messageListAdapter?.submitList(messageList)
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

        chatViewModel.refreshCurrentMessagePosition
            .onEach {
                messageListAdapter?.notifyItemChanged(it, false)
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

        chatViewModel.notifyRecyclerRemove
            .onEach {
                messageListAdapter?.notifyItemRemoved(it)
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

        chatViewModel.onAvatarIconClickIntent
            .onEach {
                startActivity(it)
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

        chatViewModel.onReplyNearMessageClick
            .onEach {
                onThemeNearMessageClick(it)
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

        chatViewModel.searchResult
            .onEach {
                binding.scopeSectioned.visibility = View.VISIBLE
                chatViewModel.roomEntity?.let { chatRoom ->
                    chatRoomMessageSearchAdapter.setData(it.second, it.first, chatRoom.members)
                }
                val size = if (it.second.any { it is LoadMoreEntity }) it.second.size - 1 else it.second.size
                binding.sectionedTitle.text = getString(R.string.text_sectioned_search_news, size)
            }.launchIn(this@ChatNormalActivity.lifecycleScope)

        chatViewModel.onMessageRead
            .onEach {
                getUnreadData()
            }.launchIn(this@ChatNormalActivity.lifecycleScope)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListener() {
        binding.inviteIV.setOnClickListener {
            chatViewModel.roomEntity?.let { roomEntity ->
                when (roomEntity.roomType) {
                    ChatRoomType.group, ChatRoomType.discuss -> {
                        chatViewModel.getChatHomePage(roomEntity.id)
                        addMember()
                    }

                    ChatRoomType.friend -> {
                        if (roomEntity.memberIds.isNotEmpty()) {
                            val bundle =
                                bundleOf(
                                    BundleKey.ACCOUNT_IDS.key() to roomEntity.memberIds
                                )
                            IntentUtil.startIntent(this, CreateDiscussActivity::class.java, bundle)
                        } else {
                            roomEntity.id?.let {
                                CoroutineScope(Dispatchers.Main).launch {
                                    val list = chatViewModel.doGetChatRoomMemberIds(it).single()
                                    if (list.isNotEmpty()) {
                                        val bundle =
                                            bundleOf(
                                                BundleKey.ACCOUNT_IDS.key() to list
                                            )
                                        IntentUtil.startIntent(this@ChatNormalActivity, CreateDiscussActivity::class.java, bundle)
                                    } else {
                                        Toast.makeText(this@ChatNormalActivity, getString(R.string.text_new_chat_room_failure), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        // nothing
                    }
                }
            }
        }
        binding.leftAction.setOnClickListener {
            KeyboardHelper.hide(it)
            chatViewModel.roomEntity?.let {
                UserPref.getInstance(this).removeCurrentRoomId(it.id)
            }
//            UserPref.getInstance(this).currentRoomId = ""
            finish()
            overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out)
        }
        binding.rightAction.setOnClickListener {
            if (!isFinishing) rightViewToggle()
        }

        binding.ivChannel.setOnClickListener {
            if (chatFragment is ChatFragment) {
                (chatFragment as ChatFragment).doChannelChangeAction()
            }
        }

        binding.titleBar.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                chatViewModel.roomEntity?.let {
                    when (it.type) {
                        ChatRoomType.person -> {
                            val bundle =
                                bundleOf(
                                    BundleKey.ACCOUNT_ID.key() to selfUserId,
                                    BundleKey.ACCOUNT_TYPE.key() to UserType.EMPLOYEE.name,
                                    BundleKey.WHERE_COME.key() to javaClass.simpleName
                                )
                            IntentUtil.startIntent(
                                this@ChatNormalActivity,
                                SelfInformationHomepageActivity::class.java,
                                bundle
                            )
                        }

                        ChatRoomType.services -> {
                            // 物件聊天室 好像沒了
//                        if (!it.businessId.isNullOrEmpty()) {
//                            val businessEntities = ChatRoomReference.getInstance().findServiceBusinessByServiceNumberIdAndOwnerIdAndNotRoomId(it.serviceNumberId, it.ownerId, it.id, ChatRoomType.services)
//                            showSinkingBusinessMemberListPopupWindow(businessEntities)
//                        }
                            val profile = UserProfileReference.findById(null, it.ownerId)
                            profile?.let { userProfile ->
                                if (UserType.VISITOR == userProfile.userType || UserType.CONTACT == userProfile.userType) {
                                    if (!checkClientMainPageFromAiff()) {
                                        val bundle =
                                            bundleOf(
                                                BundleKey.ACCOUNT_TYPE.key() to userProfile.userType,
                                                BundleKey.ACCOUNT_ID.key() to it.ownerId,
                                                BundleKey.ROOM_ID.key() to it.id,
                                                BundleKey.USER_NICKNAME.key() to userProfile.nickName,
                                                BundleKey.WHERE_COME.key() to javaClass.simpleName
                                            )
                                        IntentUtil.startIntent(this@ChatNormalActivity, VisitorHomepageActivity::class.java, bundle)
                                    }
                                } else {
                                    val bundle =
                                        bundleOf(
                                            BundleKey.ACCOUNT_TYPE.key() to userProfile.userType,
                                            BundleKey.ACCOUNT_ID.key() to userProfile.id,
                                            BundleKey.WHERE_COME.key() to javaClass.simpleName
                                        )
                                    IntentUtil.startIntent(this@ChatNormalActivity, EmployeeInformationHomepageActivity::class.java, bundle)
                                }
                            }
                        }

                        ChatRoomType.group, ChatRoomType.discuss -> {
                            memberPopupWindow?.let {
                                if (it.isShowing) return@launch
                            }
                            showSinkingMemberListPopupWindow()
                        }

                        ChatRoomType.friend, ChatRoomType.strange -> {
                            if (!it.businessId.isNullOrEmpty()) {
//                                showSinkingBusinessMemberListPopupWindow()
                            } else {
                                val targetMember = it.memberIds.find { id -> id != selfUserId }
                                targetMember?.let { id ->
                                    chatViewModel.getUserItem(id)
                                }
                            }
                        }

                        ChatRoomType.serviceMember -> {
                            showSinkingServiceMemberListPopupWindow()
                        }

                        ChatRoomType.subscribe -> {
                            val bundle =
                                bundleOf(
                                    BundleKey.SUBSCRIBE_NUMBER_ID.key() to it.serviceNumberId,
                                    BundleKey.ROOM_ID.key() to it.id,
                                    BundleKey.IS_SUBSCRIBE.key() to true,
                                    BundleKey.WHERE_COME.key() to javaClass.simpleName
                                )
                            IntentUtil.startIntent(this@ChatNormalActivity, SubscribeInformationHomepageActivity::class.java, bundle)
                            overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out)
                        }

                        else -> {}
                    }
                }
            }
        }

        binding.difbDown.setOnClickListener {
            if (binding.messageRV.scrollState != 0) {
                return@setOnClickListener
            }
            hideDifbDown(10f)
            scrollToCurrentMessage(binding.messageRV.adapter!!.itemCount - 1)
            if (binding.floatingLastMessageTV.visibility == View.VISIBLE) {
                binding.floatingLastMessageTV.visibility = View.GONE
                doGettingNewMessage = false
            }
        }

        binding.expandIV.setOnClickListener {
            if (it.tag == null) { // 放大
                it.tag = "change height"
                binding.expandIV.setImageResource(R.drawable.collapse_white)
                binding.themeMRV.setMaxHeight(setupDefaultThemeHeight(0.0))
                binding.themeMRV.refreshData()
            } else { // 縮小至最高2/3
                it.tag = null
                binding.themeMRV.setMaxHeight(setupDefaultThemeHeight(2.0 / 3.0))
                binding.themeMRV.refreshData()
                binding.expandIV.setImageResource(R.drawable.expand_white)
            }
        }

        binding.themeCloseIV.setOnClickListener {
            binding.expandIV.tag = null
            binding.themeMRV.setMaxHeight(setupDefaultThemeHeight(2.0 / 3.0))
            binding.themeMRV.clearData()
            binding.themeMRV.visibility = View.GONE
            chatViewModel.isThemeOpen = false
            chatViewModel.themeMessage = null
            chatViewModel.nearMessage = null
            this.themeId = ""
            isReply = false
        }

        binding.floatingLastMessageTV.setOnClickListener {
            it.visibility = View.GONE
            val linearLayoutManager = binding.messageRV.layoutManager as LinearLayoutManager
            scrollToCurrentMessage(linearLayoutManager.itemCount - 1)
        }

        // 點擊子聊天室時可以關閉且不會擋到滑動 recyclerview
        binding.themeMRV.setOnTouchListener { p0, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_SCROLL, MotionEvent.ACTION_DOWN ->
                    subRoomOnTouchDownTime =
                        System.currentTimeMillis()

                MotionEvent.ACTION_UP -> {
                    val upTime = System.currentTimeMillis()
                    if (upTime - subRoomOnTouchDownTime < 70) {
                        doThemeCloseAction()
                    }
                }
            }
            false
        }

        binding.etSearch.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                }

                override fun afterTextChanged(s: Editable) {
                    binding.clearInput.visibility = if (s.toString().isEmpty()) View.GONE else View.VISIBLE
                }
            }
        )

        binding.clearInput.setOnClickListener { v: View? ->
            binding.etSearch.text?.clear()
            chatRoomMessageSearchAdapter.setData(
                Lists.newArrayList(),
                "",
                Lists.newArrayList()
            )
            KeyboardHelper.open(binding.etSearch)
            enterSearchMode()
        }

        binding.searchCancelTV.setOnClickListener {
            KeyboardHelper.hide(binding.searchCancelTV)
            exitSearchMode()
        }

        binding.etSearch.setOnEditorActionListener { v: TextView, actionId: Int, event: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (Strings.isNullOrEmpty(v.text.toString().trim { it <= ' ' })) {
                    Toast
                        .makeText(
                            this,
                            getString(R.string.text_input_search_key_word),
                            Toast.LENGTH_SHORT
                        ).show()
                } else {
                    binding.searchRV.adapter = chatRoomMessageSearchAdapter
                    binding.searchBottomBar.visibility = View.GONE
                    enterSearchMode()
                    KeyboardHelper.hide(binding.etSearch)
                    searchKeyword = v.text.toString()
                    chatViewModel.search(searchKeyword)
                    binding.etSearch.clearFocus()
                }
                return@setOnEditorActionListener true
            }
            false
        }

        binding.scopeSectioned.setOnClickListener {
            binding.searchRV.visibility = if (binding.searchRV.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            binding.icExtend.setImageResource(if (binding.searchRV.visibility == View.VISIBLE) R.drawable.ic_arrow_top else R.drawable.ic_arrow_down)
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (isSearchMode) {
                        exitSearchMode()
                    } else {
                        finish()
                    }
                }
            }
        )

        binding.downIV.setOnClickListener {
            onSearchResultDownClick()
        }

        binding.upIV.setOnClickListener {
            onSearchResultUpClick()
        }

        binding.xrefreshLayout
            .setOnRefreshListener(this@ChatNormalActivity)
            .setOnBackgroundClickListener(this@ChatNormalActivity)
    }

    private fun getUnreadData() =
        CoroutineScope(Dispatchers.IO).launch {
            chatViewModel.roomEntity?.let {
                // -1 是被左滑標記未讀
                if (it.unReadNum > 0 || it.unReadNum == -1) {
                    homeViewModel.setRead(it)
                    DBManager.getInstance().setChatRoomListItemInteractionTime(it.id)
                    it.updateTime = System.currentTimeMillis()
                    isNeedRefreshList = true
                }
                // 如果在服務號列表下，只拿服務號列表的未讀數
                if (it.listClassify == ChatRoomSource.SERVICE) {
                    homeViewModel.getServiceRoomUnReadSum()
                } else {
                    homeViewModel.getChatRoomListUnReadSum()
                }
            }
        }

    private fun initActivityLaunch() {
        addMemberARL =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    val bundle = it.data?.extras
                    bundle?.let {
                        val listType = object : TypeToken<List<UserProfileEntity>>() {}.type
                        val data = JsonHelper.getInstance().from<List<UserProfileEntity>>(bundle.getString("data"), listType)
                        onInviteSuccess(data)
                    }
                }
            }

        updateGroupARL =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    it.data?.let { data ->
                        val sessionId = data.getStringExtra(BundleKey.EXTRA_SESSION_ID.key())
                        val groupName = data.getStringExtra(BundleKey.EXTRA_TITLE.key())
                        chatViewModel.roomEntity = data.getSerializableExtra(BundleKey.EXTRA_SESSION.key()) as ChatRoomEntity?
                        chatViewModel.roomEntity?.let {
                            it.roomType = ChatRoomType.group
                            it.name = groupName
                            init()
                        } ?: run {
                            chatViewModel.roomEntity = ChatRoomReference.getInstance().findById(sessionId)
                        }
                    }
                }
            }

        toGroupSessionARL =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    it.data?.let { data ->
                        val extra = data.getStringArrayExtra(Constant.ACTIVITY_RESULT)
                        extra?.let {
                            chatViewModel.roomEntity?.name = extra[0]
                            setTitleBar()
                        }
                    }
                }
            }

        addProvisionalMemberARL =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                CoroutineScope(Dispatchers.IO).launch {
                    if (it.resultCode == RESULT_OK) {
                        val bundle = it.data?.extras
                        bundle?.let {
                            val listType = object : TypeToken<List<String>>() {}.type
                            val data = JsonHelper.getInstance().from<List<String>>(bundle.getString("data"), listType)
                            if (chatFragment is ChatFragment) {
                                (chatFragment as ChatFragment).onRefreshMemberList(data)
                            }
                        }
                    }
                }
            }

        mediaPreviewARL =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    binding.funMedia.visibility = View.GONE
                    binding.chatKeyboardLayout.clearIconState()
                    result.data?.let { intent ->
                        val selectorJson = intent.extras?.getString("data")
                        val selectorData =
                            JsonHelper.getInstance().fromToMap<String, String>(selectorJson)
                        val isOriginalStr = selectorData["isOriginal"]
                        val listStr = selectorData["list"]

                        val list =
                            JsonHelper.getInstance().fromToList(
                                listStr,
                                Array<ImageBean>::class.java
                            )
                        sendFileSize = list.size
                        isSendSingleFile = sendFileSize == 1
                        CELog.e(selectorJson)
                        executeSendPhotos(
                            Lists.newArrayList<AMediaBean>(list),
                            isOriginalStr.toBoolean()
                        )
                    }
                }
            }
    }

    // 更新交互時間
    private fun setWhereCome(whereCome: String?) =
        CoroutineScope(Dispatchers.Main).launch {
            whereCome?.let {
                chatViewModel.roomEntity?.let { entity ->
                    if ((
                            it == ContactPersonFragment::class.java.simpleName ||
                                it == CommunitiesSearchFragment::class.java.simpleName ||
                                it == ServiceNumberSearchFragment::class.java.simpleName ||
                                it == ContactPersonClientSearchFragment::class.java.simpleName
                        ) &&
                        !entity.type.equals(ChatRoomType.person)
                    ) {
                        DBManager.getInstance().setChatRoomListItemInteractionTime(entity.id) // 個人聊天室不更新交互時間
                        entity.updateTime = System.currentTimeMillis()
                        EventBusUtils.sendEvent(EventMsg<Any?>(MsgConstant.NOTICE_REFRESH_CHAT_ROOM_LIST)) // 更新列表
                    }
                } ?: run {
                    // 針對還沒有好友聊天室的情況
                    newFriendRoomWhereCome = it
                }
            }
        }

    private fun initVirtualChatRoom() =
        CoroutineScope(Dispatchers.Main).launch {
            binding.title.text = getTitleText()
            setMessageList()
        }

    private fun initChatRoom() =
        CoroutineScope(Dispatchers.IO).launch {
            chatViewModel.roomEntity?.let { chatRoomEntity ->
                if (chatRoomEntity.roomType == ChatRoomType.undef) chatViewModel.setRoomType()
                when (chatRoomEntity.roomType) {
                    ChatRoomType.serviceMember,
                    ChatRoomType.bossServiceNumber,
                    ChatRoomType.services,
                    ChatRoomType.serviceINumberStaff,
                    ChatRoomType.serviceONumberStaff,
                    ChatRoomType.provisional,
                    ChatRoomType.bossSecretary
                    -> {
                        themeStyle = RoomThemeStyle.SERVICES
                        ThemeHelper.updateServiceChatRoomTheme(isServiceRoom = true)
                    }

                    ChatRoomType.bossOwnerWithSecretary,
                    ChatRoomType.bossOwner,
                    ChatRoomType.group,
                    ChatRoomType.friend,
                    ChatRoomType.person,
                    ChatRoomType.discuss,
                    ChatRoomType.system
                    -> ThemeHelper.updateServiceChatRoomTheme(isServiceRoom = false)

                    else -> {
                        if (!Strings.isNullOrEmpty(chatRoomEntity.businessId)) {
                            themeStyle = RoomThemeStyle.BUSINESS
                        }
                        ThemeHelper.updateServiceChatRoomTheme(isServiceRoom = false)
                    }
                }
                setMessageList()
                setThemeStyle()
                setTitleBar()
                setLoginDevicesStatus()
                setContainer(chatRoomEntity)

                if (chatRoomEntity.roomType == ChatRoomType.serviceMember ||
                    chatRoomEntity.roomType == ChatRoomType.bossSecretary ||
                    chatRoomEntity.roomType == ChatRoomType.bossOwnerWithSecretary
                ) {
                    chatViewModel.getServiceEntity(chatRoomEntity.serviceNumberId)
                }
                getMembers(chatRoomEntity)
                if (chatRoomEntity.serviceNumberOpenType.contains("O") && chatRoomEntity.type == ChatRoomType.services) {
                    chatViewModel.getBusinessCardInfo(chatRoomEntity.serviceNumberId) // 取得電子名片設定
                    chatViewModel.getLastChannelFrom(chatRoomEntity.id) // 取得渠道名稱以發送電子名片
                }
            }
        }

    private fun getMembers(chatRoomEntity: ChatRoomEntity?) =
        CoroutineScope(Dispatchers.IO).launch {
            chatRoomEntity?.let {
                chatViewModel.getChatMemberList(it)
            }
        }

    private fun setThemeStyle() =
        CoroutineScope(Dispatchers.Main).launch {
            isGreenTheme = ThemeHelper.isGreenTheme()
            // 服務號以外且是green主題都呈現綠色
            window.statusBarColor = if (isGreenTheme && themeStyle != RoomThemeStyle.SERVICES) resources.getColor(R.color.color_015F57, null) else themeStyle.mainColor
            binding.titleBar.setBackgroundColor(if (isGreenTheme && themeStyle != RoomThemeStyle.SERVICES) resources.getColor(R.color.color_015F57, null) else themeStyle.mainColor)
            binding.searchBar.setBackgroundColor(if (isGreenTheme && themeStyle != RoomThemeStyle.SERVICES) resources.getColor(R.color.color_015F57, null) else themeStyle.mainColor)
            binding.floatTimeBoxTV.setBackgroundResource(themeStyle.floatingTimeBox)
            binding.xrefreshLayout.setBackgroundColor(themeStyle.auxiliaryColor)
            binding.messageRV.setBackgroundColor(themeStyle.auxiliaryColor)
            binding.chatKeyboardLayout.setThemeStyle(themeStyle)
        }

    // 設置 title bar
    private fun setTitleBar() =
        CoroutineScope(Dispatchers.Main).launch {
            chatViewModel.roomEntity?.let { roomEntity ->
                if (roomEntity.roomType == ChatRoomType.undef) chatViewModel.setRoomType()
                when (roomEntity.roomType) {
                    // 專業服務號
                    ChatRoomType.services -> {
                        binding.title.text = getTitleText()
                        getServiceIcon(roomEntity.ownerId)
                    }

                    // 服務號成員聊天室
                    ChatRoomType.serviceMember -> {
                        val drawable =
                            if (roomEntity.serviceNumberType == ServiceNumberType.BOSS && roomEntity.ownerId == selfUserId) {
                                if (isGreenTheme) R.drawable.ic_service_member_b_green else R.drawable.ic_service_member_b
                            } else {
                                if (isGreenTheme) R.drawable.ic_service_member_group__green_16dp else R.drawable.ic_service_member_group_16dp
                            }

                        binding.title.text =
                            TextViewHelper.setLeftImage(
                                this@ChatNormalActivity,
                                getTitleText(),
                                drawable
                            )
                    }

                    // 商務號
                    ChatRoomType.bossServiceNumber,
                    // 外部服務號服務人員
                    ChatRoomType.serviceONumberStaff,
                    // 商務號擁有者
                    ChatRoomType.bossOwner
                    -> {
                        getServiceIcon(roomEntity.ownerId)
                    }

                    // 商務號秘書聊天室
                    ChatRoomType.bossSecretary -> {
                        if (roomEntity.serviceNumberOwnerId == selfUserId) {
                            binding.title.text =
                                TextViewHelper.setLeftImage(
                                    this@ChatNormalActivity,
                                    getTitleText(),
                                    if (isGreenTheme) R.drawable.ic_service_member_b_green else R.drawable.ic_service_member_b
                                )
                        } else {
                            binding.title.text =
                                TextViewHelper.setLeftImage(
                                    this@ChatNormalActivity,
                                    getTitleText(),
                                    if (isGreenTheme) R.drawable.ic_service_member_group__green_16dp else R.drawable.ic_service_member_group_16dp
                                )
                        }
                    }

                    // 群組聊天室
                    ChatRoomType.group -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            // 判斷是否顯示 + 按鈕
                            chatViewModel.doHandleGroupMemberPrivilege()

                            var memberSize = 0
                            roomEntity.chatRoomMember?.let { memberIds ->
                                if (memberIds.isNotEmpty()) {
                                    memberSize = memberIds.size
                                } else {
                                    val groupInfo = DBManager.getInstance().queryGroupInfo(roomEntity.id)
                                    groupInfo?.let { group ->
                                        memberSize = group.memberIds?.size ?: -1
                                    }
                                }
                            } ?: run {
                                roomEntity.memberIds?.let {
                                    memberSize = it.size
                                }
                            }
                            CoroutineScope(Dispatchers.Main).launch {
                                binding.title.text =
                                    TextViewHelper.getTitleWithIcon(
                                        binding.title.context,
                                        roomEntity.name,
                                        memberSize,
                                        R.drawable.icon_group_chat_room
                                    )
                            }
                        }
                    }

                    // 個人
                    ChatRoomType.person -> {
                        binding.title.text =
                            TextViewHelper.setLeftImage(
                                this@ChatNormalActivity,
                                getTitleText(),
                                if (isGreenTheme) R.drawable.ic_green_self_room_20dp else R.drawable.icon_self_chat_room_20dp
                            )
                    }

                    // 多人
                    ChatRoomType.discuss -> {
                        binding.title.text = getTitleText()
                        binding.inviteIV.visibility = View.VISIBLE // 多人聊天室一律顯示 + (新增成員)
                    }

                    // 內部服務號服務人員
                    ChatRoomType.serviceINumberStaff -> {
                        binding.title.text = getTitleText()
                    }

                    ChatRoomType.serviceINumberAsker -> {
                        binding.title.text =
                            TextViewHelper.setLeftImage(
                                this@ChatNormalActivity,
                                getTitleText(),
                                R.drawable.icon_subscribe_number_pink_15dp
                            )
                    }

                    ChatRoomType.system -> {
                        val systemUserName = TokenPref.getInstance(this@ChatNormalActivity).systemUserName
                        binding.title.text = if (systemUserName.isNullOrEmpty()) getTitleText() else systemUserName
                    }

                    ChatRoomType.friend -> {
                        binding.inviteIV.visibility = View.VISIBLE
                        binding.rightAction.setImageResource(R.drawable.icon_aipower_open)
                        TokenPref.getInstance(this@ChatNormalActivity).isEnableCall
                        binding.title.text = getTitleText()
                    }

                    else -> {
                        binding.rightAction.setImageResource(R.drawable.icon_aipower_open)
                        TokenPref.getInstance(this@ChatNormalActivity).isEnableCall
                        binding.title.text = getTitleText()
                    }
                }
            } ?: run {
                if (selfUserId.isNullOrEmpty()) {
                    binding.rightAction.visibility = View.GONE
                }
            }
        }

    private fun getServiceIcon(roomOwnerId: String) =
        CoroutineScope(Dispatchers.IO).launch {
            UserProfileService.getProfileIsEmployee(
                this@ChatNormalActivity,
                roomOwnerId,
                object : ServiceCallBack<UserType, RefreshSource> {
                    override fun complete(
                        type: UserType,
                        refreshSource: RefreshSource
                    ) {
                        CoroutineScope(Dispatchers.Main).launch {
                            when (type) {
                                UserType.VISITOR ->
                                    binding.title.text =
                                        TextViewHelper.setLeftImage(
                                            this@ChatNormalActivity,
                                            chatViewModel.roomEntity?.name,
                                            R.drawable.ic_visitor_15dp
                                        )

                                UserType.CONTACT ->
                                    binding.title.text =
                                        TextViewHelper.setLeftImage(
                                            this@ChatNormalActivity,
                                            chatViewModel.roomEntity?.name,
                                            R.drawable.ic_customer_15dp
                                        )

                                else -> {
                                    binding.title.text = chatViewModel.roomEntity?.name
                                }
                            }
                        }
                    }

                    override fun error(message: String) {
                        CoroutineScope(Dispatchers.Main).launch {
                            binding.title.text =
                                TextViewHelper.setLeftImage(
                                    this@ChatNormalActivity,
                                    chatViewModel.roomEntity?.name,
                                    R.drawable.ic_customer_15dp
                                )
                        }
                    }
                }
            )
        }

    private fun setLoginDevicesStatus() =
        CoroutineScope(Dispatchers.IO).launch {
            chatViewModel.roomEntity?.let {
                if (it.roomType == ChatRoomType.person) {
                    binding.RvLoginDevices.adapter = loginDevicesInfoAdapter
                    homeViewModel.startCountdown(5)
                    homeViewModel.getLoginDevicesList()
                    CoroutineScope(Dispatchers.Main).launch {
                        binding.ivDevices.setOnClickListener {
                            if (binding.scopeDevicesList.isGone) {
                                binding.scopeDevicesList.visibility = View.VISIBLE
                            } else {
                                binding.scopeDevicesList.visibility = View.GONE
                            }
                        }
                    }
                }
            }
        }

    private fun setContainer(chatRoomEntity: ChatRoomEntity) =
        CoroutineScope(Dispatchers.Main).launch {
            binding.themeMRV.setContainer(chatRoomEntity)
        }

    // 取得各聊天室標題
    private suspend fun getTitleText(): String =
        withContext(Dispatchers.IO) {
//        val localChatRoomEntity =  ChatRoomReference.getInstance().findById(roomId)
            chatViewModel.roomEntity?.let {
                if (!Strings.isNullOrEmpty(it.businessName)) {
                    withContext(Dispatchers.Main) {
                        binding.tvBusinessName.visibility = View.VISIBLE
                        binding.tvBusinessName.text = MessageFormat.format("{0}", it.businessName)
                    }
                }
                when (it.roomType) {
                    // 商務號擁有者
                    ChatRoomType.bossOwner -> {
                        return@withContext it.getServicesNumberTitle(selfUserId) ?: ""
                    }

                    // 商務號秘書聊天室
                    ChatRoomType.bossSecretary,
                    ChatRoomType.bossOwnerWithSecretary
                    -> {
                        withContext(Dispatchers.Main) {
                            binding.tvBusinessName.visibility = View.VISIBLE
                            binding.tvBusinessName.text = it.serviceNumberName
                        }
                        return@withContext getString(R.string.text_chat_room_service_member_business, "(" + it.memberIds.size + ")")
                    }

                    // 內部服務號服務人員
                    ChatRoomType.serviceINumberStaff -> {
                        val customProfile = DBManager.getInstance().queryUser(it.ownerId)
                        return@withContext customProfile?.nickName ?: ""
                    }

                    ChatRoomType.serviceINumberAsker,
                    // 服務號
                    ChatRoomType.services
                    -> {
                        // 商務號
//                    if (ServiceNumberType.BOSS == it.serviceNumberType) {
//                        return@withContext it.name
//                    }
                        return@withContext it.serviceNumberName
                    }

                    // 服務號成員聊天室
                    ChatRoomType.serviceMember -> {
                        val memberIds = AccountRoomRelReference.findMemberIdsByRoomId(null, roomId)
                        withContext(Dispatchers.Main) {
                            binding.tvBusinessName.visibility = View.VISIBLE
                            binding.tvBusinessName.text = MessageFormat.format("{0}", it.serviceNumberName)
                        }
                        return@withContext getString(
                            R.string.text_chat_room_service_member_other,
                            "(" + memberIds.size + ")"
                        )
                    }

                    // 個人
                    ChatRoomType.person -> {
                        val selfName = DBManager.getInstance().queryUser(selfUserId).nickName
                        return@withContext selfName
                    }

                    // 多人
                    ChatRoomType.discuss -> {
                        if (it.isCustomName) {
                            return@withContext TextViewHelper.getHandledTitle(
                                it.name,
                                it.chatRoomMember?.size ?: it.memberIds.size,
                                false
                            )
                        } else {
                            return@withContext TextViewHelper.getDiscussTitle(it.chatRoomMember)
                        }
                    }

                    // 群發
                    ChatRoomType.broadcast -> {
                        return@withContext "群發訊息"
                    }

                    // 服務號的服務人員
                    ChatRoomType.serviceONumberStaff -> {
                        return@withContext it.name
                    }
                    // 夥伴聊天室
                    ChatRoomType.friend -> {
                        return@withContext chatViewModel.getFriendAliasName(it)
                    }

                    else -> {
                        if (selfUserId != it.ownerId) {
                            var title = "未知"
                            if (!Strings.isNullOrEmpty(it.name)) {
                                title = it.name
                            } else {
                                val userProfileEntity = DBManager.getInstance().queryFriend(it.ownerId)
                                userProfileEntity?.let {
                                    if (!Strings.isNullOrEmpty(it.name)) {
                                        title = it.name
                                    }
                                }
                            }
                            return@withContext title
                        }
                    }
                }

                return@withContext it.name
            } ?: run {
                return@withContext userName
            }
        }

    private fun setMessageList() =
        CoroutineScope(Dispatchers.IO).launch {
            initMessageList()
            if (!NetworkUtils.isNetworkAvailable(this@ChatNormalActivity)) {
                CELog.w("當前無網絡連接，請檢查您的網絡")
                chatViewModel.roomEntity?.let {
                    highLightUnReadLine(it.unReadNum > 0)
                }
                chatViewModel.disPlayMessageFromDatabase()
                return@launch
            }
            chatViewModel.loadChatMessageListFromDb()
        }

    private fun initBottomRoomList() =
        CoroutineScope(Dispatchers.Main).launch {
            chatViewModel.roomEntity?.let {
                if (binding.rvBottomRoomList.adapter == null) {
                    binding.rvBottomRoomList.adapter = advisoryRoomAdapter
                }
                if (ChatRoomType.services == it.type && ChatRoomType.provisional != it.roomType) {
                    chatViewModel.getServiceNumberMemberRoom(
                        it.serviceNumberId,
                        themeStyle.serviceMemberIconResId
                    )
                }

                binding.rvBottomRoomList.layoutManager = LinearLayoutManager(this@ChatNormalActivity, LinearLayoutManager.HORIZONTAL, false)
                chatViewModel.getConsultTodoList(it.type, it.id)
            }
        }

    private fun setStartPosition(
        isFirst: Boolean,
        position: Int
    ) {
        searchedMessage?.let {
            searchKeyword = intent.getStringExtra(BundleKey.SEARCH_KEY.key()) ?: ""
            doScrollToSelectMessageItemAndSetKeyword(searchKeyword, it)
        } ?: run {
            if (!chatViewModel.isUserScroll) {
                scrollToCurrentMessage(position)
                hideDifbDown()
            }
        }
        if (isFirst) {
            binding.messageRV.setRecyclerViewShowLastItem()
        }
    }

    private fun setUnfinishedMessage() =
        CoroutineScope(Dispatchers.IO).launch {
            chatViewModel.roomEntity?.let {
                val unfinishedEdited = it.unfinishedEdited
                if (!Strings.isNullOrEmpty(unfinishedEdited)) {
                    withContext(Dispatchers.Main) {
                        binding.chatKeyboardLayout.setUnfinishedEdited(
                            InputLogBean.from(
                                unfinishedEdited
                            )
                        )
                    }
                }
            }
        }

    private fun initKeyboard() =
        CoroutineScope(Dispatchers.Main).launch {
            keyBoardBarListener = KeyBoardBarListener()
            binding.chatKeyboardLayout.apply {
                recorder()
                setOnKeyBoardBarListener(keyBoardBarListener)
                setKeyboardChatRoomEntity()
            }
            binding.chatKeyboardLayout.setOnKeyBoardBarListener(keyBoardBarListener)
            binding.funMedia.setChatNormalKeyBoardBarListener(keyBoardBarListener)
            chatViewModel.roomEntity?.let {
                if (selfUserId == it.ownerId && it.serviceNumberOpenType.contains("C")) {
                    setChatDisable(R.string.text_consultation_finished_can_not_reply_message)
                }
                if (it.type == ChatRoomType.friend) { // 禁用輸入框只用於好友聊天室的判斷
                    val user =
                        DBManager
                            .getInstance()
                            .queryFriend(if (it.memberIds.size > 1) it.memberIds.firstOrNull { id -> id != selfUserId } else it.ownerId)
                    user?.let { u ->
                        if (u.status == User.Status.DISABLE) {
                            binding.chatKeyboardLayout.setKeyboardDisabled(
                                true,
                                getString(R.string.text_forbidden_user_can_not_send_message)
                            )
                            disableInvite()
                        }
                    }
                }
            }
        }

    private fun setKeyboardChatRoomEntity() {
        chatViewModel.roomEntity?.let {
            binding.chatKeyboardLayout.setChatRoomEntity(
                it,
                ChatRoomType.FRIEND_or_GROUP_or_DISCUS_or_SERVICE_MEMBER.contains(
                    it.type
                ),
                ChatRoomType.GROUP_or_DISCUSS_or_SERVICE_MEMBER.contains(
                    it.type
                )
            )
        }
    }

    fun highLightUnReadLine(needProcess: Boolean) {
        if (needProcess) {
            CoroutineScope(Dispatchers.Main).launch {
                delay(300L)
                val unReadIndex =
                    chatViewModel.mainMessageData.indexOf(
                        MessageEntity.Builder().id("以下為未讀訊息").build()
                    )
                if (unReadIndex > 0) {
                    val unReadMessage = chatViewModel.mainMessageData[unReadIndex]
                    doScrollToSelectMessageItemAndSetKeyword("", unReadMessage)
                }
            }
        }
    }

    /**
     * 內部訊息搜索，移動上下鍵到該被搜索到的訊息位置
     */
    private fun doScrollToSelectMessageItemAndSetKeyword(
        keyword: String,
        message: MessageEntity
    ) {
        messageListAdapter?.setKeyword(keyword)
        val index = chatViewModel.mainMessageData.indexOf(message)
        if (index >= 0) {
            val entity = chatViewModel.mainMessageData[index]
            entity.isAnimator = true
            scrollToCurrentMessage(index, entity)
        } else {
            Handler().postDelayed({
                chatViewModel.loadMoreForGlobalSearch(message)
            }, 100)
        }
    }

    /**
     * 滑動訊息到目標位置
     * @param index 需要滑動到的位置
     * @param payload payload
     * */
    private fun scrollToCurrentMessage(
        index: Int,
        payload: MessageEntity? = null
    ) {
        (binding.messageRV.layoutManager as LinearLayoutManager?)!!.scrollToPositionWithOffset(index - 1, 0)
        Handler().postDelayed(
            object : Runnable {
                override fun run() {
                    binding.messageRV.layoutManager?.let {
                        binding.messageRV.post {
                            val firstPosition = (it as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
                            val lastPosition = it.findLastVisibleItemPosition()
                            it.scrollToPositionWithOffset(index - 1, 0)
                            if (index - 1 == firstPosition || (index - 1 in (firstPosition + 1)..lastPosition)) {
                                messageListAdapter?.notifyItemChanged(index, payload ?: false)
                            } else {
                                Handler().postDelayed(this, 500)
                            }
                        }
                    }
                }
            },
            30
        )
    }

    private fun onSearchResultUpClick() {
        searchSelectorIndex++
        if (searchSelectorIndex >= chatRoomMessageSearchAdapter.getData().size - 1) {
            searchSelectorIndex = chatRoomMessageSearchAdapter.getData().size - 1
        }
        onSearchResultSelect()
    }

    private fun onSearchResultDownClick() {
        searchSelectorIndex--
        if (searchSelectorIndex < 0) {
            searchSelectorIndex = 0
        }
        onSearchResultSelect()
    }

    private fun onSearchResultSelect() {
        setSearchSelector()

        val currentMessage =
            if (chatRoomMessageSearchAdapter.currentList[searchSelectorIndex] is MessageEntity) {
                chatRoomMessageSearchAdapter.currentList[searchSelectorIndex] as MessageEntity
            } else {
                null
            }
        currentMessage?.isAnimator = true
        val currentPosition = messageListAdapter?.currentList?.indexOf(currentMessage) ?: 0
        scrollToCurrentMessage(currentPosition, currentMessage)
    }

    private fun setSearchSelector() {
        val maxSize = chatRoomMessageSearchAdapter.getData().size
        resetSearchSelector()
        binding.indicatorTV.text = MessageFormat.format("{0}/{1}", searchSelectorIndex + 1, maxSize)
        when {
            maxSize == 1 -> {
                binding.downIV.apply {
                    isEnabled = false
                    alpha = 0.3f
                }
                binding.upIV.apply {
                    isEnabled = false
                    alpha = 0.3f
                }
            }

            searchSelectorIndex <= 0 -> {
                binding.downIV.apply {
                    isEnabled = false
                    alpha = 0.3f
                }
            }

            searchSelectorIndex >= maxSize - 1 -> {
                binding.upIV.apply {
                    isEnabled = false
                    alpha = 0.3f
                }
            }
        }
    }

    private fun resetSearchSelector() {
        binding.upIV.apply {
            isEnabled = true
            alpha = 1f
        }
        binding.downIV.apply {
            isEnabled = true
            alpha = 1f
        }
    }

    fun initMessageList() =
        CoroutineScope(Dispatchers.Main).launch {
            chatViewModel.roomEntity?.let {
                binding.messageRV.apply {
                    setRecycledViewPool(chatViewModel.messageListRecyclerViewPool)
                    adapter = messageListAdapter
                }

                messageListAdapter?.setOnMessageClickListener(this@ChatNormalActivity)
                messageListAdapter?.registerAdapterDataObserver(
                    object :
                        RecyclerView.AdapterDataObserver() {
                        override fun onItemRangeInserted(
                            positionStart: Int,
                            itemCount: Int
                        ) {
                            super.onItemRangeInserted(positionStart, itemCount)
                            if (itemCount == 1 && positionStart != 0) {
                                val isCanScroll = UiHelper.canScrollBottom(binding.messageRV)
                                if (!isCanScroll) {
                                    binding.messageRV.scrollToPosition(messageListAdapter!!.itemCount - 1)
                                    if (!chatViewModel.isUserScroll) {
                                        hideDifbDown()
                                    }
                                } else {
                                    messageListAdapter?.let {
                                        if (doGettingNewMessage) {
                                            showFloatingLastMessage(it.getLastMessage())
                                        }
                                    }
                                }
                            }
                        }
                    }
                )
                binding.messageRV.addOnScrollListener(
                    object : RecyclerView.OnScrollListener() {
                        override fun onScrolled(
                            recyclerView: RecyclerView,
                            dx: Int,
                            dy: Int
                        ) {
                            super.onScrolled(recyclerView, dx, dy)
                            if (recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                                val lastItemPosition = layoutManager.findLastVisibleItemPosition()
                                if (chatViewModel.mainMessageData.isNotEmpty()) {
                                    if (lastItemPosition == chatViewModel.mainMessageData.size - 1) {
                                        hideDifbDown(55f)
                                        if (binding.floatingLastMessageTV.visibility == View.VISIBLE) {
                                            binding.floatingLastMessageTV.visibility = View.GONE
                                            doGettingNewMessage = false
                                        }
                                    } else {
                                        showDifbDown()
                                    }
                                }
                            }
                        }

                        override fun onScrollStateChanged(
                            recyclerView: RecyclerView,
                            newState: Int
                        ) {
                            super.onScrollStateChanged(recyclerView, newState)
                            when (newState) {
                                RecyclerView.SCROLL_STATE_IDLE -> {
                                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                                    val lastItemPosition = layoutManager.findLastVisibleItemPosition()
                                    if (chatViewModel.mainMessageData.isNotEmpty()) {
                                        val lastMessageIndex = messageListAdapter?.itemCount?.minus(1)
                                        lastMessageIndex?.let {
                                            if (lastItemPosition == it) {
                                                hideDifbDown(55f)
                                                if (binding.floatingLastMessageTV.visibility == View.VISIBLE) {
                                                    binding.floatingLastMessageTV.visibility = View.GONE
                                                    doGettingNewMessage = false
                                                } else {
                                                    // nothing
                                                }
                                            } else {
                                                showDifbDown()
                                            }
                                        }
                                    }
                                }

                                RecyclerView.SCROLL_STATE_DRAGGING -> {
                                    chatViewModel.isUserScroll = true
                                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                                    lastItemPosition = layoutManager.findLastVisibleItemPosition()
                                    val index = layoutManager.findFirstVisibleItemPosition()
                                    if (chatViewModel.mainMessageData.size > index && index > -1) {
                                        val message = chatViewModel.mainMessageData[index]
                                        val dateTime = TimeUtil.getDateShowString(message.sendTime, true)
                                        binding.floatTimeBoxTV.text = dateTime
                                        binding.floatTimeBoxTV.visibility = View.VISIBLE
                                        binding.floatTimeBoxTV.alpha = 1.0f
                                        binding.floatTimeBoxTV.removeCallbacks(timeBoxTarget)
                                        binding.floatTimeBoxTV.postDelayed(timeBoxTarget, 1500L)
                                    }

                                    if (chatViewModel.mainMessageData.isNotEmpty() && lastItemPosition == chatViewModel.mainMessageData.size - 1) {
                                        if (chatViewModel.loadMoreMsgs.isNotEmpty()) {
                                            onLoadMoreMsg(chatViewModel.loadMoreMsgs)
                                            chatViewModel.loadMoreMsgDB()
                                        }
                                        if (binding.floatingLastMessageTV.visibility == View.VISIBLE) {
                                            doFloatingLastMessageClickAction(binding.floatingLastMessageTV)
                                        }
                                    }
                                }

                                RecyclerView.SCROLL_STATE_SETTLING -> {
                                    chatViewModel.isUserScroll = true
                                }
                            }
                        }
                    }
                )
            }
        }

    fun doFloatingLastMessageClickAction(view: View) {
        view.visibility = View.GONE
        doGettingNewMessage = false
        binding.messageRV.adapter?.let {
            binding.messageRV.scrollToPosition(it.itemCount - 1)
        }
    }

    fun onLoadMoreMsg(messages: List<MessageEntity>) {
        binding.xrefreshLayout.completeRefresh()
        if (messages.isNotEmpty()) {
            chatViewModel.mainMessageData.addAll(messages)
        } else {
            chatViewModel.recordMode = false
        }
    }

    /**
     * 底部浮動信息視窗
     */
    private fun showFloatingLastMessage(message: MessageEntity) =
        CoroutineScope(Dispatchers.IO).launch {
            chatViewModel.roomEntity?.let { chatRoom ->
                val senderId = message.senderId
                if (selfUserId == senderId) return@launch
                val senderProfile = DBManager.getInstance().queryUser(senderId)
                senderProfile?.let {
                    val name = it.nickName
                    var content = message.content().simpleContent()
                    if (message.content() is AtContent) {
                        try {
                            val ceMentions = (message.content() as AtContent).mentionContents
                            val builder = AtMatcherHelper.matcherAtUsers("@", ceMentions, chatRoom.membersTable)
                            content = builder.toString()
                        } catch (e: Exception) {
                            content = getString(R.string.text_marked_message)
                        }
                    }
                    if (!Strings.isNullOrEmpty(content)) {
                        withContext(Dispatchers.Main) {
                            binding.floatingLastMessageTV.visibility = View.VISIBLE
                            binding.floatingLastMessageTV.text = MessageFormat.format("{0}: {1}", name, content)
                        }
                    }
                }
            }
        }

    private fun hideDifbDown(dip: Float = 10f) =
        CoroutineScope(Dispatchers.Main).launch {
            val bottomDistance = UiHelper.dip2px(this@ChatNormalActivity, dip)
            binding.difbDown
                .animate()
                .translationY((binding.difbDown.height + bottomDistance).toFloat())
                .setInterpolator(LinearInterpolator())
                .start()
        }

    private fun showDifbDown() =
        CoroutineScope(Dispatchers.Main).launch {
            binding.difbDown
                .animate()
                .translationY(0F)
                .setListener(
                    object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator) {
                            binding.difbDown.visibility = View.VISIBLE
                        }

                        override fun onAnimationEnd(animation: Animator) {
                        }

                        override fun onAnimationCancel(animation: Animator) {
                        }

                        override fun onAnimationRepeat(animation: Animator) {
                        }
                    }
                ).setInterpolator(LinearInterpolator())
                .start()
        }

    private fun rightViewToggle() {
        chatViewModel.roomEntity?.let {
            if (it.roomType == ChatRoomType.undef) chatViewModel.setRoomType()
            binding.rightAction.setImageResource(R.drawable.icon_aipower_close)
            when (it.roomType) {
                ChatRoomType.person -> showSelfPopupWindow()
                ChatRoomType.system -> showSystemPopupWindow()
                ChatRoomType.discuss -> showDiscussPopupWindow()
                ChatRoomType.friend -> showSinglePopupWindow()
                ChatRoomType.group -> showGroupPopupWindow()
                ChatRoomType.bossSecretary, ChatRoomType.bossOwnerWithSecretary -> showBossServiceMemberPopupWindow()
                ChatRoomType.bossOwner -> showServicePopupWindow()
                ChatRoomType.bossServiceNumber -> showServicePopupWindow()
                ChatRoomType.serviceINumberAsker -> showServiceRoomEmployeePopupWindow()
                ChatRoomType.serviceINumberStaff -> showServiceRoomAgentPopupWindow()
                ChatRoomType.serviceMember -> showServiceMemberRoomPopupWindow()
                ChatRoomType.serviceONumberStaff -> showServiceRoomContactPopupWindow()
                ChatRoomType.provisional -> showServiceRoomAgentPopupWindow()
                ChatRoomType.subscribe -> showServiceRoomAgentPopupWindow()

                else -> {} // nothing
            }
        }
    }

    private fun initPopupWindows() =
        CoroutineScope(Dispatchers.Main).launch {
            val popupBinding = GridViewBinding.inflate(layoutInflater)
            popupBinding.root.setBackgroundColor(if (isGreenTheme && themeStyle != RoomThemeStyle.SERVICES) resources.getColor(R.color.color_015F57, null) else themeStyle.mainColor)
            popupBinding.recyclerView.apply {
                layoutManager = GridLayoutManager(this@ChatNormalActivity, 4)
                addItemDecoration(GridItemDecoration())
                itemAnimator = DefaultItemAnimator()
                setHasFixedSize(true)
                measure(0, 0)
            }
            aiffPopupWindows = PopupWindow(popupBinding.root, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            aiffPopupWindows?.apply {
                setBackgroundDrawable(ColorDrawable(0x10101010))
                isOutsideTouchable = true
                isFocusable = true
                setOnDismissListener {
                    binding.rightAction.setImageResource(R.drawable.icon_aipower_open)
                }
            }
        }

    private fun doShowLoginDeviceSettingBottomSheetDialog(deviceRecordItem: DeviceRecordItem) =
        CoroutineScope(Dispatchers.Main).launch {
            BottomSheetDialogBuilder(this@ChatNormalActivity, layoutInflater)
                .getOnlineDeviceOperation(deviceRecordItem, { isCurrentDevice ->
                    if (isCurrentDevice) {
                        LogoutSmsDialogFragment().show(supportFragmentManager, "Logout")
                    } else {
                        AlertView
                            .Builder()
                            .setContext(this@ChatNormalActivity)
                            .setStyle(AlertView.Style.Alert)
                            .setMessage(getString(R.string.text_device_force_logout_tip))
                            .setOthers(
                                arrayOf(getString(R.string.cancel), getString(R.string.text_for_sure))
                            ).setOnItemClickListener { o, position ->
                                if (position == 1) {
                                    deviceRecordItem.deviceId?.let {
                                        homeViewModel.doForceLogoutDevice(it)
                                    }
                                }
                            }.build()
                            .setCancelable(true)
                            .show()
                    }
                }, {
                    deviceRecordItem.rememberMe?.let {
                        if (it) {
                            // 取消自動登入
                            AlertView
                                .Builder()
                                .setContext(this@ChatNormalActivity)
                                .setStyle(AlertView.Style.Alert)
                                .setMessage(getString(R.string.text_device_cancel_auto_login_tip))
                                .setOthers(
                                    arrayOf(getString(R.string.cancel), getString(R.string.text_for_sure))
                                ).setOnItemClickListener { o, position ->
                                    if (position == 1) {
                                        homeViewModel.doCancelAutoLogin(deviceRecordItem.id)
                                    }
                                }.build()
                                .setCancelable(true)
                                .show()
                        } else {
                            // 允許自動登入
                            AlertView
                                .Builder()
                                .setContext(this@ChatNormalActivity)
                                .setStyle(AlertView.Style.Alert)
                                .setMessage(getString(R.string.text_device_allow_auto_login_tip))
                                .setOthers(
                                    arrayOf(getString(R.string.cancel), getString(R.string.text_for_sure))
                                ).setOnItemClickListener { o, position ->
                                    if (position == 1) {
                                        homeViewModel.doAllowAutoLogin(deviceRecordItem.id)
                                    }
                                }.build()
                                .setCancelable(true)
                                .show()
                        }
                    }
                }, {
                    // 刪除設備
                    AlertView
                        .Builder()
                        .setContext(this@ChatNormalActivity)
                        .setStyle(AlertView.Style.Alert)
                        .setMessage(getString(R.string.text_device_delete_login_device_tip))
                        .setOthers(
                            arrayOf(getString(R.string.cancel), getString(R.string.text_for_sure))
                        ).setOnItemClickListener { o, position ->
                            if (position == 1) {
                                deviceRecordItem.uniqueID?.let {
                                    homeViewModel.doDeleteLoginDevice(it, deviceRecordItem.id, deviceRecordItem.deviceId)
                                }
                            }
                        }.build()
                        .setCancelable(true)
                        .show()
                })
                .show()
        }

    private fun showSelfPopupWindow() =
        CoroutineScope(Dispatchers.IO).launch {
            val aiffList = getAiffList(AiffDisplayLocation.SelfRoom.name)
            val popupWindow = getPopupWindow(aiffList, AiffDisplayLocation.SelfRoom.name)
            withContext(Dispatchers.Main) {
                popupWindow.showAsDropDown(binding.titleBar)
            }
        }

    private fun showSystemPopupWindow() =
        CoroutineScope(Dispatchers.IO).launch {
            val aiffList = getAiffList(AiffDisplayLocation.SystemRoom.name)
            val popupWindow = getPopupWindow(aiffList, AiffDisplayLocation.SystemRoom.name)
            withContext(Dispatchers.Main) {
                popupWindow.showAsDropDown(binding.titleBar)
            }
        }

    private fun showDiscussPopupWindow() =
        CoroutineScope(Dispatchers.IO).launch {
            val aiffList = getAiffList(AiffDisplayLocation.DiscussRoom.name)
            val popupWindow = getPopupWindow(aiffList, AiffDisplayLocation.DiscussRoom.name)
            withContext(Dispatchers.Main) {
                popupWindow.showAsDropDown(binding.titleBar)
            }
        }

    private fun showSinglePopupWindow() =
        CoroutineScope(Dispatchers.IO).launch {
            val aiffList = getAiffList(AiffDisplayLocation.PrivateRoom.name)
            val popupWindow = getPopupWindow(aiffList, AiffDisplayLocation.PrivateRoom.name)
            withContext(Dispatchers.Main) {
                popupWindow.showAsDropDown(binding.titleBar)
            }
        }

    private fun showServiceRoomContactPopupWindow() =
        CoroutineScope(Dispatchers.IO).launch {
            val aiffList = getAiffList(AiffDisplayLocation.ServiceRoomContact.name)
            val popupWindow = getPopupWindow(aiffList, AiffDisplayLocation.ServiceRoomContact.name)
            withContext(Dispatchers.Main) {
                popupWindow.showAsDropDown(binding.titleBar)
            }
        }

    private fun showServiceMemberRoomPopupWindow() =
        CoroutineScope(Dispatchers.IO).launch {
            val aiffList = getAiffList(AiffDisplayLocation.ServiceMemberRoom.name)
            val popupWindow =
                getPopupWindow(
                    aiffList,
                    AiffDisplayLocation.ServiceMemberRoom.name
                )
            withContext(Dispatchers.Main) {
                popupWindow.showAsDropDown(binding.titleBar)
            }
        }

    private fun showServiceRoomEmployeePopupWindow() =
        CoroutineScope(Dispatchers.IO).launch {
            val aiffList = getAiffList(AiffDisplayLocation.ServiceRoomEmployee.name)
            val popupWindow = getPopupWindow(aiffList, AiffDisplayLocation.ServiceRoomEmployee.name, true)
            withContext(Dispatchers.Main) {
                popupWindow.showAsDropDown(binding.titleBar)
            }
        }

    private fun showServiceRoomAgentPopupWindow() =
        CoroutineScope(Dispatchers.IO).launch {
            val aiffList = getAiffList(AiffDisplayLocation.ServiceRoomAgent.name)
            val popupWindow =
                getPopupWindow(
                    aiffList,
                    AiffDisplayLocation.ServiceRoomAgent.name
                )
            withContext(Dispatchers.Main) {
                popupWindow.showAsDropDown(binding.titleBar)
            }
        }

    private fun showBossServiceMemberPopupWindow() =
        CoroutineScope(Dispatchers.IO).launch {
            val aiffList = getAiffList(AiffDisplayLocation.BossServiceMemberRoom.name)
            val popupWindow = getPopupWindow(aiffList, AiffDisplayLocation.BossServiceMemberRoom.name)
            withContext(Dispatchers.Main) {
                popupWindow.showAsDropDown(binding.titleBar)
            }
        }

    /**
     * 群組頂部進階選單
     * group_chat_menu_out
     */
    private fun showGroupPopupWindow() =
        CoroutineScope(Dispatchers.IO).launch {
            chatViewModel.roomEntity?.let {
                val aiffList: MutableList<RichMenuInfo> = Lists.newArrayList()
                var displayLocation: String?
                if (it.ownerId == selfUserId) { // group owner
                    aiffList.addAll(getAiffList(AiffDisplayLocation.GroupRoomOwner.name, AiffDisplayLocation.GroupRoom.name))
                    displayLocation =
                        AiffDisplayLocation.GroupRoomOwner.name + "," + AiffDisplayLocation.GroupRoom.name
                } else { // not group owner
                    aiffList.addAll(getAiffList(AiffDisplayLocation.GroupRoom.name))
                    displayLocation = AiffDisplayLocation.GroupRoom.name
                }

                val popupWindow = getPopupWindow(aiffList, displayLocation)
                withContext(Dispatchers.Main) {
                    popupWindow.showAsDropDown(binding.titleBar)
                }
            }
        }

    // 服務號聊天室進階功能
    private fun showServicePopupWindow() =
        CoroutineScope(Dispatchers.IO).launch {
            val aiffList = getAiffList(AiffDisplayLocation.BossServiceRoom.name)
            val popupWindow =
                getPopupWindow(
                    aiffList,
                    AiffDisplayLocation.BossServiceRoom.name + "," + AiffDisplayLocation.ServiceRoom.name
                )
            withContext(Dispatchers.Main) {
                popupWindow.showAsDropDown(binding.titleBar)
            }
        }

    // 服務號成員聊天室，成員列表
    private fun showSinkingServiceMemberListPopupWindow() =
        CoroutineScope(Dispatchers.Main).launch {
            chatViewModel.roomEntity?.let {
                val gridMemberViewBinding = GridMemberViewBinding.inflate(layoutInflater)
                gridMemberViewBinding.apply {
                    root.setBackgroundColor(if (isGreenTheme && themeStyle != RoomThemeStyle.SERVICES) resources.getColor(R.color.color_015F57, null) else themeStyle.mainColor)
                    recyclerView.apply {
                        layoutManager =
                            LinearLayoutManager(
                                this@ChatNormalActivity,
                                LinearLayoutManager.HORIZONTAL,
                                false
                            )
                        itemAnimator = DefaultItemAnimator()
                        setHasFixedSize(true)
                        val memberList = it.agentsList.toMutableList()
                        memberList.add(0, UserProfileEntity())
                        chatRoomMembersAdapter =
                            ChatRoomMembersAdapter(false)
                                .setData(memberList)
                                .setOnItemClickListener(this@ChatNormalActivity)
                        adapter = chatRoomMembersAdapter
                    }
                    chatRoomMembersAdapter?.refreshData()
                    memberPopupWindow = PopupWindow(root, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    memberPopupWindow?.apply {
                        setBackgroundDrawable(ColorDrawable(0x10101010))
                        isOutsideTouchable = true
                        isFocusable = true
                        setOnDismissListener {
                            binding.rightAction.setImageResource(R.drawable.icon_aipower_open)
                        }
                        showAsDropDown(binding.titleBar)
                    }
                }
            }
        }

    /**
     * 下沈式成員列表
     */

    private fun showSinkingMemberListPopupWindow() =
        CoroutineScope(Dispatchers.Main).launch {
            chatViewModel.roomEntity?.let {
                val gridMemberViewBinding = GridMemberViewBinding.inflate(layoutInflater)
                gridMemberViewBinding.apply {
//                root.setBackgroundColor(themeStyle.mainColor)
                    recyclerView.apply {
                        layoutManager =
                            LinearLayoutManager(
                                this@ChatNormalActivity,
                                LinearLayoutManager.HORIZONTAL,
                                false
                            )
                        itemAnimator = DefaultItemAnimator()
                        setHasFixedSize(true)
                        val memberList = getSinkingGroupMemberList()

                        chatRoomMembersAdapter =
                            ChatRoomMembersAdapter(false)
                                .setData(memberList)
                                .setOnItemClickListener(this@ChatNormalActivity)
                        adapter = chatRoomMembersAdapter
                        chatRoomMembersAdapter?.refreshData()
                    }
                }
                memberPopupWindow = PopupWindow(gridMemberViewBinding.root, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                memberPopupWindow?.apply {
                    setBackgroundDrawable(ColorDrawable(0x10101010))
                    isOutsideTouchable = false
                    isFocusable = true
                    showAsDropDown(binding.titleBar)
                    setOnDismissListener {
                        binding.rightAction.setImageResource(R.drawable.icon_aipower_open)
                        ViewCompat
                            .animate(binding.rightAction)
                            .rotation(0f)
                            .setDuration(1)
                            .start()
                    }
                }
            }
        }

    private suspend fun getSinkingGroupMemberList(): MutableList<UserProfileEntity> =
        withContext(Dispatchers.IO) {
            chatViewModel.roomEntity?.let {
                it.chatRoomMember?.let { chatMember ->
                    var memberList = mutableListOf<UserProfileEntity>()
                    chatMember.forEach { member ->
                        val userProfileEntity = DBManager.getInstance().queryUser(member.memberId)
                        userProfileEntity?.let { userProfile ->
                            if (it.ownerId == member.memberId && it.roomType == ChatRoomType.group) {
                                userProfile.groupPrivilege = GroupPrivilegeEnum.Owner
                            } else {
                                userProfile.groupPrivilege = member.privilege
                            }
                            memberList.add(userProfile)
                        }
                    }

                    // 如果是社團，按照擁有者 -> 管理員 -> 一般成員排序
                    if (it.roomType == ChatRoomType.group) {
                        memberList = SortUtil.sortGroupOwnerManagerByPrivilege(memberList)
                    }
                    // 主頁
                    memberList.add(0, UserProfileEntity())
                    return@withContext memberList
                } ?: run { return@withContext mutableListOf<UserProfileEntity>() }
            } ?: run { return@withContext mutableListOf<UserProfileEntity>() }
        }

    private suspend fun getPopupWindow(
        aiffList: List<RichMenuInfo>,
        displayLocation: String
    ): PopupWindow =
        getPopupWindow(
            aiffList,
            displayLocation,
            chatViewModel.roomEntity!!.provisionalIds.contains(selfUserId) && chatViewModel.roomEntity!!.listClassify == ChatRoomSource.MAIN
        )

    /**
     * 右上下拉選單
     * @param isEmployee 判斷內部服務號 是否是需要諮詢的員工 或是 服務人員
     */
    private suspend fun getPopupWindow(
        aiffList: List<RichMenuInfo>,
        displayLocation: String,
        isEmployee: Boolean
    ): PopupWindow =
        withContext(Dispatchers.Main) {
            val popupWindowBinding = GridViewBinding.inflate(layoutInflater)
            chatViewModel.roomEntity?.let {
                popupWindowBinding.root.setBackgroundColor(if (isGreenTheme && themeStyle != RoomThemeStyle.SERVICES) resources.getColor(R.color.color_015F57, null) else themeStyle.mainColor)
            } ?: run {
                popupWindowBinding.root.setBackgroundColor(themeStyle.mainColor)
            }
            val popupWindow =
                PopupWindow(
                    popupWindowBinding.root,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            if (aiffList.isEmpty()) {
                popupWindowBinding.tvMore.visibility = View.GONE
            } else {
                popupWindowBinding.tvMore.visibility = View.VISIBLE
            }

            // rich menu
            val richMenuInfoList: MutableList<RichMenuInfo> = getRichMenuList(isEmployee)
            richMenuInfoList.addAll(aiffList)
            val richMenuAdapter =
                RichMenuAdapter(
                    if (richMenuInfoList.size >= 16) {
                        richMenuInfoList.subList(
                            0,
                            16
                        )
                    } else {
                        richMenuInfoList
                    }
                )

            richMenuAdapter.setOnItemClickListener { adapter: BaseQuickAdapter<*, *>, view: View?, position: Int ->
                popupWindow.dismiss()
                val info = adapter.getItem(position) as RichMenuInfo
                onRichMenuItemClick(info)
            }

            popupWindowBinding.tvMore.setOnClickListener {
                popupWindow.dismiss()
                aiffManager?.showAiffList(aiffList)
            }

            // recyclerView
            val gridLayoutManager = GridLayoutManager(this@ChatNormalActivity, 4)
            popupWindowBinding.recyclerView.apply {
                layoutManager = gridLayoutManager
                addItemDecoration(GridItemDecoration())
                itemAnimator = DefaultItemAnimator()
                setHasFixedSize(true)
                adapter = richMenuAdapter
                measure(0, 0)
            }

            // popupWindows setting
            popupWindow.apply {
                setBackgroundDrawable(ColorDrawable(0x10101010))
                isOutsideTouchable = true
                isFocusable = true
                setOnDismissListener {
                    binding.rightAction.setImageResource(
                        R.drawable.icon_aipower_open
                    )
                }
            }

            return@withContext popupWindow
        }

    private suspend fun getAiffList(vararg displayLocation: String): List<RichMenuInfo> =
        withContext(Dispatchers.IO) {
            val aiffInfoList = AiffDB.getInstance(this@ChatNormalActivity).aiffInfoDao.aiffInfoListByIndex
            val aiffSet = LinkedHashSet<RichMenuInfo>()
            aiffInfoList.forEach {
                if (it.embedLocation != AiffEmbedLocation.ChatRoomMenu.name) return@forEach
                displayLocation.forEach { location ->
                    it.displayLocation?.let { displayLocation ->
                        if (displayLocation.contains(location)) {
                            val info =
                                RichMenuInfo(
                                    RichMenuInfo.MenuType.AIFF.type,
                                    it.id,
                                    it.pictureId,
                                    it.title,
                                    it.name,
                                    it.pinTimestamp,
                                    it.useTimestamp
                                )
                            aiffSet.add(info)
                        }
                    } ?: run {
                        // 支援指定服務號處理
                        if (it.incomingAiff.equals("serviceNumber")) {
                            it.serviceNumberIds?.let { ids ->
                                chatViewModel.roomEntity?.let { entity ->
                                    if (ids.contains(entity.serviceNumberId)) {
                                        when (it.userType_APP) {
                                            User.Type.EMPLOYEE -> { // Aiff對象∶服務方使用
                                                if (entity.ownerId != selfUserId) {
                                                    val info =
                                                        RichMenuInfo(
                                                            RichMenuInfo.MenuType.AIFF.type,
                                                            it.id,
                                                            it.pictureId,
                                                            it.title,
                                                            it.name,
                                                            it.pinTimestamp,
                                                            it.useTimestamp
                                                        )
                                                    aiffSet.add(info)
                                                }
                                            }

                                            User.Type.CONTACT -> { // Aiff對象∶客戶方使用
                                                if (entity.ownerId == selfUserId) {
                                                    val info =
                                                        RichMenuInfo(
                                                            RichMenuInfo.MenuType.AIFF.type,
                                                            it.id,
                                                            it.pictureId,
                                                            it.title,
                                                            it.name,
                                                            it.pinTimestamp,
                                                            it.useTimestamp
                                                        )
                                                    aiffSet.add(info)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            val sortList = aiffSet.toMutableList()
            sortList.sort()
            return@withContext sortList
        }

    private suspend fun getRichMenuList(isEmployee: Boolean): MutableList<RichMenuInfo> =
        withContext(Dispatchers.IO) {
            val richMenuInfoList = Lists.newArrayList<RichMenuInfo>()
            chatViewModel.roomEntity?.let {
                var richMenuInfo1: RichMenuInfo? = null
                var richMenuInfo2: RichMenuInfo? = null
                var richMenuInfo3: RichMenuInfo? = null
                richMenuInfoList.addAll(
                    RichMenuInfo.getServiceNumberChatRoomTopRichMenus(
                        it.isMute(),
                        it.type == ChatRoomType.services,
                        isEmployee || it.type != ChatRoomType.services
                    )
                )
                when (it.roomType) {
                    ChatRoomType.group -> {
                        // 是群主
                        if (it.ownerId == selfUserId) {
                            richMenuInfo1 =
                                RichMenuInfo(
                                    RichMenuInfo.MenuType.FIXED.type,
                                    RichMenuInfo.FixedMenuId.DISMISS_CROWD,
                                    R.drawable.ic_quit,
                                    R.string.base_top_rich_menu_dismiss_crowd,
                                    true
                                )
                        } else {
                            richMenuInfo1 =
                                RichMenuInfo(
                                    RichMenuInfo.MenuType.FIXED.type,
                                    RichMenuInfo.FixedMenuId.EXIT_CROWD,
                                    R.drawable.ic_quit,
                                    R.string.base_top_rich_menu_exit_crowd,
                                    true
                                )
                        }
                        richMenuInfo2 =
                            RichMenuInfo(
                                RichMenuInfo.MenuType.FIXED.type,
                                RichMenuInfo.FixedMenuId.MAIN_PAGE,
                                R.drawable.ic_home_page,
                                R.string.text_home_page,
                                true
                            )
                        richMenuInfoList.add(richMenuInfo2)
                        richMenuInfoList.add(richMenuInfo1)
                    }

                    ChatRoomType.discuss -> {
                        richMenuInfo1 =
                            RichMenuInfo(
                                RichMenuInfo.MenuType.FIXED.type,
                                RichMenuInfo.FixedMenuId.UPGRADE,
                                R.drawable.new_group,
                                R.string.text_transfer_to_crowd,
                                true
                            )
                        richMenuInfo2 =
                            RichMenuInfo(
                                RichMenuInfo.MenuType.FIXED.type,
                                RichMenuInfo.FixedMenuId.EXIT_DISCUSS,
                                R.drawable.ic_quit,
                                R.string.base_top_rich_menu_exit_discuss,
                                true
                            )
                        richMenuInfo3 =
                            RichMenuInfo(
                                RichMenuInfo.MenuType.FIXED.type,
                                RichMenuInfo.FixedMenuId.MAIN_PAGE,
                                R.drawable.ic_home_page,
                                R.string.text_home_page,
                                true
                            )
                        richMenuInfoList.add(richMenuInfo1)
                        richMenuInfoList.add(richMenuInfo3)
                        richMenuInfoList.add(richMenuInfo2)
                    }

                    else -> {} // nothing
                }
            }
            return@withContext richMenuInfoList
        }

    private fun onRichMenuItemClick(info: RichMenuInfo) =
        CoroutineScope(Dispatchers.IO).launch {
            if (info.type == RichMenuInfo.MenuType.FIXED.type) {
                when (info.menuId) {
                    RichMenuInfo.FixedMenuId.SEARCH -> doSearchAction()
                    RichMenuInfo.FixedMenuId.AMPLIFICATION, RichMenuInfo.FixedMenuId.MUTE -> {
                        chatViewModel.roomEntity?.let {
                            chatViewModel.changeMute(it.isMute, it.id)
                        }
                    }

                    // 升級成為社團
                    RichMenuInfo.FixedMenuId.UPGRADE -> {
                        upgradeToGroup()
                    }

                    // 退出聊天室
                    RichMenuInfo.FixedMenuId.DISMISS_CROWD,
                    RichMenuInfo.FixedMenuId.EXIT_CROWD,
                    RichMenuInfo.FixedMenuId.EXIT_DISCUSS
                    -> {
                        showExitChatRoom()
                    }

                    RichMenuInfo.FixedMenuId.NEW_MEMBER -> {
                        addProvisionalMember()
                    }

                    RichMenuInfo.FixedMenuId.MAIN_PAGE -> {
                        toMainPage()
                    }

                    // 記事
                    RichMenuInfo.FixedMenuId.TODO -> {
                        chatViewModel.roomEntity?.let {
                            IntentUtil.startIntent(
                                this@ChatNormalActivity,
                                ChatRoomFilterActivity::class.java,
                                bundleOf(
                                    BundleKey.INTENT_TO_TODO.key() to true,
                                    BundleKey.ROOM_ID.key() to it.id
                                )
                            )
                        }
                    }

                    // 篩選
                    RichMenuInfo.FixedMenuId.FILTER -> {
                        chatViewModel.roomEntity?.let {
                            val bundle =
                                bundleOf(
                                    BundleKey.ROOM_ID.key() to it.id
                                )
                            IntentUtil.startIntent(this@ChatNormalActivity, ChatRoomFilterActivity::class.java, bundle)
                        }
                    }

                    else -> {} // nothing
                }
            } else if (info.type == RichMenuInfo.MenuType.AIFF.type) {
                val aiffInfo = AiffDB.getInstance(this@ChatNormalActivity).aiffInfoDao.getAiffInfo(info.id)
                aiffManager?.showAiffViewByInfo(aiffInfo)
                aiffInfo.useTimestamp = System.currentTimeMillis()
                AiffDB.getInstance(this@ChatNormalActivity).aiffInfoDao.upsert(aiffInfo)
            }
        }

    private fun upgradeToGroup() =
        CoroutineScope(Dispatchers.IO).launch {
            chatViewModel.roomEntity?.let {
                val bundle =
                    bundleOf(
                        BundleKey.ROOM_ID.key() to it.id,
                        BundleKey.MEMBERS_LIST.key() to it.chatRoomMember.map { it.memberId }
                    )
                if (it.isCustomName) {
                    bundle.putString(BundleKey.CHAT_ROOM_NAME.key(), it.name)
                }
                IntentUtil.startIntent(this@ChatNormalActivity, CreateGroupActivity::class.java, bundle)
            }
        }

    private fun showExitChatRoom() =
        CoroutineScope(Dispatchers.IO).launch {
            chatViewModel.roomEntity?.let {
                val message =
                    if (it.ownerId == selfUserId && it.roomType == ChatRoomType.group) {
                        "您是否要解散社團？"
                    } else if (it.roomType == ChatRoomType.group) {
                        "是否退出社團？"
                    } else {
                        "退出後將清除聊天室及聊天室紀錄，您確定要退出嗎？"
                    }
                val title = if (it.roomType != ChatRoomType.group) "退出聊天室" else ""
                withContext(Dispatchers.Main) {
                    AlertView
                        .Builder()
                        .setContext(this@ChatNormalActivity)
                        .setStyle(AlertView.Style.Alert)
                        .setTitle(title)
                        .setMessage(message)
                        .setOthers(
                            arrayOf("取消", "確定")
                        ).setOnItemClickListener { o, position ->
                            if (position == 1) {
                                if (it.ownerId == selfUserId && it.roomType == ChatRoomType.group) {
                                    dismissGroup()
                                } else {
                                    quitGroup()
                                }
                            }
                        }.build()
                        .setCancelable(true)
                        .show()
                }
            }
        }

    private fun showProvisionalToServiceNumber(chatRoomEntity: ChatRoomEntity) =
        CoroutineScope(Dispatchers.Main).launch {
            AlertView
                .Builder()
                .setContext(this@ChatNormalActivity)
                .setStyle(AlertView.Style.Alert)
                .setTitle("您已成為服務號成員")
                .setMessage("此服務號管理者已將您加入到服務號成員的行列之中")
                .setOthers(
                    arrayOf(
                        getString(R.string.text_for_sure)
                    )
                ).setOnItemClickListener { o, position ->
                    val bundle =
                        bundleOf(
                            BundleKey.EXTRA_SESSION_ID.key() to chatRoomEntity.id
                        )
                    IntentUtil.startIntent(this@ChatNormalActivity, ChatActivity::class.java, bundle)
                    finish()
                }.build()
                .setCancelable(true)
                .show()
        }

    private fun dismissGroup() =
        CoroutineScope(Dispatchers.IO).launch {
            chatViewModel.roomEntity?.let {
                if (selfUserId == it.ownerId) {
                    chatViewModel.roomDismiss(it.id)
                }
            }
        }

    private fun quitGroup() =
        CoroutineScope(Dispatchers.IO).launch {
            chatViewModel.roomEntity?.let {
                chatViewModel.chatMemberExit(it.id, it.ownerId)
            }
        }

    private fun addProvisionalMember() =
        CoroutineScope(Dispatchers.IO).launch {
            chatViewModel.roomEntity?.let {
                val bundle =
                    bundleOf(
                        BundleKey.ROOM_ID.key() to it.id,
                        BundleKey.SERVICE_NUMBER_ID.key() to it.serviceNumberId,
                        BundleKey.SUBSCRIBE_AGENT_ID.key() to it.serviceNumberAgentId,
                        BundleKey.ROOM_TYPE.key() to InvitationType.ProvisionalMember.name,
                        BundleKey.PROVISIONAL_MEMBER_IDS.key() to it.provisionalIds
                    )
                IntentUtil.launchIntent(this@ChatNormalActivity, MemberInvitationActivity::class.java, addProvisionalMemberARL, bundle)
            }
        }

    private fun toMainPage() =
        CoroutineScope(Dispatchers.IO).launch {
            chatViewModel.roomEntity?.let {
                val bundle =
                    bundleOf(
                        BundleKey.ROOM_ID.key() to it.id,
                        BundleKey.ROOM_TYPE.key() to it.type.name
                    )
                if (NetworkUtils.isNetworkAvailable(this@ChatNormalActivity)) {
                    IntentUtil.startIntent(this@ChatNormalActivity, MainPageActivity::class.java, bundle)
                }
            }
        }

    private fun toServiceNumberHomePage() =
        CoroutineScope(Dispatchers.IO).launch {
            chatViewModel.roomEntity?.let {
                val bundle =
                    bundleOf(
                        BundleKey.SERVICE_NUMBER_ID.key() to it.serviceNumberId
                    )
                IntentUtil.startIntent(this@ChatNormalActivity, ServiceNumberManageActivity::class.java, bundle)
            }
        }

    private fun doSearchAction() =
        CoroutineScope(Dispatchers.Main).launch {
            enterSearchMode()
        }

    private fun enterSearchMode() {
        isSearchMode = true
        binding.searchBar.visibility = View.VISIBLE
        binding.searchRV.visibility = View.VISIBLE
        binding.searchMask.visibility = View.VISIBLE
        binding.searchBottomBar.visibility = View.GONE
        binding.scopeSectioned.visibility = View.GONE
        binding.titleBar.visibility = View.INVISIBLE
    }

    private fun exitSearchMode() {
        isSearchMode = false
        binding.searchBar.visibility = View.GONE
        binding.searchRV.visibility = View.GONE
        binding.searchMask.visibility = View.GONE
        binding.scopeSectioned.visibility = View.GONE
        binding.searchBottomBar.visibility = View.GONE
        binding.titleBar.visibility = View.VISIBLE
        binding.searchRV.adapter = null
        binding.etSearch.setText("")
        chatRoomMessageSearchAdapter.submitList(Lists.newArrayList())
        messageListAdapter?.apply {
            clearSearchMode()
        }
    }

    private fun setUnreadCount(
        count: Int,
        roomId: String = ""
    ) = CoroutineScope(Dispatchers.IO).launch {
        chatViewModel.roomEntity?.let {
            if (it.id == roomId || roomId.isEmpty()) {
                return@launch
            }
            setUnreadCount(count)
        }
    }

    private fun setUnreadCount(count: Int) =
        CoroutineScope(Dispatchers.Main).launch {
            if (count > 0) {
                binding.unreadNum.text = getUnreadText(count)
                binding.unreadNum.visibility = View.VISIBLE
            } else {
                binding.unreadNum.visibility = View.GONE
            }
        }

    private fun addMember() =
        CoroutineScope(Dispatchers.IO).launch {
            chatViewModel.roomEntity?.let {
                var bundle: Bundle? = null
                it.chatRoomMember?.let { chatMember ->
                    val chatRoomMemberIds = chatMember.map { member -> member.memberId }
                    bundle =
                        bundleOf(
                            BundleKey.ROOM_ID.key() to it.id,
                            BundleKey.ACCOUNT_IDS.key() to chatRoomMemberIds,
                            BundleKey.ROOM_TYPE.key() to InvitationType.GroupRoom.name
                        )
                } ?: run {
                    bundle =
                        bundleOf(
                            BundleKey.ROOM_ID.key() to it.id,
                            BundleKey.ACCOUNT_IDS.key() to it.memberIds,
                            BundleKey.ROOM_TYPE.key() to InvitationType.GroupRoom.name
                        )
                }
                IntentUtil.launchIntent(this@ChatNormalActivity, MemberInvitationActivity::class.java, addMemberARL, bundle)
            }
        }

    private fun onInviteSuccess(userList: List<UserProfileEntity>) =
        CoroutineScope(Dispatchers.IO).launch {
            chatViewModel.roomEntity?.let {
                if (it.roomType != ChatRoomType.group) {
                    it.members.addAll(userList)
                    it.memberIds.addAll(userList.map { it.id })
                }
                chatViewModel.doHandleMemberFromDB(userList.map { it.id }, it.id)
                setTitleBar()
//            chatViewModel.updateSession(it.id)
                chatRoomMembersAdapter?.refreshData()
                CoroutineScope(Dispatchers.Main).launch {
                    ToastUtils.showToast(this@ChatNormalActivity, getString(R.string.text_invite_member_success))
                }
            }
        }

    // 判斷是否有 aiff主頁
    // todo CoroutineScope
    fun checkClientMainPageFromAiff(): Boolean {
        val aiffDao = AiffDB.getInstance(this@ChatNormalActivity).aiffInfoDao
        val aiffInfoList = aiffDao.aiffInfoListByIndex
        if (aiffInfoList != null && aiffInfoList.size > 0) {
            aiffInfoList.forEach {
                if (it.embedLocation == AiffEmbedLocation.ContactHome.name) {
                    val aiffInfo = aiffDao.getAiffInfo(it.id)
                    aiffManager?.showAiffViewByInfo(aiffInfo)
                    return true
                }
            }
        }
        return false
    }

    fun toChatRoomByUserProfile(userProfileEntity: UserProfileEntity?) =
        CoroutineScope(Dispatchers.IO).launch {
            userProfileEntity?.let { userProfileEntity ->
                if (userProfileEntity.id != selfUserId) {
                    userProfileEntity.roomId?.let {
                        if (userProfileEntity.roomId.isNotEmpty()) {
                            val bundle =
                                bundleOf(
                                    BundleKey.EXTRA_SESSION_ID.key() to it
                                )
                            IntentUtil.startIntent(this@ChatNormalActivity, ChatNormalActivity::class.java, bundle)
                        } else {
                            doCreateNewChatRoom(userProfileEntity)
                        }
                    } ?: run {
                        doCreateNewChatRoom(userProfileEntity)
                    }
                } else {
                    val bundle =
                        bundleOf(
                            BundleKey.ACCOUNT_TYPE.key() to UserType.EMPLOYEE.name,
                            BundleKey.ACCOUNT_ID.key() to selfUserId,
                            BundleKey.WHERE_COME.key() to javaClass.simpleName
                        )
                    IntentUtil.startIntent(this@ChatNormalActivity, SelfInformationHomepageActivity::class.java, bundle)
                }
            }
            withContext(Dispatchers.Main) {
                memberPopupWindow?.dismiss()
            }
        }

    private fun doCreateNewChatRoom(userProfileEntity: UserProfileEntity) {
        val bundle =
            bundleOf(
                BundleKey.USER_NICKNAME.key() to userProfileEntity.nickName,
                BundleKey.USER_ID.key() to userProfileEntity.id
            )
        IntentUtil.startIntent(this@ChatNormalActivity, ChatNormalActivity::class.java, bundle)
    }

    override fun onKeyDown(
        keyCode: Int,
        event: KeyEvent?
    ): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isFloatViewOpenAndExecuteClose) return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onStop() {
        super.onStop()
        chatViewModel.roomEntity?.let {
            val bean = binding.chatKeyboardLayout.inputHET.unfinishedEditBean
            bean.isTheme = chatViewModel.isThemeOpen && !Strings.isNullOrEmpty(this.themeId)
            bean.id = themeId

            val unfinishedEdited =
                ChatRoomReference.getInstance().getUnfinishedEdited(
                    it.id
                )
            val localBean = InputLogBean.from(unfinishedEdited)
            // 草稿訊息
            if (localBean.text != bean.text) {
                val roomId = it.id
                val content =
                    if (Strings.isNullOrEmpty(bean.text.trim { it <= ' ' })) "" else bean.text
                bean.text = content
                ChatRoomReference
                    .getInstance()
                    .updateUnfinishedEditedAndTimeById(roomId, bean.toJson())
                it.unfinishedEdited = content
                EventBusUtils.sendEvent(EventMsg<Any>(MsgConstant.REFRESH_ROOM_BY_LOCAL))
            }
            // 發送失敗訊息
            if (it.failedMessage != null) EventBusUtils.sendEvent(EventMsg<Any>(MsgConstant.REFRESH_ROOM_BY_LOCAL))
        }
    }

    override fun onDestroy() {
        chatViewModel.roomEntity?.let {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            UserPref.getInstance(this).removeCurrentRoomId(it.id)
            addMemberARL?.unregister()
            addProvisionalMemberARL?.unregister()
            updateGroupARL?.unregister()
            toGroupSessionARL?.unregister()
            messageListAdapter?.onDestroy()
            binding.messageRV.adapter = null
            it.lastMessage?.let { lastMsg ->
                it.updateTime = lastMsg.sendTime
            }
            if (isNeedRefreshList) {
                // 當設成已讀後，在離開聊天室後刷新列表
                EventBusUtils.sendEvent(EventMsg<Any>(MsgConstant.NOTICE_REFRESH_CHAT_ROOM_LIST))
            } else {
                EventBusUtils.sendEvent(EventMsg<Any>(MsgConstant.NOTICE_REFRESH_CHAT_ROOM_LIST))
                if (it.listClassify == ChatRoomSource.MAIN) {
                    // 重新整理一般聊天列表下方tab未讀數
                    EventBusUtils.sendEvent(EventMsg<Any>(MsgConstant.UPDATE_MAIN_BADGE_NUMBER_EVENT))
                }
            }
        }
        ThemeHelper.updateServiceChatRoomTheme(isServiceRoom = false)
        super.onDestroy()
    }

    private fun clearAtMessage() {
//        chatViewModel.roomEntity?.let {
//            chatViewModel.clearIsAtMeFlag(it.id)
//        }
    }

    fun updateAccountForMessage(mAccount: UserProfileEntity) {
        for (i in chatViewModel.mainMessageData.indices) {
            val msg = chatViewModel.mainMessageData[i]
            if (mAccount.id == msg.senderId) {
                chatViewModel.mainMessageData[i].senderName =
                    if (Strings.isNullOrEmpty(mAccount.alias)) mAccount.nickName else mAccount.alias
                messageListAdapter?.notifyItemChanged(i)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public fun handleEvent(event: EventMsg<Any>) {
        binding.chatKeyboardLayout.handleEvent(event)
        when (event.code) {
            MsgConstant.NOTICE_SELF_EXIT_ROOM -> {
                val roomId = event.string
                chatViewModel.roomEntity?.let {
                    if (it.id == roomId) {
                        finish()
                    }
                }
            }

            MsgConstant.NOTICE_CLOSE_OLD_ROOM -> finish()

            MsgConstant.UPDATE_MAIN_BADGE_NUMBER_EVENT,
            MsgConstant.UPDATE_SERVICE_BADGE_NUMBER_EVENT
            -> {
                if (event.data is BadgeDataModel) {
                    val badgeDataModel = event.data as BadgeDataModel
                    setUnreadCount(badgeDataModel.unReadNumber, badgeDataModel.roomId)
                    advisoryRoomAdapter.shouldNotifyUnreadIcon(badgeDataModel.roomId)
                }
            }

            MsgConstant.GROUP_UPGRADE_FILTER -> {
                // 修改群聊名称
                val groupUpgradeBean = event.data as GroupUpgradeBean
                val title = groupUpgradeBean.title
                val sessionId = groupUpgradeBean.sessionId
                chatViewModel.roomEntity?.let {
                    if (sessionId.isNullOrEmpty()) return
                    if (title.isNullOrEmpty()) return
                    if (it.id == sessionId) {
                        chatViewModel.upgradeToGroup(it.id)
                    }
                }
            }
            // 多人聊天室被改名
            MsgConstant.NOTICE_DISCUSS_ROOM_TITLE_UPDATE -> {
                val roomId = event.data.toString()
                if (roomId == chatViewModel.roomEntity?.id) {
                    chatViewModel.getChatRoomEntity(roomId)
                }
            }

            MsgConstant.CHAT_TITLE_FILTER -> {
                val title = event.data.toString()
                binding.title.text = title
            }

            MsgConstant.ACCOUNT_REFRESH_FILTER -> {
                val userProfile = event.data as UserProfileEntity
                chatViewModel.roomEntity?.let {
                    val memberIds = AccountRoomRelReference.findMemberIdsByRoomId(null, it.id)
                    if (memberIds.contains(userProfile.id)) {
                        setTitleBar()
                        updateAccountForMessage(userProfile)
                    }
                }
            }

            MsgConstant.NOTICE_FINISH_ACTIVITY -> {
                finish()
                overridePendingTransition(0, 0)
            }

            MsgConstant.NOTICE_KEEP_SCREEN_ON -> {
                runOnUiThread {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }
            // 臨時成員轉成服務人員
            MsgConstant.NOTICE_SERVICE_NUMBER_ADD_FROM_PROVISIONAL -> {
                val serviceNumberAddMode =
                    JsonHelper
                        .getInstance()
                        .from(event.data.toString(), ServiceNumberAddModel::class.java)
                chatViewModel.roomEntity?.let {
                    // 先判斷是否是同聊天室
                    if (serviceNumberAddMode.roomId != it.id) return
                    // 判斷是否是同服務號
                    if (serviceNumberAddMode.serviceNumberId != it.serviceNumberId) return
                    // 再判斷當前的服務人員是否是臨時成員
                    if (serviceNumberAddMode.memberIds.contains(selfUserId)) {
                        chatViewModel.serviceNumberAddFromProvisional(serviceNumberAddMode.roomId)
                    } else {
                        serviceNumberAddMode.memberIds.forEach {
                            if (chatFragment is ChatFragment) {
                                (chatFragment as ChatFragment).onRemoveMember(it)
                            }
                        }
                    }
                }
            }
            // 多人聊天室成員離開 更新下拉成員選單及 title
            MsgConstant.NOTICE_DISCUSS_MEMBER_EXIT -> {
                val memberExitSocket =
                    JsonHelper.getInstance().from(event.data, DiscussMemberSocket::class.java)
                chatViewModel.roomEntity?.let {
                    if (memberExitSocket.roomId == it.id) {
                        chatViewModel.setDiscussRoomTitleWhenMemberRemoved(
                            it,
                            Lists.newArrayList(memberExitSocket.userId)
                        )
                    }
                }
            }

            // 多人聊天室 成員加入
            MsgConstant.NOTICE_DISCUSS_MEMBER_ADD,
            // 社團/服務號臨時成员加入
            MsgConstant.GROUP_REFRESH_FILTER
            -> {
                event.data?.let {
                    chatViewModel.roomEntity?.let { chatRoomEntity ->
                        var isDelete = false
                        val roomId =
                            try {
                                // 邀請成員
                                val newMemberData =
                                    JsonHelper.getInstance().fromToMap<String, Any>(it.toString())
                                newMemberData["roomId"].toString()
                            } catch (e: Exception) {
                                // 成員退出
                                val newMemberData =
                                    JsonHelper
                                        .getInstance()
                                        .from(it, GroupRefreshBean::class.java)
                                isDelete = true
                                newMemberData.sessionId
                            }

                        if (roomId == chatRoomEntity.id) {
                            chatViewModel.doHandleMemberFromDB(
                                roomId = roomId,
                                isMemberRemoved = isDelete
                            )
                        }
                    }
                }
            }

            // 社團有人主動退出
            MsgConstant.USER_EXIT -> {
                val userExitBean = event.data as UserExitBean
                chatViewModel.roomEntity?.let { chatRoomEntity ->
                    if (userExitBean.roomId.equals(chatRoomEntity.id)) {
                        chatViewModel.doHandleMemberFromDB(
                            Lists.newArrayList(userExitBean.userId),
                            chatRoomEntity.id,
                            true
                        )
                    }

                    val mUserExitBean = event.data as UserExitBean
                    val roomId1 = mUserExitBean.roomId
                    val title1 = mUserExitBean.title
                    val imageUrl1 = mUserExitBean.imageUrl
                    val userId1 = mUserExitBean.userId
                    AccountRoomRelReference.deleteRelByRoomIdAndAccountId(null, roomId1, userId1)
                    if (chatRoomEntity.id == mUserExitBean.roomId) {
                        chatRoomEntity.name = title1
                        chatRoomEntity.avatarId = imageUrl1
                        refreshPager(chatRoomEntity)
                        if (chatRoomEntity.ownerId == mUserExitBean.userId) {
                            chatRoomEntity.ownerId = ""
                        }
                        getMemberPrivilege()
                    } else {
                    }
                }
            }

            // 多人聊天室or社團成員被剔除
            MsgConstant.NOTICE_DISCUSS_GROUP_MEMBER_REMOVED -> {
                val data = JsonHelper.getInstance().fromToMap<String, Any>(event.data.toString())
                chatViewModel.roomEntity?.let { chatRoomEntity ->
                    if (data["roomId"] == chatRoomEntity.id) {
                        val deletedMemberIds = data["deletedMemberIds"] as List<String>?
                        deletedMemberIds?.let {
                            if (it.contains(selfUserId)) {
                                chatViewModel.doKickOutChatRoomByOtherMember(chatRoomEntity.id)
                            } else {
                                chatViewModel.doHandleMemberFromDB(it, chatRoomEntity.id, true)
                            }
                        }
                    }
                }
            }
            // 多人聊天室成員更名
            MsgConstant.NOTICE_DISCUSS_GROUP_USER_PROFILE_CHANGED -> {
                val userProfile =
                    JsonHelper.getInstance().fromToMap<String, Any>(event.data.toString())
                chatViewModel.roomEntity?.let { chatRoomEntity ->
                    val userId = userProfile["userId"].toString()
                    if (chatRoomEntity.chatRoomMember.any { it.memberId == userId }) {
                        chatViewModel.doUpdateRoomTitle(
                            chatRoomEntity.id,
                            userId,
                            userProfile["nickName"].toString()
                        )
                    }
                }
            }

            MsgConstant.REFRESH_CUSTOMER_NAME -> {
                chatViewModel.roomEntity?.let {
                    val roomIdList = event.data as List<String>
                    if (roomIdList.contains(it.id)) {
                        chatViewModel.getRoomItem(it.id)
                    }
                }
            }

            MsgConstant.Do_UPDATE_CONTACT_BY_LOCAL -> { // 夥伴聊天室名稱更新
                val profileId = event.data as String
                chatViewModel.roomEntity?.let {
                    if (it.type == ChatRoomType.friend) {
                        binding.title.text =
                            chatViewModel.updateContactPersonChatRoomTitle(it, profileId)
                    }

                    binding.messageRV.adapter?.let {
                        it.notifyItemRangeChanged(0, it.itemCount)
                    }
                }
            }

            MsgConstant.MEMBER_EXIT_DISMISS_ROOM -> { // 多人聊天室退出聊天
                val roomId = event.data as String
                if (roomId == chatViewModel.roomEntity?.id) {
                    finish()
                }
            }

            MsgConstant.DESKTOP_LOGIN_SUCCESS -> {
                if (chatViewModel.roomEntity?.type == ChatRoomType.person) {
                    homeViewModel.getLoginDevicesList()
                }
            }

            MsgConstant.NOTICE_DISMISS_DISCUSS_ROOM -> {
                val roomId = event.data as String
                if (roomId == chatViewModel.roomEntity?.id) {
                    finish()
                }
            }

            MsgConstant.SCROLL_TO_TARGET_MESSAGE_POSITION -> {
                chatViewModel.roomEntity?.let {
                    val scrollData =
                        JsonHelper.getInstance().from(
                            event.string,
                            MutableMap::class.java
                        )
                    if (scrollData["roomId"] == it.id) {
                        val unReadIndex =
                            chatViewModel.mainMessageData.indexOf(
                                MessageEntity
                                    .Builder()
                                    .id(
                                        scrollData["messageId"] as String
                                    ).build()
                            )
                        if (unReadIndex > 0) {
                            val unReadMessage = chatViewModel.mainMessageData[unReadIndex]
                            doScrollToSelectMessageItemAndSetKeyword("", unReadMessage)
                        } else {
                        }
                    } else {
                    }
                }
            }

            MsgConstant.NOTICE_APPEND_NEW_MESSAGE_IDS -> {
                doGettingNewMessage = true
                chatViewModel.onMessageReceived(event.string)
            }

            MsgConstant.NOTICE_APPEND_OFFLINE_MESSAGE_IDS -> {
                val appendOffLineMessageIds =
                    JsonHelper.getInstance().fromToList(
                        event.string,
                        Array<String>::class.java
                    )
                chatViewModel.appendOfflineMessage(appendOffLineMessageIds)
                getUnreadData()
            }

            MsgConstant.NOTICE_APPEND_MESSAGE -> {
//                val appendMessage = JsonHelper.getInstance().from(
//                    event.string, MessageEntity::class.java
//                )
//                chatViewModel.roomEntity?.let {
//                    appendMessage?.let { message ->
//                        if (it.id == message.roomId) {
//                            displayMainMessage(
//                                isNew = false,
//                                isSendMsgDisplay = false,
//                                scrollToBottom = false,
//                                message,
//                                it.type == ChatRoomType.friend
//                            )
//                        }
//                    }
//                }
            }

//            MsgConstant.SEND_PHOTO_MEDIA_SELECTOR -> {
//                val selectorJson = event.string
//                val selectorData =
//                    JsonHelper.getInstance().fromToMap<String, String>(selectorJson)
//                val isOriginalStr = selectorData["isOriginal"]
//                val listStr = selectorData["list"]
//
//                val list =
//                    JsonHelper.getInstance().fromToList(
//                        listStr,
//                        Array<ImageBean>::class.java
//                    )
//                sendFileSize = list.size
//                isSendSingleFile = sendFileSize == 1
//                CELog.e(selectorJson)
//                executeSendPhotos(
//                    Lists.newArrayList<AMediaBean>(list),
//                    isOriginalStr.toBoolean()
//                )
//            }

            MsgConstant.NOTICE_EXECUTION_BUSINESS_CREATE_ACTION -> {
                val path = event.string
                executeTask(Lists.newArrayList(), path, false)
            }

            MsgConstant.REFRESH_FILTER ->
                if (!chatViewModel.recordMode) {
//                    chatViewModel.messageListRequestAsc(chatViewModel.mainMessageData.lastOrNull())
                } else {
                }

            MsgConstant.CLEAN_MSGS_FILTER -> onCleanMsgs()
            MsgConstant.REMOVE_FRIEND_FILTER -> {
                val account = event.data as UserProfileEntity?
                account?.let {
                    chatViewModel.roomEntity?.let { roomEntity ->
                        if (it.roomId == roomEntity.id) finish()
                    }
                }
            }

            MsgConstant.INTERNET_STSTE_FILTER -> {
                // EVAN_FLAG 2020-02-18 1.10.0 暫時拔除 linphone
                if ("true" == event.data.toString()) {
                    chatViewModel.loadChatMessageListFromDb()
                }
            }

            MsgConstant.SESSION_REFRESH_FILTER ->
                try {
                    val data = JsonHelper.getInstance().fromToMap<String, String>(event.string)
                    val key = data["key"]
                    val values = data["values"]
                    val roomId = data["roomId"]
                    chatViewModel.roomEntity?.let {
                        if (key == null || TextUtils.isEmpty(key) || it.id != roomId) {
                            return
                        }
                        when (key) {
                            "avatarUrl" -> {
                                it.avatarId = values
                                refreshPager(it)
                            }

                            "name" -> {
                                it.name = values
                                refreshPager(it)
                            }

                            "ownerId" -> {
                                it.ownerId = values
                                refreshPager(it)
                                hideNoOwnerNotify()
                            }

                            else -> refreshPager(it)
                        }
                    }
                } catch (e: Exception) {
                    CELog.e(e.message)
                }

            MsgConstant.CANCEL_FILTER -> {
                finish()
            }

            MsgConstant.MSG_RECEIVED_FILTER -> {
                val receiverMessage =
                    JsonHelper.getInstance().from(
                        event.string,
                        MessageEntity::class.java
                    )
                receiverMessage?.let {
                    chatViewModel.onReceive(receiverMessage)
                }
            }

            MsgConstant.MSG_STATUS_FILTER -> {
                val mMsgStatusBean = event.data as MsgStatusBean
                chatViewModel.onMessageSendStats(mMsgStatusBean)
            }

            MsgConstant.MSG_NOTICE_FILTER -> {
                val mMsgNoticeBean = event.data as MsgNoticeBean
                chatViewModel.onReadReceived(mMsgNoticeBean)
            }

            MsgConstant.SEND_UPDATE_AVATAR -> {
                val mUpdateAvatarBean = event.data as UpdateAvatarBean
                val avatar = mUpdateAvatarBean.avatar
                if (avatar == null || "" == avatar) {
                    return
                }

                if (NoticeCode.UPDATE_USER_AVATAR.getName() == mUpdateAvatarBean.type) {
                    var i = 0
                    while (i < chatViewModel.mainMessageData.size) {
                        val msg = chatViewModel.mainMessageData[i]
                        if (mUpdateAvatarBean.userId == msg.senderId) {
                            msg.avatarId = avatar
//                            Log.e(TAG, " message sender avatar id ")
                            messageListAdapter?.notifyItemChanged(i)
                        }
                        i++
                    }
                } else {
                }
            }

            MsgConstant.NOTICE_SERVICE_NUMBER_CONSULT_EVENT -> {
                val data = JsonHelper.getInstance().fromToMap<String, String>(event.string)
                val consultRoomId = data["consultRoomId"]
                val transferReason = data["transferReason"]

                consultRoomId?.let { consultId ->
                    chatViewModel.roomEntity?.let {
                        // 移除諮詢服務
                        if ("ConsultComplete" == data["event"]) {
                            chatViewModel.removeConsultRoom(consultId)
                        } else if ("ConsultStart" == data["event"]) {
                            // 加入諮詢服務
                            if (selfUserId == data["userId"] && it.serviceNumberAgentId == data["userId"] && it.id == data["srcRoomId"]) {
                                chatViewModel.addConsultRoom(consultId)
                            } else {
                            }
                        } else {
                        }
                    }
                }
            }

            MsgConstant.SERVICE_NUMBER_PERSONAL_START -> {
                val map = event.data as Map<String, String>
                chatViewModel.roomEntity?.let {
                    if (it.id == map["roomId"]) {
                        if (selfUserId != map["serviceNumberAgentId"]) {
                            chatViewModel.doServiceNumberServicedStatus(null)
                            chatViewModel.getProvisionalMember(it.id)
                        } else {
                        }
                    } else {
                    }
                }
            }

            MsgConstant.SERVICE_NUMBER_TRANSFER_STATUS, MsgConstant.SERVICE_NUMBER_PERSONAL_STOP -> {
                val targetRoomId = event.data as String
                chatViewModel.roomEntity?.let {
                    if (it.id == targetRoomId) {
                        chatViewModel.doServiceNumberServicedStatus(null)
                        chatViewModel.getProvisionalMember(it.id)
                    } else {
                    }
                }
            }

            MsgConstant.APPOINT_STATUS_CHECKING -> {
                val rId = event.data as String
                chatViewModel.roomEntity?.let {
                    if (it.isService(selfUserId)) {
                        if (it.id == rId) {
                            chatViewModel.doAppointStatus(rId)
                        } else {
                        }
                    } else {
                    }
                }
            }

            MsgConstant.UPDATE_MESSAGE_STATUS ->
                if (event.data is MessageEntity) {
                    val msg = event.data as MessageEntity
                    val position = chatViewModel.mainMessageData.indexOf(msg)
                    chatViewModel.mainMessageData[position].content =
                        msg.content().toStringContent()
                    if (position >= 0) {
//                        binding.messageRV.refreshData(position, msg)
                    } else {
                    }
                } else {
                }

            MsgConstant.BUSINESS_BINDING_ROOM_EVENT -> {
                val datas = event.data as Map<String, String>
                val bindRoomId = datas["roomId"]
                chatViewModel.roomEntity?.let {
                    if (ChatRoomType.FRIEND_or_DISCUSS.contains(it.type) && it.id == bindRoomId) {
                        bindingBusiness(
                            bindRoomId,
                            BusinessContent(
                                datas["businessId"],
                                datas["businessName"],
                                BusinessCode.of(
                                    datas["businessCode"]
                                )
                            ),
                            false
                        )
                    } else {
                    }
                }
            }

            MsgConstant.NOTICE_PROVISIONAL_MEMBER_REMOVED -> {
                chatViewModel.roomEntity?.let {
                    val idSets = event.data as Set<String>
                    val existIds = it.provisionalIds.iterator()
                    while (existIds.hasNext()) {
                        for (id in idSets) {
                            if (existIds.next() == id) {
                                existIds.remove()
                                break
                            }
                        }
                    }
                    chatViewModel.getMemberProfileEntity(it.provisionalIds)
                }
            }

            MsgConstant.NOTICE_PROVISIONAL_MEMBER_ADDED, MsgConstant.NOTICE_SERVICE_NUMBER_ADD_FROM_PROVISIONAL -> {
                chatViewModel.roomEntity?.let {
                    val provisionalMemberAddedSocket =
                        JsonHelper.getInstance().from(
                            event.data,
                            ProvisionalMemberAddedSocket::class.java
                        )
                    if (provisionalMemberAddedSocket.roomId == it.id) {
                        chatViewModel.getProvisionalMember(provisionalMemberAddedSocket.roomId)
                    } else {
                    }
                }
            }

            MsgConstant.MESSAGE_QUICK_REPLY -> {
                chatViewModel.roomEntity?.let {
                    val listType = object : TypeToken<List<QuickReplyItem?>?>() {}.type
                    val quickReplySocket =
                        JsonHelper.getInstance().from(
                            event.data.toString(),
                            QuickReplySocket::class.java
                        )
                    if (quickReplySocket.roomId == it.id) {
                    } else {
                    }
                }
            }

            MsgConstant.MESSAGE_AI_CONSULTATION_QUOTED_IMAGE -> {
                val quotedImage = event.data as String
                sendAiQuoteImage(quotedImage)
            }

            MsgConstant.MESSAGE_AI_CONSULTATION_QUOTED_VIDEO -> {
                val quotedVideo = event.data as String
                sendAiQuoteVideo(quotedVideo)
            }

            MsgConstant.NOTICE_CLEAR_CHAT_ROOM_ALL_MESSAGE -> {
                chatViewModel.onRoomMessageClean()
            }

            MsgConstant.UPDATE_LINE_CUSTOMER_AVATAR -> {
//                    binding.messageRV.refreshData()
            }

            MsgConstant.UPDATE_TODO_EXPIRED_COUNT_EVENT, MsgConstant.UI_NOTICE_TODO_REFRESH -> {
                chatViewModel.roomEntity?.let {
                    chatViewModel.getConsultTodoList(it.type, it.id)
                }
            }

            MsgConstant.NOTICE_MEDIA_SELECTOR_REFRESH -> {
                // 更新圖庫
                binding.funMedia.refreshData()
            }
        }
    }

    private fun sendAiQuoteImage(imagePath: String) {
        sendFileSize = 1
//        val filePath = PictureParse.parsePath(this, imagePath)
        chatViewModel.sendImage(isOriginPhoto = true, path = imagePath, isQuote = true)
    }

    private fun sendAiQuoteVideo(videoPath: String) {
        sendFileSize = 1
        val iVideo: IVideoSize = VideoSizeFromVideoFile(videoPath)
        chatViewModel.sendVideo(iVideo, true)
    }

    private fun hideNoOwnerNotify() {
        binding.clNotifyNoOwner.visibility = View.GONE
    }

    fun onCleanMsgs() {
        chatViewModel.mainMessageData.clear()
        refreshListView()
    }

    // 抓取社團成員的權限
    private fun getMemberPrivilege() {
        chatViewModel.roomEntity?.let {
            chatViewModel.getChatMember(it.id)
        }
    }

    fun disableInvite() {
        binding.inviteIV.visibility = View.GONE
    }

    fun triggerToolbarClick() {
        binding.title.performClick()
    }

    fun showToolBar(show: Boolean) {
        binding.titleBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    fun navigateToSubscribePage() {
        chatViewModel.roomEntity?.let {
            val bundle =
                bundleOf(
                    BundleKey.SUBSCRIBE_NUMBER_ID.key() to it.serviceNumberId,
                    BundleKey.ROOM_ID.key() to it.id,
                    BundleKey.IS_SUBSCRIBE.key() to true,
                    BundleKey.WHERE_COME.key() to javaClass.simpleName
                )
            IntentUtil.startIntent(this@ChatNormalActivity, SubscribeInformationHomepageActivity::class.java, bundle)
            overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out)
        }
    }

    private fun setRightCancelView() =
        CoroutineScope(Dispatchers.Main).launch {
            rightCancelTV = TextView(this@ChatNormalActivity)
            rightCancelTV?.apply {
                text = "取消"
                textSize = 16f
                setPadding(10, 0, 10, 0)
                setTextColor(Color.parseColor("#ffffff"))
            }
        }

    fun setRightView() {
        rightCancelTV ?: setRightCancelView()
        binding.rightAction.setOnClickListener {
            hideChecked()
        }
    }

    fun hideChecked() {
        for (message in chatViewModel.mainMessageData) {
            message.isShowChecked = false
            message.isDelete = false
        }
        refreshListView()
    }

    fun setChannelIconVisibility(
        @DrawableRes resId: Int,
        status: ServiceNumberStatus
    ) = CoroutineScope(Dispatchers.Main).launch {
        binding.ivChannel.setImageResource(resId)
        binding.ivChannel.visibility = if (status == ServiceNumberStatus.ON_LINE) View.GONE else View.VISIBLE
    }

    fun showTopMenu(show: Boolean) =
        CoroutineScope(Dispatchers.Main).launch {
            binding.llRight.visibility = if (show) View.VISIBLE else View.GONE
        }

    fun refreshPager(chatRoomEntity: ChatRoomEntity) =
        CoroutineScope(Dispatchers.IO).launch {
            chatViewModel.roomEntity = chatRoomEntity
            setTitleBar()
            ChatRoomReference.getInstance().save(chatRoomEntity)
        }

    // 成員下拉點擊
    override fun onItemClick(
        v: View?,
        account: UserProfileEntity?,
        position: Int
    ) {
        account?.let {
            it.id?.let {
                toChatRoomByUserProfile(account)
            } ?: run {
                when (chatViewModel.roomEntity?.type) {
                    ChatRoomType.serviceMember -> toServiceNumberHomePage() // 服務號主頁
                    else -> toMainPage() // 聊天室主頁
                }
                memberPopupWindow?.dismiss()
            }
        }
    }

//    override fun onMessageSlideReply(messageEntity: MessageEntity) {
//        executeReply(messageEntity)
//    }

    override fun onTipMessageClick(message: MessageEntity) {
        val content = message.content()
        val input =
            if (content is TextContent) {
                content.simpleContent()
            } else if (content is AtContent) {
                chatViewModel.roomEntity?.let {
                    val ceMentions = content.mentionContents
                    val builder = AtMatcherHelper.matcherAtUsers("@", ceMentions, it.membersTable)
                    builder.toString()
                }
            } else {
                null
            }
        input?.let {
            binding.chatKeyboardLayout.clearInputArea()
            binding.chatKeyboardLayout.setInputHETText(input)
            KeyboardHelper.open(binding.chatKeyboardLayout.inputHET)
        }
    }

    override fun onAtMessageClick(id: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val account = DBManager.getInstance().queryFriend(id)
            toChatRoomByUserProfile(account)
        }
    }

    override fun onImageMessageClick(message: MessageEntity) {
        if (MessageType.BUSINESS == message.type) {
            triggerToolbarClick()
        } else if (MessageType.IMAGE == message.type) {
            chatViewModel.roomEntity?.let {
                val bundle = Bundle()
                bundle.putSerializable(BundleKey.PHOTO_GALLERY_MESSAGE.key(), message)
                bundle.putString(BundleKey.ROOM_ID.key(), it.id)
                bundle.putString(BundleKey.CHAT_ROOM_NAME.key(), it.name)
                bundle.putString(BundleKey.ROOM_TYPE.key(), ChatRoomEnum.NORMAL_ROOM.name)
                startIntent(
                    this,
                    MediaGalleryActivity::class.java,
                    bundle
                )
            }
        }
    }

    override fun onVideoMessageClick(message: MessageEntity) {
        val bundle =
            bundleOf(
                BundleKey.PHOTO_GALLERY_MESSAGE.key() to message,
                BundleKey.ROOM_ID.key() to message.roomId,
                BundleKey.ROOM_TYPE.key() to ChatRoomEnum.NORMAL_ROOM.name
            )
        startIntent(this, MediaGalleryActivity::class.java, bundle)
    }

    override fun onMessageLongClick(message: MessageEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                binding.themeMRV.visibility = View.GONE
                binding.funMedia.visibility = View.GONE
            }
            val richMenu = getMessageLongClickMenu(message)
            showMessageLongClickMenu(message, richMenu)
        }
    }

    override fun onThemeNearMessageClick(nearMessageSequence: Int) {
        if (isDestroyed || isFinishing) return
        CoroutineScope(Dispatchers.IO).launch {
            val currentNearMessage = chatViewModel.mainMessageData.find { it.sequence == nearMessageSequence }
            currentNearMessage?.let {
                val index = chatViewModel.mainMessageData.indexOf(it)
                binding.messageRV.post {
                    it.isAnimator = true
                    scrollToCurrentMessage(index)
                }
            } ?: run {
                if (!isDestroyed || !isFinishing) {
                    chatViewModel.mainMessageData.firstOrNull { it.sequence != 0 }?.let {
                        chatViewModel.getLoadMoreMessageList(it, nearMessageSequence)
                    }
                }
            }
        }
    }

    override fun onThemeUnderMessageClick(themeId: String?) {
        showThemeView(themeId)
    }

    override fun buildScreenShot(message: MessageEntity) {
        buildUpRangeScreenshotData(message)
    }

    override fun onMessageStatusClick(message: MessageEntity) {
        chatViewModel.resendMessage(message)
    }

    override fun onAvatarIconClick(senderId: String) {
        chatViewModel.onAvatarIconClick(senderId)
    }

    private fun showMessageLongClickMenu(
        message: MessageEntity,
        richMenuBottom: MutableList<RichMenuBottom>
    ) = CoroutineScope(Dispatchers.Main).launch {
        chatViewModel.roomEntity?.let {
            binding.chatKeyboardLayout.setRichMenuGridCount(5).setOnItemClickListener(
                message,
                richMenuBottom,
                classifyAiffInMenu(),
                object : BottomRichMeunAdapter.OnItemClickListener {
                    override fun onClick(
                        msg: MessageEntity,
                        menu: RichMenuBottom,
                        position: Int
                    ) {
                        binding.chatKeyboardLayout.showKeyboard()
                        onMessageBottomMenuClick(menu, msg)
                    }

                    override fun onCancle() {
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(1000)
                            chatViewModel.isObserverKeyboard = true
                        }
                    }
                }
            ) { _, aiffId ->
                val aiffInfo = AiffDB.getInstance(this@ChatNormalActivity).aiffInfoDao.getAiffInfo(aiffId)
                aiffManager?.showAiffViewByInfo(aiffInfo)
            }
        }
    }

    private suspend fun getMessageLongClickMenu(message: MessageEntity): MutableList<RichMenuBottom> =
        withContext(Dispatchers.IO) {
            val menu = mutableListOf<RichMenuBottom>()
            when {
                !message.themeId.isNullOrEmpty() && !message.nearMessageContent.isNullOrEmpty() -> {
                    menu.addAll(getCurrentLongClickMenu(RichMenuType.REPLY_RICH, message))
                }

                message.type == MessageType.AT -> {
                    menu.addAll(getCurrentLongClickMenu(RichMenuType.AT_RICH, message))
                }

                message.type == MessageType.TEXT ||
                    message.type == MessageType.BUSINESS_TEXT -> {
                    menu.addAll(getCurrentLongClickMenu(RichMenuType.TEXT_RICH, message))
                }

                message.type == MessageType.VOICE -> {
                    menu.addAll(getCurrentLongClickMenu(RichMenuType.VOICE_RICH, message))
                }

                message.type == MessageType.FILE ||
                    message.type == MessageType.IMAGE_TEXT -> {
                    menu.addAll(getCurrentLongClickMenu(RichMenuType.OTHER_RICH, message))
                }

                message.type == MessageType.STICKER -> {
                    menu.addAll(getCurrentLongClickMenu(RichMenuType.STICKER_RICH, message))
                }

                message.type == MessageType.IMAGE -> {
                    menu.addAll(getCurrentLongClickMenu(RichMenuType.IMAGE_RICH, message))
                }

                message.type == MessageType.CALL -> {
                    menu.addAll(getCurrentLongClickMenu(RichMenuType.CALL_RICH, message))
                }

                message.type == MessageType.VIDEO -> {
                    menu.addAll(getCurrentLongClickMenu(RichMenuType.VIDEO_RICH, message))
                }

                message.type == MessageType.TEMPLATE -> {
                    menu.addAll(getCurrentLongClickMenu(RichMenuType.TEMPLATE_RICH, message))
                }

                else -> {
                    menu.addAll(getCurrentLongClickMenu(RichMenuType.TEXT_RICH, message))
                }
            }

            chatViewModel.roomEntity?.let {
                val hasBusinessSystem = UserPref.getInstance(this@ChatNormalActivity).hasBusinessSystem()
                if (it.type == ChatRoomType.subscribe || !hasBusinessSystem) {
                    menu.remove(RichMenuBottom.TASK)
                }

                if (selfUserId == it.ownerId &&
                    it.serviceNumberOpenType.contains("C") ||
                    (it.serviceNumberOpenType.contains("O") && ChatRoomType.serviceMember != it.type)
                ) {
                    menu.remove(RichMenuBottom.REPLY)
                }

                if (ChatRoomType.services == it.type) {
                    menu.remove(RichMenuBottom.RECOVER)
                }

                if (ChatRoomType.system == it.type) {
                    menu.remove(RichMenuBottom.TODO)
                }
            }

            return@withContext menu
        }

    private suspend fun getCurrentLongClickMenu(
        type: RichMenuType,
        message: MessageEntity
    ): MutableList<RichMenuBottom> =
        withContext(Dispatchers.IO) {
            val currentMenu = mutableListOf<RichMenuBottom>()

            // 失敗的訊息只給刪除功能
            if (MessageStatus.FAILED_or_ERROR.contains(message.status)) {
                currentMenu.add(RichMenuBottom.DELETE)
                return@withContext currentMenu
            }

            if (RichMenuType.CALL_RICH == type) {
                currentMenu.addAll(type.get())
                return@withContext currentMenu
            }

            if (message.senderId == selfUserId) {
                val status = message.status?.value ?: -1
                val retractValidMinute =
                    TokenPref.getInstance(this@ChatNormalActivity).retractValidMinute * 60L
                // 內部服務號可回收信息
                chatViewModel.roomEntity?.let {
                    if ((status > 0 && status != 2 && it.type != ChatRoomType.subscribe) &&
                        (System.currentTimeMillis() - message.sendTime) / 1000 <= retractValidMinute
                    ) {
                        currentMenu.add(RichMenuBottom.RECOVER)
                    }
                }
            }
            currentMenu.addAll(type.get())

            val isThemeMessage =
                if (!message.nearMessageId.isNullOrEmpty()) {
                    true
                } else {
                    chatViewModel.mainMessageData.any { it.themeId == message.id }
                }

            if (isThemeMessage) {
                currentMenu.removeIf {
                    it == RichMenuBottom.RECOVER
//                        || it == RichMenuBottom.DELETE
                }
            }

            return@withContext currentMenu
        }

    /**
     * 顯示主題聊天室
     */
    private fun showThemeView(themeId: String?) {
        val themeMessage = MessageReference.findById(themeId)
        themeMessage?.let {
            if (it.from == ChannelType.FB && it.type == MessageType.TEMPLATE) return
        }

        binding.expandIV.visibility = View.GONE
        binding.themeMRV.clearData()
        binding.themeMRV.setMaxHeight(setupDefaultThemeHeight(2.0 / 3.0))
        val themeMessages = findThemeMessageListByThemeId(themeId)

        for (message in themeMessages) {
            displayThemeMessage(themeId!!, false, message)
        }
        binding.themeMRV.visibility = View.VISIBLE
        chatViewModel.isThemeOpen = true
        chatViewModel.themeMessage = binding.themeMRV.themeData
        chatViewModel.nearMessage = binding.themeMRV.nearData
        this.themeId = themeId
        binding.themeMRV.post {
            val maxHeight = setupDefaultThemeHeight(2.0 / 3.0)
            val height = binding.themeMRV.height
            if (maxHeight == height) {
                binding.expandIV.visibility = View.VISIBLE
            } else {
                binding.expandIV.visibility = View.GONE
            }
        }
    }

    private fun setupDefaultThemeHeight(proportion: Double): Int {
        val paramsHeight =
            if (proportion == 0.0) ConstraintLayout.LayoutParams.MATCH_PARENT else ConstraintLayout.LayoutParams.WRAP_CONTENT
        val params =
            ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, paramsHeight)
        params.apply {
            verticalBias = 1.0f
            bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        }
        binding.themeMRV.layoutParams = params

        val toolbarHeight = UiHelper.dip2px(this, 44.0f)
        val inputHeight = UiHelper.dip2px(this, 45.0f)
        val displayHeight = UiHelper.getDisplayHeight(this)
        val roomHeight = displayHeight - (toolbarHeight + inputHeight)
        return (roomHeight * proportion).toInt()
    }

    fun findThemeMessageListByThemeId(themeId: String?): List<MessageEntity> {
        val iterator: Iterator<MessageEntity> = chatViewModel.mainMessageData.iterator()
        var currentDate = ""
        val dateMessageList = mutableListOf<MessageEntity>()
        chatViewModel.roomEntity?.let {
            while (iterator.hasNext()) {
                val message = iterator.next()
                if ((themeId == message.id || themeId == message.themeId) && MessageFlag.RETRACT != message.flag) {
                    val sendDate = formatDateSafely(message.sendTime)
                    val messageDate =
                        getDateLabelMessage(message.sendTime, it.id, message.id, message.themeId)
                    val isMessageListAlreadyAddDateLabel =
                        dateMessageList.find { it.sendTime == TimeUtil.getDayBegin(message.sendTime) }

                    if (sendDate != currentDate && isMessageListAlreadyAddDateLabel == null) {
                        currentDate = sendDate
                        dateMessageList.add(messageDate)
                    }

                    dateMessageList.add(message)
                }
            }
            dateMessageList.sort()
        }
        return dateMessageList
    }

    private fun formatDateSafely(timestamp: Long): String =
        try {
            if (timestamp < 0) {
                throw IllegalArgumentException("Invalid timestamp: $timestamp")
            }
            messageTimeLineFormat.format(timestamp)
        } catch (e: Exception) {
            Log.e("DateFormatting", "Error formatting date: ${e.message}")
            "date error"
        }

    /**
     * 取得時間標籤 MessageEntity
     * @param sendTime 訊息發送的時間
     * @param roomId 原本的聊天室ID
     * */
    private fun getDateLabelMessage(
        sendTime: Long,
        roomId: String?,
        messageId: String?,
        themeId: String?
    ): MessageEntity {
        val dayBegin = TimeUtil.getDayBegin(sendTime)
        val message =
            MessageEntity
                .Builder()
                .id(Tools.generateTimeMessageId(dayBegin))
                .sendTime(dayBegin)
                .roomId(roomId)
                .status(MessageStatus.SUCCESS)
                .sourceType(SourceType.SYSTEM)
                .content(UndefContent("TIME_LINE").toStringContent())
                .sendTime(dayBegin)
        themeId?.let {
            message.themeId(it)
        } ?: run {
            messageId?.let { message.themeId(it) }
        }

        return message.build()
    }

    /**
     * 顯示主題訊息
     */
    fun displayThemeMessage(
        themeId: String,
        isSendMsgDisplay: Boolean,
        message: MessageEntity
    ) {
        binding.themeMRV
            .setThemeId(themeId)
            .setData(message)
            .refreshToBottom()
    }

    /**
     * 組裝截圖資料
     */
    private fun buildUpRangeScreenshotData(entity: MessageEntity) {
        // 取消選取
        if (entity.isShowSelection == true) {
            entity.isShowSelection = false
            screenShotData.remove(entity)
            val removeIndex = chatViewModel.mainMessageData.indexOf(entity)
            binding.messageRV.adapter!!.notifyItemChanged(removeIndex, false)
            return
        }

        var currentIndex = 0
        // 點擊的 message item
        val clickIndex = chatViewModel.mainMessageData.indexOf(entity)

        if (screenShotData.isEmpty()) {
            // 進入截圖狀態之後，都沒有選取
            currentIndex = clickIndex
        } else if (screenShotData.size == 1) {
            // 剛進截圖狀態
            currentIndex = chatViewModel.mainMessageData.indexOf(screenShotData[0])
        } else {
            val first = chatViewModel.mainMessageData.indexOf(screenShotData[0])
            val last =
                chatViewModel.mainMessageData.indexOf(screenShotData[screenShotData.size - 1])
            if (clickIndex < first) currentIndex = first
            if (clickIndex > last) currentIndex = last

            if (clickIndex in (first + 1) until last) {
                val selectMessage = chatViewModel.mainMessageData[clickIndex]
                selectMessage.isShowSelection = true
                screenShotData.add(selectMessage)
                binding.messageRV.adapter?.notifyItemChanged(clickIndex, false)
                return
            }
        }

        val startIndex = min(currentIndex.toDouble(), clickIndex.toDouble()).toInt()
        val endIndex = max(currentIndex.toDouble(), clickIndex.toDouble()).toInt()
        for (i in startIndex..endIndex) {
            val selectMessage = chatViewModel.mainMessageData[i]
            if (!screenShotData.contains(selectMessage)) {
                selectMessage.isShowSelection = true
                screenShotData.add(selectMessage)
                binding.messageRV.adapter?.notifyItemChanged(i, false)
            }
        }
        screenShotData.sort()
    }

    // AIFF聊天室消息菜單
    private suspend fun classifyAiffInMenu(): List<RichMenuInfo> =
        withContext(Dispatchers.IO) {
            val aiffList: MutableList<RichMenuInfo> = Lists.newArrayList()
            chatViewModel.roomEntity?.let {
                // Aiff消息菜單
                val aiffInfoList =
                    AiffDB.getInstance(this@ChatNormalActivity).aiffInfoDao.aiffInfoListByUseTime
                if (aiffInfoList.size > 0) {
                    for (aiff in aiffInfoList) {
                        if (aiff.embedLocation == AiffEmbedLocation.MessageMenu.name) {
                            if (ChatRoomType.self == it.type) {
                                val info =
                                    RichMenuInfo(
                                        RichMenuInfo.MenuType.AIFF.type,
                                        aiff.id,
                                        aiff.pictureId,
                                        aiff.title,
                                        aiff.name,
                                        aiff.pinTimestamp,
                                        aiff.useTimestamp
                                    )
                                aiffList.add(info)
                            } else if (ChatRoomType.system == it.type) {
                                val info =
                                    RichMenuInfo(
                                        RichMenuInfo.MenuType.AIFF.type,
                                        aiff.id,
                                        aiff.pictureId,
                                        aiff.title,
                                        aiff.name,
                                        aiff.pinTimestamp,
                                        aiff.useTimestamp
                                    )
                                aiffList.add(info)
                            } else if (ChatRoomType.discuss == it.type) {
                                val info =
                                    RichMenuInfo(
                                        RichMenuInfo.MenuType.AIFF.type,
                                        aiff.id,
                                        aiff.pictureId,
                                        aiff.title,
                                        aiff.name,
                                        aiff.pinTimestamp,
                                        aiff.useTimestamp
                                    )
                                aiffList.add(info)
                            } else if (ChatRoomType.friend == it.type) {
                                val info =
                                    RichMenuInfo(
                                        RichMenuInfo.MenuType.AIFF.type,
                                        aiff.id,
                                        aiff.pictureId,
                                        aiff.title,
                                        aiff.name,
                                        aiff.pinTimestamp,
                                        aiff.useTimestamp
                                    )
                                aiffList.add(info)
                            } else if (ChatRoomType.group == it.type) {
                                // 社團聊天室
                                val info =
                                    RichMenuInfo(
                                        RichMenuInfo.MenuType.AIFF.type,
                                        aiff.id,
                                        aiff.pictureId,
                                        aiff.title,
                                        aiff.name,
                                        aiff.pinTimestamp,
                                        aiff.useTimestamp
                                    )
                                aiffList.add(info)
                            } else if (ChatRoomType.serviceMember == it.type && ServiceNumberType.BOSS == it.serviceNumberType) {
                                // 商務號秘書群聊天室
                                val info =
                                    RichMenuInfo(
                                        RichMenuInfo.MenuType.AIFF.type,
                                        aiff.id,
                                        aiff.pictureId,
                                        aiff.title,
                                        aiff.name,
                                        aiff.pinTimestamp,
                                        aiff.useTimestamp
                                    )
                                aiffList.add(info)
                            } else if (ChatRoomType.services == it.type && ServiceNumberType.BOSS == it.serviceNumberType) {
                                // 商務號聊天室
                                val info =
                                    RichMenuInfo(
                                        RichMenuInfo.MenuType.AIFF.type,
                                        aiff.id,
                                        aiff.pictureId,
                                        aiff.title,
                                        aiff.name,
                                        aiff.pinTimestamp,
                                        aiff.useTimestamp
                                    )
                                aiffList.add(info)
                            } else if (ChatRoomType.services == it.type &&
                                it.serviceNumberOpenType.contains(
                                    "I"
                                ) &&
                                it.ownerId != userId
                            ) {
                                // 服務號員工進線聊天室(服務人員)
                                val info =
                                    RichMenuInfo(
                                        RichMenuInfo.MenuType.AIFF.type,
                                        aiff.id,
                                        aiff.pictureId,
                                        aiff.title,
                                        aiff.name,
                                        aiff.pinTimestamp,
                                        aiff.useTimestamp
                                    )
                                aiffList.add(info)
                            } else if (ChatRoomType.services == it.type &&
                                it.serviceNumberOpenType.contains(
                                    "I"
                                ) &&
                                it.ownerId == userId
                            ) {
                                // 服務號員工進線聊天室(詢問者)
                                val info =
                                    RichMenuInfo(
                                        RichMenuInfo.MenuType.AIFF.type,
                                        aiff.id,
                                        aiff.pictureId,
                                        aiff.title,
                                        aiff.name,
                                        aiff.pinTimestamp,
                                        aiff.useTimestamp
                                    )
                                aiffList.add(info)
                            } else if (ChatRoomType.serviceMember == it.type && ServiceNumberType.BOSS != it.serviceNumberType) {
                                // 服務號服務成員聊天室
                                val info =
                                    RichMenuInfo(
                                        RichMenuInfo.MenuType.AIFF.type,
                                        aiff.id,
                                        aiff.pictureId,
                                        aiff.title,
                                        aiff.name,
                                        aiff.pinTimestamp,
                                        aiff.useTimestamp
                                    )
                                aiffList.add(info)
                            } else if (ChatRoomType.services == it.type &&
                                it.serviceNumberOpenType.contains(
                                    "O"
                                ) &&
                                it.ownerId != userId
                            ) {
                                // 服務號客戶進線聊天室
                                val info =
                                    RichMenuInfo(
                                        RichMenuInfo.MenuType.AIFF.type,
                                        aiff.id,
                                        aiff.pictureId,
                                        aiff.title,
                                        aiff.name,
                                        aiff.pinTimestamp,
                                        aiff.useTimestamp
                                    )
                                aiffList.add(info)
                            }
                        }
                    }
                }
                aiffList.sort()
            }
            return@withContext aiffList
        }

    private fun onMessageBottomMenuClick(
        menu: RichMenuBottom,
        message: MessageEntity
    ) {
        when (menu) {
            RichMenuBottom.MULTI_COPY -> {
                message.isDelete = menu.isMulti
                openBottomRichMenu(
                    RichMenuBottom.MULTI_COPY,
                    OpenBottomRichMeunType.MULTIPLE_SELECTION,
                    Lists.newArrayList(
                        RichMenuBottom.MULTI_COPY,
                        RichMenuBottom.CANCEL
                    )
                )
            }

            RichMenuBottom.MULTI_TRANSPOND -> {
                message.isDelete = menu.isMulti
                openBottomRichMenu(
                    RichMenuBottom.MULTI_TRANSPOND,
                    OpenBottomRichMeunType.MULTIPLE_SELECTION,
                    Lists.newArrayList(
                        RichMenuBottom.MULTI_TRANSPOND,
                        RichMenuBottom.CANCEL
                    )
                )
            }

            RichMenuBottom.DELETE -> {
                executeDelete(message)
                actionStatus = ActionStatus.SCROLL
            }

            RichMenuBottom.RECOVER -> {
                binding.tvTip.text =
                    getString(
                        R.string.text_retract_tip,
                        TokenPref.getInstance(this).retractValidMinute
                    )
                binding.scopeRetractTip.visibility = View.VISIBLE
                if (TokenPref.getInstance(this).retractRemind) {
                    binding.scopeRetractTipText.visibility = View.GONE
                }
                binding.cbTip.setOnClickListener { v ->
                    if (binding.scopeRetractTip.visibility == View.GONE) {
                        binding.scopeRetractTip.visibility = View.VISIBLE
                    }
                }
                binding.btnEdit.setOnClickListener { v ->
                    // 編輯
                    if (binding.cbTip.isChecked) {
                        TokenPref
                            .getInstance(
                                this
                            ).retractRemind = true
                    }
                    executeRecover(
                        Lists.newArrayList(
                            message
                        )
                    )
                    actionStatus = ActionStatus.SCROLL
                    onTipMessageClick(message)
                }
                binding.btnRetract.setOnClickListener { v ->
                    // 收回
                    if (binding.cbTip.isChecked) {
                        TokenPref
                            .getInstance(
                                this
                            ).retractRemind = true
                    }
                    executeRecover(
                        Lists.newArrayList(
                            message
                        )
                    )
                    actionStatus = ActionStatus.SCROLL
                }
            }

            RichMenuBottom.REPLY -> {
                executeReply(message)
                actionStatus = ActionStatus.SCROLL
            }

            RichMenuBottom.SHARE -> {
                executeShare(message)
                actionStatus = ActionStatus.SCROLL
            }

            RichMenuBottom.SCREENSHOTS -> {
                message.isShowSelection = true
                screenShotData.add(message)
//                binding.xrefreshLayout.setBackgroundColor(-0xadadae)
                binding.messageRV.setBackgroundColor(-0xadadae)
                showToolBar(false)
                val richMenuBottoms: List<RichMenuBottom> =
                    Lists.newArrayList(
                        RichMenuBottom.ANONYMOUS.position(0),
                        RichMenuBottom.PREVIEW.position(1),
                        RichMenuBottom.SHARE.position(2),
                        RichMenuBottom.CANCEL.position(3),
                        RichMenuBottom.SAVE.position(4)
                    )
                openBottomRichMenu(
                    RichMenuBottom.SCREENSHOTS,
                    OpenBottomRichMeunType.RANGE_SELECTION,
                    richMenuBottoms
                )
            }

            RichMenuBottom.TASK -> {
                message.isShowSelection = true
                screenShotData.add(message)
                binding.xrefreshLayout.setBackgroundColor(-0xadadae)
                binding.messageRV.setBackgroundColor(-0xadadae)
                showToolBar(false)
                val taskRichMenus: List<RichMenuBottom> =
                    Lists.newArrayList(
                        RichMenuBottom.ANONYMOUS.position(0),
                        RichMenuBottom.NEXT
                            .position(1)
                            .str(R.string.alert_preview),
                        RichMenuBottom.CANCEL.position(2),
                        RichMenuBottom.CONFIRM.position(3)
                    )
                openBottomRichMenu(
                    RichMenuBottom.TASK,
                    OpenBottomRichMeunType.RANGE_SELECTION,
                    taskRichMenus
                )
            }

            RichMenuBottom.TODO -> {
                executeTodo(message)
                actionStatus = ActionStatus.SCROLL
            }

            else -> {}
        }
    }

    /**
     * 打開模式定義共用function，後其從夠定義列舉函數
     */
    private fun openBottomRichMenu(
        type: RichMenuBottom,
        action: OpenBottomRichMeunType,
        gridMenus: List<RichMenuBottom>
    ) {
        when (action) {
            OpenBottomRichMeunType.RANGE_SELECTION -> {
                messageListAdapter?.setAdapterMode(MessageAdapterMode.RANGE_SELECTION)
                // 先關閉背景點擊，CheckBox太小會誤觸發
                binding.xrefreshLayout.setOnBackgroundClickListener(null)
                binding.chatKeyboardLayout
                    .setRichMenuGridCount(gridMenus.size)
                    .setOnItemClickListener(
                        null,
                        gridMenus,
                        Lists.newArrayList(),
                        object : BottomRichMeunAdapter.OnItemClickListener {
                            override fun onClick(
                                msg: MessageEntity?,
                                menu: RichMenuBottom,
                                position: Int
                            ) {
                                actionStatus = ActionStatus.SCROLL
                                when (menu) {
                                    RichMenuBottom.ANONYMOUS -> {
                                        MessageDomino.clear()
                                        messageListAdapter?.switchAnonymousMode(messageListAdapter?.isAnonymousMode?.not() ?: true)
                                    }

                                    RichMenuBottom.PREVIEW, RichMenuBottom.NEXT, RichMenuBottom.SAVE, RichMenuBottom.SHARE, RichMenuBottom.CONFIRM -> {
                                        executeScreenshots(RichMenuBottom.NEXT == menu, menu)
                                    }

                                    RichMenuBottom.CANCEL -> {
                                        clearScreenshotsFunction()
                                    }

                                    else -> {}
                                }
                            }

                            override fun onCancle() {
                                actionStatus = ActionStatus.SCROLL
                                messageListAdapter?.setAdapterMode(MessageAdapterMode.DEFAULT)
                                messageListAdapter?.switchAnonymousMode(false)
                            }
                        },
                        null
                    )
                binding.chatKeyboardLayout.hideKeyboard()
            }

            OpenBottomRichMeunType.MULTIPLE_SELECTION -> {
                messageListAdapter?.setAdapterMode(MessageAdapterMode.SELECTION)
//                binding.messageRV.setAdapterMode(MessageAdapterMode.SELECTION)
                // 先關閉背景點擊，CheckBox太小會誤觸發
                binding.xrefreshLayout.setOnBackgroundClickListener(null)
                binding.chatKeyboardLayout.setRichMenuGridCount(2).setOnItemClickListener(
                    null,
                    gridMenus,
                    Lists.newArrayList(),
                    object : BottomRichMeunAdapter.OnItemClickListener {
                        override fun onClick(
                            msg: MessageEntity?,
                            menu: RichMenuBottom?,
                            position: Int
                        ) {
                            actionStatus = ActionStatus.SCROLL
                            if (position == 1) {
                                for (m in chatViewModel.mainMessageData) {
                                    m.isDelete = false
                                }
                                chatViewModel.isObserverKeyboard = false
                                binding.chatKeyboardLayout.showKeyboard()
                            } else if (position == 0) {
                                // EVAN_REFACTOR: 2019-09-09 選取後邏輯過濾。
                                val messages: MutableList<MessageEntity> = Lists.newArrayList()
                                messageListAdapter?.let {
                                    it.currentList.forEach {
                                        if (it.isDelete == true) {
                                            messages.add(it)
                                        }
                                    }
                                }
                                if (messages.isEmpty()) {
                                    Toast
                                        .makeText(
                                            this@ChatNormalActivity,
                                            getString(R.string.text_at_least_select_one_message),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    return
                                }

                                when (type) {
                                    RichMenuBottom.MULTI_TRANSPOND -> executeTranspond(messages)
                                    RichMenuBottom.MULTI_COPY -> executeCopy(messages)
                                    RichMenuBottom.TASK -> executeTask(messages, "", false)
                                    else -> {}
                                }
                                for (m in chatViewModel.mainMessageData) {
                                    m.isDelete = false
                                }
                                chatViewModel.isObserverKeyboard = false
                                binding.chatKeyboardLayout.showKeyboard()
                            }

                            binding.xrefreshLayout.setOnBackgroundClickListener(this@ChatNormalActivity)
                            messageListAdapter?.let {
                                it.setAdapterMode(MessageAdapterMode.DEFAULT)
                            }
//                            binding.messageRV.setAdapterMode(MessageAdapterMode.DEFAULT)
                        }

                        override fun onCancle() {
                            actionStatus = ActionStatus.SCROLL
                            messageListAdapter?.let {
                                it.setAdapterMode(MessageAdapterMode.DEFAULT)
                            }
//                            binding.messageRV.setAdapterMode(MessageAdapterMode.DEFAULT)
                        }
                    },
                    null
                )

                binding.chatKeyboardLayout.hideKeyboard()
            }

            OpenBottomRichMeunType.DIRECT_EXECUTION -> {}
        }
    }

    /**
     * 執行多筆刪除
     * 完成進階選單刪除多筆信息邏輯。
     * date 2019/09/10
     */
    private fun executeDelete(messages: MessageEntity) {
        AlertView
            .Builder()
            .setContext(this)
            .setStyle(AlertView.Style.Alert)
            .setMessage(getString(R.string.text_only_delete_message_from_yourself))
            .setOthers(arrayOf(getString(R.string.cancel), getString(R.string.text_delete)))
            .setOnItemClickListener { o: Any?, position: Int ->
                if (position == 1) {
                    chatViewModel.deleteMessages(messages)
                }
            }.build()
            .setCancelable(true)
            .show()
    }

    /**
     * 執行多筆轉發
     * 完成多筆刪除邏輯。
     * date 2019/09/10
     */
    private fun executeTranspond(messages: List<MessageEntity>) {
        chatViewModel.roomEntity?.let {
            ActivityTransitionsControl.toTransfer(
                this,
                messages,
                Lists.newArrayList(
                    it.id
                )
            ) { intent: Intent, s: String? ->
                intent.putExtra(
                    BundleKey.ROOM_ID.key(),
                    it.id
                )

                start(this, intent)
            }
        }
    }

    /**
     * 執行分享邏輯
     * date 2019/09/10
     */
    private fun executeShare(message: MessageEntity) {
        if (MessageType.AT_or_TEXT_or_IMAGE_or_VIDDO.contains(message.type)) {
            when (val content = message.content()) {
                is VideoContent -> {
                    shareVideo(content)
                }

                is TextContent -> {
                    shareText(content)
                }

                is ImageContent -> {
                    val entity = MessageReference.findById(message.id)
                    entity?.let {
                        shareImage(it.content() as ImageContent)
                    }
                }

                is AtContent -> {
                    shareAt(content)
                }
            }
        }
        actionStatus = ActionStatus.SCROLL
    }

    fun showErrorToast(errorMessage: String) =
        CoroutineScope(Dispatchers.Main).launch {
            ToastUtils.showToast(this@ChatNormalActivity, errorMessage)
        }

    /**
     * 可分享標註訊息，且格式文字
     */
    private fun shareAt(atContent: AtContent) {
        chatViewModel.roomEntity?.let {
            val sendIntent = Intent()
            var content = ""
            try {
                val ceMentions = atContent.mentionContents
                val ssb = AtMatcherHelper.matcherAtUsers("@", ceMentions, it.membersTable)
                content = ssb.toString()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            if (Strings.isNullOrEmpty(content)) {
                showErrorToast(getString(R.string.text_marked_message_format_error))
                return
            }
            sendIntent.putExtra(BundleKey.ROOM_ID.key(), it.id)
            sendIntent.setAction(Intent.ACTION_SEND)
            sendIntent.setType("text/plain")
            sendIntent.putExtra(Intent.EXTRA_TEXT, content)
            start(this, sendIntent)
        }
    }

    /**
     * 分享視頻到外部App
     * version 1.10.0 待定
     */
    private fun shareVideo(videoContent: VideoContent) {
        chatViewModel.roomEntity?.let {
            val sendIntent = Intent()
            sendIntent.putExtra(BundleKey.ROOM_ID.key(), it.id)
            sendIntent.setAction(Intent.ACTION_SEND)
            sendIntent.setType("video/mp4")
            sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(videoContent.android_local_path))
            start(this, sendIntent)
        }
    }

    /**
     * 分享 文字訊息
     */
    private fun shareText(textContent: TextContent) {
        chatViewModel.roomEntity?.let {
            val sendIntent = Intent()
            sendIntent.putExtra(BundleKey.ROOM_ID.key(), it.id)
            sendIntent.setAction(Intent.ACTION_SEND)
            sendIntent.setType("text/plain")
            sendIntent.putExtra(Intent.EXTRA_TEXT, textContent.text)
            start(this, sendIntent)
        }
    }

    /**
     * 分享 圖片訊息
     */
    private fun shareImage(imageContent: ImageContent) =
        CoroutineScope(Dispatchers.IO).launch {
            showLoadingView()
            chatViewModel.roomEntity?.let { room ->
                val path = imageContent.url
                if (URLUtil.isValidUrl(path)) {
                    try {
                        val bitmap =
                            Glide
                                .with(this@ChatNormalActivity)
                                .asBitmap()
                                .load(path)
                                .submit()
                                .get()
                        bitmap?.let { b ->
                            val filename = "Image_${System.currentTimeMillis()}.jpg"
                            val values =
                                ContentValues().apply {
                                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                                    }
                                }
                            val uri =
                                contentResolver.insert(
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    values
                                )
                            uri?.let {
                                contentResolver.openOutputStream(it)?.use { outputStream ->
                                    b.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                                }

                                val sendIntent =
                                    Intent().apply {
                                        action = Intent.ACTION_SEND
                                        type = "image/*"
                                        putExtra(BundleKey.ROOM_ID.key(), room.id)
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                start(this@ChatNormalActivity, sendIntent)
                            } ?: run {
                                showErrorToast(getString(R.string.text_share_media_failure))
                            }
                        } ?: run {
                            showErrorToast(getString(R.string.text_share_media_failure))
                        }
                    } catch (e: Exception) {
                        showErrorToast(getString(R.string.api_http_failure))
                    }
                } else {
                    val imageEntity = DaVinci.with(this@ChatNormalActivity).imageLoader.getImage(path)
                    imageEntity?.let { entity ->
                        val filename = "Image_${System.currentTimeMillis()}.jpg"
                        val values =
                            ContentValues().apply {
                                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                                }
                            }
                        val uri =
                            contentResolver.insert(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                values
                            )
                        uri?.let {
                            contentResolver.openOutputStream(it)?.use { outputStream ->
                                entity.bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                            }

                            val sendIntent =
                                Intent().apply {
                                    action = Intent.ACTION_SEND
                                    type = "image/*"
                                    putExtra(BundleKey.ROOM_ID.key(), room.id)
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                            start(this@ChatNormalActivity, sendIntent)
                        } ?: run {
                            showErrorToast(getString(R.string.text_share_media_failure))
                        }
                    } ?: run {
                        showErrorToast(getString(R.string.api_http_failure))
                    }
                }
            }
            stopIosProgressBar()
        }

    private fun stopIosProgressBar() =
        CoroutineScope(Dispatchers.Main).launch {
            progressBar?.let {
                if (it.isShowing) {
                    it.dismiss()
                }
            }
        }

    /**
     * 執行回復邏輯(主題聊天室)
     * date 2019/09/10
     */
    private fun executeReply(message: MessageEntity) {
        isReply = true
        this.themeId = ""
        binding.themeMRV.setMaxHeight(setupDefaultThemeHeight(2.0 / 3.0))
        binding.chatKeyboardLayout.hideBottomPop()
        binding.themeMRV.visibility = View.VISIBLE
        chatViewModel.isThemeOpen = true
        chatViewModel.themeMessage = binding.themeMRV.themeData
        chatViewModel.nearMessage = binding.themeMRV.nearData
        this.themeId = if (!Strings.isNullOrEmpty(message.themeId)) message.themeId else message.id
        showThemeView(themeId)
        actionStatus = ActionStatus.SCROLL
    }

    /**
     * 執行多筆複製。
     * 複製以空格區分，並以信息時間排序
     * date 2019/09/10
     */
    private fun executeCopy(messages: List<MessageEntity>) {
        chatViewModel.roomEntity?.let {
            sortMessageByDate(messages, SortType.ASC)
            val builder = StringBuilder()
            for (m in messages) {
                if (MessageType.AT_or_TEXT.contains(m.type)) {
                    val content: IMessageContent<*> = m.content()
                    if (content is AtContent) {
                        try {
                            val ceMentions = content.mentionContents
                            val ssb =
                                AtMatcherHelper.matcherAtUsers(
                                    "@",
                                    ceMentions,
                                    it.membersTable
                                )
                            builder.append(ssb).append(" ")
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    if (content is TextContent) {
                        builder.append(content.text).append(" ")
                    }
                }
            }

            if (builder.isNotEmpty()) {
                builder.deleteCharAt(builder.length - 1)
            }

            val cmb = getSystemService(Activity.CLIPBOARD_SERVICE) as ClipboardManager
            cmb.text = builder.toString()
            Toast
                .makeText(this, getString(R.string.warning_copied), Toast.LENGTH_SHORT)
                .show()
            actionStatus = ActionStatus.SCROLL
        }
    }

    /**
     * 依照時間排序信息
     * date 2019/09/10
     */
    private fun sortMessageByDate(
        messages: List<MessageEntity>,
        type: SortType
    ) {
        Collections.sort(
            messages
        ) { t1: MessageEntity, t2: MessageEntity ->
            if (SortType.ASC == type) {
                return@sort Longs.compare(
                    t1.sendTime,
                    t2.sendTime
                )
            } else {
                return@sort Longs.compare(
                    t2.sendTime,
                    t1.sendTime
                )
            }
        }
    }

    /**
     * 關閉截圖功能
     */
    private fun clearScreenshotsFunction() =
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                binding.chatKeyboardLayout.showKeyboard()
                showToolBar(true)
                setThemeStyle()
                binding.xrefreshLayout.setOnBackgroundClickListener(this@ChatNormalActivity)
            }
            for (m in chatViewModel.mainMessageData) {
                m.isShowSelection = false
            }
            screenShotData.clear()
            chatViewModel.isObserverKeyboard = false
            messageListAdapter?.setAdapterMode(MessageAdapterMode.DEFAULT)
            messageListAdapter?.switchAnonymousMode(false)
        }

    /**
     * ScreenshotsPreview
     * 依照選取範圍截圖
     */
    fun executeScreenshots(
        isTask: Boolean,
        action: RichMenuBottom
    ) = CoroutineScope(Dispatchers.IO).launch {
        if (screenShotData.isEmpty()) {
            return@launch
        }
        showLoadingView(R.string.wording_processing)

        // 獲取item的數量
        // recycler的完整高度 用於創建bitmap時使用
        // 獲取最大可用內存
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        // 使用1/8的緩存
        val cacheSize = maxMemory / 8
        // 把每個item的繪圖緩存存儲在LruCache中
        val bitmapCache = LruCache<Int, Bitmap>(cacheSize)
        val indexList: MutableList<Int> = Lists.newArrayList()
        screenShotData.forEach(
            Consumer { selectedMessage: MessageEntity ->
                indexList.add(chatViewModel.mainMessageData.indexOf(selectedMessage))
            }
        )
        var height: Int = 0
        var count = 0
        for (i in indexList) {
            val holder = messageListAdapter?.getHolder(binding.messageRV, i)
            holder?.let {
                it.apply {
                    itemView.isDrawingCacheEnabled = true
                    itemView.buildDrawingCache()
                    setIsRecyclable(false)
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    // 測量
                    it.itemView.measure(
                        View.MeasureSpec.makeMeasureSpec(binding.messageRV.width, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                    )
                    // 佈局
                    it.itemView.layout(0, 0, it.itemView.measuredWidth, it.itemView.measuredHeight)
                    it.itemView.isDrawingCacheEnabled = true
                    it.itemView.buildDrawingCache()
                    val cache = it.itemView.drawingCache
                    cache?.let {
                        bitmapCache.put(i, it)
                    }
                    // 獲取itemView的實際高度並累加
                    height += it.itemView.measuredHeight
                    count++
                    if (count == indexList.size) {
                        val snapShot = bitmapCache.snapshot()
                        var top = 0
                        val screenBitmap =
                            Bitmap.createBitmap(
                                binding.messageRV.measuredWidth,
                                height,
                                Bitmap.Config.RGB_565
                            )
                        // 創建一個canvas畫板
                        val canvas = Canvas(screenBitmap)
                        canvas.drawColor(Color.WHITE)
                        // 當前bitmap的高度
                        val paint = Paint()
                        snapShot.forEach { index, bitmap ->
                            canvas.drawBitmap(bitmap, 0f, top.toFloat(), paint)
                            top += bitmap.height
                        }
                        hideLoadingView()
                        saveScreenshots(screenBitmap, action)
                    }
                }, 100L)
            }
        }
    }

    private fun showLoadingView() =
        CoroutineScope(Dispatchers.Main).launch {
            progressBar =
                IosProgressBar.show(
                    this@ChatNormalActivity,
                    getString(R.string.wording_loading),
                    true,
                    false
                ) { }
        }

    fun showLoadingView(resId: Int) =
        CoroutineScope(Dispatchers.Main).launch {
            progressBar =
                IosProgressBar.show(
                    this@ChatNormalActivity,
                    resId,
                    true,
                    false
                ) { dialog: DialogInterface? -> }
        }

    fun hideLoadingView() =
        CoroutineScope(Dispatchers.Main).launch {
            progressBar?.let {
                if (it.isShowing) {
                    it.dismiss()
                }
            }
        }

    private fun saveScreenshots(
        bitmap: Bitmap,
        action: RichMenuBottom
    ) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val file = BitmapHelper.bitmapToFile(bitmap, externalCacheDir!!.path, "screenshots.jpg")
            when (action) {
                RichMenuBottom.SAVE -> {
                    saveScreenshots(file.path)
                }

                RichMenuBottom.SHARE, RichMenuBottom.PREVIEW -> {
                    chatViewModel.roomEntity?.let {
                        val bundle =
                            bundleOf(
                                BundleKey.ACTION.key() to RichMenuBottom.SAVE.name,
                                BundleKey.ROOM_ID.key() to it.id,
                                BundleKey.FILE_PATH.key() to file.path
                            )
                        IntentUtil.startIntent(this@ChatNormalActivity, ScreenshotsPreviewActivity::class.java, bundle)
                    }
                }

                RichMenuBottom.CONFIRM -> {
                    executeTask(Lists.newArrayList(), file.path, false)
                }

                else -> {
                    // nothing
                }
            }
            clearScreenshotsFunction()
        } catch (e: Exception) {
        }
    }

    /**
     * 儲存截圖
     */
    private fun saveScreenshots(filePath: String) =
        CoroutineScope(Dispatchers.IO).launch {
            val screenshots = BitmapHelper.getBitmapFromLocal(filePath)
            var saveImagePath: String? = null
            val imageFileName = "JPEG_" + "down" + System.currentTimeMillis() + ".jpg"
            val storageDir = File(DownloadUtil.downloadImageDir)
            var success = true
            if (!storageDir.exists()) {
                success = storageDir.mkdirs()
            }
            if (success) {
                val imageFile = File(storageDir, imageFileName)
                saveImagePath = imageFile.absolutePath
                try {
                    val fout: OutputStream = FileOutputStream(imageFile)
                    screenshots.compress(Bitmap.CompressFormat.JPEG, 100, fout)
                    fout.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                val f = File(saveImagePath) // 權限問題會不能使用
                val contentUri = Uri.fromFile(f)
                mediaScanIntent.setData(contentUri)
                sendBroadcast(mediaScanIntent)
                withContext(Dispatchers.Main) {
                    Toast
                        .makeText(
                            this@ChatNormalActivity,
                            String.format(getString(R.string.bruce_photo_save), saveImagePath),
                            Toast.LENGTH_SHORT
                        ).show()
                }
            }
        }

    /**
     * 執行任務鍵
     */
    fun executeTask(
        entities: List<MessageEntity>,
        path: String?,
        isToolbarSource: Boolean
    ) {
        chatViewModel.roomEntity?.let {
            // api 取得任務列表，成功show list，失敗 show toast
            val messages: MutableList<MessageEntity> = Lists.newArrayList()
            // 過濾物件訊息
            for (entity in entities) {
                if (MessageType.BUSINESS != entity.type) {
                    messages.add(entity)
                }
            }
            val messageIds = arrayOfNulls<String>(messages.size)
            for (i in messages.indices) {
                messageIds[i] = messages[i].id
            }

            if (ChatRoomType.discuss == it.type) {
                businessRelationOrCreate(
                    if (isToolbarSource) BusinessTaskAction.CREATE_BUSINESS_RELATIONAL else BusinessTaskAction.CREATE_BUSINESS,
                    null,
                    messageIds,
                    path
                )
            } else if (ChatRoomType.services == it.type) {
                businessRelationOrCreate(BusinessTaskAction.CREATE_BUSINESS, null, messageIds, path)
            } else {
                val action =
                    if ((
                            ChatRoomType.FRIEND_or_DISCUSS.contains(
                                it.type
                            ) &&
                                isToolbarSource
                        )
                    ) {
                        BusinessTaskAction.CREATE_BUSINESS_RELATIONAL
                    } else {
                        BusinessTaskAction.CREATE_BUSINESS
                    }
                businessRelationOrCreate(action, null, messageIds, path)
            }
        }
    }

    /**
     * 執行回復邏輯(主題聊天室)
     * date 2019/09/10
     */
    private fun executeTodo(message: MessageEntity) {
        var title = getString(R.string.text_new_message)
        chatViewModel.roomEntity?.let {
            title =
                when (message.type) {
                    MessageType.AT ->
                        AtMatcherHelper
                            .matcherAtUsers(
                                "@",
                                (message.content() as AtContent).mentionContents,
                                it.membersTable
                            ).toString()

                    else -> message.content().simpleContent()
                }
        }
        val now = System.currentTimeMillis()

        val entity =
            TodoEntity
                .Builder()
                .title(title)
                .status(TodoStatus.PROGRESS)
                .processStatus(ProcessStatus.UN_SYNC_CREATE)
                .openClock(EnableType.N.isStatus)
                .remindTime(-1)
                .createTime(now)
                .updateTime(now)
                .roomId(message.roomId!!)
                .messageId(message.id!!)
                .userId(selfUserId)
                .build()

        val todoSettingDialog =
            TodoSettingDialog(
                this,
                entity.roomId,
                entity.id,
                Lists.newArrayList(entity)
            )
        todoSettingDialog.setRemindListener(setRemindListener)
        todoSettingDialog.show()
        actionStatus = ActionStatus.SCROLL
    }

    private val setRemindListener =
        OnSetRemindTime { isRemind: Boolean ->
            if (isRemind) {
                if (!Settings.canDrawOverlays(this)) {
                    val today = DateTime()
                    val setNoticeDate =
                        DateTime(
                            TokenPref.getInstance(this).remindNotice
                        )

                    if (today.isAfter(setNoticeDate)) {
                        val others =
                            arrayOf(
                                getString(R.string.cancel),
                                getString(R.string.text_sure)
                            )
                        AlertView
                            .Builder()
                            .setContext(this)
                            .setStyle(AlertView.Style.Alert)
                            .setMessage(getString(R.string.text_tip_for_float_window_permission))
                            .setOthers(others)
                            .setOnItemClickListener { o: Any?, pos: Int ->
                                if (pos == 1) {
                                    val intent2 = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                                    startActivityForResult(
                                        intent2,
                                        TodoListFragment.REQUEST_CODE
                                    )
                                } else {
                                    val dt = DateTime()
                                    val forPattern = DateTimeFormat.forPattern("yyyy-MM-dd")
                                    val dtp =
                                        forPattern.parseDateTime(
                                            dt.plusDays(7).toString("yyyy-MM-dd")
                                        )
                                    // 拒絕給予權限時，紀錄時間，一週後再問一次
                                    TokenPref.getInstance(this).remindNotice = dtp.millis
                                    Toast
                                        .makeText(
                                            this,
                                            getString(R.string.text_tip2_for_float_window_permission),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                }
                            }.build()
                            .setCancelable(true)
                            .show()
                    } else {
                        Toast
                            .makeText(
                                this,
                                getString(R.string.text_tip3_for_float_window_permission),
                                Toast.LENGTH_SHORT
                            ).show()
                    }
                }
            }
        }

    /**
     * 關連物件或建立物件聊天室
     * EVAN_FLAG 2020-03-30 (1.10.0) 物件聊天室相關
     */
    fun businessRelationOrCreate(
        action: BusinessTaskAction,
        businessContent: BusinessContent?,
        messageIds: Array<String?>?,
        path: String?
    ) {
        chatViewModel.roomEntity?.let {
            ActivityManager.addActivity(this)
            if (businessContent != null) {
                businessContent.code = BusinessCode.TASK
                businessContent.avatarUrl = ""
            }

            if (BusinessTaskAction.CREATE_BUSINESS == action) {
                ActivityTransitionsControl.navigateToCreateBusinessTask(
                    this,
                    action.name,
                    it,
                    businessContent,
                    messageIds,
                    path
                ) { intent: Intent?, s: String? ->
                    start(
                        this,
                        intent!!
                    )
                }
            } else if (BusinessTaskAction.CREATE_BUSINESS_ROOM == action) {
                ActivityTransitionsControl.navigateToCreateBusinessTask(
                    this,
                    action.name,
                    it,
                    businessContent,
                    messageIds,
                    path
                ) { intent: Intent?, s: String? ->
                    start(
                        this,
                        intent!!
                    )
                }
            } else if (BusinessTaskAction.CREATE_SERVICE_BUSINESS_ROOM == action) {
                ActivityTransitionsControl.navigateToCreateBusinessTask(
                    this,
                    action.name,
                    it,
                    businessContent,
                    messageIds,
                    path
                ) { intent: Intent?, s: String? ->
                    start(
                        this,
                        intent!!
                    )
                }
            } else if (BusinessTaskAction.CREATE_BUSINESS_RELATIONAL == action) {
                ActivityTransitionsControl.navigateToCreateBusinessTask(
                    this,
                    action.name,
                    it,
                    businessContent,
                    messageIds,
                    path
                ) { intent: Intent?, s: String? ->
                    start(
                        this,
                        intent!!
                    )
                }
            }
        }
    }

    /**
     * 執行多筆收回
     * 完成多筆收回邏輯。
     * date 2019/09/10
     */

    private fun executeRecover(messages: MutableList<MessageEntity>?) {
        if (messages == null) {
            return
        }

        // EVAN_FLAG 2019-12-16 (1.8.0) William 額外需求，去除主題聊天是訊息，禁止回收
        var hasThemeMessage = false
        val iterator = messages.iterator()
        while (iterator.hasNext()) {
            val m = iterator.next()
            if (checkIsThemeMessage(m)) {
                iterator.remove()
                Toast
                    .makeText(
                        this,
                        getString(R.string.text_theme_message_can_not_be_retracted),
                        Toast.LENGTH_SHORT
                    ).show()
                continue
            }

            if (!Strings.isNullOrEmpty(m.nearMessageId)) {
                iterator.remove()
                hasThemeMessage = true
            }
        }

        if (hasThemeMessage) {
            Toast
                .makeText(
                    this,
                    getString(R.string.text_theme_message_can_not_be_retracted),
                    Toast.LENGTH_SHORT
                ).show()
        }

        if (messages.isEmpty()) {
            return
        }
        showLoadingView(R.string.text_retracting)
        chatViewModel.executeRetractCount = 0
        for (m in messages) {
            chatViewModel.doMessageRetract(m, messages.size)
        }
    }

    /**
     * 檢查是否為主題訊息
     */
    private fun checkIsThemeMessage(msg: MessageEntity): Boolean {
        for (mainMessageDatum in chatViewModel.mainMessageData) {
            if (msg.id == mainMessageDatum.themeId) {
                return true
            }
        }
        return false
    }

    override fun onBackgroundClick(refreshLayout: XRefreshLayout?) {
        actionStatus = ActionStatus.SCROLL
    }

    fun showIsNotMemberMessage(errorMessage: String) {
        ToastUtils.showCenterToast(
            this,
            errorMessage
        )
        if (errorMessage != getString(R.string.api_there_no_internet_connection)) {
            chatViewModel.roomEntity?.let {
                chatViewModel.deletedRoom(it.id)
            }
        }
    }

    fun updateFacebookStatus(message: MessageEntity) {
        if (chatViewModel.isThemeOpen) {
            binding.themeMRV.notifyChange(message)
        }
    }

    fun stopRefresh() {
        binding.xrefreshLayout.completeRefresh()
    }

    fun bindingBusiness(
        roomId: String?,
        content: BusinessContent,
        isSendLastMessage: Boolean
    ) {
        val status = ChatRoomReference.getInstance().updateBusinessContent(roomId, content)
        if (status) {
            chatViewModel.roomEntity?.let {
                it.businessId = content.id
                it.businessCode = content.code
                it.businessName = content.name
            }
        }

        setThemeStyle()
        // KeyboardLayout themeStyle
        binding.chatKeyboardLayout.setThemeStyle(RoomThemeStyle.BUSINESS)
        Toast
            .makeText(
                this,
                getString(R.string.text_chat_room_connecting_mission),
                Toast.LENGTH_SHORT
            ).show()
        if (isSendLastMessage) {
            chatViewModel.sendBusiness(content)
        }
    }

    private fun setChatDisable(text: Int) {
        binding.chatKeyboardLayout.setChatDisable(getString(text))
    }

    private fun sendMessage(sendData: SendData) {
        if (sendData.type == MessageType.TEXT && Strings.isNullOrEmpty(sendData.content.trim { it <= ' ' })) {
            Toast
                .makeText(
                    this,
                    getString(R.string.text_can_not_send_empty_message),
                    Toast.LENGTH_SHORT
                ).show()
            Log.e("TAG", "onSendBtnClick sendData 不可發送空白訊息")
            return
        }
        // 判斷是否有機器人服務，是否強直接手
        chatViewModel.roomEntity?.let {
            chatViewModel.sendButtonClicked(binding.chatKeyboardLayout.inputHET.textData)
        }
    }

    // 子聊天室 根據設定是否自動關閉
    fun setThemeOpen(isThemeOpen: Boolean) {
        if (chatViewModel.isThemeOpen) {
            if (TokenPref.getInstance(this).isAutoCloseSubChat) {
                doThemeCloseAction()
                chatViewModel.isThemeOpen = isThemeOpen
            } else {
                executeReply(binding.themeMRV.nearData)
            }
        }
    }

    fun doThemeCloseAction() {
        binding.expandIV.tag = null
        binding.themeMRV.setMaxHeight(setupDefaultThemeHeight(2.0 / 3.0))
        binding.themeMRV.clearData()
        binding.themeMRV.visibility = View.GONE
//        binding.facebookGroup.visibility = View.GONE
        chatViewModel.isThemeOpen = false
        chatViewModel.themeMessage = null
        chatViewModel.nearMessage = null
        this.themeId = ""
        isReply = false
    }

    fun dismissSendVideoProgress() {
        if (sendVideoProgress != null && sendVideoProgress?.isShowing == true) {
            sendVideoProgress?.dismiss()
        }
    }

    fun updateMsgStatus(
        messageId: String?,
        status: MessageStatus
    ) {
        messageId?.let {
            val index =
                chatViewModel.mainMessageData.indexOf(MessageEntity.Builder().id(it).build())
            if (index < 0) {
                return
            }
            chatViewModel.mainMessageData[index].status = status
            refreshListView()
        }
    }

    fun refreshListView() {
        messageListAdapter?.let {
            it.notifyItemRangeChanged(0, it.itemCount)
        }
    }

    /**
     * 更新進度條UI
     */
    fun updateMsgProgress(
        messageId: String,
        progress: Int
    ) {
        val index =
            chatViewModel.mainMessageData.indexOf(MessageEntity.Builder().id(messageId).build())
        if (index < 0) {
            return
        }

        val message = chatViewModel.mainMessageData[index]
        if (MessageType.IMAGE_or_VIDEO_or_FILE.contains(message.type)) {
            val content: IMessageContent<*> = message.content()
            if (content is ImageContent) {
                content.progress = progress.toString() + ""
            }
            if (content is FileContent) {
                content.progress = progress.toString() + ""
            }
            if (content is VideoContent) {
                content.progress = progress.toString() + ""
            }

            message.content = content.toStringContent()
//            binding.messageRV.refreshData(index, message)
        }
    }

    fun updateSendVideoProgress() {
        if (sendFileSize == 0) {
            dismissSendVideoProgress()
            addProgressValue = 0
            return
        }
        if (sendVideoProgress != null) {
            val progressBar = sendVideoProgress!!.findViewById<ProgressBar>(R.id.progress_bar)
            if (addProgressValue == 0) {
                addProgressValue = 100 / sendFileSize
            }
            progressBar.progress += addProgressValue
            sendFileSize--
            if (sendFileSize == 0) {
                dismissSendVideoProgress()
                addProgressValue = 0
            }
        } else {
            dismissSendVideoProgress()
        }
    }

    fun updateSendVideoProgress(progress: Int) {
        if (isSendSingleFile) {
            sendVideoProgress?.let {
                sendVideoProgressBinding?.let {
                    it.progressBar.let { bar ->
                        bar.progress = progress
                        if (progress >= 100) {
                            sendFileSize = 0
                            dismissSendVideoProgress()
                        }
                    }
                }
            }
        }
    }

    fun showSendVideoProgress(message: String) {
        if (sendFileSize == 0) return
        val screenWidth = UiHelper.getDisplayWidth(this)
        if (sendVideoProgressBinding == null) {
            val params =
                FrameLayout.LayoutParams(
                    screenWidth / 3,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                )
            sendVideoProgressBinding =
                ProgressSendVideoBinding.inflate(LayoutInflater.from(this)).apply {
                    root.layoutParams = params
                }
        }
        if (sendVideoProgress == null) {
            sendVideoProgress = Dialog(this)
            sendVideoProgress!!.setContentView(sendVideoProgressBinding!!.root)
        } else {
            if (sendVideoProgress?.isShowing == false) {
                sendVideoProgressBinding!!.progressBar.progress = 0
            }
        }
        sendVideoProgress!!.setCancelable(false)
        sendVideoProgressBinding!!.message.text = message
        if (sendVideoProgress?.isShowing == false) {
            sendVideoProgress?.show()
        }
    }

    fun showNoMoreMessage() {
//        chatRoomMessageSearchAdapter.setData(chatRoomMessageSearchAdapter.getData(), keyWord, chatRoomMemberTable)
    }

    // 如果沒有管理者，則全部人都顯示
    // 有管理者，只有管理者顯示
    private fun filterToShowNoOwnerNotify(groupRoomManager: List<ChatRoomMemberResponse>?) {
        chatViewModel.roomEntity?.let {
            if (it.type != ChatRoomType.group) return
            if (it.ownerId.isNotEmpty()) return
            if (groupRoomManager == null) return
            if (groupRoomManager.isEmpty()) {
                showNoOwnerNotify()
            } else {
                groupRoomManager.forEach(
                    Consumer { member: ChatRoomMemberResponse ->
                        if (member.memberId == selfUserId) {
                            showNoOwnerNotify()
                        }
                    }
                )
            }
        }
    }

    private fun showNoOwnerNotify() {
        binding.clNotifyNoOwner.visibility = View.VISIBLE
        binding.tvNoOnwerText.movementMethod = LinkMovementMethod.getInstance()
        binding.tvNoOnwerText.text =
            KeyWordHelper.matcherKeys(
                -0xff8901,
                getString(R.string.group_room_no_owner),
                getString(R.string.text_become_group_owner)
            ) {
                chatViewModel.roomEntity?.let {
                    chatViewModel.becomeOwner(
                        it.id
                    )
                }
            }
    }

    fun transferModeDisplay(messages: List<MessageEntity>) {
        chatViewModel.mainMessageData.clear()
        for (msg in messages) {
            if (msg.sendNum!! > 0) {
                msg.status = MessageStatus.SUCCESS
            }
            if (msg.receivedNum!! > 0) {
                msg.status = MessageStatus.RECEIVED
            }
            if (msg.readedNum!! > 0) {
                msg.status = MessageStatus.READ
            }
            chatViewModel.mainMessageData.add(msg)
        }
//        binding.messageRV.refreshToBottom(actionStatus.status())
    }

    private fun setMentionSelectList() {
        chatViewModel.roomEntity?.let {
            val cellHeight = UiHelper.dip2px(this@ChatNormalActivity, 46f)
            mentionSelectAdapter =
                MentionSelectAdapter(this@ChatNormalActivity).setUserProfiles(it.membersLinkedList).setKeyword("")
            if (it.membersLinkedList.size >= 8) {
                val params = binding.mentionMHRV.layoutParams
                params.height = cellHeight * 4
            }
            binding.mentionMHRV.apply {
                setMaxHeight(cellHeight * 4)
                setBackgroundColor(Color.WHITE)
                layoutManager = GridLayoutManager(this@ChatNormalActivity, 2)
                addItemDecoration(GridItemDecoration(Color.WHITE))
                itemAnimator = DefaultItemAnimator()
                setHasFixedSize(false)
            }
        }

        binding.chatKeyboardLayout.setOnMentionFeatureListener(
            object :
                OnMentionFeatureListener {
                override fun onShowMention(
                    editText: HadEditText,
                    isMultiSelect: Boolean,
                    keyword: String
                ) {
                    binding.mentionMHRV.visibility = View.VISIBLE
                    binding.mentionMHRV.adapter =
                        mentionSelectAdapter
                            .setKeyword(keyword)
                            .setOnSelectItemListener { ecUserProfile: UserProfileEntity?, _: Int, needCalculatePosition: Boolean ->
                                editText.appendMentionSelect(
                                    ecUserProfile,
                                    true,
                                    needCalculatePosition
                                )
                            }
                    if (!isMultiSelect) {
                        mentionSelectAdapter.setKeyword(keyword).reset()
                    } else {
                        mentionSelectAdapter.setKeyword(keyword).refreshData()
                    }
                    binding.mentionMHRV.measure(0, 0)
                }

                override fun onHideMention(
                    editText: HadEditText?,
                    keyword: String
                ) {
                    mentionSelectAdapter.setKeyword(keyword)
                    binding.mentionMHRV.visibility = View.GONE
                    binding.mentionMHRV.adapter = null
                }

                override fun onNotifyMentionDataChanged(users: LinkedList<UserProfileEntity>) {
                    mentionSelectAdapter.setUserProfiles(users).refreshData()
                }
            }
        )
    }

    private fun setup() =
        CoroutineScope(Dispatchers.Main).launch {
            chatViewModel.roomEntity?.let {
                chatViewModel.queryMemberIsBlock()
            }
        }

    override fun onRefresh() {
        chatViewModel.loadMoreMessage()
    }

    inner class KeyBoardBarListener : OnChatKeyBoardListener {
        override fun onSendBtnClick(
            sendData: SendData,
            enableSend: Boolean
        ) {
            chatViewModel.roomEntity?.let {
                sendMessage(sendData)
            } ?: run {
                chatViewModel.doAddContact(userId, userName, sendData)
            }
        }

        override fun onRecordingSendAction(
            path: String,
            duration: Int
        ) {
            chatViewModel.sendVoice(path, duration)
        }

        override fun onRecordingStartAction() {
            if (ContextCompat.checkSelfPermission(
                    this@ChatNormalActivity,
                    permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                if (checkMicAvailable()) {
                    showRecordingWindow()
                }
            } else {
                recordPermissionResult.launch(permission.RECORD_AUDIO)
            }
        }

        override fun onUserDefEmoticonClicked(
            tag: String,
            packageId: String
        ) {
//            presenter.sendSticker(tag, packageId);
        }

        override fun onStickerClicked(
            stickerId: String,
            packageId: String
        ) {
            chatViewModel.sendSticker(stickerId, packageId)
        }

        override fun onOpenVideo() {
            binding.funMedia.visibility = View.GONE
            isActivityForResult = true
            binding.chatKeyboardLayout.clearIconState()

            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            try {
                createVideoFile()?.let {
                    videoFile = it
                    val uri =
                        FileProvider.getUriForFile(
                            this@ChatNormalActivity,
                            this@ChatNormalActivity.packageName + ".fileprovider",
                            videoFile
                        )
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                    intent.putExtra(
                        MediaStore.EXTRA_VIDEO_QUALITY,
                        CamcorderProfile.QUALITY_HIGH
                    )
                    sendVideoCaptureResult.launch(intent)
                }
            } catch (error: Exception) {
//                Log.e(TAG, "error=${error.message}")
            }
        }

        /**
         * 打開文件夾列表
         */
        override fun onOpenFolders() {
            binding.funMedia.visibility = View.GONE
            isActivityForResult = true
            binding.chatKeyboardLayout.clearIconState()
            val themeColor = if (isGreenTheme && themeStyle != RoomThemeStyle.SERVICES) resources.getColor(R.color.color_015F57, null) else themeStyle?.mainColor ?: R.color.color_6BC2BA
            sendFileOpenResult.launch(
                Intent(this@ChatNormalActivity, FileExplorerActivity::class.java).putExtra(BundleKey.THEME_COLOR.key(), themeColor)
            )
            this@ChatNormalActivity.overridePendingTransition(
                R.anim.slide_right_in,
                R.anim.slide_right_out
            )
        }

        private fun checkForSelfPermissions(permission: Array<String>): Boolean {
            for (i in permission.indices) {
                if (ContextCompat.checkSelfPermission(
                        this@ChatNormalActivity,
                        permission[i]
                    ) == PackageManager.PERMISSION_DENIED
                ) {
                    return false
                }
            }
            return true
        }

        override fun onOpenCamera() {
            binding.funMedia.visibility = View.GONE
            isActivityForResult = true
            binding.chatKeyboardLayout.clearIconState()

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            try {
                createImageFile()?.let {
                    photoFile = it
                    val uri =
                        FileProvider.getUriForFile(
                            this@ChatNormalActivity,
                            this@ChatNormalActivity.packageName + ".fileprovider",
                            photoFile
                        )
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                    sendImageCaptureResult.launch(intent)
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }

        private val digitalStyle: PictureSelectorStyle
            // set Digital Style
            get() {
                val pictureSelectorStyle = PictureSelectorStyle()
                val blueTitleBarStyle = TitleBarStyle()
                blueTitleBarStyle.titleBackgroundColor =
                    ContextCompat.getColor(this@ChatNormalActivity, if (isGreenTheme) R.color.color_015F57 else R.color.colorPrimary)

                val numberBlueBottomNavBarStyle = BottomNavBarStyle()
                numberBlueBottomNavBarStyle.bottomPreviewNormalTextColor =
                    ContextCompat.getColor(this@ChatNormalActivity, R.color.ps_color_9b)
                numberBlueBottomNavBarStyle.bottomPreviewSelectTextColor =
                    ContextCompat.getColor(this@ChatNormalActivity, if (isGreenTheme) R.color.color_015F57 else R.color.colorPrimary)
                numberBlueBottomNavBarStyle.bottomNarBarBackgroundColor =
                    ContextCompat.getColor(this@ChatNormalActivity, R.color.ps_color_white)
                numberBlueBottomNavBarStyle.bottomSelectNumResources = if (isGreenTheme) R.drawable.bg_media_select_green else R.drawable.album_num_selected
                numberBlueBottomNavBarStyle.bottomEditorTextColor =
                    ContextCompat.getColor(this@ChatNormalActivity, R.color.ps_color_53575e)
                numberBlueBottomNavBarStyle.bottomOriginalTextColor =
                    ContextCompat.getColor(this@ChatNormalActivity, R.color.ps_color_53575e)

                val numberBlueSelectMainStyle = SelectMainStyle()
                numberBlueSelectMainStyle.statusBarColor =
                    ContextCompat.getColor(this@ChatNormalActivity, if (isGreenTheme) R.color.color_015F57 else R.color.colorPrimary)
                numberBlueSelectMainStyle.isSelectNumberStyle = true
                numberBlueSelectMainStyle.isPreviewSelectNumberStyle = true
                numberBlueSelectMainStyle.selectBackground = if (isGreenTheme) R.drawable.album_num_selector_green else R.drawable.album_num_selector
                numberBlueSelectMainStyle.mainListBackgroundColor =
                    ContextCompat.getColor(this@ChatNormalActivity, R.color.ps_color_white)
                numberBlueSelectMainStyle.previewSelectBackground =
                    if (isGreenTheme) R.drawable.album_num_selector_green else R.drawable.album_preview_num_selector

                numberBlueSelectMainStyle.selectNormalTextColor =
                    ContextCompat.getColor(this@ChatNormalActivity, R.color.ps_color_9b)
                numberBlueSelectMainStyle.selectTextColor =
                    ContextCompat.getColor(this@ChatNormalActivity, if (isGreenTheme) R.color.color_015F57 else R.color.colorPrimary)
                numberBlueSelectMainStyle.setSelectText(R.string.ps_completed)

                pictureSelectorStyle.titleBarStyle = blueTitleBarStyle
                pictureSelectorStyle.bottomBarStyle = numberBlueBottomNavBarStyle
                pictureSelectorStyle.selectMainStyle = numberBlueSelectMainStyle
                return pictureSelectorStyle
            }

        // 聊天室圖片選擇器
        override fun onOpenGallery() {
            isActivityForResult = true
            binding.chatKeyboardLayout.clearIconState()

            // 选择照片
            PictureSelector
                .create(this@ChatNormalActivity)
                .openGallery(SelectMimeType.ofAll())
                .setSelectorUIStyle(digitalStyle)
                .setSelectionMode(SelectModeConfig.MULTIPLE)
                .isWithSelectVideoImage(true)
                .setMaxSelectNum(9)
                .setMinSelectNum(0)
                .setMinVideoSelectNum(0)
                .setMaxVideoSelectNum(9)
                .isPreviewVideo(true)
                .isPreviewImage(true)
                .isDisplayCamera(false)
                .isOriginalSkipCompress(false)
                .isGif(true)
                .isOpenClickSound(false)
                .setSelectFilterListener { media: LocalMedia ->
                    if (media.mimeType.contains("video/") && media.mimeType != "video/mp4") {
                        ToastUtils.showToast(
                            this@ChatNormalActivity,
                            getString(R.string.text_video_limit_mp4_format)
                        )
                        return@setSelectFilterListener true
                    }
                    chatViewModel.appointResp?.let { resp ->
                        if (resp.lastFrom != null && resp.lastFrom == ChannelType.LINE) {
                            if (media.mimeType.contains("image/") && media.size >= 10 * FileSizeUnit.MB) {
                                ToastUtils.showToast(
                                    this@ChatNormalActivity,
                                    getString(R.string.text_file_size_limit, 10)
                                )
                                return@setSelectFilterListener true
                            } else if (media.mimeType.contains("video/mp4") && media.size >= 200 * FileSizeUnit.MB) {
                                ToastUtils.showToast(
                                    this@ChatNormalActivity,
                                    getString(R.string.text_file_size_limit, 200)
                                )
                                return@setSelectFilterListener true
                            }
                        } else if (resp.lastFrom != null && resp.lastFrom == ChannelType.FB) {
                            if (media.mimeType.contains("image/") && media.size >= 25 * FileSizeUnit.MB) {
                                ToastUtils.showToast(
                                    this@ChatNormalActivity,
                                    getString(R.string.text_file_size_limit, 25)
                                )
                                return@setSelectFilterListener true
                            } else if (media.mimeType.contains("video/mp4") && media.size >= 25 * FileSizeUnit.MB) {
                                ToastUtils.showToast(
                                    this@ChatNormalActivity,
                                    getString(R.string.text_file_size_limit, 25)
                                )
                                return@setSelectFilterListener true
                            }
                        } else if (resp.lastFrom != null && resp.lastFrom == ChannelType.IG) {
                            if (media.mimeType.contains("image/") && media.size >= 8 * FileSizeUnit.MB) {
                                ToastUtils.showToast(
                                    this@ChatNormalActivity,
                                    getString(R.string.text_file_size_limit, 8)
                                )
                                return@setSelectFilterListener true
                            } else if (media.mimeType.contains("video/mp4") && media.size >= 25 * FileSizeUnit.MB) {
                                ToastUtils.showToast(
                                    this@ChatNormalActivity,
                                    getString(R.string.text_file_size_limit, 25)
                                )
                                return@setSelectFilterListener true
                            }
                        } else if (resp.lastFrom != null && resp.lastFrom == ChannelType.GOOGLE) {
                            if (media.mimeType.contains("image/") && media.size >= 5 * FileSizeUnit.MB) {
                                ToastUtils.showToast(
                                    this@ChatNormalActivity,
                                    getString(R.string.text_file_size_limit, 5)
                                )
                                return@setSelectFilterListener true
                            }
                        }
                    }
                    false
                }.setImageEngine(createGlideEngine())
                .forResult(
                    object : OnResultCallbackListener<LocalMedia> {
                        override fun onResult(result: ArrayList<LocalMedia>) {
                            sendFileSize = result.size
                            isSendSingleFile = sendFileSize == 1
                            result.forEach {
                                if (it.mimeType.contains("image/")) {
                                    // 圖片
                                    chatViewModel.sendImage(isOriginPhoto = true, path = it.realPath)
                                } else if (it.mimeType.contains("video/mp4")) { // 影片
                                    chatViewModel.sendVideo(
                                        VideoSizeFromVideoFile(it.realPath),
                                        false
                                    )
                                }
                            }
                        }

                        override fun onCancel() {
                        }
                    }
                )
        }

        /**
         * 打開表情功能
         */
        override fun onOpenEmoticon() {
            binding.funMedia.visibility = View.GONE
        }

        /**
         * 打開錄音功能
         */
        override fun onOpenRecord() {
            binding.funMedia.visibility = View.GONE
        }

        /**
         * 打開多媒體選擇器
         */
        override fun onOpenMultimediaSelector() {
            binding.funMedia.setType(this@ChatNormalActivity, MultimediaHelper.Type.FILE, themeStyle, -1)
            if (binding.funMedia.isGone) {
                binding.funMedia.visibility = View.VISIBLE
            }
        }

        /**
         * 打開圖片選擇器
         */
        override fun onOpenPhotoSelector(isChange: Boolean) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                for (permission in permissions) {
                    val isPermissionGranted = this@ChatNormalActivity.checkSelfPermission(permission)
                    if (isPermissionGranted == PackageManager.PERMISSION_DENIED) {
                        launcher.launch(
                            arrayOf(
                                Manifest.permission.READ_MEDIA_IMAGES,
                                Manifest.permission.READ_MEDIA_AUDIO,
                                Manifest.permission.READ_MEDIA_VIDEO
                            )
                        )
                        return
                    }
                }
                showFunMedia(isChange)
            } else {
                if (this@ChatNormalActivity.checkSelfPermission(permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    showFunMedia(isChange)
                } else {
                    storagePermissionResult.launch(permission.WRITE_EXTERNAL_STORAGE)
                }
            }
        }

        override fun onOpenConsult() {
        }

        /**
         * 導航到圖片選擇器預覽
         */
        override fun toMediaSelectorPreview(
            isOriginal: Boolean,
            type: String,
            current: String,
            data: TreeMap<String, String>,
            position: Int
        ) {
            mediaPreviewARL.launch(
                Intent(this@ChatNormalActivity, MediaSelectorPreviewActivity::class.java)
                    .putExtra(
                        BundleKey.IS_ORIGINAL.key(),
                        isOriginal
                    ).putExtra(BundleKey.MAX_COUNT.key(), current)
                    .putExtra(BundleKey.TYPE.key(), type)
                    .putExtra(BundleKey.CURRENT.key(), current)
                    .putExtra(BundleKey.VIDEO_POSITION.key(), position)
                    .putExtra(BundleKey.DATA.key(), JsonHelper.getInstance().toJson(data))
            )
        }

        override fun onSlideUpSendImage(
            type: MultimediaHelper.Type,
            list: List<AMediaBean>,
            isOriginal: Boolean
        ) {
            sendFileSize = list.size
            isSendSingleFile = sendFileSize == 1
            when (type) {
                MultimediaHelper.Type.IMAGE -> executeSendPhotos(list, isOriginal)
                MultimediaHelper.Type.FILE -> for (bean in list) {
                    val fileType = FileUtil.getFileType(bean.path)
                    when (fileType) {
                        Global.FileType_Png, Global.FileType_Jpg, Global.FileType_jpeg, Global.FileType_bmp -> {
                            chatViewModel.sendImage(isOriginal, bean.path)
                        }

                        Global.FileType_gif -> {
                            val bitmapBean = PictureParse.parseGifPath(this@ChatNormalActivity, bean.path)
                            chatViewModel.sendGifImg(
                                bitmapBean.url,
                                bean.path,
                                bitmapBean.width,
                                bitmapBean.height
                            )
                        }

                        Global.FileType_mov, Global.FileType_mp4, Global.FileType_rmvb, Global.FileType_avi -> {
                            val iVideoSize: IVideoSize = VideoSizeFromVideoFile(bean.path)
                            chatViewModel.sendVideo(iVideoSize)
                        }

                        else -> chatViewModel.sendFile(bean.path)
                    }
                }

                MultimediaHelper.Type.VIDEO -> executeSendVideos(list, isOriginal)
                else -> {}
            }
        }

        override fun onMediaSelector(
            type: MultimediaHelper.Type,
            list: List<AMediaBean>,
            isOriginal: Boolean
        ) {
            binding.funMedia.visibility = View.GONE
            binding.chatKeyboardLayout.clearIconState()
            onSlideUpSendImage(type, list, isOriginal)
        }

        override fun onInputClick() {
            binding.funMedia.visibility = View.GONE
        }

        override fun onSoftKeyboardStartOpened(keyboardHeightInPx: Int) {
//            if (binding.scopeRobotChat.getVisibility() == View.VISIBLE)
//                binding.guideLine.setGuidelinePercent(0.79f);
        }

        override fun onSoftKeyboardEndOpened(keyboardHeightInPx: Int) {
        }

        override fun onSoftKeyboardClosed() {
//            binding.lyChildChat.onSoftKeyboardClosed(0);
//            if (binding.messageRV.adapter != null) binding.messageRV.adapter?.refreshData()

            //            if (binding.scopeRobotChat.getVisibility() == View.VISIBLE)
//                binding.guideLine.setGuidelinePercent(0.89f);
        }

        override fun onOpenExtraArea() {
            chatViewModel.roomEntity?.let {
                binding.chatKeyboardLayout.doInitExtraArea(
                    chatViewModel.isSettingBusinessCardInfo.value &&
                        it.listClassify == ChatRoomSource.SERVICE &&
                        it.serviceNumberOpenType.contains(
                            "O"
                        ) &&
                        it.type == ChatRoomType.services,
                    it,
                    isProvisionMember,
                    it.serviceNumberType == ServiceNumberType.BOSS && it.serviceNumberOwnerId == selfUserId && it.type == ChatRoomType.services
                )
                if (chatViewModel.channelType.isEmpty()) {
                    // 取得渠道名稱以發送電子名片
                    chatViewModel.getLastChannelFrom(it.id)
                }
            }
        }

        override fun onBusinessCardSend() {
            showLoadingView(R.string.welcome_tip_04)
            chatViewModel.roomEntity?.let {
                chatViewModel.doSendBusinessCard(it.id)
            }
        }

        override fun onBusinessMemberCardSend() {
            showLoadingView(R.string.welcome_tip_04)
            chatViewModel.roomEntity?.let {
                chatViewModel.doSendBusinessMemberCard(it.id)
            }
        }

        override fun onOpenCameraDialog() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                if (!checkForSelfPermissions(permissionsCamera)) {
                    cameraPermissionLauncher.launch(permissionsCamera)
                } else {
                    doOpenCamera()
                }
            } else {
                if (!checkForSelfPermissions(permissionsTIRAMISU)) {
                    cameraPermissionLauncher.launch(permissionsTIRAMISU)
                } else {
                    doOpenCamera()
                }
            }
        }

//        override fun onPicSelected(list: List<PhotoBean>) {}
    }

    private fun doOpenCamera() {
        BottomSheetDialogBuilder(this, layoutInflater)
            .doOpenMedia({
                binding.chatKeyboardLayout.doCameraAction()
            }, {
                binding.chatKeyboardLayout.doVideoAction()
            })
            .show()
    }

    /**
     * 處理送出影片
     */
    private fun executeSendVideos(
        list: List<AMediaBean>?,
        isOriginal: Boolean
    ) {
        if (list.isNullOrEmpty()) {
            Toast
                .makeText(
                    this,
                    getString(R.string.text_select_video_first_please),
                    Toast.LENGTH_SHORT
                ).show()
            return
        }
        for (i in list.indices) {
            val fileType = FileUtil.getFileType(list[i].path)
            when (fileType) {
                Global.FileType_mp4 -> {
                    val iVideoSize: IVideoSize = VideoSizeFromVideoFile(list[i].path)
                    chatViewModel.sendVideo(iVideoSize)
                }

                else -> chatViewModel.sendFile(list[i].path)
            }
        }
    }

    private fun showFunMedia(isChange: Boolean) {
        if (isChange) {
            binding.funMedia.setChangeVisibility()
        } else {
            binding.funMedia.setType(
                this@ChatNormalActivity,
                MultimediaHelper.Type.IMAGE,
                themeStyle,
                MEDIA_SELECTOR_REQUEST_CODE
            )
        }

        binding.funMedia.visibility = View.VISIBLE
    }

    /**
     * 處理送出圖片
     */
    private fun executeSendPhotos(
        list: List<AMediaBean>?,
        isOriginal: Boolean
    ) {
        if (list.isNullOrEmpty()) {
            Toast
                .makeText(
                    this,
                    getString(R.string.text_select_image_first_please),
                    Toast.LENGTH_SHORT
                ).show()
            return
        }
        val copy = list.toMutableList()
        CoroutineScope(Dispatchers.IO).launch {
            for (i in copy.indices) {
                val fileType = FileUtil.getFileType(copy[i].path)
                when (fileType) {
                    Global.FileType_Png, Global.FileType_Jpg, Global.FileType_jpeg, Global.FileType_bmp -> {
                        chatViewModel.sendImage(isOriginal, copy[i].path)
                    }

                    Global.FileType_gif -> {
                        val bitmapBean = PictureParse.parseGifPath(this@ChatNormalActivity, copy[i].path)
                        chatViewModel.sendGifImg(
                            bitmapBean.url,
                            copy[i].path,
                            bitmapBean.width,
                            bitmapBean.height
                        )
                    }

                    Global.FileType_mp4 -> {
                        val iVideoSize: IVideoSize = VideoSizeFromVideoFile(copy[i].path)
                        chatViewModel.sendVideo(iVideoSize)
                    }

                    else -> chatViewModel.sendFile(copy[i].path)
                }
                delay(1000)
            }
        }
    }

    private fun checkMicAvailable(): Boolean {
        if (AudioLib.getInstance(this).isPlaying) {
            Toast
                .makeText(
                    this,
                    getString(R.string.text_mic_phone_is_being_used),
                    Toast.LENGTH_SHORT
                ).show()
            return false
        }

        if (binding.chatKeyboardLayout.isRecording) {
            Toast
                .makeText(this, getString(R.string.text_recording), Toast.LENGTH_SHORT)
                .show()
            return false
        }
        return true
    }

    fun showRecordingWindow() {
        // 通知禁止休眠
        EventBusUtils.sendEvent(EventMsg<Any?>(MsgConstant.NOTICE_KEEP_SCREEN_ON))

        binding.chatKeyboardLayout.hideBottomPop()
        binding.chatKeyboardLayout.setRecordIconState()
        binding.chatKeyboardLayout.post {
            binding.chatKeyboardLayout.setSelect(
                ChatKeyboardLayout.BottomFunType.FUN_RECORD,
                true
            )
        }
    }

    private fun createVideoFile(): File? {
        val dtf = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
        val strDate: String = dtf.format(LocalDateTime.now())
        val imageFileName = "Aile_" + strDate + "_.mp4"
        val tempFile: File = File(cacheDir, imageFileName)
        return tempFile
    }

    private fun createImageFile(): File? {
        val dtf = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
        val strDate: String = dtf.format(LocalDateTime.now())
        val imageFileName = "Aile_$strDate.jpg"
        val tempFile = File(cacheDir, imageFileName)
        return tempFile
    }

    internal enum class ActionStatus(
        var status: Boolean
    ) {
        RICH_MENU(false),
        SCROLL(true);

        fun status(): Boolean = status
    }

    internal enum class SortType {
        ASC,
        DESC
    }
}
