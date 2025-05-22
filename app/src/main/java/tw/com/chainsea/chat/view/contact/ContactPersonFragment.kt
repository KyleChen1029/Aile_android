package tw.com.chainsea.chat.view.contact

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import tw.com.chainsea.android.common.json.JsonHelper
import tw.com.chainsea.ce.sdk.bean.CustomerEntity
import tw.com.chainsea.ce.sdk.bean.GroupEntity
import tw.com.chainsea.ce.sdk.bean.ServiceNum
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity
import tw.com.chainsea.ce.sdk.bean.account.UserType
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType
import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberType
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity
import tw.com.chainsea.ce.sdk.bean.servicenumber.type.GroupPrivilegeEnum
import tw.com.chainsea.ce.sdk.database.DBManager
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.event.EventMsg
import tw.com.chainsea.ce.sdk.event.MsgConstant
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference
import tw.com.chainsea.ce.sdk.reference.ServiceNumberReference
import tw.com.chainsea.ce.sdk.service.type.RefreshSource
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.config.BundleKey
import tw.com.chainsea.chat.databinding.FragmentContactPerson2Binding
import tw.com.chainsea.chat.lib.ToastUtils
import tw.com.chainsea.chat.mainpage.view.MainPageActivity
import tw.com.chainsea.chat.network.contact.ContactPersonViewModel
import tw.com.chainsea.chat.network.contact.ViewModelFactory
import tw.com.chainsea.chat.searchfilter.view.activity.CreateGroupActivity
import tw.com.chainsea.chat.ui.activity.ChatActivity
import tw.com.chainsea.chat.ui.activity.ChatNormalActivity
import tw.com.chainsea.chat.util.IntentUtil
import tw.com.chainsea.chat.util.SortUtil
import tw.com.chainsea.chat.view.account.homepage.ServicesNumberManagerHomepageActivity
import tw.com.chainsea.chat.view.contact.adapter.ContactPersonAdapter
import tw.com.chainsea.chat.view.homepage.BossServiceNumberHomepageActivity
import tw.com.chainsea.chat.view.homepage.EmployeeInformationHomepageActivity
import tw.com.chainsea.chat.view.homepage.SelfInformationHomepageActivity
import tw.com.chainsea.chat.view.homepage.VisitorHomepageActivity
import tw.com.chainsea.chat.view.qrcode.QrCodeType
import tw.com.chainsea.chat.view.qrcode.ServiceNumberQrCodeActivity
import tw.com.chainsea.chat.view.service.ServiceBroadcastEditorActivity
import tw.com.chainsea.chat.view.service.ServiceNumberManageActivity
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemTouchHelperCallback
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemTouchHelperExtension

/**
 * 首頁聯絡人頁面
 * */
class ContactPersonFragment :
    Fragment(),
    ContactPersonAdapter.OnContactPersonListener,
    ContactPersonAdapter.OnGroupClick {
    private lateinit var viewModel: ContactPersonViewModel
    private lateinit var binding: FragmentContactPerson2Binding
    private val contactPersonAdapter: ContactPersonAdapter by lazy {
        ContactPersonAdapter()
    }
    private val userId: String by lazy {
        TokenPref.getInstance(requireContext()).userId
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentContactPerson2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        observeData()
        initView()
        initData()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            viewModel.getAllData(RefreshSource.REMOTE)
        }
    }

    /**
     * 初始化 viewModel
     * */
    private fun initViewModel() {
        val contactPersonFactory = ViewModelFactory(requireActivity().application)
        viewModel =
            ViewModelProvider(
                requireActivity(),
                contactPersonFactory
            )[ContactPersonViewModel::class.java]
    }

    /**
     * 初始化 view
     * */
    private fun initView() {
        val mCallback = ItemTouchHelperCallback(ItemTouchHelper.START)
        val itemTouchHelper = ItemTouchHelperExtension(mCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvChatRoomList)

        contactPersonAdapter.setOnContactPersonListener(this)
        contactPersonAdapter.setOnGroupClickListener(this)
        binding.rvChatRoomList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = contactPersonAdapter
        }
    }

    /**
     * 初始化 Data
     * */
    fun initData() {
        viewModel.getAllData(RefreshSource.LOCAL)
    }

    /**
     * 觀察 liveData
     * */
    private fun observeData() {
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            ToastUtils.showToast(requireContext(), message)
            Log.e(javaClass.simpleName, message)
        }

        viewModel.chatRoomData.observe(viewLifecycleOwner) {
            val status: Boolean = ChatRoomReference.getInstance().save(it)
            if (status) {
                val bundle =
                    bundleOf(
                        BundleKey.EXTRA_SESSION_ID.key() to it.id,
                        BundleKey.WHERE_COME.key() to javaClass.simpleName
                    )
                if (it.type == ChatRoomType.services) {
                    IntentUtil.startIntent(requireContext(), ChatActivity::class.java, bundle)
                } else {
                    IntentUtil.startIntent(requireContext(), ChatNormalActivity::class.java, bundle)
                }
//                navigationToChat(it.id)
            } else {
                ToastUtils.showToast(requireContext(), "save room entity failed ")
            }
        }

        viewModel.roomId.observe(viewLifecycleOwner) {
            it?.let {
                navigationToChat(it)
            }
        }

        viewModel.serviceNumberEntity.observe(viewLifecycleOwner) {
            when (it.serviceNumberType) {
                ServiceNumberType.BOSS.type -> {
                    val bundle =
                        bundleOf(
                            BundleKey.BROADCAST_ROOM_ID.key() to it.broadcastRoomId,
                            BundleKey.SERVICE_NUMBER_ID.key() to it.serviceNumberId
                        )
                    IntentUtil.startIntent(requireContext(), BossServiceNumberHomepageActivity::class.java, bundle)
                }

                ServiceNumberType.MANAGER.type -> {
                    if (it.isManager || it.isOwner) {
                        val bundle = bundleOf(BundleKey.SERVICE_NUMBER_ID.key() to it.serviceNumberId)
                        IntentUtil.startIntent(requireContext(), ServicesNumberManagerHomepageActivity::class.java, bundle)
                    } else {
                        val bundle = bundleOf(BundleKey.SERVICE_NUMBER_ID.key() to it.serviceNumberId)
                        IntentUtil.startIntent(requireContext(), ServiceNumberManageActivity::class.java, bundle)
                    }
                }

                else -> {
                    val bundle = bundleOf(BundleKey.SERVICE_NUMBER_ID.key() to it.serviceNumberId)
                    IntentUtil.startIntent(requireContext(), ServiceNumberManageActivity::class.java, bundle)
                }
            }
        }

        // 加好友失敗 進入虛擬聊天室
        viewModel.addFriendFailed.observe(viewLifecycleOwner) {
            if (!it.first) {
                val bundle =
                    bundleOf(
                        BundleKey.USER_NICKNAME.key() to it.second,
                        BundleKey.USER_ID.key() to it.third
                    )
                IntentUtil.startIntent(requireContext(), ChatNormalActivity::class.java, bundle)
            }
        }

        viewModel.contactListData
            .onEach {
                contactPersonAdapter.setData(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.onGetLocalDataDone
            .onEach {
                viewModel.getAllData(RefreshSource.REMOTE)
            }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    /**
     * 點擊自己 item 事件
     * */
    override fun onSelfItemClick(profile: UserProfileEntity) {
        navigationToProfilePage()
    }

    /**
     * 點擊自己頭像事件
     * */
    override fun onSelfItemAvatarClick(profile: UserProfileEntity) {
        val name = requireContext().javaClass.simpleName
        val bundle =
            bundleOf(
                BundleKey.ACCOUNT_TYPE.key() to UserType.EMPLOYEE.name,
                BundleKey.ACCOUNT_ID.key() to userId,
                BundleKey.WHERE_COME.key() to name
            )
        IntentUtil.startIntent(requireContext(), SelfInformationHomepageActivity::class.java, bundle)
    }

    /**
     * 點擊 QR Code 事件
     * */
    override fun onBarCodeClick() {
        val bundle =
            bundleOf(
                BundleKey.TYPE.key() to QrCodeType.person,
                BundleKey.TITLE.key() to getString(R.string.text_tenant_qrcode)
            )
//        IntentUtil.startIntent(requireContext(), QrCodeActivity::class.java, bundle)
        IntentUtil.startIntent(requireContext(), ServiceNumberQrCodeActivity::class.java)
    }

    /**
     * 點擊其他 item 事件
     * 是好友才能聊天，不是的話會先加聯絡人好友
     * */
    override fun onProfileItemClick(profile: UserProfileEntity) {
        if (profile.roomId != null && profile.roomId != "null" && profile.roomId != "") {
            navigationToChat(profile.roomId)
        } else {
            val bundle =
                bundleOf(
                    BundleKey.USER_NICKNAME.key() to profile.nickName,
                    BundleKey.USER_ID.key() to profile.id,
                    BundleKey.WHERE_COME.key() to javaClass.simpleName // 更新聊天室交互時間
                )
            IntentUtil.startIntent(requireContext(), ChatNormalActivity::class.java, bundle)
        }
    }

    /**
     * 點擊其他大頭像事件
     * */
    override fun onProfileAvatarClick(profile: UserProfileEntity) {
        intentToHomePage(profile.id, profile.userType)
    }

    /**
     * 用戶頁面主頁點擊事件
     * */
    override fun onProfileHomeClick(profile: UserProfileEntity) {
        intentToHomePage(profile.id, profile.userType)
    }

    private fun intentToHomePage(
        profileId: String,
        profileType: UserType
    ) {
        if (userId == profileId) {
            IntentUtil.startIntent(requireContext(), SelfInformationHomepageActivity::class.java)
        } else {
            val bundle =
                bundleOf(
                    BundleKey.ACCOUNT_ID.key() to profileId,
                    BundleKey.ACCOUNT_TYPE.key() to profileType,
                    BundleKey.WHERE_COME.key() to javaClass.simpleName
                )
            IntentUtil.startIntent(requireContext(), EmployeeInformationHomepageActivity::class.java, bundle)
        }
    }

    /**
     *  創建社團 item 點擊事件
     * */
    override fun onCreateGroupItemClick() {
        IntentUtil.startIntent(requireContext(), CreateGroupActivity::class.java)
    }

    /**
     *  社團 item 點擊事件
     * */
    override fun onGroupItemClick(entity: GroupEntity) {
        val chatRoomEntity = ChatRoomReference.getInstance().findById(entity.id)
        if (chatRoomEntity == null) {
            viewModel.getRoomItem(entity.id)
        } else {
            navigationToChat(chatRoomEntity)
        }
    }

    /**
     * 群組頁面點擊事件
     * */
    override fun onGroupHomeClick(entity: GroupEntity) {
        val groupEntity = ChatRoomReference.getInstance().findById(entity.id)
        groupEntity?.let {
            toMainPage(it)
        } ?: run {
            viewModel.getRoomItem(entity.id)
        }
    }

    private fun toMainPage(entity: ChatRoomEntity) =
        CoroutineScope(Dispatchers.IO).launch {
            var memberList = mutableListOf<UserProfileEntity>()
            entity.chatRoomMember?.let { chatMember ->
                chatMember.forEach { member ->
                    val userProfileEntity = DBManager.getInstance().queryUser(member.memberId)
                    if (entity.ownerId == member.memberId && entity.roomType == ChatRoomType.group) {
                        userProfileEntity.groupPrivilege = GroupPrivilegeEnum.Owner
                    } else {
                        userProfileEntity.groupPrivilege = member.privilege
                    }
                    memberList.add(userProfileEntity)
                }

                // 如果是社團，按照擁有者 -> 管理員 -> 一般成員排序
                if (entity.roomType == ChatRoomType.group) {
                    memberList = SortUtil.sortGroupOwnerManagerByPrivilege(memberList)
                }
            }
            val bundle =
                bundleOf(
                    BundleKey.ROOM_ID.key() to entity.id,
                    BundleKey.MEMBERS_LIST.key() to JsonHelper.getInstance().toJson(memberList.filter { user -> user.alias != "ALL" }),
                    BundleKey.ROOM_TYPE.key() to entity.type.name
                )
            IntentUtil.startIntent(requireContext(), MainPageActivity::class.java, bundle)
        }

    /**
     *  服務號 item 點擊事件
     * */
    override fun onServiceItemClick(serviceNum: ServiceNum) {
        val roomId = serviceNum.roomId
        val hasData = ChatRoomReference.getInstance().hasLocalData(roomId)
        ServiceNumberReference.updateSubscribeServiceNumberTime(serviceNum.serviceNumberId)
        if (hasData) {
            val entity =
                ChatRoomReference
                    .getInstance()
                    .findById2(userId, roomId, true, true, true, true, true)
            entity?.let {
                ChatRoomReference.getInstance().updateInteractionTimeById(it.id)
                it.updateTime = System.currentTimeMillis()
                navigationToChat(it)
            } ?: run {
                viewModel.getRoomItem(roomId)
            }
        } else {
            viewModel.getRoomItem(roomId)
        }
        CoroutineScope(Dispatchers.IO).launch {
            viewModel.getSubscribeServiceNumberList(RefreshSource.LOCAL)
        }
    }

    /**
     * 訂閱號頁面點擊事件
     * */
    override fun onServiceHomeClick(serviceNumberId: String) {
        viewModel.getServiceNumberEntity(serviceNumberId)
    }

    override fun onServiceNumberBroadcastClick(entity: ServiceNumberEntity) {
        val bundle =
            bundleOf(
                BundleKey.TITLE.key() to entity.broadcastRoomId,
                BundleKey.BROADCAST_ROOM_ID.key() to entity.name,
                BundleKey.SERVICE_NUMBER_ID.key() to entity.serviceNumberId
            )
        IntentUtil.startIntent(requireContext(), ServiceBroadcastEditorActivity::class.java, bundle)
    }

    override fun onServiceNumberContactItemClick(entity: CustomerEntity) {
        viewModel.getCustomerRoom(entity.id)
    }

    override fun onServiceNumberContactHomeClick(entity: CustomerEntity) {
        val bundle =
            bundleOf(
                BundleKey.ACCOUNT_TYPE.key() to UserType.VISITOR,
                BundleKey.ACCOUNT_ID.key() to entity.id,
                BundleKey.ROOM_ID.key() to entity.roomId,
                BundleKey.USER_NICKNAME.key() to entity.nickName,
                BundleKey.WHERE_COME.key() to javaClass.simpleName
            )
        IntentUtil.startIntent(requireContext(), VisitorHomepageActivity::class.java, bundle)
    }

    /**
     * 導向自己的聊天室頁面
     */
    private fun navigationToProfilePage() {
        viewModel.getSelfChatRoom()
    }

    /**
     * 導向聊天室頁面 (服務號及社團使用)
     */
    private fun navigationToChat(chatRoomEntity: ChatRoomEntity) {
        val bundle =
            bundleOf(
                BundleKey.EXTRA_SESSION_ID.key() to chatRoomEntity.id,
                BundleKey.WHERE_COME.key() to javaClass.simpleName
            )
        if (chatRoomEntity.roomType == ChatRoomType.serviceMember ||
            chatRoomEntity.roomType == ChatRoomType.bossSecretary ||
            chatRoomEntity.roomType == ChatRoomType.bossOwnerWithSecretary
        ) {
            IntentUtil.startIntent(requireContext(), ChatActivity::class.java, bundle)
        } else {
            IntentUtil.startIntent(requireContext(), ChatNormalActivity::class.java, bundle)
        }
    }

    /**
     * 導向聊天室頁面
     * */
    private fun navigationToChat(roomId: String) {
        val entity = ChatRoomReference.getInstance().findById(roomId)
        if (entity == null) {
            viewModel.getRoomItem(roomId)
        } else {
            viewModel.setChatRoomNotDeleted(roomId)

            val bundle =
                bundleOf(
                    BundleKey.EXTRA_SESSION_ID.key() to roomId,
                    BundleKey.WHERE_COME.key() to javaClass.simpleName
                )
            if (entity.type == ChatRoomType.services) {
                IntentUtil.startIntent(requireContext(), ChatActivity::class.java, bundle)
            } else {
                IntentUtil.startIntent(requireContext(), ChatNormalActivity::class.java, bundle)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun handleAsyncEvent(eventMsg: EventMsg<*>) {
        when (eventMsg.code) {
            MsgConstant.USER_EXIT, MsgConstant.ADD_GROUP_FILTER, MsgConstant.GROUP_UPGRADE_FILTER, MsgConstant.GROUP_REFRESH_FILTER -> {
                viewModel.getGroupRoomList(RefreshSource.REMOTE)
                viewModel.synGroupRoomList()
            }

            MsgConstant.INTERNET_STSTE_FILTER -> viewModel.getAllData(RefreshSource.REMOTE)
            MsgConstant.ACCOUNT_REFRESH_FILTER -> {
                val account = eventMsg.data as UserProfileEntity
                if (userId == account.id) {
                    CoroutineScope(Dispatchers.IO).launch {
                        viewModel.getSelfProfile(RefreshSource.REMOTE)
                    }
                } else {
                    viewModel.getAllData(RefreshSource.REMOTE)
                }
            }

            MsgConstant.REMOVE_FRIEND_FILTER, MsgConstant.ADD_FRIEND_FILTER -> {
                viewModel.getAllContentList(RefreshSource.REMOTE)
                viewModel.getServiceNumberContactList(RefreshSource.REMOTE)
                CoroutineScope(Dispatchers.IO).launch {
                    viewModel.getCollectionData(RefreshSource.REMOTE)
                }
            }

            MsgConstant.SELF_REFRESH_FILTER -> {
                CoroutineScope(Dispatchers.IO).launch {
                    viewModel.getSelfProfile(RefreshSource.REMOTE)
                    viewModel.getCollectionData(RefreshSource.REMOTE)
                }
            }

            MsgConstant.RECOMMEND_REFRESH_FILTER -> {}
            MsgConstant.REMOVE_SERVICE_NUM_FILTER, MsgConstant.ADD_SERVICE_NUM_FILTER -> {
                CoroutineScope(Dispatchers.IO).launch {
                    viewModel.getSubscribeServiceNumberList(
                        RefreshSource.REMOTE
                    )
                }
            }

            MsgConstant.REMOVE_LOVE_ACCOUNT_FILTER, MsgConstant.ADD_LOVE_ACCOUNT_FILTER -> {
                CoroutineScope(Dispatchers.IO).launch {
                    viewModel.getCollectionData(RefreshSource.LOCAL)
                }
                viewModel.getServiceNumberContactList(RefreshSource.REMOTE)
                viewModel.getAllContentList(RefreshSource.REMOTE)
            }

            MsgConstant.NOTICE_UPDATE_AVATARS -> {
                CoroutineScope(Dispatchers.IO).launch {
                    viewModel.getCollectionData(RefreshSource.LOCAL)
                }
                viewModel.getServiceNumberContactList(RefreshSource.REMOTE)
                viewModel.getAllContentList(RefreshSource.REMOTE)
            }

            MsgConstant.NOTICE_REFRESH_HOMEPAGE_AVATAR -> {
                CoroutineScope(Dispatchers.IO).launch {
                    viewModel.getSelfProfile(
                        RefreshSource.LOCAL
                    )
                }
            }

            // 接收到新增服務號，刷新服務號
            MsgConstant.SERVICE_NUMBER_PERSONAL_START -> {
                CoroutineScope(Dispatchers.IO).launch {
                    viewModel.getSubscribeServiceNumberList(RefreshSource.REMOTE)
                }
            }

            MsgConstant.Do_UPDATE_CONTACT_BY_LOCAL -> {
                initData()
            }

            MsgConstant.NOTICE_CREATE_ROOM -> {
                viewModel.getAllData(RefreshSource.REMOTE)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handleMainEvent(eventMsg: EventMsg<*>) {
        when (eventMsg.code) {
            MsgConstant.REMOVE_GROUP_FILTER -> {
                viewModel.getGroupRoomList(RefreshSource.REMOTE)
                viewModel.synGroupRoomList()
            }
        }
    }

    override fun onOpen(contactViewHolderType: ContactViewHolderType) {
        viewModel.addGroupOpenList(contactViewHolderType)
    }

    override fun onClose(contactViewHolderType: ContactViewHolderType) {
        viewModel.removeGroupOpenList(contactViewHolderType)
    }
}
