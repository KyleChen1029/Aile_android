package tw.com.chainsea.chat.view.contact.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.common.base.Strings
import tw.com.chainsea.ce.sdk.bean.GroupEntity
import tw.com.chainsea.ce.sdk.database.DBManager
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.ItemContactPersonGroupBinding
import tw.com.chainsea.chat.util.NoDoubleClickListener
import java.text.MessageFormat

class GroupListAdapter :
    BaseContactAdapter<GroupEntity, GroupListAdapter.GroupItemViewHolder>(GroupListDiffCallback()) {

    fun setData(data: List<GroupEntity>) {
        submitList(data)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupItemViewHolder {
        val binding = ItemContactPersonGroupBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GroupItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GroupItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class GroupItemViewHolder(private val binding: ItemContactPersonGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(groupEntity: GroupEntity) {
            if (groupEntity.isAddHardCode()) {
                binding.civIcon.setImageResource(R.drawable.icon_create_group)
                binding.tvName.text = groupEntity.name
                binding.tvMood.text = "與好友建立社團"
                binding.tvNumber.text = ""
                binding.ivHome.visibility = View.GONE
                binding.ivCall.visibility = View.GONE
                binding.clContentItem.setOnClickListener(object : NoDoubleClickListener() {
                    override fun onNoDoubleClick(v: View) {
                        onContactPersonListener?.onCreateGroupItemClick()
                    }
                })
                binding.ivHome.setOnClickListener(null)
                binding.ivCall.setOnClickListener(null)
            } else {
                binding.tvName.text = groupEntity.name
                binding.tvNumber.text =
                    MessageFormat.format("({0})", groupEntity.memberIds?.size)
                binding.civIcon.loadAvatarIcon(
                    groupEntity.avatarId,
                    groupEntity.name,
                    groupEntity.id
                )

                val builder = java.lang.StringBuilder()
                groupEntity.memberIds?.let {
                    for (id in it) {
                        val user = DBManager.getInstance().queryUser(id)
                        user?.let { userEntity ->
                            builder.append(
                                if (Strings.isNullOrEmpty(userEntity.alias)) userEntity.nickName else userEntity.alias
                            )
                            builder.append(", ")
                        }
                    }
                }

                binding.tvMood.text = builder
                binding.ivHome.visibility = View.VISIBLE
                binding.ivCall.visibility = View.GONE
                binding.clContentItem.setOnClickListener(object : NoDoubleClickListener() {
                    override fun onNoDoubleClick(v: View) {
                        binding.layoutSwip.resetStatus()
                        onContactPersonListener?.onGroupItemClick(groupEntity)
                    }
                })
                binding.civIcon.setOnClickListener(object : NoDoubleClickListener() {
                    override fun onNoDoubleClick(v: View) {
                        onContactPersonListener?.onGroupHomeClick(groupEntity)
                    }
                })

                binding.ivHome.setOnClickListener(object : NoDoubleClickListener() {
                    override fun onNoDoubleClick(v: View) {
                        binding.layoutSwip.resetStatus()
                        onContactPersonListener?.onGroupHomeClick(groupEntity)
                    }
                })
                binding.ivCall.setOnClickListener(object : NoDoubleClickListener() {
                    override fun onNoDoubleClick(v: View) {
                    }
                })
            }
        }
    }

    class GroupListDiffCallback : DiffUtil.ItemCallback<GroupEntity>() {
        override fun areItemsTheSame(
            oldItem: GroupEntity, newItem: GroupEntity
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: GroupEntity, newItem: GroupEntity
        ): Boolean {
            var isMemberSame = true
            val membersPairs = oldItem.memberIds?.zip(newItem.memberIds!!)
            if (membersPairs != null) {
                for ((item1, item2) in membersPairs) {
                    if (item1 != item2) {
                        isMemberSame = false
                        break
                    }
                }
            }
            return oldItem.name == newItem.name && oldItem.avatarId == newItem.avatarId && isMemberSame

        }
    }
}