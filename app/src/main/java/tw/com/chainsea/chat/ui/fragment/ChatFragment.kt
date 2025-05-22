package tw.com.chainsea.chat.ui.fragment

import android.Manifest
import android.Manifest.permission
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.media.CamcorderProfile
import android.net.Uri
import android.os.Build
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
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.LinearInterpolator
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.URLUtil
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.textfield.TextInputEditText
import com.google.common.base.Strings
import com.google.common.collect.Lists
import com.google.common.collect.Maps
import com.google.common.collect.Queues
import com.google.common.collect.Sets
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.android.common.datetime.DateTimeHelper
import tw.com.chainsea.android.common.event.KeyboardHelper
import tw.com.chainsea.android.common.image.BitmapHelper
import tw.com.chainsea.android.common.json.JsonHelper
import tw.com.chainsea.android.common.log.CELog
import tw.com.chainsea.android.common.multimedia.AMediaBean
import tw.com.chainsea.android.common.multimedia.ImageBean
import tw.com.chainsea.android.common.multimedia.MultimediaHelper
import tw.com.chainsea.android.common.text.KeyWordHelper
import tw.com.chainsea.android.common.text.StringHelper
import tw.com.chainsea.android.common.ui.UiHelper
import tw.com.chainsea.android.common.video.IVideoSize
import tw.com.chainsea.android.common.video.VideoSizeFromVideoFile
import tw.com.chainsea.ce.sdk.bean.BadgeDataModel
import tw.com.chainsea.ce.sdk.bean.FacebookTag
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
import tw.com.chainsea.ce.sdk.bean.msg.ChannelType.WEICHAT
import tw.com.chainsea.ce.sdk.bean.msg.FacebookCommentStatus
import tw.com.chainsea.ce.sdk.bean.msg.FacebookPostStatus
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
import tw.com.chainsea.ce.sdk.bean.msg.content.VoiceContent
import tw.com.chainsea.ce.sdk.bean.room.AiServiceWarnedSocket
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEnum
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType
import tw.com.chainsea.ce.sdk.bean.room.QuickReplyItem
import tw.com.chainsea.ce.sdk.bean.room.QuickReplySocket
import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberStatus
import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberType
import tw.com.chainsea.ce.sdk.bean.todo.TodoEntity
import tw.com.chainsea.ce.sdk.bean.todo.TodoStatus
import tw.com.chainsea.ce.sdk.database.DBManager
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.database.sp.UserPref
import tw.com.chainsea.ce.sdk.event.EventBusUtils
import tw.com.chainsea.ce.sdk.event.EventMsg
import tw.com.chainsea.ce.sdk.event.MsgConstant
import tw.com.chainsea.ce.sdk.http.ce.request.FromAppointRequest
import tw.com.chainsea.ce.sdk.http.ce.request.ServiceNumberChatroomAgentServicedRequest
import tw.com.chainsea.ce.sdk.http.ce.request.SyncContactRequest
import tw.com.chainsea.ce.sdk.network.NetworkManager.provideRetrofit
import tw.com.chainsea.ce.sdk.network.model.common.ApiErrorData
import tw.com.chainsea.ce.sdk.network.model.common.CommonResponse
import tw.com.chainsea.ce.sdk.network.model.common.ServiceSwitchIdentityResponse
import tw.com.chainsea.ce.sdk.network.model.request.ServiceSwitchIdentityRequest
import tw.com.chainsea.ce.sdk.network.model.request.ServicesIdentityListRequest
import tw.com.chainsea.ce.sdk.network.model.response.ChatRoomMemberResponse
import tw.com.chainsea.ce.sdk.network.model.response.ServicesIdentityListResponse
import tw.com.chainsea.ce.sdk.network.services.IdentityListService
import tw.com.chainsea.ce.sdk.network.services.SwitchIdentityService
import tw.com.chainsea.ce.sdk.reference.AccountRoomRelReference
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference
import tw.com.chainsea.ce.sdk.reference.MessageReference
import tw.com.chainsea.ce.sdk.reference.UserProfileReference
import tw.com.chainsea.ce.sdk.service.ChatRoomService
import tw.com.chainsea.ce.sdk.service.ChatServiceNumberService
import tw.com.chainsea.ce.sdk.service.ChatServiceNumberService.ServicedTransferType
import tw.com.chainsea.ce.sdk.service.RepairMessageService
import tw.com.chainsea.ce.sdk.service.UserProfileService
import tw.com.chainsea.ce.sdk.service.listener.AgentSnatchCallback
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack
import tw.com.chainsea.ce.sdk.service.type.ChatRoomSource
import tw.com.chainsea.ce.sdk.service.type.RefreshSource
import tw.com.chainsea.ce.sdk.socket.ce.bean.ProvisionalMemberAddedSocket
import tw.com.chainsea.ce.sdk.socket.ce.code.NoticeCode
import tw.com.chainsea.ce.sdk.util.BitmapUtil
import tw.com.chainsea.chat.App
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.aiff.AiffManager
import tw.com.chainsea.chat.aiff.database.AiffDB
import tw.com.chainsea.chat.base.Constant
import tw.com.chainsea.chat.config.AiffEmbedLocation
import tw.com.chainsea.chat.config.BundleKey
import tw.com.chainsea.chat.config.InvitationType
import tw.com.chainsea.chat.databinding.FragmentChatBinding
import tw.com.chainsea.chat.databinding.PopupProvisionMemberActionBinding
import tw.com.chainsea.chat.databinding.ProgressSendVideoBinding
import tw.com.chainsea.chat.flow.dataclass.ReSendMessageFormat
import tw.com.chainsea.chat.flow.dataclass.SendBusinessFormat
import tw.com.chainsea.chat.flow.dataclass.SendButtonClickedFormat
import tw.com.chainsea.chat.flow.dataclass.SendFileFormat
import tw.com.chainsea.chat.flow.dataclass.SendImageFormat
import tw.com.chainsea.chat.flow.dataclass.SendImageGifFormat
import tw.com.chainsea.chat.flow.dataclass.SendImageWithBitMapFormat
import tw.com.chainsea.chat.flow.dataclass.SendVideoFormat
import tw.com.chainsea.chat.flow.dataclass.SendVoiceFormat
import tw.com.chainsea.chat.keyboard.ChatKeyboardLayout
import tw.com.chainsea.chat.keyboard.ChatKeyboardLayout.OnChatKeyBoardListener
import tw.com.chainsea.chat.keyboard.ChatKeyboardLayout.OnMentionFeatureListener
import tw.com.chainsea.chat.keyboard.MentionSelectAdapter
import tw.com.chainsea.chat.keyboard.media.MediaBean.MediaListener
import tw.com.chainsea.chat.keyboard.media.MediaSelectorPreviewActivity
import tw.com.chainsea.chat.keyboard.view.HadEditText
import tw.com.chainsea.chat.keyboard.view.HadEditText.SendData
import tw.com.chainsea.chat.lib.ActivityManager
import tw.com.chainsea.chat.lib.AtMatcherHelper
import tw.com.chainsea.chat.lib.PictureParse
import tw.com.chainsea.chat.lib.ToastUtils
import tw.com.chainsea.chat.mediagallery.view.MediaGalleryActivity
import tw.com.chainsea.chat.messagekit.enums.OpenBottomRichMeunType
import tw.com.chainsea.chat.messagekit.enums.RichMenuBottom
import tw.com.chainsea.chat.messagekit.enums.RichMenuType
import tw.com.chainsea.chat.messagekit.lib.AudioLib
import tw.com.chainsea.chat.messagekit.lib.FileUtil
import tw.com.chainsea.chat.messagekit.lib.Global
import tw.com.chainsea.chat.messagekit.lib.MessageDomino
import tw.com.chainsea.chat.messagekit.listener.CheckFacebookCommentStatus
import tw.com.chainsea.chat.messagekit.listener.OnChatRoomTitleChangeListener
import tw.com.chainsea.chat.messagekit.listener.OnFacebookReplyClick
import tw.com.chainsea.chat.messagekit.listener.OnMainMessageControlEventListener
import tw.com.chainsea.chat.messagekit.listener.OnMainMessageScrollStatusListener
import tw.com.chainsea.chat.messagekit.listener.OnMessageSlideReply
import tw.com.chainsea.chat.messagekit.listener.OnRobotChatMessageClickListener
import tw.com.chainsea.chat.messagekit.listener.OnTemplateClickListener
import tw.com.chainsea.chat.messagekit.main.adapter.BottomRichMeunAdapter
import tw.com.chainsea.chat.messagekit.main.adapter.MessageAdapterMode
import tw.com.chainsea.chat.messagekit.main.viewholder.VoiceMessageView
import tw.com.chainsea.chat.network.contact.ViewModelFactory
import tw.com.chainsea.chat.searchfilter.view.activity.MemberInvitationActivity
import tw.com.chainsea.chat.service.ActivityTransitionsControl
import tw.com.chainsea.chat.style.RoomThemeStyle
import tw.com.chainsea.chat.ui.activity.ChatActivity
import tw.com.chainsea.chat.ui.activity.ChatNormalActivity
import tw.com.chainsea.chat.ui.activity.FileExplorerActivity
import tw.com.chainsea.chat.ui.adapter.ChatRoomMessageSearchAdapter
import tw.com.chainsea.chat.ui.adapter.OnMessageItemClick
import tw.com.chainsea.chat.ui.adapter.OnProvisionalMemberItemClick
import tw.com.chainsea.chat.ui.adapter.ProvisionalMemberAdapter
import tw.com.chainsea.chat.ui.adapter.QuickReplyAdapter
import tw.com.chainsea.chat.ui.adapter.entity.ChannelEntity
import tw.com.chainsea.chat.ui.adapter.entity.RichMenuInfo
import tw.com.chainsea.chat.ui.dialog.BottomSheetDialogBuilder
import tw.com.chainsea.chat.ui.dialog.TransferDialogBuilder
import tw.com.chainsea.chat.ui.dialog.TransferDialogBuilder.OnSubmitListener
import tw.com.chainsea.chat.ui.dialog.WaitTransferDialogBuilder
import tw.com.chainsea.chat.util.DaVinci
import tw.com.chainsea.chat.util.DownloadUtil
import tw.com.chainsea.chat.util.GlideEngine.Companion.createGlideEngine
import tw.com.chainsea.chat.util.IntentUtil.launchIntent
import tw.com.chainsea.chat.util.IntentUtil.start
import tw.com.chainsea.chat.util.IntentUtil.startIntent
import tw.com.chainsea.chat.util.NoDoubleClickListener
import tw.com.chainsea.chat.util.ThemeHelper
import tw.com.chainsea.chat.util.TimeUtil
import tw.com.chainsea.chat.view.business.BusinessTaskAction
import tw.com.chainsea.chat.view.chat.ChatViewModel
import tw.com.chainsea.chat.view.chatroom.adapter.AdvisoryRoomAdapter
import tw.com.chainsea.chat.view.chatroom.adapter.SmallRoomData
import tw.com.chainsea.chat.view.consultai.ConsultAIActivity
import tw.com.chainsea.chat.view.todo.OnSetRemindTime
import tw.com.chainsea.chat.view.todo.TodoListFragment
import tw.com.chainsea.chat.view.todo.TodoSettingDialog
import tw.com.chainsea.chat.widget.GridItemDecoration
import tw.com.chainsea.chat.widget.xrefreshlayout.XRefreshLayout
import tw.com.chainsea.chat.widget.xrefreshlayout.XRefreshLayout.OnBackgroundClickListener
import tw.com.chainsea.custom.view.alert.AlertView
import tw.com.chainsea.custom.view.popup.SimpleTextTooltip
import tw.com.chainsea.custom.view.progress.IosProgressBar
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.MessageFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Collections
import java.util.LinkedList
import java.util.Locale
import java.util.Objects
import java.util.Queue
import java.util.TreeMap
import java.util.function.Consumer
import kotlin.math.max
import kotlin.math.min

/**
 * chat place
 * Created by 90Chris on 2016/4/21.
 */
class ChatFragment :
    Fragment(),
    MediaListener,
    XRefreshLayout.OnRefreshListener,
    OnBackgroundClickListener,
    OnProvisionalMemberItemClick,
    TimeUtil.OnNetworkTimeListener {
    companion object {
        private val TAG: String = ChatFragment::class.java.simpleName
        const val REQUEST_CONSULT_AI_CODE: Int = 30
        private const val MEDIA_SELECTOR_REQUEST_CODE: Int = 0x2705
        private const val SHARE_SCREENSHOTS_RESULT_CODE = 0x1389
        private val messageTimeLineFormat = SimpleDateFormat("MMMdd日(EEE)", Locale.TAIWAN)

        fun newInstance(
            userName: String?,
            userId: String?
        ): ChatFragment {
            val fragment = ChatFragment()
            val bundle = Bundle()
            bundle.putString(BundleKey.USER_NICKNAME.key(), userName)
            bundle.putString(BundleKey.USER_ID.key(), userId)
            fragment.arguments = bundle
            return fragment
        }

        fun newInstance(
            msg: MessageEntity?,
            unreadMessageId: String?,
            keyWord: String?,
            themeStyle: RoomThemeStyle?
        ): ChatFragment {
            val fragment = ChatFragment()
            val bundle = Bundle()
            bundle.putSerializable(BundleKey.EXTRA_MESSAGE.key(), msg)
            bundle.putString(BundleKey.UNREAD_MESSAGE_ID.key(), unreadMessageId)
            bundle.putString(BundleKey.SEARCH_KEY.key(), keyWord)
            bundle.putSerializable(BundleKey.CHAT_ROOM_STYLE.key(), themeStyle)
            fragment.arguments = bundle
            return fragment
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
                        t2.sendTime,
                        t1.sendTime
                    )
                } else {
                    return@sort Longs.compare(
                        t2.sendTime,
                        t1.sendTime
                    )
                }
            }
        }
    }

    private var currentDate: String? = null
    private var progressBar: IosProgressBar? = null

    private lateinit var binding: FragmentChatBinding
    private var userName: String? = null
    private var userId: String? = null

    private var themeId: String? = ""

    var checkOnceUnreadNumber: Boolean = false

    private lateinit var mentionSelectAdapter: MentionSelectAdapter
    private lateinit var photoFile: File
    private lateinit var videoFile: File
    private var isReply = false

    private var onChatRoomTitleChangeListener: OnChatRoomTitleChangeListener? = null
    private var robotChatRecord = ""
    private lateinit var provisionalMemberAdapter: ProvisionalMemberAdapter

    // 紀錄手指按下 subRoom 的時間
    var subRoomOnTouchDownTime: Long = 0
    private val chatRoomMemberTable: MutableMap<String, String> = HashMap()
    private lateinit var intentFilter: IntentFilter
    private lateinit var timeZoneChangeReceiver: TimeZoneChangeReceiver
    private var searchKeyWord: String? = null
    private val isGreenTheme: Boolean = ThemeHelper.isGreenTheme()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
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

    fun setOnChatRoomTitleChangeListener(onChatRoomTitleChangeListener: OnChatRoomTitleChangeListener?) {
        this.onChatRoomTitleChangeListener = onChatRoomTitleChangeListener
    }

    // 相簿權限
    private val launcher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            result.forEach { (_, isGranted) ->
                if (!isGranted) {
                    ToastUtils.showToast(
                        requireContext(),
                        getString(R.string.text_need_storage_permission)
                    )
                    return@registerForActivityResult
                }
            }
            binding.funMedia.init(requireActivity())
            showFunMedia(false)
        }

    // Camera權限
    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            result.forEach { (_, isGranted) ->
                if (!isGranted) {
                    ToastUtils.showToast(
                        requireContext(),
                        getString(R.string.text_need_camera_permission)
                    )
                    return@registerForActivityResult
                }
            }
            doOpenCamera()
        }
    private val storagePermissionResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                binding.funMedia.init(requireActivity())
                showFunMedia(false)
            } else {
                ToastUtils.showToast(
                    requireContext(),
                    getString(R.string.text_need_storage_permission)
                )
            }
        }
    private val shareScreenShotResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let { intent ->
                    val bundle = intent.extras
                    bundle?.let { b ->
                        val typeToken = object : TypeToken<List<String>>() {}.type
                        val ids =
                            JsonHelper
                                .getInstance()
                                .from<List<String>>(
                                    b.getString("data"),
                                    typeToken
                                )
                        val filePath = b.getString(BundleKey.FILE_PATH.key())
                        chatViewModel.doExecutionSendImage(ids, filePath)
                    }
                }
            }
        }
    private val sendVideoCaptureResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val size = VideoSizeFromVideoFile(videoFile.absolutePath)
                chatViewModel.sendVideo(size, isFacebookReplyPublic)
            }
        }

    private val sendImageCaptureResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                chatViewModel.sendImage(
                    path = photoFile.absolutePath,
                    isFacebookReplyPublic = isFacebookReplyPublic
                )
            }
        }

    private val sendFileOpenResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let { data ->
                    val filePaths: List<String>? = data.getStringArrayListExtra(BundleKey.FILE_PATH_LIST.key())
                    filePaths?.let { paths ->
                        sendFileSize = paths.size
                        isSendSingleFile = sendFileSize == 1
                        for (filePath in paths) {
                            val fileType = FileUtil.getFileType(filePath)
                            when (fileType) {
                                Global.FileType_Png, Global.FileType_Jpg, Global.FileType_jpeg, Global.FileType_bmp -> {
                                    chatViewModel.sendImage(true, filePath)
                                }

                                Global.FileType_gif -> {
                                    val bitmapBean =
                                        PictureParse.parseGifPath(
                                            requireContext(),
                                            filePath
                                        )
                                    chatViewModel.sendGifImg(
                                        bitmapBean.url,
                                        filePath,
                                        bitmapBean.width,
                                        bitmapBean.height,
                                        isFacebookReplyPublic
                                    )
                                }

                                Global.FileType_mov, Global.FileType_mp4, Global.FileType_rmvb, Global.FileType_avi -> {
                                    val iVideoSize: IVideoSize = VideoSizeFromVideoFile(filePath)
                                    if (iVideoSize.size() == 0L) {
                                        chatViewModel.sendFile(filePath, isFacebookReplyPublic)
                                    } else {
                                        chatViewModel.sendVideo(iVideoSize, isFacebookReplyPublic)
                                    }
                                }

                                else -> chatViewModel.sendFile(filePath, isFacebookReplyPublic)
                            }
                        }
                    }
                }
            }
        }
    private lateinit var keyBoardBarListener: KeyBoardBarListener
    private var aiffManager: AiffManager? = null

    // 商業號可切換身份的 list
    private var identityList: List<ServicesIdentityListResponse>? = ArrayList()
    private var keyWord = ""
    private var isProvisionMember = false

    // 點擊臨時成員的 popup window
    private lateinit var provisionMemberPopupWindow: PopupWindow

    // 用於判斷重複點擊相同 item 能夠正確的 dismiss
    private var isProvisionMemberPopupShowing = false
    private var isSendSingleFile = false
    private var sendFileSize = 0
    private var addProgressValue = 0
    private val selfUserId: String by lazy {
        TokenPref.getInstance(requireContext()).userId
    }

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

    var themeStyle: RoomThemeStyle? = RoomThemeStyle.UNDEF

    // 避免重複點擊
    private var isActivityForResult = false

    private var messageEntity: MessageEntity? = null
    var mIsBlock: Boolean = false
    private var lastItemPosition = 0
    private var actionStatus = ActionStatus.SCROLL

    // Whether the unread dividing line has been crossed to determine
    private val undeadLineMessage: MessageEntity? = null
    private var undeadLineDrawable = false
    private lateinit var mediaPreviewARL: ActivityResultLauncher<Intent>

    private lateinit var chatViewModel: ChatViewModel

    private var isFacebookReplyPublic = false
    private var isMetaReplyOverTime = false
    private var servicedDurationTime = 0L

    // AI 諮詢引用的回覆
    private val aiConsultQuoteTextResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let { data ->
                    val bundle = data.extras
                    if (bundle != null) {
                        val listType = object : TypeToken<MessageType>() {}.type
                        val typeString = bundle.getString(BundleKey.CONSULT_AI_QUOTE_TYPE.key())
                        typeString?.let {
                            try {
                                val messageType: MessageType = JsonHelper.getInstance().from(it, listType)
                                when (messageType) {
                                    MessageType.TEXT -> {
                                        val quoteString = data.getStringExtra(BundleKey.CONSULT_AI_QUOTE_STRING.key())
                                        binding.chatKeyboardLayout.setUnfinishedEdited(
                                            InputLogBean.from(quoteString)
                                        )
                                    }
                                    else -> {}
                                }
                            } catch (ignored: Exception) {
                            }
                        }
                    }
                }
            }
        }

    // 服務號點選諮詢後回來 result
    private val consultSelectResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == REQUEST_CONSULT_AI_CODE) {
                result.data?.let { data ->
                    val bundle = data.extras
                    if (bundle != null) {
                        val consultAiId =
                            bundle.getString(BundleKey.CONSULT_AI_ID.key())
                        consultAiId?.let { id ->
                            onAiConsultSelected(id)
                        }
                        launchIntent(
                            requireContext(),
                            ConsultAIActivity::class.java,
                            aiConsultQuoteTextResult,
                            data.extras
                        )
                    }
                }
            } else {
                result.data?.let {
                    onConsultSelected(it)
                }
            }
        }
    private val advisoryRoomAdapter: AdvisoryRoomAdapter =
        AdvisoryRoomAdapter(aiConsultQuoteTextResult)

    private lateinit var adapter: ChatRoomMessageSearchAdapter

    // /麥克風權限
    private val recordPermissionResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                if (checkMicAvailable()) {
                    showRecordingWindow()
                }
            } else {
                val intent =
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.setData(Uri.parse("package:" + requireContext().packageName))
                start(requireContext(), intent)
                ToastUtils.showToast(
                    requireContext(),
                    getString(R.string.text_need_mic_permission)
                )
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat, container, false)
        initViewModel()
        provisionMemberPopupWindow =
            PopupWindow(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        provisionMemberPopupWindow.isOutsideTouchable = true

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        EventBusUtils.register(this)
        arguments?.let {
            this.messageEntity =
                it.getSerializable(BundleKey.EXTRA_MESSAGE.key()) as MessageEntity?
            val unreadMessageId = it.getString(BundleKey.UNREAD_MESSAGE_ID.key())
            userName = it.getString(BundleKey.USER_NICKNAME.key())
            userId = it.getString(BundleKey.USER_ID.key())
            searchKeyWord = it.getString(BundleKey.SEARCH_KEY.key())
            themeStyle = it.getSerializable(BundleKey.CHAT_ROOM_STYLE.key()) as RoomThemeStyle?
            chatViewModel.roomEntity?.let { entity ->
                checkOnceUnreadNumber = entity.unReadNum > 0
                chatViewModel.unreadNum = entity.unReadNum
                themeStyle()
            }
            chatViewModel.unreadMessageId = unreadMessageId ?: ""
        }
        chatViewModel.mainMessageData.clear()

        intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED)
        timeZoneChangeReceiver = TimeZoneChangeReceiver()
        requireContext().registerReceiver(timeZoneChangeReceiver, intentFilter)

        chatViewModel.roomEntity?.let { entity ->
            isProvisionMember =
                entity.provisionalIds.contains(selfUserId) &&
                entity.listClassify == ChatRoomSource.MAIN
            if (entity.serviceNumberStatus == ServiceNumberStatus.ROBOT_SERVICE && (selfUserId != entity.ownerId)) { // AI服務
                setAiServices()
                binding.rvBottomRoomList.visibility = View.GONE
                ToastUtils.showToast(
                    requireContext(),
                    getString(R.string.text_robot_servicing_right_now)
                )
            }

            if (entity.provisionalIds != null && entity.provisionalIds.isNotEmpty()) {
                if (entity.provisionalIds.contains(selfUserId) && entity.listClassify != ChatRoomSource.MAIN) {
                    binding.scopeProvisionalMemberList.visibility = View.GONE
                } else {
                    chatViewModel.getMemberProfileEntity(entity.provisionalIds)
                }
            } else {
                binding.scopeProvisionalMemberList.visibility = View.GONE
            }

            setup()
            init()
            if (entity.type == ChatRoomType.group) {
                // 判斷是否要顯示 "沒有擁有者" 訊息
                chatViewModel.getChatMember(entity.id)
            }

            val ownerId = entity.ownerId
            val hasOwner = UserProfileReference.hasLocalData(null, ownerId)
            if (hasOwner) {
                setInputHint() // first time call
                // 未編輯完成
                val unfinishedEdited = entity.unfinishedEdited
                if (!Strings.isNullOrEmpty(unfinishedEdited)) {
                    binding.chatKeyboardLayout.setUnfinishedEdited(
                        InputLogBean.from(
                            unfinishedEdited
                        )
                    )
                }
            } else {
                UserProfileService.getProfile(
                    requireContext(),
                    RefreshSource.REMOTE,
                    ownerId,
                    object : ServiceCallBack<UserProfileEntity?, RefreshSource?> {
                        override fun error(message: String) {
                        }

                        override fun complete(
                            t: UserProfileEntity?,
                            e: RefreshSource?
                        ) {
                            setInputHint()
                            // 未編輯完成
                            val unfinishedEdited = entity.unfinishedEdited
                            if (!Strings.isNullOrEmpty(unfinishedEdited)) {
                                binding.chatKeyboardLayout.setUnfinishedEdited(
                                    InputLogBean.from(
                                        unfinishedEdited
                                    )
                                )
                            }
                        }
                    }
                )
            }
            aiffManager = AiffManager(requireActivity(), entity.id)
        }
        chatViewModel.clearIsAtMeFlag()
        provisionalMemberAdapter = ProvisionalMemberAdapter(this)
        binding.rvProvisionalMemberList.adapter = provisionalMemberAdapter

        // EVAN_FLAG 2020-06-09 (1.11.0) 圖片選擇器，預覽後返回
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

        initData()
        observeData()
    }

    private fun initData() {
        messageEntity?.isAnimator = true
        chatViewModel.recordMode = messageEntity != null
        messageEntity?.let {
            chatViewModel.setKeyWordByMessage(searchKeyWord ?: "", it)
        } ?: run {
            chatViewModel.roomEntity?.let {
                highLightUnReadLine(it.unReadNum > 0)
            }
            chatViewModel.disPlayMessageFromDatabase()
            chatViewModel.loadChatMessageList()
        }
        initMessageList()
        initKeyboard()
        initListener()
        chatViewModel.roomEntity?.let {
            chatViewModel.getLastChannelFrom(it.id)
        }
    }

    /**
     * 點擊臨時成員頭像
     * @param item 該臨時成員資料
     * @param position 該臨時成員在 recyclerView 的 position
     * @param itemView 該臨時成員的頭像 需要當 popupWindows 的 anchor
     */
    override fun onClickEvent(
        item: UserProfileEntity,
        position: Int,
        itemView: View
    ) {
        if (isProvisionMemberPopupShowing && provisionMemberPopupWindow.contentView.tag as Int == position) {
            isProvisionMemberPopupShowing = false
            return
        } else {
            isProvisionMemberPopupShowing = true
        }

        if (context != null) {
            val popupProvisionMemberActionBinding =
                PopupProvisionMemberActionBinding.inflate(
                    LayoutInflater.from(
                        context
                    ),
                    null,
                    false
                )
            provisionMemberPopupWindow.contentView = popupProvisionMemberActionBinding.root
            popupProvisionMemberActionBinding.tvName.text = item.nickName
            popupProvisionMemberActionBinding.btnChat.setOnClickListener { v ->
                if (provisionMemberPopupWindow.isShowing) provisionMemberPopupWindow.dismiss()
                chatWithProvisionalMember(item)
            }
            popupProvisionMemberActionBinding.btnMainPage.setOnClickListener { v ->
                if (provisionMemberPopupWindow.isShowing) provisionMemberPopupWindow.dismiss()
                goMainPageOfProvisionalMember(item)
            }
            popupProvisionMemberActionBinding.btnRemoveMember.setOnClickListener { v ->
                if (provisionMemberPopupWindow.isShowing) provisionMemberPopupWindow.dismiss()
                chatViewModel.removeProvisionalMember(item.id)
            }

            if (item.id == selfUserId) {
                popupProvisionMemberActionBinding.btnRemoveMember.visibility = View.GONE
            }

            itemView.post {
                if (!provisionMemberPopupWindow.isShowing) {
                    provisionMemberPopupWindow.contentView.tag = position
                    provisionMemberPopupWindow.showAsDropDown(
                        itemView,
                        itemView.left * position,
                        itemView.top,
                        Gravity.CENTER
                    )
                }
            }
        }
    }

    private fun chatWithProvisionalMember(entity: UserProfileEntity) {
        val chatRoomEntity =
            if (entity.id == selfUserId) {
                ChatRoomReference.getInstance().findSelfRoom(entity.id)
            } else {
                ChatRoomReference.getInstance().findById(entity.roomId)
            }
        chatRoomEntity?.let {
            val intent =
                Intent(
                    requireContext(),
                    ChatNormalActivity::class.java
                ).putExtra(BundleKey.USER_ID.key(), entity.id)
                    .putExtra(BundleKey.USER_NICKNAME.key(), entity.nickName)
            start(requireContext(), intent)
        } ?: run {
            if (entity.roomId.isEmpty()) {
                chatViewModel.addContact(entity)
            } else {
                ActivityTransitionsControl.navigateToChat(requireContext(), entity.roomId) { intent: Intent, _: String ->
                    start(requireContext(), intent)
                }
            }
        }
    }

    private fun goMainPageOfProvisionalMember(entity: UserProfileEntity) {
        ActivityTransitionsControl.navigateToEmployeeHomePage(
            requireActivity(),
            entity.id,
            entity.userType
        ) { intent: Intent, _: String ->
            start(
                requireContext(),
                intent
            )
        }
    }

    fun onTouchEvent(event: MotionEvent?): Boolean {
        if (binding.scopeRetractTip.visibility == View.VISIBLE) {
            binding.scopeRetractTip.visibility = View.GONE
        }
        return false
    }

    fun onRefreshMemberList(list: List<String>) {
        if (provisionalMemberAdapter.itemCount > 0) {
            onAddProvisionalMember(list)
        } else {
            chatViewModel.getMemberProfileEntity(list)
        }
    }

    fun onRemoveMember(memberId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            if (provisionalMemberAdapter.currentList.isNotEmpty()) {
                val newProvisionalList =
                    provisionalMemberAdapter.currentList.filter { user ->
                        user.id != memberId
                    }
                provisionalMemberAdapter.submitList(newProvisionalList)
            } else {
                clearProvisionalMember()
            }
        }
    }

    internal enum class ActionStatus(
        var status: Boolean
    ) {
        RICH_MENU(false),
        SCROLL(true)
        ;

        fun status(): Boolean = status
    }

    // Screenshot start and end information
    private val screenShotData: MutableList<MessageEntity> = Lists.newArrayList()

    // CreateBusinessTask
    internal enum class SortType {
        ASC,
        DESC
    }

    private fun initViewModel() {
        val viewModelFactory = ViewModelFactory(requireActivity().application)
        chatViewModel = ViewModelProvider(requireActivity(), viewModelFactory)[ChatViewModel::class.java]
    }

    private fun observeData() {
        chatViewModel.chatRoomEntity.observe(
            viewLifecycleOwner
        ) { chatRoomEntity: ChatRoomEntity ->
            onRefreshMemberList(chatRoomEntity.provisionalIds)
        }

        chatViewModel.managerList.observe(
            viewLifecycleOwner
        ) { groupRoomManager: List<ChatRoomMemberResponse>? ->
            this.filterToShowNoOwnerNotify(
                groupRoomManager
            )
        }

        chatViewModel.becomeOwnerStatus.observe(
            viewLifecycleOwner
        ) { string: String? ->
            ToastUtils.showToast(requireContext(), string)
            hideNoOwnerNotify()
        }

        chatViewModel.errorMessage.observe(
            viewLifecycleOwner
        ) { errorMessage: String? ->
            ToastUtils.showToast(requireContext(), errorMessage)
        }

        chatViewModel.isCancelAiWarningSuccess.observe(
            viewLifecycleOwner
        ) { isSuccess: Boolean ->
            hideLoadingView()
            if (isSuccess) {
                Toast.makeText(requireContext(), getString(R.string.text_ai_warning_return), Toast.LENGTH_SHORT).show()
            } else {
                Toast
                    .makeText(
                        requireContext(),
                        getString(R.string.text_toast_operator_failure),
                        Toast.LENGTH_SHORT
                    ).show()
            }
        }

        chatViewModel.consultList.observe(
            viewLifecycleOwner
        ) { consultList: List<SmallRoomData> ->
            if (consultList.isEmpty()) {
                chatViewModel.roomEntity?.let {
                    it.aiConsultId = ""
                }
            }
            advisoryRoomAdapter.setData(consultList)
            binding.rvBottomRoomList.visibility =
                if ((consultList.isNotEmpty())) View.VISIBLE else View.GONE
        }

        chatViewModel.consultRoomId.observe(
            viewLifecycleOwner
        ) { consultRoomId: Pair<Boolean, String?> ->
            if (consultRoomId.first) {
                chatViewModel.roomEntity?.let {
                    it.aiConsultId = consultRoomId.second
                }
            } else {
                val bundle = Bundle()
                bundle.putString(BundleKey.EXTRA_SESSION_ID.key(), consultRoomId.second)
                startIntent(requireContext(), ChatActivity::class.java, bundle)
            }
        }

        chatViewModel.startConsultError.observe(
            viewLifecycleOwner
        ) { errorData: ApiErrorData ->
            AlertView
                .Builder()
                .setContext(requireContext())
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
            viewLifecycleOwner
        ) { provisionalIds: List<String> ->
            chatViewModel.getMemberProfileEntity(provisionalIds)
        }

        chatViewModel.sendCloseExtraLayout.observe(
            viewLifecycleOwner
        ) { isClose: Boolean ->
            if (isClose) {
                binding.chatKeyboardLayout.doExtraAction(binding.chatKeyboardLayout)
                hideLoadingView()
            }
        }
        chatViewModel.hideLoadingDialog.observe(
            viewLifecycleOwner
        ) { isHide: Boolean ->
            if (isHide) hideLoadingView()
        }
        chatViewModel.sendNotifyChange
            .onEach {
                binding.messageRV.notifyChange(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendChatRoomTitleChangeListener
            .onEach {
                if (onChatRoomTitleChangeListener != null) {
                    onChatRoomTitleChangeListener!!.onTitleChangeListener(it.senderName ?: "")
                }
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendThemeMRVData
            .onEach {
                binding.themeMRV.setData(
                    if (Strings.isNullOrEmpty(it.themeId)) it.nearMessageId else it.themeId,
                    it
                )
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendBindingDataHandled
            .onEach {
                handleBindingData(it.first, it.second, it.third)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendShowMetaOverTimeView
            .onEach {
                if (it) {
                    showMetaOverTimeView()
                } else {
                    dismissMetaOverTimeView()
                }
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendScrollToTop
            .onEach {
                scrollToTop()
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendHighLightUnReadLine
            .onEach {
                highLightUnReadLine(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendShowIsNotMemberMessage
            .onEach {
                showIsNotMemberMessage(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendSetQuickReply
            .onEach {
                setQuickReply(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendSnatchedAgent
            .onEach {
                showSnatchDialog(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendShowLoadingView
            .onEach {
                showLoadingView(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendHideLoadingView
            .onEach {
                hideLoadingView()
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendShowErrorMsg
            .onEach {
                it?.let { message ->
                    showErrorMsg(message)
                }
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendTransferModeDisplay
            .onEach {
                transferModeDisplay(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendSetupAppointStatus
            .onEach {
                setupAppointStatus(it)
                setup()
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendSetRobotChatRecord
            .onEach { resp ->
                resp?.let {
                    setRobotChatRecord(it)
                }
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendLoadRobotChatRecordLink
            .onEach {
                binding.robotChatMessage.loadUrl(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendRobotStopStatus
            .onEach {
                requireActivity().finish()
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendDisplayThemeMessage
            .onEach {
                displayThemeMessage(it.first, it.second, it.third)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendSearchKeyWord
            .onEach {
                setSearchKeyWord(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendShowSendVideoProgress
            .onEach {
                showSendVideoProgress(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendOnSendFacebookImageReplySuccess
            .onEach {
                onSendFacebookImageReplySuccess()
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendOnSendFacebookImageReplyFailed
            .onEach {
                onSendFacebookImageReplyFailed(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendInitProvisionMemberList
            .onEach {
                onCompleteProvisionalMemberList(it.first, it.second)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendOnRefreshMore
            .onEach {
                onRefreshMore(it.first, it.second)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendMoveToFacebookReplyMessage
            .onEach {
                moveToFacebookReplyMessage(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendUpdateFacebookStatus
            .onEach {
                updateFacebookStatus(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendDoChatRoomSnatchByAgent
            .onEach {
                hideLoadingView()
                doChatRoomSnatchByAgent(it.first, it.second)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendShowToast
            .onEach {
                showToast(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendShowErrorToast
            .onEach {
                showErrorToast(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendClearTypedMessage
            .onEach {
                clearTypedMessage()
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendRefreshListView
            .onEach {
                refreshListView()
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendShowNoMoreMessage
            .onEach {
                showNoMoreMessage()
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendUpdateMsgStatusPair
            .onEach {
                updateMsgStatus(it.first, it.second)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendUpdateSendVideoProgress
            .onEach {
                updateSendVideoProgress()
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendUpdateSendVideoProgressInt
            .onEach {
                updateSendVideoProgress(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendUpdateMsgProgress
            .onEach {
                updateMsgProgress(it.first, it.second)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendUpdateMsgStatus
            .onEach {
                updateMsgStatus(it.second, it.third)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendDismissSendVideoProgress
            .onEach {
                dismissSendVideoProgress()
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendSetThemeOpen
            .onEach {
                setThemeOpen(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendMessageToUI
            .onEach {
                sendMessage(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendSetup
            .onEach {
                initData()
                setup()
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendSetupBottomServicedStatus
            .onEach {
                setupBottomServicedStatus(it.first, it.second, it.third)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendSetIsBlock
            .onEach {
                binding.chatKeyboardLayout.setIsBlock(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendNavigateToChat
            .onEach {
                ActivityTransitionsControl.navigateToChat(
                    requireActivity(),
                    it,
                    ChatFragment::class.java.simpleName
                ) { intent: Intent, _: String ->
                    start(requireContext(), intent)
                }
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendSetChatDisable
            .onEach {
                setChatDisable(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendSetChatEnable
            .onEach {
                binding.chatKeyboardLayout.setChatEnable()
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendClearProvisionalMember
            .onEach {
                clearProvisionalMember()
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.scopeRetractTipInvisible
            .onEach {
                binding.scopeRetractTip.visibility = View.GONE
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.sendBindingBusiness
            .onEach {
                bindingBusiness(it.first, it.second, it.third)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.refreshCurrentMessagePosition
            .onEach {
                binding.messageRV.refreshCurrentMessage(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.onMessageDeleted
            .onEach {
                binding.messageRV.adapter?.notifyItemRemoved(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.showSnatchRobotDialog
            .onEach {
                showSnatchRobotDialog(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        chatViewModel.onSnatchRobotSuccess
            .onEach {
                onSnatchRobot(it.first, it.second)
            }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun setChatDisable(text: Int) {
        binding.chatKeyboardLayout.setChatDisable(getString(text))
    }

    private fun showSnatchDialog(data: Any) {
        val agentName: String =
            when (data) {
                is SendVideoFormat -> data.agentName
                is SendImageFormat -> data.agentName
                is SendImageWithBitMapFormat -> data.agentName
                is SendImageGifFormat -> data.agentName
                is SendFileFormat -> data.agentName
                is SendButtonClickedFormat -> data.agentName
                is SendVoiceFormat -> data.agentName
                is SendBusinessFormat -> data.agentName
                is ReSendMessageFormat -> data.agentName
                else -> {
                    ""
                }
            }
        getSnatchDialog(
            String.format(
                getString(R.string.text_agent_servicing_snatch_by_agent),
                agentName
            )
        ).setOthers(
            arrayOf(
                getString(R.string.cancel),
                getString(R.string.text_for_sure)
            )
        ).setOnItemClickListener { o: Any?, position: Int ->
            if (position == 1) {
                chatViewModel.doAgentSnatchByAgent(
                    object : AgentSnatchCallback {
                        override fun onSnatchSuccess() {
                            when (data) {
                                is SendVideoFormat -> chatViewModel.sendVideo(data.iVideoSize)
                                is SendImageFormat -> chatViewModel.sendImage(data.agentName, data.thumbnailName)
                                is SendImageWithBitMapFormat -> chatViewModel.sendImage(data.agentName, data.path, data.bitmap)
                                is SendImageGifFormat -> chatViewModel.sendGifImg(data.davitPath, data.localPath, data.width, data.height)
                                is SendFileFormat -> chatViewModel.sendFile(data.path)
                                is SendButtonClickedFormat -> chatViewModel.sendButtonClicked(data.sendData)
                                is SendVoiceFormat -> chatViewModel.sendVoice(data.path, data.duration)
                                is SendBusinessFormat -> chatViewModel.sendBusiness(data.businessContent)
                                is ReSendMessageFormat -> chatViewModel.retrySend(data.message)
                            }
                        }
                    }
                )
                setServicedGreenStatus()
            }
        }.build()
            .setCancelable(true)
            .show()
    }

    private fun handleBindingData(
        isNew: Boolean,
        scrollToBottom: Boolean,
        message: MessageEntity
    ) = CoroutineScope(Dispatchers.Main).launch {
        val canScrollBottom = binding.messageRV.canScrollBottom()
        if (undeadLineMessage != null && !undeadLineDrawable) {
            chatViewModel.roomEntity?.let {
                val scrollIndex = chatViewModel.mainMessageData.indexOf(undeadLineMessage) - it.unReadNum
                refreshDataAndScrollToPosition(scrollIndex - 1)
            }
        } else {
            if (isNew && canScrollBottom) {
                binding.messageRV.refreshData()
                if (selfUserId != message.senderId) {
                    if (chatViewModel.doesNotExist) {
                        showFloatingLastMessage(message)
                    }
                }
            } else {
                if (scrollToBottom) {
                    binding.messageRV.refreshToBottom(actionStatus.status())
                } else {
                    binding.messageRV.refreshData()
                }
            }
        }
    }

    private fun hideNoOwnerNotify() {
        binding.clNotifyNoOwner.visibility = View.GONE
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

    private fun showLoadingView() {
        progressBar = IosProgressBar.show(requireContext(), getString(R.string.wording_loading), true, false) { }
    }

    private fun setInputHint() {
        chatViewModel.roomEntity?.let {
            if (it.serviceNumberStatus == ServiceNumberStatus.ROBOT_SERVICE) {
                binding.chatKeyboardLayout.setInputHint(
                    getString(R.string.text_robot_hand_over_immediately)
                )
            } else if (ChatRoomType.services == it.type) {
                binding.chatKeyboardLayout.setInputHint(
                    getString(
                        if (it.serviceNumberOpenType.contains("I")) R.string.text_input_hint_inside_sentences else R.string.text_input_hint_outside_sentences,
                        if (it.serviceNumberName.length > 5) {
                            it.serviceNumberName.substring(
                                0,
                                3
                            ) + "..."
                        } else {
                            it.serviceNumberName
                        },
                        if (it.name.length > 5) {
                            it.name.substring(
                                0,
                                3
                            ) + "..."
                        } else {
                            it.name
                        }
                    )
                )
            } else {
                binding.chatKeyboardLayout.setInputHint(getString(R.string.text_hint_input_message)) // 預設
            }
        }
    }

    private fun setupDefaultThemeHeight(proportion: Double): Int {
        val paramsHeight =
            if (proportion == 0.0) ConstraintLayout.LayoutParams.MATCH_PARENT else ConstraintLayout.LayoutParams.WRAP_CONTENT
        val params =
            ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, paramsHeight)
        params.verticalBias = 1.0f
        params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        binding.themeMRV.layoutParams = params
        context?.let {
            val toolbarHeight = UiHelper.dip2px(it, 44.0f)
            val inputHeight = UiHelper.dip2px(it, 45.0f)
            val displayHeight = UiHelper.getDisplayHeight(it)
            val roomHeight = displayHeight - (toolbarHeight + inputHeight)
            return (roomHeight * proportion).toInt()
        }
        return proportion.toInt()
    }

    fun themeStyle() {
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
        try {
            themeStyle?.let {
                binding.floatTimeBoxTV.setBackgroundResource(it.floatingTimeBox)
                binding.xrefreshLayout.setBackgroundColor(it.auxiliaryColor)
                binding.messageRV.setBackgroundColor(it.auxiliaryColor)
                binding.ivConsult.setImageResource(it.consultIconResId)
                binding.chatKeyboardLayout.setThemeStyle(it)
            }
        } catch (err: Exception) {
            // null checked
            Log.e(
                TAG,
                err.message.toString()
            )
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setup() {
        context?.let { mContext ->
            if (!isAdded) return
            val cellHeight = UiHelper.dip2px(mContext, 46f)
            binding.mentionMHRV.setMaxHeight(cellHeight * 4)
            binding.mentionMHRV.setBackgroundColor(Color.WHITE)
            binding.mentionMHRV.layoutManager =
                GridLayoutManager(mContext, 2)
            binding.mentionMHRV.addItemDecoration(GridItemDecoration(Color.WHITE))
            binding.mentionMHRV.itemAnimator = DefaultItemAnimator()
            binding.mentionMHRV.setHasFixedSize(false)
            chatViewModel.roomEntity?.let { entity ->
                this.mentionSelectAdapter =
                    MentionSelectAdapter(mContext)
                        .setUserProfiles(entity.membersLinkedList)
                        .setKeyword("")
                if (entity.membersLinkedList.size >= 8) {
                    val params = binding.mentionMHRV.layoutParams
                    params.height = cellHeight * 4
                }

                binding.chatKeyboardLayout.setOnMentionFeatureListener(
                    object : OnMentionFeatureListener {
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
                                        editText.appendMentionSelect(ecUserProfile, true, needCalculatePosition)
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

                binding.xrefreshLayout.setOnRefreshListener(this).setOnBackgroundClickListener(this)

                val bottomDistance = UiHelper.dip2px(mContext, 55f)
                binding.difbDown
                    .animate()
                    .translationY((binding.difbDown.height + bottomDistance).toFloat())
                    .setInterpolator(LinearInterpolator())
                    .start()

                // 設定滾動狀態監聽器
                binding.messageRV.setOnMainMessageScrollStatusListener(
                    object :
                        OnMainMessageScrollStatusListener {
                        override fun onStopScrolling(recyclerView: RecyclerView) {
                            // 获取最后一个可见view的位置
                            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                            val lastItemPosition = layoutManager.findLastVisibleItemPosition()
                            val firstItemPosition = layoutManager.findFirstVisibleItemPosition()
                            if (chatViewModel.mainMessageData.size > firstItemPosition && firstItemPosition > -1) {
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

                            if (chatViewModel.mainMessageData.isNotEmpty() && lastItemPosition == chatViewModel.mainMessageData.size - 1) {
                                binding.difbDown
                                    .animate()
                                    .translationY((binding.difbDown.height + bottomDistance).toFloat())
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
                                if (binding.floatingLastMessageTV.visibility == View.VISIBLE) {
                                    binding.floatingLastMessageTV.visibility = View.GONE
                                }
                            }
                        }

                        override fun onDragScrolling(recyclerView: RecyclerView) {
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
                                if (binding.floatingLastMessageTV.isVisible) {
                                    doFloatingLastMessageClickAction(binding.floatingLastMessageTV)
                                }
                            }
                        }

                        override fun onAutoScrolling(recyclerView: RecyclerView) {
                        }
                    }
                )
                binding.themeMRV.setContainer(entity)

                binding.chatKeyboardLayout.rootView.viewTreeObserver.addOnGlobalLayoutListener(
                    object : OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            val mRootPreHeight =
                                binding.chatKeyboardLayout.rootView.height
                                    .toFloat()
                            if (mRootPreHeight != 0f) {
                                val layoutParams =
                                    binding.scopeRobotChat.layoutParams as ConstraintLayout.LayoutParams
                                layoutParams.bottomMargin = mRootPreHeight.toInt()
                                binding.scopeRobotChat.requestLayout()
                                binding.scopeRobotChat.invalidate()
                                binding.chatKeyboardLayout.rootView.viewTreeObserver.removeOnGlobalLayoutListener(
                                    this
                                )
                            }
                        }
                    }
                )

                chatViewModel.queryMemberIsBlock()
                initBottomRoomList()
                if (entity.type == ChatRoomType.serviceMember) {
                    initBottomMemberControl()
                }

                if (entity.isService(selfUserId) || entity.isBoos(selfUserId)) {
                    if (ServiceNumberType.BOSS == entity.serviceNumberType) {
                        getIdentityList()
                    }
                    // chatViewModel.doServiceNumberServicedStatus(null)
                }
                binding.ivClose.setOnClickListener { _ ->
                    binding.scopeRobotChat.visibility = View.GONE
                }
                binding.robotChatMessage.settings.builtInZoomControls = true
                binding.robotChatMessage.settings.displayZoomControls = false
                binding.robotChatMessage.settings.domStorageEnabled = true
                binding.robotChatMessage.settings.javaScriptEnabled = true
                binding.robotChatMessage.webViewClient =
                    object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView,
                            url: String
                        ): Boolean {
                            view.loadUrl(url)
                            return true
                        }

                        override fun onPageFinished(
                            view: WebView,
                            url: String
                        ) {
                            super.onPageFinished(view, url)
                            stopIosProgressBar()
                        }
                    }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun initMessageList() {
        chatViewModel.roomEntity?.let {
            val owner = UserProfileReference.findById(null, it.ownerId)
            binding.messageRV.setContainer(chatViewModel.mainMessageData, it, owner)
            binding.messageRV.setOnTouchListener { view, motionEvent -> false }
        }
    }

    fun initKeyboard() {
        binding.chatKeyboardLayout.recorder()
        keyBoardBarListener = KeyBoardBarListener()
        binding.chatKeyboardLayout.setOnKeyBoardBarListener(keyBoardBarListener)
        binding.funMedia.setKeyBoardBarListener(keyBoardBarListener)
        chatViewModel.roomEntity?.let {
            if (selfUserId == it.ownerId && it.serviceNumberOpenType.contains("C")) {
                setChatDisable(R.string.text_consultation_finished_can_not_reply_message)
            }
        }
    }

    fun initListener() {
        binding.messageRV.setOnMessageControlEventListener(
            object :
                OnMainMessageControlEventListener<MessageEntity>() {
                override fun onItemClick(entity: MessageEntity) {
                }

                /**
                 * 範圍選取
                 */
                override fun doRangeSelection(entity: MessageEntity) {
                    buildUpRangeScreenshotData(entity)
                }

                /**
                 * 補漏訓 id to id
                 * @param current  // 比較新
                 * @param previous // 比較舊
                 */
                override fun makeUpMessages(
                    current: MessageEntity,
                    previous: MessageEntity
                ) {
                }

                override fun onItemChange(entity: MessageEntity) {
                }

                override fun onInvalidAreaClick(entity: MessageEntity) {
                    binding.chatKeyboardLayout.showKeyboard()
                }

                override fun onImageClick(entity: MessageEntity) {
                    if (MessageType.BUSINESS == entity.type) {
                        if (activity is ChatActivity) {
                            (activity as ChatActivity).triggerToolbarClick()
                        }
                    } else if (MessageType.IMAGE == entity.type) {
                        chatViewModel.roomEntity?.let {
                            val bundle = Bundle()
                            bundle.putSerializable(BundleKey.PHOTO_GALLERY_MESSAGE.key(), entity)
                            bundle.putString(BundleKey.ROOM_ID.key(), it.id)
                            bundle.putString(BundleKey.CHAT_ROOM_NAME.key(), it.name)
                            bundle.putString(BundleKey.ROOM_TYPE.key(), ChatRoomEnum.NORMAL_ROOM.name)
                            startIntent(
                                requireContext(),
                                MediaGalleryActivity::class.java,
                                bundle
                            )
                        }
                    }
                }

                override fun onLongClick(
                    entity: MessageEntity,
                    pressX: Int,
                    pressY: Int
                ) {
                    if (binding.themeMRV.visibility == View.VISIBLE) {
                        binding.themeMRV.visibility = View.GONE
                    }
                    if (binding.searchBottomBar.visibility == View.VISIBLE) {
                        return
                    }
                    binding.funMedia.visibility = View.GONE
                    chatViewModel.isObserverKeyboard = false
                    actionStatus = ActionStatus.RICH_MENU
                    binding.chatKeyboardLayout.hideKeyboard()
                    binding.chatKeyboardLayout.isOpenFuncView
                    val gridMenus: MutableList<RichMenuBottom> = Lists.newArrayList()

                    val messageType = entity.type
                    if (!Strings.isNullOrEmpty(entity.themeId) && !Strings.isNullOrEmpty(entity.nearMessageContent)) {
                        gridMenus.addAll(setupRichMenuData(RichMenuType.REPLY_RICH, entity))
                    } else if (MessageType.AT == messageType) {
                        gridMenus.addAll(setupRichMenuData(RichMenuType.AT_RICH, entity))
                    } else if (MessageType.TEXT == messageType) {
                        gridMenus.addAll(setupRichMenuData(RichMenuType.TEXT_RICH, entity))
                    } else if (MessageType.VOICE == messageType) {
                        gridMenus.addAll(setupRichMenuData(RichMenuType.VOICE_RICH, entity))
                    } else if (MessageType.FILE == messageType) {
                        gridMenus.addAll(setupRichMenuData(RichMenuType.OTHER_RICH, entity))
                    } else if (MessageType.STICKER == messageType) {
                        gridMenus.addAll(setupRichMenuData(RichMenuType.STICKER_RICH, entity))
                    } else if (MessageType.IMAGE == messageType) {
                        gridMenus.addAll(setupRichMenuData(RichMenuType.IMAGE_RICH, entity))
                    } else if (MessageType.IMAGE_TEXT == messageType) {
                        gridMenus.addAll(setupRichMenuData(RichMenuType.OTHER_RICH, entity))
                    } else if (MessageType.CALL == messageType) {
                        gridMenus.addAll(setupRichMenuData(RichMenuType.CALL_RICH, entity))
                    } else if (MessageType.BUSINESS_TEXT == messageType) {
                        gridMenus.addAll(setupRichMenuData(RichMenuType.TEXT_RICH, entity))
                    } else if (MessageType.VIDEO == messageType) {
                        gridMenus.addAll(setupRichMenuData(RichMenuType.VIDEO_RICH, entity))
                    } else if (MessageType.TEMPLATE == messageType) {
                        gridMenus.addAll(setupRichMenuData(RichMenuType.TEMPLATE_RICH, entity))
                    } else {
                        gridMenus.addAll(setupRichMenuData(RichMenuType.TEXT_RICH, entity))
                    }
                    chatViewModel.roomEntity?.let {
                        if (ChatRoomType.subscribe == it.type ||
                            !UserPref
                                .getInstance(
                                    requireContext()
                                ).hasBusinessSystem()
                        ) {
                            gridMenus.remove(RichMenuBottom.TASK)
                        }

                        // 1. 如果使用者是owner(ios 只有移除諮詢服務號), 請服務號是諮詢服務號則不顯示
                        // 2. 如果是 serviceNumber opentype 有O代表對外, 不顯示
                        if (selfUserId == it.ownerId &&
                            it.serviceNumberOpenType.contains("C") ||
                            (it.serviceNumberOpenType.contains("O") && ChatRoomType.serviceMember != it.type)
                        ) {
                            gridMenus.remove(RichMenuBottom.REPLY)
                        }

                        if (ChatRoomType.services == it.type) {
                            gridMenus.remove(RichMenuBottom.RECOVER)
                        }

                        if (ChatRoomType.system == it.type) {
                            gridMenus.remove(RichMenuBottom.TODO)
                        }

                        binding.chatKeyboardLayout
                            .setRichMenuGridCount(5)
                            .setOnItemClickListener(
                                entity,
                                gridMenus,
                                classifyAiffInMenu(it),
                                object : BottomRichMeunAdapter.OnItemClickListener {
                                    override fun onClick(
                                        msg: MessageEntity,
                                        menu: RichMenuBottom,
                                        position: Int
                                    ) {
                                        binding.chatKeyboardLayout.showKeyboard()
                                        chatViewModel.isObserverKeyboard = true

                                        when (menu) {
                                            RichMenuBottom.MULTI_COPY -> {
                                                msg.isDelete = menu.isMulti
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
                                                msg.isDelete = menu.isMulti
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
                                                executeDelete(msg)
                                                actionStatus = ActionStatus.SCROLL
                                            }

                                            RichMenuBottom.RECOVER -> {
                                                binding.tvTip.text =
                                                    getString(
                                                        R.string.text_retract_tip,
                                                        TokenPref.getInstance(requireActivity()).retractValidMinute
                                                    )
                                                binding.scopeRetractTip.visibility = View.VISIBLE
                                                if (TokenPref.getInstance(requireContext()).retractRemind) {
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
                                                                requireContext()
                                                            ).retractRemind = true
                                                    }
                                                    executeRecover(
                                                        Lists.newArrayList(
                                                            msg
                                                        )
                                                    )
                                                    actionStatus = ActionStatus.SCROLL
                                                    onTipClick(msg)
                                                }
                                                binding.btnRetract.setOnClickListener { v ->
                                                    // 收回
                                                    if (binding.cbTip.isChecked) {
                                                        TokenPref
                                                            .getInstance(
                                                                requireContext()
                                                            ).retractRemind = true
                                                    }
                                                    executeRecover(
                                                        Lists.newArrayList(
                                                            msg
                                                        )
                                                    )
                                                    actionStatus = ActionStatus.SCROLL
                                                }
                                            }

                                            RichMenuBottom.REPLY -> {
                                                executeReply(msg)
                                                actionStatus = ActionStatus.SCROLL
                                            }

                                            RichMenuBottom.SHARE -> {
                                                executeShare(msg)
                                                actionStatus = ActionStatus.SCROLL
                                            }

                                            RichMenuBottom.SCREENSHOTS -> {
                                                msg.isShowSelection = true
                                                screenShotData.add(msg)
                                                binding.xrefreshLayout.setBackgroundColor(-0xadadae)
                                                binding.messageRV.setBackgroundColor(-0xadadae)
                                                if (activity is ChatActivity) {
                                                    (activity as ChatActivity?)!!.showToolBar(false)
                                                }
                                                binding.clBottomServicedBar.visibility = View.GONE
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
                                                msg.isShowSelection = true
                                                screenShotData.add(msg)
                                                binding.xrefreshLayout.setBackgroundColor(-0xadadae)
                                                binding.messageRV.setBackgroundColor(-0xadadae)
                                                if (activity is ChatActivity) {
                                                    (activity as ChatActivity?)!!.showToolBar(false)
                                                }
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
                                                executeTodo(msg)
                                                actionStatus = ActionStatus.SCROLL
                                            }

                                            else -> {}
                                        }
                                    }

                                    override fun onCancle() {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            delay(1000)
                                            chatViewModel.isObserverKeyboard = true
                                            actionStatus = ActionStatus.SCROLL
                                        }
                                    }
                                }
                            ) { _, aiffId ->
                                val aiffInfo =
                                    AiffDB.getInstance(requireContext()).aiffInfoDao.getAiffInfo(aiffId)
                                aiffManager!!.showAiffViewByInfo(aiffInfo)
                            }
                    }
                }

                override fun onAtSpanClick(userId: String) {
                    if (activity is ChatActivity) {
                        val account = DBManager.getInstance().queryFriend(userId)
                        (activity as ChatActivity).toChatRoomByUserProfile(account)
                    }
                }

                override fun onSendNameClick(sendId: String) {
                    binding.chatKeyboardLayout.appendMentionSelectById(sendId)
                }

                override fun onTipClick(entity: MessageEntity) {
                    if (MessageType.AT_or_TEXT.contains(entity.type)) {
                        var input = ""
                        val content: IMessageContent<*> = entity.content()
                        if (content is TextContent) {
                            input = content.simpleContent()
                        } else if (content is AtContent) {
                            chatViewModel.roomEntity?.let {
                                val ceMentions = content.mentionContents
                                val builder =
                                    AtMatcherHelper.matcherAtUsers("@", ceMentions, it.membersTable)
                                input = builder.toString()
                            }
                        }

                        if (!Strings.isNullOrEmpty(input)) {
                            binding.chatKeyboardLayout.clearInputArea()
                            binding.chatKeyboardLayout.setInputHETText(input)
                            KeyboardHelper.open(requireView())
                        }
                    }
                }

                override fun onSubscribeAgentAvatarClick(senderId: String) {
                    if (activity is ChatActivity) {
                        (activity as ChatActivity).navigateToSubscribePage()
                    }
                }

                override fun onAvatarClick(senderId: String) {
                    chatViewModel.roomEntity?.let { entity ->
                        if (ChatRoomType.system == entity.type) {
                            return
                        }
                        UserProfileService.getProfile(
                            requireContext(),
                            RefreshSource.LOCAL,
                            senderId,
                            object : ServiceCallBack<UserProfileEntity, RefreshSource> {
                                override fun complete(
                                    profileEntity: UserProfileEntity,
                                    source: RefreshSource
                                ) {
                                    if (ChatRoomType.services == entity.type) {
                                        ActivityManager.addActivity(activity as ChatActivity)
                                        if (!(requireContext() as ChatActivity).checkClientMainPageFromAiff()) {
                                            ActivityTransitionsControl.navigateToVisitorHomePage(
                                                requireContext(),
                                                entity.ownerId,
                                                profileEntity.roomId,
                                                UserType.VISITOR,
                                                profileEntity.nickName
                                            ) { intent: Intent, s: String? ->
                                                start(
                                                    requireContext(),
                                                    intent.putExtra(
                                                        BundleKey.WHERE_COME.key(),
                                                        profileEntity.name
                                                    )
                                                )
                                            }
                                        }
                                        return
                                    }

                                    if (ChatRoomType.friend == entity.type) {
                                        val userType = profileEntity.userType
                                        if (UserType.VISITOR == userType) {
                                            Toast
                                                .makeText(
                                                    requireContext(),
                                                    getString(R.string.text_no_guest_page),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            return
                                        }
                                        ActivityManager.addActivity(activity as ChatActivity?)
                                        ActivityTransitionsControl.navigateToEmployeeHomePage(
                                            requireContext(),
                                            profileEntity.id,
                                            profileEntity.userType
                                        ) { intent: Intent, s: String? ->
                                            startActivityForResult(
                                                intent,
                                                100
                                            )
                                            isActivityForResult = true
                                        }
                                    } else if (!Strings.isNullOrEmpty(profileEntity.roomId)) {
                                        ActivityTransitionsControl.navigateToChat(
                                            requireActivity(),
                                            profileEntity.roomId
                                        ) { intent: Intent, _: String? ->
                                            start(
                                                requireContext(),
                                                intent
                                            )
                                            requireActivity().finish()
                                        }
                                    } else {
                                        if (!(requireActivity() as ChatActivity).checkClientMainPageFromAiff()) {
                                            ActivityTransitionsControl.navigateToVisitorHomePage(
                                                requireContext(),
                                                entity.ownerId,
                                                profileEntity.roomId,
                                                UserType.VISITOR,
                                                profileEntity.nickName
                                            ) { intent: Intent, s: String? ->
                                                start(
                                                    requireContext(),
                                                    intent.putExtra(
                                                        BundleKey.WHERE_COME.key(),
                                                        profileEntity.name
                                                    )
                                                )
                                            }
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

                override fun onAvatarLoad(
                    iv: ImageView,
                    senderUrl: String?
                ) {
                    if (senderUrl == null) {
                        return
                    }
                    if (URLUtil.isValidUrl(senderUrl)) {
                        Glide
                            .with(requireContext())
                            .load(senderUrl)
                            .apply(
                                RequestOptions()
                                    .placeholder(R.drawable.default_avatar)
                                    .error(R.drawable.default_avatar)
                                    .fitCenter()
                            ).into(iv)
                    }
                }

                override fun onContentUpdate(
                    msgId: String,
                    formatName: String,
                    formatContent: String
                ) {
                    MessageReference.updateMessageFormat(msgId, formatName, formatContent)
                }

                override fun updateReplyMessageWhenVideoDownload(messageId: String?) {
                    chatViewModel.mainMessageData.forEachIndexed { index, messageEntity ->
                        messageEntity.nearMessageId?.let {
                            if (it == messageId) {
                                binding.messageRV.adapter?.notifyItemChanged(index, false)
                            }
                        }
                    }
                }

                override fun copyText(entity: MessageEntity) {
                    executeCopy(Lists.newArrayList(entity))
                }

                override fun replyText(entity: MessageEntity) {
                    executeReply(entity)
                }

                override fun tranSend(entity: MessageEntity) {
                    executeTranspond(Lists.newArrayList(entity))
                }

                override fun retry(entity: MessageEntity) {
                    executeReRetry(entity)
                }

                override fun cellect(entity: MessageEntity) {
                }

                override fun shares(
                    entity: MessageEntity,
                    image: View
                ) {
                    executeShare(entity)
                }

                override fun choice() {
                    showChecked()
                }

                override fun delete(entity: MessageEntity) {
                    executeDelete(entity)
                }

                override fun enLarge(entity: MessageEntity) {
                    ActivityTransitionsControl.navigateToEnLargeMessage(
                        requireContext(),
                        entity
                    ) { intent: Intent, s: String? ->
                        start(
                            requireContext(),
                            intent
                        )
                        requireActivity().overridePendingTransition(
                            R.anim.open_enter,
                            R.anim.open_exit
                        )
                    }
                }

                override fun onPlayComplete(msg: MessageEntity) {
                    val index = chatViewModel.mainMessageData.indexOf(msg)
                    if (chatViewModel.mainMessageData.size - 1 > index) {
                        for (i in index + 1 until chatViewModel.mainMessageData.size) {
                            if (MessageType.VOICE == msg.type && msg.content() is VoiceContent) {
                                val voiceContent = (msg.content() as VoiceContent)
                                if (voiceContent.isRead) {
                                    continue
                                } else {
                                    val holder = binding.messageRV.adapter!!.getHolder(i)
                                    if (holder is VoiceMessageView) {
                                        holder.playLeft()
                                    }
                                    return
                                }
                            }
                        }
                    }
                }

                override fun retractMsg(msg: MessageEntity) {
                    executeRecover(Lists.newArrayList(msg))
                }

                override fun showRePlyPanel(msg: MessageEntity) {
                    showThemeView(msg.themeId)
                }

                override fun findReplyMessage(messageId: String) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val filterData: List<MessageEntity> = chatViewModel.mainMessageData.filter { it.id == messageId }
                        if (filterData.isNotEmpty()) {
                            val message = filterData[0]
                            val index = chatViewModel.mainMessageData.indexOf(message)
                            binding.messageRV.post {
                                message.isAnimator = true
                                (binding.messageRV.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                                    index,
                                    0
                                )
                                binding.messageRV.adapter!!.notifyItemChanged(index)
                            }
                        } else {
                            context?.let {
                                withContext(Dispatchers.Main) {
                                    Toast
                                        .makeText(
                                            it,
                                            getString(R.string.text_can_not_find_message),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                }
                            }
                        }
                    }
                }

                override fun onVideoClick(entity: MessageEntity) {
                    val bundle = Bundle()
                    bundle.putSerializable(BundleKey.PHOTO_GALLERY_MESSAGE.key(), entity)
                    bundle.putString(BundleKey.ROOM_ID.key(), entity.roomId)
                    bundle.putString(BundleKey.ROOM_TYPE.key(), ChatRoomEnum.NORMAL_ROOM.name)
                    startIntent(
                        requireContext(),
                        MediaGalleryActivity::class.java,
                        bundle
                    )
                }

                override fun locationMsg(msg: MessageEntity) {
                }

                override fun onStopOtherVideoPlayback(msg: MessageEntity) {
                    val iterator: Iterator<MessageEntity> =
                        chatViewModel.mainMessageData.iterator()
                    var index = 0
                    while (iterator.hasNext()) {
                        val m = iterator.next()
                        if (MessageType.VIDEO == m.type && m.content() is VideoContent) {
                            if (m != msg && (m.content() as VideoContent).isPlaying) { // 如果不是該視頻訊息且在播放
                                binding.messageRV.adapter!!.notifyItemChanged(index)
                            }
                        }
                        index++
                    }
                }
            }
        )

        binding.messageRV.setOnRobotClickListener(
            object : OnRobotChatMessageClickListener {
                override fun onLinkClickListener(link: String) {
                    if (link.isNotEmpty()) {
                        binding.scopeRobotChat.visibility = View.VISIBLE
                        binding.robotChatMessage.loadUrl(link)
                    }
                }
            }
        )

        binding.messageRV.setOnTemplateClickListener(
            object : OnTemplateClickListener {
                override fun onTemplateClick(content: String) {
                    chatViewModel.roomEntity?.let {
                        chatViewModel.sendQuickReplyMessage(it.id, "Action", content)
                    }
                }
            }
        )

        binding.messageRV.setOnMessageClickListener(
            object : OnMessageSlideReply {
                override fun onMessageSlideReply(messageEntity: MessageEntity) {
                    executeReply(messageEntity)
                }
            }
        )

        binding.messageRV.setOnFacebookPublicReplyClick(
            object : OnFacebookReplyClick {
                override fun onPublicReply(
                    message: MessageEntity,
                    postId: String,
                    commentId: String
                ) {
                    isFacebookReplyPublic = true
                    setFacebookCommentStatus(message)
                    binding.tvFacebookReplyTypeText.text = getString(R.string.facebook_public_reply)
                    binding.chatKeyboardLayout.metaOverTimeView.visibility = View.GONE
                }

                override fun onPrivateReply(message: MessageEntity) {
                    // 超過7天
                    if (isMetaReplyOverTime) {
                        Toast
                            .makeText(
                                requireContext(),
                                getString(R.string.facebook_overtime_toast),
                                Toast.LENGTH_SHORT
                            ).show()
                        return
                    }
                    // 已經回覆過
                    if (message.isFacebookPrivateReplied!!) {
                        Toast
                            .makeText(
                                requireContext(),
                                getString(R.string.facebook_already_private_replied),
                                Toast.LENGTH_SHORT
                            ).show()
                        val facebookTag =
                            JsonHelper.getInstance().from(
                                message.tag,
                                FacebookTag::class.java
                            )
                        chatViewModel.moveToFacebookReplyMessage(
                            message.id,
                            facebookTag.data.commentId,
                            chatViewModel.mainMessageData
                        )
                        return
                    }

                    isFacebookReplyPublic = false
                    setFacebookCommentStatus(message)
                    binding.tvFacebookReplyTypeText.text = getString(R.string.facebook_private_reply)
                }
            }
        )

        binding.messageRV.setCheckCommentStatus(
            object : CheckFacebookCommentStatus {
                override fun checkStatus(message: MessageEntity) {
                    CoroutineScope(Dispatchers.IO).launch {
                        chatViewModel.checkCommentStatus(message).await()
                    }
                }
            }
        )

        binding.chatKeyboardLayout.setFacebookOverTimeViewClickListener {
            Toast
                .makeText(
                    requireContext(),
                    getString(R.string.facebook_overtime_toast),
                    Toast.LENGTH_SHORT
                ).show()
        }
    }

    private fun setFacebookCommentStatus(message: MessageEntity) {
        executeFacebookReply(message)
        binding.chatKeyboardLayout.inputHET.isFocusableInTouchMode = true
        binding.chatKeyboardLayout.inputHET.requestFocus()
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.chatKeyboardLayout.inputHET, InputMethodManager.SHOW_IMPLICIT)
        binding.facebookGroup.visibility = View.VISIBLE
    }

    private fun initBottomMemberControl() {
        chatViewModel.roomEntity?.let {
            if (it.type != ChatRoomType.serviceMember) return@let
            if (App.getInstance().serviceChatRoom == null) return

            val waitTransferDialogBuilder = WaitTransferDialogBuilder(requireContext())
            binding.btnWaitTransfer.setOnClickListener { v ->
                waitTransferDialogBuilder.create().show()
            }
            val currentTransferChatRoom =
                App.getInstance().serviceChatRoom.filter { transferChatRoom ->
                    transferChatRoom.serviceNumberId == it.serviceNumberId && transferChatRoom.isTransferFlag && transferChatRoom.serviceNumberStatus != ServiceNumberStatus.TIME_OUT
                }

            if (currentTransferChatRoom.isNotEmpty()) {
                binding.tvWaitTransferNumber.text = getString(R.string.text_wait_transfer_number, currentTransferChatRoom.size)
                binding.tvWaitTransferNumber.visibility = View.VISIBLE
                binding.clBottomMemberControl.visibility = View.VISIBLE
                waitTransferDialogBuilder.setList(currentTransferChatRoom)
                binding.btnWaitTransfer.performClick()
            } else {
                binding.tvWaitTransferNumber.visibility = View.GONE
            }
        }
    }

    private fun initBottomRoomList() {
        chatViewModel.roomEntity?.let {
            if (binding.rvBottomRoomList.adapter == null) {
                binding.rvBottomRoomList.adapter = advisoryRoomAdapter
            }
            if (ChatRoomType.services == it.type && ChatRoomType.provisional != it.roomType) {
                themeStyle?.let { theme ->
                    chatViewModel.getServiceNumberMemberRoom(
                        it.serviceNumberId,
                        theme.serviceMemberIconResId
                    )
                }
            }

            binding.rvBottomRoomList.layoutManager =
                LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
            chatViewModel.getConsultTodoList(it.type, it.id)
        }
    }

    private fun showChecked() {
        for (message in chatViewModel.mainMessageData) {
            message.isShowChecked = true
        }
        refreshListView()
        setRightView()
    }

    private fun setRightView() {
        val activity = activity as ChatActivity
        activity.setRightView()
    }

    fun hideChecked() {
        for (message in chatViewModel.mainMessageData) {
            message.isShowChecked = false
            message.isDelete = false
        }
        refreshListView()
    }

    fun getInputAreaContent(): SendData = binding.chatKeyboardLayout.inputHET.textData

    fun clearTypedMessage() {
        binding.chatKeyboardLayout.clearInputArea()
    }

    /**
     * 底部浮動信息視窗
     */
    private fun showFloatingLastMessage(message: MessageEntity) {
        chatViewModel.roomEntity?.let {
            var name = message.senderName
            if (selfUserId == message.senderId) {
                name = getString(R.string.text_me)
            }
            var context = message.content().simpleContent()
            if (message.content() is AtContent) {
                try {
                    val ceMentions = (message.content() as AtContent).mentionContents
                    val builder =
                        AtMatcherHelper.matcherAtUsers(
                            "@",
                            ceMentions,
                            it.membersTable
                        )
                    context = builder.toString()
                } catch (e: Exception) {
                    context = getString(R.string.text_marked_message)
                }
            }
            if (!Strings.isNullOrEmpty(context)) {
                binding.floatingLastMessageTV.visibility = View.VISIBLE
                binding.floatingLastMessageTV.text =
                    MessageFormat.format("{0}: {1}", name, context)
            }
        }
    }

    fun onRefreshMore(
        entities: MutableList<MessageEntity>,
        lastMsg: MessageEntity
    ) {
        if (entities.isEmpty() || entities.size <= 1) {
            stopRefresh()
            return
        }
        chatViewModel.roomEntity?.let {
            currentDate = messageTimeLineFormat.format(776188800)
            for (msg in entities) {
                if (chatViewModel.mainMessageData.contains(msg) && msg.senderId != null) {
                    continue
                }

                val date = messageTimeLineFormat.format(msg.sendTime)
                if (date != currentDate) {
                    currentDate = date
                    val dayBegin = TimeUtil.getDayBegin(msg.sendTime)
                    val timeMessage =
                        MessageEntity
                            .Builder()
                            .id(Tools.generateTimeMessageId(dayBegin))
                            .roomId(it.id)
                            .status(MessageStatus.SUCCESS)
                            .sourceType(SourceType.SYSTEM)
                            .content(UndefContent("TIME_LINE").toStringContent())
                            .sendTime(dayBegin)
                            .build()
                    timeMessage.sendTime = dayBegin
                    if (!chatViewModel.mainMessageData.contains(timeMessage)) {
                        chatViewModel.mainMessageData.add(timeMessage)
                    }
                }

                if (chatViewModel.mainMessageData.indexOf(msg) == -1) {
                    chatViewModel.mainMessageData.add(msg)
                }
            }

            chatViewModel.mainMessageData.sortBy { it.sendTime }
            val index = chatViewModel.mainMessageData.indexOf(lastMsg)

            if (index <= 0) {
                stopRefresh()
                return
            }
            stopRefresh()
            chatViewModel.checkFacebookReplyType(entities)
            Handler(Looper.getMainLooper()).post {
                binding.messageRV.refreshToPosition(index - 1)
            }

            if (binding.scopeSearch.visibility == View.VISIBLE) {
                chatViewModel.loadMoreBySearch(keyWord, chatViewModel.mainMessageData)
            }
        }
    }

    fun showToast(resId: Int) {
        Toast.makeText(requireContext(), resId, Toast.LENGTH_SHORT).show()
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
                content.progress =
                    progress.toString() + ""
            }
            if (content is VideoContent) {
                content.progress = progress.toString() + ""
            }

            message.content = content.toStringContent()
            binding.messageRV.refreshData(index, message)
        }
    }

    fun updateMsgStatus(
        messageId: String,
        sendNum: Int
    ) {
        val index =
            chatViewModel.mainMessageData.indexOf(MessageEntity.Builder().id(messageId).build())
        if (index < 0) {
            return
        }

        val iMessage = chatViewModel.mainMessageData[index]
        if (sendNum < 0) {
            iMessage.status = MessageStatus.FAILED
        } else {
            iMessage.status = MessageStatus.SUCCESS
        }
        iMessage.sendNum = sendNum
        chatViewModel.displayMainMessageLogic(isNew = false, scrollToBottom = false, iMessage)
    }

    fun updateMsgNotice(
        messageId: String,
        receivedNum: Int,
        readNum: Int,
        sendNum: Int
    ) {
        val index =
            chatViewModel.mainMessageData.indexOf(MessageEntity.Builder().id(messageId).build())
        if (index < 0) {
            return
        }
        val message = chatViewModel.mainMessageData[index]
        val oldReadNum = message.readedNum!!
        val oldReceivedNum = message.receivedNum!!
        val oldSendNum = message.sendNum!!

        if (receivedNum > oldReceivedNum) {
            message.receivedNum = receivedNum
            DBManager.getInstance().updateReceivedNum(messageId, receivedNum)
        }

        if (readNum > oldReadNum) {
            message.readedNum = readNum
            DBManager.getInstance().updateReadNum(messageId, readNum)
        }
        if (sendNum > oldSendNum) {
            message.sendNum = sendNum
            MessageReference.save(message.roomId, message)
        }
        chatViewModel.roomEntity?.let {
            if (it.type == ChatRoomType.group || it.type == ChatRoomType.discuss) {
                message.status = MessageStatus.SUCCESS
                DBManager.getInstance().updateMessageStatus(messageId, MessageStatus.SUCCESS)
            } else {
                if (readNum > 0) {
                    message.status = MessageStatus.READ
                    DBManager.getInstance().updateMessageStatus(messageId, MessageStatus.READ)
                } else if (readNum == 0 && receivedNum > 0) {
                    message.status = MessageStatus.RECEIVED
                    DBManager.getInstance().updateMessageStatus(messageId, MessageStatus.RECEIVED)
                } else {
                    message.status = MessageStatus.SUCCESS
                    DBManager.getInstance().updateMessageStatus(messageId, MessageStatus.SUCCESS)
                }
            }
            chatViewModel.refreshCurrentMessage(messageId)
        }
    }

    fun deleteMessage(messageId: String) {
        chatViewModel.roomEntity?.let {
            val index = chatViewModel.mainMessageData.indexOf(MessageEntity.Builder().id(messageId).build())
            if (index > -1) {
                chatViewModel.mainMessageData.removeAt(index)
                val lastMessageEntity =
                    MessageReference.findMessageByRoomIdAndStatusAndLimitOne(
                        null,
                        it.id,
                        MessageStatus.getValidStatus(),
                        MessageReference.Sort.DESC
                    )
                it.lastMessage = lastMessageEntity
                refreshListView()
            }
        }
    }

    // ---- 主題聊天室相關 ---------------------------------------------------------------------------------------------------------------------------------------------------

    /**
     * According to the current incoming theme Id query the current data belongs to the theme-related message
     */
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
     * 顯示主題聊天室
     */
    private fun showThemeView(themeId: String?) {
        if (binding.searchBottomBar.visibility == View.VISIBLE) {
            return
        }

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
     * 取得主題訊息
     */
    fun getThemeMessage(): MessageEntity? =
        if (chatViewModel.isThemeOpen) {
            binding.themeMRV.themeData
        } else {
            null
        }

    /**
     * 如果主題訊息存在，取得最有一筆
     */
    fun getNearMessage(): MessageEntity? =
        if (chatViewModel.isThemeOpen) {
            binding.themeMRV.nearData
        } else {
            null
        }

    fun isThemeOpen(): Boolean = chatViewModel.isThemeOpen

    fun getChildRoomId(): String = ""

    fun isChildRoomOpen(): Boolean = false

    fun displayChildRoomMessage(
        roomId: String,
        message: MessageEntity
    ) {
    }

    // 子聊天室 根據設定是否自動關閉
    fun setThemeOpen(isThemeOpen: Boolean) {
        if (chatViewModel.isThemeOpen) {
            if (TokenPref.getInstance(requireContext()).isAutoCloseSubChat) {
                doThemeCloseAction()
                chatViewModel.isThemeOpen = isThemeOpen
            } else {
                executeReply(binding.themeMRV.nearData)
            }
        }
    }

    fun displayLogos(logoBeans: List<Int>) {
    }

    override fun onMediaClick(i: Int) {
        if (i == R.drawable.plugin_file) {
            this.isActivityForResult = true
            sendFileOpenResult.launch(
                Intent(requireContext(), FileExplorerActivity::class.java)
            )
        }
        requireActivity().overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.let { intentData ->
            if (requestCode == 100 && resultCode == 101) {
                mIsBlock = intentData.getBooleanExtra("isBlock", false)
                chatViewModel.roomEntity?.let {
                    if (it.type == ChatRoomType.friend) {
                        binding.chatKeyboardLayout.setIsBlock(mIsBlock)
                    }
                }
            }
            if (resultCode == Activity.RESULT_OK) {
                when (requestCode) {
                    SHARE_SCREENSHOTS_RESULT_CODE -> {
                        val screenshotsPath = requireArguments().getString(BundleKey.FILE_PATH.key())
                        val roomIdsArray = intentData.getStringArrayExtra(Constant.ACTIVITY_RESULT)
                        val roomIdsList: List<String> = roomIdsArray?.filterNotNull() ?: emptyList()
                        ChatRoomService.getInstance().checkRoomEntities(
                            requireContext(),
                            roomIdsArray,
                            object : ServiceCallBack<List<String>, Enum<*>> {
                                override fun complete(
                                    strings: List<String>,
                                    anEnum: Enum<*>
                                ) {
                                    chatViewModel.sendScreenshotsImageToRooms(
                                        roomIdsList,
                                        screenshotsPath
                                    )
                                }

                                override fun error(message: String) {
                                }
                            }
                        )
                    }

                    MEDIA_SELECTOR_REQUEST_CODE -> requireArguments().putBundle("PREVIEW", intentData.extras)
                }
            }
        }
    }

    override fun onRefresh() {
        if (chatViewModel.mainMessageData.isNotEmpty()) {
            val msg = chatViewModel.getFirstMsgId()
            chatViewModel.refreshMoreMsg(msg)
        } else {
            chatViewModel.refreshMoreMsg(null)
            stopRefresh()
        }
    }

    fun onCleanMsgs() {
        chatViewModel.mainMessageData.clear()
        refreshListView()
    }

    private val clickListener: NoDoubleClickListener =
        object : NoDoubleClickListener() {
            override fun onNoDoubleClick(v: View) {
                if (v == binding.difbDown) {
                    doScrollToBottomAction()
                } else if (v == binding.ivConsult) {
                    doConsultListAction()
                } else if (v == binding.expandIV) {
                    doThemeExpandAction(v)
                } else if (v == binding.themeCloseIV) {
                    doThemeCloseAction()
                } else if (v == binding.floatingLastMessageTV) {
                    doFloatingLastMessageClickAction(v)
                } else if (v == binding.ivRobotRecord || v == binding.ivRobotRecord2) {
                    showRobotChatRecord(robotChatRecord)
                }
            }
        }

    @SuppressLint("ClickableViewAccessibility")
    private fun init() {
        chatViewModel.roomEntity?.let {
            App.getInstance().chatRoomId = it.id
        }
        binding.difbDown.setOnClickListener(clickListener)
        binding.ivConsult.setOnClickListener(clickListener)
        binding.expandIV.setOnClickListener(clickListener)
        binding.themeCloseIV.setOnClickListener(clickListener)
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
        binding.floatingLastMessageTV.setOnClickListener(clickListener)
        binding.ivRobotRecord.setOnClickListener(clickListener)
        binding.ivRobotRecord2.setOnClickListener(clickListener)
    }

    /**
     * 滑動到最底部控件
     */
    fun doScrollToBottomAction() {
        if (binding.messageRV.scrollState != 0) {
            return
        }
        val bottomDistance = UiHelper.dip2px(requireActivity(), 10f)
        binding.difbDown
            .animate()
            .translationY((binding.difbDown.height + bottomDistance).toFloat())
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
        binding.messageRV.scrollToPosition(binding.messageRV.adapter!!.itemCount - 1)
        if (binding.floatingLastMessageTV.visibility == View.VISIBLE) {
            binding.floatingLastMessageTV.visibility = View.GONE
        }
    }

    private fun setupAppointStatus(appointResp: FromAppointRequest.Resp?) {
        if (!isAdded) return
        if (appointResp == null) {
            return
        }

        val lastFrom = appointResp.lastFrom
        if (appointResp.otherFroms == null) {
            appointResp.otherFroms = Sets.newHashSet()
        }
        appointResp.otherFroms.add(lastFrom)

        if (ServiceNumberStatus.OFF_LINE == appointResp.status || ServiceNumberStatus.TIME_OUT == appointResp.status) {
            // 商務號擁者停止服務
            chatViewModel.roomEntity?.let {
                if (ServiceNumberType.BOSS == it.serviceNumberType && selfUserId == it.serviceNumberOwnerId) {
                    if (it.isServiceNumberOwnerStop) {
                        setAppointOfflineStatus(lastFrom)
                    }
                } else {
                    setAppointOfflineStatus(lastFrom)
                }
            }
        }

        if (lastFrom != null) {
            @DrawableRes val resId =
                when (lastFrom) {
                    ChannelType.LINE -> R.drawable.ic_line
                    ChannelType.FB -> R.drawable.ic_fb
                    ChannelType.AILE_WEB_CHAT, ChannelType.QBI, ChannelType.AIWOW -> R.drawable.qbi_icon
                    ChannelType.WEICHAT -> R.drawable.wechat_icon
                    ChannelType.GOOGLE -> R.drawable.ic_google_message
                    ChannelType.IG -> R.drawable.ic_ig
                    else -> R.drawable.ce_icon
                }
            if (activity is ChatActivity) {
                (activity as ChatActivity).setChannelIconVisibility(resId, appointResp.status)
            }
        }
    }

    fun updateFacebookStatus(message: MessageEntity) {
        binding.messageRV.notifyChange(message)
        if (chatViewModel.isThemeOpen) {
            binding.themeMRV.notifyChange(message)
        }
    }

    fun moveToFacebookReplyMessage(position: Int) {
        binding.messageRV.post {
            (binding.messageRV.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                position,
                0
            )
            binding.messageRV.adapter?.notifyItemChanged(position)
        }
    }

    private fun setAppointOfflineStatus(lastFrom: ChannelType?) {
        lastFrom?.let { from ->
            binding.civServicedAgentAvatar.visibility = View.GONE
            binding.ivServicedStatus.visibility = View.GONE
            binding.tvServicedDuration.visibility = View.GONE
            clearProvisionalMember()
            if (ChannelType.QBI == from) {
                setChatDisable(R.string.text_qbi_channel_not_support_offline_reply)
            }
        }
    }

    fun doChannelChangeAction() {
        chatViewModel.appointResp?.let { resp ->
            if (ServiceNumberStatus.OFF_LINE_or_TIME_OUT.contains(resp.status)) {
                val availableChannels: MutableList<ChannelType> =
                    Lists.newArrayList(
                        resp.otherFroms
                    )
                availableChannels.remove(resp.lastFrom)

                if (availableChannels.size == 0) {
                    Toast.makeText(requireActivity(), getString(R.string.text_can_not_change_to_other_channel), Toast.LENGTH_SHORT).show()
                } else {
                    val channelList: MutableList<ChannelEntity> = mutableListOf()
                    availableChannels.sortWith { s1: ChannelType, s2: ChannelType -> s1.index - s2.index }
                    for (t in availableChannels) {
                        when (t) {
                            WEICHAT ->
                                channelList.add(
                                    ChannelEntity(
                                        WEICHAT,
                                        "We Chat",
                                        R.drawable.wechat_icon
                                    )
                                )

                            ChannelType.AILE_WEB_CHAT ->
                                channelList.add(
                                    ChannelEntity(
                                        ChannelType.AILE_WEB_CHAT,
                                        "Aile Web Chat",
                                        R.drawable.qbi_icon
                                    )
                                )

                            ChannelType.QBI ->
                                channelList.add(
                                    ChannelEntity(
                                        ChannelType.QBI,
                                        "Qbi",
                                        R.drawable.qbi_icon
                                    )
                                )

                            ChannelType.AIWOW ->
                                channelList.add(
                                    ChannelEntity(
                                        ChannelType.AIWOW,
                                        "AiWow",
                                        R.drawable.qbi_icon
                                    )
                                )

                            ChannelType.FB ->
                                channelList.add(
                                    ChannelEntity(
                                        ChannelType.FB,
                                        "Facebook",
                                        R.drawable.ic_fb
                                    )
                                )

                            ChannelType.LINE ->
                                channelList.add(
                                    ChannelEntity(
                                        ChannelType.LINE,
                                        "Line",
                                        R.drawable.ic_line
                                    )
                                )

                            ChannelType.GOOGLE ->
                                channelList.add(
                                    ChannelEntity(
                                        ChannelType.GOOGLE,
                                        "Google",
                                        R.drawable.ic_google_message
                                    )
                                )

                            ChannelType.IG ->
                                channelList.add(
                                    ChannelEntity(
                                        ChannelType.IG,
                                        "Instagram",
                                        R.drawable.ic_ig
                                    )
                                )

                            else ->
                                channelList.add(
                                    ChannelEntity(
                                        ChannelType.CE,
                                        "Aile",
                                        R.drawable.ce_icon
                                    )
                                )
                        }
                    }
                    BottomSheetDialogBuilder(requireContext(), layoutInflater)
                        .getSwitchReplyChannelDialog(
                            channelList,
                            { channelType: ChannelType ->
                                chatViewModel.doSwitchChannel(channelType)
                            }
                        ).show()
                }
            }
        }
    }

    var isAlreadyCount: Boolean = false

    private fun countServiceDuration() {
        if (!isAlreadyCount) {
            isAlreadyCount = true
            binding.tvServicedDuration.post(servicedDuration)
        }
    }

    private val servicedDuration: Runnable by lazy {
        Runnable {
            val startTime = binding.tvServicedDuration.tag as Long
            if (servicedDurationTime != 0L) {
                val duration = servicedDurationTime - startTime
                binding.tvServicedDuration.text = DateTimeHelper.convertSecondsToHMmSs(duration)
                servicedDurationTime += 1000L
                binding.tvServicedDuration.postDelayed(servicedDuration, 1000L)
            }
        }
    }

    private lateinit var transferDialog: Dialog

    private fun createTransferDialog(onSubmitListener: OnSubmitListener): Dialog =
        TransferDialogBuilder(activity)
            .setOnSubmitListener(onSubmitListener)
            .create()

    private fun disableInputBarForChannelIsOffline(resp: ServiceNumberChatroomAgentServicedRequest.Resp) {
        resp.lastFrom?.let { lastFrom ->
            if (lastFrom == ChannelType.AILE_WEB_CHAT &&
                resp.serviceNumberStatus != ServiceNumberStatus.ON_LINE
            ) {
                binding.chatKeyboardLayout.setKeyboardDisabled(
                    true,
                    getString(R.string.text_boss_exit_room_cannot_reply)
                )
            } else if (lastFrom == ChannelType.FB &&
                resp.serviceNumberStatus != ServiceNumberStatus.ON_LINE &&
                chatViewModel.appointResp?.isLastMessageTimeOut == true
            ) {
                binding.chatKeyboardLayout.setKeyboardDisabled(
                    true,
                    getString(R.string.text_exceeded_fb_messenger_24_hour_time_limit)
                )
            } else {
                binding.chatKeyboardLayout.setKeyboardDisabled(false, "")
            }
        } ?: run {
            binding.chatKeyboardLayout.setKeyboardDisabled(false, "")
        }
    }

    private fun setRobotChatRecord(resp: ServiceNumberChatroomAgentServicedRequest.Resp) {
        // 沒有啟用機器人服務 所以不顯示按鈕
        // ivRobotRecord 是諮詢中要顯示的 icon
        // ivRobotRecord2 是結束服務後要顯示的 icon
        chatViewModel.roomEntity?.let {
            if (resp.robotChatRecordLink == null || resp.robotChatRecordLink.isEmpty()) {
                binding.ivRobotRecord2.visibility = View.GONE
                binding.ivRobotRecord.visibility = View.GONE
            } else {
                if (ServiceNumberType.BOSS == it.serviceNumberType) {
                    if (ServiceNumberStatus.OFF_LINE_or_TIME_OUT.contains(resp.serviceNumberStatus)) {
                        if (it.serviceNumberOwnerId == selfUserId) {
                            if (resp.isServiceNumberOwnerStop) {
                                binding.ivRobotRecord2.visibility = View.VISIBLE
                                binding.ivRobotRecord.visibility = View.GONE
                            } else {
                                binding.ivRobotRecord2.visibility = View.GONE
                                binding.ivRobotRecord.visibility = View.VISIBLE
                            }
                        }
                    } else {
                        binding.ivRobotRecord2.visibility = View.GONE
                        binding.ivRobotRecord.visibility = View.VISIBLE
                    }
                } else {
                    if (ServiceNumberStatus.OFF_LINE_or_TIME_OUT.contains(resp.serviceNumberStatus)) {
                        binding.ivRobotRecord2.visibility = View.VISIBLE
                        binding.ivRobotRecord.visibility = View.GONE
                    } else if (ServiceNumberStatus.ON_LINE == resp.serviceNumberStatus) {
                        binding.ivRobotRecord2.visibility = View.GONE
                        binding.ivRobotRecord.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    // 設定服務號的控制bar, 換手按鈕
    private fun setupBottomServicedStatus(
        status: ServiceNumberStatus,
        type: ServiceNumberType,
        resp: ServiceNumberChatroomAgentServicedRequest.Resp?
    ) {
        resp?.let { resP ->

            resP.robotChatRecordLink?.let { link ->
                robotChatRecord = link
            }
            // 如果服務號 timeout 會出現 icon 重疊
            if (status == ServiceNumberStatus.TIME_OUT) {
                // 如果是商務號擁有者 還需要擁有者停止
                if (!resp.isServiceNumberOwnerStop && type != ServiceNumberType.BOSS) {
                    return
                }
            }
            binding.civServicedAgentAvatar.setOnClickListener(null)
            binding.ivTransfer.setOnClickListener(null)
            binding.ivTransfer.visibility = View.GONE
            binding.tvServicedDuration.tag = 0L
            binding.ivServicedStatus.setImageResource(R.drawable.circle_reeen_bg)
            disableInputBarForChannelIsOffline(resP)

            val serviceNumberAgentId = resP.serviceNumberAgentId

            binding.civServicedAgentAvatar.setOnClickListener(
                object : NoDoubleClickListener() {
                    override fun onNoDoubleClick(v: View) {
                        doAgentStopService(resP)
                    }
                }
            )
            chatViewModel.roomEntity?.let { chatRoom ->
                when (status) {
                    ServiceNumberStatus.ON_LINE -> {
                        // 商務號擁有者
                        if ((type == ServiceNumberType.BOSS && !resp.isServiceNumberOwnerStop && chatRoom.serviceNumberOwnerId == selfUserId) || !isProvisionMember) {
                            val textTooltips: MutableSet<String> = Sets.newLinkedHashSet()
                            textTooltips.add("prompt_close_serviced")
                            /** 2023/11/22 ID1005663
                             * 調整只有 專業服務號 正在服務中的人員才會出現咨詢功能的圖示
                             * 其他服務號一直顯示
                             */
                            if (ServiceNumberType.PROFESSIONAL == chatRoom.serviceNumberType) {
                                binding.ivConsult.visibility =
                                    if (selfUserId == serviceNumberAgentId) View.VISIBLE else View.GONE
                            } else {
                                binding.ivConsult.visibility = View.VISIBLE
                            }

                            binding.clBottomServicedControl.visibility = View.VISIBLE
                            var agentName = UserProfileReference.findAccountName(null, serviceNumberAgentId)
                            if (agentName == null) agentName = ""
                            val avatarId = UserProfileReference.findAccountAvatarId(null, resP.serviceNumberAgentId)
                            if (ServiceNumberType.PROFESSIONAL == type) {
                                if (resP.serviceNumberAgentId != null) {
                                    binding.civServicedAgentAvatar.loadAvatarIcon(
                                        avatarId,
                                        agentName,
                                        serviceNumberAgentId
                                    )
                                }
                            } else {
                                binding.civServicedAgentAvatar.loadAvatarIcon(
                                    chatRoom.serviceNumberAvatarId,
                                    agentName,
                                    serviceNumberAgentId
                                )
                            }

                            if (ServiceNumberType.BOSS == chatRoom.serviceNumberType) {
                                binding.tvServicedDuration.visibility = View.GONE
                            } else {
                                // 如果是主管號
                                if (resP.startTime > 0) {
                                    TimeUtil.getNetworkTime(this)
                                    binding.tvServicedDuration.tag = resP.startTime
                                    binding.tvServicedDuration.visibility = View.VISIBLE
                                } else {
                                    binding.tvServicedDuration.tag = 0
                                    binding.tvServicedDuration.removeCallbacks(servicedDuration)
                                }
                            }

                            if (ServiceNumberType.PROFESSIONAL != chatRoom.serviceNumberType) {
                                return
                            }

                            if (!Strings.isNullOrEmpty(serviceNumberAgentId) && selfUserId != serviceNumberAgentId) {
                                binding.ivTransfer.setOnClickListener(null)
                                binding.ivTransfer.visibility = View.GONE
                            }
                            // 強制接手後狀態
                            if (!resP.isTransferFlag && selfUserId == serviceNumberAgentId) {
                                binding.ivTransfer.visibility = View.VISIBLE
                                textTooltips.add("prompt_post_transfer")
                                binding.ivTransfer.setImageResource(R.drawable.icon_serviced_relay_green)
                                binding.ivTransfer.setOnClickListener { _ ->
                                    transferDialog =
                                        createTransferDialog { message: String ->
                                            binding.ivServicedStatus.setImageResource(R.drawable.circle_blue_bg)
                                            doServiceNumberTransfer(
                                                ServicedTransferType.TRANSFER,
                                                resP,
                                                message
                                            )
                                            transferDialog.dismiss()
                                            Toast
                                                .makeText(requireActivity(), getString(R.string.text_waiting_for_snatch_service), Toast.LENGTH_SHORT)
                                                .show()
                                        }
                                    if (!transferDialog.isShowing) transferDialog.show()
                                }
                            }
                            // 發起換手後的狀態
                            if (resP.isTransferFlag && selfUserId == serviceNumberAgentId) {
                                binding.ivTransfer.visibility = View.VISIBLE
                                binding.ivServicedStatus.setImageResource(R.drawable.circle_blue_bg)
//                    binding.ivConsult.visibility = View.GONE
                                binding.ivTransfer.setImageResource(R.drawable.icon_serviced_relay_cancel_green)
                                binding.ivTransfer.setOnClickListener { _ ->
                                    binding.ivServicedStatus.setImageResource(R.drawable.circle_reeen_bg)
                                    doServiceNumberTransfer(
                                        ServicedTransferType.TRANSFER_CANCEL,
                                        resP,
                                        ""
                                    )
                                }
                            }
                            // 其他成員發起換手的狀態
                            if (resP.isTransferFlag && !Strings.isNullOrEmpty(serviceNumberAgentId) && selfUserId != serviceNumberAgentId) {
                                binding.ivTransfer.visibility = View.VISIBLE
                                textTooltips.add("prompt_handling_transfer")
                                binding.ivServicedStatus.setImageResource(R.drawable.circle_blue_bg)
                                binding.ivTransfer.setImageResource(R.drawable.icon_serviced_relay_blue)
                                binding.ivTransfer.setOnClickListener { v ->
                                    AlertView
                                        .Builder()
                                        .setContext(requireContext())
                                        .setStyle(AlertView.Style.Alert)
                                        .setMessage(getString(R.string.text_tip_waiting_for_snatch_service, chatRoom.serviceNumberName))
                                        .setOthers(arrayOf(getString(R.string.cancel), getString(R.string.text_sure)))
                                        .setOnItemClickListener { o: Any?, position: Int ->
                                            if (position == 1) {
                                                binding.ivServicedStatus.setImageResource(R.drawable.circle_reeen_bg)
                                                doServiceNumberTransfer(
                                                    ServicedTransferType.TRANSFER_COMPLETE,
                                                    resP,
                                                    ""
                                                )
                                            }
                                        }.build()
                                        .setCancelable(true)
                                        .show()
                                }
                            }
                            handleMultipleTextTooltips(Queues.newLinkedBlockingQueue(textTooltips))
                        } else {
                            binding.clBottomServicedControl.visibility = View.GONE
                            binding.ivConsult.visibility = View.GONE
                        }
                    }

                    ServiceNumberStatus.OFF_LINE,
                    ServiceNumberStatus.TIME_OUT
                    -> {
                        if (ServiceNumberType.BOSS == type && chatRoom.serviceNumberOwnerId == selfUserId && !resp.isServiceNumberOwnerStop) {
                            binding.clBottomServicedControl.visibility = View.VISIBLE
                            binding.ivConsult.visibility = View.VISIBLE
                        } else {
                            binding.clBottomServicedControl.visibility = View.GONE
                            binding.ivConsult.visibility = View.GONE
                        }
                    }

                    else -> {
                        binding.clBottomServicedControl.visibility = View.GONE
                        binding.ivConsult.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun showRobotChatRecord(link: String) {
        binding.ivIcon.setImageResource(R.drawable.ic_robot_service)
        binding.tvTitle.text = getString(R.string.text_robot_charting_record)
        binding.ivClose.visibility = View.VISIBLE
        binding.robotChatMessage.loadUrl(link)

        val params = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, 0)
        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        params.bottomToBottom = binding.robotChatMessageGuideline.id
        binding.scopeRobotChat.layoutParams = params
        binding.scopeRobotChat.invalidate()
        binding.scopeRobotChat.requestLayout()
        binding.scopeRobotChat.visibility =
            if (binding.scopeRobotChat.visibility == View.GONE) View.VISIBLE else View.GONE
        KeyboardHelper.hide(view)
    }

    private fun setAiServices() {
        if (activity is ChatActivity) (activity as ChatActivity).showTopMenu(false)
        if (chatViewModel.roomEntity?.isWarned == true) {
            // Ai 監控中
            binding.tvTitle.text = getString(R.string.service_room_sectioned_monitor_ai_service)
            binding.ivIcon.setImageResource(R.mipmap.ic_monitor_ai)
            binding.ivTurnToAiServices.visibility = View.VISIBLE
            binding.ivTurnToAiServices.setOnClickListener { _ ->
                UserPref.getInstance(requireActivity()).setServiceRoomIntroChannel(
                    true,
                    "turn_to_ai_services"
                )
                showTurnMonitorAiToAiServicingDialog()
            }
            val textTooltips: MutableSet<String> = Sets.newLinkedHashSet()
            textTooltips.add("turn_to_ai_services")
            handleMultipleTextTooltips(Queues.newLinkedBlockingQueue(textTooltips))
        } else {
            binding.ivTurnToAiServices.visibility = View.GONE
            binding.tvTitle.text = getString(R.string.service_room_sectioned_robot_service)
            binding.ivIcon.setImageResource(R.drawable.ic_robot_service)
        }
        binding.scopeRobotChat.visibility = View.VISIBLE
    }

    private fun showTurnMonitorAiToAiServicingDialog() {
        AlertView
            .Builder()
            .setContext(requireContext())
            .setStyle(AlertView.Style.Alert)
            .setMessage(getString(R.string.turn_to_ai_servicing))
            .setOthers(arrayOf(getString(R.string.cancel), getString(R.string.text_sure)))
            .setOnItemClickListener { o: Any?, position: Int ->
                if (position == 1) {
                    chatViewModel.roomEntity?.let {
                        chatViewModel.cancelAiWarning(it.id)
                    }
                }
            }.build()
            .setCancelable(false)
            .show()
    }

    private fun doServiceNumberTransfer(
        type: ServicedTransferType,
        resp: ServiceNumberChatroomAgentServicedRequest.Resp,
        reason: String
    ) {
        ChatServiceNumberService.servicedTransferHandle(
            requireActivity(),
            type,
            resp.roomId,
            reason,
            object : ServiceCallBack<String, ServicedTransferType> {
                override fun complete(
                    s: String,
                    type: ServicedTransferType
                ) {
                    when (type) {
                        ServicedTransferType.TRANSFER_SNATCH -> {
                            resp.setSnatch(true)
                            resp.isTransferFlag = true
                            resp.serviceNumberAgentId = selfUserId
                        }

                        ServicedTransferType.TRANSFER -> {
                            resp.isTransferFlag = true
                            resp.setSnatch(false)
                        }

                        ServicedTransferType.TRANSFER_CANCEL -> {
                            resp.isTransferFlag = false
                            resp.setSnatch(false)
                        }

                        ServicedTransferType.TRANSFER_COMPLETE -> {
                            resp.isTransferFlag = false
                            resp.setSnatch(false)
                            resp.serviceNumberAgentId = selfUserId
                        }
                    }
                    if (resp.istSnatch()) {
                        // 多了這個判斷是為了修正強制接手後 換手圖示更新錯誤問題
                        setup()
                    } else {
                        chatViewModel.roomEntity?.let {
                            setupBottomServicedStatus(
                                resp.serviceNumberStatus,
                                it.serviceNumberType,
                                resp
                            )
                        }
                    }
                }

                override fun error(message: String) {
                    Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // 第一次開始服務號會出現的提醒視窗
    private fun handleMultipleTextTooltips(textTooltipQueue: Queue<String>) {
        if (textTooltipQueue.isEmpty() || activity == null) {
            return
        }

        val target = textTooltipQueue.remove()
        if (!UserPref.getInstance(requireActivity()).isServiceRoomIntroChannel(target)) {
            val anchorView: View
            val text =
                when (target) {
                    "prompt_close_serviced" -> {
                        anchorView = binding.civServicedAgentAvatar
                        getString(R.string.text_servicing_can_end_service)
                    }

                    "prompt_post_transfer" -> {
                        anchorView = binding.ivTransfer
                        getString(R.string.text_snatch_service)
                    }

                    "turn_to_ai_services" -> {
                        anchorView = binding.ivTurnToAiServices
                        getString(R.string.turn_to_ai_servicing_hint)
                    }

                    else -> {
                        anchorView = binding.ivTransfer
                        getString(R.string.text_waiting)
                    }
                }

            SimpleTextTooltip
                .Builder(requireActivity())
                .anchorView(anchorView)
                .backgroundColor(Color.BLACK)
                .arrowColor(Color.BLACK)
                .textColor(Color.WHITE)
                .textSize(13.0f)
                .dismissOnInsideTouch(true)
                .dismissOnOutsideTouch(false)
                .text(text)
                .gravity(Gravity.TOP)
                .animated(false)
                .arrowHeight(7f)
                .arrowWidth(10f)
                .transparentOverlay(true)
                .dismissTimer(3000L) // 定時器關閉
                .onDismissListener {
                    if (target != "turn_to_ai_services") {
                        UserPref.getInstance(requireActivity()).setServiceRoomIntroChannel(
                            true,
                            target
                        )
                        handleMultipleTextTooltips(textTooltipQueue)
                    }
                }.build()
                .show()
        }
    }

    /**
     * Consultation function, first navigate to the consultation list
     */
    fun doConsultListAction() {
        binding.funMedia.visibility = View.GONE
        if (activity is ChatActivity) {
            (activity as ChatActivity).doSearchCancelAction()
        }
        val bundle = Bundle()
        chatViewModel.roomEntity?.let {
            bundle.putString(BundleKey.ROOM_ID.key(), it.id)
            bundle.putString(BundleKey.SERVICE_NUMBER_ID.key(), it.serviceNumberId)
            bundle.putString(BundleKey.ROOM_TYPE.key(), InvitationType.ServiceNUmberConsultationAI.name)
            bundle.putString(BundleKey.CONSULT_AI_ID.key(), it.aiConsultId)
            launchIntent(
                requireContext(),
                MemberInvitationActivity::class.java,
                consultSelectResult,
                bundle
            )
        }
    }

    // 選擇 Ai 諮詢
    private fun onAiConsultSelected(consultId: String) {
        chatViewModel.roomEntity?.let {
            chatViewModel.addAiConsultRoom(consultId, it.id, it.serviceNumberId)
        }
    }

    // 諮詢選擇完
    private fun onConsultSelected(intent: Intent) {
        val consultRoomId = intent.getStringExtra(BundleKey.ROOM_ID.key())
        consultRoomId?.let { id ->
            chatViewModel.roomEntity?.let {
                chatViewModel.startConsult(it.id, id)
            }
        }
    }

    /**
     * EVAN_FLAG 2019-09-05 主題聊天室收合控制
     * (1.9.0) 預設高度自動增長並設置最高為2/3
     */
    fun doThemeExpandAction(view: View) {
        if (view.tag == null) { // 放大
            view.tag = "change height"
            binding.expandIV.setImageResource(R.drawable.collapse_white)
            binding.themeMRV.setMaxHeight(setupDefaultThemeHeight(0.0))
            binding.themeMRV.refreshData()
        } else { // 縮小至最高2/3
            view.tag = null
            binding.themeMRV.setMaxHeight(setupDefaultThemeHeight(2.0 / 3.0))
            binding.themeMRV.refreshData()
            binding.expandIV.setImageResource(R.drawable.expand_white)
        }
    }

    fun doThemeCloseAction() {
        binding.expandIV.tag = null
        binding.themeMRV.setMaxHeight(setupDefaultThemeHeight(2.0 / 3.0))
        binding.themeMRV.clearData()
        binding.themeMRV.visibility = View.GONE
        binding.facebookGroup.visibility = View.GONE
        chatViewModel.isThemeOpen = false
        chatViewModel.themeMessage = null
        chatViewModel.nearMessage = null
        this.themeId = ""
        if (isMetaReplyOverTime) {
            binding.chatKeyboardLayout.metaOverTimeView.visibility = View.VISIBLE
        }
        isReply = false
    }

    // EVAN_FLAG 2019-11-20 (1.8.0) 服務號接線或斷線
    //    @OnClick(R.id.agnetActionTV)
    fun doPersonalServiceEventAction() {
        chatViewModel.roomEntity?.let {
            val serviceNumberAgentId = it.serviceNumberAgentId
            chatViewModel.doServiceNumberStop(it.id)
            if (ServiceNumberType.PROFESSIONAL == it.serviceNumberType) {
                if (selfUserId == serviceNumberAgentId) {
                    chatViewModel.doServiceNumberStop(it.id) // 當專人為自己
                }
            } else if (ServiceNumberType.PROFESSIONAL != it.serviceNumberType) {
                if (!Strings.isNullOrEmpty(serviceNumberAgentId)) {
                    chatViewModel.doServiceNumberStop(it.id) // 當專人為自己
                }
            }
        }
    }

    // 商務號點擊右下角頭像可切換身份
    private fun showSwitchIdentityDialog(resp: ServiceNumberChatroomAgentServicedRequest.Resp) {
        identityList?.let { list ->
            chatViewModel.roomEntity?.let { room ->
                val filterSelfList = list.filter { it.id != room.identityId }
                BottomSheetDialogBuilder(requireContext(), layoutInflater)
                    .getSwitchIdentityDialog(
                        filterSelfList,
                        { servicesIdentityListResponse: ServicesIdentityListResponse ->
                            sendSwitchIdentity(servicesIdentityListResponse)
                        },
                        {
                            showStopServiceDialog(resp)
                        }
                    ).show()
            }
        }
    }

    private fun getIdentityList() {
        chatViewModel.roomEntity?.let {
            provideRetrofit(requireContext())
                .create(IdentityListService::class.java)
                .getIdentityList(
                    ServicesIdentityListRequest(
                        SyncContactRequest.PAGE_SIZE,
                        0L,
                        it.serviceNumberId
                    )
                ).enqueue(
                    object : Callback<CommonResponse<List<ServicesIdentityListResponse>>> {
                        override fun onResponse(
                            call: Call<CommonResponse<List<ServicesIdentityListResponse>>>,
                            response: Response<CommonResponse<List<ServicesIdentityListResponse>>>
                        ) {
                            identityList = response.body()?.items
                            changeIdentity()
                        }

                        override fun onFailure(
                            call: Call<CommonResponse<List<ServicesIdentityListResponse>>>,
                            t: Throwable
                        ) {
                        }
                    }
                )
        }
    }

    private fun showSwitchIdentityToast(servicesIdentityListResponse: ServicesIdentityListResponse) {
        val message =
            String.format(
                getString(R.string.switch_identity_toast),
                servicesIdentityListResponse.text
            )
        ToastUtils.showToast(requireContext(), message)
    }

    private fun sendSwitchIdentity(servicesIdentityListResponse: ServicesIdentityListResponse) {
        chatViewModel.roomEntity?.let {
            showLoadingView(R.string.wording_processing)
            provideRetrofit(requireContext())
                .create(SwitchIdentityService::class.java)
                .setIdentity(
                    ServiceSwitchIdentityRequest(
                        SyncContactRequest.PAGE_SIZE,
                        0L,
                        it.serviceNumberId,
                        servicesIdentityListResponse.id
                    )
                ).enqueue(
                    object : Callback<ServiceSwitchIdentityResponse?> {
                        override fun onResponse(
                            call: Call<ServiceSwitchIdentityResponse?>,
                            response: Response<ServiceSwitchIdentityResponse?>
                        ) {
                            showSwitchIdentityToast(servicesIdentityListResponse)
                            hideLoadingView()
                            changeIdentity(servicesIdentityListResponse)
                        }

                        override fun onFailure(
                            call: Call<ServiceSwitchIdentityResponse?>,
                            t: Throwable
                        ) {
                            hideLoadingView()
                            ToastUtils.showToast(
                                requireContext(),
                                getString(R.string.switch_identity_failed_toast)
                            )
                            showSwitchIdentityToast(servicesIdentityListResponse)
                        }
                    }
                )
        }
    }

    // 一開始進來那 identityId 去比對 list 做更換
    private fun changeIdentity() {
        chatViewModel.roomEntity?.let { room ->
            identityList?.let {
                if (it.isNotEmpty()) {
                    val filterList: ServicesIdentityListResponse = it.firstOrNull { item -> item.id == room.identityId } ?: ServicesIdentityListResponse()
                    changeIdentity(filterList)
                }
            }
        }
    }

    // 點擊列表做更換
    private fun changeIdentity(servicesIdentityListResponse: ServicesIdentityListResponse) {
        binding.civServicedAgentAvatar.loadAvatarIcon(
            servicesIdentityListResponse.avatarId,
            servicesIdentityListResponse.nickName,
            servicesIdentityListResponse.id
        )
        chatViewModel.roomEntity?.let {
            it.identityId = servicesIdentityListResponse.id
            chatViewModel.switchIdentity(servicesIdentityListResponse)
        }
    }

    private fun showStopServiceDialog(resp: ServiceNumberChatroomAgentServicedRequest.Resp) {
        chatViewModel.roomEntity?.let {
            val serviceNumberAgentId = resp.serviceNumberAgentId
            if (ServiceNumberType.PROFESSIONAL == it.serviceNumberType && selfUserId != serviceNumberAgentId) {
                val agentName = UserProfileReference.findAccountName(null, serviceNumberAgentId)
                val msg = getString(R.string.text_tip_waiting_for_snatch_service, StringHelper.getString(agentName, getString(R.string.text_someone)).toString())

                AlertView
                    .Builder()
                    .setContext(requireContext())
                    .setStyle(AlertView.Style.Alert)
                    .setMessage(msg)
                    .setOthers(arrayOf(getString(R.string.cancel), getString(R.string.text_sure)))
                    .setOnItemClickListener { o: Any?, position: Int ->
                        if (position == 1) {
                            binding.ivServicedStatus.setImageResource(R.drawable.circle_reeen_bg)
                            doServiceNumberTransfer(
                                ServicedTransferType.TRANSFER_SNATCH,
                                resp,
                                ""
                            )
                        }
                    }.build()
                    .setCancelable(true)
                    .show()
                return
            }

            var message = getString(R.string.text_you_servicing_and_sure_to_close_service)
            if (ServiceNumberType.NORMAL == it.serviceNumberType) { // 一般
                message = getString(R.string.text_tip_servicing_and_sure_to_close_service, it.name)
            } else if (ServiceNumberType.PROFESSIONAL == it.serviceNumberType) { // 專業
                message = getString(R.string.text_tip_servicing_and_sure_to_close_service, it.name) // 當前服務者
            } else if (ServiceNumberType.BOSS == it.serviceNumberType) {
                message =
                    if (selfUserId != it.serviceNumberOwnerId) {
                        getString(R.string.text_tip_servicing_customer_and_sure_to_close_service, it.serviceNumberName, it.name) // 秘書號
                    } else {
                        getString(R.string.text_tip_servicing_and_sure_to_close_service, it.name) // 擁有者
                    }
            }

            AlertView
                .Builder()
                .setContext(requireContext())
                .setStyle(AlertView.Style.Alert)
                .setMessage(message)
                .setOthers(arrayOf(getString(R.string.cancel), getString(R.string.text_sure)))
                .setOnItemClickListener { o: Any?, position: Int ->
                    if (position == 1) {
                        chatViewModel.stopService()
                    }
                }.build()
                .setCancelable(true)
                .show()
        }
    }

    // EVAN_FLAG 2019-11-19 (1.8.0) 結束多渠道服務，
    //  並停止專人服務
    private fun doAgentStopService(resp: ServiceNumberChatroomAgentServicedRequest.Resp) {
        // 商業服務號右下角頭像可切換身份
        chatViewModel.roomEntity?.let {
            if (ServiceNumberType.BOSS == it.serviceNumberType && it.serviceNumberOwnerId != selfUserId) {
                showSwitchIdentityDialog(resp)
            } else {
                showStopServiceDialog(resp)
            }
        }
    }

    private fun clearProvisionalMember() {
        binding.scopeProvisionalMemberList.visibility = View.GONE
        chatViewModel.roomEntity?.provisionalIds?.clear()
        provisionalMemberAdapter.submitList(Lists.newArrayList())
    }

    fun refreshPager(session: ChatRoomEntity) {
        if (activity is ChatActivity) {
            (activity as ChatActivity).refreshPager(session)
        }
    }

    fun refreshUnReadNum() {
    }

    fun refreshUI() {
        refreshListView()
    }

    fun updateSenderName(
        senderName: String,
        senderId: String
    ) {
        for (message in chatViewModel.mainMessageData) {
            if (message.senderId != null && message.senderId == senderId) {
                message.senderName = senderName
            }
        }
        refreshListView()
    }

    fun onLoadMoreMsg(messages: List<MessageEntity>) {
        binding.xrefreshLayout.completeRefresh()
        if (messages.isNotEmpty()) {
            chatViewModel.mainMessageData.addAll(messages)
            refreshListView()
        } else {
            chatViewModel.recordMode = false
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
        binding.messageRV.refreshToBottom(actionStatus.status())
    }

    fun scrollToTop() {
        binding.messageRV.post {
            binding.messageRV.refreshToPosition(0)
        }
    }

    fun getLastMsgTime(): Long {
//        if (mainMessageData.isNotEmpty()) {
//            return mainMessageData[mainMessageData.size - 1].sendTime
//        }
        return 0
    }

    fun finishActivity() {
        requireActivity().finish()
    }

    /**
     * 處理用戶在多渠道服務號是否上線
     * 檢查為服務號聊天室，及狀態欄位為顯示狀態
     */
    fun doChannelOnLineStatus(roomId: String) {
        chatViewModel.roomEntity?.let {
            if (it.isService(selfUserId)) {
                // EVAN_FLAG 2019-11-19 (1.8.0) 多渠道進線錯亂影響到其它聊天室
                if (it.id == roomId) {
                    chatViewModel.doAppointStatus(roomId)
                }
            }
        }
    }

    fun doChangeAgentStatus() {
    }

    fun showSnatchRobotDialog(data: Any) {
        val dialog = getSnatchDialog(getString(R.string.text_robot_servicing_snatch_by_agent))
        dialog
            .setOnItemClickListener { o, position ->
                if (position == 1) {
                    chatViewModel.snatchRobot(data)
                    showLoadingView()
                }
            }.build()
            .setCancelable(true)
            .show()
    }

    fun doShowSnatchRobotChat(sendData: SendData) {
        getSnatchDialog(getString(R.string.text_robot_servicing_snatch_by_agent))
            .setOthers(
                arrayOf(
                    getString(R.string.picture_cancel),
                    getString(R.string.text_robot_servicing_transfer)
                )
            ).setOnItemClickListener { o: Any?, position: Int ->
                if (position == 1) {
                    chatViewModel.doRobotSnatchByAgent(sendData)
                    showLoadingView()
                }
            }.build()
            .setCancelable(true)
            .show()
    }

    fun getSnatchDialog(message: String?): AlertView.Builder =
        AlertView
            .Builder()
            .setContext(requireContext())
            .setStyle(AlertView.Style.Alert)
            .setMessage(message)
            .setOthers(
                arrayOf(
                    getString(R.string.picture_cancel),
                    getString(R.string.text_robot_servicing_transfer)
                )
            )

    private fun stopIosProgressBar() {
        progressBar?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
    }

    fun doChatRoomSnatchByAgent(
        isSuccess: Boolean,
        sendData: SendData
    ) {
        if (isSuccess) {
            chatViewModel.roomEntity?.let {
                binding.scopeRobotChat.visibility = View.GONE
                binding.ivTurnToAiServices.visibility = View.GONE
                if (activity is ChatActivity) {
                    (activity as ChatActivity?)!!.showTopMenu(true)
                }
                it.serviceNumberStatus = ServiceNumberStatus.ON_LINE
                setInputHint()
                chatViewModel.doServiceNumberServicedStatus(null)
                sendMessage(sendData)
                initBottomRoomList()
                stopIosProgressBar()
            }
        } else {
            ToastUtils.showToast(requireContext(), getString(R.string.text_can_not_snatch))
        }
    }

    fun onSnatchRobot(
        isSuccess: Boolean,
        data: Any
    ) {
        if (isSuccess) {
            chatViewModel.roomEntity?.let {
                binding.scopeRobotChat.visibility = View.GONE
                binding.ivTurnToAiServices.visibility = View.GONE
                if (activity is ChatActivity) {
                    (activity as ChatActivity?)!!.showTopMenu(true)
                }
                it.serviceNumberStatus = ServiceNumberStatus.ON_LINE
                when (data) {
                    is SendVideoFormat -> {
                        chatViewModel.sendVideo(data.iVideoSize)
                    }

                    is SendImageFormat -> {
                        chatViewModel.sendImage(data.agentName, data.thumbnailName)
                    }

                    is SendImageWithBitMapFormat -> {
                        chatViewModel.sendImage(data.agentName, data.path, data.bitmap)
                    }

                    is SendVoiceFormat -> {
                        chatViewModel.sendVoice(data.path, data.duration)
                    }
                }
                chatViewModel.doServiceNumberServicedStatus(null)
                initBottomRoomList()
                stopIosProgressBar()
            }
        } else {
            ToastUtils.showToast(requireContext(), getString(R.string.text_can_not_snatch))
        }
    }

    fun onCompleteProvisionalMemberList(
        entities: List<UserProfileEntity>,
        newMemberIds: List<String>
    ) {
        chatViewModel.roomEntity?.let {
            it.provisionalIds?.let { ids ->
                if (ids.isEmpty()) it.provisionalIds = newMemberIds
            }
            val list =
                if (it.roomType == ChatRoomType.provisional) {
                    entities.filter { it.id == selfUserId }.toMutableList()
                } else {
                    entities
                }
            provisionalMemberAdapter.submitList(list)
            binding.scopeProvisionalMemberList.visibility =
                if (list.isNotEmpty()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }
    }

    fun onAddProvisionalMember(addNewMemberIds: List<String>) {
        chatViewModel.roomEntity?.let {
            val oldProvisionalMemberList: MutableList<String> =
                Lists.newArrayList(
                    it.provisionalIds
                )
            oldProvisionalMemberList.removeAll(addNewMemberIds)
            oldProvisionalMemberList.addAll(addNewMemberIds)
            it.provisionalIds = oldProvisionalMemberList
            provisionalMemberAdapter.submitList(
                chatViewModel.getMemberProfileEntityDirectly(
                    oldProvisionalMemberList
                )
            )
        }
    }

    fun showErrorMsg(msg: String) {
        ToastUtils.showToast(requireContext(), msg)
    }

    fun showErrorToast(errorMessage: String) {
        ToastUtils.showToast(requireContext(), errorMessage)
    }

    fun showNoMoreMessage() {
        adapter.setData(adapter.getData(), keyWord, chatRoomMemberTable)
    }

    fun showIsNotMemberMessage(errorMessage: String) {
        if (!isAdded) return
        ToastUtils.showCenterToast(
            requireContext(),
            errorMessage
        )
        if (errorMessage != getString(R.string.api_there_no_internet_connection)) {
            chatViewModel.roomEntity?.let {
                chatViewModel.deletedRoom(it.id)
            }
        }
    }

    fun setSearchKeyWord(keyWord: String) {
        Objects.requireNonNull(binding.messageRV.adapter).setKeyword(keyWord).refreshData()
    }

    fun release() {
        chatViewModel.clearSession()
    }

    override fun onBackgroundClick(refreshLayout: XRefreshLayout) {
        this.actionStatus = ActionStatus.SCROLL
        if (binding.messageRV.isShowCheckBox) {
            binding.messageRV.setAdapterMode(MessageAdapterMode.DEFAULT)
        }
        // 當搜索模式開啟不控制背景點擊
        if (binding.searchBottomBar.visibility == View.GONE) {
            binding.chatKeyboardLayout.showKeyboard()
        }
    }

    private fun sendFacebookPrivateReply(
        message: MessageEntity,
        content: SendData
    ) {
        if (!message.isFacebookPrivateReplied!!) {
            chatViewModel.sendFacebookPrivateReply(message, content)
            closeFacebookTheme()
            binding.messageRV.refreshToBottom(true)
        } else {
            Toast
                .makeText(
                    requireContext(),
                    getString(R.string.facebook_already_private_replied),
                    Toast.LENGTH_SHORT
                ).show()
        }
    }

    private fun sendFacebookPublicReply(
        message: MessageEntity,
        content: SendData
    ) {
        chatViewModel.sendFacebookPublicReply(message, content)
        binding.messageRV.refreshToBottom(true)
        closeFacebookTheme()
        if (isMetaReplyOverTime) {
            binding.chatKeyboardLayout.metaOverTimeView.visibility = View.VISIBLE
        }
    }

    private fun closeFacebookTheme() {
        CoroutineScope(Dispatchers.Main).launch {
            binding.chatKeyboardLayout.inputHET.setText("")
            binding.themeMRV.visibility = View.GONE
            KeyboardHelper.hide(binding.chatKeyboardLayout.inputHET)
        }
    }

    private fun sendMessage(sendData: SendData) {
        if (sendData.type == MessageType.TEXT &&
            Strings.isNullOrEmpty(
                sendData.content.trim { it <= ' ' }
            )
        ) {
            Toast
                .makeText(
                    requireContext(),
                    getString(R.string.text_can_not_send_empty_message),
                    Toast.LENGTH_SHORT
                ).show()
            Log.e("TAG", "onSendBtnClick sendData 不可發送空白訊息")
            return
        }
        // 判斷是否有機器人服務，是否強直接手
        chatViewModel.roomEntity?.let {
            if (ServiceNumberStatus.ROBOT_SERVICE == it.serviceNumberStatus && it.ownerId != selfUserId) {
                doShowSnatchRobotChat(sendData)
            } else {
                // 送其他內容，隱藏 QuickReply
                binding.rvQuickReplyList.visibility = View.GONE
                chatViewModel.sendButtonClicked(binding.chatKeyboardLayout.inputHET.textData)
            }
        }
    }

    private fun checkMicAvailable(): Boolean {
        if (AudioLib.getInstance(requireContext()).isPlaying) {
            Toast.makeText(requireContext(), getString(R.string.text_mic_phone_is_being_used), Toast.LENGTH_SHORT).show()
            return false
        }

        if (binding.chatKeyboardLayout.isRecording) {
            Toast.makeText(requireContext(), getString(R.string.text_recording), Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun createVideoFile(): File? {
        context?.let {
            val dtf = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
            val strDate: String = dtf.format(LocalDateTime.now())
            val imageFileName = "Aile_" + strDate + "_.mp4"
            val tempFile: File = File(it.cacheDir, imageFileName)
            return tempFile
        } ?: return null
    }

    private fun doOpenCamera() {
        BottomSheetDialogBuilder(requireContext(), layoutInflater)
            .doOpenMedia(
                {
                    binding.chatKeyboardLayout.doCameraAction()
                },
                {
                    binding.chatKeyboardLayout.doVideoAction()
                }
            ).show()
    }

    @Throws(IOException::class)
    private fun createImageFile(): File? {
        context?.let {
            val dtf = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
            val strDate: String = dtf.format(LocalDateTime.now())
            val imageFileName = "Aile_$strDate.jpg"
            val tempFile = File(it.cacheDir, imageFileName)
            return tempFile
        } ?: return null
    }

    inner class KeyBoardBarListener : OnChatKeyBoardListener {
        override fun onSendBtnClick(
            sendData: SendData,
            enableSend: Boolean
        ) {
            chatViewModel.roomEntity?.let {
                val message =
                    chatViewModel.mainMessageData
                        .stream()
                        .filter { messageEntity: MessageEntity -> messageEntity.id == themeId }
                        .findFirst()
                        .orElse(null)
                if (message != null && message.from == ChannelType.FB && isFacebookReplyPublic) {
                    // 公開回覆
                    sendFacebookPublicReply(message, sendData)
                } else if (message != null && message.from == ChannelType.FB && !isFacebookReplyPublic) {
                    // 私訊回覆
                    sendFacebookPrivateReply(message, sendData)
                } else {
                    sendMessage(sendData)
                }
            } ?: run {
                chatViewModel.doAddContact(userId, userName, sendData)
            }
        }

        override fun onRecordingSendAction(
            path: String,
            duration: Int
        ) {
            chatViewModel.sendVoice(path, duration, isFacebookReplyPublic)
        }

        override fun onRecordingStartAction() {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
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
                            requireActivity(),
                            requireActivity().packageName + ".fileprovider",
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
                Log.e(TAG, "error=${error.message}")
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
                Intent(requireActivity(), FileExplorerActivity::class.java).putExtra(BundleKey.THEME_COLOR.key(), themeColor)
            )
            requireActivity().overridePendingTransition(
                R.anim.slide_right_in,
                R.anim.slide_right_out
            )
        }

        private fun checkForSelfPermissions(permission: Array<String>): Boolean {
            for (i in permission.indices) {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
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
                            requireActivity(),
                            requireActivity().packageName + ".fileprovider",
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
                    ContextCompat.getColor(requireContext(), if (isGreenTheme) R.color.color_06B4A5 else R.color.colorPrimary)

                val numberBlueBottomNavBarStyle = BottomNavBarStyle()
                numberBlueBottomNavBarStyle.bottomPreviewNormalTextColor =
                    ContextCompat.getColor(requireContext(), R.color.ps_color_9b)
                numberBlueBottomNavBarStyle.bottomPreviewSelectTextColor =
                    ContextCompat.getColor(requireContext(), if (isGreenTheme) R.color.color_06B4A5 else R.color.colorPrimary)
                numberBlueBottomNavBarStyle.bottomNarBarBackgroundColor =
                    ContextCompat.getColor(requireContext(), R.color.ps_color_white)
                numberBlueBottomNavBarStyle.bottomSelectNumResources = if (isGreenTheme) R.drawable.bg_media_select_green1 else R.drawable.album_num_selected
                numberBlueBottomNavBarStyle.bottomEditorTextColor =
                    ContextCompat.getColor(requireContext(), R.color.ps_color_53575e)
                numberBlueBottomNavBarStyle.bottomOriginalTextColor =
                    ContextCompat.getColor(requireContext(), R.color.ps_color_53575e)

                val numberBlueSelectMainStyle = SelectMainStyle()
                numberBlueSelectMainStyle.statusBarColor =
                    ContextCompat.getColor(requireContext(), if (isGreenTheme) R.color.color_06B4A5 else R.color.colorPrimary)
                numberBlueSelectMainStyle.isSelectNumberStyle = true
                numberBlueSelectMainStyle.isPreviewSelectNumberStyle = true
                numberBlueSelectMainStyle.selectBackground = if (isGreenTheme) R.drawable.album_num_selector_green1 else R.drawable.album_num_selector
                numberBlueSelectMainStyle.mainListBackgroundColor =
                    ContextCompat.getColor(requireContext(), R.color.ps_color_white)
                numberBlueSelectMainStyle.previewSelectBackground =
                    if (isGreenTheme) R.drawable.album_num_selector_green1 else R.drawable.album_preview_num_selector

                numberBlueSelectMainStyle.selectNormalTextColor =
                    ContextCompat.getColor(requireContext(), R.color.ps_color_9b)
                numberBlueSelectMainStyle.selectTextColor =
                    ContextCompat.getColor(requireContext(), if (isGreenTheme) R.color.color_06B4A5 else R.color.colorPrimary)
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
                .create(requireActivity())
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
                    if (media.mimeType
                            .contains("video/") &&
                        media.mimeType != "video/mp4"
                    ) {
                        ToastUtils.showToast(
                            requireActivity(),
                            getString(R.string.text_video_limit_mp4_format)
                        )
                        return@setSelectFilterListener true
                    }
                    chatViewModel.appointResp?.let { resp ->
                        if (resp.lastFrom != null && resp.lastFrom == ChannelType.LINE) {
                            if (media.mimeType
                                    .contains("image/") &&
                                media.size >= 10 * FileSizeUnit.MB
                            ) {
                                ToastUtils.showToast(
                                    requireActivity(),
                                    getString(R.string.text_file_size_limit, 10)
                                )
                                return@setSelectFilterListener true
                            } else if (media.mimeType
                                    .contains("video/mp4") &&
                                media.size >= 200 * FileSizeUnit.MB
                            ) {
                                ToastUtils.showToast(
                                    requireActivity(),
                                    getString(R.string.text_file_size_limit, 200)
                                )
                                return@setSelectFilterListener true
                            }
                        } else if (resp.lastFrom != null && resp.lastFrom == ChannelType.FB) {
                            if (media.mimeType
                                    .contains("image/") &&
                                media.size >= 25 * FileSizeUnit.MB
                            ) {
                                ToastUtils.showToast(
                                    requireActivity(),
                                    getString(R.string.text_file_size_limit, 25)
                                )
                                return@setSelectFilterListener true
                            } else if (media.mimeType
                                    .contains("video/mp4") &&
                                media.size >= 25 * FileSizeUnit.MB
                            ) {
                                ToastUtils.showToast(
                                    requireActivity(),
                                    getString(R.string.text_file_size_limit, 25)
                                )
                                return@setSelectFilterListener true
                            }
                        } else if (resp.lastFrom != null && resp.lastFrom == ChannelType.IG) {
                            if (media.mimeType
                                    .contains("image/") &&
                                media.size >= 8 * FileSizeUnit.MB
                            ) {
                                ToastUtils.showToast(
                                    requireActivity(),
                                    getString(R.string.text_file_size_limit, 8)
                                )
                                return@setSelectFilterListener true
                            } else if (media.mimeType
                                    .contains("video/mp4") &&
                                media.size >= 25 * FileSizeUnit.MB
                            ) {
                                ToastUtils.showToast(
                                    requireActivity(),
                                    getString(R.string.text_file_size_limit, 25)
                                )
                                return@setSelectFilterListener true
                            }
                        } else if (resp.lastFrom != null && resp.lastFrom == ChannelType.GOOGLE) {
                            if (media.mimeType
                                    .contains("image/") &&
                                media.size >= 5 * FileSizeUnit.MB
                            ) {
                                ToastUtils.showToast(
                                    requireActivity(),
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
            binding.funMedia.setType(requireContext(), MultimediaHelper.Type.FILE, themeStyle, -1)
            if (binding.funMedia.visibility == View.GONE) {
                binding.funMedia.visibility = View.VISIBLE
            }
        }

        /**
         * 打開圖片選擇器
         */
        override fun onOpenPhotoSelector(isChange: Boolean) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                for (permission in permissions) {
                    val isPermissionGranted = requireContext().checkSelfPermission(permission)
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
                if (requireContext().checkSelfPermission(permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
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
                Intent(requireActivity(), MediaSelectorPreviewActivity::class.java)
                    .putExtra(BundleKey.IS_ORIGINAL.key(), isOriginal)
                    .putExtra(BundleKey.MAX_COUNT.key(), current)
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
                            val bitmapBean = PictureParse.parseGifPath(requireContext(), bean.path)
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
//                binding.guideLine.setGuidelinePercent(0.79f)
        }

        override fun onSoftKeyboardEndOpened(keyboardHeightInPx: Int) {
        }

        override fun onSoftKeyboardClosed() {
            if (binding.messageRV.adapter != null) binding.messageRV.adapter?.refreshData()
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

//        override fun onPicSelected(list: List<PhotoBean>) {
//
//        }
    }

    // 設定 Quick Reply list
    fun setQuickReply(quickReplyItemList: List<QuickReplyItem>) {
        if (quickReplyItemList.isEmpty()) return
        binding.rvQuickReplyList.visibility = View.VISIBLE
        binding.messageRV.scrollToPosition(binding.messageRV.adapter!!.itemCount - 1)
        binding.rvQuickReplyList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val quickReplyAdapter = QuickReplyAdapter(quickReplyItemList)
        quickReplyAdapter.setOnQuickReplyClickListener { type: String, data: String ->
            chatViewModel.roomEntity?.let {
                chatViewModel.sendQuickReplyMessage(
                    it.id,
                    type,
                    data
                )
                binding.rvQuickReplyList.visibility = View.GONE
            }
        }
        binding.rvQuickReplyList.adapter = quickReplyAdapter
    }

    fun onSendFacebookImageReplySuccess() {
        closeFacebookTheme()
    }

    fun onSendFacebookImageReplyFailed(errorMessage: String) {
        closeFacebookTheme()
        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
    }

    private fun dismissMetaOverTimeView() =
        CoroutineScope(Dispatchers.Main).launch {
            if (isAdded) {
                isMetaReplyOverTime = false
                binding.chatKeyboardLayout.metaOverTimeView.visibility = View.GONE
            }
        }

    private fun showMetaOverTimeView() {
        if (isAdded) {
            CoroutineScope(Dispatchers.Main).launch {
                isMetaReplyOverTime = true
                binding.chatKeyboardLayout.metaOverTimeTextView.text = if (chatViewModel.channelType == "facebook") getString(R.string.facebook_overtime) else getString(R.string.instagram_overtime)
                binding.chatKeyboardLayout.metaOverTimeView.visibility = View.VISIBLE
            }
        }
    }

    private var sendVideoProgress: Dialog? = null
    private var sendVideoProgressBinding: ProgressSendVideoBinding? = null

    fun showSendVideoProgress(message: String) {
        if (sendFileSize == 0) return
        sendVideoProgress?.let {
            if (it.isShowing) return
        }
        val screenWidth = UiHelper.getDisplayWidth(requireActivity())
        if (sendVideoProgressBinding == null) {
            sendVideoProgressBinding = ProgressSendVideoBinding.inflate(LayoutInflater.from(requireContext()))
        }

        if (sendVideoProgress == null) {
            sendVideoProgress = Dialog(requireContext())
            sendVideoProgress!!.setContentView(sendVideoProgressBinding!!.root)

            val params =
                FrameLayout.LayoutParams(
                    screenWidth / 3,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                )
            sendVideoProgressBinding!!.root.layoutParams = params
        } else {
            sendVideoProgressBinding!!.progressBar.progress = 0
        }
        sendVideoProgress!!.setCancelable(false)
        sendVideoProgressBinding!!.message.text = message
        sendVideoProgress?.show()
    }

    fun dismissSendVideoProgress() {
        if (sendVideoProgress != null && sendVideoProgress?.isShowing == true) {
            sendVideoProgress?.dismiss()
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
                val progressBar = it.findViewById<ProgressBar>(R.id.progress_bar)
                progressBar?.let { bar ->
                    bar.progress = progress
                    if (progress >= 100) {
                        sendFileSize = 0
                        dismissSendVideoProgress()
                    }
                }
            }
        }
    }

    fun setServicedGreenStatus() {
        binding.ivServicedStatus.setImageResource(R.drawable.circle_reeen_bg)
    }

    fun showLoadingView(resId: Int) {
        if (activity == null) return
        progressBar =
            IosProgressBar.show(
                activity,
                resId,
                true,
                false
            ) { dialog: DialogInterface? -> }
    }

    fun hideLoadingView() {
        progressBar?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        chatViewModel.roomEntity?.let {
            this.undeadLineDrawable = true
            binding.chatKeyboardLayout.setChatRoomEntity(
                it,
                ChatRoomType.FRIEND_or_GROUP_or_DISCUS_or_SERVICE_MEMBER.contains(
                    it.type
                ),
                ChatRoomType.GROUP_or_DISCUSS_or_SERVICE_MEMBER.contains(
                    it.type
                )
            )
            this.mentionSelectAdapter =
                MentionSelectAdapter(requireActivity()).setUserProfiles(
                    it.membersLinkedList
                )
            chatViewModel.queryMemberIsBlock()
            chatViewModel.isVisible = true
            chatViewModel.sendNoticeRead()
            setInputHint() // second call
            chatViewModel.doServiceNumberServicedStatus(null)
            if (!isReply) setupToDefault()
        }
    }

    override fun onPause() {
        super.onPause()
        chatViewModel.isVisible = false
        binding.chatKeyboardLayout.hideBottomPop()
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
            RepairMessageService.stop(requireContext(), it.id)
        }
        AudioLib.getInstance(requireContext()).stopPlay() // 防止返回后还在播放語音的情况
        requireContext().unregisterReceiver(timeZoneChangeReceiver)

        EventBusUtils.unregister(this)
        App.getInstance().chatRoomId = ""
        mediaPreviewARL.unregister()
        binding.funMedia.onDestroy(requireContext())
        binding.messageRV.adapter?.onDestroy()
        super.onDestroy()
    }

    fun refreshListView() {
        binding.messageRV.refreshData()
    }

    /**
     * 移動訊息 Item UI 到指定可視位置
     */
    fun refreshDataAndScrollToPosition(index: Int) {
        binding.messageRV.scrollToPosition(actionStatus.status(), index)
    }

    fun highLightUnReadLine(needProcess: Boolean) {
        if (needProcess) {
            CoroutineScope(Dispatchers.Main).launch {
                delay(300L)
                val unReadIndex =
                    chatViewModel.mainMessageData.indexOf(
                        MessageEntity
                            .Builder()
                            .id("以下為未讀訊息")
                            .build()
                    )
                if (unReadIndex > 0) {
                    val unReadMessage =
                        chatViewModel.mainMessageData[unReadIndex]
                    doScrollToSelectMessageItemAndSetKeyword("", unReadMessage)
                }
            }
        }
    }

    /**
     * updateSendVideoProgress 影片轉碼發送流程
     */
    var tempProgress: String = ""

    fun updateSendVideoProgress(
        isConvert: Boolean,
        progress: String
    ) {
        val screenWidth = UiHelper.getDisplayWidth(requireActivity())
        if (sendVideoProgressBinding == null) {
            sendVideoProgressBinding =
                ProgressSendVideoBinding.inflate(LayoutInflater.from(requireContext()))
        }
        sendVideoProgress?.setCancelable(false) ?: run {
            sendVideoProgress = Dialog(requireContext())
            sendVideoProgress!!.setContentView(sendVideoProgressBinding!!.root)
            val params =
                FrameLayout.LayoutParams(
                    screenWidth,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                )
            sendVideoProgressBinding!!.root.layoutParams = params
        }
        if (isConvert) {
            sendVideoProgressBinding!!.message.text =
                getString(R.string.text_transferring_video_message)
            sendVideoProgressBinding!!.progressBar.progress = progress.toInt()

            if (sendVideoProgress?.isShowing == false) {
                sendVideoProgress?.show()
            }
//            if (progress == "6666666") {
//                //轉碼完成接著發送
//                sendVideoProgressBinding!!.message.text =
//                    getString(R.string.text_sending_video_message)
//                sendVideoProgressBinding!!.progressBar.progress = 0
//            }
        } else {
            // 發送影片進度
            if (progress != tempProgress) {
                tempProgress = progress
                sendVideoProgressBinding!!.progressBar.progress = progress.toInt()
                if (progress == "100") {
                    sendVideoProgress!!.dismiss()
                }
            }
        }
    }

    fun stopRefresh() {
        binding.xrefreshLayout.completeRefresh()
    }

    fun updateAccountForMessage(mAccount: UserProfileEntity) {
        for (i in chatViewModel.mainMessageData.indices) {
            val msg = chatViewModel.mainMessageData[i]
            if (mAccount.id == msg.senderId) {
                chatViewModel.mainMessageData[i].senderName = if (Strings.isNullOrEmpty(mAccount.alias)) mAccount.nickName else mAccount.alias
            }
            refreshListView()
        }
    }

    /**
     * Socket 收到信息會廣域通知到這裡
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handleEvent(eventMsg: EventMsg<*>) {
        binding.chatKeyboardLayout.handleEvent(eventMsg)
        chatViewModel.roomEntity?.let { roomEntity ->
            when (eventMsg.code) {
                MsgConstant.SCROLL_TO_TARGET_MESSAGE_POSITION -> {
                    val scrollData =
                        JsonHelper.getInstance().from(
                            eventMsg.string,
                            MutableMap::class.java
                        )
                    if (scrollData["roomId"] == roomEntity.id) {
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

                MsgConstant.NOTICE_NEW_MESSAGE_CHECKING_SEVICENUMBER_SERVICED -> {}
                MsgConstant.NOTICE_APPEND_NEW_MESSAGE_IDS -> {
                    val appendNewMessageIds =
                        JsonHelper.getInstance().fromToList(
                            eventMsg.string,
                            Array<String>::class.java
                        )

                    val localEntities =
                        MessageReference.findByIdsAndRoomId(
                            null,
                            appendNewMessageIds.toTypedArray<String>(),
                            roomEntity.id
                        )
                    for (entity in localEntities) {
                        if (entity.tag != null && entity.tag!!.isNotEmpty()) {
                            val facebookTag =
                                JsonHelper.getInstance().from(
                                    entity.tag,
                                    FacebookTag::class.java
                                )
                            if (facebookTag.data.replyType.isNotEmpty()) {
                                if ("private" == facebookTag.data.replyType) {
                                    val commentId = facebookTag.data.commentId
                                    val targetMessageList = chatViewModel.mainMessageData.filter { message -> message.tag?.isNotEmpty() == true && message.tag!!.contains(commentId) }

                                    targetMessageList.forEach(
                                        Consumer { message: MessageEntity ->
                                            MessageReference.updateFacebookPrivateReplyStatus(
                                                roomEntity.id,
                                                message.id,
                                                true
                                            )
                                            message.isFacebookPrivateReplied = true
                                            updateFacebookStatus(message)
                                        }
                                    )
                                }
                            }
                        }
                        chatViewModel.displayMainMessageLogic(
                            isNew = true,
                            scrollToBottom = true,
                            entity
                        )
                        if (entity.sourceType == SourceType.SYSTEM && entity.content.contains("回覆通路")) {
                            chatViewModel.roomEntity?.let {
                                chatViewModel.getLastChannelFrom(it.id)
                                chatViewModel.doServiceNumberServicedStatus(null)
                            }
                        }
                    }
                }

                MsgConstant.NOTICE_APPEND_OFFLINE_MESSAGE_IDS -> {
                    val appendOffLineMessageIds =
                        JsonHelper.getInstance().fromToList(
                            eventMsg.string,
                            Array<String>::class.java
                        )
                    val localEntities =
                        MessageReference.findByIdsAndRoomId(
                            null,
                            appendOffLineMessageIds.toTypedArray<String>(),
                            roomEntity.id
                        )
                    for (entity in localEntities) {
                        chatViewModel.displayMainMessageLogic(
                            isNew = false,
                            scrollToBottom = false,
                            entity
                        )
                    }
                }

                MsgConstant.NOTICE_APPEND_MESSAGE -> {
                    val appendMessage =
                        JsonHelper.getInstance().from(
                            eventMsg.string,
                            MessageEntity::class.java
                        )
                    appendMessage?.let { message ->
                        if (roomEntity.id == message.roomId) {
                            chatViewModel.displayMainMessageLogic(
                                isNew = false,
                                scrollToBottom = false,
                                message
                            )
                        }
                    }
                }

//                MsgConstant.SEND_PHOTO_MEDIA_SELECTOR -> {
//                    val selectorJson = eventMsg.string
//                    val selectorData = JsonHelper.getInstance().fromToMap<String, String>(selectorJson)
//                    val isOriginalStr = selectorData["isOriginal"]
//                    val listStr = selectorData["list"]
//
//                    val list =
//                        JsonHelper.getInstance().fromToList(
//                            listStr,
//                            Array<ImageBean>::class.java
//                        )
//                    sendFileSize = list.size
//                    isSendSingleFile = sendFileSize == 1
//                    CELog.e(selectorJson)
//                    executeSendPhotos(Lists.newArrayList<AMediaBean>(list), isOriginalStr.toBoolean())
//                }

                MsgConstant.NOTICE_EXECUTION_BUSINESS_CREATE_ACTION -> {
                    val path = eventMsg.string
                    executeTask(Lists.newArrayList(), path, false)
                }

                MsgConstant.ACCOUNT_REFRESH_FILTER -> {
                    val userProfile = eventMsg.data as UserProfileEntity
                    val members = roomEntity.members
                    if (members != null && members.isNotEmpty()) {
                        val iterator = members.iterator()
                        var hasUser = false
                        while (iterator.hasNext()) {
                            val user = iterator.next()
                            if (user.id == userProfile.id) {
                                iterator.remove()
                                hasUser = true
                                break
                            }
                        }
                        if (hasUser) {
                            roomEntity.members.add(userProfile)
                        }
                        binding.messageRV.refreshData()
                    } else {
                    }
                }

                MsgConstant.REFRESH_FILTER ->
                    if (!chatViewModel.recordMode) {
                        chatViewModel.messageListRequestAsc(chatViewModel.mainMessageData.lastOrNull())
                    } else {
                    }

                MsgConstant.CLEAN_MSGS_FILTER -> onCleanMsgs()
                MsgConstant.REMOVE_FRIEND_FILTER -> {
                    val account = eventMsg.data as UserProfileEntity?
                    account?.let {
                        if (it.roomId == roomEntity.id) {
                            finishActivity()
                        }
                    }
                }

                MsgConstant.INTERNET_STSTE_FILTER -> {
                    // EVAN_FLAG 2020-02-18 1.10.0 暫時拔除 linphone
                    chatViewModel.dimQueryLastReadMsgForInternet()
                }

                MsgConstant.SESSION_REFRESH_FILTER ->
                    try {
                        val data = JsonHelper.getInstance().fromToMap<String, String>(eventMsg.string)
                        val key = data["key"]
                        val values = data["values"]
                        val roomId = data["roomId"]
                        if (key == null || TextUtils.isEmpty(key) || roomEntity.id != roomId) {
                            return
                        }
                        when (key) {
                            "avatarUrl" -> {
                                roomEntity.avatarId = values
                                refreshPager(roomEntity)
                            }

                            "name" -> {
                                roomEntity.name = values
                                refreshPager(roomEntity)
                            }

                            "ownerId" -> {
                                roomEntity.ownerId = values
                                refreshPager(roomEntity)
                                hideNoOwnerNotify()
                            }

                            else -> refreshPager(roomEntity)
                        }
                    } catch (e: Exception) {
                        CELog.e(e.message)
                    }

                MsgConstant.CANCEL_FILTER -> {
                    requireActivity().finish()
                }

                MsgConstant.MSG_RECEIVED_FILTER -> {
                    val receiverMessage =
                        JsonHelper.getInstance().from(
                            eventMsg.string,
                            MessageEntity::class.java
                        )
                    receiverMessage?.let {
                        chatViewModel.onReceive(receiverMessage)
                    }
                }

                MsgConstant.MSG_STATUS_FILTER -> {
                    val mMsgStatusBean = eventMsg.data as MsgStatusBean
                    val messageId = mMsgStatusBean.messageId
                    val sendNum = mMsgStatusBean.sendNum
                    updateMsgStatus(messageId, sendNum)
                }

                MsgConstant.MSG_NOTICE_FILTER -> {
                    val mMsgNoticeBean = eventMsg.data as MsgNoticeBean
                    val messageId1 = mMsgNoticeBean.messageId
                    val receivedNum1 = mMsgNoticeBean.receivedNum
                    val readNum1 = mMsgNoticeBean.readNum
                    val sendNum1 = mMsgNoticeBean.sendNum
                    updateMsgNotice(messageId1, receivedNum1, readNum1, sendNum1)
                }

                MsgConstant.USER_EXIT -> {
                    val mUserExitBean = eventMsg.data as UserExitBean
                    val roomId1 = mUserExitBean.roomId
                    val title1 = mUserExitBean.title
                    val imageUrl1 = mUserExitBean.imageUrl
                    val userId1 = mUserExitBean.userId
                    AccountRoomRelReference.deleteRelByRoomIdAndAccountId(null, roomId1, userId1)
                    if (roomEntity.id == mUserExitBean.roomId) {
                        roomEntity.name = title1
                        roomEntity.avatarId = imageUrl1
                        refreshPager(roomEntity)

                        if (roomEntity.ownerId == mUserExitBean.userId) {
                            roomEntity.ownerId = ""
                        }
                        getMemberPrivilege()
                    } else {
                    }
                }

                MsgConstant.SEND_UPDATE_AVATAR -> {
                    val mUpdateAvatarBean = eventMsg.data as UpdateAvatarBean
                    val avatar = mUpdateAvatarBean.avatar
                    if (avatar == null || "" == avatar) {
                        return
                    }
                    var needRefresh = false
                    if (NoticeCode.UPDATE_USER_AVATAR.getName() == mUpdateAvatarBean.type) {
                        //                if (JocketManager.UPDATE_USER_AVATAR.equals(mUpdateAvatarBean.getType())) {

                        var i = 0
                        while (i < chatViewModel.mainMessageData.size) {
                            val msg = chatViewModel.mainMessageData[i]
                            if (mUpdateAvatarBean.userId == msg.senderId) {
                                msg.avatarId = avatar
                                Log.e(TAG, " message sender avatar id ")
                                needRefresh = true
                            }
                            i++
                        }
                        if (needRefresh) {
                            refreshListView()
                        } else {
                        }
                    } else {
                    }
                }

                MsgConstant.NOTICE_SERVICE_NUMBER_CONSULT_EVENT -> {
                    val data = JsonHelper.getInstance().fromToMap<String, String>(eventMsg.string)
                    val consultRoomId = data["consultRoomId"]
//                    val transferReason = data["transferReason"]

                    consultRoomId?.let { consultId ->
                        // 移除諮詢服務
                        if ("ConsultComplete" == data["event"]) {
                            chatViewModel.removeConsultRoom(consultId)
                        } else if ("ConsultStart" == data["event"]) {
                            // 加入諮詢服務
                            if (selfUserId == data["userId"] && roomEntity.serviceNumberAgentId == data["userId"] && roomEntity.id == data["srcRoomId"]) {
                                chatViewModel.addConsultRoom(consultId)
                            } else {
                            }
                        } else {
                        }
                    }
                }

                MsgConstant.SERVICE_NUMBER_PERSONAL_START -> {
                    val map = eventMsg.data as Map<String, String>
                    if (roomEntity.id == map["roomId"]) {
                        if (selfUserId != map["serviceNumberAgentId"]) {
                            if (roomEntity.serviceNumberStatus == ServiceNumberStatus.ROBOT_SERVICE) {
                                // 當其他成員從AI服務聊天室接手時觸發關閉AI視窗
                                binding.scopeRobotChat.visibility = View.GONE
                            }
                            chatViewModel.doServiceNumberServicedStatus(null)
                            chatViewModel.getProvisionalMember(roomEntity.id)
                        } else {
                        }
                    } else {
                    }
                }

                MsgConstant.SERVICE_NUMBER_TRANSFER_STATUS, MsgConstant.SERVICE_NUMBER_PERSONAL_STOP -> {
                    val targetRoomId = eventMsg.data as String
                    if (roomEntity.id == targetRoomId) {
                        chatViewModel.doServiceNumberServicedStatus(null)
                        chatViewModel.getProvisionalMember(roomEntity.id)
                    } else {
                    }
                }

                MsgConstant.APPOINT_STATUS_CHECKING -> {
                    val rId = eventMsg.data as String
                    if (roomEntity.isService(selfUserId)) {
                        if (roomEntity.id == rId) {
                            chatViewModel.doAppointStatus(rId)
                        } else {
                        }
                    } else {
                    }
                }

                MsgConstant.UPDATE_MESSAGE_STATUS ->
                    if (eventMsg.data is MessageEntity) {
                        val msg = eventMsg.data as MessageEntity
                        val position = chatViewModel.mainMessageData.indexOf(msg)
                        chatViewModel.mainMessageData[position].content = msg.content().toStringContent()
                        if (position >= 0) {
                            binding.messageRV.refreshData(position, msg)
                        } else {
                        }
                    } else {
                    }

                MsgConstant.BUSINESS_BINDING_ROOM_EVENT -> {
                    val datas = eventMsg.data as Map<String, String>
                    val bindRoomId = datas["roomId"]
                    if (ChatRoomType.FRIEND_or_DISCUSS.contains(roomEntity.type) && roomEntity.id == bindRoomId) {
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

                MsgConstant.NOTICE_ROBOT_SERVICE_WARNED -> {
                    val aiServiceWarnedSocket =
                        JsonHelper.getInstance().from(
                            eventMsg.data,
                            AiServiceWarnedSocket::class.java
                        )
                    if (roomEntity.serviceNumberStatus == ServiceNumberStatus.ROBOT_SERVICE && roomEntity.id == aiServiceWarnedSocket.roomId) {
                        roomEntity.isWarned = !aiServiceWarnedSocket.content.cancel
                        setAiServices()
                    } else {
                    }
                }

                MsgConstant.NOTICE_PROVISIONAL_MEMBER_REMOVED -> {
                    val idSets = eventMsg.data as Set<String>
                    val existIds = roomEntity.provisionalIds.iterator()
                    while (existIds.hasNext()) {
                        for (id in idSets) {
                            if (existIds.next() == id) {
                                existIds.remove()
                                break
                            }
                        }
                    }
                    chatViewModel.getMemberProfileEntity(roomEntity.provisionalIds)
                }

                MsgConstant.NOTICE_PROVISIONAL_MEMBER_ADDED, MsgConstant.NOTICE_SERVICE_NUMBER_ADD_FROM_PROVISIONAL -> {
                    val provisionalMemberAddedSocket =
                        JsonHelper.getInstance().from(
                            eventMsg.data,
                            ProvisionalMemberAddedSocket::class.java
                        )
                    if (provisionalMemberAddedSocket.roomId == roomEntity.id) {
                        chatViewModel.getProvisionalMember(provisionalMemberAddedSocket.roomId)
                    } else {
                    }
                }

                MsgConstant.GROUP_REFRESH_FILTER -> {}
                MsgConstant.MESSAGE_QUICK_REPLY -> {
//                    val listType = object : TypeToken<List<QuickReplyItem?>?>() {}.type
                    val quickReplySocket =
                        JsonHelper.getInstance().from(
                            eventMsg.data.toString(),
                            QuickReplySocket::class.java
                        )
                    if (quickReplySocket.roomId == roomEntity.id) {
                        setQuickReply(quickReplySocket.items)
                    } else {
                    }
                }

                MsgConstant.MESSAGE_AI_CONSULTATION_QUOTED_IMAGE -> {
                    val quotedImage = eventMsg.data as String
                    sendAiQuoteImage(quotedImage)
                }

                MsgConstant.MESSAGE_AI_CONSULTATION_QUOTED_VIDEO -> {
                    val quotedVideo = eventMsg.data as String
                    sendAiQuoteVideo(quotedVideo)
                }

                MsgConstant.NOTICE_CLEAR_CHAT_ROOM_ALL_MESSAGE -> binding.messageRV.adapter!!.clearMessage()
                MsgConstant.UPDATE_SERVICE_BADGE_NUMBER_EVENT -> {
                    val badgeDataModel = eventMsg.data as BadgeDataModel
                    advisoryRoomAdapter.shouldNotifyUnreadIcon(badgeDataModel.roomId)
                }

                MsgConstant.FACEBOOK_COMMENT_DELETE, MsgConstant.FACEBOOK_COMMENT_UPDATE -> {
                    // Facebook 留言編輯
                    val updateRoomId = (eventMsg.data as JSONObject).optString("roomId")
                    if (updateRoomId != roomEntity.id) return
                    val commentId = (eventMsg.data as JSONObject).optString("commentId")
                    val event = (eventMsg.data as JSONObject).optString("event")
                    val facebookCommentStatus = FacebookCommentStatus.of(event)
                    var i = 0
                    while (i < chatViewModel.mainMessageData.size) {
                        val message = chatViewModel.mainMessageData[i]
                        if (message.tag != null) {
                            val facebookTag =
                                JsonHelper.getInstance().from(
                                    message.tag,
                                    FacebookTag::class.java
                                )
                            if (facebookTag.data.commentId == commentId) {
                                if (facebookCommentStatus == FacebookCommentStatus.Delete) {
                                    if (chatViewModel.isThemeOpen && message.id == themeId) {
                                        closeFacebookTheme()
                                        Toast
                                            .makeText(
                                                requireContext(),
                                                getString(R.string.facebook_comment_deleted),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                    }
                                }
                                message.facebookCommentStatus = facebookCommentStatus
                                chatViewModel.refreshFacebookComment(
                                    message,
                                    facebookCommentStatus
                                )
                                return
                            }
                        }
                        i++
                    }
                }

                MsgConstant.FACEBOOK_POST_DELETE -> {
                    // Facebook 貼文刪除
                    val roomIds = (eventMsg.data as JSONObject).optJSONArray("roomIds") ?: return
                    if (!roomIds.toString().contains(roomEntity.id)) return
                    val postId = (eventMsg.data as JSONObject).optString("postId")
                    val postEvent = (eventMsg.data as JSONObject).optString("event")
                    var i = 0
                    while (i < chatViewModel.mainMessageData.size) {
                        val message = chatViewModel.mainMessageData[i]
                        if (message.tag != null) {
                            val facebookTag =
                                JsonHelper.getInstance().from(
                                    message.tag,
                                    FacebookTag::class.java
                                )
                            if (facebookTag.data == null) {
                                i++
                                continue
                            }
                            if (facebookTag.data.postId == null) {
                                i++
                                continue
                            }
                            if (facebookTag.data.postId == postId) {
                                if (chatViewModel.isThemeOpen && message.id == themeId) {
                                    closeFacebookTheme()
                                    Toast
                                        .makeText(
                                            requireContext(),
                                            getString(R.string.facebook_post_deleted),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                }

                                message.facebookPostStatus = FacebookPostStatus.of(postEvent)
                                MessageReference.updateFacebookPostStatus(
                                    roomEntity.id,
                                    message.id,
                                    FacebookPostStatus.Delete
                                )
                                binding.messageRV.notifyChange(message)
                                chatViewModel.updateFacebookPostStatus(message)
                            }
                        }
                        i++
                    }
                }

                MsgConstant.ON_FACEBOOK_PRIVATE_REPLY -> {
                    val facebookReplyId = eventMsg.data as String
                    val facebookMessage = MessageReference.findById(facebookReplyId)
                    binding.messageRV.notifyChange(facebookMessage)
                }

                MsgConstant.UPDATE_LINE_CUSTOMER_AVATAR -> {
                    binding.messageRV.refreshData()
                }

                MsgConstant.Do_UPDATE_CONTACT_BY_LOCAL -> {
                    val contactId = eventMsg.data as String
                    binding.messageRV.adapter?.updateMessageList(contactId)
                }

                MsgConstant.UPDATE_TODO_EXPIRED_COUNT_EVENT,
                MsgConstant.UI_NOTICE_TODO_REFRESH
                -> {
                    chatViewModel.getConsultTodoList(roomEntity.type, roomEntity.id)
                }

                MsgConstant.QUOTE_MESSAGE -> {
                    val quote = eventMsg.data as String
                    binding.chatKeyboardLayout.setInputHETText(quote)
                }

                MsgConstant.QUOTE_AND_SEND_MESSAGE -> {
                    val quote = eventMsg.data as String
                    val sendData = SendData(MessageType.TEXT, quote)
                    chatViewModel.sendButtonClicked(sendData)
                }

                MsgConstant.QUOTE_AND_SEND_TEMPLATE_MESSAGE -> {
                    val quote = eventMsg.data as String
                    val sendData = SendData(MessageType.TEMPLATE, quote)
                    chatViewModel.sendButtonClicked(sendData)
                }

                MsgConstant.QUOTE_AND_SEND_IMAGE_MESSAGE -> {
                    CoroutineScope(Dispatchers.IO).launch {
                        val imageUrl = eventMsg.data as String
                        val image = BitmapUtil.doHandleAiffImage(requireContext(), imageUrl).await()
                        val picture = File(image.first)
                        if (picture.exists()) {
                            image.second?.let {
                                chatViewModel.sendImage(picture.absolutePath, picture.absolutePath, it)
                            }
                        }
                    }
                }

                else -> {}
            }
        }
    }

    // 抓取社團成員的權限
    private fun getMemberPrivilege() {
        chatViewModel.roomEntity?.let {
            chatViewModel.getChatMember(it.id)
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
                binding.messageRV.setAdapterMode(MessageAdapterMode.RANGE_SELECTION)
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
                                        binding.messageRV.switchAnonymous()
                                    }

                                    RichMenuBottom.PREVIEW, RichMenuBottom.NEXT, RichMenuBottom.SAVE, RichMenuBottom.SHARE, RichMenuBottom.CONFIRM -> {
                                        executeScreenshots(RichMenuBottom.NEXT == menu, menu)
                                        binding.clBottomServicedBar.visibility = View.VISIBLE
                                    }

                                    RichMenuBottom.CANCEL -> {
                                        clearScreenshotsFunction()
                                        binding.clBottomServicedBar.visibility = View.VISIBLE
                                    }

                                    else -> {}
                                }
                            }

                            override fun onCancle() {
                                actionStatus = ActionStatus.SCROLL
                                binding.messageRV.setAdapterMode(MessageAdapterMode.DEFAULT)
                            }
                        },
                        null
                    )
                binding.chatKeyboardLayout.hideKeyboard()
            }

            OpenBottomRichMeunType.MULTIPLE_SELECTION -> {
                binding.messageRV.setAdapterMode(MessageAdapterMode.SELECTION)
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
                                for (m in chatViewModel.mainMessageData) {
                                    if (m.isDelete!!) {
                                        messages.add(m)
                                    }
                                }

                                if (messages.isEmpty()) {
                                    Toast.makeText(requireContext(), getString(R.string.text_at_least_select_one_message), Toast.LENGTH_SHORT).show()
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

                            binding.xrefreshLayout.setOnBackgroundClickListener(this@ChatFragment)
                            binding.messageRV.setAdapterMode(MessageAdapterMode.DEFAULT)
                        }

                        override fun onCancle() {
                            actionStatus = ActionStatus.SCROLL
                            binding.messageRV.setAdapterMode(MessageAdapterMode.DEFAULT)
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
     * 處理送出圖片
     */
    private fun executeSendPhotos(
        list: List<AMediaBean>?,
        isOriginal: Boolean
    ) {
        if (list.isNullOrEmpty()) {
            Toast
                .makeText(
                    requireActivity(),
                    getString(R.string.text_select_image_first_please),
                    Toast.LENGTH_SHORT
                ).show()
            return
        }
        val copy = list.toMutableList()
        for (i in copy.indices) {
            val fileType = FileUtil.getFileType(list[i].path)
            when (fileType) {
                Global.FileType_Png, Global.FileType_Jpg, Global.FileType_jpeg, Global.FileType_bmp -> {
                    chatViewModel.sendImage(isOriginal, copy[i].path)
                }
                Global.FileType_gif -> {
                    val bitmapBean = PictureParse.parseGifPath(requireContext(), list[i].path)
                    chatViewModel.sendGifImg(
                        bitmapBean.url,
                        list[i].path,
                        bitmapBean.width,
                        bitmapBean.height
                    )
                }

                Global.FileType_mp4 -> {
                    val iVideoSize: IVideoSize = VideoSizeFromVideoFile(list[i].path)
                    chatViewModel.sendVideo(iVideoSize)
                }

                else -> chatViewModel.sendFile(list[i].path)
            }
        }
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
                    requireActivity(),
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

    /**
     * 執行多筆轉發
     * 完成多筆刪除邏輯。
     * date 2019/09/10
     */
    private fun executeTranspond(messages: List<MessageEntity>) {
        chatViewModel.roomEntity?.let {
            ActivityTransitionsControl.toTransfer(
                requireContext(),
                messages,
                Lists.newArrayList(
                    it.id
                )
            ) { intent: Intent, s: String? ->
                intent.putExtra(
                    BundleKey.ROOM_ID.key(),
                    it.id
                )

                start(requireContext(), intent)
            }
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
            .setContext(requireContext())
            .setStyle(AlertView.Style.Alert)
            .setMessage(getString(R.string.text_only_delete_message_from_yourself))
            .setOthers(arrayOf(getString(R.string.cancel), getString(R.string.text_delete)))
            .setOnItemClickListener { o: Any?, position: Int ->
                if (position == 1) {
//                    if (checkIsThemeMessage(messages)) {
//                        Toast.makeText(
//                            requireActivity(),
//                            getString(R.string.text_theme_message_can_not_be_deleted),
//                            Toast.LENGTH_SHORT
//                        ).show()
//                        return@setOnItemClickListener
//                    }

                    chatViewModel.deleteMessages(messages)
                }
            }.build()
            .setCancelable(true)
            .show()
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
                Toast.makeText(requireActivity(), getString(R.string.text_theme_message_can_not_be_retracted), Toast.LENGTH_SHORT).show()
                continue
            }

            if (!Strings.isNullOrEmpty(m.nearMessageId)) {
                iterator.remove()
                hasThemeMessage = true
            }
        }

        if (hasThemeMessage) {
            Toast.makeText(requireActivity(), getString(R.string.text_theme_message_can_not_be_retracted), Toast.LENGTH_SHORT).show()
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
            val last = chatViewModel.mainMessageData.indexOf(screenShotData[screenShotData.size - 1])
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

        val startIndex =
            min(currentIndex.toDouble(), clickIndex.toDouble()).toInt()
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

    private fun sendAiQuoteImage(imagePath: String) {
        sendFileSize = 1
//        val filePath = PictureParse.parsePath(requireContext(), imagePath)
        chatViewModel.sendImage(isOriginPhoto = true, path = imagePath, isQuote = true)
    }

    private fun sendAiQuoteVideo(videoPath: String) {
        sendFileSize = 1
        val iVideo: IVideoSize = VideoSizeFromVideoFile(videoPath)
        chatViewModel.sendVideo(iVideo, true)
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
            ActivityManager.addActivity(activity as ChatActivity?)
            if (businessContent != null) {
                businessContent.code = BusinessCode.TASK
                businessContent.avatarUrl = ""
            }

            if (BusinessTaskAction.CREATE_BUSINESS == action) {
                ActivityTransitionsControl.navigateToCreateBusinessTask(
                    requireActivity(),
                    action.name,
                    it,
                    businessContent,
                    messageIds,
                    path
                ) { intent: Intent?, s: String? ->
                    start(
                        requireContext(),
                        intent!!
                    )
                }
            } else if (BusinessTaskAction.CREATE_BUSINESS_ROOM == action) {
                ActivityTransitionsControl.navigateToCreateBusinessTask(
                    requireActivity(),
                    action.name,
                    it,
                    businessContent,
                    messageIds,
                    path
                ) { intent: Intent?, s: String? ->
                    start(
                        requireContext(),
                        intent!!
                    )
                }
            } else if (BusinessTaskAction.CREATE_SERVICE_BUSINESS_ROOM == action) {
                ActivityTransitionsControl.navigateToCreateBusinessTask(
                    requireActivity(),
                    action.name,
                    it,
                    businessContent,
                    messageIds,
                    path
                ) { intent: Intent?, s: String? ->
                    start(
                        requireContext(),
                        intent!!
                    )
                }
            } else if (BusinessTaskAction.CREATE_BUSINESS_RELATIONAL == action) {
                ActivityTransitionsControl.navigateToCreateBusinessTask(
                    requireActivity(),
                    action.name,
                    it,
                    businessContent,
                    messageIds,
                    path
                ) { intent: Intent?, s: String? ->
                    start(
                        requireContext(),
                        intent!!
                    )
                }
            }
        }
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

        themeStyle()
        // KeyboardLayout themeStyle
        binding.chatKeyboardLayout.setThemeStyle(RoomThemeStyle.BUSINESS)
        Toast.makeText(requireActivity(), getString(R.string.text_chat_room_connecting_mission), Toast.LENGTH_SHORT).show()
        if (isSendLastMessage) {
            chatViewModel.sendBusiness(content)
        }
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

            val cmb = requireContext().getSystemService(Activity.CLIPBOARD_SERVICE) as ClipboardManager
            cmb.text = builder.toString()
            Toast.makeText(requireContext(), getString(R.string.warning_copied), Toast.LENGTH_SHORT).show()
            this@ChatFragment.actionStatus = ActionStatus.SCROLL
        }
    }

    /**
     * 關閉截圖功能
     */
    private fun clearScreenshotsFunction() {
        for (m in chatViewModel.mainMessageData) {
            m.isShowSelection = false
        }
        screenShotData.clear()
        chatViewModel.isObserverKeyboard = false
        binding.chatKeyboardLayout.showKeyboard()
        if (activity is ChatActivity) {
            (activity as ChatActivity).showToolBar(true)
        }
        themeStyle()
        binding.xrefreshLayout.setOnBackgroundClickListener(this@ChatFragment)
        binding.messageRV
            .setAdapterMode(MessageAdapterMode.DEFAULT)
            .clearAnonymous()
    }

    var height: Int = 0
    var executeCount: Int = 0

    /**
     * ScreenshotsPreview
     * 依照選取範圍截圖
     */
    fun executeScreenshots(
        isTask: Boolean,
        action: RichMenuBottom
    ) {
        if (screenShotData.isEmpty()) {
            return
        }
        height = 0
        executeCount = 0
        showLoadingView(R.string.wording_processing)

        binding.messageRV.setBackgroundColor(Color.WHITE)
        val adapter = binding.messageRV.adapter

        // 獲取item的數量
        // recycler的完整高度 用於創建bitmap時使用
        // 獲取最大可用內存
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        // 使用1/8的緩存
        val cacheSize = maxMemory / 8
        // 把每個item的繪圖緩存存儲在LruCache中
        val bitmapCache = LruCache<String, Bitmap>(cacheSize)
        val indexList: MutableList<Int> = Lists.newArrayList()
        screenShotData.forEach(
            Consumer { selectedMessage: MessageEntity ->
                indexList.add(chatViewModel.mainMessageData.indexOf(selectedMessage))
            }
        )

        for (i in indexList) {
            val holder = adapter!!.getHolder(i)
            adapter.getHolder(i).itemView.isDrawingCacheEnabled = true
            adapter.getHolder(i).itemView.buildDrawingCache()
            holder.setIsRecyclable(false)
            Handler().postDelayed({
                // 測量
                holder.itemView.measure(
                    View.MeasureSpec.makeMeasureSpec(
                        binding.messageRV.width,
                        View.MeasureSpec.EXACTLY
                    ),
                    View.MeasureSpec.makeMeasureSpec(
                        0,
                        View.MeasureSpec.UNSPECIFIED
                    )
                )
                //                //佈局
                holder.itemView.layout(
                    0,
                    0,
                    holder.itemView.measuredWidth,
                    holder.itemView.measuredHeight
                )
                //                //開啟繪圖緩存
                holder.itemView.isDrawingCacheEnabled = true
                holder.itemView.buildDrawingCache()
                val drawingCache = holder.itemView.drawingCache
                if (drawingCache != null) {
                    bitmapCache.put(i.toString(), drawingCache)
                }
                // 獲取itemView的實際高度並累加
                height += holder.itemView.measuredHeight
                executeCount++
                if (executeCount == indexList.size) {
                    // 創建保存截圖的bitmap
                    var bigBitmap: Bitmap? = null
                    // 根據計算出的recyclerView高度創建bitmap
                    bigBitmap =
                        Bitmap.createBitmap(
                            binding.messageRV.measuredWidth,
                            height,
                            Bitmap.Config.RGB_565
                        )
                    // 創建一個canvas畫板
                    val canvas = Canvas(bigBitmap)
                    // 獲取recyclerView的背景顏色
                    val background =
                        binding.messageRV.background
                    // 畫出recyclerView的背景色 這裡只用了color一種 有需要也可以自己擴展
                    if (background is ColorDrawable) {
                        val color = background.color
                        canvas.drawColor(color)
                    }
                    // 當前bitmap的高度
                    var top = 0
                    // 畫筆
                    val paint = Paint()
                    for (ii in indexList) {
                        val bitmap = bitmapCache[ii.toString()]
                        if (bitmap != null) {
                            canvas.drawBitmap(bitmap, 0f, top.toFloat(), paint)
                            top += bitmap.height
                        }
                    }

                    binding.messageRV.setBackgroundColor(Color.TRANSPARENT)

                    // ~~~~~ end ~~~~~~
                    hideLoadingView()
                    try {
                        val file =
                            BitmapHelper.bitmapToFile(
                                bigBitmap,
                                requireContext()
                                    .externalCacheDir!!
                                    .path,
                                "screenshots.jpg"
                            )
                        if (RichMenuBottom.SAVE == action) {
                            saveScreenshots(file.path)
                            clearScreenshotsFunction()
                            return@postDelayed
                        }

                        if (RichMenuBottom.SHARE == action) {
                            chatViewModel.roomEntity?.let {
                                val bundle = Bundle()
                                bundle.putString(BundleKey.FROM_ROOM_IDS.key(), it.id)
                                bundle.putString(BundleKey.FILE_PATH.key(), file.path)
                                bundle.putSerializable(
                                    BundleKey.ROOM_TYPE.key(),
                                    InvitationType.ShareIn.name
                                )
                                launchIntent(
                                    requireContext(),
                                    MemberInvitationActivity::class.java,
                                    shareScreenShotResult,
                                    bundle
                                )
                                clearScreenshotsFunction()
                                return@postDelayed
                            }
                        }

                        if (RichMenuBottom.CONFIRM == action) {
                            executeTask(
                                Lists.newArrayList(),
                                file.path,
                                false
                            )
                            clearScreenshotsFunction()
                            return@postDelayed
                        }

                        val actionName =
                            if (isTask) RichMenuBottom.TASK.name else RichMenuBottom.SAVE.name
                        ActivityTransitionsControl.navigateToScreenshotsPreview(
                            requireActivity(),
                            chatViewModel.roomEntity,
                            file.path,
                            actionName
                        ) { intent: Intent, s: String? ->
                            start(
                                requireContext(),
                                intent
                            )
                            clearScreenshotsFunction()
                        }
                    } catch (e: Exception) {
                        CELog.e(e.message)
                    }
                }
            }, 100L)
        }
    }

    /**
     * 儲存截圖
     */
    private fun saveScreenshots(filePath: String) {
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
            requireActivity().sendBroadcast(mediaScanIntent)
            Toast
                .makeText(
                    requireActivity(),
                    String.format(getString(R.string.bruce_photo_save), saveImagePath),
                    Toast.LENGTH_SHORT
                ).show()
        }
    }

    /**
     * 執行重發邏輯
     * date 2019/09/10
     */
    private fun executeReRetry(message: MessageEntity) {
        chatViewModel.mainMessageData.remove(message)
        chatViewModel.retrySend(message)
        this@ChatFragment.actionStatus = ActionStatus.SCROLL
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
        this@ChatFragment.actionStatus = ActionStatus.SCROLL
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
                val ssb =
                    AtMatcherHelper.matcherAtUsers("@", ceMentions, it.membersTable)
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
            start(requireContext(), sendIntent)
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
            start(requireContext(), sendIntent)
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
            start(requireContext(), sendIntent)
        }
    }

    /**
     * 分享 圖片訊息
     */
    private fun shareImage(imageContent: ImageContent) =
        CoroutineScope(Dispatchers.IO).launch {
            chatViewModel.roomEntity?.let { room ->
                val path = imageContent.url
                if (URLUtil.isValidUrl(path)) {
                    try {
                        val bitmap =
                            Glide
                                .with(requireContext())
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
                                requireContext().contentResolver.insert(
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    values
                                )
                            uri?.let {
                                requireContext().contentResolver.openOutputStream(it)?.use { outputStream ->
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
                                start(requireContext(), sendIntent)
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
                    val imageEntity = DaVinci.with(requireContext()).imageLoader.getImage(path)
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
                            requireContext().contentResolver.insert(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                values
                            )
                        uri?.let {
                            requireContext().contentResolver.openOutputStream(it)?.use { outputStream ->
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
                            start(requireContext(), sendIntent)
                        } ?: run {
                            showErrorToast(getString(R.string.text_share_media_failure))
                        }
                    } ?: run {
                        showErrorToast(getString(R.string.api_http_failure))
                    }
                }
            }
        }

    /**
     * 顯示 Facebook 回覆
     */
    private fun executeFacebookReply(message: MessageEntity) {
        isReply = true
        var themeId = ""
        binding.themeMRV.setMaxHeight(setupDefaultThemeHeight(2.0 / 3.0))
        binding.themeMRV.visibility = View.VISIBLE
        chatViewModel.isThemeOpen = true
        chatViewModel.themeMessage = binding.themeMRV.themeData
        chatViewModel.nearMessage = binding.themeMRV.nearData
        themeId = if (!Strings.isNullOrEmpty(message.themeId)) message.themeId ?: "" else message.id ?: ""
        showFacebookThemeView(themeId)
        actionStatus = ActionStatus.SCROLL
    }

    /**
     * 顯示 Facebook 主題聊天室
     */
    private fun showFacebookThemeView(themeId: String) {
        if (binding.searchBottomBar.visibility == View.VISIBLE) {
            return
        }
        binding.expandIV.visibility = View.GONE
        binding.themeMRV.clearData()
        binding.themeMRV.setMaxHeight(setupDefaultThemeHeight(2.0 / 3.0))
        val themeMessages = findFirstMessageByThemeId(themeId)
        themeMessages?.let {
            displayThemeMessage(themeId, false, it)
        }
        binding.themeMRV.visibility = View.VISIBLE
        chatViewModel.isThemeOpen = true
        chatViewModel.themeMessage = binding.themeMRV.themeData
        chatViewModel.nearMessage = binding.themeMRV.nearData
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

    fun findFirstMessageByThemeId(themeId: String): MessageEntity? {
        for (message in chatViewModel.mainMessageData) {
            if (message.id == themeId) {
                return message
            }
        }
        return null
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
        this@ChatFragment.actionStatus = ActionStatus.SCROLL
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
                requireContext(),
                entity.roomId,
                entity.id,
                Lists.newArrayList(entity)
            )
        todoSettingDialog.setRemindListener(setRemindListener)
        todoSettingDialog.show()
        this@ChatFragment.actionStatus = ActionStatus.SCROLL
    }

    private val setRemindListener =
        OnSetRemindTime { isRemind: Boolean ->
            if (isRemind) {
                if (!Settings.canDrawOverlays(requireContext())) {
                    val today = DateTime()
                    val setNoticeDate =
                        DateTime(
                            TokenPref.getInstance(requireContext()).remindNotice
                        )

                    if (today.isAfter(setNoticeDate)) {
                        val others =
                            arrayOf(
                                getString(R.string.cancel),
                                getString(R.string.text_sure)
                            )
                        AlertView
                            .Builder()
                            .setContext(requireContext())
                            .setStyle(AlertView.Style.Alert)
                            .setMessage(getString(R.string.text_tip_for_float_window_permission))
                            .setOthers(others)
                            .setOnItemClickListener { o: Any?, pos: Int ->
                                if (pos == 1) {
                                    val intent2 =
                                        Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                                    if (requireContext() is Activity) {
                                        requireActivity().startActivityForResult(
                                            intent2,
                                            TodoListFragment.REQUEST_CODE
                                        )
                                    }
                                } else {
                                    val dt = DateTime()
                                    val forPattern =
                                        DateTimeFormat.forPattern("yyyy-MM-dd")
                                    val dtp =
                                        forPattern.parseDateTime(
                                            dt.plusDays(7).toString("yyyy-MM-dd")
                                        )
                                    // 拒絕給予權限時，紀錄時間，一週後再問一次
                                    TokenPref.getInstance(requireContext()).remindNotice =
                                        dtp.millis
                                    Toast
                                        .makeText(
                                            requireContext(),
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
                                requireContext(),
                                getString(R.string.text_tip3_for_float_window_permission),
                                Toast.LENGTH_SHORT
                            ).show()
                    }
                }
            }
        }

    /**
     * 底部進階選單，依照分類建立按鈕
     * date 2019/09/10
     */
    private fun setupRichMenuData(
        type: RichMenuType,
        msg: MessageEntity
    ): List<RichMenuBottom> {
        // EVAN_FLAG 2020-03-03 (1.10.0) 長按先判斷是否為主題聊天串，如果是，則處理收回與刪除不同邏輯
        var hasThemeMessage = false
        if (checkIsThemeMessage(msg)) {
            hasThemeMessage = true
        }
        if (!Strings.isNullOrEmpty(msg.nearMessageId)) {
            hasThemeMessage = true
        }

        val gridNames: MutableList<RichMenuBottom> = Lists.newArrayList()
        if (RichMenuType.CALL_RICH == type) {
            gridNames.addAll(type.get())
            return gridNames
        }

        if (msg.senderId == selfUserId) {
            try {
                val value = msg.status?.value ?: -1
                val entity = ChatRoomReference.getInstance().findById(msg.roomId)
                // 改服務號可回收信息
                if ((
                        value > 0 &&
                            value != 2 &&
                            ChatRoomType.subscribe != entity.type
                    ) &&
                    (System.currentTimeMillis() - msg.sendTime) / 1000 <= TokenPref
                        .getInstance(
                            requireActivity()
                        ).retractValidMinute * 60L
                ) {
                    gridNames.add(RichMenuBottom.RECOVER)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        gridNames.addAll(type.get())

        // EVAN_FLAG 2020-03-03 (1.10.0) 如果是主題串
        if (hasThemeMessage) {
            gridNames.remove(RichMenuBottom.DELETE)
            gridNames.remove(RichMenuBottom.RECOVER)
        }

        // EVAN_FLAG 2020-03-03 (1.10.0) 如果是失敗訊息只給 刪除功能
        if (MessageStatus.FAILED_or_ERROR.contains(msg.status)) {
            return Lists.newArrayList(RichMenuBottom.DELETE)
        }
        return gridNames
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

    fun doFloatingLastMessageClickAction(view: View) {
        view.visibility = View.GONE
        binding.messageRV.refreshToBottom(actionStatus.status())
    }

    /**
     * 內部訊息搜索模式開啟
     */
    fun doSearchAction(
        searchBar: View,
        editText: TextInputEditText,
        clearInput: AppCompatImageView
    ) {
        binding.scopeProvisionalMemberList.visibility = View.GONE
        searchBar.visibility = View.VISIBLE
        binding.scopeSearch.visibility = View.VISIBLE
        binding.scopeSearch.setBackgroundColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.white
            )
        )
        // 關閉訊息點擊事件
        binding.chatKeyboardLayout.invisibleKeyboard()
        isFloatViewOpenAndExecuteClose
        val searchMessages: MutableList<MessageEntity> = Lists.newArrayList()
        binding.searchBottomBar.visibility = View.GONE
        binding.scopeSectioned.visibility = View.GONE
        binding.searchRV.layoutManager = LinearLayoutManager(requireContext())
        binding.searchRV.itemAnimator = DefaultItemAnimator()
        binding.searchRV.setHasFixedSize(true)

        chatRoomMemberTable.clear()
        chatViewModel.roomEntity?.let {
            it.members?.let { members ->
                for (user in members) {
                    chatRoomMemberTable[user.id] =
                        if (!Strings.isNullOrEmpty(user.alias)) user.alias else user.nickName
                }
            }
        }
        chatViewModel.sendShowLoadMoreMsg.observe(
            viewLifecycleOwner
        ) { messageEntities: List<MessageEntity> ->
            val allSearchMessages = adapter.getData()
            val origin = allSearchMessages.size
            sortMessageByDate(
                messageEntities,
                SortType.DESC
            )
            adapter.setData(
                chatViewModel.getSearchMessageList(messageEntities, true),
                keyWord,
                chatRoomMemberTable
            )
            binding.sectionedTitle.text = getString(R.string.text_sectioned_search_news, messageEntities.size)
            if (messageEntities.size > origin) binding.searchRV.scrollToPosition(origin + 1)
        }
        adapter =
            ChatRoomMessageSearchAdapter(
                object : OnMessageItemClick {
                    override fun onItemClick(
                        item: MessageEntity,
                        position: Int,
                        itemView: View
                    ) {
                        binding.scopeSearch.visibility = View.GONE
                        binding.searchBottomBar.visibility = View.VISIBLE
                        binding.searchBottomBar.setBackgroundColor(if (isGreenTheme && themeStyle != RoomThemeStyle.SERVICES) resources.getColor(R.color.color_015F57, null) else themeStyle?.mainColor ?: R.color.color_6BC2BA)
                        binding.scopeSectioned.visibility = View.GONE
                        searchSelectorIndex = position
                        binding.indicatorTV.text =
                            MessageFormat.format(
                                "{0}/{1}",
                                searchSelectorIndex + 1,
                                adapter.getData().size
                            )
                        doScrollToSelectMessageItemAndSetKeyword(keyWord, item)
                    }

                    override fun onLoadMoreClick() {
                        if (chatViewModel.mainMessageData.isNotEmpty()) {
                            val msg = chatViewModel.getFirstMsgId()
                            chatViewModel.searchMoreMsg(msg)
                        } else {
                            chatViewModel.searchMoreMsg(null)
                            stopRefresh()
                        }
                    }
                },
                isGreenTheme
            )
        binding.searchRV.adapter = adapter
        binding.scopeSectioned.setOnClickListener { v ->
            binding.scopeSearch.visibility =
                if (binding.scopeSearch.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            binding.icExtend.setImageResource(if (binding.scopeSearch.visibility == View.VISIBLE) R.drawable.ic_arrow_top else R.drawable.ic_arrow_down)
            binding.emptyLayout.visibility =
                if (binding.scopeSearch.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
        editText.addTextChangedListener(
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
                    clearInput.visibility = if (s.toString().isEmpty()) View.GONE else View.VISIBLE
                }
            }
        )
        clearInput.setOnClickListener { v: View? ->
            editText.text?.clear()
            binding.scopeSectioned.visibility = View.GONE
            adapter.setData(
                Lists.newArrayList(),
                "",
                Maps.newHashMap()
            )
            KeyboardHelper.open(searchBar)
            binding.searchBottomBar.visibility = View.GONE
        }
        editText.setOnEditorActionListener { v: TextView, actionId: Int, event: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (Strings.isNullOrEmpty(
                        v.text.toString().trim { it <= ' ' }
                    )
                ) {
                    Toast
                        .makeText(requireActivity(), getString(R.string.text_input_search_key_word), Toast.LENGTH_SHORT)
                        .show()
                } else {
                    KeyboardHelper.hide(searchBar)
                    doMessageSearch(
                        binding.scopeSearch,
                        adapter,
                        v.text.toString(),
                        searchMessages,
                        binding.indicatorTV,
                        binding.upIV,
                        binding.downIV
                    )
                    editText.clearFocus()
                }
                return@setOnEditorActionListener true
            }
            false
        }

        binding.downIV.setOnClickListener { v ->
            searchSelectorIndex--
            if (searchSelectorIndex < 0) {
                searchSelectorIndex = 0
            }
            doSearchSelectors(
                binding.upIV,
                v,
                editText,
                binding.indicatorTV,
                adapter.getData()
            )
        }

        binding.upIV.setOnClickListener { v ->
            searchSelectorIndex++
            if (searchSelectorIndex >= adapter.getData().size - 1) {
                searchSelectorIndex = adapter.getData().size - 1
            }
            doSearchSelectors(
                v,
                binding.downIV,
                editText,
                binding.indicatorTV,
                adapter.getData()
            )
        }
    }

    /**
     * 內部訊息搜索結果
     */
    private fun doSearchSelectors(
        upIV: View,
        downIV: View,
        editText: TextInputEditText,
        tv: TextView,
        searchMessages: List<MessageEntity>
    ) {
        tv.text = MessageFormat.format("{0}/{1}", searchSelectorIndex + 1, searchMessages.size)
        upIV.isEnabled = true
        upIV.alpha = 1.0f
        downIV.isEnabled = true
        downIV.alpha = 1.0f
        if (searchSelectorIndex <= 0) {
            downIV.isEnabled = false
            downIV.alpha = 0.3f
        } else if (searchSelectorIndex >= searchMessages.size - 1) {
            upIV.isEnabled = false
            upIV.alpha = 0.3f
        }

        val message = searchMessages[searchSelectorIndex]
        val keyword = Objects.requireNonNull(editText.text).toString()
        doScrollToSelectMessageItemAndSetKeyword(keyword, message)
    }

    /**
     * 內部訊息搜索，移動上下鍵到該被搜索到的訊息位置
     */
    private fun doScrollToSelectMessageItemAndSetKeyword(
        keyword: String,
        message: MessageEntity
    ) {
        val index = chatViewModel.mainMessageData.indexOf(message)
        if (index != 0) {
            val entity = chatViewModel.mainMessageData[index]
            entity.isAnimator = true
            (binding.messageRV.layoutManager as LinearLayoutManager?)!!.scrollToPositionWithOffset(
                index - 1,
                0
            )
            binding.messageRV.adapter!!.notifyItemChanged(index, entity)
        } else {
            (binding.messageRV.layoutManager as LinearLayoutManager?)!!.scrollToPositionWithOffset(
                0,
                0
            )
            binding.messageRV.adapter!!.notifyItemChanged(0)
        }
    }

    var searchSelectorIndex: Int = 0

    private fun doMessageSearch(
        resultLayout: View,
        adapter: ChatRoomMessageSearchAdapter,
        keyword: String,
        searchMessages: MutableList<MessageEntity>,
        tv: TextView,
        vararg controllers: View
    ) {
        if (resultLayout.visibility == View.GONE) {
            resultLayout.visibility = View.VISIBLE
        }
        searchSelectorIndex = 0
        searchMessages.clear()
        searchMessages.addAll(
            chatViewModel.filterMessageFromLocalDB(
                keyword,
                chatViewModel.mainMessageData
            )
        )
        binding.sectionedTitle.text =
            getString(R.string.text_sectioned_search_news, searchMessages.size)
        resultLayout.setBackgroundColor(
            ContextCompat.getColor(
                requireActivity(),
                if (!Strings.isNullOrEmpty(keyword) && searchMessages.isNotEmpty()) R.color.white else R.color.transparent
            )
        )
        binding.scopeSectioned.visibility =
            if (!Strings.isNullOrEmpty(keyword) && searchMessages.isNotEmpty()) View.VISIBLE else View.GONE
        tv.text = MessageFormat.format("{0}/{1}", searchSelectorIndex + 1, searchMessages.size)

        for (v in controllers) {
            v.isEnabled = searchMessages.size != 0
            v.alpha = if (searchMessages.size == 0) 0.3f else 1.0f
        }
        sortMessageByDate(searchMessages, SortType.DESC)
        keyWord = keyword
        if (searchMessages.isEmpty()) {
            Toast
                .makeText(
                    requireActivity(),
                    getString(R.string.text_not_find_any_result),
                    Toast.LENGTH_SHORT
                ).show()
            return
        } else {
            adapter.setData(
                chatViewModel.getSearchMessageList(searchMessages, true),
                keyword,
                chatRoomMemberTable
            )
        }

        binding.messageRV.setKeyword(keyword).refreshData()
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
                themeStyle()
                if (activity is ChatActivity) {
                    (activity as ChatActivity).showToolBar(true)
                }
                screenShotData.clear()
                binding.messageRV.adapter!!.setAnonymous(false)
                binding.messageRV.setAdapterMode(MessageAdapterMode.DEFAULT)
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
                if (isMetaReplyOverTime) {
                    binding.chatKeyboardLayout.metaOverTimeView.visibility = View.VISIBLE
                }
                return true
            }
            return false
        }

    /**
     * 內部訊息搜索模式關閉
     */
    fun doSearchCancelAction() {
        searchSelectorIndex = 0
        binding.indicatorTV.text = "0/0"
        binding.chatKeyboardLayout.showKeyboard()
        binding.scopeSearch.visibility = View.GONE
        binding.scopeSectioned.visibility = View.GONE
        binding.searchBottomBar.visibility = View.GONE
        // 打開訊息點擊事件
        binding.messageRV.adapter
            ?.setMode(MessageAdapterMode.DEFAULT)
            ?.setKeyword("")
            ?.refreshData()
        binding.chatKeyboardLayout.inputHET.requestFocus()
        chatViewModel.roomEntity?.let {
            if (it.provisionalIds.isNotEmpty() && it.roomType != ChatRoomType.provisional) {
                binding.scopeProvisionalMemberList.visibility = View.VISIBLE
            }
        }
    }

    /**
     * 回覆聊天室預設值樣式
     */
    fun setupToDefault() {
        // 進階選單 & 關閉標註模式 & 關閉主題聊天室
        if (!this.isActivityForResult) {
            isFloatViewOpenAndExecuteClose
        }
        // 關閉標註模式
        binding.chatKeyboardLayout.hideMention(null, "")
        // 底部浮動信息欄
        if (binding.floatingLastMessageTV.visibility == View.VISIBLE) {
            binding.floatingLastMessageTV.visibility = View.GONE
        }
    }

    fun hideKeyboard() {
        binding.chatKeyboardLayout.hideKeyboard()
        binding.chatKeyboardLayout.showKeyboard()
    }

    private fun showFunMedia(isChange: Boolean) {
        if (isChange) {
            binding.funMedia.setChangeVisibility()
        } else {
            binding.funMedia.setType(
                requireContext(),
                MultimediaHelper.Type.IMAGE,
                themeStyle,
                MEDIA_SELECTOR_REQUEST_CODE
            )
        }

        binding.funMedia.visibility = View.VISIBLE
    }

    // AIFF聊天室消息菜單
    private fun classifyAiffInMenu(entity: ChatRoomEntity?): List<RichMenuInfo> {
        val aiffList: MutableList<RichMenuInfo> = Lists.newArrayList()
        // Aiff消息菜單
        val aiffInfoList = AiffDB.getInstance(requireContext()).aiffInfoDao.aiffInfoListByUseTime
        if (aiffInfoList.size > 0) {
            for (aiff in aiffInfoList) {
                if (aiff.embedLocation == AiffEmbedLocation.MessageMenu.name) {
                    if (ChatRoomType.self == entity!!.type) {
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
                    } else if (ChatRoomType.system == entity.type) {
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
                    } else if (ChatRoomType.discuss == entity.type) {
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
                    } else if (ChatRoomType.friend == entity.type) {
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
                    } else if (ChatRoomType.group == entity.type) {
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
                    } else if (ChatRoomType.serviceMember == entity.type && ServiceNumberType.BOSS == entity.serviceNumberType) {
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
                    } else if (ChatRoomType.services == entity.type && ServiceNumberType.BOSS == entity.serviceNumberType) {
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
                    } else if (ChatRoomType.services == entity.type &&
                        entity.serviceNumberOpenType.contains(
                            "I"
                        ) &&
                        entity.ownerId != userId
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
                    } else if (ChatRoomType.services == entity.type &&
                        entity.serviceNumberOpenType.contains(
                            "I"
                        ) &&
                        entity.ownerId == userId
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
                    } else if (ChatRoomType.serviceMember == entity.type && ServiceNumberType.BOSS != entity.serviceNumberType) {
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
                    } else if (ChatRoomType.services == entity.type &&
                        entity.serviceNumberOpenType.contains(
                            "O"
                        ) &&
                        entity.ownerId != userId
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
        return aiffList
    }

    override fun onNetworkTime(time: Long) {
        if (servicedDurationTime == 0L) {
            servicedDurationTime = time
        }
        countServiceDuration()
    }

    inner class TimeZoneChangeReceiver : BroadcastReceiver() {
        override fun onReceive(
            context: Context,
            intent: Intent
        ) {
            val action = intent.action
            if (Intent.ACTION_TIMEZONE_CHANGED == action) {
                refreshListView()
            }
        }
    }
}
