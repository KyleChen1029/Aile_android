package tw.com.chainsea.chat.view.roomList.serviceRoomList.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.ItemServiceNumberGroupBinding
import tw.com.chainsea.chat.util.ThemeHelper
import tw.com.chainsea.chat.view.roomList.mainRoomList.RoomListAdapter
import tw.com.chainsea.chat.view.roomList.mainRoomList.RoomListClickInterface
import tw.com.chainsea.chat.view.roomList.serviceRoomList.ServiceNumberListModel
import tw.com.chainsea.chat.view.roomList.serviceRoomList.ServiceNumberListType
import tw.com.chainsea.chat.view.roomList.serviceRoomList.adapter.ServiceNumberAdapter.ServiceNumberGroupViewHolder

class ServiceNumberAdapter : ListAdapter<ServiceNumberListModel, ServiceNumberGroupViewHolder>(ServiceNumberAdapterDiffCallBack()) {
    private lateinit var roomListRecyclerViewPool: RecyclerView.RecycledViewPool
    private val serviceNumberControlRecyclerViewPool by lazy { RecyclerView.RecycledViewPool() }
    private var roomListClickInterface: RoomListClickInterface? = null
    private var onGroupClick: OnGroupClick? = null

    fun setData(data: MutableList<ServiceNumberListModel>) {
        submitList(data.toMutableList())
    }

    fun setRoomListRecyclerViewPool(pool: RecyclerView.RecycledViewPool) {
        roomListRecyclerViewPool = pool
    }

    fun setRoomListClickInterface(roomListClickInterface: RoomListClickInterface) {
        this.roomListClickInterface = roomListClickInterface
    }

    fun setOnGroupClickInterface(onGroupClick: OnGroupClick) {
        this.onGroupClick = onGroupClick
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ServiceNumberGroupViewHolder {
        val binding =
            ItemServiceNumberGroupBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ServiceNumberGroupViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ServiceNumberGroupViewHolder,
        position: Int
    ) {
        holder.setNormalInfo(getItem(position))
        when (getItemViewType(position)) {
            ServiceNumberListType.ServiceNumberGroup.ordinal -> {
                holder.bindServiceGroup(getItem(position))
            }

            ServiceNumberListType.MyService.ordinal -> {
                holder.bindOtherGroup(getItem(position), holder.itemView.context.getString(R.string.service_room_sectioned_my_service))
            }

            ServiceNumberListType.Serviced.ordinal -> {
                holder.bindOtherGroup(getItem(position), holder.itemView.context.getString(R.string.service_room_sectioned_others_service))
            }

            ServiceNumberListType.AIService.ordinal -> {
                holder.bindOtherGroup(getItem(position), holder.itemView.context.getString(R.string.service_room_sectioned_robot_service))
            }

            ServiceNumberListType.MonitorAI.ordinal -> {
                holder.bindOtherGroup(getItem(position), holder.itemView.context.getString(R.string.service_room_sectioned_monitor_ai_service))
            }

            ServiceNumberListType.UnService.ordinal -> {
                holder.bindOtherGroup(getItem(position), holder.itemView.context.getString(R.string.service_room_sectioned_no_agent_unread))
            }

            ServiceNumberListType.Other.ordinal -> {
                holder.bindOtherGroup(getItem(position), holder.itemView.context.getString(R.string.service_room_sectioned_other))
            }
        }
    }

    override fun getItemViewType(position: Int): Int = getItem(position).type.ordinal

    inner class ServiceNumberGroupViewHolder(
        private val binding: ItemServiceNumberGroupBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bindServiceGroup(serviceNumberListModel: ServiceNumberListModel) {
            binding.tvServiceNumberName.text = serviceNumberListModel.serviceNumberEntity?.name
            binding.root.setOnClickListener {
                getItem(absoluteAdapterPosition).isOpen = getItem(absoluteAdapterPosition).isOpen.not()
                notifyItemChanged(absoluteAdapterPosition, false)
            }
            serviceNumberListModel.serviceNumberIcon?.let {
                binding.ivServiceNumberIcon.setImageResource(it)
            }

            if (serviceNumberListModel.isOpen) {
                Log.d("Kyle117", "isOpen serviceNumber name = ${serviceNumberListModel.serviceNumberEntity?.name}")
                onGroupClick?.onOpen(serviceNumberListModel.serviceNumberEntity?.serviceNumberId, serviceNumberListModel.type)
                setControlList(serviceNumberListModel)
                loadServiceNumberRoom(serviceNumberListModel, limit = 10, offset = 0)
            } else {
                Log.d("Kyle117", "Close serviceNumber name = ${serviceNumberListModel.serviceNumberEntity?.name}")
                onGroupClick?.onClose(serviceNumberListModel.serviceNumberEntity?.serviceNumberId, serviceNumberListModel.type)
                binding.rvServiceNumberChatRoomList.apply {
                    adapter = null
                    visibility = View.GONE
                }
                binding.rvServiceNumberControlList.visibility = View.GONE
                serviceNumberListModel.serviceNumberEndServiceChatRoom.clear()
                binding.ivMore.visibility = View.GONE
            }
            binding.ivMore.setOnClickListener {
                Log.d("Kyle117", "bindServiceGroup: size = ${serviceNumberListModel.serviceNumberEndServiceChatRoom.size}, serviceNumberId = ${serviceNumberListModel.serviceNumberEntity?.serviceNumberId}")
                val offset = serviceNumberListModel.serviceNumberEndServiceChatRoom.size
                loadServiceNumberRoom(serviceNumberListModel, limit = 10, offset = offset)
            }
        }

        // 進線、我的服務、服務中、AI 等群組
        fun bindOtherGroup(
            serviceNumberListModel: ServiceNumberListModel,
            titleText: String
        ) {
            binding.ivServiceNumberIcon.visibility = View.GONE
            val title =
                if (serviceNumberListModel.type == ServiceNumberListType.Other) {
                    titleText
                } else {
                    titleText + "(${serviceNumberListModel.serviceNumberEndServiceChatRoom.size})"
                }
            binding.tvServiceNumberName.text = title
            binding.root.setOnClickListener {
                getItem(absoluteAdapterPosition).isOpen = getItem(absoluteAdapterPosition).isOpen.not()
                notifyItemChanged(absoluteAdapterPosition, false)
            }
            if (serviceNumberListModel.isOpen) {
                onGroupClick?.onOpen(serviceNumberListModel.serviceNumberEntity?.serviceNumberId, serviceNumberListModel.type)
                setChatRoomList(serviceNumberListModel)
                if (serviceNumberListModel.type == ServiceNumberListType.Other) { // 依時間分組
                    binding.ivMore.visibility = if (serviceNumberListModel.serviceNumberEndServiceChatRoom.size >= 10) View.VISIBLE else View.GONE
                    binding.ivMore.setOnClickListener {
                        Log.d("Kyle117", "bindServiceGroup: size = ${serviceNumberListModel.serviceNumberEndServiceChatRoom.size}, serviceNumberId = ${serviceNumberListModel.serviceNumberEntity?.serviceNumberId}")
                        CoroutineScope(Dispatchers.IO).launch {
                            val result =
                                ChatRoomReference.getInstance().queryOnlineServiceRoomByTime(
                                    serviceNumberListModel.serviceNumberEndServiceChatRoom.size,
                                    10
                                )
                            if (result.isNotEmpty()) {
                                serviceNumberListModel.serviceNumberEndServiceChatRoom.addAll(result)
                                setChatRoomList(serviceNumberListModel)
                                CoroutineScope(Dispatchers.Main).launch {
                                    binding.ivMore.visibility =
                                        if (serviceNumberListModel.serviceNumberEndServiceChatRoom.size >= 10) View.VISIBLE else View.GONE
                                }
                            } else {
                                CoroutineScope(Dispatchers.Main).launch {
                                    binding.ivMore.visibility = View.GONE
                                }
                            }
                        }
                    }
                }
            } else {
                onGroupClick?.onClose(serviceNumberListModel.serviceNumberEntity?.serviceNumberId, serviceNumberListModel.type)
                binding.rvServiceNumberChatRoomList.apply {
                    adapter = null
                    visibility = View.GONE
                }
                binding.ivMore.visibility = View.GONE
            }
        }

        private fun loadServiceNumberRoom(
            serviceNumberListModel: ServiceNumberListModel,
            limit: Int = 10,
            offset: Int = 0
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                // 現有資料列表
                val existingChatRooms = serviceNumberListModel.serviceNumberEndServiceChatRoom

                // 查詢資料
                val result =
                    ChatRoomReference
                        .getInstance()
                        .queryServiceRoomByServiceNumberId(
                            serviceNumberListModel.serviceNumberEntity?.serviceNumberId,
                            offset,
                            limit
                        )

                // 篩選掉已存在於 UnServiceList 的資料
                val unServiceList = currentList.find { it.type == ServiceNumberListType.UnService }
                val aiServiceList = currentList.find { it.type == ServiceNumberListType.AIService }
                val myServiceList = currentList.find { it.type == ServiceNumberListType.MyService }
                val monitorAiServiceList = currentList.find { it.type == ServiceNumberListType.MonitorAI }
                val servicedList = currentList.find { it.type == ServiceNumberListType.Serviced }

                val newData =
                    result.filter { item ->
                        unServiceList?.serviceNumberEndServiceChatRoom?.none { it.id == item.id } ?: true &&
                            aiServiceList?.serviceNumberEndServiceChatRoom?.none { it.id == item.id } ?: true &&
                            myServiceList?.serviceNumberEndServiceChatRoom?.none { it.id == item.id } ?: true &&
                            monitorAiServiceList?.serviceNumberEndServiceChatRoom?.none { it.id == item.id } ?: true &&
                            servicedList?.serviceNumberEndServiceChatRoom?.none { it.id == item.id } ?: true &&
                            existingChatRooms.none { it.id == item.id }
                    }

                // 合併新資料到現有列表
                if (newData.isNotEmpty()) {
                    existingChatRooms.addAll(newData)
                }

                // 如果新資料不足，補充更多資料
                if (newData.size < limit) {
                    val remainingLimit = limit - newData.size
                    val additionalResult =
                        ChatRoomReference
                            .getInstance()
                            .queryServiceRoomByServiceNumberId(
                                serviceNumberListModel.serviceNumberEntity?.serviceNumberId,
                                offset + limit,
                                remainingLimit
                            )
                    val additionalData =
                        additionalResult.filter { item ->
                            unServiceList?.serviceNumberEndServiceChatRoom?.none { unItem ->
                                item.id == unItem.id
                            } ?: true &&
                                existingChatRooms.none { it.id == item.id }
                        }
                    existingChatRooms.addAll(additionalData)
                }

                // 主執行緒更新 UI
                withContext(Dispatchers.Main) {
                    setChatRoomList(serviceNumberListModel)
                    binding.ivMore.visibility =
                        if (existingChatRooms.size >= offset + limit) View.VISIBLE else View.GONE
                }
            }
        }

        // 設置基本訊息
        @SuppressLint("SetTextI18n")
        fun setNormalInfo(serviceNumberListModel: ServiceNumberListModel) =
            CoroutineScope(Dispatchers.Main).launch {
                binding.tvOpen.setImageResource(if (serviceNumberListModel.isOpen) R.drawable.ic_expand else R.drawable.ic_close)
                binding.root.setBackgroundColor(if (serviceNumberListModel.isOpen) Color.WHITE else 0xFFF7F7F7.toInt())
                val visibility =
                    if (!serviceNumberListModel.isOpen && serviceNumberListModel.unReadNum > 0) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                binding.tvUnread.visibility = visibility
                binding.tvUnread.text = serviceNumberListModel.unReadNum.toString()
            }

        // 控制列表
        private fun setControlList(serviceNumberListModel: ServiceNumberListModel) {
            val serviceNumberControlAdapter = ServiceNumberControlAdapter()
            serviceNumberControlAdapter.setData(serviceNumberListModel.serviceNumberControl.map { it.value }.toMutableList())
            serviceNumberControlAdapter.setServiceNumberEntity(serviceNumberListModel)
            binding.rvServiceNumberControlList.apply {
//                setRecycledViewPool(serviceNumberControlRecyclerViewPool)
                adapter = serviceNumberControlAdapter
                layoutManager = GridLayoutManager(binding.root.context, 5)
                visibility = View.VISIBLE
            }
        }

        // 聊天室列表
        private fun setChatRoomList(serviceNumberListModel: ServiceNumberListModel) =
            CoroutineScope(Dispatchers.Main).launch {
                val chatRoomAdapter =
                    RoomListAdapter(
                        ThemeHelper.isGreenTheme(),
                        true
                    )
                chatRoomAdapter.apply {
                    roomListClickInterface?.let { setRoomListClickInterface(it) }
                    setData(
                        TokenPref.getInstance(binding.root.context).userId,
                        serviceNumberListModel.serviceNumberEndServiceChatRoom
                    )
                }
                binding.rvServiceNumberChatRoomList.apply {
                    setRecycledViewPool(roomListRecyclerViewPool)
                    adapter = chatRoomAdapter
                    layoutManager = LinearLayoutManager(binding.root.context)
                    visibility = View.VISIBLE
                }
            }
    }

    interface OnGroupClick {
        fun onOpen(
            serviceNumberId: String?,
            serviceNumberListType: ServiceNumberListType
        )

        fun onClose(
            serviceNumberId: String?,
            serviceNumberListType: ServiceNumberListType
        )
    }

    class ServiceNumberAdapterDiffCallBack : DiffUtil.ItemCallback<ServiceNumberListModel>() {
        override fun areItemsTheSame(
            oldItem: ServiceNumberListModel,
            newItem: ServiceNumberListModel
        ): Boolean =
            oldItem.id == newItem.id &&
                oldItem.type == newItem.type

        override fun areContentsTheSame(
            oldItem: ServiceNumberListModel,
            newItem: ServiceNumberListModel
        ): Boolean {
            var isChatRoomSame = true
            var isChatRoomServicingSame = true

            oldItem.serviceNumberEndServiceChatRoom.forEach lit@{ oldChatRoom ->
                newItem.serviceNumberEndServiceChatRoom.forEach { newChatRoom ->
                    if (oldChatRoom.id == newChatRoom.id) {
                        if (oldChatRoom.isTransferFlag != newChatRoom.isTransferFlag) {
                            isChatRoomSame = oldChatRoom.isTransferFlag == newChatRoom.isTransferFlag &&
                                oldChatRoom.serviceNumberStatus == newChatRoom.serviceNumberStatus &&
                                oldChatRoom.avatarId == newChatRoom.avatarId &&
                                oldChatRoom.serviceNumberAvatarId == newChatRoom.serviceNumberAvatarId
                            return@lit
                        }
                    }
                }
            }

            oldItem.serviceNumberServicingChatRoom.forEach lit@{ oldChatRoom ->
                newItem.serviceNumberServicingChatRoom.forEach { newChatRoom ->
                    if (oldChatRoom.id == newChatRoom.id) {
                        isChatRoomServicingSame = oldChatRoom.isTransferFlag == newChatRoom.isTransferFlag &&
                            oldChatRoom.serviceNumberStatus == newChatRoom.serviceNumberStatus &&
                            oldChatRoom.avatarId == newChatRoom.avatarId &&
                            oldChatRoom.serviceNumberAvatarId == newChatRoom.serviceNumberAvatarId
                        return@lit
                    }
                }
            }

            return isChatRoomSame &&
                isChatRoomServicingSame &&
                oldItem.unReadNum == newItem.unReadNum &&
                oldItem.serviceNumberServicingChatRoom.size == newItem.serviceNumberServicingChatRoom.size &&
                oldItem.serviceNumberEndServiceChatRoom.size == newItem.serviceNumberEndServiceChatRoom.size &&
                !newItem.isOpen
        }

        override fun getChangePayload(
            oldItem: ServiceNumberListModel,
            newItem: ServiceNumberListModel
        ): Any? {
            if (oldItem.unReadNum != newItem.unReadNum) return newItem.unReadNum
            if (oldItem.serviceNumberEndServiceChatRoom.size != newItem.serviceNumberEndServiceChatRoom.size) return newItem.serviceNumberEndServiceChatRoom
            return super.getChangePayload(oldItem, newItem)
        }
    }
}
