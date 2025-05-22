package tw.com.chainsea.custom.view.recyclerview.itemdecoration;

import androidx.recyclerview.widget.RecyclerView;

/**
 * current by evan on 2020-04-01
 *
 * @author Evan Wang
 * @date 2020-04-01
 */
public class SubItemTouchHelperExtension extends ItemTouchHelperExtension {


    /**
     * Creates an ItemTouchHelper that will work with the given Callback.
     * <p>
     * You can attach ItemTouchHelper to a RecyclerView via
     * {@link #attachToRecyclerView(RecyclerView)}. Upon attaching, it will add an item decoration,
     * an onItemTouchListener and a Child attach / detach listener to the RecyclerView.
     *
     * @param callback The Callback which controls the behavior of this touch helper.
     */
    public SubItemTouchHelperExtension(Callback callback) {
        super(callback);
    }


    private ItemTouchHelperExtension parentItemTouchHelperExtension = null;


    public void setParent(ItemTouchHelperExtension parent) {
        this.parentItemTouchHelperExtension = parent;
    }


    @Override
    public void closeOpenedPreItem() {
        super.closeOpenedPreItem();
        if (parentItemTouchHelperExtension != null) {
//            parentItemTouchHelperExtension.closeOpenedPreItem();
        }
    }

}
