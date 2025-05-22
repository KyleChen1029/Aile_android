package tw.com.chainsea.chat.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import tw.com.chainsea.android.common.json.JsonHelper
import tw.com.chainsea.ce.sdk.bean.room.QuickReplyItem
import tw.com.chainsea.chat.databinding.ItemQuickReplyBinding

class QuickReplyAdapter(private val quickReplyList: List<QuickReplyItem>) :
    RecyclerView.Adapter<QuickReplyAdapter.QuickReplyViewHolder>() {

    private var onQuickReplyClick: OnQuickReplyClick? = null

    fun setOnQuickReplyClickListener(onQuickReplyClick: OnQuickReplyClick) {
        this.onQuickReplyClick = onQuickReplyClick
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuickReplyViewHolder {
        val binding =
            ItemQuickReplyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QuickReplyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return quickReplyList.size
    }

    override fun onBindViewHolder(holder: QuickReplyViewHolder, position: Int) {
        holder.bind(quickReplyList[position])
    }

    inner class QuickReplyViewHolder(private val binding: ItemQuickReplyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(quickReplyItem: QuickReplyItem) {
            binding.tvQuickReplyText.text = quickReplyItem.action.title
            binding.root.setOnClickListener {
                onQuickReplyClick?.onQuickReplyClick(quickReplyItem.type, JsonHelper.getInstance().toJson(quickReplyItem.action))
            }
        }
    }
}

fun interface OnQuickReplyClick {
    fun onQuickReplyClick(type: String, data: String)
}