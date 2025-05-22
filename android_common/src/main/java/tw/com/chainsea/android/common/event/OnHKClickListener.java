package tw.com.chainsea.android.common.event;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;


/**
 * Click the event to instantly close the soft keyboard to monitor the event
 *
 * @param <T>
 */
public abstract class OnHKClickListener<T> implements View.OnClickListener {

    private int tagKey = -1;
    private boolean isHide = true;

    public abstract void onClick(View v, T t);

    protected OnHKClickListener() {

    }

    public OnHKClickListener(int tagKey, boolean isHide) {
        this.tagKey = tagKey;
        this.isHide = isHide;
    }

    public OnHKClickListener(int tagKey) {
        this(tagKey, true);
    }

    public OnHKClickListener(boolean isHide) {
        this(-1, isHide);
    }

    @Override
    public void onClick(View v) {
        if (this.isHide) {
            InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
        try {
            Object tag = null;
            if (this.tagKey != -1) {
                tag = v.getTag(this.tagKey);
            } else {
                tag = v.getTag();
            }
            onClick(v, (T) tag);
        } catch (NullPointerException e) {
            onClick(v, null);
        }
    }


    public static <T> OnHKClickListener<T> newEmpty() {
        return new OnHKClickListener<T>() {
            @Override
            public void onClick(View v, T t) {

            }
        };
    }
}
