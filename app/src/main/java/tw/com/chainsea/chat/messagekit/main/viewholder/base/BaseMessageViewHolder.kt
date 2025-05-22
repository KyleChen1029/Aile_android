package tw.com.chainsea.chat.messagekit.main.viewholder.base

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.common.base.Strings
import com.google.common.collect.Maps
import com.google.common.collect.Sets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.android.common.animator.AnimatorHelper
import tw.com.chainsea.android.common.random.RandomHelper
import tw.com.chainsea.android.common.ui.UiHelper
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity
import tw.com.chainsea.ce.sdk.bean.msg.MessageStatus
import tw.com.chainsea.ce.sdk.bean.msg.MessageType
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType
import tw.com.chainsea.ce.sdk.customview.AvatarIcon
import tw.com.chainsea.ce.sdk.database.DBManager
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.reference.MessageReference
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.ItemBaseMessageBinding
import tw.com.chainsea.chat.databinding.ItemLeftMessageBinding
import tw.com.chainsea.chat.databinding.ItemRightMessageBinding
import tw.com.chainsea.chat.messagekit.lib.MessageDomino
import tw.com.chainsea.chat.messagekit.main.adapter.MessageAdapterMode
import tw.com.chainsea.chat.messagekit.main.adapter.OnMessageClickListener
import tw.com.chainsea.chat.messagekit.main.viewholder.ReplyMessageType
import tw.com.chainsea.chat.messagekit.main.viewholder.listener.OnMessageItemEvanListener
import tw.com.chainsea.chat.util.DownloadUtil
import tw.com.chainsea.chat.util.ThemeHelper
import tw.com.chainsea.chat.util.TimeUtil.getHHmm
import tw.com.chainsea.custom.view.image.CircleImageView
import java.lang.ref.WeakReference

abstract class BaseMessageViewHolder(
    protected val binding: ItemBaseMessageBinding,
    private val chatRoomEntity: ChatRoomEntity,
    private val chatMembers: MutableList<UserProfileEntity> = mutableListOf(),
    private val onMessageSlideReply: OnMessageSlideReply? = null
) : RecyclerView.ViewHolder(binding.root) {
    private val selfUserId by lazy { TokenPref.getInstance(binding.root.context).userId }
    protected var isAnonymousMode = false
    val downloadDir by lazy { DownloadUtil.downloadFileDir }
    var message: MessageEntity? = null
    protected lateinit var onMessageClickListener: WeakReference<OnMessageClickListener?>
    private var mode = MessageAdapterMode.DEFAULT
    private var tmpSenderName = ""
    private var tmpSenderAvatarId = ""
    protected val rightMessageBinding: ItemRightMessageBinding by lazy { ItemRightMessageBinding.inflate(LayoutInflater.from(binding.root.context)) }
    protected val leftMessageBinding: ItemLeftMessageBinding by lazy { ItemLeftMessageBinding.inflate(LayoutInflater.from(binding.root.context)) }

    private val avatarIconClickListener: View.OnClickListener =
        View.OnClickListener {
            if (ChatRoomType.system == chatRoomEntity.type) return@OnClickListener
            message?.let {
                it.senderId?.let { senderId ->
                    onMessageClickListener.get()?.onAvatarIconClick(senderId)
                }
            }
        }

    abstract fun onMessageClick()

    @JvmName("functionOfKotlin")
    fun setOnMessageClickListener(onMessageClickListener: OnMessageClickListener?) {
        this.onMessageClickListener = WeakReference(onMessageClickListener)
    }

    fun setMode(mode: MessageAdapterMode) {
        this.mode = mode
    }

    fun setAsAnonymousMode(isAnonymousMode: Boolean) {
        this.isAnonymousMode = isAnonymousMode
    }

    open fun bind(message: MessageEntity) {
        this@BaseMessageViewHolder.message = message
        binding.cbCheckBox.setButtonDrawable(if (ThemeHelper.isGreenTheme()) R.drawable.green_gray_checkbox_style else R.drawable.default_checkbox_style)
        setSenderName()
        setSenderIcon()
        setSendTime(message)
        initSwipeMenuLayout()
        initOnTouchListener()
        setIsNeedShake(message)
        setModeView()
        setReadNum()
        setMessageStatus()
    }

    fun setIsNeedShake(message: MessageEntity) {
        message.isAnimator?.let {
            if (it) {
                AnimatorHelper.shakeAnimation(
                    binding.root,
                    true,
                    200L
                ) { _, status: AnimatorHelper.Status ->
                    if (status == AnimatorHelper.Status.END) message.isAnimator = false
                }
            }
        }
    }

    private fun initSwipeMenuLayout() =
        CoroutineScope(Dispatchers.Main).launch {
            binding.swipeLayout.setOnExpandListener {
                onMessageSlideReply?.let {
                    it.onMessageSlideReply(message!!)
                    binding.swipeLayout.quickClose()
                }
            }
        }

    private fun initOnTouchListener() {
        val layout =
            if (isRightMessage()) rightMessageBinding.flRightMessageContainer else leftMessageBinding.flLeftMessageBody
        layout.setOnTouchListener(
            object :
                OnMessageItemEvanListener<MessageEntity>(binding.root.context, true, message) {
                override fun onClick(
                    v: View?,
                    t: MessageEntity?
                ) {
                    when (mode) {
                        MessageAdapterMode.SELECTION -> {
                            binding.cbCheckBox.isChecked = !binding.cbCheckBox.isChecked
                            message?.isDelete = message?.isDelete?.not()
                        }

                        MessageAdapterMode.RANGE_SELECTION -> {
                        }

                        else -> {
                            onMessageClick()
                        }
                    }
                }

                override fun onDoubleClick(
                    v: View?,
                    t: MessageEntity?
                ) {
                }

                override fun onLongClick(
                    v: View?,
                    t: MessageEntity?
                ) {
                    message?.let {
                        onMessageClickListener.get()?.onMessageLongClick(it)
                    }
                }
            }
        )
    }

    private val onGlobalLayoutListener by lazy {
        ViewTreeObserver.OnGlobalLayoutListener {
            if (binding.root.height != 0) {
                binding.maskLayer.layoutParams.height = binding.root.height
                binding.maskLayer.invalidate()
                binding.maskLayer.requestLayout()
            }
        }
    }

    private fun setModeView() {
        setSelectionView()
        when (mode) {
            MessageAdapterMode.RANGE_SELECTION -> {
                binding.maskLayer.visibility = View.VISIBLE
                binding.root.viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener)
                message?.let {
                    binding.maskLayer.alpha = if (it.isShowSelection == true) 0.0f else 0.68f
                }
                binding.maskLayer.setOnClickListener {
                    message?.let {
                        onMessageClickListener.get()?.buildScreenShot(it)
                    }
                }
            }

            MessageAdapterMode.SELECTION -> {
            }

            else -> {
                binding.root.viewTreeObserver.removeOnGlobalLayoutListener(onGlobalLayoutListener)
                binding.maskLayer.visibility = View.GONE
                binding.cbCheckBox.visibility = View.GONE
                binding.swipeLayout.isSwipeEnable = true
                binding.root.setOnClickListener(null)
                val layout =
                    if (isRightMessage()) rightMessageBinding.clRightLayout else leftMessageBinding.flLeftMessageContainer
                layout.setOnClickListener(null)
            }
        }
    }

    private fun setSelection() {
        message?.let {
            it.isShowSelection = it.isShowSelection?.not()
        }
    }

    private fun setSelectionView() {
        message?.let { message ->
            binding.cbCheckBox.visibility =
                if (message.isShowChecked == true) View.VISIBLE else View.GONE
            binding.swipeLayout.isSwipeEnable = false
            binding.cbCheckBox.isChecked = message.isDelete == true
            val layout =
                if (isRightMessage()) rightMessageBinding.clRightLayout else leftMessageBinding.flLeftMessageContainer
            layout.setOnClickListener {
                binding.cbCheckBox.isChecked = !binding.cbCheckBox.isChecked
                message.isDelete = message.isDelete?.not()
                setSelection()
            }
            binding.cbCheckBox.setOnClickListener {
                binding.cbCheckBox.isChecked = !binding.cbCheckBox.isChecked
                message.isDelete = message.isDelete?.not()
                setSelectionView()
            }
        }
    }

    private fun setSenderIcon() =
        CoroutineScope(Dispatchers.IO).launch {
            message?.let {
                withContext(Dispatchers.Main) {
                    leftMessageBinding.ivLeftAvatar.visibility =
                        if (isRightMessage()) View.GONE else View.VISIBLE
                    rightMessageBinding.ivRightAvatar.visibility =
                        if (isRightMessage()) View.GONE else View.GONE
                }

                when {
                    // 個人聊天室
                    ChatRoomType.person == chatRoomEntity.type -> {
                        withContext(Dispatchers.Main) {
                            leftMessageBinding.ivLeftAvatar.setImageResource(R.drawable.res_self_message_icon_l)
                            rightMessageBinding.ivRightAvatar.setImageResource(R.drawable.res_self_message_icon_r)
                            leftMessageBinding.ivLeftAvatar.visibility = View.VISIBLE
                            rightMessageBinding.ivRightAvatar.visibility = View.VISIBLE
                        }
                    }

                    // 系統聊天室
                    ChatRoomType.system == chatRoomEntity.type -> {
                        getCurrentSenderImageView().loadAvatarIcon(
                            it.avatarId,
                            it.senderName ?: "Unknown",
                            it.senderId
                        )
                    }

                    // 諮詢ai聊天室
                    ChatRoomType.consultAi == chatRoomEntity.roomType -> {
                        getCurrentSenderImageView().setImageResource(R.drawable.ic_ai_consultation)
                    }

                    // 匿名模式
                    isAnonymousMode && !isRightMessage() -> {
                        val domino = getDomino(it.senderId)
                        withContext(Dispatchers.Main) {
                            domino?.let { messageDomino ->
                                getCurrentSenderImageView().setImageResource(messageDomino.resId)
                            }
                        }
                    }

                    // 左邊的訊息
                    !isRightMessage() -> {
                        val avatarId =
                            if (it.avatarId.isNullOrEmpty()) {
                                if (tmpSenderName == it.senderName) tmpSenderAvatarId else it.avatarId
                            } else {
                                it.avatarId
                            }
                        getCurrentSenderImageView().loadAvatarIcon(
                            avatarId,
                            it.senderName ?: "",
                            it.id
                        )
                        tmpSenderName = it.senderName ?: ""
                        tmpSenderAvatarId = it.avatarId ?: ""
                    }

                    else -> {
                        // nothing
                    }
                }
            }
            withContext(Dispatchers.Main) {
                getCurrentSenderImageView().setOnClickListener(avatarIconClickListener)
            }
        }

    private fun setSenderName() =
        CoroutineScope(Dispatchers.IO).launch {
            if (isSelfSend()) {
                withContext(Dispatchers.Main) {
                    getCurrentSenderTextView().visibility = View.GONE
                }
                return@launch
            }
            message?.let {
                withContext(Dispatchers.Main) {
                    if (isAnonymousMode) {
                        withContext(Dispatchers.Main) {
                            val domino =
                                getDomino(it.senderId)?.let {
                                    MessageDomino.DominoName.valueOf(it.name).getName()
                                } ?: ""
                            getCurrentSenderTextView().text = domino
                        }
                    } else {
                        getCurrentSenderTextView().text = it.senderName
                    }
                }
            }
        }

    private fun setSendTime(message: MessageEntity) =
        CoroutineScope(Dispatchers.Main).launch {
            if (isRightMessage()) {
                rightMessageBinding.tvRightSendTime.text = getHHmm(message.sendTime)
            } else {
                leftMessageBinding.tvLeftSendTime.text = getHHmm(message.sendTime)
            }
        }

    fun setBubbleView() =
        CoroutineScope(Dispatchers.Main).launch {
            message?.let {
                if (it.type == MessageType.TEMPLATE) {
                    binding.clMessageContainer.removeAllViews()
                    if (isRightMessage()) {
                        binding.clMessageContainer.addView(rightMessageBinding.root)
                    } else {
                        binding.clMessageContainer.addView(leftMessageBinding.root)
                    }
                }
            }
        }

    fun setBubbleView(view: View) =
        CoroutineScope(Dispatchers.Main).launch {
            message?.let {
                view.parent?.let {
                    (it as ViewGroup).removeAllViews()
                }

                binding.clMessageContainer.removeAllViews()
                if (isRightMessage()) {
                    binding.clMessageContainer.addView(rightMessageBinding.root)
                    rightMessageBinding.flRightMessageBody.apply {
                        addView(view)
                    }
                    if (isSystemMessage(it)) {
                        setMessageBackground(view, R.drawable.bubble_send_f)
                    } else {
                        setMessageBackground(view, R.drawable.bubble_send)
                    }
                } else {
                    binding.clMessageContainer.addView(leftMessageBinding.root)
                    leftMessageBinding.flLeftMessageBody.apply {
                        addView(view)
                    }
                    setMessageBackground(view, R.drawable.bubble_receive)
                }
            }
        }

    private fun setMessageBackground(
        view: View,
        res: Int
    ) {
        message?.let {
            if (it.type == MessageType.TEMPLATE) return
//            if (it.type == MessageType.IMAGE) return
//            if (it.type == MessageType.VIDEO) return
            view.setBackgroundResource(res)
        }
    }

    private fun setReadNum() =
        CoroutineScope(Dispatchers.IO).launch {
            if (!isRightMessage()) return@launch
            message?.let {
                if (MessageType.CALL != it.type) {
                    var totalCount = it.sendNum ?: (chatMembers.size - 1)
                    val readCount = it.readedNum ?: 0
                    val receivedCount = it.receivedNum ?: 0
                    withContext(Dispatchers.Main) {
                        rightMessageBinding.llReadNum.removeAllViews()
                        // 多人
                        if (ChatRoomType.GROUP_or_DISCUSS.contains(chatRoomEntity.type)) {
                            if (readCount > 5) {
                                rightMessageBinding.llReadNum.addView(getReadNumView(R.drawable.read_mark, true))
                                rightMessageBinding.llReadNum.addView(getReadTextView(readCount))
                                totalCount -= readCount
                            } else if (readCount > 0) {
                                repeat(readCount) {
                                    rightMessageBinding.llReadNum.addView(getReadNumView(R.drawable.read_mark, false))
                                    totalCount--
                                }
                            }

                            if (totalCount > 5) {
                                rightMessageBinding.llReadNum.addView(getReadNumView(R.drawable.unread_mark, true))
                                rightMessageBinding.llReadNum.addView(getReadTextView(totalCount))
                            } else if (totalCount > 0) {
                                repeat(totalCount) {
                                    rightMessageBinding.llReadNum.addView(getReadNumView(R.drawable.unread_mark, false))
                                }
                            }
                        } else {
                            // 單人
                            if (readCount > 0) {
                                // 大空心圆 readCount
                                rightMessageBinding.llReadNum.addView(getReadNumView(R.drawable.read_mark, true))
                            } else if (receivedCount > 0) {
                                // 大空心圆 receivedCount
                                rightMessageBinding.llReadNum.addView(getReadNumView(R.drawable.unread_mark, true))
                            }
                        }
                    }
                }
            }
        }

    private fun setMessageStatus() =
        CoroutineScope(Dispatchers.Main).launch {
            if (!isRightMessage()) return@launch
            message?.let { message ->
                when (message.status) {
                    MessageStatus.ERROR, MessageStatus.UPDATE_ERROR, MessageStatus.FAILED -> {
                        rightMessageBinding.ivRightMessageStatus.visibility = View.VISIBLE
                        rightMessageBinding.tvRightSendTime.visibility = View.GONE
                        rightMessageBinding.llReadNum.visibility = View.GONE
                        rightMessageBinding.ivRightMessageSendingStatus.visibility = View.GONE
                        rightMessageBinding.ivRightMessageStatus.setOnClickListener {
                            onMessageClickListener.get()?.onMessageStatusClick(message)
                        }
                    }

                    MessageStatus.SENDING -> {
                        rightMessageBinding.ivRightMessageSendingStatus.setImageResource(if (ThemeHelper.isGreenTheme()) R.drawable.ic_sending_green else R.drawable.ic_sending)
                        rightMessageBinding.ivRightMessageSendingStatus.visibility = View.VISIBLE
                        rightMessageBinding.ivRightMessageStatus.visibility = View.GONE
                    }

                    else -> {
                        rightMessageBinding.ivRightMessageStatus.visibility = View.GONE
                        rightMessageBinding.llReadNum.visibility = View.VISIBLE
                        rightMessageBinding.ivRightMessageSendingStatus.visibility = View.GONE
                        rightMessageBinding.tvRightSendTime.visibility = View.VISIBLE
                    }
                }
            }
        }

    private suspend fun getReadNumView(
        resourceDrawable: Int,
        isBigCircle: Boolean
    ): CircleImageView =
        withContext(Dispatchers.Main) {
            val circleImageView = CircleImageView(binding.root.context)
            val params =
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            params.gravity = Gravity.CENTER_VERTICAL
            if (isBigCircle) {
                params.weight = UiHelper.dip2px(binding.root.context, 6f).toFloat()
                params.height = UiHelper.dip2px(binding.root.context, 6f)
            } else {
                params.weight = UiHelper.dip2px(binding.root.context, 5f).toFloat()
                params.height = UiHelper.dip2px(binding.root.context, 5f)
            }
            circleImageView.layoutParams = params
            circleImageView.setImageResource(resourceDrawable)
            return@withContext circleImageView
        }

    @SuppressLint("SetTextI18n")
    private suspend fun getReadTextView(readCount: Int): TextView =
        withContext(Dispatchers.Main) {
            val textView = TextView(binding.root.context)
            val params =
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            params.rightMargin = 6
            textView.apply {
                layoutParams = params
                textSize = 10f
                setTextColor(ContextCompat.getColor(binding.root.context, R.color.black))
                text = readCount.toString()
            }
            return@withContext textView
        }

    private fun isSystemMessage(message: MessageEntity): Boolean =
        message.type == MessageType.SYSTEM &&
            message.senderId != selfUserId

    fun isRightMessage(): Boolean =
        if (ChatRoomType.person == chatRoomEntity.roomType) {
            Sets.newHashSet("android", "ios").contains(message?.osType)
        } else if (ChatRoomType.consultAi == chatRoomEntity.roomType) {
            selfUserId == message?.senderId
        } else if (ChatRoomType.SERVICES_or_SUBSCRIBE.contains(chatRoomEntity.roomType) && selfUserId != chatRoomEntity.ownerId) {
            chatRoomEntity.ownerId != message?.senderId
        } else {
            !TextUtils.isEmpty(selfUserId) && selfUserId == message?.senderId
        }

    fun onContentUpdate(
        formatName: String,
        formatContent: String
    ) = CoroutineScope(Dispatchers.IO).launch {
        message?.let {
            MessageReference.updateMessageFormat(it.id, formatName, formatContent)
        }
    }

    fun onContentUpdate(
        replyMessageType: ReplyMessageType,
        formatName: String,
        formatContent: String
    ) = CoroutineScope(Dispatchers.IO).launch {
        message?.let {
            val messageId =
                if (replyMessageType == ReplyMessageType.NearMessage) {
                    it.nearMessageId
                } else {
                    it.id
                }
            MessageReference.updateMessageFormat(messageId, formatName, formatContent)
        }
    }

    fun clearCallback() {
        onMessageClickListener.get()?.let {
            onMessageClickListener.clear()
        }
    }

    suspend fun getSenderUserProfile(senderId: String?): UserProfileEntity? =
        withContext(Dispatchers.IO) {
            senderId?.let {
                val userProfile = DBManager.getInstance().queryUser(senderId)
                return@withContext userProfile
            }
        }

    suspend fun getMemberName(): Map<String, String> =
        withContext(Dispatchers.IO) {
            if (chatMembers.isEmpty()) {
                val members = DBManager.getInstance().queryEmployeeList()
                if (members.isNotEmpty()) {
                    return@withContext handleMemberName(members)
                } else {
                    return@withContext Maps.newHashMap<String, String>()
                }
            } else {
                return@withContext handleMemberName(chatMembers)
            }
        }

    private suspend fun handleMemberName(members: MutableList<UserProfileEntity>) =
        withContext(Dispatchers.IO) {
            val result = Maps.newHashMap<String, String>()
            members.forEach {
                result[it.id] =
                    if (isAnonymousMode) {
                        getDomino(it.id)?.name
                    } else {
                        if (!Strings.isNullOrEmpty(it.alias)) it.alias else it.nickName
                    }
            }
            return@withContext result
        }

    private suspend fun getCurrentSenderTextView(): TextView =
        withContext(Dispatchers.IO) {
            if (isRightMessage()) {
                rightMessageBinding.tvRightAccountName
            } else {
                leftMessageBinding.tvLeftAccountName
            }
        }

    private suspend fun getCurrentSenderImageView(): AvatarIcon =
        withContext(Dispatchers.IO) {
            if (isRightMessage()) {
                rightMessageBinding.ivRightAvatar
            } else {
                leftMessageBinding.ivLeftAvatar
            }
        }

    private suspend fun isSelfSend(): Boolean =
        withContext(Dispatchers.IO) {
            message?.let {
                return@withContext it.senderId == selfUserId
            }
            return@withContext false
        }

    protected suspend fun getDomino(userId: String?): MessageDomino? =
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
}

interface OnMessageSlideReply {
    fun onMessageSlideReply(messageEntity: MessageEntity)
}
