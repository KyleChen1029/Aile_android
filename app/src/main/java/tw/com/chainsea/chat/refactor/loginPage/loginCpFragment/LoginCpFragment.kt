package tw.com.chainsea.chat.refactor.loginPage.loginCpFragment

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.http.cp.CpApiPath
import tw.com.chainsea.ce.sdk.socket.cp.CpSocket
import tw.com.chainsea.chat.BuildConfig
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.config.SystemConfig
import tw.com.chainsea.chat.databinding.DialogCustomDomainBinding
import tw.com.chainsea.chat.databinding.FragmentLoginPhoneBinding
import tw.com.chainsea.chat.lib.ToastUtils
import tw.com.chainsea.chat.lib.Util
import tw.com.chainsea.chat.refactor.ServerEnvironment
import tw.com.chainsea.chat.ui.dialog.IosProgressDialog
import tw.com.chainsea.chat.ui.utils.countrycode.CountryActivity
import tw.com.chainsea.chat.util.NoDoubleClickListener
import java.util.Locale

class LoginCpFragment :
    Fragment(),
    LoginCpFragmentContract.IView {
    private val mPresenter: LoginCpFragmentPresenter by lazy {
        LoginCpFragmentPresenter(this)
    }

    private val mProgressDialog by lazy {
        IosProgressDialog(requireContext())
    }

    private val navController: NavController by lazy {
        NavHostFragment.findNavController(this)
    }

    private lateinit var mActivityResultLauncher: ActivityResultLauncher<Intent>

    private lateinit var mBinding: FragmentLoginPhoneBinding
    private var countryCode = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentLoginPhoneBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initClickListener()

        mPresenter.getVersion(requireContext())
        mActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.let {
                        it.extras?.let { bundle ->
                            val countryNumber = bundle.getString("countryNumber")
                            countryNumber?.let { code ->
                                mBinding.txtCountryCode.text = code
                                TokenPref.getInstance(context).countryCode = code
                                countryCode = code
                            }
                        }
                    }
                }
            }
    }

    private fun initView() {
        countryCode = TokenPref.getInstance(requireContext()).countryCode
        if (countryCode.isNotEmpty()) {
            mBinding.txtCountryCode.text = countryCode
        }
        mBinding.cbRememberMe.isChecked = TokenPref.getInstance(requireContext()).isRememberMe
        if (mBinding.cbRememberMe.isChecked) {
            val number = TokenPref.getInstance(requireContext()).accountNumber
            mBinding.edtAccount.setText(number)
            mBinding.btnJoin.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.yellow))
            mBinding.btnJoin.isClickable = true
        } else {
            mBinding.btnJoin.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.color_FAD391))
            mBinding.btnJoin.isClickable = false
        }

        mBinding.edtAccount.doAfterTextChanged {
            mBinding.btnJoin.setBackgroundColor(if (Util.checkMobile(countryCode + it)) ContextCompat.getColor(requireActivity(), R.color.yellow) else ContextCompat.getColor(requireActivity(), R.color.color_FAD391))
            mBinding.btnJoin.isClickable = Util.checkMobile(countryCode + it)
        }
    }

    private fun initClickListener() {
        mBinding.btnJoin.setOnClickListener(clickListener)
        mBinding.txtChangeToAccount.setOnClickListener(clickListener)
        mBinding.txtChangeToPhone.setOnClickListener(clickListener)
        mBinding.txtCountryCode.setOnClickListener(clickListener)
    }

    private val clickListener: NoDoubleClickListener =
        object : NoDoubleClickListener() {
            override fun onNoDoubleClick(v: View) {
                when (v) {
                    mBinding.txtCountryCode -> {
                        mActivityResultLauncher.launch(Intent(activity, CountryActivity::class.java))
                    }

                    mBinding.btnJoin -> {
                        loginFlow()
                    }

                    mBinding.txtChangeToAccount -> {
                        changeToAccount()
                    }

                    mBinding.txtChangeToPhone -> {
                        changeToPhone()
                    }
                }
            }
        }

    private fun showChangeCpServerDialog() {
        val servers =
            when (BuildConfig.FLAVOR) {
                "dev", "qa", "uat" -> {
                    arrayOf(BuildConfig.FLAVOR.uppercase(Locale.ROOT), "自定義")
                }

                else -> {
                    arrayOf()
                }
            }

        if (servers.isEmpty()) return

        AlertDialog
            .Builder(requireActivity())
            .setItems(servers) { _: DialogInterface?, which: Int ->
                when (which) {
                    0 -> {
                        when (BuildConfig.FLAVOR.uppercase(Locale.ROOT)) {
                            ServerEnvironment.DEV.name -> {
                                changeServer(CpApiPath.DEV_SERVER, CpSocket.DEV_SERVER_SOCKET, ServerEnvironment.DEV.name, ServerEnvironment.DEV.name)
                                setCustomDomain("")
                            }

                            ServerEnvironment.QA.name -> {
                                changeServer(CpApiPath.QA_SERVER, CpSocket.QA_SERVER_SOCKET, ServerEnvironment.QA.name, ServerEnvironment.QA.name)
                                setCustomDomain("")
                            }

                            ServerEnvironment.UAT.name -> {
                                changeServer(CpApiPath.UAT_SERVER, CpSocket.UAT_SERVER_SOCKET, ServerEnvironment.UAT.name, ServerEnvironment.UAT.name)
                                setCustomDomain("")
                            }
                        }
                    }

                    else -> {
                        val dialog = getCustomDomainDialog()
                        dialog?.let {
                            it.show()
                            val windows = it.window
                            windows?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        }
                    }
                }
            }.show()
    }

    private fun getCustomDomainDialog(): Dialog? {
        activity?.let { activity ->
            val dialog = Dialog(activity)
            val dialogBinding = DialogCustomDomainBinding.inflate(layoutInflater)
            dialog.apply {
                setContentView(dialogBinding.root)
                setCancelable(false)
            }

            dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
            dialogBinding.btnSubmit.setOnClickListener {
                val customDomain = dialogBinding.etCustomDomain.text.toString()
                if (customDomain.startsWith("http://") || customDomain.startsWith("https://")) {
                    changeServer(customDomain.plus("cp/openapi/"), customDomain.plus("cp"), customDomain, ServerEnvironment.SELF_DEFINE.name)
                    setCustomDomain(customDomain)
                    dialog.dismiss()
                } else {
                    Toast.makeText(activity, "請輸入正確的網址", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }
            return dialog
        }
        return null
    }

    private fun setCustomDomain(customDomain: String) {
        TokenPref.getInstance(requireActivity()).selfDefineEnvironment = customDomain // Save domain
    }

    override fun onResume() {
        super.onResume()
        if (SystemConfig.isCpMode) {
            val customDomain = TokenPref.getInstance(requireActivity()).selfDefineEnvironment
            if (!customDomain.isNullOrEmpty()) {
                changeServer(customDomain.plus("cp/openapi/"), customDomain.plus("cp"), customDomain, ServerEnvironment.SELF_DEFINE.name)
            } else {
                when (BuildConfig.FLAVOR.uppercase()) {
                    ServerEnvironment.DEV.name -> {
                        changeServer(CpApiPath.DEV_SERVER, CpSocket.DEV_SERVER_SOCKET, ServerEnvironment.DEV.name, ServerEnvironment.DEV.name)
                    }

                    ServerEnvironment.QA.name -> {
                        changeServer(CpApiPath.QA_SERVER, CpSocket.QA_SERVER_SOCKET, ServerEnvironment.QA.name, ServerEnvironment.QA.name)
                    }

                    ServerEnvironment.UAT.name -> {
                        changeServer(CpApiPath.UAT_SERVER, CpSocket.UAT_SERVER_SOCKET, ServerEnvironment.UAT.name, ServerEnvironment.UAT.name)
                    }

                    else -> {
                        changeServer(CpApiPath.FORMAL_SERVER, CpSocket.FORMAL_SERVER, "", ServerEnvironment.FORMAL.name)
                    }
                }
            }
        }
    }

    private fun changeServer(
        baseUrl: String,
        cpSocketUrl: String,
        textString: String,
        prefText: String
    ) {
        mBinding.tvChangeServer.text = textString
        mPresenter.changeServer(requireContext(), baseUrl, cpSocketUrl, prefText)
    }

    private fun changeToAccount() {
        mBinding.txtChangeToAccount.visibility = View.GONE
        mBinding.txtChangeToPhone.visibility = View.VISIBLE
        mBinding.txtForgetPassword.visibility = View.VISIBLE
        mBinding.edtPassword.visibility = View.VISIBLE
    }

    private fun changeToPhone() {
        mBinding.txtChangeToAccount.visibility = View.VISIBLE
        mBinding.txtChangeToPhone.visibility = View.GONE
        mBinding.txtForgetPassword.visibility = View.GONE
        mBinding.edtPassword.visibility = View.GONE
    }

    override fun setChangeServerButton() {
        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                mBinding.tvChangeServer.visibility = View.VISIBLE // 判斷現在版本是否大於server版本, 才可以顯示切換server功能
            }
        }
        mBinding.ivChangeServer.setOnLongClickListener { v ->
            showChangeCpServerDialog()
            false
        }
    }

    override fun showProgressDialog() {
        mProgressDialog.show()
    }

    override fun dismissProgressDialog() {
        mProgressDialog.dismiss()
    }

    override fun onApiSuccess(response: Any) {
        val bundle =
            bundleOf(
                "onceToken" to response.toString(),
                "countryCode" to mBinding.txtCountryCode.text.toString(),
                "phoneNumber" to mBinding.edtAccount.text.toString()
            )
        navController.navigateUp()
        navController.navigate(R.id.action_loginCpFragment_to_loginSmsFragment, bundle)
    }

    override fun onApiFailed(
        errorCode: String,
        errorMessage: String
    ) {
        ToastUtils.showToast(requireContext(), errorMessage)
    }

    override fun goToRegisterPage() {
        val bundle = bundleOf("countryCode" to mBinding.txtCountryCode.text.toString(), "phoneNumber" to mBinding.edtAccount.text.toString())
        navController.navigate(R.id.action_loginCpFragment_to_loginRegisterFragment, bundle)
    }

    private fun loginFlow() {
        val account: String? = mBinding.edtAccount.text?.toString()
        val countryCode = mBinding.txtCountryCode.text.toString()
        TokenPref.getInstance(context).countryCode = countryCode
        TokenPref.getInstance(context).accountNumber = account ?: ""
        mPresenter.login(
            requireContext(),
            countryCode,
            account
                ?: "",
            mBinding.cbRememberMe.isChecked
        )
    }

    override fun onDestroyView() {
        mActivityResultLauncher.unregister()
        mBinding.btnJoin.setOnClickListener(null)
        mBinding.txtChangeToAccount.setOnClickListener(null)
        mBinding.txtChangeToPhone.setOnClickListener(null)
        mBinding.txtCountryCode.setOnClickListener(null)
        super.onDestroyView()
    }
}
