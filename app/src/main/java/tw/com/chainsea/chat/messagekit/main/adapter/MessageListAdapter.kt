package tw.com.chainsea.chat.messagekit.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.common.base.Strings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity
import tw.com.chainsea.ce.sdk.bean.msg.ChannelType
import tw.com.chainsea.ce.sdk.bean.msg.MessageFlag
import tw.com.chainsea.ce.sdk.bean.msg.SourceType
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.chat.databinding.ItemBaseMessageBinding
import tw.com.chainsea.chat.databinding.ItemMessageSystemBinding
import tw.com.chainsea.chat.messagekit.main.viewholder.AtMessageView1
import tw.com.chainsea.chat.messagekit.main.viewholder.FacebookCommentHolder1
import tw.com.chainsea.chat.messagekit.main.viewholder.FileMessageView1
import tw.com.chainsea.chat.messagekit.main.viewholder.ImageMessageView1
import tw.com.chainsea.chat.messagekit.main.viewholder.ReplyMessageView1
import tw.com.chainsea.chat.messagekit.main.viewholder.StickerMessage1
import tw.com.chainsea.chat.messagekit.main.viewholder.TemplateMessageView1
import tw.com.chainsea.chat.messagekit.main.viewholder.TextMessageView1
import tw.com.chainsea.chat.messagekit.main.viewholder.TipMessageView1
import tw.com.chainsea.chat.messagekit.main.viewholder.VideoMessageView1
import tw.com.chainsea.chat.messagekit.main.viewholder.VoiceMessageView1
import tw.com.chainsea.chat.messagekit.main.viewholder.base.BaseMessageViewHolder
import tw.com.chainsea.chat.messagekit.main.viewholder.base.OnMessageSlideReply
import java.lang.ref.WeakReference

class MessageListAdapter(
    private val chatRoomEntity: ChatRoomEntity,
    private val onMessageSlideReply: OnMessageSlideReply
) : ListAdapter<MessageEntity, RecyclerView.ViewHolder>(MessageListAdapterDiffCallBack()) {
    private val chatMembers = mutableListOf<UserProfileEntity>()

    private lateinit var onMessageClickListener: WeakReference<OnMessageClickListener?>

    private var adapterMode = MessageAdapterMode.DEFAULT
    private var keyword = ""
    var isAnonymousMode = false
    private val tempViewHolder = mutableListOf<RecyclerView.ViewHolder>()

    fun setChatMembers(chatMembers: MutableList<UserProfileEntity>) {
        this.chatMembers.clear()
        this.chatMembers.addAll(chatMembers)
    }

    fun setOnMessageClickListener(onMessageClickListener: OnMessageClickListener) {
        this.onMessageClickListener = WeakReference(onMessageClickListener)
    }

    @JvmName("functionOfKotlin")
    fun setKeyword(keyword: String) {
        this.keyword = keyword
    }

    fun clearSearchMode() {
        this.keyword = ""
        currentList.forEachIndexed { index, _ ->
            if (getItemViewType(index) == MessageType.At.ordinal ||
                getItemViewType(index) == MessageType.Text.ordinal ||
                getItemViewType(index) == MessageType.ThemeMessage.ordinal
            ) {
                notifyItemChanged(index)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemBaseMessageBinding.inflate(layoutInflater, parent, false)

        when (viewType) {
            MessageType.Retract.ordinal -> {
                val tipMessageBinding = ItemMessageSystemBinding.inflate(layoutInflater, null, false)
                return TipMessageView1(tipMessageBinding)
            }

            MessageType.ThemeMessage.ordinal -> {
                return ReplyMessageView1(binding, layoutInflater, chatRoomEntity, chatMembers, onMessageSlideReply = onMessageSlideReply)
            }

            MessageType.Text.ordinal -> {
                return TextMessageView1(binding, layoutInflater, chatRoomEntity, onMessageSlideReply = onMessageSlideReply)
            }

            MessageType.At.ordinal -> {
                return AtMessageView1(binding, layoutInflater, chatMembers, chatRoomEntity, onMessageSlideReply = onMessageSlideReply)
            }

            MessageType.Image.ordinal -> {
                return ImageMessageView1(binding, layoutInflater, chatRoomEntity, onMessageSlideReply = onMessageSlideReply)
            }

            MessageType.Sticker.ordinal -> {
                return StickerMessage1(binding, layoutInflater, chatRoomEntity, onMessageSlideReply = onMessageSlideReply)
            }

            MessageType.File.ordinal -> {
                return FileMessageView1(binding, layoutInflater, chatRoomEntity, onMessageSlideReply = onMessageSlideReply)
            }

            MessageType.Voice.ordinal -> {
                return VoiceMessageView1(binding, layoutInflater, chatRoomEntity, onMessageSlideReply = onMessageSlideReply)
            }

            MessageType.Video.ordinal -> {
                return VideoMessageView1(binding, layoutInflater, chatRoomEntity, onMessageSlideReply = onMessageSlideReply)
            }

            MessageType.Template.ordinal -> {
                return TemplateMessageView1(binding, chatRoomEntity)
            }

            MessageType.FacebookComment.ordinal -> {
                return FacebookCommentHolder1(binding, chatRoomEntity)
            }

            else -> {
                return TextMessageView1(binding, layoutInflater, chatRoomEntity, onMessageSlideReply = onMessageSlideReply)
            }
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        // 滑動回覆
        if (holder is BaseMessageViewHolder) {
            holder.setOnMessageClickListener(onMessageClickListener.get())
            holder.setMode(adapterMode)
            holder.setAsAnonymousMode(isAnonymousMode)
        } else if (holder is TipMessageView1) {
            holder.setMode(adapterMode)
        }

        when (holder) {
            is TipMessageView1 -> {
                holder.bind(getItem(position))
                holder.setOnTipClickListener(onMessageClickListener.get())
            }

            is ReplyMessageView1 -> {
                holder.setKeyword(keyword)
                holder.bind(getItem(position))
            }

            is TextMessageView1 -> {
                holder.setKeyword(keyword)
                holder.bind(getItem(position))
            }

            is AtMessageView1 -> {
                holder.setKeyword(keyword)
                holder.bind(getItem(position))
            }

            is ImageMessageView1 -> {
                holder.bind(getItem(position))
            }

            is StickerMessage1 -> {
                holder.bind(getItem(position))
            }

            is FileMessageView1 -> {
                tempViewHolder.add(holder)
                holder.bind(getItem(position))
            }

            is VoiceMessageView1 -> {
                holder.bind(getItem(position))
            }

            is VideoMessageView1 -> {
                tempViewHolder.add(holder)
                holder.bind(getItem(position))
            }

            is TemplateMessageView1 -> {
                holder.bind(getItem(position))
            }

            is FacebookCommentHolder1 -> {
                holder.bind(getItem(position))
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        val messageType = message.type?.type
        messageType?.let { type ->
            return when {
                MessageFlag.RETRACT == message.flag || SourceType.SYSTEM == message.sourceType -> MessageType.Retract.ordinal
                !Strings.isNullOrEmpty(message.themeId) -> MessageType.ThemeMessage.ordinal
                message.from != null && message.from == ChannelType.FB && type == MessageType.Template.name -> MessageType.FacebookComment.ordinal
                type == MessageType.Text.name -> MessageType.Text.ordinal
                type == MessageType.At.name -> MessageType.At.ordinal
                type == MessageType.Image.name -> MessageType.Image.ordinal
                type == MessageType.Sticker.name -> MessageType.Sticker.ordinal
                type == MessageType.File.name -> MessageType.File.ordinal
                type == MessageType.Voice.name -> MessageType.Voice.ordinal
                type == MessageType.Video.name -> MessageType.Video.ordinal
                type == MessageType.Call.name -> MessageType.Call.ordinal
                type == MessageType.Business.name -> MessageType.Business.ordinal
                type == MessageType.Broadcast.name -> MessageType.Broadcast.ordinal
                type == MessageType.Transfer.name -> MessageType.Transfer.ordinal
                type == MessageType.Template.name -> MessageType.Template.ordinal
                else -> MessageType.None.ordinal
            }
        } ?: run {
            return MessageType.None.ordinal
        }
    }

    fun setAdapterMode(mode: MessageAdapterMode) =
        CoroutineScope(Dispatchers.Main).launch {
            adapterMode = mode
            when (mode) {
                // 截圖
                MessageAdapterMode.RANGE_SELECTION -> {
                    notifyItemRangeChanged(0, itemCount, false)
                }

                // 多選
                MessageAdapterMode.SELECTION -> {
                    currentList.forEach {
                        if (it.type != tw.com.chainsea.ce.sdk.bean.msg.MessageType.TEMPLATE) {
                            it.isShowChecked = true
                        }
                    }
                    notifyItemRangeChanged(0, itemCount, false)
                }

                else -> {
                    currentList.forEach {
                        it.isShowChecked = false
                    }
                    notifyItemRangeChanged(0, itemCount, false)
                }
            }
        }

    suspend fun getHolder(
        parent: ViewGroup,
        position: Int
    ): RecyclerView.ViewHolder =
        withContext(Dispatchers.Main) {
            val holder = onCreateViewHolder(parent, getItemViewType(position))
            onBindViewHolder(holder, position)
            return@withContext holder
        }

    fun switchAnonymousMode(isAnonymous: Boolean) {
        this.isAnonymousMode = isAnonymous
        notifyItemRangeChanged(0, itemCount, false)
    }

    fun getLastMessage(): MessageEntity = getItem(itemCount - 1)

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is BaseMessageViewHolder) {
            holder.clearCallback()
        }
        if (holder is VideoMessageView1) {
            holder.clearEventBus()
        }
        if (holder is FileMessageView1) {
            holder.clearEventBus()
        }
        if (holder is ReplyMessageView1) {
            holder.clearEventBus()
        }
        super.onViewRecycled(holder)
    }

    fun onDestroy() {
        tempViewHolder.forEach {
            if (it is BaseMessageViewHolder) {
                it.clearCallback()
            }
            if (it is VideoMessageView1) {
                it.clearEventBus()
            }
            if (it is FileMessageView1) {
                it.clearEventBus()
            }
            if (it is ReplyMessageView1) {
                it.clearEventBus()
            }
        }
        tempViewHolder.clear()
    }

    class MessageListAdapterDiffCallBack : DiffUtil.ItemCallback<MessageEntity>() {
        override fun areItemsTheSame(
            oldItem: MessageEntity,
            newItem: MessageEntity
        ): Boolean =
            oldItem.id == newItem.id &&
                oldItem == newItem

        override fun areContentsTheSame(
            oldItem: MessageEntity,
            newItem: MessageEntity
        ): Boolean =
            oldItem.content == newItem.content &&
                oldItem.flag == newItem.flag &&
                oldItem.status == newItem.status &&
                oldItem.isShowChecked == newItem.isShowChecked &&
                oldItem.sequence == newItem.sequence &&
                oldItem.isAnimator == newItem.isAnimator
    }

    fun clearObjects() {
        onMessageClickListener.get()?.let {
            onMessageClickListener.clear()
        }
    }
}

enum class MessageType {
    None,
    Retract,
    ThemeMessage,
    Text,
    At,
    Image,
    Sticker,
    File,
    Voice,
    Video,
    Call,
    Business,
    Broadcast,
    Transfer,
    Template,
    FacebookComment
}

interface OnMessageClickListener {
    fun onTipMessageClick(message: MessageEntity)

    fun onAtMessageClick(id: String)

    fun onImageMessageClick(message: MessageEntity)

    fun onVideoMessageClick(message: MessageEntity)

    fun onMessageLongClick(message: MessageEntity)

    fun onThemeNearMessageClick(nearMessageSequence: Int)

    fun onThemeUnderMessageClick(themeId: String?)

    fun buildScreenShot(message: MessageEntity)

    fun onMessageStatusClick(message: MessageEntity)

    fun onAvatarIconClick(senderId: String)
}
