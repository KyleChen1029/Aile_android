package tw.com.chainsea.chat.service.fcm.model;

public class FcmData {
    private String body;
    private String title;
    private String unReadCount;
    private Content content;
    private String code;
    private String messageId;
    private String type;
    private String room;
    private String senderName;
    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUnReadCount() {
        return unReadCount;
    }

    public void setUnReadCount(String unReadCount) {
        this.unReadCount = unReadCount;
    }

    public Content getContent() {return content;}

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getSenderName() {
        return senderName;
    }
}
