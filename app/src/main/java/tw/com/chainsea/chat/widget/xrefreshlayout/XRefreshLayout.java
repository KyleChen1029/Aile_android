package tw.com.chainsea.chat.widget.xrefreshlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.OverScroller;

import androidx.annotation.NonNull;
import androidx.annotation.Px;
import androidx.core.view.NestedScrollingParent;
import androidx.core.view.ViewCompat;

import com.google.common.collect.Range;

import java.util.Date;

import tw.com.chainsea.android.common.ui.UiHelper;
import tw.com.chainsea.chat.widget.xrefreshlayout.loadinglayout.DefaultLoadingLayout;
import tw.com.chainsea.chat.widget.xrefreshlayout.loadinglayout.ILoadingLayout;


/**
 * Created by dance on 2017/4/2.
 */

public class XRefreshLayout extends FrameLayout implements NestedScrollingParent {


    private int MIN_LOADING_LAYOUG_HEIGHT = 40;
    private int MAX_LOADING_LAYOUG_HEIGHT;
    private int MAX_DURATION = 2000;
    //range when overscroll header or footer
    private int OVERSCROLL_RANGE = 80;
    private boolean isEnableRefresh = false;
    private boolean isEnableloadMore = false;
    private ILoadingLayout loadingLayout;
    private View header;
    private View footer;
    private View refreshView;
    private OverScroller scroller;
    private boolean isNeedInitLoadingLayout = false;
    public int duration = 0;
    boolean isRelease = false;
    private boolean isSmoothScrolling = false;

    private OnRefreshListener refreshListener;
    private OnLoadMoreListener loadMoreListener;
    private OnBackgroundClickListener backgroundClickListener;
    private OnScrollerListener onScrollerListener;

    public XRefreshLayout(Context context) {
        this(context, null);
    }

    public XRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XRefreshLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        MIN_LOADING_LAYOUG_HEIGHT = UiHelper.dip2px(context, MIN_LOADING_LAYOUG_HEIGHT, 0.5f);
        OVERSCROLL_RANGE = UiHelper.dip2px(context, OVERSCROLL_RANGE, 0.5f);

        scroller = new OverScroller(getContext());
        loadingLayout = new DefaultLoadingLayout();

    }


    protected void initLoadingLayout() {
        if (header != null) {
            removeView(header);
        }
        if (footer != null) {
            removeView(footer);
        }

        header = loadingLayout.createLoadingHeader(getContext(), this);
        footer = loadingLayout.createLoadingFooter(getContext(), this);

        addView(header);
        addView(footer);
        header.setVisibility(GONE);
        footer.setVisibility(GONE);
        //init header and footer view.
        loadingLayout.initAndResetHeader();
        loadingLayout.initAndResetFooter();
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() != 1) {
            throw new IllegalArgumentException("XRefreshLayout must have only 1 child to pull!");
        }
        refreshView = getChildAt(0);

        //create loading layout:  header and footer
        initLoadingLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        MAX_LOADING_LAYOUG_HEIGHT = getMeasuredHeight() / 2;
        measureHeaderAndFooter(widthMeasureSpec, header);
        measureHeaderAndFooter(widthMeasureSpec, footer);

        refreshView.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY));
    }

    /**
     * limit header and footer min-height, max-height
     *
     * @param widthMeasureSpec
     * @param view
     */
    private void measureHeaderAndFooter(int widthMeasureSpec, View view) {
        int height = Math.max(MIN_LOADING_LAYOUG_HEIGHT, view.getMeasuredHeight());
        height = Math.min(height, MAX_LOADING_LAYOUG_HEIGHT);
        view.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        header.layout(0, -header.getMeasuredHeight(), header.getMeasuredWidth(), 0);
        refreshView.layout(0, 0, refreshView.getMeasuredWidth(), refreshView.getMeasuredHeight());
        footer.layout(0, refreshView.getBottom(), footer.getMeasuredWidth(), refreshView.getBottom()
            + footer.getMeasuredHeight());
    }


    Range<Float> rangeY = null;
    Range<Float> rangeX = null;
    long tmpTime = 0L;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isSmoothScrolling) {
            return true;
        }

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                rangeY = Range.open(Math.abs(ev.getY()) - 5f, Math.abs(ev.getY()) + 5f);
                rangeX = Range.open(Math.abs(ev.getX()) - 5f, Math.abs(ev.getX()) + 5f);
                tmpTime = new Date().getTime();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                if (new Date().getTime() - tmpTime < 500L) {
                    if (rangeY.contains(Math.abs(ev.getY())) && rangeX.contains(Math.abs(ev.getX()))) {
//                        L.e("onclick");
                        if (backgroundClickListener != null) {
                            backgroundClickListener.onBackgroundClick(this);
                        }
                        // EVAN_FLAG 2019-08-31 點擊背景事件，做callback接口 
                    }
                }
                rangeY = null;
                rangeX = null;
                tmpTime = 0L;
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes) {
        isRelease = false;
    }

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int nestedScrollAxes) {
        loadingLayout.initAndResetHeader();
        loadingLayout.initAndResetFooter();
        isPullHeader = false;
        isPullFooter = false;
        return true;
    }


    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
    }

    /**
     * when release from XRefreshLayout!
     *
     * @param child
     */
    @Override
    public void onStopNestedScroll(@NonNull View child) {
        isRelease = true;
        if (isPullHeader && isEnableRefresh) {
            if (getScrollY() <= -header.getMeasuredHeight()) {
                //header shown fully.
                int dy = -header.getMeasuredHeight() - getScrollY();
                smoothScroll(dy);
                loadingLayout.onHeaderRefreshing();
                if (refreshListener != null) {
                    refreshListener.onRefresh();
                }
            } else {
                //hide header smoothly.
                isNeedInitLoadingLayout = true;
                int dy = -getScrollY();
                smoothScroll(dy);

            }
        } else if (isPullFooter && isEnableloadMore) {
            if (getScrollY() >= footer.getMeasuredHeight()) {
                //footer shown fully.
                int dy = footer.getMeasuredHeight() - getScrollY();
                smoothScroll(dy);
                loadingLayout.onFooterRefreshing();
                if (loadMoreListener != null) {
                    loadMoreListener.onLoadMore();
                }
            } else {
                //hide footer smoothly.
                isNeedInitLoadingLayout = true;
                int dy = -getScrollY();
                smoothScroll(dy);
            }
        } else {
            //hide footer smoothly.
            isNeedInitLoadingLayout = true;
            int dy = -getScrollY();
            smoothScroll(dy);
        }
//        tmpTime = 0l;
//        range = null;
    }


    /**
     * smooth scroll to target val.
     *
     * @param dy
     */
    private void smoothScroll(int dy) {
        duration = calculateDuration(Math.abs(dy));
//        duration = 800;
        scroller.startScroll(0, getScrollY(), 0, dy, duration);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    /**
     * calculate the duration for animation by dy.
     *
     * @param dy
     * @return
     */
    private int calculateDuration(int dy) {
        float fraction = dy * 1F / MAX_LOADING_LAYOUG_HEIGHT;
        return (int) (fraction * MAX_DURATION);
    }

    boolean isPullHeader, isPullFooter;
    float dy;

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed) {
        this.dy = dy;

        isPullHeader = (dy < 0 && getScrollY() <= 0 && !ViewCompat.canScrollVertically(refreshView, -1))
            || (dy >= 0 && getScrollY() < 0);
        isPullFooter = (dy > 0 && !ViewCompat.canScrollVertically(refreshView, 1) && getScrollY() >= 0)
            || (dy < 0 && getScrollY() > 0 && getScrollY() <= getFooterScrollRange() && !isPullHeader);

        if (isPullHeader || isPullFooter) {
            scrollBy(0, dy);
            consumed[1] = dy;
        }

    }

    private int getHeaderScrollRange() {
        return header.getMeasuredHeight() + OVERSCROLL_RANGE;
    }

    private int getFooterScrollRange() {
        return footer.getMeasuredHeight() + OVERSCROLL_RANGE;
    }


    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
    }

    @Override
    public boolean onNestedPreFling(@NonNull View target, float velocityX, float velocityY) {
        if (isPullFooter) {
            return true;
        }
        return !(dy < 0) && (getScrollY() > getFooterScrollRange() || getScrollY() < 0);
    }

    @Override
    public boolean onNestedFling(@NonNull View target, float velocityX, float velocityY, boolean consumed) {
        return false;
    }

    @Override
    public int getNestedScrollAxes() {
        return ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    @Override
    public void scrollTo(@Px int x, @Px int y) {
        if (isPullHeader) {
            if (y < -getHeaderScrollRange()) {
                y = -getHeaderScrollRange();
            } else if (y > 0) {
                y = 0;
            }

            //call percent
            float percent = Math.abs(y) * 1f / header.getMeasuredHeight();
            percent = Math.min(percent, 1f);
            if (!isRelease) {
                loadingLayout.onPullHeader(percent);
            }

        } else if (isPullFooter) {
            if (y > getFooterScrollRange()) {
                y = getFooterScrollRange();
            } else if (y < 0) {
                y = 0;
            }

            //call percent
            float percent = Math.abs(y) * 1f / footer.getMeasuredHeight();
            percent = Math.min(percent, 1f);
            if (!isRelease) {
                loadingLayout.onPullFooter(percent);
            }
        }
        super.scrollTo(x, y);
    }


    //    int tmpY = 0;Range

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (onScrollerListener != null) {
            onScrollerListener.onScroller(XRefreshLayout.this, scroller);
        }

        isSmoothScrolling = isRelease && Math.abs(scroller.getCurrY()) > 8;
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            ViewCompat.postInvalidateOnAnimation(this);
        } else {
            //animation finish.
            if (isNeedInitLoadingLayout) {
                isNeedInitLoadingLayout = false;
                loadingLayout.initAndResetHeader();
                loadingLayout.initAndResetFooter();
            }
        }
    }


    /**
     * set your custom loadinglayout.
     *
     * @param loadingLayout
     */
    public void setLoadingLayout(ILoadingLayout loadingLayout) {
        if (isRelease && isSmoothScrolling) {
            return;
        }
        this.loadingLayout = loadingLayout;
        initLoadingLayout();
        requestLayout();
    }

    /**
     * complete the refresh state!
     */
    public void completeRefresh() {
        isNeedInitLoadingLayout = true;
        smoothScroll(-getScrollY());

    }


    public XRefreshLayout setOnRefreshListener(OnRefreshListener listener) {
        this.refreshListener = listener;
        isEnableRefresh = true;
        header.setVisibility(VISIBLE);
        return this;
    }

    public XRefreshLayout setOnLoadMoreListener(OnLoadMoreListener listener) {
        this.loadMoreListener = listener;
        isEnableloadMore = true;
        footer.setVisibility(VISIBLE);
        return this;
    }

    // EVAN_REFACTOR: 2019-08-31 背景點擊事件
    public XRefreshLayout setOnBackgroundClickListener(OnBackgroundClickListener backgroundClickListener) {
        this.backgroundClickListener = backgroundClickListener;
        return this;
    }

    // EVAN_REFACTOR: 2019-08-31 背景點擊事件
    public XRefreshLayout setOnScrollerListener(OnScrollerListener onScrollerListener) {
        this.onScrollerListener = onScrollerListener;
        return this;
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }


    public interface OnRefreshListener {
        void onRefresh();
    }

    // EVAN_REFACTOR: 2019-08-31 背景點擊事件監聽器
    public interface OnBackgroundClickListener {
        void onBackgroundClick(XRefreshLayout refreshLayout);
    }

    // EVAN_FLAG 2019-09-03 全局滑動監聽事件監聽器
    public interface OnScrollerListener {
        void onScroller(XRefreshLayout refreshLayout, OverScroller scroller);
    }

}
