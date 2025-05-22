package tw.com.chainsea.chat.widget.flowlayout;

import android.content.Context;
import android.view.View;
import android.widget.Checkable;
import android.widget.FrameLayout;

/**
 * Created by jaosnking on 18/8/31.
 */
public class TagView extends FrameLayout implements Checkable {
    private boolean isChecked;
    private static final int[] CHECK_STATE = new int[]{android.R.attr.state_checked};

    public TagView(Context context) {
        super(context);
    }

    public View getTagView() {
        return getChildAt(0);
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        int[] states = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(states, CHECK_STATE);
        }
        return states;
    }


    /**
     * 改变点击的状态
     *
     * @param checked The new checked state
     */
    @Override
    public void setChecked(boolean checked) {
        if (this.isChecked != checked) {
            this.isChecked = checked;
            refreshDrawableState();
        }
    }

    /**
     * @return view的状态，是否点击
     */
    @Override
    public boolean isChecked() {
        return isChecked;
    }

    /**
     * 翻转状态
     */
    @Override
    public void toggle() {
        setChecked(!isChecked);
    }


}
