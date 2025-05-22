package tw.com.chainsea.chat.chatroomfilter

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import tw.com.chainsea.chat.chatroomfilter.adpter.FilterFileAdapter

class FilterFileFragment :
    BaseChatRoomFilterFragment() {

    companion object {
        fun newInstance(multipleChoiceCallback: MultipleChoiceCallback?): FilterFileFragment {
            val filterFileFragment = FilterFileFragment()
            filterFileFragment.multipleChoiceCallback = multipleChoiceCallback
            return filterFileFragment
        }
    }


    private val filterFileAdapter = FilterFileAdapter()
    private var multipleChoiceCallback: MultipleChoiceCallback? = null

    override fun setIsMultipleChoiceMode(isMultipleChoiceMode: Boolean) {
        filterFileAdapter.apply {
            setIsMultipleChoiceMode(isMultipleChoiceMode)
            this.multipleChoiceCallback = this@FilterFileFragment.multipleChoiceCallback
        }
    }

    override fun getMultipleChoiceList(): MutableList<*> {
        return filterFileAdapter.getMultipleChoiceList()
    }

    override fun isHasData(): Boolean {
        return filterFileAdapter.itemCount > 0
    }

    fun setFilterFileList(list: List<BaseFilterModel>) {
        filterFileAdapter.setData(list)
        checkDataListIsEmpty(list)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
    }

    private fun initAdapter() {
        context?.let {
            binding?.rvList?.apply {
                layoutManager = LinearLayoutManager(it)
                adapter = filterFileAdapter
                addItemDecoration(stickHeaderDecoration)
            }
        }
    }
}