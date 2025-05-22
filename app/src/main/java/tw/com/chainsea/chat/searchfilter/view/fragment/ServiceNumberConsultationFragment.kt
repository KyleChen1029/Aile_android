package tw.com.chainsea.chat.searchfilter.view.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.config.BundleKey
import tw.com.chainsea.chat.databinding.FragmentServiceNumberConsultationBinding
import tw.com.chainsea.chat.lib.ToastUtils
import tw.com.chainsea.chat.network.contact.ViewModelFactory
import tw.com.chainsea.chat.ui.adapter.SearchFilterListAdapter
import tw.com.chainsea.chat.searchfilter.viewmodel.SearchFilterSharedViewModel
import tw.com.chainsea.chat.searchfilter.viewmodel.ServiceNumberConsultationViewModel
import tw.com.chainsea.chat.ui.fragment.ChatFragment
import tw.com.chainsea.chat.util.ThemeHelper
import tw.com.chainsea.custom.view.progress.IosProgressBar

class ServiceNumberConsultationFragment : Fragment() {

    private lateinit var serviceNumberConsultationViewModel: ServiceNumberConsultationViewModel
    //the data connection between SearchFilterFragment and viewpager fragment(ContactPersonFragment)
    private val sharedViewModel by activityViewModels<SearchFilterSharedViewModel>()
    private lateinit var searchFilterListAdapter: SearchFilterListAdapter
    private lateinit var binding: FragmentServiceNumberConsultationBinding
    private lateinit var progressBar: IosProgressBar
    companion object {
        fun newInstance() = ServiceNumberConsultationFragment()
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
    ) = DataBindingUtil.inflate<FragmentServiceNumberConsultationBinding>(
        inflater,
        R.layout.fragment_service_number_consultation,
        container,
        false
    ).apply {
        binding = this
        init()
        serviceNumberConsultationViewModel.apply {
            sendQueryList.onEach {
                rvServiceNumberConsultList.adapter = searchFilterListAdapter
                searchFilterListAdapter.setData(it.first, it.second.toMutableList())
                scopeNoData.getRoot().visibility = if(it.second.isNotEmpty()) View.GONE else View.VISIBLE
                hideLoadingView()
            }.launchIn(viewLifecycleOwner.lifecycleScope)

            selectedServiceNumberConsultItem.onEach {
                //選中諮詢服務號
                if(it.first.isNotEmpty())
                    sharedViewModel.sendGlobalSearchNewRecord.emit(Pair(it.first, Any()))
                sharedViewModel.sendResultForServiceNumberConsultation.emit(it.second)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

            selectedAiConsultationItem.onEach {
                //選中AI諮詢服務號
                // 紀錄搜尋文字
                if(it.first.isNotEmpty())
                    sharedViewModel.sendGlobalSearchNewRecord.emit(Pair(it.first, Any()))

                // 已經有 consult id 不打 API
                if (it.second.isNotEmpty()) {
                    serviceNumberConsultationViewModel.startedAiConsultationSuccess.emit(it.second)
                } else {
                    serviceNumberConsultationViewModel.doStartAiConsultation(sharedViewModel.activeRoomId.value)
                }
            }.launchIn(viewLifecycleOwner.lifecycleScope)

            startedAiConsultationSuccess.onEach {
                //打開AI諮詢聊天室 it = consultId
                val bundle = bundleOf(
                    BundleKey.CONSULT_AI_ID.key() to it,
                    BundleKey.ROOM_ID.key() to sharedViewModel.activeRoomId.value,
                    BundleKey.SERVICE_NUMBER_ID.key() to sharedViewModel.activeServiceNUmberId.value
                )
                val intent = Intent()
                intent.putExtras(bundle)
                requireActivity().setResult(ChatFragment.REQUEST_CONSULT_AI_CODE, intent)
                requireActivity().finish()
            }.launchIn(viewLifecycleOwner.lifecycleScope)

            startedAiConsultationFailure.onEach {
                ToastUtils.showToast(requireContext(), getString(it))
            }.launchIn(viewLifecycleOwner.lifecycleScope)

            dismissLoading.onEach {
                hideLoadingView()
            }.launchIn(viewLifecycleOwner.lifecycleScope)
        }

        sharedViewModel.apply {
            sendInputText.onEach {
                serviceNumberConsultationViewModel.filter(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)
        }
        lifecycleOwner = this@ServiceNumberConsultationFragment.viewLifecycleOwner
    }.root

    override fun onResume() {
        super.onResume()
        showLoadingView()
        serviceNumberConsultationViewModel.getAllServiceNumberConsultationList(
            sharedViewModel.activeServiceNUmberId.value,
            sharedViewModel.activeConsultAIId.value
            //,sharedViewModel.activeServiceNumberConsultArray
        )
    }

    fun init() {
        initViewModel()
        searchFilterListAdapter = SearchFilterListAdapter(sharedViewModel.ownerId.value, serviceNumberConsultationViewModel, ThemeHelper.isGreenTheme())
        binding.rvServiceNumberConsultList.adapter = searchFilterListAdapter
    }

    private fun initViewModel() {
        val factory = ViewModelFactory(requireActivity().application)
        serviceNumberConsultationViewModel = ViewModelProvider(this, factory)[ServiceNumberConsultationViewModel::class.java]
    }

    private fun showLoadingView() {
        lifecycleScope.launch(Dispatchers.Main) {
            progressBar = IosProgressBar.show(
                requireContext(), "", true, false
            ) { }
        }
    }

    private fun hideLoadingView() {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                if (progressBar.isShowing)
                    progressBar.dismiss()
            } catch (ignored: Exception) { }
        }
    }
}