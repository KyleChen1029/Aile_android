package tw.com.chainsea.chat.messagekit.theme.viewholder;

import android.graphics.Color;
import androidx.annotation.NonNull;
import android.text.SpannableStringBuilder;
import android.view.View;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.keyboard.ChatTextView;
import tw.com.chainsea.chat.messagekit.theme.viewholder.base.ThemeMessageBubbleView;

/**
 * current by evan on 2019-12-06
 */
public class NoneThemeMessageView extends ThemeMessageBubbleView {
    ChatTextView contentCTV;

    public NoneThemeMessageView(@NonNull View itemView) {
        super(itemView);
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

    @Override
    protected boolean showName() {
        return !isRightMessage();
    }

    @Override
    protected int getContentResId() {
        return R.layout.msgkit_none;
    }

    @Override
    protected void inflateContentView() {
        contentCTV = findView(R.id.contentCTV);
        contentCTV.setTextColor(Color.WHITE);
    }

    @Override
    protected void bindContentView() {
        contentCTV.setText(new SpannableStringBuilder("[?????]"));
    }
}
