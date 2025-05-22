package tw.com.chainsea.custom.view.recyclerview.itemdecoration;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * current by evan on 2020-04-01
 *
 * @author Evan Wang
 * @date 2020-04-01
 */
public class DividerItemDecorationWithoutLastItem extends RecyclerView.ItemDecoration {
    private static final int DEFAULT_DIVIDER_HEIGHT = 1;

    public static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;

    public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;

    protected int mOrientation;
    protected int dividerHeight;
    protected Context mContext;
    protected Paint mPaddingPaint;
    protected Paint mDividerPaint;

    public DividerItemDecorationWithoutLastItem(Context context) {
        this(context, VERTICAL_LIST, -1, -1);
    }

    public DividerItemDecorationWithoutLastItem(Context context, int orientation) {
        this(context, orientation, -1, -1);
    }

    public DividerItemDecorationWithoutLastItem(Context context, int orientation, int padding, int dividerHeight) {
        setOrientation(orientation);
        mContext = context;

        init();
        if (dividerHeight != -1) this.dividerHeight = dividerHeight;
    }

    public DividerItemDecorationWithoutLastItem(Context context, int orientation, int startpadding, int endpadding, int dividerHeight) {
        setOrientation(orientation);
        mContext = context;

        init();
        if (dividerHeight != -1) this.dividerHeight = dividerHeight;
    }

    private void init() {
        dividerHeight = DEFAULT_DIVIDER_HEIGHT;

        mPaddingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaddingPaint.setColor(ContextCompat.getColor(mContext, android.R.color.white));
        mPaddingPaint.setStyle(Paint.Style.FILL);

        mDividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDividerPaint.setColor(ContextCompat.getColor(mContext, android.R.color.darker_gray));
        mDividerPaint.setStyle(Paint.Style.FILL);
    }

    public void setOrientation(int orientation) {
        if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST) {
            throw new IllegalArgumentException("invalid orientation");
        }
        mOrientation = orientation;
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDraw(c, parent, state);
        if (mOrientation == VERTICAL_LIST) {
            drawVertical(c, parent);
        } else {
            drawHorizontal(c, parent);
        }
    }

    public void drawVertical(Canvas c, RecyclerView parent) {
        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount - 1; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int top = child.getBottom() + params.bottomMargin +
                Math.round(ViewCompat.getTranslationY(child));
            final int bottom = top + dividerHeight;

            c.drawRect(left, top, left, bottom, mPaddingPaint);
            c.drawRect(right, top, right, bottom, mPaddingPaint);
            c.drawRect(left, top, right, bottom, mDividerPaint);
        }
    }

    public void drawHorizontal(Canvas c, RecyclerView parent) {
        final int top = parent.getPaddingTop();
        final int bottom = parent.getHeight() - parent.getPaddingBottom();

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount - 1; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int left = child.getRight() + params.rightMargin +
                Math.round(ViewCompat.getTranslationX(child));
            final int right = left + dividerHeight;
            c.drawRect(left, top, right, top, mPaddingPaint);
            c.drawRect(left, bottom, right, bottom, mPaddingPaint);
            c.drawRect(left, top, right, bottom, mDividerPaint);
        }
    }
}
