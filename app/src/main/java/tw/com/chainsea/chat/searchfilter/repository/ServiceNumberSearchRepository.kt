package tw.com.chainsea.chat.searchfilter.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import tw.com.chainsea.ce.sdk.database.DBManager

class ServiceNumberSearchRepository {

    fun queryAllSubscribeServiceNumbers() = flow {
        emit(
            DBManager.getInstance().querySubscribeServiceNumber().filter { !it.serviceOpenType.contains("C") }
        )
    }.flowOn(Dispatchers.IO)

    fun queryAllSubscribeServiceNumbersByName(name: String) = flow {
        emit(
            DBManager.getInstance().querySubscribeServiceNumbersByName(name).filter { !it.serviceOpenType.contains("C") }
        )
    }.flowOn(Dispatchers.IO)
}