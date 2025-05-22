package tw.com.chainsea.chat.view.setting

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import tw.com.chainsea.chat.BuildConfig.VERSION_CODE
import tw.com.chainsea.chat.BuildConfig.VERSION_NAME
import tw.com.chainsea.chat.BuildConfig.qaUpdate
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.ActivityAboutBinding
import tw.com.chainsea.chat.util.ThemeHelper
import tw.com.chainsea.chat.view.BaseActivity
import java.text.SimpleDateFormat
import java.util.Locale

class AboutActivity : BaseActivity() {
    lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initTheme()
        init()
        initClickListener()
    }

    private fun initTheme() {
        val isGreenTheme = ThemeHelper.isGreenTheme()
        binding.logoImage.visibility = if (isGreenTheme) View.INVISIBLE else View.VISIBLE
        binding.logoName.visibility = if (isGreenTheme) View.INVISIBLE else View.VISIBLE
        binding.clRepair.visibility = if (isGreenTheme) View.INVISIBLE else View.VISIBLE
        binding.pagerTitle.text = if (isGreenTheme) getString(R.string.text_settings_about) else getString(R.string.system_setting_item_about)
    }

    @SuppressLint("SetTextI18n")
    private fun init() {
        val versionName = VERSION_NAME
        val date = (
            VERSION_CODE.toString().toLong() * 1000 + (
                SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.TAIWAN).parse("2017/01/01 00:00:00")?.time
                    ?: 0
            )
        )
        val versionCode = SimpleDateFormat("yyMMddHHmm", Locale.TAIWAN).format(date)
        binding.tvVersionName.text = "Ver $versionName build $qaUpdate$versionCode"
    }

    private fun initClickListener() {
        binding.clRepair.setOnClickListener {
            startActivity(Intent(this, RepairsActivity::class.java))
        }

        binding.tvVersionCode.setOnClickListener {
            val clipboard: ClipboardManager =
                getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(getString(R.string.app_name), binding.tvVersionCode.text)
            clipboard.setPrimaryClip(clip)
        }

        binding.back.setOnClickListener {
            finish()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out)
    }
}
