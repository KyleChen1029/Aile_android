package tw.com.chainsea.custom.view.image;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;

import tw.com.chainsea.custom.view.R;

/**
 * current by evan on 2019-10-30
 */
public class RoundImageView extends androidx.appcompat.widget.AppCompatImageView {
    private static final String TAG = "RoundImageView.class";

    /**
     * 圖片的型別，圓形or圓角
     */
    private int type;
    public static final int TYPE_CIRCLE = 0;
    public static final int TYPE_ROUND = 1;

    /**
     * 圓角大小的預設值
     */
    private static final int BODER_RADIUS_DEFAULT = 10;
    /**
     * 圓角的大小
     */
    private int mBorderRadiusAll;

    /**
     * 上、左、下、右
     */
    private int mBorderRadiusTop;
    private int mBorderRadiusLeft;
    private int mBorderRadiusBottom;
    private int mBorderRadiusRight;


    /**
     * 繪圖的Paint
     */
    private Paint mBitmapPaint;
    /**
     * 圓角的半徑
     */
    private int mRadius;
    /**
     * 3x3 矩陣，主要用於縮小放大
     */
    private Matrix mMatrix;
    /**
     * 渲染影象，使用影象為繪製圖形著色
     */
    private BitmapShader mBitmapShader;
    /**
     * view的寬度
     */
    private int mWidth;
    private RectF mRoundRect;

    /**
     * 建構函式:獲取自定義屬性
     */
    public RoundImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mMatrix = new Matrix();
        mBitmapPaint = new Paint();
        mBitmapPaint.setAntiAlias(true);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundImageView);
        mBorderRadiusAll = a.getDimensionPixelSize(R.styleable.RoundImageView_borderRadiusAll, (int) TypedValue
            .applyDimension(TypedValue.COMPLEX_UNIT_DIP, BODER_RADIUS_DEFAULT, getResources().getDisplayMetrics()));// 預設為10dp

        mBorderRadiusTop = a.getDimensionPixelSize(R.styleable.RoundImageView_borderRadiusTop, (int) TypedValue
            .applyDimension(TypedValue.COMPLEX_UNIT_DIP, mBorderRadiusTop, getResources().getDisplayMetrics()));// 預設為10dp
        mBorderRadiusLeft = a.getDimensionPixelSize(R.styleable.RoundImageView_borderRadiusLeft, (int) TypedValue
            .applyDimension(TypedValue.COMPLEX_UNIT_DIP, mBorderRadiusLeft, getResources().getDisplayMetrics()));// 預設為10dp
        mBorderRadiusBottom = a.getDimensionPixelSize(R.styleable.RoundImageView_borderRadiusBottom, (int) TypedValue
            .applyDimension(TypedValue.COMPLEX_UNIT_DIP, mBorderRadiusBottom, getResources().getDisplayMetrics()));// 預設為10dp
        mBorderRadiusRight = a.getDimensionPixelSize(R.styleable.RoundImageView_borderRadiusRight, (int) TypedValue
            .applyDimension(TypedValue.COMPLEX_UNIT_DIP, mBorderRadiusRight, getResources().getDisplayMetrics()));// 預設為10dp


        type = a.getInt(R.styleable.RoundImageView_type, TYPE_CIRCLE);// 預設為Circle
        a.recycle();

        // 圓角圖片的範圍
        if (type == TYPE_ROUND)
            mRoundRect = new RectF(0, 0, getWidth(), getHeight());
    }

    /**
     * 關於view的寬高:主要用於當設定型別為圓形時，我們強制讓view的寬和高一致
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        Log.e("TAG", "onMeasure");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        /**
         * 如果型別是圓形，則強制改變view的寬高一致，以小值為準;如果是圓角則不用管寬高問題
         */
        if (type == TYPE_CIRCLE) {
            mWidth = Math.min(getMeasuredWidth(), getMeasuredHeight());
            mRadius = mWidth / 2;
            setMeasuredDimension(mWidth, mWidth);
        }

    }

    /**
     * 1. 設定BitmapShader
     * 2. 繪製
     */
    @Override
    protected void onDraw(Canvas canvas) {
        if (getDrawable() == null) {
            return;
        }
        setUpShader();

        if (type == TYPE_ROUND) {
            canvas.drawRoundRect(mRoundRect, mBorderRadiusAll, mBorderRadiusAll, mBitmapPaint);
        } else {
            canvas.drawCircle(mRadius, mRadius, mRadius, mBitmapPaint);
            // drawSomeThing(canvas);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 圓角圖片的範圍
        if (type == TYPE_ROUND)
            mRoundRect = new RectF(0, 0, getWidth(), getHeight());
    }

    /**
     * 初始化BitmapShader//給paint附加一個具有某種魔力(縮放)的shader
     * 1. drawable轉化為我們的bitmap
     * 2. 設定shader 的mode
     * 3. 設定scale: 因為最終縮放完成的圖片一定要大於我們的view的區域
     * 4. scale設定給matrix
     * 5. matrix設定給shader
     * 6. shader設定給paint
     */
    private void setUpShader() {
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }

        try {
            Bitmap bmp = drawableToBitamp(drawable);
            // 將bmp作為著色器，就是在指定區域內繪製bmp;此構造方法用的是拉伸mode: clamp
            mBitmapShader = new BitmapShader(bmp, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            float scale = 1.0f;
            if (type == TYPE_CIRCLE) {
                // 拿到bitmap寬或高的小值//我們需要畫正方形
                int bSize = Math.min(bmp.getWidth(), bmp.getHeight());
                scale = mWidth * 1.0f / bSize;

            } else if (type == TYPE_ROUND) {
                // 如果圖片的寬或者高與view的寬高不匹配，計算出需要縮放的比例；縮放後的圖片的寬高，一定要大於我們view的寬高；所以我們這裡取大值；//view的寬度/bitmap的寬度=縮放倍數
                scale = Math.max(getWidth() * 1.0f / bmp.getWidth(), getHeight()
                    * 1.0f / bmp.getHeight());
            }
            // shader的變換矩陣，我們這裡主要用於放大或者縮小
            mMatrix.setScale(scale, scale);
            // 設定變換矩陣
            mBitmapShader.setLocalMatrix(mMatrix);
            // 先給paint設定shader;然後畫筆會有所不同哦,例如
            mBitmapPaint.setShader(mBitmapShader);
        } catch (Exception e) {

        }

    }

    /**
     * drawable轉bitmap
     *
     * @param drawable
     * @return
     */
    private Bitmap drawableToBitamp(Drawable drawable) {

//        Log.i(TAG, "drawable: " + drawable.toString());
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bd = (BitmapDrawable) drawable;
            return bd.getBitmap();
        }


        if (drawable instanceof GradientDrawable) {
            GradientDrawable bd = (GradientDrawable) drawable;
//            Log.i(TAG, "~~~"+ bd.getIntrinsicWidth());
//            Log.i(TAG, "drawable: " + bd.toString());
//            return Bitmap.createBitmap(, )
        }

        //以下為drawable轉bitmap的通用方法
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
//        Log.i(TAG, "w: " + w + ", h: " + h);
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * 新增一個方法
     * bitmap轉drawable
     */
    private Drawable bitmapToDrawable(Bitmap bitmap) {
        Drawable drawable = new BitmapDrawable(null, bitmap);
        return drawable;
    }

    //狀態儲存與恢復
    private static final String STATE_INSTANCE = "state_instance";
    private static final String STATE_TYPE = "state_type";
    private static final String STATE_BORDER_RADIUS = "state_border_radius";

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(STATE_INSTANCE, super.onSaveInstanceState());
        bundle.putInt(STATE_TYPE, type);
        bundle.putInt(STATE_BORDER_RADIUS, mBorderRadiusAll);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            super.onRestoreInstanceState(((Bundle) state)
                .getParcelable(STATE_INSTANCE));
            this.type = bundle.getInt(STATE_TYPE);
            this.mBorderRadiusAll = bundle.getInt(STATE_BORDER_RADIUS);
        } else {
            super.onRestoreInstanceState(state);
        }

    }

    /**
     * 動態修改圓角大小
     */
    public void setBorderRadius(int borderRadius) {
        int pxVal = dp2px(borderRadius);
        if (this.mBorderRadiusAll != pxVal) {
            this.mBorderRadiusAll = pxVal;
            invalidate();
        }
    }

    /**
     * 動態設定type
     */
    public void setType(int type) {
        if (this.type != type) {
            this.type = type;
            if (this.type != TYPE_ROUND && this.type != TYPE_CIRCLE) {
                this.type = TYPE_CIRCLE;
            }
            requestLayout();
        }

    }

    private int dp2px(int dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            dpVal, getResources().getDisplayMetrics());
    }
}
