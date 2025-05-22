package tw.com.chainsea.chat.base

import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.viewpager2.adapter.FragmentStateAdapter

class BaseViewPager2Adapter(fa: FragmentActivity) :
    FragmentStateAdapter(fa) {

    private val data = ArrayList<Pair<String, Fragment>>()

    fun setData(newData: ArrayList<Pair<String, Fragment>>) {
        val callback = DiffCallback(data, newData)
        val diff = DiffUtil.calculateDiff(callback)
        data.clear()
        data.addAll(newData)
        diff.dispatchUpdatesTo(this)
    }

    override fun createFragment(position: Int): Fragment {
        return data[position].second
    }

    override fun getItemCount(): Int {
        return data.size
    }

    private class DiffCallback(
        var oldData: List<Pair<String, Fragment>>,
        var newData: List<Pair<String, Fragment>>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return oldData.size
        }

        override fun getNewListSize(): Int {
            return newData.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldData[oldItemPosition] === newData[newItemPosition]
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldData[oldItemPosition].hashCode() == newData[newItemPosition].hashCode()
        }
    }
}
