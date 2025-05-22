package tw.com.chainsea.chat.widget.photo;

import android.content.Context;
import android.graphics.Canvas;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class PhotoRecyclerView extends RecyclerView {
    private OnDispatchTouchListener mOnDispatchTouchListener;

    public PhotoRecyclerView(Context context) {
        this(context, null);
        init();
    }

    public boolean isIntercepter = true;

    public PhotoRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
        init();
    }

    public PhotoRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        // 啟用子檢視排序功能
        setChildrenDrawingOrderEnabled(true);
    }

    @Override
    public boolean dispatchTouchEvent(final MotionEvent event) {
        if (mOnDispatchTouchListener != null) {
            mOnDispatchTouchListener.onDispatchTouch(event);
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        if (isIntercepter) {
            return super.onInterceptTouchEvent(e);
        } else {
            return false;
        }

    }

    public void setmOnDispatchTouchListener(OnDispatchTouchListener mOnDispatchTouchListener) {
        this.mOnDispatchTouchListener = mOnDispatchTouchListener;
    }

    public interface OnDispatchTouchListener {
        void onDispatchTouch(MotionEvent event);
    }

    private int mSelectedPosition = 0;

    @Override
    public void onDraw(Canvas c) {
        mSelectedPosition = getChildAdapterPosition(getFocusedChild());
        super.onDraw(c);
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        int position = mSelectedPosition;
        if (position < 0) {
            return i;
        } else {
            if (i == childCount - 1) {
                if (position > i) {
                    position = i;
                }
                return position;
            }
            if (i == position) {
                return childCount - 1;
            }
        }
        return i;
    }
}
