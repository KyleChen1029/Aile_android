package tw.com.chainsea.chat.view.roomList.mainRoomList

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.common.base.Strings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tw.com.chainsea.android.common.json.JsonHelper
import tw.com.chainsea.android.common.text.StringHelper
import tw.com.chainsea.ce.sdk.bean.InputLogBean
import tw.com.chainsea.ce.sdk.bean.account.UserType
import tw.com.chainsea.ce.sdk.bean.msg.MessageFlag
import tw.com.chainsea.ce.sdk.bean.msg.MessageType
import tw.com.chainsea.ce.sdk.bean.msg.SourceType
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType
import tw.com.chainsea.ce.sdk.bean.room.JoinToChatRoom
import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberStatus
import tw.com.chainsea.ce.sdk.database.DBManager
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.http.ce.model.User
import tw.com.chainsea.ce.sdk.service.UserProfileService
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack
import tw.com.chainsea.ce.sdk.service.type.ChatRoomSource
import tw.com.chainsea.ce.sdk.service.type.RefreshSource
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.base.Constant
import tw.com.chainsea.chat.config.BundleKey
import tw.com.chainsea.chat.config.CallStatus
import tw.com.chainsea.chat.databinding.ItemAddNewChatRoomBinding
import tw.com.chainsea.chat.databinding.ItemRoomRecentBinding
import tw.com.chainsea.chat.ui.activity.ChatActivity
import tw.com.chainsea.chat.ui.activity.ChatNormalActivity
import tw.com.chainsea.chat.util.ContentUtil.getFormatContent
import tw.com.chainsea.chat.util.IntentUtil
import tw.com.chainsea.chat.util.TextViewHelper
import tw.com.chainsea.chat.util.TimeUtil
import tw.com.chainsea.chat.util.UnreadUtil

class RoomListAdapter(
    private val isGreenTheme: Boolean,
    private val isFromServiceNumberListPage: Boolean = false
) : ListAdapter<Any, RecyclerView.ViewHolder>(RoomListAdapterDiffCallBack()) {
    private var roomListAdapterInterface: RoomListAdapterInterface? = null
    private var roomListClickInterface: RoomListClickInterface? = null
    private var selfUserId: String = ""

    companion object {
        const val VIEW_TYPE_ITEM = 1
        const val VIEW_TYPE_BUTTON = 2
    }

    fun setData(
        userId: String,
        chatRoomList: List<Any>
    ) {
        selfUserId = userId
        submitList(chatRoomList)
    }

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is JoinToChatRoom -> VIEW_TYPE_BUTTON
            is ChatRoomEntity -> VIEW_TYPE_ITEM
            else -> throw IllegalArgumentException("Invalid type of data $position")
        }

    override fun getItemCount(): Int = currentList.size

    fun setRoomListAdapterInterface(roomListAdapterInterface: RoomListAdapterInterface) {
        this.roomListAdapterInterface = roomListAdapterInterface
    }

    fun setRoomListClickInterface(roomListClickInterface: RoomListClickInterface) {
        this.roomListClickInterface = roomListClickInterface
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder =
        when (viewType) {
            VIEW_TYPE_ITEM ->
                RoomViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_room_recent,
                        parent,
                        false
                    )
                )
            VIEW_TYPE_BUTTON ->
                AddNewChatRoomViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_add_new_chat_room,
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
            VIEW_TYPE_ITEM -> {
                holder as RoomViewHolder
                // 如果是服務號頁面，則不顯示左右滑
                if (isFromServiceNumberListPage) {
                    holder.binding.layoutSwip.isCanRightSwipe = false
                    holder.binding.layoutSwip.isCanLeftSwipe = false
                } else {
                    holder.binding.layoutSwip.isCanRightSwipe = true
                    holder.binding.layoutSwip.isCanLeftSwipe = true
                }
                val chatRoomEntity = getItem(position) as ChatRoomEntity
                val selfUserId = TokenPref.getInstance(holder.itemView.context).userId
                val roomType = if (chatRoomEntity.roomType != ChatRoomType.undef) chatRoomEntity.roomType else chatRoomEntity.type

                holder.binding.clContentCell.setBackgroundResource(R.drawable.selector_item_list)
                // 分各種聊天室設定 最後在設定共同的
                when (roomType) {
                    ChatRoomType.person -> holder.bindPerson(chatRoomEntity)
                    ChatRoomType.discuss -> holder.bindDiscuss(chatRoomEntity)
                    ChatRoomType.provisional -> holder.bindProvisional(chatRoomEntity)
                    ChatRoomType.group -> holder.bindGroup(chatRoomEntity)
                    ChatRoomType.bossSecretary -> holder.bindBossSecretary(chatRoomEntity)
                    ChatRoomType.subscribe -> holder.bindSubscribe(chatRoomEntity, selfUserId)
                    ChatRoomType.services -> holder.bindServices(chatRoomEntity, position)
                    ChatRoomType.broadcast -> holder.bindBroadcast()
                    ChatRoomType.friend -> holder.bindFriend(chatRoomEntity)
                    ChatRoomType.system -> holder.bindSystemUser(chatRoomEntity)
                    ChatRoomType.serviceMember -> holder.bindServiceNumberMemberRoom(chatRoomEntity)
                    else -> {
                        holder.bindOther(chatRoomEntity)
                    }
                }

                holder.bindNormal(chatRoomEntity, selfUserId)
            }
            VIEW_TYPE_BUTTON -> (holder as AddNewChatRoomViewHolder).bind()
        }
    }

    inner class AddNewChatRoomViewHolder(
        private val binding: ItemAddNewChatRoomBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.joinToChat.setOnClickListener {
                roomListAdapterInterface?.joinChatRoom()
            }
        }
    }

    inner class RoomViewHolder(
        val binding: ItemRoomRecentBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        // 自己的聊天室
        fun bindPerson(chatRoomEntity: ChatRoomEntity) {
            // 左滑
            binding.ivDelete.visibility = View.GONE
            binding.ivMute.visibility = View.VISIBLE

            val entity = DBManager.getInstance().queryFriend(chatRoomEntity.ownerId)
            entity?.let {
                binding.tvName.text = TextViewHelper.setLeftImage(binding.root.context, it.nickName, if (isGreenTheme) R.drawable.ic_green_self_room_16dp else R.drawable.icon_self_chat_room_20dp)
                binding.civIcon.loadAvatarIcon(it.avatarId, it.nickName, it.id)
            }
        }

        // 好友聊天室
        fun bindFriend(entity: ChatRoomEntity) {
            var avatarId: String? = null
            var name: String? = null
            var friendId: String? = null
            entity.memberIds?.let { memberIds ->
                memberIds.firstOrNull { id -> id != selfUserId }.let { member ->
                    val user = DBManager.getInstance().queryFriend(member)
                    user?.let {
                        name = it.alias?.ifEmpty { it.nickName?.ifEmpty { it.name?.ifEmpty { entity.name } } }
                        avatarId = it.avatarId
                        friendId = it.id
                    } ?: run {
                        name = entity.name
                    }
                }
            } ?: run {
                name = entity.name
            }
            binding.tvName.text = name
            binding.civIcon.loadAvatarIcon(avatarId ?: entity.avatarId, name ?: entity.name, friendId ?: entity.id)
            // 左滑
            binding.ivDelete.visibility = View.VISIBLE
            binding.ivMute.visibility = View.VISIBLE
        }

        // 多人聊天室
        fun bindDiscuss(chatRoomEntity: ChatRoomEntity) {
            if (chatRoomEntity.chatRoomMember == null || chatRoomEntity.chatRoomMember.size == 0) {
                // 有時候 db 的 chatRoomMember 會消失，之後再找原因，先用此方法代替 再去打一次 api 拿資料
                roomListAdapterInterface?.getChatMember(
                    chatRoomEntity.id,
                    chatRoomEntity.isCustomName
                )
                return
            } else {
                binding.civIcon.getChatRoomMemberIdsAndLoadMultiAvatarIcon(chatRoomEntity.chatRoomMember, chatRoomEntity.id)
            }
            // 左滑
            binding.ivDelete.visibility = View.VISIBLE
            binding.ivMute.visibility = View.VISIBLE
            if (!chatRoomEntity.isCustomName) {
                TextViewHelper.setDiscussTitle(binding.tvName, chatRoomEntity.chatRoomMember)
            } else {
                chatRoomEntity.chatRoomMember?.let {
                    TextViewHelper.setDiscussTitle(binding.tvName, chatRoomEntity.name, chatRoomEntity.chatRoomMember.size, true)
                }
            }
        }

        // 臨時成員聊天室
        fun bindProvisional(chatRoomEntity: ChatRoomEntity) {
            // 左滑
            binding.ivDelete.visibility = View.GONE
            binding.ivMute.visibility = View.GONE
            binding.tvName.text = binding.root.context.getString(R.string.text_service_number_search_title_format, chatRoomEntity.name, chatRoomEntity.serviceNumberName)
            binding.civIcon.loadAvatarIcon(chatRoomEntity.avatarId, chatRoomEntity.name, chatRoomEntity.id)
        }

        // 群組聊天室
        fun bindGroup(chatRoomEntity: ChatRoomEntity) {
            CoroutineScope(Dispatchers.IO).launch {
                var memberCount = 0
                if (chatRoomEntity.memberIds.size <= 0) {
                    val groupInfo = DBManager.getInstance().queryGroupInfo(chatRoomEntity.id)
                    groupInfo?.let {
                        memberCount = it.memberIds?.size ?: -1
                    }
                } else {
                    memberCount = chatRoomEntity.memberIds.size
                }
                CoroutineScope(Dispatchers.Main).launch {
                    binding.tvName.text = TextViewHelper.getTitleWithIcon(binding.root.context, chatRoomEntity.name, memberCount, R.drawable.icon_group_chat_room)
                    binding.civIcon.loadAvatarIcon(chatRoomEntity.avatarId, chatRoomEntity.name, chatRoomEntity.id)
                }
            }
        }

        // 商務號秘書群聊天室
        fun bindBossSecretary(chatRoomEntity: ChatRoomEntity) {
            // 左滑
            binding.ivDelete.visibility = View.VISIBLE
            binding.ivMute.visibility = View.VISIBLE

            binding.tvName.text = TextViewHelper.setLeftImage(binding.root.context, binding.root.context.getString(R.string.text_service_number_member_boss_owner_title_format, chatRoomEntity.serviceNumberName), if (isGreenTheme)R.drawable.ic_service_member_group__green_16dp else R.drawable.ic_service_member_b)

            // 要用服務號的頭圖
            binding.civIcon.loadAvatarIcon(chatRoomEntity.serviceNumberAvatarId, chatRoomEntity.name, chatRoomEntity.id)
        }

        // 服務號成員聊天室
        fun bindServiceNumberMemberRoom(chatRoomEntity: ChatRoomEntity) {
            // 左滑
            binding.ivDelete.visibility = View.VISIBLE
            binding.ivMute.visibility = View.VISIBLE

            binding.tvName.text = TextViewHelper.setLeftImage(binding.root.context, chatRoomEntity.name + "的服務成員", if (isGreenTheme) R.drawable.ic_service_member_green_16dp else R.drawable.ic_service_member_group_16dp)

            // 要用服務號的頭圖
            binding.civIcon.loadAvatarIcon(chatRoomEntity.serviceNumberAvatarId, chatRoomEntity.name, chatRoomEntity.id)
        }

        // 內部服務號聊天室
        fun bindSubscribe(
            chatRoomEntity: ChatRoomEntity,
            selfUserId: String
        ) {
            // 左滑
            binding.ivDelete.visibility = View.VISIBLE
            binding.ivMute.visibility = View.VISIBLE

            if (chatRoomEntity.ownerId == selfUserId) {
                binding.tvName.text = TextViewHelper.setLeftImage(binding.root.context, chatRoomEntity.name, R.drawable.icon_subscribe_number_pink_15dp)
            }
            binding.civIcon.loadAvatarIcon(chatRoomEntity.avatarId, chatRoomEntity.name, chatRoomEntity.id)
        }

        // 服務號聊天室
        fun bindServices(
            chatRoomEntity: ChatRoomEntity,
            position: Int
        ) {
            binding.civIcon.loadAvatarIcon(chatRoomEntity.avatarId, chatRoomEntity.name, chatRoomEntity.id)

            // 服務號列表
            if (isFromServiceNumberListPage || chatRoomEntity.listClassify != ChatRoomSource.MAIN) {
                val name: String = chatRoomEntity.getServicesNumberTitle(selfUserId)
                binding.tvName.text = name
                UserProfileService.getProfileIsEmployee(
                    binding.root.context,
                    chatRoomEntity.ownerId,
                    object : ServiceCallBack<UserType, RefreshSource> {
                        override fun complete(
                            type: UserType,
                            refreshSource: RefreshSource
                        ) {
                            when (type) {
                                UserType.VISITOR ->
                                    binding.tvName.text =
                                        TextViewHelper.setLeftImage(
                                            binding.root.context,
                                            name,
                                            R.drawable.ic_visitor_15dp
                                        )

                                UserType.CONTACT ->
                                    binding.tvName.text =
                                        TextViewHelper.setLeftImage(
                                            binding.root.context,
                                            name,
                                            R.drawable.ic_customer_15dp
                                        )
                                else -> {}
                            }
                        }

                        override fun error(message: String) {
                            binding.tvName.text =
                                TextViewHelper.setLeftImage(
                                    binding.root.context,
                                    name,
                                    R.drawable.ic_customer_15dp
                                )
                        }
                    }
                )
                binding.txtWaitTransfer.visibility = if (chatRoomEntity.isTransferFlag) View.VISIBLE else View.GONE
                binding.clContentCell.setBackgroundResource(
                    if (ServiceNumberStatus.TIME_OUT == chatRoomEntity.serviceNumberStatus) {
                        R.drawable.selector_item_list_timeout
                    } else if (isGreenTheme) {
                        R.drawable.selector_item_list_green
                    } else {
                        R.drawable.selector_item_list
                    }
                )
            } else {
                binding.txtWaitTransfer.visibility = View.GONE
                // 聊天室列表
                // 左滑, 客戶聊天室or訪客聊天室不顯示刪除功能
                binding.ivDelete.visibility = if (chatRoomEntity.serviceNumberOwnerId != selfUserId) View.VISIBLE else View.GONE
                binding.ivMute.visibility = View.VISIBLE

                val customerEntity = DBManager.getInstance().queryCustomer(chatRoomEntity.ownerId)
                customerEntity?.let {
                    val leftDrawable =
                        if (customerEntity.userType == User.Type.VISITOR) {
                            // 訪客
                            R.drawable.ic_visitor_15dp
                        } else {
                            // 有登入
                            R.drawable.ic_customer_15dp
                        }
                    binding.tvName.text = TextViewHelper.setLeftImage(binding.root.context, chatRoomEntity.name, leftDrawable)
                } ?: run {
                    if (!isFromServiceNumberListPage) {
                        // 第一次進線有聊天室 但 db 沒存
                        roomListAdapterInterface?.getCustomer(position)
                    }
                }
            }
        }

        // 群發
        fun bindBroadcast() {
        }

        fun bindSystemUser(chatRoomEntity: ChatRoomEntity) {
            // 左滑
            binding.ivDelete.visibility = View.VISIBLE
            binding.ivMute.visibility = View.VISIBLE

            val systemUserAvatarId = TokenPref.getInstance(binding.root.context).systemUserAvatarId
            val systemUserName = TokenPref.getInstance(binding.root.context).systemUserName
            val systemUserId = TokenPref.getInstance(binding.root.context).systemUserId
            systemUserAvatarId?.let {
                binding.tvName.text = systemUserName ?: chatRoomEntity.name
                binding.civIcon.loadAvatarIcon(
                    it,
                    systemUserName ?: chatRoomEntity.name,
                    systemUserId ?: chatRoomEntity.id
                )
            } ?: run {
                chatRoomEntity.name?.let {
                    binding.tvName.text = it
                    binding.civIcon.loadAvatarIcon(chatRoomEntity.avatarId, it, chatRoomEntity.id)
                }
            }
        }

        // 其他的聊天室
        fun bindOther(chatRoomEntity: ChatRoomEntity) {
            // 左滑
            binding.ivDelete.visibility = View.VISIBLE
            binding.ivMute.visibility = View.VISIBLE

            chatRoomEntity.name?.let {
                binding.tvName.text = it
                binding.civIcon.loadAvatarIcon(chatRoomEntity.avatarId, it, chatRoomEntity.id)
            }
        }

        fun bindNormal(
            chatRoomEntity: ChatRoomEntity,
            selfUserId: String
        ) {
            binding.civSmallIcon.visibility = if (chatRoomEntity.isTop) View.VISIBLE else View.GONE
            binding.ivTop.setImageResource(if (chatRoomEntity.isTop) R.drawable.ic_no_top else R.drawable.ic_top)
            binding.ivMute.setImageResource(if (chatRoomEntity.isMute) R.drawable.amplification else R.drawable.not_remind)
            binding.ivRemind.visibility = if (chatRoomEntity.isMute) View.VISIBLE else View.GONE

            val sourceType = DBManager.getInstance().querySourceTypeFromLastMessage(chatRoomEntity.id)
            val content = DBManager.getInstance().queryContentFromLastMessage(chatRoomEntity.id)
            setLastMessage(chatRoomEntity.id, selfUserId, sourceType, content)

            // 商務號擁有者：如果沒有進線服務的未讀訊息，則顯示 N
            val unreadNumber = UnreadUtil.getUnreadText(chatRoomEntity, selfUserId, chatRoomEntity.unReadNum, chatRoomEntity.isAtMe)
            if (Strings.isNullOrEmpty(unreadNumber)) {
                binding.tvUnread.visibility = View.INVISIBLE
            } else {
                binding.tvUnread.text = unreadNumber
                if (SourceType.of(sourceType) == SourceType.LOGIN) {
                    binding.tvUnread.text = "N"
                }
                binding.tvUnread.visibility = View.VISIBLE
            }

            // 未編輯訊息
            val inputLogBean = InputLogBean.from(chatRoomEntity.unfinishedEdited)
            if (!Strings.isNullOrEmpty(inputLogBean.text)) {
                binding.tvContent.text = TextViewHelper.setLeftImage(binding.root.context, inputLogBean.text, R.drawable.edit_gray)
            }

            // 訊息發送失敗
            chatRoomEntity.failedMessage?.let {
                binding.tvContent.text = TextViewHelper.setLeftImage(binding.root.context, it.getContent(it.content), R.drawable.ic_mes_failure_14dp)
            }

            // 我的最愛
            binding.ivFavourite.visibility = if (chatRoomEntity.isFavourite) View.VISIBLE else View.INVISIBLE

            // 左滑未讀
            binding.tvSetupUnreadTag.text = StringHelper.autoNewLine(binding.root.context, R.string.alert_notes, if (chatRoomEntity.unReadNum == 0) R.string.room_cell_swipe_menu_setup_unread else R.string.room_cell_swipe_menu_setup_read)
            binding.tvSetupUnreadTag.setBackgroundColor(if (chatRoomEntity.unReadNum == 0) -0xa59dd else -0x774e22)

            binding.clContentItem.setOnClickListener {
                it.isEnabled = false
                openChatRoom(chatRoomEntity, it)
            }
            binding.ivTop.setOnClickListener { pinTop(chatRoomEntity) }
            binding.ivMute.setOnClickListener { mute(chatRoomEntity) }
            binding.tvSetupUnreadTag.setOnClickListener { setupUnread(chatRoomEntity) }
            binding.ivDelete.setOnClickListener { deleteRoom(chatRoomEntity.id) }
        }

        // 導航至 Chat Room
        private fun openChatRoom(
            chatRoomEntity: ChatRoomEntity,
            view: View
        ) {
            roomListClickInterface?.onOpenChat(view)
            val bundle = bundleOf(BundleKey.EXTRA_SESSION_ID.key() to chatRoomEntity.id)
            if (chatRoomEntity.type == ChatRoomType.services ||
                chatRoomEntity.type == ChatRoomType.subscribe ||
                chatRoomEntity.type == ChatRoomType.serviceMember
            ) {
                IntentUtil.startIntent(binding.root.context, ChatActivity::class.java, bundle)
            } else {
                IntentUtil.startIntent(binding.root.context, ChatNormalActivity::class.java, bundle)
            }
        }

        // 置頂 並重新排序&顯示小圖釘
//        private fun notifyPinTop(roomId: String) = CoroutineScope(Dispatchers.IO).launch {
//            val sortList: MutableList<ChatRoomEntity> = Lists.newArrayList()
//            currentList.forEach {
//                if (it is ChatRoomEntity) {
//                    sortList.add(it)
//                }
//            }
//            val isTop = !sortList[absoluteAdapterPosition].isTop
//            sortList[absoluteAdapterPosition].isTop = isTop
//            DBManager.getInstance().setChatRoomListItemTop(roomId, isTop)
//            sortList.sort()
//            withContext(Dispatchers.Main) {
//                val newList: MutableList<Any> = Lists.newArrayList()
//                newList.addAll(sortList)
//                submitList(newList)
//                notifyItemChanged(absoluteAdapterPosition)
//            }
//        }

        // 置頂
        private fun pinTop(chatRoomEntity: ChatRoomEntity) {
            binding.layoutSwip.resetStatus()
            roomListAdapterInterface?.pinTop(chatRoomEntity.id, chatRoomEntity.isTop)
        }

        // 靜音
        private fun mute(chatRoomEntity: ChatRoomEntity) {
            binding.layoutSwip.resetStatus()
            roomListAdapterInterface?.muteRoom(chatRoomEntity.id, chatRoomEntity.isMute)
        }

        // 設置未讀/已讀
        private fun setupUnread(chatRoomEntity: ChatRoomEntity) {
            binding.layoutSwip.resetStatus()
            roomListAdapterInterface?.setupUnread(chatRoomEntity.id, chatRoomEntity.unReadNum)
        }

        // 滑動刪除
        private fun deleteRoom(roomId: String) {
            binding.layoutSwip.resetStatus()
            roomListAdapterInterface?.deleteRoom(roomId)
        }

        // 取得最後訊息
        private fun getLastMessageContent(
            senderId: String,
            sourceType: String,
            flag: Int,
            senderName: String,
            type: String,
            content: String,
            selfUserId: String
        ): CharSequence {
            val builder = SpannableStringBuilder()
            val systemUserId = TokenPref.getInstance(binding.root.context).systemUserId
            if (selfUserId == senderId) {
                builder.append("我 : ")
            } else if (SourceType.of(sourceType) == SourceType.SYSTEM || SourceType.of(sourceType) == SourceType.LOGIN) {
                builder.append("")
            } else if (systemUserId == senderId) {
                // Aile Chat
                val systemUserName = TokenPref.getInstance(binding.root.context).systemUserName
                builder.append("${systemUserName ?: senderName} : ")
            } else {
                builder.append(senderName).append(" : ")
            }

            // 收回的訊息
            if (MessageFlag.of(flag) == MessageFlag.RETRACT) {
                builder.append(Constant.RETRACT_MSG)
            } else {
                builder.append(getFormatContent(MessageType.of(type), content))
            }

            return builder
        }

        private fun setLastMessage(
            roomId: String,
            selfUserId: String,
            sourceType: String,
            content: String?
        ) {
            if (!content.isNullOrEmpty()) {
                val type = DBManager.getInstance().queryTypeFromLastMessage(roomId)
                val senderId = DBManager.getInstance().querySenderIdFromLastMessage(roomId)
                val sendTime = DBManager.getInstance().querySendTimeFromLastMessage(roomId)
                if (MessageType.of(type) == MessageType.CALL) {
                    binding.ivPhone.visibility = View.VISIBLE
                    val jsonObject = JsonHelper.getInstance().toJsonObject(content)
                    when (jsonObject.optString("status")) {
                        CallStatus.CallEnd.name -> {
                            val callTime = jsonObject.optInt("callTime")
                            binding.tvContent.text =
                                binding.root.context.getString(
                                    R.string.text_phone_call_end,
                                    callTime / 60,
                                    callTime % 60
                                )
                        }

                        CallStatus.CallCancel.name -> {
                            binding.tvContent.text =
                                binding.root.context.getString(R.string.text_phone_call_cancel)
                        }

                        CallStatus.CallBusy.name -> {
                            if (senderId == selfUserId) {
                                binding.ivPhone.setImageResource(R.drawable.ic_phone_busy)
                                binding.tvContent.text =
                                    binding.root.context.getString(R.string.text_phone_call_busy)
                            } else {
                                binding.ivPhone.setImageResource(R.drawable.ic_phone_fill)
                                binding.tvContent.text =
                                    binding.root.context.getString(R.string.text_phone_call_busy_myself)
                            }
                        }

                        else -> binding.tvContent.text = ""
                    }
                    if (sendTime > 0) {
                        binding.tvTime.text = TimeUtil.getTimeShowString(sendTime, true)
                    }
                } else {
                    val flag = DBManager.getInstance().queryFlagFromLastMessage(roomId)
                    var sendName = DBManager.getInstance().querySenderNameFromLastMessage(roomId)
                    if (sendName == "") {
                        val userProfile = DBManager.getInstance().queryUser(senderId)
                        userProfile?.let {
                            sendName = userProfile.nickName ?: ""
                        }
                    }
                    binding.tvContent.text = getLastMessageContent(senderId, sourceType, flag, sendName, type, content, selfUserId)
                    binding.ivPhone.visibility = View.GONE
                }
                binding.tvTime.text = TimeUtil.getTimeShowString(sendTime, true)
                binding.tvTime.visibility = View.VISIBLE
            } else {
                binding.tvContent.text = ""
                binding.tvTime.visibility = View.GONE
                binding.ivPhone.visibility = View.GONE
            }
        }
    }
}

class RoomListAdapterDiffCallBack : DiffUtil.ItemCallback<Any>() {
    override fun areItemsTheSame(
        oldItem: Any,
        newItem: Any
    ): Boolean {
        if (oldItem is ChatRoomEntity && newItem is ChatRoomEntity) {
            var isChatRoomMemberSame = true
            var isMemberIdsSame = true
            var isMemberSame = true
            if (oldItem.chatRoomMember != null && newItem.chatRoomMember != null) {
                if (oldItem.chatRoomMember.size == 0 && newItem.chatRoomMember.size > 0) {
                    isChatRoomMemberSame = false
                }
                val chatRoomMemberPairs = oldItem.chatRoomMember.zip(newItem.chatRoomMember)
                for ((item1, item2) in chatRoomMemberPairs) {
                    if (item1 != item2) {
                        isChatRoomMemberSame = false
                        break
                    }
                }
                val memberIdsPairs = oldItem.memberIds.zip(newItem.memberIds)
                for ((item1, item2) in memberIdsPairs) {
                    if (item1 != item2) {
                        isMemberIdsSame = false
                        break
                    }
                }

                val membersPairs = oldItem.members.zip(newItem.members)
                for ((item1, item2) in membersPairs) {
                    if (item1 != item2) {
                        isMemberSame = false
                        break
                    }
                }
            }
            return oldItem.id == newItem.id &&
                oldItem.weights == newItem.weights &&
                oldItem.lastMessage == newItem.lastMessage &&
                oldItem.lastMessageStr == newItem.lastMessageStr &&
                oldItem.updateTime == newItem.updateTime &&
                oldItem.isMute == newItem.isMute &&
                oldItem.unReadNum == newItem.unReadNum &&
                oldItem.isFavourite == newItem.isFavourite &&
                oldItem.isTop == newItem.isTop &&
                oldItem.name == newItem.name &&
                oldItem.isAtMe == newItem.isAtMe &&
                oldItem.dfrTime == newItem.dfrTime &&
                isChatRoomMemberSame &&
                isMemberIdsSame &&
                isMemberSame &&
                oldItem.avatarId == newItem.avatarId &&
                oldItem.isServiceNumberOwnerStop == newItem.isServiceNumberOwnerStop &&
                oldItem.deleted == newItem.deleted &&
                oldItem.isTransferFlag == newItem.isTransferFlag &&
                oldItem.transferReason == newItem.transferReason &&
                oldItem.serviceNumberStatus == newItem.serviceNumberStatus &&
                oldItem.unfinishedEdited == newItem.unfinishedEdited
        } else if (oldItem is JoinToChatRoom && newItem is JoinToChatRoom) {
            return oldItem.name == newItem.name
        } else {
            return false
        }
    }

    override fun areContentsTheSame(
        oldItem: Any,
        newItem: Any
    ): Boolean =
        if (oldItem is ChatRoomEntity && newItem is ChatRoomEntity) {
            oldItem == newItem
        } else if (oldItem is JoinToChatRoom && newItem is JoinToChatRoom) {
            oldItem == newItem
        } else {
            false
        }
}

interface RoomListAdapterInterface {
    fun getGroupChatMember(roomId: String)

    fun getChatMember(
        roomId: String,
        isCustomName: Boolean
    )

    fun getCustomer(position: Int)

    fun muteRoom(
        roomId: String,
        isMute: Boolean
    )

    fun pinTop(
        roomId: String,
        isTop: Boolean
    )

    fun deleteRoom(roomId: String)

    fun setupUnread(
        roomId: String,
        unreadNum: Int
    )

    fun joinChatRoom()

    fun getFriendRoomMember(roomId: String)
}

interface RoomListClickInterface {
    fun onOpenChat(view: View)
}
