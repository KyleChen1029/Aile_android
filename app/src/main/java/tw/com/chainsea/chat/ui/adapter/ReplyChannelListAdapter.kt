package tw.com.chainsea.chat.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import tw.com.chainsea.ce.sdk.bean.msg.ChannelType
import tw.com.chainsea.chat.databinding.ItemReplyChannelBinding
import tw.com.chainsea.chat.ui.adapter.entity.ChannelEntity

class ReplyChannelListAdapter(
    private val identities: List<ChannelEntity>,
    val callback: (ChannelType) -> Unit
) : Adapter<ReplyChannelListViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ReplyChannelListViewHolder {
        val binding =
            ItemReplyChannelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReplyChannelListViewHolder(binding, callback)
    }

    override fun getItemCount(): Int = identities.size

    override fun onBindViewHolder(
        holder: ReplyChannelListViewHolder,
        position: Int
    ) {
        holder.bind(identities[position])
    }
}

class ReplyChannelListViewHolder(
    val binding: ItemReplyChannelBinding,
    val callback: (ChannelType) -> Unit
) : ViewHolder(binding.root) {
    fun bind(entity: ChannelEntity) {
        binding.ivChannel.setImageResource(entity.resId)
        binding.tvChannelName.text = entity.name
        binding.root.setOnClickListener {
            callback.invoke(entity.code)
        }
    }
}
