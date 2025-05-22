package tw.com.chainsea.chat.messagekit.main.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.android.common.json.JsonHelper
import tw.com.chainsea.android.common.log.CELog
import tw.com.chainsea.ce.sdk.bean.FacebookContentTypes
import tw.com.chainsea.ce.sdk.bean.FacebookTag
import tw.com.chainsea.ce.sdk.bean.msg.FacebookCommentStatus
import tw.com.chainsea.ce.sdk.bean.msg.FacebookPostStatus
import tw.com.chainsea.ce.sdk.bean.msg.TemplateContent
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.ItemFacebookCommentBinding
import tw.com.chainsea.chat.messagekit.listener.OnFacebookReplyClick
import tw.com.chainsea.chat.messagekit.listener.TextViewLinkClickListener
import tw.com.chainsea.chat.util.ThemeHelper
import tw.com.chainsea.chat.util.TimeUtil
import tw.com.chainsea.chat.util.UrlTextUtil

class FacebookCommentAdapter(
    private val message: MessageEntity?,
    private val onFacebookReplyClick: OnFacebookReplyClick?
) : RecyclerView.Adapter<FacebookCommentAdapter.FacebookCommentViewHolder>() {
    private val urlTextUtil = UrlTextUtil()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FacebookCommentViewHolder {
        val binding =
            ItemFacebookCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FacebookCommentViewHolder(binding)
    }

    override fun getItemCount(): Int = 1

    override fun onBindViewHolder(
        holder: FacebookCommentViewHolder,
        position: Int
    ) {
        holder.bind(message)
    }

    inner class FacebookCommentViewHolder(
        private val binding: ItemFacebookCommentBinding
    ) : ViewHolder(binding.root) {
        fun bind(message: MessageEntity?) {
            val tag: FacebookTag? =
                message?.let {
                    JsonHelper.getInstance().from(it.tag, FacebookTag::class.java)
                }

            message?.let { message ->
                val templateContent = message.content() as TemplateContent
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
                            buildTextContent(message.content)
                        }
                    }
                }

                binding.tvFacebookComment.post {
                    CoroutineScope(Dispatchers.IO).launch {
                        if (getIsEllipsis()) {
                            withContext(Dispatchers.Main) {
                                binding.tvShowMore.visibility = View.VISIBLE
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                binding.tvShowMore.visibility = View.GONE
                            }
                        }
                    }
                }

                binding.tvTime.text = TimeUtil.getHHmm(message.sendTime)
                setFacebookStatus(message.facebookPostStatus, message.facebookCommentStatus)
                initListener(message, templateContent, tag)
            }
        }

        private fun initListener(
            message: MessageEntity,
            templateContent: TemplateContent,
            tag: FacebookTag?
        ) = CoroutineScope(Dispatchers.Main).launch {
            binding.tvShowMore.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    if (getIsEllipsis()) {
                        withContext(Dispatchers.Main) {
                            binding.tvFacebookComment.maxLines = 999
                            binding.tvShowMore.text =
                                binding.root.context.getString(R.string.facebook_comment_show_less)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            binding.tvFacebookComment.maxLines = 5
                            binding.tvShowMore.text =
                                binding.root.context.getString(R.string.facebook_comment_show_more)
                        }
                    }
                }
            }

            // 查看原始貼文
            binding.tvShowOriginPost.setOnClickListener {
                templateContent.actions?.let {
                    it.forEach {
                        it.url?.let { url ->
                            val facebookIntent = Intent(Intent.ACTION_VIEW)
                            val facebookUrl = getFacebookPageURL(binding.root.context, url)
                            facebookIntent.setData(Uri.parse(facebookUrl))
                            binding.root.context.startActivity(facebookIntent)
                        }
                    }
                }
            }

            // 公開回覆
            binding.tvPublicReply.setOnClickListener {
                tag?.let {
                    onFacebookReplyClick?.onPublicReply(
                        message,
                        it.data.postId,
                        it.data.commentId
                    )
                }
            }

            // 私訊回覆
            binding.tvPrivateReply.setOnClickListener {
                onFacebookReplyClick?.onPrivateReply(message)
            }
        }

        private fun getFacebookPageURL(
            context: Context,
            url: String
        ): String {
            val packageManager = context.packageManager
            try {
                // 判斷是否有安裝 facebook app
                val facebookApp = packageManager.getPackageInfo("com.facebook.katana", 0)
                if (facebookApp.applicationInfo.enabled) {
                    return "fb://facewebmodal/f?href=$url"
                }
            } catch (e: PackageManager.NameNotFoundException) {
                CELog.e("getFacebookPageURL Error", e)
            }
            return url // normal web url
        }

        private fun setFacebookStatus(
            postStatus: FacebookPostStatus,
            commentStatus: FacebookCommentStatus
        ) = CoroutineScope(Dispatchers.Main).launch {
            when {
                postStatus == FacebookPostStatus.Delete -> {
                    binding.tvStatus.text = binding.root.context.getString(R.string.facebook_post_status_deleted)
                    binding.tvStatus.visibility = View.VISIBLE
                    binding.tvShowOriginPost.visibility = View.GONE
                    binding.groupReply.visibility = View.GONE
                }

                commentStatus == FacebookCommentStatus.Delete -> {
                    binding.tvStatus.text = binding.root.context.getString(R.string.facebook_comment_status_deleted)
                    binding.tvStatus.visibility = View.VISIBLE
                    binding.tvShowOriginPost.visibility = View.VISIBLE
                    binding.groupReply.visibility = View.GONE
                }

                commentStatus == FacebookCommentStatus.Update -> {
                    binding.tvStatus.text = binding.root.context.getString(R.string.facebook_comment_status_edited)
                    binding.tvStatus.visibility = View.VISIBLE
                    binding.tvShowOriginPost.visibility = View.VISIBLE
                    binding.groupReply.visibility = View.VISIBLE
                }

                else -> {
                    binding.tvStatus.visibility = View.GONE
                    binding.tvShowOriginPost.visibility = View.VISIBLE
                    binding.groupReply.visibility = View.VISIBLE
                }
            }
        }

        // 判斷內容是否有超過五行被折疊
        // 要顯示 "查看更多" or "顯示更少"
        private suspend fun getIsEllipsis(): Boolean =
            withContext(Dispatchers.IO) {
                if (binding.tvFacebookComment.visibility == View.GONE) return@withContext false
                val layout = binding.tvFacebookComment.layout
                layout?.let {
                    val lines: Int = layout.lineCount
                    if (lines > 0) {
                        val ellipsisCount: Int = layout.getEllipsisCount(lines - 1)
                        return@withContext ellipsisCount > 0
                    }
                }
                return@withContext false
            }

        /**
         * 建立圖片訊息
         * @param imageUrl 圖片網址
         * */
        private fun buildImageContent(imageUrl: String?) {
            val builder = replaceOriginText(binding.tvFacebookComment.text.toString(), "[圖片]")
            setFacebookComment(builder)
            binding.ivFacebookImage.visibility = View.VISIBLE
            try {
                Glide
                    .with(binding.ivFacebookImage.context)
                    .load(imageUrl)
                    .error(if (ThemeHelper.isGreenTheme()) R.drawable.image_load_error_green else R.drawable.image_load_error)
                    .into(binding.ivFacebookImage)
            } catch (ignored: Exception) {
            }
        }

        /**
         * 建立影片訊息
         * */
        private fun buildVideoContent() {
            val builder = replaceOriginText(binding.tvFacebookComment.text.toString(), "[影片]")
            setFacebookComment(builder)
        }

        /**
         * 建立連結訊息
         * @param url 網址
         * */
        private fun buildLinkContent(url: String) {
            val builder =
                if (urlTextUtil.isUrlFormat(binding.tvFacebookComment.text.toString())) {
                    StringBuilder(binding.tvFacebookComment.text)
                } else {
                    if (binding.tvFacebookComment.text
                            .toString()
                            .isNotEmpty()
                    ) {
                        StringBuilder(binding.tvFacebookComment.text).append(url)
                    } else {
                        getDefaultStringBuilder().append(url)
                    }
                }
            setFacebookComment(builder)
        }

        /**
         * 建立文字訊息
         * @param content 文字訊息
         * */
        private fun buildTextContent(content: String?) {
            val builder = getDefaultStringBuilder()
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
            val stringSpannableString = urlTextUtil.getUrlSpannableString(binding.tvFacebookComment, builder)
            binding.tvFacebookComment.text = stringSpannableString
            binding.tvFacebookComment.setOnTouchListener(TextViewLinkClickListener(stringSpannableString))
            binding.ivFacebookImage.visibility = View.GONE
        }

        /**
         * 替換原本的訊息
         * 主要是因為如果是 圖片+文字 影片+文字 會是兩包 JsonObject
         * 將原本設置好的文字訊息加上 [圖片] 或是 [影片] Tag
         * 影片的連結要替換成短網址
         * @param replaceString 替換的訊息
         * @param messageTag 圖片或影片的 tag e.g. [圖片]、[影片]
         * */
        private fun replaceOriginText(
            replaceString: String,
            messageTag: String
        ): StringBuilder =
            if (binding.tvFacebookComment.text.isNotEmpty()) {
                val index = binding.tvFacebookComment.text.indexOf(" : ")
                val builder = StringBuilder(replaceString)
                builder.insert(index + 3, "$messageTag ")
                builder.append(" ")
            } else {
                getDefaultStringBuilder().append(messageTag).append(" ")
            }

        private fun getDefaultStringBuilder(): StringBuilder = StringBuilder("${message?.senderName} : ")
    }
}
