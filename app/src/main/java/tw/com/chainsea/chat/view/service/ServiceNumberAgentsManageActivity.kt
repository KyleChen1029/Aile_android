package tw.com.chainsea.chat.view.service

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.event.EventMsg
import tw.com.chainsea.ce.sdk.event.MsgConstant
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.config.BundleKey
import tw.com.chainsea.chat.databinding.ActivityServiceNumberAgentsManageBinding
import tw.com.chainsea.chat.lib.ToastUtils
import tw.com.chainsea.chat.network.contact.ViewModelFactory
import tw.com.chainsea.chat.view.BaseActivity
import tw.com.chainsea.chat.view.service.adapter.ServiceNumberAgentAdapter
import tw.com.chainsea.chat.view.service.adapter.ServiceNumberAgentAdapter.OnManagementClick
import tw.com.chainsea.custom.view.alert.AlertView
import tw.com.chainsea.custom.view.alert.PrivilegeManagerDialog
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemTouchHelperCallback
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemTouchHelperExtension

class ServiceNumberAgentsManageActivity : BaseActivity(), OnManagementClick {

    lateinit var binding: ActivityServiceNumberAgentsManageBinding
    private val serviceNumberId by lazy { intent.getStringExtra(BundleKey.SERVICE_NUMBER_ID.key())!! }
    private val broadcastRoomId by lazy { intent.getStringExtra(BundleKey.BROADCAST_ROOM_ID.key())!! }
    private val agentAdapter: ServiceNumberAgentAdapter by lazy {
        val userId = TokenPref.getInstance(this).userId
        val callback = ItemTouchHelperCallback(ItemTouchHelper.START)
        val extension = ItemTouchHelperExtension(callback)
        extension.attachToRecyclerView(binding.rvAgentsList)
        ServiceNumberAgentAdapter(
            userId,
            serviceNumberId
        ).setItemTouchHelperExtension(extension)
            .setOnManagementClickListener(this@ServiceNumberAgentsManageActivity)
    }
    lateinit var viewModel: ServiceNumberAgentsManageViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServiceNumberAgentsManageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setStatusBarColor()
        initViewModel()
        initRecyclerView()
        initListener()
        observeData()
        getData()
    }

    private fun initListener() {
        binding.leftAction.setOnClickListener { finish() }
    }

    override fun onPause() {
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out)
        super.onPause()
    }

    private fun getData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.getIsTenantManager()
                viewModel.getServiceEntity(serviceNumberId)
            }
        }
    }

    private fun initViewModel() {
        val serviceNumberAgentsManageRepository = ViewModelFactory(application)
        viewModel = ViewModelProvider(
            this, serviceNumberAgentsManageRepository
        )[ServiceNumberAgentsManageViewModel::class.java]
    }

    private fun observeData() {
        viewModel.isTenantManager.observe(this) {
            agentAdapter.setIsTenantOwner(it).refreshData()
        }

        viewModel.agentsList.observe(this) {
            agentAdapter.setData(it).refreshData()
        }

        viewModel.modifySuccess.observe(this) {
            viewModel.getServiceEntityFromRemote(serviceNumberId)
        }

        viewModel.message.observe(this) {
            ToastUtils.showToast(this, getString(it))
        }
    }

    private fun initRecyclerView() {
        binding.rvAgentsList.apply {
            layoutManager = LinearLayoutManager(this@ServiceNumberAgentsManageActivity)
            adapter = agentAdapter
        }
    }

    private fun setStatusBarColor() {
        window.statusBarColor = -0x943d46
    }

    override fun onTranOwnerClick(profile: UserProfileEntity) {
        PrivilegeManagerDialog(this).getTransferOwnerDialog(getString(R.string.text_sure_to_transfer_ownership, profile.nickName)) {
            viewModel.modifyOwner(serviceNumberId, profile.id)
        }.show()
    }

    override fun onDesignateClick(profile: UserProfileEntity) {
        PrivilegeManagerDialog(this).getDesignateManagerDialog(getString(R.string.text_sure_to_designate_management, profile.nickName)) {
            viewModel.addManager(serviceNumberId, profile.id)
        }.show()
    }

    override fun onRemoveManagementClick(profile: UserProfileEntity) {
        PrivilegeManagerDialog(this).getCancelManagerDialog(getString(R.string.text_sure_to_cancel_management, profile.nickName)) {
            viewModel.removeManager(serviceNumberId, profile.id)
        }.show()
    }

    override fun onDeleteClick(profile: UserProfileEntity) {
        PrivilegeManagerDialog(this).getDeleteMemberDialog(String.format(getString(R.string.service_number_angent_delete), profile.nickName)) {
            viewModel.deleteAgent(serviceNumberId, profile.id)
        }.show()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: EventMsg<Any>) {
        when (event.code) {
            MsgConstant.DELETE_SERVICE_NUMBER_OTHER_MEMBER -> {
                val id = event.data as Set<String>
                agentAdapter.refreshDataWhenRemove(id)
            }

            MsgConstant.SERVICE_NUMBER_UPDATE -> {
                viewModel.getIsTenantManager()
                viewModel.getServiceEntityFromRemote(serviceNumberId)
            }

            MsgConstant.NOTICE_PROVISIONAL_MEMBER_ADDED -> {
                viewModel.getServiceEntityFromRemote(serviceNumberId)
            }
        }
    }

    private fun showConfirmDialog(message: String, callback: () -> Unit) {
        AlertView.Builder().setContext(this).setStyle(AlertView.Style.Alert)
            .setMessage(message)
            .setOthers(
                arrayOf(
                    getString(R.string.alert_cancel), getString(R.string.alert_confirm)
                )
            )
            .setOnItemClickListener { o: Any?, position: Int ->
                if (position == 1) {
                    callback.invoke()
                }
            }
            .build()
            .setCancelable(true)
            .setOnDismissListener(null)
            .show()
    }
}