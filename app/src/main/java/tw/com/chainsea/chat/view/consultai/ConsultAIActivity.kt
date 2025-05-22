package tw.com.chainsea.chat.view.consultai

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.common.collect.Lists
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.android.common.event.KeyboardHelper
import tw.com.chainsea.android.common.json.JsonHelper
import tw.com.chainsea.android.common.log.CELog
import tw.com.chainsea.android.common.multimedia.AMediaBean
import tw.com.chainsea.android.common.multimedia.MultimediaHelper
import tw.com.chainsea.android.common.ui.UiHelper
import tw.com.chainsea.ce.sdk.bean.msg.MessageStatus
import tw.com.chainsea.ce.sdk.bean.msg.MessageType
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEnum
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType
import tw.com.chainsea.ce.sdk.bean.room.QuickReplyItem
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.event.EventMsg
import tw.com.chainsea.ce.sdk.event.MsgConstant
import tw.com.chainsea.ce.sdk.event.MsgConstant.MESSAGE_AI_CONSULTATION_QUOTED_IMAGE
import tw.com.chainsea.ce.sdk.event.MsgConstant.MESSAGE_AI_CONSULTATION_QUOTED_VIDEO
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference
import tw.com.chainsea.ce.sdk.reference.UserProfileReference
import tw.com.chainsea.ce.sdk.socket.ce.bean.AiConsultMessageSocket
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.config.BundleKey
import tw.com.chainsea.chat.databinding.ActivityConsultAiBinding
import tw.com.chainsea.chat.keyboard.ChatKeyboardLayout.OnChatKeyBoardListener
import tw.com.chainsea.chat.keyboard.view.HadEditText.SendData
import tw.com.chainsea.chat.lib.ToastUtils
import tw.com.chainsea.chat.mediagallery.view.MediaGalleryActivity
import tw.com.chainsea.chat.messagekit.enums.RichMenuBottom
import tw.com.chainsea.chat.messagekit.listener.OnMainMessageControlEventListener
import tw.com.chainsea.chat.messagekit.listener.OnMainMessageScrollStatusListener
import tw.com.chainsea.chat.messagekit.listener.OnTemplateClickListener
import tw.com.chainsea.chat.messagekit.main.adapter.BottomRichMeunAdapter
import tw.com.chainsea.chat.network.contact.ViewModelFactory
import tw.com.chainsea.chat.style.RoomThemeStyle
import tw.com.chainsea.chat.ui.adapter.QuickReplyAdapter
import tw.com.chainsea.chat.util.IntentUtil.startIntent
import tw.com.chainsea.chat.util.ThemeHelper
import tw.com.chainsea.chat.util.TimeUtil
import tw.com.chainsea.chat.util.UnreadUtil
import tw.com.chainsea.chat.view.BaseActivity
import tw.com.chainsea.chat.view.todo.OnSetRemindTime
import tw.com.chainsea.chat.view.todo.TodoListFragment
import tw.com.chainsea.chat.view.todo.TodoSettingDialog
import tw.com.chainsea.custom.view.alert.AlertView
import java.util.TreeMap

class ConsultAIActivity : BaseActivity() {
    private lateinit var binding: ActivityConsultAiBinding
    private val consultAIViewModel: ConsultAIViewModel by lazy {
        val viewModelFactory = ViewModelFactory(application)
        ViewModelProvider(this, viewModelFactory)[ConsultAIViewModel::class.java]
    }

    private val serviceNumberId by lazy {
        intent.getStringExtra(BundleKey.SERVICE_NUMBER_ID.key())
    }

    private val serviceNumberRoomId by lazy {
        intent.getStringExtra(BundleKey.ROOM_ID.key())
    }

    private val consultId by lazy {
        intent.getStringExtra(BundleKey.CONSULT_AI_ID.key())
    }

    private val selfProfile by lazy {
        UserProfileReference.findById(null, TokenPref.getInstance(application).userId)
    }

    private var onPermissionGranted: () -> Unit = {} // 權限請求成功

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val permissionsTIRAMISU: Array<String> =
        arrayOf(
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_IMAGES
        )
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            // Handle Permission granted/rejected
            if (isGranted) {
                onPermissionGranted()
            } else {
                ToastUtils.showToast(
                    this,
                    getString(R.string.text_need_storage_permission)
                )
            }
        }
    private val requestMultiplePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            result.forEach { (permission, isGranted) ->
                if (!isGranted) {
                    if (permission == Manifest.permission.READ_MEDIA_IMAGES || permission == Manifest.permission.READ_MEDIA_VIDEO) {
                        ToastUtils.showToast(this, getString(R.string.text_need_storage_permission))
                    }
                    return@registerForActivityResult
                }
            }
            onPermissionGranted()
        }

    private val mainMessageData = mutableListOf<MessageEntity>()

    private var timeBoxTarget: Runnable =
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
                                getString(R.string.alert_cancel),
                                getString(R.string.alert_confirm)
                            )
                        AlertView
                            .Builder()
                            .setContext(this)
                            .setStyle(AlertView.Style.Alert)
                            .setMessage("為了讓您有更好的操作體驗，請允許使用浮動視窗權限。")
                            .setOthers(others)
                            .setOnItemClickListener { o: Any?, pos: Int ->
                                if (pos == 1) {
                                    val intent2 =
                                        Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                                    startActivityForResult(
                                        intent2,
                                        TodoListFragment.REQUEST_CODE
                                    )
                                } else {
                                    val dt = DateTime()
                                    val forPattern =
                                        DateTimeFormat.forPattern("yyyy-MM-dd")
                                    val dtp =
                                        forPattern.parseDateTime(
                                            dt.plusDays(7).toString("yyyy-MM-dd")
                                        )
                                    // 拒絕給予權限時，紀錄時間，一週後再問一次
                                    TokenPref.getInstance(this).remindNotice = dtp.millis
                                    Toast
                                        .makeText(
                                            this,
                                            "許可權授予失敗，無法開啟浮動視窗",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                }
                            }.build()
                            .setCancelable(true)
                            .show()
                    } else {
                        Toast
                            .makeText(this, "請賦予懸浮視窗權限以獲得最即時的通知", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeHelper.updateServiceChatRoomTheme(isServiceRoom = false)
        binding = ActivityConsultAiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        CoroutineScope(Dispatchers.IO).launch {
            initTitle()
            initKeyboard()
            initMessageList().await()
            initListener()
            observerData()
            getUnreadData()
            consultId?.let {
                consultAIViewModel.getAIHistoryMessageList(it, serviceNumberRoomId!!)
            }
        }
    }

    private fun initTitle() {
        binding.title.text = getString(R.string.consult_ai_text)
    }

    private fun initKeyboard() {
        binding.chatKeyboardLayout.hideEmojiFeature()
        binding.chatKeyboardLayout.hideLeftFeature()
        binding.chatKeyboardLayout.setThemeStyle(RoomThemeStyle.DISCUSS)
        binding.chatKeyboardLayout.setInputHint(getString(R.string.text_hint_input_message)) // 預設
        binding.chatKeyboardLayout.setRecordEnable(false)
    }

    private fun initListener() =
        CoroutineScope(Dispatchers.Main).launch {
            binding.leftAction.setOnClickListener {
                KeyboardHelper.hide(this@ConsultAIActivity.currentFocus)
                finish()
            }
            binding.xrefreshLayout.setOnBackgroundClickListener { binding.chatKeyboardLayout.showKeyboard() }
            binding.difbDown.setOnClickListener {
                binding.messageRV.adapter?.let {
                    binding.messageRV.scrollToPosition(it.itemCount - 1)
                    val bottomDistance = UiHelper.dip2px(this@ConsultAIActivity, 10f)
                    binding.difbDown
                        .animate()
                        .translationY((binding.difbDown.height + bottomDistance).toFloat())
                        .setInterpolator(LinearInterpolator())
                        .start()
                }
            }
            binding.chatKeyboardLayout.setOnKeyBoardBarListener(
                object : OnChatKeyBoardListener {
                    override fun onSendBtnClick(
                        sendData: SendData,
                        enableSend: Boolean
                    ) {
                        consultAIViewModel.sendConsultAIMessage(
                            serviceNumberRoomId!!,
                            serviceNumberId!!,
                            sendData.content
                        )
                        binding.chatKeyboardLayout.clearInputArea()
                        binding.rvQuickReplyList.visibility = View.GONE
                    }

                    override fun onRecordingSendAction(
                        path: String,
                        duration: Int
                    ) {
                    }

                    override fun onRecordingStartAction() {
                    }

                    override fun onUserDefEmoticonClicked(
                        tag: String,
                        uri: String
                    ) {
                    }

                    override fun onStickerClicked(
                        stickerId: String,
                        packageId: String
                    ) {
                    }

                    override fun onOpenCamera() {
                    }

                    override fun onOpenVideo() {
                    }

                    override fun onOpenFolders() {
                    }

                    override fun onOpenGallery() {
                    }

                    override fun onOpenPhotoSelector(isChange: Boolean) {
                    }

                    override fun onOpenConsult() {
                    }

                    override fun toMediaSelectorPreview(
                        isOriginal: Boolean,
                        type: String,
                        current: String,
                        data: TreeMap<String, String>,
                        position: Int
                    ) {
                    }

                    override fun onOpenMultimediaSelector() {
                    }

                    override fun onOpenRecord() {
                    }

                    override fun onOpenEmoticon() {
                    }

//                    override fun onPicSelected(list: List<PhotoBean>) {
//                    }

                    override fun onMediaSelector(
                        type: MultimediaHelper.Type,
                        list: List<AMediaBean>,
                        isOriginal: Boolean
                    ) {
                    }

                    override fun onSoftKeyboardStartOpened(keyboardHeightInPx: Int) {
                    }

                    override fun onSoftKeyboardEndOpened(keyboardHeightInPx: Int) {
                        if (binding.funMedia.visibility != View.VISIBLE) {
                            val lastItemPosition = binding.messageRV.lastItemPosition
                            binding.messageRV.post {
                                binding.messageRV.mScrollToPosition(lastItemPosition)
                            }
                        }
                    }

                    override fun onInputClick() {
                    }

                    override fun onSoftKeyboardClosed() {
                    }

                    override fun onOpenExtraArea() {
                    }

                    override fun onBusinessCardSend() {
                    }

                    override fun onBusinessMemberCardSend() {
                    }

                    override fun onOpenCameraDialog() {
                    }

                    override fun onSlideUpSendImage(
                        type: MultimediaHelper.Type?,
                        list: MutableList<AMediaBean>?,
                        isOriginal: Boolean
                    ) {
                    }
                }
            )

            // 設定滾動狀態監聽器
            binding.messageRV.setOnMainMessageScrollStatusListener(
                object :
                    OnMainMessageScrollStatusListener {
                    override fun onStopScrolling(recyclerView: RecyclerView) {
                        // 获取最后一个可见view的位置

                        val layoutManager = recyclerView.layoutManager
                        if (layoutManager is LinearLayoutManager) {
                            val lastItemPosition = layoutManager.findLastVisibleItemPosition()
                            val firstItemPosition = layoutManager.findFirstVisibleItemPosition()
                            if (firstItemPosition < mainMessageData.size - 10 && firstItemPosition > -1) {
                                binding.difbDown.visibility = View.VISIBLE
                                binding.difbDown
                                    .animate()
                                    .translationY(0f)
                                    .setInterpolator(LinearInterpolator())
                                    .start()
                            }

                            if (mainMessageData.isNotEmpty() && lastItemPosition == mainMessageData.size - 1) {
                                val bottomDistance = UiHelper.dip2px(this@ConsultAIActivity, 55f)
                                binding.difbDown
                                    .animate()
                                    .translationY((binding.difbDown.height + bottomDistance).toFloat())
                                    .setInterpolator(LinearInterpolator())
                                    .start()
                            }
                        }
                    }

                    override fun onDragScrolling(recyclerView: RecyclerView) {
                        val layoutManager = recyclerView.layoutManager
                        if (layoutManager is LinearLayoutManager) {
                            val index = layoutManager.findFirstVisibleItemPosition()

                            if (mainMessageData.size > index && index > -1) {
                                val message: MessageEntity = mainMessageData[index]
                                val dateTime = TimeUtil.getDateShowString(message.sendTime, true)
                                binding.floatTimeBoxTV.text = dateTime
                                binding.floatTimeBoxTV.visibility = View.VISIBLE
                                binding.floatTimeBoxTV.alpha = 1.0f
                                binding.floatTimeBoxTV.removeCallbacks(timeBoxTarget)
                                binding.floatTimeBoxTV.postDelayed(timeBoxTarget, 1500L)
                            }
                        }
                    }

                    override fun onAutoScrolling(recyclerView: RecyclerView) {
                    }
                }
            )

            binding.messageRV.setOnMessageControlEventListener(
                object :
                    OnMainMessageControlEventListener<MessageEntity>() {
                    override fun makeUpMessages(
                        current: MessageEntity?,
                        previous: MessageEntity?
                    ) {
                    }

                    override fun doRangeSelection(entity: MessageEntity?) {
                    }

                    override fun onItemClick(entity: MessageEntity?) {
                    }

                    override fun onItemChange(entity: MessageEntity?) {
                    }

                    override fun onInvalidAreaClick(entity: MessageEntity?) {
                    }

                    override fun onImageClick(entity: MessageEntity?) {
                        val mediaData =
                            mainMessageData.filter {
                                it.type == MessageType.IMAGE || it.type == MessageType.VIDEO
                            }
                        val bundle =
                            bundleOf(
                                BundleKey.PHOTO_GALLERY_MESSAGE.key() to entity,
                                BundleKey.ROOM_TYPE.key() to ChatRoomEnum.AI_CONSULTATION_ROOM.name,
                                BundleKey.EXTRA_SESSION_LIST.key() to JsonHelper.getInstance().toJson(mediaData),
                                BundleKey.ROOM_ID.key() to serviceNumberRoomId
                            )
                        startIntent(this@ConsultAIActivity, MediaGalleryActivity::class.java, bundle)
                    }

                    override fun onLongClick(
                        msg: MessageEntity,
                        pressX: Int,
                        pressY: Int
                    ) {
                        if (msg.type == MessageType.TEMPLATE) return
                        consultAIViewModel.getBottomRichMenu(msg)
                        binding.chatKeyboardLayout.hideKeyboard()
                    }

                    override fun onTipClick(entity: MessageEntity?) {
                        CELog.d("onTipClick")
                    }

                    override fun onAvatarClick(senderId: String?) {
                        CELog.d("onAvatarClick")
                    }

                    override fun onSubscribeAgentAvatarClick(senderId: String?) {
                        CELog.d("onSubscribeAgentAvatarClick")
                    }

                    override fun onAtSpanClick(userId: String?) {
                        CELog.d("onAtSpanClick")
                    }

                    override fun onAvatarLoad(
                        iv: ImageView?,
                        senderId: String?
                    ) {
                        CELog.d("onAvatarLoad")
                    }

                    override fun onSendNameClick(sendId: String?) {
                        CELog.d("onSendNameClick")
                    }

                    override fun onContentUpdate(
                        msgId: String?,
                        formatName: String?,
                        formatContent: String?
                    ) {
                        CELog.d("onContentUpdate")
                    }

                    override fun copyText(entity: MessageEntity?) {
                        CELog.d("copyText")
                    }

                    override fun replyText(entity: MessageEntity?) {
                        CELog.d("replyText")
                    }

                    override fun tranSend(entity: MessageEntity?) {
                        CELog.d("tranSend")
                    }

                    override fun retry(entity: MessageEntity?) {
                        CELog.d("retry")
                    }

                    override fun cellect(entity: MessageEntity?) {
                        CELog.d("cellect")
                    }

                    override fun shares(
                        entity: MessageEntity?,
                        image: View?
                    ) {
                        CELog.d("shares")
                    }

                    override fun choice() {
                        CELog.d("choice")
                    }

                    override fun delete(entity: MessageEntity?) {
                        CELog.d("delete")
                    }

                    override fun enLarge(entity: MessageEntity?) {
                        CELog.d("enLarge")
                    }

                    override fun onPlayComplete(entity: MessageEntity?) {
                        CELog.d("onPlayComplete")
                    }

                    override fun onStopOtherVideoPlayback(entity: MessageEntity?) {
                        CELog.d("onStopOtherVideoPlayback")
                    }

                    override fun retractMsg(entity: MessageEntity?) {
                        CELog.d("retractMsg")
                    }

                    override fun showRePlyPanel(entity: MessageEntity?) {
                        CELog.d("showRePlyPanel")
                    }

                    override fun locationMsg(entity: MessageEntity?) {
                        CELog.d("locationMsg")
                    }

                    override fun findReplyMessage(messageId: String?) {
                        CELog.d("findReplyMessage")
                    }

                    override fun onVideoClick(entity: MessageEntity?) {
                        if (!checkForSelfPermissions()) {
                            requestPermissionAndDoAction { sendVideoMessage(entity) }
                        } else {
                            sendVideoMessage(entity)
                        }
                    }

                    override fun updateReplyMessageWhenVideoDownload(messageId: String?) {
                    }
                }
            )
        }

    private fun requestPermissionAndDoAction(action: () -> Unit) { // 請求權限並執行
        onPermissionGranted = action
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            requestMultiplePermissionLauncher.launch(permissionsTIRAMISU)
        }
    }

    private fun sendVideoMessage(entity: MessageEntity?) {
        entity?.let { e ->
            val mediaData =
                mainMessageData.filter {
                    it.type == MessageType.IMAGE || it.type == MessageType.VIDEO
                }
            val bundle =
                bundleOf(
                    BundleKey.PHOTO_GALLERY_MESSAGE.key() to e,
                    BundleKey.ROOM_TYPE.key() to ChatRoomEnum.AI_CONSULTATION_ROOM.name,
                    BundleKey.EXTRA_SESSION_LIST.key() to JsonHelper.getInstance().toJson(mediaData),
                    BundleKey.ROOM_ID.key() to serviceNumberRoomId
                )
            startIntent(this@ConsultAIActivity, MediaGalleryActivity::class.java, bundle)
        }
    }

    private fun checkForSelfPermissions(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            for (permission in permissionsTIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }

    // 初始化訊息列表
    private fun initMessageList() =
        CoroutineScope(Dispatchers.Main).async {
            serviceNumberRoomId?.let {
                val chatRoomEntity = ChatRoomReference.getInstance().findById(it)
                chatRoomEntity.roomType = ChatRoomType.consultAi
                val layoutManager = (binding.messageRV.layoutManager) as LinearLayoutManager
                layoutManager.stackFromEnd = false
                binding.messageRV.setContainer(mainMessageData, chatRoomEntity, selfProfile)
                binding.messageRV.setOnTemplateClickListener(
                    object : OnTemplateClickListener {
                        override fun onTemplateClick(content: String) {
                            consultAIViewModel.sendAIQuickReply(
                                serviceNumberRoomId!!,
                                serviceNumberId!!,
                                content,
                                "Action"
                            )
                        }
                    }
                )
            }
        }

    private fun observerData() =
        CoroutineScope(Dispatchers.Main).launch {
            homeViewModel.serviceRoomUnreadNumber.observe(this@ConsultAIActivity) { count: Int ->
                setUnreadCount(count)
            }

            consultAIViewModel.consultAiSendMessage.observe(this@ConsultAIActivity) {
                val targetMessageIndex = mainMessageData.indexOf(it.second)
                mainMessageData[targetMessageIndex].status = if (it.first) MessageStatus.SUCCESS else MessageStatus.FAILED
                binding.messageRV.adapter?.notifyItemChanged(targetMessageIndex)
            }

            consultAIViewModel.quoteMessageResult.observe(this@ConsultAIActivity) {
                setResult(RESULT_OK, it)
                finish()
            }

            consultAIViewModel.messageList.observe(this@ConsultAIActivity) {
                displayMessage(it)
            }

//        consultAIViewModel.quoteMessageError.observe(this@ConsultAIActivity) {
//            Toast.makeText(this@ConsultAIActivity, it, Toast.LENGTH_SHORT).show()
//        }

            consultAIViewModel.todoMessageResult.observe(this@ConsultAIActivity) {
                val todoSettingDialog = TodoSettingDialog(this@ConsultAIActivity, it.roomId, it.id, Lists.newArrayList(it))
                todoSettingDialog.setRemindListener(setRemindListener)
                todoSettingDialog.show()
            }

            consultAIViewModel.quickReplyList.observe(this@ConsultAIActivity) {
                setQuickReply(it)
            }

            consultAIViewModel.bottomRichMenuList.observe(this@ConsultAIActivity) {
                binding.chatKeyboardLayout
                    .setRichMenuGridCount(5)
                    .setOnItemClickListener(
                        it.first,
                        it.second,
                        mutableListOf(),
                        object :
                            BottomRichMeunAdapter.OnItemClickListener {
                            override fun onClick(
                                msg: MessageEntity,
                                menu: RichMenuBottom?,
                                position: Int
                            ) {
                                when (menu) {
                                    RichMenuBottom.QUOTE -> {
                                        when (msg.type) {
                                            MessageType.TEXT -> {
                                                consultAIViewModel.quoteMessage(msg)
                                            }

                                            MessageType.IMAGE, MessageType.VIDEO -> {
                                                val mediaData =
                                                    mainMessageData.filter {
                                                        it.type == MessageType.IMAGE || it.type == MessageType.VIDEO
                                                    }
                                                val bundle =
                                                    bundleOf(
                                                        BundleKey.PHOTO_GALLERY_MESSAGE.key() to msg,
                                                        BundleKey.ROOM_TYPE.key() to ChatRoomEnum.AI_CONSULTATION_ROOM.name,
                                                        BundleKey.EXTRA_SESSION_LIST.key() to JsonHelper.getInstance().toJson(mediaData),
                                                        BundleKey.ROOM_ID.key() to serviceNumberRoomId
                                                    )
                                                startIntent(this@ConsultAIActivity, MediaGalleryActivity::class.java, bundle)
                                            }

                                            else -> {
                                                // nothing
                                            }
                                        }
                                    }

                                    RichMenuBottom.COPY -> {
                                        consultAIViewModel.copyMessage(msg)
                                    }

                                    else -> {
                                        // nothing
                                    }
                                }
                                binding.chatKeyboardLayout.showKeyboard()
                            }

                            override fun onCancle() {
                            }
                        },
                        null
                    )
            }
        }

    private fun getUnreadData() =
        CoroutineScope(Dispatchers.Main).launch {
            homeViewModel.getServiceRoomUnReadSum()
        }

    /**
     * 設置 未讀數量
     * @param count 未讀數量
     * */
    private fun setUnreadCount(count: Int) =
        CoroutineScope(Dispatchers.Main).launch {
            if (count > 0) {
                binding.unreadNum.text = UnreadUtil.getUnreadText(count)
                binding.unreadNum.visibility = View.VISIBLE
            } else {
                binding.unreadNum.visibility = View.GONE
            }
        }

    /**
     * 設置 QuickReply List
     * @param quickReplyItemList QuickReply List
     * */
    private fun setQuickReply(quickReplyItemList: List<QuickReplyItem>) =
        CoroutineScope(Dispatchers.IO).launch {
            if (quickReplyItemList.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    binding.rvQuickReplyList.visibility = View.VISIBLE
                    binding.messageRV.scrollToPosition(binding.messageRV.adapter!!.itemCount - 1)
                }
                val quickReplyAdapter = QuickReplyAdapter(quickReplyItemList)
                quickReplyAdapter.setOnQuickReplyClickListener { type: String, data: String ->
                    consultAIViewModel.sendAIQuickReply(
                        serviceNumberRoomId!!,
                        serviceNumberId!!,
                        data,
                        "Action"
                    )
                    binding.rvQuickReplyList.visibility = View.GONE
                }
                withContext(Dispatchers.Main) {
                    binding.rvQuickReplyList.apply {
                        layoutManager =
                            LinearLayoutManager(
                                this@ConsultAIActivity,
                                LinearLayoutManager.HORIZONTAL,
                                false
                            )
                        adapter = quickReplyAdapter
                    }
                }
            }
        }

    /**
     * 顯示訊息
     * @param messageEntityList 需要顯示的訊息
     * */
    private fun displayMessage(messageEntityList: MutableList<MessageEntity>) =
        CoroutineScope(Dispatchers.IO).launch {
            val originSize = mainMessageData.size
            mainMessageData.addAll(messageEntityList)
            val newSize = mainMessageData.size
            withContext(Dispatchers.Main) {
                binding.messageRV.adapter?.notifyItemRangeInserted(originSize, newSize)
                binding.messageRV.scrollToPosition(mainMessageData.size - 1)
            }
        }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(eventMessage: EventMsg<Any>) {
        when (eventMessage.code) {
            MsgConstant.NOTICE_INTELLIGENT_ASSISTANCE -> {
                val aiConsultMessageSocket = AiConsultMessageSocket().parse(eventMessage.data)
                aiConsultMessageSocket.quickReplyItem?.let {
                    if (it.isNotEmpty()) {
                        setQuickReply(it)
                    }
                }
                consultAIViewModel.buildConsultAiReply(
                    serviceNumberRoomId!!,
                    aiConsultMessageSocket.messagelist
                )
            }

            MESSAGE_AI_CONSULTATION_QUOTED_IMAGE,
            MESSAGE_AI_CONSULTATION_QUOTED_VIDEO -> { // 引用圖片/影片後關閉聊天室
                val quotedMedia = eventMessage.data as String
                if (quotedMedia.isNotEmpty()) {
                    finish()
                }
            }

            MsgConstant.UPDATE_MESSAGE_STATUS -> {
                if (eventMessage.data is MessageEntity) {
                    val msg = eventMessage.data as MessageEntity
                    val position = mainMessageData.indexOf(msg)
                    mainMessageData[position].content = msg.content().toStringContent()
                    if (position >= 0) {
                        binding.messageRV.refreshData(position, msg)
                    }
                }
            }
        }
    }
}
