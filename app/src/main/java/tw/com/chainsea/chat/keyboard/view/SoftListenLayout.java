package tw.com.chainsea.chat.keyboard.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.RelativeLayout;

public abstract class SoftListenLayout extends RelativeLayout {
    private int mMaxParentHeight = 0;
    private int mOldHeight = 0;
    private final int mMinLayoutHeight;
    private final int mMaxNavBarHeight;
    private int measureHeight;
    private boolean isKeyboardOpen;

    public SoftListenLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(metrics);
        mMinLayoutHeight = metrics.heightPixels * 2 / 3; //the height of layout is at least 2/3 of screen height
        mMaxNavBarHeight = metrics.heightPixels / 6; // max height of navigation bar is 1/6 of height
    }

    /**
     * when keyboard hide, three onMeasure will be called
     * onMeasure measureHeight = 1533
     * onMeasure measureHeight = 725
     * onLayout top = 0, bottom = 1533
     * onMeasure measureHeight = 725
     * onLayout top = 0, bottom = 725
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureHeight = measureHeight(heightMeasureSpec);

        if (mMaxParentHeight != 0 && Math.abs(measureHeight - mOldHeight) < mMaxNavBarHeight) {  //for some devices whose the bottom navigation bar can be hidden or shown
            mMaxParentHeight += (measureHeight - mOldHeight);
        } else if (mMaxParentHeight == 0 || measureHeight > mMinLayoutHeight) {  //ignore keyboard shown making height shorter
            mMaxParentHeight = measureHeight;
        }

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int expandSpec = MeasureSpec.makeMeasureSpec(mMaxParentHeight, heightMode);
        super.onMeasure(widthMeasureSpec, expandSpec);

        mOldHeight = measureHeight;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, mMaxParentHeight);
        if (mMaxParentHeight - measureHeight > 0 && !isKeyboardOpen) {
            OnSoftKeyboardPop(mMaxParentHeight - measureHeight);
            isKeyboardOpen = true;
        } else if (mMaxParentHeight - measureHeight == 0 && isKeyboardOpen) {
            OnSoftKeyboardClose();
            isKeyboardOpen = false;
        }
    }

    private int measureHeight(int pHeightMeasureSpec) {
        int result = 0;
        int heightMode = MeasureSpec.getMode(pHeightMeasureSpec);
        int heightSize = MeasureSpec.getSize(pHeightMeasureSpec);

        switch (heightMode) {
            case MeasureSpec.AT_MOST:
            case MeasureSpec.EXACTLY:
                result = heightSize;
                break;
            case MeasureSpec.UNSPECIFIED:
                break;
        }
        return result;
    }

    abstract void OnSoftKeyboardPop(int height);

    abstract void OnSoftKeyboardClose();
}
