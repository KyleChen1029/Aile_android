package tw.com.chainsea.ce.sdk.bean.room

import com.google.common.collect.Lists
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import tw.com.chainsea.android.common.json.JsonHelper
import tw.com.chainsea.android.common.log.CELog

data class QuickReplySocket(
    var items: List<QuickReplyItem>,
    var id: String = "",
    var text: String = "",
    var roomId: String = ""
) {
    constructor(): this(Lists.newArrayList(), "", "")

    fun parse(jsonData: String): QuickReplySocket? {
        try {
            val listType = object : TypeToken<List<QuickReplyItem?>?>() {}.type
            val jsonObject = JSONObject(jsonData)
            val quickReply: JSONObject? = jsonObject.optJSONObject("quickReply")
            quickReply?.let {
                val items = JsonHelper.getInstance().from<List<QuickReplyItem>>(it.optJSONArray("items").toString(), listType)
                this.items = items
                this.id = jsonObject.optString("id")
                this.text = jsonObject.optString("text")
                return this
            }

        } catch (e: Exception) {
            CELog.e("QuickReply Parse Error: " + e.message)
        }
        return null
    }
}

data class QuickReplyItem(
    val action: QuickReplyActionItem,
    val type: String
)

data class QuickReplyActionItem(
    val displayText: String,
    val data: String,
    val type: String,
    val title: String
)

data class QuickReplyActionItemData(
    val keyId: String,
    val name: String,
    val type: String
)