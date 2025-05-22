package tw.com.chainsea.chat.view.contact.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.common.base.Strings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tw.com.chainsea.ce.sdk.bean.CustomerEntity
import tw.com.chainsea.ce.sdk.bean.GroupEntity
import tw.com.chainsea.ce.sdk.bean.ServiceNum
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity
import tw.com.chainsea.ce.sdk.bean.response.AileTokenApply.Resp.AiffInfo
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.ItemContactListGroupBinding
import tw.com.chainsea.chat.databinding.ItemContactPersonSelfBinding
import tw.com.chainsea.chat.util.NoDoubleClickListener
import tw.com.chainsea.chat.view.contact.ContactListModel
import tw.com.chainsea.chat.view.contact.ContactViewHolderType
import tw.com.chainsea.chat.view.contact.deepCopy
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemNoSwipeViewHolder

class ContactPersonAdapter : ListAdapter<ContactListModel, RecyclerView.ViewHolder>(ContactPersonDiffCallback()) {
    private var onContactPersonListener: OnContactPersonListener? = null
    private var onGroupClick: OnGroupClick? = null

    fun setData(data: List<ContactListModel>) =
        CoroutineScope(Dispatchers.Main).launch {
            submitList(data)
        }

    fun setOnGroupClickListener(onGroupClick: OnGroupClick) {
        this.onGroupClick = onGroupClick
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val sectionViewHolder = ItemContactListGroupBinding.inflate(inflater, parent, false)
        when (viewType) {
            ContactViewHolderType.SELF.ordinal -> {
                val selfBinding = ItemContactPersonSelfBinding.inflate(inflater, parent, false)
                return SelfViewHolder(selfBinding)
            }

            ContactViewHolderType.COLLECTS.ordinal -> {
                return CollectionViewHolder(sectionViewHolder)
            }

            ContactViewHolderType.SUBSCRIBE_SERVICE_NUMBER.ordinal -> {
                return SubscribeViewHolder(sectionViewHolder)
            }

            ContactViewHolderType.GROUP.ordinal -> {
                return GroupViewHolder(sectionViewHolder)
            }

            ContactViewHolderType.EMPLOYEE.ordinal -> {
                return EmployeeViewHolder(sectionViewHolder)
            }

            ContactViewHolderType.CUSTOMER.ordinal -> {
                return CustomerViewHolder(sectionViewHolder)
            }

            ContactViewHolderType.BLOCK.ordinal -> {
                return BlockEmployeeViewHolder(sectionViewHolder)
            }

            ContactViewHolderType.AIFF.ordinal -> {
                return AiffViewHolder(sectionViewHolder)
            }

            else -> {
                return CollectionViewHolder(sectionViewHolder)
            }
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when (getItemViewType(position)) {
            ContactViewHolderType.SELF.ordinal -> {
                (holder as SelfViewHolder).bind(getItem(position))
            }

            ContactViewHolderType.COLLECTS.ordinal -> {
                (holder as CollectionViewHolder).onBind(getItem(position))
            }

            ContactViewHolderType.SUBSCRIBE_SERVICE_NUMBER.ordinal -> {
                (holder as SubscribeViewHolder).onBind(getItem(position))
            }

            ContactViewHolderType.GROUP.ordinal -> {
                (holder as GroupViewHolder).onBind(getItem(position))
            }

            ContactViewHolderType.EMPLOYEE.ordinal -> {
                (holder as EmployeeViewHolder).onBind(getItem(position))
            }

            ContactViewHolderType.CUSTOMER.ordinal -> {
                (holder as CustomerViewHolder).onBind(getItem(position))
            }

            ContactViewHolderType.BLOCK.ordinal -> {
                (holder as BlockEmployeeViewHolder).onBind(getItem(position))
            }

            ContactViewHolderType.AIFF.ordinal -> {
                (holder as AiffViewHolder).onBind(getItem(position))
            }
        }
    }

    fun setOnContactPersonListener(onContactPersonListener: OnContactPersonListener) {
        this.onContactPersonListener = onContactPersonListener
    }

    override fun getItemViewType(position: Int): Int = getItem(position).type.ordinal

    /**
     * 個人資料UI
     */
    inner class SelfViewHolder(
        private val selfBinding: ItemContactPersonSelfBinding
    ) : ItemNoSwipeViewHolder<Any?>(selfBinding.root) {
        fun bind(contactListModel: ContactListModel) {
            val profile = (contactListModel.data as UserProfileEntity)
            val name =
                if (!Strings.isNullOrEmpty(profile.alias)) profile.alias else profile.nickName
            selfBinding.tvName.text = name
            selfBinding.tvTenantName.text =
                TokenPref
                    .getInstance(
                        selfBinding.tvTenantName.context
                    ).cpCurrentTenant.tenantName
            selfBinding.civIcon.loadAvatarIcon(profile.avatarId, name, profile.id)
            selfBinding.ivBarCode.setOnClickListener(
                object : NoDoubleClickListener() {
                    override fun onNoDoubleClick(v: View) {
                        onContactPersonListener?.onBarCodeClick()
                    }
                }
            )
            itemView.setOnClickListener(
                object : NoDoubleClickListener() {
                    override fun onNoDoubleClick(v: View) {
                        onContactPersonListener?.onSelfItemClick(profile)
                    }
                }
            )
            selfBinding.civIcon.setOnClickListener(
                object : NoDoubleClickListener() {
                    override fun onNoDoubleClick(v: View) {
                        onContactPersonListener?.onSelfItemAvatarClick(profile)
                    }
                }
            )
        }
    }

    /**
     * 我的收藏UI
     */
    inner class CollectionViewHolder(
        private val binding: ItemContactListGroupBinding
    ) : BaseViewHolder(binding) {
        fun onBind(contactListModel: ContactListModel) {
            setNormalSetting(contactListModel)
            if (contactListModel.isOpen) {
                val collectionAdapter = CollectListAdapter()
                collectionAdapter.onContactPersonListener = onContactPersonListener
                binding.rvList.apply {
                    adapter = collectionAdapter
                    layoutManager = LinearLayoutManager(context)
                    val subList = getSubList(contactListModel.data as List<Any>)
                    collectionAdapter.setData((subList as List<UserProfileEntity>).toMutableList())
                }

                binding.ivMore.setOnClickListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        val loadMoreList = getLoadMoreList(contactListModel.data as List<Any>)
                        withContext(Dispatchers.Main) {
                            collectionAdapter.setData((loadMoreList as List<UserProfileEntity>).toMutableList())
                        }
                    }
                }
            }
        }
    }

    /**
     * 訂閱號UI  Subscribe
     */
    inner class SubscribeViewHolder(
        private val binding: ItemContactListGroupBinding
    ) : BaseViewHolder(binding) {
        fun onBind(contactListModel: ContactListModel) {
            setNormalSetting(contactListModel)
            if (contactListModel.isOpen) {
                val subscribeListAdapter = SubscribeListAdapter()
                subscribeListAdapter.onContactPersonListener = onContactPersonListener
                binding.rvList.apply {
                    adapter = subscribeListAdapter
                    layoutManager = LinearLayoutManager(context)
                    val subList = getSubList(contactListModel.data as List<Any>)
                    subscribeListAdapter.setData((subList as List<ServiceNum>).toMutableList())
                }

                binding.ivMore.setOnClickListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        val loadMoreList = getLoadMoreList(contactListModel.data as List<Any>)
                        withContext(Dispatchers.Main) {
                            subscribeListAdapter.setData((loadMoreList as List<ServiceNum>).toMutableList())
                        }
                    }
                }
            }
        }
    }

    /**
     * 群組UI
     */
    inner class GroupViewHolder(
        private val binding: ItemContactListGroupBinding
    ) : BaseViewHolder(binding) {
        fun onBind(contactListModel: ContactListModel) {
            setNormalSetting(contactListModel)
            if (contactListModel.isOpen) {
                val groupListAdapter = GroupListAdapter()
                groupListAdapter.onContactPersonListener = onContactPersonListener
                binding.rvList.apply {
                    adapter = groupListAdapter
                    layoutManager = LinearLayoutManager(context)
                    val subList = getSubList(contactListModel.data as List<Any>)
                    groupListAdapter.setData((subList as List<GroupEntity>).toMutableList())
                }

                binding.ivMore.setOnClickListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        val loadMoreList = getLoadMoreList(contactListModel.data as List<Any>)
                        withContext(Dispatchers.Main) {
                            groupListAdapter.setData((loadMoreList as List<GroupEntity>).toMutableList())
                        }
                    }
                }
            }
        }
    }

    /**
     * 員工UI
     */
    inner class EmployeeViewHolder(
        private val binding: ItemContactListGroupBinding
    ) : BaseViewHolder(binding) {
        fun onBind(contactListModel: ContactListModel) {
            setNormalSetting(contactListModel)
            if (contactListModel.isOpen) {
                val employeeListAdapter = EmployeeListAdapter()
                employeeListAdapter.onContactPersonListener = onContactPersonListener
                binding.rvList.apply {
                    adapter = employeeListAdapter
                    layoutManager = LinearLayoutManager(context)
                    val subList = getSubList(contactListModel.data as List<Any>)
                    employeeListAdapter.setData((subList as List<UserProfileEntity>).toMutableList())
                }

                binding.ivMore.setOnClickListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        val loadMoreList = getLoadMoreList(contactListModel.data as List<Any>)
                        withContext(Dispatchers.Main) {
                            employeeListAdapter.setData((loadMoreList as List<UserProfileEntity>).toMutableList())
                        }
                    }
                }
            }
        }
    }

    /**
     * 客戶
     */
    inner class CustomerViewHolder(
        private val binding: ItemContactListGroupBinding
    ) : BaseViewHolder(binding) {
        fun onBind(contactListModel: ContactListModel) {
            setNormalSetting(contactListModel)
            if (contactListModel.isOpen) {
                val customerListAdapter = CustomerListAdapter()
                customerListAdapter.onContactPersonListener = onContactPersonListener
                binding.rvList.apply {
                    adapter = customerListAdapter
                    layoutManager = LinearLayoutManager(context)
                    val subList = getSubList(contactListModel.data as List<Any>)
                    customerListAdapter.setData((subList as List<CustomerEntity>).toMutableList())
                }

                binding.ivMore.setOnClickListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        val loadMoreList = getLoadMoreList(contactListModel.data as List<Any>)
                        withContext(Dispatchers.Main) {
                            customerListAdapter.setData((loadMoreList as List<CustomerEntity>).toMutableList())
                        }
                    }
                }
            }
        }
    }

    /**
     * 封鎖
     */
    inner class BlockEmployeeViewHolder(
        private val binding: ItemContactListGroupBinding
    ) : BaseViewHolder(binding) {
        fun onBind(contactListModel: ContactListModel) {
            setNormalSetting(contactListModel)
            if (contactListModel.isOpen) {
                val blockListAdapter = BlockListAdapter()
                blockListAdapter.onContactPersonListener = onContactPersonListener
                binding.rvList.apply {
                    adapter = blockListAdapter
                    layoutManager = LinearLayoutManager(context)
                    val subList = getSubList(contactListModel.data as List<Any>)
                    blockListAdapter.setData((subList as List<UserProfileEntity>).toMutableList())
                }

                binding.ivMore.setOnClickListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        val loadMoreList = getLoadMoreList(contactListModel.data as List<Any>)
                        withContext(Dispatchers.Main) {
                            blockListAdapter.setData((loadMoreList as List<UserProfileEntity>).toMutableList())
                        }
                    }
                }
            }
        }
    }

    /**
     * 應用
     * */
    inner class AiffViewHolder(
        private val binding: ItemContactListGroupBinding
    ) : BaseViewHolder(binding) {
        fun onBind(contactListModel: ContactListModel) {
            setNormalSetting(contactListModel)
            if (contactListModel.isOpen) {
                val aiffListAdapter = AiffListAdapter()
                binding.rvList.apply {
                    adapter = aiffListAdapter
                    layoutManager = LinearLayoutManager(context)
                    val subList = getSubList(contactListModel.data as List<Any>)
                    aiffListAdapter.setData((subList as List<AiffInfo>).toMutableList())
                }

                binding.ivMore.setOnClickListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        val loadMoreList = getLoadMoreList(contactListModel.data as List<Any>)
                        withContext(Dispatchers.Main) {
                            aiffListAdapter.setData((loadMoreList as List<AiffInfo>).toMutableList())
                        }
                    }
                }
            }
        }
    }

    open inner class BaseViewHolder(
        private val binding: ItemContactListGroupBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private var offset = 20

        fun setNormalSetting(contactListModel: ContactListModel) {
            binding.tvOpen.setImageResource(if (contactListModel.isOpen) R.drawable.ic_expand else R.drawable.ic_close)
            binding.root.setBackgroundColor(if (contactListModel.isOpen) Color.WHITE else 0xFFF7F7F7.toInt())
            if (!contactListModel.isOpen) {
                binding.rvList.adapter = null
                binding.ivMore.visibility = View.GONE
            }

            binding.ivMore.visibility =
                if ((contactListModel.data as List<*>).size >= offset && contactListModel.isOpen) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

            binding.tvTitle.text = contactListModel.title
            val data = contactListModel.data
            var count = 0
            if (data is MutableList<*>) {
                if (contactListModel.type == ContactViewHolderType.GROUP) {
                    if (data.size - 1 == 0) {
                        binding.tvCount.visibility = View.GONE
                    }
                    count = data.size - 1
                } else {
                    val visibility =
                        if (data.isNotEmpty()) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                    binding.tvCount.visibility = visibility
                    count = data.size
                }
            }
            binding.tvCount.text = String.format(count.toString())
            binding.root.setOnClickListener {
                val copyData = getItem(absoluteAdapterPosition).deepCopy()
                copyData.isOpen = !copyData.isOpen
                if (copyData.isOpen) {
                    onGroupClick?.onOpen(copyData.type)
                } else {
                    offset = 20
                    onGroupClick?.onClose(copyData.type)
                }
                updateItem(absoluteAdapterPosition, copyData)
            }
        }

        fun getSubList(data: List<Any>): List<Any> =
            if (data.size < offset) {
                data.subList(0, data.size)
            } else {
                data.subList(0, offset)
            }

        suspend fun getLoadMoreList(data: List<Any>): List<Any> =
            withContext(Dispatchers.IO) {
                val loadMoreList =
                    if (data.size < offset + 20) {
                        val list = data.subList(0, data.size)
                        offset += data.size - offset
                        list
                    } else {
                        val list = data.subList(0, offset + 20)
                        offset += 20
                        list
                    }
                withContext(Dispatchers.Main) {
                    binding.ivMore.visibility =
                        if (offset < data.size) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                }
                return@withContext loadMoreList
            }
    }

    fun updateItem(
        position: Int,
        updatedModel: ContactListModel
    ) {
        val currentList = currentList.deepCopy()
        currentList[position] = updatedModel
        submitList(currentList)
    }

    class ContactPersonDiffCallback : DiffUtil.ItemCallback<ContactListModel>() {
        override fun areItemsTheSame(
            oldItem: ContactListModel,
            newItem: ContactListModel
        ): Boolean = oldItem.type == newItem.type

        override fun areContentsTheSame(
            oldItem: ContactListModel,
            newItem: ContactListModel
        ): Boolean {
            if (oldItem.type == newItem.type) {
                when (oldItem.type) {
                    ContactViewHolderType.SELF -> {
                        val oldData = oldItem.data as UserProfileEntity
                        val newData = newItem.data as UserProfileEntity
                        return oldData.id == newData.id &&
                            oldData.avatarId == newData.avatarId &&
                            oldData.nickName == newData.nickName
                    }

                    ContactViewHolderType.GROUP -> {
                        val oldData = oldItem.data as MutableList<GroupEntity>
                        val newData = newItem.data as MutableList<GroupEntity>

                        return oldData.size == newData.size &&
                            oldData.zip(newData).all { (old, new) ->
                                val memberIdsPairs =
                                    old.memberIds?.let {
                                        it.zip(new.memberIds ?: it)
                                    }
                                var isMemberIdsSame = true
                                if (memberIdsPairs != null) {
                                    for ((item1, item2) in memberIdsPairs) {
                                        if (item1 != item2) {
                                            isMemberIdsSame = false
                                            break
                                        }
                                    }
                                }
                                old.id == new.id &&
                                    old.name == new.name &&
                                    old.avatarId == new.avatarId &&
                                    isMemberIdsSame &&
                                    oldItem.isOpen == newItem.isOpen
                            }
                    }

                    ContactViewHolderType.COLLECTS -> {
                        val oldData = oldItem.data as List<UserProfileEntity>
                        val newData = newItem.data as List<UserProfileEntity>
                        return oldData.size == newData.size &&
                            oldData.zip(newData).all { (old, new) ->
                                old.id == new.id &&
                                    old.nickName == new.nickName &&
                                    old.avatarId == new.avatarId &&
                                    old.alias == new.alias &&
                                    old.mood == new.mood &&
                                    oldItem.isOpen == newItem.isOpen
                            }
                    }

                    ContactViewHolderType.EMPLOYEE -> {
                        val oldData = oldItem.data as List<UserProfileEntity>
                        val newData = newItem.data as List<UserProfileEntity>
                        return oldData.size == newData.size &&
                            oldData.zip(newData).all { (old, new) ->
                                old.id == new.id &&
                                    old.nickName == new.nickName &&
                                    old.avatarId == new.avatarId &&
                                    old.alias == new.alias &&
                                    old.mood == new.mood &&
                                    oldItem.isOpen == newItem.isOpen
                            }
                    }

                    ContactViewHolderType.BLOCK -> {
                        val oldData = oldItem.data as List<UserProfileEntity>
                        val newData = newItem.data as List<UserProfileEntity>
                        return oldData.size == newData.size &&
                            oldData.zip(newData).all { (old, new) ->
                                old.id == new.id &&
                                    old.nickName == new.nickName &&
                                    old.avatarId == new.avatarId &&
                                    old.alias == new.alias &&
                                    old.mood == new.mood &&
                                    oldItem.isOpen == newItem.isOpen
                            }
                    }

                    ContactViewHolderType.CUSTOMER -> {
                        val oldData = oldItem.data as List<CustomerEntity>
                        val newData = newItem.data as List<CustomerEntity>
                        return oldData.size == newData.size &&
                            oldData.zip(newData).all { (old, new) ->
                                old.id == new.id &&
                                    old.nickName == new.nickName &&
                                    old.avatarId == new.avatarId &&
                                    old.customerName == new.customerName &&
                                    oldItem.isOpen == newItem.isOpen
                            }
                    }

                    ContactViewHolderType.AIFF -> {
                        val oldData = oldItem.data as List<AiffInfo>
                        val newData = newItem.data as List<AiffInfo>
                        return oldData.size == newData.size &&
                            oldData.zip(newData).all { (old, new) ->
                                old.id == new.id &&
                                    old.name == new.name &&
                                    old.description == new.description &&
                                    old.url == new.url &&
                                    old.title == new.title &&
                                    old.displayType == new.displayType &&
                                    oldItem.isOpen == newItem.isOpen
                            }
                    }

                    ContactViewHolderType.SUBSCRIBE_SERVICE_NUMBER -> {
                        val oldData = oldItem.data as List<ServiceNum>
                        val newData = newItem.data as List<ServiceNum>
                        return oldData.size == newData.size &&
                            oldData.zip(newData).all { (old, new) ->
                                old.serviceNumberId == new.serviceNumberId &&
                                    old.name == new.name &&
                                    old.description == new.description &&
                                    old.serviceNumberAvatarId == new.serviceNumberAvatarId &&
                                    oldItem.isOpen == newItem.isOpen
                            }
                    }
                }
            }
            return oldItem == newItem &&
                oldItem.isOpen == newItem.isOpen
        }
    }

    interface OnGroupClick {
        fun onOpen(contactViewHolderType: ContactViewHolderType)

        fun onClose(contactViewHolderType: ContactViewHolderType)
    }

    /**
     * onSelfItemClick 自己點擊事件
     * onBarCodeClick 顯示自己QRode
     * onProfileItemClick 用戶點擊事件
     * onGroupItemClick 群組點擊事件
     * onServiceItemClick 訂閱號點擊事件
     * onLabelItemClick 標籤點擊事件
     * onProfileAvatarClick 用戶頭像點擊事件
     * onProfileHomeClick 用戶頁面點擊事件
     * onGroupHomeClick 群組頁面點擊事件
     * onServiceHomeClick 訂閱號頁面點擊事件
     * onLabelEdit 標籤編輯事件
     * onLabelDelete 標籤刪除事件
     */
    interface OnContactPersonListener {
        fun onSelfItemClick(profile: UserProfileEntity)

        fun onSelfItemAvatarClick(profile: UserProfileEntity)

        fun onBarCodeClick()

        fun onProfileItemClick(profile: UserProfileEntity)

        fun onProfileAvatarClick(profile: UserProfileEntity)

        fun onProfileHomeClick(profile: UserProfileEntity)

        fun onCreateGroupItemClick()

        fun onGroupItemClick(crowdEntity: GroupEntity)

        fun onGroupHomeClick(entity: GroupEntity)

        fun onServiceItemClick(serviceNum: ServiceNum)

        fun onServiceHomeClick(serviceNumberId: String)

        fun onServiceNumberBroadcastClick(entity: ServiceNumberEntity)

        fun onServiceNumberContactItemClick(entity: CustomerEntity)

        fun onServiceNumberContactHomeClick(entity: CustomerEntity)
    }
}
