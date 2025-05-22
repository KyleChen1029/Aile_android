package tw.com.chainsea.chat.searchfilter.view.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import tw.com.chainsea.android.common.event.KeyboardHelper
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.config.BundleKey
import tw.com.chainsea.chat.databinding.ActivityGlobalSearchNewBinding
import tw.com.chainsea.chat.extension.onSubmit
import tw.com.chainsea.chat.network.contact.ViewModelFactory
import tw.com.chainsea.chat.searchfilter.viewmodel.SearchFilterSharedViewModel
import tw.com.chainsea.chat.util.ThemeHelper
import tw.com.chainsea.chat.view.BaseActivity
import tw.com.chainsea.chat.view.base.BottomTabEnum
import tw.com.chainsea.custom.view.progress.IosProgressBar

class GlobalSearchNewActivity : BaseActivity() {

    //the data connection between SearchFilterFragment and top Activity(GlobalSearchNewActivity)
    private lateinit var searchFilterSharedViewModel : SearchFilterSharedViewModel
    private lateinit var binding: ActivityGlobalSearchNewBinding
    private lateinit var progressBar: IosProgressBar
    private var progress = 0
    private val bottomTab by lazy {
        intent.getSerializableExtra(BundleKey.BOTTOM_TAB.key()) as? BottomTabEnum
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeHelper.updateServiceChatRoomTheme(false)
        DataBindingUtil.setContentView<ActivityGlobalSearchNewBinding>(this, R.layout.activity_global_search_new).apply {
            binding = this
            init()
            viewModel = this@GlobalSearchNewActivity.searchFilterSharedViewModel.apply {
                sharedPageType.value = SearchFilterSharedViewModel.SearchFilterPageType.GLOBAL_SEARCH
                bottomTab?.let {
                    globalSearchTabEntry.value = it //紀錄當前BottomTab位置，用來定位搜尋結果Tab
                }
                sendInputText.onEach {//點選文字紀錄
                    etSearch.setText(it)
                    showLoadingView()
                }.launchIn(this@GlobalSearchNewActivity.lifecycleScope)

                sendCollectDataDone.onEach { n ->
                    progress += n
                    if(progress > 2) {
                        hideLoadingView()
                        progress  = 0
                        searchFilterSharedViewModel.sendVerifyCountAndTransfer.emit(Unit)
                    }
                }.launchIn(this@GlobalSearchNewActivity.lifecycleScope)
            }
            lifecycleOwner = this@GlobalSearchNewActivity

            scopeEmit.setOnClickListener {
                hideKeyboard()
                finish()
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out)
            }
        }
    }

    private fun initViewModel() {
        val factory = ViewModelFactory(application)
        searchFilterSharedViewModel =
            ViewModelProvider(this, factory)[SearchFilterSharedViewModel::class.java]
    }

    private fun init() {
        initViewModel()
        binding.apply {
            etSearch.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

                override fun afterTextChanged(s: Editable?) {
                    clearInput.apply {
                        if(visibility == View.GONE)
                            visibility = View.VISIBLE
                        if(s.toString().isEmpty())
                            visibility = View.GONE
                    }
                }
            })
            etSearch.onSubmit {
                lifecycleScope.launch {
                    searchFilterSharedViewModel.apply {
                        sendInputText.emit(etSearch.text.toString())
                        globalSearchInput.value = etSearch.text.toString()
                    }
                }
                hideKeyboard()
            }
        }

    }

    private fun hideKeyboard() {
        KeyboardHelper.hide(this.currentFocus)
        binding.etSearch.clearFocus()
    }

    fun clearInputText(view: View) {
        binding.etSearch.text?.clear()
        binding.clearInput.apply {
            if(visibility == View.VISIBLE)
                visibility = View.GONE
        }
        if(searchFilterSharedViewModel.globalSearchInput.value.isNotEmpty()) {
            lifecycleScope.launch {
                searchFilterSharedViewModel.sendInputText.emit("")
            }
            searchFilterSharedViewModel.globalSearchInput.value = ""
        }
        binding.etSearch.requestFocus()
        KeyboardHelper.open(this.currentFocus)
    }

    private fun showLoadingView() {
        lifecycleScope.launch(Dispatchers.Main) {
            progressBar = IosProgressBar.show(
                this@GlobalSearchNewActivity, getString(R.string.wording_searching), true, false
            ) { }
        }
    }

    private fun hideLoadingView() {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                if (progressBar.isShowing)
                    progressBar.dismiss()
            } catch (ignored: Exception) { }
        }
    }
}