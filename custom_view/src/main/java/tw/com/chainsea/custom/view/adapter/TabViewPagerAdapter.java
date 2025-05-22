package tw.com.chainsea.custom.view.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * current by evan on 2020-07-13
 *
 * @author Evan Wang
 * @date 2020-07-13
 */
public class TabViewPagerAdapter<T extends Fragment> extends FragmentStatePagerAdapter {
    private List<T> fragments = Lists.newArrayList();


    public TabViewPagerAdapter(FragmentManager fm, List<T> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    @Override
    @NonNull
    public T getItem(int position) {
        return fragments.get(position); //does not happen
    }


    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        super.destroyItem(container, position, object);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    public TabViewPagerAdapter removeAll() {
        this.fragments = Lists.newArrayList();
        return this;
    }


    public TabViewPagerAdapter add(T t) {
        this.fragments.add(t);
        return this;
    }

    public TabViewPagerAdapter add(List<T> list) {
        this.fragments.addAll(list);
        return this;
    }

    public TabViewPagerAdapter add(int index, T t) {
        this.fragments.add(index, t);
        return this;
    }

//    @Override
//    public void notifyDataSetChanged() {
////        super.notifyDataSetChanged();
//    }
}
