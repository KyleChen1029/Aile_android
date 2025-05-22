package tw.com.chainsea.chat.messagekit.main.viewholder

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.webkit.URLUtil
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.android.common.log.CELog
import tw.com.chainsea.android.common.ui.UiHelper
import tw.com.chainsea.android.common.video.VideoHelper
import tw.com.chainsea.ce.sdk.bean.msg.MessageStatus
import tw.com.chainsea.ce.sdk.bean.msg.content.VideoContent
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.event.EventMsg
import tw.com.chainsea.ce.sdk.event.MsgConstant
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.ItemBaseMessageBinding
import tw.com.chainsea.chat.databinding.MsgkitVideoBinding
import tw.com.chainsea.chat.messagekit.main.FileDownloadSuccessEvent
import tw.com.chainsea.chat.messagekit.main.MessageVideoDownloadProgress
import tw.com.chainsea.chat.messagekit.main.viewholder.base.BaseMessageViewHolder
import tw.com.chainsea.chat.messagekit.main.viewholder.base.OnMessageSlideReply
import tw.com.chainsea.chat.util.DownloadUtil
import tw.com.chainsea.chat.util.DownloadUtil.getVideoDuration
import tw.com.chainsea.chat.util.DownloadingTask
import java.io.File

class VideoMessageView1(
    binding: ItemBaseMessageBinding,
    layoutInflater: LayoutInflater,
    chatRoomEntity: ChatRoomEntity,
    onMessageSlideReply: OnMessageSlideReply
) : BaseMessageViewHolder(binding, chatRoomEntity, onMessageSlideReply = onMessageSlideReply) {
    companion object {
        private const val SMALL_PIC_SIZE: Int = 175
    }

    private var videoBinding: MsgkitVideoBinding = MsgkitVideoBinding.inflate(layoutInflater)
    private var downloadStatus = DownloadingStatus.UNDEF
    private val downloadTask: DownloadingTask?
        get() {
            return message?.let {
                val videoContent = it.content() as VideoContent
                DownloadUtil.getCurrentDownloadTask(videoContent.url)
            }
        }

    override fun onMessageClick() {
        downloadOrPlayVideo()
    }

    override fun bind(message: MessageEntity) {
        super.bind(message)
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        CoroutineScope(Dispatchers.IO).launch {
            setBubbleView(videoBinding.root)
            val videoContent = message.content() as VideoContent

            withContext(Dispatchers.Main) {
                val params = videoBinding.root.layoutParams
                val zoomSize = getZoomSize(message, videoContent)
                val widthPx = UiHelper.dip2px(videoBinding.root.context, zoomSize[0].toFloat(), 0.5f)
                val heightPx = UiHelper.dip2px(videoBinding.root.context, zoomSize[1].toFloat(), 0.5f)
                params?.let {
                    params.width = widthPx
                    params.height = heightPx
                    // 如果沒高度寬度 給預設
                    if (params.width <= 0) params.width = 300
                    if (params.height <= 0) params.height = 600
                    videoBinding.root.layoutParams = params
                }
                videoBinding.thumbnailRIV.visibility = View.VISIBLE
                videoBinding.clPlay.visibility = View.VISIBLE
                videoBinding.progressBar.setOnFileClickListener {
                    downloadTask?.let {
                        it.task.cancel()
                        downloadStatus = DownloadingStatus.CANCELED
                        Toast.makeText(videoBinding.root.context, videoBinding.root.context.getString(R.string.canceled_downloading), Toast.LENGTH_SHORT).show()
                        videoBinding.progressBar.isCanceledLoading = false
                        canceledOrFailureHandle()
                        deleteFile(it.path)
                        EventBus.getDefault().post(
                            EventMsg(
                                MsgConstant.MESSAGE_VIDEO_UPDATE_DOWNLOAD_CANCEL,
                                message.id
                            )
                        )
                    }
                }
                videoBinding.root.background = null
            }
            setMessageStatus()
            setPreviewImage()
        }
    }

    private fun setMessageStatus() =
        CoroutineScope(Dispatchers.IO).launch {
            message?.let { message ->
                val videoContent = message.content() as VideoContent
                downloadTask?.let {
                    updateProgress(it.progress)
                    return@launch
                }

                if (videoContent.progress.isNullOrEmpty()) return@launch
                val pgs = videoContent.progress.toInt()
                when (message.status) {
                    MessageStatus.READ, MessageStatus.RECEIVED, MessageStatus.SUCCESS -> {
                        withContext(Dispatchers.Main) {
                            if (pgs >= 100) {
                                videoBinding.progressBar.visibility = View.GONE
                                videoBinding.clPlay.visibility = View.VISIBLE
                            } else {
                                videoBinding.progressBar.progress = pgs
                                videoBinding.clPlay.visibility = View.GONE
                            }
                        }
                    }

                    MessageStatus.SENDING -> {
                        withContext(Dispatchers.Main) {
                            if (pgs >= 100) {
                                videoBinding.progressBar.progress = 0
                                videoBinding.progressBar.visibility = View.GONE
                                videoBinding.clPlay.visibility = View.VISIBLE
                            } else {
                                videoBinding.progressBar.progress = pgs
                                videoBinding.clPlay.visibility = View.GONE
                            }
                        }
                    }

                    MessageStatus.FAILED, MessageStatus.ERROR -> {
                    }

                    else -> {
                        // nothing
                    }
                }
            }
        }

    // 設置影片預覽圖
    private fun setPreviewImage() =
        CoroutineScope(Dispatchers.IO).launch {
            message?.let { message ->
                try {
                    val videoContent = message.content() as VideoContent
                    val thumbnailUrl =
                        if (message.content.isNotEmpty()) videoContent.thumbnailUrl ?: "" else ""
                    val file = getVideoFile(videoContent)
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
                            videoBinding.tvPeriod.text = duration.toString()
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
                            if (thumbnailUrl.isNotEmpty()) {
                                val thumbnailWidth = if (videoContent.thumbnailWidth > 0) videoContent.thumbnailWidth else 300
                                val thumbnailHeight = if (videoContent.thumbnailHeight > 0) videoContent.thumbnailHeight else 600
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
                                val mediaMetadataRetriever = MediaMetadataRetriever()
                                mediaMetadataRetriever.setDataSource(
                                    videoContent.url,
                                    HashMap<String, String>()
                                )
                                val bmFrame =
                                    mediaMetadataRetriever.getFrameAtTime(1000) // unit in microsecond
                                bmFrame?.let {
                                    withContext(Dispatchers.Main) {
                                        videoBinding.thumbnailRIV.setImageBitmap(bmFrame)
                                    }
                                }
                            }
                        } else {
                            request =
                                Glide
                                    .with(videoBinding.thumbnailRIV)
                                    .load(R.drawable.image_load_error)
                                    .apply(
                                        RequestOptions()
                                            .override(Companion.SMALL_PIC_SIZE)
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
                } catch (e: Exception) {
                }
            }
        }

    private suspend fun getZoomSize(
        message: MessageEntity,
        videoContent: VideoContent
    ): IntArray =
        withContext(Dispatchers.IO) {
            return@withContext if (videoContent.width != 0 && videoContent.height != 0) {
                zoomImage(videoContent.width, videoContent.height)
            } else if (videoContent.thumbnailHeight != 0 && videoContent.thumbnailWidth != 0) {
                zoomImage(videoContent.thumbnailWidth, videoContent.thumbnailHeight)
            } else {
                val file = getVideoFile(videoContent)
                val size =
                    if (file == null) VideoHelper.size(videoContent.url) else VideoHelper.size(file.absolutePath)
                if (size[0] > 0 && size[1] > 0) {
                    zoomImage(size[0], size[1])
                } else {
                    size
                }
            }
        }

    // 判斷是否已下載
    private suspend fun getVideoFile(videoContent: VideoContent): File? =
        withContext(Dispatchers.IO) {
            val downloadPath = DownloadUtil.downloadFileDir + videoContent.name
            return@withContext if (videoContent.android_local_path != null && File(videoContent.android_local_path).exists()) {
                File(videoContent.android_local_path)
            } else if (File(downloadPath).exists()) {
                File(downloadPath)
            } else {
                null
            }
        }

    private suspend fun zoomImage(
        width: Int,
        height: Int
    ): IntArray =
        withContext(Dispatchers.IO) {
            return@withContext if (width > height) {
                intArrayOf(SMALL_PIC_SIZE, (height * Companion.SMALL_PIC_SIZE) / width)
            } else {
                intArrayOf((width * SMALL_PIC_SIZE) / height, Companion.SMALL_PIC_SIZE)
            }
        }

    private fun updateProgress(progress: Int) =
        CoroutineScope(Dispatchers.Main).launch {
            videoBinding.progressBar.progress = progress
            if (videoBinding.progressBar.progress >= 100) {
                videoBinding.progressBar.visibility = View.GONE
                videoBinding.clPlay.visibility = View.VISIBLE
            } else {
                videoBinding.progressBar.visibility = View.VISIBLE
                videoBinding.clPlay.visibility = View.GONE
            }
        }

    private fun onDownSuccess(
        videoContent: VideoContent,
        file: File
    ) = CoroutineScope(Dispatchers.IO).launch {
        message?.let { message ->
            if (downloadStatus == DownloadingStatus.CANCELED) {
                canceledOrFailureHandle()
                deleteFile(DownloadUtil.downloadFileDir + videoContent.name)
            } else {
                if (file.exists()) handleDownloadSuccessful(videoContent, file)
            }
            onContentUpdate(videoContent::class.java.name, videoContent.toStringContent())
        }
    }

    private fun canceledOrFailureHandle() =
        CoroutineScope(Dispatchers.IO).launch {
            message?.let { message ->
                val videoContent = message.content() as VideoContent
                videoContent.progress = "0"
                videoContent.isDownload = false
                message.content = videoContent.toStringContent()
                withContext(Dispatchers.Main) {
                    videoBinding.root.post {
                        videoBinding.progressBar.progress = 0
                        videoBinding.progressBar.visibility = View.GONE
                        videoBinding.progressBar.isCanceledLoading = false
                        videoBinding.playIV.setImageResource(R.drawable.ic_video_download)
                        videoBinding.tvPeriod.visibility = View.GONE
                        videoBinding.clPlay.visibility = View.VISIBLE
                    }
                }
                onContentUpdate(videoContent.javaClass.name, videoContent.toStringContent())
            }
        }

    private fun handleDownloadSuccessful(
        videoContent: VideoContent,
        file: File
    ) = CoroutineScope(Dispatchers.IO).launch {
        withContext(Dispatchers.Main) {
            videoBinding.progressBar.progress = 0
            videoBinding.progressBar.visibility = View.GONE
            videoBinding.progressBar.isCanceledLoading = false
            videoBinding.playIV.setImageResource(R.drawable.play)
            videoBinding.clPlay.visibility = View.VISIBLE
            videoBinding.tvPeriod.text = getVideoDuration(file.absolutePath)
            videoBinding.tvPeriod.visibility = View.VISIBLE
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val contentUri = Uri.fromFile(file)
            mediaScanIntent.data = contentUri
            videoBinding.root.context.sendBroadcast(mediaScanIntent)
        }
        videoContent.isDownload = true
        videoContent.progress = "100"
        downloadStatus = DownloadingStatus.SUCCESS
        onContentUpdate(videoContent.javaClass.name, videoContent.toStringContent())
        val eventModel = FileDownloadSuccessEvent(message!!.id!!, file.absolutePath)
        EventBus.getDefault().post(EventMsg(MsgConstant.MESSAGE_VIDEO_ON_DOWNLOAD_SUCCESS, eventModel))
    }

    private fun deleteFile(downloadPath: String) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val file = File(downloadPath)
                val isDeleted = file.delete()
                if (isDeleted) {
                    downloadStatus = DownloadingStatus.FAILURE
                }
                CELog.e(if (isDeleted) "刪除檔案成功" else "刪除檔案失敗")
            } catch (e: Exception) {
                CELog.e("刪除檔案失敗", e)
            }
        }

    private fun downloadOrPlayVideo() =
        CoroutineScope(Dispatchers.IO).launch {
            message?.let { message ->
                val videoContent = message.content() as VideoContent
                val downloadPath = DownloadUtil.downloadFileDir + videoContent.name
                val isFileExists = File(downloadPath).exists() || (!videoContent.android_local_path.isNullOrEmpty() && File(videoContent.android_local_path).exists())
                if (isFileExists) {
                    onMessageClickListener?.get()?.onVideoMessageClick(message)
                    videoBinding.progressBar.setOnFileClickListener(null)
                } else {
                    downloadStatus = DownloadingStatus.UNDEF
                    DownloadUtil.doDownloadVideoFile(
                        videoContent,
                        { progress ->
                            if (downloadStatus == DownloadingStatus.CANCELED || downloadStatus == DownloadingStatus.FAILURE) {
                                canceledOrFailureHandle()
                            } else {
                                EventBus.getDefault().post(EventMsg(MsgConstant.MESSAGE_VIDEO_UPDATE_DOWNLOAD_PROGRESS, MessageVideoDownloadProgress(progress, message.id!!)))
                                videoContent.progress = progress.toString()
                            }
                        },
                        { downloadFile ->
                            onDownSuccess(videoContent, downloadFile)
                        },
                        { errorMessage ->
                            downloadTask?.let {
                                deleteFile(it.path)
                            }
                            canceledOrFailureHandle()
                        }
                    )
                }
            }
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

    enum class DownloadingStatus {
        UNDEF,
        SUCCESS,
        CANCELED,
        FAILURE
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(eventMsg: EventMsg<Any>) {
        when (eventMsg.code) {
            MsgConstant.MESSAGE_VIDEO_UPDATE_DOWNLOAD_PROGRESS -> {
                val messageVideoDownloadProgress = eventMsg.data as MessageVideoDownloadProgress
                if (messageVideoDownloadProgress.messageId == message?.id) {
                    downloadTask?.let {
                        updateProgress(messageVideoDownloadProgress.progress)
                    }
                }
            }

            MsgConstant.MESSAGE_VIDEO_ON_DOWNLOAD_SUCCESS -> {
                val data = eventMsg.data as FileDownloadSuccessEvent
                if (message!!.id == data.messageId) {
                    val videoContent = message!!.content() as VideoContent
                    val file = File(data.filePath)
                    videoBinding.progressBar.progress = 0
                    videoBinding.progressBar.visibility = View.GONE
                    videoBinding.progressBar.isCanceledLoading = false
                    videoBinding.playIV.setImageResource(R.drawable.play)
                    videoBinding.tvPeriod.text = getVideoDuration(file.absolutePath)
                    videoBinding.tvPeriod.visibility = View.VISIBLE
                    videoContent.isDownload = true
                    videoContent.progress = "100"
                    downloadStatus = DownloadingStatus.SUCCESS
                    onContentUpdate(videoContent.javaClass.name, videoContent.toStringContent())
                }
            }

            MsgConstant.MESSAGE_VIDEO_UPDATE_DOWNLOAD_CANCEL -> {
                val messageId = eventMsg.data as String
                if (messageId == message?.id) {
                    canceledOrFailureHandle()
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
