package tw.com.chainsea.chat.view.roomList.mainRoomList

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.google.common.collect.Lists
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import tw.com.chainsea.android.common.json.JsonHelper
import tw.com.chainsea.ce.sdk.bean.GroupRefreshBean
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType
import tw.com.chainsea.ce.sdk.bean.room.DiscussMemberSocket
import tw.com.chainsea.ce.sdk.bean.room.JoinToChatRoom
import tw.com.chainsea.ce.sdk.event.EventBusUtils
import tw.com.chainsea.ce.sdk.event.EventMsg
import tw.com.chainsea.ce.sdk.event.MsgConstant
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.config.BundleKey
import tw.com.chainsea.chat.config.InvitationType
import tw.com.chainsea.chat.databinding.FragmentMainRoomList3Binding
import tw.com.chainsea.chat.messagekit.listener.RecyclerScrollMoreListener
import tw.com.chainsea.chat.network.contact.ViewModelFactory
import tw.com.chainsea.chat.network.mainroom.MainRoomListViewModel
import tw.com.chainsea.chat.searchfilter.view.activity.MemberInvitationActivity
import tw.com.chainsea.chat.ui.dialog.IosProgressDialog
import tw.com.chainsea.chat.util.ThemeHelper
import tw.com.chainsea.chat.view.base.viewmodel.HomeViewModel
import tw.com.chainsea.chat.view.chat.ChatViewModel
import tw.com.chainsea.custom.view.alert.AlertView

class MainRoomList3Fragment :
    Fragment(),
    RoomListAdapterInterface,
    RoomListClickInterface {
    private val homeViewModel by activityViewModels<HomeViewModel>()
    private val chatViewModel by lazy {
        val factory = ViewModelFactory(requireActivity().application)
        ViewModelProvider(requireActivity(), factory)[ChatViewModel::class.java]
    }
    private lateinit var mainRoomListViewModel: MainRoomListViewModel
    private lateinit var binding: FragmentMainRoomList3Binding
    private val roomListAdapter: RoomListAdapter by lazy {
        RoomListAdapter(ThemeHelper.isGreenTheme())
    }
    private var dataTime = 0L
    private val observer =
        object : AdapterDataObserver() {
            override fun onItemRangeInserted(
                positionStart: Int,
                itemCount: Int
            ) {
                super.onItemRangeInserted(positionStart, itemCount)
                Log.d("Kyle112", "currentIndex=${mainRoomListViewModel.currentIndex}, itemCount=$itemCount, chunkSize=${mainRoomListViewModel.chunkSize}")
                // 一般聊天列表第一次資料載入20筆即可算載入完成，剩下的資料用分頁呈現
//            homeViewModel.sendShowPageRefreshByDbDone.postValue(HomeViewModel.PageReadyType.MainChatPage)
                // 當position 0可見時，更新時滑動至最上方
                val layoutManager = binding.rvChatRoomList.layoutManager as LinearLayoutManager
                if (layoutManager.findLastVisibleItemPosition() < layoutManager.childCount) {
                    layoutManager.postOnAnimation {
                        layoutManager.scrollToPosition(0)
                    }
                }
            }
        }
    private val progressDialog by lazy { IosProgressDialog(requireContext()) }

    companion object {
        fun newInstance() = MainRoomList3Fragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = createDataBindingView(inflater, container)

    private fun createDataBindingView(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DataBindingUtil
        .inflate<FragmentMainRoomList3Binding>(
            inflater,
            R.layout.fragment_main_room_list3,
            container,
            false
        ).apply {
            binding = this
            initViewModel()

            homeViewModel.sendMainChatRoomListRefresh
                .onEach {
                    // true = 火箭頁同步, false = 主頁刷新
                    Log.d("Kyle111", "sendMainChatRoomListRefreshByDB ${System.currentTimeMillis()}")
                    mainRoomListViewModel.refreshList(homeViewModel.preloadStepEnum)
                }.launchIn(viewLifecycleOwner.lifecycleScope)

            homeViewModel.sendMainChatRoomListRefreshByDB
                .onEach {
                    mainRoomListViewModel.refreshListByDb()
                }.launchIn(viewLifecycleOwner.lifecycleScope)

            homeViewModel.onCustomerGot.observe(viewLifecycleOwner) {
                roomListAdapter.notifyItemChanged(it)
            }

            chatViewModel.isRoomMute.observe(viewLifecycleOwner) {
                mainRoomListViewModel.refreshListByDb()
            }

            chatViewModel.isRoomTop.observe(viewLifecycleOwner) {
                mainRoomListViewModel.refreshListByDb()
            }

            chatViewModel.isRoomDeleted.observe(viewLifecycleOwner) {
                mainRoomListViewModel.refreshListByDb()
            }

            chatViewModel.isMessageRead.observe(viewLifecycleOwner) {
                mainRoomListViewModel.refreshListByDb()
            }
            homeViewModel.sendRefreshBossServiceNumberOwnerChanged.observe(viewLifecycleOwner) {
                if (it) mainRoomListViewModel.refreshList(homeViewModel.preloadStepEnum)
            }

            homeViewModel.sendRefreshListByAPI.observe(viewLifecycleOwner) {
                if (it) mainRoomListViewModel.refreshList(homeViewModel.preloadStepEnum)
            }

            lifecycleOwner = this@MainRoomList3Fragment.viewLifecycleOwner
        }.root

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        showShimmer()
        roomListAdapter.setRoomListAdapterInterface(this)
        roomListAdapter.setRoomListClickInterface(this)

        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.initialPrefetchItemCount = 20
        binding.rvChatRoomList.apply {
            setRecycledViewPool(homeViewModel.roomListRecyclerViewPool)
            setItemViewCacheSize(20)
            setHasFixedSize(true)
            itemAnimator = null
            isNestedScrollingEnabled = false
            adapter = roomListAdapter
        }
        EventBus.getDefault().post(EventMsg<Any?>(MsgConstant.UPDATE_MAIN_BADGE_NUMBER_EVENT))
        EventBusUtils.register(this)
        roomListAdapter.registerAdapterDataObserver(observer)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBusUtils.unregister(this)
        roomListAdapter.unregisterAdapterDataObserver(observer)
    }

    private fun initViewModel() {
        val mainRoomListFactory = ViewModelFactory(requireActivity().application)
        mainRoomListViewModel =
            ViewModelProvider(requireActivity(), mainRoomListFactory)[MainRoomListViewModel::class.java]

        mainRoomListViewModel.apply {
            binding.rvChatRoomList.addOnScrollListener(
                RecyclerScrollMoreListener(binding.rvChatRoomList.layoutManager as LinearLayoutManager) { totalItemCount, lastVisibleItemPosition ->
                    // 加載更多數據
                    if (totalItemCount > chunkSize) {
                        chunkSize = totalItemCount // 取得目前加載的數量，當reload列表時，要以最新的chunkSize去撈相對應的數量
                    }
                    if (totalItemCount == (lastVisibleItemPosition + 1)) {
                        doLoadMore(roomListAdapter.currentList, lastVisibleItemPosition)
                    }
                }
            )

            sendChatRoomList
                .onEach {
                    // 因為創建時就會有個人聊天室的存在，不會有空白狀況
                    hideShimmer()
                    if (it.size < 15) {
                        // 顯示新增聊天室按鈕
                        val joinToChatRoom = JoinToChatRoom(name = getString(R.string.text_new_chat_room))
                        val chatList: MutableList<Any> = Lists.newArrayList(it)
                        chatList.add(joinToChatRoom)
                        roomListAdapter.setData(selfUserId, chatList)
                    } else {
                        roomListAdapter.setData(selfUserId, it)
                    }
                }.launchIn(viewLifecycleOwner.lifecycleScope)

            sendChatRoomListByLoadMore
                .onEach {
                    roomListAdapter.setData(selfUserId, it)
                    isLoadMoreIng = false
                }.launchIn(viewLifecycleOwner.lifecycleScope)

            sendRefreshByDB
                .onEach {
                    refreshListByDb()
                }.launchIn(viewLifecycleOwner.lifecycleScope)

            onAvatarUpdated
                .onEach {
                    val currentRoomPosition = roomListAdapter.currentList.indexOf(it)
                    roomListAdapter.notifyItemChanged(currentRoomPosition, false)
                }.launchIn(viewLifecycleOwner.lifecycleScope)

            onSelfAvatarUpdated
                .onEach {
                    val currentRoomPosition = roomListAdapter.currentList.indexOf(it)
                    roomListAdapter.notifyItemChanged(currentRoomPosition, false)
                }.launchIn(viewLifecycleOwner.lifecycleScope)

            onAtChatRoom
                .onEach { pair ->
                    roomListAdapter.currentList.forEachIndexed { index, any ->
                        if (any is ChatRoomEntity && any.id == pair.first) {
                            any.isAtMe = pair.second
                            roomListAdapter.notifyItemChanged(index)
                            return@forEachIndexed
                        }
                    }
                    val roomSortList = roomListAdapter.currentList.filter { it is ChatRoomEntity }.toMutableList()
                    val joinRoom = roomListAdapter.currentList.filter { it is JoinToChatRoom }
                    (roomSortList as MutableList<ChatRoomEntity>).sort()
                    val chatList: MutableList<Any> = Lists.newArrayList(roomSortList)
                    if (chatList.size <= 15 && joinRoom.size > 0) {
                        chatList.add(joinRoom.get(0))
                    }
                    roomListAdapter.setData(selfUserId, chatList)
                }.launchIn(viewLifecycleOwner.lifecycleScope)
        }
    }

    private fun showShimmer() {
        dataTime = System.currentTimeMillis()
        Log.d("Kyle111", "showShimmer = ${System.currentTimeMillis()}")
        binding.rvChatRoomList.visibility = View.GONE
        binding.includeShimmer.getRoot().visibility = View.VISIBLE
    }

    private fun hideShimmer() {
        Log.d("Kyle111", "hideShimmer = ${System.currentTimeMillis()}")
        binding.rvChatRoomList.visibility = View.VISIBLE
        binding.includeShimmer.root.visibility = View.GONE
    }

    fun scrollToTop() {
        binding.rvChatRoomList.post {
            binding.rvChatRoomList.scrollToPosition(0)
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun handleAsyncEvent(eventMsg: EventMsg<*>) {
        when (eventMsg.code) {
            MsgConstant.REFRESH_FILTER,
            MsgConstant.ADD_FRIEND_FILTER,
            MsgConstant.USER_EXIT,
            MsgConstant.ADD_GROUP_FILTER,
            MsgConstant.BUSINESS_BINDING_ROOM_EVENT,
            MsgConstant.REFRESH_ROOM_BY_LOCAL,
            MsgConstant.MSG_STATUS_FILTER,
            MsgConstant.REMOVE_LOVE_ACCOUNT_FILTER,
            MsgConstant.ADD_LOVE_ACCOUNT_FILTER,
            MsgConstant.SERVICE_NUMBER_PERSONAL_STOP,
            MsgConstant.REFRESH_CUSTOMER_NAME -> {
                if (homeViewModel.loadingStatus.value != HomeViewModel.LoadingType.FirstLoading) {
                    // 避免火箭頁載入時重覆打api
                    mainRoomListViewModel.refreshList(homeViewModel.preloadStepEnum)
                }
            }

            MsgConstant.REMOVE_GROUP_FILTER ->
                if (homeViewModel.loadingStatus.value != HomeViewModel.LoadingType.FirstLoading) {
                    mainRoomListViewModel.refreshListByDb()
                }

            // 多人聊天室有人加入
            MsgConstant.NOTICE_DISCUSS_MEMBER_ADD,
            // 多人聊天室有人"主動"離開
            MsgConstant.NOTICE_DISCUSS_MEMBER_EXIT -> {
                context?.let {
                    val discussMemberSocket = JsonHelper.getInstance().from(eventMsg.data, DiscussMemberSocket::class.java)
                    mainRoomListViewModel.updateDiscussOrGroupRoom(discussMemberSocket.roomId, discussMemberSocket.type)
                }
            }
            // 多人聊天室/社團有人被踢除
            MsgConstant.NOTICE_DISCUSS_GROUP_MEMBER_REMOVED -> {
                val data = JsonHelper.getInstance().fromToMap<String, String>(eventMsg.data.toString())
                val roomId = data["roomId"]
                val roomType = data["type"]
                val deletedMemberIds = data["deletedMemberIds"] as List<*>?
                roomType?.let {
                    when (it) {
                        ChatRoomType.discuss.name -> {
                            roomId?.let { mainRoomListViewModel.updateDiscussOrGroupRoom(roomId = it, roomType = ChatRoomType.discuss) }
                        }

                        ChatRoomType.group.name -> {
                            roomId?.let { mainRoomListViewModel.updateDiscussOrGroupRoom(roomId = it, roomType = ChatRoomType.group) }
                        }

                        else -> {}
                    }
                }
            }

            // 社團有人邀請加入/或是退出
            MsgConstant.GROUP_REFRESH_FILTER -> {
                try {
                    // 邀請加入
                    val data = eventMsg.data as JSONObject
                    if (data.optString("roomId").isNotEmpty()) {
                        mainRoomListViewModel.getChatRoomMember(data.optString("roomId"))
                    }
                } catch (e: Exception) {
                    // 退出
                    val data = JsonHelper.getInstance().from(eventMsg.data, GroupRefreshBean::class.java)
                    data?.let {
                        if (it.sessionId.isNotEmpty()) {
                            mainRoomListViewModel.getChatRoomMember(it.sessionId)
                        }
                    }
                }
            }

            // 從另一個平台(Desktop) 退出
            MsgConstant.NOTICE_DISMISS_DISCUSS_ROOM -> {
                val roomId = eventMsg.data as String
                mainRoomListViewModel.disableChatRoom(roomId)
            }

            // 多人聊天室有人更新nickName
            MsgConstant.NOTICE_DISCUSS_GROUP_USER_PROFILE_CHANGED -> {
                val userProfile = JsonHelper.getInstance().fromToMap<String, Any>(eventMsg.data.toString())
                val userId = userProfile["userId"].toString()
                mainRoomListViewModel.updateDiscussRoomTitle(userId, userProfile["nickName"].toString())
            }
            // 多人聊天室有人更新avatar
            MsgConstant.NOTICE_UPDATE_AVATARS -> {
                val avatarData = JsonHelper.getInstance().fromToMap<String, String>(eventMsg.data.toString())
                val userId = avatarData["accountId"].toString()
                val avatarId = avatarData["avatarId"].toString()
                mainRoomListViewModel.updateAvatarInDiscussRoom(userId, avatarId)
            }

            // 更新自己的頭圖
            MsgConstant.NOTICE_REFRESH_HOMEPAGE_AVATAR -> {
                mainRoomListViewModel.updateSelfAvatar()
            }

            // 更新自己的聊天室名稱
            MsgConstant.SELF_REFRESH_FILTER -> {
                mainRoomListViewModel.updateSelfRoomTitle(eventMsg.data.toString())
            }

            // 有新的聊天室
            MsgConstant.NOTICE_CREATE_ROOM -> {
                mainRoomListViewModel.refreshList(homeViewModel.preloadStepEnum)
            }

            // 臨時成員移除
            MsgConstant.NOTICE_SELF_EXIT_ROOM -> {
                mainRoomListViewModel.removeProvisionalRoom(eventMsg.data.toString())
            }

            // 有人 At(@) 自己
            MsgConstant.MESSAGE_AT -> {
                eventMsg.data?.let {
                    mainRoomListViewModel.getAtChatRoom(it.toString())
                }
            }

            // 多人轉社團
            MsgConstant.GROUP_UPGRADE_FILTER -> {
                mainRoomListViewModel.refreshList(homeViewModel.preloadStepEnum)
            }
            // 清空聊天紀錄-包含last msg
            MsgConstant.NOTICE_CLEAR_CHAT_ROOM_ALL_MESSAGE -> {
                mainRoomListViewModel.refreshListByDb()
            }

            MsgConstant.CHANGE_LAST_MESSAGE -> {
                val changeRoomId = eventMsg.data as String
                mainRoomListViewModel.changeLastMessage(changeRoomId)
            }
            // 更新好友聊天室名稱(別名)
            MsgConstant.Do_UPDATE_CONTACT_BY_LOCAL -> {
                roomListAdapter.notifyItemRangeChanged(0, roomListAdapter.itemCount)
            }
            // 離開聊天室後刷新列表
            MsgConstant.NOTICE_REFRESH_CHAT_ROOM_LIST -> {
                mainRoomListViewModel.refreshListByDb()
            }
        }
    }

    // 顯示是否刪除聊天室的 dialog
    private fun showDeleteRoomDialog(roomId: String) {
        AlertView
            .Builder()
            .setContext(requireContext())
            .setStyle(AlertView.Style.Alert)
            .setMessage(getString(R.string.warning_want_to_delete_this_chat_room))
            .setOthers(
                arrayOf(
                    getString(R.string.alert_cancel),
                    getString(R.string.alert_confirm)
                )
            ).setOnItemClickListener { o, clickPosition ->
                if (clickPosition == 1) {
                    chatViewModel.deleteRoom(roomId)
                }
            }.build()
            .setCancelable(true)
            .show()
    }

    override fun getGroupChatMember(roomId: String) {
        mainRoomListViewModel.getGroupChatMember(roomId)
    }

    // 取得多人聊天室成員
    override fun getChatMember(
        roomId: String,
        isCustomName: Boolean
    ) {
        mainRoomListViewModel.getDiscussChatMember(roomId, isCustomName)
    }

    // 第一次進線，取得客戶資料存資料庫
    override fun getCustomer(position: Int) {
        homeViewModel.getCustomer(position)
    }

    // 是否靜音聊天室
    override fun muteRoom(
        roomId: String,
        isMute: Boolean
    ) {
        chatViewModel.changeMute(isMute, roomId)
    }

    override fun pinTop(
        roomId: String,
        isTop: Boolean
    ) {
        chatViewModel.changeTop(isTop, roomId)
    }

    override fun deleteRoom(roomId: String) {
        showDeleteRoomDialog(roomId)
    }

    override fun setupUnread(
        roomId: String,
        unreadNum: Int
    ) {
        chatViewModel.setupUnread(roomId, unreadNum)
    }

    override fun joinChatRoom() {
        startActivity(
            Intent(requireActivity(), MemberInvitationActivity::class.java)
                .putExtra(BundleKey.ROOM_TYPE.key(), InvitationType.Discuss.name)
        )
    }

    override fun getFriendRoomMember(roomId: String) {
        chatViewModel.getChatMember(roomId)
    }

    private var clickItemView: View? = null

    override fun onResume() {
        super.onResume()
        dismissIosDialog()
        clickItemView?.isEnabled = true
    }

    override fun onOpenChat(view: View) {
        showIosDialog()
        clickItemView = view
    }

    fun showIosDialog() {
        activity?.let {
            if (!it.isFinishing && !it.isDestroyed) {
                progressDialog.show()
            }
        }
    }

    fun dismissIosDialog() {
        progressDialog.dismiss()
    }
}
