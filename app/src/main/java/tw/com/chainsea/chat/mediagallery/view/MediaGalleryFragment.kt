package tw.com.chainsea.chat.mediagallery.view

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.SeekBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.android.common.log.CELog
import tw.com.chainsea.ce.sdk.bean.msg.MessageType
import tw.com.chainsea.ce.sdk.bean.msg.content.IMessageContent
import tw.com.chainsea.ce.sdk.bean.msg.content.ImageContent
import tw.com.chainsea.ce.sdk.bean.msg.content.VideoContent
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEnum
import tw.com.chainsea.ce.sdk.event.EventBusUtils
import tw.com.chainsea.ce.sdk.event.EventMsg
import tw.com.chainsea.ce.sdk.event.MsgConstant
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.config.BundleKey
import tw.com.chainsea.chat.databinding.FragmentMediaGalleryBinding
import tw.com.chainsea.chat.lib.ToastUtils
import tw.com.chainsea.chat.mediagallery.viewmodel.MediaGalleryViewModel
import tw.com.chainsea.chat.messagekit.listener.PlayerCallback
import tw.com.chainsea.chat.ui.dialog.BottomSheetDialogBuilder
import tw.com.chainsea.chat.util.DaVinci
import tw.com.chainsea.chat.util.DownloadUtil
import tw.com.chainsea.chat.util.ThemeHelper
import tw.com.chainsea.custom.view.progress.IosProgressBar
import java.io.File

class MediaGalleryFragment :
    Fragment(),
    PlayerCallback {
    private val viewModel by activityViewModels<MediaGalleryViewModel>()
    private lateinit var progressBar: IosProgressBar
    private lateinit var binding: FragmentMediaGalleryBinding
    private var isComponentClick = false // 判斷點擊的是元件或空白處
    private var isCanceledDownload = false // 是否取消下載
    private lateinit var messageEntity: MessageEntity
    private var isVideoDownload = true // 是否下載影片
    private var onPermissionGranted: () -> Unit = {} // 權限請求成功
    private var isDownloading = false // 判斷是否已經在下載
    private val isGreenTheme = ThemeHelper.isGreenTheme()
    private var isServiceRoomTheme = ThemeHelper.isServiceRoomTheme

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val permissionsTIRAMISU: Array<String> =
        arrayOf(
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_IMAGES
        )
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            // Handle Permission granted/rejected
            if (isGranted) {
                onPermissionGranted()
            } else {
                activity?.let {
                    ToastUtils.showToast(
                        it,
                        getString(R.string.text_need_storage_permission)
                    )
                }
            }
        }
    private val requestMultiplePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            result.forEach { (permission, isGranted) ->
                if (!isGranted) {
                    if (permission == Manifest.permission.READ_MEDIA_IMAGES || permission == Manifest.permission.READ_MEDIA_VIDEO) {
                        activity?.let {
                            ToastUtils.showToast(it, getString(R.string.text_need_storage_permission))
                        }
                    }
                    return@registerForActivityResult
                }
            }
            onPermissionGranted()
        }

    private fun requestPermissionAndDoAction(action: () -> Unit) { // 請求權限並執行
        onPermissionGranted = action
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            requestMultiplePermissionLauncher.launch(permissionsTIRAMISU)
        }
    }

    companion object {
        fun newInstance(entity: MessageEntity): MediaGalleryFragment {
            val fragment = MediaGalleryFragment()
            val args = Bundle()
            args.putSerializable(BundleKey.EXTRA_MESSAGE.key(), entity)
            fragment.arguments = args
            return fragment
        }

        fun newInstance() = MediaGalleryFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = createDataBindingView(inflater, container)

    private fun createDataBindingView(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DataBindingUtil
        .inflate<FragmentMediaGalleryBinding>(
            inflater,
            R.layout.fragment_media_gallery,
            container,
            false
        ).apply {
            binding = this
            binding.scopeActions.setBackgroundColor(
                ResourcesCompat.getColor(
                    resources,
                    if (isGreenTheme && isServiceRoomTheme) {
                        R.color.color_6BC2BA
                    } else if (isGreenTheme) {
                        R.color.color_015F57
                    } else {
                        R.color.colorPrimary
                    },
                    null
                )
            )
            binding.clToolBar.setBackgroundColor(
                ResourcesCompat.getColor(
                    resources,
                    if (isGreenTheme && isServiceRoomTheme) {
                        R.color.color_6BC2BA
                    } else if (isGreenTheme) {
                        R.color.color_015F57
                    } else {
                        R.color.colorPrimary
                    },
                    null
                )
            )
            viewModel.apply {
                messageEntity = arguments?.getSerializable(BundleKey.EXTRA_MESSAGE.key()) as MessageEntity
                title.text = roomName.value
                val iContent = messageEntity.content()
                when (iContent) {
                    is ImageContent -> {
                        showLoadingView()
                        when {
                            iContent.url.endsWith(".gif") && iContent.url.startsWith("http") -> {
                                // gif file from network
                                Glide
                                    .with(requireActivity())
                                    .asGif()
                                    .load(iContent.url)
                                    .error(if (isGreenTheme) R.drawable.image_load_error_green else R.drawable.image_load_error)
                                    .listener(OnGifRequestListener(progressBar))
                                    .into(photoView)
                            }

                            iContent.url.endsWith(".gif") && !iContent.url.startsWith("http") -> {
                                // gif file from local
                                val file = File(iContent.url)
                                Glide
                                    .with(requireActivity())
                                    .asGif()
                                    .load(file)
                                    .error(if (isGreenTheme) R.drawable.image_load_error_green else R.drawable.image_load_error)
                                    .listener(OnGifRequestListener(progressBar))
                                    .into(photoView)
                            }

                            else -> {
                                Glide
                                    .with(requireActivity())
                                    .load(iContent.url)
                                    .dontTransform()
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .listener(OnRequestListener(progressBar))
                                    .fitCenter()
                                    .error(if (isGreenTheme) R.drawable.image_load_error_green else R.drawable.image_load_error)
                                    .into(photoView)
                            }
                        }

                        photoView.visibility = View.VISIBLE

                        when (viewModel.roomType.value) {
                            ChatRoomEnum.AI_CONSULTATION_ROOM -> { // AI諮詢引用
                                groupActionContainer.visibility = View.GONE
                                btnQuoteAction.visibility = View.VISIBLE
                                btnQuoteAction.setOnClickListener {
                                    isComponentClick = true
                                    showLoadingView()
//                                showQuoteDialog(iContent)
                                    lifecycleScope.launch(Dispatchers.Main) {
                                        if (!checkForSelfPermissions()) {
                                            requestPermissionAndDoAction { doStartDownloadImage(iContent.url) }
                                        } else {
                                            doStartDownloadImage(iContent.url)
                                        }
                                    }
                                }
                            }

                            else -> {}
                        }
                    }

                    is VideoContent -> {
                        val downloadVideoPath = getVideoSavePath(iContent)
                        iContent.filePath?.let {
                            val videoFile = File(it)
                            if (videoFile.exists()) {
                                checkPermissionBeforePlayVideo(iContent.filePath) // 本地上傳的影片
                            } else {
                                try {
                                    // 檢查本地下載影片
                                    val videoFile1 = File(downloadVideoPath)
                                    if (videoFile1.exists()) {
                                        checkPermissionBeforePlayVideo(videoFile1.absolutePath)
                                    } else {
                                        showDownloadVideoOption(iContent) // 其他android手機內的檔案路徑
                                    }
                                } catch (e: Exception) {
                                    // 本地影片不存在
                                    showDownloadVideoOption(iContent)
                                }
                            }
                        } ?: run {
                            try {
                                val videoFile = File(downloadVideoPath)
                                if (videoFile.exists()) {
                                    // 已下載的影片
                                    checkPermissionBeforePlayVideo(videoFile.absolutePath)
                                } else {
                                    // 尚未下載的影片
                                    showDownloadVideoOption(iContent)
                                }
                            } catch (e: Exception) {
                                // 本地影片不存在
                                showDownloadVideoOption(iContent)
                            }
                        }

                        loadingBar.setOnFileClickListener {
                            isComponentClick = true
                            isCanceledDownload = true
                            // 取消下載
                            lifecycleScope.launch(Dispatchers.Main) {
                                binding.ivVideoDownload.visibility = View.VISIBLE
                                binding.loadingBar.visibility = View.GONE
                            }
                            activity?.let {
                                ToastUtils.showToast(it, getString(R.string.canceled_downloading))
                            }
                        }

                        when (viewModel.roomType.value) {
                            ChatRoomEnum.AI_CONSULTATION_ROOM -> { // AI諮詢引用
                                groupActionContainer.visibility = View.GONE
                                btnQuoteAction.visibility = View.VISIBLE
                                btnQuoteAction.setOnClickListener {
                                    isComponentClick = true
                                    if (videoView.isPlaying()) videoView.pausePlayer()
                                    startQuoteVideo(iContent)
                                }
                            }

                            else -> {}
                        }
                    }
                }

                sendTouchedScreenEvent
                    .onEach {
                        // 點擊事件觸發功能列
                        if (!isComponentClick) {
                            if (iContent is VideoContent) {
                                llVideoControllerContainer.visibility = if (llVideoControllerContainer.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                                scopeActions.visibility = llVideoControllerContainer.visibility
                                clToolBar.visibility = llVideoControllerContainer.visibility
                                ivVideoPlay.setImageResource(
                                    if (videoView.isPlaying()) {
                                        R.drawable.ic_video_pause
                                    } else {
                                        R.drawable.ic_video_play
                                    }
                                )
                            } else {
                                scopeActions.visibility = if (scopeActions.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                                clToolBar.visibility = if (clToolBar.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                                llVideoControllerContainer.visibility = View.GONE
                            }
                        }
                        isComponentClick = false
                    }.launchIn(viewLifecycleOwner.lifecycleScope)

                ivShareAction.setOnClickListener {
                    // 分享
                    isComponentClick = true
                    if (!checkForSelfPermissions()) {
                        requestPermissionAndDoAction { doMediaShare(iContent, chatRoomId.value) }
                    } else {
                        lifecycleScope.launch(Dispatchers.IO) {
                            doMediaShare(iContent, chatRoomId.value)
                        }
                    }
                }
                ivDownloadAction.setOnClickListener {
                    if (isDownloading) return@setOnClickListener
                    // 下載
                    isComponentClick = true
                    lifecycleScope.launch(Dispatchers.Main) {
                        doMediaDownload(iContent)
                    }
                }
                leftAction.setOnClickListener {
                    // 返回
                    isComponentClick = true
                    activity?.finish()
                }
                ivVideoPlay.setOnClickListener {
                    // 播放
                    isComponentClick = true

                    if (videoView.isPlaying()) {
                        videoView.pausePlayer()
                        ivVideoPlay.setImageResource(R.drawable.ic_video_play)
                    } else {
                        videoView.startPlayer()
                        ivVideoPlay.setImageResource(R.drawable.ic_video_pause)
                    }
                }
                ivForwardVideo.setOnClickListener {
                    // 快轉
                    isComponentClick = true
                    videoView.doFastForwardVideo()
                }
                ivReverseVideo.setOnClickListener {
                    // 倒退
                    isComponentClick = true
                    videoView.doReverseVideo()
                }
            }
            lifecycleOwner = this@MediaGalleryFragment.viewLifecycleOwner
        }.root

    override fun onResume() {
        super.onResume()
        binding.apply {
//            if(isVideoDownload && messageEntity.content() is VideoContent) //判斷影片是否已經下載至本地
//                binding.ivDownloadAction.visibility = View.GONE

            if (clToolBar.visibility == View.VISIBLE) clToolBar.visibility = View.GONE
            if (scopeActions.visibility == View.VISIBLE) scopeActions.visibility = View.GONE
            if (llVideoControllerContainer.visibility == View.VISIBLE) llVideoControllerContainer.visibility = View.GONE
        }
    }

    private fun startQuoteVideo(videoContent: VideoContent) {
        lifecycleScope.launch(Dispatchers.Main) {
            showLoadingView()
//            showQuoteDialog(videoContent)
            val videoFile = File(getVideoSavePath(videoContent))
            if (videoFile.exists()) {
                // 已下載的影片直接引用
                doQuotedVideoToChatRoom(videoFile)
            } else {
                // 引用前先下載
                if (!checkForSelfPermissions()) {
                    requestPermissionAndDoAction {
                        startDownloadVideo(
                            videoContent,
                            true
                        )
                    }
                } else {
                    startDownloadVideo(videoContent, true)
                }
            }
        }
    }

    private fun startPlayVideo() {
        binding.apply {
//                ivDownloadAction.visibility = View.GONE
            photoView.visibility = View.GONE
            isVideoDownload = true
            videoView.startPlayer()
            videoView.playerCallback = this@MediaGalleryFragment
            // 播放进度条事件
            seekBarVideoProgress.setOnSeekBarChangeListener(
                object :
                    SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        p0: SeekBar?,
                        progress: Int,
                        p2: Boolean
                    ) {}

                    override fun onStartTrackingTouch(p0: SeekBar?) {
                        isComponentClick = true
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar) {
                        val total = seekBar.progress
                        videoView.player?.seekTo(total.toLong())
                    }
                }
            )
        }
    }

    private fun releasePlayVideo() =
        CoroutineScope(Dispatchers.Main).launch {
            if (binding.videoView.isPlaying()) {
                binding.videoView.releasePlayer()
            }
        }

    private fun showLoadingView() {
        activity?.let {
            progressBar =
                IosProgressBar.show(
                    it,
                    "",
                    true,
                    true
                ) { }
        }
    }

    class OnRequestListener(
        private val progressBar2: IosProgressBar
    ) : RequestListener<Drawable?> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable?>,
            isFirstResource: Boolean
        ): Boolean {
            try {
                if (progressBar2.isShowing) {
                    progressBar2.dismiss()
                }
            } catch (ignored: Exception) {
            }
            return false
        }

        override fun onResourceReady(
            resource: Drawable,
            model: Any,
            target: Target<Drawable?>?,
            dataSource: DataSource,
            isFirstResource: Boolean
        ): Boolean {
            try {
                if (progressBar2.isShowing) {
                    progressBar2.dismiss()
                }
            } catch (ignored: Exception) {
            }
            return false
        }
    }

    class OnGifRequestListener(
        private val progressBar2: IosProgressBar
    ) : RequestListener<GifDrawable?> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<GifDrawable?>,
            isFirstResource: Boolean
        ): Boolean {
            try {
                if (progressBar2.isShowing) {
                    progressBar2.dismiss()
                }
            } catch (ignored: Exception) {
            }
            return false
        }

        override fun onResourceReady(
            resource: GifDrawable,
            model: Any,
            target: Target<GifDrawable?>?,
            dataSource: DataSource,
            isFirstResource: Boolean
        ): Boolean {
            try {
                if (progressBar2.isShowing) {
                    progressBar2.dismiss()
                }
            } catch (ignored: Exception) {
            }
            return false
        }
    }

    private fun doMediaShare(
        content: IMessageContent<MessageType>,
        roomId: String?
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            when (content) {
                is ImageContent -> {
                    doHandleImageShare(content, roomId)
                }

                is VideoContent -> {
                    doHandleVideoShare(content, roomId)
                }
            }
        }
    }

    // 分享影片
    private fun doHandleVideoShare(
        content: VideoContent,
        roomId: String?
    ) {
        try {
            var videoFile: File? = null
            content.filePath?.let {
                videoFile = File(it)
            } ?: run {
                videoFile = File(getVideoSavePath(content))
            }
            videoFile?.let {
                val videoUri =
                    FileProvider.getUriForFile(
                        requireActivity(),
                        requireActivity().packageName + ".fileprovider",
                        it
                    )
                val sendIntent: Intent =
                    Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_STREAM, videoUri)
                        putExtra(BundleKey.ROOM_ID.key(), roomId)
                        type = "video/*"
                    }

                val shareIntent = Intent.createChooser(sendIntent, null)
                requireActivity().startActivity(shareIntent)
            }
        } catch (e: java.lang.Exception) {
            CELog.e(e.message, e)
        }
    }

    // 分享圖片
    private fun doHandleImageShare(
        content: ImageContent,
        roomId: String?
    ) = CoroutineScope(Dispatchers.IO).launch {
        val path = content.filePath
        if (URLUtil.isValidUrl(path)) {
            try {
                // 使用 Glide 加載圖片
                val bitmap =
                    Glide
                        .with(requireActivity())
                        .asBitmap()
                        .load(path)
                        .submit()
                        .get()
                val imageUri = getMediaUri(bitmap)
                imageUri?.let { uri ->
                    // 將圖片寫入 MediaStore
                    requireActivity().contentResolver.openOutputStream(uri)?.use { outputStream ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    }

                    // 創建 Intent 發送圖片
                    val sendIntent =
                        Intent().apply {
                            putExtra(BundleKey.ROOM_ID.key(), roomId)
                            action = Intent.ACTION_SEND
                            type = "image/*"
                            putExtra(Intent.EXTRA_STREAM, uri) // 使用插入後的 URI
                        }

                    val shareIntent = Intent.createChooser(sendIntent, null)
                    requireActivity().startActivity(shareIntent)
                } ?: run {
                    ToastUtils.showToast(requireActivity(), getString(R.string.text_share_media_failure))
                }
            } catch (e: Exception) {
                CELog.e(e.message, e)
                ToastUtils.showToast(requireActivity(), getString(R.string.text_share_media_failure))
            }
        } else {
            val entity = DaVinci.with(requireActivity()).imageLoader.getImage(path)
            if (entity != null) {
                val imageUri = getMediaUri(entity.bitmap)
                val sendIntent = Intent()
                sendIntent.putExtra(
                    BundleKey.ROOM_ID.key(),
                    roomId
                )
                sendIntent.setAction(Intent.ACTION_SEND_MULTIPLE)
                sendIntent.setType("image/*")
                sendIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
                requireActivity().startActivity(sendIntent)
            } else {
                ToastUtils.showToast(requireActivity(), requireActivity().getString(R.string.text_share_media_failure))
            }
        }
    }

    private suspend fun getMediaUri(bitmap: Bitmap?): Uri? =
        withContext(Dispatchers.IO) {
            bitmap?.let {
                // 使用 ContentValues 插入圖片到 MediaStore
                val contentValues =
                    ContentValues().apply {
                        put(
                            MediaStore.Images.Media.DISPLAY_NAME,
                            "image_${System.currentTimeMillis()}.jpg"
                        ) // 自定義名稱
                        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                        } // 公共圖片資料夾
                    }

                // 插入圖片並獲取 URI
                val contentResolver = requireActivity().contentResolver
                val imageUri =
                    contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                return@withContext imageUri
            } ?: return@withContext null
        }

    private fun showDownloadVideoOption(videoContent: VideoContent) {
        // 尚未下載的影片
        binding.apply {
            llVideoDownloadContainer.visibility = View.VISIBLE
            photoView.visibility = View.GONE
            isVideoDownload = false
            ivVideoDownload.setOnClickListener {
                isComponentClick = true
                // 下載影片
                if (!checkForSelfPermissions()) {
                    requestPermissionAndDoAction { startDownloadVideo(videoContent) }
                } else {
                    startDownloadVideo(videoContent)
                }
            }
        }
    }

    private fun doMediaDownload(content: IMessageContent<MessageType>) {
        when (content) {
            is ImageContent -> {
                BottomSheetDialogBuilder(requireActivity(), layoutInflater)
                    .doMediaDownloadAction {
                        if (!checkForSelfPermissions()) {
                            requestPermissionAndDoAction { doStartDownloadImage(content.url) }
                        } else {
                            doStartDownloadImage(content.url)
                        }
                    }.show()
            }

            is VideoContent -> {
                // 下載影片
                if (!checkForSelfPermissions()) {
                    requestPermissionAndDoAction { startDownloadVideo(content) }
                } else {
                    startDownloadVideo(content)
                }
            }
        }
    }

    private fun checkForSelfPermissions(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            for (permission in permissionsTIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        requireActivity(),
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }

    private fun doStartDownloadImage(url: String) {
        DownloadUtil.downloadImageFromUrl(url, { filePath ->
            filePath?.let {
                val file = File(it)
                galleryAddPic(file)
            }
        }, { _ ->
            showToastForAIConsultation(R.string.bruce_photo_save_failed)
        })
    }

    private fun galleryAddPic(file: File) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val contentUri = Uri.fromFile(file)
        mediaScanIntent.setData(contentUri)
        activity?.sendBroadcast(mediaScanIntent)
        showToastForAIConsultation(R.string.text_save_picture_to_album_already)
        if (viewModel.roomType.value == ChatRoomEnum.AI_CONSULTATION_ROOM) {
            // 先儲存才能引用
            EventBusUtils.sendEvent(EventMsg(MsgConstant.MESSAGE_AI_CONSULTATION_QUOTED_IMAGE, file.absolutePath))
            activity?.finish()
        }
        EventBusUtils.sendEvent(EventMsg<Any?>(MsgConstant.NOTICE_MEDIA_SELECTOR_REFRESH)) // 更新聊天室MediaSelector2Layout
    }

    private fun doQuotedVideoToChatRoom(file: File) {
        EventBusUtils.sendEvent(EventMsg(MsgConstant.MESSAGE_AI_CONSULTATION_QUOTED_VIDEO, file.absolutePath))
        activity?.finish()
    }

    override fun onPause() {
        super.onPause()
        binding.apply {
            if (clToolBar.visibility == View.VISIBLE) clToolBar.visibility = View.GONE
            if (scopeActions.visibility == View.VISIBLE) scopeActions.visibility = View.GONE
            if (llVideoControllerContainer.visibility == View.VISIBLE) llVideoControllerContainer.visibility = View.GONE
            if (videoView.isPlaying()) videoView.pausePlayer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.videoView.releasePlayer()
        binding.videoView.playerCallback = null
    }

    override fun onVideoPlayedTimeChanged(
        currentTime: Int,
        duration: Int
    ) {
        lifecycleScope.launch(Dispatchers.Main) {
            binding.apply {
                tvPlayerTime.text = videoView.getUpdateTimeFormat(currentTime)
                tvPlayerTimeLeft.text = getString(R.string.text_video_time_left, videoView.getUpdateTimeFormat(duration - currentTime))
                seekBarVideoProgress.progress = currentTime
                seekBarVideoProgress.max = duration
                if (videoView.isPlaying()) {
                    ivVideoPlay.setImageResource(R.drawable.ic_video_pause)
                } else {
                    ivVideoPlay.setImageResource(R.drawable.ic_video_play)
                }
            }
        }
    }

    private fun startDownloadVideo(
        videoContent: VideoContent,
        isQuote: Boolean = false
    ) = CoroutineScope(Dispatchers.IO).launch {
        isDownloading = true
        releasePlayVideo()
        lifecycleScope.launch(Dispatchers.Main) {
            binding.loadingBar.visibility = View.VISIBLE
            binding.ivVideoDownload.visibility = View.GONE
            binding.loadingBar.progress = 0
            binding.llVideoControllerContainer.visibility = View.GONE
            binding.llVideoDownloadContainer.visibility = View.VISIBLE
        }
        binding.apply {
            loadingBar.progress = 0
            videoContent.progress = "0"
            DownloadUtil.doDownloadVideoFile(
                videoContent,
                onDownloadProgress = { progress ->
                    loadingBar.progress = progress
                },
                onDownloadSuccess = { file ->
                    binding.videoView.initMediaController()
                    loadingBar.isCanceledLoading = false
                    videoContent.isDownload = !isCanceledDownload
                    if (!isCanceledDownload) {
                        videoContent.android_local_path = file.absolutePath
                        lifecycleScope.launch(Dispatchers.Main) {
                            llVideoDownloadContainer.visibility = View.GONE
                        }
                        if (isQuote) {
                            doQuotedVideoToChatRoom(file)
                        } else {
                            checkPermissionBeforePlayVideo(file.absolutePath)
                        }
                    } else {
                        videoContent.progress = null
                        loadingBar.progress = 0
                        lifecycleScope.launch(Dispatchers.Main) {
                            loadingBar.visibility = View.GONE
                            llVideoDownloadContainer.visibility = View.VISIBLE
                        }
                        DownloadUtil.handleCancelDownload(
                            file,
                            isVideoDownload = {
                                isVideoDownload = it
                            }
                        )
                    }
                    ToastUtils.showToast(requireActivity(), getString(R.string.text_filter_download_media_succeed))
                    EventBusUtils.sendEvent(EventMsg<Any?>(MsgConstant.NOTICE_MEDIA_SELECTOR_REFRESH)) // 更新聊天室MediaSelector2Layout
                    isDownloading = false
                },
                onDownloadFailed = {
                    ToastUtils.showToast(requireActivity(), getString(R.string.text_download_media_failure))
                    isDownloading = false
                }
            )
        }
    }

    private fun showToastForAIConsultation(message: Int) =
        CoroutineScope(Dispatchers.Main).launch {
            when (viewModel.roomType.value) {
                ChatRoomEnum.AI_CONSULTATION_ROOM -> {
                    if (message == R.string.bruce_photo_save_failed) {
                        // 下載失敗在AI諮詢是顯示「引用失敗 」
                        activity?.let {
                            ToastUtils.showToast(it, getString(R.string.text_media_quote_failure))
                        }
                    }
                }

                ChatRoomEnum.NORMAL_ROOM -> {
                    activity?.let {
                        ToastUtils.showToast(it, getString(message))
                    }
                }
            }
        }

    private fun getVideoSavePath(iContent: VideoContent) = DownloadUtil.downloadFileDir + iContent.name

    private fun checkPermissionBeforePlayVideo(videoPath: String) {
        binding.videoView.initPlayer(videoPath)
        if (!checkForSelfPermissions()) {
            requestPermissionAndDoAction {
                startPlayVideo()
            }
        } else {
            startPlayVideo()
        }
    }
}
