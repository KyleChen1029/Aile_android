package tw.com.chainsea.chat.network.contact

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import tw.com.chainsea.ce.sdk.bean.CrowdEntity
import tw.com.chainsea.ce.sdk.bean.CustomerEntity
import tw.com.chainsea.ce.sdk.bean.ServiceNum
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity
import tw.com.chainsea.ce.sdk.bean.label.Label
import tw.com.chainsea.ce.sdk.bean.label.LabelRequest
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.http.ce.ApiPath
import tw.com.chainsea.ce.sdk.http.ce.request.AddContactFriendRequest
import tw.com.chainsea.ce.sdk.http.ce.response.AddFriendResponse
import tw.com.chainsea.ce.sdk.http.ce.response.AiffListResponse
import tw.com.chainsea.ce.sdk.http.ce.response.SyncServicenumberRsp
import tw.com.chainsea.ce.sdk.network.model.common.BaseRequest
import tw.com.chainsea.ce.sdk.network.model.common.CommonResponse
import tw.com.chainsea.ce.sdk.network.model.request.AddressBookCustomFriendInfoRequest
import tw.com.chainsea.ce.sdk.network.model.request.ServiceNumberRoomItemRequest
import tw.com.chainsea.ce.sdk.network.model.request.UserItemRequest
import tw.com.chainsea.ce.sdk.network.model.response.ServiceNumberRoomItemResponse
import tw.com.chainsea.ce.sdk.network.services.SelfProfileService
import tw.com.chainsea.chat.view.chat.ChatService

interface ContactPersonService : SelfProfileService, ChatService {

    @POST(ApiPath.labelItem)
    suspend fun getCollection(@Body labelRequest: LabelRequest): Response<Label>

    @POST(ApiPath.syncLabel)
    suspend fun getLabels(@Body baseRequest: BaseRequest): Response<CommonResponse<List<Label>>>

    @POST(ApiPath.syncSubscribeServicenumber)
    suspend fun getSubscribeServiceNumberList(): Response<CommonResponse<List<ServiceNum>>>

    @POST(ApiPath.chatRoomList)
    suspend fun getGroupRoomList(): Response<CommonResponse<List<CrowdEntity>>>

    @POST(ApiPath.syncEmployee)
    suspend fun getEmployeeList(@Body baseRequest: BaseRequest): Response<CommonResponse<List<UserProfileEntity>>>

    @POST(ApiPath.serviceNumberContactList)
    suspend fun getServiceNumberContactList(@Body baseRequest: BaseRequest): Response<CommonResponse<List<CustomerEntity>>>

    @POST(ApiPath.labelDelete)
    suspend fun deleteLabel(@Body labelRequest: LabelRequest): Response<CommonResponse<String>>

    @POST(ApiPath.addressBookAdd)
    suspend fun addContactFriend(@Body addContactFriend: AddContactFriendRequest): Response<AddFriendResponse>

    @POST(ApiPath.addressBookSync)
    suspend fun syncFriends(): Response<CommonResponse<List<UserProfileEntity>>>

    @POST(ApiPath.aiffList)
    suspend fun getAiffList(): Response<AiffListResponse>

    @POST(ApiPath.syncContact)
    suspend fun getContactList(@Body baseRequest: BaseRequest): Response<CommonResponse<List<CustomerEntity>>>

    @POST(ApiPath.syncServiceNumber)
    suspend fun syncServiceNumber(@Body request: BaseRequest): Response<SyncServicenumberRsp>

    @POST(ApiPath.userItem)
    suspend fun getUserItem(@Body request: UserItemRequest): Response<UserProfileEntity>

    @POST(ApiPath.addressBookCustomFriendInfo)
    suspend fun modifyFriendInfo(@Body request: AddressBookCustomFriendInfoRequest): Response<CommonResponse<Any>>

    @POST(ApiPath.serviceRoomItem)
    suspend fun getServiceRoomItem(@Body request: ServiceNumberRoomItemRequest): Response<ChatRoomEntity>

}