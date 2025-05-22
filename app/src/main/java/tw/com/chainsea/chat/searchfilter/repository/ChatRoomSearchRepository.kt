package tw.com.chainsea.chat.searchfilter.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType
import tw.com.chainsea.ce.sdk.database.DBManager
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference

class ChatRoomSearchRepository {

    fun getAllChatRoomEntities(ownerId: String, chatRoomTypeList: List<ChatRoomType>) = flow {
        emit(
            ChatRoomReference.getInstance().findAllChatRoomsByType(
                null,
                ownerId,
                chatRoomTypeList,
                true,
                false,
                true
            )
        )
    }.flowOn(Dispatchers.IO)

//    fun getAllChatRoomEntitiesByKeyWord(ownerId: String, chatRoomTypeList: List<ChatRoomType>, keyWord: String) = flow {
//        emit(
//            ChatRoomReference.getInstance().findAllChatRoomsByTypeAndKeyWord(
//                null,
//                ownerId,
//                chatRoomTypeList,
//                keyWord,
//                true,
//                false,
//                true
//            )
//        )
//    }.flowOn(Dispatchers.IO)

    fun findRoomById(roomId: String) = flow {
        emit(
            ChatRoomReference.getInstance().findById(roomId)
        )
    }.flowOn(Dispatchers.IO)

//    fun queryCustomerInfo(ownerId: String) = flow {
//        emit(
//            DBManager.getInstance().queryCustomer(ownerId)
//        )
//    }.flowOn(Dispatchers.IO)

    fun queryFriend(userId: String) = flow {
        emit(
            DBManager.getInstance().queryFriend(userId)
        )
    }.flowOn(Dispatchers.IO)

    fun queryServiceNumberChatRoomByTypeAndName(ownerId: String, chatRoomTypeList: List<ChatRoomType>, keyWord: String) = flow {
        emit(
            ChatRoomReference.getInstance().findAllChatRoomsByTypeAndName(
                null,
                ownerId,
                chatRoomTypeList,
                keyWord,
                true,
                false,
                true
            )
        )
    }.flowOn(Dispatchers.IO)

    fun queryUserProfileFromMemberIds(memberIds: List<String>) = flow {
        emit(
            DBManager.getInstance().queryMembersFromUser(memberIds)
        )
    }.flowOn(Dispatchers.IO)

    fun queryChatRoomByKeyWord(userId: String, keyWord: String) = flow {
        emit(
            ChatRoomReference.getInstance().findAllChatRoomsByKeyword(userId, keyWord)
        )
    }.flowOn(Dispatchers.IO)
}