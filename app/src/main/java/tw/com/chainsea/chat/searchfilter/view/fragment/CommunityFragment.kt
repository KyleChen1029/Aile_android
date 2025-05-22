package tw.com.chainsea.chat.searchfilter.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.FragmentCommunityRoomTabBinding
import tw.com.chainsea.chat.ui.adapter.SearchFilterListAdapter
import tw.com.chainsea.chat.searchfilter.viewmodel.CommunityViewModel
import tw.com.chainsea.chat.searchfilter.viewmodel.SearchFilterSharedViewModel
import tw.com.chainsea.chat.util.ThemeHelper

class CommunityFragment : Fragment() {

    private val communityViewModel by viewModels<CommunityViewModel>()
    //the data connection between SearchFilterFragment and viewpager fragment(ContactPersonFragment)
    private val sharedViewModel by activityViewModels<SearchFilterSharedViewModel>()
    private lateinit var searchFilterListAdapter: SearchFilterListAdapter
    private lateinit var binding: FragmentCommunityRoomTabBinding
    companion object {
        fun newInstance() = CommunityFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return createDataBindingView(inflater, container)
    }

    private fun createDataBindingView(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ) = DataBindingUtil.inflate<FragmentCommunityRoomTabBinding>(
        inflater,
        R.layout.fragment_community_room_tab,
        container,
        false
    ).apply {
        binding = this
        init()
        communityViewModel.apply {
            sendQueryList.onEach {
                scopeSectioned.visibility = if(it.first.isEmpty()) View.GONE else View.VISIBLE
                searchFilterListAdapter.setData(it.first, it.second.toMutableList())
                scopeNoData.getRoot().visibility = if(it.second.isNotEmpty()) View.GONE else View.VISIBLE
            }.launchIn(viewLifecycleOwner.lifecycleScope)

            sendFilterQueryList.onEach {
                if(it.first.isNotEmpty() && it.second.isNotEmpty())
                    sectionedTitle.text = getString(R.string.text_sectioned_communities_filter, it.second.size)
                scopeSectioned.visibility = if(it.first.isEmpty()) View.GONE else View.VISIBLE
                searchFilterListAdapter.setData(it.first, it.second.toMutableList())
                rvCommunityChatRoomList.adapter = searchFilterListAdapter
                scopeNoData.getRoot().visibility = if(it.second.isNotEmpty()) View.GONE else View.VISIBLE
                if(it.first.isEmpty())
                    rvCommunityChatRoomList.post {
                        rvCommunityChatRoomList.scrollToPosition(0)
                    }
            }.launchIn(viewLifecycleOwner.lifecycleScope)

            sendEmployeeSelectedList.onEach {
                //更新SearchFilterFragment下方頭圖列
                //sharedViewModel.sendEmployeeSelectedList.emit(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

            sendChatRoomText.onEach {
                //更新點選過的文字紀錄
                sharedViewModel.employeesTextRecordList.emit(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

            sendCommunitySelectedItem.onEach {
                //更新SearchFilterFragment下方頭圖列
                sharedViewModel.sendEmployeeSelectedItem.emit(Pair(it.first, it.second))
                rvCommunityChatRoomList.post {
                    rvCommunityChatRoomList.scrollToPosition(it.third)
                }
            }.launchIn(viewLifecycleOwner.lifecycleScope)
        }

        sharedViewModel.apply {
            sendInputText.onEach {
                communityViewModel.filter(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

            sendSelectedMemberItem.onEach {
                communityViewModel.updateSelectedItemList(it) //選中後更新背景樣式
            }.launchIn(viewLifecycleOwner.lifecycleScope)

            sendDismissSelectedMemberItem.onEach {
                communityViewModel.dismissSelectedItem(it) //取消選中背景樣式
            }.launchIn(viewLifecycleOwner.lifecycleScope)

            sendAvatarSelectedItem.onEach {//選中頭圖紀錄背景樣式
                communityViewModel.updateSelectedItemList(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)
        }
        scopeSectioned.setOnClickListener {
            rvCommunityChatRoomList.visibility = if(rvCommunityChatRoomList.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            icExtend.setImageResource(if(rvCommunityChatRoomList.visibility == View.VISIBLE) R.drawable.ic_arrow_down else R.drawable.ic_arrow_top)
        }
        lifecycleOwner = this@CommunityFragment.viewLifecycleOwner
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        communityViewModel.getAllCommunityRoom()
    }

    fun init() {
        searchFilterListAdapter = SearchFilterListAdapter(sharedViewModel.ownerId.value, communityViewModel, ThemeHelper.isGreenTheme())
        binding.rvCommunityChatRoomList.adapter = searchFilterListAdapter
    }
}