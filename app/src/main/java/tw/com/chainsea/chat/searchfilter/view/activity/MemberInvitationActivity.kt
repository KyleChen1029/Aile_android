package tw.com.chainsea.chat.searchfilter.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import tw.com.chainsea.android.common.event.KeyboardHelper
import tw.com.chainsea.android.common.json.JsonHelper
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.event.EventBusUtils
import tw.com.chainsea.ce.sdk.event.EventMsg
import tw.com.chainsea.ce.sdk.event.MsgConstant
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.config.BundleKey
import tw.com.chainsea.chat.config.InvitationType
import tw.com.chainsea.chat.databinding.ActivityMemberInvitationBinding
import tw.com.chainsea.chat.lib.ActivityManager
import tw.com.chainsea.chat.lib.NetworkUtils
import tw.com.chainsea.chat.lib.ToastUtils
import tw.com.chainsea.chat.network.contact.ViewModelFactory
import tw.com.chainsea.chat.searchfilter.viewmodel.SearchFilterSharedViewModel
import tw.com.chainsea.chat.searchfilter.viewmodel.SearchFilterSharedViewModel.SearchFilterPageType
import tw.com.chainsea.chat.ui.activity.ChatNormalActivity
import tw.com.chainsea.chat.util.IntentUtil
import tw.com.chainsea.chat.util.ThemeHelper
import tw.com.chainsea.chat.view.BaseActivity
import tw.com.chainsea.custom.view.progress.IosProgressBar

class MemberInvitationActivity : BaseActivity() {
    // the data connection between SearchFilterFragment and top Activity(CreateGroupActivity)
    private lateinit var searchFilterSharedViewModel: SearchFilterSharedViewModel
    private lateinit var binding: ActivityMemberInvitationBinding
    private var progressBar: IosProgressBar? = null
    private val isGreenTheme by lazy { ThemeHelper.isGreenTheme() }
    private val isServiceRoomTheme by lazy { ThemeHelper.isServiceRoomTheme }
    private val roomType by lazy {
        intent.getStringExtra(BundleKey.ROOM_TYPE.key())
    }
    private val roomId by lazy {
        intent.getStringExtra(BundleKey.ROOM_ID.key())
    }
    private val activeServiceNumberId by lazy {
        intent.getStringExtra(BundleKey.SERVICE_NUMBER_ID.key())
    }

    private val activeConsultAIId by lazy {
        intent.getStringExtra(BundleKey.CONSULT_AI_ID.key())
    }

    //    private val activeServiceConsultArray by lazy {
//        intent.getSerializableExtra(BundleKey.BLACK_LIST.key()) as? List<ActiveServiceConsultArray>
//    }
    private var progress = 0

    private fun initTheme() {
        binding.toolbar.clToolBar.setBackgroundColor(
            getColor(
                if (isServiceRoomTheme) {
                    R.color.color_6BC2BA
                } else if (isGreenTheme) {
                    R.color.color_015F57
                } else {
                    R.color.colorPrimary
                }
            )
        )
        window.statusBarColor =
            getColor(
                if (isServiceRoomTheme) {
                    R.color.color_6BC2BA
                } else if (isGreenTheme) {
                    R.color.color_015F57
                } else {
                    R.color.colorPrimary
                }
            )
        binding.btnSubmit.setBackgroundColor(
            getColor(
                if (isServiceRoomTheme) {
                    R.color.color_6BC2BA
                } else if (isGreenTheme) {
                    R.color.color_015F57
                } else {
                    R.color.colorPrimary
                }
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityMemberInvitationBinding>(this, R.layout.activity_member_invitation).apply {
            binding = this
            initViewModel()
            initTheme()
            viewModel =
                this@MemberInvitationActivity.searchFilterSharedViewModel.apply {
                    roomType?.let { it ->
                        when (it) {
                            InvitationType.ProvisionalMember.name -> {
                                sharedPageType.value = SearchFilterPageType.PROVISIONAL_MEMBER
                                toolbar.title.text = getString(R.string.text_invite_provisional_member)
                                intent?.let { it2 ->
                                    memberIds.clear()
                                    serviceNumberId.value = it2.getStringExtra(BundleKey.SERVICE_NUMBER_ID.key()).toString()
                                    val provisionalMembers = it2.getStringArrayListExtra(BundleKey.PROVISIONAL_MEMBER_IDS.key())
                                    provisionalMembers?.add(it2.getStringExtra(BundleKey.SUBSCRIBE_AGENT_ID.key())) // 現在正在服務的人員 id
                                    provisionalMembers?.let {
                                        memberIds.addAll(it)
                                    }
                                }
                            }

                            InvitationType.MessageToTransfer.name -> {
                                sharedPageType.value = SearchFilterPageType.MESSAGE_TRANSFER
                                toolbar.title.text = getString(R.string.text_message_to_transfer)
                                searchFilterSharedViewModel.transferMessageIds = intent.getStringArrayExtra(BundleKey.TRANSEND_MSG_IDS.key()) as Array<String>
                            }

                            InvitationType.ShareIn.name -> {
                                sharedPageType.value = SearchFilterPageType.SHARE_IN
                                toolbar.title.text = getString(R.string.text_message_to_share)
                            }

                            InvitationType.ShareScreenShot.name -> {
                                sharedPageType.value = SearchFilterPageType.ShareScreenShot
                                toolbar.title.text = getString(R.string.text_message_to_share)
                            }

                            InvitationType.GroupRoom.name -> {
                                sharedPageType.value = SearchFilterPageType.INVITATION
                                toolbar.title.text = getString(R.string.text_invitation)
                                intent?.let { it2 ->
                                    memberIds.clear()
                                    val groupMembers = it2.getStringArrayListExtra(BundleKey.ACCOUNT_IDS.key())
                                    groupMembers?.let {
                                        memberIds.addAll(it)
                                    }
                                }
                            }

                            InvitationType.Discuss.name -> { // 快來新增聊天吧入口
                                sharedPageType.value = SearchFilterPageType.INVITATION
                                toolbar.title.text = getString(R.string.text_invitation)
                                memberIds.clear()
                            }

                            InvitationType.ServiceNUmberConsultationAI.name -> {
                                sharedPageType.value = SearchFilterPageType.SERVICE_NUMBER_CONSULTATION_AI
                                toolbar.title.text = getString(R.string.text_service_number_consultation_ai)
                                activeServiceNumberId?.let {
                                    activeServiceNUmberId.value = it // 服務號聊天室的服務號id
                                } ?: ""
                                roomId?.let {
                                    activeRoomId.value = it // 服務號聊天室Id
                                } ?: ""
                                this@MemberInvitationActivity.activeConsultAIId?.let {
                                    activeConsultAIId.value = it
                                } ?: ""
                            }

                            else -> {
                                sharedPageType.value = SearchFilterPageType.INVITATION
                                toolbar.title.text = getString(R.string.text_invitation)
                                intent?.let { it2 ->
                                    memberIds.clear()
                                    val groupMembers = it2.getStringArrayListExtra(BundleKey.ACCOUNT_IDS.key())
                                    groupMembers?.let {
                                        memberIds.addAll(it)
                                    }
                                }
                            }
                        }
                    }
                    scopeSelectListBottomBar.visibility = View.GONE
                    toolbar.leftAction.setOnClickListener {
                        back()
                    }
                    selectedMemberList // 更新確認按鈕選中狀態
                        .onEach {
                            if (it.isNotEmpty()) {
                                btnSubmit.text = getString(R.string.text_create_group_submit, it.size.toString())
                                employeeSelectedList.clear()
                                employeeSelectedList.addAll(it)
                                scopeSelectListBottomBar.visibility = View.VISIBLE
                            } else {
                                scopeSelectListBottomBar.visibility = View.GONE
                            }
                        }.launchIn(this@MemberInvitationActivity.lifecycleScope)

                    submitMemberIds
                        .onEach {
                            submitToInitiation(it)
                        }.launchIn(this@MemberInvitationActivity.lifecycleScope)

                    transferMsgFailure
                        .onEach {
                            ToastUtils.showToast(this@MemberInvitationActivity, it)
                        }.launchIn(this@MemberInvitationActivity.lifecycleScope)
                    transferMsgSuccess
                        .onEach {
                            if (it.first == 1) {
                                EventBusUtils.sendEvent(EventMsg<Any?>(MsgConstant.REFRESH_FILTER))
                                val bundle = bundleOf(BundleKey.EXTRA_SESSION_ID.key() to it.second)
                                back()
                                IntentUtil.startIntent(this@MemberInvitationActivity, ChatNormalActivity::class.java, bundle)
                            } else {
                                ToastUtils.showToast(this@MemberInvitationActivity, getString(R.string.text_transfer_to_N_chat_room_already, it.first))
                                back()
                            }
                        }.launchIn(this@MemberInvitationActivity.lifecycleScope)

                    transferMsgNotFoundUser
                        .onEach {
                            ToastUtils.showToast(this@MemberInvitationActivity, getString(R.string.text_transfer_not_found_user, it))
                        }.launchIn(this@MemberInvitationActivity.lifecycleScope)

                    btnSubmit.setOnClickListener {
                        // 確認送出按鈕
                        if (NetworkUtils.isNetworkAvailable(this@MemberInvitationActivity)) {
                            showLoadingView()
                            onInvitationClick(this@MemberInvitationActivity, TokenPref.getInstance(this@MemberInvitationActivity).userId, roomType)
                        } else {
                            val toast =
                                when (sharedPageType.value) {
                                    SearchFilterPageType.INVITATION, SearchFilterPageType.PROVISIONAL_MEMBER -> getString(R.string.text_invite_member_failure)
                                    SearchFilterPageType.ShareScreenShot, SearchFilterPageType.SHARE_IN -> getString(R.string.text_share_media_failure)
                                    SearchFilterPageType.MESSAGE_TRANSFER -> getString(R.string.text_transfer_message_failure)
                                    else -> getString(R.string.text_invite_member_failure)
                                }
                            ToastUtils.showToast(this@MemberInvitationActivity, toast)
                        }
                    }

                    sendCollectDataDone
                        .onEach { n ->
                            progress += n
                            if (progress > 2) {
                                hideLoadingView()
                                progress = 0
                                searchFilterSharedViewModel.sendVerifyCountAndTransfer.emit(Unit)
                            }
                        }.launchIn(this@MemberInvitationActivity.lifecycleScope)

                    sendResultForServiceNumberConsultation
                        .onEach {
                            setResult(RESULT_OK, Intent().putExtra(BundleKey.ROOM_ID.key(), it))
                            back()
                        }.launchIn(this@MemberInvitationActivity.lifecycleScope)

                    sendOnAddProvisionalMemberComplete
                        .onEach {
                            val bundle = bundleOf("data" to JsonHelper.getInstance().toJson(it))
                            setResult(bundle)
                        }.launchIn(this@MemberInvitationActivity.lifecycleScope)

                    sendToastFailMsg
                        .onEach {
                            toastFailMsg(it)
                        }.launchIn(this@MemberInvitationActivity.lifecycleScope)

                    sendOnCompleteMemberAdded
                        .onEach {
                            val bundle = bundleOf("data" to JsonHelper.getInstance().toJson(it))
                            setResult(bundle)
                        }.launchIn(this@MemberInvitationActivity.lifecycleScope)

                    sendCompleted
                        .onEach {
                            finish()
                        }.launchIn(this@MemberInvitationActivity.lifecycleScope)
                }
            lifecycleOwner = this@MemberInvitationActivity
        }
    }

    private fun initViewModel() {
        val factory = ViewModelFactory(application)
        searchFilterSharedViewModel =
            ViewModelProvider(this, factory)[SearchFilterSharedViewModel::class.java]

        searchFilterSharedViewModel.apply {
            sendCreateDiscussRoomSuccess
                .onEach {
                    ToastUtils.showToast(this@MemberInvitationActivity, getString(R.string.text_create_room_successfully))
                    startActivity(
                        Intent(this@MemberInvitationActivity, ChatNormalActivity::class.java)
                            .putExtra(BundleKey.EXTRA_SESSION_ID.key(), it.id)
                    )
                    hideLoadingView()
                    back()
                }.launchIn(this@MemberInvitationActivity.lifecycleScope)
            sendCreateDiscussRoomFailure
                .onEach {
                    ToastUtils.showToast(this@MemberInvitationActivity, getString(it))
                    hideLoadingView()
                    back()
                }.launchIn(this@MemberInvitationActivity.lifecycleScope)
        }
    }

    private fun submitToInitiation(ids: List<String>) {
        roomId?.let {
            when (roomType) {
                InvitationType.ProvisionalMember.name -> {
                    searchFilterSharedViewModel.doProvisionalMemberAdd(it, ids)
                }

                InvitationType.GroupRoom.name -> {
                    searchFilterSharedViewModel.doChatMemberAdd(it, ids)
                }

                else -> {
                    onDiscussInvited(ids) // 聊天室內分享
                }
            }
        } ?: run {
            when (searchFilterSharedViewModel.sharedPageType.value) {
                SearchFilterPageType.SHARE_IN -> onShareIn(ids)
                SearchFilterPageType.INVITATION -> { // 從一般聊天列表下方 創建多人聊天室
                    val newIds = ids.toMutableList()
                    newIds.add(TokenPref.getInstance(this@MemberInvitationActivity).userId) // 成員包含自己
                    searchFilterSharedViewModel.doCreateRoom(newIds, ChatRoomType.discuss)
                }

                else -> {}
            }
        }
    }

    private fun back() {
        finish()
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out)
        if (roomType == InvitationType.ShareIn.name) {
            ActivityManager.finishAll()
        }
        KeyboardHelper.hide(this.currentFocus)
    }

    private fun onDiscussInvited(ids: List<String>) {
        val bundle = bundleOf("data" to JsonHelper.getInstance().toJson(ids))
        setResult(bundle)
    }

    private fun onShareIn(ids: List<String>) {
        val bundle =
            bundleOf(
                "data" to JsonHelper.getInstance().toJson(ids),
                BundleKey.FILE_PATH.key() to intent.getStringExtra(BundleKey.FILE_PATH.key())
            )
        setResult(bundle)
    }

    fun setResult(bundle: Bundle) {
        val intent = Intent()
        intent.putExtras(bundle)
        setResult(RESULT_OK, intent)

        finish()
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out)
        KeyboardHelper.hide(this.currentFocus)
    }

    fun toastFailMsg(msg: String) {
        hideLoadingView()
        ToastUtils.showToast(this, msg)
    }

    override fun onKeyDown(
        keyCode: Int,
        event: KeyEvent?
    ): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            back()
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun showLoadingView() {
        lifecycleScope.launch(Dispatchers.Main) {
            progressBar =
                IosProgressBar.show(
                    this@MemberInvitationActivity,
                    "",
                    true,
                    false
                ) { }
        }
    }

    private fun hideLoadingView() {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                if (progressBar?.isShowing == true) {
                    progressBar?.dismiss()
                }
            } catch (ignored: Exception) {
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (progressBar?.isShowing == true) {
            progressBar?.dismiss()
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun handleEvent(event: EventMsg<Any>) {
        when (event.code) {
            // 多人聊天室or社團成員被剔除
            MsgConstant.NOTICE_DISCUSS_GROUP_MEMBER_REMOVED -> {
                val data = JsonHelper.getInstance().fromToMap<String, Any>(event.data.toString())
                val deletedMemberIds = data["deletedMemberIds"] as List<String>?
                deletedMemberIds?.let {
                    val roomId = data["roomId"] as String
                    if (it.contains(searchFilterSharedViewModel.userId) && this.roomId == roomId) {
                        ToastUtils.showToast(this, getString(R.string.text_kick_out_member_by_others))
                        finish()
                    }
                }
            }
        }
    }
}
