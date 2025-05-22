package tw.com.chainsea.chat.view.contact.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.common.base.Strings
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity
import tw.com.chainsea.chat.databinding.ItemContactPersonEmployeeBinding
import tw.com.chainsea.chat.util.NoDoubleClickListener
import java.text.MessageFormat

open class EmployeeListAdapter :
    BaseContactAdapter<UserProfileEntity, EmployeeListAdapter.EmployeeItemViewHolder>(
        EmployeeListDiffCallback()
    ) {

    fun setData(data: List<UserProfileEntity>) {
        submitList(data)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeeItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemContactPersonEmployeeBinding.inflate(inflater, parent, false)
        return EmployeeItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EmployeeItemViewHolder, position: Int) {
        val profileEntity = getItem(position)
        holder.bind(profileEntity)
    }


    inner class EmployeeItemViewHolder(private val binding: ItemContactPersonEmployeeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(profile: UserProfileEntity) {
            val name =
                profile.alias?.ifEmpty { profile.nickName?.ifEmpty { profile.name.ifEmpty { "Unknown" } } }
            binding.civIcon.loadAvatarIcon(profile.avatarId, name ?: "Unknown", profile.id)
            if (profile.isAddCell) {
                binding.tvName.text = name
                binding.tvMood.text = profile.alias
                binding.rvLabel.adapter = null
                binding.ivHome.visibility = View.GONE
                binding.ivCall.visibility = View.GONE
                binding.clContentItem.setOnClickListener(null)
                binding.civIcon.setOnClickListener(null)
                binding.ivHome.setOnClickListener(null)
                binding.ivCall.setOnClickListener(null)
            } else {
                binding.tvName.text = name
                if (!Strings.isNullOrEmpty(profile.duty) || !Strings.isNullOrEmpty(
                        profile.department
                    )
                ) {
                    binding.tvMood.text =
                        MessageFormat.format("{0}/{1}", profile.duty, profile.department)
                } else {
                    binding.tvMood.text = ""
                }

                val labels = profile.labels
                binding.rvLabel.adapter = null
                if (labels != null && labels.isNotEmpty()) {
                    val adapter = EmployeeLabelAdapter(labels)
                    binding.rvLabel.layoutManager = GridLayoutManager(
                        binding.rvLabel.context, 2, GridLayoutManager.HORIZONTAL, true
                    )
                    binding.rvLabel.adapter = adapter
                }
                binding.ivHome.visibility = View.VISIBLE
                binding.ivCall.visibility = View.GONE
                binding.clContentItem.setOnClickListener(object : NoDoubleClickListener() {
                    override fun onNoDoubleClick(v: View) {
                        binding.layoutSwip.resetStatus()
                        onContactPersonListener?.onProfileItemClick(profile)
                    }
                })
                binding.civIcon.setOnClickListener(object : NoDoubleClickListener() {
                    override fun onNoDoubleClick(v: View) {
                        onContactPersonListener?.onProfileAvatarClick(profile)
                    }
                })

                binding.ivHome.setOnClickListener(object : NoDoubleClickListener() {
                    override fun onNoDoubleClick(v: View) {
                        binding.layoutSwip.resetStatus()
                        onContactPersonListener?.onProfileHomeClick(profile)
                    }
                })
            }
        }
    }


    class EmployeeListDiffCallback : DiffUtil.ItemCallback<UserProfileEntity>() {
        override fun areItemsTheSame(
            oldItem: UserProfileEntity, newItem: UserProfileEntity
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: UserProfileEntity, newItem: UserProfileEntity
        ): Boolean {
            return oldItem.name == newItem.name && oldItem.avatarId == newItem.avatarId && oldItem.mood == newItem.mood

        }
    }
}