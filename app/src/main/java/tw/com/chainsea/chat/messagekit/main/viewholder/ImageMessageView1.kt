package tw.com.chainsea.chat.messagekit.main.viewholder

import android.view.LayoutInflater
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.android.common.ui.UiHelper
import tw.com.chainsea.ce.sdk.bean.msg.content.ImageContent
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.ItemBaseMessageBinding
import tw.com.chainsea.chat.databinding.MsgkitImage1Binding
import tw.com.chainsea.chat.messagekit.main.viewholder.base.BaseMessageViewHolder
import tw.com.chainsea.chat.messagekit.main.viewholder.base.OnMessageSlideReply
import java.io.File

class ImageMessageView1(
    binding: ItemBaseMessageBinding,
    layoutInflater: LayoutInflater,
    chatRoomEntity: ChatRoomEntity,
    onMessageSlideReply: OnMessageSlideReply
) : BaseMessageViewHolder(binding, chatRoomEntity, onMessageSlideReply = onMessageSlideReply) {
    companion object {
        private const val SMALL_PIC_SIZE = 175
    }

    private val imageMessageBinding = MsgkitImage1Binding.inflate(layoutInflater)

    override fun onMessageClick() {
        message?.let {
            onMessageClickListener?.get()?.onImageMessageClick(it)
        }
    }

    override fun bind(message: MessageEntity) {
        super.bind(message)
        CoroutineScope(Dispatchers.IO).launch {
            setBubbleView(imageMessageBinding.root)
            setImage()
        }
    }

    private fun setImage() =
        CoroutineScope(Dispatchers.IO).launch {
            message?.let { message ->
                val size = UiHelper.dip2px(imageMessageBinding.root.context, SMALL_PIC_SIZE.toFloat(), 0.5f)
                val imageContent = message.content() as ImageContent
                if (imageContent.url == null) {
                    withContext(Dispatchers.Main) {
                        Glide
                            .with(imageMessageBinding.photoRIV)
                            .load(R.drawable.image_load_error)
                            .apply(
                                RequestOptions().override(100).fitCenter()
                            ).into(imageMessageBinding.photoRIV)
                        return@withContext
                    }
                }

                val thumbnailUrl =
                    if (imageContent.url.endsWith(".gif")) imageContent.url else imageContent.thumbnailUrl
                var request: RequestBuilder<*>? = null
                if (thumbnailUrl.isNullOrEmpty()) {
                    imageContent.isFailedToLoad = true
                    message.content = imageContent.toStringContent()
                } else if (thumbnailUrl.endsWith(".gif") && !thumbnailUrl.startsWith("http")) {
                    val file = File(thumbnailUrl)
                    request =
                        Glide
                            .with(imageMessageBinding.photoRIV)
                            .asGif()
                            .load(file)
                            .apply(
                                RequestOptions()
                                    .override(size)
                                    .placeholder(R.drawable.file_msg_down_bg)
                                    .error(R.drawable.image_load_error)
                            ).fitCenter()
                            .listener(
                                OnRequestListener(
                                    imageContent,
                                    message
                                )
                            )
                } else {
                    request =
                        Glide
                            .with(imageMessageBinding.photoRIV)
                            .load(thumbnailUrl)
                            .apply(
                                RequestOptions()
                                    .override(size)
                                    .placeholder(R.drawable.file_msg_down_bg)
                                    .error(R.drawable.image_load_error)
                                    .fitCenter()
                            ).listener(
                                OnRequestListener(
                                    imageContent,
                                    message
                                )
                            )
                }
                if (imageContent.isFailedToLoad) {
                    request =
                        Glide
                            .with(imageMessageBinding.photoRIV)
                            .load(R.drawable.image_load_error)
                            .apply(
                                RequestOptions()
                                    .override(100)
                                    .fitCenter()
                            )
                }

                withContext(Dispatchers.Main) {
                    request?.into(imageMessageBinding.photoRIV)
                    imageMessageBinding.root.background = null
                }
            }
        }

    class OnRequestListener<T : Any>(
        private val imageContent: ImageContent,
        private val message: MessageEntity
    ) : RequestListener<T> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<T>,
            isFirstResource: Boolean
        ): Boolean {
            imageContent.isFailedToLoad = true
//            message.content = imageContent.toStringContent()
            return false
        }

        override fun onResourceReady(
            resource: T,
            model: Any,
            target: Target<T>?,
            dataSource: DataSource,
            isFirstResource: Boolean
        ): Boolean = false
    }
}
