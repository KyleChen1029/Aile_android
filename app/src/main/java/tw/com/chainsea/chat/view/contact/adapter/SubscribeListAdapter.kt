package tw.com.chainsea.chat.view.contact.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import tw.com.chainsea.ce.sdk.bean.ServiceNum
import tw.com.chainsea.chat.databinding.ItemContactPersonServiceBinding
import tw.com.chainsea.chat.util.NoDoubleClickListener

class SubscribeListAdapter :
    BaseContactAdapter<ServiceNum, SubscribeListAdapter.SubscribeItemViewHolder>(
        SubscribeListDiffCallback()
    ) {


    fun setData(data: List<ServiceNum>) {
        submitList(data)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubscribeItemViewHolder {
        val binding = ItemContactPersonServiceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SubscribeItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubscribeItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SubscribeItemViewHolder(private val binding: ItemContactPersonServiceBinding) :
        RecyclerView.ViewHolder(binding.root) {


        fun bind(serviceNum: ServiceNum) {
            binding.tvName.text = serviceNum.name
            binding.tvMood.text = serviceNum.description
            binding.civIcon.loadAvatarIcon(
                serviceNum.serviceNumberAvatarId,
                serviceNum.name,
                serviceNum.serviceNumberId
            )
            binding.clContentItem.setOnClickListener(object : NoDoubleClickListener() {
                override fun onNoDoubleClick(v: View) {
                    binding.layoutSwip.resetStatus()
                    onContactPersonListener?.onServiceItemClick(serviceNum)
                }
            })
            binding.civIcon.setOnClickListener(object : NoDoubleClickListener() {
                override fun onNoDoubleClick(v: View) {
                    onContactPersonListener?.onServiceHomeClick(serviceNum.serviceNumberId)
                }
            })

            binding.ivHome.setOnClickListener(object : NoDoubleClickListener() {
                override fun onNoDoubleClick(v: View) {
                    binding.layoutSwip.resetStatus()
                    onContactPersonListener?.onServiceHomeClick(serviceNum.serviceNumberId)
                }
            })
        }
    }


    class SubscribeListDiffCallback : DiffUtil.ItemCallback<ServiceNum>() {
        override fun areItemsTheSame(
            oldItem: ServiceNum, newItem: ServiceNum
        ): Boolean {
            return oldItem.serviceNumberId == newItem.serviceNumberId
        }

        override fun areContentsTheSame(
            oldItem: ServiceNum, newItem: ServiceNum
        ): Boolean {
            return oldItem.serviceNumberAvatarId == newItem.serviceNumberAvatarId && oldItem.name == newItem.name
        }

    }

}