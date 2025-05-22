package tw.com.chainsea.custom.view.recyclerview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.recyclerview.widget.RecyclerView;

import tw.com.chainsea.custom.view.R;

/**
 * current by evan on 2019-12-03
 */
public class MaxHeightRecyclerView extends RecyclerView {
    private int mMaxHeight = 0;

    public MaxHeightRecyclerView(Context context) {
        super(context);
    }

    public MaxHeightRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public MaxHeightRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs);
    }

    @SuppressLint("CustomViewStyleable")
    private void initialize(Context context, AttributeSet attrs) {
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.MaxHeightScrollView);
        mMaxHeight = arr.getLayoutDimension(R.styleable.MaxHeightScrollView_maxHeight, mMaxHeight);
        arr.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mMaxHeight > 0) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxHeight, MeasureSpec.AT_MOST);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setMaxHeight(int mMaxHeight) {
        if (mMaxHeight >= 0) {
            this.mMaxHeight = mMaxHeight;
        }
    }
}
