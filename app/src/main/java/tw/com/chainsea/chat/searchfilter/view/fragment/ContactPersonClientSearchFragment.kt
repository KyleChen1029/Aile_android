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
import tw.com.chainsea.ce.sdk.bean.GroupEntity
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.config.BundleKey
import tw.com.chainsea.chat.databinding.FragmentContactPersonClientSearchBinding
import tw.com.chainsea.chat.lib.ToastUtils
import tw.com.chainsea.chat.network.contact.ViewModelFactory
import tw.com.chainsea.chat.searchfilter.viewmodel.ContactPersonClientSearchViewModel
import tw.com.chainsea.chat.searchfilter.viewmodel.SearchFilterSharedViewModel
import tw.com.chainsea.chat.service.ActivityTransitionsControl
import tw.com.chainsea.chat.ui.activity.ChatNormalActivity
import tw.com.chainsea.chat.ui.adapter.GlobalSearchFilterListAdapter
import tw.com.chainsea.chat.util.ThemeHelper

class ContactPersonClientSearchFragment : Fragment() {
    companion object {
        fun newInstance() = ContactPersonClientSearchFragment()
    }

    private lateinit var contactPersonClientSearchViewModel: ContactPersonClientSearchViewModel
    private val sharedViewModel by activityViewModels<SearchFilterSharedViewModel>()
    private lateinit var binding: FragmentContactPersonClientSearchBinding
    private lateinit var globalSearchFilterListAdapter: GlobalSearchFilterListAdapter
    private var keyWord = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = createDataBindingView(inflater, container)

    private fun createDataBindingView(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DataBindingUtil
        .inflate<FragmentContactPersonClientSearchBinding>(
            inflater,
            R.layout.fragment_contact_person_client_search,
            container,
            false
        ).apply {
            binding = this
            init()
            contactPersonClientSearchViewModel.apply {
                when (sharedViewModel.sharedPageType.value) {
                    SearchFilterSharedViewModel.SearchFilterPageType.MESSAGE_TRANSFER, // 轉發
                    SearchFilterSharedViewModel.SearchFilterPageType.SHARE_IN -> { // 分享
                        initData() // 初始化資料
                        employeeCustomerFilterList
                            .onEach {
                                globalSearchFilterListAdapter.setData(it.first, it.second)
                                rvPeopleList.adapter = globalSearchFilterListAdapter
                                scopeNoData.getRoot().visibility = if (it.second.isNotEmpty()) View.GONE else View.VISIBLE
                                sharedViewModel.sendTotalResultCount.emit(Pair(SearchFilterSharedViewModel.SearchResultTab.CONTACT, it.third))
                                sharedViewModel.sendCollectDataDone.emit(1)
                            }.launchIn(viewLifecycleOwner.lifecycleScope)

                        sendContactPersonSelectedItem
                            .onEach {
                                sharedViewModel.sendEmployeeSelectedItem.emit(Pair(it.first, it.second))
                            }.launchIn(viewLifecycleOwner.lifecycleScope)

                        sendSelectedItem
                            .onEach {
                                globalSearchFilterListAdapter.setData(it.first, it.second)
                            }.launchIn(viewLifecycleOwner.lifecycleScope)
                    }

                    else -> {
                        employeeCustomerFilterList
                            .onEach {
                                globalSearchFilterListAdapter.setData(it.first, it.second)
                                if (it.first.isNotEmpty()) {
                                    rvPeopleList.adapter = globalSearchFilterListAdapter
                                }
                                scopeNoData.getRoot().visibility = if (it.second.isNotEmpty()) View.GONE else View.VISIBLE
                                sharedViewModel.sendTotalResultCount.emit(Pair(SearchFilterSharedViewModel.SearchResultTab.CONTACT, if (it.first.isNotEmpty()) it.third else -1))
                                sharedViewModel.sendCollectDataDone.emit(1)
                            }.launchIn(viewLifecycleOwner.lifecycleScope)

                        navigateToChatRoom
                            .onEach {
                                if (it.first.isNotEmpty()) {
                                    sharedViewModel.sendGlobalSearchNewRecord.emit(Pair(it.first, it.second))
                                }
                                when (it.second) {
                                    is UserProfileEntity -> {
                                        if ((it.second as UserProfileEntity).roomId.isNullOrEmpty()) {
                                            startActivity(
                                                Intent(requireActivity(), ChatNormalActivity::class.java)
                                                    .putExtra(BundleKey.USER_ID.key(), (it.second as UserProfileEntity).id)
                                                    .putExtra(BundleKey.USER_NICKNAME.key(), (it.second as UserProfileEntity).nickName)
                                            )
                                        } else {
                                            ActivityTransitionsControl.navigateToChat(
                                                requireActivity(),
                                                (it.second as UserProfileEntity).roomId
                                            ) { i, _ -> startActivity(i) }
                                        }
                                    }

                                    is GroupEntity -> {
                                        ActivityTransitionsControl.groupNavigateToChatRoom(
                                            requireActivity(),
                                            (it.second as GroupEntity).id,
                                            ContactPersonClientSearchFragment::class.java.simpleName
                                        ) { intent: Intent, _: String? ->
                                            startActivity(intent)
                                        }
                                    }

                                    is ChatRoomEntity -> {
                                        ActivityTransitionsControl.navigateToChat(
                                            requireActivity(),
                                            (it.second as ChatRoomEntity).id
                                        ) { intent: Intent, _: String? ->
                                            startActivity(intent)
                                        }
                                    }
                                }
                            }.launchIn(viewLifecycleOwner.lifecycleScope)

                        navigateToCustomerChatRoom
                            .onEach {
                                if (it.first.isNotEmpty()) {
                                    sharedViewModel.sendGlobalSearchNewRecord.emit(Pair(it.first, it.third))
                                }
                                ActivityTransitionsControl.navigateToChat(
                                    requireActivity(),
                                    it.second.id
                                ) { intent: Intent, _: String? ->
                                    startActivity(intent)
                                }
                            }.launchIn(viewLifecycleOwner.lifecycleScope)

                        navigateToCustomerChatRoomFailure
                            .onEach {
                                ToastUtils.showToast(requireActivity(), it)
                            }.launchIn(viewLifecycleOwner.lifecycleScope)
                    }
                }
            }

            sharedViewModel.apply {
                when (sharedPageType.value) {
                    SearchFilterSharedViewModel.SearchFilterPageType.MESSAGE_TRANSFER, // 轉發
                    SearchFilterSharedViewModel.SearchFilterPageType.SHARE_IN -> { // 分享

                        sendInputText
                            .onEach {
                                if (it.isNotEmpty()) {
                                    contactPersonClientSearchViewModel.filter(it)
                                } else {
                                    contactPersonClientSearchViewModel.showAllData()
                                }
                            }.launchIn(viewLifecycleOwner.lifecycleScope)

                        sendSelectedMemberItem
                            .onEach {
                                contactPersonClientSearchViewModel.updateSelectedItemList(it) // 選中後更新背景樣式
                            }.launchIn(viewLifecycleOwner.lifecycleScope)

                        sendDismissSelectedMemberItem
                            .onEach {
                                contactPersonClientSearchViewModel.dismissSelectedItem(it) // 取消頭圖選中背景樣式
                            }.launchIn(viewLifecycleOwner.lifecycleScope)
                    }

                    else -> { // 全局搜尋
                        sendInputText
                            .onEach {
                                contactPersonClientSearchViewModel.apply {
                                    globalFilter(it, sharedViewModel.ownerId.value)
                                }
                            }.launchIn(viewLifecycleOwner.lifecycleScope)
                    }
                }
            }

            lifecycleOwner = this@ContactPersonClientSearchFragment.viewLifecycleOwner
        }.root

    private fun initData() {
        contactPersonClientSearchViewModel.initData(sharedViewModel.ownerId.value)
    }

    fun init() {
        initViewModel()
        globalSearchFilterListAdapter = GlobalSearchFilterListAdapter(sharedViewModel.ownerId.value, contactPersonClientSearchViewModel, sharedViewModel.sharedPageType.value, this, ThemeHelper.isGreenTheme())
    }

    private fun initViewModel() {
        val factory = ViewModelFactory(requireActivity().application)
        contactPersonClientSearchViewModel = ViewModelProvider(this, factory)[ContactPersonClientSearchViewModel::class.java]
    }
}
