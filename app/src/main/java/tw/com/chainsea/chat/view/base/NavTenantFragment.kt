package tw.com.chainsea.chat.view.base

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import tw.com.chainsea.android.common.ui.UiHelper
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.http.cp.respone.RelationTenant
import tw.com.chainsea.ce.sdk.reference.ServiceNumberReference
import tw.com.chainsea.ce.sdk.service.ChatServiceNumberService
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack
import tw.com.chainsea.ce.sdk.service.type.RefreshSource
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.config.BundleKey
import tw.com.chainsea.chat.config.SystemConfig
import tw.com.chainsea.chat.databinding.FragmentNavTenantBinding
import tw.com.chainsea.chat.databinding.PopupJoinTenantBinding
import tw.com.chainsea.chat.network.tenant.TenantViewModel
import tw.com.chainsea.chat.service.ActivityTransitionsControl
import tw.com.chainsea.chat.util.IntentUtil
import tw.com.chainsea.chat.util.SystemKit
import tw.com.chainsea.chat.util.ThemeHelper
import tw.com.chainsea.chat.util.UnreadUtil
import tw.com.chainsea.chat.view.account.fragments.SystemSettingActivity
import tw.com.chainsea.chat.view.account.homepage.ServicesNumberManagerHomepageActivity
import tw.com.chainsea.chat.view.base.viewmodel.HomeViewModel
import tw.com.chainsea.chat.view.group.GroupCreateActivity
import tw.com.chainsea.chat.view.qrcode.QrCodeActivity
import tw.com.chainsea.chat.view.qrcode.QrCodeType
import tw.com.chainsea.chat.view.setting.RepairsActivity

class NavTenantFragment : Fragment() {
    private lateinit var binding: FragmentNavTenantBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var tenantViewModel: TenantViewModel
    private var serviceNumberId: String = ""
    private val otherTenantAdapter = OtherTenantAdapter(::joinTenant, ::changeTenant)
    private var createTenant: Boolean = false
    private var joinTenant: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNavTenantBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        ThemeHelper.setTheme(requireActivity())
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        initObserver()
        initClickListener()
    }

    private fun initViewModel() {
        tenantViewModel = ViewModelProvider(requireActivity())[TenantViewModel::class.java]
        viewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]
    }

    private fun initObserver() {
        viewModel.chatRoomUnreadNumber.observe(viewLifecycleOwner) { calculateUnreadNumber() }
        viewModel.serviceRoomUnreadNumber.observe(viewLifecycleOwner) { calculateUnreadNumber() }
        tenantViewModel.onTenantAvatarGot.observe(viewLifecycleOwner) { bitmap ->
            bitmap?.let {
                binding.img.setImageBitmap(bitmap)
            } ?: run {
                binding.img.setImageResource(R.drawable.invalid_name)
            }
        }
        tenantViewModel.tenantList.observe(viewLifecycleOwner) { data: List<RelationTenant>? ->

            val relationTenantList = mutableListOf<RelationTenant>()
            relationTenantList.addAll(data ?: TokenPref.getInstance(context).cpRelationTenantList)

            val currentTenant = TokenPref.getInstance(requireContext()).cpCurrentTenant
            setCurrentTenant(currentTenant)

            val filterCurrentTenant = mutableListOf<RelationTenant>()

            // 過濾目前所在的團隊
            filterCurrentTenant.addAll(
                relationTenantList
                    .filterNot { it.tenantId == currentTenant.tenantId }
                    .toMutableList()
            )

            // 是否顯示 “其他團隊 title”
            if (filterCurrentTenant.size == 0) {
                binding.tvOtherTitle.visibility = View.GONE
            } else {
                binding.tvOtherTitle.visibility = View.VISIBLE
            }

            setManagerIcon(currentTenant)
            createTenant = TokenPref.getInstance(requireActivity()).createTenantPermission > 0
            joinTenant = TokenPref.getInstance(requireActivity()).joinTenantPermission > 0
            // 加入 "加入團隊" 的item
            when {
                createTenant && joinTenant -> {
                    filterCurrentTenant.add(RelationTenant(getString(R.string.join_group)))
                }

                createTenant && !joinTenant -> {
                    filterCurrentTenant.add(RelationTenant(getString(R.string.create_group)))
                }

                !createTenant && joinTenant -> {
                    filterCurrentTenant.add(RelationTenant(getString(R.string.join_group)))
                }
            }
            updateTenantRelationList(filterCurrentTenant)
            updateUndeadCount(relationTenantList, currentTenant)
        }
    }

    private fun changeTenant(tenant: RelationTenant) {
        viewModel.preloadJob.cancel()
        SystemKit.changeTenant(requireActivity(), tenant, false)
    }

    private fun joinTenant(targetView: View) {
        val binding = PopupJoinTenantBinding.inflate(layoutInflater)
        val joinTenantPopupWindows = PopupWindow()
        val height = UiHelper.dp2px(context, 128f).toInt()
        when {
            createTenant && joinTenant -> {
                // popupWindows setting
                joinTenantPopupWindows.apply {
                    this.contentView = binding.root
                    this.height = height
                    this.width = UiHelper.dp2px(context, 146f).toInt()
                    this.isOutsideTouchable = true
                    this.isFocusable = false
                }
                binding.tvJoinTeamMenuJoin.setOnClickListener {
                    joinTenantPopupWindows.dismiss()
                    ActivityTransitionsControl.navigateScannerJoinGroup(context)
                }

                binding.tvJoinTeamMenuCreate.setOnClickListener {
                    joinTenantPopupWindows.dismiss()
                    IntentUtil.start(requireContext(), Intent(context, GroupCreateActivity::class.java))
                }

                joinTenantPopupWindows.showAsDropDown(
                    targetView,
                    height / 2,
                    -height - targetView.height,
                    Gravity.END
                )
            }

            createTenant && !joinTenant -> {
                IntentUtil.start(requireContext(), Intent(context, GroupCreateActivity::class.java))
            }

            !createTenant && joinTenant -> {
                ActivityTransitionsControl.navigateScannerJoinGroup(context)
            }
        }
    }

    private fun setManagerIcon(currentTenant: RelationTenant) {
        if (currentTenant.manageServiceNumberInfo != null) {
            currentTenant.manageServiceNumberInfo?.let { serviceNumberInfo ->
                serviceNumberId = serviceNumberInfo.id
            }
            val serviceNumberEntity = ServiceNumberReference.findServiceNumberById(serviceNumberId)
            if (serviceNumberEntity != null) {
                if (serviceNumberEntity.isOwner || serviceNumberEntity.isManager) {
                    binding.ivManager.visibility = View.VISIBLE
                }
            } else {
                ChatServiceNumberService.findServiceNumber(
                    requireContext(),
                    serviceNumberId,
                    RefreshSource.ALL,
                    object : ServiceCallBack<ServiceNumberEntity, RefreshSource> {
                        override fun complete(
                            entity: ServiceNumberEntity,
                            source: RefreshSource
                        ) {
                            if (entity.isManager || entity.isOwner) {
                                if (isAdded) {
                                    requireActivity().runOnUiThread {
                                        binding.ivManager.visibility = View.VISIBLE
                                    }
                                }
                            }
                        }

                        override fun error(message: String) {}
                    }
                )
            }
        }
    }

    private fun updateUndeadCount(
        data: List<RelationTenant>,
        currentTenant: RelationTenant
    ) {
        var unreadCount = 0
        data.forEach {
            if (currentTenant.tenantCode != it.tenantCode) {
                unreadCount += it.unReadNum // 影響左上角選單
            }
        }
        viewModel.unreadCount.value = unreadCount
    }

    private fun updateTenantRelationList(data: List<RelationTenant>) {
        otherTenantAdapter.setData(data)
        binding.layoutOthers.apply {
            adapter = otherTenantAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun calculateUnreadNumber() {
        val count =
            viewModel.serviceRoomUnreadNumber.value!! + viewModel.chatRoomUnreadNumber.value!!
        if (count == 0) {
            binding.txtUnread.visibility = View.GONE
        } else {
            binding.txtUnread.text = UnreadUtil.getUnreadText(count)
            binding.txtUnread.visibility = View.VISIBLE
        }
    }

    private fun setCurrentTenant(currentTenant: RelationTenant?) {
        if (!SystemConfig.isCpMode) return
        binding.txtName.text = currentTenant?.abbreviationTenantName ?: ""
        tenantViewModel.getTenantAvatar(currentTenant?.avatarId)
    }

    private fun closeDrawer() {
        lifecycleScope.launch { viewModel.sendCloseDrawer.emit(Unit) }
    }

    private fun initClickListener() {
        binding.tvSetting.setOnClickListener {
            closeDrawer()
            IntentUtil.start(requireContext(), Intent(context, SystemSettingActivity::class.java))
        }
        binding.tvReport.setOnClickListener {
            closeDrawer()
            IntentUtil.start(requireContext(), Intent(context, RepairsActivity::class.java))
        }

        binding.ivManager.setOnClickListener {
            closeDrawer()
            val intent = Intent(context, ServicesNumberManagerHomepageActivity::class.java)
            intent.putExtra(BundleKey.SERVICE_NUMBER_ID.key(), serviceNumberId)
            IntentUtil.start(requireContext(), intent)
        }

        binding.ivInvite.setOnClickListener {
            closeDrawer()
            val intent = Intent()
            intent.setClass(requireActivity(), QrCodeActivity::class.java)
            intent.putExtra(BundleKey.TYPE.key(), QrCodeType.person)
            IntentUtil.start(requireContext(), intent)
        }
    }
}
