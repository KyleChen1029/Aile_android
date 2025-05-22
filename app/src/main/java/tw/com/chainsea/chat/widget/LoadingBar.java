package tw.com.chainsea.chat.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.ColorInt;
import androidx.core.content.res.ResourcesCompat;

import java.util.Objects;

import tw.com.chainsea.android.common.image.BitmapHelper;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.messagekit.main.viewholder.FileMessageView;

public class LoadingBar extends ProgressBar implements View.OnClickListener {
    private static final String TAG = LoadingBar.class.getSimpleName();

    private Paint mPaint;
    private Mode mMode;
    private int mTextColor;
    private int mTextSize;
    private int mTextMargin;
    private int mReachedColor;
    private int mReachedHeight;
    private int mUnReachedColor;
    private int mUnReachedHeight;
    private boolean mIsCapRounded;
    private boolean mIsHiddenText;

    private int mRadius;

    private int mMaxUnReachedEndX;
    private int mMaxStrokeWidth;

    private int mTextHeight;
    private int mTextWidth;

    private RectF mArcRectF;
    private Rect mTextRect = new Rect();

    private String mText;

    private FileMessageView.OnFileClickListener onFileClickListener;

    public boolean isCanceledLoading = false;

    public void setOnFileClickListener(FileMessageView.OnFileClickListener mFileClickListener) {
        onFileClickListener = mFileClickListener;
    }

    public LoadingBar(Context context) {
        this(context, null);
    }

    public LoadingBar(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.progressBarStyle);
    }

    public LoadingBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initDefaultAttrs(context);
        initCustomAttrs(context, attrs);

        mMaxStrokeWidth = Math.max(mReachedHeight, mUnReachedHeight);
        isCanceledLoading = false;
    }

    private void initDefaultAttrs(Context context) {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        mMode = Mode.System;
        mTextColor = Color.parseColor("#70A800");
        mTextSize = LoadingBar.sp2px(context, 10);
        mTextMargin = LoadingBar.dp2px(context, 4);
        mReachedColor = Color.parseColor("#FFFFFF");
        mReachedHeight = LoadingBar.dp2px(context, 2);
        mUnReachedColor = Color.parseColor("#CCCCCC");
        mUnReachedHeight = LoadingBar.dp2px(context, 1);
        mIsCapRounded = false;
        mIsHiddenText = false;

        mRadius = LoadingBar.dp2px(context, 16);
    }

    private void initCustomAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LoadingBar);
        final int N = typedArray.getIndexCount();
        for (int i = 0; i < N; i++) {
            initAttr(typedArray.getIndex(i), typedArray);
        }
        typedArray.recycle();
    }

    protected void initAttr(int attr, TypedArray typedArray) {
        if (attr == R.styleable.LoadingBar_loading_bar_mode) {
            int ordinal = typedArray.getInt(attr, Mode.System.ordinal());
            mMode = Mode.values()[ordinal];
        } else if (attr == R.styleable.LoadingBar_loading_bar_textColor) {
            mTextColor = typedArray.getColor(attr, mTextColor);
        } else if (attr == R.styleable.LoadingBar_loading_bar_textSize) {
            mTextSize = typedArray.getDimensionPixelOffset(attr, mTextSize);
        } else if (attr == R.styleable.LoadingBar_loading_bar_textMargin) {
            mTextMargin = typedArray.getDimensionPixelOffset(attr, mTextMargin);
        } else if (attr == R.styleable.LoadingBar_loading_bar_reachedColor) {
            mReachedColor = typedArray.getColor(attr, mReachedColor);
        } else if (attr == R.styleable.LoadingBar_loading_bar_reachedHeight) {
            mReachedHeight = typedArray.getDimensionPixelOffset(attr, mReachedHeight);
        } else if (attr == R.styleable.LoadingBar_loading_bar_unReachedColor) {
            mUnReachedColor = typedArray.getColor(attr, mUnReachedColor);
        } else if (attr == R.styleable.LoadingBar_loading_bar_unReachedHeight) {
            mUnReachedHeight = typedArray.getDimensionPixelOffset(attr, mUnReachedHeight);
        } else if (attr == R.styleable.LoadingBar_loading_bar_isCapRounded) {
            mIsCapRounded = typedArray.getBoolean(attr, mIsCapRounded);
            if (mIsCapRounded) {
                mPaint.setStrokeCap(Paint.Cap.ROUND);
            }
        } else if (attr == R.styleable.LoadingBar_loading_bar_isHiddenText) {
            mIsHiddenText = typedArray.getBoolean(attr, mIsHiddenText);
        } else if (attr == R.styleable.LoadingBar_loading_bar_radius) {
            mRadius = typedArray.getDimensionPixelOffset(attr, mRadius);
        }
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setOnClickListener(this);
        if (mMode == Mode.System) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else if (mMode == Mode.Horizontal) {
            calculateTextWidthAndHeight();

            int width = MeasureSpec.getSize(widthMeasureSpec);

            int expectHeight = getPaddingTop() + getPaddingBottom();
            if (mIsHiddenText) {
                expectHeight += Math.max(mReachedHeight, mUnReachedHeight);
            } else {
                expectHeight += Math.max(mTextHeight, Math.max(mReachedHeight, mUnReachedHeight));
            }
            int height = resolveSize(expectHeight, heightMeasureSpec);
            setMeasuredDimension(width, height);

            mMaxUnReachedEndX = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        } else if (mMode == Mode.Circle) {
            int expectSize = mRadius * 2 + mMaxStrokeWidth + getPaddingLeft() + getPaddingRight();
            int width = resolveSize(expectSize, widthMeasureSpec);
            int height = resolveSize(expectSize, heightMeasureSpec);
            expectSize = Math.min(width, height);

            mRadius = (expectSize - getPaddingLeft() - getPaddingRight() - mMaxStrokeWidth) / 2;
            if (mArcRectF == null) {
                mArcRectF = new RectF();
            }
            mArcRectF.set(0, 0, mRadius * 2, mRadius * 2);

            setMeasuredDimension(expectSize, expectSize);
        } else if (mMode == Mode.Comet) {
            // TODO
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else if (mMode == Mode.Wave) {
            // TODO
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        if (mMode == Mode.System) {
            super.onDraw(canvas);
        } else if (mMode == Mode.Horizontal) {
            onDrawHorizontal(canvas);
        } else if (mMode == Mode.Circle) {
            if (!isCanceledLoading)
                onDrawCircle(canvas);
        } else if (mMode == Mode.Comet) {
            // TODO
            super.onDraw(canvas);
        } else if (mMode == Mode.Wave) {
            // TODO
            super.onDraw(canvas);
        }
    }

    private void onDrawHorizontal(Canvas canvas) {
        canvas.save();
        canvas.translate(getPaddingLeft(), getMeasuredHeight() / 2f);

        float reachedRatio = getProgress() * 1.0f / getMax();
        float reachedEndX = reachedRatio * mMaxUnReachedEndX;

        if (mIsHiddenText) {
            if (reachedEndX > mMaxUnReachedEndX) {
                reachedEndX = mMaxUnReachedEndX;
            }
            if (reachedEndX > 0) {
                mPaint.setColor(mReachedColor);
                mPaint.setStrokeWidth(mReachedHeight);
                mPaint.setStyle(Paint.Style.STROKE);
                canvas.drawLine(0, 0, reachedEndX, 0, mPaint);
            }

            float unReachedStartX = reachedEndX;
            if (mIsCapRounded) {
                unReachedStartX += (mReachedHeight + mUnReachedHeight) * 1.0f / 2;
            }
            if (unReachedStartX < mMaxUnReachedEndX) {
                mPaint.setColor(mUnReachedColor);
                mPaint.setStrokeWidth(mUnReachedHeight);
                mPaint.setStyle(Paint.Style.STROKE);
                canvas.drawLine(unReachedStartX, 0, mMaxUnReachedEndX, 0, mPaint);
            }
        } else {
            calculateTextWidthAndHeight();
            int maxReachedEndX = mMaxUnReachedEndX - mTextWidth - mTextMargin;
            if (reachedEndX > maxReachedEndX) {
                reachedEndX = maxReachedEndX;
            }
            if (reachedEndX > 0) {
                mPaint.setColor(mReachedColor);
                mPaint.setStrokeWidth(mReachedHeight);
                mPaint.setStyle(Paint.Style.STROKE);

                canvas.drawLine(0, 0, reachedEndX, 0, mPaint);
            }

            mPaint.setTextAlign(Paint.Align.LEFT);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mTextColor);
            float textStartX = reachedEndX > 0 ? reachedEndX + mTextMargin : reachedEndX;
            canvas.drawText(mText, textStartX, mTextHeight / 2f, mPaint);

            float unReachedStartX = textStartX + mTextWidth + mTextMargin;
            if (unReachedStartX < mMaxUnReachedEndX) {
                mPaint.setColor(mUnReachedColor);
                mPaint.setStrokeWidth(mUnReachedHeight);
                mPaint.setStyle(Paint.Style.STROKE);
                canvas.drawLine(unReachedStartX, 0, mMaxUnReachedEndX, 0, mPaint);
            }
        }

        canvas.restore();
    }

    private void onDrawCircle(Canvas canvas) {
        canvas.save();
        canvas.translate(getPaddingLeft() + mMaxStrokeWidth / 2f, getPaddingTop() + mMaxStrokeWidth / 2f);
        //the circle of background
//        mPaint.setStyle(Paint.Style.STROKE);
//        mPaint.setColor(mUnReachedColor);
//        mPaint.setStrokeWidth(mUnReachedHeight);
//        canvas.drawCircle(mRadius, mRadius, mRadius, mPaint);

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mReachedColor);
        mPaint.setStrokeWidth(mReachedHeight);
        float sweepAngle = getProgress() * 1.0f / getMax() * 360;
        canvas.drawArc(mArcRectF, 0, sweepAngle, false, mPaint);

        if (!mIsHiddenText) {
            Bitmap bitmap = BitmapHelper.drawableToBitmap(Objects.requireNonNull(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_file_cancel, null)));
            float bitmapX = mArcRectF.centerX() - (float) bitmap.getWidth() / 2;
            float bitmapY = mArcRectF.centerY() - (float) bitmap.getHeight() / 2;
            canvas.drawBitmap(bitmap, bitmapX, bitmapY, null);
        }

        canvas.restore();
    }

    public void setCometColors(@ColorInt int reachedColor, @ColorInt int unReachedColor) {
        this.mReachedColor = reachedColor;
        this.mUnReachedColor = unReachedColor;
//        nvalidateI() 或者postInvalidate()
        postInvalidate();
    }

    @SuppressLint("DefaultLocale")
    private void calculateTextWidthAndHeight() {
        //fix by Michael 修改参数溢出问题。
        //mText = String.format("%d", getProgress() * 100 / getMax()) + "%";
        mText = String.format("%d", (int) (getProgress() * 1.0f / getMax() * 100)) + "%";
        mPaint.setTextSize(mTextSize);
        mPaint.setStyle(Paint.Style.FILL);

        mPaint.getTextBounds(mText, 0, mText.length(), mTextRect);
        mTextWidth = mTextRect.width();
        mTextHeight = mTextRect.height();
    }

    @Override
    public void onClick(View v) {
        if (onFileClickListener != null) {
            isCanceledLoading = true;
            onFileClickListener.performClick();
        }
    }

    private enum Mode {
        System,
        Horizontal,
        Circle,
        Comet,
        Wave
    }

    public static int dp2px(Context context, float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
    }

    public static int sp2px(Context context, float spValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, context.getResources().getDisplayMetrics());
    }
}
