package tw.com.chainsea.chat.ui.adapter.entity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType
import tw.com.chainsea.ce.sdk.bean.servicenumber.type.GroupPrivilegeEnum
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.ItemChatRoomMemberBinding
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemSwipeWithActionWidthViewHolder
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemTouchHelperExtension

class MainPageMemberListAdapter(
    private val userId: String,
    private val showTransferOwnerDialog: (UserProfileEntity) -> Unit,
    private val showDesignateManagerDialog: (UserProfileEntity) -> Unit,
    private val showDeleteDialog: (UserProfileEntity) -> Unit,
    private val showCancelManagerDialog: (UserProfileEntity) -> Unit,
) : ListAdapter<UserProfileEntity, MainPageMemberListAdapter.ChatRoomMembersViewHolder>(DiffCallback()) {

    private var itemTouchHelperExtension: ItemTouchHelperExtension? = null
    private var roomType: ChatRoomType?= null
    private var privilegeMySelf: GroupPrivilegeEnum?= null

    fun setData(privilegeEnum: GroupPrivilegeEnum, type: ChatRoomType, newData: MutableList<UserProfileEntity>) {
        privilegeMySelf = privilegeEnum
        roomType = type
        submitList(newData)
    }

    /**
     * 滑塊事件監聽器
     */
    fun setItemTouchHelperExtension(itemTouchHelperExtension: ItemTouchHelperExtension) {
        this.itemTouchHelperExtension = itemTouchHelperExtension
    }

    inner class ChatRoomMembersViewHolder(
        private val binding: ItemChatRoomMemberBinding
    ): ItemSwipeWithActionWidthViewHolder<Any?>(binding.root) {

        init {
            super.setMenuViews(binding.llLeftMenu, binding.llRightMenu)
            super.setContentItemView(binding.clContentItem)
        }

        fun bind(item: UserProfileEntity) {

            item.nickName?.let {
                binding.ivAvatarIcon.loadAvatarIcon(item.avatarId, it, item.id)
                binding.tvName.text = it
            }

            when(roomType) {
                ChatRoomType.group -> {
                    when(item.groupPrivilege) {
                        GroupPrivilegeEnum.Owner -> {
                            //顯示左上擁有者圖案
                            binding.ivIdentity.visibility = View.VISIBLE
                            binding.ivIdentity.setImageResource(R.drawable.ic_owner)
                        }
                        GroupPrivilegeEnum.Manager -> {
                            //顯示左上管理者圖案
                            binding.ivIdentity.visibility = View.VISIBLE
                            binding.ivIdentity.setImageResource(R.drawable.ic_manager)
                        }
                        else -> {
                            binding.ivIdentity.visibility = View.GONE
                        }
                    }
                }
                ChatRoomType.discuss -> {
                    binding.ivIdentity.visibility = View.GONE
                }
                else -> {}
            }

            setRightMenu(userId, item)

            binding.tvTranOwner.setOnClickListener { //移轉擁有權
                itemTouchHelperExtension?.closeOpened()
                showTransferOwnerDialog.invoke(item)
            }
            binding.tvDesignate.setOnClickListener {//委派管理權
                itemTouchHelperExtension?.closeOpened()
                showDesignateManagerDialog.invoke(item)
            }
            binding.ivDelete.setOnClickListener {//刪除
                itemTouchHelperExtension?.closeOpened()
                showDeleteDialog.invoke(item)
            }
            binding.tvRemoveManagement.setOnClickListener {//取消管理權
                itemTouchHelperExtension?.closeOpened()
                showCancelManagerDialog.invoke(item)
            }

            binding.clContentItem.setOnClickListener {
                itemTouchHelperExtension?.closeOpened()
            }

            binding.root.setOnClickListener {
                itemTouchHelperExtension?.closeOpened()
            }
        }

        private fun setRightMenu(userId: String, item: UserProfileEntity) {
            if(roomType == ChatRoomType.group) {
                when (privilegeMySelf) {
                    GroupPrivilegeEnum.Owner -> setOwnerPermissions(item)
                    GroupPrivilegeEnum.Manager -> setManagerPermissions(userId, item)
                    GroupPrivilegeEnum.Common -> {
                        binding.tvTranOwner.visibility = View.GONE
                        binding.tvDesignate.visibility = View.GONE
                        binding.ivDelete.visibility = View.GONE
                        binding.tvRemoveManagement.visibility = View.GONE
                    }
                    else -> {}
                }
            } else {
                //discuss
                binding.ivDelete.visibility = if(item.id != userId) View.VISIBLE else View.GONE
            }
        }

        private fun setOwnerPermissions(profile: UserProfileEntity) {
            binding.apply {
                when (profile.groupPrivilege) {
                    GroupPrivilegeEnum.Manager -> {
                        tvTranOwner.visibility = View.VISIBLE
                        tvDesignate.visibility = View.GONE
                        tvRemoveManagement.visibility = View.VISIBLE
                        ivDelete.visibility = View.VISIBLE
                    }

                    GroupPrivilegeEnum.Common -> {
                        tvTranOwner.visibility = View.VISIBLE
                        tvDesignate.visibility = View.VISIBLE
                        ivDelete.visibility = View.VISIBLE
                        tvRemoveManagement.visibility = View.GONE
                    }
                    else -> {}
                }
            }
        }

        private fun setManagerPermissions(userId: String, profile: UserProfileEntity) {
            when (profile.groupPrivilege) {
                GroupPrivilegeEnum.Manager -> {
                    if(userId != profile.id) {
                        binding.tvTranOwner.visibility = View.GONE
                        binding.tvDesignate.visibility = View.GONE
                        binding.tvRemoveManagement.visibility = View.VISIBLE
                        binding.ivDelete.visibility = View.VISIBLE
                    }
                }

                GroupPrivilegeEnum.Common -> {
                    binding.tvTranOwner.visibility = View.GONE
                    binding.tvDesignate.visibility = View.VISIBLE
                    binding.ivDelete.visibility = View.VISIBLE
                    binding.tvRemoveManagement.visibility = View.GONE
                }
                else -> {}
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomMembersViewHolder {
        val binding = DataBindingUtil.inflate<ItemChatRoomMemberBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_chat_room_member,
            parent,
            false
        )
        return ChatRoomMembersViewHolder(binding)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ChatRoomMembersViewHolder, position: Int) {
        holder.bind(
            getItem(position)
        )
    }


    class DiffCallback : DiffUtil.ItemCallback<UserProfileEntity>() {
        override fun areItemsTheSame(oldItem: UserProfileEntity, newItem: UserProfileEntity): Boolean {
            return oldItem.id == newItem.id && oldItem.groupPrivilege == newItem.groupPrivilege
        }

        override fun areContentsTheSame(oldItem: UserProfileEntity, newItem: UserProfileEntity): Boolean {
            return oldItem == newItem
        }
    }
}