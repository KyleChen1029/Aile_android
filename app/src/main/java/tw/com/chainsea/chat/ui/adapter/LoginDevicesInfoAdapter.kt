package tw.com.chainsea.chat.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import tw.com.chainsea.ce.sdk.network.model.response.DeviceRecordItem
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.ItemLoginDeviceBinding

class LoginDevicesInfoAdapter:
    ListAdapter<DeviceRecordItem, LoginDevicesInfoAdapter.ViewHolder>(LoginDevicesInfoDiffCallback()) {

    var itemClickListener: ((item: DeviceRecordItem) -> Unit)? = null
    var isGreenTheme = false
    fun setTheme(isGreen: Boolean) {
        isGreenTheme = isGreen
    }
    class ViewHolder(
        val binding: ItemLoginDeviceBinding,
        val onItemClickListener: ((math: DeviceRecordItem) -> Unit)?,
        val isGreenTheme: Boolean
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DeviceRecordItem) {
            binding.item = item

            binding.Cl.setBackgroundColor(ContextCompat.getColor(binding.root.context,
                if(item.isOnline == true) {
                    if(isGreenTheme)
                        R.color.color_06B4A5
                    else
                        R.color.color_29AEE1
                } else
                    R.color.color_D8D8D8
            ))
            binding.deviceIcon.setImageResource(if(item.osType == "pc") R.drawable.ic_device_list else R.drawable.ic_device_phone)
            binding.root.setOnClickListener {
                onItemClickListener?.invoke(item)
            }

            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLoginDeviceBinding.inflate(
            LayoutInflater.from(parent.context)
        ).apply {
            root.layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
            )
        }
        return ViewHolder(binding, itemClickListener, isGreenTheme)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class LoginDevicesInfoDiffCallback : DiffUtil.ItemCallback<DeviceRecordItem>() {
    override fun areItemsTheSame(oldItem: DeviceRecordItem, newItem: DeviceRecordItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: DeviceRecordItem,
        newItem: DeviceRecordItem
    ): Boolean {
        return oldItem == newItem
    }

}