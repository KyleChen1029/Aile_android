package tw.com.chainsea.chat.searchfilter.view.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import tw.com.chainsea.android.common.event.KeyboardHelper
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.config.BundleKey
import tw.com.chainsea.chat.databinding.ActivityCreateDiscussBinding
import tw.com.chainsea.chat.lib.NetworkUtils
import tw.com.chainsea.chat.lib.ToastUtils
import tw.com.chainsea.chat.network.contact.ViewModelFactory
import tw.com.chainsea.chat.searchfilter.viewmodel.SearchFilterSharedViewModel
import tw.com.chainsea.chat.ui.activity.ChatNormalActivity
import tw.com.chainsea.chat.util.NoDoubleClickListener
import tw.com.chainsea.chat.view.BaseActivity
import tw.com.chainsea.custom.view.progress.IosProgressBar

class CreateDiscussActivity : BaseActivity() {
    // the data connection between SearchFilterFragment and top Activity(CreateDiscussActivity)
    private lateinit var searchFilterSharedViewModel: SearchFilterSharedViewModel
    private lateinit var binding: ActivityCreateDiscussBinding
    private lateinit var progressBar: IosProgressBar
    private var discussRoomName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityCreateDiscussBinding>(this, R.layout.activity_create_discuss).apply {
            binding = this
            init()
            viewModel =
                this@CreateDiscussActivity.searchFilterSharedViewModel.apply {
                    intent?.let {
                        val chatRoomMembers = it.getStringArrayListExtra(BundleKey.ACCOUNT_IDS.key())
                        chatRoomMembers?.let { members ->
                            userMemberIds.clear()
                            userMemberIds.addAll(members.toMutableList())
                            // 重組初始成員姓名
                            searchFilterSharedViewModel.appendDiscussRoomMemberName(members.toMutableList())
                        }
                    }
                    sharedPageType.value = SearchFilterSharedViewModel.SearchFilterPageType.CREATE_DISCUSS

                    sendMemberName
                        .onEach {
                            tieRoomName.hint = it.first
                            groupAvatarCIV.loadMultiAvatarIcon(it.third)
                        }.launchIn(this@CreateDiscussActivity.lifecycleScope)

                    selectedMemberList
                        .onEach {
                            btnSubmit.text = getString(R.string.text_create_group_submit, it.size.toString())
                            employeeSelectedList.clear()
                            employeeSelectedList.addAll(it)
                        }.launchIn(this@CreateDiscussActivity.lifecycleScope)

                    sendCreateDiscussRoomSuccess
                        .onEach {
                            ToastUtils.showToast(this@CreateDiscussActivity, getString(R.string.text_create_room_successfully))
                            val intent = Intent(this@CreateDiscussActivity, ChatNormalActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            intent.putExtra(BundleKey.EXTRA_SESSION_ID.key(), it.id)
                            startActivity(intent)
                            closePage()
                        }.launchIn(this@CreateDiscussActivity.lifecycleScope)

                    sendCreateDiscussRoomFailure
                        .onEach {
                            ToastUtils.showToast(this@CreateDiscussActivity, getString(it))
                            closePage()
                        }.launchIn(this@CreateDiscussActivity.lifecycleScope)
                }
            lifecycleOwner = this@CreateDiscussActivity
            tieRoomName.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                    }

                    override fun afterTextChanged(s: Editable?) {
                        s?.let {
                            discussRoomName = it.toString()
                        }
                    }
                }
            )
        }
    }

    private fun initViewModel() {
        val factory = ViewModelFactory(application)
        searchFilterSharedViewModel =
            ViewModelProvider(this, factory)[SearchFilterSharedViewModel::class.java]
    }

    private fun init() {
        initViewModel()
        binding.toolbar.apply {
            title.text = getString(R.string.text_create_discuss_chat_room)
            leftAction.setOnClickListener(
                object : NoDoubleClickListener() {
                    override fun onNoDoubleClick(v: View?) {
                        closePage()
                    }
                }
            )
        }
        binding.btnCancel.setOnClickListener(
            object : NoDoubleClickListener() {
                override fun onNoDoubleClick(v: View?) {
                    closePage()
                }
            }
        )
        binding.btnSubmit.setOnClickListener(
            object : NoDoubleClickListener() {
                override fun onNoDoubleClick(v: View?) {
                    createDiscussRoom()
                }
            }
        )
    }

    private fun createDiscussRoom() {
        if (NetworkUtils.isNetworkAvailable(this@CreateDiscussActivity)) {
            if (searchFilterSharedViewModel.discussMemberIds.size > 2) {
                showLoadingView()
                val memberIds = mutableListOf<String>()
                searchFilterSharedViewModel.discussMemberIds.forEach {
                    memberIds.add(it.key)
                }
                searchFilterSharedViewModel.doCreateRoom(
                    memberIds,
                    ChatRoomType.discuss,
                    discussRoomName.ifEmpty { "" },
                    discussRoomName.isNotEmpty()
                )
            } else {
                ToastUtils.showToast(this, getString(R.string.text_create_discuss_tip))
            }
        } else {
            ToastUtils.showToast(this@CreateDiscussActivity, getString(R.string.text_new_chat_room_failure))
        }
    }

    private fun closePage() {
        hideLoadingView()
        KeyboardHelper.hide(this@CreateDiscussActivity.currentFocus)
        finish()
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out)
    }

    private fun showLoadingView() {
        progressBar =
            IosProgressBar.show(
                this,
                getString(R.string.wording_loading),
                true,
                false
            ) { }
    }

    private fun hideLoadingView() {
        try {
            if (progressBar.isShowing) progressBar.dismiss()
        } catch (ignored: Exception) {
        }
    }
}
