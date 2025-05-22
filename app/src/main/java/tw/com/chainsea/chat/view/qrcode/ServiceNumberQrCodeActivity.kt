package tw.com.chainsea.chat.view.qrcode

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DimenRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.common.collect.Lists
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.config.BundleKey
import tw.com.chainsea.chat.databinding.ActivityServiceNumberQrCodeBinding
import tw.com.chainsea.chat.lib.ToastUtils
import tw.com.chainsea.chat.network.contact.ViewModelFactory
import tw.com.chainsea.chat.util.IntentUtil
import tw.com.chainsea.chat.view.BaseActivity
import java.util.Objects

class ServiceNumberQrCodeActivity : BaseActivity() {

    private lateinit var binding: ActivityServiceNumberQrCodeBinding
    private val serviceNumberQrCodeViewModel by lazy {
        val factory = ViewModelFactory(application)
        ViewModelProvider(this, factory)[ServiceNumberQrCodeViewModel::class.java]
    }
    private lateinit var qrCodeAdapter: QrCodeAdapter
    private var targetPosition = 0

    private var qrCodeData: QrCodeData? = null
    private val shareMoreDialog: ServiceNumberQrCodeShareMoreDialog by lazy {
        ServiceNumberQrCodeShareMoreDialog(
            this
        )
    }

    // 往上滑動 listener
    private val gesture: GestureDetector by lazy {
        GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                if (distanceY > 50 && !shareMoreDialog.isShowing) {
                    shareMore()
                }

                return super.onScroll(e1, e2, distanceX, distanceY)
            }
        })
    }

    private val downloadPermissionResult: ActivityResultLauncher<String> =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                downloadQrCode()
            } else {
                Toast.makeText(
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
                if (Objects.requireNonNull<Boolean?>(result[Manifest.permission.READ_MEDIA_IMAGES]) == true && Objects.requireNonNull<Boolean?>(
                        result[Manifest.permission.READ_MEDIA_VIDEO]
                    ) == true
                ) {
                    downloadQrCode()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.text_need_storage_permission),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        targetPosition = intent.getIntExtra(BundleKey.TARGET_QR_CODE_POSITION.key(), 0)
        window.statusBarColor = Color.WHITE
        binding = ActivityServiceNumberQrCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initListener()
        initRecyclerView()
        observerData()
        serviceNumberQrCodeViewModel.getSelfProfileData()


    }

    private fun initListener() {
        binding.ivBack.setOnClickListener { finish() }
        binding.ivEdit.setOnClickListener {
            Toast.makeText(this, getString(R.string.text_please_go_to_desktop), Toast.LENGTH_SHORT)
                .show()
        }

        binding.vpQrCode.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setPageInfo(position)
            }
        })

        binding.llCopy.setOnClickListener {
            qrCodeData?.let {
                val clipboard: ClipboardManager =
                    getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(getString(R.string.app_name), it.qrCodeLink)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(
                    this,
                    getString(R.string.text_qr_code_copy_success),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.llDownload.setOnClickListener {
            requirePermission()
        }

        binding.llShareToLine.setOnClickListener {
            qrCodeData?.let {
                it.serviceNumber?.let {
                    it.businessCardInfo?.let {
                        IntentUtil.shareToLine(this, it.shareLiffUrl)
                    }
                }
            }
        }

        binding.llShareMore.setOnClickListener {
            shareMore()
        }
    }

    private fun initRecyclerView() {
        binding.vpQrCode.apply {
            qrCodeAdapter = QrCodeAdapter()
            adapter = qrCodeAdapter
            setPreviewBothSide(R.dimen.dp_20,R.dimen.dp_35)
            val recyclerView = getChildAt(0) as RecyclerView
            recyclerView.apply {
                overScrollMode = View.OVER_SCROLL_NEVER
                clipChildren = false
                clipToPadding = false
            }
        }
    }

    private fun observerData() {
        serviceNumberQrCodeViewModel.qrCodeList.observe(this) {
            qrCodeAdapter.submitList(it)
        }

        serviceNumberQrCodeViewModel.onSelfProfileDataGet.observe(this) {
            if (it != null) {
                serviceNumberQrCodeViewModel.getQrCodeData()
                shareMoreDialog.setUserProfileData(it)
                shareMoreDialog.setTenantData(TokenPref.getInstance(this).cpCurrentTenant)
            }
        }

        serviceNumberQrCodeViewModel.onDownloadBusinessCardSuccess.observe(this) {
            if (it) {
                ToastUtils.showToast(
                    this,
                    getString(R.string.photo_save_to_photo)
                )
            }
        }

        serviceNumberQrCodeViewModel.initStartPosition.observe(this) {
            if (it) {
                if (binding.vpQrCode.currentItem == 0) {
                    binding.vpQrCode.currentItem = targetPosition
                    setPageInfo(targetPosition)
                }
            }
        }
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
        } else downloadPermissionResult.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    // 下載 qr code
    private fun downloadQrCode() = CoroutineScope(Dispatchers.IO).launch {
        qrCodeData?.let { qrCodeData ->
            qrCodeData.serviceNumber?.let { serviceNumber ->
                serviceNumber.businessCardInfo?.let {
                    serviceNumberQrCodeViewModel.downloadBusinessCard(it.imageCardUrl)
                }
            }
        }
    }

    // 分享更多
    private fun shareMore() {
        qrCodeData?.let { qrCodeData ->
            qrCodeData.qrCode?.let {
                shareMoreDialog.apply {
                    setQrCodeData(qrCodeData)
                    show()
                }
            }
        }
    }

    // 顯示頁面資訊
    private fun setPageInfo(position: Int) = CoroutineScope(Dispatchers.Main).launch {
        qrCodeData = qrCodeAdapter.getCurrentQrCodeData(position)
        val isCanShowBottomBar = qrCodeAdapter.isCanShowBottomBar(position)
        val visibility = if (isCanShowBottomBar) View.VISIBLE else View.INVISIBLE
        binding.llBottomBar.visibility = visibility
        binding.ivSlideIcon.visibility = visibility
        binding.tvSlideUp.visibility = visibility
        binding.ivEdit.visibility = visibility
        CoroutineScope(Dispatchers.Main).launch {
            binding.tvTitle.text = qrCodeAdapter.getCurrentTitle(position)
        }
    }


    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        gesture.onTouchEvent(event)
        return super.dispatchTouchEvent(event)
    }
}

// 讓viewpager左右兩邊的item能夠出現在畫面上
fun ViewPager2.setPreviewBothSide(@DimenRes nextItemVisibleSize: Int, @DimenRes currentItemHorizontalMargin: Int) {
    this.offscreenPageLimit = 1
    val nextItemVisiblePx = resources.getDimension(nextItemVisibleSize)
    val currentItemHorizontalMarginPx = resources.getDimension(currentItemHorizontalMargin)
    val pageTranslationX = nextItemVisiblePx + currentItemHorizontalMarginPx
    val pageTransformer = ViewPager2.PageTransformer { page: View, position: Float ->
        page.translationX = -pageTranslationX * position
    }
    this.setPageTransformer(pageTransformer)

    val itemDecoration = HorizontalMarginItemDecoration(
        context,
        currentItemHorizontalMargin
    )
    this.addItemDecoration(itemDecoration)
}