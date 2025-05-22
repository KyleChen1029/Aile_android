package tw.com.chainsea.custom.view.text.alpha;

import android.content.Context;
import android.util.AttributeSet;

import tw.com.chainsea.custom.view.text.spantouchfix.SpanTouchFixTextView;

/**
 * current by evan on 2019-11-06
 */
public class AlphaTextView extends SpanTouchFixTextView {

    private AlphaViewHelper mAlphaViewHelper;

    public AlphaTextView(Context context) {
        super(context);
    }

    public AlphaTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AlphaTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private AlphaViewHelper getAlphaViewHelper() {
        if (mAlphaViewHelper == null) {
            mAlphaViewHelper = new AlphaViewHelper(this);
        }
        return mAlphaViewHelper;
    }

    @Override
    protected void onSetPressed(boolean pressed) {
        super.onSetPressed(pressed);
        getAlphaViewHelper().onPressedChanged(this, pressed);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        getAlphaViewHelper().onEnabledChanged(this, enabled);
    }

    /**
     * 设置是否要在 press 时改变透明度
     *
     * @param changeAlphaWhenPress 是否要在 press 时改变透明度
     */
    public void setChangeAlphaWhenPress(boolean changeAlphaWhenPress) {
        getAlphaViewHelper().setChangeAlphaWhenPress(changeAlphaWhenPress);
    }

    /**
     * 设置是否要在 disabled 时改变透明度
     *
     * @param changeAlphaWhenDisable 是否要在 disabled 时改变透明度
     */
    public void setChangeAlphaWhenDisable(boolean changeAlphaWhenDisable) {
        getAlphaViewHelper().setChangeAlphaWhenDisable(changeAlphaWhenDisable);
    }
}