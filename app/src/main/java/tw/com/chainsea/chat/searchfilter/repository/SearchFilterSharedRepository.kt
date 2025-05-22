package tw.com.chainsea.chat.searchfilter.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import tw.com.chainsea.ce.sdk.bean.msg.MessageType
import tw.com.chainsea.ce.sdk.bean.msg.content.ImageContent
import tw.com.chainsea.ce.sdk.bean.msg.content.VideoContent
import tw.com.chainsea.ce.sdk.bean.parameter.Sort
import tw.com.chainsea.ce.sdk.database.DBManager
import tw.com.chainsea.ce.sdk.reference.MessageReference
import tw.com.chainsea.chat.util.DownloadUtil
import java.io.File

class SearchFilterSharedRepository {
    fun getAllMessageEntities(types: List<MessageType>) = flow {
        emit(
            MessageReference.findByType(null, types)
        )
    }.flowOn(Dispatchers.IO)

    fun queryFriend(id: String) = flow {
        emit(
            DBManager.getInstance().queryFriend(id)
        )
    }.flowOn(Dispatchers.IO)

    fun getAllMessageEntitiesByKeyWord(types: List<MessageType>, keyWord: String) = flow {
        emit(
            MessageReference.findAllMessagesByTypeAndKeyWord(types, keyWord)
        )
    }.flowOn(Dispatchers.IO)

    fun getAllMessageEntitiesByKeyWordForServiceNumber(types: List<MessageType>, keyWord: String) = flow {
        emit(
            MessageReference.findAllMessagesByTypeAndKeyWordForServiceNumber(types, keyWord)
        )
    }.flowOn(Dispatchers.IO)

    fun findAllMediaMessageByRoomId(roomId: String) = flow {
        emit(
            MessageReference.findAllMediaMessageByRoomId(roomId)
        )
    }.flowOn(Dispatchers.IO)

    fun findAllMessageByRoomId(roomId: String, sort: String) = flow {
       val mediaMessage = MessageReference.filterMediaMessageByRoomId(roomId, sort)

        val filterMediaMessage = mediaMessage.filter {
            if (it.type == MessageType.IMAGE) {
                it.content() is ImageContent
                        && (it.content() as ImageContent).filePath != null
                        && (it.content() as ImageContent).filePath.isNotEmpty()
            } else if (it.type == MessageType.VIDEO) {
                if (it.content() is VideoContent) {
                    val videoContent = it.content() as VideoContent
                    val downloadPath = DownloadUtil.downloadFileDir + it.sendTime + "_" + videoContent.name
                    videoContent.android_local_path?.let { localPath ->
                        if (localPath.isNotEmpty()) {
                            val localFile = File(videoContent.android_local_path)
                            if (localFile.exists()) {
                                true
                            } else {
                                File(downloadPath).exists()
                            }
                        } else {
                            File(downloadPath).exists()
                        }
                    } ?: run { File(downloadPath).exists() }
                } else {
                    false
                }
            } else {
                true
            }

        }.toMutableList()

        emit(filterMediaMessage)
    }.flowOn(Dispatchers.IO)
}