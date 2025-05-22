package tw.com.chainsea.ce.sdk.bean.msg.content;

import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import tw.com.chainsea.ce.sdk.bean.msg.TargetType;
import tw.com.chainsea.ce.sdk.bean.msg.MessageType;

/**
 * current by evan on 2020-01-08
 */
public class MentionContent {
    List<String> userIds = Lists.newArrayList();
    @SerializedName("type")
    MessageType type = MessageType.UNDEF;
    @SerializedName("objectType")
    TargetType objectType = TargetType.UNDEF;
    @SerializedName("content")
    Object content;

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public TargetType getObjectType() {
        return objectType;
    }

    public void setObjectType(TargetType objectType) {
        this.objectType = objectType;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }
}