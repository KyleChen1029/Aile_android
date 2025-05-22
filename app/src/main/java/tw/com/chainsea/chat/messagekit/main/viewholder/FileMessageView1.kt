package tw.com.chainsea.chat.messagekit.main.viewholder

import android.content.res.ColorStateList
import android.icu.text.DecimalFormat
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.google.common.base.Strings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.android.common.file.FileHelper
import tw.com.chainsea.android.common.log.CELog
import tw.com.chainsea.android.common.text.KeyWordHelper
import tw.com.chainsea.android.common.ui.UiHelper
import tw.com.chainsea.ce.sdk.bean.msg.MessageStatus
import tw.com.chainsea.ce.sdk.bean.msg.content.FileContent
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.event.EventMsg
import tw.com.chainsea.ce.sdk.event.MsgConstant
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.ItemBaseMessageBinding
import tw.com.chainsea.chat.databinding.ItemMessageFileBinding
import tw.com.chainsea.chat.messagekit.enums.FileType
import tw.com.chainsea.chat.messagekit.lib.FileUtil
import tw.com.chainsea.chat.messagekit.main.MessageVideoDownloadProgress
import tw.com.chainsea.chat.messagekit.main.viewholder.base.BaseMessageViewHolder
import tw.com.chainsea.chat.messagekit.main.viewholder.base.OnMessageSlideReply
import tw.com.chainsea.chat.util.DownloadUtil
import tw.com.chainsea.chat.util.DownloadUtil.downloadFileDir
import tw.com.chainsea.chat.util.DownloadingTask
import java.io.File

class FileMessageView1(
    binding: ItemBaseMessageBinding,
    layoutInflater: LayoutInflater,
    chatRoomEntity: ChatRoomEntity,
    onMessageSlideReply: OnMessageSlideReply
) : BaseMessageViewHolder(binding, chatRoomEntity, onMessageSlideReply = onMessageSlideReply) {
    private val fileMessageBinding = ItemMessageFileBinding.inflate(layoutInflater)
    private var newestStatus = DownloadingStatus.UNDEF
    private val downloadTask: DownloadingTask?
        get() {
            return message?.let {
                val fileContent = it.content() as FileContent
                DownloadUtil.getCurrentDownloadTask(fileContent.url)
            }
        }

    override fun onMessageClick() {
        onFileClick()
    }

    override fun bind(message: MessageEntity) {
        super.bind(message)
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        CoroutineScope(Dispatchers.IO).launch {
            setBubbleView(fileMessageBinding.root)

            val fileContent = message.content() as FileContent
            fileContent.name?.let {
                val fileTypeString = FileHelper.getFileTyle(it)
                val fileType = FileType.of(fileTypeString)
                withContext(Dispatchers.Main) {
                    val param = fileMessageBinding.root.layoutParams
                    param?.let {
                        it.width = UiHelper.dip2px(fileMessageBinding.root.context, 165F)
                    }
                    fileMessageBinding.tvFileName.text = fileType.getName()
                    fileMessageBinding.ivFileIcon.setImageResource(fileType.drawable)
                }
            } ?: run {
                fileContent.name = "Unknown"
                withContext(Dispatchers.Main) {
                    fileMessageBinding.tvFileName.visibility = View.INVISIBLE
                    fileMessageBinding.ivFileIcon.setImageResource(R.drawable.file_message_icon_file)
                }
            }
            setStatus()
            withContext(Dispatchers.Main) {
                fileMessageBinding.progressBar.setOnFileClickListener {
                    downloadTask?.let {
                        newestStatus = DownloadingStatus.CANCELED
                        Toast.makeText(fileMessageBinding.root.context, fileMessageBinding.root.context.getString(R.string.canceled_downloading), Toast.LENGTH_SHORT).show()
                        it.task.cancel()
                        fileMessageBinding.progressBar.isCanceledLoading = false
                        canceledOrFailureHandle("", newestStatus)
                    }
                }
            }
        }
    }

    private fun setStatus() =
        CoroutineScope(Dispatchers.IO).launch {
            message?.let {
                val fileContent = it.content() as FileContent
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
                        fileMessageBinding.tvFileStatus.text = if (file.exists()) "已下載" else "未下載"
                        fileMessageBinding.progressBar.progress = 0
                        fileMessageBinding.progressBar.visibility = View.GONE
                        if (downloadTask != null) {
                            fileMessageBinding.tvFileStatus.text = "正在下載"
                            setBackgroundTint(true)
                            if (!Strings.isNullOrEmpty(progress)) {
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

    private fun startDownloadFile(file: File) =
        message?.let {
            setBackgroundTint(true)
            CoroutineScope(Dispatchers.IO).launch {
                val fileContent = it.content() as FileContent
                fileContent.progress = "0"
                fileContent.isDownload = true
                val dir = File(downloadDir)
                if (!dir.exists()) dir.mkdir()
                withContext(Dispatchers.Main) {
                    fileMessageBinding.progressBar.progress = 0
                    fileMessageBinding.progressBar.visibility = View.VISIBLE
                    fileMessageBinding.tvFileStatus.text = "正在下載"
                }

                DownloadUtil.doDownloadFile(
                    fileUrl = fileContent.url,
                    fileName = file.name,
                    fileSize = fileContent.size.toLong(),
                    onDownloadSuccess = { _ ->
                        setBackgroundTint(false)
                        setDownloadSuccessStatus()
                        fileContent.isDownload = true
                        fileContent.progress = "100"
                        it.content = fileContent.toStringContent()
                        onContentUpdate(
                            fileContent.javaClass.name,
                            fileContent.toStringContent()
                        )
                        if (newestStatus == DownloadingStatus.CANCELED) {
                            canceledOrFailureHandle("", newestStatus)
                        }
                        newestStatus = DownloadingStatus.SUCCESS
                    },
                    onDownloadProgress = { progress ->
                        it.content = fileContent.toStringContent()
                        EventBus.getDefault().post(
                            EventMsg(
                                MsgConstant.MESSAGE_VIDEO_UPDATE_DOWNLOAD_PROGRESS,
                                MessageVideoDownloadProgress(
                                    progress,
                                    it.id!!
                                )
                            )
                        )
                    },
                    onDownloadFailed = { errorMsg ->
                        newestStatus = DownloadingStatus.FAILURE
                        canceledOrFailureHandle(
                            errorMsg,
                            DownloadingStatus.FAILURE
                        )
                    }
                )
            }
        }

    private fun setDownloadSuccessStatus() =
        CoroutineScope(Dispatchers.Main).launch {
            fileMessageBinding.progressBar.progress = 0
            fileMessageBinding.progressBar.visibility = View.GONE
            fileMessageBinding.tvFileStatus.text = "已下載"
            fileMessageBinding.progressBar.isCanceledLoading = false
            setBackgroundTint(false)
        }

    private fun canceledOrFailureHandle(
        errorMsg: String?,
        status: DownloadingStatus
    ) = CoroutineScope(Dispatchers.IO).launch {
        setBackgroundTint(false)
        message?.let { message ->
            val fileContent = message.content() as FileContent
            fileContent.progress = null
            fileContent.isDownload = false
            message.content = fileContent.toStringContent()
            withContext(Dispatchers.Main) {
                fileMessageBinding.progressBar.progress = 0
                fileMessageBinding.progressBar.visibility = View.GONE
                if (status == DownloadingStatus.CANCELED) {
                    fileMessageBinding.tvFileStatus.text = "未下載"
                } else {
                    fileMessageBinding.tvFileStatus.text = "下載失敗"
                }
            }
            onContentUpdate(fileContent.javaClass.name, fileContent.toStringContent())
            try {
                downloadTask?.let {
                    val downloadFile = File(it.path)
                    downloadFile.delete()
                }
                CELog.e("下載檔案失敗，刪除檔案成功")
            } catch (e: Exception) {
                CELog.e("下載檔案失敗，刪除檔案失敗", errorMsg)
            }
        }
    }

    private fun setBackgroundTint(isDownload: Boolean) =
        CoroutineScope(Dispatchers.Main).launch {
            if (isDownload) {
                val tint =
                    ColorStateList.valueOf(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.loading_background
                        )
                    )
                ViewCompat.setBackgroundTintList(fileMessageBinding.root, tint)
            } else {
                ViewCompat.setBackgroundTintList(fileMessageBinding.root, null)
            }
        }

    private fun onFileClick() =
        CoroutineScope(Dispatchers.IO).launch {
            message?.let { message ->
                val fileContent = message.content() as FileContent
                val file =
                    if (!Strings.isNullOrEmpty(fileContent.android_local_path)) {
                        File(fileContent.android_local_path)
                    } else if (fileContent.size == 0) {
                        null
                    } else {
                        File("${DownloadUtil.downloadFileDir}${fileContent.name}")
                    }
                file?.let {
                    try {
                        if (it.exists()) {
                            FileUtil.openFile(file.absolutePath, fileMessageBinding.root.context)
                        } else {
                            startDownloadFile(file)
                        }
                    } catch (e: Exception) {
                        CELog.e("download file failed. reason = " + e.message)
                        message.status = MessageStatus.RECEIVED
                    }
                }
            }
        }

    private fun updateProgress(progress: Int) =
        CoroutineScope(Dispatchers.Main).launch {
            fileMessageBinding.progressBar.progress = progress
            if (fileMessageBinding.progressBar.progress >= 100) {
                fileMessageBinding.progressBar.visibility = View.GONE
                setDownloadSuccessStatus()
            } else {
                fileMessageBinding.progressBar.visibility = View.VISIBLE
            }
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
        }
    }

    fun clearEventBus() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }
}

private enum class DownloadingStatus {
    UNDEF,
    SUCCESS,
    CANCELED,
    FAILURE
}
