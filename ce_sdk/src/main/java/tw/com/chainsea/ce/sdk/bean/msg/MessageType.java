package tw.com.chainsea.ce.sdk.bean.msg;

import com.google.common.collect.Sets;
import com.google.gson.annotations.SerializedName;
import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

import java.util.List;
import java.util.Set;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.bean.msg.content.AtContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.BroadcastContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.BusinessContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.CallContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.FileContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.IMessageContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.ImageContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.MentionContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.StickerContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.TextContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.TransferContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.UndefContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.VideoContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.VoiceContent;

/**
 * ReceiveMsgType
 * Created by 90Chris on 2014/11/10.
 */
public enum MessageType {
    @SerializedName("Text") TEXT("Text") {
        @Override
        public IMessageContent<MessageType> from(String content) {
            try {
                TextContent tc = JsonHelper.getInstance().from(content, TextContent.class);
                if (tc != null && tc.getText() == null) {
                    tc.setText(content);
                    return tc;
                }
                if (tc == null) {
                    throw new NullPointerException();
                }
                return tc;
            } catch (Exception e) {
                return new TextContent(content);
            }
        }
    },
    @SerializedName("Image") IMAGE("Image") {
        @Override
        public IMessageContent<MessageType> from(String content) {
            return JsonHelper.getInstance().from(content, ImageContent.class);
        }
    },
    @SerializedName("Voice") VOICE("Voice") {
        @Override
        public IMessageContent<MessageType> from(String content) {
            return JsonHelper.getInstance().from(content, VoiceContent.class);
        }
    },
    @SerializedName("Video") VIDEO("Video") {
        @Override
        public IMessageContent<MessageType> from(String content) {
            return JsonHelper.getInstance().from(content, VideoContent.class);
        }
    },
    @SerializedName("File") FILE("File") {
        @Override
        public IMessageContent<MessageType> from(String content) {
            return JsonHelper.getInstance().from(content, FileContent.class);
        }
    },
    @SerializedName("Sticker") STICKER("Sticker") {
        @Override
        public IMessageContent<MessageType> from(String content) {
            return JsonHelper.getInstance().from(content, StickerContent.class);
        }
    },
    @SerializedName("At") AT("At") { //目前只有文字, 表示針對特定被標注的人
        @Override
        public IMessageContent<MessageType> from(String content) {
            List<MentionContent> mentionContents = JsonHelper.getInstance().fromToList(content, MentionContent[].class);
            for (MentionContent m : mentionContents) {
                IMessageContent aContent;
                String c = m.getContent() == null ? "" : JsonHelper.getInstance().toJson(m.getContent());

                aContent = m.getType().from(c);
                m.setContent(aContent);
            }
            return new AtContent(mentionContents);
        }
    },
    @SerializedName("Business") BUSINESS("Business") {
        @Override
        public IMessageContent<MessageType> from(String content) {
            return JsonHelper.getInstance().from(content, BusinessContent.class);
        }
    },
    @SerializedName("Transfer") TRANSFER("Transfer") {
        @Override
        public IMessageContent<MessageType> from(String content) {
            return JsonHelper.getInstance().from(content, TransferContent.class);
        }
    },
    @SerializedName("Template") TEMPLATE("Template") {
        @Override
        public IMessageContent<MessageType> from(String content) {
            return JsonHelper.getInstance().from(content, TemplateContent.class);
        }
    },
    @SerializedName("Broadcast") BROADCAST("Broadcast") {
        @Override
        public IMessageContent<MessageType> from(String content) {
            return JsonHelper.getInstance().from(content, BroadcastContent.class);
        }
    },
    @SerializedName("Call") CALL("Call") {
        @Override
        public IMessageContent<MessageType> from(String content) {
            try {
                return JsonHelper.getInstance().from(content, CallContent.class);
            } catch (Exception e) {
                return new CallContent(content);
            }
        }
    },
    @SerializedName("None") UNDEF("None") {
        @Override
        public IMessageContent<MessageType> from(String content) {
            if (content == null) {
                return new UndefContent();
            } else {
                return new UndefContent(content);
            }
        }
    },

    @SerializedName("BusinessText") BUSINESS_TEXT("BusinessText") {
        @Override
        public IMessageContent<MessageType> from(String content) {
            return new UndefContent();
        }
    },
    @SerializedName("Location") LOCATION("Location") {
        @Override
        public IMessageContent<MessageType> from(String content) {
            return new UndefContent();
        }
    },
    @SerializedName("ImageText") IMAGE_TEXT("ImageText") {
        @Override
        public IMessageContent<MessageType> from(String content) {
            return new UndefContent();
        }
    },
    @SerializedName("ListText") LIST_TEXT("ListText") {
        @Override
        public IMessageContent<MessageType> from(String content) {
            return new UndefContent();
        }
    },
    @SerializedName("Ad") AD("Ad") {
        @Override
        public IMessageContent<MessageType> from(String content) {
            return new UndefContent();
        }
    },
    @SerializedName("Line") LINE("Line") {
        @Override
        public IMessageContent<MessageType> from(String content) {
            return new UndefContent();
        }
    },

    @SerializedName("System") SYSTEM("System") {
        @Override
        public IMessageContent<MessageType> from(String content) {
            return new UndefContent();
        }
    };

    private String type;
    public String getValue() {
        return type;
    }


    public static class MessageTypeAdapter  {
        @ToJson
        String toJson(MessageType type) {
            return type.type;
        }

        @FromJson
        MessageType fromJson(String type) {
            return MessageType.of(type);
        }
    }

    /**
     * get client enum code from server string code
     *
     * @param severCode server string code
     * @return client enum code
     */
    public static MessageType of(String severCode) {
        if (severCode == null) {
            return UNDEF;
        }
        for (MessageType item : values()) {
            if (item.getValue().equals(severCode)) {
                return item;
            }
        }
        return UNDEF;
    }

    public abstract IMessageContent<MessageType> from(String content);

    public static Set<MessageType> NON_UPLOAD_TYPES = Sets.newHashSet(TEXT, AT, STICKER, BUSINESS);
    public static Set<MessageType> AT_or_TEXT = Sets.newHashSet(TEXT, AT);
    public static Set<MessageType> IMAGE_or_VIDEO_or_FILE = Sets.newHashSet(IMAGE, VIDEO, FILE);
    public static Set<MessageType> AT_or_TEXT_or_IMAGE_or_VIDDO = Sets.newHashSet(TEXT, AT, IMAGE, VIDEO);

    MessageType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
