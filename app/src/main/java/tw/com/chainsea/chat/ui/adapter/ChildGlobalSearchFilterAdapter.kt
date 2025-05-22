package tw.com.chainsea.chat.ui.adapter

import android.annotation.SuppressLint
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.common.base.Strings
import tw.com.aile.sdk.bean.filter.FilterRoomType
import tw.com.aile.sdk.bean.filter.SearchFilterEntity
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.ce.sdk.bean.CustomerEntity
import tw.com.chainsea.ce.sdk.bean.GroupEntity
import tw.com.chainsea.ce.sdk.bean.PicSize
import tw.com.chainsea.ce.sdk.bean.ServiceNum
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity
import tw.com.chainsea.ce.sdk.bean.msg.MessageFlag
import tw.com.chainsea.ce.sdk.bean.msg.MessageType
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType
import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberType
import tw.com.chainsea.ce.sdk.database.DBManager
import tw.com.chainsea.ce.sdk.http.ce.model.LastMessageType
import tw.com.chainsea.ce.sdk.http.ce.model.User
import tw.com.chainsea.ce.sdk.service.AvatarService
import tw.com.chainsea.ce.sdk.service.ChatRoomService
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.base.Constant
import tw.com.chainsea.chat.databinding.ItemChatRoomFriendsBinding
import tw.com.chainsea.chat.databinding.ItemCommunityRoomBinding
import tw.com.chainsea.chat.databinding.ItemEmployeeCustomerListBinding
import tw.com.chainsea.chat.databinding.ItemServiceNumberRoomBinding
import tw.com.chainsea.chat.searchfilter.viewmodel.ChatRoomSearchViewModel
import tw.com.chainsea.chat.searchfilter.viewmodel.CommunitiesSearchViewModel
import tw.com.chainsea.chat.searchfilter.viewmodel.ContactPersonClientSearchViewModel
import tw.com.chainsea.chat.searchfilter.viewmodel.SearchFilterSharedViewModel
import tw.com.chainsea.chat.searchfilter.viewmodel.ServiceNumberSearchViewModel
import tw.com.chainsea.chat.util.NoDoubleClickListener
import tw.com.chainsea.chat.util.TextViewHelper
import tw.com.chainsea.chat.util.ThemeHelper
import tw.com.chainsea.chat.util.TimeUtil

class ChildGlobalSearchFilterAdapter(
    private val userId: String,
    private val viewModel: Any,
    val keyWord: String,
    val data: SearchFilterEntity,
    val entities: List<Any>,
    val pageType: SearchFilterSharedViewModel.SearchFilterPageType,
    val lifecycleOwner: LifecycleOwner,
    val isGreenTheme: Boolean
) : ListAdapter<Any, RecyclerView.ViewHolder>(DiffCallback()) {
    companion object {
        const val TYPE_CHAT_ROOM = 1
        const val TYPE_CONTACT_PERSON = 2
        const val TYPE_COMMUNITIES = 3
        const val TYPE_SERVICE_NUMBER = 4
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
                lastMessage.senderName?.let { senderName ->
                    builder.append(senderName).append(" : ")
                }
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

    inner class ContactPersonCustomerFilterListViewHolder(
        private val binding: ItemEmployeeCustomerListBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: Any,
            viewModel: ContactPersonClientSearchViewModel,
            position: Int
        ) {
            when (item) {
                is UserProfileEntity -> {
                    val name =
                        if (!Strings.isNullOrEmpty(item.alias) && !Strings.isNullOrEmpty(item.originName)) {
                            item.originName + "(" + item.alias + ")"
                        } else {
                            item.originName?.ifEmpty { item.name?.ifEmpty { "Unknown" } } ?: "Unknown"
                        }
                    binding.ivAvatar.loadAvatarIcon(item.avatarId, item.alias.ifEmpty { item.originName }, item.id)
                    binding.tvName.text =
                        if (keyWord.isNotEmpty()) {
                            setKeyWordColor(name)
                        } else {
                            name
                        }
                    binding.ivSlice.visibility = View.GONE
                }

                is CustomerEntity -> {
                    val name =
                        if (!Strings.isNullOrEmpty(item.customerName) && !Strings.isNullOrEmpty(item.nickName)) {
                            item.nickName + " (" + item.customerName + ") "
                        } else {
                            item.nickName?.ifEmpty { item.name?.ifEmpty { "Unknown" } } ?: "Unknown"
                        }
                    binding.ivAvatar.loadAvatarIcon(item.avatarId, name, item.id)
                    binding.tvName.text =
                        if (keyWord.isNotEmpty()) {
                            setKeyWordColor(name)
                        } else {
                            name
                        }
                    binding.ivSlice.visibility = View.VISIBLE
                }
            }

            when (pageType) {
                SearchFilterSharedViewModel.SearchFilterPageType.MESSAGE_TRANSFER,
                SearchFilterSharedViewModel.SearchFilterPageType.SHARE_IN
                -> {
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
                    binding.root.setOnClickListener {
                        viewModel.onSelectedItem(item, position)
                    }
                }

                else -> {
                    binding.root.setOnClickListener(
                        object : NoDoubleClickListener() {
                            override fun onNoDoubleClick(v: View?) {
                                viewModel.onSelectContactPersonCustomerItem(item)
                            }
                        }
                    )
                }
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

    @SuppressLint("SetTextI18n")
    inner class ChatRoomFilterListViewHolder(
        private val binding: ItemChatRoomFriendsBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private var selfNickName = ""

        fun bind(
            item: ChatRoomEntity,
            viewModel: Any
        ) {
            binding.item = item
            if (item.type.equals(ChatRoomType.discuss)) {
                item.chatRoomMember?.let {
                    binding.ivAvatar.getChatRoomMemberIdsAndLoadMultiAvatarIcon(item.chatRoomMember, item.id)
                } ?: run {
                    when (viewModel) {
                        is ChatRoomSearchViewModel -> {
                            viewModel.getChatMember(item).observe(lifecycleOwner) { members ->
                                binding.ivAvatar.getChatRoomMemberIdsAndLoadMultiAvatarIcon(members, item.id)
                            }
                        }

                        else -> {}
                    }
                }
            } else if (item.type.equals(ChatRoomType.person)) {
                val self = DBManager.getInstance().queryFriend(item.ownerId)
                selfNickName = self.alias?.ifEmpty { self.nickName?.ifEmpty { self.name?.ifEmpty { "Unknown" } } } ?: "Unknown"
                binding.ivAvatar.loadAvatarIcon(self.avatarId, selfNickName, self.id)
            } else if (item.type.equals(ChatRoomType.group)) {
                handleGroupItem(item)
            } else {
                binding.ivAvatar.loadAvatarIcon(item.avatarId, item.name ?: "", item.id)
            }
            binding.ivMute.visibility = if (item.isMute) View.VISIBLE else View.GONE
            if (item.unReadNum > 0) {
                binding.tvUnread.text = item.unReadNum.toString()
            }

            binding.tvUnread.visibility = if (item.unReadNum > 0) View.VISIBLE else View.GONE
            binding.pinIcon.visibility = if (item.isTop) View.VISIBLE else View.GONE

            updateIvSlice(item, userId)
            updateTextViews(data, item, keyWord, userId, selfNickName)

            item.lastMessage?.let {
                binding.tvTime.text = TimeUtil.getTimeShowString(it.sendTime, true)
            }

            when (pageType) {
                SearchFilterSharedViewModel.SearchFilterPageType.MESSAGE_TRANSFER,
                SearchFilterSharedViewModel.SearchFilterPageType.SHARE_IN
                -> {
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
                        when (viewModel) {
                            is ChatRoomSearchViewModel -> viewModel.onSelectTransferItem(item)
                            is CommunitiesSearchViewModel -> viewModel.onSelectChatRoomItem(item, data.roomType == FilterRoomType.NEWS)
                            is ServiceNumberSearchViewModel -> viewModel.onSelectedTransferItem(item)
                        }
                    }
                }

                else -> {
                    binding.root.setOnClickListener(
                        object : NoDoubleClickListener() {
                            override fun onNoDoubleClick(v: View?) {
                                when (viewModel) {
                                    is ChatRoomSearchViewModel -> viewModel.onSelectChatRoomItem(item, data.roomType == FilterRoomType.NEWS)
                                    is CommunitiesSearchViewModel -> viewModel.onSelectChatRoomItem(item, data.roomType == FilterRoomType.NEWS)
                                    is ServiceNumberSearchViewModel -> viewModel.onSelectServiceNumberItem(item, data.roomType == FilterRoomType.NEWS)
                                }
                            }
                        }
                    )
                }
            }

            binding.executePendingBindings()
        }

        private fun updateTextViews(
            data: SearchFilterEntity,
            item: ChatRoomEntity,
            keyWord: String,
            userId: String,
            nickName: String = ""
        ) {
            val nameText =
                when {
                    !item.serviceNumberType.equals(ServiceNumberType.BOSS) && item.type == ChatRoomType.serviceMember -> // 服務號成員聊天室
                        binding.root.context.getString(R.string.text_service_number_member_title_format, item.serviceNumberName)

                    item.serviceNumberType.equals(ServiceNumberType.BOSS) && item.type == ChatRoomType.serviceMember && item.serviceNumberOwnerId.equals(userId) -> // 商務號助理聊天室
                        binding.root.context.getString(R.string.text_service_number_member_boss_owner_title_format, item.serviceNumberName)

                    item.serviceNumberType.equals(ServiceNumberType.BOSS) && item.type == ChatRoomType.serviceMember && !item.serviceNumberOwnerId.equals(userId) -> // 商務號秘書成員聊天室
                        binding.root.context.getString(R.string.text_service_number_member_title_format, item.serviceNumberName)

                    item.type == ChatRoomType.services && item.serviceNumberType == ServiceNumberType.BOSS && item.serviceNumberOwnerId == userId -> setKeyWordColor(item.name) // 商務號擁有者客戶聊天室
                    item.type == ChatRoomType.services -> setKeyWordColor("${item.name}@${item.serviceNumberName}")

                    item.type.equals(ChatRoomType.discuss) || item.type.equals(ChatRoomType.group) -> {
                        if (data.roomType == FilterRoomType.NEWS) { // 消息聊天室名稱不需要有關鍵字顏色
                            item.chatRoomMember?.let {
                                TextViewHelper.getHandledTitle(item.name, it.size, true)
                            } ?: run {
                                TextViewHelper.getHandledTitle(item.name, item.memberIds.size, true)
                            }
                        } else {
                            val spannableString = setKeyWordColor(TextViewHelper.getHandledTitleExcludeNumber(item.name, true))
                            val builder = SpannableStringBuilder(spannableString)
                            if (item.type.equals(ChatRoomType.group)) {
                                val group = DBManager.getInstance().queryGroupInfo(item.id)
                                group?.let {
                                    it.memberIds?.let { ids ->
                                        builder.append("(${ids.size})")
                                    }
                                }
                            } else {
                                item.chatRoomMember?.let {
                                    builder.append("(${it.size})")
                                } ?: run {
                                    builder.append("(${item.memberIds.size})")
                                }
                            }
                        }
                    }

                    else -> {
                        if (data.roomType == FilterRoomType.NEWS) {
                            if (item.type.equals(ChatRoomType.person)) nickName else item.name
                        } else {
                            if (keyWord.isNotEmpty()) {
                                setKeyWordColor(if (item.type.equals(ChatRoomType.person)) nickName else item.name)
                            } else {
                                if (item.type.equals(ChatRoomType.person)) {
                                    nickName
                                } else {
                                    item.name
                                }
                            }
                        }
                    }
                }

            val lastMsgText =
                when (data.roomType) {
                    FilterRoomType.NEWS -> {
                        if (item.isSearchMultipleMessage) {
                            // 顯示多則消息不需設定關鍵字調色
                            item.lastMessage.getContent(item.searchMessageCount)
                        } else {
                            if (item.lastMessage.type == MessageType.AT) { // 針對AT訊息format
                                val builder = SpannableStringBuilder()
                                val content = item.lastMessage.getContent(item.searchMessageCount)
                                val atName = ChatRoomService.getAtContent(item.lastMessage.content).toString().replace(content, "")
                                builder.append(atName).append(setKeyWordColor(content))
                            } else {
                                setKeyWordColor(item.lastMessage.getContent(item.searchMessageCount))
                            }
                        }
                    }

                    FilterRoomType.FRIENDS -> item.lastMessage?.let { getItemContent(it) }
                    else ->
                        item.lastMessage?.let {
                            if (keyWord.isNotEmpty()) {
                                when (it.type) {
                                    MessageType.TEXT,
                                    MessageType.AT
                                    -> {
                                        if (it.content.contains(keyWord)) {
                                            setKeyWordColor(it.content)
                                        } else {
                                            checkItemContent(item, it)
                                        }
                                    }

                                    else -> {
                                        checkItemContent(item, it)
                                    }
                                }
                            } else {
                                checkItemContent(item, it)
                            }
                        }
                }

            if (data.roomType == FilterRoomType.SERVICE_NUMBER_CHATROOM) {
                binding.apply {
                    tvName.visibility = View.GONE
                    tvLastMsg.visibility = View.GONE
                    tvTime.visibility = View.GONE
                    tvUnread.visibility = View.GONE
                    ivSlice.visibility = View.GONE
                    group1.visibility = View.VISIBLE
                    customerChatRoomTitle.text = nameText
                }
            } else {
                binding.tvName.text = nameText
                binding.tvLastMsg.text = lastMsgText
            }
        }

        private fun checkItemContent(
            item: ChatRoomEntity,
            message: MessageEntity
        ) = if (item.type == ChatRoomType.group || item.type == ChatRoomType.discuss) {
            if (keyWord.isNotEmpty()) {
                setKeyWordColor(message.content)
            } else {
                getItemContent(message)
            }
        } else {
            getItemContent(message)
        }

        private fun updateIvSlice(
            item: ChatRoomEntity,
            userId: String
        ) {
            val isGreenTheme = ThemeHelper.isGreenTheme()
            val imageResource =
                when {
                    item.type.equals(ChatRoomType.services) && item.serviceNumberOpenType.contains("O") -> {
                        val customer = DBManager.getInstance().queryCustomer(item.ownerId)
                        customer?.let {
                            if (it.userType.equals(User.Type.CONTACT)) {
                                R.drawable.ic_customer_15dp
                            } else {
                                R.drawable.ic_visitor_15dp
                            }
                        }
                    }

                    item.type == ChatRoomType.serviceMember && item.serviceNumberType.equals(ServiceNumberType.BOSS) && item.serviceNumberOwnerId == userId -> if (isGreenTheme) R.drawable.ic_service_member_group__green_16dp else R.drawable.ic_service_member_b
                    item.type == ChatRoomType.serviceMember -> R.drawable.ic_service_member_group_16dp
                    item.ownerId == userId && item.type == ChatRoomType.subscribe -> R.drawable.icon_subscribe_number_pink_15dp
                    item.type == ChatRoomType.person -> if (isGreenTheme) R.drawable.ic_green_self_room_16dp else R.drawable.icon_self_chat_room_16dp
                    item.type.equals(ChatRoomType.group) && data.roomType == FilterRoomType.NEWS -> R.drawable.icon_group_chat_room
                    else -> null
                }

            if (imageResource != null) {
                if (data.roomType == FilterRoomType.SERVICE_NUMBER_CHATROOM) {
                    binding.ivSlice1.visibility = View.VISIBLE
                    binding.ivSlice1.setImageResource(imageResource)
                } else {
                    binding.ivSlice.visibility = View.VISIBLE
                    binding.ivSlice.setImageResource(imageResource)
                }
            } else if (item.serviceNumberOpenType.contains("I") || item.serviceNumberOpenType.isEmpty()) {
                binding.ivSlice.visibility = View.GONE
            } else {
                if (data.roomType == FilterRoomType.SERVICE_NUMBER_CHATROOM) {
                    binding.ivSlice1.visibility = View.GONE
                } else {
                    binding.ivSlice.visibility = View.GONE
                }
            }
        }

        private fun handleGroupItem(item: ChatRoomEntity) {
            binding.ivAvatar.loadAvatarIcon(item.avatarId, item.name, item.id)
            item.lastMessage?.let {
                binding.tvLastMsg.text =
                    setKeyWordColor(
                        binding.root.context
                            .getString(R.string.text_group_chatroom_last_message_contain, it.content)
                    )
            }
        }
    }

    private fun setKeyWordColor(text: String) = ThemeHelper.matcherSearchAllSentence(text, keyWord)

    inner class CommunitiesFilterListViewHolder(
        private val binding: ItemCommunityRoomBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: GroupEntity,
            viewModel: ContactPersonClientSearchViewModel,
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

            val spannableString = setKeyWordColor(TextViewHelper.getHandledTitleExcludeNumber(item.name, true))
            val builder = SpannableStringBuilder(spannableString)

            binding.tvName.text =
                item.memberIds?.let {
                    builder.append("(${it.size})")
                }

            when (pageType) {
                SearchFilterSharedViewModel.SearchFilterPageType.MESSAGE_TRANSFER,
                SearchFilterSharedViewModel.SearchFilterPageType.SHARE_IN
                -> {
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
                        viewModel.onSelectedItem(item, position)
                    }
                }

                else -> {
                    binding.root.setOnClickListener(
                        object : NoDoubleClickListener() {
                            override fun onNoDoubleClick(v: View?) {
                                viewModel.onSelectChatRoomItem(item)
                            }
                        }
                    )
                }
            }
            binding.executePendingBindings()
        }
    }

    inner class ServiceNumberFilterListViewHolder(
        private val binding: ItemServiceNumberRoomBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: ServiceNum,
            viewModel: ServiceNumberSearchViewModel
        ) {
            binding.ivAvatar.loadAvatarIcon(item.serviceNumberAvatarId, item.name, item.serviceNumberId)
            binding.tvName.text =
                if (keyWord.isNotEmpty()) {
                    setKeyWordColor(item.name)
                } else {
                    item.name
                }

            when (pageType) {
                SearchFilterSharedViewModel.SearchFilterPageType.MESSAGE_TRANSFER,
                SearchFilterSharedViewModel.SearchFilterPageType.SHARE_IN
                -> {
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
                    binding.root.setOnClickListener(
                        object : NoDoubleClickListener() {
                            override fun onNoDoubleClick(v: View?) {
                                viewModel.onSelectedTransferItem(item)
                            }
                        }
                    )
                }

                else -> {
                    binding.root.setOnClickListener(
                        object : NoDoubleClickListener() {
                            override fun onNoDoubleClick(v: View?) {
                                viewModel.onSelectServiceNumberItem(item, data.roomType == FilterRoomType.NEWS)
                            }
                        }
                    )
                }
            }

            binding.executePendingBindings()
        }
    }

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is ChatRoomEntity -> TYPE_CHAT_ROOM
            is UserProfileEntity, is CustomerEntity -> TYPE_CONTACT_PERSON
            is GroupEntity -> TYPE_COMMUNITIES
            is ServiceNum -> TYPE_SERVICE_NUMBER
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

            TYPE_CONTACT_PERSON ->
                ContactPersonCustomerFilterListViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_employee_customer_list,
                        parent,
                        false
                    )
                )

            TYPE_COMMUNITIES ->
                CommunitiesFilterListViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_community_room,
                        parent,
                        false
                    )
                )

            TYPE_SERVICE_NUMBER ->
                ServiceNumberFilterListViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_service_number_room,
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
        when (holder.itemViewType) {
            TYPE_CHAT_ROOM ->
                (holder as ChatRoomFilterListViewHolder).bind(
                    getItem(position) as ChatRoomEntity,
                    viewModel
                )

            TYPE_CONTACT_PERSON ->
                (holder as ContactPersonCustomerFilterListViewHolder).bind(
                    getItem(position) as Any,
                    viewModel as ContactPersonClientSearchViewModel,
                    position
                )

            TYPE_COMMUNITIES ->
                (holder as CommunitiesFilterListViewHolder).bind(
                    getItem(position) as GroupEntity,
                    viewModel as ContactPersonClientSearchViewModel,
                    position
                )

            TYPE_SERVICE_NUMBER ->
                (holder as ServiceNumberFilterListViewHolder).bind(
                    getItem(position) as ServiceNum,
                    viewModel as ServiceNumberSearchViewModel
                )
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(
            oldItem: Any,
            newItem: Any
        ): Boolean =
            when {
                oldItem is ChatRoomEntity && newItem is ChatRoomEntity -> oldItem.id == newItem.id
                oldItem is UserProfileEntity && newItem is UserProfileEntity -> oldItem.id == newItem.id
                oldItem is CustomerEntity && newItem is CustomerEntity -> oldItem.id == newItem.id
                oldItem is GroupEntity && newItem is GroupEntity -> oldItem.id == newItem.id
                oldItem is ServiceNum && newItem is ServiceNum -> oldItem.serviceNumberId == newItem.serviceNumberId
                else -> false
            }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(
            oldItem: Any,
            newItem: Any
        ): Boolean =
            when {
                oldItem is ChatRoomEntity && newItem is ChatRoomEntity -> oldItem == newItem
                oldItem is UserProfileEntity && newItem is UserProfileEntity -> oldItem == newItem
                oldItem is CustomerEntity && newItem is CustomerEntity -> oldItem == newItem
                oldItem is GroupEntity && newItem is GroupEntity -> oldItem == newItem
                oldItem is ServiceNum && newItem is ServiceNum -> oldItem == newItem
                else -> false
            }
    }
}
