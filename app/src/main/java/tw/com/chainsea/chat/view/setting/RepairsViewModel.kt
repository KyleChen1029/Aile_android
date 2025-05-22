package tw.com.chainsea.chat.view.setting

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.common.collect.Lists
import com.luck.picture.lib.entity.LocalMedia
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.Buffer
import okio.BufferedSink
import okio.ForwardingSink
import okio.buffer
import org.json.JSONObject
import tw.com.chainsea.android.common.client.type.FileMedia
import tw.com.chainsea.android.common.file.FileHelper
import tw.com.chainsea.android.common.version.VersionHelper
import tw.com.chainsea.ce.sdk.config.AppConfig
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.http.cp.CpApiPath
import tw.com.chainsea.ce.sdk.http.cp.base.CpNewRequestBase
import tw.com.chainsea.ce.sdk.service.FileService.RepairArgs
import java.io.File
import java.io.IOException

class RepairsViewModel(
    private val application: Application,
): ViewModel() {

    val sendRepairSuccess = MutableLiveData<Boolean>()
    val mLocalMedia : MutableList<LocalMedia> = mutableListOf()

    fun doRepair(
        name: String,
        type: String,
        content: String,
        localMedia: MutableList<String>,
        logs: MutableList<String>,
        progressListener: UploadProgressListener,
        completionListener: (Boolean) -> Unit
    ) {
        val builders = MultipartBody.Builder().setType(MultipartBody.FORM)
        logs.addAll(localMedia)
        val tempDir: String = application.filesDir.toString() + "/log/"
        val tempLogPath: MutableList<String> = Lists.newArrayList()
        for (mLogPath in logs) { //取Log
            val file = File(mLogPath)
            if (file.totalSpace > 0) {
                val tempPath = tempDir + "t_" + file.name
                try {
                    FileHelper.copy(mLogPath, tempPath, false)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                tempLogPath.add(tempPath)
            }
        }
        tempLogPath.forEach { path ->
            val fileType = path.substring(path.lastIndexOf("."))
            val mediaType = if (fileType == ".log") {
                "text/x-markdown; charset=utf-8".toMediaTypeOrNull()
            } else {
                FileMedia.of(fileType)
            }
            val fileBody = RequestBody.create(mediaType, File(path))
            builders.addFormDataPart("file", path, fileBody)
        }

        val args = RepairArgs(TokenPref.getInstance(application).cpTokenId)
            .name(name)
            .type(type)
            .version(VersionHelper.getVersionName(application))
            .content(content)
            .osType(AppConfig.osType)
            .toJson()
        builders.addFormDataPart("args", args)

        val requestBody = builders.build()
        val progressRequestBody = ProgressRequestBody(requestBody, progressListener)

        val request = Request.Builder()
            .url(CpNewRequestBase.BASE_URL + CpApiPath.BASE_REPAIR)
            .post(progressRequestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                completionListener(false)
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let {
                    try {
                        val jsonObject = JSONObject(it.string())
                        val success = jsonObject.getBoolean("success")
                        val status = jsonObject.getString("status")
                        completionListener(success && status == "0000")

                        tempLogPath.forEach { file ->
                            File(file).delete()
                        }

                    } catch (e: Exception) {
                        completionListener(false)
                    }
                } ?: run {
                    completionListener(false)
                }
            }
        })
    }
}

interface UploadProgressListener {
    fun onProgressUpdate(progress: Int)
}

private class ProgressRequestBody(
    private val requestBody: RequestBody,
    private val progressListener: UploadProgressListener
) : RequestBody() {
    override fun contentType(): MediaType? = requestBody.contentType()

    override fun contentLength(): Long = requestBody.contentLength()

    override fun writeTo(sink: BufferedSink) {
        val totalBytes = contentLength()
        var uploadedBytes = 0L

        val forwardingSink = object : ForwardingSink(sink) {
            override fun write(source: Buffer, byteCount: Long) {
                super.write(source, byteCount)
                uploadedBytes += byteCount
                progressListener.onProgressUpdate(((uploadedBytes.toFloat() / totalBytes) * 100).toInt())
            }
        }

        val bufferedSink = forwardingSink.buffer()
        requestBody.writeTo(bufferedSink)
        bufferedSink.flush()
    }
}