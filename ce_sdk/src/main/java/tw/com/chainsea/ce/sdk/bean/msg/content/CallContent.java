package tw.com.chainsea.ce.sdk.bean.msg.content;

import org.json.JSONObject;

import tw.com.chainsea.ce.sdk.bean.msg.MessageType;

/**
 * current by evan on 2020-01-08
 */

public class CallContent implements IMessageContent<MessageType> {
    private static final long serialVersionUID = -7603914144132937573L;

    String content;

    public CallContent(String content) {
        this.content = content;
    }

    @Override
    public MessageType getType() {
        return MessageType.CALL;
    }

    @Override
    public String toStringContent() {
        return this.content;
    }

    @Override
    public String simpleContent() {
        return "[通話]";
    }

    @Override
    public String getFilePath() {
        return null;
    }

    @Override
    public JSONObject getSendObj() {
        return null;
    }

    public String getContent() {
        return content;
    }
}
