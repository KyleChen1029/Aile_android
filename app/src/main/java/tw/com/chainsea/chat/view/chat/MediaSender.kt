package tw.com.chainsea.chat.view.chat

import tw.com.chainsea.android.common.video.IVideoSize

interface MediaSender {
    suspend fun sendImageMedia(path1: String, path2: String): Result<Unit>
    suspend fun sendVideoMedia(videoSize: IVideoSize): Result<Unit>
    suspend fun sendGifMedia(url: String, path: String, width: Int, height: Int): Result<Unit>
}