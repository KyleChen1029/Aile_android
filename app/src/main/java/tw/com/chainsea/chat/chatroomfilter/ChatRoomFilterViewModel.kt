package tw.com.chainsea.chat.chatroomfilter

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Patterns
import androidx.core.content.FileProvider
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.android.common.file.FileHelper
import tw.com.chainsea.android.common.log.CELog
import tw.com.chainsea.ce.sdk.bean.msg.MessageType
import tw.com.chainsea.ce.sdk.bean.msg.content.AtContent
import tw.com.chainsea.ce.sdk.bean.msg.content.FileContent
import tw.com.chainsea.ce.sdk.bean.msg.content.ImageContent
import tw.com.chainsea.ce.sdk.bean.msg.content.TextContent
import tw.com.chainsea.ce.sdk.bean.msg.content.VideoContent
import tw.com.chainsea.ce.sdk.http.ce.base.BaseViewModel
import tw.com.chainsea.ce.sdk.http.ce.base.TokenRepository
import tw.com.chainsea.ce.sdk.network.model.common.ApiResult
import tw.com.chainsea.chat.chatroomfilter.model.FilterFileModel
import tw.com.chainsea.chat.chatroomfilter.model.FilterLinkModel
import tw.com.chainsea.chat.chatroomfilter.model.FilterMediaModel
import tw.com.chainsea.chat.config.BundleKey
import tw.com.chainsea.chat.messagekit.enums.FileType
import tw.com.chainsea.chat.util.DownloadUtil
import tw.com.chainsea.chat.util.IntentUtil
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class ChatRoomFilterViewModel(
    private val application: Application,
    private val tokenRepository: TokenRepository,
    private val chatRoomFilterRepository: ChatRoomFilterRepository
) : BaseViewModel(application, tokenRepository) {
    val filterMediaMessageList = MutableLiveData<MutableList<BaseFilterModel>>()
    val filterLinkMessageList = MutableLiveData<MutableList<BaseFilterModel>>()
    val filterFileMessageList = MutableLiveData<MutableList<BaseFilterModel>>()
    val onWebMetaDataGet = MutableLiveData<FilterLinkModel>()
    val onDownloadSucceed = MutableLiveData<MutableList<Uri>>()
    val onDownloadError = MutableLiveData<Pair<Boolean, Int>>()
    var roomId = ""
    private val handler = Handler(Looper.getMainLooper())

    fun queryChatRoomMessage(sortType: String = "DESC") =
        viewModelScope.launch(Dispatchers.IO) {
            filterMediaMessage(sortType)
            filterLinkMessage(sortType)
            filterFileMessage(sortType)
        }

    /**
     * 從聊天室過濾媒體訊息
     * @param roomId roomId
     * @param sortType DESC, ASC
     * */
    fun filterMediaMessage(sortType: String = "DESC") =
        viewModelScope.launch(Dispatchers.IO) {
            chatRoomFilterRepository.getFilterMediaMessage(roomId, sortType).collect {
                when (it) {
                    is ApiResult.Success -> {
                        filterMediaMessageList.postValue(setDateItem(it.data))
                    }

                    else -> {
                        // nothing
                    }
                }
            }
        }

    /**
     * 從聊天室過濾連結訊息
     * @param roomId roomId
     * @param sortType DESC, ASC
     * */
    fun filterLinkMessage(sortType: String = "DESC") =
        viewModelScope.launch(Dispatchers.IO) {
            chatRoomFilterRepository.getFilterLinkMessage(roomId, sortType).collect {
                when (it) {
                    is ApiResult.Success -> {
                        val completeList = setDateItem(it.data)
                        filterLinkMessageList.postValue(completeList)
                        getLinkMetadata(completeList)
                    }

                    else -> {
                        // nothing
                    }
                }
            }
        }

    /**
     * 取得連結內的 metadata 訊息
     * @param filterLinkList 已經過濾過的連結訊息
     * */
    private suspend fun getLinkMetadata(filterLinkList: MutableList<BaseFilterModel>) {
        filterLinkList.forEach { filterLinkModel ->
            if (filterLinkModel is FilterLinkModel) {
                chatRoomFilterRepository.getLinkMetadata(application, filterLinkModel.url).collect {
                    when (it) {
                        is ApiResult.Success -> {
                            filterLinkModel.title = it.data.title
                            it.data.image?.let {
                                filterLinkModel.image = it
                            } ?: run {
                                filterLinkModel.imageUrl = it.data.imageUrl
                            }
                            onWebMetaDataGet.postValue(filterLinkModel)
                        }

                        else -> {
//                             nothing
                        }
                    }
                }
            }
        }
    }

    /**
     * 從聊天室過濾檔案訊息
     * @param roomId roomId
     * @param sortType DESC, ASC
     * */
    fun filterFileMessage(sortType: String = "DESC") =
        viewModelScope.launch(Dispatchers.IO) {
            chatRoomFilterRepository.getFilterFileMessage(roomId, sortType).collect {
                when (it) {
                    is ApiResult.Success -> {
                        filterFileMessageList.postValue(setDateItem(it.data))
                    }

                    else -> {
                        // nothing
                    }
                }
            }
        }

    private val dateFormat = SimpleDateFormat("yyyy年MM月", Locale.TAIWAN)
    private val itemDateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.TAIWAN)

    /**
     * 設置日期的 item
     * @param messageList 訊息清單
     * */
    private suspend fun setDateItem(
        messageList: MutableList<MessageEntity>,
        sortType: String = "DESC"
    ) = withContext(Dispatchers.Main) {
        val resultList = mutableListOf<BaseFilterModel>()
        var date = ""
        messageList.forEach { messageEntity ->
            val messageDate = dateFormat.format(messageEntity.sendTime)
            if (date != messageDate) {
                date = messageDate
                val timeMediaModel = BaseFilterModel()
                timeMediaModel.date = date
                timeMediaModel.type = MessageType.SYSTEM
                if (!resultList.contains(timeMediaModel)) {
                    resultList.add(timeMediaModel)
                }
            }
            getFilterItem(messageEntity)?.let {
                resultList.add(it)
            }
        }
        return@withContext resultList
    }

    /**
     * 按照需要的 type 取得相關資訊
     * @param messageEntity 訊息
     * */
    private suspend fun getFilterItem(messageEntity: MessageEntity): BaseFilterModel? =
        withContext(Dispatchers.IO) {
            when (messageEntity.type) {
                MessageType.IMAGE -> {
                    val filterMediaModel = FilterMediaModel(messageEntity = messageEntity)
                    val imageContent = messageEntity.content() as ImageContent
                    filterMediaModel.thumbnail =
                        if (imageContent.url.endsWith(".gif")) imageContent.url else imageContent.thumbnailUrl
                    filterMediaModel.type = messageEntity.type!!
                    return@withContext filterMediaModel
                }

                MessageType.VIDEO -> {
                    val filterMediaModel = FilterMediaModel(messageEntity = messageEntity)
                    val videoContent = messageEntity.content() as VideoContent

                    if (videoContent.thumbnailUrl == null || videoContent.thumbnailUrl.isEmpty()) {
                        videoContent.android_local_path?.let {
                            val originLocalFile = File(it)
                            if (originLocalFile.exists()) {
                                filterMediaModel.thumbnail = it
                            } else {
                                val downloadPath = DownloadUtil.downloadFileDir + messageEntity.sendTime + "_" + (messageEntity.content() as VideoContent).name
                                val downloadFile = File(downloadPath)
                                if (downloadFile.exists()) {
                                    filterMediaModel.thumbnail = downloadPath
                                }
                            }
                        } ?: run {
                            val downloadPath = DownloadUtil.downloadFileDir + messageEntity.sendTime + "_" + (messageEntity.content() as VideoContent).name
                            val downloadFile = File(downloadPath)
                            if (downloadFile.exists()) {
                                filterMediaModel.thumbnail = downloadPath
                            }
                        }
                    } else {
                        filterMediaModel.thumbnail = videoContent.thumbnailUrl
                    }
                    filterMediaModel.videoDuration = videoContent.duration
                    filterMediaModel.type = messageEntity.type!!
                    return@withContext filterMediaModel
                }

                MessageType.AT -> {
                    val filterLinkModel = FilterLinkModel()
                    val messageDate = itemDateFormat.format(messageEntity.sendTime)
                    val atContent = messageEntity.content() as AtContent
                    try {
                        val urlContent = (atContent.mentionContents[0].content as TextContent).text
                        val currentUrl = extractLinks(urlContent)
                        filterLinkModel.itemDate = messageDate
                        filterLinkModel.url = currentUrl
                        filterLinkModel.type = messageEntity.type!!
                    } catch (e: Exception) {
                        CELog.e(e.message)
                    }

                    return@withContext filterLinkModel
                }

                MessageType.TEXT -> {
                    val filterLinkModel = FilterLinkModel()
                    val messageDate = itemDateFormat.format(messageEntity.sendTime)
                    val textContent = messageEntity.content() as TextContent
                    val currentUrl = extractLinks(textContent.text)
                    filterLinkModel.itemDate = messageDate
                    filterLinkModel.url = currentUrl
                    filterLinkModel.type = messageEntity.type!!
                    return@withContext filterLinkModel
                }

                MessageType.FILE -> {
                    val filterFileModel = FilterFileModel()
                    val messageDate = itemDateFormat.format(messageEntity.sendTime)
                    val fileContent = messageEntity.content() as FileContent
                    filterFileModel.itemDate = messageDate
                    val fileType =
                        fileContent.name?.let {
                            FileHelper.getFileTyle(it)
                        } ?: run { "" }
                    val drawableType = FileType.of(fileType)
                    filterFileModel.fileIcon = drawableType.drawable
                    filterFileModel.fileUrl = fileContent.url ?: ""
                    filterFileModel.fileName = fileContent.name ?: "Unknown"
                    filterFileModel.type = messageEntity.type!!
                    return@withContext filterFileModel
                }

                else -> {
                    return@withContext null
                }
            }
        }

    /**
     * 從 TextMessage 裡取得 url link
     * @param text 訊息
     * */
    private fun extractLinks(text: String): String {
        val m = Patterns.WEB_URL.matcher(text)
        while (m.find()) {
            val url = m.group()
            return url
        }
        return ""
    }

    /**
     * 下載照片/影片
     * @param isShare 是否是點擊分享
     * @param messageList 照片/影片的訊息
     * */
    fun downloadMedia(
        isShare: Boolean,
        messageList: MutableList<FilterMediaModel>
    ) = viewModelScope.launch(Dispatchers.IO) {
        val totalSize = messageList.size
        var doneSize = 0
        var errorSize = 0
        val uris = ArrayList<Uri>()
        messageList.forEach {
            if (it.type == MessageType.VIDEO) {
                val videoContent = it.messageEntity.content() as VideoContent
                if (videoContent.url.contains("http")) {
                    DownloadUtil.doDownloadFile(videoContent.url, videoContent.name, {
                        val uri =
                            FileProvider.getUriForFile(
                                application,
                                application.packageName + ".fileprovider",
                                it
                            )
                        uris.add(uri)
                        doneSize++
                    }, {
                        errorSize++
                        doneSize++
                    })
                } else {
                    val uri =
                        FileProvider.getUriForFile(
                            application,
                            application.packageName + ".fileprovider",
                            File(videoContent.url)
                        )
                    uris.add(uri)
                    doneSize++
                }
            } else if (it.type == MessageType.IMAGE) {
                val imageContent = it.messageEntity.content() as ImageContent
                DownloadUtil.downloadImageFromUrl(imageContent.url, {
                    val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                    val file = File(it)
                    val uri =
                        FileProvider.getUriForFile(
                            application,
                            application.packageName + ".fileprovider",
                            file
                        )
                    uris.add(uri)
                    mediaScanIntent.setData(uri)
                    application.sendBroadcast(mediaScanIntent)
                    doneSize++
                }, {
                    doneSize++
                    errorSize++
                })
            }
        }
        handler.post(
            object : Runnable {
                override fun run() {
                    if (doneSize == totalSize) {
                        if (errorSize == doneSize) {
                            onDownloadError.postValue(Pair(true, errorSize))
                        } else {
                            if (isShare) {
                                shareFile(uris)
                            }
                            if (errorSize != 0) {
                                onDownloadError.postValue(Pair(false, errorSize))
                            } else {
                                onDownloadSucceed.postValue(uris)
                            }
                        }
                    } else {
                        handler.postDelayed(this, 1000)
                    }
                }
            }
        )
    }

    /**
     * 下載附件
     * @param isShare 是否是點擊分享
     * @param fileList 附件訊息的list
     * */
    fun downloadFile(
        isShare: Boolean,
        fileList: MutableList<FilterFileModel>
    ) = viewModelScope.launch(Dispatchers.IO) {
        val totalSize = fileList.size
        var doneSize = 0
        var errorSize = 0
        val uris = ArrayList<Uri>()
        fileList.forEach {
            if (it.fileUrl.isNotEmpty()) {
                if (it.fileUrl.contains("http")) {
                    DownloadUtil.doDownloadFile(fileUrl = it.fileUrl, fileName = it.fileName, onDownloadSuccess = {
                        val uri =
                            FileProvider.getUriForFile(
                                application,
                                application.packageName + ".fileprovider",
                                it
                            )
                        uris.add(uri)
                        doneSize++
                    }, onDownloadFailed = {
                        doneSize++
                        errorSize++
                    })
                } else {
                    val uri =
                        FileProvider.getUriForFile(
                            application,
                            application.packageName + ".fileprovider",
                            File(it.fileUrl)
                        )
                    uris.add(uri)
                    doneSize++
                }
            } else {
                doneSize++
                errorSize++
            }
        }
        handler.post(
            object : Runnable {
                override fun run() {
                    if (doneSize == totalSize) {
                        if (errorSize == doneSize) {
                            onDownloadError.postValue(Pair(true, errorSize))
                        } else {
                            if (isShare) {
                                shareFile(uris)
                            }
                            if (errorSize != 0) {
                                onDownloadError.postValue(Pair(false, errorSize))
                            } else {
                                onDownloadSucceed.postValue(uris)
                            }
                        }
                    } else {
                        handler.postDelayed(this, 1000)
                    }
                }
            }
        )
    }

    /**
     * 分享照片/影片/附件
     * @param uris 照片/影片的 uri
     * */
    fun shareFile(uris: ArrayList<Uri>) =
        viewModelScope.launch(Dispatchers.IO) {
            val sendIntent = getShareIntent()
            sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            sendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            sendIntent.putExtra(BundleKey.IS_FROM_FILTER.key(), true)
            application.startActivity(sendIntent)
        }

    /**
     * 分享連結
     * @param links 連結的list
     * */
    fun shareLink(links: MutableList<FilterLinkModel>) =
        viewModelScope.launch(Dispatchers.IO) {
            val shareLink = StringBuilder()
            links.forEachIndexed { index, filterLinkModel ->
                shareLink.append(links[index].url)
                if (index < links.size - 1) {
                    shareLink.append("\n")
                }
            }
            IntentUtil.shareText(application, shareLink.toString(), true)
        }

    /**
     * 取得分享需要的 intent
     * */
    private fun getShareIntent(): Intent {
        val sendIntent = Intent()
        sendIntent.setAction(Intent.ACTION_SEND_MULTIPLE)
        sendIntent.setType("*/*")
        return sendIntent
    }

    /**
     * 停止下載 移除 handler
     * */
    fun stopDownload() {
        handler.removeCallbacksAndMessages(null)
    }
}
