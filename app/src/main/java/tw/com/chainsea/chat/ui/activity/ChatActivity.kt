package tw.com.chainsea.chat.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.common.base.Strings
import com.google.common.collect.Lists
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.android.common.event.KeyboardHelper
import tw.com.chainsea.android.common.json.JsonHelper
import tw.com.chainsea.ce.sdk.bean.BadgeDataModel
import tw.com.chainsea.ce.sdk.bean.GroupRefreshBean
import tw.com.chainsea.ce.sdk.bean.GroupUpgradeBean
import tw.com.chainsea.ce.sdk.bean.UserExitBean
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity
import tw.com.chainsea.ce.sdk.bean.account.UserType
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType
import tw.com.chainsea.ce.sdk.bean.room.DiscussMemberSocket
import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberStatus
import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberType
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberAddModel
import tw.com.chainsea.ce.sdk.bean.servicenumber.type.GroupPrivilegeEnum
import tw.com.chainsea.ce.sdk.database.DBManager
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.database.sp.UserPref
import tw.com.chainsea.ce.sdk.event.EventBusUtils
import tw.com.chainsea.ce.sdk.event.EventMsg
import tw.com.chainsea.ce.sdk.event.MsgConstant
import tw.com.chainsea.ce.sdk.http.ce.model.User
import tw.com.chainsea.ce.sdk.network.model.response.DeviceRecordItem
import tw.com.chainsea.ce.sdk.reference.AccountRoomRelReference
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference
import tw.com.chainsea.ce.sdk.reference.UserProfileReference
import tw.com.chainsea.ce.sdk.service.UserProfileService
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack
import tw.com.chainsea.ce.sdk.service.type.ChatRoomSource
import tw.com.chainsea.ce.sdk.service.type.RefreshSource
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.aiff.AiffManager
import tw.com.chainsea.chat.aiff.database.AiffDB
import tw.com.chainsea.chat.base.Constant
import tw.com.chainsea.chat.chatroomfilter.ChatRoomFilterActivity
import tw.com.chainsea.chat.config.AiffDisplayLocation
import tw.com.chainsea.chat.config.AiffEmbedLocation
import tw.com.chainsea.chat.config.BundleKey
import tw.com.chainsea.chat.config.InvitationType
import tw.com.chainsea.chat.databinding.ActivityChatLayoutBinding
import tw.com.chainsea.chat.databinding.GridMemberViewBinding
import tw.com.chainsea.chat.databinding.GridViewBinding
import tw.com.chainsea.chat.lib.NetworkUtils
import tw.com.chainsea.chat.lib.ToastUtils
import tw.com.chainsea.chat.mainpage.view.MainPageActivity
import tw.com.chainsea.chat.messagekit.listener.OnChatRoomTitleChangeListener
import tw.com.chainsea.chat.network.contact.ViewModelFactory
import tw.com.chainsea.chat.searchfilter.view.activity.CreateDiscussActivity
import tw.com.chainsea.chat.searchfilter.view.activity.CreateGroupActivity
import tw.com.chainsea.chat.searchfilter.view.activity.MemberInvitationActivity
import tw.com.chainsea.chat.searchfilter.view.fragment.CommunitiesSearchFragment
import tw.com.chainsea.chat.searchfilter.view.fragment.ContactPersonClientSearchFragment
import tw.com.chainsea.chat.searchfilter.view.fragment.ServiceNumberSearchFragment
import tw.com.chainsea.chat.style.RoomThemeStyle
import tw.com.chainsea.chat.ui.adapter.ChatRoomMembersAdapter
import tw.com.chainsea.chat.ui.adapter.LoginDevicesInfoAdapter
import tw.com.chainsea.chat.ui.adapter.RichMenuAdapter
import tw.com.chainsea.chat.ui.adapter.entity.RichMenuInfo
import tw.com.chainsea.chat.ui.dialog.BottomSheetDialogBuilder
import tw.com.chainsea.chat.ui.fragment.ChatFragment
import tw.com.chainsea.chat.util.IntentUtil
import tw.com.chainsea.chat.util.SortUtil
import tw.com.chainsea.chat.util.TextViewHelper
import tw.com.chainsea.chat.util.ThemeHelper
import tw.com.chainsea.chat.util.UnreadUtil.getUnreadText
import tw.com.chainsea.chat.view.BaseActivity
import tw.com.chainsea.chat.view.chat.ChatViewModel
import tw.com.chainsea.chat.view.contact.ContactPersonFragment
import tw.com.chainsea.chat.view.homepage.EmployeeInformationHomepageActivity
import tw.com.chainsea.chat.view.homepage.SelfInformationHomepageActivity
import tw.com.chainsea.chat.view.homepage.SubscribeInformationHomepageActivity
import tw.com.chainsea.chat.view.homepage.VisitorHomepageActivity
import tw.com.chainsea.chat.view.login.LogoutSmsDialogFragment
import tw.com.chainsea.chat.view.service.ServiceNumberManageActivity
import tw.com.chainsea.chat.widget.GridItemDecoration
import tw.com.chainsea.custom.view.alert.AlertView
import java.text.MessageFormat

class ChatActivity :
    BaseActivity(),
    ChatRoomMembersAdapter.OnItemClickListener {
    private val viewModelFactory by lazy { ViewModelFactory(application) }

    private val chatViewModel: ChatViewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[ChatViewModel::class.java]
    }

    private val binding: ActivityChatLayoutBinding by lazy {
        ActivityChatLayoutBinding.inflate(layoutInflater)
    }

    private var aiffManager: AiffManager? = null
    private var roomId = ""
    private var userName = ""
    private val selfUserId by lazy {
        TokenPref.getInstance(this).userId
    }

    private var chatFragment: ChatFragment? = null

    private var themeStyle = RoomThemeStyle.UNDEF

    private var aiffPopupWindows: PopupWindow? = null
    private var memberPopupWindow: PopupWindow? = null

    // activity result launcher
    private var addMemberARL: ActivityResultLauncher<Intent>? = null
    private var addProvisionalMemberARL: ActivityResultLauncher<Intent>? = null
    private var updateGroupARL: ActivityResultLauncher<Intent>? = null
    private var toGroupSessionARL: ActivityResultLauncher<Intent>? = null

    // 個人聊天室裝置 adapter
    private val loginDevicesInfoAdapter by lazy { LoginDevicesInfoAdapter() }

    // 成員列表 Adapter
    private var chatRoomMembersAdapter: ChatRoomMembersAdapter? = null

    private var isDeletedMember = false

    private var rightCancelTV: TextView? = null

    private var newFriendRoomWhereCome: String? = null
    private var isNeedRefreshList: Boolean = false
    private var isGreenTheme: Boolean = false

    override fun onStart() {
        super.onStart()
        initActivityLaunch()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        onViewCreated()
        chatViewModel.setChatRoomNotDeleted()
    }

    private fun onViewCreated() {
        init()
        observerData()
        initListener()
        initPopupWindows()
        clearAtMessage()
    }

    private fun init() =
        CoroutineScope(Dispatchers.IO).launch {
            intent?.let {
                if (it.hasExtra(BundleKey.EXTRA_SESSION_ID.key())) {
                    roomId = it.getStringExtra(BundleKey.EXTRA_SESSION_ID.key()).toString()
                    val serializable = it.getSerializableExtra(BundleKey.EXTRA_ROOM_ENTITY.key())
                    chatViewModel.roomEntity =
                        if (serializable is ChatRoomEntity) {
                            serializable
                        } else {
                            ChatRoomReference.getInstance().findById2("", roomId, true, true, true, true, true)
                        }
                }

                // 交互時間
                if (it.hasExtra(BundleKey.WHERE_COME.key())) {
                    val whereCome = it.getStringExtra(BundleKey.WHERE_COME.key())
                    setWhereCome(whereCome)
                }

                userName = it.getStringExtra(BundleKey.USER_NICKNAME.key()).toString()
                val friendUserId = it.getStringExtra(BundleKey.USER_ID.key())

                chatViewModel.roomEntity?.let {
                    chatViewModel.setRoomType()
                    UserPref.getInstance(this@ChatActivity).setCurrentRoomIds(it.id)
                    aiffManager = AiffManager(this@ChatActivity, roomId)
                    chatViewModel.checkIsChatMember()
                    chatViewModel.getChatMemberFromService()
                    initChatRoom()
                } ?: run {
                    if (!Strings.isNullOrEmpty(friendUserId)) {
                        chatViewModel.getChatRoomEntity(friendUserId!!, userName)
                        initVirtualChatRoom()
                    } else if (roomId.isNotEmpty()) {
                        chatViewModel.getRoomItem(roomId)
                    } else {
                        // nothing
                    }
                }
            }
            chatViewModel.apply {
                sendUpdatedRoomTitle
                    .onEach { newTitle ->
                        roomEntity?.let {
                            it.name = newTitle
                            it.isCustomName = true
                            setTitleBar()
                        }
                    }.launchIn(this@ChatActivity.lifecycleScope)
            }
        }

    @SuppressLint("SetTextI18n")
    private fun observerData() {
        homeViewModel.chatRoomUnreadNumber.observe(this) { count: Int -> setUnreadCount(count) }
        homeViewModel.serviceRoomUnreadNumber.observe(this) { count: Int -> setUnreadCount(count) }

        homeViewModel.sendLoginDevicesList.observe(this) {
            loginDevicesInfoAdapter.setTheme(isGreenTheme)
            loginDevicesInfoAdapter.submitList(it)
            binding.scopeDevices.visibility = View.VISIBLE
            binding.scopeDevicesList.visibility = View.VISIBLE
            binding.devicesNumber.text = it.size.toString()
            loginDevicesInfoAdapter.itemClickListener = { deviceRecordItem ->
                if (homeViewModel.timer.hasObservers()) homeViewModel.timer.removeObservers(this)
                doShowLoginDeviceSettingBottomSheetDialog(deviceRecordItem)
            }
        }

        homeViewModel.timer.observe(this) {
            if (it <= 0) {
                if (binding.scopeDevicesList.visibility == View.VISIBLE) {
                    binding.scopeDevicesList.visibility = View.GONE
                }
            }
        }
        homeViewModel.sendToast.observe(this) {
            Toast.makeText(this, getString(it), Toast.LENGTH_SHORT).show()
        }

        chatViewModel.chatRoomEntity.observe(this) {
            chatViewModel.setRoomType()
            chatViewModel.roomEntity = it
            UserPref.getInstance(this@ChatActivity).setCurrentRoomIds(it.id)
//            UserPref.getInstance(this@ChatActivity).currentRoomId = it.id
            initChatRoom()
            aiffManager = AiffManager(this@ChatActivity, it.id)
            // 加完好友通知更新聊天室列表
            EventBus.getDefault().post(EventMsg<Any>(MsgConstant.REFRESH_ROOM_BY_LOCAL))
            // 新增聊天室後更新交互時間
            setWhereCome(newFriendRoomWhereCome)
            newFriendRoomWhereCome = null
        }

        chatViewModel.updateChatRoomEntity.observe(this) {
            chatViewModel.roomEntity = it
            setTitleBar()
            EventBusUtils.sendEvent(
                EventMsg<Any?>(
                    MsgConstant.GROUP_REFRESH_FILTER,
                    GroupRefreshBean(it.id)
                )
            )
        }

        chatViewModel.sendUpdateMember.observe(this) {
            chatViewModel.roomEntity = it.second
            isDeletedMember = it.first
            setTitleBar()
            ChatRoomReference.getInstance().save(it.second)
            chatRoomMembersAdapter?.setData(it.third)?.refreshData()
        }

        chatViewModel.sendProvisionalMember.observe(this) {
            chatFragment?.onRefreshMemberList(it)
        }

        chatViewModel.sendCloseChatActivity.observe(this) {
            if (it > 0) {
                ToastUtils.showToast(this, getString(it))
            }
            EventBus.getDefault().post(EventMsg<Any>(MsgConstant.NOTICE_REFRESH_CHAT_ROOM_LIST))
            finish()
        }

        // UpgradeGroup
        chatViewModel.upgradeGroup.observe(this) {
            CoroutineScope(Dispatchers.IO).launch {
                chatViewModel.roomEntity?.let { chatRoomEntity ->
                    chatRoomEntity.name = it.name
                    chatRoomEntity.type = ChatRoomType.group
                    chatRoomEntity.roomType = ChatRoomType.group
                    chatRoomEntity.ownerId = it.ownerId
                    chatRoomEntity.members.clear()
                    chatRoomEntity.memberIds.clear()
                    // 將擁有者放到第一個 再來是管理者 最後才是一般成員
                    val soredList = SortUtil.sortOwnerManagerByBoolean(it.members)
                    soredList.forEach {
                        chatRoomEntity.members.add(it)
                        chatRoomEntity.memberIds.add(it.id)
                        DBManager.getInstance().insertFriends(it)
                    }
                    chatRoomMembersAdapter?.refreshData()
                    setTitleBar()
                    AccountRoomRelReference.deleteRelByRoomId(null, chatRoomEntity.id)
                    AccountRoomRelReference.batchSaveByAccountIdsAndRoomId(
                        null,
                        chatRoomEntity.id,
                        chatRoomEntity.memberIds
                    )
                    ChatRoomReference.getInstance().save(chatRoomEntity)
                    DBManager.getInstance().insertGroup(it)
                    ChatRoomReference.getInstance().updateTitleById(chatRoomEntity.id, it.name)
                }
            }
        }

        // 顯示轉乘服務人員提示
        chatViewModel.showServiceNumberAddFromProvisionalDialog.observe(this) {
            showProvisionalToServiceNumber(it)
        }

        chatViewModel.userProfile.observe(this) {
            val bundle =
                bundleOf(
                    BundleKey.ACCOUNT_TYPE.key() to it.userType,
                    BundleKey.ACCOUNT_ID.key() to it.id,
                    BundleKey.WHERE_COME.key() to javaClass.simpleName
                )
            IntentUtil.startIntent(this, EmployeeInformationHomepageActivity::class.java, bundle)
        }

        chatViewModel.isRoomDismissSuccess.observe(this) {
            Toast.makeText(this, getString(R.string.text_disband_crowd_success), Toast.LENGTH_SHORT).show()
            chatFragment?.release()
            exitRoom()
        }

        chatViewModel.isChatMemberExitSuccess.observe(this) {
            Toast.makeText(this@ChatActivity, getString(R.string.text_leave_crowd_success), Toast.LENGTH_SHORT).show()
            exitRoom()
        }

        chatViewModel.isRoomMute.observe(this) {
            chatViewModel.roomEntity?.setMute(it)
        }

        chatViewModel.agentsList.observe(this) {
            chatViewModel.roomEntity?.agentsList = it
        }

        chatViewModel.errorMessage.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            EventBus.getDefault().post(EventMsg<Any>(MsgConstant.REFRESH_ROOM_BY_LOCAL))
            homeViewModel.sendRefreshDB.postValue(true) // 刷新列表
            finish()
        }

        chatViewModel.memberList.observe(this) {
            chatViewModel.roomEntity?.members = it
        }
        chatViewModel.sendToast
            .onEach {
                ToastUtils.showToast(this, getString(it))
            }.launchIn(this@ChatActivity.lifecycleScope)
        chatViewModel.sendToastByWord
            .onEach { string ->
                ToastUtils.showToast(this, string)
            }.launchIn(this@ChatActivity.lifecycleScope)
        chatViewModel.sendGroupAddPermission
            .onEach { isGroupManagerOrOwner ->
                chatViewModel.roomEntity?.let {
                    if (!ChatRoomType.SERVICES_or_SUBSCRIBE_or_SYSTEM_or_PERSON_or_SERVICE_MEMBER.contains(it.type)) {
                        if (it.type == ChatRoomType.group && !isGroupManagerOrOwner) {
                            binding.inviteIV.visibility = View.GONE
                        } else {
                            binding.inviteIV.visibility = View.VISIBLE
                        }
                    } else {
                        binding.inviteIV.visibility = View.GONE
                    }
                }
            }.launchIn(this@ChatActivity.lifecycleScope)
    }

    private fun exitRoom() =
        CoroutineScope(Dispatchers.IO).launch {
            chatViewModel.roomEntity?.let {
                EventBusUtils.sendEvent(EventMsg<Any?>(MsgConstant.REMOVE_GROUP_FILTER, it.id))
                chatFragment?.release()
                finish()
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out)
            }
        }

    private fun initListener() {
        binding.searchCancelTV.setOnClickListener { doSearchCancelAction() }
        binding.inviteIV.setOnClickListener {
            chatViewModel.roomEntity?.let { roomEntity ->
                when (roomEntity.roomType) {
                    ChatRoomType.group, ChatRoomType.discuss -> {
                        chatViewModel.getChatHomePage(roomEntity.id)
                        addMember()
                    }

                    ChatRoomType.friend -> {
                        if (roomEntity.memberIds.isNotEmpty()) {
                            val bundle =
                                bundleOf(
                                    BundleKey.ACCOUNT_IDS.key() to roomEntity.memberIds
                                )
                            IntentUtil.startIntent(this, CreateDiscussActivity::class.java, bundle)
                        } else {
                            roomEntity.id?.let {
                                CoroutineScope(Dispatchers.Main).launch {
                                    val list = chatViewModel.doGetChatRoomMemberIds(it).single()
                                    if (list.isNotEmpty()) {
                                        val bundle =
                                            bundleOf(
                                                BundleKey.ACCOUNT_IDS.key() to list
                                            )
                                        IntentUtil.startIntent(this@ChatActivity, CreateDiscussActivity::class.java, bundle)
                                    } else {
                                        Toast.makeText(this@ChatActivity, getString(R.string.text_new_chat_room_failure), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        // nothing
                    }
                }
            }
        }
        binding.leftAction.setOnClickListener {
            KeyboardHelper.hide(it)
            chatViewModel.roomEntity?.let {
                UserPref.getInstance(this).removeCurrentRoomId(it.id)
            }
//            UserPref.getInstance(this).currentRoomId = ""
            finish()
            overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out)
        }
        binding.rightAction.setOnClickListener {
            if (!isFinishing) rightViewToggle()
        }

        binding.ivSearch.setOnClickListener { doSearchAction() }

        binding.ivChannel.setOnClickListener {
            if (chatFragment is ChatFragment) {
                (chatFragment as ChatFragment).doChannelChangeAction()
            }
        }

        binding.titleBar.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                chatViewModel.roomEntity?.let {
                    when (it.type) {
                        ChatRoomType.person -> {
                            val bundle =
                                bundleOf(
                                    BundleKey.ACCOUNT_ID.key() to selfUserId,
                                    BundleKey.ACCOUNT_TYPE.key() to UserType.EMPLOYEE.name,
                                    BundleKey.WHERE_COME.key() to javaClass.simpleName
                                )
                            IntentUtil.startIntent(
                                this@ChatActivity,
                                SelfInformationHomepageActivity::class.java,
                                bundle
                            )
                        }

                        ChatRoomType.services -> {
                            // 物件聊天室 好像沒了
//                        if (!it.businessId.isNullOrEmpty()) {
//                            val businessEntities = ChatRoomReference.getInstance().findServiceBusinessByServiceNumberIdAndOwnerIdAndNotRoomId(it.serviceNumberId, it.ownerId, it.id, ChatRoomType.services)
//                            showSinkingBusinessMemberListPopupWindow(businessEntities)
//                        }
                            val profile = UserProfileReference.findById(null, it.ownerId)
                            profile?.let { userProfile ->
                                if (UserType.VISITOR == userProfile.userType || UserType.CONTACT == userProfile.userType) {
                                    if (!checkClientMainPageFromAiff()) {
                                        val bundle =
                                            bundleOf(
                                                BundleKey.ACCOUNT_TYPE.key() to userProfile.userType,
                                                BundleKey.ACCOUNT_ID.key() to it.ownerId,
                                                BundleKey.ROOM_ID.key() to it.id,
                                                BundleKey.USER_NICKNAME.key() to userProfile.nickName,
                                                BundleKey.WHERE_COME.key() to javaClass.simpleName
                                            )
                                        IntentUtil.startIntent(this@ChatActivity, VisitorHomepageActivity::class.java, bundle)
                                    }
                                } else {
                                    val bundle =
                                        bundleOf(
                                            BundleKey.ACCOUNT_TYPE.key() to userProfile.userType,
                                            BundleKey.ACCOUNT_ID.key() to userProfile.id,
                                            BundleKey.WHERE_COME.key() to javaClass.simpleName
                                        )
                                    IntentUtil.startIntent(this@ChatActivity, EmployeeInformationHomepageActivity::class.java, bundle)
                                }
                            }
                        }

                        ChatRoomType.group, ChatRoomType.discuss -> {
                            memberPopupWindow?.let {
                                if (it.isShowing) return@launch
                            }
                            showSinkingMemberListPopupWindow()
                        }

                        ChatRoomType.friend, ChatRoomType.strange -> {
                            if (!it.businessId.isNullOrEmpty()) {
//                                showSinkingBusinessMemberListPopupWindow()
                            } else {
                                val targetMember = it.memberIds.find { id -> id != selfUserId }
                                targetMember?.let { id ->
                                    chatViewModel.getUserItem(id)
                                }
                            }
                        }

                        ChatRoomType.serviceMember -> {
                            showSinkingServiceMemberListPopupWindow()
                        }

                        ChatRoomType.subscribe -> {
                            val bundle =
                                bundleOf(
                                    BundleKey.SUBSCRIBE_NUMBER_ID.key() to it.serviceNumberId,
                                    BundleKey.ROOM_ID.key() to it.id,
                                    BundleKey.IS_SUBSCRIBE.key() to true,
                                    BundleKey.WHERE_COME.key() to javaClass.simpleName
                                )
                            IntentUtil.startIntent(this@ChatActivity, SubscribeInformationHomepageActivity::class.java, bundle)
                            overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out)
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    private fun getUnreadData() =
        CoroutineScope(Dispatchers.IO).launch {
            chatViewModel.roomEntity?.let {
                // -1 是被左滑標記未讀
                if (it.unReadNum > 0 || it.unReadNum == -1) {
                    homeViewModel.setRead(it)
                    DBManager.getInstance().setChatRoomListItemInteractionTime(it.id)
                    it.updateTime = System.currentTimeMillis()
                    isNeedRefreshList = true
                }
                // 如果在服務號列表下，只拿服務號列表的未讀數
                if (it.listClassify == ChatRoomSource.SERVICE) {
                    homeViewModel.getServiceRoomUnReadSum()
                } else {
                    homeViewModel.getChatRoomListUnReadSum()
                }
            }
        }

    private fun initActivityLaunch() {
        addMemberARL =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    val bundle = it.data?.extras
                    bundle?.let {
                        val listType = object : TypeToken<List<UserProfileEntity>>() {}.type
                        val data = JsonHelper.getInstance().from<List<UserProfileEntity>>(bundle.getString("data"), listType)
                        onInviteSuccess(data)
                    }
                }
            }

        updateGroupARL =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    it.data?.let { data ->
                        val sessionId = data.getStringExtra(BundleKey.EXTRA_SESSION_ID.key())
                        val groupName = data.getStringExtra(BundleKey.EXTRA_TITLE.key())
                        chatViewModel.roomEntity = data.getSerializableExtra(BundleKey.EXTRA_SESSION.key()) as ChatRoomEntity?
                        chatViewModel.roomEntity?.let {
                            it.roomType = ChatRoomType.group
                            it.name = groupName
                            init()
                        } ?: run {
                            chatViewModel.roomEntity = ChatRoomReference.getInstance().findById(sessionId)
                        }
                    }
                }
            }

        toGroupSessionARL =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    it.data?.let { data ->
                        val extra = data.getStringArrayExtra(Constant.ACTIVITY_RESULT)
                        extra?.let {
                            chatViewModel.roomEntity?.name = extra[0]
                            setTitleBar()
                        }
                    }
                }
            }

        addProvisionalMemberARL =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                CoroutineScope(Dispatchers.IO).launch {
                    if (it.resultCode == RESULT_OK) {
                        val bundle = it.data?.extras
                        bundle?.let {
                            val listType = object : TypeToken<List<String>>() {}.type
                            val data = JsonHelper.getInstance().from<List<String>>(bundle.getString("data"), listType)
                            if (chatFragment is ChatFragment) {
                                (chatFragment as ChatFragment).onRefreshMemberList(data)
                            }
                        }
                    }
                }
            }
    }

    // 更新交互時間
    private fun setWhereCome(whereCome: String?) {
        whereCome?.let {
            chatViewModel.roomEntity?.let { entity ->
                if ((
                        it == ContactPersonFragment::class.java.simpleName ||
                            it == CommunitiesSearchFragment::class.java.simpleName ||
                            it == ServiceNumberSearchFragment::class.java.simpleName ||
                            it == ContactPersonClientSearchFragment::class.java.simpleName
                    ) &&
                    !entity.type.equals(ChatRoomType.person)
                ) {
                    DBManager.getInstance().setChatRoomListItemInteractionTime(entity.id) // 個人聊天室不更新交互時間
                    entity.updateTime = System.currentTimeMillis()
                    EventBusUtils.sendEvent(EventMsg<Any?>(MsgConstant.NOTICE_REFRESH_CHAT_ROOM_LIST)) // 更新列表
                }
            } ?: run {
                // 針對還沒有好友聊天室的情況
                newFriendRoomWhereCome = it
            }
        }
    }

    private fun initVirtualChatRoom() =
        CoroutineScope(Dispatchers.Main).launch {
            binding.title.text = getTitleText()
            addFragment(ChatFragment.newInstance(userName, selfUserId))
        }

    private fun initChatRoom() =
        CoroutineScope(Dispatchers.IO).launch {
            chatViewModel.roomEntity?.let { chatRoomEntity ->
                if (chatRoomEntity.roomType == ChatRoomType.undef) chatViewModel.setRoomType()
                when (chatRoomEntity.roomType) {
                    ChatRoomType.serviceMember,
                    ChatRoomType.bossServiceNumber,
                    ChatRoomType.services,
                    ChatRoomType.serviceINumberStaff,
                    ChatRoomType.serviceONumberStaff,
                    ChatRoomType.provisional,
                    ChatRoomType.bossSecretary -> {
                        themeStyle = RoomThemeStyle.SERVICES
                        ThemeHelper.updateServiceChatRoomTheme(isServiceRoom = true)
                    }

                    ChatRoomType.bossOwnerWithSecretary,
                    ChatRoomType.bossOwner,
                    ChatRoomType.group,
                    ChatRoomType.friend,
                    ChatRoomType.person,
                    ChatRoomType.discuss,
                    ChatRoomType.system -> {
                        themeStyle = RoomThemeStyle.FRIEND
                        ThemeHelper.updateServiceChatRoomTheme(isServiceRoom = false)
                    }

                    else -> {
                        if (!Strings.isNullOrEmpty(chatRoomEntity.businessId)) {
                            themeStyle = RoomThemeStyle.BUSINESS
                        }
                        ThemeHelper.updateServiceChatRoomTheme(isServiceRoom = false)
                    }
                }
                intent?.let {
                    val msg = it.getSerializableExtra(BundleKey.EXTRA_MESSAGE.key()) as MessageEntity?
                    val keyword = it.getStringExtra(BundleKey.SEARCH_KEY.key())
                    val unreadId = it.getStringExtra(BundleKey.UNREAD_MESSAGE_ID.key())
                    if (chatRoomEntity.type == ChatRoomType.services ||
                        chatRoomEntity.type == ChatRoomType.subscribe ||
                        chatRoomEntity.type == ChatRoomType.serviceMember
                    ) {
                        addFragment(ChatFragment.newInstance(msg, unreadId, keyword, themeStyle))
                    }
                }

                setThemeStyle()
                setTitleBar()
                setLoginDevicesStatus()
                getUnreadData()
                if (chatRoomEntity.roomType == ChatRoomType.serviceMember ||
                    chatRoomEntity.roomType == ChatRoomType.bossSecretary ||
                    chatRoomEntity.roomType == ChatRoomType.bossOwnerWithSecretary
                ) {
                    chatViewModel.getServiceEntity(chatRoomEntity.serviceNumberId)
                }
                getMembers(chatRoomEntity)
                if (chatRoomEntity.serviceNumberOpenType.contains("O") && chatRoomEntity.type == ChatRoomType.services) {
                    chatViewModel.getBusinessCardInfo(chatRoomEntity.serviceNumberId) // 取得電子名片設定
                    chatViewModel.getLastChannelFrom(chatRoomEntity.id) // 取得渠道名稱以發送電子名片
                }
            }
        }

    private fun getMembers(chatRoomEntity: ChatRoomEntity?) =
        CoroutineScope(Dispatchers.IO).launch {
            chatRoomEntity?.let {
                chatViewModel.getChatMemberList(it)
            }
        }

    private fun setThemeStyle() =
        CoroutineScope(Dispatchers.Main).launch {
            isGreenTheme = ThemeHelper.isGreenTheme()
            // 服務號以外且是green主題都呈現綠色
            window.statusBarColor = if (isGreenTheme && themeStyle != RoomThemeStyle.SERVICES) resources.getColor(R.color.color_015F57, null) else themeStyle.mainColor
            binding.titleBar.setBackgroundColor(if (isGreenTheme && themeStyle != RoomThemeStyle.SERVICES) resources.getColor(R.color.color_015F57, null) else themeStyle.mainColor)
            binding.searchBar.setBackgroundColor(if (isGreenTheme && themeStyle != RoomThemeStyle.SERVICES) resources.getColor(R.color.color_015F57, null) else themeStyle.mainColor)
        }

    // 設置 title bar
    private fun setTitleBar() =
        CoroutineScope(Dispatchers.Main).launch {
            chatViewModel.roomEntity?.let { roomEntity ->
                if (roomEntity.roomType == ChatRoomType.undef) chatViewModel.setRoomType()
                when (roomEntity.roomType) {
                    // 專業服務號
                    ChatRoomType.services -> {
                        binding.title.text = getTitleText()
                        getServiceIcon(roomEntity.ownerId)
                    }

                    // 服務號成員聊天室
                    ChatRoomType.serviceMember -> {
                        val drawable =
                            if (roomEntity.serviceNumberType == ServiceNumberType.BOSS && roomEntity.ownerId == selfUserId) {
                                if (isGreenTheme) R.drawable.ic_service_member_b_green else R.drawable.ic_service_member_b
                            } else {
                                if (isGreenTheme) R.drawable.ic_service_member_group__green_16dp else R.drawable.ic_service_member_group_16dp
                            }

                        binding.title.text =
                            TextViewHelper.setLeftImage(
                                this@ChatActivity,
                                getTitleText(),
                                drawable
                            )
                    }

                    // 商務號
                    ChatRoomType.bossServiceNumber,
                    // 外部服務號服務人員
                    ChatRoomType.serviceONumberStaff,
                    // 商務號擁有者
                    ChatRoomType.bossOwner -> {
                        getServiceIcon(roomEntity.ownerId)
                        binding.tvBusinessName.apply {
                            text = TextViewHelper.setLeftImage(this@ChatActivity, roomEntity.serviceNumberName, R.drawable.ic_slice_o)
                            visibility = View.VISIBLE
                        }
                    }

                    // 商務號秘書聊天室
                    ChatRoomType.bossSecretary -> {
                        if (roomEntity.serviceNumberOwnerId == selfUserId) {
                            binding.title.text =
                                TextViewHelper.setLeftImage(
                                    this@ChatActivity,
                                    getTitleText(),
                                    if (isGreenTheme) R.drawable.ic_service_member_b_green else R.drawable.ic_service_member_b
                                )
                        } else {
                            binding.title.text =
                                TextViewHelper.setLeftImage(
                                    this@ChatActivity,
                                    getTitleText(),
                                    if (isGreenTheme) R.drawable.ic_service_member_group__green_16dp else R.drawable.ic_service_member_group_16dp
                                )
                        }
                    }

                    // 群組聊天室
                    ChatRoomType.group -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            var memberSize = 0
                            roomEntity.chatRoomMember?.let { memberIds ->
                                if (memberIds.isNotEmpty()) {
                                    memberSize = memberIds.size
                                } else {
                                    val groupInfo = DBManager.getInstance().queryGroupInfo(roomEntity.id)
                                    groupInfo?.let { group ->
                                        memberSize = group.memberIds?.size ?: -1
                                    }
                                }
                            } ?: run {
                                roomEntity.memberIds?.let {
                                    memberSize = it.size
                                }
                            }
                            CoroutineScope(Dispatchers.Main).launch {
                                binding.title.text =
                                    TextViewHelper.getTitleWithIcon(
                                        binding.title.context,
                                        roomEntity.name,
                                        memberSize,
                                        R.drawable.icon_group_chat_room
                                    )
                            }
                        }
                    }

                    // 個人
                    ChatRoomType.person -> {
                        binding.title.text =
                            TextViewHelper.setLeftImage(
                                this@ChatActivity,
                                getTitleText(),
                                if (isGreenTheme) R.drawable.ic_green_self_room_20dp else R.drawable.icon_self_chat_room_20dp
                            )
                    }

                    // 多人
                    ChatRoomType.discuss -> {
                        binding.title.text = getTitleText()
                    }

                    // 內部服務號服務人員
                    ChatRoomType.serviceINumberStaff -> {
                        binding.title.text = getTitleText()
                        binding.tvBusinessName.apply {
                            text = TextViewHelper.setLeftImage(this@ChatActivity, roomEntity.serviceNumberName, R.drawable.ic_slice_i)
                            visibility = View.VISIBLE
                        }
                    }

                    ChatRoomType.serviceINumberAsker -> {
                        binding.title.text =
                            TextViewHelper.setLeftImage(
                                this@ChatActivity,
                                getTitleText(),
                                R.drawable.icon_subscribe_number_pink_15dp
                            )
                    }

                    ChatRoomType.system -> {
                        val systemUserName = TokenPref.getInstance(this@ChatActivity).systemUserName
                        binding.title.text = if (systemUserName.isNullOrEmpty()) getTitleText() else systemUserName
                    }

                    else -> {
                        binding.rightAction.setImageResource(R.drawable.icon_aipower_open)
                        TokenPref.getInstance(this@ChatActivity).isEnableCall
                        binding.title.text = getTitleText()
                    }
                }
                // 判斷是否顯示 + 按鈕；因為chatFragment已經針對group有設置了getChatMember，所以為了不要再次請求api，這邊只針對群組以外的聊天室做判斷
                if (roomEntity.type == ChatRoomType.group) {
                    chatViewModel.doHandleGroupMemberPrivilege()
                }
            } ?: run {
                if (selfUserId.isNullOrEmpty()) {
                    binding.rightAction.visibility = View.GONE
                }
            }
        }

    private fun getServiceIcon(roomOwnerId: String) =
        CoroutineScope(Dispatchers.IO).launch {
            UserProfileService.getProfileIsEmployee(
                this@ChatActivity,
                roomOwnerId,
                object : ServiceCallBack<UserType, RefreshSource> {
                    override fun complete(
                        type: UserType,
                        refreshSource: RefreshSource
                    ) {
                        CoroutineScope(Dispatchers.Main).launch {
                            when (type) {
                                UserType.VISITOR ->
                                    binding.title.text =
                                        TextViewHelper.setLeftImage(
                                            this@ChatActivity,
                                            chatViewModel.roomEntity?.name,
                                            R.drawable.ic_visitor_15dp
                                        )

                                UserType.CONTACT ->
                                    binding.title.text =
                                        TextViewHelper.setLeftImage(
                                            this@ChatActivity,
                                            chatViewModel.roomEntity?.name,
                                            R.drawable.ic_customer_15dp
                                        )

                                else -> {
                                    binding.title.text = chatViewModel.roomEntity?.name
                                }
                            }
                        }
                    }

                    override fun error(message: String) {
                        CoroutineScope(Dispatchers.Main).launch {
                            binding.title.text =
                                TextViewHelper.setLeftImage(
                                    this@ChatActivity,
                                    chatViewModel.roomEntity?.name,
                                    R.drawable.ic_customer_15dp
                                )
                        }
                    }
                }
            )
        }

    private fun setLoginDevicesStatus() =
        CoroutineScope(Dispatchers.IO).launch {
            chatViewModel.roomEntity?.let {
                if (it.roomType == ChatRoomType.person) {
                    binding.RvLoginDevices.adapter = loginDevicesInfoAdapter
                    homeViewModel.startCountdown(5)
                    homeViewModel.getLoginDevicesList()
                    binding.ivDevices.setOnClickListener {
                        if (binding.scopeDevicesList.visibility == View.GONE) {
                            binding.scopeDevicesList.visibility = View.VISIBLE
                        } else {
                            binding.scopeDevicesList.visibility = View.GONE
                        }
                    }
                }
            }
        }

    // 取得各聊天室標題
    private suspend fun getTitleText(): String =
        withContext(Dispatchers.IO) {
//        val localChatRoomEntity =  ChatRoomReference.getInstance().findById(roomId)
            chatViewModel.roomEntity?.let {
                if (!Strings.isNullOrEmpty(it.businessName)) {
                    withContext(Dispatchers.Main) {
                        binding.tvBusinessName.visibility = View.VISIBLE
                        binding.tvBusinessName.text = MessageFormat.format("{0}", it.businessName)
                    }
                }
                when (it.roomType) {
                    // 商務號擁有者
                    ChatRoomType.bossOwner -> {
                        return@withContext it.getServicesNumberTitle(selfUserId) ?: ""
                    }

                    // 商務號秘書聊天室
                    ChatRoomType.bossSecretary,
                    ChatRoomType.bossOwnerWithSecretary -> {
                        withContext(Dispatchers.Main) {
                            binding.tvBusinessName.visibility = View.VISIBLE
                            binding.tvBusinessName.text = it.serviceNumberName
                        }
                        return@withContext getString(R.string.text_chat_room_service_member_business, "(" + it.memberIds.size + ")")
                    }

                    // 內部服務號服務人員
                    ChatRoomType.serviceINumberStaff -> {
                        val customProfile = DBManager.getInstance().queryUser(it.ownerId)
                        return@withContext customProfile?.nickName ?: ""
                    }

                    ChatRoomType.serviceINumberAsker,
                    // 服務號
                    ChatRoomType.services -> {
                        // 商務號
//                    if (ServiceNumberType.BOSS == it.serviceNumberType) {
//                        return@withContext it.name
//                    }
                        return@withContext it.serviceNumberName
                    }

                    // 服務號成員聊天室
                    ChatRoomType.serviceMember -> {
                        val memberIds = AccountRoomRelReference.findMemberIdsByRoomId(null, roomId)
                        withContext(Dispatchers.Main) {
                            binding.tvBusinessName.visibility = View.VISIBLE
                            binding.tvBusinessName.text = MessageFormat.format("{0}", it.serviceNumberName)
                        }
                        return@withContext getString(
                            R.string.text_chat_room_service_member_other,
                            "(" + memberIds.size + ")"
                        )
                    }

                    // 個人
                    ChatRoomType.person -> {
                        val selfName = DBManager.getInstance().queryUser(selfUserId).nickName
                        return@withContext selfName
                    }

                    // 多人
                    ChatRoomType.discuss -> {
                        if (it.isCustomName) {
                            return@withContext TextViewHelper.getHandledTitle(
                                it.name,
                                it.chatRoomMember?.size ?: it.memberIds.size,
                                false
                            )
                        } else {
                            return@withContext TextViewHelper.getDiscussTitle(it.chatRoomMember)
                        }
                    }

                    // 群發
                    ChatRoomType.broadcast -> {
                        return@withContext "群發訊息"
                    }

                    // 服務號的服務人員
                    ChatRoomType.serviceONumberStaff -> {
                        return@withContext it.name
                    }
                    // 夥伴聊天室
                    ChatRoomType.friend -> {
                        return@withContext chatViewModel.getFriendAliasName(it)
                    }

                    else -> {
                        if (selfUserId != it.ownerId) {
                            var title = "未知"
                            if (!Strings.isNullOrEmpty(it.name)) {
                                title = it.name
                            } else {
                                val userProfileEntity = DBManager.getInstance().queryFriend(it.ownerId)
                                userProfileEntity?.let {
                                    if (!Strings.isNullOrEmpty(it.name)) {
                                        title = it.name
                                    }
                                }
                            }
                            return@withContext title
                        }
                    }
                }

                return@withContext it.name
            } ?: run {
                return@withContext userName
            }
        }

    @SuppressLint("CommitTransaction")
    private fun addFragment(chatFragment: ChatFragment) =
        CoroutineScope(Dispatchers.Main).launch {
            this@ChatActivity.chatFragment = chatFragment
            chatFragment.setOnChatRoomTitleChangeListener(
                object : OnChatRoomTitleChangeListener {
                    override fun onTitleChangeListener(title: String) {
                        binding.title.text = title
                    }
                }
            )

            val transaction = supportFragmentManager.beginTransaction()
            supportFragmentManager.fragments.forEach {
                if (it is ChatFragment) {
                    supportFragmentManager.beginTransaction().remove(it).commit()
                }
            }
            if (!chatFragment.isAdded && !isFinishing && !isDestroyed) {
                transaction.add(R.id.contentFL, chatFragment, javaClass.simpleName).commit()
            }
        }

    private fun rightViewToggle() {
        chatViewModel.roomEntity?.let {
            if (it.roomType == ChatRoomType.undef) chatViewModel.setRoomType()
            binding.rightAction.setImageResource(R.drawable.icon_aipower_close)
            when (it.roomType) {
                ChatRoomType.person -> showSelfPopupWindow()
                ChatRoomType.system -> showSystemPopupWindow()
                ChatRoomType.discuss -> showDiscussPopupWindow()
                ChatRoomType.friend -> showSinglePopupWindow()
                ChatRoomType.group -> showGroupPopupWindow()
                ChatRoomType.bossSecretary, ChatRoomType.bossOwnerWithSecretary -> showBossServiceMemberPopupWindow()
                ChatRoomType.bossOwner -> showServicePopupWindow()
                ChatRoomType.bossServiceNumber -> showServicePopupWindow()
                ChatRoomType.serviceINumberAsker -> showServiceRoomEmployeePopupWindow()
                ChatRoomType.serviceINumberStaff -> showServiceRoomAgentPopupWindow()
                ChatRoomType.serviceMember -> showServiceMemberRoomPopupWindow()
                ChatRoomType.serviceONumberStaff -> showServiceRoomContactPopupWindow()
                ChatRoomType.provisional -> showServiceRoomAgentPopupWindow()
                ChatRoomType.subscribe -> showServiceRoomAgentPopupWindow()

                else -> {} // nothing
            }
        }
    }

    private fun initPopupWindows() =
        CoroutineScope(Dispatchers.Main).launch {
            val popupBinding = GridViewBinding.inflate(layoutInflater)
            popupBinding.root.setBackgroundColor(if (isGreenTheme && themeStyle != RoomThemeStyle.SERVICES) resources.getColor(R.color.color_015F57, null) else themeStyle.mainColor)
            popupBinding.recyclerView.apply {
                layoutManager = GridLayoutManager(this@ChatActivity, 4)
                addItemDecoration(GridItemDecoration())
                itemAnimator = DefaultItemAnimator()
                setHasFixedSize(true)
                measure(0, 0)
            }
            aiffPopupWindows = PopupWindow(popupBinding.root, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            aiffPopupWindows?.apply {
                setBackgroundDrawable(ColorDrawable(0x10101010))
                isOutsideTouchable = true
                isFocusable = true
                setOnDismissListener {
                    binding.rightAction.setImageResource(R.drawable.icon_aipower_open)
                }
            }
        }

    private fun doShowLoginDeviceSettingBottomSheetDialog(deviceRecordItem: DeviceRecordItem) =
        CoroutineScope(Dispatchers.Main).launch {
            BottomSheetDialogBuilder(this@ChatActivity, layoutInflater)
                .getOnlineDeviceOperation(deviceRecordItem, { isCurrentDevice ->
                    if (isCurrentDevice) {
                        LogoutSmsDialogFragment().show(supportFragmentManager, "Logout")
                    } else {
                        AlertView
                            .Builder()
                            .setContext(this@ChatActivity)
                            .setStyle(AlertView.Style.Alert)
                            .setMessage(getString(R.string.text_device_force_logout_tip))
                            .setOthers(
                                arrayOf(getString(R.string.cancel), getString(R.string.text_for_sure))
                            ).setOnItemClickListener { o, position ->
                                if (position == 1) {
                                    deviceRecordItem.deviceId?.let {
                                        homeViewModel.doForceLogoutDevice(it)
                                    }
                                }
                            }.build()
                            .setCancelable(true)
                            .show()
                    }
                }, {
                    deviceRecordItem.rememberMe?.let {
                        if (it) {
                            // 取消自動登入
                            AlertView
                                .Builder()
                                .setContext(this@ChatActivity)
                                .setStyle(AlertView.Style.Alert)
                                .setMessage(getString(R.string.text_device_cancel_auto_login_tip))
                                .setOthers(
                                    arrayOf(getString(R.string.cancel), getString(R.string.text_for_sure))
                                ).setOnItemClickListener { o, position ->
                                    if (position == 1) {
                                        homeViewModel.doCancelAutoLogin(deviceRecordItem.id)
                                    }
                                }.build()
                                .setCancelable(true)
                                .show()
                        } else {
                            // 允許自動登入
                            AlertView
                                .Builder()
                                .setContext(this@ChatActivity)
                                .setStyle(AlertView.Style.Alert)
                                .setMessage(getString(R.string.text_device_allow_auto_login_tip))
                                .setOthers(
                                    arrayOf(getString(R.string.cancel), getString(R.string.text_for_sure))
                                ).setOnItemClickListener { o, position ->
                                    if (position == 1) {
                                        homeViewModel.doAllowAutoLogin(deviceRecordItem.id)
                                    }
                                }.build()
                                .setCancelable(true)
                                .show()
                        }
                    }
                }, {
                    // 刪除設備
                    AlertView
                        .Builder()
                        .setContext(this@ChatActivity)
                        .setStyle(AlertView.Style.Alert)
                        .setMessage(getString(R.string.text_device_delete_login_device_tip))
                        .setOthers(
                            arrayOf(getString(R.string.cancel), getString(R.string.text_for_sure))
                        ).setOnItemClickListener { o, position ->
                            if (position == 1) {
                                deviceRecordItem.uniqueID?.let {
                                    homeViewModel.doDeleteLoginDevice(it, deviceRecordItem.id, deviceRecordItem.deviceId)
                                }
                            }
                        }.build()
                        .setCancelable(true)
                        .show()
                })
                .show()
        }

    private fun showSelfPopupWindow() =
        CoroutineScope(Dispatchers.IO).launch {
            val aiffList = getAiffList(AiffDisplayLocation.SelfRoom.name)
            val popupWindow = getPopupWindow(aiffList, AiffDisplayLocation.SelfRoom.name)
            withContext(Dispatchers.Main) {
                popupWindow.showAsDropDown(binding.titleBar)
            }
        }

    private fun showSystemPopupWindow() =
        CoroutineScope(Dispatchers.IO).launch {
            val aiffList = getAiffList(AiffDisplayLocation.SystemRoom.name)
            val popupWindow = getPopupWindow(aiffList, AiffDisplayLocation.SystemRoom.name)
            withContext(Dispatchers.Main) {
                popupWindow.showAsDropDown(binding.titleBar)
            }
        }

    private fun showDiscussPopupWindow() =
        CoroutineScope(Dispatchers.IO).launch {
            val aiffList = getAiffList(AiffDisplayLocation.DiscussRoom.name)
            val popupWindow = getPopupWindow(aiffList, AiffDisplayLocation.DiscussRoom.name)
            withContext(Dispatchers.Main) {
                popupWindow.showAsDropDown(binding.titleBar)
            }
        }

    private fun showSinglePopupWindow() =
        CoroutineScope(Dispatchers.IO).launch {
            val aiffList = getAiffList(AiffDisplayLocation.PrivateRoom.name)
            val popupWindow = getPopupWindow(aiffList, AiffDisplayLocation.PrivateRoom.name)
            withContext(Dispatchers.Main) {
                popupWindow.showAsDropDown(binding.titleBar)
            }
        }

    private fun showServiceRoomContactPopupWindow() =
        CoroutineScope(Dispatchers.IO).launch {
            val aiffList = getAiffList(AiffDisplayLocation.ServiceRoomContact.name)
            val popupWindow = getPopupWindow(aiffList, AiffDisplayLocation.ServiceRoomContact.name)
            withContext(Dispatchers.Main) {
                popupWindow.showAsDropDown(binding.titleBar)
            }
        }

    private fun showServiceMemberRoomPopupWindow() =
        CoroutineScope(Dispatchers.IO).launch {
            val aiffList = getAiffList(AiffDisplayLocation.ServiceMemberRoom.name)
            val popupWindow =
                getPopupWindow(
                    aiffList,
                    AiffDisplayLocation.ServiceMemberRoom.name
                )
            withContext(Dispatchers.Main) {
                popupWindow.showAsDropDown(binding.titleBar)
            }
        }

    private fun showServiceRoomEmployeePopupWindow() =
        CoroutineScope(Dispatchers.IO).launch {
            val aiffList = getAiffList(AiffDisplayLocation.ServiceRoomEmployee.name)
            val popupWindow = getPopupWindow(aiffList, AiffDisplayLocation.ServiceRoomEmployee.name, true)
            withContext(Dispatchers.Main) {
                popupWindow.showAsDropDown(binding.titleBar)
            }
        }

    private fun showServiceRoomAgentPopupWindow() =
        CoroutineScope(Dispatchers.IO).launch {
            val aiffList = getAiffList(AiffDisplayLocation.ServiceRoomAgent.name)
            val popupWindow =
                getPopupWindow(
                    aiffList,
                    AiffDisplayLocation.ServiceRoomAgent.name
                )
            withContext(Dispatchers.Main) {
                popupWindow.showAsDropDown(binding.titleBar)
            }
        }

    private fun showBossServiceMemberPopupWindow() =
        CoroutineScope(Dispatchers.IO).launch {
            val aiffList = getAiffList(AiffDisplayLocation.BossServiceMemberRoom.name)
            val popupWindow = getPopupWindow(aiffList, AiffDisplayLocation.BossServiceMemberRoom.name)
            withContext(Dispatchers.Main) {
                popupWindow.showAsDropDown(binding.titleBar)
            }
        }

    /**
     * 群組頂部進階選單
     * group_chat_menu_out
     */
    private fun showGroupPopupWindow() =
        CoroutineScope(Dispatchers.IO).launch {
            chatViewModel.roomEntity?.let {
                val aiffList: MutableList<RichMenuInfo> = Lists.newArrayList()
                var displayLocation: String?
                if (it.ownerId == selfUserId) { // group owner
                    aiffList.addAll(getAiffList(AiffDisplayLocation.GroupRoomOwner.name, AiffDisplayLocation.GroupRoom.name))
                    displayLocation =
                        AiffDisplayLocation.GroupRoomOwner.name + "," + AiffDisplayLocation.GroupRoom.name
                } else { // not group owner
                    aiffList.addAll(getAiffList(AiffDisplayLocation.GroupRoom.name))
                    displayLocation = AiffDisplayLocation.GroupRoom.name
                }

                val popupWindow = getPopupWindow(aiffList, displayLocation)
                withContext(Dispatchers.Main) {
                    popupWindow.showAsDropDown(binding.titleBar)
                }
            }
        }

    // 服務號聊天室進階功能
    private fun showServicePopupWindow() =
        CoroutineScope(Dispatchers.IO).launch {
            val aiffList = getAiffList(AiffDisplayLocation.BossServiceRoom.name)
            val popupWindow =
                getPopupWindow(
                    aiffList,
                    AiffDisplayLocation.BossServiceRoom.name + "," + AiffDisplayLocation.ServiceRoom.name
                )
            withContext(Dispatchers.Main) {
                popupWindow.showAsDropDown(binding.titleBar)
            }
        }

    // 服務號成員聊天室，成員列表
    private fun showSinkingServiceMemberListPopupWindow() =
        CoroutineScope(Dispatchers.Main).launch {
            chatViewModel.roomEntity?.let {
                val gridMemberViewBinding = GridMemberViewBinding.inflate(layoutInflater)
                gridMemberViewBinding.apply {
                    root.setBackgroundColor(if (isGreenTheme && themeStyle != RoomThemeStyle.SERVICES) resources.getColor(R.color.color_015F57, null) else themeStyle.mainColor)
                    recyclerView.apply {
                        layoutManager =
                            LinearLayoutManager(
                                this@ChatActivity,
                                LinearLayoutManager.HORIZONTAL,
                                false
                            )
                        itemAnimator = DefaultItemAnimator()
                        setHasFixedSize(true)
                        val memberList = it.agentsList.toMutableList()
                        memberList.add(0, UserProfileEntity())
                        chatRoomMembersAdapter =
                            ChatRoomMembersAdapter(false)
                                .setData(memberList)
                                .setOnItemClickListener(this@ChatActivity)
                        adapter = chatRoomMembersAdapter
                    }
                    chatRoomMembersAdapter?.refreshData()
                    memberPopupWindow = PopupWindow(root, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    memberPopupWindow?.apply {
                        setBackgroundDrawable(ColorDrawable(0x10101010))
                        isOutsideTouchable = true
                        isFocusable = true
                        setOnDismissListener {
                            binding.rightAction.setImageResource(R.drawable.icon_aipower_open)
                        }
                        showAsDropDown(binding.titleBar)
                    }
                }
            }
        }

    /**
     * 下沈式成員列表
     */

    private fun showSinkingMemberListPopupWindow() =
        CoroutineScope(Dispatchers.Main).launch {
            chatViewModel.roomEntity?.let {
                val gridMemberViewBinding = GridMemberViewBinding.inflate(layoutInflater)
                gridMemberViewBinding.apply {
//                root.setBackgroundColor(themeStyle.mainColor)
                    recyclerView.apply {
                        layoutManager =
                            LinearLayoutManager(
                                this@ChatActivity,
                                LinearLayoutManager.HORIZONTAL,
                                false
                            )
                        itemAnimator = DefaultItemAnimator()
                        setHasFixedSize(true)
                        withContext(Dispatchers.IO) {
                            it.chatRoomMember?.let { chatMember ->
                                var memberList = mutableListOf<UserProfileEntity>()
                                chatMember.forEach { member ->
                                    val userProfileEntity = DBManager.getInstance().queryUser(member.memberId)
                                    userProfileEntity?.let { userProfile ->
                                        if (it.ownerId == member.memberId && it.roomType == ChatRoomType.group) {
                                            userProfile.groupPrivilege = GroupPrivilegeEnum.Owner
                                        } else {
                                            userProfile.groupPrivilege = member.privilege
                                        }
                                        memberList.add(userProfile)
                                    }
                                }

                                // 如果是社團，按照擁有者 -> 管理員 -> 一般成員排序
                                if (it.roomType == ChatRoomType.group) {
                                    memberList = SortUtil.sortGroupOwnerManagerByPrivilege(memberList)
                                }

                                // 主頁
                                memberList.add(0, UserProfileEntity())
                                chatRoomMembersAdapter =
                                    ChatRoomMembersAdapter(false)
                                        .setData(memberList)
                                        .setOnItemClickListener(this@ChatActivity)
                            }
                        }
                        adapter = chatRoomMembersAdapter
                        chatRoomMembersAdapter?.refreshData()
                    }
                }
                memberPopupWindow = PopupWindow(gridMemberViewBinding.root, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                memberPopupWindow?.apply {
                    setBackgroundDrawable(ColorDrawable(0x10101010))
                    isOutsideTouchable = false
                    isFocusable = true
                    showAsDropDown(binding.titleBar)
                    setOnDismissListener {
                        binding.rightAction.setImageResource(R.drawable.icon_aipower_open)
                        ViewCompat
                            .animate(binding.rightAction)
                            .rotation(0f)
                            .setDuration(1)
                            .start()
                    }
                }
            }
        }

    private suspend fun getPopupWindow(
        aiffList: List<RichMenuInfo>,
        displayLocation: String
    ): PopupWindow =
        getPopupWindow(
            aiffList,
            displayLocation,
            chatViewModel.roomEntity!!.provisionalIds.contains(selfUserId) && chatViewModel.roomEntity!!.listClassify == ChatRoomSource.MAIN
        )

    /**
     * 右上下拉選單
     * @param isEmployee 判斷內部服務號 是否是需要諮詢的員工 或是 服務人員
     */
    private suspend fun getPopupWindow(
        aiffList: List<RichMenuInfo>,
        displayLocation: String,
        isEmployee: Boolean
    ): PopupWindow =
        withContext(Dispatchers.Main) {
            val popupWindowBinding = GridViewBinding.inflate(layoutInflater)
            chatViewModel.roomEntity?.let {
                popupWindowBinding.root.setBackgroundColor(if (isGreenTheme && themeStyle != RoomThemeStyle.SERVICES) resources.getColor(R.color.color_015F57, null) else themeStyle.mainColor)
            } ?: run {
                popupWindowBinding.root.setBackgroundColor(themeStyle.mainColor)
            }
            val popupWindow =
                PopupWindow(
                    popupWindowBinding.root,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            if (aiffList.isEmpty()) {
                popupWindowBinding.tvMore.visibility = View.GONE
            } else {
                popupWindowBinding.tvMore.visibility = View.VISIBLE
            }

            // rich menu
            val richMenuInfoList: MutableList<RichMenuInfo> = getRichMenuList(isEmployee)
            richMenuInfoList.addAll(aiffList)
            val richMenuAdapter =
                RichMenuAdapter(
                    if (richMenuInfoList.size >= 16) {
                        richMenuInfoList.subList(
                            0,
                            16
                        )
                    } else {
                        richMenuInfoList
                    }
                )

            richMenuAdapter.setOnItemClickListener { adapter: BaseQuickAdapter<*, *>, view: View?, position: Int ->
                popupWindow.dismiss()
                val info = adapter.getItem(position) as RichMenuInfo
                onRichMenuItemClick(info)
            }

            popupWindowBinding.tvMore.setOnClickListener {
                popupWindow.dismiss()
                aiffManager?.showAiffList(aiffList)
            }

            // recyclerView
            val gridLayoutManager = GridLayoutManager(this@ChatActivity, 4)
            popupWindowBinding.recyclerView.apply {
                layoutManager = gridLayoutManager
                addItemDecoration(GridItemDecoration())
                itemAnimator = DefaultItemAnimator()
                setHasFixedSize(true)
                adapter = richMenuAdapter
                measure(0, 0)
            }

            // popupWindows setting
            popupWindow.apply {
                setBackgroundDrawable(ColorDrawable(0x10101010))
                isOutsideTouchable = true
                isFocusable = true
                setOnDismissListener {
                    binding.rightAction.setImageResource(
                        R.drawable.icon_aipower_open
                    )
                }
            }

            return@withContext popupWindow
        }

    private suspend fun getAiffList(vararg displayLocation: String): List<RichMenuInfo> =
        withContext(Dispatchers.IO) {
            val aiffInfoList = AiffDB.getInstance(this@ChatActivity).aiffInfoDao.aiffInfoListByIndex
            val aiffSet = LinkedHashSet<RichMenuInfo>()
            aiffInfoList.forEach {
                if (it.embedLocation != AiffEmbedLocation.ChatRoomMenu.name) return@forEach
                displayLocation.forEach { location ->
                    it.displayLocation?.let { displayLocation ->
                        if (displayLocation.contains(location)) {
                            val info =
                                RichMenuInfo(
                                    RichMenuInfo.MenuType.AIFF.type,
                                    it.id,
                                    it.pictureId,
                                    it.title,
                                    it.name,
                                    it.pinTimestamp,
                                    it.useTimestamp
                                )
                            aiffSet.add(info)
                        }
                    } ?: run {
                        // 支援指定服務號處理
                        if (it.incomingAiff.equals("serviceNumber")) {
                            it.serviceNumberIds?.let { ids ->
                                chatViewModel.roomEntity?.let { entity ->
                                    if (ids.contains(entity.serviceNumberId)) {
                                        when (it.userType_APP) {
                                            User.Type.EMPLOYEE -> { // Aiff對象∶服務方使用
                                                if (entity.ownerId != selfUserId) {
                                                    val info =
                                                        RichMenuInfo(
                                                            RichMenuInfo.MenuType.AIFF.type,
                                                            it.id,
                                                            it.pictureId,
                                                            it.title,
                                                            it.name,
                                                            it.pinTimestamp,
                                                            it.useTimestamp
                                                        )
                                                    aiffSet.add(info)
                                                }
                                            }

                                            User.Type.CONTACT -> { // Aiff對象∶客戶方使用
                                                if (entity.ownerId == selfUserId) {
                                                    val info =
                                                        RichMenuInfo(
                                                            RichMenuInfo.MenuType.AIFF.type,
                                                            it.id,
                                                            it.pictureId,
                                                            it.title,
                                                            it.name,
                                                            it.pinTimestamp,
                                                            it.useTimestamp
                                                        )
                                                    aiffSet.add(info)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            val sortList = aiffSet.toMutableList()
            sortList.sort()
            return@withContext sortList
        }

    private suspend fun getRichMenuList(isEmployee: Boolean): MutableList<RichMenuInfo> =
        withContext(Dispatchers.IO) {
            val richMenuInfoList = Lists.newArrayList<RichMenuInfo>()
            chatViewModel.roomEntity?.let {
                var richMenuInfo1: RichMenuInfo? = null
                var richMenuInfo2: RichMenuInfo? = null
                var richMenuInfo3: RichMenuInfo? = null
                richMenuInfoList.addAll(
                    RichMenuInfo.getServiceNumberChatRoomTopRichMenus(
                        it.isMute(),
                        it.type == ChatRoomType.services,
                        isEmployee || it.type != ChatRoomType.services
                    )
                )
                when (it.roomType) {
                    ChatRoomType.group -> {
                        // 是群主
                        if (it.ownerId == selfUserId) {
                            richMenuInfo1 =
                                RichMenuInfo(
                                    RichMenuInfo.MenuType.FIXED.type,
                                    RichMenuInfo.FixedMenuId.DISMISS_CROWD,
                                    R.drawable.ic_quit,
                                    R.string.base_top_rich_menu_dismiss_crowd,
                                    true
                                )
                        } else {
                            richMenuInfo1 =
                                RichMenuInfo(
                                    RichMenuInfo.MenuType.FIXED.type,
                                    RichMenuInfo.FixedMenuId.EXIT_CROWD,
                                    R.drawable.ic_quit,
                                    R.string.base_top_rich_menu_exit_crowd,
                                    true
                                )
                        }
                        richMenuInfo2 =
                            RichMenuInfo(
                                RichMenuInfo.MenuType.FIXED.type,
                                RichMenuInfo.FixedMenuId.MAIN_PAGE,
                                R.drawable.ic_home_page,
                                R.string.text_home_page,
                                true
                            )
                        richMenuInfoList.add(richMenuInfo2)
                        richMenuInfoList.add(richMenuInfo1)
                    }

                    ChatRoomType.discuss -> {
                        richMenuInfo1 =
                            RichMenuInfo(
                                RichMenuInfo.MenuType.FIXED.type,
                                RichMenuInfo.FixedMenuId.UPGRADE,
                                R.drawable.new_group,
                                R.string.text_transfer_to_crowd,
                                true
                            )
                        richMenuInfo2 =
                            RichMenuInfo(
                                RichMenuInfo.MenuType.FIXED.type,
                                RichMenuInfo.FixedMenuId.EXIT_DISCUSS,
                                R.drawable.ic_quit,
                                R.string.base_top_rich_menu_exit_discuss,
                                true
                            )
                        richMenuInfo3 =
                            RichMenuInfo(
                                RichMenuInfo.MenuType.FIXED.type,
                                RichMenuInfo.FixedMenuId.MAIN_PAGE,
                                R.drawable.ic_home_page,
                                R.string.text_home_page,
                                true
                            )
                        richMenuInfoList.add(richMenuInfo1)
                        richMenuInfoList.add(richMenuInfo3)
                        richMenuInfoList.add(richMenuInfo2)
                    }

                    else -> {} // nothing
                }
            }
            return@withContext richMenuInfoList
        }

    private fun onRichMenuItemClick(info: RichMenuInfo) =
        CoroutineScope(Dispatchers.IO).launch {
            if (info.type == RichMenuInfo.MenuType.FIXED.type) {
                when (info.menuId) {
                    RichMenuInfo.FixedMenuId.SEARCH -> doSearchAction()
                    RichMenuInfo.FixedMenuId.AMPLIFICATION, RichMenuInfo.FixedMenuId.MUTE -> {
                        chatViewModel.roomEntity?.let {
                            chatViewModel.changeMute(it.isMute, it.id)
                        }
                        // ChatRoomService.getInstance().changeMute
                    }

                    // 升級成為社團
                    RichMenuInfo.FixedMenuId.UPGRADE -> {
                        upgradeToGroup()
                    }

                    // 退出聊天室
                    RichMenuInfo.FixedMenuId.DISMISS_CROWD,
                    RichMenuInfo.FixedMenuId.EXIT_CROWD,
                    RichMenuInfo.FixedMenuId.EXIT_DISCUSS -> {
                        showExitChatRoom()
                    }

                    RichMenuInfo.FixedMenuId.NEW_MEMBER -> {
                        addProvisionalMember()
                    }

                    RichMenuInfo.FixedMenuId.MAIN_PAGE -> {
                        toMainPage()
                    }

                    // 記事
                    RichMenuInfo.FixedMenuId.TODO -> {
                        chatViewModel.roomEntity?.let {
                            IntentUtil.startIntent(
                                this@ChatActivity,
                                ChatRoomFilterActivity::class.java,
                                bundleOf(
                                    BundleKey.INTENT_TO_TODO.key() to true,
                                    BundleKey.ROOM_ID.key() to it.id
                                )
                            )
                        }
                    }

                    // 篩選
                    RichMenuInfo.FixedMenuId.FILTER -> {
                        chatViewModel.roomEntity?.let {
                            val bundle =
                                bundleOf(
                                    BundleKey.ROOM_ID.key() to it.id
                                )
                            IntentUtil.startIntent(this@ChatActivity, ChatRoomFilterActivity::class.java, bundle)
                        }
                    }

                    else -> {} // nothing
                }
            } else if (info.type == RichMenuInfo.MenuType.AIFF.type) {
                val aiffInfo = AiffDB.getInstance(this@ChatActivity).aiffInfoDao.getAiffInfo(info.id)
                aiffManager?.showAiffViewByInfo(aiffInfo)
                aiffInfo.useTimestamp = System.currentTimeMillis()
                AiffDB.getInstance(this@ChatActivity).aiffInfoDao.upsert(aiffInfo)
            }
        }

    private fun upgradeToGroup() =
        CoroutineScope(Dispatchers.IO).launch {
            chatViewModel.roomEntity?.let {
                val bundle =
                    bundleOf(
                        BundleKey.ROOM_ID.key() to it.id,
                        BundleKey.MEMBERS_LIST.key() to it.chatRoomMember.map { it.memberId }
                    )
                if (it.isCustomName) {
                    bundle.putString(BundleKey.CHAT_ROOM_NAME.key(), it.name)
                }
                IntentUtil.startIntent(this@ChatActivity, CreateGroupActivity::class.java, bundle)
            }
        }

    private fun showExitChatRoom() =
        CoroutineScope(Dispatchers.IO).launch {
            chatViewModel.roomEntity?.let {
                val message =
                    if (it.ownerId == selfUserId && it.roomType == ChatRoomType.group) {
                        "您是否要解散社團？"
                    } else if (it.roomType == ChatRoomType.group) {
                        "是否退出社團？"
                    } else {
                        "退出後將清除聊天室及聊天室紀錄，您確定要退出嗎？"
                    }
                val title = if (it.roomType != ChatRoomType.group) "退出聊天室" else ""
                withContext(Dispatchers.Main) {
                    AlertView
                        .Builder()
                        .setContext(this@ChatActivity)
                        .setStyle(AlertView.Style.Alert)
                        .setTitle(title)
                        .setMessage(message)
                        .setOthers(
                            arrayOf("取消", "確定")
                        ).setOnItemClickListener { o, position ->
                            if (position == 1) {
                                if (it.ownerId == selfUserId && it.roomType == ChatRoomType.group) {
                                    dismissGroup()
                                } else {
                                    quitGroup()
                                }
                            }
                        }.build()
                        .setCancelable(true)
                        .show()
                }
            }
        }

    private fun showProvisionalToServiceNumber(chatRoomEntity: ChatRoomEntity) =
        CoroutineScope(Dispatchers.Main).launch {
            AlertView
                .Builder()
                .setContext(this@ChatActivity)
                .setStyle(AlertView.Style.Alert)
                .setTitle("您已成為服務號成員")
                .setMessage("此服務號管理者已將您加入到服務號成員的行列之中")
                .setOthers(
                    arrayOf(
                        getString(R.string.text_for_sure)
                    )
                ).setOnItemClickListener { o, position ->
                    val bundle =
                        bundleOf(
                            BundleKey.EXTRA_SESSION_ID.key() to chatRoomEntity.id
                        )
                    IntentUtil.startIntent(this@ChatActivity, ChatActivity::class.java, bundle)
                    finish()
                }.build()
                .setCancelable(true)
                .show()
        }

    private fun dismissGroup() =
        CoroutineScope(Dispatchers.IO).launch {
            chatViewModel.roomEntity?.let {
                if (selfUserId == it.ownerId) {
                    chatViewModel.roomDismiss(it.id)
                }
            }
        }

    private fun quitGroup() =
        CoroutineScope(Dispatchers.IO).launch {
            chatViewModel.roomEntity?.let {
                chatViewModel.chatMemberExit(it.id, it.ownerId)
            }
        }

    private fun addProvisionalMember() =
        CoroutineScope(Dispatchers.IO).launch {
            chatViewModel.roomEntity?.let {
                val bundle =
                    bundleOf(
                        BundleKey.ROOM_ID.key() to it.id,
                        BundleKey.SERVICE_NUMBER_ID.key() to it.serviceNumberId,
                        BundleKey.SUBSCRIBE_AGENT_ID.key() to it.serviceNumberAgentId,
                        BundleKey.ROOM_TYPE.key() to InvitationType.ProvisionalMember.name,
                        BundleKey.PROVISIONAL_MEMBER_IDS.key() to it.provisionalIds
                    )
                IntentUtil.launchIntent(this@ChatActivity, MemberInvitationActivity::class.java, addProvisionalMemberARL, bundle)
            }
        }

    private fun toMainPage() =
        CoroutineScope(Dispatchers.IO).launch {
            chatViewModel.roomEntity?.let {
                val bundle =
                    bundleOf(
                        BundleKey.ROOM_ID.key() to it.id,
                        BundleKey.ROOM_TYPE.key() to it.type.name
                    )
                if (NetworkUtils.isNetworkAvailable(this@ChatActivity)) {
                    IntentUtil.startIntent(this@ChatActivity, MainPageActivity::class.java, bundle)
                }
            }
        }

    private fun toServiceNumberHomePage() =
        CoroutineScope(Dispatchers.IO).launch {
            chatViewModel.roomEntity?.let {
                val bundle =
                    bundleOf(
                        BundleKey.SERVICE_NUMBER_ID.key() to it.serviceNumberId
                    )
                IntentUtil.startIntent(this@ChatActivity, ServiceNumberManageActivity::class.java, bundle)
            }
        }

    private fun doSearchAction() =
        CoroutineScope(Dispatchers.Main).launch {
            binding.etSearch.requestFocus()
            KeyboardHelper.open(binding.etSearch)
            chatFragment?.doSearchAction(binding.searchBar, binding.etSearch, binding.clearInput)
        }

    private fun setUnreadCount(
        count: Int,
        roomId: String = ""
    ) = CoroutineScope(Dispatchers.IO).launch {
        chatViewModel.roomEntity?.let {
            if (it.id == roomId || roomId.isEmpty()) {
                return@launch
            }
            setUnreadCount(count)
        }
    }

    private fun setUnreadCount(count: Int) =
        CoroutineScope(Dispatchers.Main).launch {
            if (count > 0) {
                binding.unreadNum.text = getUnreadText(count)
                binding.unreadNum.visibility = View.VISIBLE
            } else {
                binding.unreadNum.visibility = View.GONE
            }
        }

    private fun addMember() =
        CoroutineScope(Dispatchers.IO).launch {
            chatViewModel.roomEntity?.let {
                var bundle: Bundle? = null
                it.chatRoomMember?.let { chatMember ->
                    val chatRoomMemberIds = chatMember.map { member -> member.memberId }
                    bundle =
                        bundleOf(
                            BundleKey.ROOM_ID.key() to it.id,
                            BundleKey.ACCOUNT_IDS.key() to chatRoomMemberIds,
                            BundleKey.ROOM_TYPE.key() to InvitationType.GroupRoom.name
                        )
                } ?: run {
                    bundle =
                        bundleOf(
                            BundleKey.ROOM_ID.key() to it.id,
                            BundleKey.ACCOUNT_IDS.key() to it.memberIds,
                            BundleKey.ROOM_TYPE.key() to InvitationType.GroupRoom.name
                        )
                }
                IntentUtil.launchIntent(this@ChatActivity, MemberInvitationActivity::class.java, addMemberARL, bundle)
            }
        }

    private fun onInviteSuccess(userList: List<UserProfileEntity>) =
        CoroutineScope(Dispatchers.IO).launch {
            chatViewModel.roomEntity?.let {
                if (it.roomType != ChatRoomType.group) {
                    it.members.addAll(userList)
                    it.memberIds.addAll(userList.map { it.id })
                }
                setTitleBar()
//            chatViewModel.updateSession(it.id)
                chatRoomMembersAdapter?.refreshData()
                CoroutineScope(Dispatchers.Main).launch {
                    ToastUtils.showToast(this@ChatActivity, getString(R.string.text_invite_member_success))
                }
            }
        }

    // 判斷是否有 aiff主頁
    // todo CoroutineScope
    fun checkClientMainPageFromAiff(): Boolean {
        val aiffDao = AiffDB.getInstance(this@ChatActivity).aiffInfoDao
        val aiffInfoList = aiffDao.aiffInfoListByIndex
        if (aiffInfoList != null && aiffInfoList.size > 0) {
            aiffInfoList.forEach {
                if (it.embedLocation == AiffEmbedLocation.ContactHome.name) {
                    val aiffInfo = aiffDao.getAiffInfo(it.id)
                    aiffManager?.showAiffViewByInfo(aiffInfo)
                    return true
                }
            }
        }
        return false
    }

    fun toChatRoomByUserProfile(userProfileEntity: UserProfileEntity?) =
        CoroutineScope(Dispatchers.IO).launch {
            userProfileEntity?.let { userProfileEntity ->
                if (userProfileEntity.id != selfUserId) {
                    userProfileEntity.roomId?.let {
                        if (userProfileEntity.roomId.isNotEmpty()) {
                            val bundle =
                                bundleOf(
                                    BundleKey.EXTRA_SESSION_ID.key() to it
                                )
                            IntentUtil.startIntent(this@ChatActivity, ChatActivity::class.java, bundle)
                        } else {
                            doCreateNewChatRoom(userProfileEntity)
                        }
                    } ?: run {
                        doCreateNewChatRoom(userProfileEntity)
                    }
                } else {
                    val bundle =
                        bundleOf(
                            BundleKey.ACCOUNT_TYPE.key() to UserType.EMPLOYEE.name,
                            BundleKey.ACCOUNT_ID.key() to selfUserId,
                            BundleKey.WHERE_COME.key() to javaClass.simpleName
                        )
                    IntentUtil.startIntent(this@ChatActivity, SelfInformationHomepageActivity::class.java, bundle)
                }
            }
            withContext(Dispatchers.Main) {
                memberPopupWindow?.dismiss()
            }
        }

    private fun doCreateNewChatRoom(userProfileEntity: UserProfileEntity) {
        val bundle =
            bundleOf(
                BundleKey.USER_NICKNAME.key() to userProfileEntity.nickName,
                BundleKey.USER_ID.key() to userProfileEntity.id
            )
        IntentUtil.startIntent(this@ChatActivity, ChatActivity::class.java, bundle)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        ev?.let {
            if (it.action == MotionEvent.ACTION_UP) {
                chatFragment?.onTouchEvent(ev)
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onKeyDown(
        keyCode: Int,
        event: KeyEvent?
    ): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 如果是開搜索 關閉搜索
            if (binding.searchBar.visibility == View.VISIBLE) {
                doSearchCancelAction()
                return true
            }

            // 如果是開主題聊天室(子聊天室) 關閉
            chatFragment?.let {
                if (it.isFloatViewOpenAndExecuteClose) {
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        chatViewModel.roomEntity?.let {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            UserPref.getInstance(this).removeCurrentRoomId(it.id)
            addMemberARL?.unregister()
            addProvisionalMemberARL?.unregister()
            updateGroupARL?.unregister()
            toGroupSessionARL?.unregister()
            it.lastMessage?.let { lastMsg ->
                it.updateTime = lastMsg.sendTime
            }
            if (isNeedRefreshList) {
                // 當設成已讀後，在離開聊天室後刷新列表
                EventBusUtils.sendEvent(EventMsg<Any>(MsgConstant.NOTICE_REFRESH_CHAT_ROOM_LIST))
            } else {
                EventBusUtils.sendEvent(EventMsg<Any>(MsgConstant.NOTICE_REFRESH_CHAT_ROOM_LIST))
                if (it.listClassify == ChatRoomSource.MAIN) {
                    // 重新整理一般聊天列表下方tab未讀數
                    EventBusUtils.sendEvent(EventMsg<Any>(MsgConstant.UPDATE_MAIN_BADGE_NUMBER_EVENT))
                }
            }

            when (it.roomType) {
                ChatRoomType.serviceMember,
                ChatRoomType.bossServiceNumber,
                ChatRoomType.services,
                ChatRoomType.serviceINumberStaff,
                ChatRoomType.serviceONumberStaff,
                ChatRoomType.provisional,
                ChatRoomType.bossSecretary -> {
                    themeStyle = RoomThemeStyle.SERVICES
                    ThemeHelper.updateServiceChatRoomTheme(isServiceRoom = true)
                }

                ChatRoomType.bossOwnerWithSecretary,
                ChatRoomType.bossOwner,
                ChatRoomType.group,
                ChatRoomType.friend,
                ChatRoomType.person,
                ChatRoomType.discuss,
                ChatRoomType.system -> {
                    themeStyle = RoomThemeStyle.FRIEND
                    ThemeHelper.updateServiceChatRoomTheme(isServiceRoom = false)
                }

                else -> {
                    if (!Strings.isNullOrEmpty(it.businessId)) {
                        themeStyle = RoomThemeStyle.BUSINESS
                    }
                    ThemeHelper.updateServiceChatRoomTheme(isServiceRoom = false)
                }
            }
        }
        super.onDestroy()
    }

    private fun clearAtMessage() {
//        chatViewModel.roomEntity?.let {
//            chatViewModel.clearIsAtMeFlag(it.id)
//        }
    }

    // 物件聊天室
//    private fun showSinkingBusinessMemberListPopupWindow(businessRoomList: List<ChatRoomEntity>) = CoroutineScope(Dispatchers.IO).launch{
//        val businessViewBinding = layoutInflater.inflate(R.layout.popup_business_room_list, null, false)
//        chatRoomEntity?.let {
//            BusinessService.getBusinessItem(this@ChatActivity, it.id, it.businessId, object : ServiceCallBack<BusinessItem, RefreshSource> {
//                override fun error(message: String?) {
//                    // todo
//                    // addBusinessCard(false, contentView, themeStyle, null)
//                }
//
//                override fun complete(t: BusinessEntity, e: RefreshSource?) {
//                    // todo
//                    // addBusinessCard(false, contentView, themeStyle, null)
//                }
//            })
//        }
//    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public fun handleEvent(event: EventMsg<Any>) {
        when (event.code) {
            MsgConstant.NOTICE_SELF_EXIT_ROOM -> {
                val roomId = event.string
                chatViewModel.roomEntity?.let {
                    if (it.id == roomId) {
                        finish()
                    }
                }
            }

            MsgConstant.NOTICE_CLOSE_OLD_ROOM -> finish()

            MsgConstant.UPDATE_MAIN_BADGE_NUMBER_EVENT,
            MsgConstant.UPDATE_SERVICE_BADGE_NUMBER_EVENT -> {
                if (event.data is BadgeDataModel) {
                    val badgeDataModel = event.data as BadgeDataModel
                    setUnreadCount(badgeDataModel.unReadNumber, badgeDataModel.roomId)
                }
            }

            MsgConstant.GROUP_UPGRADE_FILTER -> {
                // 修改群聊名称
                val groupUpgradeBean = event.data as GroupUpgradeBean
                val title = groupUpgradeBean.title
                val sessionId = groupUpgradeBean.sessionId
                chatViewModel.roomEntity?.let {
                    if (sessionId.isNullOrEmpty()) return
                    if (title.isNullOrEmpty()) return
                    if (it.id == sessionId) {
                        chatViewModel.upgradeToGroup(it.id)
                    }
                }
            }
            // 多人聊天室被改名
            MsgConstant.NOTICE_DISCUSS_ROOM_TITLE_UPDATE -> {
                val roomId = event.data.toString()
                if (roomId == chatViewModel.roomEntity?.id) {
                    chatViewModel.getChatRoomEntity(roomId)
                }
            }

            MsgConstant.CHAT_TITLE_FILTER -> {
                val title = event.data.toString()
                binding.title.text = title
            }

            MsgConstant.ACCOUNT_REFRESH_FILTER -> {
                val userProfile = event.data as UserProfileEntity
                chatViewModel.roomEntity?.let {
                    val memberIds = AccountRoomRelReference.findMemberIdsByRoomId(null, it.id)
                    if (memberIds.contains(userProfile.id)) {
                        setTitleBar()
                        chatFragment?.updateAccountForMessage(userProfile)
                    }
                }
            }

            MsgConstant.NOTICE_FINISH_ACTIVITY -> {
                finish()
                overridePendingTransition(0, 0)
            }

            MsgConstant.NOTICE_KEEP_SCREEN_ON -> {
                runOnUiThread {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }
            // 臨時成員轉成服務人員
            MsgConstant.NOTICE_SERVICE_NUMBER_ADD_FROM_PROVISIONAL -> {
                val serviceNumberAddMode = JsonHelper.getInstance().from(event.data.toString(), ServiceNumberAddModel::class.java)
                chatViewModel.roomEntity?.let {
                    // 先判斷是否是同聊天室
                    if (serviceNumberAddMode.roomId != it.id) return
                    // 判斷是否是同服務號
                    if (serviceNumberAddMode.serviceNumberId != it.serviceNumberId) return
                    // 再判斷當前的服務人員是否是臨時成員
                    if (serviceNumberAddMode.memberIds.contains(selfUserId)) {
                        chatViewModel.serviceNumberAddFromProvisional(serviceNumberAddMode.roomId)
                    } else {
                        serviceNumberAddMode.memberIds.forEach {
                            if (chatFragment is ChatFragment) {
                                (chatFragment as ChatFragment).onRemoveMember(it)
                            }
                        }
                    }
                }
            }
            // 多人聊天室成員離開 更新下拉成員選單及 title
            MsgConstant.NOTICE_DISCUSS_MEMBER_EXIT -> {
                val memberExitSocket = JsonHelper.getInstance().from(event.data, DiscussMemberSocket::class.java)
                chatViewModel.roomEntity?.let {
                    if (memberExitSocket.roomId == it.id) {
                        chatViewModel.setDiscussRoomTitleWhenMemberRemoved(it, Lists.newArrayList(memberExitSocket.userId))
                    }
                }
            }

            // 多人聊天室 成員加入
            MsgConstant.NOTICE_DISCUSS_MEMBER_ADD,
            // 社團/服務號臨時成员加入
            MsgConstant.GROUP_REFRESH_FILTER -> {
                event.data?.let {
                    chatViewModel.roomEntity?.let { chatRoomEntity ->
                        var isDelete = false
                        val roomId =
                            try {
                                // 邀請成員
                                val newMemberData = JsonHelper.getInstance().fromToMap<String, Any>(it.toString())
                                newMemberData["roomId"].toString()
                            } catch (e: Exception) {
                                // 成員退出
                                val newMemberData = JsonHelper.getInstance().from(it.toString(), GroupRefreshBean::class.java)
                                isDelete = true
                                newMemberData.sessionId
                            }

                        if (roomId == chatRoomEntity.id) {
                            chatViewModel.doHandleMemberFromDB(roomId = roomId, isMemberRemoved = isDelete)
                        }
                    }
                }
            }

            // 社團有人主動退出
            MsgConstant.USER_EXIT -> {
                val userExitBean = event.data as UserExitBean
                chatViewModel.roomEntity?.let { chatRoomEntity ->
                    if (userExitBean.roomId.equals(chatRoomEntity.id)) {
                        chatViewModel.doHandleMemberFromDB(Lists.newArrayList(userExitBean.userId), chatRoomEntity.id, true)
                    }
                }
            }

            // 多人聊天室or社團成員被剔除
            MsgConstant.NOTICE_DISCUSS_GROUP_MEMBER_REMOVED -> {
                val data = JsonHelper.getInstance().fromToMap<String, Any>(event.data.toString())
                chatViewModel.roomEntity?.let { chatRoomEntity ->
                    if (data["roomId"] == chatRoomEntity.id) {
                        val deletedMemberIds = data["deletedMemberIds"] as List<String>?
                        deletedMemberIds?.let {
                            if (it.contains(selfUserId)) {
                                chatViewModel.doKickOutChatRoomByOtherMember(chatRoomEntity.id)
                            } else {
                                chatViewModel.doHandleMemberFromDB(it, chatRoomEntity.id, true)
                            }
                        }
                    }
                }
            }
            // 多人聊天室成員更名
            MsgConstant.NOTICE_DISCUSS_GROUP_USER_PROFILE_CHANGED -> {
                val userProfile = JsonHelper.getInstance().fromToMap<String, Any>(event.data.toString())
                chatViewModel.roomEntity?.let { chatRoomEntity ->
                    val userId = userProfile["userId"].toString()
                    if (chatRoomEntity.chatRoomMember.any { it.memberId == userId }) {
                        chatViewModel.doUpdateRoomTitle(chatRoomEntity.id, userId, userProfile["nickName"].toString())
                    }
                }
            }

            MsgConstant.REFRESH_CUSTOMER_NAME -> {
                chatViewModel.roomEntity?.let {
                    val roomIdList = event.data as List<String>
                    if (roomIdList.contains(it.id)) {
                        chatViewModel.getRoomItem(it.id)
                    }
                }
            }

            MsgConstant.Do_UPDATE_CONTACT_BY_LOCAL -> { // 夥伴聊天室名稱更新
                val profileId = event.data as String
                chatViewModel.roomEntity?.let {
                    if (it.type == ChatRoomType.friend) {
                        binding.title.text = chatViewModel.updateContactPersonChatRoomTitle(it, profileId)
                    }
                }
            }

            MsgConstant.MEMBER_EXIT_DISMISS_ROOM -> { // 多人聊天室退出聊天
                val roomId = event.data as String
                if (roomId == chatViewModel.roomEntity?.id) {
                    finish()
                }
            }

            MsgConstant.DESKTOP_LOGIN_SUCCESS -> {
                if (chatViewModel.roomEntity?.type == ChatRoomType.person) {
                    homeViewModel.getLoginDevicesList()
                }
            }

            MsgConstant.NOTICE_DISMISS_DISCUSS_ROOM -> {
                val roomId = event.data as String
                if (roomId == chatViewModel.roomEntity?.id) {
                    finish()
                }
            }
        }
    }

    fun disableInvite() {
        binding.inviteIV.visibility = View.GONE
    }

    fun triggerToolbarClick() {
        binding.title.performClick()
    }

    fun showToolBar(show: Boolean) {
        binding.titleBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    fun navigateToSubscribePage() {
        chatViewModel.roomEntity?.let {
            val bundle =
                bundleOf(
                    BundleKey.SUBSCRIBE_NUMBER_ID.key() to it.serviceNumberId,
                    BundleKey.ROOM_ID.key() to it.id,
                    BundleKey.IS_SUBSCRIBE.key() to true,
                    BundleKey.WHERE_COME.key() to javaClass.simpleName
                )
            IntentUtil.startIntent(this@ChatActivity, SubscribeInformationHomepageActivity::class.java, bundle)
            overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out)
        }
    }

    private fun setRightCancelView() =
        CoroutineScope(Dispatchers.Main).launch {
            rightCancelTV = TextView(this@ChatActivity)
            rightCancelTV?.apply {
                text = "取消"
                textSize = 16f
                setPadding(10, 0, 10, 0)
                setTextColor(Color.parseColor("#ffffff"))
            }
        }

    fun setRightView() {
        rightCancelTV ?: setRightCancelView()
        binding.rightAction.setOnClickListener {
            chatFragment?.hideChecked()
        }
    }

    fun setChannelIconVisibility(
        @DrawableRes resId: Int,
        status: ServiceNumberStatus
    ) = CoroutineScope(Dispatchers.Main).launch {
        binding.ivChannel.setImageResource(resId)
        binding.ivChannel.visibility = if (status == ServiceNumberStatus.ON_LINE) View.GONE else View.VISIBLE
    }

    fun showTopMenu(show: Boolean) =
        CoroutineScope(Dispatchers.Main).launch {
            binding.llRight.visibility = if (show) View.VISIBLE else View.GONE
        }

    fun doSearchCancelAction() =
        CoroutineScope(Dispatchers.Main).launch {
            KeyboardHelper.hide(binding.searchCancelTV)
            binding.searchBar.visibility = View.GONE
            binding.etSearch.text?.clear()
            chatFragment?.doSearchCancelAction()
        }

    fun refreshPager(chatRoomEntity: ChatRoomEntity) =
        CoroutineScope(Dispatchers.IO).launch {
            chatViewModel.roomEntity = chatRoomEntity
            setTitleBar()
            ChatRoomReference.getInstance().save(chatRoomEntity)
        }

    // 成員下拉點擊
    override fun onItemClick(
        v: View?,
        account: UserProfileEntity?,
        position: Int
    ) {
        account?.let {
            it.id?.let {
                toChatRoomByUserProfile(account)
            } ?: run {
                when (chatViewModel.roomEntity?.type) {
                    ChatRoomType.serviceMember -> toServiceNumberHomePage() // 服務號主頁
                    else -> toMainPage() // 聊天室主頁
                }
            }
        }
    }
}
