package tw.com.chainsea.chat.network.tenant

import android.app.Application
import com.google.common.collect.ImmutableMap
import com.google.common.collect.Maps
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import tw.com.chainsea.android.common.client.type.FileMedia
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.http.cp.CpApiPath
import tw.com.chainsea.ce.sdk.network.model.common.ApiErrorData
import tw.com.chainsea.ce.sdk.network.model.common.ApiResult
import tw.com.chainsea.ce.sdk.network.model.request.TenantAvatarRequest
import tw.com.chainsea.ce.sdk.network.model.request.TenantGuarantorRequest
import tw.com.chainsea.ce.sdk.network.services.AvatarService
import tw.com.chainsea.ce.sdk.network.services.TenantService
import tw.com.chainsea.ce.sdk.service.FileService.Args
import java.io.File


/**
 * 團隊相關
 * 要注意有分 Cp Ce
 * */
class TenantRepository(
    private val tenantCpService: TenantService, private val tenantCeService: TenantService,
    private val avatarService: AvatarService
) {


    //取得團隊列表
    fun getTenantList() = flow {
        emit(ApiResult.Loading(true))
        val response = tenantCpService.getTenantList()
        response.body()?.let {
            it._header_?.let { header ->
                if (header.success!!) {
                    emit(ApiResult.Success(it))
                }
            }
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }

    //取得擔保人列表
    fun getTenantGuarantorList() = flow {
        emit(ApiResult.Loading(true))
        val response = tenantCeService.getTenantGuarantorList()
        response.body()?.let {
            emit(ApiResult.Success(it))
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }

    // 掃描擔保人加入
    fun tenantGuarantorAdd(userId: String) = flow {
        emit(ApiResult.Loading(true))
        val response = tenantCeService.tenantGuarantorAdd(TenantGuarantorRequest(userId))
        response.body()?.let {
            it._header_?.let { header ->
                if (header.success!!) {
                    emit(ApiResult.Success(header.success))
                } else {
                    emit(
                        ApiResult.Failure<Boolean>(
                            ApiErrorData(
                                header.errorMessage!!,
                                header.errorCode!!
                            )
                        )
                    )
                }
            }

        }
    }.flowOn(Dispatchers.IO).catch { e ->
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }

    // 被掃描的同意當擔保人
    fun tenantGuarantorJoin(userId: String) = flow {
        emit(ApiResult.Loading(true))
        val response = tenantCeService.tenantReGuarantorAgree(TenantGuarantorRequest(userId))
        response.body()?.let {
            it._header_?.let { header ->
                if (header.success!!) {
                    emit(ApiResult.Success(header.success))
                } else {
                    emit(
                        ApiResult.Failure<Boolean>(
                            ApiErrorData(
                                header.errorMessage!!,
                                header.errorCode!!
                            )
                        )
                    )
                }
            }
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }

    // 被掃描的拒絕當擔保人
    fun tenantGuarantorReject(userId: String) = flow {
        emit(ApiResult.Loading(true))
        val response = tenantCeService.tenantReGuarantorReject(TenantGuarantorRequest(userId))
        response.body()?.let {
            it._header_?.let { header ->
                if (header.success!!) {
                    emit(ApiResult.Success(header.success))
                } else {
                    emit(
                        ApiResult.Failure<Boolean>(
                            ApiErrorData(
                                header.errorMessage!!,
                                header.errorCode!!
                            )
                        )
                    )
                }
            }
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }

    /**
     * 上傳團隊頭像
     * */
    fun updateTenantAvatar(
        application: Application,
        filePath: String,
        fileName: String,
        size: Int,
        tokenId: String
    ) = flow {
        emit(ApiResult.Loading(true))
        val url = TokenPref.getInstance(application).currentTenantUrl + CpApiPath.TENANT_UPDATE;
        val builders = MultipartBody.Builder().setType(MultipartBody.FORM)
        val fileType = fileName.substring(fileName.lastIndexOf("."), fileName.length)
        val mediaType = FileMedia.of(fileType)
        val fileBody = RequestBody.create(mediaType, File(filePath))
        builders.addFormDataPart("avatar", fileName, fileBody)

        val args = Args(tokenId)
            .x(0)
            .y(0)
            .size(size)
            .toJson()
        val formData: Map<String, String> = Maps.newHashMap(ImmutableMap.of("args", args))
        for ((key, value) in formData.entries) {
            builders.addFormDataPart(key, value)
        }

        val rb = Request.Builder()
            .url(url)
            .post(builders.build())

        val request: Request = rb.build()

        val response = OkHttpClient().newCall(request).execute()
        response.body?.let {
            val jsonObject = JSONObject(it.string())
            emit(ApiResult.Success(jsonObject))
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }

    /**
     * 取得頭像
     * */
    fun getTenantAvatarUrl(avatarId: String) = flow {
        emit(ApiResult.Loading(true))

        val response = avatarService.getTenantAvatarUrl(TenantAvatarRequest(avatarId))
        response.body()?.let {
            emit(ApiResult.Success(it))
        }

    }.flowOn(Dispatchers.IO).catch { e ->
        e.message?.let {
            emit(ApiResult.Failure(ApiErrorData(it, e.stackTrace[0].toString())))
        } ?: run {
            emit(ApiResult.Failure(ApiErrorData(e.toString(), e.stackTrace[0].toString())))
        }
    }

}