package tw.com.chainsea.ce.sdk.network.services

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity
import tw.com.chainsea.ce.sdk.http.ce.ApiPath
import tw.com.chainsea.ce.sdk.network.model.common.CommonResponse
import tw.com.chainsea.ce.sdk.network.model.request.SelfProfileRequest

interface SelfProfileService {
    @POST(ApiPath.userProfile)
    suspend fun getSelfProfile(): Response<UserProfileEntity>

    @POST(ApiPath.userUpdateProfile)
    suspend fun updateSelfProfile(@Body request: SelfProfileRequest.UpdateProfile): Response<CommonResponse<Any>>
}