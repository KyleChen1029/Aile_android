package tw.com.chainsea.chat.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.common.collect.Lists
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity
import tw.com.chainsea.ce.sdk.bean.servicenumber.type.GroupPrivilegeEnum
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.GroupMemberListBinding
import tw.com.chainsea.chat.databinding.ItemAddNewMemberBinding
import tw.com.chainsea.chat.databinding.ItemShowAllMembersBinding
import tw.com.chainsea.chat.mainpage.model.MoreMembersEntity
import tw.com.chainsea.chat.mainpage.model.NewMemberEntity
import tw.com.chainsea.chat.mainpage.viewmodel.MainPageViewModel

class MainPageMemberListAdapter(
    private val viewModel: MainPageViewModel
) : ListAdapter<Any, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        const val TYPE_MEMBER_LIST = 1
        const val TYPE_NEW_MEMBER = 2
        const val TYPE_MORE_MEMBER = 3
    }

    init {
        submitList(Lists.newArrayList())
    }

    fun setData(newData: MutableList<Any>) {
        submitList(newData)
    }

    inner class MainPageMemberListViewHolder(
        private val binding: GroupMemberListBinding
    ): RecyclerView.ViewHolder(binding.root){

        fun bind(item: UserProfileEntity, viewModel: MainPageViewModel) {
            binding.item = item
            item.nickName?.let { binding.ivAvatar.loadAvatarIcon(item.avatarId, it, item.id) }

            //顯示左上擁有者圖案
            if (item.groupPrivilege == GroupPrivilegeEnum.Owner) {
                binding.ivPrivilege.visibility = View.VISIBLE
                binding.ivPrivilege.setImageResource(R.drawable.ic_owner)
            }

            //顯示左上管理者圖案
            if (item.groupPrivilege == GroupPrivilegeEnum.Manager) {
                binding.ivPrivilege.visibility = View.VISIBLE
                binding.ivPrivilege.setImageResource(R.drawable.ic_manager)
            }

            binding.root.setOnClickListener {
                viewModel.onSelectEmployeeItem(item)
            }
            binding.executePendingBindings()
        }
    }

    inner class MainPageAddNewMemberViewHolder(
        private val binding: ItemAddNewMemberBinding
    ): RecyclerView.ViewHolder(binding.root){

        fun bind(item: NewMemberEntity, viewModel: MainPageViewModel) {

            binding.root.setOnClickListener {
                viewModel.onSelectEmployeeItem(item)
            }
            binding.executePendingBindings()
        }
    }

    inner class MainPageShowAllMembersViewHolder(
        private val binding: ItemShowAllMembersBinding
    ): RecyclerView.ViewHolder(binding.root){

        fun bind(item: MoreMembersEntity, viewModel: MainPageViewModel) {
            binding.apply {
                ivShowAll.setColorFilter(ContextCompat.getColor(viewModel.getApplication(), R.color.loading_background))
                ivShowAll.loadAvatarIcon(item.avatarId, item.userName, null)
                tvMembersLeftCount.text = item.memberLeftCount
                root.setOnClickListener {
                    viewModel.onShowAllMembersClick()
                }
                executePendingBindings()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            TYPE_MEMBER_LIST -> MainPageMemberListViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.group_member_list,
                    parent,
                    false
                )
            )
            TYPE_NEW_MEMBER -> MainPageAddNewMemberViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_add_new_member,
                    parent,
                    false
                )
            )
            TYPE_MORE_MEMBER -> MainPageShowAllMembersViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_show_all_members,
                    parent,
                    false
                )
            )
            else -> throw IllegalArgumentException("MainPageMemberListAdapter Invalid view type")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)) {
            is UserProfileEntity -> TYPE_MEMBER_LIST
            is NewMemberEntity -> TYPE_NEW_MEMBER
            is MoreMembersEntity -> TYPE_MORE_MEMBER
            else -> throw IllegalArgumentException("MainPageMemberListAdapter Invalid type of data $position")
        }
    }
    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        return when(holder.itemViewType) {
            TYPE_MEMBER_LIST -> (holder as MainPageMemberListViewHolder).bind(
                getItem(position) as UserProfileEntity,
                viewModel
            )
            TYPE_NEW_MEMBER -> (holder as MainPageAddNewMemberViewHolder).bind(
                getItem(position) as NewMemberEntity,
                viewModel
            )
            TYPE_MORE_MEMBER -> (holder as MainPageShowAllMembersViewHolder).bind(
                getItem(position) as MoreMembersEntity,
                viewModel
            )
            else -> {}
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return when {
                oldItem is UserProfileEntity && newItem is UserProfileEntity -> oldItem.id == newItem.id
                oldItem is NewMemberEntity && newItem is NewMemberEntity -> oldItem.roomId == newItem.roomId
                oldItem is MoreMembersEntity && newItem is MoreMembersEntity -> oldItem.memberLeftCount == newItem.memberLeftCount
                else -> true
            }
        }
        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return when {
                oldItem is UserProfileEntity && newItem is UserProfileEntity -> oldItem == newItem
                oldItem is NewMemberEntity && newItem is NewMemberEntity -> oldItem == newItem
                oldItem is MoreMembersEntity && newItem is MoreMembersEntity -> oldItem == newItem
                else -> true
            }
        }
    }
}