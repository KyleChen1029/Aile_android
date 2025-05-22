package tw.com.chainsea.custom.view.pager;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * current by evan on 2020-01-02
 */
public class CannotSlidingViewpager extends ViewPager {
    private boolean isCanScroll;
    private float beforeX = 0.0f;

    public CannotSlidingViewpager(@NonNull Context context) {
        super(context);
    }

    public CannotSlidingViewpager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (this.isCanScroll) {
            return super.dispatchTouchEvent(ev);
        } else {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    this.beforeX = ev.getX();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float motionValue = ev.getX() - beforeX;
                    if (motionValue < 0) {
                        return true;
                    }
                    beforeX = ev.getX();
                    break;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public void setCanScroll(boolean canScroll) {
        isCanScroll = canScroll;
    }
}
