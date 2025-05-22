package tw.com.chainsea.chat.searchfilter.view.fragment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.util.Pair
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import tw.com.chainsea.android.common.event.KeyboardHelper
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.base.BaseViewPager2Adapter
import tw.com.chainsea.chat.config.BundleKey
import tw.com.chainsea.chat.databinding.FragmentSearchFilterBinding
import tw.com.chainsea.chat.extension.onSubmit
import tw.com.chainsea.chat.searchfilter.viewmodel.SearchFilterSharedViewModel
import tw.com.chainsea.chat.searchfilter.viewmodel.SearchFilterViewModel
import tw.com.chainsea.chat.service.ActivityTransitionsControl
import tw.com.chainsea.chat.ui.activity.ChatActivity
import tw.com.chainsea.chat.ui.activity.ChatNormalActivity
import tw.com.chainsea.chat.ui.adapter.EmployeesSelectedListAdapter
import tw.com.chainsea.chat.ui.adapter.SearchEmployeesAvatarRecordListAdapter
import tw.com.chainsea.chat.ui.adapter.SearchEmployeesTextRecordListAdapter
import tw.com.chainsea.chat.util.ThemeHelper
import tw.com.chainsea.chat.view.base.BottomTabEnum
import java.util.LinkedList

class SearchFilterFragment : Fragment() {
    private val searchFilterViewModel by viewModels<SearchFilterViewModel>()
    private val sharedViewModel by activityViewModels<SearchFilterSharedViewModel>()

    private lateinit var binding: FragmentSearchFilterBinding
    private var mFragments = LinkedList<Fragment>()
    private var mFragmentPairs = ArrayList<Pair<String, Fragment>>()
    private lateinit var employeeSelectedAdapter: EmployeesSelectedListAdapter
    private lateinit var searchEmployeesAvatarRecordListAdapter: SearchEmployeesAvatarRecordListAdapter
    private lateinit var searchEmployeesTextRecordListAdapter: SearchEmployeesTextRecordListAdapter
    private lateinit var mViewPager2Adapter: BaseViewPager2Adapter
    private var searchAvatarRecordItemCount = 7
    private var searchTextRecordItemCount = 5
    private val searchPageRecord: MutableMap<SearchFilterSharedViewModel.SearchResultTab, Int> = mutableMapOf()
    private val selfId by lazy {
        TokenPref.getInstance(requireActivity()).userId
    }
    private val isGreenTheme: Boolean by lazy { ThemeHelper.isGreenTheme() }
    private val isServiceRoomTheme: Boolean by lazy { ThemeHelper.isServiceRoomTheme }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = createDataBindingView(inflater, container)

    private fun createDataBindingView(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DataBindingUtil
        .inflate<FragmentSearchFilterBinding>(
            inflater,
            R.layout.fragment_search_filter,
            container,
            false
        ).apply {
            binding = this
            init()
            viewModel =
                this@SearchFilterFragment.searchFilterViewModel.apply {
                    // 下方列表顯示被選中成員頭圖
                    allEmployeeSelectedList
                        .onEach {
                            employeeSelectedAdapter.setData(it.toMutableList(), sharedViewModel.sharedPageType.value, sharedViewModel.userMemberIds.size)
                            rvSelectList.visibility = if (it.isEmpty()) View.GONE else View.VISIBLE
                            sharedViewModel.selectedMemberList.emit(it.toMutableList()) // 更新上層按鈕顯示選中成員數量
                        }.launchIn(viewLifecycleOwner.lifecycleScope)

                    // 點選已儲存的搜尋文字顯示在輸入框
                    searchInput
                        .onEach {
                            etSearch.setText(it)
                            sharedViewModel.sendInputText.emit(it)
                            hideKeyboard()
                        }.launchIn(viewLifecycleOwner.lifecycleScope)

                    // 清除輸入框文字
                    sendClearInputText
                        .onEach {
                            clearInput()
                        }.launchIn(viewLifecycleOwner.lifecycleScope)

                    // 關閉鍵盤
                    sendCloseKeyboard
                        .onEach {
                            hideKeyboard()
                        }.launchIn(viewLifecycleOwner.lifecycleScope)

                    // 顯示點選過的頭圖紀錄
                    employeesAvatarRecordList
                        .onEach {
                            val filteredList =
                                it.filterNot { avatarRecord ->
                                    // 過濾頭圖紀錄中有discussMember裡的成員
                                    sharedViewModel.userMemberIds.any { b ->
                                        avatarRecord.id.equals(b)
                                    }
                                }
                            searchEmployeesAvatarRecordListAdapter.setData(
                                if (filteredList.size <= searchAvatarRecordItemCount) {
                                    filteredList.toMutableList()
                                } else {
                                    filteredList.subList(0, searchAvatarRecordItemCount).toMutableList()
                                }
                            )
                        }.launchIn(viewLifecycleOwner.lifecycleScope)

                    // 顯示搜尋並點選頭圖的文字紀錄
                    employeesTextRecordList
                        .onEach {
                            searchEmployeesTextRecordListAdapter.setData(
                                if (it.size <= searchTextRecordItemCount) {
                                    it.toMutableList()
                                } else {
                                    it.subList(0, searchTextRecordItemCount).toMutableList()
                                }
                            )
                            rvSearchTextRecord.adapter = searchEmployeesTextRecordListAdapter
                        }.launchIn(viewLifecycleOwner.lifecycleScope)

                    sendOnRemoveEmployeeItem
                        .onEach {
                            sharedViewModel.sendOnRemoveEmployeeItem.emit(it)
                        }.launchIn(viewLifecycleOwner.lifecycleScope)

                    // 下方列表顯示被選中成員頭圖
                    sharedViewModel.sendOwnerSelectedItem
                        .onEach {
                            // 處理Owner Avatar
                            getOwnerAvatar(it)
                        }.launchIn(viewLifecycleOwner.lifecycleScope)

                    sharedViewModel.sendEmployeeSelectedItem
                        .onEach {
                            // 處理被點選的成員
                            getSelectedMember(it.first, it.second, isAvatarRecordSelected = false)
                        }.launchIn(viewLifecycleOwner.lifecycleScope)

                    sendSelectedMemberList
                        .onEach {
                            employeeSelectedAdapter.setData(it.toMutableList(), sharedViewModel.sharedPageType.value, sharedViewModel.userMemberIds.size)
                            sharedViewModel.selectedMemberList.emit(it.toMutableList()) // 更新上層按鈕顯示選中成員數量
                            rvSelectList.visibility = if (it.isEmpty()) View.GONE else View.VISIBLE
                        }.launchIn(viewLifecycleOwner.lifecycleScope)

                    sendSelectedMemberItem
                        .onEach {
                            sharedViewModel.sendSelectedMemberItem.emit(it.first) // 更新下方所有TAB中的選中資料
                            sharedViewModel.handleDiscussRoomMemberName(it.first, it.second)
                        }.launchIn(viewLifecycleOwner.lifecycleScope)

                    sendDismissSelectedMemberItem
                        .onEach {
                            sharedViewModel.sendDismissSelectedMemberItem.emit(it) // 更新下方所有TAB中的取消選中資料
                            sharedViewModel.handleDiscussRoomMemberName(it, false) // 點選X取消選取
                        }.launchIn(viewLifecycleOwner.lifecycleScope)
                    sendAvatarSelectedItem
                        .onEach {
                            // 點選頭圖紀錄更新選取背景色
                            sharedViewModel.sendAvatarSelectedItem.emit(it)
                        }.launchIn(viewLifecycleOwner.lifecycleScope)

                    sendIntentToOpenChatRoom
                        .onEach {
                            startActivity(
                                Intent(requireActivity(), ChatNormalActivity::class.java)
                                    .putExtra(BundleKey.EXTRA_SESSION_ID.key(), it)
                            )
                        }.launchIn(viewLifecycleOwner.lifecycleScope)

                    sendIntentToOpenServiceNumberChatRoom
                        .onEach {
                            startActivity(
                                Intent(requireActivity(), ChatActivity::class.java)
                                    .putExtra(BundleKey.EXTRA_SESSION_ID.key(), it)
                            )
                        }.launchIn(viewLifecycleOwner.lifecycleScope)

                    sendIntentToOpenChatRoomForContact
                        .onEach {
                            if (it.roomId.isNullOrEmpty()) {
                                startActivity(
                                    Intent(requireActivity(), ChatNormalActivity::class.java)
                                        .putExtra(BundleKey.USER_ID.key(), it.id)
                                        .putExtra(BundleKey.USER_NICKNAME.key(), it.nickName)
                                )
                            } else {
                                ActivityTransitionsControl.navigateToChat(
                                    requireActivity(),
                                    it.roomId
                                ) { i, _ -> startActivity(i) }
                            }
                        }.launchIn(viewLifecycleOwner.lifecycleScope)

                    sendChangeViewPagerItem
                        .onEach {
                            binding.viewPager.currentItem = it
                        }.launchIn(viewLifecycleOwner.lifecycleScope)
                }

            sharedViewModel.apply {
                searchFilterViewModel.pageType.value = this.sharedPageType.value
                searchFilterViewModel.memberIds = this.memberIds
                // 儲存搜尋並點選頭圖的文字紀錄
                employeesTextRecordList
                    .onEach {
                        searchFilterViewModel.saveSearchTextRecord(it)
                    }.launchIn(viewLifecycleOwner.lifecycleScope)

                sendGlobalSearchNewRecord
                    .onEach {
                        // 全局搜尋更新記錄
                        searchFilterViewModel.updateAvatarTextRecord(it.first, it.second)
                    }.launchIn(viewLifecycleOwner.lifecycleScope)

                sendTotalResultCount
                    .onEach {
                        when (it.first) {
                            SearchFilterSharedViewModel.SearchResultTab.CONTACT -> {
                                binding.lyTab.getTabAt(0)?.text =
                                    if (it.second > -1) {
                                        getString(R.string.text_name_and_number, mFragmentPairs[0].first, it.second)
                                    } else {
                                        mFragmentPairs[0].first
                                    }
                                searchPageRecord[SearchFilterSharedViewModel.SearchResultTab.CONTACT] = it.second
                            }

                            SearchFilterSharedViewModel.SearchResultTab.CHAT_ROOM -> {
                                binding.lyTab.getTabAt(1)?.text =
                                    if (it.second > -1) {
                                        getString(R.string.text_name_and_number, mFragmentPairs[1].first, it.second)
                                    } else {
                                        mFragmentPairs[1].first
                                    }
                                searchPageRecord[SearchFilterSharedViewModel.SearchResultTab.CHAT_ROOM] = it.second
                            }

                            SearchFilterSharedViewModel.SearchResultTab.SERVICE_NUMBER -> {
                                binding.lyTab.getTabAt(2)?.text =
                                    if (it.second > -1) {
                                        getString(R.string.text_name_and_number, mFragmentPairs[2].first, it.second)
                                    } else {
                                        mFragmentPairs[2].first
                                    }
                                searchPageRecord[SearchFilterSharedViewModel.SearchResultTab.SERVICE_NUMBER] =
                                    it.second
                            }
                        }
                    }.launchIn(viewLifecycleOwner.lifecycleScope)

                sendVerifyCountAndTransfer
                    .onEach {
                        // 定位到有搜索結果的大分組
                        when (viewPager.currentItem) {
                            0 -> {
                                if ((searchPageRecord[SearchFilterSharedViewModel.SearchResultTab.CONTACT] ?: 0) == 0) {
                                    if ((searchPageRecord[SearchFilterSharedViewModel.SearchResultTab.CHAT_ROOM] ?: 0) > 0) {
                                        viewPager.setCurrentItem(1, true)
                                    } else if ((searchPageRecord[SearchFilterSharedViewModel.SearchResultTab.SERVICE_NUMBER] ?: 0) > 0) {
                                        viewPager.setCurrentItem(2, true)
                                    }
                                }
                            }

                            1 -> {
                                if ((searchPageRecord[SearchFilterSharedViewModel.SearchResultTab.CHAT_ROOM] ?: 0) == 0) {
                                    if ((searchPageRecord[SearchFilterSharedViewModel.SearchResultTab.CONTACT] ?: 0) > 0) {
                                        viewPager.setCurrentItem(0, true)
                                    } else if ((searchPageRecord[SearchFilterSharedViewModel.SearchResultTab.SERVICE_NUMBER] ?: 0) > 0) {
                                        viewPager.setCurrentItem(2, true)
                                    }
                                }
                            }

                            2 -> {
                                if ((searchPageRecord[SearchFilterSharedViewModel.SearchResultTab.SERVICE_NUMBER] ?: 0) == 0) {
                                    if ((searchPageRecord[SearchFilterSharedViewModel.SearchResultTab.CONTACT] ?: 0) > 0) {
                                        viewPager.setCurrentItem(0, true)
                                    } else if ((searchPageRecord[SearchFilterSharedViewModel.SearchResultTab.CHAT_ROOM] ?: 0) > 0) {
                                        viewPager.setCurrentItem(1, true)
                                    }
                                }
                            }
                        }
                    }.launchIn(viewLifecycleOwner.lifecycleScope)
            }
            lifecycleOwner = this@SearchFilterFragment.viewLifecycleOwner
        }.root

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        searchFilterViewModel.initSearchRecord(sharedViewModel.ownerId.value)
    }

    private fun initGlobalSearch() {
        if (sharedViewModel.sharedPageType.value == SearchFilterSharedViewModel.SearchFilterPageType.GLOBAL_SEARCH) {
            binding.groupInputArea.visibility = View.GONE // 全局搜尋時，不顯示共用的輸入框
        }
    }

    private fun initConsultationAI() {
        if (sharedViewModel.sharedPageType.value == SearchFilterSharedViewModel.SearchFilterPageType.SERVICE_NUMBER_CONSULTATION_AI) {
            binding.rvSearchAvatarRecord.visibility = View.GONE // 諮詢服務號頁面不顯示頭圖紀錄
        }
    }

    private fun init() {
        sharedViewModel.ownerId.value = selfId

        initGlobalSearch()
        initConsultationAI()
        initViewPager2()

        employeeSelectedAdapter =
            EmployeesSelectedListAdapter(
                selfId,
                searchFilterViewModel,
                isGreenTheme
            )
        searchEmployeesAvatarRecordListAdapter = SearchEmployeesAvatarRecordListAdapter(searchFilterViewModel)
        searchEmployeesTextRecordListAdapter = SearchEmployeesTextRecordListAdapter(searchFilterViewModel)
        binding.apply {
            rvSearchAvatarRecord.adapter = searchEmployeesAvatarRecordListAdapter
            rvSelectList.adapter = employeeSelectedAdapter
            rvSearchAvatarRecord.layoutManager = GridLayoutManager(requireActivity(), searchAvatarRecordItemCount)
            rvSearchTextRecord.layoutManager = GridLayoutManager(requireActivity(), searchTextRecordItemCount)
            etSearch.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(
                        p0: CharSequence?,
                        p1: Int,
                        p2: Int,
                        p3: Int
                    ) {
                    }

                    override fun onTextChanged(
                        p0: CharSequence?,
                        p1: Int,
                        p2: Int,
                        p3: Int
                    ) {
                    }

                    override fun afterTextChanged(s: Editable?) {
                        clearInput.apply {
                            if (visibility == View.GONE) {
                                visibility = View.VISIBLE
                            }
                            if (s.toString().isEmpty()) {
                                visibility = View.GONE
                            }
                        }
                    }
                }
            )
            etSearch.onSubmit {
                lifecycleScope.launch {
                    sharedViewModel.sendInputText.emit(etSearch.text.toString())
                }
                hideKeyboard()
            }
        }
    }

    private fun initViewPager2() {
        val titles: MutableList<String> = mutableListOf()
        mViewPager2Adapter = BaseViewPager2Adapter(requireActivity())
        binding.viewPager.adapter = mViewPager2Adapter
        mFragmentPairs.clear()

        initTab(titles)

        if (mFragmentPairs.isNotEmpty()) {
            binding.viewPager.offscreenPageLimit = mFragmentPairs.size
            mViewPager2Adapter.setData(mFragmentPairs)
            if (mFragmentPairs.size > 1) {
                TabLayoutMediator(binding.lyTab, binding.viewPager) { tab, position ->
                    tab.text = mFragmentPairs[position].first
                }.attach()
                binding.lyTab.setTabTextColors(
                    ResourcesCompat.getColor(
                        resources,
                        if (isGreenTheme && isServiceRoomTheme) {
                            R.color.color_6BC2BA
                        } else if (isGreenTheme) {
                            R.color.color_015F57
                        } else {
                            R.color.colorPrimary
                        },
                        null
                    ),
                    ResourcesCompat.getColor(
                        resources,
                        if (isGreenTheme && isServiceRoomTheme) {
                            R.color.color_6BC2BA
                        } else if (isGreenTheme) {
                            R.color.color_015F57
                        } else {
                            R.color.colorPrimary
                        },
                        null
                    )
                )
                binding.lyTab.setSelectedTabIndicatorColor(
                    ResourcesCompat.getColor(
                        resources,
                        if (isGreenTheme && isServiceRoomTheme) {
                            R.color.color_6BC2BA
                        } else if (isGreenTheme) {
                            R.color.color_015F57
                        } else {
                            R.color.colorPrimary
                        },
                        null
                    )
                )
            } else {
                binding.lyTab.visibility = View.GONE
            }

            if (sharedViewModel.sharedPageType.value == SearchFilterSharedViewModel.SearchFilterPageType.GLOBAL_SEARCH) {
                // 只有全局搜尋需要對應Tab
                // 切換BottomTab對應到搜尋Tab
                when (sharedViewModel.globalSearchTabEntry.value) {
                    BottomTabEnum.MAIN -> binding.viewPager.setCurrentItem(1, false)
                    BottomTabEnum.SERVICE -> binding.viewPager.setCurrentItem(2, false)
                    BottomTabEnum.CONTACT -> binding.viewPager.setCurrentItem(0, false)
                    else -> {}
                }
            }
        }
    }

    private fun initTab(titles: MutableList<String>) {
        when (sharedViewModel.sharedPageType.value) {
            SearchFilterSharedViewModel.SearchFilterPageType.CREATE_GROUP, // 創建社團
            SearchFilterSharedViewModel.SearchFilterPageType.CREATE_DISCUSS, // 創建多人聊天室
            SearchFilterSharedViewModel.SearchFilterPageType.PROVISIONAL_MEMBER -> // 邀請臨時成員
                addContactPersonTab(titles)

            SearchFilterSharedViewModel.SearchFilterPageType.INVITATION -> { // 邀請
                addContactPersonTab(titles)
                addChatRoomTab(titles)
            }

            SearchFilterSharedViewModel.SearchFilterPageType.MESSAGE_TRANSFER, // 轉發
            SearchFilterSharedViewModel.SearchFilterPageType.SHARE_IN, // 內部分享
            SearchFilterSharedViewModel.SearchFilterPageType.GLOBAL_SEARCH -> { // 全局搜尋
                // 搜尋結果的大分組順序調整, 順序固定為: 聯絡人、聊天室、服務號
                addContactPersonSearchTab(titles)
                addChatRoomSearchTab(titles)
                addServiceNumberSearchTab(titles)
            }

            SearchFilterSharedViewModel.SearchFilterPageType.SERVICE_NUMBER_CONSULTATION_AI -> { // 服務號諮詢
                addServiceNumberConsultationTab(titles)
            }

            else -> addContactPersonTab(titles)
        }

        titles.forEachIndexed { index, title ->
            mFragmentPairs.add(Pair(title, mFragments[index]))
        }
    }

    //    private fun addCommunitiesTab(titles: MutableList<String>) { //功能已合併至其他頁面
//        titles.add(getString(R.string.text_group))
//        mFragments.add(CommunityFragment.newInstance())
//    }
    private fun addServiceNumberConsultationTab(titles: MutableList<String>) { // 服務號諮詢頁
        titles.add(getString(R.string.text_service_number_consultation_ai))
        mFragments.add(ServiceNumberConsultationFragment.newInstance())
    }

    private fun addChatRoomTab(titles: MutableList<String>) { // 邀請頁
        titles.add(getString(R.string.text_chat_room))
        mFragments.add(ChatRoomFragment.newInstance())
    }

    private fun addContactPersonTab(titles: MutableList<String>) { // 建立多人/社團/邀請/臨時成員頁
        titles.add(getString(R.string.text_contact_person))
        mFragments.add(ContactPersonFragment.newInstance())
    }

    private fun addContactPersonSearchTab(titles: MutableList<String>) { // 全局搜尋/轉發
        titles.add(getString(R.string.text_contact_person))
        mFragments.add(ContactPersonClientSearchFragment.newInstance())
    }

    private fun addServiceNumberSearchTab(titles: MutableList<String>) { // 全局搜尋/轉發
        titles.add(getString(R.string.text_service_number))
        mFragments.add(ServiceNumberSearchFragment.newInstance())
    }

    private fun addChatRoomSearchTab(titles: MutableList<String>) { // 全局搜尋/轉發
        titles.add(getString(R.string.text_chat_room))
        mFragments.add(ChatRoomSearchFragment.newInstance())
    }

    private fun clearInput() {
        binding.etSearch.text?.clear()
        binding.clearInput.apply {
            if (isVisible) {
                visibility = View.GONE
            }
        }
        lifecycleScope.launch {
            sharedViewModel.sendInputText.emit("")
        }
        binding.etSearch.requestFocus()
        KeyboardHelper.open(this.view)
    }

    private fun hideKeyboard() {
        KeyboardHelper.hide(this@SearchFilterFragment.requireActivity().currentFocus)
        binding.etSearch.clearFocus()
    }
}
