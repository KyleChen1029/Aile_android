package tw.com.chainsea.chat.view.chatroom.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType
import tw.com.chainsea.ce.sdk.bean.todo.TodoEntity
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.chatroomfilter.ChatRoomFilterActivity
import tw.com.chainsea.chat.config.BundleKey
import tw.com.chainsea.chat.databinding.ItemAdvisorySmallRoomBinding
import tw.com.chainsea.chat.databinding.ItemAdvisoryTodoBinding
import tw.com.chainsea.chat.ui.activity.ChatActivity
import tw.com.chainsea.chat.util.IntentUtil
import tw.com.chainsea.chat.util.ThemeHelper
import tw.com.chainsea.chat.view.consultai.ConsultAIActivity

class AdvisoryRoomAdapter(
    val consultAILauncher: ActivityResultLauncher<Intent>
) : ListAdapter<SmallRoomData, RecyclerView.ViewHolder>(AdvisoryRoomAdapterDiffCallBack()) {
    companion object {
        const val SERVICE_CONSULT_ROOM = 1
        const val TODO = 2
    }

    fun setData(data: List<SmallRoomData>) {
        submitList(data)
    }

    fun shouldNotifyUnreadIcon(roomId: String) =
        CoroutineScope(Dispatchers.IO).launch {
            currentList.forEachIndexed { index, smallRoomData ->
                if (smallRoomData.roomId == roomId) {
                    smallRoomData.unReadNum = 1
                    withContext(Dispatchers.Main) {
                        notifyItemChanged(index)
                    }
                    return@launch
                }
            }
        }

    override fun getItemViewType(position: Int): Int =
        if (getItem(position).todoList.isEmpty()) {
            SERVICE_CONSULT_ROOM
        } else {
            TODO
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val holder =
            when (viewType) {
                SERVICE_CONSULT_ROOM -> {
                    val binding = ItemAdvisorySmallRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                    SmallRoomViewHolder(binding)
                }

                else -> {
                    val binding = ItemAdvisoryTodoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                    TodoSmallIconViewHolder(binding)
                }
            }
        return holder
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when (getItemViewType(position)) {
            SERVICE_CONSULT_ROOM -> {
                (holder as SmallRoomViewHolder).bind(getItem(position))
            }

            else -> {
                (holder as TodoSmallIconViewHolder).bind(getItem(position))
            }
        }
    }

    inner class SmallRoomViewHolder(
        val binding: ItemAdvisorySmallRoomBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: SmallRoomData) {
            binding.tvUnread.visibility = View.GONE
            when (data.roomType) {
                ChatRoomType.serviceMember -> {
                    binding.tvUnread.visibility = if (data.unReadNum > 0) View.VISIBLE else View.GONE
                    binding.civIcon.setImageResource(data.avatarId.toInt())
                }

                ChatRoomType.services -> {
                    var serviceNumberChatRoom: ChatRoomEntity? = null
                    CoroutineScope(Dispatchers.IO).launch {
                        serviceNumberChatRoom = ChatRoomReference.getInstance().findById(data.roomId)
                        serviceNumberChatRoom?.let {
                            withContext(Dispatchers.Main) {
                                binding.tvUnread.visibility = if (it.unReadNum > 0) View.VISIBLE else View.GONE
                                binding.civIcon.loadAvatarIcon(data.avatarId, it.name, data.serviceNumberId)
                            }
                        } ?: run {
                            withContext(Dispatchers.Main) {
                                binding.civIcon.loadAvatarIcon(data.avatarId, "", data.serviceNumberId)
                            }
                        }
                    }
                }

                else -> {
                    binding.civIcon.setImageResource(R.drawable.ic_ai_consultation)
                }
            }

            binding.root.setOnClickListener {
                if (data.roomType == ChatRoomType.consultAi) {
                    val bundle =
                        bundleOf(
                            BundleKey.ROOM_ID.key() to data.serviceNumberRoomId,
                            BundleKey.SERVICE_NUMBER_ID.key() to data.serviceNumberId,
                            BundleKey.CONSULT_AI_ID.key() to data.roomId
                        )
                    IntentUtil.launchIntent(it.context, ConsultAIActivity::class.java, consultAILauncher, bundle)
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        ChatRoomReference.getInstance().updateUnread(data.roomId)
                        withContext(Dispatchers.Main) {
                            notifyItemChanged(absoluteAdapterPosition)
                        }
                    }

                    IntentUtil.startIntent(
                        it.context,
                        ChatActivity::class.java,
                        bundleOf(BundleKey.EXTRA_SESSION_ID.key() to data.roomId)
                    )
                }
                clearUnread(data)
            }
        }
    }

    private fun clearUnread(data: SmallRoomData) {
        data.unReadNum = 0
        notifyItemChanged(currentList.indexOf(data))
    }

    @SuppressLint("SetTextI18n")
    inner class TodoSmallIconViewHolder(
        val binding: ItemAdvisoryTodoBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: SmallRoomData) {
            if (data.unReadNum > 0) {
                binding.tvUnread.text = data.unReadNum.toString()
                binding.tvUnread.visibility = View.VISIBLE
            } else {
                binding.tvUnread.text = ""
                binding.tvUnread.visibility = View.GONE
            }

            binding.ivTodoIcon.setImageResource(
                if (ThemeHelper.isGreenTheme() || ThemeHelper.isServiceRoomTheme) {
                    R.drawable.icon_todo_small_service_room
                } else {
                    R.drawable.icon_todo_small_normal_room
                }
            )

            binding.root.setOnClickListener {
                IntentUtil.startIntent(
                    it.context,
                    ChatRoomFilterActivity::class.java,
                    bundleOf(
                        BundleKey.INTENT_TO_TODO.key() to true,
                        BundleKey.ROOM_ID.key() to data.roomId
                    )
                )
            }
        }
    }

    class AdvisoryRoomAdapterDiffCallBack : DiffUtil.ItemCallback<SmallRoomData>() {
        override fun areItemsTheSame(
            oldItem: SmallRoomData,
            newItem: SmallRoomData
        ): Boolean = oldItem == newItem

        override fun areContentsTheSame(
            oldItem: SmallRoomData,
            newItem: SmallRoomData
        ): Boolean =
            oldItem.roomId == newItem.roomId &&
                oldItem.serviceNumberRoomId == newItem.serviceNumberRoomId &&
                oldItem.avatarId == newItem.avatarId &&
                oldItem.unReadNum == newItem.unReadNum &&
                oldItem.todoList.size == newItem.todoList.size
    }
}

data class SmallRoomData(
    val roomType: ChatRoomType,
    val roomId: String,
    var avatarId: String = "",
    var unReadNum: Int = 0,
    val serviceNumberRoomId: String = "",
    val serviceNumberId: String = "",
    val roomName: String = "",
    val todoList: List<TodoEntity> = emptyList(),
    val sort: Int = 99
)
