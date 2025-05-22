package tw.com.chainsea.ce.sdk.bean.msg.content;

import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

import java.io.Serializable;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.bean.msg.MessageType;

/**
 * current by evan on 2020-08-19
 *
 * @author Evan Wang
 * date 2020-08-19
 */
public class BroadcastContent implements IMessageContent<MessageType>, Serializable {
    private static final long serialVersionUID = 978718029931879466L;

    @SerializedName(value = "type")
    MessageType insideType;
    @SerializedName(value = "content")
    String content;

    public BroadcastContent(MessageType insideType, String content) {
        this.insideType = insideType;
        this.content = content;
    }

    public IMessageContent content() {
        if (insideType == null) {
            return new UndefContent();
        }

        return insideType.from(content);
    }

    @Override
    public MessageType getType() {
        return MessageType.BROADCAST;
    }

    @Override
    public String toStringContent() {
        return JsonHelper.getInstance().toJson(this);
    }

    @Override
    public String simpleContent() {
        return content().simpleContent();
    }

    @Override
    public String getFilePath() {
        return null;
    }

    @Override
    public JSONObject getSendObj(){
        return null;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public MessageType getInsideType() {
        return insideType;
    }

    public void setInsideType(MessageType insideType) {
        this.insideType = insideType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
