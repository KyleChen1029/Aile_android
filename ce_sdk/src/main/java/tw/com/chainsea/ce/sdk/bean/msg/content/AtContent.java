package tw.com.chainsea.ce.sdk.bean.msg.content;


import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.bean.msg.TargetType;
import tw.com.chainsea.ce.sdk.bean.msg.MessageType;

/**
 * current by evan on 2020-01-08
 */

public class AtContent implements IMessageContent<MessageType> {
    private static final long serialVersionUID = -2274556934352016930L;

    List<MentionContent> mentionContents;

    @Override
    public MessageType getType() {
        return MessageType.AT;
    }

    @Override
    public String toStringContent() {
        if (this.mentionContents != null && !this.mentionContents.isEmpty()) {
            return JsonHelper.getInstance().toJson(this.mentionContents);
        }
        return "[]";
    }

    public AtContent(List<MentionContent> mentionContents) {
        this.mentionContents = mentionContents;
    }

    public List<MentionContent> getMentionContents() {
        return mentionContents;
    }

    public void filter() {
        Iterator<MentionContent> iterator = mentionContents.iterator();
        while (iterator.hasNext()) {
            MentionContent content = iterator.next();
            if (TargetType.UNDEF.equals(content.objectType) || MessageType.UNDEF.equals(content.getType())) {
                iterator.remove();
            }
        }
    }

    @Override
    public String simpleContent() {
        return "[標註訊息]";
    }

    @Override
    public String getFilePath() {
        return null;
    }

    @Override
    public JSONObject getSendObj() {
        return null;
    }
}
