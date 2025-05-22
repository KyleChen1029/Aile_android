package tw.com.chainsea.chat.util

import android.content.Context
import com.google.common.collect.ImmutableMap
import com.google.common.collect.Maps
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import tw.com.chainsea.android.common.client.type.Media
import tw.com.chainsea.android.common.json.JsonHelper
import tw.com.chainsea.android.common.log.CELog
import tw.com.chainsea.ce.sdk.bean.msg.content.ImageContent
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.http.ProgressRequestBody
import tw.com.chainsea.ce.sdk.http.ce.ApiPath
import tw.com.chainsea.ce.sdk.service.FileService.Args
import tw.com.chainsea.chat.R
import java.io.File
import kotlin.Unit

class UploadUtil(
    private val context: Context
) {
    private val tokenId by lazy { TokenPref.getInstance(context).tokenId }
    private val baseFileUploadUrl by lazy {
        TokenPref
            .getInstance(
                context
            ).currentTenantUrl + ApiPath.ROUTE + ApiPath.baseFileUpload
    }

    suspend fun uploadImage(
        filePath: String,
        onUploadSuccess: (String) -> (Unit),
        onUploadProgress: (Int) -> (Unit),
        onUploadError: (String) -> (Unit)
    ) = withContext(Dispatchers.IO) {
        val file = File(filePath)
        val client = OkHttpClient()
        val requestBody = getRequestProgressBody(file, onUploadProgress)
        val multipartBody = getMultipartBody(Args(tokenId), requestBody, file.name)
        val request = getRequest(baseFileUploadUrl, multipartBody)
        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            if (response.isSuccessful) { // 上傳成功
                responseBody?.let {
                    val content = JsonHelper.getInstance().from(it, ImageContent::class.java)
                    onUploadSuccess.invoke(content.toStringContent())
                }
            } else { // 上傳失敗
                onUploadError.invoke(
                    responseBody ?: context.getString(R.string.text_upload_image_failure)
                )
            }
            response.close()
        } catch (e: Exception) {
            CELog.e("UploadUtil uploadImage Exception", e)
            val errorMessage = e.message ?: context.getString(R.string.text_upload_image_failure)
            onUploadError.invoke(errorMessage)
        }
    }

    private fun getMultipartBody(
        args: Args,
        requestBody: ProgressRequestBody,
        fileName: String
    ): MultipartBody {
        val filePart = MultipartBody.Part.createFormData("file", fileName, requestBody)
        val formDataPart = Maps.newHashMap(ImmutableMap.of("args", args.toJson()))
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
        formDataPart.forEach { (key, value) ->
            builder.addFormDataPart(key, value)
        }
        builder.addPart(filePart)
        return builder.build()
    }

    private fun getFileMediaType(filePath: String) = Media.findByFileType(filePath).mediaType

    private fun getRequestProgressBody(
        file: File,
        onUploadProgress: (Int) -> (Unit)
    ): ProgressRequestBody =
        ProgressRequestBody(file, getFileMediaType(file.absolutePath)) { progress ->
            onUploadProgress(progress) // 回報上傳進度
        }

    private fun getRequest(
        baseUrl: String,
        multipartBody: MultipartBody
    ) = Request
        .Builder()
        .url(baseUrl)
        .post(multipartBody)
        .build()
}
