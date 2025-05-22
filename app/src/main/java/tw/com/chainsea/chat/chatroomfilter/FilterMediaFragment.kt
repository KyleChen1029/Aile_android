package tw.com.chainsea.chat.chatroomfilter

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import tw.com.chainsea.ce.sdk.bean.msg.MessageType
import tw.com.chainsea.chat.chatroomfilter.adpter.FilterMediaAdapter


class FilterMediaFragment : BaseChatRoomFilterFragment() {

    companion object {
        fun newInstance(roomId: String, multipleChoiceCallback: MultipleChoiceCallback?): FilterMediaFragment {
            val filterMediaFragment = FilterMediaFragment()
            filterMediaFragment.roomId = roomId
            filterMediaFragment.multipleChoiceCallback = multipleChoiceCallback
            return filterMediaFragment
        }
    }

    private var roomId: String = ""
    private var multipleChoiceCallback: MultipleChoiceCallback? = null
    private val filterMediaList = mutableListOf<BaseFilterModel>()

    private val filterMediaAdapter = FilterMediaAdapter()

    override fun setIsMultipleChoiceMode(isMultipleChoiceMode: Boolean) {
        filterMediaAdapter.apply {
            setIsMultipleChoiceMode(isMultipleChoiceMode)
            multipleChoiceCallback = this@FilterMediaFragment.multipleChoiceCallback
        }
    }

    override fun getMultipleChoiceList(): MutableList<*> {
        return filterMediaAdapter.getMultipleChoiceList()
    }

    override fun isHasData(): Boolean {
        return filterMediaAdapter.itemCount > 0
    }

    fun setMessageSort(sort: String) {
        filterMediaAdapter.setSort(sort)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        filterMediaAdapter.setRoomId(roomId)
        initAdapter()
    }

    fun setFilterMediaList(list: List<BaseFilterModel>) {
        filterMediaList.clear()
        filterMediaList.addAll(list)
        filterMediaAdapter.setData(list)
        checkDataListIsEmpty(list)
    }

    private fun initAdapter() {
        context?.let {
            binding?.rvList?.apply {
                val gridLayoutManager = GridLayoutManager(it, 3)
                gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (filterMediaList[position].type == MessageType.SYSTEM) {
                            3
                        } else {
                            1
                        }
                    }
                }
                layoutManager = gridLayoutManager
                adapter = filterMediaAdapter
                addItemDecoration(stickHeaderDecoration)
            }
        }
    }
}