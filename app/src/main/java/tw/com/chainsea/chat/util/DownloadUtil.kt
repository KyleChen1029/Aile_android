package tw.com.chainsea.chat.util

import android.annotation.SuppressLint
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Environment
import android.provider.MediaStore
import android.webkit.URLUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import tw.com.chainsea.android.common.log.CELog
import tw.com.chainsea.ce.sdk.bean.msg.content.VideoContent
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.chat.App
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import kotlin.Unit
import kotlin.math.abs

/**
 * 檔案下載工具
 * */
object DownloadUtil {
    val downloadFileDir =
        Environment.getExternalStorageDirectory().toString() + "/Download/"
    val downloadImageDir =
        Environment.getExternalStorageDirectory().absolutePath + "/Pictures/"
    private var fileIndex = 0
    private val downloadTasks = mutableMapOf<String, DownloadingTask>()

    /**
     * 從 Url 下載圖片
     * @param url 圖片網址
     * @param onDownloadSuccess 下載完成後的 callback
     * @param onDownloadFailed 下載失敗的 callback
     * */
    fun downloadImageFromUrl(
        url: String,
        onDownloadSuccess: (String?) -> Unit,
        onDownloadFailed: (String?) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        if (!URLUtil.isValidUrl(url)) return@launch
        if (url.endsWith(".gif")) {
            try {
                Glide
                    .with(App.getInstance())
                    .download(url)
                    .listener(
                        object : RequestListener<File?> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<File?>,
                                isFirstResource: Boolean
                            ): Boolean {
                                onDownloadFailed.invoke(e?.message)
                                return false
                            }

                            override fun onResourceReady(
                                resource: File,
                                model: Any,
                                target: Target<File?>?,
                                dataSource: DataSource,
                                isFirstResource: Boolean
                            ): Boolean {
                                CoroutineScope(Dispatchers.IO).launch {
                                    val path = saveGifImage(getBytesFromFile(resource), createFileName(url))
                                    onDownloadSuccess(path)
                                }
                                return true
                            }
                        }
                    ).submit()
            } catch (e: IOException) {
                onDownloadFailed.invoke(e.message)
            }
        } else {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            client.newCall(request).enqueue(
                object : Callback {
                    override fun onFailure(
                        call: Call,
                        e: IOException
                    ) {
                        onDownloadFailed.invoke(e.message)
                    }

                    override fun onResponse(
                        call: Call,
                        response: Response
                    ) {
                        CoroutineScope(Dispatchers.IO).launch {
                            createDir()
                            val body = response.body
                            body?.let {
                                val bytes = it.bytes()
                                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                saveImage(
                                    bitmap,
                                    createFileName(url),
                                    onDownloadSuccess,
                                    onDownloadFailed
                                )
                            }
                        }
                    }
                }
            )
        }
    }

    /**
     * 儲存圖片到指定資料夾
     * @param bitmap 要儲存的圖片
     * @param imageFileName 該圖片的 uuid
     * @param onDownloadCallback 下載完成的 callback
     * @param onDownloadFailed 下載失敗的 callback
     */
    private fun saveImage(
        bitmap: Bitmap?,
        imageFileName: String,
        onDownloadCallback: (String?) -> Unit,
        onDownloadFailed: (String?) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        if (bitmap == null) {
            onDownloadFailed.invoke("下載失敗")
            return@launch
        }
        try {
            val targetFile = getNotDuplicateFile(downloadImageDir, imageFileName)
            if (!targetFile.exists()) {
                FileOutputStream(targetFile).use { out ->
                    bitmap.compress(
                        Bitmap.CompressFormat.PNG,
                        100,
                        out
                    )
                    out.flush()
                    out.close()
                }
            }
            onDownloadCallback.invoke(targetFile.path)
        } catch (e: IOException) {
            onDownloadFailed.invoke(e.message)
            e.printStackTrace()
        }
        fileIndex = 0
    }

    fun getNotDuplicateFileForJava(
        path: String,
        originName: String,
        callback: (File) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        val file = getNotDuplicateFile(path, originName)
        callback.invoke(file)
    }

    suspend fun getNotDuplicateFile(
        path: String,
        originName: String
    ): File =
        withContext(Dispatchers.IO) {
            val file = File(path, originName)
            if (file.exists()) {
                fileIndex++
                val newNameBuilder = StringBuilder()
                val fileIndex = originName.substringAfter("(").substringBefore(")")
                val regex = """\(([^()]*)\)""".toRegex()
                if (fileIndex != originName) {
                    newNameBuilder.append(
                        originName.replace(regex, "(${this@DownloadUtil.fileIndex})")
                    )
                } else {
                    val splitName = originName.split(".")
                    splitName.forEachIndexed { index, s ->
                        if (index == 0) {
                            newNameBuilder.append(s)
                            newNameBuilder.append("(${this@DownloadUtil.fileIndex})")
                        } else {
                            newNameBuilder.append(".")
                            newNameBuilder.append(s)
                        }
                    }
                }
                return@withContext getNotDuplicateFile(path, newNameBuilder.toString())
            } else {
                val newFile = File(path, originName)
                return@withContext newFile
            }
        }

    suspend fun saveGifImage(
        bytes: ByteArray?,
        imgName: String
    ): String =
        withContext(Dispatchers.IO) {
            var fos: FileOutputStream? = null
            try {
                val externalStoragePublicDirectory = downloadImageDir + imgName
                val customDownloadDirectory =
                    File(externalStoragePublicDirectory, "Merry_Christmas")
                if (!customDownloadDirectory.exists()) {
                    customDownloadDirectory.mkdirs()
                }
                if (customDownloadDirectory.exists()) {
                    val file = File(customDownloadDirectory, imgName)
                    fos = FileOutputStream(file)
                    fos.write(bytes)
                    fos.flush()
                    fos.close()
                    file?.let {
                        val values = ContentValues()
                        values.put(MediaStore.Images.Media.TITLE, file.name)
                        values.put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
                        values.put(MediaStore.Images.Media.DESCRIPTION, "")
                        values.put(MediaStore.Images.Media.MIME_TYPE, "image/gif")
                        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
                        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
                        values.put(MediaStore.Images.Media.DATA, file.absolutePath)

                        val contentResolver = App.getInstance().contentResolver
                        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                        return@withContext externalStoragePublicDirectory
                    }
                }
            } catch (e: Exception) {
                CELog.e(this@DownloadUtil.javaClass.simpleName, e)
            }
            return@withContext ""
        }

    @Throws(IOException::class)
    private suspend fun getBytesFromFile(file: File?): ByteArray? =
        withContext(Dispatchers.IO) {
            file?.let {
                val length = it.length()
                if (length > Int.MAX_VALUE) {
                    throw IOException("File is too large!")
                }
                val bytes = ByteArray(length.toInt())
                var offset = 0
                var numRead = 0
                val `is`: InputStream = FileInputStream(it)
                `is`.use { `is` ->
                    while (offset < bytes.size &&
                        (`is`.read(bytes, offset, bytes.size - offset).also { numRead = it }) >= 0
                    ) {
                        offset += numRead
                    }
                }
                if (offset < bytes.size) {
                    throw IOException("Could not completely read file " + it.name)
                }
                return@withContext bytes
            }
            return@withContext null
        }

    /**
     * 創建資料夾
     * */
    private fun createDir() =
        CoroutineScope(Dispatchers.IO).launch {
            val dir = File(downloadImageDir)
            if (!dir.exists()) {
                dir.mkdirs()
            }
        }

    /**
     * 生成圖片檔名
     * @param url 圖片網址
     * */
    private suspend fun createFileName(url: String) =
        withContext(Dispatchers.IO) {
            val imageUUID = url.split("/").last()
            return@withContext (if (url.endsWith(".gif")) "GIF_" else "JPEG_") + "down" + imageUUID + (
                if (url.endsWith(
                        ".gif"
                    )
                ) {
                    ".gif"
                } else {
                    ".jpg"
                }
            )
        }

    @Throws(java.lang.Exception::class)
    suspend fun doDownloadVideoFile(
        videoContent: VideoContent,
        onDownloadProgress: (Int) -> Unit,
        onDownloadSuccess: (File) -> Unit,
        onDownloadFailed: (String?) -> Unit
    ): Call =
        withContext(Dispatchers.IO) {
            val downloadDir = File(downloadFileDir)
            val file = getNotDuplicateFile(downloadFileDir, videoContent.name)
            if (!downloadDir.exists()) {
                if (!downloadDir.mkdir()) {
                    throw java.lang.Exception("mkdir failed.")
                }
            }
            val url = videoContent.url
            val tokenId = TokenPref.getInstance(App.getInstance()).tokenId
            val downloadUrl = "$url?tokenId=$tokenId"
            val request = Request.Builder().url(downloadUrl).build()
            val call = download(request, file, videoContent.size, onDownloadProgress, onDownloadSuccess, onDownloadFailed)
            addDownloadTask(url, file.path, call)
            return@withContext call
        }

    @Throws(java.lang.Exception::class)
    fun doDownloadVideoFileForJava(
        videoContent: VideoContent,
        downloadVideoPath: String,
        tokenId: String,
        onDownloadProgress: (Int) -> Unit,
        onDownloadSuccess: (File) -> Unit,
        onDownloadFailed: (String?) -> Unit
    ) {
        val file = File(downloadVideoPath)
        val url = videoContent.url
        val dir = File(downloadFileDir)
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                throw java.lang.Exception("mkdir failed.")
            }
        }
        val downloadUrl = "$url?tokenId=$tokenId"
        val request = Request.Builder().url(downloadUrl).build()
        download(request, file, videoContent.size, onDownloadProgress, onDownloadSuccess, onDownloadFailed)
    }

    fun doDownloadFile(
        fileUrl: String,
        fileName: String,
        onDownloadSuccess: (File) -> Unit,
        onDownloadFailed: (String?) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        doDownloadFile(fileUrl, fileName, 0L, onDownloadSuccess, {}, onDownloadFailed)
    }

    suspend fun doDownloadFile(
        fileUrl: String,
        fileName: String,
        fileSize: Long,
        onDownloadSuccess: (File) -> Unit,
        onDownloadProgress: (Int) -> Unit,
        onDownloadFailed: (String?) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        val downloadDir = File(downloadFileDir)
        val file = getNotDuplicateFile(downloadFileDir, fileName)
        if (!downloadDir.exists()) {
            if (!downloadDir.mkdir()) {
                throw java.lang.Exception("mkdir failed.")
            }
        }

        val tokenId = TokenPref.getInstance(App.getInstance()).tokenId
        val downloadUrl = "$fileUrl?tokenId=$tokenId"
        val request = Request.Builder().url(downloadUrl).build()
        val call = download(request, file, fileSize, onDownloadProgress, onDownloadSuccess, onDownloadFailed)

        addDownloadTask(fileUrl, file.absolutePath, call)
    }

    private fun addDownloadTask(
        url: String,
        path: String,
        call: Call
    ) {
        val downloadTask = DownloadingTask(path = path, task = call)
        downloadTasks[url] = downloadTask
    }

    private fun download(
        request: Request,
        downloadFile: File,
        totalFileSize: Long,
        onDownloadProgress: (Int) -> Unit,
        onDownloadSuccess: (File) -> Unit,
        onDownloadFailed: (String?) -> Unit
    ): Call {
        val newCall = OkHttpClient().newCall(request)
        newCall.enqueue(
            object : Callback {
                override fun onFailure(
                    call: Call,
                    e: IOException
                ) {
                    onDownloadFailed.invoke("Download Error")
                    fileIndex = 0
                }

                override fun onResponse(
                    call: Call,
                    response: Response
                ) {
                    if (response.isSuccessful) {
                        response.use { res ->
                            val bytes = ByteArray(4096)
                            val fileOutputStream = FileOutputStream(downloadFile)
                            res.body?.byteStream()?.let { inputStream ->
                                try {
                                    var currentProgress = 0L
                                    var len = 0
                                    do {
                                        if (len != 0) {
                                            currentProgress += len.toLong()
                                            fileOutputStream.write(bytes, 0, len)
                                            onDownloadProgress.invoke(
                                                if (totalFileSize == 0L) {
                                                    100
                                                } else {
                                                    abs((currentProgress * 100 / totalFileSize).toDouble()).toInt()
                                                }
                                            )
                                            downloadTasks[request.url.toString()]?.progress =
                                                when {
                                                    totalFileSize <= 0L -> {
                                                        // Log 錯誤並回傳 100（或者視情況改成 0）
                                                        println("Warning: totalFileSize is 0 or negative for URL: ${request.url}")
                                                        100
                                                    }
                                                    else -> {
                                                        ((currentProgress * 100) / totalFileSize).toInt()
                                                    }
                                                }
                                        }
                                        len = inputStream.read(bytes, 0, bytes.size)
                                    } while (len != -1)
                                    onDownloadSuccess.invoke(downloadFile)
                                } catch (e: Exception) {
                                    downloadFile.delete()
                                    e.printStackTrace()
                                } finally {
                                    inputStream.close()
                                    fileOutputStream.close()
                                    fileIndex = 0
                                    downloadTasks.forEach {
                                        val downloadTask = it.value
                                        if (downloadTask.task.request() == request) {
                                            downloadTasks.remove(it.key)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
        return newCall
    }

    fun handleCancelDownload(
        file: File,
        isVideoDownload: (Boolean) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val isDelete = file.delete()
            CELog.e(if (isDelete) "刪除檔案成功" else "刪除檔案失敗")
            if (isDelete) isVideoDownload.invoke(false)
        } catch (e1: java.lang.Exception) {
            CELog.e("刪除檔案失敗", e1.message)
        }
    }

    fun getVideoDuration(videoPath: String?): String {
        // 設定影片路徑
        try {
            videoPath?.let {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(it)
                // 取得影片長度
                val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                time?.let {
                    val timeInMilliSec = it.toInt()
                    retriever.release()
                    return getUpdateTimeFormat(timeInMilliSec)
                } ?: run {
                    retriever.release()
                    return "00:00"
                }
            } ?: run {
                return "00:00"
            }
        } catch (e: Exception) {
            return "00:00"
        }
    }

    @SuppressLint("DefaultLocale")
    fun getUpdateTimeFormat(millisecond: Int): String {
        // 将毫秒转换为秒
        val second = millisecond / 1000
        // 计算小时
        val hh = second / 3600
        // 计算分钟
        val mm = second % 3600 / 60
        // 计算秒
        val ss = second % 60
        // 判断时间单位的位数
        val str: String =
            if (hh != 0) { // 表示时间单位为三位
                String.format("%02d:%02d:%02d", hh, mm, ss)
            } else {
                String.format("%02d:%02d", mm, ss)
            }
        return str
    }

    /**
     * 取得正在下載的 task
     * @param url 下載的網址(需要原始網址)
     * */
    fun getCurrentDownloadTask(url: String): DownloadingTask? {
        clearTasks()
        return downloadTasks[url]
    }

    /**
     * 清理 task
     * */
    private fun clearTasks() {
        downloadTasks.forEach {
            val downloadTask = it.value
            if (downloadTask.task.isCanceled()) {
                downloadTasks.remove(it.key)
            }
        }
    }
}

data class DownloadingTask(
    val path: String,
    var progress: Int = 0,
    val task: Call
)
