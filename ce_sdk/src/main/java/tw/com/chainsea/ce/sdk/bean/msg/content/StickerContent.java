package tw.com.chainsea.ce.sdk.bean.msg.content;

import org.json.JSONObject;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.bean.msg.MessageType;

/**
 * current by evan on 2020-01-08
 */
public class StickerContent implements IMessageContent<MessageType> {
    private static final long serialVersionUID = -6675364554483794366L;

    String id;
    String url;
    String packageId;

    public StickerContent(String id, String packageId) {
        this.id = id;
        this.packageId = packageId;
    }

    @Override
    public MessageType getType() {
        return MessageType.STICKER;
    }

    @Override
    public String toStringContent() {
        return JsonHelper.getInstance().toJson(this);
    }

    @Override
    public String simpleContent() {
        return "[表情]";
    }

    @Override
    public String getFilePath() {
        return null;
    }

    @Override
    public JSONObject getSendObj()  {
        return null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }
}
