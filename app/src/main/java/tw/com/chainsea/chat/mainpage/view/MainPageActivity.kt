package tw.com.chainsea.chat.mainpage.view

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import tw.com.chainsea.android.common.json.JsonHelper
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.event.EventMsg
import tw.com.chainsea.ce.sdk.event.MsgConstant
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.config.BundleKey
import tw.com.chainsea.chat.databinding.ActivityMultiplePeopleMainPageBinding
import tw.com.chainsea.chat.lib.ToastUtils
import tw.com.chainsea.chat.mainpage.viewmodel.MainPageViewModel
import tw.com.chainsea.chat.network.contact.ViewModelFactory
import tw.com.chainsea.chat.view.BaseActivity

class MainPageActivity : BaseActivity() {
    private lateinit var viewModel: MainPageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityMultiplePeopleMainPageBinding>(
            this,
            R.layout.activity_multiple_people_main_page
        )
        initViewModel()
        viewModel.sendCloseMainPage
            .onEach {
                ToastUtils.showToast(this, getString(it))
                EventBus.getDefault().post(EventMsg<Any>(MsgConstant.NOTICE_CLOSE_OLD_ROOM))
                val navController = findNavController(R.id.nav_host_fragment_content_multiple_people_main_page)
                navController.popBackStack(R.id.MultiplePeopleMainPageFragment, true)
                navController.navigateUp()
                finish()
            }.launchIn(this@MainPageActivity.lifecycleScope)
    }

    private fun initViewModel() {
        val factory = ViewModelFactory(application)
        viewModel =
            ViewModelProvider(this, factory)[MainPageViewModel::class.java]
    }

    override fun onResume() {
        super.onResume()
        viewModel.navState?.let {
            val navController =
                findNavController(R.id.nav_host_fragment_content_multiple_people_main_page)
            navController.popBackStack(it, true)
            navController.navigate(it)
        }
    }

    override fun onStart() {
        super.onStart()
        intent?.let {
//            val listType = object : TypeToken<List<UserProfileEntity>>() {}.type
//            val memberList: List<UserProfileEntity> = JsonHelper.getInstance().from(
//                it.getStringExtra(BundleKey.MEMBERS_LIST.key()),
//                listType
//            )
//            viewModel.memberList.clear()
//            viewModel.memberList.addAll(memberList)
            val bundle =
                bundleOf(
                    BundleKey.ROOM_ID.key() to it.getStringExtra(BundleKey.ROOM_ID.key()),
                    BundleKey.ROOM_TYPE.key() to it.getStringExtra(BundleKey.ROOM_TYPE.key())
                )
            findNavController(R.id.nav_host_fragment_content_multiple_people_main_page).navigate(
                R.id.MultiplePeopleMainPageFragment,
                bundle
            )
        }
    }

    override fun onBackPressed() {
        val navController = findNavController(R.id.nav_host_fragment_content_multiple_people_main_page)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_multiple_people_main_page)
        val backStackEntryCount = navHostFragment?.childFragmentManager?.backStackEntryCount
        if (backStackEntryCount == 0 ||
            navController.currentDestination?.id == R.id.MultiplePeopleMainPageFragment
        ) {
            finish()
            return
        }

        navController.popBackStack()
        super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        val navController = findNavController(R.id.nav_host_fragment_content_multiple_people_main_page)
        // 當前fragment是mainPageFragment不用紀錄
        val currentDestinationId = navController.currentDestination?.id
        viewModel.navState = if (currentDestinationId != R.id.MultiplePeopleMainPageFragment) currentDestinationId else null
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
                    if (it.contains(TokenPref.getInstance(this).userId) && viewModel.entity.value.id == roomId) {
                        viewModel.doKickOutChatRoomByOtherMember(roomId)
                    }
                }
            }

            MsgConstant.NOTICE_FINISH_ACTIVITY -> {
                finish()
            }
        }
    }
}
