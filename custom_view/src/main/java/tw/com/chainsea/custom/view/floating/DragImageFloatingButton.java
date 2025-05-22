package tw.com.chainsea.custom.view.floating;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.DrawableRes;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

import tw.com.chainsea.custom.view.R;

/**
 * current by evan on 2020-01-30
 */
public class DragImageFloatingButton extends FloatingActionButton implements View.OnTouchListener, View.OnClickListener {
    private static final String TAG = DragImageFloatingButton.class.getSimpleName();
    private boolean showLog = false;

    private final static float CLICK_DRAG_TOLERANCE = 10; // Often, there will be a slight, unintentional, drag when the user taps the FAB, so we need to account for this.
    private boolean isMove = false;
    private float downRawX, downRawY;
    private float dX, dY;
    private int customSize;
    private int imageSize;

    @DrawableRes
    private int stillImageRes;
    @DrawableRes
    private int actionImageRes;

    private boolean useDrag = false;
    private float moveAlpha;
    private boolean useMoveAlpha = false;

    private OnFloatClickListener onFloatClickListener;

    public DragImageFloatingButton(Context ctx) {
        this(ctx, null);
    }

    public DragImageFloatingButton(Context ctx, AttributeSet attrs) {
        this(ctx, attrs, R.attr.DragImageFloatingButtonStyle);
    }

    public DragImageFloatingButton(Context ctx, AttributeSet attrs, int defStyleAttr) {
        super(ctx, attrs, defStyleAttr);
        setAttributeSet(ctx, attrs, defStyleAttr);
        init(ctx);
    }

    /**
     * 設定預設 屬性與自定義屬性
     *
     * @author Evan Wang
     * @since 0.0.1
     */
    @SuppressLint({"CustomViewStyleable", "PrivateResource"})
    private void setAttributeSet(Context ctx, AttributeSet attrs, int defStyleAttr) {
        // 自定義屬性
        TypedArray ca = ctx.obtainStyledAttributes(attrs, R.styleable.DragImageFloatingButton, defStyleAttr, 0);
        this.stillImageRes = ca.getResourceId(R.styleable.DragImageFloatingButton_stillRes, -1);
        this.actionImageRes = ca.getResourceId(R.styleable.DragImageFloatingButton_actionRes, -1);
        this.moveAlpha = ca.getFloat(R.styleable.DragImageFloatingButton_moveAlpha, 1.0f);
        this.useMoveAlpha = ca.getBoolean(R.styleable.DragImageFloatingButton_useMoveAlpha, false);
        this.useDrag = ca.getBoolean(R.styleable.DragImageFloatingButton_useDrag, false);

        // 元件預設屬性
        TypedArray da = ctx.obtainStyledAttributes(attrs, R.styleable.FloatingActionButton, defStyleAttr, 0);
        this.customSize = da.getDimensionPixelSize(R.styleable.FloatingActionButton_fabCustomSize, 0);
        this.imageSize = da.getDimensionPixelSize(R.styleable.FloatingActionButton_maxImageSize, 0);

        if (this.imageSize >= this.customSize || this.customSize == 0) {
            this.customSize = this.imageSize + 10;
        }

        if (this.stillImageRes > 0) {
            setImageDrawable(ResourcesCompat.getDrawable(ctx.getResources(), this.stillImageRes, null));
        }

        if (this.customSize > 0) {
            setCustomSize(this.customSize);
        }
        ca.recycle();
        da.recycle();
    }

    private void init(Context ctx) {
        try {
            Method method = Objects.requireNonNull(getClass().getSuperclass()).getDeclaredMethod("getSizeDimension");
            method.setAccessible(true);
            Object r = method.invoke(this);
            Field f = super.getClass().getSuperclass().getDeclaredField("maxImageSize");
            f.setAccessible(true);
            f.set(this, this.imageSize);
        } catch (Exception e) {

        }

        setStyles();
        setOnTouchListener(this.useDrag ? this : null);
        setOnClickListener(this);
    }


    /**
     * 設定成透明 浮動按鈕樣式
     *
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    private void setStyles() {
        this.setCompatElevation(0.0f);
        this.setAlpha(1.0f);
        this.setCompatPressedTranslationZ(0.0f);
        this.setBackgroundColor(Color.TRANSPARENT);
        this.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        this.setBackgroundDrawable(null);
        this.setBackground(null);

        this.setRippleColor(Color.TRANSPARENT);
    }

    /**
     * 設定是否可拖曳
     *
     * @param useDrag
     * @return this
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public DragImageFloatingButton setUseDrag(boolean useDrag) {
        this.useDrag = useDrag;
        setOnTouchListener(this.useDrag ? this : null);
        return this;
    }


    /**
     * 設定靜態顯示圖示
     *
     * @param stillImageRes
     * @return this
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public DragImageFloatingButton setStillImageRes(@DrawableRes int stillImageRes) {
        this.stillImageRes = stillImageRes;
        if (this.stillImageRes > 0) {
            setImageDrawable(ResourcesCompat.getDrawable(getContext().getResources(), this.stillImageRes, null));
        }
        return this;
    }

    /**
     * 設定拖曳中顯示圖示
     *
     * @param actionImageRes
     * @return this
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public DragImageFloatingButton setActionImageRes(@DrawableRes int actionImageRes) {
        this.actionImageRes = actionImageRes;
        return this;
    }


    /**
     * 設定拖曳中 Alpha 值
     *
     * @param moveAlpha
     * @return this
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public DragImageFloatingButton setMoveAlpha(float moveAlpha) {
        this.moveAlpha = moveAlpha;
        return this;
    }

    /**
     * 是否使用拖曳中使用 Alpha
     *
     * @param useMoveAlpha
     * @return this
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public DragImageFloatingButton setUseMoveAlpha(boolean useMoveAlpha) {
        this.useMoveAlpha = useMoveAlpha;
        return this;
    }

    /**
     * 設定點擊 + 拖曳事件監聽器
     *
     * @param onFloatClickListener
     * @return this
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public DragImageFloatingButton setOnFloatClickListener(OnFloatClickListener onFloatClickListener) {
        this.onFloatClickListener = onFloatClickListener;
        return this;
    }

    /**
     * 是否正在移動
     *
     * @return boolean
     * @author Evan Wang
     * @version 0.0.1
     * @since 0.0.1
     */
    public boolean isMove() {
        return isMove;
    }

    //    @Override
//    public boolean onTouch(View view, MotionEvent motionEvent) {
//
//        int action = motionEvent.getAction();
//        if (action == MotionEvent.ACTION_DOWN) {
//            downRawX = motionEvent.getRawX();
//            downRawY = motionEvent.getRawY();
//            dX = view.getX() - downRawX;
//            dY = view.getY() - downRawY;
//
//            view.setAlpha(this.useMoveAlpha ? this.moveAlpha : 1.0f);
//            if (this.actionImageRes > 0) {
//                setImageDrawable(ResourcesCompat.getDrawable(getContext().getResources(), this.actionImageRes, null));
//            }
//
//            return true; // Consumed
//
//        } else if (action == MotionEvent.ACTION_MOVE) {
//            this.isMove = true;
//            int viewWidth = view.getWidth();
//            int viewHeight = view.getHeight();
//
//            View viewParent = (View) view.getParent();
//            int parentWidth = viewParent.getWidth();
//            int parentHeight = viewParent.getHeight();
//
//            float newX = motionEvent.getRawX() + dX;
//            newX = Math.max(0, newX); // Don't allow the FAB past the left hand side of the parent
//            newX = Math.min(parentWidth - viewWidth, newX); // Don't allow the FAB past the right hand side of the parent
//
//            float newY = motionEvent.getRawY() + dY;
//            newY = Math.max(0, newY); // Don't allow the FAB past the top of the parent
//            newY = Math.min(parentHeight - viewHeight, newY); // Don't allow the FAB past the bottom of the parent
//            move(newX, newY);
//
//            if (this.onFloatClickListener != null) {
//                this.onFloatClickListener.onMove(this, motionEvent);
//            }
//
//            return true; // Consumed
//
//        } else if (action == MotionEvent.ACTION_UP) {
//            this.isMove = false;
//
//            view.setAlpha(1.0f);
//            if (this.stillImageRes > 0) {
//                setImageDrawable(ResourcesCompat.getDrawable(getContext().getResources(), this.stillImageRes, null));
//            }
//            float upRawX = motionEvent.getRawX();
//            float upRawY = motionEvent.getRawY();
//
//            float upDX = upRawX - downRawX;
//            float upDY = upRawY - downRawY;
//
//            if (this.onFloatClickListener != null) {
//                this.onFloatClickListener.onMoveUp(this, motionEvent);
//            }
//
//            if (Math.abs(upDX) < CLICK_DRAG_TOLERANCE && Math.abs(upDY) < CLICK_DRAG_TOLERANCE) { // A click
//                return performClick();
//            } else { // A drag
//                return true; // Consumed
//            }
//        } else {
//            return super.onTouchEvent(motionEvent);
//        }
//    }
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        return switch (action) {
            case MotionEvent.ACTION_DOWN -> handleActionDown(view, motionEvent);
            case MotionEvent.ACTION_MOVE -> handleActionMove(view, motionEvent);
            case MotionEvent.ACTION_UP -> handleActionUp(view, motionEvent);
            default -> super.onTouchEvent(motionEvent);
        };
    }

    private boolean handleActionDown(View view, MotionEvent event) {
        downRawX = event.getRawX();
        downRawY = event.getRawY();
        dX = view.getX() - downRawX;
        dY = view.getY() - downRawY;

        view.setAlpha(this.useMoveAlpha ? this.moveAlpha : 1.0f);
        updateImageIfNeeded(this.actionImageRes);

        return true; // Consumed
    }

    private boolean handleActionMove(View view, MotionEvent event) {
        this.isMove = true;

        float[] newCoordinates = calculateBoundedPosition(view, event);
        move(newCoordinates[0], newCoordinates[1]);

        notifyMoveListener(event);

        return true; // Consumed
    }

    private float[] calculateBoundedPosition(View view, MotionEvent event) {
        int viewWidth = view.getWidth();
        int viewHeight = view.getHeight();

        View viewParent = (View) view.getParent();
        int parentWidth = viewParent.getWidth();
        int parentHeight = viewParent.getHeight();

        float newX = event.getRawX() + dX;
        newX = Math.max(0, newX);
        newX = Math.min(parentWidth - viewWidth, newX);

        float newY = event.getRawY() + dY;
        newY = Math.max(0, newY);
        newY = Math.min(parentHeight - viewHeight, newY);

        return new float[]{newX, newY};
    }

    private boolean handleActionUp(View view, MotionEvent event) {
        this.isMove = false;

        view.setAlpha(1.0f);
        updateImageIfNeeded(this.stillImageRes);

        float upRawX = event.getRawX();
        float upRawY = event.getRawY();

        float upDX = upRawX - downRawX;
        float upDY = upRawY - downRawY;

        notifyMoveUpListener(event);

        if (isClick(upDX, upDY)) {
            return performClick();
        }

        return true; // Consumed as drag
    }

    private boolean isClick(float dX, float dY) {
        return Math.abs(dX) < CLICK_DRAG_TOLERANCE && Math.abs(dY) < CLICK_DRAG_TOLERANCE;
    }

    private void updateImageIfNeeded(int imageRes) {
        if (imageRes > 0) {
            setImageDrawable(ResourcesCompat.getDrawable(getContext().getResources(), imageRes, null));
        }
    }

    private void notifyMoveListener(MotionEvent event) {
        if (this.onFloatClickListener != null) {
            this.onFloatClickListener.onMove(this, event);
        }
    }

    private void notifyMoveUpListener(MotionEvent event) {
        if (this.onFloatClickListener != null) {
            this.onFloatClickListener.onMoveUp(this, event);
        }
    }


    public void move(float x, float y) {
        this.animate()
            .x(x)
            .y(y)
            .setDuration(0)
            .start();
    }

    @Override
    public void onClick(View v) {
        if (this.onFloatClickListener != null) {
            InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            onFloatClickListener.onClick(this, this.getTag());
        }
    }


    private void sendLog(String logMessage) {
        if (this.showLog) {
            Log.w(TAG, logMessage);
        }
    }
}
