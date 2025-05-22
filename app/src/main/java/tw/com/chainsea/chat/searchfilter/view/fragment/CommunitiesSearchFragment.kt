package tw.com.chainsea.chat.searchfilter.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import tw.com.chainsea.ce.sdk.bean.CrowdEntity
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.config.BundleKey
import tw.com.chainsea.chat.databinding.FragmentCommunitiesSearchBinding
import tw.com.chainsea.chat.searchfilter.view.activity.SearchMessageListActivity
import tw.com.chainsea.chat.searchfilter.viewmodel.CommunitiesSearchViewModel
import tw.com.chainsea.chat.searchfilter.viewmodel.SearchFilterSharedViewModel
import tw.com.chainsea.chat.service.ActivityTransitionsControl
import tw.com.chainsea.chat.ui.activity.ChatNormalActivity
import tw.com.chainsea.chat.ui.adapter.GlobalSearchFilterListAdapter
import tw.com.chainsea.chat.util.ThemeHelper

// 社團搜尋資料併入聊天室頁簽，暫時棄用ver3.2.1
class CommunitiesSearchFragment : Fragment() {
    companion object {
        fun newInstance() = CommunitiesSearchFragment()
    }

    private val communitiesSearchViewModel by viewModels<CommunitiesSearchViewModel>()
    private val sharedViewModel by activityViewModels<SearchFilterSharedViewModel>()
    private lateinit var binding: FragmentCommunitiesSearchBinding
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
        .inflate<FragmentCommunitiesSearchBinding>(
            inflater,
            R.layout.fragment_communities_search,
            container,
            false
        ).apply {
            binding = this
            init()
            communitiesSearchViewModel.apply {
                sendFilterQueryList
                    .onEach {
                        globalSearchFilterListAdapter.setData(it.first, it.second)
                        if (it.first.isNotEmpty()) {
                            rvCommunityList.adapter = globalSearchFilterListAdapter
                        }
                        scopeNoData.getRoot().visibility = if (it.second.isNotEmpty()) View.GONE else View.VISIBLE
                        tvHint.visibility = if (it.second.isNotEmpty()) View.VISIBLE else View.GONE
                        sharedViewModel.sendCollectDataDone.emit(1)
                    }.launchIn(viewLifecycleOwner.lifecycleScope)

                navigateToChatRoom
                    .onEach {
                        if (it.first.isNotEmpty()) {
                            sharedViewModel.sendGlobalSearchNewRecord.emit(Pair(it.first, it.second))
                        }
                        when (it.second) {
                            is CrowdEntity -> {
                                ActivityTransitionsControl.navigateToChat(
                                    requireActivity(),
                                    (it.second as CrowdEntity).id
                                ) { intent: Intent, _: String? ->
                                    startActivity(intent)
                                }
                            }

                            is ChatRoomEntity -> {
                                ActivityTransitionsControl.navigateToChat(
                                    requireActivity(),
                                    it.second as ChatRoomEntity,
                                    CommunitiesSearchFragment::class.java.simpleName
                                ) { i, _ ->
                                    startActivity(i)
                                }
                            }
                        }
                    }.launchIn(viewLifecycleOwner.lifecycleScope)

                navigateToMessageList
                    .onEach {
                        if (it.first.isNotEmpty()) {
                            sharedViewModel.sendGlobalSearchNewRecord.emit(Pair(it.first, it.third))
                        }
                        if (it.second.size == 1) {
                            startActivity(
                                Intent(requireActivity(), ChatNormalActivity::class.java)
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

                sendInitDataDone
                    .onEach {
                        communitiesSearchViewModel.filter(requireActivity(), keyWord)
                    }.launchIn(viewLifecycleOwner.lifecycleScope)
            }

            sharedViewModel.apply {
                sendInputText
                    .onEach {
                        if (communitiesSearchViewModel.localEntities.isEmpty()) {
                            initData()
                            keyWord = it
                        } else {
                            communitiesSearchViewModel.filter(requireActivity(), it)
                        }
                    }.launchIn(viewLifecycleOwner.lifecycleScope)
            }

            lifecycleOwner = this@CommunitiesSearchFragment.viewLifecycleOwner
        }.root

    private fun initData() {
        communitiesSearchViewModel.getAllCommunityRoom(sharedViewModel.localAllMessagesEntities)
    }

    fun init() {
        globalSearchFilterListAdapter = GlobalSearchFilterListAdapter(sharedViewModel.ownerId.value, communitiesSearchViewModel, sharedViewModel.sharedPageType.value, this, ThemeHelper.isGreenTheme())
    }
}
