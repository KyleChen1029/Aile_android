package tw.com.chainsea.chat.refactor.loginPage

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import tw.com.chainsea.chat.App
import tw.com.chainsea.chat.App.APP_STATUS_NORMAL
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.config.BundleKey

class LoginCpActivity :
    AppCompatActivity(),
    LoginCpContract.IView {
    private val mNavController: NavController by lazy {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navHostFragment.navController
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (!isGranted) {
                // 不允許開啟通知
                Toast.makeText(this, getString(R.string.text_need_notification_permission), Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.APP_STATUS = APP_STATUS_NORMAL
        DataBindingUtil.setContentView<ViewDataBinding>(this, R.layout.activity_login_cp)
//        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
//        mNavController = navHostFragment.navController
        setOnBackPressCallback()
//        mPresenter.getPermission(this)

        // 檢查權限, 如果沒有權限就開啟通知
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // 直接跳轉到 創建團隊/加入團隊 頁面
        val bundle = intent.extras
        bundle?.let {
            if (it.getBoolean(BundleKey.TO_CREATE_OR_JOIN_TENANT.name, false)) {
                mNavController.navigate(R.id.action_loginCpFragment_to_loginCreateOrJoinFragment)
            }
        }
    }

    private fun setOnBackPressCallback() {
        onBackPressedDispatcher.addCallback {
            mNavController.currentDestination?.let {
                if (it.id == R.id.loginCpFragment) {
                    return@let
                }
                if (it.id != R.id.loginCreateOrJoinFragment) {
                    mNavController.popBackStack()
                }
            }
        }
    }
}
