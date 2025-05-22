package tw.com.chainsea.chat.widget;

import android.text.TextPaint;
import android.text.style.UnderlineSpan;

import androidx.annotation.NonNull;

/**
 * Created by jerry.yang on 2018/7/18.
 * desc:
 */
public class NoLineClickableSpan extends UnderlineSpan {

    @Override
    public void updateDrawState(@NonNull TextPaint ds) {
        super.updateDrawState(ds);
        ds.setColor(ds.linkColor);
        ds.setUnderlineText(false);
    }
}
