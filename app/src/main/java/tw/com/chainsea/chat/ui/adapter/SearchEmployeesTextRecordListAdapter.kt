package tw.com.chainsea.chat.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.common.collect.Lists
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.EmployeeSearchRecordTextListBinding
import tw.com.chainsea.chat.searchfilter.viewmodel.SearchFilterViewModel

class SearchEmployeesTextRecordListAdapter(
    private val viewModel: SearchFilterViewModel
) : ListAdapter<String, SearchEmployeesTextRecordListAdapter.SearchEmployeesTextRecordListAViewHolder>(DiffCallback()) {

    init {
        submitList(Lists.newArrayList())
    }
    fun setData(newData: MutableList<String>) {
        submitList(newData)
    }
    inner class SearchEmployeesTextRecordListAViewHolder(
        private val binding: EmployeeSearchRecordTextListBinding
    ): RecyclerView.ViewHolder(binding.root){

        fun bind(text: String, viewModel: SearchFilterViewModel) {
            binding.tvName.text = text//substringWithinByteLimit(text, 9)
            binding.root.setOnClickListener {
                viewModel.onSelectedSearchTextItem(text)
            }
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchEmployeesTextRecordListAViewHolder {
        val binding = DataBindingUtil.inflate<EmployeeSearchRecordTextListBinding>(
            LayoutInflater.from(parent.context),
            R.layout.employee_search_record_text_list,
            parent,
            false
        )
        return SearchEmployeesTextRecordListAViewHolder(binding)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: SearchEmployeesTextRecordListAViewHolder, position: Int) {
        holder.bind(getItem(position), viewModel)
    }


    class DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}