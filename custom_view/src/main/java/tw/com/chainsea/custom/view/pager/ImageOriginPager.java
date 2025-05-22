package tw.com.chainsea.custom.view.pager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

/**
 * current by evan on 2019-11-15
 */
public class ImageOriginPager extends ViewPager {

    public ImageOriginPager(Context context) {
        super(context);
    }

    public ImageOriginPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent event) {
        try {
            return super.onTouchEvent(event);
        } catch (IllegalArgumentException ignored) {
            Log.e("ImageOriginPager-error", "IllegalArgumentException 錯誤被活捉了!");
            return false;
        }
    }

    //
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        try {
            return super.onInterceptTouchEvent(event);
        } catch (IllegalArgumentException ignored) {
            Log.e("ImageOriginPager-error", "IllegalArgumentException 錯誤被活捉了!");
            return false;
        }
    }

}
