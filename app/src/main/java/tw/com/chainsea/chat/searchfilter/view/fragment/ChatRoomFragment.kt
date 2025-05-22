package tw.com.chainsea.chat.searchfilter.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.common.collect.Lists
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.FragmentChatRoomTabBinding
import tw.com.chainsea.chat.network.contact.ViewModelFactory
import tw.com.chainsea.chat.ui.adapter.SearchFilterListAdapter
import tw.com.chainsea.chat.searchfilter.viewmodel.ChatRoomViewModel
import tw.com.chainsea.chat.searchfilter.viewmodel.SearchFilterSharedViewModel
import tw.com.chainsea.chat.util.ThemeHelper

class ChatRoomFragment : Fragment() {

    private lateinit var chatRoomViewModel: ChatRoomViewModel
    //the data connection between SearchFilterFragment and viewpager fragment(ContactPersonFragment)
    private val sharedViewModel by activityViewModels<SearchFilterSharedViewModel>()
    private lateinit var searchFilterListAdapter: SearchFilterListAdapter
    private lateinit var binding: FragmentChatRoomTabBinding
    companion object {
        fun newInstance() = ChatRoomFragment()
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
    ) = DataBindingUtil.inflate<FragmentChatRoomTabBinding>(
        inflater,
        R.layout.fragment_chat_room_tab,
        container,
        false
    ).apply {
        binding = this
        init()
        chatRoomViewModel.apply {
            sendQueryList.onEach {
                searchFilterListAdapter.setData(it.first, it.second.toMutableList())
                scopeNoData.getRoot().visibility = if(it.second.isNotEmpty()) View.GONE else View.VISIBLE
            }.launchIn(viewLifecycleOwner.lifecycleScope)

            sendFilterQueryList.onEach {
                searchFilterListAdapter.setData(it.first, it.second.toMutableList())
                scopeNoData.getRoot().visibility = if(it.second.isNotEmpty()) View.GONE else View.VISIBLE
                sharedViewModel.sendTotalResultCount.emit(Pair(SearchFilterSharedViewModel.SearchResultTab.CHAT_ROOM, if(it.first.isNotEmpty()) it.second.size else -1))
                binding.rvChatRoomList.adapter = searchFilterListAdapter
            }.launchIn(viewLifecycleOwner.lifecycleScope)

            sendChatRoomText.onEach {
                //更新點選過的文字紀錄
                sharedViewModel.employeesTextRecordList.emit(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

            sendEmployeeSelectedItem.onEach {
                //更新SearchFilterFragment下方頭圖列
                sharedViewModel.sendEmployeeSelectedItem.emit(Pair(it.first, it.second))
            }.launchIn(viewLifecycleOwner.lifecycleScope)
        }

        sharedViewModel.apply {
            sendInputText.onEach {
                chatRoomViewModel.filter(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

            sendSelectedMemberItem.onEach {
                chatRoomViewModel.updateSelectedItemList(it) //選中後更新背景樣式
            }.launchIn(viewLifecycleOwner.lifecycleScope)

            sendDismissSelectedMemberItem.onEach {
                chatRoomViewModel.dismissSelectedItem(it) //取消選中背景樣式
            }.launchIn(viewLifecycleOwner.lifecycleScope)

            sendAvatarSelectedItem.onEach {//選中頭圖紀錄背景樣式
                chatRoomViewModel.updateSelectedItemList(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)
        }
        lifecycleOwner = this@ChatRoomFragment.viewLifecycleOwner
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val chatRoomTypeList = Lists.newArrayList<ChatRoomType>()
        chatRoomTypeList.apply {
            add(ChatRoomType.friend)
        }
        chatRoomViewModel.getAllChatRoom(
            sharedViewModel.ownerId.value,
            chatRoomTypeList,
            sharedViewModel.memberIds
        )
    }

    fun init() {
        initViewModel()
        searchFilterListAdapter = SearchFilterListAdapter(sharedViewModel.ownerId.value, chatRoomViewModel, ThemeHelper.isGreenTheme())
        binding.rvChatRoomList.adapter = searchFilterListAdapter
    }

    fun initViewModel() {
        val factory = ViewModelFactory(requireActivity().application)
        chatRoomViewModel = ViewModelProvider(this, factory)[ChatRoomViewModel::class.java]
    }
}