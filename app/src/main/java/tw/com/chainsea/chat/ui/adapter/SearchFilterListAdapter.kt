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
import com.google.common.collect.Lists
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.ce.sdk.bean.GroupEntity
import tw.com.chainsea.ce.sdk.bean.PicSize
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity
import tw.com.chainsea.ce.sdk.bean.msg.MessageFlag
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.bean.servicenumber.AIConsultation
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberConsultEntity
import tw.com.chainsea.ce.sdk.http.ce.model.LastMessageType
import tw.com.chainsea.ce.sdk.service.AvatarService
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.base.Constant
import tw.com.chainsea.chat.databinding.EmployeeListBinding
import tw.com.chainsea.chat.databinding.ItemChatRoomFriendsBinding
import tw.com.chainsea.chat.databinding.ItemCommunityRoomBinding
import tw.com.chainsea.chat.databinding.ItemServiceNumberRoomBinding
import tw.com.chainsea.chat.databinding.LayoutAiConsultationBinding
import tw.com.chainsea.chat.searchfilter.viewmodel.ChatRoomViewModel
import tw.com.chainsea.chat.searchfilter.viewmodel.CommunityViewModel
import tw.com.chainsea.chat.searchfilter.viewmodel.ContactPersonViewModel
import tw.com.chainsea.chat.searchfilter.viewmodel.ServiceNumberConsultationViewModel
import tw.com.chainsea.chat.util.ThemeHelper
import tw.com.chainsea.chat.util.TimeUtil

class SearchFilterListAdapter(
    private val userId: String,
    private val viewModel: Any,
    private val isGreenTheme: Boolean
) : ListAdapter<Any, RecyclerView.ViewHolder>(DiffCallback()) {
    init {
        submitList(Lists.newArrayList())
    }

    companion object {
        const val TYPE_CHAT_ROOM = 1
        const val TYPE_CONTACT_PERSON = 2
        const val TYPE_COMMUNITY = 3
        const val TYPE_SERVICE_NUMBER = 4
        const val TYPE_AI_CONSULTATION = 5
    }

    lateinit var keyWord: String

    fun setData(
        keyWord: String,
        newData: MutableList<Any>
    ) {
        this.keyWord = keyWord
        submitList(newData)
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
    inner class ChatRoomListViewHolder(
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
                binding.tvLastMsg.text = getItemContent(it)
                binding.tvTime.text = TimeUtil.getTimeShowString(item.lastMessage.sendTime, true)
            }
            itemView.setBackgroundColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (item.isSelected) {
                        if (isGreenTheme) {
                            R.color.color_E8F7F0
                        } else {
                            R.color.color_F0FAFF
                        }
                    } else {
                        R.color.transparent
                    }
                )
            )
            binding.root.setOnClickListener {
                viewModel.onSelectChatRoomItem(item, position)
            }

            binding.executePendingBindings()
        }

        fun update(item: Any) {
            if (item is ChatRoomEntity) {
                itemView.setBackgroundColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        if (item.isSelected) {
                            if (isGreenTheme) {
                                R.color.color_E8F7F0
                            } else {
                                R.color.color_F0FAFF
                            }
                        } else {
                            R.color.transparent
                        }
                    )
                )
            }
        }
    }

    private fun setKeyWordColor(text: String) = ThemeHelper.matcherSearchAllSentence(text, keyWord)

    inner class CommunityChatRoomListViewHolder(
        private val binding: ItemCommunityRoomBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: GroupEntity,
            viewModel: CommunityViewModel,
            position: Int
        ) {
            binding.item = item
            AvatarService.post(
                binding.root.context,
                item.avatarId,
                PicSize.SMALL,
                binding.ivAvatar,
                R.mipmap.ic_new_head
            )

            binding.tvName.text =
                if (keyWord.isNotEmpty()) {
                    setKeyWordColor(item.name)
                } else {
                    item.name
                }

            itemView.setBackgroundColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (item.isSelected) {
                        if (isGreenTheme) {
                            R.color.color_E8F7F0
                        } else {
                            R.color.color_F0FAFF
                        }
                    } else {
                        R.color.transparent
                    }
                )
            )
            binding.root.setOnClickListener {
                viewModel.onSelectCommunityRoomItem(item, position)
            }

            binding.executePendingBindings()
        }

        fun update(item: Any) {
            if (item is GroupEntity) {
                itemView.setBackgroundColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        if (item.isSelected) {
                            if (isGreenTheme) {
                                R.color.color_E8F7F0
                            } else {
                                R.color.color_F0FAFF
                            }
                        } else {
                            R.color.transparent
                        }
                    )
                )
            }
        }
    }

    inner class ServiceNumberConsultationListViewHolder(
        private val binding: ItemServiceNumberRoomBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: ServiceNumberConsultEntity,
            viewModel: ServiceNumberConsultationViewModel
        ) {
            binding.ivAvatar.loadAvatarIcon(item.serviceNumberAvatarId, item.name, item.serviceNumberId)

            binding.tvName.text =
                if (keyWord.isNotEmpty()) {
                    setKeyWordColor(item.name)
                } else {
                    item.name
                }
            binding.tvLastMsg.text = item.description

            binding.root.setOnClickListener {
                viewModel.onSelectServiceNumberItem(keyWord, item.roomId)
            }

            binding.executePendingBindings()
        }
    }

    inner class ContactPersonListViewHolder(
        private val binding: EmployeeListBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: UserProfileEntity,
            viewModel: ContactPersonViewModel,
            position: Int
        ) {
            binding.ivAvatar.loadAvatarIcon(item.avatarId, item.nickName.ifEmpty { item.alias }, item.id)
            binding.tvName.text =
                if (keyWord.isNotEmpty()) {
                    setKeyWordColor(item.nickName)
                } else {
                    item.nickName
                }

            itemView.setBackgroundColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (item.isSelected) {
                        if (isGreenTheme) {
                            R.color.color_E8F7F0
                        } else {
                            R.color.color_F0FAFF
                        }
                    } else {
                        R.color.transparent
                    }
                )
            )
            binding.root.setOnClickListener {
                viewModel.onSelectEmployeeItem(item, position)
            }

            binding.executePendingBindings()
        }

        fun update(item: Any) {
            if (item is UserProfileEntity) {
                itemView.setBackgroundColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        if (item.isSelected) {
                            if (isGreenTheme) {
                                R.color.color_E8F7F0
                            } else {
                                R.color.color_F0FAFF
                            }
                        } else {
                            R.color.transparent
                        }
                    )
                )
            }
        }
    }

    inner class AiConsultationViewHolder(
        private val binding: LayoutAiConsultationBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: AIConsultation,
            viewModel: ServiceNumberConsultationViewModel
        ) {
            binding.ivAvatar.setImageResource(R.drawable.ic_ai_consultation)
            binding.tvName.text =
                if (keyWord.isNotEmpty()) {
                    setKeyWordColor(item.name)
                } else {
                    item.name
                }

            binding.root.setOnClickListener {
                viewModel.onSelectAiConsultation(keyWord, item.consultId ?: "")
            }

            binding.executePendingBindings()
        }
    }

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is ChatRoomEntity -> TYPE_CHAT_ROOM
            is GroupEntity -> TYPE_COMMUNITY
            is UserProfileEntity -> TYPE_CONTACT_PERSON
            is ServiceNumberConsultEntity -> TYPE_SERVICE_NUMBER
            is AIConsultation -> TYPE_AI_CONSULTATION
            else -> throw IllegalArgumentException("Invalid type of data $position")
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder =
        when (viewType) {
            TYPE_CHAT_ROOM ->
                ChatRoomListViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_chat_room_friends,
                        parent,
                        false
                    )
                )

            TYPE_COMMUNITY ->
                CommunityChatRoomListViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_community_room,
                        parent,
                        false
                    )
                )

            TYPE_SERVICE_NUMBER ->
                ServiceNumberConsultationListViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_service_number_room,
                        parent,
                        false
                    )
                )

            TYPE_CONTACT_PERSON ->
                ContactPersonListViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.employee_list,
                        parent,
                        false
                    )
                )

            TYPE_AI_CONSULTATION ->
                AiConsultationViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.layout_ai_consultation,
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
                (holder as ChatRoomListViewHolder).bind(
                    getItem(position) as ChatRoomEntity,
                    viewModel as ChatRoomViewModel,
                    position
                )

            TYPE_COMMUNITY ->
                (holder as CommunityChatRoomListViewHolder).bind(
                    getItem(position) as GroupEntity,
                    viewModel as CommunityViewModel,
                    position
                )

            TYPE_SERVICE_NUMBER ->
                (holder as ServiceNumberConsultationListViewHolder).bind(
                    getItem(position) as ServiceNumberConsultEntity,
                    viewModel as ServiceNumberConsultationViewModel
                )

            TYPE_CONTACT_PERSON ->
                (holder as ContactPersonListViewHolder).bind(
                    getItem(position) as UserProfileEntity,
                    viewModel as ContactPersonViewModel,
                    position
                )

            TYPE_AI_CONSULTATION ->
                (holder as AiConsultationViewHolder).bind(
                    getItem(position) as AIConsultation,
                    viewModel as ServiceNumberConsultationViewModel
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
                is ContactPersonListViewHolder -> {
                    val item = getItem(position)
                    holder.update(item)
                }

                is ChatRoomListViewHolder -> {
                    val item = getItem(position)
                    holder.update(item)
                }

                is CommunityChatRoomListViewHolder -> {
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
                oldItem is UserProfileEntity && newItem is UserProfileEntity -> oldItem.id == newItem.id
                oldItem is ChatRoomEntity && newItem is ChatRoomEntity -> oldItem.id == newItem.id
                oldItem is GroupEntity && newItem is GroupEntity -> oldItem.id == newItem.id
                oldItem is ServiceNumberConsultEntity && newItem is ServiceNumberConsultEntity -> oldItem.serviceNumberId == newItem.serviceNumberId
                else -> false
            }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(
            oldItem: Any,
            newItem: Any
        ): Boolean =
            when {
                oldItem is UserProfileEntity && newItem is UserProfileEntity -> oldItem == newItem
                oldItem is ChatRoomEntity && newItem is ChatRoomEntity -> oldItem == newItem
                oldItem is GroupEntity && newItem is GroupEntity -> oldItem == newItem
                oldItem is ServiceNumberConsultEntity && newItem is ServiceNumberConsultEntity -> oldItem == newItem
                else -> false
            }

        override fun getChangePayload(
            oldItem: Any,
            newItem: Any
        ): Any =
            when {
                oldItem is UserProfileEntity && newItem is UserProfileEntity -> {
                    if (oldItem.isSelected != newItem.isSelected) {
                        newItem
                    } else {
                        Any()
                    }
                }

                oldItem is ChatRoomEntity && newItem is ChatRoomEntity -> {
                    if (oldItem.isSelected != newItem.isSelected) {
                        newItem
                    } else {
                        Any()
                    }
                }

                oldItem is GroupEntity && newItem is GroupEntity -> {
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
