package tw.com.chainsea.custom.view.banner;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import tw.com.chainsea.android.common.ui.UiHelper;

/**
 * current by evan on 2020-11-04
 *
 * @author Evan Wang
 * @date 2020-11-04
 */
public class CardLinearSnapHelper extends LinearSnapHelper {
    public boolean mNoNeedToScroll = false;
    public int[] finalSnapDistance = {0, 0};

    private int mLastPos;

    private int mPagePadding = 15; // 卡片的padding, 卡片间的距离等于2倍的mPagePadding
    private int mShowLeftCardWidth = 20;   // 左边卡片显示大小
    private int mCardWidth; // 卡片宽度
    private int mOnePageWidth; // 滑动一页的距离
    private int mCardGalleryWidth;

    private int mFirstItemPos;
    private int mCurrentItemOffset;

    BannerRecyclerView mRecyclerView;
    Context mContext;

    public void attachToRecyclerView(@Nullable BannerRecyclerView recyclerView) throws IllegalStateException {
        this.mRecyclerView = recyclerView;
        this.mContext = mRecyclerView.getContext();

        this.mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                int edgeMargin = (parent.getWidth() - view.getLayoutParams().width) / 2;

                int position = parent.getChildAdapterPosition(view);
                if (position == 0) {
                    outRect.left = edgeMargin;
                }
                if (position == state.getItemCount() - 1) {
                    outRect.right = edgeMargin;
                }
            }
        });
        addOnScrollListener(recyclerView);
        initWidth();
        super.attachToRecyclerView(recyclerView);
    }

    private void addOnScrollListener(BannerRecyclerView recyclerView) {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    mNoNeedToScroll = getCurrentItem() == 0 ||
                        getCurrentItem() == Objects.requireNonNull(recyclerView.getAdapter()).getItemCount() - 2;
                    if (finalSnapDistance[0] == 0
                        && finalSnapDistance[1] == 0) {
                        mCurrentItemOffset = 0;
                        mLastPos = getCurrentItem();
                        mRecyclerView.dispatchOnPageSelected(mLastPos);
                    }
                } else {
                    mNoNeedToScroll = false;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mCurrentItemOffset += dx;
//                onScrolledChangedCallback();
            }
        });
    }

    private void initWidth() {
        mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mRecyclerView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                mCardGalleryWidth = mRecyclerView.getWidth();
                mCardWidth = mCardGalleryWidth - UiHelper.dip2px(mContext, 2 * (mPagePadding + mShowLeftCardWidth));
                mOnePageWidth = mCardWidth;
//                scrollToPosition(mFirstItemPos);
            }
        });
    }

    public void setCurrentItem(int item, boolean smoothScroll) {
        if (mRecyclerView == null) {
            return;
        }

//        initWidth();
        if (smoothScroll) {
            mRecyclerView.smoothScrollToPosition(item);
        } else {
            scrollToPosition(item);
        }
    }


    public void scrollToPosition(int pos) {
        if (mRecyclerView == null) {
            return;
        }
        ((LinearLayoutManager) mRecyclerView.getLayoutManager()).scrollToPositionWithOffset(pos, UiHelper.dip2px(mContext, (mPagePadding + mShowLeftCardWidth) / 1.5f));
        mCurrentItemOffset = 0;
        mLastPos = pos;
        mRecyclerView.dispatchOnPageSelected(mLastPos);
    }

    public int getCurrentItem() {
        return mRecyclerView.getLayoutManager().getPosition(findSnapView(mRecyclerView.getLayoutManager()));
    }

    @Override
    public int[] calculateDistanceToFinalSnap(@NonNull RecyclerView.LayoutManager layoutManager, @NonNull View targetView) {
        if (mNoNeedToScroll) {
            finalSnapDistance[0] = 0;
            finalSnapDistance[1] = 0;
        } else {
            finalSnapDistance = super.calculateDistanceToFinalSnap(layoutManager, targetView);
        }
        return finalSnapDistance;
    }
}
