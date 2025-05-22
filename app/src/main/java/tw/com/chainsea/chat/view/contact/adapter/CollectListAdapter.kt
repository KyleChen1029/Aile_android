package tw.com.chainsea.chat.view.contact.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.common.base.Strings
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity
import tw.com.chainsea.chat.databinding.ItemContactPersonCollectionBinding
import tw.com.chainsea.chat.util.NoDoubleClickListener

class CollectListAdapter :
    BaseContactAdapter<UserProfileEntity, CollectListAdapter.CollectItemViewHolder>(
        CollectListDiffCallback()
    ) {
    fun setData(data: List<UserProfileEntity>) {
        submitList(data)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CollectItemViewHolder {
        val binding =
            ItemContactPersonCollectionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return CollectItemViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: CollectItemViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    inner class CollectItemViewHolder(
        private val binding: ItemContactPersonCollectionBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(profile: UserProfileEntity) {
            binding.tvName.text = profile.nickName
            if (!Strings.isNullOrEmpty(profile.duty) || !Strings.isNullOrEmpty(profile.department)) {
                binding.tvMood.text = "${profile.duty}/${profile.department}"
            } else {
                binding.tvMood.text = ""
            }

            // Avatar
            binding.civIcon.loadAvatarIcon(profile.avatarId, profile.nickName, profile.id)

            val labels = profile.labels
            if (labels != null && labels.isNotEmpty()) {
                val adapter = EmployeeLabelAdapter(labels)
                binding.rvLabel.layoutManager =
                    GridLayoutManager(
                        binding.rvLabel.context,
                        2,
                        GridLayoutManager.HORIZONTAL,
                        true
                    )
                binding.rvLabel.adapter = adapter
            }
            binding.clContentItem.setOnClickListener(
                object : NoDoubleClickListener() {
                    override fun onNoDoubleClick(v: View) {
                        binding.layoutSwip.resetStatus()
                        onContactPersonListener?.onProfileItemClick(profile)
                    }
                }
            )
            binding.civIcon.setOnClickListener(
                object : NoDoubleClickListener() {
                    override fun onNoDoubleClick(v: View) {
                        onContactPersonListener?.onProfileAvatarClick(profile)
                    }
                }
            )
            binding.ivHome.setOnClickListener(
                object : NoDoubleClickListener() {
                    override fun onNoDoubleClick(v: View) {
                        binding.layoutSwip.resetStatus()
                        onContactPersonListener?.onProfileHomeClick(profile)
                    }
                }
            )
        }
    }

    class CollectListDiffCallback : DiffUtil.ItemCallback<UserProfileEntity>() {
        override fun areItemsTheSame(
            oldItem: UserProfileEntity,
            newItem: UserProfileEntity
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: UserProfileEntity,
            newItem: UserProfileEntity
        ): Boolean = oldItem.nickName == newItem.nickName && oldItem.avatarId == newItem.avatarId
    }
}
