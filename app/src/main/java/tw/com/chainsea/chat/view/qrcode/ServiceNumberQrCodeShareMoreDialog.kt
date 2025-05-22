package tw.com.chainsea.chat.view.qrcode

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Patterns.EMAIL_ADDRESS
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import com.bumptech.glide.Glide
import tw.com.chainsea.android.common.ui.UiHelper
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity
import tw.com.chainsea.ce.sdk.http.cp.respone.RelationTenant
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.DialogShareMoreBinding
import tw.com.chainsea.chat.util.IntentUtil
import tw.com.chainsea.chat.util.ThemeHelper
import java.util.regex.Pattern


class ServiceNumberQrCodeShareMoreDialog(context: Context) : Dialog(context) {

    private var binding: DialogShareMoreBinding = DialogShareMoreBinding.inflate(layoutInflater)
    private var qrCodeData: QrCodeData? = null
    private val emailPattern = EMAIL_ADDRESS.toString()
    private val phonePattern = "09\\d{2}(\\d{6})"
    private var userProfile: UserProfileEntity? = null
    private var currentTenant: RelationTenant? = null

    // 往下滑動 listener
    private val gesture: GestureDetector by lazy {
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                if (distanceY < 0 && binding.scrollView.scrollY == 0 && isShowing) {
                    dismiss()
                }

                return super.onScroll(e1, e2, distanceX, distanceY)
            }
        })
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        window?.attributes?.windowAnimations = R.style.ios_bottom_dialog_anim
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        window?.setGravity(Gravity.BOTTOM)
        initListener()
        qrCodeData?.let {
            it.businessCardInfo?.let {
                Glide.with(binding.ivBusinessCard)
                    .load(it.imageCardUrl)
                    .into(binding.ivBusinessCard)
            }
        }
    }

    fun setQrCodeData(qrCodeData: QrCodeData) {
        this.qrCodeData = qrCodeData
        qrCodeData.serviceNumber?.let { serviceNumberEntity ->
            serviceNumberEntity.businessCardInfo?.let {
                Glide.with(binding.ivBusinessCard)
                    .load(it.imageCardUrl)
                    .into(binding.ivBusinessCard)
            }
        }
    }

    fun setUserProfileData(userProfileEntity: UserProfileEntity) {
        this.userProfile = userProfileEntity
    }

    fun setTenantData(currentTenant: RelationTenant) {
        this.currentTenant = currentTenant
    }


    private fun initListener() {
        binding.ivClose.setOnClickListener { dismiss() }
        binding.btnShareToOtherApplication.setOnClickListener {
            qrCodeData?.let {
                val shareText = "${binding.etShareText.text} ${it.qrCodeLink}"
                IntentUtil.shareText(context, shareText)
            }
        }

        binding.etPhoneOrEmail.doOnTextChanged { text, start, before, count ->
            if (binding.tvErrorHint.visibility == View.VISIBLE) {
                clearErrorFormatMessage()
            }
        }

        binding.etPhoneOrEmail.doAfterTextChanged {
            checkPhoneAndEmailFormat(it.toString())
        }

        binding.btnConfirm.setOnClickListener {
            val phoneOrEmail = binding.etPhoneOrEmail.text.toString()
            if (!isValid(phoneOrEmail)) {
                setErrorFormatMessage()
                return@setOnClickListener
            }

            qrCodeData?.let {
                val shareText = "${binding.etShareText.text} ${it.qrCodeLink}"
                if (isPhone(phoneOrEmail)) {
                    IntentUtil.shareToSms(context, phoneOrEmail, shareText)
                } else if (isEmail(phoneOrEmail)) {
                    val title = StringBuilder()
                    if (userProfile != null) {
                        title.append(userProfile!!.nickName)
                        title.append("-")
                    }
                    if (currentTenant != null) {
                        title.append(currentTenant!!.tenantName)
                    }
                    title.append("的電子名片")
                    IntentUtil.shareToMail(context, phoneOrEmail, title.toString(), shareText)
                }
            }
        }
    }

    override fun show() {
        super.show()
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(UiHelper.getDisplayWidth(context), (UiHelper.getDisplayHeight(context) * 0.98).toInt())
    }

    private fun clearErrorFormatMessage() {
        binding.tvErrorHint.visibility = View.GONE
        binding.etPhoneOrEmail.backgroundTintList = ColorStateList.valueOf(
            Color.DKGRAY
        )
    }

    private fun setErrorFormatMessage() {
        binding.tvErrorHint.visibility = View.VISIBLE
        binding.etPhoneOrEmail.backgroundTintList = ColorStateList.valueOf(
            context.getColor(
                R.color.red_n
            )
        )
    }

    /**
     *  判斷輸入的是手機還是Email
     *  @param content 內容
     * */
    private fun checkPhoneAndEmailFormat(content: String) {
        if (content.isEmpty()) return
        if (content.startsWith("09")) {
            if (isPhone(content)) return
        } else {
            if (isEmail(content)) return
        }
        setErrorFormatMessage()
    }

    /**
     *  判斷輸入內容是否是手機或Email
     *  @param content 內容
     * */
    private fun isValid(content: String): Boolean {
        return isPhone(content) || isEmail(content)
    }

    /**
     *  判斷是否是手機
     *  @param content 內容
     * */
    private fun isPhone(content: String): Boolean {
        return Pattern.compile(phonePattern)
            .matcher(content)
            .find()
    }

    /**
     *  判斷是否是 Email
     *  @param content 內容
     * */
    private fun isEmail(content: String): Boolean {
        return Pattern.compile(emailPattern)
            .matcher(content)
            .find()
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        gesture.onTouchEvent(event)
        return super.dispatchTouchEvent(event)
    }
}