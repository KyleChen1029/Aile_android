package tw.com.chainsea.chat.messagekit.main.viewholder

import android.view.LayoutInflater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity
import tw.com.chainsea.ce.sdk.bean.msg.content.AtContent
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.chat.databinding.ItemBaseMessageBinding
import tw.com.chainsea.chat.databinding.ItemMessageAtBinding
import tw.com.chainsea.chat.lib.AtMatcherHelper
import tw.com.chainsea.chat.messagekit.main.viewholder.base.BaseMessageViewHolder
import tw.com.chainsea.chat.messagekit.main.viewholder.base.OnMessageSlideReply

class AtMessageView1(
    binding: ItemBaseMessageBinding,
    layoutInflater: LayoutInflater,
    chatMembers: MutableList<UserProfileEntity>,
    chatRoomEntity: ChatRoomEntity,
    onMessageSlideReply: OnMessageSlideReply
) : BaseMessageViewHolder(binding, chatRoomEntity, onMessageSlideReply = onMessageSlideReply) {
    private val atMessageBinding = ItemMessageAtBinding.inflate(layoutInflater)
    private var keyword: String = ""

    override fun onMessageClick() {
    }

    override fun bind(message: MessageEntity) {
        super.bind(message)
        CoroutineScope(Dispatchers.IO).launch {
            setBubbleView(atMessageBinding.root)
            val memberName = getMemberName()
            if (message.content() is AtContent) {
                val atContent = message.content() as AtContent
                val mentions = atContent.mentionContents
                val builder =
                    AtMatcherHelper.matcherAtUsersWithKeyword(mentions, memberName, keyword) {
                        onMessageClickListener.get()?.onAtMessageClick(it)
                    }
                withContext(Dispatchers.Main) {
                    atMessageBinding.tvAtMessage.text = builder
                    atMessageBinding.tvAtMessage.movementMethod =
                        TextClickMovementMethod(
                            { onMessageClick() },
                            { onMessageClickListener?.get()?.onMessageLongClick(message) }
                        )
                }
            }
        }
    }

    fun setKeyword(keyword: String) {
        this.keyword = keyword
    }
}
