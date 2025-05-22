package tw.com.chainsea.chat.chatroomfilter

import android.app.Application
import android.graphics.Bitmap
import android.util.Patterns
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import tw.com.chainsea.android.common.log.CELog
import tw.com.chainsea.android.common.ui.UiHelper
import tw.com.chainsea.ce.sdk.bean.msg.MessageType
import tw.com.chainsea.ce.sdk.bean.msg.content.ImageContent
import tw.com.chainsea.ce.sdk.bean.msg.content.VideoContent
import tw.com.chainsea.ce.sdk.network.model.common.ApiErrorData
import tw.com.chainsea.ce.sdk.network.model.common.ApiResult
import tw.com.chainsea.ce.sdk.network.model.response.OpenGraphResult
import tw.com.chainsea.ce.sdk.reference.MessageReference
import tw.com.chainsea.chat.util.DownloadUtil
import java.io.File

class ChatRoomFilterRepository {
    /**
     * 從聊天室過濾媒體訊息
     * @param roomId roomId
     * @param sortType DESC, ASC
     * */
    fun getFilterMediaMessage(
        roomId: String,
        sortType: String = "DESC"
    ) = flow {
        emit(ApiResult.Loading(true))

        val mediaMessage = MessageReference.filterMediaMessageByRoomId(roomId, sortType)
        val filterMediaMessage =
            mediaMessage
                .filter {
                    if (it.type == MessageType.IMAGE) {
                        it.content() is ImageContent &&
                            (it.content() as ImageContent).filePath != null &&
                            (it.content() as ImageContent).filePath.isNotEmpty()
                    } else if (it.type == MessageType.VIDEO) {
                        if (it.content() is VideoContent) {
                            val videoContent = it.content() as VideoContent
                            val downloadPath = DownloadUtil.downloadFileDir + it.sendTime + "_" + videoContent.name
                            val downloadFile = File(downloadPath)
                            videoContent.android_local_path?.let { localPath ->
                                if (localPath.isNotEmpty()) {
                                    val localFile = File(videoContent.android_local_path)
                                    if (localFile.exists()) {
                                        true
                                    } else {
                                        downloadFile.exists()
                                    }
                                } else {
                                    downloadFile.exists()
                                }
                            } ?: run { downloadFile.exists() }
                        } else {
                            false
                        }
                    } else {
                        false
                    }
                }.toMutableList()

        emit(ApiResult.Success(filterMediaMessage))
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("filterMediaMessage Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }

    /**
     * 從聊天室過濾連結訊息
     * @param roomId roomId
     * @param sortType DESC, ASC
     * */
    fun getFilterLinkMessage(
        roomId: String,
        sortType: String = "DESC"
    ) = flow {
        emit(ApiResult.Loading(true))
        val textMessageList = MessageReference.filterMessageByRoomId(roomId, sortType, MessageType.TEXT, MessageType.AT)
        val filterList =
            textMessageList
                .filter {
                    extractLinks(it.content).isNotEmpty()
                }.toMutableList()
        emit(ApiResult.Success(filterList))
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("filterLinkMessage Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }

    /**
     * 取得連結內的 metadata 訊息
     * @param application application
     * @param url 需要拿取 metadata 的 url
     * */
    fun getLinkMetadata(
        application: Application,
        url: String
    ) = flow {
        emit(ApiResult.Loading(true))
        val openGraphResult = OpenGraphResult()
        try {
            // 如果只有 www 開頭 會拿不到
            var newUrl = url
            if (!url.startsWith("http") || !url.startsWith("https")) {
                newUrl = "https://$url"
            }
            val response =
                Jsoup
                    .connect(newUrl)
                    .userAgent("Mozilla")
                    .execute()
            val document: Document = response.parse()

            val ogTags = document.select("meta[property^=og:]")
            when {
                ogTags.size > 0 ->
                    ogTags.forEachIndexed { index, _ ->
                        val tag = ogTags[index]
                        val text = tag.attr("property")

                        when (text) {
                            "og:image" -> {
                                openGraphResult.imageUrl = (tag.attr("content"))
                            }

                            "og:title" -> {
                                openGraphResult.title = (tag.attr("content"))
                            }
                        }
                    }
            }

            // Get the title of the website
            val title = document.title()
            openGraphResult.url = url
            if (openGraphResult.title.isEmpty()) {
                openGraphResult.title = title
            }

            if (openGraphResult.imageUrl.isEmpty()) {
                // Get the logo source of the website
                val img: Element = document.select("img").first()
                // Locate the src attribute
                val imgSrc: String = img.absUrl("src")
                // Download image from URL
                if (imgSrc.startsWith("http") || imgSrc.startsWith("https")) {
                    val bitmap =
                        Glide
                            .with(application)
                            .asBitmap()
                            .load(imgSrc)
                            .submit()
                            .get()
                    openGraphResult.image =
                        Bitmap.createScaledBitmap(
                            bitmap,
                            UiHelper.dp2px(application, 70F).toInt(),
                            UiHelper.dp2px(application, 70f).toInt(),
                            false
                        )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        emit(ApiResult.Success(openGraphResult))
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("getLinkMetadata Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }

    /**
     * 從聊天室過濾檔案訊息
     * @param roomId roomId
     * @param sortType DESC, ASC
     * */
    fun getFilterFileMessage(
        roomId: String,
        sortType: String = "DESC"
    ) = flow {
        emit(ApiResult.Loading(true))
        val filterMessageList = MessageReference.filterMessageByRoomId(roomId, sortType, MessageType.FILE)
        emit(ApiResult.Success(filterMessageList))
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("getFilterFileMessage Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
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
}
