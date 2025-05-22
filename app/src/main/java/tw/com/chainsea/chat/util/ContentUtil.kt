package tw.com.chainsea.chat.util

import android.text.SpannableStringBuilder
import org.json.JSONArray
import tw.com.chainsea.android.common.json.JsonHelper
import tw.com.chainsea.ce.sdk.bean.msg.MessageFlag
import tw.com.chainsea.ce.sdk.bean.msg.MessageType
import tw.com.chainsea.ce.sdk.bean.msg.SourceType
import tw.com.chainsea.ce.sdk.bean.msg.content.IMessageContent
import tw.com.chainsea.ce.sdk.bean.msg.content.UndefContent
import tw.com.chainsea.ce.sdk.database.DBManager
import tw.com.chainsea.ce.sdk.http.ce.model.LastMessageType
import tw.com.chainsea.ce.sdk.service.ChatRoomService
import tw.com.chainsea.chat.base.Constant

object ContentUtil {
    fun getFormatContent(type: MessageType?, content: String): CharSequence {
        return if (type == null) "" else when (type) {
            MessageType.AT -> ChatRoomService.getAtContent(content)
            MessageType.VIDEO -> "[影片]"
            MessageType.LOCATION -> "[位置]"
            MessageType.BUSINESS -> "[物件]"
            MessageType.FILE -> "[文件]"
            MessageType.VOICE -> "[語音]"
            MessageType.IMAGE -> "[圖片]"
            MessageType.AD -> "[廣告]"
            MessageType.STICKER -> "[表情]"
            MessageType.TEXT -> getContent(content)
            MessageType.TEMPLATE -> "[卡片訊息]"
            else -> "[未知訊息格式]"
        }
    }

    fun getContent(input: String): String {
        return runCatching {
            val jsonObject = JsonHelper.getInstance().toJsonObject(input)
            jsonObject.getString("text")
        }.getOrElse {
            runCatching {
                val jsonArray = JSONArray(input)
                val firstItem = jsonArray.getJSONObject(0)
                val contentObject = firstItem.getJSONObject("content")
                contentObject.getString("text")
            }.getOrElse { input }
        }
    }

    fun getItemContent(userId: String, roomId: String): CharSequence {
        val sourceType = DBManager.getInstance().querySourceTypeFromLastMessage(roomId)
        val senderId = DBManager.getInstance().querySenderIdFromLastMessage(roomId)
        val senderName = DBManager.getInstance().querySenderNameFromLastMessage(roomId)
        val flag = DBManager.getInstance().queryFlagFromLastMessage(roomId)
        val type = DBManager.getInstance().queryTypeFromLastMessage(roomId)
        val content = DBManager.getInstance().queryContentFromLastMessage(roomId)
        val builder = SpannableStringBuilder()
        if(!sourceType.isNullOrEmpty()) {
            if (userId == senderId) { //如果是自己就改用我
                builder.append("我 : ")
            } else if (LastMessageType.SourceType.SYSTEM == SourceType.of(sourceType).name || LastMessageType.SourceType.LOGIN == SourceType.of(sourceType).name || LastMessageType.SourceType.SATISFACTION == SourceType.of(sourceType).name) {
                builder.append("")
            } else {
                builder.append(senderName).append(" : ")
            }
        }

        if (MessageFlag.RETRACT == MessageFlag.of(flag)) { //回收訊息
            builder.append(Constant.RETRACT_MSG)
        } else {
            builder.append(getFormatContent(MessageType.of(type), content))
        }

        return builder
    }

    fun content(type: MessageType?, content: String): IMessageContent<MessageType> {
        return if (type == null) {
            UndefContent()
        } else type.from(content)
    }
}