package tw.com.chainsea.ce.sdk.bean.msg.content;

import org.json.JSONObject;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.bean.msg.MessageType;

/**
 * current by evan on 2020-02-10
 */

public class BusinessTextContent implements IMessageContent<MessageType> {
    private static final long serialVersionUID = -142040234009723074L;

    private String content;
    private String title;
    private String businessCode;
    private String businessId;
    private String pictureUrl;

    public BusinessTextContent(String content, String title, String businessCode, String businessId, String pictureUrl) {
        this.content = content;
        this.title = title;
        this.businessCode = businessCode;
        this.businessId = businessId;
        this.pictureUrl = pictureUrl;
    }

    @Override
    public MessageType getType() {
        return MessageType.BUSINESS_TEXT;
    }

    @Override
    public String toStringContent() {
        return JsonHelper.getInstance().toJson(this);
    }

    @Override
    public String simpleContent() {
        return "[商業]";
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

    public String getTitle() {
        return title;
    }

    public String getBusinessCode() {
        return businessCode;
    }

    public String getBusinessId() {
        return businessId;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }
}
