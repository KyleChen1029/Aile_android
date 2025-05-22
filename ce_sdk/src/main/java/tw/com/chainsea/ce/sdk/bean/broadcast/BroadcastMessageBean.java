package tw.com.chainsea.ce.sdk.bean.broadcast;

import androidx.annotation.NonNull;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import tw.com.chainsea.ce.sdk.bean.msg.MessageType;
import tw.com.chainsea.ce.sdk.bean.msg.content.IMessageContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.TextContent;

/**
 * current by evan on 2020-08-18
 *
 * @author Evan Wang
 * date 2020-08-18
 */
public class BroadcastMessageBean implements Serializable {
    private static final long serialVersionUID = 4045970856077270861L;

    String roomId;
    String messageId;
    String serviceNumberId;
    long broadcastTime;
    String type;
    String content;
    List<TopicEntity> topicArray = Lists.newArrayList();
    IMessageContent<MessageType> iContent;

    public BroadcastMessageBean(String roomId, String messageId, String serviceNumberId, long broadcastTime, String type, String content, List<TopicEntity> topicArray, IMessageContent<MessageType> iContent) {
        this.roomId = roomId;
        this.messageId = messageId;
        this.serviceNumberId = serviceNumberId;
        this.broadcastTime = broadcastTime;
        this.type = type;
        this.content = content;
        this.topicArray = topicArray;
        this.iContent = iContent;
    }

    public BroadcastMessageBean() {
    }

    private static List<TopicEntity> $default$topicArray() {
        return Lists.newArrayList();
    }

    public static BroadcastMessageBeanBuilder Build() {
        return new BroadcastMessageBeanBuilder();
    }

    public Set<String> topicIds() {
        Set<String> ids = Sets.newHashSet();
        for (TopicEntity entity : topicArray) {
            if (!entity.isHardCode()) {
                ids.add(entity.id);
            }
        }
        return ids;
    }

    public List<TopicEntity> getValidTopicItems() {
        List<TopicEntity> list = Lists.newArrayList();
        for (TopicEntity entity : topicArray) {
            if (!entity.isHardCode()) {
                list.add(entity);
            }
        }
        return list;
    }


    public JSONObject buildSendObj() {
        try {
            JSONObject object = new JSONObject()
                .put("roomId", this.roomId)
                .put("messageId", this.messageId)
                .put("serviceNumberId", this.serviceNumberId)
                .put("type", this.type)
                .put("content", new JSONObject()
                    .put("type", iContent.getType().getValue())
                    .put("content", iContent instanceof TextContent ? iContent.simpleContent() : iContent.toStringContent())
                    .toString())
                .put("topicIds", new JSONArray(topicIds()));
            if (broadcastTime > 0) {
                object.put("broadcastTime", this.broadcastTime);
            }
            return object;
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    public JSONObject buildSendObj(IMessageContent<MessageType> iContent) {
        try {
            JSONObject object = new JSONObject()
                .put("roomId", this.roomId)
                .put("messageId", this.messageId)
                .put("serviceNumberId", this.serviceNumberId)
                .put("type", this.type)
                .put("content", new JSONObject()
                    .put("type", iContent.getType().getValue())
                    .put("content", iContent.toStringContent())
                    .toString())
                .put("topicIds", new JSONArray(topicIds()));


            if (broadcastTime > 0) {
                object.put("broadcastTime", this.broadcastTime);
//                object.put("broadcastTime", String.valueOf(this.broadcastTime));
            }
            return object;
        } catch (Exception e) {
            return new JSONObject();
        }
    }


    public JSONObject buildUpdateObj() {
        try {
            JSONObject object = new JSONObject()
                .put("roomId", this.roomId)
                .put("messageId", this.messageId)
                .put("serviceNumberId", this.serviceNumberId)
                .put("type", this.type)
                .put("content", new JSONObject()
                    .put("type", iContent.getType().getValue())
                    .put("content", iContent instanceof TextContent ? iContent.simpleContent() : iContent.toStringContent())
                    .toString())
                .put("topicIds", new JSONArray(topicIds()));
            if (broadcastTime > 0) {
                object.put("broadcastTime", this.broadcastTime);
            } else {
                object.put("broadcastTime", 0);
            }
            return object;
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    public String getRoomId() {
        return this.roomId;
    }

    public String getMessageId() {
        return this.messageId;
    }

    public String getServiceNumberId() {
        return this.serviceNumberId;
    }

    public long getBroadcastTime() {
        return this.broadcastTime;
    }

    public String getType() {
        return this.type;
    }

    public String getContent() {
        return this.content;
    }

    public List<TopicEntity> getTopicArray() {
        return this.topicArray;
    }

    public IMessageContent<MessageType> getIContent() {
        return this.iContent;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void setServiceNumberId(String serviceNumberId) {
        this.serviceNumberId = serviceNumberId;
    }

    public void setBroadcastTime(long broadcastTime) {
        this.broadcastTime = broadcastTime;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTopicArray(List<TopicEntity> topicArray) {
        this.topicArray = topicArray;
    }

    public void setIContent(IMessageContent<MessageType> iContent) {
        this.iContent = iContent;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof BroadcastMessageBean))
            return false;
        final BroadcastMessageBean other = (BroadcastMessageBean) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$roomId = this.getRoomId();
        final Object other$roomId = other.getRoomId();
        if (!Objects.equals(this$roomId, other$roomId))
            return false;
        final Object this$messageId = this.getMessageId();
        final Object other$messageId = other.getMessageId();
        if (!Objects.equals(this$messageId, other$messageId))
            return false;
        final Object this$serviceNumberId = this.getServiceNumberId();
        final Object other$serviceNumberId = other.getServiceNumberId();
        if (!Objects.equals(this$serviceNumberId, other$serviceNumberId))
            return false;
        if (this.getBroadcastTime() != other.getBroadcastTime()) return false;
        final Object this$type = this.getType();
        final Object other$type = other.getType();
        if (!Objects.equals(this$type, other$type)) return false;
        final Object this$content = this.getContent();
        final Object other$content = other.getContent();
        if (!Objects.equals(this$content, other$content))
            return false;
        final Object this$topicArray = this.getTopicArray();
        final Object other$topicArray = other.getTopicArray();
        if (!Objects.equals(this$topicArray, other$topicArray))
            return false;
        final Object this$iContent = this.getIContent();
        final Object other$iContent = other.getIContent();
        return Objects.equals(this$iContent, other$iContent);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof BroadcastMessageBean;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $roomId = this.getRoomId();
        result = result * PRIME + ($roomId == null ? 43 : $roomId.hashCode());
        final Object $messageId = this.getMessageId();
        result = result * PRIME + ($messageId == null ? 43 : $messageId.hashCode());
        final Object $serviceNumberId = this.getServiceNumberId();
        result = result * PRIME + ($serviceNumberId == null ? 43 : $serviceNumberId.hashCode());
        final long $broadcastTime = this.getBroadcastTime();
        result = result * PRIME + (int) ($broadcastTime >>> 32 ^ $broadcastTime);
        final Object $type = this.getType();
        result = result * PRIME + ($type == null ? 43 : $type.hashCode());
        final Object $content = this.getContent();
        result = result * PRIME + ($content == null ? 43 : $content.hashCode());
        final Object $topicArray = this.getTopicArray();
        result = result * PRIME + ($topicArray == null ? 43 : $topicArray.hashCode());
        final Object $iContent = this.getIContent();
        result = result * PRIME + ($iContent == null ? 43 : $iContent.hashCode());
        return result;
    }

    @NonNull
    public String toString() {
        return "BroadcastMessageBean(roomId=" + this.getRoomId() + ", messageId=" + this.getMessageId() + ", serviceNumberId=" + this.getServiceNumberId() + ", broadcastTime=" + this.getBroadcastTime() + ", type=" + this.getType() + ", content=" + this.getContent() + ", topicArray=" + this.getTopicArray() + ", iContent=" + this.getIContent() + ")";
    }

    public BroadcastMessageBeanBuilder toBuilder() {
        return new BroadcastMessageBeanBuilder().roomId(this.roomId).messageId(this.messageId).serviceNumberId(this.serviceNumberId).broadcastTime(this.broadcastTime).type(this.type).content(this.content).topicArray(this.topicArray).iContent(this.iContent);
    }

    public static class BroadcastMessageBeanBuilder {
        private String roomId;
        private String messageId;
        private String serviceNumberId;
        private long broadcastTime;
        private String type;
        private String content;
        private List<TopicEntity> topicArray$value;
        private boolean topicArray$set;
        private IMessageContent<MessageType> iContent;

        BroadcastMessageBeanBuilder() {
        }

        public BroadcastMessageBeanBuilder roomId(String roomId) {
            this.roomId = roomId;
            return this;
        }

        public BroadcastMessageBeanBuilder messageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        public BroadcastMessageBeanBuilder serviceNumberId(String serviceNumberId) {
            this.serviceNumberId = serviceNumberId;
            return this;
        }

        public BroadcastMessageBeanBuilder broadcastTime(long broadcastTime) {
            this.broadcastTime = broadcastTime;
            return this;
        }

        public BroadcastMessageBeanBuilder type(String type) {
            this.type = type;
            return this;
        }

        public BroadcastMessageBeanBuilder content(String content) {
            this.content = content;
            return this;
        }

        public BroadcastMessageBeanBuilder topicArray(List<TopicEntity> topicArray) {
            this.topicArray$value = topicArray;
            this.topicArray$set = true;
            return this;
        }

        public BroadcastMessageBeanBuilder iContent(IMessageContent<MessageType> iContent) {
            this.iContent = iContent;
            return this;
        }

        public BroadcastMessageBean build() {
            List<TopicEntity> topicArray$value = this.topicArray$value;
            if (!this.topicArray$set) {
                topicArray$value = BroadcastMessageBean.$default$topicArray();
            }
            return new BroadcastMessageBean(roomId, messageId, serviceNumberId, broadcastTime, type, content, topicArray$value, iContent);
        }

        @NonNull
        public String toString() {
            return "BroadcastMessageBean.BroadcastMessageBeanBuilder(roomId=" + this.roomId + ", messageId=" + this.messageId + ", serviceNumberId=" + this.serviceNumberId + ", broadcastTime=" + this.broadcastTime + ", type=" + this.type + ", content=" + this.content + ", topicArray$value=" + this.topicArray$value + ", iContent=" + this.iContent + ")";
        }
    }
}





