package tw.com.chainsea.chat.view.homepage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import tw.com.chainsea.ce.sdk.network.model.response.DictionaryItems
import tw.com.chainsea.chat.databinding.ItemServiceNumberTimeBinding


class ServiceNumberTimeOutAdapter(private val timeData: List<DictionaryItems>, private val onItemClickListener: (Int) -> Unit) :
    RecyclerView.Adapter<ServiceNumberTimeOutAdapter.TimeOutViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeOutViewHolder {
        val binding =
            ItemServiceNumberTimeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TimeOutViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TimeOutViewHolder, position: Int) {
        holder.bind(timeData[position])
    }

    override fun getItemCount(): Int {
        return timeData.size
    }

    inner class TimeOutViewHolder(val binding: ItemServiceNumberTimeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(timeValue: DictionaryItems) {
            binding.tvTimeValue.text = timeValue.text
            binding.root.setOnClickListener {
                onItemClickListener.invoke(timeValue.value.toInt())
            }
        }
    }
}
