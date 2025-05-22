package tw.com.chainsea.chat.mediagallery.view

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.WindowManager
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.android.common.json.JsonHelper
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEnum
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.config.BundleKey
import tw.com.chainsea.chat.databinding.ActivityMediaGalleryBinding
import tw.com.chainsea.chat.mediagallery.viewmodel.MediaGalleryViewModel
import tw.com.chainsea.chat.network.contact.ViewModelFactory
import tw.com.chainsea.chat.ui.adapter.MediaPagerAdapter
import tw.com.chainsea.chat.util.ThemeHelper
import tw.com.chainsea.chat.util.serializable
import tw.com.chainsea.chat.view.BaseActivity
import kotlin.math.abs

class MediaGalleryActivity : BaseActivity() {
    private lateinit var binding: ActivityMediaGalleryBinding
    private var messageEntity: MessageEntity? = null
    private val roomId by lazy {
        intent.getStringExtra(BundleKey.ROOM_ID.key())
    }
    private val roomName by lazy {
        intent.getStringExtra(BundleKey.CHAT_ROOM_NAME.key())
    }
    private val roomType by lazy {
        intent.getStringExtra(BundleKey.ROOM_TYPE.key())
    }
    private val isFromFilterPage by lazy {
        intent.getBooleanExtra(BundleKey.IS_FROM_FILTER.key(), false)
    }

    private val messageSort by lazy {
        intent.getStringExtra(BundleKey.MESSAGE_SORT.key()) ?: "DESC"
    }

    private lateinit var mediaGalleryViewModel: MediaGalleryViewModel
    private lateinit var mediaPagerAdapter : MediaPagerAdapter
    private var downX: Float = 0f
    private var downY: Float = 0f
    private val clickDistanceThreshold = 10
    private val isGreenTheme by lazy { ThemeHelper.isGreenTheme() }
    private val isServiceRoomTheme = ThemeHelper.isServiceRoomTheme
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityMediaGalleryBinding>(
            this,
            R.layout.activity_media_gallery
        ).apply {
            binding = this
            window.statusBarColor = ResourcesCompat.getColor(resources, if(isGreenTheme&&isServiceRoomTheme) R.color.color_6BC2BA else if(isGreenTheme) R.color.color_015F57 else R.color.colorPrimary, null)
            initViewModel()
            initAdapter()
            intent?.let {
                messageEntity = it.serializable(BundleKey.PHOTO_GALLERY_MESSAGE.key())
            }
            roomType?.let {
                mediaGalleryViewModel.roomType.value = ChatRoomEnum.valueOf(it) //聊天室類型
                when(it) {
                    ChatRoomEnum.NORMAL_ROOM.name -> { //一般聊天室
                        handleNormalRoomLogic()
                    }
                    ChatRoomEnum.AI_CONSULTATION_ROOM.name -> { //AI諮詢聊天室
                        handleAiConsultationRoomLogic()
                    }
                }
            }

            lifecycleOwner = this@MediaGalleryActivity
        }
    }
    //AI諮詢聊天室處理邏輯
    private fun handleAiConsultationRoomLogic() {
        roomId?.let {
            mediaGalleryViewModel.getChatRoomInfo(roomId)
        }
        val listType = object : TypeToken<List<MessageEntity>>() {}.type
        val data = JsonHelper.getInstance().from<List<MessageEntity>>(
            intent.getStringExtra(BundleKey.EXTRA_SESSION_LIST.key()),
            listType
        )
        data?.let {
            mediaPagerAdapter.setData(it)
            mediaPagerAdapter.notifyItemChanged(it.indexOf(messageEntity))
            binding.viewpager2.setCurrentItem(it.indexOf(messageEntity), false)
        }
    }

    //一般聊天室處理邏輯
    private fun handleNormalRoomLogic() {
        roomId?.let {
            mediaGalleryViewModel.chatRoomId.value = it
        }
        roomName?.let {
            mediaGalleryViewModel.roomName.value = it
        } ?:run {
            mediaGalleryViewModel.getChatRoomInfo(roomId)
        }
        mediaGalleryViewModel.apply {
            initData(messageEntity, isFromFilterPage, messageSort)

            sendMediaPosition.onEach {
                mediaPagerAdapter.setData(it.second)
                mediaPagerAdapter.notifyItemChanged(it.first)
                binding.viewpager2.setCurrentItem(it.first, false)
            }.launchIn(this@MediaGalleryActivity.lifecycleScope)
        }
    }

    private fun initAdapter() {
        mediaPagerAdapter = MediaPagerAdapter(this@MediaGalleryActivity)
        binding.viewpager2.adapter = mediaPagerAdapter
    }

    private fun initViewModel() {
        val factory = ViewModelFactory(application)
        mediaGalleryViewModel =
            ViewModelProvider(this, factory)[MediaGalleryViewModel::class.java]
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        // 判斷滑動/點擊
        ev?.let {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = it.x
                    downY = it.y
                }
                MotionEvent.ACTION_UP -> {
                    if (isAClick(downX, it.x, downY, it.y)) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            lifecycleScope.launch {
                                mediaGalleryViewModel.sendTouchedScreenEvent.emit(Unit)
                            }
                        } , 100)
                    }
                }

                else -> {}
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun isAClick(startX: Float, endX: Float, startY: Float, endY: Float): Boolean {
        // 判定是否為點擊事件
        val differenceX = abs(startX - endX)
        val differenceY = abs(startY - endY)
        return differenceX < clickDistanceThreshold && differenceY < clickDistanceThreshold
    }
}