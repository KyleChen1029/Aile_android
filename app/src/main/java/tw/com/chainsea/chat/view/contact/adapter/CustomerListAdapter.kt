package tw.com.chainsea.chat.view.contact.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.common.base.Strings
import tw.com.chainsea.ce.sdk.bean.CustomerEntity
import tw.com.chainsea.chat.databinding.ItemContactPersonCustomerBinding
import tw.com.chainsea.chat.util.NoDoubleClickListener

class CustomerListAdapter :
    BaseContactAdapter<CustomerEntity, CustomerListAdapter.CustomerItemViewHolder>(
        CustomerListDiffCallback()
    ) {


    fun setData(data: List<CustomerEntity>) {
        submitList(data)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerItemViewHolder {
        val binding = ItemContactPersonCustomerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CustomerItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomerItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CustomerItemViewHolder(private val binding: ItemContactPersonCustomerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(customerEntity: CustomerEntity) {
            binding.ivHome.visibility = View.VISIBLE
            binding.ivCall.visibility = View.GONE
            val name = if (!Strings.isNullOrEmpty(
                    customerEntity.customerName
                )
            ) customerEntity.customerName else customerEntity.nickName
            binding.tvName.text = name
            binding.civIcon.loadAvatarIcon(customerEntity.avatarId, name, customerEntity.id)
            binding.clContentItem.setOnClickListener(object : NoDoubleClickListener() {
                override fun onNoDoubleClick(v: View) {
                    binding.layoutSwip.resetStatus()
                    onContactPersonListener?.onServiceNumberContactItemClick(customerEntity)
                }
            })
            binding.civIcon.setOnClickListener(object : NoDoubleClickListener() {
                override fun onNoDoubleClick(v: View) {
                    onContactPersonListener?.onServiceNumberContactHomeClick(customerEntity)
                }
            })
            binding.ivHome.setOnClickListener(object : NoDoubleClickListener() {
                override fun onNoDoubleClick(v: View) {
                    binding.layoutSwip.resetStatus()
                    onContactPersonListener?.onServiceNumberContactHomeClick(customerEntity)
                }
            })
        }
    }

    class CustomerListDiffCallback : DiffUtil.ItemCallback<CustomerEntity>() {
        override fun areItemsTheSame(
            oldItem: CustomerEntity, newItem: CustomerEntity
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: CustomerEntity, newItem: CustomerEntity
        ): Boolean {
            return oldItem.name == newItem.name && oldItem.avatarId == newItem.avatarId

        }
    }
}