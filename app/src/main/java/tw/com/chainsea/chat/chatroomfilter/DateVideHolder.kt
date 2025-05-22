package tw.com.chainsea.chat.chatroomfilter

import androidx.recyclerview.widget.RecyclerView
import tw.com.chainsea.chat.databinding.ItemFilterTimeBinding

class DateVideHolder(private val binding: ItemFilterTimeBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(baseFilterModel: BaseFilterModel) {
        binding.tvDate.text = baseFilterModel.date
    }
}