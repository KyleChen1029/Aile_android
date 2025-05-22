package tw.com.chainsea.chat.mainpage.repository

import com.google.common.collect.Lists
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.database.DBManager
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference
import tw.com.chainsea.ce.sdk.reference.MessageReference


class MainPageRepository {

    fun getMemberDetail(list: List<String>) = flow {
        val userList: MutableList<UserProfileEntity> = Lists.newArrayList()
        list.forEach {
            userList.add(DBManager.getInstance().queryFriend(it))
        }
        emit(userList)
    }.flowOn(Dispatchers.IO)

    fun getChatRoomEntity(id: String) = flow {
        emit(
            ChatRoomReference.getInstance().findById(id)
        )
    }.flowOn(Dispatchers.IO)

    fun updateChatRoomName(name: String, roomId: String) = flow {
        emit(
            ChatRoomReference.getInstance().updateChatRoomNameById(roomId, name)
        )
    }.flowOn(Dispatchers.IO)

    fun deleteMessageByRoomId(roomId: String) = flow {
        emit(
            MessageReference.deleteMessageByRoomId(roomId)
        )
    }.flowOn(Dispatchers.IO)

    fun deleteLastMessageByRoomId(roomId: String) = flow {
        emit(
            ChatRoomReference.getInstance().deleteChatRoomLastMsg(roomId)
        )
    }.flowOn(Dispatchers.IO)

    fun saveChatRoomEntity(entity: ChatRoomEntity) = flow {
        emit(
            ChatRoomReference.getInstance().save(entity)
        )
    }.flowOn(Dispatchers.IO)

}
