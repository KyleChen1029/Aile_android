package tw.com.chainsea.chat.chatroomfilter.adpter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tw.com.chainsea.ce.sdk.bean.msg.MessageType
import tw.com.chainsea.chat.chatroomfilter.BaseFilterModel
import tw.com.chainsea.chat.chatroomfilter.DateVideHolder
import tw.com.chainsea.chat.chatroomfilter.FilterDataType
import tw.com.chainsea.chat.chatroomfilter.model.FilterFileModel
import tw.com.chainsea.chat.chatroomfilter.model.FilterLinkModel
import tw.com.chainsea.chat.databinding.ItemFilterFileBinding
import tw.com.chainsea.chat.databinding.ItemFilterTimeBinding

class FilterFileAdapter : BaseFilterAdapter() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            FilterDataType.DATE.ordinal -> {
                val binding = ItemFilterTimeBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return DateVideHolder(binding)
            }

            FilterDataType.MEDIA.ordinal -> {
                val binding = ItemFilterFileBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return FilterFileViewHolder(binding)
            }

            else -> {
                val binding = ItemFilterFileBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return FilterFileViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            FilterDataType.DATE.ordinal -> {
                (holder as DateVideHolder).bind(getItem(position))
            }

            FilterDataType.FILE.ordinal -> {
                (holder as FilterFileViewHolder).bind(getItem(position))
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).type == MessageType.SYSTEM) {
            FilterDataType.DATE.ordinal
        } else {
            FilterDataType.FILE.ordinal
        }
    }

    inner class FilterFileViewHolder(private val binding: ItemFilterFileBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(baseFilterModel: BaseFilterModel) = CoroutineScope(Dispatchers.IO).launch {
            checkIsMultipleChoiceMode(binding.tvSelect, baseFilterModel)
            withContext(Dispatchers.Main) {
                val filterFileModel = baseFilterModel as FilterFileModel
                binding.tvFileName.text = filterFileModel.fileName
                binding.tvDate.text = filterFileModel.itemDate
                binding.ivFileIcon.setImageResource(filterFileModel.fileIcon)
                binding.root.setOnClickListener {
                    if (isMultipleChoiceMode) {
                        addMultipleChoiceItem<FilterFileModel>(binding.tvSelect, filterFileModel)
                    }
                }
            }
        }
    }
}