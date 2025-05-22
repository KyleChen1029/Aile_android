package tw.com.chainsea.chat.keyboard.media.adatper;

import android.animation.ObjectAnimator;
import android.graphics.Canvas;
import android.util.Log;
import android.util.Property;
import android.view.View;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

/**
 * current by evan on 2020-06-01
 *
 * @author Evan Wang
 * @date 2020-06-01
 */
public class PicDragCallback extends ItemTouchHelper.Callback {
    private static final String TAG = PicDragCallback.class.getSimpleName();

    //    private RecyclerView recyclerView;
    private MediaSelectorAdapter adapter;
    private int deltePosition = -1;
    private boolean isInside = false;
    private View triggerBoundary;

    private PicDragListener picDragListener;
    private RecyclerView.ViewHolder tempHolder;


    private float mScale = 1.2f;
    private float mAlpha = 1.0f;

    private float mInsideScale = 0.86f;
    private float mInsideAlpha = 0.3f;

    private float mMoveScale = mScale;

    private boolean isSwipedEnd = false;

    public PicDragCallback(RecyclerView recyclerView, @NonNull MediaSelectorAdapter adapter, View triggerBoundary) {
//        this.recyclerView = recyclerView;
        this.adapter = adapter;
        this.triggerBoundary = triggerBoundary;
    }

    public PicDragCallback(@NonNull MediaSelectorAdapter adapter, View triggerBoundary) {
//        this.recyclerView = recyclerView;
        this.adapter = adapter;
        this.triggerBoundary = triggerBoundary;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        Log.i(TAG, "getMovementFlags");
//        int dragFlags;

//        if (viewHolder instanceof MediaSelectorAdapter.PhotoItemViewHolder) {
//            return 0;
//        }
//        dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
//        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT; // 拖易
//        int swipeFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT; // 滑動


//        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
//        int swipeFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;


        int dragFlags = ItemTouchHelper.UP;
        int swipeFlags = ItemTouchHelper.UP;


//        int swipeFlags = 0;
        return makeMovementFlags(dragFlags, swipeFlags);
//        return makeMovementFlags(0, swipeFlags);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
//        Log.i(TAG, "onMove");
//        if (viewHolder.getItemViewType() != target.getItemViewType()) {
//            return false;
//        }
//        if (target instanceof MediaSelectorAdapter.PhotoItemViewHolder) {
//            return false;
//        }
//        List<String> list = this.adapter.getData();
//        if (list == null || list.size() < 2) {
//            return false;
//        }
//        int from = viewHolder.getAdapterPosition();
//        int endPosition = target.getAdapterPosition();
//        Log.d("jiabin", "onMove from:" + from + " end:" + endPosition);
//        this.deltrPosition = endPosition;
//        Collections.swap(list, from, endPosition);
//        this.adapter.notifyItemMoved(from, endPosition);
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        if (this.isInside) {
            int position = viewHolder.getAdapterPosition();
            viewHolder.itemView.post(() -> adapter.notifyItemChanged(position));
//            clearView(this.recyclerView, viewHolder);
        }
        Log.i(TAG, "onSwiped");
    }

    @Override
    public long getAnimationDuration(@NonNull RecyclerView recyclerView, int animationType, float animateDx, float animateDy) {
        Log.i(TAG, "getAnimationDuration");
        if (this.isInside) {
            Log.i(TAG, "getAnimationDuration return ");
            return 0;
        }
        return super.getAnimationDuration(recyclerView, animationType, animateDx, animateDy);
    }


    private void startActivatingAnim(View view, float from, float to, long duration) {
        Object tag = view.getTag();
        if (tag instanceof ObjectAnimator) {
            return;
        }
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, scaleProperty, from, to);
        animator.setDuration(duration);
        animator.start();
        view.setTag(animator);
    }

    private boolean isActivatingAniming(View view) {
        Object tag = view.getTag();
        if (tag instanceof ObjectAnimator) {
            ObjectAnimator animator = (ObjectAnimator) tag;
            return animator.isRunning();
        }
        return false;
    }

    private void clearActivatingAnim(View view) {
        Object tag = view.getTag();
        if (tag instanceof ObjectAnimator) {
            ObjectAnimator animator = (ObjectAnimator) tag;
            animator.cancel();
            view.setTag(null);
        }
    }

    private ScaleProperty scaleProperty = new ScaleProperty("scale");

    public static class ScaleProperty extends Property<View, Float> {
        public ScaleProperty(String name) {
            super(Float.class, name);
        }

        @Override
        public Float get(View object) {
            return object.getScaleX();
        }

        @Override
        public void set(View object, Float value) {
            object.setScaleX(value);
            object.setScaleY(value);
        }
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        Log.i(TAG, "onSelectedChanged");
        // 不在闲置状态
        //Log.d("jiabin", "onSelectedChanged:" + actionState);
        //mActionState = actionState;
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
//            viewHolder.itemView.setScaleX(mScale);
//            viewHolder.itemView.setScaleY(mScale);
            clearActivatingAnim(viewHolder.itemView);
//            startActivatingAnim(viewHolder.itemView, 1.0f, mScale, 200);
            viewHolder.itemView.setAlpha(mAlpha);
            if (picDragListener != null) picDragListener.onDragStart();
            this.deltePosition = viewHolder.getAdapterPosition();
            tempHolder = viewHolder;
            Log.d("jiabin", "onSelectedChanged delPos:" + this.deltePosition);
        } else {
            if (picDragListener != null) picDragListener.onDragFinish(this.isInside);
            if (this.isInside && this.deltePosition >= 0 && tempHolder != null) {
                this.isSwipedEnd = true;
//                clearActivatingAnim(tempHolder.itemView);
//                tempHolder.itemView.setScaleX(1.0f);
//                tempHolder.itemView.setScaleY(1.0f);
//                tempHolder.itemView.setAlpha(1.0f);
//                clearView(super.mRecyclerView);
//                tempHolder.itemView.setVisibility(View.INVISIBLE);
//                this.recyclerView
//                clearView(this.recyclerView, tempHolder);
                if (picDragListener != null) picDragListener.onTriggerBoundary(this.deltePosition);
//                Log.i(TAG, "clearView");
//                clearActivatingAnim(tempHolder.itemView);
//                startActivatingAnim(tempHolder.itemView, mScale, 1.0f, 150);
//        viewHolder.itemView.setScaleX(1.0f);
//        viewHolder.itemView.setScaleY(1.0f);
//                tempHolder.itemView.setAlpha(1.0f);
//                this.adapter.notifyItemChanged(delPos);
//                this.adapter.notifyDataSetChanged();
//                this.adapter.removeItemFromDrag(delPos);
                this.isInside = false;
            }
            this.deltePosition = -1;
            tempHolder = null;
        }
        super.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        Log.i(TAG, "onChildDraw");
//        if (triggerBoundary == null || isActivatingAniming(viewHolder.itemView)) {
//            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
//            Log.i(TAG, "onChildDraw return");
//            return;
//        }
        int delAreaWidth = triggerBoundary.getWidth();
        int delAreaHeight = triggerBoundary.getHeight();

        int[] delLocation = new int[2];
        triggerBoundary.getLocationInWindow(delLocation);
        int delAreaX = delLocation[0];
        int delAreaY = delLocation[1];

        int itemWidth = viewHolder.itemView.getWidth();
        int itemHeight = viewHolder.itemView.getHeight();
        int[] itemLocation = new int[2];
        viewHolder.itemView.getLocationInWindow(itemLocation);
        int itemX = itemLocation[0];
        int itemY = itemLocation[1];

        //Log.d("jiabin","itemWidth:" + itemWidth + " | itemHeight:" + itemHeight + " | itemX:" + itemX + " | itemY:" + itemY);

        int scaleItemWidth = (int) (itemWidth * mMoveScale);
        int scaleItemHeight = (int) (itemHeight * mMoveScale);

//        int scaleItemWidth = itemWidth;
//        int scaleItemHeight = itemHeight;

//        int itemRight = itemX + scaleItemWidth;
//        int itemBottom = itemY + scaleItemHeight;

        int centerX = itemX + scaleItemWidth / 2;
        int centerY = itemY + scaleItemHeight / 2;

        boolean isInside = false;
//        if (itemBottom > delAreaY && itemY < delAreaY + delAreaHeight && itemRight > delAreaX && itemX < delAreaX + delAreaWidth) {
//            isInside = true;
//        } else {
//            isInside = false;
//        }

//        Log.i(TAG, String.format("delAreaHeight:: %s"))

//        if (centerY > delAreaY && centerY < delAreaY + delAreaHeight && centerX > delAreaX && centerX < delAreaX + delAreaWidth) {
//            isInside = true;
//        } else {
//            isInside = false;
//        }

        isInside = centerY < delAreaY;
        if (isInside != this.isInside) {
            if (tempHolder != null) {
                if (isInside) {
                    mMoveScale = mInsideScale;
//                    viewHolder.itemView.setScaleX(mInsideScale);
//                    viewHolder.itemView.setScaleY(mInsideScale);
//                    clearActivatingAnim(viewHolder.itemView);
//                    startActivatingAnim(viewHolder.itemView, mScale, mInsideScale, 150);
//                    viewHolder.itemView.setAlpha(mInsideAlpha);
                    //viewHolder.itemView.clearAnimation();
                    //viewHolder.itemView.startAnimation(mInScaleAnim);
                } else {
                    mMoveScale = mScale;
//                    viewHolder.itemView.setScaleX(mScale);
//                    viewHolder.itemView.setScaleY(mScale);
//                    clearActivatingAnim(viewHolder.itemView);
//                    startActivatingAnim(viewHolder.itemView, mInsideScale, mScale, 150);
//                    viewHolder.itemView.setAlpha(mAlpha);
                    //viewHolder.itemView.clearAnimation();
                    //viewHolder.itemView.startAnimation(mOutScaleAnim);
                }
            }
            if (picDragListener != null) {
                picDragListener.onDragAreaChange(isInside, tempHolder == null);
            }
        }
        this.isInside = isInside;

        viewHolder.itemView.setAlpha(this.isSwipedEnd ? 0.0f : 1.0f);
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public void onMoved(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, int fromPos, @NonNull RecyclerView.ViewHolder target, int toPos, int x, int y) {
        Log.i(TAG, "onMoved");
        super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
    }

    @Override
    public void onChildDrawOver(@NonNull Canvas c, @NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        Log.i(TAG, "onChildDrawOver");
        super.onChildDrawOver(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        Log.i(TAG, "clearView");
        clearActivatingAnim(viewHolder.itemView);
        startActivatingAnim(viewHolder.itemView, mScale, 1.0f, 150);
//        viewHolder.itemView.setScaleX(1.0f);
//        viewHolder.itemView.setScaleY(1.0f);
        this.isSwipedEnd = false;
//        super.clearView(recyclerView, viewHolder);
    }


    @Override
    public boolean isLongPressDragEnabled() {
        // 支持长按拖拽功能
        return true;
    }

    //
    @Override
    public boolean isItemViewSwipeEnabled() {
        // 不支持滑动功能
        return true;
    }

    public interface PicDragListener {

        void onDragStart();

        void onDragFinish(boolean isInside);

        void onDragAreaChange(boolean isInside, boolean isIdle);

        void onTriggerBoundary(int position);
    }

    public void setDragListener(PicDragListener picDragListener) {
        this.picDragListener = picDragListener;
    }

    /**
     * 设置选中后的放大效果
     *
     * @param scale
     */
    public PicDragCallback setScale(float scale) {
        mScale = scale;
        mMoveScale = mScale;
        return this;

//        mInScaleAnim = new ScaleAnimation(1.0f, mInsideScale / mMoveScale, 1.0f, mInsideScale / mMoveScale,
//                Animation.RELATIVE_TO_SELF, 0.5f * mMoveScale, Animation.RELATIVE_TO_SELF, 1.5f * mMoveScale / mInsideScale);
//        mInScaleAnim.setFillAfter(true);
//        mInScaleAnim.setDuration(300);
//        mOutScaleAnim = new ScaleAnimation(mInsideScale / mMoveScale, 1.0f, mInsideScale / mMoveScale, 1.0f,
//                Animation.RELATIVE_TO_SELF, 0.5f * mMoveScale, Animation.RELATIVE_TO_SELF, 1.5f * mMoveScale / mInsideScale);
//        mOutScaleAnim.setDuration(300);
    }

    /**
     * 设置选中后的透明效果
     *
     * @param alpha
     */
    public PicDragCallback setAlpha(@FloatRange(from = 0.0f, to = 1.0f) float alpha) {
        mAlpha = alpha;
        return this;
    }
}
