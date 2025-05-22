package tw.com.chainsea.ce.sdk.bean.msg.content;

import org.json.JSONObject;

import tw.com.chainsea.ce.sdk.bean.msg.MessageType;

/**
 * current by evan on 2020-02-07
 */
public class UndefContent implements IMessageContent<MessageType> {
    private static final long serialVersionUID = -8677680281216472784L;

    String text = MessageType.UNDEF.name();

    public UndefContent() {}

    public UndefContent(String text) {
        this.text = text;
    }

    @Override
    public MessageType getType() {
        return MessageType.TEXT;
    }

    @Override
    public String toStringContent() {
        return this.text;
    }

    @Override
    public String simpleContent() {
        return "[暫不支持此消息類型]";
    }

    @Override
    public String getFilePath() {
        return null;
    }

    @Override
    public JSONObject getSendObj()  {
        return null;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}