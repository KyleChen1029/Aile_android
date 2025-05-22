package tw.com.chainsea.chat.view.chat

import com.google.common.collect.Lists
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.android.common.log.CELog
import tw.com.chainsea.ce.sdk.bean.GroupEntity
import tw.com.chainsea.ce.sdk.bean.label.Label
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType
import tw.com.chainsea.ce.sdk.bean.room.RoomItemRequest
import tw.com.chainsea.ce.sdk.bean.room.RoomItemResponse
import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberStatus
import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberType
import tw.com.chainsea.ce.sdk.config.AppConfig
import tw.com.chainsea.ce.sdk.database.DBContract.REFRESH_TIME_SOURCE
import tw.com.chainsea.ce.sdk.database.DBContract.REFRESH_TIME_SOURCE.CHAT_ROBOT_SERVICE_LIST
import tw.com.chainsea.ce.sdk.database.DBContract.REFRESH_TIME_SOURCE.SYNC_ROOM_NORMAL
import tw.com.chainsea.ce.sdk.database.DBContract.REFRESH_TIME_SOURCE.SYNC_ROOM_UNREAD
import tw.com.chainsea.ce.sdk.database.DBContract.REFRESH_TIME_SOURCE.SYNC_ROOM_UNREAD_ONCE
import tw.com.chainsea.ce.sdk.database.DBManager
import tw.com.chainsea.ce.sdk.http.ce.request.SyncContactRequest
import tw.com.chainsea.ce.sdk.network.model.common.ApiErrorData
import tw.com.chainsea.ce.sdk.network.model.common.ApiResult
import tw.com.chainsea.ce.sdk.network.model.common.BaseRequest
import tw.com.chainsea.ce.sdk.network.model.common.CommonResponse
import tw.com.chainsea.ce.sdk.network.model.common.ErrorCode
import tw.com.chainsea.ce.sdk.network.model.common.RequestHeader
import tw.com.chainsea.ce.sdk.network.model.request.AddChatRoomManager
import tw.com.chainsea.ce.sdk.network.model.request.BecomeOwnerRequest
import tw.com.chainsea.ce.sdk.network.model.request.BusinessCardRequest
import tw.com.chainsea.ce.sdk.network.model.request.ChatMemberExitRequest
import tw.com.chainsea.ce.sdk.network.model.request.ChatRoomHomePageRequest
import tw.com.chainsea.ce.sdk.network.model.request.ChatRoomNameUpdateDetail
import tw.com.chainsea.ce.sdk.network.model.request.ChatRoomNameUpdateRequest
import tw.com.chainsea.ce.sdk.network.model.request.CleanChatRoomMessage
import tw.com.chainsea.ce.sdk.network.model.request.CreateRoomRequest
import tw.com.chainsea.ce.sdk.network.model.request.DeleteChatRoomManager
import tw.com.chainsea.ce.sdk.network.model.request.DeleteMessageRequest
import tw.com.chainsea.ce.sdk.network.model.request.DeleteRoomRequest
import tw.com.chainsea.ce.sdk.network.model.request.DeletedChatRoomMember
import tw.com.chainsea.ce.sdk.network.model.request.DismissRoomRequest
import tw.com.chainsea.ce.sdk.network.model.request.FromAppointRequest
import tw.com.chainsea.ce.sdk.network.model.request.GetMessageListRequest
import tw.com.chainsea.ce.sdk.network.model.request.GetRoomMemberRequest
import tw.com.chainsea.ce.sdk.network.model.request.MemberListRequest
import tw.com.chainsea.ce.sdk.network.model.request.MessageItemRequest
import tw.com.chainsea.ce.sdk.network.model.request.MessageReadv2Request
import tw.com.chainsea.ce.sdk.network.model.request.MuteRequest
import tw.com.chainsea.ce.sdk.network.model.request.QuickReplyRequest
import tw.com.chainsea.ce.sdk.network.model.request.RobotChatRoomSnatchRequest
import tw.com.chainsea.ce.sdk.network.model.request.SendAtMessageReadRequest
import tw.com.chainsea.ce.sdk.network.model.request.StartConsultRequest
import tw.com.chainsea.ce.sdk.network.model.request.SyncMessageBetweenRequest
import tw.com.chainsea.ce.sdk.network.model.request.SyncMessageRequest
import tw.com.chainsea.ce.sdk.network.model.request.SyncServiceNumberActiveListRequest
import tw.com.chainsea.ce.sdk.network.model.request.TopRoomRequest
import tw.com.chainsea.ce.sdk.network.model.response.CancelAiWarningRequest
import tw.com.chainsea.ce.sdk.network.model.response.ChatRoomMemberResponse
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference
import tw.com.chainsea.ce.sdk.reference.MessageReference
import tw.com.chainsea.ce.sdk.service.ChatRoomService
import tw.com.chainsea.ce.sdk.service.type.ChatRoomSource
import tw.com.chainsea.chat.view.base.PreloadStepEnum

class ChatRepository(
    private val chatService: ChatService
) {
    /**
     * 取得Chat Room 資訊
     * */
    fun getRoomItem(
        roomId: String,
        userId: String
    ) = flow {
        emit(ApiResult.Loading(true))
        val response = chatService.getRoomItem(RoomItemRequest(roomId))
        response.body()?.let { body ->
            body.let { chatRoomEntity ->
                if (chatRoomEntity._header_.success!!) {
                    if (ServiceNumberStatus.OFF_LINE == chatRoomEntity.serviceNumberStatus) {
                        chatRoomEntity.serviceNumberAgentId = ""
                    }
                    setChatType(chatRoomEntity, userId)
                    emit(ApiResult.Success(chatRoomEntity))
                } else {
                    chatRoomEntity._header_?.let {
                        it.errorCode?.let {
//                            if(error == ErrorCode.RoomNotExist.type || error == ErrorCode.ChatMemberInvalid.type) {
                            ChatRoomReference.getInstance().deleteById(roomId)
//                            }
                        }
                        emit(
                            ApiResult.Failure<RoomItemResponse>(
                                ApiErrorData(
                                    it.errorMessage!!,
                                    it.errorCode!!
                                )
                            )
                        )
                    }
                }
            }
        }
        emit(ApiResult.Loading<RoomItemResponse>(false))
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("getRoomItem Error", e)
        emit(ApiResult.Failure(ApiErrorData(e.message!!, e.stackTrace[0].toString())))
    }

    /**
     * 取得聊天室成員&權限
     * */
    fun getChatMember(roomId: String) =
        flow {
            emit(ApiResult.Loading(true))
            val response = chatService.getMember(GetRoomMemberRequest(roomId))
            response.body()?.let {
                it._header_?.let { header ->
                    if (header.success!!) {
                        it.items?.let {
                            // 多人聊天室需要 chatMember，過濾刪除的之後按照 joinTime 排序
                            val filterList = it.filter { !it.deleted }.sortedBy { it.joinTime }
                            ChatRoomReference.getInstance().updateChatRoomMember(roomId, filterList)
                            emit(ApiResult.Success(filterList))
                        }
                    } else {
                        emit(ApiResult.Failure<List<ChatRoomMemberResponse>>(ApiErrorData(header.errorMessage!!, header.errorCode!!)))
                    }
                }
            }
            emit(ApiResult.Loading<List<ChatRoomMemberResponse>>(false))
        }.flowOn(Dispatchers.IO).catch { e ->
            CELog.e("getChatMember Error", e)
            emit(ApiResult.Failure(ApiErrorData(e.message!!, e.stackTrace[0].toString())))
        }

    /**
     * 用於撈取全部 RoomList 時，有多人聊天室的話，撈取成員資料以便組合頭圖和聊天室名稱
     * */
    fun getDiscussRoomChatMember(
        roomId: String,
        roomType: ChatRoomType = ChatRoomType.undef,
        isCustomName: Boolean
    ) = channelFlow {
        getChatMember(roomId).collect {
            when (it) {
                is ApiResult.Failure -> {
                    send(
                        ApiResult.Failure<ApiErrorData>(
                            ApiErrorData(
                                it.errorMessage.errorMessage,
                                it.errorMessage.errorCode
                            )
                        )
                    )
                }

                is ApiResult.Success -> {
                    val chatRoomMemberList = it.data
                    // 判斷是否有自行設定聊天室名稱
                    if (!isCustomName && roomType == ChatRoomType.discuss) {
                        val name = StringBuilder()
                        val subList =
                            if (chatRoomMemberList.size >= 4) {
                                chatRoomMemberList.subList(0, 4)
                            } else {
                                chatRoomMemberList
                            }
                        // 多人聊天室名稱
                        subList.forEachIndexed { index, chatRoomMemberResponse ->
                            val userProfile =
                                DBManager
                                    .getInstance()
                                    .queryFriend(chatRoomMemberResponse.memberId)
                            userProfile?.let {
                                name.append(it.nickName)
                                // 取前四個
                                if (index < subList.size - 1) {
                                    name.append(",")
                                } else {
                                    return@forEachIndexed
                                }
                            }
                        }
                        val status =
                            ChatRoomReference
                                .getInstance()
                                .updateChatRoomTitle(roomId, name.toString())
                        send(ApiResult.Success(status))
                    } else {
                        send(ApiResult.Success(true))
                    }
                }

                else -> {
                    send(it)
                }
            }
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("getDiscussRoomChatMember Error", e)
        emit(
            ApiResult.Failure(
                ApiErrorData(
                    e.message!!,
                    e.stackTrace[0].toString()
                )
            )
        )
    }

    /**
     * 成為社團擁有者
     * */
    fun becomeOwner(roomId: String) =
        flow {
            emit(ApiResult.Loading(true))
            val response = chatService.becomeOwner(BecomeOwnerRequest(roomId))
            response.body()?.let {
                emit(ApiResult.Success(it))
            }
        }.flowOn(Dispatchers.IO).catch { e ->
            CELog.e("becomeOwner Error", e)
            emit(ApiResult.Failure(ApiErrorData(e.message!!, e.stackTrace[0].toString())))
        }

    /**
     * sync/room/unread
     */
    fun getRoomUnReadList(
        userId: String,
        refreshTime: Long
    ) = flow {
        emit(ApiResult.Loading(true))
        val response = chatService.syncRoomUnreadList(BaseRequest(refreshTime))
        response.body()?.let {
            it.items?.let { list ->
                list.forEach { entity ->
                    // 聊天室或服務號分類
                    setChatType(entity, userId)
                    entity.lastMessage?.let { message ->
                        entity.updateTime = message.sendTime
                    } ?: run {
                        entity.updateTime = 0L
                    }
                }
                // 把標記被刪除的濾掉, 然後再DB砍掉
                val validEntities: List<ChatRoomEntity> = ChatRoomService.filterByDelete(list)
                // replace local data base
                val saveStatus = ChatRoomReference.getInstance().syncSave(validEntities)

                if (saveStatus) {
                    DBManager
                        .getInstance()
                        .updateOrInsertApiInfoField(SYNC_ROOM_UNREAD, it.refreshTime)
                }
                emit(ApiResult.SaveStatus<Boolean>(saveStatus))
            }

            if (it.hasNextPage == false) {
                // save the time of right now
                DBManager
                    .getInstance()
                    .updateOrInsertApiInfoField(SYNC_ROOM_UNREAD_ONCE, System.currentTimeMillis())
            }

            it.hasNextPage?.let { has ->
                emit(ApiResult.NextPage<List<ChatRoomEntity>>(has))
            } ?: run {
                emit(ApiResult.NextPage<List<ChatRoomEntity>>(false))
            }
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("getRoomUnReadList Error", e)
        emit(ApiResult.Failure(ApiErrorData(e.message!!, e.stackTrace[0].toString())))
    }

    /**
     * sync/room
     */
    fun getAllChatRoomList(
        userId: String,
        type: PreloadStepEnum
    ) = flow {
        emit(ApiResult.Loading(true))
        val response =
            chatService.syncRoomList(
                BaseRequest(
                    DBManager.getInstance().getLastRefreshTime(ChatRoomSource.SERVICE.name)
                )
            )
        response.body()?.let {
            it._header_?.let { header ->
                if (header.success == true) {
                    it.items?.let { list ->
                        list.forEach { entity ->
                            // 聊天室或服務號分類
                            setChatType(entity, userId)
                            entity.lastMessage?.let { message ->
                                // 將最後一則訊息時間更新至update time方便排序
                                entity.updateTime = message.sendTime
                            } ?: run {
                                entity.updateTime = 0L
                            }
                        }
                        // 把標記被刪除的濾掉, 然後再DB砍掉
                        val validEntities = ChatRoomService.filterByDelete(list)
                        // replace local data base
                        // 為了解決火箭頁同步之後以及聊天列表訊息的排序問題，將lastMessage sendTime與updateTime的方法進行獨立的處理，此做法能確保訊息在聊天過程能依據原有的權重和updateTime邏輯
                        val saveStatus =
                            if (type != PreloadStepEnum.SYNC_DONE) {
                                ChatRoomReference.getInstance().syncSave(validEntities)
                            } else {
                                ChatRoomReference.getInstance().save(validEntities)
                            }
                        // 更新refresh time
                        if (saveStatus) {
                            DBManager
                                .getInstance()
                                .updateOrInsertApiInfoField(ChatRoomSource.SERVICE.name, it.refreshTime)
                        }
                    }
                    it.hasNextPage?.let { has ->
                        emit(ApiResult.NextPage<List<ChatRoomEntity>>(has))
                    } ?: run {
                        emit(ApiResult.NextPage<List<ChatRoomEntity>>(false))
                    }
                } else {
                    emit(ApiResult.Failure<List<ChatRoomEntity>>(ApiErrorData(header.errorMessage!!, header.errorCode!!)))
                }
            }
//            it.items?.let { list ->
//                list.forEach { entity ->
//                    //聊天室或服務號分類
//                    setChatType(entity, userId)
//                    entity.lastMessage?.let { message -> //將最後一則訊息時間更新至update time方便排序
//                        entity.updateTime = message.sendTime
//                    }?:run {
//                        entity.updateTime = 0L
//                    }
//                }
//                // 把標記被刪除的濾掉, 然後再DB砍掉
//                val validEntities = ChatRoomService.filterByDelete(list)
//                // replace local data base
//                //為了解決火箭頁同步之後以及聊天列表訊息的排序問題，將lastMessage sendTime與updateTime的方法進行獨立的處理，此做法能確保訊息在聊天過程能依據原有的權重和updateTime邏輯
//                val saveStatus =
//                    if(type != PreloadStepEnum.SYNC_DONE)
//                        ChatRoomReference.getInstance().syncSave(validEntities)
//                    else
//                        ChatRoomReference.getInstance().save(validEntities)
//                //更新refresh time
//                if (saveStatus) {
//                    DBManager.getInstance()
//                        .updateOrInsertApiInfoField(ChatRoomSource.SERVICE.name, it.refreshTime)
//                }
//            }
//            it.hasNextPage?.let { has ->
//                emit(ApiResult.NextPage<List<ChatRoomEntity>>(has))
//            } ?: run {
//                emit(ApiResult.NextPage<List<ChatRoomEntity>>(false))
//            }
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("getAllChatRoomList Error", e)
        emit(ApiResult.Failure(ApiErrorData(e.message!!, e.stackTrace[0].toString())))
    }

    // 聊天室&服務號分類
    private fun setChatType(
        chatRoomEntity: ChatRoomEntity,
        userId: String
    ) {
        // 聊天室或服務號分類
        if (ChatRoomType.serviceMember == chatRoomEntity.type) {
            if (ServiceNumberType.BOSS == chatRoomEntity.serviceNumberType && userId == chatRoomEntity.serviceNumberOwnerId) { // 服務號成員聊天室且是商務號擁有者
                chatRoomEntity.listClassify = ChatRoomSource.MAIN
            } else {
                chatRoomEntity.listClassify = ChatRoomSource.SERVICE
            }
        } else if (ChatRoomType.services == chatRoomEntity.type) {
            if (chatRoomEntity.ownerId == userId) { // 我自己進線的服務號聊天室，服務號並且是擁有者
                chatRoomEntity.type = ChatRoomType.subscribe
                chatRoomEntity.listClassify = ChatRoomSource.MAIN
            } else if (ServiceNumberType.BOSS == chatRoomEntity.serviceNumberType && userId == chatRoomEntity.serviceNumberOwnerId) { // 我的商務號聊天室
                chatRoomEntity.listClassify = ChatRoomSource.MAIN
            } else if (chatRoomEntity.provisionalIds.contains(userId)) {
                chatRoomEntity.listClassify = ChatRoomSource.MAIN // 臨時成員聊天室顯示在一般聊天列表
            } else {
                chatRoomEntity.listClassify = ChatRoomSource.SERVICE
            }
        } else {
            chatRoomEntity.listClassify = ChatRoomSource.MAIN
        }
    }

    /**
     * room/create
     */
    fun doCreateRoom(
        ids: List<String>,
        type: ChatRoomType,
        roomName: String = "",
        isCustomName: Boolean = false
    ) = flow {
        emit(ApiResult.Loading(true))
        val response =
            chatService.roomCreate(
                CreateRoomRequest(
                    ids,
                    type.name,
                    "",
                    roomName,
                    isCustomName
                )
            )
        response.body()?.let {
            it._header_?.let { header ->
                header.status?.let { status ->
                    if (status == "0000") {
                        emit(ApiResult.Success(it.id))
                    }
                }
            }
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("doCreateRoom Error", e)
        emit(ApiResult.Failure(ApiErrorData(e.message!!, e.stackTrace[0].toString())))
    }

    /**
     * chat/room/update
     */
    fun doUpdateRoomName(
        roomId: String,
        roomName: String
    ) = flow {
        emit(ApiResult.Loading(true))
        val response =
            chatService.chatRoomNameUpdate(
                ChatRoomNameUpdateRequest(
                    ChatRoomNameUpdateDetail(roomName),
                    roomId
                )
            )
        response.body()?.let {
            it._header_?.let { header ->
                header.status?.let { status ->
                    if (status == "0000") {
                        emit(ApiResult.Success(it))
                    }
                }
            }
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("doUpdateRoomName Error", e)
        emit(ApiResult.Failure(ApiErrorData(e.message!!, e.stackTrace[0].toString())))
    }

    /**
     * 取得聊天室列表
     *
     * @param refreshTime 上一次更新時間
     * */
    fun syncRoomNormal(refreshTime: Long) =
        flow {
            emit(ApiResult.Loading(true))
            val response = chatService.syncRoomNormal(BaseRequest(refreshTime = refreshTime))
            response.body()?.let {
                it.items?.forEach { syncRoomNormalResponse ->
                    ChatRoomReference.getInstance().save(syncRoomNormalResponse)
                }

                it.hasNextPage?.let {
                    emit(ApiResult.NextPage<Boolean>(it))
                }
                DBManager.getInstance().updateOrInsertApiInfoField(SYNC_ROOM_NORMAL, it.refreshTime)
                emit(ApiResult.Success(it.items))
            }
        }.flowOn(Dispatchers.IO).catch { e ->
            CELog.e("syncRoomNormal Error", e)
            e.message?.let {
                emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
            } ?: run {
                emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
            }
        }

    /**
     * 取得"進線中"和"機器人服務中"的聊天室紀錄
     *
     * @param serviceNumberIds 服務號 ID
     * @param refreshTime 上一次更新時間
     * @param userId 自己的 user ID
     * */
    fun syncServiceNumberActiveList(
        serviceNumberIds: List<String> = Lists.newArrayList(),
        refreshTime: Long,
        userId: String
    ) = flow {
        emit(ApiResult.Loading(true))
        val response =
            chatService.syncServiceNumberActiveList(
                SyncServiceNumberActiveListRequest(
                    serviceNumberIds,
                    refreshTime
                )
            )
        response.body()?.let {
            it.items?.let { serviceNumberRoomList ->
                serviceNumberRoomList.forEach { serviceNumberRoom ->
                    setChatType(serviceNumberRoom, userId)
                    val status = ChatRoomReference.getInstance().saveChatRoomFromSync(serviceNumberRoom)
                    emit(ApiResult.SaveStatus<Boolean>(status))
                }
            }
            DBManager.getInstance().updateOrInsertApiInfoField(
                REFRESH_TIME_SOURCE.SYNC_SERVICE_NUMBER_ACTIVE_LIST,
                it.refreshTime
            )
            // 取得下一頁資料
            it.hasNextPage?.let { has ->
                emit(ApiResult.NextPage<List<Label>>(has))
            } ?: run {
                emit(ApiResult.NextPage<List<Label>>(false))
            }
            emit(ApiResult.Success(it))
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("syncServiceNumberActiveList Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }

    /**
     * 取得機器人服務中的列表
     *
     * @param refreshTime 上一次更新時間
     * */
    fun getRobotServiceList(refreshTime: Long) =
        flow {
            emit(ApiResult.Loading(true))
            val response = chatService.getRobotServiceList(BaseRequest(refreshTime))
            response.body()?.let {
                it.items?.let { roomList ->
                    roomList.forEach { room ->
                        val status = ChatRoomReference.getInstance().syncSave(room)
                        emit(ApiResult.SaveStatus<Boolean>(status))
                    }
                }

                DBManager
                    .getInstance()
                    .updateOrInsertApiInfoField(CHAT_ROBOT_SERVICE_LIST, it.refreshTime)
                // 取得下一頁資料
                it.hasNextPage?.let { has ->
                    emit(ApiResult.NextPage<List<Label>>(has))
                } ?: run {
                    emit(ApiResult.NextPage<List<Label>>(false))
                }
                emit(ApiResult.Success(it))
            }
        }.flowOn(Dispatchers.IO).catch { e ->
            CELog.e("getRobotServiceList Error", e)
            e.message?.let {
                emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
            } ?: run {
                emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
            }
        }

    fun syncRoomServiceNumber(
        refreshTime: Long,
        selfUserId: String
    ) = flow {
        emit(ApiResult.Loading(true))
        val response = chatService.syncRoomServiceNumber(BaseRequest(refreshTime))
        response.body()?.let {
            it.items?.let { roomList ->
                roomList.forEach { entity ->
                    setChatType(entity, selfUserId)
                    val status = ChatRoomReference.getInstance().saveChatRoomFromSync(entity)
                    emit(ApiResult.SaveStatus<Boolean>(status))
                }
                DBManager
                    .getInstance()
                    .updateOrInsertApiInfoField(REFRESH_TIME_SOURCE.SYNC_ROOM_SERVICE_NUMBER, it.refreshTime)
            }
            // 取得下一頁資料
            it.hasNextPage?.let { has ->
                emit(ApiResult.NextPage<List<Label>>(has))
            } ?: run {
                emit(ApiResult.NextPage<List<Label>>(false))
            }
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("syncRoomServiceNumber Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }

    fun cancelAiWarning(roomId: String) =
        flow {
            emit(ApiResult.Loading(true))
            val response = chatService.cancelAiWarning(CancelAiWarningRequest(roomId))
            response.body()?.let {
                emit(ApiResult.Success(it))
            }
            emit(ApiResult.Loading(false))
        }.flowOn(Dispatchers.IO).catch { e ->
            CELog.e("cancelAiWarning Error", e)
            e.message?.let {
                emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
            } ?: run {
                emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
            }
        }

    /**
     * 發送 QuickReply
     * @param roomId 聊天室 id
     * @param type QuickReply 類型
     * @param content QuickReply 內容
     * */
    fun sendQuickReply(
        roomId: String,
        type: String,
        content: String
    ) = flow {
        emit(ApiResult.Loading(true))
        val response = chatService.sendQuickReply(QuickReplyRequest(roomId, type, content))
        response.body()?.let {
            emit(ApiResult.Success(it))
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("sendQuickReply Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }

    // 聊天室主頁
    fun getHomePage(roomId: String) =
        flow {
            emit(ApiResult.Loading(true))
            val response = chatService.chatRoomHomepage(ChatRoomHomePageRequest(roomId))
            response.body()?.let {
                emit(ApiResult.Success(it))
            }
        }.flowOn(Dispatchers.IO).catch { e ->
            CELog.e("getHomePage Error", e)
            e.message?.let {
                emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
            } ?: run {
                emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
            }
        }

    //
    fun getMemberList(roomId: String) =
        flow {
            emit(ApiResult.Loading(true))
            val response = chatService.getMemberList(MemberListRequest(roomId))
            response.body()?.let {
                emit(ApiResult.Success(it.items))
            }
        }.flowOn(Dispatchers.IO).catch { e ->
            CELog.e("getMemberList Error", e)
            e.message?.let {
                emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
            } ?: run {
                emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
            }
        }

    fun roomDismiss(roomId: String) =
        flow {
            emit(ApiResult.Loading(true))
            val response = chatService.roomDismiss(DismissRoomRequest(roomId))
            response.body()?.let {
                if (it._header_!!.success!!) {
                    emit(ApiResult.Success(it))
                }
            }
        }.flowOn(Dispatchers.IO).catch { e ->
            CELog.e("roomDismiss Error", e)
            e.message?.let {
                emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
            } ?: run {
                emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
            }
        }

    fun chatMemberExit(
        roomId: String,
        ownerId: String
    ) = flow {
        emit(ApiResult.Loading(true))
        val response = chatService.chatMemberExit(ChatMemberExitRequest(roomId, ownerId))
        response.body()?.let {
            if (it._header_!!.success!!) {
                emit(ApiResult.Success(it))
            }
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("chatMemberExit Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }

    fun cancelRoomMute(roomId: String) =
        flow {
            emit(ApiResult.Loading(true))
            val response = chatService.cancelMuteRoom(MuteRequest(roomId))
            response.body()?.let {
                ChatRoomReference.getInstance().updateMuteById(roomId, false)
                emit(ApiResult.Success(it))
            }
        }.flowOn(Dispatchers.IO).catch { e ->
            CELog.e("cancelRoomMute Error", e)
            e.message?.let {
                emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
            } ?: run {
                emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
            }
        }

    fun muteRoom(roomId: String) =
        flow {
            emit(ApiResult.Loading(true))
            val response = chatService.muteRoom(MuteRequest(roomId))
            response.body()?.let {
                ChatRoomReference.getInstance().updateMuteById(roomId, true)
                emit(ApiResult.Success(it))
            }
        }.flowOn(Dispatchers.IO).catch { e ->
            CELog.e("muteRoom Error", e)
            e.message?.let {
                emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
            } ?: run {
                emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
            }
        }

    fun cancelRoomTop(roomId: String) =
        flow {
            emit(ApiResult.Loading(true))
            val response = chatService.cancelTopRoom(TopRoomRequest(roomId))
            response.body()?.let {
                ChatRoomReference.getInstance().updateTopAndTopTimeById(roomId, false, 0L)
                emit(ApiResult.Success(it))
            }
        }.flowOn(Dispatchers.IO).catch { e ->
            CELog.e("cancelRoomTop Error", e)
            e.message?.let {
                emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
            } ?: run {
                emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
            }
        }

    fun topRoom(roomId: String) =
        flow {
            emit(ApiResult.Loading(true))
            val response = chatService.topRoom(TopRoomRequest(roomId))
            response.body()?.let {
                ChatRoomReference.getInstance().updateTopAndTopTimeById(roomId, true, System.currentTimeMillis())
                emit(ApiResult.Success(it))
            }
        }.flowOn(Dispatchers.IO).catch { e ->
            CELog.e("topRoom Error", e)
            e.message?.let {
                emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
            } ?: run {
                emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
            }
        }

    fun deleteRoom(roomId: String) =
        flow {
            emit(ApiResult.Loading(true))
            val response = chatService.deleteRoom(DeleteRoomRequest(roomId))
            response.body()?.let {
                val status = DBManager.getInstance().deleteRoomListItem(roomId)
                if (status) {
                    emit(ApiResult.Success(it))
                }
            }
        }.flowOn(Dispatchers.IO).catch { e ->
            CELog.e("deleteRoom Error", e)
            e.message?.let {
                emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
            } ?: run {
                emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
            }
        }

    private val newList: MutableList<GroupEntity> = Lists.newArrayList()

    fun getAllGroupList(refreshTime: Long) =
        flow {
            emit(ApiResult.Loading(true))
            val response = chatService.getGroupList(BaseRequest(refreshTime))
            response.body()?.let {
                it.items?.let { list ->
                    list.forEach { entity ->
                        if (!entity.deleted && !entity.member_deleted) {
                            DBManager.getInstance().saveGroupInfo(entity)
                            newList.add(entity)
                        }
                    }
                }
                DBManager.getInstance().updateOrInsertApiInfoField(REFRESH_TIME_SOURCE.SYNC_GROUP, it.refreshTime)
                it.hasNextPage?.let { has ->
                    emit(ApiResult.NextPage<List<GroupEntity>>(has))
                    if (!has) {
                        val oldList = newList.map { list -> list }
                        emit(ApiResult.Success(oldList))
                        newList.clear()
                    }
                } ?: run {
                    emit(ApiResult.NextPage<List<GroupEntity>>(false))
                }
            }
        }.flowOn(Dispatchers.IO).catch { e ->
            CELog.e("getAllGroupList Error", e)
            e.message?.let {
                emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
            } ?: run {
                emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
            }
        }

    fun startConsult(
        srcRoomId: String,
        consultRoomId: String
    ) = flow {
        emit(ApiResult.Loading(true))
        val response = chatService.startConsult(StartConsultRequest(srcRoomId, consultRoomId))
        response.body()?.let {
            it._header_?.let { header ->
                if (header.success!!) {
                    emit(ApiResult.Success(it))
                } else {
                    emit(ApiResult.Failure<CommonResponse<Any>>(ApiErrorData(header.errorMessage!!, header.errorCode!!)))
                }
            }
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("startConsult Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }

    fun getChatRoomEntity(id: String) =
        flow {
            emit(
                ChatRoomReference.getInstance().findById(id)
            )
        }.flowOn(Dispatchers.IO)

    fun clearIsAtMeFlag(roomId: String) =
        flow {
            emit(ChatRoomReference.getInstance().updateIsAtMeFlag(roomId, false))
        }.flowOn(Dispatchers.IO)

    /**
     * 取得所有有 at 自己的聊天室
     * */
    fun getAtChatRoom() =
        flow {
            emit(ApiResult.Loading(true))
            val response = chatService.getAtChatRoom()
            response.body()?.let {
                it._header_?.let { header ->
                    if (header.success!!) {
                        emit(ApiResult.Success(it.roomIds))
                    }
                }
            }
        }.flowOn(Dispatchers.IO).catch { e ->
            CELog.e("getAtChatRoom Error", e)
            e.message?.let {
                emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
            } ?: run {
                emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
            }
        }

    /**
     * 社團聊天室主頁背景圖片查看
     * */
    fun getCrowdHomePagePics(roomId: String) =
        flow {
            emit(ApiResult.Loading(true))
            val response = chatService.getHomepagePics(ChatRoomHomePageRequest(roomId))
            response.body()?.let {
                it.roomPics?.let { data ->
                    emit(ApiResult.Success(data))
                }
            }
        }.flowOn(Dispatchers.IO).catch { e ->
            CELog.e("messageRead Error", e)
            e.message?.let {
                emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
            } ?: run {
                emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
            }
        }

    /**
     * 社團聊天室主頁刪除成員
     * */
    fun doDeletedMember(
        roomId: String,
        userIds: List<String>
    ) = flow {
        emit(ApiResult.Loading(true))
        val response = chatService.doDeleteMember(DeletedChatRoomMember(roomId, userIds))
        response.body()?.let {
            it._header_?.let { header ->
                header.status?.let { status ->
                    emit(ApiResult.Success(status == "0000"))
                }
            }
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("doDeletedMember Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }

    /**
     * 社團聊天室主頁轉移擁有者
     * */
    fun doModifyChatRoomOwner(
        roomId: String,
        userId: String
    ) = flow {
        emit(ApiResult.Loading(true))
        val response =
            chatService.chatRoomNameUpdate(
                ChatRoomNameUpdateRequest(
                    ChatRoomNameUpdateDetail(null, userId),
                    roomId
                )
            )
        response.body()?.let {
            it._header_?.let { header ->
                header.status?.let { status ->
                    emit(ApiResult.Success(status == "0000"))
                }
            }
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("doModifyChatRoomOwner Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }

    /**
     * 社團聊天室主頁委派管理權
     * */
    fun doAddChatRoomManager(
        roomId: String,
        userIds: List<String>
    ) = flow {
        emit(ApiResult.Loading(true))
        val response = chatService.doAddChatRoomManager(AddChatRoomManager(roomId, userIds))
        response.body()?.let {
            it._header_?.let { header ->
                header.status?.let { status ->
                    emit(ApiResult.Success(status == "0000"))
                }
            }
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("doAddChatRoomManager Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }

    /**
     * 社團聊天室主頁取消管理權
     * */
    fun doDeleteChatRoomManager(
        roomId: String,
        userIds: List<String>
    ) = flow {
        emit(ApiResult.Loading(true))
        val response = chatService.doDeleteChatRoomManager(DeleteChatRoomManager(roomId, userIds))
        response.body()?.let {
            it._header_?.let { header ->
                header.status?.let { status ->
                    emit(ApiResult.Success(status == "0000"))
                }
            }
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("doDeleteChatRoomManager Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }

    /**
     * 清除聊天紀錄
     * */
    fun doCleanChatRoomMessage(roomId: String) =
        flow {
            emit(ApiResult.Loading(true))
            val response = chatService.doCleanChatRoomMessage(CleanChatRoomMessage(roomId))
            response.body()?.let {
                it._header_?.let { header ->
                    header.status?.let { status ->
                        emit(ApiResult.Success(status == "0000"))
                    }
                }
            }
        }.flowOn(Dispatchers.IO).catch { e ->
            CELog.e("doCleanChatRoomMessage Error", e)
            e.message?.let {
                emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
            } ?: run {
                emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
            }
        }

    fun sendAtMessageRead(roomId: String) =
        flow {
            emit(ApiResult.Loading(true))
            val response = chatService.sendAtMessageRead(SendAtMessageReadRequest(roomId))
            response.body()?.let {
                val status = ChatRoomReference.getInstance().updateIsAtMeFlag(roomId, false)
                emit(ApiResult.Success(status))
            }
        }.flowOn(Dispatchers.IO).catch { e ->
            CELog.e("sendAtMessageRead Error", e)
            e.message?.let {
                emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
            } ?: run {
                emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
            }
        }

    fun getMessageList(
        roomId: String,
        lastMessageId: String
    ) = flow {
        emit(ApiResult.Loading(true))
        val response = chatService.getMessageList(GetMessageListRequest(roomId = roomId, lastMessageId = lastMessageId))
        response.body()?.let {
            it._header_?.let { header ->
                if (header.success!!) {
                    it.items?.let { message ->
                        message.forEach {
                            MessageReference.save(roomId, it)
                        }
                    }
                    emit(ApiResult.Success(it.items))
                } else {
                    emit(ApiResult.Success(mutableListOf<MessageEntity>()))
                }
            }
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("getMessageList Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }

    /**
     * 發送電子名片
     * */
    fun doSendBusinessCard(
        roomId: String,
        channelType: String
    ) = flow {
        emit(ApiResult.Loading(true))
        val response = chatService.doSendBusinessCard(BusinessCardRequest(roomId, channelType))
        response.body()?.let {
            it._header_?.let { header ->
                header.status?.let { status ->
                    emit(ApiResult.Success(status == "0000"))
                }
            }
        } ?: run {
            emit(ApiResult.Success(false))
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("doSendBusinessCard Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }

    /**
     * 取得目前服務號渠道
     * */
    fun getFromAppoint(roomId: String) =
        flow {
            emit(ApiResult.Loading(true))
            val response = chatService.getFromAppoint(FromAppointRequest(roomId))
            response.body()?.let {
                emit(ApiResult.Success(it))
                it._header_?.let { header ->
                    header.success?.let { success ->
                        if (!success) {
                            if (header.errorCode == ErrorCode.RoomNotExist.type || header.errorCode == ErrorCode.ChatMemberInvalid.type) {
                                ChatRoomReference.getInstance().deleteById(roomId)
                            }
                        }
                    }
                }
            }
        }.flowOn(Dispatchers.IO).catch { e ->
            CELog.e("doSendBusinessCard Error", e)
            e.message?.let {
                emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
            } ?: run {
                emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
            }
        }

    /**
     * 發送企業會員卡片
     * */
    fun doSendBusinessMemberCard(
        roomId: String,
        channelType: String
    ) = flow {
        emit(ApiResult.Loading(true))
        val response = chatService.doSendBusinessMemberCard(BusinessCardRequest(roomId, channelType))
        response.body()?.let {
            it.sendSuccess?.let { sendSuccess ->
                emit(ApiResult.Success(sendSuccess))
            } ?: run {
                it._header_?.let { header ->
                    header.errorCode?.let { code ->
                        if (code == "Ce.bindAile.hadBind") {
                            // 已加入過企業會員
                            emit(ApiResult.Success(header))
                        } else {
                            emit(ApiResult.Success(false))
                        }
                    }
                }
            }
        } ?: run {
            emit(ApiResult.Success(false))
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("doSendBusinessMemberCard Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }

    fun syncMessage(
        roomId: String,
        sequence: Int,
        sort: String = "desc",
        pageSize: Int = 20
    ) = flow {
        emit(ApiResult.Loading(true))
        val response = chatService.syncMessage(SyncMessageRequest(roomId, sequence, sort, pageSize))
        response.body()?.let {
            it._header_?.let { header ->
                if (header.success!!) {
                    it.items?.let { messageList ->
                        messageList.forEach { message ->
                            message.nearMessageId?.let {
                                getNearMessageItem(roomId, it)
                            }
                            MessageReference.save(roomId, message)
                        }
                    }
                    emit(ApiResult.Success(it.items))
                }
            }
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("syncMessage Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }

    fun syncMessageBetweenSequence(
        roomId: String,
        previousSequence: Int,
        lastSequence: Int
    ) = flow {
        emit(ApiResult.Loading(true))
        val response = chatService.syncMessageBetweenSequence(SyncMessageBetweenRequest(roomId, previousSequence, lastSequence))
        response.body()?.let {
            it._header_?.let { header ->
                if (header.success!!) {
                    it.items?.let { messageList ->
                        messageList.forEach { message ->
                            runBlocking {
                                message.nearMessageId?.let { nearMessageId ->
                                    getMessageItem(roomId, nearMessageId).collect {
                                        when (it) {
                                            is ApiResult.Success -> {
                                                val nearMessage = MessageReference.findById(nearMessageId)
                                                nearMessage?.let {
                                                    message.nearMessageType = it.type
                                                    message.nearMessageContent = it.content
                                                    message.nearMessageSenderId = it.senderId
                                                    MessageReference.save(roomId, message)
                                                }
                                            }

                                            else -> {
                                                // nothing
                                            }
                                        }
                                    }
                                } ?: run {
                                    MessageReference.save(roomId, message)
                                }
                            }
                        }
                    }
                    if (it.hasNextPage == true) {
                        emit(ApiResult.NextPage(true))
                    } else {
                        emit(ApiResult.Success(it))
                    }
                }
            }
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("syncMessageBetweenSequence Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }

    fun deleteMessage(
        roomId: String,
        messageId: String
    ) = flow {
        emit(ApiResult.Loading(true))
        val response = chatService.deleteMessage(DeleteMessageRequest(roomId, Lists.newArrayList(messageId)))
        response.body()?.let {
            it._header_?.let { header ->
                if (header.success == true) {
                    emit(ApiResult.Success(header))
                } else {
                    emit(ApiResult.Failure<CommonResponse<Any>>(ApiErrorData(header.errorMessage!!, header.errorCode!!)))
                }
            }
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("deleteMessage Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }

    private suspend fun getNearMessageItem(
        roomId: String,
        messageId: String
    ) {
        withContext(Dispatchers.IO) {
            try {
                val response = chatService.getReplyNearMessageItem(MessageItemRequest(messageId))
                response.body()?.let {
                    MessageReference.save(roomId, it)
                }
            } catch (e: Exception) {
                CELog.e(e.message)
            }
        }
    }

    fun getMessageItem(
        roomId: String,
        messageId: String
    ) = flow {
        emit(ApiResult.Loading(true))
        val response = chatService.getMessageItem(MessageItemRequest(messageId))
        response.body()?.let {
            val status = MessageReference.save(roomId, it)
            if (status) {
                emit(ApiResult.Success(it))
            }
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("getMessageItem Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }

    fun snatchRobot(roomId: String) =
        flow {
            emit(ApiResult.Loading(true))
            val response =
                chatService.snatchRobotServicing(
                    RobotChatRoomSnatchRequest(
                        SyncContactRequest.PAGE_SIZE,
                        0L,
                        RequestHeader(
                            AppConfig.tokenForNewAPI,
                            AppConfig.LANGUAGE
                        ),
                        roomId
                    )
                )
            response.body()?.let {
                emit(ApiResult.Success(it))
            }
        }.flowOn(Dispatchers.IO).catch { e ->
            CELog.e("snatchRobot Error", e)
            e.message?.let {
                emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
            } ?: run {
                emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
            }
        }

    fun getMessageBetweenSequence(
        roomId: String,
        previousSequence: Int,
        lastSequence: Int
    ) = flow {
        emit(ApiResult.Loading(true))
        val result = DBManager.getInstance().getMessageBetweenSequence(roomId, previousSequence, lastSequence)
        emit(ApiResult.Success(result))
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("getMessageBetweenSequence Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }

    fun getSearchMoreMessage(
        roomId: String,
        sequence: Int
    ) = flow {
        emit(ApiResult.Loading(true))
        val entities =
            MessageReference.findRoomMessageList(
                roomId,
                sequence,
                MessageReference.Sort.ASC,
                200
            )
        emit(ApiResult.Success(entities))
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("getSearchMoreMessage Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }

    /**
     * 訊息已讀
     * @param roomId 需要已讀的聊天室 id
     * */
    fun messageRead(
        roomId: String
    ) = flow {
        emit(ApiResult.Loading(true))
        val response = chatService.messageReadv2(MessageReadv2Request(roomId))
        response.body()?.let {
            it._header_?.let { header ->
                DBManager.getInstance().setChatRoomListItemUnreadNum(roomId, 0)
                MessageReference.updateReadFlagByRoomId(roomId)
                emit(ApiResult.Success(header.success == true))
            } ?: run {
                emit(ApiResult.Failure<Boolean>(ApiErrorData("", "")))
            }
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("messageRead Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }

    fun getMessageListFromIds(
        roomId: String,
        messageIds: List<String>
    ) = flow {
        emit(ApiResult.Loading(true))
        val localEntities = MessageReference.findByIdsAndRoomId(null, messageIds.toTypedArray(), roomId)
        emit(ApiResult.Success(localEntities))
    }.flowOn(Dispatchers.IO).catch { e ->
        CELog.e("getMessageListFromIds Error", e)
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }

//    fun getMessageItem(roomId: String, messageId: String) = CoroutineScope(Dispatchers.IO).async {
//        try {
//            val response = chatService.getMessageItem(MessageItemRequest(messageId))
//            response.body()?.let {
//                MessageReference.save(roomId, it)
//            }
//        } catch (e: Exception) {
//            CELog.e(e.message)
//        }
//    }
}
