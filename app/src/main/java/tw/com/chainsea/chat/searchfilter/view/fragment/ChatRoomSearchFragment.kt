package tw.com.chainsea.chat.searchfilter.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.config.BundleKey
import tw.com.chainsea.chat.databinding.FragmentChatRoomSearchBinding
import tw.com.chainsea.chat.network.contact.ViewModelFactory
import tw.com.chainsea.chat.searchfilter.view.activity.SearchMessageListActivity
import tw.com.chainsea.chat.searchfilter.viewmodel.ChatRoomSearchViewModel
import tw.com.chainsea.chat.searchfilter.viewmodel.SearchFilterSharedViewModel
import tw.com.chainsea.chat.ui.activity.ChatActivity
import tw.com.chainsea.chat.ui.activity.ChatNormalActivity
import tw.com.chainsea.chat.ui.adapter.GlobalSearchFilterListAdapter
import tw.com.chainsea.chat.util.ThemeHelper

class ChatRoomSearchFragment : Fragment() {
    private lateinit var chatRoomSearchViewModel: ChatRoomSearchViewModel
    private val sharedViewModel by activityViewModels<SearchFilterSharedViewModel>()
    private lateinit var globalSearchFilterListAdapter: GlobalSearchFilterListAdapter
    private lateinit var binding: FragmentChatRoomSearchBinding
    private var keyWord = ""

    companion object {
        fun newInstance() = ChatRoomSearchFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = createDataBindingView(inflater, container)

    private fun createDataBindingView(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DataBindingUtil
        .inflate<FragmentChatRoomSearchBinding>(
            inflater,
            R.layout.fragment_chat_room_search,
            container,
            false
        ).apply {
            binding = this
            init()
            chatRoomSearchViewModel.apply {
                when (sharedViewModel.sharedPageType.value) {
                    SearchFilterSharedViewModel.SearchFilterPageType.MESSAGE_TRANSFER, // 轉發
                    SearchFilterSharedViewModel.SearchFilterPageType.SHARE_IN -> { // 分享
                        initData()

                        sendQueryList
                            .onEach {
                                globalSearchFilterListAdapter.setData(it.first, it.second)
                                rvChatRoomSearchList.adapter = globalSearchFilterListAdapter
                                scopeNoData.getRoot().visibility = if (it.second.isNotEmpty()) View.GONE else View.VISIBLE
                                tvHint.visibility = if (it.second.isNotEmpty()) View.VISIBLE else View.GONE
                                sharedViewModel.sendTotalResultCount.emit(Pair(SearchFilterSharedViewModel.SearchResultTab.CHAT_ROOM, it.third))
                                sharedViewModel.sendCollectDataDone.emit(1)
                            }.launchIn(viewLifecycleOwner.lifecycleScope)

                        sendChatRoomSelectedItem
                            .onEach {
                                sharedViewModel.sendEmployeeSelectedItem.emit(Pair(it.first, it.second))
                            }.launchIn(viewLifecycleOwner.lifecycleScope)

                        sendSelectedItemList
                            .onEach {
                                globalSearchFilterListAdapter.setData(it.first, it.second)
                            }.launchIn(viewLifecycleOwner.lifecycleScope)
                    }
                    else -> {
                        sendQueryList
                            .onEach {
                                globalSearchFilterListAdapter.setData(it.first, it.second)
                                if (it.first.isNotEmpty()) {
                                    rvChatRoomSearchList.adapter = globalSearchFilterListAdapter
                                }
                                scopeNoData.getRoot().visibility = if (it.second.isNotEmpty()) View.GONE else View.VISIBLE
                                tvHint.visibility = if (it.second.isNotEmpty()) View.VISIBLE else View.GONE
                                sharedViewModel.sendTotalResultCount.emit(Pair(SearchFilterSharedViewModel.SearchResultTab.CHAT_ROOM, if (it.first.isNotEmpty()) it.third else -1))
                                sharedViewModel.sendCollectDataDone.emit(1)
                            }.launchIn(viewLifecycleOwner.lifecycleScope)

                        navigateToChatRoom
                            .onEach {
                                if (it.first.isNotEmpty()) {
                                    sharedViewModel.sendGlobalSearchNewRecord.emit(Pair(it.first, it.second))
                                }
                                if (it.second.type == ChatRoomType.services ||
                                    it.second.type == ChatRoomType.subscribe ||
                                    it.second.type == ChatRoomType.serviceMember
                                ) {
                                    startActivity(
                                        Intent(requireActivity(), ChatActivity::class.java)
                                            .putExtra(BundleKey.EXTRA_SESSION_ID.key(), it.second.id)
                                    )
                                } else {
                                    startActivity(
                                        Intent(requireActivity(), ChatNormalActivity::class.java)
                                            .putExtra(BundleKey.EXTRA_SESSION_ID.key(), it.second.id)
                                    )
                                }
                            }.launchIn(viewLifecycleOwner.lifecycleScope)

                        navigateToMessageList
                            .onEach {
                                if (it.first.isNotEmpty()) {
                                    sharedViewModel.sendGlobalSearchNewRecord.emit(Pair(it.first, it.third))
                                }
                                if (it.second.size == 1) {
                                    if (it.third.type == ChatRoomType.services ||
                                        it.third.type == ChatRoomType.subscribe ||
                                        it.third.type == ChatRoomType.serviceMember
                                    ) {
                                        startActivity(
                                            Intent(requireActivity(), ChatActivity::class.java)
                                                .putExtra(BundleKey.EXTRA_SESSION_ID.key(), it.third.id)
                                                .putExtra(BundleKey.EXTRA_MESSAGE.key(), it.second.first())
                                                .putExtra(BundleKey.SEARCH_KEY.key(), it.first)
                                        )
                                    } else {
                                        startActivity(
                                            Intent(requireActivity(), ChatNormalActivity::class.java)
                                                .putExtra(BundleKey.EXTRA_SESSION_ID.key(), it.third.id)
                                                .putExtra(BundleKey.EXTRA_MESSAGE.key(), it.second.first())
                                                .putExtra(BundleKey.SEARCH_KEY.key(), it.first)
                                        )
                                    }
                                } else {
                                    startActivity(
                                        Intent(requireActivity(), SearchMessageListActivity::class.java)
                                            .putExtra(BundleKey.SEARCH_KEY.key(), it.first)
                                            .putExtra(BundleKey.EXTRA_MESSAGE.key(), it.second.toTypedArray())
                                            .putExtra(BundleKey.EXTRA_SESSION_ID.key(), it.third.id)
                                    )
                                }
                            }.launchIn(viewLifecycleOwner.lifecycleScope)
                    }
                }
            }

            sharedViewModel.apply {
                when (sharedViewModel.sharedPageType.value) {
                    SearchFilterSharedViewModel.SearchFilterPageType.MESSAGE_TRANSFER, // 轉發
                    SearchFilterSharedViewModel.SearchFilterPageType.SHARE_IN -> { // 分享
                        sendInputText
                            .onEach {
                                if (it.isNotEmpty()) {
                                    chatRoomSearchViewModel.filter(it)
                                } else {
                                    chatRoomSearchViewModel.showAllData()
                                }
                            }.launchIn(viewLifecycleOwner.lifecycleScope)

                        sendSelectedMemberItem
                            .onEach {
                                chatRoomSearchViewModel.updateSelectedItemList(it) // 選中後更新背景樣式
                            }.launchIn(viewLifecycleOwner.lifecycleScope)

                        sendDismissSelectedMemberItem
                            .onEach {
                                chatRoomSearchViewModel.dismissSelectedItem(it) // 取消頭圖選中背景樣式
                            }.launchIn(viewLifecycleOwner.lifecycleScope)
                    }
                    else -> {
                        sendInputText
                            .onEach {
                                chatRoomSearchViewModel.globalFilter(sharedViewModel.ownerId.value, it)
                            }.launchIn(viewLifecycleOwner.lifecycleScope)
                    }
                }
            }
        }.root

    private fun initData() {
        chatRoomSearchViewModel.initChatRoomDiscussFriendsData(
            sharedViewModel.ownerId.value
        )
    }

    private fun init() {
        initViewModel()
        globalSearchFilterListAdapter =
            GlobalSearchFilterListAdapter(
                sharedViewModel.ownerId.value,
                chatRoomSearchViewModel,
                sharedViewModel.sharedPageType.value,
                this,
                ThemeHelper.isGreenTheme()
            )
    }

    private fun initViewModel() {
        val factory = ViewModelFactory(requireActivity().application)
        chatRoomSearchViewModel = ViewModelProvider(this, factory)[ChatRoomSearchViewModel::class.java]
    }
}
