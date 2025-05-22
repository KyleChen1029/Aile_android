package tw.com.chainsea.chat.messagekit.main.viewholder

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewTreeObserver
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.android.common.text.KeyWordHelper
import tw.com.chainsea.ce.sdk.bean.msg.MessageFlag
import tw.com.chainsea.ce.sdk.bean.msg.SourceType
import tw.com.chainsea.ce.sdk.bean.msg.content.TextContent
import tw.com.chainsea.ce.sdk.bean.msg.content.UndefContent
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.reference.UserProfileReference
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.ItemMessageSystemBinding
import tw.com.chainsea.chat.messagekit.main.adapter.MessageAdapterMode
import tw.com.chainsea.chat.messagekit.main.adapter.OnMessageClickListener
import tw.com.chainsea.chat.util.ThemeHelper
import java.text.SimpleDateFormat
import java.util.Locale

class TipMessageView1(
    private val binding: ItemMessageSystemBinding
) : RecyclerView.ViewHolder(binding.root) {
    private val selfUserId by lazy { TokenPref.getInstance(binding.root.context).userId }
    private val isGreenTheme by lazy { ThemeHelper.isGreenTheme() }
    private var mode: MessageAdapterMode = MessageAdapterMode.DEFAULT
    private var onMessageClickListener: OnMessageClickListener? = null
    private val onGlobalLayoutListener by lazy {
        ViewTreeObserver.OnGlobalLayoutListener {
            if (binding.root.height != 0) {
                binding.maskLayer.layoutParams.height = binding.root.height
                binding.maskLayer.invalidate()
                binding.maskLayer.requestLayout()
            }
        }
    }

    fun setOnTipClickListener(onMessageClickListener: OnMessageClickListener?) {
        this.onMessageClickListener = onMessageClickListener
    }

    @SuppressLint("SetTextI18n")
    fun bind(message: MessageEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            setModeView(message)
            when {
                SourceType.SYSTEM == message.sourceType && message.content() is UndefContent -> {
                    val undefContent = message.content() as UndefContent
                    withContext(Dispatchers.Main) {
                        binding.tvMessage.apply {
                            if ("TIME_LINE" == undefContent.text) {
                                setTextColor(
                                    if (isGreenTheme) {
                                        ResourcesCompat.getColor(
                                            binding.root.resources,
                                            R.color.color_015F57,
                                            null
                                        )
                                    } else {
                                        -0x894635
                                    }
                                )
                                setBackgroundResource(if (isGreenTheme) R.drawable.time_msg_bg_green else R.drawable.time_msg_bg)
                                val date =
                                    SimpleDateFormat(
                                        "MMMdd日(EEE)",
                                        Locale.TAIWAN
                                    ).format(message.sendTime)
                                text = date
                            } else if ("UNREAD" == undefContent.text) {
                                setTextColor(Color.parseColor("#888888"))
                                setBackgroundResource(R.drawable.sys_msg_bg)
                                text = "以下為未讀訊息"
                            }
                        }
                    }
                }

                SourceType.SYSTEM == message.sourceType && message.content() is TextContent -> {
                    val textContent = (message.content() as TextContent).simpleContent()
                    withContext(Dispatchers.Main) {
                        if (textContent.contains("進線") ||
                            textContent
                                .uppercase(Locale.ROOT)
                                .contains("END")
                        ) {
                            binding.tvMessage.apply {
                                setTextColor(-0x1)
                                setBackgroundResource(R.drawable.sys_msg_current_state_bg)
                                text = textContent.replace("-", "")
                            }
                        } else if (textContent.contains("切換")) {
                            binding.tvMessage.apply {
                                setTextColor(-0x1)
                                setBackgroundResource(R.drawable.sys_msg_switch_bg)
                                text = textContent.replace("-", "")
                            }
                        } else if (textContent.contains("擁有")) {
                            binding.tvMessage.apply {
                                setTextColor(ContextCompat.getColor(context, R.color.item_name))
                                setBackgroundResource(R.drawable.sys_msg_has_owner_bg)
                                text = textContent
                            }
                        } else {
                            binding.tvMessage.apply {
                                setTextColor(-0x1)
                                setBackgroundResource(R.drawable.sys_msg_current_state_bg)
                                text = textContent
                            }
                        }
                    }
                }

                MessageFlag.RETRACT == message.flag -> {
                    withContext(Dispatchers.Main) {
                        binding.tvMessage.apply {
                            setBackgroundResource(R.drawable.sys_msg_bg)
                            setTextColor(-0xa1a1a2)
                        }
                    }

                    withContext(Dispatchers.Main) {
                        binding.tvMessage.setBackgroundResource(R.drawable.sys_msg_bg)
                    }
                    if (selfUserId == message.senderId) {
                        withContext(Dispatchers.Main) {
                            binding.tvMessage.text =
                                KeyWordHelper.matcherKeys(
                                    -0xb56f1e,
                                    binding.tvMessage.context.getString(R.string.text_you_retract_message) + "  " +
                                        binding.tvMessage.context
                                            .getString(R.string.text_edit_again),
                                    binding.tvMessage.context.getString(R.string.text_edit_again)
                                ) {
                                    onMessageClickListener?.onTipMessageClick(message)
                                }
                            binding.tvMessage.movementMethod = LinkMovementMethod.getInstance()
                        }
                    } else {
                        val userProfile =
                            UserProfileReference.findById(null, message.senderId)
                        userProfile?.let {
                            withContext(Dispatchers.Main) {
                                binding.tvMessage.text = "${it.nickName}收回訊息"
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setModeView(message: MessageEntity) =
        CoroutineScope(Dispatchers.Main).launch {
            when (mode) {
                MessageAdapterMode.RANGE_SELECTION -> {
                    binding.maskLayer.visibility = View.VISIBLE
                    binding.root.viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener)
                    binding.maskLayer.alpha = if (message.isShowSelection == true) 0.0f else 0.68f
                    binding.maskLayer.setOnClickListener {
                        onMessageClickListener?.buildScreenShot(message)
                    }
                }

                else -> {
                    binding.root.viewTreeObserver.removeOnGlobalLayoutListener(onGlobalLayoutListener)
                    binding.maskLayer.visibility = View.GONE
                }
            }
        }

    fun setMode(mode: MessageAdapterMode) {
        this.mode = mode
    }
}
