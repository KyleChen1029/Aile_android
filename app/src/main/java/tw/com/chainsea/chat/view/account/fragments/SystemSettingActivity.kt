package tw.com.chainsea.chat.view.account.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentTransaction
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.http.ce.ApiManager
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener
import tw.com.chainsea.chat.BuildConfig
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.FragmentSystemSettingBinding
import tw.com.chainsea.chat.view.BaseActivity
import tw.com.chainsea.chat.view.setting.AboutActivity
import tw.com.chainsea.chat.view.setting.RepairsActivity

class SystemSettingActivity : BaseActivity() {
    private lateinit var binding: FragmentSystemSettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentSystemSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initListener()
        setVersion()
    }

    @SuppressLint("SetTextI18n")
    private fun setVersion() {
        binding.tvVersion.text = "Ver ${BuildConfig.VERSION_NAME}"
    }

    private fun initListener() {
        binding.ivBack.setOnClickListener { finish() }
        binding.clAccount.setOnClickListener {
            val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
            ft
                .setCustomAnimations(
                    R.anim.slide_left_in,
                    R.anim.slide_left_out,
                    R.anim.slide_right_in,
                    R.anim.slide_right_out
                ).add(R.id.fl_self, SelfProfileFragment())
                .addToBackStack(null)
                .commit()
        }
        binding.clAbout.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
        binding.clRepair.setOnClickListener {
            startActivity(Intent(this, RepairsActivity::class.java))
        }
        binding.sbEnable.isEnabled = true
        binding.sbEnable.isChecked = !TokenPref.getInstance(this).isMute
        binding.sbEnable.setOnCheckedChangeListener { _, isChecked ->
            switchMessageNotify(this, isChecked)
        }

        binding.sbCloseSubChatRoom.isChecked = TokenPref.getInstance(this).isAutoCloseSubChat
        binding.sbCloseSubChatRoom.setOnCheckedChangeListener { _, isChecked ->
            TokenPref.getInstance(this).setIsAutoCloseSubChat(isChecked)
        }
    }

    private fun switchMessageNotify(
        context: Context,
        isMute: Boolean
    ) {
        if (!isMute) {
            ApiManager.getInstance().doUserMute(
                context,
                object : ApiListener<String?> {
                    override fun onSuccess(s: String?) {
                        TokenPref.getInstance(context).isMute = true
                    }

                    override fun onFailed(errorMessage: String) {
                    }
                }
            )
        } else {
            ApiManager.doUserMuteCancel(
                context,
                object : ApiListener<String?> {
                    override fun onSuccess(s: String?) {
                        TokenPref.getInstance(context).isMute = false
                    }

                    override fun onFailed(errorMessage: String) {
                    }
                }
            )
        }
    }
}
