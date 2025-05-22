package tw.com.chainsea.chat.searchfilter.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import tw.com.chainsea.ce.sdk.database.DBManager
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference

class ContactPersonClientRepository {

    fun queryCustomers() = flow {
        emit(
            DBManager.getInstance().queryCustomers()
        )
    }.flowOn(Dispatchers.IO)
    fun queryCustomersByName(name: String) = flow {
        emit(
            DBManager.getInstance().queryCustomersByName(name)
        )
    }
    fun queryAllGroupRoom() = flow {
        emit(
            DBManager.getInstance().findAllGroups()
        )
    }.flowOn(Dispatchers.IO)

    fun queryAllGroupRoomByName(name: String) = flow {
        emit(
            DBManager.getInstance().findAllGroupsByName(name)
        )
    }.flowOn(Dispatchers.IO)

    fun queryGroupRoom(roomId: String) = flow {
        emit(
            ChatRoomReference.getInstance().findById(roomId)
        )
    }.flowOn(Dispatchers.IO)

    fun queryUsersByName(name: String, userId: String) = flow {
        emit(
            DBManager.getInstance().queryAllContactsByName(name, userId)
        )
    }.flowOn(Dispatchers.IO)
}