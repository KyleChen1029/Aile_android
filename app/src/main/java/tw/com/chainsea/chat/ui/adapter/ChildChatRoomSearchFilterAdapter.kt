package tw.com.chainsea.chat.ui.adapter

import android.annotation.SuppressLint
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import tw.com.aile.sdk.bean.filter.FilterRoomType
import tw.com.aile.sdk.bean.filter.SearchFilterEntity
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.ce.sdk.SdkLib
import tw.com.chainsea.ce.sdk.bean.msg.MessageFlag
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.http.ce.model.LastMessageType
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.base.Constant
import tw.com.chainsea.chat.databinding.ItemChatRoomFriendsBinding
import tw.com.chainsea.chat.searchfilter.viewmodel.ChatRoomViewModel
import tw.com.chainsea.chat.util.ThemeHelper
import tw.com.chainsea.chat.util.TimeUtil

class ChildChatRoomSearchFilterAdapter(
    private val userId: String,
    private val viewModel: Any,
    val keyWord: String,
    val data: SearchFilterEntity,
    val entities: List<Any>
) : ListAdapter<Any, RecyclerView.ViewHolder>(DiffCallback()) {
    companion object {
        const val TYPE_CHAT_ROOM = 1
    }

    init {
        submitList(entities)
    }

    fun getItemContent(lastMessage: MessageEntity): CharSequence {
        val builder = SpannableStringBuilder()
        lastMessage.sourceType?.let {
            if (userId == lastMessage.senderId) { // 如果是自己就改用我
                builder.append("我 : ")
            } else if (LastMessageType.SourceType.SYSTEM == it.name || LastMessageType.SourceType.LOGIN == it.name || LastMessageType.SourceType.SATISFACTION == it.name) {
                builder.append("")
            } else {
                builder.append(lastMessage.senderName).append(" : ")
            }
        }

        lastMessage.flag?.let {
            if (MessageFlag.RETRACT.flag == it.flag) { // 回收訊息
                builder.append(Constant.RETRACT_MSG)
            } else {
                builder.append(lastMessage.getFormatContent())
            }
        }

        return builder
    }

    @SuppressLint("SetTextI18n")
    inner class ChatRoomFilterListViewHolder(
        private val binding: ItemChatRoomFriendsBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: ChatRoomEntity,
            viewModel: ChatRoomViewModel,
            position: Int
        ) {
            binding.item = item
            binding.ivAvatar.loadAvatarIcon(item.avatarId, item.name, item.id)

            if (item.unReadNum > 0) {
                binding.tvUnread.text = item.unReadNum.toString()
            }

            binding.tvUnread.visibility = if (item.unReadNum > 0) View.VISIBLE else View.GONE
            binding.pinIcon.visibility = if (item.isTop) View.VISIBLE else View.GONE

            binding.tvName.text =
                if (keyWord.isNotEmpty()) {
                    setKeyWordColor(item.name)
                } else {
                    item.name
                }

            item.lastMessage?.let {
                binding.tvTime.text = TimeUtil.getTimeShowString(it.sendTime, true)
            }

            if (data.roomType == FilterRoomType.FRIENDS) {
                item.lastMessage?.let {
                    binding.tvLastMsg.text = getItemContent(it)
                }
            } else {
                item.lastMessage?.let {
                    binding.tvLastMsg.text =
                        if (keyWord.isNotEmpty()) {
                            setKeyWordColor(it.content)
                        } else {
                            it.content
                        }
                }
            }
            itemView.setBackgroundColor(ContextCompat.getColor(SdkLib.getAppContext(), if (item.isSelected) R.color.color_F0FAFF else R.color.transparent))
            binding.root.setOnClickListener {
                viewModel.onSelectChatRoomItem(item, position)
            }

            binding.executePendingBindings()
        }

        fun update(item: Any) {
            if (item is ChatRoomEntity) {
                itemView.setBackgroundColor(ContextCompat.getColor(SdkLib.getAppContext(), if (item.isSelected) R.color.color_F0FAFF else R.color.transparent))
            }
        }
    }

    private fun setKeyWordColor(text: String) = ThemeHelper.matcherSearchAllSentence(text, keyWord)

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is ChatRoomEntity -> TYPE_CHAT_ROOM
            else -> throw IllegalArgumentException("Invalid type of data $position")
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder =
        when (viewType) {
            TYPE_CHAT_ROOM ->
                ChatRoomFilterListViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_chat_room_friends,
                        parent,
                        false
                    )
                )

            else -> throw IllegalArgumentException("Invalid view type")
        }

    // binds the list items to a view
    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when (holder.itemViewType) {
            TYPE_CHAT_ROOM ->
                (holder as ChatRoomFilterListViewHolder).bind(
                    getItem(position) as ChatRoomEntity,
                    viewModel as ChatRoomViewModel,
                    position
                )
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            when (holder) {
                is ChatRoomFilterListViewHolder -> {
                    val item = getItem(position)
                    holder.update(item)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(
            oldItem: Any,
            newItem: Any
        ): Boolean =
            when {
                oldItem is ChatRoomEntity && newItem is ChatRoomEntity -> oldItem.id == newItem.id
                else -> false
            }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(
            oldItem: Any,
            newItem: Any
        ): Boolean =
            when {
                oldItem is ChatRoomEntity && newItem is ChatRoomEntity -> oldItem == newItem
                else -> false
            }

        override fun getChangePayload(
            oldItem: Any,
            newItem: Any
        ): Any =
            when {
                oldItem is ChatRoomEntity && newItem is ChatRoomEntity -> {
                    if (oldItem.isSelected != newItem.isSelected) {
                        newItem
                    } else {
                        Any()
                    }
                }

                else -> Any()
            }
    }
}
