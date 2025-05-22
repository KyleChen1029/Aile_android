package tw.com.chainsea.custom.view.recyclerview.itemdecoration;

import androidx.recyclerview.widget.RecyclerView;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * current by evan on 2020-04-01
 *
 * @author Evan Wang
 * @date 2020-04-01
 */
public class ParentItemTouchHelperExtension extends ItemTouchHelperExtension {


    private Set<SubItemTouchHelperExtension> subItemTouchHelperExtensions = Sets.newHashSet();

    /**
     * Creates an ItemTouchHelper that will work with the given Callback.
     * <p>
     * You can attach ItemTouchHelper to a RecyclerView via
     * {@link #attachToRecyclerView(RecyclerView)}. Upon attaching, it will add an item decoration,
     * an onItemTouchListener and a Child attach / detach listener to the RecyclerView.
     *
     * @param callback The Callback which controls the behavior of this touch helper.
     */
    public ParentItemTouchHelperExtension(Callback callback) {
        super(callback);
    }


    public void addChild(SubItemTouchHelperExtension child) {
        subItemTouchHelperExtensions.add(child);
    }


    @Override
    public void closeOpenedPreItem() {
        super.closeOpenedPreItem();
        for (SubItemTouchHelperExtension sub: subItemTouchHelperExtensions) {
            sub.closeOpenedPreItem();
        }
    }
}