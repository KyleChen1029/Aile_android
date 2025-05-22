package tw.com.chainsea.ce.sdk.network.services

import retrofit2.Response
import retrofit2.http.POST
import tw.com.chainsea.ce.sdk.http.cp.CpApiPath
import tw.com.chainsea.ce.sdk.http.cp.respone.CheckVersionResponse
import tw.com.chainsea.ce.sdk.network.model.common.CommonResponse

interface VersionService {

    @POST(CpApiPath.BASE_VERSION_CHECK)
    suspend fun getVersion(): Response<CheckVersionResponse>
}