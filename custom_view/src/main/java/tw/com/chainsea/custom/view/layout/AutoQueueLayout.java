package tw.com.chainsea.custom.view.layout;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AutoQueueLayout extends ViewGroup {

    private static final int PADDING_HOR = 8;//子view水平方向padding
    private static final int PADDING_VERTICAL = 6;//子view垂直方向padding
    private static final int TEXT_MARGIN_HOR = 10;//子view之间的水平间距
    private static final int TEXT_MARGIN_VERTICAL = 10;//行间距

    private int num = 0;//最多字个数

    /**
     * @param context
     */
    public AutoQueueLayout(Context context) {
        super(context);
    }

    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
    public AutoQueueLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    /**
     * @param context
     * @param attrs
     */
    public AutoQueueLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    //    @Override
//    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//        int childCount = getChildCount();
//        int autualWidth = r - l;//当前容器宽度
//        int x = 0;// 横坐标开始
//        int y = 0;//纵坐标开始
//        int rows = 1;
//        for (int i = 0; i < childCount; i++) {
//            View view = getChildAt(i);
//            int width = view.getMeasuredWidth();
//            int height = view.getMeasuredHeight();
//            x += width + TEXT_MARGIN_HOR;
//            if (x > autualWidth - TEXT_MARGIN_HOR) {//判断累积高度
//                if (i != 0) {
//                    x = width + TEXT_MARGIN_HOR;
//                    rows++;
//                }
//
//                //当一个子view长度超出父view长度时
//                if (x > autualWidth - TEXT_MARGIN_HOR) {//判断单个高度
//                    //如果子view是textview的话处理文字
//                    if (view instanceof TextView) {
//                        TextView tv = ((TextView) view);
//                        if (num == 0) {//只计算一次
//                            int wordNum = tv.getText().toString().length();
//                            num = wordNum * (autualWidth - 2 * TEXT_MARGIN_HOR) / width - 1;
//                        }
//                        String text = tv.getText().toString();
//                        text = text.substring(0, num) + "...";
//                        tv.setText(text);
//                    }
//
//                    x = autualWidth - TEXT_MARGIN_HOR;
//                    width = autualWidth - (2 * TEXT_MARGIN_HOR);
//                }
//            }
//            y = rows * (height + TEXT_MARGIN_VERTICAL);
//            view.layout(x - width, y - height, x, y);
//
//        }
//    }
    private final LayoutContext context = new LayoutContext();

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        context.reset(r - l);
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            layoutChild(view, context);
        }
    }

    private static class LayoutContext {
        int containerWidth;
        int x = 0;
        int y = 0;
        int rows = 1;
        int num = 0;

        LayoutContext() {
        }

        void reset(int containerWidth) {
            this.containerWidth = containerWidth;
            this.x = 0;
            this.y = 0;
            this.rows = 1;
        }
    }

    private void layoutChild(View view, LayoutContext ctx) {
        int width = view.getMeasuredWidth();
        int height = view.getMeasuredHeight();

        // 更新 x 坐標
        ctx.x += width + TEXT_MARGIN_HOR;

        // 檢查是否需要換行
        if (needsNewRow(ctx.x, ctx.containerWidth)) {
            if (ctx.x != width + TEXT_MARGIN_HOR) { // 不是第一個元素
                ctx.x = width + TEXT_MARGIN_HOR;
                ctx.rows++;
            }

            // 處理超寬視圖
            if (isViewTooWide(ctx.x, ctx.containerWidth)) {
                handleOversizedView(view, ctx);
                width = ctx.containerWidth - (2 * TEXT_MARGIN_HOR);
                ctx.x = ctx.containerWidth - TEXT_MARGIN_HOR;
            }
        }

        // 計算 y 坐標
        ctx.y = ctx.rows * (height + TEXT_MARGIN_VERTICAL);

        // 佈局子視圖
        view.layout(ctx.x - width, ctx.y - height, ctx.x, ctx.y);
    }

    private boolean needsNewRow(int x, int containerWidth) {
        return x > containerWidth - TEXT_MARGIN_HOR;
    }

    private boolean isViewTooWide(int x, int containerWidth) {
        return x > containerWidth - TEXT_MARGIN_HOR;
    }

    private void handleOversizedView(View view, LayoutContext ctx) {
        if (!(view instanceof TextView)) {
            return;
        }

        TextView tv = (TextView) view;
        if (ctx.num == 0) {
            int wordNum = tv.getText().toString().length();
            int viewWidth = view.getMeasuredWidth();
            ctx.num = wordNum * (ctx.containerWidth - 2 * TEXT_MARGIN_HOR) / viewWidth - 1;
        }

        String text = tv.getText().toString();
        text = text.substring(0, ctx.num) + "...";
        tv.setText(text);
    }

    public float getCharacterWidth(String text, float size) {
        if (null == text || text.isEmpty())
            return 0;
        float width = 0;
        Paint paint = new Paint();
        paint.setTextSize(size);
        float text_width = paint.measureText(text);// 得到总体长度
        width = text_width / text.length();// 每一个字符的长度

        return width;
    }

    //    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        int x = 0;//横坐标
//        int y = 0;//纵坐标
//        int rows = 1;//总行数
//        int actualWidth = MeasureSpec.getSize(widthMeasureSpec);//实际宽度
//        int childCount = getChildCount();
//        for (int index = 0; index < childCount; index++) {
//            View child = getChildAt(index);
//            child.setPadding(PADDING_HOR, PADDING_VERTICAL, PADDING_HOR, PADDING_VERTICAL);
//            child.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
//            int width = child.getMeasuredWidth();
//            int height = child.getMeasuredHeight();
//            x += width + TEXT_MARGIN_HOR;
//            if (x > actualWidth - TEXT_MARGIN_HOR) {//换行
//                if (index != 0) {
//                    x = width + TEXT_MARGIN_HOR;
//                    rows++;
//                }
//            }
//            y = rows * (height + TEXT_MARGIN_VERTICAL);
//        }
//        setMeasuredDimension(actualWidth, y + TEXT_MARGIN_VERTICAL);
//    }
// 類成員變數
    private final MeasureContext measureContext = new MeasureContext();

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int actualWidth = MeasureSpec.getSize(widthMeasureSpec);
        measureContext.reset(actualWidth);

        measureChildren(measureContext);

        setMeasuredDimension(actualWidth, measureContext.y + TEXT_MARGIN_VERTICAL);
    }

    private static class MeasureContext {
        int containerWidth;
        int x = 0;
        int y = 0;
        int rows = 1;

        MeasureContext() {
        }

        void reset(int containerWidth) {
            this.containerWidth = containerWidth;
            this.x = 0;
            this.y = 0;
            this.rows = 1;
        }
    }

    private void measureChildren(MeasureContext ctx) {
        int childCount = getChildCount();

        for (int index = 0; index < childCount; index++) {
            View child = getChildAt(index);
            setupChildAndMeasure(child);

            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();

            // 更新水平位置
            ctx.x += width + TEXT_MARGIN_HOR;

            // 檢查是否需要換行
            if (needsNewRow(ctx.x, ctx.containerWidth, index)) {
                ctx.x = width + TEXT_MARGIN_HOR;
                ctx.rows++;
            }

            // 更新垂直位置
            ctx.y = ctx.rows * (height + TEXT_MARGIN_VERTICAL);
        }
    }

    private void setupChildAndMeasure(View child) {
        child.setPadding(PADDING_HOR, PADDING_VERTICAL, PADDING_HOR, PADDING_VERTICAL);
        child.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
    }

    private boolean needsNewRow(int x, int containerWidth, int index) {
        return x > containerWidth - TEXT_MARGIN_HOR && index != 0;
    }
}

