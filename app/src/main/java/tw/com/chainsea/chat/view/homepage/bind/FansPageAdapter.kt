package tw.com.chainsea.chat.view.homepage.bind

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.common.collect.Lists
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tw.com.chainsea.ce.sdk.network.model.response.FansPageModel
import tw.com.chainsea.chat.databinding.ItemBindFansPageBinding

/**
 * 選擇粉絲專頁的 Adapter
 * */
class FansPageAdapter(
    private val callback: (Boolean) -> Unit
) : RecyclerView.Adapter<FansPageAdapter.FansPageViewHolder>() {
    private val fansPageList = Lists.newArrayList<FansPageModel>()

    @SuppressLint("NotifyDataSetChanged")
    fun setData(fansPageList: List<FansPageModel>) {
        this.fansPageList.clear()
        this.fansPageList.addAll(fansPageList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FansPageViewHolder {
        val binding =
            ItemBindFansPageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FansPageViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: FansPageViewHolder,
        position: Int
    ) {
        holder.bind(fansPageList[position], callback)
    }

    override fun getItemCount(): Int = fansPageList.size

    suspend fun getSelectedFansPage(): FansPageModel? =
        withContext(Dispatchers.IO) {
            return@withContext fansPageList.find { it.isSelected }
        }

    private fun clearSelected() {
        fansPageList.forEach {
            it.isSelected = false
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    inner class FansPageViewHolder(
        private val binding: ItemBindFansPageBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            fansPage: FansPageModel,
            callback: (Boolean) -> Unit
        ) {
            binding.fansPageName.text = fansPage.name
            if (fansPage.isSelected) {
                binding.root.setBackgroundColor(Color.parseColor("#d8d8d8"))
            } else {
                binding.root.setBackgroundColor(Color.WHITE)
            }
            binding.root.setOnClickListener {
                clearSelected()
                fansPage.isSelected = !fansPage.isSelected
                notifyDataSetChanged()
                CoroutineScope(Dispatchers.Main).launch {
                    callback.invoke(getSelectedFansPage() != null)
                }
            }
        }
    }
}
