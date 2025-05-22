package tw.com.chainsea.chat.util

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.MediaController
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.annotation.OptIn
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.ActivityMovieBinding
import tw.com.chainsea.chat.messagekit.listener.PlayerCallback

class PlayerView : ConstraintLayout {
    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val defaultWidth: Int = 1920
    private val defaultHeight: Int = 1080

    // 播放器
    var player: ExoPlayer? = null

    // 進度條
    private var seekBarJob: Job? = null
    var playerCallback: PlayerCallback? = null

    // 实例化音量控制器
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
    private var binding: ActivityMovieBinding =
        ActivityMovieBinding.inflate(LayoutInflater.from(context), this, true)

    private val scaleGestureDetector =
        ScaleGestureDetector(
            context,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    val scaleFactor = detector.scaleFactor

                    // 根據 scaleFactor 改變視頻的尺寸
                    val layoutParams = binding.videoCVV.layoutParams
                    layoutParams.width = (layoutParams.width * scaleFactor).toInt()
                    layoutParams.height = (layoutParams.height * scaleFactor).toInt()
                    binding.videoCVV.layoutParams = layoutParams

                    return true
                }
            }
        )

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean = scaleGestureDetector.onTouchEvent(event) || super.onTouchEvent(event)

    init {
        initVolume()
        initMediaController()
    }

    private fun initVolume() {
        audioManager?.let {
            val volumeMax = it.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            binding.volumeControlSB.max = volumeMax
            // 預設靜音
            binding.volumeControlSB.progress = 0
        }
    }

    fun initMediaController() {
        // 实例化控制器
        val controller = MediaController(context)
        controller.visibility = GONE
        initListener()
    }

    private fun initListener() {
        binding.expandIV.setOnClickListener { }
        binding.playIV.setOnClickListener {
            if (player?.isPlaying == true) {
                pausePlayer()
            } else {
                startPlayer()
            }
        }
        binding.videoCVV.setOnCompletionListener {
            binding.playIV.setImageResource(R.drawable.icon_play_blue)
        }

        // 播放进度条事件
        binding.seekSB.setOnSeekBarChangeListener(
            object : OnSeekBarChangeListener {
                override fun onProgressChanged(
                    p0: SeekBar?,
                    progress: Int,
                    p2: Boolean
                ) {
                    val currentTime = getUpdateTimeFormat(progress)
                    binding.currentTimeTV.text = currentTime
                    player?.let {
                        if (it.duration.toInt() == progress) {
                            pausePlayer()
                            it.seekTo(0)
                        }
                    }
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    val total = seekBar.progress
                    player?.seekTo(total.toLong())
                }
            }
        )
    }

    @OptIn(UnstableApi::class)
    fun initPlayer(path: String) =
        CoroutineScope(Dispatchers.Main).launch {
            val defaultBandwidthMeter = DefaultBandwidthMeter.Builder(context).build()
            val defaultTrackSelector = DefaultTrackSelector(context)
            val mediaItem = MediaItem.fromUri(Uri.parse(path))
            player =
                ExoPlayer
                    .Builder(context)
                    .setTrackSelector(defaultTrackSelector)
                    .setBandwidthMeter(defaultBandwidthMeter)
                    .build()
            player?.apply {
                setVideoSurfaceView(binding.videoCVV)
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = true
//            repeatMode = Player.REPEAT_MODE_ALL
            }
            player?.seekTo(0)
            player?.addListener(
                object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        super.onPlaybackStateChanged(playbackState)
                        when (playbackState) {
                            Player.STATE_ENDED -> {
                                seekBarJob?.cancel()
                            }

                            Player.STATE_READY -> {
                                startPlayer()
                            }

                            else -> {}
                        }
                    }
                }
            )
            // 監聽視頻大小
            player?.addListener(
                object : Player.Listener {
                    override fun onVideoSizeChanged(videoSize: VideoSize) {
                        super.onVideoSizeChanged(videoSize)
                        val videoWidth = videoSize.width
                        val videoHeight = videoSize.height

                        // 獲取螢幕的寬度和高度
                        val displayMetrics = context.resources.displayMetrics
                        val screenWidth = displayMetrics.widthPixels
                        val screenHeight = displayMetrics.heightPixels

                        // 計算視頻的寬度和高度與螢幕的寬度和高度的比例
                        val widthRatio = videoWidth.toFloat() / screenWidth
                        val heightRatio = videoHeight.toFloat() / screenHeight

                        // 根據視頻的寬度和高度與螢幕的寬度和高度的比例來設定 PlayerView 的寬度和高度
                        val layoutParams = binding.videoCVV.layoutParams
                        if (widthRatio > heightRatio) {
                            layoutParams.width = screenWidth
                            layoutParams.height = (videoHeight / widthRatio).toInt()
                        } else {
                            layoutParams.width = (videoWidth / heightRatio).toInt()
                            layoutParams.height = screenHeight
                        }
                        binding.videoCVV.layoutParams = layoutParams
                    }
                }
            )

            seekBarJob = currentDuration()
        }

    fun pausePlayer() =
        CoroutineScope(Dispatchers.Main).launch {
            player?.pause()
            binding.playIV.setImageResource(R.drawable.icon_play_blue)
        }

    fun startPlayer() =
        CoroutineScope(Dispatchers.Main).launch {
            player?.play()
            binding.playIV.setImageResource(R.drawable.icon_pause_blue)
        }

    private fun currentDuration() =
        CoroutineScope(Dispatchers.IO).launch {
            player?.let {
                while (true) {
                    withContext(Dispatchers.Main) {
                        val duration = it.duration.toInt()
                        val currentTime = it.contentPosition.toInt()
                        // 目前時間
                        val currentTimeString = getUpdateTimeFormat(currentTime)
                        binding.currentTimeTV.text = currentTimeString
                        binding.seekSB.progress = currentTime

                        // 總共時間
                        val totalTimeString = getUpdateTimeFormat(duration)
                        binding.totallyTimeTV.text = totalTimeString
                        binding.seekSB.max = duration
                        playerCallback?.onVideoPlayedTimeChanged(currentTime, duration)
                    }
                }
            }
        }

    fun releasePlayer() {
        player?.release()
        player = null
        seekBarJob?.cancel()
    }

    fun isPlaying(): Boolean = player?.isPlaying ?: false

    fun doFastForwardVideo() {
        player?.let {
            it.seekTo(it.currentPosition + 10000)
        }
    }

    fun doReverseVideo() {
        player?.let {
            it.seekTo(it.currentPosition - 10000)
        }
    }

    /**
     * 时间格式化
     *
     * @param millisecond 总时间 毫秒
     */
    @SuppressLint("DefaultLocale")
    fun getUpdateTimeFormat(millisecond: Int): String {
        // 将毫秒转换为秒
        val second = millisecond / 1000
        // 计算小时
        val hh = second / 3600
        // 计算分钟
        val mm = second % 3600 / 60
        // 计算秒
        val ss = second % 60
        // 判断时间单位的位数
        var str: String? = null
        str =
            if (hh != 0) { // 表示时间单位为三位
                String.format("%02d:%02d:%02d", hh, mm, ss)
            } else {
                String.format("%02d:%02d", mm, ss)
            }
        return str
    }

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        // 设置宽高
        val defaultWidth = getDefaultSize(defaultWidth, widthMeasureSpec)
        val defaultHeight = getDefaultSize(defaultHeight, heightMeasureSpec)
        setMeasuredDimension(defaultWidth, defaultHeight)
    }
}
