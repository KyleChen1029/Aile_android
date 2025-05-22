package tw.com.chainsea.chat.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.common.collect.Lists
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tw.com.chainsea.ce.sdk.bean.AvatarRecord
import tw.com.chainsea.ce.sdk.bean.RoomType
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType
import tw.com.chainsea.ce.sdk.customview.AvatarIcon
import tw.com.chainsea.ce.sdk.database.DBManager
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference
import tw.com.chainsea.ce.sdk.reference.ServiceNumberReference
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.EmployeeSearchRecordAvatarListBinding
import tw.com.chainsea.chat.searchfilter.viewmodel.SearchFilterViewModel
import java.lang.StringBuilder

class SearchEmployeesAvatarRecordListAdapter(
    private val viewModel: SearchFilterViewModel
) : ListAdapter<AvatarRecord, SearchEmployeesAvatarRecordListAdapter.SearchEmployeesAvatarRecordListViewHolder>(DiffCallback()) {
    init {
        submitList(Lists.newArrayList())
    }

    fun setData(newData: MutableList<AvatarRecord>) {
        submitList(newData)
    }

    inner class SearchEmployeesAvatarRecordListViewHolder(
        private val binding: EmployeeSearchRecordAvatarListBinding
    ): RecyclerView.ViewHolder(binding.root){

        fun bind(item: AvatarRecord, viewModel: SearchFilterViewModel) {
            when(item.type) {
                RoomType.Discuss -> {
                    CoroutineScope(Dispatchers.Main).launch {
                        getDiscussRoomAvatarAndName(binding.tvName, binding.ivAvatar, item.id)
                    }
                }
                RoomType.Crowd -> {
                    CoroutineScope(Dispatchers.Main).launch {
                        getCrowdRoomAvatarAndName(binding.tvName, binding.ivAvatar, item.id)
                    }
                }
                RoomType.ServiceOutSideChatRoom,
                RoomType.ChatRoom -> {
                    CoroutineScope(Dispatchers.Main).launch {
                        getOtherRoomAvatarAndName(binding.tvName, binding.ivAvatar, item.id)
                    }
                }
                RoomType.ServiceNumber -> {
                    CoroutineScope(Dispatchers.Main).launch {
                        getServiceNumberAvatarAndName(binding.tvName, binding.ivAvatar, item.id)
                    }
                }
                RoomType.Contact -> {
                    CoroutineScope(Dispatchers.Main).launch {
                        getContactAvatarAndName(binding.tvName, binding.ivAvatar, item.id)
                    }
                }
                RoomType.Customer -> {
                    CoroutineScope(Dispatchers.Main).launch {
                        getCustomerAvatarAndName(binding.tvName, binding.ivAvatar, item.id)
                    }
                }
                RoomType.System -> {
                    val name = TokenPref.getInstance(binding.root.context).systemUserName
                    val avatarId = TokenPref.getInstance(binding.root.context).systemUserAvatarId
                    binding.tvName.text = name
                    binding.ivAvatar.loadAvatarIcon(avatarId, name, item.id)
                }
                else -> { }
            }

            binding.root.setOnClickListener {
                viewModel.onSelectEmployeeAvatarItem(item)
            }
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchEmployeesAvatarRecordListViewHolder {
        val binding = DataBindingUtil.inflate<EmployeeSearchRecordAvatarListBinding>(
            LayoutInflater.from(parent.context),
            R.layout.employee_search_record_avatar_list,
            parent,
            false
        )
        return SearchEmployeesAvatarRecordListViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: SearchEmployeesAvatarRecordListViewHolder,
        position: Int
    ) {
        holder.bind(
            getItem(position),
            viewModel)
    }

    class DiffCallback : DiffUtil.ItemCallback<AvatarRecord>() {
        override fun areItemsTheSame(oldItem: AvatarRecord, newItem: AvatarRecord): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: AvatarRecord, newItem: AvatarRecord): Boolean {
            return oldItem == newItem
        }
    }

    fun getDiscussRoomAvatarAndName(name: TextView, avatarIcon: AvatarIcon, roomId: String?) {
        roomId?.let { roomId ->
            val entity = ChatRoomReference.getInstance().findById(roomId)
            entity?.let { item ->
                if(!item.isCustomName) {
                    val title = StringBuilder()
                    item.chatRoomMember?.let { members ->
                        val subList = if(members.size >= 4)
                            members.subList(0, 4)
                        else
                            members

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
                    }?: run {
                        val subList = if(item.memberIds.size >= 4)
                            item.memberIds.subList(0, 4)
                        else
                            item.memberIds

                        subList.forEachIndexed { index, s ->
                            val member = DBManager.getInstance().queryFriend(s)
                            member?.let { user ->
                                title.append(user.nickName)
                                if (index < subList.size - 1) {
                                    title.append(",")
                                } else {
                                    return@forEachIndexed
                                }
                            }
                        }
                    }
                    name.text = title
                }else
                    name.text = item.name

                item.chatRoomMember?.let {
                    avatarIcon.getChatRoomMemberIdsAndLoadMultiAvatarIcon(item.chatRoomMember, item.id)
                }?: run {
                    item.memberIds?.let {
                        avatarIcon.loadMultiAvatarIcon(it, roomId)
                    }?:run {
                        item.avatarId?.let {
                            avatarIcon.loadAvatarIcon(it, item.name, item.id)
                        }
                    }
                }
            }
        }
    }
    fun getCrowdRoomAvatarAndName(name: TextView, avatarIcon: AvatarIcon, roomId: String?) {
        roomId?.let {
            val entity = ChatRoomReference.getInstance().findById(it)
            entity?.let { e ->
                avatarIcon.loadAvatarIcon(e.avatarId, e.name, e.id)
                name.text = e.name
            }
        }
    }

    fun getOtherRoomAvatarAndName(name: TextView, avatarIcon: AvatarIcon, roomId: String?) {
        roomId?.let {
            val entity = ChatRoomReference.getInstance().findById(it)
            entity?.let { e ->
                e.name?.let { n ->
                    avatarIcon.loadAvatarIcon(e.avatarId, n, e.id)
                    name.text = n
                }?:run {
                    //個人聊天室name是null
                    if(e.type.equals(ChatRoomType.person)) {
                        val user = DBManager.getInstance().queryFriend(e.ownerId)
                        avatarIcon.loadAvatarIcon(user.avatarId, user.nickName, user.id)
                        name.text = user.nickName
                    }
                }
            }?:run {
                viewModel.removeAvatarItem(roomId)
            }
        }
    }
    fun getServiceNumberAvatarAndName(name: TextView, avatarIcon: AvatarIcon, id: String?) {
        id?.let {
            val entity = ServiceNumberReference.findServiceNumberById(it)
            entity.name?.let { n ->
                avatarIcon.loadAvatarIcon(entity.avatarId, n, entity.serviceNumberId)
                val title = "$n@${entity.name}"
                name.text = title
            }
        }
    }
    fun getContactAvatarAndName(name: TextView, avatarIcon: AvatarIcon, id: String?) {
        id?.let {
            val entity = DBManager.getInstance().queryFriend(it)
            entity.nickName?.let { n ->
                avatarIcon.loadAvatarIcon(entity.avatarId, n, entity.id)
                name.text = n
            }
        }
    }
    fun getCustomerAvatarAndName(name: TextView, avatarIcon: AvatarIcon, id: String?) {
        id?.let {
            val entity = DBManager.getInstance().queryCustomer(id)
            entity.nickName?.let { n ->
                avatarIcon.loadAvatarIcon(entity.avatarId, n, entity.id)
                name.text = n
            }
        }
    }
}