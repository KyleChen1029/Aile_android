package tw.com.chainsea.chat.view.base

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.google.common.base.Strings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.android.common.event.KeyboardHelper
import tw.com.chainsea.android.common.json.JsonHelper
import tw.com.chainsea.android.common.log.CELog
import tw.com.chainsea.ce.sdk.SdkLib
import tw.com.chainsea.ce.sdk.bean.msg.FacebookCommentStatus
import tw.com.chainsea.ce.sdk.bean.msg.FacebookPostStatus
import tw.com.chainsea.ce.sdk.bean.msg.MessageFlag
import tw.com.chainsea.ce.sdk.bean.msg.MessageType
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType.bossOwnerWithSecretary
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType.bossSecretary
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType.serviceMember
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType.services
import tw.com.chainsea.ce.sdk.bean.todo.TodoEntity
import tw.com.chainsea.ce.sdk.cache.ChatMemberCacheService
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.database.sp.UserPref
import tw.com.chainsea.ce.sdk.event.EventBusUtils
import tw.com.chainsea.ce.sdk.event.EventMsg
import tw.com.chainsea.ce.sdk.event.MsgConstant
import tw.com.chainsea.ce.sdk.event.SocketEvent
import tw.com.chainsea.ce.sdk.event.SocketEventEnum
import tw.com.chainsea.ce.sdk.http.cp.respone.CheckVersionResponse
import tw.com.chainsea.ce.sdk.network.model.response.Guarantor
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference
import tw.com.chainsea.ce.sdk.service.ChatRoomService
import tw.com.chainsea.ce.sdk.socket.cp.model.GuarantorJoinContent
import tw.com.chainsea.ce.sdk.socket.cp.model.TenantDeleteMember
import tw.com.chainsea.chat.App
import tw.com.chainsea.chat.BuildConfig
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.base.BaseServiceActivity
import tw.com.chainsea.chat.config.BundleKey
import tw.com.chainsea.chat.config.Const
import tw.com.chainsea.chat.config.ScannerType
import tw.com.chainsea.chat.config.SystemConfig
import tw.com.chainsea.chat.databinding.ActivityHomeBinding
import tw.com.chainsea.chat.databinding.GridViewBinding
import tw.com.chainsea.chat.lib.ActivityManager
import tw.com.chainsea.chat.lib.AlertChooseDialogUtil
import tw.com.chainsea.chat.lib.ChatService
import tw.com.chainsea.chat.lib.NetworkChangeReceiver
import tw.com.chainsea.chat.lib.NetworkUtils
import tw.com.chainsea.chat.lib.NotifyHelper
import tw.com.chainsea.chat.lib.ToastUtils
import tw.com.chainsea.chat.network.contact.ContactPersonViewModel
import tw.com.chainsea.chat.network.contact.ViewModelFactory
import tw.com.chainsea.chat.receiver.AlarmValues
import tw.com.chainsea.chat.refactor.loginPage.LoginCpActivity
import tw.com.chainsea.chat.searchfilter.view.activity.CreateGroupActivity
import tw.com.chainsea.chat.searchfilter.view.activity.GlobalSearchNewActivity
import tw.com.chainsea.chat.service.ActivityTransitionsControl
import tw.com.chainsea.chat.service.HeadsetPlugReceiver
import tw.com.chainsea.chat.service.TodoAlarmTimerService
import tw.com.chainsea.chat.service.fcm.FCMTokenManager
import tw.com.chainsea.chat.ui.activity.ChatActivity
import tw.com.chainsea.chat.ui.activity.ChatNormalActivity
import tw.com.chainsea.chat.ui.activity.EcologyActivity
import tw.com.chainsea.chat.ui.adapter.RichMenuAdapter
import tw.com.chainsea.chat.ui.adapter.entity.RichMenuInfo
import tw.com.chainsea.chat.util.BadgeUtil
import tw.com.chainsea.chat.util.IntentUtil
import tw.com.chainsea.chat.util.NoDoubleClickListener
import tw.com.chainsea.chat.util.SystemKit
import tw.com.chainsea.chat.view.account.ChangeTenantActivity
import tw.com.chainsea.chat.view.base.viewmodel.HomeViewModel
import tw.com.chainsea.chat.view.base.viewmodel.HomeViewModel.PageReadyType
import tw.com.chainsea.chat.view.contact.ContactPersonFragment
import tw.com.chainsea.chat.view.roomList.mainRoomList.MainRoomList3Fragment
import tw.com.chainsea.chat.view.roomList.serviceRoomList.ServiceNumberSortType
import tw.com.chainsea.chat.view.roomList.serviceRoomList.ServiceRoomList3Fragment
import tw.com.chainsea.chat.view.todo.TodoOverviewFragment
import tw.com.chainsea.chat.widget.GridItemDecoration
import tw.com.chainsea.custom.view.alert.AlertView
import java.text.MessageFormat
import kotlin.math.min
import kotlin.system.exitProcess

class HomeActivity : BaseServiceActivity() {
    val binding: ActivityHomeBinding by lazy {
        ActivityHomeBinding.inflate(layoutInflater)
    }

    private lateinit var contactViewModel: ContactPersonViewModel

    // 右上 rich menu popup window
    private var topMenuPopupWindow: PopupWindow? = null

    // 耳機插入廣播
    private val headsetPlugReceiver: HeadsetPlugReceiver = HeadsetPlugReceiver()

    // 網路更換廣播
    private val networkChangeReceiver = NetworkChangeReceiver.newInstance()

    // 首頁
    private val mainRoomListFragment: MainRoomList3Fragment by lazy { MainRoomList3Fragment.newInstance() }

    // 服務號
    private val serviceRoomListFragment: ServiceRoomList3Fragment by lazy { ServiceRoomList3Fragment.newInstance() }

    // 聯絡人
    private val contactPersonFragment: ContactPersonFragment by lazy { ContactPersonFragment() }

    // 工作列表
    private val todoOverviewFragment: TodoOverviewFragment by lazy { TodoOverviewFragment.newInstance() }

    // 使用者要連按兩次返回才會關閉 app
    private var exitTime: Long = 0

    private val bottomTabAdapter =
        BottomTabAdapter {
            switchBottomTab(it)
        }
    private var syncCompleted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // setting theme before onCreate
        super.onCreate(savedInstanceState)

        // 重新開啟
        if (App.APP_STATUS != App.APP_STATUS_NORMAL) {
            App.reInitApp()
            finish()
            return
        }

        ActivityManager.addBaseActivity(this)
        setContentView(binding.root)

        initViewModel()
        initSocket()
        observeData()
        initFragment()
        checkVersion()
        initListener()
        registerReceiver()
    }

    private fun initSocket() {
        ChatService.getInstance().connect(this)
    }

    private fun autoLoginLoadData() {
        // CP創建團隊回復
        SystemKit.recoverTransTenant(this)
    }

    private fun preLoadData(isNeedToShowPreloadPage: Boolean) {
        if (isNeedToShowPreloadPage) {
            homeViewModel.loadingStatus.value = HomeViewModel.LoadingType.FirstLoading
            binding.preLoadPage.visibility = View.VISIBLE
            homeViewModel.doSyncDataByRocket.value = true
            homeViewModel.preloadJob.start()
        } else {
            homeViewModel.backgroundSync()
            homeViewModel.loadingStatus.value = HomeViewModel.LoadingType.OtherLoading
            autoLoginLoadData()
            homeViewModel.doSyncDataByRocket.value = false
            val bundle = intent.extras
            bundle?.let {
                val bundleType = it.getString(BundleKey.TYPE.key())
                val roomId = it.getString(BundleKey.EXTRA_SESSION_ID.key())
                val messageId = it.getString(BundleKey.EXTRA_MESSAGE.key())
                Log.d("AileFireBaseMessagingService", "HomeActivity preLoadData bundleType:$bundleType roomId:$roomId messageId:$messageId")
                if (bundleType != null && roomId != null && messageId != null) {
                    if (bundleType == "FcmNotification") { // FCM點擊pendingIntent跳轉該聊天室
                        CoroutineScope(Dispatchers.IO).launch {
                            val chatRoomEntity = homeViewModel.queryRoomById(roomId)
                            chatRoomEntity?.let { room ->
                                startActivity(
                                    Intent(
                                        this@HomeActivity,
                                        if (room.type == serviceMember ||
                                            room.type == services ||
                                            room.type == bossOwnerWithSecretary ||
                                            room.type == bossSecretary
                                        ) {
                                            ChatActivity::class.java
                                        } else {
                                            ChatNormalActivity::class.java
                                        }
                                    ).putExtra(BundleKey.EXTRA_SESSION_ID.key(), room.id)
                                        .putExtra(BundleKey.UNREAD_MESSAGE_ID.key(), messageId)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getData() {
        val isNeedSaveTenant = intent.getBooleanExtra(BundleKey.IS_NEED_SAVE_TENANT.key(), false)
        tenantViewModel.getTenantList(isNeedSaveTenant)
        intent.removeExtra(BundleKey.IS_NEED_SAVE_TENANT.key())
        homeViewModel.getServiceRoomUnReadSum()
        homeViewModel.getChatRoomListUnReadSum()
        homeViewModel.getTodoExpiredCount()
    }

    private fun initViewModel() {
        val contactPersonFactory = ViewModelFactory(application)
        contactViewModel =
            ViewModelProvider(this, contactPersonFactory)[ContactPersonViewModel::class.java]

        homeViewModel.apply {
            sendGoLoginPage
                .onEach { errorMessage ->
                    errorMessage?.let {
                        if (it.isNotEmpty()) {
                            showForceLogoutDialog(it)
                        }
                    }
                }.launchIn(this@HomeActivity.lifecycleScope)
            sendGoHomePage
                .onEach {
                    if (it) {
                        autoLoginLoadData()
                    }
                }.launchIn(this@HomeActivity.lifecycleScope)
        }
    }

    private fun initBottomTab() {
        binding.rvBottomTabs.apply {
            layoutManager = GridLayoutManager(this@HomeActivity, bottomTabAdapter.itemCount)
            this.adapter = bottomTabAdapter
        }
    }

    private fun registerReceiver() {
        // 耳機
        val headsetPlugIntentFilter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
        registerReceiver(headsetPlugReceiver, headsetPlugIntentFilter)
        val networkIntentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        // 網路
        registerReceiver(networkChangeReceiver, networkIntentFilter)
    }

    @SuppressLint("CommitTransaction")
    private fun initFragment() {
        supportFragmentManager
            .beginTransaction()
            .add(R.id.contentFL, mainRoomListFragment, getString(R.string.toolbar_chat_title))
            .add(R.id.contentFL, serviceRoomListFragment, getString(R.string.toolbar_service_number_title))
            .show(mainRoomListFragment)
            .hide(serviceRoomListFragment)
            .commitAllowingStateLoss()
        binding.toolBar.tvToolBarTitle.text = getString(R.string.toolbar_chat_title)
    }

    private fun isBindAileFromAutoLogin(
        bindUrl: String?,
        isCollectInfo: Boolean
    ) {
        val countryCode = TokenPref.getInstance(this).countryCode
        if (!isCollectInfo && countryCode == Const.AREA_CODE_TAIWAN) {
            bindUrl?.let {
                if (!BuildConfig.DEBUG) {
                    if (it.isNotEmpty()) {
                        startActivity(
                            Intent(this, EcologyActivity::class.java).putExtra(
                                BundleKey.BIND_URL.key(),
                                it
                            )
                        )
                    }
                }
            }
        }
    }

    private fun checkVersion() {
//        if (!App.getInstance().isCheckVersionUpdate) {
        homeViewModel.forceRefreshSqlData()
//        }
    }

    private fun initListener() {
        binding.toolBar.root.setOnClickListener { doServiceTabToolBarClick() }
        binding.toolBar.ivAdd.setOnClickListener { doOpenTopRichMenuAction(it as ImageView) }
        binding.toolBar.ivGlobalSearch.setOnClickListener(
            object : NoDoubleClickListener() {
                override fun onNoDoubleClick(v: View?) {
                    bottomTabAdapter.getCurrentTab()?.let {
                        startActivity(
                            Intent(this@HomeActivity, GlobalSearchNewActivity::class.java)
                                .putExtra(BundleKey.BOTTOM_TAB.key(), it.type)
                        )
                    }
                }
            }
        )
        binding.toolBar.ivScanning.setOnClickListener {
            ActivityTransitionsControl.navigateScanner(this)
        }
        binding.noInternet.getRoot().setOnClickListener {
            IntentUtil.start(this, Intent(Settings.ACTION_WIFI_SETTINGS))
        }

        binding.toolBar.btnChangeTenant.setOnClickListener {
            if (binding.nav.isShown) {
                binding.drawer.close()
            } else {
                binding.drawer.open()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun observeData() {
        homeViewModel.unreadCount.observe(this) { count ->
            val currentTenant = TokenPref.getInstance(this).cpCurrentTenant
            binding.toolBar.btnChangeTenant.loadTenantAvatarIcon(currentTenant)
            if (count == 0) {
                binding.toolBar.ivBadge.visibility = View.GONE
            } else {
                binding.toolBar.ivBadge.visibility = View.VISIBLE
            }
        }

        contactViewModel.allContactListData.observe(this) {
            homeViewModel.getContactAccountSize()
        }

        homeViewModel.chatRoomUnreadNumber.observe(this) {
            bottomTabAdapter.setUnReadNum(it, BottomTabEnum.MAIN)
        }

        homeViewModel.serviceRoomUnreadNumber.observe(this) {
            bottomTabAdapter.setUnReadNum(it, BottomTabEnum.SERVICE)
        }

        homeViewModel.contactAccountSize.observe(this) {
            if (it > 1 && contactPersonFragment.isVisible) {
                val contactString = getString(R.string.toolbar_contact_title)
                binding.toolBar.tvToolBarTitle.text =
                    MessageFormat.format("{0}({1})", contactString, it)
            }
        }

        homeViewModel.shouldShowServiceNumberTab.observe(this) {
//            if(!it)
//                homeViewModel.sendShowPageRefreshByDbDone.postValue(PageReadyType.ServiceNumberPage) //沒有任何服務號時，通知火箭頁載入服務號列表已完成
            homeViewModel.getBottomTab(it)
            if (!it && serviceRoomListFragment.isVisible) {
                switchBottomTab(BottomTab(BottomTabEnum.MAIN))
            }
        }

        homeViewModel.bottomTabData.observe(this) {
            bottomTabAdapter.setBottomData(it)
            initBottomTab()
            getData()
        }

        tenantViewModel.guarantorList.observe(this) {
            showGuarantorMissDialog(it)
        }

        homeViewModel.showUpdateDialog.observe(this) { showUpdateDialog(it) }

        homeViewModel.isNeedProLoad.observe(this) {
            if (NetworkUtils.isNetworkAvailable(this@HomeActivity)) {
                homeViewModel.autoLogin()
                preLoadData(it) // 火箭頁加載邏輯
            }
        }

        homeViewModel.preloadApi.observe(this) {
            binding.txtHint.text = getString(R.string.text_arrive_to_aile_soon) + it
        }

        homeViewModel.preloadApiStep.observe(this) {
            binding.txtProgress.text = String.format(getString(R.string.text_arrive_to_aile_soon_text), it)
        }

        homeViewModel.preloadTotalCount.observe(this) {
            binding.progressBar.max = it
        }

        homeViewModel.preloadCount.observe(this) {
            binding.progressBar.progress = it
        }

        homeViewModel.sendRefreshDB.observe(this) {
            if (it) {
                homeViewModel.getShouldShowServiceNumberTab()
            }
        }

        homeViewModel.isBindAile.observe(this) {
            isBindAileFromAutoLogin(it.first, it.second)
        }

        tenantViewModel.onChangeTenant.observe(this) {
            SystemKit.changeTenant(this, it, false)
        }

        homeViewModel.todoExpiredCount.observe(this) {
            bottomTabAdapter.setUnReadNum(it, BottomTabEnum.TODO)
        }

        // 跳轉到 創建團隊/加入團隊 頁面
        tenantViewModel.toCreateOrJoinTenant.observe(this) {
            val intent = Intent(this, LoginCpActivity::class.java)
            val bundle = bundleOf(BundleKey.TO_CREATE_OR_JOIN_TENANT.name to true)
            intent.putExtras(bundle)
            startActivity(intent)
            finish()
        }
        homeViewModel.sendCloseDrawer
            .onEach {
                if (binding.drawer.isShown) {
                    binding.drawer.close()
                }
            }.launchIn(this.lifecycleScope)

        /**
         * 當一般聊天列表和服務號列表頁面都載入完成後，才關閉火箭載入頁
         */
        homeViewModel.sendShowPageRefreshByDbDone.observe(this) {
            if (it == PageReadyType.MainChatPage) {
                CoroutineScope(Dispatchers.Main).launch {
                    binding.preLoadPage.visibility = View.GONE
                }
                homeViewModel.sendRefreshDB.postValue(true)
                Log.d("Kyle111", "hide preLoadPage")
                Log.d("Kyle111", "doSyncByRocket: ${homeViewModel.doSyncDataByRocket.value}, syncCompleted: $syncCompleted")
                if (homeViewModel.doSyncDataByRocket.value == false && !syncCompleted) {
                    CoroutineScope(Dispatchers.IO).launch {
                        homeViewModel.sendMainChatRoomListRefresh.emit(false) // 當一般聊天列表和服務號列表頁面都載入完成後，再打sync/room撈最新資料
                        syncCompleted = true // 為了以防serviceRoomList3在開合服務號分類時觸發onChanged會不斷重整一般聊天列表資料
                    }
                }
            }
        }
    }

    private fun showUpdateDialog(checkVersionResponse: CheckVersionResponse) {
        // 有新版本
        AlertChooseDialogUtil.getInstance().show(
            this,
            "檢測到新版本(" + checkVersionResponse.versionName + "),是否更新?",
            checkVersionResponse.upgradeKind
        ) { choose: Boolean ->
            if (choose) {
                val intent =
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=tw.com.chainsea.chat")
                    )
                startActivity(intent)
                if ("must" == checkVersionResponse.upgradeKind) {
                    exitProcess(0)
                }
            }
        }
    }

    // 少擔保人 dialog
    private fun showGuarantorMissDialog(guarantorList: List<Guarantor>) {
        AlertView
            .Builder()
            .setContext(this)
            .setStyle(AlertView.Style.Alert)
            .setTitle("重要通知")
            .setMessage("提醒您，團隊夥伴已離職，請立即取得團隊其他成員同意。")
            .setOthers(
                arrayOf(
                    getString(R.string.cancel),
                    getString(R.string.text_for_sure)
                )
            ).setOnItemClickListener { o, position ->
                // 0 是取消
                if (position == 0) {
                    val tenantList = TokenPref.getInstance(this).cpRelationTenantList
                    if (tenantList.size > 1) {
                        startActivity(Intent(this, ChangeTenantActivity::class.java))
                    } else {
                        SystemKit.logoutToLoginPage()
                    }
                } else {
                    ActivityTransitionsControl.navigateScannerReScanGuarantor(this, JsonHelper.getInstance().toJson(guarantorList))
                }
            }.build()
            .show()
    }

    private fun doOpenTopRichMenuAction(arrowImageView: ImageView) {
        val richMenuInfoList = RichMenuInfo.getBaseTopRichMenus()
        val topMenuBinding = GridViewBinding.inflate(layoutInflater)
        if (topMenuPopupWindow == null) {
            topMenuPopupWindow =
                PopupWindow(
                    topMenuBinding.root,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
        }
        topMenuBinding.tvMore.visibility = View.GONE
        val richMenuAdapter = RichMenuAdapter(richMenuInfoList)
        richMenuAdapter.setOnItemClickListener { adapter, view, position ->
            KeyboardHelper.hide(view)
            topMenuPopupWindow?.dismiss()
            val info = adapter.getItem(position) as RichMenuInfo
            val fixedMenuId = info.menuId
            if (info.type == RichMenuInfo.MenuType.FIXED.type) {
                if (fixedMenuId == RichMenuInfo.FixedMenuId.NEW_GROUP) {
                    IntentUtil.start(this, Intent(this, CreateGroupActivity::class.java))
                }
                overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out)
            }
        }
        richMenuAdapter.setOnBlockItemClickListener { v, isEnable ->
            ToastUtils.showToast(this, "請先解除封鎖")
        }
        topMenuBinding.recyclerView.apply {
            layoutManager = GridLayoutManager(this@HomeActivity, 4)
            addItemDecoration(GridItemDecoration())
            itemAnimator = DefaultItemAnimator()
            setHasFixedSize(true)
            adapter = richMenuAdapter
        }

        topMenuPopupWindow?.apply {
            setBackgroundDrawable(ColorDrawable(0x10101010))
            isOutsideTouchable = true
            isFocusable = false
            setOnDismissListener {
                binding.toolBar.ivAdd.setImageResource(R.drawable.nav_bar_arrow_down)
            }
        }

        arrowImageView.tag?.let {
            topMenuPopupWindow?.apply {
                dismiss()
                setOnDismissListener(null)
            }
            arrowImageView.tag = null
        } ?: run {
            arrowImageView.tag = "isOpen"
            binding.toolBar.ivAdd.setImageResource(R.drawable.nav_bar_arrow_up)
            topMenuPopupWindow?.showAsDropDown(binding.toolBar.root)
        }
    }

    /**
     * 刪除內存 DB 資料並且跳轉 FirstLoadActivity
     */
    private fun cleanDBAndRetrieveData() {
        val cleanInterval = 774800000 // 9 天內不觸發清除 DB 以免太過頻繁

        ChatMemberCacheService.retryCount = 0
        val sharedLastCleanTime: SharedPreferences =
            getSharedPreferences("cleanTimePref", MODE_PRIVATE)
        val lastCleanTime = sharedLastCleanTime.getLong("LAST_CLEAN_DB_TIME", 0)
        if (System.currentTimeMillis() - lastCleanTime < cleanInterval) {
            CELog.w("Within interval, no need to clean database, time to next clean time::" + (cleanInterval - (System.currentTimeMillis() - lastCleanTime)) / 60000 + " mins")
            return
        }
        for (name in SdkLib.getAppContext().databaseList()) {
            if (SdkLib.getAppContext().deleteDatabase(name)) {
                CELog.w(
                    "success deleted db:$name"
                )
            } else {
                CELog.w("failed to deleted db:$name")
            }
        }
        SystemKit.changeTenant(
            this,
            TokenPref.getInstance(SdkLib.getAppContext()).cpCurrentTenant,
            true
        )
    }

    private fun switchBottomTab(tab: BottomTab) {
        if (bottomTabAdapter.isCurrentTab(tab.type)) {
            if (tab.type == BottomTabEnum.MAIN) {
                mainRoomListFragment.scrollToTop() // 一般聊天列表重複點擊事件
            }
            return
        }
        val title =
            tab.title.ifEmpty {
                bottomTabAdapter.getTabTitle(tab.type)
            }

        binding.toolBar.tvToolBarTitle.text = title
        hideAllFragment()
        when (tab.type) {
            BottomTabEnum.MAIN -> {
                if (!tab.isSelected) {
                    ToastUtils.showToast(this, "切換至聊天室")
                }
                binding.toolBar.ivToolBarDownIcon.visibility = View.GONE
                binding.toolBar.ivScanning.visibility = View.VISIBLE
                showFragment(mainRoomListFragment, title)
            }

            BottomTabEnum.SERVICE -> {
                if (!tab.isSelected) {
                    ToastUtils.showToast(this, "切換至服務號，請勿做私人聊天用途")
                    homeViewModel.sendRefreshListByAPI.postValue(true) // refresh service number latest data
                } else {
                    serviceRoomListFragment.scrollToTop()
                }
                binding.toolBar.ivToolBarDownIcon.visibility = View.VISIBLE
                binding.toolBar.ivScanning.visibility = View.VISIBLE
                showFragment(serviceRoomListFragment, title)
            }

            BottomTabEnum.CONTACT -> {
                homeViewModel.getContactAccountSize()
                binding.toolBar.ivToolBarDownIcon.visibility = View.GONE
                binding.toolBar.ivScanning.visibility = View.VISIBLE
                showFragment(contactPersonFragment, title)
            }

            BottomTabEnum.TODO -> {
                binding.toolBar.ivToolBarDownIcon.visibility = View.GONE
                todoOverviewFragment.refreshNowTime()
                showFragment(todoOverviewFragment, title)
            }
        }
    }

    @SuppressLint("CommitTransaction")
    private fun showFragment(
        fragment: Fragment,
        tag: String
    ) {
        if (!fragment.isAdded && supportFragmentManager.findFragmentByTag(tag) == null) {
            supportFragmentManager.executePendingTransactions()
            supportFragmentManager
                .beginTransaction()
                .add(R.id.contentFL, fragment, tag)
                .commitAllowingStateLoss()
        }
        supportFragmentManager.beginTransaction().show(fragment).commitAllowingStateLoss()
    }

    @SuppressLint("CommitTransaction")
    private fun hideAllFragment() {
        val fragmentManager = supportFragmentManager
        for (fragment in fragmentManager.fragments) {
            if (fragment !is NavHostFragment) {
                fragmentManager.beginTransaction().hide(fragment).commitAllowingStateLoss()
            }
        }
    }

    private fun showTokenInvalidDialog() {
        if (SystemConfig.isCpMode) return
        SystemKit.cleanCE()
        App.getInstance().currentActivity()?.let {
            AlertView
                .Builder()
                .setContext(it)
                .setStyle(AlertView.Style.Alert)
                .setTitle(getString(R.string.alert_prompt))
                .setMessage("您的帳號已在其他設備上登錄")
                .setOthers(arrayOf(getString(R.string.alert_cancel)))
                .setOnItemClickListener { o, position ->
                    val intent = Intent(it, LoginCpActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK + Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    ActivityManager.findBaseActivity()
                    ActivityManager.finishAll()
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out)
                }.build()
                .show()
        }
    }

    private fun doServiceTabToolBarClick() {
        if (!bottomTabAdapter.isCurrentTab(BottomTabEnum.SERVICE)) return
        AlertView
            .Builder()
            .setContext(this)
            .setStyle(AlertView.Style.ActionSheet)
            .setMessage(getString(R.string.service_room_switch_sort_title))
            .setOthers(
                arrayOf(
                    getString(R.string.service_room_switch_to_category_sort),
                    getString(R.string.service_room_switch_to_time_sort)
                )
            ).setOnItemClickListener { _, position ->
                // position == 0 依照分類
                // position == 1 依照時間
                val serviceNumberSortType = ServiceNumberSortType.of(position)
                serviceRoomListFragment.doSwitchSortMode(serviceNumberSortType)
            }.build()
            .setCancelable(true)
            .show()
    }

    override fun onStart() {
        super.onStart()
        homeViewModel.isFirstLoading = intent.getBooleanExtra(BundleKey.IS_FIRST_LOADING.key(), false)
    }

    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.IO).launch {
            tenantViewModel.getTenantList()
            if (!NetworkUtils.isNetworkAvailable(this@HomeActivity)) {
                homeViewModel.sendRefreshDB.postValue(true)
                homeViewModel.sendMainChatRoomListRefreshByDB.emit(Unit)
                CoroutineScope(Dispatchers.Main).launch {
                    binding.noInternet.root.visibility = View.VISIBLE
                }
            } else {
                val intent = intent
                intent.getStringExtra(BundleKey.SCANNER_TYPE.key())?.takeIf {
                    it == ScannerType.FirstJoinTenant.name
                } ?: tenantViewModel.getTenantGuarantorList()
                intent.removeExtra(BundleKey.SCANNER_TYPE.key())
                this@HomeActivity.intent = intent
            }

            UserPref.getInstance(this@HomeActivity).removeAllCurrentRoomId()
        }
        homeViewModel.getShouldShowServiceNumberTab()
    }

    override fun onStop() {
        super.onStop()
        homeViewModel.savePreloadStep()
    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityManager.removeBaseActivity(this)
        if (App.APP_STATUS == App.APP_STATUS_NORMAL) {
            unregisterReceiver()
        }
        FCMTokenManager.release()
    }

    override fun onRestart() {
        super.onRestart()
        homeViewModel.sendRefreshListByAPI.postValue(true) // 從背景切回前景刷新列表
    }

    private fun unregisterReceiver() {
        try {
            unregisterReceiver(networkChangeReceiver)
            unregisterReceiver(headsetPlugReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onKeyDown(
        keyCode: Int,
        event: KeyEvent?
    ): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return if (exitTime == 0L) {
                ToastUtils.showToast(this, "再按一次退出程序")
                exitTime = System.currentTimeMillis()
                true
            } else {
                finish()
                false
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handleEvent(event: EventMsg<Any>) {
        try {
            when (event.code) {
                MsgConstant.SESSION_UPDATE_FILTER, MsgConstant.SYNC_READ, MsgConstant.BADGE_UPDATE_FILTER -> {
                    ChatRoomService.getInstance().getBadge(this)
                }

                MsgConstant.TOKEN_INVALID_FILTER -> {
                    showTokenInvalidDialog()
                }

                MsgConstant.SEND_NOTIFICATION_FILTER -> {
                    if (Strings.isNullOrEmpty(event.string)) return
                    val messageEntity =
                        JsonHelper.getInstance().from(event.string, MessageEntity::class.java)
                    if (MessageType.BROADCAST == messageEntity.type) return
                    val localEntity =
                        ChatRoomReference
                            .getInstance()
                            .findById2("", messageEntity.roomId, false, false, true, false, false)
                    NotifyHelper.sendMessageNewNotify(this, localEntity, messageEntity)
                }

                MsgConstant.MSG_RECEIVED_FILTER -> {
                    val receiverMessage =
                        JsonHelper.getInstance().from(event.string, MessageEntity::class.java)
                    receiverMessage?.let {
                        if (MessageFlag.RETRACT == it.flag) {
                            NotifyHelper.sendMessageNewNotify(this, null, receiverMessage)
                        }
                    }
                }

                MsgConstant.UPDATE_ALL_BADGE_NUMBER_EVENT -> {
                    val badge = UserPref.getInstance(this).brand
                    BadgeUtil().setCount(this@HomeActivity, min(badge, 999))
                }

                MsgConstant.UPDATE_MAIN_BADGE_NUMBER_EVENT -> {
                    homeViewModel.getChatRoomListUnReadSum()
                }

                MsgConstant.UPDATE_SERVICE_BADGE_NUMBER_EVENT -> {
                    homeViewModel.getServiceRoomUnReadSum()
                }

                MsgConstant.UPDATE_TODO_EXPIRED_COUNT_EVENT -> {
                    bottomTabAdapter.setUnReadNum(event.data.toString().toInt(), BottomTabEnum.TODO)
                }

                MsgConstant.INTERNET_STSTE_FILTER -> {
                    val isInternet = event.data as Boolean
                    binding.noInternet.getRoot().visibility = if (isInternet) View.GONE else View.VISIBLE
                }

                MsgConstant.UI_NOTICE_TODO_UPDATE_ALARM, MsgConstant.UI_NOTICE_TODO_DELETE_ALARM -> {
                    TodoAlarmTimerService.setAlarmTimer(
                        this,
                        AlarmValues.TIMER_ACTION,
                        AlarmManager.RTC_WAKEUP,
                        JsonHelper.getInstance().from(event.string, TodoEntity::class.java)
                    )
                }

                MsgConstant.UI_NOTICE_TO_TODO_ITEM -> {
                    val data = JsonHelper.getInstance().from(event.string, TodoEntity::class.java)
                    switchBottomTab(BottomTab(BottomTabEnum.TODO))
                    todoOverviewFragment.selectToTodoItem(data)
                }

                MsgConstant.REFRESH_FCM_TOKEN_ID -> {
                    FCMTokenManager.refreshFCMTokenIdToRemote(this)
                }

//                MsgConstant.SWITCH_BASE_STATUS_BAR_COLOR -> {
//                    if (event.data is Int) {
//                        window.statusBarColor = event.data as Int
//                    }
//                }

                MsgConstant.REFRESH_ROOM_BY_LOCAL -> {
                    EventBusUtils.sendEvent(EventMsg<Any>(MsgConstant.GROUP_REFRESH_FILTER))
                }

                MsgConstant.NOTICE_PLAY_NOTIFY_TONE -> {
                    val ringName = event.string
                    NotifyHelper.playNotifyTone(this, ringName)
                }

                MsgConstant.CLEAN_DB_AND_RELOAD -> {
                    cleanDBAndRetrieveData()
                }

                // 被移除服務號或是加入服務號 是否顯示服務號 tab
                MsgConstant.DELETE_SERVICE_NUMBER_MEMBER, MsgConstant.NOTICE_SERVICE_NUMBER_REFRESH_BY_DB -> {
                    homeViewModel.getShouldShowServiceNumberTab()
                }

                MsgConstant.NOTICE_GUARANTOR_JOIN -> {
                    reGuarantorJoin(event.data as GuarantorJoinContent)
                }
                // 服務號遭禁用
                MsgConstant.NOTICE_SERVICE_NUMBER_DISABLE -> {
                    val broadcastRoomId = event.data as String
                    homeViewModel.disableServiceNumber(broadcastRoomId)
                }

                MsgConstant.FACEBOOK_COMMENT_UPDATE -> {
                    val commentId = (event.data as JSONObject).optString("commentId")
                    val roomId = (event.data as JSONObject).optString("roomId")
                    homeViewModel.setFacebookCommentStatus(roomId, commentId, FacebookCommentStatus.Update)
                }

                MsgConstant.FACEBOOK_COMMENT_DELETE -> {
                    val commentId = (event.data as JSONObject).optString("commentId")
                    val roomId = (event.data as JSONObject).optString("roomId")
                    homeViewModel.setFacebookCommentStatus(roomId, commentId, FacebookCommentStatus.Delete)
                }

                MsgConstant.FACEBOOK_POST_DELETE -> {
                    val postId = (event.data as JSONObject).optString("postId")
                    val roomId = (event.data as JSONObject).optJSONArray("roomId")
                    homeViewModel.setFacebookPostStatus(roomId, postId, FacebookPostStatus.Delete)
                }

                MsgConstant.SERVICE_NUMBER_UPDATE -> {
                    val serviceNumberId = (event.data as String)
                    homeViewModel.checkIsBossServiceNumberOwnerModify(serviceNumberId)
                }

                MsgConstant.NOTICE_UPDATE_AVATARS -> {
                    val data = JsonHelper.getInstance().fromToMap<String, String>(event.string)
                    homeViewModel.updateUserAvatar(data)
                }

                MsgConstant.UPDATE_CUSTOMER_NAME -> {
                    homeViewModel.updateCustomer(event.data.toString())
                }
            }
        } catch (e: Exception) {
            CELog.e(e.message)
            e.printStackTrace()
        }
    }

    // 顯示被團隊刪除 Dialog
    private fun showTenantDeleteDialog(tenantDeleteMember: TenantDeleteMember) {
        AlertView
            .Builder()
            .setContext(this)
            .setStyle(AlertView.Style.Alert)
            .setTitle(String.format(getString(R.string.tenant_delete_member), tenantDeleteMember.tenantName))
            .setDestructive(getString(R.string.text_for_sure))
            .setOnItemClickListener { _: Any?, position: Int ->
                tenantViewModel.tenantDeleteMember(tenantDeleteMember)
            }.build()
            .setCancelable(false)
            .show()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onEvent(socketEvent: SocketEvent) {
        super.onEvent(socketEvent)
        when (socketEvent.type) {
            SocketEventEnum.TenantUnReadNum -> {
                tenantViewModel.getTenantList()
            }
            // 被移除團隊
            SocketEventEnum.TenantDeleteMember -> {
                homeViewModel.clearDataWhenLeaveTenant()
                val tenantDeleteMember = JsonHelper.getInstance().from(socketEvent.data, TenantDeleteMember::class.java)
                showTenantDeleteDialog(tenantDeleteMember)
            }

            else -> {}
        }
    }
}
