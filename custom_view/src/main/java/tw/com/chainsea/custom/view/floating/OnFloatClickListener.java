package tw.com.chainsea.custom.view.floating;

import android.view.MotionEvent;

/**
 * current by evan on 2020-01-30
 */
public interface OnFloatClickListener<T> {
    void onClick(DragImageFloatingButton view, T tag);

    void onMove(DragImageFloatingButton view, MotionEvent motionEvent);

    void onMoveUp(DragImageFloatingButton view, MotionEvent motionEvent);
}
