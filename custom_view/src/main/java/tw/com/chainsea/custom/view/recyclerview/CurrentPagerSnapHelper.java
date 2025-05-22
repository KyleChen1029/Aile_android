package tw.com.chainsea.custom.view.recyclerview;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

/**
 * current by evan on 2020-04-14
 *
 * @author Evan Wang
 * @date 2020-04-14
 */
public class CurrentPagerSnapHelper extends PagerSnapHelper {
    private static final String TAG = CurrentPagerSnapHelper.class.getSimpleName();
    OnCurrentPagerSnapListener onCurrentPagerSnapListener;

    public CurrentPagerSnapHelper() {
        super();
    }

    @Nullable
    @Override
    public int[] calculateDistanceToFinalSnap(@NonNull RecyclerView.LayoutManager layoutManager, @NonNull View targetView) {
        if (this.onCurrentPagerSnapListener != null) {
            int current = ((LinearLayoutManager) layoutManager).findFirstCompletelyVisibleItemPosition();
            if (current != -1) {
                this.onCurrentPagerSnapListener.onCurrentPosition(current);
            }
        }
        return super.calculateDistanceToFinalSnap(layoutManager, targetView);
    }

    public CurrentPagerSnapHelper setUpRecyclerView(@Nullable RecyclerView recyclerView) {
        super.attachToRecyclerView(recyclerView);
        return this;
    }


    public CurrentPagerSnapHelper setOnCurrentPagerSnapListener(OnCurrentPagerSnapListener onCurrentPagerSnapListener) {
        this.onCurrentPagerSnapListener = onCurrentPagerSnapListener;
        return this;
    }

    @Nullable
    @Override
    public View findSnapView(RecyclerView.LayoutManager layoutManager) {
        int current = ((LinearLayoutManager) layoutManager).findFirstCompletelyVisibleItemPosition();
        return super.findSnapView(layoutManager);
    }

    @Override
    public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
        return super.findTargetSnapPosition(layoutManager, velocityX, velocityY);
    }

    @Override
    protected LinearSmoothScroller createSnapScroller(RecyclerView.LayoutManager layoutManager) {
        return super.createSnapScroller(layoutManager);
    }

    public interface OnCurrentPagerSnapListener {
        void onCurrentPosition(int position);

    }
}