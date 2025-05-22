package tw.com.chainsea.ce.sdk.bean.msg

import com.google.common.collect.Lists
import com.google.gson.annotations.SerializedName
import org.json.JSONObject
import tw.com.chainsea.android.common.json.JsonHelper
import tw.com.chainsea.ce.sdk.bean.msg.content.Action
import tw.com.chainsea.ce.sdk.bean.msg.content.IMessageContent

data class TemplateContent(
    val orientation: String? = "",
    val text: String? = "",
    val title: String? = "",
    val imageUrl: String? = "",
    @SerializedName("type")
    val templateType: String? = "",
    val actions: List<Action>? = Lists.newArrayList(),
    val elements: List<TemplateElement>? = Lists.newArrayList(),
    val defaultAction: Action?
) : IMessageContent<MessageType> {
    override fun getType(): MessageType {
        return MessageType.TEMPLATE
    }

    override fun toStringContent(): String {
        return JsonHelper.getInstance().toJson(this)
    }

    override fun simpleContent(): String {
        return "[卡片訊息]"
    }

    override fun getFilePath(): String {
        return ""
    }

    override fun getSendObj(): JSONObject {
        return JSONObject()
    }
}


data class TemplateElement(
    val imageUrl: String? = "",
    val title: String? = "",
    val actions: List<TemplateElementAction>? = Lists.newArrayList()
)

data class TemplateElementAction(
    val displayText: String? = "",
    val data: String? = "",
    val type: String? = "",
    val title: String? = "",
    val url: String? = ""
)