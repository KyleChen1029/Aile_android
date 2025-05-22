package tw.com.chainsea.custom.view.pager;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.viewpager.widget.ViewPager;

import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * current by evan on 2020-09-14
 *
 * @author Evan Wang
 * @date 2020-09-14
 */
public class NoScrollViewPager extends ViewPager {
    private boolean isScroll;

    public NoScrollViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoScrollViewPager(Context context) {
        super(context);
    }

    /**
     * 1.dispatchTouchEvent一般情況不做處理
     * ,如果修改了預設的返回值,子孩子都無法收到事件
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev); // return true;不行
    }

    /**
     * 是否攔截
     * 攔截:會走到自己的onTouchEvent方法裡面來
     * 不攔截:事件傳遞給子孩子
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
// return false;//可行,不攔截事件,
// return true;//不行,孩子無法處理事件
//return super.onInterceptTouchEvent(ev);//不行,會有細微移動
        if (isScroll) {
            return super.onInterceptTouchEvent(ev);
        } else {
            return false;
        }
    }

    /**
     * 是否消費事件
     * 消費:事件就結束
     * 不消費:往父控制元件傳
     */
    @Override
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent ev) {
//return false;// 可行,不消費,傳給父控制元件
//return true;// 可行,消費,攔截事件
//super.onTouchEvent(ev); //不行,
//雖然onInterceptTouchEvent中攔截了,
//但是如果viewpage裡面子控制元件不是viewgroup,還是會呼叫這個方法.
        if (isScroll) {
            return super.onTouchEvent(ev);
        } else {
            this.performClick();
            return true;// 可行,消費,攔截事件
        }
    }

    public void setScroll(boolean scroll) {
        isScroll = scroll;
    }
}
