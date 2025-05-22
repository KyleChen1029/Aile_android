package tw.com.chainsea.chat.messagekit.main.viewholder

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.chat.databinding.ItemBaseMessageBinding
import tw.com.chainsea.chat.messagekit.listener.OnFacebookReplyClick
import tw.com.chainsea.chat.messagekit.main.adapter.FacebookCommentAdapter
import tw.com.chainsea.chat.messagekit.main.viewholder.base.BaseMessageViewHolder

class FacebookCommentHolder1(
    binding: ItemBaseMessageBinding,
    chatRoomEntity: ChatRoomEntity
) : BaseMessageViewHolder(binding, chatRoomEntity) {
    private var onFacebookReplyClick: OnFacebookReplyClick? = null

    fun setOnFacebookReplyClick(onFacebookReplyClick: OnFacebookReplyClick) {
        this.onFacebookReplyClick = onFacebookReplyClick
    }

    override fun onMessageClick() {
    }

    override fun bind(message: MessageEntity) {
        setBubbleView()
        if (message.content.isEmpty()) return
        leftMessageBinding.rvLeftMultiTemplateList.apply {
            visibility = View.VISIBLE
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = FacebookCommentAdapter(message, onFacebookReplyClick)
        }
    }
}
