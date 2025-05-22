package tw.com.chainsea.ce.sdk.network.services

import android.graphics.Bitmap
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import tw.com.chainsea.ce.sdk.http.cp.CpApiPath
import tw.com.chainsea.ce.sdk.network.model.request.TenantAvatarRequest

interface AvatarService {

    @POST(CpApiPath.TENANT_AVATAR_URL)
    suspend fun getTenantAvatarUrl(@Body request: TenantAvatarRequest): Response<Bitmap>
}