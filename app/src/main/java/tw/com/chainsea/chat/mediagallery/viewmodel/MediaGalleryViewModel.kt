package tw.com.chainsea.chat.mediagallery.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEnum
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType
import tw.com.chainsea.ce.sdk.database.DBManager
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.mainpage.repository.MainPageRepository
import tw.com.chainsea.chat.searchfilter.repository.SearchFilterSharedRepository
class MediaGalleryViewModel(
    private val application: Application,
    private val searchFilterSharedRepository: SearchFilterSharedRepository,
    private val mainPageRepository: MainPageRepository
) : ViewModel() {

    private val mediaList = mutableListOf<MessageEntity>()
    val sendMediaPosition = MutableSharedFlow<Pair<Int, List<MessageEntity>>>()
    val sendTouchedScreenEvent = MutableSharedFlow<Unit>()
    val chatRoomId = MutableStateFlow("")
    val roomName = MutableStateFlow("")
    val roomType = MutableStateFlow(ChatRoomEnum.NORMAL_ROOM)

    fun initData(messageEntity: MessageEntity?, isFromFilterPage: Boolean = false, sort: String = "DESC") = viewModelScope.launch(Dispatchers.IO) {
        messageEntity?.let {
            it.roomId?.let { roomId ->
                val flow = if (isFromFilterPage) {
                    searchFilterSharedRepository.findAllMessageByRoomId(roomId, sort)
                } else {
                    searchFilterSharedRepository.findAllMediaMessageByRoomId(roomId)
                }
                flow.onEach { list ->
                    mediaList.clear()
                    mediaList.addAll(list)
                    sendMediaPosition.emit(Pair(mediaList.indexOf(it), mediaList))
                }.collect()
            }
        }
    }
    fun getChatRoomInfo(roomId: String?) = viewModelScope.launch(Dispatchers.IO) {
        roomId?.let {
            mainPageRepository.getChatRoomEntity(roomId).onEach { e ->
                e?.let {
                    val name = if (it.type == ChatRoomType.person) {
                        val entity = DBManager.getInstance().queryFriend(it.ownerId)
                        entity?.nickName ?:run { it.name }
                    } else {
                        it.name
                    }
                    roomName.value = if(roomType.value == ChatRoomEnum.NORMAL_ROOM) name else application.getString(R.string.text_quote_to_someone, it.name)
                }
            }.collect()
        }
    }
}