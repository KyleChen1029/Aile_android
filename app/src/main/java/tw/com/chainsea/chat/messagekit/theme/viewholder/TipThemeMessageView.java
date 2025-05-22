package tw.com.chainsea.chat.messagekit.theme.viewholder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import java.text.SimpleDateFormat;
import java.util.Locale;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.ce.sdk.bean.msg.SourceType;
import tw.com.chainsea.ce.sdk.bean.msg.content.UndefContent;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.messagekit.theme.viewholder.base.ThemeMessageViewBase;
import tw.com.chainsea.chat.util.ThemeHelper;

/**
 * current by evan on 2019-12-06
 */
public class TipThemeMessageView extends ThemeMessageViewBase {

    boolean isGreenTheme = false;
    private TextView msgkitTipContent;

    public TipThemeMessageView(@NonNull View itemView) {
        super(itemView);
        isGreenTheme = ThemeHelper.INSTANCE.isGreenTheme();
    }

    @Override
    protected int getResId() {
        return R.layout.item_msg_tip;
    }

    @Override
    protected void inflate() {
        msgkitTipContent = findView(R.id.msgkit_tip_content);
    }

    @Override
    public void refresh(MessageEntity message) {
        if (SourceType.SYSTEM.equals(message.getSourceType()) && message.content() instanceof UndefContent) {
            UndefContent undefContent = (UndefContent) message.content();
            if ("TIME_LINE".equals(undefContent.getText())) {
                msgkitTipContent.setTextColor(isGreenTheme ? ResourcesCompat.getColor(msgkitTipContent.getContext().getResources(), R.color.color_015F57, null) : 0xFF76B9CB);
                msgkitTipContent.setBackgroundResource(R.drawable.bg_theme_time_msg_bg);
                String date = new SimpleDateFormat("MMMdd日(EEE)", Locale.TAIWAN).format(message.getSendTime());
                msgkitTipContent.setText(date);
                msgkitTipContent.setVisibility(View.VISIBLE);
            }
        }
    }
}
