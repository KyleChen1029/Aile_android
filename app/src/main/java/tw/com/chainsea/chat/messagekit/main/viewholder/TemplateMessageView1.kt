package tw.com.chainsea.chat.messagekit.main.viewholder

import android.content.Context
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.android.common.json.JsonHelper
import tw.com.chainsea.ce.sdk.bean.msg.TemplateContent
import tw.com.chainsea.ce.sdk.bean.msg.TemplateElementAction
import tw.com.chainsea.ce.sdk.bean.msg.content.Action
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity
import tw.com.chainsea.ce.sdk.database.sp.UserPref
import tw.com.chainsea.chat.aiff.AiffManager
import tw.com.chainsea.chat.databinding.ItemBaseMessageBinding
import tw.com.chainsea.chat.messagekit.main.adapter.TemplateMultiAdapter
import tw.com.chainsea.chat.messagekit.main.viewholder.Constant.ActionType
import tw.com.chainsea.chat.messagekit.main.viewholder.base.BaseMessageViewHolder
import tw.com.chainsea.chat.util.IntentUtil
import tw.com.chainsea.chat.util.TimeUtil.getHHmm
import java.net.URI

class TemplateMessageView1(
    binding: ItemBaseMessageBinding,
    chatRoomEntity: ChatRoomEntity
) : BaseMessageViewHolder(binding, chatRoomEntity) {
    private var onTemplateClickListener: OnTemplateClickListener? = null

    fun setOnTemplateClickListener(onTemplateClickListener: OnTemplateClickListener) {
        this.onTemplateClickListener = onTemplateClickListener
    }

    override fun onMessageClick() {
    }

    override fun bind(message: MessageEntity) {
        super.bind(message)
        setBubbleView()
        CoroutineScope(Dispatchers.IO).launch {
            if (message.content.isEmpty()) return@launch
            val templateContent = message.content() as TemplateContent
            binding.swipeLayout.isSwipeEnable = false
            val sendTime = getHHmm(message.sendTime)
            val layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
            val templateAdapter =
                TemplateMultiAdapter(templateContent, { action ->
                    when (action) {
                        is TemplateElementAction -> {
                            ActionClick(
                                message = message,
                                templateElementAction = action,
                                context = binding.root.context
                            ).onTemplateClick()
                        }

                        is Action -> {
                            ActionClick(
                                message = message,
                                action = action,
                                context = binding.root.context
                            ).onTemplateClick()
                        }
                    }
                }, { templateElement ->
                })
            withContext(Dispatchers.Main) {
                if (isRightMessage()) {
                    rightMessageBinding.apply {
                        tvRightSendTime.visibility = View.GONE
                        clRightLayout.visibility = View.VISIBLE
                        clRightMultiTemplateContainer.visibility = View.VISIBLE
                        tvRightTemplateSendTime.text = sendTime
                        rvRightMultiTemplateList.apply {
                            this.layoutManager = layoutManager
                            this.adapter = templateAdapter
                        }
                    }
                } else {
                    leftMessageBinding.apply {
                        tvLeftSendTime.visibility = View.GONE
                        rightMessageBinding.tvRightAccountName.visibility = View.GONE
                        tvLeftAccountName.visibility = View.VISIBLE
                        clLeftLayout.visibility = View.VISIBLE
                        clLeftMultiTemplateContainer.visibility = View.VISIBLE
                        tvLeftTemplateSendTime.text = sendTime
                        rvLeftMultiTemplateList.apply {
                            this.visibility = View.VISIBLE
                            this.layoutManager = layoutManager
                            this.adapter = templateAdapter
                        }
                        val params = flLeftMessageContainer.layoutParams as ConstraintLayout.LayoutParams
                        params.bottomToTop = clLeftMultiTemplateContainer.id
                    }
                }
            }
        }
    }

    inner class ActionClick(
        private val message: MessageEntity,
        private val action: Action? = null,
        private val templateElementAction: TemplateElementAction? = null,
        private val context: Context? = null
    ) {
        fun onTemplateClick() {
            val type = if (action == null) templateElementAction?.type else action.type
            when (type) {
                ActionType.AIFF -> {
                    action?.let {
                        if (!it.url.contains("aiff.aile.com")) {
                            val roomId = message.roomId
                            val aiffManager = AiffManager(context, roomId)
                            aiffManager.addAiffWebView(action.url)
                            return
                        }

                        try {
                            val uri = URI(it.url)
                            val segments = uri.path.split("/")
                            var id = segments[segments.size - 1]
                            if (id.contains("?")) {
                                val str1 = id.substring(0, id.indexOf("?"))
                                id = id.substring(str1.length + 1)
                            }

                            val roomId = UserPref.getInstance(context).currentRoomId
                            val aiffManager = AiffManager(context, roomId)
                            aiffManager.showAiffById(id)
                        } catch (e: Exception) {
                        }
                    }
                }

                ActionType.LINK -> {
                }

                ActionType.POSTBACK -> {
                    onTemplateClickListener?.onTemplateClick(JsonHelper.getInstance().toJson(templateElementAction))
                }

                else -> {
                    val url = if (action == null) templateElementAction?.url else action.url
                    context?.let {
                        IntentUtil.launchUrl(it, url!!)
                    }
                }
            }
        }
    }

    interface OnTemplateClickListener {
        fun onTemplateClick(json: String)
    }
}
