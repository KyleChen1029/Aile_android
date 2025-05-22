package tw.com.chainsea.chat.messagekit.main.viewholder.listener;

import android.content.Context;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

import tw.com.chainsea.chat.util.VibratorKit;

public abstract class OnMessageItemEvanListener<T> implements View.OnTouchListener {
    private T t;
    private Context ctx;
    private View root;

    private boolean isFeedback = false;

    private int clickCount; // 点击次数
    private float downX;
    private float downY;
    private float moveX;
    private float moveY;
    private float upX;
    private float upY;

    private long lastDownTime;
    private long lastUpTime;
    private long firstClick;
    private long secondClick;

    private boolean isDoubleClick = false;
    private int MAX_LONG_PRESS_TIME = 500;// 长按/双击最长等待时间
    private int MAX_SINGLE_CLICK_TIME = 200;// 单击最长等待时间
    private int MAX_MOVE_FOR_CLICK = 20;// 最长改变距离,超过则算移动

    private final Handler eventHandler = new Handler();

    private final Runnable longPressTask = new Runnable() {
        @Override
        public void run() {
            // 處理長按
            clickCount = 0;
            VibratorKit.longClick();
            onLongClick(root, t);
        }
    };

    private final Runnable singleClickTask = new Runnable() {
        @Override
        public void run() {
            // 處理單擊
            clickCount = 0;
            onClick(root, t);
        }
    };


    public OnMessageItemEvanListener(Context ctx, boolean isFeedback, T t) {
        this.ctx = ctx;
        this.isFeedback = isFeedback;
        this.t = t;
    }

    public abstract void onClick(View v, T t);

    // 雙點事件
    public abstract void onDoubleClick(View v, T t);

    // 長按事件
    public abstract void onLongClick(View v, T t);

    // 設定反饋
    public void setFeedback(boolean isFeedback) {
        this.isFeedback = isFeedback;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        this.root = v;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                eventHandler.removeCallbacks(longPressTask);
                lastDownTime = System.currentTimeMillis();
                downX = event.getX();
                downY = event.getY();
                clickCount++;
                if (singleClickTask != null) {
                    eventHandler.removeCallbacks(singleClickTask);
                }
                if (!isDoubleClick)
                    eventHandler.postDelayed(longPressTask, MAX_LONG_PRESS_TIME);
                if (1 == clickCount) {
                    firstClick = System.currentTimeMillis();
                } else if (clickCount == 2) { // 雙擊
                    secondClick = System.currentTimeMillis();
                    if (secondClick - firstClick <= MAX_LONG_PRESS_TIME) {
                        //處理雙擊
                        VibratorKit.doubleClick();
                        onDoubleClick(v, this.t);
                        isDoubleClick = true;
                        clickCount = 0;
                        firstClick = 0;
                        secondClick = 0;
                        eventHandler.removeCallbacks(singleClickTask);
                        eventHandler.removeCallbacks(longPressTask);
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                // 如果是 Scroll View 在滑動時
                eventHandler.removeCallbacks(singleClickTask);
                eventHandler.removeCallbacks(longPressTask);
                break;
            case MotionEvent.ACTION_MOVE:
                moveX = event.getX();
                moveY = event.getY();
                float absMx = Math.abs(moveX - downX);
                float absMy = Math.abs(moveY - downY);

                if (absMy > MAX_MOVE_FOR_CLICK) {
                    eventHandler.removeCallbacks(longPressTask);
                    eventHandler.removeCallbacks(singleClickTask);
                    isDoubleClick = false;
                    clickCount = 0; // 移動了
                }

//                if (absMy > MAX_MOVE_FOR_CLICK || absMx > MAX_MOVE_FOR_CLICK) {
//                    eventHandler.removeCallbacks(longPressTask);
//                    eventHandler.removeCallbacks(singleClickTask);
//                    isDoubleClick = false;
//                    clickCount = 0; // 移動了
//                }
//                if (absMy >= 5) {
//                    float mX = event.getX();
//                    if (downX > mX) {
////                        rightMenu.setAlpha(1.0f);
//                    } else if (downX < mX) {
////                        leftMenu.setAlpha(1.0f);
//                    }
//                    // 處理移動
//                    eventHandler.removeCallbacks(longPressTask);
//                    eventHandler.removeCallbacks(singleClickTask);
//                    isDoubleClick = false;
//                    clickCount = 0; // 移動了
//                }
                break;
            case MotionEvent.ACTION_UP:
                lastUpTime = System.currentTimeMillis();
                upX = event.getX();
                upY = event.getY();
                float mx = Math.abs(upX - downX);
                float my = Math.abs(upY - downY);
                if (my <= MAX_MOVE_FOR_CLICK) {
                    if ((lastUpTime - lastDownTime) <= MAX_LONG_PRESS_TIME) {
                        eventHandler.removeCallbacks(longPressTask);
                        if (!isDoubleClick)
                            eventHandler.postDelayed(singleClickTask, MAX_SINGLE_CLICK_TIME);
                    } else {
                        // 超出雙擊時間區間
                        clickCount = 0;
                    }
                }
//                if (mx <= MAX_MOVE_FOR_CLICK && my <= MAX_MOVE_FOR_CLICK) {
//                    if ((lastUpTime - lastDownTime) <= MAX_LONG_PRESS_TIME) {
//                        eventHandler.removeCallbacks(longPressTask);
//                        if (!isDoubleClick)
//                            eventHandler.postDelayed(singleClickTask, MAX_SINGLE_CLICK_TIME);
//                    } else {
//                        // 超出雙擊時間區間
//                        clickCount = 0;
//                    }
//                }
                else {
                    eventHandler.removeCallbacks(longPressTask);
                    // 移動了
                    clickCount = 0;
//                    int scrollX = getScrollX();
//                    if (downX > upX) { // 左邊菜單打開
//                        smoothScrollTo(Math.abs(scrollX / 3) > screenWidth / 3, menuWidth + (int) (menuWidth * 0.4f));
//                        if (onSlidingListener != null) {
//                            onSlidingListener.onLeftMenuOpen(SlidingItem.this);
//                        }
//                    } else if (downX < upX) { // 右邊菜單打開
//                        smoothScrollTo(Math.abs(scrollX / 3) < screenWidth / 3, (int) (menuWidth * 0.6f));
//                        if (onSlidingListener != null) {
//                            onSlidingListener.onRightMenuOpen(SlidingItem.this);
//                        }
//                    }
                    downX = 0.0f;
                    return true;
                }
                if (isDoubleClick) isDoubleClick = false;
                break;
        }
        return true;
    }
}
