package tw.com.chainsea.chat.ui.adapter

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.common.collect.Lists
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.android.common.text.KeyWordHelper
import tw.com.chainsea.ce.sdk.bean.common.EnableType
import tw.com.chainsea.ce.sdk.bean.msg.MessageType
import tw.com.chainsea.ce.sdk.service.ChatRoomService
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.ItemChatRoomSearchMessageListBinding
import tw.com.chainsea.chat.util.TimeUtil
import tw.com.chainsea.chat.searchfilter.viewmodel.SearchMessageListViewModel
import tw.com.chainsea.chat.util.ThemeHelper

class SearchMessageListAdapter(
    private val viewModel: SearchMessageListViewModel,
    private val isGreenTheme: Boolean
) : ListAdapter<MessageEntity, SearchMessageListAdapter.SearchMessageListViewHolder>(DiffCallback()) {
    private var keyWord = ""

    init {
        submitList(Lists.newArrayList())
    }

    fun setData(keyWord: String, newData: MutableList<MessageEntity>) {
        this.keyWord = keyWord
        submitList(newData)
    }
    inner class SearchMessageListViewHolder(
        private val binding: ItemChatRoomSearchMessageListBinding
    ): RecyclerView.ViewHolder(binding.root){

        fun bind(entity: MessageEntity, viewModel: SearchMessageListViewModel) {
            binding.item = entity
            binding.apply {
                entity.senderName?.let { ivAvatar.loadAvatarIcon(entity.avatarId, it, entity.id) }
                if(entity.type == MessageType.AT) { //針對AT訊息format
                    val builder = SpannableStringBuilder()
                    val content = entity.getContent(entity.content)
                    val atName = ChatRoomService.getAtContent(entity.content).toString().replace(content, "")
                    tvLastMsg.text = builder.append(atName).append(
                        ThemeHelper.matcherSearchAllSentence(content, keyWord)
                    )
                }else{
                    tvLastMsg.text = ThemeHelper.matcherSearchAllSentence(entity.getContent(entity.content), keyWord)
                }
                tvTime.text = TimeUtil.getTimeShowString(entity.sendTime, true)
                tvName.text =
                    if(entity.enable == EnableType.N)
                        entity.senderName + binding.root.context.getString(R.string.text_forbidden) else entity.senderName

                root.setOnClickListener {
                    viewModel.navigateToChatRoom(entity)
                }
                executePendingBindings()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchMessageListViewHolder {
        val binding = DataBindingUtil.inflate<ItemChatRoomSearchMessageListBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_chat_room_search_message_list,
            parent,
            false
        )
        return SearchMessageListViewHolder(binding)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: SearchMessageListViewHolder, position: Int) {
        holder.bind(getItem(position), viewModel)
    }


    class DiffCallback : DiffUtil.ItemCallback<MessageEntity>() {
        override fun areItemsTheSame(oldItem: MessageEntity, newItem: MessageEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MessageEntity, newItem: MessageEntity): Boolean {
            return oldItem == newItem
        }
    }
}