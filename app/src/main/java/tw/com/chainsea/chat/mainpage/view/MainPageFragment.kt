package tw.com.chainsea.chat.mainpage.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.common.collect.Lists
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import tw.com.chainsea.android.common.json.JsonHelper
import tw.com.chainsea.android.common.ui.UiHelper
import tw.com.chainsea.ce.sdk.bean.GroupRefreshBean
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType
import tw.com.chainsea.ce.sdk.bean.servicenumber.type.GroupPrivilegeEnum
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.event.EventBusUtils
import tw.com.chainsea.ce.sdk.event.EventMsg
import tw.com.chainsea.ce.sdk.event.MsgConstant
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.base.Constant
import tw.com.chainsea.chat.config.BundleKey
import tw.com.chainsea.chat.config.InvitationType
import tw.com.chainsea.chat.databinding.FragmentMultiplePeopleMainPageBinding
import tw.com.chainsea.chat.lib.NetworkUtils
import tw.com.chainsea.chat.lib.ToastUtils
import tw.com.chainsea.chat.mainpage.model.MemberRoomType
import tw.com.chainsea.chat.mainpage.model.MoreMembersEntity
import tw.com.chainsea.chat.mainpage.model.NewMemberEntity
import tw.com.chainsea.chat.mainpage.viewmodel.MainPageViewModel
import tw.com.chainsea.chat.searchfilter.view.activity.MemberInvitationActivity
import tw.com.chainsea.chat.service.ActivityTransitionsControl
import tw.com.chainsea.chat.ui.activity.ClipImageActivity
import tw.com.chainsea.chat.ui.adapter.MainPageMemberListAdapter
import tw.com.chainsea.chat.ui.dialog.BottomSheetDialogBuilder
import tw.com.chainsea.chat.util.NoDoubleClickListener

class MainPageFragment : Fragment() {
    private val mainPageViewModel by activityViewModels<MainPageViewModel>()
    private lateinit var binding: FragmentMultiplePeopleMainPageBinding
    private lateinit var mainPageMemberListAdapter: MainPageMemberListAdapter
    private var memberItemCount = 5
    private val wholeMemberList: MutableList<Any> = Lists.newArrayList() // 暫存所有成員列表3 row - 1 or 2資料
    private val addMemberARL =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let {
                    val bundle = it.extras
                    val listType =
                        object :
                            TypeToken<ArrayList<UserProfileEntity?>?>() {}.type
                    val data =
                        JsonHelper.getInstance().from<List<UserProfileEntity>>(
                            bundle?.getString("data"),
                            listType
                        )
                    onInviteSuccess(data)
                }
            }
        }
    private var type = ""
    private lateinit var updateChatNameDialogFragment: UpdateChatNameDialogFragment
    private val userId by lazy {
        TokenPref.getInstance(requireActivity()).userId
    }
    private val mutex = Mutex()

    companion object {
        fun newInstance() = MainPageFragment()
    }

    private val permissions: Array<String> =
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
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
                        showToast(R.string.text_need_camera_permission)
                    } else if (permission == Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                        showToast(R.string.text_need_storage_permission)
                    }
                    return@registerForActivityResult
                }
            }
            when (mainPageViewModel.photoType.value) {
                MainPageViewModel.PhotoType.AVATAR -> {
                    showUpLoadAvatarDialog()
                }
                MainPageViewModel.PhotoType.BACKGROUND -> {
                    showUpLoadBackgroundDialog()
                }
                else -> {}
            }
        }
    private val registerPermissionTIRAMISU =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            result.forEach { (permission, isGranted) ->
                if (!isGranted) {
                    if (permission == Manifest.permission.CAMERA) {
                        showToast(R.string.text_need_camera_permission)
                    } else if (permission == Manifest.permission.READ_MEDIA_IMAGES) {
                        showToast(R.string.text_need_storage_permission)
                    }
                    return@registerForActivityResult
                }
            }
            when (mainPageViewModel.photoType.value) {
                MainPageViewModel.PhotoType.AVATAR -> {
                    showUpLoadAvatarDialog()
                }
                MainPageViewModel.PhotoType.BACKGROUND -> {
                    showUpLoadBackgroundDialog()
                }
                else -> {}
            }
        }
    private val uploadAvatarARL =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val groupAvatarName = result.data?.getStringExtra(BundleKey.RESULT_PIC_URI.key()).toString()
                if (NetworkUtils.isNetworkAvailable(requireContext())) {
                    mainPageViewModel.doUploadAvatar(
                        groupAvatarName,
                        TokenPref.getInstance(requireContext()).tokenId,
                        mainPageViewModel.chatRoomId.value
                    )
                } else {
                    ToastUtils.showToast(requireContext(), getString(R.string.network_error))
                }
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    setProgressBarMessage(R.string.text_upload_image_failure)
                }
            }
        }
    private val uploadBackgroundARL =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val groupBackgroundName = result.data?.getStringExtra(BundleKey.RESULT_PIC_URI.key()).toString()
                if (NetworkUtils.isNetworkAvailable(requireContext())) {
                    CoroutineScope(Dispatchers.IO).launch {
                        setProgressBarMessage(R.string.text_uploading_image)
                    }
                    mainPageViewModel.doUploadBackground(
                        groupBackgroundName,
                        TokenPref.getInstance(requireContext()).tokenId,
                        mainPageViewModel.chatRoomId.value
                    )
                } else {
                    ToastUtils.showToast(requireContext(), getString(R.string.network_error))
                }
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    setProgressBarMessage(R.string.text_upload_image_failure)
                }
            }
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
        .inflate<FragmentMultiplePeopleMainPageBinding>(
            inflater,
            R.layout.fragment_multiple_people_main_page,
            container,
            false
        ).apply {
            binding = this
            init()
            viewModel =
                this@MainPageFragment.mainPageViewModel.apply {
                    sendNavigateToPage
                        .onEach {
                            ActivityTransitionsControl.navigateToEmployeeHomePage(
                                requireActivity(),
                                it.id,
                                it.userType
                            ) { i, _ -> startActivity(i) }
                        }.launchIn(viewLifecycleOwner.lifecycleScope)

                    sendInviteNewMember
                        .onEach {
                            val intent =
                                Intent(requireActivity(), MemberInvitationActivity::class.java)
                                    .putExtra(BundleKey.ROOM_ID.key(), it.roomId)
                                    .putExtra(BundleKey.ACCOUNT_IDS.key(), getMemberIdsList(mainPageViewModel.memberList))
                                    .putExtra(BundleKey.ROOM_TYPE.key(), InvitationType.GroupRoom.name)
                            addMemberARL.launch(intent)
                        }.launchIn(viewLifecycleOwner.lifecycleScope)

                    sendUpdateRoomNameFailure
                        .onEach {
                            ToastUtils.showToast(requireActivity(), getString(R.string.text_chat_room_name_update_failure))
                        }.launchIn(viewLifecycleOwner.lifecycleScope)

                    sendUpdateRoomNameSuccess
                        .onEach {
                            updateChatNameDialogFragment.dismiss()
                            tvGroupName.text = it.second.name
                            chatRoomName.value = it.second.name
                            if (it.first) {
                                ToastUtils.showToast(requireActivity(), getString(R.string.text_chat_room_name_update_success))
                            }
                        }.launchIn(viewLifecycleOwner.lifecycleScope)

                    showAllMembers
                        .onEach {
                            findNavController().navigate(R.id.action_MultiplePeopleMainPageFragment_to_AllMembersFragment)
                        }.launchIn(viewLifecycleOwner.lifecycleScope)
                }

            ivBack.setOnClickListener {
                requireParentFragment().requireActivity().finish()
            }
            ivSettings.setOnClickListener {
                findNavController().navigate(R.id.action_MultiplePeopleMainPageFragment_to_settingsFragment)
            }
            val screenWidth = UiHelper.getDisplayWidth(requireActivity())
            memberItemCount =
                when (screenWidth) {
                    in 0..1300 -> {
                        6
                    }

                    else -> {
                        7
                    }
                }
            rvMemberList.layoutManager = GridLayoutManager(requireActivity(), memberItemCount)

            cameraBackground.setOnClickListener(
                object : NoDoubleClickListener() {
                    override fun onNoDoubleClick(v: View?) {
                        mainPageViewModel.photoType.value = MainPageViewModel.PhotoType.BACKGROUND
                        doOnOpenCameraAndGallery()
                    }
                }
            )

            cameraAvatar.setOnClickListener(
                object : NoDoubleClickListener() {
                    override fun onNoDoubleClick(v: View?) {
                        mainPageViewModel.photoType.value = MainPageViewModel.PhotoType.AVATAR
                        doOnOpenCameraAndGallery()
                    }
                }
            )
            lifecycleOwner = this@MainPageFragment.viewLifecycleOwner
        }.root

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        mainPageViewModel.apply {
            arguments?.let {
                chatRoomId.value = it.getString(BundleKey.ROOM_ID.key()) ?: ""
                type = it.getString(BundleKey.ROOM_TYPE.key()).toString()
            }
            binding.ivBackground.setColorFilter(ContextCompat.getColor(requireContext(), R.color.color_4C000000)) // 加上背景圖片遮罩

            sendUploadMessage
                .onEach {
                    setProgressBarMessage(it)
                }.launchIn(viewLifecycleOwner.lifecycleScope)

            sendHomePagePics.observe(viewLifecycleOwner) { url ->
                try {
                    Glide
                        .with(requireActivity())
                        .load(url)
                        .dontTransform()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .error(R.drawable.back_c)
                        .into(binding.ivBackground)
                } catch (ignored: Exception) {
                }
            }

            sendUpdateRoomAvatarId
                .onEach {
                    binding.civGroupAvatar.loadAvatarIcon(it, entity.value.name, entity.value.id)
                    EventBusUtils.sendEvent(
                        EventMsg<Any?>(MsgConstant.GROUP_REFRESH_FILTER, GroupRefreshBean(chatRoomId.value, null, null))
                    )
                    setProgressBarMessage(R.string.text_update_image_success)
                }.launchIn(viewLifecycleOwner.lifecycleScope)

            sendSortedMemberList
                .onEach {
                    initRoomInfo(it.first, it.second)
                }.launchIn(viewLifecycleOwner.lifecycleScope)
        }
    }

    override fun onResume() {
        super.onResume()
        mainPageViewModel.apply {
            getChatRoomEntity(userId, chatRoomId.value)
        }
    }

    private fun checkForSelfPermissions(permission: Array<String>): Boolean {
        for (i in permission.indices) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    permission[i]
                ) == PackageManager.PERMISSION_DENIED
            ) {
                return false
            }
        }
        return true
    }

    // 社團背景圖上傳
    private fun showUpLoadBackgroundDialog() {
        BottomSheetDialogBuilder(requireActivity(), layoutInflater)
            .getUploadBackgroundPicture(
                albumCallback = {
                    val intent = Intent(requireContext(), ClipImageActivity::class.java)
                    intent.putExtra(
                        Constant.INTENT_IMAGE_GET_WAY,
                        Constant.INTENT_CODE_ALBUM
                    )
                    intent.putExtra(
                        Constant.INTENT_IMAGE_CONFIRM_TYPE,
                        Constant.INTENT_CODE_CROP
                    )
                    uploadBackgroundARL.launch(intent)
                },
                cameraCallback = {
                    val intent = Intent(requireContext(), ClipImageActivity::class.java)
                    intent.putExtra(
                        Constant.INTENT_IMAGE_GET_WAY,
                        Constant.INTENT_CODE_CAMERA
                    )
                    intent.putExtra(
                        Constant.INTENT_IMAGE_CONFIRM_TYPE,
                        Constant.INTENT_CODE_CROP
                    )
                    uploadBackgroundARL.launch(intent)
                }
            ).show()
    }

    // 社團頭圖上傳
    private fun showUpLoadAvatarDialog() {
        BottomSheetDialogBuilder(requireActivity(), layoutInflater)
            .getUploadAvatar(
                albumCallback = {
                    val intent = Intent(requireContext(), ClipImageActivity::class.java)
                    intent.putExtra(
                        Constant.INTENT_IMAGE_GET_WAY,
                        Constant.INTENT_CODE_ALBUM
                    )
                    intent.putExtra(
                        Constant.INTENT_IMAGE_CONFIRM_TYPE,
                        Constant.INTENT_CODE_CROP
                    )
                    uploadAvatarARL.launch(intent)
                },
                cameraCallback = {
                    val intent = Intent(requireContext(), ClipImageActivity::class.java)
                    intent.putExtra(
                        Constant.INTENT_IMAGE_GET_WAY,
                        Constant.INTENT_CODE_CAMERA
                    )
                    intent.putExtra(
                        Constant.INTENT_IMAGE_CONFIRM_TYPE,
                        Constant.INTENT_CODE_CROP
                    )
                    uploadAvatarARL.launch(intent)
                }
            ).show()
    }

    private fun doOnOpenCameraAndGallery() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (!checkForSelfPermissions(permissions)) {
                registerPermission.launch(permissions)
            } else {
                when (mainPageViewModel.photoType.value) {
                    MainPageViewModel.PhotoType.AVATAR -> {
                        showUpLoadAvatarDialog()
                    }
                    MainPageViewModel.PhotoType.BACKGROUND -> {
                        showUpLoadBackgroundDialog()
                    }
                    else -> {}
                }
            }
        } else {
            if (!checkForSelfPermissions(permissionsTIRAMISU)) {
                registerPermissionTIRAMISU.launch(permissionsTIRAMISU)
            } else {
                when (mainPageViewModel.photoType.value) {
                    MainPageViewModel.PhotoType.AVATAR -> {
                        showUpLoadAvatarDialog()
                    }
                    MainPageViewModel.PhotoType.BACKGROUND -> {
                        showUpLoadBackgroundDialog()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun initRoomInfo(
        item: ChatRoomEntity,
        memberList: List<UserProfileEntity>
    ) {
        wholeMemberList.clear()
        wholeMemberList.addAll(memberList)
        when (item.type) {
            ChatRoomType.group -> { // 社團
                doHandleGroupRoomLogic(item, wholeMemberList)
            }
            ChatRoomType.discuss -> { // 多人聊天室
                doHandleDiscussRoomLogic(item, wholeMemberList)
            }
            else -> {}
        }
        mainPageViewModel.apply {
            if (item.name != null) {
                chatRoomName.value =
                    if (item.name.length > 20) item.name.substring(0, 20) else item.name
            }
            entity.value = item
            binding.ivSettings.visibility = View.VISIBLE // 設定頁需要先取得entity資料才能正常顯示
        }

        // 頭圖顯示 多人聊天室/社團
        if (item.type == ChatRoomType.discuss) {
            binding.civGroupAvatar.getChatRoomMemberIdsAndLoadMultiAvatarIcon(
                item.chatRoomMember,
                item.id
            )
        } else {
            binding.civGroupAvatar.loadAvatarIcon(
                item.avatarId,
                item.name,
                item.id
            )
        }
        mainPageMemberListAdapter.setData(wholeMemberList)

        if (binding.ivEdit.visibility == View.VISIBLE) {
            binding.scopeEditGroupName.setOnClickListener {
                updateChatNameDialogFragment =
                    UpdateChatNameDialogFragment(
                        mainPageViewModel.chatRoomName.value,
                        item.type,
                        onConfirm = {
                            mainPageViewModel.apply {
                                doUpdateChatRoomName(it, chatRoomId.value)
                            }
                        }
                    )
                updateChatNameDialogFragment.show(this@MainPageFragment.parentFragmentManager, null)
            }
        }

        binding.tvMemberNumber.text = getString(R.string.text_member_number, memberList.ifEmpty { mainPageViewModel.memberList }.size) // 更新成員數量
    }

    private fun doHandleGroupRoomLogic(
        item: ChatRoomEntity,
        members: MutableList<Any>
    ) {
        if (mainPageViewModel.privilege.value == GroupPrivilegeEnum.Owner || mainPageViewModel.privilege.value == GroupPrivilegeEnum.Manager) {
            // 社團擁有者或管理者權限設定
            binding.apply {
                cameraAvatar.visibility = View.VISIBLE
                cameraBackground.visibility = View.VISIBLE
                ivEdit.visibility = View.VISIBLE
            }
            initOwnerOrManagerLayout(item, members)
        } else {
            initCommonLayout(item, members)
        }
    }

    /**
     * 社團擁有者/管理者/多人聊天室成員列表規則
     */
    private fun initOwnerOrManagerLayout(
        item: ChatRoomEntity,
        members: MutableList<Any>
    ) {
        var adjustMemberList: MutableList<Any> = members.map { it }.toMutableList()
        if (members.size > memberItemCount * 3 - 1) { // 管理者/擁有者角度∶ 當成員數大於3排時的處理，需要保留2個空位
            adjustMemberList = adjustMemberList.subList(0, memberItemCount * 3 - 2)

            val users =
                if (members.getOrNull(memberItemCount * 3 - 2) is UserProfileEntity) {
                    (members.getOrNull(memberItemCount * 3 - 2) as UserProfileEntity)
                } else {
                    null
                }

            adjustMemberList.add(
                MoreMembersEntity(
                    item.id,
                    if (item.type == ChatRoomType.group) MemberRoomType.Crowd else MemberRoomType.Discuss,
                    users?.avatarId ?: "",
                    users?.nickName ?: "",
                    getString(R.string.text_format_more_members, members.size - adjustMemberList.size)
                )
            )
        }
        wholeMemberList.clear()
        wholeMemberList.addAll(adjustMemberList)

        wholeMemberList.add(
            NewMemberEntity(
                item.id,
                if (item.type == ChatRoomType.group) MemberRoomType.Crowd else MemberRoomType.Discuss
            )
        )
    }

    private fun initCommonLayout(
        item: ChatRoomEntity,
        members: MutableList<Any>
    ) {
        if (members.size > memberItemCount * 3) { // 一般成員角度∶ 當成員數大於3排時的處理，需要保留1個空位給show all
            var adjustMemberList: MutableList<Any> = members.map { it }.toMutableList()

            val users =
                if (members.getOrNull(memberItemCount * 3 - 1) is UserProfileEntity) {
                    (members.getOrNull(memberItemCount * 3 - 1) as UserProfileEntity)
                } else {
                    null
                }

            adjustMemberList = adjustMemberList.subList(0, memberItemCount * 3 - 1)
            adjustMemberList.add(
                MoreMembersEntity(
                    item.id,
                    MemberRoomType.Crowd,
                    users?.avatarId ?: "",
                    users?.nickName ?: "",
                    getString(R.string.text_format_more_members, members.size - adjustMemberList.size)
                )
            )
            wholeMemberList.clear()
            wholeMemberList.addAll(adjustMemberList)
        }
    }

    private fun doHandleDiscussRoomLogic(
        item: ChatRoomEntity,
        members: MutableList<Any>
    ) {
        binding.ivEdit.visibility = View.VISIBLE
        initOwnerOrManagerLayout(item, members)
    }

    private fun onInviteSuccess(data: List<UserProfileEntity>) {
        val transformMember: MutableList<Any> = mutableListOf()
        mainPageViewModel.memberList.addAll(data) // 新增成員，更新列表
        transformMember.addAll(mainPageViewModel.memberList)

        if ((mainPageViewModel.privilege.value == GroupPrivilegeEnum.Owner || mainPageViewModel.privilege.value == GroupPrivilegeEnum.Manager) || type == ChatRoomType.discuss.name) { // 社團擁有者or管理者or多人聊天室
            binding.apply {
                cameraAvatar.visibility = View.VISIBLE
                cameraBackground.visibility = View.VISIBLE
            }
            // 重新整理列表資料邏輯∶ 先移除最後的+號,最後的more，再把成員加上，最後再把+號加回去
            wholeMemberList.removeIf { it is NewMemberEntity } // 先移除最後的+號，
            wholeMemberList.removeIf { it is MoreMembersEntity } // 再移除最後的more，
            initOwnerOrManagerLayout(mainPageViewModel.entity.value, transformMember)
        } else {
            initCommonLayout(mainPageViewModel.entity.value, transformMember)
        }
        binding.tvMemberNumber.text = getString(R.string.text_member_number, mainPageViewModel.memberList.size) // 更新成員數量
        mainPageMemberListAdapter.setData(wholeMemberList)
        binding.rvMemberList.adapter = mainPageMemberListAdapter

        // setup discuss chat room avatar icon
        if (mainPageViewModel.entity.value.type == ChatRoomType.discuss) {
            val memberIds = mainPageViewModel.memberList.map { it.id }
            binding.civGroupAvatar.loadMultiAvatarIcon(
                memberIds,
                mainPageViewModel.entity.value.id
            )
        }

        showToast(R.string.text_invite_member_success)
    }

    private fun init() {
        mainPageMemberListAdapter = MainPageMemberListAdapter(mainPageViewModel)
        binding.rvMemberList.adapter = mainPageMemberListAdapter
    }

    private fun showToast(message: Int) {
        ToastUtils.showToast(requireActivity(), getString(message))
    }

    override fun onDestroy() {
        super.onDestroy()
        addMemberARL.unregister()
        registerPermission.unregister()
        registerPermissionTIRAMISU.unregister()
        uploadAvatarARL.unregister()
        uploadBackgroundARL.unregister()
    }

    private suspend fun setProgressBarMessage(msgResource: Int) {
        mutex.withLock {
            withContext(Dispatchers.Main) {
                binding.apply {
                    message.text = getString(msgResource)
                    if (msgResource == R.string.text_update_image_success || msgResource == R.string.text_upload_image_failure) {
                        delay(700L)
                        clLoadingProgress.visibility = View.GONE
                    } else {
                        clLoadingProgress.visibility = View.VISIBLE
                        delay(700L)
                    }
                }
            }
        }
    }
}
