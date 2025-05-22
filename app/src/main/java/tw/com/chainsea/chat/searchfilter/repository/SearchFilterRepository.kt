package tw.com.chainsea.chat.searchfilter.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import tw.com.chainsea.ce.sdk.bean.AvatarRecord
import tw.com.chainsea.ce.sdk.database.DBManager
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference
import tw.com.chainsea.ce.sdk.reference.UserProfileReference
import tw.com.chainsea.chat.util.SharedPreferenceManager

class SearchFilterRepository(private val sharedPreferenceManager: SharedPreferenceManager) {

    fun getEmployeesAvatarRecord() : Flow<List<AvatarRecord>> {
        return sharedPreferenceManager.getEmployeesAvatarRecord().flowOn(Dispatchers.IO)
    }

    fun getEmployeesTextRecord(): Flow<List<String>> {
        return sharedPreferenceManager.getEmployeesTextRecord().flowOn(Dispatchers.IO)
    }

    fun saveEmployeesTextRecord(records: List<String>) : Flow<Boolean> {
        return sharedPreferenceManager.saveEmployeesTextRecord(records = records).flowOn(Dispatchers.IO)
    }

    fun addEmployeesAvatarRecord(record: AvatarRecord) : Flow<Boolean> {
        return sharedPreferenceManager.addEmployeesAvatarRecord(record).flowOn(Dispatchers.IO)
    }

    fun removeAvatarRecord(roomId: String) = sharedPreferenceManager.removeEmployeesAvatarRecord(roomId).flowOn(Dispatchers.IO)

    fun findUserProfileById(userId: String) = flow {
        emit(
            UserProfileReference.findById(null, userId)
        )
    }.flowOn(Dispatchers.IO)

    fun findChatRoomById(roomId: String) = flow {
        emit(
            ChatRoomReference.getInstance().findById(roomId)
        )
    }.flowOn(Dispatchers.IO)
    fun findCrowdById(id: String) = flow {
        emit(
            DBManager.getInstance().queryGroup(id)
        )
    }.flowOn(Dispatchers.IO)

    fun findServiceNumberById(serviceNumberId: String) = flow {
        emit(
            DBManager.getInstance().queryServiceNumberById(serviceNumberId)
        )
    }.flowOn(Dispatchers.IO)
}
