package tw.com.chainsea.chat.view.contact.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import tw.com.chainsea.ce.sdk.bean.response.AileTokenApply.Resp.AiffInfo
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.aiff.AiffDialog
import tw.com.chainsea.chat.aiff.AiffKey
import tw.com.chainsea.chat.databinding.ItemContactPersonAiffBinding
import tw.com.chainsea.chat.util.NoDoubleClickListener

class AiffListAdapter :
    BaseContactAdapter<AiffInfo, AiffListAdapter.AiffItemViewHolder>(AiffListDiffCallback()) {


    fun setData(data: List<AiffInfo>) {
        submitList(data)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AiffItemViewHolder {
        val binding =
            ItemContactPersonAiffBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AiffItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AiffItemViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class AiffItemViewHolder(private val binding: ItemContactPersonAiffBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(aiffInfo: AiffInfo) {
            binding.tvName.text = aiffInfo.name
            binding.tvDescribe.text = aiffInfo.description
            Glide.with(itemView.context).load(aiffInfo.pictureId)
                .into(binding.aiffIcon)
            itemView.setOnClickListener(object : NoDoubleClickListener() {
                override fun onNoDoubleClick(v: View) {
                    goToAiffPage(aiffInfo)
                }
            })
        }

        private fun goToAiffPage(aiffInfo: AiffInfo) {
            val intent = Intent(binding.root.context, AiffDialog::class.java)
            intent.putExtra(AiffKey.TITLE, aiffInfo.title)
            intent.putExtra(AiffKey.URL, aiffInfo.url)
            intent.putExtra(AiffKey.DISPLAY_TYPE, aiffInfo.displayType)
            itemView.context.startActivity(intent)
            if (itemView.context is Activity) {
                (itemView.context as Activity).overridePendingTransition(
                    R.anim.ios_dialog_enter, R.anim.ios_dialog_exit
                )
            }
        }
    }

    class AiffListDiffCallback : DiffUtil.ItemCallback<AiffInfo>() {
        override fun areItemsTheSame(
            oldItem: AiffInfo, newItem: AiffInfo
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: AiffInfo, newItem: AiffInfo
        ): Boolean {
            return oldItem.name == newItem.name &&
                    oldItem.url == newItem.url &&
                    oldItem.description == newItem.description &&
                    oldItem.title == newItem.title

        }
    }
}