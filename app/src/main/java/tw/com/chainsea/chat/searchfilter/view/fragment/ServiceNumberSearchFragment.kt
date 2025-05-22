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
import tw.com.chainsea.ce.sdk.bean.ServiceNum
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.config.BundleKey
import tw.com.chainsea.chat.databinding.FragmentServiceNumberSearchBinding
import tw.com.chainsea.chat.network.contact.ViewModelFactory
import tw.com.chainsea.chat.service.ActivityTransitionsControl
import tw.com.chainsea.chat.ui.activity.ChatActivity
import tw.com.chainsea.chat.searchfilter.view.activity.SearchMessageListActivity
import tw.com.chainsea.chat.ui.adapter.GlobalSearchFilterListAdapter
import tw.com.chainsea.chat.searchfilter.viewmodel.SearchFilterSharedViewModel
import tw.com.chainsea.chat.searchfilter.viewmodel.ServiceNumberSearchViewModel
import tw.com.chainsea.chat.util.IntentUtil
import tw.com.chainsea.chat.util.ThemeHelper

class ServiceNumberSearchFragment : Fragment() {

    companion object {
        fun newInstance() = ServiceNumberSearchFragment()
    }
    private lateinit var serviceNumberSearchViewModel: ServiceNumberSearchViewModel
    private val sharedViewModel by activityViewModels<SearchFilterSharedViewModel>()
    private lateinit var binding: FragmentServiceNumberSearchBinding
    private lateinit var globalSearchFilterListAdapter: GlobalSearchFilterListAdapter
    private var keyWord = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return createDataBindingView(inflater, container)
    }

    private fun createDataBindingView(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ) = DataBindingUtil.inflate<FragmentServiceNumberSearchBinding>(
        inflater,
        R.layout.fragment_service_number_search,
        container,
        false
    ).apply {
        binding = this
        init()
        serviceNumberSearchViewModel.apply {

            when(sharedViewModel.sharedPageType.value) {
                SearchFilterSharedViewModel.SearchFilterPageType.MESSAGE_TRANSFER, //轉發
                SearchFilterSharedViewModel.SearchFilterPageType.SHARE_IN -> { //分享
                    initData()

                    sendFilterQueryList.onEach {
                        globalSearchFilterListAdapter.setData(it.first, it.second)
                        rvServiceNumberList.adapter = globalSearchFilterListAdapter
                        scopeNoData.getRoot().visibility = if(it.second.isNotEmpty()) View.GONE else View.VISIBLE
                        tvHint.visibility = if(it.second.isNotEmpty()) View.VISIBLE else View.GONE
                        sharedViewModel.sendTotalResultCount.emit(Pair(SearchFilterSharedViewModel.SearchResultTab.SERVICE_NUMBER, it.third))
                        sharedViewModel.sendCollectDataDone.emit(1)
                    }.launchIn(viewLifecycleOwner.lifecycleScope)

                    sendServiceNumberSelectedItem.onEach {
                        sharedViewModel.sendEmployeeSelectedItem.emit(Pair(it.first, it.second))
                    }.launchIn(viewLifecycleOwner.lifecycleScope)

                    sendSelectedItem.onEach {
                        globalSearchFilterListAdapter.setData(it.first, it.second)
                    }.launchIn(viewLifecycleOwner.lifecycleScope)
                }
                else -> {
                    sendFilterQueryList.onEach {
                        globalSearchFilterListAdapter.setData(it.first, it.second)
                        if(it.first.isNotEmpty())
                            rvServiceNumberList.adapter = globalSearchFilterListAdapter
                        scopeNoData.getRoot().visibility = if(it.second.isNotEmpty()) View.GONE else View.VISIBLE
                        tvHint.visibility = if(it.second.isNotEmpty()) View.VISIBLE else View.GONE
                        sharedViewModel.sendTotalResultCount.emit(Pair(SearchFilterSharedViewModel.SearchResultTab.SERVICE_NUMBER, if(it.first.isNotEmpty()) it.third else -1))
                        sharedViewModel.sendCollectDataDone.emit(1)
                    }.launchIn(viewLifecycleOwner.lifecycleScope)

                    navigateToChatRoom.onEach {
                        if(it.first.isNotEmpty())
                            sharedViewModel.sendGlobalSearchNewRecord.emit(Pair(it.first, it.second))
                        if(it.second is ServiceNum) {
                            ActivityTransitionsControl.navigateToChat(
                                requireActivity(), (it.second as ServiceNum).roomId
                            ) { intent: Intent, _: String? ->
                                IntentUtil.start(requireContext(), intent)
                            }
                        }else if(it.second is ChatRoomEntity) {
                            ActivityTransitionsControl.navigateToChat(
                                requireActivity(),
                                it.second as ChatRoomEntity,
                                ServiceNumberSearchFragment::class.java.simpleName
                            ) { intent: Intent, _: String? ->
                                startActivity(intent)
                            }
                        }
                    }.launchIn(viewLifecycleOwner.lifecycleScope)

                    navigateToMessageList.onEach {
                        if(it.first.isNotEmpty())
                            sharedViewModel.sendGlobalSearchNewRecord.emit(Pair(it.first, it.third))
                        if(it.second.size == 1) {
                            startActivity(
                                Intent(requireActivity(), ChatActivity::class.java)
                                    .putExtra(BundleKey.EXTRA_SESSION_ID.key(), it.third.id)
                                    .putExtra(BundleKey.EXTRA_MESSAGE.key(), it.second.first())
                                    .putExtra(BundleKey.SEARCH_KEY.key(), it.first)
                            )
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
            when(sharedViewModel.sharedPageType.value) {
                SearchFilterSharedViewModel.SearchFilterPageType.MESSAGE_TRANSFER, //轉發
                SearchFilterSharedViewModel.SearchFilterPageType.SHARE_IN -> { //分享
                    sendInputText.onEach {
                        if(it.isNotEmpty())
                            serviceNumberSearchViewModel.filter(it)
                        else
                            serviceNumberSearchViewModel.showAllData()
                    }.launchIn(viewLifecycleOwner.lifecycleScope)

                    sendSelectedMemberItem.onEach {
                        serviceNumberSearchViewModel.updateSelectedItemList(it) //選中後更新背景樣式
                    }.launchIn(viewLifecycleOwner.lifecycleScope)

                    sendDismissSelectedMemberItem.onEach {
                        serviceNumberSearchViewModel.dismissSelectedItem(it) //取消頭圖選中背景樣式
                    }.launchIn(viewLifecycleOwner.lifecycleScope)
                }
                else -> {
                    sendInputText.onEach {
                        serviceNumberSearchViewModel.globalFilter(it, sharedViewModel.ownerId.value)
                    }.launchIn(viewLifecycleOwner.lifecycleScope)
                }
            }
        }

        lifecycleOwner = this@ServiceNumberSearchFragment.viewLifecycleOwner

    }.root

    private fun initData() {
        serviceNumberSearchViewModel.getAllServiceNumber(sharedViewModel.ownerId.value)
    }

    fun init() {
        initViewModel()
        globalSearchFilterListAdapter = GlobalSearchFilterListAdapter(sharedViewModel.ownerId.value, serviceNumberSearchViewModel, sharedViewModel.sharedPageType.value, this, ThemeHelper.isGreenTheme())
    }
    private fun initViewModel() {
        val factory = ViewModelFactory(requireActivity().application)
        serviceNumberSearchViewModel = ViewModelProvider(this, factory)[ServiceNumberSearchViewModel::class.java]
    }
}