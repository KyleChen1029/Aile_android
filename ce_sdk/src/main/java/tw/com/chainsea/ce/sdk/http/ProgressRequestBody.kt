package tw.com.chainsea.ce.sdk.http

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File

class ProgressRequestBody(
    private val file: File,
    private val contentType: MediaType?,
    private val progressCallback: (progress: Int) -> Unit
) : RequestBody() {
    override fun contentType(): MediaType? = contentType

    override fun contentLength(): Long = file.length()

    override fun writeTo(sink: BufferedSink) {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        val inputStream = file.inputStream()
        var uploaded = 0L
        val total = contentLength()

        inputStream.use { input ->
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                sink.write(buffer, 0, read)
                uploaded += read
                val progress = ((uploaded * 100) / total).toInt()
                progressCallback(progress)
            }
        }
    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 2048
    }
}
