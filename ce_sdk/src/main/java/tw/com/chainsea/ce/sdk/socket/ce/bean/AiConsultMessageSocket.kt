package tw.com.chainsea.ce.sdk.socket.ce.bean

import com.google.common.collect.Lists
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import tw.com.chainsea.android.common.json.JsonHelper
import tw.com.chainsea.android.common.log.CELog
import tw.com.chainsea.ce.sdk.bean.room.QuickReplyItem

data class AiConsultMessageSocket(
    val serviceNumberId: String = "",
    val consultId: String = "",
    val linkId: String = "",
    val messagelist: List<AiConsultMessageList> = Lists.newArrayList(),
    val userId: String = "",
    val roomId: String = "",
    var quickReplyItem: List<QuickReplyItem>? = Lists.newArrayList()
) {

    // 主要是要將 quickReply 移除掉
    fun parse(data: Any): AiConsultMessageSocket {
        try {
            val jsonObject = JSONObject(data.toString())
            val messageList = jsonObject.optJSONArray("messagelist")
            val quickReplyItem = Lists.newArrayList<QuickReplyItem>()
            messageList?.let {
                for (i in 0 until it.length()) {
                    val message = it.optJSONObject(i)
                    val content = JSONObject(message.optString("content"))
                    // 判斷內容是否有 QuickReply 有的話移除並加入到另一個 list
                    if (content.has("quickReply")) {
                        val listType = object : TypeToken<List<QuickReplyItem>>() {}.type
                        val quickReplyItems =
                            content.optJSONObject("quickReply")?.optString("items")
                        quickReplyItem.addAll(
                            JsonHelper.getInstance().from(quickReplyItems, listType)
                        )
                        content.remove("quickReply")
                    }
                    // 再將 content put 回去
                    jsonObject.optJSONArray("messagelist")?.optJSONObject(i)
                        ?.put("content", JsonHelper.getInstance().toJson(content))
                }
            }

            val aiConsultMessageSocket = JsonHelper.getInstance()
                .from(jsonObject.toString(), AiConsultMessageSocket::class.java)
            if (quickReplyItem.size > 0) {
                aiConsultMessageSocket.quickReplyItem = quickReplyItem
            }

            return aiConsultMessageSocket
        } catch (e: Exception) {
            CELog.e("AiConsultMessageSocket parse error: $data", e)
        }

        return this
    }
}

data class AiConsultMessageList(
    val type: String = "",
    val content: String
)