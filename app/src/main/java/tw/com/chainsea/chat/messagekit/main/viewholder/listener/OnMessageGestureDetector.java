package tw.com.chainsea.chat.messagekit.main.viewholder.listener;

import static android.view.GestureDetector.SimpleOnGestureListener;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

/**
 * current by evan on 2019-11-06
 */
public abstract class OnMessageGestureDetector<M> extends SimpleOnGestureListener implements View.OnTouchListener {
    private static final String TAG = OnMessageGestureDetector.class.getSimpleName();
    private Context ctx;
    private boolean isFeedback = true;
    private M m;

    private View root;

    private float downX = 0;
    private float downY = 0;

    private float upX = 0;
    private float upY = 0;

    private GestureDetector gestureDetector;

    public abstract void onClick(View v, M m);

    // 雙點事件
    public abstract void onDoubleClick(View v, M m);

    // 長按事件
    public abstract void onLongClick(View v, float x, float y, M m);


    public OnMessageGestureDetector(Context ctx, boolean isFeedback, M m) {
        this.ctx = ctx;
        this.isFeedback = isFeedback;
        this.m = m;
        this.gestureDetector = new GestureDetector(ctx, this);
    }

//    @Override
//    public boolean onScroll(MotionEvent e1, MotionEvent e2, final float distanceX, final float distanceY) {
//        //滑動監聽
//        return true;
//    }

    @Override
    public void onLongPress(@NonNull MotionEvent e) {
        Log.i(TAG, "onLongPress");
        onLongClick(this.root, downX, downY, m);
    }

    @Override
    public boolean onDoubleTap(@NonNull MotionEvent e) {
        Log.i(TAG, "onDoubleTap");
        onDoubleClick(root, m);
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
        Log.i(TAG, "onSingleTapConfirmed");
        onClick(root, m);
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
        //滑動慣性處理
        return super.onFling(e1, e2, velocityX, velocityY);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        this.root = v;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getRawX();
                downY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:

                break;
            case MotionEvent.ACTION_UP:
                upX = event.getRawX();
                upX = event.getRawY();
                break;
        }
        return gestureDetector.onTouchEvent(event);
    }
}
