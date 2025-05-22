package tw.com.chainsea.chat.messagekit.main.viewholder

import android.view.LayoutInflater
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.android.common.text.KeyWordHelper
import tw.com.chainsea.ce.sdk.bean.msg.content.TextContent
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.chat.databinding.ItemBaseMessageBinding
import tw.com.chainsea.chat.databinding.ItemMessageTextBinding
import tw.com.chainsea.chat.messagekit.main.viewholder.base.BaseMessageViewHolder
import tw.com.chainsea.chat.messagekit.main.viewholder.base.OnMessageSlideReply
import tw.com.chainsea.chat.util.UrlTextUtil

class TextMessageView1(
    binding: ItemBaseMessageBinding,
    layoutInflater: LayoutInflater,
    chatRoomEntity: ChatRoomEntity,
    onMessageSlideReply: OnMessageSlideReply
) : BaseMessageViewHolder(binding, chatRoomEntity, onMessageSlideReply = onMessageSlideReply) {
    private val textMessageBinding = ItemMessageTextBinding.inflate(layoutInflater)
    private var keyword: String = ""

    override fun onMessageClick() {
    }

    override fun bind(message: MessageEntity) {
        super.bind(message)
        CoroutineScope(Dispatchers.IO).launch {
            setBubbleView(textMessageBinding.root)
            if (message.content() is TextContent) {
                val textContent = message.content() as TextContent
                val text =
                    UrlTextUtil().getUrlSpannableString(
                        textMessageBinding.contentCTV,
                        KeyWordHelper.matcherSearchBackground(-0xfc7, textContent.text, keyword)
                    )
                withContext(Dispatchers.Main) {
                    textMessageBinding.contentCTV.setText(text, TextView.BufferType.NORMAL)
                    textMessageBinding.contentCTV.movementMethod =
                        TextClickMovementMethod(
                            { onMessageClick() },
                            { onMessageClickListener.get()?.onMessageLongClick(message) }
                        )
                }
            }
        }
    }

    fun setKeyword(keyword: String) {
        this.keyword = keyword
    }
}
