package tw.com.chainsea.chat.messagekit.main.viewholder

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.droidsonroids.gif.GifDrawable
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.android.common.log.CELog
import tw.com.chainsea.ce.sdk.bean.msg.content.StickerContent
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.http.ce.ApiManager
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener
import tw.com.chainsea.ce.sdk.http.ce.request.StickerDownloadRequest
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.ItemBaseMessageBinding
import tw.com.chainsea.chat.databinding.ItemMessageStickerBinding
import tw.com.chainsea.chat.messagekit.main.viewholder.base.BaseMessageViewHolder
import tw.com.chainsea.chat.messagekit.main.viewholder.base.OnMessageSlideReply
import java.io.File

class StickerMessage1(
    binding: ItemBaseMessageBinding,
    layoutInflater: LayoutInflater,
    chatRoomEntity: ChatRoomEntity,
    onMessageSlideReply: OnMessageSlideReply
) : BaseMessageViewHolder(binding, chatRoomEntity, onMessageSlideReply = onMessageSlideReply) {
    private val stickerMessageBinding = ItemMessageStickerBinding.inflate(layoutInflater)

    override fun onMessageClick() {
    }

    override fun bind(message: MessageEntity) {
        super.bind(message)
        CoroutineScope(Dispatchers.IO).launch {
            setBubbleView(stickerMessageBinding.root)
            if (message.content.isEmpty()) return@launch
            withContext(Dispatchers.Main) {
                stickerMessageBinding.root.background = null
            }
            val stickerContent = message.content() as StickerContent
            getSticker(stickerContent.packageId, stickerContent.id) {
                CoroutineScope(Dispatchers.Main).launch {
                    it?.let {
                        stickerMessageBinding.msgEmoticons.setImageDrawable(it)
                    } ?: run {
                        stickerMessageBinding.msgEmoticons.setImageResource(
                            R.drawable.image_load_error
                        )
                    }
                }
            }
        }
    }

    private fun getSticker(
        packageId: String,
        stickerId: String,
        callback: (Drawable?) -> (Unit)
    ) = CoroutineScope(Dispatchers.IO).launch {
        if (stickerId.isEmpty()) return@launch
        if (packageId.isEmpty()) return@launch
        val context = binding.root.context
        try {
            val inputSteam = context.assets.open("emoticons/qbi/$stickerId")
            callback.invoke(GifDrawable(inputSteam))
        } catch (e: Exception) {
            CELog.e("getSticker Error", e)
        }

        val tempPath =
            "${context.cacheDir}/sticker/$packageId${StickerDownloadRequest.Type.PICTURE.path}"
        val stickerFile = File(tempPath + stickerId)
        if (stickerFile.exists()) {
            callback.invoke(GifDrawable(stickerFile))
        } else {
            ApiManager.doStickerDownload(
                context,
                true,
                packageId,
                stickerId,
                StickerDownloadRequest.Type.PICTURE,
                object : ApiListener<Drawable> {
                    override fun onSuccess(drawable: Drawable?) {
                        callback.invoke(drawable)
                    }

                    override fun onFailed(errorMessage: String?) {
                        callback.invoke(null)
                        CELog.e("download sticker error: $errorMessage")
                    }
                }
            )
        }
    }
}
