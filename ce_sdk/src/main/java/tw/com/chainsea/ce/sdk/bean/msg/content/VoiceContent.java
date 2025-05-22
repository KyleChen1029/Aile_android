package tw.com.chainsea.ce.sdk.bean.msg.content;

import org.json.JSONObject;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.bean.msg.MessageType;

/**
 * current by evan on 2020-01-08
 * 語音
 */
public class VoiceContent implements IMessageContent<MessageType> {
    private static final long serialVersionUID = 285957821403269619L;

    double duration;
    String url;
    // ui control
    private boolean isRead;
    private boolean isDownLoad;

    public VoiceContent(double duration, String url) {
        this.duration = duration;
        this.url = url;
    }

    public VoiceContent(double duration, String url, boolean isRead) {
        this.duration = duration;
        this.url = url;
        this.isRead = isRead;
    }

    @Override
    public MessageType getType() {
        return MessageType.VOICE;
    }

    @Override
    public String toStringContent() {
        return JsonHelper.getInstance().toJson(this);
    }

    @Override
    public String simpleContent() {
        return "[語音]";
    }


    @Override
    public String getFilePath() {
        return url;
    }

    @Override
    public JSONObject getSendObj()  {
        return null;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public boolean isDownLoad() {
        return isDownLoad;
    }

    public void setDownLoad(boolean downLoad) {
        isDownLoad = downLoad;
    }
}
