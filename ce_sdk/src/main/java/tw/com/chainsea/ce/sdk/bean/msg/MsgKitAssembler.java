package tw.com.chainsea.ce.sdk.bean.msg;

import java.util.List;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.msg.content.BusinessContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.BusinessTextContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.FileContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.IMessageContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.ImageContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.MentionContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.StickerContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.TextContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.VideoContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.VoiceContent;
import tw.com.chainsea.ce.sdk.config.AppConfig;

/**
 * assembler of account or message
 * Created by 90Chris on 2016/4/21.
 */
public class MsgKitAssembler {
    /**
     * assemble AT MessageFrom
     */
    public static MessageEntity assembleSendAtMessage(String sessionId, String messageId, String senderId, String senderUrl, String content, String sendName) {
        List<MentionContent> mentionContents = JsonHelper.getInstance().fromToList(content, MentionContent[].class);
        return new MessageEntity.Builder()
                .id(messageId)
                .roomId(sessionId)
                .senderId(senderId)
                .senderName(sendName)
                .avatarId(senderUrl)
                .flag(MessageFlag.OWNER)
                .osType(AppConfig.osType)
                .content(JsonHelper.getInstance().toJson(mentionContents))
                .type(MessageType.AT)
                .status(MessageStatus.SENDING)
                .flag(MessageFlag.OWNER)
                .sendTime(System.currentTimeMillis())
                .build();
    }

    /**
     * assemble Business Information
     */
    public static MessageEntity assembleSendBusinessMessage(String roomId, String messageId, UserProfileEntity self, BusinessContent content) {
        return new MessageEntity.Builder()
                .id(messageId)
                .roomId(roomId)
                .senderId(self.getId())
                .senderName(self.getNickName())
                .avatarId(self.getAvatarId())
                .flag(MessageFlag.OWNER)
                .osType(AppConfig.osType)
                .content(JsonHelper.getInstance().toJson(content))
                .type(MessageType.BUSINESS)
                .status(MessageStatus.SENDING)
                .flag(MessageFlag.OWNER)
                .sendTime(System.currentTimeMillis())
                .build();
    }


    public static MessageEntity assembleSendTextMessage(String sessionId, String messageId, String senderId, String senderUrl, String content, String sendName) {
        return new MessageEntity.Builder()
                .id(messageId)
                .roomId(sessionId)
                .senderId(senderId)
                .senderName(sendName)
                .flag(MessageFlag.OWNER)
                .osType(AppConfig.osType)
                .avatarId(senderUrl)
                .status(MessageStatus.SENDING)
                .sendTime(System.currentTimeMillis())
                .content(new TextContent(content).toStringContent())
                .type(MessageType.TEXT)
                .build();
//        return builder.build();
    }

    public static MessageEntity assembleSendTemplateMessage(String sessionId, String messageId, String senderId, String senderUrl, TemplateContent content, String sendName) {
        return new MessageEntity.Builder()
                .id(messageId)
                .roomId(sessionId)
                .senderId(senderId)
                .senderName(sendName)
                .flag(MessageFlag.OWNER)
                .osType(AppConfig.osType)
                .avatarId(senderUrl)
                .status(MessageStatus.SENDING)
                .sendTime(System.currentTimeMillis())
                .content(new TemplateContent(content.getOrientation(), content.getText(), content.getTitle(), content.getImageUrl(), content.getTemplateType(), content.getActions(), content.getElements(), content.getDefaultAction()).toStringContent())
                .type(MessageType.TEMPLATE)
                .build();
    }

    public static MessageEntity assembleBusinessTextMsg(String sessionId, String messageId, String senderId, String senderUrl, String content, String sendName,
                                                        String businessId, String businessCode, String businessContent, String businessTile, String pictureUrl) {

//        BusinessTextMsgFormat formatText = new BusinessTextMsgFormat(businessContent, businessTile, businessCode, businessId, pictureUrl);

        //    public BusinessTextMsgFormat(String content, String title, String businessCode, String businessId, String pictureUrl) {
        String stringContent =
                new BusinessTextContent(
                        businessContent,
                        businessTile,
                        businessCode,
                        businessId,
                        pictureUrl
                        ).toStringContent();

        return new MessageEntity.Builder()
                .content(stringContent)
//                .format(formatText)
                .id(messageId)
                .avatarId(senderUrl)
                .roomId(sessionId)
                .senderName(sendName)
                .senderId(senderId)
                .type(MessageType.BUSINESS_TEXT)
                .build();
    }

    public static MessageEntity assembleCallMsg(String roomId, String messageId, String senderId, String senderUrl, String sendName, String content) {
        IMessageContent<MessageType> callContent = MessageType.CALL.from(content);
        return new MessageEntity.Builder()
//                .format(formatCall)
                .id(messageId)
                .roomId(roomId)
                .content(callContent.toStringContent())
                .senderId(senderId)
                .senderName(sendName)
                .avatarId(senderUrl)
                .type(MessageType.CALL)
                .build();
    }

    public static MessageEntity assembleSystemMsg(String sessionId, String time, int type) {
//        TipMsgFormat formatTip = new TipMsgFormat(time);
//        formatTip.setType(type);
        return new MessageEntity.Builder()
                .id(Tools.generateMessageId())
                .status(MessageStatus.SUCCESS)
                .sourceType(SourceType.SYSTEM)
                .sendTime(System.currentTimeMillis())
                .roomId(sessionId)
//                .format(formatTip)
                .build();
    }


    public static MessageEntity assembleVoiceMsg(String sessionId, String messageId, String senderId, String senderUrl, String sendName, int duration, String path) {
//        VoiceMsgFormat format = new VoiceMsgFormat(duration, path, false);
//        public VoiceMsgFormat(int duration, String url, boolean isRead) {
        String content = new VoiceContent(duration, path, false).toStringContent();

        return new MessageEntity.Builder()
                .content(content)
//                .format(format)
                .id(messageId)
                .avatarId(senderUrl)
                .roomId(sessionId)
                .senderName(sendName)
                .senderId(senderId)
                .type(MessageType.VOICE)
                .sendTime(System.currentTimeMillis())
                .build();
    }


    public static MessageEntity assembleSendVoiceMessage(String roomId, String messageId, String senderId, String sendName, String senderUrl, int duration, String path) {
        return new MessageEntity.Builder()
                .id(messageId)
                .roomId(roomId)
                .avatarId(senderUrl)
                .senderId(senderId)
                .senderName(sendName)
                .type(MessageType.VOICE)
                .status(MessageStatus.SENDING)
                .flag(MessageFlag.OWNER)
                .osType(AppConfig.osType)
                .sendTime(System.currentTimeMillis())
                .content(new VoiceContent(duration, path, false).toStringContent())
                .build();
    }

    public static MessageEntity assembleSendStickerMessage(String sessionId, String messageId, String senderId, String senderUrl, String sendName, String tag, String packageId) {
        return new MessageEntity.Builder()
                .id(messageId)
                .senderId(senderId)
                .roomId(sessionId)
                .senderName(sendName)
                .avatarId(senderUrl)
                .type(MessageType.STICKER)
                .flag(MessageFlag.OWNER)
                .osType(AppConfig.osType)
                .status(MessageStatus.SENDING)
                .sendTime(System.currentTimeMillis())
                .content(new StickerContent(tag, packageId).toStringContent())
                .build();
    }

    public static MessageEntity assembleSendFileMessage(String baseUrl, String sessionId, String messageId, String senderId, String senderUrl, String sendName, String path, String fileName, int size, String url) {
        if (url != null && url.startsWith("/openapi")) {
            url = baseUrl + url;
//            url = NetConfig.getInstance().getUrl() + url;
        }
        return new MessageEntity.Builder()
                .id(messageId)
                .avatarId(senderUrl)
                .roomId(sessionId)
                .senderName(sendName)
                .senderId(senderId)
                .type(MessageType.FILE)
//                .localPath(path)
                .osType(AppConfig.osType)
                .status(MessageStatus.SENDING)
                .sendTime(System.currentTimeMillis())
                .content(new FileContent(fileName, size, url, path).toStringContent())
                .build();
    }

    // EVAN_FLAG 2020-01-09 (1.9.0) support Video Message
    public static MessageEntity assembleVideoMsg(String sessionId, String messageId, String senderId, String senderUrl, String sendName, String url, String name, int size, int width, int height) {
        String content = new VideoContent(
                height,
                width,
                size,
                url,
                name
        ).toStringContent();

        return new MessageEntity.Builder()
                .content(content)
//                .format(format)
                .id(messageId)
                .avatarId(senderUrl)
                .roomId(sessionId)
                .senderName(sendName)
                .senderId(senderId)
                .type(MessageType.VIDEO)
                .build();
    }

    public static MessageEntity assembleSendImageMessage(String sessionId, String messageId, String senderId, String senderUrl, String sendName, String name, String thumbnailName, int width, int height) {
        return new MessageEntity.Builder()
                .id(messageId)
                .avatarId(senderUrl)
                .roomId(sessionId)
                .senderName(sendName)
                .senderId(senderId)
                .type(MessageType.IMAGE)
                .flag(MessageFlag.OWNER)
                .osType(AppConfig.osType)
                .status(MessageStatus.SENDING)
                .sendTime(System.currentTimeMillis())
                .content(ImageContent.Build()
                        .url(name)
                        .thumbnailUrl(thumbnailName)
                        .width(width)
                        .height(height)
                        .thumbnailWidth(width)
                        .thumbnailHeight(height)
                        .build()
                        .toStringContent())
                .build();
    }
}
