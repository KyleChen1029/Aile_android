package tw.com.chainsea.chat.view.chat

import android.app.Application
import com.luck.picture.lib.entity.LocalMedia
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tw.com.chainsea.android.common.video.VideoSizeFromVideoFile
import tw.com.chainsea.chat.lib.PictureParse
import kotlin.coroutines.cancellation.CancellationException

class SequentialMediaSender(
    private val viewModelScope: CoroutineScope,
    private val mediaSender: MediaSender,
    private val onStateChange: (SendingState) -> Unit,
    private val application: Application
) {
    private var sendingJob: Job? = null

    fun send(mediaList: List<LocalMedia>) {
        sendingJob?.cancel()
        sendingJob = viewModelScope.launch {
            try {
                mediaList.forEachIndexed { index, media ->
                    onStateChange(SendingState.Sending(index + 1, mediaList.size))

                    val result = when {
                        media.mimeType.isNullOrEmpty() -> {
                            Result.failure(IllegalArgumentException("Empty mime type"))
                        }

                        media.mimeType.equals("image/gif", true) -> {
                            val bitmapBean = PictureParse.parseGifPath(application, media.realPath)
                            mediaSender.sendGifMedia(
                                bitmapBean.url,
                                media.realPath,
                                bitmapBean.width,
                                bitmapBean.height
                            )
                        }

                        media.mimeType == "video/mp4" -> {
                            val videoSize = VideoSizeFromVideoFile(media.realPath)
                            mediaSender.sendVideoMedia(videoSize)
                        }

                        media.mimeType.equals("image/png", true) ||
                                media.mimeType.equals("image/jpeg", true) -> {
                            val path = PictureParse.parsePath(application, media.realPath)
                            mediaSender.sendImageMedia(path[0], path[1])
                        }

                        else -> Result.failure(IllegalArgumentException("Unsupported mime type"))
                    }

                    result.onFailure { error ->
                        onStateChange(SendingState.Error(error.message ?: "Unknown error", index))
                        return@launch
                    }
                    delay(100)
                }

                onStateChange(SendingState.Completed)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                onStateChange(SendingState.Error(e.message ?: "Unknown error", -1))
            }
        }
    }

    fun cancel() {
        sendingJob?.cancel()
    }
}