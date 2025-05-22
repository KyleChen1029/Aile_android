package tw.com.chainsea.chat.view.roomList.serviceRoomList.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberType
import tw.com.chainsea.chat.config.BundleKey
import tw.com.chainsea.chat.databinding.ItemSectionActionBinding
import tw.com.chainsea.chat.ui.activity.ChatActivity
import tw.com.chainsea.chat.ui.dialog.WaitTransferDialogBuilder
import tw.com.chainsea.chat.util.IntentUtil
import tw.com.chainsea.chat.view.account.homepage.ServicesNumberManagerHomepageActivity
import tw.com.chainsea.chat.view.roomList.serviceRoomList.ServiceNumberListModel
import tw.com.chainsea.chat.view.roomList.serviceRoomList.adapter.ActionBean.BROADCAST
import tw.com.chainsea.chat.view.roomList.serviceRoomList.adapter.ActionBean.CHAT
import tw.com.chainsea.chat.view.roomList.serviceRoomList.adapter.ActionBean.HOME
import tw.com.chainsea.chat.view.roomList.serviceRoomList.adapter.ActionBean.MEMBERS
import tw.com.chainsea.chat.view.roomList.serviceRoomList.adapter.ActionBean.WAIT_TRANSFER
import tw.com.chainsea.chat.view.roomList.serviceRoomList.adapter.ActionBean.WELCOME_MESSAGE
import tw.com.chainsea.chat.view.service.ServiceNumberAgentsManageActivity
import tw.com.chainsea.chat.view.service.ServiceNumberManageActivity

/**
 * 服務號列表 各個服務號群組底下的控制項 (主頁、成員、聊天等)
 * */
class ServiceNumberControlAdapter : RecyclerView.Adapter<ServiceNumberControlAdapter.ServiceNumberControlViewHolder>() {
    private val data = mutableListOf<ActionBean>()
    private var serviceNumberListModel: ServiceNumberListModel? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: MutableList<ActionBean>) {
        this.data.clear()
        this.data.addAll(data)
        notifyDataSetChanged()
    }

    fun setServiceNumberEntity(serviceNumberListModel: ServiceNumberListModel?) {
        this.serviceNumberListModel = serviceNumberListModel
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ServiceNumberControlViewHolder {
        val binding =
            ItemSectionActionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ServiceNumberControlViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ServiceNumberControlViewHolder,
        position: Int
    ) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    inner class ServiceNumberControlViewHolder(
        private val binding: ItemSectionActionBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(actionBean: ActionBean) {
            binding.ivActionIcon.setImageResource(actionBean.resId)
            binding.tvActionName.text = binding.root.context.getString(actionBean.nameResId)
            binding.root.setOnClickListener {
                serviceNumberListModel?.serviceNumberEntity?.let {
                    when (actionBean) {
                        CHAT -> {
                            val intent =
                                Intent(binding.root.context, ChatActivity::class.java)
                                    .putExtra(BundleKey.EXTRA_SESSION_ID.key(), it.serviceMemberRoomId)
                            IntentUtil.start(binding.root.context, intent)
                        }

                        HOME -> {
                            val intent =
                                if (ServiceNumberType.MANAGER.type == it.serviceNumberType) {
                                    if (it.isManager || it.isOwner) {
                                        Intent(binding.root.context, ServicesNumberManagerHomepageActivity::class.java)
                                            .putExtra(BundleKey.SERVICE_NUMBER_ID.key(), it.serviceNumberId)
                                    } else {
                                        Intent(binding.root.context, ServiceNumberManageActivity::class.java)
                                            .putExtra(BundleKey.SERVICE_NUMBER_ID.key(), it.serviceNumberId)
                                    }
                                } else {
                                    Intent(binding.root.context, ServiceNumberManageActivity::class.java)
                                        .putExtra(BundleKey.SERVICE_NUMBER_ID.key(), it.serviceNumberId)
                                }

                            IntentUtil.start(binding.root.context, intent)
                        }

                        MEMBERS -> {
                            val intent =
                                Intent(binding.root.context, ServiceNumberAgentsManageActivity::class.java)
                                    .putExtra(BundleKey.BROADCAST_ROOM_ID.key(), it.broadcastRoomId)
                                    .putExtra(BundleKey.SERVICE_NUMBER_ID.key(), it.serviceNumberId)
                            IntentUtil.start(binding.root.context, intent)
                        }

                        WAIT_TRANSFER -> {
                            val transferList = mutableListOf<ChatRoomEntity>()
                            serviceNumberListModel?.serviceNumberServicingChatRoom?.forEach {
                                if (it.isTransferFlag) transferList.add(it)
                            }
                            WaitTransferDialogBuilder(binding.root.context, transferList).create().show()
                        }

                        BROADCAST -> {
                        }

                        WELCOME_MESSAGE -> {
                        }
                    }
                }
            }
        }
    }
}
