package tw.com.chainsea.chat.mainpage.view

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.LayoutRoomNameUpdateBinding
import tw.com.chainsea.chat.lib.ToastUtils

class UpdateChatNameDialogFragment(
    private val roomName: String,
    private val type: ChatRoomType,
    private val onConfirm: (String) -> Unit
): DialogFragment() {


    override fun onCreateDialog(savedInstanceState: Bundle?)=
        AlertDialog.Builder(requireActivity()).setView(
            DataBindingUtil.inflate<LayoutRoomNameUpdateBinding>(
                requireActivity().layoutInflater,
                R.layout.layout_room_name_update,
                null,
                false
            ).apply {
                tvWordNumber.text = getString(R.string.text_chat_room_name_limit, roomName.length)
                tvTitle.text = if(type == ChatRoomType.discuss) getString(R.string.text_chat_room_modify_name) else getString(R.string.text_input_group_chat_room_name)
                etRoomName.hint = if(type == ChatRoomType.discuss) getString(R.string.text_input_discuss_chat_room_name_please) else getString(R.string.text_input_group_chat_room_name_please)
                etRoomName.setText(roomName)
                etRoomName.doOnTextChanged { text, _, _, _ ->
                    text?.let {
                        tvWordNumber.text = getString(R.string.text_chat_room_name_limit, it.length)
                    }
                }
                btnCancel.setOnClickListener {
                    dismiss()
                }
                btnConfirm.setOnClickListener {
                    etRoomName.text?.let {
                        if(it.isEmpty()){
                            ToastUtils.showToast(requireActivity(), if(type == ChatRoomType.discuss) getString(R.string.text_input_discuss_chat_room_name_please) else getString(R.string.text_input_group_chat_room_name_please))
                        }else
                            onConfirm.invoke(it.toString())
                    }
                }
                lifecycleOwner = this@UpdateChatNameDialogFragment
            }.root
        ).create().apply { setCanceledOnTouchOutside(true) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.setCancelable(true)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}