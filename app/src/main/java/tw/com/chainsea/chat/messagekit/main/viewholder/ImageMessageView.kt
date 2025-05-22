package tw.com.chainsea.chat.messagekit.main.viewholder

import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.viewbinding.ViewBinding
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
import tw.com.chainsea.android.common.log.CELog
import tw.com.chainsea.ce.sdk.bean.msg.content.ImageContent
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.MsgkitImageBinding
import tw.com.chainsea.chat.messagekit.main.viewholder.base.MessageBubbleView
import tw.com.chainsea.chat.util.DaVinci
import tw.com.chainsea.chat.widget.LoadingBar
import java.io.File

class ImageMessageView : MessageBubbleView<ImageContent> {
    private lateinit var binding: MsgkitImageBinding

    companion object {
        private const val SMALL_PIC_SIZE = 360
    }
    constructor(binding: ViewBinding) : super(binding) {
        this.binding =
            MsgkitImageBinding.inflate(LayoutInflater.from(itemView.context), null, false)
        getView(this.binding.root)
    }

    override fun getContentResId(): Int = R.layout.msgkit_image

    override fun getChildView(): View = binding.root

    override fun onDoubleClick(
        v: View?,
        message: MessageEntity?
    ) {
        CELog.d("ImageMessageView onDoubleClick")
    }

    override fun onLongClick(
        v: View?,
        x: Float,
        y: Float,
        message: MessageEntity?
    ) {
        onMessageControlEventListener?.onLongClick(message, x.toInt(), y.toInt())
    }

    override fun onClick(
        v: View?,
        message: MessageEntity?
    ) {
        super.onClick(v, message)
        onMessageControlEventListener?.onImageClick(message)
    }

    override fun leftBackground(): Int = R.drawable.msgkit_trans_bg

    override fun rightBackground(): Int = R.drawable.msgkit_trans_bg

    override fun showName(): Boolean = !isRightMessage

    override fun bindContentView(imageContent: ImageContent) {
        CoroutineScope(Dispatchers.IO).launch {
            val thumbnailUrl =
                if (imageContent.url.endsWith(".gif")) imageContent.url else imageContent.thumbnailUrl
            val width = if (imageContent.width == 0) 200 else imageContent.width
            val height = if (imageContent.height == 0) 200 else imageContent.height
            val params = binding.occupationCL.layoutParams
            val zoomSize = zoomImage(width, height)
            params.width = zoomSize[0]
            params.height = zoomSize[1]
            withContext(Dispatchers.Main) {
                binding.occupationCL.layoutParams = params
                binding.occupationCL.background =
                    ResourcesCompat.getDrawable(
                        binding.occupationCL.context.resources,
                        R.drawable.file_msg_down_bg,
                        null
                    )
            }
            setImage(imageContent)
        }
    }

    private fun setImage(imageContent: ImageContent) =
        CoroutineScope(Dispatchers.IO).launch {
            if (imageContent.url == null) {
                withContext(Dispatchers.Main) {
                    Glide
                        .with(binding.photoRIV)
                        .load(R.drawable.image_load_error)
                        .apply(
                            RequestOptions().override(100).fitCenter()
                        ).into(binding.photoRIV)
                    return@withContext
                }
            }
            val thumbnailUrl = if (imageContent.url.endsWith(".gif")) imageContent.url else imageContent.thumbnailUrl
            var request: RequestBuilder<*>? = null
            if (thumbnailUrl.isNullOrEmpty()) {
                imageContent.isFailedToLoad = true
                message.content = imageContent.toStringContent()
            } else if (thumbnailUrl.startsWith("smallandroid")) {
                val image = DaVinci.with().imageLoader.getImage(thumbnailUrl)
                image?.bitmap?.let {
                    request =
                        Glide
                            .with(binding.photoRIV)
                            .load(it)
                            .apply(
                                RequestOptions()
                                    .override(SMALL_PIC_SIZE)
                                    .placeholder(R.drawable.file_msg_down_bg)
                                    .error(if (isGreenTheme) R.drawable.image_load_error_green else R.drawable.image_load_error)
                                    .fitCenter()
                            ).listener(OnRequestListener(imageContent, binding.occupationCL, binding.progressBar))
                }
            } else if (thumbnailUrl.endsWith(".gif") && !thumbnailUrl.startsWith("http")) {
                val file = File(thumbnailUrl)
                request =
                    Glide
                        .with(binding.photoRIV)
                        .asGif()
                        .load(file)
                        .apply(
                            RequestOptions()
                                .override(SMALL_PIC_SIZE)
                                .placeholder(R.drawable.file_msg_down_bg)
                                .error(if (isGreenTheme) R.drawable.image_load_error_green else R.drawable.image_load_error)
                        ).fitCenter()
                        .listener(OnRequestListener(imageContent, binding.occupationCL, binding.progressBar))
            } else {
                request =
                    Glide
                        .with(binding.photoRIV)
                        .load(thumbnailUrl)
                        .apply(
                            RequestOptions()
                                .override(SMALL_PIC_SIZE)
                                .placeholder(R.drawable.file_msg_down_bg)
                                .error(if (isGreenTheme) R.drawable.image_load_error_green else R.drawable.image_load_error)
                                .fitCenter()
                        ).listener(OnRequestListener(imageContent, binding.occupationCL, binding.progressBar))
            }
            if (imageContent.isFailedToLoad) {
                withContext(Dispatchers.Main) {
                    binding.occupationCL.background = null
                }
                request =
                    Glide
                        .with(binding.photoRIV)
                        .load(if (isGreenTheme) R.drawable.image_load_error_green else R.drawable.image_load_error)
                        .apply(
                            RequestOptions()
                                .override(100)
                                .fitCenter()
                        )
            }

            withContext(Dispatchers.Main) {
                request?.into(binding.photoRIV)
            }
        }

    private suspend fun zoomImage(
        width: Int,
        height: Int
    ): IntArray =
        withContext(Dispatchers.IO) {
            return@withContext if (width > height) {
                intArrayOf(SMALL_PIC_SIZE, (height * SMALL_PIC_SIZE) / width)
            } else {
                intArrayOf((width * SMALL_PIC_SIZE) / height, SMALL_PIC_SIZE)
            }
        }

    inner class OnRequestListener<T : Any>(
        private val imageContent: ImageContent,
        private val occupationCL: ConstraintLayout,
        private val progressBar: LoadingBar
    ) : RequestListener<T> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<T>,
            isFirstResource: Boolean
        ): Boolean {
            occupationCL.background = null
            progressBar.visibility = View.GONE
            imageContent.isFailedToLoad = true
            message.content = imageContent.toStringContent()
            return false
        }

        override fun onResourceReady(
            resource: T,
            model: Any,
            target: Target<T>?,
            dataSource: DataSource,
            isFirstResource: Boolean
        ): Boolean {
            occupationCL.background = null
            progressBar.visibility = View.GONE
            return false
        }
    }
}
