package tw.com.chainsea.chat.messagekit.theme.viewholder;

import androidx.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.ce.sdk.bean.msg.content.TextContent;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.messagekit.theme.viewholder.base.ThemeMessageBubbleView;


/**
 * text message view
 * Created by 90Chris on 2016/4/20.
 */
public class TextThemeMessageView extends ThemeMessageBubbleView {

    private TextView tvContent;
    private long exitTime;

    public TextThemeMessageView(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    protected int getContentResId() {
        return R.layout.msgkit_reply_text;
    }

    @Override
    protected void inflateContentView() {
        tvContent = findView(R.id.msgkit_text_content);
    }

    @Override
    protected void bindContentView() {
        TextContent textContent = (TextContent) getMsg().content();
//        TextMsgFormat formatText = (TextMsgFormat) getMsg().getFormat();
        String content = textContent.getText();
        if ("%{}%".equals(content)) {
            content = "{}";
        }
//        tvContent.setMaxLines();
        tvContent.setText(content);
//        tvContent.setText(Html.fromHtml(formatText.getContent()));
    }

    @Override
    protected boolean showName() {
        return !isRightMessage();
    }

    @Override
    public void onClick(View v, MessageEntity message) {
        if (onListDilogItemClickListener != null) {
            onListDilogItemClickListener.locationMsg(msg);
        }
    }

    @Override
    public void onDoubleClick(View v, MessageEntity message) {

    }

    @Override
    public void onLongClick(View v, float x, float y, MessageEntity message) {

    }
}
