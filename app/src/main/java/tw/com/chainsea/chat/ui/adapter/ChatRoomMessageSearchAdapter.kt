package tw.com.chainsea.chat.ui.adapter

import android.graphics.Color
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.common.base.Strings
import com.google.common.collect.Maps
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.android.common.text.KeyWordHelper
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity
import tw.com.chainsea.ce.sdk.bean.msg.MessageType
import tw.com.chainsea.ce.sdk.bean.msg.content.AtContent
import tw.com.chainsea.ce.sdk.database.DBManager
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.ItemSearchLoadMoreBinding
import tw.com.chainsea.chat.databinding.ItemSearchMsgBinding
import tw.com.chainsea.chat.lib.AtMatcherHelper
import tw.com.chainsea.chat.ui.adapter.entity.LoadMoreEntity
import tw.com.chainsea.chat.util.TimeUtil

class ChatRoomMessageSearchAdapter(
    private val onMessageItemClick: OnMessageItemClick,
    private val isGreenTheme: Boolean
) : ListAdapter<Any, RecyclerView.ViewHolder>(MessageDiffCallback()) {
    var keyword: String = ""
    var membersTable: Map<String, String> = mapOf()

    companion object {
        const val TYPE_MESSAGE = 1
        const val TYPE_LOAD_MORE = 2
    }

    fun setData(
        data: List<Any>,
        keyWord: String,
        members: MutableList<UserProfileEntity>
    ) {
        keyword = keyWord
        membersTable = handleMemberName(members)
        submitList(data)
    }

    fun setData(
        data: List<Any>,
        keyWord: String,
        members: Map<String, String>
    ) {
        keyword = keyWord
        membersTable = members
        submitList(data)
    }

    private fun handleMemberName(members: MutableList<UserProfileEntity>): Map<String, String> {
        val result = Maps.newHashMap<String, String>()
        members.forEach {
            result[it.id] = if (!Strings.isNullOrEmpty(it.alias)) it.alias else it.nickName
        }
        return result
    }

    fun getData(): List<MessageEntity> = currentList.filterIsInstance<MessageEntity>()

    inner class MessageViewHolder(
        private val binding: ItemSearchMsgBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: MessageEntity,
            p: Int
        ) {
            val senderProfile = DBManager.getInstance().queryUser(item.senderId)
            senderProfile?.let {
                binding.sendName.text =
                    if (senderProfile.status == "Disable") {
                        senderProfile.nickName + binding.root.context.getString(R.string.text_forbidden)
                    } else {
                        senderProfile.nickName
                    }
                binding.ivAvatar.loadAvatarIcon(senderProfile.avatarId, senderProfile.nickName, item.senderId ?: "")
            }

            val type: MessageType = item.type ?: MessageType.UNDEF

            when (type) {
                MessageType.TEXT -> {
                    val text: Spanned =
                        KeyWordHelper.matcherSearchTitle(
                            if (isGreenTheme) Color.parseColor("#06B4A5") else -0xb56f1e,
                            item.content().simpleContent(),
                            keyword
                        )
                    binding.msgContent.text = text
                }

                MessageType.AT -> {
                    if (item.content() is AtContent) {
                        val ceMentions = (item.content() as AtContent).mentionContents
                        val builder = AtMatcherHelper.matcherAtUsersWithKeyword(ceMentions, membersTable, keyword)
                        binding.msgContent.text =
                            KeyWordHelper.matcherSearchTitle(if (isGreenTheme) Color.parseColor("#06B4A5") else -0xb56f1e, builder.toString(), keyword)
                    }
                }

                MessageType.CALL, MessageType.IMAGE, MessageType.FILE, MessageType.VIDEO, MessageType.VOICE, MessageType.STICKER ->
                    binding.msgContent.setText(
                        item.content().simpleContent()
                    )

                else -> binding.msgContent.text = item.content().simpleContent()
            }
            val date = TimeUtil.getTimeShowString(item.sendTime, true)
            binding.msgTime.text = date
            binding.root.setOnClickListener {
                onMessageItemClick.onItemClick(item, p, binding.ivAvatar)
            }

            binding.executePendingBindings()
        }
    }

    inner class LoadMoreViewHolder(
        private val binding: ItemSearchLoadMoreBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.btnLoadMore.setOnClickListener {
                onMessageItemClick.onLoadMoreClick()
            }
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder =
        when (viewType) {
            TYPE_MESSAGE ->
                MessageViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_search_msg,
                        parent,
                        false
                    )
                )

            TYPE_LOAD_MORE ->
                LoadMoreViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_search_load_more,
                        parent,
                        false
                    )
                )

            else -> throw IllegalArgumentException("Invalid view type")
        }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when (getItem(position)) {
            is MessageEntity -> {
                (holder as MessageViewHolder).bind(getItem(position) as MessageEntity, position)
            }

            is LoadMoreEntity -> {
                (holder as LoadMoreViewHolder).bind()
            }
        }
    }

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is MessageEntity -> TYPE_MESSAGE
            is LoadMoreEntity -> TYPE_LOAD_MORE
            else -> throw IllegalArgumentException("Invalid type of data $position")
        }
}

class MessageDiffCallback : DiffUtil.ItemCallback<Any>() {
    override fun areItemsTheSame(
        oldItem: Any,
        newItem: Any
    ): Boolean =
        if (oldItem is MessageEntity && newItem is MessageEntity) {
            oldItem.id == newItem.id
        } else {
            true
        }

    override fun areContentsTheSame(
        oldItem: Any,
        newItem: Any
    ): Boolean =
        if (oldItem is MessageEntity && newItem is MessageEntity) {
            oldItem == newItem
        } else {
            true
        }
}

interface OnMessageItemClick {
    fun onItemClick(
        item: MessageEntity,
        position: Int,
        itemView: View
    )

    fun onLoadMoreClick()
}
