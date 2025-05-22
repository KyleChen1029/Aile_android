package tw.com.chainsea.custom.view.recyclerview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ListView;

import tw.com.chainsea.custom.view.R;

/**
 * current by evan on 2020-01-30
 */
public class ConstraintHeightListView extends ListView {
    private float mMaxHeight = 100;//默认100px

    public ConstraintHeightListView(Context context) {
        this(context, null);
    }

    public ConstraintHeightListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressLint("CustomViewStyleable")
    public ConstraintHeightListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MaxHeightScrollView, 0, defStyleAttr);
        int count = array.getIndexCount();
        for (int i = 0; i < count; i++) {
            int type = array.getIndex(i);
            if (type == R.styleable.MaxHeightScrollView_maxHeight) {
                //获得布局中限制的最大高度
                mMaxHeight = array.getDimension(type, -1);
            }
        }
        array.recycle();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //获取lv本身高度
        int specSize = MeasureSpec.getSize(heightMeasureSpec);
        //限制高度小于lv高度,设置为限制高度
        if (mMaxHeight <= specSize && mMaxHeight > -1) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(Float.valueOf(mMaxHeight).intValue(), MeasureSpec.AT_MOST);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public ConstraintHeightListView setMaxHeight(int maxHeight) {
        this.mMaxHeight = maxHeight;
        return this;
    }
}
