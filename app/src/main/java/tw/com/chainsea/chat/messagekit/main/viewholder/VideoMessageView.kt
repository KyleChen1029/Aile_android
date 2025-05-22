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
import androidx.core.content.res.ResourcesCompat
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.android.common.log.CELog
import tw.com.chainsea.android.common.video.VideoHelper
import tw.com.chainsea.ce.sdk.bean.msg.MessageStatus
import tw.com.chainsea.ce.sdk.bean.msg.content.VideoContent
import tw.com.chainsea.ce.sdk.event.EventMsg
import tw.com.chainsea.ce.sdk.event.MsgConstant
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.MsgkitVideoBinding
import tw.com.chainsea.chat.messagekit.main.MessageVideoDownloadProgress
import tw.com.chainsea.chat.messagekit.main.viewholder.base.MessageBubbleView
import tw.com.chainsea.chat.util.DownloadUtil
import tw.com.chainsea.chat.util.DownloadUtil.getVideoDuration
import tw.com.chainsea.chat.util.DownloadingTask
import java.io.File

class VideoMessageView(
    binding: ViewBinding
) : MessageBubbleView<VideoContent>(binding) {
    private var binding: MsgkitVideoBinding =
        MsgkitVideoBinding.inflate(LayoutInflater.from(itemView.context), null, false)

    companion object {
        private const val SMALL_PIC_SIZE: Int = 360
    }

    private val downloadTask: DownloadingTask?
        get() {
            return message?.let {
                val videoContent = it.content() as VideoContent
                DownloadUtil.getCurrentDownloadTask(videoContent.url)
            }
        }

    private var downloadStatus = DownloadingStatus.UNDEF

    private var isLoadingVideoFirstFrame: Boolean = false

    override fun getContentResId(): Int = R.layout.msgkit_video

    override fun getChildView(): View = binding.root

    override fun rightBackground(): Int = R.drawable.msgkit_trans_bg

    override fun leftBackground() = R.drawable.msgkit_trans_bg

    override fun onDoubleClick(
        v: View?,
        message: MessageEntity?
    ) {
        CELog.d("onDoubleClick")
    }

    override fun onLongClick(
        v: View?,
        x: Float,
        y: Float,
        message: MessageEntity?
    ) {
        onMessageControlEventListener?.onLongClick(message, x.toInt(), y.toInt())
    }

    override fun showName(): Boolean = !isRightMessage

    override fun bindContentView(videoContent: VideoContent) {
        binding.thumbnailRIV.setImageDrawable(null)
        isLoadingVideoFirstFrame = false
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        CoroutineScope(Dispatchers.IO).launch {
            val params = binding.occupationCL.layoutParams
            val zoomSize = getZoomSize(videoContent)
            params.width = zoomSize[0]
            params.height = zoomSize[1]
            // 如果沒高度寬度 給預設
            if (params.width <= 0) params.width = 300
            if (params.height <= 0) params.height = 600

            withContext(Dispatchers.Main) {
                binding.occupationCL.layoutParams = params
                binding.occupationCL.background =
                    ResourcesCompat.getDrawable(
                        binding.root.context.resources,
                        R.drawable.file_msg_down_bg,
                        null
                    )
                binding.thumbnailRIV.visibility = View.VISIBLE
                binding.clPlay.visibility = View.VISIBLE
            }

            setMessageStatus(videoContent.progress)
            setPreviewImage(videoContent)
        }
    }

    private fun setMessageStatus(progress: String?) =
        CoroutineScope(Dispatchers.IO).launch {
            if (progress.isNullOrEmpty()) return@launch
            val pgs = progress.toInt()
            when (message.status) {
                MessageStatus.READ, MessageStatus.RECEIVED, MessageStatus.SUCCESS -> {
                    withContext(Dispatchers.Main) {
                        if (pgs >= 100) {
                            binding.progressBar.visibility = View.GONE
                            binding.clPlay.visibility = View.VISIBLE
                        } else {
                            binding.progressBar.progress = pgs
                            binding.clPlay.visibility = View.GONE
                        }
                    }
                }

                MessageStatus.SENDING -> {
                    withContext(Dispatchers.Main) {
                        if (pgs >= 100) {
                            binding.progressBar.progress = 0
                            binding.progressBar.visibility = View.GONE
                            binding.clPlay.visibility = View.VISIBLE
                        } else {
                            binding.progressBar.progress = pgs
                            binding.clPlay.visibility = View.GONE
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

    // 設置影片預覽圖
    private fun setPreviewImage(videoContent: VideoContent) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val thumbnailUrl = if (message.content.isNotEmpty()) videoContent.thumbnailUrl ?: "" else ""
                val file = getVideoFile(videoContent)
                var request: RequestBuilder<Drawable>? = null
                file?.let {
                    withContext(Dispatchers.Main) {
                        binding.playIV.setImageResource(R.drawable.play)
                        binding.clPlay.visibility = View.VISIBLE
                        val duration =
                            if (videoContent.duration == null || videoContent.duration == 0.0) {
                                getVideoDuration(it.absolutePath)
                            } else {
                                getUpdateTimeFormat((videoContent.duration * 1000).toInt())
                            }
                        binding.tvPeriod.text = duration.toString()
                        binding.tvPeriod.visibility = View.VISIBLE
                    }
                    val videoThumbnail = thumbnailUrl.ifEmpty { it.absolutePath }
                    withContext(Dispatchers.Main) {
                        request =
                            Glide
                                .with(binding.thumbnailRIV)
                                .load(videoThumbnail)
                                .apply(
                                    RequestOptions()
                                        .override(SMALL_PIC_SIZE)
                                        .fitCenter()
                                )
                    }
                } ?: run {
                    withContext(Dispatchers.Main) {
                        binding.playIV.setImageResource(R.drawable.ic_video_download)
                        binding.tvPeriod.visibility = View.GONE
                        binding.clPlay.visibility = View.VISIBLE
                    }
                    if (URLUtil.isValidUrl(videoContent.url) || videoContent.url.endsWith(".mp4")) {
                        if (thumbnailUrl.isNotEmpty()) {
                            val thumbnailWidth =
                                if (videoContent.thumbnailWidth > 0) videoContent.thumbnailWidth else 300
                            val thumbnailHeight =
                                if (videoContent.thumbnailHeight > 0) videoContent.thumbnailHeight else 600
                            request =
                                Glide
                                    .with(binding.thumbnailRIV)
                                    .load(thumbnailUrl)
                                    .apply(
                                        RequestOptions()
                                            .frame(1000)
                                            .override(thumbnailWidth, thumbnailHeight)
                                            .fitCenter()
                                    )
                        } else {
                            isLoadingVideoFirstFrame = true
                            val mediaMetadataRetriever = MediaMetadataRetriever()
                            mediaMetadataRetriever.setDataSource(
                                videoContent.url,
                                HashMap<String, String>()
                            )
                            val bmFrame =
                                mediaMetadataRetriever.getFrameAtTime(1000) // unit in microsecond
                            withContext(Dispatchers.Main) {
                                bmFrame?.let {
                                    if (!isLoadingVideoFirstFrame) return@withContext
                                    binding.thumbnailRIV.setImageBitmap(bmFrame)
                                }
                            }
                        }
                    } else {
                        request =
                            Glide
                                .with(binding.thumbnailRIV)
                                .load(if (isGreenTheme) R.drawable.image_load_error_green else R.drawable.image_load_error)
                                .apply(
                                    RequestOptions()
                                        .override(SMALL_PIC_SIZE)
                                )
                        withContext(Dispatchers.Main) {
                            binding.clPlay.visibility = View.GONE
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    request?.diskCacheStrategy(DiskCacheStrategy.ALL)?.into(
                        object : CustomTarget<Drawable>() {
                            override fun onLoadFailed(errorDrawable: Drawable?) {
                                super.onLoadFailed(errorDrawable)
                            }

                            override fun onLoadStarted(placeholder: Drawable?) {
                                super.onLoadStarted(placeholder)
                            }

                            override fun onResourceReady(
                                resource: Drawable,
                                transition: Transition<in Drawable>?
                            ) {
                                binding.thumbnailRIV.setImageDrawable(resource)
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                            }
                        }
                    )
                }
            } catch (ignored: Exception) {
            }
        }

    private suspend fun getZoomSize(videoContent: VideoContent): IntArray =
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
            val downloadPath =
                DownloadUtil.downloadFileDir + videoContent.name
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
                intArrayOf(SMALL_PIC_SIZE, (height * SMALL_PIC_SIZE) / width)
            } else {
                intArrayOf((width * SMALL_PIC_SIZE) / height, SMALL_PIC_SIZE)
            }
        }

    private fun updateProgress(progress: Int) =
        CoroutineScope(Dispatchers.Main).launch {
            binding.progressBar.progress = progress
            if (binding.progressBar.progress >= 100) {
                binding.progressBar.visibility = View.GONE
                binding.clPlay.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.VISIBLE
                binding.clPlay.visibility = View.GONE
            }
        }

    private fun onDownSuccess(
        videoContent: VideoContent,
        file: File
    ) = CoroutineScope(Dispatchers.IO).launch {
        if (downloadStatus == DownloadingStatus.CANCELED) {
            canceledOrFailureHandle(videoContent)
            deleteFile(DownloadUtil.downloadFileDir + videoContent.name)
        } else {
            if (file.exists()) {
                handleDownloadSuccessful(videoContent, file)
            }
        }
    }

    private fun canceledOrFailureHandle(videoContent: VideoContent) =
        CoroutineScope(Dispatchers.IO).launch {
            videoContent.progress = "0"
            videoContent.isDownload = false
            downloadStatus = DownloadingStatus.UNDEF
            message.content = videoContent.toStringContent()
            withContext(Dispatchers.Main) {
                binding.root.post {
                    binding.progressBar.progress = 0
                    binding.progressBar.visibility = View.GONE
                    binding.progressBar.isCanceledLoading = false
                    binding.playIV.setImageResource(R.drawable.ic_video_download)
                    binding.tvPeriod.visibility = View.GONE
                    binding.clPlay.visibility = View.VISIBLE
                }
            }
            deleteFile(DownloadUtil.downloadFileDir + videoContent.name)
            onMessageControlEventListener?.onContentUpdate(message.id, videoContent.javaClass.name, videoContent.toStringContent())
        }

    private fun handleDownloadSuccessful(
        videoContent: VideoContent,
        file: File
    ) = CoroutineScope(Dispatchers.IO).launch {
        withContext(Dispatchers.Main) {
            binding.progressBar.progress = 0
            binding.progressBar.visibility = View.GONE
            binding.progressBar.isCanceledLoading = false
            binding.playIV.setImageResource(R.drawable.play)
            binding.tvPeriod.text = getVideoDuration(file.absolutePath)
            binding.tvPeriod.visibility = View.VISIBLE
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val contentUri = Uri.fromFile(file)
            mediaScanIntent.data = contentUri
            binding.root.context.sendBroadcast(mediaScanIntent)
        }
        videoContent.isDownload = true
        videoContent.progress = "100"
        downloadStatus = DownloadingStatus.SUCCESS
        onMessageControlEventListener?.updateReplyMessageWhenVideoDownload(message.id)
        onMessageControlEventListener?.onContentUpdate(message.id, videoContent.javaClass.name, videoContent.toStringContent())
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

    override fun onClick(
        v: View,
        message: MessageEntity
    ) {
        super.onClick(v, message)
        CoroutineScope(Dispatchers.IO).launch {
            val videoContent = message.content() as VideoContent
            val downloadPath = DownloadUtil.downloadFileDir + videoContent.name
            val isFileExists = File(downloadPath).exists() || (!videoContent.android_local_path.isNullOrEmpty() && File(videoContent.android_local_path).exists())
            if (isFileExists) {
                onMessageControlEventListener?.onVideoClick(message)
//                binding.progressBar.setOnFileClickListener(null)
            } else {
                binding.progressBar.setOnFileClickListener {
                    downloadTask?.let {
                        it.task.cancel()
                        downloadStatus = DownloadingStatus.CANCELED
                        canceledOrFailureHandle(videoContent)
                        Toast.makeText(binding.root.context, binding.root.context.getString(R.string.canceled_downloading), Toast.LENGTH_SHORT).show()
                    }
                }
                DownloadUtil.doDownloadVideoFile(
                    videoContent,
                    { progress ->
                        if (downloadStatus == DownloadingStatus.CANCELED) {
                            canceledOrFailureHandle(videoContent)
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
                            canceledOrFailureHandle(videoContent)
                        }
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

    private enum class DownloadingStatus {
        UNDEF,
        SUCCESS,
        CANCELED,
        FAILURE
    }

    init {
        getView(this.binding.root)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(eventMsg: EventMsg<Any>) {
        when (eventMsg.code) {
            MsgConstant.MESSAGE_VIDEO_UPDATE_DOWNLOAD_PROGRESS -> {
                val messageVideoDownloadProgress = eventMsg.data as MessageVideoDownloadProgress
                if (messageVideoDownloadProgress.messageId == message.id) {
                    updateProgress(messageVideoDownloadProgress.progress)
                }
            }
        }
    }

    fun unRegisterEventBus() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }
}
