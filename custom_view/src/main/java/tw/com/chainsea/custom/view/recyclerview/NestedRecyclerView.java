package tw.com.chainsea.custom.view.recyclerview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.NestedScrollingParent;
import androidx.recyclerview.widget.RecyclerView;

/**
 * current by evan on 2020-04-15
 *
 * @author Evan Wang
 * date 2020-04-15
 */
public class NestedRecyclerView extends RecyclerView implements NestedScrollingParent {
    private View nestedScrollTarget = null;
    private boolean nestedScrollTargetIsBeingDragged = false;
    private boolean nestedScrollTargetWasUnableToScroll = false;
    private boolean skipsTouchInterception = false;


    public NestedRecyclerView(@NonNull Context context) {
        super(context);
    }

    public NestedRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public NestedRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean temporarilySkipsInterception = nestedScrollTarget != null;
        if (temporarilySkipsInterception) {
            // If a descendent view is scrolling we set a flag to temporarily skip our onInterceptTouchEvent implementation
            skipsTouchInterception = true;
        }
        boolean handled = super.dispatchTouchEvent(ev);
        if (temporarilySkipsInterception) {
            skipsTouchInterception = false;
            // If the first dispatch yielded no result or we noticed that the descendent view is unable to scroll in the
            // direction the user is scrolling, we dispatch once more but without skipping our onInterceptTouchEvent.
            // Note that RecyclerView automatically cancels active touches of all its descendents once it starts scrolling
            // so we don't have to do that.
            if (!handled || nestedScrollTargetWasUnableToScroll) {
                handled = super.dispatchTouchEvent(ev);
            }
        }
        return handled;
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        return !skipsTouchInterception && super.onInterceptTouchEvent(e);
//        return super.onInterceptTouchEvent(e);
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        if (target.equals(nestedScrollTarget) && !nestedScrollTargetIsBeingDragged) {
            if (dyConsumed != 0) {
                // The descendent was actually scrolled, so we won't bother it any longer.
                // It will receive all future events until it finished scrolling.
                nestedScrollTargetIsBeingDragged = true;
                nestedScrollTargetWasUnableToScroll = false;
            } else if (dyUnconsumed != 0) {
                // The descendent tried scrolling in response to touch movements but was not able to do so.
                // We remember that in order to allow RecyclerView to take over scrolling.
                nestedScrollTargetWasUnableToScroll = true;
                try {
                    target.getParent().requestDisallowInterceptTouchEvent(false);
                } catch (Exception ignored) {

                }
//                target.parent?.requestDisallowInterceptTouchEvent(false)
            }
        }


//        super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
    }


    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes) {
        if (axes != 0 && View.SCROLL_AXIS_VERTICAL != 0) {
            // A descendent started scrolling, so we'll observe it.
            nestedScrollTarget = target;
            nestedScrollTargetIsBeingDragged = false;
            nestedScrollTargetWasUnableToScroll = false;
        }

        super.onNestedScrollAccepted(child, target, axes);
    }

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int nestedScrollAxes) {
        return nestedScrollAxes != 0 && View.SCROLL_AXIS_VERTICAL != 0;
//        return super.onStartNestedScroll(child, target, nestedScrollAxes);
    }


    @Override
    public void onStopNestedScroll(@NonNull View child) {
        nestedScrollTarget = null;
        nestedScrollTargetIsBeingDragged = false;
        nestedScrollTargetWasUnableToScroll = false;
//        super.onStopNestedScroll(child);
    }
}
