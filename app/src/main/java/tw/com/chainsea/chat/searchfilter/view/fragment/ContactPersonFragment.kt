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
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.FragmentContactPersonTabBinding
import tw.com.chainsea.chat.ui.adapter.SearchFilterListAdapter
import tw.com.chainsea.chat.searchfilter.viewmodel.ContactPersonViewModel
import tw.com.chainsea.chat.searchfilter.viewmodel.SearchFilterSharedViewModel
import tw.com.chainsea.chat.util.ThemeHelper

class ContactPersonFragment : Fragment() {

    private val contactPersonViewModel by viewModels<ContactPersonViewModel>()

    //the data connection between SearchFilterFragment and viewpager fragment(ContactPersonFragment)
    private val sharedViewModel by activityViewModels<SearchFilterSharedViewModel>()
    private lateinit var binding: FragmentContactPersonTabBinding
    private lateinit var searchFilterListAdapter: SearchFilterListAdapter
    companion object {
        fun newInstance() = ContactPersonFragment()
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
    ) = DataBindingUtil.inflate<FragmentContactPersonTabBinding>(
        inflater,
        R.layout.fragment_contact_person_tab,
        container,
        false
    ).apply {
        binding = this
        init()
        contactPersonViewModel.apply {
            employeeList.onEach {
                searchFilterListAdapter.setData(
                    it.first,
                    it.second.toMutableList()
                )
                scopeNoData.getRoot().visibility = if(it.second.isNotEmpty()) View.GONE else View.VISIBLE
            }.launchIn(viewLifecycleOwner.lifecycleScope)

            employeeFilterList.onEach {
                searchFilterListAdapter.setData(
                    it.first,
                    it.second.toMutableList()
                )
                rvPartnerList.adapter = searchFilterListAdapter
                scopeNoData.getRoot().visibility = if(it.second.isNotEmpty()) View.GONE else View.VISIBLE
                if(it.first.isEmpty())
                    rvPartnerList.post {
                        rvPartnerList.scrollToPosition(0)
                    }
                sharedViewModel.sendTotalResultCount.emit(Pair(SearchFilterSharedViewModel.SearchResultTab.CONTACT, if(it.first.isNotEmpty()) it.second.size else -1))
            }.launchIn(viewLifecycleOwner.lifecycleScope)

            sendOwnerSelectedItem.onEach {
                //創建聊天室預設成員頭圖
                sharedViewModel.sendOwnerSelectedItem.emit(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

            sendEmployeeSelectedItem.onEach {
                sharedViewModel.sendEmployeeSelectedItem.emit(Pair(it.first, it.second))
                rvPartnerList.post {
                    rvPartnerList.scrollToPosition(it.third)
                }
            }.launchIn(viewLifecycleOwner.lifecycleScope)

            employeesTextRecordList.onEach {
                //更新點選過的文字紀錄
                sharedViewModel.employeesTextRecordList.emit(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

            when(sharedViewModel.sharedPageType.value) {
                SearchFilterSharedViewModel.SearchFilterPageType.CREATE_GROUP -> addGroupRoomOwner(sharedViewModel.ownerId.value) //創建社團新增預設擁有者
                SearchFilterSharedViewModel.SearchFilterPageType.CREATE_DISCUSS,//創建多人聊天室新增預設成員
                SearchFilterSharedViewModel.SearchFilterPageType.TRANSFER_TO_GROUP -> addDiscussRoomMember(sharedViewModel.userMemberIds) //多人聊天室轉社團
                else -> {}
            }
        }

        sharedViewModel.apply {
            sendInputText.onEach {
               contactPersonViewModel.filter(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

            sendOnRemoveEmployeeItem.onEach {
                contactPersonViewModel.addEmployeeItem(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

            sendSelectedMemberItem.onEach {
                contactPersonViewModel.updateSelectedItemList(it) //選中後更新背景樣式
            }.launchIn(viewLifecycleOwner.lifecycleScope)

            sendDismissSelectedMemberItem.onEach {
                contactPersonViewModel.dismissSelectedItem(it) //取消頭圖選中背景樣式
            }.launchIn(viewLifecycleOwner.lifecycleScope)

            sendAvatarSelectedItem.onEach {//選中頭圖紀錄背景樣式
                contactPersonViewModel.updateSelectedItemList(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)
        }
        lifecycleOwner = this@ContactPersonFragment.viewLifecycleOwner
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contactPersonViewModel.initPartnerData(
            requireActivity(),
            sharedViewModel.ownerId.value,
            sharedViewModel.memberIds,
            sharedViewModel.serviceNumberId.value,
            sharedViewModel.userMemberIds)
    }

    fun init() {
        searchFilterListAdapter = SearchFilterListAdapter(sharedViewModel.ownerId.value, contactPersonViewModel, ThemeHelper.isGreenTheme())
        binding.rvPartnerList.adapter = searchFilterListAdapter
        binding.rvPartnerList.setItemViewCacheSize(20)
    }
}