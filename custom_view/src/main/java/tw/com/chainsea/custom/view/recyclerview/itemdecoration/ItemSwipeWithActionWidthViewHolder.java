package tw.com.chainsea.custom.view.recyclerview.itemdecoration;

import android.view.View;

/**
 * current by evan on 2020-04-01
 *
 * @author Evan Wang
 * date 2020-04-01
 */
public abstract class ItemSwipeWithActionWidthViewHolder<T> extends ItemBaseViewHolder<T> implements Extension {

    public ItemSwipeWithActionWidthViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public float getActionWidth() {
        return getRightMenu().getWidth();
    }

    @Override
    public float getLeftMenuActionWidth() {
        return getLeftMenu().getWidth();
    }

    @Override
    public float getRightMenuActionWidth() {

        return getRightMenu().getWidth();
    }
}
