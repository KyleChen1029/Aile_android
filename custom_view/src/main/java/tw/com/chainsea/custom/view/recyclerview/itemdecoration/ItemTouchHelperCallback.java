package tw.com.chainsea.custom.view.recyclerview.itemdecoration;

import android.graphics.Canvas;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import android.view.View;

/**
 * current by evan on 2020-04-01
 *
 * @author Evan Wang
 * @date 2020-04-01
 */
public class ItemTouchHelperCallback extends ItemTouchHelperExtension.Callback {
    private static final String TAG = ItemTouchHelperCallback.class.getSimpleName();
    OnMoveListener onMoveListener;
    int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
    int flag = -1;
    String tag = "";

    public ItemTouchHelperCallback(int swipeFlags) {
        this.swipeFlags = swipeFlags;
    }

    public ItemTouchHelperCallback setFlag(int flag) {
        this.flag = flag;
        return this;
    }

    public ItemTouchHelperCallback setTag(String tag) {
        this.tag = tag;
        return this;
    }


    public ItemTouchHelperCallback setOnMoveListener(OnMoveListener onMoveListener) {
        this.onMoveListener = onMoveListener;
        return this;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof ItemNoSwipeViewHolder) {
            return 0;
        }
        return makeMovementFlags(0, swipeFlags);
//        return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
//        RecyclerView.Adapter adapter = (recyclerView.getAdapter());
//        adapter.move(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        if (onMoveListener != null) {
            onMoveListener.move(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        }
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
//        Log.i(TAG, "onSwiped::");
        if (onMoveListener != null) {
            onMoveListener.swiped(flag, tag, viewHolder, direction);
        }
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (dY != 0 && dX == 0) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
        if (viewHolder instanceof ItemBaseViewHolder) {
            ItemBaseViewHolder holder = (ItemBaseViewHolder) viewHolder;
//        if (viewHolder instanceof ItemSwipeWithActionWidthNoSpringViewHolder) {
//            if (dX < -holder.mActionContainer.getWidth()) {
//                dX = -holder.mActionContainer.getWidth();
//            } else if (dX > holder.mActionContainer.getWidth()) {
//                dX = holder.mActionContainer.getWidth();
//            }
//            if (onMoveListener != null){
//                onMoveListener.setTranslationX(dX);
//            }
////            holder.mViewContent.setTranslationX(dX);
//            return;
//        }

            // 滑動固定，有彈簧
            if (viewHolder instanceof ItemSwipeWithActionWidthViewHolder && holder.getContentItem() != null) {
                holder.getContentItem().setTranslationX(dX);
                holder.getLeftMenu().setAlpha(dX < 0 ? 0.0f : 1.0f);

                holder.getRightMenu().setAlpha(dX < 0 ? 1.0f : 0.0f);
                holder.getLeftMenu().setVisibility(dX < 0 ? View.GONE : View.VISIBLE);
                holder.getRightMenu().setVisibility(dX < 0 ? View.VISIBLE : View.GONE);
                if (dX < 0) {
                    holder.getRightMenu().setAlpha(Math.abs(dX * 3) / holder.getRightMenu().getWidth());
                } else if (dX > 0) {
                    holder.getLeftMenu().setAlpha(Math.abs(dX * 3) / holder.getLeftMenu().getWidth());
                } else {
                    holder.getLeftMenu().setAlpha(0.0f);
                    holder.getRightMenu().setAlpha(0.0f);
                }
                return;
            }

            if (viewHolder != null && holder.getContentItem() != null) {
                holder.getContentItem().setTranslationX(dX);
            }
        }
    }


    public interface OnMoveListener {
        void move(int from, int to);

        void swiped(int flag, String tag, RecyclerView.ViewHolder viewHolder, int direction);
    }
}