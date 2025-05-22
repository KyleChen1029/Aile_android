package tw.com.chainsea.chat.chatroomfilter.adpter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tw.com.chainsea.android.common.ui.UiHelper
import tw.com.chainsea.ce.sdk.bean.msg.MessageType
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.chatroomfilter.BaseFilterModel
import tw.com.chainsea.chat.chatroomfilter.DateVideHolder
import tw.com.chainsea.chat.chatroomfilter.FilterDataType
import tw.com.chainsea.chat.chatroomfilter.model.FilterLinkModel
import tw.com.chainsea.chat.databinding.ItemFilterLinkBinding
import tw.com.chainsea.chat.databinding.ItemFilterTimeBinding
import tw.com.chainsea.chat.util.IntentUtil

class FilterLinkAdapter : BaseFilterAdapter() {
    fun setWebMetaData(filterLinkModel: FilterLinkModel) {
        data.forEachIndexed { index, baseFilterModel ->
            if (baseFilterModel is FilterLinkModel) {
                if (baseFilterModel.url == filterLinkModel.url) {
                    notifyItemChanged(index)
                    return@forEachIndexed
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        when (viewType) {
            FilterDataType.DATE.ordinal -> {
                val binding =
                    ItemFilterTimeBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                return DateVideHolder(binding)
            }

            FilterDataType.MEDIA.ordinal -> {
                val binding =
                    ItemFilterLinkBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                return FilterLinkViewHolder(binding)
            }

            else -> {
                val binding =
                    ItemFilterLinkBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                return FilterLinkViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when (getItemViewType(position)) {
            FilterDataType.DATE.ordinal -> {
                (holder as DateVideHolder).bind(getItem(position))
            }

            FilterDataType.LINK.ordinal -> {
                (holder as FilterLinkViewHolder).bind(getItem(position))
            }
        }
    }

    override fun getItemViewType(position: Int): Int =
        if (getItem(position).type == MessageType.SYSTEM) {
            FilterDataType.DATE.ordinal
        } else {
            FilterDataType.LINK.ordinal
        }

    inner class FilterLinkViewHolder(
        private val binding: ItemFilterLinkBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(baseFilterModel: BaseFilterModel) =
            CoroutineScope(Dispatchers.Main).launch {
                val filterLinkModel = baseFilterModel as FilterLinkModel
                checkIsMultipleChoiceMode(binding.tvSelect, baseFilterModel)

                binding.tvDate.text = filterLinkModel.itemDate
                binding.tvLink.text = filterLinkModel.url
                binding.tvTitle.text = filterLinkModel.title

                if (filterLinkModel.url.contains(".facebook.")) {
                    binding.ivLinkPreview.setImageResource(R.drawable.ic_fb)
                } else {
                    filterLinkModel.image?.let {
                        binding.ivLinkPreview.setImageBitmap(it)
                    } ?: run {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val bitmap =
                                    Glide
                                        .with(binding.root.context)
                                        .asBitmap()
                                        .load(baseFilterModel.imageUrl)
                                        .apply(
                                            RequestOptions()
                                                .override(UiHelper.dp2px(binding.root.context, 70F).toInt())
                                        ).centerCrop()
                                        .submit()
                                        .get()
                                withContext(Dispatchers.Main) {
                                    binding.ivLinkPreview.setImageBitmap(bitmap)
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    binding.ivLinkPreview.setImageResource(R.drawable.icon_filter_error_image)
                                }
                            }
                        }
                    }
                }
                binding.root.setOnClickListener {
                    if (isMultipleChoiceMode) {
                        addMultipleChoiceItem<FilterLinkModel>(binding.tvSelect, filterLinkModel)
                    } else {
                        val url =
                            if (!filterLinkModel.url.startsWith("http") || !filterLinkModel.url.startsWith("https")) {
                                "https://${filterLinkModel.url}"
                            } else {
                                filterLinkModel.url
                            }
                        IntentUtil.launchUrl(binding.root.context, url)
                    }
                }
            }
    }
}
