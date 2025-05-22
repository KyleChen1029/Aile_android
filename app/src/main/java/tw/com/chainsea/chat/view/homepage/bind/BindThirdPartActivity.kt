package tw.com.chainsea.chat.view.homepage.bind

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tw.com.chainsea.android.common.ui.UiHelper
import tw.com.chainsea.ce.sdk.network.model.response.FansPageModel
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.config.BundleKey
import tw.com.chainsea.chat.databinding.ActivityBindingThirdPartBinding
import tw.com.chainsea.chat.databinding.DialogBindFansPageBinding
import tw.com.chainsea.chat.network.contact.ViewModelFactory
import tw.com.chainsea.chat.ui.dialog.BottomSheetDialogBuilder
import tw.com.chainsea.chat.view.BaseActivity
import tw.com.chainsea.custom.view.progress.IosProgressBar
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.DividerItemDecorationWithoutLastItem


/**
 * 綁定 Facebook 和 Instagram 頁面
 * */
class BindThirdPartActivity : BaseActivity() {

    private val binding by lazy { ActivityBindingThirdPartBinding.inflate(layoutInflater) }
    private val thirdPartListAdapter by lazy {
        ThirdPartListAdapter {
            bindThirdPartViewModel.checkIsLogin(this, it)
        }
    }

    private var selectFansPageDialog: Dialog? = null
    private val dialogBinding by lazy { DialogBindFansPageBinding.inflate(layoutInflater) }
    private val selectFansAdapter = FansPageAdapter {
        dialogBinding.tvConfirm.isEnabled = it
    }

    private lateinit var progressBar: IosProgressBar

    private val bindThirdPartViewModel by lazy {
        val viewModelFactory = ViewModelFactory(application)
        ViewModelProvider(this, viewModelFactory)[BindThirdPartViewModel::class.java]
    }

    private val serviceNumberId by lazy {
        intent.getStringExtra(BundleKey.SERVICE_NUMBER_ID.name) ?: ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        observer()
        initListener()
        initThirdPartList()
        initDialog()
        bindThirdPartViewModel.getServiceNumber(serviceNumberId)
        window.statusBarColor = -0x943d46
    }

    private fun observer() {
        bindThirdPartViewModel.loginResult.observe(this) {
            if (it == ThirdPartEnum.Instagram) {
                val fansPageId = thirdPartListAdapter.getFacebookFansPageId()
                if (!fansPageId.isNullOrEmpty()) {
                    bindThirdPartViewModel.getFansPage(fansPageId)
                    return@observe
                }
            }
            bindThirdPartViewModel.getFansPage()
        }

        bindThirdPartViewModel.facebookFansPageModel.observe(this) {
            if (it.isEmpty()) {
                Toast.makeText(this, getString(R.string.not_connect_instagram), Toast.LENGTH_SHORT).show()
            } else {
                showFansPage(it)
            }
        }

        bindThirdPartViewModel.onBindSuccess.observe(this) {
            val toastString = if (it.third) getString(R.string.bind_success) else getString(R.string.bind_failed)
            Toast.makeText(this, toastString, Toast.LENGTH_SHORT).show()
            selectFansPageDialog?.dismiss()
            dismissLoading()
            if (!it.third) return@observe
            bindThirdPartViewModel.getServiceNumber(serviceNumberId)
        }

        bindThirdPartViewModel.thirdPartList.observe(this) {
            binding.rvThirdPartList.apply {
                layoutManager = LinearLayoutManager(this@BindThirdPartActivity)
                addItemDecoration(DividerItemDecorationWithoutLastItem(this@BindThirdPartActivity, LinearLayoutManager.VERTICAL))
                thirdPartListAdapter.setData(it)
                adapter = thirdPartListAdapter
            }
        }

        bindThirdPartViewModel.boundFacebookFansPage.observe(this) {
            thirdPartListAdapter.setBindInfo(
                ThirdPartEnum.Facebook,
                it.fansPageId,
                getString(R.string.linked_facebook_fans_page),
                it.name
            )
        }

        bindThirdPartViewModel.boundInstagramFansPage.observe(this) {
            thirdPartListAdapter.setBindInfo(
                ThirdPartEnum.Instagram,
                it.groupCode,
                getString(R.string.linked_instagram_fans_page),
                it.name
            )
        }

        bindThirdPartViewModel.unBindFansPage.observe(this) {
            thirdPartListAdapter.clearBindInfo(it, if (it == ThirdPartEnum.Facebook) getString(R.string.bind_facebook) else getString(R.string.bind_instagram))
        }

        bindThirdPartViewModel.unBindFansPageError.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }

        bindThirdPartViewModel.showUnBindDialog.observe(this) {
            showUnBindDialog(it.first, it.second, it.third)
        }
    }

    private fun initListener() {
        binding.leftAction.setOnClickListener { finish() }
    }

    private fun initThirdPartList() {
        bindThirdPartViewModel.getThirdPartList()
    }

    private fun initDialog() = CoroutineScope(Dispatchers.Main).launch {
        selectFansPageDialog = Dialog(this@BindThirdPartActivity)
        selectFansPageDialog?.apply {
            setContentView(dialogBinding.root)
            setCancelable(false)
        }
        dialogBinding.tvConfirm.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val facebookFansPageModel = selectFansAdapter.getSelectedFansPage()
                facebookFansPageModel?.let {
                    bindThirdPartViewModel.getFansPageAccessToken(it.id, it.name)
                    showLoading()
                } ?: run {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@BindThirdPartActivity,
                            getString(R.string.bind_select_account),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
        dialogBinding.rvFansPageList.apply {
            addItemDecoration(DividerItemDecoration(this@BindThirdPartActivity, DividerItemDecoration.VERTICAL))
            layoutManager = LinearLayoutManager(this@BindThirdPartActivity)
            this.adapter = selectFansAdapter
            setHasFixedSize(true)
        }
    }

    /**
     * 顯示可以連結的粉絲專頁
     * @param fansPage 粉絲專頁的資料
     * */
    private fun showFansPage(fansPage: List<FansPageModel>) = CoroutineScope(Dispatchers.Main).launch {
        selectFansAdapter.setData(fansPage)
        selectFansPageDialog?.apply {
            show()
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setLayout((UiHelper.getDisplayWidth(this@BindThirdPartActivity) * 0.9).toInt(), (UiHelper.getDisplayHeight(this@BindThirdPartActivity) * 0.6).toInt())
        }
    }

    private fun showUnBindDialog(thirdPartEnum: ThirdPartEnum, fansPageId: String, name: String) {
        BottomSheetDialogBuilder(this, layoutInflater).getUnBindFansPageDialog(name) {
            bindThirdPartViewModel.unBindFansPage(thirdPartEnum, fansPageId)
        }.show()
    }

    private fun showLoading() = CoroutineScope(Dispatchers.Main).launch {
        progressBar = IosProgressBar.show(this@BindThirdPartActivity, "", true, false
        ) { _: DialogInterface? -> }
    }

    private fun dismissLoading() = CoroutineScope(Dispatchers.Main).launch {
        progressBar.dismiss()
    }
}