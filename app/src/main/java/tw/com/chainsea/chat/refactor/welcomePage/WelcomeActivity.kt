package tw.com.chainsea.chat.refactor.welcomePage

//noinspection SuspiciousImport
import android.R
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import tw.com.chainsea.ce.sdk.config.AppConfig
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.chat.App
import tw.com.chainsea.chat.config.BundleKey
import tw.com.chainsea.chat.databinding.ActivityWelcomeBinding
import tw.com.chainsea.chat.network.contact.ViewModelFactory
import tw.com.chainsea.chat.refactor.loginPage.LoginCpActivity
import tw.com.chainsea.chat.service.ActivityTransitionsControl
import tw.com.chainsea.chat.util.NoDoubleClickListener
import tw.com.chainsea.chat.view.base.HomeActivity
import tw.com.chainsea.chat.view.base.viewmodel.HomeViewModel

class WelcomeActivity :
    AppCompatActivity(),
    WelcomeContract.IView {
    private lateinit var mBinding: ActivityWelcomeBinding
    private val homeViewModel: HomeViewModel by lazy {
        val homeViewModelFactory = ViewModelFactory(application)
        ViewModelProvider(this, homeViewModelFactory)[HomeViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        App.APP_STATUS = App.APP_STATUS_NORMAL
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.TRANSPARENT
        if (TokenPref.getInstance(this).isFirstTime) {
            guildFlow()
        } else {
            val token = TokenPref.getInstance(this).cpTokenId
            if (token.isNotEmpty()) {
                autoLoginFlow()
            } else {
                startActivity(Intent(this@WelcomeActivity, LoginCpActivity::class.java))
            }
        }
    }

    private fun guildFlow() {
        mBinding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        val welcomeGuildAdapter = WelcomeGuildAdapter(this)
        mBinding.viewpager.adapter = welcomeGuildAdapter
        mBinding.btnStart.setOnClickListener(
            object : NoDoubleClickListener() {
                override fun onNoDoubleClick(v: View?) {
                    TokenPref.getInstance(this@WelcomeActivity).setIsFirstTime(false)
                    startActivity(Intent(this@WelcomeActivity, LoginCpActivity::class.java))
                    finish()
                }
            }
        )

        for (i in 0 until welcomeGuildAdapter.itemCount) {
            val radioButton = RadioButton(this)
            radioButton.apply {
                scaleX = 0.7f
                scaleY = 0.7f
                isClickable = false
                buttonTintList =
                    ColorStateList(
                        arrayOf(
                            intArrayOf(-R.attr.state_enabled),
                            intArrayOf(R.attr.state_enabled)
                        ),
                        intArrayOf(
                            Color.parseColor("#0084c0"), // disable
                            Color.parseColor("#0084c0") // enable
                        )
                    )
            }
            mBinding.indicator.addView(radioButton)
        }

        mBinding.viewpager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    mBinding.indicator.check(mBinding.indicator.getChildAt(position).id)
                }
            }
        )
    }

    private fun autoLoginFlow() {
        AppConfig.tokenForNewAPI = TokenPref.getInstance(this).tokenId
        homeViewModel.setServiceUrl()
        startActivity(
            Intent(this@WelcomeActivity, HomeActivity::class.java)
                .putExtra(BundleKey.AUTO_LOGIN.key(), true)
        )
        finish()
    }

    override fun goToLoginPage() {
        startActivity(Intent(this, LoginCpActivity::class.java))
        finish()
    }

    override fun goToHomePage(
        isBindAile: Boolean,
        bindUrl: String?,
        isCollectInfo: Boolean
    ) {
        val whereCome = intent.getStringExtra(BundleKey.WHERE_COME.key())
        val roomId = intent.getStringExtra(BundleKey.ROOM_ID.key())
        val messageId = intent.getStringExtra(BundleKey.MESSAGE_ID.key())
        ActivityTransitionsControl.navigationToBase(this@WelcomeActivity, whereCome, roomId, messageId, isBindAile, bindUrl, isCollectInfo) { intent: Intent, s: String ->
            startActivity(intent)
            finish()
        }
    }

    override fun goToHomePage() {
        startActivity(Intent(this@WelcomeActivity, HomeActivity::class.java))
        finish()
    }

    override fun goToHomePageWithError(errorMessage: String) {
        val intent = Intent(this, HomeActivity::class.java).putExtra("error", errorMessage)
        startActivity(intent)
        finish()
    }
}
