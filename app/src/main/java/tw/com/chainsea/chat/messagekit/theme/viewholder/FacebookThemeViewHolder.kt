package tw.com.chainsea.chat.messagekit.theme.viewholder

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.android.common.json.JsonHelper
import tw.com.chainsea.ce.sdk.bean.FacebookContentTypes
import tw.com.chainsea.ce.sdk.bean.FacebookTag
import tw.com.chainsea.ce.sdk.bean.msg.FacebookCommentStatus
import tw.com.chainsea.ce.sdk.bean.msg.FacebookPostStatus
import tw.com.chainsea.ce.sdk.bean.msg.TemplateContent
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.messagekit.listener.TextViewLinkClickListener
import tw.com.chainsea.chat.messagekit.theme.viewholder.base.ThemeMessageBubbleView
import tw.com.chainsea.chat.util.TimeUtil
import tw.com.chainsea.chat.util.UrlTextUtil

class FacebookThemeViewHolder(
    val view: View
) : ThemeMessageBubbleView(view) {
    private var tvContent: TextView? = null
    private var ivImage: ImageView? = null
    private var tvStatus: TextView? = null

    private val urlTextUtil = UrlTextUtil()

    override fun onClick(
        v: View?,
        message: MessageEntity?
    ) {
    }

    override fun onDoubleClick(
        v: View?,
        message: MessageEntity?
    ) {
    }

    override fun onLongClick(
        v: View?,
        x: Float,
        y: Float,
        message: MessageEntity?
    ) {
    }

    override fun showName(): Boolean = false

    override fun getContentResId(): Int = R.layout.item_facebook_reply_theme

    override fun inflateContentView() {
        tvContent = findView(R.id.tv_content)
        ivImage = findView(R.id.iv_image)
        tvStatus = findView(R.id.tv_status)
        tvTime = findView(R.id.tv_time)
    }

    override fun bindContentView() {
        val tag: FacebookTag? =
            msg?.let {
                JsonHelper.getInstance().from(it.tag, FacebookTag::class.java)
            }
        ivImage?.visibility = View.GONE
        tvStatus?.visibility = View.GONE
        tvContent?.visibility = View.GONE
        tag?.let { tag ->
            tag.data.content?.let { content ->
                if (content.isNotEmpty()) {
                    content.forEach { tagContent ->
                        tagContent.type?.let { contentType ->
                            when (contentType) {
                                FacebookContentTypes.Video -> buildVideoContent()
                                FacebookContentTypes.Image -> buildImageContent(tagContent.url)
                                FacebookContentTypes.Link -> buildLinkContent(tagContent.url)
                                else -> buildTextContent(tagContent.content)
                            }
                        }
                    }
                } else {
                    buildTextContent(msg.content)
                }
            }
        }

        val content = msg.content() as TemplateContent
        tvTime.text = TimeUtil.getHHmm(msg.sendTime)
        content.text?.let {
            if (it.isEmpty()) {
                tvContent?.visibility = View.GONE
            } else {
                tvContent?.apply {
                    visibility = View.VISIBLE
                    val stringSpannableString = urlTextUtil.getUrlSpannableString(this, it)
                    text = stringSpannableString
                    setOnTouchListener(TextViewLinkClickListener(stringSpannableString))
                }
            }
        }
        when {
            msg.facebookPostStatus == FacebookPostStatus.Delete -> {
                tvStatus?.text = context.getString(R.string.facebook_post_status_deleted)
                tvStatus?.visibility = View.VISIBLE
            }

            msg.facebookCommentStatus == FacebookCommentStatus.Delete -> {
                tvStatus?.text = context.getString(R.string.facebook_comment_status_deleted)
                tvStatus?.visibility = View.VISIBLE
            }

            msg.facebookCommentStatus == FacebookCommentStatus.Update -> {
                tvStatus?.text = context.getString(R.string.facebook_comment_status_edited)
                tvStatus?.visibility = View.VISIBLE
            }

            else -> {
                tvStatus?.visibility = View.GONE
            }
        }
    }

    /**
     * 建立影片訊息
     * */
    private fun buildVideoContent() {
        tvContent?.let {
            val builder = StringBuilder(it.text.toString())
            setFacebookComment(builder)
        }
    }

    /**
     * 建立圖片訊息
     * @param imageUrl 圖片網址
     * */
    private fun buildImageContent(imageUrl: String?) {
        tvContent?.let {
            val builder = StringBuilder(it.text.toString())
            setFacebookComment(builder)
            ivImage?.visibility = View.VISIBLE
            Glide
                .with(ivImage?.context!!)
                .load(imageUrl)
                .apply(
                    RequestOptions()
                        .override(400)
                        .placeholder(R.drawable.loading_area)
                        .error(R.drawable.image_load_error)
                        .fitCenter()
                ).into(ivImage!!)
        }
    }

    /**
     * 建立連結訊息
     * @param url 網址
     * */
    private fun buildLinkContent(url: String) {
        tvContent?.let {
            val builder =
                if (urlTextUtil.isUrlFormat(it.text.toString())) {
                    StringBuilder(it.text)
                } else {
                    if (it.text.toString().isNotEmpty()) {
                        StringBuilder(it.text).append(url)
                    } else {
                        StringBuilder().append(url)
                    }
                }
            setFacebookComment(builder)
        }
    }

    /**
     * 建立文字訊息
     * @param content 文字訊息
     * */
    private fun buildTextContent(content: String?) {
        val builder = StringBuilder()
        content?.let { builder.append(content) }
        builder.append(" ")
        setFacebookComment(builder)
    }

    /**
     * 設置 Facebook 訊息
     * @param builder 整理好的 Facebook 訊息
     * */
    @SuppressLint("ClickableViewAccessibility")
    private fun setFacebookComment(builder: StringBuilder) {
        tvContent?.let {
            val stringSpannableString =
                urlTextUtil.getUrlSpannableString(it, builder)
            it.text = stringSpannableString
            it.setOnTouchListener(TextViewLinkClickListener(stringSpannableString))
            ivImage?.visibility = View.GONE
        }
    }
}
