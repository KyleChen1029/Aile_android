package tw.com.chainsea.chat.view.account.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.database.sp.UserPref
import tw.com.chainsea.ce.sdk.service.type.RefreshSource
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.account.AccountDeleteActivity
import tw.com.chainsea.chat.config.SystemConfig
import tw.com.chainsea.chat.databinding.FragmentSelfProfileBinding
import tw.com.chainsea.chat.lib.ToastUtils
import tw.com.chainsea.chat.network.contact.ViewModelFactory
import tw.com.chainsea.chat.network.selfprofile.SelfProfileViewModel
import tw.com.chainsea.chat.util.SystemKit
import tw.com.chainsea.chat.view.login.LogoutSmsDialogFragment
import tw.com.chainsea.custom.view.alert.AlertView

class SelfProfileFragment : Fragment() {
    private lateinit var viewModel: SelfProfileViewModel

    private lateinit var binding: FragmentSelfProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSelfProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        initData()
        initViewModel()
        observeData()
        initListener()
        getData()
    }

    private fun initData() {
        binding.tvPhoneNumber.text = TokenPref.getInstance(requireContext()).accountNumber
        binding.sbHidePhone.isChecked = UserPref.getInstance(requireContext()).mobileVisible
        binding.sbHidePhone.isEnabled = false
    }

    private fun getData() {
        viewModel.getSelfProfile(RefreshSource.REMOTE)
    }

    private fun initViewModel() {
        val selfProfileFactory = ViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(this, selfProfileFactory)[SelfProfileViewModel::class.java]
    }

    private fun observeData() {
        viewModel.selfProfileData.observe(viewLifecycleOwner) {
            binding.tvEmail.text = it.email
            binding.sbHidePhone.isChecked = it.isMobileVisible
            UserPref.getInstance(requireContext()).mobileVisible = it.isMobileVisible
            binding.sbHidePhone.isEnabled = true
        }

        viewModel.mobileVisible.observe(viewLifecycleOwner) {
            UserPref.getInstance(requireContext()).mobileVisible = it
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            ToastUtils.showToast(requireContext(), it)
            binding.sbHidePhone.isChecked = !binding.sbHidePhone.isChecked
            binding.sbHidePhone.isEnabled = true
        }
    }

    private fun initListener() {
        binding.sbHidePhone.setOnCheckedChangeListener { _, isCheck ->
            viewModel.updateMobileVisible(isCheck)
        }
        binding.ivBack.setOnClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }

        binding.clLogout.setOnClickListener { showLogoutDialog() }

        binding.clDelete.setOnClickListener {
            val intent = Intent(activity, AccountDeleteActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showLogoutDialog() {
        if (SystemConfig.isCpMode) {
            LogoutSmsDialogFragment().show(parentFragmentManager, "Logout")
        } else {
            AlertView
                .Builder()
                .setContext(context)
                .setStyle(AlertView.Style.Alert)
                .setMessage(getString(R.string.system_setting_logout_confirm))
                .setOthers(
                    arrayOf(
                        getString(R.string.alert_cancel),
                        getString(R.string.alert_confirm)
                    )
                ).setOnItemClickListener { o: Any?, position: Int ->
                    if (position == 1) {
                        SystemKit.logoutToLoginPage()
                    }
                }.build()
                .setCancelable(true)
                .show()
        }
    }
}
