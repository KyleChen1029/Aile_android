package tw.com.chainsea.chat.view.homepage.bind

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tw.com.chainsea.chat.databinding.ItemThirdPartBinding

/**
 * 已經綁定的粉絲專頁列表
 * */
class ThirdPartListAdapter(
    private val itemClickCallBack: (ThirdPartListModel) -> Unit
) : RecyclerView.Adapter<ThirdPartListAdapter.ThirdPartViewHolder>() {
    private val list = mutableListOf<ThirdPartListModel>()

    @SuppressLint("NotifyDataSetChanged")
    fun setData(list: List<ThirdPartListModel>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    fun setBindInfo(
        type: ThirdPartEnum,
        fansPageId: String,
        name: String,
        infoString: String
    ) = CoroutineScope(Dispatchers.IO).launch {
        val targetData = list.find { it.type == type }
        targetData?.let {
            it.fansPageString = infoString
            it.id = fansPageId
            it.name = name
            withContext(Dispatchers.Main) {
                notifyItemChanged(list.indexOf(targetData))
            }
        }
    }

    fun clearBindInfo(
        type: ThirdPartEnum,
        title: String
    ) = CoroutineScope(Dispatchers.IO).launch {
        val targetData = list.find { it.type == type }
        targetData?.let {
            it.fansPageString = ""
            it.id = ""
            it.name = title
            withContext(Dispatchers.Main) {
                notifyItemChanged(list.indexOf(targetData))
            }
        }
    }

    public fun getFacebookFansPageId(): String? {
        val facebookFansPages = list.find { it.type == ThirdPartEnum.Facebook }
        return facebookFansPages?.id
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ThirdPartViewHolder {
        val binding = ItemThirdPartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ThirdPartViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ThirdPartViewHolder,
        position: Int
    ) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    inner class ThirdPartViewHolder(
        private val binding: ItemThirdPartBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ThirdPartListModel) {
            binding.icon.setImageResource(item.icon)
            binding.tvName.text = item.name
            if (item.fansPageString.isNotEmpty()) {
                binding.tvFansPage.text = item.fansPageString
                binding.tvFansPage.visibility = View.VISIBLE
            } else {
                binding.tvFansPage.visibility = View.GONE
            }
            binding.root.setOnClickListener {
                // 如果有綁定過 Facebook 粉絲專頁，再綁定 Instagram 時需拿粉絲專頁 id 去打 Graph api
                if (item.type == ThirdPartEnum.Instagram && item.fansPageString.isEmpty()) {
                    val fansPagesId = getFacebookFansPageId()
                    fansPagesId?.let {
                        item.id = it
                    }
                }
                itemClickCallBack.invoke(item)
            }
        }
    }
}
