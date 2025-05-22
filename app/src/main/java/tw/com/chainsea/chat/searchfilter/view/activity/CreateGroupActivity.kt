package tw.com.chainsea.chat.searchfilter.view.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import tw.com.chainsea.android.common.event.KeyboardHelper
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.event.EventBusUtils
import tw.com.chainsea.ce.sdk.event.EventMsg
import tw.com.chainsea.ce.sdk.event.MsgConstant
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.base.Constant
import tw.com.chainsea.chat.config.BundleKey
import tw.com.chainsea.chat.databinding.ActivityCreateGroupBinding
import tw.com.chainsea.chat.lib.NetworkUtils
import tw.com.chainsea.chat.lib.ToastUtils
import tw.com.chainsea.chat.network.contact.ViewModelFactory
import tw.com.chainsea.chat.searchfilter.viewmodel.SearchFilterSharedViewModel
import tw.com.chainsea.chat.ui.activity.ChatNormalActivity
import tw.com.chainsea.chat.ui.activity.ClipImageActivity
import tw.com.chainsea.chat.util.DaVinci
import tw.com.chainsea.chat.util.NoDoubleClickListener
import tw.com.chainsea.chat.view.BaseActivity
import tw.com.chainsea.custom.view.alert.AlertView
import tw.com.chainsea.custom.view.progress.IosProgressBar

class CreateGroupActivity : BaseActivity() {
    // the data connection between SearchFilterFragment and top Activity(CreateGroupActivity)
    private lateinit var searchFilterSharedViewModel: SearchFilterSharedViewModel
    private lateinit var binding: ActivityCreateGroupBinding
    private var canNext = false
    private var groupAvatarName: String = ""
    private lateinit var progressBar: IosProgressBar
    private val roomId by lazy {
        intent.getStringExtra(BundleKey.ROOM_ID.key())
    }
    private val memberList by lazy {
        intent.getStringArrayListExtra(BundleKey.MEMBERS_LIST.key())
    }
    private val chatRoomName by lazy {
        intent.getStringExtra(BundleKey.CHAT_ROOM_NAME.key())
    }
    private val userId by lazy {
        TokenPref.getInstance(this).userId
    }
    private val permissions: Array<String> =
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val permissionsTIRAMISU: Array<String> =
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_MEDIA_IMAGES
        )
    private val registerPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            result.forEach { (permission, isGranted) ->
                if (!isGranted) {
                    if (permission == Manifest.permission.CAMERA) {
                        ToastUtils.showToast(this, getString(R.string.text_need_camera_permission))
                    } else if (permission == Manifest.permission.READ_EXTERNAL_STORAGE) {
                        ToastUtils.showToast(this, getString(R.string.text_need_storage_permission))
                    }
                    return@registerForActivityResult
                }
            }
            showPicDialog()
        }
    private val registerPermissionTIRAMISU =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            result.forEach { (permission, isGranted) ->
                if (!isGranted) {
                    if (permission == Manifest.permission.CAMERA) {
                        ToastUtils.showToast(this, getString(R.string.text_need_camera_permission))
                    } else if (permission == Manifest.permission.READ_MEDIA_IMAGES) {
                        ToastUtils.showToast(this, getString(R.string.text_need_storage_permission))
                    }
                    return@registerForActivityResult
                }
            }
            showPicDialog()
        }

    private val uploadImageARL =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.data != null) {
                groupAvatarName = result.data?.getStringExtra(BundleKey.RESULT_PIC_URI.key()).toString()
                if (NetworkUtils.isNetworkAvailable(this)) {
                    groupAvatarName.let {
                        val imageEntity = DaVinci.with().imageLoader.getImage(it)
                        val groupAvatarBitmap = imageEntity.bitmap
                        binding.groupAvatarCIV.setImageBitmap(groupAvatarBitmap)
                    }
                } else {
                    ToastUtils.showToast(this, getString(R.string.network_error))
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityCreateGroupBinding>(this, R.layout.activity_create_group).apply {
            binding = this
            init()
            viewModel =
                this@CreateGroupActivity.searchFilterSharedViewModel.apply {
                    sharedPageType.value =
                        if (roomId == null) {
                            // 沒帶roomId是創建社團，otherwise 轉為社團
                            SearchFilterSharedViewModel.SearchFilterPageType.CREATE_GROUP
                        } else {
                            SearchFilterSharedViewModel.SearchFilterPageType.TRANSFER_TO_GROUP
                        }

                    chatRoomName?.let {
                        etGroupName.value = it // 轉為社團，若聊天室名稱被改過 isCustomName=true，則自動帶入
                        tieRoomName.hint = it
                    }
                    memberList?.let {
                        userMemberIds.clear()
                        it.remove(userId)
                        userMemberIds.add(userId)
                        userMemberIds.addAll(it)
                    }

                    selectedMemberList
                        .onEach {
                            btnSubmit.text = getString(R.string.text_create_group_submit, it.size.toString())
                            employeeSelectedList.clear()
                            employeeSelectedList.addAll(it)
                        }.launchIn(this@CreateGroupActivity.lifecycleScope)

                    createGroupSuccessful
                        .onEach {
                            hideLoadingView()
                            ToastUtils.showToast(this@CreateGroupActivity, getString(R.string.text_create_group_successful))
                            EventBusUtils.sendEvent(EventMsg<Any>(MsgConstant.NOTICE_REFRESH_CHAT_ROOM_LIST))
                            val intent =
                                Intent(this@CreateGroupActivity, ChatNormalActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            intent.putExtra(BundleKey.EXTRA_SESSION_ID.key(), it)
                            intent.putExtra(BundleKey.EXTRA_TITLE.key(), etGroupName.value)
                            startActivity(intent)
                            closePage()
                        }.launchIn(this@CreateGroupActivity.lifecycleScope)
                    createGroupFailure
                        .onEach {
                            hideLoadingView()
                            ToastUtils.showToast(this@CreateGroupActivity, it)
                        }.launchIn(this@CreateGroupActivity.lifecycleScope)
                    sendTransferToCrowdRoomSuccess
                        .onEach {
                            hideLoadingView()
                            ToastUtils.showToast(this@CreateGroupActivity, getString(R.string.text_transfer_to_crowd_success))
                            val intent =
                                Intent(this@CreateGroupActivity, ChatNormalActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            intent.putExtra(BundleKey.EXTRA_SESSION_ID.key(), it)
                            intent.putExtra(BundleKey.EXTRA_TITLE.key(), etGroupName.value)
                            startActivity(intent)
                            closePage()
                        }.launchIn(this@CreateGroupActivity.lifecycleScope)
                    sendTransferToCrowdRoomFailure
                        .onEach {
                            hideLoadingView()
                            ToastUtils.showToast(this@CreateGroupActivity, getString(it))
                        }.launchIn(this@CreateGroupActivity.lifecycleScope)
                }
            lifecycleOwner = this@CreateGroupActivity
        }
    }

    fun onOpenGallery() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (!checkForSelfPermissions(permissions)) {
                registerPermission.launch(permissions)
            } else {
                showPicDialog()
            }
        } else {
            if (!checkForSelfPermissions(permissionsTIRAMISU)) {
                registerPermissionTIRAMISU.launch(permissionsTIRAMISU)
            } else {
                showPicDialog()
            }
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
            title.text = if (roomId == null) getString(R.string.text_create_crowd) else getString(R.string.text_transfer_to_crowd)
            leftAction.setOnClickListener(
                object : NoDoubleClickListener() {
                    override fun onNoDoubleClick(v: View?) {
                        closePage()
                    }
                }
            )
        }
        binding.cameraIV.setOnClickListener(
            object : NoDoubleClickListener() {
                override fun onNoDoubleClick(v: View?) {
                    onOpenGallery()
                }
            }
        )
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
                    doSubmitAction()
                }
            }
        )
    }

    private fun doSubmitAction() {
        if (groupAvatarName.isEmpty()) {
            AlertView
                .Builder()
                .setContext(this)
                .setStyle(AlertView.Style.Alert)
                .setTitleSize(16f)
                .setTitle(getString(R.string.text_upload_group_avatar_please))
                .setCancelText(getString(R.string.text_for_sure))
                .setOnItemClickListener { _: Any?, _: Int ->
                }.build()
                .setCancelable(true)
                .show()
        } else if (binding.tieRoomName.text
                .toString()
                .isEmpty() &&
            searchFilterSharedViewModel.etGroupName.value.isEmpty()
        ) { // 當帶過來的名稱和輸入框都為空時, 要求輸入
            AlertView
                .Builder()
                .setContext(this)
                .setStyle(AlertView.Style.Alert)
                .setTitleSize(16f)
                .setTitle(getString(R.string.text_input_group_name_please))
                .setCancelText(getString(R.string.text_for_sure))
                .setOnItemClickListener { _: Any?, _: Int ->
                }.build()
                .setCancelable(true)
                .show()
        } else if (searchFilterSharedViewModel.employeeSelectedList.size < 3) {
            AlertView
                .Builder()
                .setContext(this)
                .setStyle(AlertView.Style.Alert)
                .setTitleSize(16f)
                .setTitle(getString(R.string.text_group_member_issue))
                .setCancelText(getString(R.string.text_for_sure))
                .setOnItemClickListener { _: Any?, _: Int ->
                }.build()
                .setCancelable(true)
                .show()
        } else {
            showLoadingView()

            if (binding.tieRoomName.text?.isNotEmpty() == true) {
                // 當輸入框有文字時，更新etGroupName
                searchFilterSharedViewModel.etGroupName.value = binding.tieRoomName.text.toString()
            }

            when (searchFilterSharedViewModel.sharedPageType.value) {
                SearchFilterSharedViewModel.SearchFilterPageType.CREATE_GROUP -> searchFilterSharedViewModel.createGroup(groupAvatarName)
                SearchFilterSharedViewModel.SearchFilterPageType.TRANSFER_TO_GROUP -> {
                    roomId?.let {
                        searchFilterSharedViewModel.transferToGroup(groupAvatarName, it)
                    }
                }

                else -> {}
            }
        }
    }

    private fun closePage() {
        finish()
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out)
        KeyboardHelper.hide(this.currentFocus)
    }

    override fun onResume() {
        super.onResume()
        canNext = true
    }

    private fun showPicDialog() {
        if (canNext) {
            canNext = false
            AlertView
                .Builder()
                .setContext(this)
                .setStyle(AlertView.Style.ActionSheet)
                .setOthers(
                    arrayOf(
                        getString(R.string.warning_photos),
                        getString(R.string.warning_camera)
                    )
                ).setCancelText(getString(R.string.text_create_group_cancel))
                .setOnItemClickListener { o: Any?, position: Int ->
                    if (position == 0) {
                        val intent = Intent(this, ClipImageActivity::class.java)
                        intent.putExtra(
                            Constant.INTENT_IMAGE_GET_WAY,
                            Constant.INTENT_CODE_ALBUM
                        )
                        intent.putExtra(
                            Constant.INTENT_IMAGE_CONFIRM_TYPE,
                            Constant.INTENT_CODE_CROP
                        )
                        uploadImageARL.launch(intent)
                    } else if (position == 1) {
                        val intent = Intent(this, ClipImageActivity::class.java)
                        intent.putExtra(
                            Constant.INTENT_IMAGE_GET_WAY,
                            Constant.INTENT_CODE_CAMERA
                        )
                        intent.putExtra(
                            Constant.INTENT_IMAGE_CONFIRM_TYPE,
                            Constant.INTENT_CODE_CROP
                        )
                        uploadImageARL.launch(intent)
                    }
                }.build()
                .setOnDismissListener { canNext = true }
                .setCancelable(true)
                .show()
        }
    }

    private fun checkForSelfPermissions(permission: Array<String>): Boolean {
        for (i in permission.indices) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission[i]
                ) == PackageManager.PERMISSION_DENIED
            ) {
                return false
            }
        }
        return true
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
