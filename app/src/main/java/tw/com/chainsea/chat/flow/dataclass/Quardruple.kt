package tw.com.chainsea.chat.flow.dataclass

import android.graphics.Bitmap
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.android.common.video.IVideoSize
import tw.com.chainsea.ce.sdk.bean.msg.content.BusinessContent
import tw.com.chainsea.chat.keyboard.view.HadEditText

data class SendVideoFormat (
    val agentName: String = "",
    val iVideoSize: IVideoSize
)

data class SendImageFormat (
    val agentName: String = "",
    val thumbnailName: String
)

data class SendImageWithBitMapFormat (
    val agentName: String = "",
    val path: String,
    val bitmap: Bitmap
)

data class SendImageGifFormat (
    val agentName: String,
    val davitPath: String,
    val localPath: String,
    val width: Int,
    val height: Int
)

data class SendFileFormat (
    val agentName: String,
    val path: String
)

data class SendButtonClickedFormat (
    val agentName: String,
    val sendData: HadEditText.SendData
)

data class SendVoiceFormat (
    val agentName: String = "",
    val path: String,
    val duration: Int
)

data class SendBusinessFormat (
    val agentName: String,
    val businessContent: BusinessContent
)

data class ReSendMessageFormat (
    val agentName: String,
    val message: MessageEntity
)
