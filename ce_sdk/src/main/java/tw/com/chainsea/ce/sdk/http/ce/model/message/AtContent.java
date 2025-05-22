package tw.com.chainsea.ce.sdk.http.ce.model.message;

import java.util.List;

public class AtContent {
    private Content content;
    private String objectType;
    private String type;
    private List<String> userIds;

    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

     public static class Content{
        String text;

        public String getText() {
            return text;
        }
    }
}
