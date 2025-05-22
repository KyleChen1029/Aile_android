package tw.com.chainsea.chat.messagekit.theme.viewholder;

import android.graphics.Color;
import androidx.annotation.NonNull;
import android.text.SpannableStringBuilder;
import android.view.View;

import java.util.List;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.ce.sdk.bean.msg.MessageType;
import tw.com.chainsea.ce.sdk.bean.msg.content.AtContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.MentionContent;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.keyboard.ChatTextView;
import tw.com.chainsea.chat.lib.AtMatcherHelper;
import tw.com.chainsea.chat.messagekit.theme.viewholder.base.ThemeMessageBubbleView;

/**
 * current by evan on 2019-12-03
 */
public class AtThemeMessageView extends ThemeMessageBubbleView {

    ChatTextView contentCTV;

    public AtThemeMessageView(@NonNull View itemView) {
        super(itemView);
    }


    @Override
    protected boolean showName() {
        return !isRightMessage();
    }

    @Override
    protected int getContentResId() {
        return R.layout.msgkit_at;
    }

    @Override
    protected void inflateContentView() {
        contentCTV = findView(R.id.contentCTV);
        contentCTV.setTextColor(Color.WHITE);

    }

    @Override
    protected void bindContentView() {
        if (MessageType.AT.equals(getMsg().getType()) && getMsg().content() instanceof AtContent) {
            try {
                AtContent atContent = (AtContent) getMsg().content();
                List<MentionContent> ceMentions = atContent.getMentionContents();
                contentCTV.setText(AtMatcherHelper.matcherAtUsers("@", ceMentions, getMembersTable()));
            } catch (Exception e) {
                contentCTV.setText(new SpannableStringBuilder("[標註訊息]"));
            }
        }
    }


    @Override
    public void onClick(View v, MessageEntity message) {

    }

    @Override
    public void onDoubleClick(View v, MessageEntity message) {

    }

    @Override
    public void onLongClick(View v, float x, float y, MessageEntity message) {

    }
}
