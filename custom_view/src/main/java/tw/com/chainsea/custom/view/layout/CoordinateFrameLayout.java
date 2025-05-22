package tw.com.chainsea.custom.view.layout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;


public class CoordinateFrameLayout extends FrameLayout {
    private float downX = 0;
    private float downY = 0;

    private float upX = 0;
    private float upY = 0;

    public CoordinateFrameLayout(Context context) {
        this(context, null);
    }

    public CoordinateFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CoordinateFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
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
        this.performClick();
        return super.onTouchEvent(event);
    }


    public float getDownX() {
        return this.downX;
    }

    public float getDownY() {
        return this.downY;
    }

    public float getUpX() {
        return this.upX;
    }

    public float getUpY() {
        return this.upY;
    }

}
