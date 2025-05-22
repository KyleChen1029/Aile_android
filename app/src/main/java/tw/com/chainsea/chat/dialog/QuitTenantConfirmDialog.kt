package tw.com.chainsea.chat.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.core.widget.addTextChangedListener
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.DialogQuitTenantConfirmBinding
import java.security.SecureRandom

class QuitTenantConfirmDialog(
    private val context: Context,
    private val callback: () -> Unit
) : Dialog(context) {
    private val binding: DialogQuitTenantConfirmBinding =
        DialogQuitTenantConfirmBinding.inflate(layoutInflater)
    private val random = SecureRandom()
    private val randomVerifyCode by lazy {
        val min = 10000
        val max = 99999
        val randomNum: Int = min + random.nextInt(max - min + 1)
        random.nextInt(randomNum)
    }

    init {
        setContentView(binding.root)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCancelable(false)
        setRandomCode()
        initListener()
    }

    private fun setRandomCode() {
        binding.tvMessage.text = context.getString(R.string.text_quit_tenant_message, randomVerifyCode)
    }

    private fun initListener() {
        binding.tvCancel.setOnClickListener { dismiss() }
        binding.tvConfirm.setOnClickListener {
            if (checkIsPass()) {
                dismiss()
                callback.invoke()
            }
        }

        binding.cbCheckQuit.setOnCheckedChangeListener { compoundButton, b ->
            checkIsPass()
        }
        binding.etVerifyCode.addTextChangedListener {
            checkIsPass()
        }
    }

    private fun checkIsPass(): Boolean {
        binding.tvConfirm.isEnabled = false
        if (!binding.cbCheckQuit.isChecked) return false
        if (binding.etVerifyCode.text.toString() != randomVerifyCode.toString()) return false
        binding.tvConfirm.isEnabled = true
        return true
    }
}
