package tw.com.chainsea.chat.chatroomfilter

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.chatroomfilter.adpter.ChatRoomFilterAdapter
import tw.com.chainsea.chat.chatroomfilter.model.FilterFileModel
import tw.com.chainsea.chat.chatroomfilter.model.FilterLinkModel
import tw.com.chainsea.chat.chatroomfilter.model.FilterMediaModel
import tw.com.chainsea.chat.config.BundleKey
import tw.com.chainsea.chat.databinding.ActivityChatCoomFilterBinding
import tw.com.chainsea.chat.network.contact.ViewModelFactory
import tw.com.chainsea.chat.ui.dialog.BottomSheetDialogBuilder
import tw.com.chainsea.chat.util.ThemeHelper
import tw.com.chainsea.chat.view.BaseActivity
import tw.com.chainsea.chat.view.todo.TodoOverviewFragment
import tw.com.chainsea.custom.view.progress.IosProgressBar
import java.util.Objects

class ChatRoomFilterActivity :
    BaseActivity(),
    MultipleChoiceCallback,
    OnDataGetCallback {
    private val chatRoomId by lazy { intent.getStringExtra(BundleKey.ROOM_ID.key()) ?: "" }
    private val binding by lazy { ActivityChatCoomFilterBinding.inflate(layoutInflater) }
    private var isShare = false
    private lateinit var progressBar: IosProgressBar
    private val isGreenTheme: Boolean by lazy { ThemeHelper.isGreenTheme() }
    private val isServiceRoomTheme: Boolean by lazy { ThemeHelper.isServiceRoomTheme }
    private val viewModel by lazy {
        val chatRoomFilterViewModel = ViewModelFactory(application)
        ViewModelProvider(this, chatRoomFilterViewModel)[ChatRoomFilterViewModel::class.java]
    }

    private val fragmentListAdapter by lazy {
        ChatRoomFilterAdapter(supportFragmentManager, lifecycle, filterFragment)
    }
    private val filterFragment by lazy {
        mutableListOf(
            FilterMediaFragment.newInstance(chatRoomId, this),
            FilterLinkFragment.newInstance(this),
            FilterFileFragment.newInstance(this),
            TodoOverviewFragment
                .newInstance(this)
                .setBundle(
                    bundleOf(
                        BundleKey.ROOM_ID.key() to chatRoomId,
                        BundleKey.IS_FROM_FILTER.key() to true
                    )
                )
        )
    }

    private val fragmentTitle by lazy {
        mutableListOf(
            getString(R.string.text_filter_title_media),
            getString(R.string.text_filter_title_link),
            getString(R.string.text_filter_title_file),
            getString(R.string.text_filter_title_todo)
        )
    }

    private val downloadPermissionResult: ActivityResultLauncher<String> =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                download()
            } else {
                Toast
                    .makeText(
                        this,
                        getString(R.string.text_need_storage_permission),
                        Toast.LENGTH_SHORT
                    ).show()
            }
        }
    private val launcher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result: Map<String, Boolean> ->
            if (result[Manifest.permission.READ_MEDIA_IMAGES] != null && result[Manifest.permission.READ_MEDIA_VIDEO] != null) {
                if (Objects.requireNonNull<Boolean?>(result[Manifest.permission.READ_MEDIA_IMAGES]) == true &&
                    Objects.requireNonNull<Boolean?>(
                        result[Manifest.permission.READ_MEDIA_VIDEO]
                    ) == true
                ) {
                    download()
                } else {
                    Toast
                        .makeText(
                            this,
                            getString(R.string.text_need_storage_permission),
                            Toast.LENGTH_SHORT
                        ).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initViewPager()
        observerData()
        initListener()
        initTheme()
        viewModel.roomId = chatRoomId
//        viewModel.queryChatRoomMessage(chatRoomId)
        val isNeedToToDo = intent.getBooleanExtra(BundleKey.INTENT_TO_TODO.key(), false)
        if (isNeedToToDo) {
            binding.vpFragmentList.setCurrentItem(3, false)
        }
    }

    private fun initTheme() {
        val backgroundColor = getBackGroundColor()
        binding.titleBar.setBackgroundColor(
            ResourcesCompat.getColor(
                resources,
                backgroundColor,
                null
            )
        )
        window.statusBarColor =
            ResourcesCompat.getColor(
                resources,
                backgroundColor,
                null
            )
        binding.tabLayout.setTabTextColors(
            ResourcesCompat.getColor(
                resources,
                backgroundColor,
                null
            ),
            ResourcesCompat.getColor(
                resources,
                backgroundColor,
                null
            )
        )
        binding.tabLayout.setSelectedTabIndicatorColor(
            ResourcesCompat.getColor(
                resources,
                backgroundColor,
                null
            )
        )
    }

    private fun getBackGroundColor(): Int =
        if (isGreenTheme && isServiceRoomTheme) {
            R.color.color_6BC2BA
        } else if (isGreenTheme && !isServiceRoomTheme) {
            R.color.color_015F57
        } else {
            if (isServiceRoomTheme) {
                R.color.color_6BC2BA
            } else {
                R.color.colorPrimary
            }
        }

    private fun initViewPager() {
        binding.vpFragmentList.apply {
            adapter = fragmentListAdapter
            offscreenPageLimit = filterFragment.size
        }
        TabLayoutMediator(
            binding.tabLayout,
            binding.vpFragmentList
        ) { tab, position ->
            tab.text = fragmentTitle[position]
        }.attach()

        binding.vpFragmentList.registerOnPageChangeCallback(
            object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    if (fragmentListAdapter.currentFragment != fragmentListAdapter.getCurrentFragment(position)) {
                        cancelMultipleChoiceMode()
                    }
                    fragmentListAdapter.currentFragment = fragmentListAdapter.getCurrentFragment(position)
                    if (fragmentListAdapter.currentFragment is TodoOverviewFragment) {
                        binding.tvMultipleChoice.visibility = View.GONE
                        binding.ivToolBarDownIcon.visibility = View.GONE
                    } else {
                        binding.tvMultipleChoice.visibility = View.VISIBLE
                        binding.ivToolBarDownIcon.visibility = View.VISIBLE
                    }
                    binding.clMultipleChoiceBottomBar.visibility = if (isCurrentFragmentHasData()) View.GONE else View.INVISIBLE
                }
            }
        )
    }

    private fun observerData() {
        viewModel.filterMediaMessageList.observe(this) { data ->
            filterFragment.find { it is FilterMediaFragment }?.let {
                (it as FilterMediaFragment).setFilterMediaList(data)
                binding.clMultipleChoiceBottomBar.visibility = if (isCurrentFragmentHasData()) View.GONE else View.INVISIBLE
            }
        }

        viewModel.filterLinkMessageList.observe(this) { data ->
            filterFragment.find { it is FilterLinkFragment }?.let {
                (it as FilterLinkFragment).setFilterLinkList(data)
            }
        }

        viewModel.filterFileMessageList.observe(this) { data ->
            filterFragment.find { it is FilterFileFragment }?.let {
                (it as FilterFileFragment).setFilterFileList(data)
            }
        }

        viewModel.onWebMetaDataGet.observe(this) { data ->
            filterFragment.find { it is FilterLinkFragment }?.let {
                (it as FilterLinkFragment).onWebMetaDataGet(data)
            }
        }

        viewModel.onDownloadSucceed.observe(this) {
            if (!isShare) {
                val fragment = fragmentListAdapter.currentFragment
                val text =
                    if (fragment is FilterMediaFragment) {
                        getString(R.string.text_filter_download_media_succeed)
                    } else {
                        getString(
                            R.string.text_filter_download_file_succeed
                        )
                    }
                Toast
                    .makeText(
                        this,
                        text,
                        Toast.LENGTH_SHORT
                    ).show()
            }
            dismissLoadingView()
            cancelMultipleChoiceMode()
        }

        viewModel.onDownloadError.observe(this) {
            if (it.first) {
                Toast.makeText(this, getString(R.string.text_filter_file_download_all_failed), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, String.format(getString(R.string.text_filter_file_download_some_failed), it.second), Toast.LENGTH_SHORT).show()
            }
            dismissLoadingView()
            cancelMultipleChoiceMode()
        }
    }

    private fun initListener() {
        binding.leftAction.setOnClickListener { finish() }
        binding.tvMultipleChoice.setOnClickListener {
            it.isSelected = it.isSelected.not()
            if (it.isSelected) {
                setMultipleChoiceMode()
            } else {
                cancelMultipleChoiceMode()
            }
        }
        binding.ivDownload.setOnClickListener {
            if (!isHasChoice()) return@setOnClickListener
            isShare = false
            requirePermission()
        }

        binding.ivShare.setOnClickListener {
            if (!isHasChoice()) return@setOnClickListener
            isShare = true
            when (val fragment = fragmentListAdapter.currentFragment) {
                is FilterMediaFragment, is FilterFileFragment -> {
                    requirePermission()
                }

                is FilterLinkFragment -> {
                    val choiceList = fragment.getMultipleChoiceList()
                    viewModel.shareLink(choiceList as MutableList<FilterLinkModel>)
                    cancelMultipleChoiceMode()
                }
            }
        }

        binding.clTitle.setOnClickListener {
            if (fragmentListAdapter.currentFragment is TodoOverviewFragment) return@setOnClickListener
            val sortDialog =
                BottomSheetDialogBuilder(this, layoutInflater).getFilterSortDialog {
                    viewModel.queryChatRoomMessage(it)
                    filterFragment.find { it is FilterMediaFragment }?.let { fragment ->
                        (fragment as FilterMediaFragment).setMessageSort(it)
                    }
                }
            sortDialog.show()
        }
    }

    private fun download() {
        when (val fragment = fragmentListAdapter.currentFragment) {
            is FilterMediaFragment -> {
                val choiceList = fragment.getMultipleChoiceList()
                showLoadingView()
                viewModel.downloadMedia(isShare, choiceList as MutableList<FilterMediaModel>)
            }

            is FilterFileFragment -> {
                val choiceList = fragment.getMultipleChoiceList()
                showLoadingView()
                viewModel.downloadFile(isShare, choiceList as MutableList<FilterFileModel>)
            }
        }
    }

    private fun isHasChoice(): Boolean {
        val fragment = fragmentListAdapter.currentFragment
        if (fragment is TodoOverviewFragment) return false
        fragment as BaseChatRoomFilterFragment
        val choiceList = fragment.getMultipleChoiceList()
        if (choiceList.isEmpty()) {
            Toast
                .makeText(this, getString(R.string.text_filter_no_selected), Toast.LENGTH_SHORT)
                .show()
            return false
        }
        return true
    }

    private fun showLoadingView() {
        progressBar =
            IosProgressBar.show(
                this,
                "",
                true,
                true
            ) {
            }
        progressBar.setOnDismissListener {
            viewModel.stopDownload()
        }
    }

    private fun dismissLoadingView() {
        if (progressBar.isShowing) progressBar.dismiss()
    }

    // 檢查權限
    private fun requirePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO
                )
            )
        } else {
            downloadPermissionResult.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    private fun cancelMultipleChoiceMode() {
        val fragment = fragmentListAdapter.currentFragment
        if (fragment is TodoOverviewFragment) return
        fragment?.let {
            it as BaseChatRoomFilterFragment
            binding.clMultipleChoiceBottomBar.visibility = if (isCurrentFragmentHasData()) View.GONE else View.INVISIBLE
            binding.tvMultipleChoice.text = getString(R.string.text_filter_multiple_choice)
            it.setIsMultipleChoiceMode(false)
            onSelected(0)
            binding.tvMultipleChoice.isSelected = false
        }
    }

    private fun setMultipleChoiceMode() {
        val fragment = fragmentListAdapter.currentFragment
        if (fragment is TodoOverviewFragment) return
        fragment?.let {
            it as BaseChatRoomFilterFragment
            if (it is FilterLinkFragment) {
                binding.ivDownload.visibility = View.GONE
            } else {
                binding.ivDownload.visibility = View.VISIBLE
            }
            binding.clMultipleChoiceBottomBar.visibility = View.VISIBLE
            binding.tvMultipleChoice.text = getString(R.string.cancel)
            it.setIsMultipleChoiceMode(true)
        }
    }

    private fun isCurrentFragmentHasData(): Boolean {
        when (val currentFragment = fragmentListAdapter.currentFragment) {
            is FilterMediaFragment -> {
                return currentFragment.isHasData()
            }

            is FilterLinkFragment -> {
                return currentFragment.isHasData()
            }

            is FilterFileFragment -> {
                return currentFragment.isHasData()
            }

            is TodoOverviewFragment -> {
                return true
            }
        }
        return false
    }

    override fun onSelected(size: Int) {
        binding.tvMultipleChoiceSelectedText.text =
            String.format(getString(R.string.text_filter_selected_text), size)
    }

    override fun onDataGet(isEmpty: Boolean) {
        if (isEmpty) cancelMultipleChoiceMode()
        CoroutineScope(Dispatchers.Main).launch {
            binding.clMultipleChoiceBottomBar.visibility =
                if (!isEmpty) View.GONE else View.INVISIBLE
        }
    }
}
