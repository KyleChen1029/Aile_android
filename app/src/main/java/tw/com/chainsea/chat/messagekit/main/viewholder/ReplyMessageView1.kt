package tw.com.chainsea.chat.messagekit.main.viewholder

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.icu.text.DecimalFormat
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.common.base.Strings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.android.common.file.FileHelper
import tw.com.chainsea.android.common.log.CELog
import tw.com.chainsea.android.common.text.KeyWordHelper
import tw.com.chainsea.android.common.video.VideoHelper
import tw.com.chainsea.android.common.voice.VoiceHelper
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity
import tw.com.chainsea.ce.sdk.bean.msg.MessageStatus
import tw.com.chainsea.ce.sdk.bean.msg.MessageType
import tw.com.chainsea.ce.sdk.bean.msg.content.AtContent
import tw.com.chainsea.ce.sdk.bean.msg.content.FileContent
import tw.com.chainsea.ce.sdk.bean.msg.content.ImageContent
import tw.com.chainsea.ce.sdk.bean.msg.content.StickerContent
import tw.com.chainsea.ce.sdk.bean.msg.content.TextContent
import tw.com.chainsea.ce.sdk.bean.msg.content.VideoContent
import tw.com.chainsea.ce.sdk.bean.msg.content.VoiceContent
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.event.EventMsg
import tw.com.chainsea.ce.sdk.event.MsgConstant
import tw.com.chainsea.ce.sdk.http.ce.request.StickerDownloadRequest
import tw.com.chainsea.ce.sdk.reference.MessageReference
import tw.com.chainsea.ce.sdk.service.StickerService
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack
import tw.com.chainsea.ce.sdk.service.type.RefreshSource
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.ItemBaseMessageBinding
import tw.com.chainsea.chat.databinding.ItemMessageAtBinding
import tw.com.chainsea.chat.databinding.ItemMessageFileBinding
import tw.com.chainsea.chat.databinding.ItemMessageReplyBinding
import tw.com.chainsea.chat.databinding.ItemMessageStickerBinding
import tw.com.chainsea.chat.databinding.ItemMessageTextBinding
import tw.com.chainsea.chat.databinding.MsgkitImageBinding
import tw.com.chainsea.chat.databinding.MsgkitVideoBinding
import tw.com.chainsea.chat.databinding.MsgkitVoiceBinding
import tw.com.chainsea.chat.lib.AtMatcherHelper
import tw.com.chainsea.chat.messagekit.enums.FileType
import tw.com.chainsea.chat.messagekit.main.FileDownloadSuccessEvent
import tw.com.chainsea.chat.messagekit.main.MessageVideoDownloadProgress
import tw.com.chainsea.chat.messagekit.main.viewholder.base.BaseMessageViewHolder
import tw.com.chainsea.chat.messagekit.main.viewholder.base.OnMessageSlideReply
import tw.com.chainsea.chat.util.DaVinci
import tw.com.chainsea.chat.util.DownloadUtil
import tw.com.chainsea.chat.util.DownloadUtil.getVideoDuration
import tw.com.chainsea.chat.util.DownloadingTask
import tw.com.chainsea.chat.util.UrlTextUtil
import java.io.File

class ReplyMessageView1(
    binding: ItemBaseMessageBinding,
    private val layoutInflater: LayoutInflater,
    chatRoomEntity: ChatRoomEntity,
    chatMembers: MutableList<UserProfileEntity>,
    onMessageSlideReply: OnMessageSlideReply
) : BaseMessageViewHolder(binding, chatRoomEntity, chatMembers, onMessageSlideReply = onMessageSlideReply) {
    companion object {
        private const val SMALL_PIC_SIZE: Int = 360
    }

    private val replyMessageBinding = ItemMessageReplyBinding.inflate(layoutInflater)
    private var downloadJob: Call? = null
    private var keyword = ""

    override fun onMessageClick() {
    }

    override fun bind(message: MessageEntity) {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        message.isAnimator?.let {
            if (it) {
                setIsNeedShake(message)
                return@let
            }
        }
        super.bind(message)
        CoroutineScope(Dispatchers.IO).launch {
            setBubbleView(replyMessageBinding.root)
            setNearMessageAvatar()

            message.nearMessageId?.let {
                val nearMessage = MessageReference.findById(it)
                nearMessage?.let {
                    message.nearMessageType = it.type
                    message.nearMessageContent = it.content
                    message.nearMessageSenderId = it.senderId
                    it.type?.let { setMessage(it, ReplyMessageType.NearMessage) }
                }
            }

            message.type?.let {
                setMessage(it, ReplyMessageType.ReplyMessage)
            }
            withContext(Dispatchers.Main) {
                replyMessageBinding.llNearMessage.setOnClickListener {
                    val nearMessage = MessageReference.findById(message.nearMessageId)
                    nearMessage?.let {
                        onMessageClickListener.get()?.onThemeNearMessageClick(it.sequence!!)
                    }
                }

                replyMessageBinding.llNearMessage.setOnLongClickListener {
                    message.let {
                        onMessageClickListener?.get()?.onMessageLongClick(it)
                    }
                    true
                }

                replyMessageBinding.underFrameLayout.setOnLongClickListener {
                    message.let {
                        onMessageClickListener.get()?.onMessageLongClick(it)
                    }
                    true
                }

                replyMessageBinding.ivReply.setOnClickListener {
                    onMessageClickListener.get()?.onThemeUnderMessageClick(message.themeId)
                }
            }
        }
    }

    private fun setMessage(
        type: MessageType,
        replyMessageType: ReplyMessageType
    ) = CoroutineScope(Dispatchers.Main).launch {
        val layout =
            if (replyMessageType == ReplyMessageType.ReplyMessage) replyMessageBinding.underFrameLayout else replyMessageBinding.nearFrameLayout
        var messageView: View? = null
        when (type) {
            MessageType.TEXT -> {
                messageView = assemblyTextView(replyMessageType)
            }

            MessageType.AT -> {
                messageView = assemblyAtView(replyMessageType)
            }

            MessageType.IMAGE -> {
                messageView = assemblyImageView(replyMessageType)
            }

            MessageType.STICKER -> {
                messageView = assemblyStickerView(replyMessageType)
            }

            MessageType.FILE -> {
                messageView = assemblyFileView(replyMessageType)
            }

            MessageType.VOICE -> {
                messageView = assemblyVoiceView(replyMessageType)
            }

            MessageType.VIDEO -> {
                messageView = assemblyVideoView(replyMessageType)
            }

            else -> {}
        }
        messageView?.let {
            layout.visibility = View.VISIBLE
            layout.removeAllViews()
            layout.addView(it)
        }
    }

    private fun setNearMessageAvatar() =
        CoroutineScope(Dispatchers.IO).launch {
            message?.let {
                if (isAnonymousMode) {
                    val anonymousAvatar = getDomino(it.nearMessageSenderId)?.resId
                    anonymousAvatar?.let {
                        replyMessageBinding.ivNearAvatar.setImageResource(it)
                    }
                } else {
                    val senderUserProfile = getSenderUserProfile(it.nearMessageSenderId)
                    senderUserProfile?.let {
                        replyMessageBinding.ivNearAvatar.loadAvatarIcon(it.avatarId, it.nickName, it.id)
                    }
                }
            }
        }

    private suspend fun assemblyTextView(replyMessageType: ReplyMessageType): View? =
        withContext(Dispatchers.Main) {
            message?.let { message ->
                val textContent =
                    if (replyMessageType == ReplyMessageType.ReplyMessage) message.content() as TextContent else message.nearMessageContent() as TextContent
                val textBinding =
                    ItemMessageTextBinding.inflate(
                        layoutInflater,
                        getReplyMessageParent(replyMessageType),
                        false
                    )
                val text =
                    UrlTextUtil().getUrlSpannableString(
                        textBinding.contentCTV,
                        KeyWordHelper.matcherSearchBackground(-0xfc7, textContent.text, keyword)
                    )
                withContext(Dispatchers.Main) {
                    textBinding.contentCTV.text = text
                    textBinding.contentCTV.movementMethod =
                        TextClickMovementMethod(
                            { onMessageClick() },
                            { onMessageClickListener.get()?.onMessageLongClick(message) }
                        )
                }
                return@withContext textBinding.root
            }
        }

    private suspend fun assemblyAtView(replyMessageType: ReplyMessageType): View? =
        withContext(Dispatchers.Main) {
            message?.let { message ->
                val atContent =
                    if (replyMessageType == ReplyMessageType.ReplyMessage) message.content() as AtContent else message.nearMessageContent() as AtContent
                val mentionContent = atContent.mentionContents
                val builder =
                    AtMatcherHelper.matcherAtUsersWithKeyword(mentionContent, getMemberName(), keyword) {
                        onMessageClickListener.get()?.onAtMessageClick(it)
                    }
                val atTextMessageBinding =
                    ItemMessageAtBinding.inflate(
                        layoutInflater,
                        getReplyMessageParent(replyMessageType),
                        false
                    )
                withContext(Dispatchers.Main) {
                    atTextMessageBinding.tvAtMessage.text = builder
                    atTextMessageBinding.tvAtMessage.movementMethod =
                        TextClickMovementMethod(
                            { onMessageClick() },
                            { onMessageClickListener.get()?.onMessageLongClick(message) }
                        )
                }
                return@withContext atTextMessageBinding.root
            }
        }

    private suspend fun assemblyImageView(replyMessageType: ReplyMessageType): View? =
        withContext(Dispatchers.Main) {
            val currentMessage =
                if (replyMessageType == ReplyMessageType.NearMessage) {
                    MessageReference.findById(message?.nearMessageId)
                } else {
                    message
                }
            currentMessage?.let { message ->
                val imageMessageBinding =
                    MsgkitImageBinding.inflate(
                        layoutInflater,
                        getReplyMessageParent(replyMessageType),
                        true
                    )
                setImage(imageMessageBinding, replyMessageType)
                imageMessageBinding.occupationCL.setOnClickListener {
                    onMessageClickListener.get()?.onImageMessageClick(message)
                }
                return@withContext imageMessageBinding.root
            }
        }

    private fun setImage(
        imageMessageBinding: MsgkitImageBinding,
        replyMessageType: ReplyMessageType
    ) = CoroutineScope(Dispatchers.IO).launch {
        try {
            message?.let { message ->
                val imageContent =
                    if (replyMessageType == ReplyMessageType.ReplyMessage) message.content() as ImageContent else message.nearMessageContent() as ImageContent
                val thumbnailUrl =
                    if (imageContent.url.endsWith(".gif")) imageContent.url else imageContent.thumbnailUrl
                var request: RequestBuilder<*>? = null
                if (thumbnailUrl.isNullOrEmpty()) {
                    imageContent.isFailedToLoad = true
//                message.content = imageContent.toStringContent()
                } else if (thumbnailUrl.startsWith("smallandroid")) {
                    val image = DaVinci.with().imageLoader.getImage(thumbnailUrl)
                    image?.bitmap?.let {
                        request =
                            Glide
                                .with(imageMessageBinding.photoRIV)
                                .load(it)
                                .apply(
                                    RequestOptions()
                                        .override(SMALL_PIC_SIZE)
                                        .placeholder(R.drawable.file_msg_down_bg)
                                        .error(R.drawable.image_load_error)
                                        .fitCenter()
                                ).listener(
                                    ImageMessageView1.OnRequestListener(
                                        imageContent,
                                        message
//                                imageMessageBinding.occupationCL,
//                                imageMessageBinding.progressBar
                                    )
                                )
                    }
                } else if (thumbnailUrl.endsWith(".gif") && !thumbnailUrl.startsWith("http")) {
                    val file = File(thumbnailUrl)
                    request =
                        Glide
                            .with(imageMessageBinding.photoRIV)
                            .asGif()
                            .load(file)
                            .apply(
                                RequestOptions()
                                    .override(SMALL_PIC_SIZE)
                                    .placeholder(R.drawable.file_msg_down_bg)
                                    .error(R.drawable.image_load_error)
                            ).fitCenter()
                            .listener(
                                ImageMessageView1.OnRequestListener(
                                    imageContent,
                                    message
//                            imageMessageBinding.occupationCL,
//                            imageMessageBinding.progressBar
                                )
                            )
                } else {
                    request =
                        Glide
                            .with(imageMessageBinding.photoRIV)
                            .load(thumbnailUrl)
                            .apply(
                                RequestOptions()
                                    .override(SMALL_PIC_SIZE)
                                    .placeholder(R.drawable.file_msg_down_bg)
                                    .error(R.drawable.image_load_error)
                                    .fitCenter()
                            ).listener(
                                ImageMessageView1.OnRequestListener(
                                    imageContent,
                                    message
//                            imageMessageBinding.occupationCL,
//                            imageMessageBinding.progressBar
                                )
                            )
                }
                if (imageContent.isFailedToLoad) {
                    withContext(Dispatchers.Main) {
                        imageMessageBinding.occupationCL.background = null
                    }
                    request =
                        Glide
                            .with(imageMessageBinding.photoRIV)
                            .load(R.drawable.image_load_error)
                            .apply(
                                RequestOptions()
                                    .override(100)
                                    .fitCenter()
                            )
                }

                withContext(Dispatchers.Main) {
                    request?.into(imageMessageBinding.photoRIV)
                }
            }
        } catch (ignored: Exception) {
        }
    }

    private suspend fun assemblyStickerView(replyMessageType: ReplyMessageType): View? =
        withContext(Dispatchers.Main) {
            message?.let { message ->
                val stickerMessageView =
                    ItemMessageStickerBinding.inflate(
                        layoutInflater,
                        getReplyMessageParent(replyMessageType),
                        true
                    )
                val stickerContent =
                    if (replyMessageType == ReplyMessageType.ReplyMessage) message.content() as StickerContent else message.nearMessageContent() as StickerContent
                StickerService.postSticker(
                    stickerMessageView.root.context,
                    stickerContent.packageId,
                    stickerContent.id,
                    StickerDownloadRequest.Type.PICTURE,
                    object : ServiceCallBack<Drawable, RefreshSource> {
                        override fun complete(
                            drawable: Drawable,
                            source: RefreshSource
                        ) {
                            stickerMessageView.msgEmoticons.setImageDrawable(drawable)
                        }

                        override fun error(message: String) {
                            stickerMessageView.msgEmoticons.setImageResource(R.drawable.image_load_error)
                        }
                    }
                )
                return@withContext stickerMessageView.root
            }
        }

    private suspend fun assemblyFileView(replyMessageType: ReplyMessageType): View? =
        withContext(Dispatchers.Main) {
            message?.let { message ->
                val fileMessageView =
                    ItemMessageFileBinding.inflate(
                        layoutInflater,
                        getReplyMessageParent(replyMessageType),
                        true
                    )
                val fileContent =
                    if (replyMessageType == ReplyMessageType.ReplyMessage) message.content() as FileContent else message.nearMessageContent() as FileContent
                fileContent.name?.let {
                    val fileTypeString = FileHelper.getFileTyle(it)
                    val fileType = FileType.of(fileTypeString)
                    withContext(Dispatchers.Main) {
                        fileMessageView.tvFileName.text = fileType.getName()
                        fileMessageView.ivFileIcon.setImageResource(fileType.drawable)
                    }
                } ?: run {
                    fileContent.name = "Unknown"
                    withContext(Dispatchers.Main) {
                        fileMessageView.tvFileName.visibility = View.GONE
                        fileMessageView.ivFileIcon.setImageResource(R.drawable.file_message_icon_file)
                    }
                }
                setFileStatus(fileContent, fileMessageView)

                return@withContext fileMessageView.root
            }
        }

    private fun setFileStatus(
        fileContent: FileContent,
        fileMessageBinding: ItemMessageFileBinding
    ) = CoroutineScope(Dispatchers.IO).launch {
        message?.let {
            val progress = fileContent.progress
            val status = it.status
            val size = formatSizeToDisplay(fileContent.size.toLong())
            withContext(Dispatchers.Main) {
                fileMessageBinding.progressBar.visibility =
                    if (progress.isNullOrEmpty() || "100" == progress) View.GONE else View.VISIBLE
                fileMessageBinding.tvFileStatus.text = ""
                fileMessageBinding.tvFileName.text =
                    KeyWordHelper.matcherSearchBackground(-0xfc7, fileContent.name, "")
                fileMessageBinding.tvFileSize.text = size
            }
            val file =
                if (!Strings.isNullOrEmpty(fileContent.android_local_path)) {
                    File(fileContent.android_local_path)
                } else {
                    File("${DownloadUtil.downloadFileDir}${fileContent.name}")
                }

            if (isRightMessage()) {
                when (status) {
                    MessageStatus.READ,
                    MessageStatus.RECEIVED,
                    MessageStatus.SUCCESS -> {
                        withContext(Dispatchers.Main) {
                            fileMessageBinding.tvFileStatus.text = "已送出"
                            if (!file.exists()) {
                                fileMessageBinding.tvFileStatus.text = "未下載"
                            }
                            if (!Strings.isNullOrEmpty(progress)) {
                                val pgs = progress.toInt()
                                if (pgs >= 100) {
                                    fileMessageBinding.progressBar.visibility = View.GONE
                                } else {
                                    fileMessageBinding.tvFileStatus.text = "正在下載"
                                    fileMessageBinding.progressBar.progress = pgs
                                    fileMessageBinding.progressBar.visibility = View.VISIBLE
                                }
                            }
                        }
                    }

                    MessageStatus.SENDING -> {
                        withContext(Dispatchers.Main) {
                            fileMessageBinding.tvFileStatus.text = "正在上傳"
                            if (!Strings.isNullOrEmpty(progress)) {
                                val pgs = progress.toInt()
                                if (pgs >= 100) {
                                    fileMessageBinding.progressBar.progress = 0
                                    fileMessageBinding.progressBar.visibility = View.GONE
                                    fileMessageBinding.tvFileStatus.text = "已送出"
                                } else {
                                    fileMessageBinding.progressBar.progress = pgs
                                    fileMessageBinding.progressBar.visibility = View.VISIBLE
                                }
                            }
                        }
                    }

                    MessageStatus.IS_REMOTE -> {
                        withContext(Dispatchers.Main) {
                            if (file.exists()) {
                                fileMessageBinding.tvFileStatus.text = "已送出"
                            } else {
                                fileMessageBinding.tvFileStatus.text = "檔案不存在"
                            }
                        }
                    }

                    MessageStatus.FAILED,
                    MessageStatus.ERROR -> {
                        withContext(Dispatchers.Main) {
                            fileMessageBinding.tvFileStatus.text = "送出失敗"
                        }
                    }

                    else -> {
                        withContext(Dispatchers.Main) {
                            fileMessageBinding.tvFileStatus.text = "送出失敗"
                        }
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    fileMessageBinding.tvFileStatus.text =
                        if (file.exists()) "已下載" else "未下載"
                    fileMessageBinding.progressBar.progress = 0
                    fileMessageBinding.progressBar.visibility = View.GONE
                    if (!Strings.isNullOrEmpty(progress)) {
                        fileMessageBinding.tvFileStatus.text = "正在下載"
                        val pgs = progress.toInt()
                        if (pgs >= 100) {
                            fileMessageBinding.progressBar.progress = 0
                            fileMessageBinding.progressBar.visibility = View.GONE
                            fileMessageBinding.tvFileStatus.text = "已下載"
                        } else {
                            fileMessageBinding.progressBar.progress = pgs
                            fileMessageBinding.progressBar.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    private suspend fun formatSizeToDisplay(size: Long): String =
        withContext(Dispatchers.IO) {
            val decimalFormat = DecimalFormat("#.0")
            return@withContext when {
                size < 1024 -> "${size}B"
                size < 1048576 -> "${decimalFormat.format(size / 1024.0)}K"
                else -> "${decimalFormat.format(size / 1048576.0)}M"
            }
        }

    private suspend fun assemblyVoiceView(replyMessageType: ReplyMessageType): View? =
        withContext(Dispatchers.Main) {
            message?.let { message ->
                val voiceMessageView =
                    MsgkitVoiceBinding.inflate(
                        layoutInflater,
                        getReplyMessageParent(replyMessageType),
                        true
                    )
                val voiceContent =
                    if (replyMessageType == ReplyMessageType.ReplyMessage) message.content() as VoiceContent else message.nearMessageContent() as VoiceContent
                voiceMessageView.msgVoiceRightContent.visibility =
                    if (isRightMessage()) View.VISIBLE else View.GONE
                voiceMessageView.msgVoiceLeftContent.visibility =
                    if (isRightMessage()) View.GONE else View.VISIBLE
                if (isRightMessage()) {
                    voiceContent.isRead = true
                } else {
                    voiceMessageView.messageVoiceDot.visibility =
                        if (voiceContent.isRead) View.INVISIBLE else View.VISIBLE
                }
                val voiceTextView =
                    if (isRightMessage()) voiceMessageView.msgVoiceRightContent else voiceMessageView.msgVoiceLeftContent
                voiceTextView.text = VoiceHelper.strDuration(voiceContent.duration * 1000)
                return@withContext voiceMessageView.root
            }
        }

    private var videoMessageView: MsgkitVideoBinding? = null

    private suspend fun assemblyVideoView(replyMessageType: ReplyMessageType): View? =
        withContext(Dispatchers.Main) {
            message?.let { message ->
                videoMessageView =
                    MsgkitVideoBinding.inflate(
                        layoutInflater,
                        getReplyMessageParent(replyMessageType),
                        true
                    )
                val videoContent =
                    if (replyMessageType == ReplyMessageType.ReplyMessage) message.content() as VideoContent else message.nearMessageContent() as VideoContent

                val messageId = if (replyMessageType == ReplyMessageType.ReplyMessage) message.id else message.nearMessageId
                videoMessageView?.let { binding ->
                    setPreviewImage(videoContent, binding, replyMessageType)
                    binding.occupationCL.setOnClickListener {
                        downloadVideo(binding, replyMessageType)
                    }

                    binding.progressBar.setOnFileClickListener {
                        getDownloadTask(videoContent)?.let {
                            it.task.cancel()
                            canceledOrFailureHandle(binding, replyMessageType)
                            EventBus.getDefault().post(EventMsg(MsgConstant.MESSAGE_VIDEO_UPDATE_DOWNLOAD_CANCEL, messageId))
                        }
                    }
                }

                return@withContext videoMessageView?.root
            }
        }

    // 設置影片預覽圖
    private fun setPreviewImage(
        videoContent: VideoContent,
        videoBinding: MsgkitVideoBinding,
        replyMessageType: ReplyMessageType
    ) = CoroutineScope(Dispatchers.IO).launch {
        try {
            message?.let { message ->
                val thumbnailUrl =
                    if (message.content.isNotEmpty()) videoContent.thumbnailUrl ?: "" else ""
                val file = getVideoFile(replyMessageType)
                var request: RequestBuilder<Drawable>? = null
                file?.let {
                    withContext(Dispatchers.Main) {
                        videoBinding.playIV.setImageResource(R.drawable.play)
                        videoBinding.clPlay.visibility = View.VISIBLE
                        val duration =
                            if (videoContent.duration == null || videoContent.duration == 0.0) {
                                getVideoDuration(it.absolutePath)
                            } else {
                                getUpdateTimeFormat((videoContent.duration * 1000).toInt())
                            }
                        videoBinding.tvPeriod.text = duration
                        videoBinding.tvPeriod.visibility = View.VISIBLE
                    }
                    val videoThumbnail = thumbnailUrl.ifEmpty { it.absolutePath }
                    withContext(Dispatchers.Main) {
                        request =
                            Glide.with(videoBinding.thumbnailRIV).load(videoThumbnail).apply(
                                RequestOptions()
                                    .frame(1000)
                                    .override(SMALL_PIC_SIZE)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .fitCenter()
                            )
                    }
                } ?: run {
                    withContext(Dispatchers.Main) {
                        videoBinding.playIV.setImageResource(R.drawable.ic_video_download)
                        videoBinding.tvPeriod.visibility = View.GONE
                        videoBinding.clPlay.visibility = View.VISIBLE
                    }
                    if (URLUtil.isValidUrl(videoContent.url) || videoContent.url.endsWith(".mp4")) {
                        val size = getZoomSize(videoContent, replyMessageType)
                        val thumbnailWidth = if (size[0] > 0) size[0] else 300
                        val thumbnailHeight = if (size[1] > 0) size[1] else 600
                        if (thumbnailUrl.isNotEmpty()) {
                            request =
                                Glide
                                    .with(videoBinding.thumbnailRIV)
                                    .load(thumbnailUrl)
                                    .apply(
                                        RequestOptions()
                                            .frame(1000)
                                            .override(thumbnailWidth, thumbnailHeight)
                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                                            .fitCenter()
                                    )
                        } else {
                            val params = videoBinding.root.layoutParams
                            val zoomSize = getZoomSize(videoContent, replyMessageType)
                            params?.let {
                                params.width = zoomSize[0]
                                params.height = zoomSize[1]
                                // 如果沒高度寬度 給預設
                                if (params.width <= 0) params.width = 300
                                if (params.height <= 0) params.height = 600
                                videoBinding.root.layoutParams = params
                            }

                            val mediaMetadataRetriever = MediaMetadataRetriever()
                            mediaMetadataRetriever.setDataSource(
                                videoContent.url,
                                HashMap<String, String>()
                            )
                            val bmFrame =
                                mediaMetadataRetriever.getFrameAtTime(1000) // unit in microsecond
                            bmFrame?.let {
                                withContext(Dispatchers.Main) {
                                    videoBinding.thumbnailRIV.setImageBitmap(it)
                                }
                            }
                        }
                    } else {
                        request =
                            Glide
                                .with(videoBinding.thumbnailRIV)
                                .load(R.drawable.image_load_error)
                                .apply(
                                    RequestOptions().override(SMALL_PIC_SIZE)
                                )
                        withContext(Dispatchers.Main) {
                            videoBinding.clPlay.visibility = View.GONE
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    request
                        ?.diskCacheStrategy(DiskCacheStrategy.ALL)
                        ?.into(videoBinding.thumbnailRIV)
                }
            }
        } catch (e: Exception) {
        }
    }

    private fun getDownloadTask(videoContent: VideoContent): DownloadingTask? =
        message?.let {
            DownloadUtil.getCurrentDownloadTask(videoContent.url)
        }

    private fun downloadVideo(
        videoBinding: MsgkitVideoBinding,
        replyMessageType: ReplyMessageType
    ) = CoroutineScope(Dispatchers.IO).launch {
        val currentMessage =
            if (replyMessageType == ReplyMessageType.NearMessage) {
                MessageReference.findById(message?.nearMessageId)
            } else {
                message
            }
        currentMessage?.let { messageEntity ->
            val videoContent = currentMessage.content() as VideoContent
            DownloadUtil.doDownloadVideoFile(
                videoContent = videoContent,
                onDownloadProgress = {
                    EventBus.getDefault().post(EventMsg(MsgConstant.MESSAGE_VIDEO_UPDATE_DOWNLOAD_PROGRESS, MessageVideoDownloadProgress(it, messageEntity.id!!)))
                    updateProgress(it)
                },
                onDownloadSuccess = {
                    handleDownloadSuccess(replyMessageType, videoContent, it)
                    setPreviewImage(videoContent, videoBinding, replyMessageType)
                },
                onDownloadFailed = {
                    getDownloadTask(videoContent)?.let {
                        val file = File(it.path)
                        if (file.exists()) {
                            file.delete()
                        }
                    }
                }
            )
        }
    }

    private fun updateProgress(progress: Int) =
        CoroutineScope(Dispatchers.Main).launch {
            videoMessageView?.let {
                it.progressBar.progress = progress
                if (it.progressBar.progress >= 100) {
                    it.progressBar.visibility = View.GONE
                    it.tvPeriod.visibility = View.VISIBLE
                    it.clPlay.visibility = View.VISIBLE
                } else {
                    it.progressBar.visibility = View.VISIBLE
                    it.tvPeriod.visibility = View.GONE
                    it.clPlay.visibility = View.GONE
                }
            }
        }

    private fun handleDownloadSuccess(
        replyMessageType: ReplyMessageType,
        videoContent: VideoContent,
        file: File
    ) {
        val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        intent.data = Uri.fromFile(file)
        binding.root.context.sendBroadcast(intent)
        videoContent.isDownload = true
        videoContent.progress = "100"
        onContentUpdate(replyMessageType, videoContent.javaClass.name, videoContent.toStringContent())
        message?.let {
            if (replyMessageType == ReplyMessageType.NearMessage) {
                val eventModel = FileDownloadSuccessEvent(it.nearMessageId!!, file.absolutePath)
                EventBus.getDefault().post(EventMsg(MsgConstant.MESSAGE_VIDEO_ON_DOWNLOAD_SUCCESS, eventModel))
            } else {
                val eventModel = FileDownloadSuccessEvent(it.id!!, file.absolutePath)
                EventBus.getDefault().post(EventMsg(MsgConstant.MESSAGE_VIDEO_ON_DOWNLOAD_SUCCESS, eventModel))
            }
        }
    }

    private fun canceledOrFailureHandle(
        videoBinding: MsgkitVideoBinding,
        replyMessageType: ReplyMessageType
    ) = CoroutineScope(Dispatchers.IO).launch {
        val currentMessage =
            if (replyMessageType == ReplyMessageType.NearMessage) {
                MessageReference.findById(message?.nearMessageId)
            } else {
                message
            }
        currentMessage?.let {
            val videoContent = it.content() as VideoContent
            val currentFile = getVideoFile(replyMessageType)
            currentFile?.let { deleteFile(it.absolutePath) }
            videoContent.progress = "0"
            videoContent.isDownload = false
//            it.content = videoContent.toStringContent()
//            onContentUpdate(videoContent.javaClass.name, videoContent.toStringContent())
            withContext(Dispatchers.Main) {
                videoBinding.progressBar.progress = 0
                videoBinding.progressBar.visibility = View.GONE
                videoBinding.progressBar.isCanceledLoading = false
                videoBinding.playIV.setImageResource(R.drawable.ic_video_download)
                videoBinding.tvPeriod.visibility = View.GONE
                videoBinding.clPlay.visibility = View.VISIBLE
            }
        }
    }

    private fun deleteFile(downloadPath: String) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val file = File(downloadPath)
                val isDeleted = file.delete()
                CELog.e(if (isDeleted) "刪除檔案成功" else "刪除檔案失敗")
            } catch (e: Exception) {
                CELog.e("刪除檔案失敗", e)
            }
        }

    // 判斷是否已下載
    private suspend fun getVideoFile(
        replyMessageType: ReplyMessageType
    ): File? =
        withContext(Dispatchers.IO) {
            val currentMessage =
                if (replyMessageType == ReplyMessageType.NearMessage) {
                    MessageReference.findById(message?.nearMessageId)
                } else {
                    message
                }
            currentMessage?.let {
                val videoContent = it.content() as VideoContent
                val downloadPath = DownloadUtil.downloadFileDir + currentMessage.sendTime + "_" + videoContent.name
                val downloadFile = File(downloadPath)
                if (downloadFile.exists()) return@withContext downloadFile

                val localPath = videoContent.android_local_path ?: ""
                val localFile = File(localPath)
                if (localFile.exists()) return@withContext localFile
            }

            return@withContext null
        }

    /**
     * 若影片長度超過 1 小時, 格式為 h:mm:ss, e.g., 影片時長1小時3分06秒, 顯示為 1:03:06
     * 若影片長度少於 1 小時, 格式為 m:ss, e.g., 影片時長3分06秒, 顯示為 3:06
     * 若影片時長為5秒，則顯示為 0:05
     */
    @SuppressLint("DefaultLocale")
    private suspend fun getUpdateTimeFormat(millisecond: Int): String =
        withContext(Dispatchers.IO) {
            // 将毫秒转换为秒
            val second = millisecond / 1000
            // 计算小时
            val hh = second / 3600
            // 计算分钟
            val mm = second % 3600 / 60
            // 计算秒
            val ss = second % 60
            // 判断时间单位的位数
            val str: String =
                if (hh != 0) { // 表示时间单位为三位
                    String.format("%02d:%02d:%02d", hh, mm, ss)
                } else {
                    String.format("%02d:%02d", mm, ss)
                }
            return@withContext str
        }

    private suspend fun zoomImage(
        width: Int,
        height: Int
    ): IntArray =
        withContext(Dispatchers.IO) {
            return@withContext if (width > height) {
                intArrayOf(SMALL_PIC_SIZE, (height * SMALL_PIC_SIZE) / width)
            } else {
                intArrayOf((width * SMALL_PIC_SIZE) / height, SMALL_PIC_SIZE)
            }
        }

    fun setKeyword(keyword: String) {
        this.keyword = keyword
    }

    private suspend fun getZoomSize(
        videoContent: VideoContent,
        replyMessageType: ReplyMessageType
    ): IntArray =
        withContext(Dispatchers.IO) {
            return@withContext if (videoContent.width != 0 && videoContent.height != 0) {
                zoomImage(videoContent.width, videoContent.height)
            } else if (videoContent.thumbnailHeight != 0 && videoContent.thumbnailWidth != 0) {
                zoomImage(videoContent.thumbnailWidth, videoContent.thumbnailHeight)
            } else {
                val file = getVideoFile(replyMessageType)
                val size =
                    if (file == null) VideoHelper.size(videoContent.url) else VideoHelper.size(file.absolutePath)
                if (size[0] > 0 && size[1] > 0) {
                    zoomImage(size[0], size[1])
                } else {
                    size
                }
            }
        }

    private fun getReplyMessageParent(replyMessageType: ReplyMessageType): ViewGroup =
        if (replyMessageType == ReplyMessageType.ReplyMessage) {
            replyMessageBinding.underFrameLayout
        } else {
            replyMessageBinding.nearFrameLayout
        }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handleEvent(eventMsg: EventMsg<*>) {
        message?.let { message ->
            when (eventMsg.code) {
                MsgConstant.MESSAGE_VIDEO_UPDATE_DOWNLOAD_PROGRESS -> {
                    val messageVideoDownloadProgress = eventMsg.data as MessageVideoDownloadProgress
                    when (messageVideoDownloadProgress.messageId) {
                        message.id -> {
                            updateProgress(messageVideoDownloadProgress.progress)
                        }

                        else -> {
                            updateProgress(messageVideoDownloadProgress.progress)
                        }
                    }
                }

                MsgConstant.MESSAGE_VIDEO_ON_DOWNLOAD_SUCCESS -> {
                    val eventModel = eventMsg.data as FileDownloadSuccessEvent
                    if (message.id == eventModel.messageId || message.nearMessageId == eventModel.messageId) {
                        message.nearMessageId?.let {
                            val nearMessage = MessageReference.findById(it)
                            nearMessage?.let {
                                message.nearMessageType = it.type
                                message.nearMessageContent = it.content
                                message.nearMessageSenderId = it.senderId
                                it.type?.let { setMessage(it, ReplyMessageType.NearMessage) }
                            }
                        }

                        message.type?.let {
                            setMessage(it, ReplyMessageType.ReplyMessage)
                        }
                    } else {
                    }
                }

                MsgConstant.MESSAGE_VIDEO_UPDATE_DOWNLOAD_CANCEL -> {
                    val messageId = eventMsg.data as String
                    videoMessageView?.let {
                        when (messageId) {
                            message.id -> {
                                canceledOrFailureHandle(it, ReplyMessageType.ReplyMessage)
                            }

                            else -> {
                                canceledOrFailureHandle(it, ReplyMessageType.NearMessage)
                            }
                        }
                    }
                }

                else -> {
                }
            }
        }
    }

    fun clearEventBus() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }
}

enum class ReplyMessageType {
    NearMessage,
    ReplyMessage
}
