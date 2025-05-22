package tw.com.chainsea.chat.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Px;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

/**
 * Created by sunhui on 2018/1/4.
 */

public class GridItemDecoration extends RecyclerView.ItemDecoration {
    Paint mVerPaint, mHorPaint;
    Builder mBuilder;

    private Paint mPaint = new Paint();


    public GridItemDecoration() {
//        init(builder);
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.LTGRAY);
    }

    public GridItemDecoration(int color) {
//        init(builder);
        mPaint.setAntiAlias(true);
        mPaint.setColor(color);
    }

    void init(Builder builder) {
        this.mBuilder = builder;
        mVerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mVerPaint.setStyle(Paint.Style.FILL);
        mVerPaint.setColor(builder.verColor);
        mHorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHorPaint.setStyle(Paint.Style.FILL);
        mHorPaint.setColor(builder.horColor);
    }

    private void drawHorizontal(Canvas c, RecyclerView parent) {

        int childCount = parent.getChildCount();
        int rowCount = childCount / getSpanCount(parent);
        if (rowCount <= 0) {
            return;
        }
        for (int i = 0; i < childCount; i += getSpanCount(parent)) {
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            //is draw head divider
            //version 1.1 update
            final int left = parent.getPaddingLeft();
            final int right = parent.getWidth() - parent.getPaddingRight();
            final int top = child.getBottom() + params.bottomMargin;
            final int bottom = top;
            c.drawLine(left, top, right, bottom, mPaint);
        }
    }

    private void drawVertical(Canvas c, RecyclerView parent) {
        final int childCount = parent.getChildCount();
        int spanCount = getSpanCount(parent);
        if (spanCount <= 0) {
            return;
        }
        for (int i = 0; i < childCount; i++) {
            if (i % (spanCount - 1) == 0 && i != 0) {
                continue;
            }
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int top = parent.getPaddingTop();
            final int bottom = parent.getHeight() - parent.getPaddingBottom();
            final int left = child.getRight() + params.rightMargin;
            final int right = left;

            c.drawLine(left, top, right, bottom, mPaint);
        }
    }

    private int getSpanCount(RecyclerView parent) {
        // 列数
        int spanCount = -1;
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {

            spanCount = ((GridLayoutManager) layoutManager).getSpanCount();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            spanCount = ((StaggeredGridLayoutManager) layoutManager)
                .getSpanCount();
        }
        return spanCount;
    }

    private boolean isLastRaw(RecyclerView parent, int pos, int spanCount,
                              int childCount) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            childCount = childCount - childCount % spanCount;
            return pos >= childCount;
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int orientation = ((StaggeredGridLayoutManager) layoutManager)
                .getOrientation();
            // StaggeredGridLayoutManager 且纵向滚动
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                childCount = childCount - childCount % spanCount;

                return pos >= childCount;
            } else
            // StaggeredGridLayoutManager 且横向滚动
            {

                return (pos + 1) % spanCount == 0;
            }
        }
        return false;
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDraw(c, parent, state);
        drawHorizontal(c, parent);
        drawVertical(c, parent);
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        /*int spanCount = getSpanCount(parent);
        int childCount = parent.getAdapter().getItemCount();
        int itemPosition = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewLayoutPosition();

        if (mBuilder.isExistHeadView)
            itemPosition -= 1;
        //set head divider offset
        //version 1.1 update
        if (mBuilder.isShowHeadDivider && itemPosition == -1)
            outRect.set(0, 0, 0, mBuilder.dividerHorSize);

        if (itemPosition < 0)
            return;

        int column = itemPosition % spanCount;
        int bottom = 0;

        int left = column * mBuilder.dividerVerSize / spanCount;
        int right = mBuilder.dividerVerSize - (column + 1) * mBuilder.dividerVerSize / spanCount;

        if (!(isLastRaw(parent, itemPosition, spanCount, childCount) && !mBuilder.isShowLastDivider))
            bottom = mBuilder.dividerHorSize;

        outRect.set(left, 0, right, bottom);
        marginOffsets(outRect, spanCount, itemPosition);*/
    }

    private void marginOffsets(Rect outRect, int spanCount, int itemPosition) {
        if (mBuilder.marginRight == 0 && mBuilder.marginLeft == 0) {
            return;
        }

        int itemShrink = (mBuilder.marginLeft + mBuilder.marginRight) / spanCount;
        outRect.left += (mBuilder.marginLeft - (itemPosition % spanCount) * itemShrink);

        outRect.right += ((itemPosition % spanCount) + 1) * itemShrink - mBuilder.marginLeft;
    }

    public static class Builder {
        private Context c;
        int horColor;
        int verColor;
        int dividerHorSize;
        int dividerVerSize;
        int marginLeft, marginRight;
        boolean isShowLastDivider = false;
        boolean isExistHeadView = false;
        boolean isShowHeadDivider = false;

        public Builder(Context c) {
            this.c = c;
        }

        /**
         * 设置divider的颜色
         *
         * @param color
         * @return
         */
        public Builder color(@ColorRes int color) {
            this.horColor = c.getResources().getColor(color);
            this.verColor = c.getResources().getColor(color);
            return this;
        }

        /**
         * 单独设置横向divider的颜色
         *
         * @param horColor
         * @return
         */
        public Builder horColor(@ColorRes int horColor) {
            this.horColor = c.getResources().getColor(horColor);
            return this;
        }

        /**
         * 单独设置纵向divider的颜色
         *
         * @param verColor
         * @return
         */
        public Builder verColor(@ColorRes int verColor) {
            this.verColor = c.getResources().getColor(verColor);
            return this;
        }

        /**
         * 设置divider的宽度
         *
         * @param size
         * @return
         */
        public Builder size(@Px int size) {
            this.dividerHorSize = size;
            this.dividerVerSize = size;
            return this;
        }

        /**
         * 设置横向divider的宽度
         *
         * @param horSize
         * @return
         */
        public Builder horSize(@Px int horSize) {
            this.dividerHorSize = horSize;
            return this;
        }

        /**
         * 设置纵向divider的宽度
         *
         * @param verSize
         * @return
         */
        public Builder verSize(@Px int verSize) {
            this.dividerVerSize = verSize;
            return this;
        }

        /**
         * 设置剔除HeadView的RecyclerView左右两边的外间距
         *
         * @param marginLeft
         * @param marginRight
         * @return
         */
        public Builder margin(@Px int marginLeft, @Px int marginRight) {
            this.marginLeft = marginLeft;
            this.marginRight = marginRight;
            return this;
        }

        /**
         * 最后一行divider是否需要顯示
         *
         * @param isShow
         * @return
         */
        public Builder showLastDivider(boolean isShow) {
            this.isShowLastDivider = isShow;
            return this;
        }

        /**
         * HeadView行divider是否需要顯示
         * Version 1.1 add
         *
         * @param isShow
         * @return
         */
        public Builder showHeadDivider(boolean isShow) {
            this.isShowHeadDivider = isShow;
            return this;
        }

        /**
         * 是否包含HeadView
         *
         * @param isExistHead
         * @return
         */
        public Builder isExistHead(boolean isExistHead) {
            this.isExistHeadView = isExistHead;
            return this;
        }

        /*public GridItemDecoration build() {
            return new GridItemDecoration(this);
        }*/

    }
}
