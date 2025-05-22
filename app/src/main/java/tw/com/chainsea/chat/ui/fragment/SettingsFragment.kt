package tw.com.chainsea.chat.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType
import tw.com.chainsea.ce.sdk.bean.servicenumber.type.GroupPrivilegeEnum
import tw.com.chainsea.ce.sdk.event.EventBusUtils
import tw.com.chainsea.ce.sdk.event.EventMsg
import tw.com.chainsea.ce.sdk.event.MsgConstant
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.FragmentSettingsBinding
import tw.com.chainsea.chat.lib.ToastUtils
import tw.com.chainsea.chat.mainpage.viewmodel.MainPageViewModel
import tw.com.chainsea.custom.view.alert.AlertView


class SettingsFragment : Fragment() {

    private lateinit var binding : FragmentSettingsBinding
    private val mainPageViewModel by activityViewModels<MainPageViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return createDataBindingView(inflater, container)
    }
    private fun createDataBindingView(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ) = DataBindingUtil.inflate<FragmentSettingsBinding>(
        inflater,
        R.layout.fragment_settings,
        container,
        false
    ).apply {
        binding = this
        mainPageViewModel.apply {
            isExitCrowdSuccess.onEach {
                EventBusUtils.sendEvent(EventMsg<Any?>(MsgConstant.REMOVE_GROUP_FILTER, it))
                requireActivity().finish()
            }.launchIn(viewLifecycleOwner.lifecycleScope)

            isExitDiscussRoomSuccess.onEach {
                EventBusUtils.sendEvent(EventMsg<Any?>(MsgConstant.MEMBER_EXIT_DISMISS_ROOM, it))
                requireActivity().finish()
            }.launchIn(viewLifecycleOwner.lifecycleScope)

            sendToast.onEach {
                showToast(it)
                if(it == R.string.text_clear_chat_record_success) //清除聊天室紀錄
                    EventBusUtils.sendEvent(EventMsg<Any?>(MsgConstant.NOTICE_CLEAR_CHAT_ROOM_ALL_MESSAGE))
            }.launchIn(viewLifecycleOwner.lifecycleScope)
        }
        lifecycleOwner = this@SettingsFragment.viewLifecycleOwner
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainPageViewModel.apply {
            if(entity.value.type == ChatRoomType.group) {
                binding.tvDisband.text = if(privilege.value == GroupPrivilegeEnum.Owner) getString(R.string.text_disband_crowd) else getString(R.string.text_leave_crowd)
            } else if(entity.value.type == ChatRoomType.discuss) {
                binding.tvDisband.text = getString(R.string.text_exit_group)
            }

            binding.scopeExitChatRoom.setOnClickListener {
                if(entity.value.type == ChatRoomType.group) {
                    when(privilege.value) {
                        GroupPrivilegeEnum.Owner -> showDisbandDialog()
                        else -> showLeaveChatRoomDialog()
                    }
                } else if(entity.value.type == ChatRoomType.discuss) {
                    //退出聊天
                    showLeaveChatRoomDialog()
                }
            }
        }
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.scopeClearChatRecord.setOnClickListener {
            showClearMessageDialog()
        }
    }
    private fun showToast(message: Int) {
        ToastUtils.showToast(requireActivity(), getString(message))
    }

    private fun showClearMessageDialog() {
        AlertView.Builder().setContext(requireContext()).setStyle(AlertView.Style.Alert)
            .setTitle(getString(R.string.text_clear_chat_record))
            .setMessage(getString(R.string.text_clear_chat_record_tips))
            .setOthers(
                arrayOf(
                    getString(R.string.alert_cancel)
                )
            )
            .setDestructive(getString(R.string.alert_confirm))
            .setOnItemClickListener { o: Any?, position: Int ->
                if (position == 1) {
                    mainPageViewModel.doCleanMessage()
                }
            }
            .build()
            .setCancelable(true)
            .setOnDismissListener(null)
            .show()
    }
    private fun showDisbandDialog() {
        AlertView.Builder().setContext(requireContext()).setStyle(AlertView.Style.Alert)
            .setTitle(getString(R.string.text_disband_crowd))
            .setMessage(getString(R.string.text_disband_crowd_tips))
            .setOthers(
                arrayOf(
                    getString(R.string.alert_cancel)
                )
            )
            .setDestructive(getString(R.string.alert_confirm))
            .setOnItemClickListener { o: Any?, position: Int ->
                if (position == 1) {
                    mainPageViewModel.doDisbandCrowd()
                }
            }
            .build()
            .setCancelable(true)
            .setOnDismissListener(null)
            .show()
    }
    private fun showLeaveChatRoomDialog() {
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
}