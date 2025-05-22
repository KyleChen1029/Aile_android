package tw.com.chainsea.ce.sdk.network.services

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import tw.com.chainsea.ce.sdk.http.ce.ApiPath
import tw.com.chainsea.ce.sdk.network.model.common.BaseRequest
import tw.com.chainsea.ce.sdk.network.model.response.SyncTodoResponse

interface TodoService {

    @POST(ApiPath.syncTodo)
    suspend fun syncTodo(@Body request: BaseRequest): Response<SyncTodoResponse>
}