package tw.com.chainsea.chat.messagekit.main.viewholder

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.ce.sdk.bean.msg.TemplateContent
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.ItemMsgBubbleBinding
import tw.com.chainsea.chat.messagekit.main.adapter.FacebookCommentAdapter
import tw.com.chainsea.chat.messagekit.main.viewholder.base.MessageBubbleView

class FacebookCommentHolder(
    private val baseBinding: ViewBinding
) : MessageBubbleView<TemplateContent>(baseBinding) {
    override fun getChildView(): View = baseBinding.root

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

    override fun getContentResId(): Int = R.layout.item_facebook_comment

    override fun bindContentView(data: TemplateContent?) {
        baseBinding as ItemMsgBubbleBinding
        baseBinding.rvMultiTemplateList
        baseBinding.rvMultiTemplateList.apply {
            visibility = View.VISIBLE
            layoutManager =
                LinearLayoutManager(baseBinding.root.context, LinearLayoutManager.HORIZONTAL, false)
            adapter = FacebookCommentAdapter(message, onFacebookReplyClick.get())
        }
    }
}
