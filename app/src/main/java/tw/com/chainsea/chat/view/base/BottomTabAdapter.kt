package tw.com.chainsea.chat.view.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.common.collect.Lists
import tw.com.chainsea.chat.databinding.ItemBottomTabBinding
import tw.com.chainsea.chat.util.UnreadUtil

class BottomTabAdapter(private val itemClickCallback: (tab: BottomTab) -> (Unit)) :
    RecyclerView.Adapter<BottomTabAdapter.TabViewHolder>() {


    private val tabData = Lists.newArrayList<BottomTab>()

    fun setBottomData(bottomTabList: List<BottomTab>) {
        val originSelectedTab = tabData.filter {it.isSelected }.getOrElse(0) { bottomTabList[0] }
        if (originSelectedTab.type != BottomTabEnum.MAIN) {
            bottomTabList.forEach {
                it.isSelected = false
                if (it.type == originSelectedTab.type) it.isSelected = true
            }
        }
        this.tabData.clear()
        this.tabData.addAll(bottomTabList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabViewHolder {
        val viewBinding =
            ItemBottomTabBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TabViewHolder(viewBinding)
    }

    override fun getItemCount(): Int {
        return tabData.size
    }

    override fun onBindViewHolder(holder: TabViewHolder, position: Int) {
        holder.bind(tabData[position])
    }

    fun setUnReadNum(count: Int, type: BottomTabEnum) {
        tabData.forEachIndexed { index, bottomTab ->
            if (bottomTab.type == type) {
                bottomTab.unRead = count
                notifyItemChanged(index, false) // 不要有動畫
                return
            }
        }
    }

    fun isCurrentTab(type: BottomTabEnum): Boolean {
        val selected =  tabData.filter { tab-> tab.isSelected }
        return if (selected.isNotEmpty()) {
            selected[0].type == type
        } else {
            false
        }
    }

    fun getTabTitle(type: BottomTabEnum): String {
        val title = tabData.filter { tab -> tab.type == type }
        return if (title.isNotEmpty()) {
            title[0].title
        } else {
            ""
        }
    }

    fun getCurrentTab(): BottomTab? {
        return tabData.firstOrNull { tab -> tab.isSelected }
    }

    inner class TabViewHolder(private val binding: ItemBottomTabBinding) :
        RecyclerView.ViewHolder(binding.root) {


        fun bind(tab: BottomTab) {
            if (tab.unRead > 0) {
                binding.tvUnreadNumber.visibility = View.VISIBLE
                binding.tvUnreadNumber.text = UnreadUtil.getUnreadText(tab.unRead)
            } else {
                binding.tvUnreadNumber.visibility = View.GONE
            }
            binding.ivTab.isSelected = tab.isSelected
            binding.ivTab.setImageResource(tab.src)
            binding.ivTab.setOnClickListener {
                itemClickCallback.invoke(tab)
                if (tab.isSelected) return@setOnClickListener
                setIsSelected(tab)
            }
        }

        private fun setIsSelected(tab: BottomTab) {
            tabData.forEachIndexed { index, bottomTab ->
                bottomTab.isSelected = tab.type == bottomTab.type
                notifyItemChanged(index)
            }
        }
    }
}
