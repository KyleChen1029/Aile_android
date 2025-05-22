package tw.com.chainsea.chat.chatroomfilter

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import tw.com.chainsea.chat.chatroomfilter.adpter.FilterLinkAdapter
import tw.com.chainsea.chat.chatroomfilter.model.FilterLinkModel

class FilterLinkFragment : BaseChatRoomFilterFragment() {


    companion object {
        fun newInstance(multipleChoiceCallback: MultipleChoiceCallback?): FilterLinkFragment {
            val filterLinkFragment = FilterLinkFragment()
            filterLinkFragment.multipleChoiceCallback = multipleChoiceCallback
            return filterLinkFragment
        }
    }


    private val filterLinkAdapter = FilterLinkAdapter()
    private var multipleChoiceCallback: MultipleChoiceCallback? = null

    override fun setIsMultipleChoiceMode(isMultipleChoiceMode: Boolean) {
        filterLinkAdapter.apply {
            setIsMultipleChoiceMode(isMultipleChoiceMode)
            multipleChoiceCallback = this@FilterLinkFragment.multipleChoiceCallback
        }
    }

    override fun getMultipleChoiceList(): MutableList<*> {
        return filterLinkAdapter.getMultipleChoiceList()
    }

    override fun isHasData(): Boolean {
        return filterLinkAdapter.itemCount > 0
    }

    fun setFilterLinkList(list: List<BaseFilterModel>) {
        filterLinkAdapter.setData(list)
        checkDataListIsEmpty(list)
    }


    fun onWebMetaDataGet(filterLinkModel: FilterLinkModel) {
        filterLinkAdapter.setWebMetaData(filterLinkModel)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
    }

    private fun initAdapter() {
        context?.let {
            binding?.rvList?.apply {
                layoutManager = LinearLayoutManager(it)
                adapter = filterLinkAdapter
                addItemDecoration(stickHeaderDecoration)
            }
        }
    }
}