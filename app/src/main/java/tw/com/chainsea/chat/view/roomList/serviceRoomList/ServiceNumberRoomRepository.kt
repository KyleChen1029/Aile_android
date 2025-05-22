package tw.com.chainsea.chat.view.roomList.serviceRoomList

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import tw.com.chainsea.android.common.log.CELog
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.database.DBContract.REFRESH_TIME_SOURCE
import tw.com.chainsea.ce.sdk.database.DBManager
import tw.com.chainsea.ce.sdk.http.ce.request.SyncContactRequest
import tw.com.chainsea.ce.sdk.network.model.common.ApiErrorData
import tw.com.chainsea.ce.sdk.network.model.common.ApiResult
import tw.com.chainsea.ce.sdk.network.model.request.AiServicingChatRoomRequest
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference
import tw.com.chainsea.ce.sdk.reference.ServiceNumberReference

class ServiceNumberRoomRepository(private val serviceNumberRoomService: ServiceNumberRoomService) {


    /**
     * 從資料庫取得自己在的服務號
     * @param selfUserId 自己的 userId
     * */
    fun getServiceNumberFromDb(selfUserId: String) = flow {
        emit(ApiResult.Loading(true))
        val serviceNumberList = ServiceNumberReference.findSelfServiceNumber(selfUserId)
        val selfServiceNumberList = serviceNumberList.filter { serviceNumber ->
            serviceNumber.memberItems.any { it.id == selfUserId }
        }.sortedBy { it.name }.toMutableList()
        emit(ApiResult.Success(selfServiceNumberList))
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("getServiceNumberFromDb Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }

    fun getServiceNumberChatRoomFromDb(selfUserId: String) = flow {
        emit(ApiResult.Loading(true))
        val serviceNumberChatRoomList = ChatRoomReference.getInstance().queryOnlineServiceRoom(selfUserId)
        emit(ApiResult.Success(serviceNumberChatRoomList))
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("getServiceNumberChatRoomFromDbByGroup Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }

    fun getServiceNumberChatRoomFromDbByTime() = flow {
        emit(ApiResult.Loading(true))
        val serviceNumberChatRoomList = ChatRoomReference.getInstance().queryOnlineServiceRoomByTime(0, 10)
        emit(ApiResult.Success(serviceNumberChatRoomList))
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("getServiceNumberChatRoomFromDbByTime Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }

    /**
     * 從API取得AI服務聊天室
     * */
    fun getRobotServicingCatRoomFromServer() = flow {
        emit(ApiResult.Loading(true))
        val refreshTime = DBManager.getInstance().getLastRefreshTime(REFRESH_TIME_SOURCE.CHAT_ROOM_ROBOT_SERVICE_LIST)
        val response = serviceNumberRoomService.getRobotServicingCatRoomFromServer(AiServicingChatRoomRequest(refreshTime, SyncContactRequest.PAGE_SIZE))
        response.body()?.let {
            DBManager.getInstance().updateOrInsertApiInfoField(REFRESH_TIME_SOURCE.CHAT_ROOM_ROBOT_SERVICE_LIST, it.refreshTime)
            it._header_?.let {  header ->
                if (header.success!!) {
                    it.items?.let {
                        ChatRoomReference.getInstance().save(it)
                    }
                    emit(ApiResult.Success(it.items))
                }
            }
            it.hasNextPage?.let { has ->
                emit(ApiResult.NextPage<MutableList<ChatRoomEntity>>(has))
            }?: run {
                emit(ApiResult.NextPage<MutableList<ChatRoomEntity>>(false))
            }
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("getRobotChatServiceList Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }
}