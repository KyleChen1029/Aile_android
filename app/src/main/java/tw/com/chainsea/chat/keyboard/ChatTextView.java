package tw.com.chainsea.chat.keyboard;

import android.content.Context;
import android.util.AttributeSet;

import tw.com.chainsea.custom.view.text.OnSpanClickListener;
import tw.com.chainsea.custom.view.text.alpha.AlphaTextView;

/**
 * HadTextView
 * Created by 90Chris on 2015/11/24.
 */
public class ChatTextView extends AlphaTextView implements OnSpanClickListener {
    Context mContext;

    public ChatTextView(Context context) {
        super(context);
        init(context);
    }

    public ChatTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ChatTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        setText(getText());
    }

//    @Override
//    public void setText(CharSequence text, BufferType type) {
//        mOriginText = text;
//        if (!TextUtils.isEmpty(text)) {
//            SpannableStringBuilder builder = new SpannableStringBuilder(text);
//            Linkify.addLinks(builder, mAutoLinkMaskCompat, mLinkTextColor, mLinkBgColor, this);
//            text = builder;
//        }
//        super.setText(text, type);
//    }

//    @Override
//    public void setText(CharSequence text, BufferType type) {
//        if (!TextUtils.isEmpty(text)) {
//            SpannableStringBuilder builder = new SpannableStringBuilder(text);
//            EmoticonHandler.getInstance(mContext).setTextFace(text.toString(), builder, 0, Utils.getFontSize(getTextSize()));
//            Pattern pattern = Pattern.compile("(?:http|https):\\/\\/((?:[\\w-]+)(?:\\.[\\w-]+)+)(?:[\\w.,@?^=%&amp;:\\/~+#-]*[\\w@?^=%&amp;\\/~+#-])?");
//            Linkify.addLinks(builder, pattern,  new String[]{"http://", "https://"}, Linkify.WEB_URLS, ContextCompat.getColorStateList(getContext(), R.color.link_color), ColorStateList.valueOf(Color.TRANSPARENT), this);
//            text = builder;
//        }
//        super.setText(text, type);
//    }


    @Override
    public boolean onSpanClick(String text) {
        return false;
    }
}
