package tw.com.chainsea.chat.view.contact.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import tw.com.chainsea.ce.sdk.bean.label.Label
import tw.com.chainsea.chat.databinding.ItemContactPersonEmployeeLabelBinding

abstract class BaseContactAdapter<T, VH : RecyclerView.ViewHolder>(private val diffUtil: DiffUtil.ItemCallback<T>) :
    ListAdapter<T, VH>(diffUtil) {
    var onContactPersonListener: ContactPersonAdapter.OnContactPersonListener? = null


    // 員工資訊內標籤
    internal class EmployeeLabelAdapter(private var labels: List<Label>) :
        RecyclerView.Adapter<EmployeeLabelAdapter.EmployeeLabelViewHolder?>() {
        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): EmployeeLabelViewHolder {
            val inflater = LayoutInflater.from(viewGroup.context)
            val binding = ItemContactPersonEmployeeLabelBinding.inflate(inflater, viewGroup, false)
            return EmployeeLabelViewHolder(binding)
        }

        override fun onBindViewHolder(holder: EmployeeLabelViewHolder, position: Int) {
            val label = labels[position]
            holder.bind(label.name)
        }

        override fun getItemCount(): Int {
            return labels.size.coerceAtMost(4)
        }

        inner class EmployeeLabelViewHolder(private val binding: ItemContactPersonEmployeeLabelBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(labelName: String) {
                binding.tvLabelName.text = labelName
            }
        }
    }
}