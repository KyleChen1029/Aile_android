package tw.com.chainsea.chat.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity
import tw.com.chainsea.chat.databinding.ItemProvisionalMemberBinding


class ProvisionalMemberAdapter(private val onProvisionalMemberItem : OnProvisionalMemberItemClick) :
    ListAdapter<UserProfileEntity, ProvisionalMemberAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemProvisionalMemberBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UserProfileEntity, p: Int) {
            binding.ivAvatar.loadAvatarIcon(item.avatarId, item.nickName, item.id)
            binding.root.setOnClickListener {
                onProvisionalMemberItem.onClickEvent(item, p, binding.ivAvatar)
            }

            binding.executePendingBindings()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemProvisionalMemberBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }
}

class DiffCallback : DiffUtil.ItemCallback<UserProfileEntity>() {
    override fun areItemsTheSame(oldItem: UserProfileEntity, newItem: UserProfileEntity): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: UserProfileEntity,
        newItem: UserProfileEntity
    ): Boolean {
        return oldItem == newItem
    }

}

interface OnProvisionalMemberItemClick {
    fun onClickEvent(item: UserProfileEntity, position: Int, itemView: View)
}