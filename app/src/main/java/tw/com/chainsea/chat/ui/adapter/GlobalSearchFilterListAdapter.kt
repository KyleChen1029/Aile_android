package tw.com.chainsea.chat.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.common.collect.Lists
import tw.com.aile.sdk.bean.filter.FilterTab
import tw.com.aile.sdk.bean.filter.SearchFilterEntity
import tw.com.chainsea.ce.sdk.bean.CustomerEntity
import tw.com.chainsea.ce.sdk.bean.GroupEntity
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.ItemChatRoomFilterSectionedBinding
import tw.com.chainsea.chat.searchfilter.viewmodel.ChatRoomSearchViewModel
import tw.com.chainsea.chat.searchfilter.viewmodel.ContactPersonClientSearchViewModel
import tw.com.chainsea.chat.searchfilter.viewmodel.SearchFilterSharedViewModel
import tw.com.chainsea.chat.searchfilter.viewmodel.ServiceNumberSearchViewModel
import tw.com.chainsea.chat.util.ThemeHelper

class GlobalSearchFilterListAdapter(
    private val userId: String,
    private val viewModel: Any,
    private val pageType: SearchFilterSharedViewModel.SearchFilterPageType,
    private val lifecycleOwner: LifecycleOwner,
    private val isGreenTheme: Boolean
) : ListAdapter<SearchFilterEntity, RecyclerView.ViewHolder>(DiffCallback()) {

    init {
        submitList(Lists.newArrayList())
    }

    companion object {
        const val TYPE_FILTER_CHAT_ROOM = 1
        const val TYPE_FILTER_CONTACT_PERSON = 2
        const val TYPE_FILTER_COMMUNITY = 3
        const val TYPE_FILTER_SERVICE_NUMBER = 4
    }

    lateinit var keyWord: String
    fun setData(keyWord: String, newData: MutableList<SearchFilterEntity>) {
        this.keyWord = keyWord
        submitList(newData)
    }

    inner class CommunitiesSearchTabViewHolder(
        private val binding: ItemChatRoomFilterSectionedBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: SearchFilterEntity,
            viewModel: ContactPersonClientSearchViewModel
        ) {
            updateViewAndData(item, binding, viewModel)
        }
    }

    inner class ServiceNumberSearchTabViewHolder(
        private val binding: ItemChatRoomFilterSectionedBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: SearchFilterEntity,
            viewModel: ServiceNumberSearchViewModel
        ) {
            updateViewAndData(item, binding, viewModel)
        }
    }

    inner class ContactPersonSearchTabViewHolder(
        private val binding: ItemChatRoomFilterSectionedBinding
    ): RecyclerView.ViewHolder(binding.root){

        fun bind(
            item: SearchFilterEntity,
            viewModel: ContactPersonClientSearchViewModel
        ) {
            updateViewAndData(item, binding, viewModel)
        }
    }

    inner class ChatRoomSearchTabViewHolder(
        private val binding: ItemChatRoomFilterSectionedBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: SearchFilterEntity,
            viewModel: ChatRoomSearchViewModel
        ) {
            updateViewAndData(item, binding, viewModel)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return when (item.tab) {
            FilterTab.CHAT_ROOM -> TYPE_FILTER_CHAT_ROOM
            FilterTab.CONTACT_PERSON -> TYPE_FILTER_CONTACT_PERSON
            FilterTab.COMMUNITY -> TYPE_FILTER_COMMUNITY
            FilterTab.SERVICE_NUMBER -> TYPE_FILTER_SERVICE_NUMBER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_FILTER_COMMUNITY -> CommunitiesSearchTabViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_chat_room_filter_sectioned,
                    parent,
                    false
                )
            )
            TYPE_FILTER_SERVICE_NUMBER -> ServiceNumberSearchTabViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_chat_room_filter_sectioned,
                    parent,
                    false
                )
            )
            TYPE_FILTER_CONTACT_PERSON -> ContactPersonSearchTabViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_chat_room_filter_sectioned,
                    parent,
                    false
            ))
            TYPE_FILTER_CHAT_ROOM -> ChatRoomSearchTabViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_chat_room_filter_sectioned,
                    parent,
                    false
                )
            )
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            TYPE_FILTER_COMMUNITY -> (holder as CommunitiesSearchTabViewHolder).bind(
                getItem(position),
                viewModel as ContactPersonClientSearchViewModel
            )
            TYPE_FILTER_SERVICE_NUMBER -> (holder as ServiceNumberSearchTabViewHolder).bind(
                getItem(position),
                viewModel as ServiceNumberSearchViewModel
            )
            TYPE_FILTER_CONTACT_PERSON -> (holder as ContactPersonSearchTabViewHolder).bind(
                getItem(position),
                viewModel as ContactPersonClientSearchViewModel
            )
            TYPE_FILTER_CHAT_ROOM -> (holder as ChatRoomSearchTabViewHolder).bind(
                getItem(position),
                viewModel as ChatRoomSearchViewModel
            )
        }
    }

    fun updateViewAndData(item: SearchFilterEntity, binding: ItemChatRoomFilterSectionedBinding, viewModel: Any) {
        binding.item = item
        val list = if(item.data.size > 5 && !item.isLoadMore) item.data.subList(0, 5) else item.data
        binding.clLoadMore.visibility = if(item.data.size > 5 && !item.isLoadMore && item.isExpand) View.VISIBLE else View.GONE
        val childGlobalSearchFilterAdapter = ChildGlobalSearchFilterAdapter(userId, viewModel, keyWord, item, list, pageType, lifecycleOwner, isGreenTheme)
        binding.rvList.apply {
            adapter = childGlobalSearchFilterAdapter
            visibility = if(item.isExpand) View.VISIBLE else View.GONE
        }

        binding.apply {
            scopeSectioned.setOnClickListener {
                item.isExpand = !item.isExpand
                scopeSectioned.setBackgroundColor(ContextCompat.getColor(binding.root.context, if(rvList.visibility == View.VISIBLE) R.color.color_FAFAFA else R.color.transparent))
                rvList.visibility = if(rvList.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                icExtend.setImageResource(if(rvList.visibility == View.VISIBLE) R.drawable.ic_arrow_top else R.drawable.ic_arrow_down)
                clLoadMore.visibility = if(rvList.visibility == View.VISIBLE && childGlobalSearchFilterAdapter.itemCount <= 5 && item.data.size > 5 && !item.isLoadMore) View.VISIBLE else View.GONE
            }
            clLoadMore.setOnClickListener {
                clLoadMore.visibility = View.GONE
                item.isLoadMore = !item.isLoadMore
                childGlobalSearchFilterAdapter.submitList(item.data)
            }
            binding.executePendingBindings()
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SearchFilterEntity>() {
        override fun areItemsTheSame(
            oldItem: SearchFilterEntity,
            newItem: SearchFilterEntity
        ): Boolean {
            return oldItem.data.size == newItem.data.size
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(
            oldItem: SearchFilterEntity,
            newItem: SearchFilterEntity
        ): Boolean {
            return when {
                oldItem.data is ChatRoomEntity && newItem.data is ChatRoomEntity -> oldItem.data as ChatRoomEntity == newItem.data as ChatRoomEntity
                oldItem.data is UserProfileEntity && newItem.data is UserProfileEntity -> oldItem.data as UserProfileEntity == newItem.data as UserProfileEntity
                oldItem.data is CustomerEntity && newItem.data is CustomerEntity -> oldItem.data as CustomerEntity == newItem.data as CustomerEntity
                else -> {
                    oldItem.data.forEach {
                        if(it is GroupEntity){
                            if(it != newItem.data)
                                return false
                        }
                    }
                    false
                }
            }
        }
    }

}
