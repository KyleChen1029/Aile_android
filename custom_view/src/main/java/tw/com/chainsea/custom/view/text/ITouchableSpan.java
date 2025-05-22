package tw.com.chainsea.custom.view.text;

import android.view.View;

/**
 * current by evan on 2019-11-06
 */
public interface ITouchableSpan {
    void setPressed(boolean pressed);
    void onClick(View widget);
}
