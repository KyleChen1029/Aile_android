package tw.com.chainsea.chat.ui.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.common.collect.ImmutableMap
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import tw.com.chainsea.android.common.json.JsonHelper
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType
import tw.com.chainsea.ce.sdk.bean.servicenumber.type.GroupPrivilegeEnum
import tw.com.chainsea.ce.sdk.event.EventBusUtils
import tw.com.chainsea.ce.sdk.event.EventMsg
import tw.com.chainsea.ce.sdk.event.MsgConstant
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.config.BundleKey
import tw.com.chainsea.chat.config.InvitationType
import tw.com.chainsea.chat.databinding.FragmentAllMembersBinding
import tw.com.chainsea.chat.lib.ToastUtils
import tw.com.chainsea.chat.mainpage.viewmodel.MainPageViewModel
import tw.com.chainsea.chat.searchfilter.view.activity.MemberInvitationActivity
import tw.com.chainsea.chat.ui.adapter.entity.MainPageMemberListAdapter
import tw.com.chainsea.custom.view.alert.AlertView
import tw.com.chainsea.custom.view.alert.PrivilegeManagerDialog
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemTouchHelperCallback
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemTouchHelperExtension


class AllMembersFragment : Fragment() {

    private lateinit var binding : FragmentAllMembersBinding
    private val mainPageViewModel by activityViewModels<MainPageViewModel>()
    private val mainPageMemberListAdapter by lazy {
        MainPageMemberListAdapter(
            userId = mainPageViewModel.ownerId.value,
            showTransferOwnerDialog = { entity ->
                showTransferOwnerDialog(entity)
            },
            showDesignateManagerDialog = { entity ->
                showDesignateManagerDialog(entity)
            },
            showDeleteDialog = { entity ->
                showDeleteMemberDialog(entity)
            },
            showCancelManagerDialog = { entity ->
                showCancelManagerDialog(entity)
            }
        )
    }


    private val addMemberARL = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let {
                onInviteSuccess()
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return createDataBindingView(inflater, container)
    }
    private fun createDataBindingView(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ) = DataBindingUtil.inflate<FragmentAllMembersBinding>(
        inflater,
        R.layout.fragment_all_members,
        container,
        false
    ).apply {
        binding = this
        initDiscussSwipe()
        rvMembersList.adapter = mainPageMemberListAdapter
        mainPageViewModel.apply {
            getChatRoomMember(chatRoomId.value, entity.value)

            sendSortedMemberList.onEach {
                initRoomInfo(it.first, it.second)
                //擁有者及管理者顯示新增成員按鈕
                ivAdd.visibility = if((privilege.value == GroupPrivilegeEnum.Owner || privilege.value == GroupPrivilegeEnum.Manager) || it.first.type == ChatRoomType.discuss) View.VISIBLE else View.GONE
                btnLeave.text = getString(if(it.first.type == ChatRoomType.group) R.string.text_leave_crowd else R.string.text_exit_group)
                //只有一般成員顯示退出按鈕
                groupLeave.visibility = if(privilege.value == GroupPrivilegeEnum.Common || it.first.type == ChatRoomType.discuss) View.VISIBLE else View.GONE
                if(it.third) rvMembersList.adapter = mainPageMemberListAdapter //移轉擁有權後須renew權限滑塊
            }.launchIn(viewLifecycleOwner.lifecycleScope)

            isExitCrowdSuccess.onEach {
                EventBusUtils.sendEvent(EventMsg<Any?>(MsgConstant.REMOVE_GROUP_FILTER, it))
                requireActivity().finish()
            }.launchIn(viewLifecycleOwner.lifecycleScope)

            isExitDiscussRoomSuccess.onEach {
                EventBusUtils.sendEvent(EventMsg<Any?>(MsgConstant.MEMBER_EXIT_DISMISS_ROOM, it))
                requireActivity().finish()
            }.launchIn(viewLifecycleOwner.lifecycleScope)

            ivAdd.setOnClickListener {
                val intent = Intent(requireActivity(), MemberInvitationActivity::class.java)
                    .putExtra(BundleKey.ROOM_ID.key(), chatRoomId.value)
                    .putExtra(BundleKey.ACCOUNT_IDS.key(), getMemberIdsList(mainPageViewModel.memberList))
                    .putExtra(BundleKey.ROOM_TYPE.key(), InvitationType.GroupRoom.name)
                addMemberARL.launch(intent)
            }
            btnLeave.setOnClickListener {
                showLeaveCrowdDialog()
            }

            sendToast.onEach {
                showToast(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

            sendNoticeRoomOwnerChanged.onEach {
                val json = JsonHelper.getInstance().toJson<ImmutableMap<String, String>>(
                    ImmutableMap.of(
                        "key", "ownerId",
                        "values", it.second,
                        "roomId", it.first
                    )
                )
                EventBusUtils.sendEvent(
                    EventMsg<Any?>(
                        MsgConstant.SESSION_REFRESH_FILTER,
                        json
                    )
                )
            }.launchIn(viewLifecycleOwner.lifecycleScope)

            refreshGroupMemberPermission.onEach {
                initCrowdSwipe()
            }.launchIn(viewLifecycleOwner.lifecycleScope)
        }
        ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        etSearch.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

            override fun afterTextChanged(s: Editable?) {
                clearInput.apply {
                    if(visibility == View.GONE)
                        visibility = View.VISIBLE
                    if(s.toString().isEmpty())
                        visibility = View.GONE
                }
                mainPageViewModel.searchMember(s.toString())
            }
        })
        clearInput.setOnClickListener {
            etSearch.setText("")
            mainPageViewModel.apply {
                keyWord.value = "" //清空搜尋
                initAdapter(privilege.value, entity.value.type, memberList)
            }
        }
        lifecycleOwner = this@AllMembersFragment.viewLifecycleOwner
    }.root

    private fun initAdapter(privilegeEnum: GroupPrivilegeEnum, type: ChatRoomType, memberList: MutableList<UserProfileEntity>) {
        mainPageMemberListAdapter.setData(privilegeEnum, type, memberList)
    }
    private fun initCrowdSwipe() {
        if(mainPageViewModel.privilege.value != GroupPrivilegeEnum.Common) { //社團一般成員不觸發權限滑塊
            val mCallback = ItemTouchHelperCallback(ItemTouchHelper.START)
            val itemTouchHelper = ItemTouchHelperExtension(mCallback)
            itemTouchHelper.attachToRecyclerView(binding.rvMembersList)
            mainPageMemberListAdapter.setItemTouchHelperExtension(itemTouchHelper)
        }
    }

    private fun initDiscussSwipe() {
        if(mainPageViewModel.entity.value.type == ChatRoomType.discuss) {
            val mCallback = ItemTouchHelperCallback(ItemTouchHelper.START)
            val itemTouchHelper = ItemTouchHelperExtension(mCallback)
            itemTouchHelper.attachToRecyclerView(binding.rvMembersList)
            mainPageMemberListAdapter.setItemTouchHelperExtension(itemTouchHelper)
        }
    }

    private fun onInviteSuccess() {
        mainPageViewModel.apply {
            getChatRoomMember(chatRoomId.value, entity.value)
        }
        showToast(R.string.text_invite_member_success)
    }

    private fun showToast(message: Int) {
        ToastUtils.showToast(requireActivity(), getString(message))
    }

    private fun initRoomInfo(item: ChatRoomEntity, memberList: List<UserProfileEntity>) {
        binding.tvMemberCount.text = getString(R.string.text_member_number, memberList.size)
        initAdapter(mainPageViewModel.privilege.value, item.type, memberList.toMutableList())
    }

    private fun showDeleteMemberDialog(item: UserProfileEntity) {
        PrivilegeManagerDialog(requireContext()).getDeleteMemberDialog(getString(
            if(mainPageViewModel.entity.value.type == ChatRoomType.group)
                R.string.text_sure_to_delete_member_name
            else
                R.string.text_sure_to_delete_discuss_member_name
            , item.nickName)) {
            mainPageViewModel.doDeleteMember(item)
        }.show()
    }

    private fun showTransferOwnerDialog(item: UserProfileEntity) {
        PrivilegeManagerDialog(requireContext()).getTransferOwnerDialog(getString(R.string.text_sure_to_transfer_ownership, item.nickName)) {
            mainPageViewModel.doTransferChatRoomOwner(item)
        }.show()
    }

    private fun showDesignateManagerDialog(item: UserProfileEntity) {
        PrivilegeManagerDialog(requireContext()).getDesignateManagerDialog(getString(R.string.text_sure_to_designate_management, item.nickName)) {
            mainPageViewModel.doDesignateManager(item)
        }.show()
    }

    private fun showCancelManagerDialog(item: UserProfileEntity) {
        PrivilegeManagerDialog(requireContext()).getCancelManagerDialog(getString(R.string.text_sure_to_cancel_management, item.nickName)) {
            mainPageViewModel.doCancelManager(item)
        }.show()
    }

    private fun showLeaveCrowdDialog() {
        val intTitle = if(mainPageViewModel.entity.value.type == ChatRoomType.group)
            R.string.text_leave_crowd
        else
            R.string.text_exit_group_title
        AlertView.Builder().setContext(requireContext()).setStyle(AlertView.Style.Alert)
            .setTitle(getString(intTitle))
            .setMessage(getString(R.string.text_leave_crowd_tips))
            .setOthers(
                arrayOf(
                    getString(R.string.alert_cancel)
                )
            )
            .setDestructive(getString(R.string.alert_confirm))
            .setOnItemClickListener { o: Any?, position: Int ->
                if (position == 1) {
                    mainPageViewModel.doLeaveChatRoom()
                }
            }
            .build()
            .setCancelable(true)
            .setOnDismissListener(null)
            .show()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        mainPageViewModel.keyWord.value = "" //清空搜尋
        addMemberARL.unregister()
    }
}