package tw.com.chainsea.chat.keyboard.emoticon.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import tw.com.chainsea.chat.BuildConfig
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.RecordLayoutBinding
import tw.com.chainsea.chat.messagekit.lib.AudioLib
import tw.com.chainsea.chat.util.ThemeHelper
import java.text.SimpleDateFormat
import java.util.Timer
import java.util.TimerTask
import kotlin.math.abs

class RecordLayout : ConstraintLayout {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, onRecordAudioFinish: (String, Int) -> Unit) : this(context, null) {
        this.onRecordAudioFinish = onRecordAudioFinish
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val binding: RecordLayoutBinding =
        RecordLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    // 錄製的時長
    private var recordTime = 0L

    // 限制的時長(Debug: 30秒, 正式: 120秒)
    private val limitRecordTime = if (BuildConfig.DEBUG) 30000 else 120000

    private var timer: Timer? = null

    private var recordAudioPath = ""

    private lateinit var onRecordAudioFinish: (String, Int) -> Unit

    private val audioLib = AudioLib.getInstance(context)

    // 取消錄音需要往上滑動的距離
    private val cancelY = 200

    // 使用者一開始的Y
    private var pointY = 0f
    private var isGreenTheme = false

    init {
        setListener()
        isGreenTheme = ThemeHelper.isGreenTheme()
        binding.ivRecordAudio.setImageResource(if (isGreenTheme) R.drawable.icon_record_audio_off_green else R.drawable.icon_record_audio_off)
    }

    fun setFinishCallBack(onRecordAudioFinish: (String, Int) -> Unit) {
        this.onRecordAudioFinish = onRecordAudioFinish
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListener() {
        binding.ivRecordAudio.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    pointY = motionEvent.y
                    startRecordAudio()
                }

                MotionEvent.ACTION_MOVE -> {
                    if (abs(pointY - motionEvent.y) > cancelY) {
                        showCancelRecordAudioText()
                    } else {
                        showStartRecordAudioText()
                    }
                }

                MotionEvent.ACTION_UP -> {
                    if (abs(pointY - motionEvent.y) > cancelY) {
                        stopRecordAudio()
                    } else {
                        checkRecordTime()
                    }
                }
            }

            true
        }
    }

    // 顯示要取消時的文字
    private fun showCancelRecordAudioText() {
        binding.tvRecordCancel.visibility = View.VISIBLE
        binding.tvRecordHint.visibility = View.INVISIBLE
        binding.tvRecordTime.visibility = View.INVISIBLE
    }

    // 顯示正在錄音的文字
    private fun showStartRecordAudioText() {
        binding.tvRecordCancel.visibility = View.INVISIBLE
        binding.tvRecordHint.visibility = View.INVISIBLE
        binding.tvRecordTime.visibility = View.VISIBLE
    }

    // 顯示停止錄音的文字
    @SuppressLint("SetTextI18n")
    private fun showStopRecordAudioText() {
        binding.tvRecordTime.text = "00:00"
        binding.tvRecordCancel.visibility = View.INVISIBLE
        binding.tvRecordHint.visibility = View.VISIBLE
        binding.tvRecordTime.visibility = View.INVISIBLE
    }

    @SuppressLint("SimpleDateFormat")
    private fun startRecordAudio() {
        binding.ivRecordAudio.setImageResource(if (isGreenTheme) R.drawable.icon_record_audio_on_green else R.drawable.icon_record_audio_on)
        // 時間計算
        recordTime = System.currentTimeMillis()
        showStartRecordAudioText()

        if (timer == null) timer = Timer()
        timer!!.schedule(
            object : TimerTask() {
                override fun run() {
                    binding.tvRecordTime.post {
                        binding.tvRecordTime.text =
                            SimpleDateFormat("mm:ss").format(System.currentTimeMillis() - recordTime)
                    }
                }
            },
            50,
            1000
        )
        recordAudioPath = AudioLib.getInstance(context).generatePath(context)
        // 開始錄製
        audioLib.start(recordAudioPath) {
            // 這邊是 db 大小 -> 可用於之後的音波顯示
        }
    }

    private fun checkRecordTime() {
        val currentRecordTime = System.currentTimeMillis() - recordTime
        if (currentRecordTime < 1000) {
            Toast.makeText(context, "聲音訊息過短", Toast.LENGTH_SHORT).show()
        } else if (currentRecordTime > limitRecordTime) {
            Toast.makeText(context, "錄音時常限制為2分鐘!", Toast.LENGTH_SHORT).show()
        } else {
            val period = audioLib.complete()
            onRecordAudioFinish.invoke(recordAudioPath, abs(period))
        }
        stopRecordAudio()
    }

    private fun stopRecordAudio() {
        binding.ivRecordAudio.setImageResource(if (isGreenTheme) R.drawable.icon_record_audio_off_green else R.drawable.icon_record_audio_off)
        showStopRecordAudioText()
        audioLib.cancel()
        timer?.cancel()
        timer?.purge()
        timer = null
        recordTime = 0
    }
}
