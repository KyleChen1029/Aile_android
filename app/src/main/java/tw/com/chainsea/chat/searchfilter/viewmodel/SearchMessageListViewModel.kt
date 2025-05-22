package tw.com.chainsea.chat.searchfilter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.ce.sdk.bean.common.EnableType
import tw.com.chainsea.ce.sdk.database.DBManager
import tw.com.chainsea.ce.sdk.http.ce.model.User

class SearchMessageListViewModel : ViewModel() {
    val sendNavigateToChatRoom = MutableSharedFlow<MessageEntity>()
    val sendUpdatedListResult = MutableSharedFlow<Pair<MutableList<MessageEntity>, String>>()

    fun navigateToChatRoom(item: MessageEntity) =
        viewModelScope.launch {
            sendNavigateToChatRoom.emit(item)
        }

    fun checkMemberIsForbidden(
        list: MutableList<MessageEntity>,
        keyWord: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        list.forEach {
            val user = DBManager.getInstance().queryFriend(it.senderId)
            user?.let { info ->
                it.enable = if (info.status.equals(User.Status.DISABLE)) EnableType.N else EnableType.Y
                it.senderName = user.alias?.ifEmpty { user.nickName }
                it.avatarId = user.avatarId
            }
        }
        list.sortByDescending { it.sendTime }
        sendUpdatedListResult.emit(Pair(list.toMutableList(), keyWord))
    }
}
