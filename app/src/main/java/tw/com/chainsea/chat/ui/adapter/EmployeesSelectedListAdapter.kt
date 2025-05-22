package tw.com.chainsea.chat.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.common.collect.Lists
import tw.com.chainsea.ce.sdk.bean.GroupEntity
import tw.com.chainsea.ce.sdk.bean.ServiceNum
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType
import tw.com.chainsea.ce.sdk.customview.AvatarIcon
import tw.com.chainsea.ce.sdk.database.DBManager
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.ChatRoomFriendSelectedListBinding
import tw.com.chainsea.chat.databinding.ChatRoomSelectedListBinding
import tw.com.chainsea.chat.databinding.EmployeeSelectedListBinding
import tw.com.chainsea.chat.databinding.SubscribeServiceNumberSelectedListBinding
import tw.com.chainsea.chat.searchfilter.viewmodel.SearchFilterSharedViewModel
import tw.com.chainsea.chat.searchfilter.viewmodel.SearchFilterViewModel
import java.lang.StringBuilder

class EmployeesSelectedListAdapter(
    private val ownerId: String,
    private val viewModel: SearchFilterViewModel,
    private val isGreenTheme: Boolean
) : ListAdapter<Any, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        const val ENTITY_CROWD = 1
        const val ENTITY_USER_PROFILE = 2
        const val ENTITY_CHAT_ROOM = 3
        const val ENTITY_SERVICE_NUMBER = 4
    }

    private lateinit var pageType : SearchFilterSharedViewModel.SearchFilterPageType
    private var memberNum = 0

    init {
        submitList(Lists.newArrayList())
    }

    fun setData(newData: MutableList<Any>, type: SearchFilterSharedViewModel.SearchFilterPageType, existMemberNum: Int = 0) {
        pageType = type
        memberNum = existMemberNum
        submitList(newData)
    }
    inner class EmployeeListViewHolder(
        private val binding: EmployeeSelectedListBinding
    ): RecyclerView.ViewHolder(binding.root){

        fun bind(item: UserProfileEntity, position: Int, viewModel: SearchFilterViewModel) {
            binding.apply {
                this.item = item
                ivAvatar.loadAvatarIcon(item.avatarId, item.nickName.ifEmpty { item.alias }, item.id)
                when(pageType) {
                    SearchFilterSharedViewModel.SearchFilterPageType.CREATE_GROUP -> { //創建社團
                        if (ivRemoved.visibility == View.GONE)
                            ivRemoved.visibility = View.VISIBLE
                        ivRemoved.setImageResource(if (item.id.equals(ownerId)) R.drawable.ic_owner else if(isGreenTheme) R.drawable.ic_del2_green else R.drawable.del2)
                        ivRemoved.setOnClickListener {
                            if(!item.id.equals(ownerId))
                                viewModel.onRemoveEmployeeItem(item)
                        }
                    }
                    SearchFilterSharedViewModel.SearchFilterPageType.CREATE_DISCUSS -> { //創建多人聊天室
                        ivRemoved.visibility = if(position < memberNum) View.GONE else View.VISIBLE
                        ivRemoved.setImageResource(if(isGreenTheme) R.drawable.ic_del2_green else R.drawable.del2)
                        ivRemoved.setOnClickListener {
                            viewModel.onRemoveEmployeeItem(item)
                        }
                    }
                    SearchFilterSharedViewModel.SearchFilterPageType.TRANSFER_TO_GROUP -> { //多人聊天室轉社團
                        ivRemoved.visibility =
                            if(position < memberNum && !item.id.equals(ownerId)) View.GONE else View.VISIBLE
                        ivRemoved.setImageResource(if (item.id.equals(ownerId)) R.drawable.ic_owner else if(isGreenTheme) R.drawable.ic_del2_green else R.drawable.del2)
                        ivRemoved.setOnClickListener {
                            if(!item.id.equals(ownerId)) //如果非群主，則移除
                                viewModel.onRemoveEmployeeItem(item)
                        }
                    }
                    else -> {
                        //邀請
                        if (ivRemoved.visibility == View.GONE)
                            ivRemoved.visibility = View.VISIBLE
                        ivRemoved.setImageResource(if(isGreenTheme) R.drawable.ic_del2_green else R.drawable.del2)
                        ivRemoved.setOnClickListener {
                            viewModel.onRemoveEmployeeItem(item)
                        }
                    }
                }
                executePendingBindings()
            }
        }
    }

    inner class FriendsChatRoomListViewHolder(
        private val binding: ChatRoomFriendSelectedListBinding
    ): RecyclerView.ViewHolder(binding.root){
        fun bind(item: ChatRoomEntity, viewModel: SearchFilterViewModel) {
            binding.tvName.text = item.name
            when(item.type) {
                ChatRoomType.discuss -> getDiscussRoomAvatarAndName(binding.tvName, binding.ivAvatar, item) //組合多人聊天室頭圖
                ChatRoomType.person -> {
                    val self = DBManager.getInstance().queryFriend(item.ownerId)
                    binding.ivAvatar.loadAvatarIcon(self.avatarId, self.nickName, self.id)
                    binding.tvName.text = self.nickName
                }
                else -> binding.ivAvatar.loadAvatarIcon(item.avatarId, item.name, item.id)
            }
            binding.ivRemoved.setImageResource(if(item.id.equals(ownerId)) R.drawable.ic_owner else if(isGreenTheme) R.drawable.ic_del2_green else R.drawable.del2)
            binding.ivRemoved.setOnClickListener {
                if(!item.id.equals(ownerId))
                    viewModel.onRemoveEmployeeItem(item)
            }
            binding.executePendingBindings()
        }
    }

    private fun getDiscussRoomAvatarAndName(name: TextView, avatarIcon: AvatarIcon, item: ChatRoomEntity) {
            if(!item.isCustomName) {
                val title = StringBuilder()
                val subList = if(item.chatRoomMember.size >= 4)
                    item.chatRoomMember.subList(0, 4)
                else
                    item.chatRoomMember

                subList.forEachIndexed { index, s ->
                    val member = DBManager.getInstance().queryFriend(s.memberId)
                    member?.let { user ->
                        title.append(user.nickName)
                        if (index < subList.size - 1) {
                            title.append(",")
                        } else {
                            return@forEachIndexed
                        }
                    }
                }
                name.text = title
            }else
                name.text = item.name

            avatarIcon.getChatRoomMemberIdsAndLoadMultiAvatarIcon(item.chatRoomMember, item.id)
    }

    inner class CommunitiesListViewHolder(
        private val binding: ChatRoomSelectedListBinding
    ): RecyclerView.ViewHolder(binding.root){

        fun bind(item: GroupEntity, viewModel: SearchFilterViewModel) {
            binding.item = item
            binding.ivAvatar.loadAvatarIcon(item.avatarId, item.name, item.id)
            binding.ivRemoved.setImageResource(if(isGreenTheme) R.drawable.ic_del2_green else R.drawable.del2)
            binding.ivRemoved.setOnClickListener {
                viewModel.onRemoveEmployeeItem(item)
            }
            binding.executePendingBindings()
        }
    }

    inner class SubscribeServiceNumberListViewHolder(
        private val binding: SubscribeServiceNumberSelectedListBinding
    ): RecyclerView.ViewHolder(binding.root){
        fun bind(item: ServiceNum, viewModel: SearchFilterViewModel) {
            binding.item = item
            binding.ivAvatar.loadAvatarIcon(item.serviceNumberAvatarId, item.name, item.serviceNumberId)
            binding.ivRemoved.setImageResource(if(isGreenTheme) R.drawable.ic_del2_green else R.drawable.del2)
            binding.ivRemoved.setOnClickListener {
                viewModel.onRemoveEmployeeItem(item)
            }
            binding.executePendingBindings()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is GroupEntity -> ENTITY_CROWD
            is UserProfileEntity -> ENTITY_USER_PROFILE
            is ChatRoomEntity -> ENTITY_CHAT_ROOM
            is ServiceNum -> ENTITY_SERVICE_NUMBER
            else -> throw IllegalArgumentException("Invalid type of data $position")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            ENTITY_USER_PROFILE -> EmployeeListViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.employee_selected_list,
                    parent,
                    false
                )
            )
            ENTITY_CROWD -> CommunitiesListViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.chat_room_selected_list,
                    parent,
                    false
                ))
            ENTITY_CHAT_ROOM -> FriendsChatRoomListViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.chat_room_friend_selected_list,
                    parent,
                    false
                ))
            ENTITY_SERVICE_NUMBER -> SubscribeServiceNumberListViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.subscribe_service_number_selected_list,
                    parent,
                    false
                ))
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItem(position)) {
            is UserProfileEntity -> (holder as EmployeeListViewHolder).bind(
                getItem(position) as UserProfileEntity,
                position,
                viewModel
            )
            is GroupEntity -> (holder as CommunitiesListViewHolder).bind(
                getItem(position) as GroupEntity,
                viewModel
            )
            is ChatRoomEntity -> (holder as FriendsChatRoomListViewHolder).bind(
                getItem(position) as ChatRoomEntity,
                viewModel
            )
            is ServiceNum -> (holder as SubscribeServiceNumberListViewHolder).bind(
                getItem(position) as ServiceNum,
                viewModel
            )
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return when {
                oldItem is UserProfileEntity && newItem is UserProfileEntity -> oldItem.id == newItem.id
                oldItem is GroupEntity && newItem is GroupEntity -> oldItem.id == newItem.id
                oldItem is ChatRoomEntity && newItem is ChatRoomEntity -> oldItem.id == newItem.id
                oldItem is ServiceNum && newItem is ServiceNum -> oldItem.serviceNumberId == newItem.serviceNumberId
                else -> false
            }
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return when {
                oldItem is UserProfileEntity && newItem is UserProfileEntity -> oldItem == newItem
                oldItem is GroupEntity && newItem is GroupEntity -> oldItem == newItem
                oldItem is ChatRoomEntity && newItem is ChatRoomEntity -> oldItem == newItem
                oldItem is ServiceNum && newItem is ServiceNum -> oldItem == newItem
                else -> false
            }
        }
    }
}