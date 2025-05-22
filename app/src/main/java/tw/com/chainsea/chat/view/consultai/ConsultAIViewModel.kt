package tw.com.chainsea.chat.view.consultai

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.common.collect.Lists
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.android.common.json.JsonHelper
import tw.com.chainsea.ce.sdk.bean.msg.MessageStatus
import tw.com.chainsea.ce.sdk.bean.msg.MessageType
import tw.com.chainsea.ce.sdk.bean.msg.MsgKitAssembler
import tw.com.chainsea.ce.sdk.bean.msg.SourceType
import tw.com.chainsea.ce.sdk.bean.msg.Tools
import tw.com.chainsea.ce.sdk.bean.msg.content.IMessageContent
import tw.com.chainsea.ce.sdk.bean.msg.content.TextContent
import tw.com.chainsea.ce.sdk.bean.msg.content.UndefContent
import tw.com.chainsea.ce.sdk.bean.room.QuickReplyItem
import tw.com.chainsea.ce.sdk.bean.todo.TodoEntity
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.http.ce.base.BaseViewModel
import tw.com.chainsea.ce.sdk.http.ce.base.TokenRepository
import tw.com.chainsea.ce.sdk.network.model.common.ApiResult
import tw.com.chainsea.ce.sdk.reference.UserProfileReference
import tw.com.chainsea.ce.sdk.socket.ce.bean.AiConsultMessageList
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.config.BundleKey
import tw.com.chainsea.chat.messagekit.enums.RichMenuBottom
import tw.com.chainsea.chat.util.TimeUtil
import java.text.SimpleDateFormat
import java.util.Locale

class ConsultAIViewModel(
    private val application: Application,
    private val consultAIRepository: ConsultAIRepository,
    private val tokenRepository: TokenRepository
) : BaseViewModel(application, tokenRepository) {
    val consultAiSendMessage = MutableLiveData<Pair<Boolean, MessageEntity>>()

    val quoteMessageResult = MutableLiveData<Intent>()

//    val quoteMessageError = MutableLiveData<String>()

    val todoMessageResult = MutableLiveData<TodoEntity>()

    private val dateTagMessage = mutableListOf<MessageEntity>()
    val messageList = MutableLiveData<MutableList<MessageEntity>>()

    val quickReplyList = MutableLiveData<List<QuickReplyItem>>()

    val bottomRichMenuList = MutableLiveData<Pair<MessageEntity, List<RichMenuBottom>>>()

    private val selfUserProfile by lazy {
        UserProfileReference.findById(null, TokenPref.getInstance(application).userId)
    }

    private val messageTimeLineFormat = SimpleDateFormat("MMMdd日(EEE)", Locale.TAIWAN)

    /**
     * 發送訊息給 AI
     * @param serviceNumberRoomId 當前的服務號聊天室ID
     * @param serviceNumberId 當前的服務號ID
     * @param messageEntity 需要發送的訊息
     * */
    fun sendConsultAIMessage(
        serviceNumberRoomId: String,
        serviceNumberId: String,
        content: String
    ) = viewModelScope.launch(
        Dispatchers.IO
    ) {
        val messageEntity =
            MsgKitAssembler.assembleSendTextMessage(
                serviceNumberRoomId,
                Tools.generateMessageId(),
                selfUserProfile.id,
                selfUserProfile.avatarId,
                content,
                selfUserProfile.nickName
            )
        val messageList = addDateLabel(mutableListOf(messageEntity), serviceNumberRoomId)
        this@ConsultAIViewModel.messageList.postValue(messageList)
        checkTokenValid(
            consultAIRepository.sendConsultAIMessage(
                serviceNumberRoomId,
                serviceNumberId,
                content,
                "Text"
            )
        )?.collect {
            when (it) {
                is ApiResult.Success -> {
                    consultAiSendMessage.postValue(Pair(true, messageEntity))
                }
                is ApiResult.Failure -> {
                    consultAiSendMessage.postValue(Pair(false, messageEntity))
                }

                else -> {
                    // nothing
                }
            }
        }
    }

    /**
     * 複製訊息
     * @param messageEntity 當前選到的訊息
     * */
    fun copyMessage(messageEntity: MessageEntity) =
        viewModelScope.launch {
            val content: IMessageContent<MessageType> = messageEntity.content()
            if (content is TextContent) {
                val clipBoard = application.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
                val clip = ClipData.newPlainText("Aile", content.text)
                clipBoard?.setPrimaryClip(clip)
            }
        }

    /**
     * 引用訊息
     * @param messageEntity 當前選到的訊息
     * */
    fun quoteMessage(messageEntity: MessageEntity) =
        viewModelScope.launch {
            val intent = Intent()
            var bundle = Bundle()
            when (val content: IMessageContent<MessageType> = messageEntity.content()) {
                is TextContent -> {
                    val typeJson = JsonHelper.getInstance().toJson(messageEntity.type)
                    bundle =
                        bundleOf(
                            BundleKey.CONSULT_AI_QUOTE_TYPE.key() to typeJson,
                            BundleKey.CONSULT_AI_QUOTE_STRING.key() to content.text
                        )
                }
                else -> {
                    // nothing
                }
            }
            intent.putExtras(bundle)
            quoteMessageResult.postValue(intent)
        }

    /**
     * 取得 AI 歷史訊息
     * @param consultId 諮詢 ID
     * @param serviceNumberRoomId 原本的服務號聊天室ID
     * */
    fun getAIHistoryMessageList(
        consultId: String,
        serviceNumberRoomId: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        checkTokenValid(consultAIRepository.getAIHistoryMessageList(consultId))?.collect {
            when (it) {
                is ApiResult.Success -> {
                    var messageList = mutableListOf<MessageEntity>()
                    it.data.items?.forEach {
                        val messageEntity =
                            MessageEntity
                                .Builder()
                                .senderId(it.senderId)
                                .sendTime(it.sendTime)
                                .senderName(it.senderName)
                                .roomId(serviceNumberRoomId)
                                .type(MessageType.of(it.type))
                                .id(it.id)
                                .content(it.content)
                                .build()
                        messageList.add(messageEntity)
                    }
                    messageList.sort()
                    messageList = addDateLabel(messageList, serviceNumberRoomId)
                    this@ConsultAIViewModel.messageList.postValue(messageList)
                    // 判斷最新一則訊息是否有 Quick Reply
                    if (messageList.isNotEmpty()) {
                        val latestMessage = messageList.last()
                        if (latestMessage.content.contains("quickReply")) {
                            val listType = object : TypeToken<List<QuickReplyItem?>?>() {}.type
                            val items =
                                JSONObject(latestMessage.content)
                                    .optJSONObject("quickReply")
                                    ?.optJSONArray("items")
                            items?.let {
                                val quickReplyItemList =
                                    JsonHelper
                                        .getInstance()
                                        .from<List<QuickReplyItem>>(it.toString(), listType)
                                quickReplyList.postValue(quickReplyItemList)
                            }
                        }
                    }
                }
                else -> {
                    // nothing
                }
            }
        }
    }

    /**
     * 建立AI回覆訊息
     * @param serviceNumberRoomId 原本的服務號聊天室ID
     * @param aiMessageList socket 回來的 messageList
     * */
    fun buildConsultAiReply(
        serviceNumberRoomId: String,
        aiMessageList: List<AiConsultMessageList>
    ) = viewModelScope.launch(Dispatchers.IO) {
        val sender = application.getString(R.string.consult_ai_text)
        var messageList = mutableListOf<MessageEntity>()
        aiMessageList.forEach {
            val messageBuilder =
                MessageEntity
                    .Builder()
                    .id(Tools.generateMessageId())
                    .senderId(sender)
                    .roomId(serviceNumberRoomId)
                    .senderName(sender)
                    .status(MessageStatus.SUCCESS)
                    .content(it.content)
                    .type(MessageType.of(it.type))
                    .sendTime(System.currentTimeMillis())
            messageList.add(messageBuilder.build())
        }
        messageList = addDateLabel(messageList, serviceNumberRoomId)
        this@ConsultAIViewModel.messageList.postValue(messageList)
    }

    /**
     * 發送 QuickReply
     * @param serviceNumberRoomId 當前的服務號聊天室ID
     * @param content QuickReply 的內容
     * */
    fun sendAIQuickReply(
        serviceNumberRoomId: String,
        serviceNumberId: String,
        content: String,
        type: String = "Action"
    ) = viewModelScope.launch(Dispatchers.IO) {
        checkTokenValid(consultAIRepository.sendConsultAIMessage(serviceNumberRoomId, serviceNumberId, content, type))?.collect {
        }
    }

    fun getBottomRichMenu(messageEntity: MessageEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            val longClickMenuList = Lists.newLinkedList<RichMenuBottom>()
            when (messageEntity.type) {
                MessageType.TEXT -> {
                    longClickMenuList.add(RichMenuBottom.QUOTE)
                    longClickMenuList.add(RichMenuBottom.COPY)
                }
                MessageType.IMAGE -> {
                    longClickMenuList.add(RichMenuBottom.QUOTE)
                }
                MessageType.VIDEO -> {
                    longClickMenuList.add(RichMenuBottom.QUOTE)
                }
                else -> {
                    // nothing
                }
            }
            bottomRichMenuList.postValue(Pair(messageEntity, longClickMenuList))
        }

    /**
     * 在訊息中插入時間標籤訊息
     * @param messageEntityList 要顯示的訊息 List
     * @param serviceNumberRoomId 原本的服務號聊天室ID
     * */
    private suspend fun addDateLabel(
        messageEntityList: MutableList<MessageEntity>,
        serviceNumberRoomId: String?
    ): MutableList<MessageEntity> =
        withContext(Dispatchers.IO) {
            var currentDate = ""
            val messageList = mutableListOf<MessageEntity>()
            messageEntityList.forEach { messageEntity ->
                val sendDate = messageTimeLineFormat.format(messageEntity.sendTime)
                val dateLabelMessage = getDateLabelMessage(messageEntity.sendTime, serviceNumberRoomId)
                // 避免重複新增時間標籤
                val isMessageListAlreadyAddDateLabel = dateTagMessage.contains(dateLabelMessage)
                if (sendDate != currentDate && !messageList.contains(dateLabelMessage) && !isMessageListAlreadyAddDateLabel) {
                    currentDate = sendDate
                    dateLabelMessage?.let {
                        dateTagMessage.add(it)
                        messageList.add(it)
                    }
                }
                messageList.add(messageEntity)
            }
            return@withContext messageList
        }

    /**
     * 取得時間標籤 MessageEntity
     * @param sendTime 訊息發送的時間
     * @param serviceNumberRoomId 原本的服務號聊天室ID
     * */
    private suspend fun getDateLabelMessage(
        sendTime: Long,
        serviceNumberRoomId: String?
    ): MessageEntity? =
        withContext(Dispatchers.IO) {
            serviceNumberRoomId?.let {
                val dayBegin = TimeUtil.getDayBegin(sendTime)
                return@withContext MessageEntity
                    .Builder()
                    .id(Tools.generateTimeMessageId(dayBegin))
                    .sendTime(dayBegin)
                    .roomId(it)
                    .status(MessageStatus.SUCCESS)
                    .sourceType(SourceType.SYSTEM)
                    .content(UndefContent("TIME_LINE").toStringContent())
                    .sendTime(dayBegin)
                    .build()
            } ?: return@withContext null
        }
}
