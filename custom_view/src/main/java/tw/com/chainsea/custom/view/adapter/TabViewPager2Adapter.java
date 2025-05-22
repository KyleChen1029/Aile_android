package tw.com.chainsea.custom.view.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * current by evan on 2020-07-13
 *
 * @author Evan Wang
 * @date 2020-07-13
 */
public class TabViewPager2Adapter<T extends Fragment> extends FragmentStateAdapter {
    private List<T> fragments = Lists.newArrayList();

    public TabViewPager2Adapter(FragmentActivity fa) {
        super(fa);
    }

    public TabViewPager2Adapter removeAll() {
        this.fragments = Lists.newArrayList();
        return this;
    }


    public TabViewPager2Adapter add(T t) {
        this.fragments.add(t);
        return this;
    }

    public TabViewPager2Adapter add(List<T> list) {
        this.fragments.addAll(list);
        return this;
    }

    public TabViewPager2Adapter add(int index , T t) {
        this.fragments.add(index, t);
        return this;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }

    public void setData(List<T> newFragments) {
        DiffCallback callback = new DiffCallback(fragments, newFragments);
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(callback);

        fragments.clear();
        fragments.addAll(newFragments);

        diff.dispatchUpdatesTo(this);
    }

    private class DiffCallback extends DiffUtil.Callback {

        List<T> oldData;
        List<T> newData;

        public DiffCallback(
                List<T> oldData,
                List<T> newData
        ) {
            this.oldData = oldData;
            this.newData = newData;
        }

        @Override
        public int getOldListSize() {
            return oldData.size();
        }

        @Override
        public int getNewListSize() {
            return newData.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldData.get(oldItemPosition) == newData.get(newItemPosition);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return oldData.get(oldItemPosition).hashCode() == newData.get(newItemPosition).hashCode();
        }
    }
}
