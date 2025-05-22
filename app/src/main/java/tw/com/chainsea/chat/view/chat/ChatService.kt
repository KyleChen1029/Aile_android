package tw.com.chainsea.chat.view.chat

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.ce.sdk.bean.CrowdEntity
import tw.com.chainsea.ce.sdk.bean.GroupEntity
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.bean.room.RoomItemRequest
import tw.com.chainsea.ce.sdk.bean.room.RoomItemResponse
import tw.com.chainsea.ce.sdk.http.ce.ApiPath
import tw.com.chainsea.ce.sdk.http.cp.base.BaseResponse
import tw.com.chainsea.ce.sdk.network.model.common.BaseRequest
import tw.com.chainsea.ce.sdk.network.model.common.CommonResponse
import tw.com.chainsea.ce.sdk.network.model.request.AddChatRoomManager
import tw.com.chainsea.ce.sdk.network.model.request.BecomeOwnerRequest
import tw.com.chainsea.ce.sdk.network.model.request.BusinessCardRequest
import tw.com.chainsea.ce.sdk.network.model.request.ChatMemberExitRequest
import tw.com.chainsea.ce.sdk.network.model.request.ChatRoomHomePageRequest
import tw.com.chainsea.ce.sdk.network.model.request.ChatRoomNameUpdateRequest
import tw.com.chainsea.ce.sdk.network.model.request.CheckCommentStatusRequest
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
import tw.com.chainsea.ce.sdk.network.model.request.MessageReadRequest
import tw.com.chainsea.ce.sdk.network.model.request.MessageReadv2Request
import tw.com.chainsea.ce.sdk.network.model.request.MuteRequest
import tw.com.chainsea.ce.sdk.network.model.request.QuickReplyRequest
import tw.com.chainsea.ce.sdk.network.model.request.RobotChatRoomSnatchRequest
import tw.com.chainsea.ce.sdk.network.model.request.SendAtMessageReadRequest
import tw.com.chainsea.ce.sdk.network.model.request.SendAtMessageRequest
import tw.com.chainsea.ce.sdk.network.model.request.SendFacebookCommentRequest
import tw.com.chainsea.ce.sdk.network.model.request.StartConsultRequest
import tw.com.chainsea.ce.sdk.network.model.request.SyncMessageBetweenRequest
import tw.com.chainsea.ce.sdk.network.model.request.SyncMessageRequest
import tw.com.chainsea.ce.sdk.network.model.request.SyncServiceNumberActiveListRequest
import tw.com.chainsea.ce.sdk.network.model.request.TopRoomRequest
import tw.com.chainsea.ce.sdk.network.model.response.CancelAiWarningRequest
import tw.com.chainsea.ce.sdk.network.model.response.ChatRoomHomePageResponse
import tw.com.chainsea.ce.sdk.network.model.response.ChatRoomMemberResponse
import tw.com.chainsea.ce.sdk.network.model.response.CheckCommentResponse
import tw.com.chainsea.ce.sdk.network.model.response.CreateRoomResponse
import tw.com.chainsea.ce.sdk.network.model.response.FromAppointResponse
import tw.com.chainsea.ce.sdk.network.model.response.GetAtChatRoomResponse
import tw.com.chainsea.ce.sdk.network.model.response.RobotServiceResponse
import tw.com.chainsea.ce.sdk.network.model.response.SendFacebookCommentResponse
import tw.com.chainsea.ce.sdk.network.model.response.SyncRoomNormalResponse

interface ChatService {
    /**
     * 聊天室資訊
     * */
    @POST(ApiPath.chatRoomItem)
    suspend fun getRoomItem(
        @Body roomItemRequest: RoomItemRequest
    ): Response<RoomItemResponse>

    /**
     * 抓取聊天室成員(主要是抓成員權限)
     * */
    @POST(ApiPath.syncChatMember)
    suspend fun getMember(
        @Body
        request: GetRoomMemberRequest
    ): Response<CommonResponse<List<ChatRoomMemberResponse>>>

    @POST(ApiPath.syncChatMember)
    fun getMemberForJava(
        @Body
        request: GetRoomMemberRequest
    ): Call<CommonResponse<List<ChatRoomMemberResponse>>>

    /**
     * 成為社團擁有者
     * */
    @POST(ApiPath.becomeOwner)
    suspend fun becomeOwner(
        @Body request: BecomeOwnerRequest
    ): Response<CommonResponse<Any>>

    /**
     *  7days 未讀聊天室資料
     * */
    @POST(ApiPath.syncRoomUnread)
    suspend fun syncRoomUnreadList(
        @Body
        baseRequest: BaseRequest
    ): Response<CommonResponse<List<ChatRoomEntity>>>

    /**
     * 同步所有聊天室資料
     * */
    @POST(ApiPath.syncRoom)
    suspend fun syncRoomList(
        @Body
        baseRequest: BaseRequest
    ): Response<CommonResponse<List<ChatRoomEntity>>>

    /**
     * 多人聊天室創建
     * */
    @POST(ApiPath.chatRoomCreate)
    suspend fun roomCreate(
        @Body createRoomRequest: CreateRoomRequest
    ): Response<CreateRoomResponse>

    /**
     * 建立社團
     * */
    @POST(ApiPath.chatRoomCreate)
    suspend fun crowdRoomCreate(
        @Body body: RequestBody
    ): Response<CreateRoomResponse>

    /**
     * 升級社團
     * */
    @Multipart
    @POST(ApiPath.chatRoomUpgrade)
    suspend fun chatRoomUpgrade(
        @Part args: MultipartBody.Part,
        @Part file: MultipartBody.Part
    ): Response<Any>

    /**
     * /chat/room/homepage
     * */
    @POST(ApiPath.chatRoomHomepage)
    suspend fun chatRoomHomepage(
        @Body createRoomHomePageRequest: ChatRoomHomePageRequest
    ): Response<CrowdEntity>

    /**
     * /chat/room/update
     * */
    @POST(ApiPath.chatRoomUpdate)
    suspend fun chatRoomNameUpdate(
        @Body chatRoomNameUpdateRequest: ChatRoomNameUpdateRequest
    ): Response<CommonResponse<Any>>

    // 一般聊天室
    @POST(ApiPath.syncRoomNormal)
    suspend fun syncRoomNormal(
        @Body
        baseRequest: BaseRequest
    ): Response<CommonResponse<List<SyncRoomNormalResponse>>>

    // 查詢進線中和機器人服務中的聊天室記錄
    @POST(ApiPath.syncServiceNumberActiveList)
    suspend fun syncServiceNumberActiveList(
        @Body request: SyncServiceNumberActiveListRequest
    ): Response<CommonResponse<List<ChatRoomEntity>>>

    // 取得機器人服務中的列表
    @POST(ApiPath.chatRoomRobotServiceList)
    suspend fun getRobotServiceList(
        @Body request: BaseRequest
    ): Response<CommonResponse<List<ChatRoomEntity>>>

    @POST(ApiPath.syncRoomServicenumber)
    suspend fun syncRoomServiceNumber(
        @Body request: BaseRequest
    ): Response<CommonResponse<List<ChatRoomEntity>>>

    @POST(ApiPath.cancelAiWarning)
    suspend fun cancelAiWarning(
        @Body request: CancelAiWarningRequest
    ): Response<Any>

    @POST(ApiPath.messageSend)
    suspend fun sendQuickReply(
        @Body request: QuickReplyRequest
    ): Response<Any>

    @POST(ApiPath.syncGroup)
    suspend fun getGroupList(
        @Body request: BaseRequest
    ): Response<CommonResponse<List<GroupEntity>>>

    @POST(ApiPath.memberList)
    suspend fun getMemberList(
        @Body request: MemberListRequest
    ): Response<CommonResponse<List<UserProfileEntity>>>

    @POST(ApiPath.chatRoomDismiss)
    suspend fun roomDismiss(
        @Body request: DismissRoomRequest
    ): Response<CommonResponse<Any>>

    @POST(ApiPath.chatMemberExit)
    suspend fun chatMemberExit(
        @Body request: ChatMemberExitRequest
    ): Response<CommonResponse<Any>>

    @POST(ApiPath.chatRoomMuteCancel)
    suspend fun cancelMuteRoom(
        @Body request: MuteRequest
    ): Response<Any>

    @POST(ApiPath.chatRoomMute)
    suspend fun muteRoom(
        @Body request: MuteRequest
    ): Response<Any>

    @POST(ApiPath.chatRoomTopCancel)
    suspend fun cancelTopRoom(
        @Body request: TopRoomRequest
    ): Response<Any>

    @POST(ApiPath.chatRoomTop)
    suspend fun topRoom(
        @Body request: TopRoomRequest
    ): Response<Any>

    @POST(ApiPath.chatRoomRecentDelete)
    suspend fun deleteRoom(
        @Body request: DeleteRoomRequest
    ): Response<Any>

    @POST(ApiPath.messageRead)
    suspend fun messageRead(
        @Body request: MessageReadRequest
    ): Response<Any>

    @POST(ApiPath.serviceNumberConsult)
    suspend fun startConsult(
        @Body request: StartConsultRequest
    ): Response<CommonResponse<Any>>

    @POST(ApiPath.chatRoomAt)
    fun sendAtMessage(
        @Body request: SendAtMessageRequest
    ): Call<CommonResponse<Any>>

    @POST(ApiPath.chatRoomAtList)
    suspend fun getAtChatRoom(): Response<GetAtChatRoomResponse>

    @POST(ApiPath.chatRoomAtRead)
    suspend fun sendAtMessageRead(
        @Body request: SendAtMessageReadRequest
    ): Response<CommonResponse<Any>>

    @POST(ApiPath.chatRoomHomepagePics)
    suspend fun getHomepagePics(
        @Body request: ChatRoomHomePageRequest
    ): Response<ChatRoomHomePageResponse>

    @POST(ApiPath.chatMemberDelete)
    suspend fun doDeleteMember(
        @Body request: DeletedChatRoomMember
    ): Response<CommonResponse<Any>>

    @POST(ApiPath.addChatRoomManager)
    suspend fun doAddChatRoomManager(
        @Body request: AddChatRoomManager
    ): Response<CommonResponse<Any>>

    @POST(ApiPath.deleteChatRoomManager)
    suspend fun doDeleteChatRoomManager(
        @Body request: DeleteChatRoomManager
    ): Response<CommonResponse<Any>>

    @POST(ApiPath.messageClean)
    suspend fun doCleanChatRoomMessage(
        @Body request: CleanChatRoomMessage
    ): Response<CommonResponse<Any>>

    @POST(ApiPath.sendFacebookPublicReply)
    fun sendFacebookPublicComment(
        @Body request: SendFacebookCommentRequest
    ): Call<CommonResponse<SendFacebookCommentResponse>>

    @POST(ApiPath.sendFacebookPrivateReply)
    fun sendFacebookPrivateComment(
        @Body request: SendFacebookCommentRequest
    ): Call<CommonResponse<SendFacebookCommentResponse>>

    @POST(ApiPath.checkCommentStatus)
    fun checkCommentStatus(
        @Body request: CheckCommentStatusRequest
    ): Call<CheckCommentResponse>

    @POST(ApiPath.messageList)
    suspend fun getMessageList(
        @Body request: GetMessageListRequest
    ): Response<CommonResponse<List<MessageEntity>>>

    @POST(ApiPath.sendBusinessCard)
    suspend fun doSendBusinessCard(
        @Body request: BusinessCardRequest
    ): Response<CommonResponse<Any>>

    @POST(ApiPath.fromAppoint)
    suspend fun getFromAppoint(
        @Body request: FromAppointRequest
    ): Response<FromAppointResponse>

    @POST(ApiPath.sendBusinessMemberCard)
    suspend fun doSendBusinessMemberCard(
        @Body request: BusinessCardRequest
    ): Response<CommonResponse<Any>>

    @POST(ApiPath.messageItem)
    suspend fun getMessageItem(
        @Body request: MessageItemRequest
    ): Response<MessageEntity>

    @POST(ApiPath.messageItem)
    suspend fun getReplyNearMessageItem(
        @Body request: MessageItemRequest
    ): Response<MessageEntity>

    @POST(ApiPath.syncMessage)
    suspend fun syncMessage(
        @Body request: SyncMessageRequest
    ): Response<CommonResponse<List<MessageEntity>>>

    @POST(ApiPath.messageDelete)
    suspend fun deleteMessage(
        @Body request: DeleteMessageRequest
    ): Response<CommonResponse<Any>>

    @POST(ApiPath.syncMessage)
    suspend fun syncMessageBetweenSequence(
        @Body request: SyncMessageBetweenRequest
    ): Response<CommonResponse<List<MessageEntity>>>

    @POST(ApiPath.chatRoomRobotServiceSnatch)
    suspend fun snatchRobotServicing(
        @Body robotChatRoomSnatchRequest: RobotChatRoomSnatchRequest
    ): Response<RobotServiceResponse>

    @POST(ApiPath.messageReadv2)
    suspend fun messageReadv2(
        @Body request: MessageReadv2Request
    ): Response<BaseResponse>
}
